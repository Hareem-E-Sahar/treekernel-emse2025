package control;

import static control.Constants.DATE;
import static control.Constants.DATETIME;
import static control.Constants.INTERNAL_DATE;
import static control.Constants.INTERNAL_DATETIME;
import static control.Constants.INTERNAL_TIME;
import static control.Constants.TIME;
import static extra.Utils.addErrorMsg;
import static extra.Utils.capitalize;
import static extra.Utils.removeSpace;
import static extra.Utils.space2Underline;
import static model.domain.relationship.RelationshipConstants.ONE;
import static model.domain.relationship.RelationshipConstants.ONE_MANY;
import static model.domain.relationship.RelationshipConstants.ZERO_MANY;
import static model.xml.RAPDISConstants.ACTIVITIES;
import static model.xml.RAPDISConstants.ASSOCIATIONS;
import static model.xml.RAPDISConstants.ATTRIBUTES;
import static model.xml.RAPDISConstants.ATTRIBUTE_TYPE;
import static model.xml.RAPDISConstants.CARDINALITY_CLASS1;
import static model.xml.RAPDISConstants.CARDINALITY_CLASS2;
import static model.xml.RAPDISConstants.CLASSES;
import static model.xml.RAPDISConstants.CLASSES_CONTENT;
import static model.xml.RAPDISConstants.DECISION_NAME;
import static model.xml.RAPDISConstants.DEFINITIONS_CONTENT;
import static model.xml.RAPDISConstants.DESCRIPTION;
import static model.xml.RAPDISConstants.DESTINATION;
import static model.xml.RAPDISConstants.DIAGRAMS;
import static model.xml.RAPDISConstants.ELEMENTS;
import static model.xml.RAPDISConstants.FORK_NAME;
import static model.xml.RAPDISConstants.ID;
import static model.xml.RAPDISConstants.ID_ELEMENT;
import static model.xml.RAPDISConstants.ID_EXTERNAL;
import static model.xml.RAPDISConstants.ID_ID;
import static model.xml.RAPDISConstants.ID_LINK;
import static model.xml.RAPDISConstants.JOIN_NAME;
import static model.xml.RAPDISConstants.LINKS;
import static model.xml.RAPDISConstants.LINK_TYPE;
import static model.xml.RAPDISConstants.MAXIMUM;
import static model.xml.RAPDISConstants.MERGE_NAME;
import static model.xml.RAPDISConstants.MINIMUM;
import static model.xml.RAPDISConstants.NAME;
import static model.xml.RAPDISConstants.OBJECTS;
import static model.xml.RAPDISConstants.OBJECT_ID;
import static model.xml.RAPDISConstants.PROBABILITIES;
import static model.xml.RAPDISConstants.PROBABLE;
import static model.xml.RAPDISConstants.PROCESS_CONTENT;
import static model.xml.RAPDISConstants.RESOURCES;
import static model.xml.RAPDISConstants.RESOURCES_CONTENT;
import static model.xml.RAPDISConstants.RESOURCE_NAME;
import static model.xml.RAPDISConstants.RESOURCE_QUANTITY;
import static model.xml.RAPDISConstants.RESOURCE_TERM;
import static model.xml.RAPDISConstants.ROLE;
import static model.xml.RAPDISConstants.ROLE_CLASS1;
import static model.xml.RAPDISConstants.ROLE_CLASS2;
import static model.xml.RAPDISConstants.SOURCE;
import static model.xml.RAPDISConstants.TERMS_CONTENT;
import static model.xml.RAPDISConstants.TERMS_SUBSTANTIVES;
import static model.xml.RAPDISConstants.TOOL_DATE;
import static model.xml.RAPDISConstants.TOOL_DATETIME;
import static model.xml.RAPDISConstants.TOOL_TIME;
import static model.xml.RAPDISConstants.TYPE;
import static model.xml.RAPDISConstants.TYPE_ACTIVITY;
import static model.xml.RAPDISConstants.TYPE_ASSOCIATION;
import static model.xml.RAPDISConstants.TYPE_ASSOCIATIVE;
import static model.xml.RAPDISConstants.TYPE_CLASS;
import static model.xml.RAPDISConstants.TYPE_COMPOSITION;
import static model.xml.RAPDISConstants.TYPE_END;
import static model.xml.RAPDISConstants.TYPE_FORK;
import static model.xml.RAPDISConstants.TYPE_INPUT_OUTPUT;
import static model.xml.RAPDISConstants.TYPE_JOIN;
import static model.xml.RAPDISConstants.TYPE_MERGE;
import static model.xml.RAPDISConstants.TYPE_SPLIT;
import static model.xml.RAPDISConstants.TYPE_START;
import static model.xml.RAPDISConstants.TYPE_SWIMLANES;
import static model.xml.RapdisFiles.getClassesPath;
import static model.xml.RapdisFiles.getDefinitionsPath;
import static model.xml.RapdisFiles.getLocationPath;
import static model.xml.RapdisFiles.getProcessPath;
import static model.xml.RapdisFiles.getResourcePath;
import static model.xml.RapdisFiles.getTermsPath;
import static model.xml.XmlHelper.getNodeAttributeValue;
import static model.xml.XmlHelper.getNodeByName;
import static model.xml.XmlHelper.getNodeValue;
import static view.StylesManager.ERROR;
import static view.StylesManager.STATUS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import model.an.ActivityNetwork;
import model.an.Link;
import model.an.nodes.ActivityNode;
import model.an.nodes.Decision;
import model.an.nodes.End;
import model.an.nodes.Fork;
import model.an.nodes.Join;
import model.an.nodes.Merge;
import model.an.nodes.Start;
import model.domain.ClassDiagram;
import model.domain.DmClass;
import model.domain.relationship.Relationship;
import model.project.Project;
import model.project.Resource;
import model.xml.Definitions;
import model.xml.RAPDISConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import view.GuiConsole;
import animation.Annotation;
import com.scatter.model.dataGenerators.DataGenerator;
import com.scatter.model.metamodel.Attribute;
import com.scatter.model.metamodel.AttributeFactory;
import distributions.Sampleble;
import distributions.Triangular;
import exceptions.AnimationException;
import exceptions.ProjectBuilderException;
import extra.MessageBox;

