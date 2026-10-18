package fr.francle.donnees;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
Les régions jouables : chaque information n'est écrite qu'ici, une seule fois.
Le reste du code vient chercher ses données dans cet enum.
 */
public enum RegionFr {
    // nouvelles régions et régions inchangées : nom, latitude, longitude, époque, zone, superficie,
    // spécialité culinaire, personnalité, capitale régionale, monument, image.
    // Les noms et les indices en français et en anglais (repris de la version web) ; la capitale, un nom de ville,
    // est la même dans les deux langues
    HAUTS_DE_FRANCE(Texte.pareil("Hauts-de-France"), 50.62925, 3.057256, Epoque.NOUVELLE, Zone.METROPOLE, 31814,
            new Texte("la bière", "beer"),
            Texte.pareil("Charles de Gaulle"), "Lille",
            new Texte("la cathédrale d'Amiens", "Amiens Cathedral"), "hauts-de-france.png"),
    NORMANDIE(new Texte("Normandie", "Normandy"), 49.443232, 1.099971, Epoque.NOUVELLE, Zone.METROPOLE, 29907,
            new Texte("la matelote", "matelote"),
            new Texte("Guillaume le Conquérant", "William the Conqueror"), "Rouen",
            new Texte("le Mont-Saint-Michel", "Mont-Saint-Michel"), "normandie.png"),
    BRETAGNE(new Texte("Bretagne", "Brittany"), 48.117266, -1.6777926, Epoque.LES_DEUX, Zone.METROPOLE, 27208,
            new Texte("la crêpe", "crêpes"),
            Texte.pareil("Louis Guilloux"), "Rennes",
            new Texte("les alignements de Carnac", "the Carnac stones"), "bretagne.png"),
    PAYS_DE_LA_LOIRE(Texte.pareil("Pays de la Loire"), 47.218371, -1.553621, Epoque.LES_DEUX, Zone.METROPOLE, 32082,
            new Texte("la fouace", "fouace"),
            Texte.pareil("Jules Verne"), "Nantes",
            new Texte("le château de Saumur", "Saumur Castle"), "pays-de-la-loire.png"),
    ILE_DE_FRANCE(Texte.pareil("Île-de-France"), 48.856614, 2.3522219, Epoque.LES_DEUX, Zone.METROPOLE, 12012,
            new Texte("le macaron", "macarons"),
            Texte.pareil("Alexandre Dumas"), "Paris",
            new Texte("la tour Eiffel", "the Eiffel Tower"), "ile-de-france.png"),
    CENTRE_VAL_DE_LOIRE(Texte.pareil("Centre-Val de Loire"), 47.902964, 1.909251, Epoque.NOUVELLE, Zone.METROPOLE, 39151,
            new Texte("la tarte Tatin", "tarte Tatin"),
            new Texte("Jeanne d'Arc", "Joan of Arc"), "Orléans",
            new Texte("le château de Chambord", "Château de Chambord"), "centre-val-de-loire.png"),
    GRAND_EST(Texte.pareil("Grand Est"), 48.5734053, 7.7521113, Epoque.NOUVELLE, Zone.METROPOLE, 57433,
            new Texte("la choucroute", "choucroute"),
            Texte.pareil("Paul Verlaine"), "Strasbourg",
            new Texte("la cathédrale de Reims", "Reims Cathedral"), "grand-est.png"),
    BOURGOGNE_FRANCHE_COMTE(new Texte("Bourgogne-Franche-Comté", "Burgundy-Franche-Comté"), 47.322047, 5.04148, Epoque.NOUVELLE, Zone.METROPOLE, 47784,
            new Texte("le bœuf bourguignon", "beef bourguignon"),
            Texte.pareil("Victor Hugo"), "Dijon",
            new Texte("les Hospices de Beaune", "the Hospices de Beaune"), "bourgogne-franche-comte.png"),
    AUVERGNE_RHONE_ALPES(Texte.pareil("Auvergne-Rhône-Alpes"), 45.764043, 4.835659, Epoque.NOUVELLE, Zone.METROPOLE, 69711,
            new Texte("le coq au vin", "coq au vin"),
            Texte.pareil("Jean Moulin"), "Lyon",
            new Texte("le mont Blanc", "Mont Blanc"), "auvergne-rhone-alpes.png"),
    NOUVELLE_AQUITAINE(Texte.pareil("Nouvelle-Aquitaine"), 44.837789, -0.57918, Epoque.NOUVELLE, Zone.METROPOLE, 84060,
            new Texte("les huîtres du Bassin d'Arcachon", "Arcachon Bay oysters"),
            Texte.pareil("Montaigne"), "Bordeaux",
            new Texte("la dune du Pilat", "the Dune of Pilat"), "nouvelle-aquitaine.png"),
    OCCITANIE(new Texte("Occitanie", "Occitania"), 43.604652, 1.444209, Epoque.NOUVELLE, Zone.METROPOLE, 72724,
            new Texte("le confit de canard", "duck confit"),
            Texte.pareil("Jean Jaurès"), "Toulouse",
            new Texte("la cité de Carcassonne", "the medieval city of Carcassonne"), "occitanie.png"),
    PROVENCE_ALPES_COTE_D_AZUR(Texte.pareil("Provence-Alpes-Côte d'Azur"), 43.296482, 5.36978, Epoque.LES_DEUX, Zone.METROPOLE, 31400,
            new Texte("la bouillabaisse", "bouillabaisse"),
            Texte.pareil("Fernandel"), "Marseille",
            new Texte("le palais des Papes d'Avignon", "the Palace of the Popes in Avignon"), "provence-alpes-cote-dazur.png"),
    CORSE(new Texte("Corse", "Corsica"), 41.919229, 8.738635, Epoque.LES_DEUX, Zone.METROPOLE, 8680,
            new Texte("le figatellu", "figatellu"),
            new Texte("Napoléon Bonaparte", "Napoleon Bonaparte"), "Ajaccio",
            new Texte("les calanques de Piana", "the Calanques of Piana"), "corse.png"),
    MAYOTTE(Texte.pareil("Mayotte"), -12.780600, 45.227800, Epoque.LES_DEUX, Zone.OUTRE_MER, 374,
            new Texte("le mataba", "mataba"),
            Texte.pareil("Zéna M'Déré"), "Mamoudzou",
            new Texte("le lac Dziani", "Lake Dziani"), "mayotte.png"),
    LA_REUNION(new Texte("La Réunion", "Réunion"), -20.882057, 55.450675, Epoque.LES_DEUX, Zone.OUTRE_MER, 2504,
            new Texte("le rhum arrangé", "rhum arrangé"),
            Texte.pareil("Michel Houellebecq"), "Saint-Denis",
            new Texte("le piton de la Fournaise", "Piton de la Fournaise"), "la-reunion.png"),
    GUYANE(new Texte("Guyane", "French Guiana"), 4.9227, -52.3269, Epoque.LES_DEUX, Zone.OUTRE_MER, 83534,
            new Texte("le poulet boucané", "poulet boucané"),
            Texte.pareil("Henri Salvador"), "Cayenne",
            new Texte("le centre spatial de Kourou", "the Kourou space centre"), "guyane.png"),
    MARTINIQUE(Texte.pareil("Martinique"), 14.6160647, -61.0587804, Epoque.LES_DEUX, Zone.OUTRE_MER, 1128,
            new Texte("le gratin de bananes", "banana gratin"),
            Texte.pareil("Aimé Césaire"), "Fort-de-France",
            new Texte("la montagne Pelée", "Mount Pelée"), "martinique.png"),
    GUADELOUPE(Texte.pareil("Guadeloupe"), 17.302606, -62.717692, Epoque.LES_DEUX, Zone.OUTRE_MER, 1703,
            new Texte("les accras de morue", "accras"),
            Texte.pareil("Teddy Riner"), "Basse-Terre",
            new Texte("le volcan de la Soufrière", "La Soufrière volcano"), "guadeloupe.png"),

