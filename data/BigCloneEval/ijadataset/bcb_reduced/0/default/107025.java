import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;

public class MiGClient {

    private static HttpClient httpclient = null;

    @SuppressWarnings("deprecation")
    private String response;

    MiGClient() {
        try {
            Protocol.registerProtocol("https", new Protocol("https", new MiGSSLSocketFactory(), 443));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpclient = new HttpClient();
    }

    public static void main(String[] args) throws HttpException, IOException, GeneralSecurityException {
        MiGClient client = new MiGClient();
        MigJob myjob = new MigJob("NQueenJob boards/zomghats", "zomghats.mrsl");
        client.submitJob(myjob);
    }

    public void listDir(String path) {
        GetMethod httpget = new GetMethod("https://mig-1.imada.sdu.dk/cgi-bin/ls.py?flags=a&with_html=false");
        try {
            httpclient.executeMethod(httpget);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpget.releaseConnection();
        }
    }

    public void upload(Serializable object, String destination, String filename) {
        upload(object.toString(), destination, filename, "");
    }

    public void upload(String content, String destination, String filename, String contenttype) {
        PutMethod httpput = new PutMethod("https://mig-1.imada.sdu.dk/" + destination + filename);
        StringRequestEntity entity = null;
        try {
            entity = new StringRequestEntity(content);
            httpput.setRequestEntity(entity);
            if (contenttype != "") httpput.setRequestHeader("Content-Type", contenttype);
            httpclient.executeMethod(httpput);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpput.releaseConnection();
        }
    }

    public void submitJob(MigJob job) {
        upload(job.toString(), "", job.getFilename(), "submitmrsl");
    }

    public void submitAndExtract(Queue<Board2> boards, String destination, String filename, int maxsteps, String rec) {
        PutMethod httpput = new PutMethod("https://mig-1.imada.sdu.dk/" + destination + filename);
        ZipOutputStream out = null;
        ObjectOutputStream objout = null;
        try {
            int count = 0;
            int filecount = 0;
            for (Board2 board : boards) {
                if ((count % 1000) == 0) {
                    filecount++;
                    if (count != 0) {
                        out.close();
                    }
                    out = new ZipOutputStream(new FileOutputStream("NQ-" + board.size + "-" + maxsteps + "-" + filecount + ".zip"));
                }
                out.putNextEntry(new ZipEntry("board" + board.size + "-" + maxsteps + "-" + rec + "-" + count + ".obj"));
                objout = new ObjectOutputStream(out);
                objout.writeObject(board);
                out.closeEntry();
                MigJob job = new MigJob("NQueenJob boards/board" + board.size + "-" + maxsteps + "-" + rec + "-" + count + ".obj", "board" + count + ".mrsl");
                out.putNextEntry(new ZipEntry("board" + board.size + "-" + count + ".mrsl"));
                out.write(job.toString().getBytes());
                out.closeEntry();
                count++;
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpput.releaseConnection();
        }
    }
}
