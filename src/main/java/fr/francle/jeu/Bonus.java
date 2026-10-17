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
    CARTE   « Où est le Finistère ? » : le joueur clique sur la carte (mode A, celui des joueurs)
    NOM     « Quel est ce département ? » : un département est colorié, le joueur écrit son nom ou son numéro (mode B)
    CHOIX   le joueur choisit lui-même un département sur la carte, puis écrit son nom ou son numéro (mode C, repris
            de la version web) : pas de question imposée, ni d'ordre. Une Tentative y garde le département choisi
            (demande) et celui qui a été écrit (donne)
    NUMERO  « Quel est le numéro de la Guadeloupe ? » : pour une région d'un seul département, où cliquer sur le
            seul département de la carte serait gratuit (comme le choisir, puis recopier le nom de la région)
 */
public class Bonus {

    public enum Question { CARTE, NOM, CHOIX, NUMERO }

    // A, B ou C : choisi de notre côté, dans le code, sans le montrer aux joueurs. NOM pour essayer le mode B,
    // CHOIX pour le mode C
    public static final Question MODE = Question.CARTE;

    // TERMINE : tous les départements ont eu leur réponse ; PASSE : le joueur a arrêté avant (bouton « Passer », ou
    // jeu fermé au milieu). Les deux comptent dans les statistiques, avec les réponses données
    public enum Etat { EN_COURS, TERMINE, PASSE }

    private final RegionFr region;
    private final Question question;
    private final List<Departement> ordre;
    private final List<Tentative> tentatives = new ArrayList<>();
    private Etat etat = Etat.EN_COURS;

    // ordre : les départements de la région, dans l'ordre des questions (en mode CHOIX, le joueur choisit le sien :
    // celui-ci ne sert plus qu'à relire les fichiers d'avant la phase 10). mode : CARTE, NOM ou CHOIX, remplacé par
    // NUMERO pour une région d'un seul département
    public Bonus(RegionFr region, List<Departement> ordre, Question mode) {
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
    }

    // le bonus de la partie du jour : le même ordre pour tout le monde, et le même après une relance du jeu
    public static Bonus duJour(RegionFr region, LocalDate date) {
        return new Bonus(region, ordre(region, date), MODE);
    }

    // le bonus d'une partie libre : un ordre au hasard, à chaque fois différent (rien n'est enregistré)
    public static Bonus auHasard(RegionFr region, Random hasard) {
        List<Departement> ordre = new ArrayList<>(region.getDepartements());
        Collections.shuffle(ordre, hasard);
        return new Bonus(region, ordre, MODE);
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
    // autre région est une réponse fausse, qui compte
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
        if (trouve(donne)) {
            return new ReponseBonus.DejaTrouve(donne);
        }
        return new ReponseBonus.Jouee(ajouter(choisi, donne));
    }

    // mode CHOIX, la réponse écrite : le nom ou le numéro d'un des 101 départements, ou, comme pour les régions, le
    // début d'un nom qui n'en évoque qu'un (« fin » joue le Finistère). Parmi les 101, et pas seulement ceux de la
    // région : sinon, la liste des suggestions donnerait la réponse. Une saisie qui n'est pas un département, ou qui
    // en évoque plusieurs, ne coûte rien
    public ReponseBonus repondre(Departement choisi, String saisie) {
        Optional<Departement> departement = Departement.chercher(saisie);
        if (departement.isEmpty()) {
            // (la langue ne sert qu'à ranger les suggestions : ici, seul leur nombre compte)
            List<Departement> evoques = Departement.suggestions(saisie, Departement.tous(), Langue.FR);
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
    public List<Departement> getOrdre() { return ordre; }
    public List<Tentative> getTentatives() { return Collections.unmodifiableList(tentatives); }
}
