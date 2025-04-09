package net.sourceforge.mords.docs.server.store;

import com.tdcs.docs.common.IndexObj;
import com.tdcs.docs.common.Document;
import com.tdcs.docs.common.DocumentObject;
import com.tdcs.docs.common.Index;
import com.tdcs.docs.common.DocServer;
import com.tdcs.docs.common.MetaData;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * @author david
 */
public class DocServerEngine extends UnicastRemoteObject implements DocServer {

    private Properties props;

    private String baseLine;

    private String templatePath;

    private String archivePath;

    /** Creates a new instance of DocServerEngine */
    public DocServerEngine(Properties props) throws RemoteException {
        super();
        this.props = props;
        baseLine = props.getProperty("base", ".");
        templatePath = props.getProperty("templates", ".");
        archivePath = props.getProperty("archive", ".");
    }

    public Map<String, String> listTemplates(Index index) throws RemoteException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        try {
            File templateDir = new File(templatePath);
            if (templateDir.isDirectory() && templateDir.exists()) {
                Iterator<String> it = index.getCategories().iterator();
                while (it.hasNext()) {
                    String cat = it.next();
                    String path = templatePath + File.separator + cat;
                    File catDir = new File(path);
                    File[] templates = catDir.listFiles();
                    for (int i = 0; i < templates.length; i++) {
                        map.put(templates[i].getName(), cat);
                    }
                }
            } else {
                throw new RemoteException("Template path does not exist.");
            }
        } catch (IOException ioe) {
            throw new RemoteException("Unable to read templates", ioe);
        }
        return map;
    }

    public Document getTemplate(String category, String templateId) throws RemoteException {
        DocumentObject doc = new DocumentObject(false);
        try {
            doc.setName(templateId);
            doc.setData(getData(templatePath + File.separator + File.separator + category + File.separator + templateId));
        } catch (FileNotFoundException fnfe) {
            throw new RemoteException("\"" + templateId + "\" was not found.", fnfe);
        } catch (IOException ioe) {
            throw new RemoteException("Problem reading \"" + templateId + "\"");
        }
        return doc;
    }

    public Document getDocument(Index index, String category, String id) throws RemoteException {
        RmiDocument doc = new RmiDocument(index, baseLine, category, id);
        MetaData mtdt = new RmiMetaData(props, index, category, id);
        doc.setMetaData(mtdt);
        return doc;
    }

    public void add(Document doc) throws RemoteException {
        try {
            FileOutputStream fos = new FileOutputStream(makePath(doc));
            fos.write(doc.getData());
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            throw new RemoteException("Unable to write file.", ioe);
        }
    }

    public void remove(Document doc) throws RemoteException {
        try {
            File f = new File(makePath(doc));
            boolean done = f.delete();
            if (!done) {
                throw new RemoteException("Unable to delete the document.");
            }
        } catch (IOException ioe) {
            throw new RemoteException("Problem deleting the document.");
        }
    }

    public Map<String, String> listDocs(Index index) throws RemoteException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        Iterator<String> it = index.getCategories().iterator();
        while (it.hasNext()) {
            String category = it.next();
            Collection<String> files = listDocs(index, category);
            Iterator<String> itf = files.iterator();
            while (itf.hasNext()) {
                map.put(itf.next(), category);
            }
        }
        return map;
    }

    public Collection<String> listDocs(Index index, String category) throws RemoteException {
        Vector<String> list = new Vector<String>();
        try {
            File baseDir = new File(baseLine + File.separator + index.getIndex() + File.separator + category);
            System.out.println("Looking into " + baseDir.getAbsolutePath());
            if (!baseDir.isDirectory() || !baseDir.canRead()) {
                throw new FileNotFoundException(category + " does not point to a readable directory.");
            }
            File[] files = baseDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                list.add(files[i].getName());
            }
        } catch (FileNotFoundException fnfe) {
            throw new RemoteException("Missing file.", fnfe);
        } catch (IOException ioe) {
            throw new RemoteException("Unable to read files.", ioe);
        }
        return list;
    }

    public Collection<String> listIndices() throws RemoteException {
        Vector<String> v = new Vector<String>();
        try {
            File f = new File(baseLine);
            File[] list = f.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    v.add(list[i].getName());
                }
            }
        } catch (Exception e) {
            throw new RemoteException("Problem reading indices.", e);
        }
        return v;
    }

    public Index getIndex(String indexValue) throws RemoteException {
        IndexObj index = new IndexObj(indexValue);
        try {
            File f = new File(baseLine + File.separator + indexValue);
            File[] list = f.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    index.addCategory(list[i].getName());
                }
            }
        } catch (SecurityException se) {
            throw new RemoteException("Problem loading index data.", se);
        }
        return index;
    }

    public void add(Index index) throws RemoteException {
        try {
            File f = new File(baseLine + File.separator + index.getIndex());
            boolean indexDone = f.mkdir();
            if (!indexDone) {
                throw new RemoteException("Unable to create index folder.");
            }
            String indexPath = f.getAbsolutePath();
            File tempDir = new File(templatePath);
            File[] tmps = tempDir.listFiles();
            for (int i = 0; i < tmps.length; i++) {
                File currT = tmps[i];
                if (currT.isDirectory()) {
                    String newSub = indexPath + File.separator + currT.getName();
                    File newFile = new File(newSub);
                    try {
                        newFile.mkdirs();
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
            }
        } catch (IOException ioe) {
            throw new RemoteException("Problem creating index folders.", ioe);
        }
        RmiMetaData mtdt = new RmiMetaData(props, index, null, null);
        mtdt.close();
    }

    public boolean archive(Index index, Properties props) throws RemoteException {
        return false;
    }

    public void remove(Index index) throws RemoteException {
        try {
            File f = new File(baseLine + File.separator + index.getIndex());
            deleteFiles(f);
        } catch (IOException ioe) {
            throw new RemoteException("Problem deleting files.", ioe);
        }
    }

    public String toString() {
        String val = "Document Server";
        return val;
    }

    protected byte[] getData(String path) throws FileNotFoundException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(path);
        int i = 0;
        while ((i = fis.read()) != -1) {
            baos.write(i);
        }
        fis.close();
        return baos.toByteArray();
    }

    protected void writeData(String path, byte[] data) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    protected String makePath(Document doc) throws RemoteException {
        String val = baseLine + File.separator + doc.getIndex().getIndex() + File.separator + doc.getCategory() + File.separator + doc.getId();
        return val;
    }

    protected void deleteFiles(File f) throws IOException {
        if (!f.isDirectory()) {
            f.delete();
        } else {
            File[] list = f.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteFiles(f);
            }
        }
    }

    public Collection<String> listTemplates() throws RemoteException {
        Vector<String> v = new Vector<String>();
        try {
            File templateDir = new File(templatePath);
            File[] templates = templateDir.listFiles();
            for (int i = 0; i < templates.length; i++) {
                v.add(templates[i].getName());
            }
        } catch (Exception e) {
            throw new RemoteException("Unable to read templates", e);
        }
        return v;
    }

    public Document getTemplate(String templateId) throws RemoteException {
        DocumentObject doc = new DocumentObject(false);
        try {
            doc.setName(templateId);
            doc.setData(getData(templatePath + templateId));
        } catch (FileNotFoundException fnfe) {
            throw new RemoteException("\"" + templateId + "\" was not found.", fnfe);
        } catch (IOException ioe) {
            throw new RemoteException("Problem reading \"" + templateId + "\"");
        }
        return doc;
    }
}
