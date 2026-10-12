import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class main{


    public static void main (String[] args){
        Parametres lesparametres = new Parametres();
        lesparametres.setChoixdelaregion(1);
        lesparametres.setChoiximage(0);
        lesparametres.setChoixlaliste(0);
        commentjouer didacticiel = new commentjouer();
        int findujeu = - 1;
        while(findujeu !=0){
            String lejeu = JOptionPane.showInputDialog("0 = arrêt, 1 = jeu, 2 = paramètres, 3 = didacticiel. Faites votre choix : ");
            // Echap ou Annuler renvoie null : on quitte le jeu
            if (lejeu == null){
                lejeu = "0";
            }
            try{
            findujeu = Integer.parseInt(lejeu);}
            catch(NumberFormatException nfe){
                findujeu = -2;
            }
            JFrame frame;
            switch(findujeu){
                case 0 :
                    JOptionPane.showMessageDialog( null, "Merci d'avoir joué !", "", JOptionPane.PLAIN_MESSAGE );
                    System.exit(0);
                    break;
                case 1 :
                    ModeRegions mode = ModeRegions.depuisCode(lesparametres.getChoixdelaregion());
                    // une nouvelle Partie à chaque fois : rien ne peut traîner de la partie précédente
                    Partie partie = new Partie(RegionFr.tirer(mode), mode);
                    JOptionPane.showMessageDialog( null, "Quelle est cette région française ?", "", JOptionPane.PLAIN_MESSAGE );
                    representation larep = new representation();
                    if (lesparametres.getChoiximage() == 0) {
                        if ((lesparametres.getChoixdelaregion() == 0) || (lesparametres.getChoixdelaregion() == 2))
                        {
                            larep.setRegion(larep.onmetlimage("autre"));
                        }
                        else{
                            larep.setRegion(larep.onmetlimage(partie.getCible().getCle()));
                        }
                    }
                    else {
                        larep.setRegion(larep.onmetlimage("autre"));
                    }
                        frame = larep.interfacesimple();
                        Message lemessage = new Message();
                        while (partie.enCours()) {
                            lemessage.tour(partie, lesparametres);
                        }
                        lemessage.messagedefin(partie);
                        frame.setVisible(false);
                    break;
                case 2 :
                    String choixdesparam = JOptionPane.showInputDialog("Que voulez-vous modifier ? 0 = retour, 1 = anciennes ou nouvelles régions, 2 = images présentes ou non, 3 = liste des régions ou non");
                    int choixparam = 0;
                    try {
                        choixparam = Integer.parseInt(choixdesparam);
                    }
                    catch(NumberFormatException nfe){
                        choixparam = -2;
                    }
                    int choix;
                    String lechoix;
                    switch(choixparam){
                        case 1 :
                            lechoix = JOptionPane.showInputDialog("0 = anciennes régions, 1 = nouvelles régions, 2 = toutes les régions");
                            try{
                                choix = Integer.parseInt(lechoix);
                            }
                            catch(NumberFormatException nfe){
                                choix = -2;
                            }
                            choix = lesparametres.ancienneounouvelle(choix);
                            break;
                        case 2 :
                            lechoix = JOptionPane.showInputDialog("0 = garder les images, 1 = enlever les images");
                            try{
                                choix = Integer.parseInt(lechoix);
                            }
                            catch(NumberFormatException nfe){
                                choix = -2;
                            }
                            choix = lesparametres.imageounon(choix);
                            break;
                        case 3 :
                            lechoix = JOptionPane.showInputDialog("0 = garder la liste des régions, 1 = enlever la liste des régions");
                            try{
                                choix = Integer.parseInt(lechoix);
                            }
                            catch(NumberFormatException nfe){
                                choix = -2;
                            }
                            choix = lesparametres.choixlisteounon(choix);
                            break;
                        default:
                            JOptionPane.showMessageDialog( null, "Retour au menu", "", JOptionPane.PLAIN_MESSAGE );
                            break;

                    }
                    break;
                case 3 :
                    representation didacticiele = new representation();
                    int boucle = 1;
                    didacticiele.setRegion(didacticiele.jpgjeu());
                    frame = didacticiele.interfacesimple();
                    frame.setVisible(true);
                    while (boucle != 0){
                    String fin = JOptionPane.showInputDialog("0 pour revenir au menu");
                        // Echap ou Annuler renvoie null : on revient au menu
                        if (fin == null){
                            fin = "0";
                        }
                        try{
                            boucle = Integer.parseInt(fin);
                        }
                        catch(NumberFormatException nfe){
                            // saisie qui n'est pas un nombre : on reste dans le didacticiel
                            boucle = 1;
                        }
                    }
                    frame.setVisible(false);
                    break;
                default :
                    JOptionPane.showMessageDialog( null, "Retour au menu", "", JOptionPane.PLAIN_MESSAGE );
                    break;
        }



        }

    }



    /*
    //commentaire test

    public possibilitedelutilisateur (){
        tu connais, on lui donne des possibilites entre commentjouer, parametres, statistiques, faire des guess, ça me paraît bien
    }
    public verifsens(){
        bon en gros c'est ce qui permettrait de tourner la flèche dans le bon sens
        il y aurait genre 8 flèches maximum du coup dans les sens cardinaux, faut trouver une méthode MATHEMATIQUE STONK
    }
     */
}

