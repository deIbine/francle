package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Les contours des départements, et la carte du bonus : dessin ajusté au composant, clic reconnu
class CarteDepartementsTest {

    private static final LocalDate JOUR = LocalDate.of(2030, 3, 10);

    private static Departement departement(String code) {
        return Departement.parCode(code).orElseThrow();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(RegionFr.class)
    @DisplayName("Chaque région a ses contours : un tracé pour chacun de ses départements, et rien d'autre")
    void contoursDeChaqueRegion(RegionFr region) {
        Contours contours = Contours.de(region);

        assertEquals(region.getDepartements(), List.copyOf(contours.formes().keySet()));
        for (var forme : contours.formes().values()) {
            Rectangle2D cadre = forme.getBounds2D();
            assertFalse(cadre.isEmpty());
            // dans le rectangle annoncé par le fichier (à l'arrondi près)
            assertTrue(cadre.getMinX() >= -1 && cadre.getMaxX() <= contours.largeur() + 1, region.name());
            assertTrue(cadre.getMinY() >= -1 && cadre.getMaxY() <= contours.hauteur() + 1, region.name());
        }
        assertEquals(region == RegionFr.ILE_DE_FRANCE, contours.zoom().isPresent());
    }

    @Test
    @DisplayName("Règle pair-impair : un tracé à l'intérieur d'un autre est un trou (une enclave), un tracé à côté une île")
    void trousEtIles() {
        // un carré de 100 percé d'un carré de 20 en son milieu, et une île de 10 à côté
        var forme = Contours.forme(List.of(
                new int[]{0, 0, 100, 0, 100, 100, 0, 100},
                new int[]{40, 40, 60, 40, 60, 60, 40, 60},
                new int[]{200, 0, 210, 0, 210, 10, 200, 10}));

        assertTrue(forme.contains(10, 10));
        assertFalse(forme.contains(50, 50), "le trou");
        assertTrue(forme.contains(205, 5), "l'île");
        assertFalse(forme.contains(150, 5));
    }

    @Test
    @DisplayName("ajuster fait tenir la source dans le cadre, centrée, sans la déformer")
    void ajuster() {
        AffineTransform transformation = CarteDepartements.ajuster(new Rectangle2D.Double(0, 0, 2000, 1000),
                new Rectangle(10, 10, 400, 400));

        assertEquals(new Point2D.Double(10, 110), transformation.transform(new Point2D.Double(0, 0), null));
        assertEquals(new Point2D.Double(410, 310), transformation.transform(new Point2D.Double(2000, 1000), null));
    }

    // tous les départements trouvés en balayant le composant, pixel par pixel
    private static Set<Departement> balayer(CarteDepartements carte) {
        Set<Departement> trouves = new HashSet<>();
        for (int y = 0; y < carte.getHeight(); y++) {
            for (int x = 0; x < carte.getWidth(); x++) {
                carte.departementSous(new Point(x, y)).ifPresent(trouves::add);
            }
        }
        return trouves;
    }

    private static CarteDepartements carte(RegionFr region, List<Departement> clics) {
        CarteDepartements carte = new CarteDepartements(clics::add);
        carte.setSize(400, 220);
        carte.montrer(Bonus.duJour(region, JOUR));
        return carte;
    }

    @Test
    @DisplayName("Chaque département de la carte se trouve sous au moins un pixel, et seulement eux")
    void toutesLesFormesSontCliquables() {
        CarteDepartements carte = carte(RegionFr.OCCITANIE, new ArrayList<>());

        assertEquals(Set.copyOf(RegionFr.OCCITANIE.getDepartements()), balayer(carte));
        assertEquals(Optional.empty(), carte.departementSous(new Point(0, 0)));
    }

    @Test
    @DisplayName("Île-de-France : Paris est assez grand dans l'encart zoomé pour être cliqué facilement")
    void encartDeParis() {
        CarteDepartements carte = carte(RegionFr.ILE_DE_FRANCE, new ArrayList<>());

        assertEquals(Set.copyOf(RegionFr.ILE_DE_FRANCE.getDepartements()), balayer(carte));
        int pixelsDeParis = 0;
        for (int y = 0; y < carte.getHeight(); y++) {
            for (int x = 0; x < carte.getWidth(); x++) {
                if (carte.departementSous(new Point(x, y)).equals(Optional.of(departement("75")))) {
                    pixelsDeParis++;
                }
            }
        }
        // sans l'encart, Paris ne couvrirait qu'une dizaine de pixels ; avec, plusieurs centaines
        assertTrue(pixelsDeParis > 400, "Paris : " + pixelsDeParis + " pixels");
    }

    @Test
    @DisplayName("Avant d'avoir une taille, la carte ne trouve rien (et ne plante pas)")
    void sansTaille() {
        CarteDepartements carte = new CarteDepartements(d -> {});
        carte.montrer(Bonus.duJour(RegionFr.BRETAGNE, JOUR));

        assertEquals(Optional.empty(), carte.departementSous(new Point(5, 5)));
    }
}
