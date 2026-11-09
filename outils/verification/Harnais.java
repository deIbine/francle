package fr.francle.ui;

import fr.francle.donnees.*;
import fr.francle.jeu.*;
import fr.francle.stats.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.accessibility.*;
import javax.imageio.ImageIO;
import javax.swing.*;

// harnais de vérification (passe du 9 novembre 2026) : pilote la vraie fenêtre. Hors de src : Maven ne le compile
// pas ; voir outils/verification/LISEZMOI.md
public class Harnais {
    static String IMG;
    static Fenetre f;
    static final List<String> dialogues = Collections.synchronizedList(new ArrayList<>());

    static Object champ(String nom) throws Exception {
        Field c = Fenetre.class.getDeclaredField(nom); c.setAccessible(true); return c.get(f);
    }
    static Object appel(String nom, Object... args) throws Exception {
        for (Method m : Fenetre.class.getDeclaredMethods()) {
            if (m.getName().equals(nom) && m.getParameterCount() == args.length) {
                m.setAccessible(true); return m.invoke(f, args);
            }
        }
        throw new NoSuchMethodException(nom);
    }
    static void edt(ThrowingRunnable r) throws Exception {
        Exception[] e = new Exception[1];
        SwingUtilities.invokeAndWait(() -> { try { r.run(); } catch (Exception x) { e[0] = x; } });
        if (e[0] != null) throw e[0];
    }
    interface ThrowingRunnable { void run() throws Exception; }

    static String textes(Component c) {
        StringBuilder sb = new StringBuilder();
        if (c instanceof JLabel l && l.getText() != null) sb.append(l.getText()).append(" | ");
        if (c instanceof AbstractButton b && b.getText() != null) sb.append("[").append(b.getText()).append("] ");
        if (c instanceof Container k) for (Component e : k.getComponents()) sb.append(textes(e));
        return sb.toString();
    }

