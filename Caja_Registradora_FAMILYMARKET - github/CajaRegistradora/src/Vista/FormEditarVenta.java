package Vista;

import Componentes.Estilo;
import Modelo.Cliente;
import Modelo.ItemVenta;
import Modelo.Moneda;
import Modelo.Producto;
import Modelo.Venta;
import Servicio.ServicioClientes;
import Servicio.ServicioProductos;
import Servicio.ServicioVentas;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Corrige una venta ya guardada: cambia el cliente y los productos (con sus unidades).
 * Al guardar, el stock se ajusta solo (las unidades viejas vuelven y las nuevas se descuentan).
 */
public class FormEditarVenta extends javax.swing.JFrame {
    private final Venta venta;
    private final Consumer<Venta> alGuardar;                 // lo que hace la ventana que abrio esta (actualizar, boleta...)
    private final List<ItemVenta> items = new ArrayList<>(); // copia de los productos de la venta
    private boolean cargando = false;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Cód.", "Producto", "Cant.", "P. unit.", "Importe", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 2;      // solo la cantidad se edita
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return (columna == 2 || columna == 5) ? Integer.class : String.class;
        }
    };

    public FormEditarVenta() {
        this(null, null);
    }

    public FormEditarVenta(Venta venta, Consumer<Venta> alGuardar) {
        this.venta = venta;
        this.alGuardar = alGuardar;
        initComponents();
        setLocationRelativeTo(null);
        spnCantidad.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);
        int[] ancho = {45, 170, 50, 80, 85, 60};
        for (int columna = 0; columna < ancho.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(ancho[columna]);
        }
        modelo.addTableModelListener(e -> {
            if (!cargando && e.getColumn() == 2 && e.getFirstRow() >= 0) {
                final int fila = e.getFirstRow();
                SwingUtilities.invokeLater(() -> cambiarCantidad(fila));
            }
        });
        Estilo.alCambiar(txtCodCliente, this::previsualizar);
        Estilo.alCambiar(txtCodProducto, this::previsualizar);
        if (venta != null) {
            lblTitulo.setText("EDITAR VENTA " + venta.getCodigo());
            txtCodCliente.setText(venta.getCliente().getCodigo());
            for (ItemVenta it : venta.getItems()) {
                items.add(new ItemVenta(it));
            }
        }
        refrescar();
    }

    // unidades que se pueden poner de un producto: su stock mas lo que esta venta ya tiene
    private int disponible(Producto p) {
        return p.getStock() + ServicioVentas.unidadesEnVenta(venta, p);
    }

    private void cambiarCantidad(int fila) {
        if (fila >= items.size()) {
            return;
        }
        ItemVenta it = items.get(fila);
        try {
            int cant = Integer.parseInt(String.valueOf(modelo.getValueAt(fila, 2)).trim());
            if (cant < 1 || cant > 1000) {
                throw new IllegalArgumentException("La cantidad debe estar entre 1 y 1000. Para quitar el producto usa QUITAR PRODUCTO.");
            }
            int disp = disponible(it.getProducto());
            if (cant > disp) {
                throw new IllegalArgumentException("Stock insuficiente de " + it.getProducto().getNombre() + ": disponible " + disp + ".");
            }
            it.setCantidad(cant);
        } catch (NumberFormatException ex) {
            Estilo.aviso(this, "La cantidad debe ser un número entero.");
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
        refrescar();      // vuelve a dibujar con la cantidad valida
    }

    private void agregar() {
        Producto p = ServicioProductos.buscar(txtCodProducto.getText());
        if (p == null) {
            Estilo.aviso(this, txtCodProducto.getText().trim().isEmpty()
                    ? "Ingrese el código del producto." : "No existe un producto con ese código.");
            return;
        }
        try {
            int cant = Integer.parseInt(spnCantidad.getValue().toString());
            ItemVenta existente = null;
            for (ItemVenta it : items) {
                if (it.getProducto() == p) {
                    existente = it;
                }
            }
            int yaTiene = (existente == null) ? 0 : existente.getCantidad();
            if (yaTiene + cant > disponible(p)) {
                throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre() + ": disponible " + disponible(p)
                        + (yaTiene > 0 ? " (la boleta ya tiene " + yaTiene + ")" : "") + ".");
            }
            if (existente != null) {
                existente.sumar(cant);
            } else {
                if (items.size() >= Venta.MAX_PRODUCTOS) {
                    throw new IllegalArgumentException("Una boleta puede tener máximo " + Venta.MAX_PRODUCTOS + " productos.");
                }
                items.add(new ItemVenta(p, cant));
            }
            txtCodProducto.setText("");
            spnCantidad.setValue(1);
            refrescar();
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione un producto de la tabla para quitarlo.");
            return;
        }
        items.remove(fila);
        refrescar();
    }

    // vuelve a llenar la tabla y los totales
    private void refrescar() {
        cargando = true;
        modelo.setRowCount(0);
        for (ItemVenta it : items) {
            modelo.addRow(new Object[]{it.getProducto().getCodigo(), it.getProducto().getNombre(), it.getCantidad(),
                Moneda.formato(it.getPrecioUnitario()), Moneda.formato(it.subtotal()), disponible(it.getProducto())});
        }
        cargando = false;
        previsualizar();
    }

    private void previsualizar() {
        Cliente c = ServicioClientes.buscar(txtCodCliente.getText());
        if (txtCodCliente.getText().trim().isEmpty()) {
            lblCliente.setForeground(Estilo.GRIS);
            lblCliente.setText("Escribe el código del cliente");
        } else if (c == null) {
            lblCliente.setForeground(Estilo.ROJO);
            lblCliente.setText("Cliente no encontrado");
        } else {
            lblCliente.setForeground(Estilo.TEXTO);
            lblCliente.setText(c.getNombre() + " (" + c.getEdad() + " años)" + (c.tieneDescuento() ? " - 15% dscto." : ""));
        }
        Producto p = ServicioProductos.buscar(txtCodProducto.getText());
        if (txtCodProducto.getText().trim().isEmpty()) {
            lblProducto.setForeground(Estilo.GRIS);
            lblProducto.setText(" ");
        } else if (p == null) {
            lblProducto.setForeground(Estilo.ROJO);
            lblProducto.setText("Producto no encontrado");
        } else {
            lblProducto.setForeground(Estilo.TEXTO);
            lblProducto.setText(p.getNombre() + " - " + Moneda.formato(p.getPrecio()) + " - disponible: " + disponible(p));
        }
        double sub = 0;
        for (ItemVenta it : items) {
            sub += it.subtotal();
        }
        sub = Moneda.redondear(sub);
        double desc = (c == null) ? 0 : Moneda.redondear(sub * c.getPorcentajeDescuento());
        lblSubtotal.setText("Subtotal: " + Moneda.formato(sub));
        lblDescuento.setText("Descuento: " + Moneda.formato(desc));
        lblCobrar.setText("TOTAL: " + Moneda.formato(Moneda.redondear(sub - desc)));
    }

    private void guardar() {
        if (venta == null) {
            dispose();
            return;
        }
        try {
            ServicioVentas.editarVenta(venta, txtCodCliente.getText(), items);
            dispose();
            if (alGuardar != null) {
                alGuardar.accept(venta);
            }
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    private void txtCodProductoActionPerformed(java.awt.event.ActionEvent evt) {
        agregar();      // Enter agrega el producto
    }

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {
        agregar();
    }

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {
        quitar();
    }

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {
        guardar();
    }

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlRaiz = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        pnlCliente = new javax.swing.JPanel();
        txtCodCliente = new javax.swing.JTextField();
        lblCliente = new javax.swing.JLabel();
        pnlProductos = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        txtCodProducto = new javax.swing.JTextField();
        spnCantidad = new javax.swing.JSpinner();
        btnAgregar = new javax.swing.JButton();
        lblProducto = new javax.swing.JLabel();
        btnQuitar = new javax.swing.JButton();
        lblAyuda = new javax.swing.JLabel();
        lblSubtotal = new javax.swing.JLabel();
        lblDescuento = new javax.swing.JLabel();
        lblCobrar = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Family Market - Editar venta");
        setResizable(false);

        pnlRaiz.setBackground(new java.awt.Color(236, 240, 247));
        pnlRaiz.setPreferredSize(new java.awt.Dimension(820, 640));
        pnlRaiz.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 24));
        lblTitulo.setText("EDITAR VENTA");
        pnlRaiz.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 772, 40));

        pnlCliente.setBackground(new java.awt.Color(255, 255, 255));
        pnlCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CLIENTE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlCliente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        txtCodCliente.setBackground(new java.awt.Color(255, 255, 255));
        txtCodCliente.setForeground(new java.awt.Color(28, 40, 60));
        txtCodCliente.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtCodCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código del cliente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlCliente.add(txtCodCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 26, 220, 56));

        lblCliente.setForeground(new java.awt.Color(28, 40, 60));
        lblCliente.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblCliente.setText(" ");
        pnlCliente.add(lblCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 40, 510, 30));
        pnlRaiz.add(pnlCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 60, 772, 92));

        pnlProductos.setBackground(new java.awt.Color(255, 255, 255));
        pnlProductos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "PRODUCTOS DE LA BOLETA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlProductos.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        pnlProductos.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 26, 520, 294));

        txtCodProducto.setBackground(new java.awt.Color(255, 255, 255));
        txtCodProducto.setForeground(new java.awt.Color(28, 40, 60));
        txtCodProducto.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtCodProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código del producto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        txtCodProducto.addActionListener(this::txtCodProductoActionPerformed);
        pnlProductos.add(txtCodProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(548, 26, 208, 56));

        spnCantidad.setBackground(new java.awt.Color(255, 255, 255));
        spnCantidad.setForeground(new java.awt.Color(28, 40, 60));
        spnCantidad.setFont(new java.awt.Font("SansSerif", 0, 14));
        spnCantidad.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cantidad", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlProductos.add(spnCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(548, 90, 84, 56));

        btnAgregar.setBackground(new java.awt.Color(31, 55, 85));
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnAgregar.setText("AGREGAR");
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        pnlProductos.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 96, 116, 44));

        lblProducto.setForeground(new java.awt.Color(110, 125, 148));
        lblProducto.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblProducto.setText(" ");
        pnlProductos.add(lblProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(548, 150, 208, 40));

        btnQuitar.setBackground(new java.awt.Color(31, 55, 85));
        btnQuitar.setForeground(new java.awt.Color(255, 255, 255));
        btnQuitar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnQuitar.setText("QUITAR PRODUCTO");
        btnQuitar.setFocusPainted(false);
        btnQuitar.addActionListener(this::btnQuitarActionPerformed);
        pnlProductos.add(btnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(548, 200, 208, 40));

        lblAyuda.setForeground(new java.awt.Color(110, 125, 148));
        lblAyuda.setFont(new java.awt.Font("SansSerif", 0, 11));
        lblAyuda.setText("<html>Para cambiar las unidades de un producto, haz doble clic en su columna Cant.</html>");
        pnlProductos.add(lblAyuda, new org.netbeans.lib.awtextra.AbsoluteConstraints(548, 250, 208, 60));
        pnlRaiz.add(pnlProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 160, 772, 336));

        lblSubtotal.setForeground(new java.awt.Color(28, 40, 60));
        lblSubtotal.setFont(new java.awt.Font("SansSerif", 0, 14));
        lblSubtotal.setText("Subtotal: S/ 0.00");
        pnlRaiz.add(lblSubtotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 506, 300, 22));

        lblDescuento.setForeground(new java.awt.Color(28, 40, 60));
        lblDescuento.setFont(new java.awt.Font("SansSerif", 0, 14));
        lblDescuento.setText("Descuento: S/ 0.00");
        pnlRaiz.add(lblDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 528, 300, 22));

        lblCobrar.setForeground(new java.awt.Color(35, 113, 237));
        lblCobrar.setFont(new java.awt.Font("SansSerif", 1, 22));
        lblCobrar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCobrar.setText("TOTAL: S/ 0.00");
        pnlRaiz.add(lblCobrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 514, 456, 36));

        btnGuardar.setBackground(new java.awt.Color(35, 113, 237));
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnGuardar.setText("GUARDAR CAMBIOS");
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        pnlRaiz.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 574, 230, 46));

        btnCancelar.setBackground(new java.awt.Color(13, 27, 47));
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnCancelar.setText("CANCELAR");
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        pnlRaiz.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(264, 574, 170, 46));

        getContentPane().add(pnlRaiz, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblCobrar;
    private javax.swing.JLabel lblDescuento;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JPanel pnlProductos;
    private javax.swing.JPanel pnlRaiz;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JTable tabla;
    private javax.swing.JTextField txtCodCliente;
    private javax.swing.JTextField txtCodProducto;
    // End of variables declaration//GEN-END:variables
}
