package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Francle {


    public static void main (String[] args){
        Reglages reglages = new Reglages();
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
                    ModeRegions mode = reglages.getMode();
                    // une nouvelle Partie à chaque fois : rien ne peut traîner de la partie précédente
                    Partie partie = new Partie(RegionFr.tirer(mode), mode);
                    JOptionPane.showMessageDialog( null, "Quelle est cette région française ?", "", JOptionPane.PLAIN_MESSAGE );
                    Representation larep = new Representation();
                    // l'image de la région seulement en mode "nouvelles régions" : les anciennes n'en ont pas, et
                    // en mode "toutes", l'image d'une région de 2016 trahirait l'époque de la réponse
                    if (reglages.isAvecImages() && mode == ModeRegions.NOUVELLES) {
                        larep.setRegion(larep.onmetlimage(partie.getCible().getCle()));
                    }
                    else {
                        larep.setRegion(larep.onmetlimage("autre"));
                    }
                        frame = larep.interfacesimple();
                        Message lemessage = new Message();
                        while (partie.enCours()) {
                            lemessage.tour(partie, reglages);
                        }
                        lemessage.messagedefin(partie);
                        frame.setVisible(false);
                    break;
                case 2 :
                    Parametres.modifier(reglages);
                    break;
                case 3 :
                    Representation didacticiele = new Representation();
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

}
