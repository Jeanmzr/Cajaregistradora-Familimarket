package Servicio;

import DAO.ProductoDAO;
import DAO.VentaDAO;
import Modelo.Cliente;
import Modelo.ItemVenta;
import Modelo.Moneda;
import Modelo.Producto;
import Modelo.Usuario;
import Modelo.Venta;
import Persistencia.RepositorioMemoria;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** capa de logica de las ventas y de los reportes */
public class ServicioVentas {

    private static final VentaDAO ventaDAO = new VentaDAO();
    private static final ProductoDAO productoDAO = new ProductoDAO();

    private ServicioVentas() {}

    // ---------- registrar (se usa el CODIGO del cliente y el CODIGO de cada producto) ----------
    public static Venta registrarVenta(String codigoCliente, List<ItemVenta> items) {
        if (codigoCliente == null || codigoCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el codigo del cliente.");
        }
        Cliente cliente = ServicioClientes.buscar(codigoCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("No existe un cliente con el codigo " + codigoCliente.trim() + ".");
        }
        Usuario sesion = ServicioSesion.getUsuarioActual();
        String atendidoPor = (sesion == null) ? "-" : sesion.getNombre();

        Venta.validar(cliente, items);
        verificarStock(items, null);                  // no se vende mas de lo que hay en stock
        Venta venta = new Venta(cliente, items, atendidoPor);
        ventaDAO.insertar(venta);       // transaccion: boleta + detalle + baja de stock en la BD
        for (ItemVenta it : items) {
            it.getProducto().descontar(it.getCantidad());      // la venta baja el stock
        }
        RepositorioMemoria.agregarVenta(venta);
        return venta;
    }

