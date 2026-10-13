package samuelospina.imc.cliente.vistas;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import samuelospina.imc.rmi.lib.DatosImc;
import samuelospina.imc.rmi.lib.IRemotaCalculoImc;

/**
 * Cliente Swing de la guia del IMC: pide el peso y la altura e invoca de
 * forma remota (RMI estandar de Java) el metodo del servidor que calcula el Indice de
 * Masa Corporal. Aqui NO se hace ningun calculo.
 *
 * La interfaz esta pintada con el kit {@link Glass} (material translucido,
 * fondo animado, controles flotantes).
 *
 * @author Samuel David Ospina De Avila
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    private static final int MAX_HISTORIAL = 2;

    /** Nombre con el que el servidor enlaza el objeto remoto en el registro RMI. */
    private static final String NOMBRE_SERVICIO = "CalculoImcRemoto";

    // RMI estandar no necesita CallHandler ni Client: basta el stub del registro
    IRemotaCalculoImc calculoImcRemoto;

    private CardLayout tarjetas;
    private JPanel contenedor;
    private float ultimoValor = 0f;
    private Timer contador;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Calculo de IMC · Cliente RMI");

        Glass.Fondo fondo = new Glass.Fondo();
        fondo.setLayout(new BorderLayout(0, 18));
        fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        setContentPane(fondo);

        fondo.add(construirCabecera(), BorderLayout.NORTH);

        tarjetas = new CardLayout();
        contenedor = new JPanel(tarjetas);
        contenedor.setOpaque(false);
        contenedor.add(construirPanelConexion(), "conexion");
        contenedor.add(construirPanelCalculo(), "calcular");

        segmentado = new Glass.Segmentado(new String[]{"CONEXION", "CALCULAR IMC"},
                i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "calcular"));

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(segmentado, BorderLayout.CENTER);
        centro.add(fila, BorderLayout.NORTH);
        centro.add(contenedor, BorderLayout.CENTER);
        fondo.add(centro, BorderLayout.CENTER);

        setMinimumSize(new Dimension(740, 640));
        setSize(getMinimumSize());
        setLocationRelativeTo(null);
    }

    private JPanel construirCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Indice de Masa Corporal");
        titulo.setFont(Glass.fuente(Font.BOLD, 27f));
        titulo.setForeground(Glass.TINTA);

        JLabel subtitulo = new JLabel("Cliente RMI  ·  Guia del IMC  ·  Samuel Ospina");
        subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
        subtitulo.setForeground(Glass.TINTA_SUAVE);

        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(5));
        titulos.add(subtitulo);
        cabecera.add(titulos, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(new Glass.Chip("RMI estandar", Glass.CALOR));
        derecha.add(new Glass.Chip("kg / m²", Glass.FRIO));
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

        campoIPServidor = new Glass.Campo("localhost", Glass.FRIO);
        campoPuertoServidor = new Glass.Campo("9007", Glass.FRIO);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(Glass.rotulo("DIRECCION IP"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campoIPServidor, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(Glass.rotulo("PUERTO DE RED"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campoPuertoServidor, c);

        btnIniciar = new Glass.Pildora("Conectar", Glass.VERDE);
        btnIniciar.addActionListener(evt -> btnIniciarActionPerformed(evt));
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 0, 9, 0);
        panel.add(btnIniciar, c);

        txtEstado = new JLabel("Desconectado");
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

        JLabel ayuda = new JLabel("<html><div style='width:460px'>Al conectar, el cliente busca el "
                + "objeto remoto en el registro RMI y recibe su stub: llama a calcularImc() como si "
                + "fuera un metodo local y el calculo ocurre en el servidor.</div></html>");
        ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
        ayuda.setForeground(Glass.TINTA_SUAVE);
        c.gridy = 4;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(ayuda, c);

        return panel;
    }

    private JPanel construirPanelCalculo() {
        Glass.Tarjeta panel = new Glass.Tarjeta();
        panel.setLayout(new BorderLayout(0, 22));
        panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));

        JPanel entrada = new JPanel(new GridBagLayout());
        entrada.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        campoPeso = campoGrande();
        campoAltura = campoGrande();
        campoPeso.addActionListener(evt -> campoAltura.requestFocusInWindow());
        campoAltura.addActionListener(evt -> btnCalcularActionPerformed(evt));

        c.insets = new Insets(0, 0, 0, 10);
        entrada.add(etiquetaMenor("PESO (KG)"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 18);
        entrada.add(campoPeso, c);

        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 10);
        entrada.add(etiquetaMenor("ALTURA (M)"), c);
        c.gridx = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 18);
        entrada.add(campoAltura, c);

        btnCalcular = new Glass.Pildora("CALCULAR", Glass.CALOR);
        btnCalcular.addActionListener(evt -> btnCalcularActionPerformed(evt));
        c.gridx = 4;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        entrada.add(btnCalcular, c);

        panel.add(entrada, BorderLayout.NORTH);

        // Bloque de resultado
        JPanel resultado = new JPanel();
        resultado.setOpaque(false);
        resultado.setLayout(new BoxLayout(resultado, BoxLayout.Y_AXIS));

        JLabel rotulo = etiquetaMenor("INDICE DE MASA CORPORAL");
        rotulo.setAlignmentX(LEFT_ALIGNMENT);

        txtResultado = new JLabel("0.0");
        txtResultado.setFont(Glass.fuente(Font.BOLD, 52f));
        txtResultado.setForeground(Glass.CALOR);
        txtResultado.setAlignmentX(LEFT_ALIGNMENT);

        txtMensaje = new JLabel("Conecte con el servidor y escriba el peso y la altura.");
        txtMensaje.setFont(Glass.fuente(Font.PLAIN, 13f));
        txtMensaje.setForeground(Glass.TINTA);
        txtMensaje.setAlignmentX(LEFT_ALIGNMENT);

        // Escala visual: solo ubica el IMC que devolvio el servidor
        medidor = new Glass.Medidor(new float[]{10f, 18.5f, 24.9f, 29.9f, 45f},
                new Color[]{Glass.FRIO, Glass.VERDE, Glass.CALOR, Glass.ROJO},
                new String[]{"bajo", "normal", "sobrepeso", "alto"});
        medidor.setAlignmentX(LEFT_ALIGNMENT);

        resultado.add(rotulo);
        resultado.add(Box.createVerticalStrut(8));
        resultado.add(txtResultado);
        resultado.add(Box.createVerticalStrut(10));
        resultado.add(txtMensaje);
        resultado.add(Box.createVerticalStrut(18));
        resultado.add(medidor);
        resultado.add(Box.createVerticalStrut(18));

        // Accesos rapidos: solo rellenan los campos, el calculo sigue siendo del servidor
        JPanel rapidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rapidos.setOpaque(false);
        rapidos.setAlignmentX(LEFT_ALIGNMENT);
        rapidos.add(etiquetaMenor("PRUEBE CON  "));
        for (String[] v : new String[][]{{"50", "1.75"}, {"65", "1.70"}, {"78", "1.70"}, {"95", "1.65"}}) {
            Glass.Chip chip = new Glass.Chip(v[0] + " kg · " + v[1] + " m", Glass.FRIO);
            chip.setFont(Glass.fuente(Font.BOLD, 11f));
            chip.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            chip.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    campoPeso.setText(v[0]);
                    campoAltura.setText(v[1]);
                    campoAltura.requestFocusInWindow();
                }
            });
            rapidos.add(chip);
        }
        resultado.add(rapidos);
        panel.add(resultado, BorderLayout.CENTER);

        // Pie: historial de invocaciones remotas y latencia de la ultima
        historial = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        historial.setOpaque(false);
        historial.add(etiquetaMenor("HISTORIAL  "));

        txtLatencia = etiquetaMenor("");
        txtLatencia.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(historial, BorderLayout.CENTER);
        pie.add(txtLatencia, BorderLayout.EAST);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private Glass.Campo campoGrande() {
        Glass.Campo campo = new Glass.Campo("", Glass.FRIO);
        campo.setFont(Glass.fuente(Font.BOLD, 22f));
        campo.setHorizontalAlignment(SwingConstants.CENTER);
        campo.setPreferredSize(new Dimension(110, 50));
        return campo;
    }

    private JLabel etiquetaMenor(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Glass.fuente(Font.BOLD, 11f));
        l.setForeground(Glass.TINTA_SUAVE);
        return l;
    }

    /** Animacion del contador: interpola del valor anterior al nuevo. */
    private void animarResultado(float desde, float hasta) {
        if (contador != null && contador.isRunning()) {
            contador.stop();
        }
        final long inicio = System.currentTimeMillis();
        final int duracion = 420;
        contador = new Timer(16, null);
        contador.addActionListener(e -> {
            float p = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) duracion);
            float suave = 1 - (1 - p) * (1 - p) * (1 - p);   // ease-out cubico
            float valor = desde + (hasta - desde) * suave;
            txtResultado.setText(String.format(Locale.US, "%.2f", valor));
            if (p >= 1f) {
                txtResultado.setText(String.valueOf(hasta));
                contador.stop();
            }
        });
        contador.start();
    }

    /** Agrega la invocacion al historial, conservando solo las mas recientes. */
    private void agregarAlHistorial(String texto) {
        Glass.Chip chip = new Glass.Chip(texto, Glass.CALOR);
        chip.setFont(Glass.fuente(Font.BOLD, 11f));
        historial.add(chip, 1);   // despues del rotulo: la mas reciente primero
        while (historial.getComponentCount() > MAX_HISTORIAL + 1) {
            historial.remove(historial.getComponentCount() - 1);
        }
        historial.revalidate();
        historial.repaint();
    }

    /**
     * Tramo del medidor para un IMC ya calculado por el servidor. Usa los
     * mismos cortes de la guia, solo para saber que segmento iluminar.
     */
    private int tramoDe(float imc) {
        if (imc < 18.5f) {
            return 0;
        } else if (imc <= 24.9f) {
            return 1;
        } else if (imc <= 29.9f) {
            return 2;
        }
        return 3;
    }

    // ------------------------------------------------------------------
    // Invocacion remota
    // ------------------------------------------------------------------
    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        if (btnIniciar.getText().equalsIgnoreCase("Desconectar")) {
            desconectar();
            return;
        }
        int puerto;
        try {
            puerto = Integer.parseInt(campoPuertoServidor.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El PUERTO DE RED debe ser un numero entero",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ip = campoIPServidor.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba la DIRECCION IP del servidor",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Registry registro = LocateRegistry.getRegistry(ip, puerto);
            calculoImcRemoto = (IRemotaCalculoImc) registro.lookup(NOMBRE_SERVICIO);
            btnIniciar.setText("Desconectar");
            btnIniciar.setBackground(Glass.ROJO);
            txtEstado.setText("Conectado a " + ip + ":" + puerto);
            txtEstado.setForeground(Glass.VERDE);
            segmentado.setSeleccion(1);
            repaint();
        } catch (Exception ex) {
            System.out.println("ERROR AL CONECTAR");
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void desconectar() {
        // En RMI estandar no hay una conexion que cerrar: se suelta el stub.
        calculoImcRemoto = null;
        btnIniciar.setText("Conectar");
        btnIniciar.setBackground(Glass.VERDE);
        txtEstado.setText("Desconectado");
        txtEstado.setForeground(Glass.ROJO);
        repaint();
    }

    /** Lee un numero positivo de un campo; avisa y devuelve null si no lo es. */
    private Float leerPositivo(Glass.Campo campo, String nombre) {
        String texto = campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba " + nombre,
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        float valor;
        try {
            valor = Float.parseFloat(texto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "\"" + texto + "\" no es un numero valido",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (Float.isNaN(valor) || Float.isInfinite(valor) || valor <= 0) {
            JOptionPane.showMessageDialog(this, "Debe ser un numero mayor que 0: " + nombre,
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return valor;
    }

    private void btnCalcularActionPerformed(java.awt.event.ActionEvent evt) {
        final IRemotaCalculoImc remoto = calculoImcRemoto;
        if (remoto == null) {
            JOptionPane.showMessageDialog(this, "Cliente OffLine, Conecte con el Servidor",
                    "Sin Conexión", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Validacion de los datos ANTES de invocar al servidor.
        // Aqui no se calcula nada: el IMC es responsabilidad del servidor.
        final Float peso = leerPositivo(campoPeso, "el peso en kilogramos");
        if (peso == null) {
            return;
        }
        final Float altura = leerPositivo(campoAltura, "la altura en metros");
        if (altura == null) {
            return;
        }
        if (altura > 3f) {
            JOptionPane.showMessageDialog(this,
                    "La altura va en metros, no en centimetros (por ejemplo 1.70)",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Thread hilo = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Peso: " + peso + "\nAltura: " + altura);
                    long inicio = System.nanoTime();
                    DatosImc datos = remoto.calcularImc(new DatosImc(peso, altura));
                    long ms = (System.nanoTime() - inicio) / 1000000;
                    System.out.println("IMC: " + datos.getResultado()
                            + "\nMensaje: " + datos.getInterpretacion());
                    // La interfaz se toca desde el hilo de Swing (EDT)
                    SwingUtilities.invokeLater(() -> mostrarResultado(datos, ms));
                } catch (Exception ex) {
                    System.out.println("ERROR con el cliente " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        desconectar();
                        JOptionPane.showMessageDialog(VentanaPrincipal.this,
                                "Se perdio la comunicacion con el servidor", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
        };
        hilo.start();
    }

    private void mostrarResultado(DatosImc datos, long ms) {
        String msj = datos.getInterpretacion();
        boolean error = msj == null || msj.startsWith("ERROR");
        txtResultado.setForeground(error ? Glass.ROJO : Glass.CALOR);
        if (error) {
            txtResultado.setText("—");
            medidor.limpiar();
        } else {
            animarResultado(ultimoValor, datos.getResultado());
            ultimoValor = datos.getResultado();
            medidor.setValor(datos.getResultado(), tramoDe(datos.getResultado()));
            agregarAlHistorial(datos.getPeso() + " kg · " + datos.getAltura() + " m → "
                    + datos.getResultado());
        }
        txtMensaje.setText(msj);
        txtLatencia.setText("LATENCIA  " + ms + " ms");
    }

    public JLabel getTxtEstado() {
        return txtEstado;
    }

    public JButton getBtnIniciar() {
        return btnIniciar;
    }

    private JButton btnIniciar;
    private JButton btnCalcular;
    private Glass.Campo campoAltura;
    private Glass.Campo campoIPServidor;
    private Glass.Campo campoPeso;
    private Glass.Campo campoPuertoServidor;
    private Glass.Medidor medidor;
    private Glass.Segmentado segmentado;
    private JPanel historial;
    private JLabel txtEstado;
    private JLabel txtLatencia;
    private JLabel txtMensaje;
    private JLabel txtResultado;
}
