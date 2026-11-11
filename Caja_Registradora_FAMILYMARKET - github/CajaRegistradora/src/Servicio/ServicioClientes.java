package Servicio;

import DAO.ClienteDAO;
import Modelo.Cliente;
import Persistencia.Codigos;
import Persistencia.RepositorioMemoria;
import java.util.ArrayList;
import java.util.List;

/** logica del formulario de clientes: el codigo se genera solo (C001, C002...) */
public class ServicioClientes {
    private static final ClienteDAO dao = new ClienteDAO();

    private ServicioClientes() {}

    public static String siguienteCodigo() {
        return Codigos.cliente(RepositorioMemoria.siguienteNumeroCliente());
    }

    public static Cliente registrar(String nombre, int edad, String dni, String telefono) {
        String dniLimpio = (dni == null) ? "" : dni.trim();
        for (Cliente c : RepositorioMemoria.listarClientes()) {
            if (c.getDni().equals(dniLimpio)) {
                throw new IllegalArgumentException("Ya existe un cliente con ese DNI (" + c.getCodigo() + ").");
            }
        }
        Cliente cliente = new Cliente(siguienteCodigo(), nombre, edad, dni, telefono);
        dao.insertar(cliente);                      // primero la base de datos...
        RepositorioMemoria.agregarCliente(cliente); // ...y si salio bien, la lista en memoria
        return cliente;
    }

    // corrige los datos de un cliente ya guardado (conserva su codigo y sus boletas)
    public static Cliente editar(String codigo, String nombre, int edad, String dni, String telefono) {
        Cliente cliente = buscar(codigo);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no existe.");
        }
        String dniLimpio = (dni == null) ? "" : dni.trim();
        for (Cliente c : RepositorioMemoria.listarClientes()) {
            if (c != cliente && c.getDni().equals(dniLimpio)) {
                throw new IllegalArgumentException("Ya existe un cliente con ese DNI (" + c.getCodigo() + ").");
            }
        }
        Cliente nuevo = new Cliente(cliente.getCodigo(), nombre, edad, dni, telefono);  // valida los datos nuevos
        dao.actualizar(nuevo);                      // se guarda en la base de datos antes de cambiar el objeto
        cliente.setNombre(nombre);
        cliente.setEdad(edad);
        cliente.setDni(dni);
        cliente.setTelefono(telefono);
        return cliente;
    }

    public static void eliminar(String codigo) {
        Cliente cliente = buscar(codigo);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no existe.");
        }
        if (!ServicioVentas.filtrar(null, null, codigo).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar a " + cliente.getNombre()
                    + ": tiene ventas (boletas) registradas.");
        }
        dao.eliminar(cliente.getCodigo());
        RepositorioMemoria.eliminarCliente(cliente);
    }

    public static Cliente buscar(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Cliente c : RepositorioMemoria.listarClientes()) {
            if (c.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return c;
            }
        }
        return null;
    }

    public static List<Cliente> listar() {
        return RepositorioMemoria.listarClientes();
    }

    // filtra por codigo, nombre, DNI o telefono
    public static List<Cliente> filtrar(String texto) {
        String t = (texto == null) ? "" : texto.trim().toLowerCase();
        List<Cliente> lista = new ArrayList<>();
        for (Cliente c : RepositorioMemoria.listarClientes()) {
            if (t.isEmpty() || c.getCodigo().toLowerCase().contains(t)
                    || c.getNombre().toLowerCase().contains(t)
                    || c.getDni().contains(t) || c.getTelefono().contains(t)) {
                lista.add(c);
            }
        }
        return lista;
    }
}
