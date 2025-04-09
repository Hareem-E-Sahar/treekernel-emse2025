package com.gorillalogic.compile.xmidoc.v1.splice;

import com.gorillalogic.config.*;
import com.gorillalogic.compile.xmidoc.v1.V1Constants;
import com.gorillalogic.util.XMLUtil;
import com.gorillalogic.util.IOUtil;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.xpath.XPathResult;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** splices components into an XMI file, allowing "cut-and-paste" type operations
 *
 *  behavior of the importer can be configured using the
 *  <code>XMISplicer.Config</code> class
 */
public class XMISplicer {

    /** configures the behavior of the <code>XMISplicer</code> class */
    public static class Config {

        /** output stream for importer log messages */
        public PrintStream log = System.out;

        /** output stream for crtitcal errors */
        public PrintStream err = System.err;

        /** if true, print stack trace on exception */
        public boolean stackTraceOnException = false;

        /** if true, print verbose status info to log */
        public boolean verbose = false;

        public int uniqueNameSuffix = 0;

        public Config() {
        }
    }

    Document _doc;

    Element _docElement;

    Element _modelElement;

    String _modelName;

    Hashtable _elementIDMap = new Hashtable();

    Config _cfg = new Config();

    XMLUtil.XPathProcessor _xpathProcessor;

    /** Creates a new instance of XMISplicer */
    public XMISplicer() {
    }

    /** used with the default ctor, when <code>importXmi()<code> will be called explicitly
     */
    public void setConfig(Config cfg) {
        _cfg = cfg;
    }

    public void spliceMethod(String modelFileNameIn, String methodOwnerPath, String methodName, String methodText) throws SpliceException {
        spliceMethod(modelFileNameIn, methodOwnerPath, methodName, methodText, modelFileNameIn);
    }

    public void spliceMethod(String modelFileNameIn, String methodFullPath, String methodText) throws SpliceException {
        spliceMethodFullPath(modelFileNameIn, methodFullPath, methodText, modelFileNameIn);
    }

    public void spliceMethodFullPath(String modelFileNameIn, String methodFullPath, String methodText, String modelFileNameOut) throws SpliceException {
        int lastDot = methodFullPath.lastIndexOf(".");
        if (lastDot == -1) {
            throw new SpliceException("could not extract method name from method path \"" + methodFullPath + "\"");
        } else {
            String ownerPath = methodFullPath.substring(0, lastDot);
            String methodName = methodFullPath.substring(lastDot + 1);
            spliceMethod(modelFileNameIn, ownerPath, methodName, methodText, modelFileNameOut);
        }
    }

