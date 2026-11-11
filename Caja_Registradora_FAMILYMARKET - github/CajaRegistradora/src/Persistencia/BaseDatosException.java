package Persistencia;

import java.sql.SQLException;

/**
 * Error al hablar con la base de datos. Hereda de IllegalArgumentException a proposito:
 * los formularios ya capturan esa excepcion y muestran el mensaje al usuario.
 */
public class BaseDatosException extends IllegalArgumentException {

    public BaseDatosException(String mensaje, SQLException causa) {
        super(mensaje + (causa == null ? "" : " (" + causa.getMessage() + ")"), causa);
    }
}
