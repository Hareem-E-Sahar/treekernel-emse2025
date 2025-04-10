import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility for reading Dalmatian's XML configuration file.
 */
public class Configuration {

    private static String filename;

    private static Document document = null;

    /**
	 * Initialize attributes.
	 */
    static {
        initFilename();
        initDocument();
    }

    /**
	 * Gets the value of an option.
	 * 
	 * @param category
	 *     Category the option is located in.
	 * @param option
	 *     Option to retrieve.
	 */
    public static String getOption(String category, String option) throws IOException {
        NodeList nodeList;
        Element categoryElement, optionElement;
        nodeList = document.getElementsByTagName(category);
        if (nodeList.getLength() == 0) throw new IOException("[Configuration] Could not find category.");
        categoryElement = (Element) nodeList.item(0);
        nodeList = categoryElement.getElementsByTagName(option);
        if (nodeList.getLength() == 0) throw new IOException("[Configuration] Could not find option.");
        optionElement = (Element) nodeList.item(0);
        return optionElement.getFirstChild().getNodeValue();
    }

    /**
	 * Initializes and parses the document.
	 */
    private static void initDocument() {
        DocumentBuilder builder;
        DocumentBuilderFactory factory;
        NodeList list;
        String message;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(filename);
        } catch (ParserConfigurationException pce) {
            message = "[Configuration] Cannot create XML parser.";
            throw new ExceptionInInitializerError(message);
        } catch (SAXException se) {
            message = "[Configuration] Cannot parse configuration file.";
            throw new ExceptionInInitializerError(message);
        } catch (IOException ie) {
            message = "[Configuration] Cannot open configuration file.";
            throw new ExceptionInInitializerError(message);
        }
    }

    private static void initFilename() {
        String home;
        home = System.getProperty("user.home");
        filename = home + "/.config/dalmatian/configuration.xml";
    }

    /**
	 * Tests %Configuration.
	 */
    public static void main(String[] args) {
        String host, name, user, password;
        System.out.println();
        System.out.println("****************************************");
        System.out.println("Configuration");
        System.out.println("****************************************");
        System.out.println();
        try {
            host = Configuration.getOption("database", "host");
            name = Configuration.getOption("database", "name");
            user = Configuration.getOption("database", "user");
            password = Configuration.getOption("database", "password");
            System.out.printf("%s %s %s %s\n", host, name, user, password);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println();
        System.out.println("****************************************");
        System.out.println("Configuration");
        System.out.println("****************************************");
        System.out.println();
    }
}
