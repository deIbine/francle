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
| 3 | Séparer logique et affichage, tests, Maven | 🟡 En cours (3.1 faite : classe `Partie`) |
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
- ~~**`parseInt` en double dans le didacticiel**~~ : réglé (petits correctifs après la Phase 2).

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

## Petits correctifs (entre les phases 2 et 3) ✅

- **Double conversion dans le didacticiel** : une saisie non numérique était remplacée par `"1"` puis reconvertie ;
  le `catch` affecte désormais directement `boucle = 1`, comme le menu principal.
- **Superficie avec séparateur de milliers** (« 27 208 km² ») : une seule méthode, `Message.avecmilliers`,
  pour les deux affichages. Espace insécable U+00A0 imposée, car l'espace fine U+202F du format français
  de Java s'affiche en carré vide avec certaines polices.

---

## Phase 3 : séparer la logique de l'affichage, et ajouter des tests

**Pourquoi.** Tout est encore mélangé dans `main.java` :

- les règles du jeu affichent elles-mêmes des popups : `Region.jeu()` décide si la proposition est bonne
  *et* appelle `JOptionPane`. On ne peut donc pas la tester automatiquement : en Phases 1 et 2, il a fallu
  tester à côté (exécution à la main, comparaison des décisions) ou jouer à la main ;
- deux classes pour une même région : `RegionFr` (les données) et `Region` (l'objet de jeu, une copie
  modifiable). C'est le détour laissé exprès en Phase 2, avec par exemple `pourcentage` qui doit retrouver
  la `RegionFr` à partir de la clé de la `Region` ;
- `superinterface` stocke la distance sous forme de `String`, et l'historique des essais vit dans un tableau
  de 100 cases dont 5 servent ;
