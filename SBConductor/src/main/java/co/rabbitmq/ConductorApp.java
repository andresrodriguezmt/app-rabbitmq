package co.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.rabbitmq.Cliente;
import co.rabbitmq.Conductor;
import co.rabbitmq.Asignacion;
import co.rabbitmq.Producer;
import co.rabbitmq.Receiver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

public class ConductorApp extends JFrame implements ActionListener {

    private final Producer producer;
    private final Receiver receiver;
    private final ObjectMapper mapper = new ObjectMapper();

    // Nombres de colas
    private final String COLA_SOLICITUDES_CONDUCTORES = "solicitudes_conductores";
    private String COLA_ASIGNACION_CLIENTE = "";
    private final String COLA_CONFIRMACION_REGISTRO = "confirmacion_conductor";

    // Paleta de colores
    private final Color COLOR_AZUL_PROFESIONAL = new Color(16, 52, 120);
    private final Color COLOR_AZUL_SUAVE = new Color(40, 80, 160);
    private final Color COLOR_DORADO = new Color(212, 175, 55);
    private final Color COLOR_BEIGE = new Color(245, 240, 230);
    private final Color COLOR_AZUL_OSCURO = new Color(10, 35, 80);
    private final Color COLOR_EXITO = new Color(60, 140, 60);
    private final Color COLOR_ERROR = new Color(180, 50, 50);

    // Fuentes más compactas
    private final Font FUENTE_TITULO = new Font("DejaVu Sans", Font.BOLD, 20);
    private final Font FUENTE_SUBTITULO = new Font("DejaVu Sans", Font.BOLD, 14);
    private final Font FUENTE_NORMAL = new Font("DejaVu Sans", Font.PLAIN, 12);
    private final Font FUENTE_BOTON = new Font("DejaVu Sans", Font.BOLD, 13);
    private final Font FUENTE_MENSAJES = new Font("DejaVu Sans", Font.PLAIN, 11);

    // Componentes UI
    private JTextField nombreField, vehiculoField;
    private JComboBox<String> zonaComboBox;
    private JButton enviarBtn, limpiarBtn;
    private JTextArea mensajesArea;
    private JLabel labelStatus;
    private String nombreConductor;

