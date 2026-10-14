package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.stats.Sauvegarde;
import fr.francle.stats.SauvegardeIllisible;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Les réglages se lisent et se modifient sans ouvrir de popup, et s'enregistrent dans un fichier
class ReglagesTest {

    @TempDir
    Path dossier;

    @Test
    @DisplayName("Réglages par défaut : nouvelles régions, avec images, liste et suggestions, thème clair")
    void parDefaut() {
        Reglages reglages = new Reglages();

        assertEquals(ModeRegions.NOUVELLES, reglages.getMode());
        assertTrue(reglages.isAvecImages());
        assertTrue(reglages.isAvecListe());
        assertTrue(reglages.isAvecSuggestions());
        assertFalse(reglages.isThemeSombre());
        assertFalse(reglages.isUnitesAnglaises());
        assertFalse(reglages.isSilhouetteTournee());
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

    @Test
    @DisplayName("Premier lancement : pas de fichier, les réglages par défaut")
    void pasDeFichier() throws IOException {
        Reglages reglages = Reglages.sauvegarde(dossier.resolve("reglages.json")).charger();

        assertEquals(ModeRegions.NOUVELLES, reglages.getMode());
        assertFalse(reglages.isThemeSombre());
    }

    @Test
    @DisplayName("Enregistrer puis charger redonne les mêmes réglages")
    void allerRetour() throws IOException {
        Sauvegarde<Reglages> sauvegarde = Reglages.sauvegarde(dossier.resolve("reglages.json"));
        Reglages reglages = new Reglages();
        reglages.setMode(ModeRegions.ANCIENNES);
        reglages.setAvecSuggestions(false);
        reglages.setThemeSombre(true);
        reglages.setUnitesAnglaises(true);
        reglages.setSilhouetteTournee(true);

        sauvegarde.enregistrer(reglages);
        Reglages relus = sauvegarde.charger();

        assertEquals(ModeRegions.ANCIENNES, relus.getMode());
        assertTrue(relus.isAvecImages());
        assertFalse(relus.isAvecSuggestions());
        assertTrue(relus.isThemeSombre());
        assertTrue(relus.isUnitesAnglaises());
        assertTrue(relus.isSilhouetteTournee());
        assertTrue(Files.readString(sauvegarde.getFichier()).contains("\"themeSombre\" : true"));
    }

    @Test
    @DisplayName("Un fichier d'une version plus ancienne (réglage absent) garde la valeur par défaut de ce réglage")
    void reglageAbsent() throws IOException {
        Path fichier = dossier.resolve("reglages.json");
        Files.writeString(fichier, "{ \"mode\" : \"ANCIENNES\" }");

        Reglages reglages = Reglages.sauvegarde(fichier).charger();

        assertEquals(ModeRegions.ANCIENNES, reglages.getMode());
        assertTrue(reglages.isAvecImages());
        assertFalse(reglages.isThemeSombre());
    }

    @Test
    @DisplayName("Un mode inconnu (comme l'ancien « TOUTES ») rend le fichier illisible : il est mis de côté")
    void modeInconnu() throws IOException {
        Path fichier = dossier.resolve("reglages.json");
        Files.writeString(fichier, "{ \"mode\" : \"TOUTES\" }");
        Sauvegarde<Reglages> sauvegarde = Reglages.sauvegarde(fichier);

        assertThrows(SauvegardeIllisible.class, sauvegarde::charger);
        assertFalse(Files.exists(fichier));
        assertEquals(ModeRegions.NOUVELLES, sauvegarde.parDefaut().getMode());
    }
}
