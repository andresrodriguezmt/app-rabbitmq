package co.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import co.rabbitmq.Receiver;
import co.rabbitmq.Producer;
import co.rabbitmq.Conductor;
import co.rabbitmq.Cliente;
import co.rabbitmq.Asignacion;

import java.util.*;
import java.util.concurrent.*;

public class Servidor {

    private final Producer producer;
    private final Receiver receiver;
    private final ObjectMapper mapper = new ObjectMapper();

    private final ArrayList<Conductor> listaConductores = new ArrayList<>();
    private final ArrayList<String> listaClientes = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private static final String COLA_SOLICITUDES_VIAJE = "solicitudes_viaje";
    private static final String COLA_SOLICITUDES_CONDUCTORES = "solicitudes_conductores";
    private static final String COLA_ASIGNACION_CONDUCTOR = "asignacion_conductor";
    private static final String COLA_REGISTRO_CLIENTES = "registro_clientes";
    private static final String COLA_CONFIRMACION_CONDUCTOR = "confirmacion_conductor";

    private static final String EXCHANGE_ASIGNACIONES_ZONAS = "asignaciones_zonas";
    private static final String ROUTING_KEY_NORTE = "zona.norte";
    private static final String ROUTING_KEY_SUR = "zona.sur";
    private static final String ROUTING_KEY_CENTRO = "zona.centro";

    private static final String EXCHANGE_ANUNCIOS = "anuncios";

    private static final String ESTADO_LIBRE = "Libre";

    public Servidor(Producer producer, Receiver receiver) {
        this.producer = producer;
        this.receiver = receiver;
    }

