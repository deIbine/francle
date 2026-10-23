package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.donnees.Zone;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// La carte de France de la fin de partie : les couleurs, la disposition, le dessin
class CarteFinTest {

    // une partie de cette cible, avec ces propositions
    private static Partie partie(RegionFr cible, ModeRegions mode, String... propositions) {
        Partie partie = new Partie(cible, mode);
        for (String proposition : propositions) {
            partie.proposer(proposition);
        }
        return partie;
    }

    @Test
    @DisplayName("Le dégradé va du jaune pâle (0 %) à l'orange foncé (100 %), en passant par l'orange")
    void degradeAuxBouts() {
        assertEquals(CarteFin.LOIN, CarteFin.degrade(0));
        assertEquals(CarteFin.MILIEU, CarteFin.degrade(50));
        assertEquals(CarteFin.PROCHE, CarteFin.degrade(100));
        // hors des bornes (ce qui n'arrive pas), la couleur du bout le plus proche plutôt qu'une erreur
        assertEquals(CarteFin.LOIN, CarteFin.degrade(-5));
        assertEquals(CarteFin.PROCHE, CarteFin.degrade(120));
    }

    @Test
    @DisplayName("Le dégradé est continu et toujours plus foncé : plus proche, jamais plus clair")
    void degradeContinu() {
        for (int p = 0; p < 100; p++) {
            Color avant = CarteFin.degrade(p);
            Color apres = CarteFin.degrade(p + 1);
            // chaque composante baisse (ou reste), et jamais d'un coup : pas de palier visible
            assertTrue(apres.getRed() <= avant.getRed() && apres.getGreen() <= avant.getGreen()
                    && apres.getBlue() <= avant.getBlue(), p + " % -> " + (p + 1) + " %");
            assertTrue(Math.abs(apres.getGreen() - avant.getGreen()) <= 3, "saut à " + p + " %");
        }
    }

    @Test
    @DisplayName("melanger donne a, b, ou la couleur à mi-chemin")
    void melanger() {
        Color noir = Color.BLACK;
        Color blanc = Color.WHITE;

        assertEquals(noir, CarteFin.melanger(noir, blanc, 0));
        assertEquals(blanc, CarteFin.melanger(noir, blanc, 1));
        assertEquals(new Color(128, 128, 128), CarteFin.melanger(noir, blanc, 0.5));
    }

    @Test
    @DisplayName("Partie gagnée : la réponse en vert, les proposées selon leur proximité, les autres au fond")
    void couleursPartieGagnee() {
        Partie partie = partie(RegionFr.AUVERGNE_RHONE_ALPES, ModeRegions.NOUVELLES,
                "Bretagne", "Occitanie", "Auvergne-Rhône-Alpes");

        assertEquals(CarteFin.TROUVEE, CarteFin.couleur(RegionFr.AUVERGNE_RHONE_ALPES, partie));
        for (Essai essai : partie.getEssais().subList(0, 2)) {
            assertEquals(CarteFin.degrade(essai.pourcentage()), CarteFin.couleur(essai.proposition(), partie));
        }
        // l'Occitanie, voisine, est plus foncée que la Bretagne, à l'autre bout de la France
        assertTrue(CarteFin.couleur(RegionFr.OCCITANIE, partie).getGreen()
                < CarteFin.couleur(RegionFr.BRETAGNE, partie).getGreen());
        assertEquals(CarteFin.FOND, CarteFin.couleur(RegionFr.GRAND_EST, partie));
        assertEquals(CarteFin.FOND, CarteFin.couleur(RegionFr.GUYANE, partie));
    }

