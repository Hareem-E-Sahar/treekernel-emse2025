package net.sf.jcablib.test;

import java.awt.*;
import java.io.*;
import java.util.*;

public class DiscoverCab {

    public static void main(String args[]) {
        Frame f = new Frame();
        FileDialog fd = new FileDialog(f, "Find an Installer file", FileDialog.LOAD);
        fd.show();
        if (fd.getDirectory() != null) {
            File exe = new File(fd.getDirectory(), fd.getFile());
            long offset;
            try {
                offset = findOffset(exe);
                fd = new FileDialog(f, "Name the output Cabinet", FileDialog.SAVE);
                fd.show();
                if (fd.getDirectory() != null) {
                    File cab = new File(fd.getDirectory(), fd.getFile());
                    copyFromOffset(offset, exe, cab);
                }
            } catch (IOException e) {
            }
        }
        System.exit(0);
    }

    public static long findOffset(File exe) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(exe, "r");
        long max = (long) Math.max(140000, raf.length() - 3);
        raf.close();
        raf = null;
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(exe), 8192));
        long index = 0;
        long offset = 0;
        while (index < max) {
            if (in.read() == (byte) 'M') {
                in.mark(3);
                if (in.read() == (byte) 'S' && in.read() == (byte) 'C' && in.read() == (byte) 'F') {
                    offset = index;
                    index += 3;
                } else {
                    in.reset();
                }
            }
            index++;
        }
        in.close();
        in = null;
        return offset;
    }

    public static void copyFromOffset(long offset, File exe, File cab) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(exe));
        FileOutputStream out = new FileOutputStream(cab);
        byte[] buffer = new byte[4096];
        int bytes_read;
        in.skipBytes((int) offset);
        while ((bytes_read = in.read(buffer)) != -1) out.write(buffer, 0, bytes_read);
        in.close();
        out.close();
        in = null;
        out = null;
    }
}
