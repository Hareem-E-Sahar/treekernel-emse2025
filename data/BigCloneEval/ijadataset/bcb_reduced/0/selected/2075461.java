package com.izforge.izpack.compiler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *  Web installer class with the Kunststoff L&F support.
 *
 * @author     Julien Ponge
 */
public class WebKunststoffPackager extends WebPackager {

    /**
   *  The constructor.
   *
   * @param  outputFilename  The output filename.
   * @param  plistener       The packager listener.
   * @exception  Exception   Description of the Exception
   */
    public WebKunststoffPackager(String outputFilename, PackagerListener plistener) throws Exception {
        super(outputFilename, plistener);
        sendMsg("Copying the Kunststoff library ...");
        ZipInputStream skeleton_is = null;
        InputStream rawInput = getClass().getResourceAsStream("/lib/kunststoff.jar");
        if (rawInput == null) {
            skeleton_is = new JarInputStream(new FileInputStream(Compiler.IZPACK_HOME + "lib" + File.separator + "kunststoff.jar"));
        } else {
            skeleton_is = new ZipInputStream(rawInput);
        }
        ZipEntry zentry;
        while ((zentry = skeleton_is.getNextEntry()) != null) {
            if (zentry.isDirectory()) continue;
            outJar.putNextEntry(new ZipEntry(zentry.getName()));
            copyStream(skeleton_is, outJar);
            outJar.closeEntry();
            skeleton_is.closeEntry();
        }
    }

    /**
   *  Tells the packager to finish the job (misc writings, cleanups, closings ,
   *  ...).
   *
   * @exception  Exception  Description of the Exception
   */
    public void finish() throws Exception {
        DataOutputStream datOut;
        ObjectOutputStream objOut;
        int size;
        int i;
        sendMsg("Finishing the enpacking ...");
        outJar.putNextEntry(new ZipEntry("kind"));
        datOut = new DataOutputStream(outJar);
        datOut.writeUTF("web-kunststoff");
        datOut.flush();
        outJar.closeEntry();
        outJar.putNextEntry(new ZipEntry("packs.info"));
        objOut = new ObjectOutputStream(outJar);
        size = packs.size();
        objOut.writeInt(size);
        for (i = 0; i < size; i++) objOut.writeObject(packs.get(i));
        objOut.flush();
        outJar.closeEntry();
        outJar.putNextEntry(new ZipEntry("langpacks.info"));
        datOut = new DataOutputStream(outJar);
        size = langpacks.size();
        datOut.writeInt(size);
        for (i = 0; i < size; i++) datOut.writeUTF((String) langpacks.get(i));
        datOut.flush();
        outJar.closeEntry();
        outJar.flush();
        outJar.close();
        sendStop();
    }
}
