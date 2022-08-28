import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.Scanner;


public class main{


    public static void main (String[] args){

        Parametres lesparametres = new Parametres();
        lesparametres.setChoixdelaregion(1);
        int findujeu;
        findujeu = -1;
        while(findujeu !=0){
            Scanner scanner = new Scanner(System.in);
            System.out.println("0 = arret, 1 = jeu, 2 = parametres");
            System.out.println("Faites votre choix : ");
            findujeu = scanner.nextInt();
            switch(findujeu){
                case 0 :
                    System.out.println("Merci d'avoir joue !");
                    break;
                case 1 :
                    Region Francaise = new Region();
                    int compteur=0;
                    System.out.println("Quelle est cette region francaise ?");
                    String region;
                    region = Francaise.aleatoire(lesparametres.getChoixdelaregion());
                    //A mettre dans les regions avec l'aléatoire
                    representation larep = new representation();
                    JFrame frame = new JFrame();
                    frame.getContentPane().setLayout(new FlowLayout());
                    frame.getContentPane().add(new JLabel(new ImageIcon(larep.getRegion())));
                    frame.pack();
                    frame.setVisible(true);
                    //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // if you want the X button to close the ap
                    System.out.println((region));
                    while (compteur !=5){
                        compteur=Francaise.jeu(region, compteur,lesparametres.getChoixdelaregion());
                    }
                    break;
                case 2 :
                    System.out.println("Que voulez vous-modifier ?");
                    System.out.println("0 = retour, 1 = anciennes ou nouvelles regions");
                    int choixparam = -1;
                    choixparam = scanner.nextInt();
                    switch(choixparam){
                        case 0 :
                            System.out.println("Retour au menu");
                            break;
                        case 1 :
                            System.out.println("0 = anciennes regions, 1 = nouvelles regions, 2 = toutes les regions");
                            int choixregion = -1;
                            choixregion = scanner.nextInt();
                            choixregion = lesparametres.ancienneounouvelle(choixregion);
                    }
                default :
                    System.out.println("Essaie encore");
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
class Parametres{

    private int modedejeu = 0;
    private int choixdelaregion;

    public int getChoixdelaregion() {
        return choixdelaregion;
    }

    public void setChoixdelaregion(int choixdelaregion) {
        this.choixdelaregion = choixdelaregion;
    }

    public int ancienneounouvelle(int choixregion){
        if (choixregion == 0)
        {
            //tour de magie pour permettre le fonctionnement avec les anciennes régions, probablement avec des booleebs
            System.out.println("Tout est ok, les anciennes regions sont maintenant jouables");
            this.setChoixdelaregion(0);
        }
        else if (choixregion == 1){
            //même tour de magie
            System.out.println("Tout est ok, les nouvelles regions sont maintenant jouables");
            this.setChoixdelaregion(1);
            //donner la possibilite de jouer avec toutes les regions en meme temps ?
        }
        else if (choixregion == 2){
            System.out.println("Tout est ok, les anciennes et nouvelles regions sont maintenant jouables");
            this.setChoixdelaregion(2);
        }
        else {
            System.out.println("Rien n'a été modifié");
        }
        return choixregion;
    }
    /*
    private int unite (0 = km, 1 = miles)
    private int theme (0 = light, 1 = dark)
    private int langage (a voir en fontion de notre ambition)
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

class Region{
    private String villeprincipale;
    private int serie;
    private int seriemax;
    private int moyennedistance;

    public String aleatoire(int mode){
        String aleatoire;
        double chiffre;

        if (mode==0){
            chiffre = Math.floor(Math.random()*26);
            int valeur = (int)chiffre;
            aleatoire = choixpossible(mode, valeur);
        }
        else if (mode==1)
        {
            chiffre = Math.floor(Math.random()*18);
            int valeur = (int)chiffre;
            aleatoire = choixpossible(mode, valeur);
        }
        else
        {
            chiffre = Math.floor(Math.random()*26);
            int valeur = (int)chiffre;
            aleatoire = choixpossible(mode, valeur);
        }

        return aleatoire;
    }

    public String choixpossible(int mode, int valeur){
        String aleatoire;

        if (mode == 2){
            int de = 0;
            double lareponse = Math.floor(Math.random()*1);
            de = (int)lareponse;
            if (de == 0) {mode = 0;}
            else if (de == 1){mode = 1;}
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
                aleatoire = "Champagne-Ardenne";
                break;
            case 18:
                aleatoire = "Midi-Pyrenees";
                if (mode == 1){aleatoire = "Occitanie";}
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
            default:
                aleatoire = "Nord-Pas-de-Calais";
                if (mode == 1){aleatoire = "Hauts-de-France";}
                break;
        }
                return aleatoire;
    }

    /*

     */


    public int jeu(String region, int compteur, int modedejeu){

        Scanner scanner = new Scanner(System.in);
        System.out.println("Noter votre choix : ");
        String supposition = scanner.nextLine();
        double distance;
        if (supposition.equals(region)){
            System.out.println("Vous etes a 0 km");
            System.out.println("C'est trouve, bravo ! ");
            compteur = 5;
            return compteur;
        }
        else {
            System.out.println("Rate !");
            distance = verifdistance(region,supposition);
            if (distance==0){
                System.out.println("Recommence petit joueur");
            }
            else {
                System.out.println("Vous etes a " + distance + " km");
                int pourcentage = moyenne(region, distance, modedejeu);
                //int pourcentage = calculmoyenne(distance,mediane);
                System.out.println("Pourcentage de distance = " + pourcentage + "%");
                String fleche = this.veriffleche(region,supposition);
                System.out.println(fleche);
                compteur = compteur + 1;
            }
            if (compteur == 5 ){
                System.out.println("Vous n'avez plus de suppositions, la reponse etait " + region);
                return compteur;
            }
            else {
                return compteur;
            }

        }



    }



    public double verifdistance(String region, String supposition){
        //donc soit on récupère le fichier en .csv, soit on admet un tableau avec les infos dedans
        //le calcul dans tous les cas
        double distance;
        int r;
        r = 6371;

        double[] lasupposition;
        double[] laregion;
        lasupposition = veriflatlon(supposition);
        laregion = veriflatlon(region);
        distance = Math.acos(Math.sin(degresversradian(laregion[1]))*Math.sin(degresversradian(lasupposition[1]))+Math.cos(degresversradian(laregion[1]))*Math.cos(degresversradian(lasupposition[1]))*Math.cos(degresversradian((laregion[0]-lasupposition[0])))) * r;

        if ((laregion[0] == 0) || (lasupposition[0] == 0)){
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


    public double[] veriflatlon(String region) {
        double latitude;
        double longitude;
        switch (region) {
            case "Nord-Pas-de-Calais":
                latitude = 50.62925;
                longitude = 3.057256;
                break;
            case "Hauts-de-France":
                latitude = 50.62925;
                longitude = 3.057256;
                break;
            case "Picardie":
                latitude = 49.844067;
                longitude = 2.295753;
                break;
            case "Haute-Normandie":
                latitude = 49.443232;
                longitude = 1.099971;
                break;
            case "Normandie" :
                latitude = 49.443232;
                longitude = 1.099971;
                break;
            case "Basse-Normandie":
                latitude = 49.182863;
                longitude = -0.370679;
                break;
            case "Bretagne":
                latitude = 48.117266;
                longitude = -1.6777926;
                break;
            case "Pays de la Loire":
                latitude = 47.218371;
                longitude = -1.553621;
                break;
            case "Poitou-Charentes":
                latitude = 46.580224;
                longitude = 0.340375;
                break;
            case "Ile-de-France":
                latitude = 48.856614;
                longitude = 2.3522219;
                break;
            case "Centre":
                latitude = 47.902964;
                longitude = 1.909251;
                break;
            case "Centre-Val de Loire" :
                latitude = 47.902964;
                longitude = 1.909251;
                break;
            case "Champagne-Ardenne":
                latitude = 48.956682;
                longitude = 4.363073;
                break;
            case "Lorraine":
                latitude = 49.1193089;
                longitude = 6.1757156;
                break;
            case "Alsace":
                latitude = 48.5734053;
                longitude = 7.7521113;
                break;
            case "Grand Est" :
                latitude = 48.5734053;
                longitude = 7.7521113;
                break;
            case "Bourgogne":
                latitude = 47.322047;
                longitude = 5.04148;
                break;
            case "Bourgogne-Franche-Comte":
                latitude = 47.322047;
                longitude = 5.04148;
                break;
            case "Franche-Comte":
                latitude = 47.237829;
                longitude = 6.0240539;
                break;
            case "Rhone-Alpes":
                latitude = 45.764043;
                longitude = 4.835659;
                break;
            case "Auvergne-Rhone-Alpes" :
                latitude = 45.764043;
                longitude = 4.835659;
                break;
            case "Auvergne":
                latitude = 45.777222;
                longitude = 3.087025;
                break;
            case "Limousin":
                latitude = 45.833619;
                longitude = 1.261105;
                break;
            case "Aquitaine":
                latitude = 44.837789;
                longitude = -0.57918;
                break;
            case "Nouvelle-Aquitaine" :
                latitude = 44.837789;
                longitude = -0.57918;
                break;
            case "Midi-Pyrenees":
                latitude = 43.604652;
                longitude = 1.444209;
                break;
            case "Occitanie" :
                latitude = 43.604652;
                longitude = 1.444209;
                break;
            case "Languedoc-Roussillon":
                latitude = 43.610769;
                longitude = 3.876716;
                break;
            case "Provence-Alpes-Cote d'Azur":
                latitude = 43.296482;
                longitude = 5.36978;
                break;
            case "Corse":
                latitude = 41.919229;
                longitude = 8.738635;
                break;
            case "Mayotte":
                latitude = -12.780600;
                longitude = 45.227800;
                break;
            case "La Reunion":
                latitude = -20.882057;
                longitude = 55.450675;
                break;
            case "Guyane":
                latitude = 4.9227;
                longitude = -52.3269;
                break;
            case "Martinique":
                latitude = 14.6160647;
                longitude = -61.0587804;
                break;
            case "Guadeloupe":
                latitude = 17.302606;
                longitude = -62.717692;
                break;
            default:
                latitude = 0;
                longitude = 0;
                break;

        }
        double[] latlon = {latitude,longitude};
        return latlon;
    }

    public int moyenne(String region, double ladistance, int modedejeu){
        double latitude;
        double longitude;
        int cas = 0;
        double moyenne = 0;
        int stop = 0;
        int r = 6371;
        double [] laregion;
        laregion = veriflatlon(region);
        double distance;
        double [] tableaudemoyenne = new double[100];
        //double [] petittableaudemoyenne = new double[18];
        if (modedejeu == 2)
        {
            int de = 0;
            double lareponse = Math.floor(Math.random()*1);
            de = (int)lareponse;
            if (de == 0) {modedejeu = 0;}
            else if (de == 1){modedejeu = 1;}
        }
        if (modedejeu == 0) {
            while (cas <= 26) {
                double[] letestmoyenne;
                letestmoyenne = lesregionsenint(cas, modedejeu);
                distance = Math.acos(Math.sin(degresversradian(laregion[1])) * Math.sin(degresversradian(letestmoyenne[1])) + Math.cos(degresversradian(laregion[1])) * Math.cos(degresversradian(letestmoyenne[1])) * Math.cos(degresversradian((laregion[0] - letestmoyenne[0])))) * r;
                tableaudemoyenne[cas] = distance;
                //System.out.println(tableaudemoyenne[cas]);
                cas = cas + 1;
            }
        }
        if (modedejeu == 1){
            while (cas <= 17) {
                double[] letestmoyenne;
                letestmoyenne = lesregionsenint(cas, modedejeu);
                distance = Math.acos(Math.sin(degresversradian(laregion[1])) * Math.sin(degresversradian(letestmoyenne[1])) + Math.cos(degresversradian(laregion[1])) * Math.cos(degresversradian(letestmoyenne[1])) * Math.cos(degresversradian((laregion[0] - letestmoyenne[0])))) * r;
                tableaudemoyenne[cas] = distance;
                //System.out.println(petittableaudemoyenne[cas]);
                cas = cas + 1;
            }
        }

        int pourcentage = 0;
        /*
        if (modedejeu == 0)
        {
        tableaudemoyenne = tri_selection(tableaudemoyenne);
        double mediane = calculmediane(tableaudemoyenne);
        pourcentage = calculpourcentage(tableaudemoyenne, ladistance);
        }

        if (modedejeu == 1)
        {
            petittableaudemoyenne = tri_selection(petittableaudemoyenne);
            double mediane = calculmediane(petittableaudemoyenne);
            pourcentage = calculpourcentage(petittableaudemoyenne, ladistance);
        }

         */
        tableaudemoyenne = tri_selection(tableaudemoyenne);
        double mediane = calculmediane(tableaudemoyenne);
        pourcentage = calculpourcentage(tableaudemoyenne, ladistance);
        return pourcentage;
    }

    public int calculpourcentage(double[] tab, double distance){
        double calcul = tab[26] - distance;
        calcul = (calcul * 100) / tab[26];
        System.out.println(calcul + " " + distance + " " + tab[26]);
        int pourcentagefinal = (int)calcul;
        return pourcentagefinal;
    }
    public int calculmoyenne(double distance, double mediane){
        double calcul = mediane - distance;
        calcul = (calcul * 100) / mediane;
        System.out.println(calcul + " " + distance + " " + mediane);
        if (calcul < 0)
        {
            calcul = calcul * (-1);
        }
        int pourcentagefinal = (int)calcul;
        return pourcentagefinal;
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

    public double calculmediane(double[] tab){
        int mediane = tab.length / 2;
        int medianedeux = (tab.length/2) + 1;
        double medianne = (tab[mediane] + tab[medianedeux]) / 2;
        return medianne;

    }

    public double[] lesregionsenint(int cas, int modedejeu){
        double latitude = 0;
        double longitude = 0;

        if (modedejeu == 2){
            int de = 0;
            double lareponse = Math.floor(Math.random()*1);
            de = (int)lareponse;
            if (de == 0) {modedejeu = 0;}
            else if (de == 1){modedejeu = 1;}
        }

        if (modedejeu == 0)
        {


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
        }
        else if (modedejeu == 1)
        {
            switch (cas) {
                case 0:
                    latitude = 50.62925;
                    longitude = 3.057256;
                    break;
                case 1:
                    latitude = 49.443232;
                    longitude = 1.099971;
                    break;
                case 2:
                    latitude = 48.117266;
                    longitude = -1.6777926;
                    break;
                case 3:
                    latitude = 47.218371;
                    longitude = -1.553621;
                    break;
                case 4:
                    latitude = 48.856614;
                    longitude = 2.3522219;
                    break;
                case 5:
                    latitude = 47.902964;
                    longitude = 1.909251;
                    break;
                case 6:
                    latitude = 48.5734053;
                    longitude = 7.7521113;
                    break;
                case 7:
                    latitude = 47.322047;
                    longitude = 5.04148;
                    break;

                case 8:
                    latitude = 45.764043;
                    longitude = 4.835659;
                    break;

                case 9:
                    latitude = 44.837789;
                    longitude = -0.57918;
                    break;
                case 10:
                    latitude = 43.604652;
                    longitude = 1.444209;
                    break;
                case 11:
                    latitude = 43.296482;
                    longitude = 5.36978;
                    break;
                case 12:
                    latitude = 41.919229;
                    longitude = 8.738635;
                    break;
                case 13:
                    latitude = -12.780600;
                    longitude = 45.227800;
                    break;
                case 14:
                    latitude = -20.882057;
                    longitude = 55.450675;
                    break;
                case 15:
                    latitude = 4.9227;
                    longitude = -52.3269;
                    break;
                case 16:
                    latitude = 14.6160647;
                    longitude = -61.0587804;
                    break;
                case 17:
                    latitude = 17.302606;
                    longitude = -62.717692;
                    break;
                default:
                    latitude = 0;
                    longitude = 0;
                    break;
            }
        }
        double[] latlon = {latitude,longitude};
        return latlon;
    }
    public int verifpourcentage(){
        //l'idée est d'envoyer un pourcentage en fonction de la distance du joueur

        Region Lille = new Region();
        double distance;
        //distance = Lille.verifdistance();
        int pourcentage;
        pourcentage = 2;
        return pourcentage;

        /*
        Pour le moment, je n'ai pas d'idées de comment faire le calcul pour avoir le pourcentage autre
        qu'avec des potentiels switch et case partout, ce qui n'est pas optimisé
        il y a forcément la possibilité de faire un calcul de pourcentage avec un facteur de distance
        mais je ne le connais pas encore
         */


    }

    public String veriffleche(String region, String supposition) {
        //soustraire la latitude et la longitude de chacun pourrait nous donner un chiffre ?
        double besoin;
        double[] lasupposition;
        double[] laregion;
        lasupposition = veriflatlon(supposition);
        laregion = veriflatlon(region);
        double soustraction1;
        double soustraction2;
        String fleche;

        double latregion = lasupposition[0]- laregion[0];
        double longregion = lasupposition[1]-laregion[1];

        //if (latregion

        double carotte;
        double x;
        double y;
        y = Math.sin(degresversradian(lasupposition[1])-degresversradian(laregion[1]))*Math.cos(degresversradian(lasupposition[0]));
        x = Math.cos(degresversradian(laregion[0]))*Math.sin(degresversradian(lasupposition[0]))-Math.sin(degresversradian(laregion[0]))-Math.cos(degresversradian(lasupposition[0]))*Math.cos(degresversradian(lasupposition[1])-degresversradian(laregion[1]));
        carotte = Math.atan2(y,x);
        carotte = (carotte * 180/Math.PI+360)%360;

        //9 éléments possibles, pour le moment on ne va mettre que 5 fleches
        /*
        if ((soustraction1 > 0) && (soustraction2 > 0)){
            fleche = ("Fleche vers la droite");
        }
        else if ((soustraction1 > 0) && (soustraction2 < 0)){
            fleche = ("Fleche vers le haut");
        }
        else if ((soustraction1 < 0) && (soustraction2 > 0)){
            fleche = ("Fleche vers la gauche");
        }
        else if ((soustraction1 < 0) && (soustraction2 < 0)){
            fleche = ("Fleche vers le bas");
        }
        else {
            fleche = ("Felicitations !");
        }
        if (soustraction1 == soustraction2){
            fleche = ("Felicitations !");
        }

         */
        /*
        if ((carotte > 179.5) && (carotte < 182)){
            fleche = ("Le degre est de " + carotte + " la fleche est probablement au Nord");
        }
        else if ((carotte > 175) && (carotte < 179.5)){
            fleche = ("Le degre est de " + carotte + " la fleche est probablement a l'Est");
        }
        else if ((carotte > 182) && (carotte < 190)){
            fleche = ("Le degre est de " + carotte + " la fleche est probablement a l'Ouest");
        }
        else {
            fleche = ("Le degre est de " + carotte + " la fleche est probablement au Sud");
        }
        //mettre des tues chauffes à la place ? Cela revient pas aux pourcentages ? Cela permettrait de simplifier les pourcentages...

         */

        System.out.println("Latitude de direction = " + latregion + " et la longitude de direction " + longregion);
        fleche = "carotte";

        return fleche;
    }
}

class representation{


    public static BufferedImage Image(String path) {
        BufferedImage img = null;
        try {
            img = javax.imageio.ImageIO.read(new File(path));
        } catch (IOException e) {
        }
        return img;
    }
    private Image region = representation.Image("C:/Users/cleme/Downloads/Corse.png");

    public Image getRegion() {
        return region;
    }


}
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



class commentjouer{

    si l'utilisateur utilise cette fonction, on lui donne une image qui expkique ce qu'il doit faire, s'inspirer du screen
}

class reussirafairelesgraphismesmagnifique{

}

class internet ?{

        }*/
/*
Obligation de faire une classe convertisseur pour essayer de prendre les suppositions des utilisateurs ratées ou vérifier les accents et les tirets ?
 */

/*
    double latitude;
    double longitude;
    public double[] veriflatlon(String region) {
        switch (region) {


        }
        double[] latlon = {latitude,longitude};
        return latlon;
    }*/
