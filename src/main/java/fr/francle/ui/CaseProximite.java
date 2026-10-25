package fr.francle.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Optional;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/*
La case « Proximité » du tableau des essais : le pourcentage sur une pastille de la couleur de la région sur la carte
de fin (CarteFin.degrade : jaune pâle si loin, orange foncé si proche ; vert pour la bonne réponse). Le tableau et la
carte parlent ainsi la même langue de couleurs, et on lit d'un coup d'œil si l'on se rapproche.

Un « renderer » est un tampon : la JTable lui demande de se préparer pour chaque case (getTableCellRendererComponent),
puis dessine le composant à l'endroit de la case. Ici, on retient la couleur de la case en préparant, et on peint la
pastille dans paintComponent, sous le texte que dessine le JLabel d'origine.
 */
final class CaseProximite extends DefaultTableCellRenderer {

    // un texte foncé, lisible sur tout le dégradé (du jaune pâle à l'orange foncé), dans les deux thèmes ; blanc sur
    // le vert de la réponse
    static final Color TEXTE = new Color(0x202020);
    static final Color TEXTE_TROUVE = Color.WHITE;
    // la pastille laisse une marge autour d'elle dans la case
    private static final int MARGE_X = 10;
    private static final int MARGE_Y = 4;

    private final TableauEssais tableau;
    // la couleur de la case en cours de dessin, ou rien pour une ligne pas encore jouée
    private Color pastille;

    CaseProximite(TableauEssais tableau) {
        this.tableau = tableau;
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    // la couleur de la pastille d'une ligne : vert pour la réponse, le dégradé de la carte pour un autre essai, rien
    // pour une ligne vide. Sans affichage : testée à part
    static Optional<Color> couleur(TableauEssais tableau, int ligne) {
        if (tableau.trouve(ligne)) {
            return Optional.of(CarteFin.TROUVEE);
        }
        return tableau.essai(ligne).map(essai -> CarteFin.degrade(essai.pourcentage()));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object valeur, boolean selectionnee, boolean focus,
                                                   int ligne, int colonne) {
        super.getTableCellRendererComponent(table, valeur, selectionnee, focus, ligne, colonne);
        // la JTable peut trier ou déplacer ses lignes : on demande au modèle avec SON numéro de ligne
        int ligneDuModele = table.convertRowIndexToModel(ligne);
        pastille = couleur(tableau, ligneDuModele).orElse(null);
        if (pastille != null) {
            setForeground(tableau.trouve(ligneDuModele) ? TEXTE_TROUVE : TEXTE);
            setFont(getFont().deriveFont(Font.BOLD));
        }
        return this;
    }

    // une case opaque peindrait son fond par-dessus tout : la pastille disparaîtrait. Transparente quand elle en a
    // une, la case laisse voir le fond du tableau autour de la pastille
    @Override
    public boolean isOpaque() {
        return pastille == null && super.isOpaque();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (pastille != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(pastille);
            int hauteur = getHeight() - 2 * MARGE_Y;
            g2.fillRoundRect(MARGE_X, MARGE_Y, getWidth() - 2 * MARGE_X, hauteur, hauteur, hauteur);
            g2.dispose();
        }
        // puis le texte, par-dessus (le JLabel d'origine)
        super.paintComponent(g);
    }
}
