package org.apache.wml.dom;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import org.apache.wml.WMLDocument;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * @xerces.internal
 * @version $Id: WMLDocumentImpl.java 447257 2006-09-18 05:40:07Z mrglavas $
 * @author <a href="mailto:david@topware.com.tw">David Li</a>
 */
public class WMLDocumentImpl extends DocumentImpl implements WMLDocument {

    private static final long serialVersionUID = -6582904849512384104L;

    private static Hashtable _elementTypesWML;

    private static final Class[] _elemClassSigWML = new Class[] { WMLDocumentImpl.class, String.class };

    public Element createElement(String tagName) throws DOMException {
        Class elemClass;
        Constructor cnst;
        elemClass = (Class) _elementTypesWML.get(tagName);
        if (elemClass != null) {
            try {
                cnst = elemClass.getConstructor(_elemClassSigWML);
                return (Element) cnst.newInstance(new Object[] { this, tagName });
            } catch (Exception except) {
                Throwable thrw;
                if (except instanceof java.lang.reflect.InvocationTargetException) thrw = ((java.lang.reflect.InvocationTargetException) except).getTargetException(); else thrw = except;
                System.out.println("Exception " + thrw.getClass().getName());
                System.out.println(thrw.getMessage());
                throw new IllegalStateException("Tag '" + tagName + "' associated with an Element class that failed to construct.");
            }
        }
        return new WMLElementImpl(this, tagName);
    }

    static {
        _elementTypesWML = new Hashtable();
        _elementTypesWML.put("b", WMLBElementImpl.class);
        _elementTypesWML.put("noop", WMLNoopElementImpl.class);
        _elementTypesWML.put("a", WMLAElementImpl.class);
        _elementTypesWML.put("setvar", WMLSetvarElementImpl.class);
        _elementTypesWML.put("access", WMLAccessElementImpl.class);
        _elementTypesWML.put("strong", WMLStrongElementImpl.class);
        _elementTypesWML.put("postfield", WMLPostfieldElementImpl.class);
        _elementTypesWML.put("do", WMLDoElementImpl.class);
        _elementTypesWML.put("wml", WMLWmlElementImpl.class);
        _elementTypesWML.put("tr", WMLTrElementImpl.class);
        _elementTypesWML.put("go", WMLGoElementImpl.class);
        _elementTypesWML.put("big", WMLBigElementImpl.class);
        _elementTypesWML.put("anchor", WMLAnchorElementImpl.class);
        _elementTypesWML.put("timer", WMLTimerElementImpl.class);
        _elementTypesWML.put("small", WMLSmallElementImpl.class);
        _elementTypesWML.put("optgroup", WMLOptgroupElementImpl.class);
        _elementTypesWML.put("head", WMLHeadElementImpl.class);
        _elementTypesWML.put("td", WMLTdElementImpl.class);
        _elementTypesWML.put("fieldset", WMLFieldsetElementImpl.class);
        _elementTypesWML.put("img", WMLImgElementImpl.class);
        _elementTypesWML.put("refresh", WMLRefreshElementImpl.class);
        _elementTypesWML.put("onevent", WMLOneventElementImpl.class);
        _elementTypesWML.put("input", WMLInputElementImpl.class);
        _elementTypesWML.put("prev", WMLPrevElementImpl.class);
        _elementTypesWML.put("table", WMLTableElementImpl.class);
        _elementTypesWML.put("meta", WMLMetaElementImpl.class);
        _elementTypesWML.put("template", WMLTemplateElementImpl.class);
        _elementTypesWML.put("br", WMLBrElementImpl.class);
        _elementTypesWML.put("option", WMLOptionElementImpl.class);
        _elementTypesWML.put("u", WMLUElementImpl.class);
        _elementTypesWML.put("p", WMLPElementImpl.class);
        _elementTypesWML.put("select", WMLSelectElementImpl.class);
        _elementTypesWML.put("em", WMLEmElementImpl.class);
        _elementTypesWML.put("i", WMLIElementImpl.class);
        _elementTypesWML.put("card", WMLCardElementImpl.class);
    }

    public WMLDocumentImpl(DocumentType doctype) {
        super(doctype, false);
    }
}
