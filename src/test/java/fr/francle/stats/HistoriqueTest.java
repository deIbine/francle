package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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
