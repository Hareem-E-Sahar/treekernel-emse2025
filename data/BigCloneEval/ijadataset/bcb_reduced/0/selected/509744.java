package com.calipso.reportgenerator.common;

import com.calipso.reportgenerator.common.exception.InfoException;
import com.calipso.reportgenerator.reportcalculator.Matrix;
import com.calipso.reportgenerator.reportdefinitions.ReportSourceDefinition;
import com.calipso.reportgenerator.reportdefinitions.ReportDefinition;
import com.calipso.reportgenerator.reportdefinitions.ReportView;
import java.io.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.*;
import org.xml.sax.InputSource;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

/**
 * Representa un reporte completo con sus datos incluidos. Utilizado normalmente para enportar un informe y dejarlo conjelado para su posteriri a nalisis off line o en otro momento
 */
public class MicroReport implements Serializable {

    private Matrix matrix;

    private ReportSourceDefinition reportSourceDefinition;

    private ReportDefinition reportDefinition;

    private ReportView reportView;

    private String name;

    private Map views;

    private String userName;

    private Map definitionsInfo;

    private Map configuration;

    private Map params;

    public Map getParams() {
        return params;
    }

    public void setParams(Map params) {
        this.params = params;
    }

    public MicroReport(Matrix matrix, ReportSourceDefinition reportSourceDefinition, ReportDefinition reportDefinition, ReportView reportView, String name, String userName, Map views, Map params) {
        this.matrix = matrix;
        this.reportSourceDefinition = reportSourceDefinition;
        this.reportDefinition = reportDefinition;
        this.reportView = reportView;
        this.name = name;
        this.userName = userName;
        this.views = views;
        this.params = params;
    }

    /**
   * Lista de vistas
   * @return views
   */
    public Map getViews() {
        if (views == null) {
            views = new HashMap();
        }
        return views;
    }

