package fr.francle.ui;

import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

// Les images : chargement depuis les ressources du classpath, choix de l'image, taille dans le cadre
class RepresentationTest {

    @ParameterizedTest
    @EnumSource(RegionFr.class)
    @DisplayName("La silhouette de chaque région existe dans les ressources")
    void imageExiste(RegionFr region) {
        String chemin = Representation.silhouette(region, true);
        assertNotNull(Representation.charger(chemin), chemin);
    }

    @Test
    @DisplayName("L'image par défaut existe")
    void defautExiste() {
        assertNotNull(Representation.charger(Representation.DEFAUT));
    }

    @Test
    @DisplayName("Une image absente donne null, sans exception")
    void imageAbsente() {
        assertNull(Representation.charger("regions/inexistante.png"));
        assertNull(Representation.icone("regions/inexistante.png", 100, 100));
    }

    @Test
    @DisplayName("La silhouette, sauf si le joueur l'a désactivée")
    void choixDeLaSilhouette() {
        assertEquals("regions/bretagne.png", Representation.silhouette(RegionFr.BRETAGNE, true));
        assertEquals("regions/alsace.png", Representation.silhouette(RegionFr.ALSACE, true));
        assertEquals(Representation.DEFAUT, Representation.silhouette(RegionFr.BRETAGNE, false));
    }

    @Test
    @DisplayName("Ajuster au cadre garde les proportions, en réduisant comme en agrandissant")
    void ajuster() {
        // trop large : c'est la largeur qui limite
        assertEquals(new Dimension(420, 210), Representation.ajuster(840, 420, 420, 320));
        // trop haute : c'est la hauteur qui limite
        assertEquals(new Dimension(160, 320), Representation.ajuster(500, 1000, 420, 320));
        // trop petite : agrandie jusqu'à toucher un bord du cadre
        assertEquals(new Dimension(420, 210), Representation.ajuster(100, 50, 420, 320));
    }

    @Test
    @DisplayName("Tourner : un quart de tour échange largeur et hauteur, un demi-tour les garde")
    void tournerQuartDeTour() {
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);

        BufferedImage quart = Representation.tourner(image, 90);
        BufferedImage demi = Representation.tourner(image, 180);

        // les cosinus et sinus des double ne tombent pas pile sur 0 : un pixel de plus au plus
        assertEquals(100, quart.getWidth(), 1);
        assertEquals(200, quart.getHeight(), 1);
        assertEquals(200, demi.getWidth(), 1);
        assertEquals(100, demi.getHeight(), 1);
    }

    @Test
    @DisplayName("Tourner de 45° agrandit la toile pour qu'aucun coin ne soit coupé")
    void tournerEnBiais() {
        BufferedImage carre = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        BufferedImage tournee = Representation.tourner(carre, 45);

        // la diagonale d'un carré de 100 : 100 × √2 ≈ 141,4
        assertEquals(142, tournee.getWidth(), 1);
        assertEquals(142, tournee.getHeight(), 1);
    }

    @Test
    @DisplayName("Rogner garde le plus petit rectangle qui contient les pixels visibles")
    void rogner() {
        BufferedImage image = new BufferedImage(100, 80, BufferedImage.TYPE_INT_ARGB);
        // un seul rectangle opaque, de (20, 10) à (59, 39) : 40 × 30 pixels
        for (int y = 10; y < 40; y++) {
            for (int x = 20; x < 60; x++) {
                image.setRGB(x, y, 0xFF336699);
            }
        }

        BufferedImage rognee = Representation.rogner(image);

        assertEquals(40, rognee.getWidth());
        assertEquals(30, rognee.getHeight());
        // une image entièrement transparente reste telle quelle
        BufferedImage vide = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        assertSame(vide, Representation.rogner(vide));
    }

    @Test
    @DisplayName("Une silhouette tournée tient toujours dans le cadre")
    void iconeTournee() {
        for (int degres : new int[] {0, 30, 90, 200, 359}) {
            var icone = Representation.icone("regions/bretagne.png", 400, 220, degres);

            assertTrue(icone.getIconWidth() <= 400 && icone.getIconHeight() <= 220, degres + "°");
            assertTrue(icone.getIconWidth() == 400 || icone.getIconHeight() == 220, degres + "°");
        }
    }

    @Test
    @DisplayName("L'icône d'une vraie image tient dans le cadre")
    void icone() {
        var icone = Representation.icone("regions/bretagne.png", 420, 320);

        assertTrue(icone.getIconWidth() <= 420 && icone.getIconHeight() <= 320);
        assertTrue(icone.getIconWidth() == 420 || icone.getIconHeight() == 320);
    }
}
