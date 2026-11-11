package Persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexion a MySQL (XAMPP). Cada DAO pide una conexion, la usa y la cierra (try-with-resources).
 * Cambia aqui los datos si tu XAMPP usa otro puerto, usuario o clave.
 */
public final class Conexion {
    private static final String HOST = "localhost";
    private static final String PUERTO = "3307";          // puerto de MySQL en tu XAMPP
    private static final String BASE_DATOS = "bd_pedidos";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";               // XAMPP trae root sin clave

    // serverTimezone=UTC evita el error de "server time zone unrecognized" de XAMPP en Windows
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DATOS
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    private Conexion() {}

    public static Connection obtener() throws SQLException {
        // la propiedad "caja.url" solo se usa para pruebas; normalmente se usa URL
        return DriverManager.getConnection(System.getProperty("caja.url", URL), USUARIO, CLAVE);
    }
}
