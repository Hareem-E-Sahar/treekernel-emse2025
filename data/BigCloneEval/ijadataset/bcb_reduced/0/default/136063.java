import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Classe FormatCSVRouteLecteur<br/>
 * <b>FormatCSVRouteLecteur permet de mettre � jour les donn�es sur des routes.</b>
 * <p>
 * Cette classe est compos�e d'un constructeur par d�faut et d'une unique m�thode static 
 * qui permet de modifier les donn�es d'une route d�j� existante dans la m�moire du GPS.
 * @see Vecteur#setNom(String)
 * @see Vecteur#setVitesse(int)
 * @author groupe 2
 * @version 1.0

 *</p>
 *
 */
public class FormatCSVRouteLecteur {

    /**
	 * Constructeur de FormatCSVRouteLecteur<br/>
	 * <p>
	 * Constructeur sans param�tre, il cr�e uniquement un espace m�moire.
	 * </p>
	 */
    public FormatCSVRouteLecteur() {
    }

    /**
	 * A partir d'un fichier, g�n�ralement nomm�, {@code routes.csv}, lireInfo va pouvoir 
	 * extraire, ligne par ligne donc route par routes, les donn�es du fichier csv auxquelles
	 * il faut faire une mise � jour. Par mise � jour, on entend la modification du nom ou de 
	 * la vitessse r�glement�e.
	 * La mise � jour de la base de donn�es des routes ne sera effectu� seulement si la 
	 * r�f�rence de la route est d�j� pr�sente dans la m�moire du GPS.
	 * @param fichierCSV: le fichier .csv qui contient tous les routes, leur nom et la vitesse
	 * maximale � ne pas d�passer.
	 */
    public static void lireInfo(Scanner fichierCSV) {
        StringTokenizer text;
        String ligne = "";
        String id = "";
        while (fichierCSV.hasNextLine()) {
            int nb = 3;
            ligne = fichierCSV.nextLine();
            text = new StringTokenizer(ligne, ",");
            while (text.hasMoreTokens() && nb == 3) {
                id = text.nextToken();
                for (Vecteur vecteur : DonneesGPS.getL_Vecteurs()) {
                    if (vecteur.getId().equals(id)) {
                        vecteur.setNom(text.nextToken());
                        vecteur.setVitesse(Integer.parseInt(text.nextToken()));
                    }
                }
                nb--;
            }
        }
    }
}
