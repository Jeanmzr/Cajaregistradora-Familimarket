package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Cliente;
import Modelo.Empleado;
import Modelo.ItemVenta;
import Modelo.Moneda;
import Modelo.Producto;
import Modelo.Usuario;
import Modelo.Venta;
import Reporte.BoletaPDF;
import Servicio.ServicioClientes;
import Servicio.ServicioProductos;
import Servicio.ServicioSesion;
import Servicio.ServicioVentas;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Caja: una boleta puede llevar varios productos. Cada venta baja el stock; las ventas se pueden editar. */
public class FormVentas extends javax.swing.JPanel implements Modulo {
    private final List<ItemVenta> carrito = new ArrayList<>();
    private final DefaultTableModel modeloCarrito = Estilo.modelo(new String[]{"Cód.", "Producto", "Cant.", "P. unit.", "Importe", "Stock"});
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"N°", "Boleta", "Fecha", "Cód. cliente",
        "Cliente", "Productos", "Unid.", "Subtotal", "Descuento", "Total"});

    public FormVentas() {
        initComponents();
        spnCantidad.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
        Usuario sesion = ServicioSesion.getUsuarioActual();
        boolean puedeVender = sesion != null && sesion.puedeVender();

        // ----- carrito (la ultima columna muestra el stock que queda de cada producto) -----
        tablaCarrito.setModel(modeloCarrito);
        Estilo.tabla(tablaCarrito);
        Estilo.estilizar(jScrollPane2);
        int[] ancho = {45, 150, 40, 65, 70, 50};
        for (int columna = 0; columna < ancho.length; columna++) {
            tablaCarrito.getColumnModel().getColumn(columna).setPreferredWidth(ancho[columna]);
        }

        if (!puedeVender) {      // este rol solo consulta las ventas
            for (java.awt.Component c : new java.awt.Component[]{txtCodCliente, txtCodProducto, spnCantidad,
                btnAgregar, btnQuitar, btnRegistrar, btnLimpiar, btnEditar}) {
                c.setEnabled(false);
            }
            lblCliente.setForeground(Estilo.ROJO);
            lblCliente.setText("Tu rol no registra ventas, solo las consulta.");
        }

        // ----- historial de ventas -----
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);
        int[] anchos = {40, 100, 125, 108, 170, 212, 55, 90, 90, 90};
        for (int columna = 0; columna < anchos.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(anchos[columna]);
        }
        btnEliminar.setEnabled(sesion != null && sesion.puedeEliminar());

        Runnable vista = this::previsualizar;
        Estilo.alCambiar(txtCodCliente, vista);
        Estilo.alCambiar(txtCodProducto, vista);
        actualizar();
    }

    // ---------- carrito ----------
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
            for (ItemVenta it : carrito) {
                if (it.getProducto() == p) {
                    existente = it;
                }
            }
            // no se puede poner en el carrito mas de lo que hay en stock
            int enCarrito = (existente == null) ? 0 : existente.getCantidad();
            if (enCarrito + cant > p.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre() + ": disponible " + p.getStock()
                        + (enCarrito > 0 ? " (ya tienes " + enCarrito + " en el carrito)" : "") + ".");
            }
            if (existente != null) {
                existente.sumar(cant);
            } else {
                if (carrito.size() >= Venta.MAX_PRODUCTOS) {
                    throw new IllegalArgumentException("Una boleta puede tener máximo " + Venta.MAX_PRODUCTOS + " productos.");
                }
                carrito.add(new ItemVenta(p, cant));
            }
            txtCodProducto.setText("");
            spnCantidad.setValue(1);
            refrescarCarrito();
            txtCodProducto.requestFocus();
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    private void quitar() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione un producto del carrito para quitarlo.");
            return;
        }
        carrito.remove(fila);
        refrescarCarrito();
    }

    private void refrescarCarrito() {
        modeloCarrito.setRowCount(0);
        for (ItemVenta it : carrito) {
            modeloCarrito.addRow(new Object[]{it.getProducto().getCodigo(), it.getProducto().getNombre(),
                it.getCantidad(), Moneda.formato(it.getPrecioUnitario()), Moneda.formato(it.subtotal()),
                it.getProducto().getStock()});
        }
        previsualizar();
    }

    // nombre del cliente / producto (con su stock) y totales mientras se escribe
    private void previsualizar() {
        Cliente c = ServicioClientes.buscar(txtCodCliente.getText());
        Producto p = ServicioProductos.buscar(txtCodProducto.getText());
        if (btnRegistrar.isEnabled()) {
            if (txtCodCliente.getText().trim().isEmpty()) {
                lblCliente.setForeground(Estilo.GRIS);
                lblCliente.setText("Escribe el código del cliente");
            } else if (c == null) {
                lblCliente.setForeground(Estilo.ROJO);
                lblCliente.setText("Cliente no encontrado");
            } else {
                lblCliente.setForeground(Estilo.TEXTO);
                lblCliente.setText(c.getNombre() + " (" + c.getEdad() + " años)"
                        + (c.tieneDescuento() ? " - 15% dscto." : ""));
            }
        }
        if (txtCodProducto.getText().trim().isEmpty()) {
            lblProducto.setForeground(Estilo.GRIS);
            lblProducto.setText("Escribe el código del producto");
        } else if (p == null) {
            lblProducto.setForeground(Estilo.ROJO);
            lblProducto.setText("Producto no encontrado");
        } else if (p.getStock() == 0) {
            lblProducto.setForeground(Estilo.ROJO);
            lblProducto.setText(p.getNombre() + " - AGOTADO (stock 0)");
        } else {
            lblProducto.setForeground(Estilo.TEXTO);
            lblProducto.setText(p.getNombre() + " - " + Moneda.formato(p.getPrecio()) + " - stock: " + p.getStock());
        }
        double sub = 0;
        for (ItemVenta it : carrito) {
            sub += it.subtotal();
        }
        sub = Moneda.redondear(sub);
        double desc = (c == null) ? 0 : Moneda.redondear(sub * c.getPorcentajeDescuento());
        lblSubtotal.setText("Subtotal: " + Moneda.formato(sub));
        lblDescuento.setText("Descuento: " + Moneda.formato(desc));
        lblCobrar.setText("TOTAL: " + Moneda.formato(Moneda.redondear(sub - desc)));
    }

    // ---------- venta ----------
    private void registrar() {
        try {
            Usuario u = ServicioSesion.getUsuarioActual();
            List<ItemVenta> items = new ArrayList<>(carrito);
            Venta venta;
            if (u instanceof Empleado) {       // el empleado guarda el codigo del cliente y del producto
                venta = ((Empleado) u).registrarVenta(txtCodCliente.getText(), items);
            } else {
                venta = ServicioVentas.registrarVenta(txtCodCliente.getText(), items);
            }
            limpiarTodo();
            actualizar();
            abrirPdf(venta);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void abrirPdf(Venta venta) {
        try {
            File pdf = BoletaPDF.generar(venta);
            if (!BoletaPDF.abrir(pdf)) {
                JOptionPane.showMessageDialog(this, "Boleta guardada en:\n" + pdf.getAbsolutePath());
            }
        } catch (IOException ex) {
            Estilo.aviso(this, "No se pudo crear el PDF (¿está abierto en otro programa?):\n" + ex.getMessage());
        }
    }

    private void verBoleta() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione una venta de la tabla.");
            return;
        }
        abrirPdf(ServicioVentas.obtenerVenta(fila));
    }

    // abre la ventana para corregir la venta elegida; al guardar se genera la boleta con los cambios
    private void editarVenta() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione una venta de la tabla para editarla.");
            return;
        }
        new FormEditarVenta(ServicioVentas.obtenerVenta(fila), this::despuesDeEditar).setVisible(true);
    }

    private void despuesDeEditar(Venta venta) {
        actualizar();
        abrirPdf(venta);       // boleta nueva con los cambios
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione una venta de la tabla.");
            return;
        }
        Venta v = ServicioVentas.obtenerVenta(fila);
        if (Estilo.confirmar(this, "¿Eliminar la venta " + v.getCodigo() + " por " + Moneda.formato(v.total())
                + "? Sus productos vuelven al stock.")) {
            ServicioVentas.eliminarVenta(v);
            actualizar();
        }
    }

    private void limpiarTodo() {
        carrito.clear();
        txtCodCliente.setText("");
        txtCodProducto.setText("");
        spnCantidad.setValue(1);
        refrescarCarrito();
        txtCodCliente.requestFocus();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        int num = 1;
        for (Venta v : ServicioVentas.listarVentas()) {
            modelo.addRow(v.RegistrarDatos(num++));
        }
        lblTotalVendido.setText("Total vendido: " + Moneda.formato(ServicioVentas.totalIngresado()));
        lblCantidad.setText("Ventas registradas: " + ServicioVentas.cantidadVentas()
                + "   |   Productos vendidos: " + ServicioVentas.unidadesVendidas());
        refrescarCarrito();      // el stock del carrito puede haber cambiado
    }

    private void txtCodClienteActionPerformed(java.awt.event.ActionEvent evt) {
        txtCodProducto.requestFocus();
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

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {
        registrar();
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        limpiarTodo();
    }

    private void btnBoletaActionPerformed(java.awt.event.ActionEvent evt) {
        verBoleta();
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        editarVenta();
    }

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        eliminar();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        pnlVenta = new javax.swing.JPanel();
        txtCodCliente = new javax.swing.JTextField();
        lblCliente = new javax.swing.JLabel();
        txtCodProducto = new javax.swing.JTextField();
        lblProducto = new javax.swing.JLabel();
        spnCantidad = new javax.swing.JSpinner();
        btnAgregar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaCarrito = new javax.swing.JTable();
        lblSubtotal = new javax.swing.JLabel();
        lblDescuento = new javax.swing.JLabel();
        lblCobrar = new javax.swing.JLabel();
        btnRegistrar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        btnBoleta = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        lblTotalVendido = new javax.swing.JLabel();
        lblCantidad = new javax.swing.JLabel();

        setBackground(new java.awt.Color(236, 240, 247));
        setPreferredSize(new java.awt.Dimension(1005, 650));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setText("VENTAS");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        pnlVenta.setBackground(new java.awt.Color(255, 255, 255));
        pnlVenta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NUEVA VENTA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlVenta.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        txtCodCliente.setBackground(new java.awt.Color(255, 255, 255));
        txtCodCliente.setForeground(new java.awt.Color(28, 40, 60));
        txtCodCliente.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtCodCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código del cliente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        txtCodCliente.addActionListener(this::txtCodClienteActionPerformed);
        pnlVenta.add(txtCodCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 26, 270, 54));

        lblCliente.setForeground(new java.awt.Color(110, 125, 148));
        lblCliente.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblCliente.setText(" ");
        pnlVenta.add(lblCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 82, 270, 18));

        txtCodProducto.setBackground(new java.awt.Color(255, 255, 255));
        txtCodProducto.setForeground(new java.awt.Color(28, 40, 60));
        txtCodProducto.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtCodProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Código del producto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        txtCodProducto.addActionListener(this::txtCodProductoActionPerformed);
        pnlVenta.add(txtCodProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 106, 270, 54));

        lblProducto.setForeground(new java.awt.Color(110, 125, 148));
        lblProducto.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblProducto.setText(" ");
        pnlVenta.add(lblProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 162, 270, 18));

        spnCantidad.setBackground(new java.awt.Color(255, 255, 255));
        spnCantidad.setForeground(new java.awt.Color(28, 40, 60));
        spnCantidad.setFont(new java.awt.Font("SansSerif", 0, 14));
        spnCantidad.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cantidad", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlVenta.add(spnCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 190, 120, 54));

        btnAgregar.setBackground(new java.awt.Color(31, 55, 85));
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnAgregar.setText("AGREGAR");
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        pnlVenta.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(144, 192, 140, 50));

        tablaCarrito.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tablaCarrito);
        pnlVenta.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(296, 26, 436, 216));

        lblSubtotal.setForeground(new java.awt.Color(28, 40, 60));
        lblSubtotal.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblSubtotal.setText("Subtotal: S/ 0.00");
        pnlVenta.add(lblSubtotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(744, 26, 205, 22));

        lblDescuento.setForeground(new java.awt.Color(28, 40, 60));
        lblDescuento.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblDescuento.setText("Descuento: S/ 0.00");
        pnlVenta.add(lblDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(744, 50, 205, 22));

        lblCobrar.setForeground(new java.awt.Color(35, 113, 237));
        lblCobrar.setFont(new java.awt.Font("SansSerif", 1, 18));
        lblCobrar.setText("TOTAL: S/ 0.00");
        pnlVenta.add(lblCobrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(744, 74, 205, 28));

        btnRegistrar.setBackground(new java.awt.Color(35, 113, 237));
        btnRegistrar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnRegistrar.setText("REGISTRAR VENTA");
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(this::btnRegistrarActionPerformed);
        pnlVenta.add(btnRegistrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(744, 112, 205, 38));

        btnQuitar.setBackground(new java.awt.Color(31, 55, 85));
        btnQuitar.setForeground(new java.awt.Color(255, 255, 255));
        btnQuitar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnQuitar.setText("QUITAR PRODUCTO");
        btnQuitar.setFocusPainted(false);
        btnQuitar.addActionListener(this::btnQuitarActionPerformed);
        pnlVenta.add(btnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(744, 156, 205, 38));

        btnLimpiar.setBackground(new java.awt.Color(13, 27, 47));
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        pnlVenta.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(744, 200, 205, 38));
        add(pnlVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 64, 957, 262));

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 336, 957, 250));

        btnBoleta.setBackground(new java.awt.Color(31, 55, 85));
        btnBoleta.setForeground(new java.awt.Color(255, 255, 255));
        btnBoleta.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnBoleta.setText("VER BOLETA (PDF)");
        btnBoleta.setFocusPainted(false);
        btnBoleta.addActionListener(this::btnBoletaActionPerformed);
        add(btnBoleta, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 598, 195, 38));

        btnEliminar.setBackground(new java.awt.Color(200, 40, 40));
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnEliminar.setText("ELIMINAR VENTA");
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(229, 598, 170, 38));

        btnEditar.setBackground(new java.awt.Color(31, 55, 85));
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnEditar.setText("EDITAR VENTA");
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(409, 598, 170, 38));

        lblTotalVendido.setForeground(new java.awt.Color(35, 113, 237));
        lblTotalVendido.setFont(new java.awt.Font("SansSerif", 1, 16));
        lblTotalVendido.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalVendido.setText("Total vendido: S/ 0.00");
        add(lblTotalVendido, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 594, 381, 24));

        lblCantidad.setForeground(new java.awt.Color(110, 125, 148));
        lblCantidad.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblCantidad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCantidad.setText("Ventas registradas: 0");
        add(lblCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 620, 521, 20));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBoleta;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblCobrar;
    private javax.swing.JLabel lblDescuento;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalVendido;
    private javax.swing.JPanel pnlVenta;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JTable tabla;
    private javax.swing.JTable tablaCarrito;
    private javax.swing.JTextField txtCodCliente;
    private javax.swing.JTextField txtCodProducto;
    // End of variables declaration//GEN-END:variables
}
