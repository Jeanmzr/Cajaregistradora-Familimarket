package Modelo;

// Herencia de Usuario: ve reportes (por fecha, por cliente, boletas de un cliente),
// el dinero ingresado y los productos vendidos. Tambien administra productos.
public class Encargado extends Usuario {

    public Encargado(String usuario, String clave, String nombre) {
        super(usuario, clave, nombre, "ENCARGADO");
    }

    @Override
    public boolean puedeVerReportes() {
        return true;
    }

    @Override
    public boolean puedeGestionarProductos() {
        return true;
    }

    @Override
    public boolean puedeEliminar() {
        return true;
    }

    @Override
    public String getDescripcion() {
        return getNombre() + " (Encargado)";
    }
}
