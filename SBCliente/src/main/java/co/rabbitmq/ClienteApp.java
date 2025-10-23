package co.rabbitmq;


import com.fasterxml.jackson.databind.ObjectMapper;

import co.rabbitmq.Cliente;
import co.rabbitmq.Conductor;
import co.rabbitmq.Asignacion;
import co.rabbitmq.Producer;
import co.rabbitmq.Receiver;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClienteApp extends JFrame implements ActionListener {

    private final Producer producer;
    private final Receiver receiver;
    private final ObjectMapper mapper = new ObjectMapper();

    // --- Colores y fuentes ---
    private final Color COLOR_VINOTINTO = new Color(120, 16, 52);
    private final Color COLOR_VINOTINTO_SUAVE = new Color(160, 40, 80);
    private final Color COLOR_DORADO = new Color(212, 175, 55);
    private final Color COLOR_BEIGE = new Color(245, 240, 230);
    private final Color COLOR_BORDO_OSCURO = new Color(80, 10, 35);
    private final Color COLOR_EXITO = new Color(60, 140, 60);
    private final Color COLOR_ERROR = new Color(180, 50, 50);

    // Fuentes compactas
    private final Font FUENTE_TITULO = new Font("DejaVu Sans", Font.BOLD, 20);
    private final Font FUENTE_SUBTITULO = new Font("DejaVu Sans", Font.BOLD, 14);
    private final Font FUENTE_NORMAL = new Font("DejaVu Sans", Font.PLAIN, 12);
    private final Font FUENTE_BOTON = new Font("DejaVu Sans", Font.BOLD, 13);
    private final Font FUENTE_MENSAJES = new Font("DejaVu Sans", Font.PLAIN, 11);

    // --- Componentes UI ---
    private JPanel panelPrincipal;
    private JLabel labelStatus;
    private JTextField nombreField, origenField, destinoField;
    private JComboBox<String> zonaComboBox;
    private JButton enviarBtn, limpiarBtn;
    private JTextArea mensajesArea;

    // --- Colas ---
    private static final String COLA_SOLICITUDES = "solicitudes_viaje";
    private static final String COLA_ASIGNACION = "asignacion_conductor";
    private static final String COLA_REGISTRO_CLIENTES = "registro_clientes";
    private String nombreCliente;

    public ClienteApp(Producer producer, Receiver receiver) {
        this.producer = producer;
        this.receiver = receiver;

        solicitarNombreCliente();
        configurarVentana();
        inicializarComponentes();
        configurarLayout();
        visualizarVentana();
        escucharAsignaciones();
        escucharAnuncios();
    }

    private void solicitarNombreCliente() {
        nombreCliente = JOptionPane.showInputDialog(null,
                "Ingrese su nombre para registrarse:",
                "Registro de Cliente",
                JOptionPane.PLAIN_MESSAGE);

        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Debe ingresar un nombre válido para continuar.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        nombreCliente = nombreCliente.trim();

        try {
            // Solo enviamos el nombre en formato JSON
            String json = mapper.writeValueAsString(nombreCliente);
            producer.sendToQueue(COLA_REGISTRO_CLIENTES, json);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error registrando cliente: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    // ------------------ Configuración UI ------------------
    private void configurarVentana() {
        setTitle("🚕 Cliente: " + nombreCliente);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 600));
        setPreferredSize(new Dimension(400, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BEIGE);
    }

    private void inicializarComponentes() {
        panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.setBackground(COLOR_BEIGE);

        JPanel panelHeader = crearPanelHeader();
        JPanel panelFormulario = crearPanelFormulario();
        JPanel panelMensajes = crearPanelMensajes();
        JPanel panelFooter = crearPanelFooter();

        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        panelCentro.setBackground(COLOR_BEIGE);
        panelCentro.add(panelFormulario, BorderLayout.NORTH);
        panelCentro.add(panelMensajes, BorderLayout.CENTER);

        panelPrincipal.add(panelHeader, BorderLayout.NORTH);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        panelPrincipal.add(panelFooter, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    private JPanel crearPanelHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BEIGE);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel labelIconoTitulo = new JLabel("🚖");
        labelIconoTitulo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        labelIconoTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel labelTitulo = new JLabel("Luxury Ride Service");
        labelTitulo.setFont(FUENTE_TITULO);
        labelTitulo.setForeground(COLOR_VINOTINTO);
        labelTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel labelSubtitulo = new JLabel("Solicitud de Viaje");
        labelSubtitulo.setFont(FUENTE_SUBTITULO);
        labelSubtitulo.setForeground(COLOR_VINOTINTO_SUAVE);
        labelSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(COLOR_BEIGE);
        panelTitulo.add(labelTitulo, BorderLayout.NORTH);
        panelTitulo.add(labelSubtitulo, BorderLayout.SOUTH);

        panel.add(labelIconoTitulo, BorderLayout.WEST);
        panel.add(panelTitulo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_VINOTINTO, 1, true),
                        "📋 Información del Viaje",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_VINOTINTO
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 🔸 Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        JLabel labelNombre = crearEtiqueta("Nombre:");
        panel.add(labelNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        nombreField = crearTextField("");
        nombreField.setText(nombreCliente);
        nombreField.setEnabled(false);
        nombreField.setForeground(Color.BLACK);
        panel.add(nombreField, gbc);

        // 🔸 Origen
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        JLabel labelOrigen = crearEtiqueta("Origen:");
        panel.add(labelOrigen, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        origenField = crearTextField("Punto de partida");
        panel.add(origenField, gbc);

        // 🔸 Destino
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
        JLabel labelDestino = crearEtiqueta("Destino:");
        panel.add(labelDestino, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        destinoField = crearTextField("Destino final");
        panel.add(destinoField, gbc);

        // 🔸 NUEVO: Campo Zona
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.4;
        JLabel labelZona = crearEtiqueta("Zona:");
        panel.add(labelZona, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        zonaComboBox = crearComboBoxZona();
        panel.add(zonaComboBox, gbc);

        // 🔸 Botones
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        JPanel panelBotones = crearPanelBotones();
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FUENTE_NORMAL);
        label.setForeground(COLOR_BORDO_OSCURO);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JTextField crearTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FUENTE_NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_VINOTINTO_SUAVE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        // Agregar focus listener para mejor UX
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });

        return field;
    }

    private JComboBox<String> crearComboBoxZona() {
        String[] zonas = {"Seleccione zona", "Norte", "Sur", "Centro"};
        JComboBox<String> comboBox = new JComboBox<>(zonas);
        comboBox.setFont(FUENTE_NORMAL);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(COLOR_BORDO_OSCURO);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_VINOTINTO_SUAVE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return comboBox;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBackground(Color.WHITE);

        enviarBtn = crearBotonPremium("🚖 Solicitar Viaje", COLOR_VINOTINTO);
        limpiarBtn = crearBotonSecundario("🗑️ Limpiar", COLOR_VINOTINTO_SUAVE);

        enviarBtn.addActionListener(this);
        limpiarBtn.addActionListener(e -> limpiarFormulario());

        panel.add(enviarBtn);
        panel.add(limpiarBtn);
        return panel;
    }

    private JButton crearBotonPremium(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));

        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });

        return boton;
    }

    private JButton crearBotonSecundario(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(Color.WHITE);
        boton.setForeground(color);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));

        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
                boton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(color);
            }
        });

        return boton;
    }

    private JPanel crearPanelMensajes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_VINOTINTO, 1, true),
                        "📨 Historial",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_VINOTINTO
                ),
                new EmptyBorder(5, 5, 5, 5)
        ));

        mensajesArea = new JTextArea(6, 30);
        mensajesArea.setEditable(false);
        mensajesArea.setFont(FUENTE_MENSAJES);
        mensajesArea.setBackground(new Color(250, 250, 245));
        mensajesArea.setForeground(new Color(60, 40, 40));
        mensajesArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        mensajesArea.setLineWrap(true);
        mensajesArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(mensajesArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_VINOTINTO_SUAVE, 1, true));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(COLOR_BEIGE);
        labelStatus = new JLabel("⚡ Conectado - Complete el formulario");
        labelStatus.setFont(FUENTE_NORMAL);
        labelStatus.setForeground(COLOR_VINOTINTO_SUAVE);
        panel.add(labelStatus);
        return panel;
    }

    private void configurarLayout() {
        setContentPane(panelPrincipal);
        pack();
    }

    private void visualizarVentana() {
        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enviarBtn) {
            try {
                enviarSolicitud();
            } catch (Exception ex) {
                mostrarError("Error al enviar solicitud: " + ex.getMessage());
            }
        }
    }

    private void enviarSolicitud() throws Exception {
        String origen = obtenerTextoValido(origenField, "Punto de partida");
        String destino = obtenerTextoValido(destinoField, "Destino final");
        String zona = (String) zonaComboBox.getSelectedItem();

        // Validaciones
        if (origen.isEmpty() || destino.isEmpty()) {
            mostrarAdvertencia("Complete todos los campos.");
            return;
        }

        if (zona.equals("Seleccione zona")) {
            mostrarAdvertencia("Seleccione una zona.");
            zonaComboBox.requestFocus();
            return;
        }

        Cliente cliente = new Cliente(UUID.randomUUID().toString(), nombreCliente, origen, destino, "Esperando", zona);
        String json = mapper.writeValueAsString(cliente);

        producer.sendToQueue(COLA_SOLICITUDES, json);

        agregarMensaje("🚖 SOLICITUD ENVIADA",
                "Cliente: " + nombreCliente +
                        "\nRuta: " + origen + " → " + destino +
                        "\nZona: " + zona);

        labelStatus.setText("✅ Solicitud enviada - Esperando asignación...");
        labelStatus.setForeground(COLOR_EXITO);
        enviarBtn.setEnabled(false);
        limpiarBtn.setEnabled(false);
    }

    private String obtenerTextoValido(JTextField field, String placeholder) {
        String texto = field.getText().trim();
        return (texto.equals(placeholder) || texto.isEmpty()) ? "" : texto;
    }

    private void agregarMensaje(String tipo, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss"));
            mensajesArea.append("[" + timestamp + "] " + tipo + "\n" + mensaje + "\n\n");
            mensajesArea.setCaretPosition(mensajesArea.getDocument().getLength());
        });
    }

    private void limpiarFormulario() {
        origenField.setText("Punto de partida");
        destinoField.setText("Destino final");
        zonaComboBox.setSelectedIndex(0);
        origenField.setForeground(Color.GRAY);
        destinoField.setForeground(Color.GRAY);
        origenField.requestFocus();

        enviarBtn.setEnabled(true);
        limpiarBtn.setEnabled(true);
        labelStatus.setText("⚡ Conectado - Complete el formulario");
        labelStatus.setForeground(COLOR_VINOTINTO_SUAVE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "⚠️ Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    private void escucharAsignaciones() {
        Thread listenerThread = new Thread(() -> {
            try {
                receiver.listenQueue(COLA_ASIGNACION, (queue, message) -> {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            recibirAsignacionExtern(message);
                        } catch (Exception e) {
                            mostrarError("Error procesando mensaje: " + e.getMessage());
                        }
                    });
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> mostrarError("Error iniciando escucha: " + e.getMessage()));
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void escucharAnuncios() {
        Thread anunciosThread = new Thread(() -> {
            try {
                receiver.listenQueue("anuncios_clientes", (queue, message) -> {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            procesarAnuncio(message);
                        } catch (Exception e) {
                            //
                        }
                    });
                });
            } catch (Exception e) {
                //
            }
        });
        anunciosThread.setDaemon(true);
        anunciosThread.start();
    }


    private void procesarAnuncio(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> anuncio = mapper.readValue(json, Map.class);

            String titulo = anuncio.get("titulo");
            String mensaje = anuncio.get("mensaje");

            SwingUtilities.invokeLater(() -> {
                agregarMensaje("📢 " + titulo, mensaje);

                // Mostrar también como popup
                JOptionPane.showMessageDialog(this,
                        mensaje,
                        titulo,
                        JOptionPane.INFORMATION_MESSAGE);
            });

        } catch (Exception e) {
            //
        }
    }

    public void recibirAsignacionExtern(String json) throws Exception {

        if (Objects.equals(json, "No hay conductores disponibles") ||
                Objects.equals(json, "No hay conductores disponibles en tu zona")) {
            SwingUtilities.invokeLater(() -> {
                agregarMensaje("❌ ASIGNACIÓN FALLIDA",
                        "No hay conductores disponibles en este momento");
                labelStatus.setText("❌ Sin conductor - Intente más tarde");
                labelStatus.setForeground(COLOR_ERROR);
                enviarBtn.setEnabled(true);
                limpiarBtn.setEnabled(true);
            });
            return;
        }
        try {
            Asignacion asignacion = mapper.readValue(json, Asignacion.class);
            String nombreClienteAsignacion = asignacion.getCliente().getNombre();
            if (nombreClienteAsignacion != null && nombreClienteAsignacion.trim().equalsIgnoreCase(nombreCliente.trim())) {
                SwingUtilities.invokeLater(() -> {
                    agregarMensaje("✅ ASIGNACIÓN CONFIRMADA",
                            "Cliente: " + asignacion.getCliente().getNombre() +
                                    "\nConductor: " + asignacion.getConductor().getNombre() +
                                    "\nVehículo: " + asignacion.getConductor().getVehiculo() +
                                    "\nZona: " + (asignacion.getConductor().getZona() != null ?
                                    asignacion.getConductor().getZona() : "No especificada"));
                    labelStatus.setText("🎉 Conductor en camino! - " + asignacion.getConductor().getNombre());
                    labelStatus.setForeground(COLOR_DORADO);
                });
            }

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                agregarMensaje("📩 MENSAJE SISTEMA", json);
            });
        }
    }
}


