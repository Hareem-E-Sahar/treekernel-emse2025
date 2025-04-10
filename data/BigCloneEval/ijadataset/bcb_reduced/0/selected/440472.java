package fi.kaila.suku.kontroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import fi.kaila.suku.server.SukuServer;
import fi.kaila.suku.server.SukuServerImpl;
import fi.kaila.suku.swing.Suku;
import fi.kaila.suku.util.Resurses;
import fi.kaila.suku.util.SukuException;
import fi.kaila.suku.util.pojo.SukuData;

/**
 * The Class SukuKontrollerLocalImpl.
 * 
 * @author FIKAAKAIL
 * 
 *         Controller class to implement local application
 */
public class SukuKontrollerLocalImpl implements SukuKontroller {

    private static Preferences sr = null;

    private SukuServer server = null;

    private static Logger logger = null;

    private boolean isConnected = false;

    private File file = null;

    private Suku host = null;

    private String schema = null;

    /**
	 * Instantiates a new suku kontroller local impl.
	 * 
	 * @param host
	 *            the host
	 * @throws SukuException
	 *             the suku exception
	 */
    public SukuKontrollerLocalImpl(Suku host) throws SukuException {
        this.host = host;
        this.server = new SukuServerImpl(null, null);
        sr = Preferences.userRoot();
        logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void getConnection(String host, String dbname, String userid, String passwd) throws SukuException {
        isConnected = false;
        this.server.getConnection(host, dbname, userid, passwd);
        isConnected = true;
    }

    @Override
    public void resetConnection() {
        this.server.resetConnection();
        isConnected = false;
    }

    /**
	 * get stored parameter from user preferences.
	 * 
	 * @param o
	 *            (owner name)
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return value
	 */
    @Override
    public String getPref(Object o, String key, String def) {
        return sr.get(o.getClass().getName() + "." + key, def);
    }

    /**
	 * store value in user preferences.
	 * 
	 * @param o
	 *            the o
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
    @Override
    public void putPref(Object o, String key, String value) {
        sr.put(o.getClass().getName() + "." + key, value);
    }

    @Override
    public SukuData getSukuData(String... params) throws SukuException {
        return this.server.getSukuData(params);
    }

    @Override
    public boolean openFile(String filter) {
        Preferences sr = Preferences.userRoot();
        String[] filters = filter.split(";");
        String koe = sr.get(filters[0], ".");
        logger.fine("Hakemisto on: " + koe);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new fi.kaila.suku.util.SettingFilter(filter));
        chooser.setDialogTitle("Open " + filter + " file");
        chooser.setSelectedFile(new File(koe + "/."));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File f = chooser.getSelectedFile();
        if (f == null) {
            return false;
        }
        String filename = f.getAbsolutePath();
        file = new File(filename);
        this.server.setLocalFile(filename);
        logger.info("Valittiin: " + filename);
        String tmp = f.getAbsolutePath().replace("\\", "/");
        int i = tmp.lastIndexOf("/");
        sr.put(filters[0], tmp.substring(0, i));
        return true;
    }

    @Override
    public SukuData getSukuData(SukuData request, String... params) throws SukuException {
        return this.server.getSukuData(request, params);
    }

    @Override
    public long getFileLength() {
        if (file != null) {
            return file.length();
        }
        return 0;
    }

    @Override
    public InputStream getInputStream() {
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                logger.log(Level.WARNING, "Failed to get input stream for file", e);
            }
        }
        return null;
    }

    @Override
    public String getFileName() {
        if (file != null) {
            return file.getName();
        }
        return null;
    }

    @Override
    public boolean createLocalFile(String filter) {
        Preferences sr = Preferences.userRoot();
        String[] filters = filter.split(";");
        String koe = sr.get(filters[0], ".");
        logger.fine("Hakemisto on: " + koe);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new fi.kaila.suku.util.SettingFilter(filter));
        chooser.setDialogTitle("Create " + filter + " file");
        chooser.setSelectedFile(new File(koe + "/."));
        if (chooser.showSaveDialog(host) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File f = chooser.getSelectedFile();
        if (f == null) {
            return false;
        }
        String filename = f.getAbsolutePath();
        if (filename == null) return false;
        if (f.exists()) {
            int answer = JOptionPane.showConfirmDialog(host, Resurses.getString("FILE_EXISTS") + " [" + filename + "] \n" + Resurses.getString("FILE_OVERWRITE"), Resurses.getString(Resurses.SUKU), JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        if (filters.length > 0) {
            int typeIdx = filename.lastIndexOf(".");
            if (typeIdx <= filename.length() - 10) {
                if (!filename.toLowerCase().endsWith(filters[0].toLowerCase())) {
                    filename += "." + filters[0];
                }
            }
        }
        file = new File(filename);
        this.server.setLocalFile(filename);
        logger.info("Valittiin: " + filename);
        String tmp = f.getAbsolutePath().replace("\\", "/");
        int i = tmp.lastIndexOf("/");
        sr.put(filters[0], tmp.substring(0, i));
        return true;
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        if (file != null) {
            return new FileOutputStream(file);
        }
        return null;
    }

    @Override
    public InputStream openLocalFile(String path) {
        if (openFile(path)) {
            if (path != null) {
                if (file == null) return null;
                String mainPath = file.getAbsolutePath();
                try {
                    return new FileInputStream(mainPath);
                } catch (FileNotFoundException e) {
                    logger.log(Level.WARNING, e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public String getFilePath() {
        return file.getAbsolutePath().replace("\\", "/");
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean saveFile(String filter, InputStream in) {
        if (createLocalFile(filter)) {
            try {
                byte[] buffer = new byte[1024];
                int readBytes = 0;
                FileOutputStream fos = new FileOutputStream(file);
                while ((readBytes = in.read(buffer)) >= 0) {
                    fos.write(buffer, 0, readBytes);
                }
                in.close();
                fos.close();
                return true;
            } catch (Exception e) {
                logger.log(Level.INFO, "saveFile", e);
            }
        }
        return false;
    }

    @Override
    public boolean isWebStart() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public String getSchema() {
        if (isConnected) {
            return schema;
        }
        return null;
    }

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }
}
