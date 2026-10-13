package fr.francle.jeu;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// La région du jour, vérifiée sur n'importe quelle date : c'est le test qui choisit le jour, pas l'horloge
class RegionDuJourTest {

    private static final LocalDate DEBUT = RegionDuJour.PREMIER_JOUR;
    private static final int TAILLE = RegionFr.tirables(ModeRegions.NOUVELLES).size();

    @Test
    @DisplayName("Numéro de partie à la Wordle : 1 le premier jour, puis +1 par jour")
    void numero() {
        assertEquals(1, RegionDuJour.numero(DEBUT));
        assertEquals(2, RegionDuJour.numero(DEBUT.plusDays(1)));
        assertEquals(366, RegionDuJour.numero(DEBUT.plusYears(1)));
        assertEquals(0, RegionDuJour.numero(DEBUT.minusDays(1)));
    }

    // Valeurs figées le 13 octobre 2026, avant la partie n° 1. Elles ne prouvent pas que le calcul est « juste »
    // (il n'y a pas de bonne réponse en dehors de lui) : elles vérifient qu'il ne CHANGE pas. Si ce test casse,
    // la région du jour a changé pour tout le monde (ordre des régions, graine, mélange modifiés…).
    // Des dates passées seulement, pour ne pas révéler les réponses des parties à venir.
    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "2000-01-01, AUVERGNE_RHONE_ALPES",
            "2024-02-29, LA_REUNION",
            "2026-01-01, NORMANDIE",
            "2026-10-12, HAUTS_DE_FRANCE",
    })
    @DisplayName("Valeurs de référence : le calcul ne doit jamais changer")
    void reference(LocalDate date, RegionFr attendue) {
        assertEquals(attendue, RegionDuJour.pour(date));
    }

    @Test
    @DisplayName("Chaque région revient exactement une fois par cycle, avant comme après le premier jour")
    void uneFoisParCycle() {
        Set<RegionFr> toutes = new HashSet<>(RegionFr.tirables(ModeRegions.NOUVELLES));
        for (int cycle = -30; cycle < 30; cycle++) {
            Set<RegionFr> vues = new HashSet<>();
            for (int jour = 0; jour < TAILLE; jour++) {
                vues.add(RegionDuJour.pour(DEBUT.plusDays((long) cycle * TAILLE + jour)));
            }
            assertEquals(toutes, vues, "cycle " + cycle);
        }
    }

    @Test
    @DisplayName("Jamais deux jours de suite la même région, même d'un cycle à l'autre")
    void jamaisDeuxJoursDeSuite() {
        RegionFr hier = RegionDuJour.pour(DEBUT.minusDays(2001));
        for (int jour = -2000; jour < 2000; jour++) {
            RegionFr aujourdhui = RegionDuJour.pour(DEBUT.plusDays(jour));
            assertNotEquals(hier, aujourdhui, "jour " + jour);
            hier = aujourdhui;
        }
    }

    @Test
    @DisplayName("Uniquement des nouvelles régions, et le même mode pour tout le monde")
    void nouvellesRegions() {
        List<RegionFr> nouvelles = RegionFr.tirables(ModeRegions.NOUVELLES);
        for (int jour = 0; jour < 400; jour++) {
            assertTrue(nouvelles.contains(RegionDuJour.pour(DEBUT.plusDays(jour))));
        }
        assertEquals(ModeRegions.NOUVELLES, RegionDuJour.MODE);
    }

    @Test
    @DisplayName("La même date donne toujours la même région")
    void deterministe() {
        LocalDate date = LocalDate.of(2030, 6, 15);
        assertEquals(RegionDuJour.pour(date), RegionDuJour.pour(LocalDate.of(2030, 6, 15)));
    }
}
