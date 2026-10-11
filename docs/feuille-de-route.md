# Feuille de route

Plan d'amélioration de Francle, établi le 11 octobre 2026.
Chaque phase associe une amélioration du jeu à une notion Java à apprendre.

## État d'avancement

| Phase | Sujet | État |
|---|---|---|
| 0.5 | Dépôt Git propre | ✅ Fait (PR #16) |
| 0 | Environnement de développement | ✅ Fait (JDK 21, IntelliJ 2026.2) |
| 1 | Corriger les bugs | ⬜ À faire |
| 2 | Centraliser les données des régions | ⬜ À faire |
| 3 | Séparer logique et affichage, tests, Maven/Gradle | ⬜ À faire |
| 4 | Interface Swing en une seule fenêtre | ⬜ À faire |
| 5 | Nouvelles fonctionnalités | ⬜ À faire |

---

## Phase 0.5 : dépôt Git propre ✅

Fait dans la PR [#16](https://github.com/deIbine/francle/pull/16) :

- `.gitignore` complet (IDE, dossiers de build, fichiers système) ;
- `.gitattributes` : fins de ligne normalisées (le dépôt mélangeait CRLF et LF) ;
- `oldcode/` déplacé dans `archive/projet-universitaire/`, indices dans `docs/indices.md`,
  images renommées sans espaces ni accents, `LICENCE.MD` renommé en `LICENSE` ;
- README réécrit ;
- suppression des 16 branches `patch-*` créées par l'éditeur web de GitHub
  (le contenu utile des PR non fusionnées a été récupéré dans `docs/indices.md`).

**Conventions adoptées pour la suite :**

- messages de commit en français, à l'impératif (« Corrige le calcul de distance ») ;
- une branche par phase, fusionnée dans `main` via une Pull Request ;
- pas de réécriture de l'historique (`rebase` ou `push --force` sur `main`).

---

## Phase 0 : environnement de développement ✅

**Notions :**

- **JRE** : exécute du Java. **JDK** : le JRE plus les outils de développement (`javac`, `jar`…).
- **`PATH`** : les dossiers où Windows cherche une commande comme `javac`.
- **`JAVA_HOME`** : indique aux outils de build (Maven, Gradle) où se trouve le JDK.
- **LTS** (*Long Term Support*) : seules certaines versions de Java sont maintenues plusieurs
  années (17, 21, 25…). Les versions 18 et 19 ne l'étaient que 6 mois.
- **Configuration d'un projet IntelliJ** (`.idea/`, propre à chaque machine) : le *source root*
  (où est le code), les dossiers exclus (ici `archive/`, qui contient une deuxième classe `main`),
  le *SDK* (le JDK utilisé) et la *run configuration* (quelle classe lancer, depuis quel dossier).

**Fait :**

- JDK 21 Temurin installé, avec `PATH` et `JAVA_HOME` configurés ;
- jeu compilé et lancé en ligne de commande
  (`javac -encoding UTF-8 -d out main.java` puis `java -cp out main`) ;
- IntelliJ IDEA 2026.2.3 installé et configuré : le jeu se lance depuis l'IDE.
  JetBrains a arrêté l'édition Community après la version 2025.2 ; la version unifiée
  reste gratuite pour le développement Java, Maven, Gradle et Git.

**Leçons retenues :**

- un installeur lancé en mode `/quiet` ne peut pas afficher la fenêtre UAC : il échoue sans rien dire ;
- `winget` peut rester bloqué à la fin d'un téléchargement (service *Delivery Optimization*) ;
  `curl -C -` permet de reprendre un téléchargement interrompu ;
- toujours vérifier l'empreinte SHA-256 d'un installeur téléchargé.

**Facultatif :** désinstaller IntelliJ 2022.1, PyCharm 2022.1 et les anciens JDK
(`~/.jdks/openjdk-18`, `openjdk-19`, `C:\Program Files\Java\jdk1.8` et `jre1.8`).

---

## Phase 1 : corriger les bugs

| Bug | Où (`main.java`) | Effet visible |
|---|---|---|
| Latitude et longitude inversées dans la formule de distance | `verifdistance` | Rennes → Strasbourg : environ 1050 km au lieu de 700 km ; pourcentages incohérents |
| `essai` jamais remis à 0 | `main`, `case 1` | Une nouvelle partie affiche les essais de la précédente |
| Le dé est relancé pendant la vérification en mode « toutes les régions » | `latitudeetlongitude` | « Alsace » n'est reconnue qu'une fois sur deux |
| `break` manquant après le didacticiel | `main`, `case 3` | Message « Retour au menu » en trop |

La bonne formule (loi des cosinus sphérique) :
`d = R · acos(sin φ1 · sin φ2 + cos φ1 · cos φ2 · cos(λ1 − λ2))`,
avec φ la latitude, λ la longitude et R = 6371 km.
Le code actuel utilise la longitude à la place de la latitude.

**Ce que tu apprends :** comparer une formule mathématique au code, la portée et la
réinitialisation des variables, le *fall-through* d'un `switch`.

---

## Phase 2 : mettre les données au même endroit

Les coordonnées de chaque région sont aujourd'hui copiées à 3 endroits
(`latitudeetlongitude`, `choixpossible`, `lesregionsenint`). C'est d'ailleurs l'origine
des problèmes de tableaux de 26 ou 27 cases notés dans l'ancien README.

- Un seul `enum RegionFr` (ou un `record`) contient le nom, la latitude, la longitude,
  les indices, la superficie, l'image et l'époque (ancienne ou nouvelle région).
- Les grands `switch` sont remplacés par des `List` et des `Map`. Le tirage devient
  `liste.get(random.nextInt(liste.size()))`, sans aucun nombre écrit en dur.
- Le pourcentage se calcule avec `Collections.max(...)` au lieu d'un tri fait main
  sur un tableau de 100 cases.

**Ce que tu apprends :** `enum`, `record`, les collections, le principe DRY
(*Don't Repeat Yourself* : ne pas dupliquer). Le fichier devrait perdre plusieurs centaines de lignes.

---

## Phase 3 : séparer la logique de l'affichage, et ajouter des tests

- Une classe `Partie` contient les règles (essais, victoire, indices) sans aucune
  `JOptionPane`. L'interface ne fait que l'appeler.
- Des tests JUnit : `distance(Paris, Lille) ≈ 204 km`, « après 5 échecs, la partie est perdue »…
- Passage à **Maven ou Gradle** : `src/main/java`, `src/main/resources` pour les images, packages.
- Une **GitHub Action** qui compile et teste chaque Pull Request.

**Ce que tu apprends :** la séparation des responsabilités (une version légère de MVC),
les tests unitaires, les outils de build, l'intégration continue.

---

## Phase 4 : une vraie interface Swing

Une seule fenêtre au lieu de la cascade de popups :

- l'image de la région ;
- un champ de saisie avec autocomplétion des noms de régions ;
- un tableau des essais (`JTable`) qui se remplit au fur et à mesure ;
- une **flèche de direction**, comme dans Worldle. La méthode `fleche()` est presque prête :
  il manque la conversion des degrés en radians, puis le découpage de l'azimut en 8 directions.

**Ce que tu apprends :** la programmation événementielle (`ActionListener`), les layouts
et la trigonométrie appliquée.

---

## Phase 5 : nouvelles fonctionnalités

- **Statistiques à la Wordle**, sauvegardées dans un fichier : parties jouées,
  pourcentage de victoires, répartition par nombre d'essais.
- **Région du jour**, la même pour tout le monde, choisie à partir de la date
  (`new Random(LocalDate.now().toEpochDay())`).
- **Indices pour les anciennes régions** : elles n'en ont pas aujourd'hui, et leur superficie vaut 0.
- **Indices variés** : piocher parmi les personnalités alternatives de `docs/indices.md`.
- Plus tard : une version web avec Spring Boot, ou une variante sur l'empire de Charlemagne.