    // anciennes régions (avant 2016)
    NORD_PAS_DE_CALAIS(Texte.pareil("Nord-Pas-de-Calais"), 50.62925, 3.057256, 12414,
            new Texte("le potjevleesch", "potjevleesch"),
            Texte.pareil("Robespierre"), "Lille",
            new Texte("le beffroi d'Arras", "the Arras belfry"), "nord-pas-de-calais.png"),
    PICARDIE(new Texte("Picardie", "Picardy"), 49.844067, 2.295753, 19399,
            new Texte("la ficelle picarde", "ficelle picarde"),
            Texte.pareil("Jean de La Fontaine"), "Amiens",
            new Texte("la baie de Somme", "the Bay of the Somme"), "picardie.png"),
    HAUTE_NORMANDIE(new Texte("Haute-Normandie", "Upper Normandy"), 49.443232, 1.099971, 12317,
            new Texte("le canard à la rouennaise", "Rouen-style duck"),
            Texte.pareil("Gustave Flaubert"), "Rouen",
            new Texte("les falaises d'Étretat", "the cliffs of Étretat"), "haute-normandie.png"),
    BASSE_NORMANDIE(new Texte("Basse-Normandie", "Lower Normandy"), 49.182863, -0.370679, 17589,
            new Texte("le camembert", "Camembert"),
            new Texte("Guillaume le Conquérant", "William the Conqueror"), "Caen",
            new Texte("le Mont-Saint-Michel", "Mont-Saint-Michel"), "basse-normandie.png"),
    POITOU_CHARENTES(Texte.pareil("Poitou-Charentes"), 46.580224, 0.340375, 25810,
            new Texte("le cognac", "cognac"),
            Texte.pareil("François Mitterrand"), "Poitiers",
            Texte.pareil("Fort Boyard"), "poitou-charentes.png"),
    CENTRE(Texte.pareil("Centre"), 47.902964, 1.909251, 39151,
            new Texte("la tarte Tatin", "tarte Tatin"),
            Texte.pareil("Honoré de Balzac"), "Orléans",
            new Texte("le château de Chambord", "Château de Chambord"), "centre.png"),
    CHAMPAGNE_ARDENNE(Texte.pareil("Champagne-Ardenne"), 48.956682, 4.363073, 25606,
            new Texte("le champagne", "champagne"),
            Texte.pareil("Arthur Rimbaud"), "Châlons-en-Champagne",
            new Texte("la cathédrale de Reims", "Reims Cathedral"), "champagne-ardenne.png"),
    LORRAINE(Texte.pareil("Lorraine"), 49.1193089, 6.1757156, 23547,
            new Texte("la quiche lorraine", "quiche Lorraine"),
            new Texte("Jeanne d'Arc", "Joan of Arc"), "Metz",
            new Texte("la place Stanislas à Nancy", "Place Stanislas in Nancy"), "lorraine.png"),
    ALSACE(Texte.pareil("Alsace"), 48.5734053, 7.7521113, 8280,
            new Texte("la flammekueche", "flammekueche"),
            Texte.pareil("Albert Schweitzer"), "Strasbourg",
            new Texte("le château du Haut-Kœnigsbourg", "Haut-Kœnigsbourg Castle"), "alsace.png"),
    BOURGOGNE(new Texte("Bourgogne", "Burgundy"), 47.322047, 5.04148, 31582,
            new Texte("les escargots de Bourgogne", "snails cooked in garlic butter"),
            Texte.pareil("Gustave Eiffel"), "Dijon",
            new Texte("les Hospices de Beaune", "the Hospices de Beaune"), "bourgogne.png"),
    FRANCHE_COMTE(Texte.pareil("Franche-Comté"), 47.237829, 6.0240539, 16202,
            new Texte("le comté", "Comté cheese"),
            Texte.pareil("Louis Pasteur"), "Besançon",
            new Texte("la saline royale d'Arc-et-Senans", "the Royal Saltworks at Arc-et-Senans"), "franche-comte.png"),
    RHONE_ALPES(Texte.pareil("Rhône-Alpes"), 45.764043, 4.835659, 43698,
            new Texte("le gratin dauphinois", "gratin dauphinois"),
            Texte.pareil("Antoine de Saint-Exupéry"), "Lyon",
            new Texte("le mont Blanc", "Mont Blanc"), "rhone-alpes.png"),
    AUVERGNE(Texte.pareil("Auvergne"), 45.777222, 3.087025, 26013,
            new Texte("la truffade", "truffade"),
            Texte.pareil("Blaise Pascal"), "Clermont-Ferrand",
            new Texte("le puy de Dôme", "the Puy de Dôme"), "auvergne.png"),
    LIMOUSIN(Texte.pareil("Limousin"), 45.833619, 1.261105, 16942,
            new Texte("le clafoutis", "clafoutis"),
            Texte.pareil("Auguste Renoir"), "Limoges",
            new Texte("le lac de Vassivière", "Lake Vassivière"), "limousin.png"),
    AQUITAINE(Texte.pareil("Aquitaine"), 44.837789, -0.57918, 41309,
            new Texte("le canelé", "canelés"),
            Texte.pareil("Montesquieu"), "Bordeaux",
            new Texte("la dune du Pilat", "the Dune of Pilat"), "aquitaine.png"),
    MIDI_PYRENEES(Texte.pareil("Midi-Pyrénées"), 43.604652, 1.444209, 45348,
            new Texte("la saucisse de Toulouse", "Toulouse sausage"),
            Texte.pareil("Henri de Toulouse-Lautrec"), "Toulouse",
            new Texte("le cirque de Gavarnie", "the Cirque de Gavarnie"), "midi-pyrenees.png"),
    LANGUEDOC_ROUSSILLON(Texte.pareil("Languedoc-Roussillon"), 43.610769, 3.876716, 27376,
            new Texte("le cassoulet", "cassoulet"),
            Texte.pareil("Georges Brassens"), "Montpellier",
            new Texte("le pont du Gard", "the Pont du Gard"), "languedoc-roussillon.png");

