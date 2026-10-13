package fr.francle.jeu;

import fr.francle.donnees.RegionFr;

// la direction à suivre depuis la région proposée pour aller vers la cible, comme la flèche de Worldle
public enum Direction {
    NORD("↑"), NORD_EST("↗"), EST("→"), SUD_EST("↘"), SUD("↓"), SUD_OUEST("↙"), OUEST("←"), NORD_OUEST("↖"),
    // deux régions différentes peuvent avoir la même capitale (Hauts-de-France et Nord-Pas-de-Calais : Lille) :
    // à 0 km, aucune direction n'a de sens
    SUR_PLACE("●");

    // les 8 directions de la rose des vents, dans le sens des aiguilles d'une montre à partir du nord
    private static final Direction[] ROSE = {NORD, NORD_EST, EST, SUD_EST, SUD, SUD_OUEST, OUEST, NORD_OUEST};

    private final String symbole;

    Direction(String symbole) {
        this.symbole = symbole;
    }

    public String getSymbole() { return symbole; }

    // la direction pour aller de "depuis" (la proposition) vers "vers" (la cible)
    public static Direction entre(RegionFr depuis, RegionFr vers) {
        if (depuis.distance(vers) < 1) {
            return SUR_PLACE;
        }
        return depuisAzimut(azimut(depuis, vers));
    }

    // azimut initial (le cap à prendre au départ, sur le plus court chemin) de "depuis" vers "vers",
    // en degrés de 0 à 360 : 0 = nord, 90 = est, 180 = sud, 270 = ouest
    // formule : θ = atan2(sin Δλ · cos φ2, cos φ1 · sin φ2 − sin φ1 · cos φ2 · cos Δλ), φ = latitude, λ = longitude
    static double azimut(RegionFr depuis, RegionFr vers) {
        // Math.sin et Math.cos attendent des radians, pas des degrés
        double lat1 = Math.toRadians(depuis.getLatitude());
        double lat2 = Math.toRadians(vers.getLatitude());
        double deltaLon = Math.toRadians(vers.getLongitude() - depuis.getLongitude());
        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        // atan2 renvoie un angle entre -180° et 180° : on le ramène entre 0° et 360°
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    // chaque direction couvre une part de 45°, centrée sur son angle : le nord va de 337,5° à 22,5°
    static Direction depuisAzimut(double azimut) {
        // 359° / 45 = 7,98, arrondi à 8 : le modulo 8 ramène ce secteur sur le nord (0)
        int secteur = (int) Math.round(azimut / 45) % ROSE.length;
        return ROSE[secteur];
    }
}
