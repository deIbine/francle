package fr.francle.jeu;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.donnees.Zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/*
Les règles du jeu, sans aucun affichage : Partie reçoit les saisies du joueur et dit ce qui s'est passé.
C'est l'interface (Francle, Message) qui demande les saisies et affiche les résultats.
 */
public class Partie {

    // 6, comme Worldle et la version web (5 jusqu'à la Phase 6) : un essai de plus pour le cinquième indice
    public static final int ESSAIS_MAX = 6;

    public enum Etat { EN_COURS, GAGNEE, PERDUE, ABANDONNEE }

    private final RegionFr cible;
    private final ModeRegions mode;
    private final List<Essai> essais = new ArrayList<>();
    private Etat etat = Etat.EN_COURS;

    public Partie(RegionFr cible, ModeRegions mode) {
        this.cible = cible;
        this.mode = mode;
    }

    // joue la saisie du joueur et dit ce qui s'est passé : un essai joué, une saisie inconnue, ambiguë, une région de
    // l'autre époque, ou une région déjà proposée. Seul le premier cas coûte un essai.
    // La saisie est le nom d'une région, ou, comme dans la version web, le début d'un nom qui n'en évoque qu'une
    // (« bret » + Entrée joue la Bretagne : c'est la seule suggestion)
    public Reponse proposer(String saisie) {
        verifierEnCours();
        Optional<RegionFr> trouvee = RegionFr.chercher(saisie, mode);
        if (trouvee.isEmpty()) {
            // le nom exact d'une région, mais pas de ce mode : elle est de l'autre époque. On le dit, plutôt que
            // « pas une région reconnue ». Et avant de chercher des suggestions : sinon « Bourgogne », en nouvelles
            // régions, jouerait Bourgogne-Franche-Comté (sa seule suggestion), un essai que le joueur n'a pas voulu
            Optional<RegionFr> autreEpoque = RegionFr.chercher(saisie);
            if (autreEpoque.isPresent()) {
                return new Reponse.AutreEpoque(autreEpoque.get());
            }
            List<RegionFr> evoquees = RegionFr.suggestions(saisie, mode);
            if (evoquees.size() > 1) {
                return new Reponse.Plusieurs();
            }
            if (evoquees.isEmpty()) {
                return new Reponse.Inconnue();
            }
            trouvee = Optional.of(evoquees.get(0));
        }
        RegionFr proposition = trouvee.get();
        if (proposees().contains(proposition)) {
            return new Reponse.DejaProposee(proposition);
        }
        double distance = cible.distance(proposition);
        Essai essai = new Essai(proposition, distance, pourcentage(cible, distance), Direction.entre(proposition, cible));
        essais.add(essai);
        if (proposition == cible) {
            etat = Etat.GAGNEE;
        } else if (essais.size() == ESSAIS_MAX) {
            etat = Etat.PERDUE;
        }
        return new Reponse.Jouee(essai);
    }

    public void abandonner() {
        verifierEnCours();
        etat = Etat.ABANDONNEE;
    }

    // jouer dans une partie terminée est une erreur de programmation, pas une saisie du joueur
    private void verifierEnCours() {
        if (etat != Etat.EN_COURS) {
            throw new IllegalStateException("La partie est terminée : " + etat);
        }
    }

    // les indices révélés jusqu'ici : un par essai raté, dans l'ordre (un essai gagnant n'en révèle pas). Une fois la
    // partie finie (gagnée, perdue ou abandonnée), tous : il n'y a plus rien à deviner, mais encore à apprendre
    public List<Indice> indicesReveles() {
        if (!enCours()) {
            return List.of(Indice.values());
        }
        List<Indice> indices = new ArrayList<>();
        for (int i = 0; i < essais.size(); i++) {
            if (essais.get(i).proposition() != cible) {
                Indice.apresEssai(i).ifPresent(indices::add);
            }
        }
        return indices;
    }

    public RegionFr getCible() { return cible; }
    public ModeRegions getMode() { return mode; }
    public Etat getEtat() { return etat; }
    public boolean enCours() { return etat == Etat.EN_COURS; }
    // une vue en lecture seule : l'interface peut lire les essais, pas en ajouter
    public List<Essai> getEssais() { return Collections.unmodifiableList(essais); }

    // les régions déjà proposées : les rejouer est refusé. L'interface les retire des suggestions et les barre dans la
    // liste des régions
    public Set<RegionFr> proposees() {
        return essais.stream().map(Essai::proposition).collect(Collectors.toUnmodifiableSet());
    }

    // pourcentage de proximité : 100 % à 0 km, 0 % pour la région de référence la plus éloignée de la cible.
    // Pour une cible en métropole, seules les régions de métropole servent de référence : sinon l'outre-mer,
    // à plus de 6000 km, écrase toute la métropole entre 88 % et 98 %.
    static int pourcentage(RegionFr cible, double distance) {
        double distanceMax = 0;
        for (RegionFr autre : RegionFr.values()) {
            if (cible.getZone() == Zone.OUTRE_MER || autre.getZone() == Zone.METROPOLE) {
                distanceMax = Math.max(distanceMax, cible.distance(autre));
            }
        }
        // arrondi et non troncature : (int) 99.9999 donnerait 99 % pour une distance de quelques centimètres
        int pourcentage = (int) Math.round((distanceMax - distance) * 100 / distanceMax);
        // une proposition en outre-mer pour une cible en métropole dépasse la référence : 0 %, pas un pourcentage négatif
        return Math.max(0, pourcentage);
    }
}