    @Test
    @DisplayName("Partie perdue ou abandonnée : la réponse en bleu foncé")
    void couleursPartiePerdue() {
        Partie perdue = partie(RegionFr.CORSE, ModeRegions.NOUVELLES,
                "Bretagne", "Normandie", "Grand Est", "Occitanie", "Île-de-France", "Guyane");
        Partie abandonnee = partie(RegionFr.CORSE, ModeRegions.NOUVELLES, "Bretagne");
        abandonnee.abandonner();

        assertEquals(Partie.Etat.PERDUE, perdue.getEtat());
        assertEquals(CarteFin.MANQUEE, CarteFin.couleur(RegionFr.CORSE, perdue));
        assertEquals(CarteFin.MANQUEE, CarteFin.couleur(RegionFr.CORSE, abandonnee));
        // une proposition en outre-mer a sa couleur, dans son encart
        assertNotEquals(CarteFin.FOND, CarteFin.couleur(RegionFr.GUYANE, perdue));
    }

    // une carte de la taille du cadre de la fenêtre, qui montre cette partie
    private static CarteFin carte(Partie partie) {
        CarteFin carte = new CarteFin(RegionFr::getNom);
        carte.setSize(400, 220);
        carte.montrer(partie);
        return carte;
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(ModeRegions.class)
    @DisplayName("Chaque région du mode se trouve sous au moins un pixel, outre-mer compris, et seulement elles")
    void toutesLesRegionsSeVoient(ModeRegions mode) {
        CarteFin carte = carte(partie(RegionFr.BRETAGNE, mode));
        Set<RegionFr> trouvees = new HashSet<>();
        for (int y = 0; y < carte.getHeight(); y++) {
            for (int x = 0; x < carte.getWidth(); x++) {
                carte.regionSous(new Point(x, y)).ifPresent(trouvees::add);
            }
        }

        Set<RegionFr> attendues = RegionFr.tirables(mode).stream().collect(Collectors.toSet());
        assertEquals(attendues, trouvees);
        assertTrue(attendues.stream().anyMatch(region -> region.getZone() == Zone.OUTRE_MER));
        assertEquals(Optional.empty(), carte.regionSous(new Point(0, 0)));
    }

    @Test
    @DisplayName("La bulle d'aide nomme la région survolée, dans la langue que donne la fenêtre")
    void bulleDAide() {
        CarteFin carte = new CarteFin(region -> "« " + region.getNom() + " »");
        carte.setSize(400, 220);
        carte.montrer(partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES));
        Point dansLaBretagne = null;
        for (int y = 0; y < carte.getHeight() && dansLaBretagne == null; y++) {
            for (int x = 0; x < carte.getWidth() && dansLaBretagne == null; x++) {
                if (carte.regionSous(new Point(x, y)).equals(Optional.of(RegionFr.BRETAGNE))) {
                    dansLaBretagne = new Point(x, y);
                }
            }
        }

        assertNotNull(dansLaBretagne);
        var survol = new java.awt.event.MouseEvent(carte, java.awt.event.MouseEvent.MOUSE_MOVED, 0, 0,
                dansLaBretagne.x, dansLaBretagne.y, 0, false);
        assertEquals("« Bretagne »", carte.getToolTipText(survol));
    }

    @Test
    @DisplayName("Le dessin montre bien la réponse en vert et une proposition dans le dégradé")
    void dessin() {
        Partie partie = partie(RegionFr.AUVERGNE_RHONE_ALPES, ModeRegions.NOUVELLES, "Bretagne", "Auvergne-Rhône-Alpes");
        CarteFin carte = carte(partie);
        BufferedImage image = new BufferedImage(400, 220, BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        carte.paint(g);
        g.dispose();

        Color bretagne = CarteFin.couleur(RegionFr.BRETAGNE, partie);
        int verts = 0;
        int dansLeDegrade = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                if (rgb == (CarteFin.TROUVEE.getRGB() & 0xFFFFFF)) {
                    verts++;
                } else if (rgb == (bretagne.getRGB() & 0xFFFFFF)) {
                    dansLeDegrade++;
                }
            }
        }
        // l'Auvergne-Rhône-Alpes et la Bretagne font chacune plusieurs centaines de pixels sur cette carte
        assertTrue(verts > 300, "réponse : " + verts + " pixels");
        assertTrue(dansLeDegrade > 150, "Bretagne : " + dansLeDegrade + " pixels");
    }
}
