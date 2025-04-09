package net.sourceforge.magex.preparation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * JarReader is used to read all files from the application JAR
 *  file, in order to embed them with the map.
 */
public class JarReader {

    /** File name of the map index file */
    private static final String MAP_INDEX_FILE = "maps.dat";

    /** File name of the JAR manifest file */
    private static final String MANIFEST = "META-INF/MANIFEST.MF";

    /** List of lines from the manifest that shouldn't come up in the JAD file */
    private static final String[] MANIFEST_ONLY_LINES = { "Manifest-Version", "Ant-Version", "Created-By" };

    /** Name of the line in manifest that contains information about the midlet name. */
    private static final String MIDLET_NAME = "MIDlet-Name";

    /** Line for the JAD file -- file size of JAR */
    private static final String JAD_FILESIZE = "MIDlet-Jar-Size";

    /** Line for the JAD file -- file name of JAR */
    private static final String JAD_FILENAME = "MIDlet-Jar-URL";

    /** Separator of values from names in JAD file (write use only) */
    private static final String JAD_SEPARATOR = ": ";

    /** 
     * Buffer size for copying the input to the output, 64 K should be big enough to transfer one whole
     * entry at a time.
     */
    private int BUFFER_SIZE = 65536;

    /** Path to the application JAR file */
    private String jarPath;

    /** The open application JAR file */
    private ZipFile jarFile;

    /** Input map ids */
    private int[] jarMapIds;

    /** Output JAD file name */
    private String jadFileName;

    /** Output JAD file */
    private OutputStream jadFile;

    /** The JAR Midlet name */
    private String midletName;

    /** New map id */
    private int newMapId;

    /** Lines of the manifest file that should go into the jad file */
    private Vector<String> manifestLines;

