package fedora.server.journal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ fedora.server.journal.helpers.AllUnitTests.class, fedora.server.journal.readerwriter.AllUnitTests.class, fedora.server.journal.xmlhelpers.AllUnitTests.class })
public class AllUnitTests {

    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(AllUnitTests.class.getName());
        suite.addTest(fedora.server.journal.helpers.AllUnitTests.suite());
        suite.addTest(fedora.server.journal.readerwriter.AllUnitTests.suite());
        suite.addTest(fedora.server.journal.xmlhelpers.AllUnitTests.suite());
        return suite;
    }
}
