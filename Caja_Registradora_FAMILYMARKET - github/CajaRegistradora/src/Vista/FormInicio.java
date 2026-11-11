package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Moneda;
import Modelo.Usuario;
import Modelo.Venta;
import Servicio.ServicioClientes;
import Servicio.ServicioProductos;
import Servicio.ServicioSesion;
import Servicio.ServicioVentas;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/** Pantalla de inicio: saludo, cifras generales y ultimas ventas */
public class FormInicio extends javax.swing.JPanel implements Modulo {
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"Boleta", "Fecha", "Cliente", "Productos", "Total"});

    public FormInicio() {
        initComponents();
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);
    }

    @Override
    public void actualizar() {
        Usuario u = ServicioSesion.getUsuarioActual();
        lblSaludo.setText("Bienvenido, " + u.getNombre() + "  (" + u.getTipo() + ")");
        lblTotal.setText(Moneda.formato(ServicioVentas.totalIngresado()));
        lblVentas.setText(String.valueOf(ServicioVentas.cantidadVentas()));
        lblClientes.setText(String.valueOf(ServicioClientes.listar().size()));
        lblProductos.setText(String.valueOf(ServicioProductos.listar().size()));

        modelo.setRowCount(0);
        List<Venta> ventas = ServicioVentas.listarVentas();
        for (int i = ventas.size() - 1; i >= 0 && i >= ventas.size() - 8; i--) {
            Venta v = ventas.get(i);
            modelo.addRow(new Object[]{v.getCodigo(), v.getFecha(), v.getCliente().getNombre(),
                v.descripcionProductos(), Moneda.formato(v.total())});
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        lblSaludo = new javax.swing.JLabel();
        pnlCartaTotal = new javax.swing.JPanel();
        lblTituloTotal = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        pnlCartaVentas = new javax.swing.JPanel();
        lblTituloVentas = new javax.swing.JLabel();
        lblVentas = new javax.swing.JLabel();
        pnlCartaClientes = new javax.swing.JPanel();
        lblTituloClientes = new javax.swing.JLabel();
        lblClientes = new javax.swing.JLabel();
        pnlCartaProductos = new javax.swing.JPanel();
        lblTituloProductos = new javax.swing.JLabel();
        lblProductos = new javax.swing.JLabel();
        lblUltimas = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();

        setBackground(new java.awt.Color(236, 240, 247));
        setPreferredSize(new java.awt.Dimension(1005, 650));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setText("INICIO");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        lblSaludo.setForeground(new java.awt.Color(110, 125, 148));
        lblSaludo.setFont(new java.awt.Font("SansSerif", 0, 15));
        lblSaludo.setText(" ");
        add(lblSaludo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 62, 957, 24));

        pnlCartaTotal.setBackground(new java.awt.Color(255, 255, 255));
        pnlCartaTotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(208, 217, 230)));
        pnlCartaTotal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTituloTotal.setForeground(new java.awt.Color(110, 125, 148));
        lblTituloTotal.setFont(new java.awt.Font("SansSerif", 1, 12));
        lblTituloTotal.setText("Total vendido");
        pnlCartaTotal.add(lblTituloTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 12, 196, 20));

        lblTotal.setForeground(new java.awt.Color(35, 113, 237));
        lblTotal.setFont(new java.awt.Font("SansSerif", 1, 26));
        lblTotal.setText("");
        pnlCartaTotal.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 38, 196, 40));
        add(pnlCartaTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 96, 228, 92));

        pnlCartaVentas.setBackground(new java.awt.Color(255, 255, 255));
        pnlCartaVentas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(208, 217, 230)));
        pnlCartaVentas.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTituloVentas.setForeground(new java.awt.Color(110, 125, 148));
        lblTituloVentas.setFont(new java.awt.Font("SansSerif", 1, 12));
        lblTituloVentas.setText("Ventas registradas");
        pnlCartaVentas.add(lblTituloVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 12, 196, 20));

        lblVentas.setForeground(new java.awt.Color(13, 27, 47));
        lblVentas.setFont(new java.awt.Font("SansSerif", 1, 26));
        lblVentas.setText("");
        pnlCartaVentas.add(lblVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 38, 196, 40));
        add(pnlCartaVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(266, 96, 228, 92));

        pnlCartaClientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlCartaClientes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(208, 217, 230)));
        pnlCartaClientes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTituloClientes.setForeground(new java.awt.Color(110, 125, 148));
        lblTituloClientes.setFont(new java.awt.Font("SansSerif", 1, 12));
        lblTituloClientes.setText("Clientes");
        pnlCartaClientes.add(lblTituloClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 12, 196, 20));

        lblClientes.setForeground(new java.awt.Color(13, 27, 47));
        lblClientes.setFont(new java.awt.Font("SansSerif", 1, 26));
        lblClientes.setText("");
        pnlCartaClientes.add(lblClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 38, 196, 40));
        add(pnlCartaClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(508, 96, 228, 92));

        pnlCartaProductos.setBackground(new java.awt.Color(255, 255, 255));
        pnlCartaProductos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(208, 217, 230)));
        pnlCartaProductos.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        lblTituloProductos.setForeground(new java.awt.Color(110, 125, 148));
        lblTituloProductos.setFont(new java.awt.Font("SansSerif", 1, 12));
        lblTituloProductos.setText("Productos");
        pnlCartaProductos.add(lblTituloProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 12, 196, 20));

        lblProductos.setForeground(new java.awt.Color(13, 27, 47));
        lblProductos.setFont(new java.awt.Font("SansSerif", 1, 26));
        lblProductos.setText("");
        pnlCartaProductos.add(lblProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 38, 196, 40));
        add(pnlCartaProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 96, 228, 92));

        lblUltimas.setForeground(new java.awt.Color(28, 40, 60));
        lblUltimas.setFont(new java.awt.Font("SansSerif", 1, 14));
        lblUltimas.setText("ÚLTIMAS VENTAS");
        add(lblUltimas, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 204, 400, 22));

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 232, 957, 400));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblClientes;
    private javax.swing.JLabel lblProductos;
    private javax.swing.JLabel lblSaludo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloClientes;
    private javax.swing.JLabel lblTituloProductos;
    private javax.swing.JLabel lblTituloTotal;
    private javax.swing.JLabel lblTituloVentas;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblUltimas;
    private javax.swing.JLabel lblVentas;
    private javax.swing.JPanel pnlCartaClientes;
    private javax.swing.JPanel pnlCartaProductos;
    private javax.swing.JPanel pnlCartaTotal;
    private javax.swing.JPanel pnlCartaVentas;
    private javax.swing.JTable tabla;
    // End of variables declaration//GEN-END:variables
}
