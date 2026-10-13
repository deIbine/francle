import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

class representation{

    /*
    class permettant de charger les images
    Les images sont des ressources : Maven les copie depuis src/main/resources à côté des classes compilées
    (et dans le .jar). On les cherche donc dans le classpath, quel que soit le dossier d'où le jeu est lancé.
     */
    String chemin = "regions/";
    // l'image, ou null si la ressource n'existe pas ou ne se lit pas
    public static BufferedImage Image(String path) {
        // "/" au début : chemin depuis la racine du classpath, et non depuis le package de la classe
        URL ressource = representation.class.getResource("/" + path);
        if (ressource == null) {
            return null;
        }
        try {
            return javax.imageio.ImageIO.read(ressource);
        } catch (IOException e) {
            return null;
        }
    }
    private Image region = representation.Image("toto");

    public void setRegion(Image region) {
        this.region = region;
    }

    public JFrame interfacesimple(){
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(this.getRegion())));
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    public Image onmetlimage(String region){
        // l'image de la région si elle en a une ; sinon (clé inconnue, ancienne région) l'image par défaut
        String fichier = RegionFr.chercher(region, ModeRegions.TOUTES)
                .map(RegionFr::getImage)
                .orElse("defaut.png");
        this.region = representation.Image(chemin + fichier);
        return this.region;
    }
    public Image getRegion() {
        return region;
    }


    public Image jpgjeu(){
        this.region = representation.Image(chemin + "defaut.png");
        //ajouter commentjouer.png
        return this.region;
    }

}
