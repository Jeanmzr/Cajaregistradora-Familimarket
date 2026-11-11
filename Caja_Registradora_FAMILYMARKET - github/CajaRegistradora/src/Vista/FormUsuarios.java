package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Usuario;
import Servicio.ServicioUsuarios;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/** Registro de usuarios y edicion de las cuentas guardadas (nombre, usuario y rol). Solo lo ve el administrador. */
public class FormUsuarios extends javax.swing.JPanel implements Modulo {
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"Nombre", "Usuario", "Rol"});
    private Usuario editando;     // cuenta que se esta editando (null = se esta registrando una nueva)

    public FormUsuarios() {
        initComponents();
        //las listas de roles salen del servicio, asi siempre coinciden con los roles reales
        cbxRol.setModel(new DefaultComboBoxModel<>(ServicioUsuarios.ROLES));
        cbxFiltroRol.removeAllItems();
        cbxFiltroRol.addItem("TODOS");
        for (String r : ServicioUsuarios.ROLES) {
            cbxFiltroRol.addItem(r);
        }
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);

        Estilo.alCambiar(txtBuscar, this::cargarTabla);
        actualizar();
    }

    private void registrar() {
        if (editando != null) {
            guardarEdicion();
            return;
        }
        try {
            Usuario u = ServicioUsuarios.registrar(cbxRol.getSelectedItem().toString(), txtUsuario.getText(),
                    new String(txtClave.getPassword()), txtNombre.getText());
            JOptionPane.showMessageDialog(this, "Usuario '" + u.getUsuario() + "' registrado como " + u.getTipo() + ".");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ---------- editar: se cargan los datos de la cuenta elegida y se guardan los cambios ----------
    private void iniciarEdicion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione un usuario de la tabla para editarlo.");
            return;
        }
        Usuario u = ServicioUsuarios.buscar(modelo.getValueAt(fila, 1).toString());
        if (u == null) {
            return;
        }
        editando = u;
        txtNombre.setText(u.getNombre());
        txtUsuario.setText(u.getUsuario());
        txtClave.setText("");
        cbxRol.setSelectedItem(u.getTipo());
        ponerModoEdicion();
        txtNombre.requestFocus();
    }

    private void guardarEdicion() {
        try {
            String anterior = editando.getUsuario();
            ServicioUsuarios.editar(anterior, cbxRol.getSelectedItem().toString(), txtUsuario.getText(), txtNombre.getText());
            JOptionPane.showMessageDialog(this, "Cuenta '" + anterior + "' actualizada.");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo editar", JOptionPane.WARNING_MESSAGE);
        }
    }

    // el mismo formulario sirve para registrar y para editar: cambian los textos
    private void ponerModoEdicion() {
        boolean activo = editando != null;
        ((TitledBorder) pnlNuevo.getBorder()).setTitle(activo ? "EDITAR USUARIO '" + editando.getUsuario() + "'" : "NUEVO USUARIO");
        btnRegistrar.setText(activo ? "GUARDAR CAMBIOS" : "REGISTRAR");
        btnLimpiar.setText(activo ? "CANCELAR" : "LIMPIAR");
        txtClave.setEnabled(!activo);      // la contraseña no se cambia desde aqui
        pnlNuevo.repaint();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione un usuario de la tabla.");
            return;
        }
        String usuario = modelo.getValueAt(fila, 1).toString();
        if (!Estilo.confirmar(this, "¿Eliminar al usuario '" + usuario + "'?")) {
            return;
        }
        try {
            ServicioUsuarios.eliminar(usuario);
            cargarTabla();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    // limpia el formulario y cancela la edicion si la habia
    private void limpiar() {
        editando = null;
        txtNombre.setText("");
        txtUsuario.setText("");
        txtClave.setText("");
        cbxRol.setSelectedIndex(0);
        ponerModoEdicion();
        txtNombre.requestFocus();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Usuario u : ServicioUsuarios.filtrar(cbxFiltroRol.getSelectedItem().toString(), txtBuscar.getText())) {
            modelo.addRow(new Object[]{u.getNombre(), u.getUsuario(), u.getTipo()});
        }
        lblRegistros.setText("Cantidad de registros: " + modelo.getRowCount());
    }

    @Override
    public void actualizar() {
        cargarTabla();
    }

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {
        registrar();
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        limpiar();
    }

    private void cbxFiltroRolActionPerformed(java.awt.event.ActionEvent evt) {
        if (cbxFiltroRol.getSelectedItem() != null) {
            cargarTabla();
        }
    }

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        eliminar();
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        iniciarEdicion();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        pnlNuevo = new javax.swing.JPanel();
        txtNombre = new javax.swing.JTextField();
        txtUsuario = new javax.swing.JTextField();
        txtClave = new javax.swing.JPasswordField();
        btnRegistrar = new javax.swing.JButton();
        cbxRol = new javax.swing.JComboBox();
        lblAyuda = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();
        txtBuscar = new javax.swing.JTextField();
        cbxFiltroRol = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        btnEliminar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        lblRegistros = new javax.swing.JLabel();

        setBackground(new java.awt.Color(236, 240, 247));
        setPreferredSize(new java.awt.Dimension(1005, 650));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setText("USUARIOS");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        pnlNuevo.setBackground(new java.awt.Color(255, 255, 255));
        pnlNuevo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NUEVO USUARIO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        txtNombre.setBackground(new java.awt.Color(255, 255, 255));
        txtNombre.setForeground(new java.awt.Color(28, 40, 60));
        txtNombre.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtNombre.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nombre completo", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 28, 300, 56));

        txtUsuario.setBackground(new java.awt.Color(255, 255, 255));
        txtUsuario.setForeground(new java.awt.Color(28, 40, 60));
        txtUsuario.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Usuario", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(326, 28, 180, 56));

        txtClave.setBackground(new java.awt.Color(255, 255, 255));
        txtClave.setForeground(new java.awt.Color(28, 40, 60));
        txtClave.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtClave.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contraseña (mín. 4 caracteres)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(txtClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(516, 28, 245, 56));

        btnRegistrar.setBackground(new java.awt.Color(35, 113, 237));
        btnRegistrar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnRegistrar.setText("REGISTRAR");
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(this::btnRegistrarActionPerformed);
        pnlNuevo.add(btnRegistrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(771, 34, 170, 44));

        cbxRol.setBackground(new java.awt.Color(255, 255, 255));
        cbxRol.setForeground(new java.awt.Color(28, 40, 60));
        cbxRol.setFont(new java.awt.Font("SansSerif", 0, 14));
        cbxRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TRABAJADOR", "ENCARGADO", "ADMINISTRADOR" }));
        cbxRol.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Rol", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(cbxRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 90, 300, 56));

        lblAyuda.setForeground(new java.awt.Color(110, 125, 148));
        lblAyuda.setFont(new java.awt.Font("SansSerif", 0, 11));
        lblAyuda.setText("Trabajador: vende | Encargado: reportes y productos | Administrador: todo");
        pnlNuevo.add(lblAyuda, new org.netbeans.lib.awtextra.AbsoluteConstraints(326, 104, 435, 30));

        btnLimpiar.setBackground(new java.awt.Color(13, 27, 47));
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        pnlNuevo.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(771, 96, 170, 44));
        add(pnlNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 64, 957, 156));

        txtBuscar.setBackground(new java.awt.Color(255, 255, 255));
        txtBuscar.setForeground(new java.awt.Color(28, 40, 60));
        txtBuscar.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtBuscar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Buscar por nombre o usuario", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 232, 430, 56));

        cbxFiltroRol.setBackground(new java.awt.Color(255, 255, 255));
        cbxFiltroRol.setForeground(new java.awt.Color(28, 40, 60));
        cbxFiltroRol.setFont(new java.awt.Font("SansSerif", 0, 14));
        cbxFiltroRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "TRABAJADOR", "ENCARGADO", "ADMINISTRADOR" }));
        cbxFiltroRol.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtrar por rol", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        cbxFiltroRol.addActionListener(this::cbxFiltroRolActionPerformed);
        add(cbxFiltroRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(464, 232, 210, 56));

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 298, 957, 284));

        btnEliminar.setBackground(new java.awt.Color(200, 40, 40));
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnEliminar.setText("ELIMINAR");
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 596, 170, 42));

        btnEditar.setBackground(new java.awt.Color(31, 55, 85));
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnEditar.setText("EDITAR");
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(204, 596, 170, 42));

        lblRegistros.setForeground(new java.awt.Color(28, 40, 60));
        lblRegistros.setFont(new java.awt.Font("SansSerif", 1, 13));
        lblRegistros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRegistros.setText("Cantidad de registros: 0");
        add(lblRegistros, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 606, 381, 24));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JComboBox<String> cbxFiltroRol;
    private javax.swing.JComboBox<String> cbxRol;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblRegistros;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlNuevo;
    private javax.swing.JTable tabla;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JPasswordField txtClave;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
