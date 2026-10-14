package fr.francle.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/*
L'autocomplétion, faite maison : sous le champ de saisie, une liste de suggestions mise à jour à chaque lettre.
  ↓ et ↑   parcourent la liste
  Entrée   choisit la suggestion surlignée ; sans suggestion surlignée, valide ce qui est écrit
  Échap    ferme la liste
  un clic  choisit une suggestion

La liste est posée dans le calque des popups de la fenêtre (JLayeredPane.POPUP_LAYER), par-dessus le reste.
Ce n'est ni une fenêtre ni un JPopupMenu : un composant léger qui ne prend jamais le focus, si bien que le
champ garde le clavier pendant qu'on tape.

Cette classe ne connaît ni les régions ni le jeu : la fenêtre lui fournit la fonction qui calcule les suggestions
(source) et l'action à lancer quand on valide.
 */
class Autocompletion {

    private static final int LIGNES_MAX = 6;

    private final JTextField champ;
    private final Function<String, List<String>> source;
    private final Runnable valider;

    private final DefaultListModel<String> modele = new DefaultListModel<>();
    private final JList<String> liste = new JList<>(modele);
    private final JScrollPane cadre = new JScrollPane(liste);

    Autocompletion(JTextField champ, Function<String, List<String>> source, Runnable valider) {
        this.champ = champ;
        this.source = source;
        this.valider = valider;

        liste.setFocusable(false);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // la liste n'a jamais le focus : le thème dessinerait la ligne choisie dans le gris « inactif », peu visible.
        // Le renderer (le tampon qui dessine chaque ligne) impose la couleur d'une sélection active.
        liste.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> liste, Object valeur, int ligne,
                                                          boolean choisie, boolean focus) {
                super.getListCellRendererComponent(liste, valeur, ligne, choisie, focus);
                if (choisie) {
                    setBackground(UIManager.getColor("List.selectionBackground"));
                    setForeground(UIManager.getColor("List.selectionForeground"));
                }
                return this;
            }
        });
        cadre.setFocusable(false);
        // un nom trop long pour la largeur du champ est coupé, plutôt que d'ajouter une barre de défilement
        cadre.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cadre.setVisible(false);

        // à chaque modification du texte, quelle qu'en soit la cause (frappe, collage, effacement)
        champ.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { mettreAJour(); }
            @Override public void removeUpdate(DocumentEvent e) { mettreAJour(); }
            // changement de mise en forme, pas de texte : rien à faire
            @Override public void changedUpdate(DocumentEvent e) { }
        });
        liste.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int ligne = liste.locationToIndex(e.getPoint());
                if (ligne >= 0) {
                    choisir(modele.get(ligne));
                }
            }
        });
        // le champ perd le clavier (clic ailleurs, menu ouvert) : la liste n'a plus de raison d'être
        champ.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { fermer(); }
        });
        // la fenêtre change de taille : la liste suit le champ
        champ.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { placer(); }
            @Override public void componentMoved(ComponentEvent e) { placer(); }
        });

        // ces touches sont reliées au champ lui-même (WHEN_FOCUSED) : elles passent avant tout le reste,
        // et Entrée remplace le comportement normal du champ
        lier("ENTER", this::entree);
        lier("DOWN", () -> deplacer(1));
        lier("UP", () -> deplacer(-1));
        lier("ESCAPE", this::fermer);
    }

    boolean estOuverte() { return cadre.isVisible(); }

    List<String> suggestions() {
        return Collections.list(modele.elements());
    }

    // ---- les touches ----

    private void lier(String touche, Runnable quoiFaire) {
        champ.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(touche), touche);
        champ.getActionMap().put(touche, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quoiFaire.run();
            }
        });
    }

    private void entree() {
        String choisie = liste.getSelectedValue();
        if (estOuverte() && choisie != null) {
            choisir(choisie);
        } else {
            fermer();
            valider.run();
        }
    }

    // une ligne plus bas (+1) ou plus haut (-1), sans sortir de la liste
    private void deplacer(int sens) {
        if (!estOuverte()) {
            return;
        }
        int ligne = Math.max(0, Math.min(modele.size() - 1, liste.getSelectedIndex() + sens));
        liste.setSelectedIndex(ligne);
        liste.ensureIndexIsVisible(ligne);
    }

    // ---- la liste ----

    private void choisir(String suggestion) {
        // setText déclenche mettreAJour, qui peut rouvrir la liste (« Normandie » -> 3 suggestions) : on la ferme après
        champ.setText(suggestion);
        fermer();
    }

    private void mettreAJour() {
        String texte = champ.getText();
        List<String> suggestions = source.apply(texte);
        modele.clear();
        suggestions.forEach(modele::addElement);
        // rien à proposer, ou la seule suggestion est déjà écrite exactement : la liste ne servirait à rien.
        // « Exactement » : equals et non equalsIgnoreCase, sans strip. Pour « bretagne » ou « Bretagne  », la liste
        // reste, et permet de reprendre l'orthographe correcte du nom (majuscule, accents, tirets).
        boolean dejaEcrite = suggestions.size() == 1 && suggestions.get(0).equals(texte);
        if (suggestions.isEmpty() || dejaEcrite) {
            fermer();
            return;
        }
        // aucune ligne surlignée : Entrée valide ce qui est écrit, sauf si le joueur a choisi avec les flèches
        liste.clearSelection();
        liste.setVisibleRowCount(Math.min(LIGNES_MAX, suggestions.size()));
        ouvrir();
    }

    private void ouvrir() {
        JLayeredPane calques = champ.getRootPane() == null ? null : champ.getRootPane().getLayeredPane();
        if (calques == null) {
            return;
        }
        if (cadre.getParent() != calques) {
            // tant qu'elle n'était dans aucune fenêtre, la liste n'a pas suivi un éventuel changement de thème
            SwingUtilities.updateComponentTreeUI(cadre);
            calques.add(cadre, JLayeredPane.POPUP_LAYER);
        }
        cadre.setVisible(true);
        placer();
    }

    // juste sous le champ, de la même largeur, assez haute pour LIGNES_MAX lignes au plus
    private void placer() {
        if (!estOuverte()) {
            return;
        }
        Point coin = SwingUtilities.convertPoint(champ, 0, champ.getHeight(), cadre.getParent());
        int hauteur = cadre.getPreferredSize().height;
        cadre.setBounds(coin.x, coin.y, champ.getWidth(), hauteur);
        cadre.revalidate();
        cadre.repaint();
    }

    private void fermer() {
        cadre.setVisible(false);
    }
}
