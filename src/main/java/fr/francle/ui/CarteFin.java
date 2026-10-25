package fr.francle.ui;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.swing.JComponent;

/*
La carte de France de la fin de partie, dessinée à la main comme la carte des départements : chaque région est une
forme (ContoursFrance) remplie de sa couleur.
    vert             la réponse, quand la partie est gagnée
    bleu foncé       la réponse, quand elle est perdue ou abandonnée
    jaune → orange   les régions proposées : plus la couleur est foncée, plus elles étaient proches de la réponse
    fond             les autres : clair en thème clair, gris bleuté en thème sombre
À gauche, la métropole du mode de la partie ; à droite, les cinq régions d'outre-mer, chacune dans son encart, sur
deux colonnes. Seuls le fond et les bords suivent le thème : les autres couleurs sont les mêmes dans les deux.

Une fois la partie finie, rien n'est plus à deviner : le survol d'une région donne son nom (bulle d'aide), pour
apprendre celles qu'on connaît mal.
 */
final class CarteFin extends JComponent {

    // les couleurs des cartes d'origine du jeu (outils/generer_images.py), et celles de la maquette validée
    static final Color FOND = new Color(0xFBF9F6);
    static final Color TRAIT = new Color(0x8C98B8);
    // en thème sombre, les régions non proposées : un gris bleuté un peu plus clair que la fenêtre, et non le blanc
    // cassé, qui éblouissait (phase 16). Les couleurs des réponses et des propositions, elles, ne changent pas
    static final Color FOND_SOMBRE = new Color(0x5B6270);
    static final Color TRAIT_SOMBRE = new Color(0x8D97AB);
    static final Color TROUVEE = new Color(0x2EA043);
    static final Color MANQUEE = new Color(0x3A4BC4);
    // le dégradé des régions proposées : trois couleurs, de la plus éloignée (0 %) à la plus proche (100 %)
    static final Color LOIN = new Color(0xFFE08C);
    static final Color MILIEU = new Color(0xFAA450);
    static final Color PROCHE = new Color(0xE26232);
    private static final int MARGE = 6;
    // la part de la largeur que prennent les encarts d'outre-mer, sur deux colonnes de trois lignes
    private static final double PART_ENCARTS = 0.36;
    private static final int COLONNES = 2;
    private static final int LIGNES = 3;

    private final ContoursFrance contours = ContoursFrance.de();
    // le nom d'une région dans la langue du joueur, pour la bulle d'aide : demandé à chaque survol, la langue a pu
    // changer depuis la fin de la partie
    private final Function<RegionFr, String> nommer;
    private Partie partie;
    // les régions de métropole du mode de la partie
    private Map<RegionFr, Path2D> metropole = Map.of();

    CarteFin(Function<RegionFr, String> nommer) {
        this.nommer = nommer;
        // inscrit le composant auprès du gestionnaire des bulles d'aide : getToolTipText est alors appelé au survol
        setToolTipText("");
    }

    // la partie finie à montrer
    void montrer(Partie finie) {
        partie = finie;
        metropole = contours.regions(finie.getMode());
        repaint();
    }

    // ---- les couleurs : sans affichage, testées à part ----

    // la couleur d'une région, d'après la partie : la réponse, une région proposée, ou une autre (FOND, en thème clair)
    static Color couleur(RegionFr region, Partie partie) {
        return couleur(region, partie, FOND);
    }

    // la même, avec le fond du thème pour les régions qui ne sont ni la réponse ni proposées
    static Color couleur(RegionFr region, Partie partie, Color fond) {
        if (region == partie.getCible()) {
            return partie.getEtat() == Partie.Etat.GAGNEE ? TROUVEE : MANQUEE;
        }
        return partie.getEssais().stream()
                .filter(essai -> essai.proposition() == region)
                .findFirst()
                .map(essai -> degrade(essai.pourcentage()))
                .orElse(fond);
    }

    // le dégradé continu : de LOIN à MILIEU sur la première moitié, puis de MILIEU à PROCHE. Trois couleurs plutôt
    // que deux : un simple mélange du jaune pâle et de l'orange foncé passerait par des teintes ternes au milieu
    static Color degrade(int pourcentage) {
        double t = Math.clamp(pourcentage, 0, 100) / 100.0;
        return t <= 0.5 ? melanger(LOIN, MILIEU, t * 2) : melanger(MILIEU, PROCHE, (t - 0.5) * 2);
    }

