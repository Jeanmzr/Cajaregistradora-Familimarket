package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Moneda;
import Modelo.Producto;
import Modelo.Usuario;
import Servicio.ServicioProductos;
import Servicio.ServicioSesion;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Productos con precio y stock (cada producto tiene un limite de stock y la venta lo va bajando).
 * El encargado y el administrador registran, editan y eliminan; el trabajador solo consulta.
 */
public class FormProductos extends javax.swing.JPanel implements Modulo {
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"Código", "Producto", "Precio", "Stock"});
    private Producto editando;     // producto que se esta editando (null = se esta registrando uno nuevo)

    public FormProductos() {
        initComponents();
        spnStock.setModel(new SpinnerNumberModel(0, 0, Producto.STOCK_MAXIMO, 1));
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);

        Usuario sesion = ServicioSesion.getUsuarioActual();
        boolean puedeEditar = sesion != null && sesion.puedeGestionarProductos();
        if (!puedeEditar) {      // el trabajador solo consulta
            txtNombre.setEnabled(false);
            txtPrecio.setEnabled(false);
            spnStock.setEnabled(false);
            btnRegistrar.setEnabled(false);
            btnLimpiar.setEnabled(false);
            btnEditar.setToolTipText("Tu rol solo permite consultar productos");
            btnEliminar.setToolTipText("Tu rol solo permite consultar productos");
        }
        btnEliminar.setEnabled(puedeEditar);
        btnEditar.setEnabled(puedeEditar);

        Estilo.alCambiar(txtBuscar, this::cargarTabla);
        actualizar();
    }

    private void registrar() {
        if (editando != null) {
            guardarEdicion();
            return;
        }
        try {
            Producto p = ServicioProductos.registrar(txtNombre.getText(), txtPrecio.getText(),
                    Integer.parseInt(spnStock.getValue().toString()));
            JOptionPane.showMessageDialog(this, "Producto registrado con el código " + p.getCodigo() + ".");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ---------- editar: se cargan los datos del producto elegido y se guardan los cambios ----------
    private void iniciarEdicion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione un producto de la tabla para editarlo.");
            return;
        }
        Producto p = ServicioProductos.buscar(modelo.getValueAt(fila, 0).toString());
        if (p == null) {
            return;
        }
        editando = p;
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        spnStock.setValue(p.getStock());
        ponerModoEdicion();
        txtNombre.requestFocus();
    }

    private void guardarEdicion() {
        try {
            String codigo = editando.getCodigo();
            ServicioProductos.editar(codigo, txtNombre.getText(), txtPrecio.getText(),
                    Integer.parseInt(spnStock.getValue().toString()));
            JOptionPane.showMessageDialog(this, "Producto " + codigo + " actualizado.");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    // el mismo formulario sirve para registrar y para editar: cambian los textos
    private void ponerModoEdicion() {
        boolean activo = editando != null;
        ((TitledBorder) pnlNuevo.getBorder()).setTitle(activo ? "EDITAR PRODUCTO " + editando.getCodigo() : "NUEVO PRODUCTO");
        btnRegistrar.setText(activo ? "GUARDAR CAMBIOS" : "REGISTRAR");
        btnLimpiar.setText(activo ? "CANCELAR" : "LIMPIAR");
        lblCodigo.setText((activo ? "Editando " + editando.getCodigo() : "Código: " + ServicioProductos.siguienteCodigo()
                + " (lo asigna el sistema)") + "   |   Límite de stock: " + Producto.STOCK_MAXIMO + " unidades");
        pnlNuevo.repaint();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione un producto de la tabla.");
            return;
        }
        String codigo = modelo.getValueAt(fila, 0).toString();
        if (!Estilo.confirmar(this, "¿Eliminar el producto " + codigo + " - " + modelo.getValueAt(fila, 1) + "?")) {
            return;
        }
        try {
            ServicioProductos.eliminar(codigo);
            actualizar();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    // limpia el formulario y cancela la edicion si la habia
    private void limpiar() {
        editando = null;
        txtNombre.setText("");
        txtPrecio.setText("");
        spnStock.setValue(0);
        ponerModoEdicion();
        txtNombre.requestFocus();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Producto p : ServicioProductos.filtrar(txtBuscar.getText())) {
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), Moneda.formato(p.getPrecio()), p.getStock()});
        }
        lblRegistros.setText("Cantidad de registros: " + modelo.getRowCount());
    }

    @Override
    public void actualizar() {
        ponerModoEdicion();
        cargarTabla();
    }

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {
        registrar();
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        limpiar();
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
        txtPrecio = new javax.swing.JTextField();
        spnStock = new javax.swing.JSpinner();
        btnRegistrar = new javax.swing.JButton();
        lblCodigo = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();
        txtBuscar = new javax.swing.JTextField();
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
        lblTitulo.setText("PRODUCTOS");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        pnlNuevo.setBackground(new java.awt.Color(255, 255, 255));
        pnlNuevo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NUEVO PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        txtNombre.setBackground(new java.awt.Color(255, 255, 255));
        txtNombre.setForeground(new java.awt.Color(28, 40, 60));
        txtNombre.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtNombre.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nombre del producto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 28, 400, 56));

        txtPrecio.setBackground(new java.awt.Color(255, 255, 255));
        txtPrecio.setForeground(new java.awt.Color(28, 40, 60));
        txtPrecio.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtPrecio.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Precio (S/)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(txtPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(426, 28, 160, 56));

        spnStock.setBackground(new java.awt.Color(255, 255, 255));
        spnStock.setForeground(new java.awt.Color(28, 40, 60));
        spnStock.setFont(new java.awt.Font("SansSerif", 0, 14));
        spnStock.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stock", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlNuevo.add(spnStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(596, 28, 150, 56));

        btnRegistrar.setBackground(new java.awt.Color(35, 113, 237));
        btnRegistrar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnRegistrar.setText("REGISTRAR");
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(this::btnRegistrarActionPerformed);
        pnlNuevo.add(btnRegistrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(771, 34, 170, 44));

        lblCodigo.setForeground(new java.awt.Color(30, 90, 180));
        lblCodigo.setFont(new java.awt.Font("SansSerif", 1, 13));
        lblCodigo.setText(" ");
        pnlNuevo.add(lblCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 104, 730, 30));

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
        txtBuscar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Buscar por código o nombre", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 232, 430, 56));

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblRegistros;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlNuevo;
    private javax.swing.JSpinner spnStock;
    private javax.swing.JTable tabla;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
