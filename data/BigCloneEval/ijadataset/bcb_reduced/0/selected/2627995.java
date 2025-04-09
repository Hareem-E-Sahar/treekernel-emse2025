package freestyleLearning.learningUnitViewAPI;

import java.net.URL;
import java.security.Security;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.JAFDataHandlerDeserializerFactory;
import org.apache.axis.encoding.ser.JAFDataHandlerSerializerFactory;

/**
 * FSLLearningUnitViewOpenUSSWebServiceClient.
 * Client for invoking web services from OpenUSS.
 * @author Carsten Fiedler
 */
public class FSLLearningUnitViewOpenUSSWebServiceClient {

    private FSLLearningUnitViewManager learningUnitViewManager;

    private FSLLearningUnitViewElementsManager learningUnitViewElementsManager;

    private String serverName;

    private String username;

    private String password;

    /**
	 * Constructor.
	 * @param <code>FSLLearningUnitViewManager</code> learningUnitViewManager
	 * @param <code>FSLLearningUnitViewElementsManager</code> learningUnitViewElementsManager
	 * @param <code>String</code> serverName
	 * @param <code>String</code> username
	 * @param <code>String</code> password
	 */
    public FSLLearningUnitViewOpenUSSWebServiceClient(FSLLearningUnitViewManager learningUnitViewManager, FSLLearningUnitViewElementsManager learningUnitViewElementsManager, String serverName, String username, String password) {
        this.learningUnitViewManager = learningUnitViewManager;
        this.learningUnitViewElementsManager = learningUnitViewElementsManager;
        this.serverName = serverName;
        this.username = username;
        this.password = password;
    }

