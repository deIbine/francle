package fr.francle.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// L'icône de la fenêtre, dessinée en mémoire
class IconeFenetreTest {

    @Test
    @DisplayName("Une image carrée par taille, de 16 à 256 pixels")
    void tailles() {
        List<BufferedImage> images = IconeFenetre.images();
        assertEquals(IconeFenetre.TAILLES.length, images.size());
        for (int i = 0; i < images.size(); i++) {
            assertEquals(IconeFenetre.TAILLES[i], images.get(i).getWidth());
            assertEquals(IconeFenetre.TAILLES[i], images.get(i).getHeight());
        }
    }

    @Test
    @DisplayName("Les coins sont transparents, le bord bleu, et la France blanche au milieu")
    void dessin() {
        BufferedImage image = IconeFenetre.dessiner(ContoursFrance.de(), 64);
        // le coin, hors de l'arrondi
        assertEquals(0, image.getRGB(0, 0) >>> 24);
        // le milieu d'un côté, dans la marge : le fond bleu
        assertEquals(IconeFenetre.FOND.getRGB(), image.getRGB(32, 2));
        // le centre de l'icône tombe en France (le centre de la métropole, vers le Centre-Val de Loire)
        assertEquals(IconeFenetre.FRANCE.getRGB(), image.getRGB(30, 30));
    }
}
