package com.das.misc.app;

import java.awt.Container;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.Element;
import com.das.core.app.AppBase;
import com.das.document.logic.Document;
import com.das.document.logic.DocumentModel;
import com.das.preference.logic.Preference;
import com.das.preference.logic.PreferenceModel;
import com.das.util.AppConstants;
import com.das.util.HibernateUtil;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

public class Indexer extends AppBase {

    private boolean uploading = false;

    private List<Document> documents;

    private final Timer timer = new Timer();

    private static final String TEMPFOLDER = "C:/temp/gribbles-das/upload";

    private static final int MAXRESULT = 5;

    private static final long INTERVAL = 7000L;

    private static final String TAG_DOCUMENTS = "documents";

    private static final String TAG_DOCUMENT = "document";

    private static final String TAG_ID = "id";

    private static final String TAG_PREFIX = "prefix";

    private static final String TAG_BARCODE = "barcode";

    private static final String TAG_LOCATION = "location";

    private static final String TAG_NAME = "name";

    private static final String TAG_OWNER = "owner";

    private static final String TAG_DATAENTRYFLAG = "dataEntryFlag";

    private static final String TAG_BACKLOGFLAG = "backLogFlag";

    /**
	 * Create the panel
	 */
    public Indexer(Container container, Map<String, Object> params) {
        super();
        setPreferredSize(new Dimension(1000, 500));
        setParams(params);
        initPanel(this, container, getParams());
        final JLabel indexerLabel = new JLabel();
        indexerLabel.setText("Indexer");
        indexerLabel.setBounds(41, 45, 66, 16);
        add(indexerLabel);
        startStopUpload();
    }

    public void testUploadZip() {
        String url = "http://localhost:8080/dms/batch/doUpload.action";
        PostMethod filePost = new PostMethod(url);
        try {
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
            File file = new File("D:/Profiles/tatseng.ho/My Documents/My Scans/samples/dms.zip");
            Part[] parts = { new FilePart("upload", file) };
            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            System.out.println("file path is : " + file.exists() + " :: " + file.getAbsolutePath());
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                System.out.println("Upload success");
            } else {
                System.out.println("Upload failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (filePost != null) {
                filePost.releaseConnection();
            }
        }
    }

