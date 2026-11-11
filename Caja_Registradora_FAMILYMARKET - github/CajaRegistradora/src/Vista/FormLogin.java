package Vista;

import Componentes.Logo;
import Modelo.Usuario;
import Servicio.ServicioSesion;
import Servicio.ServicioUsuarios;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Inicio de sesion: solo usuario y contraseña (el rol lo tiene cada usuario registrado) */
public class FormLogin extends javax.swing.JFrame {

    public FormLogin() {
        initComponents();
        setLocationRelativeTo(null);
        // el logo se dibuja en codigo dentro de la franja de la izquierda
        pnlLogo.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlLogo.add(new Logo());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        Usuario u = ServicioSesion.iniciarSesion(txtUsuario.getText(), new String(txtClave.getPassword()));
        if (u == null) {
            lblError.setText("Usuario o contraseña incorrectos");
            txtClave.setText("");
            txtClave.requestFocus();
            return;
        }
        new FormPrincipal().setVisible(true);
        dispose();
    }

    // una cuenta nueva siempre nace como TRABAJADOR; solo el administrador crea otros roles
    private void crearCuenta() {
        JTextField nombre = new JTextField();
        JTextField usuario = new JTextField();
        JPasswordField clave = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Nombre completo:"));
        p.add(nombre);
        p.add(new JLabel("Usuario:"));
        p.add(usuario);
        p.add(new JLabel("Contraseña (mínimo 4 caracteres):"));
        p.add(clave);
        while (JOptionPane.showConfirmDialog(this, p, "Crear una cuenta nueva",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                ServicioUsuarios.registrar("TRABAJADOR", usuario.getText(), new String(clave.getPassword()), nombre.getText());
                txtUsuario.setText(usuario.getText().trim());
                txtClave.setText("");
                lblError.setText(" ");
                JOptionPane.showMessageDialog(this, "Cuenta creada como TRABAJADOR.\nYa puedes iniciar sesión.");
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {
        ingresar();
    }

    private void btnCuentaActionPerformed(java.awt.event.ActionEvent evt) {
        crearCuenta();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlRaiz = new javax.swing.JPanel();
        pnlFranja = new javax.swing.JPanel();
        pnlLogo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSub = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        txtClave = new javax.swing.JPasswordField();
        lblError = new javax.swing.JLabel();
        btnIngresar = new javax.swing.JButton();
        btnCuenta = new javax.swing.JButton();
        lblAyuda = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Family Market - Iniciar sesión");
        setResizable(false);

        pnlRaiz.setBackground(new java.awt.Color(228, 232, 242));
        pnlRaiz.setPreferredSize(new java.awt.Dimension(1100, 680));
        pnlRaiz.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlFranja.setBackground(new java.awt.Color(240, 242, 247));
        pnlFranja.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlLogo.setBackground(new java.awt.Color(12, 47, 100));
        pnlLogo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlFranja.add(pnlLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 60));
        pnlRaiz.add(pnlFranja, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 60));

        pnlTarjeta.setBackground(new java.awt.Color(246, 247, 250));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(35, 113, 237)));
        pnlTarjeta.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTitulo.setForeground(new java.awt.Color(50, 100, 200));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 22));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Iniciar sesión");
        pnlTarjeta.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 24, 400, 32));

        lblSub.setForeground(new java.awt.Color(110, 125, 148));
        lblSub.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSub.setText("Entra con tu usuario y contraseña");
        pnlTarjeta.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 400, 18));

        txtUsuario.setBackground(new java.awt.Color(255, 255, 255));
        txtUsuario.setForeground(new java.awt.Color(28, 40, 60));
        txtUsuario.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Usuario", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlTarjeta.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(28, 100, 344, 58));

        txtClave.setBackground(new java.awt.Color(255, 255, 255));
        txtClave.setForeground(new java.awt.Color(28, 40, 60));
        txtClave.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtClave.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contraseña", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlTarjeta.add(txtClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(28, 168, 344, 58));

        lblError.setForeground(new java.awt.Color(200, 40, 40));
        lblError.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblError.setText(" ");
        pnlTarjeta.add(lblError, new org.netbeans.lib.awtextra.AbsoluteConstraints(28, 234, 344, 20));

        btnIngresar.setBackground(new java.awt.Color(35, 113, 237));
        btnIngresar.setForeground(new java.awt.Color(255, 255, 255));
        btnIngresar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnIngresar.setText("INGRESAR");
        btnIngresar.setFocusPainted(false);
        btnIngresar.addActionListener(this::btnIngresarActionPerformed);
        pnlTarjeta.add(btnIngresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 264, 170, 42));

        btnCuenta.setBackground(new java.awt.Color(255, 255, 255));
        btnCuenta.setForeground(new java.awt.Color(50, 100, 200));
        btnCuenta.setFont(new java.awt.Font("SansSerif", 1, 12));
        btnCuenta.setText("Crear una cuenta nueva");
        btnCuenta.setFocusPainted(false);
        btnCuenta.addActionListener(this::btnCuentaActionPerformed);
        pnlTarjeta.add(btnCuenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 320, 300, 34));

        lblAyuda.setForeground(new java.awt.Color(110, 125, 148));
        lblAyuda.setFont(new java.awt.Font("SansSerif", 0, 11));
        lblAyuda.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAyuda.setText("Prueba: admin / admin123");
        pnlTarjeta.add(lblAyuda, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 404, 400, 18));
        pnlRaiz.add(pnlTarjeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 120, 400, 440));

        getContentPane().add(pnlRaiz, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCuenta;
    private javax.swing.JButton btnIngresar;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFranja;
    private javax.swing.JPanel pnlLogo;
    private javax.swing.JPanel pnlRaiz;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JPasswordField txtClave;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
