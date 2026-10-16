package fr.francle.jeu;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Les règles d'une partie, vérifiées sans aucun affichage
class PartieTest {

    // joue les propositions dans l'ordre et renvoie la partie
    private static Partie jouer(RegionFr cible, ModeRegions mode, String... propositions) {
        Partie partie = new Partie(cible, mode);
        for (String proposition : propositions) {
            partie.proposer(proposition);
        }
        return partie;
    }

    @Test
    @DisplayName("Trouver la région au premier essai gagne la partie, à 0 km et 100 %")
    void victoireAuPremierEssai() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);

        Essai essai = assertInstanceOf(Reponse.Jouee.class, partie.proposer("  BRETAGNE ")).essai();

        assertEquals(Partie.Etat.GAGNEE, partie.getEtat());
        assertFalse(partie.enCours());
        assertEquals(0, essai.distance());
        assertEquals(100, essai.pourcentage());
    }

    @Test
    @DisplayName("Une saisie inconnue ne coûte pas d'essai")
    void saisieInconnue() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);

        assertEquals(new Reponse.Inconnue(), partie.proposer("Paris"));
        assertEquals(new Reponse.Inconnue(), partie.proposer(""));
        assertTrue(partie.getEssais().isEmpty());
        assertTrue(partie.enCours());
    }

    @Test
    @DisplayName("Une région déjà proposée est refusée sans coûter d'essai, même écrite autrement")
    void regionDejaProposee() {
        Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES, "Corse");

        assertEquals(new Reponse.DejaProposee(RegionFr.CORSE), partie.proposer("corse"));
        assertEquals(1, partie.getEssais().size());
        assertTrue(partie.enCours());
    }

    @Test
    @DisplayName("Une ancienne région est refusée en mode « nouvelles régions »")
    void ancienneRegionRefusee() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);

        assertEquals(new Reponse.Inconnue(), partie.proposer("Alsace"));
    }

    @Test
    @DisplayName("Cinq essais ratés font perdre la partie")
    void defaite() {
        Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES,
                "Corse", "Normandie", "Occitanie", "Grand Est", "Guyane");

        assertEquals(Partie.Etat.PERDUE, partie.getEtat());
        assertEquals(Partie.ESSAIS_MAX, partie.getEssais().size());
    }

    @Test
    @DisplayName("Quatre essais ratés : la partie continue")
    void quatreEchecs() {
        Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES,
                "Corse", "Normandie", "Occitanie", "Grand Est");

        assertTrue(partie.enCours());
    }

    @Test
    @DisplayName("Trouver au 5e essai est une victoire, pas une défaite")
    void victoireAuCinquiemeEssai() {
        Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES,
                "Corse", "Normandie", "Occitanie", "Grand Est", "Bretagne");

        assertEquals(Partie.Etat.GAGNEE, partie.getEtat());
    }

    @Test
    @DisplayName("Jouer dans une partie terminée est une erreur de programmation")
    void jouerApresLaFin() {
        Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES, "Bretagne");

        assertThrows(IllegalStateException.class, () -> partie.proposer("Corse"));
        assertThrows(IllegalStateException.class, partie::abandonner);
    }

    @Test
    @DisplayName("Abandonner termine la partie dans l'état ABANDONNEE")
    void abandon() {
        Partie partie = jouer(RegionFr.CORSE, ModeRegions.ANCIENNES, "Alsace");

        partie.abandonner();

        assertEquals(Partie.Etat.ABANDONNEE, partie.getEtat());
        assertEquals(1, partie.getEssais().size());
    }

    @Test
    @DisplayName("On ne peut proposer que les régions du mode joué : une région de l'autre époque ne coûte pas d'essai")
    void autreEpoqueRefusee() {
        Partie anciennes = new Partie(RegionFr.ALSACE, ModeRegions.ANCIENNES);
        Partie nouvelles = new Partie(RegionFr.GRAND_EST, ModeRegions.NOUVELLES);

        // Grand Est et Alsace ont la même capitale : avant la Phase 6, le mode « toutes » permettait de proposer
        // l'une pour l'autre, à 0 km, sans gagner. Les deux époques ne se mélangent plus
        assertEquals(new Reponse.Inconnue(), anciennes.proposer("Grand Est"));
        assertEquals(new Reponse.Inconnue(), nouvelles.proposer("Alsace"));
        assertTrue(anciennes.getEssais().isEmpty());
        assertTrue(nouvelles.getEssais().isEmpty());
    }

    @Test
    @DisplayName("Les essais sont gardés dans l'ordre, en lecture seule")
    void essaisEnLectureSeule() {
        Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES, "Corse", "Normandie");

        List<Essai> essais = partie.getEssais();

        assertEquals(List.of(RegionFr.CORSE, RegionFr.NORMANDIE),
                essais.stream().map(Essai::proposition).toList());
        assertThrows(UnsupportedOperationException.class, essais::clear);
    }

    @Test
    @DisplayName("Indices : superficie, spécialité, personnalité, capitale, puis plus rien")
    void ordreDesIndices() {
        assertEquals(Indice.SUPERFICIE, Indice.apresEssai(0).orElseThrow());
        assertEquals(Indice.SPECIALITE, Indice.apresEssai(1).orElseThrow());
        assertEquals(Indice.PERSONNALITE, Indice.apresEssai(2).orElseThrow());
        assertEquals(Indice.CAPITALE, Indice.apresEssai(3).orElseThrow());
        assertTrue(Indice.apresEssai(4).isEmpty());
        assertTrue(Indice.apresEssai(-1).isEmpty());
    }

    @Test
    @DisplayName("Indices révélés : un par essai raté, aucun pour l'essai gagnant")
    void indicesReveles() {
        assertEquals(List.of(), new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES).indicesReveles());
        assertEquals(List.of(Indice.SUPERFICIE, Indice.SPECIALITE),
                jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES, "Corse", "Normandie").indicesReveles());
        assertEquals(List.of(Indice.SUPERFICIE),
                jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES, "Corse", "Bretagne").indicesReveles());
        // 5 essais ratés : 4 indices seulement, il n'y en a pas de cinquième
        assertEquals(List.of(Indice.values()),
                jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES,
                        "Corse", "Normandie", "Occitanie", "Grand Est", "Guyane").indicesReveles());
    }

    @Test
    @DisplayName("Pourcentage (formule B) : Bretagne → Normandie 76 %, Grand Est 35 %, Corse 0 %")
    void pourcentagesConnus() {
        RegionFr cible = RegionFr.BRETAGNE;

        assertEquals(76, Partie.pourcentage(cible, cible.distance(RegionFr.NORMANDIE)));
        assertEquals(35, Partie.pourcentage(cible, cible.distance(RegionFr.GRAND_EST)));
        assertEquals(0, Partie.pourcentage(cible, cible.distance(RegionFr.CORSE)));
    }

    @Test
    @DisplayName("Le pourcentage reste entre 0 et 100 pour toutes les paires de régions")
    void pourcentageBorne() {
        for (RegionFr cible : RegionFr.values()) {
            for (RegionFr proposition : RegionFr.values()) {
                int pourcentage = Partie.pourcentage(cible, cible.distance(proposition));
                assertTrue(pourcentage >= 0 && pourcentage <= 100,
                        cible + " → " + proposition + " : " + pourcentage + " %");
            }
        }
    }
}
