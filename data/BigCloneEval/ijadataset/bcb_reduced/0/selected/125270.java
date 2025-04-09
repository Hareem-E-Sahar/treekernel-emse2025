package edu.ucdavis.genomics.metabolomics.binbase.bci.server.jmx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.Logger;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.io.Copy;

/**
 * 
 * @author wohlgemuth
 * @version Apr 21, 2006
 * 
 * @jmx.mbean description = "Is needed for the configuration of the import"
 *            name="binbase:service=Import" extends =
 *            "javax.management.MBeanRegistration"
 */
public class ServiceJMX implements ServiceJMXMBean {

    private Logger logger = Logger.getLogger(getClass());

    private Collection dirs = new Vector();

    private Vector databases = new Vector();

    /**
	 * rejects all imports and exports
	 */
    private boolean disableServices = false;

    private boolean validateSources = true;

    public ServiceJMX() {
        super();
    }

    /**
	 * @jmx.managed-operation description = "return all registered directories
	 *                        where sample files can be"
	 * @author wohlgemuth
	 * @version Apr 21, 2006
	 * @return
	 * @throws BinBaseException
	 */
    public Collection getImportDirectories() throws BinBaseException {
        return dirs;
    }

    /**
	 * @jmx.managed-operation description = "adds a directory"
	 * @author wohlgemuth
	 * @version Jul 16, 2006
	 * @param string
	 */
    public void addDirectory(String string) {
        if (string.endsWith(File.separator) == false) {
            string = string + File.separator;
        }
        if (dirs.contains(string) == false) {
            File dir = new File(string);
            if (dir.exists() == false) {
                logger.info("creating directory for import: " + string);
                dir.mkdirs();
            }
            dirs.add(string);
            this.store();
        }
    }

    /**
	 * @jmx.managed-operation description = "removes a directoy"
	 * @author wohlgemuth
	 * @version Jul 16, 2006
	 * @param string
	 */
    public void removeDirectory(String string) {
        dirs.remove(string);
        this.store();
    }

    /**
	 * @jmx.managed-operation description = "delete all directories"
	 * @author wohlgemuth
	 * @version Jul 16, 2006
	 */
    public void clearDirectorys() {
        dirs.clear();
        this.store();
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return null;
    }

    public void postRegister(Boolean registrationDone) {
        try {
            File file = new File(getClass().getName() + ".properties");
            if (!file.exists()) {
                return;
            }
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            this.dirs = (Collection) in.readObject();
            this.disableServices = (Boolean) in.readObject();
            this.validateSources = (Boolean) in.readObject();
            this.databases = (Vector) in.readObject();
        } catch (Exception e) {
            logger.error("postRegister(Boolean)", e);
        }
    }

    /**
	 * @throws IOException
	 * @throws BinBaseException
	 * @jmx.managed-operation description="clustered statistics"
	 */
    public void uploadImportFile(String fileName, byte[] content) throws java.io.FileNotFoundException, IOException, BinBaseException {
        if (this.getImportDirectories().isEmpty()) {
            logger.info("creating directory for netcdf...");
            addDirectory("import");
        }
        this.uploadFileToDir(fileName, (String) getImportDirectories().iterator().next(), content);
    }

    /**
	 * @throws IOException
	 * @throws BinBaseException
	 * @jmx.managed-operation description="clustered statistics"
	 */
    public void uploadConfigFile(String fileName, byte[] content) throws java.io.FileNotFoundException, IOException, BinBaseException {
        File file = new File("config/");
        if (file.exists() == false) {
            file.mkdirs();
        }
        Copy.copy(new ByteArrayInputStream(content), new FileOutputStream("config/" + fileName));
    }

    /**
	 * @throws IOException
	 * @throws BinBaseException
	 * @jmx.managed-operation description="clustered statistics"
	 */
    public Collection listConfigFiles() {
        File file = new File("config/");
        if (file.exists() == false) {
            file.mkdirs();
        }
        File[] files = file.listFiles();
        Collection result = new Vector();
        for (int i = 0; i < files.length; i++) {
            result.add(files[i].getName());
        }
        return result;
    }

