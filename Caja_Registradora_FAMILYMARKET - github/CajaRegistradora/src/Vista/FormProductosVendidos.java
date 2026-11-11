package Vista;
import Componentes.Estilo;
import Modelo.Moneda;
import Modelo.Venta;
import Servicio.ServicioVentas;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**Productos vendidos*/
public class FormProductosVendidos extends javax.swing.JFrame {
    DefaultTableModel modeloProductos;
    private final List<Venta> ventas;

    public FormProductosVendidos() {
        this(ServicioVentas.listarVentas());
    }

    // muestra los productos de las ventas que se le pasen (por ejemplo, las del reporte filtrado)
    public FormProductosVendidos(List<Venta> ventas) {
        this.ventas = ventas;
        initComponents();
        setLocationRelativeTo(null);
        String titulos[]={"Código","Producto","Unidades vendidas","Dinero ingresado"};
        modeloProductos=Estilo.modelo(titulos);
        tblProductos.setModel(modeloProductos);
        Estilo.tabla(tblProductos);
        cargarProductos();
    }

    void cargarProductos(){
        for (Object[] fila : ServicioVentas.resumenPorProducto(ventas)) {
            modeloProductos.addRow(fila);
        }
        lblUnidades.setText("Total de unidades: " + ServicioVentas.unidadesDe(ventas));
        lblIngreso.setText("Dinero ingresado: "
                + Moneda.formato(ServicioVentas.totalDe(ventas)));
    }

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlPrincipal = new javax.swing.JPanel();
        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        lblUnidades = new javax.swing.JLabel();
        lblIngreso = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Productos vendidos");
        setResizable(false);

        pnlPrincipal.setBackground(new java.awt.Color(240, 244, 250));
        pnlPrincipal.setPreferredSize(new java.awt.Dimension(540, 450));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlEncabezado.setBackground(new java.awt.Color(13, 27, 47));
        pnlEncabezado.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 22));
        lblTitulo.setText("PRODUCTOS VENDIDOS");
        pnlEncabezado.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 18, 420, 34));
        pnlPrincipal.add(pnlEncabezado, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 540, 70));

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblProductos);
        pnlPrincipal.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 500, 280));

        lblUnidades.setForeground(new java.awt.Color(28, 40, 60));
        lblUnidades.setFont(new java.awt.Font("SansSerif", 1, 14));
        lblUnidades.setText("Total de unidades: 0");
        pnlPrincipal.add(lblUnidades, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, 330, 26));

        lblIngreso.setForeground(new java.awt.Color(35, 113, 237));
        lblIngreso.setFont(new java.awt.Font("SansSerif", 1, 14));
        lblIngreso.setText("Dinero ingresado: S/ 0.00");
        pnlPrincipal.add(lblIngreso, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 408, 330, 26));

        btnCerrar.setBackground(new java.awt.Color(31, 55, 85));
        btnCerrar.setForeground(new java.awt.Color(255, 255, 255));
        btnCerrar.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnCerrar.setText("CERRAR");
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 390, 150, 40));

        getContentPane().add(pnlPrincipal, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblIngreso;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUnidades;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblProductos;
    // End of variables declaration//GEN-END:variables
}
