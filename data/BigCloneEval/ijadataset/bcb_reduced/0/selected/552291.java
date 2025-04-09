package com.qbrowser.persist;

import java.awt.TextArea;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.*;
import java.util.zip.CRC32;

/**
 *
 * @author Administrator
 */
public class Zipper {

    private ArrayList alltargetdirs = new ArrayList();

    private HashSet alljyogai = new HashSet();

    /**
	 * 何もしないOutputStream。
	 * ファイルの圧縮後のサイズを調べるために使う。
	 */
    private class IdleOutputStream extends OutputStream {

        public void write(int b) throws IOException {
        }
    }

    /** Creates a new instance of Main */
    public Zipper() {
    }

    void getAllTargetFile(File rootdir) {
        File[] files = rootdir.listFiles();
        for (int i = 0; i < files.length; i++) {
            alltargetdirs.add(files[i]);
            if (files[i].isDirectory()) {
                processDirectory(files[i]);
            }
        }
    }

    void processDirectory(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            alltargetdirs.add(files[i]);
            if (files[i].isDirectory()) {
                processDirectory(files[i]);
            }
        }
    }

    String extractRelativeZipPath(String original, String exclude) {
        int i = original.indexOf(exclude);
        if (i != -1) {
            return original.substring(i + exclude.length() + 1);
        } else {
            return original;
        }
    }

    String convertZipEntrySeparator(String input) {
        StringBuffer result = new StringBuffer();
        char[] inputc = input.toCharArray();
        for (int i = 0; i < inputc.length; i++) {
            if (inputc[i] == '\\') {
                result.append('/');
            } else {
                result.append(inputc[i]);
            }
        }
        return result.toString();
    }

    /** 圧縮レベル (0～9) */
    private static final int COMPRESS_LEVEL = 0;

    /**
	 * ファイルをzipファイルに圧縮する。
	 * @param files 圧縮されるファイル
	 * @param zipFile zipファイル
	 * @throws IOException ファイル入出力エラー
	 */
    public String zipForDir(File jarnameDir, File zipFile, boolean printProgress, String extension) throws IOException {
        alltargetdirs.clear();
        getAllTargetFile(jarnameDir);
        String zipnonamaehakorenisubeshi = jarnameDir.getName() + extension;
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(zipFile));
        int allsize = alltargetdirs.size();
        for (int i = 0; i < allsize; i++) {
            File tfile = (File) alltargetdirs.get(i);
            String directparent = tfile.getParentFile().getName();
            if (tfile.isDirectory()) {
            } else {
                writeEntry(tfile, output, jarnameDir);
            }
            if (printProgress) {
                System.out.println("Zip中・・・(" + i + "/" + allsize + ")");
            }
        }
        output.finish();
        output.close();
        alltargetdirs.clear();
        return zipnonamaehakorenisubeshi;
    }

    /**
	 * ファイルとその階層を指定し、ZipOutputStreamにファイルを追加する。
	 * @param file ファイル
	 * @param output ZipOutputStream
	 * @param depth ファイルの階層
	 * @throws IOException ファイル入出力エラー
	 */
    private void writeEntry(File file, ZipOutputStream output, File oya) throws IOException {
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        String fn = extractRelativeZipPath(file.getAbsolutePath(), oya.getAbsolutePath());
        ZipEntry entry = new ZipEntry(this.convertZipEntrySeparator(fn));
        output.putNextEntry(entry);
        int b;
        while ((b = input.read()) != -1) {
            output.write(b);
        }
        input.close();
        output.closeEntry();
    }

    /**
	 * ファイルのCRC-32チェックサムを取得する。
	 * @param file ファイル
	 * @return CRC-32チェックサム
	 * @throws IOException ファイル入出力エラー
	 */
    private long getCRCValue(File file) throws IOException {
        CRC32 crc = new CRC32();
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        int b;
        while ((b = input.read()) != -1) {
            crc.update(b);
        }
        input.close();
        return crc.getValue();
    }

    /**
	 * ファイルの圧縮後のサイズを調べる。
	 * @param file ファイル
	 * @return 圧縮後のサイズ
	 * @throws IOException 入出力エラー
	 */
    private long getCompressedSize(File file) throws IOException {
        ZipEntry entry = new ZipEntry(file.getName());
        entry.setMethod(ZipEntry.DEFLATED);
        ZipOutputStream out = new ZipOutputStream(new IdleOutputStream());
        out.setLevel(COMPRESS_LEVEL);
        out.putNextEntry(entry);
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        int b;
        while ((b = input.read()) != -1) {
            out.write(b);
        }
        input.close();
        out.closeEntry();
        out.close();
        return entry.getCompressedSize();
    }
}
