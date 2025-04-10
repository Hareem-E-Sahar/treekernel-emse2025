package frost;

import java.awt.Component;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;
import javax.swing.JFileChooser;
import org.w3c.dom.Document;
import frost.messages.*;

public class FileAccess {

    private static Logger logger = Logger.getLogger(FileAccess.class.getName());

    /**
	 * Writes a file to disk after opening a saveDialog window
	 * @param parent The parent component, often 'this' can be used
	 * @param conten The data to write to disk.
	 * @param lastUsedDirectory The saveDialog starts at this directory
	 * @param title The saveDialog gets this title
	 */
    public static void saveDialog(Component parent, String content, String lastUsedDirectory, String title) {
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(title);
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);
        int returnVal = fc.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
                MainFrame.frostSettings.setValue("lastUsedDirectory", file.getParent());
                if (!file.isDirectory()) {
                    writeFile(content, file, "UTF-8");
                }
            }
        }
    }

    /**
     * removes unwanted files from the keypool
     * @param keyPath the directory to clean
     */
    public static void cleanKeypool(String keyPath) {
        File[] chunks = (new File(keyPath)).listFiles();
        String date = DateFun.getExtendedDate();
        String fileSeparator = System.getProperty("file.separator");
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i].isFile()) {
                if (chunks[i].length() == 0 || chunks[i].getName().endsWith(".tmp")) chunks[i].delete();
                if (!chunks[i].getName().startsWith(date) && chunks[i].getName().endsWith(".idx")) chunks[i].delete();
                if (!chunks[i].getName().startsWith(date) && chunks[i].getName().endsWith(".loc")) chunks[i].delete();
            }
        }
    }

    /**
     * Writes a byte[] to disk
     * @param data the byte[] with the data to write
     * @param file the destination file
     */
    public static void writeByteArray(byte[] data, File file) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            fileOut.write(data);
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in writeByteArray(byte[] data, File file)", e);
        }
    }

    /**
     * Reads a file and returns it's content in a byte[]
     * @param file the file to read
     * @return byte[] with the files content
     */
    public static byte[] readByteArray(String filename) {
        return readByteArray(new File(filename));
    }

    public static byte[] readByteArray(File file) {
        try {
            byte[] data = new byte[(int) file.length()];
            FileInputStream fileIn = new FileInputStream(file);
            DataInputStream din = new DataInputStream(fileIn);
            din.readFully(data);
            fileIn.close();
            return data;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readByteArray(File file)", e);
        }
        return null;
    }

    /**
     * Reads a file and returns it contents in a String
     */
    public static String read(String path) {
        FileReader fr;
        StringBuffer content = new StringBuffer();
        int c;
        try {
            fr = new FileReader(path);
            while ((c = fr.read()) != -1) {
                content.append((char) c);
            }
            fr.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in read(String path)", e);
            return ("Read Error");
        }
        return content.toString();
    }

    /**
     * Returns all files starting from given directory/file that have a given extension.
     */
    public static ArrayList getAllEntries(File file, final String extension) {
        ArrayList files = new ArrayList();
        getAllFiles(file, extension, files);
        return files;
    }

    /**
     * Returns all files starting from given directory/file that have a given extension.
     */
    private static void getAllFiles(File file, String extension, ArrayList filesLst) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] dirfiles = file.listFiles();
                if (dirfiles != null) {
                    for (int i = 0; i < dirfiles.length; i++) {
                        getAllFiles(dirfiles[i], extension, filesLst);
                    }
                }
            }
            if (extension.length() == 0 || file.getName().endsWith(extension)) {
                filesLst.add(file);
            }
        }
    }

    /**
     * Writes zip file
     */
    public static void writeZipFile(byte[] content, String entry, File file) {
        if (content.length == 0) {
            Exception e = new Exception();
            e.fillInStackTrace();
            logger.log(Level.SEVERE, "Tried to zip an empty file!  Send this output to a dev" + " and describe what you were doing.", e);
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry(entry);
            ze.setSize(content.length);
            zos.putNextEntry(ze);
            zos.write(content);
            zos.flush();
            zos.closeEntry();
            zos.close();
            fos.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in writeZipFile(byte[] content, String entry, File file)", e);
        }
    }

    /**
     * Reads first zip file entry and returns content in a String
     */
    public static String readZipFile(String path) {
        return readZipFile(new File(path));
    }

    public static String readZipFile(File file) {
        byte[] content = readZipFileBinary(file);
        if (content != null) {
            return new String(content);
        }
        return null;
    }

    public static byte[] readZipFileBinary(File file) {
        if (!file.isFile() || file.length() == 0) return null;
        int bufferSize = 4096;
        try {
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            try {
                ArrayList chunks = new ArrayList();
                ArrayList chunkLen = new ArrayList();
                zis.getNextEntry();
                byte[] zipData = null;
                int len;
                boolean bur = true;
                int off = 0;
                int num = bufferSize;
                while (zis.available() == 1) {
                    bur = true;
                    off = 0;
                    num = bufferSize;
                    zipData = new byte[bufferSize];
                    while (bur && zis.available() == 1) {
                        len = zis.read(zipData, off, num);
                        off += len;
                        if (off >= bufferSize) bur = false; else num = num - len;
                    }
                    chunks.add(zipData);
                    chunkLen.add(new Integer(off));
                }
                fis.close();
                zis.close();
                int overallLen = 0;
                Iterator i = chunkLen.iterator();
                while (i.hasNext()) {
                    Integer aChunkLen = (Integer) i.next();
                    overallLen += aChunkLen.intValue();
                }
                overallLen++;
                byte[] resultbytes = new byte[overallLen];
                int actOffset = 0;
                for (int x = 0; x < chunks.size(); x++) {
                    byte[] aChunk = (byte[]) chunks.get(x);
                    int aChunkLen = ((Integer) chunkLen.get(x)).intValue();
                    if (x == chunks.size() - 1) aChunkLen++;
                    System.arraycopy(aChunk, 0, resultbytes, actOffset, aChunkLen);
                    actOffset += aChunkLen;
                }
                return resultbytes;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception thrown in readZipFile(String path) \n" + "Offending file saved as badfile.zip, send to a dev for analysis", e);
                File badFile = new File("badfile.zip");
                file.renameTo(badFile);
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Exception thrown in readZipFile(String path)", e);
        }
        return null;
    }

    /**
     * Reads file and returns a Vector of lines
     */
    public static Vector readLines(File file) {
        return readLines(file.getPath());
    }

    public static Vector readLines(String path) {
        BufferedReader f;
        String line;
        line = "";
        Vector data = new Vector();
        try {
            f = new BufferedReader(new FileReader(path));
            while ((line = f.readLine()) != null) {
                data.add(line.trim());
            }
            f.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readLines(String path)", e);
        }
        return data;
    }

    /**
     * Reads a file and returns its contents in a String
     */
    public static String readFile(File file) {
        return readFile(file.getPath());
    }

    public static String readFile(String path) {
        BufferedReader f;
        String line = new String();
        StringBuffer stringBuffer = new StringBuffer();
        try {
            f = new BufferedReader(new FileReader(path));
            while ((line = f.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            f.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readFile(String path)", e);
        }
        return stringBuffer.toString();
    }

    /**
	 * Reads a file, line by line, and adds a \n after each one.
	 * You can specify the encoding to use when reading.
	 * @param path
	 * @param encoding
	 * @return the contents of the file
	 */
    public static String readFile(String path, String encoding) {
        String line;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStreamReader iSReader = new InputStreamReader(new FileInputStream(path), encoding);
            BufferedReader reader = new BufferedReader(iSReader);
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
            reader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readFile(String path, String encoding)", e);
        }
        return stringBuffer.toString();
    }

    /**
     * Writes a file "file" to "path"
     */
    public static void writeFile(String content, String filename) {
        writeFile(content, new File(filename));
    }

    /**
	 * Writes a file "file" to "path", being able to specify the encoding
	 */
    public static void writeFile(String content, String filename, String encoding) {
        writeFile(content, new File(filename), encoding);
    }

    public static void writeFile(String content, File file) {
        FileWriter f1;
        try {
            f1 = new FileWriter(file);
            f1.write(content);
            f1.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file)", e);
        }
    }

    public static void writeFile(String content, File file, String encoding) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream, encoding);
            BufferedReader inputReader = new BufferedReader(new StringReader(content));
            String lineSeparator = System.getProperty("line.separator");
            String line = inputReader.readLine();
            while (line != null) {
                outputWriter.write(line + lineSeparator);
                line = inputReader.readLine();
            }
            outputWriter.close();
            inputReader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file, String encoding)", e);
        }
    }

    /**
     * Returns filenames in a directory
     */
    public static String[] getFilenames(String Path) {
        File FileObject = new File(Path);
        String[] filenames;
        if (FileObject.isDirectory()) filenames = FileObject.list(); else filenames = new String[0];
        return filenames;
    }

    /**
     * Deletes the given directory and ALL FILES/DIRS IN IT !!!
     * USE CAREFUL !!!
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Reads a keyfile from disk and adds the keys to a map
     * @param source keyfile as String or as File
     * @param chk Map that will be used to add the keys
     * @param exchange the exchange flag of SharedFileObject will be set to this value
     */
    public static FrostIndex readKeyFile(String source) {
        return readKeyFile(new File(source));
    }

    public static FrostIndex readKeyFile(File source) {
        if (!source.isFile() || !(source.length() > 0)) return new FrostIndex(new HashMap()); else {
            int counter = 0;
            Document d = null;
            try {
                d = XMLTools.parseXmlFile(source.getPath(), false);
            } catch (IllegalArgumentException t) {
                logger.log(Level.SEVERE, "Exception thrown in readKeyFile(File source): \n" + "Offending file saved as badfile.xml - send it to a dev for analysis", t);
                File badfile = new File("badfile.xml");
                source.renameTo(badfile);
            }
            if (d == null) {
                logger.warning("Couldn't parse index file.");
                return null;
            }
            FrostIndex idx = new FrostIndex(d.getDocumentElement());
            Iterator i = idx.getFiles().iterator();
            while (i.hasNext()) {
                SharedFileObject newKey = (SharedFileObject) i.next();
                if (!newKey.isValid()) {
                    i.remove();
                    logger.warning("invalid key found");
                    continue;
                }
            }
            return idx;
        }
    }

    public static void writeKeyFile(FrostIndex idx, String destination) {
        writeKeyFile(idx, new File(destination));
    }

    public static void writeKeyFile(FrostIndex idx, File destination) {
        if (idx.getFiles().size() == 0) {
            return;
        }
        File tmpFile = new File(destination.getPath() + ".tmp");
        int itemsAppended = 0;
        synchronized (idx) {
            Iterator i = idx.getFiles().iterator();
            while (i.hasNext()) {
                SharedFileObject current = (SharedFileObject) i.next();
                if (current.getOwner() != null && Core.getInstance().getIdentities().getEnemies().get(current.getOwner()) != null) {
                    i.remove();
                    continue;
                }
                itemsAppended++;
            }
        }
        if (itemsAppended == 0) {
            logger.warning("writeKeyFile called with no files to add?");
            return;
        }
        boolean writeOK = false;
        try {
            Document doc = XMLTools.getXMLDocument(idx);
            writeOK = XMLTools.writeXmlFile(doc, tmpFile.getPath());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception thrown in writeKeyFile(FrostIndex idx, File destination)", t);
        }
        if (writeOK) {
            File oldFile = new File(destination.getPath() + ".old");
            oldFile.delete();
            destination.renameTo(oldFile);
            tmpFile.renameTo(destination);
        } else {
            tmpFile.delete();
        }
    }

    public static String readFileRaw(String path) {
        return readFileRaw(new File(path));
    }

    public static String readFileRaw(File file) {
        if (!file.exists()) return null;
        return readFile(file);
    }

    /**
	 * This method copies the contents of one file to another. If the destination file didn't exist, 
	 * it is created. If it did exist, its contents are overwritten.
	 * @param sourceName name of the source file
	 * @param destName name of the destination file
	 * @throws IOException if there was a problem while copying the file
	 */
    public static void copyFile(String sourceName, String destName) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(sourceName).getChannel();
            destChannel = new FileOutputStream(destName).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException exception) {
            throw exception;
        } finally {
            if (sourceChannel != null) {
                try {
                    sourceChannel.close();
                } catch (IOException ex) {
                }
            }
            if (destChannel != null) {
                try {
                    destChannel.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
