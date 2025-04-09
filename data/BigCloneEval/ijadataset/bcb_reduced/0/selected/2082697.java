package algutil.fichier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ConversionFichier {

    /**
	 * Convertie la taille en octets en une cha�ne formatt�e. Ex : 452 octets,
	 * 7.2 ko, 55.4 mo
	 */
    public static String tailleOctets2String(long taille) {
        String format = "";
        if (taille < 1024) {
            format = taille + " octets";
        } else if ((float) taille / 1024 < 1024) {
            format = (float) taille / 1024 + "";
            if (format.indexOf(".") != -1) {
                if (format.indexOf(".") + 2 <= format.length()) {
                    format = format.substring(0, format.indexOf(".") + 2);
                }
            }
            format += " ko";
        } else {
            format = (float) taille / 1024 / 1024 + "";
            if (format.indexOf(".") != -1) {
                if (format.indexOf(".") + 2 <= format.length()) {
                    format = format.substring(0, format.indexOf(".") + 2);
                }
            }
            format += " mo";
        }
        return format;
    }

    public static void zipper(File fileIN, File fileOUT) throws IOException {
        byte[] buf = new byte[1024];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileOUT));
        FileInputStream in = new FileInputStream(fileIN);
        out.putNextEntry(new ZipEntry(fileIN.getPath()));
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
        out.close();
    }

    public static void deZipper(File fileZipIN, File fileOUT) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZipIN));
        zis.getNextEntry();
        FileOutputStream fos = new FileOutputStream(fileOUT);
        int lu = -1;
        byte[] tampon = new byte[1024];
        do {
            lu = zis.read(tampon);
            if (lu > 0) fos.write(tampon, 0, lu);
        } while (lu > 0);
        fos.flush();
        fos.close();
        zis.close();
    }

    public static String convertTextFile2String(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer("");
        String ligne = null;
        while ((ligne = br.readLine()) != null) {
            sb.append(ligne).append(" ");
        }
        return sb.toString().trim();
    }
}
