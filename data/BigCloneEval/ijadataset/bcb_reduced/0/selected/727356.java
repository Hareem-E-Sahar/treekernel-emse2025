package org.webthree.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.excalibur.store.Store;
import org.webthree.store.StoreManager;

public final class FilesystemStoreManager extends AbstractLogEnabled implements Contextualizable, Parameterizable, StoreManager, ThreadSafe {

    protected Context context;

    protected Parameters params = new Parameters();

    protected HashMap storeMapping = new HashMap();

    protected String directoryPath = null;

    public Iterator getStores() {
        File file = new File(this.directoryPath);
        String[] files = file.list();
        Set set = new HashSet();
        for (int i = 0; i < files.length; i++) {
            file = new File(this.directoryPath + File.separator + files[i]);
            if (file.isDirectory() && !OUTBOUND_SYNCDIR.equals(files[i]) && !METADATA_SYNCDIR.equals(files[i]) && !DATABASE_STOREDIR.equals(files[i])) {
                set.add(files[i]);
            }
        }
        return set.iterator();
    }

    /**
     * Contextualize the Component
     *
     * @param  context the Context of the Application
     * @exception  ContextException
     */
    public void contextualize(final Context context) throws ContextException {
        this.context = context;
        org.apache.cocoon.environment.Context ctx = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        String path = ctx.getRealPath("/");
        directoryPath = new File(path).getParentFile().getParentFile().getAbsolutePath() + File.separator + "store";
        try {
            File file = new File(directoryPath);
            if (!file.exists() && !file.mkdir()) throw new IOException("Error creating store directory '" + directoryPath + "': ");
            if (!file.isDirectory()) throw new IOException("'" + directoryPath + "' is not a directory");
            if (!file.canRead() || !file.canWrite()) throw new IOException("Directory '" + directoryPath + "' is not readable/writable");
        } catch (IOException x) {
            this.getLogger().error("Konnte Verzeichnis nicht ordnungsgem�� erstellen!", x);
            throw new ContextException(x.getMessage());
        }
    }

    public void parameterize(Parameters params) throws ParameterException {
        int order = params.getParameterAsInteger("order", 301);
        this.params.setParameter("order", "" + order);
        this.params.setParameter("cacheable", params.getParameter("cacheable", "false"));
    }

    /**
     * Get the store with the given name. The specific with that component is
     * that there are some assertions.
     * assert: the store name contains an underscore because it has the form
     * <partie>_<datum> otherwise an IOException is thrown.
     * 
     */
    public synchronized Store getStore(String name) throws IOException {
        if ((null != name && name.length() > 0 && name.indexOf('_') > 0) || OUTBOUND_SYNCDIR.equals(name) || METADATA_SYNCDIR.equals(name)) {
            if (storeMapping.containsKey(name)) {
                return (FilesystemStore) storeMapping.get(name);
            }
            if (this.hasStore(name)) {
                try {
                    FilesystemStore fs = new FilesystemStore();
                    fs.enableLogging(this.getLogger());
                    fs.contextualize(this.context);
                    this.params.setParameter("directory", this.directoryPath + File.separator + name);
                    fs.parameterize(this.params);
                    fs.initialize();
                    storeMapping.put(name, fs);
                    return fs;
                } catch (Exception x) {
                    this.getLogger().error("FilesystemStore '" + name + "' konnte nicht erstellt werden!");
                    throw new IOException("FilesystemStore '" + name + "' konnte nicht erstellt werden!");
                }
            }
            if (OUTBOUND_SYNCDIR.equals(name)) {
                this.createStore(OUTBOUND_SYNCDIR);
                return (FilesystemStore) storeMapping.get(OUTBOUND_SYNCDIR);
            }
            if (METADATA_SYNCDIR.equals(name)) {
                this.createStore(METADATA_SYNCDIR);
                return (FilesystemStore) storeMapping.get(METADATA_SYNCDIR);
            }
        }
        throw new IOException("Invalid Storename (" + name + ")!");
    }