class Message{

    private static final String[] TITRES = {"Essai un", "Essai deux", "Essai trois", "Essai quatre"};
    private static final String[] LIBELLES = {" Superficie : ", " Premier indice : ", " Deuxième indice : ", " Troisième indice : "};

    // un tour de jeu : demande une proposition, la transmet à la partie, puis affiche ce qu'elle répond
    public void tour(Partie partie, Parametres lesparametres){
        String saisie;
        if (lesparametres.getChoixlaliste() == 0) {
            saisie = JOptionPane.showInputDialog("Quelle est votre supposition ? (1 pour avoir la liste) (Echap pour abandonner) \n \n"
                    + historique(partie));
        }
        else {
            saisie = JOptionPane.showInputDialog("Quelle est votre supposition ? (Echap pour abandonner)");
        }
        // Echap ou Annuler renvoie null : le joueur abandonne
        if (saisie == null){
            partie.abandonner();
            return;
        }
        // la liste existe dans les trois modes : "1" n'est plus réservé au mode "nouvelles régions"
        if (saisie.equals("1") && (lesparametres.getChoixlaliste() == 0)){
            touteslesregions(ModeRegions.depuisCode(lesparametres.getChoixdelaregion()));
            return;
        }
        Optional<Essai> essai = partie.proposer(saisie);
        if (essai.isEmpty()){
            JOptionPane.showMessageDialog( null, "La saisie n'a pas été reconnue \n \n" +
                    "Veuillez réessayer", "Saisie non reconnue", JOptionPane.PLAIN_MESSAGE );
        }
        // partie terminée (victoire ou 5e essai raté) : c'est le message de fin qui s'affiche
        else if (partie.enCours()){
            essairate(partie, essai.get());
        }
    }

    private void essairate(Partie partie, Essai essai){
        int numero = partie.getEssais().size() - 1;
        Indice indice = Indice.apresEssai(numero).orElseThrow();
        JOptionPane.showMessageDialog(null,
                "C'est raté ! " +
                        "\nVous êtes à " + String.format("%.2f", essai.distance()) + " km." +
                        "\nVotre pourcentage de proximité est de : " + essai.pourcentage() + "%." +
                        "\n" + phrase(indice, partie.getCible()), TITRES[numero], JOptionPane.PLAIN_MESSAGE);
    }

