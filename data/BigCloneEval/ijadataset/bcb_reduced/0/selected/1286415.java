package nl.utwente.ewi.stream.network;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.utwente.ewi.stream.utils.SourceInfo;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Reads source nodes from the sources.xml file
 *
 * @author rein
 */
public class Sources {

    private static final String SOURCES_FILENAME = "sources.xml";

    private static final Logger logger = Logger.getLogger(Sources.class.getName());

    /**
   * This method tries to instantiate a source node that is defined in the sources.xml file.
   * @param sourceName - The source name
   * @param name - The name that the source PE should get
   * @param attributes - The columns that should be projected by the source node
   * @param conditions - The conditions to which incoming data should comply.
   * @return A StreamSource instance, or null.
   */
    public static StreamSource getSourceNodeInstance(String sourceName, String name, List attributes, Map conditions) {
        SAXBuilder builder = new SAXBuilder();
        try {
            InputStream is = QueryNetworkManager.config.getClass().getClassLoader().getResourceAsStream(SOURCES_FILENAME);
            Document doc = builder.build(is);
            List sources = doc.getRootElement().getChildren();
            boolean found = false;
            for (int i = 0; i < sources.size() && !found; i++) {
                Element source = (Element) sources.get(i);
                if (source.getChild("name").getTextTrim().equals(sourceName)) {
                    found = true;
                    String klazz = source.getChild("class").getTextTrim();
                    Map<String, String> options = new HashMap<String, String>();
                    Element opts;
                    if ((opts = source.getChild("options")) != null) {
                        for (Object o : opts.getChildren()) {
                            Element el = (Element) o;
                            options.put(el.getName(), el.getTextTrim());
                        }
                    }
                    Constructor c = Class.forName(klazz).getConstructor(String.class, Map.class, List.class, Map.class);
                    return (StreamSource) c.newInstance(name, options, attributes, conditions);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting stream source: ", ex);
        }
        return null;
    }

    public static List<SourceInfo> getSourcesInfo() {
        ArrayList<SourceInfo> info = new ArrayList<SourceInfo>();
        SAXBuilder builder = new SAXBuilder();
        InputStream is = QueryNetworkManager.config.getClass().getClassLoader().getResourceAsStream(SOURCES_FILENAME);
        try {
            Document doc = builder.build(is);
            List sources = doc.getRootElement().getChildren();
            boolean found = false;
            for (int i = 0; i < sources.size() && !found; i++) {
                Element source = (Element) sources.get(i);
                if (source.getChild("desc") != null) {
                    info.add(new SourceInfo(source.getChild("name").getTextTrim(), source.getChild("desc").getTextTrim()));
                } else {
                    info.add(new SourceInfo(source.getChild("name").getTextTrim()));
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error getting source information: ", ex);
        }
        return info;
    }
}
