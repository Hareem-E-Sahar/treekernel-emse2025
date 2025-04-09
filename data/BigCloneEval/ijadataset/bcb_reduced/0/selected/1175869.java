package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.description.AttachmentType;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescriptionFactory;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.axis2.jaxws.utility.SAAJFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.activation.DataHandler;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

/** Static Utilty Classes used by the MethodMarshaller implementations in the alt package. */
public class MethodMarshallerUtils {

    private static Log log = LogFactory.getLog(MethodMarshallerUtils.class);

    private static JAXBBlockFactory factory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);

    /** Intentionally Private.  This is a static utility class */
    private MethodMarshallerUtils() {
    }

    /**
     * Returns the list of PDElements that need to be marshalled onto the wire
     *
     * @param marshalDesc
     * @param params          ParameterDescription for this operation
     * @param sigArguments    arguments
     * @param isInput         indicates if input or output  params(input args on client, 
     *                        output args on server)
     * @param isDocLitWrapped
     * @param isRPC
     * @return PDElements
     */
    static List<PDElement> getPDElements(MarshalServiceRuntimeDescription marshalDesc, ParameterDescription[] params, Object[] sigArguments, boolean isInput, boolean isDocLitWrapped, boolean isRPC) {
        List<PDElement> pdeList = new ArrayList<PDElement>();
        int index = 0;
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];
            if (pd.getMode() == Mode.IN && isInput || pd.getMode() == Mode.INOUT || pd.getMode() == Mode.OUT && !isInput) {
                Object value = sigArguments[i];
                if (isAsyncHandler(value)) {
                    continue;
                }
                if (isHolder(value)) {
                    value = ((Holder) value).value;
                }
                Class formalType = pd.getParameterActualType();
                QName qName = null;
                if (pd.isHeader()) {
                    qName = new QName(pd.getTargetNamespace(), pd.getParameterName());
                } else if (isDocLitWrapped) {
                    qName = new QName(pd.getTargetNamespace(), pd.getPartName());
                } else if (isRPC) {
                    qName = new QName(pd.getPartName());
                } else {
                    qName = new QName(pd.getTargetNamespace(), pd.getParameterName());
                }
                Element element = null;
                AttachmentDescription attachmentDesc = pd.getAttachmentDescription();
                if (attachmentDesc != null) {
                    PDElement pde = createPDElementForAttachment(pd, qName, value, formalType);
                    pdeList.add(pde);
                } else {
                    if (!marshalDesc.getAnnotationDesc(formalType).hasXmlRootElement()) {
                        if (pd.isListType()) {
                            List<Object> list = new ArrayList<Object>();
                            if (formalType.isArray()) {
                                for (int count = 0; count < Array.getLength(value); count++) {
                                    Object obj = Array.get(value, count);
                                    list.add(obj);
                                }
                            }
                            element = new Element(list, qName, List.class);
                        } else {
                            element = new Element(value, qName, formalType);
                        }
                    } else {
                        element = new Element(value, qName);
                    }
                    PDElement pde = new PDElement(pd, element, null);
                    pdeList.add(pde);
                }
            }
        }
        return pdeList;
    }

    /**
     * @param pd
     * @param qName
     * @param value
     * @param formalType
     * @return
     */
    private static PDElement createPDElementForAttachment(ParameterDescription pd, QName qName, Object value, Class formalType) {
        PDElement pde;
        if (log.isDebugEnabled()) {
            log.debug("Creating a PDElement for an attachment value: " + ((value == null) ? "null" : value.getClass().getName()));
            log.debug("ParameterDescription = " + pd.toString());
        }
        AttachmentDescription attachmentDesc = pd.getAttachmentDescription();
        AttachmentType attachmentType = attachmentDesc.getAttachmentType();
        if (attachmentType == AttachmentType.SWA) {
            Attachment attachment = new Attachment(value, formalType, attachmentDesc, pd.getPartName());
            pde = new PDElement(pd, null, null, attachment);
        } else {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pdElementErr"));
        }
        return pde;
    }

    /**
     * Return the list of PDElements that is unmarshalled from the wire
     * 
     * @param params ParameterDescription for this operation
     * @param message Message
     * @param packages set of packages needed to unmarshal objects for this operation
     * @param isInput indicates if input or output  params (input on server, output on client)
     * @param hasReturnInBody if isInput=false, then this parameter indicates whether a 
     * return value is expected in the body.
     * @param unmarshalByJavaType in most scenarios this is null.  
     * Only use this in the scenarios that require unmarshalling by java type
     * @return ParamValues
     */
    static List<PDElement> getPDElements(ParameterDescription[] params, Message message, TreeSet<String> packages, boolean isInput, boolean hasReturnInBody, Class[] unmarshalByJavaType) throws XMLStreamException {
        List<PDElement> pdeList = new ArrayList<PDElement>();
        int totalBodyBlocks = 0;
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];
            if (pd.getMode() == Mode.IN && isInput || pd.getMode() == Mode.INOUT || pd.getMode() == Mode.OUT && !isInput) {
                if (!pd.isHeader() && !isSWAAttachment(pd)) {
                    totalBodyBlocks++;
                }
            }
        }
        if (!isInput && hasReturnInBody) {
            totalBodyBlocks++;
        }
        int index = (!isInput && hasReturnInBody) ? 1 : 0;
        int swaIndex = 0;
        for (int i = 0; i < params.length; i++) {
            ParameterDescription pd = params[i];
            if (pd.getMode() == Mode.IN && isInput || pd.getMode() == Mode.INOUT || pd.getMode() == Mode.OUT && !isInput) {
                Block block = null;
                JAXBBlockContext context = new JAXBBlockContext(packages);
                AttachmentDescription attachmentDesc = pd.getAttachmentDescription();
                if (attachmentDesc == null) {
                    if (unmarshalByJavaType != null && unmarshalByJavaType[i] != null) {
                        context.setProcessType(unmarshalByJavaType[i]);
                        context.setIsxmlList(pd.isListType());
                    }
                    if (pd.isHeader()) {
                        String localName = pd.getParameterName();
                        block = message.getHeaderBlock(pd.getTargetNamespace(), localName, context, factory);
                    } else {
                        if (totalBodyBlocks > 1) {
                            block = message.getBodyBlock(index, context, factory);
                        } else {
                            block = message.getBodyBlock(context, factory);
                        }
                        index++;
                    }
                    Element element = new Element(block.getBusinessObject(true), block.getQName());
                    PDElement pde = new PDElement(pd, element, unmarshalByJavaType == null ? null : unmarshalByJavaType[i]);
                    pdeList.add(pde);
                } else {
                    if (attachmentDesc.getAttachmentType() == AttachmentType.SWA) {
                        String partName = pd.getPartName();
                        String cid = null;
                        if (log.isDebugEnabled()) {
                            log.debug("Getting the attachment dataHandler for partName=" + partName);
                        }
                        if (partName != null && partName.length() > 0) {
                            cid = message.getAttachmentID(partName);
                        }
                        if (cid == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Attachment dataHandler was not found.  Fallback to use attachment " + swaIndex);
                            }
                            cid = message.getAttachmentID(swaIndex);
                        }
                        DataHandler dh = message.getDataHandler(cid);
                        Attachment attachment = new Attachment(dh, cid);
                        PDElement pde = new PDElement(pd, null, null, attachment);
                        pdeList.add(pde);
                        swaIndex++;
                    } else {
                        throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pdElementErr"));
                    }
                }
            }
        }
        return pdeList;
    }

    /**
     * Creates the request signature arguments (server) from a list
     * of element eabled object (PDEements)
     * @param pds ParameterDescriptions for this Operation
     * @param pvList Element enabled object
     * @return Signature Args
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static Object[] createRequestSignatureArgs(ParameterDescription[] pds, List<PDElement> pdeList) throws InstantiationException, IOException, IllegalAccessException, ClassNotFoundException {
        Object[] args = new Object[pds.length];
        int pdeIndex = 0;
        for (int i = 0; i < args.length; i++) {
            PDElement pde = (pdeIndex < pdeList.size()) ? pdeList.get(pdeIndex) : null;
            ParameterDescription pd = pds[i];
            if (pde == null || pde.getParam() != pd) {
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), null);
                } else {
                    args[i] = null;
                }
            } else {
                Object value = null;
                if (pde.getAttachment() != null) {
                    value = pde.getAttachment().getDataHandler();
                } else {
                    value = pde.getElement().getTypeValue();
                }
                pdeIndex++;
                if (ConvertUtils.isConvertable(value, pd.getParameterActualType())) {
                    value = ConvertUtils.convert(value, pd.getParameterActualType());
                } else {
                    String objectClass = (value == null) ? "null" : value.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("convertProblem", objectClass, pd.getParameterActualType().getName()));
                }
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), value);
                } else {
                    args[i] = value;
                }
            }
        }
        return args;
    }

    /**
     * Update the signature arguments on the client with the unmarshalled element enabled objects
     * (pvList)
     *
     * @param pds           ParameterDescriptions
     * @param pdeList       Element Enabled objects
     * @param signatureArgs Signature Arguments (the out/inout holders are updated)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static void updateResponseSignatureArgs(ParameterDescription[] pds, List<PDElement> pdeList, Object[] signatureArgs) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        int pdeIndex = 0;
        for (int i = 0; i < pds.length; i++) {
            PDElement pde = (pdeIndex < pdeList.size()) ? pdeList.get(pdeIndex) : null;
            ParameterDescription pd = pds[i];
            if (pde != null && pde.getParam() == pd) {
                Object value = null;
                if (pde.getAttachment() == null) {
                    value = pde.getElement().getTypeValue();
                } else {
                    value = pde.getAttachment().getDataHandler();
                }
                pdeIndex++;
                if (ConvertUtils.isConvertable(value, pd.getParameterActualType())) {
                    value = ConvertUtils.convert(value, pd.getParameterActualType());
                } else {
                    String objectClass = (value == null) ? "null" : value.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("convertProblem", objectClass, pd.getParameterActualType().getName()));
                }
                if (isHolder(signatureArgs[i])) {
                    ((Holder) signatureArgs[i]).value = value;
                }
            }
        }
    }

    /**
     * Marshal the element enabled objects (pvList) to the Message
     *
     * @param pdeList  element enabled objects
     * @param message  Message
     * @param packages Packages needed to do a JAXB Marshal
     * @throws MessageException
     */
    static void toMessage(List<PDElement> pdeList, Message message, TreeSet<String> packages) throws WebServiceException {
        int totalBodyBlocks = 0;
        for (int i = 0; i < pdeList.size(); i++) {
            PDElement pde = pdeList.get(i);
            if (!pde.getParam().isHeader() && pde.getElement() != null) {
                totalBodyBlocks++;
            }
        }
        int index = message.getNumBodyBlocks();
        for (int i = 0; i < pdeList.size(); i++) {
            PDElement pde = pdeList.get(i);
            JAXBBlockContext context = new JAXBBlockContext(packages);
            Attachment attachment = pde.getAttachment();
            if (attachment == null) {
                if (pde.getByJavaTypeClass() != null) {
                    context.setProcessType(pde.getByJavaTypeClass());
                    if (pde.getParam() != null) {
                        context.setIsxmlList(pde.getParam().isListType());
                    }
                }
                Block block = factory.createFrom(pde.getElement().getElementValue(), context, pde.getElement().getQName());
                if (pde.getParam().isHeader()) {
                    QName qname = block.getQName();
                    message.setHeaderBlock(qname.getNamespaceURI(), qname.getLocalPart(), block);
                } else {
                    if (totalBodyBlocks < 1) {
                        message.setBodyBlock(block);
                    } else {
                        message.setBodyBlock(index, block);
                    }
                    index++;
                }
            } else {
                AttachmentType type = pde.getParam().getAttachmentDescription().getAttachmentType();
                if (type == AttachmentType.SWA) {
                    message.addDataHandler(attachment.getDataHandler(), attachment.getContentID());
                    message.setDoingSWA(true);
                } else {
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pdElementErr"));
                }
            }
        }
    }

    /**
     * Marshals the return object to the message (used on server to marshal return object)
     *
     * @param returnElement              element
     * @param returnType
     * @param marshalDesc
     * @param message
     * @param marshalByJavaTypeClass..we must do this for RPC...discouraged otherwise
     * @param isHeader
     * @throws MessageException
     */
    static void toMessage(Element returnElement, Class returnType, boolean isList, MarshalServiceRuntimeDescription marshalDesc, Message message, Class marshalByJavaTypeClass, boolean isHeader) throws WebServiceException {
        JAXBBlockContext context = new JAXBBlockContext(marshalDesc.getPackages());
        if (marshalByJavaTypeClass != null) {
            context.setProcessType(marshalByJavaTypeClass);
            context.setIsxmlList(isList);
        }
        Block block = factory.createFrom(returnElement.getElementValue(), context, returnElement.getQName());
        if (isHeader) {
            message.setHeaderBlock(returnElement.getQName().getNamespaceURI(), returnElement.getQName().getLocalPart(), block);
        } else {
            message.setBodyBlock(block);
        }
    }

    /**
     * Unmarshal the return object from the message
     *
     * @param packages
     * @param message
     * @param unmarshalByJavaTypeClass Used only to indicate unmarshaling by type...only necessary
     *                                 in some scenarios
     * @param isHeader
     * @param headerNS                 (only needed if isHeader)
     * @param headerLocalPart          (only needed if isHeader)
     * @param hasOutputBodyParams (true if the method has out or inout params other 
     * than the return value)
     * @return Element
     * @throws WebService
     * @throws XMLStreamException
     */
    static Element getReturnElement(TreeSet<String> packages, Message message, Class unmarshalByJavaTypeClass, boolean isList, boolean isHeader, String headerNS, String headerLocalPart, boolean hasOutputBodyParams) throws WebServiceException, XMLStreamException {
        JAXBBlockContext context = new JAXBBlockContext(packages);
        if (unmarshalByJavaTypeClass != null && !isHeader) {
            context.setProcessType(unmarshalByJavaTypeClass);
            context.setIsxmlList(isList);
        }
        Block block = null;
        boolean isBody = false;
        if (isHeader) {
            block = message.getHeaderBlock(headerNS, headerLocalPart, context, factory);
        } else {
            if (hasOutputBodyParams) {
                block = message.getBodyBlock(0, context, factory);
                isBody = true;
            } else {
                block = message.getBodyBlock(context, factory);
                isBody = true;
            }
        }
        if (isBody && block == null) {
            if (log.isDebugEnabled()) {
                log.debug("Empty Body Block Found in response Message for wsdl Operation defintion that expects an Output");
                log.debug("Return type associated with SEI operation is not void, Body Block cannot be null");
            }
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("MethodMarshallerUtilErr1"));
        }
        Element returnElement = new Element(block.getBusinessObject(true), block.getQName());
        return returnElement;
    }

    /**
     * Marshaling a fault is essentially the same for rpc/lit and doc/lit. This method is used by
     * all of the MethodMarshallers
     *
     * @param throwable     Throwable to marshal
     * @param operationDesc OperationDescription
     * @param packages      Packages needed to marshal the object
     * @param message       Message
     */
    static void marshalFaultResponse(Throwable throwable, MarshalServiceRuntimeDescription marshalDesc, OperationDescription operationDesc, Message message) {
        Throwable t = ClassUtils.getRootCause(throwable);
        if (log.isDebugEnabled()) {
            log.debug("Marshal Throwable =" + throwable.getClass().getName());
            log.debug("  rootCause =" + t.getClass().getName());
            log.debug("  exception=" + t.toString());
            log.debug("  stack=" + stackToString(t));
        }
        XMLFault xmlfault = null;
        try {
            FaultDescription fd = operationDesc.resolveFaultByExceptionName(t.getClass().getCanonicalName());
            if (fd != null) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a Service Exception");
                }
                JAXBBlockContext context = new JAXBBlockContext(marshalDesc.getPackages());
                Object faultBeanObject = null;
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                String faultInfo = fd.getFaultInfo();
                if (faultInfo == null || faultInfo.length() == 0) {
                    faultBeanObject = LegacyExceptionUtil.createFaultBean(t, fd, marshalDesc);
                } else {
                    Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
                    faultBeanObject = getFaultInfo.invoke(t, null);
                }
                if (log.isErrorEnabled()) {
                    log.debug("The faultBean type is" + faultBeanObject.getClass().getName());
                }
                if (faultBeanObject == t || (context.getConstructionType() != JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH && isNotJAXBRootElement(faultBeanObject.getClass(), marshalDesc))) {
                    context.setProcessType(faultBeanObject.getClass());
                }
                QName faultBeanQName = new QName(faultBeanDesc.getFaultBeanNamespace(), faultBeanDesc.getFaultBeanLocalName());
                if (!marshalDesc.getAnnotationDesc(faultBeanObject.getClass()).hasXmlRootElement()) {
                    faultBeanObject = new JAXBElement(faultBeanQName, faultBeanObject.getClass(), faultBeanObject);
                }
                Block[] detailBlocks = new Block[1];
                detailBlocks[0] = factory.createFrom(faultBeanObject, context, faultBeanQName);
                if (log.isDebugEnabled()) {
                    log.debug("Create the xmlFault for the Service Exception");
                }
                String text = t.getMessage();
                if (text == null || text.length() == 0) {
                    text = t.toString();
                }
                xmlfault = new XMLFault(null, new XMLFaultReason(text), detailBlocks);
            } else {
                xmlfault = createXMLFaultFromSystemException(t);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug("An exception (" + e + ") occurred while marshalling exception (" + t + ")");
            }
            WebServiceException wse = ExceptionFactory.makeWebServiceException(e);
            xmlfault = createXMLFaultFromSystemException(wse);
        }
        message.setXMLFault(xmlfault);
    }

    /**
     * This method is used by WebService Impl and Provider to create an XMLFault (for marshalling)
     * from an exception that is a non-service exception
     *
     * @param t Throwable that represents a Service Exception
     * @return XMLFault
     */
    public static XMLFault createXMLFaultFromSystemException(Throwable t) {
        try {
            XMLFault xmlfault = null;
            if (t instanceof SOAPFaultException) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal SOAPFaultException");
                }
                SOAPFaultException sfe = (SOAPFaultException) t;
                SOAPFault soapFault = sfe.getFault();
                if (soapFault == null) {
                    xmlfault = new XMLFault(null, new XMLFaultReason(t.toString()));
                } else {
                    xmlfault = XMLFaultUtils.createXMLFault(soapFault);
                }
            } else if (t instanceof WebServiceException) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a WebServiceException");
                }
                WebServiceException wse = (WebServiceException) t;
                String text = wse.getMessage();
                if (text == null || text.length() == 0) {
                    text = wse.toString();
                }
                xmlfault = new XMLFault(null, new XMLFaultReason(text));
            } else {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a unchecked System Exception");
                }
                String text = t.getMessage();
                if (text == null || text.length() == 0) {
                    text = t.toString();
                }
                xmlfault = new XMLFault(null, new XMLFaultReason(text));
            }
            return xmlfault;
        } catch (Throwable e) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("An exception (" + e + ") occurred while marshalling exception (" + t + ")");
                }
                String text = e.getMessage();
                if (text == null || text.length() == 0) {
                    text = e.toString();
                }
                WebServiceException wse = ExceptionFactory.makeWebServiceException(e);
                return new XMLFault(null, new XMLFaultReason(text));
            } catch (Exception e2) {
                throw ExceptionFactory.makeWebServiceException(e2);
            }
        }
    }

    /**
     * Unmarshal the service/system exception from a Message. This is used by all of the
     * marshallers
     *
     * @param operationDesc
     * @param marshalDesc
     * @param message
     * @return Throwable
     * @throws WebServiceException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws XMLStreamException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    static Throwable demarshalFaultResponse(OperationDescription operationDesc, MarshalServiceRuntimeDescription marshalDesc, Message message) throws WebServiceException, ClassNotFoundException, IllegalAccessException, InstantiationException, XMLStreamException, InvocationTargetException, NoSuchMethodException {
        Throwable exception = null;
        XMLFault xmlfault = message.getXMLFault();
        Block[] detailBlocks = xmlfault.getDetailBlocks();
        QName elementQName = null;
        if (detailBlocks != null && detailBlocks.length == 1) {
            elementQName = detailBlocks[0].getQName();
        }
        FaultDescription faultDesc = null;
        if (elementQName != null) {
            for (int i = 0; i < operationDesc.getFaultDescriptions().length && faultDesc == null; i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                if (faultBeanDesc != null) {
                    QName tryQName = new QName(faultBeanDesc.getFaultBeanNamespace(), faultBeanDesc.getFaultBeanLocalName());
                    if (log.isErrorEnabled()) {
                        log.debug("  FaultDescription qname is (" + tryQName + ") and detail element qname is (" + elementQName + ")");
                    }
                    if (elementQName.equals(tryQName)) {
                        faultDesc = fd;
                    }
                }
            }
        }
        if (faultDesc == null && elementQName != null) {
            for (int i = 0; i < operationDesc.getFaultDescriptions().length && faultDesc == null; i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                if (faultBeanDesc != null) {
                    String tryName = faultBeanDesc.getFaultBeanLocalName();
                    if (elementQName.getLocalPart().equals(tryName)) {
                        faultDesc = fd;
                    }
                }
            }
        }
        if (faultDesc == null) {
            exception = createSystemException(xmlfault, message);
        } else {
            if (log.isErrorEnabled()) {
                log.debug("Ready to demarshal service exception.  The detail entry name is " + elementQName);
            }
            FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(faultDesc);
            boolean isLegacy = (faultDesc.getFaultInfo() == null || faultDesc.getFaultInfo().length() == 0);
            JAXBBlockContext blockContext = new JAXBBlockContext(marshalDesc.getPackages());
            Class faultBeanFormalClass;
            try {
                faultBeanFormalClass = loadClass(faultBeanDesc.getFaultBeanClassName());
            } catch (ClassNotFoundException e) {
                faultBeanFormalClass = loadClass(faultBeanDesc.getFaultBeanClassName(), operationDesc.getEndpointInterfaceDescription().getEndpointDescription().getAxisService().getClassLoader());
            }
            if (blockContext.getConstructionType() != JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH && isNotJAXBRootElement(faultBeanFormalClass, marshalDesc)) {
                blockContext.setProcessType(faultBeanFormalClass);
            }
            Block jaxbBlock = factory.createFrom(detailBlocks[0], blockContext);
            Object faultBeanObject = jaxbBlock.getBusinessObject(true);
            if (faultBeanObject instanceof JAXBElement) {
                faultBeanObject = ((JAXBElement) faultBeanObject).getValue();
            }
            if (log.isErrorEnabled()) {
                log.debug("Unmarshalled the detail element into a JAXB object");
            }
            Class exceptionClass;
            try {
                exceptionClass = loadClass(faultDesc.getExceptionClassName());
            } catch (ClassNotFoundException e) {
                exceptionClass = loadClass(faultDesc.getExceptionClassName(), operationDesc.getEndpointInterfaceDescription().getEndpointDescription().getAxisService().getClassLoader());
            }
            if (log.isErrorEnabled()) {
                log.debug("Found FaultDescription.  The exception name is " + exceptionClass.getName());
            }
            exception = createServiceException(xmlfault.getReason().getText(), exceptionClass, faultBeanObject, faultBeanFormalClass, marshalDesc, isLegacy);
        }
        return exception;
    }

    /**
     * @param pds
     * @return Number of inout or out parameters
     */
    static int numOutputBodyParams(ParameterDescription[] pds) {
        int count = 0;
        for (int i = 0; i < pds.length; i++) {
            if (!pds[i].isHeader()) {
                if (pds[i].getMode() == Mode.INOUT || pds[i].getMode() == Mode.OUT) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * @param value
     * @return if async handler
     */
    static boolean isAsyncHandler(Object value) {
        return (value instanceof AsyncHandler);
    }

    /**
     * @param value
     * @return true if value is holder
     */
    static boolean isHolder(Object value) {
        return value != null && Holder.class.isAssignableFrom(value.getClass());
    }

    /**
     * Crate a Holder
     *
     * @param <T>
     * @param paramType
     * @param value
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    static <T> Holder<T> createHolder(Class paramType, T value) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (Holder.class.isAssignableFrom(paramType)) {
            Holder holder = (Holder) paramType.newInstance();
            holder.value = value;
            return holder;
        }
        return null;
    }

    /**
     * Load the class
     *
     * @param className
     * @return loaded class
     * @throws ClassNotFoundException
     */
    static Class loadClass(String className) throws ClassNotFoundException {
        Class cls = ClassUtils.getPrimitiveClass(className);
        if (cls == null) {
            cls = forName(className, true, getContextClassLoader());
        }
        return cls;
    }

    /**
     * Load the class
     *
     * @param className
     * @return loaded class
     * @throws ClassNotFoundException
     */
    static Class loadClass(String className, ClassLoader cl) throws ClassNotFoundException {
        Class cls = ClassUtils.getPrimitiveClass(className);
        if (cls == null) {
            cls = forName(className, true, cl);
        }
        return cls;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize, final ClassLoader classLoader) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(new PrivilegedExceptionAction() {

                public Object run() throws ClassNotFoundException {
                    Class cls = ClassUtils.getPrimitiveClass(className);
                    if (cls == null) {
                        cls = Class.forName(className, initialize, classLoader);
                    }
                    return cls;
                }
            });
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException) e.getException();
        }
        return cl;
    }

    /** @return ClassLoader */
    private static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(new PrivilegedExceptionAction() {

                public Object run() throws ClassNotFoundException {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }
        return cl;
    }

    /**
     * Create a JAX-WS Service Exception (Generated Exception)
     *
     * @param message
     * @param exceptionclass
     * @param bean
     * @param beanFormalType
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @parma marshalDesc is used to get cached information about the exception class and bean
     */
    private static Exception createServiceException(String message, Class exceptionclass, Object bean, Class beanFormalType, MarshalServiceRuntimeDescription marshalDesc, boolean isLegacyException) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        if (log.isDebugEnabled()) {
            log.debug("Constructing JAX-WS Exception:" + exceptionclass);
        }
        Exception exception = null;
        if (isLegacyException) {
            exception = LegacyExceptionUtil.createFaultException(exceptionclass, bean, marshalDesc);
        } else {
            Constructor constructor = exceptionclass.getConstructor(new Class[] { String.class, beanFormalType });
            exception = (Exception) constructor.newInstance(new Object[] { message, bean });
        }
        return exception;
    }

    /**
     * Create a system exception
     *
     * @param message
     * @return
     */
    public static ProtocolException createSystemException(XMLFault xmlFault, Message message) {
        ProtocolException e = null;
        Protocol protocol = message.getProtocol();
        String text = xmlFault.getReason().getText();
        if (protocol == Protocol.soap11 || protocol == Protocol.soap12) {
            if (log.isDebugEnabled()) {
                log.debug("Constructing SOAPFaultException for " + text);
            }
            String protocolNS = (protocol == Protocol.soap11) ? SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE : SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
            try {
                javax.xml.soap.MessageFactory mf = SAAJFactory.createMessageFactory(protocolNS);
                SOAPBody body = mf.createMessage().getSOAPBody();
                SOAPFault soapFault = XMLFaultUtils.createSAAJFault(xmlFault, body);
                e = new SOAPFaultException(soapFault);
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred during fault processing:", ex);
                }
                e = ExceptionFactory.makeProtocolException(text, null);
            }
        } else if (protocol == Protocol.rest) {
            if (log.isDebugEnabled()) {
                log.debug("Constructing ProtocolException for " + text);
            }
            e = ExceptionFactory.makeProtocolException(text, null);
        } else if (protocol == Protocol.unknown) {
            if (log.isDebugEnabled()) {
                log.debug("Constructing ProtocolException for " + text);
            }
            e = ExceptionFactory.makeProtocolException(text, null);
        }
        return e;
    }

    /**
     * @param ed
     * @return
     */
    static MarshalServiceRuntimeDescription getMarshalDesc(EndpointDescription ed) {
        ServiceDescription sd = ed.getServiceDescription();
        return MarshalServiceRuntimeDescriptionFactory.get(sd);
    }

    /**
     * This probably should be available from the ParameterDescription
     *
     * @param cls
     * @param marshalDesc
     * @return true if primitive, wrapper, java.lang.String. Calendar (or GregorianCalendar),
     *         BigInteger etc or anything other java type that is mapped by the basic schema types
     */
    static boolean isNotJAXBRootElement(Class cls, MarshalServiceRuntimeDescription marshalDesc) {
        if (cls == String.class || cls.isPrimitive() || cls == Calendar.class || cls == byte[].class || cls == GregorianCalendar.class || cls == Date.class || cls == BigInteger.class || cls == BigDecimal.class) {
            return true;
        }
        AnnotationDesc aDesc = marshalDesc.getAnnotationDesc(cls);
        if (aDesc != null) {
            return (aDesc.getXmlRootElementName() == null);
        }
        return true;
    }

    /**
     * Get a string containing the stack of the specified exception   
     * @param e   
     * @return    
     */
    public static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        return sw.getBuffer().toString();
    }

    static boolean isSWAAttachment(ParameterDescription pd) {
        return pd.getAttachmentDescription() != null && pd.getAttachmentDescription().getAttachmentType() == AttachmentType.SWA;
    }

    /**
     * Register the unmarshalling information so that it can 
     * be used to speed up subsequent marshalling events.
     * @param mc
     * @param packages
     * @param packagesKey
     */
    static void registerUnmarshalInfo(MessageContext mc, TreeSet<String> packages, String packagesKey) throws AxisFault {
        if (mc == null || mc.getAxisMessageContext() == null || mc.getAxisMessageContext().getAxisService() == null || mc.getAxisMessageContext().getAxisOperation() == null) {
            return;
        }
        AxisOperation axisOp = mc.getAxisMessageContext().getAxisOperation();
        Parameter param = axisOp.getParameter(UnmarshalInfo.KEY);
        if (param == null) {
            UnmarshalInfo info = new UnmarshalInfo(packages, packagesKey);
            axisOp.addParameter(UnmarshalInfo.KEY, info);
            param = axisOp.getParameter(UnmarshalInfo.KEY);
            param.setTransient(true);
            UnmarshalMessageContextListener.create(mc.getAxisMessageContext().getServiceContext());
        }
    }
}
