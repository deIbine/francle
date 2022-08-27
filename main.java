import java.lang.Math;
import java.util.Scanner;
import java.util.Arrays;

public class main{


    public static void main (String[] args){

        Parametres lesparametres = new Parametres();
        lesparametres.setChoixdelaregion(0);
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

        /*
        if ((mode == 1) || (mode == 2))
        {
            if ((de == 0) && (valeur < 18))
            {
                switch(valeur){
                    case 0 :
                        aleatoire = "Hauts-de-France";
                        break;
                    case 1 :
                        aleatoire = "Normandie";
                        break;
                    case 2 :
                        aleatoire = "Bretagne";
                        break;
                    case 3 :
                        aleatoire = "Nouvelle-Aquitaine";
                        break;
                    case 4 :
                        aleatoire = "Occitanie";
                        break;
                    case 5 :
                        aleatoire = "Pays de la Loire";
                        break;
                    case 6 :
                        aleatoire = "Ile-de-France";
                        break;
                    case 7 :
                        aleatoire = "Centre-Val de Loire";
                        break;
                    case 8 :
                        aleatoire = "Bourgogne-Franche-Comté";
                        break;
                    case 9 :
                        aleatoire = "Auvergne-Rhone-Alpes";
                        break;
                    case 10 :
                        aleatoire = "Grand Est";
                        break;
                    case 11 :
                        aleatoire = "Provence-Alpes-Cote d'Azur";
                        break;
                    case 12 :
                        aleatoire = "Corse";
                        break;
                    case 13 :
                        aleatoire = "Mayotte";
                        break;
                    case 14 :
                        aleatoire = "La Reunion";
                        break;
                    case 15 :
                        aleatoire = "Guyane";
                        break;
                    case 16 :
                        aleatoire = "Martinique";
                        break;
                    case 17 :
                        aleatoire = "Guadeloupe";
                        break;
                    default :
                        aleatoire = "Nord-Pas-de-Calais";
                        break;
                }
            }
        }
        return aleatoire;

         */
    }


