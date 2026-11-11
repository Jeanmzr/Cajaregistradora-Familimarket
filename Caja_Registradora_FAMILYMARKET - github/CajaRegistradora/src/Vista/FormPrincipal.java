package Vista;

import Componentes.Estilo;
import Componentes.Iconos;
import Componentes.Logo;
import Componentes.Modulo;
import Modelo.Moneda;
import Modelo.Usuario;
import Modelo.Venta;
import Servicio.ServicioSesion;
import Servicio.ServicioVentas;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicButtonUI;

/** Ventana principal: menu lateral + barra superior. Cada rol ve solo los modulos a los que tiene acceso. */
public class FormPrincipal extends javax.swing.JFrame {
    private static final int ANCHO = 1240;     // ancho de la ventana
    private final CardLayout cartas = new CardLayout();
    private final Map<String, Modulo> modulos = new LinkedHashMap<>();
    private final Map<String, JButton> botones = new LinkedHashMap<>();

    public FormPrincipal() {
        initComponents();
        Usuario u = ServicioSesion.getUsuarioActual();
        setTitle("Family Market - " + u.getTipo());
        setLocationRelativeTo(null);

        // lo que no se puede ver en el diseñador (logo, iconos, tarjetas) se arma aqui
        pnlLogo.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlLogo.add(new Logo());
        pnlContenido.setLayout(cartas);

        // ----- modulos: cada rol ve solo los suyos -----
        registrar("INICIO", btnInicio, "inicio", new FormInicio());
        registrar("CLIENTES", btnClientes, "clientes", new FormClientes());
        registrar("PRODUCTOS", btnProductos, "productos", new FormProductos());
        registrar("VENTAS", btnVentas, "ventas", new FormVentas());
        registrar("BOLETAS", btnBoletas, "boletas", new FormBoletas());
        if (u.puedeGestionarUsuarios()) {
            registrar("USUARIOS", btnUsuarios, "usuarios", new FormUsuarios());
        } else {
            btnUsuarios.setVisible(false);
        }
        if (u.puedeVerReportes()) {
            registrar("VENDIDO", btnVendido, "vendido", new FormProductoVendido());
            registrar("REPORTES", btnReportes, "reportes", new FormReportes());
        } else {
            btnVendido.setVisible(false);
            btnReportes.setVisible(false);
        }
        acomodarMenu();

        // ----- usuario en la parte baja del menu -----
        lblAvatar.setIcon(new Avatar());
        lblRolUsuario.setText(u.getTipo().substring(0, 1) + u.getTipo().substring(1).toLowerCase());
        lblNombreUsuario.setText(u.getUsuario());

        // ----- barra superior -----
        btnMenuToggle.setIcon(Iconos.de("menu", Estilo.TEXTO, 22));
        btnCampana.setIcon(Iconos.de("campana", Estilo.TEXTO, 22));
        btnCampana.setUI(new CampanaUI());
        btnFlecha.setIcon(Iconos.de("flecha", Estilo.TEXTO, 16));
        ajustarDerecha();
        mostrar("INICIO");
    }

