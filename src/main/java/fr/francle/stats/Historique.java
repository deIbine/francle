package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
Tout ce qui est enregistré sur le disque : les parties du jour terminées, et celle du jour en cours (ou null).
Un record, donc non modifiable : chaque changement donne un nouvel Historique (avec(...)).

Les parties ne sont pas enregistrées « telles quelles » : on garde seulement les régions proposées, et on les REJOUE
dans une nouvelle Partie pour la reconstruire. Ce sont donc les règles du jeu elles-mêmes qui recalculent distances,
pourcentages, indices et état : rien à dupliquer, et rien qui puisse se contredire.
 */
public record Historique(int version, List<Resultat> resultats, PartieEnCours enCours) {

    // le numéro du format, écrit dans le fichier : si le format change un jour, on saura de quelle version il vient
    public static final int VERSION = 1;

    public Historique {
        resultats = List.copyOf(resultats);
    }

    public static Historique vide() {
        return new Historique(VERSION, List.of(), null);
    }

    // la partie du jour pour cette date : terminée, en cours, ou neuve si elle n'a pas encore été commencée
    public Partie partieDuJour(LocalDate date) {
        for (Resultat resultat : resultats) {
            if (resultat.date().equals(date)) {
                return rejouer(resultat.region(), resultat.propositions(), true);
            }
        }
        if (enCours != null && enCours.date().equals(date)) {
            return rejouer(RegionDuJour.pour(date), enCours.propositions(), false);
        }
        return new Partie(RegionDuJour.pour(date), RegionDuJour.MODE);
    }

    // l'historique après une action sur la partie du jour de cette date : en cours, elle est retenue telle quelle ;
    // terminée, elle devient un résultat (un seul par date) et il n'y a plus de partie en cours
    public Historique avec(LocalDate date, Partie partie) {
        List<RegionFr> propositions = partie.getEssais().stream().map(Essai::proposition).toList();
        if (partie.enCours()) {
            return new Historique(VERSION, resultats, new PartieEnCours(date, propositions));
        }
        List<Resultat> nouveaux = new ArrayList<>(resultats);
        nouveaux.removeIf(resultat -> resultat.date().equals(date));
        nouveaux.add(new Resultat(date, partie.getCible(), propositions, partie.getEtat()));
        nouveaux.sort(Comparator.comparing(Resultat::date));
        return new Historique(VERSION, nouveaux, null);
    }

    // terminee : la partie était finie quand elle a été enregistrée. Si elle ne l'est pas après les avoir rejouées,
    // c'est qu'elle a été abandonnée… ou que les règles ont changé depuis : une partie perdue en 5 essais avant le
    // passage à 6, ou dont une proposition en double ne compte plus. Elle reste finie : on ne rouvre jamais une
    // partie du jour terminée (le résultat enregistré, lui, garde son état pour les statistiques)
    private static Partie rejouer(RegionFr cible, List<RegionFr> propositions, boolean terminee) {
        Partie partie = new Partie(cible, RegionDuJour.MODE);
        for (RegionFr proposition : propositions) {
            if (partie.enCours()) {
                partie.proposer(proposition.getNom());
            }
        }
        if (terminee && partie.enCours()) {
            partie.abandonner();
        }
        return partie;
    }
}