/**
 * Creates an environment to animate the process
 * 
 * @author nando
 * @author modified by Bruno Araujo
 */
public abstract class ProjectBuilder {

    /**
	 * 
	 * @param projectPath
	 *            The path to find the files of the project
	 * @param diagramType
	 *            The type of diagram to search for (<B>ACTIVITIES</B> or
	 *            <B>CLASSES</B>)
	 * @return
	 * @throws ProjectBuilderException
	 */
    public static final String[] getDiagNames(String projectPath, String diagramType) throws ProjectBuilderException {
        String type = diagramType;
        String path = "";
        if (type.equals(CLASSES)) {
            path = getClassesPath(projectPath);
        } else {
            path = getProcessPath(projectPath);
        }
        String names = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            try {
                Document doc = builder.parse(new File(path));
                Element docElement = doc.getDocumentElement();
                Node content = null;
                if (type.equals(CLASSES)) {
                    content = getNodeByName(docElement.getChildNodes(), CLASSES_CONTENT);
                } else {
                    content = getNodeByName(docElement.getChildNodes(), PROCESS_CONTENT);
                }
                NodeList diagrams = getNodeByName(content.getChildNodes(), DIAGRAMS).getChildNodes();
                for (int i = 0; i < diagrams.getLength(); i++) {
                    Node n = diagrams.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        names += getNodeValue(getNodeByName(n.getChildNodes(), NAME));
                        if (i < diagrams.getLength() - 1) names += ";";
                    }
                }
                return names.split(";");
            } catch (SAXException e) {
                e.printStackTrace();
                throw new ProjectBuilderException(e.getMessage());
            } catch (IOException e) {
                throw new ProjectBuilderException("In order to open a project, it must contain an Activity Diagram and a Class Diagram.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProjectBuilderException(e.getMessage());
        }
    }

