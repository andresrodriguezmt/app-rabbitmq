package co.rabbitmq;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public class Receiver implements AutoCloseable {
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    // track consumers to allow cancel
    private final Map<String, String> consumerTags = new ConcurrentHashMap<>();
    public Receiver(String host, int port, String username, String password, String virtualHost) throws Exception {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        if (virtualHost != null) factory.setVirtualHost(virtualHost);
        connect();
    }

    private void connect() throws Exception {
        connection = factory.newConnection();
        channel = connection.createChannel();
    }
    public void listenQueue(String queueName, BiConsumer<String, String> callback) throws IOException {
        // ensure queue declared
        channel.queueDeclare(queueName, true, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            callback.accept(queueName, message);
        };

        String consumerTag = channel.basicConsume(queueName, true, deliverCallback, consumerTag1 -> {});
        consumerTags.put(queueName, consumerTag);
    }
    public void stopListeningQueue(String queueName) throws IOException {
        String tag = consumerTags.remove(queueName);
        if (tag != null && channel != null && channel.isOpen()) {
            channel.basicCancel(tag);
        }
    }
    public void bindQueueToExchange(String queueName, String exchange, String routingKey, String exchangeType) throws IOException {
        channel.exchangeDeclare(exchange, exchangeType, true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchange, routingKey);
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }
}
