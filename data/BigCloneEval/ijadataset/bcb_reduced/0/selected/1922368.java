package org.gudy.azureus2.core3.util;

import java.io.*;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.logging.LogEvent;
import org.gudy.azureus2.core3.logging.LogIDs;
import org.gudy.azureus2.core3.logging.Logger;
import org.gudy.azureus2.platform.PlatformManager;
import org.gudy.azureus2.platform.PlatformManagerCapabilities;
import org.gudy.azureus2.platform.PlatformManagerFactory;
import org.gudy.azureus2.plugins.platform.PlatformManagerException;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreOperation;
import com.aelitis.azureus.core.AzureusCoreOperationTask;

/**
 * File utility class.
 */
public class FileUtil {

    private static final LogIDs LOGID = LogIDs.CORE;

    public static final String DIR_SEP = System.getProperty("file.separator");

    private static final int RESERVED_FILE_HANDLE_COUNT = 4;

    private static List reserved_file_handles = new ArrayList();

    private static AEMonitor class_mon = new AEMonitor("FileUtil:class");

    private static Method reflectOnUsableSpace;

    static {
        try {
            reflectOnUsableSpace = File.class.getMethod("getUsableSpace", (Class[]) null);
        } catch (NoSuchMethodException e) {
            reflectOnUsableSpace = null;
        }
    }

    public static boolean isAncestorOf(File parent, File child) {
        parent = canonise(parent);
        child = canonise(child);
        if (parent.equals(child)) {
            return true;
        }
        String parent_s = parent.getPath();
        String child_s = child.getPath();
        if (parent_s.charAt(parent_s.length() - 1) != File.separatorChar) {
            parent_s += File.separatorChar;
        }
        return child_s.startsWith(parent_s);
    }

