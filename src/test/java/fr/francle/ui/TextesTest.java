package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;
import fr.francle.jeu.Tentative;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

// Les textes de la fenêtre, construits sans rien afficher, en français puis en anglais
class TextesTest {

    private static final Textes FR = new Textes(Langue.FR);
    private static final Textes EN = new Textes(Langue.EN);

    @Test
    @DisplayName("Bonus : la question, la réponse et la fin, selon la sorte de question")
    void bonus() {
        Bonus bretagne = new Bonus(RegionFr.BRETAGNE, RegionFr.BRETAGNE.getDepartements(), Bonus.Question.CARTE);
        assertEquals("Bonus 1/4 · Cliquez sur : Côtes-d'Armor", FR.questionBonus(bretagne));
        assertEquals("Bonus 1/4 · Click on: Côtes-d'Armor", EN.questionBonus(bretagne));
        Departement finistere = Departement.parCode("29").orElseThrow();
        bretagne.repondre(finistere);
        Tentative ratee = bretagne.getTentatives().get(0);
        assertEquals("Raté : Côtes-d'Armor est en rouge.", FR.reponseBonus(bretagne, ratee));
        bretagne.passer();
        assertEquals("Bonus passé : 0/4 départements trouvés.", FR.finBonus(bretagne));
        assertEquals("Bonus : 0/4.", FR.resumeBonus(bretagne));

        Bonus nom = new Bonus(RegionFr.CORSE, RegionFr.CORSE.getDepartements(), Bonus.Question.NOM);
        assertEquals("Bonus 1/2 · Quel est ce département ?", FR.questionBonus(nom));
        nom.repondre(Departement.parCode("2B").orElseThrow());
        assertEquals("Raté : c'était 2A · Corse-du-Sud.", FR.reponseBonus(nom, nom.getTentatives().get(0)));
        assertEquals("« Bretagne » n'est pas un département : réessayez.", FR.bonusInconnu(nom, " Bretagne "));

        Bonus guyane = Bonus.duJour(RegionFr.GUYANE, LocalDate.of(2030, 3, 10));
        assertEquals("Bonus · Quel est le numéro de : Guyane ?", FR.questionBonus(guyane));
        guyane.repondre("973");
        assertEquals("Bonus : bravo, c'est bien le 973 !", FR.finBonus(guyane));
    }

    @Test
    @DisplayName("Séparateur de milliers : espace insécable U+00A0")
    void milliers() {
        assertEquals("27 208", FR.milliers(27208));
        assertEquals("374", FR.milliers(374));
        assertEquals("0", FR.milliers(0));
    }

