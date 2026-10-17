package fr.francle.stats;

import fr.francle.donnees.Departement;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.ModesDifficiles;
import fr.francle.jeu.Partie;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
Une partie du jour terminée : sa date, la région à trouver, les régions proposées dans l'ordre, et comment elle a fini.

Le bonus des départements, s'il a été joué : comme pour la partie, on ne garde que les réponses, et le bonus se
reconstruit en les rejouant (rejouerBonus). Deux formats, comme dans la version web :
    bonus   (Phase 9) les numéros des départements donnés, dans l'ordre du jour : le département demandé s'en déduit,
            puisque le mode A pose les questions dans cet ordre
    paires  (depuis la Phase 10) [numéro demandé, numéro donné], dans l'ordre des réponses : en mode C, le joueur
            choisit lui-même l'ordre, il faut donc garder aussi le département qu'il a choisi
On n'écrit plus que des paires ; les deux se lisent. Un fichier écrit avant la Phase 9 n'a aucun des deux : Jackson
les laisse à null, « pas de bonus », ce qui est exactement vrai. Et une version plus ancienne du jeu qui lirait un
fichier récent ignorerait le champ inconnu (FAIL_ON_UNKNOWN_PROPERTIES est désactivé dans Sauvegarde) : le format
reste lisible dans les deux sens, sans changer de numéro de version.

modes (depuis la Phase 9 aussi, repris de la version web) : les modes difficiles fixés au premier essai, pour le
partage. Même principe : null dans un ancien fichier, ce qui se lit « sans mode difficile connu ».
 */
public record Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat,
                       List<String> bonus, ModesDifficiles modes, List<List<String>> paires) {

    // constructeur compact : il s'exécute avant de remplir les champs. List.copyOf fait une copie non modifiable,
    // pour que personne ne puisse changer un résultat après coup (et refuse une liste absente ou avec des trous)
    public Resultat {
        propositions = List.copyOf(propositions);
        bonus = bonus == null ? null : List.copyOf(bonus);
        // chaque paire copiée elle aussi : une copie de la liste seule laisserait modifier ce qu'elle contient
        paires = paires == null ? null : paires.stream().map(List::copyOf).toList();
    }

    // une partie sans bonus ni mode connu (perdue, abandonnée, ou gagnée sans avoir joué le bonus)
    public Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat) {
        this(date, region, propositions, etat, null, null, null);
    }

    // une partie avec un bonus de l'ancien format (les numéros donnés, dans l'ordre du jour)
    public Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat,
                    List<String> bonus) {
        this(date, region, propositions, etat, bonus, null, null);
    }

    public Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat,
                    List<String> bonus, ModesDifficiles modes) {
        this(date, region, propositions, etat, bonus, modes, null);
    }

    public boolean victoire() {
        return etat == Partie.Etat.GAGNEE;
    }

    // le même résultat, avec les réponses de ce bonus, écrites en paires (l'ancien format disparaît)
    public Resultat avecBonus(Bonus joue) {
        List<List<String>> reponses = joue.getTentatives().stream()
                .map(tentative -> List.of(tentative.demande().code(), tentative.donne().code()))
                .toList();
        return new Resultat(date, region, propositions, etat, null, modes, reponses);
    }

    // le même résultat, avec le bonus d'un autre (celui qui était déjà enregistré pour cette date)
    Resultat avecBonusDe(Resultat autre) {
        return new Resultat(date, region, propositions, etat, autre.bonus, modes, autre.paires);
    }

    // le bonus reconstruit en rejouant ses réponses, s'il a été joué. Il est toujours fini : enregistré en cours
    // (jeu fermé au milieu), il compte comme passé avec les réponses déjà données. On ne reprend pas un bonus.
    // Une réponse impossible (fichier modifié à la main : numéro inconnu, département déjà répondu, réponse en
    // trop) est ignorée
    public Optional<Bonus> rejouerBonus() {
        if (bonus == null && paires == null) {
            return Optional.empty();
        }
        Bonus rejoue = Bonus.duJour(region, date);
        if (paires != null) {
            for (List<String> paire : paires) {
                Optional<Departement> demande = paire.size() == 2 ? Departement.parCode(paire.get(0)) : Optional.empty();
                Optional<Departement> donne = paire.size() == 2 ? Departement.parCode(paire.get(1)) : Optional.empty();
                if (demande.isPresent() && donne.isPresent()) {
                    rejoue.relire(demande.get(), donne.get());
                }
            }
        } else {
            // l'ancien format : la réponse n° i répondait à la question n° i de l'ordre du jour. Les réponses sont
            // reprises telles quelles, quel que soit le mode du bonus d'aujourd'hui (comme dans la version web)
            for (String code : bonus) {
                Optional<Departement> donne = Departement.parCode(code);
                if (donne.isPresent() && rejoue.enCours()) {
                    rejoue.relire(rejoue.getOrdre().get(rejoue.getTentatives().size()), donne.get());
                }
            }
        }
        if (rejoue.enCours()) {
            rejoue.passer();
        }
        return Optional.of(rejoue);
    }
}
