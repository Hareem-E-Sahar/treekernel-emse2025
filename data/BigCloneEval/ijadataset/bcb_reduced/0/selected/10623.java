package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OperationsFichiers {

    /** Pour copier un fichier (le contenu d'un fichier)
	 * @param source : fichier source
	 * @param dest   : fichier destination
	 * @return       : résultat
	 */
    public static boolean copyFile(File source, File dest) {
        try {
            FileInputStream sourceFile = new FileInputStream(source);
            try {
                FileOutputStream destinationFile = null;
                try {
                    destinationFile = new FileOutputStream(dest);
                    byte buffer[] = new byte[512 * 1024];
                    int nbLecture;
                    while ((nbLecture = sourceFile.read(buffer)) != -1) {
                        destinationFile.write(buffer, 0, nbLecture);
                    }
                } finally {
                    destinationFile.close();
                }
            } finally {
                sourceFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** Pour supprimer tout ce que contient un dossier récursivement (fichiers, dossiers)
	 * @param f : le nom du dossier
	 */
    public static void deleteContentsOfDirectory(File f) {
        File l[] = f.listFiles();
        for (int i = 0; i < l.length; i++) {
            if (l[i].isDirectory()) {
                deleteContentsOfDirectory(l[i]);
                l[i].delete();
            } else l[i].delete();
        }
    }

    /** Pour supprimer tout un dossier récursivement (fichiers, dossiers)
	 * @param f : le nom du dossier
	 */
    public static void deleteDirectory(File f) {
        if (f.exists()) {
            deleteContentsOfDirectory(f);
            f.delete();
        }
    }
}
