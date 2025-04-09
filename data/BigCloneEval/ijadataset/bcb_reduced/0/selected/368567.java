package edu.columbia.hypercontent.engine;

import edu.columbia.hypercontent.*;
import edu.columbia.hypercontent.contentmanager.CMSessionData;
import edu.columbia.hypercontent.contentmanager.Download;
import edu.columbia.filesystem.IFileSystemManager;
import edu.columbia.filesystem.File;
import edu.columbia.filesystem.FileSystemException;
import edu.columbia.filesystem.impl.IOFileDataLoader;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.*;
import org.jasig.portal.AuthorizationException;

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: Oct 24, 2003
 * Time: 8:50:54 AM
 * To change this template use Options | File Templates.
 */
public class ZippingRequestImpl extends BaseWorker implements IRequest, ICMSConstants {

    final IFileSystemManager source;

    final String path;

    final ZipOutputStream zip;

    final boolean metadata;

    final CMSessionData session;

    String name;

    RequestTracker tracker;

    protected String requestID;

    protected static Random random = new Random();

    public ZippingRequestImpl(IFileSystemManager source, CMSessionData session, String path, OutputStream toStream, boolean metadata) {
        zip = new ZipOutputStream(toStream);
        this.source = source;
        this.path = path;
        this.metadata = metadata;
        this.session = session;
        name = path;
        if (name.endsWith(SEPARATOR)) {
            name = name.substring(0, name.length() - 1);
        }
        name = name.substring(name.lastIndexOf(SEPARATOR) + 1);
        if (name.trim().length() == 0) {
            name = session.global.getProject().getKey();
            int lastslash = name.lastIndexOf(SEPARATOR);
            if (lastslash > 0) {
                name = name.substring(lastslash + 1);
            }
            int dot = name.lastIndexOf(".");
            if (dot > 0) {
                name = name.substring(0, dot);
            }
        }
        name = name + ".zip";
        generateID();
    }

    protected void generateID() {
        StringBuffer buf = new StringBuffer("Request ").append(random.nextLong()).append(" (User ").append(session.userLockKey).append(", project ").append(session.global.getProject().getKey());
        if (path != null) {
            buf.append(", node ").append(path);
        }
        buf.append(")");
        this.requestID = buf.toString();
    }

    public String getID() {
        return requestID;
    }

    public void setRequestTracker(RequestTracker tracker) {
        this.tracker = tracker;
    }

    protected void getFileWorker(String path, ArrayList work) throws CMSException {
        if (session.global.hasPermission(READ_PERMISSION, path)) {
            work.add(new ZippingWorkerTask(tracker, source, path, zip, metadata));
        }
    }

    protected void getWorkersRecursive(String dir, ArrayList work) throws FileSystemException, IOException, CMSException {
        if (session.global.hasPermission(READ_PERMISSION, dir)) {
            ZipEntry entry = new ZipEntry(dir.substring(1));
            entry.setMethod(entry.STORED);
            entry.setCompressedSize(0);
            entry.setCrc(0);
            entry.setSize(0);
            entry.setTime(source.getLastModified(dir).getTime());
            zip.putNextEntry(entry);
            zip.closeEntry();
            String[] fs = source.listFiles(dir);
            String[] ids = source.listDirectories(dir);
            for (int i = 0; i < fs.length; i++) {
                getFileWorker(fs[i], work);
            }
            for (int i = 0; i < ids.length; i++) {
                getWorkersRecursive(ids[i], work);
            }
        }
    }

    public void cleanup() {
        try {
            zip.finish();
            zip.flush();
            zip.close();
            session.nextDownload = new Download(name, new IOFileDataLoader(session.zipFile), true);
            session.zipFile = null;
        } catch (Exception e) {
        }
    }

    public void run() {
        try {
            ArrayList work = new ArrayList();
            if (source.isDirectory(path)) {
                getWorkersRecursive(path, work);
            } else {
                getFileWorker(path, work);
            }
            BaseWorker[] workers = (BaseWorker[]) work.toArray(new BaseWorker[0]);
            tracker.setWorkers(workers);
            success();
        } catch (Exception e) {
            fatalError(e, path);
        }
    }
}
