import javax.swing.*;
import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
//import java.awt.image.ImageObserver;
//import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
//import java.util.Locale;
import java.util.Random;
import java.util.Objects;
//import java.util.Scanner;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.text.DecimalFormat;


/*
A réparer :

Lignes 879 et tableaux en général, vérifier comment cela fonctionne exactement
car si tableau = 100, erreur, si tableau = 99, chiffre incompréhensible en pourcentage
si tableau = 26 ou 27, ça semble correct, pourquoi ?

 */

/*
Avancées en cours :

"Réparer" les différents choix de paramètres

Choix des régions : OK, mais vérifier le cas 2, où le dé n'est pas codé à chaque fois, plus rajouter les images

Projet : apprendre Java EE pour mettre le code sur Internet

Frankle avec les régions des pays de l'empire de Charlemagne ?

Nouvel ajout :

Faire moins de message, voire implémenter complétement une nouvelle classe utilisée uniquement pour les messages
car clairement, pour le moment c'est relativement illisible

 */




public class main{


    public static void main (String[] args){
        Parametres lesparametres = new Parametres();
        lesparametres.setChoixdelaregion(1);
        lesparametres.setChoiximage(0);
        lesparametres.setChoixlaliste(0);
        commentjouer didacticiel = new commentjouer();
        superinterface[] jeusympa = new superinterface[100];
        superinterface temporaire = new superinterface();
        int essai = 0;
        int findujeu = - 1;
        while(findujeu !=0){
            String lejeu = JOptionPane.showInputDialog("0 = arrêt, 1 = jeu, 2 = paramètres, 3 = didacticiel. Faites votre choix : ");
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
                    while (essai !=5){
                        jeusympa[essai] = temporaire.superi();
                        essai = essai + 1;
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
                        try{
                            boucle = Integer.parseInt(fin);
                        }
                        catch(NumberFormatException nfe){
                            fin = "1";
                        }
                        boucle = Integer.parseInt(fin);
                    }
                    frame.setVisible(false);
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
            case 0 :
                JOptionPane.showMessageDialog( null, "Vous êtes à 0 km, 100%. C'est trouvé, bravo !", "Victoire", JOptionPane.PLAIN_MESSAGE );
                break;
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
        }
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


    public String convertisseur(String region){

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

        if (verifchoix == 2){
            int de;
            double lareponse = Math.floor(Math.random()*2);
            de = (int)lareponse;
            if (de == 0) {verifchoix = 0;}
            else {verifchoix = 1;}
        }
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
                indicedeux = ("Jeanne Bohec");
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
                indicedeux = ("Sébastien Le Prestre de Vauban");
                indicetrois = ("Toulouse");
                vrainom = ("Occitanie");
                superficie = 72724;
                break;
            case "provencealpescotedazur":
                latitude = 43.296482;
                longitude = 5.36978;
                indiceun = ("la bouillabaisse");
                indicedeux = ("Pierre Barthélemy");
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
                indicedeux = ("Zéna M'Déré");
                indicetrois = ("Mamoudzou");
                vrainom = ("Mayotte");
                superficie = 374;
                break;
            case "lareunion":
                latitude = -20.882057;
                longitude = 55.450675;
                indiceun = ("le rhum arrange");
                indicedeux = ("Sarda Garriga");
                indicetrois = ("Saint-Denis");
                vrainom = ("La Réunion");
                superficie = 2504;
                break;
            case "guyane":
                latitude = 4.9227;
                longitude = -52.3269;
                indiceun = ("le poulet boucane");
                indicedeux = ("Félix Eboué");
                indicetrois =  ("Cayenne");
                vrainom = ("Guyane");
                superficie = 83534;
                break;
            case "martinique":
                latitude = 14.6160647;
                longitude = -61.0587804;
                indiceun = ("le gratin de bananes");
                indicedeux = ("Aime Cesaire");
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
                if ((verifchoix == 0) && (test == 0)){
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
                        case "chmpagneardenne":
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

    public int jeu(Region laregion, int compteur, Parametres lesparametres, superinterface[] jeusympa){
        double distance = 0.0;
        String supposition;
        int abandon = 0;
        int pourcentage = 0;
        int code = -1;
        Message lemessage = new Message();
        if (lesparametres.getChoixlaliste() == 0) {
            supposition = this.interfaceclassique(jeusympa);
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
            touteslesregions();
        }
        else
        {
            supposition = this.convertisseur(supposition);
        if (supposition.equals(laregion.getNomdelaregion())){
            code = 0;
            lemessage.message(0, 0.0, 0, compteur, laregion);
            compteur = 5;
        }
        else {
            Region lasupposition = new Region();
            lasupposition = lasupposition.latitudeetlongitude(supposition, lesparametres.getChoixdelaregion());
            distance = verifdistance(laregion, lasupposition);
            if (abandon == -1){
                code = 1;
                lemessage.message(1,0.0, 0, compteur, laregion);
                distance = -1;
            }
            if (distance == 0) {
                code = 2;
                lemessage.message(2,0.0, 0, compteur, laregion);
            } else if (distance != -1){
                code = 3;
                pourcentage = pourcentage(laregion, distance);
                //String fleche = this.fleche(laregion,lasupposition);
                superinterface lejeu = new superinterface();
                lejeu = verifnotreinterface(lejeu,compteur,distance,pourcentage,laregion,lasupposition);
                jeusympa[compteur] = lejeu;
            }
        }
        }
        if (compteur !=5){
            lemessage.message(code, distance, pourcentage, compteur, laregion);
            if (code == 3){
                compteur = compteur + 1;
            }
        }
        if (compteur == 5) {
            JOptionPane.showMessageDialog(null,"C'est raté ! \n \n" +
                    "Voici vos essais : \n \n" +
                    "Supposition 1 : " + jeusympa[0].getSupposition() + " " + jeusympa[0].getDistance() + " km " + jeusympa[0].getPourcentage() + "%" + " Superficie : " + jeusympa[0].getIndice() + " km \n" +
                    "Supposition 2 : " + jeusympa[1].getSupposition() + " " + jeusympa[1].getDistance() + " km " + jeusympa[1].getPourcentage() + "% Premier indice : " + jeusympa[1].getIndice() + "\n"+
                    "Supposition 3 : " + jeusympa[2].getSupposition() + " " + jeusympa[2].getDistance() + " km " + jeusympa[2].getPourcentage() + "% Deuxième indice : " + jeusympa[2].getIndice() + "\n"+
                    "Supposition 4 : " + jeusympa[3].getSupposition() + " " + jeusympa[3].getDistance() + " km " + jeusympa[3].getPourcentage() + "% Troisième indice : " + jeusympa[3].getIndice() + "\n" +
                    "Supposition 5 : " + jeusympa[4].getSupposition() + " " + jeusympa[4].getDistance() + " km " + jeusympa[4].getPourcentage() + "% \n \n" +
                    "Jeu terminé, la réponse était " + laregion.getVrainom(),"Fin du jeu",JOptionPane.PLAIN_MESSAGE);
        }
        return compteur;

    }

    public void touteslesregions(){
        JOptionPane.showMessageDialog( null, "Voici les régions possibles : \n Auvergne-Rhone-Alpes \n Bretagne \n Centre-Val de Loire \n Corse \n Grand Est \n Guadeloupe \n Guyane \n Hauts-de-France \n Ile-de-France \n La Reunion \n Martinique \n Mayotte \n Normandie \n Nouvelle-Aquitaine \n Occitanie \n Pays de la Loire \n Provence-Alpes-Cote d'Azur", "", JOptionPane.PLAIN_MESSAGE );}


    public double verifdistance(Region laaregion, Region laasupposition){
        double distance;
        int r;
        r = 6371;
        distance = Math.acos(Math.sin(degresversradian(laaregion.getLongitude()))*Math.sin(degresversradian(laasupposition.getLongitude()))+Math.cos(degresversradian(laaregion.getLongitude()))*Math.cos(degresversradian(laasupposition.getLongitude()))*Math.cos(degresversradian((laaregion.getLatitude()-laasupposition.getLatitude())))) * r;
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
    la ligne en-dessous est à modifier en fonction du chemin de l'utilisateur
     */
    String chemin = "C:/Users/cleme/Desktop/Francle/PNG des régions/";
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
            this.region = representation.Image(chemin + "Corse.png");
        }
        else if (Objects.equals(region, "bourgognefranchecomte")){
            this.region = representation.Image(chemin + "Bourgogne Franche-Comté.png");
        }
        else if (Objects.equals(region, "hautsdefrance")){
            this.region = representation.Image(chemin + "Hauts de France.png");
        }
        else if (Objects.equals(region, "auvergnerhonealpes")){
            this.region = representation.Image(chemin + "Auvergne Rhone Alpes.png");
        }
        else if (Objects.equals(region,"bretagne")){
            this.region = representation.Image(chemin + "Bretagne.png");
        }
        else if (Objects.equals(region, "centrevaldeloire")){
            this.region = representation.Image(chemin + "Centre-Val De Loire.png");
        }
        else if (Objects.equals(region, "grandest")){
            this.region = representation.Image(chemin + "Grand Est.png");
        }
        else if (Objects.equals(region,"guadeloupe")){
            this.region = representation.Image(chemin + "Guadeloupe.png");
        }
        else if (Objects.equals(region,"guyane")){
            this.region = representation.Image(chemin + "Guyane.png");
        }
        else if (Objects.equals(region, "iledefrance")){
            this.region = representation.Image(chemin + "Ile-de-France.png");
        }
        else if (Objects.equals(region,"lareunion")){
            this.region = representation.Image(chemin + "La Réunion.png");
        }
        else if (Objects.equals(region,"martinique")){
            this.region = representation.Image(chemin + "Martinique.png");
        }
        else if (Objects.equals(region, "mayotte")){
            this.region = representation.Image(chemin + "Mayotte.png");
        }
        else if (Objects.equals(region,"normandie")){
            this.region = representation.Image(chemin + "Normandie.png");
        }
        else if (Objects.equals(region,"nouvelleaquitaine")){
            this.region = representation.Image(chemin + "Nouvelle Aquitaine.png");
        }
        else if (Objects.equals(region,"occitanie")){
            this.region = representation.Image(chemin + "Occitanie.png");
        }
        else if (Objects.equals(region, "paysdelaloire")){
            this.region = representation.Image(chemin + "Pays de la Loire.png");
        }
        else if (Objects.equals(region, "provencealpescotedazur")){
            this.region = representation.Image(chemin + "Provence Alpes Cote d'Azur.png");
        }
        else
        {
            this.region = representation.Image(chemin + "défaut.png");
        }

        return this.region;
    }
    public Image getRegion() {
        return region;
    }


    public Image jpgjeu(){
        this.region = representation.Image(chemin + "défaut.png");
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

