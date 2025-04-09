import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.regex.*;

public class javam {

    private static final String JAVA_ENDING = ".java";

    private static final String CLASS_ENDING = ".class";

    private static final String DEF_EXCLUDE_FILE = "exclude.xml";

    private static final String DEF_DEPENDENCY_FILE = "jmakefile.xml";

    private static final String EXCLUDE_NODE = "ex";

    private static final int MAX_ITERATIONS = 20;

    static final Pattern errorPattern = Pattern.compile("\\d+ error");

    static final Pattern fieldPattern = Pattern.compile("<Field [^\\s]+ L([^;\\s]+);");

    static final Pattern methodPattern = Pattern.compile("<Method (\\S+)\\.(?:[^\\s\\.]+)\\(");

    static final Pattern methodPattern2 = Pattern.compile("<Method \\S+\\((L[^\\s]+)\\)");

    static final Pattern internalMethodPattern = Pattern.compile("L([^\\s;]+);");

    static final Pattern classPattern = Pattern.compile("<Class (\\S+)>");

    static ArrayList excludedClasses;

    static String excludeFileName = null;

    static String dependenceFileName = null;

    static Document xmlDepDoc;

    static Document xmlExcDoc;

    static boolean useExcludeFile = false;

    static boolean useDependenceFile = false;

    static boolean forceRecompile = false;

    static boolean updateDependencyFile = false;

    static boolean useReflection = false;

    static Element rootElement;

    static boolean dependencyChanged = false;

    static boolean useDefaultExcludes = true;

    static int errors = 0;

    static int filesCompiled = 0;

    static int dependencyChanges;

    static ArrayList notFoundClasses;

    static ArrayList compiledClasses;

    static ArrayList compilerOptions;

    public javam() {
        init();
    }

