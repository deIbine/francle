package fr.francle.ui;

import fr.francle.donnees.Langue;
import fr.francle.stats.Statistiques;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// La ligne du bonus sous les barres des statistiques, sans rien afficher
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
}
