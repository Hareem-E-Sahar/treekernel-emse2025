package au.edu.usq.utfx.util.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import au.edu.usq.utfx.printers.AnsiString;
import au.edu.usq.utfx.printers.AnsiString.Colour;
import au.edu.usq.utfx.util.CanonicalForm;

/**
 * au.edu.usq.utfx.util.CanonicalForm test class.
 * 
 * <p>
 * Copyright &copy; 2004 - <a href="http://www.usq.edu.au"> University of
 * Southern Queensland. </a>
 * </p>
 * 
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the <a href="http://www.gnu.org/licenses/gpl.txt">GNU General
 * Public License v2 </a> as published by the Free Software Foundation.
 * </p>
 * 
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * </p>
 * 
 * <code>
 * $Source: /cvsroot/utf-x/utf-x/src/java/au/edu/usq/utfx/util/test/CanonicalFormTest.java,v $
 * </code>
 * 
 * @author Jacek Radajewski
 * @author Oliver Lucido (Lulu)
 * @author Stephano AhFock (Bob)
 * @version $Revision: 1.4 $ $Name:  $
 */
public class CanonicalFormTest extends TestCase {

    private String testDir = "src/java/au/edu/usq/utfx/util/test/";

    /**
     * Constructs a test with a specified name.
     * 
     * @param name the test name
     */
    public CanonicalFormTest(String name) throws Exception {
        super(name);
    }

    /**
     * Asserts that the two specified byte arrays are equal.
     *
     * @param b1 expected bytes
     * @param b2 actual bytes
     */
    public void assertEquals(byte[] b1, byte[] b2) throws Exception {
        int index;
        boolean passed = true;
        String expected, actual;
        for (index = 0; index < b1.length; index++) {
            try {
                if (b1[index] != b2[index]) {
                    passed = false;
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                passed = false;
                break;
            }
        }
        if (passed && b2.length > b1.length) {
            passed = false;
        }
        if (passed) {
            return;
        }
        expected = new AnsiString(new String(b1, 0, index), Colour.GREEN).toString();
        if (index <= b1.length) {
            expected += new AnsiString(new String(b1, index, b1.length - index), Colour.RED);
        }
        actual = new AnsiString(new String(b2, 0, index), Colour.YELLOW).toString();
        if (index <= b2.length) {
            actual += new AnsiString(new String(b2, index, b2.length - index), Colour.RED);
        }
        fail("\n\nActual   :----------\n" + actual + "\n-------------------\n\n" + "\nExpected :----------\n" + expected + "\n-------------------\n");
    }

    private void compareXML(boolean useW3CSpec, InputStream source, InputStream expected, OutputStream output) throws Exception {
        CanonicalForm cf;
        byte[] buffer;
        int bytesRead;
        ByteArrayOutputStream expectedBytes, outputBytes;
        expectedBytes = new ByteArrayOutputStream();
        outputBytes = new ByteArrayOutputStream();
        cf = new CanonicalForm(useW3CSpec);
        cf.transform(source, outputBytes);
        if (output != null) {
            output.write(outputBytes.toByteArray());
        }
        buffer = new byte[4096];
        while ((bytesRead = expected.read(buffer)) > -1) {
            expectedBytes.write(buffer, 0, bytesRead);
            buffer = new byte[4096];
        }
        assertEquals(expectedBytes.toByteArray(), outputBytes.toByteArray());
    }

    /**
     * Transforms a source XML document and compares the output to the expected
     * XML file.
     */
    public void testTransform1() throws Exception {
        compareXML(true, new FileInputStream(testDir + "canonical_form_test_1.xml"), new FileInputStream(testDir + "canonical_form_expected_1.xml"), new FileOutputStream(testDir + "canonical_form_output_1.xml"));
    }

    public void testTransform2() throws Exception {
        compareXML(true, new FileInputStream(testDir + "canonical_form_test_2.xml"), new FileInputStream(testDir + "canonical_form_expected_2.xml"), new FileOutputStream(testDir + "canonical_form_output_2.xml"));
    }

    public void testTransform5() throws Exception {
        compareXML(true, new FileInputStream(testDir + "canonical_form_test_5.xml"), new FileInputStream(testDir + "canonical_form_expected_5.xml"), new FileOutputStream(testDir + "canonical_form_output_5.xml"));
    }

    public void testTransform6() throws Exception {
        compareXML(false, new FileInputStream(testDir + "canonical_form_test_6.xml"), new FileInputStream(testDir + "canonical_form_expected_6.xml"), new FileOutputStream(testDir + "canonical_form_output_6.xml"));
    }

    public void testTransform7() throws Exception {
        compareXML(true, new FileInputStream(testDir + "canonical_form_test_7.xml"), new FileInputStream(testDir + "canonical_form_expected_7.xml"), new FileOutputStream(testDir + "canonical_form_output_7.xml"));
    }

    public void testTransform8() throws Exception {
        compareXML(true, new FileInputStream(testDir + "canonical_form_test_8.xml"), new FileInputStream(testDir + "canonical_form_expected_8.xml"), new FileOutputStream(testDir + "canonical_form_output_8.xml"));
    }

    /**
     * Tests that CanonicalForm is idempotent.
     */
    public void testIdempotency() throws Exception {
        String expectedFileName = testDir + "canonical_form_expected_6.xml";
        ByteArrayOutputStream outputBytes1, outputBytes2;
        outputBytes1 = new ByteArrayOutputStream();
        outputBytes2 = new ByteArrayOutputStream();
        compareXML(true, new FileInputStream(testDir + "canonical_form_test_6.xml"), new FileInputStream(expectedFileName), outputBytes1);
        compareXML(true, new ByteArrayInputStream(outputBytes1.toByteArray()), new FileInputStream(expectedFileName), outputBytes2);
        compareXML(true, new ByteArrayInputStream(outputBytes2.toByteArray()), new FileInputStream(expectedFileName), new ByteArrayOutputStream());
    }

    /**
     * Gets a reference to this test suite.
     * 
     * @return this test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(CanonicalFormTest.class);
        return suite;
    }
}
