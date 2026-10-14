"""Génère toutes les images du jeu à partir des contours officiels des départements et des régions.

Les images sont sur fond transparent : elles s'affichent aussi bien en thème clair qu'en thème sombre.

    regions/<image>             la silhouette de chaque région, départements tracés, montrée pendant la partie
    regions/defaut.png          la France métropolitaine et un point d'interrogation, quand la silhouette est désactivée
    cartes/nouvelles/<image>    la France des régions de 2016, la réponse en bleu (fin de partie, mode « nouvelles »)
    cartes/anciennes/<image>    la France d'avant 2016, la réponse en bleu (fin de partie, mode « anciennes »)

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

# le nom de l'image (celui de RegionFr) -> les départements de la région
DEPARTEMENTS = {
    # nouvelles régions (2016)
    "hauts-de-france.png": ["02", "59", "60", "62", "80"],
    "normandie.png": ["14", "27", "50", "61", "76"],
    "grand-est.png": ["08", "10", "51", "52", "54", "55", "57", "67", "68", "88"],
    "bourgogne-franche-comte.png": ["21", "25", "39", "58", "70", "71", "89", "90"],
    "auvergne-rhone-alpes.png": ["01", "03", "07", "15", "26", "38", "42", "43", "63", "69", "73", "74"],
    "nouvelle-aquitaine.png": ["16", "17", "19", "23", "24", "33", "40", "47", "64", "79", "86", "87"],
    "occitanie.png": ["09", "11", "12", "30", "31", "32", "34", "46", "48", "65", "66", "81", "82"],
    "centre-val-de-loire.png": ["18", "28", "36", "37", "41", "45"],
    # régions qui n'ont pas changé en 2016
    "bretagne.png": ["22", "29", "35", "56"],
    "pays-de-la-loire.png": ["44", "49", "53", "72", "85"],
    "ile-de-france.png": ["75", "77", "78", "91", "92", "93", "94", "95"],
    "provence-alpes-cote-dazur.png": ["04", "05", "06", "13", "83", "84"],
    "corse.png": ["2A", "2B"],
    "guadeloupe.png": ["971"],
    "martinique.png": ["972"],
    "guyane.png": ["973"],
    "la-reunion.png": ["974"],
    "mayotte.png": ["976"],
    # anciennes régions (avant 2016)
    "nord-pas-de-calais.png": ["59", "62"],
    "picardie.png": ["02", "60", "80"],
    "haute-normandie.png": ["27", "76"],
    "basse-normandie.png": ["14", "50", "61"],
    "poitou-charentes.png": ["16", "17", "79", "86"],
    "centre.png": ["18", "28", "36", "37", "41", "45"],
    "champagne-ardenne.png": ["08", "10", "51", "52"],
    "lorraine.png": ["54", "55", "57", "88"],
    "alsace.png": ["67", "68"],
    "bourgogne.png": ["21", "58", "71", "89"],
    "franche-comte.png": ["25", "39", "70", "90"],
    "rhone-alpes.png": ["01", "07", "26", "38", "42", "69", "73", "74"],
    "auvergne.png": ["03", "15", "43", "63"],
    "limousin.png": ["19", "23", "87"],
    "aquitaine.png": ["24", "33", "40", "47", "64"],
    "midi-pyrenees.png": ["09", "12", "31", "32", "46", "65", "81", "82"],
    "languedoc-roussillon.png": ["11", "30", "34", "48", "66"],
}

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


def defaut(chemin, regions):
    formes = [projeter(anneau, LATITUDE_FRANCE, decalage(r)) for r in regions for anneau in anneaux(r["geometry"])]
    fig, ax = figure(formes, 440, 0.06)
    for forme in formes:
        ax.add_patch(Polygon(forme, closed=True, facecolor=GRIS_DEFAUT, edgecolor=GRIS_DEFAUT, linewidth=1.5))
    ax.text(2.4 * math.cos(math.radians(LATITUDE_FRANCE)), 46.6, "?", color="white", fontsize=110,
            fontweight="bold", ha="center", va="center")
    enregistrer(fig, chemin, 1.0)


def main():
    contours, ressources = Path(sys.argv[1]), Path(sys.argv[2])
    departements = charger(contours, "departements-avec-outre-mer.geojson")
    nouvelles = charger(contours, "regions.geojson")
    anciennes = charger(contours, "regions-avant-redecoupage-2015.geojson")

    for image, codes in DEPARTEMENTS.items():
        choisis = [d for d in departements if d["properties"]["code"] in codes]
        if len(choisis) != len(codes):
            raise ValueError(f"{image} : département introuvable parmi {codes}")
        silhouette(ressources / "regions" / image, choisis)
    defaut(ressources / "regions" / "defaut.png", nouvelles)
    for image, nom in CARTES_NOUVELLES.items():
        carte(ressources / "cartes" / "nouvelles" / image, nouvelles, nom)
    for image, nom in CARTES_ANCIENNES.items():
        carte(ressources / "cartes" / "anciennes" / image, anciennes, nom)
    print(f"{len(DEPARTEMENTS) + 1} silhouettes, {len(CARTES_NOUVELLES) + len(CARTES_ANCIENNES)} cartes")


main()
