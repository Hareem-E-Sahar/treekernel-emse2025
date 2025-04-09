package com.simpledata.filetools;

import java.awt.BorderLayout;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.*;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
* create  package (ZIP File)<BR>
* Those file could be decompressed using a normal zip utilities. 
* The Header is stored 
* on a file denoted by DATA_STAMP
* <PRE>
* EXAMPLE:
* FileTree:
*    [Dir1] 
*         [Dir2]
*               -File1
*               -File2
*         [Dir3]
*               -File3
*         -File4
*    -File5
*
*ie: [Dir1] contains [Dir2],[Dir3] and -File4
*    [Dir2] contains -File1 and -File2
*    [Dir3] contains -File3
*    [Dir1] and -File5 are in the user directory (user.dir)
*
*
*  We would like to create a package name "TEST Pack" containing: 
*     -File5 and everything into [Dir1] excepted [Dir3]
*  and we want to add the creation date of this package
*  evrything will be save in test.simplepack
* ------------ Code -------------
* // create a FilePackage with user.dir as root directory (new File(""))
* FilePackage fp = new FilePackage(new File(""));
* fp.setTitle("TEST Pack");
* fp.put("Creation Date",new Date());
* fp.insertRecursive(new File(Dir1));
* fp.insertRecursive(new File(File5));
* fp.removeRecursive(new File(Dir3));
* FilePackager.packageZ(fp,new File("test."+FilePackager.EXTENSION));
* ----------- Uncompress or get info on a package -------
* -- get Infos -- 
* FilePackage fp = getFilePackage(new File("test."+FilePackager.EXTENSION));
* System.out.println(""+fp);
* -- unpack in directory [TestDir] --
* FilePackage fp = unpackageZ(
* 	new File("test."+FilePackager.EXTENSION),new File("TestDir"));
* 
* </PRE>
*/
public class FilePackager {

    private static final Logger m_log = Logger.getLogger(FilePackager.class);

    private static final int DATA_BLOCK_SIZE = 2048;

    private static final String DATA_STAMP = "SIMPLE PACKAGE OBJECT HEADER V1.SIMPLEDATA";

    /**
	* File extension that packages should provide
	*/
    public static final String EXTENSION = "simplepack";

    private static void debug(String s) {
        m_log.debug(s);
    }

    /**
	* Testing input
	*/
    public static void main(String args[]) {
        File dest = new File(args[1]);
        FilePackage fp = getFilePackage(dest);
        m_log.debug("" + fp);
    }

    /**
	* Package multiple files or directories. 
	* Content of the directories are packaged recursively
	*/
    public static boolean packageZ(FilePackage fp, File destFile) {
        try {
            FileOutputStream fos = new FileOutputStream(destFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.putNextEntry(new ZipEntry(DATA_STAMP));
            ObjectOutputStream oos = new ObjectOutputStream(zos);
            oos.writeObject(fp);
            zos.closeEntry();
            zos.flush();
            File[] sourcesFiles = fp.getFileList();
            for (int i = 0; i < sourcesFiles.length; i++) {
                File f = sourcesFiles[i];
                if (f.isDirectory()) {
                    debug(" is dir " + f);
                } else {
                    ZipEntry ze = new ZipEntry(fp.getName(f));
                    zos.putNextEntry(ze);
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    byte[] data = new byte[DATA_BLOCK_SIZE];
                    int bCnt;
                    while ((bCnt = bis.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
                        zos.write(data, 0, bCnt);
                    }
                    zos.closeEntry();
                    zos.flush();
                }
            }
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
	* Unpackage (decompress) into destination Dir
	* return a FilePackage denoting the content of this package
	*/
    public static FilePackage unpackageZ(File source, File destDir) {
        return unpackageOrList(source, destDir, true);
    }

    /**
	* Return a FilePackage describing the content of this package
	* As if it was uncompressed in user.dir
	*/
    public static FilePackage getFilePackage(File source) {
        debug("getFilePackage : " + source);
        return unpackageOrList(source, new File(""), false);
    }

    private static FilePackage unpackageOrList(File source, File destDir, boolean uncompress) {
        FilePackage fPackage = null;
        if (!source.exists()) return null;
        try {
            FileInputStream fis = new FileInputStream(source);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();
                debug("Going for " + name);
                if (name.equals(DATA_STAMP)) {
                    ObjectInputStream ois = new ObjectInputStream(zis);
                    try {
                        fPackage = (FilePackage) ois.readObject();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                        System.err.println("INVALID DATA PACKAGE");
                        return null;
                    }
                    fPackage.clean();
                } else {
                    File dest = new File(destDir, name);
                    fPackage.insert(dest);
                    if (uncompress) {
                        File temp = dest.getParentFile();
                        temp.mkdirs();
                        m_log.debug("" + dest);
                        FileOutputStream fos = new FileOutputStream(dest);
                        BufferedOutputStream bos = new BufferedOutputStream(fos, DATA_BLOCK_SIZE);
                        int byteCount = 0;
                        byte[] data = new byte[DATA_BLOCK_SIZE];
                        while ((byteCount = zis.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
                            bos.write(data, 0, byteCount);
                        }
                        bos.flush();
                        bos.close();
                    }
                }
            }
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fPackage;
    }

    /**
	* return a SimpleFileView Filter to be used in @see SimpleFileBrowser
	*/
    public static SimpleFileView getSimpleFileView() {
        return new FilePackager.FilePackageSimpleFileView();
    }

    /**
	* Class that implements SimpleFileView 
	*/
    private static class FilePackageSimpleFileView implements SimpleFileView {

        public ImageIcon getIcon(File f) {
            return SimpleFileBrowser.DEFAULT_ICON;
        }

        public String[] getExtensions() {
            String[] exts = new String[1];
            exts[0] = FilePackager.EXTENSION;
            return exts;
        }

        public String getDescription() {
            return "SIMPLE DATA PACKAGE";
        }

        public String getTypeDescription(File f) {
            return "FILE THAT CONTAINS DATA FOR SIMPLE DATA SOFTWARE";
        }

        public JPanel getPanel(File f) {
            JPanel jp = new JPanel();
            jp.setLayout(new BorderLayout());
            FilePackage fp = FilePackager.getFilePackage(f);
            if (fp == null) {
                jp.add(new JLabel("INVALID PACKAGE"), BorderLayout.NORTH);
                return jp;
            }
            Vector res = new Vector();
            res.add(new String[] { "Title" });
            res.add(new String[] { fp.getTitle() });
            res.add(new String[] { "" });
            Enumeration en = fp.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                res.add(new String[] { key });
                res.add(new String[] { fp.get(key).toString() });
                res.add(new String[] { "" });
            }
            String[][] table = new String[res.size()][2];
            for (int i = 0; i < res.size(); i++) {
                table[i] = (String[]) res.get(i);
            }
            JTable jt = new JTable();
            jt.setModel(new javax.swing.table.DefaultTableModel(table, new String[] { "Title 1" }));
            jp.add(jt, BorderLayout.CENTER);
            return jp;
        }
    }
}
