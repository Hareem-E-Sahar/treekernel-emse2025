package org.xaware.shared.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xaware.shared.util.logging.XAwareLogger;

/**
 * Unzip utility for extracting jar files to a specified location identified by toDir. toDir defaults to c:\temp.
 * setToDir() should be used to set the output location. If toDir does not exist and exception will be thrown when
 * attempting to extract the jar file.
 * 
 * @author dwieland
 */
public class Zip {

    protected static XAwareLogger lf = XAwareLogger.getXAwareLogger("org.xaware.shared.util.Zip");

    protected static final String className = "Zip";

    protected Hashtable nameVsFullName = null;

    protected static String baseDir = "";

    protected final String zipFile;

    protected static final int BUFFER = 2048;

    /** Attribute name which identifies application details */
    public static final String XAWARE_ATTR_APPLICATION = "application";

    /** Lists the BizView files included in the archive file */
    protected static final String XAPUBLISH_XML_FILE = "XAPublish.xml";

    /** Element name under which XAPublish.xml file is updated */
    protected static final String PUBLISH_ROOT = "Publish";

    /** Attribute which represents XML version */
    public static final String XAWARE_ATTR_VESION = "version";

    protected static final Namespace ns = XAwareConstants.xaNamespace;

    /**
     * Creates a new Zip instance.
     * 
     * @param zipName The name of the output zip file.
     */
    public Zip(final String zipName) {
        this.zipFile = zipName;
    }

    /**
     * Set the base directory location for adding zip entry names. The base directory name will be removed from the
     * absolute file name when creating zipentry names.
     * 
     * @param dir
     */
    public void setBaseDir(final String dir) {
        baseDir = dir;
    }

    /**
     * set a list of files to be zipped. The names must be absolute names. Use setBaseDir to make entry names relative
     * rather than absolute.
     * 
     * @param fileNames
     */
    public void setFileList(final Hashtable fileNames) {
        nameVsFullName = fileNames;
    }

