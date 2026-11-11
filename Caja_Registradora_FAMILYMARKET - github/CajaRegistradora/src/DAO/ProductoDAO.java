package DAO;

import Modelo.Producto;
import Persistencia.BaseDatosException;
import Persistencia.Codigos;
import Persistencia.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla `productos`. El codigo "P003" corresponde a idproducto = 3. */
public class ProductoDAO {

    public List<Producto> listar() {
        String sql = "SELECT idproducto, nombreproducto, preciounidad, stock, ajustevendidas "
                + "FROM productos ORDER BY idproducto";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal precio = rs.getBigDecimal("preciounidad");
                Producto p = new Producto(Codigos.producto(rs.getInt("idproducto")),
                        rs.getString("nombreproducto"),
                        precio == null ? 0 : precio.doubleValue(),
                        rs.getInt("stock"));
                p.setAjusteVendidas(rs.getInt("ajustevendidas"));
                lista.add(p);
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudieron leer los productos.", ex);
        }
        return lista;
    }

    public void insertar(Producto p) {
        String sql = "INSERT INTO productos (idproducto, nombreproducto, preciounidad, stock, ajustevendidas, indicador) "
                + "VALUES (?, ?, ?, ?, ?, 'S')";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Codigos.numero(p.getCodigo()));
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, BigDecimal.valueOf(p.getPrecio()));
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getAjusteVendidas());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo guardar el producto.", ex);
        }
    }

    // actualiza nombre, precio, stock y ajuste de "unidades vendidas" (se busca por el codigo)
    public void actualizar(Producto p) {
        String sql = "UPDATE productos SET nombreproducto = ?, preciounidad = ?, stock = ?, ajustevendidas = ? "
                + "WHERE idproducto = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setBigDecimal(2, BigDecimal.valueOf(p.getPrecio()));
            ps.setInt(3, p.getStock());
            ps.setInt(4, p.getAjusteVendidas());
            ps.setInt(5, Codigos.numero(p.getCodigo()));
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo actualizar el producto.", ex);
        }
    }

    public void eliminar(String codigo) {
        String sql = "DELETE FROM productos WHERE idproducto = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Codigos.numero(codigo));
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new BaseDatosException("No se puede eliminar el producto: esta usado en una boleta.", ex);
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo eliminar el producto.", ex);
        }
    }
}
