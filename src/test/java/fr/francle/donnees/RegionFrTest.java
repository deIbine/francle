package fr.francle.donnees;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
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

    @ParameterizedTest(name = "« {0} » en mode {1} → {2}")
    @CsvSource(delimiter = '|', value = {
            "bre        | NOUVELLES | BRETAGNE",
            "  ÎLE-de   | NOUVELLES | ILE_DE_FRANCE",
            "nor        | NOUVELLES | NORMANDIE",
            // les anciennes régions n'apparaissent que si elles sont acceptées ; Nord < Norm ; puis le mot « Normandie »
            "nor        | TOUTES    | NORD_PAS_DE_CALAIS NORMANDIE BASSE_NORMANDIE HAUTE_NORMANDIE",
            // un mot du nom, pas son début : chaque groupe trié, Centre avant Pays
            "loire      | NOUVELLES | CENTRE_VAL_DE_LOIRE PAYS_DE_LA_LOIRE",
            // d'abord le nom qui commence par la saisie, puis ceux dont un mot commence par elle
            "normandie  | TOUTES    | NORMANDIE BASSE_NORMANDIE HAUTE_NORMANDIE",
            "azur       | NOUVELLES | PROVENCE_ALPES_COTE_D_AZUR",
    })
    @DisplayName("Suggestions : nom qui commence par la saisie, puis mot qui commence par elle")
    void suggestions(String saisie, ModeRegions mode, String attendues) {
        List<RegionFr> attendu = Arrays.stream(attendues.split(" ")).map(RegionFr::valueOf).toList();
        assertEquals(attendu, RegionFr.suggestions(saisie, mode));
    }

    @Test
    @DisplayName("Pas de suggestion pour une saisie vide, inconnue, ou un petit mot (« de », « d' »)")
    void pasDeSuggestion() {
        assertEquals(List.of(), RegionFr.suggestions("", ModeRegions.TOUTES));
        assertEquals(List.of(), RegionFr.suggestions("  -' ", ModeRegions.TOUTES));
        assertEquals(List.of(), RegionFr.suggestions("xyz", ModeRegions.TOUTES));
        assertEquals(List.of(), RegionFr.suggestions("de", ModeRegions.TOUTES));
        assertEquals(List.of(), RegionFr.suggestions("d", ModeRegions.TOUTES));
    }

    @Test
    @DisplayName("Chaque région est la première suggestion pour son nom complet, et elle est reconnue")
    void premiereSuggestion() {
        for (RegionFr region : RegionFr.values()) {
            List<RegionFr> suggestions = RegionFr.suggestions(region.getNom(), ModeRegions.TOUTES);
            assertEquals(region, suggestions.get(0), region.name());
            // une suggestion choisie doit toujours être acceptée par chercher() : les deux règles restent d'accord
            for (RegionFr suggestion : suggestions) {
                assertEquals(Optional.of(suggestion), RegionFr.chercher(suggestion.getNom(), ModeRegions.TOUTES));
            }
        }
    }

    @Test
    @DisplayName("En mode « nouvelles », jamais d'ancienne région suggérée")
    void suggestionsSelonLeMode() {
        for (char lettre = 'a'; lettre <= 'z'; lettre++) {
            for (RegionFr region : RegionFr.suggestions(String.valueOf(lettre), ModeRegions.NOUVELLES)) {
                assertNotEquals(Epoque.ANCIENNE, region.getEpoque(), lettre + " → " + region);
            }
        }
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
