package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/*
La fenêtre du jeu. Elle ne contient aucune règle : elle transmet les actions du joueur à la Partie,
puis affiche ce que la Partie répond. Tout ici s'exécute sur l'Event Dispatch Thread (voir Francle.main).

Disposition : la barre de menus (Jeu, Paramètres, Aide), puis le contenu en BorderLayout imbriqués :
    CENTER  l'image
    SOUTH   la saisie et le message, le tableau des essais, les indices
 */
class Fenetre {

    // le cadre de l'image : toutes les images y sont ajustées, la fenêtre ne change donc pas de taille.
    // Assez bas pour que la fenêtre tienne sur un écran portable (1080p à 150 % : 672 points de haut utilisables)
    private static final int LARGEUR_IMAGE = 400;
    private static final int HAUTEUR_IMAGE = 220;

    // l'ordre des modes dans le menu : le mode par défaut d'abord (l'enum, lui, est rangé par époque)
    private static final List<ModeRegions> MODES = List.of(ModeRegions.NOUVELLES, ModeRegions.ANCIENNES, ModeRegions.TOUTES);

    // la touche des raccourcis de menu : Ctrl sous Windows et Linux, Cmd sur Mac
    private static final int RACCOURCI = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    private final Reglages reglages = new Reglages();
    private final TableauEssais tableau = new TableauEssais();

