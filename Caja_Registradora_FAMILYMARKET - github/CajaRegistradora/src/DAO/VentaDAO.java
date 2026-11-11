package DAO;

import Modelo.Cliente;
import Modelo.ItemVenta;
import Modelo.Producto;
import Modelo.Venta;
import Persistencia.BaseDatosException;
import Persistencia.Codigos;
import Persistencia.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a las tablas `ventas` (cabecera de la boleta) y `detalle_venta` (sus productos).
 * Cada operacion que toca varias tablas o el stock se hace en UNA transaccion:
 * si algo falla, se deshace todo (rollback). "B001-00003" corresponde a idventa = 3.
 */
public class VentaDAO {

    // Lee todas las boletas. Recibe los clientes y productos ya cargados para reusar esos mismos objetos.
    public List<Venta> listar(List<Cliente> clientes, List<Producto> productos) {
        Map<String, Cliente> porCliente = new HashMap<>();
        for (Cliente c : clientes) {
            porCliente.put(c.getCodigo(), c);
        }
        Map<String, Producto> porProducto = new HashMap<>();
        for (Producto p : productos) {
            porProducto.put(p.getCodigo(), p);
        }

        Map<Integer, List<ItemVenta>> detalles = new HashMap<>();
        List<Venta> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener()) {
            String sqlDetalle = "SELECT idventa, idproducto, preciounitario, cantidad "
                    + "FROM detalle_venta ORDER BY idventa, iddetalle";
            try (PreparedStatement ps = cn.prepareStatement(sqlDetalle); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = porProducto.get(Codigos.producto(rs.getInt("idproducto")));
                    if (p != null) {
                        BigDecimal precio = rs.getBigDecimal("preciounitario");
                        detalles.computeIfAbsent(rs.getInt("idventa"), k -> new ArrayList<>())
                                .add(new ItemVenta(p, precio.doubleValue(), rs.getInt("cantidad")));
                    }
                }
            }
            String sqlVenta = "SELECT idventa, fecha, atendidopor, idcliente, fechaedicion FROM ventas ORDER BY idventa";
            try (PreparedStatement ps = cn.prepareStatement(sqlVenta); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("idventa");
                    Cliente cliente = porCliente.get(Codigos.cliente(rs.getInt("idcliente")));
                    List<ItemVenta> items = detalles.get(id);
                    if (cliente != null && items != null && !items.isEmpty()) {
                        lista.add(new Venta(Codigos.boleta(id), cliente, items, rs.getString("atendidopor"),
                                rs.getObject("fecha", LocalDateTime.class),
                                rs.getObject("fechaedicion", LocalDateTime.class)));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudieron leer las ventas.", ex);
        }
        return lista;
    }

    // Guarda la boleta, sus productos y baja el stock. Todo o nada.
    public void insertar(Venta venta) {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int id = Codigos.numero(venta.getCodigo());
                String sql = "INSERT INTO ventas (idventa, fecha, atendidopor, idcliente) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setObject(2, venta.getFechaHora());
                    ps.setString(3, venta.getAtendidoPor());
                    ps.setInt(4, Codigos.numero(venta.getCliente().getCodigo()));
                    ps.executeUpdate();
                }
                insertarDetalles(cn, id, venta.getItems());
                descontarStock(cn, venta.getItems());
                cn.commit();
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo guardar la venta.", ex);
        }
    }

    // Cambia el cliente y los productos de una boleta ya guardada.
    // Se llama ANTES de editar el objeto en memoria: venta.getItems() todavia son los productos anteriores.
    public void actualizar(Venta venta, Cliente nuevoCliente, List<ItemVenta> nuevos) {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int id = Codigos.numero(venta.getCodigo());
                reponerStock(cn, venta.getItems());                    // lo vendido antes vuelve al stock
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM detalle_venta WHERE idventa = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                insertarDetalles(cn, id, nuevos);
                descontarStock(cn, nuevos);                            // y lo nuevo se descuenta
                String sql = "UPDATE ventas SET idcliente = ?, fechaedicion = ? WHERE idventa = ?";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, Codigos.numero(nuevoCliente.getCodigo()));
                    ps.setObject(2, LocalDateTime.now());
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo actualizar la venta.", ex);
        }
    }

    // Borra la boleta (sus productos se borran en cascada) y devuelve las unidades al stock.
    public void eliminar(Venta venta) {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                reponerStock(cn, venta.getItems());
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM ventas WHERE idventa = ?")) {
                    ps.setInt(1, Codigos.numero(venta.getCodigo()));
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo eliminar la venta.", ex);
        }
    }

    // ---------- pasos internos (usan la misma conexion/transaccion) ----------
    private void insertarDetalles(Connection cn, int idVenta, List<ItemVenta> items) throws SQLException {
        String sql = "INSERT INTO detalle_venta (idventa, idproducto, preciounitario, cantidad) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ItemVenta it : items) {
                ps.setInt(1, idVenta);
                ps.setInt(2, Codigos.numero(it.getProducto().getCodigo()));
                ps.setBigDecimal(3, BigDecimal.valueOf(it.getPrecioUnitario()));
                ps.setInt(4, it.getCantidad());
                ps.executeUpdate();
            }
        }
    }

    // resta en la BD solo si alcanza el stock (evita stock negativo aunque dos cajas vendan a la vez)
    private void descontarStock(Connection cn, List<ItemVenta> items) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE idproducto = ? AND stock >= ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ItemVenta it : items) {
                ps.setInt(1, it.getCantidad());
                ps.setInt(2, Codigos.numero(it.getProducto().getCodigo()));
                ps.setInt(3, it.getCantidad());
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Stock insuficiente de " + it.getProducto().getNombre() + ".");
                }
            }
        }
    }

    private void reponerStock(Connection cn, List<ItemVenta> items) throws SQLException {
        String sql = "UPDATE productos SET stock = LEAST(?, stock + ?) WHERE idproducto = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ItemVenta it : items) {
                ps.setInt(1, Producto.STOCK_MAXIMO);
                ps.setInt(2, it.getCantidad());
                ps.setInt(3, Codigos.numero(it.getProducto().getCodigo()));
                ps.executeUpdate();
            }
        }
    }
}
