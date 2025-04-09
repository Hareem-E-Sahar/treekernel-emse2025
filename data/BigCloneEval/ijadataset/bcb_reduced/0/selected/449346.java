package org.appspy.server.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Olivier HEDIN / olivier@appspy.org
 */
public class ReportEntityListener {

    protected Log sLog = LogFactory.getLog(ReportEntityListener.class);

    @PostLoad
    protected void loadReportContent(Report report) {
        try {
            if (report.getZippedReportContent() != null) {
                sLog.info("Unzipping report content - zipped size : " + report.getZippedReportContent().length);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(report.getZippedReportContent());
                ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
                zipInputStream.getNextEntry();
                byte[] buffer = new byte[1024];
                int nbBytes;
                while ((nbBytes = zipInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, nbBytes);
                }
                report.setReportContent(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.close();
                zipInputStream.close();
            }
        } catch (Exception ex) {
            sLog.error("Error unzipping report content", ex);
        }
    }

    @PrePersist
    public void zipReportContent(Report report) {
        try {
            if (report.getReportContent() != null) {
                sLog.info("Zipping report content");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
                zipOutputStream.putNextEntry(new ZipEntry("0"));
                zipOutputStream.write(report.getReportContent());
                zipOutputStream.closeEntry();
                report.setZippedReportContent(byteArrayOutputStream.toByteArray());
                zipOutputStream.close();
                sLog.info("Zipped size : " + report.getZippedReportContent().length);
            }
        } catch (Exception ex) {
            sLog.error("Error zipping report content", ex);
        }
    }
}
