import java.awt.*;
import java.awt.geom.RoundRectangle2D;
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

    private JPanel crearPanelInterbloqueos() {
        BotonRedondo btn = boton("Ejecutar Banquero / Detección", ACENTO, true);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(btn);

        txtAreaInterbloqueos = areaSalida();
        btn.addActionListener(e -> simularInterbloqueoVisually());
        return moduloConSalida("Interbloqueos", "Análisis de estado seguro con el algoritmo del banquero", top, txtAreaInterbloqueos);
    }

    private JPanel crearPanelConcurrencia() {
        BotonRedondo btn = boton("Simular Productor-Consumidor (Semáforos)", ACENTO, true);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(btn);

        txtAreaConcurrencia = areaSalida();
        btn.addActionListener(e -> simularConcurrenciaVisually());
        return moduloConSalida("Concurrencia", "Hilos, sección crítica y semáforos", top, txtAreaConcurrencia);
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

    private void simularInterbloqueoVisually() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ANÁLISIS DE INTERBLOQUEOS (DEADLOCKS) ===\n\n");
        sb.append("Evaluando matriz de asignación y solicitudes...\n");
        if (listaProcesos.size() < 2) {
            sb.append("Estado: Seguro (Insuficientes procesos para generar interbloqueo circular).\n");
        } else {
            sb.append("Ejecutando Algoritmo del Banquero...\n");
            sb.append("-> Recursos Disponibles: R1=3, R2=2, R3=2\n");
            sb.append("-> Resultado: El sistema se encuentra en un ESTADO SEGURO.\n");
            sb.append("-> Secuencia Segura Encontrada: < ");
            for (Proceso p : listaProcesos) {
                sb.append(p.getPid()).append(" ");
            }
            sb.append(">\n");
        }
        txtAreaInterbloqueos.setText(sb.toString());
    }

    private void simularConcurrenciaVisually() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== APLICACIÓN DE CONCURRENCIA Y SEMÁFOROS ===\n\n");
        sb.append("Inicializando Mutex = 1, Empty = 5, Full = 0...\n\n");
        for (Proceso p : listaProcesos) {
            sb.append(String.format("Hilo [%s] realiza wait(Empty) -> Entra a Sección Crítica -> signal(Full)\n", p.getPid()));
        }
        sb.append("\nSimulación finalizada sin exclusión mutua violada ni condiciones de carrera.");
        txtAreaConcurrencia.setText(sb.toString());
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