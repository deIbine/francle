# Les outils de la passe du 9 novembre 2026

Des scripts jetables, gardés pour refaire les constats de la phase 20 (voir
[feuille-de-route.md](../../docs/feuille-de-route.md#phase-20--la-passe-du-9-novembre)). Ils ne font pas partie du
jeu : Maven ne les compile pas et `./mvnw test` ne les lance pas. Une fois la phase 20 finie, on peut les supprimer.

| Fichier | Ce qu'il vérifie |
|---|---|
| `saisie.jsh` | Chaque nom de région (35 régions, chaque mode, français et anglais) et ses variantes : minuscules, majuscules, sans accents, espaces au lieu des tirets, espaces autour, apostrophe ’. Les variantes marquées `[info]` vont plus loin (espace insécable, ‘, ʼ, ‑, –). Puis les 101 départements. |
| `Harnais.java` | La vraie fenêtre, avec une horloge factice (9 novembre 2026) et des sauvegardes dans un dossier temporaire : disposition, fuites de la réponse, « Passer », le message du partage, « Abandonner », la fin de partie à plusieurs largeurs, le compte à rebours, l'unité de l'aide. Il ferme seul les boîtes de dialogue et les décrit. |
| `Harnais2.java` | Les rapports de contraste (WCAG) des deux thèmes : couleurs de FlatLaf et couleurs écrites dans le code. |
| `Harnais3.java` | Un message de deux lignes qui rapetisse le cadre de l'image, un redimensionnement en une seule passe, le focus sur la carte du bonus en thème sombre. Il réutilise `Harnais.java`. |

Les harnais sont dans le package `fr.francle.ui` pour atteindre les méthodes privées de `Fenetre` par réflexion.

## Les lancer (Windows, depuis la racine du dépôt)

```
mvnw test-compile
mvnw -q dependency:build-classpath -Dmdep.outputFile=cp.txt
set /p CP=<cp.txt
```

Le test de la saisie :

```
jshell --class-path "target\classes;%CP%" outils\verification\saisie.jsh
```

Les harnais : ils ouvrent de vraies fenêtres, et les captures vont dans le dossier donné en argument (à créer avant).

```
mkdir %TEMP%\francle-img
javac -encoding UTF-8 -cp "target\classes;%CP%" -d %TEMP%\francle-harnais outils\verification\Harnais*.java
java -cp "%TEMP%\francle-harnais;target\classes;%CP%" fr.francle.ui.Harnais %TEMP%\francle-img
java -cp "%TEMP%\francle-harnais;target\classes;%CP%" fr.francle.ui.Harnais2
java -cp "%TEMP%\francle-harnais;target\classes;%CP%" fr.francle.ui.Harnais3 %TEMP%\francle-img
```

Sous Linux ou macOS, remplacer `;` par `:` dans les classpaths. Sans écran, préfixer par `xvfb-run -a`.

Le jeu installé n'est pas touché : les sauvegardes vont dans un dossier temporaire, et le verrou n'est pas pris. Sous
Windows, la fenêtre fait 466 × 635 points (491 × 637 sous Linux : les polices diffèrent).
