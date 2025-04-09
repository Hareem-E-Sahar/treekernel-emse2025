package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.duniptech.soa.service.CompileService;
import com.duniptech.soa.service.CompileServiceInterface;
import com.duniptech.soa.service.RTSimulationService;
import com.duniptech.soa.service.RTSimulationServiceInterface;
import com.duniptech.soa.service.SimulationService;
import com.duniptech.soa.service.SimulationServiceInterface;
import com.duniptech.soa.service.UploadService;
import com.duniptech.soa.service.UploadServiceInterface;
import com.duniptech.soa.stub.version.VersionStub;
import com.duniptech.soa.util.Util;

public class TestSoaServices {

    private String directory;

    private String xmlFilePath;

    private String packageName;

    private String mainServer;

    private HashSet<String> hosts;

    private HashSet<String> fileNames;

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[3];
            args[0] = "D:/jlrisco/TrabajoExtra/Efp/src";
            args[1] = "D:/jlrisco/TrabajoExtra/Efp/lib/gpt.xml";
            args[2] = "jlrisco";
        }
        try {
            TestSoaServices test = new TestSoaServices(args);
            VersionStub version = new VersionStub();
            System.out.println(version.getVersion().get_return());
            test.testUpload();
            test.testCompile(test.packageName);
            test.testSimulate(test.packageName);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public TestSoaServices(String[] args) throws Exception {
        directory = args[0];
        xmlFilePath = args[1];
        packageName = args[2] + System.currentTimeMillis();
        hosts = new HashSet<String>();
        fileNames = new HashSet<String>();
        this.readXmlFile();
    }

    public void testUpload() throws Exception {
        ArrayList<byte[]> fileContents = new ArrayList<byte[]>();
        ArrayList<String> fileNamesAsArray = new ArrayList<String>();
        File file = null;
        FileInputStream fileStream = null;
        for (String fileName : fileNames) {
            file = new File(directory + File.separator + fileName);
            fileStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileStream.read(bytes);
            fileContents.add(bytes);
            fileNamesAsArray.add(fileName);
        }
        for (String host : hosts) {
            UploadServiceInterface service = new UploadService(host + Util.UPLOAD_SERVICE);
            service.upload(packageName, toByteArray(fileContents), toByteArray(fileNamesAsArray));
        }
    }

    public void testCompile(String serverPackage) throws Exception {
        ArrayList<String> fileNamesAsArray = new ArrayList<String>();
        for (String fileName : fileNames) fileNamesAsArray.add(fileName);
        for (String host : hosts) {
            System.out.println("Compile model at " + host + ": ");
            CompileServiceInterface service = new CompileService(host + Util.COMPILE_SERVICE);
            service.compile(serverPackage, toByteArray(fileNamesAsArray));
            System.out.println("DONE.");
        }
    }

    public void testSimulate(String serverPackage) throws Exception {
        SimulationServiceInterface service = new SimulationService(mainServer + Util.SIMULATION_SERVICE);
        Document xmlDevsSoaModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFilePath));
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(xmlDevsSoaModel), result);
        String xmlModelAsString = writer.toString();
        String response = service.simulate(InetAddress.getLocalHost().getHostAddress(), serverPackage, xmlModelAsString, 1000);
        System.out.println(response);
    }

    public void testSimulateRT(String serverPackage, double timeInSeconds) throws Exception {
        RTSimulationServiceInterface service = new RTSimulationService(mainServer + Util.RTSIMULATION_SERVICE);
        Document xmlDevsSoaModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFilePath));
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(xmlDevsSoaModel), result);
        String xmlModelAsString = writer.toString();
        String response = service.observe(InetAddress.getLocalHost().getHostAddress(), serverPackage, xmlModelAsString, timeInSeconds);
        System.out.println(response);
    }

    private byte[] toByteArray(Object obj) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(o);
            out.writeObject(obj);
            out.flush();
            out.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return o.toByteArray();
    }

    private void readXmlFile() throws Exception {
        Document xmlDevsSoaModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFilePath));
        Element xmlModel = (Element) xmlDevsSoaModel.getElementsByTagName("coupled").item(0);
        mainServer = xmlModel.getAttribute("host");
        hosts.add(mainServer);
        NodeList xmlComponentList = xmlModel.getElementsByTagName("coupled");
        for (int i = 0; i < xmlComponentList.getLength(); ++i) {
            Element xmlComponent = (Element) xmlComponentList.item(i);
            String host = xmlComponent.getAttribute("host");
            hosts.add(host);
            String javaFileName = xmlComponent.getAttribute("class") + ".java";
            fileNames.add(javaFileName);
        }
        xmlComponentList = xmlModel.getElementsByTagName("atomic");
        for (int i = 0; i < xmlComponentList.getLength(); ++i) {
            Element xmlComponent = (Element) xmlComponentList.item(i);
            String host = xmlComponent.getAttribute("host");
            hosts.add(host);
            String javaFileName = xmlComponent.getAttribute("class") + ".java";
            fileNames.add(javaFileName);
        }
        NodeList xmlPortList = xmlModel.getElementsByTagName("inport");
        for (int i = 0; i < xmlPortList.getLength(); ++i) {
            Element xmlPort = (Element) xmlPortList.item(i);
            String javaFileName = xmlPort.getAttribute("class") + ".java";
            fileNames.add(javaFileName);
        }
        xmlPortList = xmlModel.getElementsByTagName("outport");
        for (int i = 0; i < xmlPortList.getLength(); ++i) {
            Element xmlPort = (Element) xmlPortList.item(i);
            String javaFileName = xmlPort.getAttribute("class") + ".java";
            fileNames.add(javaFileName);
        }
    }

    public void test() throws Exception {
        Document xmlDevsSoaModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFilePath));
        Element xmlModel = (Element) xmlDevsSoaModel.getElementsByTagName("coupled").item(0);
        NodeList xmlComponentList = xmlModel.getChildNodes();
        for (int i = 0; i < xmlComponentList.getLength(); ++i) {
            Node xmlComponentAsNode = (Node) xmlComponentList.item(i);
            if (xmlComponentAsNode.getNodeType() == Node.ELEMENT_NODE) {
                Element xmlComponent = (Element) xmlComponentList.item(i);
                if (xmlComponent.getNodeName().equals("coupled")) {
                    StringWriter writer = new StringWriter();
                    Result result = new StreamResult(writer);
                    TransformerFactory.newInstance().newTransformer().transform(new DOMSource(xmlComponent), result);
                    String xmlComponentAsString = writer.toString();
                    System.out.println(xmlComponentAsString);
                    System.out.println(xmlComponent.getAttribute("name"));
                }
            }
        }
    }
}
