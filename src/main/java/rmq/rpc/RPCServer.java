//package rmq.rpc;

/**
 * Created by zuzhaoyue on 18/5/18.
 */
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n ==0) return 0;
        if (n == 1) return 1;
        return fib(n-1) + fib(n-2);
    }

    public static void main(String[] argv) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection  = factory.newConnection();
            final Channel channel = connection.createChannel();

            //准备接收
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            //一次只处理一个消息
            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())//收到clent发给server的暗号,默默记下来再发回去
                            .build();

                    String response = "";

                    try {
                        String message = new String(body,"UTF-8");//收到消息,计算出来准备发回去
                        int n = Integer.parseInt(message);

                        System.out.println(" [.] fib(" + message + ")");
                        response += fib(n);
                    }
                    catch (RuntimeException e){
                        System.out.println(" [.] " + e.toString());
                    }
                    finally {
                        //发回去啦,队列名是接收时指定的队列名properties.getReplyTo(),参数replyProps里带上corrid
                        String queueName = properties.getReplyTo();
                        System.out.println("===返回的队列名:" + queueName);
                        channel.basicPublish( "", queueName, replyProps, response.getBytes("UTF-8"));
                        long tag = envelope.getDeliveryTag();
                        System.out.println("===tag:" + tag);
                        channel.basicAck(envelope.getDeliveryTag(), false);//是否批量,true:一次性ack所有小于等于tag的消息,false:只ack index为tag的消息
                        // RabbitMq consumer worker thread notifies the RPC server owner thread
                        synchronized(this) {
                            this.notify();
                        }
                    }
                }
            };

            //消费rpc_queue里的任务
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized(consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {}
        }
    }
}
