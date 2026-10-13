package fr.francle.stats;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/*
L'historique sur le disque, en JSON. Seule classe du jeu qui lit ou écrit un fichier, et seule à connaître Jackson :
si on changeait un jour de format, rien d'autre ne bougerait.

Le fichier est passé au constructeur : le jeu utilise ~/.francle/historique.json, les tests un dossier temporaire
(ils ne doivent JAMAIS toucher aux vraies statistiques du joueur).
 */
public class Sauvegarde {

    // configuré une fois, partagé (un JsonMapper est prévu pour être réutilisé, et le créer coûte cher)
    private static final JsonMapper JSON = JsonMapper.builder()
            // un fichier indenté, lisible à l'œil et facile à comparer
            .enable(SerializationFeature.INDENT_OUTPUT)
            // un champ inconnu (écrit par une version plus récente du jeu) est ignoré au lieu de tout rendre illisible
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path fichier;

    public Sauvegarde(Path fichier) {
        this.fichier = fichier;
    }

    // ~/.francle/historique.json : le dossier personnel du joueur, quel que soit le dossier d'où le jeu est lancé
    public static Sauvegarde parDefaut() {
        return new Sauvegarde(Path.of(System.getProperty("user.home"), ".francle", "historique.json"));
    }

    public Path getFichier() { return fichier; }

    // pas de fichier : premier lancement, historique vide. Fichier illisible : mis de côté, et SauvegardeIllisible
    public Historique charger() throws IOException {
        if (Files.notExists(fichier)) {
            return Historique.vide();
        }
        String texte = Files.readString(fichier);
        try {
            Historique historique = JSON.readValue(texte, Historique.class);
            // un fichier qui contient juste « null » se lit sans erreur… mais ne donne rien
            if (historique == null) {
                throw new IllegalArgumentException("historique vide (null)");
            }
            return historique;
        } catch (JacksonException | IllegalArgumentException e) {
            Path deCote = nomLibre(fichier.getFileName() + ".illisible-" + LocalDateTime.now().format(HORODATAGE));
            Files.move(fichier, deCote);
            throw new SauvegardeIllisible(fichier, deCote, e);
        }
    }

    // un nom de fichier pas encore pris, à côté de l'historique : « nom », sinon « nom-2 », « nom-3 »…
    // (deux fichiers illisibles dans la même seconde auraient sinon le même nom, et le second déplacement échouerait)
    private Path nomLibre(String nom) {
        Path chemin = fichier.resolveSibling(nom);
        for (int numero = 2; Files.exists(chemin); numero++) {
            chemin = fichier.resolveSibling(nom + "-" + numero);
        }
        return chemin;
    }

    // écrit d'abord un fichier temporaire, puis le renomme : si le jeu s'arrête en pleine écriture (coupure, plantage),
    // l'ancien historique reste intact au lieu d'être remplacé par un fichier à moitié écrit
    public void enregistrer(Historique historique) throws IOException {
        Files.createDirectories(fichier.getParent());
        Path temporaire = fichier.resolveSibling(fichier.getFileName() + ".tmp");
        Files.writeString(temporaire, JSON.writeValueAsString(historique));
        try {
            Files.move(temporaire, fichier, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // certains systèmes de fichiers ne savent pas renommer « d'un seul coup » : on se contente d'un renommage simple
            Files.move(temporaire, fichier, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
