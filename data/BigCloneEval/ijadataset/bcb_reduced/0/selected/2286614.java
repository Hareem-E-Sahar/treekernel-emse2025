package info.joseluismartin.util.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRXlsExporter;

/**
 * @author jose
 *
 */
public class JasperReportXMLFileProcessor implements FileProcessor {

    private byte[] rawData;

    private Connection conn;

    private JRDataSource service;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public void processFile(File file, String outputType, boolean hasQuery) {
        InputStream reportStream = null;
        try {
            reportStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = null;
            if (hasQuery) jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn); else jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, service);
            byte[] reportBin = null;
            if ("pdf".equals(outputType)) {
                reportBin = JasperExportManager.exportReportToPdf(jasperPrint);
            } else if ("xls".equals(outputType)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JRExporter exporter = new JRXlsExporter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
                exporter.exportReport();
                reportBin = baos.toByteArray();
            }
            setRawData(reportBin);
        } catch (JRException e) {
            System.out.println("Error processing report: " + e);
            e.printStackTrace();
        }
    }

    public void processFile(byte[] rawData) {
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public void setService(JRDataSource source) {
        this.service = source;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
