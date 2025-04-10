package org.dspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.dspace.administer.RegistryImportException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.browse.BrowseException;
import org.dspace.content.NonUniqueMetadataException;
import org.dspace.servicemanager.DSpaceKernelImpl;
import org.dspace.servicemanager.DSpaceKernelInit;
import org.junit.*;
import static org.junit.Assert.*;
import mockit.*;
import org.apache.log4j.Logger;
import org.dspace.administer.MetadataImporter;
import org.dspace.administer.RegistryLoader;
import org.dspace.browse.IndexBrowse;
import org.dspace.browse.MockBrowseCreateDAOOracle;
import org.dspace.content.MetadataField;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.search.DSIndexer;
import org.dspace.storage.rdbms.MockDatabaseManager;
import org.xml.sax.SAXException;

/**
 * This is the base class for Unit Tests. It contains some generic mocks and
 * utilities that are needed by most of the unit tests developed for DSpace
 *
 * @author pvillega
 */
@UsingMocksAndStubs({ MockDatabaseManager.class, MockBrowseCreateDAOOracle.class })
public class AbstractUnitTest {

    /** log4j category */
    private static Logger log = Logger.getLogger(AbstractUnitTest.class);

    /**
     * Test properties
     */
    protected static Properties testProps;

    /**
     * Context mock object to use in the tests
     */
    protected Context context;

    /**
     * EPerson mock object to use in the tests
     */
    protected static EPerson eperson;

