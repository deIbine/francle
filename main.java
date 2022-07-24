import java.lang.Math;
import java.util.Scanner;

public class main{


    public static void main (String[] args){

        int findujeu;
        findujeu = -1;
        while(findujeu !=0){
            Scanner scanner = new Scanner(System.in);
            System.out.println("0 = arret, 1 = jeu");
            System.out.println("Faites votre choix : ");
            findujeu = scanner.nextInt();
            switch(findujeu){
                case 0 :
                    System.out.println("Merci d'avoir joue !");
                    break;
                case 1 :
                    Region Francaise = new Region();
                    int compteur=0;
                    System.out.println("Quel est cette region francaise ?");
                    String region;
                    region = Francaise.aleatoire();
                    System.out.println((region));
                    while (compteur !=5){
                        compteur=Francaise.jeu(region, compteur);
                    }
                    break;
                default :
                    System.out.println("Essaie encore");
        }

        //double distance;
        //distance = Lille.verifdistance();
        //System.out.println(distance);

        /*
        test de code classique
        imaginons que la région à trouver soit le NPDC :)
        Comment fonctionne un code classique le plus simple possible
        L'utilisateur fait un guess d'une éventuelle représentation
        on lui calcule sa distance
        si la distance est = à 0, on arrête et on dit bravo
        si ce n'est pas le cas, on continue jusqu'à 6 essais
        essayons de coder ça
         */
        //creer de l'aleatoire pour le nom de la region a trouver

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

class Region{
    private String villeprincipale;
    private int serie;
    private int seriemax;
    private int moyennedistance;

    public String aleatoire(){
        String aleatoire;
        double chiffre;

        chiffre = Math.floor(Math.random()*26);
        int valeur = (int)chiffre;

        switch(valeur){
            case 0 :
                aleatoire = "Nord-Pas-de-Calais";
                break;
            case 1 :
                aleatoire = "Picardie";
                break;
            case 2 :
                aleatoire = "Haute-Normandie";
                break;
            case 3 :
                aleatoire = "Basse-Normandie";
                break;
            case 4 :
                aleatoire = "Bretagne";
                break;
            case 5 :
                aleatoire = "Pays de la Loire";
                break;
            case 6 :
                aleatoire = "Poitou-Charentes";
                break;
            case 7 :
                aleatoire = "Ile-de-France";
                break;
            case 8 :
                aleatoire = "Centre";
                break;
            case 9 :
                aleatoire = "Champagne-Ardenne";
                break;
            case 10 :
                aleatoire = "Lorraine";
                break;
            case 11 :
                aleatoire = "Alsace";
                break;
            case 12 :
                aleatoire = "Bourgogne";
                break;
            case 13 :
                aleatoire = "Franche-Comte";
                break;
            case 14 :
                aleatoire = "Rhone-Alpes";
                break;
            case 15 :
                aleatoire = "Auvergne";
                break;
            case 16 :
                aleatoire = "Limousin";
                break;
            case 17 :
                aleatoire = "Aquitaine";
                break;
            case 18 :
                aleatoire = "Midi-Pyrenees";
                break;
            case 19 :
                aleatoire = "Languedoc-Roussillon";
                break;
            case 20 :
                aleatoire = "Provence-Alpes-Cote d'Azur";
                break;
            case 21 :
                aleatoire = "Corse";
                break;
            case 22 :
                aleatoire = "Mayotte";
                break;
            case 23 :
                aleatoire = "La Reunion";
                break;
            case 24 :
                aleatoire = "Guyane";
                break;
            case 25 :
                aleatoire = "Martinique";
                break;
            case 26 :
                aleatoire = "Guadeloupe";
                break;
            default :
                aleatoire = "Nord-Pas-de-Calais";
                break;
        }

        return aleatoire;
    }


    public int jeu(String region, int compteur){

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

        double longitude1;
        double latitude1;
        double longitude2;
        double latitude2;

        //Test entre Lille et Amiens, distance réelle de 131 km
        /*
        latitude1 = 48.117266;
        longitude1 = 1.6777926;
        latitude2 = 49.894067;
        longitude2 = 2.295753;
        */

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


        /*
        B2 = longitude1;
        C2 = latitude1;
        B3 = longitude2;
        C3 = latitude2;
        */


        distance = Math.acos(Math.sin(degresversradian(longitude1))*Math.sin(degresversradian(longitude2))+Math.cos(degresversradian(longitude1))*Math.cos(degresversradian(longitude2))*Math.cos(degresversradian((latitude1-latitude2)))) * r;
        /*
        =ACOS(SIN(RADIANS(B2))*SIN(RADIANS(B3))+COS(RADIANS(B2))*COS(RADIANS(B3))*COS(RADIANS(C2-C3)))*6371.
        normalement un simple calcul avec la latitude et la longitude
        d'ailleurs, c'est fort possible qu'on doit mettre ses fonctions dans la class Region
        */
        if ((latitude1 == 0) || (latitude2 == 0)){
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

class Parametres{
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
