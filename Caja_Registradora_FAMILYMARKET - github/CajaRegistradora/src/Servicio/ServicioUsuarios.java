package Servicio;

import DAO.UsuarioDAO;
import Modelo.Administrador;
import Modelo.Empleado;
import Modelo.Encargado;
import Modelo.Usuario;
import Persistencia.RepositorioMemoria;
import java.util.ArrayList;
import java.util.List;

/** registro, filtro y eliminacion de usuarios segun su rol */
public class ServicioUsuarios {
    public static final String[] ROLES = {"TRABAJADOR", "ENCARGADO", "ADMINISTRADOR"};

    private static final UsuarioDAO dao = new UsuarioDAO();

    private ServicioUsuarios() {}

    public static Usuario registrar(String rol, String usuario, String clave, String nombre) {
        String user = (usuario == null) ? "" : usuario.trim();
        if (buscar(user) != null) {
            throw new IllegalArgumentException("El usuario '" + user + "' ya existe.");
        }
        Usuario nuevo;
        switch (rol) {
            case "TRABAJADOR":
                nuevo = new Empleado(user, clave, nombre);
                break;
            case "ENCARGADO":
                nuevo = new Encargado(user, clave, nombre);
                break;
            case "ADMINISTRADOR":
                nuevo = new Administrador(user, clave, nombre);
                break;
            default:
                throw new IllegalArgumentException("Seleccione un rol.");
        }
        dao.insertar(nuevo);                       // primero la base de datos...
        RepositorioMemoria.agregarUsuario(nuevo);  // ...y luego la lista en memoria
        return nuevo;
    }

    // cambia el nombre, el usuario y/o el rol de una cuenta guardada (la clave no cambia)
    public static Usuario editar(String usuarioActual, String rol, String usuarioNuevo, String nombre) {
        Usuario u = buscar(usuarioActual);
        if (u == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        String nuevo = (usuarioNuevo == null) ? "" : usuarioNuevo.trim();
        if (nuevo.isEmpty()) {
            throw new IllegalArgumentException("Ingrese el usuario.");
        }
        Usuario otro = buscar(nuevo);
        if (otro != null && otro != u) {
            throw new IllegalArgumentException("El usuario '" + nuevo + "' ya existe.");
        }
        boolean rolValido = false;
        for (String r : ROLES) {
            rolValido = rolValido || r.equals(rol);
        }
        if (!rolValido) {
            throw new IllegalArgumentException("Seleccione un rol.");
        }
        boolean cambiaRol = !u.getTipo().equals(rol);
        if (cambiaRol) {
            Usuario sesion = ServicioSesion.getUsuarioActual();
            if (sesion != null && sesion.getUsuario().equals(u.getUsuario())) {
                throw new IllegalStateException("No puedes cambiar el rol de la cuenta con la que iniciaste sesion.");
            }
            if (u.getTipo().equals("ADMINISTRADOR") && contar("ADMINISTRADOR") <= 1) {
                throw new IllegalStateException("Debe quedar al menos un administrador.");
            }
        }
        new Empleado(nuevo, "1234", nombre);        // valida usuario y nombre antes de cambiar nada
        dao.actualizar(u.getUsuario(), nuevo, nombre.trim(), rol);   // se guarda en la base de datos primero
        if (cambiaRol) {
            Usuario reemplazo = u.conRol(rol);      // el rol es la clase de la cuenta: se crea una del rol nuevo
            reemplazo.setNombre(nombre);
            reemplazo.setUsuario(nuevo);
            RepositorioMemoria.reemplazarUsuario(u, reemplazo);
            return reemplazo;
        }
        u.setNombre(nombre);
        u.setUsuario(nuevo);
        return u;
    }

    private static int contar(String rol) {
        int n = 0;
        for (Usuario u : RepositorioMemoria.listarUsuarios()) {
            if (u.getTipo().equals(rol)) {
                n++;
            }
        }
        return n;
    }

    public static void eliminar(String usuario) {
        Usuario u = buscar(usuario);
        if (u == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        Usuario actual = ServicioSesion.getUsuarioActual();
        if (actual != null && actual.getUsuario().equals(u.getUsuario())) {
            throw new IllegalStateException("No puedes eliminar el usuario con el que iniciaste sesion.");
        }
        dao.eliminar(u.getUsuario());
        RepositorioMemoria.eliminarUsuario(u);
    }

    public static Usuario buscar(String usuario) {
        for (Usuario u : RepositorioMemoria.listarUsuarios()) {
            if (u.getUsuario().equalsIgnoreCase(usuario == null ? "" : usuario.trim())) {
                return u;
            }
        }
        return null;
    }

    // filtra por rol ("TODOS" = sin filtro) y por texto (nombre o usuario)
    public static List<Usuario> filtrar(String rol, String texto) {
        String t = (texto == null) ? "" : texto.trim().toLowerCase();
        List<Usuario> lista = new ArrayList<>();
        for (Usuario u : RepositorioMemoria.listarUsuarios()) {
            boolean okRol = rol == null || rol.equals("TODOS") || u.getTipo().equals(rol);
            boolean okTexto = t.isEmpty() || u.getNombre().toLowerCase().contains(t)
                    || u.getUsuario().toLowerCase().contains(t);
            if (okRol && okTexto) {
                lista.add(u);
            }
        }
        return lista;
    }
}
