package samuelospina.imc.vistas;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import samuelospina.imc.servidor.Servidor;

/**
 * Ventana del servidor: publica/retira el objeto remoto con RMI estandar y
 * muestra la bitacora de las invocaciones atendidas, con la misma interfaz
 * de vidrio del cliente.
 *
 * @author Samuel David Ospina De Avila
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    private CardLayout tarjetas;
    private JPanel contenedor;
    private Glass.Segmentado segmentado;
    private Servidor servidor;
    private int invocaciones = 0;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Calculo de IMC · Servidor RMI");

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        Glass.Fondo fondo = new Glass.Fondo();
        fondo.setLayout(new BorderLayout(0, 18));
        fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        setContentPane(fondo);

        fondo.add(construirCabecera(), BorderLayout.NORTH);

        tarjetas = new CardLayout();
        contenedor = new JPanel(tarjetas);
        contenedor.setOpaque(false);
        contenedor.add(construirPanelConexion(), "conexion");
        contenedor.add(construirPanelLog(), "log");

        segmentado = new Glass.Segmentado(
                new String[]{"CONEXION", "LOG DE INVOCACIONES"},
                i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "log"));

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(segmentado, BorderLayout.NORTH);
        centro.add(contenedor, BorderLayout.CENTER);
        fondo.add(centro, BorderLayout.CENTER);

        setMinimumSize(new Dimension(700, 580));
        setSize(getMinimumSize());
        setLocationRelativeTo(null);
    }

    private JPanel construirCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Servidor de IMC");
        titulo.setFont(Glass.fuente(Font.BOLD, 27f));
        titulo.setForeground(Glass.TINTA);

        JLabel subtitulo = new JLabel("Objeto remoto en un registro RMI  ·  Guia del IMC");
        subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
        subtitulo.setForeground(Glass.TINTA_SUAVE);

        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(5));
        titulos.add(subtitulo);
        cabecera.add(titulos, BorderLayout.WEST);

        chipInvocaciones = new Glass.Chip("0 invocaciones", Glass.FRIO);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(chipInvocaciones);
        derecha.add(new Glass.Chip("RMI estandar", Glass.CALOR));
        cabecera.add(derecha, BorderLayout.EAST);

        return cabecera;
    }

    private JPanel construirPanelConexion() {
        Glass.Tarjeta panel = new Glass.Tarjeta();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 0, 9, 12);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoIP = new Glass.Campo("", Glass.CALOR);
        campoPuerto = new Glass.Campo("9007", Glass.CALOR);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(Glass.rotulo("DIRECCION IP"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campoIP, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(Glass.rotulo("PUERTO DE RED"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campoPuerto, c);

        btnIniciar = new Glass.Pildora("INICIAR", Glass.VERDE);
        btnIniciar.addActionListener(evt -> btnIniciarActionPerformed(evt));
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 0, 9, 0);
        panel.add(btnIniciar, c);

        txtEstado = new JLabel("DETENIDO");
        txtEstado.setFont(Glass.fuente(Font.BOLD, 13f));
        txtEstado.setForeground(Glass.ROJO);

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        estado.setOpaque(false);
        estado.add(new Glass.Punto(txtEstado));
        estado.add(txtEstado);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 0, 0, 0);
        panel.add(estado, c);

        JLabel ayuda = new JLabel("<html><div style='width:470px'>Al iniciar se crea un registro RMI "
                + "en el puerto y se enlaza en el el objeto que calcula el IMC. Los clientes lo "
                + "buscan por nombre e invocan calcularImc() de forma remota.</div></html>");
        ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
        ayuda.setForeground(Glass.TINTA_SUAVE);
        c.gridy = 4;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(ayuda, c);

        return panel;
    }

    private JPanel construirPanelLog() {
        Glass.Tarjeta panel = new Glass.Tarjeta();
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        cajaLog = new Glass.Bitacora();

        JScrollPane scroll = new JScrollPane(cajaLog);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // La consola va dentro de su propia lamina de vidrio, mas oscura
        Glass.Tarjeta consola = new Glass.Tarjeta(18) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(6, 11, 25, 190));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new java.awt.Color(255, 255, 255, 45));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        consola.setLayout(new BorderLayout());
        consola.add(scroll, BorderLayout.CENTER);
        panel.add(consola, BorderLayout.CENTER);

        btnLimpiar = new Glass.Pildora("LIMPIAR", new java.awt.Color(0x263140));
        btnLimpiar.addActionListener(evt -> cajaLog.limpiar());

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.add(btnLimpiar);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        if (btnIniciar.getText().equalsIgnoreCase("INICIAR")) {
            int puerto;
            try {
                puerto = Integer.parseInt(campoPuerto.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El PUERTO DE RED debe ser un numero entero",
                        "Dato invalido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // El objeto remoto avisa desde hilos de red: la ventana solo se toca en el EDT.
                servidor = new Servidor(puerto, campoIP.getText().trim(),
                        msg -> SwingUtilities.invokeLater(() -> escribirLog(msg)));
                servidor.iniciar();
                btnIniciar.setText("DETENER");
                btnIniciar.setBackground(Glass.ROJO);
                txtEstado.setText("ONLINE");
                txtEstado.setForeground(Glass.VERDE);
                escribirLog("[SERVIDOR] Registro RMI y objeto remoto disponibles en el puerto " + puerto);
                segmentado.setSeleccion(1);
            } catch (Exception ex) {
                escribirLog("[ERROR] " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al iniciar",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (btnIniciar.getText().equalsIgnoreCase("DETENER")) {
            if (servidor != null) {
                servidor.detener();
            }
            btnIniciar.setText("INICIAR");
            btnIniciar.setBackground(Glass.VERDE);
            txtEstado.setText("OFF LINE");
            txtEstado.setForeground(Glass.ROJO);
            escribirLog("[SERVIDOR] Servicio detenido");
        }
        repaint();
    }

    /** Escribe en consola y en la bitacora, y cuenta las invocaciones atendidas. */
    private void escribirLog(String texto) {
        String marca = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a").format(new Date()) + " - ";
        System.out.println(marca + texto);
        cajaLog.agregar(marca, texto);
        if (texto.startsWith("[SOLICITUD]")) {
            invocaciones++;
            chipInvocaciones.setText(invocaciones
                    + (invocaciones == 1 ? " invocacion" : " invocaciones"));
        }
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        campoIP.setEditable(false);
        campoIP.setText(ipLocal());
    }

    /**
     * IPv4 de la red local por la que otros equipos alcanzan este servidor.
     * InetAddress.getLocalHost() suele devolver 127.0.0.1, que solo sirve
     * dentro del mismo computador; por eso se recorren las interfaces.
     */
    private static String ipLocal() {
        try {
            Enumeration<NetworkInterface> redes = NetworkInterface.getNetworkInterfaces();
            while (redes != null && redes.hasMoreElements()) {
                NetworkInterface red = redes.nextElement();
                if (!red.isUp() || red.isLoopback() || red.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> direcciones = red.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress d = direcciones.nextElement();
                    if (d instanceof Inet4Address && d.isSiteLocalAddress()) {
                        return d.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (IOException ex) {
            return "127.0.0.1";
        }
    }

    public JLabel getTxtEstado() {
        return txtEstado;
    }

    public JButton getBtnIniciar() {
        return btnIniciar;
    }

    private JButton btnIniciar;
    private JButton btnLimpiar;
    private Glass.Bitacora cajaLog;
    private Glass.Campo campoIP;
    private Glass.Campo campoPuerto;
    private Glass.Chip chipInvocaciones;
    private JLabel txtEstado;
}
