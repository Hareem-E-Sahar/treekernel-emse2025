package com.memoire.bu;

import java.io.*;
import java.text.DateFormat;
import com.memoire.fu.FuLib;
import com.memoire.fu.FuSort;
import com.memoire.vfs.VfsFile;
import com.memoire.vfs.VfsFileFile;
import com.memoire.vfs.VfsFileRam;
import com.memoire.vfs.VfsRamDisk;

/**
 * @author fred deniger
 * @version $Id: TestvfsRamDisk.java,v 1.1 2007-05-04 13:43:21 deniger Exp $
 */
final class TestvfsRamDisk {

    private TestvfsRamDisk() {
    }

    public static void syntax() {
        String s = "Syntax: furamdisk (i|e|l|t) path-to-disk [file [...]]\n" + "  c : create a disk\n" + "  i : inject files in the disk\n" + "  e : erase  files in the disk\n" + "  l : list the disk content\n" + "  t : disk tree\n";
        System.err.println(s);
        System.exit(-1);
    }

    public static void main(String[] _args) {
        if ((_args.length < 2) || (_args[0].length() != 1)) syntax();
        char command = _args[0].charAt(0);
        String disk = _args[1];
        boolean dirty = false;
        if (command == 'c') {
            if (new File(disk).exists()) {
                System.err.println("Error: the disk already exists");
                System.exit(-2);
            }
            dirty = true;
        } else {
            if (!new File(disk).exists()) {
                System.err.println("Error: can not find the disk");
                System.exit(-3);
            }
            try {
                VfsRamDisk.setInstance(TestvfsRamDisk.loadDisk(disk));
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
                System.exit(-4);
            }
        }
        if (_args.length == 2) {
            String[] p = VfsRamDisk.getInstance().getPaths();
            FuSort.sort(p);
            if (p.length > 0) {
                if (command == 't') VfsRamDisk.tree(new VfsFileRam(p[0])); else if (command == 'l') {
                    for (int i = 0; i < p.length; i++) TestvfsRamDisk.list(new VfsFileRam(p[i]));
                }
            } else if (command != 'c') System.out.println("empty disk");
        } else {
            for (int i = 2; i < _args.length; i++) {
                if ((command == 'c') || (command == 'i')) {
                    try {
                        VfsFile g = new VfsFileFile(_args[i]);
                        VfsFile f = new VfsFileRam(g.getAbsolutePath());
                        if (g.isDirectory()) {
                            System.err.println("create " + g);
                            f.mkdirs();
                        } else {
                            f.getParentVfsFile().mkdirs();
                        }
                        if (g.isFile()) {
                            InputStream in = g.getInputStream();
                            OutputStream out = f.getOutputStream();
                            int c;
                            int n = 0;
                            while ((c = in.read()) != -1) {
                                n++;
                                out.write(c);
                            }
                            in.close();
                            out.close();
                            System.err.println("copy " + g + " [" + n + " octets]");
                        }
                    } catch (IOException ex) {
                        System.err.println("Error: can not create/copy " + _args[i]);
                        System.exit(-6);
                    }
                    dirty = true;
                } else {
                    VfsFileRam f = new VfsFileRam(_args[i]);
                    if (f.exists()) {
                        if (command == 'e') {
                            f.delete();
                            dirty = true;
                        } else if (command == 't') VfsRamDisk.tree(f); else if (command == 'l') TestvfsRamDisk.list(f);
                    } else {
                        System.err.println("Error: doesn't exist " + _args[i]);
                        System.exit(-7);
                    }
                }
            }
        }
        if (dirty) {
            try {
                TestvfsRamDisk.saveDisk(disk, VfsRamDisk.getInstance());
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
                System.exit(-5);
            }
        }
        System.exit(0);
    }

    public static synchronized void saveDisk(String _path, VfsRamDisk _instance) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(_path)));
        oos.writeObject(_instance);
        oos.flush();
        oos.close();
    }

    public static synchronized VfsRamDisk loadDisk(String _path) throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(_path)));
            Object r = ois.readObject();
            ois.close();
            return (VfsRamDisk) r;
        } catch (ClassNotFoundException ex) {
            throw new IOException("bad format");
        }
    }

    public static void list(VfsFile _f) {
        String s = _f.getName();
        if ("".equals(s)) s = "/";
        String t = "            ";
        s += "                    ";
        s = s.substring(0, 20) + " ";
        if (_f.isDirectory()) {
            s += 'd';
            t += _f.list().length;
        } else if (_f.isFile()) {
            s += 'f';
            t += _f.length();
        } else s += 'u';
        t = t.substring(t.length() - 12);
        s += " " + t;
        t = FuLib.date(_f.lastModified(), java.text.DateFormat.SHORT) + " " + FuLib.time(_f.lastModified(), java.text.DateFormat.SHORT) + "                    ";
        t = t.substring(0, 20);
        s += " " + t;
        System.out.println(s);
    }
}
