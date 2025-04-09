package cat.jm.cru.common;

import cat.jm.cru.exception.CRUException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static File zip(String input, String output) throws CRUException {
        if (input == null) throw new IllegalArgumentException("Input file can't be null!");
        if (output == null) throw new IllegalArgumentException("Output file can't be null!");
        File inputFile = new File(input);
        File outputFile = new File(output);
        return zip(inputFile, outputFile);
    }

    public static File zip(File input, File output) throws CRUException {
        if (input == null) throw new IllegalArgumentException("Input file can't be null!");
        if (output == null) throw new IllegalArgumentException("Output file can't be null!");
        if (!input.exists()) throw new IllegalArgumentException("Input file must be exists!");
        if (output.exists()) throw new IllegalArgumentException("Output file already exists!");
        return doZip(input, output);
    }

    private static File doZip(File input, File output) throws CRUException {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(output));
            doZip(input, out, true);
            out.flush();
            return output;
        } catch (FileNotFoundException e) {
            throw new CRUException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CRUException(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void doZip(File input, ZipOutputStream out, boolean root) throws IOException {
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    doZip(file, out, false);
                } else {
                    String fileName;
                    if (root) fileName = file.getName(); else fileName = input.getName() + File.separatorChar + file.getName();
                    ZipEntry entry = new ZipEntry(fileName);
                    doZip(entry, file, out);
                }
            }
        } else {
            ZipEntry entry = new ZipEntry(input.getName());
            doZip(entry, input, out);
        }
    }

    private static void doZip(ZipEntry entry, File file, ZipOutputStream out) throws IOException {
        out.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int buff = 0;
        while (0 < (buff = fis.read(buffer))) {
            out.write(buffer, 0, buff);
        }
        fis.close();
        out.closeEntry();
        out.flush();
    }

    public static void main(String arg[]) throws CRUException {
        String input = "c:/test";
        String output = "c:/test.zip";
        ZipUtils.zip(input, output);
    }
}
