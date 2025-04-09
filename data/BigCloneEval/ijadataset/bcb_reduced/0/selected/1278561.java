package de.pallo.joti.sys;

import java.io.File;
import java.util.zip.CRC32;

public class DirectoryHashCreator {

    private int dirCounter = 0;

    private int fileCounter = 0;

    public String getCRC32(File inDir) {
        String temp = createHash(inDir);
        CRC32 crc32 = new CRC32();
        crc32.update(temp.getBytes());
        return getHexValue(crc32.getValue());
    }

    private String getHexValue(long v) {
        String s = Long.toHexString(v);
        return s;
    }

    public String createHash(File inDir) {
        dirCounter = 0;
        fileCounter = 0;
        StringBuffer buffer = new StringBuffer();
        appendFileInfo(inDir, buffer);
        buffer.append("\n");
        buffer.append("\n");
        buffer.append("directories = ").append(dirCounter).append("\n");
        buffer.append("files = ").append(fileCounter).append("\n");
        return buffer.toString();
    }

    private void appendFileInfo(File inDir, StringBuffer buffer) {
        File[] files = inDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File current = files[i];
            if (current.isDirectory()) {
                dirCounter++;
                appendFileInfo(current, buffer);
            } else {
                fileCounter++;
                buffer.append(current.length());
                buffer.append(":");
                buffer.append(current.getPath());
                buffer.append("\n");
            }
        }
    }
}
