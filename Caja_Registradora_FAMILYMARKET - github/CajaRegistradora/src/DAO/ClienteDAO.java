package DAO;

import Modelo.Cliente;
import Persistencia.BaseDatosException;
import Persistencia.Codigos;
import Persistencia.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla `clientes`. El codigo "C003" corresponde a idcliente = 3. */
public class ClienteDAO {

    public List<Cliente> listar() {
        String sql = "SELECT idcliente, nombre, edad, dni, telefono FROM clientes ORDER BY idcliente";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cliente(Codigos.cliente(rs.getInt("idcliente")), rs.getString("nombre"),
                        rs.getInt("edad"), rs.getString("dni"), rs.getString("telefono")));
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudieron leer los clientes.", ex);
        }
        return lista;
    }

    public void insertar(Cliente c) {
        String sql = "INSERT INTO clientes (idcliente, nombre, edad, dni, telefono) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Codigos.numero(c.getCodigo()));
            ps.setString(2, c.getNombre());
            ps.setInt(3, c.getEdad());
            ps.setString(4, c.getDni());
            ps.setString(5, c.getTelefono());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new BaseDatosException("Ya existe un cliente con ese DNI.", ex);
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo guardar el cliente.", ex);
        }
    }

    public void actualizar(Cliente c) {
        String sql = "UPDATE clientes SET nombre = ?, edad = ?, dni = ?, telefono = ? WHERE idcliente = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getEdad());
            ps.setString(3, c.getDni());
            ps.setString(4, c.getTelefono());
            ps.setInt(5, Codigos.numero(c.getCodigo()));
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new BaseDatosException("Ya existe un cliente con ese DNI.", ex);
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo actualizar el cliente.", ex);
        }
    }

    public void eliminar(String codigo) {
        String sql = "DELETE FROM clientes WHERE idcliente = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Codigos.numero(codigo));
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new BaseDatosException("No se puede eliminar el cliente: tiene boletas registradas.", ex);
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo eliminar el cliente.", ex);
        }
    }
}
