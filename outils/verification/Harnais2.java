package fr.francle.ui;

import fr.francle.donnees.*;
import fr.francle.jeu.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Harnais2 {
    static double lum(Color c) {
        double[] v = {c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0};
        for (int i = 0; i < 3; i++) v[i] = v[i] <= 0.04045 ? v[i] / 12.92 : Math.pow((v[i] + 0.055) / 1.055, 2.4);
        return 0.2126 * v[0] + 0.7152 * v[1] + 0.0722 * v[2];
    }
    static double ratio(Color a, Color b) {
        double x = lum(a), y = lum(b);
        return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
    }
    static String hex(Color c) { return c == null ? "null" : String.format("#%06X", c.getRGB() & 0xFFFFFF); }
    static void ligne(String quoi, Color avant, Color fond, double seuil) {
        if (avant == null || fond == null) { System.out.println("  " + quoi + " : couleur absente"); return; }
        double r = ratio(avant, fond);
        System.out.printf("  %-58s %s sur %s : %5.2f:1 %s%n", quoi, hex(avant), hex(fond), r, r >= seuil ? "ok" : "< " + seuil + "  ← INSUFFISANT");
    }
    static Color ui(String cle) { return UIManager.getColor(cle); }

    // la couleur la plus éloignée du fond parmi les pixels d'une icône peinte sur ce fond
    static Color pixelIcone(Icon icone, Component c, Color fond) {
        BufferedImage img = new BufferedImage(icone.getIconWidth() + 4, icone.getIconHeight() + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(fond); g.fillRect(0, 0, img.getWidth(), img.getHeight());
        icone.paintIcon(c, g, 2, 2); g.dispose();
        Color meilleur = fond; double ecart = 1;
        for (int y = 0; y < img.getHeight(); y++) for (int x = 0; x < img.getWidth(); x++) {
            Color p = new Color(img.getRGB(x, y));
            double r = ratio(p, fond);
            if (r > ecart) { ecart = r; meilleur = p; }
        }
        return meilleur;
    }

    public static void main(String[] a) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try { couleurs(); } catch (Exception e) { e.printStackTrace(); }
        });
        System.exit(0);
    }

    static void couleurs() throws Exception {
        for (boolean sombre : new boolean[]{true, false}) {
            Theme.installer(sombre);
            System.out.println("\n=== thème " + (sombre ? "SOMBRE (FlatDarkLaf)" : "CLAIR (FlatLightLaf)") + " ===");
            Color fond = ui("Panel.background");
            Color menu = ui("PopupMenu.background");
            System.out.println(" fond de la fenêtre " + hex(fond) + ", fond des menus " + hex(menu));
            System.out.println(" Contrôles (seuil 3:1) :");
            // les cases et boutons radio réellement utilisés : ceux du menu Paramètres
            JCheckBoxMenuItem caseMenu = new JCheckBoxMenuItem("x", true);
            JRadioButtonMenuItem radio = new JRadioButtonMenuItem("x", true);
            JPopupMenu pm = new JPopupMenu(); pm.add(caseMenu); pm.add(radio);
            caseMenu.updateUI(); radio.updateUI();
            Icon iCase = UIManager.getIcon("CheckBoxMenuItem.checkIcon");
            Icon iRadio = UIManager.getIcon("RadioButtonMenuItem.checkIcon");
            ligne("coche d'une case du menu (couleur de l'UIManager)", ui("CheckBoxMenuItem.icon.checkmarkColor"), menu, 3);
            ligne("coche d'une case du menu (pixel peint)", pixelIcone(iCase, caseMenu, menu), menu, 3);
            ligne("point d'un bouton radio du menu (pixel peint)", pixelIcone(iRadio, radio, menu), menu, 3);
            ligne("coche, article survolé", ui("CheckBoxMenuItem.icon.checkmarkColor"), ui("MenuItem.selectionBackground"), 3);
            JCheckBoxMenuItem grise = new JCheckBoxMenuItem("x", true); grise.setEnabled(false); pm.add(grise);
            ligne("coche d'une case grisée (silhouette tournée)", pixelIcone(iCase, grise, menu), menu, 3);
            System.out.println("    (une case NON cochée d'un menu FlatLaf ne dessine rien : pas de carré à contraster)");
            ligne("bord du champ de saisie", ui("Component.borderColor"), ui("TextField.background"), 3);
            ligne("bord du champ de saisie, face au fond de la fenêtre", ui("Component.borderColor"), fond, 3);
            ligne("bord du champ quand il a le focus", ui("Component.focusedBorderColor"), fond, 3);
            ligne("anneau de focus (Component.focusColor)", ui("Component.focusColor"), fond, 3);
            ligne("bord d'un bouton", ui("Button.borderColor"), fond, 3);
            ligne("fond d'un bouton, face au fond de la fenêtre", ui("Button.background"), fond, 3);
            ligne("bouton par défaut (Valider), fond", ui("Button.default.background"), fond, 3);
            ligne("cadre du tableau / des indices", ui("Component.borderColor"), fond, 3);
            ligne("case d'une JCheckBox (bord) [pas utilisée dans le jeu]", ui("CheckBox.icon.borderColor"), fond, 3);
            System.out.println(" Textes (seuil 4,5:1) :");
            ligne("texte ordinaire", ui("Label.foreground"), fond, 4.5);
            ligne("indice à venir (étiquette grisée)", ui("Label.disabledForeground"), fond, 4.5);
            ligne("texte fantôme du champ (« Choisissez d'abord… »)", ui("TextField.placeholderForeground"), ui("TextField.disabledBackground") != null ? ui("TextField.disabledBackground") : ui("TextField.background"), 4.5);
            ligne("article de menu grisé", ui("MenuItem.disabledForeground"), menu, 4.5);
            ligne("bouton grisé (texte)", ui("Button.disabledText"), ui("Button.disabledBackground") != null ? ui("Button.disabledBackground") : fond, 4.5);
            System.out.println(" Couleurs écrites dans le code :");
            if (sombre) {
                CarteDepartements.Palette p = CarteDepartements.SOMBRE;
                ligne("carte du bonus : département, face au fond", p.fond(), fond, 3);
                ligne("carte du bonus : trait entre départements", p.trait(), p.fond(), 3);
                ligne("carte du bonus : département à nommer / autres", p.demande(), p.fond(), 3);
                ligne("carte du bonus : survol / autres", p.survol(), p.fond(), 3);
                ligne("carte du bonus : anneau de focus / département", ui("Component.focusColor"), p.fond(), 3);
                ligne("carte du bonus : trouvé (vert) / autres", new Color(0x5CB85C), p.fond(), 3);
                ligne("carte du bonus : raté (rouge) / autres", new Color(0xE05252), p.fond(), 3);
                ligne("carte de fin : région, face au fond", CarteFin.FOND_SOMBRE, fond, 3);
                ligne("carte de fin : trait", CarteFin.TRAIT_SOMBRE, CarteFin.FOND_SOMBRE, 3);
                ligne("statistiques : barre grise, face au fond", new Color(0x787C7E), fond, 3);
                ligne("statistiques : chiffre blanc sur barre grise", Color.WHITE, new Color(0x787C7E), 4.5);
                ligne("statistiques : chiffre blanc sur barre du jour (accent)", Color.WHITE, ui("Component.accentColor"), 4.5);
            } else {
                CarteDepartements.Palette p = CarteDepartements.CLAIRE;
                ligne("carte du bonus : département, face au fond", p.fond(), fond, 3);
                ligne("carte du bonus : trait", p.trait(), p.fond(), 3);
                ligne("carte du bonus : à nommer / autres", p.demande(), p.fond(), 3);
                ligne("carte de fin : région, face au fond", CarteFin.FOND, fond, 3);
                ligne("carte de fin : trait", CarteFin.TRAIT, CarteFin.FOND, 3);
                ligne("statistiques : barre grise, face au fond", new Color(0x787C7E), fond, 3);
                ligne("statistiques : chiffre blanc sur barre grise", Color.WHITE, new Color(0x787C7E), 4.5);
            }
        }
    }
}