    /**
     * This method will be run before the first test as per @BeforeClass. It will
     * initialize resources required for the tests.
     *
     * Due to the way Maven works, unit tests can't be run from a POM package,
     * which forbids us to run the tests from the Assembly and Configuration
     * package. On the other hand we need a structure of folders to run the tests,
     * like "solr", "report", etc.
     *
     * This method will create all the folders and files required for the tests
     * in the test folder. To facilitate the work this will consist on a copy
     * from a folder in the resources folder. The ConfigurationManager will be
     * initialized to load the test "dspace.cfg" and then launch the unit tests.
     */
    @BeforeClass
    public static void initOnce() {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Dublin"));
            testProps = new Properties();
            URL properties = AbstractUnitTest.class.getClassLoader().getResource("test-config.properties");
            testProps.load(properties.openStream());
            URL origin = ClassLoader.getSystemResource("dspaceFolder");
            File source = new File(origin.getPath());
            File dspaceTmp = new File(testProps.getProperty("test.folder"));
            if (!dspaceTmp.exists()) {
                dspaceTmp.mkdirs();
            }
            copyDir(source, dspaceTmp);
            File tmpFile = new File(testProps.getProperty("test.bitstream"));
            File destParent = new File(testProps.getProperty("test.folder.assetstore"));
            destParent.mkdirs();
            File dest = new File(testProps.getProperty("test.assetstore.bitstream"));
            if (!dest.exists()) {
                dest.createNewFile();
            }
            copyFile(tmpFile, dest);
            URL configFile = AbstractUnitTest.class.getClassLoader().getResource(testProps.getProperty("test.config.file"));
            ConfigurationManager.loadConfig(configFile.getPath());
            DSpaceKernelImpl kernelImpl = null;
            try {
                kernelImpl = DSpaceKernelInit.getKernel(null);
                if (!kernelImpl.isRunning()) {
                    kernelImpl.start(ConfigurationManager.getProperty("dspace.dir"));
                }
            } catch (Exception e) {
                try {
                    if (kernelImpl != null) {
                        kernelImpl.destroy();
                    }
                } catch (Exception e1) {
                }
                String message = "Failure during filter init: " + e.getMessage();
                throw new IllegalStateException(message, e);
            }
            Context ctx = new Context();
            ctx.turnOffAuthorisationSystem();
            if (MetadataField.find(ctx, 1) == null) {
                String base = testProps.getProperty("test.folder") + File.separator + "config" + File.separator + "registries" + File.separator;
                RegistryLoader.loadBitstreamFormats(ctx, base + "bitstream-formats.xml");
                MetadataImporter.loadRegistry(base + "dublin-core-types.xml", true);
                MetadataImporter.loadRegistry(base + "sword-metadata.xml", true);
                ctx.commit();
                eperson = EPerson.find(ctx, 1);
                if (eperson == null) {
                    eperson = EPerson.create(ctx);
                    eperson.setFirstName("first");
                    eperson.setLastName("last");
                    eperson.setEmail("test@email.com");
                    eperson.setCanLogIn(true);
                }
                DSIndexer.cleanIndex(ctx);
                DSIndexer.createIndex(ctx);
                ctx.commit();
                IndexBrowse indexer = new IndexBrowse(ctx);
                indexer.setRebuild(true);
                indexer.setExecute(true);
                indexer.initBrowse();
            }
            ctx.restoreAuthSystemState();
            if (ctx.isValid()) {
                ctx.complete();
            }
            ctx = null;
        } catch (BrowseException ex) {
            log.error("Error creating the browse indexes", ex);
            fail("Error creating the browse indexes");
        } catch (RegistryImportException ex) {
            log.error("Error loading default data", ex);
            fail("Error loading default data");
        } catch (NonUniqueMetadataException ex) {
            log.error("Error loading default data", ex);
            fail("Error loading default data");
        } catch (ParserConfigurationException ex) {
            log.error("Error loading default data", ex);
            fail("Error loading default data");
        } catch (SAXException ex) {
            log.error("Error loading default data", ex);
            fail("Error loading default data");
        } catch (TransformerException ex) {
            log.error("Error loading default data", ex);
            fail("Error loading default data");
        } catch (AuthorizeException ex) {
            log.error("Error loading default data", ex);
            fail("Error loading default data");
        } catch (SQLException ex) {
            log.error("Error initializing the database", ex);
            fail("Error initializing the database");
        } catch (IOException ex) {
            log.error("Error initializing tests", ex);
            fail("Error initializing tests");
        }
    }

    /**
     * Copies one directory (And its contents) into another 
     * 
     * @param from Folder to copy
     * @param to Destination
     * @throws IOException There is an error while copying the content
     */
    protected static void copyDir(File from, File to) throws IOException {
        if (!from.isDirectory() || !to.isDirectory()) {
            throw new IOException("Both parameters must be directories. from is " + from.isDirectory() + ", to is " + to.isDirectory());
        }
        File[] contents = from.listFiles();
        for (File f : contents) {
            if (f.isFile()) {
                File copy = new File(to.getAbsolutePath() + File.separator + f.getName());
                copy.createNewFile();
                copyFile(f, copy);
            } else if (f.isDirectory()) {
                File copy = new File(to.getAbsolutePath() + File.separator + f.getName());
                copy.mkdir();
                copyDir(f, copy);
            }
        }
    }

    /**
     * Removes the copies of the origin files from the destination folder. Used
     * to remove the temporal copies of files done for testing
     *
     * @param from Folder to check
     * @param to Destination from which to remove contents
     * @throws IOException There is an error while copying the content
     */
    protected static void deleteDir(File from, File to) throws IOException {
        if (!from.isDirectory() || !to.isDirectory()) {
            throw new IOException("Both parameters must be directories. from is " + from.isDirectory() + ", to is " + to.isDirectory());
        }
        File[] contents = from.listFiles();
        for (File f : contents) {
            if (f.isFile()) {
                File copy = new File(to.getAbsolutePath() + File.separator + f.getName());
                if (copy.exists()) {
                    copy.delete();
                }
            } else if (f.isDirectory()) {
                File copy = new File(to.getAbsolutePath() + File.separator + f.getName());
                if (copy.exists() && copy.listFiles().length > 0) {
                    deleteDir(f, copy);
                }
                copy.delete();
            }
        }
    }

    /**
     * Copies one file into another
     *
     * @param from File to copy
     * @param to Destination of copy
     * @throws IOException There is an error while copying the content
     */
    protected static void copyFile(File from, File to) throws IOException {
        if (!from.isFile() || !to.isFile()) {
            throw new IOException("Both parameters must be files. from is " + from.isFile() + ", to is " + to.isFile());
        }
        FileChannel in = (new FileInputStream(from)).getChannel();
        FileChannel out = (new FileOutputStream(to)).getChannel();
        in.transferTo(0, from.length(), out);
        in.close();
        out.close();
    }

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    public void init() {
        try {
            context = new Context();
            context.setCurrentUser(eperson);
            context.commit();
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
            fail("SQL Error on AbstractUnitTest init()");
        }
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    public void destroy() {
        if (context != null && context.isValid()) {
            context.abort();
        }
    }

    /**
     * This method will be run after all tests finish as per @AfterClass. It will
     * clean resources initialized by the @BeforeClass methods.
     *
     */
    @AfterClass
    public static void destroyOnce() {
        try {
            URL origin = ClassLoader.getSystemResource("dspaceFolder");
            File source = new File(origin.getPath());
            File dspaceTmp = new File(testProps.getProperty("test.folder"));
            deleteDir(source, dspaceTmp);
            File destParent = new File(testProps.getProperty("test.folder.assetstore"));
            deleteDir(destParent, destParent);
            testProps.clear();
            testProps = null;
        } catch (IOException ex) {
            log.error("Error cleaning the temporal files of testing", ex);
        }
    }
}
