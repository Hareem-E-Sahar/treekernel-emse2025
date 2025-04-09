package faqparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Utility class to parse an HTML Document, aka an FAQ.
 * 
 * @author Lionel FLAHAUT
 * 
 */
public class SAXFAQParser {

    private DefaultHandler parsingHandler = null;

    /**
	 * Construct a new parser.
	 * 
	 * @param handler
	 *            an handler to handler content. Must be cool for FAQ to use
	 *            SAXFAQHTMLParsingHandler if you want to parse an FAQ, or
	 *            SAXFAQATGParsingHandler to parse an ATG representation of FAQ.
	 */
    public SAXFAQParser(DefaultHandler handler) {
        parsingHandler = handler;
    }

    /**
	 * Parse stream without closing it at the end.
	 * 
	 * @param stream
	 *            The stream to parse.
	 * @param characterEncoding
	 *            The character encoding.
	 */
    public void parse(InputStream stream, String characterEncoding) {
        parse(new InputSource(stream), characterEncoding);
    }

    /**
	 * Parse reader without closing it at the end.
	 * 
	 * @param reader
	 *            The reader to parse.
	 * @param characterEncoding
	 *            The character encoding.
	 */
    public void parse(Reader reader, String characterEncoding) {
        parse(new InputSource(reader), characterEncoding);
    }

    /**
	 * Parse the source, interpreting character with given encoding.
	 * 
	 * @param source
	 *            The source to parse.
	 * @param characterEncoding
	 *            The character encoding.
	 */
    private void parse(InputSource source, String characterEncoding) {
        try {
            XMLReader xmlreader = XMLReaderFactory.createXMLReader();
            xmlreader.setContentHandler(parsingHandler);
            source.setEncoding(characterEncoding);
            xmlreader.parse(source);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        long max = 0, mean = 0, min = Long.MAX_VALUE;
        ElementDescriptor elementDescriptor = new ElementDescriptor("div", "class", "question");
        ElementDescriptor elementDescriptor2 = new ElementDescriptor("div", "class", "reponse");
        ElementDescriptor discriminantDescriptor = new ElementDescriptor("span", "class", "highlight");
        elementDescriptor2.getParents().add(elementDescriptor);
        List<ElementDescriptor> l = new ArrayList<ElementDescriptor>();
        l.add(elementDescriptor);
        l.add(elementDescriptor2);
        for (int i = 0; i < 1; i++) {
            SAXFAQHTMLParsingHandler parsingHandler = new SAXFAQHTMLParsingHandler(l, discriminantDescriptor, true);
            SAXFAQParser parser = new SAXFAQParser(parsingHandler);
            File f = new File("exemple1.txt");
            FileInputStream fileInputStream = new FileInputStream(f);
            long start = System.currentTimeMillis();
            parser.parse(fileInputStream, "iso-8859-1");
            List questions = parsingHandler.getListOfElements();
            System.out.println(questions.size() + " results found");
            for (int j = 0; j < questions.size(); j++) {
                System.out.println("result : " + j);
                System.out.println(questions.get(j).toString());
            }
            long stop = System.currentTimeMillis();
            long value = stop - start;
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
            mean = (max + min) / 2;
            fileInputStream.close();
        }
        System.out.println("Max : " + max + " ms");
        System.out.println("Mean : " + mean + " ms");
        System.out.println("Min : " + min + " ms");
    }
}