    public void startStopUpload() {
        if (!isUploading()) {
            setUploading(true);
            timer.schedule(new TimerTask() {

                public void run() {
                    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                    try {
                        String url = "";
                        Transaction tx = session.beginTransaction();
                        Preference preference = new PreferenceModel();
                        List<Preference> preferences = preference.doList(preference);
                        if (preferences != null && !preferences.isEmpty()) {
                            preference = (Preference) preferences.get(0);
                        }
                        if (preference.getDmsUrl() != null && preference.getDmsUrl().length() > 0) {
                            url = preference.getDmsUrl();
                        }
                        Criteria cris = session.createCriteria(DocumentModel.class);
                        cris.add(Restrictions.eq(Document.FLD_STATUS, AppConstants.STATUS_ACTIVE));
                        Disjunction disjunction = Restrictions.disjunction();
                        disjunction.add(Restrictions.isNull(Document.FLD_UPLOADFLAG)).add(Restrictions.eq(Document.FLD_UPLOADFLAG, AppConstants.UPLOAD_FAILED));
                        cris.add(disjunction);
                        cris.setMaxResults(MAXRESULT);
                        documents = cris.list();
                        documents = resetUploadFlag(documents, AppConstants.UPLOAD_PROGRESS);
                        if (documents != null && !documents.isEmpty()) {
                            DocumentBuilderFactory factory = DocumentBuilderFactoryImpl.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            org.w3c.dom.Document xmldoc = builder.newDocument();
                            Element main, root, docelem, item;
                            root = xmldoc.createElement(TAG_DOCUMENTS);
                            for (Iterator<Document> iter = documents.iterator(); iter.hasNext(); ) {
                                Document document = iter.next();
                                if (document != null && document.getId() != null) {
                                    docelem = xmldoc.createElement(TAG_DOCUMENT);
                                    item = xmldoc.createElement(TAG_ID);
                                    item.appendChild(xmldoc.createTextNode(""));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_PREFIX);
                                    item.appendChild(xmldoc.createTextNode(document.getYear() + ""));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_BARCODE);
                                    item.appendChild(xmldoc.createTextNode(document.getBarcode()));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_LOCATION);
                                    item.appendChild(xmldoc.createTextNode(document.getLocation()));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_NAME);
                                    item.appendChild(xmldoc.createTextNode(document.getName()));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_OWNER);
                                    item.appendChild(xmldoc.createTextNode(document.getCrtby().toString()));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_DATAENTRYFLAG);
                                    item.appendChild(xmldoc.createTextNode(document.getDataEntryFlag()));
                                    docelem.appendChild(item);
                                    item = xmldoc.createElement(TAG_BACKLOGFLAG);
                                    item.appendChild(xmldoc.createTextNode(document.getBackLogFlag()));
                                    docelem.appendChild(item);
                                    root.appendChild(docelem);
                                }
                            }
                            xmldoc.appendChild(root);
                            XMLSerializer serializer = new XMLSerializer();
                            OutputFormat format = new OutputFormat();
                            StringWriter stringWriter = new StringWriter();
                            format.setEncoding("UTF-8");
                            format.setVersion("1.0");
                            format.setIndenting(true);
                            format.setIndent(4);
                            serializer.setOutputCharStream(stringWriter);
                            serializer.setOutputFormat(format);
                            serializer.serialize(xmldoc);
                            String xmlStr = stringWriter.toString();
                            System.out.println(xmlStr);
                            File folder = new File(preference.getStoreLocation() + File.separator + AppConstants.TEMP_UPLOAD_FOLDER);
                            folder.mkdirs();
                            File fxml = new File(folder, AppConstants.DMS_XMLFILE);
                            BufferedWriter writer = new BufferedWriter(new FileWriter(fxml));
                            writer.write(xmlStr);
                            writer.close();
                            File fzip = generateZip(preference, fxml, documents);
                            boolean isUploadSuccess = true;
                            if (fzip != null) {
                                isUploadSuccess = uploadZip(preference, fzip);
                            }
                            if (isUploadSuccess) {
                                documents = resetUploadFlag(documents, AppConstants.UPLOAD_SUCCESS);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            }, 0, INTERVAL);
        } else {
        }
    }

    private File generateZip(Preference preference, File xml, List<Document> documents) {
        File zfile = new File(preference.getStoreLocation() + File.separator + AppConstants.DMS_ZIPFILE);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zfile));
            FileInputStream in = new FileInputStream(xml);
            out.putNextEntry(new ZipEntry(xml.getName()));
            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
            for (Iterator<Document> iter = documents.iterator(); iter.hasNext(); ) {
                Document document = iter.next();
                in = new FileInputStream(preference.getStoreLocation() + File.separator + document.getName());
                out.putNextEntry(new ZipEntry(document.getName()));
                len = 0;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            zfile = null;
        }
        return zfile;
    }

    private boolean uploadZip(Preference preference, File file) {
        boolean result = true;
        PostMethod filePost = new PostMethod(preference.getDmsUrl());
        try {
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
            Part[] parts = { new FilePart("upload", file) };
            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                System.out.println("Upload success");
            } else {
                result = false;
                System.out.println("Upload failed");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (filePost != null) {
                filePost.releaseConnection();
            }
        }
        return result;
    }

    private List<Document> resetUploadFlag(List<Document> documents, String flag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            for (Iterator<Document> iter = documents.iterator(); iter.hasNext(); ) {
                Document document = iter.next();
                document.setUploadFlag(flag);
                session.update(document);
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        }
        return documents;
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }
}
