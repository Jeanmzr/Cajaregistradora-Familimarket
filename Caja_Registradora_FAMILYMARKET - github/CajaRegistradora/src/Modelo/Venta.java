package Modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Una venta ya registrada = una boleta. Puede tener varios productos (items).
public class Venta {
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final int MAX_PRODUCTOS = 20;
    private static int contador = 0;

    private final String codigo;          // numero de boleta: B001-00001
    private final LocalDateTime fecha;
    private final String atendidoPor;
    private Cliente cliente;
    private List<ItemVenta> items;
    private LocalDateTime fechaEdicion;   // null = la boleta nunca se edito

    public Venta(Cliente cliente, Producto producto, int cantidad, String atendidoPor) {
        this(cliente, List.of(new ItemVenta(producto, cantidad)), atendidoPor, LocalDateTime.now());
    }

    public Venta(Cliente cliente, List<ItemVenta> items, String atendidoPor) {
        this(cliente, items, atendidoPor, LocalDateTime.now());
    }

    public Venta(Cliente cliente, List<ItemVenta> items, String atendidoPor, LocalDateTime fecha) {
        validar(cliente, items);
        this.cliente = cliente;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.atendidoPor = atendidoPor;
        this.fecha = fecha;
        contador++;
        this.codigo = "B001-" + String.format("%05d", contador);
    }

    // boleta leida de la base de datos: conserva su codigo, su fecha y su fecha de edicion
    public Venta(String codigo, Cliente cliente, List<ItemVenta> items, String atendidoPor,
            LocalDateTime fecha, LocalDateTime fechaEdicion) {
        validar(cliente, items);
        this.cliente = cliente;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.atendidoPor = atendidoPor;
        this.fecha = fecha;
        this.fechaEdicion = fechaEdicion;
        this.codigo = codigo;
        int numero = Integer.parseInt(codigo.substring(codigo.indexOf('-') + 1));
        if (numero > contador) {
            contador = numero;      // la siguiente boleta continua la numeracion
        }
    }

    // reglas que debe cumplir toda boleta (al crearla y al editarla)
    public static void validar(Cliente cliente, List<ItemVenta> items) {
        if (cliente == null) {
            throw new IllegalArgumentException("Seleccione un cliente.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la venta.");
        }
        if (items.size() > MAX_PRODUCTOS) {
            throw new IllegalArgumentException("Una boleta puede tener maximo " + MAX_PRODUCTOS + " productos.");
        }
    }

    // corrige el cliente y/o los productos de una boleta ya registrada (conserva su numero y su fecha)
    public void editar(Cliente nuevoCliente, List<ItemVenta> nuevosItems) {
        validar(nuevoCliente, nuevosItems);
        this.cliente = nuevoCliente;
        this.items = Collections.unmodifiableList(new ArrayList<>(nuevosItems));
        this.fechaEdicion = LocalDateTime.now();
    }

    public boolean isEditada() { return fechaEdicion != null; }
    public String getFechaEdicion() { return fechaEdicion == null ? "" : fechaEdicion.format(FORMATO_FECHA); }

    public String getCodigo() { return codigo; }
    public String getFecha() { return fecha.format(FORMATO_FECHA); }
    public LocalDateTime getFechaHora() { return fecha; }
    public LocalDate getDia() { return fecha.toLocalDate(); }
    public String getAtendidoPor() { return atendidoPor; }
    public Cliente getCliente() { return cliente; }
    public List<ItemVenta> getItems() { return items; }

    public int getUnidades() {
        int suma = 0;
        for (ItemVenta it : items) {
            suma += it.getCantidad();
        }
        return suma;
    }

    // "Coca-Cola 1.5 L" o "Fideos Don Vittorio 500 g (+3 mas)"
    public String descripcionProductos() {
        String primero = items.get(0).getProducto().getNombre();
        return items.size() == 1 ? primero : primero + " (+" + (items.size() - 1) + " más)";
    }

    public double subtotal() {
        double suma = 0;
        for (ItemVenta it : items) {
            suma += it.subtotal();
        }
        return Moneda.redondear(suma);
    }

    public double descuento() {
        return Moneda.redondear(subtotal() * cliente.getPorcentajeDescuento());
    }

    public double total() {
        return Moneda.redondear(subtotal() - descuento());
    }

    // fila para mostrar en un JTable
    public Object[] RegistrarDatos(int num) {
        Object[] fila = {num, codigo, getFecha(), cliente.getCodigo(), cliente.getNombre(),
            descripcionProductos(), getUnidades(),
            Moneda.formato(subtotal()), Moneda.formato(descuento()), Moneda.formato(total())};
        return fila;
    }
}
