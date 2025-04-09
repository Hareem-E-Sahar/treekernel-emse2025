import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import prjfbtypes.*;
import fbench.ParserLib;

public class FBXMLParser implements Constants {

    private Document xmlDocument = null;

    private String result = null;

    private JTextPane console = null;

    private String modelType = null;

    private boolean isFile;

    public FBXMLParser(String input, String newDTD, boolean isFile, JTextPane console) throws IOException, JDOMException {
        parse(input, newDTD, isFile, console);
    }

    public FBXMLParser(String input, JTextPane console) throws IOException, JDOMException {
        parse(input, dtdPath, true, console);
    }

    public FBXMLParser(String input, boolean isFile, JTextPane console) throws IOException, JDOMException {
        parse(input, dtdPath, isFile, console);
    }

    public void parse(String input, String newDTD, boolean isFile, JTextPane console) throws IOException, JDOMException {
        this.console = console;
        this.isFile = isFile;
        input = removeInvalidChars(input);
        if (isFile) {
            if (newDTD != null) {
                try {
                    xmlDocument = ParserLib.parser(new StringReader(changeDTD(input, isFile, newDTD).toString()), false);
                } catch (Exception e) {
                    System.out.println(input);
                    e.printStackTrace();
                }
            } else {
                xmlDocument = ParserLib.parser(input, false);
            }
            if (xmlDocument == null) {
                System.exit(0);
            }
            ParserLib.validator(xmlDocument);
            result = ParserLib.xmlOutputter.outputString(xmlDocument);
        } else {
            String in = new String(input);
            in = in.replace("http://www.holobloc.com/xml/LibraryElement.dtd", newDTD);
            xmlDocument = preProcess(in);
            ParserLib.validator(xmlDocument);
            result = ParserLib.xmlOutputter.outputString(xmlDocument);
        }
        if (xmlDocument != null) {
        }
    }

