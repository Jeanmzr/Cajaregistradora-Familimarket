package Reporte;

import Modelo.Cliente;
import Modelo.ItemVenta;
import Modelo.Moneda;
import Modelo.Venta;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Crea los PDF del sistema: boleta de venta, boletas de un cliente y reporte de ventas. */
public class BoletaPDF {
    // ---- datos de la tienda (cambialos por los de tu negocio) ----
    public static final String TIENDA = "FAMILY MARKET";
    public static final String RUBRO = "Venta de abarrotes, bebidas y productos de primera necesidad";
    public static final String DIRECCION = "Av. San Felipe, Urb. San Felipe – Comas, Lima, Perú.";
    // cuando tengan RUC o telefono, agregalos aqui y en dibujarBoleta()

    private static final Color AZUL_OSCURO = new Color(13, 27, 47);
    private static final Color AZUL = new Color(35, 113, 237);
    private static final Color TEXTO = new Color(28, 40, 60);
    private static final Color GRIS = new Color(110, 125, 148);
    private static final Color BORDE = new Color(208, 217, 230);
    private static final Color FONDO = new Color(240, 244, 250);
    private static final Color BLANCO = Color.WHITE;

    private BoletaPDF() {}

    // ================= archivos =================
    private static File carpeta(String nombre) throws IOException {
        File dir = new File(nombre);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + dir.getAbsolutePath());
        }
        return dir;
    }

    private static String limpiar(String s) {
        return s.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** Boleta de una venta -> boletas/B001-00001.pdf */
    public static File generar(Venta venta) throws IOException {
        File archivo = new File(carpeta("boletas"), limpiar(venta.getCodigo()) + ".pdf");
        GeneradorPDF pdf = new GeneradorPDF(GeneradorPDF.A4_ANCHO, GeneradorPDF.A4_ALTO);
        pdf.nuevaPagina();
        dibujarBoleta(pdf, venta);
        pdf.guardar(archivo);
        return archivo;
    }

    /** Todas las boletas de UN cliente (una por pagina) -> boletas/Boletas_C001.pdf */
    public static File generarBoletasDeCliente(Cliente cliente, List<Venta> ventas) throws IOException {
        if (ventas.isEmpty()) {
            throw new IllegalArgumentException("El cliente " + cliente.getNombre() + " no tiene boletas.");
        }
        File archivo = new File(carpeta("boletas"), "Boletas_" + limpiar(cliente.getCodigo()) + ".pdf");
        GeneradorPDF pdf = new GeneradorPDF(GeneradorPDF.A4_ANCHO, GeneradorPDF.A4_ALTO);
        for (Venta v : ventas) {
            pdf.nuevaPagina();
            dibujarBoleta(pdf, v);
        }
        pdf.guardar(archivo);
        return archivo;
    }

    /** Reporte de ventas (ya filtradas) -> reportes/Reporte_20261030_153000.pdf */
    public static File generarReporte(String titulo, List<String> filtros, List<Venta> ventas,
            String generadoPor) throws IOException {
        String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File archivo = new File(carpeta("reportes"), "Reporte_" + marca + ".pdf");
        dibujarReporte(archivo, titulo, filtros, ventas, generadoPor);
        return archivo;
    }

    /** Abre el PDF con el lector predeterminado. Devuelve false si no se pudo abrir. */
    public static boolean abrir(File archivo) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
                return true;
            }
        } catch (IOException | RuntimeException ex) {
            // se avisa en la ventana con la ruta del archivo
        }
        return false;
    }

    // ================= boleta =================
    private static void dibujarBoleta(GeneradorPDF pdf, Venta venta) {
        final float izq = 40, der = 555;
        Cliente cli = venta.getCliente();

        // ---- encabezado: tienda a la izquierda, cuadro con RUC y numero a la derecha ----
        pdf.texto(TIENDA, izq, 68, 22, true, AZUL_OSCURO);
        pdf.texto(RUBRO, izq, 88, 9.5f, false, GRIS);
        pdf.texto(DIRECCION, izq, 102, 9.5f, false, GRIS);

        pdf.rect(375, 48, 180, 76, BLANCO, AZUL_OSCURO, 1.2f);
        pdf.rect(375, 48, 180, 30, AZUL_OSCURO, null, 0);
        pdf.textoCentro("BOLETA DE VENTA", 465, 68, 12, true, BLANCO);
        pdf.textoCentro(venta.getCodigo(), 465, 108, 16, true, AZUL);

        // ---- datos del cliente ----
        pdf.rect(izq, 152, der - izq, 82, FONDO, BORDE, 0.8f);
        float y = 172;
        campo(pdf, "Fecha de emision:", venta.getFecha(), 52, 150, y, 150);
        campo(pdf, "Cod. cliente:", cli.getCodigo(), 330, 410, y, 140);
        y += 20;
        campo(pdf, "Cliente:", cli.getNombre(), 52, 150, y, 170);
        campo(pdf, "DNI:", cli.getDni(), 330, 410, y, 140);
        y += 20;
        campo(pdf, "Atendido por:", venta.getAtendidoPor(), 52, 150, y, 170);
        campo(pdf, "Edad:", cli.getEdad() + " años", 330, 410, y, 140);

        // ---- detalle: una fila por producto ----
        float tabla = 256;
        pdf.rect(izq, tabla, der - izq, 26, AZUL_OSCURO, null, 0);
        pdf.texto("COD.", 48, tabla + 17, 10, true, BLANCO);
        pdf.texto("DESCRIPCION", 105, tabla + 17, 10, true, BLANCO);
        pdf.textoDerecha("CANT.", 370, tabla + 17, 10, true, BLANCO);
        pdf.textoDerecha("P. UNIT.", 460, tabla + 17, 10, true, BLANCO);
        pdf.textoDerecha("IMPORTE", 547, tabla + 17, 10, true, BLANCO);

        float fila = tabla + 26;
        for (ItemVenta it : venta.getItems()) {
            pdf.rect(izq, fila, der - izq, 26, BLANCO, BORDE, 0.8f);
            pdf.texto(it.getProducto().getCodigo(), 48, fila + 17, 11, false, TEXTO);
            pdf.texto(GeneradorPDF.recortar(it.getProducto().getNombre(), 215, 11, false), 105, fila + 17, 11, false, TEXTO);
            pdf.textoDerecha(String.valueOf(it.getCantidad()), 370, fila + 17, 11, false, TEXTO);
            pdf.textoDerecha(Moneda.formato(it.getPrecioUnitario()), 460, fila + 17, 11, false, TEXTO);
            pdf.textoDerecha(Moneda.formato(it.subtotal()), 547, fila + 17, 11, false, TEXTO);
            fila += 26;
        }

        // ---- totales ----
        float t = fila + 24;
        pdf.textoDerecha("Subtotal:", 460, t, 11, false, GRIS);
        pdf.textoDerecha(Moneda.formato(venta.subtotal()), 547, t, 11, false, TEXTO);
        t += 20;
        String etiqueta = cli.tieneDescuento() ? "Descuento (15%):" : "Descuento:";
        pdf.textoDerecha(etiqueta, 460, t, 11, false, GRIS);
        pdf.textoDerecha((cli.tieneDescuento() ? "- " : "") + Moneda.formato(venta.descuento()), 547, t, 11, false, TEXTO);
        t += 12;
        pdf.rect(340, t, der - 340, 30, AZUL, null, 0);
        pdf.texto("TOTAL A PAGAR", 350, t + 20, 11, true, BLANCO);
        pdf.textoDerecha(Moneda.formato(venta.total()), 547, t + 20, 13, true, BLANCO);

        // ---- monto en letras (a la izquierda de los totales) ----
        float l = fila + 24;
        pdf.texto("SON:", izq, l, 10, true, TEXTO);
        for (String linea : GeneradorPDF.ajustar(NumeroALetras.convertir(venta.total()), 255, 10, false)) {
            pdf.texto(linea, izq + 30, l, 10, false, TEXTO);
            l += 14;
        }

        // ---- pie ----
        float pie = t + 70;
        pdf.linea(izq, pie, der, pie, 0.8f, BORDE);
        pdf.textoCentro("¡Gracias por su compra!", 297.5f, pie + 22, 13, true, AZUL_OSCURO);
        pdf.textoCentro("Conserve esta boleta para cualquier reclamo o cambio.", 297.5f, pie + 38, 9, false, GRIS);
    }

    private static void campo(GeneradorPDF pdf, String etiqueta, String valor, float xEtiqueta, float xValor,
            float y, float anchoValor) {
        pdf.texto(etiqueta, xEtiqueta, y, 10, true, GRIS);
        pdf.texto(GeneradorPDF.recortar(valor, anchoValor, 10, false), xValor, y, 10, false, TEXTO);
    }

    // ================= reporte =================
    private static void dibujarReporte(File archivo, String titulo, List<String> filtros, List<Venta> ventas,
            String generadoPor) throws IOException {
        final float W = 842, H = 595, m = 36, alturaFila = 20;
        GeneradorPDF pdf = new GeneradorPDF(W, H);

        String[] cols = {"N°", "Boleta", "Fecha", "Cod.", "Cliente", "Productos", "Unid.", "Subtotal", "Desc.", "Total"};
        float[] anchos = {28, 72, 92, 38, 135, 130, 36, 72, 60, 71};
        boolean[] derecha = {false, false, false, false, false, false, true, true, true, true};

        // filas que entran en cada pagina (la 1ra tiene encabezado grande)
        float inicio1 = 60 + 30 + 16 * filtros.size() + 20;
        int cap1 = (int) ((H - 40 - inicio1 - alturaFila) / alturaFila);
        int capN = (int) ((H - 40 - 50 - alturaFila) / alturaFila);
        int paginas = 1;
        int restantes = ventas.size() + 3 - cap1;   // +3 filas reservadas para los totales
        while (restantes > 0) {
            paginas++;
            restantes -= capN;
        }

        int indice = 0;
        for (int p = 1; p <= paginas; p++) {
            pdf.nuevaPagina();
            float y;
            int capacidad;
            if (p == 1) {
                pdf.rect(0, 0, W, 60, AZUL_OSCURO, null, 0);
                pdf.texto(titulo, m, 30, 18, true, BLANCO);
                pdf.texto(TIENDA, m, 47, 9, false, new Color(155, 174, 199));
                pdf.textoDerecha("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        + "  por " + generadoPor, W - m, 38, 9, false, new Color(155, 174, 199));
                y = 84;
                for (String f : filtros) {
                    pdf.texto(f, m, y, 10, false, TEXTO);
                    y += 16;
                }
                y += 10;
                capacidad = cap1;
            } else {
                y = 50;
                capacidad = capN;
            }

            // encabezado de la tabla
            pdf.rect(m, y, W - 2 * m, alturaFila + 2, AZUL_OSCURO, null, 0);
            float x = m;
            for (int c = 0; c < cols.length; c++) {
                if (derecha[c]) {
                    pdf.textoDerecha(cols[c], x + anchos[c] - 4, y + 15, 9, true, BLANCO);
                } else {
                    pdf.texto(cols[c], x + 4, y + 15, 9, true, BLANCO);
                }
                x += anchos[c];
            }
            y += alturaFila + 2;

            boolean ultima = (p == paginas);
            int hasta = Math.min(ventas.size(), indice + capacidad - (ultima ? 3 : 0));
            if (!ultima) {
                hasta = Math.min(ventas.size(), indice + capacidad);
            }
            for (; indice < hasta; indice++) {
                Venta v = ventas.get(indice);
                if (indice % 2 == 1) {
                    pdf.rect(m, y, W - 2 * m, alturaFila, FONDO, null, 0);
                }
                String[] celdas = {String.valueOf(indice + 1), v.getCodigo(), v.getFecha(), v.getCliente().getCodigo(),
                    v.getCliente().getNombre(), v.descripcionProductos(), String.valueOf(v.getUnidades()),
                    Moneda.formato(v.subtotal()), Moneda.formato(v.descuento()), Moneda.formato(v.total())};
                x = m;
                for (int c = 0; c < celdas.length; c++) {
                    String s = GeneradorPDF.recortar(celdas[c], anchos[c] - 8, 9, false);
                    if (derecha[c]) {
                        pdf.textoDerecha(s, x + anchos[c] - 4, y + 14, 9, false, TEXTO);
                    } else {
                        pdf.texto(s, x + 4, y + 14, 9, false, TEXTO);
                    }
                    x += anchos[c];
                }
                y += alturaFila;
            }
            pdf.linea(m, y, W - m, y, 0.8f, BORDE);

            if (ultima) {
                y += 8;
                if (ventas.isEmpty()) {
                    pdf.texto("No hay ventas para los filtros elegidos.", m + 4, y + 14, 10, false, GRIS);
                    y += alturaFila;
                }
                pdf.rect(W - m - 260, y + 6, 260, 52, FONDO, BORDE, 0.8f);
                pdf.texto("Ventas: " + ventas.size() + "     Unidades: " + unidades(ventas), W - m - 250, y + 22, 10, false, TEXTO);
                pdf.texto("Descuentos: " + Moneda.formato(descuentos(ventas)), W - m - 250, y + 36, 10, false, TEXTO);
                pdf.texto("TOTAL VENDIDO: " + Moneda.formato(total(ventas)), W - m - 250, y + 52, 12, true, AZUL);
            }
            pdf.textoDerecha("Pagina " + p + " de " + paginas, W - m, H - 18, 9, false, GRIS);
        }
        pdf.guardar(archivo);
    }

    private static double total(List<Venta> ventas) {
        double s = 0;
        for (Venta v : ventas) {
            s += v.total();
        }
        return Moneda.redondear(s);
    }

    private static double descuentos(List<Venta> ventas) {
        double s = 0;
        for (Venta v : ventas) {
            s += v.descuento();
        }
        return Moneda.redondear(s);
    }

    private static int unidades(List<Venta> ventas) {
        int s = 0;
        for (Venta v : ventas) {
            s += v.getUnidades();
        }
        return s;
    }
}
