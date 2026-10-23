package fr.francle.ui;

import fr.francle.donnees.Langue;
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
        assertNull(reglages.getUnitesAnglaises());     // pas encore choisi : suit la langue
        assertNull(reglages.getLangue());              // pas encore choisie : celle du système
        assertFalse(reglages.isSilhouetteTournee());
        assertFalse(reglages.isDepartementsDifficiles());   // la version simple du bonus
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
        reglages.setDepartementsDifficiles(true);

        sauvegarde.enregistrer(reglages);
        Reglages relus = sauvegarde.charger();

        assertEquals(ModeRegions.ANCIENNES, relus.getMode());
        assertTrue(relus.isAvecImages());
        assertFalse(relus.isAvecSuggestions());
        assertTrue(relus.isThemeSombre());
        assertTrue(relus.getUnitesAnglaises());
        assertTrue(relus.isSilhouetteTournee());
        assertTrue(relus.isDepartementsDifficiles());
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
        // un joueur d'avant la phase 13 passe lui aussi à la version simple du bonus, celle par défaut
        assertFalse(reglages.isDepartementsDifficiles());
    }

    @Test
    @DisplayName("Les unités suivent la langue tant que le joueur ne les a pas choisies")
    void unitesSelonLaLangue() {
        Reglages reglages = new Reglages();

        reglages.setLangue(Langue.EN);
        assertTrue(reglages.unitesAnglaises());
        reglages.setLangue(Langue.FR);
        assertFalse(reglages.unitesAnglaises());

        // le joueur coche « miles » en français : son choix l'emporte, même s'il repasse en anglais puis en français
        reglages.setUnitesAnglaises(true);
        reglages.setLangue(Langue.EN);
        reglages.setLangue(Langue.FR);
        assertTrue(reglages.unitesAnglaises());
    }

    @Test
    @DisplayName("La langue choisie est enregistrée ; sans choix, rien n'est imposé dans le fichier")
    void langueEnregistree() throws IOException {
        Sauvegarde<Reglages> sauvegarde = Reglages.sauvegarde(dossier.resolve("reglages.json"));
        Reglages reglages = new Reglages();
        reglages.setLangue(Langue.EN);

        sauvegarde.enregistrer(reglages);

        assertEquals(Langue.EN, sauvegarde.charger().getLangue());
        assertTrue(Files.readString(sauvegarde.getFichier()).contains("\"langue\" : \"EN\""));
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
