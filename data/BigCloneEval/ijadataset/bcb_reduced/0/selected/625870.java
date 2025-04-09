package com.loribel.java.ant;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.loribel.commons.ant.GB_FileSetsTask;
import com.loribel.commons.util.CTools;
import com.loribel.commons.util.FTools;
import com.loribel.commons.util.GB_XmlTools;
import com.loribel.commons.xml.GB_ElementTools;
import com.loribel.commons.xml.GB_XmlWriterDefault;
import com.loribel.java.tools.GB_SourceTools;

/**
 * GB_Listener2XmlAnt
 * 
 * @author Gregory Borelli
 */
public class GB_Listener2XmlAnt extends GB_FileSetsTask {

    private File destFile;

    private String encoding;

    public GB_Listener2XmlAnt() {
    }

    public void appendListenerInfo(String a_methodName, Element a_node, String a_src) {
        String l_patternAddListener = "\\." + a_methodName + "My[a-zA-Z0-9-_]+Listener";
        Pattern l_pattern = Pattern.compile(l_patternAddListener);
        Matcher l_matcher = l_pattern.matcher(a_src);
        Element l_node;
        while (l_matcher.find()) {
            l_node = GB_XmlTools.addElement(a_node, a_methodName, null);
            System.out.println("Find OK ");
            int l_start = l_matcher.start();
            int l_end = l_matcher.end();
            System.out.println(l_start + " - " + l_end);
            System.out.println(a_src.substring(l_start, l_end));
            int l_line = GB_SourceTools.lineNumberFromIndex(a_src, l_start);
            System.out.println("line: " + l_line);
            String l_details = GB_SourceTools.lineFromIndex(a_src, l_start);
            System.out.println("line: " + l_details);
            l_node.setAttribute("line", "" + l_line);
            GB_XmlTools.addElement(l_node, "details", l_details);
        }
    }

    /**
     * Execute the task.
     */
    public void execute() throws BuildException {
        List l_files = getFileList();
        if (destFile == null) {
            throw new BuildException("destFile must be defined");
        }
        Document l_doc = GB_XmlTools.newDocument();
        Element l_root = GB_XmlTools.newElement(l_doc, "xml");
        l_doc.appendChild(l_root);
        int len = CTools.getSize(l_files);
        if (len == 0) {
            System.out.println("No files to treat");
            return;
        }
        try {
            File l_file;
            Element l_node;
            for (int i = 0; i < len; i++) {
                l_file = (File) l_files.get(i);
                l_node = GB_ElementTools.appendChildElement(l_root, "file", null);
                l_node.setAttribute("name", l_file.getAbsolutePath());
                System.out.println("treat " + l_file.getName());
                this.executeOnFile(l_node, l_file);
            }
            GB_XmlWriterDefault l_writer = new GB_XmlWriterDefault(destFile, encoding);
            l_writer.setXslFilename("check-listener.xsl");
            l_writer.writePrologue();
            l_writer.write(l_root);
            l_writer.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    protected void executeOnFile(Element a_root, File a_file) throws IOException {
        String l_src = FTools.readFile(a_file);
        appendListenerInfo("add", a_root, l_src);
        appendListenerInfo("remove", a_root, l_src);
    }

    public String getEncoding() {
        return encoding;
    }

    public void setDestFile(File a_destFile) throws IOException {
        destFile = a_destFile;
    }

    public void setEncoding(String a_encoding) {
        encoding = a_encoding;
    }
}
