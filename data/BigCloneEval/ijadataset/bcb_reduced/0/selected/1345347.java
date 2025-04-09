package test.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.exist.restlet.WebComponent;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 *
 * @author alex
 */
public class AccessTest extends TestCase {

    public AccessTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    void copy(File in, File out) throws IOException {
        FileInputStream is = new FileInputStream(in);
        FileOutputStream os = new FileOutputStream(out);
        byte[] buffer = new byte[16384];
        int len;
        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }
        is.close();
        os.close();
    }

    public static boolean deltree(File dir) {
        final List<File> queue = new ArrayList<File>();
        queue.add(dir);
        boolean ok = true;
        int mark = -1;
        while (ok && queue.size() > 0) {
            File target = queue.remove(queue.size() - 1);
            if (target.isDirectory()) {
                if (mark == queue.size()) {
                    ok = target.delete();
                    mark = -1;
                } else {
                    mark = queue.size();
                    queue.add(target);
                    target.listFiles(new FileFilter() {

                        public boolean accept(File f) {
                            queue.add(f);
                            return false;
                        }
                    });
                }
            } else {
                ok = target.delete();
            }
        }
        return ok;
    }

    Request put(String ref, Representation entity) {
        Request request = new Request(Method.PUT, new Reference(ref));
        request.setEntity(entity);
        return request;
    }

    Request post(String ref, Representation entity) {
        Request request = new Request(Method.POST, new Reference(ref));
        request.setEntity(entity);
        return request;
    }

    Request get(String ref) {
        Request request = new Request(Method.GET, new Reference(ref));
        return request;
    }

    public void testAccess() throws Exception {
        File dir = new File("test.out/access/access");
        if (dir.exists()) {
            deltree(dir);
        }
        if (!dir.mkdirs()) {
            throw new Exception("Cannot make directory " + dir.getAbsolutePath());
        }
        String[] dirs = { "data", "logs" };
        for (int i = 0; i < dirs.length; i++) {
            File dataDir = new File(dir, dirs[i]);
            if (!dataDir.mkdir()) {
                throw new Exception("Cannot make directory " + dataDir.getAbsolutePath());
            }
        }
        String[] files = { "catalog.xml", "log4j.xml", "log4j.dtd", "conf.xml" };
        for (int i = 0; i < files.length; i++) {
            copy(new File(files[i]), new File(dir, files[i]));
        }
        URL url = this.getClass().getResource("test-server.xml");
        WebComponent www = new WebComponent(url.toString());
        www.addDatabase("db", new File(dir, "conf.xml"));
        www.start();
        Client client = www.getContext().getClientDispatcher();
        Logger log = Logger.getLogger(AccessTest.class.getName());
        MediaType queryType = MediaType.valueOf("application/xquery");
        for (int i = 0; i < 1000; i++) {
            log.info("PUT " + i);
            String docRef = "riap://component/exist/db/" + i + "/test.xml";
            String otherRef = "riap://component/exist/db/" + i + "/other.xml";
            Response response = client.handle(put(docRef, new StringRepresentation("<test id='" + i + "'><target/></test>", MediaType.APPLICATION_XML)));
            if (response.isEntityAvailable()) {
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
            response = client.handle(get(docRef));
            if (response.isEntityAvailable()) {
                log.info("GET " + i + " " + response.getEntity().getText());
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
            response = client.handle(put(otherRef, new StringRepresentation("<other id='" + i + "'><target/></other>", MediaType.APPLICATION_XML)));
            if (response.isEntityAvailable()) {
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
            response = client.handle(get(otherRef));
            if (response.isEntityAvailable()) {
                log.info("GET " + i + " " + response.getEntity().getText());
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
            response = client.handle(post(docRef, new StringRepresentation("update replace /test/target with <updated>" + System.currentTimeMillis() + "</updated>", queryType)));
            if (response.isEntityAvailable()) {
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
            response = client.handle(get(docRef));
            if (response.isEntityAvailable()) {
                log.info("GET " + i + " " + response.getEntity().getText());
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
        }
        www.stop();
    }
}
