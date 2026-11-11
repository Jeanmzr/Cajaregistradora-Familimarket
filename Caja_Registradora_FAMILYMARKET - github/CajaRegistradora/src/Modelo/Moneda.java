package Modelo;

import java.util.Locale;

// Utilidades de dinero 
public class Moneda {
    public static final String SIMBOLO = "S/ ";

    private Moneda() {}

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static String formato(double valor) {
        return SIMBOLO + String.format(Locale.US, "%,.2f", valor);
    }
}
