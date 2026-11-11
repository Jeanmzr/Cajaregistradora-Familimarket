package Vista;

import Componentes.Estilo;
import Componentes.Modulo;
import Modelo.Moneda;
import Modelo.Venta;
import Reporte.BoletaPDF;
import Servicio.ServicioVentas;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/** Historial de boletas: se buscan por N° de boleta, cliente o producto y se abren en PDF */
public class FormBoletas extends javax.swing.JPanel implements Modulo {
    private final DefaultTableModel modelo = Estilo.modelo(new String[]{"Boleta", "Fecha", "Cód. cliente", "Cliente",
        "Productos", "Unid.", "Total", "Atendido por"});
    private List<Venta> mostradas = new ArrayList<>();

    public FormBoletas() {
        initComponents();
        tabla.setModel(modelo);
        Estilo.tabla(tabla);
        Estilo.estilizar(jScrollPane1);
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrir();
                }
            }
        });
        Estilo.alCambiar(txtBuscar, this::cargarTabla);
    }

    private void abrir() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.aviso(this, "Seleccione una boleta de la tabla (o haga doble clic en ella).");
            return;
        }
        try {
            File pdf = BoletaPDF.generar(mostradas.get(fila));
            if (!BoletaPDF.abrir(pdf)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Boleta guardada en:\n" + pdf.getAbsolutePath());
            }
        } catch (IOException ex) {
            Estilo.aviso(this, "No se pudo crear el PDF (¿está abierto en otro programa?):\n" + ex.getMessage());
        }
    }

    private void cargarTabla() {
        String t = txtBuscar.getText().trim().toLowerCase();
        mostradas = new ArrayList<>();
        modelo.setRowCount(0);
        for (Venta v : ServicioVentas.listarVentas()) {
            if (t.isEmpty() || v.getCodigo().toLowerCase().contains(t)
                    || v.getCliente().getNombre().toLowerCase().contains(t)
                    || v.getCliente().getCodigo().toLowerCase().contains(t)
                    || v.descripcionProductos().toLowerCase().contains(t)) {
                mostradas.add(v);
                modelo.addRow(new Object[]{v.getCodigo(), v.getFecha(), v.getCliente().getCodigo(),
                    v.getCliente().getNombre(), v.descripcionProductos(), v.getUnidades(),
                    Moneda.formato(v.total()), v.getAtendidoPor()});
            }
        }
        lblRegistros.setText("Cantidad de boletas: " + mostradas.size());
    }

    @Override
    public void actualizar() {
        cargarTabla();
    }

    private void btnAbrirActionPerformed(java.awt.event.ActionEvent evt) {
        abrir();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        btnAbrir = new javax.swing.JButton();
        lblRegistros = new javax.swing.JLabel();

        setBackground(new java.awt.Color(236, 240, 247));
        setPreferredSize(new java.awt.Dimension(1005, 650));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setForeground(new java.awt.Color(13, 27, 47));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setText("BOLETAS");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 14, 700, 40));

        txtBuscar.setBackground(new java.awt.Color(255, 255, 255));
        txtBuscar.setForeground(new java.awt.Color(28, 40, 60));
        txtBuscar.setFont(new java.awt.Font("SansSerif", 0, 14));
        txtBuscar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Buscar por N° de boleta, cliente o producto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12)));
        add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 64, 430, 56));

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabla);
        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 130, 957, 452));

        btnAbrir.setBackground(new java.awt.Color(35, 113, 237));
        btnAbrir.setForeground(new java.awt.Color(255, 255, 255));
        btnAbrir.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnAbrir.setText("ABRIR BOLETA (PDF)");
        btnAbrir.setFocusPainted(false);
        btnAbrir.addActionListener(this::btnAbrirActionPerformed);
        add(btnAbrir, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 596, 210, 42));

        lblRegistros.setForeground(new java.awt.Color(28, 40, 60));
        lblRegistros.setFont(new java.awt.Font("SansSerif", 1, 13));
        lblRegistros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRegistros.setText("Cantidad de boletas: 0");
        add(lblRegistros, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 606, 381, 24));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrir;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblRegistros;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTable tabla;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
