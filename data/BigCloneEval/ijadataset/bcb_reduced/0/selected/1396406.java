package com.liferay.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <a href="ZipWriter.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class ZipWriter implements Serializable {

    public ZipWriter() {
        _baos = new ByteArrayOutputStream();
        _zos = new ZipOutputStream(new BufferedOutputStream(_baos));
    }

    public void addEntry(String name, StringBuffer sb) throws IOException {
        addEntry(name, sb.toString());
    }

    public void addEntry(String name, String s) throws IOException {
        addEntry(name, s.getBytes());
    }

    public void addEntry(String name, byte[] byteArray) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        _zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(byteArray), _BUFFER);
        int count;
        while ((count = bis.read(_data, 0, _BUFFER)) != -1) {
            _zos.write(_data, 0, count);
        }
        bis.close();
    }

    public byte[] finish() throws IOException {
        _zos.close();
        return _baos.toByteArray();
    }

    private static final int _BUFFER = 2048;

    private ByteArrayOutputStream _baos;

    private ZipOutputStream _zos;

    private byte[] _data = new byte[_BUFFER];
}
