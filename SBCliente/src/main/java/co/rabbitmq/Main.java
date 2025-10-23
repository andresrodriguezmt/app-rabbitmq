package co.rabbitmq;

public class Main {
    private static final String host = "localhost";
    private static final int puerto = 5672;
    private static final String usuario = "guest";
    private static final String contrasenia = "guest";
    private static final String virtualHost = "apprabbit";

    public static void main(String[] args) throws Exception{
        try {
            Producer producer = new Producer(host, puerto, usuario, contrasenia, virtualHost);
            Receiver receiver = new Receiver(host, puerto, usuario, contrasenia, virtualHost);

            new ClienteApp(producer, receiver);
        }catch (Exception e){
            // excepcion
            e.printStackTrace();
        }

    }
}
