package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Les statistiques calculées à partir de résultats écrits à la main : aucune partie, aucun fichier
class StatistiquesTest {

    private static final LocalDate JOUR = LocalDate.of(2030, 3, 10);

    // un résultat « il y a n jours », en k essais (la région proposée n'a pas d'importance pour les statistiques)
    private static Resultat resultat(int ilYA, int essais, Partie.Etat etat) {
        return new Resultat(JOUR.minusDays(ilYA), RegionFr.BRETAGNE, Collections.nCopies(essais, RegionFr.CORSE), etat);
    }

    private static Resultat victoire(int ilYA, int essais) { return resultat(ilYA, essais, Partie.Etat.GAGNEE); }
    private static Resultat defaite(int ilYA) { return resultat(ilYA, Partie.ESSAIS_MAX, Partie.Etat.PERDUE); }
    private static Resultat abandon(int ilYA) { return resultat(ilYA, 1, Partie.Etat.ABANDONNEE); }

    @Test
    @DisplayName("Aucune partie : tout à zéro, et pas de division par zéro pour le pourcentage")
    void aucunePartie() {
        Statistiques stats = Statistiques.de(List.of(), JOUR);

        assertEquals(0, stats.jouees());
        assertEquals(0, stats.pourcentageVictoires());
        assertEquals(0, stats.serieActuelle());
        assertEquals(0, stats.meilleureSerie());
        assertEquals(List.of(0, 0, 0, 0, 0), stats.repartition());
    }

    @Test
    @DisplayName("Jouées, victoires (arrondies), et répartition par nombre d'essais")
    void comptes() {
        Statistiques stats = Statistiques.de(List.of(victoire(5, 3), victoire(4, 1), defaite(3), victoire(2, 3), abandon(1),
                victoire(0, 5)), JOUR);

        assertEquals(6, stats.jouees());
        assertEquals(4, stats.victoires());
        assertEquals(67, stats.pourcentageVictoires());   // 4 / 6 = 66,7 % : arrondi, pas tronqué
        assertEquals(List.of(1, 0, 2, 0, 1), stats.repartition());
    }

    @Test
    @DisplayName("Série en cours : des victoires sur des jours consécutifs, jusqu'à aujourd'hui")
    void serieEnCours() {
        Statistiques stats = Statistiques.de(List.of(victoire(2, 2), victoire(1, 4), victoire(0, 1)), JOUR);

        assertEquals(3, stats.serieActuelle());
        assertEquals(3, stats.meilleureSerie());
    }

    @Test
    @DisplayName("La série tient encore si la dernière victoire date d'hier (la partie du jour n'est pas encore jouée)")
    void serieDHier() {
        assertEquals(2, Statistiques.de(List.of(victoire(2, 2), victoire(1, 2)), JOUR).serieActuelle());
    }

    @Test
    @DisplayName("Un jour sauté, une défaite ou un abandon interrompent la série")
    void serieInterrompue() {
        // jour sauté (il y a 3 jours, pas de partie) : la série repart de 1
        assertEquals(2, Statistiques.de(List.of(victoire(5, 1), victoire(4, 1), victoire(2, 1), victoire(1, 1)), JOUR)
                .serieActuelle());
        // défaite hier
        assertEquals(0, Statistiques.de(List.of(victoire(2, 1), defaite(1)), JOUR).serieActuelle());
        // abandon aujourd'hui
        assertEquals(0, Statistiques.de(List.of(victoire(1, 1), abandon(0)), JOUR).serieActuelle());
    }

    @Test
    @DisplayName("Dernière partie avant-hier : plus de série en cours, mais la meilleure série est gardée")
    void serieTerminee() {
        Statistiques stats = Statistiques.de(List.of(victoire(4, 1), victoire(3, 1), victoire(2, 1)), JOUR);

        assertEquals(0, stats.serieActuelle());
        assertEquals(3, stats.meilleureSerie());
    }

    @Test
    @DisplayName("Meilleure série : la plus longue, même si elle est ancienne")
    void meilleureSerie() {
        Statistiques stats = Statistiques.de(List.of(victoire(10, 1), victoire(9, 1), victoire(8, 1), victoire(7, 1),
                defaite(6), victoire(1, 1), victoire(0, 1)), JOUR);

        assertEquals(2, stats.serieActuelle());
        assertEquals(4, stats.meilleureSerie());
    }

    @Test
    @DisplayName("L'ordre des résultats donnés n'a pas d'importance")
    void ordreIndifferent() {
        List<Resultat> dansLOrdre = List.of(victoire(2, 1), victoire(1, 1), victoire(0, 1));
        List<Resultat> melanges = List.of(victoire(0, 1), victoire(2, 1), victoire(1, 1));

        assertEquals(Statistiques.de(dansLOrdre, JOUR), Statistiques.de(melanges, JOUR));
    }
}
