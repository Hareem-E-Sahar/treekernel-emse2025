package gr.ageorgiadis.signature.xmldsig;

import gr.ageorgiadis.signature.ElementHandlerImpl;
import gr.ageorgiadis.util.HexStringHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;
import org.w3c.dom.html.HTMLTextAreaElement;

public class XMLDSigHandler extends ElementHandlerImpl {

    private static final Log logger = LogFactory.getLog(XMLDSigHandler.class);

    protected boolean unselectedOptionIncluded = false;

    /** Flag to control whether unselected option elements (belonging to a
	 * select element) are included in the XMLDSig or not */
    public void setUnselectedOptionIncluded(boolean unselectedOptionsIncluded) {
        logger.debug("Setting unselectedOptionsIncluded to: " + unselectedOptionsIncluded);
        this.unselectedOptionIncluded = unselectedOptionsIncluded;
    }

    protected boolean unselectedCheckboxIncluded = true;

    /** Flag to control whether unselected checkbox elements are included in
	 * the XMLDSig or not */
    public void setUnselectedCheckboxIncluded(boolean unselectedCheckboxesIncluded) {
        logger.debug("Setting unselectedCheckboxIncluded to: " + unselectedCheckboxIncluded);
        this.unselectedCheckboxIncluded = unselectedCheckboxesIncluded;
    }

    protected boolean unselectedRadioIncluded = false;

    /** Flag to control whether unselected radio buttons are included in the 
	 * XMLDSig or not */
    public void setUnselectedRadioIncluded(boolean unselectedRadioIncluded) {
        logger.debug("Setting unselectedRadioIncluded to: " + unselectedRadioIncluded);
        this.unselectedRadioIncluded = unselectedRadioIncluded;
    }

    private final Document document;

    public Document getDocument() {
        return document;
    }

    private static DocumentBuilderFactory dbf = null;

