package org.w3c.domts;

import java.lang.reflect.Constructor;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 *   This class uses Xalan-J to add XPath support
 *       to the current JAXP DOM implementation
 */
public class XalanDOMTestDocumentBuilderFactory extends JAXPDOMTestDocumentBuilderFactory {

    /**
   * Creates a JAXP implementation of DOMTestDocumentBuilderFactory.
   * @param factory null for default JAXP provider.  If not null,
   * factory will be mutated in constructor and should be released
   * by calling code upon return.
   * @param settings array of settings, may be null.
   */
    public XalanDOMTestDocumentBuilderFactory(DocumentBuilderFactory baseFactory, DocumentBuilderSetting[] settings) throws DOMTestIncompatibleException {
        super(baseFactory, settings);
    }

    protected DOMTestDocumentBuilderFactory createInstance(DocumentBuilderFactory newFactory, DocumentBuilderSetting[] mergedSettings) throws DOMTestIncompatibleException {
        return new XalanDOMTestDocumentBuilderFactory(newFactory, mergedSettings);
    }

    /**
   *  Creates XPath evaluator
   *  @param doc DOM document, may not be null
   */
    public Object createXPathEvaluator(Document doc) {
        try {
            Class xpathClass = Class.forName("org.apache.xpath.domapi.XPathEvaluatorImpl");
            Constructor constructor = xpathClass.getConstructor(new Class[] { Document.class });
            return constructor.newInstance(new Object[] { doc });
        } catch (Exception ex) {
        }
        return doc;
    }
}
