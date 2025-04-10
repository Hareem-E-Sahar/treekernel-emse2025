package com.sun.org.apache.xml.internal.dtm.ref;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/** <p>IncrementalSAXSource_Xerces takes advantage of the fact that Xerces1
 * incremental mode is already a coroutine of sorts, and just wraps our
 * IncrementalSAXSource API around it.</p>
 *
 * <p>Usage example: See main().</p>
 *
 * <p>Status: Passes simple main() unit-test. NEEDS JAVADOC.</p>
 * */
public class IncrementalSAXSource_Xerces implements IncrementalSAXSource {

    Method fParseSomeSetup = null;

    Method fParseSome = null;

    Object fPullParserConfig = null;

    Method fConfigSetInput = null;

    Method fConfigParse = null;

    Method fSetInputSource = null;

    Constructor fConfigInputSourceCtor = null;

    Method fConfigSetByteStream = null;

    Method fConfigSetCharStream = null;

    Method fConfigSetEncoding = null;

    Method fReset = null;

    SAXParser fIncrementalParser;

    private boolean fParseInProgress = false;

    /** Create a IncrementalSAXSource_Xerces, and create a SAXParser
   * to go with it. Xerces2 incremental parsing is only supported if
   * this constructor is used, due to limitations in the Xerces2 API (as of
   * Beta 3). If you don't like that restriction, tell the Xerces folks that
   * there should be a simpler way to request incremental SAX parsing.
   * */
    public IncrementalSAXSource_Xerces() throws NoSuchMethodException {
        try {
            Class xniConfigClass = ObjectFactory.findProviderClass("com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration", ObjectFactory.findClassLoader(), true);
            Class[] args1 = { xniConfigClass };
            Constructor ctor = SAXParser.class.getConstructor(args1);
            Class xniStdConfigClass = ObjectFactory.findProviderClass("com.sun.org.apache.xerces.internal.parsers.StandardParserConfiguration", ObjectFactory.findClassLoader(), true);
            fPullParserConfig = xniStdConfigClass.newInstance();
            Object[] args2 = { fPullParserConfig };
            fIncrementalParser = (SAXParser) ctor.newInstance(args2);
            Class fXniInputSourceClass = ObjectFactory.findProviderClass("com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource", ObjectFactory.findClassLoader(), true);
            Class[] args3 = { fXniInputSourceClass };
            fConfigSetInput = xniStdConfigClass.getMethod("setInputSource", args3);
            Class[] args4 = { String.class, String.class, String.class };
            fConfigInputSourceCtor = fXniInputSourceClass.getConstructor(args4);
            Class[] args5 = { java.io.InputStream.class };
            fConfigSetByteStream = fXniInputSourceClass.getMethod("setByteStream", args5);
            Class[] args6 = { java.io.Reader.class };
            fConfigSetCharStream = fXniInputSourceClass.getMethod("setCharacterStream", args6);
            Class[] args7 = { String.class };
            fConfigSetEncoding = fXniInputSourceClass.getMethod("setEncoding", args7);
            Class[] argsb = { Boolean.TYPE };
            fConfigParse = xniStdConfigClass.getMethod("parse", argsb);
            Class[] noargs = new Class[0];
            fReset = fIncrementalParser.getClass().getMethod("reset", noargs);
        } catch (Exception e) {
            IncrementalSAXSource_Xerces dummy = new IncrementalSAXSource_Xerces(new SAXParser());
            this.fParseSomeSetup = dummy.fParseSomeSetup;
            this.fParseSome = dummy.fParseSome;
            this.fIncrementalParser = dummy.fIncrementalParser;
        }
    }

    /** Create a IncrementalSAXSource_Xerces wrapped around
   * an existing SAXParser. Currently this works only for recent
   * releases of Xerces-1.  Xerces-2 incremental is currently possible
   * only if we are allowed to create the parser instance, due to
   * limitations in the API exposed by Xerces-2 Beta 3; see the
   * no-args constructor for that code.
   * 
   * @exception if the SAXParser class doesn't support the Xerces
   * incremental parse operations. In that case, caller should
   * fall back upon the IncrementalSAXSource_Filter approach.
   * */
    public IncrementalSAXSource_Xerces(SAXParser parser) throws NoSuchMethodException {
        fIncrementalParser = parser;
        Class me = parser.getClass();
        Class[] parms = { InputSource.class };
        fParseSomeSetup = me.getMethod("parseSomeSetup", parms);
        parms = new Class[0];
        fParseSome = me.getMethod("parseSome", parms);
    }

    public static IncrementalSAXSource createIncrementalSAXSource() {
        try {
            return new IncrementalSAXSource_Xerces();
        } catch (NoSuchMethodException e) {
            IncrementalSAXSource_Filter iss = new IncrementalSAXSource_Filter();
            iss.setXMLReader(new SAXParser());
            return iss;
        }
    }

    public static IncrementalSAXSource createIncrementalSAXSource(SAXParser parser) {
        try {
            return new IncrementalSAXSource_Xerces(parser);
        } catch (NoSuchMethodException e) {
            IncrementalSAXSource_Filter iss = new IncrementalSAXSource_Filter();
            iss.setXMLReader(parser);
            return iss;
        }
    }

    public void setContentHandler(org.xml.sax.ContentHandler handler) {
        ((XMLReader) fIncrementalParser).setContentHandler(handler);
    }

