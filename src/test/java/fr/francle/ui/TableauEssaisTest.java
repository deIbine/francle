package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;

import static org.junit.jupiter.api.Assertions.*;

// Le modèle du tableau des essais, vérifié sans aucune fenêtre
class TableauEssaisTest {

    @Test
    @DisplayName("Toujours 5 lignes numérotées et 5 colonnes, même sans partie")
    void dimensions() {
        TableauEssais tableau = new TableauEssais();

        assertEquals(Partie.ESSAIS_MAX, tableau.getRowCount());
        assertEquals(5, tableau.getColumnCount());
        assertEquals("Région", tableau.getColumnName(1));
        assertEquals("1", tableau.getValueAt(0, 0));
        assertEquals("5", tableau.getValueAt(4, 0));
        assertEquals("", tableau.getValueAt(0, 1));
    }

    @Test
    @DisplayName("Un essai raté remplit sa ligne : région, distance, flèche, pourcentage")
    void essaiRate() {
        Partie partie = new Partie(RegionFr.GRAND_EST, ModeRegions.NOUVELLES);
        TableauEssais tableau = new TableauEssais();
        tableau.setPartie(partie);

        partie.proposer("Bretagne");

        assertEquals("Bretagne", tableau.getValueAt(0, 1));
        assertTrue(((String) tableau.getValueAt(0, 2)).matches("\\d+ km"));
        assertEquals("→", tableau.getValueAt(0, 3));
        assertTrue(((String) tableau.getValueAt(0, 4)).matches("\\d+ %"));
        // la ligne suivante est encore vide
        assertEquals("", tableau.getValueAt(1, 1));
    }

    @Test
    @DisplayName("L'essai gagnant : 0 km, une coche, 100 %")
    void essaiGagnant() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        TableauEssais tableau = new TableauEssais();
        tableau.setPartie(partie);

        partie.proposer("Bretagne");

        assertEquals("0 km", tableau.getValueAt(0, 2));
        assertEquals(TableauEssais.TROUVE, tableau.getValueAt(0, 3));
        assertEquals("100 %", tableau.getValueAt(0, 4));
    }

    @Test
    @DisplayName("Le modèle prévient la JTable : tout le tableau pour une partie, une seule ligne pour un essai")
    void evenements() {
        TableauEssais tableau = new TableauEssais();
        List<TableModelEvent> recus = new ArrayList<>();
        // un écouteur, comme celui que la JTable installe sur son modèle
        tableau.addTableModelListener(recus::add);

        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        tableau.setPartie(partie);
        partie.proposer("Corse");
        partie.proposer("Normandie");
        tableau.essaiJoue();

        assertEquals(2, recus.size());
        // setPartie : "toutes les lignes ont pu changer"
        assertEquals(Integer.MAX_VALUE, recus.get(0).getLastRow());
        // essaiJoue : seulement la ligne du 2e essai (indice 1)
        assertEquals(1, recus.get(1).getFirstRow());
        assertEquals(1, recus.get(1).getLastRow());
        assertEquals(TableModelEvent.UPDATE, recus.get(1).getType());
    }
}
