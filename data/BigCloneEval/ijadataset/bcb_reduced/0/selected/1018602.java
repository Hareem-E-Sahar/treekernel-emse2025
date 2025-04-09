package org.commonlibrary.lcms.scorm.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonlibrary.lcms.model.Curriculum;
import org.commonlibrary.lcms.scorm.event.ScormEventListener;
import org.commonlibrary.lcms.scorm.event.engine.Exporter;
import org.commonlibrary.lcms.scorm.event.engine.ScormEventListenerBroadcaster;
import org.commonlibrary.lcms.scorm.event.engine.ScormExporterEventListener;
import org.commonlibrary.lcms.scorm.service.ScormService;
import org.commonlibrary.lcms.support.spring.beans.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ScormService implementation.
 *
 * @author jorge.elizondo
 *         Date: 08.12.2008
 *         Time: 09:45:18
 *         <p/>
 */
@Transactional
@Service("scormService")
public class ScormServiceImpl implements ScormService {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private Exporter exporter;

    @Property("clv2.data.tmp.dir")
    private String tmpDirectory;

    public void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    public void setTmpDirectory(String tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
    }

    /**
     * @see org.commonlibrary.lcms.scorm.service.ScormService#exportCurriculum(org.commonlibrary.lcms.model.Curriculum)
     */
    public InputStream exportCurriculum(Curriculum curriculum) {
        byte[] buf = new byte[1024];
        String folderName = curriculum.getName().replaceAll(" ", "_");
        java.io.File folder = new java.io.File(tmpDirectory + "/" + folderName);
        folder.mkdir();
        String imsmanifest = tmpDirectory + "/" + folderName + "/imsmanifest.xml";
        ScormEventListener listener = new ScormExporterEventListener(imsmanifest);
        ScormEventListenerBroadcaster broadcaster = new ScormEventListenerBroadcaster();
        broadcaster.register(listener);
        exporter.setBroadcaster(broadcaster);
        exporter.processCurriculum(curriculum);
        ZipOutputStream out = null;
        String outFilename = tmpDirectory + "/" + curriculum.getName() + ".zip";
        try {
            out = new ZipOutputStream(new FileOutputStream(outFilename));
            FileInputStream in = new FileInputStream(imsmanifest);
            ZipEntry zipEntry = new ZipEntry("imsmanifest.xml");
            out.putNextEntry(zipEntry);
            transferBytes(buf, out, in);
            out.closeEntry();
            in.close();
            addXsdEntry(buf, out, in, "adlcp_rootv1p2.xsd");
            addXsdEntry(buf, out, in, "ims_xml.xsd");
            addXsdEntry(buf, out, in, "imscp_rootv1p1p2.xsd");
            addXsdEntry(buf, out, in, "imsmd_rootv1p2p1.xsd");
            out.flush();
            out.close();
            folder.delete();
            return new FileInputStream(outFilename);
        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Adds an xsd file needed by Scorm to the zip file.
     * @param buf Buffer to read files from
     * @param out ZipOutputStream to write bytes to
     * @param in FileInputStream to read xsdFile
     * @param xsdFile name of the xsd file.
     * @throws IOException
     */
    private void addXsdEntry(byte[] buf, ZipOutputStream out, FileInputStream in, String xsdFile) throws IOException {
        ZipEntry zipEntry;
        InputStream xsd = getClass().getResourceAsStream("/ims/" + xsdFile);
        zipEntry = new ZipEntry(xsdFile);
        out.putNextEntry(zipEntry);
        transferBytes(buf, out, xsd);
        out.closeEntry();
        in.close();
    }

    /**
     * Transfer bytes from the file to the ZIP file
     *
     * @param buf Buffer to read files from
     * @param out ZipOutputStream to write bytes to
     * @param in  FileInputStream to read file
     * @throws IOException
     */
    private void transferBytes(byte[] buf, ZipOutputStream out, InputStream in) throws IOException {
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }
}
