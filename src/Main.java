import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;

public class Main {

    // ── Colour Palette (Catppuccin Mocha) ─────────────────────────────────────
    private static final Color BG      = new Color(0x1e1e2e);
    private static final Color SURFACE = new Color(0x313244);
    private static final Color OVERLAY = new Color(0x45475a);
    private static final Color TEXT    = new Color(0xcdd6f4);
    private static final Color SUBTEXT = new Color(0xa6adc8);
    private static final Color ACCENT  = new Color(0xcba6f7); // lavender
    private static final Color BLUE    = new Color(0x89b4fa);
    private static final Color GREEN   = new Color(0xa6e3a1);
    private static final Color RED     = new Color(0xf38ba8);
    private static final Color YELLOW  = new Color(0xf9e2af);
    private static final Color MUTED   = new Color(0x585b70);

    private static JLabel    statusLabel;
    private static JTextArea textArea;

    // ── Entry Point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Text File Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(580, 500);
        frame.setMinimumSize(new Dimension(440, 380));
        frame.setLocationRelativeTo(null);
        frame.setBackground(BG);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

        root.add(buildHeader(),       BorderLayout.NORTH);
        root.add(buildEditor(),       BorderLayout.CENTER);
        root.add(buildFooter(frame),  BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private static JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel dot = new JLabel("//");
        dot.setFont(getFont(Font.BOLD, 13));
        dot.setForeground(ACCENT);
        dot.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Text File Manager");
        title.setFont(getFont(Font.BOLD, 24));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Write, save, load and compute from  data.txt");
        subtitle.setFont(getFont(Font.PLAIN, 13));
        subtitle.setForeground(SUBTEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thin accent divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(OVERLAY);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(dot);
        panel.add(Box.createVerticalStrut(4));
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(16));
        panel.add(sep);
        return panel;
    }

    // ── Text Editor ───────────────────────────────────────────────────────────
    private static JScrollPane buildEditor() {
        textArea = new JTextArea();
        textArea.setFont(getFont(Font.PLAIN, 14));
        textArea.setBackground(SURFACE);
        textArea.setForeground(TEXT);
        textArea.setCaretColor(ACCENT);
        textArea.setSelectionColor(OVERLAY);
        textArea.setSelectedTextColor(TEXT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OVERLAY, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBackground(SURFACE);

        // Dark scrollbar
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = OVERLAY;
                trackColor = SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        return scroll;
    }

    // ── Footer (Buttons + Status bar) ─────────────────────────────────────────
    private static JPanel buildFooter(JFrame frame) {
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Button row
        JPanel btnRow = new JPanel(new GridLayout(1, 4, 10, 0));
        btnRow.setBackground(BG);

        JButton saveBtn  = makeButton("Save",   ACCENT);
        JButton loadBtn  = makeButton("Load",   BLUE);
        JButton addBtn   = makeButton("Add",     YELLOW);
        JButton clearBtn = makeButton("Clear",   MUTED);

        btnRow.add(saveBtn);
        btnRow.add(loadBtn);
        btnRow.add(addBtn);
        btnRow.add(clearBtn);

        // Status bar
        statusLabel = new JLabel("Ready — start typing or load a file");
        statusLabel.setFont(getFont(Font.PLAIN, 12));
        statusLabel.setForeground(SUBTEXT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        footer.add(btnRow,      BorderLayout.CENTER);
        footer.add(statusLabel, BorderLayout.SOUTH);

        // ── Actions ───────────────────────────────────────────────────────────
        saveBtn.addActionListener(e -> {
            File f = resolveDataFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(textArea.getText());
                setStatus("Saved to " + f.getAbsolutePath(), GREEN);
            } catch (IOException ex) {
                setStatus("Error: Save failed - " + ex.getMessage(), RED);
            }
        });

        loadBtn.addActionListener(e -> {
            File f = resolveDataFile();
            if (!f.exists()) {
                setStatus("Error: data.txt not found at " + f.getAbsolutePath(), RED);
                return;
            }
            try {
                String content = Files.readString(f.toPath());
                textArea.setText(content);
                textArea.setCaretPosition(0);
                setStatus("Loaded " + f.getAbsolutePath(), GREEN);
            } catch (IOException ex) {
                setStatus("Error: Load failed - " + ex.getMessage(), RED);
            }
        });

        addBtn.addActionListener(e -> {
            String raw = textArea.getText().trim();
            if (raw.isEmpty()) {
                setStatus("Warning: Type some numbers first (comma or newline separated)", YELLOW);
                return;
            }
            // Accept comma-separated and/or newline-separated integers
            String[] tokens = raw.split("[,\\s]+");
            long sum = 0;
            int count = 0;
            for (String t : tokens) {
                t = t.trim();
                if (t.isEmpty() || t.startsWith("=")) continue; // skip previous results
                try {
                    sum += Long.parseLong(t);
                    count++;
                } catch (NumberFormatException ex) {
                    setStatus("Error: \"" + t + "\" is not a valid integer - fix and retry", RED);
                    return;
                }
            }
            if (count == 0) {
                setStatus("Warning: No numbers found to add", YELLOW);
                return;
            }
            textArea.append("\n= Sum of " + count + " number" + (count == 1 ? "" : "s") + ": " + sum);
            setStatus("Sum: " + sum + "  (from " + count + " numbers)", GREEN);
        });

        clearBtn.addActionListener(e -> {
            textArea.setText("");
            setStatus("Cleared", SUBTEXT);
        });

        return footer;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /** Resolve data.txt next to the .class file so it's always predictable. */
    private static File resolveDataFile() {
        try {
            File loc = new File(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (loc.isFile()) loc = loc.getParentFile(); // strip filename if it's a jar
            return new File(loc, "data.txt");
        } catch (URISyntaxException e) {
            return new File("data.txt"); // working-directory fallback
        }
    }

    private static void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private static Font getFont(int style, int size) {
        for (String name : new String[]{"JetBrains Mono", "Cascadia Code", "Fira Code", "Consolas"}) {
            Font f = new Font(name, style, size);
            if (f.getFamily().equalsIgnoreCase(name)) return f;
        }
        return new Font(Font.MONOSPACED, style, size);
    }

    /** Custom rounded button with smooth hover colour shift. */
    private static JButton makeButton(String label, Color base) {
        Color hoverColor = base.brighter();
        JButton btn = new JButton(label) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? hoverColor : base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btn.setForeground(BG);
        btn.setPreferredSize(new Dimension(0, 40));
        return btn;
    }
}