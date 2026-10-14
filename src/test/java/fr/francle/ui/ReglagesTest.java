package fr.francle.ui;

import fr.francle.donnees.ModeRegions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Les réglages se lisent et se modifient sans ouvrir de popup
class ReglagesTest {

    @Test
    @DisplayName("Réglages par défaut : nouvelles régions, avec images, liste et suggestions")
    void parDefaut() {
        Reglages reglages = new Reglages();

        assertEquals(ModeRegions.NOUVELLES, reglages.getMode());
        assertTrue(reglages.isAvecImages());
        assertTrue(reglages.isAvecListe());
        assertTrue(reglages.isAvecSuggestions());
    }

    @Test
    @DisplayName("Chaque réglage se modifie indépendamment des autres")
    void modification() {
        Reglages reglages = new Reglages();

        reglages.setMode(ModeRegions.ANCIENNES);
        reglages.setAvecImages(false);

        assertEquals(ModeRegions.ANCIENNES, reglages.getMode());
        assertFalse(reglages.isAvecImages());
        assertTrue(reglages.isAvecListe());
    }
}
