package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Tentative;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.JComponent;

/*
La carte des départements du bonus, dessinée à la main (comme les barres des statistiques) : chaque département est
une forme (Path2D) remplie d'une couleur qui dit où l'on en est.
    bleu clair   pas encore demandé (plus foncé sous la souris, quand on peut cliquer)
    bleu foncé   le département à nommer (modes NOM et NUMERO)
    vert, rouge  trouvé, raté
Pendant que la réponse s'affiche, le département cliqué à tort est entouré de rouge.

Les contours sont dans les unités de leur fichier : une transformation affine (AffineTransform : une mise à l'échelle
puis un déplacement) les amène dans le composant. Pour savoir où l'on a cliqué, on fait le chemin inverse : le point
du clic, transformé à l'envers, revient dans les unités du fichier, où Path2D.contains dit dans quelle forme il tombe.

L'Île-de-France a en plus un encart à droite : le même dessin avec une autre transformation, qui agrandit Paris et la
petite couronne (sans lui, Paris ne ferait que quelques pixels).
 */
final class CarteDepartements extends JComponent {

    // les couleurs des images du jeu (outils/generer_images.py) : elles vont avec les deux thèmes
    private static final Color FOND = new Color(0xA2D1FB);
    private static final Color SURVOL = new Color(0x6FB3F2);
    private static final Color DEMANDE = new Color(0x3A4BC4);
    private static final Color TRAIT = new Color(0x7F97AD);
    private static final Color TROUVE = new Color(0x5CB85C);
    private static final Color RATE = new Color(0xE05252);
    private static final int MARGE = 6;
    // la part de la largeur que peut prendre l'encart zoomé
    private static final double PART_ENCART = 0.4;

    private final Consumer<Departement> auClic;
    private Bonus bonus;
    private Contours contours;
    // le temps que la réponse s'affiche, la carte ne répond plus aux clics
    private boolean bloquee;
    private Departement survole;
    private Departement erreur;

    // auClic : ce qu'il faut faire quand le joueur clique un département (la fenêtre le transmet au Bonus)
    CarteDepartements(Consumer<Departement> auClic) {
        this.auClic = auClic;
        MouseAdapter souris = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                survoler(departementSous(e.getPoint()).orElse(null));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                survoler(null);
            }

