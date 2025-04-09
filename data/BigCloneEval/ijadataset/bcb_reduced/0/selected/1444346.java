package org.soapfabric;

import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.soapfabric.util.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A lightweight representation of a SOAP v1.1 fault.
 * 
 * @author <a href="mailto:mbsanchez at users.sf.net">Matt Sanchez</a>
 * @version $Id: SOAPFault.java,v 1.4 2004/08/16 16:17:37 mbsanchez Exp $
 */
public class SOAPFault implements Serializable, SOAPConstants, SOAPElement {

    private static final Log LOG = LogFactory.getLog(SOAPFault.class);

    private QName _faultCode;

    private String _faultString;

    private String _faultActor;

    private List _details = new ArrayList();

    public SOAPFault() {
    }

    public SOAPFault(QName faultCode, String faultString, String faultActor) {
        _faultCode = faultCode;
        _faultString = faultString;
        _faultActor = faultActor;
    }

    public SOAPFault(Element faultElement) throws SOAPException {
        if (!StringUtils.equals(faultElement.getNamespaceURI(), NS_URI_SOAP_ENV)) {
            throw new SOAPException("Element namespace does not match " + NS_URI_SOAP_ENV);
        }
        if (!StringUtils.equals(faultElement.getLocalName(), SOAP_FAULT)) {
            throw new SOAPException("Element name does not match " + SOAP_FAULT);
        }
        NodeList list = faultElement.getElementsByTagName(SOAP_FAULTACTOR);
        if (list.getLength() != 0) {
            Element faultActor = (org.w3c.dom.Element) list.item(0);
            _faultActor = XMLUtil.getText(faultActor);
        }
        list = faultElement.getElementsByTagName(SOAP_FAULTSTRING);
        if (list.getLength() != 0) {
            Element faultString = (org.w3c.dom.Element) list.item(0);
            _faultString = XMLUtil.getText(faultString);
        }
        list = faultElement.getElementsByTagName(SOAP_FAULTCODE);
        if (list.getLength() != 0) {
            Element faultCode = (org.w3c.dom.Element) list.item(0);
            String code = XMLUtil.getText(faultCode);
            if (code.indexOf("Server") != -1) {
                _faultCode = FAULT_CODE_SERVER;
            } else {
                _faultCode = FAULT_CODE_CLIENT;
            }
        }
        list = faultElement.getElementsByTagName(SOAP_FAULTDETAIL);
        for (int i = 0; i < list.getLength(); ++i) {
            Element faultDetail = (org.w3c.dom.Element) list.item(i);
            addDetail(faultDetail);
        }
    }

    public void addDetail(Element detailElement) {
        _details.add(detailElement);
    }

