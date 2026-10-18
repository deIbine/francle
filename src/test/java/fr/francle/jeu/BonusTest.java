package fr.francle.jeu;

import fr.francle.donnees.Departement;
import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Les règles du bonus des départements
class BonusTest {

    private static final LocalDate JOUR = LocalDate.of(2030, 3, 10);

    private static Departement departement(String code) {
        return Departement.parCode(code).orElseThrow();
    }

    // la Bretagne dans l'ordre des numéros : 22, 29, 35, 56
    private static Bonus bretagne(Bonus.Question mode) {
        return new Bonus(RegionFr.BRETAGNE, RegionFr.BRETAGNE.getDepartements(), mode);
    }

    @Test
    @DisplayName("L'ordre du jour : tous les départements de la région, le même pour la même date")
    void ordreDuJour() {
        List<Departement> ordre = Bonus.ordre(RegionFr.OCCITANIE, JOUR);

        assertEquals(new HashSet<>(RegionFr.OCCITANIE.getDepartements()), new HashSet<>(ordre));
        assertEquals(13, ordre.size());
        assertEquals(ordre, Bonus.ordre(RegionFr.OCCITANIE, JOUR));
        assertEquals(ordre, Bonus.duJour(RegionFr.OCCITANIE, JOUR).getOrdre());
    }

    @Test
    @DisplayName("L'ordre change d'un jour à l'autre (au moins une fois sur une semaine)")
    void ordreChange() {
        List<Departement> premier = Bonus.ordre(RegionFr.OCCITANIE, JOUR);
        boolean change = false;
        for (int jour = 1; jour < 7; jour++) {
            change |= !premier.equals(Bonus.ordre(RegionFr.OCCITANIE, JOUR.plusDays(jour)));
        }
        assertTrue(change);
    }

    @Test
    @DisplayName("Partie libre : un ordre au hasard, avec tous les départements (anciennes régions comprises)")
    void auHasard() {
        Bonus bonus = Bonus.auHasard(RegionFr.LANGUEDOC_ROUSSILLON, new java.util.Random(1));

        assertEquals(new HashSet<>(RegionFr.LANGUEDOC_ROUSSILLON.getDepartements()), new HashSet<>(bonus.getOrdre()));
        assertEquals(Bonus.MODE, bonus.getQuestion());
        // la même graine donne le même ordre : le hasard est reproductible quand on le veut (ici, pour le test)
        assertEquals(bonus.getOrdre(), Bonus.auHasard(RegionFr.LANGUEDOC_ROUSSILLON, new java.util.Random(1)).getOrdre());
    }

