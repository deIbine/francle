package fr.francle.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

// L'image réduite au dessin, à la taille en pixels de l'écran, dessinée en mémoire (sans fenêtre)
class ImageNetteTest {

    // une image de 440 × 440 : un disque opaque sur fond transparent
    private static BufferedImage disque() {
        BufferedImage image = new BufferedImage(440, 440, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillOval(0, 0, 440, 440);
        g.dispose();
        return image;
    }

    // dessine l'icône dans une image, avec cet agrandissement de l'écran (1.5 pour Windows à 150 %)
    private static BufferedImage dessiner(ImageNette icone, double agrandissement) {
        int cote = (int) Math.ceil(icone.getIconWidth() * agrandissement);
        BufferedImage ecran = new BufferedImage(cote, cote, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ecran.createGraphics();
        g.scale(agrandissement, agrandissement);
        icone.paintIcon(null, g, 0, 0);
        g.dispose();
        return ecran;
    }

    @Test
    @DisplayName("Sa taille est en points, quelle que soit celle de l'image d'origine")
    void taille() {
        ImageNette icone = new ImageNette(disque(), 220, 220);
        assertEquals(220, icone.getIconWidth());
        assertEquals(220, icone.getIconHeight());
    }

    @Test
    @DisplayName("À 150 %, l'image est réduite à 330 pixels, et non à 220 puis agrandie")
    void reduiteALaTailleEnPixels() {
        ImageNette icone = new ImageNette(disque(), 220, 220);
        dessiner(icone, 1.5);
        BufferedImage reduite = icone.reduite(330, 330);
        assertEquals(330, reduite.getWidth());
        assertEquals(330, reduite.getHeight());
        // la même réduction est gardée tant que la taille ne change pas
        assertSame(reduite, icone.reduite(330, 330));
    }

    @Test
    @DisplayName("Les bords sont doux : entre l'intérieur opaque et le dehors transparent, des pixels à demi opaques")
    void bordsDoux() {
        BufferedImage ecran = dessiner(new ImageNette(disque(), 220, 220), 1.5);
        int milieu = ecran.getHeight() / 2;
        int demiOpaques = 0;
        for (int x = 0; x < ecran.getWidth() / 2; x++) {
            int alpha = ecran.getRGB(x, milieu) >>> 24;
            if (alpha > 0 && alpha < 255) {
                demiOpaques++;
            }
        }
        assertTrue(demiOpaques >= 1, "aucun pixel de transition sur le bord gauche");
        assertEquals(255, ecran.getRGB(ecran.getWidth() / 2, milieu) >>> 24, "le centre est opaque");
    }
}
