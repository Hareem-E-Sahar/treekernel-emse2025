package iso.io;

import java.io.*;
import java.util.zip.*;
import java.util.*;

public class SetFileControl {

    private String tempdir;

    public SetFileControl(String tempPath) {
        this.tempdir = tempPath;
    }

    public String getTempDirectory() {
        return tempdir;
    }

    public void saveSet(String path, ElementSet set) throws IOException {
        ListIterator<SetElement> iterator;
        ElementSet copy = set.createCopy();
        makeTempDirectory();
        iterator = copy.getInList().listIterator();
        while (iterator.hasNext()) {
            SetElement cur = iterator.next();
            if (cur.imgPath == null || cur.imgPath == "") throw new IOException("Missing image file's path in the ElementSet"); else {
                copyFileToTemp(cur.imgPath, cur.ID);
                cur.imgPath = cur.ID + ".png";
            }
        }
        saveElementSetHead(copy, tempdir + "/head.set");
        zipingTemp(path);
        removeTempDirectory();
    }

    private void copyFileToTemp(String path, int ID) throws IOException {
        File f = new File(path);
        if (f.exists()) if (f.isFile()) {
            FileInputStream fin = new FileInputStream(f);
            FileOutputStream fout = new FileOutputStream(tempdir + "/" + ID + ".png");
            byte[] buffer = new byte[512];
            while (fin.read(buffer) > 0) {
                fout.write(buffer);
            }
            fin.close();
            fout.close();
        } else throw new IOException("The path not point to a File!"); else throw new FileNotFoundException(path + "File not found!");
    }

    private void zipingTemp(String path) throws IOException {
        FileInputStream fin;
        byte[] buf = new byte[512];
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(path));
        String[] files = new File(tempdir).list();
        for (int i = 0; i < files.length; i++) {
            fin = new FileInputStream(tempdir + "/" + files[i]);
            zout.putNextEntry(new ZipEntry(files[i]));
            int len;
            while ((len = fin.read(buf)) > 0) {
                zout.write(buf, 0, len);
            }
            zout.closeEntry();
            fin.close();
        }
        zout.close();
    }

    private static void saveElementSetHead(ElementSet set, String path) throws IOException {
        FileWriter f_out = new FileWriter(path);
        ListIterator<SetElement> iterator = set.getInList().listIterator();
        SetElement cur;
        f_out.write("Set name:\n");
        f_out.write(set.getName() + "\n");
        f_out.write("Set size:\n");
        f_out.write(set.getInList().size() + "\n");
        f_out.write("---------SetElements---------\n");
        while (iterator.hasNext()) {
            cur = iterator.next();
            f_out.write(cur.name + "\n");
            f_out.write(cur.ID + "\n");
            f_out.write(cur.imgPath + "\n");
            f_out.write(cur.a_segment + "\n");
            f_out.write(cur.bodyHeight + "\n");
            f_out.write("=>------****------<=\n");
        }
        f_out.write("------------END--------------\n");
        f_out.close();
    }

    public ElementSet loadSet(String path) throws IOException {
        makeTempDirectory();
        unziptoTemp(path);
        ElementSet set = loadElementSetHead(tempdir + "/head.set");
        ListIterator<SetElement> iterator = set.getInList().listIterator();
        SetElement cur;
        while (iterator.hasNext()) {
            cur = iterator.next();
            cur.imgPath = tempdir + "/" + cur.imgPath;
        }
        return set;
    }

    private void unziptoTemp(String path) throws IOException {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(path));
        FileOutputStream fout;
        ZipEntry curentry;
        byte[] buffer = new byte[512];
        while ((curentry = zin.getNextEntry()) != null) {
            fout = new FileOutputStream(tempdir + "/" + curentry.getName());
            int len;
            while ((len = zin.read(buffer)) != -1) {
                fout.write(buffer, 0, len);
            }
            curentry.clone();
            fout.close();
        }
        zin.close();
    }

    private static ElementSet loadElementSetHead(String path) throws IOException {
        BufferedReader f_in = new BufferedReader(new FileReader(path));
        ElementSet set;
        SetElement cur;
        f_in.readLine();
        String name = f_in.readLine();
        set = new ElementSet(name);
        f_in.readLine();
        int size = Integer.valueOf(f_in.readLine());
        f_in.readLine();
        for (int i = 0; i < size; i++) {
            cur = new SetElement();
            cur.name = f_in.readLine();
            cur.ID = Integer.valueOf(f_in.readLine());
            cur.imgPath = f_in.readLine();
            cur.a_segment = Integer.valueOf(f_in.readLine());
            cur.bodyHeight = Integer.valueOf(f_in.readLine());
            f_in.readLine();
            set.addSetElement(cur);
        }
        f_in.close();
        return set;
    }

    public void removeTempDirectory() {
        deleteFile(new File(tempdir));
    }

    private void makeTempDirectory() {
        File dir = new File(tempdir);
        if (dir.isDirectory()) {
            if (dir.exists()) {
                deleteFile(dir);
            }
        }
        dir.mkdirs();
    }

    private boolean deleteFile(File path) {
        if (path.exists()) {
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteFile(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (path.delete());
    }
}