    /**
	 * @throws IOException
	 * @throws BinBaseException
	 * @jmx.managed-operation description="clustered statistics"
	 */
    public byte[] getConfigFile(String fileName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        File file = new File("config/" + fileName);
        if (file.exists() == false) {
            throw new IOException("source not found, name was: " + fileName);
        }
        FileInputStream in = new FileInputStream(file);
        Copy.copy(in, out);
        byte[] result = out.toByteArray();
        return result;
    }

    /**
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws BinBaseException
	 * @jmx.managed-operation description="clustered statistics"
	 */
    public void uploadFileToDir(String fileName, String directory, byte[] content) throws FileNotFoundException, IOException {
        if (fileName.toLowerCase().endsWith(".txt") == false) {
            fileName = fileName + ".txt";
        }
        Copy.copy(new ByteArrayInputStream(content), new FileOutputStream(directory + "/" + fileName));
    }

    private void store() {
        try {
            File file = new File(getClass().getName() + ".properties");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(this.getImportDirectories());
            out.writeObject(this.isDisableServices());
            out.writeObject(this.isValidateSources());
            out.writeObject(this.databases);
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
    }

    public void preDeregister() throws Exception {
        store();
    }

    public void postDeregister() {
    }

    /**
	 * @jmx.managed-operation description = "disable the import and export"
	 * @return
	 */
    public boolean isDisableServices() {
        return disableServices;
    }

    /**
	 * @jmx.managed-operation description = "disable the import and export"
	 * @param disableServices
	 */
    public void setDisableServices(boolean disableServices) {
        this.disableServices = disableServices;
        this.store();
    }

    /**
	 * @jmx.managed-operation description = "are we validating if files actually
	 *                        exist"
	 * @return
	 */
    public boolean isValidateSources() {
        return validateSources;
    }

    /**
	 * @jmx.managed-operation description = "are we validating if files actually
	 *                        exist"
	 * @param validateSources
	 */
    public void setValidateSources(boolean validateSources) {
        this.validateSources = validateSources;
        this.store();
    }

    /**
	 * @jmx.managed-operation description = "finds out if the given sample
	 *                        exists on the harddrive"
	 * @param validateSources
	 * @throws BinBaseException
	 */
    public boolean sampleExist(String sampleName) throws BinBaseException {
        Iterator it = this.getImportDirectories().iterator();
        while (it.hasNext()) {
            String dir = it.next().toString();
            File file = new File(generateFileName(dir, sampleName));
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
	 * @jmx.managed-operation description="clustered statistics"
	 * @param sampleName
	 * @return
	 * @throws BinBaseException
	 * @throws IOException
	 */
    public byte[] downloadFile(String sampleName) throws BinBaseException, IOException {
        Iterator it = this.getImportDirectories().iterator();
        while (it.hasNext()) {
            String dir = it.next().toString();
            File file = new File(generateFileName(dir, sampleName));
            if (file.exists()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                FileInputStream in = new FileInputStream(file);
                Copy.copy(in, out);
                byte[] result = out.toByteArray();
                return result;
            }
        }
        return null;
    }

    /**
	 * @jmx.managed-operation description = "generates the sample name"
	 * @param validateSources
	 * @throws BinBaseException
	 */
    public String generateFileName(String dir, String sampleName) {
        if (dir.endsWith("/") == false) {
            dir = dir + "/";
        }
        return dir + sampleName.replace(':', '_') + ".txt";
    }

    /**
	 * @jmx.managed-operation description = "name of the known databases"
	 * @param validateSources
	 * @throws BinBaseException
	 */
    public String[] getDatabases() {
        String[] result = new String[this.databases.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) this.databases.get(i);
        }
        return result;
    }

    /**
	 * @jmx.managed-operation description = "name of the known databases"
	 * @param validateSources
	 * @throws BinBaseException
	 */
    public void addDatabase(String databaseName) {
        if (this.databases.contains(databaseName) == false) {
            this.databases.add(databaseName);
        }
        this.store();
    }

    /**
	 * @jmx.managed-operation description = "name of the known databases"
	 * @param validateSources
	 * @throws BinBaseException
	 */
    public void removeDatabase(String databaseName) {
        this.databases.remove(databaseName);
        this.store();
    }

    /**
	 * @jmx.managed-operation description = "name of the known databases"
	 * @param validateSources
	 * @throws BinBaseException
	 */
    public void resetDatabases() {
        this.databases.clear();
        this.store();
    }
}
