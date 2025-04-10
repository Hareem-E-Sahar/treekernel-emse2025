package au.com.bytecode.opencsv;

import org.junit.Test;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class CSVWriterTest {

    /**
     * Test routine for converting output to a string.
     *
     * @param args the elements of a line of the cvs file
     * @return a String version
     * @throws IOException if there are problems writing
     */
    private String invokeWriter(String[] args) throws IOException {
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, ',', '\'');
        csvw.writeNext(args);
        return sw.toString();
    }

    private String invokeNoEscapeWriter(String[] args) throws IOException {
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, '\'', CSVWriter.NO_ESCAPE_CHARACTER);
        csvw.writeNext(args);
        return sw.toString();
    }

    @Test
    public void correctlyParseNullString() {
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, ',', '\'');
        csvw.writeNext(null);
        assertEquals(0, sw.toString().length());
    }

    @Test
    public void correctlyParserNullObject() {
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, ',', '\'');
        csvw.writeNext(null, false);
        assertEquals(0, sw.toString().length());
    }

    /**
     * Tests parsing individual lines.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testParseLine() throws IOException {
        String[] normal = { "a", "b", "c" };
        String output = invokeWriter(normal);
        assertEquals("'a','b','c'\n", output);
        String[] quoted = { "a", "b,b,b", "c" };
        output = invokeWriter(quoted);
        assertEquals("'a','b,b,b','c'\n", output);
        String[] empty = {};
        output = invokeWriter(empty);
        assertEquals("\n", output);
        String[] multiline = { "This is a \n multiline entry", "so is \n this" };
        output = invokeWriter(multiline);
        assertEquals("'This is a \n multiline entry','so is \n this'\n", output);
        String[] quoteLine = { "This is a \" multiline entry", "so is \n this" };
        output = invokeWriter(quoteLine);
        assertEquals("'This is a \"\" multiline entry','so is \n this'\n", output);
    }

    @Test
    public void testSpecialCharacters() throws IOException {
        String[] quoteLine = { "This is a \r multiline entry", "so is \n this" };
        String output = invokeWriter(quoteLine);
        assertEquals("'This is a \r multiline entry','so is \n this'\n", output);
    }

    @Test
    public void parseLineWithBothEscapeAndQuoteChar() throws IOException {
        String[] quoteLine = { "This is a 'multiline' entry", "so is \n this" };
        String output = invokeWriter(quoteLine);
        assertEquals("'This is a \"'multiline\"' entry','so is \n this'\n", output);
    }

    /**
     * Tests parsing individual lines.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testParseLineWithNoEscapeChar() throws IOException {
        String[] normal = { "a", "b", "c" };
        String output = invokeNoEscapeWriter(normal);
        assertEquals("'a','b','c'\n", output);
        String[] quoted = { "a", "b,b,b", "c" };
        output = invokeNoEscapeWriter(quoted);
        assertEquals("'a','b,b,b','c'\n", output);
        String[] empty = {};
        output = invokeNoEscapeWriter(empty);
        assertEquals("\n", output);
        String[] multiline = { "This is a \n multiline entry", "so is \n this" };
        output = invokeNoEscapeWriter(multiline);
        assertEquals("'This is a \n multiline entry','so is \n this'\n", output);
    }

    @Test
    public void parseLineWithNoEscapeCharAndQuotes() throws IOException {
        String[] quoteLine = { "This is a \" 'multiline' entry", "so is \n this" };
        String output = invokeNoEscapeWriter(quoteLine);
        assertEquals("'This is a \" 'multiline' entry','so is \n this'\n", output);
    }

    /**
     * Test writing to a list.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testWriteAll() throws IOException {
        List<String[]> allElements = new ArrayList<String[]>();
        String[] line1 = "Name#Phone#Email".split("#");
        String[] line2 = "Glen#1234#glen@abcd.com".split("#");
        String[] line3 = "John#5678#john@efgh.com".split("#");
        allElements.add(line1);
        allElements.add(line2);
        allElements.add(line3);
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.writeAll(allElements);
        String result = sw.toString();
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
    }

    /**
     * Test writing from a list.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testWriteAllObjects() throws IOException {
        List<String[]> allElements = new ArrayList<String[]>(3);
        String[] line1 = "Name#Phone#Email".split("#");
        String[] line2 = "Glen#1234#glen@abcd.com".split("#");
        String[] line3 = "John#5678#john@efgh.com".split("#");
        allElements.add(line1);
        allElements.add(line2);
        allElements.add(line3);
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.writeAll(allElements, false);
        String result = sw.toString();
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        String[] values = lines[1].split(",");
        assertEquals("1234", values[1]);
    }

    /**
     * Tests the option of having omitting quotes in the output stream.
     *
     * @throws IOException if bad things happen
     */
    @Test
    public void testNoQuoteChars() throws IOException {
        String[] line = { "Foo", "Bar", "Baz" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
        csvw.writeNext(line);
        String result = sw.toString();
        assertEquals("Foo,Bar,Baz\n", result);
    }

    /**
     * Tests the option of having omitting quotes in the output stream.
     *
     * @throws IOException if bad things happen
     */
    @Test
    public void testNoQuoteCharsAndNoEscapeChars() throws IOException {
        String[] line = { "Foo", "Bar", "Baz" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
        csvw.writeNext(line);
        String result = sw.toString();
        assertEquals("Foo,Bar,Baz\n", result);
    }

    /**
     * Tests the ability for the writer to apply quotes only where strings contain the separator, escape, quote or new line characters.
     */
    @Test
    public void testIntelligentQuotes() {
        String[] line = { "1", "Foo", "With,Separator", "Line\nBreak", "Hello \"Foo Bar\" World", "Bar" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
        csvw.writeNext(line, false);
        String result = sw.toString();
        assertEquals("1,Foo,\"With,Separator\",\"Line\nBreak\",\"Hello \"\"Foo Bar\"\" World\",Bar\n", result);
    }

    /**
     * Test null values.
     *
     * @throws IOException if bad things happen
     */
    @Test
    public void testNullValues() throws IOException {
        String[] line = { "Foo", null, "Bar", "baz" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.writeNext(line);
        String result = sw.toString();
        assertEquals("\"Foo\",,\"Bar\",\"baz\"\n", result);
    }

    @Test
    public void testStreamFlushing() throws IOException {
        String WRITE_FILE = "myfile.csv";
        String[] nextLine = new String[] { "aaaa", "bbbb", "cccc", "dddd" };
        FileWriter fileWriter = new FileWriter(WRITE_FILE);
        CSVWriter writer = new CSVWriter(fileWriter);
        writer.writeNext(nextLine);
        writer.close();
    }

    @Test(expected = IOException.class)
    public void flushWillThrowIOException() throws IOException {
        String[] line = { "Foo", "bar's" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriterExceptionThrower(sw);
        csvw.writeNext(line);
        csvw.flush();
    }

    @Test
    public void flushQuietlyWillNotThrowException() {
        String[] line = { "Foo", "bar's" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriterExceptionThrower(sw);
        csvw.writeNext(line);
        csvw.flushQuietly();
    }

    @Test
    public void testAlternateEscapeChar() {
        String[] line = { "Foo", "bar's" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, '\'');
        csvw.writeNext(line);
        assertEquals("\"Foo\",\"bar''s\"\n", sw.toString());
    }

    @Test
    public void testNoQuotingNoEscaping() {
        String[] line = { "\"Foo\",\"Bar\"" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
        csvw.writeNext(line);
        assertEquals("\"Foo\",\"Bar\"\n", sw.toString());
    }

    @Test
    public void testNestedQuotes() {
        String[] data = new String[] { "\"\"", "test" };
        String oracle = new String("\"\"\"\"\"\",\"test\"\n");
        CSVWriter writer = null;
        File tempFile = null;
        FileWriter fwriter = null;
        try {
            tempFile = File.createTempFile("csvWriterTest", ".csv");
            tempFile.deleteOnExit();
            fwriter = new FileWriter(tempFile);
            writer = new CSVWriter(fwriter);
        } catch (IOException e) {
            fail();
        }
        writer.writeNext(data);
        try {
            writer.close();
        } catch (IOException e) {
            fail();
        }
        try {
            fwriter.flush();
            fail();
        } catch (IOException e) {
        }
        FileReader in = null;
        try {
            in = new FileReader(tempFile);
        } catch (FileNotFoundException e) {
            fail();
        }
        StringBuilder fileContents = new StringBuilder(CSVWriter.INITIAL_STRING_SIZE);
        try {
            int ch;
            while ((ch = in.read()) != -1) {
                fileContents.append((char) ch);
            }
            in.close();
        } catch (IOException e) {
            fail();
        }
        assertTrue(oracle.equals(fileContents.toString()));
    }

    @Test
    public void testAlternateLineFeeds() {
        String[] line = { "Foo", "Bar", "baz" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, "\r");
        csvw.writeNext(line);
        String result = sw.toString();
        assertTrue(result.endsWith("\r"));
    }

    @Test
    public void testResultSetWithHeaders() throws SQLException, IOException {
        String[] header = { "Foo", "Bar", "baz" };
        String[] value = { "v1", "v2", "v3" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.setResultService(new ResultSetHelperService());
        ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);
        csvw.writeAll(rs, true);
        assertFalse(csvw.checkError());
        String result = sw.toString();
        assertNotNull(result);
        assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
    }

    @Test
    public void testMultiLineResultSetWithHeaders() throws SQLException, IOException {
        String[] header = { "Foo", "Bar", "baz" };
        String[] value = { "v1", "v2", "v3" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.setResultService(new ResultSetHelperService());
        ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);
        csvw.writeAll(rs, true);
        assertFalse(csvw.checkError());
        String result = sw.toString();
        assertNotNull(result);
        assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
    }

    @Test
    public void testResultSetWithoutHeaders() throws SQLException, IOException {
        String[] header = { "Foo", "Bar", "baz" };
        String[] value = { "v1", "v2", "v3" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.setResultService(new ResultSetHelperService());
        ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);
        csvw.writeAll(rs, false);
        assertFalse(csvw.checkError());
        String result = sw.toString();
        assertNotNull(result);
        assertEquals("\"v1\",\"v2\",\"v3\"\n", result);
    }

    @Test
    public void testMultiLineResultSetWithoutHeaders() throws SQLException, IOException {
        String[] header = { "Foo", "Bar", "baz" };
        String[] value = { "v1", "v2", "v3" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.setResultService(new ResultSetHelperService());
        ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);
        csvw.writeAll(rs, false);
        assertFalse(csvw.checkError());
        String result = sw.toString();
        assertNotNull(result);
        assertEquals("\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
    }

    @Test
    public void testResultSetTrim() throws SQLException, IOException {
        String[] header = { "Foo", "Bar", "baz" };
        String[] value = { "v1         ", "v2 ", "v3" };
        StringWriter sw = new StringWriter();
        CSVWriter csvw = new CSVWriter(sw);
        csvw.setResultService(new ResultSetHelperService());
        ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);
        csvw.writeAll(rs, true, true);
        assertFalse(csvw.checkError());
        String result = sw.toString();
        assertNotNull(result);
        assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
    }
}
