package Componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** Logo "FAMILY MARKET" dibujado con Java2D (no necesita imagenes) */
public class Logo extends JComponent {

    public Logo() {
        setPreferredSize(new Dimension(190, 56));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int y = (getHeight() - 36) / 2;

        // barra verde inclinada
        g2.rotate(-0.6, 20, y + 12);
        g2.setColor(new Color(110, 190, 70));
        g2.fillRoundRect(8, y + 6, 24, 9, 4, 4);
        g2.rotate(0.6, 20, y + 12);

        // grafico de barras
        g2.setColor(new Color(74, 144, 226));
        g2.fillRect(18, y + 24, 6, 10);
        g2.fillRect(26, y + 18, 6, 16);
        g2.fillRect(34, y + 11, 6, 23);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(17, y + 29, 41, y + 29);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("FAMILY", 52, y + 17);
        g2.drawString("MARKET", 52, y + 34);
        g2.dispose();
    }
}
