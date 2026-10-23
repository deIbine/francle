package fr.francle.jeu;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Langue;
import fr.francle.donnees.RegionFr;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/*
Le bonus des départements, après chaque victoire (partie du jour ou partie libre) : retrouver tous les départements de
la région, un par un, dans un ordre tiré de la date pour la partie du jour (le même pour tous), au hasard sinon. Une seule réponse par département : juste ou fausse, on passe au suivant.
Comme Partie, sans aucun affichage : la fenêtre transmet les réponses du joueur et affiche ce que le bonus répond.

Quatre sortes de questions :
    CARTE   « Où est le Finistère ? » : le joueur clique sur la carte (mode A, caché)
    NOM     « Quel est ce département ? » : un département est colorié, le joueur écrit son nom ou son numéro
            (mode B, caché)
    CHOIX   le joueur choisit lui-même un département sur la carte, puis écrit son nom ou son numéro (mode C, celui des
            joueurs depuis la phase 10, repris de la version web) : pas de question imposée, ni d'ordre. Une
            Tentative y garde le département choisi (demande) et celui qui a été écrit (donne)
    NUMERO  « Quel est le numéro de la Guadeloupe ? » : pour une région d'un seul département, où cliquer sur le
            seul département de la carte serait gratuit (comme le choisir, puis recopier le nom de la région)

Deux versions du mode C (depuis la phase 13) :
    simple     (celle par défaut) la liste et les suggestions ne proposent que les départements de la région ; un
               département d'une autre région est refusé sans coûter de réponse
    difficile  (une option des réglages) les 101 départements partout ; un département d'une autre région est une
               réponse fausse, qui compte
La version est fixée à la première réponse, comme les modes difficiles de la partie au premier essai : avant, un
changement de réglage compte tout de suite ; après, il attend le bonus suivant. Elle ne change rien aux autres
questions (A et B, cachés, et le numéro d'outre-mer).
 */
public class Bonus {

    public enum Question { CARTE, NOM, CHOIX, NUMERO }

    // A, B ou C : choisi de notre côté, dans le code, sans le montrer aux joueurs. NOM pour essayer le mode B,
    // CHOIX pour le mode C
    public static final Question MODE = Question.CHOIX;

    // TERMINE : tous les départements ont eu leur réponse ; PASSE : le joueur a arrêté avant (bouton « Passer », ou
    // jeu fermé au milieu). Les deux comptent dans les statistiques, avec les réponses données
    public enum Etat { EN_COURS, TERMINE, PASSE }

    private final RegionFr region;
    private final Question question;
    private final List<Departement> ordre;
    private final List<Tentative> tentatives = new ArrayList<>();
    private Etat etat = Etat.EN_COURS;
    // la version difficile (les 101 départements) plutôt que la simple (ceux de la région)
    private boolean difficile;

    // ordre : les départements de la région, dans l'ordre des questions (en mode CHOIX, le joueur choisit le sien :
    // celui-ci ne sert plus qu'à relire les fichiers d'avant la phase 10). mode : CARTE, NOM ou CHOIX, remplacé par
    // NUMERO pour une région d'un seul département. La version simple, celle par défaut
    public Bonus(RegionFr region, List<Departement> ordre, Question mode) {
        this(region, ordre, mode, false);
    }

    public Bonus(RegionFr region, List<Departement> ordre, Question mode, boolean difficile) {
        if (ordre.size() != region.getDepartements().size()
                || !new HashSet<>(ordre).equals(new HashSet<>(region.getDepartements()))) {
            throw new IllegalArgumentException("L'ordre doit contenir chaque département de " + region + " une fois");
        }
        if (mode == Question.NUMERO) {
            throw new IllegalArgumentException("NUMERO n'est pas un mode : il est choisi d'après la région");
        }
        this.region = region;
        this.ordre = List.copyOf(ordre);
        this.question = ordre.size() == 1 ? Question.NUMERO : mode;
        this.difficile = difficile;
    }

    // le bonus de la partie du jour : le même ordre pour tout le monde, et le même après une relance du jeu
    public static Bonus duJour(RegionFr region, LocalDate date) {
        return duJour(region, date, false);
    }

    public static Bonus duJour(RegionFr region, LocalDate date, boolean difficile) {
        return new Bonus(region, ordre(region, date), MODE, difficile);
    }

