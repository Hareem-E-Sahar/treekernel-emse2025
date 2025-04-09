package com.framedobjects.dashwell.utils.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import wsl.fw.exception.MdnException;

public class JMDNFile {

    private static final Logger LOGGER = Logger.getLogger(JMDNFile.class);

    private String m_wsdlUrl;

    private String m_outputDirName = null;

    /**
   * Constructs a new instance of this class with the specified URL of 
   * the WSDL document.
   * 
   * @param wsdlUrl the URL of the WSDL document.
   */
    public JMDNFile(String wsdlUrl, String outputDirName) {
        m_wsdlUrl = wsdlUrl;
        m_outputDirName = outputDirName;
    }

    /**
   * Gets the local name of the WSDL document name.<br/>
   * This is taken from {@link org.apache.axis.wsdl.toJava.JavaBuildFileWriter} such that
   * we get the same name as the WSDL2Java parser generates.
   * 
   * @return the local name of the WSDL document.
   */
    public String getWsdlLocalName() {
        int index = 0;
        String wsdlFile = m_wsdlUrl;
        if ((index = wsdlFile.lastIndexOf("/")) > 0) {
            wsdlFile = wsdlFile.substring(index + 1);
        }
        if ((index = wsdlFile.lastIndexOf("?")) > 0) {
            wsdlFile = wsdlFile.substring(0, index);
        }
        if ((index = wsdlFile.indexOf('.')) != -1) {
            return wsdlFile.substring(0, index);
        } else {
            return wsdlFile;
        }
    }

    /**
   * Trims the node (remove whitespace)
   * and normalizes it.
   * 
   * @param node the {@link org.w3c.dom.Node Node} to be normalized.
   */
    public void normalizeDomNode(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            node.setNodeValue(node.getNodeValue().trim());
            return;
        }
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) normalizeDomNode(nodes.item(i));
    }

    /**
   * Generates a crc32 checksum of a string.
   * 
   * @param str the string to be checksummed.
   * @return the checksum value.
   */
    public long generateChecksum(String str) {
        byte[] bytes = str.getBytes();
        Checksum checksumEngine = new CRC32();
        checksumEngine.update(bytes, 0, bytes.length);
        long checksum = checksumEngine.getValue();
        return checksum;
    }

    /**
   * Searches for the specified checksum in the JMDN file
   * of the current web service and returns it.<br/>
   * Returns 0 if there is no entry.<br/>
   * 
   * @return the checksum value if found, 0 otherwise.
 * @throws MdnException 
   * @throws MdnException
   */
    public long getChecksum() throws MdnException {
        try {
            File file = new File(m_outputDirName + getWsdlLocalName() + ".mdnwebservice");
            if (file.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(file.getCanonicalPath()));
                in.readLine();
                String buf = in.readLine();
                long oldChecksum = Long.parseLong(buf);
                return oldChecksum;
            }
        } catch (IOException e) {
            throw new MdnException("error.IOException", e);
        }
        return 0;
    }

    /**
   * Returns a {@link java.util.List List} containing the names of the 
   * generated classes from the webservice mdn file.
   * 
   * @return a {@link java.util.List List} of class names.
 * @throws MdnException 
   * @throws MdnException
   */
    public List getGeneratedClassNames() throws MdnException {
        List<String> classnames = new ArrayList<String>();
        try {
            File file = new File(m_outputDirName + getWsdlLocalName() + ".mdnwebservice");
            BufferedReader in = new BufferedReader(new FileReader(file.getCanonicalPath()));
            String buf;
            in.readLine();
            in.readLine();
            while ((buf = in.readLine()) != null) {
                if (!buf.equals("")) classnames.add(buf);
            }
            LOGGER.info("Loaded class names from " + file.getName());
            return classnames;
        } catch (FileNotFoundException e) {
            throw new MdnException("error.FileNotFoundException", e);
        } catch (IOException e) {
            throw new MdnException("error.IOException", e);
        }
    }

    /**
   * Creates a new webservice mdn file that stores information about the
   * current web service.
   * 
   * @param generatedClasses a {@link java.util.List List} of the generated classes.
   * @param newChecksum the value of the checksum of the WSDL document.
 * @throws MdnException 
   * @throws MdnException
   */
    public void createJMDNFile(List generatedClasses, long newChecksum) throws MdnException {
        try {
            File file = new File(m_outputDirName + getWsdlLocalName() + ".mdnwebservice");
            file.createNewFile();
            PrintWriter out = new PrintWriter(new FileWriter(file.getCanonicalPath()), true);
            out.println(m_wsdlUrl);
            out.println(newChecksum);
            for (Object o : generatedClasses) if (o != null) out.println(o);
            out.close();
            LOGGER.info("Created attributes file: " + file.getName());
        } catch (IOException e) {
            throw new MdnException("error.IOException", e);
        }
    }

    /**
   * Removes all Java source files from the output directory.
   */
    public void cleanOutputDir() {
        File outputDir = new File(m_outputDirName);
        for (File file : outputDir.listFiles()) {
            if (file.isDirectory()) {
                deleteDir(file);
            } else if (file.getName().endsWith(".java")) {
                file.delete();
            }
        }
    }

    /**
   * Removes recursively the contents of a directory.
   * 
   * @param dir the directory to be removed.
   * @return true on success, false on failure.
   */
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
