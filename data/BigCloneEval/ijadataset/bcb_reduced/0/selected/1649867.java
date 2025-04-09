package org.apache.shindig.gadgets.parse;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.parse.nekohtml.NekoSimplifiedHtmlParser;
import org.w3c.dom.DOMImplementation;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

/**
 * Benchmarks for HTML parsing and serialization
 */
public class ParseTreeSerializerBenchmark {

    private int numRuns;

    private String content;

    private GadgetHtmlParser nekoSimpleParser = new NekoSimplifiedHtmlParser(DOCUMENT_PROVIDER);

    private boolean warmup;

    private static final DOMImplementation DOCUMENT_PROVIDER = new ParseModule.DOMImplementationProvider().get();

    private ParseTreeSerializerBenchmark(String file, int numRuns) throws Exception {
        File inputFile = new File(file);
        if (!inputFile.exists() || !inputFile.canRead()) {
            System.err.println("Input file: " + file + " not found or can't be read.");
            System.exit(1);
        }
        content = new String(IOUtils.toByteArray(new FileInputStream(file)));
        this.numRuns = 10;
        warmup = true;
        runNekoSimple();
        Thread.sleep(10000L);
        this.numRuns = numRuns;
        warmup = false;
        runNekoSimple();
    }

    private void runNekoSimple() throws Exception {
        output("NekoSimple-----------------");
        timeParseDom(nekoSimpleParser);
        timeParseDomSerialize(nekoSimpleParser);
    }

    private void output(String string) {
        if (!warmup) {
            System.out.println(string);
        }
    }

    private void timeParseDom(GadgetHtmlParser parser) throws GadgetException {
        long parseStart = System.currentTimeMillis();
        for (int i = 0; i < numRuns; ++i) {
            parser.parseDom(content);
        }
        long parseMillis = System.currentTimeMillis() - parseStart;
        output("Parsing W3C DOM [" + parseMillis + " ms total: " + ((double) parseMillis) / numRuns + "ms/run]");
    }

    private void timeParseDomSerialize(GadgetHtmlParser parser) throws GadgetException {
        org.w3c.dom.Document document = parser.parseDom(content);
        try {
            long parseStart = System.currentTimeMillis();
            for (int i = 0; i < numRuns; ++i) {
                HtmlSerialization.serialize(document);
            }
            long parseMillis = System.currentTimeMillis() - parseStart;
            output("Serializing [" + parseMillis + " ms total: " + ((double) parseMillis) / numRuns + "ms/run]");
        } catch (Exception e) {
            throw new GadgetException(GadgetException.Code.HTML_PARSE_ERROR, e);
        }
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.METHOD, "html");
            long parseStart = System.currentTimeMillis();
            for (int i = 0; i < numRuns; ++i) {
                StringWriter sw = new StringWriter((content.length() * 11) / 10);
                t.transform(new DOMSource(document), new StreamResult(sw));
                sw.toString();
            }
            long parseMillis = System.currentTimeMillis() - parseStart;
            output("Serializing DOM Transformer [" + parseMillis + " ms total: " + ((double) parseMillis) / numRuns + "ms/run]");
        } catch (Exception e) {
            throw new GadgetException(GadgetException.Code.HTML_PARSE_ERROR, e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Args: <input-file> <num-runs>");
            System.exit(1);
        }
        String fileArg = args[0];
        String runsArg = args[1];
        int numRuns = -1;
        try {
            numRuns = Integer.parseInt(runsArg);
        } catch (Exception e) {
            System.err.println("Invalid num-runs argument: " + runsArg + ", reason: " + e);
        }
        try {
            new ParseTreeSerializerBenchmark(fileArg, numRuns);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
