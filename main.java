import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Objects;
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
                    Region Francaise = new Region();
                    int compteur=0;
                    // on repart de 5 essais vierges à chaque partie
                    for (int i = 0; i < 5; i++){
                        jeusympa[i] = temporaire.superi();
                    }
                    JOptionPane.showMessageDialog( null, "Quelle est cette région française ?", "", JOptionPane.PLAIN_MESSAGE );
                    representation larep = new representation();
                    Francaise.setNomdelaregion(Francaise.aleatoire(lesparametres.getChoixdelaregion()));
                    Francaise = Francaise.latitudeetlongitude(Francaise.getNomdelaregion(), lesparametres.getChoixdelaregion());
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

    public void touteslesregions(){
        JOptionPane.showMessageDialog( null, "Voici les régions possibles : \nAuvergne-Rhône-Alpes \nBretagne \nCentre-Val de Loire \nCorse \nGrand Est \nGuadeloupe \nGuyane \nHauts-de-France \nÎle-de-France \nLa Réunion \nMartinique \nMayotte \nNormandie \nNouvelle-Aquitaine \nOccitanie \nPays de la Loire \nProvence-Alpes-Côte d'Azur", "", JOptionPane.PLAIN_MESSAGE );}


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


    public Region latitudeetlongitude(String region, int verifchoix) {
        //remplacer par une initialisation donc Region()
        double latitude;
        double longitude;
        String indiceun;
        String indicedeux;
        String indicetrois;
        String vrainom;
        int superficie;

        region = this.convertisseur(region);

        switch (region)
        {
            case "hautsdefrance":
                latitude = 50.62925;
                longitude = 3.057256;
                indiceun = ("la bière");
                indicedeux = ("Charles de Gaulle");
                indicetrois = ("Lille");
                vrainom = ("Hauts-de-France");
                superficie = 31814;
                break;
            case "normandie" :
                latitude = 49.443232;
                longitude = 1.099971;
                indiceun = ("la matelote");
                indicedeux = ("Guillaume le Conquérant");
                indicetrois = ("Rouen");
                vrainom = ("Normandie");
                superficie = 29907;
                break;
            case "bretagne":
                latitude = 48.117266;
                longitude = -1.6777926;
                indiceun = ("la crepe");
                indicedeux = ("Louis Guilloux");
                indicetrois = ("Rennes");
                vrainom = ("Bretagne");
                superficie = 27208;
                break;
            case "paysdelaloire":
                latitude = 47.218371;
                longitude = -1.553621;
                indiceun = ("la fouace");
                indicedeux = ("Jules Verne");
                indicetrois = ("Nantes");
                vrainom = ("Pays de la Loire");
                superficie = 32082;
                break;
            case "iledefrance":
                latitude = 48.856614;
                longitude = 2.3522219;
                indiceun = ("le macaron");
                indicedeux = ("Alexandre Dumas");
                indicetrois = ("Paris");
                vrainom = ("Île-de-France");
                superficie = 12012;
                break;
            case "centrevaldeloire" :
                latitude = 47.902964;
                longitude = 1.909251;
                indiceun = ("la tarte tatin");
                indicedeux = ("Jeanne d'Arc");
                indicetrois = ("Orleans");
                vrainom = ("Centre-Val de Loire");
                superficie = 39151;
                break;
            case "grandest" :
                latitude = 48.5734053;
                longitude = 7.7521113;
                indiceun = ("la choucroute");
                indicedeux = ("Paul Verlaine");
                indicetrois = ("Strasbourg");
                vrainom = ("Grand Est");
                superficie = 57433;
                break;
            case "bourgognefranchecomte":
                latitude = 47.322047;
                longitude = 5.04148;
                indiceun = ("le boeuf bourguignon");
                indicedeux = ("Victor Hugo");
                indicetrois = ("Dijon");
                vrainom = ("Bourgogne-Franche-Comté");
                superficie = 47784;
                break;
            case "auvergnerhonealpes" :
                latitude = 45.764043;
                longitude = 4.835659;
                indiceun = ("le coq au vin");
                indicedeux  = ("Jean Moulin");
                indicetrois = ("Lyon");
                vrainom = ("Auvergne-Rhône-Alpes");
                superficie = 69711;
                break;
            case "nouvelleaquitaine" :
                latitude = 44.837789;
                longitude = -0.57918;
                indiceun = ("les huitres du Bassin d'Arcachon");
                indicedeux = ("Jacques Ellul");
                indicetrois = ("Bordeaux");
                vrainom = ("Nouvelle-Aquitaine");
                superficie = 84060;
                break;
            case "occitanie" :
                latitude = 43.604652;
                longitude = 1.444209;
                indiceun = ("le confit de canard");
                indicedeux = ("Jean Jaurès");
                indicetrois = ("Toulouse");
                vrainom = ("Occitanie");
                superficie = 72724;
                break;
            case "provencealpescotedazur":
                latitude = 43.296482;
                longitude = 5.36978;
                indiceun = ("la bouillabaisse");
                indicedeux = ("Fernandel");
                indicetrois = ("Marseille");
                vrainom = ("Provence-Alpes-Côte d'Azur");
                superficie = 31400;
                break;
            case "corse":
                latitude = 41.919229;
                longitude = 8.738635;
                indiceun = ("le figatellu");
                indicedeux =  ("Napoleon Bonaparte");
                indicetrois = ("Ajaccio");
                vrainom = ("Corse");
                superficie = 8680;
                break;
            case "mayotte":
                latitude = -12.780600;
                longitude = 45.227800;
                indiceun = ("le matsidza");
                indicedeux = ("Taambati Moussa");
                indicetrois = ("Mamoudzou");
                vrainom = ("Mayotte");
                superficie = 374;
                break;
            case "lareunion":
                latitude = -20.882057;
                longitude = 55.450675;
                indiceun = ("le rhum arrange");
                indicedeux = ("Michel Houellebecq");
                indicetrois = ("Saint-Denis");
                vrainom = ("La Réunion");
                superficie = 2504;
                break;
            case "guyane":
                latitude = 4.9227;
                longitude = -52.3269;
                indiceun = ("le poulet boucane");
                indicedeux = ("Henri Salvador");
                indicetrois =  ("Cayenne");
                vrainom = ("Guyane");
                superficie = 83534;
                break;
            case "martinique":
                latitude = 14.6160647;
                longitude = -61.0587804;
                indiceun = ("le gratin de bananes");
                indicedeux = ("Aimé Césaire");
                indicetrois = ("Fort-de-France");
                vrainom = ("Martinique");
                superficie = 1128;
                break;
            case "guadeloupe":
                latitude = 17.302606;
                longitude = -62.717692;
                indiceun = ("les accras de morue");
                indicedeux = ("Teddy Riner");
                indicetrois = ("Basse-Terre");
                vrainom = ("Guadeloupe");
                superficie = 1703;
                break;
            default:
                latitude = 0;
                longitude = 0;
                indiceun="";
                indicedeux ="";
                indicetrois ="";
                vrainom = "";
                superficie = 0;
                break;}
                int test = 0;
                if (longitude != 0){test = 1;}
                // les anciennes régions sont reconnues dans tous les modes, sauf "nouvelles régions uniquement" (1)
                if ((verifchoix != 1) && (test == 0)){
                    switch (region) {
                        case "nordpasdecalais":
                            latitude = 50.62925;
                            longitude = 3.057256;
                            vrainom = ("Nord-Pas-de-Calais");
                            break;
                        case "picardie":
                            latitude = 49.844067;
                            longitude = 2.295753;
                            vrainom = ("Picardie");
                            break;
                        case "hautenormandie":
                            latitude = 49.443232;
                            longitude = 1.099971;
                            vrainom = ("Haute-Normandie");
                            break;
                        case "bassenormandie":
                            latitude = 49.182863;
                            longitude = -0.370679;
                            vrainom = ("Basse-Normandie");
                            break;
                        case "poitoucharentes":
                            latitude = 46.580224;
                            longitude = 0.340375;
                            vrainom = ("Poitou-Charentes");
                            break;
                        case "centre":
                            latitude = 47.902964;
                            longitude = 1.909251;
                            vrainom = ("Centre");
                            break;
                        case "champagneardenne":
                            latitude = 48.956682;
                            longitude = 4.363073;
                            vrainom = ("Champagne-Ardenne");
                            break;
                        case "lorraine":
                            latitude = 49.1193089;
                            longitude = 6.1757156;
                            vrainom = ("Lorraine");
                            break;
                        case "alsace":
                            latitude = 48.5734053;
                            longitude = 7.7521113;
                            vrainom = ("Alsace");
                            break;
                        case "bourgogne":
                            latitude = 47.322047;
                            longitude = 5.04148;
                            vrainom = ("Bourgogne");
                            break;
                        case "franchecomte":
                            latitude = 47.237829;
                            longitude = 6.0240539;
                            vrainom = ("Franche-Comté");
                            break;
                        case "rhonealpes":
                            latitude = 45.764043;
                            longitude = 4.835659;
                            vrainom = ("Rhône-Alpes");
                            break;
                        case "auvergne":
                            latitude = 45.777222;
                            longitude = 3.087025;
                            vrainom = ("Auvergne");
                            break;
                        case "limousin":
                            latitude = 45.833619;
                            longitude = 1.261105;
                            vrainom = ("Limousin");
                            break;
                        case "aquitaine":
                            latitude = 44.837789;
                            longitude = -0.57918;
                            vrainom = ("Aquitaine");
                            break;
                        case "midipyrenees":
                            latitude = 43.604652;
                            longitude = 1.444209;
                            vrainom = ("Midi-Pyrénées");
                            break;
                        case "languedocroussillon":
                            latitude = 43.610769;
                            longitude = 3.876716;
                            vrainom = ("Languedoc-Roussillon");
                            break;
                        default :
                            latitude = 0;
                            longitude = 0;
                    }

        }
        Region laregion = new Region();
        laregion.setNomdelaregion(region);
        laregion.setIndiceun(indiceun);
        laregion.setIndicedeux(indicedeux);
        laregion.setIndicetrois(indicetrois);
        laregion.setLatitude(latitude);
        laregion.setLongitude(longitude);
        laregion.setVrainom(vrainom);
        laregion.setSuperficie(superficie);
        return laregion;
}

    public String aleatoire(int mode){
        int nombrederegion;
        Random notrealeatoire = new Random();
        if ((mode == 0) || (mode == 2))
        {
            nombrederegion = 26 + 1;
        }
        else {
            nombrederegion = 17 + 1;
        }
        int chiffre = notrealeatoire.nextInt(nombrederegion);
        return choixpossible(mode, chiffre);
    }


    public String choixpossible(int mode, int valeur){
        String aleatoire = "";

        if (mode == 2){
            int de;
            double lareponse = Math.floor(Math.random()*2);
            de = (int)lareponse;
            if (de == 0) {mode = 0;}
            else {mode = 1;}
        }

        switch(valeur) {
            case 0:
                aleatoire = "Nord-Pas-de-Calais";
                if (mode == 1) {aleatoire = "Hauts-de-France";}
                break;
            case 1:
                aleatoire = "Guyane";
                break;
            case 2:
                aleatoire = "Haute-Normandie";
                if (mode == 1) {aleatoire = "Normandie";}
                break;
            case 3:
                aleatoire = "Martinique";
                break;
            case 4:
                aleatoire = "Bretagne";
                break;
            case 5:
                aleatoire = "Aquitaine";
                if (mode == 1){aleatoire ="Nouvelle-Aquitaine";}
                break;
            case 6:
                aleatoire = "Pays de la Loire";
                break;
            case 7:
                aleatoire = "Ile-de-France";
                break;
            case 8:
                aleatoire = "Centre";
                if (mode == 1){aleatoire = "Centre-Val de Loire";}
                break;
            case 9:
                aleatoire = "Mayotte";
                break;
            case 10:
                aleatoire = "Provence-Alpes-Cote d'Azur";
                break;
            case 11:
                aleatoire = "Alsace";
                if (mode == 1){aleatoire = "Grand Est";}
                break;
            case 12:
                aleatoire = "Bourgogne";
                if (mode == 1){aleatoire = "Bourgogne-Franche-Comte";}
                break;
            case 13:
                aleatoire = "Guadeloupe";
                break;
            case 14:
                aleatoire = "Rhone-Alpes";
                if (mode == 1){aleatoire = "Auvergne-Rhone-Alpes";}
                break;
            case 15:
                aleatoire = "Corse";
                break;
            case 16:
                aleatoire = "La Reunion";
                break;
            case 17:
                aleatoire = "Midi-Pyrenees";
                if (mode == 1){aleatoire = "Occitanie";}
                break;
            case 18:
                aleatoire = "Champagne-Ardenne";
                break;
            case 19:
                aleatoire = "Languedoc-Roussillon";
                break;
            case 20:
                aleatoire = "Lorraine";
                break;
            case 21:
                aleatoire = "Auvergne";
                break;
            case 22:
                aleatoire = "Poitou-Charentes";
                break;
            case 23:
                aleatoire = "Limousin";
                break;
            case 24:
                aleatoire = "Picardie";
                break;
            case 25:
                aleatoire = "Basse-Normandie";
                break;
            case 26:
                aleatoire = "Franche-Comte";
                break;
        }
                return aleatoire;
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
            lemessage.touteslesregions();
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

    public int pourcentage(Region region, double ladistance){
        int cas = 0;
        double distance;
        double [] tableaudemoyenne = new double[100];
        Region test;
            while (cas <= 26) {
                test = lesregionsenint(cas);
                distance = verifdistance(region,test);
                tableaudemoyenne[cas] = distance;
                cas = cas + 1;
            }
        int pourcentage;
        tableaudemoyenne = tri_selection(tableaudemoyenne);
        pourcentage = calculpourcentage(tableaudemoyenne, ladistance);
        return pourcentage;
    }

    public int calculpourcentage(double[] tab, double distance){
        double calcul = tab[26] - distance;
        calcul = (calcul * 100) / tab[26];
        return (int)calcul;
    }


    public double[] tri_selection(double[] tab)
    {
        int test = 0;
        int cpt = 0;
        while (test==0){
            if (tab[cpt] > tab[cpt + 1]){
                double buffer = tab[cpt];
                tab[cpt] = tab[cpt + 1];
                tab[cpt + 1] = buffer;
                cpt = 0;
            }
            else {
                cpt = cpt + 1;
                if (cpt == 26)
                {
                    test = 1;
                }
            }
        }
        return tab;
    }


    public Region lesregionsenint(int cas){
        double latitude;
        double longitude;

        switch (cas) {
            case 0 :
                latitude = 50.62925;
                longitude = 3.057256;
                break;
            case 1:
                latitude = 49.844067;
                longitude = 2.295753;
                break;
            case 2 :
                latitude = 49.443232;
                longitude = 1.099971;
                break;
            case 3 :
                latitude = 49.182863;
                longitude = -0.370679;
                break;
            case 4 :
                latitude = 48.117266;
                longitude = -1.6777926;
                break;
            case 5 :
                latitude = 47.218371;
                longitude = -1.553621;
                break;
            case 6 :
                latitude = 46.580224;
                longitude = 0.340375;
                break;
            case 7:
                latitude = 48.856614;
                longitude = 2.3522219;
                break;
            case 8 :
                latitude = 47.902964;
                longitude = 1.909251;
                break;
            case 9 :
                latitude = 48.956682;
                longitude = 4.363073;
                break;
            case 10 :
                latitude = 49.1193089;
                longitude = 6.1757156;
                break;
            case 11:
                latitude = 48.5734053;
                longitude = 7.7521113;
                break;
            case 12 :
                latitude = 47.322047;
                longitude = 5.04148;
                break;
            case 13:
                latitude = 47.237829;
                longitude = 6.0240539;
                break;
            case 14 :
                latitude = 45.764043;
                longitude = 4.835659;
                break;
            case 15 :
                latitude = 45.777222;
                longitude = 3.087025;
                break;
            case 16 :
                latitude = 45.833619;
                longitude = 1.261105;
                break;
            case 17 :
                latitude = 44.837789;
                longitude = -0.57918;
                break;
            case 18 :
                latitude = 43.604652;
                longitude = 1.444209;
                break;
            case 19 :
                latitude = 43.610769;
                longitude = 3.876716;
                break;
            case 20 :
                latitude = 43.296482;
                longitude = 5.36978;
                break;
            case 21 :
                latitude = 41.919229;
                longitude = 8.738635;
                break;
            case 22 :
                latitude = -12.780600;
                longitude = 45.227800;
                break;
            case 23 :
                latitude = -20.882057;
                longitude = 55.450675;
                break;
            case 24 :
                latitude = 4.9227;
                longitude = -52.3269;
                break;
            case 25 :
                latitude = 14.6160647;
                longitude = -61.0587804;
                break;
            case 26 :
                latitude = 17.302606;
                longitude = -62.717692;
                break;
            default:
                latitude = 0;
                longitude = 0;
                break;
        }
        Region letest = new Region();
        letest.setLatitude(latitude);
        letest.setLongitude(longitude);
        return letest;
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

        if (Objects.equals(region, "corse")){
            this.region = representation.Image(chemin + "corse.png");
        }
        else if (Objects.equals(region, "bourgognefranchecomte")){
            this.region = representation.Image(chemin + "bourgogne-franche-comte.png");
        }
        else if (Objects.equals(region, "hautsdefrance")){
            this.region = representation.Image(chemin + "hauts-de-france.png");
        }
        else if (Objects.equals(region, "auvergnerhonealpes")){
            this.region = representation.Image(chemin + "auvergne-rhone-alpes.png");
        }
        else if (Objects.equals(region,"bretagne")){
            this.region = representation.Image(chemin + "bretagne.png");
        }
        else if (Objects.equals(region, "centrevaldeloire")){
            this.region = representation.Image(chemin + "centre-val-de-loire.png");
        }
        else if (Objects.equals(region, "grandest")){
            this.region = representation.Image(chemin + "grand-est.png");
        }
        else if (Objects.equals(region,"guadeloupe")){
            this.region = representation.Image(chemin + "guadeloupe.png");
        }
        else if (Objects.equals(region,"guyane")){
            this.region = representation.Image(chemin + "guyane.png");
        }
        else if (Objects.equals(region, "iledefrance")){
            this.region = representation.Image(chemin + "ile-de-france.png");
        }
        else if (Objects.equals(region,"lareunion")){
            this.region = representation.Image(chemin + "la-reunion.png");
        }
        else if (Objects.equals(region,"martinique")){
            this.region = representation.Image(chemin + "martinique.png");
        }
        else if (Objects.equals(region, "mayotte")){
            this.region = representation.Image(chemin + "mayotte.png");
        }
        else if (Objects.equals(region,"normandie")){
            this.region = representation.Image(chemin + "normandie.png");
        }
        else if (Objects.equals(region,"nouvelleaquitaine")){
            this.region = representation.Image(chemin + "nouvelle-aquitaine.png");
        }
        else if (Objects.equals(region,"occitanie")){
            this.region = representation.Image(chemin + "occitanie.png");
        }
        else if (Objects.equals(region, "paysdelaloire")){
            this.region = representation.Image(chemin + "pays-de-la-loire.png");
        }
        else if (Objects.equals(region, "provencealpescotedazur")){
            this.region = representation.Image(chemin + "provence-alpes-cote-dazur.png");
        }
        else
        {
            this.region = representation.Image(chemin + "defaut.png");
        }

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

