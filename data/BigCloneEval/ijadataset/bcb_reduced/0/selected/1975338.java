package com.ibm.wala.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * Simple utilities for accessing files.
 */
public class FileUtil {

    /**
   * List all the files in a directory that match a regular expression
   * 
   * @param recurse
   *          recurse to subdirectories?
   * @throws IllegalArgumentException
   *           if dir is null
   */
    public static Collection<File> listFiles(String dir, String regex, boolean recurse) {
        if (dir == null) {
            throw new IllegalArgumentException("dir is null");
        }
        File d = new File(dir);
        Pattern p = null;
        if (regex != null) {
            p = Pattern.compile(regex);
        }
        return listFiles(d, recurse, p);
    }

    private static Collection<File> listFiles(File directory, boolean recurse, Pattern p) {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        HashSet<File> result = HashSetFactory.make();
        for (int i = 0; i < files.length; i++) {
            if (p == null || p.matcher(files[i].getAbsolutePath()).matches()) {
                result.add(files[i]);
            }
            if (recurse && files[i].isDirectory()) {
                result.addAll(listFiles(files[i], recurse, p));
            }
        }
        return result;
    }

    /**
   * This may be a resource leak:
   * http://bugs.sun.com/view_bug.do?bug_id=4724038
   * 
   * We may have to reconsider using nio for this, or apply one of the horrible
   * workarounds listed in the bug report above.
   */
    public static void copy(String srcFileName, String destFileName) throws IOException {
        if (srcFileName == null) {
            throw new IllegalArgumentException("srcFileName is null");
        }
        if (destFileName == null) {
            throw new IllegalArgumentException("destFileName is null");
        }
        FileChannel src = null;
        FileChannel dest = null;
        try {
            src = new FileInputStream(srcFileName).getChannel();
            dest = new FileOutputStream(destFileName).getChannel();
            long n = src.size();
            MappedByteBuffer buf = src.map(FileChannel.MapMode.READ_ONLY, 0, n);
            dest.write(buf);
        } finally {
            if (dest != null) {
                try {
                    dest.close();
                } catch (IOException e1) {
                }
            }
            if (src != null) {
                try {
                    src.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    /**
   * delete all files (recursively) in a directory. This is dangerous. Use with
   * care.
   * 
   * @throws IOException
   *           if there's a problem deleting some file
   */
    public static void deleteContents(String directory) throws IOException {
        File f = new File(directory);
        if (!f.exists()) {
            return;
        }
        if (!f.isDirectory()) {
            throw new IOException(directory + " is not a vaid directory");
        }
        for (String s : f.list()) {
            deleteRecursively(new File(f, s));
        }
    }

    private static void deleteRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (String s : f.list()) {
                deleteRecursively(new File(f, s));
            }
        }
        boolean b = f.delete();
        if (!b) {
            throw new IOException("failed to delete " + f);
        }
    }

    /**
   * Create a {@link FileOutputStream} corresponding to a particular file name.
   * Delete the existing file if one exists.
   */
    public static final FileOutputStream createFile(String fileName) throws IOException {
        if (fileName == null) {
            throw new IllegalArgumentException("null file");
        }
        File f = new File(fileName);
        if (f.getParentFile() != null && !f.getParentFile().exists()) {
            boolean result = f.getParentFile().mkdirs();
            if (!result) {
                throw new IOException("failed to create " + f.getParentFile());
            }
        }
        if (f.exists()) {
            f.delete();
        }
        boolean result = f.createNewFile();
        if (!result) {
            throw new IOException("failed to create " + f);
        }
        return new FileOutputStream(f);
    }

    /**
   * read fully the contents of s and return a byte array holding the result
   * 
   * @throws IOException
   */
    public static byte[] readBytes(InputStream s) throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("null s");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n = s.read(b);
        while (n != -1) {
            out.write(b, 0, n);
            n = s.read(b);
        }
        byte[] bb = out.toByteArray();
        out.close();
        return bb;
    }

    /**
   * write string s into file f
   * 
   * @param f
   * @param content
   * @throws IOException
   */
    public static void writeFile(File f, String content) throws IOException {
        new FileWriter(f).append(content).close();
    }
}
