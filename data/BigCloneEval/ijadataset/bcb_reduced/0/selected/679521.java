package xml.parser;

import java.net.URISyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.parser.XMLDTDFilter;
import org.apache.xerces.xni.parser.XMLDTDSource;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import sidekick.IAsset;
import sidekick.SideKickParsedData;
import xml.CharSequenceReader;
import xml.AntXmlParsedData;
import xml.XmlParsedData;
import xml.XmlPlugin;
import xml.SchemaMappingManager;
import xml.completion.CompletionInfo;
import xml.gui.XmlModeToolBar;
import xml.parser.MyEntityResolver.IOExceptionWithLocation;
import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;
import static xml.Debug.*;

/**
 * A SideKick XML parser that uses this under the covers:
 * reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
 *
 * @author kerik-sf
 * @version $Id: XercesParserImpl.java 21148 2012-02-18 15:09:52Z kerik-sf $
 */
public class XercesParserImpl extends XmlParser {

    public static String COMPLETION_INFO_CACHE_ENTRY = "CompletionInfo";

    private View view = null;

    private Map<View, JPanel> panels = new HashMap<View, JPanel>();

    public XercesParserImpl() {
        super("xml");
    }

    @Override
    public void activate(View view) {
        this.view = view;
    }

    /**
	 * a buffer read lock is hold arround parse()
	 */
    public SideKickParsedData parse(Buffer buffer, DefaultErrorSource errorSource) {
        long start = System.currentTimeMillis();
        Log.log(Log.NOTICE, XercesParserImpl.class, "parsing started @" + start);
        stopped = false;
        CharSequence text;
        text = buffer.getSegment(0, buffer.getLength());
        XmlParsedData data = createXmlParsedData(buffer.getName(), buffer.getMode().toString(), false);
        if (text.length() == 0) return data;
        SchemaMapping mapping;
        if (SchemaMappingManager.isSchemaMappingEnabled(buffer)) {
            mapping = SchemaMappingManager.getSchemaMappingForBuffer(buffer);
        } else {
            mapping = null;
        }
        ErrorListErrorHandler errorHandler = new ErrorListErrorHandler(errorSource, buffer.getPath());
        MyEntityResolver resolver = new MyEntityResolver(buffer, errorHandler);
        GrabIdsAndCompletionInfoHandler handler = new GrabIdsAndCompletionInfoHandler(this, buffer, errorHandler, data, resolver);
        XMLReader reader = null;
        SchemaAutoLoader schemaLoader = null;
        try {
            reader = new org.apache.xerces.parsers.SAXParser(new EntityMgrFixerConfiguration(null, new CachedGrammarPool(buffer)));
            reader.setFeature("http://xml.org/sax/features/validation", buffer.getBooleanProperty("xml.validate"));
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", buffer.getBooleanProperty("xml.validate"));
            reader.setFeature("http://apache.org/xml/features/validation/schema", buffer.getBooleanProperty("xml.validate"));
            if (buffer.getBooleanProperty("xml.validate.ignore-dtd")) {
                reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            }
            reader.setFeature("http://xml.org/sax/features/namespaces", !buffer.getBooleanProperty("xml.namespaces.disable"));
            reader.setFeature("http://xml.org/sax/features/use-entity-resolver2", true);
            reader.setFeature("http://apache.org/xml/features/xinclude", buffer.getBooleanProperty("xml.xinclude"));
            reader.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", buffer.getBooleanProperty("xml.xinclude.fixup-base-uris"));
            reader.setProperty("http://xml.org/sax/properties/declaration-handler", handler);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            schemaLoader = new SchemaAutoLoader(reader, mapping, buffer);
            schemaLoader.setErrorHandler(errorHandler);
            schemaLoader.setContentHandler(handler);
            schemaLoader.setEntityResolver(resolver);
            handler.setSchemaAutoLoader(schemaLoader);
            reader = schemaLoader;
            if (!SchemaMappingManager.isSchemaMappingEnabled(buffer)) {
                String schemaFromProp = buffer.getStringProperty(SchemaMappingManager.BUFFER_SCHEMA_PROP);
                if (schemaFromProp != null) {
                    String baseURI = xml.PathUtilities.pathToURL(buffer.getPath());
                    Log.log(Log.NOTICE, this, "forcing schema to {" + baseURI + "," + schemaFromProp + "}");
                    try {
                        schemaLoader.forceSchema(baseURI, schemaFromProp);
                    } catch (IOException ioe) {
                        Log.log(Log.WARNING, this, "I/O error loading forced schema: " + ioe.getClass() + ": " + ioe.getMessage());
                    } catch (URISyntaxException e) {
                        Log.log(Log.WARNING, this, "forced schema URL is invalid: " + e.getMessage());
                    }
                }
            }
        } catch (SAXException se) {
            Log.log(Log.ERROR, this, "unexpected error preparing XML parser, please report", se);
        }
        CompletionInfo info = CompletionInfo.getCompletionInfoForBuffer(buffer);
        if (info != null) data.setCompletionInfo("", info);
        Exception errorParsing = null;
        InputSource source = new InputSource();
        String rootDocument = buffer.getStringProperty("xml.root");
        if (rootDocument != null) {
            Log.log(Log.NOTICE, this, "rootDocument specified; " + "parsing " + rootDocument);
            rootDocument = MiscUtilities.constructPath(MiscUtilities.getParentOfPath(buffer.getPath()), rootDocument);
            source.setSystemId(xml.PathUtilities.pathToURL(rootDocument));
        } else {
            source.setCharacterStream(new CharSequenceReader(text));
            source.setSystemId(xml.PathUtilities.pathToURL(buffer.getPath()));
        }
        try {
            reader.parse(source);
        } catch (StoppedException e) {
            errorParsing = e;
        } catch (IOExceptionWithLocation ioe) {
            String msg = "I/O error while parsing: " + ioe.getMessage() + ", caused by " + ioe.getCause().getClass().getName() + ": " + ioe.getCause().getMessage();
            Log.log(Log.WARNING, this, msg);
            errorSource.addError(ErrorSource.ERROR, ioe.path, ioe.line, 0, 0, msg);
            errorParsing = ioe;
        } catch (IOException ioe) {
            Log.log(Log.WARNING, this, "I/O error while parsing: " + ioe.getClass().getName() + ": " + ioe.getMessage());
            errorSource.addError(ErrorSource.ERROR, buffer.getPath(), 0, 0, 0, ioe.getClass() + ": " + ioe.getMessage());
            errorParsing = ioe;
        } catch (SAXParseException spe) {
            errorParsing = spe;
        } catch (SAXException se) {
            String msg = "SAX exception while parsing";
            Throwable t = se.getException();
            if (msg != null) {
                msg += ": " + se.getMessage();
            }
            if (t != null) {
                msg += " caused by " + t;
            }
            Log.log(Log.WARNING, this, msg);
            errorSource.addError(ErrorSource.ERROR, buffer.getPath(), 0, 0, 0, msg);
            errorParsing = se;
        } finally {
            if (schemaLoader != null && schemaLoader.getSchemaURL() != null) {
                buffer.setStringProperty(SchemaMappingManager.BUFFER_AUTO_SCHEMA_PROP, schemaLoader.getSchemaURL());
            }
        }
        if (!(errorParsing instanceof StoppedException)) {
            ConstructTreeHandler treeHandler = new ConstructTreeHandler(this, buffer, text, errorHandler, data, resolver);
            reader = null;
            try {
                reader = new org.apache.xerces.parsers.SAXParser(new EntityMgrFixerConfiguration(null, new CachedGrammarPool(buffer)));
                reader.setFeature("http://xml.org/sax/features/validation", false);
                reader.setFeature("http://xml.org/sax/features/namespaces", !buffer.getBooleanProperty("xml.namespaces.disable"));
                reader.setFeature("http://xml.org/sax/features/use-entity-resolver2", true);
                reader.setFeature("http://apache.org/xml/features/xinclude", false);
                reader.setContentHandler(treeHandler);
                reader.setEntityResolver(resolver);
            } catch (SAXException se) {
                Log.log(Log.ERROR, this, "error preparing to parse 2nd pass), please report !", se);
            }
            source = new InputSource();
            source.setCharacterStream(new CharSequenceReader(text));
            source.setSystemId(xml.PathUtilities.pathToURL(buffer.getPath()));
            try {
                reader.parse(source);
            } catch (StoppedException e) {
            } catch (IOException e) {
                if (errorParsing == null || !e.getClass().equals(errorParsing.getClass()) || (!e.toString().equals(errorParsing.toString()))) {
                    Log.log(Log.ERROR, this, "I/O error upon snd reparse :" + e.getClass() + ": " + e.getMessage());
                }
            } catch (SAXParseException e) {
            } catch (SAXException se) {
                if (errorParsing == null || !se.getClass().equals(errorParsing.getClass()) || (!se.toString().equals(errorParsing.toString()))) {
                    String msg = "SAX exception while parsing (constructing sidekick tree)";
                    Throwable t = se.getException();
                    if (msg != null) {
                        msg += ": " + se.getMessage();
                    }
                    if (t != null) {
                        msg += " caused by " + t;
                    }
                    Log.log(Log.WARNING, this, msg);
                    errorSource.addError(ErrorSource.ERROR, buffer.getPath(), 0, 0, 0, msg);
                }
            }
        }
        DefaultMutableTreeNode root = data.root;
        IAsset rootAsset = (IAsset) root.getUserObject();
        if ("project".equals(rootAsset.getName())) {
            buffer.setMode("ant");
            AntXmlParsedData pd = new AntXmlParsedData(buffer.getName(), false);
            pd.root = data.root;
            pd.tree = data.tree;
            pd.expansionModel = data.expansionModel;
            data = pd;
        }
        data.done(view);
        long end = System.currentTimeMillis();
        Log.log(Log.NOTICE, XercesParserImpl.class, "parsing has taken " + (end - start) + "ms");
        return data;
    }

    private XmlParsedData createXmlParsedData(String filename, String modeName, boolean html) {
        String dataClassName = jEdit.getProperty("xml.xmlparseddata." + modeName);
        if (dataClassName != null) {
            try {
                Class dataClass = Class.forName(dataClassName);
                java.lang.reflect.Constructor con = dataClass.getConstructor(String.class, Boolean.TYPE);
                return (XmlParsedData) con.newInstance(filename, html);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new XmlParsedData(filename, html);
    }

    public JPanel getPanel() {
        if (view != null) {
            String mode = view.getBuffer().getMode().toString();
            String supported = jEdit.getProperty("xml.xmltoolbar.modes");
            if (supported.indexOf(mode) > -1) {
                JPanel panel = panels.get(view);
                if (panel != null) {
                    return panel;
                }
                XmlModeToolBar toolbar = new XmlModeToolBar(view);
                panels.put(view, toolbar);
                return toolbar;
            }
        }
        return null;
    }

    static class StoppedException extends SAXException {

        StoppedException() {
            super("Parsing stopped");
        }
    }
}
