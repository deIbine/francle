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
    @DisplayName("Chaque région se retrouve par son propre nom dans au moins un mode")
    void chercherSonNom() {
        for (RegionFr region : RegionFr.values()) {
            boolean trouvee = Arrays.stream(ModeRegions.values())
                    .anyMatch(mode -> RegionFr.chercher(region.getNom(), mode).equals(Optional.of(region)));
            assertTrue(trouvee, region.name());
        }
    }

    @Test
    @DisplayName("Recherche selon le mode : chaque mode refuse les régions de l'autre époque")
    void chercherSelonLeMode() {
        assertTrue(RegionFr.chercher("Alsace", ModeRegions.NOUVELLES).isEmpty());
        assertEquals(Optional.of(RegionFr.ALSACE), RegionFr.chercher("Alsace", ModeRegions.ANCIENNES));
        assertTrue(RegionFr.chercher("Grand Est", ModeRegions.ANCIENNES).isEmpty());
        assertEquals(Optional.of(RegionFr.GRAND_EST), RegionFr.chercher("Grand Est", ModeRegions.NOUVELLES));
        // les régions inchangées en 2016 sont des deux époques
        assertEquals(Optional.of(RegionFr.BRETAGNE), RegionFr.chercher("Bretagne", ModeRegions.ANCIENNES));
        assertEquals(Optional.of(RegionFr.BRETAGNE), RegionFr.chercher("Bretagne", ModeRegions.NOUVELLES));
        assertTrue(RegionFr.chercher("Paris", ModeRegions.NOUVELLES).isEmpty());
    }

    @ParameterizedTest(name = "« {0} » en mode {1} → {2}")
    @CsvSource(delimiter = '|', value = {
            "bre        | NOUVELLES | BRETAGNE",
            "  ÎLE-de   | NOUVELLES | ILE_DE_FRANCE",
            "nor        | NOUVELLES | NORMANDIE",
            // jamais de région de l'autre époque (pas de Normandie ici) ; d'abord le nom, puis le mot « Normandie »
            "nor        | ANCIENNES | NORD_PAS_DE_CALAIS BASSE_NORMANDIE HAUTE_NORMANDIE",
            // un mot du nom, pas son début : chaque groupe trié, Centre avant Pays
            "loire      | NOUVELLES | CENTRE_VAL_DE_LOIRE PAYS_DE_LA_LOIRE",
            // d'abord le nom qui commence par la saisie, puis ceux dont un mot commence par elle
            "normandie  | ANCIENNES | BASSE_NORMANDIE HAUTE_NORMANDIE",
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
        for (ModeRegions mode : ModeRegions.values()) {
            assertEquals(List.of(), RegionFr.suggestions("", mode));
            assertEquals(List.of(), RegionFr.suggestions("  -' ", mode));
            assertEquals(List.of(), RegionFr.suggestions("xyz", mode));
            assertEquals(List.of(), RegionFr.suggestions("de", mode));
            assertEquals(List.of(), RegionFr.suggestions("d", mode));
        }
    }

    @ParameterizedTest
    @EnumSource(ModeRegions.class)
    @DisplayName("Chaque région du mode est la première suggestion pour son nom complet, et elle est reconnue")
    void premiereSuggestion(ModeRegions mode) {
        for (RegionFr region : RegionFr.tirables(mode)) {
            List<RegionFr> suggestions = RegionFr.suggestions(region.getNom(), mode);
            assertEquals(region, suggestions.get(0), region.name());
            // une suggestion choisie doit toujours être acceptée par chercher() : les deux règles restent d'accord
            for (RegionFr suggestion : suggestions) {
                assertEquals(Optional.of(suggestion), RegionFr.chercher(suggestion.getNom(), mode));
            }
        }
    }

    @ParameterizedTest(name = "mode {0} : jamais de région {1}")
    @CsvSource({"NOUVELLES, ANCIENNE", "ANCIENNES, NOUVELLE"})
    @DisplayName("Jamais de région de l'autre époque suggérée")
    void suggestionsSelonLeMode(ModeRegions mode, Epoque interdite) {
        for (char lettre = 'a'; lettre <= 'z'; lettre++) {
            for (RegionFr region : RegionFr.suggestions(String.valueOf(lettre), mode)) {
                assertNotEquals(interdite, region.getEpoque(), lettre + " → " + region);
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
    @CsvSource({"NOUVELLES, 18", "ANCIENNES, 27"})
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
            assertTrue(mode.contient(region));
        }
    }

    @Test
    @DisplayName("Chaque région, ancienne comme nouvelle, a son nom et ses 5 indices dans les deux langues, et son image")
    void indicesRemplis() {
        for (RegionFr region : RegionFr.values()) {
            assertTrue(region.getSuperficie() > 0, region.getNom());
            assertFalse(region.getCapitale().isBlank(), region.getNom());
            for (Langue langue : Langue.values()) {
                assertFalse(region.getNom(langue).isBlank(), region.getNom());
                assertFalse(region.getSpecialite().dans(langue).isBlank(), region.getNom());
                assertFalse(region.getPersonnalite().dans(langue).isBlank(), region.getNom());
                assertFalse(region.getMonument().dans(langue).isBlank(), region.getNom());
            }
            assertNotNull(region.getImage(), region.getNom());
        }
    }

    @Test
    @DisplayName("Deux régions n'ont jamais le même fichier d'image")
    void imagesDistinctes() {
        assertEquals(RegionFr.values().length,
                Arrays.stream(RegionFr.values()).map(RegionFr::getImage).distinct().count());
    }

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource(delimiter = '|', value = {
            "HAUTS_DE_FRANCE         | NORD_PAS_DE_CALAIS PICARDIE",
            "NORMANDIE               | HAUTE_NORMANDIE BASSE_NORMANDIE",
            "GRAND_EST               | ALSACE CHAMPAGNE_ARDENNE LORRAINE",
            "BOURGOGNE_FRANCHE_COMTE | BOURGOGNE FRANCHE_COMTE",
            "AUVERGNE_RHONE_ALPES    | AUVERGNE RHONE_ALPES",
            "NOUVELLE_AQUITAINE      | AQUITAINE LIMOUSIN POITOU_CHARENTES",
            "OCCITANIE               | LANGUEDOC_ROUSSILLON MIDI_PYRENEES",
            "CENTRE_VAL_DE_LOIRE     | CENTRE",
    })
    @DisplayName("La superficie d'une nouvelle région est la somme de celles qu'elle a réunies (à 2 km² près : arrondis)")
    void superficiesCoherentes(RegionFr nouvelle, String anciennes) {
        int somme = Arrays.stream(anciennes.split(" ")).map(RegionFr::valueOf).mapToInt(RegionFr::getSuperficie).sum();
        assertEquals(nouvelle.getSuperficie(), somme, 2);
    }

    @Test
    @DisplayName("Les noms anglais sont acceptés, quelle que soit la langue choisie")
    void nomsAnglais() {
        assertEquals(Optional.of(RegionFr.BRETAGNE), RegionFr.chercher("brittany", ModeRegions.NOUVELLES));
        assertEquals(Optional.of(RegionFr.CORSE), RegionFr.chercher("Corsica", ModeRegions.ANCIENNES));
        assertEquals(Optional.of(RegionFr.BASSE_NORMANDIE), RegionFr.chercher("Lower Normandy", ModeRegions.ANCIENNES));
    }

    @Test
    @DisplayName("L'apostrophe typographique (’) est ignorée comme l'apostrophe droite")
    void apostropheTypographique() {
        assertEquals(Optional.of(RegionFr.PROVENCE_ALPES_COTE_D_AZUR),
                RegionFr.chercher("Provence-Alpes-Côte d’Azur", ModeRegions.NOUVELLES));
    }

    @Test
    @DisplayName("Deux régions différentes n'ont jamais le même nom simplifié, dans aucune langue")
    void nomsDistincts() {
        java.util.Map<String, RegionFr> vus = new java.util.HashMap<>();
        for (RegionFr region : RegionFr.values()) {
            for (Langue langue : Langue.values()) {
                RegionFr deja = vus.putIfAbsent(RegionFr.simplifier(region.getNom(langue)), region);
                assertTrue(deja == null || deja == region, region + " et " + deja);
            }
        }
    }

    @Test
    @DisplayName("Suggestions : les deux noms sont cherchés, et chaque liste est triée dans la langue du joueur")
    void suggestionsBilingues() {
        // « bri » : Brittany (la Bretagne), même pour un joueur francophone
        assertEquals(List.of(RegionFr.BRETAGNE), RegionFr.suggestions("bri", ModeRegions.NOUVELLES, Langue.FR));
        // « bur » : Burgundy-Franche-Comté (la Bourgogne-Franche-Comté)
        assertEquals(List.of(RegionFr.BOURGOGNE_FRANCHE_COMTE),
                RegionFr.suggestions("bur", ModeRegions.NOUVELLES, Langue.EN));
        // « co » : Corse / Corsica. En anglais, Corsica avant… rien d'autre ; en français, Corse seule aussi
        assertTrue(RegionFr.suggestions("co", ModeRegions.NOUVELLES, Langue.EN).contains(RegionFr.CORSE));
    }
}