    @Test
    @DisplayName("Un ordre qui n'est pas celui des départements de la région est refusé")
    void ordreInvalide() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bonus(RegionFr.BRETAGNE, List.of(departement("22"), departement("29")), Bonus.Question.CARTE));
        assertThrows(IllegalArgumentException.class, () -> new Bonus(RegionFr.BRETAGNE,
                List.of(departement("22"), departement("29"), departement("35"), departement("44")), Bonus.Question.CARTE));
        assertThrows(IllegalArgumentException.class,
                () -> new Bonus(RegionFr.BRETAGNE, RegionFr.BRETAGNE.getDepartements(), Bonus.Question.NUMERO));
    }

    @Test
    @DisplayName("Une réponse par département, juste ou fausse, puis le suivant ; fini après le dernier")
    void unClicParDepartement() {
        Bonus bonus = bretagne(Bonus.Question.CARTE);

        assertEquals(departement("22"), bonus.demande());
        assertEquals(new ReponseBonus.Jouee(new Tentative(departement("22"), departement("22"))),
                bonus.repondre(departement("22")));
        assertEquals(departement("29"), bonus.demande());
        assertEquals(2, bonus.rang());
        bonus.repondre(departement("56"));          // faux
        bonus.repondre(departement("35"));
        assertTrue(bonus.enCours());
        bonus.repondre(departement("56"));

        assertEquals(Bonus.Etat.TERMINE, bonus.getEtat());
        assertEquals(3, bonus.score());
        assertEquals(4, bonus.total());
        assertFalse(bonus.tentative(departement("29")).orElseThrow().juste());
        assertThrows(IllegalStateException.class, bonus::demande);
        assertThrows(IllegalStateException.class, () -> bonus.repondre(departement("22")));
    }

    @Test
    @DisplayName("Passer arrête le bonus : les départements sans réponse ne sont pas trouvés")
    void passer() {
        Bonus bonus = bretagne(Bonus.Question.CARTE);
        bonus.repondre(departement("22"));

        bonus.passer();

        assertEquals(Bonus.Etat.PASSE, bonus.getEtat());
        assertEquals(1, bonus.score());
        assertEquals(Optional.empty(), bonus.tentative(departement("29")));
        assertThrows(IllegalStateException.class, bonus::passer);
    }

    @Test
    @DisplayName("Mode B : le nom ou le numéro ; une saisie qui n'est pas un département ne coûte rien")
    void modeNom() {
        Bonus bonus = bretagne(Bonus.Question.NOM);

        assertEquals(Bonus.Question.NOM, bonus.getQuestion());
        assertEquals(new ReponseBonus.Inconnue(), bonus.repondre("Bretagne"));
        assertEquals(1, bonus.rang());
        assertTrue(bonus.repondre("cotes d'armor") instanceof ReponseBonus.Jouee jouee && jouee.tentative().juste());
        assertTrue(bonus.repondre("29") instanceof ReponseBonus.Jouee jouee && jouee.tentative().juste());
        // un département d'une autre région est une réponse, fausse
        assertTrue(bonus.repondre("Gironde") instanceof ReponseBonus.Jouee jouee && !jouee.tentative().juste());
        assertEquals(2, bonus.score());
    }

    @Test
    @DisplayName("Une région d'un seul département : on demande le numéro, et seulement le numéro")
    void numero() {
        Bonus bonus = Bonus.duJour(RegionFr.GUADELOUPE, JOUR);

        assertEquals(Bonus.Question.NUMERO, bonus.getQuestion());
        assertEquals(1, bonus.total());
        assertEquals(new ReponseBonus.Inconnue(), bonus.repondre("Guadeloupe"));
        assertEquals(new ReponseBonus.Inconnue(), bonus.repondre("975"));
        assertTrue(bonus.repondre(" 971 ") instanceof ReponseBonus.Jouee jouee && jouee.tentative().juste());
        assertEquals(Bonus.Etat.TERMINE, bonus.getEtat());
        assertEquals(1, bonus.score());
    }

    @Test
    @DisplayName("Un mauvais numéro en outre-mer est une réponse fausse")
    void mauvaisNumero() {
        Bonus bonus = Bonus.duJour(RegionFr.MAYOTTE, JOUR);

        bonus.repondre("974");

        assertEquals(0, bonus.score());
        assertFalse(bonus.enCours());
    }

    // ---- le mode C : le joueur choisit le département, puis le nomme ----

    @Test
    @DisplayName("Mode C : pas de question imposée ; le joueur choisit l'ordre, une réponse par département")
    void modeChoix() {
        Bonus bonus = bretagne(Bonus.Question.CHOIX);

        assertEquals(Bonus.Question.CHOIX, bonus.getQuestion());
        assertThrows(IllegalStateException.class, bonus::demande);
        // le Morbihan d'abord, bien nommé : la tentative garde le département choisi et celui qui a été écrit
        assertEquals(new ReponseBonus.Jouee(new Tentative(departement("56"), departement("56"))),
                bonus.repondre(departement("56"), departement("56")));
        // puis le Finistère, nommé à tort Côtes-d'Armor : une réponse fausse, qui compte
        assertTrue(bonus.repondre(departement("29"), departement("22")) instanceof ReponseBonus.Jouee jouee
                && !jouee.tentative().juste());
        assertEquals(List.of(departement("22"), departement("35")), bonus.restants());
        assertEquals(3, bonus.rang());
        // un département qui a déjà sa réponse ne se choisit plus
        assertThrows(IllegalArgumentException.class, () -> bonus.repondre(departement("29"), departement("29")));
        // ni un département d'une autre région
        assertThrows(IllegalArgumentException.class, () -> bonus.repondre(departement("33"), departement("33")));
        bonus.repondre(departement("22"), departement("22"));
        bonus.repondre(departement("35"), departement("35"));

        assertEquals(Bonus.Etat.TERMINE, bonus.getEtat());
        assertEquals(3, bonus.score());
        assertFalse(bonus.tentative(departement("29")).orElseThrow().juste());
    }

    @Test
    @DisplayName("Mode C : un département déjà trouvé est refusé sans rien coûter ; un nom donné à tort, non")
    void dejaTrouve() {
        Bonus bonus = bretagne(Bonus.Question.CHOIX);
        bonus.repondre(departement("56"), departement("56"));
        bonus.repondre(departement("29"), departement("35"));     // faux : l'Ille-et-Vilaine n'est pas trouvée

        assertEquals(new ReponseBonus.DejaTrouve(departement("56")), bonus.repondre(departement("22"), departement("56")));
        assertEquals(2, bonus.getTentatives().size());
        // l'Ille-et-Vilaine a été donnée, mais au mauvais endroit : elle peut l'être encore
        assertTrue(bonus.repondre(departement("35"), departement("35")) instanceof ReponseBonus.Jouee);
        // un département d'une autre région est une réponse fausse, qui compte
        assertTrue(bonus.repondre(departement("22"), departement("33")) instanceof ReponseBonus.Jouee jouee
                && !jouee.tentative().juste());
        assertEquals(Bonus.Etat.TERMINE, bonus.getEtat());
    }

    @Test
    @DisplayName("Mode C, la saisie : un nom, un numéro, ou un début de nom qui n'évoque qu'un département des 101")
    void saisieChoix() {
        Bonus bonus = bretagne(Bonus.Question.CHOIX);

        assertEquals(new ReponseBonus.Inconnue(), bonus.repondre(departement("22"), "Bretagne"));
        // « haute » évoque Haute-Corse, Haute-Garonne… : on demande de choisir, sans rien coûter
        assertEquals(new ReponseBonus.Plusieurs(), bonus.repondre(departement("22"), "haute"));
        assertTrue(bonus.getTentatives().isEmpty());
        assertTrue(bonus.repondre(departement("22"), "22") instanceof ReponseBonus.Jouee jouee
                && jouee.tentative().juste());
        // « fin » n'évoque que le Finistère, parmi les 101
        assertEquals(new ReponseBonus.Jouee(new Tentative(departement("29"), departement("29"))),
                bonus.repondre(departement("29"), "fin"));
        assertTrue(bonus.repondre(departement("35"), "ille et vilaine") instanceof ReponseBonus.Jouee jouee
                && jouee.tentative().juste());
        // la suggestion choisie, avec son numéro (« Morbihan (56) »), répond bien avec ce département
        assertTrue(bonus.repondre(departement("56"), "Morbihan (56)") instanceof ReponseBonus.Jouee jouee
                && jouee.tentative().juste());
    }

    @Test
    @DisplayName("Mode C en outre-mer : la question du numéro, comme dans les autres modes")
    void choixOutreMer() {
        Bonus bonus = new Bonus(RegionFr.GUYANE, RegionFr.GUYANE.getDepartements(), Bonus.Question.CHOIX);

        assertEquals(Bonus.Question.NUMERO, bonus.getQuestion());
        assertEquals(departement("973"), bonus.demande());
    }

    @Test
    @DisplayName("Relire une réponse d'un fichier : acceptée si elle est possible, ignorée sinon")
    void relire() {
        Bonus bonus = bretagne(Bonus.Question.CHOIX);

        assertTrue(bonus.relire(departement("29"), departement("22")));
        assertFalse(bonus.relire(departement("29"), departement("29")));     // déjà répondu
        assertFalse(bonus.relire(departement("33"), departement("33")));     // pas dans la région
        assertEquals(List.of(new Tentative(departement("29"), departement("22"))), bonus.getTentatives());
    }
}
