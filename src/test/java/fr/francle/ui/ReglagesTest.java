package fr.francle.ui;

import fr.francle.donnees.ModeRegions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Les réglages se lisent et se modifient sans ouvrir de popup
class ReglagesTest {

    @Test
    @DisplayName("Réglages par défaut : nouvelles régions, avec images et avec liste")
    void parDefaut() {
        Reglages reglages = new Reglages();

        assertEquals(ModeRegions.NOUVELLES, reglages.getMode());
        assertTrue(reglages.isAvecImages());
        assertTrue(reglages.isAvecListe());
    }

    @Test
    @DisplayName("Chaque réglage se modifie indépendamment des autres")
    void modification() {
        Reglages reglages = new Reglages();

        reglages.setMode(ModeRegions.TOUTES);
        reglages.setAvecImages(false);

        assertEquals(ModeRegions.TOUTES, reglages.getMode());
        assertFalse(reglages.isAvecImages());
        assertTrue(reglages.isAvecListe());
    }
}
