package fr.francle.ui;

import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.stats.Sauvegarde;

import java.nio.file.Path;

/*
Les réglages du joueur, sans aucun affichage : le menu Paramètres de la fenêtre les modifie.
Depuis la Phase 6, ils sont enregistrés dans ~/.francle/reglages.json et retrouvés au lancement suivant.

Jackson lit et écrit cet objet grâce à ses accesseurs : getMode/setMode donne le champ « mode » du fichier,
isThemeSombre/setThemeSombre le champ « themeSombre »… Un champ absent du fichier (écrit par une version plus
ancienne du jeu) garde simplement sa valeur par défaut ci-dessous.

Deux réglages peuvent valoir null, « pas encore choisi » : la langue (on prend alors celle du système) et les unités
(on suit alors la langue : miles en anglais, km en français, comme la version web). Dès que le joueur choisit, son
choix l'emporte. Les méthodes langue() et unitesAnglaises(), sans « get » ni « is », donnent la valeur à utiliser ;
Jackson ne les voit pas, il n'enregistre que le choix du joueur.
 */
class Reglages {

    private ModeRegions mode = ModeRegions.NOUVELLES;
    private boolean avecImages = true;
    private boolean avecListe = true;
    private boolean avecSuggestions = true;
    private boolean themeSombre = false;
    private Boolean unitesAnglaises = null;     // Boolean, pas boolean : il peut valoir null
    private Langue langue = null;
    private boolean silhouetteTournee = false;
    // « Comment jouer » a déjà été montré au lancement. Absent d'un ancien fichier, il vaut false : un joueur qui a déjà
    // joué verra l'aide une fois, ce qui lui présente les nouveautés
    private boolean aideVue = false;
    // la version difficile du bonus (les 101 départements dans la liste et les suggestions). Absent d'un ancien
    // fichier, il vaut false : la version simple, celle par défaut pour tout le monde (phase 13)
    private boolean departementsDifficiles = false;

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

    // miles et miles carrés au lieu des kilomètres : seulement à l'affichage, le jeu calcule toujours en km
    public Boolean getUnitesAnglaises() { return unitesAnglaises; }
    public void setUnitesAnglaises(Boolean unitesAnglaises) { this.unitesAnglaises = unitesAnglaises; }

    public Langue getLangue() { return langue; }
    public void setLangue(Langue langue) { this.langue = langue; }

    // la langue du jeu : celle choisie, sinon celle du système
    Langue langue() {
        return langue != null ? langue : Langue.duSysteme();
    }

    // les unités à afficher : celles choisies, sinon celles de la langue
    boolean unitesAnglaises() {
        return unitesAnglaises != null ? unitesAnglaises : langue() == Langue.EN;
    }

    public boolean isSilhouetteTournee() { return silhouetteTournee; }
    public void setSilhouetteTournee(boolean silhouetteTournee) { this.silhouetteTournee = silhouetteTournee; }

    public boolean isAideVue() { return aideVue; }
    public void setAideVue(boolean aideVue) { this.aideVue = aideVue; }

    public boolean isDepartementsDifficiles() { return departementsDifficiles; }
    public void setDepartementsDifficiles(boolean departementsDifficiles) { this.departementsDifficiles = departementsDifficiles; }
}
