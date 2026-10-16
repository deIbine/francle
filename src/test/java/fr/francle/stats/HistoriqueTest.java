package fr.francle.stats;

import fr.francle.donnees.Departement;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Essai;
import fr.francle.jeu.ModesDifficiles;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// L'historique : retenir la partie du jour, puis la reconstruire en rejouant ses propositions
class HistoriqueTest {

    // une date lointaine : les tests ne révèlent pas les réponses des prochaines parties du jour
    private static final LocalDate JOUR = LocalDate.of(2030, 3, 10);
    private static final RegionFr CIBLE = RegionDuJour.pour(JOUR);

    // une région de la partie du jour qui n'est pas la cible
    private static RegionFr autre(int rang) {
        return RegionFr.tirables(RegionDuJour.MODE).stream().filter(r -> r != CIBLE).toList().get(rang);
    }

    private static List<RegionFr> propositions(Partie partie) {
        return partie.getEssais().stream().map(Essai::proposition).toList();
    }

    @Test
    @DisplayName("Rien d'enregistré : une partie du jour neuve, sur la région du jour")
    void partieNeuve() {
        Partie partie = Historique.vide().partieDuJour(JOUR);

        assertEquals(CIBLE, partie.getCible());
        assertTrue(partie.enCours());
        assertTrue(partie.getEssais().isEmpty());
    }

    @Test
    @DisplayName("Une partie en cours est retenue, puis reprise avec les mêmes essais (distances recalculées)")
    void partieEnCours() {
        Partie jouee = Historique.vide().partieDuJour(JOUR);
        jouee.proposer(autre(0).getNom());
        jouee.proposer(autre(1).getNom());

        Historique historique = Historique.vide().avec(JOUR, jouee);
        Partie reprise = historique.partieDuJour(JOUR);

        assertEquals(new PartieEnCours(JOUR, List.of(autre(0), autre(1))), historique.enCours());
        assertTrue(historique.resultats().isEmpty());
        assertTrue(reprise.enCours());
        assertEquals(jouee.getEssais(), reprise.getEssais());
    }

    @Test
    @DisplayName("Une partie gagnée devient un résultat, et la partie en cours disparaît")
    void partieGagnee() {
        Partie jouee = Historique.vide().partieDuJour(JOUR);
        jouee.proposer(autre(0).getNom());
        Historique enCours = Historique.vide().avec(JOUR, jouee);
        jouee.proposer(CIBLE.getNom());

        Historique historique = enCours.avec(JOUR, jouee);

        assertNull(historique.enCours());
        assertEquals(List.of(new Resultat(JOUR, CIBLE, List.of(autre(0), CIBLE), Partie.Etat.GAGNEE)),
                historique.resultats());
        Partie reprise = historique.partieDuJour(JOUR);
        assertEquals(Partie.Etat.GAGNEE, reprise.getEtat());
        assertEquals(propositions(jouee), propositions(reprise));
    }

    @Test
    @DisplayName("Un abandon est reconstruit comme un abandon, pas comme une partie en cours")
    void abandon() {
        Partie jouee = Historique.vide().partieDuJour(JOUR);
        jouee.proposer(autre(0).getNom());
        jouee.abandonner();

        Partie reprise = Historique.vide().avec(JOUR, jouee).partieDuJour(JOUR);

        assertEquals(Partie.Etat.ABANDONNEE, reprise.getEtat());
        assertEquals(1, reprise.getEssais().size());
    }

    @Test
    @DisplayName("Une partie perdue (5 essais ratés) est reconstruite comme perdue")
    void defaite() {
        Partie jouee = Historique.vide().partieDuJour(JOUR);
        for (int i = 0; i < Partie.ESSAIS_MAX; i++) {
            jouee.proposer(autre(i).getNom());
        }

        Partie reprise = Historique.vide().avec(JOUR, jouee).partieDuJour(JOUR);

        assertEquals(Partie.Etat.PERDUE, reprise.getEtat());
        assertEquals(jouee.getEssais(), reprise.getEssais());
    }

    @Test
    @DisplayName("Une partie finie sous d'anciennes règles (une proposition en double) reste finie")
    void anciennesRegles() {
        // enregistrée avant que les doublons soient refusés : rejouée aujourd'hui, elle n'a plus qu'un essai
        List<RegionFr> propositions = List.of(autre(0), autre(0));
        Historique historique = new Historique(Historique.VERSION,
                List.of(new Resultat(JOUR, CIBLE, propositions, Partie.Etat.PERDUE)), null);

        Partie reprise = historique.partieDuJour(JOUR);

        assertFalse(reprise.enCours());
        assertEquals(1, reprise.getEssais().size());
    }

    @Test
    @DisplayName("Une partie en cours d'un autre jour est ignorée : aujourd'hui, une partie neuve")
    void enCoursDHier() {
        Partie hier = Historique.vide().partieDuJour(JOUR.minusDays(1));
        hier.proposer(autre(0).getNom());
        Historique historique = Historique.vide().avec(JOUR.minusDays(1), hier);

        Partie aujourdhui = historique.partieDuJour(JOUR);

        assertTrue(aujourdhui.getEssais().isEmpty());
        assertEquals(CIBLE, aujourdhui.getCible());
    }

