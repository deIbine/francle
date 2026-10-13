package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/*
La fenêtre du jeu. Elle ne contient aucune règle : elle transmet les actions du joueur à la Partie,
puis affiche ce que la Partie répond. Tout ici s'exécute sur l'Event Dispatch Thread (voir Francle.main).

Disposition (BorderLayout imbriqués) :
    NORTH   la barre de boutons
    CENTER  l'image
    SOUTH   la saisie et le message, le tableau des essais, les indices
 */
class Fenetre {

    // le cadre de l'image : toutes les images y sont ajustées, la fenêtre ne change donc pas de taille.
    // Assez bas pour que la fenêtre tienne sur un écran portable (1080p à 150 % : 672 points de haut utilisables)
    private static final int LARGEUR_IMAGE = 400;
    private static final int HAUTEUR_IMAGE = 220;

    private final Reglages reglages = new Reglages();
    private final TableauEssais tableau = new TableauEssais();

    private final JFrame frame = new JFrame("Francle");
    private final JLabel image = new JLabel();
    private final JTextField saisie = new JTextField(20);
    private final JButton valider = new JButton("Valider");
    private final JButton abandonner = new JButton("Abandonner");
    private final JButton nouvellePartie = new JButton("Nouvelle partie");
    private final JButton liste = new JButton("Liste des régions");
    private final JLabel message = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea indices = new JTextArea(Indice.values().length, 30);

    private Partie partie;

    Fenetre() {
        construire();
        brancher();
        commencer();
    }

    void afficher() {
        frame.pack();                       // la taille juste nécessaire aux composants
        frame.setLocationRelativeTo(null);  // centrée sur l'écran
        frame.setVisible(true);
        saisie.requestFocusInWindow();
    }

    // ---- construction : les composants et leur disposition ----

