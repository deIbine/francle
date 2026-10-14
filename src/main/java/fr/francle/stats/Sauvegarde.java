package fr.francle.stats;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/*
Un objet enregistré sur le disque, en JSON : l'historique des parties, et depuis la Phase 6 les réglages du joueur.
Seule classe du jeu qui lit ou écrit un fichier, et seule à connaître Jackson : si on changeait un jour de format,
rien d'autre ne bougerait.

Sauvegarde<T> est générique : le T est le type de l'objet enregistré (Historique, Reglages…). Le mécanisme est le
même pour tous : lecture, écriture sans risque de fichier à moitié écrit, fichier abîmé mis de côté. On l'écrit
donc une seule fois, au lieu d'une copie par type.

Le fichier est passé au constructeur : le jeu utilise le dossier ~/.francle, les tests un dossier temporaire
(ils ne doivent JAMAIS toucher aux vrais fichiers du joueur).
 */
public class Sauvegarde<T> {

    // configuré une fois, partagé (un JsonMapper est prévu pour être réutilisé, et le créer coûte cher)
    private static final JsonMapper JSON = JsonMapper.builder()
            // un fichier indenté, lisible à l'œil et facile à comparer
            .enable(SerializationFeature.INDENT_OUTPUT)
            // un champ inconnu (écrit par une version plus récente du jeu) est ignoré au lieu de tout rendre illisible
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    // le dossier personnel du joueur, quel que soit le dossier d'où le jeu est lancé
    public static final Path DOSSIER = Path.of(System.getProperty("user.home"), ".francle");

    private final Path fichier;
    // Java efface les types génériques à la compilation : à l'exécution, Jackson ne saurait pas quel T fabriquer.
    // On lui donne donc la classe elle-même (Historique.class…)
    private final Class<T> type;
    // fabrique la valeur quand il n'y a pas de fichier (premier lancement) ou qu'il est illisible.
    // Un Supplier plutôt qu'une valeur : un objet modifiable (Reglages) ne doit pas être partagé entre deux appels
    private final Supplier<T> valeurParDefaut;

    public Sauvegarde(Path fichier, Class<T> type, Supplier<T> valeurParDefaut) {
        this.fichier = fichier;
        this.type = type;
        this.valeurParDefaut = valeurParDefaut;
    }

    // ~/.francle/historique.json
    public static Sauvegarde<Historique> historique() {
        return historique(DOSSIER.resolve("historique.json"));
    }

    public static Sauvegarde<Historique> historique(Path fichier) {
        return new Sauvegarde<>(fichier, Historique.class, Historique::vide);
    }

    public Path getFichier() { return fichier; }

    public T parDefaut() { return valeurParDefaut.get(); }

    // pas de fichier : premier lancement, valeur par défaut. Fichier illisible : mis de côté, et SauvegardeIllisible
    public T charger() throws IOException {
        if (Files.notExists(fichier)) {
            return parDefaut();
        }
        String texte = Files.readString(fichier);
        try {
            T valeur = JSON.readValue(texte, type);
            // un fichier qui contient juste « null » se lit sans erreur… mais ne donne rien
            if (valeur == null) {
                throw new IllegalArgumentException("contenu vide (null)");
            }
            return valeur;
        } catch (JacksonException | IllegalArgumentException e) {
            Path deCote = nomLibre(fichier.getFileName() + ".illisible-" + LocalDateTime.now().format(HORODATAGE));
            Files.move(fichier, deCote);
            throw new SauvegardeIllisible(fichier, deCote, e);
        }
    }

    // un nom de fichier pas encore pris, à côté du fichier : « nom », sinon « nom-2 », « nom-3 »…
    // (deux fichiers illisibles dans la même seconde auraient sinon le même nom, et le second déplacement échouerait)
    private Path nomLibre(String nom) {
        Path chemin = fichier.resolveSibling(nom);
        for (int numero = 2; Files.exists(chemin); numero++) {
            chemin = fichier.resolveSibling(nom + "-" + numero);
        }
        return chemin;
    }

    // écrit d'abord un fichier temporaire, puis le renomme : si le jeu s'arrête en pleine écriture (coupure, plantage),
    // l'ancien fichier reste intact au lieu d'être remplacé par un fichier à moitié écrit
    public void enregistrer(T valeur) throws IOException {
        Files.createDirectories(fichier.getParent());
        Path temporaire = fichier.resolveSibling(fichier.getFileName() + ".tmp");
        Files.writeString(temporaire, JSON.writeValueAsString(valeur));
        try {
            Files.move(temporaire, fichier, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // certains systèmes de fichiers ne savent pas renommer « d'un seul coup » : on se contente d'un renommage simple
            Files.move(temporaire, fichier, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
