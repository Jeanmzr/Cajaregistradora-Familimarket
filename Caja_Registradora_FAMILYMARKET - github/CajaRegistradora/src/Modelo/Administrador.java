package Modelo;

// Herencia de Usuario: tiene acceso a todo, incluido el registro de usuarios
public class Administrador extends Usuario {

    public Administrador(String usuario, String clave, String nombre) {
        super(usuario, clave, nombre, "ADMINISTRADOR");
    }

    @Override
    public boolean puedeVerReportes() { return true; }

    @Override
    public boolean puedeVender() { return true; }

    @Override
    public boolean puedeGestionarProductos() { return true; }

    @Override
    public boolean puedeEliminar() { return true; }

    @Override
    public boolean puedeGestionarUsuarios() { return true; }

    @Override
    public String getDescripcion() {
        return getNombre() + " (Administrador)";
    }
}
