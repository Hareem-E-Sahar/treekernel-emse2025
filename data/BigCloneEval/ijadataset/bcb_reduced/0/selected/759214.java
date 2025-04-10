package de.sonivis.tool.mwapiconnector.tests;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.sonivis.tool.core.CorePlugin;
import de.sonivis.tool.core.DataModelPreferencesControl;
import de.sonivis.tool.core.ModelManager;
import de.sonivis.tool.core.datamodel.InfoSpace;
import de.sonivis.tool.core.datamodel.dao.hibernate.InfoSpaceDAO;
import de.sonivis.tool.core.datamodel.exceptions.CannotConnectToDatabaseException;
import de.sonivis.tool.core.tests.AbstractEmptyDatabaseTestCase;
import de.sonivis.tool.mwapiconnector.PageInfoQuery;
import de.sonivis.tool.mwapiconnector.extractors.ApiExtractor;
import de.sonivis.tool.mwapiconnector.extractors.ApiExtractorArguments;
import de.sonivis.tool.mwapiconnector.extractors.ApiExtractorWizardPage;
import de.sonivis.tool.mwapiconnector.extractors.IApiExtractorArguments;

/**
 * Test case for the MediaWiki {@link ApiExtractor}.
 * 
 * @author Janette Lehmann
 * @version $Revision$, $Date$
 */
public class ApiExtractorParametersTest extends AbstractEmptyDatabaseTestCase {

    /**
	 * Class logging.
	 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExtractorParametersTest.class);

    /**
	 * Representative of the class under test.
	 */
    private ApiExtractor ae = null;

    /**
	 * Absolute or relative path to a file.
	 */
    private String path = "C:/Users/Nette/Desktop/wiki-traffic/extract/AllArticlesWithOutRevs.dat";

    /**
	 * {@inheritDoc}
	 * 
	 * @see TestCase#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final URL wikiUrl = new URL("http://en.wikipedia.org/w/");
        final IApiExtractorArguments iaea = new ApiExtractorArguments(wikiUrl, null, null);
        this.ae = new ApiExtractor(iaea);
        CorePlugin.getDefault().getPreferenceStore().setValue(DataModelPreferencesControl.INTERNAL_DB_HOSTNAME, "localhost");
        CorePlugin.getDefault().getPreferenceStore().setValue(DataModelPreferencesControl.INTERNAL_DATABASE, "wikiextractEmpty");
        CorePlugin.getDefault().getPreferenceStore().setValue(DataModelPreferencesControl.INTERNAL_DB_USERNAME, "root");
        CorePlugin.getDefault().getPreferenceStore().setValue(DataModelPreferencesControl.INTERNAL_DB_PASSWORD, "RedChilli");
    }

    /**
	 * {@inheritDoc}
	 * 
	 * @see TestCase#tearDown()
	 */
    @Override
    protected void tearDown() {
        this.ae = null;
    }

    public void testExtractFromArticleSelectionCleanUp() {
        Session s = null;
        Transaction tx = null;
        try {
            s = ModelManager.getInstance().getCurrentSession();
            tx = s.beginTransaction();
            s.createSQLQuery("TRUNCATE actorcontentelementrelation;").executeUpdate();
            s.createSQLQuery("TRUNCATE contextrelation;").executeUpdate();
            s.createSQLQuery("TRUNCATE interactionrelation;").executeUpdate();
            s.createSQLQuery("TRUNCATE actor;").executeUpdate();
            s.createSQLQuery("TRUNCATE contentelement;").executeUpdate();
            s.createSQLQuery("TRUNCATE infospaceitemproperty;").executeUpdate();
            s.createSQLQuery("TRUNCATE infospaceitem;").executeUpdate();
            s.flush();
            s.clear();
            tx.commit();
        } catch (final HibernateException he) {
            tx.rollback();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception occurred when trying to delete test InfoSpace - transaction was rolled back. InfoSpace and possibly several contained entities were not deleted. Must be deleted by hand.", he);
            }
            throw he;
        } catch (final CannotConnectToDatabaseException e) {
            fail("Persistence store is not available.");
        } finally {
            s.close();
        }
    }

    /**
	 * This test case has to be customized before being run!
	 * <p>
	 * The path to the file to be used has to be set in {@link #path}. The
	 * corresponding wiki URL has to be adapted in {@link #setUp()} accordingly.
	 * </p>
	 * <p>
	 * If you want to keep the result of this operation you might want to
	 * comment the database emptying instructions at the end of the method. If
	 * you do so, the test will fail in the end due to super class'
	 * {@link #tearDown()} failing to remove the {@link InfoSpace} instance from
	 * the database.
	 * </p>
	 */
    public void tExtractFromArticleSelection() {
        CorePlugin.getDefault().getPreferenceStore().setValue("isArticleSelectionExtract", true);
        CorePlugin.getDefault().getPreferenceStore().setValue("ArticleSelectionFileName", this.path);
        CorePlugin.getDefault().getPreferenceStore().setValue(ApiExtractorWizardPage.MEDIAWIKI_API_EXTRACT_ONLY_LATEST_REVISION, true);
        CorePlugin.getDefault().getPreferenceStore().setValue(ApiExtractorWizardPage.MEDIAWIKI_API_EXTRACT_START_DATE, "2009-01-01");
        final IProgressMonitor monitor = new NullProgressMonitor();
        try {
            this.ae.extract(this.infoSpace, monitor);
        } catch (final Exception e) {
            if (e instanceof MalformedURLException) {
                fail("Wiki URL is incorrect");
            }
            if (e instanceof CannotConnectToDatabaseException) {
                fail("Persistence store is not available.");
            }
            e.printStackTrace();
            fail("Exception of type " + e.getClass().getSimpleName() + " occurred.");
        }
    }
}
