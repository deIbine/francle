package fr.francle.stats;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/*
Un seul jeu ouvert à la fois. Chaque fenêtre garde l'historique en mémoire et réécrit tout le fichier à chaque essai :
avec deux fenêtres ouvertes, la dernière à enregistrer effacerait les parties jouées dans l'autre.

Le jeu « verrouille » donc un fichier au lancement (~/.francle/francle.lock) : c'est le système d'exploitation qui
tient le verrou, pas le contenu du fichier (vide). Un second lancement le trouve pris et s'arrête. Et si le jeu
s'arrête brutalement (plantage, coupure), le système relâche le verrou de lui-même : le jeu ne reste jamais bloqué,
contrairement à un simple fichier « déjà ouvert » qu'on oublierait d'effacer.
 */
public final class Verrou implements AutoCloseable {

    // ~/.francle/francle.lock
    public static final Path FICHIER = Sauvegarde.DOSSIER.resolve("francle.lock");

    private final FileChannel canal;
    private final FileLock verrou;

    private Verrou(FileChannel canal, FileLock verrou) {
        this.canal = canal;
        this.verrou = verrou;
    }

    // le verrou, ou rien s'il est déjà pris (par un autre jeu ouvert). IOException : le fichier ne peut pas être créé
    // (dossier en lecture seule…) ; à l'appelant de décider s'il joue quand même
    public static Optional<Verrou> prendre(Path fichier) throws IOException {
        Files.createDirectories(fichier.getParent());
        FileChannel canal = FileChannel.open(fichier, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            // tryLock n'attend pas : il rend null si un autre programme tient déjà le verrou
            FileLock verrou = canal.tryLock();
            if (verrou != null) {
                return Optional.of(new Verrou(canal, verrou));
            }
        } catch (OverlappingFileLockException e) {
            // le verrou est déjà tenu par ce même programme (Java le dit par une exception, et non par null)
        }
        canal.close();
        return Optional.empty();
    }

    // relâche le verrou : un autre lancement pourra le prendre. Le jeu, lui, ne l'appelle jamais : il le garde jusqu'à
    // sa fermeture, où le système le relâche
    @Override
    public void close() throws IOException {
        verrou.release();
        canal.close();
    }
}
