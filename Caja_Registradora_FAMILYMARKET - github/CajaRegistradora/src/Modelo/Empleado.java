package Modelo;

import Servicio.ServicioVentas;

// Herencia de Usuario (rol TRABAJADOR): atiende la caja. Guarda el codigo del cliente
// y el codigo del producto de la venta que esta registrando.
public class Empleado extends Usuario {
    private String codigoCliente = "";
    private String codigoProducto = "";

    public Empleado(String usuario, String clave, String nombre) {
        super(usuario, clave, nombre, "TRABAJADOR");
    }

    public String getCodigoCliente() { return codigoCliente; }
    public String getCodigoProducto() { return codigoProducto; }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = (codigoCliente == null) ? "" : codigoCliente.trim().toUpperCase();
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = (codigoProducto == null) ? "" : codigoProducto.trim().toUpperCase();
    }

    // Registra una venta usando el codigo del cliente y el codigo del producto
    public Venta registrarVenta(String codigoCliente, String codigoProducto, int cantidad) {
        setCodigoCliente(codigoCliente);
        setCodigoProducto(codigoProducto);
        return ServicioVentas.registrarVenta(this.codigoCliente, this.codigoProducto, cantidad);
    }

    // Venta con varios productos (carrito)
    public Venta registrarVenta(String codigoCliente, java.util.List<ItemVenta> items) {
        setCodigoCliente(codigoCliente);
        if (!items.isEmpty()) {
            setCodigoProducto(items.get(items.size() - 1).getProducto().getCodigo());
        }
        return ServicioVentas.registrarVenta(this.codigoCliente, items);
    }

    @Override
    public boolean puedeVerReportes() {
        return false;
    }

    @Override
    public boolean puedeVender() {
        return true;
    }

    @Override
    public String getDescripcion() {
        return getNombre() + " (Trabajador)";
    }
}
