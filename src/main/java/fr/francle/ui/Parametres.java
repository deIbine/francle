package fr.francle.ui;

import fr.francle.donnees.ModeRegions;

import javax.swing.JOptionPane;

// le menu des paramètres en popups : il ne fait que modifier les Reglages (remplacé par la barre de menus en Phase 4.3)
class Parametres {

    static void modifier(Reglages reglages) {
        int choix = demander("Que voulez-vous modifier ? 0 = retour, 1 = anciennes ou nouvelles régions, 2 = images présentes ou non, 3 = liste des régions ou non");
        switch (choix) {
            case 1 :
                choisirRegions(reglages, demander("0 = anciennes régions, 1 = nouvelles régions, 2 = toutes les régions"));
                break;
            case 2 :
                choisirImages(reglages, demander("0 = garder les images, 1 = enlever les images"));
                break;
            case 3 :
                choisirListe(reglages, demander("0 = garder la liste des régions, 1 = enlever la liste des régions"));
                break;
            default :
                informer("Retour au menu");
                break;
        }
    }

    // le nombre tapé par le joueur, ou -1 si ce n'en est pas un
    // (Echap ou Annuler renvoie null, et Integer.parseInt(null) lève aussi NumberFormatException)
    private static int demander(String question) {
        try {
            return Integer.parseInt(JOptionPane.showInputDialog(question));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void choisirRegions(Reglages reglages, int choix) {
        if (choix < 0 || choix > 2) {
            informer("Pas de changements");
            return;
        }
        reglages.setMode(ModeRegions.depuisCode(choix));
        switch (reglages.getMode()) {
            case ANCIENNES : informer("Les anciennes régions sont maintenant jouables"); break;
            case NOUVELLES : informer("Les nouvelles régions sont maintenant jouables"); break;
            default : informer("Toutes les régions sont maintenant jouables"); break;
        }
    }

    private static void choisirImages(Reglages reglages, int choix) {
        if (choix != 0 && choix != 1) {
            informer("Pas de changements");
            return;
        }
        reglages.setAvecImages(choix == 0);
        informer(reglages.isAvecImages() ? "Vous jouez avec les images" : "Vous jouez sans les images");
    }

    private static void choisirListe(Reglages reglages, int choix) {
        if (choix != 0 && choix != 1) {
            informer("Pas de changements");
            return;
        }
        reglages.setAvecListe(choix == 0);
        informer(reglages.isAvecListe() ? "Vous jouez avec la liste" : "Vous jouez sans la liste");
    }

    private static void informer(String message) {
        JOptionPane.showMessageDialog(null, message, "", JOptionPane.PLAIN_MESSAGE);
    }
}
