package com.uspto.pati.Redbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import com.uspto.pati.PatiConstants;
import com.uspto.pati.common.ClaimDependencyHandler;
import com.uspto.pati.common.PartNumberHandler;

public class XMLTransformation {

    private static Logger LOG = Logger.getLogger("XMLTransformation");

    private static long COUNTER = 0;

    private static long start, end, total;

    private static long SL_COUNTER = 0;

    private static long PASS_COUNTER = 0;

    private static long FAIL_COUNTER = 0;

    public static void transformRedBookFiles() throws IOException {
        start = System.currentTimeMillis();
        FileHandler handler = new FileHandler(PatiConstants.LOG_FILE_LOC + "RedbookTransformationErrorLog.log");
        LOG.addHandler(handler);
        LOG.setLevel(Level.FINE);
        XMLTransformation st = new XMLTransformation();
        try {
            File folder = new File(PatiConstants.REDBOOK_OUTPUT_FOLDER);
            File[] listOfFiles = folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("\\d*");
                }
            });
            for (File parentFolder : listOfFiles) {
                File subFolder = new File(PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName());
                File[] listOfXMLFiles = subFolder.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xml");
                    }
                });
                for (File file : listOfXMLFiles) {
                    String fileString = RedBookRenameFiles.fileToString(file);
                    if (fileString.contains("us-sequence-listing.dtd")) {
                        LOG.fine("Sequence Listing XML File: " + file.getName());
                        SL_COUNTER++;
                    } else if (fileString.contains(PatiConstants.DTD_V15)) {
                        COUNTER++;
                        File sources = new File(PatiConstants.DTD_V15_RESOURCES);
                        copyTransformationResources(sources, subFolder);
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_CLAIMS_DTD_V15_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_CLM.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_ABST_DTD_V15_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_ABST.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_SPEC_DTD_V15_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_SPEC.xml"));
                        deleteTransformationResources(PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName());
                        PASS_COUNTER++;
                        PartNumberHandler.parsePartListNode(file.getAbsolutePath().replace(".xml", "_SPEC.xml"));
                    } else if (fileString.contains(PatiConstants.DTD_V16)) {
                        COUNTER++;
                        File sources = new File(PatiConstants.DTD_V16_RESOURCES);
                        copyTransformationResources(sources, subFolder);
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_CLAIMS_DTD_V16_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_CLM.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_ABST_DTD_V16_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_ABST.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_SPEC_DTD_V16_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_SPEC.xml"));
                        deleteTransformationResources(PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName());
                        PASS_COUNTER++;
                        PartNumberHandler.parsePartListNode(file.getAbsolutePath().replace(".xml", "_SPEC.xml"));
                    } else if (fileString.contains(PatiConstants.DTD_V40)) {
                        COUNTER++;
                        File sources = new File(PatiConstants.DTD_V40_RESOURCES);
                        copyTransformationResources(sources, subFolder);
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_CLAIMS_DTD_V40_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_CLM.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_ABST_DTD_V40_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_ABST.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_SPEC_DTD_V40_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_SPEC.xml"));
                        deleteTransformationResources(PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName());
                        PASS_COUNTER++;
                        PartNumberHandler.parsePartListNode(file.getAbsolutePath().replace(".xml", "_SPEC.xml"));
                        ClaimDependencyHandler.addClaimDependencyAttribute(file.getAbsolutePath().replace(".xml", "_CLM.xml"));
                    } else if (fileString.contains(PatiConstants.DTD_V41)) {
                        COUNTER++;
                        File sources = new File(PatiConstants.DTD_V41_RESOURCES);
                        copyTransformationResources(sources, subFolder);
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_CLAIMS_DTD_V41_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_CLM.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_ABST_DTD_V41_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_ABST.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_SPEC_DTD_V41_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_SPEC.xml"));
                        deleteTransformationResources(PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName());
                        PASS_COUNTER++;
                        PartNumberHandler.parsePartListNode(file.getAbsolutePath().replace(".xml", "_SPEC.xml"));
                        ClaimDependencyHandler.addClaimDependencyAttribute(file.getAbsolutePath().replace(".xml", "_CLM.xml"));
                    } else if (fileString.contains(PatiConstants.DTD_V42)) {
                        COUNTER++;
                        File sources = new File(PatiConstants.DTD_V42_RESOURCES);
                        copyTransformationResources(sources, subFolder);
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_CLAIMS_DTD_V42_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_CLM.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_ABST_DTD_V42_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_ABST.xml"));
                        st.transform(file.getAbsolutePath(), PatiConstants.IN_SPEC_DTD_V42_XSL, PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName() + "\\" + file.getName().replace(".xml", "_SPEC.xml"));
                        deleteTransformationResources(PatiConstants.REDBOOK_OUTPUT_FOLDER + parentFolder.getName());
                        PASS_COUNTER++;
                        PartNumberHandler.parsePartListNode(file.getAbsolutePath().replace(".xml", "_SPEC.xml"));
                        ClaimDependencyHandler.addClaimDependencyAttribute(file.getAbsolutePath().replace(".xml", "_CLM.xml"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FAIL_COUNTER++;
        }
        LOG.fine(MessageFormat.format("Generated {0} XML Documents", COUNTER * 3));
        LOG.fine(MessageFormat.format("Applications processed: {0}", COUNTER));
        LOG.fine(MessageFormat.format("Sequence listing counter: {0}", SL_COUNTER));
        LOG.fine(MessageFormat.format("Application passed: {0}", PASS_COUNTER));
        LOG.fine(MessageFormat.format("Applicaiton failed: {0}", FAIL_COUNTER));
        end = System.currentTimeMillis();
        total = end - start;
        LOG.info("Time taken to generate XML's is :" + total);
    }

    public void transform(String inXML, String inXSL, String outTXT) {
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xslStream = new StreamSource(inXSL);
        Transformer transformer;
        try {
            transformer = factory.newTransformer(xslStream);
            StreamSource in = new StreamSource(inXML);
            StreamResult out = new StreamResult(outTXT);
            transformer.transform(in, out);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            LOG.fine("Invalid factory configuration");
            LOG.fine(e.getMessageAndLocation());
        } catch (TransformerException e) {
            e.printStackTrace();
            LOG.fine("Error during transformation");
            LOG.fine(e.getMessageAndLocation());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.fine("Error during transformation");
        }
    }

    public static void copyTransformationResources(File sourceLocation, File targetLocation) throws IOException, FileNotFoundException {
        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
                }
                String[] children = sourceLocation.list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return !name.contains(".svn");
                    }
                });
                for (int i = 0; i < children.length; i++) {
                    copyTransformationResources(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
                }
            } else {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteTransformationResources(String filePath) {
        File oFile = new File(filePath);
        if (oFile.isDirectory()) {
            File[] aFiles = oFile.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return !name.endsWith(".xml");
                }
            });
            for (File oFileCur : aFiles) {
                deleteTransformationResources(oFileCur.getAbsolutePath());
            }
        }
        oFile.delete();
    }

    public static void main(String args[]) throws IOException {
        XMLTransformation.transformRedBookFiles();
    }
}
