package fr.francle.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/*
Une image nette à toutes les tailles d'écran.

Sous Windows réglé à 150 %, un « point » de Swing fait 1,5 pixel. Une image réduite d'avance à la taille en points
(220 de haut) était donc agrandie au dessin (330 pixels) : des bords en escalier. Celle-ci garde l'image d'origine
(440 pixels) et ne la réduit qu'au moment de se dessiner, à la taille en PIXELS de l'écran : le pinceau (Graphics2D)
connaît l'agrandissement de l'écran, c'est l'échelle de sa transformation. Le résultat est gardé : tant que la taille
ne change pas, l'image n'est pas recalculée.

Pour Swing, c'est une icône comme une autre (l'interface Icon) : sa taille en points, et de quoi se dessiner.
 */
final class ImageNette implements Icon {

    private final BufferedImage origine;
    private final int largeur;
    private final int hauteur;
    // la dernière réduction calculée, et sa taille en pixels
    private BufferedImage reduite;

    // l'image d'origine, affichée à cette taille en points
    ImageNette(BufferedImage origine, int largeur, int hauteur) {
        this.origine = origine;
        this.largeur = largeur;
        this.hauteur = hauteur;
    }

    @Override public int getIconWidth() { return largeur; }
    @Override public int getIconHeight() { return hauteur; }

    @Override
    public void paintIcon(Component composant, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        AffineTransform echelle = g2.getTransform();
        // la taille en pixels de l'écran : 330 pour 220 points à 150 %
        int largeurPixels = Math.max(1, (int) Math.round(largeur * echelle.getScaleX()));
        int hauteurPixels = Math.max(1, (int) Math.round(hauteur * echelle.getScaleY()));
        BufferedImage image = reduite(largeurPixels, hauteurPixels);
        // l'image réduite tombe pixel pour pixel sur l'écran ; l'interpolation ne sert que si l'arrondi décale d'un pixel
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, x, y, largeur, hauteur, null);
        g2.dispose();
    }

    // l'image d'origine réduite à cette taille en pixels, calculée une fois par taille
    BufferedImage reduite(int largeurPixels, int hauteurPixels) {
        if (reduite == null || reduite.getWidth() != largeurPixels || reduite.getHeight() != hauteurPixels) {
            reduite = reduire(origine, largeurPixels, hauteurPixels);
        }
        return reduite;
    }

    // SCALE_SMOOTH fait la moyenne des pixels d'origine qui tombent dans chaque pixel réduit : le plus doux des modes
    // de Java pour réduire (une simple interpolation sauterait des pixels, et ferait des bords crénelés)
    static BufferedImage reduire(BufferedImage image, int largeur, int hauteur) {
        // getScaledInstance rend une image « à fabriquer » ; ImageIcon attend qu'elle soit entièrement prête (sinon le
        // dessin ci-dessous pourrait n'en recopier qu'une partie)
        Image douce = new ImageIcon(image.getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH)).getImage();
        BufferedImage resultat = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pinceau = resultat.createGraphics();
        pinceau.drawImage(douce, 0, 0, null);
        pinceau.dispose();
        return resultat;
    }
}
