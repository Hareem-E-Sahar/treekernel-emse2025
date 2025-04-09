package test.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.exist.restlet.WebComponent;
import org.exist.restlet.XMLDBResource;
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
public class QueryTest extends TestCase {

    long startTime = 0;

    public QueryTest(String testName) {
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

    void startTimer() {
        startTime = System.currentTimeMillis();
    }

    long endTimer() {
        return System.currentTimeMillis() - startTime;
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

    public void testQuery() throws Exception {
        File dir = new File("test.out/access/query");
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
        Logger log = Logger.getLogger(QueryTest.class.getName());
        log.info("Loading content into database...");
        MediaType queryType = MediaType.valueOf("application/xquery");
        String[] tagNames = { "x", "y", "z", "x", "y", "z", "x", "x", "y", "z" };
        String[] childTagNames = { "a", "a", "a", "b", "b", "b", "c", "c", "c", "a" };
        for (int i = 0; i < tagNames.length; i++) {
            String docRef = "riap://component/exist/db/d" + i + ".xml";
            Response response = client.handle(put(docRef, new StringRepresentation("<doc><" + tagNames[i] + "><" + childTagNames[i] + "/></" + tagNames[i] + "></doc>", MediaType.APPLICATION_XML)));
            if (response.isEntityAvailable()) {
                response.getEntity().release();
            }
            assertTrue(response.getStatus().isSuccess());
        }
        Response response = client.handle(post("riap://component/exist/db/", new StringRepresentation("<doc>{//a}</doc>", queryType)));
        if (response.isEntityAvailable()) {
            log.info(response.getEntityAsText());
            response.getEntity().release();
        }
        assertTrue(response.getStatus().isSuccess());
        String xqueryRef = "riap://component/exist/db/query.xq";
        String xquery = "<doc>{//a}</doc>";
        response = client.handle(put(xqueryRef, new StringRepresentation(xquery, queryType)));
        if (response.isEntityAvailable()) {
            response.getEntity().release();
        }
        assertTrue(response.getStatus().isSuccess());
        log.info("DB located XQuery tests: ");
        Request request = get("riap://component/exist/db/");
        request.getAttributes().put(XMLDBResource.XQUERY_NAME, xqueryRef);
        for (int i = 0; i < 5; i++) {
            startTimer();
            response = client.handle(request);
            if (response.isEntityAvailable()) {
                log.info(response.getEntityAsText());
                response.getEntity().release();
            }
            log.info(i + ": elapsed=" + endTimer());
            assertTrue(response.getStatus().isSuccess());
        }
        log.info("File located XQuery tests: ");
        File xqueryFile = new File(dir, "query.xq");
        FileWriter out = new FileWriter(xqueryFile);
        out.write(xquery);
        out.close();
        request.getAttributes().put(XMLDBResource.XQUERY_NAME, xqueryFile.toURI().toString());
        for (int i = 0; i < 5; i++) {
            startTimer();
            response = client.handle(request);
            if (response.isEntityAvailable()) {
                log.info(response.getEntityAsText());
                response.getEntity().release();
            }
            log.info(i + ": elapsed=" + endTimer());
            assertTrue(response.getStatus().isSuccess());
        }
        www.stop();
    }
}
