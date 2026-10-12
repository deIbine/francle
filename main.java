import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Objects;
import java.text.Collator;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class main{


    public static void main (String[] args){
        Parametres lesparametres = new Parametres();
        lesparametres.setChoixdelaregion(1);
        lesparametres.setChoiximage(0);
        lesparametres.setChoixlaliste(0);
        commentjouer didacticiel = new commentjouer();
        superinterface[] jeusympa = new superinterface[100];
        superinterface temporaire = new superinterface();
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
                    Region Francaise = Region.depuis(RegionFr.tirer(ModeRegions.depuisCode(lesparametres.getChoixdelaregion())));
                    int compteur=0;
                    // on repart de 5 essais vierges à chaque partie
                    for (int i = 0; i < 5; i++){
                        jeusympa[i] = temporaire.superi();
                    }
                    JOptionPane.showMessageDialog( null, "Quelle est cette région française ?", "", JOptionPane.PLAIN_MESSAGE );
                    representation larep = new representation();
                    if (lesparametres.getChoiximage() == 0) {
                        if ((lesparametres.getChoixdelaregion() == 0) || (lesparametres.getChoixdelaregion() == 2))
                        {
                            larep.setRegion(larep.onmetlimage("autre"));
                        }
                        else{
                            larep.setRegion(larep.onmetlimage(Francaise.getNomdelaregion()));
                        }
                    }
                    else {
                        larep.setRegion(larep.onmetlimage("autre"));
                    }
                        frame = larep.interfacesimple();
                        while (compteur != 5) {
                            compteur = Francaise.jeu(Francaise, compteur, lesparametres, jeusympa);
                        }
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
                            fin = "1";
                        }
                        boucle = Integer.parseInt(fin);
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
    public void message(int code, double distanceux, int pourcentage, int compteur, Region laregion){
        String distance = String.format("%.2f",distanceux);
        switch (code){
            case 1 :
                JOptionPane.showMessageDialog( null, "Raté ! \n \n" +
                        "Vous avez abandonné le jeu !", "Abandon", JOptionPane.PLAIN_MESSAGE );
                break;
            case 2 :
                JOptionPane.showMessageDialog( null, "La saisie n'a pas été reconnue \n \n" +
                        "Veuillez réessayer", "Saisie non reconnue", JOptionPane.PLAIN_MESSAGE );
                break;
            case 3 :
                switch (compteur){
                    case 0 :
                        JOptionPane.showMessageDialog(null,
                                "C'est raté ! " +
                                        "\nVous êtes à " + distance + " km." +
                                        "\nVotre pourcentage de proximité est de : " + pourcentage + "%." +
                                        "\nLa superficie de la région recherchée est de " + laregion.getSuperficie() + " km.", "Essai un",JOptionPane.PLAIN_MESSAGE);
                        break;
                    case 1 :
                        JOptionPane.showMessageDialog(null,
                                "C'est raté ! " +
                                        "\nVous êtes à " + distance + " km." +
                                        "\nVotre pourcentage de proximité est de : " + pourcentage + "%." +
                                        "\nLa spécialité culinaire de la région recherchée est : " + laregion.getIndiceun() + ".", "Essai deux",JOptionPane.PLAIN_MESSAGE);
                        break;
                    case 2 :
                        JOptionPane.showMessageDialog(null,
                                "C'est raté ! " +
                                        "\nVous êtes à " + distance + " km." +
                                        "\nVotre pourcentage de proximité est de : " + pourcentage + "%." +
                                        "\nLa personnalité connue régionale de la région recherchée est : " + laregion.getIndicedeux() + ".", "Essai trois",JOptionPane.PLAIN_MESSAGE);
                        break;
                    case 3 :
                        JOptionPane.showMessageDialog(null,
                                "C'est raté ! " +
                                        "\nVous êtes à " + distance + " km." +
                                        "\nVotre pourcentage de proximité est de : " + pourcentage + "%." +
                                        "\nLa capitale régionale de la région recherchée est : " + laregion.getIndicetrois() + ".", "Essai quatre",JOptionPane.PLAIN_MESSAGE);
                        break;
                }
        break;
        }
    }
    public void messagedefin(superinterface [] jeusympa, Region laregion, int code){
        String felicitations = "";
        String message = "";
        if (code == 0){
            felicitations = "Bravo ! \n \n";
            message = "Vous avez trouvé la bonne réponse ! \nFélicitations et à demain !";
        }
        else {
            felicitations = "C'est raté ! \n \n";
            message = "La réponse était " + laregion.getVrainom() + "\nBon courage et à demain !";
        }
        JOptionPane.showMessageDialog(null,felicitations +
                "Voici vos essais : \n \n" +
                "Supposition 1 : " + jeusympa[0].getSupposition() + " " + jeusympa[0].getDistance() + " km " + jeusympa[0].getPourcentage() + "%" + " Superficie : " + jeusympa[0].getIndice() + " km \n" +
                "Supposition 2 : " + jeusympa[1].getSupposition() + " " + jeusympa[1].getDistance() + " km " + jeusympa[1].getPourcentage() + "% Premier indice : " + jeusympa[1].getIndice() + "\n"+
                "Supposition 3 : " + jeusympa[2].getSupposition() + " " + jeusympa[2].getDistance() + " km " + jeusympa[2].getPourcentage() + "% Deuxième indice : " + jeusympa[2].getIndice() + "\n"+
                "Supposition 4 : " + jeusympa[3].getSupposition() + " " + jeusympa[3].getDistance() + " km " + jeusympa[3].getPourcentage() + "% Troisième indice : " + jeusympa[3].getIndice() + "\n" +
                "Supposition 5 : " + jeusympa[4].getSupposition() + " " + jeusympa[4].getDistance() + " km " + jeusympa[4].getPourcentage() + "% \n \n" +
                message,"Fin du jeu",JOptionPane.PLAIN_MESSAGE);
    }


    public String interfaceclassique(superinterface [] jeusympa){
        String supposition = "";
        //Ajout 3

        //trouver un moyen de mettre la superficie après le guess 1 plus ajouter le dernier guess avant de noter quand c'est terminé pour le dernier guess
        supposition = JOptionPane.showInputDialog("Quelle est votre supposition ? (1 pour avoir la liste) (Echap pour abandonner) \n \n" +
                "Supposition 1 : " + jeusympa[0].getSupposition() + " " + jeusympa[0].getDistance() + " km " + jeusympa[0].getPourcentage() + "%" + " Superficie : " + jeusympa[0].getIndice() + " km \n" +
                "Supposition 2 : " + jeusympa[1].getSupposition() + " " + jeusympa[1].getDistance() + " km " + jeusympa[1].getPourcentage() + "% Premier indice : " + jeusympa[1].getIndice() + "\n"+
                "Supposition 3 : " + jeusympa[2].getSupposition() + " " + jeusympa[2].getDistance() + " km " + jeusympa[2].getPourcentage() + "% Deuxième indice : " + jeusympa[2].getIndice() + "\n"+
                "Supposition 4 : " + jeusympa[3].getSupposition() + " " + jeusympa[3].getDistance() + " km " + jeusympa[3].getPourcentage() + "% Troisième indice : " + jeusympa[3].getIndice() + "\n" +
                "Supposition 5 : " + jeusympa[4].getSupposition() + " " + jeusympa[4].getDistance() + " km " + jeusympa[4].getPourcentage() + "% \n");
        return supposition;
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

class superinterface{
    private String distance;
    private int pourcentage;
    private String indice;
    private String supposition;

    public superinterface superi(){
        superinterface linterface = new superinterface();
        linterface.setDistance("");
        linterface.setPourcentage(0);
        linterface.setIndice("");
        linterface.setSupposition("");
        return linterface;

    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(int pourcentage) {
        this.pourcentage = pourcentage;
    }

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }

    public String getSupposition() {
        return supposition;
    }

    public void setSupposition(String supposition) {
        this.supposition = supposition;
    }
}
/*
Les régions jouables : chaque information n'est écrite qu'ici, une seule fois.
Le reste du code vient chercher ses données dans cet enum.
 */
enum Epoque { ANCIENNE, NOUVELLE, LES_DEUX }

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
    // nouvelles régions et régions inchangées : nom, latitude, longitude, époque, superficie,
    // spécialité culinaire, personnalité, capitale régionale, image
    HAUTS_DE_FRANCE("Hauts-de-France", 50.62925, 3.057256, Epoque.NOUVELLE, 31814, "la bière", "Charles de Gaulle", "Lille", "hauts-de-france.png"),
    NORMANDIE("Normandie", 49.443232, 1.099971, Epoque.NOUVELLE, 29907, "la matelote", "Guillaume le Conquérant", "Rouen", "normandie.png"),
    BRETAGNE("Bretagne", 48.117266, -1.6777926, Epoque.LES_DEUX, 27208, "la crepe", "Louis Guilloux", "Rennes", "bretagne.png"),
    PAYS_DE_LA_LOIRE("Pays de la Loire", 47.218371, -1.553621, Epoque.LES_DEUX, 32082, "la fouace", "Jules Verne", "Nantes", "pays-de-la-loire.png"),
    ILE_DE_FRANCE("Île-de-France", 48.856614, 2.3522219, Epoque.LES_DEUX, 12012, "le macaron", "Alexandre Dumas", "Paris", "ile-de-france.png"),
    CENTRE_VAL_DE_LOIRE("Centre-Val de Loire", 47.902964, 1.909251, Epoque.NOUVELLE, 39151, "la tarte tatin", "Jeanne d'Arc", "Orleans", "centre-val-de-loire.png"),
    GRAND_EST("Grand Est", 48.5734053, 7.7521113, Epoque.NOUVELLE, 57433, "la choucroute", "Paul Verlaine", "Strasbourg", "grand-est.png"),
    BOURGOGNE_FRANCHE_COMTE("Bourgogne-Franche-Comté", 47.322047, 5.04148, Epoque.NOUVELLE, 47784, "le boeuf bourguignon", "Victor Hugo", "Dijon", "bourgogne-franche-comte.png"),
    AUVERGNE_RHONE_ALPES("Auvergne-Rhône-Alpes", 45.764043, 4.835659, Epoque.NOUVELLE, 69711, "le coq au vin", "Jean Moulin", "Lyon", "auvergne-rhone-alpes.png"),
    NOUVELLE_AQUITAINE("Nouvelle-Aquitaine", 44.837789, -0.57918, Epoque.NOUVELLE, 84060, "les huitres du Bassin d'Arcachon", "Jacques Ellul", "Bordeaux", "nouvelle-aquitaine.png"),
    OCCITANIE("Occitanie", 43.604652, 1.444209, Epoque.NOUVELLE, 72724, "le confit de canard", "Jean Jaurès", "Toulouse", "occitanie.png"),
    PROVENCE_ALPES_COTE_D_AZUR("Provence-Alpes-Côte d'Azur", 43.296482, 5.36978, Epoque.LES_DEUX, 31400, "la bouillabaisse", "Fernandel", "Marseille", "provence-alpes-cote-dazur.png"),
    CORSE("Corse", 41.919229, 8.738635, Epoque.LES_DEUX, 8680, "le figatellu", "Napoleon Bonaparte", "Ajaccio", "corse.png"),
    MAYOTTE("Mayotte", -12.780600, 45.227800, Epoque.LES_DEUX, 374, "le matsidza", "Taambati Moussa", "Mamoudzou", "mayotte.png"),
    LA_REUNION("La Réunion", -20.882057, 55.450675, Epoque.LES_DEUX, 2504, "le rhum arrange", "Michel Houellebecq", "Saint-Denis", "la-reunion.png"),
    GUYANE("Guyane", 4.9227, -52.3269, Epoque.LES_DEUX, 83534, "le poulet boucane", "Henri Salvador", "Cayenne", "guyane.png"),
    MARTINIQUE("Martinique", 14.6160647, -61.0587804, Epoque.LES_DEUX, 1128, "le gratin de bananes", "Aimé Césaire", "Fort-de-France", "martinique.png"),
    GUADELOUPE("Guadeloupe", 17.302606, -62.717692, Epoque.LES_DEUX, 1703, "les accras de morue", "Teddy Riner", "Basse-Terre", "guadeloupe.png"),

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
    private final int superficie;
    private final String specialite;
    private final String personnalite;
    private final String capitale;
    private final String image;

    RegionFr(String nom, double latitude, double longitude, Epoque epoque, int superficie,
             String specialite, String personnalite, String capitale, String image) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.epoque = epoque;
        this.superficie = superficie;
        this.specialite = specialite;
        this.personnalite = personnalite;
        this.capitale = capitale;
        this.image = image;
    }

    // constructeur des anciennes régions, qui n'ont ni indices ni image
    RegionFr(String nom, double latitude, double longitude) {
        this(nom, latitude, longitude, Epoque.ANCIENNE, 0, "", "", "", null);
    }

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
        return Optional.ofNullable(PAR_CLE.get(Region.convertisseur(saisie))).filter(mode::reconnait);
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

    public String getCle() { return Region.convertisseur(nom); }
    public String getNom() { return nom; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Epoque getEpoque() { return epoque; }
    public int getSuperficie() { return superficie; }
    public String getSpecialite() { return specialite; }
    public String getPersonnalite() { return personnalite; }
    public String getCapitale() { return capitale; }
    // null pour les régions sans image
    public String getImage() { return image; }
}

class Region{

    private double latitude;
    private double longitude;
    private String indiceun;
    private String indicedeux;
    private String indicetrois;
    private String nomdelaregion;
    private String vrainom;
    private int superficie;

    public int getSuperficie() {
        return superficie;
    }

    public void setSuperficie(int superficie) {
        this.superficie = superficie;
    }

    public String getVrainom() {
        return vrainom;
    }

    public void setVrainom(String vrainom) {
        this.vrainom = vrainom;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getIndiceun() {
        return indiceun;
    }

    public void setIndiceun(String indiceun) {
        this.indiceun = indiceun;
    }

    public String getIndicedeux() {
        return indicedeux;
    }

    public void setIndicedeux(String indicedeux) {
        this.indicedeux = indicedeux;
    }

    public String getIndicetrois() {
        return indicetrois;
    }

    public void setIndicetrois(String indicetrois) {
        this.indicetrois = indicetrois;
    }

    public String getNomdelaregion() {
        return nomdelaregion;
    }

    public void setNomdelaregion(String nomdelaregion) {
        this.nomdelaregion = nomdelaregion;
    }


    // fabrique une Region de jeu à partir des données de l'enum
    public static Region depuis(RegionFr source){
        Region laregion = new Region();
        laregion.setNomdelaregion(source.getCle());
        laregion.setVrainom(source.getNom());
        laregion.setLatitude(source.getLatitude());
        laregion.setLongitude(source.getLongitude());
        laregion.setSuperficie(source.getSuperficie());
        laregion.setIndiceun(source.getSpecialite());
        laregion.setIndicedeux(source.getPersonnalite());
        laregion.setIndicetrois(source.getCapitale());
        return laregion;
    }

    public static String convertisseur(String region){

        //supprimer les espaces
        String lessai = region.replaceAll("\\s","");

        //supprimer les majuscules

        lessai = lessai.toLowerCase();

        //supprimer les ponctuations

        lessai = lessai.replaceAll("\\p{Punct}", "");

        //supprimer les accents

        lessai = Normalizer.normalize(lessai,Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(lessai).replaceAll("");
    }


    public superinterface verifnotreinterface(superinterface jeusympa, int compteur, double distance, int pourcentage, Region laregion, Region lasupposition){

        String superficie = "";

        if (compteur == 0){
            int nombre = laregion.getSuperficie();
            superficie = String.valueOf(nombre);
        }
        switch (compteur){
            case 0 :
                jeusympa.setIndice(superficie);
                break;
            case 1 :
                jeusympa.setIndice(laregion.getIndiceun());
                break;
            case 2 :
                jeusympa.setIndice(laregion.getIndicedeux());
                break;
            case 3 :
                jeusympa.setIndice(laregion.getIndicetrois());
                break;
        }
        String test = String.format("%.2f",distance);
        jeusympa.setDistance(test);
        jeusympa.setPourcentage(pourcentage);
        jeusympa.setSupposition(lasupposition.getVrainom());


        return jeusympa;
    }

    public superinterface reussite(superinterface jeusympa, Region laregion){
        jeusympa.setDistance("0");
        jeusympa.setPourcentage(100);
        jeusympa.setSupposition(laregion.getVrainom());
        jeusympa.setIndice("");

        return jeusympa;
    }

    public int jeu(Region laregion, int compteur, Parametres lesparametres, superinterface[] jeusympa){
        double distance = 0.0;
        String supposition;
        int abandon = 0;
        int pourcentage = 0;
        int code = -1;
        Message lemessage = new Message();
        Region lasupposition = new Region();
        if (lesparametres.getChoixlaliste() == 0) {
            supposition = lemessage.interfaceclassique(jeusympa);
        }
        else {
            supposition = JOptionPane.showInputDialog("Quelle est votre supposition ? (Echap pour abandonner)");
        }
        try {
            if (supposition.equals("")){}
        }
        catch (NullPointerException npe)
        {
            compteur = 5;
            supposition = "";
            abandon = -1;
        }
        if ((supposition.equals("1")) && (lesparametres.getChoixdelaregion() == 1) && (lesparametres.getChoixlaliste() == 0))
        {
            lemessage.touteslesregions(ModeRegions.depuisCode(lesparametres.getChoixdelaregion()));
        }
        else
        {
            supposition = Region.convertisseur(supposition);
        if (supposition.equals(laregion.getNomdelaregion())){
            code = 0;
            superinterface lejeu = new superinterface();
            lejeu = reussite(lejeu,laregion);
            jeusympa[compteur] = lejeu;
            compteur = 5;
        }
        else {
            ModeRegions mode = ModeRegions.depuisCode(lesparametres.getChoixdelaregion());
            Optional<RegionFr> trouvee = RegionFr.chercher(supposition, mode);
            if (abandon == -1){
                code = 1;
                distance = -1;
            }
            else if (trouvee.isEmpty()) {
                code = 2;
            } else {
                lasupposition = Region.depuis(trouvee.get());
                distance = verifdistance(laregion, lasupposition);
                code = 3;
                pourcentage = pourcentage(laregion, distance);
                //String fleche = this.fleche(laregion,lasupposition);
                superinterface lejeu = new superinterface();
                lejeu = verifnotreinterface(lejeu,compteur,distance,pourcentage,laregion,lasupposition);
                jeusympa[compteur] = lejeu;
            }
        }
        }
        if ((compteur !=5) || (code == 0)){
            lemessage.message(code, distance, pourcentage, compteur, laregion);
            if (code == 3){
                compteur = compteur + 1;
            }
        }
        if (compteur == 5) {
            lemessage.messagedefin(jeusympa,laregion,code);
        }
        return compteur;

    }

    public double verifdistance(Region laaregion, Region laasupposition){
        double distance;
        int r;
        r = 6371;
        // loi des cosinus sphérique : d = R * acos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2)*cos(lon1 - lon2))
        double lat1 = degresversradian(laaregion.getLatitude());
        double lat2 = degresversradian(laasupposition.getLatitude());
        double deltaLon = degresversradian(laaregion.getLongitude() - laasupposition.getLongitude());
        double cosAngle = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        // les arrondis des double peuvent donner 1.0000000000000002, et acos(>1) renvoie NaN
        cosAngle = Math.min(1.0, cosAngle);
        distance = Math.acos(cosAngle) * r;
        if ((laaregion.getLatitude() == 0) || (laasupposition.getLatitude() == 0)){
            distance = 0;
        }
        return distance;
    }

    public double degresversradian(double latoulon){
        double pi;
        pi = Math.PI;
        double degre;
        degre = latoulon *(pi/180);
        return degre;
    }

    // pourcentage de proximité : 100 % à 0 km, 0 % pour la région la plus éloignée de la cible
    public int pourcentage(Region region, double ladistance){
        double distanceMax = 0;
        for (RegionFr autre : RegionFr.values()) {
            distanceMax = Math.max(distanceMax, verifdistance(region, Region.depuis(autre)));
        }
        return (int) ((distanceMax - ladistance) * 100 / distanceMax);
    }

    public String fleche(Region laregion, Region lasupposition){
        /*
        Tentative de calcul de l'azimut

        Mettre les résultats non satisfaisants que l'on a depuis
         */
        String sens = "";
        double resultat;

        double x = Math.cos(laregion.getLatitude())*Math.sin(lasupposition.getLatitude()) - Math.sin(laregion.getLatitude()) * Math.cos(lasupposition.getLatitude()) * Math.cos((lasupposition.getLongitude()) - (laregion.getLongitude()));
        double y = Math.sin((lasupposition.getLongitude())-(laregion.getLongitude())) * Math.cos(lasupposition.getLatitude());

        resultat = Math.atan2(y,x);

        System.out.println(resultat);
        return sens;
    }
}

class representation{

    /*
    class permettant de charger les images
    le chemin est relatif : lancer le jeu depuis le dossier du projet
     */
    String chemin = "regions/";
    public static BufferedImage Image(String path) {
        BufferedImage img = null;
        try {
            img = javax.imageio.ImageIO.read(new File(path));
        } catch (IOException e) {
        }
        return img;
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

