package co.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Producer implements AutoCloseable {
    private final ConnectionFactory factory;

    public Producer(String host, int port, String username, String password, String virtualHost) {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        if (virtualHost != null) factory.setVirtualHost(virtualHost);
    }
    public void sendToExchange(String exchange, String exchangeType, String routingKey, String message) throws Exception {
        try (Connection conn = factory.newConnection();
             Channel ch = conn.createChannel()) {


            ch.exchangeDeclare(exchange, exchangeType, true);

            ch.basicPublish(exchange, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void sendToQueue(String queueName, String message) throws Exception {
        try (Connection conn = factory.newConnection();
             Channel ch = conn.createChannel()) {

            ch.queueDeclare(queueName, true, false, false, null);
            ch.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void close() {
        // nothing to close because we open/close per send; if quieres mantener conexión, cambia diseño
    }
}
