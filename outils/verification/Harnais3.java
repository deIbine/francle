package fr.francle.ui;

import fr.francle.donnees.*;
import fr.francle.jeu.*;
import java.awt.*;
import java.lang.reflect.*;
import javax.swing.*;

public class Harnais3 {
    public static void main(String[] a) throws Exception {
        Harnais.IMG = a[0];
        SwingUtilities.invokeAndWait(Harnais::fermeur);
        // 1. un message de deux lignes pendant la partie : la silhouette garde-t-elle ses 220 points ?
        Harnais.nouvelle(false, Langue.FR);
        Harnais.edt(() -> {
            JComponent cadre = (JComponent) Harnais.champ("cadre");
            JLabel image = (JLabel) Harnais.champ("image");
            System.out.println("1. cadre au début : " + cadre.getHeight() + " pt (icône " + image.getIcon().getIconHeight() + " pt)");
            JTextField s = (JTextField) Harnais.champ("saisie");
            s.setText("Languedoc-Roussillon");   // ancienne région, en partie du jour (nouvelles régions)
            Harnais.appel("jouer");
        });
        Harnais.edt(() -> {
            JFrame fr = Harnais.frame(); fr.validate();
            JComponent cadre = (JComponent) Harnais.champ("cadre");
            JLabel image = (JLabel) Harnais.champ("image");
            Message m = (Message) Harnais.champ("message");
            System.out.println("1. après « Languedoc-Roussillon » : message " + m.getHeight() + " pt « " + Harnais.message() + " » ; cadre " + cadre.getHeight()
                    + " pt, icône " + image.getIcon().getIconHeight() + " pt" + (cadre.getHeight() < image.getIcon().getIconHeight() ? "  → SILHOUETTE ROGNÉE de " + (image.getIcon().getIconHeight() - cadre.getHeight()) + " pt" : ""));
            Harnais.capture(fr.getRootPane(), "I-message-2-lignes");
        });
        // 2. un redimensionnement en une seule passe de disposition (comme quand le joueur tire le bord)
        Harnais.edt(() -> {
            JFrame fr = Harnais.frame();
            Partie p = new Partie(RegionFr.PROVENCE_ALPES_COTE_D_AZUR, ModeRegions.NOUVELLES);
            Harnais.appel("montrerPartie", p);
            p.abandonner();
            Harnais.appel("terminer");
            fr.validate();
        });
        Thread.sleep(300);
        Harnais.edt(() -> {
            JFrame fr = Harnais.frame();
            fr.setSize(380, fr.getHeight());
            fr.validate();          // une seule passe, puis rien ne la redemande
            Message m = (Message) Harnais.champ("message");
            int besoin = m.getPreferredSize().height;  // (calculée maintenant à la bonne largeur)
            System.out.println("2. après une seule passe à 380 pt : message " + m.getWidth() + "x" + m.getHeight() + ", il lui faut " + besoin + " pt"
                    + (m.getHeight() < besoin ? "  → DEUXIÈME LIGNE COUPÉE" : ""));
            Harnais.capture(fr.getRootPane(), "J-une-passe-380");
        });
        // 3. thème sombre : la carte du bonus avec le focus du clavier
        Harnais.nouvelle(true, Langue.FR);
        Harnais.edt(() -> {
            Partie p = (Partie) Harnais.champ("partie");
            JTextField s = (JTextField) Harnais.champ("saisie");
            s.setText(p.getCible().getNom()); Harnais.appel("jouer");
        });
        Thread.sleep(300);
        Harnais.edt(() -> {
            CarteDepartements carte = (CarteDepartements) Harnais.champ("carte");
            Bonus b = (Bonus) Harnais.champ("bonus");
            carte.requestFocusInWindow();
        });
        Thread.sleep(300);
        Harnais.edt(() -> {
            CarteDepartements carte = (CarteDepartements) Harnais.champ("carte");
            Bonus b = (Bonus) Harnais.champ("bonus");
            Field v = CarteDepartements.class.getDeclaredField("vise"); v.setAccessible(true);
            v.set(carte, b.proposes().get(0));
            carte.repaint();
            System.out.println("3. carte du bonus : focus=" + carte.isFocusOwner() + ", visé " + b.proposes().get(0));
            Harnais.capture(Harnais.frame().getRootPane(), "K-bonus-focus-sombre");
        });
        System.exit(0);
    }
}