    /** 
     * Given a valid application JAR file, opens it, finds out how many maps 
     * are there and keeps the file open for further operations. Reads the JAR file manifest
     * and keeps the new midlet name for writing the new manifest version (if 
     * not null, otherwise no change).
     * 
     * @param jarPath the path to the application JAR file
     * @param midletName the new "midlet name" for the JAR file
     */
    public JarReader(String jarPath, String midletName) throws DataPrepException {
        ZipEntry mapIndex;
        this.jarPath = jarPath;
        this.midletName = midletName;
        try {
            this.jarFile = new ZipFile(jarPath);
        } catch (IOException e) {
            if (Main.DEBUG) e.printStackTrace();
            throw new DataPrepException(DataPrepException.INVALID_JAR);
        }
        this.parseManifest();
        if ((mapIndex = this.jarFile.getEntry(MAP_INDEX_FILE)) != null) {
            DataInputStream is = null;
            int maxId = 0;
            try {
                is = new DataInputStream(this.jarFile.getInputStream(mapIndex));
                this.jarMapIds = new int[is.readInt()];
                for (int i = 0; i < this.jarMapIds.length; ++i) {
                    this.jarMapIds[i] = is.readInt();
                    if (this.jarMapIds[i] > maxId) {
                        maxId = this.jarMapIds[i];
                    }
                }
                this.newMapId = maxId + 1;
            } catch (IOException e) {
                if (Main.DEBUG) e.printStackTrace();
                throw new DataPrepException(DataPrepException.INVALID_JAR);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                }
            }
        } else {
            this.newMapId = 1;
            this.jarMapIds = new int[0];
        }
    }

    /**
     * Returns the ID of the new map to be written to the output JAR file.
     *
     * @return the ID of the new map
     */
    int getNewMapId() {
        return this.newMapId;
    }

    /**
     * Opens the JAD file for this process for output, therefore gaining a
     * write lock on it. Throws an exception in case of an error.
     *
     * @param name the output JAD file name
     */
    public void openJad(String name) throws DataPrepException {
        try {
            this.jadFileName = name;
            this.jadFile = new FileOutputStream(name);
        } catch (Exception e) {
            if (Main.DEBUG) e.printStackTrace();
            throw new DataPrepException(DataPrepException.WRITE_ACCESS_ERROR);
        }
    }

    /**
     * Parses the manifest of the JAR file, puts all the lines needed for JAD file
     * to manifestLines. Excludes all lines that begin with strings from MANIFEST_ONLY_LINES.
     * Throws an exception if the manifest file is not in the JAR or has an invalid format.
     * Changes the midlet name in the manifest lines -- replaces it with the midletName member,
     * if it's not null.     
     */
    private void parseManifest() throws DataPrepException {
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(this.jarFile.getInputStream(this.jarFile.getEntry(MANIFEST))));
            String line;
            boolean add;
            this.manifestLines = new Vector<String>();
            while (is.ready()) {
                line = is.readLine();
                add = true;
                if (line == null) {
                    break;
                } else if (line.equals("")) {
                    continue;
                } else if (this.midletName != null && line.startsWith(MIDLET_NAME)) {
                    line = MIDLET_NAME + JAD_SEPARATOR + midletName;
                }
                this.manifestLines.add(line);
            }
            is.close();
        } catch (Exception e) {
            if (Main.DEBUG) e.printStackTrace();
            throw new DataPrepException(DataPrepException.INVALID_JAR);
        }
    }

    /**
     * Writes the maps.dat file entry into the output JAR, including the newly embedded 
     * map.
     *
     * @param output the output ZIP stream to write to
     */
    void writeMapIndex(ZipOutputStream output) throws IOException {
        DataOutputStream os;
        output.putNextEntry(new ZipEntry(MAP_INDEX_FILE));
        os = new DataOutputStream(output);
        os.writeInt(this.jarMapIds.length + 1);
        for (int i = 0; i < this.jarMapIds.length; ++i) {
            os.writeInt(this.jarMapIds[i]);
        }
        os.writeInt(this.newMapId);
        os.flush();
        output.closeEntry();
    }

    /**
     * Given the output file size and location information, writes the output JAD file.
     * Uses the contents of the manifestLines member, which is set-up in constructor.
     *
     * @param jarLocation the location of the JAR file the JAD should point to 
     * @param jarSize the size of the JAR file the JAD should point to
     */
    void writeJad(String jarLocation, long jarSize) throws IOException {
        DataOutputStream os = new DataOutputStream(this.jadFile);
        for (int i = 0; i < this.manifestLines.size(); ++i) {
            for (int j = 0; j < MANIFEST_ONLY_LINES.length; ++j) {
                if (this.manifestLines.elementAt(i).startsWith(MANIFEST_ONLY_LINES[j])) {
                    continue;
                }
            }
            os.writeBytes(this.manifestLines.elementAt(i));
            os.writeByte('\n');
        }
        os.writeBytes(JAD_FILESIZE + JAD_SEPARATOR + jarSize + "\n");
        os.writeBytes(JAD_FILENAME + JAD_SEPARATOR + jarLocation + "\n");
        os.flush();
        this.jadFile.close();
        this.jadFile = null;
    }

    /**
     * Copies all the contents of the input JAR file to the output JAR file, excluding
     * the maps.dat file.
     *
     * @param output an open ZIP output stream to write to
     */
    void writeAllInputContents(ZipOutputStream output) throws IOException {
        Enumeration<? extends ZipEntry> jarEntries = this.jarFile.entries();
        ZipEntry curEntry;
        InputStream is;
        DataOutputStream os;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while (jarEntries.hasMoreElements()) {
            curEntry = jarEntries.nextElement();
            if (curEntry.getName().equals(MAP_INDEX_FILE) || curEntry.getName().equals(MANIFEST)) {
                continue;
            }
            is = this.jarFile.getInputStream(curEntry);
            output.putNextEntry(curEntry);
            while ((bytesRead = is.read(buffer)) > 0) {
                output.write(buffer, 0, bytesRead);
            }
            output.closeEntry();
        }
        output.putNextEntry(new ZipEntry(MANIFEST));
        os = new DataOutputStream(output);
        for (int i = 0; i < this.manifestLines.size(); ++i) {
            os.writeBytes(this.manifestLines.elementAt(i));
            os.writeByte('\n');
        }
        os.flush();
        output.closeEntry();
    }
}
