package Modelo;

// Una linea de la boleta: un producto y cuantas unidades se llevan.
// Guarda el precio del momento de la venta: si luego se edita el precio del producto, la boleta no cambia.
public class ItemVenta {
    private final Producto producto;
    private final double precioUnitario;
    private int cantidad;

    public ItemVenta(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("Seleccione un producto.");
        }
        this.producto = producto;
        this.precioUnitario = producto.getPrecio();
        this.cantidad = validar(cantidad);
    }

    // linea leida de la base de datos: conserva el precio al que se vendio
    public ItemVenta(Producto producto, double precioUnitario, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("Seleccione un producto.");
        }
        this.producto = producto;
        this.precioUnitario = precioUnitario;
        this.cantidad = validar(cantidad);
    }

    // copia de una linea (para editar una venta sin tocar la original hasta guardar)
    public ItemVenta(ItemVenta otro) {
        this.producto = otro.producto;
        this.precioUnitario = otro.precioUnitario;
        this.cantidad = otro.cantidad;
    }

    private static int validar(int cantidad) {
        if (cantidad < 1 || cantidad > 1000) {
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 1000.");
        }
        return cantidad;
    }

    public Producto getProducto() { return producto; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }

    public void setCantidad(int cantidad) {
        this.cantidad = validar(cantidad);
    }

    public void sumar(int mas) {
        this.cantidad = validar(cantidad + mas);
    }

    public double subtotal() {
        return Moneda.redondear(precioUnitario * cantidad);
    }
}