    private static Document loadRapdisXML(String projectPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(projectPath));
            return doc;
        } catch (Exception e) {
        }
        return null;
    }

    private static String[] loadInternalClassesOption(String projectPath) {
        Properties properties = new Properties();
        InputStream in = null;
        String[] options = new String[3];
        try {
            in = new FileInputStream(projectPath.substring(0, projectPath.lastIndexOf(".") + 1) + "properties");
            properties.load(in);
            options[0] = properties.getProperty(TOOL_DATE);
            options[1] = properties.getProperty(TOOL_TIME);
            options[2] = properties.getProperty(TOOL_DATETIME);
            in.close();
            return options;
        } catch (Exception e) {
            options[0] = "";
            options[1] = "";
            options[2] = "";
            return options;
        }
    }

    private static void saveInternalClassOption(String internalClass, boolean option, String projectPath) {
        Properties properties = new Properties();
        try {
            FileOutputStream out = null;
            out = new FileOutputStream(projectPath.substring(0, projectPath.lastIndexOf(".") + 1) + "properties");
            properties.setProperty(internalClass, String.valueOf(option));
            properties.store(out, "changed");
            out.close();
        } catch (IOException e) {
            GuiConsole.print(ERROR, e.getMessage());
        }
    }

    /**
	 * Reads the xml project file and constructs the project NDAN. The
	 * projectPath refers to the RAPDIS project, and the list of projects refers
	 * to the Process Simulator projects.
	 * 
	 * @param projectPath
	 *            The path to the RAPDIS project.
	 * @return A list of Process Simulator Projects
	 * @throws ProjectBuilderException
	 *             if shit happens.
	 */
    public static final Project buildProject(String projectPath, int classDiag, int activityDiag) throws ProjectBuilderException {
        Project result = null;
        GuiConsole.print(STATUS, "Importing UML Project " + projectPath);
        String location = getLocationPath(projectPath);
        String processPath = getProcessPath(projectPath);
        String resourcePath = getResourcePath(projectPath);
        String termsPath = getTermsPath(projectPath);
        String definitionsPath = getDefinitionsPath(projectPath);
        String classesPath = getClassesPath(projectPath);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            try {
                Document processDoc = builder.parse(new File(processPath));
                Document definitionsDoc = builder.parse(new File(definitionsPath));
                Document classesDoc = builder.parse(new File(classesPath));
                Document resourceDoc = null;
                Document termsDoc = null;
                try {
                    resourceDoc = builder.parse(new File(resourcePath));
                    termsDoc = builder.parse(new File(termsPath));
                } catch (SAXException e) {
                    e.printStackTrace();
                    throw new ProjectBuilderException(e.getMessage());
                } catch (IOException e) {
                }
                List<Resource> resourceList = buildResourcesList(resourceDoc, termsDoc);
                Definitions definitions = buildDefinitions(definitionsDoc, resourceList);
                NodeList diagramList = getDiagramList(processDoc);
                ClassDiagram classList = buildClassDiagram(projectPath, classesDoc, definitionsDoc, classDiag);
                int diagramPos = 0;
                int i = 0;
                while (i < diagramList.getLength() && result == null) {
                    Node diagramNode = diagramList.item(i);
                    if (diagramNode.getNodeType() == Node.ELEMENT_NODE) {
                        if (diagramPos == activityDiag) {
                            Project project = new Project();
                            project.activityNetwork = new ActivityNetwork(null);
                            project.resources = resourceList;
                            project.classDiagram = classList;
                            project.location = location;
                            buildProject(project, diagramNode, definitions, classList);
                            result = project;
                        }
                        diagramPos++;
                    }
                    i++;
                }
            } catch (SAXException e) {
                e.printStackTrace();
                throw new ProjectBuilderException(e.getMessage());
            } catch (IOException e) {
                throw new ProjectBuilderException("In order to open a project, it must contain an Activity Diagram and a Class Diagram.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProjectBuilderException(e.getMessage());
        }
        return result;
    }

    /**
	 * Constructs the definitions from the definitions document.
	 * 
	 * @param definitionsDoc
	 *            The document of the xml.
	 * @param resourceList
	 */
    private static Definitions buildDefinitions(Document definitionsDoc, List<Resource> resourceList) {
        Definitions def = new Definitions();
        Map<String, String> resourceIdNameMap = new HashMap<String, String>();
        for (Resource r : resourceList) {
            resourceIdNameMap.put(r.id, r.getName());
        }
        Element definitions = definitionsDoc.getDocumentElement();
        Node definitionsContent = getNodeByName(definitions.getChildNodes(), DEFINITIONS_CONTENT);
        NodeList activities = getNodeByName(definitionsContent.getChildNodes(), ACTIVITIES).getChildNodes();
        for (int i = 0; i < activities.getLength(); i++) {
            Node activity = activities.item(i);
            if (activity.getNodeType() == Node.ELEMENT_NODE) {
                String actID = getNodeAttributeValue(activity, OBJECT_ID);
                double min = new Double(getNodeAttributeValue(activity, MINIMUM));
                double mp = new Double(getNodeAttributeValue(activity, PROBABLE));
                double max = new Double(getNodeAttributeValue(activity, MAXIMUM));
                Sampleble sample = new Triangular(min, mp, max);
                def.idSamplebleMap.put(actID, sample);
                List<Resource> resourceNeededList = new LinkedList<Resource>();
                NodeList resources = getNodeByName(activity.getChildNodes(), RESOURCES).getChildNodes();
                for (int j = 0; j < resources.getLength(); j++) {
                    Node resourceNode = resources.item(j);
                    if (resourceNode.getNodeType() == Node.ELEMENT_NODE) {
                        int qty = new Integer(getNodeAttributeValue(resourceNode, RESOURCE_QUANTITY));
                        String id = getNodeValue(resourceNode);
                        Resource resource = new Resource(resourceIdNameMap.get(id), qty);
                        resourceNeededList.add(resource);
                    }
                }
                def.idResourcesMap.put(actID, resourceNeededList);
                String description = getNodeValue(getNodeByName(activity.getChildNodes(), DESCRIPTION));
                def.idDescriptionMap.put(actID, description == null ? "" : description);
            }
        }
        NodeList objects = getNodeByName(definitionsContent.getChildNodes(), OBJECTS).getChildNodes();
        for (int i = 0; i < objects.getLength(); i++) {
            Node object = objects.item(i);
            if (object.getNodeType() == Node.ELEMENT_NODE) {
                String idSource = getNodeAttributeValue(object, OBJECT_ID);
                try {
                    NodeList activities2 = getNodeByName(object.getChildNodes(), ACTIVITIES).getChildNodes();
                    String description = getNodeValue(getNodeByName(object.getChildNodes(), DESCRIPTION));
                    def.idDescriptionMap.put(idSource, description == null ? "" : description);
                    for (int j = 0; j < activities2.getLength(); j++) {
                        Node activity = activities2.item(j);
                        if (activity.getNodeType() == Node.ELEMENT_NODE) {
                            String idDestination = getNodeAttributeValue(activity, ID_ID);
                            ArrayList<Double> probList = new ArrayList<Double>();
                            NodeList probabilities = getNodeByName(activity.getChildNodes(), PROBABILITIES).getChildNodes();
                            for (int k = 0; k < probabilities.getLength(); k++) {
                                Node probability = probabilities.item(k);
                                if (probability.getNodeType() == Node.ELEMENT_NODE) {
                                    double prob = new Double(getNodeValue(probability)) / 100;
                                    probList.add(prob);
                                }
                            }
                            def.sourceDestinationProbabilityMap.put(idSource + "-" + idDestination, probList);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return def;
    }

    /**
	 * Returns the resource available list in the RAPDIS project.
	 * 
	 * @param resourceDoc
	 *            The quantity of the resources.
	 * @param termsDoc
	 *            The terms where the resource names are.
	 * @return The resource list availability.
	 */
    private static List<Resource> buildResourcesList(Document resourceDoc, Document termsDoc) {
        List<Resource> resourceList = new LinkedList<Resource>();
        if (resourceDoc != null && termsDoc != null) {
            Element resources = resourceDoc.getDocumentElement();
            Node resourcesContent = getNodeByName(resources.getChildNodes(), RESOURCES_CONTENT);
            NodeList resourceNodes = resourcesContent.getChildNodes();
            for (int i = 0; i < resourceNodes.getLength(); i++) {
                Node resourceNode = resourceNodes.item(i);
                if (resourceNode.getNodeType() == Node.ELEMENT_NODE) {
                    String termID = getNodeAttributeValue(resourceNode, RESOURCE_TERM);
                    String quantity = getNodeAttributeValue(resourceNode, RESOURCE_QUANTITY);
                    String resourceID = getNodeAttributeValue(resourceNode, ID_ID);
                    Resource resource = new Resource(termID, new Integer(quantity));
                    resource.id = resourceID;
                    resourceList.add(resource);
                }
            }
            Element terms = termsDoc.getDocumentElement();
            Node termsContent = getNodeByName(terms.getChildNodes(), TERMS_CONTENT);
            Node substantives = getNodeByName(termsContent.getChildNodes(), TERMS_SUBSTANTIVES);
            Node others = getNodeByName(substantives.getChildNodes(), RESOURCE_NAME);
            NodeList resourcesNames = others.getChildNodes();
            Map<String, String> resourceIdNameMap = new HashMap<String, String>();
            for (int i = 0; i < resourcesNames.getLength(); i++) {
                Node resourceNode = resourcesNames.item(i);
                if (resourceNode.getNodeType() == Node.ELEMENT_NODE) {
                    String id = getNodeAttributeValue(resourceNode, ID_ID);
                    String name = getNodeValue(getNodeByName(resourceNode.getChildNodes(), NAME));
                    resourceIdNameMap.put(id, name);
                }
            }
            for (Resource r : resourceList) {
                r.setName(resourceIdNameMap.get(r.getName()));
            }
        }
        return resourceList;
    }

    /**
	 * Returns the classes available list in the RAPDIS project.
	 * 
	 * @param resourceDoc
	 *            The quantity of the resources.
	 * @param termsDoc
	 *            The terms where the resource names are.
	 * @return The resource list availability.
	 */
    private static ClassDiagram buildClassDiagram(String projectPath, Document classDoc, Document definitionDoc, int diagPos) {
        ClassDiagram classDiagram = new ClassDiagram();
        ClassDiagram result = null;
        Element classes = classDoc.getDocumentElement();
        Element definitions = definitionDoc.getDocumentElement();
        Node classesContent = getNodeByName(classes.getChildNodes(), CLASSES_CONTENT);
        NodeList diagrams = getNodeByName(classesContent.getChildNodes(), DIAGRAMS).getChildNodes();
        int nodePos = 0;
        int i = 0;
        while (i < diagrams.getLength() && result == null) {
            Node diagram = diagrams.item(i);
            if (diagram.getNodeType() == Node.ELEMENT_NODE) {
                if (nodePos == diagPos) {
                    buildClasses(projectPath, classDoc, definitions, classDiagram, diagram);
                    buildRelationships(classDoc, definitions, classDiagram, diagram);
                    result = classDiagram;
                }
                nodePos++;
            }
            i++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void buildClasses(String projectPath, Document classDoc, Element definitions, ClassDiagram classDiagram, Node diagram) {
        NodeList classNodes = getNodeByName(diagram.getChildNodes(), ELEMENTS).getChildNodes();
        Node classDefinitionsContent = getNodeByName(getNodeByName(definitions.getChildNodes(), DEFINITIONS_CONTENT).getChildNodes(), CLASSES);
        NodeList classDefinitionNodes = classDefinitionsContent.getChildNodes();
        boolean dateTime = false;
        AttributeFactory factory = new AttributeFactory();
        final String invariantsRegex = "[\\s\\t]*inv[\\s\\t]+[\\w\\W]+[\\s\\t\\W\\w]";
        final String annotationsRegex = "@(\\w?)+";
        final String attrAnnotationRegex = "@attribute\\((.?)*\\)";
        final String attrValuesAnnotationRegex = "([\\w?]+)[\\s\\t]*[=][\\s\\t]*[\"]([-\\w?\\.?]+)[\"][\\s\\t]*";
        Pattern invariantsPattern = Pattern.compile(invariantsRegex);
        Pattern annotationsPattern = Pattern.compile(annotationsRegex);
        Pattern attrAnnotationPattern = Pattern.compile(attrAnnotationRegex);
        Pattern attrValuesAnnotationPattern = Pattern.compile(attrValuesAnnotationRegex);
        for (int i = 0; i < classNodes.getLength(); i++) {
            Node classNode = classNodes.item(i);
            if (classNode.getNodeType() == Node.ELEMENT_NODE) {
                String name = removeSpace(capitalize(getNodeValue(getNodeByName(classNode.getChildNodes(), NAME))));
                if (name.equalsIgnoreCase(DATETIME)) {
                    if (loadInternalClassesOption(projectPath)[2].equalsIgnoreCase("true")) {
                        dateTime = true;
                    } else if (loadInternalClassesOption(projectPath)[2].equalsIgnoreCase("false")) {
                        dateTime = false;
                    } else {
                        String answer = MessageBox.showOptionsDialogs("Do you want to use the tool internal " + DATETIME + " class instead of the one in your model?");
                        if (answer.startsWith("Yes")) {
                            dateTime = true;
                            if (answer.endsWith("SAVE")) {
                                saveInternalClassOption(TOOL_DATETIME, true, projectPath);
                            }
                        } else {
                            if (answer.endsWith("SAVE")) {
                                saveInternalClassOption(TOOL_DATETIME, false, projectPath);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < classNodes.getLength(); i++) {
            Node classNode = classNodes.item(i);
            if (classNode.getNodeType() == Node.ELEMENT_NODE && getNodeAttributeValue(classNode, ATTRIBUTE_TYPE).equals(TYPE_CLASS)) {
                String name = removeSpace(capitalize(getNodeValue(getNodeByName(classNode.getChildNodes(), NAME))));
                if (name.equalsIgnoreCase(DATE)) {
                    if (dateTime) {
                        name = INTERNAL_DATE;
                    } else if (loadInternalClassesOption(projectPath)[0].equalsIgnoreCase("true")) {
                        name = INTERNAL_DATE;
                    } else if (loadInternalClassesOption(projectPath)[0].isEmpty()) {
                        String answer = MessageBox.showOptionsDialogs("Do you want to use the tool internal " + DATE + " class instead of the one in your model?");
                        if (answer.startsWith("Yes")) {
                            name = INTERNAL_DATE;
                            if (answer.endsWith("SAVE")) {
                                saveInternalClassOption(TOOL_DATE, true, projectPath);
                            }
                        } else {
                            if (answer.endsWith("SAVE")) {
                                saveInternalClassOption(TOOL_DATE, false, projectPath);
                            }
                        }
                    }
                } else if (name.equalsIgnoreCase(TIME)) {
                    if (dateTime) {
                        name = INTERNAL_TIME;
                    } else if (loadInternalClassesOption(projectPath)[1].equalsIgnoreCase("true")) {
                        name = INTERNAL_TIME;
                    } else if (loadInternalClassesOption(projectPath)[1].isEmpty()) {
                        String answer = MessageBox.showOptionsDialogs("Do you want to use the tool internal " + TIME + " class instead of the one in your model?");
                        if (answer.startsWith("Yes")) {
                            name = INTERNAL_TIME;
                            if (answer.endsWith("SAVE")) {
                                saveInternalClassOption(TOOL_TIME, true, projectPath);
                            }
                        } else {
                            if (answer.endsWith("SAVE")) {
                                saveInternalClassOption(TOOL_TIME, false, projectPath);
                            }
                        }
                    }
                } else if (name.equalsIgnoreCase(DATETIME)) {
                    if (dateTime) {
                        name = INTERNAL_DATETIME;
                    }
                }
                DmClass dmClass = new DmClass(getNodeAttributeValue(classNode, ID), name);
                for (int j = 0; j < classDefinitionNodes.getLength(); j++) {
                    Node classDefinitionNode = classDefinitionNodes.item(j);
                    if (classDefinitionNode.getNodeType() == Node.ELEMENT_NODE && getNodeAttributeValue(classDefinitionNode, ID_ELEMENT).equals(getNodeAttributeValue(classNode, ID_EXTERNAL))) {
                        Node attributesNode = getNodeByName(classDefinitionNode.getChildNodes(), ATTRIBUTES);
                        NodeList attributeNodes = attributesNode.getChildNodes();
                        for (int k = 0; k < attributeNodes.getLength(); k++) {
                            Node attributeNode = attributeNodes.item(k);
                            if (attributeNode.getNodeType() == Node.ELEMENT_NODE) {
                                String attrName = getNodeValue(getNodeByName(attributeNode.getChildNodes(), NAME));
                                String attrType = getNodeAttributeValue(attributeNode, TYPE);
                                dmClass.addAttribute(factory.createAttribute(attrName, attrType));
                            }
                        }
                        Node description = getNodeByName(classDefinitionNode.getChildNodes(), DESCRIPTION);
                        String descValue = getNodeValue(description);
                        Matcher mi = invariantsPattern.matcher(descValue);
                        if (mi.find()) {
                            dmClass.setInv(mi.group());
                        }
                        Matcher ma = annotationsPattern.matcher(descValue);
                        if (ma.find()) {
                            dmClass.setAutoGenerated(ma.group().equalsIgnoreCase("@processIn"));
                        }
                        Matcher mAttr = attrAnnotationPattern.matcher(descValue);
                        while (mAttr.find()) {
                            Matcher mAttrValue = attrValuesAnnotationPattern.matcher(mAttr.group(0));
                            HashMap<String, String> attributeParams = new HashMap<String, String>();
                            while (mAttrValue.find()) {
                                String attrName = mAttrValue.group(1);
                                String attrValue = mAttrValue.group(2);
                                attributeParams.put(attrName, attrValue);
                            }
                            String paramName = null;
                            if ((paramName = attributeParams.get("name")) != null) {
                                Attribute attr = dmClass.getAttrMap().get(paramName);
                                try {
                                    if (attributeParams.get("generator") != null) {
                                        Class genClass = Class.forName("com.scatter.model.dataGenerators." + attributeParams.get("generator"));
                                        Constructor constructor = genClass.getConstructor(null);
                                        DataGenerator generator = (DataGenerator) constructor.newInstance(null);
                                        attr.setGenerator(generator);
                                        if (attributeParams.get("minRange") != null && attributeParams.get("maxRange") != null) {
                                            boolean isInteger = attributeParams.get("generator").equalsIgnoreCase("IntegerGenerator") || attributeParams.get("generator").equalsIgnoreCase("DateTimeGenerator");
                                            Class partypesMinRange[] = new Class[1];
                                            partypesMinRange[0] = attributeParams.get("generator").equalsIgnoreCase("FloatGenerator") ? Double.TYPE : Integer.TYPE;
                                            Class partypesMaxRange[] = new Class[1];
                                            partypesMaxRange[0] = attributeParams.get("generator").equalsIgnoreCase("FloatGenerator") ? Double.TYPE : Integer.TYPE;
                                            Method setMinRangeMeth = null;
                                            try {
                                                setMinRangeMeth = attr.getClass().getMethod("setMinRange", partypesMinRange);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            int minRangeInt, maxRangeInt = 0;
                                            double minRangeDouble, maxRangeDouble;
                                            Method setMaxRangeMeth = attr.getClass().getMethod("setMaxRange", partypesMaxRange);
                                            if (isInteger) {
                                                minRangeInt = new Integer(attributeParams.get("minRange")).intValue();
                                                maxRangeInt = new Integer(attributeParams.get("maxRange")).intValue();
                                                setMinRangeMeth.invoke(attr, minRangeInt);
                                                setMaxRangeMeth.invoke(attr, maxRangeInt);
                                            } else {
                                                minRangeDouble = new Double(attributeParams.get("minRange")).doubleValue();
                                                maxRangeDouble = new Double(attributeParams.get("maxRange")).doubleValue();
                                                setMinRangeMeth.invoke(attr, minRangeDouble);
                                                setMaxRangeMeth.invoke(attr, maxRangeDouble);
                                            }
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                classDiagram.addClass(dmClass);
            }
        }
    }

    private static void buildRelationships(Document classDoc, Element definitions, ClassDiagram classDiagram, Node diagram) {
        Node linkDefinitionsContent = getNodeByName(getNodeByName(definitions.getChildNodes(), DEFINITIONS_CONTENT).getChildNodes(), ASSOCIATIONS);
        NodeList linkDefinitionNodes = linkDefinitionsContent.getChildNodes();
        NodeList linkNodes = getNodeByName(diagram.getChildNodes(), LINKS).getChildNodes();
        for (int i = 0; i < linkNodes.getLength(); i++) {
            Node linkNode = linkNodes.item(i);
            if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
                if (!getNodeAttributeValue(linkNode, TYPE).equals(TYPE_ASSOCIATIVE)) {
                    DmClass source = classDiagram.getClass(getNodeAttributeValue(linkNode, SOURCE));
                    DmClass target = classDiagram.getClass(getNodeAttributeValue(linkNode, DESTINATION));
                    if (getNodeAttributeValue(linkNode, TYPE).equals(RAPDISConstants.TYPE_GENERALIZATION)) {
                        target.setInheritedClass(source);
                        continue;
                    }
                    Relationship relationship = new Relationship(getNodeAttributeValue(linkNode, TYPE));
                    relationship.setSource(source);
                    relationship.setDestination(target);
                    setRelDefinition(classDiagram, relationship, linkNode, linkDefinitionNodes);
                    classDiagram.addRelationship(relationship);
                    source.addRelationship(relationship, target);
                }
            }
        }
        for (int i = 0; i < linkNodes.getLength(); i++) {
            Node linkNode = linkNodes.item(i);
            if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
                if (getNodeAttributeValue(linkNode, TYPE).equals(TYPE_ASSOCIATIVE)) {
                    String linkId = getNodeAttributeValue(linkNode, DESTINATION);
                    for (int n = 0; n < linkNodes.getLength(); n++) {
                        if ((linkNodes.item(n).getNodeType() == Node.ELEMENT_NODE) && getNodeAttributeValue(linkNodes.item(n), ID_ID).equals(linkId)) {
                            Relationship relationship = new Relationship(TYPE_ASSOCIATION);
                            DmClass source = classDiagram.getClass(getNodeAttributeValue(linkNode, SOURCE));
                            relationship.setSource(source);
                            DmClass target = classDiagram.getClass(getNodeAttributeValue(linkNodes.item(n), SOURCE));
                            relationship.setDestination(target);
                            relationship.setId(linkId + " " + SOURCE);
                            relationship.setMultiplicity(ONE);
                            relationship.setMultiplicityDest(ONE);
                            setRelDefinition(classDiagram, relationship, linkNode, linkDefinitionNodes);
                            classDiagram.addRelationship(relationship);
                            source.deleteRelationship(linkId);
                            target.deleteRelationship(linkId);
                            source.addRelationship(relationship, target);
                            relationship = new Relationship(TYPE_ASSOCIATION);
                            source = classDiagram.getClass(getNodeAttributeValue(linkNode, SOURCE));
                            relationship.setSource(source);
                            target = classDiagram.getClass(getNodeAttributeValue(linkNodes.item(n), DESTINATION));
                            relationship.setDestination(target);
                            relationship.setId(linkId + " " + DESTINATION);
                            relationship.setMultiplicity(ONE);
                            relationship.setMultiplicityDest(ONE);
                            setRelDefinition(classDiagram, relationship, linkNode, linkDefinitionNodes);
                            classDiagram.addRelationship(relationship);
                            classDiagram.delRelationship(linkId);
                            source.deleteRelationship(linkId);
                            target.deleteRelationship(linkId);
                            source.addRelationship(relationship, target);
                        }
                    }
                }
            }
        }
    }

    private static void setRelDefinition(ClassDiagram cd, Relationship rel, Node linkNode, NodeList linkDefinitions) {
        for (int i = 0; i < linkDefinitions.getLength(); i++) {
            if (linkDefinitions.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Node linkDef = linkDefinitions.item(i);
                if (getNodeAttributeValue(linkDef, ID_LINK).equals(getNodeAttributeValue(linkNode, ID_ID))) {
                    if (rel.getId().isEmpty()) rel.setId(getNodeAttributeValue(linkDef, ID_LINK));
                    rel.setName(capitalize(getNodeValue(getNodeByName(linkDef.getChildNodes(), ROLE))).trim());
                    rel.setRole(getNodeValue(getNodeByName(linkDef.getChildNodes(), ROLE_CLASS1)).trim());
                    rel.setRoleDest(getNodeValue(getNodeByName(linkDef.getChildNodes(), ROLE_CLASS2)).trim());
                    if (rel.getMultiplicity() == null || rel.getMultiplicity().isEmpty()) rel.setMultiplicity(getNodeValue(getNodeByName(linkDef.getChildNodes(), CARDINALITY_CLASS1)).trim());
                    if (rel.getMultiplicityDest() == null || rel.getMultiplicityDest().isEmpty()) rel.setMultiplicityDest(getNodeValue(getNodeByName(linkDef.getChildNodes(), CARDINALITY_CLASS2)).trim());
                }
            }
        }
        if (rel.getId().isEmpty()) rel.setId(getNodeAttributeValue(linkNode, ID_ID));
        if (rel.getName() == null || rel.getName().trim().isEmpty()) {
            DmClass dmc = cd.getClass(getNodeAttributeValue(linkNode, DESTINATION));
            String pos = rel.getId().substring(rel.getId().indexOf(" ") + 1);
            if (dmc == null) {
                if (pos.equals(SOURCE)) {
                    rel.setName(cd.getClass(getNodeAttributeValue(linkNode, SOURCE)).getName() + "_" + cd.getRelationship(getNodeAttributeValue(linkNode, DESTINATION)).getSource().getName());
                } else {
                    rel.setName(cd.getClass(getNodeAttributeValue(linkNode, SOURCE)).getName() + "_" + cd.getRelationship(getNodeAttributeValue(linkNode, DESTINATION)).getDestination().getName());
                }
            } else {
                rel.setName(cd.getClass(getNodeAttributeValue(linkNode, SOURCE)).getName() + "_" + dmc.getName());
            }
        }
        if (rel.getMultiplicity() == null || rel.getMultiplicity().trim().isEmpty()) {
            if (rel.getType().equals(TYPE_COMPOSITION)) rel.setMultiplicity(ONE_MANY); else rel.setMultiplicity(ZERO_MANY);
        }
        if ((rel.getMultiplicityDest() == null || rel.getMultiplicityDest().trim().isEmpty()) && rel.getType().equals(TYPE_ASSOCIATION)) {
            rel.setMultiplicityDest(ZERO_MANY);
        }
    }

    /**
	 * Returns the node list with the diagrams in the ProcessDoc document..
	 * 
	 * @param processDoc
	 *            The doc of the xml processes file.
	 * @return All diagram process.
	 */
    private static NodeList getDiagramList(Document processDoc) {
        Element processes = processDoc.getDocumentElement();
        Node processesContent = getNodeByName(processes.getChildNodes(), PROCESS_CONTENT);
        Node diagrams = getNodeByName(processesContent.getChildNodes(), DIAGRAMS);
        return diagrams.getChildNodes();
    }

    /**
	 * Creates a Project by the diagram node of RAPDIS processes.xml file.
	 * 
	 * @param project
	 *            To build.
	 * @param diagram
	 *            to sweep.
	 */
    private static void buildProject(Project project, Node diagram, Definitions definitions, ClassDiagram classes) {
        Node name = getNodeByName(diagram.getChildNodes(), NAME);
        project.name = space2Underline(getNodeValue(name));
        Map<Short, model.an.nodes.Node> idNodeMap = new HashMap<Short, model.an.nodes.Node>();
        Map<String, String> idInternalExternalMap = new HashMap<String, String>();
        Node elements = getNodeByName(diagram.getChildNodes(), ELEMENTS);
        Node links = getNodeByName(diagram.getChildNodes(), LINKS);
        buildElements(project, elements.getChildNodes(), idNodeMap, definitions, idInternalExternalMap);
        buildLinks(project, links.getChildNodes(), idNodeMap, definitions, idInternalExternalMap);
        buildInOut(project.activityNetwork, elements.getChildNodes(), links.getChildNodes(), classes);
    }

    /**
	 * Inserts inputs and outputs to the activities of diagram. It always must
	 * be called after buildElements and buildClassDiagram
	 * 
	 * @param an
	 * @param elements
	 * @param links
	 * @throws AnimationException
	 *             if some object was modeled as input or output of an activity,
	 *             but it was not found in Class Model.
	 */
    private static void buildInOut(ActivityNetwork an, NodeList elements, NodeList links, ClassDiagram classes) throws AnimationException {
        for (model.an.nodes.Node node : an.getNodes()) {
            if (node.getClass().getName().equals("model.an.nodes.ActivityNode")) {
                ActivityNode at = (ActivityNode) node;
                for (int i = 0; i < links.getLength(); i++) {
                    Node link = links.item(i);
                    if (link.getNodeType() == Node.ELEMENT_NODE && getNodeAttributeValue(link, SOURCE).equals(at.getId())) {
                        for (int j = 0; j < elements.getLength(); j++) {
                            Node element = elements.item(j);
                            if (element.getNodeType() == Node.ELEMENT_NODE && getNodeAttributeValue(element, TYPE).equals(TYPE_INPUT_OUTPUT) && getNodeAttributeValue(element, ID).equals(getNodeAttributeValue(link, DESTINATION))) {
                                DmClass dmClass = classes.getClassByName(getNodeValue(getNodeByName(element.getChildNodes(), NAME)));
                                if (dmClass != null) {
                                    at.getOutObjects().add(dmClass);
                                } else {
                                    throw new AnimationException("Object " + getNodeValue(getNodeByName(element.getChildNodes(), NAME)) + " was modeled as output of " + at.getName() + ", but it was not found in Class Model.");
                                }
                            }
                        }
                    } else if (link.getNodeType() == Node.ELEMENT_NODE && getNodeAttributeValue(link, DESTINATION).equals(at.getId())) {
                        for (int j = 0; j < elements.getLength(); j++) {
                            Node element = elements.item(j);
                            if (element.getNodeType() == Node.ELEMENT_NODE && getNodeAttributeValue(element, TYPE).equals(TYPE_INPUT_OUTPUT) && getNodeAttributeValue(element, ID).equals(getNodeAttributeValue(link, SOURCE))) {
                                DmClass dmClass = classes.getClassByName(getNodeValue(getNodeByName(element.getChildNodes(), NAME)));
                                if (dmClass != null) {
                                    at.getInObjects().add(dmClass);
                                } else {
                                    throw new AnimationException("Object " + getNodeValue(getNodeByName(element.getChildNodes(), NAME)) + " was modeled as input of " + at.getName() + ", but it was not found in Class Model.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
	 * Build the elements with the nodes informed.
	 * 
	 * @param project
	 *            To be built.
	 * @param childNodes
	 *            The node to add.
	 * @param idNodeMap
	 *            the map id Node.
	 * @throws AnimationException
	 *             for many reasons.
	 */
    private static void buildElements(Project project, NodeList childNodes, Map<Short, model.an.nodes.Node> idNodeMap, Definitions definitions, Map<String, String> idInternalExternalMap) throws AnimationException {
        String errors = "";
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node element = childNodes.item(i);
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                String type = getNodeAttributeValue(element, TYPE);
                String name = getNodeValue(getNodeByName(element.getChildNodes(), NAME));
                String id = getNodeAttributeValue(element, ID);
                String idDef = getNodeAttributeValue(element, ID_EXTERNAL);
                if (type.equals(TYPE_ACTIVITY)) {
                    ActivityNode activity = new ActivityNode(definitions.idSamplebleMap.get(idDef), definitions.idResourcesMap.get(idDef), name, definitions.idDescriptionMap.get(idDef));
                    activity.setId(id);
                    project.activityNetwork.addNode(activity);
                    idNodeMap.put(Short.valueOf(id), activity);
                    idInternalExternalMap.put(id, idDef);
                } else if (type.equals(TYPE_FORK) && name.equals(FORK_NAME)) {
                    Fork fork = new Fork();
                    fork.setName(FORK_NAME);
                    project.activityNetwork.addNode(fork);
                    idNodeMap.put(Short.valueOf(id), fork);
                    idInternalExternalMap.put(id, idDef);
                } else if (type.equals(TYPE_JOIN) && name.equals(JOIN_NAME)) {
                    Join join = new Join();
                    join.setName(JOIN_NAME);
                    project.activityNetwork.addNode(join);
                    idNodeMap.put(Short.valueOf(id), join);
                    idInternalExternalMap.put(id, idDef);
                } else if (type.equals(TYPE_SPLIT) && name.equals(DECISION_NAME)) {
                    Decision split = new Decision();
                    try {
                        split.setName(Annotation.getDecisionMergeName("DECISION", definitions.idDescriptionMap.get(idDef)));
                        project.activityNetwork.addNode(split);
                        idNodeMap.put(Short.valueOf(id), split);
                        idInternalExternalMap.put(id, idDef);
                    } catch (AnimationException e) {
                        errors = addErrorMsg(errors, "\nMODEL ERROR: " + e.getMessage());
                    }
                } else if (type.equals(TYPE_MERGE) && name.equals(MERGE_NAME)) {
                    Merge merge = new Merge();
                    try {
                        merge.setName(Annotation.getDecisionMergeName(MERGE_NAME, definitions.idDescriptionMap.get(idDef)));
                        project.activityNetwork.addNode(merge);
                        idNodeMap.put(Short.valueOf(id), merge);
                        idInternalExternalMap.put(id, idDef);
                    } catch (AnimationException e) {
                        errors = addErrorMsg(errors, "\nMODEL ERROR: " + e.getMessage());
                    }
                } else if (type.equals(TYPE_START)) {
                    Start start = new Start();
                    project.activityNetwork.setStartNode(start);
                    project.activityNetwork.addNode(start);
                    idNodeMap.put(Short.valueOf(id), start);
                    idInternalExternalMap.put(id, idDef);
                } else if (type.equals(TYPE_END)) {
                    End end = new End();
                    project.activityNetwork.setEndNode(end);
                    project.activityNetwork.addNode(end);
                    idNodeMap.put(Short.valueOf(id), end);
                    idInternalExternalMap.put(id, idDef);
                } else if (type.equals(TYPE_SWIMLANES)) {
                    Node elements = getNodeByName(element.getChildNodes(), ELEMENTS);
                    buildElements(project, elements.getChildNodes(), idNodeMap, definitions, idInternalExternalMap);
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new AnimationException(errors);
        }
    }

    /**
	 * Build the links with the nodes informed.
	 * 
	 * @param project
	 *            To be built.
	 * @param childNodes
	 *            The node to add.
	 * @param idNodeMap
	 *            the map id Node.
	 */
    private static void buildLinks(Project project, NodeList childNodes, Map<Short, model.an.nodes.Node> idNodeMap, Definitions definitions, Map<String, String> idInternalExternalMap) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node element = childNodes.item(i);
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                String type = getNodeAttributeValue(element, TYPE);
                if (type.equals(LINK_TYPE)) {
                    String source = getNodeAttributeValue(element, SOURCE);
                    String destination = getNodeAttributeValue(element, DESTINATION);
                    model.an.nodes.Node sourceNode = idNodeMap.get(Short.valueOf(source));
                    model.an.nodes.Node destinationNode = idNodeMap.get(Short.valueOf(destination));
                    String idExternalSource = idInternalExternalMap.get(source);
                    String idExternalDestination = idInternalExternalMap.get(destination);
                    ArrayList<Double> probList = definitions.sourceDestinationProbabilityMap.get(idExternalSource + "-" + idExternalDestination);
                    String guardCondition = "";
                    if (idNodeMap.get(Short.valueOf(source)).getClass().getName().endsWith("Decision")) {
                        guardCondition = Annotation.getGuardCondition(definitions.idDescriptionMap.get(idExternalSource), sourceNode.getName(), destinationNode.getName()).replaceAll("\n", " ").trim();
                    }
                    if (probList == null) {
                        probList = new ArrayList<Double>();
                    }
                    Link link = new Link(sourceNode, destinationNode, guardCondition, probList);
                    destinationNode.addParent(sourceNode);
                    project.activityNetwork.addLink(link);
                }
            }
        }
    }
}
