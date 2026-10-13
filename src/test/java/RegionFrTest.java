import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Les données des régions : recherche des saisies, distances, tirage, images
class RegionFrTest {

    @ParameterizedTest(name = "« {0} » → {1}")
    @CsvSource(delimiter = '|', value = {
            "Île-de-France            | iledefrance",
            "  ile de FRANCE          | iledefrance",
            "Provence-Alpes-Côte d'Azur | provencealpescotedazur",
            "La Réunion               | lareunion",
    })
    @DisplayName("simplifier ignore espaces, majuscules, ponctuation et accents")
    void simplifier(String saisie, String attendu) {
        assertEquals(attendu, RegionFr.simplifier(saisie));
    }

    @Test
    @DisplayName("Chaque région se retrouve par son propre nom dans le mode « toutes »")
    void chercherSonNom() {
        for (RegionFr region : RegionFr.values()) {
            assertEquals(Optional.of(region), RegionFr.chercher(region.getNom(), ModeRegions.TOUTES));
        }
    }

    @Test
    @DisplayName("Recherche selon le mode : les anciennes régions sont refusées seulement en mode « nouvelles »")
    void chercherSelonLeMode() {
        assertTrue(RegionFr.chercher("Alsace", ModeRegions.NOUVELLES).isEmpty());
        assertEquals(Optional.of(RegionFr.ALSACE), RegionFr.chercher("Alsace", ModeRegions.ANCIENNES));
        assertEquals(Optional.of(RegionFr.ALSACE), RegionFr.chercher("Alsace", ModeRegions.TOUTES));
        // une nouvelle région reste reconnue comme proposition en mode « anciennes »
        assertEquals(Optional.of(RegionFr.GRAND_EST), RegionFr.chercher("Grand Est", ModeRegions.ANCIENNES));
        assertTrue(RegionFr.chercher("Paris", ModeRegions.TOUTES).isEmpty());
    }

    @ParameterizedTest(name = "{0} → {1} ≈ {2} km")
    @CsvSource({
            "BRETAGNE,       GRAND_EST,                  698",
            "ILE_DE_FRANCE,  AUVERGNE_RHONE_ALPES,       392",
            "ILE_DE_FRANCE,  PROVENCE_ALPES_COTE_D_AZUR, 661",
    })
    @DisplayName("Distances connues entre capitales régionales (à 5 km près)")
    void distancesConnues(RegionFr depart, RegionFr arrivee, double attendu) {
        assertEquals(attendu, depart.distance(arrivee), 5);
    }

    @Test
    @DisplayName("La distance est symétrique, nulle d'une région à elle-même, et jamais NaN")
    void distanceCoherente() {
        // tolérance d'un mètre : près de 1, acos amplifie les erreurs d'arrondi des double
        // (une erreur de 1e-16 sur le cosinus donne quelques centimètres)
        double unMetre = 0.001;
        for (RegionFr a : RegionFr.values()) {
            assertEquals(0, a.distance(a), unMetre, a.getNom());
            for (RegionFr b : RegionFr.values()) {
                assertFalse(Double.isNaN(a.distance(b)), a + " → " + b);
                assertEquals(a.distance(b), b.distance(a), unMetre);
            }
        }
    }

    @ParameterizedTest(name = "{0} : {1} régions")
    @CsvSource({"NOUVELLES, 18", "ANCIENNES, 27", "TOUTES, 35"})
    @DisplayName("Nombre de régions tirables dans chaque mode")
    void tirables(ModeRegions mode, int attendu) {
        assertEquals(attendu, RegionFr.tirables(mode).size());
    }

    @ParameterizedTest
    @EnumSource(ModeRegions.class)
    @DisplayName("Une région tirée est toujours tirable et reconnue dans son mode")
    void tirer(ModeRegions mode) {
        for (int i = 0; i < 200; i++) {
            RegionFr region = RegionFr.tirer(mode);
            assertTrue(mode.peutTirer(region));
            assertTrue(mode.reconnait(region));
        }
    }

    @ParameterizedTest
    @EnumSource(RegionFr.class)
    @DisplayName("L'image de chaque région existe dans les ressources")
    void imageExiste(RegionFr region) {
        String fichier = region.getImage() == null ? "defaut.png" : region.getImage();
        assertNotNull(Representation.Image("regions/" + fichier), fichier);
    }

    @Test
    @DisplayName("Toute région qu'on peut faire deviner en mode « nouvelles » a ses 4 indices")
    void indicesRemplis() {
        // les anciennes régions n'ont pas encore d'indices (Phase 5)
        for (RegionFr region : RegionFr.tirables(ModeRegions.NOUVELLES)) {
            assertTrue(region.getSuperficie() > 0, region.getNom());
            assertFalse(region.getSpecialite().isBlank(), region.getNom());
            assertFalse(region.getPersonnalite().isBlank(), region.getNom());
            assertFalse(region.getCapitale().isBlank(), region.getNom());
        }
    }
}
