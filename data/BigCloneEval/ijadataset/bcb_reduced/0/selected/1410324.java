package backend.param;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.xml.sax.SAXException;
import backend.core.security.Session;
import backend.event.type.EventType;
import backend.event.type.GeneralError;
import backend.exchange.xml.export.AbstractONDEXExport;
import backend.exchange.xml.export.ExportArguments;
import backend.intermediate.AbstractONDEXIntermediate;
import backend.intermediate.IntermediateArguments;
import backend.mapping.AbstractONDEXMapping;
import backend.mapping.MappingArguments;
import backend.param.args.ArgumentDefinition;
import backend.parser.AbstractONDEXParser;
import backend.parser.ParserArguments;

/**
 * 
 * @author hindlem
 *
 */
public class XMLParser {

    private static final String ONDEX = "ONDEX";

    private static final String NAME = "Name";

    private static final String PARSER = "Parser";

    private static final String MAP = "Map";

    private static final String INTERMEDIATE = "Intermediate";

    private static final String EXPORT = "Export";

    private static final String DDIR = "DataDirectory";

    private static final String DFILE = "DataFile";

    private static final String REMOVEPREV = "RemovePreviousGraph";

    private static final String PERSISTANTLAYER = "PersistantLayer";

    private static final String INDEXGRAPH = "IndexGraph";

    private static final String PARAMETER = "Parameter";

    private ArrayList<EventType> errors = new ArrayList<EventType>();

    private Session s;

    private static final Pattern delim = Pattern.compile(";");

    public XMLParser(Session s) {
        this.s = s;
    }

