package Servicio;

import Modelo.Usuario;
import Persistencia.RepositorioMemoria;

/** inicio y cierre de sesión */
public class ServicioSesion {
    private static Usuario usuarioActual;   // privado: se lee con getUsuarioActual()

    private ServicioSesion() {}

    // devuelve el usuario si los datos son correctos si no, null
    public static Usuario iniciarSesion(String tipo, String usuario, String clave) {
        for (Usuario u : RepositorioMemoria.listarUsuarios()) {
            if (u.getTipo().equals(tipo) && u.validar(usuario, clave)) {
                usuarioActual = u;
                return u;
            }
        }
        return null;
    }

    // el rol lo da el propio usuario registrado (ya no se elige al ingresar)
    public static Usuario iniciarSesion(String usuario, String clave) {
        for (Usuario u : RepositorioMemoria.listarUsuarios()) {
            if (u.validar(usuario == null ? "" : usuario.trim(), clave)) {
                usuarioActual = u;
                return u;
            }
        }
        return null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}