    // l'indice rédigé en phrase, pour le message qui suit un essai raté
    private static String phrase(Indice indice, RegionFr cible){
        switch (indice){
            case SUPERFICIE : return "La superficie de la région recherchée est de " + valeur(indice, cible) + " km².";
            case SPECIALITE : return "La spécialité culinaire de la région recherchée est : " + valeur(indice, cible) + ".";
            case PERSONNALITE : return "La personnalité connue régionale de la région recherchée est : " + valeur(indice, cible) + ".";
            default : return "La capitale régionale de la région recherchée est : " + valeur(indice, cible) + ".";
        }
    }

    // la valeur brute de l'indice, pour l'historique des essais
    private static String valeur(Indice indice, RegionFr cible){
        switch (indice){
            case SUPERFICIE : return avecmilliers(cible.getSuperficie());
            case SPECIALITE : return cible.getSpecialite();
            case PERSONNALITE : return cible.getPersonnalite();
            default : return cible.getCapitale();
        }
    }

    public void messagedefin(Partie partie){
        String felicitations = "";
        String message = "";
        if (partie.getEtat() == Partie.Etat.GAGNEE){
            felicitations = "Bravo ! \n \n";
            message = "Vous avez trouvé la bonne réponse ! \nFélicitations et à demain !";
        }
        else if (partie.getEtat() == Partie.Etat.ABANDONNEE){
            felicitations = "Vous avez abandonné ! \n \n";
            message = "La réponse était " + partie.getCible().getNom() + "\nBon courage et à demain !";
        }
        else {
            felicitations = "C'est raté ! \n \n";
            message = "La réponse était " + partie.getCible().getNom() + "\nBon courage et à demain !";
        }
        JOptionPane.showMessageDialog(null,felicitations +
                "Voici vos essais : \n \n" +
                historique(partie) + " \n" +
                message,"Fin du jeu",JOptionPane.PLAIN_MESSAGE);
    }

    // les 5 lignes "Supposition n : région distance km pourcentage% indice", vides pour les essais pas encore joués
    public static String historique(Partie partie){
        List<Essai> essais = partie.getEssais();
        StringBuilder texte = new StringBuilder();
        for (int i = 0; i < Partie.ESSAIS_MAX; i++){
            String proposition = "";
            String distance = "";
            int pourcentage = 0;
            String indice = "";
            if (i < essais.size()){
                Essai essai = essais.get(i);
                proposition = essai.proposition().getNom();
                boolean trouve = essai.proposition() == partie.getCible();
                distance = trouve ? "0" : String.format("%.2f", essai.distance());
                pourcentage = essai.pourcentage();
                // un essai réussi ne révèle pas d'indice
                if (!trouve){
                    indice = Indice.apresEssai(i).map(lindice -> valeur(lindice, partie.getCible())).orElse("");
                }
            }
            texte.append("Supposition ").append(i + 1).append(" : ").append(proposition).append(" ")
                    .append(distance).append(" km ").append(pourcentage).append("%");
            if (i == 0){
                texte.append(LIBELLES[i]).append(indice).append(" km² \n");
            }
            else if (i < LIBELLES.length){
                texte.append(LIBELLES[i]).append(indice).append("\n");
            }
            else {
                texte.append(" \n");
            }
        }
        return texte.toString();
    }

    // 27208 -> "27 208" : séparateur de milliers à la française. On impose l'espace insécable (U+00A0),
    // car l'espace fine (U+202F) du format français de Java s'affiche en carré vide avec certaines polices.
    public static String avecmilliers(int nombre){
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRENCH);
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0", symboles).format(nombre);
    }

    public void touteslesregions(ModeRegions mode){
        JOptionPane.showMessageDialog( null, "Voici les régions possibles : \n" + listedesregions(mode), "", JOptionPane.PLAIN_MESSAGE );}

    // les noms des régions jouables, dans l'ordre alphabétique français (Île-de-France rangée à la lettre I)
    public static String listedesregions(ModeRegions mode){
        List<String> noms = new ArrayList<>();
        for (RegionFr region : RegionFr.tirables(mode)) {
            noms.add(region.getNom());
        }
        noms.sort(Collator.getInstance(Locale.FRENCH));
        return String.join("\n", noms);
    }


}

