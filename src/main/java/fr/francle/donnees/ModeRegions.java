package fr.francle.donnees;

// les régions jouées, choisies dans les paramètres
public enum ModeRegions {
    ANCIENNES, NOUVELLES, TOUTES;

    // correspondance avec le choix tapé dans le menu des paramètres (0, 1 ou 2)
    public static ModeRegions depuisCode(int code) {
        switch (code) {
            case 0 : return ANCIENNES;
            case 2 : return TOUTES;
            default : return NOUVELLES;
        }
    }

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
