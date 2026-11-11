package Modelo;

public class Producto {
    public static final int STOCK_MAXIMO = 10000;     // limite de stock de cada producto

    private final String codigo;
    private String nombre;
    private double precio;
    private int stock;               // unidades disponibles: baja con cada venta
    private int ajusteVendidas;      // correccion manual de "unidades vendidas" (modulo Producto Vendido)

    public Producto(String codigo, String nombre, double precio, int stock) {
        this.codigo = codigo;
        setNombre(nombre);
        setPrecio(precio);
        setStock(stock);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre del producto.");
        }
        this.nombre = nombre.trim();
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0 || stock > STOCK_MAXIMO) {
            throw new IllegalArgumentException("El stock debe estar entre 0 y " + STOCK_MAXIMO + ".");
        }
        this.stock = stock;
    }

    public boolean hayStock(int cantidad) {
        return cantidad <= stock;
    }

    // la venta baja el stock
    public void descontar(int cantidad) {
        if (cantidad > stock) {
            throw new IllegalArgumentException("Stock insuficiente de " + nombre + ": disponible " + stock + ".");
        }
        stock -= cantidad;
    }

    // al eliminar o reducir una venta el stock vuelve a subir
    public void reponer(int cantidad) {
        stock = Math.min(STOCK_MAXIMO, stock + cantidad);
    }

    public int getAjusteVendidas() {
        return ajusteVendidas;
    }

    public void setAjusteVendidas(int ajusteVendidas) {
        this.ajusteVendidas = ajusteVendidas;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
