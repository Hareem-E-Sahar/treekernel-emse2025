package jcfs.benchmarks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import jcfs.core.fs.RFile;
import jcfs.core.fs.RFileInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * commons functions
 * @author enrico
 */
public class BenchCommons {

    protected static int ROUNDS = 20;

    protected String testId = System.nanoTime() + "";

    protected static void clean(File dir) {
        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                f.delete();
            }
        }
    }

    protected static byte[] genArray(int size) {
        byte[] res = new byte[size];
        Arrays.fill(res, (byte) 1);
        return res;
    }

    protected static void readFully(File file, int expected) throws IOException {
        int count = FileUtils.readFileToByteArray(file).length;
        if (count != expected) {
            throw new IOException("Expected " + expected + " bytes but read " + count);
        }
    }

    protected static void readFully(RFile file, int expected) throws IOException {
        InputStream in = new RFileInputStream(file);
        int count = IOUtils.toByteArray(in).length;
        in.close();
        if (count != expected) {
            throw new IOException("Expected " + expected + " bytes but read " + count);
        }
    }
}
