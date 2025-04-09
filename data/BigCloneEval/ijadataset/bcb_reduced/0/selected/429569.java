package org.michelemostarda;

import junit.framework.TestCase;
import java.io.*;

public class ConverterTest extends TestCase {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private PrintStream ps = new PrintStream(baos);

    private Emitter testEmitter;

    private Converter converter;

    public void setUp() {
        testEmitter = new Emitter(ps);
        converter = new Converter(testEmitter);
    }

    public void tearDown() {
        baos.reset();
        testEmitter = null;
        converter = null;
    }

    public void testItalic() throws IOException {
        String in = "this is _italic_ text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this is <i>italic</i> text");
    }

    public void testBold() throws IOException {
        String in = "this is *bold* text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this is <strong>bold</strong> text");
    }

    public void testCode() throws IOException {
        String in = "this is `code terms` text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this is <tt>code terms</tt> text");
    }

    public void testSuper() throws IOException {
        String in = "this is ^super^ code text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this is <sup>super</sup> code text");
    }

    public void testSub() throws IOException {
        String in = "this is ,,sub,, code text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this is <sub>sub</sub> code text");
    }

    public void testNotSub() throws IOException {
        String in = "this isn't ,sub, text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this isn't ,sub, text");
    }

    public void testStrike() throws IOException {
        String in = "this is ~~strike~~ text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this is <strike>strike</strike> text");
    }

    public void testNotStrike() throws IOException {
        String in = "this isn't ~strike~ text";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("this isn't ~strike~ text");
    }

    public void testConverterPalindromeClosuers() throws IOException {
        String in = "aaaa _bb bb_ ccc *dd dd* eeee";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("aaaa <i>bb bb</i> ccc <strong>dd dd</strong> eeee");
    }

    public void testConverterNonPalindromeClosuers() throws IOException {
        String in = "aaaa _bb bb* ccc _dd dd* eeee";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("aaaa <i>bb bb<strong> ccc </i>dd dd</strong> eeee");
    }

    public void testTitlePalindromeClosures() throws IOException {
        String in = "=title one= ==title two== *bold*";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>title one</h1> <h2>title two</h2> <strong>bold</strong>");
    }

    public void testTitleNonPalindromeClosures() throws IOException {
        String in = "=title one== =title two== *bold*";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>title one</h1> <h1>title two</h1> <strong>bold</strong>");
    }

    public void testCloseStackMarkers() throws IOException {
        String in = "=title one ==title two *bold _italic";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>title one </h1>title two <strong>bold <i>italic</i></strong>");
    }

    public void testLink() throws IOException {
        String in = "=Link Test= [http://path/to/link Link description bla bla]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Link Test</h1> <a href=\"http://path/to/link\">Link description bla bla</a>");
    }

    public void testEmptyLink() throws IOException {
        String in = "=Empty Link Test= []";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Empty Link Test</h1> ");
    }

    public void testLinkOpened() throws IOException {
        String in = "=Open Link Test= [";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Open Link Test</h1> ");
    }

    public void testLinkClosed() throws IOException {
        String in = "=Empty Link Test= ]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Empty Link Test</h1> ");
    }

    public void testLinkWithOperatorsInDescription() throws IOException {
        String in = "=Link with operators in description= [http://path/to/link description ==title== _italic_ *bold*]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Link with operators in description</h1> <a href=\"http://path/to/link\">description <h2>title</h2> <i>italic</i> <strong>bold</strong></a>");
    }

    public void testLinkWithOperatorsInURL() throws IOException {
        String in = "=Link with operators in description= [http://path/to/_link_ description _italic_ *bold*]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Link with operators in description</h1> <a href=\"http://path/to/_link_\">description <i>italic</i> <strong>bold</strong></a>");
    }

    public void testInternalLink() throws IOException {
        String in = "=Internal Link= [InternalLink Internal link description _italic_ *bold*]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Internal Link</h1> <a href=\"InternalLink\">Internal link description <i>italic</i> <strong>bold</strong></a>");
    }

    public void testImage() throws IOException {
        String in = "=Link to image= [http://path/to/image.jpeg image description in _italic_ and *bold*]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Link to image</h1> <img src=\"http://path/to/image.jpeg\">image description in <i>italic</i> and <strong>bold</strong></img>");
    }

    public void testNoImage() throws IOException {
        String in = "=Link to wrong image= [http://path/to/image.xxx wrong image description in _italic_ and *bold*]";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<h1>Link to wrong image</h1> <a href=\"http://path/to/image.xxx\">wrong image description in <i>italic</i> and <strong>bold</strong></a>");
    }

    public void testBasicVerbatim() throws IOException {
        String in = "Normal text {{{escaped text\n here}}}";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("Normal text <pre><code>escaped text\n here </code></pre> ");
    }

    public void testVerbatimMantainsContentUnchanged() throws IOException {
        String in = "{{{An example of preserved ${@escaped} text}}}";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<pre><code>An example of preserved $ { @escaped } text </code></pre> ");
    }

    public void testVerbatimWrongOpening() throws IOException {
        String in = "Normal text {{ends with wrong opening}}}";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("Normal text {{ends with wrong opening}}}");
    }

    public void testBasicVerbatimWrongClosure() throws IOException {
        String in = "Normal text {{{ends with wrong closure}}";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("Normal text <pre><code>ends with wrong closure }} </code></pre> ");
    }

    public void testVerbatimOperatorsInside() throws IOException {
        String in = "External *text* {{{ *internal text* with _operators_ }}} post _test_ *operators*";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("External <strong>text</strong> <pre><code>*internal text* with _operators_ </code></pre>   post   <i> test </i>   <strong> operators </strong> ");
    }

    public void testDivider() throws IOException {
        String in = "This is a divider: ----";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("This is a divider: <hr/>");
    }

    public void testSingleCarriageReturn() throws IOException {
        String in = "This *text\n_contains single \n carriage return.";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("This <strong>text<i>contains single  carriage return.</i></strong>");
    }

    public void testMultiCarriageReturn() throws IOException {
        String in = "This *text _contains multi \n\n carriage returns.";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("This <strong>text <i>contains multi </i></strong><br/> carriage returns.");
    }

    public void testTableSingleRow() throws IOException {
        String in = "||col 1,1||col 1,2||col 1,3||\n\n";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<table><tr><td>col 1,1</td><td>col 1,2</td><td>col 1,3</td><td></tr></table><br/>");
    }

    public void testTableMultiRow() throws IOException {
        String in = "||col 1,1||col 1,2||col 1,3||\n" + "||col 2,1||col 2,2||col 2,3||\n\n";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("<table><tr><td>col 1,1</td><td>col 1,2</td><td>col 1,3</td><td></tr><tr><td>col 2,1</td><td>col 2,2</td><td>col 2,3</td><td></tr></table><br/>");
    }

    public void testList() throws IOException {
        String in = "Pre text\n" + "  first entry\n" + "  second entry\n" + "  third entry\n\n";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("");
    }

    public void testOrderedList() throws IOException {
        String in = "Pre text\n" + "  #first entry\n" + "  #second entry\n" + "  #third entry\n\n";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("");
    }

    public void testComment() throws IOException {
        String in = "This is text #this is a comment\n\n" + "This is other text.";
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
        converter.parse(is);
        checkResult("This is text <br/>This is other text.");
    }

    public void testInputFile() throws IOException {
        converter.parseUnit(new File("test_input/Test1.wiki"));
    }

    void checkResult(String expected) {
        ps.flush();
        assertEquals("Unespected result.", expected, baos.toString());
    }
}
