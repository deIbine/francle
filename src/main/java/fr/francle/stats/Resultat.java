package fr.francle.stats;

import fr.francle.donnees.Departement;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Partie;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
Une partie du jour terminée : sa date, la région à trouver, les régions proposées dans l'ordre, et comment elle a fini.

bonus (depuis la Phase 9) : les numéros des départements donnés au bonus, dans l'ordre (cliqués ou nommés), ou null
si le bonus n'a pas été joué. Comme pour la partie, on ne garde que les réponses : le bonus se reconstruit en les
rejouant (rejouerBonus). Un fichier écrit avant la Phase 9 n'a pas ce champ : Jackson le laisse à null, « pas de
bonus », ce qui est exactement vrai. Et une version plus ancienne du jeu qui lirait un fichier récent ignorerait ce
champ inconnu (FAIL_ON_UNKNOWN_PROPERTIES est désactivé dans Sauvegarde) : le format reste lisible dans les deux
sens, sans changer de numéro de version.
 */
public record Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat,
                       List<String> bonus) {

    // constructeur compact : il s'exécute avant de remplir les champs. List.copyOf fait une copie non modifiable,
    // pour que personne ne puisse changer un résultat après coup (et refuse une liste absente ou avec des trous)
    public Resultat {
        propositions = List.copyOf(propositions);
        bonus = bonus == null ? null : List.copyOf(bonus);
    }

    // une partie sans bonus (perdue, abandonnée, ou gagnée sans avoir joué le bonus)
    public Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat) {
        this(date, region, propositions, etat, null);
    }

    public boolean victoire() {
        return etat == Partie.Etat.GAGNEE;
    }

    // le bonus reconstruit en rejouant ses réponses, s'il a été joué. Il est toujours fini : enregistré en cours
    // (jeu fermé au milieu), il compte comme passé avec les réponses déjà données. On ne reprend pas un bonus
    public Optional<Bonus> rejouerBonus() {
        if (bonus == null) {
            return Optional.empty();
        }
        Bonus rejoue = Bonus.duJour(region, date);
        for (String code : bonus) {
            // un numéro inconnu (fichier modifié à la main) est ignoré ; une réponse en trop aussi
            Optional<Departement> departement = Departement.parCode(code);
            if (departement.isPresent() && rejoue.enCours()) {
                rejoue.repondre(departement.get());
            }
        }
        if (rejoue.enCours()) {
            rejoue.passer();
        }
        return Optional.of(rejoue);
    }
}