- tout est dans un seul fichier, d'où les avertissements `auxiliary class … should not be accessed from
  outside its own source file` ;
- les tests de caractérisation des Phases 1 et 2 vivaient dans un dossier temporaire : ils ne sont ni
  versionnés ni relançables par quelqu'un d'autre.

**Découpage choisi** (le 12 octobre 2026) : **3.1** `Partie` d'abord, testée hors dépôt comme en Phase 2 ;
**3.2** Maven, JUnit (les tests de la 3.1 y sont réécrits) et intégration continue ; **3.3** un fichier par classe
et des packages.

**Ce qu'on ferait :**

1. une classe **`Partie`** qui contient les règles (essais restants, victoire, abandon, indice à révéler),
   sans aucun affichage ; l'interface lui transmet la saisie et affiche ce qu'elle renvoie ;
2. un **`record Essai`** (proposition, distance, pourcentage, indice) à la place de `superinterface`,
   et une `List<Essai>` à la place du tableau de 100 cases ; le jeu manipule directement les `RegionFr` ;
3. **un fichier par classe**, rangées dans des **packages** (`francle.donnees`, `francle.jeu`, `francle.interface`…) ;
4. **Maven ou Gradle** : un fichier `pom.xml` décrit le projet (version de Java, dépendances, images
   dans `src/main/resources`). N'importe quel IDE l'ouvre sans réglage manuel : ça remplace la
   configuration IntelliJ faite à la main en Phase 0 ;
5. de vrais **tests JUnit**, versionnés, qui reprennent les vérifications faites à la main : distances
   connues, recherche dans les 3 modes, pourcentages entre 0 et 100, 5 échecs = défaite… ;
6. une **GitHub Action** qui compile et lance les tests à chaque PR (✅ ou ❌ directement sur la PR).

### 3.1 : la classe `Partie` ✅

- **`Partie`** : les règles (essais, victoire, défaite, abandon), sans aucun affichage. `proposer(saisie)` renvoie
  l'`Essai` joué, ou `Optional.empty()` pour une saisie inconnue (qui ne coûte pas d'essai). L'état est un
  `enum Etat` (`EN_COURS`, `GAGNEE`, `PERDUE`, `ABANDONNEE`) au lieu du `compteur = 5` qui servait à tout.
- **`record Essai`** (proposition, distance, pourcentage) et une `List<Essai>` à la place de `superinterface`
  et de son tableau de 100 cases ; **`enum Indice`** pour l'ordre des indices.
- La distance passe dans `RegionFr` (`a.distance(b)`), le pourcentage dans `Partie`, la simplification des saisies
  dans `RegionFr.simplifier`. **`Region` et `superinterface` sont supprimées.**
- `Message.tour()` demande la saisie, la transmet à `Partie` et affiche la réponse.
- **Filet** : `JOptionPane` et `JFrame` remplacés par des doublures (`--patch-module`), 300 sessions rejouées :
  77 752 lignes identiques. Un mini-banc de 13 vérifications teste `Partie` sans aucun clic.
- **Bugs d'origine corrigés ensuite**, chacun dans son commit : le message d'abandon n'était jamais affiché
  (« C'est raté ! » à la place) ; « 1 pour avoir la liste » ne marchait qu'en mode « nouvelles régions ».

Résultat : `main.java` passe de 978 à 828 lignes.

**Comment le penser.** *Séparation des responsabilités* : une classe = une raison de changer (les règles
changent avec le *game design*, l'affichage avec l'interface). *Testabilité* : « si je ne peux pas tester
une règle sans cliquer, c'est qu'elle est mal rangée ».

**Ce que tu apprends :** `record`, packages et visibilité (`public`, `private`, visibilité du package),
JUnit 5 (`@Test`, `assertEquals`), Maven (cycle `compile` / `test` / `package`, `src/main/java` et
`src/test/java`), les ressources (`getResource`) au lieu des chemins relatifs fragiles, l'intégration continue.
C'est la phase la plus formatrice sur l'architecture, et la plus grosse.

---

## Phase 4 : une vraie interface Swing

**Pourquoi.** Le jeu est une cascade de popups (saisir, valider, lire, OK, recommencer) ; l'historique
est un texte dans la question suivante, et l'image s'ouvre dans une fenêtre séparée.

**Ce qu'on ferait :**

- une seule fenêtre : l'image de la région, un champ de saisie, un **tableau des essais** (`JTable`)
  qui se remplit au fur et à mesure ;
- l'**autocomplétion** des noms de régions pendant la frappe ;
- la **flèche de direction**, comme dans Worldle : le brouillon `Partie.fleche()` existe déjà. Il manque la
  conversion des degrés en radians (le même piège qu'en Phase 1), puis le découpage de l'azimut
  en 8 directions (↑ ↗ → ↘ ↓ ↙ ← ↖) ;
- les menus (paramètres, didacticiel) dans la fenêtre, au lieu de taper 0, 1, 2 ou 3.

**Comment le penser.** La *programmation événementielle* : le programme ne déroule plus ses lignes dans
l'ordre, il **réagit** à des événements (`ActionListener`). Le *fil de Swing* (*Event Dispatch Thread*) :
tout ce qui touche à l'interface s'exécute sur un fil précis, première rencontre avec la programmation
concurrente. **La Phase 3 est un prérequis** : avec une classe `Partie` indépendante, la nouvelle
interface ne fait que la brancher ; sans elle, il faudrait réécrire les règles une seconde fois.

**Ce que tu apprends :** les événements, les layouts, `JTable`, l'EDT, la trigonométrie appliquée (azimut).

---

## Phase 5 : nouvelles fonctionnalités

| Fonctionnalité | Ce que ça apprend | Difficulté après la Phase 2 |
|---|---|---|
| **Indices des anciennes régions** (aujourd'hui ni superficie ni indices) | Recherche de données | Très facile : 1 ligne par région dans `RegionFr` |
| **Indices variés**, tirés parmi les personnalités alternatives de `docs/indices.md` | `List` dans un `enum`, hasard | Facile |
| **Statistiques à la Wordle** : parties jouées, % de victoires, répartition par nombre d'essais | Lire et écrire des fichiers (`java.nio.file`), JSON ou propriétés | Moyen |
| **Région du jour**, la même pour tout le monde | `LocalDate`, hasard reproductible avec une graine (`new Random(date.toEpochDay())`) | Moyen : piège subtil, l'ordre de la liste fait partie du résultat |
| **Version web** avec Spring Boot | Serveur HTTP, API, HTML | Gros projet, réaliste après la Phase 3 |
| **Variante Charlemagne** (ou départements, pays européens) | Généraliser `RegionFr` en jeu de données interchangeable | Moyen à gros |

**Comment le penser.** La plupart de ces fonctionnalités sont devenues faciles grâce aux Phases 2 et 3 :
c'est la récompense d'une bonne architecture, chaque nouvelle fonctionnalité coûte moins cher que la
précédente. Les ajouter *avant* la Phase 3 voudrait dire tester encore en cliquant, dans un fichier déjà trop gros.

**Ordre recommandé : 3 → 4 → 5**, en commençant la Phase 3 par la classe `Partie` et les tests JUnit.
Exception possible : les indices des anciennes régions, faisables à tout moment pour un résultat visible rapide.
