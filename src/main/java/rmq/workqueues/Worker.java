//package rmq.workqueues;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by zuzhaoyue on 18/5/15.
 */
public class Worker {
    private final static String QUEUE_NAME = "hello0517";
    public static void main(String[] argv) throws IOException,
            InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        int prefetchCount = 1;
        channel.basicQos(prefetchCount);//代表让服务器不要同时给一个消费者超过1个消息,直到当前的消息被消耗掉

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                try{
                    doWork(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("[x] Done");
                    long tag = envelope.getDeliveryTag();//该消息的index
                    boolean multiple = false;//是否批量,true:一次性ack所有小于等于tag的消息,false:只ack index为tag的消息
                    channel.basicAck(tag, multiple);

                }
            }
        };
        // autoack改为false,打开manaul message ack
        // autoack 值为true代表只要发出的消息都自动有一个ack
        // 值false代表服务器会等待明确的ack,而不是自动返回的
        // 英文版:
        // true if the server should consider messages
        //* acknowledged once delivered;
        // false if the server should expect
        //* explicit acknowledgements
        boolean autoAck = false;
        String result = channel.basicConsume(QUEUE_NAME, autoAck, consumer);
        System.out.println("result:" + result);


    }
    //模拟执行任务的方法,一个点代表一秒
    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.') Thread.sleep(1000);
        }
    }
}
