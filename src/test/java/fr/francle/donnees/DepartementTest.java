package fr.francle.donnees;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Les départements et le découpage des régions, lus dans resources/departements/departements.json
class DepartementTest {

    @Test
    @DisplayName("101 départements, chacun avec son propre numéro et son propre nom")
    void centUn() {
        List<Departement> tous = Departement.tous();

        assertEquals(101, tous.size());
        assertEquals(101, tous.stream().map(Departement::code).distinct().count());
        assertEquals(101, tous.stream().map(Departement::nom).distinct().count());
    }

    // le cœur du découpage : à chaque époque, la France est découpée sans trou ni recouvrement
    @ParameterizedTest(name = "{0}")
    @EnumSource(ModeRegions.class)
    @DisplayName("Dans chaque mode, chaque département est dans une région et une seule")
    void unDecoupage(ModeRegions mode) {
        List<Departement> vus = new ArrayList<>();
        for (RegionFr region : RegionFr.tirables(mode)) {
            vus.addAll(region.getDepartements());
        }

        assertEquals(101, vus.size(), "un département compté deux fois, ou oublié");
        assertEquals(Set.copyOf(Departement.tous()), new HashSet<>(vus));
    }

    @Test
    @DisplayName("Toutes les régions ont des départements, l'outre-mer un seul")
    void chaqueRegion() {
        for (RegionFr region : RegionFr.values()) {
            int attendu = region.getZone() == Zone.OUTRE_MER ? 1 : 2;
            assertTrue(region.getDepartements().size() >= attendu, region.name());
        }
        assertEquals(1, RegionFr.MAYOTTE.getDepartements().size());
        assertEquals(13, RegionFr.OCCITANIE.getDepartements().size());
    }

    @Test
    @DisplayName("Une nouvelle région réunit exactement les départements des anciennes qu'elle remplace")
    void fusions() {
        Set<Departement> anciennes = new HashSet<>(RegionFr.MIDI_PYRENEES.getDepartements());
        anciennes.addAll(RegionFr.LANGUEDOC_ROUSSILLON.getDepartements());

        assertEquals(anciennes, Set.copyOf(RegionFr.OCCITANIE.getDepartements()));
    }

    @Test
    @DisplayName("Le nom des fichiers d'une région est celui de son image, sans « .png »")
    void fichier() {
        assertEquals("provence-alpes-cote-dazur", RegionFr.PROVENCE_ALPES_COTE_D_AZUR.getFichier());
        assertEquals("bretagne", RegionFr.BRETAGNE.getFichier());
    }

    @ParameterizedTest(name = "« {0} » → {1}")
    @CsvSource(delimiter = '|', value = {
            "29             | 29",
            "Finistère      | 29",
            "  finistere    | 29",
            "2a             | 2A",
            "corse du sud   | 2A",
            "1              | 01",
            "01             | 01",
            "971            | 971",
            "territoire de belfort | 90",
            "Finistère (29) | 29",
            "Corse-du-Sud (2A) | 2A",
            "corse du sud 2a | 2A",
            "La Réunion (974) | 974",
    })
    @DisplayName("chercher reconnaît un département par son nom ou son numéro")
    void chercher(String saisie, String code) {
        assertEquals(Departement.parCode(code), Departement.chercher(saisie));
        assertTrue(Departement.parCode(code).isPresent());
    }

    @ParameterizedTest(name = "« {0} »")
    @CsvSource({"Bretagne", "0", "20", "975", "''", "Paris 2", "Finistère (22)"})
    @DisplayName("chercher ne reconnaît ni une région, ni un numéro qui n'existe pas")
    void inconnu(String saisie) {
        assertEquals(Optional.empty(), Departement.chercher(saisie));
    }

    @Test
    @DisplayName("Les suggestions : d'abord les noms qui commencent par la saisie, puis les mots")
    void suggestions() {
        List<Departement> corse = RegionFr.CORSE.getDepartements();
        List<Departement> bretagne = RegionFr.BRETAGNE.getDepartements();

        assertEquals(List.of("Corse-du-Sud", "Haute-Corse"), noms(Departement.suggestions("corse", corse, Langue.FR)));
        assertEquals(List.of("Haute-Corse"), noms(Departement.suggestions("haute", corse, Langue.FR)));
        // seulement parmi les départements donnés : la Charente-Maritime n'est pas en Bretagne
        assertEquals(List.of("Côtes-d'Armor"), noms(Departement.suggestions("c", bretagne, Langue.FR)));
        assertEquals(List.of(), Departement.suggestions("", bretagne, Langue.FR));
    }

    @Test
    @DisplayName("Le nom et le numéro, comme dans les suggestions du bonus : « Finistère (29) »")
    void nomEtNumero() {
        assertEquals("Finistère (29)", Departement.parCode("29").orElseThrow().nomEtNumero());
        assertEquals("Corse-du-Sud (2A)", Departement.parCode("2A").orElseThrow().nomEtNumero());
        // chaque suggestion se relit comme son département
        Departement.tous().forEach(departement ->
                assertEquals(Optional.of(departement), Departement.chercher(departement.nomEtNumero())));
    }

    private static List<String> noms(List<Departement> departements) {
        return departements.stream().map(Departement::nom).toList();
    }
}
