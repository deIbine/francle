"""Génère toutes les images du jeu, et les contours des départements, à partir des contours officiels.

Les images sont sur fond transparent : elles s'affichent aussi bien en thème clair qu'en thème sombre.

    regions/<image>             la silhouette de chaque région, départements tracés, montrée pendant la partie
    regions/defaut.png          la France métropolitaine et un point d'interrogation, quand la silhouette est désactivée
    cartes/nouvelles/<image>    la France des régions de 2016, la réponse en bleu (fin de partie, mode « nouvelles »)
    cartes/anciennes/<image>    la France d'avant 2016, la réponse en bleu (fin de partie, mode « anciennes »)
    departements/<région>.json  les contours de chaque département de la région, pour le bonus des départements
    france.json                 les contours de chaque région (métropole, et l'outre-mer en encarts), pour la carte
                                de fin de partie que le jeu dessine lui-même

Les départements de chaque région ne sont pas écrits ici : ils sont lus dans departements/departements.json, le
fichier dont se sert aussi le jeu.

Les contours viennent du dépôt https://github.com/gregoiredavid/france-geojson (données IGN et INSEE, licence ouverte
Etalab). Ils ne sont pas dans le dépôt de Francle (près de 6 Mo) : les télécharger dans un dossier à part.
    departements-avec-outre-mer.geojson
    regions.geojson
    regions-avant-redecoupage-2015.geojson

Utilisation, depuis la racine du projet (Python 3 avec matplotlib et Pillow) :
    python outils/generer_images.py <dossier des contours> src/main/resources
"""
import json
import math
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")  # dessine dans un fichier, sans ouvrir de fenêtre
import matplotlib.pyplot as plt
from matplotlib.patches import Polygon
from PIL import Image, ImageFilter

# la liste des départements et ceux de chaque région : le même fichier que celui du jeu (Decoupage.java)
LISTE = Path(__file__).parent.parent / "src" / "main" / "resources" / "departements" / "departements.json"

# le nom de l'image -> le nom de la région dans les fichiers de contours, pour les cartes de fin de partie
CARTES_NOUVELLES = {
    "hauts-de-france.png": "Hauts-de-France",
    "normandie.png": "Normandie",
    "grand-est.png": "Grand Est",
    "bourgogne-franche-comte.png": "Bourgogne-Franche-Comté",
    "auvergne-rhone-alpes.png": "Auvergne-Rhône-Alpes",
    "nouvelle-aquitaine.png": "Nouvelle-Aquitaine",
    "occitanie.png": "Occitanie",
    "centre-val-de-loire.png": "Centre-Val de Loire",
    "bretagne.png": "Bretagne",
    "pays-de-la-loire.png": "Pays de la Loire",
    "ile-de-france.png": "Île-de-France",
    "provence-alpes-cote-dazur.png": "Provence-Alpes-Côte d'Azur",
    "corse.png": "Corse",
}
CARTES_ANCIENNES = {
    "nord-pas-de-calais.png": "Nord-Pas-de-Calais",
    "picardie.png": "Picardie",
    "haute-normandie.png": "Haute-Normandie",
    "basse-normandie.png": "Basse-Normandie",
    "poitou-charentes.png": "Poitou-Charentes",
    "centre.png": "Centre",
    "champagne-ardenne.png": "Champagne-Ardenne",
    "lorraine.png": "Lorraine",
    "alsace.png": "Alsace",
    "bourgogne.png": "Bourgogne",
    "franche-comte.png": "Franche-Comté",
    "rhone-alpes.png": "Rhône-Alpes",
    "auvergne.png": "Auvergne",
    "limousin.png": "Limousin",
    "aquitaine.png": "Aquitaine",
    "midi-pyrenees.png": "Midi-Pyrénées",
    "languedoc-roussillon.png": "Languedoc-Roussillon",
    # les régions inchangées ont une carte dans chaque époque
    "bretagne.png": "Bretagne",
    "pays-de-la-loire.png": "Pays de la Loire",
    "ile-de-france.png": "Île-de-France",
    "provence-alpes-cote-dazur.png": "Provence-Alpes-Côte d'Azur",
    "corse.png": "Corse",
}