    public void spliceMethod(String modelFileNameIn, String methodOwnerPath, String methodName, String methodText, String modelFileNameOut) throws SpliceException {
        InputStream xmiIn = null;
        ByteArrayOutputStream xmiOut = null;
        OutputStream modelOut = null;
        ByteArrayInputStream splicedXmiIn = null;
        try {
            if (modelFileNameIn.endsWith(".zargo") || modelFileNameIn.endsWith(".zuml")) {
                xmiIn = IOUtil.extractFromZipFile(new File(modelFileNameIn), ".xmi");
            } else if (modelFileNameIn.endsWith(".zip")) {
                xmiIn = IOUtil.extractFromZipFile(new File(modelFileNameIn), ".xml");
            } else {
                xmiIn = getFileInputStream(modelFileNameIn);
            }
            xmiOut = new ByteArrayOutputStream();
            spliceMethod(methodOwnerPath, methodName, methodText, xmiIn, xmiOut);
            xmiIn.close();
            xmiOut.close();
            splicedXmiIn = new ByteArrayInputStream(xmiOut.toByteArray());
            if (modelFileNameOut.endsWith(".zargo") || modelFileNameOut.endsWith(".zuml")) {
                replaceXmiInPoseidonFile(modelFileNameOut, splicedXmiIn);
            } else {
                modelOut = getFileOutputStream(modelFileNameOut);
                int c;
                while ((c = splicedXmiIn.read()) != -1) {
                    modelOut.write(c);
                }
                modelOut.close();
            }
            splicedXmiIn.close();
        } catch (IOException e) {
            throw new SpliceException("IO Error splicing method:", e);
        } finally {
            if (xmiIn != null) {
                try {
                    xmiIn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
            if (xmiOut != null) {
                try {
                    xmiOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
            if (modelOut != null) {
                try {
                    xmiOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
            if (splicedXmiIn != null) {
                try {
                    splicedXmiIn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
        }
    }

    public void spliceMethod(String ownerPath, String methodName, String newMethodBody, InputStream xmiIn, OutputStream xmiOut) throws SpliceException {
        trace("XMI splice begins...");
        trace("creating DOM from XMI...");
        createDoc(xmiIn);
        trace("creating XPathProcessor...");
        _xpathProcessor = new XMLUtil.XPathProcessor(getDoc());
        trace("locating UML model...");
        locateModel();
        trace("loading xmi.id map...");
        loadElementIDMap();
        trace("locating method body for method " + ownerPath + "." + methodName + "...");
        Element methodBodyElement = locateMethodBody(ownerPath, methodName);
        trace("splicing in supplied code ");
        spliceMethod(newMethodBody, methodBodyElement);
        trace("writing new XMI document... ");
        writeDoc(xmiOut);
    }

    public Document getDoc() {
        return _doc;
    }

    private Element getReferencedElement(Element e) {
        return getReferencedElement(e.getAttribute(V1Constants.XMIIDREF_ATTR));
    }

    private Element getReferencedElement(String id) {
        return (Element) _elementIDMap.get(id);
    }

    private void createDoc(InputStream in) throws SpliceException {
        trace("creating DOM Document");
        try {
            _doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            _docElement = _doc.getDocumentElement();
            in.close();
        } catch (Exception e) {
            if (_cfg.stackTraceOnException) {
                e.printStackTrace();
            }
            throw new SpliceException("Exception caught creating DOM Document: " + e.getMessage());
        }
    }

    private void locateModel() {
        trace("locating model element");
        NodeList models = _docElement.getElementsByTagName(V1Constants.MODEL_TAG);
        if (models.getLength() == 1) {
            _modelElement = (Element) models.item(0);
        }
        if (models.getLength() < 1) {
            trace("createDoc() found " + models.getLength() + " models -- nothing to import");
        } else if (models.getLength() > 1) {
            trace("createDoc() found " + models.getLength() + " models -- compiling first model only");
            _modelElement = (Element) models.item(0);
        }
        if (_modelElement != null) {
            _modelName = _modelElement.getAttribute(V1Constants.MODEL_NAME_ATTR);
        }
    }

    /** pre-loads the classes and stereotypes into the map for later use
     */
    private void loadElementIDMap() {
        trace("loading xmi.id element map");
        loadElementIDMap("*");
    }

    private void loadElementIDMap(String tag) {
        NodeList elems = _docElement.getElementsByTagName(tag);
        int numElems = elems.getLength();
        for (int i = 0; i < numElems; i++) {
            Element elem = (Element) elems.item(i);
            String id = elem.getAttribute(V1Constants.XMIID_ATTR);
            if (id.length() > 0) {
                _elementIDMap.put(id, elem);
            }
        }
    }

    public Element locateMethodBody(String ownerPath, String methodName) throws SpliceException {
        if (ownerPath == null || ownerPath.length() == 0) {
            throw new SpliceException("invalid ownerPath in locateMethod: \"" + ownerPath + "\"");
        }
        if (methodName == null || methodName.length() == 0) {
            throw new SpliceException("invalid method name in locateMethod: \"" + methodName + "\"");
        }
        return locateMethodBody_Poseidon(ownerPath, methodName);
    }

    private Element locateMethodBody_Poseidon(String ownerPath, String methodName) throws SpliceException {
        trace("finding operation element from Poseidon XMI.......");
        Element operationElement = locateOperationElement_Poseidon(ownerPath, methodName);
        if (operationElement == null) {
            throw new SpliceException("cannot find operation element for method " + methodName + " on Entity " + ownerPath);
        }
        trace("finding body element from Poseidon XMI.......");
        Element bodyElement = locateBody_Poseidon(operationElement);
        if (bodyElement == null) {
            throw new SpliceException("cannot find body element for method " + methodName + " on Entity " + ownerPath);
        }
        return bodyElement;
    }

    private Element locateOperationElement_Poseidon(String ownerPath, String methodName) throws SpliceException {
        Element e = _modelElement;
        String pathToResolve = ownerPath;
        while (pathToResolve.charAt(0) == '/') {
            pathToResolve = pathToResolve.substring(1);
        }
        int slashPos = pathToResolve.indexOf("/");
        while (slashPos != -1) {
            String packageName = pathToResolve.substring(0, slashPos);
            trace("searching for package " + packageName + "...");
            e = _xpathProcessor.getFirstElement("*/" + V1Constants.PACKAGE_TAG + "[@" + V1Constants.PACKAGE_NAME_ATTR + "=\"" + packageName + "\"]", e);
            if (e == null) {
                throw new SpliceException("unable to locate element for package " + packageName);
            }
            pathToResolve = pathToResolve.substring(slashPos + 1);
            slashPos = pathToResolve.indexOf("/");
        }
        trace("searching for class " + pathToResolve + "...");
        e = _xpathProcessor.getFirstElement("*/" + V1Constants.CLASS_TAG + "[@" + V1Constants.CLASS_NAME_ATTR + "=\"" + pathToResolve + "\"]", e);
        trace("searching for method " + methodName + "...");
        XPathResult methods = _xpathProcessor.getElements("*/" + V1Constants.OPERATION_TAG + "[@" + V1Constants.OPERATION_NAME_ATTR + "=\"" + methodName + "\"]", e);
        Element methodElement = (Element) methods.iterateNext();
        if (methods.iterateNext() != null) {
            throw new SpliceException("not yet supported, multiple methods with the same name....");
        }
        return methodElement;
    }

    private Element locateBody_Poseidon(Element operationElement) {
        Element bodyElement = null;
        String specID = operationElement.getAttribute(V1Constants.XMIID_ATTR);
        String xpath = "following-sibling::" + V1Constants.METHOD_TAG + "/" + V1Constants.METHOD_SPECIFICATION_TAG + "/" + V1Constants.OPERATION_TAG + "[attribute::" + V1Constants.XMIIDREF_ATTR + "=\"" + specID + "\"]" + "/ancestor::" + V1Constants.METHOD_TAG;
        Element methodElement = _xpathProcessor.getFirstElement(xpath, operationElement);
        bodyElement = _xpathProcessor.getFirstElement("*/" + V1Constants.METHOD_EXPRESSION_TAG, methodElement);
        return bodyElement;
    }

    private Element getTaggedValueElement_Poseidon(Element parent, String tagName) {
        Element valueElement = null;
        trace("searching for tagged value with tag: " + tagName);
        XPathResult taggedValues = _xpathProcessor.getElements("*/" + V1Constants.TAGGEDVALUE_TAG, parent);
        Node candidate = taggedValues.iterateNext();
        while (valueElement == null && candidate != null) {
            Node tvType = _xpathProcessor.getFirstElement(V1Constants.TAGGEDVALUE_TYPE_TAG, candidate);
            Element tvDefRef = (Element) _xpathProcessor.getFirstElement(V1Constants.TAGDEFINITION_TAG, tvType);
            Element tvDefinition = getReferencedElement(tvDefRef);
            String tvTypeName = tvDefinition.getAttribute(V1Constants.TAGDEFINITION_TAGTYPE_ATTR);
            if (tvTypeName.equals(tagName)) {
                valueElement = (Element) candidate;
            }
            candidate = taggedValues.iterateNext();
        }
        return valueElement;
    }

    private void spliceMethod(String methodText, Element methodBodyElement) {
        spliceMethod_Poseidon(methodText, methodBodyElement);
    }

    private void spliceMethod_Poseidon(String methodText, Element methodBodyElement) {
        methodBodyElement.setAttribute(V1Constants.METHOD_BODY_ATTR, methodText);
    }

    private void escapeMethods_Poseidon() {
        NodeList bodies = getDoc().getElementsByTagName(V1Constants.METHOD_EXPRESSION_TAG);
        for (int i = 0; i < bodies.getLength(); i++) {
            Element bodyElement = (Element) bodies.item(i);
            String body = bodyElement.getAttribute(V1Constants.METHOD_BODY_ATTR);
            body = escapeMethod_Poseidon(body);
            bodyElement.setAttribute(V1Constants.METHOD_BODY_ATTR, body);
        }
    }

    private String escapeMethod_Poseidon(String methodText) {
        String rc = methodText;
        rc = rc.replaceAll("\n", "&#10;");
        rc = rc.replaceAll("\r", "");
        return rc;
    }

    private void writeDoc(OutputStream out) throws SpliceException {
        writeDoc_Poseidon(out);
    }

    private void writeDoc_Poseidon(OutputStream out) throws SpliceException {
        try {
            escapeMethods_Poseidon();
            String s = XMLUtil.nodeToString(getDoc());
            s = s.replaceAll("&amp;#10;", "&#10;");
            out.write(s.getBytes());
        } catch (IOException e) {
            throw new SpliceException("IO Error writing output: ", e);
        }
    }

    private void trace(String s) {
        if (_cfg.verbose) {
            _cfg.log.println("(XMISplicer) " + s);
        }
    }

    private static OutputStream getFileOutputStream(String filePath) throws SpliceException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
        } catch (IOException e) {
            throw new SpliceException("Error: cannot open \"" + filePath + "\" for writing: " + e.getMessage());
        }
        if (out == null) {
            throw new SpliceException("Error: cannot open \"" + filePath + "\" for writing...");
        }
        return out;
    }

    private static InputStream getFileInputStream(String filepath) throws SpliceException {
        InputStream in = null;
        try {
            if (filepath.endsWith("zargo") || filepath.endsWith("zuml")) {
                in = IOUtil.extractFromZipFile(new File(filepath), ".xmi");
            } else {
                in = new FileInputStream(filepath);
            }
        } catch (FileNotFoundException e) {
            throw new SpliceException("-- File \"" + filepath + "\" cannot be found...");
        } catch (IOException e) {
            throw new SpliceException(e.getMessage(), e);
        }
        if (in == null) {
            throw new SpliceException("-- requested input file " + filepath + " cannot be opened for input.");
        }
        return in;
    }

    /** replaces the XMI entry in a Poseidon file, first backing up the 
     *  original file. This method manages the files and names so that
     *  the new (replaced) file has the same name as the original, and the
     *  backup is just a rename of the original to it's same name with
     *  ".gl_bak" appended.
     */
    public static void replaceXmiInPoseidonFile(String zargoFilePath, InputStream replacementXmiStream) throws SpliceException {
        File origFile = new File(zargoFilePath);
        String origFileDir = origFile.getParent();
        if (origFileDir == null || origFileDir.length() == 0) {
            origFileDir = ".";
        }
        String origFileName = origFile.getName();
        File tempFile = null;
        try {
            tempFile = File.createTempFile(origFileName, "gl_tmp", new File(origFileDir));
        } catch (IOException e) {
            throw new SpliceException("Error creating temp file for zip copy:" + e.getMessage(), e);
        }
        String tempFileName = tempFile.getPath();
        replaceXmiInPoseidonFile(zargoFilePath, replacementXmiStream, tempFileName);
        File backupFile = new File(zargoFilePath + ".gl_bak");
        backupFile.delete();
        boolean origOK = origFile.renameTo(backupFile);
        if (origOK) {
            File origFile2 = new File(zargoFilePath);
            boolean replaceOK = tempFile.renameTo(origFile2);
            if (!replaceOK) {
                throw new SpliceException("error, could not rename " + tempFile.getPath() + " to " + origFile2.getPath() + " - spliced file is " + tempFile.getPath());
            }
        } else {
            throw new SpliceException("error, could not rename " + origFile.getPath() + " to " + backupFile.getPath() + " - spliced file is " + tempFile.getPath());
        }
    }

    /** creates a new Poseidon file with the XMI part replaced
     *  from the input stream
     *  @param zargoFilePath the path to the source Poseidon file
     *  @param replacementXmiStream where to read the replaced XMI from
     *  @param outputFilePath the path for the output file
     */
    public static void replaceXmiInPoseidonFile(String zargoFilePath, InputStream replacementXmiStream, String outputFilePath) throws SpliceException {
        ZipFile zip = null;
        try {
            File zipFile = new File(zargoFilePath);
            zip = new ZipFile(zipFile);
            boolean found = false;
            Enumeration entries = zip.entries();
            while (!found && entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith("xmi")) {
                    found = true;
                    IOUtil.replaceEntryInZipFile(zargoFilePath, entryName, replacementXmiStream, outputFilePath);
                }
            }
        } catch (IOException e) {
            throw new SpliceException("Zip Exception caught while replacing entry in Poseidon file: ", e);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    throw new SpliceException("error closing zipFile: " + e.getMessage(), e);
                }
            }
        }
    }

    private static String _usage = "Usage: java com.gorillalogic.compile.splice.XMISplicer -[evh] methodName xmiInputFilePath xmiOutputFilePath [methodFilePath]\n" + "    -e              - show stack trace on exceptions \n" + "    -v              - verbose logging \n" + "    -h              - print this message to stdout\n" + "  if no methodFile is specified, the method will be read from stdin";

    public static String _xmiInputFilePath = "";

    public static String _xmiOutputFilePath = "";

    public static String _methodName = "";

    public static String _methodInputFilePath = null;

    private static ZipFile _zipIn = null;

    public static void main(String argv[]) throws SpliceException {
        System.out.println(_bannerMessage);
        Config cmdLineConfig = parseArgs(argv);
        if (cmdLineConfig == null) {
            System.err.println(_usage);
            return;
        }
        cmdLineConfig.log.println("replacing method " + _methodName + " in file " + _xmiInputFilePath + " with text from " + (_methodInputFilePath == null ? "stdin" : "_methodInputFileName") + ", output will be to " + _xmiOutputFilePath);
        try {
            Bootstrap._verbose = cmdLineConfig.verbose;
            Bootstrap.init();
        } catch (ConfigurationException e) {
            System.err.println("Error Bootstrap initialization: " + e);
        }
        XMISplicer splicer = new XMISplicer();
        splicer.setConfig(cmdLineConfig);
        InputStream methodIn = null;
        try {
            if (_methodInputFilePath != null) {
                methodIn = getFileInputStream(_methodInputFilePath);
            } else {
                methodIn = System.in;
            }
            String methodText = null;
            try {
                methodText = IOUtil.inputStreamToString(methodIn);
            } catch (IOException e) {
                throw new SpliceException("Error reading in replacement method body: " + e.getMessage(), e);
            }
            splicer.spliceMethodFullPath(_xmiInputFilePath, _methodName, methodText, _xmiOutputFilePath);
            cmdLineConfig.log.println("success.");
        } catch (SpliceException e) {
            cmdLineConfig.err.println("*** Error while processing XMI file: " + e.getMessage());
            if (cmdLineConfig.stackTraceOnException) {
                e.printStackTrace();
            }
            cmdLineConfig.err.println("*** exiting...");
        } finally {
            if (methodIn != null) {
                try {
                    methodIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Config parseArgs(String argv[]) {
        Config cfg = new Config();
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.charAt(0) == '-') {
                switch(arg.charAt(1)) {
                    case 'v':
                        cfg.verbose = true;
                        break;
                    case 'e':
                        cfg.stackTraceOnException = true;
                        break;
                    case 'h':
                        return null;
                    default:
                        System.err.println("-- unknown command-line flag " + arg);
                        return null;
                }
            } else {
                if (_methodName.equals("")) {
                    _methodName = arg;
                } else if (_xmiInputFilePath.equals("")) {
                    _xmiInputFilePath = arg;
                } else if (_xmiOutputFilePath.equals("")) {
                    _xmiOutputFilePath = arg;
                } else if (_methodInputFilePath == null) {
                    _methodInputFilePath = arg;
                } else {
                    System.err.println("-- syntax error: too many command-line parameters: " + arg + "...");
                    return null;
                }
            }
        }
        if (_xmiInputFilePath.equals("")) {
            System.err.println("-- no input file specified.");
            return null;
        }
        if (_xmiOutputFilePath.equals("")) {
            System.err.println("-- no output file specified.");
            return null;
        }
        if (_methodName.equals("")) {
            System.err.println("-- no method name specified.");
            return null;
        }
        return cfg;
    }

    private static final String _bannerMessage = "XMISplicer  (c) 2003 Gorilla Logic, Inc.";
}
