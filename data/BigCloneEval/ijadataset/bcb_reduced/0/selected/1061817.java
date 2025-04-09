package common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipIO {

    public static final int EOF = -1;

    public void createZip(String _filesToZip[], String _targetZip) {
        byte[] buffer = new byte[18024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_targetZip));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (int i = 0; i < _filesToZip.length; i++) {
                FileInputStream in = new FileInputStream(_filesToZip[i]);
                out.putNextEntry(new ZipEntry(_filesToZip[i]));
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void getEntry(String _dir, ZipFile _zipFile, ZipEntry _target) throws ZipException, IOException {
        String parentName = _dir;
        File objFile = new File(_dir + _target.getName());
        if (_target.isDirectory()) {
            objFile.getAbsoluteFile().mkdirs();
        } else {
            BufferedInputStream bis = new BufferedInputStream(_zipFile.getInputStream(_target));
            if ((parentName = objFile.getParent()) != null) {
                File dir = new File(parentName);
                dir.getAbsoluteFile().mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(objFile));
            int c;
            while ((c = bis.read()) != EOF) {
                bos.write((byte) c);
            }
            bos.close();
        }
    }

    public void unpackZip(String _targetdir, String _zipfile) throws FileNotFoundException, ZipException, IOException {
        Enumeration enumer;
        ZipFile zipFile = new ZipFile(_zipfile);
        enumer = zipFile.entries();
        while (enumer.hasMoreElements()) {
            ZipEntry target = (ZipEntry) enumer.nextElement();
            System.out.print(target.getName() + " .");
            getEntry(_targetdir, zipFile, target);
            System.out.println(". unpacked");
        }
    }

    public File extractMapFile(String _zipfile) {
        try {
            File temp = File.createTempFile("map", ".xml", new File("."));
            temp.deleteOnExit();
            ZipFile zipFile = new ZipFile(_zipfile);
            for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry zipEntry = ((ZipEntry) entries.nextElement());
                if (zipEntry.getName().endsWith("map.xml")) {
                    InputStream in = zipFile.getInputStream(zipEntry);
                    OutputStream out = new FileOutputStream(temp);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                    return temp;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
