package net.lukemurphey.nsia.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import com.martiansoftware.jsap.JSAPException;
import net.lukemurphey.nsia.Application;
import net.lukemurphey.nsia.GenericUtils;
import net.lukemurphey.nsia.NoDatabaseConnectionException;

public class TestApplication {

    private static Application app = null;

    public static String DEFAULT_TEST_DATABASE_PATH = "tmp/test_database";

    public static Application getApplication() throws TestApplicationException {
        return getApplication(false);
    }

    /**
	 * Get the given property from the local properties file.
	 * @param name
	 * @return
	 * @throws IOException
	 */
    public static String getProperty(String name) throws IOException {
        File propsFile = new File("dev/local.properties");
        if (propsFile.exists()) {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFile);
            props.load(fis);
            fis.close();
            return props.getProperty(name, null);
        }
        return null;
    }

    public static synchronized Application getApplication(boolean startServices) throws TestApplicationException {
        try {
            if (app == null) {
                createDatabaseCopy();
                app = new Application(TestResources.getBaseDir() + DEFAULT_TEST_DATABASE_PATH, startServices);
            }
        } catch (IOException e) {
            throw new TestApplicationException(e);
        } catch (NoDatabaseConnectionException e) {
            throw new TestApplicationException(e);
        } catch (JSAPException e) {
            throw new TestApplicationException(e);
        }
        return app;
    }

    public static synchronized void stopApplication() {
        if (app != null) {
            app.shutdown(true);
            app = null;
        }
    }

    private static void createDatabaseCopy() throws IOException {
        deleteDatabase();
        copyDirectory(new File(TestResources.getBaseDir() + "dev/test/test_database"), new File(TestResources.getBaseDir() + DEFAULT_TEST_DATABASE_PATH), true);
    }

    private static void deleteDatabase() {
        File test_db = new File(TestResources.getBaseDir() + DEFAULT_TEST_DATABASE_PATH);
        GenericUtils.deleteDirectory(test_db);
    }

    private static void copyDirectory(File sourceLocation, File targetLocation, boolean ignoreDotFiles) throws IOException {
        if (ignoreDotFiles && sourceLocation.getName().startsWith(".")) {
            return;
        }
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), ignoreDotFiles);
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
