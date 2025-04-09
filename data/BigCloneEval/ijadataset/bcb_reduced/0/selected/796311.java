package info.joseluismartin.util.processor;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import org.apache.commons.io.FileUtils;

/**
 * Process a jasper file using the connection in the system to retrieve the data
 * @author Jose A. Corbacho
 * 
 */
public class JasperReportFileProcessor implements FileProcessor {

    private byte[] rawData;

    private Connection conn;

    private JRDataSource service;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public JasperReportFileProcessor() {
    }

    public JasperReportFileProcessor(Connection conn) {
        this.conn = conn;
    }

    public void processFile(File file, String outputType, boolean hasQuery) {
        try {
            JasperPrint print = null;
            if (hasQuery) print = JasperFillManager.fillReport(new FileInputStream(file), parameters, conn); else print = JasperFillManager.fillReport(new FileInputStream(file), parameters, service);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JRExporter exporter = new JRPdfExporter();
            if ("pdf".equals(outputType)) {
                exporter = new JRPdfExporter();
            } else if ("xls".equals(outputType)) {
                exporter = new JRXlsExporter();
            }
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            exporter.exportReport();
            setRawData(baos.toByteArray());
        } catch (JRException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processFile(byte[] rawData) {
    }

    public void setRawData(byte[] data) {
        this.rawData = data;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public static void main(String[] args) {
        try {
            JasperReport report = JasperCompileManager.compileReport("/home/jose/Projects/telmma/Documents/Code/testParameters.jrxml");
            System.out.println("Query en el jasper: " + report.getQuery().getText());
            for (JRParameter param : report.getParameters()) {
                if (!param.isSystemDefined()) System.out.println(param.getName());
            }
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("NombreCiudad", "Huelva");
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
            byte[] reportBin = JasperExportManager.exportReportToPdf(jasperPrint);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                File outputFile;
                try {
                    outputFile = File.createTempFile("simple_report", ".pdf");
                    FileUtils.writeByteArrayToFile(outputFile, reportBin);
                    System.out.println("OutputFile -> " + outputFile.getName() + " " + outputFile.getTotalSpace());
                    desktop.open(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    public void setService(JRDataSource service) {
        this.service = service;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
