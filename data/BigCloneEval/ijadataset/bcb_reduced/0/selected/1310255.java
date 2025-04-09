package net.jetrix.tools.patcher;

import java.io.*;
import java.util.zip.*;

/**
 * Computes released files' CRC32 and write them into the update.crc file.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 588 $, $Date: 2005-01-12 13:56:30 -0500 (Wed, 12 Jan 2005) $
 */
public class UpdateList {

    private static String path;

    private static PrintWriter out;

    public static void main(String[] argv) throws IOException {
        if (argv.length > 0) {
            path = argv[0];
        } else {
            path = ".";
        }
        out = new PrintWriter(new FileWriter(path + File.separator + "update.crc"), true);
        browseDirectory(new File(path));
        out.close();
    }

    /**
     * Recurse through directories and output files CRCs
     *
     * @param directory base directory
     */
    public static void browseDirectory(File directory) throws IOException {
        File listeFichiers[] = directory.listFiles();
        for (int i = 0; i < listeFichiers.length; i++) {
            File f = listeFichiers[i];
            if (f.isFile()) {
                String name = f.toString().substring(path.toString().length() + 1);
                if (!"update".equals(name)) {
                    out.println(name + "\t" + getFileCRC32(f) + "\t" + f.length());
                }
            } else {
                browseDirectory(f);
            }
        }
    }

    /**
     * Compute CRC32 for the specified file.
     *
     * @param file
     *
     * @return CRC32
     */
    public static long getFileCRC32(File file) throws IOException {
        if (file.exists() && file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            CRC32 check = new CRC32();
            int b = fis.read();
            while (b != -1) {
                b = fis.read();
                check.update(b);
            }
            fis.close();
            return check.getValue();
        } else {
            return 0;
        }
    }
}
