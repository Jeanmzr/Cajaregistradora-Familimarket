package Reporte;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Creador de PDF minimo (sin librerias externas). Usa las fuentes Helvetica y
 * Helvetica-Bold que todo lector de PDF trae. Las coordenadas tienen el origen
 * ARRIBA a la izquierda (como en Swing) y estan en puntos (A4 = 595 x 842).
 */
public class GeneradorPDF {
    public static final float A4_ANCHO = 595f;
    public static final float A4_ALTO = 842f;

    private final float ancho;
    private final float alto;
    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder actual;

    public GeneradorPDF(float ancho, float alto) {
        this.ancho = ancho;
        this.alto = alto;
    }

    public void nuevaPagina() {
        actual = new StringBuilder();
        paginas.add(actual);
    }

    public int cantidadPaginas() {
        return paginas.size();
    }

    // ---------------- dibujo ----------------
    public void texto(String s, float x, float y, float tam, boolean negrita, Color color) {
        if (s == null || s.isEmpty()) {
            return;
        }
        fijarColor(color, true);
        actual.append("BT /F").append(negrita ? 2 : 1).append(' ').append(n(tam)).append(" Tf ")
                .append(n(x)).append(' ').append(n(alto - y)).append(" Td (")
                .append(escapar(s)).append(") Tj ET\n");
    }

    public void textoDerecha(String s, float xDerecha, float y, float tam, boolean negrita, Color color) {
        texto(s, xDerecha - ancho(s, tam, negrita), y, tam, negrita, color);
    }

    public void textoCentro(String s, float xCentro, float y, float tam, boolean negrita, Color color) {
        texto(s, xCentro - ancho(s, tam, negrita) / 2f, y, tam, negrita, color);
    }

    public void linea(float x1, float y1, float x2, float y2, float grosor, Color color) {
        fijarColor(color, false);
        actual.append(n(grosor)).append(" w ").append(n(x1)).append(' ').append(n(alto - y1)).append(" m ")
                .append(n(x2)).append(' ').append(n(alto - y2)).append(" l S\n");
    }

    // relleno y/o borde (null = sin relleno / sin borde)
    public void rect(float x, float y, float w, float h, Color relleno, Color borde, float grosor) {
        if (relleno != null) {
            fijarColor(relleno, true);
        }
        if (borde != null) {
            fijarColor(borde, false);
            actual.append(n(grosor)).append(" w\n");
        }
        actual.append(n(x)).append(' ').append(n(alto - y - h)).append(' ').append(n(w)).append(' ')
                .append(n(h)).append(" re ");
        if (relleno != null && borde != null) {
            actual.append("B\n");
        } else if (relleno != null) {
            actual.append("f\n");
        } else if (borde != null) {
            actual.append("S\n");
        } else {
            actual.append("n\n");
        }
    }

    // ---------------- utilidades de texto ----------------
    public static float ancho(String s, float tam, boolean negrita) {
        int[] tabla = negrita ? ANCHO_NEGRITA : ANCHO_NORMAL;
        String base = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        float suma = 0;
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            suma += (c >= 32 && c <= 126) ? tabla[c - 32] : 556;
        }
        return suma * tam / 1000f;
    }

    // corta el texto con "..." si no entra en el ancho dado
    public static String recortar(String s, float maxAncho, float tam, boolean negrita) {
        if (ancho(s, tam, negrita) <= maxAncho) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && ancho(t + "...", tam, negrita) > maxAncho) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "...";
    }

    // parte un texto largo en varias lineas
    public static List<String> ajustar(String s, float maxAncho, float tam, boolean negrita) {
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : s.split(" ")) {
            String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
            if (ancho(prueba, tam, negrita) > maxAncho && linea.length() > 0) {
                lineas.add(linea.toString());
                linea = new StringBuilder(palabra);
            } else {
                linea = new StringBuilder(prueba);
            }
        }
        if (linea.length() > 0) {
            lineas.add(linea.toString());
        }
        return lineas;
    }

    // ---------------- guardar ----------------
    public void guardar(File archivo) throws IOException {
        if (paginas.isEmpty()) {
            throw new IOException("El PDF no tiene paginas.");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>();
        escribir(out, "%PDF-1.4\n");

        int total = 4 + paginas.size() * 2;     // objetos: catalogo, paginas, 2 fuentes, y (contenido+pagina) por hoja
        objeto(out, posiciones, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < paginas.size(); i++) {
            kids.append(6 + i * 2).append(" 0 R ");
        }
        objeto(out, posiciones, 2, "<< /Type /Pages /Kids [ " + kids + "] /Count " + paginas.size() + " >>");
        objeto(out, posiciones, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objeto(out, posiciones, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");

        for (int i = 0; i < paginas.size(); i++) {
            byte[] contenido = paginas.get(i).toString().getBytes("windows-1252");
            int idContenido = 5 + i * 2;
            int idPagina = 6 + i * 2;

            posiciones.add(out.size());
            escribir(out, idContenido + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
            out.write(contenido);
            escribir(out, "\nendstream\nendobj\n");

            objeto(out, posiciones, idPagina, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + n(ancho) + " " + n(alto)
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + idContenido + " 0 R >>");
        }

        int inicioXref = out.size();
        escribir(out, "xref\n0 " + (total + 1) + "\n0000000000 65535 f \n");
        for (int pos : posiciones) {
            escribir(out, String.format("%010d 00000 n \n", pos));
        }
        escribir(out, "trailer\n<< /Size " + (total + 1) + " /Root 1 0 R >>\nstartxref\n" + inicioXref + "\n%%EOF\n");

        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            out.writeTo(fos);
        }
    }

    // ---------------- internos ----------------
    private void objeto(ByteArrayOutputStream out, List<Integer> posiciones, int id, String cuerpo) throws IOException {
        posiciones.add(out.size());
        escribir(out, id + " 0 obj\n" + cuerpo + "\nendobj\n");
    }

    private void escribir(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes("windows-1252"));
    }

    private void fijarColor(Color c, boolean relleno) {
        actual.append(n(c.getRed() / 255f)).append(' ').append(n(c.getGreen() / 255f)).append(' ')
                .append(n(c.getBlue() / 255f)).append(relleno ? " rg\n" : " RG\n");
    }

    private static String n(float f) {
        return String.format(Locale.US, "%.2f", f);
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    // anchos de Helvetica de los caracteres 32 (espacio) a 126 (~), en milesimas de punto
    private static final int[] ANCHO_NORMAL = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584};
    private static final int[] ANCHO_NEGRITA = {
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584};
}
