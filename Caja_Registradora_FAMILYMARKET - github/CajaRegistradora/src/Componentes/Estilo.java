package Componentes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

/** colores y componentes con el mismo estilo para todos los formularios */
public class Estilo {
    public static final Color AZUL_OSCURO = new Color(13, 27, 47);
    public static final Color AZUL = new Color(35, 113, 237);
    public static final Color AZUL_MEDIO = new Color(31, 55, 85);
    public static final Color FONDO = new Color(236, 240, 247);
    public static final Color MENU = new Color(12, 47, 100);
    public static final Color MENU_SEL = new Color(52, 116, 206);
    public static final Color TEXTO = new Color(28, 40, 60);
    public static final Color GRIS = new Color(110, 125, 148);
    public static final Color ROJO = new Color(200, 40, 40);
    public static final Color BORDE = new Color(208, 217, 230);

    private Estilo() {}

    public static DefaultTableModel modelo(String[] titulos) {
        return new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static void tabla(JTable tabla) {
        tabla.setRowHeight(26);
        tabla.setFont(new Font("SansSerif", 0, 13));
        tabla.setGridColor(new Color(220, 226, 236));
        tabla.setSelectionBackground(new Color(35, 113, 237));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("SansSerif", 1, 13));
        tabla.getTableHeader().setBackground(new Color(13, 27, 47));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        if (tabla.getTableHeader().getDefaultRenderer() instanceof javax.swing.table.DefaultTableCellRenderer) {
            ((javax.swing.table.DefaultTableCellRenderer) tabla.getTableHeader().getDefaultRenderer())
                    .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
    }

    public static Border titulado(String titulo) {
        return BorderFactory.createTitledBorder(null, titulo, TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("SansSerif", 1, 12));
    }

    public static JTextField campo(String titulo) {
        JTextField t = new JTextField();
        t.setBackground(Color.WHITE);
        t.setForeground(TEXTO);
        t.setFont(new Font("SansSerif", 0, 14));
        t.setBorder(titulado(titulo));
        return t;
    }

    public static JSpinner spinner(String titulo, SpinnerModel modelo) {
        JSpinner s = new JSpinner(modelo);
        s.setFont(new Font("SansSerif", 0, 14));
        s.setBorder(titulado(titulo));
        return s;
    }

    public static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", 1, 13));
        b.setFocusPainted(false);
        return b;
    }

    public static JLabel etiqueta(String texto, int tam, boolean negrita, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", negrita ? 1 : 0, tam));
        l.setForeground(color);
        return l;
    }

    // panel blanco con borde y titulo opcional
    public static JPanel tarjeta(String titulo) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(titulo == null ? BorderFactory.createLineBorder(BORDE) : titulado(titulo));
        return p;
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // da a un JScrollPane creado en el diseñador el mismo aspecto que scroll(JTable)
    public static void estilizar(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
    }

    // alto fijo para componentes dentro de un BoxLayout vertical
    public static void alto(JComponent c, int alto) {
        c.setPreferredSize(new Dimension(100, alto));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public static void alCambiar(JTextComponent c, Runnable accion) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
