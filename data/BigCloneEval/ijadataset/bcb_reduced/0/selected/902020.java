package com.atech.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *  This file is part of ATech Tools library.
 *  
 *  <one line to give the library's name and a brief idea of what it does.>
 *  Copyright (C) 2007  Andy (Aleksander) Rozman (Atech-Software)
 *  
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 *  
 *  
 *  For additional information about this project please visit our project site on 
 *  http://atech-tools.sourceforge.net/ or contact us via this emails: 
 *  andyrozman@users.sourceforge.net or andy@atech-software.com
 *  
 *  @author Andy
 *
*/
public class PackFiles {

    /**
     * The fix directory.
     */
    public String fixDirectory = "";

    private Hashtable<String, FilesList> filesGroups = new Hashtable<String, FilesList>();

    /**
     * Instantiates a new pack files.
     * 
     * @param fixDir the fix dir
     */
    public PackFiles(String fixDir) {
        try {
            this.fixDirectory = fixDir;
            File f = new File(this.fixDirectory);
            System.setProperty("user.dir", f.getCanonicalPath());
            System.out.println(f.getCanonicalPath());
            zipFromDirectories(new File(this.fixDirectory));
            zipFiles();
        } catch (Exception ex) {
            System.out.println("ex: " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * Zip files in directory.
     * 
     * @param directory the directory
     * @param outname the outname
     */
    public static void zipFilesInDirectory(File directory, String outname) {
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outname));
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                FileInputStream in = new FileInputStream(files[i]);
                out.putNextEntry(new ZipEntry(files[i].getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }

    /**
     * Zip from directories.
     * 
     * @param dir the dir
     */
    public void zipFromDirectories(File dir) {
        File files[] = dir.listFiles();
        System.out.println("Processing directory: " + dir);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
            } else {
                try {
                    String name = files[i].getName().substring(0, files[i].getName().lastIndexOf("_"));
                    if (this.filesGroups.containsKey(name)) {
                        this.filesGroups.get(name).addFile(files[i]);
                    } else {
                        FilesList fl = new FilesList(name, this.fixDirectory + "/");
                        fl.addFile(files[i]);
                        this.filesGroups.put(name, fl);
                    }
                } catch (Exception ex) {
                    System.out.println("Exception on parse (" + files[i] + "): " + ex);
                }
            }
        }
    }

    /**
     * Display results.
     */
    public void displayResults() {
        for (Enumeration<String> ie = this.filesGroups.keys(); ie.hasMoreElements(); ) {
            String s = ie.nextElement();
            System.out.println(" " + s);
        }
    }

    /**
     * Zip files.
     */
    public void zipFiles() {
        for (Enumeration<String> ie = this.filesGroups.keys(); ie.hasMoreElements(); ) {
            String s = ie.nextElement();
            System.out.println(" " + s);
            FilesList fl = this.filesGroups.get(s);
            fl.process();
            fl.zip();
        }
    }

    /**
 * The main method.
 * 
 * @param args the arguments
 */
    public static void main(String args[]) {
        System.exit(0);
    }

    private class FilesList {

        private String name;

        private String path;

        private Hashtable<String, File> lst;

        int min;

        int max;

        public FilesList(String name, String path) {
            this.path = path;
            this.name = name;
            this.lst = new Hashtable<String, File>();
        }

        public void addFile(File f) {
            this.lst.put(f.getName(), f);
        }

        public void process() {
            for (int i = 1; i < 255; i++) {
                if (this.lst.containsKey(name + "_" + i + ".html")) {
                    min = i;
                    break;
                }
            }
            int maxx = min;
            for (int i = min; i < 255; i++) {
                if (this.lst.containsKey(name + "_" + i + ".html")) {
                    maxx = i;
                }
            }
            this.max = maxx;
        }

        public String getOutName() {
            if (min == max) {
                return name + " [" + min + "].zip";
            } else return name + " [" + min + "-" + max + "].zip";
        }

        public void zip() {
            byte[] buf = new byte[1024];
            try {
                String outFilename = getOutName();
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
                for (int i = min; i <= max; i++) {
                    String fname = name + "_" + i + ".html";
                    FileInputStream in = new FileInputStream(path + fname);
                    out.putNextEntry(new ZipEntry(fname));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
                out.close();
            } catch (IOException ex) {
                System.out.println("Exception: " + ex);
            }
        }
    }
}