    // ---------- menu ----------
    private void registrar(String clave, JButton boton, String icono, JPanel panel) {
        modulos.put(clave, (Modulo) panel);
        pnlContenido.add(panel, clave);
        botones.put(clave, boton);
        boton.setUI(new MenuUI());
        boton.setIcon(Iconos.de(icono, Color.WHITE, 20));
        boton.setIconTextGap(14);
        boton.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 10));
        boton.setRolloverEnabled(true);
    }

    // los botones que el rol no puede usar se ocultan; los demas se acomodan sin dejar huecos
    private void acomodarMenu() {
        JButton[] todos = {btnInicio, btnClientes, btnProductos, btnVentas, btnBoletas, btnUsuarios, btnVendido, btnReportes};
        int y = 88;
        for (JButton b : todos) {
            if (b.isVisible()) {
                colocar(pnlMenu, b, 0, y, 235, 44);
                y += 46;
            }
        }
    }

    // AbsoluteLayout guarda la posicion de cada componente: para moverlo hay que volver a agregarlo
    private static void colocar(JComponent padre, JComponent hijo, int x, int y, int ancho, int alto) {
        padre.remove(hijo);
        padre.add(hijo, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y, ancho, alto));
        padre.revalidate();
        padre.repaint();
    }

    private void mostrar(String clave) {
        modulos.get(clave).actualizar();
        cartas.show(pnlContenido, clave);
        for (Map.Entry<String, JButton> e : botones.entrySet()) {
            e.getValue().putClientProperty("seleccionado", e.getKey().equals(clave));
            e.getValue().repaint();
        }
        btnCampana.putClientProperty("cantidad", ventasDeHoy());
        btnCampana.repaint();
    }

    // al ocultar el menu, el contenido ocupa todo el ancho
    private void ajustarDerecha() {
        int x = pnlMenu.isVisible() ? 235 : 0;
        int ancho = ANCHO - x;
        colocar(pnlRaiz, pnlDerecha, x, 0, ancho, 700);
        colocar(pnlDerecha, pnlBarra, 0, 0, ancho, 50);
        colocar(pnlDerecha, pnlContenido, 0, 50, ancho, 650);
        colocar(pnlBarra, btnCampana, ancho - 80, 9, 36, 32);
        colocar(pnlBarra, btnFlecha, ancho - 38, 9, 34, 32);
    }

    private int ventasDeHoy() {
        int n = 0;
        for (Venta v : ServicioVentas.listarVentas()) {
            if (v.getDia().equals(LocalDate.now())) {
                n++;
            }
        }
        return n;
    }

    private void mostrarNotificaciones() {
        double total = ServicioVentas.totalDe(ServicioVentas.filtrar(LocalDate.now(), LocalDate.now(), null));
        JOptionPane.showMessageDialog(this, "Ventas de hoy: " + ventasDeHoy() + "\nTotal vendido hoy: " + Moneda.formato(total),
                "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
    }

    private void menuSesion(JButton origen) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem cambiar = new JMenuItem("Cambiar sesión");
        cambiar.addActionListener(e -> {
            ServicioSesion.cerrarSesion();
            new FormLogin().setVisible(true);
            dispose();
        });
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> System.exit(0));
        m.add(cambiar);
        m.add(salir);
        m.show(origen, origen.getWidth() - 140, origen.getHeight());
    }

    // ---------- eventos del diseñador ----------
    private void btnInicioActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("INICIO");
    }

    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("CLIENTES");
    }

    private void btnProductosActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("PRODUCTOS");
    }

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("VENTAS");
    }

    private void btnBoletasActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("BOLETAS");
    }

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("USUARIOS");
    }

    private void btnVendidoActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("VENDIDO");
    }

    private void btnReportesActionPerformed(java.awt.event.ActionEvent evt) {
        mostrar("REPORTES");
    }

    private void btnMenuToggleActionPerformed(java.awt.event.ActionEvent evt) {
        pnlMenu.setVisible(!pnlMenu.isVisible());
        ajustarDerecha();
    }

    private void btnCampanaActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarNotificaciones();
    }

    private void btnFlechaActionPerformed(java.awt.event.ActionEvent evt) {
        menuSesion(btnFlecha);
    }

    // =============== dibujo propio (no aparece en el diseñador) ===============

    /** Boton del menu lateral: el seleccionado se pinta azul con una muesca a la derecha */
    private static class MenuUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean seleccionado = Boolean.TRUE.equals(c.getClientProperty("seleccionado"));
            if (seleccionado) {
                g2.setColor(Estilo.MENU_SEL);
                g2.fillRoundRect(12, 0, c.getWidth() - 12, c.getHeight(), 8, 8);
                Path2D.Double muesca = new Path2D.Double();      // triangulo que apunta al contenido
                muesca.moveTo(c.getWidth(), c.getHeight() / 2.0 - 7);
                muesca.lineTo(c.getWidth() - 8, c.getHeight() / 2.0);
                muesca.lineTo(c.getWidth(), c.getHeight() / 2.0 + 7);
                muesca.closePath();
                g2.setColor(Estilo.FONDO);
                g2.fill(muesca);
            } else if (((AbstractButton) c).getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(12, 0, c.getWidth() - 12, c.getHeight(), 8, 8);
            }
            g2.dispose();
            super.paint(g, c);
        }
    }

    /** Campana con un circulo naranja que muestra cuantas ventas hubo hoy */
    private static class CampanaUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            Object valor = c.getClientProperty("cantidad");
            int cantidad = (valor instanceof Integer) ? (Integer) valor : 0;
            if (cantidad > 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 150, 30));
                g2.fillOval(c.getWidth() - 17, 2, 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                String t = cantidad > 9 ? "9+" : String.valueOf(cantidad);
                g2.drawString(t, c.getWidth() - 10 - g2.getFontMetrics().stringWidth(t) / 2, 12);
                g2.dispose();
            }
        }
    }

    /** Circulo con la silueta del usuario */
    private static class Avatar implements Icon {
        @Override
        public int getIconWidth() { return 38; }
        @Override
        public int getIconHeight() { return 38; }

        @Override
        public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(225, 232, 245));
            g2.fillOval(x, y, 38, 38);
            g2.dispose();
            Iconos.de("usuarios", Estilo.MENU_SEL, 22).paintIcon(c, g, x + 8, y + 8);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlRaiz = new javax.swing.JPanel();
        pnlMenu = new javax.swing.JPanel();
        pnlLogo = new javax.swing.JPanel();
        btnInicio = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnBoletas = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        btnVendido = new javax.swing.JButton();
        btnReportes = new javax.swing.JButton();
        pnlUsuario = new javax.swing.JPanel();
        lblAvatar = new javax.swing.JLabel();
        lblRolUsuario = new javax.swing.JLabel();
        lblNombreUsuario = new javax.swing.JLabel();
        pnlDerecha = new javax.swing.JPanel();
        pnlBarra = new javax.swing.JPanel();
        btnMenuToggle = new javax.swing.JButton();
        btnCampana = new javax.swing.JButton();
        btnFlecha = new javax.swing.JButton();
        pnlContenido = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Family Market");
        setResizable(false);

        pnlRaiz.setBackground(new java.awt.Color(236, 240, 247));
        pnlRaiz.setPreferredSize(new java.awt.Dimension(1240, 700));
        pnlRaiz.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlMenu.setBackground(new java.awt.Color(12, 47, 100));
        pnlMenu.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlLogo.setBackground(new java.awt.Color(12, 47, 100));
        pnlLogo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlMenu.add(pnlLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 235, 60));

        btnInicio.setForeground(new java.awt.Color(255, 255, 255));
        btnInicio.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnInicio.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInicio.setText("Inicio");
        btnInicio.setOpaque(false);
        btnInicio.setContentAreaFilled(false);
        btnInicio.setBorderPainted(false);
        btnInicio.setFocusPainted(false);
        btnInicio.addActionListener(this::btnInicioActionPerformed);
        pnlMenu.add(btnInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 88, 235, 44));

        btnClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnClientes.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnClientes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnClientes.setText("Clientes");
        btnClientes.setOpaque(false);
        btnClientes.setContentAreaFilled(false);
        btnClientes.setBorderPainted(false);
        btnClientes.setFocusPainted(false);
        btnClientes.addActionListener(this::btnClientesActionPerformed);
        pnlMenu.add(btnClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 134, 235, 44));

        btnProductos.setForeground(new java.awt.Color(255, 255, 255));
        btnProductos.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnProductos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnProductos.setText("Productos");
        btnProductos.setOpaque(false);
        btnProductos.setContentAreaFilled(false);
        btnProductos.setBorderPainted(false);
        btnProductos.setFocusPainted(false);
        btnProductos.addActionListener(this::btnProductosActionPerformed);
        pnlMenu.add(btnProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, 235, 44));

        btnVentas.setForeground(new java.awt.Color(255, 255, 255));
        btnVentas.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnVentas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVentas.setText("Ventas");
        btnVentas.setOpaque(false);
        btnVentas.setContentAreaFilled(false);
        btnVentas.setBorderPainted(false);
        btnVentas.setFocusPainted(false);
        btnVentas.addActionListener(this::btnVentasActionPerformed);
        pnlMenu.add(btnVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 226, 235, 44));

        btnBoletas.setForeground(new java.awt.Color(255, 255, 255));
        btnBoletas.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnBoletas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBoletas.setText("Boletas");
        btnBoletas.setOpaque(false);
        btnBoletas.setContentAreaFilled(false);
        btnBoletas.setBorderPainted(false);
        btnBoletas.setFocusPainted(false);
        btnBoletas.addActionListener(this::btnBoletasActionPerformed);
        pnlMenu.add(btnBoletas, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 272, 235, 44));

        btnUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        btnUsuarios.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnUsuarios.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnUsuarios.setText("Usuarios");
        btnUsuarios.setOpaque(false);
        btnUsuarios.setContentAreaFilled(false);
        btnUsuarios.setBorderPainted(false);
        btnUsuarios.setFocusPainted(false);
        btnUsuarios.addActionListener(this::btnUsuariosActionPerformed);
        pnlMenu.add(btnUsuarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 318, 235, 44));

        btnVendido.setForeground(new java.awt.Color(255, 255, 255));
        btnVendido.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnVendido.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVendido.setText("Producto Vendido");
        btnVendido.setOpaque(false);
        btnVendido.setContentAreaFilled(false);
        btnVendido.setBorderPainted(false);
        btnVendido.setFocusPainted(false);
        btnVendido.addActionListener(this::btnVendidoActionPerformed);
        pnlMenu.add(btnVendido, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 364, 235, 44));

        btnReportes.setForeground(new java.awt.Color(255, 255, 255));
        btnReportes.setFont(new java.awt.Font("SansSerif", 0, 14));
        btnReportes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnReportes.setText("Reportes");
        btnReportes.setOpaque(false);
        btnReportes.setContentAreaFilled(false);
        btnReportes.setBorderPainted(false);
        btnReportes.setFocusPainted(false);
        btnReportes.addActionListener(this::btnReportesActionPerformed);
        pnlMenu.add(btnReportes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 410, 235, 44));

        pnlUsuario.setOpaque(false);
        pnlUsuario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblAvatar.setForeground(new java.awt.Color(28, 40, 60));
        lblAvatar.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblAvatar.setText(" ");
        pnlUsuario.add(lblAvatar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 9, 38, 38));

        lblRolUsuario.setForeground(new java.awt.Color(255, 255, 255));
        lblRolUsuario.setFont(new java.awt.Font("SansSerif", 1, 12));
        lblRolUsuario.setText("Rol");
        pnlUsuario.add(lblRolUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 10, 160, 18));

        lblNombreUsuario.setForeground(new java.awt.Color(160, 185, 220));
        lblNombreUsuario.setFont(new java.awt.Font("SansSerif", 0, 11));
        lblNombreUsuario.setText("usuario");
        pnlUsuario.add(lblNombreUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 28, 160, 16));
        pnlMenu.add(pnlUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 235, 56));
        pnlRaiz.add(pnlMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 235, 700));

        pnlDerecha.setBackground(new java.awt.Color(236, 240, 247));
        pnlDerecha.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlBarra.setBackground(new java.awt.Color(255, 255, 255));
        pnlBarra.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(208, 217, 230)));
        pnlBarra.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        btnMenuToggle.setContentAreaFilled(false);
        btnMenuToggle.setBorderPainted(false);
        btnMenuToggle.setToolTipText("Mostrar / ocultar el menú");
        btnMenuToggle.setFocusPainted(false);
        btnMenuToggle.addActionListener(this::btnMenuToggleActionPerformed);
        pnlBarra.add(btnMenuToggle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 9, 34, 32));

        btnCampana.setContentAreaFilled(false);
        btnCampana.setBorderPainted(false);
        btnCampana.setToolTipText("Ventas de hoy");
        btnCampana.setFocusPainted(false);
        btnCampana.addActionListener(this::btnCampanaActionPerformed);
        pnlBarra.add(btnCampana, new org.netbeans.lib.awtextra.AbsoluteConstraints(925, 9, 36, 32));

        btnFlecha.setContentAreaFilled(false);
        btnFlecha.setBorderPainted(false);
        btnFlecha.setFocusPainted(false);
        btnFlecha.addActionListener(this::btnFlechaActionPerformed);
        pnlBarra.add(btnFlecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(967, 9, 34, 32));
        pnlDerecha.add(pnlBarra, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1005, 50));

        pnlContenido.setBackground(new java.awt.Color(236, 240, 247));
        pnlContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlDerecha.add(pnlContenido, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1005, 650));
        pnlRaiz.add(pnlDerecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(235, 0, 1005, 700));

        getContentPane().add(pnlRaiz, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBoletas;
    private javax.swing.JButton btnCampana;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnFlecha;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnMenuToggle;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnReportes;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVendido;
    private javax.swing.JButton btnVentas;
    private javax.swing.JLabel lblAvatar;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblRolUsuario;
    private javax.swing.JPanel pnlBarra;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlDerecha;
    private javax.swing.JPanel pnlLogo;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JPanel pnlRaiz;
    private javax.swing.JPanel pnlUsuario;
    // End of variables declaration//GEN-END:variables
}