    /**
     * L�scht den Store vom Speicher. Alle Daten des Stores sind somit verloren.
     */
    public synchronized void deleteStore(String name) {
        if (null != name && name.length() > 0) {
            if (storeMapping.containsKey(name)) {
                ((FilesystemStore) storeMapping.get(name)).close();
                storeMapping.remove(name);
            }
            try {
                File file = new File(this.directoryPath + File.separator + name);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        File list[] = file.listFiles();
                        for (int i = 0; i < list.length; i++) {
                            try {
                                list[i].delete();
                            } catch (Exception x) {
                                this.getLogger().error("Konnte Datei nicht l�schen. Path=" + list[i].getAbsolutePath());
                            }
                        }
                    }
                    file.delete();
                }
            } catch (Exception x) {
                this.getLogger().error("Konnte Datei nicht l�schen. Path=" + this.directoryPath + File.separator + name);
            }
        }
    }

    /**
     * Pr�ft, ob der Store mit Index-Datei bereits existiert.
     */
    public synchronized boolean hasStore(String name) {
        if (null != name && name.length() > 0) {
            if (storeMapping.containsKey(name)) {
                return true;
            } else {
                try {
                    File file = new File(this.directoryPath + File.separator + name);
                    return file.exists();
                } catch (Exception x) {
                    getLogger().error(x.getMessage(), x);
                }
            }
        }
        return false;
    }

    /**
     * Write whole directory into a zip file using naming convention
     * backup_<timestamp>_<partie>_<datum>.zip
     */
    public synchronized void backupStore(String name) throws IOException {
        if (null != name && name.length() > 0) {
            String path = this.directoryPath + File.separator + name;
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                String list[] = file.list();
                byte[] buf = new byte[1024];
                ZipOutputStream out = null;
                String zipFileName = null;
                try {
                    zipFileName = this.directoryPath + File.separator + "backup_" + System.currentTimeMillis() + "_" + name + ".zip";
                    out = new ZipOutputStream(new FileOutputStream(zipFileName));
                } catch (FileNotFoundException x) {
                    getLogger().error("This should never happen", x);
                    throw new IOException("Couldn't get ZipFile (" + zipFileName + ")!");
                }
                for (int i = 0; i < list.length; i++) {
                    try {
                        FileInputStream in = new FileInputStream(path + File.separator + list[i]);
                        out.putNextEntry(new ZipEntry(list[i]));
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                        in.close();
                    } catch (IOException x) {
                        getLogger().error(x.getMessage(), x);
                    }
                }
                out.close();
            } else {
                throw new IOException("Invalid Store (" + name + ")!");
            }
        } else {
            throw new IOException("Invalid Store (" + name + ")!");
        }
    }

    public void createStore(String name) throws IOException {
        if ((null != name && name.length() > 0 && name.indexOf('_') > 0) || OUTBOUND_SYNCDIR.equals(name) || METADATA_SYNCDIR.equals(name)) {
            try {
                if (this.hasStore(name) && !OUTBOUND_SYNCDIR.equals(name) && !METADATA_SYNCDIR.equals(name)) {
                    this.backupStore(name);
                    this.deleteStore(name);
                }
            } catch (Exception x) {
                getLogger().error(x.getMessage(), x);
            }
            try {
                FilesystemStore fs = new FilesystemStore();
                fs.enableLogging(this.getLogger());
                fs.contextualize(this.context);
                this.params.setParameter("directory", this.directoryPath + File.separator + name);
                fs.parameterize(this.params);
                fs.initialize();
                storeMapping.put(name, fs);
            } catch (Exception x) {
                this.getLogger().error("FilesystemStore '" + name + "' konnte nicht erstellt werden!");
                throw new IOException("FilesystemStore '" + name + "' konnte nicht erstellt werden!");
            }
        }
    }
}
