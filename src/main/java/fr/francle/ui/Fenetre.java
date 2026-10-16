package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;
import fr.francle.jeu.Reponse;
import fr.francle.jeu.ReponseBonus;
import fr.francle.jeu.RegionDuJour;
import fr.francle.jeu.Tentative;
import fr.francle.stats.Historique;
import fr.francle.stats.Sauvegarde;
import fr.francle.stats.Statistiques;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;

/*
La fenêtre du jeu. Elle ne contient aucune règle : elle transmet les actions du joueur à la Partie (ou au Bonus),
puis affiche ce qu'ils répondent. Tout ici s'exécute sur l'Event Dispatch Thread (voir Francle.main).

Disposition : la barre de menus (Jeu, Paramètres, Aide), puis le contenu en BorderLayout imbriqués :
    CENTER  l'image, ou la carte des départements pendant le bonus (un CardLayout : deux « cartes » empilées au
            même endroit, dont une seule est visible)
    SOUTH   la saisie et le message, le tableau des essais, les indices
 */
class Fenetre {

    // le cadre de l'image : toutes les images y sont ajustées, la fenêtre ne change donc pas de taille.
    // Assez bas pour que la fenêtre tienne sur un écran portable (1080p à 150 % : 672 points de haut utilisables)
    private static final int LARGEUR_IMAGE = 400;
    private static final int HAUTEUR_IMAGE = 220;

    // l'ordre des modes dans le menu : le mode par défaut d'abord (l'enum, lui, est rangé par époque)
    private static final List<ModeRegions> MODES = List.of(ModeRegions.NOUVELLES, ModeRegions.ANCIENNES);

    // la touche des raccourcis de menu : Ctrl sous Windows et Linux, Cmd sur Mac
    private static final int RACCOURCI = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    private final TableauEssais tableau = new TableauEssais();
    private final JTable table = new JTable(tableau);

    // chaque action et la clé de son nom dans les fichiers de textes : à chaque changement de langue, on leur donne
    // leur nouveau nom. Déclarée AVANT les actions : les champs s'initialisent dans l'ordre du fichier, et action()
    // la remplit
    private final Map<Action, String> nomsDesActions = new LinkedHashMap<>();

