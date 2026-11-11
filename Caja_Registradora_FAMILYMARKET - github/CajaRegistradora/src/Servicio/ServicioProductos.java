package Servicio;

import DAO.ProductoDAO;
import Modelo.Producto;
import Persistencia.Codigos;
import Persistencia.RepositorioMemoria;
import java.util.ArrayList;
import java.util.List;

/** logica del formulario de productos: el codigo se genera solo (P001, P002...) */
public class ServicioProductos {
    private static final ProductoDAO dao = new ProductoDAO();

    private ServicioProductos() {}

    public static String siguienteCodigo() {
        return Codigos.producto(RepositorioMemoria.siguienteNumeroProducto());
    }

    public static Producto registrar(String nombre, String precio, int stock) {
        double valor = leerPrecio(precio);
        verificarNombreLibre(nombre, null);
        Producto producto = new Producto(siguienteCodigo(), nombre, valor, stock);
        dao.insertar(producto);                       // primero la base de datos...
        RepositorioMemoria.agregarProducto(producto); // ...y si salio bien, la lista en memoria
        return producto;
    }

    // corrige el nombre, el precio y el stock de un producto ya guardado
    public static Producto editar(String codigo, String nombre, String precio, int stock) {
        Producto producto = buscar(codigo);
        if (producto == null) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        double valor = leerPrecio(precio);
        verificarNombreLibre(nombre, producto);
        Producto nuevo = new Producto(producto.getCodigo(), nombre, valor, stock);   // valida los datos nuevos
        nuevo.setAjusteVendidas(producto.getAjusteVendidas());
        dao.actualizar(nuevo);                           // se guarda en la base de datos antes de cambiar el objeto
        producto.setNombre(nombre);
        producto.setPrecio(valor);
        producto.setStock(stock);
        return producto;
    }

    private static void verificarNombreLibre(String nombre, Producto excepto) {
        String nombreLimpio = (nombre == null) ? "" : nombre.trim();
        for (Producto p : RepositorioMemoria.listarProductos()) {
            if (p != excepto && p.getNombre().equalsIgnoreCase(nombreLimpio)) {
                throw new IllegalArgumentException("Ya existe el producto " + p.getCodigo() + " con ese nombre.");
            }
        }
    }

    private static double leerPrecio(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el precio.");
        }
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El precio debe ser un numero (ej. 12.50).");
        }
    }

    public static void eliminar(String codigo) {
        Producto producto = buscar(codigo);
        if (producto == null) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        boolean vendido = false;
        for (Modelo.Venta v : ServicioVentas.listarVentas()) {
            for (Modelo.ItemVenta it : v.getItems()) {
                if (it.getProducto().getCodigo().equals(producto.getCodigo())) {
                    vendido = true;
                }
            }
        }
        if (vendido) {
            throw new IllegalStateException("No se puede eliminar " + producto.getNombre()
                    + ": ya fue vendido en alguna boleta.");
        }
        dao.eliminar(producto.getCodigo());
        RepositorioMemoria.eliminarProducto(producto);
    }

    public static Producto buscar(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Producto p : RepositorioMemoria.listarProductos()) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return p;
            }
        }
        return null;
    }

    public static List<Producto> listar() {
        return RepositorioMemoria.listarProductos();
    }

    public static List<Producto> filtrar(String texto) {
        String t = (texto == null) ? "" : texto.trim().toLowerCase();
        List<Producto> lista = new ArrayList<>();
        for (Producto p : RepositorioMemoria.listarProductos()) {
            if (t.isEmpty() || p.getCodigo().toLowerCase().contains(t)
                    || p.getNombre().toLowerCase().contains(t)) {
                lista.add(p);
            }
        }
        return lista;
    }
}
