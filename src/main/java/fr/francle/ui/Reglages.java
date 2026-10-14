package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.stats.Sauvegarde;

import java.nio.file.Path;

/*
Les réglages du joueur, sans aucun affichage : le menu Paramètres de la fenêtre les modifie.
Depuis la Phase 6, ils sont enregistrés dans ~/.francle/reglages.json et retrouvés au lancement suivant.

Jackson lit et écrit cet objet grâce à ses accesseurs : getMode/setMode donne le champ « mode » du fichier,
isThemeSombre/setThemeSombre le champ « themeSombre »… Un champ absent du fichier (écrit par une version plus
ancienne du jeu) garde simplement sa valeur par défaut ci-dessous.
 */
class Reglages {

    private ModeRegions mode = ModeRegions.NOUVELLES;
    private boolean avecImages = true;
    private boolean avecListe = true;
    private boolean avecSuggestions = true;
    private boolean themeSombre = false;

    // ~/.francle/reglages.json, à côté de l'historique
    static Sauvegarde<Reglages> sauvegarde() {
        return sauvegarde(Sauvegarde.DOSSIER.resolve("reglages.json"));
    }

    static Sauvegarde<Reglages> sauvegarde(Path fichier) {
        return new Sauvegarde<>(fichier, Reglages.class, Reglages::new);
    }

    public ModeRegions getMode() { return mode; }
    public void setMode(ModeRegions mode) { this.mode = mode; }

    public boolean isAvecImages() { return avecImages; }
    public void setAvecImages(boolean avecImages) { this.avecImages = avecImages; }

    public boolean isAvecListe() { return avecListe; }
    public void setAvecListe(boolean avecListe) { this.avecListe = avecListe; }

    public boolean isAvecSuggestions() { return avecSuggestions; }
    public void setAvecSuggestions(boolean avecSuggestions) { this.avecSuggestions = avecSuggestions; }

    public boolean isThemeSombre() { return themeSombre; }
    public void setThemeSombre(boolean themeSombre) { this.themeSombre = themeSombre; }
}
