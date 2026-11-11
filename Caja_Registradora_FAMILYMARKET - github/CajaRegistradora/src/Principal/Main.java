package Principal;

import Persistencia.BaseDatosException;
import Persistencia.RepositorioMemoria;
import Servicio.DatosDePrueba;
import Vista.FormLogin;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        try {
            RepositorioMemoria.cargarDesdeBD();      // lee usuarios, productos, clientes y ventas de MySQL
        } catch (BaseDatosException ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar con la base de datos.\nRevisa que XAMPP (MySQL, puerto 3307) este encendido.\n\n"
                    + ex.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (RepositorioMemoria.listarUsuarios().isEmpty()) {
            DatosDePrueba.cargar();                  // solo la primera vez, cuando la BD esta vacia
        }
        FormLogin formulario = new FormLogin();
        formulario.setVisible(true);
    }
}
