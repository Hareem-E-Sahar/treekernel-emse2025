package net.frontlinesms.resources;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.*;
import net.frontlinesms.Utils;
import org.apache.log4j.Logger;

/**
 * Utility methods used for loading resources from the file system. 
 * @author Alex
 */
public class ResourceUtils {

    /** System property: user.home */
    public static final String SYSPROPERTY_USER_HOME = "user.home";

    /** Logging object for this class */
    private static Logger LOG = Utils.getLogger(ResourceUtils.class);

    /** The size of byte buffers used in this class. */
    private static final int BUFFER_SIZE = 2048;

    /** Name of directory that discarded resources are put in after an upgrade of FrontlineSMS. */
    private static final String GRAVEYARD = "old";

    /** The location of {@link UserHomeFilePropertySet} files. */
    public static final String PROPERTIES_DIRECTORY_NAME = "properties";

    /** The filename extension used for {@link UserHomeFilePropertySet} files. */
    private static final String PROPERTIES_EXTENSION = ".properties";

    /** Name of the FrontlineSMS resource initialisation file. */
    private static final String RESOURCE_INI_FILE = "frontlinesms.ini";

    /** Property key within # for the location of the resources directory */
    private static final String PROPKEY_RESOURCE_PATH = "resources.path";

    /** Location of resources for this instance of FrontlineSMS.  This is set via the method {#getConfigDirectoryPath()} the first time we try to access the field. */
    private static String resourcePath;

    /** @return the user home path */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
	 * Unzips a compressed archive to the specified output directory.  The archive's directory
	 * structure is rebuilt in the output directory if it does not already exist.  Optionally,
	 * old versions of files can be kept if they are present.
	 * @param inputArchive
	 * @param outputDirectory
	 * @param overwrite
	 * @throws IOException
	 */
    public static final void unzip(File inputArchive, File outputDirectory, boolean overwrite) throws IOException {
        if (!inputArchive.exists() || !inputArchive.isFile()) throw new IllegalArgumentException("Input archive not found: " + inputArchive.getPath());
        if (!outputDirectory.exists() || !outputDirectory.isDirectory()) throw new IllegalArgumentException("Output directory does not exist: " + outputDirectory.getPath());
        unzip(new FileInputStream(inputArchive), outputDirectory, overwrite);
    }