    // la couleur à la part t du chemin de a vers b (t = 0 : a, t = 1 : b), composante par composante
    static Color melanger(Color a, Color b, double t) {
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // ---- la géométrie : où se dessine chaque région ----

    // une vue : une forme, et la transformation qui l'amène à sa place dans le composant
    private record Vue(RegionFr region, Path2D forme, AffineTransform transformation) {}

    // les cadres des encarts, en plus des vues (pour dessiner leur bordure)
    private record Disposition(List<Vue> vues, List<Rectangle> encarts) {}

    private Disposition disposer() {
        Rectangle zone = new Rectangle(MARGE, MARGE, getWidth() - 2 * MARGE, getHeight() - 2 * MARGE);
        int largeurEncarts = (int) (zone.width * PART_ENCARTS);
        Rectangle gauche = new Rectangle(zone.x, zone.y, zone.width - largeurEncarts - MARGE, zone.height);
        AffineTransform principale = CarteDepartements.ajuster(
                new Rectangle2D.Double(0, 0, contours.largeur(), contours.hauteur()), gauche);
        List<Vue> vues = new ArrayList<>();
        metropole.forEach((region, forme) -> vues.add(new Vue(region, forme, principale)));

        // les encarts, ligne par ligne, dans l'ordre du fichier : chacun ajusté à sa case, à sa propre échelle
        List<Rectangle> encarts = new ArrayList<>();
        int largeurCase = largeurEncarts / COLONNES;
        int hauteurCase = zone.height / LIGNES;
        int rang = 0;
        for (Map.Entry<RegionFr, ContoursFrance.Encart> entree : contours.outreMer().entrySet()) {
            Rectangle cadre = new Rectangle(zone.x + zone.width - largeurEncarts + (rang % COLONNES) * largeurCase,
                    zone.y + (rang / COLONNES) * hauteurCase, largeurCase - 4, hauteurCase - 4);
            Rectangle interieur = new Rectangle(cadre.x + 4, cadre.y + 4, cadre.width - 8, cadre.height - 8);
            ContoursFrance.Encart encart = entree.getValue();
            vues.add(new Vue(entree.getKey(), encart.forme(), CarteDepartements.ajuster(
                    new Rectangle2D.Double(0, 0, encart.largeur(), encart.hauteur()), interieur)));
            encarts.add(cadre);
            rang++;
        }
        return new Disposition(vues, encarts);
    }

    // la région sous ce point du composant, en métropole ou dans un encart
    Optional<RegionFr> regionSous(Point point) {
        if (partie == null || getWidth() <= 2 * MARGE || getHeight() <= 2 * MARGE) {
            return Optional.empty();
        }
        for (Vue vue : disposer().vues()) {
            try {
                Point2D dansLeFichier = vue.transformation().inverseTransform(point, null);
                if (vue.forme().contains(dansLeFichier)) {
                    return Optional.of(vue.region());
                }
            } catch (NoninvertibleTransformException e) {
                return Optional.empty();   // une transformation d'échelle 0 : composant pas encore dimensionné
            }
        }
        return Optional.empty();
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        return regionSous(e.getPoint()).map(nommer).orElse(null);
    }

    // ---- le dessin ----

    @Override
    protected void paintComponent(Graphics g) {
        if (partie == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Disposition disposition = disposer();
        // d'abord toutes les couleurs, puis tous les bords : un bord dessiné avant le remplissage de la région
        // voisine serait à moitié recouvert, et les frontières n'auraient pas toutes la même épaisseur
        Map<Vue, Shape> formes = new LinkedHashMap<>();
        // le thème est demandé à chaque dessin : un changement de thème se voit au dessin suivant
        boolean sombre = Theme.sombre();
        Color fond = sombre ? FOND_SOMBRE : FOND;
        for (Vue vue : disposition.vues()) {
            // la forme transformée, et non le pinceau : le trait garde ainsi 1 pixel d'épaisseur, quelle que soit
            // l'échelle (comme dans CarteDepartements)
            Shape forme = vue.transformation().createTransformedShape(vue.forme());
            formes.put(vue, forme);
            g2.setColor(couleur(vue.region(), partie, fond));
            g2.fill(forme);
        }
        g2.setColor(sombre ? TRAIT_SOMBRE : TRAIT);
        g2.setStroke(new BasicStroke(1f));
        formes.values().forEach(g2::draw);
        // un cadre fin autour de chaque encart : on voit que ces régions ne sont pas à leur place ni à l'échelle
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
        disposition.encarts().forEach(g2::draw);
        g2.dispose();
    }
}
