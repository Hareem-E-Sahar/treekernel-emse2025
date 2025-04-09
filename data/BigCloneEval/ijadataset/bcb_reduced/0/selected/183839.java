package de.iteratec.visio.model;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.xml.sax.SAXException;
import de.iteratec.visio.model.exceptions.MasterNotFoundException;
import de.iteratec.visio.model.exceptions.NoSuchElementException;

public class InformationFlowShapeGenerationTest extends TestCase {

    private static final String VISIO_TEMPLATE_NAME = "VisioInformationFlowTemplate.vdx";

    private Document createDocument() throws IOException, ParserConfigurationException, SAXException {
        InputStream input = ClassLoader.getSystemResourceAsStream(VISIO_TEMPLATE_NAME);
        Document document = DocumentFactory.getInstance().loadDocument(input);
        input.close();
        return document;
    }

    public void testShapeGeneration() throws FileNotFoundException, IOException, NoSuchElementException, MasterNotFoundException, ParserConfigurationException, SAXException {
        Document document = createDocument();
        Page page = document.getPage(0);
        Shape shape = page.createNewShape("Application");
        assertEquals("We expect one shape on the page", 1, page.getShapes().length);
        shape.setPosition(3.00, 4.00);
        double pinX = shape.getPinX();
        double pinY = shape.getPinY();
        assertEquals("PinX wrong", 3.00, pinX, 0.01);
        assertEquals("PinY wrong", 4.00, pinY, 0.01);
        shape.setAngle(0.5);
        shape.setSize(2.00, 2.50);
        double width = shape.getWidth();
        double height = shape.getHeight();
        assertEquals("Width wrong", 2.00, width, 0.01);
        assertEquals("Height wrong", 2.50, height, 0.01);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("BusinessObject", "My BO 123");
        shape.setCustomProperties(props);
        Master master = document.getMaster("Application");
        assertEquals("We expect the shape to have the requested master", master.getID(), shape.getMaster().getID());
        Shape[] masterShapes = master.getShapes();
        assertEquals(1, masterShapes.length);
        Shape top = masterShapes[0];
        top.getPinX();
        top.getPinY();
    }

    public void testShapeGenerationAndWrite() throws IOException, NoSuchElementException, MasterNotFoundException, ParserConfigurationException, SAXException {
        Document document = createDocument();
        Page page = document.getPage(0);
        Shape shape = page.createNewShape("Application");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("ApplicationName", "App XY");
        props.put("ApplicationVersion", "Version 1.0");
        props.put("BusinessObjects", "My BO 123 THIS IS INTENDED TO BE REALLY LARGE TO FORCE A RESCALE..." + " BUT YOU NEED A LOT OF CHARACTERS FOR THAT.");
        shape.setCustomProperties(props);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        document.write(byteOut);
        String xml = byteOut.toString();
        byteOut.close();
        assertTrue(xml.contains("App XY"));
        assertTrue(xml.contains("Version 1.0"));
        assertTrue(xml.contains("My BO 123 THIS IS INTENDED TO BE REALLY LARGE TO FORCE A RESCALE..."));
        String fileName = "testresult/InformationFlowShapeGenerationTest_testShapeGenerationAndWrite.vdx";
        writeToFileAndOpen(document, fileName);
    }

    public void testConnectTwoAppShapesWithConnectorAndWrite() throws IOException, NoSuchElementException, MasterNotFoundException, ParserConfigurationException, SAXException {
        Document document = createDocument();
        Page page = document.getPage(0);
        Shape shape1 = page.createNewShape("Application");
        Map<String, Object> props1 = new HashMap<String, Object>();
        props1.put("ApplicationName", "App XY");
        props1.put("ApplicationVersion", "Version 1.0");
        props1.put("BusinessObjects", "My BO 123 THIS IS INTENDED TO BE REALLY LARGE TO FORCE A RESCALE..." + " BUT YOU NEED A LOT OF CHARACTERS FOR THAT.");
        shape1.setCustomProperties(props1);
        shape1.setPosition(1.0, 2.0);
        shape1.setSize(1.0, 2.0);
        Shape shape2 = page.createNewShape("Application");
        Map<String, Object> props2 = new HashMap<String, Object>();
        props2.put("ApplicationName", "App Z");
        props2.put("ApplicationVersion", "Version 2.5");
        props2.put("BusinessObjects", "SIMPLE BO, ANOTHER BO");
        shape2.setCustomProperties(props2);
        shape2.setPosition(1.0, 5.0);
        shape2.setSize(1.0, 2.0);
        Shape connector = page.createNewConnector("InformationFlowBidirectional", shape1, shape2);
        connector.insertText("My connector text");
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        document.write(byteOut);
        String xml = byteOut.toString();
        byteOut.close();
        assertTrue(xml.contains("App XY"));
        assertTrue(xml.contains("Version 1.0"));
        assertTrue(xml.contains("My BO 123 THIS IS INTENDED TO BE REALLY LARGE TO FORCE A RESCALE..."));
        assertTrue(xml.contains("App Z"));
        assertTrue(xml.contains("Version 2.5"));
        assertTrue(xml.contains("SIMPLE BO, ANOTHER BO"));
        assertTrue(xml.contains("My connector text"));
        String fileName = "testresult/InformationFlowShapeGenerationTest_testConnectTwoAppShapesWithConnectorAndWrite.vdx";
        writeToFileAndOpen(document, fileName);
    }

    private void writeToFileAndOpen(Document document, String fileName) throws IOException {
        File outFile = new File(fileName);
        outFile.getParentFile().mkdirs();
        document.save(outFile);
        if (Desktop.isDesktopSupported()) {
            if (Desktop.getDesktop().isSupported(Action.OPEN)) {
                try {
                    Desktop.getDesktop().open(outFile);
                } catch (IOException e) {
                }
            }
        }
    }
}
