package com.polarrose.xmlrpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *  An XmlRpcClient represents a connection to an XML-RPC enabled server. It
 *  implements the XmlRpcInvocationHandler so that it may be used as a relay
 *  to other XML-RPC servers when installed in an XmlRpcServer.
 *
 *  @author Greger Olsson
 */
public class XmlRpcClient extends XmlRpcParser implements XmlRpcInvocationHandler {

    /**
     *  Creates a new client with the ability to send XML-RPC messages
     *  to the the server at the given URL.
     *
     *  @param url the URL at which the XML-RPC service is locaed
     *
     *  @param streamMessages Indicates whether or not to stream messages directly
     *                        or if the messages should be completed locally
     *                        before being sent all at once. Streaming is not
     *                        directly supported by XML-RPC, since the
     *                        Content-Length header is not included in the HTTP post.
     *                        If the other end is not relying on Content-Length,
     *                        streaming the message directly is much more efficient.
     * @throws MalformedURLException
     */
    public XmlRpcClient(String url, boolean streamMessages) throws MalformedURLException {
        this(new URL(url), streamMessages);
    }

    /**
     *  @see XmlRpcClient(String,boolean)
     */
    public XmlRpcClient(URL url, boolean streamMessages) {
        this.url = url;
        this.streamMessages = streamMessages;
        if (!streamMessages) {
            writer = new StringWriter(2048);
        }
    }

