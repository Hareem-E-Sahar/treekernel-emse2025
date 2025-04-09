package upmc.pstl.fw4exAuthenticated.util;

import java.io.*;
import java.util.zip.*;

public class FilesZipUtils {

    public FilesZipUtils() {
        super();
    }

    /**
	  * compresse un fichier
	  * 
	  * @param src
	  *            le fichier a compresser
	  * @param dest
	  *            la destination du fichier decompress�
	  * @throws ZipException
	  * @throws IOException
	  */
    public static void compress(File src, File dest) throws ZipException, IOException {
        FileOutputStream fout = new FileOutputStream(dest);
        ZipOutputStream zout = new ZipOutputStream(fout);
        ZipEntry ze = new ZipEntry(src.getName());
        zout.putNextEntry(ze);
        FileInputStream in = new FileInputStream(src);
        byte[] tab = new byte[4096];
        int lu = -1;
        do {
            lu = in.read(tab);
            if (lu > 0) zout.write(tab, 0, lu);
        } while (lu > 0);
        zout.finish();
        in.close();
        zout.closeEntry();
        zout.close();
        fout.close();
        in.close();
    }

    /**
	  * decompresse un fichier
	  * 
	  * @param fos
	  *            fichier a decompresser
	  * @param dest
	  *            cible
	  * @throws IOException
	  */
    public static void decompress(String fos, File dest) throws IOException {
        FileOutputStream fout = new FileOutputStream(dest);
        FileInputStream in = new FileInputStream(fos);
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry zen = zin.getNextEntry();
        byte[] tab = new byte[4096];
        int lu = -1;
        do {
            lu = zin.read(tab);
            if (lu > 0) fout.write(tab, 0, lu);
        } while (lu > 0);
        fout.flush();
        zin.closeEntry();
        zin.close();
        fout.close();
        fout.close();
        in.close();
    }
}
