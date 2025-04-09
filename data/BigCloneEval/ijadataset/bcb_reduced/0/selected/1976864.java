package de.fzj.roctopus.tests;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.fzj.roctopus.Site;
import de.fzj.roctopus.exceptions.RoctopusException;

public abstract class OneSiteForAllSites extends TestBase implements OneSiteInjectable {

    private static final Log log = LogFactory.getLog(OneSiteForAllSites.class);

    private Site site1;

    public OneSiteForAllSites(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        log.info("###############################" + this.getName());
    }

    public Site getSite1() {
        return site1;
    }

    public void setSite1(Site site1) {
        this.site1 = site1;
    }

    public void testOneSite() throws Exception {
        getOneSite().run(getSite1(), null);
    }

    public String getName() {
        return super.getName() + ":" + getSite1().toString();
    }

    protected abstract OneSiteInjected getOneSite();

    protected TestSuite getTestSuite() throws Exception {
        TestSuite suite = new TestSuite();
        Context context = Context.ContextFactory.getContext();
        List<Site> sites = null;
        try {
            sites = context.getGrid().getAllSites();
        } catch (RoctopusException e) {
            e.printStackTrace();
        }
        for (Site site : sites) {
            Constructor con = this.getClass().getConstructor(new Class[] { String.class });
            Test test = (Test) con.newInstance("testOneSite");
            ((OneSiteInjectable) test).setSite1(site);
            suite.addTest(test);
        }
        return suite;
    }
}