    // les actions du joueur. Chacune réunit un nom, un raccourci et ce qu'il faut faire ; un article de menu et
    // un bouton peuvent partager la même : la désactiver grise les deux à la fois
    private final Action partieDuJour = action("Partie du jour", KeyStroke.getKeyStroke(KeyEvent.VK_D, RACCOURCI), this::jouerDuJour);
    private final Action partieLibre = action("Partie libre", KeyStroke.getKeyStroke(KeyEvent.VK_N, RACCOURCI), this::jouerLibre);
    private final Action liste = action("Liste des régions", KeyStroke.getKeyStroke(KeyEvent.VK_L, RACCOURCI), this::montrerListe);
    private final Action abandonner = action("Abandonner", null, this::abandonner);
    private final Action quitter = action("Quitter", KeyStroke.getKeyStroke(KeyEvent.VK_Q, RACCOURCI), this::quitter);
    private final Action aide = action("Comment jouer", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), this::montrerAide);

    private final JFrame frame = new JFrame("Francle");
    private final JLabel image = new JLabel();
    private final JTextField saisie = new JTextField(20);
    private final JButton valider = new JButton("Valider");
    private final JButton boutonAbandonner = new JButton(abandonner);
    // à la fin de la partie, « Partie libre » prend la place d'« Abandonner »
    private final JButton boutonRejouer = new JButton(partieLibre);
    private final JLabel message = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea indices = new JTextArea(Indice.values().length, 30);

    // l'horloge qui donne la date du jour. Un paramètre plutôt qu'un appel direct à LocalDate.now() : un test ou un
    // harnais de vérification peut ainsi lancer la fenêtre « le 1er janvier 2030 », sans toucher à l'horloge du système
    private final Clock horloge;

    // la partie affichée : celle du jour ou une partie libre
    private Partie partie;
    // la partie du jour et sa date : si le joueur y revient le même jour, il la retrouve telle qu'il l'a laissée
    // (sinon, revenir à la partie du jour l'effacerait, et donnerait des essais en plus)
    private Partie duJour;
    private LocalDate dateDuJour;

    Fenetre() {
        this(Clock.systemDefaultZone());
    }

    Fenetre(Clock horloge) {
        this.horloge = horloge;
        construire();
        valider.addActionListener(e -> jouer());
        // la touche Entrée dans le champ : l'autocomplétion choisit la suggestion surlignée, ou appelle jouer()
        new Autocompletion(saisie, this::suggestions, this::jouer);
        // au lancement, la partie du jour
        jouerDuJour();
    }

    void afficher() {
        frame.pack();                       // la taille juste nécessaire aux composants
        frame.setLocationRelativeTo(null);  // centrée sur l'écran
        frame.setVisible(true);
        saisie.requestFocusInWindow();
    }

    // une Action à partir d'un nom, d'un raccourci (ou null) et du code à exécuter
    private static Action action(String nom, KeyStroke raccourci, Runnable quoiFaire) {
        Action action = new AbstractAction(nom) {
            @Override
            public void actionPerformed(ActionEvent e) {
                quoiFaire.run();
            }
        };
        // le raccourci n'est utilisé que par les articles de menu (un bouton l'ignore)
        action.putValue(Action.ACCELERATOR_KEY, raccourci);
        return action;
    }

    // ---- construction : les composants et leur disposition ----

    private void construire() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(menus());
        JPanel contenu = new JPanel(new BorderLayout(0, 10));
        contenu.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(LARGEUR_IMAGE, HAUTEUR_IMAGE));
        contenu.add(image, BorderLayout.CENTER);
        contenu.add(bas(), BorderLayout.SOUTH);
        frame.setContentPane(contenu);
    }

    private JMenuBar menus() {
        JMenu jeu = new JMenu("Jeu");
        // Alt+J ouvre le menu (la lettre est soulignée quand on appuie sur Alt)
        jeu.setMnemonic(KeyEvent.VK_J);
        jeu.add(partieDuJour);
        jeu.add(partieLibre);
        jeu.add(liste);
        jeu.add(abandonner);
        jeu.addSeparator();
        jeu.add(quitter);

        JMenu parametres = new JMenu("Paramètres");
        parametres.setMnemonic(KeyEvent.VK_P);
        // boutons radio : le ButtonGroup garantit qu'un seul mode est coché à la fois
        ButtonGroup groupe = new ButtonGroup();
        for (ModeRegions mode : MODES) {
            JRadioButtonMenuItem article = new JRadioButtonMenuItem(Textes.mode(mode), mode == reglages.getMode());
            article.addActionListener(e -> {
                reglages.setMode(mode);
                prevenirProchainePartie();
            });
            groupe.add(article);
            parametres.add(article);
        }
        parametres.addSeparator();
        // cases à cocher : indépendantes l'une de l'autre
        JCheckBoxMenuItem images = new JCheckBoxMenuItem("Silhouette de la région", reglages.isAvecImages());
        images.addActionListener(e -> {
            reglages.setAvecImages(images.isSelected());
            prevenirProchainePartie();
        });
        JCheckBoxMenuItem avecListe = new JCheckBoxMenuItem("Liste des régions", reglages.isAvecListe());
        avecListe.addActionListener(e -> {
            reglages.setAvecListe(avecListe.isSelected());
            // celui-ci compte tout de suite : l'article « Liste des régions » du menu Jeu se grise ou non
            liste.setEnabled(reglages.isAvecListe());
        });
        JCheckBoxMenuItem avecSuggestions = new JCheckBoxMenuItem("Suggestions pendant la frappe", reglages.isAvecSuggestions());
        // compte dès la prochaine lettre tapée
        avecSuggestions.addActionListener(e -> reglages.setAvecSuggestions(avecSuggestions.isSelected()));
        parametres.add(images);
        parametres.add(avecListe);
        parametres.add(avecSuggestions);

        JMenu menuAide = new JMenu("Aide");
        menuAide.setMnemonic(KeyEvent.VK_A);
        menuAide.add(aide);

        JMenuBar barre = new JMenuBar();
        barre.add(jeu);
        barre.add(parametres);
        barre.add(menuAide);
        return barre;
    }

    private JPanel bas() {
        // pas de FlowLayout ici : quand « Nouvelle partie » (plus large) remplace « Abandonner », la ligne déborde,
        // FlowLayout renvoie le bouton à la ligne, et cette seconde ligne est coupée (la hauteur a été fixée par
        // pack). Avec BorderLayout, le champ prend la largeur qui reste et les boutons ne bougent jamais de ligne.
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        boutons.add(valider);
        boutons.add(boutonAbandonner);
        boutons.add(boutonRejouer);
        JPanel ligneSaisie = new JPanel(new BorderLayout());
        ligneSaisie.add(saisie, BorderLayout.CENTER);
        ligneSaisie.add(boutons, BorderLayout.EAST);

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

    // la partie du jour : créée au premier appel de la journée, puis reprise telle quelle (en cours ou terminée)
    private void jouerDuJour() {
        LocalDate aujourdhui = LocalDate.now(horloge);
        if (duJour == null || !aujourdhui.equals(dateDuJour)) {
            dateDuJour = aujourdhui;
            duJour = new Partie(RegionDuJour.pour(aujourdhui), RegionDuJour.MODE);
        }
        montrerPartie(duJour);
    }

    // une partie libre : une nouvelle Partie à chaque fois, avec le mode choisi dans les réglages
    private void jouerLibre() {
        ModeRegions mode = reglages.getMode();
        montrerPartie(new Partie(RegionFr.tirer(mode), mode));
    }

    // affiche une partie dans l'état où elle est (neuve, en cours ou terminée), uniquement à partir de cet état :
    // c'est ce qui permet de reprendre la partie du jour sans rien avoir mémorisé d'autre
    private void montrerPartie(Partie aMontrer) {
        partie = aMontrer;
        frame.setTitle(partie == duJour ? Textes.titreDuJour(dateDuJour) : Textes.TITRE_LIBRE);
        tableau.setPartie(partie);
        afficherIndices();
        liste.setEnabled(reglages.isAvecListe());
        saisie.setText("");
        if (!partie.enCours()) {
            terminer();
            return;
        }
        montrer(Representation.silhouette(partie.getCible(), partie.getMode(), reglages.isAvecImages()));
        message.setText(partie.getEssais().isEmpty() ? "Quelle est cette région française ?" : Textes.apresEssai(partie));
        activerSaisie(true);
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

    // les noms à suggérer pour ce début de saisie, parmi les régions acceptées dans le mode de la partie
    private List<String> suggestions(String debut) {
        if (!reglages.isAvecSuggestions()) {
            return List.of();
        }
        return RegionFr.suggestions(debut, partie.getMode()).stream().map(RegionFr::getNom).toList();
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
        boutonRejouer.requestFocusInWindow();
    }

    // le mode et la silhouette ne changent pas la partie en cours : ils comptent à la suivante (le mode, seulement
    // pour les parties libres : la partie du jour se joue toujours avec les nouvelles régions)
    private void prevenirProchainePartie() {
        if (partie.enCours()) {
            message.setText("Les nouveaux réglages s'appliqueront à la prochaine partie.");
        }
    }

    private void montrerListe() {
        JOptionPane.showMessageDialog(frame, "Voici les régions possibles :\n" + Textes.listeDesRegions(partie.getMode()),
                "Liste des régions", JOptionPane.PLAIN_MESSAGE);
    }

    private void montrerAide() {
        JOptionPane.showMessageDialog(frame, Textes.REGLES, "Comment jouer", JOptionPane.INFORMATION_MESSAGE);
    }

    // comme un clic sur la croix de la fenêtre : un seul chemin pour quitter
    private void quitter() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    // ---- petits utilitaires d'affichage ----

    private void activerSaisie(boolean active) {
        saisie.setEnabled(active);
        valider.setEnabled(active);
        // grise à la fois le bouton et l'article du menu Jeu
        abandonner.setEnabled(active);
        boutonAbandonner.setVisible(active);
        boutonRejouer.setVisible(!active);
        // la touche Entrée déclenche « Valider » pendant la partie, « Nouvelle partie » à la fin
        frame.getRootPane().setDefaultButton(active ? valider : boutonRejouer);
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
