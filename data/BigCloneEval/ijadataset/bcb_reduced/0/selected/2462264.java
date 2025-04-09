package equilibrium.commons.report.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.jasperreports.engine.JasperPrint;
import equilibrium.commons.io.StreamUtils;

public class ZIPReportExporter implements ReportExporter {

    private final ReportExporter reportExporter;

    public ZIPReportExporter(ReportExporter reportExporter) {
        this.reportExporter = reportExporter;
    }

    public void exportReport(JasperPrint jasperPrint, File outputFile) {
        reportExporter.exportReport(jasperPrint, outputFile);
        createZipArchive(outputFile);
    }

    public void exportReport(File jasperPrintCache, File outputFile) {
        reportExporter.exportReport(jasperPrintCache, outputFile);
        createZipArchive(outputFile);
    }

    private void createZipArchive(File file) {
        File zipFile = new File(file.getPath() + ".zip");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (File childFile : file.listFiles()) {
                insertFileToZip(childFile, zipOutputStream);
            }
            zipOutputStream.close();
        } catch (IOException e) {
            throw new ReportExporterException("Could not create zip with exported report files", e);
        }
        file.delete();
    }

    private void insertFileToZip(File childFile, ZipOutputStream zipOutputStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(childFile.getName());
        FileInputStream fileInputStream = new FileInputStream(childFile);
        zipOutputStream.putNextEntry(zipEntry);
        StreamUtils.inputToOutput(fileInputStream, zipOutputStream);
        zipOutputStream.closeEntry();
        childFile.delete();
    }
}
