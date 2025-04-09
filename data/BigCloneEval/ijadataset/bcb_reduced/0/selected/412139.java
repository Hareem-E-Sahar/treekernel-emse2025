package com.siteeval.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.util.zip.*;
import java.util.Calendar;
import java.sql.SQLException;

public class SysBackup {

    private static Log log = LogFactory.getLog(SysBackup.class);

    public static synchronized boolean backupOfdb(java.sql.Connection conn, String path) {
        boolean flag = false;
        try {
            java.sql.Statement stmt = conn.createStatement();
            if (false) {
                java.sql.ResultSet rs = stmt.executeQuery("exec sp_helpdevice");
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }
            String sql = "EXEC sp_addumpdevice 'disk', 'temp', '" + path + "' ;" + "BACKUP DATABASE " + conn.getCatalog() + " TO temp with init;" + "EXEC sp_dropdevice 'temp'; ";
            log.debug(sql.replaceAll(";", "\n"));
            stmt.execute(sql);
            flag = true;
        } catch (SQLException e) {
            System.out.println("���ݷ����쳣��");
            e.printStackTrace();
        } finally {
            return flag;
        }
    }

    public static String nextFile(String s) {
        Calendar cal = Calendar.getInstance();
        String week[] = { "������", "����һ", "���ڶ�", "������", "������", "������", "������" };
        if (s.equalsIgnoreCase("week")) return week[cal.get(Calendar.DAY_OF_WEEK) - 1]; else if (s.equalsIgnoreCase("month")) return cal.get(Calendar.DATE) + "��"; else return "";
    }

    static final int BUFFER = 2048;

    public static synchronized boolean wzip(String zipFileName, String inputFile) {
        try {
            if (zipFileName == null || !zipFileName.endsWith(".zip")) throw new Exception("ϵͳ�������ô���!");
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            File f = new File(inputFile);
            wzip(out, f, f.getName());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void wzip(ZipOutputStream out, File f, String base) {
        byte data[] = new byte[BUFFER];
        BufferedInputStream origin = null;
        FileInputStream fin = null;
        String temp = null;
        int cont = 0;
        try {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                log.debug("Adding: " + fs[i]);
                temp = fs[i].getAbsolutePath();
                cont = temp.indexOf(base);
                if (fs[i].isDirectory()) {
                    temp = temp.substring(cont).replace('\\', '/') + "/";
                    out.putNextEntry(new ZipEntry(temp));
                    wzip(out, fs[i], base);
                } else {
                    fin = new FileInputStream(fs[i]);
                    origin = new BufferedInputStream(fin, BUFFER);
                    out.putNextEntry(new ZipEntry(temp.substring(cont)));
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��������:��ѹ���ļ�
     * @param zipFileName   zip�ļ������Ŀ��·��
     * @param outputDirectory   ��ѹ���ļ����·��
     */
    public static synchronized boolean unzip(String zipFileName, String outputDirectory) {
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(zipFileName);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            int count;
            byte[] data = null;
            if (outputDirectory == null || outputDirectory.trim().equals("")) {
                if (zipFileName.lastIndexOf("\\") != -1) outputDirectory = zipFileName.substring(0, zipFileName.lastIndexOf("\\") + 1); else outputDirectory = "";
            } else outputDirectory = outputDirectory + File.separator;
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                if (entry.isDirectory()) {
                    File dir = new File(entry.getName());
                    if (!dir.exists()) dir.mkdir();
                } else {
                    data = new byte[BUFFER];
                    dest = new BufferedOutputStream(new FileOutputStream(outputDirectory + entry.getName()), BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
                zis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
