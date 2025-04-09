package org.subrecord.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.subrecord.Hasher;
import org.subrecord.RecordManager;
import org.subrecord.exception.StorageException;
import org.subrecord.util.Commons;

/**
 * File storage implementation. Each record is located in the following way:
 * $dataRoot/hash($domain)/hash($table)/hash($key)/$key.zip each property is a
 * zip entry and the whole record is compressed. The record itself is just a
 * serialzed stream of bytes. From filesystem perspective it is just
 * human-readable and human-accessible directory/file structure and each record
 * is a regular ZIP file one can view.
 * 
 * @author przemek
 * 
 */
public class FileRecordManager implements RecordManager {

    protected static final Logger LOG = Logger.getLogger(FileRecordManager.class);

    private Hasher hasher;

    private String dataRoot;

    public FileRecordManager(String dataRoot, Hasher hasher) {
        this.hasher = hasher;
        this.dataRoot = dataRoot;
    }

    public boolean putRecord(Serializable domain, Serializable table, Serializable key, Map<String, Serializable> record) throws Exception {
        String recordPath = createDataPath(domain, table, key);
        boolean succeded = new File(recordPath).mkdirs();
        String recordFile = Commons.glue(recordPath, "/", key, ".zip");
        LOG.debug(recordFile);
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File(recordFile)));
        try {
            for (Map.Entry<String, Serializable> entry : record.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                ObjectOutputStream os = new ObjectOutputStream(zip);
                os.writeObject(entry.getValue());
                zip.closeEntry();
            }
        } finally {
            zip.close();
        }
        return true;
    }

    public Map<String, Serializable> getRecord(Serializable domain, Serializable table, Serializable key) throws Exception {
        String recordPath = createDataPath(domain, table, key);
        File record = new File(Commons.glue(recordPath, "/", key, ".zip"));
        if (!record.exists()) {
            throw new StorageException("Record not found, key=" + key);
        }
        LOG.debug("path to record: " + record);
        ZipInputStream zip = new ZipInputStream(new FileInputStream(record));
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            ObjectInputStream is = new ObjectInputStream(zip);
            Object o = is.readObject();
            map.put(entry.getName(), (Serializable) o);
            entry = zip.getNextEntry();
        }
        zip.close();
        return map;
    }

    public boolean removeRecord(Serializable domain, Serializable table, Serializable key) throws Exception {
        String recordPath = createDataPath(domain, table, key);
        boolean succeded = new File(recordPath).mkdirs();
        String recordFile = Commons.glue(recordPath, "/", key, ".zip");
        LOG.debug(recordFile);
        new File(recordFile).delete();
        return true;
    }

    private String createDataPath(Serializable domain, Serializable table, Serializable key) throws Exception {
        return Commons.glue(dataRoot, "/", hasher.hash(domain), "/", hasher.hash(table), "/", hasher.hash(key));
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }
}
