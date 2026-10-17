package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Essai;
import fr.francle.jeu.ModesDifficiles;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
Tout ce qui est enregistré sur le disque : les parties du jour terminées (avec leur bonus), et celle du jour en cours
(ou null).
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
        return avec(date, partie, modesDuJour(date).orElse(null));
    }

    // la même, avec les modes difficiles de la partie (fixés au premier essai), retenus pour le partage
    public Historique avec(LocalDate date, Partie partie, ModesDifficiles modes) {
        List<RegionFr> propositions = partie.getEssais().stream().map(Essai::proposition).toList();
        if (partie.enCours()) {
            return new Historique(VERSION, resultats, new PartieEnCours(date, propositions, modes));
        }
        Resultat termine = new Resultat(date, partie.getCible(), propositions, partie.getEtat(), null, modes);
        // le bonus déjà enregistré pour cette date, s'il y en a un, est gardé
        Optional<Resultat> avant = resultat(date);
        return remplacer(avant.map(termine::avecBonusDe).orElse(termine), null);
    }

    // les modes difficiles de la partie du jour de cette date, terminée ou en cours, s'ils sont connus
    public Optional<ModesDifficiles> modesDuJour(LocalDate date) {
        Optional<ModesDifficiles> termine = resultat(date).map(Resultat::modes);
        if (termine.isPresent()) {
            return termine;
        }
        return Optional.ofNullable(enCours).filter(partie -> partie.date().equals(date)).map(PartieEnCours::modes);
    }

    // l'historique après une réponse au bonus de la partie du jour gagnée à cette date (ou après l'avoir passé).
    // Un bonus sans aucune réponse n'a pas commencé : il n'est pas enregistré, et reste à jouer
    public Historique avecBonus(LocalDate date, Bonus bonus) {
        Resultat resultat = resultat(date).filter(Resultat::victoire)
                .orElseThrow(() -> new IllegalStateException("Pas de partie du jour gagnée le " + date));
        if (bonus.getTentatives().isEmpty()) {
            return this;
        }
        return remplacer(resultat.avecBonus(bonus), enCours);
    }

    // le bonus de la partie du jour de cette date, s'il a été joué (toujours fini : voir Resultat.rejouerBonus)
    public Optional<Bonus> bonusDuJour(LocalDate date) {
        return resultat(date).flatMap(Resultat::rejouerBonus);
    }

    private Optional<Resultat> resultat(LocalDate date) {
        return resultats.stream().filter(resultat -> resultat.date().equals(date)).findFirst();
    }

    // les résultats où celui de la même date est remplacé par le nouveau (un seul résultat par date), triés par date
    private Historique remplacer(Resultat nouveau, PartieEnCours partieEnCours) {
        List<Resultat> nouveaux = new ArrayList<>(resultats);
        nouveaux.removeIf(resultat -> resultat.date().equals(nouveau.date()));
        nouveaux.add(nouveau);
        nouveaux.sort(Comparator.comparing(Resultat::date));
        return new Historique(VERSION, nouveaux, partieEnCours);
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
