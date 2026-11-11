package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Moneda;
import Modelo.Producto;
import Servicio.ServicioProductos;
import Servicio.ServicioVentas;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 * Producto vendido: unidades y dinero por producto. Las unidades vendidas se pueden editar:
 * si se cambian a un numero menor el stock sube, y si se cambian a uno mayor el stock baja.
 */
public class FormProductoVendido extends javax.swing.JPanel implements Modulo {
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"Código", "Producto", "Unidades vendidas",
        "Dinero ingresado", "Stock actual"});
    private Producto seleccionado;      // producto elegido en la tabla (null = ninguno)
    private boolean cargando = false;

    public FormProductoVendido() {
        initComponents();
        spnUnidades.setModel(new SpinnerNumberModel(0, 0, 100000, 1));
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                elegirFila();
            }
        });
        spnUnidades.addChangeListener(e -> mostrarEfecto());
        sinSeleccion();
    }

    private void sinSeleccion() {
        seleccionado = null;
        lblSeleccion.setText("Seleccione un producto de la tabla para editar sus unidades vendidas");
        lblEfecto2.setText(" ");
        spnUnidades.setEnabled(false);
        btnEditar.setEnabled(false);
    }

    private void elegirFila() {
        if (cargando) {
            return;
        }
        int fila = tabla.getSelectedRow();
        seleccionado = (fila < 0) ? null : ServicioProductos.buscar(modelo.getValueAt(fila, 0).toString());
        if (seleccionado == null) {
            sinSeleccion();
            return;
        }
        int unidades = Integer.parseInt(modelo.getValueAt(fila, 2).toString());
        spnUnidades.setEnabled(true);
        btnEditar.setEnabled(true);
        spnUnidades.setValue(unidades);
        lblSeleccion.setText("Producto: " + seleccionado.getCodigo() + " - " + seleccionado.getNombre()
                + "   (vendidas: " + unidades + ", stock: " + seleccionado.getStock() + ")");
        mostrarEfecto();
    }

    // muestra como quedaria el stock con el numero que hay en el selector
    private void mostrarEfecto() {
        if (seleccionado == null) {
            return;
        }
        int actual = ServicioVentas.unidadesVendidasDe(seleccionado);
        int nuevas = Integer.parseInt(spnUnidades.getValue().toString());
        int nuevoStock = seleccionado.getStock() + (actual - nuevas);
        boolean alcanza = nuevoStock >= 0 && nuevoStock <= Producto.STOCK_MAXIMO;
        lblEfecto2.setForeground(alcanza ? new Color(30, 90, 180) : Estilo.ROJO);
        lblEfecto2.setText("Stock: " + seleccionado.getStock() + "  ->  " + nuevoStock + (alcanza ? "" : "  (no es posible)"));
    }

    private void editar() {
        if (seleccionado == null) {
            Estilo.aviso(this, "Seleccione un producto de la tabla.");
            return;
        }
        int antes = ServicioVentas.unidadesVendidasDe(seleccionado);
        int stockAntes = seleccionado.getStock();
        int nuevas = Integer.parseInt(spnUnidades.getValue().toString());
        try {
            ServicioVentas.editarUnidadesVendidas(seleccionado.getCodigo(), nuevas);
            JOptionPane.showMessageDialog(this, "Producto " + seleccionado.getCodigo() + ": unidades vendidas " + antes
                    + " -> " + nuevas + ".\nStock " + stockAntes + " -> " + seleccionado.getStock() + ".");
            actualizar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo editar", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void actualizar() {
        cargando = true;
        modelo.setRowCount(0);
        for (Object[] fila : ServicioVentas.resumenConAjustes()) {
            modelo.addRow(fila);
        }
        cargando = false;
        lblTotal.setText("Total vendido: " + Moneda.formato(ServicioVentas.totalIngresadoAjustado()));
        lblUnidades.setText("Productos vendidos: " + ServicioVentas.unidadesVendidasAjustadas());
        sinSeleccion();
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        editar();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        pnlResumen = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lblUnidades = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        pnlEditar = new javax.swing.JPanel();
        lblSeleccion = new javax.swing.JLabel();
        spnUnidades = new javax.swing.JSpinner();
        btnEditar = new javax.swing.JButton();
        lblEfecto1 = new javax.swing.JLabel();
        lblEfecto2 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(236, 240, 247));
        setPreferredSize(new java.awt.Dimension(1005, 650));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setText("PRODUCTO VENDIDO");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        pnlResumen.setBackground(new java.awt.Color(255, 255, 255));
        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "RESUMEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlResumen.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTotal.setForeground(new java.awt.Color(35, 113, 237));
        lblTotal.setFont(new java.awt.Font("SansSerif", 1, 17));
        lblTotal.setText("Total vendido: S/ 0.00");
        pnlResumen.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 34, 420, 30));

        lblUnidades.setForeground(new java.awt.Color(28, 40, 60));
        lblUnidades.setFont(new java.awt.Font("SansSerif", 1, 15));
        lblUnidades.setText("Productos vendidos: 0");
        pnlResumen.add(lblUnidades, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 36, 400, 26));
        add(pnlResumen, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 64, 957, 84));

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 160, 957, 320));

        pnlEditar.setBackground(new java.awt.Color(255, 255, 255));
        pnlEditar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "EDITAR UNIDADES VENDIDAS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlEditar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblSeleccion.setForeground(new java.awt.Color(28, 40, 60));
        lblSeleccion.setFont(new java.awt.Font("SansSerif", 1, 13));
        lblSeleccion.setText(" ");
        pnlEditar.add(lblSeleccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 26, 925, 24));

        spnUnidades.setBackground(new java.awt.Color(255, 255, 255));
        spnUnidades.setForeground(new java.awt.Color(28, 40, 60));
        spnUnidades.setFont(new java.awt.Font("SansSerif", 0, 14));
        spnUnidades.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nuevas unidades vendidas", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        pnlEditar.add(spnUnidades, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 58, 240, 56));

        btnEditar.setBackground(new java.awt.Color(35, 113, 237));
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnEditar.setText("EDITAR");
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        pnlEditar.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 64, 170, 44));

        lblEfecto1.setForeground(new java.awt.Color(110, 125, 148));
        lblEfecto1.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblEfecto1.setText("Si bajas las unidades, el stock aumenta; si las subes, el stock baja.");
        pnlEditar.add(lblEfecto1, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 58, 490, 22));

        lblEfecto2.setForeground(new java.awt.Color(30, 90, 180));
        lblEfecto2.setFont(new java.awt.Font("SansSerif", 1, 14));
        lblEfecto2.setText(" ");
        pnlEditar.add(lblEfecto2, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 84, 490, 26));
        add(pnlEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 490, 957, 146));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblEfecto1;
    private javax.swing.JLabel lblEfecto2;
    private javax.swing.JLabel lblSeleccion;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblUnidades;
    private javax.swing.JPanel pnlEditar;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JSpinner spnUnidades;
    private javax.swing.JTable tabla;
    // End of variables declaration//GEN-END:variables
}