    private final Texte nom;
    private final double latitude;
    private final double longitude;
    private final Epoque epoque;
    private final Zone zone;
    private final int superficie;
    private final Texte specialite;
    private final Texte personnalite;
    private final String capitale;
    private final Texte monument;
    private final String image;

    RegionFr(Texte nom, double latitude, double longitude, Epoque epoque, Zone zone, int superficie,
             Texte specialite, Texte personnalite, String capitale, Texte monument, String image) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.epoque = epoque;
        this.zone = zone;
        this.superficie = superficie;
        this.specialite = specialite;
        this.personnalite = personnalite;
        this.capitale = capitale;
        this.monument = monument;
        this.image = image;
    }

    // constructeur des anciennes régions : toutes en métropole. Comme l'autre, il exige les cinq indices et l'image :
    // une région incomplète ne compile pas
    RegionFr(Texte nom, double latitude, double longitude, int superficie,
             Texte specialite, Texte personnalite, String capitale, Texte monument, String image) {
        this(nom, latitude, longitude, Epoque.ANCIENNE, Zone.METROPOLE, superficie, specialite, personnalite, capitale,
                monument, image);
    }

    // compilée une seule fois, et non à chaque saisie. Déclarée AVANT le bloc static ci-dessous, qui s'en sert
    // via getCle() : les initialisations static s'exécutent dans l'ordre du fichier.
    private static final Pattern ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    // index des régions par nom simplifié, en français ET en anglais ("bretagne", "brittany", "iledefrance"...),
    // construit une seule fois : le joueur peut écrire l'un ou l'autre, quelle que soit la langue choisie
    private static final Map<String, RegionFr> PAR_CLE = new HashMap<>();
    static {
        for (RegionFr region : values()) {
            for (Langue langue : Langue.values()) {
                PAR_CLE.put(simplifier(region.getNom(langue)), region);
            }
        }
    }

    private static final Random HASARD = new Random();

    // la région correspondant à la saisie du joueur, ou Optional.empty() si elle est inconnue ou refusée dans ce mode
    public static Optional<RegionFr> chercher(String saisie, ModeRegions mode) {
        return chercher(saisie).filter(mode::contient);
    }

    // la région correspondant à la saisie, quelle que soit son époque
    public static Optional<RegionFr> chercher(String saisie) {
        return Optional.ofNullable(PAR_CLE.get(simplifier(saisie)));
    }

    // les suggestions pour un joueur francophone
    public static List<RegionFr> suggestions(String saisie, ModeRegions mode) {
        return suggestions(saisie, mode, Langue.FR);
    }

    // les régions à proposer pendant la frappe, parmi celles acceptées dans ce mode, sans tenir compte des accents,
    // majuscules, espaces et tirets : d'abord celles dont un NOM commence par la saisie ("nor" -> Normandie),
    // puis celles dont un MOT commence par elle ("loire" -> Pays de la Loire) ; chaque groupe par ordre alphabétique
    // du nom dans la langue du joueur. Les noms des deux langues sont cherchés : "bri" trouve la Bretagne (Brittany)
    public static List<RegionFr> suggestions(String saisie, ModeRegions mode, Langue langue) {
        String debut = simplifier(saisie);
        List<RegionFr> parNom = new ArrayList<>();
        List<RegionFr> parMot = new ArrayList<>();
        if (debut.isEmpty()) {
            return parNom;
        }
        for (RegionFr region : values()) {
            if (!mode.contient(region)) {
                continue;
            }
            if (region.noms().anyMatch(nom -> simplifier(nom).startsWith(debut))) {
                parNom.add(region);
            } else if (region.noms().anyMatch(nom -> mots(nom).stream().anyMatch(mot -> mot.startsWith(debut)))) {
                parMot.add(region);
            }
        }
        Comparator<RegionFr> alphabetique = Comparator.comparing(region -> region.getNom(langue),
                Collator.getInstance(langue.getLocale()));
        parNom.sort(alphabetique);
        parMot.sort(alphabetique);
        parNom.addAll(parMot);
        return parNom;
    }

    // le nom de la région dans chaque langue (« Bretagne », « Brittany »)
    private Stream<String> noms() {
        return Arrays.stream(Langue.values()).map(this::getNom);
    }

    // les mots d'un nom, simplifiés, sans les petits mots ("de", "la", "d'") : "Provence-Alpes-Côte d'Azur" ->
    // provence, alpes, cote, azur. Sinon "d" ou "de" ferait apparaître la moitié des régions.
    static List<String> mots(String nom) {
        List<String> mots = new ArrayList<>();
        for (String mot : MOTS.split(nom)) {
            String simple = simplifier(mot);
            if (simple.length() >= 3) {
                mots.add(simple);
            }
        }
        return mots;
    }

    // ce qui sépare les mots d'un nom : espaces, tirets, apostrophes
    private static final Pattern MOTS = Pattern.compile("[\\s\\-'’]+");

    public static List<RegionFr> tirables(ModeRegions mode) {
        List<RegionFr> liste = new ArrayList<>();
        for (RegionFr region : values()) {
            if (mode.contient(region)) {
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

    // "Île-de-France " -> "iledefrance" : sans espaces, majuscules, ponctuation ni accents. \p{Punct} ne connaît que
    // la ponctuation ASCII : on y ajoute l'apostrophe typographique (’), qu'un correcteur ou un clavier tape souvent
    static String simplifier(String saisie) {
        String texte = saisie.replaceAll("\\s", "");
        texte = texte.toLowerCase();
        texte = texte.replaceAll("[\\p{Punct}’]", "");
        texte = Normalizer.normalize(texte, Normalizer.Form.NFD);
        return ACCENTS.matcher(texte).replaceAll("");
    }

    // la clé et le nom tout court sont en français : ils servent à RegionDuJour (le tri qui donne la région du jour
    // ne doit dépendre d'aucune langue choisie), aux fichiers de sauvegarde et aux tests
    public String getCle() { return simplifier(nom.fr()); }
    public String getNom() { return nom.fr(); }
    public String getNom(Langue langue) { return nom.dans(langue); }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Epoque getEpoque() { return epoque; }
    public Zone getZone() { return zone; }
    public int getSuperficie() { return superficie; }
    public Texte getSpecialite() { return specialite; }
    public Texte getPersonnalite() { return personnalite; }
    public String getCapitale() { return capitale; }
    // un lieu célèbre de la région, cinquième et dernier indice (repris de la version web)
    public Texte getMonument() { return monument; }
    // le nom du fichier de la silhouette et des cartes (« bretagne.png »)
    public String getImage() { return image; }
    // le même sans « .png » : le nom de la région dans les fichiers des départements (« bretagne »)
    public String getFichier() { return image.substring(0, image.length() - ".png".length()); }
    // ses départements, par ordre de numéro : ceux de la région à son époque (la Picardie en a trois, les
    // Hauts-de-France cinq)
    public List<Departement> getDepartements() { return Decoupage.de(this); }

    // les régions de l'autre époque qui recouvrent celle-ci : pour l'Alsace, le Grand Est ; pour le Grand Est,
    // l'Alsace, la Champagne-Ardenne et la Lorraine ; aucune pour une région inchangée en 2016 (Bretagne…).
    // Rien de nouveau n'est écrit pour cela : le lien est déduit des départements (deux régions d'époques différentes
    // qui ont un département en commun), que l'on connaît déjà pour le bonus. En 2016, aucun département n'a changé
    // de région : une ancienne région est donc tout entière dans une seule nouvelle
    public List<RegionFr> autreEpoque() {
        if (epoque == Epoque.LES_DEUX) {
            return List.of();
        }
        Epoque autre = epoque == Epoque.ANCIENNE ? Epoque.NOUVELLE : Epoque.ANCIENNE;
        return Arrays.stream(values())
                .filter(region -> region.epoque == autre)
                .filter(region -> region.getDepartements().stream().anyMatch(getDepartements()::contains))
                .toList();
    }
}