    /**
   * retorna un Zip con el micro report
   * @param outFileName
   * @return
   * @throws com.calipso.reportgenerator.common.InfoException
   */
    public ZipOutputStream getZip(String outFileName, boolean csvSerialize) throws InfoException {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFileName));
            configuration = null;
            if (!csvSerialize) {
                addObjectToZip(1, out, matrix, reportDefinition.getId() + "_Matrix", "Matrix");
            } else {
                addMatrixToZip(out, matrix, reportDefinition.getId() + "_Matrix", "Matrix");
            }
            addObjectToZip(2, out, reportSourceDefinition, reportDefinition.getId() + "_ReportSourceDefinition", "ReportSourceDefinition");
            addObjectToZip(2, out, reportDefinition, reportDefinition.getId() + "_ReportDefinition", "ReportDefinition");
            if (reportView != null) {
                addObjectToZip(2, out, reportView, reportDefinition.getId() + "_ReportView", "ReportView");
            }
            for (int i = 0; i < getViews().size(); i++) {
                addObjectToZip(2, out, getViews().values().toArray()[i], reportDefinition.getId() + "_ReportView" + i, "ReportView" + i);
            }
            params = ReportMap.setParametersToSimpleType(params);
            addObjectToZip(1, out, params, "Params", "Params");
            addObjectToZip(1, out, getConfiguration(), "description", "description");
            return out;
        } catch (Exception e) {
            throw new InfoException(LanguageTraslator.traslate("266"), e);
        }
    }

    private void addMatrixToZip(ZipOutputStream zipOutputStream, Matrix matrix, String name, String typeName) throws IOException, InfoException {
        ByteArrayOutputStream out = MatrixCsvSerializer.csvSerialize(matrix);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        int len;
        byte[] buf = new byte[1024];
        zipOutputStream.putNextEntry(new ZipEntry(name));
        while ((len = in.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, len);
        }
        getConfiguration().put(typeName, name);
        zipOutputStream.closeEntry();
        in.close();
    }

    /**
   * Agrega el objeto al zip
   * @param serializerType  1- serializado, 2- XML
   * @param zipOutputStream
   * @param o Objeto a aagregar
   * @param name
   * @throws org.exolab.castor.xml.ValidationException
   * @throws org.exolab.castor.xml.MarshalException
   * @throws java.io.IOException
   */
    protected void addObjectToZip(int serializerType, ZipOutputStream zipOutputStream, Object o, String name, String typeName) throws ValidationException, MarshalException, IOException, InfoException {
        ByteArrayInputStream in = null;
        ObjectOutputStream oos;
        int len;
        byte[] buf = new byte[1024];
        if (serializerType == 1) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(outputStream);
            oos.writeObject(o);
            in = new ByteArrayInputStream(outputStream.toByteArray());
        } else if (serializerType == 2) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = null;
            try {
                String writerClassName;
                String javaVersion = System.getProperty("java.vm.version");
                if (javaVersion.startsWith("1.4")) {
                    writerClassName = "org.apache.xalan.serialize.WriterToUTF8";
                } else {
                    writerClassName = "com.sun.org.apache.xml.internal.serializer.WriterToUTF8";
                }
                Class writerClass = Class.forName(writerClassName);
                writer = (Writer) writerClass.getConstructor(new Class[] { OutputStream.class }).newInstance(new Object[] { outputStream });
            } catch (Exception e) {
                throw new InfoException(LanguageTraslator.traslate("471"), e);
            }
            Marshaller.marshal(o, writer);
            in = new ByteArrayInputStream(outputStream.toByteArray());
        }
        zipOutputStream.putNextEntry(new ZipEntry(name));
        while ((len = in.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, len);
        }
        getConfiguration().put(typeName, name);
        zipOutputStream.closeEntry();
        in.close();
    }

    /**
   * Inicializa un Micro report desde un zip
   * @param microReportFileName
   * @param reportGeneratorConfiguration
   * @throws com.calipso.reportgenerator.common.InfoException
   */
    public MicroReport(String microReportFileName, ReportGeneratorConfiguration reportGeneratorConfiguration) throws InfoException {
        try {
            java.util.zip.ZipFile zipFile = new ZipFile(microReportFileName);
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("description"));
            ObjectInputStream ois;
            ois = new ObjectInputStream(inputStream);
            configuration = (HashMap) ois.readObject();
            reportSourceDefinition = (ReportSourceDefinition) loadSerializedXmlObject(zipFile, "ReportSourceDefinition", ReportSourceDefinition.class);
            try {
                String matrixFileName = getConfiguration().get("Matrix").toString();
                matrix = (Matrix) MatrixCsvSerializer.deserialize(reportGeneratorConfiguration, zipFile.getInputStream(zipFile.getEntry(matrixFileName)), reportSourceDefinition);
            } catch (Exception e) {
                matrix = (Matrix) loadSerializedObject(zipFile, "Matrix");
            }
            reportDefinition = (ReportDefinition) loadSerializedXmlObject(zipFile, "ReportDefinition", ReportDefinition.class);
            reportView = (ReportView) loadSerializedXmlObject(zipFile, "ReportView", ReportView.class);
            params = (Map) loadSerializedObject(zipFile, "Params");
            loadReportViews(zipFile);
        } catch (Exception e) {
            throw new InfoException(LanguageTraslator.traslate("265"), e);
        }
    }

    private Object loadSerializedObject(ZipFile zipFile, String name) throws IOException, ClassNotFoundException {
        InputStream inputStream;
        if (getConfiguration().containsKey(name)) {
            ZipEntry zipEntry = zipFile.getEntry(getConfiguration().get(name).toString());
            inputStream = zipFile.getInputStream(zipEntry);
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            return ois.readObject();
        }
        return null;
    }

    private Object loadSerializedXmlObject(ZipFile zipFile, String name, Class classLoad) throws IOException, MarshalException, ValidationException {
        InputStream inputStream;
        InputSource inputSource;
        if (getConfiguration().containsKey(name)) {
            ZipEntry zipEntry = zipFile.getEntry(getConfiguration().get(name).toString());
            inputStream = zipFile.getInputStream(zipEntry);
            inputSource = new InputSource(inputStream);
            return Unmarshaller.unmarshal(classLoad, inputSource);
        }
        return null;
    }

    private void loadReportViews(ZipFile zipFile) throws IOException, MarshalException, ValidationException {
        int count = 0;
        boolean eol = false;
        String viewName;
        Object addicReportView;
        while (!eol) {
            viewName = "ReportView" + count;
            addicReportView = loadSerializedXmlObject(zipFile, viewName, ReportView.class);
            if (addicReportView != null) {
                getViews().put(((ReportView) addicReportView).getId(), addicReportView);
            }
            eol = !getConfiguration().containsKey(viewName);
            count = count + 1;
        }
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public ReportSourceDefinition getReportSourceDefinition() {
        return reportSourceDefinition;
    }

    public ReportDefinition getReportDefinition() {
        return reportDefinition;
    }

    public ReportView getReportView() {
        return reportView;
    }

    public String getName() {
        return name;
    }

    public Map getDefinitionsInfo() {
        if (definitionsInfo == null) {
            definitionsInfo = new HashMap();
            ReportView reportView;
            DefinitionInfo definitionInfo;
            for (int i = 0; i < getViews().size(); i++) {
                reportView = (ReportView) getViews().values().toArray()[i];
                definitionInfo = new DefinitionInfo();
                definitionInfo.setId(reportView.getId());
                definitionInfo.setDescription(reportView.getDescription());
                definitionsInfo.put(reportView.getId(), definitionInfo);
            }
        }
        return definitionsInfo;
    }

    public Map getConfiguration() {
        if (configuration == null) {
            configuration = new HashMap();
        }
        return configuration;
    }

    /**
   * Indica si el reporte es el musmo al buscado
   *
   * @param fileName Microreport
   * @param reportGeneratorConfiguration
   * @param reportDefinitionID
   * @param params Parametros de reporte
   */
    public static boolean sameReport(String fileName, ReportGeneratorConfiguration reportGeneratorConfiguration, String reportDefinitionID, Map params) throws InfoException {
        ObjectInputStream ois = null;
        java.util.zip.ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(fileName);
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("description"));
            ois = new ObjectInputStream(inputStream);
        } catch (Exception e) {
            throw new InfoException(LanguageTraslator.traslate("460"), e);
        }
        Map configuration = null;
        try {
            configuration = (HashMap) ois.readObject();
        } catch (Exception e) {
            throw new InfoException(LanguageTraslator.traslate("459"), e);
        }
        if (configuration.containsKey("ReportDefinition")) {
            if (!(reportDefinitionID + "_ReportDefinition").equalsIgnoreCase(configuration.get("ReportDefinition").toString())) {
                return false;
            }
        } else {
            return false;
        }
        if (configuration.containsKey("Params")) {
            try {
                ZipEntry zipEntry = zipFile.getEntry(configuration.get("Params").toString());
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                ObjectInputStream ois2 = new ObjectInputStream(inputStream);
                Map microReportParams = (Map) ois2.readObject();
                return equalParams(microReportParams, params);
            } catch (Exception e) {
                throw new InfoException(LanguageTraslator.traslate("461"), e);
            }
        } else {
            return true;
        }
    }

    /**
   * Compara dos Map y retorna si estan todos las entradas  y si todos los valores son iguales
   * @param microReportParams
   * @param params
   * @return
   */
    private static boolean equalParams(Map microReportParams, Map params) {
        Iterator microReportValues = microReportParams.entrySet().iterator();
        while (microReportValues.hasNext()) {
            Map.Entry microReportValue = (Map.Entry) microReportValues.next();
            if (params.containsKey(microReportValue.getKey())) {
                if (!params.get(microReportValue.getKey()).equals(microReportValue.getValue())) {
                    return false;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
