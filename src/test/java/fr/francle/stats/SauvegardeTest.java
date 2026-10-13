package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// La sauvegarde sur le disque, dans un dossier temporaire que JUnit crée avant chaque test et efface après :
// les tests ne touchent jamais au vrai ~/.francle du joueur
class SauvegardeTest {

    @TempDir
    Path dossier;

    private static final LocalDate JOUR = LocalDate.of(2030, 3, 10);

    private static Historique exemple() {
        return new Historique(Historique.VERSION,
                List.of(new Resultat(JOUR.minusDays(1), RegionFr.BRETAGNE, List.of(RegionFr.CORSE, RegionFr.BRETAGNE),
                        Partie.Etat.GAGNEE)),
                new PartieEnCours(JOUR, List.of(RegionFr.OCCITANIE)));
    }

    @Test
    @DisplayName("Premier lancement (pas de fichier) : historique vide, sans erreur")
    void pasDeFichier() throws IOException {
        Sauvegarde sauvegarde = new Sauvegarde(dossier.resolve("francle/historique.json"));

        assertEquals(Historique.vide(), sauvegarde.charger());
    }

    @Test
    @DisplayName("Enregistrer puis charger redonne exactement le même historique (dossier créé au besoin)")
    void allerRetour() throws IOException {
        Sauvegarde sauvegarde = new Sauvegarde(dossier.resolve("sous/dossier/historique.json"));

        sauvegarde.enregistrer(exemple());

        assertEquals(exemple(), sauvegarde.charger());
    }

    @Test
    @DisplayName("Le fichier est du JSON lisible : dates ISO, régions par leur nom, et aucun fichier temporaire oublié")
    void formatLisible() throws IOException {
        Path fichier = dossier.resolve("historique.json");
        new Sauvegarde(fichier).enregistrer(exemple());

        String json = Files.readString(fichier);

        assertTrue(json.contains("\"version\" : 1"), json);
        assertTrue(json.contains("\"date\" : \"2030-03-09\""), json);
        assertTrue(json.contains("\"BRETAGNE\""), json);
        assertTrue(json.contains("\"etat\" : \"GAGNEE\""), json);
        assertTrue(json.contains("\n"), "indenté sur plusieurs lignes");
        // Files.list ouvre le dossier : try-with-resources le referme (sinon Windows le garde verrouillé)
        try (var fichiers = Files.list(dossier)) {
            assertEquals(List.of(fichier), fichiers.toList());
        }
    }

    @Test
    @DisplayName("Enregistrer remplace l'ancien fichier")
    void remplacement() throws IOException {
        Sauvegarde sauvegarde = new Sauvegarde(dossier.resolve("historique.json"));
        sauvegarde.enregistrer(exemple());

        sauvegarde.enregistrer(Historique.vide());

        assertEquals(Historique.vide(), sauvegarde.charger());
    }

    @Test
    @DisplayName("Un champ inconnu (d'une version plus récente) est ignoré")
    void champInconnu() throws IOException {
        Path fichier = dossier.resolve("historique.json");
        Files.writeString(fichier, """
                { "version" : 1, "resultats" : [ ], "enCours" : null, "theme" : "sombre" }
                """);

        assertEquals(Historique.vide(), new Sauvegarde(fichier).charger());
    }

    @Test
    @DisplayName("Fichier abîmé : mis de côté (pas effacé), et une erreur qui le dit")
    void fichierIllisible() throws IOException {
        Path fichier = dossier.resolve("historique.json");
        Files.writeString(fichier, "{ \"version\" : 1, \"resultats\" : [ { \"date\" : \"pas une date\" } ");

        SauvegardeIllisible erreur = assertThrows(SauvegardeIllisible.class, () -> new Sauvegarde(fichier).charger());

        assertFalse(Files.exists(fichier), "le fichier abîmé ne reste pas à sa place");
        assertTrue(Files.exists(erreur.getDeCote()), "il est gardé de côté");
        assertTrue(erreur.getDeCote().getFileName().toString().startsWith("historique.json.illisible-"));
        assertTrue(Files.readString(erreur.getDeCote()).contains("pas une date"), "son contenu est intact");
        // et le jeu peut repartir : le chargement suivant donne un historique vide
        assertEquals(Historique.vide(), new Sauvegarde(fichier).charger());
    }

    @Test
    @DisplayName("Région inconnue ou fichier « null » : illisible aussi")
    void contenusInvalides() throws IOException {
        Path fichier = dossier.resolve("historique.json");
        for (String contenu : List.of("null", "",
                "{ \"version\" : 1, \"resultats\" : [ { \"date\" : \"2030-03-09\", \"region\" : \"ATLANTIDE\","
                        + " \"propositions\" : [ ], \"etat\" : \"GAGNEE\" } ] }")) {
            Files.writeString(fichier, contenu);
            assertThrows(SauvegardeIllisible.class, () -> new Sauvegarde(fichier).charger(), "« " + contenu + " »");
        }
        // trois fichiers abîmés dans la même seconde : trois copies distinctes, aucune écrasée
        try (var fichiers = Files.list(dossier)) {
            assertEquals(3, fichiers.filter(f -> f.getFileName().toString().contains(".illisible-")).count());
        }
    }
}