    public void setLexicalHandler(org.xml.sax.ext.LexicalHandler handler) {
        try {
            ((XMLReader) fIncrementalParser).setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        } catch (org.xml.sax.SAXNotRecognizedException e) {
        } catch (org.xml.sax.SAXNotSupportedException e) {
        }
    }

    public void setDTDHandler(org.xml.sax.DTDHandler handler) {
        ((XMLReader) fIncrementalParser).setDTDHandler(handler);
    }

    /** startParse() is a simple API which tells the IncrementalSAXSource
   * to begin reading a document.
   *
   * @throws SAXException is parse thread is already in progress
   * or parsing can not be started.
   * */
    public void startParse(InputSource source) throws SAXException {
        if (fIncrementalParser == null) throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_STARTPARSE_NEEDS_SAXPARSER, null));
        if (fParseInProgress) throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_STARTPARSE_WHILE_PARSING, null));
        boolean ok = false;
        try {
            ok = parseSomeSetup(source);
        } catch (Exception ex) {
            throw new SAXException(ex);
        }
        if (!ok) throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COULD_NOT_INIT_PARSER, null));
    }

    /** deliverMoreNodes() is a simple API which tells the coroutine
   * parser that we need more nodes.  This is intended to be called
   * from one of our partner routines, and serves to encapsulate the
   * details of how incremental parsing has been achieved.
   *
   * @param parsemore If true, tells the incremental parser to generate
   * another chunk of output. If false, tells the parser that we're
   * satisfied and it can terminate parsing of this document.
   * @return Boolean.TRUE if the CoroutineParser believes more data may be available
   * for further parsing. Boolean.FALSE if parsing ran to completion.
   * Exception if the parser objected for some reason.
   * */
    public Object deliverMoreNodes(boolean parsemore) {
        if (!parsemore) {
            fParseInProgress = false;
            return Boolean.FALSE;
        }
        Object arg;
        try {
            boolean keepgoing = parseSome();
            arg = keepgoing ? Boolean.TRUE : Boolean.FALSE;
        } catch (SAXException ex) {
            arg = ex;
        } catch (IOException ex) {
            arg = ex;
        } catch (Exception ex) {
            arg = new SAXException(ex);
        }
        return arg;
    }

    private boolean parseSomeSetup(InputSource source) throws SAXException, IOException, IllegalAccessException, java.lang.reflect.InvocationTargetException, java.lang.InstantiationException {
        if (fConfigSetInput != null) {
            Object[] parms1 = { source.getPublicId(), source.getSystemId(), null };
            Object xmlsource = fConfigInputSourceCtor.newInstance(parms1);
            Object[] parmsa = { source.getByteStream() };
            fConfigSetByteStream.invoke(xmlsource, parmsa);
            parmsa[0] = source.getCharacterStream();
            fConfigSetCharStream.invoke(xmlsource, parmsa);
            parmsa[0] = source.getEncoding();
            fConfigSetEncoding.invoke(xmlsource, parmsa);
            Object[] noparms = new Object[0];
            fReset.invoke(fIncrementalParser, noparms);
            parmsa[0] = xmlsource;
            fConfigSetInput.invoke(fPullParserConfig, parmsa);
            return parseSome();
        } else {
            Object[] parm = { source };
            Object ret = fParseSomeSetup.invoke(fIncrementalParser, parm);
            return ((Boolean) ret).booleanValue();
        }
    }

    private static final Object[] noparms = new Object[0];

    private static final Object[] parmsfalse = { Boolean.FALSE };

    private boolean parseSome() throws SAXException, IOException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        if (fConfigSetInput != null) {
            Object ret = (Boolean) (fConfigParse.invoke(fPullParserConfig, parmsfalse));
            return ((Boolean) ret).booleanValue();
        } else {
            Object ret = fParseSome.invoke(fIncrementalParser, noparms);
            return ((Boolean) ret).booleanValue();
        }
    }

    /** Simple unit test. Attempt coroutine parsing of document indicated
   * by first argument (as a URI), report progress.
   */
    public static void _main(String args[]) {
        System.out.println("Starting...");
        CoroutineManager co = new CoroutineManager();
        int appCoroutineID = co.co_joinCoroutineSet(-1);
        if (appCoroutineID == -1) {
            System.out.println("ERROR: Couldn't allocate coroutine number.\n");
            return;
        }
        IncrementalSAXSource parser = createIncrementalSAXSource();
        com.sun.org.apache.xml.internal.serialize.XMLSerializer trace;
        trace = new com.sun.org.apache.xml.internal.serialize.XMLSerializer(System.out, null);
        parser.setContentHandler(trace);
        parser.setLexicalHandler(trace);
        for (int arg = 0; arg < args.length; ++arg) {
            try {
                InputSource source = new InputSource(args[arg]);
                Object result = null;
                boolean more = true;
                parser.startParse(source);
                for (result = parser.deliverMoreNodes(more); result == Boolean.TRUE; result = parser.deliverMoreNodes(more)) {
                    System.out.println("\nSome parsing successful, trying more.\n");
                    if (arg + 1 < args.length && "!".equals(args[arg + 1])) {
                        ++arg;
                        more = false;
                    }
                }
                if (result instanceof Boolean && ((Boolean) result) == Boolean.FALSE) {
                    System.out.println("\nParser ended (EOF or on request).\n");
                } else if (result == null) {
                    System.out.println("\nUNEXPECTED: Parser says shut down prematurely.\n");
                } else if (result instanceof Exception) {
                    throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException((Exception) result);
                }
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    }
}
