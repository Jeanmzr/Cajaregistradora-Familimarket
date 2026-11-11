package DAO;

import Modelo.Administrador;
import Modelo.Empleado;
import Modelo.Encargado;
import Modelo.Usuario;
import Persistencia.BaseDatosException;
import Persistencia.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla `usuarios`. El rol (tipo) decide que clase hija de Usuario se crea al leer. */
public class UsuarioDAO {

    public List<Usuario> listar() {
        String sql = "SELECT usuario, clave, nombre, tipo FROM usuarios ORDER BY idusuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crear(rs.getString("tipo"), rs.getString("usuario"),
                        rs.getString("clave"), rs.getString("nombre")));
            }
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudieron leer los usuarios.", ex);
        }
        return lista;
    }

    // polimorfismo: devuelve un Empleado, Encargado o Administrador segun el rol guardado
    private Usuario crear(String tipo, String usuario, String clave, String nombre) {
        switch (tipo) {
            case "TRABAJADOR":
                return new Empleado(usuario, clave, nombre);
            case "ENCARGADO":
                return new Encargado(usuario, clave, nombre);
            case "ADMINISTRADOR":
                return new Administrador(usuario, clave, nombre);
            default:
                throw new IllegalArgumentException("Rol desconocido en la base de datos: " + tipo);
        }
    }

    public void insertar(Usuario u) {
        String sql = "INSERT INTO usuarios (usuario, clave, nombre, tipo) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getClave());
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getTipo());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new BaseDatosException("El usuario '" + u.getUsuario() + "' ya existe.", ex);
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo guardar el usuario.", ex);
        }
    }

    // el usuario se identifica por su nombre de usuario anterior (puede cambiar al editar)
    public void actualizar(String usuarioAnterior, String usuarioNuevo, String nombre, String tipo) {
        String sql = "UPDATE usuarios SET usuario = ?, nombre = ?, tipo = ? WHERE usuario = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuarioNuevo);
            ps.setString(2, nombre);
            ps.setString(3, tipo);
            ps.setString(4, usuarioAnterior);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new BaseDatosException("El usuario '" + usuarioNuevo + "' ya existe.", ex);
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo actualizar el usuario.", ex);
        }
    }

    public void eliminar(String usuario) {
        String sql = "DELETE FROM usuarios WHERE usuario = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BaseDatosException("No se pudo eliminar el usuario.", ex);
        }
    }
}
