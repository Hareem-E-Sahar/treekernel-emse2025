package com.hs.framework.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *  ѹ�����ѹ�� �Ƽ�ʹ�ã� zip,unZip�������ֽ��� zipFile,unZipFile�������ļ��� <p>
 *
 *  031219:wjz <p>
 *
 *  ����˰�ͨ���ѹ�� ,<p>
 *
 *  ����addZipEntry����,����������ZIP�ļ�������ļ�
 *
 *@author     Administrator
 *@created    2001��12��29�� modified by wujinzhong 030416
 */
public class ZipUtil {

    private static final int BUFFER = 2048;

    /**
     *  ѹ����ʣ�0��9��
     */
    private static int zipLevel = 9;

    private static ZipUtil zu = null;

    /**
     *  Constructor for the ZipUtil object
     */
    public ZipUtil() {
    }

    /**
     *  Sets the value of the zipLevel property.
     *
     *@param  aZipLevel  the new value of the zipLevel property
     *@roseuid           3C16C72101A8
     */
    public static void setZipLevel(int aZipLevel) {
        zipLevel = aZipLevel;
    }

    /**
     *  ��ȡ����ĵ�CRCֵ,���ؽ��Ϊlong add by wjz 031121
     *
     *@param  b  Description of Parameter
     *@return    The CRCValue value
     */
    public static long getCRCLongValue(byte[] b) {
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(b);
        return crc.getValue();
    }

    /**
     *  ��ȡ����ĵ�CRCֵ,���ؽ��Ϊ�ַ���ʽ,���8λ,ǰ�油�� add by wjz 031121
     *
     *@param  b  Description of Parameter
     *@return    The CRCStrValue value
     */
    public static String getCRCStrValue(byte[] b) {
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(b);
        String s = Long.toHexString(crc.getValue());
        while (s.length() < 8) {
            s = "0" + s;
        }
        return s.toUpperCase();
    }

    /**
     *  Access method for the zipLevel property.
     *
     *@return     the current value of the zipLevel property
     *@roseuid    3C16C72101C6
     */
    public static int getZipLevel() {
        return zipLevel;
    }

    /**
     *  ��ȡZipUtil����
     *
     *@return     ZipUtil_Java
     *@roseuid    3C16C7210220
     */
    public static ZipUtil getZipUtil() {
        if (zu == null) {
            zu = new ZipUtil();
        }
        return zu;
    }

    /**
     *  �����е�ZIP�ļ������һ���ļ� <p>
     *
     *  add by wjz 031219
     *
     *@param  srcZipFile    ԭʼZIP�ļ�
     *@param  newEntryFile  �¼ӵ��ļ����������ļ��У�
     *@return               Description of the Returned Value
     */
    public static boolean addZipEntry(String srcZipFile, String newEntryFile) {
        String dstFile = srcZipFile + "2";
        boolean ret = addZipEntry(srcZipFile, newEntryFile, dstFile);
        if (ret) {
            try {
                ManageFile.copyFile(dstFile, srcZipFile);
                ManageFile.DeleteFile(dstFile, false);
            } catch (Exception ex) {
                LogUtil.getLogger().error(ex.getMessage(), ex);
                return false;
            }
        }
        return ret;
    }