    private void init() {
        excludedClasses = new ArrayList();
        if (useDefaultExcludes) {
            excludedClasses.add("java.");
            excludedClasses.add("javax.");
            excludedClasses.add("org.");
            excludedClasses.add("sun.");
        }
        excludedClasses.add("[L");
        excludedClasses.add("[B");
        excludedClasses.add("[I");
        excludedClasses.add("[C");
        excludedClasses.add("void");
        excludedClasses.add("int");
        excludedClasses.add("String");
        excludedClasses.add("boolean");
        excludedClasses.add("double");
        excludedClasses.add("long");
        excludedClasses.add("byte");
        excludedClasses.add("char");
        excludedClasses.add("float");
        excludedClasses.add("short");
        DocumentBuilderFactory dbf;
        DocumentBuilder db = null;
        if (useExcludeFile) {
            try {
                dbf = DocumentBuilderFactory.newInstance();
                db = dbf.newDocumentBuilder();
                xmlExcDoc = db.parse(new File(excludeFileName));
            } catch (IOException fnfe) {
                System.err.println("Cannot open exclude file.");
                System.exit(-1);
            } catch (SAXException se) {
                se.printStackTrace();
                System.exit(-1);
            } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
                System.exit(-1);
            }
            if (xmlExcDoc != null) {
                Element rootNode = xmlExcDoc.getDocumentElement();
                NodeList nodeList = rootNode.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node tmpNode = (Node) nodeList.item(i);
                    if (tmpNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element tmpElement = (Element) tmpNode;
                        String nodeName = tmpElement.getNodeName();
                        if (nodeName.equals(EXCLUDE_NODE)) {
                            String exValue = tmpElement.getAttribute("class");
                            if (exValue.endsWith("*")) exValue = exValue.substring(0, exValue.length() - 1);
                            excludedClasses.add(exValue);
                        }
                    }
                }
            }
            dbf = null;
            db = null;
            xmlExcDoc = null;
        }
        if (useDependenceFile) {
            try {
                dbf = DocumentBuilderFactory.newInstance();
                db = dbf.newDocumentBuilder();
                xmlDepDoc = db.parse(new File(dependenceFileName));
                rootElement = xmlDepDoc.getDocumentElement();
            } catch (FileNotFoundException fnfe) {
                if (!updateDependencyFile) {
                    System.err.println("Cannot locate dependency file.");
                    System.exit(-1);
                } else {
                    xmlDepDoc = db.newDocument();
                    rootElement = xmlDepDoc.createElement("dependency");
                    xmlDepDoc.appendChild(rootElement);
                }
            } catch (IOException ioe) {
                System.err.println("Error openning dependency file.");
                System.exit(-1);
            } catch (SAXException se) {
                se.printStackTrace();
                System.exit(-1);
            } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private void writeDepFile() {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(xmlDepDoc);
            StreamResult result = new StreamResult(new FileOutputStream(dependenceFileName));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException tce) {
            System.err.println("* Transformer Factory error");
            System.err.println("  " + tce.getMessage());
            Throwable x = tce;
            if (tce.getException() != null) x = tce.getException();
            x.printStackTrace();
        } catch (TransformerException te) {
            System.err.println("* Transformation error");
            System.err.println("  " + te.getMessage());
            Throwable x = te;
            if (te.getException() != null) x = te.getException();
            x.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Cannot create dependency file.");
        }
    }

    private Element getElementForClass(String className) {
        NodeList nList = rootElement.getElementsByTagName("class");
        for (int i = 0; i < nList.getLength(); i++) {
            if (((Element) nList.item(i)).getAttribute("name").equals(className)) return (Element) nList.item(i);
        }
        return null;
    }

    private Element getDependentElementForClass(Element parentElement, String childClassName) {
        NodeList nList = parentElement.getElementsByTagName("depClass");
        for (int i = 0; i < nList.getLength(); i++) {
            if (((Element) nList.item(i)).getAttribute("name").equals(childClassName)) return (Element) nList.item(i);
        }
        return null;
    }

    private boolean instNode(Element parentElement, String childClassName) {
        if (useDependenceFile && (parentElement != null) && (!exclude(childClassName))) {
            Element tmpElement = getDependentElementForClass(parentElement, childClassName);
            if (tmpElement == null) {
                tmpElement = xmlDepDoc.createElement("depClass");
                tmpElement.setAttribute("name", childClassName);
                parentElement.appendChild(tmpElement);
                dependencyChanges++;
                dependencyChanged = true;
            }
            return true;
        }
        return false;
    }

    public ArrayList createClassList(String rootClass, ArrayList currentClassList) {
        if (exclude(rootClass)) return currentClassList;
        excludedClasses.add(rootClass);
        int index;
        Element thisElement = null;
        if (useDependenceFile) thisElement = getElementForClass(rootClass);
        if ((thisElement == null) && (useDependenceFile)) {
            thisElement = xmlDepDoc.createElement("class");
            thisElement.setAttribute("name", rootClass);
            rootElement.appendChild(thisElement);
            dependencyChanged = true;
        } else {
            if (useDependenceFile) {
                NodeList nList = thisElement.getElementsByTagName("depClass");
                int tmpLen = nList.getLength();
                for (int i = 0; i < tmpLen; i++) {
                    Element tmpElement = (Element) nList.item(i);
                    String depClass = tmpElement.getAttribute("name");
                    index = currentClassList.indexOf(depClass);
                    if (index == -1) {
                        currentClassList = createClassList(depClass, currentClassList);
                    }
                }
            }
        }
        if (useReflection) {
            Class thisClass = null;
            try {
                thisClass = Class.forName(rootClass);
            } catch (ClassNotFoundException cnfe) {
            } catch (NoClassDefFoundError ncdfe) {
            }
            if (thisClass != null) {
                Constructor[] cons;
                try {
                    cons = thisClass.getDeclaredConstructors();
                    for (int i = 0; i < cons.length; i++) {
                        Class[] constructorParams = cons[i].getParameterTypes();
                        for (int j = 0; j < constructorParams.length; j++) {
                            instNode(thisElement, constructorParams[j].getName());
                            index = currentClassList.indexOf(constructorParams[j].getName());
                            if (index == -1) {
                                currentClassList = createClassList(constructorParams[j].getName(), currentClassList);
                            }
                        }
                    }
                } catch (NoClassDefFoundError nce) {
                    String message = nce.getMessage();
                    int pos1 = message.lastIndexOf(" ");
                    String problemClassName = message.substring(pos1 + 1).replace('/', '.');
                    index = currentClassList.indexOf(problemClassName);
                    if (index == -1) {
                        currentClassList = createClassList(problemClassName, currentClassList);
                    }
                    instNode(thisElement, problemClassName);
                }
                Method[] methods;
                try {
                    methods = thisClass.getDeclaredMethods();
                    for (int i = 0; i < methods.length; i++) {
                        Class[] params = methods[i].getParameterTypes();
                        for (int j = 0; j < params.length; j++) {
                            instNode(thisElement, params[j].getName());
                            index = currentClassList.indexOf(params[j].getName());
                            if (index == -1) {
                                currentClassList = createClassList(params[j].getName(), currentClassList);
                            }
                        }
                        params = methods[i].getExceptionTypes();
                        for (int j = 0; j < params.length; j++) {
                            instNode(thisElement, params[j].getName());
                            index = currentClassList.indexOf(params[j].getName());
                            if (index == -1) {
                                currentClassList = createClassList(params[j].getName(), currentClassList);
                            }
                        }
                        Class fieldType = methods[i].getReturnType();
                        instNode(thisElement, fieldType.getName());
                        index = currentClassList.indexOf(fieldType.getName());
                        if (index == -1) {
                            currentClassList = createClassList(fieldType.getName(), currentClassList);
                        }
                    }
                } catch (NoClassDefFoundError nce) {
                    String message = nce.getMessage();
                    int pos1 = message.lastIndexOf(" ");
                    String problemClassName = message.substring(pos1 + 1).replace('/', '.');
                    index = currentClassList.indexOf(problemClassName);
                    if (index == -1) {
                        currentClassList = createClassList(problemClassName, currentClassList);
                    }
                    instNode(thisElement, problemClassName);
                }
                Field[] fields;
                try {
                    fields = thisClass.getFields();
                    for (int i = 0; i < fields.length; i++) {
                        Class fieldType = fields[i].getType();
                        instNode(thisElement, fieldType.getName());
                        index = currentClassList.indexOf(fieldType.getName());
                        if (index == -1) {
                            currentClassList = createClassList(fieldType.getName(), currentClassList);
                        }
                    }
                } catch (NoClassDefFoundError nce) {
                    String message = nce.getMessage();
                    int pos1 = message.lastIndexOf(" ");
                    String problemClassName = message.substring(pos1 + 1).replace('/', '.');
                    index = currentClassList.indexOf(problemClassName);
                    if (index == -1) {
                        currentClassList = createClassList(problemClassName, currentClassList);
                    }
                    instNode(thisElement, problemClassName);
                }
                Class[] interfaces;
                try {
                    interfaces = thisClass.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++) {
                        instNode(thisElement, interfaces[i].getName());
                        index = currentClassList.indexOf(interfaces[i].getName());
                        if (index == -1) {
                            currentClassList = createClassList(interfaces[i].getName(), currentClassList);
                        }
                    }
                } catch (NoClassDefFoundError nce) {
                    String message = nce.getMessage();
                    int pos1 = message.lastIndexOf(" ");
                    String problemClassName = message.substring(pos1 + 1).replace('/', '.');
                    index = currentClassList.indexOf(problemClassName);
                    if (index == -1) {
                        currentClassList = createClassList(problemClassName, currentClassList);
                    }
                    instNode(thisElement, problemClassName);
                }
                Class superClass;
                try {
                    superClass = thisClass.getSuperclass();
                    if (superClass != null) {
                        instNode(thisElement, superClass.getName());
                        index = currentClassList.indexOf(superClass.getName());
                        if (index == -1) {
                            currentClassList = createClassList(superClass.getName(), currentClassList);
                        }
                    }
                } catch (NoClassDefFoundError nce) {
                    String message = nce.getMessage();
                    int pos1 = message.lastIndexOf(" ");
                    String problemClassName = message.substring(pos1 + 1).replace('/', '.');
                    index = currentClassList.indexOf(problemClassName);
                    if (index == -1) {
                        currentClassList = createClassList(problemClassName, currentClassList);
                    }
                    instNode(thisElement, problemClassName);
                }
                try {
                    Runtime rt = Runtime.getRuntime();
                    String classPath = System.getProperty("java.class.path");
                    String command = "javap -c -b -classpath " + classPath + " " + rootClass;
                    Process p = rt.exec(command);
                    BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String outLine;
                    String codeString = new String();
                    while ((outLine = out.readLine()) != null) {
                        Matcher m = fieldPattern.matcher(outLine);
                        while (m.find()) {
                            String fieldClassName = (m.group(1)).replace('/', '.');
                            instNode(thisElement, fieldClassName);
                            index = currentClassList.indexOf(fieldClassName);
                            if (index == -1) {
                                currentClassList = createClassList(fieldClassName, currentClassList);
                            }
                        }
                        m = methodPattern.matcher(outLine);
                        while (m.find()) {
                            String fieldClassName = m.group(1);
                            instNode(thisElement, fieldClassName);
                            index = currentClassList.indexOf(fieldClassName);
                            if (index == -1) {
                                currentClassList = createClassList(fieldClassName, currentClassList);
                            }
                        }
                        m = methodPattern2.matcher(outLine);
                        while (m.find()) {
                            String methodParameters = m.group(1);
                            Matcher m2 = internalMethodPattern.matcher(methodParameters);
                            while (m2.find()) {
                                String fieldClassName = (m2.group(1)).replace('/', '.');
                                instNode(thisElement, fieldClassName);
                                index = currentClassList.indexOf(fieldClassName);
                                if (index == -1) {
                                    currentClassList = createClassList(fieldClassName, currentClassList);
                                }
                            }
                        }
                        m = classPattern.matcher(outLine);
                        while (m.find()) {
                            String fieldClassName = m.group(1);
                            instNode(thisElement, fieldClassName);
                            index = currentClassList.indexOf(fieldClassName);
                            if (index == -1) {
                                currentClassList = createClassList(fieldClassName, currentClassList);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        index = currentClassList.indexOf(rootClass);
        if (index == -1) {
            currentClassList.add(rootClass);
        }
        excludedClasses.remove(rootClass);
        return currentClassList;
    }

    private boolean exclude(String className) {
        try {
            Class tmpClass = Class.forName(className);
            if (tmpClass.isPrimitive()) {
                return true;
            }
        } catch (ClassNotFoundException cnfe) {
        } catch (NoClassDefFoundError ncdfe) {
        }
        for (int i = 0; i < excludedClasses.size(); i++) {
            String curTest = (String) excludedClasses.get(i);
            if (className.startsWith(curTest)) return true;
        }
        return false;
    }

    public void compileList(ArrayList classList) {
        int classListSize = classList.size();
        ArrayList classNames = new ArrayList();
        for (int i = 0; i < classListSize; i++) {
            classNames.add(((Class) classList.get(i)).getName());
        }
        classList.clear();
        Runtime.getRuntime().gc();
        compileNameList(classNames);
    }

    public String getClassFile(String className) {
        String fullClassFileName;
        String classPath = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        ArrayList classPathDirs = new ArrayList();
        while (st.hasMoreTokens()) {
            classPathDirs.add(st.nextToken());
        }
        int pathSize = classPathDirs.size();
        String classFileName = File.separator + className.replace('.', File.separatorChar) + CLASS_ENDING;
        int j = 0;
        while (j < pathSize) {
            String curPath = (String) classPathDirs.get(j);
            fullClassFileName = curPath.concat(classFileName);
            File classFile = new File(fullClassFileName);
            if (classFile.exists()) {
                return fullClassFileName;
            }
        }
        return null;
    }

    public void compileNameList(ArrayList classNames) {
        String classPath = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        ArrayList classPathDirs = new ArrayList();
        while (st.hasMoreTokens()) {
            classPathDirs.add(st.nextToken());
        }
        int pathSize = classPathDirs.size();
        int classListSize = classNames.size();
        for (int i = 0; i < classListSize; i++) {
            String className = (String) classNames.get(i);
            String javaFileName = File.separator + className.replace('.', File.separatorChar) + JAVA_ENDING;
            String classFileName = File.separator + className.replace('.', File.separatorChar) + CLASS_ENDING;
            int j = 0;
            boolean found = false;
            String fullJavaFileName = "";
            while ((!found) && (j < pathSize)) {
                String curPath = (String) classPathDirs.get(j);
                fullJavaFileName = curPath.concat(javaFileName);
                String fullClassFileName = curPath.concat(classFileName);
                File javaFile = new File(fullJavaFileName);
                File classFile = new File(fullClassFileName);
                if (javaFile.exists()) {
                    found = true;
                    boolean compile = true;
                    if (classFile.exists()) {
                        if (javaFile.lastModified() <= classFile.lastModified()) compile = false; else classFile.delete();
                    }
                    if ((compile || forceRecompile) && (!compiledClasses.contains(className))) {
                        String command = "javac.exe -classpath " + classPath + " ";
                        for (int k = 0; k < compilerOptions.size(); k++) {
                            String curOption = (String) compilerOptions.get(k);
                            command = (command.concat(curOption)).concat(" ");
                        }
                        command = command.concat(fullJavaFileName);
                        Runtime rt = Runtime.getRuntime();
                        System.out.print("compiling " + className);
                        try {
                            Process p = rt.exec(command);
                            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                            BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            boolean startOut = true;
                            String errorLine;
                            String outLine;
                            Matcher m;
                            while ((errorLine = error.readLine()) != null) {
                                if (startOut) {
                                    System.err.println();
                                    startOut = false;
                                }
                                m = errorPattern.matcher(errorLine);
                                if (!m.lookingAt()) System.err.println(errorLine);
                            }
                            startOut = true;
                            while ((outLine = stdOut.readLine()) != null) {
                                if (startOut) {
                                    System.err.println();
                                    startOut = false;
                                }
                                System.out.println(outLine);
                            }
                            int exitValue = p.waitFor();
                            if (exitValue != 0) {
                                errors++;
                                return;
                            } else {
                                filesCompiled++;
                                compiledClasses.add(className);
                                System.out.println(" (done)");
                            }
                        } catch (Exception e) {
                            System.err.println(e.getMessage() + " [command could not be completed]");
                            errors++;
                            filesCompiled--;
                            return;
                        }
                    }
                }
                j++;
            }
            if ((!found) && (!notFoundClasses.contains(className))) {
                System.err.println("could not locate file for " + className);
                notFoundClasses.add(className);
                errors++;
            }
        }
    }

    private static void printHelp() {
        System.out.println("JAVAM Version 1.0 alpha, created by Lee McCauley (t-mccauley@memphis.edu)");
        System.out.println();
        System.out.println("Usage: javam [options] Complete_Class_Name");
        System.out.println("    Options:");
        System.out.println("    -x <excludeFile>        use named exclude file.");
        System.out.println("    -xd                     do not use default excluded classes for");
        System.out.println("                            JDK 1.4.0 (java.*;javax.*;org.*;sun.*).");
        System.out.println("                            Primitive classes will always be excluded.");
        System.out.println("                            This should be used with an additional");
        System.out.println("                            exclude file.");
        System.out.println("    -f <dependenceFile>     use named dependence file");
        System.out.println("    -classpath <classpath>  use the specified classpath for");
        System.out.println("                            compilation.");
        System.out.println("    -r                      force recomplile of all classes.");
        System.out.println("    -u                      update/create dependence file");
        System.out.println("                            based on java reflection of classes.");
        System.out.println("    -auto                   Use reflection to determine dependencies.");
        System.out.println("                            This slows down compilation but can be used");
        System.out.println("                            with the -u option to generate or update a");
        System.out.println("                            dependency file.");
        System.out.println("                            file or if no dependence file is available.");
        System.out.println("    -C<compilerOption>      Passes through the string 'compilerOption' to");
        System.out.println("                            the compiler.");
        System.out.println("    -h or -?                display this help text and exit.");
        System.out.println();
        System.out.println("Requirements: javac.exe and javap.exe are in the path.");
        System.out.println();
    }

    public static void main(String[] args) {
        String curDir = System.getProperty("user.dir");
        Class rootClass = null;
        compilerOptions = new ArrayList();
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-x")) {
                excludeFileName = args[++i];
                useExcludeFile = true;
            }
            if (args[i].equals("-f")) {
                dependenceFileName = args[++i];
                useDependenceFile = true;
            }
            if ((args[i].equals("-h")) || (args[i].equals("-?"))) {
                printHelp();
                System.exit(0);
            }
            if (args[i].equals("-r")) {
                forceRecompile = true;
            }
            if (args[i].equals("-u")) {
                updateDependencyFile = true;
            }
            if (args[i].equals("-xd")) {
                useDefaultExcludes = false;
            }
            if (args[i].equals("-auto")) {
                useReflection = true;
            }
            if (args[i].equals("-classpath")) {
                System.setProperty("java.class.path", args[++i]);
            }
            if (args[i].startsWith("-C")) {
                compilerOptions.add(args[i].substring(2));
            }
        }
        if (excludeFileName == null) {
            excludeFileName = curDir.concat(File.separator + DEF_EXCLUDE_FILE);
            if ((new File(excludeFileName)).exists()) useExcludeFile = true;
        }
        if (dependenceFileName == null) {
            dependenceFileName = curDir.concat(File.separator + DEF_DEPENDENCY_FILE);
            if ((new File(dependenceFileName)).exists() || updateDependencyFile) useDependenceFile = true;
        }
        javam jm = new javam();
        ArrayList classList = new ArrayList();
        dependencyChanges = 1;
        int iterations = 0;
        notFoundClasses = new ArrayList();
        compiledClasses = new ArrayList();
        while ((dependencyChanges > 0) && (iterations < MAX_ITERATIONS)) {
            dependencyChanges = 0;
            classList = jm.createClassList(args[args.length - 1], classList);
            jm.compileNameList(classList);
            classList.clear();
            iterations++;
        }
        if (updateDependencyFile && dependencyChanged) jm.writeDepFile();
        System.out.println();
        System.out.println(new String("Total Errors: ").concat(Integer.toString(errors)));
        System.out.println(new String("Files Compiled: ").concat(Integer.toString(filesCompiled)));
    }
}
