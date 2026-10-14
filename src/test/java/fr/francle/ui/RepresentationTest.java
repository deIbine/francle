package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.donnees.Zone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Dimension;
import java.util.Optional;

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

    @ParameterizedTest
    @EnumSource(ModeRegions.class)
    @DisplayName("Carte de fin de partie : pour chaque région de métropole du mode, et elle existe")
    void carte(ModeRegions mode) {
        for (RegionFr region : RegionFr.tirables(mode)) {
            Optional<String> carte = Representation.carte(region, mode);

            assertEquals(region.getZone() == Zone.METROPOLE, carte.isPresent(), region.name());
            carte.ifPresent(chemin -> assertNotNull(Representation.charger(chemin), chemin));
        }
    }

    @Test
    @DisplayName("Une région inchangée en 2016 a une carte de chaque époque")
    void carteSelonLEpoque() {
        assertEquals(Optional.of("cartes/nouvelles/bretagne.png"), Representation.carte(RegionFr.BRETAGNE, ModeRegions.NOUVELLES));
        assertEquals(Optional.of("cartes/anciennes/bretagne.png"), Representation.carte(RegionFr.BRETAGNE, ModeRegions.ANCIENNES));
        assertEquals(Optional.empty(), Representation.carte(RegionFr.GUYANE, ModeRegions.NOUVELLES));
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
    @DisplayName("L'icône d'une vraie image tient dans le cadre")
    void icone() {
        var icone = Representation.icone("cartes/nouvelles/bretagne.png", 420, 320);

        assertTrue(icone.getIconWidth() <= 420 && icone.getIconHeight() <= 320);
        assertTrue(icone.getIconWidth() == 420 || icone.getIconHeight() == 320);
    }
}
