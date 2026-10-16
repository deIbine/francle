package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

// Les textes de la fenêtre, construits sans rien afficher
class TextesTest {

    @Test
    @DisplayName("Séparateur de milliers : espace insécable U+00A0")
    void milliers() {
        assertEquals("27 208", Textes.milliers(27208));
        assertEquals("374", Textes.milliers(374));
        assertEquals("0", Textes.milliers(0));
    }

    @Test
    @DisplayName("Distance arrondie au kilomètre, avec séparateur de milliers, même sur un système en anglais")
    void distance() {
        Locale avant = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            assertEquals("912 km", Textes.distance(912.37, false));
            assertEquals("913 km", Textes.distance(912.5, false));
            assertEquals("6 789 km", Textes.distance(6789.4, false));
            assertEquals("0 km", Textes.distance(0, false));
        } finally {
            Locale.setDefault(avant); // ne pas perturber les autres tests
        }
    }

    @Test
    @DisplayName("En miles : 1 mile = 1,609344 km, arrondi au mile, avec séparateur de milliers")
    void distanceEnMiles() {
        assertEquals("1 mi", Textes.distance(1.609344, true));
        assertEquals("567 mi", Textes.distance(912.37, true));
        assertEquals("4 219 mi", Textes.distance(6789.4, true));
        assertEquals("0 mi", Textes.distance(0, true));
    }

    @Test
    @DisplayName("Superficie en km² ou en miles carrés (1 mi² ≈ 2,59 km²)")
    void superficie() {
        assertEquals("27 208 km²", Textes.superficie(27208, false));
        assertEquals("10 505 mi²", Textes.superficie(27208, true));
        assertEquals("144 mi²", Textes.superficie(374, true));
    }

    @Test
    @DisplayName("Les quatre indices de la Bretagne")
    void indices() {
        RegionFr bretagne = RegionFr.BRETAGNE;

        assertEquals("Superficie : 27 208 km²", Textes.indice(Indice.SUPERFICIE, bretagne, false));
        assertEquals("Superficie : 10 505 mi²", Textes.indice(Indice.SUPERFICIE, bretagne, true));
        assertEquals("Spécialité culinaire : la crêpe", Textes.indice(Indice.SPECIALITE, bretagne, false));
        assertEquals("Personnalité : Louis Guilloux", Textes.indice(Indice.PERSONNALITE, bretagne, false));
        assertEquals("Capitale régionale : Rennes", Textes.indice(Indice.CAPITALE, bretagne, true));
        assertEquals("Monument : les alignements de Carnac", Textes.indice(Indice.MONUMENT, bretagne, false));
    }

    @Test
    @DisplayName("Titre de la partie du jour : son numéro, à la Wordle")
    void titreDuJour() {
        assertEquals("Francle n° 1", Textes.titreDuJour(LocalDate.of(2026, 10, 13)));
        assertEquals("Francle n° 20", Textes.titreDuJour(LocalDate.of(2026, 11, 1)));
    }

    @Test
    @DisplayName("Chaque mode de jeu a son propre nom dans le menu")
    void modes() {
        assertEquals("Nouvelles régions (depuis 2016)", Textes.mode(ModeRegions.NOUVELLES));
        assertEquals("Anciennes régions (avant 2016)", Textes.mode(ModeRegions.ANCIENNES));
        // deux modes avec le même nom seraient indiscernables dans le menu
        assertEquals(ModeRegions.values().length,
                java.util.Arrays.stream(ModeRegions.values()).map(Textes::mode).distinct().count());
    }

    @Test
    @DisplayName("Essais restants, au singulier comme au pluriel")
    void apresEssai() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Corse");
        assertEquals("C'est raté ! Encore 5 essais.", Textes.apresEssai(partie));

        partie.proposer("Normandie");
        partie.proposer("Occitanie");
        partie.proposer("Grand Est");
        partie.proposer("Guyane");
        assertEquals("C'est raté ! Encore 1 essai.", Textes.apresEssai(partie));
    }

    @Test
    @DisplayName("Messages de fin : victoire, défaite, abandon")
    void fin() {
        Partie gagnee = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        gagnee.proposer("Bretagne");
        assertEquals("Bravo ! Trouvé en 1 essai.", Textes.fin(gagnee));

        Partie perdue = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        for (String proposition : List.of("Corse", "Normandie", "Occitanie", "Grand Est", "Guyane", "Mayotte")) {
            perdue.proposer(proposition);
        }
        assertEquals("C'est raté ! La réponse était Bretagne.", Textes.fin(perdue));

        Partie abandonnee = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        abandonnee.abandonner();
        assertEquals("Vous avez abandonné. La réponse était Bretagne.", Textes.fin(abandonnee));
    }

    @Test
    @DisplayName("Pas de message de fin pour une partie en cours")
    void finEnCours() {
        assertThrows(IllegalStateException.class,
                () -> Textes.fin(new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES)));
    }

    @Test
    @DisplayName("Saisie non reconnue : citée sans ses espaces autour")
    void nonReconnue() {
        assertEquals("« Paris » n'est pas une région reconnue : réessayez.", Textes.nonReconnue("  Paris "));
    }

    @Test
    @DisplayName("Région déjà proposée : son nom et une invitation à en essayer une autre")
    void dejaProposee() {
        assertEquals("Corse a déjà été proposée : essayez une autre région.", Textes.dejaProposee(RegionFr.CORSE));
    }

    @Test
    @DisplayName("La liste des régions est triée à la française (Île-de-France à la lettre I)")
    void listeTriee() {
        List<String> noms = List.of(Textes.listeDesRegions(ModeRegions.NOUVELLES).split("\n"));

        assertEquals(18, noms.size());
        assertEquals("Auvergne-Rhône-Alpes", noms.get(0));
        assertTrue(noms.indexOf("Hauts-de-France") < noms.indexOf("Île-de-France"));
        assertTrue(noms.indexOf("Île-de-France") < noms.indexOf("La Réunion"));
    }
}
