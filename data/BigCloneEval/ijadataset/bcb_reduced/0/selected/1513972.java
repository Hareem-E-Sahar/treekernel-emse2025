package org.soapfabric.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soapfabric.HTTPConstants;
import org.soapfabric.Marshaller;
import org.soapfabric.SOAPException;
import org.soapfabric.SOAPFault;
import org.soapfabric.SOAPFaultException;
import org.soapfabric.SOAPMessage;
import org.soapfabric.SOAPTransport;
import org.soapfabric.TransportException;
import org.soapfabric.Unmarshaller;
import org.soapfabric.client.locator.DefinitionLocator;
import org.soapfabric.client.locator.EndpointLocator;
import org.soapfabric.client.locator.LocatorException;
import org.soapfabric.server.config.Client;
import org.soapfabric.server.config.ClientDigester;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Factory used by Web service clients to create dynamic invocation stubs.  Generated stubs 
 * should  be considered thread-safe.
 * 
 * @author <a href="mailto:mbsanchez at users.sf.net">Matt Sanchez</a>
 * @version $Id: ServiceProxy.java,v 1.6 2004/08/16 16:17:38 mbsanchez Exp $
 */
public class ServiceProxy {

    private static final Log LOG = LogFactory.getLog(ServiceProxy.class);

    private static Map _clientConfig = new Hashtable();

    /**
	 * Creates a new service proxy that implements the given interface and locates its endpoint
	 * using the given {@link EndpointLocator}.
	 * 
	 * @param iface the returned proxy will implement this interface
	 * @param locator used by the proxy to locate the service endpoint
	 * @return a {@link Stub} that can be used to invoke a service
	 */
    public static Stub newProxyInstance(final Class iface, final EndpointLocator locator) {
        return newProxyInstance(iface, locator, null, null);
    }

    /**
	 * Creates a new service proxy that implements the given interface and locates its endpoint
	 * using the given {@link EndpointLocator}.  
	 * 
	 * The optional {@link DefinitionLocator} will be used to locate the service WSDL.  This is
	 *  useful for services that require strict binding to there WSDL definition.
	 * 
	 * @param iface the returned proxy will implement this interface
	 * @param epLocator used by the proxy to locate the service endpoint
	 * @param wsdlLocator used by the proxy to locate a WSDL definition for the service
	 * @param portType the name of the WSDL portType to bind to, if null uses the first portType in the WSDL definitions document
	 * @return a {@link Stub} that can be used to invoke a service
	 */
    public static Stub newProxyInstance(final Class iface, final EndpointLocator epLocator, final DefinitionLocator wsdlLocator, final QName portType) {
        ClassLoader loader = iface.getClassLoader();
        configureClient(loader);
        Enhancer e = new Enhancer();
        e.setClassLoader(loader);
        e.setCallback(new ProxyHandler(epLocator, wsdlLocator, portType, loader));
        e.setInterfaces(new Class[] { iface, Stub.class });
        return (Stub) e.create();
    }

    private static class ProxyHandler extends AbstractStub implements MethodInterceptor {

        private EndpointLocator _epLocator;

        private DefinitionLocator _wsdlLocator;

        private ClassLoader _loader;

        private Definition _wsdl;

        private QName _portType;

        private Map _soapActionCache = new HashMap();