    public void iniciar() {
        try {

            executor.submit(() -> {
                try {
                    receiver.listenQueue(COLA_SOLICITUDES_VIAJE, (queue, mensaje) -> {
                        procesarSolicitudCliente(mensaje);
                    });
                    System.out.println("✅ Escuchando cola de clientes: " + COLA_SOLICITUDES_VIAJE);
                } catch (Exception e) {
                    System.err.println("Error escucha clientes: " + e.getMessage());
                }
            });


            executor.submit(() -> {
                try {
                    receiver.listenQueue(COLA_SOLICITUDES_CONDUCTORES, (queue, mensaje) -> {
                        procesarSolicitudConductor(mensaje);
                    });
                    System.out.println("✅ Escuchando cola de conductores: " + COLA_SOLICITUDES_CONDUCTORES);
                } catch (Exception e) {
                    System.err.println("Error escucha conductores: " + e.getMessage());
                }
            });


            executor.submit(() -> {
                try {
                    receiver.listenQueue(COLA_REGISTRO_CLIENTES, (queue, mensaje) -> {
                        procesarRegistroCliente(mensaje);
                    });
                    System.out.println("✅ Escuchando cola de registro clientes: " + COLA_REGISTRO_CLIENTES);
                } catch (Exception e) {
                    System.err.println("Error escucha registro clientes: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error iniciando servidor: " + e.getMessage());
        }
    }


    public void enviarAnuncio(String mensaje) {
        try {
            // Crear estructura del anuncio
            Map<String, String> anuncio = new HashMap<>();
            anuncio.put("tipo", "anuncio");
            anuncio.put("mensaje", mensaje);
            anuncio.put("timestamp", new Date().toString());
            anuncio.put("titulo", "¡¡¡Importante anuncio!!!");

            String jsonAnuncio = mapper.writeValueAsString(anuncio);

            // Enviar al exchange de tipo fanout (sin routing key)
            enviarMensajeExchange(EXCHANGE_ANUNCIOS, "fanout", "", jsonAnuncio);

            System.out.println("📢 Anuncio enviado a todos los usuarios: " + mensaje);

        } catch (Exception e) {
            System.err.println("❌ Error enviando anuncio: " + e.getMessage());
        }
    }


    private void procesarSolicitudCliente(String mensaje) {
        try {
            Cliente cliente = mapper.readValue(mensaje, Cliente.class);
            System.out.println("🧍 Solicitud recibida del cliente: " + cliente.getNombre() + " - Zona: " + cliente.getZona());
            asignarConductor(cliente);
        } catch (JsonProcessingException e) {
            System.err.println("Error procesando solicitud cliente: " + e.getMessage());
        }
    }


    private void procesarSolicitudConductor(String mensaje) {
        try {
            Conductor conductor = mapper.readValue(mensaje, Conductor.class);
            conductor.setDisponibilidad(ESTADO_LIBRE);
            listaConductores.add(conductor);
            System.out.println("🚗 Conductor registrado: " + conductor.getNombre() + " - Zona: " + conductor.getZona());


            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("nombre", conductor.getNombre()); // Incluir nombre para filtrado
            respuesta.put("estado", "registrado");
            respuesta.put("mensaje", "Tu registro fue exitoso. Bienvenido a Luxury Ride 🚘");
            respuesta.put("zona", conductor.getZona());

            String jsonRespuesta = mapper.writeValueAsString(respuesta);
            enviarMensajeCola(COLA_CONFIRMACION_CONDUCTOR, jsonRespuesta);

            System.out.println("📩 Confirmación enviada para conductor: " + conductor.getNombre());

        } catch (Exception e) {
            System.err.println("Error procesando conductor: " + e.getMessage());
        }
    }


    private void procesarRegistroCliente(String mensaje) {
        try {
            String nombreCliente = mapper.readValue(mensaje, String.class);
            if (!listaClientes.contains(nombreCliente)) {
                listaClientes.add(nombreCliente);
                System.out.println("🧾 Cliente registrado: " + nombreCliente);
            } else {
                System.out.println("⚠️ Cliente ya estaba registrado: " + nombreCliente);
            }
        } catch (Exception e) {
            System.err.println("Error procesando registro cliente: " + e.getMessage());
        }
    }


    private void asignarConductor(Cliente cliente) {
        // Buscar conductores disponibles en la misma zona que el cliente
        Optional<Conductor> conductorLibre = listaConductores.stream()
                .filter(c -> ESTADO_LIBRE.equals(c.getDisponibilidad()))
                .filter(c -> c.getZona() != null && c.getZona().equalsIgnoreCase(cliente.getZona()))
                .findFirst();

        if (conductorLibre.isEmpty()) {
            System.out.println("❌ No hay conductores disponibles en zona " + cliente.getZona() + " para: " + cliente.getNombre());
            enviarMensajeCola(COLA_ASIGNACION_CONDUCTOR, "No hay conductores disponibles en tu zona");
            return;
        }

        Conductor conductor = conductorLibre.get();
        conductor.setDisponibilidad("Ocupado");

        Asignacion asignacion = new Asignacion(cliente, conductor);

        try {
            String json = mapper.writeValueAsString(asignacion);

            String routingKey = obtenerRoutingKeyPorZona(conductor.getZona());
            if (routingKey != null) {
                enviarMensajeExchange(EXCHANGE_ASIGNACIONES_ZONAS, "topic", routingKey, json);
                System.out.println("✅ Asignado " + conductor.getNombre() + " → " + cliente.getNombre() +
                        " - Zona: " + conductor.getZona() + " - Routing Key: " + routingKey);
            } else {
                System.err.println("❌ Zona no válida: " + conductor.getZona());
            }


            enviarMensajeCola(COLA_ASIGNACION_CONDUCTOR, json);

        } catch (JsonProcessingException e) {
            System.err.println("Error enviando asignación: " + e.getMessage());
        }
    }


    private String obtenerRoutingKeyPorZona(String zona) {
        if (zona == null) return null;

        switch (zona.toLowerCase()) {
            case "norte":
                return ROUTING_KEY_NORTE;
            case "sur":
                return ROUTING_KEY_SUR;
            case "centro":
                return ROUTING_KEY_CENTRO;
            default:
                return null;
        }
    }


    private void enviarMensajeCola(String cola, String mensaje) {
        try {
            producer.sendToQueue(cola, mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando mensaje a " + cola + ": " + e.getMessage());
        }
    }


    private void enviarMensajeExchange(String exchange, String exchangeType, String routingKey, String mensaje) {
        try {
            producer.sendToExchange(exchange, exchangeType, routingKey, mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando mensaje a exchange " + exchange + " con routing key " + routingKey + ": " + e.getMessage());
        }
    }
    public List<String> getListaConductores() {
        List<String> nombres = new ArrayList<>();
        for (Conductor c : listaConductores) {
            nombres.add(c.getNombre() + " - Zona: " + c.getZona());
        }
        return nombres;
    }
    public List<String> getListaClientes() {
        return new ArrayList<>(listaClientes);
    }
}

