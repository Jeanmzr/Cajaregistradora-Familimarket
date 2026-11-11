package Reporte;

/** Convierte un monto a letras para la boleta: 120.50 -> CIENTO VEINTE CON 50/100 SOLES */
public class NumeroALetras {
    private static final String[] MENORES = {"CERO", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE",
        "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE",
        "DIECIOCHO", "DIECINUEVE", "VEINTE", "VEINTIUNO", "VEINTIDÓS", "VEINTITRÉS", "VEINTICUATRO",
        "VEINTICINCO", "VEINTISÉIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"};
    private static final String[] DECENAS = {"", "", "", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA",
        "SETENTA", "OCHENTA", "NOVENTA"};
    private static final String[] CENTENAS = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS",
        "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

    private NumeroALetras() {}

    public static String convertir(double monto) {
        long entero = (long) monto;
        int centimos = (int) Math.round((monto - entero) * 100);
        if (centimos == 100) {
            entero++;
            centimos = 0;
        }
        return letras(entero) + " CON " + String.format("%02d", centimos) + "/100 SOLES";
    }

    private static String letras(long n) {
        if (n >= 1_000_000) {
            long m = n / 1_000_000;
            String s = (m == 1) ? "UN MILLÓN" : apocope(letras(m)) + " MILLONES";
            return (n % 1_000_000 == 0) ? s : s + " " + letras(n % 1_000_000);
        }
        if (n >= 1000) {
            long m = n / 1000;
            String s = (m == 1) ? "MIL" : apocope(letras(m)) + " MIL";
            return (n % 1000 == 0) ? s : s + " " + letras(n % 1000);
        }
        if (n >= 100) {
            if (n == 100) {
                return "CIEN";
            }
            return CENTENAS[(int) (n / 100)] + (n % 100 == 0 ? "" : " " + letras(n % 100));
        }
        if (n >= 30) {
            return DECENAS[(int) (n / 10)] + (n % 10 == 0 ? "" : " Y " + MENORES[(int) (n % 10)]);
        }
        return MENORES[(int) n];
    }

    // "VEINTIUNO MIL" -> "VEINTIÚN MIL" ; "UNO" -> "UN"
    private static String apocope(String s) {
        if (s.endsWith("VEINTIUNO")) {
            return s.substring(0, s.length() - 9) + "VEINTIÚN";
        }
        if (s.endsWith("UNO")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }
}
