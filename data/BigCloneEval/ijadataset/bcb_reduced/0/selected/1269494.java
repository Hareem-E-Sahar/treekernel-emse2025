package edu.harvard.hul.ois.fits.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import edu.harvard.hul.ois.fits.FitsOutput;

public class TestOutput {

    private static final String USAGE = "generatetestoutput [create | test]";

    private static final String TEST_INPUT = "tests/input";

    private static final String TEST_OUTPUT = "tests/output";

    /**
	 * @param args
	 * @throws Exception 
	 */
    public static void main(String[] args) throws Exception {
        String action = "";
        if (args.length == 0) {
            System.out.println("Invalid Syntax");
            System.out.println("Usage:\n" + USAGE);
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                System.out.println("Usage:\n" + USAGE);
                System.exit(0);
            } else if (args[i].equals("create")) {
                action = "g";
                break;
            } else if (args[i].equals("test")) {
                action = "t";
                break;
            }
        }
        if (action.equals("g")) {
            generate();
        } else if (action.equals("t")) {
            test();
        } else {
            System.out.println("Usage:\n" + USAGE);
            System.exit(0);
        }
    }

    private static void generate() throws Exception {
        System.out.print("This will erase all existing test output, continue? [y/n]: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String userConfirm = null;
        try {
            userConfirm = br.readLine();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        if (!userConfirm.equalsIgnoreCase("y")) {
            System.out.println("exiting");
            System.exit(0);
        }
        File inputDir = new File(TEST_INPUT);
        File outputDir = new File(TEST_OUTPUT);
        for (File output : outputDir.listFiles()) {
            if (!output.getPath().endsWith(".xml")) {
                continue;
            }
            System.out.println("deleting " + output.getPath());
            output.delete();
        }
        Fits fits = new Fits("");
        for (File input : inputDir.listFiles()) {
            if (input.isDirectory()) {
                continue;
            }
            System.out.println("processing " + input.getPath());
            FitsOutput fitsOutput = fits.examine(input);
            fitsOutput.saveToDisk(outputDir.getPath() + File.separator + input.getName() + ".xml");
        }
        System.out.println("All Done");
    }

    private static void test() throws Exception {
        Fits fits = new Fits("");
        SAXBuilder builder = new SAXBuilder();
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        File inputDir = new File(TEST_INPUT);
        File outputDir = new File(TEST_OUTPUT);
        int passed = 0;
        int failed = 0;
        for (File input : inputDir.listFiles()) {
            if (input.isDirectory()) {
                continue;
            }
            StringWriter sw = new StringWriter();
            FitsOutput fitsOut = fits.examine(input);
            serializer.output(fitsOut.getFitsXml(), sw);
            String actualStr = sw.toString();
            File outputFile = new File(outputDir + File.separator + input.getName() + ".xml");
            if (!outputFile.exists()) {
                System.err.println("Not Found: " + outputFile.getPath());
                continue;
            }
            Document expectedXml = builder.build(new FileInputStream(outputFile));
            sw = new StringWriter();
            serializer.output(expectedXml, sw);
            String expectedStr = sw.toString();
            FitsMetadataElement item = fitsOut.getMetadataElement("filename");
            DifferenceListener myDifferenceListener = new IgnoreAttributeValuesDifferenceListener();
            Diff diff = new Diff(expectedStr, actualStr);
            diff.overrideDifferenceListener(myDifferenceListener);
            if (diff.similar()) {
                System.out.println("PASS: " + item.getValue());
                passed++;
            } else {
                System.err.println("FAIL: " + item.getValue());
                failed++;
            }
        }
        System.out.println("All Done");
        System.out.println(passed + " tests passed");
        if (failed != 0) {
            System.err.println(failed + " tests failed");
        } else {
            System.out.println(failed + " tests failed");
        }
    }
}
