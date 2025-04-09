package com.tonicsystems.jarjar.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

class IoUtil {

    private IoUtil() {
    }

    public static void pipe(InputStream is, OutputStream out, byte[] buf) throws IOException {
        for (; ; ) {
            int amt = is.read(buf);
            if (amt < 0) break;
            out.write(buf, 0, amt);
        }
    }

    public static void copy(File from, File to, byte[] buf) throws IOException {
        InputStream in = new FileInputStream(from);
        try {
            OutputStream out = new FileOutputStream(to);
            try {
                pipe(in, out, buf);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * Create a copy of an zip file without its empty directories.
     * @param inputFile
     * @param outputFile
     * @throws IOException
     */
    public static void copyZipWithoutEmptyDirectories(final File inputFile, final File outputFile) throws IOException {
        final byte[] buf = new byte[0x2000];
        final ZipFile inputZip = new ZipFile(inputFile);
        final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(outputFile));
        try {
            final Enumeration<? extends ZipEntry> e = inputZip.entries();
            final ArrayList<ZipEntry> sortedList = new ArrayList<ZipEntry>();
            while (e.hasMoreElements()) {
                final ZipEntry entry = e.nextElement();
                sortedList.add(entry);
            }
            Collections.sort(sortedList, new Comparator<ZipEntry>() {

                public int compare(ZipEntry o1, ZipEntry o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (int i = sortedList.size() - 1; i >= 0; i--) {
                final ZipEntry inputEntry = sortedList.get(i);
                final String name = inputEntry.getName();
                final boolean isEmptyDirectory;
                if (inputEntry.isDirectory()) {
                    if (i == sortedList.size() - 1) {
                        isEmptyDirectory = true;
                    } else {
                        final String nextName = sortedList.get(i + 1).getName();
                        isEmptyDirectory = !nextName.startsWith(name);
                    }
                } else {
                    isEmptyDirectory = false;
                }
                if (isEmptyDirectory) {
                    sortedList.remove(inputEntry);
                } else {
                    final ZipEntry outputEntry = new ZipEntry(inputEntry);
                    outputStream.putNextEntry(outputEntry);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final InputStream is = inputZip.getInputStream(inputEntry);
                    IoUtil.pipe(is, baos, buf);
                    is.close();
                    outputStream.write(baos.toByteArray());
                }
            }
        } finally {
            outputStream.close();
        }
    }
}