    public ConductorApp(Producer producer, Receiver receiver) {
        this.producer = producer;
        this.receiver = receiver;

        solicitarNombreConductor();
        configurarVentana();
        inicializarComponentes();
        visualizarVentana();

        Thread listenerConfirmation = new Thread(() -> {
            try {
                receiver.listenQueue(COLA_CONFIRMACION_REGISTRO, (queue, message) -> {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            procesarMensajeConfirmacion(message);
                        } catch (Exception e) {
                            //
                        }
                    });
                });
            } catch (Exception e) {
                //
            }
        });

        listenerConfirmation.setDaemon(true);
        listenerConfirmation.start();

        escucharAnuncios();
    }

    private void solicitarNombreConductor() {
        nombreConductor = JOptionPane.showInputDialog(null,
                "Ingrese su nombre para registrarse:",
                "Registro de Conductor",
                JOptionPane.PLAIN_MESSAGE);

        if (nombreConductor == null || nombreConductor.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Debe ingresar un nombre válido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        nombreConductor = nombreConductor.trim();
        setTitle("🚖 Conductor: " + nombreConductor);
    }

    private void configurarVentana() {
        setTitle("🚖 Conductor: " + nombreConductor);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 600));
        setPreferredSize(new Dimension(400, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BEIGE);
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.setBackground(COLOR_BEIGE);

        panelPrincipal.add(crearPanelHeader(), BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        panelCentro.add(crearPanelFormulario(), BorderLayout.NORTH);
        panelCentro.add(crearPanelMensajes(), BorderLayout.CENTER);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        panelPrincipal.add(crearPanelFooter(), BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    private JPanel crearPanelHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BEIGE);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel labelIcono = new JLabel("👨‍✈️");
        labelIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        labelIcono.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel labelTitulo = new JLabel("Luxury Ride Driver");
        labelTitulo.setFont(FUENTE_TITULO);
        labelTitulo.setForeground(COLOR_AZUL_PROFESIONAL);
        labelTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel labelSubtitulo = new JLabel("Registro Premium");
        labelSubtitulo.setFont(FUENTE_SUBTITULO);
        labelSubtitulo.setForeground(COLOR_AZUL_SUAVE);
        labelSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(COLOR_BEIGE);
        panelTitulo.add(labelTitulo, BorderLayout.NORTH);
        panelTitulo.add(labelSubtitulo, BorderLayout.SOUTH);

        panel.add(labelIcono, BorderLayout.WEST);
        panel.add(panelTitulo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_AZUL_PROFESIONAL, 1, true),
                        "📋 Información del Conductor",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_AZUL_PROFESIONAL
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campo Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        JLabel labelNombre = crearEtiqueta("Nombre:");
        panel.add(labelNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        nombreField = crearTextField("");
        nombreField.setText(nombreConductor);
        nombreField.setEnabled(false);
        nombreField.setForeground(Color.BLACK);
        panel.add(nombreField, gbc);

        // Campo Vehículo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        JLabel labelVehiculo = crearEtiqueta("Vehículo:");
        panel.add(labelVehiculo, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        vehiculoField = crearTextField("Modelo y marca", "vehiculo");
        panel.add(vehiculoField, gbc);

        // Campo Zona
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
        JLabel labelZona = crearEtiqueta("Zona:");
        panel.add(labelZona, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        zonaComboBox = crearComboBoxZona();
        panel.add(zonaComboBox, gbc);

        // Botones
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        JPanel panelBotones = crearPanelBotones();
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FUENTE_NORMAL);
        label.setForeground(COLOR_AZUL_OSCURO);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JTextField crearTextField(String placeholder, String tipo) {
        JTextField field = new JTextField();
        field.setFont(FUENTE_NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_AZUL_SUAVE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

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

    private JTextField crearTextField(String placeholder) {
        return crearTextField(placeholder, "");
    }

    private JComboBox<String> crearComboBoxZona() {
        String[] zonas = {"Seleccione zona", "Norte", "Sur", "Centro"};
        JComboBox<String> comboBox = new JComboBox<>(zonas);
        comboBox.setFont(FUENTE_NORMAL);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(COLOR_AZUL_OSCURO);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_AZUL_SUAVE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        comboBox.addActionListener(e -> {
            if (comboBox.getSelectedIndex() > 0) {
                String zonaSeleccionada = (String) comboBox.getSelectedItem();
                actualizarColaAsignacion(zonaSeleccionada);
            }
        });

        return comboBox;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBackground(Color.WHITE);

        enviarBtn = crearBotonPremium("✅ Registrarse", COLOR_AZUL_PROFESIONAL);
        limpiarBtn = crearBotonSecundario("🗑️ Limpiar", COLOR_AZUL_SUAVE);

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
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
                        BorderFactory.createLineBorder(COLOR_AZUL_PROFESIONAL, 1, true),
                        "📨 Historial",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_AZUL_PROFESIONAL
                ),
                new EmptyBorder(5, 5, 5, 5)
        ));

        mensajesArea = new JTextArea(6, 30); // Más compacto
        mensajesArea.setEditable(false);
        mensajesArea.setFont(FUENTE_MENSAJES);
        mensajesArea.setBackground(new Color(250, 248, 245));
        mensajesArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        mensajesArea.setLineWrap(true);
        mensajesArea.setWrapStyleWord(true);

        JScrollPane scrollMensajes = new JScrollPane(mensajesArea);
        scrollMensajes.setBorder(BorderFactory.createLineBorder(COLOR_AZUL_SUAVE, 1, true));
        scrollMensajes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollMensajes, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(COLOR_BEIGE);
        labelStatus = new JLabel("⚡ Conectado - Seleccione zona");
        labelStatus.setFont(FUENTE_NORMAL);
        labelStatus.setForeground(COLOR_AZUL_SUAVE);
        panel.add(labelStatus);
        return panel;
    }

    private void visualizarVentana() {
        pack();
        setVisible(true);
    }

    private void actualizarColaAsignacion(String zona) {
        String nuevaCola;
        switch (zona.toLowerCase()) {
            case "norte": nuevaCola = "asignacion_cliente_norte"; break;
            case "sur": nuevaCola = "asignacion_cliente_sur"; break;
            case "centro": nuevaCola = "asignacion_cliente_centro"; break;
            default: nuevaCola = ""; return;
        }

        if (!nuevaCola.equals(COLA_ASIGNACION_CLIENTE)) {
            COLA_ASIGNACION_CLIENTE = nuevaCola;
            iniciarEscuchaAsignaciones();
            agregarMensaje("ZONA SELECCIONADA", "Zona: " + zona + "\nEscuchando: " + COLA_ASIGNACION_CLIENTE);
        }
    }

    private void iniciarEscuchaAsignaciones() {
        if (COLA_ASIGNACION_CLIENTE == null || COLA_ASIGNACION_CLIENTE.isEmpty()) return;

        final String colaActual = COLA_ASIGNACION_CLIENTE;
        Thread hiloAsignaciones = new Thread(() -> {
            try {
                receiver.listenQueue(colaActual, (queue, message) -> {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            procesarMensajeAsignacion(message);
                        } catch (Exception e) {
                            //
                        }
                    });
                });
            } catch (Exception e) {
                //
            }
        });
        hiloAsignaciones.setDaemon(true);
        hiloAsignaciones.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enviarBtn) {
            try {
                enviarRegistro();
            } catch (Exception ex) {
                mostrarError("Error enviando registro: " + ex.getMessage());
            }
        }
    }

    private void enviarRegistro() throws JsonProcessingException {
        String vehiculo = vehiculoField.getText().trim();
        String zona = (String) zonaComboBox.getSelectedItem();

        if (vehiculo.isEmpty() || vehiculo.equals("Modelo y marca") || zona.equals("Seleccione zona")) {
            mostrarAdvertencia("Complete todos los campos.");
            return;
        }

        Conductor conductor = new Conductor(
                UUID.randomUUID().toString(),
                nombreConductor,
                vehiculo,
                "Sin cliente asignado",
                "",
                zona
        );

        String json = mapper.writeValueAsString(conductor);

        try {
            producer.sendToQueue(COLA_SOLICITUDES_CONDUCTORES, json);
            agregarMensaje("ENVIANDO REGISTRO",
                    "Nombre: " + nombreConductor +
                            "\nVehículo: " + vehiculo +
                            "\nZona: " + zona +
                            "\nHora: " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } catch (Exception ex) {
            mostrarError("Error enviando registro: " + ex.getMessage());
        }
    }

    private void escucharAnuncios() {
        Thread anunciosThread = new Thread(() -> {
            try {
                receiver.listenQueue("anuncios_conductores", (queue, message) -> {
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
    private void procesarMensajeConfirmacion(String json) {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, String> mensajeMap = mapper.readValue(json, Map.class);
                String nombreEnMensaje = mensajeMap.get("nombre");

                if (nombreEnMensaje != null && nombreEnMensaje.equals(nombreConductor)) {
                    agregarMensaje("🚖 REGISTRO COMPLETADO", "Registro confirmado para: " + nombreConductor);
                    labelStatus.setText("✅ Registrado - Esperando asignaciones");
                    labelStatus.setForeground(COLOR_EXITO);
                    enviarBtn.setEnabled(false);
                    limpiarBtn.setEnabled(false);
                }
            } catch (Exception e) {
                agregarMensaje("📩 MENSAJE SISTEMA", json);
            }
        });
    }

    private void procesarMensajeAsignacion(String json) {
        try {
            if (json.contains("No hay conductores disponibles")) {
                SwingUtilities.invokeLater(() -> {
                    agregarMensaje("❌ ASIGNACIÓN FALLIDA", "No hay conductores disponibles");
                    labelStatus.setText("❌ Sin conductor asignado");
                    labelStatus.setForeground(COLOR_ERROR);
                });
                return;
            }

            Asignacion asignacion = mapper.readValue(json, Asignacion.class);
            SwingUtilities.invokeLater(() -> {
                agregarMensaje("🚖 NUEVA SOLICITUD",
                        "Cliente: " + asignacion.getCliente().getNombre() +
                                "\nRuta: " + asignacion.getCliente().getOrigen() + " → " + asignacion.getCliente().getDestino() +
                                "\nZona: " + (asignacion.getConductor() != null ? asignacion.getConductor().getZona() : "N/A"));
                labelStatus.setText("📱 Nueva solicitud - " + asignacion.getCliente().getNombre());
                labelStatus.setForeground(COLOR_DORADO);
            });

        } catch (JsonProcessingException e) {
            procesarMensajeConfirmacion(json);
        } catch (Exception e) {
            agregarMensaje("❌ ERROR", "Mensaje: " + json + "\nError: " + e.getMessage());
        }
    }

    private void agregarMensaje(String tipo, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            mensajesArea.append("[" + timestamp + "] " + tipo + "\n" + mensaje + "\n\n");
            mensajesArea.setCaretPosition(mensajesArea.getDocument().getLength());
        });
    }

    private void limpiarFormulario() {
        vehiculoField.setText("Modelo y marca");
        zonaComboBox.setSelectedIndex(0);
        vehiculoField.setForeground(Color.GRAY);
        vehiculoField.requestFocus();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "⚠️ Advertencia", JOptionPane.WARNING_MESSAGE);
    }
}