    // Revisa que el stock alcance para todos los productos de la lista.
    // Si se esta editando una venta, sus unidades actuales cuentan como disponibles.
    public static void verificarStock(List<ItemVenta> items, Venta ventaEditada) {
        Map<String, Integer> pedido = new LinkedHashMap<>();
        Map<String, Producto> productos = new LinkedHashMap<>();
        for (ItemVenta it : items) {
            pedido.merge(it.getProducto().getCodigo(), it.getCantidad(), Integer::sum);
            productos.put(it.getProducto().getCodigo(), it.getProducto());
        }
        for (Map.Entry<String, Integer> e : pedido.entrySet()) {
            Producto p = productos.get(e.getKey());
            int disponible = p.getStock() + unidadesEnVenta(ventaEditada, p);
            if (e.getValue() > disponible) {
                throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre()
                        + ": disponible " + disponible + ", pedido " + e.getValue() + ".");
            }
        }
    }

    // unidades de un producto que ya tiene una venta (0 si no la tiene o la venta es null)
    public static int unidadesEnVenta(Venta venta, Producto producto) {
        int n = 0;
        if (venta != null) {
            for (ItemVenta it : venta.getItems()) {
                if (it.getProducto() == producto) {
                    n += it.getCantidad();
                }
            }
        }
        return n;
    }

    // Corrige una venta ya registrada (cliente y/o productos). El stock se ajusta solo:
    // las unidades viejas vuelven al stock y las nuevas se descuentan.
    public static Venta editarVenta(Venta venta, String codigoCliente, List<ItemVenta> nuevos) {
        if (!RepositorioMemoria.listarVentas().contains(venta)) {
            throw new IllegalArgumentException("La venta ya no existe.");
        }
        if (codigoCliente == null || codigoCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el codigo del cliente.");
        }
        Cliente cliente = ServicioClientes.buscar(codigoCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("No existe un cliente con el codigo " + codigoCliente.trim() + ".");
        }
        Venta.validar(cliente, nuevos);
        verificarStock(nuevos, venta);
        ventaDAO.actualizar(venta, cliente, nuevos);     // transaccion en la BD (usa los items anteriores de la venta)
        for (ItemVenta viejo : venta.getItems()) {
            viejo.getProducto().reponer(viejo.getCantidad());
        }
        for (ItemVenta nuevo : nuevos) {
            nuevo.getProducto().descontar(nuevo.getCantidad());
        }
        venta.editar(cliente, nuevos);
        return venta;
    }

    // venta de un solo producto
    public static Venta registrarVenta(String codigoCliente, String codigoProducto, int cantidad) {
        if (codigoProducto == null || codigoProducto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el codigo del producto.");
        }
        Producto producto = ServicioProductos.buscar(codigoProducto);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con el codigo " + codigoProducto.trim() + ".");
        }
        List<ItemVenta> items = new ArrayList<>();
        items.add(new ItemVenta(producto, cantidad));
        return registrarVenta(codigoCliente, items);
    }

    // al eliminar una venta, sus unidades vuelven al stock
    public static void eliminarVenta(Venta venta) {
        ventaDAO.eliminar(venta);       // transaccion en la BD: devuelve el stock y borra la boleta
        for (ItemVenta it : venta.getItems()) {
            it.getProducto().reponer(it.getCantidad());
        }
        RepositorioMemoria.eliminarVenta(venta);
    }

    public static List<Venta> listarVentas() {
        return RepositorioMemoria.listarVentas();
    }

    public static Venta obtenerVenta(int posicion) {
        return RepositorioMemoria.obtenerVenta(posicion);
    }

    public static int cantidadVentas() {
        return RepositorioMemoria.listarVentas().size();
    }

    // ---------- reportes: por fecha y/o por cliente (null = sin filtro) ----------
    public static List<Venta> filtrar(LocalDate desde, LocalDate hasta, String codigoCliente) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser mayor que 'Hasta'.");
        }
        List<Venta> lista = new ArrayList<>();
        for (Venta v : RepositorioMemoria.listarVentas()) {
            boolean okDesde = desde == null || !v.getDia().isBefore(desde);
            boolean okHasta = hasta == null || !v.getDia().isAfter(hasta);
            boolean okCliente = codigoCliente == null || v.getCliente().getCodigo().equalsIgnoreCase(codigoCliente);
            if (okDesde && okHasta && okCliente) {
                lista.add(v);
            }
        }
        return lista;
    }

    // ---------- totales (de todas las ventas o de una lista filtrada) ----------
    public static double totalDe(List<Venta> ventas) {
        double suma = 0;
        for (Venta venta : ventas) {
            suma += venta.total();
        }
        return Moneda.redondear(suma);
    }

    public static double descuentosDe(List<Venta> ventas) {
        double suma = 0;
        for (Venta venta : ventas) {
            suma += venta.descuento();
        }
        return Moneda.redondear(suma);
    }

    public static int unidadesDe(List<Venta> ventas) {
        int suma = 0;
        for (Venta venta : ventas) {
            suma += venta.getUnidades();
        }
        return suma;
    }

    public static double totalIngresado() {
        return totalDe(RepositorioMemoria.listarVentas());
    }

    public static double totalDescuentos() {
        return descuentosDe(RepositorioMemoria.listarVentas());
    }

    public static int unidadesVendidas() {
        return unidadesDe(RepositorioMemoria.listarVentas());
    }

    // ---------- modulo "Producto Vendido": unidades vendidas editables ----------
    // Unidades que muestra el modulo = ventas registradas + la correccion manual (nunca menos de 0).
    private static List<Object[]> filasAjustadas() {
        Map<Producto, Integer> unidades = new LinkedHashMap<>();
        Map<Producto, Double> ingresos = new LinkedHashMap<>();
        for (Venta venta : RepositorioMemoria.listarVentas()) {
            double factor = 1 - venta.getCliente().getPorcentajeDescuento();
            for (ItemVenta item : venta.getItems()) {
                unidades.merge(item.getProducto(), item.getCantidad(), Integer::sum);
                ingresos.merge(item.getProducto(), item.subtotal() * factor, Double::sum);
            }
        }
        for (Producto p : RepositorioMemoria.listarProductos()) {
            if (p.getAjusteVendidas() != 0) {
                unidades.putIfAbsent(p, 0);
                ingresos.putIfAbsent(p, 0.0);
            }
        }
        List<Object[]> filas = new ArrayList<>();
        for (Producto p : unidades.keySet()) {
            int total = Math.max(0, unidades.get(p) + p.getAjusteVendidas());
            if (total > 0) {
                double dinero = Math.max(0, ingresos.get(p) + p.getAjusteVendidas() * p.getPrecio());
                filas.add(new Object[]{p, total, Moneda.redondear(dinero)});
            }
        }
        return filas;
    }

    // filas: codigo, nombre, unidades vendidas, dinero ingresado, stock actual
    public static List<Object[]> resumenConAjustes() {
        List<Object[]> filas = new ArrayList<>();
        for (Object[] f : filasAjustadas()) {
            Producto p = (Producto) f[0];
            filas.add(new Object[]{p.getCodigo(), p.getNombre(), f[1], Moneda.formato((Double) f[2]), p.getStock()});
        }
        return filas;
    }

    public static int unidadesVendidasAjustadas() {
        int n = 0;
        for (Object[] f : filasAjustadas()) {
            n += (Integer) f[1];
        }
        return n;
    }

    public static double totalIngresadoAjustado() {
        double suma = 0;
        for (Object[] f : filasAjustadas()) {
            suma += (Double) f[2];
        }
        return Moneda.redondear(suma);
    }

    // unidades que muestra el modulo para un producto
    public static int unidadesVendidasDe(Producto producto) {
        for (Object[] f : filasAjustadas()) {
            if (f[0] == producto) {
                return (Integer) f[1];
            }
        }
        return 0;
    }

    // Cambia las unidades vendidas de un producto. Si bajan, el stock sube; si suben, el stock baja.
    public static void editarUnidadesVendidas(String codigoProducto, int nuevas) {
        Producto p = ServicioProductos.buscar(codigoProducto);
        if (p == null) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        if (nuevas < 0) {
            throw new IllegalArgumentException("Las unidades vendidas no pueden ser negativas.");
        }
        int actual = unidadesVendidasDe(p);
        int diferencia = nuevas - actual;                 // positivo = se vendio mas
        if (diferencia > p.getStock()) {
            throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre()
                    + ": solo quedan " + p.getStock() + " unidades.");
        }
        int stockAntes = p.getStock();
        int ajusteAntes = p.getAjusteVendidas();
        p.setStock(p.getStock() - diferencia);            // lanza error si pasaria el limite del stock
        int vendidasPorVentas = 0;
        for (Venta venta : RepositorioMemoria.listarVentas()) {
            vendidasPorVentas += unidadesEnVenta(venta, p);
        }
        p.setAjusteVendidas(nuevas - vendidasPorVentas);
        try {
            productoDAO.actualizar(p);                    // guarda stock y ajuste en la BD
        } catch (RuntimeException ex) {
            p.setStock(stockAntes);                       // si la BD falla, se deshace el cambio en memoria
            p.setAjusteVendidas(ajusteAntes);
            throw ex;
        }
    }

    public static List<Object[]> resumenPorProducto() {
        return resumenPorProducto(RepositorioMemoria.listarVentas());
    }

    // Una fila por producto: codigo, nombre, unidades vendidas, dinero ingresado.
    public static List<Object[]> resumenPorProducto(List<Venta> ventas) {
        Map<String, Producto> productos = new LinkedHashMap<>();
        Map<String, Integer> unidades = new LinkedHashMap<>();
        Map<String, Double> ingresos = new LinkedHashMap<>();

        for (Venta venta : ventas) {
            double factor = 1 - venta.getCliente().getPorcentajeDescuento();
            for (ItemVenta item : venta.getItems()) {
                String clave = item.getProducto().getCodigo();
                productos.putIfAbsent(clave, item.getProducto());
                unidades.merge(clave, item.getCantidad(), Integer::sum);
                ingresos.merge(clave, item.subtotal() * factor, Double::sum);
            }
        }

        List<Object[]> filas = new ArrayList<>();
        for (String clave : productos.keySet()) {
            filas.add(new Object[]{clave, productos.get(clave).getNombre(), unidades.get(clave),
                Moneda.formato(ingresos.get(clave))});
        }
        return filas;
    }
}
