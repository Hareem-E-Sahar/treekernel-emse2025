package com.rapidminer.io.community;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.rapidminer.Process;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.flow.ProcessRenderer;
import com.rapidminer.io.Base64;
import com.rapidminer.io.process.XMLImporter;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.XMLException;

/**
 * 
 * @author Simon Fischer
 *
 */
public class MyExperimentWorkflowConverter {

    private static final String WORKFLOW_CHARSET = "UTF-8";

    public String convertProcessToWorkflow(Process process, String title, String description, String tags, License license, SharingPermission sharingPermission) throws ParserConfigurationException, XMLException, IOException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element workflowElem = doc.createElement("workflow");
        doc.appendChild(workflowElem);
        XMLTools.setTagContents(workflowElem, "title", title);
        if ((description == null) || description.isEmpty()) {
            description = process.getRootOperator().getUserDescription();
        }
        if ((description != null) && !description.isEmpty()) {
            XMLTools.setTagContents(workflowElem, "description", description);
        }
        XMLTools.setTagContents(workflowElem, "license-type", license.getId());
        XMLTools.setTagContents(workflowElem, "content-type", "application/vnd.rapidminer.rmp+zip");
        Element contentElement = doc.createElement("content");
        workflowElem.appendChild(contentElement);
        contentElement.setAttribute("encoding", "base64");
        contentElement.setAttribute("type", "binary");
        String base64Content = Base64.encodeBytes(makeZip(process, tags, title));
        contentElement.appendChild(doc.createTextNode(base64Content));
        try {
            String base64Preview = Base64.encodeBytes(makePreviewImage(process));
            Element previewElement = doc.createElement("preview");
            workflowElem.appendChild(previewElement);
            previewElement.setAttribute("encoding", "base64");
            previewElement.setAttribute("type", "binary");
            previewElement.appendChild(doc.createTextNode(base64Preview));
        } catch (IOException e) {
            LogService.getRoot().log(Level.WARNING, "Error encoding preview image: " + e, e);
        }
        Element permissionsElem = doc.createElement("permissions");
        workflowElem.appendChild(permissionsElem);
        Element permissionElem = doc.createElement("permission");
        permissionsElem.appendChild(permissionElem);
        switch(sharingPermission) {
            case PRIVATE:
                break;
            case PUBLIC_VIEW:
                XMLTools.setTagContents(permissionElem, "category", "public");
                Element privilege = doc.createElement("privilege");
                privilege.setAttribute("type", "view");
                permissionElem.appendChild(privilege);
                break;
            case PUBLIC_DOWNLOAD:
                XMLTools.setTagContents(permissionElem, "category", "public");
                privilege = doc.createElement("privilege");
                privilege.setAttribute("type", "view");
                permissionElem.appendChild(privilege);
                privilege = doc.createElement("privilege");
                privilege.setAttribute("type", "download");
                permissionElem.appendChild(privilege);
                break;
        }
        return XMLTools.toString(doc, Charset.forName(WORKFLOW_CHARSET));
    }

    private byte[] makeZip(Process process, String tags, String title) throws IOException, XMLException, ParserConfigurationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        streamZip(process, tags, title, buffer);
        return buffer.toByteArray();
    }

    private void streamZip(Process process, String tags, String title, OutputStream out) throws IOException, XMLException, ParserConfigurationException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        zipOut.putNextEntry(new ZipEntry("process.xml"));
        Document doc = process.getRootOperator().getDOMRepresentation();
        XMLTools.setTagContents(doc.getDocumentElement(), "title", title);
        zipOut.write(XMLTools.toString(doc, XMLImporter.PROCESS_FILE_CHARSET).getBytes(XMLImporter.PROCESS_FILE_CHARSET));
        zipOut.closeEntry();
        zipOut.putNextEntry(new ZipEntry("preview.png"));
        streamPreviewImage(process, zipOut);
        zipOut.closeEntry();
        zipOut.putNextEntry(new ZipEntry("preview.svg"));
        zipOut.write(makeSVG(process));
        zipOut.closeEntry();
        zipOut.putNextEntry(new ZipEntry("metadata.xml"));
        XMLTools.stream(makeMetaData(process, tags), zipOut, XMLImporter.PROCESS_FILE_CHARSET);
        zipOut.closeEntry();
        zipOut.finish();
    }

    private Document makeMetaData(Process process, String tags) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("rapidminer-process");
        doc.appendChild(root);
        XMLTools.setTagContents(root, "tags", tags);
        Element operatorsElem = doc.createElement("operators");
        root.appendChild(operatorsElem);
        for (Operator op : process.getRootOperator().getAllInnerOperators()) {
            Element opElem = doc.createElement("operator");
            operatorsElem.appendChild(opElem);
            XMLTools.setTagContents(opElem, "name", op.getName());
            XMLTools.setTagContents(opElem, "type", op.getName());
            if (op.getUserDescription() != null) {
                XMLTools.setTagContents(opElem, "description", op.getUserDescription());
            }
        }
        Element inputsElem = doc.createElement("inputs");
        root.appendChild(inputsElem);
        int i = 1;
        for (String loc : process.getContext().getInputRepositoryLocations()) {
            if ((loc != null) && !loc.isEmpty()) {
                Element inputElem = doc.createElement("input");
                inputsElem.appendChild(inputElem);
                XMLTools.setTagContents(inputElem, "name", "Input " + i);
                XMLTools.setTagContents(inputElem, "location", loc);
            }
            i++;
        }
        Element outputsElem = doc.createElement("outputs");
        root.appendChild(outputsElem);
        i = 1;
        for (String loc : process.getContext().getOutputRepositoryLocations()) {
            if ((loc != null) && !loc.isEmpty()) {
                Element outputElem = doc.createElement("output");
                outputsElem.appendChild(outputElem);
                XMLTools.setTagContents(outputElem, "name", "Output " + i);
                XMLTools.setTagContents(outputElem, "location", loc);
            }
            i++;
        }
        return doc;
    }

    private void streamPreviewImage(Process process, OutputStream out) throws IOException {
        ProcessRenderer renderer = RapidMinerGUI.getMainFrame().getProcessPanel().getProcessRenderer();
        BufferedImage image = new BufferedImage(renderer.getWidth(), renderer.getHeight(), BufferedImage.TYPE_INT_RGB);
        renderer.paint(image.getGraphics());
        ImageIO.write(image, "png", out);
    }

    private byte[] makePreviewImage(Process process) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        streamPreviewImage(process, buffer);
        return buffer.toByteArray();
    }

    private void streamSVG(final Process process, final OutputStream out) throws IOException {
        ProcessRenderer renderer = RapidMinerGUI.getMainFrame().getProcessPanel().getProcessRenderer();
        renderer.setDoubleBuffered(false);
        SVGGraphics2D g = new SVGGraphics2D(out, renderer.getSize());
        g.startExport();
        renderer.render(g);
        g.endExport();
        renderer.setDoubleBuffered(true);
    }

    private byte[] makeSVG(Process process) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        streamSVG(process, buffer);
        return buffer.toByteArray();
    }
}
