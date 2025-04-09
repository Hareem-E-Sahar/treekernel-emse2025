package edu.columbia.hypercontent.engine;

import edu.columbia.hypercontent.RequestTracker;
import edu.columbia.hypercontent.FileHolder;
import edu.columbia.hypercontent.Project;
import edu.columbia.hypercontent.DocumentFactory;
import edu.columbia.filesystem.IFileSystemManager;
import edu.columbia.filesystem.File;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: Oct 24, 2003
 * Time: 9:00:47 AM
 * To change this template use Options | File Templates.
 */
public class ZippingWorkerTask extends BaseWorker {

    ZipOutputStream zip;

    RequestTracker tracker;

    String path;

    IFileSystemManager source;

    boolean metadata;

    public ZippingWorkerTask(RequestTracker requestTracker, IFileSystemManager source, String path, ZipOutputStream zip, boolean metadata) {
        this.zip = zip;
        tracker = requestTracker;
        this.path = path;
        this.source = source;
        this.metadata = metadata;
    }

    protected boolean isOutputUpToDate() {
        return false;
    }

    public void run() {
        try {
            File file = source.getFile(path);
            ZipEntry entry = new ZipEntry(path.substring(1));
            entry.setTime(source.getLastModified(path).getTime());
            entry.setComment(file.getCreator());
            zip.putNextEntry(entry);
            file.write(zip);
            zip.closeEntry();
            if (metadata) {
                byte[] metabytes = new String(DocumentFactory.getRDFChars(file.getMetaData())).getBytes("UTF-8");
                entry = new ZipEntry(path.substring(1) + ".rdf");
                entry.setTime(source.getLastModified(path).getTime());
                zip.putNextEntry(entry);
                zip.write(metabytes);
                zip.closeEntry();
            }
            success();
        } catch (Exception e) {
            fatalError(e, null);
        }
    }
}
