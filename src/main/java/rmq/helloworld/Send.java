//package rmq.helloworld;
/**
 * Created by zuzhaoyue on 18/5/15.
 */
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
public class Send {
    private final static String QUEUE_NAME = "hello2";

    public static void main(String[] argv) throws Exception {
        //创建一个连接服务器的连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //为了发送信息,我们必须声明一个队列,然后才可以向这个队列中发消息,这个声明是幂等的,也就是说仅会在这个队列不存在时,才会创建一个队列。

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");

        //最后,我们关系连接和队列
        channel.close();
        connection.close();
    }

}
