package fr.francle.ui;

import fr.francle.jeu.Partie;
import fr.francle.stats.Statistiques;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/*
Le contenu de la fenêtre des statistiques, à la Wordle : quatre chiffres, puis la répartition des victoires par nombre
d'essais en barres horizontales. Un simple panneau : Fenetre le montre dans une boîte de dialogue, et la vérification
peut le dessiner en mémoire sans ouvrir de fenêtre.
 */
final class FenetreStatistiques {

    private FenetreStatistiques() {}

    // essaisDuJour : le nombre d'essais de la victoire du jour, pour mettre sa barre en valeur (0 si pas de victoire)
    static JPanel panneau(Statistiques stats, int essaisDuJour, Textes textes) {
        JPanel chiffres = new JPanel(new GridLayout(1, 4, 12, 0));
        chiffres.add(chiffre(String.valueOf(stats.jouees()), textes.texte("statistiques.jouees")));
        chiffres.add(chiffre(textes.pourcentage(stats.pourcentageVictoires()), textes.texte("statistiques.victoires")));
        chiffres.add(chiffre(String.valueOf(stats.serieActuelle()), textes.texte("statistiques.serie")));
        chiffres.add(chiffre(String.valueOf(stats.meilleureSerie()), textes.texte("statistiques.meilleureSerie")));

        JComponent barres = new Repartition(stats.repartition(), essaisDuJour - 1);
        barres.setBorder(BorderFactory.createTitledBorder(textes.texte("statistiques.repartition")));

        JPanel panneau = new JPanel(new BorderLayout(0, 12));
        panneau.add(chiffres, BorderLayout.NORTH);
        panneau.add(barres, BorderLayout.CENTER);
        if (stats.jouees() == 0) {
            panneau.add(new JLabel(textes.texte("statistiques.vide"), SwingConstants.CENTER),
                    BorderLayout.SOUTH);
        }
        return panneau;
    }

    // un grand chiffre, et sa légende en dessous
    private static JPanel chiffre(String valeur, String legende) {
        JLabel grand = new JLabel(valeur, SwingConstants.CENTER);
        grand.setFont(grand.getFont().deriveFont(Font.BOLD, grand.getFont().getSize2D() * 2.2f));
        JLabel petit = new JLabel(legende, SwingConstants.CENTER);
        JPanel bloc = new JPanel(new BorderLayout());
        bloc.add(grand, BorderLayout.CENTER);
        bloc.add(petit, BorderLayout.SOUTH);
        return bloc;
    }

    /*
    Les barres de la répartition, dessinées à la main. Swing appelle paintComponent chaque fois qu'il faut (re)dessiner
    le composant (affichage, fenêtre redimensionnée…) : on n'y fait que dessiner, à partir des données, jamais
    autre chose. Les tailles sont calculées à partir de getWidth() et getHeight() : le dessin suit la taille réelle.
     */
    private static final class Repartition extends JComponent {

        private static final int HAUTEUR_LIGNE = 26;
        private static final Color GRIS = new Color(0x787C7E);

        private final List<Integer> valeurs;
        private final int enValeur;      // l'indice de la barre mise en valeur, ou -1

        Repartition(List<Integer> valeurs, int enValeur) {
            this.valeurs = valeurs;
            this.enValeur = enValeur;
            setPreferredSize(new Dimension(320, HAUTEUR_LIGNE * Partie.ESSAIS_MAX + 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            // une copie du « pinceau » : on peut changer ses réglages sans gêner le dessin des autres composants
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics mesures = g2.getFontMetrics();

            // la zone de dessin, à l'intérieur de la bordure (le titre « Victoires par… » en fait partie)
            var marges = getInsets();
            int x0 = marges.left + 6;
            int largeurUtile = getWidth() - marges.left - marges.right - 12;
            int y0 = marges.top + 4;
            int colonneChiffre = mesures.stringWidth(String.valueOf(Partie.ESSAIS_MAX)) + 10;
            // la plus longue barre prend toute la largeur ; Math.max(1, …) évite une division par zéro sans victoire
            int maximum = Math.max(1, Collections.max(valeurs));
            Color accent = UIManager.getColor("Component.accentColor");

            for (int i = 0; i < valeurs.size(); i++) {
                int y = y0 + i * HAUTEUR_LIGNE;
                int milieu = y + (HAUTEUR_LIGNE + mesures.getAscent() - mesures.getDescent()) / 2;
                g2.setColor(getForeground());
                g2.drawString(String.valueOf(i + 1), x0, milieu);

                String nombre = String.valueOf(valeurs.get(i));
                int largeurMax = largeurUtile - colonneChiffre;
                // même une barre à 0 reste visible, assez large pour son nombre
                int largeur = Math.max(mesures.stringWidth(nombre) + 14, largeurMax * valeurs.get(i) / maximum);
                g2.setColor(i == enValeur && accent != null ? accent : GRIS);
                g2.fillRoundRect(x0 + colonneChiffre, y + 3, largeur, HAUTEUR_LIGNE - 6, 6, 6);
                g2.setColor(Color.WHITE);
                g2.drawString(nombre, x0 + colonneChiffre + largeur - mesures.stringWidth(nombre) - 7, milieu);
            }
            g2.dispose();
        }
    }
}
