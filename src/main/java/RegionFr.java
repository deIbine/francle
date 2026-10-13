import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

/*
Les régions jouables : chaque information n'est écrite qu'ici, une seule fois.
Le reste du code vient chercher ses données dans cet enum.
 */
enum RegionFr {
    // nouvelles régions et régions inchangées : nom, latitude, longitude, époque, zone, superficie,
    // spécialité culinaire, personnalité, capitale régionale, image
    HAUTS_DE_FRANCE("Hauts-de-France", 50.62925, 3.057256, Epoque.NOUVELLE, Zone.METROPOLE, 31814, "la bière", "Charles de Gaulle", "Lille", "hauts-de-france.png"),
    NORMANDIE("Normandie", 49.443232, 1.099971, Epoque.NOUVELLE, Zone.METROPOLE, 29907, "la matelote", "Guillaume le Conquérant", "Rouen", "normandie.png"),
    BRETAGNE("Bretagne", 48.117266, -1.6777926, Epoque.LES_DEUX, Zone.METROPOLE, 27208, "la crêpe", "Louis Guilloux", "Rennes", "bretagne.png"),
    PAYS_DE_LA_LOIRE("Pays de la Loire", 47.218371, -1.553621, Epoque.LES_DEUX, Zone.METROPOLE, 32082, "la fouace", "Jules Verne", "Nantes", "pays-de-la-loire.png"),
    ILE_DE_FRANCE("Île-de-France", 48.856614, 2.3522219, Epoque.LES_DEUX, Zone.METROPOLE, 12012, "le macaron", "Alexandre Dumas", "Paris", "ile-de-france.png"),
    CENTRE_VAL_DE_LOIRE("Centre-Val de Loire", 47.902964, 1.909251, Epoque.NOUVELLE, Zone.METROPOLE, 39151, "la tarte Tatin", "Jeanne d'Arc", "Orléans", "centre-val-de-loire.png"),
    GRAND_EST("Grand Est", 48.5734053, 7.7521113, Epoque.NOUVELLE, Zone.METROPOLE, 57433, "la choucroute", "Paul Verlaine", "Strasbourg", "grand-est.png"),
    BOURGOGNE_FRANCHE_COMTE("Bourgogne-Franche-Comté", 47.322047, 5.04148, Epoque.NOUVELLE, Zone.METROPOLE, 47784, "le bœuf bourguignon", "Victor Hugo", "Dijon", "bourgogne-franche-comte.png"),
    AUVERGNE_RHONE_ALPES("Auvergne-Rhône-Alpes", 45.764043, 4.835659, Epoque.NOUVELLE, Zone.METROPOLE, 69711, "le coq au vin", "Jean Moulin", "Lyon", "auvergne-rhone-alpes.png"),
    NOUVELLE_AQUITAINE("Nouvelle-Aquitaine", 44.837789, -0.57918, Epoque.NOUVELLE, Zone.METROPOLE, 84060, "les huîtres du Bassin d'Arcachon", "Jacques Ellul", "Bordeaux", "nouvelle-aquitaine.png"),
    OCCITANIE("Occitanie", 43.604652, 1.444209, Epoque.NOUVELLE, Zone.METROPOLE, 72724, "le confit de canard", "Jean Jaurès", "Toulouse", "occitanie.png"),
    PROVENCE_ALPES_COTE_D_AZUR("Provence-Alpes-Côte d'Azur", 43.296482, 5.36978, Epoque.LES_DEUX, Zone.METROPOLE, 31400, "la bouillabaisse", "Fernandel", "Marseille", "provence-alpes-cote-dazur.png"),
    CORSE("Corse", 41.919229, 8.738635, Epoque.LES_DEUX, Zone.METROPOLE, 8680, "le figatellu", "Napoléon Bonaparte", "Ajaccio", "corse.png"),
    MAYOTTE("Mayotte", -12.780600, 45.227800, Epoque.LES_DEUX, Zone.OUTRE_MER, 374, "le matsidza", "Taambati Moussa", "Mamoudzou", "mayotte.png"),
    LA_REUNION("La Réunion", -20.882057, 55.450675, Epoque.LES_DEUX, Zone.OUTRE_MER, 2504, "le rhum arrangé", "Michel Houellebecq", "Saint-Denis", "la-reunion.png"),
    GUYANE("Guyane", 4.9227, -52.3269, Epoque.LES_DEUX, Zone.OUTRE_MER, 83534, "le poulet boucané", "Henri Salvador", "Cayenne", "guyane.png"),
    MARTINIQUE("Martinique", 14.6160647, -61.0587804, Epoque.LES_DEUX, Zone.OUTRE_MER, 1128, "le gratin de bananes", "Aimé Césaire", "Fort-de-France", "martinique.png"),
    GUADELOUPE("Guadeloupe", 17.302606, -62.717692, Epoque.LES_DEUX, Zone.OUTRE_MER, 1703, "les accras de morue", "Teddy Riner", "Basse-Terre", "guadeloupe.png"),

