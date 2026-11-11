package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Cliente;
import Modelo.Moneda;
import Modelo.Venta;
import Reporte.BoletaPDF;
import Servicio.ServicioClientes;
import Servicio.ServicioSesion;
import Servicio.ServicioVentas;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

/** Reportes: por fecha, por cliente y boletas de un solo cliente; las ventas guardadas se pueden editar */
public class FormReportes extends javax.swing.JPanel implements Modulo {
    private final List<String> codigos = new ArrayList<>();     // codigo de cada item del combo (null = todos)
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"N°", "Boleta", "Fecha", "Cód. cliente",
        "Cliente", "Productos", "Unid.", "Subtotal", "Descuento", "Total"});
    private List<Venta> filtradas = new ArrayList<>();
    private boolean cargando = false;

    public FormReportes() {
        initComponents();
        configurarFecha(spnDesde);
        configurarFecha(spnHasta);
        spnDesde.addChangeListener(e -> aplicar());
        spnHasta.addChangeListener(e -> aplicar());

        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);
        int[] anchos = {40, 100, 125, 108, 170, 212, 55, 90, 90, 90};
        for (int columna = 0; columna < anchos.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(anchos[columna]);
        }
        actualizar();
    }

    // un selector de fecha (dia/mes/año) con la fecha de hoy
    private static void configurarFecha(JSpinner s) {
        s.setModel(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
    }

    private static LocalDate aDia(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String clienteElegido() {
        int fila = cbxCliente.getSelectedIndex();
        return (fila <= 0 || fila >= codigos.size()) ? null : codigos.get(fila);
    }

    // lee los filtros y llena la tabla
    private void aplicar() {
        if (cargando) {
            return;
        }
        lblError.setText(" ");
        LocalDate desde = chkFecha.isSelected() ? aDia(spnDesde) : null;
        LocalDate hasta = chkFecha.isSelected() ? aDia(spnHasta) : null;
        spnDesde.setEnabled(chkFecha.isSelected());
        spnHasta.setEnabled(chkFecha.isSelected());
        try {
            filtradas = ServicioVentas.filtrar(desde, hasta, clienteElegido());
        } catch (IllegalArgumentException ex) {
            filtradas = new ArrayList<>();
            lblError.setText(ex.getMessage());
        }
        modelo.setRowCount(0);
        int num = 1;
        for (Venta v : filtradas) {
            modelo.addRow(v.RegistrarDatos(num++));
        }
        lblTotal.setText("Total vendido: " + Moneda.formato(ServicioVentas.totalDe(filtradas)));
        lblUnidades.setText("Productos vendidos: " + ServicioVentas.unidadesDe(filtradas));
        lblVentas.setText("Cantidad de ventas: " + filtradas.size());
        lblDescuentos.setText("Descuentos otorgados: " + Moneda.formato(ServicioVentas.descuentosDe(filtradas)));
    }

    private List<String> descripcionFiltros() {
        List<String> f = new ArrayList<>();
        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        f.add(chkFecha.isSelected()
                ? "Fechas: del " + aDia(spnDesde).format(df) + " al " + aDia(spnHasta).format(df)
                : "Fechas: todas");
        f.add("Cliente: " + (clienteElegido() == null ? "todos" : cbxCliente.getSelectedItem()));
        return f;
    }

    private void reportePdf() {
        if (!lblError.getText().trim().isEmpty()) {
            Estilo.aviso(this, "Corrige las fechas antes de generar el reporte.");
            return;
        }
        try {
            String titulo = clienteElegido() == null ? "REPORTE DE VENTAS" : "REPORTE DE VENTAS POR CLIENTE";
            File pdf = BoletaPDF.generarReporte(titulo, descripcionFiltros(), filtradas,
                    ServicioSesion.getUsuarioActual().getNombre());
            abrir(pdf);
        } catch (IOException ex) {
            Estilo.aviso(this, "No se pudo crear el PDF:\n" + ex.getMessage());
        }
    }

    // todas las boletas de UN cliente (sin importar el filtro de fechas)
    private void boletasCliente() {
        String codigo = clienteElegido();
        if (codigo == null) {
            Estilo.aviso(this, "Elige un cliente en el filtro \"Cliente\" para ver sus boletas.");
            return;
        }
        Cliente cliente = ServicioClientes.buscar(codigo);
        try {
            abrir(BoletaPDF.generarBoletasDeCliente(cliente, ServicioVentas.filtrar(null, null, codigo)));
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        } catch (IOException ex) {
            Estilo.aviso(this, "No se pudo crear el PDF:\n" + ex.getMessage());
        }
    }

    private void verBoleta() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione una venta de la tabla.");
            return;
        }
        try {
            abrir(BoletaPDF.generar(filtradas.get(fila)));
        } catch (IOException ex) {
            Estilo.aviso(this, "No se pudo crear el PDF:\n" + ex.getMessage());
        }
    }

    // corrige los datos guardados de la venta elegida (cliente y productos)
    private void editar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione una venta de la tabla para editarla.");
            return;
        }
        new FormEditarVenta(filtradas.get(fila), v -> aplicar()).setVisible(true);
    }

    private void abrir(File pdf) {
        if (!BoletaPDF.abrir(pdf)) {
            javax.swing.JOptionPane.showMessageDialog(this, "PDF guardado en:\n" + pdf.getAbsolutePath());
        }
    }

    @Override
    public void actualizar() {
        cargando = true;
        String anterior = clienteElegido();
        cbxCliente.removeAllItems();
        codigos.clear();
        cbxCliente.addItem("TODOS");
        codigos.add(null);
        int seleccion = 0;
        for (Cliente c : ServicioClientes.listar()) {
            cbxCliente.addItem(c.getDescripcion());
            codigos.add(c.getCodigo());
            if (c.getCodigo().equals(anterior)) {
                seleccion = codigos.size() - 1;
            }
        }
        cbxCliente.setSelectedIndex(seleccion);
        cargando = false;
        aplicar();
    }

    private void chkFechaActionPerformed(java.awt.event.ActionEvent evt) {
        aplicar();
    }

    private void cbxClienteActionPerformed(java.awt.event.ActionEvent evt) {
        aplicar();
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        chkFecha.setSelected(false);
        cbxCliente.setSelectedIndex(0);
        spnDesde.setValue(new Date());
        spnHasta.setValue(new Date());
        aplicar();
    }

    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt) {
        reportePdf();
    }

    private void btnClienteActionPerformed(java.awt.event.ActionEvent evt) {
        boletasCliente();
    }

    private void btnBoletaActionPerformed(java.awt.event.ActionEvent evt) {
        verBoleta();
    }

    private void btnProductosActionPerformed(java.awt.event.ActionEvent evt) {
        new FormProductosVendidos(filtradas).setVisible(true);
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        editar();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        pnlFiltros = new javax.swing.JPanel();
        chkFecha = new javax.swing.JCheckBox();
        spnDesde = new javax.swing.JSpinner();
        spnHasta = new javax.swing.JSpinner();
        cbxCliente = new javax.swing.JComboBox();
        btnLimpiar = new javax.swing.JButton();
        pnlResumen = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lblUnidades = new javax.swing.JLabel();
        lblVentas = new javax.swing.JLabel();
        lblDescuentos = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        btnPdf = new javax.swing.JButton();
        btnCliente = new javax.swing.JButton();
        btnBoleta = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();

        setBackground(new java.awt.Color(236, 240, 247));
        setPreferredSize(new java.awt.Dimension(1005, 650));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setText("REPORTES");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        pnlFiltros.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltros.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FILTROS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlFiltros.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        chkFecha.setForeground(new java.awt.Color(28, 40, 60));
        chkFecha.setFont(new java.awt.Font("SansSerif", 1, 13));
        chkFecha.setText("Filtrar por fecha");
        chkFecha.setOpaque(false);
        chkFecha.addActionListener(this::chkFechaActionPerformed);
        pnlFiltros.add(chkFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 28, 170, 28));

        spnDesde.setBackground(new java.awt.Color(255, 255, 255));
        spnDesde.setForeground(new java.awt.Color(28, 40, 60));
        spnDesde.setFont(new java.awt.Font("SansSerif", 0, 14));
        spnDesde.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Desde", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlFiltros.add(spnDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(196, 22, 150, 52));

        spnHasta.setBackground(new java.awt.Color(255, 255, 255));
        spnHasta.setForeground(new java.awt.Color(28, 40, 60));
        spnHasta.setFont(new java.awt.Font("SansSerif", 0, 14));
        spnHasta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hasta", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlFiltros.add(spnHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(356, 22, 150, 52));

        cbxCliente.setBackground(new java.awt.Color(255, 255, 255));
        cbxCliente.setForeground(new java.awt.Color(28, 40, 60));
        cbxCliente.setFont(new java.awt.Font("SansSerif", 0, 14));
        cbxCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS" }));
        cbxCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cliente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        cbxCliente.addActionListener(this::cbxClienteActionPerformed);
        pnlFiltros.add(cbxCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(516, 22, 270, 52));

        btnLimpiar.setBackground(new java.awt.Color(31, 55, 85));
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnLimpiar.setText("LIMPIAR FILTROS");
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        pnlFiltros.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(794, 28, 150, 40));
        add(pnlFiltros, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 64, 957, 84));

        pnlResumen.setBackground(new java.awt.Color(255, 255, 255));
        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "RESUMEN DEL REPORTE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlResumen.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTotal.setForeground(new java.awt.Color(35, 113, 237));
        lblTotal.setFont(new java.awt.Font("SansSerif", 1, 17));
        lblTotal.setText("Total vendido: S/ 0.00");
        pnlResumen.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 26, 430, 28));

        lblUnidades.setForeground(new java.awt.Color(28, 40, 60));
        lblUnidades.setFont(new java.awt.Font("SansSerif", 1, 15));
        lblUnidades.setText("Productos vendidos: 0");
        pnlResumen.add(lblUnidades, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 28, 440, 26));

        lblVentas.setForeground(new java.awt.Color(110, 125, 148));
        lblVentas.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblVentas.setText("Cantidad de ventas: 0");
        pnlResumen.add(lblVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 58, 430, 22));

        lblDescuentos.setForeground(new java.awt.Color(110, 125, 148));
        lblDescuentos.setFont(new java.awt.Font("SansSerif", 0, 13));
        lblDescuentos.setText("Descuentos otorgados: S/ 0.00");
        pnlResumen.add(lblDescuentos, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 58, 440, 22));
        add(pnlResumen, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 158, 957, 90));

        lblError.setForeground(new java.awt.Color(200, 40, 40));
        lblError.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblError.setText(" ");
        add(lblError, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 252, 957, 18));

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 274, 957, 300));

        btnPdf.setBackground(new java.awt.Color(35, 113, 237));
        btnPdf.setForeground(new java.awt.Color(255, 255, 255));
        btnPdf.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnPdf.setText("REPORTE EN PDF");
        btnPdf.setFocusPainted(false);
        btnPdf.addActionListener(this::btnPdfActionPerformed);
        add(btnPdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 586, 170, 40));

        btnCliente.setBackground(new java.awt.Color(35, 113, 237));
        btnCliente.setForeground(new java.awt.Color(255, 255, 255));
        btnCliente.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnCliente.setText("BOLETAS DEL CLIENTE (PDF)");
        btnCliente.setFocusPainted(false);
        btnCliente.addActionListener(this::btnClienteActionPerformed);
        add(btnCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(202, 586, 270, 40));

        btnBoleta.setBackground(new java.awt.Color(31, 55, 85));
        btnBoleta.setForeground(new java.awt.Color(255, 255, 255));
        btnBoleta.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnBoleta.setText("VER BOLETA");
        btnBoleta.setFocusPainted(false);
        btnBoleta.addActionListener(this::btnBoletaActionPerformed);
        add(btnBoleta, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 586, 150, 40));

        btnProductos.setBackground(new java.awt.Color(31, 55, 85));
        btnProductos.setForeground(new java.awt.Color(255, 255, 255));
        btnProductos.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnProductos.setText("PRODUCTOS VENDIDOS");
        btnProductos.setFocusPainted(false);
        btnProductos.addActionListener(this::btnProductosActionPerformed);
        add(btnProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(638, 586, 210, 40));

        btnEditar.setBackground(new java.awt.Color(31, 55, 85));
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnEditar.setText("EDITAR");
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(856, 586, 125, 40));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBoleta;
    private javax.swing.JButton btnCliente;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnPdf;
    private javax.swing.JButton btnProductos;
    private javax.swing.JComboBox<String> cbxCliente;
    private javax.swing.JCheckBox chkFecha;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDescuentos;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblUnidades;
    private javax.swing.JLabel lblVentas;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JSpinner spnDesde;
    private javax.swing.JSpinner spnHasta;
    private javax.swing.JTable tabla;
    // End of variables declaration//GEN-END:variables
}
