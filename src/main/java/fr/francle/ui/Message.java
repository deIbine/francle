package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;

import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.swing.JOptionPane;

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
                        "\nVous êtes à " + String.format(Locale.FRENCH, "%.2f", essai.distance()) + " km." +
                        "\nVotre pourcentage de proximité est de : " + essai.pourcentage() + "%." +
                        "\nLa région recherchée est dans cette direction : " + essai.direction().getSymbole() +
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

    // les 5 lignes "Supposition n : région distance km flèche pourcentage% indice", vides pour les essais pas encore joués
    public static String historique(Partie partie){
        List<Essai> essais = partie.getEssais();
        StringBuilder texte = new StringBuilder();
        for (int i = 0; i < Partie.ESSAIS_MAX; i++){
            String proposition = "";
            String distance = "";
            String fleche = "";
            int pourcentage = 0;
            String indice = "";
            if (i < essais.size()){
                Essai essai = essais.get(i);
                proposition = essai.proposition().getNom();
                boolean trouve = essai.proposition() == partie.getCible();
                distance = trouve ? "0" : String.format(Locale.FRENCH, "%.2f", essai.distance());
                pourcentage = essai.pourcentage();
                // la flèche et son espace, et rien du tout sur la ligne gagnante
                fleche = trouve ? "" : essai.direction().getSymbole() + " ";
                // un essai réussi ne révèle pas d'indice
                if (!trouve){
                    indice = Indice.apresEssai(i).map(lindice -> valeur(lindice, partie.getCible())).orElse("");
                }
            }
            texte.append("Supposition ").append(i + 1).append(" : ").append(proposition).append(" ")
                    .append(distance).append(" km ").append(fleche).append(pourcentage).append("%");
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
