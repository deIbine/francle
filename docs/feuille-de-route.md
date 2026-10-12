# Feuille de route

Plan d'amélioration de Francle, établi le 11 octobre 2026.
Chaque phase associe une amélioration du jeu à une notion Java à apprendre.
Les leçons à retenir de chaque phase sont rassemblées dans [lecons.md](lecons.md).

## État d'avancement

| Phase | Sujet | État |
|---|---|---|
| 0.5 | Dépôt Git propre | ✅ Fait (PR #16) |
| 0 | Environnement de développement | ✅ Fait (JDK 21, IntelliJ 2026.2) |
| 1 | Corriger les bugs | ✅ Fait (4 bugs corrigés) |
| 2 | Centraliser les données des régions | ✅ Fait (enum RegionFr, −466 lignes) |
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

## Phase 1 : corriger les bugs ✅

Méthode appliquée à chaque bug : **reproduire** (prouver le bug par un test), **corriger**,
puis **revérifier avec le même test**. Un commit par bug.

| Bug | Comment il a été repéré | Correction | Réflexe à retenir |
|---|---|---|---|
| Latitude et longitude inversées dans `verifdistance` : est-ouest surestimé de 50 % (Rennes → Strasbourg à 1050 km au lieu de 700 km) | Comparaison avec des distances réelles connues. L'écart vaut 1 / cos(48°) ≈ 1,5 : un degré de longitude compté 111 km au lieu de 74 km | Formule découpée en variables nommées (`lat1`, `lat2`, `deltaLon`), protection contre `acos(>1)` = `NaN` (3,8 % des cas sur des coordonnées aléatoires) | Vérifier une formule contre une valeur connue ; ne jamais comparer des `double` avec `==` |
| `essai` jamais remis à 0 : une nouvelle partie gardait l'historique de la précédente | Exécution à la main de la variable sur deux parties | Compteur local d'une boucle `for` au lieu d'une variable globale au programme | Une variable doit vivre aussi longtemps que ce qu'elle représente ; corriger la racine plutôt que poser un pansement |
| Dé relancé dans `latitudeetlongitude` en mode « toutes les régions » : Alsace reconnue 1 fois sur 2, y compris comme région à deviner | Méthode appelée 1000 fois (496/1000) | Recherche dans les anciennes régions pour tout mode sauf 1 | Une méthode qui cherche doit être déterministe ; se méfier du copier-coller hors contexte |
| `break` manquant après le didacticiel : message « Retour au menu » en trop | `javac -Xlint:fallthrough` signale la ligne exacte | Ajout du `break` | Les `case` sont des étiquettes, pas des blocs ; écouter les avertissements du compilateur |

La bonne formule (loi des cosinus sphérique) :
`d = R · acos(sin φ1 · sin φ2 + cos φ1 · cos φ2 · cos(λ1 − λ2))`,
avec φ la latitude, λ la longitude et R = 6371 km.

**Ce que tu apprends :** comparer une formule mathématique au code, la portée et la
réinitialisation des variables, le *fall-through* d'un `switch`, les avertissements de `javac`.

**Découvertes à traiter plus tard :**

- ~~**Pourcentage de proximité peu parlant**~~ : réglé en Phase 2 (formule B). Constat d'origine : il était calculé par rapport
  à la région la plus éloignée de la cible, souvent un département d'outre-mer à plus de 9000 km.
  Toute la métropole se retrouve entre 88 % et 98 % (pour la Bretagne : Corse 88 %, Normandie 97 %).
  Options : calcul limité à la métropole, distance maximale fixe, ou autre formule.
- ~~**`distance == 0` signifie « région inconnue »**~~ : réglé en Phase 2 (`Optional`).
- **`parseInt` en double dans le didacticiel** (`main`, `case 3`) : code redondant à simplifier.

---

## Phase 2 : mettre les données au même endroit ✅

Les données des régions étaient copiées à 5 endroits (`latitudeetlongitude`, `choixpossible`,
`lesregionsenint`, `onmetlimage`, `touteslesregions`), avec des nombres magiques (26, 27, 17 + 1, 100).
C'était l'origine des problèmes de tableaux de 26 ou 27 cases notés dans l'ancien README.

**Ce qui a été fait** (un commit par étape, le jeu fonctionne à chaque commit) :

1. **Filet de sécurité** : un test de caractérisation photographie le comportement (recherche dans
   les 3 modes, 1225 distances et pourcentages, régions tirables, images comparées pixel par pixel).
   Après chaque étape, la sortie est comparée à la photo : identique sur 1397 lignes.
2. **`enum RegionFr`** : source unique des 35 régions (nom, coordonnées, époque, superficie, indices,
   image), plus les `enum` `Epoque` (`ANCIENNE`, `NOUVELLE`, `LES_DEUX`) et `ModeRegions`, qui porte
   les règles « quelles régions tirer » et « quelles régions reconnaître ».
3. **Recherche** des propositions : `RegionFr.chercher` renvoie un `Optional` au lieu d'une distance nulle.
4. **Tirage** : `RegionFr.tirer(mode)` ; suppression des 413 lignes de `switch`.
5. **Pourcentage** : un simple maximum au lieu d'une troisième copie des coordonnées et d'un tri.
6. **Images et liste des régions** lues dans l'`enum` (liste triée avec un `Collator` français).
7. **Ménage** : sentinelle `latitude == 0`, `degresversradian` (remplacé par `Math.toRadians`), imports inutiles.

Résultat : `main.java` passe de 1421 à 955 lignes ; ajouter une région = **1 ligne**.

**Bugs découverts et corrigés en chemin :**

- une région ayant la même capitale que la cible (Alsace pour le Grand Est…) était refusée
  comme « saisie non reconnue », car la distance valait exactement 0 ;
- la Bourgogne-Franche-Comté manquait dans la liste des régions (touche `1`) ;
- le tirage en mode « toutes les régions » n'était pas équitable (1/27 ou 1/54 selon les régions).

**Ce que tu apprends :** `enum` avec champs, constructeurs et méthodes, bloc `static`, `HashMap`,
`Optional`, références de méthode, méthodes `static` et de fabrique, `Collator` ; le principe DRY
(une *connaissance* à un seul endroit, pas seulement un texte) ; le refactoring sous filet de sécurité
(construire à côté, migrer, supprimer) ; remplacer les valeurs spéciales par une vraie notion d'absence.

**Ajouts après le refactoring** (changements de comportement volontaires, dans des commits séparés) :

- **Pourcentage de proximité, formule B** : pour une cible en métropole, 0 % correspond à la région
  *métropolitaine* la plus éloignée (Bretagne : Normandie 76 %, Grand Est 35 %, Corse 0 % ; avant :
  97 %, 92 %, 88 %). Une cible en outre-mer garde la référence de toutes les régions. Chaque région
  porte désormais sa `Zone` (métropole, outre-mer). Le pourcentage est arrondi au lieu d'être tronqué
  (une distance de quelques centimètres donnait 99 %) et ne descend jamais sous 0 %.
  Conséquence de B : la référence dépend de la cible, les pourcentages sont plus sévères pour une
  région en bordure (Grand Est) que pour une région centrale.
- **Indices corrigés** : crêpe, tarte Tatin, Orléans, bœuf, huîtres, Napoléon, rhum arrangé,
  poulet boucané ; superficie affichée en km² au lieu de km.

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
