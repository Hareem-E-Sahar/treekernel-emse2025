package org.ces.cagt.commun;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 *
 * @author Mhamed
 */
public class FileCopier {

    /** Creates a new instance of FileCopier */
    public FileCopier() {
    }

    public boolean copier(String source, String nomFichierSource, java.io.File destination) {
        boolean resultat = false;
        OutputStream tmpOut;
        try {
            tmpOut = new BufferedOutputStream(new FileOutputStream(nomFichierSource + "001.tmp"));
            InputStream is = getClass().getResourceAsStream(source + nomFichierSource);
            int i;
            while ((i = is.read()) != -1) tmpOut.write(i);
            tmpOut.close();
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(new File(nomFichierSource + "001.tmp")).getChannel();
            out = new FileOutputStream(destination).getChannel();
            in.transferTo(0, in.size(), out);
            resultat = true;
        } catch (java.io.FileNotFoundException f) {
        } catch (java.io.IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        new File(nomFichierSource + "001.tmp").delete();
        return (resultat);
    }
}
