import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class VentanaPrincipal extends JFrame {

    // ---------------------------------------------------------------------
    // Paleta clara y sobria
    // ---------------------------------------------------------------------
    private static final Color FONDO      = new Color(0xF4F6FA);
    private static final Color TARJETA    = Color.WHITE;
    private static final Color BORDE      = new Color(0xE2E8F0);
    private static final Color TEXTO      = new Color(0x1F2A44);
    private static final Color TENUE      = new Color(0x6B7A90);
    private static final Color LATERAL    = new Color(0x14213D);
    private static final Color LAT_TEXTO  = new Color(0xA9B4CC);
    private static final Color ACENTO     = new Color(0x4F46E5);
    private static final Color ACENTO_CLR = new Color(0x818CF8);
    private static final Color EXITO      = new Color(0x10B981);
    private static final Color PELIGRO    = new Color(0xEF4444);
    private static final Color SUPERFICIE = new Color(0xF8FAFC);

    private static final Color[] PALETA_GANTT = {
        new Color(0x4F46E5), new Color(0x0EA5E9), new Color(0x10B981), new Color(0xF59E0B),
        new Color(0xF43F5E), new Color(0x8B5CF6), new Color(0x14B8A6), new Color(0xF97316)
    };

    private static final String F_UI = elegir(Font.SANS_SERIF, "Segoe UI", "SF Pro Text", "Helvetica Neue", "Arial");
    private static final String F_MONO = elegir(Font.MONOSPACED, "Cascadia Mono", "Consolas", "Courier New");

    private static String elegir(String respaldo, String... preferidas) {
        Set<String> disp = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : preferidas) {
            if (disp.contains(p)) return p;
        }
        return respaldo;
    }

    private static Font ui(int estilo, int tam) { return new Font(F_UI, estilo, tam); }
    private static Font mono(int tam) { return new Font(F_MONO, Font.PLAIN, tam); }

    // ---------------------------------------------------------------------
    // Datos y componentes
    // ---------------------------------------------------------------------
    private List<Proceso> listaProcesos = new ArrayList<>();

    private JTextField txtPID, txtLlegada, txtRafaga, txtPrioridad, txtMemoria, txtQuantum;
    private JComboBox<String> cbAlgoritmo;
    private JTable tablaIngresados, tablaResultados;
    private DefaultTableModel modelIngresados, modelResultados;
    private GanttPanel ganttPanel;
    private JLabel valRetorno, valEspera, valRespuesta;

    private JTextField txtParticionesRAM, txtPidLiberar;
    private JComboBox<String> cbEstrategia;
    private JTextArea txtAreaMemoria;
    private MemoriaPanel memoriaPanel;
    private JLabel valMemTotal, valMemUsada, valMemFrag, valMemLibre;
    private GestorMemoria gestorMemoria;
    private final Set<String> asignados = new HashSet<>();
    private JTextArea txtAreaInterbloqueos;
    private JTextArea txtAreaConcurrencia;

    private JTextField txtHilos, txtIncrementos;
    private JLabel valSin, espSin, valCon, espCon;
    private Insignia estSin, estCon;

    private JComboBox<String> cbEscenario;
    private JTable tablaAsignacion, tablaMaximo, tablaDisponibles;
    private GrafoPanel grafoPanel;
    private Insignia veredicto;

    private JTable tablaPCB;
    private DefaultTableModel modelPCB;

    private CardLayout cards;
    private JPanel contenido;
    private JLabel lblEstado;

    public VentanaPrincipal() {
        configurarLookAndFeel();

        setTitle("SimuSO | Simulador de Recursos del Sistema Operativo");
        setSize(1240, 860);
        setMinimumSize(new Dimension(1050, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);

        cards = new CardLayout();
        contenido = new JPanel(cards);
        contenido.setBackground(FONDO);
        contenido.add(crearPanelCPU(), "CPU");
        contenido.add(crearPanelMemoria(), "MEMORIA");
        contenido.add(crearPanelInterbloqueos(), "DEADLOCK");
        contenido.add(crearPanelConcurrencia(), "CONCURRENCIA");
        contenido.add(crearPanelPCB(), "PCB");

        raiz.add(crearBarraLateral(), BorderLayout.WEST);
        raiz.add(contenido, BorderLayout.CENTER);

        setContentPane(raiz);
        actualizarEstado();
    }

    private void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Se usa el aspecto por defecto si falla
        }
        UIManager.put("OptionPane.background", TARJETA);
        UIManager.put("Panel.background", TARJETA);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("OptionPane.messageFont", ui(Font.PLAIN, 13));
        UIManager.put("Button.font", ui(Font.PLAIN, 13));
    }

    // =====================================================================
    // BARRA LATERAL
    // =====================================================================
    private JPanel crearBarraLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(LATERAL);
        lateral.setPreferredSize(new Dimension(255, 0));

        JLabel logo = new JLabel("SimuSO");
        logo.setFont(ui(Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html>Simulador de recursos<br>del sistema operativo</html>");
        sub.setFont(ui(Font.PLAIN, 12));
        sub.setForeground(LAT_TEXTO);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cab = new JPanel();
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        cab.setOpaque(false);
        cab.setBorder(new EmptyBorder(30, 26, 26, 14));
        cab.setAlignmentX(Component.LEFT_ALIGNMENT);
        cab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cab.add(logo);
        cab.add(Box.createVerticalStrut(6));
        cab.add(sub);
        lateral.add(cab);

        ButtonGroup grupo = new ButtonGroup();
        String[][] items = {
            {"01", "Planificador CPU", "CPU"},
            {"02", "Memoria RAM", "MEMORIA"},
            {"03", "Interbloqueos", "DEADLOCK"},
            {"04", "Concurrencia", "CONCURRENCIA"},
            {"05", "PCB y ciclo de vida", "PCB"}
        };
        boolean primero = true;
        for (String[] it : items) {
            NavBoton b = new NavBoton(it[0], it[1]);
            final String id = it[2];
            b.addActionListener(e -> cards.show(contenido, id));
            grupo.add(b);
            lateral.add(b);
            if (primero) { b.setSelected(true); primero = false; }
        }

        lateral.add(Box.createVerticalGlue());

        lblEstado = new JLabel();
        lblEstado.setFont(ui(Font.PLAIN, 12));
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel curso = new JLabel("Sistemas Operativos I");
        curso.setFont(ui(Font.PLAIN, 11));
        curso.setForeground(LAT_TEXTO);
        curso.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pie = new JPanel();
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(10, 26, 24, 14));
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);
        pie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        pie.add(lblEstado);
        pie.add(Box.createVerticalStrut(4));
        pie.add(curso);
        lateral.add(pie);

        return lateral;
    }

    private void actualizarEstado() {
        if (lblEstado != null) {
            lblEstado.setText("Procesos en cola: " + listaProcesos.size());
        }
    }

    // =====================================================================
    // FÁBRICAS DE COMPONENTES
    // =====================================================================
    private JPanel moduloBase() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(FONDO);
        p.setBorder(new EmptyBorder(28, 32, 22, 32));
        return p;
    }

    private JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel t = new JLabel(titulo);
        t.setFont(ui(Font.BOLD, 26));
        t.setForeground(TEXTO);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel s = new JLabel(subtitulo);
        s.setFont(ui(Font.PLAIN, 13));
        s.setForeground(TENUE);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(s);
        return p;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(ui(Font.BOLD, 12));
        l.setForeground(TENUE);
        return l;
    }

    private JLabel tituloTarjeta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(ui(Font.BOLD, 14));
        l.setForeground(TEXTO);
        l.setBorder(new EmptyBorder(0, 2, 10, 0));
        return l;
    }

    private Tarjeta tarjeta(String titulo, JComponent cuerpo) {
        Tarjeta t = new Tarjeta(new BorderLayout());
        if (titulo != null) t.add(tituloTarjeta(titulo), BorderLayout.NORTH);
        t.add(cuerpo, BorderLayout.CENTER);
        return t;
    }

    private JTextField campo(String valor, int columnas) {
        JTextField f = new JTextField(valor, columnas);
        f.setBackground(Color.WHITE);
        f.setForeground(TEXTO);
        f.setCaretColor(ACENTO);
        f.setFont(ui(Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(new BordeRedondo(BORDE), new EmptyBorder(4, 8, 4, 8)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(new CompoundBorder(new BordeRedondo(ACENTO), new EmptyBorder(4, 8, 4, 8)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(new CompoundBorder(new BordeRedondo(BORDE), new EmptyBorder(4, 8, 4, 8)));
            }
        });
        return f;
    }

    private BotonRedondo boton(String texto, Color color, boolean relleno) {
        return new BotonRedondo(texto, color, relleno);
    }

    private JTextArea areaSalida() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setBackground(SUPERFICIE);
        a.setForeground(TEXTO);
        a.setCaretColor(ACENTO);
        a.setFont(mono(13));
        a.setMargin(new Insets(12, 14, 12, 14));
        return a;
    }

    private JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUI(new ScrollFino());
        sp.getHorizontalScrollBar().setUI(new ScrollFino());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(10, 10));
        return sp;
    }

    private DefaultTableModel modelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int col) { return false; }
        };
    }

    private JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(Color.WHITE);
        t.setForeground(TEXTO);
        t.setGridColor(new Color(0xEEF1F6));
        t.setFont(ui(Font.PLAIN, 13));
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(0xEEF2FF));
        t.setSelectionForeground(ACENTO);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v == null ? "" : v.toString(), false, false, r, c);
                l.setBackground(SUPERFICIE);
                l.setForeground(TENUE);
                l.setFont(ui(Font.BOLD, 12));
                l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDE), new EmptyBorder(0, 12, 0, 6)));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setPreferredSize(new Dimension(l.getPreferredSize().width, 32));
                return l;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, false, r, c);
                if (!s) {
                    l.setBackground(Color.WHITE);
                    l.setForeground(TEXTO);
                }
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                return l;
            }
        });
        return t;
    }

    private JComboBox<String> combo(String[] opciones) {
        JComboBox<String> cb = new JComboBox<>(opciones);
        cb.setFont(ui(Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXTO);
        cb.setOpaque(false);
        cb.setBorder(new BordeRedondo(BORDE));
        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                BasicArrowButton b = new BasicArrowButton(BasicArrowButton.SOUTH, Color.WHITE, Color.WHITE, TENUE, Color.WHITE);
                b.setBorder(new EmptyBorder(0, 4, 0, 6));
                return b;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean foco) {
                g.setColor(Color.WHITE);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
            @Override public void paintCurrentValue(Graphics g, Rectangle r, boolean foco) {
                @SuppressWarnings("unchecked")
                ListCellRenderer<Object> rend = (ListCellRenderer<Object>) comboBox.getRenderer();
                Component c = rend.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setFont(comboBox.getFont());
                c.setBackground(Color.WHITE);
                c.setForeground(TEXTO);
                currentValuePane.paintComponent(g, c, comboBox, r.x, r.y, r.width, r.height, false);
            }
        });
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v, int i, boolean sel, boolean foc) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, v, i, sel, false);
                l.setBackground(sel ? new Color(0xEEF2FF) : Color.WHITE);
                l.setForeground(sel ? ACENTO : TEXTO);
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                return l;
            }
        });
        return cb;
    }

    // =====================================================================
    // COMPONENTES PERSONALIZADOS
    // =====================================================================
    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // Tarjeta blanca con esquinas redondeadas y sombra suave
    private static class Tarjeta extends JPanel {
        private static final long serialVersionUID = 1L;
        Tarjeta(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(20, 22, 22, 22));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            for (int i = 5; i >= 1; i--) {
                g2.setColor(new Color(31, 42, 68, 9));
                g2.fill(new RoundRectangle2D.Float(4 - i, 3 - i + 2, w - 8 + 2 * i, h - 8 + 2 * i, 20, 20));
            }
            g2.setColor(TARJETA);
            g2.fill(new RoundRectangle2D.Float(4, 3, w - 8, h - 8, 18, 18));
            g2.setColor(BORDE);
            g2.draw(new RoundRectangle2D.Float(4, 3, w - 9, h - 9, 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Borde redondeado para campos
    private static class BordeRedondo extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final Color color;
        BordeRedondo(Color color) { this.color = color; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
    }

    // Botón redondeado
    private static class BotonRedondo extends JButton {
        private static final long serialVersionUID = 1L;
        private final Color base;
        private final boolean relleno;
        BotonRedondo(String texto, Color base, boolean relleno) {
            super(texto);
            this.base = base;
            this.relleno = relleno;
            setFont(ui(Font.BOLD, 13));
            setForeground(relleno ? Color.WHITE : base);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            boolean sobre = getModel().isRollover();
            int w = getWidth(), h = getHeight();
            if (relleno) {
                g2.setColor(sobre ? base.darker() : base);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            } else {
                g2.setColor(sobre ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 28) : Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(base);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Botón del menú lateral
    private static class NavBoton extends JToggleButton {
        private static final long serialVersionUID = 1L;
        NavBoton(String numero, String texto) {
            super(numero + "    " + texto);
            setFont(ui(Font.PLAIN, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(14, 32, 14, 10));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            if (isSelected()) {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(12, 3, getWidth() - 24, getHeight() - 6, 14, 14);
                g2.setColor(ACENTO_CLR);
                g2.fillRoundRect(12, 12, 4, getHeight() - 24, 4, 4);
                setForeground(Color.WHITE);
            } else {
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 14));
                    g2.fillRoundRect(12, 3, getWidth() - 24, getHeight() - 6, 14, 14);
                }
                setForeground(LAT_TEXTO);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Barra de desplazamiento fina
    private static class ScrollFino extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0xCBD5E1);
            trackColor = Color.WHITE;
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(Color.WHITE);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
        @Override protected JButton createDecreaseButton(int o) { return cero(); }
        @Override protected JButton createIncreaseButton(int o) { return cero(); }
        private JButton cero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }

    // Diagrama de Gantt con barras de colores
    private static class GanttPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient List<PlanificadorCPU.BloqueGantt> bloques = new ArrayList<>();

        GanttPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 96));
        }

        void setBloques(List<PlanificadorCPU.BloqueGantt> nuevos) {
            bloques = nuevos == null ? new ArrayList<>() : new ArrayList<>(nuevos);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth();

            if (bloques.isEmpty()) {
                g2.setColor(SUPERFICIE);
                g2.fillRoundRect(0, 6, w - 1, 64, 14, 14);
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 6, w - 1, 64, 14, 14);
                g2.setFont(ui(Font.PLAIN, 13));
                g2.setColor(TENUE);
                String msg = "Ejecuta una simulación para ver el diagrama de Gantt";
                int tw = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, (w - tw) / 2, 44);
                g2.dispose();
                return;
            }

            int total = 0;
            for (PlanificadorCPU.BloqueGantt b : bloques) total = Math.max(total, b.tiempoFin);
            if (total <= 0) { g2.dispose(); return; }

            Map<String, Integer> indices = new LinkedHashMap<>();
            int margen = 2;
            double ancho = w - 2 * margen;
            double unidad = ancho / total;
            int y = 6, alto = 56;
            int ultimaEtiqueta = -100;

            for (PlanificadorCPU.BloqueGantt b : bloques) {
                int x1 = margen + (int) Math.round(b.tiempoInicio * unidad);
                int x2 = margen + (int) Math.round(b.tiempoFin * unidad);
                int bw = Math.max(2, x2 - x1 - 3);

                boolean ocioso = b.pid != null && b.pid.toUpperCase().startsWith("IDLE");
                Color c;
                if (ocioso) {
                    c = new Color(0xCBD5E1);
                } else {
                    Integer idx = indices.get(b.pid);
                    if (idx == null) { idx = indices.size(); indices.put(b.pid, idx); }
                    c = PALETA_GANTT[idx % PALETA_GANTT.length];
                }
                g2.setColor(c);
                g2.fillRoundRect(x1, y, bw, alto, 12, 12);

                g2.setFont(ui(Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String texto = b.pid == null ? "" : b.pid;
                if (fm.stringWidth(texto) + 8 < bw) {
                    g2.setColor(ocioso ? TENUE : Color.WHITE);
                    g2.drawString(texto, x1 + (bw - fm.stringWidth(texto)) / 2, y + alto / 2 + 5);
                }

                g2.setFont(ui(Font.PLAIN, 11));
                g2.setColor(TENUE);
                String t = String.valueOf(b.tiempoInicio);
                if (x1 - ultimaEtiqueta >= 24) {
                    g2.drawString(t, x1, y + alto + 18);
                    ultimaEtiqueta = x1;
                }
            }
            String fin = String.valueOf(total);
            g2.setFont(ui(Font.PLAIN, 11));
            g2.setColor(TENUE);
            int fw = g2.getFontMetrics().stringWidth(fin);
            g2.drawString(fin, w - margen - fw, y + alto + 18);
            g2.dispose();
        }
    }

    // Mapa visual de las particiones de memoria
    private static class MemoriaPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient List<BloqueMemoria> bloques = new ArrayList<>();
        private transient Map<String, Color> colores = new HashMap<>();

        MemoriaPanel() {
            setOpaque(false);
        }

        void setDatos(List<BloqueMemoria> nuevos, Map<String, Color> col) {
            bloques = nuevos == null ? new ArrayList<>() : new ArrayList<>(nuevos);
            colores = col == null ? new HashMap<>() : new HashMap<>(col);
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(100, 36 + Math.max(1, bloques.size()) * 40);
        }

        private static int leyenda(Graphics2D g2, int x, String texto, Color c) {
            g2.setColor(c);
            g2.fillRoundRect(x, 4, 12, 12, 4, 4);
            g2.setColor(TENUE);
            g2.setFont(ui(Font.PLAIN, 12));
            g2.drawString(texto, x + 18, 15);
            return x + 18 + g2.getFontMetrics().stringWidth(texto);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth();

            int lx = leyenda(g2, 0, "Proceso", PALETA_GANTT[0]);
            lx = leyenda(g2, lx + 18, "Fragmentación interna", new Color(0xFDE68A));
            leyenda(g2, lx + 18, "Libre", new Color(0xE2E8F0));

            if (bloques.isEmpty()) {
                g2.setFont(ui(Font.PLAIN, 13));
                g2.setColor(TENUE);
                String msg = "Presiona \"Crear memoria\" para ver las particiones";
                int tw = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, (w - tw) / 2, 80);
                g2.dispose();
                return;
            }

            int maximo = 1;
            for (BloqueMemoria b : bloques) maximo = Math.max(maximo, b.getTamañoKB());
            int x0 = 130;
            int anchoBarra = Math.max(80, w - x0 - 10);

            for (int i = 0; i < bloques.size(); i++) {
                BloqueMemoria b = bloques.get(i);
                int y = 30 + i * 40;

                g2.setColor(TEXTO);
                g2.setFont(ui(Font.BOLD, 13));
                g2.drawString("Bloque " + b.getId(), 0, y + 14);
                g2.setColor(TENUE);
                g2.setFont(ui(Font.PLAIN, 12));
                g2.drawString(b.getTamañoKB() + " KB", 0, y + 27);

                int largo = Math.max(40, (int) Math.round(anchoBarra * (double) b.getTamañoKB() / maximo));
                RoundRectangle2D barra = new RoundRectangle2D.Float(x0, y, largo, 30, 12, 12);
                g2.setColor(new Color(0xE2E8F0));
                g2.fill(barra);

                if (b.isOcupado()) {
                    int tam = Math.max(1, b.getTamañoKB());
                    int usado = (int) Math.round(largo * (double) (tam - b.getFragmentacionInterna()) / tam);
                    Color c = colores.getOrDefault(b.getPidProcesoAsignado(), ACENTO);
                    Shape previo = g2.getClip();
                    g2.setClip(barra);
                    g2.setColor(c);
                    g2.fillRect(x0, y, usado, 30);
                    g2.setColor(new Color(0xFDE68A));
                    g2.fillRect(x0 + usado, y, largo - usado, 30);
                    g2.setClip(previo);

                    g2.setFont(ui(Font.BOLD, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    String pid = b.getPidProcesoAsignado();
                    int usadoKB = b.getTamañoKB() - b.getFragmentacionInterna();
                    String etiqueta = pid + "  " + usadoKB + " KB";
                    if (fm.stringWidth(etiqueta) + 10 < usado) {
                        g2.setColor(Color.WHITE);
                        g2.drawString(etiqueta, x0 + (usado - fm.stringWidth(etiqueta)) / 2, y + 20);
                    } else if (fm.stringWidth(pid) + 10 < usado) {
                        g2.setColor(Color.WHITE);
                        g2.drawString(pid, x0 + (usado - fm.stringWidth(pid)) / 2, y + 20);
                    }
                    if (b.getFragmentacionInterna() > 0) {
                        g2.setFont(ui(Font.PLAIN, 12));
                        fm = g2.getFontMetrics();
                        String f = "frag. " + b.getFragmentacionInterna() + " KB";
                        if (fm.stringWidth(f) + 10 < largo - usado) {
                            g2.setColor(new Color(0x92400E));
                            g2.drawString(f, x0 + usado + (largo - usado - fm.stringWidth(f)) / 2, y + 20);
                        }
                    }
                } else {
                    g2.setFont(ui(Font.PLAIN, 13));
                    g2.setColor(TENUE);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Libre", x0 + (largo - fm.stringWidth("Libre")) / 2, y + 20);
                }
                g2.setColor(BORDE);
                g2.draw(barra);
            }
            g2.dispose();
        }
    }

    // Etiqueta redondeada que cambia de color según el estado
    private static class Insignia extends JLabel {
        private static final long serialVersionUID = 1L;
        private Color fondo = new Color(0x94A3B8);

        Insignia(String texto) {
            super(texto);
            setFont(ui(Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBorder(new EmptyBorder(6, 14, 6, 14));
            setOpaque(false);
        }

        void estado(String texto, Color color) {
            setText(texto);
            fondo = color;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Grafo de asignación de recursos: procesos (círculos) y recursos (cuadros)
    private static class GrafoPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final BasicStroke SOLIDO = new BasicStroke(2f);
        private static final BasicStroke PUNTEADO = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f);

        private transient int[][] asig = new int[0][0];
        private transient int[][] need = new int[0][0];
        private transient int[] disp = new int[0];
        private transient boolean[] bloqueados;

        GrafoPanel() {
            setOpaque(false);
        }

        void setDatos(int[][] a, int[][] mx, int[] d, boolean[] bloq) {
            int n = a.length;
            int m = d.length;
            asig = new int[n][m];
            need = new int[n][m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    asig[i][j] = a[i][j];
                    need[i][j] = mx[i][j] - a[i][j];
                }
            }
            disp = d.clone();
            bloqueados = bloq == null ? null : bloq.clone();
            repaint();
        }

        private static void flecha(Graphics2D g2, double x1, double y1, double x2, double y2) {
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
            double ang = Math.atan2(y2 - y1, x2 - x1);
            double largo = 9;
            Path2D.Double cabeza = new Path2D.Double();
            cabeza.moveTo(x2, y2);
            cabeza.lineTo(x2 - largo * Math.cos(ang - Math.PI / 7), y2 - largo * Math.sin(ang - Math.PI / 7));
            cabeza.lineTo(x2 - largo * Math.cos(ang + Math.PI / 7), y2 - largo * Math.sin(ang + Math.PI / 7));
            cabeza.closePath();
            g2.fill(cabeza);
        }

        private static void numero(Graphics2D g2, int valor, double x, double y) {
            g2.setFont(ui(Font.BOLD, 11));
            String t = String.valueOf(valor);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(t) + 8;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect((int) x - w / 2, (int) y - 8, w, 16, 8, 8);
            g2.setColor(TEXTO);
            g2.drawString(t, (int) x - fm.stringWidth(t) / 2, (int) y + 4);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth();
            int h = getHeight();
            int n = asig.length;
            int m = disp.length;

            // Leyenda
            g2.setFont(ui(Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.setStroke(SOLIDO);
            g2.setColor(EXITO);
            g2.drawLine(0, 10, 20, 10);
            g2.setColor(TENUE);
            String t1 = "Asignado";
            g2.drawString(t1, 26, 15);
            int x2 = 26 + fm.stringWidth(t1) + 16;
            g2.setStroke(PUNTEADO);
            g2.setColor(PELIGRO);
            g2.drawLine(x2, 10, x2 + 20, 10);
            g2.setColor(TENUE);
            g2.drawString("Solicita (bloqueado)", x2 + 26, 15);
            g2.setStroke(SOLIDO);
            if (n == 0 || m == 0) {
                g2.dispose();
                return;
            }

            int top = 40;
            int bottom = 8;
            int xp = 54;
            int xr = w - 60;
            int rp = 15;
            double[] yp = new double[n];
            double[] yr = new double[m];
            for (int i = 0; i < n; i++) yp[i] = top + (h - top - bottom) * (i + 0.5) / n;
            for (int j = 0; j < m; j++) yr[j] = top + (h - top - bottom) * (j + 0.5) / m;

            // Aristas de asignación
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (asig[i][j] > 0) {
                        g2.setStroke(SOLIDO);
                        g2.setColor(EXITO);
                        flecha(g2, xr - 24, yr[j] - 4, xp + rp, yp[i] - 4);
                        if (asig[i][j] > 1) {
                            numero(g2, asig[i][j], xp + rp + (xr - 24 - xp - rp) * 0.3, yp[i] - 4 + (yr[j] - yp[i]) * 0.3);
                        }
                    }
                }
            }
            // Solicitudes de los procesos bloqueados
            if (bloqueados != null) {
                for (int i = 0; i < n; i++) {
                    if (!bloqueados[i]) continue;
                    for (int j = 0; j < m; j++) {
                        if (need[i][j] > 0) {
                            g2.setStroke(PUNTEADO);
                            g2.setColor(PELIGRO);
                            flecha(g2, xp + rp, yp[i] + 4, xr - 24, yr[j] + 4);
                            if (need[i][j] > 1) {
                                numero(g2, need[i][j], xp + rp + (xr - 24 - xp - rp) * 0.7, yp[i] + 4 + (yr[j] - yp[i]) * 0.7);
                            }
                        }
                    }
                }
            }
            g2.setStroke(SOLIDO);

            // Nodos de procesos
            for (int i = 0; i < n; i++) {
                boolean bloq = bloqueados != null && bloqueados[i];
                g2.setColor(bloq ? new Color(0xFEE2E2) : new Color(0xEEF2FF));
                g2.fillOval(xp - rp, (int) yp[i] - rp, rp * 2, rp * 2);
                g2.setColor(bloq ? PELIGRO : ACENTO);
                g2.drawOval(xp - rp, (int) yp[i] - rp, rp * 2, rp * 2);
                g2.setFont(ui(Font.BOLD, 12));
                fm = g2.getFontMetrics();
                String nombre = "P" + i;
                g2.setColor(TEXTO);
                g2.drawString(nombre, xp - fm.stringWidth(nombre) / 2, (int) yp[i] + 4);
            }
            // Nodos de recursos
            for (int j = 0; j < m; j++) {
                int total = disp[j];
                for (int i = 0; i < n; i++) total += asig[i][j];
                g2.setColor(new Color(0xFFF7ED));
                g2.fillRoundRect(xr - 24, (int) yr[j] - 17, 48, 34, 8, 8);
                g2.setColor(new Color(0xF59E0B));
                g2.drawRoundRect(xr - 24, (int) yr[j] - 17, 48, 34, 8, 8);
                g2.setColor(TEXTO);
                g2.setFont(ui(Font.BOLD, 12));
                fm = g2.getFontMetrics();
                String nombre = "R" + j;
                g2.drawString(nombre, xr - fm.stringWidth(nombre) / 2, (int) yr[j] - 2);
                g2.setFont(ui(Font.PLAIN, 10));
                fm = g2.getFontMetrics();
                String inst = "x" + total;
                g2.setColor(TENUE);
                g2.drawString(inst, xr - fm.stringWidth(inst) / 2, (int) yr[j] + 12);
            }
            g2.dispose();
        }
    }

    // Pastilla con un valor destacado
    private static class Pastilla extends JPanel {
        private static final long serialVersionUID = 1L;
        Pastilla(JLabel titulo, JLabel valor) {
            super();
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(10, 16, 10, 16));
            titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            valor.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(titulo);
            add(Box.createVerticalStrut(2));
            add(valor);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(SUPERFICIE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JLabel valorMetrica() {
        JLabel l = new JLabel("—");
        l.setFont(ui(Font.BOLD, 20));
        l.setForeground(ACENTO);
        return l;
    }

    private JLabel nombreMetrica(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(ui(Font.PLAIN, 12));
        l.setForeground(TENUE);
        return l;
    }

    // =====================================================================
    // MÓDULO 1: CPU
    // =====================================================================
    private JPanel crearPanelCPU() {
        JPanel panel = moduloBase();

        txtPID = campo("P1", 8);
        txtLlegada = campo("0", 8);
        txtRafaga = campo("5", 8);
        txtPrioridad = campo("1", 8);
        txtMemoria = campo("120", 8);

        BotonRedondo btnAgregar = boton("Agregar proceso", EXITO, true);
        BotonRedondo btnLimpiar = boton("Limpiar lista", PELIGRO, false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.gridx = 0; g.weightx = 0; form.add(etiqueta("PID"), g);
        g.gridx = 1; g.weightx = 1; form.add(txtPID, g);
        g.gridx = 2; g.weightx = 0; form.add(etiqueta("Llegada (ms)"), g);
        g.gridx = 3; g.weightx = 1; form.add(txtLlegada, g);
        g.gridx = 4; g.weightx = 0; form.add(etiqueta("Ráfaga (ms)"), g);
        g.gridx = 5; g.weightx = 1; form.add(txtRafaga, g);

        g.gridy = 1;
        g.gridx = 0; g.weightx = 0; form.add(etiqueta("Prioridad"), g);
        g.gridx = 1; g.weightx = 1; form.add(txtPrioridad, g);
        g.gridx = 2; g.weightx = 0; form.add(etiqueta("Memoria (KB)"), g);
        g.gridx = 3; g.weightx = 1; form.add(txtMemoria, g);
        g.gridx = 4; g.weightx = 0; form.add(btnAgregar, g);
        g.gridx = 5; g.weightx = 1; form.add(btnLimpiar, g);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        JPanel enc = encabezado("Planificador de CPU", "FCFS, SJF y Round Robin con diagrama de Gantt y promedios");
        enc.setAlignmentX(Component.LEFT_ALIGNMENT);
        Tarjeta cardForm = tarjeta("Nuevo proceso", form);
        cardForm.setAlignmentX(Component.LEFT_ALIGNMENT);
        norte.add(enc);
        norte.add(Box.createVerticalStrut(10));
        norte.add(cardForm);

        modelIngresados = modelo(new String[]{"PID", "Llegada", "Ráfaga", "Prioridad", "Memoria"});
        tablaIngresados = tabla(modelIngresados);
        modelResultados = modelo(new String[]{"PID", "Fin", "Retorno", "Espera", "Respuesta"});
        tablaResultados = tabla(modelResultados);

        JPanel tablas = new JPanel(new GridLayout(1, 2, 14, 0));
        tablas.setOpaque(false);
        tablas.add(tarjeta("Procesos ingresados", scroll(tablaIngresados)));
        tablas.add(tarjeta("Resultados de ejecución", scroll(tablaResultados)));

        cbAlgoritmo = combo(new String[]{"FCFS / FIFO", "SJF (No Expropiativo)", "Round Robin (RR)"});
        cbAlgoritmo.setPreferredSize(new Dimension(210, 38));
        txtQuantum = campo("2", 4);
        BotonRedondo btnEjecutar = boton("Ejecutar simulación", ACENTO, true);

        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        control.setOpaque(false);
        control.add(etiqueta("Algoritmo"));
        control.add(cbAlgoritmo);
        control.add(etiqueta("Quantum (RR)"));
        control.add(txtQuantum);
        control.add(btnEjecutar);

        ganttPanel = new GanttPanel();
        valRetorno = valorMetrica();
        valEspera = valorMetrica();
        valRespuesta = valorMetrica();

        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(new Pastilla(nombreMetrica("Retorno promedio"), valRetorno));
        metricas.add(new Pastilla(nombreMetrica("Espera promedio"), valEspera));
        metricas.add(new Pastilla(nombreMetrica("Respuesta promedio"), valRespuesta));

        JPanel cuerpoGantt = new JPanel(new BorderLayout(0, 8));
        cuerpoGantt.setOpaque(false);
        cuerpoGantt.add(ganttPanel, BorderLayout.CENTER);
        cuerpoGantt.add(metricas, BorderLayout.SOUTH);

        JPanel sur = new JPanel(new BorderLayout(0, 10));
        sur.setOpaque(false);
        sur.add(control, BorderLayout.NORTH);
        sur.add(tarjeta("Diagrama de Gantt", cuerpoGantt), BorderLayout.CENTER);

        panel.add(norte, BorderLayout.NORTH);
        panel.add(tablas, BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarProceso());
        btnLimpiar.addActionListener(e -> limpiarListas());
        btnEjecutar.addActionListener(e -> ejecutarSimulacion());

        return panel;
    }

    // =====================================================================
    // MÓDULOS 2, 3, 4 Y 5
    // =====================================================================
    private JPanel moduloConSalida(String titulo, String subtitulo, JComponent controles, JTextArea area) {
        JPanel panel = moduloBase();
        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        norte.add(encabezado(titulo, subtitulo), BorderLayout.NORTH);
        norte.add(controles, BorderLayout.CENTER);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(tarjeta("Resultado", scroll(area)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelMemoria() {
        JPanel panel = moduloBase();

        txtParticionesRAM = campo("100, 500, 200, 300, 600", 16);
        cbEstrategia = combo(new String[]{"First Fit (primer ajuste)", "Best Fit (mejor ajuste)", "Worst Fit (peor ajuste)"});
        cbEstrategia.setPreferredSize(new Dimension(220, 38));
        txtPidLiberar = campo("P1", 6);

        BotonRedondo btnCrear = boton("Crear memoria", ACENTO, false);
        BotonRedondo btnAsignar = boton("Asignar procesos", ACENTO, true);
        BotonRedondo btnLiberar = boton("Liberar", PELIGRO, false);
        BotonRedondo btnReiniciar = boton("Reiniciar", TENUE, false);

        JPanel config = new JPanel(new GridBagLayout());
        config.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.gridx = 0; g.weightx = 0; config.add(etiqueta("Particiones (KB)"), g);
        g.gridx = 1; g.weightx = 1; config.add(txtParticionesRAM, g);
        g.gridx = 2; g.weightx = 0; config.add(btnCrear, g);
        g.gridx = 3; g.weightx = 0; config.add(etiqueta("Liberar proceso"), g);
        g.gridx = 4; g.weightx = 0.4; config.add(txtPidLiberar, g);
        g.gridx = 5; g.weightx = 0; config.add(btnLiberar, g);

        g.gridy = 1;
        g.gridx = 0; g.weightx = 0; config.add(etiqueta("Estrategia"), g);
        g.gridx = 1; g.weightx = 1; config.add(cbEstrategia, g);
        g.gridx = 2; g.weightx = 0; config.add(btnAsignar, g);
        g.gridx = 5; g.weightx = 0; config.add(btnReiniciar, g);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        JPanel enc = encabezado("Memoria RAM", "Particiones fijas con First Fit, Best Fit y Worst Fit");
        enc.setAlignmentX(Component.LEFT_ALIGNMENT);
        Tarjeta cardConfig = tarjeta("Configuración", config);
        cardConfig.setAlignmentX(Component.LEFT_ALIGNMENT);
        norte.add(enc);
        norte.add(Box.createVerticalStrut(10));
        norte.add(cardConfig);

        memoriaPanel = new MemoriaPanel();
        JScrollPane scrollMapa = scroll(memoriaPanel);
        scrollMapa.setBorder(BorderFactory.createEmptyBorder());
        scrollMapa.getViewport().setBackground(Color.WHITE);
        scrollMapa.setOpaque(false);
        scrollMapa.getViewport().setOpaque(false);

        valMemTotal = valorMetrica();
        valMemUsada = valorMetrica();
        valMemFrag = valorMetrica();
        valMemLibre = valorMetrica();
        JPanel metricas = new JPanel(new GridLayout(4, 1, 0, 10));
        metricas.setOpaque(false);
        metricas.add(new Pastilla(nombreMetrica("Memoria total"), valMemTotal));
        metricas.add(new Pastilla(nombreMetrica("Usada por procesos"), valMemUsada));
        metricas.add(new Pastilla(nombreMetrica("Fragmentación interna"), valMemFrag));
        metricas.add(new Pastilla(nombreMetrica("Particiones libres"), valMemLibre));

        txtAreaMemoria = areaSalida();
        txtAreaMemoria.setFont(mono(12));
        JScrollPane scrollLog = scroll(txtAreaMemoria);
        scrollLog.setPreferredSize(new Dimension(100, 48));

        metricas.setPreferredSize(new Dimension(230, 100));
        JPanel centro = new JPanel(new BorderLayout(14, 0));
        centro.setOpaque(false);
        centro.add(tarjeta("Mapa de memoria", scrollMapa), BorderLayout.CENTER);
        centro.add(metricas, BorderLayout.EAST);

        panel.add(norte, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        panel.add(tarjeta("Registro de operaciones", scrollLog), BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearMemoria());
        btnAsignar.addActionListener(e -> asignarProcesosMemoria());
        btnLiberar.addActionListener(e -> liberarMemoria());
        btnReiniciar.addActionListener(e -> reiniciarMemoria());

        refrescarMemoria();
        return panel;
    }

    // =====================================================================
    // MÓDULO 3: INTERBLOQUEOS (usa GestorDeadlock)
    // =====================================================================
    private static final String[] ESCENARIOS = {
        "Caso seguro (5 procesos, 3 recursos)",
        "Interbloqueo (2 procesos, 2 recursos)",
        "Interbloqueo circular (3 procesos, 3 recursos)"
    };

    private JPanel conTitulo(String titulo, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(etiqueta(titulo), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPanelInterbloqueos() {
        JPanel panel = moduloBase();

        cbEscenario = combo(ESCENARIOS);
        cbEscenario.setPreferredSize(new Dimension(360, 38));
        BotonRedondo btnEvaluar = boton("Evaluar estado (Banquero)", ACENTO, true);

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fila.setOpaque(false);
        fila.add(etiqueta("Escenario"));
        fila.add(cbEscenario);
        fila.add(btnEvaluar);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        JPanel enc = encabezado("Interbloqueos", "Algoritmo del banquero (evitación), grafo de recursos y condiciones de Coffman");
        enc.setAlignmentX(Component.LEFT_ALIGNMENT);
        Tarjeta cardCtrl = tarjeta(null, fila);
        cardCtrl.setAlignmentX(Component.LEFT_ALIGNMENT);
        norte.add(enc);
        norte.add(Box.createVerticalStrut(10));
        norte.add(cardCtrl);

        tablaAsignacion = tabla(modelo(new String[]{"Proceso"}));
        tablaMaximo = tabla(modelo(new String[]{"Proceso"}));
        tablaDisponibles = tabla(modelo(new String[]{"R0"}));

        JScrollPane scAsig = scroll(tablaAsignacion);
        JScrollPane scMax = scroll(tablaMaximo);
        JScrollPane scDisp = scroll(tablaDisponibles);
        scDisp.setPreferredSize(new Dimension(100, 62));

        JPanel dosTablas = new JPanel(new GridLayout(1, 2, 12, 0));
        dosTablas.setOpaque(false);
        dosTablas.add(conTitulo("Asignación (Allocation)", scAsig));
        dosTablas.add(conTitulo("Máximo (Max)", scMax));

        JPanel datos = new JPanel(new BorderLayout(0, 10));
        datos.setOpaque(false);
        datos.add(dosTablas, BorderLayout.CENTER);
        datos.add(conTitulo("Disponibles (Available)", scDisp), BorderLayout.SOUTH);

        grafoPanel = new GrafoPanel();

        JPanel centro = new JPanel(new GridLayout(1, 2, 14, 0));
        centro.setOpaque(false);
        centro.add(tarjeta("Datos del sistema (editables)", datos));
        centro.add(tarjeta("Grafo de asignación de recursos", grafoPanel));

        veredicto = new Insignia("SIN EVALUAR");
        txtAreaInterbloqueos = areaSalida();
        txtAreaInterbloqueos.setFont(mono(12));
        JScrollPane scSalida = scroll(txtAreaInterbloqueos);
        scSalida.setPreferredSize(new Dimension(100, 130));

        JPanel filaVeredicto = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaVeredicto.setOpaque(false);
        filaVeredicto.add(veredicto);

        JPanel cuerpoSalida = new JPanel(new BorderLayout(0, 8));
        cuerpoSalida.setOpaque(false);
        cuerpoSalida.add(filaVeredicto, BorderLayout.NORTH);
        cuerpoSalida.add(scSalida, BorderLayout.CENTER);

        panel.add(norte, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        panel.add(tarjeta("Resultado del algoritmo", cuerpoSalida), BorderLayout.SOUTH);

        cbEscenario.addActionListener(e -> cargarEscenario(cbEscenario.getSelectedIndex()));
        btnEvaluar.addActionListener(e -> evaluarInterbloqueo());

        cargarEscenario(0);
        return panel;
    }

    // =====================================================================
    // MÓDULO 4: CONCURRENCIA (usa MonitorConcurrencia)
    // =====================================================================
    private JPanel tarjetaEscenario(String titulo, String subtitulo, JLabel valor, JLabel esperado, Insignia estado) {
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);

        JLabel sub = new JLabel(subtitulo);
        sub.setFont(ui(Font.PLAIN, 12));
        sub.setForeground(TENUE);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        valor.setFont(ui(Font.BOLD, 34));
        valor.setForeground(TEXTO);
        valor.setAlignmentX(Component.LEFT_ALIGNMENT);

        esperado.setFont(ui(Font.PLAIN, 13));
        esperado.setForeground(TENUE);
        esperado.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filaEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaEstado.setOpaque(false);
        filaEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaEstado.add(estado);

        cuerpo.add(sub);
        cuerpo.add(valor);
        cuerpo.add(esperado);
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(filaEstado);
        return tarjeta(titulo, cuerpo);
    }

    private JPanel crearPanelConcurrencia() {
        JPanel panel = moduloBase();

        txtHilos = campo("5", 4);
        txtIncrementos = campo("20", 5);
        BotonRedondo btnComparar = boton("Ejecutar comparación", ACENTO, true);
        BotonRedondo btnBinario = boton("Semáforo binario", ACENTO, false);
        BotonRedondo btnContador = boton("Semáforo contador", ACENTO, false);

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        fila1.setOpaque(false);
        fila1.add(etiqueta("Hilos"));
        fila1.add(txtHilos);
        fila1.add(etiqueta("Incrementos por hilo"));
        fila1.add(txtIncrementos);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        fila2.setOpaque(false);
        fila2.add(btnComparar);
        fila2.add(btnBinario);
        fila2.add(btnContador);

        JPanel config = new JPanel();
        config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
        config.setOpaque(false);
        fila1.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila2.setAlignmentX(Component.LEFT_ALIGNMENT);
        config.add(fila1);
        config.add(fila2);

        valSin = new JLabel("—");
        espSin = new JLabel("Valor esperado: —");
        estSin = new Insignia("PENDIENTE");
        valCon = new JLabel("—");
        espCon = new JLabel("Valor esperado: —");
        estCon = new Insignia("PENDIENTE");

        JPanel escenarios = new JPanel(new GridLayout(1, 2, 14, 0));
        escenarios.setOpaque(false);
        escenarios.add(tarjetaEscenario("Sin sincronización", "Acceso libre al contador compartido", valSin, espSin, estSin));
        escenarios.add(tarjetaEscenario("Con sincronización (mutex)", "Sección crítica protegida con ReentrantLock", valCon, espCon, estCon));
        escenarios.setPreferredSize(new Dimension(100, 215));
        escenarios.setMaximumSize(new Dimension(Integer.MAX_VALUE, 215));

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        JPanel enc = encabezado("Concurrencia", "Hilos, condición de carrera, mutex y semáforos");
        enc.setAlignmentX(Component.LEFT_ALIGNMENT);
        Tarjeta cardConfig = tarjeta("Configuración", config);
        cardConfig.setAlignmentX(Component.LEFT_ALIGNMENT);
        escenarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        norte.add(enc);
        norte.add(Box.createVerticalStrut(10));
        norte.add(cardConfig);
        norte.add(Box.createVerticalStrut(12));
        norte.add(escenarios);

        txtAreaConcurrencia = areaSalida();
        txtAreaConcurrencia.setFont(mono(12));

        panel.add(norte, BorderLayout.NORTH);
        panel.add(tarjeta("Salida de la simulación", scroll(txtAreaConcurrencia)), BorderLayout.CENTER);

        btnComparar.addActionListener(e -> ejecutarComparacion());
        btnBinario.addActionListener(e -> ejecutarSemaforoBinario());
        btnContador.addActionListener(e -> ejecutarSemaforoContador());
        return panel;
    }

    private JPanel crearPanelPCB() {
        BotonRedondo btn = boton("Actualizar estados", ACENTO, true);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(btn);

        modelPCB = modelo(new String[]{"PID", "Estado Actual", "Prioridad", "Memoria Asignada", "T. Llegada"});
        tablaPCB = tabla(modelPCB);

        JPanel panel = moduloBase();
        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        norte.add(encabezado("PCB y ciclo de vida", "Bloque de control de cada proceso y su estado actual"), BorderLayout.NORTH);
        norte.add(top, BorderLayout.CENTER);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(tarjeta("Tabla de procesos", scroll(tablaPCB)), BorderLayout.CENTER);

        btn.addActionListener(e -> actualizarPCBVisually());
        return panel;
    }

    // =====================================================================
    // LÓGICA DE EVENTOS (misma que la versión anterior)
    // =====================================================================
    private void agregarProceso() {
        try {
            String pid = txtPID.getText();
            int llegada = Integer.parseInt(txtLlegada.getText());
            int rafaga = Integer.parseInt(txtRafaga.getText());
            int prioridad = Integer.parseInt(txtPrioridad.getText());
            int memoria = Integer.parseInt(txtMemoria.getText());

            Proceso p = new Proceso(pid, llegada, rafaga, prioridad, memoria);
            listaProcesos.add(p);

            modelIngresados.addRow(new Object[]{pid, llegada, rafaga, prioridad, memoria + " KB"});
            txtPID.setText("P" + (listaProcesos.size() + 1));
            actualizarEstado();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Por favor revise que los datos ingresados sean válidos.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarListas() {
        listaProcesos.clear();
        modelIngresados.setRowCount(0);
        modelResultados.setRowCount(0);
        ganttPanel.setBloques(null);
        valRetorno.setText("—");
        valEspera.setText("—");
        valRespuesta.setText("—");
        txtPID.setText("P1");
        actualizarEstado();
    }

    private void ejecutarSimulacion() {
        if (listaProcesos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un proceso para simular.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modelResultados.setRowCount(0);
        List<PlanificadorCPU.BloqueGantt> gantt;

        int seleccion = cbAlgoritmo.getSelectedIndex();
        if (seleccion == 0) {
            gantt = PlanificadorCPU.simularFCFS(listaProcesos);
        } else if (seleccion == 1) {
            gantt = PlanificadorCPU.simularSJF(listaProcesos);
        } else {
            int q = Integer.parseInt(txtQuantum.getText());
            gantt = PlanificadorCPU.simularRoundRobin(listaProcesos, q);
        }

        double sumaRet = 0, sumaEsp = 0, sumaResp = 0;
        for (Proceso p : listaProcesos) {
            modelResultados.addRow(new Object[]{
                p.getPid(),
                p.getTiempoFinalizacion() + " ms",
                p.getTiempoRetorno() + " ms",
                p.getTiempoEspera() + " ms",
                p.getTiempoRespuesta() + " ms"
            });
            sumaRet += p.getTiempoRetorno();
            sumaEsp += p.getTiempoEspera();
            sumaResp += p.getTiempoRespuesta();
        }

        int total = listaProcesos.size();
        ganttPanel.setBloques(gantt);
        valRetorno.setText(String.format("%.2f ms", sumaRet / total));
        valEspera.setText(String.format("%.2f ms", sumaEsp / total));
        valRespuesta.setText(String.format("%.2f ms", sumaResp / total));

        actualizarPCBVisually();
    }

    // ---------------------------------------------------------------------
    // MEMORIA: usa GestorMemoria (First Fit, Best Fit, Worst Fit)
    // ---------------------------------------------------------------------
    private static final String[] ESTRATEGIAS = {"First Fit", "Best Fit", "Worst Fit"};

    private int[] leerParticiones() {
        String[] partes = txtParticionesRAM.getText().trim().split("[,;\\s]+");
        int[] r = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            r[i] = Integer.parseInt(partes[i]);
            if (r[i] <= 0) throw new NumberFormatException("Tamaño no positivo");
        }
        return r;
    }

    private boolean crearMemoria() {
        try {
            int[] tamanos = leerParticiones();
            gestorMemoria = new GestorMemoria(tamanos);
            asignados.clear();
            txtAreaMemoria.setText("");
            logMemoria("Memoria creada con " + tamanos.length + " particiones.");
            refrescarMemoria();
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Escriba los tamaños de las particiones en KB, separados por comas.\nEjemplo: 100, 500, 200, 300, 600",
                    "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void asignarProcesosMemoria() {
        if (listaProcesos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero agregue procesos en el módulo 01 (Planificador CPU).", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (gestorMemoria == null && !crearMemoria()) {
            return;
        }
        int idx = cbEstrategia.getSelectedIndex();
        String estrategia = ESTRATEGIAS[idx];
        boolean huboNuevos = false;

        for (Proceso p : listaProcesos) {
            if (asignados.contains(p.getPid())) {
                continue;
            }
            huboNuevos = true;
            boolean ok;
            if (idx == 0) {
                ok = gestorMemoria.asignarFirstFit(p);
            } else if (idx == 1) {
                ok = gestorMemoria.asignarBestFit(p);
            } else {
                ok = gestorMemoria.asignarWorstFit(p);
            }
            if (ok) {
                asignados.add(p.getPid());
                BloqueMemoria b = bloqueDe(p.getPid());
                logMemoria(String.format("[%s] %s (%d KB) -> Bloque %d (%d KB), fragmentación interna %d KB",
                        estrategia, p.getPid(), p.getTamañoMemoriaMB(), b.getId(), b.getTamañoKB(), b.getFragmentacionInterna()));
            } else {
                logMemoria(String.format("[%s] %s (%d KB) -> RECHAZADO: ninguna partición libre alcanza",
                        estrategia, p.getPid(), p.getTamañoMemoriaMB()));
            }
        }
        if (!huboNuevos) {
            logMemoria("Todos los procesos ya tienen memoria asignada.");
        }
        refrescarMemoria();
    }

    private void liberarMemoria() {
        String pid = txtPidLiberar.getText().trim();
        if (gestorMemoria == null || !asignados.contains(pid)) {
            JOptionPane.showMessageDialog(this, "El proceso \"" + pid + "\" no tiene memoria asignada.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BloqueMemoria b = bloqueDe(pid);
        int id = b == null ? 0 : b.getId();
        gestorMemoria.liberarProceso(pid);
        asignados.remove(pid);
        logMemoria("[LIBERADO] " + pid + " dejó el Bloque " + id + ".");
        refrescarMemoria();
    }

    private void reiniciarMemoria() {
        if (gestorMemoria == null) {
            return;
        }
        gestorMemoria.reiniciarMemoria();
        asignados.clear();
        logMemoria("Memoria reiniciada: todas las particiones quedaron libres.");
        refrescarMemoria();
    }

    private BloqueMemoria bloqueDe(String pid) {
        if (gestorMemoria == null) {
            return null;
        }
        for (BloqueMemoria b : gestorMemoria.getParticiones()) {
            if (b.isOcupado() && b.getPidProcesoAsignado().equals(pid)) {
                return b;
            }
        }
        return null;
    }

    private void logMemoria(String linea) {
        txtAreaMemoria.append(linea + "\n");
        txtAreaMemoria.setCaretPosition(txtAreaMemoria.getDocument().getLength());
    }

    private void refrescarMemoria() {
        Map<String, Color> colores = new HashMap<>();
        for (int i = 0; i < listaProcesos.size(); i++) {
            colores.put(listaProcesos.get(i).getPid(), PALETA_GANTT[i % PALETA_GANTT.length]);
        }
        List<BloqueMemoria> bloques = gestorMemoria == null ? null : gestorMemoria.getParticiones();
        memoriaPanel.setDatos(bloques, colores);

        if (bloques == null) {
            valMemTotal.setText("—");
            valMemUsada.setText("—");
            valMemFrag.setText("—");
            valMemLibre.setText("—");
            return;
        }
        int total = 0, usada = 0, frag = 0, libres = 0;
        for (BloqueMemoria b : bloques) {
            total += b.getTamañoKB();
            if (b.isOcupado()) {
                usada += b.getTamañoKB() - b.getFragmentacionInterna();
                frag += b.getFragmentacionInterna();
            } else {
                libres += b.getTamañoKB();
            }
        }
        valMemTotal.setText(total + " KB");
        valMemUsada.setText(usada + " KB");
        valMemFrag.setText(frag + " KB");
        valMemLibre.setText(libres + " KB");
    }

    // ---------------------------------------------------------------------
    // Utilidad: captura lo que las clases de lógica imprimen con System.out
    // ---------------------------------------------------------------------
    private String capturarSalida(Runnable tarea) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            tarea.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------------
    // INTERBLOQUEOS
    // ---------------------------------------------------------------------
    private DefaultTableModel modeloMatriz(int[][] datos) {
        int m = datos[0].length;
        String[] cols = new String[m + 1];
        cols[0] = "Proc.";
        for (int j = 0; j < m; j++) cols[j + 1] = "R" + j;
        DefaultTableModel mod = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int fila, int col) { return col > 0; }
        };
        for (int i = 0; i < datos.length; i++) {
            Object[] fila = new Object[m + 1];
            fila[0] = "P" + i;
            for (int j = 0; j < m; j++) fila[j + 1] = datos[i][j];
            mod.addRow(fila);
        }
        return mod;
    }

    private DefaultTableModel modeloDisponibles(int[] d) {
        String[] cols = new String[d.length];
        Object[] fila = new Object[d.length];
        for (int j = 0; j < d.length; j++) {
            cols[j] = "R" + j;
            fila[j] = d[j];
        }
        DefaultTableModel mod = new DefaultTableModel(cols, 0);
        mod.addRow(fila);
        return mod;
    }

    private void cargarEscenario(int idx) {
        int[][] al;
        int[][] mx;
        int[] av;
        if (idx == 0) {
            al = new int[][]{{0, 1, 0}, {2, 0, 0}, {3, 0, 2}, {2, 1, 1}, {0, 0, 2}};
            mx = new int[][]{{7, 5, 3}, {3, 2, 2}, {9, 0, 2}, {2, 2, 2}, {4, 3, 3}};
            av = new int[]{3, 3, 2};
        } else if (idx == 1) {
            al = new int[][]{{1, 0}, {0, 1}};
            mx = new int[][]{{1, 1}, {1, 1}};
            av = new int[]{0, 0};
        } else {
            al = new int[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
            mx = new int[][]{{1, 1, 0}, {0, 1, 1}, {1, 0, 1}};
            av = new int[]{0, 0, 0};
        }
        tablaAsignacion.setModel(modeloMatriz(al));
        tablaMaximo.setModel(modeloMatriz(mx));
        tablaDisponibles.setModel(modeloDisponibles(av));
        tablaAsignacion.setRowHeight(26);
        tablaMaximo.setRowHeight(26);
        tablaDisponibles.setRowHeight(26);
        grafoPanel.setDatos(al, mx, av, null);
        veredicto.estado("SIN EVALUAR", new Color(0x94A3B8));
        txtAreaInterbloqueos.setText("Escenario cargado. Puede editar las matrices y presionar \"Evaluar estado\".");
    }

    private int[][] leerMatriz(JTable t) {
        if (t.isEditing()) {
            t.getCellEditor().stopCellEditing();
        }
        DefaultTableModel mod = (DefaultTableModel) t.getModel();
        int n = mod.getRowCount();
        int m = mod.getColumnCount() - 1;
        int[][] r = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                r[i][j] = Integer.parseInt(String.valueOf(mod.getValueAt(i, j + 1)).trim());
                if (r[i][j] < 0) throw new NumberFormatException("Valor negativo");
            }
        }
        return r;
    }

    private int[] leerVector(JTable t) {
        if (t.isEditing()) {
            t.getCellEditor().stopCellEditing();
        }
        DefaultTableModel mod = (DefaultTableModel) t.getModel();
        int m = mod.getColumnCount();
        int[] r = new int[m];
        for (int j = 0; j < m; j++) {
            r[j] = Integer.parseInt(String.valueOf(mod.getValueAt(0, j)).trim());
            if (r[j] < 0) throw new NumberFormatException("Valor negativo");
        }
        return r;
    }

    private void evaluarInterbloqueo() {
        int[][] al;
        int[][] mx;
        int[] av;
        try {
            al = leerMatriz(tablaAsignacion);
            mx = leerMatriz(tablaMaximo);
            av = leerVector(tablaDisponibles);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Todos los valores de las tablas deben ser números enteros mayores o iguales a 0.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int n = al.length;
        int m = av.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (mx[i][j] < al[i][j]) {
                    JOptionPane.showMessageDialog(this, "El máximo de P" + i + " en R" + j + " no puede ser menor que lo que ya tiene asignado.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        GestorDeadlock gestor = new GestorDeadlock(n, m, al, mx, av);
        boolean[] seguro = new boolean[1];
        String salida = capturarSalida(() -> {
            gestor.mostrarMatrices();
            seguro[0] = gestor.evaluarEstadoSeguro();
        });
        boolean[] bloqueados = gestor.getProcesosBloqueados();

        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (bloqueados[i]) {
                if (nombres.length() > 0) nombres.append(", ");
                nombres.append("P").append(i);
            }
        }
        boolean retencion = false;
        for (int i = 0; i < n; i++) {
            int tiene = 0;
            int necesita = 0;
            for (int j = 0; j < m; j++) {
                tiene += al[i][j];
                necesita += mx[i][j] - al[i][j];
            }
            if (tiene > 0 && necesita > 0) retencion = true;
        }

        StringBuilder sb = new StringBuilder(salida.trim());
        sb.append("\n\n--- CONDICIONES DE INTERBLOQUEO (COFFMAN) ---\n");
        sb.append("1. Exclusión mutua    : Presente (cada recurso lo usa un solo proceso a la vez)\n");
        sb.append("2. Retención y espera : ").append(retencion
                ? "Presente (hay procesos que retienen recursos y piden más)\n"
                : "No se presenta (ningún proceso retiene recursos mientras espera)\n");
        sb.append("3. No apropiación     : Presente (los recursos no se quitan a la fuerza)\n");
        sb.append("4. Espera circular    : ").append(seguro[0]
                ? "No se detecta (existe una secuencia segura)\n"
                : "Presente entre " + nombres + "\n");
        sb.append(seguro[0]
                ? "Conclusión: no hay interbloqueo, porque las cuatro condiciones no se cumplen a la vez.\n"
                : "Conclusión: se cumplen las cuatro condiciones, por lo tanto hay interbloqueo.\n");
        sb.append("\nEstrategia aplicada: EVITACIÓN con el algoritmo del banquero.\n");
        if (!seguro[0]) {
            String primero = nombres.toString().split(",")[0].trim();
            sb.append("Recuperación posible: finalizar o expropiar recursos a ").append(primero).append(" y volver a evaluar.\n");
        }

        txtAreaInterbloqueos.setText(sb.toString());
        txtAreaInterbloqueos.setCaretPosition(0);
        if (seguro[0]) {
            veredicto.estado("ESTADO SEGURO: sin interbloqueo", EXITO);
        } else {
            veredicto.estado("INTERBLOQUEO DETECTADO en " + nombres, PELIGRO);
        }
        grafoPanel.setDatos(al, mx, av, bloqueados);
    }

    // ---------------------------------------------------------------------
    // CONCURRENCIA
    // ---------------------------------------------------------------------
    private int[] leerHilosEIncrementos() {
        try {
            int h = Integer.parseInt(txtHilos.getText().trim());
            int i = Integer.parseInt(txtIncrementos.getText().trim());
            if (h < 1 || h > 50 || i < 1 || i > 500) throw new NumberFormatException("Fuera de rango");
            return new int[]{h, i};
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Use entre 1 y 50 hilos y entre 1 y 500 incrementos por hilo.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void mostrarResultado(JLabel valor, JLabel esperado, Insignia estado, int obtenido, int esp, boolean sincronizado) {
        valor.setText(String.valueOf(obtenido));
        esperado.setText("Valor esperado: " + esp);
        if (obtenido == esp) {
            valor.setForeground(EXITO);
            estado.estado(sincronizado ? "CONSISTENTE" : "SIN ERROR ESTA VEZ", EXITO);
        } else {
            valor.setForeground(PELIGRO);
            estado.estado("CONDICIÓN DE CARRERA: se perdieron " + (esp - obtenido), PELIGRO);
        }
    }

    private void ejecutarComparacion() {
        int[] d = leerHilosEIncrementos();
        if (d == null) {
            return;
        }
        final int hilos = d[0];
        final int incrementos = d[1];
        final int esperado = hilos * incrementos;

        MonitorConcurrencia sin = new MonitorConcurrencia();
        String salidaSin = capturarSalida(() -> sin.ejecutarSinSincronizacion(hilos, incrementos));
        int obtenidoSin = sin.getContadorCompartido();

        MonitorConcurrencia con = new MonitorConcurrencia();
        String salidaCon = capturarSalida(() -> con.ejecutarConSincronizacion(hilos, incrementos));
        int obtenidoCon = con.getContadorCompartido();

        mostrarResultado(valSin, espSin, estSin, obtenidoSin, esperado, false);
        mostrarResultado(valCon, espCon, estCon, obtenidoCon, esperado, true);
        txtAreaConcurrencia.setText(salidaSin.trim() + "\n\n" + salidaCon.trim());
        txtAreaConcurrencia.setCaretPosition(0);
    }

    private void ejecutarSemaforoBinario() {
        int[] d = leerHilosEIncrementos();
        if (d == null) {
            return;
        }
        final int hilos = d[0];
        final int incrementos = d[1];
        String salida = capturarSalida(() -> new MonitorConcurrencia().ejecutarConSemaforoBinario(hilos, incrementos));
        txtAreaConcurrencia.setText(salida.trim());
        txtAreaConcurrencia.setCaretPosition(0);
    }

    private void ejecutarSemaforoContador() {
        String salida = capturarSalida(() -> new MonitorConcurrencia().simularSemaforoContador());
        txtAreaConcurrencia.setText(salida.trim());
        txtAreaConcurrencia.setCaretPosition(0);
    }

    private void actualizarPCBVisually() {
        modelPCB.setRowCount(0);
        if (listaProcesos.isEmpty()) {
            return;
        }
        for (Proceso p : listaProcesos) {
            String estado = p.getEstado() != null ? p.getEstado() : "Listo";
            if (p.getTiempoFinalizacion() > 0) {
                estado = "Terminado";
            }
            modelPCB.addRow(new Object[]{
                p.getPid(),
                estado,
                p.getPrioridad(),
                p.getTamañoMemoriaMB() + " KB",
                p.getTiempoLlegada() + " ms"
            });
        }
    }
}