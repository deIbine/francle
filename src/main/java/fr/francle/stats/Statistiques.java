package fr.francle.stats;

import fr.francle.jeu.Partie;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
Les statistiques à la Wordle, calculées à partir des résultats des parties du jour (jamais enregistrées elles-mêmes :
on les recalcule à chaque fois, elles ne peuvent donc pas se désynchroniser de l'historique).

  repartition : le nombre de victoires en 1, 2, 3, 4 et 5 essais (repartition.get(0) = victoires en 1 essai)
  série       : des victoires sur des jours CONSÉCUTIFS. Une défaite, un abandon ou un jour sans partie l'interrompt.
 */
public record Statistiques(int jouees, int victoires, int serieActuelle, int meilleureSerie, List<Integer> repartition) {

    public Statistiques {
        repartition = List.copyOf(repartition);
    }

    // arrondi : 2 victoires sur 3 donnent 67 %, pas 66 %
    public int pourcentageVictoires() {
        return jouees == 0 ? 0 : (int) Math.round(victoires * 100.0 / jouees);
    }

    public static Statistiques de(List<Resultat> resultats, LocalDate aujourdhui) {
        List<Resultat> parDate = new ArrayList<>(resultats);
        parDate.sort(Comparator.comparing(Resultat::date));

        List<Integer> repartition = new ArrayList<>(Collections.nCopies(Partie.ESSAIS_MAX, 0));
        int victoires = 0;
        int serie = 0;
        int meilleureSerie = 0;
        LocalDate veille = null;     // la date du résultat précédent
        for (Resultat resultat : parDate) {
            if (resultat.victoire()) {
                victoires++;
                int essais = resultat.propositions().size();
                repartition.set(essais - 1, repartition.get(essais - 1) + 1);
                // la série continue si le résultat précédent était une victoire la veille ; sinon elle repart de 1
                boolean lendemain = veille != null && veille.plusDays(1).equals(resultat.date());
                serie = lendemain && serie > 0 ? serie + 1 : 1;
                meilleureSerie = Math.max(meilleureSerie, serie);
            } else {
                serie = 0;
            }
            veille = resultat.date();
        }
        // la série n'est « en cours » que si la dernière partie date d'aujourd'hui ou d'hier : sinon, un jour a été sauté
        boolean recente = veille != null && !veille.isBefore(aujourdhui.minusDays(1));
        int serieActuelle = recente ? serie : 0;
        return new Statistiques(parDate.size(), victoires, serieActuelle, meilleureSerie, repartition);
    }
}
