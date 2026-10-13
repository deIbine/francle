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
| 3 | Séparer logique et affichage, tests, Maven, packages | ✅ Fait (PR #21, #22 et 3.3) |
| 4 | Interface Swing en une seule fenêtre | ✅ Fait (PR #24 à #28) |
| 5 | Région du jour et statistiques | 🔄 En cours |
| 6 | Indices plus riches | ⬜ À faire |
| 7 | Version web (Spring Boot) | ⬜ À faire |

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

## Phase 3 : séparer la logique de l'affichage, et ajouter des tests ✅

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

### 3.2 : Maven, JUnit et intégration continue ✅

- **`pom.xml`** : Java 21, UTF-8, JUnit 6.1.3 (*scope* `test`, via son BOM), versions des plugins fixées,
  `target/francle.jar` exécutable (`java -jar`).
- **Maven Wrapper** (`mvnw`, `mvnw.cmd`) : Maven 3.9.16, vérifié par son empreinte SHA-256 ; fins de ligne des
  scripts imposées dans `.gitattributes`.
- **Arborescence Maven** : `src/main/java`, `src/main/resources` (images), `src/test/java`. Les images sont lues
  avec `getResource` : le jeu se lance depuis n'importe quel dossier, et depuis le `.jar`.
- **70 tests JUnit** (`PartieTest`, `RegionFrTest`, `MessageTest`), dont des tests paramétrés ; vérifiés en
  réintroduisant des bugs (inversion latitude/longitude, 6 essais).
- **Intégration continue** : `.github/workflows/ci.yml` lance `./mvnw -B verify` à chaque PR.
- **Corrigé en chemin** : les distances suivaient la langue du système (`698.45` sur une machine anglaise).
- **IntelliJ** : ouvrir le `pom.xml` remplace la configuration manuelle de la Phase 0.

### 3.3 : un fichier par classe, des packages ✅

- **Ménage** : la classe vide `commentjouer` et les brouillons en commentaire (`interfacerapide`, `Statistiques`…)
  sont supprimés ; leurs idées sont reportées dans les phases suivantes (statistiques en Phase 5, le reste en Phase 6).
- **Un fichier par classe** (11 fichiers), chacun avec ses seuls imports : plus d'avertissement « auxiliary class ».
- **Conventions de nommage** : `main` devient `Francle`, `representation` devient `Representation`.
- **Trois packages**, avec des dépendances dans un seul sens (`ui` → `jeu` → `donnees`) :

  | Package | Classes | Rôle |
  |---|---|---|
  | `fr.francle.donnees` | `RegionFr`, `ModeRegions`, `Epoque`, `Zone` | Les régions et leurs informations |
  | `fr.francle.jeu` | `Partie`, `Essai`, `Indice` | Les règles, sans affichage |
  | `fr.francle.ui` | `Francle`, `Message`, `Parametres`, `Representation` | Les popups et les images |

  (`interface`, prévu à l'origine, est un mot réservé de Java : d'où `ui`, pour *user interface*.)
- **Visibilité réduite au nécessaire** : seul ce qu'un autre package utilise est `public`.
- **Tests rangés dans le même package que leur classe** ; deux nouveaux : `RepresentationTest` (images) et
  `LancementTest`, qui vérifie la classe à lancer déclarée dans le `pom.xml` (un oubli que ni le compilateur ni les
  70 autres tests n'avaient vu : le `.jar` ne se lançait plus).

Résultat de la Phase 3 : 978 lignes dans un seul fichier → 11 fichiers rangés en 3 packages, 72 tests JUnit,
une CI, et un comportement identique au départ (hors corrections volontaires) sur 300 sessions rejouées.

**Comment le penser.** *Séparation des responsabilités* : une classe = une raison de changer (les règles
changent avec le *game design*, l'affichage avec l'interface). *Testabilité* : « si je ne peux pas tester
une règle sans cliquer, c'est qu'elle est mal rangée ».

**Ce que tu apprends :** `record`, packages et visibilité (`public`, `private`, visibilité du package),
JUnit (`@Test`, `assertEquals`), Maven (cycle `compile` / `test` / `package`, `src/main/java` et
`src/test/java`), les ressources (`getResource`) au lieu des chemins relatifs fragiles, l'intégration continue.
C'est la phase la plus formatrice sur l'architecture, et la plus grosse.

---

## Phase 4 : une vraie interface Swing ✅

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

**Découpage choisi** (le 13 octobre 2026, Claude code et explique) :

1. **4.1** la flèche de direction, en logique pure dans `jeu`, affichée tout de suite dans les popups ;
   les réglages séparés de leurs popups (prérequis des menus) ;
2. **4.2** la fenêtre principale : `JFrame`, image, champ de saisie, `JTable` des essais sur un `TableModel`,
   lancement sur l'EDT, thème **FlatLaf** (première dépendance Maven hors tests) ;
3. **4.3** la barre de menus (nouvelle partie, paramètres en cases à cocher et boutons radio, comment jouer,
   quitter) à la place des boutons ; suppression des popups de `Parametres` ;
4. **4.4** l'autocomplétion, **faite maison** (filtrage des noms par préfixe pendant la frappe) plutôt
   qu'une `JComboBox` éditable.

### 4.1 : la flèche de direction et les réglages ✅

- **`enum Direction`** (`jeu`) : les 8 directions de la rose des vents avec leur flèche (↑ ↗ → ↘ ↓ ↙ ← ↖), plus
  `SUR_PLACE` (●) pour deux régions qui partagent leur capitale (Hauts-de-France et Nord-Pas-de-Calais : Lille).
  `azimut()` calcule le cap initial en degrés, `depuisAzimut()` le range dans un secteur de 45°.
- **Deux bugs du brouillon `Partie.fleche()`** : les degrés passés tels quels à `Math.sin`, et le cap calculé
  **de la cible vers la proposition** (la flèche aurait pointé à l'envers). Le brouillon est supprimé.
- **`record Essai`** gagne un champ `direction` ; la flèche s'affiche après un essai raté et dans l'historique.
- **`Reglages`** (`ui`) : le mode de jeu et deux booléens (images, liste), sans aucun affichage, au lieu des
  trois `int` 0/1/2 de `Parametres`. `Parametres` ne garde que ses popups, et `Francle.main` perd son
  `switch` imbriqué de 45 lignes.
- **26 nouveaux tests** (98 au total) : les limites des secteurs (22,4° → nord, 22,6° → nord-est), un azimut
  vérifié par une seconde méthode indépendante (82,32° de Rennes à Strasbourg), 8 trajets entre capitales.

### 4.2 : la fenêtre principale ✅

Disposition verticale façon Worldle (choisie le 13 octobre 2026) : boutons, image, saisie et message,
tableau des essais, indices. Plus de cascade de popups pour jouer.

- **`Fenetre`** : `JFrame` en `BorderLayout` imbriqués. Elle ne contient aucune règle : chaque bouton (et la touche
  Entrée) est branché par un `ActionListener` qui transmet l'action à `Partie`, puis affiche la réponse.
  Une saisie inconnue reste dans le champ, sélectionnée, avec un message ; elle ne coûte toujours pas d'essai.
- **`TableauEssais`** (`AbstractTableModel`) : la `JTable` lit chaque case dans `partie.getEssais()`. Toujours
  5 lignes (les vides montrent les essais restants) ; flèche agrandie, coche ✓ sur l'essai gagnant.
- **Fin de partie** : l'image bascule sur la **carte de France** où la région est coloriée (`resources/indices/`,
  jamais utilisées jusqu'ici ; 13 régions de métropole, les autres gardent leur silhouette).
- **`Partie.indicesReveles()`** : quels indices sont révélés est une règle, elle passe dans `jeu`.
- **`Textes`** : toutes les phrases de la fenêtre, testables sans écran. Distances arrondies au kilomètre (`912 km`).
- **`Representation`** réécrite : `charger`, `silhouette`, `carte`, `ajuster` (les images remplissent un cadre fixe
  de 400 × 220 sans être déformées), `icone`.
- **`Francle.main`** : crée la fenêtre sur l'**EDT** (`SwingUtilities.invokeLater`), avec le thème **FlatLaf**.
- **`pom.xml`** : FlatLaf 3.7.2 ; **maven-shade-plugin** 3.6.2 pour un `.jar` « tout compris » (sinon `java -jar`
  ne trouverait pas FlatLaf).
- **Supprimés** : `Message` (sa boucle de popups, son historique en texte) et le menu « tapez 0, 1, 2 ou 3 ».
  Les paramètres passent encore par les popups de `Parametres` (bouton « Paramètres »), jusqu'à la 4.3.
- **144 tests** : `TableauEssaisTest` (cases, événements envoyés à la `JTable`), `TextesTest` (remplace
  `MessageTest`), `RepresentationTest` étendu (carte de chaque région, proportions), `indicesReveles`.
- **Vérifié à l'écran** : la vraie fenêtre lancée, des essais joués en cliquant ses boutons par programme, et
  des captures d'écran. Elles ont montré une fenêtre plus haute que l'écran (cadre réduit de 420 × 320 à
  400 × 220) et des flèches trop petites.

### 4.3 : les menus ✅

- **Barre de menus** (à la place de la barre de boutons, qui disparaît : la fenêtre passe de 624 à 593 points de
  haut) :
  - **Jeu** : Nouvelle partie (Ctrl+N), Liste des régions (Ctrl+L), Abandonner, Quitter (Ctrl+Q) ;
  - **Paramètres** : le mode en **boutons radio** (un seul choix), la silhouette et la liste en **cases à cocher** ;
  - **Aide** : Comment jouer (F1).
  Menus ouvrables au clavier (Alt+J, Alt+P, Alt+A).
- **Des `Action`** au lieu d'écouteurs séparés : « Abandonner » est partagée par le bouton et le menu, et
  `setEnabled(false)` grise les deux. En fin de partie, « Nouvelle partie » remplace « Abandonner » à côté de
  « Valider », et devient le bouton de la touche Entrée.
- **Quitter** passe par le même chemin que la croix de la fenêtre (`WINDOW_CLOSING`).
- **Supprimés** : `Parametres` (ses popups « tapez 0, 1 ou 2 ») et `ModeRegions.depuisCode`, devenus inutiles. Le
  jeu n'affiche plus aucune boîte de saisie.
- **Corrigé en chemin** : la ligne de saisie était en `FlowLayout` ; le bouton « Nouvelle partie », plus large,
  passait à la ligne et disparaissait. Elle est maintenant en `BorderLayout`.
- **145 tests** (`Textes.mode`) ; menus vérifiés par programme (raccourcis déclarés, articles grisés, un seul
  bouton radio coché, mode appliqué à la partie suivante) et fenêtre dessinée en mémoire.

### 4.4 : l'autocomplétion ✅

- **`RegionFr.suggestions(saisie, mode)`** (`donnees`) : les régions acceptées dans le mode dont le **nom** commence
  par la saisie (« nor » → Normandie), puis celles dont un **mot** commence par elle (« loire » → Pays de la Loire) ;
  chaque groupe par ordre alphabétique. Mêmes simplifications que `chercher` (accents, majuscules, tirets) ; les
  petits mots (« de », « la », « d' ») sont ignorés.
- **`Autocompletion`** (`ui`), faite maison : une `JList` posée dans le calque des popups de la fenêtre, sous le champ,
  mise à jour par un `DocumentListener` à chaque lettre. ↓ ↑ pour parcourir, Entrée pour choisir (ou valider s'il n'y
  a pas de suggestion surlignée), Échap pour fermer, clic pour choisir. Elle ne connaît ni les régions ni le jeu.
- **Paramètres** : nouvelle case « Suggestions pendant la frappe » ; deux lignes dans « Comment jouer ».
- **161 tests**, dont `AutocompletionTest` : l'autocomplétion se teste **sans écran** (un champ dans un `JRootPane`),
  en suivant les vraies liaisons des touches.

### 4.4.1 : garder la liste pour corriger l'orthographe ✅

Retour de l'utilisateur après la 4.4 : avec « bretagne » tapé en entier mais sans majuscule, la liste disparaissait.
Elle ne se ferme plus que si le nom est écrit **exactement** (`equals` au lieu de `equalsIgnoreCase`, sans `strip`).
Pour « bretagne », « BRETAGNE », « ile de france » ou « Bretagne » suivi d'une espace, elle reste et propose
l'orthographe correcte. 166 tests.

Résultat de la Phase 4 : le jeu se joue dans une seule fenêtre (image, saisie avec suggestions, tableau des essais
avec flèche de direction, indices, carte de France en fin de partie), réglée par des menus. Il n'y a plus aucun popup
de saisie. Le package `ui` est réécrit (827 lignes en 7 classes, dont la moitié de logique testée sans écran) ; `jeu`
et `donnees` n'ont reçu que des ajouts (`Direction`, `indicesReveles`, `suggestions`). 72 → 166 tests.

**Comment le penser.** La *programmation événementielle* : le programme ne déroule plus ses lignes dans
l'ordre, il **réagit** à des événements (`ActionListener`). Le *fil de Swing* (*Event Dispatch Thread*) :
tout ce qui touche à l'interface s'exécute sur un fil précis, première rencontre avec la programmation
concurrente. **La Phase 3 est un prérequis** : avec une classe `Partie` indépendante, la nouvelle
interface ne fait que la brancher ; sans elle, il faudrait réécrire les règles une seconde fois.

**Ce que tu apprends :** les événements, les layouts, `JTable`, l'EDT, la trigonométrie appliquée (azimut).

---

## Phase 5 : la région du jour et les statistiques

**Pourquoi.** Francle s'inspire de Wordle et Worldle, mais il lui manque ce qui fait revenir chaque jour : une même
énigme pour tout le monde, et des statistiques qui racontent ses parties dans la durée.

**Découpage choisi** (le 13 octobre 2026, Claude code et explique) :

1. **5.1 la région du jour** : une région tirée à partir de la date, la même pour tout le monde (parmi les nouvelles
   régions), avec un numéro de partie à la Wordle. Dans le menu Jeu, « Partie du jour » et « Partie libre »
   (illimitée, comme aujourd'hui) ;
2. **5.2 les statistiques** : l'historique des parties du jour, enregistré en **JSON avec Jackson** dans le dossier de
   l'utilisateur ; parties jouées, % de victoires, série en cours et meilleure série, répartition par nombre
   d'essais ; une fenêtre de statistiques en fin de partie du jour ; la partie du jour ne se rejoue pas, même après
   avoir relancé le jeu. Seules les parties du jour comptent.

**Comment le penser.** *Déterminisme* : « la même région pour tout le monde » veut dire qu'un calcul doit donner le
même résultat sur toutes les machines, sans se parler. Le piège est subtil : la graine du hasard ne suffit pas,
l'ordre de la liste fait partie du résultat. *Persistance* : jusqu'ici, tout disparaissait à la fermeture du jeu.
Écrire sur le disque, c'est aussi prévoir le fichier absent, illisible ou d'une ancienne version.

**Ce que tu apprends :** `LocalDate` et le temps comme paramètre (pour tester n'importe quel jour), le hasard
reproductible, `java.nio.file`, la sérialisation JSON (Jackson), les tests avec un dossier temporaire (`@TempDir`).

---

## Phase 6 : des indices plus riches

| Fonctionnalité | Ce que ça apprend | Difficulté |
|---|---|---|
| **Indices des anciennes régions** (aujourd'hui ni superficie ni indices) | Recherche de données | Très facile : 1 ligne par région dans `RegionFr` |
| **Indices variés**, tirés parmi les personnalités alternatives de `docs/indices.md` | `List` dans un `enum`, hasard | Facile (attention : la partie du jour doit tirer les mêmes indices pour tout le monde) |

**Autres idées notées dans le code d'origine** (commentaires retirés en Phase 3.3, conservés ici) : distances en miles,
thème sombre, silhouette tournée au hasard pour corser le jeu, comparaison proximité / taille de la région.

---

## Phase 7 : la version web

Une **version web** avec Spring Boot : un serveur HTTP qui expose le jeu (API), et une page HTML qui le joue.
Gros projet, réaliste grâce aux Phases 3 à 5 : `Partie`, `RegionFr`, la région du jour et les statistiques sont déjà
indépendants de Swing, et le JSON de la Phase 5 est le format naturel d'une API web.

**Ce que tu apprends :** serveur HTTP, API REST, HTML et JavaScript côté navigateur, déploiement.

**Comment le penser.** Une bonne architecture se mesure au coût d'une nouvelle interface : ici, la troisième
(popups, fenêtre Swing, page web) ne devrait toucher ni aux règles ni aux données.