        ProxyHandler(final EndpointLocator epLocator, final DefinitionLocator wsdlLocator, final QName portType, final ClassLoader loader) {
            _epLocator = epLocator;
            _wsdlLocator = wsdlLocator;
            _portType = portType;
            _loader = loader;
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws ProxyException, SOAPFaultException {
            if (method.getDeclaringClass().equals(Object.class) || method.getDeclaringClass().equals(Stub.class)) {
                try {
                    return method.invoke(this, args);
                } catch (IllegalArgumentException e) {
                    throw new ProxyException("Illegal argument for target method!", e);
                } catch (IllegalAccessException e) {
                    throw new ProxyException("Illegal access to target method!", e);
                } catch (InvocationTargetException e) {
                    Throwable target = e.getTargetException();
                    if (target == null) {
                        target = e;
                    }
                    throw new ProxyException("Error invoking target method!", target);
                }
            }
            if (args == null || args.length == 0) {
                throw new ProxyException("Method has no arguments!");
            }
            SOAPMessage request = new SOAPMessage();
            boolean isMultipart = false;
            for (int i = 0; i < args.length; ++i) {
                Object obj = args[i];
                if (obj instanceof DataHandler) {
                    isMultipart = true;
                    try {
                        request.addAttachment((DataHandler) obj);
                    } catch (IOException e) {
                        throw new ProxyException("Error adding attachment to request!", e);
                    }
                }
            }
            if (isMultipart) {
                request.setMimeHeader(HTTPConstants.CONTENT_TYPE, HTTPConstants.MULTIPART_RELATED);
            } else {
                request.setMimeHeader(HTTPConstants.CONTENT_TYPE, HTTPConstants.TEXT_XML);
            }
            Object requestVal = args[0];
            if (!(requestVal instanceof DataHandler)) {
                Collection namespaces = new ArrayList();
                if (_clientConfig.containsKey(_loader)) {
                    Client client = (Client) _clientConfig.get(_loader);
                    namespaces = client.getNamespaces();
                }
                Marshaller m = getMarshaller();
                if (m == null) {
                    m = Marshaller.Factory.newInstance();
                }
                Node node = m.marshall(requestVal, namespaces);
                if (node instanceof Document) {
                    request.setBodyElement(((Document) node).getDocumentElement());
                } else if (node instanceof Element) {
                    request.setBodyElement((Element) node);
                } else {
                    throw new ProxyException("Parsed request is not of type Document or Element!");
                }
            }
            addSOAPAction(request);
            for (Iterator iter = getRequestInterceptors(); iter.hasNext(); ) {
                Interceptor ic = (Interceptor) iter.next();
                ic.intercept(request, requestVal);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(">>> SOAP request");
                LOG.debug(request);
            }
            SOAPTransport transport = getTransport();
            transport.setTimeout(getTimeout());
            try {
                ContainerUtil.start(_epLocator);
                transport.send(request, _epLocator);
                ContainerUtil.stop(_epLocator);
            } catch (TransportException e) {
                throw new ProxyException("Caught transport exception during send!", e);
            } catch (Exception e) {
                throw new ProxyException("Caught locator exception during send!", e);
            }
            SOAPMessage response = null;
            try {
                response = transport.receive();
            } catch (TransportException e) {
                throw new ProxyException("Caught transport exception during receive!", e);
            }
            if (response.isFault()) {
                SOAPFault fault = null;
                try {
                    fault = new SOAPFault(response.getBodyElement());
                } catch (SOAPException e) {
                    throw new ProxyException("Caught client-side SOAP exception while constructing SOAPFault!", e);
                }
                for (Iterator iter = getResponseInterceptors(); iter.hasNext(); ) {
                    Interceptor ic = (Interceptor) iter.next();
                    ic.intercept(fault);
                }
                Exception ex = fault.toException();
                if (ex == null) {
                    ex = createException(method, fault);
                    if ((ex != null) && (ex instanceof SOAPFaultException)) {
                        throw (SOAPFaultException) ex;
                    }
                } else if (ex instanceof SOAPFaultException) {
                    throw (SOAPFaultException) ex;
                } else {
                    LOG.error(ex);
                }
                throw new SOAPFaultException(fault);
            }
            Object returnVal = null;
            if (method.getReturnType().equals(Void.TYPE)) {
                returnVal = null;
            } else if (method.getReturnType().equals(DataHandler.class)) {
                if (response.getAttachmentCount() > 0) {
                    try {
                        returnVal = response.getAttachmentAt(0);
                    } catch (IOException e) {
                        throw new ProxyException("Error getting attachment from response!", e);
                    }
                }
            } else {
                Unmarshaller um = getUnmarshaller();
                if (um == null) {
                    um = Unmarshaller.Factory.newInstance();
                }
                if (response.getBodyElement() != null) {
                    returnVal = um.unmarshall(response.getBodyElement(), method.getReturnType());
                }
            }
            for (Iterator iter = getResponseInterceptors(); iter.hasNext(); ) {
                Interceptor ic = (Interceptor) iter.next();
                ic.intercept(response, returnVal);
            }
            return returnVal;
        }

        private SOAPFaultException createException(Method method, SOAPFault fault) {
            Class[] exTypes = method.getExceptionTypes();
            for (int i = 0; i < exTypes.length; i++) {
                Class c = exTypes[i];
                if (SOAPFaultException.class.isAssignableFrom(c)) {
                    Constructor[] ctors = c.getConstructors();
                    for (int j = 0; j < ctors.length; j++) {
                        Constructor ctor = ctors[j];
                        Class[] ctorParams = ctor.getParameterTypes();
                        if (ctorParams.length == 1) {
                            Class param = ctorParams[0];
                            if (SOAPFault.class.isAssignableFrom(param)) {
                                try {
                                    SOAPFaultException ex = (SOAPFaultException) ctor.newInstance(new Object[] { fault });
                                    return ex;
                                } catch (Throwable e) {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private synchronized void loadWsdl() throws ProxyException {
            if (_wsdlLocator != null && _wsdl == null) {
                try {
                    _wsdl = _wsdlLocator.locate();
                } catch (LocatorException e) {
                    throw new ProxyException("Error locating WSDL defintion!", e);
                }
            }
        }

        private void addSOAPAction(SOAPMessage request) throws ProxyException {
            loadWsdl();
            if (_wsdl != null) {
                Element bodyElement = request.getBodyElement();
                QName messageName = new QName(bodyElement.getNamespaceURI(), bodyElement.getLocalName());
                if (_soapActionCache.containsKey(messageName)) {
                    request.setSoapAction((String) _soapActionCache.get(messageName));
                    return;
                }
                PortType pt = null;
                Binding binding = null;
                if (_portType == null) {
                    Collection portTypes = _wsdl.getPortTypes().values();
                    if (portTypes.isEmpty()) {
                        throw new ProxyException("Error binding to WSDL: not portTypes defined!");
                    }
                    pt = (PortType) portTypes.iterator().next();
                } else {
                    pt = _wsdl.getPortType(_portType);
                    if (pt == null) {
                        throw new ProxyException("Error binding to WSDL: portType " + _portType + " not found!");
                    }
                }
                Collection bindings = _wsdl.getBindings().values();
                for (Iterator iter = bindings.iterator(); iter.hasNext(); ) {
                    Binding b = (Binding) iter.next();
                    if (b.getPortType().equals(pt)) {
                        binding = b;
                        break;
                    }
                }
                if (binding == null) {
                    throw new ProxyException("Error binding to WSDL:  binding for portType " + pt + " not found!");
                }
                BindingOperation op = null;
                List ops = binding.getBindingOperations();
                for (Iterator iter = ops.iterator(); iter.hasNext(); ) {
                    BindingOperation bo = (BindingOperation) iter.next();
                    Operation o = bo.getOperation();
                    Input in = o.getInput();
                    if (in == null) {
                        continue;
                    }
                    Message m = in.getMessage();
                    if (m == null) {
                        continue;
                    }
                    Collection parts = m.getParts().values();
                    for (Iterator iterator = parts.iterator(); iterator.hasNext(); ) {
                        Part p = (Part) iterator.next();
                        QName qname = p.getElementName();
                        if (qname == null) {
                            continue;
                        }
                        if (qname.equals(messageName)) {
                            op = bo;
                            break;
                        }
                    }
                    if (op != null) {
                        break;
                    }
                }
                if (op == null) {
                    throw new ProxyException("Error binding to WSDL: operation not found for input of type " + messageName + "!");
                }
                List ee = op.getExtensibilityElements();
                for (Iterator iter = ee.iterator(); iter.hasNext(); ) {
                    Object element = (Object) iter.next();
                    if (element instanceof SOAPOperation) {
                        SOAPOperation soapOp = (SOAPOperation) element;
                        if (soapOp.getSoapActionURI() != null) {
                            String action = soapOp.getSoapActionURI();
                            request.setSoapAction(action);
                            _soapActionCache.put(messageName, action);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static void configureClient(ClassLoader loader) {
        ClientDigester digester = new ClientDigester();
        if (!_clientConfig.containsKey(loader)) {
            try {
                URL u = loader.getResource("/wsclient.xml");
                if (u == null) {
                    u = loader.getResource("./wsclient.xml");
                }
                if (u == null) {
                    u = loader.getResource("wsclient.xml");
                }
                if (u != null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Found wsclient.xml for loader " + loader + " at " + u.toExternalForm());
                    }
                    Client client = (Client) digester.parse(u.openStream());
                    if (client != null) {
                        _clientConfig.put(loader, client);
                    }
                }
            } catch (Throwable e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Error loading wsclient.xml for loader " + loader, e);
                }
            }
        }
    }
}