    private void construire() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contenu = new JPanel(new BorderLayout(0, 10));
        contenu.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        contenu.add(barre(), BorderLayout.NORTH);
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(LARGEUR_IMAGE, HAUTEUR_IMAGE));
        contenu.add(image, BorderLayout.CENTER);
        contenu.add(bas(), BorderLayout.SOUTH);
        frame.setContentPane(contenu);
    }

    private JPanel barre() {
        JButton parametres = new JButton("Paramètres");
        JButton aide = new JButton("Comment jouer");
        parametres.addActionListener(e -> parametres());
        aide.addActionListener(e -> JOptionPane.showMessageDialog(frame, Textes.REGLES, "Comment jouer",
                JOptionPane.INFORMATION_MESSAGE));
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barre.add(nouvellePartie);
        barre.add(liste);
        barre.add(parametres);
        barre.add(aide);
        return barre;
    }

    private JPanel bas() {
        JPanel ligneSaisie = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        ligneSaisie.add(saisie);
        ligneSaisie.add(valider);
        ligneSaisie.add(abandonner);

        message.setFont(message.getFont().deriveFont(Font.BOLD));
        JPanel haut = new JPanel(new BorderLayout(0, 6));
        haut.add(ligneSaisie, BorderLayout.CENTER);
        haut.add(message, BorderLayout.SOUTH);

        indices.setEditable(false);
        indices.setFocusable(false);
        indices.setBorder(BorderFactory.createTitledBorder("Indices"));

        JPanel bas = new JPanel(new BorderLayout(0, 10));
        bas.add(haut, BorderLayout.NORTH);
        bas.add(tableEssais(), BorderLayout.CENTER);
        bas.add(indices, BorderLayout.SOUTH);
        return bas;
    }

    private JScrollPane tableEssais() {
        JTable table = new JTable(tableau);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        // toutes les colonnes centrées, sauf le nom de la région
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        for (int colonne = 0; colonne < tableau.getColumnCount(); colonne++) {
            if (colonne != 1) {
                table.getColumnModel().getColumn(colonne).setCellRenderer(centre);
            }
        }
        // les flèches en plus gros. Le renderer de base remet la police du tableau à chaque case qu'il dessine :
        // il faut donc changer la police APRÈS lui, en redéfinissant sa méthode
        DefaultTableCellRenderer fleches = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object valeur, boolean selectionnee,
                                                           boolean focus, int ligne, int colonne) {
                super.getTableCellRendererComponent(table, valeur, selectionnee, focus, ligne, colonne);
                setFont(table.getFont().deriveFont(Font.BOLD, table.getFont().getSize2D() * 1.5f));
                return this;
            }
        };
        fleches.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(fleches);
        table.setRowHeight(Math.round(table.getRowHeight() * 1.2f));
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        // la zone visible fait exactement 5 lignes : pas de barre de défilement, pas de vide en dessous
        table.setPreferredScrollableViewportSize(new Dimension(LARGEUR_IMAGE, table.getRowHeight() * Partie.ESSAIS_MAX));
        return new JScrollPane(table);
    }

    // ---- événements : ce qui se passe quand le joueur agit ----

    private void brancher() {
        // le même traitement pour un clic sur « Valider » et pour la touche Entrée dans le champ de saisie
        valider.addActionListener(e -> jouer());
        saisie.addActionListener(e -> jouer());
        abandonner.addActionListener(e -> abandonner());
        nouvellePartie.addActionListener(e -> commencer());
        liste.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Voici les régions possibles :\n" + Textes.listeDesRegions(partie.getMode()),
                "Liste des régions", JOptionPane.PLAIN_MESSAGE));
    }

    private void commencer() {
        ModeRegions mode = reglages.getMode();
        // une nouvelle Partie à chaque fois : rien ne peut traîner de la partie précédente
        partie = new Partie(RegionFr.tirer(mode), mode);
        tableau.setPartie(partie);
        montrer(Representation.silhouette(partie.getCible(), mode, reglages.isAvecImages()));
        message.setText("Quelle est cette région française ?");
        indices.setText("");
        liste.setEnabled(reglages.isAvecListe());
        activerSaisie(true);
        saisie.setText("");
        saisie.requestFocusInWindow();
    }

    private void jouer() {
        Optional<Essai> essai = partie.proposer(saisie.getText());
        if (essai.isEmpty()) {
            // une saisie non reconnue ne coûte pas d'essai : on la laisse dans le champ, sélectionnée, pour la corriger
            message.setText(Textes.nonReconnue(saisie.getText()));
            saisie.selectAll();
            return;
        }
        tableau.essaiJoue();
        saisie.setText("");
        afficherIndices();
        if (partie.enCours()) {
            message.setText(Textes.apresEssai(partie));
        } else {
            terminer();
        }
    }

    private void abandonner() {
        partie.abandonner();
        terminer();
    }

    private void terminer() {
        message.setText(Textes.fin(partie));
        activerSaisie(false);
        // la réponse sur la carte de France, quand elle existe ; sinon on garde l'image affichée
        Representation.carte(partie.getCible()).ifPresent(this::montrer);
        nouvellePartie.requestFocusInWindow();
    }

    private void parametres() {
        Parametres.modifier(reglages);
        // la liste se montre ou se cache tout de suite ; le mode et les images comptent à la prochaine partie
        liste.setEnabled(reglages.isAvecListe());
        if (partie.enCours()) {
            message.setText("Les nouveaux réglages s'appliqueront à la prochaine partie.");
        }
    }

    // ---- petits utilitaires d'affichage ----

    private void activerSaisie(boolean active) {
        saisie.setEnabled(active);
        valider.setEnabled(active);
        abandonner.setEnabled(active);
        // la touche Entrée déclenche « Valider » pendant la partie, « Nouvelle partie » à la fin
        frame.getRootPane().setDefaultButton(active ? valider : nouvellePartie);
    }

    private void montrer(String chemin) {
        ImageIcon icone = Representation.icone(chemin, LARGEUR_IMAGE, HAUTEUR_IMAGE);
        image.setIcon(icone);
    }

    private void afficherIndices() {
        List<String> lignes = new ArrayList<>();
        for (Indice indice : partie.indicesReveles()) {
            lignes.add(Textes.indice(indice, partie.getCible()));
        }
        indices.setText(String.join("\n", lignes));
    }
}
