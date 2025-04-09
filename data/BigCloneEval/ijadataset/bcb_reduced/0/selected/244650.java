package com.jawise.serviceadapter.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.jawise.serviceadapter.convert.nvp.NvpToNvpConverter;
import com.jawise.serviceadapter.convert.nvp.NvpToSoapConverter;
import com.jawise.serviceadapter.convert.nvp.NvpToXmlConverter;
import com.jawise.serviceadapter.convert.nvp.NvpToXmlRpcConverter;
import com.jawise.serviceadapter.convert.rpc.XmlRpcToNVPConverter;
import com.jawise.serviceadapter.convert.rpc.XmlRpcToSoapConverter;
import com.jawise.serviceadapter.convert.rpc.XmlRpcToXmlConverter;
import com.jawise.serviceadapter.convert.rpc.XmlRpcToXmlRpcConverter;
import com.jawise.serviceadapter.convert.soap.SoapToNvpConverter;
import com.jawise.serviceadapter.convert.soap.SoapToSoapConverter;
import com.jawise.serviceadapter.convert.soap.SoapToXmlConverter;
import com.jawise.serviceadapter.convert.soap.SoapToXmlRpcConverter;
import com.jawise.serviceadapter.convert.xml.XmlHttpToXmlHttpConverter;
import com.jawise.serviceadapter.convert.xml.XmlToNvpConverter;
import com.jawise.serviceadapter.convert.xml.XmlToSoapConverter;
import com.jawise.serviceadapter.convert.xml.XmlToXmlRpcConverter;
import com.jawise.serviceadapter.core.Binding;
import com.jawise.serviceadapter.core.MessageException;
import com.jawise.serviceadapter.core.Protocol;
import com.jawise.serviceadapter.core.Service;

public class MessageConverterFactory {

    protected static Logger logger = Logger.getLogger(MessageConverterFactory.class);

    private static Map<String, Class<? extends MessageConverter>> inputConverterRegistry = new HashMap<String, Class<? extends MessageConverter>>();

    private static Map<String, Class<? extends MessageConverter>> outputConverterRegistry = new HashMap<String, Class<? extends MessageConverter>>();

    static {
        inputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.NVP_HTTP, NvpToNvpConverter.class);
        outputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.NVP_HTTP, NvpToNvpConverter.class);
        inputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.NVP_HTTP, XmlToNvpConverter.class);
        outputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.NVP_HTTP, NvpToXmlConverter.class);
        inputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.XML_HTTP, NvpToXmlConverter.class);
        outputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.XML_HTTP, XmlToNvpConverter.class);
        inputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.XML_HTTP, XmlHttpToXmlHttpConverter.class);
        outputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.XML_HTTP, XmlHttpToXmlHttpConverter.class);
        inputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.XML_RPC, XmlRpcToXmlRpcConverter.class);
        outputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.XML_RPC, XmlRpcToXmlRpcConverter.class);
        inputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.NVP_HTTP, XmlRpcToNVPConverter.class);
        outputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.NVP_HTTP, NvpToXmlRpcConverter.class);
        inputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.XML_RPC, NvpToXmlRpcConverter.class);
        outputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.XML_RPC, XmlRpcToNVPConverter.class);
        inputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.XML_RPC, XmlToXmlRpcConverter.class);
        outputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.XML_RPC, XmlRpcToXmlConverter.class);
        inputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.XML_HTTP, XmlRpcToXmlConverter.class);
        outputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.XML_HTTP, XmlToXmlRpcConverter.class);
        inputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.SOAP11, SoapToSoapConverter.class);
        outputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.SOAP11, SoapToSoapConverter.class);
        inputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.NVP_HTTP, SoapToNvpConverter.class);
        outputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.NVP_HTTP, NvpToSoapConverter.class);
        inputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.SOAP11, NvpToSoapConverter.class);
        outputConverterRegistry.put(Protocol.NVP_HTTP + "" + Protocol.SOAP11, SoapToNvpConverter.class);
        inputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.XML_HTTP, SoapToXmlConverter.class);
        outputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.XML_HTTP, XmlToSoapConverter.class);
        inputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.SOAP11, XmlToSoapConverter.class);
        outputConverterRegistry.put(Protocol.XML_HTTP + "" + Protocol.SOAP11, SoapToXmlConverter.class);
        inputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.XML_RPC, SoapToXmlRpcConverter.class);
        outputConverterRegistry.put(Protocol.SOAP11 + "" + Protocol.XML_RPC, XmlRpcToSoapConverter.class);
        inputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.SOAP11, XmlRpcToSoapConverter.class);
        outputConverterRegistry.put(Protocol.XML_RPC + "" + Protocol.SOAP11, SoapToXmlRpcConverter.class);
    }

    @SuppressWarnings("unchecked")
    public static MessageConverter getMessageConverter(MessageContext ctx) throws MessageException {
        Service service = (Service) ctx.get("adapterService");
        Boolean input = (Boolean) ctx.get("input");
        Binding fromBinding = service.getPort().getBinding();
        Binding toBinding = service.getAdapter().getService().getPort().getBinding();
        String from = fromBinding.getProtocol();
        String to = toBinding.getProtocol();
        String key = from + "" + to;
        Class<? extends MessageConverter> converterclass = null;
        if (input) {
            converterclass = inputConverterRegistry.get(key);
        } else {
            converterclass = outputConverterRegistry.get(key);
        }
        if (converterclass == null) {
            throw new MessageException("1004");
        }
        try {
            Class[] parameterTypes = { MessageContext.class };
            Constructor<? extends MessageConverter> constructor = converterclass.getConstructor(parameterTypes);
            return constructor.newInstance(ctx);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof MessageException) {
                throw (MessageException) e.getTargetException();
            }
            logger.error(e.getMessage(), e);
            throw new MessageException("1004");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new MessageException("1004");
        }
    }
}