# les couleurs des images d'origine du projet
BLEU_CLAIR = "#a2d1fb"
BLEU_FONCE = "#3a4bc4"
FOND_CARTE = "#fbf9f6"
TRAIT_CARTE = "#8c98b8"
TRAIT_DEPARTEMENT = "#7f97ad"
GRIS_DEFAUT = "#b7c3cf"

# sur les cartes, la Corse est rapprochée du continent, comme sur les cartes d'origine (en degrés)
DECALAGE_CORSE = (-0.9, 0.7)
# la latitude moyenne de la métropole : projection des cartes de France
LATITUDE_FRANCE = 46.5


def charger(dossier, fichier):
    with open(dossier / fichier, encoding="utf8") as f:
        return json.load(f)["features"]


def anneaux(geometrie):
    """Les contours extérieurs d'un Polygon ou d'un MultiPolygon, en (longitude, latitude)."""
    polygones = [geometrie["coordinates"]] if geometrie["type"] == "Polygon" else geometrie["coordinates"]
    return [polygone[0] for polygone in polygones]


def projeter(anneau, latitude, decalage=(0, 0)):
    """Projection simple : un degré de longitude est plus court qu'un degré de latitude, d'un facteur cos(latitude)."""
    facteur = math.cos(math.radians(latitude))
    dx, dy = decalage
    return [(lon * facteur + dx, lat + dy) for lon, lat in anneau]


def figure(formes, cote_max, marge_relative):
    """Une figure sans axes, cadrée sur les formes, dont le plus grand côté fait cote_max pixels."""
    xs = [x for forme in formes for x, _ in forme]
    ys = [y for forme in formes for _, y in forme]
    marge = marge_relative * max(max(xs) - min(xs), max(ys) - min(ys))
    largeur, hauteur = max(xs) - min(xs) + 2 * marge, max(ys) - min(ys) + 2 * marge
    echelle = cote_max / max(largeur, hauteur)
    dpi = 100
    fig = plt.figure(figsize=(largeur * echelle / dpi, hauteur * echelle / dpi), dpi=dpi)
    ax = fig.add_axes((0, 0, 1, 1))
    ax.set_xlim(min(xs) - marge, max(xs) + marge)
    ax.set_ylim(min(ys) - marge, max(ys) + marge)
    ax.set_aspect("equal")
    ax.axis("off")
    return fig, ax


def enregistrer(fig, chemin, flou):
    """Enregistre sur fond transparent, adouci par un léger flou comme les images d'origine."""
    chemin.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(chemin, transparent=True)
    plt.close(fig)
    # le flou se fait en couleurs « prémultipliées » (RGBa) : sinon le noir des pixels transparents
    # se mélangerait aux bords et ferait un liseré sombre
    image = Image.open(chemin).convert("RGBa").filter(ImageFilter.GaussianBlur(flou)).convert("RGBA")
    # 256 couleurs suffisent à ces aplats, et le fichier est quatre à cinq fois plus léger
    image.quantize(256, method=Image.Quantize.FASTOCTREE).save(chemin, optimize=True)


# les contours des départements : dans un carré de 2000 unités sur le plus grand côté, y vers le bas (comme à
# l'écran), en nombres entiers. Simplifiés : un point qui s'écarte de moins d'une unité de la ligne droite entre ses
# voisins n'apporte rien (une unité fait un cinquième de pixel quand la région est dessinée sur 400 pixels)
COTE_CONTOURS = 2000
TOLERANCE = 1.0
# l'Île-de-France est vue en entier, plus un zoom sur Paris et la petite couronne
ZOOM = {"ile-de-france": ["75", "92", "93", "94"]}


def tous_les_anneaux(geometrie):
    """Tous les contours d'un Polygon ou d'un MultiPolygon, trous compris (l'enclave des Papes, du Vaucluse, est un
    trou dans la Drôme : sans lui, un clic dans l'enclave tomberait dans les deux départements)."""
    polygones = [geometrie["coordinates"]] if geometrie["type"] == "Polygon" else geometrie["coordinates"]
    return [anneau for polygone in polygones for anneau in polygone]


