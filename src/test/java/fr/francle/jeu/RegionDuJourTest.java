package fr.francle.jeu;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @Test
    @DisplayName("L'angle de la silhouette du jour : le même pour une même date, entre 0 et 359°, et varié")
    void angle() {
        Set<Integer> angles = new HashSet<>();
        LocalDate jour = RegionDuJour.PREMIER_JOUR;
        for (int i = 0; i < 100; i++, jour = jour.plusDays(1)) {
            int angle = RegionDuJour.angle(jour);
            assertTrue(angle >= 0 && angle < 360, jour + " : " + angle);
            assertEquals(angle, RegionDuJour.angle(LocalDate.of(jour.getYear(), jour.getMonth(), jour.getDayOfMonth())));
            angles.add(angle);
        }
        // 100 jours, 360 angles possibles : s'ils étaient presque tous pareils, le hasard serait mal fait
        assertTrue(angles.size() > 50, angles.size() + " angles différents seulement");
    }

    // un fuseau qui change d'heure : Paris passe à l'heure d'hiver le 25 octobre 2026 (3 h -> 2 h), et à l'heure
    // d'été le 29 mars 2026 (2 h -> 3 h)
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    @ParameterizedTest(name = "{0} à {1} h : {2} h avant minuit")
    @CsvSource({
            // un jour ordinaire : 24 heures moins l'heure qu'il est
            "2026-10-24, 1, 23",
            "2026-10-24, 15, 9",
            // le jour de 25 heures : à 1 h, il reste 24 heures (l'heure de 2 h à 3 h passe deux fois)
            "2026-10-25, 1, 24",
            "2026-10-25, 15, 9",
            // le jour de 23 heures : à 1 h, il ne reste que 22 heures (l'heure de 2 h à 3 h n'existe pas)
            "2026-03-29, 1, 22",
            "2026-03-29, 15, 9",
    })
    @DisplayName("Le temps jusqu'à minuit tient compte des changements d'heure")
    void jusquAMinuit(LocalDate date, int heure, int heuresRestantes) {
        ZonedDateTime maintenant = date.atTime(heure, 0).atZone(PARIS);
        assertEquals(Duration.ofHours(heuresRestantes), RegionDuJour.jusquAMinuit(maintenant));
    }

    @Test
    @DisplayName("Juste avant minuit, il reste une seconde ; à minuit pile, une journée entière")
    void autourDeMinuit() {
        ZonedDateTime presque = LocalDate.of(2026, 10, 24).atTime(23, 59, 59).atZone(PARIS);
        assertEquals(Duration.ofSeconds(1), RegionDuJour.jusquAMinuit(presque));
        ZonedDateTime minuit = LocalDate.of(2026, 10, 25).atStartOfDay(PARIS);
        assertEquals(Duration.ofHours(25), RegionDuJour.jusquAMinuit(minuit));
    }
}
