package org.obo.dataadapter;

import java.util.zip.*;
import org.bbop.dataadapter.*;
import org.bbop.io.IOUtil;
import org.bbop.io.ProgressableInputStream;
import org.obo.datamodel.*;
import java.io.*;
import org.apache.log4j.*;

public class SerialAdapter implements OBOAdapter {

    protected static final Logger logger = Logger.getLogger(SerialAdapter.class);

    protected String path;

    protected AdapterConfiguration config;

    protected ProgressableInputStream pfis;

    protected boolean cancelled = false;

    public DataAdapterUI getPreferredUI() {
        FileAdapterUI ui = new FileAdapterUI();
        ui.setReadOperation(READ_ONTOLOGY);
        ui.setWriteOperation(WRITE_ONTOLOGY);
        return ui;
    }

    public void cancel() {
        try {
            cancelled = true;
            if (pfis != null) pfis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public AdapterConfiguration getConfiguration() {
        return config;
    }

    public Object doOperation(IOOperation op, AdapterConfiguration oldconfig, Object o) throws DataAdapterException {
        this.config = oldconfig;
        cancelled = false;
        if (op.equals(READ_ONTOLOGY)) {
            if (oldconfig instanceof FileAdapterConfiguration) {
                FileAdapterConfiguration config = (FileAdapterConfiguration) oldconfig;
                if (config.getReadPaths().size() == 1) {
                    path = (String) config.getReadPaths().iterator().next();
                    try {
                        return getRoot();
                    } catch (DataAdapterException ex) {
                        if (cancelled) throw new CancelledAdapterException(); else throw ex;
                    }
                }
            }
            throw new DataAdapterException("Bad configuration");
        } else if (op.equals(WRITE_ONTOLOGY)) {
            if (oldconfig instanceof FileAdapterConfiguration) {
                FileAdapterConfiguration config = (FileAdapterConfiguration) oldconfig;
                path = config.getWritePath();
                write((OBOSession) o);
                return o;
            } else throw new DataAdapterException("Bad configuration");
        }
        return null;
    }

    public String getID() {
        return "OBO:Serial";
    }

    public String getName() {
        return "OBO Serial Adapter";
    }

    public IOOperation[] getSupportedOperations() {
        IOOperation[] supported = { WRITE_ONTOLOGY, READ_ONTOLOGY };
        return supported;
    }

    public String getProgressString() {
        if (pfis != null) return pfis.getProgressString(); else return "";
    }

    public Number getProgressValue() {
        if (pfis != null) return pfis.getProgressValue(); else return 0;
    }

    public OBOSession getRoot() throws DataAdapterException {
        ZipInputStream zipstream;
        try {
            pfis = IOUtil.getProgressableStream(path);
            zipstream = new ZipInputStream(new BufferedInputStream(pfis));
            zipstream.getNextEntry();
        } catch (IOException e) {
            throw new DataAdapterException("Load error", e);
        }
        ObjectInputStream stream;
        try {
            stream = new ObjectInputStream(zipstream);
        } catch (IOException e) {
            throw new DataAdapterException("Could not load file - the selected file is " + "probably not in the correct format", e);
        }
        OBOSession history;
        try {
            history = (OBOSession) stream.readObject();
            history.setLoadRemark(IOUtil.getShortName(path));
            return history;
        } catch (Exception e) {
            throw new DataAdapterException("Load error", e);
        }
    }

    public OBOSession write(OBOSession history) throws DataAdapterException {
        try {
            ZipOutputStream zipstream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
            ZipEntry entry = new ZipEntry("main");
            zipstream.putNextEntry(entry);
            zipstream.setLevel(5);
            ObjectOutputStream stream = new ObjectOutputStream(zipstream);
            stream.writeObject(history);
            stream.close();
            return history;
        } catch (Exception e) {
            throw new DataAdapterException(e, "Write error");
        }
    }

    public String getTermText(IdentifiedObject term) throws DataAdapterException {
        final StringBuffer buffer = new StringBuffer();
        OutputStream os = new OutputStream() {

            @Override
            public void write(int b) {
                buffer.append((char) b);
            }
        };
        try {
            ObjectOutputStream stream = new ObjectOutputStream(os);
            stream.writeObject(os);
        } catch (IOException ex) {
        }
        return buffer.toString();
    }
}
