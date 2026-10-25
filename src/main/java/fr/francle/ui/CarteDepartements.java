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
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/*
La carte des départements du bonus, dessinée à la main (comme les barres des statistiques) : chaque département est
une forme (Path2D) remplie d'une couleur qui dit où l'on en est.
    bleu clair   pas encore répondu (plus foncé sous la souris, quand on peut cliquer) ; bleu sourd en thème sombre
    bleu foncé   le département à nommer (très clair en thème sombre) : celui que le joueur a choisi (mode CHOIX),
                 ou celui de la question (modes NOM et NUMERO)
    vert, rouge  trouvé, raté
En mode CARTE, le département cliqué à tort est entouré de rouge jusqu'à la réponse suivante.

Les contours sont dans les unités de leur fichier : une transformation affine (AffineTransform : une mise à l'échelle
puis un déplacement) les amène dans le composant. Pour savoir où l'on a cliqué, on fait le chemin inverse : le point
du clic, transformé à l'envers, revient dans les unités du fichier, où Path2D.contains dit dans quelle forme il tombe.

L'Île-de-France a en plus un encart à droite : le même dessin avec une autre transformation, qui agrandit Paris et la
petite couronne (sans lui, Paris ne ferait que quelques pixels).

Au clavier (modes CHOIX et CARTE) : Tab amène le focus sur la carte, les flèches passent d'un département à l'autre
(dans l'ordre des numéros, sans écrire leur nom, qui serait la réponse), Entrée ou Espace choisit celui qui est
entouré. Un composant dessiné à la main n'a rien de tout cela d'office : il faut le rendre focalisable, lui donner ses
touches (InputMap et ActionMap) et dessiner soi-même où est le focus.
 */
final class CarteDepartements extends JComponent {

    // les couleurs qui dépendent du thème : le fond des départements, leur survol, celui à nommer, et les bords. En
    // thème clair, celles des images du jeu (outils/generer_images.py). En thème sombre (phase 16), un bleu plus
    // sourd, qui n'éblouit pas sur la fenêtre sombre ; le département à nommer y est donc plus CLAIR que les autres, et
    // non plus foncé : ce qui compte, c'est qu'il se détache
    record Palette(Color fond, Color survol, Color demande, Color trait) {}
    static final Palette CLAIRE = new Palette(new Color(0xA2D1FB), new Color(0x6FB3F2), new Color(0x3A4BC4),
            new Color(0x7F97AD));
    static final Palette SOMBRE = new Palette(new Color(0x3F6690), new Color(0x5A87B8), new Color(0xBFD4FF),
            new Color(0x8FA7C0));
    private static final Color TROUVE = new Color(0x5CB85C);
    private static final Color RATE = new Color(0xE05252);
    private static final int MARGE = 6;
    // la part de la largeur que peut prendre l'encart zoomé
    private static final double PART_ENCART = 0.4;

    private final Consumer<Departement> auClic;
    private Bonus bonus;
    private Contours contours;
    private Departement survole;
    private Departement erreur;
    // mode CHOIX : le département choisi, à nommer (null avant le choix, et après chaque réponse)
    private Departement choisi;
    // le département entouré quand la carte a le focus du clavier : celui qu'Entrée choisirait
    private Departement vise;