def simplifier(points, tolerance):
    """L'algorithme de Douglas-Peucker : on garde les deux extrémités, puis le point le plus éloigné de la ligne qui
    les joint s'il s'en écarte de plus de la tolérance, et l'on recommence de chaque côté. Avec une pile plutôt
    qu'une fonction récursive : un contour de plusieurs milliers de points dépasserait la limite de Python."""
    garder = [False] * len(points)
    garder[0] = garder[-1] = True
    pile = [(0, len(points) - 1)]
    while pile:
        a, b = pile.pop()
        (xa, ya), (xb, yb) = points[a], points[b]
        longueur = math.hypot(xb - xa, yb - ya)
        loin, ecart = None, tolerance
        for i in range(a + 1, b):
            x, y = points[i]
            # distance du point à la droite (a, b) ; à a lui-même si a et b sont confondus (anneau fermé)
            d = (abs((xb - xa) * (ya - y) - (xa - x) * (yb - ya)) / longueur if longueur
                 else math.hypot(x - xa, y - ya))
            if d > ecart:
                loin, ecart = i, d
        if loin is not None:
            garder[loin] = True
            pile += [(a, loin), (loin, b)]
    return [point for point, garde in zip(points, garder) if garde]


def aire(points):
    """L'aire d'un polygone (formule du lacet), pour écarter les îlots trop petits pour être vus."""
    return abs(sum(x1 * y2 - x2 * y1 for (x1, y1), (x2, y2) in zip(points, points[1:] + points[:1]))) / 2


def traces(anneaux_ecran, tolerance):
    """Des contours déjà à l'échelle de l'écran -> les tracés à écrire : simplifiés, en nombres entiers, à plat
    (x0, y0, x1, y1…), sans les îlots trop petits pour être vus. Pour les départements comme pour la carte de France."""
    resultat = []
    for anneau in anneaux_ecran:
        points = simplifier(anneau, tolerance)
        points = [(round(x), round(y)) for x, y in points[:-1]]   # le dernier point répète le premier
        if len(points) >= 3 and aire(points) >= 4:
            resultat.append([valeur for point in points for valeur in point])
    return resultat


