package pedro.io;

import pedro.metaData.*;
import pedro.mda.model.*;
import pedro.system.*;
import pedro.soa.ontology.provenance.OntologyTermProvenanceManager;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NativeFileFormatWriter {

    private static final int BUFFER_SIZE = 2048;

    private RecordModelFactory recordModelFactory;

    private RecordModel topRecordModel;

    private DocumentMetaData documentMetaData;

    private boolean omitModelStamp;

    private PedroFormContext pedroFormContext;

    public NativeFileFormatWriter(PedroFormContext pedroFormContext) {
        this.pedroFormContext = pedroFormContext;
        recordModelFactory = (RecordModelFactory) pedroFormContext.getDocumentProperty(PedroApplicationContext.RECORD_MODEL_FACTORY);
        documentMetaData = (DocumentMetaData) pedroFormContext.getDocumentProperty(PedroDocumentContext.DOCUMENT_META_DATA);
    }

    /**
	* causes Pedro to ignore whether the model stamp used in the file matches the model stamp
	* used by the application
	*/
    public void omitModelStamp() {
        omitModelStamp = true;
    }

    /**
     * assumes file will be of format "x.pdz"
     */
    public void writeFile(File file, RecordModel recordModel) throws IOException {
        String zipFileName = file.getAbsolutePath();
        int dotPosition = zipFileName.lastIndexOf(".");
        String fileRootName = zipFileName.substring(0, dotPosition);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        String nativeFileName = fileRootName + ".pdr";
        File nativeFormatFile = new File(nativeFileName);
        writeNativeFormatFile(nativeFormatFile, recordModel);
        addToZipFile(zipOut, nativeFormatFile);
        if (documentMetaData != null) {
            String metaDataFileName = fileRootName + ".meta";
            File metaDataFile = new File(metaDataFileName);
            writeMetaData(metaDataFile, recordModel);
            addToZipFile(zipOut, metaDataFile);
            metaDataFile.delete();
        }
        zipOut.close();
        nativeFormatFile.delete();
    }

    /**
     * assumes file will be of format "x.pdr"
     */
    private void writeNativeFormatFile(File file, RecordModel recordModel) throws IOException {
        PedroDataFileWriter pedroDataFileWriter = new PedroDataFileWriter(pedroFormContext, false, true);
        if (omitModelStamp == true) {
            pedroDataFileWriter.omitModelStamp();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        pedroDataFileWriter.write(fileOutputStream, recordModel);
        fileOutputStream.close();
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
        fileStream.close();
    }

    private void writeMetaData(File metaDataFile, RecordModel dataFileRootModel) throws IOException {
        PedroFormContext metaDataFormContext = (PedroFormContext) pedroFormContext.getApplicationProperty(PedroApplicationContext.META_DATA_FORM_CONTEXT);
        DocumentMetaData documentMetaData = (DocumentMetaData) pedroFormContext.getDocumentProperty(PedroDocumentContext.DOCUMENT_META_DATA);
        DocumentMetaDataUtility documentMetaDataUtility = new DocumentMetaDataUtility();
        documentMetaDataUtility.updateStatistics(documentMetaData, dataFileRootModel);
        DocumentMetaDataConverter converter = new DocumentMetaDataConverter(metaDataFormContext);
        RecordModel metaDataRootModel = converter.convertToPedroDataStructures(documentMetaData, dataFileRootModel);
        PedroDataFileWriter metaDataFileWriter = new PedroDataFileWriter(metaDataFormContext, false, true);
        FileOutputStream fileOutputStream = new FileOutputStream(metaDataFile);
        metaDataFileWriter.write(fileOutputStream, metaDataRootModel);
    }
}
