package packjacket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.DefaultListModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 * Provides some utilities to all classes.
 * @author Amandeep Grewal
 */
public class StaticUtils {

    /**
     * Counts how many occurrences there are of a string within another.
     * @param wordToFind The String to search for
     * @param string The String to search in
     * @return The number of occurrences.
     */
    public static int countOccurrencesOf(String wordToFind, String string) {
        int count = 0, index = 0;
        for (int A = 0; A < string.length(); A += (index - A) + wordToFind.length()) {
            index = string.indexOf(wordToFind, A);
            if (index == -1) break;
            count++;
        }
        return count;
    }

    /**
     * Returns all the indexes of elements in a model.
     * @param model The model to search in
     * @param sa The Strings to search for
     * @return The indexes of the elements
     */
    public static int[] indexesOf(DefaultListModel model, String sa[]) {
        int[] ia = new int[sa.length];
        for (int x = 0; x < sa.length; x++) ia[x] = model.indexOf(sa[x]);
        return ia;
    }

    /**
     * Figure out maximum number in array.
     * @param t All number to compare
     * @return Highest number
     */
    public static int max(int[] t) {
        int maximum = t[0];
        for (int i = 1; i < t.length; i++) if (t[i] > maximum) maximum = t[i];
        return maximum;
    }

    /**
     * Copyies a file from the source to the destination, byte for byte.
     * Outputfile will be created if not created, and if created will be overwritten.
     * @param src File copying from
     * @param dest File copying to
     * @throws java.io.IOException If either the source or the destination
     * is a directory, if the first byte cannot be read for any reason
     * other than the end of the file, or if some other I/O error occurs.
     * @throws java.lang.IllegalArgumentException If source or destiantion
     * is a directory.
     */
    public static void copyFile(File src, File dest) throws IOException, IllegalArgumentException {
        if (src.isDirectory()) throw new IllegalArgumentException("Source file is a directory");
        if (dest.isDirectory()) throw new IllegalArgumentException("Destination file is a directory");
        int bufferSize = 4 * 1024;
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0) out.write(buffer, 0, bytesRead);
        out.close();
        in.close();
    }

    /**
     * Copyies a file from the source to the destination, byte for byte.
     * Outputfile will be created if not created, and if created will be overwritten.
     * @param src File copying from
     * @param dest File copying to
     * @throws java.io.IOException If either the source or the destination
     * is a directory, if the first byte cannot be read for any reason
     * other than the end of the file, or if some other I/O error occurs.
     * @throws java.lang.IllegalArgumentException If source or destiantion
     * is a directory.
     */
    public static void copyFile(String src, String dest) throws IOException, IllegalArgumentException {
        copyFile(new File(src), new File(dest));
    }

    /**
     * Uploads a file to the PackJacket webserver
     * @param f the fiel to upload
     * @return the id of the log file, or 0 if it crashed
     */
    public static long uploadLog(File f) {
        long id = 0;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httppost = new HttpPost("http://packjacket.sourceforge.net/log/up.php");
            MultipartEntity mpEntity = new MultipartEntity();
            ContentBody cbFile = new FileBody(f, "image/jpeg");
            mpEntity.addPart("uploadedfile", cbFile);
            httppost.setEntity(mpEntity);
            RunnerClass.logger.info("executing request " + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            RunnerClass.logger.info("Status: " + response.getStatusLine());
            if (resEntity != null) {
                String s = EntityUtils.toString(resEntity);
                RunnerClass.logger.info("Result: " + s);
                id = Long.parseLong(s.replaceFirst("222 ", "").replace("\n", ""));
            }
            if (resEntity != null) resEntity.consumeContent();
            httpclient.getConnectionManager().shutdown();
        } catch (Exception ex) {
            RunnerClass.logger.info("Internet not available");
        }
        return id;
    }

    /**
     * GZips the main log file
     * @return the gzipped file
     * @throws IOException if any I/O error occurs
     */
    public static File gzipLog() throws IOException {
        RunnerClass.nfh.flush();
        File log = new File(RunnerClass.homedir + "pj.log");
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(new File(log.getCanonicalPath() + ".pjl")));
        FileInputStream in = new FileInputStream(log);
        int bufferSize = 4 * 1024;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0) out.write(buffer, 0, bytesRead);
        out.close();
        in.close();
        return new File(log.getCanonicalPath() + ".pjl");
    }
}