            // mousePressed plutôt que mouseClicked : mouseClicked n'arrive pas si la souris a bougé d'un pixel entre
            // l'appui et le relâchement, et le joueur croirait que son clic a été perdu
            @Override
            public void mousePressed(MouseEvent e) {
                if (cliquable()) {
                    departementSous(e.getPoint()).ifPresent(auClic);
                }
            }
        };
        addMouseListener(souris);
        addMouseMotionListener(souris);
        // inscrit le composant auprès du gestionnaire des bulles d'aide : getToolTipText est alors appelé au survol
        setToolTipText("");
    }

    // le bonus à montrer, dans l'état où il est
    void montrer(Bonus aMontrer) {
        bonus = aMontrer;
        contours = Contours.de(aMontrer.getRegion());
        bloquee = false;
        erreur = null;
        repaint();
    }

    // la réponse vient d'être donnée : on la montre, sans accepter d'autre clic. erreur : le département cliqué ou
    // nommé à tort (ou null)
    void montrerReponse(Departement erreurDonnee) {
        bloquee = true;
        erreur = erreurDonnee;
        repaint();
    }

    // la question suivante (ou la fin) : la carte répond de nouveau
    void reprendre() {
        bloquee = false;
        erreur = null;
        repaint();
    }

    private boolean cliquable() {
        return bonus != null && bonus.enCours() && !bloquee && bonus.getQuestion() == Bonus.Question.CARTE;
    }

    private void survoler(Departement departement) {
        if (departement != survole) {
            survole = departement;
            repaint();
        }
    }

    // ---- la géométrie : où se dessine chaque partie ----

    // la transformation de la carte entière, et, pour l'Île-de-France, le cadre de l'encart et sa transformation
    private record Vues(AffineTransform principale, Rectangle encart, AffineTransform zoom) {}

    private Vues vues() {
        Rectangle zone = new Rectangle(MARGE, MARGE, getWidth() - 2 * MARGE, getHeight() - 2 * MARGE);
        Rectangle2D tout = new Rectangle2D.Double(0, 0, contours.largeur(), contours.hauteur());
        if (contours.zoom().isEmpty()) {
            return new Vues(ajuster(tout, zone), null, null);
        }
        int cote = (int) Math.min(zone.height, zone.width * PART_ENCART);
        Rectangle encart = new Rectangle(zone.x + zone.width - cote, zone.y + (zone.height - cote) / 2, cote, cote);
        Rectangle reste = new Rectangle(zone.x, zone.y, zone.width - cote - MARGE, zone.height);
        Rectangle interieur = new Rectangle(encart.x + 2, encart.y + 2, encart.width - 4, encart.height - 4);
        return new Vues(ajuster(tout, reste), encart, ajuster(contours.zoom().get(), interieur));
    }

    // la transformation qui fait tenir la source dans le cadre, centrée, sans la déformer (le même rapport pour les
    // deux côtés, comme Representation.ajuster pour les images). Les opérations s'appliquent au point de la dernière
    // écrite à la première : on ramène le coin de la source à l'origine, on agrandit, puis on place dans le cadre
    static AffineTransform ajuster(Rectangle2D source, Rectangle cadre) {
        double rapport = Math.min(cadre.width / source.getWidth(), cadre.height / source.getHeight());
        AffineTransform transformation = new AffineTransform();
        transformation.translate(cadre.x + (cadre.width - source.getWidth() * rapport) / 2,
                cadre.y + (cadre.height - source.getHeight() * rapport) / 2);
        transformation.scale(rapport, rapport);
        transformation.translate(-source.getX(), -source.getY());
        return transformation;
    }

    // le département sous ce point du composant : dans l'encart s'il y en a un et que le point y est, sinon sur la
    // carte entière
    Optional<Departement> departementSous(Point point) {
        if (contours == null || getWidth() <= 2 * MARGE || getHeight() <= 2 * MARGE) {
            return Optional.empty();
        }
        Vues vues = vues();
        AffineTransform transformation = vues.encart() != null && vues.encart().contains(point)
                ? vues.zoom() : vues.principale();
        try {
            Point2D dansLeFichier = transformation.inverseTransform(point, null);
            return contours.formes().entrySet().stream()
                    .filter(entree -> entree.getValue().contains(dansLeFichier))
                    .map(Map.Entry::getKey)
                    .findFirst();
        } catch (NoninvertibleTransformException e) {
            return Optional.empty();   // une transformation d'échelle 0 : composant pas encore dimensionné
        }
    }

    // une fois le bonus fini, le survol d'un département donne son numéro et son nom : on peut apprendre ceux qu'on a
    // ratés. Pas avant, ce serait donner les réponses
    @Override
    public String getToolTipText(MouseEvent e) {
        if (bonus == null || bonus.enCours()) {
            return null;
        }
        return departementSous(e.getPoint()).map(d -> d.code() + " · " + d.nom()).orElse(null);
    }

    // ---- le dessin ----

    @Override
    protected void paintComponent(Graphics g) {
        if (contours == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Vues vues = vues();
        dessiner(g2, vues.principale());
        if (vues.encart() != null) {
            // sur la carte, le cadre de ce qui est agrandi
            g2.setColor(TRAIT);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
            g2.draw(vues.principale().createTransformedShape(contours.zoom().get()));
            // l'encart : un fond, le même dessin agrandi, limité au cadre (clip), puis une bordure
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(getBackground() != null ? getBackground() : Color.WHITE);
            g2.fill(vues.encart());
            Shape avant = g2.getClip();
            g2.clip(vues.encart());
            dessiner(g2, vues.zoom());
            g2.setClip(avant);
            g2.setColor(TRAIT);
            g2.draw(vues.encart());
        }
        g2.dispose();
    }

    private void dessiner(Graphics2D g2, AffineTransform transformation) {
        g2.setStroke(new BasicStroke(1f));
        for (Map.Entry<Departement, Path2D> entree : contours.formes().entrySet()) {
            // la forme transformée, et non le pinceau : le trait garde ainsi 1 pixel d'épaisseur, quelle que soit
            // l'échelle (un pinceau agrandi agrandirait aussi le trait)
            Shape forme = transformation.createTransformedShape(entree.getValue());
            g2.setColor(couleur(entree.getKey()));
            g2.fill(forme);
            g2.setColor(TRAIT);
            g2.draw(forme);
        }
        // (en mode NOM, le joueur peut nommer un département d'une autre région : il n'est pas sur la carte)
        if (erreur != null && contours.formes().containsKey(erreur)) {
            g2.setColor(RATE);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(transformation.createTransformedShape(contours.formes().get(erreur)));
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private Color couleur(Departement departement) {
        Optional<Tentative> tentative = bonus.tentative(departement);
        if (tentative.isPresent()) {
            return tentative.get().juste() ? TROUVE : RATE;
        }
        if (bonus.enCours() && !bloquee && bonus.getQuestion() != Bonus.Question.CARTE
                && departement.equals(bonus.demande())) {
            return DEMANDE;
        }
        return departement.equals(survole) && cliquable() ? SURVOL : FOND;
    }
}
