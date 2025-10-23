package co.rabbitmq;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServidorGUI extends JFrame implements ActionListener {

    private Servidor servidor;

    private final Color COLOR_VERDE_OSCURO = new Color(0, 80, 40);
    private final Color COLOR_VERDE_MEDIO = new Color(0, 120, 60);
    private final Color COLOR_VERDE_CLARO = new Color(100, 180, 80);
    private final Color COLOR_BEIGE = new Color(245, 240, 230);
    private final Color COLOR_EXITO = new Color(60, 140, 60);
    private final Color COLOR_ERROR = new Color(180, 50, 50);

    // --- Fuentes ---
    private final Font FUENTE_TITULO = new Font("DejaVu Sans", Font.BOLD, 18);
    private final Font FUENTE_SUBTITULO = new Font("DejaVu Sans", Font.BOLD, 13);
    private final Font FUENTE_BOTON = new Font("DejaVu Sans", Font.BOLD, 12);
    private final Font FUENTE_NORMAL = new Font("DejaVu Sans", Font.PLAIN, 11);
    private final Font FUENTE_LISTAS = new Font("DejaVu Sans", Font.PLAIN, 11);

    // --- Componentes ---
    private JButton iniciarBtn, actualizarBtn, enviarAnuncioBtn;
    private JTextArea logsArea, anuncioArea;
    private DefaultListModel<String> conductoresModel, clientesModel;
    private JList<String> conductoresList, clientesList;

    private boolean servidorIniciado = false;

    public ServidorGUI() {
        configurarVentana();
        inicializarComponentes();
        setVisible(true);
    }

    // ------------------ Configuración base ------------------
    private void configurarVentana() {
        setTitle("♫ Luxury Ride - Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BEIGE);
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(COLOR_BEIGE);
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));

        panelPrincipal.add(crearPanelHeader(), BorderLayout.NORTH);

        panelPrincipal.add(crearPanelCentral(), BorderLayout.CENTER);

        panelPrincipal.add(crearPanelLogs(), BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    private JPanel crearPanelHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BEIGE);

        JLabel labelTitulo = new JLabel("Luxury Ride Server", SwingConstants.LEFT);
        labelTitulo.setFont(FUENTE_TITULO);
        labelTitulo.setForeground(COLOR_VERDE_OSCURO);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBackground(COLOR_BEIGE);

        iniciarBtn = crearBotonPremium("🚀 Iniciar", COLOR_VERDE_OSCURO);
        actualizarBtn = crearBotonSecundario("🔄 Actualizar", COLOR_VERDE_MEDIO);
        iniciarBtn.addActionListener(this);
        actualizarBtn.addActionListener(e -> actualizarListas());

        botones.add(iniciarBtn);
        botones.add(actualizarBtn);

        panel.add(labelTitulo, BorderLayout.WEST);
        panel.add(botones, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBackground(COLOR_BEIGE);

        panel.add(crearPanelLista("🚗 Conductores", true));
        panel.add(crearPanelLista("👤 Clientes", false));
        panel.add(crearPanelAnuncio());

        return panel;
    }

    private JPanel crearPanelLista(String titulo, boolean esConductor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_VERDE_OSCURO, 1, true),
                        titulo,
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_VERDE_OSCURO
                ),
                new EmptyBorder(8, 8, 8, 8)
        ));

        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> lista = new JList<>(modelo);
        lista.setFont(FUENTE_LISTAS);
        lista.setBackground(new Color(250, 255, 250));
        lista.setSelectionBackground(COLOR_VERDE_CLARO);
        lista.setSelectionForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_VERDE_MEDIO, 1, true));

        if (esConductor) {
            conductoresModel = modelo;
            conductoresList = lista;
        } else {
            clientesModel = modelo;
            clientesList = lista;
        }

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelAnuncio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_VERDE_OSCURO, 1, true),
                        "📢 Enviar Anuncio",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_VERDE_OSCURO
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));

        anuncioArea = new JTextArea(5, 20);
        anuncioArea.setFont(FUENTE_NORMAL);
        anuncioArea.setLineWrap(true);
        anuncioArea.setWrapStyleWord(true);
        anuncioArea.setBorder(BorderFactory.createLineBorder(COLOR_VERDE_CLARO, 1, true));
        anuncioArea.setText("Escriba aquí el anuncio para todos los usuarios...");

        // Efecto placeholder
        anuncioArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (anuncioArea.getText().equals("Escriba aquí el anuncio para todos los usuarios...")) {
                    anuncioArea.setText("");
                    anuncioArea.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (anuncioArea.getText().isEmpty()) {
                    anuncioArea.setText("Escriba aquí el anuncio para todos los usuarios...");
                    anuncioArea.setForeground(Color.GRAY);
                }
            }
        });

        enviarAnuncioBtn = crearBotonPremium("📨 Enviar Anuncio a Todos", COLOR_VERDE_MEDIO);
        enviarAnuncioBtn.addActionListener(e -> enviarAnuncio());

        panel.add(new JScrollPane(anuncioArea), BorderLayout.CENTER);
        panel.add(enviarAnuncioBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelLogs() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_VERDE_OSCURO, 1, true),
                        "📝 Información del Sistema",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        FUENTE_SUBTITULO,
                        COLOR_VERDE_OSCURO
                ),
                new EmptyBorder(8, 8, 8, 8)
        ));

        logsArea = new JTextArea(10, 70);
        logsArea.setEditable(false);
        logsArea.setFont(FUENTE_NORMAL);
        logsArea.setBackground(new Color(250, 255, 250));
        logsArea.setLineWrap(true);
        logsArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(logsArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_VERDE_MEDIO, 1, true));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == iniciarBtn) iniciarServidor();
    }

    private void iniciarServidor() {
        if (!servidorIniciado) {
            try {
                final String host = "localhost";
                final int puerto = 5672;
                final String usuario = "guest";
                final String contrasenia = "guest";
                final String virtualHost = "apprabbit";

                Producer producer = new Producer(host, puerto, usuario, contrasenia, virtualHost);
                Receiver receiver = new Receiver(host, puerto, usuario, contrasenia, virtualHost);
                servidor = new Servidor(producer, receiver);

                redirectSystemOutput();
                servidor.iniciar();
                servidorIniciado = true;

                iniciarBtn.setText("✅ Servidor Activo");
                iniciarBtn.setBackground(COLOR_EXITO);
                iniciarBtn.setEnabled(false);
                iniciarActualizacionAutomatica();

            } catch (Exception ex) {
                agregarLog("❌ Error iniciando servidor: " + ex.getMessage(), COLOR_ERROR);
            }
        }
    }

    private void enviarAnuncio() {
        String mensaje = anuncioArea.getText().trim();

        if (mensaje.isEmpty() || mensaje.equals("Escriba aquí el anuncio para todos los usuarios...")) {
            JOptionPane.showMessageDialog(this, "Por favor escribe un anuncio antes de enviarlo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (servidor != null) {
            servidor.enviarAnuncio(mensaje);
            agregarLog("📢 Anuncio enviado a todos los usuarios: " + mensaje, COLOR_VERDE_OSCURO);
            anuncioArea.setText("Escriba aquí el anuncio para todos los usuarios...");
            anuncioArea.setForeground(Color.GRAY);
        } else {
            JOptionPane.showMessageDialog(this, "El servidor no está iniciado",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void redirectSystemOutput() {
        System.setOut(new java.io.PrintStream(System.out) {
            @Override
            public void println(String x) {
                SwingUtilities.invokeLater(() -> agregarLog(x, COLOR_VERDE_OSCURO));
            }
        });
        System.setErr(new java.io.PrintStream(System.err) {
            @Override
            public void println(String x) {
                SwingUtilities.invokeLater(() -> agregarLog("[ERROR] " + x, COLOR_ERROR));
            }
        });
    }

    private void actualizarListas() {
        if (servidor != null) {
            conductoresModel.clear();
            clientesModel.clear();

            List<String> conductores = servidor.getListaConductores();
            List<String> clientes = servidor.getListaClientes();

            for (String c : conductores) conductoresModel.addElement("🚗     " + c);
            for (String cl : clientes) clientesModel.addElement("👤 "+ cl);
        }
    }

    private void iniciarActualizacionAutomatica() {
        new Timer(2000, e -> {
            if (servidorIniciado) actualizarListas();
        }).start();
    }

    private void agregarLog(String mensaje, Color color) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logsArea.append("[" + timestamp + "] " + mensaje + "\n");
            logsArea.setCaretPosition(logsArea.getDocument().getLength());
        });
    }

    private JButton crearBotonPremium(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(5, 12, 5, 12));
        return boton;
    }

    private JButton crearBotonSecundario(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_BOTON);
        boton.setBackground(Color.WHITE);
        boton.setForeground(color);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(5, 12, 5, 12));
        return boton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServidorGUI::new);
    }
}