    public void addDetail(Throwable error) {
        Document doc = null;
        try {
            doc = XMLUtil.createDocument();
        } catch (ParserConfigurationException e) {
            return;
        }
        Element ex = doc.createElementNS(NS_EXCEPTION, EXCEPTION_ELEMENT);
        ex.setPrefix("e");
        ex.setAttribute("message", error.getMessage());
        ex.setAttribute("className", error.getClass().getName());
        ex.setAttribute("xmlns:e", NS_EXCEPTION);
        StackTraceElement[] trace = error.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement ste = trace[i];
            Element e = doc.createElementNS(NS_EXCEPTION, STACK_TRACE_ELEMENT);
            e.setPrefix("e");
            e.setAttribute("className", ste.getClassName());
            e.setAttribute("fileName", ste.getFileName());
            e.setAttribute("methodName", ste.getMethodName());
            e.setAttribute("lineNumber", String.valueOf(ste.getLineNumber()));
            ex.appendChild(e);
        }
        addDetail(ex);
    }

    public List getDetails() {
        return _details;
    }

    public Element getFirstDetail() {
        if (!_details.isEmpty()) {
            return (Element) _details.get(0);
        }
        return null;
    }

    public String getFirstDetailText() {
        if (!_details.isEmpty()) {
            Element e = (Element) _details.get(0);
            return XMLUtil.getText(e);
        }
        return null;
    }

    public Exception toException() {
        Element detail = getFirstDetail();
        if (detail != null) {
            String namespace = detail.getNamespaceURI();
            String localName = detail.getLocalName();
            if (StringUtils.equals(NS_EXCEPTION, detail.getNamespaceURI()) && StringUtils.equals(EXCEPTION_ELEMENT, detail.getLocalName())) {
                String className = detail.getAttribute("className");
                if (className == null) {
                    className = "java.lang.Exception";
                }
                String message = detail.getAttribute("message");
                Exception ex = null;
                try {
                    if (message == null) {
                        ex = (Exception) Class.forName(className).newInstance();
                    } else {
                        Class exClass = Class.forName(className);
                        try {
                            Constructor ctor = exClass.getConstructor(new Class[] { String.class });
                            ex = (Exception) ctor.newInstance(new Object[] { message });
                        } catch (Exception e) {
                            ex = (Exception) exClass.newInstance();
                        }
                    }
                } catch (Throwable e) {
                    ex = new Exception(message);
                }
                NodeList children = detail.getChildNodes();
                ArrayList stElements = new ArrayList();
                for (int i = 0; i < children.getLength(); ++i) {
                    Node n = children.item(i);
                    if (n instanceof Element) {
                        Element ste = (Element) n;
                        if (StringUtils.equals(NS_EXCEPTION, ste.getNamespaceURI()) && StringUtils.equals(STACK_TRACE_ELEMENT, ste.getLocalName())) {
                            try {
                                StackTraceElement st = newStackTraceElement(ste);
                                stElements.add(st);
                            } catch (Exception e) {
                                LOG.error(e);
                            }
                        }
                    }
                }
                ex.setStackTrace((StackTraceElement[]) stElements.toArray(new StackTraceElement[0]));
                return ex;
            }
        }
        return null;
    }

    public String getFaultActor() {
        return _faultActor;
    }

    public QName getFaultCode() {
        return _faultCode;
    }

    public String getFaultString() {
        return _faultString;
    }

    public void setDetails(List details) {
        _details = details;
    }

    public void setFaultActor(String faultActor) {
        _faultActor = faultActor;
    }

    public void setFaultCode(QName faultCode) {
        _faultCode = faultCode;
    }

    public void setFaultString(String faultString) {
        _faultString = faultString;
    }

    public void writeTo(OutputStream out) throws SOAPException {
        writeTo(out, false);
    }

    public void writeTo(OutputStream out, boolean prettyPrint) throws SOAPException {
        Element soapEnv = toEnvelope();
        OutputFormat format = new OutputFormat("xml", "UTF-8", prettyPrint);
        format.setOmitComments(true);
        format.setOmitDocumentType(true);
        format.setOmitXMLDeclaration(true);
        try {
            XMLSerializer s = new XMLSerializer(out, format);
            s.serialize(soapEnv);
        } catch (Throwable t) {
            throw new SOAPException(t);
        }
    }

    public Element toFaultElement() throws SOAPException {
        Document root = null;
        try {
            root = XMLUtil.createDocument();
        } catch (ParserConfigurationException e) {
            throw new SOAPException(e);
        }
        Element fault = root.createElementNS(NS_URI_SOAP_ENV, SOAP_FAULT);
        fault.setPrefix(NS_PREFIX_SOAP_ENV);
        Element faultCode = root.createElement(SOAP_FAULTCODE);
        fault.appendChild(faultCode);
        if (_faultCode != null) {
            Text text = root.createTextNode(_faultCode.getPrefix() + ":" + _faultCode.getLocalPart());
            faultCode.appendChild(text);
        }
        Element faultString = root.createElement(SOAP_FAULTSTRING);
        fault.appendChild(faultString);
        if (_faultString != null) {
            Text text = root.createTextNode(_faultString.toString());
            faultString.appendChild(text);
        }
        Element faultActor = root.createElement(SOAP_FAULTACTOR);
        fault.appendChild(faultActor);
        if (_faultActor != null) {
            Text text = root.createTextNode(_faultActor.toString());
            faultActor.appendChild(text);
        }
        if (!_details.isEmpty()) {
            Element detail = root.createElement(SOAP_FAULTDETAIL);
            fault.appendChild(detail);
            for (Iterator iter = _details.iterator(); iter.hasNext(); ) {
                Element detailElement = (Element) iter.next();
                detail.appendChild(root.importNode(detailElement, true));
            }
        }
        return fault;
    }

    public Element toEnvelope() throws SOAPException {
        Document root = null;
        try {
            root = XMLUtil.createDocument();
        } catch (ParserConfigurationException e) {
            throw new SOAPException(e);
        }
        Element soapEnv = root.createElementNS(NS_URI_SOAP_ENV, SOAP_ENVELOPE);
        soapEnv.setPrefix(NS_PREFIX_SOAP_ENV);
        soapEnv.setAttribute("xmlns:" + NS_PREFIX_SOAP_ENV, NS_URI_SOAP_ENV);
        root.appendChild(soapEnv);
        Element soapBody = root.createElementNS(NS_URI_SOAP_ENV, SOAP_BODY);
        soapBody.setPrefix(NS_PREFIX_SOAP_ENV);
        soapEnv.appendChild(soapBody);
        Element faultElement = toFaultElement();
        soapBody.appendChild(root.importNode(faultElement, true));
        return soapEnv;
    }

    private StackTraceElement newStackTraceElement(Element ste) throws Exception {
        Class c = StackTraceElement.class;
        Constructor ctor = c.getDeclaredConstructor(new Class[0]);
        ctor.setAccessible(true);
        StackTraceElement stack = (StackTraceElement) ctor.newInstance(new Object[0]);
        Field f = c.getDeclaredField("declaringClass");
        f.setAccessible(true);
        f.set(stack, ste.getAttribute("className"));
        f = c.getDeclaredField("methodName");
        f.setAccessible(true);
        f.set(stack, ste.getAttribute("methodName"));
        f = c.getDeclaredField("fileName");
        f.setAccessible(true);
        f.set(stack, ste.getAttribute("fileName"));
        f = c.getDeclaredField("lineNumber");
        f.setAccessible(true);
        f.set(stack, ste.getAttribute("lineNumber"));
        return stack;
    }
}