def contours(chemin, departements, codes_zoom):
    anneaux_par_code = {d["properties"]["code"]: tous_les_anneaux(d["geometry"]) for d in departements}
    tous = [anneau for liste in anneaux_par_code.values() for anneau in liste]
    latitude = sum(lat for anneau in tous for _, lat in anneau) / sum(len(anneau) for anneau in tous)
    projetes = {code: [projeter(anneau, latitude) for anneau in liste] for code, liste in anneaux_par_code.items()}
    xs = [x for liste in projetes.values() for anneau in liste for x, _ in anneau]
    ys = [y for liste in projetes.values() for anneau in liste for _, y in anneau]
    echelle = COTE_CONTOURS / max(max(xs) - min(xs), max(ys) - min(ys))

    def ecran(x, y):
        # la latitude monte vers le nord, l'écran descend : on retourne l'axe des y
        return (x - min(xs)) * echelle, (max(ys) - y) * echelle

    lignes = []
    points_ecrits = 0
    for d in departements:
        code = d["properties"]["code"]
        liste = traces([[ecran(x, y) for x, y in anneau] for anneau in projetes[code]], TOLERANCE)
        points_ecrits += sum(len(trace) // 2 for trace in liste)
        lignes.append(json.dumps({"code": code, "contours": liste}, separators=(",", ":")))

    entete = {"largeur": round((max(xs) - min(xs)) * echelle), "hauteur": round((max(ys) - min(ys)) * echelle)}
    if codes_zoom:
        # le cadre qui entoure ces départements, avec une marge
        points = [ecran(x, y) for code in codes_zoom for anneau in projetes[code] for x, y in anneau]
        x0, x1 = min(x for x, _ in points), max(x for x, _ in points)
        y0, y1 = min(y for _, y in points), max(y for _, y in points)
        marge = 0.06 * max(x1 - x0, y1 - y0)
        entete["zoom"] = [round(x0 - marge), round(y0 - marge), round(x1 - x0 + 2 * marge), round(y1 - y0 + 2 * marge)]
    # un département par ligne : un fichier lisible, et un diff qui montre quel département a changé
    texte = json.dumps(entete)[:-1] + ',\n"departements":[\n' + ",\n".join(lignes) + "\n]}\n"
    chemin.parent.mkdir(parents=True, exist_ok=True)
    chemin.write_text(texte, encoding="utf8", newline="\n")
    return points_ecrits


def silhouette(chemin, departements):
    contours = [anneau for d in departements for anneau in anneaux(d["geometry"])]
    # la projection se fait à la latitude de la région : la Guyane est près de l'équateur, le Nord bien plus haut
    latitude = sum(lat for anneau in contours for _, lat in anneau) / sum(len(anneau) for anneau in contours)
    formes = [projeter(anneau, latitude) for anneau in contours]
    fig, ax = figure(formes, 440, 0.12)
    # d'abord le remplissage (bord de la même couleur, pour ne pas laisser de jour entre deux départements),
    # puis les limites des départements par-dessus
    for forme in formes:
        ax.add_patch(Polygon(forme, closed=True, facecolor=BLEU_CLAIR, edgecolor=BLEU_CLAIR, linewidth=1.5))
    for forme in formes:
        ax.add_patch(Polygon(forme, closed=True, fill=False, edgecolor=TRAIT_DEPARTEMENT, linewidth=0.8))
    enregistrer(fig, chemin, 1.3)


def decalage(region):
    return DECALAGE_CORSE if region["properties"]["nom"] == "Corse" else (0, 0)


def carte(chemin, regions, nom_cible):
    formes = {}
    for region in regions:
        nom = region["properties"]["nom"]
        formes[nom] = [projeter(anneau, LATITUDE_FRANCE, decalage(region)) for anneau in anneaux(region["geometry"])]
    if nom_cible not in formes:
        raise ValueError(f"région inconnue dans les contours : {nom_cible}")
    # 600 pixels : l'image est affichée dans un cadre de 400 × 220 points, plus grand ne servirait à rien
    fig, ax = figure([forme for liste in formes.values() for forme in liste], 600, 0.05)
    for nom, liste in formes.items():
        cible = nom == nom_cible
        for forme in liste:
            ax.add_patch(Polygon(forme, closed=True, facecolor=BLEU_FONCE if cible else FOND_CARTE,
                                 edgecolor="white" if cible else TRAIT_CARTE, linewidth=1.6 if cible else 1.3,
                                 zorder=2 if cible else 1))
    enregistrer(fig, chemin, 0.6)


# la carte de France de la fin de partie, que le jeu dessine lui-même (CarteFin) pour colorier chaque région
# proposée : toutes les régions de métropole, nouvelles et anciennes, dans un même repère de 2000 unités sur le plus
# grand côté (une France que le jeu dessine sur 220 pixels : une unité y fait un dixième de pixel, d'où une tolérance
# de 2), puis les cinq régions d'outre-mer, chacune dans son propre repère : un encart, comme sur les cartes de l'INSEE
COTE_FRANCE = 2000
TOLERANCE_FRANCE = 2.0
COTE_ENCART = 400
# le département de chaque région d'outre-mer (une région d'un seul département), et le nom de son fichier
OUTRE_MER = {"971": "guadeloupe", "972": "martinique", "973": "guyane", "974": "la-reunion", "976": "mayotte"}


def france(chemin, nouvelles, anciennes, departements):
    # chaque région une seule fois, sous le nom de fichier du jeu (RegionFr.getFichier) : les régions inchangées en
    # 2016 sont prises dans les contours des nouvelles
    projetes = {}
    for images, liste in ((CARTES_NOUVELLES, nouvelles), (CARTES_ANCIENNES, anciennes)):
        par_nom = {region["properties"]["nom"]: region for region in liste}
        for image, nom in images.items():
            fichier = image.removesuffix(".png")
            if fichier not in projetes:
                region = par_nom[nom]
                projetes[fichier] = [projeter(anneau, LATITUDE_FRANCE, decalage(region))
                                     for anneau in tous_les_anneaux(region["geometry"])]
    xs = [x for liste in projetes.values() for anneau in liste for x, _ in anneau]
    ys = [y for liste in projetes.values() for anneau in liste for _, y in anneau]
    echelle = COTE_FRANCE / max(max(xs) - min(xs), max(ys) - min(ys))
    lignes = []
    points_ecrits = 0
    for fichier, liste in projetes.items():
        # la latitude monte vers le nord, l'écran descend : on retourne l'axe des y
        ecran = [[((x - min(xs)) * echelle, (max(ys) - y) * echelle) for x, y in anneau] for anneau in liste]
        trace = traces(ecran, TOLERANCE_FRANCE)
        points_ecrits += sum(len(t) // 2 for t in trace)
        lignes.append(json.dumps({"region": fichier, "contours": trace}, separators=(",", ":")))

    par_code = {d["properties"]["code"]: d for d in departements}
    encarts = []
    for code, fichier in OUTRE_MER.items():
        anneaux_dom = tous_les_anneaux(par_code[code]["geometry"])
        # projetée à sa propre latitude, comme sa silhouette : la Guyane est près de l'équateur
        latitude = sum(lat for anneau in anneaux_dom for _, lat in anneau) / sum(len(anneau) for anneau in anneaux_dom)
        liste = [projeter(anneau, latitude) for anneau in anneaux_dom]
        x0 = min(x for anneau in liste for x, _ in anneau)
        x1 = max(x for anneau in liste for x, _ in anneau)
        y0 = min(y for anneau in liste for _, y in anneau)
        y1 = max(y for anneau in liste for _, y in anneau)
        echelle_dom = COTE_ENCART / max(x1 - x0, y1 - y0)
        ecran = [[((x - x0) * echelle_dom, (y1 - y) * echelle_dom) for x, y in anneau] for anneau in liste]
        trace = traces(ecran, TOLERANCE)
        points_ecrits += sum(len(t) // 2 for t in trace)
        encarts.append(json.dumps({"region": fichier, "largeur": round((x1 - x0) * echelle_dom),
                                   "hauteur": round((y1 - y0) * echelle_dom), "contours": trace},
                                  separators=(",", ":")))

    # une région par ligne, comme pour les départements
    entete = {"largeur": round((max(xs) - min(xs)) * echelle), "hauteur": round((max(ys) - min(ys)) * echelle)}
    texte = (json.dumps(entete)[:-1] + ',\n"regions":[\n' + ",\n".join(lignes) + '\n],\n"outreMer":[\n'
             + ",\n".join(encarts) + "\n]}\n")
    chemin.parent.mkdir(parents=True, exist_ok=True)
    chemin.write_text(texte, encoding="utf8", newline="\n")
    return points_ecrits


def defaut(chemin, regions):
    formes = [projeter(anneau, LATITUDE_FRANCE, decalage(r)) for r in regions for anneau in anneaux(r["geometry"])]
    fig, ax = figure(formes, 440, 0.06)
    for forme in formes:
        ax.add_patch(Polygon(forme, closed=True, facecolor=GRIS_DEFAUT, edgecolor=GRIS_DEFAUT, linewidth=1.5))
    ax.text(2.4 * math.cos(math.radians(LATITUDE_FRANCE)), 46.6, "?", color="white", fontsize=110,
            fontweight="bold", ha="center", va="center")
    enregistrer(fig, chemin, 1.0)


def main():
    dossier, ressources = Path(sys.argv[1]), Path(sys.argv[2])
    departements = charger(dossier, "departements-avec-outre-mer.geojson")
    nouvelles = charger(dossier, "regions.geojson")
    anciennes = charger(dossier, "regions-avant-redecoupage-2015.geojson")
    with open(LISTE, encoding="utf8") as f:
        liste = json.load(f)
    par_code = {d["properties"]["code"]: d for d in departements}

    points = 0
    for region, codes in liste["regions"].items():
        # dans l'ordre de la liste, celui des numéros
        choisis = [par_code[code] for code in codes if code in par_code]
        if len(choisis) != len(codes):
            raise ValueError(f"{region} : département introuvable parmi {codes}")
        for d in choisis:
            if d["properties"]["nom"] != liste["departements"][d["properties"]["code"]]:
                raise ValueError(f"{d['properties']['code']} : le nom diffère de celui de {LISTE.name}")
        silhouette(ressources / "regions" / f"{region}.png", choisis)
        points += contours(ressources / "departements" / f"{region}.json", choisis, ZOOM.get(region))
    defaut(ressources / "regions" / "defaut.png", nouvelles)
    for image, nom in CARTES_NOUVELLES.items():
        carte(ressources / "cartes" / "nouvelles" / image, nouvelles, nom)
    for image, nom in CARTES_ANCIENNES.items():
        carte(ressources / "cartes" / "anciennes" / image, anciennes, nom)
    points_france = france(ressources / "france.json", nouvelles, anciennes, departements)
    print(f"{len(liste['regions']) + 1} silhouettes, {len(CARTES_NOUVELLES) + len(CARTES_ANCIENNES)} cartes, "
          f"{len(liste['regions'])} fichiers de contours ({points} points), la carte de France ({points_france} points)")


main()
