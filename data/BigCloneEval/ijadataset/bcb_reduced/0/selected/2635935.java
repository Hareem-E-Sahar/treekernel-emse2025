package de.fuhrmeister.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileManager {

    public final int ERROR_FILEDOESNOTEXIST = 1;

    public final int ERROR_FILEALREADYEXISTS = 2;

    public final int ERROR_CANTCOPY = 3;

    public final int ERROR_CANTCLOSEINPUTSTREAM = 4;

    public final int ERROR_CANTCLOSEOUTPUTSTREAM = 5;

    public final int ERROR_STREAMFAILURE = 6;

    public final int NO_ERRORS = 77;

    private static FileManager fileMng;

    private StringBuffer bufferedFile;

    public final String userDir = System.getProperty("user.dir") + System.getProperty("file.separator");

    public static synchronized FileManager getInstance() {
        if (fileMng == null) {
            FileManager.fileMng = new FileManager();
            return FileManager.fileMng;
        }
        return FileManager.fileMng;
    }

    public FileManager() {
        super();
    }

    public InputStream openUTF8File(final String file) throws FileNotFoundException {
        final File f = new File(file);
        InputStream fis = null;
        try {
            if (!f.exists() || f.length() == 0) {
                throw new FileNotFoundException("FileManager::openUTF8File() - configuration file or path does not exist");
            } else {
                fis = new FileInputStream(f);
            }
        } catch (final IOException e) {
            throw new FileNotFoundException("FileManager::openUTF8File() - system cannot find the given path - " + file);
        }
        return fis;
    }

    public StringBuffer readUTF8File(final String file) throws FileNotFoundException {
        bufferedFile = new StringBuffer("");
        final InputStream inputStream = openUTF8File(file);
        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String currentLine;
        try {
            while ((currentLine = br.readLine()) != null) {
                bufferedFile.append(currentLine);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return bufferedFile;
    }

    public InputStream getResource(final String file) {
        return this.getClass().getClassLoader().getResourceAsStream(file);
    }

    public OutputStream getOutputStream(final String file) {
        final File f = new File(file);
        try {
            if (!f.createNewFile()) {
                try {
                    return new FileOutputStream(f);
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeUTF8File(final StringBuffer encoded, final String file) throws FileNotFoundException, IOException {
        final File f = new File(file);
        OutputStream fos = null;
        if (!f.createNewFile()) {
            try {
                fos = new FileOutputStream(f);
                final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                bw.write(encoded.toString());
                bw.flush();
                bw.close();
            } catch (final IOException ioe) {
                throw new IOException();
            }
        } else {
            throw new FileNotFoundException("FileManager::writeUTF8File() - file \"" + file + "\" does not exist");
        }
    }

    public void newUTF8File(final StringBuffer encoded, final String file) throws IOException {
        final File f = new File(file);
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(encoded.toString());
            bw.flush();
            bw.close();
        } catch (final IOException e) {
            throw new IOException("FileManager::newUTF8File() - unexpected error while streaming file: " + file);
        }
    }

    public boolean mkdir(final String applicationPath, final String configFolder) {
        File folder = new File(applicationPath + "\\" + configFolder);
        final boolean returnValue = folder.mkdir();
        folder = null;
        return returnValue;
    }

    public void serialize(final Object obj, final String file) {
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            final ObjectOutputStream o = new ObjectOutputStream(fos);
            o.writeObject(obj);
        } catch (final IOException e) {
            System.err.println(e);
        } finally {
            try {
                fos.close();
            } catch (final Exception e) {
            }
        }
    }

    public Object deserialize(final String file) {
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            final ObjectInputStream o = new ObjectInputStream(fis);
            return o.readObject();
        } catch (final IOException e) {
            System.err.println("FUCK - " + e);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (final Exception e) {
            }
        }
        return null;
    }

    public void copy(String sourceDir, final String file, final boolean isSourceDirRelativeToUserDir, String destDir, final boolean isDestDirRelativeToUserDir) throws IOException {
        if (isSourceDirRelativeToUserDir) {
            sourceDir = userDir + System.getProperty("file.separator") + sourceDir + System.getProperty("file.separator");
        } else {
            sourceDir += System.getProperty("file.separator");
        }
        if (isDestDirRelativeToUserDir) {
            destDir = userDir + System.getProperty("file.separator") + destDir + System.getProperty("file.separator");
        } else {
            destDir += System.getProperty("file.separator");
        }
        sourceDir += file;
        destDir += file;
        copyFile(sourceDir, destDir);
    }

    public void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[0xFFFF];
        for (int len; (len = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, len);
        }
    }

    public void copyFile(final String src, final String dest) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            copy(fis, fos);
        } catch (final FileNotFoundException e) {
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (final IOException e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    public void erase(final String file) {
        final File f = new File(file);
        f.delete();
    }

    public void deleteEmptyFiles(final String dir, final String file_ext) {
        final File userdir = new File(dir);
        for (final String entry : userdir.list(new FileManager.Filter(file_ext))) {
            final File file = new File(dir + System.getProperty("separator") + entry);
            if (file.length() == 0) {
                file.delete();
            }
        }
    }

    public boolean exists(final String file) {
        return new File(file).exists();
    }

    public void initFolder(final String folder) {
        final File f = new File(folder);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    public ArrayList<String> getFileNames(final String dir, final String file_ext) {
        final File userdir = new File(dir);
        final ArrayList<String> afiles = new ArrayList<String>();
        for (final String entry : userdir.list(new FileManager.Filter(file_ext))) {
            afiles.add(entry.replaceAll(file_ext, ""));
        }
        return afiles;
    }

    public String parseLink(File f) throws Exception {
        FileInputStream fin = new FileInputStream(f);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while (true) {
            int n = fin.read(buff);
            if (n == -1) {
                break;
            }
            bout.write(buff, 0, n);
        }
        fin.close();
        byte[] link = bout.toByteArray();
        byte flags = link[0x14];
        final int shell_offset = 0x4c;
        int shell_len = 0;
        if ((flags & 0x1) > 0) {
            shell_len = bytes2short(link, shell_offset) + 2;
        }
        int file_start = 0x4c + shell_len;
        int local_sys_off = link[file_start + 0x10] + file_start;
        String l = new String(link);
        return getNullDelimitedString(link, local_sys_off);
    }

    private String getNullDelimitedString(byte[] bytes, int off) {
        int len = 0;
        while (true) {
            if (bytes[off + len] == 0) {
                break;
            }
            len++;
        }
        return new String(bytes, off, len);
    }

    private int bytes2short(byte[] bytes, int off) {
        int low = (bytes[off] < 0 ? bytes[off] + 256 : bytes[off]);
        int high = (bytes[off + 1] < 0 ? bytes[off + 1] + 256 : bytes[off + 1]) << 8;
        return 0 | low | high;
    }

    public boolean isLink(File file) throws IOException {
        File canonicalDir = file.getParentFile().getCanonicalFile();
        File fileInCanonicalDir = new File(canonicalDir, file.getName());
        return fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    private class Filter implements FilenameFilter {

        private final String filter;

        public Filter() {
            this(".txt");
        }

        public Filter(final String filter) {
            this.filter = filter;
        }

        public boolean accept(final File dir, final String file) {
            return file.toLowerCase().endsWith(filter);
        }
    }
}
