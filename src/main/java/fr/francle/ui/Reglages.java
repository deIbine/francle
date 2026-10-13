package fr.francle.ui;

import fr.francle.donnees.ModeRegions;

// les réglages du joueur, sans aucun affichage : aujourd'hui modifiés par les popups de Parametres,
// demain (Phase 4.3) par la barre de menus de la fenêtre
class Reglages {

    private ModeRegions mode = ModeRegions.NOUVELLES;
    private boolean avecImages = true;
    private boolean avecListe = true;

    public ModeRegions getMode() { return mode; }
    public void setMode(ModeRegions mode) { this.mode = mode; }

    public boolean isAvecImages() { return avecImages; }
    public void setAvecImages(boolean avecImages) { this.avecImages = avecImages; }

    public boolean isAvecListe() { return avecListe; }
    public void setAvecListe(boolean avecListe) { this.avecListe = avecListe; }
}
