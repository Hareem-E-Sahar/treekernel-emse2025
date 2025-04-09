package org.patterncoder.utils;

import java.io.*;

/**
 *
 * @author Florian Siebler 
 */
public class Utils {

    private static final String LINE_FEED = System.getProperty("line.separator");

    private Utils() {
    }

    /**
     * Liest eine UTF8-codierte Datei
     *
     * @param textFileName Name der zu lesenden Datei
     * @return Inhalt der Datei
     * @throws IOException
     */
    public static String readUTF8(String textFileName) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        Reader reader = new InputStreamReader(new FileInputStream(textFileName), "UTF8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line).append(LINE_FEED);
        }
        bufferedReader.close();
        return content.toString();
    }

    /**
     * Schreibt eine UTF8-codierte Dtaei
     *
     * @param textFileName Name der zu erstellenden Datei
     * @param content Inhalt der Datei
     * @throws IOException
     */
    public static void writeUTF8(String textFileName, String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(textFileName);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter wr = new BufferedWriter(osw);
        wr.write(content);
        wr.close();
        osw.close();
        fos.close();
    }

    /**
     * Liest eine binaere Datei
     *
     * @param fileName Name der Datei
     * @return Inhalt der Datei
     * @throws IOException
     */
    public static byte[] getBinaryFileContent(String fileName) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileName));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bs);
        byte[] ioBuf = new byte[4096];
        int bytesRead;
        while ((bytesRead = bufferedInputStream.read(ioBuf)) != -1) {
            out.write(ioBuf, 0, bytesRead);
        }
        out.close();
        bufferedInputStream.close();
        return bs.toByteArray();
    }

    /**
     * Schreibt eine binaere Datei
     *
     * @param fileName Name der Datei
     * @param content Inhalt der zu schreibenden Datei
     * @throws IOException
     */
    public static void writeBinaryFileContent(String fileName, byte[] content) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(content));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
        byte[] ioBuf = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(ioBuf)) != -1) {
            out.write(ioBuf, 0, bytesRead);
        }
        out.close();
        in.close();
    }

    /**
     * Kopiert eine Datei
     *
     * @param srcName
     * @param dstName
     * @throws IOException
     */
    public static void copyFile(String srcName, String dstName) throws IOException {
        InputStream in = new FileInputStream(srcName);
        OutputStream out = new FileOutputStream(dstName);
        byte[] ioBuf = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(ioBuf)) != -1) {
            out.write(ioBuf, 0, bytesRead);
        }
        out.close();
        in.close();
    }

    /**
     * Liest eine Text-Datei mit Default-Zeichensatz-Codierung
     *
     * @param textFileName
     * @return
     * @throws IOException
     */
    public static String getTextFileContent(String textFileName) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(textFileName));
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line).append(LINE_FEED);
        }
        bufferedReader.close();
        return content.toString();
    }
}