    // ferme toute boîte de dialogue qui s'ouvre, en notant son titre et son contenu
    static void fermeur() {
        javax.swing.Timer t = new javax.swing.Timer(150, e -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog d && d.isVisible()) {
                    String nom = d.getTitle();
                    if (nom != null && nom.startsWith("GARDER")) continue;
                    capture(d, "dialogue-" + dialogues.size());
                    dialogues.add("« " + d.getTitle() + " » redimensionnable=" + d.isResizable() + " taille=" + d.getSize().width + "x" + d.getSize().height
                            + " : " + textes(d.getContentPane()).replaceAll("<[^>]+>", "").replaceAll("\\s+", " "));
                    d.dispose();
                }
            }
        });
        t.start();
    }

    static void capture(Component c, String nom) {
        try {
            BufferedImage img = new BufferedImage(Math.max(1, c.getWidth()), Math.max(1, c.getHeight()), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            c.paint(g); g.dispose();
            ImageIO.write(img, "png", new File(IMG, nom + ".png"));
        } catch (Exception e) { System.out.println("capture " + nom + " : " + e); }
    }

    static JFrame frame() throws Exception { return (JFrame) champ("frame"); }
    static String message() throws Exception { return ((JLabel) champ("message")).getText().replaceAll("<[^>]+>", ""); }

    static void nouvelle(boolean sombre, Langue langue) throws Exception {
        Path dir = Files.createTempDirectory("francle-harnais");
        Reglages r = new Reglages();
        r.setAideVue(true);
        r.setThemeSombre(sombre);
        r.setLangue(langue);
        Sauvegarde<Reglages> sr = Reglages.sauvegarde(dir.resolve("reglages.json"));
        sr.enregistrer(r);
        Clock horloge = Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneId.of("Europe/Paris"));
        edt(() -> {
            if (f != null) frame().dispose();
            Theme.installer(sombre);
            f = new Fenetre(horloge, Sauvegarde.historique(dir.resolve("historique.json")), sr);
            f.afficher();
        });
        Thread.sleep(600);
    }

    public static void main(String[] a) throws Exception {
        IMG = a[0];
        SwingUtilities.invokeAndWait(Harnais::fermeur);

        // ---------- A. début de partie : disposition, espace entre saisie et tableau, fuites ----------
        nouvelle(false, Langue.FR);
        edt(() -> {
            JFrame fr = frame();
            System.out.println("A. fenêtre packée " + fr.getSize() + " redimensionnable=" + fr.isResizable()
                    + " minimumSizeSet=" + fr.isMinimumSizeSet() + " getMinimumSize=" + fr.getMinimumSize());
            JComponent ligne = (JComponent) champ("ligneSaisie");
            JComponent msg = (JComponent) champ("message");
            JTable table = (JTable) champ("table");
            JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
            Rectangle rl = SwingUtilities.convertRectangle(ligne.getParent(), ligne.getBounds(), fr.getContentPane());
            Rectangle rm = SwingUtilities.convertRectangle(msg.getParent(), msg.getBounds(), fr.getContentPane());
            Rectangle rt = SwingUtilities.convertRectangle(sp.getParent(), sp.getBounds(), fr.getContentPane());
            System.out.println("A. ligne de saisie " + rl + "\n   message " + rm + " « " + message() + " »\n   tableau " + rt);
            System.out.println("A. écart saisie→tableau : " + (rt.y - (rl.y + rl.height)) + " pt, dont message " + rm.height + " pt");
            capture(fr.getRootPane(), "A-debut");
            fuites("A. début");
        });

        // trois essais ratés, puis fuites encore
        edt(() -> {
            Partie p = (Partie) champ("partie");
            System.out.println("   (région du jour, pour le harnais : " + p.getCible() + ")");
            JTextField s = (JTextField) champ("saisie");
            int n = 0;
            for (RegionFr r : RegionFr.values()) {
                if (n == 3) break;
                if (r != p.getCible() && RegionFr.chercher(r.getNom(), p.getMode()).isPresent()) {
                    s.setText(r.getNom()); appel("jouer"); n++;
                }
            }
            capture(frame().getRootPane(), "A-trois-essais");
            fuites("A. après 3 essais");
            // message vide ? (pour le point 7)
            System.out.println("A. message après 3 essais : « " + message() + " »");
        });

        // ---------- B. victoire, une réponse au bonus, puis « Passer » ----------
        edt(() -> {
            Partie p = (Partie) champ("partie");
            JTextField s = (JTextField) champ("saisie");
            s.setText(p.getCible().getNom()); appel("jouer");
            Bonus b = (Bonus) champ("bonus");
            System.out.println("B. victoire. bonus : question=" + b.getQuestion() + " total=" + b.total() + " message « " + message() + " »");
            capture(frame().getRootPane(), "B-bonus");
        });
        Thread.sleep(400);
        int avant = dialogues.size();
        edt(() -> {
            Bonus b = (Bonus) champ("bonus");
            CarteDepartements carte = (CarteDepartements) champ("carte");
            Departement d = b.proposes().get(0);
            appel("cliquer", d);
            ((JTextField) champ("saisie")).setText(d.nom());
            appel("jouer");
            System.out.println("B. une réponse : score " + b.score() + "/" + b.total() + " état " + b.getEtat());
            appel("passerBonus");
            System.out.println("B. après « Passer » : état " + b.getEtat() + " score " + b.score() + "/" + b.total()
                    + " ; message « " + message() + " »");
        });
        Thread.sleep(800);
        System.out.println("B. boîtes ouvertes par « Passer » : " + dialogues.subList(avant, dialogues.size()));
        edt(() -> {
            Historique h = (Historique) champ("historique");
            Statistiques st = Statistiques.de(h.resultats(), LocalDate.of(2026, 11, 9));
            System.out.println("B. statistiques : bonus joués " + st.bonusJoues() + ", score moyen " + st.scoreMoyenBonus() + " %");
            // revenir au bonus : peut-on le reprendre ?
            appel("jouerBonus");
            Bonus b = (Bonus) champ("bonus");
            System.out.println("B. « Bonus » après « Passer » : enCours=" + b.enCours() + " ; message « " + message() + " »");
            capture(frame().getRootPane(), "B-bonus-passe");
            appel("montrerCarteDeFin");
        });
        Thread.sleep(500);

        // ---------- C. le message du partage ----------
        edt(() -> {
            System.out.println("C. avant partage : « " + message() + " »");
            appel("partager");
            System.out.println("C. juste après le 1er clic : « " + message() + " »");
        });
        Thread.sleep(4000);
        edt(() -> {
            System.out.println("C. 4 s plus tard : « " + message() + " »");
            capture(frame().getRootPane(), "C-apres-partage");
            appel("partager");
            System.out.println("C. 2e clic : « " + message() + " »");
            javax.swing.Timer[] minuteries = Arrays.stream(Fenetre.class.getDeclaredFields())
                    .filter(c -> c.getType() == javax.swing.Timer.class).map(c -> { try { c.setAccessible(true); return (javax.swing.Timer) c.get(f); } catch (Exception e) { return null; } })
                    .toArray(javax.swing.Timer[]::new);
            System.out.println("C. minuteries de la fenêtre : " + minuteries.length + " (avantResultats, minuit)");
        });
        Thread.sleep(8000);
        edt(() -> System.out.println("C. 12 s après le 1er clic : « " + message() + " »"));

        // ---------- D. l'abandon demande-t-il ? ----------
        nouvelle(false, Langue.FR);
        avantD = dialogues.size();
        edt(() -> appel("abandonner"));
        Thread.sleep(400);
        edt(() -> System.out.println("D. « Abandonner » : boîtes " + dialogues.subList(avant(), dialogues.size()) + " ; partie " + ((Partie) champ("partie")).getEtat()));

        // ---------- E. fin de partie : messages longs, à la taille packée et plus petite ----------
        String[][] cas = {{"FR", "PROVENCE_ALPES_COTE_D_AZUR"}, {"EN", "PROVENCE_ALPES_COTE_D_AZUR"}, {"FR", "BOURGOGNE_FRANCHE_COMTE"}, {"EN", "BOURGOGNE_FRANCHE_COMTE"}};
        for (String[] c : cas) {
            nouvelle(false, Langue.valueOf(c[0]));
            RegionFr cible = Arrays.stream(RegionFr.values()).filter(r -> r.name().startsWith(c[1].substring(0, 8))).findFirst().get();
            edt(() -> {
                Partie p = new Partie(cible, ModeRegions.NOUVELLES);
                appel("montrerPartie", p);
                JTextField s = (JTextField) champ("saisie");
                int n = 0;
                for (RegionFr r : RegionFr.values()) {
                    if (n == 6) break;
                    if (r != cible && RegionFr.chercher(r.getNom(), ModeRegions.NOUVELLES).isPresent()) { s.setText(r.getNom()); appel("jouer"); n++; }
                }
            });
            Thread.sleep(500);
            for (int largeur : new int[]{0, 380, 300}) {
                edt(() -> {
                    JFrame fr = frame();
                    if (largeur > 0) { fr.setSize(largeur, fr.getHeight()); }
                    fr.validate(); fr.getRootPane().validate();
                });
                edt(() -> {
                    JFrame fr = frame();
                    fr.validate();
                    Message m = (Message) champ("message");
                    int ligneH = m.getFontMetrics(m.getFont()).getHeight();
                    JPanel boutons = (JPanel) champ("boutons");
                    System.out.println("E. " + c[0] + " " + cible + " largeur " + fr.getWidth() + " : message " + m.getWidth() + "x" + m.getHeight()
                            + " (préféré " + m.getPreferredSize().height + ", ≈" + Math.round((double) m.getPreferredSize().height / ligneH) + " ligne(s)) « " + message() + " »"
                            + "\n   boutons de fin : place " + boutons.getWidth() + " pt, besoin " + boutons.getPreferredSize().width + " pt" + (boutons.getWidth() < boutons.getPreferredSize().width ? "  → COUPÉS" : ""));
                    capture(fr.getRootPane(), "E-" + c[0] + "-" + cible.name().substring(0, 8) + "-" + fr.getWidth());
                });
            }
        }

        // ---------- F. compte à rebours des statistiques ----------
        for (Langue l : Langue.values()) {
            nouvelle(false, l);
            edt(() -> {
                Partie p = (Partie) champ("partie");
                // une défaite rapide : 6 essais faux
                JTextField s = (JTextField) champ("saisie");
                int n = 0;
                for (RegionFr r : RegionFr.values()) {
                    if (n == 6) break;
                    if (r != p.getCible() && RegionFr.chercher(r.getNom(), p.getMode()).isPresent()) { s.setText(r.getNom()); appel("jouer"); n++; }
                }
            });
            Thread.sleep(800);
            edt(() -> {
                Textes t = (Textes) champ("textes");
                JPanel panneau = FenetreStatistiques.panneau(Statistiques.de(((Historique) champ("historique")).resultats(), LocalDate.of(2026, 11, 9)), 0, t, () -> Duration.ofHours(23).plusMinutes(59).plusSeconds(59));
                JOptionPane op = new JOptionPane(panneau, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{t.texte("statistiques.partager"), t.texte("fermer")});
                JDialog d = op.createDialog(frame(), "GARDER " + l);
                d.pack();
                System.out.println("F. " + l + " statistiques : redimensionnable=" + d.isResizable() + " taille " + d.getSize());
                for (Component c : tous(panneau)) {
                    if (c instanceof JLabel lab && lab.getText() != null && !lab.getText().isBlank()) {
                        boolean coupe = lab.getWidth() < lab.getPreferredSize().width;
                        if (coupe || lab.getText().contains(":")) System.out.println("   « " + lab.getText() + " » largeur " + lab.getWidth() + " / préférée " + lab.getPreferredSize().width + (coupe ? "  → COUPÉ" : ""));
                    }
                }
                capture(d.getRootPane(), "F-stats-" + l);
                d.dispose();
            });
        }

        // ---------- G. l'unité de l'exemple d'aide ----------
        nouvelle(false, Langue.EN);
        edt(() -> {
            Textes t = (Textes) champ("textes");
            Reglages r = (Reglages) champ("reglages");
            Partie p = (Partie) champ("partie");
            JTextField s = (JTextField) champ("saisie");
            RegionFr autre = p.getCible() == RegionFr.CORSE ? RegionFr.BRETAGNE : RegionFr.CORSE;
            s.setText(autre.getNom()); appel("jouer");
            JTable table = (JTable) champ("table");
            System.out.println("G. EN, unités par défaut : unitesAnglaises=" + r.unitesAnglaises());
            System.out.println("   tableau : « " + table.getValueAt(0, 2) + " » ; exemple de l'aide : « " + t.exemple() + " »");
            List<Indice> rev = p.indicesReveles();
            System.out.println("   indice superficie (affiché) : « " + t.valeurIndice(Indice.SUPERFICIE, p.getCible(), r.unitesAnglaises()) + " »");
        });

        // ---------- H. thème sombre : capture ----------
        nouvelle(true, Langue.FR);
        edt(() -> { capture(frame().getRootPane(), "H-sombre-debut"); });

        System.out.println("\nToutes les boîtes vues : ");
        dialogues.forEach(d -> System.out.println("  " + d));
        System.exit(0);
    }
    static int avantD;
    static int avant() { return avantD; }

    static List<Component> tous(Component c) {
        List<Component> l = new ArrayList<>(); l.add(c);
        if (c instanceof Container k) for (Component e : k.getComponents()) l.addAll(tous(e));
        return l;
    }

    // tout ce que la fenêtre donne à lire (textes, bulles, noms accessibles, titre) contient-il la réponse ?
    static void fuites(String quand) throws Exception {
        Partie p = (Partie) champ("partie");
        RegionFr cible = p.getCible();
        Set<String> secrets = new LinkedHashSet<>();
        for (Langue l : Langue.values()) secrets.add(cible.getNom(l).toLowerCase());
        secrets.add(cible.getImage().toLowerCase());
        secrets.add(cible.getCapitale().toLowerCase());
        secrets.add(cible.name().toLowerCase());
        List<String> lus = new ArrayList<>();
        JFrame fr = frame();
        lus.add("titre=" + fr.getTitle());
        for (Component c : tous(fr)) {
            if (c instanceof JComponent j) {
                if (j.getToolTipText() != null) lus.add("bulle " + j.getClass().getSimpleName() + "=" + j.getToolTipText());
                if (j instanceof JLabel lab) {
                    lus.add("texte=" + lab.getText());
                    if (lab.getIcon() != null) lus.add("icône " + lab.getIcon().getClass().getSimpleName() + " " + lab.getIcon());
                }
            }
            AccessibleContext ac = c.getAccessibleContext();
            if (ac != null) {
                if (ac.getAccessibleName() != null) lus.add("nomAcc " + c.getClass().getSimpleName() + "=" + ac.getAccessibleName());
                if (ac.getAccessibleDescription() != null) lus.add("descAcc " + c.getClass().getSimpleName() + "=" + ac.getAccessibleDescription());
                if (ac.getAccessibleIcon() != null) for (AccessibleIcon ai : ac.getAccessibleIcon()) lus.add("iconeAcc=" + ai.getAccessibleIconDescription());
            }
        }
        // la carte de fin, cachée sous la silhouette : sa bulle répondrait-elle ?
        CarteFin cf = (CarteFin) champ("carteFin");
        lus.add("carteFin visible=" + cf.isShowing());
        List<String> trouves = lus.stream().filter(x -> x != null && secrets.stream().anyMatch(sx -> x.toLowerCase().contains(sx))).toList();
        System.out.println(quand + " : " + lus.size() + " textes lus, contenant la réponse : " + (trouves.isEmpty() ? "aucun" : trouves));
        System.out.println("   image : " + lus.stream().filter(x -> x.startsWith("icône") || x.startsWith("iconeAcc")).toList()
                + " ; titre : " + fr.getTitle());
    }
}
