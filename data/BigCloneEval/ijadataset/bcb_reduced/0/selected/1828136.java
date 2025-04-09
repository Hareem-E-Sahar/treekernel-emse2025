package org.subrecord.impl.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.subrecord.Hasher;
import org.subrecord.RecordManager;
import org.subrecord.exception.StorageException;
import org.subrecord.model.Record;
import org.subrecord.model.Value;
import org.subrecord.util.Commons;

/**
 * File storage implementation. Each record is located in the following way:
 * $dataRoot/hash($domain)/hash($table)/hash($key)/$key.zip when hash function
 * result is some number. Each property/value is a zip entry and the whole
 * record is thus compressed. The record itself is just a serialized stream of
 * bytes. From filesystem's perspective it is just a regular human-readable and
 * human-accessible directory/file structure and each record is a valid ZIP file
 * one can view.
 * 
 * @author przemek
 * 
 */
public class FileRecordManager implements RecordManager {

    protected static final Logger LOG = Logger.getLogger(FileRecordManager.class);

    private Hasher<Long> hasher;

    private String dataRoot;

    public FileRecordManager(String dataRoot, Hasher<Long> hasher) {
        this.hasher = hasher;
        this.dataRoot = dataRoot;
    }

    public boolean putRecord(Serializable domain, Serializable table, Serializable key, Record record) throws StorageException {
        try {
            String recordPath = createDataPath(domain, table, key);
            boolean succeded = new File(recordPath).mkdirs();
            String recordFile = Commons.glue(recordPath, "/", key, ".zip");
            LOG.debug(recordFile);
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File(recordFile)));
            try {
                for (Map.Entry<String, Value> entry : record.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    ObjectOutputStream os = new ObjectOutputStream(zip);
                    os.writeObject(entry.getValue());
                    zip.closeEntry();
                }
            } finally {
                zip.close();
            }
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return true;
    }

    public Record getRecord(Serializable domain, Serializable table, Serializable key) throws StorageException {
        try {
            String recordPath = createDataPath(domain, table, key);
            File record = new File(Commons.glue(recordPath, "/", key, ".zip"));
            if (!record.exists()) {
                throw new StorageException("Record not found, key=" + key);
            }
            LOG.debug("Path to record: " + record);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(record));
            Record map = new Record();
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                ObjectInputStream is = new ObjectInputStream(zip);
                Object o = is.readObject();
                map.put(entry.getName(), (Value) o);
                entry = zip.getNextEntry();
            }
            zip.close();
            return map;
        } catch (IOException e) {
            throw new StorageException(e);
        } catch (ClassNotFoundException e) {
            throw new StorageException(e);
        }
    }

    public boolean removeRecord(Serializable domain, Serializable table, Serializable key) throws StorageException {
        String recordPath = createDataPath(domain, table, key);
        boolean succeded = new File(recordPath).mkdirs();
        String recordFile = Commons.glue(recordPath, "/", key, ".zip");
        LOG.debug(recordFile);
        new File(recordFile).delete();
        return true;
    }

    private String createDataPath(Serializable domain, Serializable table, Serializable key) {
        return Commons.glue(dataRoot, "/", hasher.hash(domain), "/", hasher.hash(table), "/", hasher.hash(key));
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }
}
