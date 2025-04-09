package archivage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import view.MainWindow;

public class ZipArchive {

    static final int BUFFER = 2048;

    /**
	 * Charge une archive dans un File[]
	 * @param mw
	 * @return les fichiers contenus dans l'archive
	 */
    public static File[] load(MainWindow mw) {
        JFileChooser filechooser = new JFileChooser();
        if (filechooser.showOpenDialog(mw) == JFileChooser.APPROVE_OPTION) {
            String filename = filechooser.getSelectedFile().getAbsolutePath();
            if (filename == null) return null;
            try {
                System.out.println("Loading: " + filename);
                BufferedOutputStream dest = null;
                BufferedInputStream is = null;
                ZipEntry entry;
                ZipFile zipfile = new ZipFile(filename);
                File[] tmp = new File[zipfile.size()];
                Enumeration e = zipfile.entries();
                int i = 0;
                while (e.hasMoreElements()) {
                    entry = (ZipEntry) e.nextElement();
                    System.out.println("Extracting: " + entry);
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    tmp[i] = new File(entry.getName());
                    tmp[i].deleteOnExit();
                    FileOutputStream fos = new FileOutputStream(tmp[i]);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                    i++;
                }
                return tmp;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
	 * Fonction qui sauvegarde le FileContener dans une archive
	 * @param mw
	 * @param filename
	 */
    public static void save(MainWindow mw, String filename) {
        if (filename != null) {
            try {
                BufferedInputStream origin = null;
                FileOutputStream dest;
                if (filename.endsWith(".cecco")) dest = new FileOutputStream(filename); else dest = new FileOutputStream(filename + ".cecco");
                System.out.println("Saving : " + filename);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                byte data[] = new byte[BUFFER];
                File[] files = new File[3];
                files[0] = FileContener.getOnto();
                files[1] = FileContener.getCurrentContr();
                files[2] = FileContener.getCurrentCacog();
                for (int i = 0; i < files.length; i++) {
                    System.out.println("Adding: " + files[i]);
                    FileInputStream fi = new FileInputStream(files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(files[i].getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
