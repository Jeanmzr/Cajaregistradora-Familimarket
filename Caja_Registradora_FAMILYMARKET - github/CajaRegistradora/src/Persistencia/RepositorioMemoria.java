package Persistencia;

import DAO.ClienteDAO;
import DAO.ProductoDAO;
import DAO.UsuarioDAO;
import DAO.VentaDAO;
import Modelo.Cliente;
import Modelo.Producto;
import Modelo.Usuario;
import Modelo.Venta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Listas en memoria (cache) de los objetos del sistema. Al iniciar se llenan desde MySQL con
 * cargarDesdeBD(); despues cada cambio se guarda en la base de datos a traves de los DAO.
 */
public class RepositorioMemoria {
    private static final ArrayList<Venta> ventas = new ArrayList<>();
    private static final ArrayList<Usuario> usuarios = new ArrayList<>();
    private static final ArrayList<Cliente> clientes = new ArrayList<>();
    private static final ArrayList<Producto> productos = new ArrayList<>();

    private static int ultimoCliente = 0;     // numero del ultimo codigo C00X usado
    private static int ultimoProducto = 0;     // numero del ultimo codigo P00X usado

    private RepositorioMemoria() {}

    // Lee todo de la base de datos (usuarios, productos, clientes y ventas)
    public static void cargarDesdeBD() {
        List<Usuario> usuariosBD = new UsuarioDAO().listar();
        List<Producto> productosBD = new ProductoDAO().listar();
        List<Cliente> clientesBD = new ClienteDAO().listar();
        List<Venta> ventasBD = new VentaDAO().listar(clientesBD, productosBD);

        usuarios.clear();
        usuarios.addAll(usuariosBD);
        productos.clear();
        productos.addAll(productosBD);
        clientes.clear();
        clientes.addAll(clientesBD);
        ventas.clear();
        ventas.addAll(ventasBD);

        ultimoProducto = 0;
        for (Producto p : productosBD) {
            ultimoProducto = Math.max(ultimoProducto, Codigos.numero(p.getCodigo()));
        }
        ultimoCliente = 0;
        for (Cliente c : clientesBD) {
            ultimoCliente = Math.max(ultimoCliente, Codigos.numero(c.getCodigo()));
        }
    }

    public static int siguienteNumeroCliente() {
        return ultimoCliente + 1;
    }

    public static int siguienteNumeroProducto() {
        return ultimoProducto + 1;
    }

    // ----- ventas -----
    public static void agregarVenta(Venta venta) {
        ventas.add(venta);
    }

    public static void eliminarVenta(Venta venta) {
        ventas.remove(venta);
    }

    public static List<Venta> listarVentas() {
        return Collections.unmodifiableList(ventas);   // solo lectura
    }

    public static Venta obtenerVenta(int posicion) {
        return ventas.get(posicion);
    }

    // ----- usuarios -----
    public static void agregarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public static void eliminarUsuario(Usuario usuario) {
        usuarios.remove(usuario);
    }

    // cambia una cuenta por otra en la misma posicion (cuando se edita el rol)
    public static void reemplazarUsuario(Usuario viejo, Usuario nuevo) {
        int posicion = usuarios.indexOf(viejo);
        if (posicion >= 0) {
            usuarios.set(posicion, nuevo);
        }
    }

    public static List<Usuario> listarUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    // ----- clientes -----
    public static void agregarCliente(Cliente cliente) {
        clientes.add(cliente);
        ultimoCliente = Math.max(ultimoCliente, Codigos.numero(cliente.getCodigo()));
    }

    public static void eliminarCliente(Cliente cliente) {
        clientes.remove(cliente);
    }

    public static List<Cliente> listarClientes() {
        return Collections.unmodifiableList(clientes);
    }

    // ----- productos -----
    public static void agregarProducto(Producto producto) {
        productos.add(producto);
        ultimoProducto = Math.max(ultimoProducto, Codigos.numero(producto.getCodigo()));
    }

    public static void eliminarProducto(Producto producto) {
        productos.remove(producto);
    }

    public static List<Producto> listarProductos() {
        return Collections.unmodifiableList(productos);
    }
}
