package Componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/** Iconos de linea dibujados con Java2D para el menu y la barra superior */
public class Iconos {

    private Iconos() {}

    public static Icon de(String nombre, Color color, int tam) {
        return new Icon() {
            @Override
            public int getIconWidth() { return tam; }
            @Override
            public int getIconHeight() { return tam; }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.scale(tam / 24.0, tam / 24.0);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                dibujar(g2, nombre);
                g2.dispose();
            }
        };
    }

    // todos los dibujos estan en una cuadricula de 24 x 24
    private static void dibujar(Graphics2D g, String n) {
        switch (n) {
            case "inicio":
                linea(g, 3, 12, 12, 4, 21, 12);
                linea(g, 5, 10, 5, 20, 19, 20, 19, 10);
                linea(g, 10, 20, 10, 14, 14, 14, 14, 20);
                break;
            case "clientes":
                g.drawOval(4, 4, 8, 8);
                g.draw(new Arc2D.Double(2, 15, 12, 12, 0, 180, Arc2D.OPEN));
                g.drawOval(14, 6, 6, 6);
                g.draw(new Arc2D.Double(14, 15, 8, 8, 0, 180, Arc2D.OPEN));
                break;
            case "usuarios":
                g.drawOval(8, 3, 8, 8);
                g.draw(new Arc2D.Double(4, 14, 16, 16, 0, 180, Arc2D.OPEN));
                break;
            case "productos":
            case "vendido":
                linea(g, 12, 3, 20, 7.5, 20, 16.5, 12, 21, 4, 16.5, 4, 7.5, 12, 3);
                linea(g, 4, 7.5, 12, 12, 20, 7.5);
                linea(g, 12, 12, 12, 21);
                break;
            case "ventas":
                linea(g, 2, 4, 5, 4, 8, 16, 19, 16, 21, 7, 6, 7);
                g.drawOval(8, 19, 3, 3);
                g.drawOval(16, 19, 3, 3);
                break;
            case "boletas":
                linea(g, 6, 3, 14, 3, 19, 8, 19, 21, 6, 21, 6, 3);
                linea(g, 14, 3, 14, 8, 19, 8);
                linea(g, 9, 13, 16, 13);
                linea(g, 9, 17, 16, 17);
                break;
            case "reportes":
                g.drawRoundRect(4, 3, 16, 18, 3, 3);
                linea(g, 8, 17, 8, 13);
                linea(g, 12, 17, 12, 9);
                linea(g, 16, 17, 16, 11);
                break;
            case "menu":
                linea(g, 4, 6, 20, 6);
                linea(g, 4, 12, 20, 12);
                linea(g, 4, 18, 20, 18);
                break;
            case "campana":
                g.draw(new Arc2D.Double(6, 4, 12, 14, 0, 180, Arc2D.OPEN));
                linea(g, 6, 11, 6, 15, 4, 17, 20, 17, 18, 15, 18, 11);
                g.drawArc(10, 18, 4, 4, 180, 180);
                break;
            case "flecha":
                linea(g, 6, 9, 12, 15, 18, 9);
                break;
            default:
                break;
        }
    }

    // une los puntos (x1,y1,x2,y2,...) con lineas
    private static void linea(Graphics2D g, double... p) {
        Path2D.Double ruta = new Path2D.Double();
        ruta.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) {
            ruta.lineTo(p[i], p[i + 1]);
        }
        g.draw(ruta);
    }
}
