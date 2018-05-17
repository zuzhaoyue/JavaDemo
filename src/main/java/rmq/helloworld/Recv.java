package rmq.helloworld;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by zuzhaoyue on 18/5/15.
 */
public class Recv {
    private final static String QUEUE_NAME = "hello1";
    public static void main(String[] argv) throws java.io.IOException,
            java.lang.InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //以下的defaultconsumer实现了consumer这个接口,这个接口被用来缓冲服务器推送过来的信息
        //一开始的set up和刚刚的send.java里的相似:1.打开一个连接,2.声明一个队列（这个队列名要和刚刚的队列名相同）
        //注意:我们在这里声明队列,因为我们可能在生产者之前开始消费
        //我们告诉服务器从队列向我们传送消息,既然它会异步传送,我们以对象的形式提供一个回调,来缓冲这些消息,直到我们准备使用它们,这正是defaultconsumer做的事情
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        String  result = channel.basicConsume(QUEUE_NAME, true, consumer);
        System.out.println("result:" + result);

        //最后,我们关系连接和队列
        channel.close();
        connection.close();

    }
}