    // anciennes régions (avant 2016) : pas encore d'indices ni d'image
    NORD_PAS_DE_CALAIS("Nord-Pas-de-Calais", 50.62925, 3.057256),
    PICARDIE("Picardie", 49.844067, 2.295753),
    HAUTE_NORMANDIE("Haute-Normandie", 49.443232, 1.099971),
    BASSE_NORMANDIE("Basse-Normandie", 49.182863, -0.370679),
    POITOU_CHARENTES("Poitou-Charentes", 46.580224, 0.340375),
    CENTRE("Centre", 47.902964, 1.909251),
    CHAMPAGNE_ARDENNE("Champagne-Ardenne", 48.956682, 4.363073),
    LORRAINE("Lorraine", 49.1193089, 6.1757156),
    ALSACE("Alsace", 48.5734053, 7.7521113),
    BOURGOGNE("Bourgogne", 47.322047, 5.04148),
    FRANCHE_COMTE("Franche-Comté", 47.237829, 6.0240539),
    RHONE_ALPES("Rhône-Alpes", 45.764043, 4.835659),
    AUVERGNE("Auvergne", 45.777222, 3.087025),
    LIMOUSIN("Limousin", 45.833619, 1.261105),
    AQUITAINE("Aquitaine", 44.837789, -0.57918),
    MIDI_PYRENEES("Midi-Pyrénées", 43.604652, 1.444209),
    LANGUEDOC_ROUSSILLON("Languedoc-Roussillon", 43.610769, 3.876716);

    private final String nom;
    private final double latitude;
    private final double longitude;
    private final Epoque epoque;
    private final Zone zone;
    private final int superficie;
    private final String specialite;
    private final String personnalite;
    private final String capitale;
    private final String image;

    RegionFr(String nom, double latitude, double longitude, Epoque epoque, Zone zone, int superficie,
             String specialite, String personnalite, String capitale, String image) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.epoque = epoque;
        this.zone = zone;
        this.superficie = superficie;
        this.specialite = specialite;
        this.personnalite = personnalite;
        this.capitale = capitale;
        this.image = image;
    }

    // constructeur des anciennes régions, toutes en métropole, qui n'ont ni indices ni image
    RegionFr(String nom, double latitude, double longitude) {
        this(nom, latitude, longitude, Epoque.ANCIENNE, Zone.METROPOLE, 0, "", "", "", null);
    }

    // compilée une seule fois, et non à chaque saisie. Déclarée AVANT le bloc static ci-dessous, qui s'en sert
    // via getCle() : les initialisations static s'exécutent dans l'ordre du fichier.
    private static final Pattern ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    // index des régions par nom simplifié ("bretagne", "iledefrance"...), construit une seule fois
    private static final Map<String, RegionFr> PAR_CLE = new HashMap<>();
    static {
        for (RegionFr region : values()) {
            PAR_CLE.put(region.getCle(), region);
        }
    }

    private static final Random HASARD = new Random();

    // la région correspondant à la saisie du joueur, ou Optional.empty() si elle est inconnue ou refusée dans ce mode
    public static Optional<RegionFr> chercher(String saisie, ModeRegions mode) {
        return Optional.ofNullable(PAR_CLE.get(simplifier(saisie))).filter(mode::reconnait);
    }

    public static List<RegionFr> tirables(ModeRegions mode) {
        List<RegionFr> liste = new ArrayList<>();
        for (RegionFr region : values()) {
            if (mode.peutTirer(region)) {
                liste.add(region);
            }
        }
        return liste;
    }

    public static RegionFr tirer(ModeRegions mode) {
        List<RegionFr> liste = tirables(mode);
        return liste.get(HASARD.nextInt(liste.size()));
    }

    // distance à vol d'oiseau entre les deux capitales régionales, en km
    // loi des cosinus sphérique : d = R * acos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2)*cos(lon1 - lon2))
    public double distance(RegionFr autre) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(autre.latitude);
        double deltaLon = Math.toRadians(longitude - autre.longitude);
        double cosAngle = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        // les arrondis des double peuvent donner 1.0000000000000002, et acos(>1) renvoie NaN
        cosAngle = Math.min(1.0, cosAngle);
        return Math.acos(cosAngle) * RAYON_TERRE;
    }

    private static final int RAYON_TERRE = 6371;

    // "Île-de-France " -> "iledefrance" : sans espaces, majuscules, ponctuation ni accents
    public static String simplifier(String saisie) {
        String texte = saisie.replaceAll("\\s", "");
        texte = texte.toLowerCase();
        texte = texte.replaceAll("\\p{Punct}", "");
        texte = Normalizer.normalize(texte, Normalizer.Form.NFD);
        return ACCENTS.matcher(texte).replaceAll("");
    }

    public String getCle() { return simplifier(nom); }
    public String getNom() { return nom; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Epoque getEpoque() { return epoque; }
    public Zone getZone() { return zone; }
    public int getSuperficie() { return superficie; }
    public String getSpecialite() { return specialite; }
    public String getPersonnalite() { return personnalite; }
    public String getCapitale() { return capitale; }
    // null pour les régions sans image
    public String getImage() { return image; }
}
