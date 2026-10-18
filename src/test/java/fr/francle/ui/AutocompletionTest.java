package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import static org.junit.jupiter.api.Assertions.*;

// L'autocomplétion, sans fenêtre : un champ dans un JRootPane (composant léger, utilisable même sans écran),
// et les touches déclenchées par les mêmes liaisons (InputMap -> ActionMap) que le vrai clavier
class AutocompletionTest {

    private JTextField champ;
    private Autocompletion autocompletion;
    private int validations;
    // ce que répond l'action de validation : saisie acceptée (vrai) ou refusée (faux)
    private boolean acceptee;

    @BeforeEach
    void preparer() {
        champ = new JTextField(20);
        JRootPane racine = new JRootPane();
        racine.getContentPane().add(champ);
        validations = 0;
        acceptee = true;
        autocompletion = new Autocompletion(champ,
                debut -> RegionFr.suggestions(debut, ModeRegions.ANCIENNES).stream().map(RegionFr::getNom).toList(),
                () -> {
                    validations++;
                    return acceptee;
                });
    }

    // comme un appui sur la touche : on suit la liaison du champ, de la touche jusqu'à l'action
    private void touche(String nom) {
        Object cle = champ.getInputMap().get(KeyStroke.getKeyStroke(nom));
        assertNotNull(cle, "aucune action liée à " + nom);
        champ.getActionMap().get(cle).actionPerformed(null);
    }

    @Test
    @DisplayName("Taper un début de nom ouvre la liste des suggestions")
    void suggestions() {
        champ.setText("nor");

        assertTrue(autocompletion.estOuverte());
        assertEquals(List.of("Nord-Pas-de-Calais", "Basse-Normandie", "Haute-Normandie"),
                autocompletion.suggestions());
    }

    @Test
    @DisplayName("↓ puis Entrée : la suggestion choisie remplace la saisie, sans valider")
    void choisirAuClavier() {
        champ.setText("nor");

        touche("DOWN");
        touche("DOWN");
        touche("ENTER");

        assertEquals("Basse-Normandie", champ.getText());
        assertFalse(autocompletion.estOuverte(), "la liste se ferme, et ne se rouvre pas pour le texte écrit par le code");
        assertEquals(0, validations);
    }

    @Test
    @DisplayName("Les flèches restent dans la liste : ↑ en haut et ↓ en bas ne sortent pas")
    void flechesAuxBords() {
        champ.setText("loire");   // 2 suggestions

        touche("UP");
        touche("DOWN");
        touche("DOWN");
        touche("DOWN");
        touche("ENTER");

        assertEquals("Pays de la Loire", champ.getText());
    }

    @Test
    @DisplayName("Entrée sans suggestion surlignée valide ce qui est écrit")
    void entreeValide() {
        champ.setText("bre");

        touche("ENTER");

        assertEquals(1, validations);
        assertEquals("bre", champ.getText());
    }

    @Test
    @DisplayName("Saisie refusée (« nor » évoque plusieurs régions) : la liste se rouvre, sans ligne surlignée")
    void refuseeRouvre() {
        acceptee = false;
        champ.setText("nor");

        touche("ENTER");

        assertEquals(1, validations);
        assertTrue(autocompletion.estOuverte(), "« choisissez dans la liste » : il faut une liste où choisir");
        assertEquals(List.of("Nord-Pas-de-Calais", "Basse-Normandie", "Haute-Normandie"), autocompletion.suggestions());

        // aucune ligne surlignée : un second Entrée valide encore le texte, au lieu de jouer une région au hasard
        touche("ENTER");
        assertEquals(2, validations);
        assertEquals("nor", champ.getText());
        assertTrue(autocompletion.estOuverte());
    }

    @Test
    @DisplayName("Saisie refusée alors que la liste était fermée (Échap) : elle se rouvre")
    void refuseeApresEchap() {
        acceptee = false;
        champ.setText("nor");
        touche("ESCAPE");

        touche("ENTER");

        assertTrue(autocompletion.estOuverte());
    }

    @Test
    @DisplayName("Saisie acceptée : la liste reste fermée")
    void accepteeFerme() {
        champ.setText("nor");

        touche("ENTER");

        assertEquals(1, validations);
        assertFalse(autocompletion.estOuverte());
    }

    @Test
    @DisplayName("Échap ferme la liste")
    void echap() {
        champ.setText("bre");

        touche("ESCAPE");

        assertFalse(autocompletion.estOuverte());
    }

    @Test
    @DisplayName("Pas de liste pour un champ vide, une saisie inconnue ou un nom déjà écrit exactement")
    void pasDeListe() {
        champ.setText("bre");
        champ.setText("");
        assertFalse(autocompletion.estOuverte());

        champ.setText("xyz");
        assertFalse(autocompletion.estOuverte());

        champ.setText("Corse");
        assertFalse(autocompletion.estOuverte());
    }

    @ParameterizedTest(name = "« {0} » → la liste propose encore « {1} »")
    // CsvSource retire les espaces autour d'une valeur, sauf entre guillemets ; et le guillemet par défaut est
    // l'apostrophe, qui apparaît dans « d'Azur » : d'où quoteCharacter = '"'
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            "bretagne                   | Bretagne",
            "BRETAGNE                   | Bretagne",
            "ile de france              | Île-de-France",
            "provence alpes cote d'azur | Provence-Alpes-Côte d'Azur",
            "\"Bretagne \"              | Bretagne",
    })
    @DisplayName("Nom reconnu mais pas écrit exactement (majuscule, accent, tiret, espace) : la liste reste, pour corriger")
    void nomReconnuMalEcrit(String saisie, String correcte) {
        champ.setText(saisie);

        assertTrue(autocompletion.estOuverte());
        assertEquals(List.of(correcte), autocompletion.suggestions());

        touche("DOWN");
        touche("ENTER");

        assertEquals(correcte, champ.getText());
        assertFalse(autocompletion.estOuverte(), "une fois corrigé, le nom est écrit exactement : plus de liste");
        assertEquals(0, validations);
    }
}
