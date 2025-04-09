package clustering.implementations;

import java.io.*;
import java.util.zip.*;
import clustering.framework.*;

/**
 * @author Tudor.Ionescu@supelec.fr

ZIPCompressor

This class is a wrapper for the ZipOutputStream class from the Java 2 Framework (in the java.util.zip package). It is an implementation of the popular dictionary based ZIP compression method and has good performances in terms of speed and compression rate.

 */
public class ZIPCompressor implements ICompressor {

    public File Compress(File input_file) throws Exception {
        byte[] data = new byte[(int) input_file.length()];
        FileInputStream fis = new FileInputStream(input_file);
        fis.read(data);
        fis.close();
        File comp_file = new File(input_file.getAbsolutePath() + ".zip");
        FileOutputStream fos = new FileOutputStream(comp_file);
        ZipOutputStream zos = new ZipOutputStream(fos);
        zos.putNextEntry(new ZipEntry(input_file.getName()));
        zos.write(data);
        zos.close();
        fos.close();
        return new File(input_file.getAbsolutePath() + ".zip");
    }
}
