package de.fraunhofer.isst.axbench.operations;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import de.fraunhofer.isst.axbench.api.AXLMessage;
import de.fraunhofer.isst.axbench.api.AXLMessage.MessageType;
import de.fraunhofer.isst.axbench.api.operations.AbstractAXLFileOperation;
import de.fraunhofer.isst.axbench.api.operations.OperationInterface;
import de.fraunhofer.isst.axbench.api.operations.OperationParameter;
import de.fraunhofer.isst.axbench.api.operations.ParameterTypes;
import de.fraunhofer.isst.axbench.axlang.api.IAXLangElement;
import de.fraunhofer.isst.axbench.axlang.elements.Model;
import de.fraunhofer.isst.axbench.axlang.utilities.AXLException;
import de.fraunhofer.isst.axbench.axlang.utilities.AXLangAttribute;
import de.fraunhofer.isst.axbench.axlang.utilities.AXLangDefinition.AdditionalXML;
import de.fraunhofer.isst.axbench.axlang.utilities.ReferenceKind;
import de.fraunhofer.isst.axbench.axlang.utilities.Role;
import de.fraunhofer.isst.axbench.axlang.utilities.attributes.Attributes;

/** 
 * @brief Converter from XML (using JDOM).
 * 
 * @todo include version check
 * @bug reading does not work yet
 * 
 * @author ekleinod
 * @version 0.9.0
 * @since 0.3
 */
public class XMLReader extends AbstractAXLFileOperation {

    private static final String ID_IN_FILENAME = "xmlfilein";

    private static final String ID_IN_SYSTEMPATH = "systempath";

    private static final String ID_OUT_ELEMENT = "axlout";

    /**
	 * @brief Constructor.
	 */
    public XMLReader() {
        super();
        OperationInterface ifInput = new OperationInterface();
        OperationParameter prmFile = new OperationParameter(ParameterTypes.FILE_IN, ID_IN_FILENAME, "aXLang XML file");
        prmFile.setExtensions(".xml");
        ifInput.addParameter(prmFile);
        OperationParameter prmPath = new OperationParameter(ParameterTypes.STRING, ID_IN_SYSTEMPATH, "XML system path", "XML system path for DTD");
        ifInput.addParameter(prmPath);
        setOperationInterface(ifInput, ParameterDirection.INPUT);
        OperationInterface ifOutput = new OperationInterface();
        OperationParameter prmElement = new OperationParameter(ParameterTypes.ELEMENT, ID_OUT_ELEMENT, "read aXLang element");
        ifOutput.addParameter(prmElement);
        setOperationInterface(ifOutput, ParameterDirection.OUTPUT);
    }

    /**
	 * @brief Reads an aXLang element from XML.
	 * 
	 * @param theInputParameters map of the input parameters (empty map or null allowed)
	 * @return map of the output parameters (empty map allowed)
	 */
    @Override
    protected Map<String, Object> executeConcreteOperation(Map<String, Object> theInputParameters) {
        Map<String, Object> mapReturn = super.executeConcreteOperation(theInputParameters);
        if (getCurrentFile() != null) {
            addAXLMessage(new AXLMessage(MessageFormat.format("Reading XML file ''{0}''.", getCurrentFile().getConcRelativeFilename())));
            try {
                SAXBuilder theBuilder = new SAXBuilder();
                theBuilder.setValidation(true);
                Document jdomDocument = null;
                if ("".equals(((String) theInputParameters.get(ID_IN_SYSTEMPATH)).trim())) {
                    jdomDocument = theBuilder.build(getCurrentFile().getInputStream());
                } else {
                    jdomDocument = theBuilder.build(getCurrentFile().getInputStream(), MessageFormat.format("{0}{1}{2}", getCurrentFile().getAbsolutePath(), File.separator, theInputParameters.get(ID_IN_SYSTEMPATH)));
                }
                addAXLMessage(MessageFormat.format("XML of file ''{0}'' valid.", getCurrentFile().getConcRelativeFilename()), MessageType.MESSAGE);
                mapReturn.put(ID_OUT_ELEMENT, fromJDOM(jdomDocument.getRootElement().getChild(AdditionalXML.ELEMENT.getXML()), null, null));
                addAXLMessage(MessageFormat.format("File ''{0}'' successfully read.", getCurrentFile().getConcRelativeFilename()), MessageType.MESSAGE);
            } catch (Exception e) {
                addAXLMessage(e.getMessage(), MessageType.ERROR);
            }
        }
        return mapReturn;
    }

    /**
	 * @brief Read and convert one element.
	 * 
	 * @param theJDOMElement JDOM element
	 * @param theModel aXLang model
	 * @param theParent current parent element
	 */
    private IAXLangElement fromJDOM(Element theJDOMElement, Model theModel, IAXLangElement theParent) {
        IAXLangElement axlReturn = null;
        Model axlModel = theModel;
        try {
            ReferenceKind theKind = ReferenceKind.valueOf(theJDOMElement.getAttributeValue(Attributes.KIND.getID()).toUpperCase());
            Role theRole = Role.valueOf(theJDOMElement.getAttributeValue(Attributes.ROLE.getID()).toUpperCase());
            Class<? extends IAXLangElement> clsElement = theRole.getType();
            Constructor<?> theConstructor = null;
            try {
                theConstructor = clsElement.getConstructor();
                axlReturn = (Model) theConstructor.newInstance();
                axlModel = (Model) axlReturn;
            } catch (NoSuchMethodException e) {
                try {
                    theConstructor = clsElement.getConstructor(Model.class, IAXLangElement.class);
                    axlReturn = (IAXLangElement) theConstructor.newInstance(axlModel, theParent);
                } catch (NoSuchMethodException e1) {
                }
            }
            if (axlReturn == null) {
                throw new AXLException("Element could not be constructed from XML.");
            }
            if (theParent != null) {
                theParent.addElement(axlReturn, theKind, theRole);
            }
            for (Element theAttributeElement : (List<Element>) theJDOMElement.getChildren(AdditionalXML.ATTRIBUTE.getXML())) {
                axlReturn.addAttribute(new AXLangAttribute(theAttributeElement.getAttributeValue(Attributes.IDENTIFIER.getID()), theAttributeElement.getTextTrim(), Boolean.parseBoolean(theAttributeElement.getAttributeValue(Attributes.ISOPTIONAL.getID())), Boolean.parseBoolean(theAttributeElement.getAttributeValue(Attributes.ISDELETEABLE.getID()))));
            }
            for (Element theElement : (List<Element>) theJDOMElement.getChildren(AdditionalXML.ELEMENT.getXML())) {
                fromJDOM(theElement, axlModel, axlReturn);
            }
        } catch (Exception e) {
            addAXLMessage(e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
        return axlReturn;
    }
}
