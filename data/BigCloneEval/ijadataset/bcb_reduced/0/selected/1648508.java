package net.bull.javamelody;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test unitaire de la classe HtmlSessionInformationsReport.
 * @author Emeric Vernat
 */
public class TestHtmlThreadInformationsReport {

    /** Check. */
    @Before
    public void setUp() {
        Utils.initialize();
    }

    private static void assertNotEmptyAndClear(StringWriter writer) {
        assertTrue("rapport vide", writer.getBuffer().length() > 0);
        writer.getBuffer().setLength(0);
    }

    /** Test.
	 * @throws IOException e */
    @Test
    public void testThreadInformations() throws IOException {
        final StringWriter writer = new StringWriter();
        new HtmlThreadInformationsReport(Collections.<ThreadInformations>emptyList(), true, writer).toHtml();
        assertNotEmptyAndClear(writer);
        new HtmlThreadInformationsReport(JavaInformations.buildThreadInformationsList(), true, writer).toHtml();
        assertNotEmptyAndClear(writer);
        new HtmlThreadInformationsReport(JavaInformations.buildThreadInformationsList(), false, writer).toHtml();
        assertNotEmptyAndClear(writer);
        final List<ThreadInformations> threads = new ArrayList<ThreadInformations>();
        final Thread thread = Thread.currentThread();
        final List<StackTraceElement> stackTrace = Arrays.asList(thread.getStackTrace());
        final String hostAddress = Parameters.getHostAddress();
        threads.add(new ThreadInformations(thread, null, 10, 10, false, hostAddress));
        threads.add(new ThreadInformations(thread, Collections.<StackTraceElement>emptyList(), 10, 10, false, hostAddress));
        threads.add(new ThreadInformations(thread, stackTrace, 10, 10, true, hostAddress));
        threads.add(new ThreadInformations(thread, stackTrace, 10, 10, false, hostAddress));
        new HtmlThreadInformationsReport(threads, true, writer).toHtml();
        assertNotEmptyAndClear(writer);
        new HtmlThreadInformationsReport(threads, true, writer).writeDeadlocks();
        assertNotEmptyAndClear(writer);
    }
}