    /**
     * Build zip file from nameVsFullName Hashtable
     * 
     */
    public void zipFile() throws IOException {
        if (nameVsFullName != null) {
            ZipOutputStream out = null;
            try {
                final FileOutputStream dest = new FileOutputStream(zipFile);
                out = new ZipOutputStream(new BufferedOutputStream(dest));
                final Enumeration fileEnum = nameVsFullName.keys();
                while (fileEnum.hasMoreElements()) {
                    final String entryName = (String) fileEnum.nextElement();
                    final String filename = (String) nameVsFullName.get(entryName);
                    if (zipFile.equals(filename) == false) {
                        addFileEntry(out, entryName, filename);
                    }
                }
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (final IOException e) {
                    }
                }
            }
        }
    }

    protected void addFileEntry(final ZipOutputStream out, final String entryName, final String filename) throws IOException {
        BufferedInputStream origin = null;
        try {
            final byte data[] = new byte[BUFFER];
            System.out.println("Adding: " + entryName);
            final FileInputStream fi = new FileInputStream(filename);
            origin = new BufferedInputStream(fi, BUFFER);
            final ZipEntry entry = new ZipEntry(entryName);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
        } catch (final IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } finally {
            if (origin != null) {
                try {
                    origin.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    /**
     * Utility method for convenience that will build a list from a directory name
     * 
     * Recursively loop thru a directory and all subdirectories adding files to fileList. This list is intended to be a
     * list of entry names with physical filenames that can be opened and added to a zip file.
     * 
     * @param fileList
     * @param files
     *            an array of physical filenames
     */
    public void buildEntryVsPhysicalNames(final Hashtable<String, String> fileList, final String[] files) {
        buildEntryVsPhysicalNameWithStringMatch(fileList, files, null);
    }

    private void buildEntryVsPhysicalNameWithStringMatch(final Hashtable<String, String> fileList, final String[] files, String dirMatch) {
        for (int i = 0; i < files.length; i++) {
            final String filename = files[i];
            final File f = new File(filename);
            if (f.getName().startsWith(".")) {
                continue;
            }
            if (f.isDirectory()) {
                final File[] filesList = f.listFiles();
                final int numFiles = filesList.length;
                final String[] moreFiles = new String[numFiles];
                for (int x = 0; x < filesList.length; x++) {
                    moreFiles[x] = filesList[x].getAbsolutePath();
                }
                buildEntryVsPhysicalNameWithStringMatch(fileList, moreFiles, dirMatch);
            } else {
                String entryName = getEntryName(filename);
                if (dirMatch == null || entryName.startsWith(dirMatch)) {
                    fileList.put(entryName, filename);
                }
            }
        }
    }

    /**
     * Extract baseDir from filename to create entry name
     * 
     * @param filename
     * @return
     */
    protected String getEntryName(final String filename) {
        final File f = new File(filename);
        String entryName = f.getName();
        entryName = f.getPath();
        if (entryName.startsWith(baseDir) && baseDir.length() > 0) {
            entryName = filename.substring(baseDir.length() + 1);
        }
        return replaceBackslash(entryName);
    }

    /**
     * Paths in xar or zip files will be forward slashes. replaceAll had difficulties with \ replace thus the need for
     * this method.
     * 
     * @param inPath
     * @return
     */
    protected static String replaceBackslash(final String inPath) {
        String pathName = inPath;
        int index = pathName.indexOf('\\');
        while (index >= 0) {
            pathName = pathName.replace('\\', '/');
            index = pathName.indexOf('\\');
        }
        if (pathName.startsWith("/")) {
            pathName = pathName.substring(1);
        }
        return pathName;
    }

    /**
     * take a directory and recursively loop thru adding all files to a xar file and build XAPublish.xml.
     * 
     * @param dirName
     * @throws Exception
     */
    public static String buildXarFromDirectory(final String dirName, final String xarName) throws Exception {
        return buildXarFromDirectory(dirName, xarName, null);
    }

    /**
     * take a directory and recursively loop thru adding all files to a xar file and build XAPublish.xml.
     * 
     * @param dirName
     * @throws Exception
     */
    public static String buildXarFromDirectory(final String dirName, final String xarName, final String xarFilter) throws Exception {
        if (dirName == null) {
            System.out.println("Base directory is empty or null ");
        }
        if (xarName == null || xarName.length() == 0) {
            System.out.println("XAR name is empty or null ");
        }
        final Hashtable<String, String> xarfiles = new Hashtable<String, String>(100);
        final File dirFile = new File(dirName);
        String zipFilename;
        if (dirName.endsWith("/") || dirName.endsWith("\\")) {
            zipFilename = dirName + xarName;
        } else {
            zipFilename = dirName + File.separator + xarName;
        }
        final Zip zipMe = new Zip(zipFilename);
        zipMe.setBaseDir(dirFile.getAbsolutePath());
        final String[] dirs = new String[] { dirName };
        zipMe.buildEntryVsPhysicalNameWithStringMatch(xarfiles, dirs, xarFilter);
        createXAPublish(dirName, xarfiles, xarName);
        zipMe.setFileList(xarfiles);
        zipMe.zipFile();
        return zipFilename;
    }

    /**
     * Build the application element for XAPublish and write XAPublish.xml
     * 
     * @param zarName
     * 
     * @param xarFilename
     *            String
     * 
     * @throws Exception
     *             Thrown if unable to create/write the XAPublish.xml file
     */
    protected static void createXAPublish(final String directory, final Hashtable<String, String> xarfiles, final String xarName) throws Exception {
        try {
            final Element application = getApplicationElement(xarfiles);
            final Element root = new Element(PUBLISH_ROOT, ns);
            root.setAttribute(XAwareConstants.XAWARE_FILE_NAME, xarName, ns);
            root.setAttribute(XAWARE_ATTR_VESION, "1", ns);
            root.addContent(application);
            final Document doc = new Document(root);
            final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            final File outFile = new File(directory + XAPUBLISH_XML_FILE);
            outputter.output(doc, new FileOutputStream(outFile));
            xarfiles.put(XAPUBLISH_XML_FILE, outFile.getAbsolutePath());
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Build the application by calling recursive getDependents() to add elements such that all dependents are added
     * before their parents.
     * 
     * @return Element
     */
    protected static Element getApplicationElement(final Hashtable nameVsFullName) {
        final Element appl = new Element(XAWARE_ATTR_APPLICATION, ns);
        if (nameVsFullName == null) {
            return null;
        }
        final Enumeration keys = nameVsFullName.keys();
        while (keys.hasMoreElements()) {
            final String key = (String) keys.nextElement();
            if (key.equals(XAPUBLISH_XML_FILE) == false) {
                appl.addContent(getComponentElement(key));
            }
        }
        return appl;
    }

    /**
     * Constructs component element from the given file
     * 
     * @param key
     *            String
     * 
     * @return Element
     */
    protected static Element getComponentElement(final String key) {
        final Element keyRoot = getKeyRootElement(key);
        final Element component = new Element(XAwareConstants.XAWARE_COMPONENT, ns);
        component.setAttribute(XAwareConstants.XAWARE_FILE_NAME, key, ns);
        String value = null;
        if (keyRoot != null) {
            value = keyRoot.getChildText(XAwareConstants.XAWARE_ATTR_DESCRIPTION, ns);
            if (value != null && value.length() > 0) {
                component.setAttribute(XAwareConstants.XAWARE_ATTR_DESCRIPTION, value, ns);
            } else {
                value = null;
            }
        }
        if (value == null) {
            value = "Description for " + key;
            component.setAttribute(XAwareConstants.XAWARE_ATTR_DESCRIPTION, value, ns);
        }
        component.setAttribute(XAwareConstants.XAWARE_FILE_TYPE, getType(key), ns);
        value = key;
        component.setAttribute(XAwareConstants.XAWARE_LOCATION, key, ns);
        component.setAttribute(XAwareConstants.XAWARE_STATUS, "active", ns);
        return component;
    }

    /**
     * Open the file if it is a .xbd, .xbc, .xdr, or .xml file and return the root element.
     * 
     * @param key
     * @return
     */
    private static Element getKeyRootElement(final String key) {
        try {
            final String filename = baseDir + File.separator + key;
            final SAXBuilder builder = new SAXBuilder();
            final Document doc = builder.build(new File(filename));
            final Element root = doc.getRootElement();
            return root;
        } catch (final Exception e) {
        }
        return null;
    }

    /**
     * Evaluate the file name and return a string indicating type based on the suffix.
     * 
     * @param key
     * @return
     */
    private static String getType(final String key) {
        if (key.endsWith(".xbd")) {
            return "xa:bizdoc";
        } else if (key.endsWith(".xbc")) {
            return "xa:bizcomponent";
        } else if (key.endsWith(".xdr")) {
            return "xa:bizdriver";
        } else if (key.endsWith(".sql")) {
            return "xa:database";
        } else if (key.endsWith(".xml")) {
            return "xa:xml";
        } else if (key.endsWith(".wsdl")) {
            return "xa:wsdl";
        } else if (key.endsWith(".xsd")) {
            return "xa:schema";
        }
        return "xa:other";
    }

    public static void main(final String argv[]) {
        if (argv.length < 2) {
            System.err.println("Usage: Zip zipfile <Directory or files to be zipped>");
            return;
        }
        try {
            final Zip zipMe = new Zip(argv[0]);
            final Hashtable<String, String> names = new Hashtable<String, String>();
            final int numParms = argv.length;
            final String[] fileNames = new String[numParms - 1];
            for (int i = 1; i < numParms; i++) {
                fileNames[i - 1] = argv[i];
            }
            final File curDir = new File("C:\\Logs");
            String curDirName = curDir.getPath();
            curDirName = curDirName.substring(0, curDirName.length());
            zipMe.setBaseDir(curDirName);
            zipMe.buildEntryVsPhysicalNames(names, fileNames);
            zipMe.setFileList(names);
            zipMe.zipFile();
        } catch (final Exception e) {
            System.err.println("Unhandled exception:");
            e.printStackTrace();
            return;
        }
    }
}
