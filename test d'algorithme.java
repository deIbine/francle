test d'algorithme 

class main{

	public possibilitedelutilisateur (){
		tu connais, on lui donne des possibilites entre commentjouer, parametres, statistiques, faire des guess, ça me paraît bien 
	}
	
	public guess(){
	recuperer le guess de l'utilisateur

	} 

	public verifsens(){
	bon en gros c'est ce qui permettrait de tourner la flèche dans le bon sens 
	il y aurait genre 8 flèches maximum du coup dans les sens cardinaux, faut trouver une méthode MATHEMATIQUE STONK
	}

	public verifdistance(){
	normalement un simple calcul avec la latitude et la longitude 
	d'ailleurs, c'est fort possible qu'on doit mettre ses fonctions dans la class Region
	}

	public verifpourcentage(){
	même concept qu'au-dessus
	}
}

class Region{
	private String villeprincipale
	private int longitude
	private int latitude
	private int serie
	private int seriemax
	private moyennedistance




}

class Statistiques{
	ok donc c'est en bonus avec des stats à modifier en fonction des tests
	inspiration de wordle
	private int nombreparties
	private int nombresdepartiesgagnées


	public calculpourcentagevictoire(){
	calcul simple du pourcentage de victoire à envoyer si l'utilisateur demande des statistiques
	}

	public repartitionvictoire(){
	se débrouiller pour sauvegarder un tableau avec les victoires de l'utilisateur, il pourra savoir, quand il a gagné, s'il a gagné en un guess, deux guess, trois guess, etc... jusqu'à 6
	}

	possibilité de faire un reset ?
	possibilité d'avoir des utilisateurs différents ?
}

class Parametres{
	private int unite (0 = km, 1 = miles)
	private int theme (0 = light, 1 = dark)
	private int langage (a voir en fontion de notre ambition)
	private int modedejeu (choix possible entre les anciennes et les nouvelles régions par exemple, 0  = ancienne, 1 = nouvelle (pourquoi pas mettre les départements ?)


	public proximitecontretaille(){
	verifier ce que ça fait dans le jeu
	}

	public supprimerimage(){
	possibilite de supprimer l'image pour faire des guess plus dur
	}

	public sensimage(){
	possibilite de aleatoirement modifier le sens de l'image
	}
}

class commentjouer{

	si l'utilisateur utilise cette fonction, on lui donne une image qui expkique ce qu'il doit faire, s'inspirer du screen
}

class reussirafairelesgraphismesmagnifique{
	
}

class internet ?{
	
}
