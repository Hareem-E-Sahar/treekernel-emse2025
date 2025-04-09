package org.pdfbox.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

/**
 * Highlighting of words in a PDF document with an XML file.
 * 
 * @author slagraulet (slagraulet@cardiweb.com)
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 * 
 * @see <a href="http://partners.adobe.com/public/developer/en/pdf/HighlightFileFormat.pdf">
 *      Adobe Highlight File Format</a>
 */
public class PDFHighlighter extends PDFTextStripper {

    private Writer highlighterOutput = null;

    private String[] searchedWords;

    private ByteArrayOutputStream textOS = null;

    private Writer textWriter = null;

    /**
     * Default constructor.
     * 
     * @throws IOException If there is an error constructing this class.
     */
    public PDFHighlighter() throws IOException {
        super();
        super.setLineSeparator("");
        super.setPageSeparator("");
        super.setWordSeparator("");
        super.setShouldSeparateByBeads(false);
        super.setSuppressDuplicateOverlappingText(false);
    }

    /**
     * Generate an XML highlight string based on the PDF.
     * 
     * @param pdDocument The PDF to find words in.
     * @param highlightWord The word to search for.
     * @param xmlOutput The resulting output xml file.
     * 
     * @throws IOException If there is an error reading from the PDF, or writing to the XML.
     */
    public void generateXMLHighlight(PDDocument pdDocument, String highlightWord, Writer xmlOutput) throws IOException {
        generateXMLHighlight(pdDocument, new String[] { highlightWord }, xmlOutput);
    }

    /**
     * Generate an XML highlight string based on the PDF.
     * 
     * @param pdDocument The PDF to find words in.
     * @param sWords The words to search for.
     * @param xmlOutput The resulting output xml file.
     * 
     * @throws IOException If there is an error reading from the PDF, or writing to the XML.
     */
    public void generateXMLHighlight(PDDocument pdDocument, String[] sWords, Writer xmlOutput) throws IOException {
        highlighterOutput = xmlOutput;
        searchedWords = sWords;
        highlighterOutput.write("<XML>\n<Body units=characters " + " version=2>\n<Highlight>\n");
        textOS = new ByteArrayOutputStream();
        textWriter = new OutputStreamWriter(textOS, "UTF-16");
        writeText(pdDocument, textWriter);
        highlighterOutput.write("</Highlight>\n</Body>\n</XML>");
        highlighterOutput.flush();
    }

    /**
     * {@inheritDoc}
     */
    protected void endPage(PDPage pdPage) throws IOException {
        textWriter.flush();
        String page = new String(textOS.toByteArray(), "UTF-16");
        textOS.reset();
        if (page.indexOf("a") != -1) {
            page = page.replaceAll("a[0-9]{1,3}", ".");
        }
        for (int i = 0; i < searchedWords.length; i++) {
            Pattern pattern = Pattern.compile(searchedWords[i], Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(page);
            while (matcher.find()) {
                int begin = matcher.start();
                int end = matcher.end();
                highlighterOutput.write("    <loc " + "pg=" + (getCurrentPageNo() - 1) + " pos=" + begin + " len=" + (end - begin) + ">\n");
            }
        }
    }

    /**
     * Command line application.
     * 
     * @param args The command line arguments to the application.
     * 
     * @throws IOException If there is an error generating the highlight file.
     */
    public static void main(String[] args) throws IOException {
        PDFHighlighter xmlExtractor = new PDFHighlighter();
        PDDocument doc = null;
        try {
            if (args.length < 2) {
                usage();
            }
            String[] highlightStrings = new String[args.length - 1];
            System.arraycopy(args, 1, highlightStrings, 0, highlightStrings.length);
            doc = PDDocument.load(args[0]);
            xmlExtractor.generateXMLHighlight(doc, highlightStrings, new OutputStreamWriter(System.out));
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    private static void usage() {
        System.err.println("usage: java " + PDFHighlighter.class.getName() + " <pdf file> word1 word2 word3 ...");
        System.exit(1);
    }
}