    private static synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
        if (dbf == null) {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        }
        return dbf;
    }

    public XMLDSigHandler() {
        try {
            document = getDocumentBuilderFactory().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onHTMLFormElement(HTMLFormElement element) {
        Element formElem = document.createElement("form");
        formElem.setAttribute("id", element.getId());
        formElem.setAttribute("name", element.getName());
        document.appendChild(formElem);
    }

    @Override
    public void onHTMLSelectElement(HTMLSelectElement element) {
        Element selectElem = document.createElement("select");
        selectElem.setAttribute("name", element.getName());
        selectElem.setAttribute("multiple", String.valueOf(element.getMultiple()));
        HTMLCollection options = element.getOptions();
        if (options != null) {
            for (int i = 0; i < options.getLength(); i++) {
                HTMLOptionElement optionElement = (HTMLOptionElement) options.item(i);
                onHTMLOptionElement(optionElement, selectElem);
            }
        } else {
            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase("option")) {
                    onHTMLOptionElement((HTMLOptionElement) node, selectElem);
                }
            }
        }
        document.getDocumentElement().appendChild(selectElem);
    }

    @Override
    public void onHTMLOptionElement(HTMLOptionElement element, Object selectObject) {
        Element selectElem = (Element) selectObject;
        Element optionElem = document.createElement("option");
        optionElem.setAttribute("value", element.getValue());
        boolean selected = element.getSelected();
        optionElem.setAttribute("selected", String.valueOf(selected));
        if (selected || (!selected && unselectedOptionIncluded)) {
            selectElem.appendChild(optionElem);
        }
    }

    @Override
    public void onHTMLTextAreaElement(HTMLTextAreaElement element) {
        String name = element.getName();
        String value = element.getValue();
        if (value == null || value.trim().length() == 0) {
            logger.debug("Textarea element is empty; element.name=" + name);
            return;
        }
        Element textareaElem = document.createElement("textarea");
        textareaElem.setAttribute("name", name);
        CDATASection cdataSection = document.createCDATASection(value);
        textareaElem.appendChild(cdataSection);
        document.getDocumentElement().appendChild(textareaElem);
    }

    private Element handleHTMLInputElement(HTMLInputElement element) {
        Element inputElem = document.createElement("input");
        inputElem.setAttribute("type", element.getType());
        return inputElem;
    }

    @Override
    public void onHTMLInputButtonElement(HTMLInputElement element) {
        String name = element.getName();
        String value = element.getValue();
        if (value == null || value.trim().length() == 0) {
            logger.debug("Button input element is empty; element.name=" + name);
            return;
        }
        Element buttonElem = handleHTMLInputElement(element);
        buttonElem.setAttribute("name", element.getName());
        buttonElem.setAttribute("value", element.getValue());
        document.getDocumentElement().appendChild(buttonElem);
    }

    @Override
    public void onHTMLInputCheckboxElement(HTMLInputElement element) {
        Element checkboxElem = handleHTMLInputElement(element);
        checkboxElem.setAttribute("name", element.getName());
        boolean checked = element.getChecked();
        checkboxElem.setAttribute("checked", "" + checked);
        checkboxElem.setAttribute("value", checked ? element.getValue() : "");
        if (checked || (!checked && unselectedCheckboxIncluded)) {
            document.getDocumentElement().appendChild(checkboxElem);
        }
    }

    @Override
    public void onHTMLInputFileElement(HTMLInputElement element) {
        Element fileElem = handleHTMLInputElement(element);
        fileElem.setAttribute("name", element.getName());
        String filename = element.getValue();
        if (filename == null || filename.trim().length() == 0) {
            logger.debug("File input element is empty; element.name=" + element.getName());
            return;
        }
        try {
            File file = new File(filename);
            fileElem.setAttribute("filename", file.getName());
            FileInputStream fis = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, count);
            }
            fis.close();
            byte[] digestBytes = digest.digest();
            fileElem.setAttribute("value", HexStringHelper.toHexString(digestBytes));
            document.getDocumentElement().appendChild(fileElem);
        } catch (FileNotFoundException e) {
            logger.warn("File not found: " + filename, e);
        } catch (IOException e) {
            logger.warn("I/O error: " + filename, e);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("No such algorithm exception: " + e);
        }
    }

    @Override
    public void onHTMLInputPasswordElement(HTMLInputElement element) {
        String name = element.getName();
        String value = element.getValue();
        if (value == null || value.trim().length() == 0) {
            logger.debug("Password input element is empty; element.name=" + name);
            return;
        }
        Element passwordElem = handleHTMLInputElement(element);
        passwordElem.setAttribute("name", name);
        passwordElem.setAttribute("value", value);
        document.getDocumentElement().appendChild(passwordElem);
    }

    @Override
    public void onHTMLInputRadioElement(HTMLInputElement element) {
        Element radioElem = handleHTMLInputElement(element);
        radioElem.setAttribute("name", element.getName());
        radioElem.setAttribute("value", element.getValue());
        boolean checked = element.getChecked();
        if (checked || (!checked && unselectedRadioIncluded)) {
            document.getDocumentElement().appendChild(radioElem);
        }
    }

    @Override
    public void onHTMLInputSubmitElement(HTMLInputElement element) {
        String name = element.getName();
        String value = element.getValue();
        if (value == null || value.trim().length() == 0) {
            logger.debug("Submit input element is empty; element.name=" + name);
            return;
        }
        Element submitElem = handleHTMLInputElement(element);
        submitElem.setAttribute("name", name);
        submitElem.setAttribute("value", value);
        document.getDocumentElement().appendChild(submitElem);
    }

    @Override
    public void onHTMLInputTextElement(HTMLInputElement element) {
        String name = element.getName();
        String value = element.getValue();
        if (value == null || value.trim().length() == 0) {
            logger.debug("Text input element is empty; element.name=" + name);
            return;
        }
        Element textElem = handleHTMLInputElement(element);
        textElem.setAttribute("name", element.getName());
        textElem.setAttribute("value", element.getValue());
        document.getDocumentElement().appendChild(textElem);
    }

    @Override
    public void onHTMLInputHiddenElement(HTMLInputElement element) {
        String name = element.getName();
        String value = element.getValue();
        if (value == null || value.trim().length() == 0) {
            logger.debug("Hidden input element is empty; element.name=" + name);
            return;
        }
        Element hiddenElem = handleHTMLInputElement(element);
        hiddenElem.setAttribute("name", name);
        hiddenElem.setAttribute("value", value);
        document.getDocumentElement().appendChild(hiddenElem);
    }
}