    /**
     *  �����е�ZIP�ļ������һ���ļ� <p>
     *
     *  add by wjz 031219
     *
     *@param  dstFile       Ŀ���ļ�
     *@param  srcZipFile    ԭʼZIP�ļ�
     *@param  newEntryFile  �¼ӵ��ļ����������ļ��У�
     *@return               Description of the Returned Value
     */
    public static boolean addZipEntry(String srcZipFile, String newEntryFile, String dstFile) {
        java.util.zip.ZipOutputStream zout = null;
        InputStream is = null;
        try {
            if (new File(newEntryFile).isDirectory() && !newEntryFile.substring(newEntryFile.length() - 1).equals(File.separator)) {
                newEntryFile = newEntryFile + File.separator;
            }
            System.err.println("============");
            File fn = new File(dstFile);
            if (!fn.exists()) {
                fn.createNewFile();
            }
            zout = new java.util.zip.ZipOutputStream(new FileOutputStream(dstFile));
            ZipFile zipfile = new ZipFile(srcZipFile);
            ZipEntry entry = null;
            Enumeration e = zipfile.entries();
            byte[] buffer = new byte[1024];
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                System.err.println(entry.getName());
                zout.putNextEntry(entry);
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                while ((count = is.read(buffer, 0, 1024)) != -1) {
                    zout.write(buffer, 0, count);
                    zout.flush();
                }
                is.close();
                zout.closeEntry();
            }
            zipFile(null, newEntryFile, "*.*", zout);
            zout.close();
            return true;
        } catch (java.io.IOException ioex) {
            LogUtil.getLogger().error(ioex.getMessage(), ioex);
            ioex.printStackTrace();
            return false;
        } finally {
            try {
                if (zout != null) {
                    zout.close();
                }
            } catch (Exception ex) {
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    /**
     *  The main program for the ZipUtil class
     *
     *@param  args  The command line arguments
     *@roseuid      3C16C721022A
     */
    public static void main(String[] args) {
        System.err.println(ZipUtil.zipFile("C:\\fpdk\\pic\\110002314000325248.tif", "C:\\fpdk\\pic\\110002314000325248.zip"));
    }

    /**
     *  ��ѹ��String���,��֧�ֶ����Ʋ���
     *
     *@param  btSrc  Դ �� ��
     *@return        ��ѹ�������
     *@roseuid       3C16C72101E4
     */
    public static synchronized byte[] unZip(byte[] btSrc) {
        byte[] BtBak = null;
        try {
            java.io.ByteArrayInputStream bai = new java.io.ByteArrayInputStream(btSrc);
            java.util.zip.ZipInputStream zipinputstream = new java.util.zip.ZipInputStream(bai);
            java.util.zip.ZipEntry zipentry = null;
            zipentry = zipinputstream.getNextEntry();
            int n;
            int m;
            int i;
            m = 0;
            BufferedInputStream bis = new BufferedInputStream(zipinputstream, 1024 * 1024);
            byte[] bt = new byte[1024 * 1024];
            int iBytes = 0;
            while ((iBytes = bis.read(bt, 0, bt.length - 1)) != -1) {
                m = m + iBytes;
                zipinputstream.skip(0);
            }
            zipinputstream.closeEntry();
            zipinputstream.close();
            bis.close();
            java.util.zip.ZipEntry zipentry0 = new java.util.zip.ZipEntry("test2");
            bai = new java.io.ByteArrayInputStream(btSrc);
            java.util.zip.ZipInputStream zipinputstream0 = new java.util.zip.ZipInputStream(bai);
            zipentry0 = zipinputstream0.getNextEntry();
            BtBak = new byte[m];
            bis = new BufferedInputStream(zipinputstream0, 1024 * 1024);
            iBytes = 0;
            int totalBytes = 0;
            while (totalBytes < BtBak.length && iBytes != -1) {
                iBytes = bis.read(BtBak, totalBytes, BtBak.length - totalBytes);
                if (iBytes != -1) {
                    totalBytes += iBytes;
                    zipinputstream0.skip(0);
                }
            }
            zipinputstream0.closeEntry();
            zipinputstream0.close();
            bis.close();
            bt = null;
            bis = null;
        } catch (IOException ce) {
            LogUtil.getLogger().error(ce.getMessage(), ce);
            BtBak = null;
        }
        return BtBak;
    }

    /**
     *  ���ļ���ѹ��ĳ��Ŀ¼��
     *
     *@param  destDir      Ŀ��·��
     *@param  zipFileName  ѹ���ļ���
     *@return              true-�ɹ�,false-ʧ��
     */
    public static boolean unZipFile(String zipFileName, String destDir) {
        if (zipFileName == null) {
            throw new IllegalArgumentException("[zipname] is empty");
        }
        File destFile = new File(destDir);
        if (!destFile.isDirectory() || !destFile.exists()) {
            ManageFile.mkDir(destDir);
        }
        ZipFile zipfile = null;
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        try {
            zipfile = new ZipFile(zipFileName);
            ZipEntry entry = null;
            int i = 0;
            byte data[] = new byte[BUFFER];
            int count;
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    ManageFile.mkDir(destDir, entry.getName());
                } else {
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    String strFileName = destDir + File.separator + entry.getName();
                    strFileName = ManageFile.refineFilePath(strFileName);
                    ManageFile.mkDir(ManageFile.getParentPath(strFileName));
                    dest = new BufferedOutputStream(new FileOutputStream(strFileName), BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (IOException e) {
            LogUtil.getLogger().error(e.getMessage(), e);
            throw new IllegalArgumentException("�ļ���ѹ��ʧ��!");
        } finally {
            try {
                dest.close();
            } catch (Exception ex) {
            }
            try {
                is.close();
            } catch (Exception ex) {
            }
            try {
                zipfile.close();
            } catch (Exception ex) {
            }
        }
        return true;
    }

    /**
     *  ��ѹ�����ļ�,��������ķ�ʽ
     *
     *@param  btSrc  ѹ�����
     *@return        �ļ���=(�ļ���,�ļ�����)
     *@deprecated    ��������ķ�ʽ�ڽ�ѹ��Winzip��ʽ���ļ�ʱ���׳��,�뾡��Ҫʹ��,���zip�ļ�Ҳ���ø���ѹ���.
     */
    public static synchronized Vector unZipMultiEntry(byte[] btSrc) {
        byte[] BtBak = null;
        String[][] strReturn;
        String strTemp = null;
        Vector vc_all = new Vector();
        Vector vc_temp;
        try {
            java.util.zip.ZipEntry ze;
            java.io.ByteArrayInputStream bai = new java.io.ByteArrayInputStream(btSrc);
            java.util.zip.ZipInputStream zipis = new java.util.zip.ZipInputStream(bai);
            DataInputStream dis = new DataInputStream(zipis);
            StringBuffer buf = new StringBuffer();
            String fileTemp = null;
            while ((ze = zipis.getNextEntry()) != null) {
                vc_temp = new Vector();
                vc_temp.addElement(ze.getName());
                buf.setLength(0);
                for (; ; ) {
                    int c = dis.read();
                    if (c <= 0) {
                        break;
                    }
                    buf.append((char) c);
                }
                fileTemp = buf.toString();
                fileTemp = new String(fileTemp.getBytes("8859_1"), "GB2312");
                vc_temp.addElement(fileTemp);
                vc_all.addElement(vc_temp);
            }
            dis.close();
            return vc_all;
        } catch (Exception ce) {
            LogUtil.getLogger().error(ce.getMessage(), ce);
            return null;
        }
    }

    /**
     *  ��ѹ�����ļ�,����ʱ�ļ��ķ�ʽ(��Ծɰ汾��ѹ���ļ�)
     *
     *@param  btSrc  ѹ�����
     *@return        �ļ���=(�ļ���,�ļ�����)
     */
    public static synchronized Vector unZipMultiEntryViaTmpFile(byte[] btSrc) {
        byte[] BtBak = null;
        String[][] strReturn;
        String strTemp = null;
        Vector vc_all = new Vector();
        Vector vc_temp;
        try {
            java.util.zip.ZipEntry ze;
            String tmpfilename = "unzip.zip";
            ManageFile.saveFile(tmpfilename, btSrc);
            DataInputStream dis = null;
            StringBuffer buf = new StringBuffer();
            String fileTemp = null;
            ZipFile zipfile = new ZipFile(tmpfilename);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                ze = (ZipEntry) e.nextElement();
                dis = new DataInputStream(zipfile.getInputStream(ze));
                vc_temp = new Vector();
                vc_temp.addElement(ze.getName());
                buf.setLength(0);
                for (; ; ) {
                    int c = dis.read();
                    if (c <= 0) {
                        break;
                    }
                    buf.append((char) c);
                }
                fileTemp = buf.toString();
                fileTemp = new String(fileTemp.getBytes("8859_1"), "GB2312");
                vc_temp.addElement(fileTemp);
                vc_all.addElement(vc_temp);
            }
            dis.close();
            return vc_all;
        } catch (Exception ce) {
            LogUtil.getLogger().error(ce.getMessage(), ce);
            return null;
        }
    }

    /**
     *  ѹ��һ���ļ�
     *
     *@param  fileVec   �ļ���=(�ļ���,�ļ�����)
     *@param  fileName  ͬʱ��ѹ��������д�����ļ���
     *@return           ѹ���Ķ���������
     */
    public byte[] zip(Vector fileVec, String fileName) {
        byte[] BtBak = null;
        byte[] btSrc = null;
        java.io.ByteArrayOutputStream bao = null;
        java.util.zip.ZipEntry zipentry;
        ManageFile mfj = new ManageFile();
        int n;
        int i;
        Vector temp = fileVec;
        String tempFileName = null;
        String tempFileContent = null;
        java.util.zip.ZipOutputStream zipoutputstream = null;
        try {
            bao = new java.io.ByteArrayOutputStream();
            zipoutputstream = new java.util.zip.ZipOutputStream(bao);
            zipoutputstream.setMethod(java.util.zip.ZipOutputStream.DEFLATED);
            int len = temp.size();
            for (int j = 0; j < len; j++) {
                tempFileName = (String) ((Vector) temp.elementAt(j)).elementAt(0);
                tempFileContent = (String) ((Vector) temp.elementAt(j)).elementAt(1);
                zipentry = new java.util.zip.ZipEntry(tempFileName);
                zipentry.setSize(tempFileContent.getBytes().length);
                zipoutputstream.putNextEntry(zipentry);
                zipoutputstream.setLevel(getZipLevel());
                zipoutputstream.write(tempFileContent.getBytes());
                zipoutputstream.closeEntry();
            }
            zipoutputstream.close();
            mfj.DeleteFile(fileName, false);
            mfj.saveFile(fileName, bao.toByteArray());
            return bao.toByteArray();
        } catch (IOException ie) {
            LogUtil.getLogger().error(ie.getMessage(), ie);
            zipoutputstream = null;
            return null;
        }
    }

    /**
     *  ѹ�����
     *
     *@param  btSrc  Դ���
     *@return        ѹ�������
     *@roseuid       3C16C72101D0
     */
    public static synchronized byte[] zip(byte[] btSrc) {
        byte[] BtBak = null;
        java.io.ByteArrayOutputStream bao = null;
        int n;
        int i;
        try {
            bao = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zipoutputstream = new java.util.zip.ZipOutputStream(bao);
            zipoutputstream.setMethod(java.util.zip.ZipOutputStream.DEFLATED);
            java.util.zip.ZipEntry zipentry = new java.util.zip.ZipEntry("srcFile.bin");
            zipentry.setSize(btSrc.length);
            zipoutputstream.putNextEntry(zipentry);
            zipoutputstream.setLevel(getZipLevel());
            zipoutputstream.write(btSrc);
            zipoutputstream.closeEntry();
            zipoutputstream.close();
        } catch (IOException ie) {
            LogUtil.getLogger().error(ie.getMessage(), ie);
            BtBak = null;
        }
        return bao.toByteArray();
    }

    /**
     *  ѹ���ļ���֧�����Ŀ¼ѹ����ļ�ѹ��
     *
     *@param  srcFile        Դ�ļ����������ļ���·��·��,��c:\zxt\feedback
     *@param  dstFile        Ŀ���ļ���
     *@param  srcFileFilter  ͨ�����*.txt,*.zip��
     *@return                true-�ɹ���false-ʧ��
     */
    public static boolean zipFile(String srcFile, String srcFileFilter, String dstFile) {
        try {
            if (new File(srcFile).isDirectory() && !srcFile.substring(srcFile.length() - 1).equals(File.separator)) {
                srcFile = srcFile + File.separator;
            }
            File fn = new File(dstFile);
            if (!fn.exists()) {
                fn.createNewFile();
            }
            java.util.zip.ZipOutputStream zout = new java.util.zip.ZipOutputStream(new FileOutputStream(dstFile));
            zipFile(null, srcFile, srcFileFilter, zout);
            zout.close();
            return true;
        } catch (java.io.IOException ioex) {
            LogUtil.getLogger().error(ioex.getMessage(), ioex);
            return false;
        }
    }

    /**
     *  ѹ���ļ���֧�����Ŀ¼ѹ����ļ�ѹ��
     *
     *@param  srcFile  Դ�ļ����������ļ���·��·��,��c:\zxt\feedback
     *@param  dstFile  Ŀ���ļ���
     *@return          true-�ɹ���false-ʧ��
     */
    public static boolean zipFile(String srcFile, String dstFile) {
        return zipFile(srcFile, "*", dstFile);
    }

    /**
     *  ��ĳ��·���µ������ļ�ѹ�� modify by wjz 031211,add filter <p>
     *
     *
     *
     *@param  from     Դ·��,��c:\zxt\feedback\
     *@param  zout     ѹ�������
     *@param  filters  Description of Parameter
     *@param  baseDir  ѹ��Ļ�׼Ŀ¼
     */
    private static void zipFile(String baseDir, String from, String filters, ZipOutputStream zout) {
        String temp_to = null;
        String temp_from = null;
        String entryFileName = null;
        String entryName = "";
        if (baseDir == null) {
            int idx = from.lastIndexOf(File.separator);
            if (idx == -1) {
                throw new IllegalArgumentException("Argument Error:[from] is not a valid filename");
            }
            baseDir = from.substring(0, idx);
            System.err.println("from=" + from);
            System.err.println("baseDir=" + baseDir);
        }
        try {
            File f = new File(from);
            int filenumber;
            String[] strFile;
            File fTemp;
            if (f.isDirectory()) {
                strFile = f.list();
                for (int i = 0; i < strFile.length; i++) {
                    if (from.endsWith(File.separator)) {
                        temp_from = from + strFile[i];
                    } else {
                        temp_from = from + File.separator + strFile[i];
                    }
                    fTemp = new File(temp_from);
                    if (fTemp.isDirectory()) {
                        zipFile(baseDir, temp_from, filters, zout);
                    } else {
                        if (!ManageFile.checkFileNames(temp_from, filters)) {
                            continue;
                        }
                        byte[] s = ManageFile.loadFromFile(temp_from);
                        entryName = temp_from.substring(baseDir.length() + 1, temp_from.length());
                        System.err.println("entrName=" + entryName);
                        ZipEntry zefile = new ZipEntry(entryName);
                        zefile.setSize(s.length);
                        zout.putNextEntry(zefile);
                        zout.write(s);
                        zout.closeEntry();
                    }
                }
            } else {
                if (!ManageFile.checkFileNames(from, filters)) {
                    return;
                }
                byte[] s = ManageFile.loadFromFile(from);
                entryName = from.substring(baseDir.length() + 1, from.length());
                System.err.println("entrName=" + entryName);
                ZipEntry zefile = new ZipEntry(entryName);
                zefile.setSize(s.length);
                zout.putNextEntry(zefile);
                zout.write(s);
                zout.closeEntry();
            }
        } catch (Exception e) {
            LogUtil.getLogger().error(e.getMessage(), e);
            throw new IllegalArgumentException("�ļ�ѹ��ʧ��!");
        }
    }
}