    // auClic : ce qu'il faut faire quand le joueur clique (ou choisit au clavier) un département : la fenêtre le
    // transmet au Bonus (mode CARTE) ou le retient comme choix (mode CHOIX)
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
                departementSous(e.getPoint()).filter(CarteDepartements.this::cliquable).ifPresent(departement -> {
                    vise = departement;
                    auClic.accept(departement);
                });
            }
        };
        addMouseListener(souris);
        addMouseMotionListener(souris);
        clavier();
        // inscrit le composant auprès du gestionnaire des bulles d'aide : getToolTipText est alors appelé au survol
        setToolTipText("");
    }

    // le bonus à montrer, dans l'état où il est
    void montrer(Bonus aMontrer) {
        bonus = aMontrer;
        contours = Contours.de(aMontrer.getRegion());
        erreur = null;
        choisi = null;
        vise = null;
        repaint();
    }

    // mode CHOIX : le département que le joueur va nommer, mis en évidence (il peut encore changer d'avis)
    void choisir(Departement departement) {
        choisi = departement;
        vise = departement;
        repaint();
    }

    Optional<Departement> getChoisi() {
        return Optional.ofNullable(choisi);
    }

    // une réponse vient d'être donnée : plus de choix (le joueur clique lui-même le suivant). erreur : en mode CARTE,
    // le département cliqué à tort, entouré de rouge (ou null)
    void montrerReponse(Departement erreurDonnee) {
        erreur = erreurDonnee;
        choisi = null;
        repaint();
    }

    // ce département peut-il être cliqué ? En mode CARTE, tous ; en mode CHOIX, ceux qui n'ont pas encore de réponse
    private boolean cliquable(Departement departement) {
        if (bonus == null || !bonus.enCours()) {
            return false;
        }
        return switch (bonus.getQuestion()) {
            case CARTE -> true;
            case CHOIX -> bonus.restants().contains(departement);
            case NOM, NUMERO -> false;
        };
    }

    // les départements cliquables, dans l'ordre des numéros : le parcours des flèches
    private List<Departement> cliquables() {
        return bonus == null ? List.of() : bonus.getRegion().getDepartements().stream().filter(this::cliquable).toList();
    }

    private void survoler(Departement departement) {
        if (departement != survole) {
            survole = departement;
            repaint();
        }
    }

    // ---- le clavier ----

    private void clavier() {
        setFocusable(true);
        // deux tables : InputMap (touche -> nom d'action), ActionMap (nom -> ce qu'il faut faire). WHEN_FOCUSED :
        // seulement quand la carte a le focus (ailleurs, Entrée garde son rôle : Valider, Partie libre…)
        touche("LEFT", "precedent", () -> deplacer(-1));
        touche("UP", "precedent", () -> deplacer(-1));
        touche("RIGHT", "suivant", () -> deplacer(1));
        touche("DOWN", "suivant", () -> deplacer(1));
        touche("ENTER", "choisir", this::choisirVise);
        touche("SPACE", "choisir", this::choisirVise);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // en arrivant sur la carte, le premier département encore à jouer est entouré
                if (vise == null || !cliquable(vise)) {
                    List<Departement> liste = cliquables();
                    vise = liste.isEmpty() ? null : liste.get(0);
                }
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    private void touche(String touche, String nom, Runnable quoiFaire) {
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(touche), nom);
        getActionMap().put(nom, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quoiFaire.run();
            }
        });
    }

    // le département suivant (sens 1) ou précédent (sens -1) parmi les cliquables ; après le dernier, le premier
    void deplacer(int sens) {
        List<Departement> liste = cliquables();
        if (liste.isEmpty()) {
            return;
        }
        int rang = liste.indexOf(vise);
        vise = rang < 0 ? liste.get(0) : liste.get(Math.floorMod(rang + sens, liste.size()));
        repaint();
    }

    // Entrée ou Espace : comme un clic sur le département entouré
    void choisirVise() {
        if (vise != null && cliquable(vise)) {
            auClic.accept(vise);
        }
    }

    Optional<Departement> getVise() {
        return Optional.ofNullable(vise);
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
            g2.setColor(palette().trait());
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
            g2.setColor(palette().trait());
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
            g2.setColor(palette().trait());
            g2.draw(forme);
        }
        if (erreur != null && contours.formes().containsKey(erreur)) {
            entourer(g2, transformation, erreur, RATE);
        }
        // le focus du clavier : le département qu'Entrée choisirait, entouré de la couleur du focus du thème
        if (isFocusOwner() && vise != null && cliquable(vise)) {
            Color focus = UIManager.getColor("Component.focusColor");
            entourer(g2, transformation, vise, focus != null ? focus : palette().demande());
        }
    }

    private void entourer(Graphics2D g2, AffineTransform transformation, Departement departement, Color couleur) {
        g2.setColor(couleur);
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(transformation.createTransformedShape(contours.formes().get(departement)));
        g2.setStroke(new BasicStroke(1f));
    }

    // les couleurs du thème installé, demandées à chaque dessin (un changement de thème se voit au dessin suivant)
    static Palette palette() {
        return Theme.sombre() ? SOMBRE : CLAIRE;
    }

    private Color couleur(Departement departement) {
        Optional<Tentative> tentative = bonus.tentative(departement);
        if (tentative.isPresent()) {
            return tentative.get().juste() ? TROUVE : RATE;
        }
        if (departement.equals(choisi) || departement.equals(questionEnCours())) {
            return palette().demande();
        }
        return departement.equals(survole) && cliquable(departement) ? palette().survol() : palette().fond();
    }

    // modes NOM et NUMERO : le département de la question, colorié pour être nommé (null sinon)
    private Departement questionEnCours() {
        boolean colorie = bonus.getQuestion() == Bonus.Question.NOM || bonus.getQuestion() == Bonus.Question.NUMERO;
        return bonus.enCours() && colorie ? bonus.demande() : null;
    }
}