    /**
     *  Invokes a method on the terminating XML-RPC end point. The supplied method name and
     *  argument collection is used to encode the call into an XML-RPC compatible message.
     *
     *  @param method The name of the method to call.
     *
     *  @param arguments The arguments to encode in the call.
     *
     *  @return The object returned from the terminating XML-RPC end point.
     *
     *  @throws XmlRpcException One or more of the supplied arguments are unserializable. That is,
     *                          the built-in serializer connot parse it or find a custom serializer
     *                          that can. There may also be problems with the socket communication.
     * @throws
     */
    public synchronized Object invoke(String method, List arguments) throws XmlRpcException, XmlRpcFault {
        beginCall(method);
        if (arguments != null) {
            Iterator argIter = arguments.iterator();
            while (argIter.hasNext()) {
                try {
                    writer.write("<param>");
                    serializer.serialize(argIter.next(), writer);
                    writer.write("</param>");
                } catch (IOException ioe) {
                    throw new XmlRpcException(XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
                }
            }
        }
        return endCall();
    }

    /**
     *  Invokes a method on the terminating XML-RPC end point. The supplied method name and
     *  argument vector is used to encode the call into XML-RPC.
     *
     *  @param method The name of the method to call.
     *
     *  @param arguments The arguments to encode in the call.
     *
     *  @return The object returned from the terminating XML-RPC end point.
     *
     *  @throws XmlRpcException One or more of the supplied arguments are unserializable. That is,
     *                          the built-in serializer connot parse it or find a custom serializer
     *                          that can. There may also be problems with the socket communication.
     */
    public synchronized Object invoke(String method, Object[] arguments) throws XmlRpcException, XmlRpcFault {
        beginCall(method);
        if (arguments != null) {
            for (int i = 0; i < arguments.length; ++i) {
                try {
                    writer.write("<param>");
                    serializer.serialize(arguments[i], writer);
                    writer.write("</param>");
                } catch (IOException ioe) {
                    throw new XmlRpcException(XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
                }
            }
        }
        return endCall();
    }

    /**
     *  A asynchronous version of invoke performing the call in a separate thread and
     *  reporting responses, faults, and exceptions through the supplied XmlRpcCallback.
     *  TODO Determine on proper strategy for instantiating Threads.
     *
     *  @param method The name of the method at the server.
     *
     *  @param arguments The arguments for the call. This may be either a java.util.List
     *                   descendant, or a java.lang.Object[] array.
     *
     *  @param callback An object implementing the XmlRpcCallback interface. If callback is
     *                  null, the call will be performed but any results, faults, or exceptions
     *                  will be ignored (fire and forget).
     */
    public void invokeAsynchronously(final String method, final Object arguments, final XmlRpcCallback callback) {
        if (callback == null) {
            new Thread() {

                public void run() {
                    try {
                        if (arguments instanceof Object[]) invoke(method, (Object[]) arguments); else invoke(method, (List) arguments);
                    } catch (XmlRpcFault e) {
                    } catch (XmlRpcException e) {
                    }
                }
            }.start();
        } else {
            new Thread() {

                public void run() {
                    Object result = null;
                    try {
                        if (arguments instanceof Object[]) result = invoke(method, (Object[]) arguments); else result = invoke(method, (List) arguments);
                        callback.onResult(result);
                    } catch (XmlRpcException e) {
                        callback.onException(e);
                    } catch (XmlRpcFault e) {
                        XmlRpcStruct fault = (XmlRpcStruct) result;
                        callback.onFault(fault.getInteger("faultCode"), fault.getString("faultString"));
                    }
                }
            }.start();
        }
    }

    /**
     *  Initializes the XML buffer to be sent to the server with the XML-RPC
     *  content common to all method calls, or serializes it directly over the writer
     *  if streaming is used. The parameters to the call are added in the execute()
     *  method, and the closing tags are appended when the call is finalized in endCall().
     *
     *  @param methodName The name of the method to call.
     */
    private void beginCall(String methodName) throws XmlRpcException {
        try {
            if (streamMessages) {
                openConnection();
                writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            } else {
                ((StringWriter) writer).getBuffer().setLength(0);
            }
            writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
            writer.write("<methodCall><methodName>");
            writer.write(methodName);
            writer.write("</methodName><params>");
        } catch (IOException ioe) {
            throw new XmlRpcException(XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
        }
    }

    /**
     *  Finalizaes the XML buffer to be sent to the server, and creates a HTTP buffer for
     *  the call. Both buffers are combined into an XML-RPC message that is sent over
     *  a socket to the server.
     *
     *  @return The parsed return value of the call.
     *
     *  @throws XmlRpcException when some IO problem occur.
     */
    private Object endCall() throws XmlRpcException, XmlRpcFault {
        try {
            writer.write("</params>");
            writer.write("</methodCall>");
            if (streamMessages) {
                writer.flush();
            } else {
                StringBuffer buffer = ((StringWriter) writer).getBuffer();
                openConnection();
                connection.setRequestProperty("Content-Length", String.valueOf(buffer.length()));
                OutputStream output = new BufferedOutputStream(connection.getOutputStream());
                output.write(buffer.toString().getBytes());
                output.flush();
                output.close();
            }
            handleResponse();
        } catch (IOException ioe) {
            throw new XmlRpcException(XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
        } finally {
            try {
                writer.close();
            } catch (IOException ignore) {
            }
            connection.disconnect();
            connection = null;
        }
        return returnValue;
    }

    /**
     *  Handles the response returned by the XML-RPC server. If the server responds with a
     *  "non-200"-HTTP response or if the XML payload is unparseable, this is interpreted
     *  as an error in communication and will result in an XmlRpcException.<p>
     *
     *  If the user does not want the socket to be kept alive or if the server does not
     *  support keep-alive, the socket is closed.
     *
     *  @param inout The stream containing the server response to interpret.
     *
     *  @throws IOException If a socket error occurrs, or if the XML returned is unparseable.
     *                      This exception is currently also thrown if a HTTP response other
     *                      than "200 OK" is received.
     * @throws XmlRpcFault
     */
    private void handleResponse() throws XmlRpcFault {
        try {
            parse(new BufferedInputStream(connection.getInputStream()));
        } catch (Exception e) {
            throw new XmlRpcException(XmlRpcMessages.getString("XmlRpcClient.ParseError"), e);
        }
        if (isFaultResponse) {
            XmlRpcStruct fault = (XmlRpcStruct) returnValue;
            isFaultResponse = false;
            throw new XmlRpcFault(fault.getInteger("faultCode"), fault.getString("faultString"));
        }
    }

    /**
     *  Override the startElement() method inherited from XmlRpcParser. This way, we may set
     *  the error flag if we run into a fault-tag.
     *
     *  @param See SAX documentation
     */
    public void startElement(String uri, String name, String qualifiedName, Attributes attributes) throws SAXException {
        if (name.equals("fault")) {
            isFaultResponse = true;
        } else {
            super.startElement(uri, name, qualifiedName, attributes);
        }
    }

    /**
     *  Stores away the one and only value contained in XML-RPC responses.
     *
     *  @param value The contained return value.
     */
    protected void handleParsedValue(Object value) {
        returnValue = value;
    }

    /**
     *  <describe>
     *
     *  @throws IOException
     */
    private void openConnection() throws IOException {
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml");
    }

    /** The server URL. */
    private URL url;

    /** Connection to the server. */
    private HttpURLConnection connection;

    /** The parsed value returned in a response. */
    private Object returnValue;

    /** Writer to which XML-RPC messages are serialized. */
    private Writer writer;

    /** Indicates wheter or not we shall stream the message directly or build them locally? */
    private boolean streamMessages;

    /** Indicates whether or not the incoming response is a fault response. */
    private boolean isFaultResponse;

    /** The serializer used to serialize arguments. */
    private XmlRpcSerializer serializer = new XmlRpcSerializer();
}
