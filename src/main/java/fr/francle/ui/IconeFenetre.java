package fr.francle.ui;

import fr.francle.donnees.ModeRegions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

/*
L'icône de la fenêtre (barre de titre, barre des tâches, Alt+Tab) : la France en blanc sur un carré bleu arrondi.
Sans elle, Windows montre la tasse de café de Java.

Elle est dessinée par le jeu, à partir des contours de la carte de fin (ContoursFrance), plutôt que lue dans un
fichier : rien à fabriquer à part, et elle existe en plusieurs tailles. Windows choisit la taille qui convient à
chaque endroit (16 pixels dans la barre de titre, 32 ou plus dans la barre des tâches, selon l'agrandissement de
l'écran) : une icône dessinée pour sa taille est plus nette qu'une grande icône réduite.
 */
final class IconeFenetre {

    static final Color FOND = new Color(0x3A4BC4);
    static final Color FRANCE = Color.WHITE;
    static final int[] TAILLES = {16, 20, 24, 32, 40, 48, 64, 128, 256};

    private IconeFenetre() {}

    // l'icône dans toutes ses tailles, pour JFrame.setIconImages
    static List<BufferedImage> images() {
        ContoursFrance contours = ContoursFrance.de();
        return Arrays.stream(TAILLES).mapToObj(taille -> dessiner(contours, taille)).toList();
    }

    static BufferedImage dessiner(ContoursFrance contours, int taille) {
        BufferedImage image = new BufferedImage(taille, taille, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(FOND);
        // des coins arrondis au cinquième de la taille, comme les icônes d'applications actuelles
        int arrondi = Math.max(3, taille / 5);
        g2.fillRoundRect(0, 0, taille, taille, arrondi, arrondi);
        // la France de métropole (les nouvelles régions la couvrent toute), avec une marge d'un huitième
        int marge = Math.max(1, taille / 8);
        AffineTransform transformation = CarteDepartements.ajuster(
                new Rectangle2D.Double(0, 0, contours.largeur(), contours.hauteur()),
                new Rectangle(marge, marge, taille - 2 * marge, taille - 2 * marge));
        g2.setColor(FRANCE);
        // toutes les régions de la même couleur : une seule forme à l'œil. Remplies seules, deux voisines laisseraient
        // entre elles une couture pâle (l'anticrénelage éclaircit le bord de chacune) : un trait de la même couleur,
        // fin, la recouvre
        g2.setStroke(new BasicStroke(Math.max(0.75f, taille / 96f)));
        for (Path2D forme : contours.regions(ModeRegions.NOUVELLES).values()) {
            Shape placee = transformation.createTransformedShape(forme);
            g2.fill(placee);
            g2.draw(placee);
        }
        g2.dispose();
        return image;
    }
}
