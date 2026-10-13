package fr.francle.donnees;

// les régions jouées, choisies dans les paramètres
public enum ModeRegions {
    ANCIENNES, NOUVELLES, TOUTES;

    // les régions que le jeu peut faire deviner dans ce mode
    boolean peutTirer(RegionFr region) {
        switch (this) {
            case ANCIENNES : return region.getEpoque() != Epoque.NOUVELLE;
            case NOUVELLES : return region.getEpoque() != Epoque.ANCIENNE;
            default : return true;
        }
    }

    // les régions acceptées comme proposition du joueur : tout, sauf les anciennes en mode "nouvelles"
    boolean reconnait(RegionFr region) {
        return this != NOUVELLES || region.getEpoque() != Epoque.ANCIENNE;
    }
}
