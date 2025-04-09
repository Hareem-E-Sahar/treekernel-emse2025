package com.ufnasoft.dms.server.database;

import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;
import java.util.zip.*;
import java.io.*;

public class ZipUnzipUtility extends Database {

    int BUFFER = 4096;

    public String zip(String filename) {
        String rvalue = "no";
        try {
            con = getConnection();
            String folder = dms_home + FS + "www" + FS + "datafiles";
            String zipfilename = dms_home + FS + "DataBackup" + FS + filename + ".zip";
            String outFilename = zipfilename;
            File fd = new File(folder);
            File filenames[] = fd.listFiles();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            byte[] buf = new byte[BUFFER];
            String tempfilename = "";
            for (int i = 0; i < filenames.length; i++) {
                FileInputStream in = new FileInputStream(filenames[i]);
                tempfilename = filenames[i].getName();
                out.putNextEntry(new ZipEntry(tempfilename));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
            rvalue = "yes";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rvalue;
    }

    public String unzip(String filename) {
        String rvalue = "no";
        try {
            con = getConnection();
            String zipfilename = dms_home + FS + "DataBackup" + FS + filename;
            String inFilename = zipfilename;
            String folder = dms_home + FS + "www" + FS + "datafiles";
            ZipInputStream in = new ZipInputStream(new FileInputStream(zipfilename));
            ZipFile zipfile = new ZipFile(zipfilename);
            Enumeration enumeration = zipfile.entries();
            String tempfilename = "";
            byte[] buff = new byte[BUFFER];
            int n;
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enumeration.nextElement();
                tempfilename = folder + FS + entry.getName();
                StringBuffer fixed = new StringBuffer(entry.getName());
                OutputStream out = new FileOutputStream(tempfilename);
                InputStream in1 = zipfile.getInputStream(entry);
                while ((n = in1.read(buff, 0, buff.length)) != -1) {
                    out.write(buff, 0, n);
                }
                in1.close();
                out.close();
            }
            zipfile.close();
            in.close();
            rvalue = "yes";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rvalue;
    }
}
