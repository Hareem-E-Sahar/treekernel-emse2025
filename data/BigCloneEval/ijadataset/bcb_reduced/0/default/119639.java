import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import java.net.*;
import java.util.*;

public class SAXBench implements ErrorHandler {

    public static final String VERSION = "@VERSION@";

    private ArrayList parsers = new ArrayList(10);

    private ArrayList tests = new ArrayList(10);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            info("Usage: java SAXBench <saxbench.xml> > results.xml");
            System.exit(1);
        }
        new SAXBench(args[0]);
    }

    public SAXBench(String config) throws Exception {
        readConfig(config);
        runTests();
    }

    private static void msg(String msg) {
        System.out.println(msg);
    }

    private static void info(String msg) {
        System.err.println(msg);
    }

    private void readConfig(String config) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        xmlReader.setContentHandler(new ConfigHandler(parsers, tests));
        xmlReader.setErrorHandler(this);
        xmlReader.parse(config);
    }

    private void runTests() throws Exception {
        int numParsers = parsers.size();
        info("Warming up SAXBench by performing a dry run of all tests...");
        for (int i = 0; i < numParsers; i++) {
            TestParser p = (TestParser) parsers.get(i);
            p.runTests(tests, true);
        }
        info("Performing benchmark tests...");
        msg("<?xml version='1.0' encoding='US-ASCII'?>");
        msg("<!DOCTYPE saxbench-results SYSTEM 'saxbench-results.dtd'>");
        msg("<saxbench-results version='" + VERSION + "'>");
        for (int i = 0; i < numParsers; i++) {
            ((TestParser) parsers.get(i)).runTests(tests, false);
        }
        msg("</saxbench-results>");
    }

    public void error(SAXParseException e) throws SAXParseException {
        fatalError(e);
    }

    public void fatalError(SAXParseException e) throws SAXParseException {
        throw e;
    }

    public void warning(SAXParseException e) {
    }

    private static class ConfigHandler extends DefaultHandler {

        TestParser currentParser = null;

        ArrayList parsers, tests;

        public ConfigHandler(ArrayList parsers, ArrayList tests) {
            this.parsers = parsers;
            this.tests = tests;
        }

        public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attr) throws SAXException {
            if (qName == "parser") {
                currentParser = new TestParser(attr.getValue("name"), attr.getValue("classpath"), attr.getValue("driver"), attr.getValue("options"));
                parsers.add(currentParser);
            } else if (qName == "property") currentParser.setProperty(attr.getValue("name"), attr.getValue("value")); else if (qName == "test") {
                int cache;
                String p = attr.getValue("cache");
                if ("predecode".equalsIgnoreCase(p)) cache = AbstractTest.PREDECODE; else if ("preload".equalsIgnoreCase(p)) cache = AbstractTest.PRELOAD; else if ("no".equalsIgnoreCase(p)) cache = AbstractTest.NONE; else throw new SAXException("Illegal cache value for test " + attr.getValue("name"));
                tests.add(new TestFile(attr.getValue("name"), attr.getValue("src"), attr.getValue("iterations"), cache));
            }
        }
    }

    private static class TestParser {

        String name, classpath, driver, options;

        Properties properties = new Properties();

        TestParser(String name, String classpath, String driver, String options) {
            this.name = name;
            this.classpath = classpath;
            this.driver = driver;
            if (options == null) this.options = ""; else this.options = options;
        }

        void setProperty(String name, String value) {
            properties.setProperty(name, value);
        }

        void runTests(ArrayList tests, boolean dryRunOnly) throws Exception {
            if (!dryRunOnly) msg(" <parser name='" + name + "' driver='" + driver + "'" + ((options != null && options.length() > 0) ? " options='" + options + "'" : "") + ">");
            int numTests = tests.size();
            for (int i = 0; i < numTests; i++) {
                runTest((TestFile) tests.get(i), dryRunOnly);
            }
            if (!dryRunOnly) msg(" </parser>");
        }

        void runTest(TestFile tf, boolean dryRunOnly) throws Exception {
            String cacheString = "";
            if (tf.cache == AbstractTest.PREDECODE) cacheString = "predecode"; else if (tf.cache == AbstractTest.PRELOAD) cacheString = "preload";
            if (!dryRunOnly) msg("  <test name='" + tf.testName + "' src='" + tf.testFile + "' cache='" + (cacheString == "" ? "no" : cacheString) + "'>");
            String cmd = "java -cp \"" + classpath + ";" + System.getProperty("java.class.path") + "\"";
            Iterator props = properties.entrySet().iterator();
            while (props.hasNext()) {
                Map.Entry entry = (Map.Entry) props.next();
                String n = (String) entry.getKey();
                String v = (String) entry.getValue();
                cmd += " -D" + n + "=" + v;
            }
            final String args = " " + driver + " " + tf.testFile + " " + (dryRunOnly ? "0" : tf.iterations) + " " + cacheString + " " + options;
            cmd += args;
            if (dryRunOnly) info("Dry run of " + name + " with " + tf.testName); else info("Testing " + name + " with " + tf.iterations + " iterations of " + tf.testName + ".");
            info("\targuments: " + args);
            Process proc = Runtime.getRuntime().exec(cmd);
            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String errors = "";
            String newLine;
            while ((newLine = stderr.readLine()) != null) {
                info("   " + newLine);
                errors += "   " + newLine + "\n";
            }
            if (!dryRunOnly && errors.length() > 0) {
                msg("   <!--\n" + errors + "   -->");
            }
            proc.waitFor();
            if (proc.exitValue() != 0) {
                info("process exited with non zero value - ignoring!");
                if (!dryRunOnly) msg("<error>Process exited with value " + proc.exitValue() + "</error>");
            } else {
                while (!dryRunOnly && (newLine = stdout.readLine()) != null) msg("   " + newLine);
            }
            if (!dryRunOnly) msg("  </test>");
        }
    }

    private static class TestFile {

        String testName, testFile, iterations;

        int cache;

        TestFile(String testName, String testFile, String iterations, int cache) {
            this.testName = testName;
            this.testFile = testFile;
            this.iterations = iterations;
            this.cache = cache;
        }
    }
}