    private String removeInvalidChars(String input) {
        String text;
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        if (isFile) {
            try {
                reader = new BufferedReader(new FileReader(input));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
                reader.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            text = contents.toString();
        } else {
            text = input;
        }
        Pattern pattern = Pattern.compile("Comment=\"[a-zA-Z0-9 \t\\-\\+\\{\\}!@#$%\\^\\&\\*\\(\\;:\\',./\\?\\\\|<>)]*\"");
        Matcher matcher = pattern.matcher(text);
        boolean found = false;
        StringBuilder newText = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            String str = matcher.group();
            str = str.substring(str.indexOf("\"") + 1);
            str = str.substring(0, str.lastIndexOf("\""));
            if (str.contains("&") || str.contains("<") || str.contains(">") || str.contains("'")) {
                if (!found) found = true;
                str = str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;").replace("\"", "&quot;");
                newText.append(text.substring(lastEnd, matcher.start()) + "Comment=\"" + str + "\"");
                lastEnd = matcher.end();
            }
        }
        if (found) {
            newText.append(text.substring(lastEnd));
            if (isFile) {
                File temp = null;
                Writer output = null;
                try {
                    temp = File.createTempFile("fbench", "");
                    if (temp != null && temp.exists()) {
                        output = new BufferedWriter(new FileWriter(temp));
                        output.write(newText.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                input = temp.getPath();
            } else {
                input = newText.toString();
            }
        }
        return input;
    }

    private StringBuilder changeDTD(String input, boolean isFile, String newDTD) throws IOException {
        Reader r = null;
        StringBuilder strBuilder = new StringBuilder();
        if (isFile) {
            input = input.replace('/', File.separatorChar);
            newDTD = newDTD.replace('/', File.separatorChar);
            File originalFile = new File(input);
            r = new FileReader(originalFile);
        } else {
            r = new StringReader(input);
        }
        BufferedReader in = new BufferedReader(r);
        String line = null;
        int count = 0;
        while ((line = in.readLine()) != null) {
            if (count == 1) {
                StringBuilder temp = new StringBuilder(line);
                if (line.contains("http") && (line.contains("FBType") || line.contains("System"))) {
                    temp.replace(line.indexOf("http"), line.indexOf("\"", line.indexOf("http")), newDTD);
                    line = temp.toString();
                } else if (line.contains("../LibraryElement.dtd")) {
                    int index = line.indexOf("../LibraryElement.dtd");
                    temp.replace(index, index + "../LibraryElement.dtd".length(), newDTD);
                    line = temp.toString();
                }
            }
            strBuilder.append(line);
            strBuilder.append(System.getProperty("line.separator"));
            count++;
        }
        return strBuilder;
    }

    private Document preProcess(Document xmlDocument) {
        DocType dt = xmlDocument.getDocType();
        if (dt != null) {
            String docType = dt.getElementName();
            if (docType.equals("FBType") || docType.equals("System")) {
                Element root = (Element) xmlDocument.getRootElement().clone();
                Document newXmlDocument = new Document(root);
                root = newXmlDocument.getRootElement();
                Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                root.addNamespaceDeclaration(xsi);
                String path = Constants.xsdModuleNetworkPath;
                root.setAttribute("noNamespaceSchemaLocation", path, xsi);
                return newXmlDocument;
            } else {
                return null;
            }
        } else {
            return xmlDocument;
        }
    }

    private Document preProcess(String inputString) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
        return this.preProcess(saxBuilder.build(new StringReader(inputString)));
    }

    public String getXmlDocumentString() {
        return result;
    }

    public Document getXmlDocument() {
        return this.xmlDocument;
    }

    protected Identification getIdentification(Element identificationElem) {
        if (identificationElem != null) {
            String Standard = identificationElem.getAttributeValue("Standard");
            String Classification = identificationElem.getAttributeValue("Classification");
            String ApplicationDomain = identificationElem.getAttributeValue("ApplicationDomain");
            String Function = identificationElem.getAttributeValue("Function");
            String Type = identificationElem.getAttributeValue("Type");
            String Description = identificationElem.getAttributeValue("Description");
            Identification identification = new Identification(Standard, Classification, ApplicationDomain, Function, Type, Description);
            return identification;
        } else {
            System.err.println("Invalid element input in getIdentification()");
            if (this.console != null) {
                console.setText("Invalid element input in getIdentification()");
            }
            return null;
        }
    }

    protected VersionInfo getVersionInfo(Element versionInfoElem) {
        if (versionInfoElem != null) {
            String Organization = versionInfoElem.getAttributeValue("Organization");
            String Version = versionInfoElem.getAttributeValue("Version");
            String Author = versionInfoElem.getAttributeValue("Author");
            String Date = versionInfoElem.getAttributeValue("Date");
            String Remarks = versionInfoElem.getAttributeValue("Remarks");
            VersionInfo versionInfo = new VersionInfo(Organization, Version, Author, Date, Remarks);
            return versionInfo;
        } else {
            System.err.println("Invalid element input in getVersionInfo()");
            if (this.console != null) {
                console.setText("Invalid element input in getVersionInfo()");
            }
            return null;
        }
    }

    protected FBCompiler getFBCompiler(Element compilerElem) {
        if (compilerElem != null) {
            String Language = compilerElem.getAttributeValue("Language");
            String Vendor = compilerElem.getAttributeValue("Vendor");
            String Product = compilerElem.getAttributeValue("Product");
            String Version = compilerElem.getAttributeValue("Version");
            FBCompiler fbCompiler = new FBCompiler(Language, Vendor, Product, Version);
            return fbCompiler;
        } else {
            System.err.println("Invalid element input in getFBCompiler()");
            if (this.console != null) {
                console.setText("Invalid element input in getFBCompiler()");
            }
            return null;
        }
    }

    protected CompilerInfo getCompilerInfo(Element compilerInfoElem) {
        if (compilerInfoElem != null && compilerInfoElem.getName().equals("CompilerInfo")) {
            List content = compilerInfoElem.getChildren();
            int size = content.size();
            String header = compilerInfoElem.getAttributeValue("header");
            String classdef = compilerInfoElem.getAttributeValue("classdef");
            Hashtable<Integer, FBCompiler> hashTable = new Hashtable<Integer, FBCompiler>(size);
            int count = 0;
            CompilerInfo compilerInfo;
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBCompiler fbCompiler = getFBCompiler(currentElement);
                    hashTable.put(new Integer(count), fbCompiler);
                    count++;
                }
                compilerInfo = new CompilerInfo(header, classdef, hashTable);
            } else {
                compilerInfo = new CompilerInfo(header, classdef, null);
            }
            return compilerInfo;
        } else {
            System.err.println("Invalid element input in getCompilerInfo()");
            if (this.console != null) {
                console.setText("Invalid element input in getCompilerInfo()");
            }
            return null;
        }
    }

    protected With getWith(Element withElem) {
        if (withElem != null) {
            String Var = withElem.getAttributeValue("Var");
            With with = new With(Var);
            return with;
        } else {
            System.err.println("Invalid element input in getWith()");
            if (this.console != null) {
                console.setText("Invalid element input in getWith()");
            }
            return null;
        }
    }

    protected FBEvent getEvent(Element eventElem) {
        if (eventElem != null && eventElem.getName().equals("Event")) {
            List content = eventElem.getChildren();
            int size = content.size();
            String Name = eventElem.getAttributeValue("Name");
            String Type = eventElem.getAttributeValue("Type");
            String Comment = eventElem.getAttributeValue("Comment");
            Hashtable<Integer, With> hashTable = new Hashtable<Integer, With>(size);
            FBEvent event;
            if (size > 0) {
                Iterator iterator = content.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    With with = getWith(currentElement);
                    hashTable.put(new Integer(count), with);
                    count++;
                }
                event = new FBEvent(Name, Type, Comment, hashTable);
            } else {
                event = new FBEvent(Name, Type, Comment, null);
            }
            return event;
        } else {
            System.err.println("Invalid element input in getEvent()");
            if (this.console != null) {
                console.setText("Invalid element input in getEvent()");
            }
            return null;
        }
    }

    protected FBEventInputs getEventInputs(Element eventInputsElem) {
        if (eventInputsElem != null && eventInputsElem.getName().equals("EventInputs")) {
            List content = eventInputsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, FBEvent> hashTable = new Hashtable<String, FBEvent>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBEvent event = getEvent(currentElement);
                    hashTable.put(event.getName(), event);
                }
                FBEventInputs eventInputs = new FBEventInputs(hashTable);
                return eventInputs;
            } else {
                System.err.println("Invalid child element size in getEventInputs()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getEventInputs()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getEventInputs()");
            if (this.console != null) {
                console.setText("Invalid element input in getEventInputs()");
            }
            return null;
        }
    }

    protected FBEventOutputs getEventOutputs(Element eventOutputsElem) {
        if (eventOutputsElem != null && eventOutputsElem.getName().equals("EventOutputs")) {
            List content = eventOutputsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, FBEvent> hashTable = new Hashtable<String, FBEvent>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBEvent event = getEvent(currentElement);
                    hashTable.put(event.getName(), event);
                }
                FBEventOutputs eventOutputs = new FBEventOutputs(hashTable);
                return eventOutputs;
            } else {
                System.err.println("Invalid child element size in getEventOutputs()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getEventOutputs()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getEventOutputs()");
            if (this.console != null) {
                console.setText("Invalid element input in getEventOutputs()");
            }
            return null;
        }
    }

    protected FBVarDeclaration getVarDeclaration(Element varDeclarationElem) {
        if (varDeclarationElem != null) {
            String Name = varDeclarationElem.getAttributeValue("Name");
            String Type = varDeclarationElem.getAttributeValue("Type");
            String ArraySize = varDeclarationElem.getAttributeValue("ArraySize");
            String InitialValue = varDeclarationElem.getAttributeValue("InitialValue");
            String Comment = varDeclarationElem.getAttributeValue("Comment");
            FBVarDeclaration varDeclaration = new FBVarDeclaration(Name, Type, ArraySize, InitialValue, Comment);
            return varDeclaration;
        } else {
            System.err.println("Invalid element input in getVarDeclaration()");
            if (this.console != null) {
                console.setText("Invalid element input in getVarDeclaration()");
            }
            return null;
        }
    }

    protected FBInputVars getInputVars(Element inputVarsElem) {
        if (inputVarsElem != null && inputVarsElem.getName().equals("InputVars")) {
            List content = inputVarsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, FBVarDeclaration> hashTable = new Hashtable<String, FBVarDeclaration>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBVarDeclaration varDeclaration = getVarDeclaration(currentElement);
                    hashTable.put(varDeclaration.getName(), varDeclaration);
                }
                FBInputVars inputVars = new FBInputVars(hashTable);
                return inputVars;
            } else {
                System.err.println("Invalid child element size in getInputVars()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getInputVars()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getInputVars()");
            if (this.console != null) {
                console.setText("Invalid element input in getInputVars()");
            }
            return null;
        }
    }

    protected FBOutputVars getOutputVars(Element outputVarsElem) {
        if (outputVarsElem != null && outputVarsElem.getName().equals("OutputVars")) {
            List content = outputVarsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, FBVarDeclaration> hashTable = new Hashtable<String, FBVarDeclaration>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBVarDeclaration varDeclaration = getVarDeclaration(currentElement);
                    hashTable.put(varDeclaration.getName(), varDeclaration);
                }
                FBOutputVars outputVars = new FBOutputVars(hashTable);
                return outputVars;
            } else {
                System.err.println("Invalid child element size in getOutputVars()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getOutputVars()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getOutputVars()");
            if (this.console != null) {
                console.setText("Invalid element input in getOutputVars()");
            }
            return null;
        }
    }

    protected Parameter getParameter(Element parameterElem) {
        if (parameterElem != null) {
            String Name = parameterElem.getAttributeValue("Name");
            String Value = parameterElem.getAttributeValue("Value");
            String Comment = parameterElem.getAttributeValue("Comment");
            Parameter parameter = new Parameter(Name, Value, Comment);
            return parameter;
        } else {
            System.err.println("Invalid element input in getParameter()");
            if (this.console != null) {
                console.setText("Invalid element input in getParameter()");
            }
            return null;
        }
    }

    protected AdapterDeclaration getAdapterDeclaration(Element adapterDeclarationElem) {
        if (adapterDeclarationElem != null && adapterDeclarationElem.getName().equals("Parameter")) {
            List content = adapterDeclarationElem.getChildren();
            int size = content.size();
            String Name = adapterDeclarationElem.getAttributeValue("Name");
            String Type = adapterDeclarationElem.getAttributeValue("Type");
            String Comment = adapterDeclarationElem.getAttributeValue("Comment");
            Hashtable<String, Parameter> hashTable = new Hashtable<String, Parameter>(size);
            AdapterDeclaration adapterDeclaration;
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Parameter parameter = getParameter(currentElement);
                    hashTable.put(Name, parameter);
                }
                adapterDeclaration = new AdapterDeclaration(Name, Type, Comment, hashTable);
            } else {
                adapterDeclaration = new AdapterDeclaration(Name, Type, Comment, null);
            }
            return adapterDeclaration;
        } else {
            System.err.println("Invalid element input in getAdapterDeclaration()");
            if (this.console != null) {
                console.setText("Invalid element input in getAdapterDeclaration()");
            }
            return null;
        }
    }

    protected Sockets getSockets(Element socketsElem) {
        if (socketsElem != null && socketsElem.getName().equals("AdapterDeclaration")) {
            List content = socketsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, AdapterDeclaration> hashTable = new Hashtable<String, AdapterDeclaration>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    AdapterDeclaration adapterDeclaration = getAdapterDeclaration(currentElement);
                    hashTable.put(adapterDeclaration.getName(), adapterDeclaration);
                }
                Sockets sockets = new Sockets(hashTable);
                return sockets;
            } else {
                System.err.println("Invalid child element size in getSockets()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getSockets()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getSockets()");
            if (this.console != null) {
                console.setText("Invalid element input in getSockets()");
            }
            return null;
        }
    }

    protected Plugs getPlugs(Element plugsElem) {
        if (plugsElem != null && plugsElem.getName().equals("AdapterDeclaration")) {
            List content = plugsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, AdapterDeclaration> hashTable = new Hashtable<String, AdapterDeclaration>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    AdapterDeclaration adapterDeclaration = getAdapterDeclaration(currentElement);
                    hashTable.put(adapterDeclaration.getName(), adapterDeclaration);
                }
                Plugs plugs = new Plugs(hashTable);
                return plugs;
            } else {
                System.err.println("Invalid child element size in getPlugs()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getPlugs()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getPlugs()");
            if (this.console != null) {
                console.setText("Invalid element input in getPlugs()");
            }
            return null;
        }
    }

    protected FBInterfaceList getInterfaceList(Element interfaceListElem) {
        if (interfaceListElem != null) {
            Element eventInputsElem = interfaceListElem.getChild("EventInputs");
            Element eventOutputsElem = interfaceListElem.getChild("EventOutputs");
            Element inputVarsElem = interfaceListElem.getChild("InputVars");
            Element outputVarsElem = interfaceListElem.getChild("OutputVars");
            Element socketsElem = interfaceListElem.getChild("Sockets");
            Element plugsElem = interfaceListElem.getChild("Plugs");
            FBEventInputs eventInputs = null;
            FBEventOutputs eventOutputs = null;
            FBInputVars inputVars = null;
            FBOutputVars outputVars = null;
            Sockets sockets = null;
            Plugs plugs = null;
            if (eventInputsElem != null) eventInputs = getEventInputs(eventInputsElem);
            if (eventOutputsElem != null) eventOutputs = getEventOutputs(eventOutputsElem);
            if (inputVarsElem != null) inputVars = getInputVars(inputVarsElem);
            if (outputVarsElem != null) outputVars = getOutputVars(outputVarsElem);
            if (socketsElem != null) sockets = getSockets(socketsElem);
            if (plugsElem != null) plugs = getPlugs(plugsElem);
            return new FBInterfaceList(eventInputs, eventOutputs, inputVars, outputVars, sockets, plugs);
        } else {
            System.err.println("Invalid element input in getInterfaceList()");
            if (this.console != null) {
                console.setText("Invalid element input in getInterfaceList()");
            }
            return null;
        }
    }

    protected InternalVars getInternalVars(Element internalVarsElem) {
        if (internalVarsElem != null && internalVarsElem.getName().equals("InternalVars")) {
            List content = internalVarsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, FBVarDeclaration> hashTable = new Hashtable<String, FBVarDeclaration>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBVarDeclaration varDeclaration = getVarDeclaration(currentElement);
                    hashTable.put(varDeclaration.getName(), varDeclaration);
                }
                InternalVars internalVars = new InternalVars(hashTable);
                return internalVars;
            } else {
                System.err.println("Invalid child element size in getInternalVars()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getInternalVars()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getInternalVars()");
            System.exit(0);
            if (this.console != null) {
                console.setText("Invalid element input in getInternalVars()");
            }
            return null;
        }
    }

    protected ECAction getECAction(Element ecActionElem) {
        if (ecActionElem != null) {
            String Algorithm = ecActionElem.getAttributeValue("Algorithm");
            String Output = ecActionElem.getAttributeValue("Output");
            ECAction ecAction = new ECAction(Algorithm, Output);
            return ecAction;
        } else {
            System.err.println("Invalid element input in getECAction()");
            if (this.console != null) {
                console.setText("Invalid element input in getECAction()");
            }
            return null;
        }
    }

    protected ECState getECState(Element ecStateElem) {
        if (ecStateElem != null && ecStateElem.getName().equals("ECState")) {
            List content = ecStateElem.getChildren();
            int size = content.size();
            String Name = ecStateElem.getAttributeValue("Name");
            String Comment = ecStateElem.getAttributeValue("Comment");
            String x = ecStateElem.getAttributeValue("x");
            String y = ecStateElem.getAttributeValue("y");
            Hashtable<Integer, ECAction> hashTable = new Hashtable<Integer, ECAction>(size);
            ECState ecState;
            if (size > 0) {
                Iterator iterator = content.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    ECAction ecAction = getECAction(currentElement);
                    hashTable.put(new Integer(count), ecAction);
                    count++;
                }
                ecState = new ECState(Name, Comment, x, y, hashTable);
            } else {
                ecState = new ECState(Name, Comment, x, y, null);
            }
            return ecState;
        } else {
            System.err.println("Invalid element input in getECState()");
            if (this.console != null) {
                console.setText("Invalid element input in getECState()");
            }
            return null;
        }
    }

    protected ECTransition getECTransition(Element ecTransitionElem) {
        if (ecTransitionElem != null) {
            String Source = ecTransitionElem.getAttributeValue("Source");
            String Destination = ecTransitionElem.getAttributeValue("Destination");
            String Condition = ecTransitionElem.getAttributeValue("Condition");
            String Comment = ecTransitionElem.getAttributeValue("Comment");
            String x = ecTransitionElem.getAttributeValue("x");
            String y = ecTransitionElem.getAttributeValue("y");
            ECTransition ecTransition = new ECTransition(Source, Destination, Condition, Comment, x, y);
            return ecTransition;
        } else {
            System.err.println("Invalid element input in getECTransition()");
            if (this.console != null) {
                console.setText("Invalid element input in getECTransition()");
            }
            return null;
        }
    }

    protected ECC getECC(Element eccElem) {
        if (eccElem != null && eccElem.getName().equals("ECC")) {
            List ecStateContent = eccElem.getChildren("ECState");
            List ecTransitionContent = eccElem.getChildren("ECTransition");
            int ecStateSize = ecStateContent.size();
            int ecTransitionSize = ecTransitionContent.size();
            if (ecStateSize > 0 && ecTransitionSize > 0) {
                Hashtable<String, ECState> ecStateHashTable = new Hashtable<String, ECState>(ecStateSize);
                Hashtable<String, ECTransition> ecTransitionHashTable = new Hashtable<String, ECTransition>(ecTransitionSize);
                Iterator ecStateIterator = ecStateContent.iterator();
                Iterator ecTransitionIterator = ecTransitionContent.iterator();
                while (ecStateIterator.hasNext()) {
                    Element currentElement = (Element) ecStateIterator.next();
                    ECState ecState = getECState(currentElement);
                    ecStateHashTable.put(ecState.getName(), ecState);
                }
                while (ecTransitionIterator.hasNext()) {
                    Element currentElement = (Element) ecTransitionIterator.next();
                    ECTransition ecTransition = getECTransition(currentElement);
                    ecTransitionHashTable.put(ecTransition.getSource() + ecTransition.getDestination() + ecTransition.getCondition(), ecTransition);
                }
                ECC ecc = new ECC(ecStateHashTable, ecTransitionHashTable);
                return ecc;
            } else {
                System.err.println("Invalid child element size in getECC()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getECC()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getECC()");
            if (this.console != null) {
                console.setText("Invalid element input in getECC()");
            }
            return null;
        }
    }

    protected FB getFB(Element fbElem) {
        if (fbElem != null && fbElem.getName().equals("FB")) {
            List content = fbElem.getChildren();
            int size = content.size();
            String Name = fbElem.getAttributeValue("Name");
            String Type = fbElem.getAttributeValue("Type");
            String Comment = fbElem.getAttributeValue("Comment");
            String x = fbElem.getAttributeValue("x");
            String y = fbElem.getAttributeValue("y");
            Hashtable<String, Parameter> hashTable = new Hashtable<String, Parameter>(size);
            FB fb;
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Parameter parameter = getParameter(currentElement);
                    hashTable.put(Name, parameter);
                }
                fb = new FB(Name, Type, Comment, x, y, hashTable);
            } else {
                fb = new FB(Name, Type, Comment, x, y, null);
            }
            return fb;
        } else {
            System.err.println("Invalid element input in getFB()");
            if (this.console != null) {
                console.setText("Invalid element input in getFB()");
            }
            return null;
        }
    }

    protected Connection getConnection(Element connectionELem) {
        if (connectionELem != null) {
            String Source = connectionELem.getAttributeValue("Source");
            String Destination = connectionELem.getAttributeValue("Destination");
            String Comment = connectionELem.getAttributeValue("Comment");
            String dx1 = connectionELem.getAttributeValue("dx1");
            String dx2 = connectionELem.getAttributeValue("dx2");
            String dy = connectionELem.getAttributeValue("dy");
            Connection connection = new Connection(Source, Destination, Comment, dx1, dx2, dy);
            return connection;
        } else {
            System.err.println("Invalid element input in getECTransition()");
            if (this.console != null) {
                console.setText("Invalid element input in getECTransition()");
            }
            return null;
        }
    }

    protected DataConnections getDataConnections(Element dataConnectionsElem) {
        if (dataConnectionsElem != null && dataConnectionsElem.getName().equals("DataConnections")) {
            List content = dataConnectionsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, Connection> hashTable = new Hashtable<String, Connection>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Connection connection = getConnection(currentElement);
                    hashTable.put(connection.getSource() + connection.getDestination(), connection);
                }
                DataConnections dataConnections = new DataConnections(hashTable);
                return dataConnections;
            } else {
                System.err.println("Invalid child element size in getDataConnections()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getDataConnections()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getDataConnections()");
            if (this.console != null) {
                console.setText("Invalid element input in getDataConnections()");
            }
            return null;
        }
    }

    protected FBD getFBD(Element fbdElem) {
        if (fbdElem != null && fbdElem.getName().equals("FBD")) {
            DataConnections dataConnections = getDataConnections(fbdElem.getChild("DataConnections"));
            List content = fbdElem.getChildren("FB");
            int size = content.size();
            if (size > 0) {
                Hashtable<String, FB> hashTable = new Hashtable<String, FB>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FB fb = getFB(currentElement);
                    hashTable.put(fb.getName(), fb);
                }
                FBD fbd = new FBD(hashTable, dataConnections);
                return fbd;
            } else {
                System.err.println("Invalid child element size in getFBD()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getFBD()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getFBD()");
            if (this.console != null) {
                console.setText("Invalid element input in getFBD()");
            }
            return null;
        }
    }

    protected ST getST(Element stElem) {
        if (stElem != null) {
            String Text = stElem.getAttributeValue("Text");
            ST st = new ST(Text);
            return st;
        } else {
            System.err.println("Invalid element input in getST()");
            if (this.console != null) {
                console.setText("Invalid element input in getST()");
            }
            return null;
        }
    }

    protected Rung getRung(Element rungElem) {
        if (rungElem != null) {
            String Output = rungElem.getAttributeValue("Output");
            String Expression = rungElem.getAttributeValue("Expression");
            String Comment = rungElem.getAttributeValue("Comment");
            Rung rung = new Rung(Output, Expression, Comment);
            return rung;
        } else {
            System.err.println("Invalid element input in getRung()");
            if (this.console != null) {
                console.setText("Invalid element input in getRung()");
            }
            return null;
        }
    }

    protected LD getLD(Element ldElem) {
        if (ldElem != null && ldElem.getName().equals("LD")) {
            List content = ldElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<Integer, Rung> hashTable = new Hashtable<Integer, Rung>(size);
                Iterator iterator = content.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Rung rung = getRung(currentElement);
                    hashTable.put(new Integer(count), rung);
                    count++;
                }
                LD ld = new LD(hashTable);
                return ld;
            } else {
                System.err.println("Invalid child element size in getLD()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getLD()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getLD()");
            if (this.console != null) {
                console.setText("Invalid element input in getLD()");
            }
            return null;
        }
    }

    protected Other getOther(Element otherElem) {
        if (otherElem != null) {
            String Language = otherElem.getAttributeValue("Language");
            String Text = otherElem.getAttributeValue("Text");
            Other other = new Other(Language, Text);
            return other;
        } else {
            System.err.println("Invalid element input in getOther()");
            if (this.console != null) {
                console.setText("Invalid element input in getOther()");
            }
            return null;
        }
    }

    protected Algorithm getAlgorithm(Element algorithmElem) {
        if (algorithmElem != null && algorithmElem.getName().equals("Algorithm")) {
            String Name = algorithmElem.getAttributeValue("Name");
            String Comment = algorithmElem.getAttributeValue("Comment");
            Element fbd = algorithmElem.getChild("FBD");
            Element st = algorithmElem.getChild("ST");
            Element ld = algorithmElem.getChild("LD");
            Element other = algorithmElem.getChild("Other");
            Algorithm algorithm = null;
            if (fbd != null) {
                algorithm = new Algorithm(Name, getFBD(fbd), Comment);
            }
            if (st != null) {
                algorithm = new Algorithm(Name, getST(st), Comment);
            }
            if (ld != null) {
                algorithm = new Algorithm(Name, getLD(ld), Comment);
            }
            if (other != null) {
                algorithm = new Algorithm(Name, getOther(other), Comment);
            }
            return algorithm;
        } else {
            System.err.println("Invalid element input in getAlgorithm()");
            if (this.console != null) {
                console.setText("Invalid element input in getAlgorithm()");
            }
            return null;
        }
    }

    protected BasicFB getBasicFB(Element basicFBElem) {
        if (basicFBElem != null && basicFBElem.getName().equals("BasicFB")) {
            Element internalVarsElem = basicFBElem.getChild("InternalVars");
            Element eccElem = basicFBElem.getChild("ECC");
            List content = basicFBElem.getChildren("Algorithm");
            int size = content.size();
            InternalVars internalVars = null;
            ECC ecc = null;
            if (internalVarsElem != null) {
                internalVars = getInternalVars(internalVarsElem);
            }
            if (eccElem != null) {
                ecc = getECC(eccElem);
            }
            BasicFB basicFB = null;
            Hashtable<String, Algorithm> hashTable = new Hashtable<String, Algorithm>(size);
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Algorithm algorithm = getAlgorithm(currentElement);
                    hashTable.put(algorithm.getName(), algorithm);
                }
                basicFB = new BasicFB(internalVars, ecc, hashTable);
            } else {
                basicFB = new BasicFB(internalVars, ecc, null);
            }
            return basicFB;
        } else {
            System.err.println("Invalid element input in getBasicFB()");
            if (this.console != null) {
                console.setText("Invalid element input in getBasicFB()");
            }
            return null;
        }
    }

    protected EventConnections getEventConnections(Element eventConnectionsElem) {
        if (eventConnectionsElem != null && eventConnectionsElem.getName().equals("EventConnections")) {
            List content = eventConnectionsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, Connection> hashTable = new Hashtable<String, Connection>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Connection connection = getConnection(currentElement);
                    hashTable.put(connection.getSource() + connection.getDestination(), connection);
                }
                EventConnections eventConnections = new EventConnections(hashTable);
                return eventConnections;
            } else {
                System.err.println("Invalid child element size in getEventConnections()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getEventConnections()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getEventConnections()");
            if (this.console != null) {
                console.setText("Invalid element input in getEventConnections()");
            }
            return null;
        }
    }

    protected AdapterConnections getAdapterConnections(Element adapterConnectionsElem) {
        if (adapterConnectionsElem != null && adapterConnectionsElem.getName().equals("AdapterConnections")) {
            List content = adapterConnectionsElem.getChildren();
            int size = content.size();
            if (size > 0) {
                Hashtable<String, Connection> hashTable = new Hashtable<String, Connection>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Connection connection = getConnection(currentElement);
                    hashTable.put(connection.getSource() + connection.getDestination(), connection);
                }
                AdapterConnections adapterConnections = new AdapterConnections(hashTable);
                return adapterConnections;
            } else {
                System.err.println("Invalid child element size in getAdapterConnections()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getAdapterConnections()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getAdapterConnections()");
            if (this.console != null) {
                console.setText("Invalid element input in getAdapterConnections()");
            }
            return null;
        }
    }

    protected FBNetwork getFBNetwork(Element fbNetworkElem) {
        if (fbNetworkElem != null && fbNetworkElem.getName().equals("FBNetwork")) {
            Element eventConnectionsElem = fbNetworkElem.getChild("EventConnections");
            Element dataConnectionsElem = fbNetworkElem.getChild("DataConnections");
            Element adapterConnectionsElem = fbNetworkElem.getChild("AdapterConnections");
            List content = fbNetworkElem.getChildren("FB");
            int size = content.size();
            EventConnections eventConnections = null;
            DataConnections dataConnections = null;
            AdapterConnections adapterConnections = null;
            if (eventConnectionsElem != null) {
                eventConnections = getEventConnections(eventConnectionsElem);
            }
            if (dataConnectionsElem != null) {
                dataConnections = getDataConnections(dataConnectionsElem);
            }
            if (adapterConnectionsElem != null) {
                adapterConnections = getAdapterConnections(adapterConnectionsElem);
            }
            FBNetwork fbNetwork = null;
            Hashtable<String, FB> hashTable = new Hashtable<String, FB>(size);
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FB fb = getFB(currentElement);
                    hashTable.put(fb.getName(), fb);
                }
                fbNetwork = new FBNetwork(hashTable, eventConnections, dataConnections, adapterConnections);
            } else {
                fbNetwork = new FBNetwork(null, eventConnections, dataConnections, adapterConnections);
            }
            return fbNetwork;
        } else {
            System.err.println("Invalid element input in getFBNetwork()");
            if (this.console != null) {
                console.setText("Invalid element input in getFBNetwork()");
            }
            return null;
        }
    }

    protected InputPrimitive getInputPrimitive(Element inputPrimitiveElem) {
        if (inputPrimitiveElem != null && inputPrimitiveElem.getName().equals("InputPrimitive")) {
            String Interface = inputPrimitiveElem.getAttributeValue("Interface");
            String Event = inputPrimitiveElem.getAttributeValue("Event");
            String Parameters = inputPrimitiveElem.getAttributeValue("Parameters");
            InputPrimitive inputPrimitive = new InputPrimitive(Interface, Event, Parameters);
            return inputPrimitive;
        } else {
            System.err.println("Invalid element input in getInputPrimitive()");
            if (this.console != null) {
                console.setText("Invalid element input in getInputPrimitive()");
            }
            return null;
        }
    }

    protected OutputPrimitive getOutputPrimitive(Element outputPrimitiveElem) {
        if (outputPrimitiveElem != null && outputPrimitiveElem.getName().equals("OutputPrimitive")) {
            String Interface = outputPrimitiveElem.getAttributeValue("Interface");
            String Event = outputPrimitiveElem.getAttributeValue("Event");
            String Parameters = outputPrimitiveElem.getAttributeValue("Parameters");
            OutputPrimitive outputPrimitive = new OutputPrimitive(Interface, Event, Parameters);
            return outputPrimitive;
        } else {
            System.err.println("Invalid element input in getOutputPrimitive()");
            if (this.console != null) {
                console.setText("Invalid element input in getOutputPrimitive()");
            }
            return null;
        }
    }

    protected ServiceTransaction getServiceTransaction(Element serviceTransactionElem) {
        if (serviceTransactionElem != null && serviceTransactionElem.getName().equals("ServiceTransaction")) {
            Element inputPrimitiveElem = serviceTransactionElem.getChild("InputPrimitive");
            List content = serviceTransactionElem.getChildren("OutputPrimitive");
            int size = content.size();
            InputPrimitive inputPrimitive = null;
            if (inputPrimitiveElem != null) {
                inputPrimitive = getInputPrimitive(inputPrimitiveElem);
            }
            ServiceTransaction serviceTransaction = null;
            Hashtable<Integer, OutputPrimitive> hashTable = new Hashtable<Integer, OutputPrimitive>(size);
            int count = 0;
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    OutputPrimitive outputPrimitive = getOutputPrimitive(currentElement);
                    hashTable.put(new Integer(count), outputPrimitive);
                    count++;
                }
                serviceTransaction = new ServiceTransaction(inputPrimitive, hashTable);
            } else {
                serviceTransaction = new ServiceTransaction(inputPrimitive, null);
            }
            return serviceTransaction;
        } else {
            System.err.println("Invalid element input in getServiceTransaction()");
            if (this.console != null) {
                console.setText("Invalid element input in getServiceTransaction()");
            }
            return null;
        }
    }

    protected ServiceSequence getServiceSequence(Element serviceSequenceElem) {
        if (serviceSequenceElem != null && serviceSequenceElem.getName().equals("ServiceSequence")) {
            List content = serviceSequenceElem.getChildren();
            int size = content.size();
            String Name = serviceSequenceElem.getAttributeValue("Name");
            String Comment = serviceSequenceElem.getAttributeValue("Comment");
            Hashtable<Integer, ServiceTransaction> hashTable = new Hashtable<Integer, ServiceTransaction>(size);
            ServiceSequence serviceSequence;
            int count = 0;
            if (size > 0) {
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    ServiceTransaction ServiceTransaction = getServiceTransaction(currentElement);
                    hashTable.put(new Integer(count), ServiceTransaction);
                    count++;
                }
                serviceSequence = new ServiceSequence(Name, Comment, hashTable);
            } else {
                serviceSequence = new ServiceSequence(Name, Comment, null);
            }
            return serviceSequence;
        } else {
            System.err.println("Invalid element input in getServiceSequence()");
            if (this.console != null) {
                console.setText("Invalid element input in getServiceSequence()");
            }
            return null;
        }
    }

    protected Service getService(Element serviceElem) {
        if (serviceElem != null && serviceElem.getName().equals("Service")) {
            List content = serviceElem.getChildren();
            int size = content.size();
            String RightInterface = serviceElem.getAttributeValue("RightInterface");
            String LeftInterface = serviceElem.getAttributeValue("LeftInterface");
            String Comment = serviceElem.getAttributeValue("Comment");
            if (size > 0) {
                Hashtable<String, ServiceSequence> hashTable = new Hashtable<String, ServiceSequence>(size);
                Iterator iterator = content.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    ServiceSequence serviceSequence = getServiceSequence(currentElement);
                    hashTable.put(serviceSequence.getName(), serviceSequence);
                }
                Service service = new Service(RightInterface, LeftInterface, Comment, hashTable);
                return service;
            } else {
                System.err.println("Invalid child element size in getService()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getService()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getService()");
            if (this.console != null) {
                console.setText("Invalid element input in getService()");
            }
            return null;
        }
    }

    public String getModelType() {
        return getModelType(xmlDocument.getRootElement());
    }

    public String getModelType(Element rootElem) {
        if (rootElem != null) {
            if (rootElem.getName().equals("FBType")) {
                modelType = "FBType";
                return modelType;
            } else if (rootElem.getName().equals("System")) {
                modelType = "System";
                return modelType;
            } else {
                System.err.println("Input rootElem doesn't contain any valid element");
                if (this.console != null) {
                    console.setText("Input rootElem doesn't contain any valid element");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getModelType()");
            if (this.console != null) {
                console.setText("Invalid element input in getModelType()");
            }
            return null;
        }
    }

    protected Application getApplication(Element applicationElem) {
        if (applicationElem != null && applicationElem.getName().equals("Application")) {
            String Name = applicationElem.getAttributeValue("Name");
            String Comment = applicationElem.getAttributeValue("Comment");
            Element fbNetworkElem = applicationElem.getChild("FBNetwork");
            FBNetwork fbNetwork = getFBNetwork(fbNetworkElem);
            Application application = new Application(Name, Comment, fbNetwork);
            return application;
        } else {
            System.err.println("Invalid element input in serviceElem()");
            if (this.console != null) {
                console.setText("Invalid element input in serviceElem()");
            }
            return null;
        }
    }

    public FBSystem getFBSystem() {
        return getFBSystem(xmlDocument.getRootElement());
    }

    public FBSystem getFBSystem(Element fbSystemElem) {
        if (fbSystemElem != null && fbSystemElem.getName().equals("System")) {
            List versionInfoList = fbSystemElem.getChildren("VersionInfo");
            int versionInfoListSize = versionInfoList.size();
            List applicationList = fbSystemElem.getChildren("Application");
            int applicationListSize = applicationList.size();
            List deviceList = fbSystemElem.getChildren("Device");
            int deviceListSize = deviceList.size();
            List resourceList = fbSystemElem.getChildren("Resource");
            int resourcesListSize = resourceList.size();
            String Name = fbSystemElem.getAttributeValue("Name");
            String Comment = fbSystemElem.getAttributeValue("Comment");
            Element identificationElem = fbSystemElem.getChild("Identification");
            Identification identification = null;
            if (versionInfoListSize > 0) {
                Hashtable<Integer, VersionInfo> versionInfos = new Hashtable<Integer, VersionInfo>(versionInfoListSize);
                Iterator iterator = versionInfoList.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    VersionInfo versionInfo = getVersionInfo(currentElement);
                    versionInfos.put(new Integer(count), versionInfo);
                    count++;
                }
                if (identificationElem != null) {
                    identification = getIdentification(identificationElem);
                }
                Hashtable<String, Application> applications = null;
                if (applicationListSize > 0) {
                    applications = new Hashtable<String, Application>(applicationListSize);
                    iterator = applicationList.iterator();
                    while (iterator.hasNext()) {
                        Element currentElement = (Element) iterator.next();
                        Application application = getApplication(currentElement);
                        applications.put(application.getName(), application);
                    }
                }
                Hashtable<String, FBDevice> devices = null;
                if (deviceListSize > 0) {
                    devices = new Hashtable<String, FBDevice>(deviceListSize);
                    iterator = deviceList.iterator();
                    while (iterator.hasNext()) {
                        Element currentElement = (Element) iterator.next();
                        FBDevice device = getDevice(currentElement);
                        devices.put(device.getName(), device);
                    }
                }
                FBSystem fbSystem = new FBSystem(Name, Comment, identification, versionInfos, applications, devices);
                return fbSystem;
            } else {
                System.err.println("Invalid child element size in getFBSystem()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getFBSystem()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getFBSystem()");
            if (this.console != null) {
                console.setText("Invalid element input in getFBSystem()");
            }
            return null;
        }
    }

    private FBDevice getDevice(Element deviceElem) {
        if (deviceElem != null && deviceElem.getName().equals("Device")) {
            String Name = deviceElem.getAttributeValue("Name");
            String Comment = deviceElem.getAttributeValue("Comment");
            String Type = deviceElem.getAttributeValue("Type");
            String X = deviceElem.getAttributeValue("x");
            String Y = deviceElem.getAttributeValue("y");
            Hashtable<String, Parameter> Parameters = null;
            List parameterList = deviceElem.getChildren("Parameter");
            int parameterListSize = parameterList.size();
            Hashtable<String, FBResource> Resources = null;
            List resourceList = deviceElem.getChildren("Resource");
            int resourceListSize = resourceList.size();
            if (parameterListSize > 0) {
                Parameters = new Hashtable<String, Parameter>();
                Iterator iterator = parameterList.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Parameter parameter = getParameter(currentElement);
                    Parameters.put(parameter.getName(), parameter);
                }
            }
            if (resourceListSize > 0) {
                Resources = new Hashtable<String, FBResource>();
                Iterator iterator = resourceList.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    FBResource resource = getResource(currentElement);
                    Resources.put(resource.getName(), resource);
                }
            }
            FBNetwork fbNetwork = null;
            Element fbNetworkElem = deviceElem.getChild("FBNetwork");
            if (fbNetworkElem != null) {
                fbNetwork = getFBNetwork(fbNetworkElem);
            }
            FBDevice device = new FBDevice(Name, Comment, Type, X, Y, Parameters, Resources, fbNetwork);
            return device;
        }
        return null;
    }

    private FBResource getResource(Element resourceElem) {
        if (resourceElem != null && resourceElem.getName().equals("Resource")) {
            String Name = resourceElem.getAttributeValue("Name");
            String Type = resourceElem.getAttributeValue("Type");
            String Comment = resourceElem.getAttributeValue("Comment");
            String X = resourceElem.getAttributeValue("x");
            String Y = resourceElem.getAttributeValue("y");
            Hashtable<String, Parameter> Parameters = null;
            List parameterList = resourceElem.getChildren("Parameter");
            int parameterListSize = parameterList.size();
            if (parameterListSize > 0) {
                Parameters = new Hashtable<String, Parameter>();
                Iterator iterator = parameterList.iterator();
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    Parameter parameter = getParameter(currentElement);
                    Parameters.put(parameter.getName(), parameter);
                }
            }
            FBNetwork fbNetwork = null;
            Element fbNetworkElem = resourceElem.getChild("FBNetwork");
            if (fbNetworkElem != null) {
                fbNetwork = getFBNetwork(fbNetworkElem);
            }
            FBResource resource = new FBResource(Name, Comment, Type, X, Y, Parameters, fbNetwork);
            return resource;
        }
        return null;
    }

    public FBDKFBType getFBDKFBType() {
        return getFBDKFBType(xmlDocument.getRootElement());
    }

    public FBDKFBType getFBDKFBType(Element fbdkFBTypeElem) {
        if (fbdkFBTypeElem != null && fbdkFBTypeElem.getName().equals("FBType")) {
            String Name = fbdkFBTypeElem.getAttributeValue("Name");
            String Comment = fbdkFBTypeElem.getAttributeValue("Comment");
            Element identificationElem = fbdkFBTypeElem.getChild("Identification");
            Element compilerInfoElem = fbdkFBTypeElem.getChild("CompilerInfo");
            Element basicFBElem = fbdkFBTypeElem.getChild("BasicFB");
            Element fbNetworkElem = fbdkFBTypeElem.getChild("FBNetwork");
            Element serviceElem = fbdkFBTypeElem.getChild("Service");
            Identification identification = null;
            CompilerInfo compilerInfo = null;
            FBInterfaceList interfaceList = getInterfaceList(fbdkFBTypeElem.getChild("InterfaceList"));
            BasicFB basicFB = null;
            FBNetwork fbNetwork = null;
            Service service = null;
            FBDKFBType fbdkFBType = null;
            List content = fbdkFBTypeElem.getChildren("VersionInfo");
            int size = content.size();
            if (size > 0) {
                Hashtable<Integer, VersionInfo> hashTable = new Hashtable<Integer, VersionInfo>(size);
                Iterator iterator = content.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Element currentElement = (Element) iterator.next();
                    VersionInfo versionInfo = getVersionInfo(currentElement);
                    hashTable.put(new Integer(count), versionInfo);
                    count++;
                }
                if (identificationElem != null) {
                    identification = getIdentification(identificationElem);
                }
                if (compilerInfoElem != null) {
                    compilerInfo = getCompilerInfo(compilerInfoElem);
                }
                if (serviceElem != null) {
                    service = getService(serviceElem);
                }
                if (basicFBElem != null) {
                    basicFB = getBasicFB(basicFBElem);
                    fbdkFBType = new FBDKFBType(Name, Comment, identification, hashTable, compilerInfo, interfaceList, basicFB, service);
                } else if (fbNetworkElem != null) {
                    fbNetwork = getFBNetwork(fbNetworkElem);
                    fbdkFBType = new FBDKFBType(Name, Comment, identification, hashTable, compilerInfo, interfaceList, fbNetwork, service);
                } else if (service != null) {
                    fbdkFBType = new FBDKFBType(Name, Comment, identification, hashTable, compilerInfo, interfaceList, service);
                } else {
                    fbdkFBType = new FBDKFBType(Name, Comment, identification, hashTable, compilerInfo, interfaceList);
                }
                return fbdkFBType;
            } else {
                System.err.println("Invalid child element size in getFBDKFBType()");
                if (this.console != null) {
                    console.setText("Invalid child element size in getFBDKFBType()");
                }
                return null;
            }
        } else {
            System.err.println("Invalid element input in getFBDKFBType()");
            if (this.console != null) {
                console.setText("Invalid element input in getFBDKFBType()");
            }
            return null;
        }
    }

    public Document postProcess(Document xmlDocument) {
        DocType docType = new DocType("FBType", "http://www.holobloc.com/xml/LibraryElement.dtd");
        xmlDocument.setDocType(docType);
        return xmlDocument;
    }

    public static void main(String[] args) {
        try {
            String newDTD = "src/dtd/LibraryElement.dtd";
            String path = "src/fbt/BasicFB.fbt";
            String sysPath = "src/fbt/FLASHER_TESTR.sys";
            FBXMLParser sysXml = new FBXMLParser(sysPath, newDTD, true, null);
            Element sysRoot = sysXml.getXmlDocument().getRootElement();
            Element applicationElem = sysRoot.getChild("Application");
            System.out.println(sysXml.getModelType());
            ParserLib.writeToScreen(sysXml.getFBSystem(sysRoot).toXML());
            System.exit(0);
            FBXMLParser xml = new FBXMLParser(path, newDTD, true, null);
            System.out.println(xml.getXmlDocumentString());
            Element root = xml.getXmlDocument().getRootElement();
            Element eventElem = root.getChild("InterfaceList").getChild("EventInputs").getChild("Event");
            ParserLib.writeToScreen(eventElem);
            System.out.println("event's Name: " + eventElem.getName());
            FBEvent event = xml.getEvent(eventElem);
            FBInterfaceList interfaceList = xml.getInterfaceList(root.getChild("InterfaceList"));
            Element fbTypeElem = root;
            FBDKFBType fbdkFBType = xml.getFBDKFBType(fbTypeElem);
            Document document = new Document();
            document.setRootElement(fbdkFBType.toXML());
            ParserLib.writeToScreen(xml.postProcess(document));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