    // les actions du joueur. Chacune réunit un nom, un raccourci et ce qu'il faut faire ; un article de menu et
    // un bouton peuvent partager la même : la désactiver grise les deux à la fois
    private final Action partieDuJour = action("action.duJour", KeyStroke.getKeyStroke(KeyEvent.VK_D, RACCOURCI), this::jouerDuJour);
    private final Action partieLibre = action("action.libre", KeyStroke.getKeyStroke(KeyEvent.VK_N, RACCOURCI), this::jouerLibre);
    private final Action liste = action("action.liste", KeyStroke.getKeyStroke(KeyEvent.VK_L, RACCOURCI), this::montrerListe);
    private final Action abandonner = action("action.abandonner", null, this::abandonner);
    private final Action statistiques = action("action.statistiques", KeyStroke.getKeyStroke(KeyEvent.VK_T, RACCOURCI), this::montrerStatistiques);
    private final Action quitter = action("action.quitter", KeyStroke.getKeyStroke(KeyEvent.VK_Q, RACCOURCI), this::quitter);
    private final Action aide = action("action.aide", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), this::montrerAide);
    private final Action bonusDuJour = action("action.bonus", KeyStroke.getKeyStroke(KeyEvent.VK_B, RACCOURCI), this::jouerBonus);
    private final Action passer = action("action.passer", null, this::passerBonus);

    private final JFrame frame = new JFrame("Francle");
    // les deux « cartes » du CardLayout, et leurs noms
    private static final String IMAGE = "image";
    private static final String DEPARTEMENTS = "departements";
    private final CardLayout cartes = new CardLayout();
    private final JPanel cadre = new JPanel(cartes);
    private final JLabel image = new JLabel();
    private final CarteDepartements carte = new CarteDepartements(this::cliquer);
    private final JTextField saisie = new JTextField(20);
    private final JButton valider = new JButton();
    private final JButton boutonAbandonner = new JButton(abandonner);
    // à la fin de la partie, « Partie libre » prend la place d'« Abandonner »
    private final JButton boutonRejouer = new JButton(partieLibre);
    // « Passer » pendant le bonus (à la place d'« Abandonner ») ; « Bonus » à la fin d'une partie du jour gagnée
    private final JButton boutonPasser = new JButton(passer);
    private final JButton boutonBonus = new JButton(bonusDuJour);
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
    // l'angle de la silhouette tournée pour la partie libre en cours, tiré au hasard à sa création
    // (celui de la partie du jour se recalcule à partir de la date : RegionDuJour.angle)
    private int angleLibre;
    private static final Random HASARD = new Random();

    // le bonus de la partie du jour : null tant qu'il n'a pas commencé
    private Bonus bonus;
    // vrai quand la fenêtre montre le bonus (la carte des départements) plutôt que la partie
    private boolean dansLeBonus;
    // après chaque réponse du bonus, le temps de voir si c'était juste, avant la question suivante. Un Timer de
    // Swing, et non un Thread.sleep : sleep bloquerait l'Event Dispatch Thread, et la fenêtre ne se redessinerait
    // plus (la réponse ne s'afficherait même pas). Le Timer, lui, rappelle apresReponse sur l'EDT, une seule fois
    private static final int DUREE_REPONSE = 1200;
    private final Timer pause = new Timer(DUREE_REPONSE, e -> apresReponse());

    // l'historique des parties du jour, les réglages, et les fichiers où ils sont enregistrés. Les sauvegardes sont
    // des paramètres, comme l'horloge : la vérification utilise un dossier temporaire, jamais les vrais fichiers du joueur
    private final Sauvegarde<Historique> sauvegardeHistorique;
    private final Sauvegarde<Reglages> sauvegardeReglages;
    private Historique historique;
    private final Reglages reglages;
    // les textes dans la langue choisie : remplacés à chaque changement de langue
    private Textes textes;
    // les problèmes de fichier à signaler au joueur dès que la fenêtre sera visible : le message de l'erreur, et la
    // clé de sa conséquence (traduite au moment de l'afficher, quand la langue est connue)
    private record Avertissement(String erreur, String consequence) {}
    private final List<Avertissement> avertissements = new ArrayList<>();
    // un échec d'enregistrement n'est signalé qu'une fois par fichier, pas à chaque essai ni à chaque réglage
    private boolean enregistrementSignale;
    private boolean reglagesSignales;

    Fenetre() {
        this(Clock.systemDefaultZone(), Sauvegarde.historique(), Reglages.sauvegarde());
    }

    Fenetre(Clock horloge, Sauvegarde<Historique> sauvegardeHistorique, Sauvegarde<Reglages> sauvegardeReglages) {
        this.horloge = horloge;
        this.sauvegardeHistorique = sauvegardeHistorique;
        this.sauvegardeReglages = sauvegardeReglages;
        historique = charger(sauvegardeHistorique, "fichiers.historique");
        reglages = charger(sauvegardeReglages, "fichiers.reglages");
        textes = new Textes(reglages.langue());
        tableau.setUnitesAnglaises(reglages.unitesAnglaises());
        construire();
        appliquerLangue();
        // les composants ont été créés avec le thème clair (installé par Francle.main) : s'il faut le sombre,
        // ils s'y réabonnent tous avant que la fenêtre ne s'affiche
        if (reglages.isThemeSombre()) {
            Theme.changer(true);
        }
        pause.setRepeats(false);
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
        if (!avertissements.isEmpty()) {
            List<String> lignes = avertissements.stream()
                    .map(avertissement -> avertissement.erreur() + "\n" + textes.texte(avertissement.consequence()))
                    .toList();
            String texte = String.join("\n\n", lignes);
            // invokeLater : la boîte de dialogue s'ouvre une fois la fenêtre dessinée, par-dessus elle
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, texte, textes.texte("fichiers.titre"),
                    JOptionPane.WARNING_MESSAGE));
        }
    }

    // un fichier absent n'est pas une erreur (premier lancement : valeur par défaut) ; un fichier illisible a été
    // mis de côté par la Sauvegarde : on le dira au joueur, et le jeu continue avec la valeur par défaut.
    // Une seule méthode pour l'historique et les réglages : <T> la rend générique, comme Sauvegarde<T>
    private <T> T charger(Sauvegarde<T> sauvegarde, String consequence) {
        try {
            return sauvegarde.charger();
        } catch (IOException e) {
            avertissements.add(new Avertissement(e.getMessage(), consequence));
            return sauvegarde.parDefaut();
        }
    }

    // une Action à partir de la clé de son nom, d'un raccourci (ou null) et du code à exécuter. Le nom lui-même
    // est donné par appliquerLangue
    private Action action(String cleDuNom, KeyStroke raccourci, Runnable quoiFaire) {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quoiFaire.run();
            }
        };
        // le raccourci n'est utilisé que par les articles de menu (un bouton l'ignore)
        action.putValue(Action.ACCELERATOR_KEY, raccourci);
        nomsDesActions.put(action, cleDuNom);
        return action;
    }

    // ---- construction : les composants et leur disposition ----

    private void construire() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contenu = new JPanel(new BorderLayout(0, 10));
        contenu.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        cadre.setPreferredSize(new Dimension(LARGEUR_IMAGE, HAUTEUR_IMAGE));
        cadre.add(image, IMAGE);
        cadre.add(carte, DEPARTEMENTS);
        contenu.add(cadre, BorderLayout.CENTER);
        contenu.add(bas(), BorderLayout.SOUTH);
        frame.setContentPane(contenu);
    }

    // la barre de menus, dans la langue du moment : reconstruite à chaque changement de langue (plus simple que de
    // renommer un à un chaque article, et les cases cochées y reprennent l'état des réglages)
    private JMenuBar menus() {
        JMenu jeu = menu("menu.jeu");
        jeu.add(partieDuJour);
        jeu.add(partieLibre);
        jeu.add(abandonner);
        jeu.add(bonusDuJour);
        jeu.addSeparator();
        jeu.add(statistiques);
        jeu.add(liste);
        jeu.addSeparator();
        jeu.add(quitter);

        JMenu parametres = menu("menu.parametres");
        // boutons radio : le ButtonGroup garantit qu'un seul mode est coché à la fois
        ButtonGroup groupe = new ButtonGroup();
        for (ModeRegions mode : MODES) {
            JRadioButtonMenuItem article = new JRadioButtonMenuItem(textes.mode(mode), mode == reglages.getMode());
            article.addActionListener(e -> {
                reglages.setMode(mode);
                retenirReglages();
                prevenirProchainePartie();
            });
            groupe.add(article);
            parametres.add(article);
        }
        parametres.addSeparator();
        // cases à cocher : indépendantes l'une de l'autre
        JCheckBoxMenuItem images = new JCheckBoxMenuItem(textes.texte("parametre.silhouette"), reglages.isAvecImages());
        JCheckBoxMenuItem tournee = new JCheckBoxMenuItem(textes.texte("parametre.tournee"), reglages.isSilhouetteTournee());
        // sans silhouette, rien à tourner : la case est grisée
        tournee.setEnabled(reglages.isAvecImages());
        images.addActionListener(e -> {
            reglages.setAvecImages(images.isSelected());
            retenirReglages();
            tournee.setEnabled(reglages.isAvecImages());
            prevenirProchainePartie();
        });
        tournee.addActionListener(e -> {
            reglages.setSilhouetteTournee(tournee.isSelected());
            retenirReglages();
            prevenirProchainePartie();
        });
        JCheckBoxMenuItem avecListe = new JCheckBoxMenuItem(textes.texte("parametre.liste"), reglages.isAvecListe());
        avecListe.addActionListener(e -> {
            reglages.setAvecListe(avecListe.isSelected());
            retenirReglages();
            // celui-ci compte tout de suite : l'article « Liste des régions » du menu Jeu se grise ou non
            liste.setEnabled(reglages.isAvecListe());
        });
        JCheckBoxMenuItem avecSuggestions = new JCheckBoxMenuItem(textes.texte("parametre.suggestions"), reglages.isAvecSuggestions());
        // compte dès la prochaine lettre tapée
        avecSuggestions.addActionListener(e -> {
            reglages.setAvecSuggestions(avecSuggestions.isSelected());
            retenirReglages();
        });
        JCheckBoxMenuItem themeSombre = new JCheckBoxMenuItem(textes.texte("parametre.themeSombre"), reglages.isThemeSombre());
        // tout de suite : toute la fenêtre change de couleurs
        themeSombre.addActionListener(e -> {
            reglages.setThemeSombre(themeSombre.isSelected());
            retenirReglages();
            Theme.changer(reglages.isThemeSombre());
        });
        JCheckBoxMenuItem miles = new JCheckBoxMenuItem(textes.texte("parametre.miles"), reglages.unitesAnglaises());
        // tout de suite aussi : le tableau et les indices se réaffichent dans la nouvelle unité. Dès ce clic, l'unité
        // est un choix du joueur : elle ne suivra plus la langue
        miles.addActionListener(e -> {
            reglages.setUnitesAnglaises(miles.isSelected());
            retenirReglages();
            tableau.setUnitesAnglaises(reglages.unitesAnglaises());
            afficherIndices();
        });
        // la langue : un sous-menu, avec le nom de chaque langue écrit dans cette langue
        JMenu langues = new JMenu(textes.texte("menu.langue"));
        ButtonGroup groupeLangues = new ButtonGroup();
        for (Langue langue : Langue.values()) {
            JRadioButtonMenuItem article = new JRadioButtonMenuItem(langue.getNomPropre(), langue == textes.getLangue());
            article.addActionListener(e -> changerLangue(langue));
            groupeLangues.add(article);
            langues.add(article);
        }
        parametres.add(images);
        parametres.add(tournee);
        parametres.add(avecListe);
        parametres.add(avecSuggestions);
        parametres.addSeparator();
        parametres.add(themeSombre);
        parametres.add(miles);
        parametres.add(langues);

        JMenu menuAide = menu("menu.aide");
        menuAide.add(aide);

        JMenuBar barre = new JMenuBar();
        barre.add(jeu);
        barre.add(parametres);
        barre.add(menuAide);
        return barre;
    }

    // un menu de la barre, avec sa lettre : Alt+J ouvre « Jeu » en français, Alt+G « Game » en anglais (la lettre est
    // soulignée quand on appuie sur Alt). Elle est dans le fichier de textes, puisqu'elle dépend du mot
    private JMenu menu(String cle) {
        JMenu menu = new JMenu(textes.texte(cle));
        menu.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(textes.texte(cle + ".lettre").charAt(0)));
        return menu;
    }

    private JPanel bas() {
        // pas de FlowLayout ici : quand « Nouvelle partie » (plus large) remplace « Abandonner », la ligne déborde,
        // FlowLayout renvoie le bouton à la ligne, et cette seconde ligne est coupée (la hauteur a été fixée par
        // pack). Avec BorderLayout, le champ prend la largeur qui reste et les boutons ne bougent jamais de ligne.
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        boutons.add(valider);
        boutons.add(boutonAbandonner);
        boutons.add(boutonRejouer);
        boutons.add(boutonPasser);
        boutons.add(boutonBonus);
        JPanel ligneSaisie = new JPanel(new BorderLayout());
        ligneSaisie.add(saisie, BorderLayout.CENTER);
        ligneSaisie.add(boutons, BorderLayout.EAST);

        message.setFont(message.getFont().deriveFont(Font.BOLD));
        JPanel haut = new JPanel(new BorderLayout(0, 6));
        haut.add(ligneSaisie, BorderLayout.CENTER);
        haut.add(message, BorderLayout.SOUTH);

        indices.setEditable(false);
        indices.setFocusable(false);

        JPanel bas = new JPanel(new BorderLayout(0, 10));
        bas.add(haut, BorderLayout.NORTH);
        bas.add(tableEssais(), BorderLayout.CENTER);
        bas.add(indices, BorderLayout.SOUTH);
        return bas;
    }

    private JScrollPane tableEssais() {
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
        // la zone visible fait exactement 6 lignes : pas de barre de défilement, pas de vide en dessous
        table.setPreferredScrollableViewportSize(new Dimension(LARGEUR_IMAGE, table.getRowHeight() * Partie.ESSAIS_MAX));
        return new JScrollPane(table);
    }

    // ---- événements : ce qui se passe quand le joueur agit ----

    // la partie du jour : créée au premier appel de la journée, puis reprise telle quelle (en cours ou terminée)
    private void jouerDuJour() {
        LocalDate aujourdhui = LocalDate.now(horloge);
        if (duJour == null || !aujourdhui.equals(dateDuJour)) {
            dateDuJour = aujourdhui;
            // neuve, en cours ou terminée selon l'historique : relancer le jeu ne donne pas d'essais en plus
            duJour = historique.partieDuJour(aujourdhui);
            // le bonus déjà joué ce jour-là (fini), ou null s'il reste à jouer
            bonus = historique.bonusDuJour(aujourdhui).orElse(null);
        }
        montrerPartie(duJour);
    }

    // une partie libre : une nouvelle Partie à chaque fois, avec le mode choisi dans les réglages
    private void jouerLibre() {
        ModeRegions mode = reglages.getMode();
        angleLibre = HASARD.nextInt(360);
        montrerPartie(new Partie(RegionFr.tirer(mode), mode));
    }

    // affiche une partie dans l'état où elle est (neuve, en cours ou terminée), uniquement à partir de cet état :
    // c'est ce qui permet de reprendre la partie du jour sans rien avoir mémorisé d'autre
    private void montrerPartie(Partie aMontrer) {
        quitterBonus();
        partie = aMontrer;
        afficherTitre();
        tableau.setPartie(partie);
        afficherIndices();
        liste.setEnabled(reglages.isAvecListe());
        saisie.setText("");
        if (!partie.enCours()) {
            terminer();
            return;
        }
        montrer(Representation.silhouette(partie.getCible(), reglages.isAvecImages()), angleSilhouette());
        message.setText(textes.apresEssai(partie));
        activerSaisie(true);
        saisie.requestFocusInWindow();
    }

    // Valider, ou Entrée dans le champ de saisie : une proposition de région, ou une réponse du bonus
    private void jouer() {
        if (dansLeBonus) {
            // pendant que la réponse précédente s'affiche, ou une fois le bonus fini, il n'y a rien à valider
            if (bonus.enCours() && !pause.isRunning()) {
                repondre(bonus.repondre(saisie.getText()));
            }
            return;
        }
        // un switch sur les trois réponses possibles : Reponse est « sealed », le compilateur vérifie qu'aucune
        // n'est oubliée. « case Reponse.DejaProposee(RegionFr region) » teste le type ET sort le champ du record
        switch (partie.proposer(saisie.getText())) {
            case Reponse.Inconnue() -> refuser(textes.nonReconnue(saisie.getText()));
            case Reponse.DejaProposee(RegionFr region) -> refuser(textes.dejaProposee(region));
            case Reponse.Jouee jouee -> essaiJoue();
        }
    }

    // une saisie refusée ne coûte pas d'essai : on la laisse dans le champ, sélectionnée, pour la corriger
    private void refuser(String explication) {
        message.setText(explication);
        saisie.selectAll();
    }

    private void essaiJoue() {
        tableau.essaiJoue();
        saisie.setText("");
        afficherIndices();
        retenirDuJour();
        if (partie.enCours()) {
            message.setText(textes.apresEssai(partie));
        } else {
            terminer();
            statistiquesEnFinDuJour();
        }
    }

    // les noms à suggérer pour ce début de saisie, parmi les régions acceptées dans le mode de la partie, dans la
    // langue du joueur
    private List<String> suggestions(String debut) {
        if (!reglages.isAvecSuggestions()) {
            return List.of();
        }
        if (dansLeBonus) {
            // mode B : les départements de la région ; pour un numéro, rien à suggérer
            if (bonus.getQuestion() != Bonus.Question.NOM) {
                return List.of();
            }
            return Departement.suggestions(debut, bonus.getRegion().getDepartements(), textes.getLangue()).stream()
                    .map(Departement::nom).toList();
        }
        return RegionFr.suggestions(debut, partie.getMode(), textes.getLangue()).stream().map(textes::nom).toList();
    }

    private void abandonner() {
        partie.abandonner();
        retenirDuJour();
        terminer();
        statistiquesEnFinDuJour();
    }

    // après chaque essai ou abandon de la partie du jour : l'historique est mis à jour et enregistré tout de suite,
    // pour qu'un plantage ou une fermeture brutale ne fasse rien perdre. Les parties libres ne sont pas enregistrées.
    private void retenirDuJour() {
        if (partie != duJour) {
            return;
        }
        historique = historique.avec(dateDuJour, duJour);
        enregistrerHistorique();
    }

    // après chaque réponse au bonus, et quand on le passe : comme pour la partie, tout de suite. Fermer le jeu en plein
    // bonus ne fait donc rien perdre : au lancement suivant, il compte comme passé, avec les réponses déjà données
    private void retenirBonus() {
        historique = historique.avecBonus(dateDuJour, bonus);
        enregistrerHistorique();
    }

    private void enregistrerHistorique() {
        try {
            sauvegardeHistorique.enregistrer(historique);
        } catch (IOException e) {
            if (!enregistrementSignale) {
                enregistrementSignale = true;
                JOptionPane.showMessageDialog(frame,
                        textes.texte("historique.erreur", sauvegardeHistorique.getFichier(), e.getMessage()),
                        textes.texte("historique.titre"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // après chaque changement dans le menu Paramètres : retrouvé au prochain lancement
    private void retenirReglages() {
        try {
            sauvegardeReglages.enregistrer(reglages);
        } catch (IOException e) {
            if (!reglagesSignales) {
                reglagesSignales = true;
                JOptionPane.showMessageDialog(frame,
                        textes.texte("reglages.erreur", sauvegardeReglages.getFichier(), e.getMessage()),
                        textes.texte("reglages.titre"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // la partie du jour vient de se terminer : les statistiques s'affichent d'elles-mêmes, comme dans Wordle.
    // invokeLater : d'abord la fin de partie dessinée (carte, message), ensuite la boîte de dialogue. Après une
    // victoire, le bonus commence dès qu'on la ferme (showMessageDialog ne rend la main qu'une fois fermée)
    private void statistiquesEnFinDuJour() {
        if (partie == duJour && !partie.enCours()) {
            SwingUtilities.invokeLater(() -> {
                montrerStatistiques();
                if (partie == duJour && bonusAJouer()) {
                    jouerBonus();
                }
            });
        }
    }

    private void montrerStatistiques() {
        LocalDate aujourdhui = LocalDate.now(horloge);
        Statistiques stats = Statistiques.de(historique.resultats(), aujourdhui);
        // la barre de la victoire du jour est mise en valeur
        int essaisDuJour = historique.resultats().stream()
                .filter(resultat -> resultat.date().equals(aujourdhui) && resultat.victoire())
                .mapToInt(resultat -> resultat.propositions().size())
                .findFirst().orElse(0);
        JOptionPane.showMessageDialog(frame, FenetreStatistiques.panneau(stats, essaisDuJour, textes),
                textes.texte("action.statistiques"), JOptionPane.PLAIN_MESSAGE);
    }

    private void terminer() {
        message.setText(messageDeFin());
        activerSaisie(false);
        // la réponse sur la carte de France (métropole) ; pour l'outre-mer, on garde l'image affichée
        Representation.carte(partie.getCible(), partie.getMode()).ifPresent(this::montrer);
        boutonRejouer.requestFocusInWindow();
    }

    // le mode et la silhouette ne changent pas la partie en cours : ils comptent à la suivante (le mode, seulement
    // pour les parties libres : la partie du jour se joue toujours avec les nouvelles régions)
    private void prevenirProchainePartie() {
        if (partie.enCours()) {
            message.setText(textes.texte("prochainePartie"));
        }
    }

    private void montrerListe() {
        JOptionPane.showMessageDialog(frame, textes.listeDesRegions(partie.getMode()),
                textes.texte("action.liste"), JOptionPane.PLAIN_MESSAGE);
    }

    private void montrerAide() {
        JOptionPane.showMessageDialog(frame, textes.aide(), textes.texte("action.aide"), JOptionPane.INFORMATION_MESSAGE);
    }

    // le joueur choisit une langue dans le menu : tout change tout de suite, même au milieu d'une partie (rien dans
    // la partie ne dépend de la langue : seulement la façon de l'écrire)
    private void changerLangue(Langue langue) {
        reglages.setLangue(langue);
        retenirReglages();
        appliquerLangue();
        // les unités suivent la langue, tant que le joueur ne les a pas choisies lui-même
        tableau.setUnitesAnglaises(reglages.unitesAnglaises());
        afficherTitre();
        if (dansLeBonus) {
            // (pendant l'affichage d'une réponse, le message garde sa langue le temps de passer à la question suivante)
            if (!pause.isRunning()) {
                afficherBonus();
            }
        } else {
            message.setText(partie.enCours() ? textes.apresEssai(partie) : messageDeFin());
        }
        afficherIndices();
    }

    // tous les textes qui ne dépendent pas de la partie : noms des actions, menus, bouton, titres du tableau
    private void appliquerLangue() {
        textes = new Textes(reglages.langue());
        nomsDesActions.forEach((action, cle) -> action.putValue(Action.NAME, textes.texte(cle)));
        frame.setJMenuBar(menus());
        frame.getRootPane().revalidate();
        valider.setText(textes.texte("valider"));
        // le bouton du bonus a un nom court, à lui : celui de l'action (« Bonus des départements ») élargirait la fenêtre
        boutonBonus.setHideActionText(true);
        boutonBonus.setText(textes.texte("bouton.bonus"));
        indices.setBorder(BorderFactory.createTitledBorder(textes.texte("indices")));
        tableau.setTextes(textes);
        // la JTable a copié les titres des colonnes à sa création : on les lui redonne, puis on redessine l'en-tête
        for (int colonne = 0; colonne < tableau.getColumnCount(); colonne++) {
            table.getColumnModel().getColumn(colonne).setHeaderValue(tableau.getColumnName(colonne));
        }
        table.getTableHeader().repaint();
    }

    private void afficherTitre() {
        frame.setTitle(partie == duJour ? textes.titreDuJour(dateDuJour) : textes.texte("titre.libre"));
    }

    // comme un clic sur la croix de la fenêtre : un seul chemin pour quitter
    private void quitter() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    // ---- le bonus des départements ----

    // le bonus existe dès que la partie du jour est gagnée (il se revoit une fois fini, depuis le menu Jeu)
    private boolean bonusGagne() {
        return duJour != null && duJour.getEtat() == Partie.Etat.GAGNEE;
    }

    // il reste un bonus à jouer : pas commencé, ou en cours
    private boolean bonusAJouer() {
        return bonusGagne() && (bonus == null || bonus.enCours());
    }

    // montre le bonus du jour dans l'état où il est : nouveau, en cours, ou fini (pour revoir la carte)
    private void jouerBonus() {
        // déjà dans le bonus (Ctrl+B en plein bonus) : rien à faire, et surtout pas le quitter, ce qui le passerait
        if (dansLeBonus) {
            return;
        }
        // d'abord la partie du jour (si l'on était en partie libre, ou si la date a changé)
        jouerDuJour();
        if (!bonusGagne()) {
            return;
        }
        if (bonus == null) {
            bonus = Bonus.duJour(duJour.getCible(), dateDuJour);
        }
        dansLeBonus = true;
        carte.montrer(bonus);
        cartes.show(cadre, DEPARTEMENTS);
        saisie.setText("");
        afficherBonus();
    }

    // un clic sur un département de la carte (la carte ne le transmet que quand on peut cliquer)
    private void cliquer(Departement departement) {
        repondre(bonus.repondre(departement));
    }

    private void repondre(ReponseBonus reponse) {
        switch (reponse) {
            case ReponseBonus.Inconnue() -> refuser(textes.bonusInconnu(bonus, saisie.getText()));
            case ReponseBonus.Jouee(Tentative tentative) -> {
                retenirBonus();
                saisie.setText("");
                carte.montrerReponse(tentative.juste() ? null : tentative.donne());
                message.setText(textes.reponseBonus(bonus, tentative));
                // rien à faire pendant que la réponse s'affiche : la carte est bloquée, la saisie et Passer grisés
                saisie.setEnabled(false);
                valider.setEnabled(false);
                passer.setEnabled(false);
                pause.restart();
            }
        }
    }

    // la réponse a été vue : la question suivante, ou la fin du bonus
    private void apresReponse() {
        carte.reprendre();
        afficherBonus();
    }

    // le bouton « Passer » : le bonus s'arrête là. S'il n'a pas commencé, il reste à jouer (bouton « Bonus »)
    private void passerBonus() {
        pause.stop();
        if (bonus.getTentatives().isEmpty()) {
            dansLeBonus = false;
            montrerPartie(duJour);
            return;
        }
        bonus.passer();
        retenirBonus();
        carte.reprendre();
        afficherBonus();
    }

    // le joueur va voir ailleurs (partie libre, partie du jour…) en plein bonus : un bonus commencé compte comme
    // passé, avec les réponses déjà données
    private void quitterBonus() {
        pause.stop();
        if (dansLeBonus && bonus.enCours() && !bonus.getTentatives().isEmpty()) {
            bonus.passer();
            // (rien de nouveau à enregistrer : les réponses le sont déjà, et un bonus relu est toujours fini)
        }
        dansLeBonus = false;
        cartes.show(cadre, IMAGE);
    }

    // les boutons et le message du bonus, d'après son état
    private void afficherBonus() {
        boolean enCours = bonus.enCours();
        // en mode CARTE, on répond en cliquant : la saisie ne sert pas
        boolean ecrire = enCours && bonus.getQuestion() != Bonus.Question.CARTE;
        saisie.setEnabled(ecrire);
        valider.setEnabled(ecrire);
        valider.setVisible(ecrire);
        abandonner.setEnabled(false);
        boutonAbandonner.setVisible(false);
        passer.setEnabled(enCours);
        boutonPasser.setVisible(enCours);
        boutonRejouer.setVisible(!enCours);
        boutonBonus.setVisible(false);
        bonusDuJour.setEnabled(true);
        message.setText(enCours ? textes.questionBonus(bonus) : textes.finBonus(bonus));
        frame.getRootPane().setDefaultButton(ecrire ? valider : enCours ? null : boutonRejouer);
        if (ecrire) {
            saisie.requestFocusInWindow();
        } else if (!enCours) {
            boutonRejouer.requestFocusInWindow();
        }
    }

    // le message de fin de partie ; pour la partie du jour dont le bonus est fini, suivi du score du bonus
    private String messageDeFin() {
        String fin = textes.fin(partie);
        if (partie == duJour && bonus != null && !bonus.enCours()) {
            return fin + " " + textes.resumeBonus(bonus);
        }
        return fin;
    }

    // ---- petits utilitaires d'affichage ----

    private void activerSaisie(boolean active) {
        saisie.setEnabled(active);
        valider.setEnabled(active);
        // caché plutôt que grisé quand il ne sert pas : la place va aux boutons de la fin (Partie libre, Bonus), et
        // la fenêtre garde sa largeur
        valider.setVisible(active);
        // grise à la fois le bouton et l'article du menu Jeu
        abandonner.setEnabled(active);
        boutonAbandonner.setVisible(active);
        boutonRejouer.setVisible(!active);
        boutonPasser.setVisible(false);
        // à la fin d'une partie du jour gagnée, tant que le bonus n'est pas fini
        boutonBonus.setVisible(!active && partie == duJour && bonusAJouer());
        bonusDuJour.setEnabled(bonusGagne());
        // la touche Entrée déclenche « Valider » pendant la partie, « Nouvelle partie » à la fin
        frame.getRootPane().setDefaultButton(active ? valider : boutonRejouer);
    }

    private void montrer(String chemin) {
        montrer(chemin, 0);
    }

    private void montrer(String chemin, int degres) {
        ImageIcon icone = Representation.icone(chemin, LARGEUR_IMAGE, HAUTEUR_IMAGE, degres);
        image.setIcon(icone);
    }

    // de combien tourner la silhouette : 0 si l'option est décochée ou s'il n'y a pas de silhouette ; sinon l'angle de
    // la partie du jour (le même pour tous) ou celui tiré pour la partie libre
    private int angleSilhouette() {
        if (!reglages.isAvecImages() || !reglages.isSilhouetteTournee()) {
            return 0;
        }
        return partie == duJour ? RegionDuJour.angle(dateDuJour) : angleLibre;
    }

    private void afficherIndices() {
        List<String> lignes = new ArrayList<>();
        for (Indice indice : partie.indicesReveles()) {
            lignes.add(textes.indice(indice, partie.getCible(), reglages.unitesAnglaises()));
        }
        indices.setText(String.join("\n", lignes));
    }
}
