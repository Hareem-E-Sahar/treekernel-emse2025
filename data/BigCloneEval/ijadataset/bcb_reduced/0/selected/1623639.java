package org.exist.storage;

import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.backup.RawDataBackup;
import org.exist.util.Configuration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataBackup implements SystemTask {

    private static final Logger LOG = Logger.getLogger(DataBackup.class);

    public static final SimpleDateFormat creationDateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");

    private String dest;

    public DataBackup() {
    }

    public DataBackup(String destination) {
        dest = destination;
    }

    @Override
    public boolean afterCheckpoint() {
        return true;
    }

    public void configure(Configuration config, Properties properties) throws EXistException {
        dest = properties.getProperty("output-dir", "backup");
        File f = new File(dest);
        if (!f.isAbsolute()) {
            dest = (String) config.getProperty(BrokerPool.PROPERTY_DATA_DIR) + File.separatorChar + dest;
            f = new File(dest);
        }
        if (f.exists() && !(f.canWrite() && f.isDirectory())) throw new EXistException("Cannot write backup files to " + f.getAbsolutePath() + ". It should be a writable directory."); else f.mkdirs();
        dest = f.getAbsolutePath();
        LOG.debug("Setting backup data directory: " + dest);
    }

    public void execute(DBBroker broker) throws EXistException {
        if (!(broker instanceof NativeBroker)) throw new EXistException("DataBackup system task can only be used " + "with the native storage backend");
        LOG.debug("Backing up data files ...");
        String creationDate = creationDateFormat.format(Calendar.getInstance().getTime());
        String outFilename = dest + File.separatorChar + creationDate + ".zip";
        LOG.debug("Archiving data files into: " + outFilename);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            Callback cb = new Callback(out);
            broker.backupToArchive(cb);
            out.close();
        } catch (IOException e) {
            LOG.warn("An IO error occurred while backing up data files: " + e.getMessage(), e);
        }
    }

    private class Callback implements RawDataBackup {

        private ZipOutputStream zout;

        private Callback(ZipOutputStream out) {
            zout = out;
        }

        public OutputStream newEntry(String name) throws IOException {
            zout.putNextEntry(new ZipEntry(name));
            return zout;
        }

        public void closeEntry() throws IOException {
            zout.closeEntry();
        }
    }
}
