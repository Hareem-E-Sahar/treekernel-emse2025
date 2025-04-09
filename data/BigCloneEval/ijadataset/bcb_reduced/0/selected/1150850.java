package pedro.soa.alerts;

import pedro.system.PedroResources;
import pedro.mda.model.*;
import pedro.util.PedroXMLParsingUtility;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AlertsBundleWriter {

    private static final int BUFFER_SIZE = 2048;

    private ArrayList alertFiles;

    private RecordModelFactory recordModelFactory;

    public AlertsBundleWriter(RecordModelFactory recordModelFactory) {
        this.recordModelFactory = recordModelFactory;
        alertFiles = new ArrayList();
    }

    public void writeFile(File file, ArrayList alerts) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        int numberOfAlerts = alerts.size();
        for (int i = 0; i < numberOfAlerts; i++) {
            String currentFileName = PedroResources.getMessage("pedro.alerts.alert.alertFileName", String.valueOf(i));
            Alert currentAlert = (Alert) alerts.get(i);
            File currentFile = createAlertFile(currentFileName, currentAlert);
            alertFiles.add(currentFile);
            addToZipFile(zipOut, currentFile);
        }
        deleteAlertFiles();
        zipOut.close();
    }

    private File createAlertFile(String fileName, Alert alert) throws IOException {
        File file = new File(fileName);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        String schemaName = recordModelFactory.getSchemaName();
        out.write("<?xml version = \"1.0\" encoding = \"UTF-8\"?>");
        out.flush();
        out.write("<alert ");
        out.write(recordModelFactory.getModelStamp());
        out.write(">");
        out.flush();
        out.write("<name>");
        out.write(alert.getName());
        out.write("</name>");
        out.write("<alertType>");
        AlertActionType alertType = alert.getAlertType();
        String phrase = AlertActionType.getPhraseForActionType(alertType);
        out.write(phrase);
        out.write("</alertType>");
        out.write("<message>");
        out.write(alert.getMessage());
        out.write("</message>");
        out.write("<author>");
        out.write(alert.getAuthor());
        out.write("</author>");
        out.write("<institution>");
        out.write(alert.getInstitution());
        out.write("</institution>");
        out.write("<emailAddress>");
        out.write(alert.getEmailAddress());
        out.write("</emailAddress>");
        MatchingCriteria matchingCriteria = alert.getMatchingCriteria();
        out.write("<recordType>");
        out.write(matchingCriteria.getRecordClassContext());
        out.write("</recordType>");
        out.write("<matchingCriteria>");
        ArrayList criteria = matchingCriteria.getCriteria();
        int numberOfCriteria = criteria.size();
        for (int i = 0; i < numberOfCriteria; i++) {
            Object criterion = criteria.get(i);
            if (criterion instanceof EditFieldMatchingCriterion) {
                EditFieldMatchingCriterion editFieldCriterion = (EditFieldMatchingCriterion) criterion;
                writeEditFieldCriterion(out, editFieldCriterion);
            } else if (criterion instanceof ListFieldMatchingCriterion) {
                ListFieldMatchingCriterion listFieldCriterion = (ListFieldMatchingCriterion) criterion;
                writeListFieldCriterion(out, listFieldCriterion);
            }
        }
        out.write("</matchingCriteria>");
        out.write("</alert>");
        out.flush();
        out.close();
        return file;
    }

    private void deleteAlertFiles() {
        int numberOfFiles = alertFiles.size();
        for (int i = 0; i < numberOfFiles; i++) {
            File currentFile = (File) alertFiles.get(i);
            currentFile.delete();
        }
    }

    private void writeEditFieldCriterion(OutputStreamWriter out, EditFieldMatchingCriterion criterion) throws IOException {
        String fieldName = criterion.getFieldName();
        String comparedValue = criterion.getComparedValue();
        out.write("<editFieldCriterion>");
        out.write("<fieldName>");
        out.write(criterion.getFieldName());
        out.write("</fieldName>");
        out.write("<operator>");
        FieldOperator operator = criterion.getOperator();
        String phrase = FieldOperator.getPhraseForOperator(operator);
        phrase = PedroXMLParsingUtility.escapeXml(phrase);
        out.write(phrase);
        out.write("</operator>");
        out.write("<comparedValue>");
        out.write(comparedValue);
        out.write("</comparedValue>");
        out.write("</editFieldCriterion>");
    }

    private void writeListFieldCriterion(OutputStreamWriter out, ListFieldMatchingCriterion criterion) throws IOException {
        String fieldName = criterion.getFieldName();
        int comparedValue = criterion.getComparedValue();
        out.write("<listFieldCriterion>");
        out.write("<fieldName>");
        out.write(criterion.getFieldName());
        out.write("</fieldName>");
        out.write("<operator>");
        FieldOperator operator = criterion.getOperator();
        String phrase = FieldOperator.getPhraseForOperator(operator);
        phrase = PedroXMLParsingUtility.escapeXml(phrase);
        out.write(phrase);
        out.write("</operator>");
        out.write("<comparedValue>");
        out.write(String.valueOf(comparedValue));
        out.write("</comparedValue>");
        out.write("<comparedChildType>");
        out.write(criterion.getComparedChildType());
        out.write("</comparedChildType>");
        out.write("</listFieldCriterion>");
    }

    private void addToZipFile(ZipOutputStream zipOut, File file) throws IOException {
        byte[] data = new byte[BUFFER_SIZE];
        FileInputStream fileStream = new FileInputStream(file);
        BufferedInputStream bufferedFileStream = new BufferedInputStream(fileStream, BUFFER_SIZE);
        ZipEntry entry = new ZipEntry(file.getName());
        zipOut.putNextEntry(entry);
        int count = 0;
        while ((count = bufferedFileStream.read(data, 0, BUFFER_SIZE)) != -1) {
            zipOut.write(data, 0, count);
        }
        zipOut.flush();
        bufferedFileStream.close();
    }
}
