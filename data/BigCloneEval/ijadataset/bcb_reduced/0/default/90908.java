import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *  Classe LecteurCSV<br/>
 * <b>LecteurCSV est une classe fille de LecteurFichier.</b>
 * <p>
 * LecteurCSV h�rite de la classe LecteurFichier qui est une classe abstraite. Elle va donc
 * pouvoir utiliser tous ses attributs mais devoir red�finir sa m�thode {@code lireFichier()}.<br/>
 * <br/>Un attribut suppl�mentaire � la classe m�re:
 * <ul> <li>le nom du fichier dans lequel se fera la lecture, ce nom ne pourra pas �tre modifiable</li></ul>
 * @see LecteurFichier
 * @see LecteurFichier#lireFichier()
 * @see FormatCSVCarteNueLecteur
 * @see FormatCSVCroisementLecteur
 * @see FormatCSVRouteLecteur
 * </p>
 * 
 * @author groupe2
 * version 1.0
 */
public class LecteurCSV extends LecteurFichier {

    /**
	 * nom est une cha�ne de caract�re qui comporte le nom de fichier format csv dans 
	 * lequel on voudra lire nos donn�es.
	 */
    String nom;

    /**Constructeur de fichier csv � lire<br/>
	 * <p>
	 * Construction de fichier csv � partir de son nom.
	 * @param nom, le nom dans le lequel le fichier va lire.
	 * </p>
	 */
    public LecteurCSV(String Nom) {
        this.nom = Nom;
    }

    /** 
	 * <p>
	 *  lireFichier pourra lire dans un fichier au format csv des �l�ments du GPS de type route,
	 * carte nue, route ou croisement.<br/>
	 * Tout d'abord, lireFichier analyse l'ent�te du fichier qu'on veut lire. En fonction de
	 * son ent�te, s'il s'agit d'une carte vierge, de croisements ou de routes, lireFichier va
	 * appeler les m�thodes static lireInfo() des classes FormatCSV...Lecteur pour d�l�guer
	 * son travail. Ainsi il peut lire tous les fichiers au format csv.
	 * Il est possible que lireFichier() peut lever une {@code FileNotFoundException} si 
	 * le fichier rentr� n'est pas {@code cartenue.csv}, {@code route.csv} ou {@code croisements.csv}.
	 * vers le fichier csv a mal �t� cr��.
	 * </p>
	 * @see LecteurFichier#LireFichier()
	 * @see EnteteCSV#entete(String)
	 * @see FormatCSVCarteNueLecteur#lireInfo(Scanner)
	 * @see FormatCSVRouteLecteur#lireInfo(Scanner)
	 * @see FormatCSVCroisementLecteur#lireInfo(Scanner)
	 */
    @Override
    void lireFichier() {
        try {
            String ligneCSV = "";
            this.file = new File(nom);
            this.scan = new Scanner(file);
            if (this.scan.hasNextLine()) ligneCSV += this.scan.nextLine();
            StringTokenizer firstLigne = new StringTokenizer(ligneCSV, "\n");
            int resultat = 0;
            if (firstLigne.hasMoreTokens()) resultat = EnteteCSV.entete(firstLigne.nextToken());
            if (resultat == 1) {
                FormatCSVCarteNueLecteur.lireInfo(scan);
            } else if (resultat == 2) {
                FormatCSVRouteLecteur.lireInfo(scan);
            } else if (resultat == 3) {
                FormatCSVCroisementLecteur.lireInfo(scan);
            } else System.out.println("Mauvaise entete de fichier");
            this.scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