    @Test
    @DisplayName("Un seul résultat par date, et les résultats restent triés par date")
    void unResultatParDate() {
        Partie lendemain = Historique.vide().partieDuJour(JOUR.plusDays(1));
        lendemain.abandonner();
        Partie jour = Historique.vide().partieDuJour(JOUR);
        jour.abandonner();

        Historique historique = Historique.vide().avec(JOUR.plusDays(1), lendemain).avec(JOUR, jour).avec(JOUR, jour);

        assertEquals(List.of(JOUR, JOUR.plusDays(1)), historique.resultats().stream().map(Resultat::date).toList());
    }

    // la partie du jour gagnée, retenue dans un historique
    private static Historique gagnee() {
        Partie partie = Historique.vide().partieDuJour(JOUR);
        partie.proposer(CIBLE.getNom());
        return Historique.vide().avec(JOUR, partie);
    }

    @Test
    @DisplayName("Le bonus est retenu réponse par réponse, puis reconstruit fini : commencé, il compte comme passé")
    void bonus() {
        Bonus bonus = Bonus.duJour(CIBLE, JOUR);
        Departement premier = bonus.demande();
        bonus.repondre(premier);                         // juste
        Historique historique = gagnee().avecBonus(JOUR, bonus);

        assertEquals(List.of(premier.code()), historique.resultats().get(0).bonus());
        Bonus relu = historique.bonusDuJour(JOUR).orElseThrow();
        // le jeu a été fermé en plein bonus : pas de reprise, il compte comme passé avec ses réponses
        assertEquals(Bonus.Etat.PASSE, relu.getEtat());
        assertEquals(bonus.getTentatives(), relu.getTentatives());
        assertEquals(1, relu.score());
    }

    @Test
    @DisplayName("Un bonus fini se relit fini, avec le même score")
    void bonusFini() {
        Bonus bonus = Bonus.duJour(CIBLE, JOUR);
        while (bonus.enCours()) {
            // la première réponse juste, toutes les autres fausses (le premier département de l'ordre, encore)
            bonus.repondre(bonus.getOrdre().get(0));
        }

        Bonus relu = gagnee().avecBonus(JOUR, bonus).bonusDuJour(JOUR).orElseThrow();

        assertEquals(Bonus.Etat.TERMINE, relu.getEtat());
        assertEquals(1, relu.score());
        assertEquals(bonus.total(), relu.getTentatives().size());
    }

    @Test
    @DisplayName("Un bonus sans réponse n'a pas commencé : rien n'est retenu, il reste à jouer")
    void bonusPasCommence() {
        Historique historique = gagnee();

        assertSame(historique, historique.avecBonus(JOUR, Bonus.duJour(CIBLE, JOUR)));
        assertEquals(Optional.empty(), historique.bonusDuJour(JOUR));
    }

    @Test
    @DisplayName("Pas de bonus sans partie du jour gagnée")
    void bonusSansVictoire() {
        Bonus bonus = Bonus.duJour(CIBLE, JOUR);
        bonus.repondre(bonus.demande());

        assertThrows(IllegalStateException.class, () -> Historique.vide().avecBonus(JOUR, bonus));
    }

    @Test
    @DisplayName("Retenir de nouveau la partie (même finie) garde son bonus")
    void bonusGarde() {
        Partie partie = Historique.vide().partieDuJour(JOUR);
        partie.proposer(CIBLE.getNom());
        Bonus bonus = Bonus.duJour(CIBLE, JOUR);
        bonus.repondre(bonus.demande());

        Historique historique = Historique.vide().avec(JOUR, partie).avecBonus(JOUR, bonus).avec(JOUR, partie);

        assertTrue(historique.bonusDuJour(JOUR).isPresent());
    }

    @Test
    @DisplayName("Les modes difficiles sont retenus avec la partie, en cours puis terminée, et le bonus les garde")
    void modes() {
        ModesDifficiles tournee = new ModesDifficiles(true, false);
        Partie partie = Historique.vide().partieDuJour(JOUR);
        partie.proposer(autre(0).getNom());

        Historique enCours = Historique.vide().avec(JOUR, partie, tournee);
        assertEquals(Optional.of(tournee), enCours.modesDuJour(JOUR));

        partie.proposer(CIBLE.getNom());
        // sans les redonner, avec(...) reprend ceux déjà retenus
        Historique gagnee = enCours.avec(JOUR, partie);
        assertEquals(Optional.of(tournee), gagnee.modesDuJour(JOUR));

        Bonus bonus = Bonus.duJour(CIBLE, JOUR);
        bonus.repondre(bonus.demande());
        assertEquals(Optional.of(tournee), gagnee.avecBonus(JOUR, bonus).modesDuJour(JOUR));
        assertEquals(Optional.empty(), Historique.vide().modesDuJour(JOUR));
    }

    @Test
    @DisplayName("Un historique ne se modifie pas : avec(...) en renvoie un nouveau")
    void nonModifiable() {
        Historique vide = Historique.vide();
        Partie partie = vide.partieDuJour(JOUR);
        partie.abandonner();

        vide.avec(JOUR, partie);

        assertTrue(vide.resultats().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> vide.resultats().add(null));
    }
}
