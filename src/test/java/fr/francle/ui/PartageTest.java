package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Direction;
import fr.francle.jeu.ModesDifficiles;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Le résultat à partager, repris de la version web, et les petites règles qui vont avec
class PartageTest {

    private static final Textes FR = new Textes(Langue.FR);
    private static final Textes EN = new Textes(Langue.EN);

    // les mêmes valeurs que les tests de la version web (domaine/partage.test.ts)
    @ParameterizedTest(name = "{0} % → {1}")
    @CsvSource({"100, 🟩🟩🟩🟩🟩", "87, 🟩🟩🟩🟩⬜", "90, 🟩🟩🟩🟩🟨", "50, 🟩🟩🟨⬜⬜", "9, ⬜⬜⬜⬜⬜", "0, ⬜⬜⬜⬜⬜"})
    @DisplayName("Un carré vert par tranche de 20 %, un jaune s'il reste au moins 10 %")
    void carres(int proximite, String attendu) {
        assertEquals(attendu, Partage.carres(proximite));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Chaque direction a son emoji")
    void emojis(Direction direction) {
        assertFalse(Partage.emoji(direction).isBlank());
    }

    @Test
    @DisplayName("Une victoire : numéro, score, une ligne par essai, le bonus, l'adresse ; jamais le nom de la région")
    void victoire() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Normandie");
        partie.proposer("Bretagne");
        Bonus bonus = new Bonus(RegionFr.BRETAGNE, RegionFr.BRETAGNE.getDepartements(), Bonus.Question.CARTE);
        bonus.repondre(Departement.parCode("22").orElseThrow());
        bonus.passer();

        String texte = Partage.texte(partie, 12, new ModesDifficiles(true, false), bonus, FR);

        String normandie = Partage.carres(partie.getEssais().get(0).pourcentage())
                + Partage.emoji(partie.getEssais().get(0).direction());
        assertEquals("#Francle n° 12 2/6 🌀\n" + normandie + "\n🟩🟩🟩🟩🟩🎉\n🗺️ 1/4\n" + Partage.ADRESSE, texte);
        assertFalse(texte.contains("Bretagne"));
        assertFalse(texte.contains("Normandie"));
    }

    @Test
    @DisplayName("Le bonus dans sa version difficile : 💪 ; la simple, et l'outre-mer (rien n'y change), sans repère")
    void bonusDifficile() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Bretagne");
        Departement morbihan = Departement.parCode("56").orElseThrow();
        for (boolean difficile : List.of(true, false)) {
            Bonus bonus = new Bonus(RegionFr.BRETAGNE, RegionFr.BRETAGNE.getDepartements(), Bonus.Question.CHOIX,
                    difficile);
            bonus.repondre(morbihan, morbihan);
            bonus.passer();

            String derniere = Partage.texte(partie, 12, ModesDifficiles.AUCUN, bonus, FR).split("\n")[2];

            assertEquals(difficile ? "🗺️ 1/4 💪" : "🗺️ 1/4", derniere);
        }
        Partie guyane = new Partie(RegionFr.GUYANE, ModeRegions.NOUVELLES);
        guyane.proposer("Guyane");
        Bonus numero = new Bonus(RegionFr.GUYANE, RegionFr.GUYANE.getDepartements(), Bonus.Question.CHOIX, true);
        numero.repondre("973");
        assertTrue(Partage.texte(guyane, 12, ModesDifficiles.AUCUN, numero, FR).contains("🗺️ 1/1\n"));
    }

    @Test
    @DisplayName("Une défaite ou un abandon : X/6 ; en anglais, « #Francle #12 » ; sans bonus ni mode, rien de plus")
    void defaite() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Corse");
        partie.abandonner();

        String texte = Partage.texte(partie, 12, ModesDifficiles.AUCUN, null, EN);

        assertTrue(texte.startsWith("#Francle #12 X/6\n"), texte);
        assertEquals(3, texte.split("\n").length);
    }

    @Test
    @DisplayName("Une silhouette cachée ne peut pas être tournée : 🙈 seul")
    void modes() {
        assertEquals("🙈", new ModesDifficiles(true, true).emojis());
        assertEquals("🌀", new ModesDifficiles(true, false).emojis());
        assertEquals("", ModesDifficiles.AUCUN.emojis());
    }

    @Test
    @DisplayName("Le compte à rebours : heures, minutes, secondes sur deux chiffres, jamais négatif")
    void duree() {
        assertEquals("03:07:09", FR.duree(Duration.ofSeconds(3 * 3600 + 7 * 60 + 9)));
        assertEquals("23:59:59", FR.duree(Duration.ofDays(1).minusSeconds(1)));
        assertEquals("00:00:00", FR.duree(Duration.ofSeconds(-5)));
    }
}