    /**
	 * Unzips a {@link ZipInputStream} to a directory.  If unzipped files already exist in the destination
	 * directory, they can be optionally overridden.
	 * @param inputArchiveAsStream
	 * @param outputDirectory
	 * @param overwriteOverwriteables
	 * @throws IOException
	 */
    public static final void unzip(InputStream inputArchiveAsStream, File outputDirectory, boolean overwriteOverwriteables) throws IOException {
        ZipInputStream in = new ZipInputStream(new BufferedInputStream(inputArchiveAsStream));
        byte[] buffer = new byte[BUFFER_SIZE];
        ZipEntry entry;
        String graveyardName = GRAVEYARD + "_" + generateGraveyardTimestamp() + File.separator;
        while ((entry = in.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                boolean remove = false;
                File outputFile = new File(outputDirectory, entry.getName());
                createDirectoryTree(outputFile);
                if (outputFile.exists() && overwriteOverwriteables && isOverwriteable(outputFile)) {
                    File graveyard = new File(outputFile.getParentFile(), graveyardName);
                    graveyard.mkdir();
                    File destination = new File(graveyard, outputFile.getName());
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destination), BUFFER_SIZE);
                    ResourceUtils.stream2stream(new FileInputStream(outputFile), out, buffer);
                    out.close();
                    remove = true;
                }
                if (!outputFile.exists() || remove) {
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile), BUFFER_SIZE);
                    ResourceUtils.stream2stream(in, out, buffer);
                    out.close();
                }
            }
        }
        in.close();
    }

    /**
	 * Generate a timestamp to be appended to the graveyard directories' names.
	 * @return string timestamp in the form YYYYMMDDHHSSMM
	 */
    private static String generateGraveyardTimestamp() {
        Calendar cal = Calendar.getInstance();
        return "" + Integer.toString(10000 + cal.get(Calendar.YEAR)).substring(1) + Integer.toString(100 + (cal.get(Calendar.MONTH) + 1)).substring(1) + Integer.toString(100 + cal.get(Calendar.DAY_OF_MONTH)).substring(1) + Integer.toString(100 + cal.get(Calendar.HOUR_OF_DAY)).substring(1) + Integer.toString(100 + cal.get(Calendar.MINUTE)).substring(1) + Integer.toString(1000 + cal.get(Calendar.MILLISECOND)).substring(1);
    }

    /**
	 * Checks if a configuration file should be over-ridden by a new version when FrontlineSMS is upgraded. 
	 * @param outputFile
	 * @return <code>true</code> if the supplied file should be overwritten; <code>false</code> otherwise
	 */
    private static boolean isOverwriteable(File outputFile) {
        return true;
    }

    /**
	 * Creates a directory and all directories above it.  This method calls itself recursively
	 * in order to create the directory at the top of a tree first.
	 * @param file
	 */
    public static void createDirectoryTree(File file) {
        file = file.getParentFile();
        if ((!file.exists() || !file.isDirectory())) {
            createDirectoryTree(file);
            boolean success = file.mkdir();
            if (!success) LOG.warn("Failed to create directory: " + file.getAbsolutePath());
        }
    }

    /**
	 * Zips the contents of a directory into a new archive.
	 * @param dataDirectoryPath
	 * @param outputArchive
	 * @throws IOException
	 */
    public static void zip(String dataDirectoryPath, File outputArchive) throws IOException {
        File dataDirectory = new File(dataDirectoryPath);
        LOG.debug("Bundling: " + dataDirectory.getPath());
        LOG.debug("      to: " + outputArchive.getPath());
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputArchive)));
        if (!dataDirectory.exists() || !dataDirectory.isDirectory()) throw new IllegalArgumentException("Not a directory: " + dataDirectory.getPath());
        byte[] buffer = new byte[ResourceUtils.BUFFER_SIZE];
        addDirectoryToZip(out, dataDirectory, dataDirectoryPath, buffer);
        out.close();
    }

    /**
	 * Recursively adds a directory and its contents to a {@link ZipOutputStream}.
	 * @param out The {@link ZipOutputStream} to zip the directory to
	 * @param directory directory to zip
	 * @param rootDirectory 
	 * @param buffer
	 * @throws IOException
	 */
    private static final void addDirectoryToZip(ZipOutputStream out, File directory, String rootDirectory, byte[] buffer) throws IOException {
        LOG.debug("Adding dir to zip: " + directory.getPath());
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) addDirectoryToZip(out, file, rootDirectory, buffer); else addFileToZip(out, file, rootDirectory, buffer);
        }
    }

    /**
	 * Adds a file to a ZipOutputStream.
	 * @param out
	 * @param file
	 * @param rootDirectory base directory being zipped. Necessary here so that the zipped file can be given a relative path
	 * @param buffer
	 * @throws IOException
	 */
    private static final void addFileToZip(ZipOutputStream out, File file, String rootDirectory, byte[] buffer) throws IOException {
        LOG.debug("Adding file to zip: " + file.getPath());
        ZipEntry entry = new ZipEntry(file.getPath().substring(rootDirectory.length()));
        out.putNextEntry(entry);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
        ResourceUtils.stream2stream(in, out, buffer);
        in.close();
    }

    /**
	 * Writes the entire contents of an InputStream to an OutputStream.
	 * @param in
	 * @param out
	 * @param buffer
	 * @throws IOException
	 */
    public static final void stream2stream(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    /**
	 * Loads a list from a textfile, ignoring any blank lines, or lines that
	 * start with a # character.
	 * 
	 * FIXME this appears to be charset dependent, which is very naughty
	 * 
	 * @param filename
	 * @return array of lines containing useful data from the supplied file
	 */
    public static final String[] getUsefulLines(String filename) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            fis = new FileInputStream(filename);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && line.charAt(0) != '#') {
                    lines.add(line);
                }
            }
        } catch (IOException ex) {
            LOG.debug("Error reading file '" + filename + "'", ex);
        } finally {
            if (br != null) try {
                br.close();
            } catch (IOException ex) {
            }
            if (isr != null) try {
                isr.close();
            } catch (IOException ex) {
            }
            if (fis != null) try {
                fis.close();
            } catch (IOException ex) {
            }
        }
        return lines.toArray(new String[lines.size()]);
    }

    /** Gets the directory containing the properties files. */
    public static File getPropertiesDirectory() {
        return new File(getConfigDirectoryPath(), PROPERTIES_DIRECTORY_NAME);
    }

    /**
	 * Gets the path to the configuration directory in which languages, conf, and properties directories all lie.
	 * @return path to the directory containing resources for FrontlineSMS
	 */
    public static synchronized String getConfigDirectoryPath() {
        if (resourcePath == null) {
            try {
                File resourceLocationsFile = new File(RESOURCE_INI_FILE);
                HashMap<String, String> resourceLocation = FilePropertySet.loadPropertyMap(resourceLocationsFile);
                resourcePath = resourceLocation.get(PROPKEY_RESOURCE_PATH);
            } catch (Throwable t) {
                LOG.warn("Problem locating resource path property.", t);
            }
            if (resourcePath == null) {
                resourcePath = getUserHome() + File.separatorChar + "FrontlineSMS" + File.separatorChar;
            }
            if (resourcePath.charAt(resourcePath.length() - 1) != File.separatorChar) {
                resourcePath += File.separatorChar;
            }
        }
        return resourcePath;
    }

    /**
	 * Gets the path of the file where a {@link UserHomeFilePropertySet} is persisted.
	 * @param propertySetName
	 * @return the path to a particular property file
	 */
    protected static final File getPropertiesFile(String propertySetName) {
        return new File(ResourceUtils.getConfigDirectoryPath() + PROPERTIES_DIRECTORY_NAME + File.separatorChar + propertySetName + PROPERTIES_EXTENSION);
    }
}
