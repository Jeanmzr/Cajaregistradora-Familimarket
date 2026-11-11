package Servicio;

/** Datos iniciales para probar el sistema (borra las lineas que no quieras). */
public class DatosDePrueba {

    private DatosDePrueba() {}

    public static void cargar() {
        // usuarios:  trabajador / 1234   encargado / admin123   admin / admin123
        ServicioUsuarios.registrar("TRABAJADOR", "empleado", "1234", "Empleado de caja");
        ServicioUsuarios.registrar("ENCARGADO", "encargado", "admin123", "Encargado de tienda");
        ServicioUsuarios.registrar("ADMINISTRADOR", "admin", "admin123", "Administrador general");

        ServicioClientes.registrar("Maria Lopez Rojas", 34, "45871236", "987654321");
        ServicioClientes.registrar("Jose Ramirez Soto", 67, "08123456", "956123478");

        ServicioProductos.registrar("Fideos Don Vittorio 500 g", "4.00", 50);
        ServicioProductos.registrar("Coca-Cola 1.5 L", "7.00", 40);
        ServicioProductos.registrar("Agua San Luis 625 ml", "1.50", 60);
        ServicioProductos.registrar("Galletas Oreo", "3.00", 30);

    }
}
