package Modelo;

/**
 * Clase ABSTRACTA hija de Persona: quien inicia sesion en el sistema.
 * Cada rol (hijo) decide a que modulos tiene acceso.
 */
public abstract class Usuario extends Persona {
    private String usuario;
    private final String clave;
    private final String tipo;   // "TRABAJADOR", "ENCARGADO" o "ADMINISTRADOR"

    protected Usuario(String usuario, String clave, String nombre, String tipo) {
        super(nombre);
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el usuario.");
        }
        if (clave == null || clave.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }
        this.usuario = usuario.trim();
        this.clave = clave;
        this.tipo = tipo;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getTipo() {
        return tipo;
    }

    // la necesita UsuarioDAO para guardar la cuenta en la base de datos
    public String getClave() {
        return clave;
    }

    public void setUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el usuario.");
        }
        this.usuario = usuario.trim();
    }

    // copia de esta cuenta con otro rol (conserva usuario, clave y nombre)
    public Usuario conRol(String rol) {
        switch (rol) {
            case "TRABAJADOR":
                return new Empleado(usuario, clave, getNombre());
            case "ENCARGADO":
                return new Encargado(usuario, clave, getNombre());
            case "ADMINISTRADOR":
                return new Administrador(usuario, clave, getNombre());
            default:
                throw new IllegalArgumentException("Seleccione un rol.");
        }
    }

    public boolean validar(String usuario, String clave) {
        return this.usuario.equals(usuario) && this.clave.equals(clave);
    }

    // METODO ABSTRACTO 2: cada tipo de usuario decide si puede ver los reportes
    public abstract boolean puedeVerReportes();

    // ----- permisos segun el rol (por defecto no tiene ninguno) -----
    public boolean puedeVender() { return false; }
    public boolean puedeGestionarProductos() { return false; }   // agregar / eliminar productos
    public boolean puedeEliminar() { return false; }             // eliminar clientes y ventas
    public boolean puedeGestionarUsuarios() { return false; }    // modulo de usuarios
}
