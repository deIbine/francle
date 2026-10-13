package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
            assertEquals("912 km", Textes.distance(912.37));
            assertEquals("913 km", Textes.distance(912.5));
            assertEquals("6 789 km", Textes.distance(6789.4));
            assertEquals("0 km", Textes.distance(0));
        } finally {
            Locale.setDefault(avant); // ne pas perturber les autres tests
        }
    }

    @Test
    @DisplayName("Les quatre indices de la Bretagne")
    void indices() {
        RegionFr bretagne = RegionFr.BRETAGNE;

        assertEquals("Superficie : 27 208 km²", Textes.indice(Indice.SUPERFICIE, bretagne));
        assertEquals("Spécialité culinaire : la crêpe", Textes.indice(Indice.SPECIALITE, bretagne));
        assertEquals("Personnalité : Louis Guilloux", Textes.indice(Indice.PERSONNALITE, bretagne));
        assertEquals("Capitale régionale : Rennes", Textes.indice(Indice.CAPITALE, bretagne));
    }

    @Test
    @DisplayName("Chaque mode de jeu a son propre nom dans le menu")
    void modes() {
        assertEquals("Nouvelles régions (depuis 2016)", Textes.mode(ModeRegions.NOUVELLES));
        assertEquals("Anciennes régions (avant 2016)", Textes.mode(ModeRegions.ANCIENNES));
        assertEquals("Toutes les régions", Textes.mode(ModeRegions.TOUTES));
        // deux modes avec le même nom seraient indiscernables dans le menu
        assertEquals(ModeRegions.values().length,
                java.util.Arrays.stream(ModeRegions.values()).map(Textes::mode).distinct().count());
    }

    @Test
    @DisplayName("Essais restants, au singulier comme au pluriel")
    void apresEssai() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Corse");
        assertEquals("C'est raté ! Encore 4 essais.", Textes.apresEssai(partie));

        partie.proposer("Normandie");
        partie.proposer("Occitanie");
        partie.proposer("Grand Est");
        assertEquals("C'est raté ! Encore 1 essai.", Textes.apresEssai(partie));
    }

    @Test
    @DisplayName("Messages de fin : victoire, défaite, abandon")
    void fin() {
        Partie gagnee = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        gagnee.proposer("Bretagne");
        assertEquals("Bravo ! Trouvé en 1 essai.", Textes.fin(gagnee));

        Partie perdue = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        for (String proposition : List.of("Corse", "Normandie", "Occitanie", "Grand Est", "Guyane")) {
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
    @DisplayName("La liste des régions est triée à la française (Île-de-France à la lettre I)")
    void listeTriee() {
        List<String> noms = List.of(Textes.listeDesRegions(ModeRegions.NOUVELLES).split("\n"));

        assertEquals(18, noms.size());
        assertEquals("Auvergne-Rhône-Alpes", noms.get(0));
        assertTrue(noms.indexOf("Hauts-de-France") < noms.indexOf("Île-de-France"));
        assertTrue(noms.indexOf("Île-de-France") < noms.indexOf("La Réunion"));
    }
}
