package Persistencia;

/**
 * Convierte entre el ID numerico de la base de datos y los codigos que ve el usuario:
 * 3 -> "P003" (producto), "C003" (cliente), "B001-00003" (boleta).
 */
public final class Codigos {

    private Codigos() {}

    public static String producto(int id) { return "P" + String.format("%03d", id); }

    public static String cliente(int id) { return "C" + String.format("%03d", id); }

    public static String boleta(int id) { return "B001-" + String.format("%05d", id); }

    // "P003" -> 3   |   "B001-00007" -> 7
    public static int numero(String codigo) {
        String digitos = codigo.substring(codigo.lastIndexOf('-') + 1).replaceAll("\\D", "");
        return Integer.parseInt(digitos);
    }
}
