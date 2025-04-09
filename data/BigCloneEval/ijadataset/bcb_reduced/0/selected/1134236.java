package sywico.core;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SyncFailedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.log4j.Logger;
import sywico.core.checksumreport.Filter;

/**
 * This non instanciable class is a collection of static methods
 *
 */
public class Util {

    public static Logger logger = Logger.getLogger(Util.class.getName());

    private Util() {
    }

    /**
     * 
     * Save a list of strings into a file
     * 
     * 
     * @param pathname
     * @param data
     */
    public static void writeStringListIntoFile(String pathname, List<String> data) {
        if (logger.isDebugEnabled()) logger.debug("writeStringListIntoFile(" + pathname + ") saving " + data.size() + " lines");
        try {
            FileOutputStream out = new FileOutputStream(pathname);
            writeStringListIntoStream(out, data);
            closeAndWaitFileOutputStream(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] writeStringListIntoByteArray(List<String> data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeStringListIntoStream(outputStream, data);
        return outputStream.toByteArray();
    }

    /**
	 * 
	 * Save a list of strings into a stream
	 * 
	 * 
	 * @param pathname
	 * @param data
	 */
    public static void writeStringListIntoStream(OutputStream out, List<String> data) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            for (String string : data) {
                writer.write(string);
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * 
	 * Read the full file into a list of strings
	 * 
	 * @param pathname
	 * @return
	 */
    public static List<String> readFileIntoStringList(String pathname) {
        if (logger.isDebugEnabled()) logger.debug("readFileIntoStringList(" + pathname + ") ");
        List<String> retVal = new ArrayList<String>();
        try {
            FileInputStream in = new FileInputStream(pathname);
            LineReader reader = new LineReader(new InputStreamReader(in));
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                retVal.add(line);
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (logger.isDebugEnabled()) logger.debug("readFileIntoStringList() read " + retVal.size() + " lines");
        return retVal;
    }

    /**
     * 
     * this small function will close the fileoutputstream and wait for the data to be synchronized
     * 
     * @param out
     * @throws IOException
     * @throws SyncFailedException
     */
    public static void closeAndWaitFileOutputStream(FileOutputStream out) throws IOException, SyncFailedException {
        out.flush();
        FileDescriptor fd = out.getFD();
        fd.sync();
        out.close();
    }

    /**
	 * write a bytearray into a file
	 * @param pathname
	 * @param data
	 */
    public static void writeFileFromByteArray(String pathname, byte[] data) {
        try {
            FileOutputStream out = new FileOutputStream(pathname);
            out.write(data);
            closeAndWaitFileOutputStream(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * read the file's contents into a bytearray.
	 */
    public static byte[] readFileIntoByteArray(String pathname) {
        byte[] bytes = null;
        try {
            File file = new File(pathname);
            InputStream is = new FileInputStream(file);
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                throw new RuntimeException("File is too large to be stored in a byte array");
            }
            bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    /**
     * Apply the prefix to all elements in the list of string.
     * 
     * @param prefix
     * @param list
     * @return an updated list of strings
     */
    public static List<String> addPrefix(String prefix, List<String> list) {
        List<String> retVal = new ArrayList<String>();
        for (String string : list) retVal.add(prefix + string);
        return retVal;
    }

    /**
	 *
     * return the checksum computed from the contents of the stream
	 *
	 */
    public static long createChecksum(InputStream fis) throws Exception {
        CheckedInputStream check = new CheckedInputStream(fis, new CRC32());
        BufferedInputStream in = new BufferedInputStream(check);
        while (in.read() != -1) {
        }
        return check.getChecksum().getValue();
    }

    /**
	  * 
	  * return the checksum computed from the contents of the file
	  */
    public static long createChecksum(String filename) {
        try {
            InputStream fis = new FileInputStream(filename);
            long retVal = createChecksum(fis);
            fis.close();
            return retVal;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
      * 
      *  Serialize an object into a stream
      *  
      * @param stream
      * @param obj
	  */
    public static void saveObjectToStream(OutputStream stream, Object obj) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(obj);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * helper function that returns a Cipher object PBEWithMD5AndDES with predefined salt and 2 iterations.
     * This function is used by saveObjectToFile() and readObjectFromFile()
     * 
     * @param password the password to use
     * @param mode either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return the cipher object initialised
     */
    public static Cipher getCipher(String password, int mode) {
        try {
            PBEKeySpec pbeKeySpec;
            PBEParameterSpec pbeParamSpec;
            SecretKeyFactory keyFac;
            pbeParamSpec = new PBEParameterSpec("-sywico-".getBytes(), 2);
            pbeKeySpec = new PBEKeySpec(password.toCharArray());
            keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(mode, pbeKey, pbeParamSpec);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * Serialize an object into a file
     * 
     * @param fileName
     * @param obj
     */
    public static void saveObjectToFile(String fileName, Object obj, String pass) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            GZIPOutputStream out = new GZIPOutputStream(fout);
            if (pass != null) {
                CipherOutputStream sout = new CipherOutputStream(out, getCipher(pass, Cipher.ENCRYPT_MODE));
                saveObjectToStream(sout, obj);
                sout.flush();
                sout.close();
            } else {
                saveObjectToStream(out, obj);
                out.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * unserialize an object from a stream
     * 
     * @param stream
     * @return
     */
    public static Object readObjectFromStream(InputStream stream) {
        try {
            ObjectInputStream in = new ObjectInputStream(stream);
            Object obj = in.readObject();
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * unserialize an object from a file. 
     * 
     * @param fileName
     * @return
     */
    public static Object readObjectFromFile(String fileName, String pass) {
        try {
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(fileName));
            Object obj;
            if (pass != null) {
                CipherInputStream sin = new CipherInputStream(in, getCipher(pass, Cipher.DECRYPT_MODE));
                obj = readObjectFromStream(sin);
                sin.close();
            } else {
                obj = readObjectFromStream(in);
                in.close();
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * like mv in unix... causes lots of troubles
     * 
     * @param orig
     * @param dest
     */
    public static void move(String orig, String dest) {
        if (!new File(orig).exists()) throw new RuntimeException("move: origin " + orig + " does not exists");
        if (new File(dest).exists()) throw new RuntimeException("move: dest " + dest + " already exists");
        if (!new File(orig).renameTo(new File(dest))) {
            logger.error(" can not properly move " + orig + " into " + dest + " . attempting copy /delete ...");
            copy(orig, dest, null);
            delete(orig, null);
            if (new File(orig).exists()) throw new RuntimeException("can not move " + orig + " into " + dest);
        }
        assert (!new File(orig).exists() && new File(dest).exists()) : "move should wait";
    }

    /**
   * used by copyStream
   */
    private static final byte[] copyStreamBuffer = new byte[1000000];

    /**
 * 
 * copy the contents of one stream into another
 * 
 * @param in
 * @param out
 * @param max. max bytesto transfer ignored if <=0
 * @return true if EOF
 */
    public static boolean copyStream(InputStream in, OutputStream out, int max) throws IOException {
        boolean retVal = false;
        int transfered = 0;
        while (max <= 0 || transfered < max) {
            synchronized (copyStreamBuffer) {
                int maxToReadThisTime = copyStreamBuffer.length;
                if (max > 0 && transfered + maxToReadThisTime > max) maxToReadThisTime = max - transfered;
                int amountRead = in.read(copyStreamBuffer, 0, maxToReadThisTime);
                if (amountRead == -1) {
                    retVal = true;
                    break;
                }
                transfered += amountRead;
                out.write(copyStreamBuffer, 0, amountRead);
            }
        }
        if (logger.isDebugEnabled()) logger.debug("copyStream() copied " + transfered + " bytes and returns " + retVal);
        return retVal;
    }

    /**
      *
      * initialize feeBack if not null, and then call doTheCopy() 
      * 
	  */
    public static void copy(String orig, String dest, boolean block, FeedBack feedBack) {
        if (feedBack != null) {
            feedBack.nextOperation("  copying from '" + orig + " to " + dest);
            FileCounter fileCounter = new FileCounter();
            fileCounter.explore(orig, new Filter(), null);
            feedBack.setTotalStepsInCurrentOperation(fileCounter.getCounter() + 1);
        }
        doCopy(orig, dest, block, feedBack);
    }

    /**
     * 
     * recursive copy of files and directories. like cp in unix 
     * 
     * TODO split me in two
     * @param orig
     * @param dest
     * @param block if true, wait until the file contents have been written into the disk
     * @param feedBack TODO
     * 
     */
    protected static void doCopy(String orig, String dest, boolean block, FeedBack feedBack) {
        if (logger.isDebugEnabled()) logger.debug("copy(" + orig + "," + dest + ")");
        File origFile = new File(orig);
        if (feedBack != null) feedBack.nextStep(orig);
        if (!origFile.exists()) throw new RuntimeException("copy: origin " + orig + " does not exists");
        if (origFile.isDirectory()) {
            File destFile = new File(dest);
            destFile.mkdirs();
            if (!destFile.isDirectory()) throw new RuntimeException("can't create :" + destFile + " . while copying from " + orig + " to " + dest);
            String[] fileList = origFile.list();
            for (String entry : fileList) doCopy(orig + File.separator + entry, dest + File.separator + entry, block, feedBack);
        } else {
            try {
                FileInputStream in = new FileInputStream(orig);
                FileOutputStream out = new FileOutputStream(dest);
                copyStream(in, out, 0);
                in.close();
                if (block) closeAndWaitFileOutputStream(out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * blocking  copy()
     * 
     * @param orig
     * @param dest
     * @param feedBack TODO
     */
    public static void copy(String orig, String dest, FeedBack feedBack) {
        copy(orig, dest, true, feedBack);
    }

    public static void delete(String name, FeedBack feedBack) {
        delete(name, feedBack, true);
    }

    public static void delete(String name, FeedBack feedBack, boolean mustExist) {
        if (!mustExist && !new File(name).exists()) {
            logger.debug(name + " not found, skip delete ");
        } else {
            if (feedBack != null) {
                feedBack.nextOperation("  deleting " + name);
                FileCounter fileCounter = new FileCounter();
                fileCounter.explore(name, new Filter(), null);
                feedBack.setTotalStepsInCurrentOperation(fileCounter.getCounter() + 1);
            }
            doDelete(name, feedBack);
        }
    }

    /**
	 * delete a file or a dir, even if non empty
	 * 
	 * @param name
	 */
    protected static void doDelete(String name, FeedBack feedBack) {
        File dir = new File(name);
        if (dir.exists()) {
            if (dir.isDirectory()) {
                for (String entry : dir.list()) if (!entry.equals("..")) doDelete(name + File.separator + entry, feedBack);
            }
            dir.delete();
        }
    }

    /**
      *
      * return true if the specified file is a binary file.
      * 
      * This implementation simply reads the first 1kb from the file and if a 0 is found, returns true  
      * 
      * @param file
      * @return
	  */
    public static boolean isBinaryFile(String file) {
        try {
            byte buffer[] = new byte[1000];
            FileInputStream in = new FileInputStream(file);
            int read = in.read(buffer);
            in.close();
            for (int i = 0; i < read; i++) if (buffer[i] == 0) return true;
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
      * 
      * modify a file name by adding a marker just before the extension.
      * e.g.: applying 'bla.txt' with marker '.zzz' will return 'bla.zzz.txt'
      * 
      * @param fileName
      * @param marker
      * @return
      */
    public static String addBeforeExtension(String fileName, String marker) {
        File file = new File(fileName);
        String simpleName = file.getName();
        int lastDotIdx = simpleName.lastIndexOf(".");
        String retVal = "";
        if (lastDotIdx == -1) retVal = simpleName + marker; else retVal = simpleName.substring(0, lastDotIdx) + marker + simpleName.substring(lastDotIdx);
        retVal = (file.getParent() == null ? ("") : (file.getParent() + File.separator)) + retVal;
        return retVal;
    }

    /**
     * 
     * builds the path to use for backing up a directory.
     * 
     * 
     * @param directory the original directory to be backed up
     * @param backupRoot where all the backups sit
     * @param date current date. used to name the entry in the backupRoot
     * @param name an optional name to use inside the backup entry. if not specified use name of directory
     */
    public static String getBackupNameFor(String directory, String backupRoot, Date date, String name) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss.SSS");
        String backupName = backupRoot + File.separator + format.format(date) + File.separator + ((name == null) ? (new File(directory).getName()) : name);
        return backupName;
    }

    public static void createParentDirectoryIfNeeded(String fullName) {
        File file = new File(fullName);
        new File(file.getParent()).mkdirs();
    }
}