    public ONDEXinitParams parse(File xmlFile, File xsdFile) {
        String systemDataDirectory = System.getProperty("ondex.dir");
        if (systemDataDirectory == null) {
            System.err.println("ondex.dir not specified in System properties");
            System.exit(1);
        }
        if (systemDataDirectory.endsWith(File.separator)) {
            systemDataDirectory = systemDataDirectory.substring(0, systemDataDirectory.length() - 1);
        }
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        XMLInputFactory2 ifact = (XMLInputFactory2) XMLInputFactory.newInstance();
        ifact.configureForXmlConformance();
        try {
            SchemaFactory factoryx = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factoryx.newSchema(new StreamSource(xsdFile));
            Validator v = schema.newValidator();
            v.validate(new StreamSource(xmlFile));
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            ONDEXinitParams params = new ONDEXinitParams();
            XMLStreamReader2 staxXmlReader = (XMLStreamReader2) ifact.createXMLStreamReader(xmlFile);
            String currentElement = null;
            String paramName = null;
            ParserInit currentParser = null;
            IntermediateInit currentIntermediate = null;
            ExportInit currentExport = null;
            MappingMethodInit currentMap = null;
            while (staxXmlReader.hasNext()) {
                int event = staxXmlReader.next();
                switch(event) {
                    case XMLStreamConstants.START_DOCUMENT:
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        currentElement = staxXmlReader.getLocalName();
                        if (currentElement.equalsIgnoreCase(ONDEX)) {
                            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                String name = staxXmlReader.getAttributeLocalName(i);
                                String value = staxXmlReader.getAttributeValue(i);
                                if (name.equalsIgnoreCase(NAME)) {
                                    params.setOndexName(value);
                                } else if (name.equalsIgnoreCase(REMOVEPREV)) {
                                    params.setRemovePreviousGraph(Boolean.parseBoolean(value));
                                } else if (name.equalsIgnoreCase(PERSISTANTLAYER)) {
                                    params.setPersitantLayer(value.toLowerCase().trim());
                                } else if (name.equalsIgnoreCase(INDEXGRAPH)) {
                                    params.setIndexGraph(Boolean.parseBoolean(value));
                                }
                            }
                        }
                        if (currentElement.equalsIgnoreCase(PARSER)) {
                            String dir = null;
                            String file = null;
                            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                String name = staxXmlReader.getAttributeLocalName(i);
                                String value = staxXmlReader.getAttributeValue(i);
                                if (name.equalsIgnoreCase(DDIR)) {
                                    dir = value;
                                    if (dir.startsWith(File.separator) || dir.startsWith("\\") || dir.startsWith("/")) {
                                        dir = systemDataDirectory + dir;
                                    } else {
                                        dir = systemDataDirectory + File.separator + dir;
                                    }
                                } else if (name.equalsIgnoreCase(DFILE)) {
                                    file = value;
                                    if (file.startsWith(File.separator) || file.startsWith("\\") || file.startsWith("/")) {
                                        file = systemDataDirectory + file;
                                    } else {
                                        file = systemDataDirectory + File.separator + file;
                                    }
                                } else if (name.equalsIgnoreCase(NAME)) {
                                    currentParser = new ParserInit(value.toLowerCase());
                                    params.getParsers().add(currentParser);
                                    if (currentParser.getParser() == null) {
                                        String className = "backend.parser." + currentParser.getName() + ".Parser";
                                        AbstractONDEXParser parser = (AbstractONDEXParser) initObject(AbstractONDEXParser.class, className, "Parser");
                                        currentParser.setParser(parser);
                                    }
                                }
                                if (currentParser != null && dir != null) {
                                    currentParser.getParserArguments().setInputDir(dir);
                                }
                                if (currentParser != null && file != null) {
                                    currentParser.getParserArguments().setInputFile(file);
                                }
                            }
                        }
                        if (currentElement.equalsIgnoreCase(INTERMEDIATE)) {
                            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                String name = staxXmlReader.getAttributeLocalName(i);
                                String value = staxXmlReader.getAttributeValue(i);
                                if (name.equalsIgnoreCase(NAME)) {
                                    currentIntermediate = new IntermediateInit(value.toLowerCase());
                                    params.getIntermediates().add(currentIntermediate);
                                    if (currentIntermediate.getIntermediate() == null) {
                                        String className = "backend.exchange.xml.intermediate." + currentIntermediate.getName() + ".Intermediate";
                                        AbstractONDEXIntermediate intermediate = (AbstractONDEXIntermediate) initObject(AbstractONDEXIntermediate.class, className, "Intermediate");
                                        currentIntermediate.setIntermediate(intermediate);
                                    }
                                }
                            }
                        }
                        if (currentElement.equalsIgnoreCase(MAP)) {
                            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                String name = staxXmlReader.getAttributeLocalName(i);
                                String value = staxXmlReader.getAttributeValue(i);
                                if (name.equalsIgnoreCase(NAME)) {
                                    currentMap = new MappingMethodInit(value.toLowerCase());
                                    params.getMaps().add(currentMap);
                                    if (currentMap.getMapping() == null) {
                                        String className = "backend.mapping." + currentMap.getName() + ".Mapping";
                                        AbstractONDEXMapping mapping = (AbstractONDEXMapping) initObject(AbstractONDEXMapping.class, className, "Mapping");
                                        currentMap.setMapping(mapping);
                                    }
                                }
                            }
                        }
                        if (currentElement.equalsIgnoreCase(EXPORT)) {
                            String file = null;
                            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                String name = staxXmlReader.getAttributeLocalName(i);
                                String value = staxXmlReader.getAttributeValue(i);
                                if (name.equalsIgnoreCase(DFILE)) {
                                    file = value;
                                    if (file.startsWith(File.separator) || file.startsWith("\\") || file.startsWith("/")) {
                                        file = systemDataDirectory + file;
                                    } else {
                                        file = systemDataDirectory + File.separator + file;
                                    }
                                } else if (name.equalsIgnoreCase(NAME)) {
                                    currentExport = new ExportInit(value.toLowerCase());
                                    params.getExports().add(currentExport);
                                    if (currentExport.getExporter() == null) {
                                        String className = "backend.exchange.xml.export." + currentExport.getName() + ".Export";
                                        AbstractONDEXExport export = (AbstractONDEXExport) initObject(AbstractONDEXExport.class, className, "Export");
                                        currentExport.setExporter(export);
                                    }
                                }
                                if (currentExport != null && file != null) {
                                    currentExport.getExportArguments().setExportFile(file);
                                }
                            }
                        }
                        if (currentElement.equalsIgnoreCase(PARAMETER)) {
                            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                String name = staxXmlReader.getAttributeLocalName(i);
                                String value = staxXmlReader.getAttributeValue(i);
                                if (name.equalsIgnoreCase(NAME)) {
                                    paramName = value;
                                }
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String elementName = staxXmlReader.getLocalName();
                        currentElement = null;
                        if (elementName.equalsIgnoreCase(PARSER) || elementName.equalsIgnoreCase(MAP) || elementName.equalsIgnoreCase(EXPORT)) {
                            if (currentParser != null) {
                                currentParser.complementMissingArguments();
                            }
                            if (currentMap != null) {
                                currentMap.complementMissingArguments();
                            }
                            if (currentExport != null) {
                                currentExport.complementMissingArguments();
                            }
                            currentParser = null;
                            currentMap = null;
                            currentExport = null;
                            paramName = null;
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        String chars = staxXmlReader.getText().trim();
                        if (chars.length() < 1) continue;
                        if (currentParser == null && currentElement != null && currentElement.equalsIgnoreCase(PERSISTANTLAYER)) {
                            params.setPersitantLayer(chars.toLowerCase().trim());
                        } else if (currentParser != null && currentElement.equalsIgnoreCase(PARAMETER) && paramName != null) {
                            Map<String, List<Object>> parserOptions = currentParser.getParserArguments().getOptions();
                            List<Object> list = parserOptions.get(paramName);
                            if (list == null) {
                                list = new ArrayList<Object>(1);
                                parserOptions.put(paramName, list);
                            }
                            String[] values = delim.split(chars);
                            for (String value : values) {
                                list.add(castToParserArgNativeObject(value, currentParser.getParser(), paramName));
                            }
                        } else if (currentIntermediate != null && currentElement.equalsIgnoreCase(PARAMETER) && paramName != null) {
                            Map<String, List<Object>> intermediateOptions = currentIntermediate.getIntermediateArguments().getOptions();
                            List<Object> list = intermediateOptions.get(paramName);
                            if (list == null) {
                                list = new ArrayList<Object>(1);
                                intermediateOptions.put(paramName, list);
                            }
                            String[] values = delim.split(chars);
                            for (String value : values) {
                                list.add(castToIntermediateArgNativeObject(value, currentIntermediate, paramName));
                            }
                        } else if (currentMap != null && currentElement != null && paramName != null) {
                            Map<String, List<Object>> mappingOptions = currentMap.getMappingArguments().getOptions();
                            List<Object> list = mappingOptions.get(paramName);
                            if (list == null) {
                                list = new ArrayList<Object>(1);
                                mappingOptions.put(paramName, list);
                            }
                            String[] values = delim.split(chars);
                            for (String value : values) {
                                list.add(castMappingArgToNativeObject(value, currentMap, paramName));
                            }
                        } else if (currentExport != null && currentElement != null && paramName != null) {
                            Map<String, List<Object>> exportOptions = currentExport.getExportArguments().getOptions();
                            List<Object> list = exportOptions.get(paramName);
                            if (list == null) {
                                list = new ArrayList<Object>(1);
                                exportOptions.put(paramName, list);
                            }
                            String[] values = delim.split(chars);
                            for (String value : values) {
                                list.add(castExportArgToNativeObject(value, currentExport, paramName));
                            }
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        ifact = null;
                        staxXmlReader.close();
                        return params;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        ifact = null;
        return null;
    }

    /**
	 * 
	 * @param chars
	 * @param currentIntermediate
	 * @param paramName
	 * @return
	 */
    private Object castToIntermediateArgNativeObject(String chars, IntermediateInit currentIntermediate, String paramName) {
        AbstractONDEXIntermediate intermediate = currentIntermediate.getIntermediate();
        if (intermediate != null) {
            for (ArgumentDefinition definition : intermediate.getArgumentDefinitions()) {
                if (definition.getName().equalsIgnoreCase(paramName)) {
                    Object obj = definition.getDefaultValue();
                    try {
                        obj = definition.parseString(chars);
                    } catch (Exception e) {
                        errors.add(new GeneralError("The " + currentIntermediate + " Parameter " + paramName + " is invalid for value " + chars + " \n error:" + e.getMessage()));
                        return definition.getDefaultValue();
                    }
                    if (obj == null) {
                        errors.add(new GeneralError("The " + currentIntermediate + " Parameter " + paramName + " does not support instansiation from String"));
                        return definition.getDefaultValue();
                    }
                    return obj;
                }
            }
            errors.add(new GeneralError("The " + intermediate.getClass() + " Parameter " + paramName + " is not a argument"));
        }
        return null;
    }

    /**
	 * 
	 * @param chars
	 * @param currentExport
	 * @param paramName
	 * @return
	 */
    private Object castExportArgToNativeObject(String chars, ExportInit currentExport, String paramName) {
        AbstractONDEXExport export = currentExport.getExporter();
        if (export != null) {
            for (ArgumentDefinition definition : export.getArgumentDefinitions()) {
                if (definition.getName().equalsIgnoreCase(paramName)) {
                    Object obj = definition.getDefaultValue();
                    try {
                        obj = definition.parseString(chars);
                    } catch (Exception e) {
                        errors.add(new GeneralError("The " + currentExport + " Parameter " + paramName + " is invalid for value " + chars + " \n error:" + e.getMessage()));
                        return definition.getDefaultValue();
                    }
                    if (obj == null) {
                        errors.add(new GeneralError("The " + currentExport + " Parameter " + paramName + " does not support instansiation from String"));
                        return definition.getDefaultValue();
                    }
                    return obj;
                }
            }
            errors.add(new GeneralError("The " + export.getClass() + " Parameter " + paramName + " is not an argument"));
        }
        return null;
    }

    /**
	 * 
	 * @param chars
	 * @param currentMapping
	 * @param paramName
	 * @return
	 */
    private Object castMappingArgToNativeObject(String chars, MappingMethodInit currentMapping, String paramName) {
        AbstractONDEXMapping mapping = currentMapping.getMapping();
        if (mapping != null) {
            for (ArgumentDefinition definition : mapping.getArgumentDefinitions()) {
                if (definition.getName().equalsIgnoreCase(paramName)) {
                    Object obj = definition.getDefaultValue();
                    try {
                        obj = definition.parseString(chars);
                    } catch (Exception e) {
                        errors.add(new GeneralError("The " + currentMapping + " Parameter " + paramName + " is invalid for value " + chars + " \n error:" + e.getMessage()));
                        return definition.getDefaultValue();
                    }
                    if (obj == null) {
                        errors.add(new GeneralError("The " + currentMapping + " Parameter " + paramName + " does not support instansiation from String"));
                        return definition.getDefaultValue();
                    }
                    return obj;
                }
            }
            errors.add(new GeneralError("The " + mapping.getClass() + " Parameter " + paramName + " is not an argument"));
        }
        return null;
    }

    /**
	 * 
	 * @param chars
	 * @param currentParser
	 * @param paramName
	 * @return
	 */
    private Object castToParserArgNativeObject(String chars, AbstractONDEXParser parser, String paramName) {
        if (parser != null) {
            for (ArgumentDefinition definition : parser.getArgumentDefinitions()) {
                if (definition.getName().equalsIgnoreCase(paramName)) {
                    Object obj = definition.getDefaultValue();
                    try {
                        obj = definition.parseString(chars);
                    } catch (Exception e) {
                        errors.add(new GeneralError("The " + parser.getName() + " Parameter " + paramName + " is invalid for value " + chars + " \n error:" + e.getMessage()));
                        return definition.getDefaultValue();
                    }
                    if (obj == null) {
                        errors.add(new GeneralError("The " + parser.getName() + " Parameter " + paramName + " does not support instansiation from String"));
                        return definition.getDefaultValue();
                    }
                    return obj;
                }
            }
            errors.add(new GeneralError("The " + parser.getClass() + " Parameter " + paramName + " is not an argument"));
        }
        return null;
    }

    private Object initObject(Class c, String className, String type) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            errors.add(new GeneralError(type + " plugin " + className + " does not exist"));
            return null;
        }
        try {
            Class<?>[] args = new Class<?>[] { Session.class };
            Constructor constructor = c.getClassLoader().loadClass(className).getConstructor(args);
            return constructor.newInstance(new Object[] { s });
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * 
	 * @author hindlem
	 *
	 */
    public static class ONDEXinitParams {

        private String ondexName = null;

        private boolean removePreviousGraph = false;

        private boolean indexGraph = true;

        private String persistantLayer = "berkeley";

        private Set<ParserInit> parsers = new LinkedHashSet<ParserInit>();

        private List<IntermediateInit> intermediates = new LinkedList<IntermediateInit>();

        private List<MappingMethodInit> maps = new LinkedList<MappingMethodInit>();

        private List<ExportInit> exports = new LinkedList<ExportInit>();

        public void setPersitantLayer(String persistantLayer) {
            this.persistantLayer = persistantLayer;
        }

        public void setIndexGraph(boolean index) {
            this.indexGraph = index;
        }

        public void setOndexName(String ondexName) {
            this.ondexName = ondexName;
        }

        public String getOndexName() {
            return ondexName;
        }

        public List<MappingMethodInit> getMaps() {
            return maps;
        }

        public boolean isRemovePreviousGraph() {
            return removePreviousGraph;
        }

        public boolean indexGraph() {
            return indexGraph;
        }

        public void setRemovePreviousGraph(boolean removePreviousGraph) {
            this.removePreviousGraph = removePreviousGraph;
        }

        public String getPersistantLayer() {
            return persistantLayer;
        }

        public List<ExportInit> getExports() {
            return exports;
        }

        public Set<ParserInit> getParsers() {
            return parsers;
        }

        public List<IntermediateInit> getIntermediates() {
            return intermediates;
        }

        public void setIntermediates(List<IntermediateInit> intermediates) {
            this.intermediates = intermediates;
        }
    }

    /**
	 * 
	 * @author hindlem
	 *
	 */
    public static class ParserInit {

        private AbstractONDEXParser parser = null;

        private ParserArguments args = new ParserArguments();

        private String name;

        public ParserInit(String name) {
            this.name = name;
        }

        public ParserArguments getParserArguments() {
            return args;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AbstractONDEXParser getParser() {
            return parser;
        }

        public void setParser(AbstractONDEXParser parser) {
            this.parser = parser;
        }

        /**
		 *	Add's the arguments missing and sets them to the default.
		 *	Unless the argument is required && defaultValue != null;
		 */
        public void complementMissingArguments() {
            if (parser == null) return;
            ArgumentDefinition[] arguList = parser.getArgumentDefinitions();
            Map<String, List<Object>> options = args.getOptions();
            for (int a = 0; a < arguList.length; a++) {
                ArgumentDefinition argDef = arguList[a];
                if (!argDef.isRequiredArgument() && argDef.getDefaultValue() != null) {
                    if (!options.containsKey(argDef.getName())) {
                        List<Object> defaultOption = new ArrayList<Object>(1);
                        defaultOption.add(argDef.getDefaultValue());
                        options.put(argDef.getName(), defaultOption);
                    }
                }
            }
        }
    }

    /**
	 * 
	 * @author hindlem
	 *
	 */
    public static class IntermediateInit {

        private AbstractONDEXIntermediate intermediate = null;

        private IntermediateArguments args = new IntermediateArguments();

        private String name;

        public IntermediateInit(String name) {
            this.name = name;
        }

        public IntermediateArguments getIntermediateArguments() {
            return args;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AbstractONDEXIntermediate getIntermediate() {
            return intermediate;
        }

        public void setIntermediate(AbstractONDEXIntermediate intermediate) {
            this.intermediate = intermediate;
        }

        /**
		 *	Add's the arguments missing and sets them to the default.
		 *	Unless the argument is required && defaultValue != null;
		 */
        public void complementMissingArguments() {
            if (intermediate == null) return;
            ArgumentDefinition[] arguList = intermediate.getArgumentDefinitions();
            Map<String, List<Object>> options = args.getOptions();
            for (int a = 0; a < arguList.length; a++) {
                ArgumentDefinition argDef = arguList[a];
                if (!argDef.isRequiredArgument() && argDef.getDefaultValue() != null) {
                    if (!options.containsKey(argDef.getName())) {
                        List<Object> defaultOption = new ArrayList<Object>(1);
                        defaultOption.add(argDef.getDefaultValue());
                        options.put(argDef.getName(), defaultOption);
                    }
                }
            }
        }
    }

    /**
	 * 
	 * @author hindlem
	 *
	 */
    public static class ExportInit {

        private AbstractONDEXExport exporter = null;

        private ExportArguments args = new ExportArguments();

        private String name;

        public ExportInit(String name) {
            this.name = name;
        }

        public ExportArguments getExportArguments() {
            return args;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AbstractONDEXExport getExporter() {
            return exporter;
        }

        public void setExporter(AbstractONDEXExport exporter) {
            this.exporter = exporter;
        }

        /**
		 *	Add's the arguments missing and sets them to the default.
		 *	Unless the argument is required && defaultValue != null;
		 */
        public void complementMissingArguments() {
            if (exporter == null) return;
            ArgumentDefinition[] arguList = exporter.getArgumentDefinitions();
            Map<String, List<Object>> options = args.getOptions();
            for (int a = 0; a < arguList.length; a++) {
                ArgumentDefinition argDef = arguList[a];
                if (!argDef.isRequiredArgument() && argDef.getDefaultValue() != null) {
                    if (!options.containsKey(argDef.getName())) {
                        List<Object> defaultOption = new ArrayList<Object>(1);
                        defaultOption.add(argDef.getDefaultValue());
                        options.put(argDef.getName(), defaultOption);
                    }
                }
            }
        }
    }

    /**
	 * 
	 * @author hindlem
	 *
	 */
    public static class MappingMethodInit {

        private AbstractONDEXMapping mapping = null;

        private MappingArguments args = new MappingArguments();

        private String name;

        public MappingMethodInit(String name) {
            this.name = name;
        }

        public MappingArguments getMappingArguments() {
            return args;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AbstractONDEXMapping getMapping() {
            return mapping;
        }

        public void setMapping(AbstractONDEXMapping mapping) {
            this.mapping = mapping;
        }

        /**
		 *	Add's the arguments missing and sets them to the default.
		 *	Unless the argument is required && defaultValue != null;
		 */
        public void complementMissingArguments() {
            if (mapping == null) return;
            ArgumentDefinition[] arguList = mapping.getArgumentDefinitions();
            Map<String, List<Object>> options = args.getOptions();
            for (int a = 0; a < arguList.length; a++) {
                ArgumentDefinition argDef = arguList[a];
                if (!argDef.isRequiredArgument() && argDef.getDefaultValue() != null) {
                    if (!options.containsKey(argDef.getName())) {
                        List<Object> defaultOption = new ArrayList<Object>(1);
                        defaultOption.add(argDef.getDefaultValue());
                        options.put(argDef.getName(), defaultOption);
                    }
                }
            }
        }
    }

    /**
	 * 
	 * @return errors or warnings thrown during parsing
	 */
    public ArrayList<EventType> getErrors() {
        return errors;
    }
}
