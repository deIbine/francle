package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Indice;
import fr.francle.jeu.ModesDifficiles;
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
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
    CENTER  la silhouette pendant la partie, la carte de France à la fin, ou la carte des départements pendant le
            bonus (un CardLayout : trois « cartes » empilées au même endroit, dont une seule est visible)
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
    private final Action actionBonus = action("action.bonus", KeyStroke.getKeyStroke(KeyEvent.VK_B, RACCOURCI), this::jouerBonus);
    private final Action passer = action("action.passer", null, this::passerBonus);
    // Ctrl+Maj+C : Ctrl+C seul copie le texte sélectionné dans le champ de saisie
    private final Action partager = action("action.partager",
            KeyStroke.getKeyStroke(KeyEvent.VK_C, RACCOURCI | KeyEvent.SHIFT_DOWN_MASK), this::partager);

    private final JFrame frame = new JFrame("Francle");
    // les trois « cartes » du CardLayout, et leurs noms
    private static final String IMAGE = "image";
    private static final String FIN = "fin";
    private static final String DEPARTEMENTS = "departements";
    private final CardLayout cartes = new CardLayout();
    private final JPanel cadre = new JPanel(cartes);
    private final JLabel image = new JLabel();
    // la bulle d'aide nomme la région dans la langue du moment : la lambda relit le champ textes à chaque survol.
    // Une référence textes::nom serait liée aux textes du moment de sa création : null ici (le constructeur ne les
    // a pas encore lus), d'où une NullPointerException dès la création de la fenêtre. « this.textes » et non
    // « textes » : le champ est déclaré plus bas, et Java refuse d'en lire le nom seul avant sa déclaration
    // (« illegal forward reference »), même dans une lambda qui ne s'exécutera que plus tard
    private final CarteFin carteFin = new CarteFin(region -> this.textes.nom(region));
    private final CarteDepartements carte = new CarteDepartements(this::cliquer);
    private final JTextField saisie = new JTextField(20);
    private final JButton valider = new JButton();
    private final JButton boutonAbandonner = new JButton(abandonner);
    // à la fin de la partie, « Partie libre » prend la place d'« Abandonner »
    private final JButton boutonRejouer = new JButton(partieLibre);
    // « Passer » pendant le bonus (à la place d'« Abandonner ») ; « Bonus » à la fin d'une partie gagnée dont le
    // bonus a été passé avant toute réponse
    private final JButton boutonPasser = new JButton(passer);
    private final JButton boutonBonus = new JButton(actionBonus);
    // pendant le bonus de la partie du jour : les statistiques, sans quitter le bonus (comme « Voir les résultats »
    // dans la version web)
    private final JButton boutonResultats = new JButton(statistiques);
    private final Message message = new Message();
    // la ligne de saisie : le champ, et à droite les boutons. Sans champ (fin de partie), les boutons passent au milieu
    private final JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private final JPanel ligneSaisie = new JPanel(new BorderLayout());
    // les indices : une ligne chacun, dans l'ordre où ils se révèlent. Ceux qui restent à venir sont écrits en gris
    // (« Capitale régionale : après l'essai 4 ») : le joueur voit ce qui l'attend
    private final JPanel indices = new JPanel(new GridLayout(Indice.values().length, 1));
    private final JLabel[] lignesIndices = Stream.generate(JLabel::new).limit(Indice.values().length)
            .toArray(JLabel[]::new);

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

    // le bonus de la partie affichée (null tant qu'il n'a pas commencé), et celui de la partie du jour, gardé pour y
    // revenir après une partie libre. Les deux désignent le même objet quand la partie affichée est celle du jour
    private Bonus bonus;
    private Bonus bonusDuJour;
    // les modes difficiles de la partie affichée, fixés à son premier essai (null avant : ce sont alors ceux des
    // réglages), et ceux de la partie du jour, enregistrés avec elle pour le partage
    private ModesDifficiles modes;
    private ModesDifficiles modesDuJour;
    // vrai quand la fenêtre montre le bonus (la carte des départements) plutôt que la partie
    private boolean dansLeBonus;
    // le bonus fini, les résultats s'ouvrent d'eux-mêmes, mais pas tout de suite : le temps de voir la couleur de la
    // dernière réponse (repris de la version web). Un Timer de Swing, et non un Thread.sleep : sleep bloquerait
    // l'Event Dispatch Thread, et la fenêtre ne se redessinerait plus (la réponse ne s'afficherait même pas). Le
    // Timer, lui, rappelle montrerResultats sur l'EDT, une seule fois. Depuis le mode C, c'est la seule minuterie du
    // bonus : le joueur choisit lui-même le département suivant, il n'y a plus de question suivante à attendre
    private static final int DUREE_AVANT_RESULTATS = 1500;
    private final Timer avantResultats = new Timer(DUREE_AVANT_RESULTATS, e -> montrerResultats());
    // à minuit, la région du jour change, même si la fenêtre reste ouverte et la partie pas finie (repris de la
    // version web). Un Timer réglé sur le temps qui reste jusqu'à minuit, reprogrammé à chaque fois
    private final Timer minuit = new Timer(0, e -> aMinuit());

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
        avantResultats.setRepeats(false);
        valider.addActionListener(e -> jouer());
        // la touche Entrée dans le champ : l'autocomplétion choisit la suggestion surlignée, ou appelle jouer() (et
        // rouvre ses suggestions si la saisie est refusée)
        new Autocompletion(saisie, this::suggestions, this::jouer);
        // au lancement, la partie du jour
        jouerDuJour();
        minuit.setRepeats(false);
        programmerMinuit();
    }

    void afficher() {
        frame.pack();                       // la taille juste nécessaire aux composants
        frame.setLocationRelativeTo(null);  // centrée sur l'écran
        frame.setVisible(true);
        saisie.requestFocusInWindow();
        // « Comment jouer » s'ouvre de lui-même au premier lancement, comme sur le web, puis plus jamais
        if (!reglages.isAideVue()) {
            reglages.setAideVue(true);
            retenirReglages();
            SwingUtilities.invokeLater(this::montrerAide);
        }
        // la partie du jour déjà finie (le jeu est relancé dans la journée) : ses résultats s'ouvrent, comme dans la
        // version web. Un bonus pas commencé attend son bouton : il ne surprend pas le joueur
        if (duJourFini()) {
            SwingUtilities.invokeLater(this::montrerResultats);
        }
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
        // l'icône de la barre de titre et de la barre des tâches, en plusieurs tailles (Windows choisit). Les boîtes de
        // dialogue ouvertes sur la fenêtre la reprennent
        frame.setIconImages(IconeFenetre.images());
        JPanel contenu = new JPanel(new BorderLayout(0, 10));
        contenu.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        cadre.setPreferredSize(new Dimension(LARGEUR_IMAGE, HAUTEUR_IMAGE));
        cadre.add(image, IMAGE);
        cadre.add(carteFin, FIN);
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
        jeu.add(actionBonus);
        jeu.addSeparator();
        jeu.add(statistiques);
        jeu.add(partager);
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
            silhouetteChangee();
        });
        tournee.addActionListener(e -> {
            reglages.setSilhouetteTournee(tournee.isSelected());
            retenirReglages();
            silhouetteChangee();
        });
        JCheckBoxMenuItem avecListe = new JCheckBoxMenuItem(textes.texte("parametre.liste"), reglages.isAvecListe());
        avecListe.addActionListener(e -> {
            reglages.setAvecListe(avecListe.isSelected());
            retenirReglages();
            // celui-ci compte tout de suite : l'article « Liste des régions » (ou « des départements ») du menu Jeu
            // se grise ou non
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
        // le bonus : un sous-menu à lui, « Bonus 🗺️ », pour sa version difficile. Un menu n'a pas de place pour une
        // phrase d'aide sous la case : elle est dans la bulle d'aide, et dans « Comment jouer »
        JMenu menuBonus = new JMenu(textes.texte("parametre.bonus"));
        JCheckBoxMenuItem difficile = new JCheckBoxMenuItem(textes.texte("parametre.departementsDifficiles"),
                reglages.isDepartementsDifficiles());
        difficile.setToolTipText(textes.texte("parametre.departementsDifficiles.aide"));
        // tout de suite pour un bonus sans réponse ; pour le suivant, si le bonus affiché a déjà commencé
        difficile.addActionListener(e -> {
            reglages.setDepartementsDifficiles(difficile.isSelected());
            retenirReglages();
            appliquerVersion();
        });
        menuBonus.add(difficile);
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
        parametres.add(menuBonus);
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
        boutons.add(valider);
        boutons.add(boutonAbandonner);
        boutons.add(boutonPasser);
        boutons.add(boutonRejouer);
        boutons.add(boutonBonus);
        boutons.add(boutonResultats);
        boutonResultats.setVisible(false);
        ligneSaisie.add(saisie, BorderLayout.CENTER);
        ligneSaisie.add(boutons, BorderLayout.EAST);

        JPanel haut = new JPanel(new BorderLayout(0, 6));
        haut.add(ligneSaisie, BorderLayout.CENTER);
        haut.add(message, BorderLayout.SOUTH);

        // une étiquette par indice, toujours là (révélé ou pas) : la hauteur de l'encadré ne change jamais
        for (JLabel ligne : lignesIndices) {
            indices.add(ligne);
        }

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
        // la proximité sur une pastille de la couleur de la carte de fin
        table.getColumnModel().getColumn(4).setCellRenderer(new CaseProximite(tableau));
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
        if (!quitterPartieLibre(textes.texte("action.duJour"))) {
            return;
        }
        LocalDate aujourdhui = LocalDate.now(horloge);
        if (duJour == null || !aujourdhui.equals(dateDuJour)) {
            dateDuJour = aujourdhui;
            // neuve, en cours ou terminée selon l'historique : relancer le jeu ne donne pas d'essais en plus
            duJour = historique.partieDuJour(aujourdhui);
            // le bonus déjà joué ce jour-là (fini), ou null s'il reste à jouer
            bonusDuJour = historique.bonusDuJour(aujourdhui).orElse(null);
            modesDuJour = historique.modesDuJour(aujourdhui).orElse(null);
        }
        montrerPartie(duJour);
    }

    // une partie libre : une nouvelle Partie à chaque fois, avec le mode choisi dans les réglages
    private void jouerLibre() {
        if (!quitterPartieLibre(textes.texte("action.libre"))) {
            return;
        }
        ModeRegions mode = reglages.getMode();
        angleLibre = HASARD.nextInt(360);
        montrerPartie(new Partie(RegionFr.tirer(mode), mode));
    }

    // vrai si l'on peut quitter la partie affichée pour une autre (titre : le nom de la commande qui la remplace). Une
    // partie libre n'est enregistrée nulle part : la quitter la perd, elle et son bonus. Une question d'abord, comme
    // pour l'abandon, mais seulement s'il y a quelque chose à perdre
    private boolean quitterPartieLibre(String titre) {
        return !libreAPerdre() || confirmer(textes.texte("libre.question"), titre);
    }

    // une partie libre en cours avec au moins un essai, ou son bonus en cours avec au moins une réponse. Une partie
    // neuve ou finie, ou un bonus sans réponse, se quittent sans rien demander ; la partie du jour aussi, qui est
    // enregistrée à chaque essai et se reprend telle quelle
    private boolean libreAPerdre() {
        if (partie == null || partie == duJour) {
            return false;
        }
        boolean partieCommencee = partie.enCours() && !partie.getEssais().isEmpty();
        boolean bonusCommence = bonus != null && bonus.enCours() && !bonus.getTentatives().isEmpty();
        return partieCommencee || bonusCommence;
    }

    // affiche une partie dans l'état où elle est (neuve, en cours ou terminée), uniquement à partir de cet état :
    // c'est ce qui permet de reprendre la partie du jour sans rien avoir mémorisé d'autre
    private void montrerPartie(Partie aMontrer) {
        quitterBonus();
        // une autre partie : son bonus (celui du jour, ou aucun pour une nouvelle partie libre)
        if (aMontrer != partie) {
            bonus = aMontrer == duJour ? bonusDuJour : null;
            modes = aMontrer == duJour ? modesDuJour : null;
        }
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
        montrerSilhouette();
        message.setText(textes.apresEssai(partie));
        activerSaisie(true);
        saisie.requestFocusInWindow();
    }

    // Valider, ou Entrée dans le champ de saisie : une proposition de région, ou une réponse du bonus. Vrai si la
    // saisie a été acceptée (un essai, une réponse du bonus), faux si elle a été refusée ou s'il n'y avait rien à jouer
    private boolean jouer() {
        String texte = saisie.getText();
        // un champ vide (ou fait d'espaces) : rien ne se passe, sans message, dans la partie comme dans le bonus.
        // Un Entrée de trop n'est pas une erreur à reprocher au joueur
        if (texte.isBlank()) {
            return false;
        }
        if (dansLeBonus) {
            // une fois le bonus fini, ou en mode C avant d'avoir choisi un département, il n'y a rien à valider
            if (!bonus.enCours()) {
                return false;
            }
            if (bonus.getQuestion() == Bonus.Question.CHOIX) {
                return carte.getChoisi().map(choisi -> repondre(bonus.repondre(choisi, texte))).orElse(false);
            }
            return repondre(bonus.repondre(texte));
        }
        // un switch sur les réponses possibles : Reponse est « sealed », le compilateur vérifie qu'aucune n'est
        // oubliée. « case Reponse.DejaProposee(RegionFr region) » teste le type ET sort le champ du record.
        // Un switch « expression » (return switch) : chaque cas donne une valeur ; au bout d'un bloc, c'est « yield »
        return switch (partie.proposer(texte)) {
            case Reponse.Inconnue() -> refuser(textes.nonReconnue(texte));
            case Reponse.Plusieurs() -> refuser(textes.texte("plusieurs", texte.strip()));
            case Reponse.AutreEpoque(RegionFr region) -> refuser(textes.autreEpoque(region));
            case Reponse.DejaProposee(RegionFr region) -> refuser(textes.dejaProposee(region));
            case Reponse.Jouee jouee -> {
                essaiJoue();
                yield true;
            }
        };
    }

    // une saisie refusée ne coûte pas d'essai : on la laisse dans le champ, sélectionnée, pour la corriger.
    // Renvoie toujours faux (la saisie n'est pas acceptée) : « return refuser(...) » se lit d'un trait
    private boolean refuser(String explication) {
        message.setText(explication);
        saisie.selectAll();
        return false;
    }

    private void essaiJoue() {
        // le premier essai fixe les modes difficiles de la partie (comme dans la version web)
        if (modes == null) {
            modes = modesChoisis();
            if (partie == duJour) {
                modesDuJour = modes;
            }
        }
        tableau.essaiJoue();
        saisie.setText("");
        afficherIndices();
        retenirDuJour();
        if (partie.enCours()) {
            message.setText(textes.apresEssai(partie));
        } else {
            terminer();
            if (bonusAJouer()) {
                // une victoire, du jour ou libre : le bonus tout de suite, à la place de la silhouette. Les
                // résultats attendront sa fin
                jouerBonus();
            } else {
                // une défaite : les résultats tout de suite
                resultatsEnFin();
            }
        }
    }

    // les noms à suggérer pour ce début de saisie, parmi les régions acceptées dans le mode de la partie, dans la
    // langue du joueur
    private List<String> suggestions(String debut) {
        if (!reglages.isAvecSuggestions()) {
            return List.of();
        }
        if (dansLeBonus) {
            // ceux que le bonus propose (Bonus.proposes : la région ou les 101 selon la version ; rien pour un
            // numéro), chacun avec son numéro (« Finistère (29) »), que la réponse accepte (Departement.chercher)
            return Departement.suggestions(debut, bonus.proposes(), textes.getLangue()).stream()
                    .map(Departement::nomEtNumero).toList();
        }
        return RegionFr.suggestions(debut, partie.getMode(), textes.getLangue()).stream().map(textes::nom).toList();
    }

    // comme dans la version web, une confirmation d'abord : un clic malheureux ne doit pas coûter la partie du jour
    private void abandonner() {
        if (!confirmer(textes.texte("abandon.question"), textes.texte("action.abandonner"))) {
            return;
        }
        partie.abandonner();
        retenirDuJour();
        terminer();
        resultatsEnFin();
    }

    // après chaque essai ou abandon de la partie du jour : l'historique est mis à jour et enregistré tout de suite,
    // pour qu'un plantage ou une fermeture brutale ne fasse rien perdre. Les parties libres ne sont pas enregistrées.
    private void retenirDuJour() {
        if (partie != duJour) {
            return;
        }
        historique = historique.avec(dateDuJour, duJour, modesDuJour);
        enregistrerHistorique();
    }

    // après chaque réponse au bonus, et quand on le passe : comme pour la partie, tout de suite. Fermer le jeu en plein
    // bonus ne fait donc rien perdre : au lancement suivant, il compte comme passé, avec les réponses déjà données
    private void retenirBonus() {
        // le bonus d'une partie libre n'est pas enregistré, comme la partie elle-même
        if (partie != duJour) {
            return;
        }
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

    // il n'y a plus rien à jouer (partie perdue ou abandonnée, bonus fini ou passé) : les résultats s'affichent
    // d'eux-mêmes, comme dans Wordle. invokeLater : d'abord la fin dessinée (carte, message), ensuite la boîte de
    // dialogue
    private void resultatsEnFin() {
        if (!partie.enCours()) {
            SwingUtilities.invokeLater(this::montrerResultats);
        }
    }

    // la fenêtre des résultats : les statistiques pour la partie du jour ; pour une partie libre (qui n'en a pas),
    // la fin de la partie et de quoi en commencer une autre, comme dans la version web
    private void montrerResultats() {
        avantResultats.stop();
        if (partie == duJour) {
            montrerStatistiques();
            return;
        }
        String rejouer = textes.texte("action.libre");
        String fermer = textes.texte("fermer");
        int choix = JOptionPane.showOptionDialog(frame, messageDeFin(), textes.texte("resultats"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{rejouer, fermer}, rejouer);
        if (choix == 0) {
            jouerLibre();
        }
    }

    private void montrerStatistiques() {
        LocalDate aujourdhui = LocalDate.now(horloge);
        Statistiques stats = Statistiques.de(historique.resultats(), aujourdhui);
        // la partie du jour finie : le compte à rebours jusqu'à la suivante, et un bouton pour partager le résultat
        boolean fini = duJourFini();
        // la barre de la victoire du jour est mise en valeur : celle de la partie du jour finie (d'hier, si minuit est
        // passé pendant son bonus), sinon celle d'aujourd'hui
        LocalDate jour = fini ? dateDuJour : aujourdhui;
        int essaisDuJour = historique.resultats().stream()
                .filter(resultat -> resultat.date().equals(jour) && resultat.victoire())
                .mapToInt(resultat -> resultat.propositions().size())
                .findFirst().orElse(0);
        JPanel panneau = FenetreStatistiques.panneau(stats, essaisDuJour, textes, fini ? this::avantLaSuivante : null);
        if (!fini) {
            JOptionPane.showMessageDialog(frame, panneau, textes.texte("action.statistiques"), JOptionPane.PLAIN_MESSAGE);
        } else {
            String partagerTexte = textes.texte("statistiques.partager");
            String fermer = textes.texte("fermer");
            int choix = JOptionPane.showOptionDialog(frame, panneau, textes.texte("action.statistiques"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{partagerTexte, fermer}, fermer);
            if (choix == 0) {
                partager();
            }
        }
        rattraperMinuit();
    }

    // minuit est passé pendant le bonus de la partie du jour (aMinuit l'a laissé finir), et ce bonus est maintenant
    // fini ou passé : une fois ses résultats vus, la fenêtre passe à la nouvelle région, comme elle l'aurait fait à
    // minuit. Pas en plein bonus (le bouton « Résultats » ne l'interrompt pas)
    private void rattraperMinuit() {
        boolean bonusFini = bonus == null || !bonus.enCours();
        if (partie == duJour && LocalDate.now(horloge).isAfter(dateDuJour) && bonusFini) {
            jouerDuJour();
        }
    }

    // le Timer se déclenchera une seconde après minuit (une seconde de marge : déclenché une milliseconde trop tôt, il
    // trouverait encore la veille)
    private void programmerMinuit() {
        long millisecondes = jusqueMinuit().toMillis() + 1000;
        minuit.setInitialDelay((int) Math.min(Integer.MAX_VALUE, millisecondes));
        minuit.restart();
    }

    // le jour a changé : si la partie du jour est à l'écran, on passe à la nouvelle. Pas en plein bonus (le joueur le
    // finit tranquillement), ni en partie libre : la nouvelle région l'attendra au prochain « Partie du jour »
    private void aMinuit() {
        if (partie == duJour && !dansLeBonus) {
            jouerDuJour();
        }
        programmerMinuit();
    }

    // la partie du jour est finie (gagnée, perdue ou abandonnée) : il y a un résultat à partager. Celle d'aujourd'hui,
    // ou celle d'hier tant qu'elle est à l'écran : minuit a pu passer pendant son bonus (aMinuit ne change pas de
    // partie en plein bonus), et le joueur qui vient de le finir doit pouvoir partager ce qu'il a joué. En partie
    // libre, après minuit, la partie d'hier n'est plus « du jour » : rien à partager avant d'avoir joué la nouvelle
    private boolean duJourFini() {
        return duJour != null && !duJour.enCours() && (partie == duJour || LocalDate.now(horloge).equals(dateDuJour));
    }

    // le temps qu'il reste jusqu'à minuit, l'heure de la région suivante, dans le fuseau de l'horloge (celui de
    // l'ordinateur) : les jours de changement d'heure, la journée ne dure pas 24 heures
    private Duration jusqueMinuit() {
        return RegionDuJour.jusquAMinuit(ZonedDateTime.now(horloge));
    }

    // le compte à rebours des statistiques : jusqu'à minuit, ou rien du tout si minuit est déjà passé pendant le bonus
    // de la partie affichée (la région suivante est là : « Prochaine région dans 00:00:00 »)
    private Duration avantLaSuivante() {
        return LocalDate.now(horloge).isAfter(dateDuJour) ? Duration.ZERO : jusqueMinuit();
    }

    // copie le résultat de la partie du jour dans le presse-papiers, prêt à être collé dans une messagerie. Le
    // presse-papiers est celui du système : une autre application peut le tenir un instant, d'où l'erreur possible
    private void partager() {
        if (!duJourFini()) {
            return;
        }
        String texte = Partage.texte(duJour, RegionDuJour.numero(dateDuJour),
                modesDuJour != null ? modesDuJour : ModesDifficiles.AUCUN, bonusDuJour, textes);
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texte), null);
            message.setText(textes.texte("partage.copie"));
        } catch (IllegalStateException e) {
            message.setText(textes.texte("partage.erreur"));
        }
    }

    private void terminer() {
        message.setText(messageDeFin());
        // tous les indices, maintenant que la partie est finie (après un abandon aussi)
        afficherIndices();
        activerSaisie(false);
        // la carte de France à la place de la silhouette : la réponse, et les régions proposées en couleur. La
        // silhouette reviendra avec la partie suivante (montrerPartie repasse par quitterBonus, qui montre l'image)
        carteFin.montrer(partie);
        cartes.show(cadre, FIN);
        boutonRejouer.requestFocusInWindow();
    }

    // le mode et la silhouette ne changent pas la partie en cours : ils comptent à la suivante (le mode, seulement
    // pour les parties libres : la partie du jour se joue toujours avec les nouvelles régions)
    private void prevenirProchainePartie() {
        if (partie.enCours()) {
            message.setText(textes.texte("prochainePartie"));
        }
    }

    // la commande « Liste » (menu Jeu, Ctrl+L) : la liste des départements tant que le bonus est affiché (même fini),
    // sauf pour la question du numéro en outre-mer ; celle des régions sinon. Elle regarde l'état du jeu au moment où
    // on l'appelle : une seule action pour les deux listes, sans rien à tenir à jour, à part son nom (nommerListe)
    private void montrerListe() {
        if (listeDuBonus()) {
            montrerListeDepartements();
        } else {
            montrerListeRegions();
        }
    }

    private boolean listeDuBonus() {
        return dansLeBonus && bonus.avecListe();
    }

    // le nom de la commande suit la liste qu'elle ouvre : appelé quand le bonus s'affiche ou s'en va, et à chaque
    // changement de langue. Changer le nom de l'Action renomme aussi l'article du menu, qui l'écoute
    private void nommerListe() {
        liste.putValue(Action.NAME, textes.texte(listeDuBonus() ? "action.listeDepartements" : "action.liste"));
    }

    // la liste des régions du mode joué, sur deux colonnes, comme dans la version web : un clic sur un nom le propose,
    // comme si on l'avait tapé ; les régions déjà proposées sont barrées et ne se cliquent plus. Pendant un bonus
    // d'outre-mer, la partie est gagnée : tout y est grisé
    private void montrerListeRegions() {
        JDialog fenetre = new JDialog(frame, textes.texte("action.liste"), true);
        Set<RegionFr> proposees = new HashSet<>(partie.getEssais().stream().map(Essai::proposition).toList());
        boolean finie = !partie.enCours();
        JPanel noms = new JPanel(new GridLayout(0, 2, 6, 0));
        for (RegionFr region : textes.regionsTriees(partie.getMode())) {
            boolean dejaProposee = proposees.contains(region);
            noms.add(nomDeListe(textes.nom(region), dejaProposee, !finie && !dejaProposee, () -> {
                // la liste se ferme d'abord : si l'essai termine la partie, la suite s'affiche dans la fenêtre du jeu
                fenetre.dispose();
                saisie.setText(textes.nom(region));
                jouer();
            }));
        }
        ouvrirListe(fenetre, textes.texte(finie ? "liste.finie" : "liste.aide"), noms);
    }

    // pendant le bonus (mode C), les départements que le bonus propose, « Finistère (29) », triés par nom : ceux de la
    // région dans la version simple, les 101 dans la difficile (Bonus.proposes). Un clic répond pour le département
    // choisi sur la carte, comme si on avait écrit son nom ; tant qu'il n'y en a pas, tout est grisé. Ceux qui ont déjà
    // leur réponse sont barrés
    private void montrerListeDepartements() {
        JDialog fenetre = new JDialog(frame, textes.texte("action.listeDepartements"), true);
        Set<Departement> repondus = new HashSet<>(bonus.repondus());
        boolean fini = !bonus.enCours();
        boolean choisi = carte.getChoisi().isPresent();
        JPanel noms = new JPanel(new GridLayout(0, 2, 6, 0));
        for (Departement departement : textes.departementsTries(bonus.proposes())) {
            boolean dejaRepondu = repondus.contains(departement);
            JButton nom = nomDeListe(departement.nomEtNumero(), dejaRepondu, !fini && choisi && !dejaRepondu, () -> {
                // la liste se ferme d'abord : si c'était la dernière réponse, les résultats s'ouvriront ensuite. Le
                // focus revient à la fenêtre du jeu, sur la carte, pour choisir le département suivant
                fenetre.dispose();
                carte.getChoisi().ifPresent(choix -> repondre(bonus.repondre(choix, departement)));
            });
            // un lecteur d'écran ne lit pas le trait du nom barré : il le dit avec des mots
            if (dejaRepondu) {
                nom.getAccessibleContext().setAccessibleName(
                        departement.nomEtNumero() + ", " + textes.texte("liste.departements.dejaRepondu"));
            }
            noms.add(nom);
        }
        String aide = fini ? "liste.departements.finie" : choisi ? "liste.departements.aide" : "liste.departements.sansChoix";
        // les 101 (version difficile), c'est 51 lignes : bien plus que la hauteur d'un écran portable (672 points
        // utilisables). Elles défilent dans une zone de 420 points au plus, comme l'aide, avec 30 points pour la barre
        // de défilement. Ceux d'une région (13 au plus, en Occitanie) tiennent sans défiler : la fenêtre se fait petite
        JScrollPane defilement = new JScrollPane(noms);
        defilement.setBorder(BorderFactory.createEmptyBorder());
        Dimension taille = noms.getPreferredSize();
        boolean defile = taille.height > HAUTEUR_LISTE;
        defilement.setPreferredSize(new Dimension(taille.width + (defile ? 30 : 0), Math.min(taille.height, HAUTEUR_LISTE)));
        // la molette : une ligne à la fois (16 points), et non un seul point par cran
        defilement.getVerticalScrollBar().setUnitIncrement(16);
        ouvrirListe(fenetre, textes.texte(aide), defilement);
    }

    private static final int HAUTEUR_LISTE = 420;

    // un nom de liste : un vrai bouton, qui se choisit aussi au clavier (Tab, Espace), barré au besoin
    private static JButton nomDeListe(String texte, boolean barre, boolean actif, Runnable clic) {
        // <s> : le nom barré. Swing sait afficher un peu de HTML dans le texte d'un bouton ou d'une étiquette
        JButton nom = new JButton(barre ? "<html><s>" + texte + "</s></html>" : texte);
        nom.setHorizontalAlignment(SwingConstants.LEFT);
        // un bouton sans bordure de FlatLaf : une liste de noms plutôt qu'un mur de boutons
        nom.putClientProperty("JButton.buttonType", "borderless");
        nom.setEnabled(actif);
        nom.addActionListener(e -> clic.run());
        return nom;
    }

    // la fenêtre d'une liste : une phrase d'aide, puis les noms. Modale : setVisible ne rend la main qu'une fois la
    // liste fermée (un clic sur un nom, Échap, ou la croix)
    private void ouvrirListe(JDialog fenetre, String aide, JComponent noms) {
        JLabel phrase = new JLabel(aide);
        phrase.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JPanel contenu = new JPanel(new BorderLayout());
        contenu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenu.add(phrase, BorderLayout.NORTH);
        contenu.add(noms, BorderLayout.CENTER);
        fenetre.setContentPane(contenu);
        // Échap ferme la liste, comme une boîte de dialogue ordinaire
        fenetre.getRootPane().registerKeyboardAction(e -> fenetre.dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        fenetre.pack();
        fenetre.setLocationRelativeTo(frame);
        fenetre.setVisible(true);
    }

    // l'aide dans une zone à défilement de taille fixe : le texte, crédits compris, dépasserait sinon la hauteur d'un
    // écran portable (672 points utilisables)
    private void montrerAide() {
        JTextArea texte = new JTextArea(textes.aide());
        texte.setEditable(false);
        // une ligne trop longue (l'exemple, en anglais surtout) passe à la ligne, entre deux mots
        texte.setLineWrap(true);
        texte.setWrapStyleWord(true);
        texte.setCaretPosition(0);       // le début du texte, pas la fin
        JScrollPane defilement = new JScrollPane(texte);
        defilement.setPreferredSize(new Dimension(600, 420));
        JOptionPane.showMessageDialog(frame, defilement, textes.texte("action.aide"), JOptionPane.INFORMATION_MESSAGE);
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
            afficherBonus();
        } else {
            message.setText(partie.enCours() ? textes.apresEssai(partie) : messageDeFin());
        }
        afficherIndices();
    }

    // tous les textes qui ne dépendent pas de la partie : noms des actions, menus, bouton, titres du tableau
    private void appliquerLangue() {
        textes = new Textes(reglages.langue());
        nomsDesActions.forEach((action, cle) -> action.putValue(Action.NAME, textes.texte(cle)));
        // (sauf la liste, dont le nom dépend aussi du bonus)
        nommerListe();
        frame.setJMenuBar(menus());
        frame.getRootPane().revalidate();
        valider.setText(textes.texte("valider"));
        // le bouton du bonus a un nom court, à lui : celui de l'action (« Bonus des départements ») élargirait la fenêtre
        boutonBonus.setHideActionText(true);
        boutonBonus.setText(textes.texte("bouton.bonus"));
        boutonResultats.setHideActionText(true);
        boutonResultats.setText(textes.texte("resultats"));
        // le cadre titré, et une marge à l'intérieur : sans elle, le texte touchait presque le trait du cadre
        indices.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(textes.texte("indices")),
                BorderFactory.createEmptyBorder(0, 8, 2, 8)));
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

    // le bonus existe dès que la partie affichée est gagnée, partie du jour ou partie libre (il se revoit une fois
    // fini, depuis le menu Jeu)
    private boolean bonusGagne() {
        return partie != null && partie.getEtat() == Partie.Etat.GAGNEE;
    }

    // il reste un bonus à jouer : pas commencé, ou en cours
    private boolean bonusAJouer() {
        return bonusGagne() && (bonus == null || bonus.enCours());
    }

    // montre le bonus de la partie affichée dans l'état où il est : nouveau, en cours, ou fini (pour revoir la carte).
    // Il commence de lui-même dès chaque victoire, partie du jour ou partie libre ; sinon, par le bouton « Bonus »
    // (passé avant toute réponse) ou le menu Jeu
    private void jouerBonus() {
        // déjà dans le bonus (Ctrl+B en plein bonus) : rien à faire, et surtout pas le quitter, ce qui le passerait
        if (dansLeBonus || !bonusGagne()) {
            return;
        }
        if (bonus == null) {
            // la partie du jour : le même ordre pour tous, retrouvé après une relance ; une partie libre : au hasard.
            // Dans la version des réglages
            boolean difficile = reglages.isDepartementsDifficiles();
            if (partie == duJour) {
                bonus = Bonus.duJour(duJour.getCible(), dateDuJour, difficile);
                bonusDuJour = bonus;
            } else {
                bonus = Bonus.auHasard(partie.getCible(), HASARD, difficile);
            }
        }
        // un bonus passé avant toute réponse, puis repris : la version a pu changer entre-temps
        appliquerVersion();
        dansLeBonus = true;
        nommerListe();
        carte.montrer(bonus);
        cartes.show(cadre, DEPARTEMENTS);
        saisie.setText("");
        // (afficherBonus donne aussi le focus du clavier à la carte : la saisie de la région, ou le bouton « Bonus »,
        // vient d'être grisée ou cachée, et le focus y resterait sinon, Tab repartant d'on ne sait où)
        afficherBonus();
    }

    // la version du réglage (simple ou difficile) vaut pour le bonus de la partie affichée tant qu'il n'a aucune
    // réponse ; à la première, elle est fixée (Bonus.changerVersion), et un changement attend le bonus suivant. Rien à
    // redessiner : les suggestions et la liste demandent au bonus ce qu'il propose à chaque fois
    private void appliquerVersion() {
        if (bonus != null && bonus.enCours() && bonus.getTentatives().isEmpty()) {
            bonus.changerVersion(reglages.isDepartementsDifficiles());
        }
    }

    // un clic sur un département de la carte, ou Entrée au clavier (la carte ne le transmet que quand on peut le
    // choisir). Mode C : le département est choisi, il reste à le nommer, et le joueur peut encore changer d'avis ;
    // mode A : c'est la réponse
    private void cliquer(Departement departement) {
        if (bonus.getQuestion() == Bonus.Question.CHOIX) {
            carte.choisir(departement);
            afficherBonus();
        } else {
            repondre(bonus.repondre(departement));
        }
    }

    // vrai si la réponse a compté (juste ou fausse), faux si elle a été refusée
    private boolean repondre(ReponseBonus reponse) {
        return switch (reponse) {
            case ReponseBonus.Inconnue() -> refuser(textes.bonusInconnu(bonus, saisie.getText()));
            case ReponseBonus.Plusieurs() -> refuser(textes.texte("bonus.plusieurs", saisie.getText().strip()));
            case ReponseBonus.DejaTrouve(Departement departement) ->
                    refuser(textes.texte("bonus.dejaTrouve", departement.nom()));
            case ReponseBonus.HorsRegion(Departement departement) ->
                    refuser(textes.texte("bonus.horsRegion", departement.nom()));
            case ReponseBonus.Jouee(Tentative tentative) -> {
                retenirBonus();
                saisie.setText("");
                // mode CARTE : le département cliqué à tort est entouré de rouge ; dans les autres modes, la bonne
                // réponse est écrite dans le message. Plus de département choisi : le joueur clique le suivant
                Bonus.Question question = bonus.getQuestion();
                carte.montrerReponse(question == Bonus.Question.CARTE && !tentative.juste() ? tentative.donne() : null);
                afficherBonus();
                // mode C : la réponse (« Bien vu », « Raté : c'était… ») reste affichée jusqu'au choix suivant. Les
                // modes A et B, eux, passent tout de suite à la question suivante (seules les couleurs le disent)
                if (bonus.enCours() && question == Bonus.Question.CHOIX) {
                    message.setText(textes.reponseBonus(bonus, tentative));
                }
                if (!bonus.enCours()) {
                    avantResultats.restart();
                }
                yield true;
            }
        };
    }

    // le bouton « Passer » : le bonus s'arrête là, et les résultats s'ouvrent. S'il n'a pas commencé, on revient à la
    // fin de la partie, et il reste à jouer (bouton « Bonus »)
    private void passerBonus() {
        if (bonus.getTentatives().isEmpty()) {
            quitterBonus();
            montrerPartie(partie);
        } else {
            bonus.passer();
            retenirBonus();
            carte.montrerReponse(null);
            afficherBonus();
        }
        resultatsEnFin();
    }

    // le joueur va voir ailleurs (partie libre, partie du jour…) en plein bonus : un bonus commencé compte comme
    // passé, avec les réponses déjà données
    private void quitterBonus() {
        // des résultats en attente seraient ceux de la partie qu'on quitte
        avantResultats.stop();
        if (dansLeBonus && bonus.enCours() && !bonus.getTentatives().isEmpty()) {
            bonus.passer();
            // (rien de nouveau à enregistrer : les réponses le sont déjà, et un bonus relu est toujours fini)
        }
        dansLeBonus = false;
        nommerListe();
        saisie.putClientProperty(TEXTE_FANTOME, null);
        cartes.show(cadre, IMAGE);
    }

    // le texte grisé d'un champ vide (« placeholder »), que FlatLaf dessine à la place du texte
    private static final String TEXTE_FANTOME = "JTextField.placeholderText";

    // les boutons et le message du bonus, d'après son état
    private void afficherBonus() {
        boolean enCours = bonus.enCours();
        Bonus.Question question = bonus.getQuestion();
        boolean choisi = carte.getChoisi().isPresent();
        // en mode CARTE, on répond en cliquant : la saisie ne sert pas. En mode CHOIX, elle attend le choix d'un
        // département : grisée, avec ce qu'il faut faire d'abord
        boolean saisieVisible = enCours && question != Bonus.Question.CARTE;
        boolean ecrire = saisieVisible && (question != Bonus.Question.CHOIX || choisi);
        montrerChamp(saisieVisible);
        saisie.setEnabled(ecrire);
        saisie.putClientProperty(TEXTE_FANTOME,
                saisieVisible && !ecrire ? textes.texte("bonus.choixDabord") : null);
        valider.setEnabled(ecrire);
        valider.setVisible(saisieVisible);
        abandonner.setEnabled(false);
        boutonAbandonner.setVisible(false);
        passer.setEnabled(enCours);
        boutonPasser.setVisible(enCours);
        // sous le bonus, comme dans la version web : « Résultats » pour la partie du jour, « Partie libre » pour une
        // partie libre (qui demande d'abord, si le bonus a déjà une réponse : rien n'en est compté, mais tout serait perdu)
        boolean duJourAffiche = partie == duJour;
        boutonResultats.setVisible(duJourAffiche);
        boutonRejouer.setVisible(!enCours || !duJourAffiche);
        boutonBonus.setVisible(false);
        actionBonus.setEnabled(true);
        partager.setEnabled(duJourFini());
        message.setText(enCours ? textes.questionBonus(bonus, choisi) : textes.finBonus(bonus));
        frame.getRootPane().setDefaultButton(ecrire ? valider : enCours ? null : boutonRejouer);
        // le focus du clavier là où est la suite : la saisie s'il faut écrire, sinon la carte pour choisir (ou
        // cliquer) un département, et à la fin, le bouton de la partie suivante
        if (ecrire) {
            saisie.requestFocusInWindow();
        } else if (enCours) {
            carte.requestFocusInWindow();
        } else {
            boutonRejouer.requestFocusInWindow();
        }
    }

    // le message de fin de partie ; pour la partie du jour dont le bonus est fini, suivi du score du bonus
    private String messageDeFin() {
        String fin = textes.fin(partie);
        if (bonus != null && !bonus.enCours()) {
            return fin + " " + textes.resumeBonus(bonus);
        }
        return fin;
    }

    // ---- petits utilitaires d'affichage ----

    // une question Oui / Non ; vrai si le joueur répond Oui. Nos propres boutons plutôt que YES_NO_OPTION : les
    // boutons de JOptionPane prennent la langue du système, pas celle choisie dans le jeu. « Non » est le bouton
    // par défaut : Entrée par réflexe ne fait rien de grave
    private boolean confirmer(String question, String titre) {
        String oui = textes.texte("oui");
        String non = textes.texte("non");
        int choix = JOptionPane.showOptionDialog(frame, question, titre, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, new Object[]{oui, non}, non);
        return choix == 0;
    }

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
        boutonResultats.setVisible(false);
        // à la fin d'une partie gagnée, tant que le bonus n'est pas fini
        boutonBonus.setVisible(!active && bonusAJouer());
        actionBonus.setEnabled(bonusGagne());
        partager.setEnabled(duJourFini());
        // AVANT le bouton par défaut : montrerChamp retire les boutons de la fenêtre puis les remet, et un bouton
        // retiré cesse d'être le bouton par défaut (JButton.removeNotify)
        montrerChamp(active);
        // la touche Entrée déclenche « Valider » pendant la partie, « Nouvelle partie » à la fin
        frame.getRootPane().setDefaultButton(active ? valider : boutonRejouer);
    }

    // le champ de saisie, ou rien : à la fin de la partie (ou du bonus), un champ vide et grisé ne dit rien d'utile.
    // Il disparaît, et les boutons qui restent (Partie libre, Bonus…) se rangent au milieu de la ligne. La ligne garde
    // sa hauteur (celle des boutons, la même que celle du champ) : la fenêtre ne bouge pas
    private void montrerChamp(boolean avecChamp) {
        if ((saisie.getParent() == ligneSaisie) == avecChamp) {
            return;
        }
        ((FlowLayout) boutons.getLayout()).setAlignment(avecChamp ? FlowLayout.RIGHT : FlowLayout.CENTER);
        // la ligne est refaite en entier. Un BorderLayout ne retient qu'UN composant par place : y mettre les boutons au
        // centre lui ferait oublier le champ, qui ne serait plus jamais placé, même une fois les boutons repartis à
        // droite. Sans champ, celui-ci n'est donc plus du tout dans la ligne (et l'autocomplétion, qui ne trouve plus
        // de fenêtre au-dessus de lui, ne s'ouvre pas)
        ligneSaisie.removeAll();
        if (avecChamp) {
            ligneSaisie.add(saisie, BorderLayout.CENTER);
            ligneSaisie.add(boutons, BorderLayout.EAST);
        } else {
            ligneSaisie.add(boutons, BorderLayout.CENTER);
        }
        // le conteneur a changé : il refait sa disposition (revalidate), puis se redessine
        ligneSaisie.revalidate();
        ligneSaisie.repaint();
    }

    private void montrer(String chemin) {
        montrer(chemin, 0);
    }

    private void montrer(String chemin, int degres) {
        Icon icone = Representation.icone(chemin, LARGEUR_IMAGE, HAUTEUR_IMAGE, degres);
        image.setIcon(icone);
    }

    // la silhouette de la partie en cours (ou la France et un point d'interrogation, en mode caché), tournée ou non
    private void montrerSilhouette() {
        montrer(Representation.silhouette(partie.getCible(), !modesAffiches().cachee()), angleSilhouette());
    }

    // les modes que donneraient les réglages actuels
    private ModesDifficiles modesChoisis() {
        return new ModesDifficiles(reglages.isSilhouetteTournee(), !reglages.isAvecImages());
    }

    // les modes de la partie : fixés si elle a commencé, sinon ceux des réglages (qui comptent donc tout de suite)
    private ModesDifficiles modesAffiches() {
        return modes != null ? modes : modesChoisis();
    }

    // la silhouette cachée ou tournée a été changée dans les Paramètres. Avant le premier essai, cela compte tout de
    // suite ; ensuite, pour la prochaine partie seulement (les modes sont fixés)
    private void silhouetteChangee() {
        if (partie.enCours() && modes == null && !dansLeBonus) {
            montrerSilhouette();
        } else {
            prevenirProchainePartie();
        }
    }

    // de combien tourner la silhouette : 0 hors du mode tourné (ou s'il n'y a pas de silhouette) ; sinon l'angle de
    // la partie du jour (le même pour tous) ou celui tiré pour la partie libre
    private int angleSilhouette() {
        if (!modesAffiches().tournee()) {
            return 0;
        }
        return partie == duJour ? RegionDuJour.angle(dateDuJour) : angleLibre;
    }

    // chaque indice sur sa ligne : révélé, sa valeur en gras ; à venir, grisé, avec l'essai après lequel il viendra.
    // Le gras passe par un peu de HTML ; la valeur est échappée, comme dans le message (Message.echapper). Une ligne
    // grisée reste en texte simple : Swing ne grise pas le HTML d'une étiquette désactivée
    private void afficherIndices() {
        List<Indice> reveles = partie.indicesReveles();
        Indice[] tous = Indice.values();
        for (int i = 0; i < tous.length; i++) {
            Indice indice = tous[i];
            JLabel ligne = lignesIndices[i];
            boolean revele = reveles.contains(indice);
            if (revele) {
                String valeur = textes.valeurIndice(indice, partie.getCible(), reglages.unitesAnglaises());
                ligne.setText("<html>" + textes.ligneIndice(indice, "<b>" + Message.echapper(valeur) + "</b>") + "</html>");
            } else {
                ligne.setText(textes.ligneIndice(indice, textes.indiceCache(indice)));
            }
            // grisée (la couleur « désactivée » du thème) tant qu'il n'est pas révélé
            ligne.setEnabled(revele);
        }
    }
}