    public int jeu(String region, int compteur, int modedejeu){

        Scanner scanner = new Scanner(System.in);
        System.out.println("Noter votre choix : ");
        String supposition = scanner.nextLine();
        double distance;
        if (supposition.equals(region)){
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

        //distance = Math.acos(Math.sin(degresversradian(longitude1))*Math.sin(degresversradian(longitude2))+Math.cos(degresversradian(longitude1))*Math.cos(degresversradian(longitude2))*Math.cos(degresversradian((latitude1-latitude2)))) * r;
        /*
        =ACOS(SIN(RADIANS(B2))*SIN(RADIANS(B3))+COS(RADIANS(B2))*COS(RADIANS(B3))*COS(RADIANS(C2-C3)))*6371.
        normalement un simple calcul avec la latitude et la longitude
        d'ailleurs, c'est fort possible qu'on doit mettre ses fonctions dans la class Region
        */
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
    double latitude;
    double longitude;
    public double[] veriflatlon(String region) {
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

        return fleche;
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

 */

/*
        double longitude1;
        double latitude1;
        double longitude2;
        double latitude2;

         */

//Test entre Lille et Amiens, distance réelle de 131 km
        /*
        latitude1 = 48.117266;
        longitude1 = 1.6777926;
        latitude2 = 49.894067;
        longitude2 = 2.295753;
        */
        /*
        double[] coordoregion = veriflatlon(region);
        latitude1 = coordoregion[0];
        longitude1 = coordoregion[1];
        double[] coordosupposition = veriflatlon(region);
        latitude2 = coordosupposition[0];
        longitude2 = coordosupposition[1];
        //System.out.println(latitude1); y'a bien un problème

         */

        /*
        //https://www.gps-longitude-latitude.net/longitude-latitude-coordonnees-gps-du-lieu
        switch (region){
            case "Nord-Pas-de-Calais":
                latitude1 = 50.62925;
                longitude1 = 3.057256;
                break;
            case "Picardie":
                latitude1 = 49.844067;
                longitude1 = 2.295753;
                break;
            case "Haute-Normandie":
                latitude1 = 49.443232;
                longitude1 = 1.099971;
                break;
            case "Basse-Normandie":
                latitude1 = 49.182863;
                longitude1 = -0.370679;
                break;
            case "Bretagne":
                latitude1 = 48.117266;
                longitude1 = -1.6777926;
                break;
            case "Pays de la Loire":
                latitude1 = 47.218371;
                longitude1 = -1.553621;
                break;
            case "Poitou-Charentes":
                latitude1 = 46.580224;
                longitude1 = 0.340375;
                break;
            case "Ile-de-France":
                latitude1 = 48.856614;
                longitude1 = 2.3522219;
                break;
            case "Centre":
                latitude1 = 47.902964;
                longitude1 = 1.909251;
                break;
            case "Champagne-Ardenne":
                latitude1 = 48.956682;
                longitude1 = 4.363073;
                break;
            case "Lorraine":
                latitude1 = 49.1193089;
                longitude1 = 6.1757156;
                break;
            case "Alsace":
                latitude1 = 48.5734053;
                longitude1 = 7.7521113;
                break;
            case "Bourgogne":
                latitude1 = 47.322047;
                longitude1 = 5.04148;
                break;
            case "Franche-Comte":
                latitude1 = 47.237829;
                longitude1 = 6.0240539;
                break;
            case "Rhone-Alpes":
                latitude1 = 45.764043;
                longitude1 = 4.835659;
                break;
            case "Auvergne":
                latitude1 = 45.777222;
                longitude1 = 3.087025;
                break;
            case "Limousin":
                latitude1 = 45.833619;
                longitude1 = 1.261105;
                break;
            case "Aquitaine":
                latitude1 = 44.837789;
                longitude1 = -0.57918;
                break;
            case "Midi-Pyrenees":
                latitude1 = 43.604652;
                longitude1 = 1.444209;
                break;
            case "Languedoc-Roussillon":
                latitude1 = 43.610769;
                longitude1 = 3.876716;
                break;
            case "Provence-Alpes-Cote d'Azur":
                latitude1 = 43.296482;
                longitude1 = 5.36978;
                break;
            case "Corse":
                latitude1 = 41.919229;
                longitude1 = 8.738635;
                break;
            case "Mayotte":
                latitude1 = -12.780600;
                longitude1 = 45.227800;
                break;
            case "La Reunion":
                latitude1 = -20.882057;
                longitude1 = 55.450675;
                break;
            case "Guyane":
                latitude1 = 4.9227;
                longitude1 = -52.3269;
                break;
            case "Martinique":
                latitude1 = 14.6160647;
                longitude1 = -61.0587804;
                break;
            case "Guadeloupe":
                latitude1 = 17.302606;
                longitude1 = -62.717692;
                break;
            default:
                latitude1 = 0;
                longitude1 = 0;
                break;

        }

        switch (supposition){
            case "Nord-Pas-de-Calais":
                latitude2 = 50.62925;
                longitude2 = 3.057256;
                break;
            case "Picardie":
                latitude2 = 49.844067;
                longitude2 = 2.295753;
                break;
            case "Haute-Normandie":
                latitude2 = 49.443232;
                longitude2 = 1.099971;
                break;
            case "Basse-Normandie":
                latitude2 = 49.182863;
                longitude2 = -0.370679;
                break;
            case "Bretagne":
                latitude2 = 48.117266;
                longitude2 = -1.6777926;
                break;
            case "Pays de la Loire":
                latitude2 = 47.218371;
                longitude2 = -1.553621;
                break;
            case "Poitou-Charentes":
                latitude2 = 46.580224;
                longitude2 = 0.340375;
                break;
            case "Ile-de-France":
                latitude2 = 48.856614;
                longitude2 = 2.3522219;
                break;
            case "Centre":
                latitude2 = 47.902964;
                longitude2 = 1.909251;
                break;
            case "Champagne-Ardenne":
                latitude2 = 48.956682;
                longitude2 = 4.363073;
                break;
            case "Lorraine":
                latitude2 = 49.1193089;
                longitude2 = 6.1757156;
                break;
            case "Alsace":
                latitude2 = 48.5734053;
                longitude2 = 7.7521113;
                break;
            case "Bourgogne":
                latitude2 = 47.322047;
                longitude2 = 5.04148;
                break;
            case "Franche-Comte":
                latitude2 = 47.237829;
                longitude2 = 6.0240539;
                break;
            case "Rhone-Alpes":
                latitude2 = 45.764043;
                longitude2 = 4.835659;
                break;
            case "Auvergne":
                latitude2 = 45.777222;
                longitude2 = 3.087025;
                break;
            case "Limousin":
                latitude2 = 45.833619;
                longitude2 = 1.261105;
                break;
            case "Aquitaine":
                latitude2 = 44.837789;
                longitude2 = -0.57918;
                break;
            case "Midi-Pyrenees":
                latitude2 = 43.604652;
                longitude2 = 1.444209;
                break;
            case "Languedoc-Roussillon":
                latitude2 = 43.610769;
                longitude2 = 3.876716;
                break;
            case "Provence-Alpes-Cote d'Azur":
                latitude2 = 43.296482;
                longitude2 = 5.36978;
                break;
            case "Corse":
                latitude2 = 41.919229;
                longitude2 = 8.738635;
                break;
            case "Mayotte":
                latitude2 = -12.780600;
                longitude2 = 45.227800;
                break;
            case "La Reunion":
                latitude2 = -20.882057;
                longitude2 = 55.450675;
                break;
            case "Guyane":
                latitude2 = 4.9227;
                longitude2 = -52.3269;
                break;
            case "Martinique":
                latitude2 = 14.6160647;
                longitude2 = -61.0587804;
                break;
            case "Guadeloupe":
                latitude2 = 17.302606;
                longitude2 = -62.717692;
                break;
            default:
                latitude2 = 0;
                longitude2 = 0;
                System.out.println("Ce n'est pas une supposition acceptable");
                break;
        }

         */




        /*
        B2 = longitude1;
        C2 = latitude1;
        B3 = longitude2;
        C3 = latitude2;
        */


 double latitude;
    double longitude;
    public double[] veriflatlon(String region) {
        switch (region) {
            case "Hauts-de-France":
                latitude = 50.62925;
                longitude = 3.057256;
                break;

            case "Normandie":
                latitude = 49.443232;
                longitude = 1.099971;
                break;
            case "Bretagne":
                latitude = 48.117266;
                longitude = -1.6777926;
                break;
            case "Pays de la Loire":
                latitude = 47.218371;
                longitude = -1.553621;
                break;
            case "Ile-de-France":
                latitude = 48.856614;
                longitude = 2.3522219;
                break;
            case "Centre-val-de-Loire":
                latitude = 47.902964;
                longitude = 1.909251;
                break;
            case "Grand Est":
                latitude = 48.5734053;
                longitude = 7.7521113;
                break;
            case "Bourgogne Franche-Comté":
                latitude = 47.322047;
                longitude = 5.04148;
                break;

            case "Auvergne Rhone-Alpes":
                latitude = 45.764043;
                longitude = 4.835659;
                break;

            case "Nouvelle-Aquitaine":
                latitude = 44.837789;
                longitude = -0.57918;
                break;
            case "Occitanie":
                latitude = 43.604652;
                longitude = 1.444209;
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