class Parametres{

    private int choixdelaregion;
    private int choiximage;
    private int choixlaliste;

    public int getChoixdelaregion() {
        return choixdelaregion;
    }

    public void setChoixdelaregion(int choixdelaregion) {
        this.choixdelaregion = choixdelaregion;
    }

    public int getChoiximage() {
        return choiximage;
    }

    public void setChoiximage(int choiximage) {
        this.choiximage = choiximage;
    }


    public int getChoixlaliste() {
        return choixlaliste;
    }

    public void setChoixlaliste(int choixlaliste) {
        this.choixlaliste = choixlaliste;
    }

    public int ancienneounouvelle(int choixregion){
        if (choixregion == 0)
        {
            //tour de magie pour permettre le fonctionnement avec les anciennes régions, probablement avec des booleebs
            JOptionPane.showMessageDialog( null, "Les anciennes régions sont maintenant jouables", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoixdelaregion(0);
        }
        else if (choixregion == 1){
            //même tour de magie
            JOptionPane.showMessageDialog( null, "Les nouvelles régions sont maintenant jouables", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoixdelaregion(1);
            //donner la possibilite de jouer avec toutes les regions en meme temps ?
        }
        else if (choixregion == 2){
            JOptionPane.showMessageDialog( null, "Toutes les régions sont maintenant jouables", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoixdelaregion(2);
        }
        else {
            JOptionPane.showMessageDialog( null, "Pas de changements", "", JOptionPane.PLAIN_MESSAGE );
        }
        return choixregion;
    }

    public int imageounon(int choiximage){
        if (choiximage == 0){
            JOptionPane.showMessageDialog( null, "Vous jouez avec les images", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoiximage(0);
        }
        else if (choiximage == 1){
            JOptionPane.showMessageDialog( null, "Vous jouez sans les images", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoiximage(1);
        }
        else {
            JOptionPane.showMessageDialog( null, "Pas de changements", "", JOptionPane.PLAIN_MESSAGE );
        }
        return choiximage;
    }

    public int choixlisteounon(int choixliste){
        if (choixliste == 0){
            JOptionPane.showMessageDialog( null, "Vous jouez avec la liste", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoixlaliste(0);
        }
        else if (choixliste == 1){
            JOptionPane.showMessageDialog( null, "Vous jouez sans la liste", "", JOptionPane.PLAIN_MESSAGE );
            this.setChoixlaliste(1);
        }
        else {
            JOptionPane.showMessageDialog( null, "Pas de changements", "", JOptionPane.PLAIN_MESSAGE );
        }
        return choixliste;
    }
    /*
    private int unite (0 = km, 1 = miles)
    private int theme (0 = light, 1 = dark)
    private int langage (a voir en fonction de notre ambition)
    private int modedejeu (choix possible entre les anciennes et les nouvelles régions par exemple, 0  = ancienne, 1 = nouvelle (pourquoi pas mettre les départements ?)


    public proximitecontretaille(){
        verifier ce que ça fait dans le jeu
    }

    public supprimerimage(){
        possibilite de supprimer l'image pour faire des guess plus dur
    }

    public sensimage(){
        possibilite de aleatoirement modifier le sens de l'image
    }

     */
}

/*
Les régions jouables : chaque information n'est écrite qu'ici, une seule fois.
Le reste du code vient chercher ses données dans cet enum.
 */
enum Epoque { ANCIENNE, NOUVELLE, LES_DEUX }

enum Zone { METROPOLE, OUTRE_MER }

enum ModeRegions {
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
    public boolean peutTirer(RegionFr region) {
        switch (this) {
            case ANCIENNES : return region.getEpoque() != Epoque.NOUVELLE;
            case NOUVELLES : return region.getEpoque() != Epoque.ANCIENNE;
            default : return true;
        }
    }

    // les régions acceptées comme proposition du joueur : tout, sauf les anciennes en mode "nouvelles"
    public boolean reconnait(RegionFr region) {
        return this != NOUVELLES || region.getEpoque() != Epoque.ANCIENNE;
    }
}

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

/*
Les règles du jeu, sans aucun affichage : Partie reçoit les saisies du joueur et dit ce qui s'est passé.
C'est l'interface (main, Message) qui demande les saisies et affiche les résultats.
 */

// une proposition jouée : la région proposée, sa distance à la cible (km) et le pourcentage de proximité
record Essai(RegionFr proposition, double distance, int pourcentage) {}

// les indices, dans l'ordre où ils sont révélés : un après chacun des quatre premiers essais ratés
enum Indice {
    SUPERFICIE, SPECIALITE, PERSONNALITE, CAPITALE;

    // l'indice révélé après l'essai raté numéro "numero" (0 = le premier), rien après le quatrième
    public static Optional<Indice> apresEssai(int numero) {
        if (numero < 0 || numero >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[numero]);
    }
}

class Partie {

    public static final int ESSAIS_MAX = 5;

    enum Etat { EN_COURS, GAGNEE, PERDUE, ABANDONNEE }

    private final RegionFr cible;
    private final ModeRegions mode;
    private final List<Essai> essais = new ArrayList<>();
    private Etat etat = Etat.EN_COURS;

    public Partie(RegionFr cible, ModeRegions mode) {
        this.cible = cible;
        this.mode = mode;
    }

    // joue la saisie du joueur : renvoie l'essai joué, ou Optional.empty() si la saisie n'est pas reconnue
    // (une saisie non reconnue ne coûte pas d'essai)
    public Optional<Essai> proposer(String saisie) {
        verifierEnCours();
        Optional<RegionFr> trouvee = RegionFr.chercher(saisie, mode);
        if (trouvee.isEmpty()) {
            return Optional.empty();
        }
        RegionFr proposition = trouvee.get();
        double distance = cible.distance(proposition);
        Essai essai = new Essai(proposition, distance, pourcentage(cible, distance));
        essais.add(essai);
        if (proposition == cible) {
            etat = Etat.GAGNEE;
        } else if (essais.size() == ESSAIS_MAX) {
            etat = Etat.PERDUE;
        }
        return Optional.of(essai);
    }

    public void abandonner() {
        verifierEnCours();
        etat = Etat.ABANDONNEE;
    }

    // jouer dans une partie terminée est une erreur de programmation, pas une saisie du joueur
    private void verifierEnCours() {
        if (etat != Etat.EN_COURS) {
            throw new IllegalStateException("La partie est terminée : " + etat);
        }
    }

    public RegionFr getCible() { return cible; }
    public Etat getEtat() { return etat; }
    public boolean enCours() { return etat == Etat.EN_COURS; }
    // une vue en lecture seule : l'interface peut lire les essais, pas en ajouter
    public List<Essai> getEssais() { return Collections.unmodifiableList(essais); }

    // pourcentage de proximité : 100 % à 0 km, 0 % pour la région de référence la plus éloignée de la cible.
    // Pour une cible en métropole, seules les régions de métropole servent de référence : sinon l'outre-mer,
    // à plus de 6000 km, écrase toute la métropole entre 88 % et 98 %.
    public static int pourcentage(RegionFr cible, double distance) {
        double distanceMax = 0;
        for (RegionFr autre : RegionFr.values()) {
            if (cible.getZone() == Zone.OUTRE_MER || autre.getZone() == Zone.METROPOLE) {
                distanceMax = Math.max(distanceMax, cible.distance(autre));
            }
        }
        // arrondi et non troncature : (int) 99.9999 donnerait 99 % pour une distance de quelques centimètres
        int pourcentage = (int) Math.round((distanceMax - distance) * 100 / distanceMax);
        // une proposition en outre-mer pour une cible en métropole dépasse la référence : 0 %, pas un pourcentage négatif
        return Math.max(0, pourcentage);
    }

    // Brouillon de la flèche de direction (à reprendre en Phase 4), recopié tel quel depuis l'ancienne classe Region :
    // les degrés ne sont pas convertis en radians, et l'azimut n'est pas encore découpé en 8 directions.
    public static String fleche(RegionFr cible, RegionFr proposition){
        String sens = "";
        double resultat;

        double x = Math.cos(cible.getLatitude())*Math.sin(proposition.getLatitude()) - Math.sin(cible.getLatitude()) * Math.cos(proposition.getLatitude()) * Math.cos((proposition.getLongitude()) - (cible.getLongitude()));
        double y = Math.sin((proposition.getLongitude())-(cible.getLongitude())) * Math.cos(proposition.getLatitude());

        resultat = Math.atan2(y,x);

        System.out.println(resultat);
        return sens;
    }
}

class representation{

    /*
    class permettant de charger les images
    Les images sont des ressources : Maven les copie depuis src/main/resources à côté des classes compilées
    (et dans le .jar). On les cherche donc dans le classpath, quel que soit le dossier d'où le jeu est lancé.
     */
    String chemin = "regions/";
    // l'image, ou null si la ressource n'existe pas ou ne se lit pas
    public static BufferedImage Image(String path) {
        // "/" au début : chemin depuis la racine du classpath, et non depuis le package de la classe
        URL ressource = representation.class.getResource("/" + path);
        if (ressource == null) {
            return null;
        }
        try {
            return javax.imageio.ImageIO.read(ressource);
        } catch (IOException e) {
            return null;
        }
    }
    private Image region = representation.Image("toto");

    public void setRegion(Image region) {
        this.region = region;
    }

    public JFrame interfacesimple(){
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(this.getRegion())));
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    public Image onmetlimage(String region){
        // l'image de la région si elle en a une ; sinon (clé inconnue, ancienne région) l'image par défaut
        String fichier = RegionFr.chercher(region, ModeRegions.TOUTES)
                .map(RegionFr::getImage)
                .orElse("defaut.png");
        this.region = representation.Image(chemin + fichier);
        return this.region;
    }
    public Image getRegion() {
        return region;
    }


    public Image jpgjeu(){
        this.region = representation.Image(chemin + "defaut.png");
        //ajouter commentjouer.png
        return this.region;
    }

}


class commentjouer {

    //class commentjouer extends JFrame implements ActionListener

    /*
    Objectif : mettre en place un code qui permet de mettre en avant comment jouer
    Soit avec un bouton directement sur le JavaScript
    Soit on niveau du menu avec un choix chiffré, en fonction de notre avancement
    Et de la connaissance au niveau de l'utilisation des boutons JavaScript
     */


}

/*

Tentative d'interface non utilisée

class interfacerapide extends JFrame implements ActionListener{
    private JTextField tfEntree;
    private JButton OKButton;
    private JPanel myPanel;
    private JLabel tfEntre;
    private JLabel Sortie;


    public void notreinterface() {
        setTitle("Francle");
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        //setContentPane(myPanel);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        tfEntree.setText("");
        Sortie.setText("");
    }
}


 */
/*
class Statistiques{
    ok donc c'est en bonus avec des stats à modifier en fonction des tests
    inspiration de wordle
    private int nombreparties
    private int nombresdepartiesgagnées


    public calculpourcentagevictoire(){
        calcul simple du pourcentage de victoire à envoyer si l'utilisateur demande des statistiques
    }

    public repartitionvictoire(){
        se débrouiller pour sauvegarder un tableau avec les victoires de l'utilisateur, il pourra savoir, quand il a gagné, s'il a gagné en un guess, deux guess, trois guess, etc... jusqu'à 6
    }

    possibilité de faire un reset ?
    possibilité d'avoir des utilisateurs différents ?
}



class internet ?{

        }*/
/*
Obligation de faire une classe convertisseur pour essayer de prendre les suppositions des utilisateurs ratées ou vérifier les accents et les tirets ?
 */

