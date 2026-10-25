package fr.francle.ui;

import fr.francle.donnees.RegionFr;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Icon;

/*
Les images du jeu : la silhouette de la région à deviner (la carte de France de la fin, elle, est dessinée par
CarteFin). Les images sont des ressources : Maven les copie depuis src/main/resources à côté des classes compilées
(et dans le .jar). On les cherche donc dans le classpath, quel que soit le dossier d'où le jeu est lancé.
 */
final class Representation {

    // toutes ces images sont produites par outils/generer_images.py, sur fond transparent (thème clair comme sombre)
    static final String SILHOUETTES = "regions/";
    static final String DEFAUT = SILHOUETTES + "defaut.png";

    private Representation() {}

    // l'image, ou null si la ressource n'existe pas ou ne se lit pas
    static BufferedImage charger(String chemin) {
        // "/" au début : chemin depuis la racine du classpath, et non depuis le package de la classe
        URL ressource = Representation.class.getResource("/" + chemin);
        if (ressource == null) {
            return null;
        }
        try {
            return ImageIO.read(ressource);
        } catch (IOException e) {
            return null;
        }
    }

    // l'image montrée pendant la partie : la silhouette de la région, ou la France et un point d'interrogation
    // quand le joueur a désactivé la silhouette
    static String silhouette(RegionFr cible, boolean avecImages) {
        return avecImages ? SILHOUETTES + cible.getImage() : DEFAUT;
    }

    // la taille d'une image agrandie ou réduite pour tenir dans le cadre, sans la déformer :
    // on applique le même rapport aux deux côtés, le plus petit des deux qui font tenir l'image
    static Dimension ajuster(int largeur, int hauteur, int largeurMax, int hauteurMax) {
        double rapport = Math.min((double) largeurMax / largeur, (double) hauteurMax / hauteur);
        return new Dimension((int) Math.round(largeur * rapport), (int) Math.round(hauteur * rapport));
    }

    // l'image prête à mettre dans un JLabel, ajustée au cadre ; null si elle est introuvable
    static Icon icone(String chemin, int largeurMax, int hauteurMax) {
        return icone(chemin, largeurMax, hauteurMax, 0);
    }

    // la même, tournée de « degres » dans le sens des aiguilles d'une montre (0 : telle quelle). Une ImageNette : elle
    // garde l'image d'origine, et ne la réduit qu'au dessin, à la taille en pixels de l'écran (nette à 150 %)
    static Icon icone(String chemin, int largeurMax, int hauteurMax, int degres) {
        BufferedImage image = charger(chemin);
        if (image == null) {
            return null;
        }
        if (degres % 360 != 0) {
            image = rogner(tourner(image, degres));
        }
        Dimension taille = ajuster(image.getWidth(), image.getHeight(), largeurMax, hauteurMax);
        return new ImageNette(image, taille.width, taille.height);
    }

    /*
    L'image tournée autour de son centre, sur une toile transparente assez grande pour qu'aucun coin ne soit coupé.
    Un rectangle L × H tourné d'un angle a occupe une boîte de |L cos a| + |H sin a| sur |L sin a| + |H cos a|.
    On ne déplace pas les pixels un par un : on règle la « transformation » du pinceau (Graphics2D), puis on dessine
    l'image normalement ; Java2D calcule où va chaque pixel.
     */
    static BufferedImage tourner(BufferedImage image, int degres) {
        double angle = Math.toRadians(degres);
        double cos = Math.abs(Math.cos(angle));
        double sin = Math.abs(Math.sin(angle));
        int largeur = (int) Math.ceil(image.getWidth() * cos + image.getHeight() * sin);
        int hauteur = (int) Math.ceil(image.getWidth() * sin + image.getHeight() * cos);

        BufferedImage tournee = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pinceau = tournee.createGraphics();
        // sans ce réglage, les bords tournés seraient en escalier
        pinceau.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        pinceau.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // les transformations s'appliquent de la dernière écrite à la première : l'image est d'abord centrée sur
        // l'origine, puis tournée, puis ramenée au centre de la nouvelle toile
        pinceau.translate(largeur / 2.0, hauteur / 2.0);
        pinceau.rotate(angle);
        pinceau.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
        pinceau.drawImage(image, 0, 0, null);
        pinceau.dispose();
        return tournee;
    }

    // l'image réduite au plus petit rectangle qui contient tous ses pixels visibles : après une rotation, les marges
    // transparentes grandissent, et la silhouette paraîtrait plus petite qu'à l'endroit une fois ajustée au cadre
    static BufferedImage rogner(BufferedImage image) {
        int gauche = image.getWidth(), haut = image.getHeight(), droite = -1, bas = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                // l'opacité (alpha) est l'octet de poids fort de la couleur ARGB
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    gauche = Math.min(gauche, x);
                    droite = Math.max(droite, x);
                    haut = Math.min(haut, y);
                    bas = Math.max(bas, y);
                }
            }
        }
        if (droite < 0) {
            return image;   // entièrement transparente : rien à rogner
        }
        return image.getSubimage(gauche, haut, droite - gauche + 1, bas - haut + 1);
    }
}
