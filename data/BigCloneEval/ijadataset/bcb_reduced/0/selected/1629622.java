package bones.doc.file;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import bones.doc.Archive;
import bones.doc.Context;
import bones.doc.Doc;
import bones.doc.Resource;
import bones.process.BonesSystemException;

/**
 * class FileArchive.java.
 * Simply match a folder/ a jar.
 */
public class FileArchive implements Archive {

    private Context parent;

    private String url;

    private long id;

    private FileDoc byId;

    private ResourceCache content = new ResourceCache();

    ;

    /**
	 * Constructor for FileArchive.
	 */
    public FileArchive(Context parent, String url) {
        super();
        this.parent = parent;
        this.url = url;
        try {
            byId = new FileDoc(this, "DocById");
        } catch (BonesSystemException e) {
        }
    }

    /**
	 * @see bones.doc.Archive#parent()
	 */
    public Resource parent() {
        return parent;
    }

    private Doc getFromCache(String name) {
        Doc doc;
        if ((doc = (Doc) content.getByName(name)) == null) {
            try {
                doc = new FileDoc(this, name);
            } catch (BonesSystemException e) {
                return null;
            }
            content.set(doc);
        }
        return doc;
    }

    /**
	 * @see bones.doc.Archive#getDoc(String)
	 */
    public Doc getDoc(String name) {
        return getFromCache(name);
    }

    /**
	 * @see bones.doc.Archive#getDoc(long)
	 */
    public Doc getDoc(long id) {
        return null;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        try {
            return new URL(url).getPath();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public String getUrl() {
        return url;
    }

    public boolean save() {
        try {
            FileOutputStream w = new FileOutputStream(url);
            JarOutputStream jar = new JarOutputStream(w);
            Resource[] values = content.getArray();
            for (int i = 0; i < values.length; i++) {
                ZipEntry ze = new ZipEntry(values[i].getName());
                jar.putNextEntry(ze);
                jar.flush();
                ObjectOutputStream oos = new ObjectOutputStream(jar);
                oos.writeObject(values[i]);
                jar.closeEntry();
                jar.close();
            }
            return true;
        } catch (FileNotFoundException e) {
            DocLog.INSTANCE.log("FileDoc", e);
            return false;
        } catch (IOException e) {
            DocLog.INSTANCE.log("FileDoc", e);
            return false;
        }
    }
}
