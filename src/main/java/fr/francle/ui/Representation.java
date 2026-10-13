package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/*
Les images du jeu : la silhouette de la région à deviner, et la carte de France qui révèle la réponse.
Les images sont des ressources : Maven les copie depuis src/main/resources à côté des classes compilées
(et dans le .jar). On les cherche donc dans le classpath, quel que soit le dossier d'où le jeu est lancé.
 */
final class Representation {

    static final String SILHOUETTES = "regions/";
    static final String CARTES = "indices/";
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

    // l'image montrée pendant la partie. La silhouette seulement en mode "nouvelles régions" : les anciennes
    // n'en ont pas, et en mode "toutes", la silhouette d'une région de 2016 trahirait l'époque de la réponse
    static String silhouette(RegionFr cible, ModeRegions mode, boolean avecImages) {
        if (avecImages && mode == ModeRegions.NOUVELLES && cible.getImage() != null) {
            return SILHOUETTES + cible.getImage();
        }
        return DEFAUT;
    }

    // la carte de France où la région est coloriée, pour la fin de partie : seulement en métropole
    static Optional<String> carte(RegionFr cible) {
        if (cible.getImage() == null) {
            return Optional.empty();
        }
        String chemin = CARTES + cible.getImage();
        return Optional.of(chemin).filter(c -> Representation.class.getResource("/" + c) != null);
    }

    // la taille d'une image agrandie ou réduite pour tenir dans le cadre, sans la déformer :
    // on applique le même rapport aux deux côtés, le plus petit des deux qui font tenir l'image
    static Dimension ajuster(int largeur, int hauteur, int largeurMax, int hauteurMax) {
        double rapport = Math.min((double) largeurMax / largeur, (double) hauteurMax / hauteur);
        return new Dimension((int) Math.round(largeur * rapport), (int) Math.round(hauteur * rapport));
    }

    // l'image prête à mettre dans un JLabel, ajustée au cadre ; null si elle est introuvable
    static ImageIcon icone(String chemin, int largeurMax, int hauteurMax) {
        BufferedImage image = charger(chemin);
        if (image == null) {
            return null;
        }
        Dimension taille = ajuster(image.getWidth(), image.getHeight(), largeurMax, hauteurMax);
        return new ImageIcon(image.getScaledInstance(taille.width, taille.height, Image.SCALE_SMOOTH));
    }
}