    @Test
    @DisplayName("Distance arrondie au kilomètre, avec séparateur de milliers, même sur un système en anglais")
    void distance() {
        Locale avant = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            assertEquals("912 km", FR.distance(912.37, false));
            assertEquals("913 km", FR.distance(912.5, false));
            assertEquals("6 789 km", FR.distance(6789.4, false));
            assertEquals("0 km", FR.distance(0, false));
        } finally {
            Locale.setDefault(avant); // ne pas perturber les autres tests
        }
    }

    @Test
    @DisplayName("En miles : 1 mile = 1,609344 km, arrondi au mile, avec séparateur de milliers")
    void distanceEnMiles() {
        assertEquals("1 mi", FR.distance(1.609344, true));
        assertEquals("567 mi", FR.distance(912.37, true));
        assertEquals("4 219 mi", FR.distance(6789.4, true));
        assertEquals("0 mi", FR.distance(0, true));
    }

    @Test
    @DisplayName("Superficie en km² ou en miles carrés (1 mi² ≈ 2,59 km²)")
    void superficie() {
        assertEquals("27 208 km²", FR.superficie(27208, false));
        assertEquals("10 505 mi²", FR.superficie(27208, true));
        assertEquals("144 mi²", FR.superficie(374, true));
    }

    @Test
    @DisplayName("Les cinq indices de la Bretagne")
    void indices() {
        RegionFr bretagne = RegionFr.BRETAGNE;

        assertEquals("Superficie : 27 208 km²", FR.indice(Indice.SUPERFICIE, bretagne, false));
        assertEquals("Superficie : 10 505 mi²", FR.indice(Indice.SUPERFICIE, bretagne, true));
        assertEquals("Spécialité culinaire : la crêpe", FR.indice(Indice.SPECIALITE, bretagne, false));
        assertEquals("Personnalité : Louis Guilloux", FR.indice(Indice.PERSONNALITE, bretagne, false));
        assertEquals("Capitale régionale : Rennes", FR.indice(Indice.CAPITALE, bretagne, true));
        assertEquals("Monument : les alignements de Carnac", FR.indice(Indice.MONUMENT, bretagne, false));
    }

    @Test
    @DisplayName("Titre de la partie du jour : son numéro, à la Wordle")
    void titreDuJour() {
        assertEquals("Francle n° 1", FR.titreDuJour(LocalDate.of(2026, 10, 13)));
        assertEquals("Francle n° 20", FR.titreDuJour(LocalDate.of(2026, 11, 1)));
    }

    @Test
    @DisplayName("Chaque mode de jeu a son propre nom dans le menu")
    void modes() {
        assertEquals("Nouvelles régions (depuis 2016)", FR.mode(ModeRegions.NOUVELLES));
        assertEquals("Anciennes régions (avant 2016)", FR.mode(ModeRegions.ANCIENNES));
        // deux modes avec le même nom seraient indiscernables dans le menu
        assertEquals(ModeRegions.values().length,
                java.util.Arrays.stream(ModeRegions.values()).map(FR::mode).distinct().count());
    }

    @Test
    @DisplayName("Essais restants, au singulier comme au pluriel")
    void apresEssai() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Corse");
        assertEquals("C'est raté ! Encore 5 essais.", FR.apresEssai(partie));

        partie.proposer("Normandie");
        partie.proposer("Occitanie");
        partie.proposer("Grand Est");
        partie.proposer("Guyane");
        assertEquals("C'est raté ! Encore 1 essai.", FR.apresEssai(partie));
    }

    @Test
    @DisplayName("Messages de fin : victoire, défaite, abandon")
    void fin() {
        Partie gagnee = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        gagnee.proposer("Bretagne");
        assertEquals("Bravo ! Trouvé en 1 essai.", FR.fin(gagnee));

        Partie perdue = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        for (String proposition : List.of("Corse", "Normandie", "Occitanie", "Grand Est", "Guyane", "Mayotte")) {
            perdue.proposer(proposition);
        }
        assertEquals("C'est raté ! La réponse était Bretagne.", FR.fin(perdue));

        Partie abandonnee = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        abandonnee.abandonner();
        assertEquals("Vous avez abandonné. La réponse était Bretagne.", FR.fin(abandonnee));
    }

    @Test
    @DisplayName("Pas de message de fin pour une partie en cours")
    void finEnCours() {
        assertThrows(IllegalStateException.class,
                () -> FR.fin(new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES)));
    }

    @Test
    @DisplayName("Saisie non reconnue : citée sans ses espaces autour")
    void nonReconnue() {
        assertEquals("« Paris » n'est pas une région reconnue : réessayez.", FR.nonReconnue("  Paris "));
    }

    @Test
    @DisplayName("Région déjà proposée : son nom et une invitation à en essayer une autre")
    void dejaProposee() {
        assertEquals("Corse a déjà été proposée : essayez une autre région.", FR.dejaProposee(RegionFr.CORSE));
    }

    @Test
    @DisplayName("La liste des régions est triée à la française (Île-de-France à la lettre I)")
    void listeTriee() {
        List<String> lignes = List.of(FR.listeDesRegions(ModeRegions.NOUVELLES).split("\n"));
        List<String> noms = lignes.subList(1, lignes.size());     // sans la phrase d'introduction

        assertEquals(18, noms.size());
        assertEquals("Auvergne-Rhône-Alpes", noms.get(0));
        assertTrue(noms.indexOf("Hauts-de-France") < noms.indexOf("Île-de-France"));
        assertTrue(noms.indexOf("Île-de-France") < noms.indexOf("La Réunion"));
    }

    @Test
    @DisplayName("Les deux fichiers de textes ont exactement les mêmes clés")
    void memesCles() {
        assertEquals(FR.cles(), EN.cles());
    }

    @Test
    @DisplayName("Chaque phrase s'écrit dans les deux langues avec les mêmes valeurs (%s, %d), dans le même ordre")
    void memesValeurs() {
        for (String cle : FR.cles()) {
            assertEquals(marques(FR, cle), marques(EN, cle), cle);
        }
    }

    // les marques de valeurs d'une phrase (%s, %d), dans l'ordre. String.format échouerait (ou oublierait une
    // valeur) si l'anglais n'avait pas les mêmes que le français
    private static List<String> marques(Textes textes, String cle) {
        String phrase = java.util.ResourceBundle.getBundle("textes.textes", textes.getLangue().getLocale()).getString(cle);
        return java.util.regex.Pattern.compile("%[sd]").matcher(phrase).results().map(r -> r.group()).toList();
    }

    @Test
    @DisplayName("En anglais : nombres à l'anglaise, noms et indices traduits, pluriels")
    void anglais() {
        RegionFr bretagne = RegionFr.BRETAGNE;
        assertEquals("27,208", EN.milliers(27208));
        assertEquals("6,789 km", EN.distance(6789.4, false));
        assertEquals("76%", EN.pourcentage(76));
        assertEquals("76 %", FR.pourcentage(76));
        assertEquals("Food specialty: crêpes", EN.indice(Indice.SPECIALITE, bretagne, false));
        assertEquals("Landmark: the Carnac stones", EN.indice(Indice.MONUMENT, bretagne, false));
        assertEquals("Area: 10,505 mi²", EN.indice(Indice.SUPERFICIE, bretagne, true));
        assertEquals("Francle #1", EN.titreDuJour(LocalDate.of(2026, 10, 13)));
        assertEquals("Brittany has already been guessed: try another region.", EN.dejaProposee(bretagne));

        Partie partie = new Partie(bretagne, ModeRegions.NOUVELLES);
        assertEquals("Which French region is this?", EN.apresEssai(partie));
        partie.proposer("Corse");
        assertEquals("Wrong! 5 guesses left.", EN.apresEssai(partie));
        partie.abandonner();
        assertEquals("You gave up. The answer was Brittany.", EN.fin(partie));
    }

    @Test
    @DisplayName("La liste en anglais est triée selon les noms anglais")
    void listeEnAnglais() {
        List<String> lignes = List.of(EN.listeDesRegions(ModeRegions.NOUVELLES).split("\n"));

        assertEquals("The possible regions:", lignes.get(0));
        assertTrue(lignes.indexOf("Brittany") < lignes.indexOf("Burgundy-Franche-Comté"));
        assertTrue(lignes.indexOf("Corsica") < lignes.indexOf("French Guiana"));
    }

    @Test
    @DisplayName("Le texte « Comment jouer » existe dans les deux langues et parle de 6 essais")
    void aide() {
        assertTrue(FR.aide().startsWith("Devinez la région française cachée en 6 essais"));
        assertTrue(EN.aide().startsWith("Guess the hidden French region in 6 guesses"));
    }
}