    /**
	 * Invoke web service for user authentication.
	 * @return <code>boolean</code> true if authentication is successsfull
	 */
    public boolean authenticateUser() {
        boolean userExits = false;
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new URL("http://" + serverName + ":8080/axis/FreestyleLearningSynchronizeProxy"));
            call.setOperationName("authenticateUser");
            userExits = Boolean.TRUE.equals(call.invoke(new Object[] { username, password }));
        } catch (Exception e) {
        }
        return userExits;
    }

    /**
	 * Uploads fslv-file to server via web service.
	 * @param <code>String</code> enrollmentId
	 * @param <code>String</code> learningUnitId
	 * @param <code>String</code> learningUnitViewId
	 * @param <code>File</code> fslvFile
	 * @return <code>boolean</code> true if transfer was successfull
	 */
    public boolean uploadLearningUnitVew(String enrollmentId, String learningUnitId, String learningUnitViewId, File fslvFile) {
        System.out.println("uploadLearningUnitVew");
        boolean uploadFinishedSuccessfull = false;
        buildLearningUnitFslvFile(fslvFile);
        try {
            DataHandler dhSource = new DataHandler(new FileDataSource(fslvFile));
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new URL("http://" + serverName + ":8080/axis/FreestyleLearningSynchronizeProxy"));
            call.setOperationName("uploadLearningUnitView");
            call.addParameter("username", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("password", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("enrollmentId", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("learningUnitId", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("learningUnitViewId", XMLType.XSD_STRING, ParameterMode.IN);
            QName qnameAttachment = new QName("uploadLearningUnitView", "DataHandler");
            call.registerTypeMapping(dhSource.getClass(), qnameAttachment, JAFDataHandlerSerializerFactory.class, JAFDataHandlerDeserializerFactory.class);
            call.addParameter("DataHandler", qnameAttachment, ParameterMode.IN);
            call.setReturnType(XMLType.AXIS_VOID);
            if (false) {
                call.setProperty(Call.ATTACHMENT_ENCAPSULATION_FORMAT, Call.ATTACHMENT_ENCAPSULATION_FORMAT_DIME);
            }
            call.invoke(new Object[] { username, password, enrollmentId, learningUnitId, learningUnitViewId, dhSource });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadFinishedSuccessfull;
    }

    /**
	 * Invokes downloading of fslv file from server.
	 * @param <code>String</code> enrollmentId
	 * @param <code>String</code> learningUnitId
	 * @param <code>String</code> learningUnitViewId
	 * @return <code>File</code> fslv file from server
	 */
    public File downloadLearningUnitView(String enrollmentId, String learningUnitId, String learningUnitViewId) {
        String fslvFileName = null;
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new URL("http://" + serverName + ":8080/axis/FreestyleLearningSynchronizeProxy"));
            call.setOperationName("downloadLearningUnitView");
            call.addParameter("username", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("password", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("enrollmentId", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("learningUnitId", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("learningUnitViewId", XMLType.XSD_STRING, ParameterMode.IN);
            QName qnameAttachment = new QName("downloadLearningUnitView", "DataHandler");
            call.registerTypeMapping(javax.activation.DataHandler.class, qnameAttachment, JAFDataHandlerSerializerFactory.class, JAFDataHandlerDeserializerFactory.class);
            call.setReturnType(qnameAttachment);
            fslvFileName = learningUnitViewElementsManager.getLearningUnitViewOriginalDataDirectory().getAbsoluteFile() + System.getProperty("file.separator") + learningUnitViewId + ".fslv";
            DataHandler handler = (DataHandler) call.invoke(new Object[] { username, password, enrollmentId, learningUnitId, learningUnitViewId });
            InputStream stream = handler.getDataSource().getInputStream();
            BufferedInputStream in = new BufferedInputStream(stream);
            FileOutputStream outputStream = new FileOutputStream(fslvFileName);
            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            byte[] data = new byte[1024 * 8];
            int size = 0;
            while ((size = in.read(data)) != -1) {
                out.write(data, 0, size);
            }
            stream.close();
            in.close();
            out.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(fslvFileName);
    }

    /**
	 * Downloads contents.xml from server to given learning unit id and learning unit view id.
	 * @param <code>String</code> enrollmentId
	 * @param <code>String</code> learningUnitId
	 * @param <code>String</code> learningUnitViewId
	 * @return <code>File</code> temporary contents.xml for synchronization
	 */
    public File downloadContentsXmlFile(String enrollmentId, String learningUnitId, String learningUnitViewId) {
        String contentsXMLFileName = null;
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new URL("http://" + serverName + ":8080/axis/FreestyleLearningSynchronizeProxy"));
            call.setOperationName("downloadContentsXmlFile");
            call.addParameter("username", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("password", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("enrollmentId", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("learningUnitId", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("learningUnitViewId", XMLType.XSD_STRING, ParameterMode.IN);
            QName qnameAttachment = new QName("downloadLearningUnitView", "DataHandler");
            call.registerTypeMapping(javax.activation.DataHandler.class, qnameAttachment, JAFDataHandlerSerializerFactory.class, JAFDataHandlerDeserializerFactory.class);
            call.setReturnType(qnameAttachment);
            contentsXMLFileName = learningUnitViewElementsManager.getLearningUnitViewOriginalDataDirectory().getAbsoluteFile() + System.getProperty("file.separator") + "server_contents.xml";
            DataHandler handler = (DataHandler) call.invoke(new Object[] { username, password, enrollmentId, learningUnitId, learningUnitViewId });
            InputStream stream = handler.getDataSource().getInputStream();
            BufferedInputStream in = new BufferedInputStream(stream);
            FileOutputStream fileOutputStream = new FileOutputStream(contentsXMLFileName);
            BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
            byte[] data = new byte[1024 * 8];
            int size = 0;
            while ((size = in.read(data)) != -1) {
                out.write(data, 0, size);
            }
            stream.close();
            in.close();
            out.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(contentsXMLFileName);
    }

    private void buildLearningUnitFslvFile(File selectedFile) {
        File outputFile = selectedFile;
        if (learningUnitViewElementsManager.isOriginalElementsOnly()) {
            String[] elementIds = learningUnitViewElementsManager.getAllLearningUnitViewElementIds();
            for (int i = 0; i < elementIds.length; i++) {
                FSLLearningUnitViewElement element = learningUnitViewElementsManager.getLearningUnitViewElement(elementIds[i], false);
                if (element.getLastModificationDate() == null) {
                    element.setLastModificationDate(String.valueOf(new Date().getTime()));
                    element.setModified(true);
                }
            }
            learningUnitViewElementsManager.setModified(true);
            learningUnitViewManager.saveLearningUnitViewData();
            if (selectedFile != null) {
                try {
                    FileOutputStream os = new FileOutputStream(outputFile);
                    ZipOutputStream zipOutputStream = new ZipOutputStream(os);
                    File[] files = (new File(learningUnitViewManager.getLearningUnitViewOriginalDataDirectory().getPath())).listFiles();
                    buildExportZipFile("", zipOutputStream, files);
                    zipOutputStream.flush();
                    zipOutputStream.finish();
                    zipOutputStream.close();
                    os.close();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }
    }

    private void buildExportZipFile(String parentDirectory, ZipOutputStream zipOutputStream, File[] files) {
        try {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    if (parentDirectory.equals("")) {
                        ZipEntry newZipEntry = new ZipEntry(files[i].getName() + "/");
                        zipOutputStream.putNextEntry(newZipEntry);
                        zipOutputStream.closeEntry();
                        zipOutputStream.flush();
                        File[] subFiles = files[i].listFiles();
                        buildExportZipFile(files[i].getName(), zipOutputStream, subFiles);
                    } else {
                        ZipEntry newZipEntry = new ZipEntry(parentDirectory + System.getProperty("file.separator") + files[i].getName() + "/");
                        zipOutputStream.putNextEntry(newZipEntry);
                        zipOutputStream.closeEntry();
                        zipOutputStream.flush();
                        File[] subFiles = files[i].listFiles();
                        buildExportZipFile(parentDirectory + System.getProperty("file.separator") + files[i].getName(), zipOutputStream, subFiles);
                    }
                } else {
                    ZipEntry newZipEntry;
                    if (parentDirectory.equals("")) {
                        newZipEntry = new ZipEntry(files[i].getName());
                    } else {
                        newZipEntry = new ZipEntry(parentDirectory + System.getProperty("file.separator") + files[i].getName());
                    }
                    zipOutputStream.putNextEntry(newZipEntry);
                    InputStream is = new FileInputStream(files[i]);
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                    is.close();
                    zipOutputStream.closeEntry();
                    zipOutputStream.flush();
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(new FSLLearningUnitViewOpenUSSWebServiceClient(null, null, "localhost", "author", "author").authenticateUser());
        System.setProperty("javax.net.ssl.trustStore", "server.cer");
        System.setProperty("javax.net.ssl.trustStorePassword", "secure");
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        System.setProperty("javax.net.debug", "all");
        System.setProperty("java.security.debug", "all");
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }
}