    public static File canonise(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ioe) {
            return file;
        }
    }

    public static String getCanonicalFileName(String filename) {
        String canonicalFileName = filename;
        try {
            canonicalFileName = new File(filename).getCanonicalPath();
        } catch (IOException ignore) {
        }
        return canonicalFileName;
    }

    public static File getUserFile(String filename) {
        return new File(SystemProperties.getUserPath(), filename);
    }

    public static File getApplicationFile(String filename) {
        String path = SystemProperties.getApplicationPath();
        if (Constants.isOSX) {
            path = path + "/" + SystemProperties.getApplicationName() + ".app/Contents/";
        }
        return new File(path, filename);
    }

    /**
   * Deletes the given dir and all files/dirs underneath
   */
    public static boolean recursiveDelete(File f) {
        String defSaveDir = COConfigurationManager.getStringParameter("Default save path");
        String moveToDir = COConfigurationManager.getStringParameter("Completed Files Directory", "");
        try {
            moveToDir = new File(moveToDir).getCanonicalPath();
        } catch (Throwable e) {
        }
        try {
            defSaveDir = new File(defSaveDir).getCanonicalPath();
        } catch (Throwable e) {
        }
        try {
            if (f.getCanonicalPath().equals(moveToDir)) {
                System.out.println("FileUtil::recursiveDelete:: not allowed to delete the MoveTo dir !");
                return (false);
            }
            if (f.getCanonicalPath().equals(defSaveDir)) {
                System.out.println("FileUtil::recursiveDelete:: not allowed to delete the default data dir !");
                return (false);
            }
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!recursiveDelete(files[i])) {
                        return (false);
                    }
                }
                if (!f.delete()) {
                    return (false);
                }
            } else {
                if (!f.delete()) {
                    return (false);
                }
            }
        } catch (Exception ignore) {
        }
        return (true);
    }

    public static boolean recursiveDeleteNoCheck(File f) {
        try {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!recursiveDeleteNoCheck(files[i])) {
                        return (false);
                    }
                }
                if (!f.delete()) {
                    return (false);
                }
            } else {
                if (!f.delete()) {
                    return (false);
                }
            }
        } catch (Exception ignore) {
        }
        return (true);
    }

    public static long getFileOrDirectorySize(File file) {
        if (file.isFile()) {
            return (file.length());
        } else {
            long res = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    res += getFileOrDirectorySize(files[i]);
                }
            }
            return (res);
        }
    }

    protected static void recursiveEmptyDirDelete(File f, Set ignore_set, boolean log_warnings) {
        try {
            String defSaveDir = COConfigurationManager.getStringParameter("Default save path");
            String moveToDir = COConfigurationManager.getStringParameter("Completed Files Directory", "");
            if (defSaveDir.trim().length() > 0) {
                defSaveDir = new File(defSaveDir).getCanonicalPath();
            }
            if (moveToDir.trim().length() > 0) {
                moveToDir = new File(moveToDir).getCanonicalPath();
            }
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (files == null) {
                    if (log_warnings) {
                        Debug.out("Empty folder delete:  failed to list contents of directory " + f);
                    }
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    File x = files[i];
                    if (x.isDirectory()) {
                        recursiveEmptyDirDelete(files[i], ignore_set, log_warnings);
                    } else {
                        if (ignore_set.contains(x.getName().toLowerCase())) {
                            if (!x.delete()) {
                                if (log_warnings) {
                                    Debug.out("Empty folder delete: failed to delete file " + x);
                                }
                            }
                        }
                    }
                }
                if (f.getCanonicalPath().equals(moveToDir)) {
                    if (log_warnings) {
                        Debug.out("Empty folder delete:  not allowed to delete the MoveTo dir !");
                    }
                    return;
                }
                if (f.getCanonicalPath().equals(defSaveDir)) {
                    if (log_warnings) {
                        Debug.out("Empty folder delete:  not allowed to delete the default data dir !");
                    }
                    return;
                }
                File[] files_inside = f.listFiles();
                if (files_inside.length == 0) {
                    if (!f.delete()) {
                        if (log_warnings) {
                            Debug.out("Empty folder delete:  failed to delete directory " + f);
                        }
                    }
                } else {
                    if (log_warnings) {
                        Debug.out("Empty folder delete:  " + files_inside.length + " file(s)/folder(s) still in \"" + f + "\" - first listed item is \"" + files_inside[0].getName() + "\". Not removing.");
                    }
                }
            }
        } catch (Exception e) {
            Debug.out(e.toString());
        }
    }

    public static String convertOSSpecificChars(String file_name_in, boolean is_folder) {
        char[] chars = file_name_in.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '"') {
                chars[i] = '\'';
            }
        }
        if (!Constants.isOSX) {
            if (Constants.isWindows) {
                String not_allowed = "\\/:?*<>|";
                for (int i = 0; i < chars.length; i++) {
                    if (not_allowed.indexOf(chars[i]) != -1) {
                        chars[i] = '_';
                    }
                }
                if (is_folder) {
                    for (int i = chars.length - 1; i >= 0 && (chars[i] == '.' || chars[i] == ' '); chars[i] = '_', i--) ;
                }
            }
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '/' || c == '\r' || c == '\n') {
                    chars[i] = ' ';
                }
            }
        }
        String file_name_out = new String(chars);
        try {
            if (Constants.isWindows) {
                while (file_name_out.endsWith(" ")) {
                    file_name_out = file_name_out.substring(0, file_name_out.length() - 1);
                }
            } else {
                String str = new File(file_name_out).getCanonicalFile().toString();
                int p = str.lastIndexOf(File.separator);
                file_name_out = str.substring(p + 1);
            }
        } catch (Throwable e) {
        }
        return (file_name_out);
    }

    public static void writeResilientConfigFile(String file_name, Map data) {
        File parent_dir = new File(SystemProperties.getUserPath());
        boolean use_backups = COConfigurationManager.getBooleanParameter("Use Config File Backups");
        writeResilientFile(parent_dir, file_name, data, use_backups);
    }

    public static void writeResilientFile(File file, Map data) {
        writeResilientFile(file.getParentFile(), file.getName(), data, false);
    }

    public static boolean writeResilientFileWithResult(File parent_dir, String file_name, Map data) {
        return (writeResilientFile(parent_dir, file_name, data));
    }

    public static void writeResilientFile(File parent_dir, String file_name, Map data, boolean use_backup) {
        writeResilientFile(parent_dir, file_name, data, use_backup, true);
    }

    public static void writeResilientFile(File parent_dir, String file_name, Map data, boolean use_backup, boolean copy_to_backup) {
        if (use_backup) {
            File originator = new File(parent_dir, file_name);
            if (originator.exists()) {
                backupFile(originator, copy_to_backup);
            }
        }
        writeResilientFile(parent_dir, file_name, data);
    }

    private static boolean writeResilientFile(File parent_dir, String file_name, Map data) {
        try {
            class_mon.enter();
            try {
                getReservedFileHandles();
                File temp = new File(parent_dir, file_name + ".saving");
                BufferedOutputStream baos = null;
                try {
                    byte[] encoded_data = BEncoder.encode(data);
                    FileOutputStream tempOS = new FileOutputStream(temp, false);
                    baos = new BufferedOutputStream(tempOS, 8192);
                    baos.write(encoded_data);
                    baos.flush();
                    if (!Constants.isCVSVersion()) {
                        tempOS.getFD().sync();
                    }
                    baos.close();
                    baos = null;
                    if (temp.length() > 1L) {
                        File file = new File(parent_dir, file_name);
                        if (file.exists()) {
                            if (!file.delete()) {
                                Debug.out("Save of '" + file_name + "' fails - couldn't delete " + file.getAbsolutePath());
                            }
                        }
                        if (temp.renameTo(file)) {
                            return (true);
                        } else {
                            Debug.out("Save of '" + file_name + "' fails - couldn't rename " + temp.getAbsolutePath() + " to " + file.getAbsolutePath());
                        }
                    }
                    return (false);
                } catch (Throwable e) {
                    Debug.out("Save of '" + file_name + "' fails", e);
                    return (false);
                } finally {
                    try {
                        if (baos != null) {
                            baos.close();
                        }
                    } catch (Exception e) {
                        Debug.out("Save of '" + file_name + "' fails", e);
                        return (false);
                    }
                }
            } finally {
                releaseReservedFileHandles();
            }
        } finally {
            class_mon.exit();
        }
    }

    public static boolean resilientConfigFileExists(String name) {
        File parent_dir = new File(SystemProperties.getUserPath());
        boolean use_backups = COConfigurationManager.getBooleanParameter("Use Config File Backups");
        return (new File(parent_dir, name).exists() || (use_backups && new File(parent_dir, name + ".bak").exists()));
    }

    public static Map readResilientConfigFile(String file_name) {
        File parent_dir = new File(SystemProperties.getUserPath());
        boolean use_backups = COConfigurationManager.getBooleanParameter("Use Config File Backups");
        return (readResilientFile(parent_dir, file_name, use_backups));
    }

    public static Map readResilientConfigFile(String file_name, boolean use_backups) {
        File parent_dir = new File(SystemProperties.getUserPath());
        if (!use_backups) {
            if (new File(parent_dir, file_name + ".bak").exists()) {
                use_backups = true;
            }
        }
        return (readResilientFile(parent_dir, file_name, use_backups));
    }

    public static Map readResilientFile(File file) {
        return (readResilientFile(file.getParentFile(), file.getName(), false, true));
    }

    public static Map readResilientFile(File parent_dir, String file_name, boolean use_backup) {
        return readResilientFile(parent_dir, file_name, use_backup, true);
    }

    public static Map readResilientFile(File parent_dir, String file_name, boolean use_backup, boolean intern_keys) {
        File backup_file = new File(parent_dir, file_name + ".bak");
        if (use_backup) {
            use_backup = backup_file.exists();
        }
        Map res = readResilientFileSupport(parent_dir, file_name, !use_backup, intern_keys);
        if (res == null && use_backup) {
            res = readResilientFileSupport(parent_dir, file_name + ".bak", false, intern_keys);
            if (res != null) {
                Debug.out("Backup file '" + backup_file + "' has been used for recovery purposes");
                writeResilientFile(parent_dir, file_name, res, false);
            } else {
                res = readResilientFileSupport(parent_dir, file_name, true, true);
            }
        }
        if (res == null) {
            res = new HashMap();
        }
        return (res);
    }

    private static Map readResilientFileSupport(File parent_dir, String file_name, boolean attempt_recovery, boolean intern_keys) {
        try {
            class_mon.enter();
            try {
                getReservedFileHandles();
                Map res = null;
                try {
                    res = readResilientFile(file_name, parent_dir, file_name, 0, false, intern_keys);
                } catch (Throwable e) {
                }
                if (res == null && attempt_recovery) {
                    res = readResilientFile(file_name, parent_dir, file_name, 0, true, intern_keys);
                    if (res != null) {
                        Debug.out("File '" + file_name + "' has been partially recovered, information may have been lost!");
                    }
                }
                return (res);
            } catch (Throwable e) {
                Debug.printStackTrace(e);
                return (null);
            } finally {
                releaseReservedFileHandles();
            }
        } finally {
            class_mon.exit();
        }
    }

    private static Map readResilientFile(String original_file_name, File parent_dir, String file_name, int fail_count, boolean recovery_mode, boolean skip_key_intern) {
        boolean using_backup = file_name.endsWith(".saving");
        File file = new File(parent_dir, file_name);
        if ((!file.exists()) || file.length() <= 1L) {
            if (using_backup) {
                if (!recovery_mode) {
                    if (fail_count == 1) {
                        Debug.out("Load of '" + original_file_name + "' fails, no usable file or backup");
                    } else {
                    }
                }
                return (null);
            }
            if (!recovery_mode) {
            }
            return (readResilientFile(original_file_name, parent_dir, file_name + ".saving", 0, recovery_mode, true));
        }
        BufferedInputStream bin = null;
        try {
            int retry_limit = 5;
            while (true) {
                try {
                    bin = new BufferedInputStream(new FileInputStream(file), 16384);
                    break;
                } catch (IOException e) {
                    if (--retry_limit == 0) {
                        throw (e);
                    }
                    if (Logger.isEnabled()) Logger.log(new LogEvent(LOGID, "Failed to open '" + file.toString() + "', retrying", e));
                    Thread.sleep(500);
                }
            }
            BDecoder decoder = new BDecoder();
            if (recovery_mode) {
                decoder.setRecoveryMode(true);
            }
            Map res = decoder.decodeStream(bin, !skip_key_intern);
            if (using_backup && !recovery_mode) {
                Debug.out("Load of '" + original_file_name + "' had to revert to backup file");
            }
            return (res);
        } catch (Throwable e) {
            Debug.printStackTrace(e);
            try {
                if (bin != null) {
                    bin.close();
                    bin = null;
                }
            } catch (Exception x) {
                Debug.printStackTrace(x);
            }
            if (!recovery_mode) {
                File bad;
                int bad_id = 0;
                while (true) {
                    File test = new File(parent_dir, file.getName() + ".bad" + (bad_id == 0 ? "" : ("" + bad_id)));
                    if (!test.exists()) {
                        bad = test;
                        break;
                    }
                    bad_id++;
                }
                if (Logger.isEnabled()) Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING, "Read of '" + original_file_name + "' failed, decoding error. " + "Renaming to " + bad.getName()));
                copyFile(file, bad);
            }
            if (using_backup) {
                if (!recovery_mode) {
                    Debug.out("Load of '" + original_file_name + "' fails, no usable file or backup");
                }
                return (null);
            }
            return (readResilientFile(original_file_name, parent_dir, file_name + ".saving", 1, recovery_mode, true));
        } finally {
            try {
                if (bin != null) {
                    bin.close();
                }
            } catch (Exception e) {
                Debug.printStackTrace(e);
            }
        }
    }

    public static void deleteResilientFile(File file) {
        file.delete();
        new File(file.getParentFile(), file.getName() + ".bak").delete();
    }

    public static void deleteResilientConfigFile(String name) {
        File parent_dir = new File(SystemProperties.getUserPath());
        new File(parent_dir, name).delete();
        new File(parent_dir, name + ".bak").delete();
    }

    private static void getReservedFileHandles() {
        try {
            class_mon.enter();
            while (reserved_file_handles.size() > 0) {
                InputStream is = (InputStream) reserved_file_handles.remove(0);
                try {
                    is.close();
                } catch (Throwable e) {
                    Debug.printStackTrace(e);
                }
            }
        } finally {
            class_mon.exit();
        }
    }

    private static void releaseReservedFileHandles() {
        try {
            class_mon.enter();
            File lock_file = new File(SystemProperties.getUserPath() + ".lock");
            lock_file.createNewFile();
            while (reserved_file_handles.size() < RESERVED_FILE_HANDLE_COUNT) {
                InputStream is = new FileInputStream(lock_file);
                reserved_file_handles.add(is);
            }
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        } finally {
            class_mon.exit();
        }
    }

    /**
     * Backup the given file to filename.bak, removing the old .bak file if necessary.
     * If _make_copy is true, the original file will copied to backup, rather than moved.
     * @param _filename name of file to backup
     * @param _make_copy copy instead of move
     */
    public static void backupFile(final String _filename, final boolean _make_copy) {
        backupFile(new File(_filename), _make_copy);
    }

    /**
     * Backup the given file to filename.bak, removing the old .bak file if necessary.
     * If _make_copy is true, the original file will copied to backup, rather than moved.
     * @param _file file to backup
     * @param _make_copy copy instead of move
     */
    public static void backupFile(final File _file, final boolean _make_copy) {
        if (_file.length() > 0L) {
            File bakfile = new File(_file.getAbsolutePath() + ".bak");
            if (bakfile.exists()) bakfile.delete();
            if (_make_copy) {
                copyFile(_file, bakfile);
            } else {
                _file.renameTo(bakfile);
            }
        }
    }

    /**
     * Copy the given source file to the given destination file.
     * Returns file copy success or not.
     * @param _source_name source file name
     * @param _dest_name destination file name
     * @return true if file copy successful, false if copy failed
     */
    public static boolean copyFile(final String _source_name, final String _dest_name) {
        return copyFile(new File(_source_name), new File(_dest_name));
    }

    public static boolean copyFile(final File _source, final File _dest) {
        try {
            copyFile(new FileInputStream(_source), new FileOutputStream(_dest));
            return true;
        } catch (Throwable e) {
            Debug.printStackTrace(e);
            return false;
        }
    }

    public static void copyFileWithException(final File _source, final File _dest) throws IOException {
        copyFile(new FileInputStream(_source), new FileOutputStream(_dest));
    }

    public static boolean copyFile(final File _source, final OutputStream _dest, boolean closeInputStream) {
        try {
            copyFile(new FileInputStream(_source), _dest, closeInputStream);
            return true;
        } catch (Throwable e) {
            Debug.printStackTrace(e);
            return false;
        }
    }

    /**
    	 * copys the input stream to the file. always closes the input stream
    	 * @param _source
    	 * @param _dest
    	 * @throws IOException
    	 */
    public static void copyFile(final InputStream _source, final File _dest) throws IOException {
        FileOutputStream dest = null;
        boolean close_input = true;
        try {
            dest = new FileOutputStream(_dest);
            close_input = false;
            copyFile(_source, dest, true);
        } finally {
            try {
                if (close_input) {
                    _source.close();
                }
            } catch (IOException e) {
            }
            if (dest != null) {
                dest.close();
            }
        }
    }

    public static void copyFile(final InputStream _source, final File _dest, boolean _close_input_stream) throws IOException {
        FileOutputStream dest = null;
        boolean close_input = _close_input_stream;
        try {
            dest = new FileOutputStream(_dest);
            close_input = false;
            copyFile(_source, dest, close_input);
        } finally {
            try {
                if (close_input) {
                    _source.close();
                }
            } catch (IOException e) {
            }
            if (dest != null) {
                dest.close();
            }
        }
    }

    public static void copyFile(InputStream is, OutputStream os) throws IOException {
        copyFile(is, os, true);
    }

    public static void copyFile(InputStream is, OutputStream os, boolean closeInputStream) throws IOException {
        try {
            if (!(is instanceof BufferedInputStream)) {
                is = new BufferedInputStream(is);
            }
            byte[] buffer = new byte[65536 * 2];
            while (true) {
                int len = is.read(buffer);
                if (len == -1) {
                    break;
                }
                os.write(buffer, 0, len);
            }
        } finally {
            try {
                if (closeInputStream) {
                    is.close();
                }
            } catch (IOException e) {
            }
            os.close();
        }
    }

    public static void copyFileOrDirectory(File from_file_or_dir, File to_parent_dir) throws IOException {
        if (!from_file_or_dir.exists()) {
            throw (new IOException("File '" + from_file_or_dir.toString() + "' doesn't exist"));
        }
        if (!to_parent_dir.exists()) {
            throw (new IOException("File '" + to_parent_dir.toString() + "' doesn't exist"));
        }
        if (!to_parent_dir.isDirectory()) {
            throw (new IOException("File '" + to_parent_dir.toString() + "' is not a directory"));
        }
        if (from_file_or_dir.isDirectory()) {
            File[] files = from_file_or_dir.listFiles();
            File new_parent = new File(to_parent_dir, from_file_or_dir.getName());
            FileUtil.mkdirs(new_parent);
            for (int i = 0; i < files.length; i++) {
                File from_file = files[i];
                copyFileOrDirectory(from_file, new_parent);
            }
        } else {
            File target = new File(to_parent_dir, from_file_or_dir.getName());
            if (!copyFile(from_file_or_dir, target)) {
                throw (new IOException("File copy from " + from_file_or_dir + " to " + target + " failed"));
            }
        }
    }

    /**
     * Returns the file handle for the given filename or it's
     * equivalent .bak backup file if the original doesn't exist
     * or is 0-sized.  If neither the original nor the backup are
     * available, a null handle is returned.
     * @param _filename root name of file
     * @return file if successful, null if failed
     */
    public static File getFileOrBackup(final String _filename) {
        try {
            File file = new File(_filename);
            if (file.length() <= 1L) {
                File bakfile = new File(_filename + ".bak");
                if (bakfile.length() <= 1L) {
                    return null;
                } else return bakfile;
            } else return file;
        } catch (Exception e) {
            Debug.out(e);
            return null;
        }
    }

    public static File getJarFileFromClass(Class cla) {
        try {
            String str = cla.getName();
            str = str.replace('.', '/') + ".class";
            URL url = cla.getClassLoader().getResource(str);
            if (url != null) {
                String url_str = url.toExternalForm();
                if (url_str.startsWith("jar:file:")) {
                    File jar_file = FileUtil.getJarFileFromURL(url_str);
                    if (jar_file != null && jar_file.exists()) {
                        return (jar_file);
                    }
                }
            }
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
        return (null);
    }

    public static File getJarFileFromURL(String url_str) {
        if (url_str.startsWith("jar:file:")) {
            url_str = url_str.replaceAll(" ", "%20");
            if (!url_str.startsWith("jar:file:/")) {
                url_str = "jar:file:/".concat(url_str.substring(9));
            }
            try {
                int posPling = url_str.lastIndexOf('!');
                String jarName = url_str.substring(4, posPling);
                URI uri;
                try {
                    uri = URI.create(jarName);
                    if (!new File(uri).exists()) {
                        throw (new FileNotFoundException());
                    }
                } catch (Throwable e) {
                    jarName = "file:/" + UrlUtils.encode(jarName.substring(6));
                    uri = URI.create(jarName);
                }
                File jar = new File(uri);
                return (jar);
            } catch (Throwable e) {
                Debug.printStackTrace(e);
            }
        }
        return (null);
    }

    public static boolean renameFile(File from_file, File to_file) {
        return renameFile(from_file, to_file, true);
    }

    public static boolean renameFile(File from_file, File to_file, boolean fail_on_existing_directory) {
        return renameFile(from_file, to_file, fail_on_existing_directory, null);
    }

    public static boolean renameFile(File from_file, File to_file, boolean fail_on_existing_directory, FileFilter file_filter) {
        if (!from_file.exists()) {
            Debug.out("renameFile: source file '" + from_file + "' doesn't exist, failing");
            return (false);
        }
        if (to_file.exists() && (fail_on_existing_directory || from_file.isFile() || to_file.isFile())) {
            Debug.out("renameFile: target file '" + to_file + "' already exists, failing");
            return (false);
        }
        File to_file_parent = to_file.getParentFile();
        if (!to_file_parent.exists()) {
            FileUtil.mkdirs(to_file_parent);
        }
        if (from_file.isDirectory()) {
            File[] files = null;
            if (file_filter != null) {
                files = from_file.listFiles(file_filter);
            } else {
                files = from_file.listFiles();
            }
            if (files == null) {
                return (true);
            }
            int last_ok = 0;
            if (!to_file.exists()) {
                to_file.mkdir();
            }
            for (int i = 0; i < files.length; i++) {
                File ff = files[i];
                File tf = new File(to_file, ff.getName());
                try {
                    if (renameFile(ff, tf, fail_on_existing_directory, file_filter)) {
                        last_ok++;
                    } else {
                        break;
                    }
                } catch (Throwable e) {
                    Debug.out("renameFile: failed to rename file '" + ff.toString() + "' to '" + tf.toString() + "'", e);
                    break;
                }
            }
            if (last_ok == files.length) {
                File[] remaining = from_file.listFiles();
                if (remaining != null && remaining.length > 0) {
                    if (file_filter == null) {
                        Debug.out("renameFile: files remain in '" + from_file.toString() + "', not deleting");
                    } else {
                        return true;
                    }
                } else {
                    if (!from_file.delete()) {
                        Debug.out("renameFile: failed to delete '" + from_file.toString() + "'");
                    }
                }
                return (true);
            }
            for (int i = 0; i < last_ok; i++) {
                File ff = files[i];
                File tf = new File(to_file, ff.getName());
                try {
                    if (!renameFile(tf, ff, false, null)) {
                        Debug.out("renameFile: recovery - failed to move file '" + tf.toString() + "' to '" + ff.toString() + "'");
                    }
                } catch (Throwable e) {
                    Debug.out("renameFile: recovery - failed to move file '" + tf.toString() + "' to '" + ff.toString() + "'", e);
                }
            }
            return (false);
        } else {
            if ((!COConfigurationManager.getBooleanParameter("Copy And Delete Data Rather Than Move")) && from_file.renameTo(to_file)) {
                return (true);
            } else {
                boolean success = false;
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(from_file);
                    fos = new FileOutputStream(to_file);
                    byte[] buffer = new byte[65536];
                    while (true) {
                        int len = fis.read(buffer);
                        if (len <= 0) {
                            break;
                        }
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    fos = null;
                    fis.close();
                    fis = null;
                    if (!from_file.delete()) {
                        Debug.out("renameFile: failed to delete '" + from_file.toString() + "'");
                        throw (new Exception("Failed to delete '" + from_file.toString() + "'"));
                    }
                    success = true;
                    return (true);
                } catch (Throwable e) {
                    Debug.out("renameFile: failed to rename '" + from_file.toString() + "' to '" + to_file.toString() + "'", e);
                    return (false);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Throwable e) {
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Throwable e) {
                        }
                    }
                    if (!success) {
                        if (to_file.exists()) {
                            to_file.delete();
                        }
                    }
                }
            }
        }
    }

    public static boolean writeStringAsFile(File file, String text) {
        try {
            return (writeBytesAsFile2(file.getAbsolutePath(), text.getBytes("UTF-8")));
        } catch (Throwable e) {
            Debug.out(e);
            return (false);
        }
    }

    public static void writeBytesAsFile(String filename, byte[] file_data) {
        writeBytesAsFile2(filename, file_data);
    }

    public static boolean writeBytesAsFile2(String filename, byte[] file_data) {
        try {
            File file = new File(filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(file_data);
            out.close();
            return (true);
        } catch (Throwable t) {
            Debug.out("writeBytesAsFile:: error: ", t);
            return (false);
        }
    }

    public static boolean deleteWithRecycle(File file, boolean force_no_recycle) {
        if (COConfigurationManager.getBooleanParameter("Move Deleted Data To Recycle Bin") && !force_no_recycle) {
            try {
                final PlatformManager platform = PlatformManagerFactory.getPlatformManager();
                if (platform.hasCapability(PlatformManagerCapabilities.RecoverableFileDelete)) {
                    platform.performRecoverableFileDelete(file.getAbsolutePath());
                    return (true);
                } else {
                    return (file.delete());
                }
            } catch (PlatformManagerException e) {
                return (file.delete());
            }
        } else {
            return (file.delete());
        }
    }

    public static String translateMoveFilePath(String old_root, String new_root, String file_to_move) {
        if (!file_to_move.startsWith(old_root)) {
            return null;
        }
        String file_suffix = file_to_move.substring(old_root.length());
        if (new_root.endsWith(File.separator)) {
            new_root = new_root.substring(0, new_root.length() - 1);
        }
        if (file_suffix.startsWith(File.separator)) {
            file_suffix = file_suffix.substring(1);
        }
        return new_root + File.separator + file_suffix;
    }

    public static void runAsTask(AzureusCoreOperationTask task) {
        AzureusCore core = AzureusCoreFactory.getSingleton();
        core.createOperation(AzureusCoreOperation.OP_FILE_MOVE, task);
    }

    /**
	 * Makes Directories as long as the directory isn't directly in Volumes (OSX)
	 * @param f
	 * @return
	 */
    public static boolean mkdirs(File f) {
        if (Constants.isOSX) {
            Pattern pat = Pattern.compile("^(/Volumes/[^/]+)");
            Matcher matcher = pat.matcher(f.getParent());
            if (matcher.find()) {
                String sVolume = matcher.group();
                File fVolume = new File(sVolume);
                if (!fVolume.isDirectory()) {
                    Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING, sVolume + " is not mounted or not available."));
                    return false;
                }
            }
        }
        return f.mkdirs();
    }

    public static String getExtension(String fName) {
        final int fileSepIndex = fName.lastIndexOf(File.separator);
        final int fileDotIndex = fName.lastIndexOf('.');
        if (fileSepIndex == fName.length() - 1 || fileDotIndex == -1 || fileSepIndex > fileDotIndex) {
            return "";
        }
        return fName.substring(fileDotIndex);
    }

    public static String readFileAsString(File file, int size_limit, String charset) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return readInputStreamAsString(fis, size_limit, charset);
        } finally {
            fis.close();
        }
    }

    public static String readFileAsString(File file, int size_limit) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return readInputStreamAsString(fis, size_limit);
        } finally {
            fis.close();
        }
    }

    public static String readInputStreamAsString(InputStream is, int size_limit) throws IOException {
        return readInputStreamAsString(is, size_limit, "ISO-8859-1");
    }

    public static String readInputStreamAsString(InputStream is, int size_limit, String charSet) throws IOException {
        StringBuffer result = new StringBuffer(1024);
        byte[] buffer = new byte[1024];
        while (true) {
            int len = is.read(buffer);
            if (len <= 0) {
                break;
            }
            result.append(new String(buffer, 0, len, charSet));
            if (size_limit >= 0 && result.length() > size_limit) {
                result.setLength(size_limit);
                break;
            }
        }
        return (result.toString());
    }

    public static String readInputStreamAsStringWithTruncation(InputStream is, int size_limit) throws IOException {
        StringBuffer result = new StringBuffer(1024);
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                int len = is.read(buffer);
                if (len <= 0) {
                    break;
                }
                result.append(new String(buffer, 0, len, "ISO-8859-1"));
                if (size_limit >= 0 && result.length() > size_limit) {
                    result.setLength(size_limit);
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
        }
        return (result.toString());
    }

    public static String readFileEndAsString(File file, int size_limit) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            if (file.length() > size_limit) {
                fis.skip(file.length() - size_limit);
            }
            StringBuffer result = new StringBuffer(1024);
            byte[] buffer = new byte[1024];
            while (true) {
                int len = fis.read(buffer);
                if (len <= 0) {
                    break;
                }
                result.append(new String(buffer, 0, len, "ISO-8859-1"));
                if (result.length() > size_limit) {
                    result.setLength(size_limit);
                    break;
                }
            }
            return (result.toString());
        } finally {
            fis.close();
        }
    }

    public static byte[] readInputStreamAsByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(32 * 1024);
        byte[] buffer = new byte[32 * 1024];
        while (true) {
            int len = is.read(buffer);
            if (len <= 0) {
                break;
            }
            baos.write(buffer, 0, len);
        }
        return (baos.toByteArray());
    }

    public static byte[] readFileAsByteArray(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) file.length());
        byte[] buffer = new byte[32 * 1024];
        InputStream is = new FileInputStream(file);
        try {
            while (true) {
                int len = is.read(buffer);
                if (len <= 0) {
                    break;
                }
                baos.write(buffer, 0, len);
            }
            return (baos.toByteArray());
        } finally {
            is.close();
        }
    }

    public static final boolean getUsableSpaceSupported() {
        return reflectOnUsableSpace != null;
    }

    public static final long getUsableSpace(File f) {
        try {
            return ((Long) reflectOnUsableSpace.invoke(f)).longValue();
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean canReallyWriteToAppDirectory() {
        if (!FileUtil.getApplicationFile("bogus").getParentFile().canWrite()) {
            return (false);
        }
        if (Constants.isWindowsVistaOrHigher) {
            try {
                File write_test = FileUtil.getApplicationFile("_az_.dll");
                FileOutputStream fos = new FileOutputStream(write_test);
                fos.write(32);
                fos.close();
                write_test.delete();
                File rename_test = FileUtil.getApplicationFile("License.txt");
                if (!rename_test.exists()) {
                    rename_test = FileUtil.getApplicationFile("GPL.txt");
                }
                if (!rename_test.exists()) {
                    File[] files = write_test.getParentFile().listFiles();
                    if (files != null) {
                        for (File f : files) {
                            String name = f.getName();
                            if (name.endsWith(".txt") || name.endsWith(".log")) {
                                rename_test = f;
                                break;
                            }
                        }
                    }
                }
                if (rename_test.exists()) {
                    File target = new File(rename_test.getParentFile(), rename_test.getName() + ".bak");
                    target.delete();
                    rename_test.renameTo(target);
                    if (rename_test.exists()) {
                        return (false);
                    }
                    target.renameTo(rename_test);
                } else {
                    Debug.out("Failed to find a suitable file for the rename test");
                    return (false);
                }
            } catch (Throwable e) {
                return (false);
            }
        }
        return (true);
    }

    /**
		 * Gets the encoding that should be used when writing script files (currently only
		 * tested for windows as this is where an issue can arise...)
		 * We also only test based on the user-data directory name to see if an explicit
		 * encoding switch is requried...
		 * @return null - use default
		 */
    private static boolean sce_checked;

    private static String script_encoding;

    public static String getScriptCharsetEncoding() {
        synchronized (FileUtil.class) {
            if (sce_checked) {
                return (script_encoding);
            }
            sce_checked = true;
            String file_encoding = System.getProperty("file.encoding", null);
            String jvm_encoding = System.getProperty("sun.jnu.encoding", null);
            if (file_encoding == null || jvm_encoding == null || file_encoding.equals(jvm_encoding)) {
                return (null);
            }
            try {
                String test_str = SystemProperties.getUserPath();
                if (!new String(test_str.getBytes(file_encoding), file_encoding).equals(test_str)) {
                    if (new String(test_str.getBytes(jvm_encoding), jvm_encoding).equals(test_str)) {
                        Debug.out("Script encoding determined to be " + jvm_encoding + " instead of " + file_encoding);
                        script_encoding = jvm_encoding;
                    }
                }
            } catch (Throwable e) {
            }
            return (script_encoding);
        }
    }
}
