package fr.francle.ui;

import fr.francle.donnees.Langue;
import fr.francle.stats.Statistiques;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.*;

// La ligne du bonus sous les barres des statistiques, et ces barres pour un lecteur d'écran, sans rien afficher
class FenetreStatistiquesTest {

    private final Textes textes = new Textes(Langue.FR);

    // des statistiques avec ce nombre de victoires et de bonus joués (le reste n'importe pas ici)
    private static Statistiques stats(int victoires, int bonusJoues, int scoreMoyen) {
        return new Statistiques(victoires, victoires, 0, 0, List.of(victoires, 0, 0, 0, 0, 0), bonusJoues, scoreMoyen);
    }

    @Test
    @DisplayName("Aucune victoire : il faut gagner une partie du jour pour jouer le bonus")
    void sansVictoire() {
        assertEquals(textes.texte("statistiques.bonus.vide"), FenetreStatistiques.ligneBonus(stats(0, 0, 0), textes));
    }

    @Test
    @DisplayName("Une victoire, mais aucun bonus joué : on ne demande pas de gagner une partie de plus")
    void victoireSansBonus() {
        String ligne = FenetreStatistiques.ligneBonus(stats(1, 0, 0), textes);
        assertEquals(textes.texte("statistiques.bonus.aucun"), ligne);
        assertFalse(ligne.contains("gagnez"), ligne);
    }

    @Test
    @DisplayName("Des bonus joués : leur score moyen")
    void avecBonus() {
        assertEquals("Bonus des départements : " + textes.pourcentage(75) + " trouvés en moyenne, sur 2 bonus",
                FenetreStatistiques.ligneBonus(stats(3, 2, 75), textes));
    }

    @Test
    @DisplayName("Les barres en mots : chacune son nombre d'essais et de victoires, celle du jour signalée")
    void descriptionDesBarres() {
        List<Integer> repartition = List.of(0, 1, 5, 0, 0, 2);
        assertEquals("1 essai : aucune victoire. 2 essais : 1 victoire. 3 essais : 5 victoires (la partie du jour). "
                        + "4 essais : aucune victoire. 5 essais : aucune victoire. 6 essais : 2 victoires.",
                FenetreStatistiques.description(repartition, 3, textes));
        assertEquals("1 guess: no wins. 2 guesses: 1 win. 3 guesses: 5 wins. 4 guesses: no wins. 5 guesses: no wins. "
                        + "6 guesses: 2 wins.",
                FenetreStatistiques.description(repartition, 0, new Textes(Langue.EN)));
    }

    @Test
    @DisplayName("La répartition a un nom et une description accessibles, qu'un lecteur d'écran peut lire")
    void repartitionAccessible() {
        Statistiques stats = new Statistiques(3, 3, 1, 2, List.of(1, 2, 0, 0, 0, 0), 0, 0);
        JPanel panneau = FenetreStatistiques.panneau(stats, 2, textes, null);
        Component barres = ((BorderLayout) panneau.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        AccessibleContext contexte = barres.getAccessibleContext();
        assertNotNull(contexte, "sans contexte accessible, un lecteur d'écran ne voit rien");
        assertEquals("Victoires par nombre d'essais", contexte.getAccessibleName());
        assertEquals(AccessibleRole.LABEL, contexte.getAccessibleRole());
        assertTrue(contexte.getAccessibleDescription().startsWith("1 essai : 1 victoire. 2 essais : 2 victoires (la partie du jour)."),
                contexte.getAccessibleDescription());
    }
}