    // le bonus d'une partie libre : un ordre au hasard, à chaque fois différent (rien n'est enregistré)
    public static Bonus auHasard(RegionFr region, Random hasard, boolean difficile) {
        List<Departement> ordre = new ArrayList<>(region.getDepartements());
        Collections.shuffle(ordre, hasard);
        return new Bonus(region, ordre, MODE, difficile);
    }

    // le joueur change de version dans les réglages : elle compte tout de suite tant que le bonus n'a pas de réponse.
    // Après, elle est fixée (le partage dit avec quelle version le bonus a été joué) : la fenêtre ne doit plus
    // l'appeler, c'est une erreur de programmation
    public void changerVersion(boolean difficile) {
        verifierEnCours();
        if (!tentatives.isEmpty()) {
            throw new IllegalStateException("La version est fixée à la première réponse");
        }
        this.difficile = difficile;
    }

    // les départements que les suggestions (et, en mode CHOIX, la liste) proposent. Mode CHOIX : ceux de la région
    // dans la version simple, les 101 dans la difficile. Mode B : ceux de la région (un département y est déjà
    // colorié : rien n'est donné). Pour un clic ou un numéro, rien à proposer
    public List<Departement> proposes() {
        return switch (question) {
            case CHOIX -> difficile ? Departement.tous() : region.getDepartements();
            case NOM -> region.getDepartements();
            case CARTE, NUMERO -> List.of();
        };
    }

    // les départements mélangés avec une graine tirée de la date, comme l'angle de la silhouette (RegionDuJour.angle).
    // Un sel à lui : sinon le mélange viendrait de la même suite de nombres que l'angle
    static List<Departement> ordre(RegionFr region, LocalDate date) {
        List<Departement> ordre = new ArrayList<>(region.getDepartements());
        Collections.shuffle(ordre, new Random(SEL + RegionDuJour.numero(date)));
        return ordre;
    }

    // les octets ASCII de « Bonus »
    private static final long SEL = 0x426F6E7573L;

    // le département de la question en cours. En mode CHOIX, il n'y en a pas : c'est le joueur qui choisit
    public Departement demande() {
        verifierEnCours();
        if (question == Question.CHOIX) {
            throw new IllegalStateException("Pas de question imposée en mode CHOIX : le joueur choisit le département");
        }
        return ordre.get(tentatives.size());
    }

    // la réponse du joueur : le département cliqué (CARTE), ou celui qu'il a nommé
    public ReponseBonus repondre(Departement donne) {
        return new ReponseBonus.Jouee(ajouter(demande(), donne));
    }

    // une réponse de plus, et le bonus se termine quand chaque département a la sienne
    private Tentative ajouter(Departement demande, Departement donne) {
        Tentative tentative = new Tentative(demande, donne);
        tentatives.add(tentative);
        if (tentatives.size() == ordre.size()) {
            etat = Etat.TERMINE;
        }
        return tentative;
    }

    // mode CHOIX : le joueur a choisi un département sur la carte, et donne un nom. Un département déjà trouvé
    // ailleurs est refusé sans coûter de réponse (le joueur s'est sûrement trompé de département sur la carte), comme
    // une région déjà proposée dans la partie ; un nom déjà donné à tort, lui, reste possible. Un département d'une
    // autre région est refusé lui aussi dans la version simple (les suggestions ne le proposent pas) ; dans la
    // difficile, c'est une réponse fausse, qui compte
    public ReponseBonus repondre(Departement choisi, Departement donne) {
        verifierEnCours();
        if (question != Question.CHOIX) {
            throw new IllegalStateException("Pas de choix libre pour la question " + question);
        }
        // choisir un département hors de la région, ou qui a déjà sa réponse, est une erreur de programmation : la
        // carte ne le permet pas
        if (!restants().contains(choisi)) {
            throw new IllegalArgumentException(choisi.code() + " ne peut pas être choisi");
        }
        if (!difficile && !region.getDepartements().contains(donne)) {
            return new ReponseBonus.HorsRegion(donne);
        }
        if (trouve(donne)) {
            return new ReponseBonus.DejaTrouve(donne);
        }
        return new ReponseBonus.Jouee(ajouter(choisi, donne));
    }

