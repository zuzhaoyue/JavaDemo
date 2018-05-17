//package rmq.workqueues;

/**
 * Created by zuzhaoyue on 18/5/16.
 */
import com.rabbitmq.client.*;

import java.io.IOException;

public class ReceiveLogs {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //上个例子中我们是向一个队列中发送消息,接收方也是从一个队列中获取,那种情况下给队列命名是很重要的,因为你需要生产者和消费者共享这个队列
        //但是这个例子里,则不需要给队列命名,首先看下需求:即时读取日志,可以看出日志系统需要的是即时性,那些旧的日志我们不需要看,所以我们必须满足以下两点
        //1.每次连接rmq时我们都需要一个新的空的队列,这个可以用随机给队列命名并创建来实现,或者更棒的方式是,让rmq服务器自己随机选择一个名字给我们
        //2.当我们关闭与rmq的连接时,这个队列得自动删除
        //当然,这个已经有封装好的方法了哈哈:channel.queueDeclare().getQueue()方法,可以创建一个暂时的,独立的,可自动删除并随机命名的队列
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        System.out.println("队列名称:" + queueName);
        //现在我们已经有了一个exchange,下一步就是让exchange向队列发送消息,exchange与队列之间的关系也叫做binging(绑定)
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
