package fr.francle.donnees;

// les régions jouées, choisies dans les paramètres : celles d'avant 2016 OU celles d'après, jamais un mélange
public enum ModeRegions {
    ANCIENNES, NOUVELLES;

    // les régions de ce mode : celles de son époque, plus les régions inchangées en 2016 (Bretagne, outre-mer…).
    // Une seule règle sert à la fois pour tirer la région à deviner et pour accepter la proposition du joueur :
    // on ne peut proposer que ce qu'on pourrait avoir à deviner
    boolean contient(RegionFr region) {
        Epoque autre = this == ANCIENNES ? Epoque.NOUVELLE : Epoque.ANCIENNE;
        return region.getEpoque() != autre;
    }
}