    // mode CHOIX, la réponse écrite : le nom ou le numéro d'un des 101 départements, ou, comme pour les régions, le
    // début d'un nom qui n'évoque qu'un seul des départements proposés (« fin » joue le Finistère) : ceux de la région
    // dans la version simple, les 101 dans la difficile, comme les suggestions. Un nom ou un numéro complet est cherché
    // d'abord parmi les 101 : dans les Pays de la Loire, version simple, « Loire » est le département 42, refusé, et
    // pas un début de Loire-Atlantique. Une saisie qui n'est pas un département, ou qui en évoque plusieurs, ne coûte
    // rien
    public ReponseBonus repondre(Departement choisi, String saisie) {
        Optional<Departement> departement = Departement.chercher(saisie);
        if (departement.isEmpty()) {
            // (la langue ne sert qu'à ranger les suggestions : ici, seul leur nombre compte)
            List<Departement> evoques = Departement.suggestions(saisie, proposes(), Langue.FR);
            if (evoques.size() > 1) {
                return new ReponseBonus.Plusieurs();
            }
            if (evoques.isEmpty()) {
                return new ReponseBonus.Inconnue();
            }
            departement = Optional.of(evoques.get(0));
        }
        return repondre(choisi, departement.get());
    }

    // une réponse relue dans un fichier : le département demandé (ou choisi) et celui qui a été donné, dans l'ordre où
    // le joueur les a joués. Faux si elle est impossible (fichier modifié à la main : un département hors de la
    // région, ou qui a déjà sa réponse, ou un bonus déjà fini) ; elle est alors ignorée
    public boolean relire(Departement demande, Departement donne) {
        if (!enCours() || !restants().contains(demande)) {
            return false;
        }
        ajouter(demande, donne);
        return true;
    }

    // les départements qui n'ont pas encore de réponse : ceux que le joueur peut encore choisir en mode CHOIX
    public List<Departement> restants() {
        return ordre.stream().filter(departement -> tentative(departement).isEmpty()).toList();
    }

    // vrai si ce bonus a sa liste des départements (menu Jeu, Ctrl+L) : en mode CHOIX seulement, où l'on choisit sur
    // la carte, puis on nomme. Pas pour la question du numéro, en outre-mer : la liste, qui montre les numéros, donnerait
    // la réponse ; la commande y reste celle des régions
    public boolean avecListe() {
        return question == Question.CHOIX;
    }

    // les départements de la région qui ont déjà leur réponse, juste ou fausse : dans la liste, ils sont barrés. Les
    // donner ailleurs ne peut être que faux (ou refusé, pour un département trouvé), et leur nom est déjà sur la carte
    public List<Departement> repondus() {
        return tentatives.stream().map(Tentative::demande).toList();
    }

    // vrai si ce département a déjà été trouvé (donné au bon endroit)
    public boolean trouve(Departement departement) {
        return tentatives.stream().anyMatch(tentative -> tentative.juste() && tentative.donne().equals(departement));
    }

    // la réponse écrite par le joueur. NOM : le nom ou le numéro d'un département ; NUMERO : un numéro seulement
    // (« Guadeloupe » à la question « Quel est le numéro de la Guadeloupe ? » ne répond à rien). Une saisie qui
    // n'est pas un département ne coûte rien, comme une région inconnue dans la partie
    public ReponseBonus repondre(String saisie) {
        verifierEnCours();
        Optional<Departement> departement = question == Question.NUMERO
                ? Departement.parCode(Departement.numero(saisie))
                : Departement.chercher(saisie);
        return departement.map(this::repondre).orElseGet(ReponseBonus.Inconnue::new);
    }

    // le joueur arrête là : les départements sans réponse sont comptés comme non trouvés
    public void passer() {
        verifierEnCours();
        etat = Etat.PASSE;
    }

    private void verifierEnCours() {
        if (etat != Etat.EN_COURS) {
            throw new IllegalStateException("Le bonus est terminé : " + etat);
        }
    }

    // le nombre de départements trouvés
    public int score() {
        return (int) tentatives.stream().filter(Tentative::juste).count();
    }

    // le nombre de départements à trouver
    public int total() {
        return ordre.size();
    }

    // le rang de la question en cours, à partir de 1 (« 3/12 »)
    public int rang() {
        return tentatives.size() + 1;
    }

    // la réponse donnée pour ce département, s'il a déjà été demandé
    public Optional<Tentative> tentative(Departement departement) {
        return tentatives.stream().filter(tentative -> tentative.demande().equals(departement)).findFirst();
    }

    public RegionFr getRegion() { return region; }
    public Question getQuestion() { return question; }
    public Etat getEtat() { return etat; }
    public boolean enCours() { return etat == Etat.EN_COURS; }
    public boolean isDifficile() { return difficile; }
    public List<Departement> getOrdre() { return ordre; }
    public List<Tentative> getTentatives() { return Collections.unmodifiableList(tentatives); }
}
