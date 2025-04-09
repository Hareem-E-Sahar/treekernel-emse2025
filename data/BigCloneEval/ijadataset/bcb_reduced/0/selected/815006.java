package jpdstore;

import java.io.*;
import java.util.*;
import java.net.*;

public class Controller {

    public static final long LAST_MODIFIED_DATE = 315532800000L;

    private static final String BLOCKCOUNT_FILE = "jpds-blockcount.dat";

    private CryptoAdapter ca;

    private File dir;

    private Map openFiles = new HashMap();

    private int blockCount;

    public Controller(CryptoAdapter adapter, File directory) {
        ca = adapter;
        dir = directory;
        blockCount = getBlockCount();
    }

    private int getBlockCount() {
        File blockCountFile = new File(dir, BLOCKCOUNT_FILE);
        if (blockCountFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(blockCountFile));
                String line = br.readLine();
                br.close();
                return Integer.parseInt(line);
            } catch (IOException ex) {
                ex.printStackTrace();
                return 1;
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return 1;
            }
        } else {
            return 1;
        }
    }

    public boolean open(String password) {
        try {
            int firstIndex = -1;
            long fileLen = -1;
            String fileName = null;
            for (int i = 0; ; i++) {
                String fn = ca.getFilename(password, i);
                File f = new File(dir, fn);
                String hdrS;
                DataInputStream in = null;
                if (f.exists()) {
                    in = new DataInputStream(new BufferedInputStream(ca.getInputStream(password, f, blockCount)));
                    byte[] hdr = new byte[4];
                    in.readFully(hdr);
                    hdrS = new String(hdr, "ISO-8859-1");
                } else {
                    hdrS = "STOPIT";
                }
                if (!(hdrS.equals("CONT") || (hdrS.equals("NONE") && fileName == null)) && firstIndex != -1) {
                    File[] fs = new File[i - firstIndex];
                    for (int j = firstIndex; j < i; j++) {
                        fs[j - firstIndex] = new File(dir, ca.getFilename(password, j));
                    }
                    OpenFile of = new OpenFile(fs, fileName, fileLen, password, firstIndex);
                    for (int j = firstIndex; j < i; j++) {
                        openFiles.put(ca.getFilename(password, j), of);
                    }
                    firstIndex = -1;
                }
                if (hdrS.equals("NONE")) {
                    in.close();
                    if (firstIndex != -1) {
                        if (fileLen != 0 || fileName != null) throw new RuntimeException("Assertion failed");
                    } else {
                        fileName = null;
                        fileLen = 0;
                        firstIndex = i;
                    }
                } else if (hdrS.equals("CONT")) {
                    in.close();
                    if (firstIndex == -1) throw new IOException("Invalid data!");
                } else if (hdrS.equals("STOPIT")) {
                    if (i == 0) return false;
                    break;
                } else if (hdrS.equals("DATA")) {
                    if (firstIndex != -1) throw new RuntimeException("Assertion failed");
                    fileName = in.readUTF();
                    fileLen = in.readLong();
                    in.close();
                    firstIndex = i;
                } else {
                    in.close();
                    throw new IOException("Invalid data: " + hdrS);
                }
            }
            if (firstIndex != -1) {
                throw new RuntimeException("Assertion failed!");
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void close(String password) {
        for (Iterator it = openFiles.values().iterator(); it.hasNext(); ) {
            OpenFile of = (OpenFile) it.next();
            if (of.getPassword().equals(password)) {
                it.remove();
            }
        }
    }

    public boolean delete(String password) {
        try {
            close(password);
            for (int i = 0; ; i++) {
                String fn = ca.getFilename(password, i);
                File f = new File(dir, fn);
                if (f.exists()) {
                    f.delete();
                } else {
                    break;
                }
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteClosed() {
        File[] f = getClosedFiles();
        for (int i = 0; i < f.length; i++) {
            f[i].delete();
        }
        return true;
    }

    public boolean create(String password, int count) {
        if (password.length() == 0) {
            for (int i = 0; i < count; i++) {
                create(ca.getGarbagePassword(), 1);
            }
            return true;
        }
        try {
            for (int i = 0; i < count; i++) {
                File f = new File(dir, ca.getFilename(password, i));
                if (f.exists()) return false;
            }
            byte[] data = "NONE".getBytes("ISO-8859-1");
            for (int i = 0; i < count; i++) {
                File f = new File(dir, ca.getFilename(password, i));
                ca.storeData(data, password, f, blockCount);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public OpenFile[] getOpenFiles() {
        Set s = new HashSet();
        s.addAll(openFiles.values());
        return (OpenFile[]) s.toArray(new OpenFile[s.size()]);
    }

    public File[] getClosedFiles() {
        File[] list = dir.listFiles();
        ArrayList clsd = new ArrayList(list.length);
        for (int i = 0; i < list.length; i++) {
            File f = list[i];
            if (ca.isFile(f)) {
                if (openFiles.get(f.getName()) == null) {
                    clsd.add(f);
                }
            }
        }
        return (File[]) clsd.toArray(new File[clsd.size()]);
    }

    public boolean insert(File fle, OpenFile slot) {
        int slotcount = (int) (fle.length() / getPayloadSize()) + 1;
        if (slot.getFiles().length < slotcount || slot.getContentName() != null || !valid(slot)) return false;
        String password = slot.getPassword();
        try {
            int bufferLength = getPayloadSize();
            byte[] buffer = new byte[bufferLength + 4];
            ByteArrayOutputStream baos;
            DataOutputStream dos = new DataOutputStream(baos = new ByteArrayOutputStream());
            dos.write("DATA".getBytes("ISO-8859-1"));
            dos.writeUTF(fle.getName());
            dos.writeLong(fle.length());
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fle)));
            int len = bufferLength;
            long flen = fle.length();
            if (flen < (long) bufferLength) {
                len = (int) flen;
                flen = 0;
            } else {
                flen -= bufferLength;
            }
            dis.readFully(buffer, 0, len);
            dos.write(buffer, 0, len);
            dos.close();
            byte[] data = baos.toByteArray();
            ca.storeData(data, password, slot.getFiles()[0], blockCount);
            data = null;
            baos = null;
            dos = null;
            for (int i = 1; i < slotcount; i++) {
                System.arraycopy("CONT".getBytes("ISO-8859-1"), 0, buffer, 0, 4);
                if (flen < (long) bufferLength) {
                    len = (int) flen;
                    buffer = new byte[len + 4];
                    System.arraycopy("CONT".getBytes("ISO-8859-1"), 0, buffer, 0, 4);
                    flen = 0;
                } else {
                    flen -= bufferLength;
                }
                dis.readFully(buffer, 4, len);
                ca.storeData(buffer, password, slot.getFiles()[i], blockCount);
            }
            if (flen > 0) throw new RuntimeException("Assertion failed!");
            if (dis.read() != -1) throw new RuntimeException("File size incorrect!");
            dis.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            close(password);
            open(password);
        }
    }

    public boolean delete(OpenFile of) {
        if (!valid(of)) return false;
        try {
            byte[] data = "NONE".getBytes("ISO-8859-1");
            for (int i = 0; i < of.getFiles().length; i++) {
                ca.storeData(data, of.getPassword(), of.getFiles()[i], blockCount);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            close(of.getPassword());
            open(of.getPassword());
        }
    }

    public boolean fetch(OpenFile of, File f) {
        if (!valid(of)) return false;
        if (of.getContentName() == null) return false;
        try {
            FileOutputStream out = new FileOutputStream(f);
            File[] fs = of.getFiles();
            DataInputStream in = new DataInputStream(ca.getInputStream(of.getPassword(), fs[0], blockCount));
            byte[] buffer = new byte[4096];
            in.readFully(buffer, 0, 4);
            if (!new String(buffer, 0, 4, "ISO-8859-1").equals("DATA")) return false;
            in.readUTF();
            in.readLong();
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            for (int i = 1; i < fs.length; i++) {
                in = new DataInputStream(ca.getInputStream(of.getPassword(), fs[i], blockCount));
                in.read(buffer, 0, 4);
                if (!new String(buffer, 0, 4, "ISO-8859-1").equals("CONT")) return false;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                in.close();
            }
            out.close();
            if (f.length() != of.getContentLength()) {
                return false;
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            close(of.getPassword());
            open(of.getPassword());
        }
    }

    public boolean importData(String source, String password) {
        try {
            return NetLoader.load(source, password, dir, ca);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean importData(URL source, String password) {
        try {
            return NetLoader.load(source, password, dir, ca);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public File getDirectory() {
        return dir;
    }

    public void setDirectory(File newDir) {
        dir = newDir;
        openFiles.clear();
        blockCount = getBlockCount();
    }

    private boolean valid(OpenFile of) {
        for (Iterator it = openFiles.values().iterator(); it.hasNext(); ) {
            OpenFile nof = (OpenFile) it.next();
            if (nof == of) return true;
        }
        return false;
    }

    public int getEmptyChunks() {
        int result = 0;
        for (Iterator it = openFiles.values().iterator(); it.hasNext(); ) {
            OpenFile of = (OpenFile) it.next();
            if (of.getContentName() == null) result++;
        }
        return result;
    }

    public int getLayersCount() {
        Set hs = new HashSet();
        for (Iterator it = openFiles.values().iterator(); it.hasNext(); ) {
            OpenFile of = (OpenFile) it.next();
            hs.add(of.getPassword());
        }
        return hs.size();
    }

    public int getOpenFilesCount() {
        int result = 0;
        for (Iterator it = openFiles.values().iterator(); it.hasNext(); ) {
            OpenFile of = (OpenFile) it.next();
            if (of.getContentName() != null) result++;
        }
        return result;
    }

    private int getPayloadSize() {
        return ca.getPayloadBlockSize() * blockCount;
    }

    public boolean canBlockCountBeSet() {
        File[] fls = dir.listFiles();
        for (int i = 0; i < fls.length; i++) {
            if (ca.isFile(fls[i])) return false;
            if (fls[i].getName().equalsIgnoreCase(BLOCKCOUNT_FILE)) return false;
        }
        return true;
    }

    public void setBlockCount(int newBlockCount) {
        if (!canBlockCountBeSet()) throw new IllegalStateException();
        if (newBlockCount <= 1) throw new IllegalArgumentException();
        try {
            File f = new File(dir, BLOCKCOUNT_FILE);
            FileWriter fw = new FileWriter(f);
            fw.write("" + newBlockCount);
            fw.close();
            f.setLastModified(LAST_MODIFIED_DATE);
            setDirectory(dir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int getPayloadBlockSize() {
        return ca.getPayloadBlockSize();
    }

    public int getRawBlockSize() {
        return ca.getRawBlockSize();
    }

    public static final int TOUCH_MODE_ACCESS = 0;

    public static final int TOUCH_MODE_MODIFY = 1;

    public static final int TOUCH_MODE_RECREATE = 2;

    public static final int TOUCH_MODE_COPY_TWICE = 3;

    public void touchSomeFiles(int percentage, int mode) {
        try {
            File[] files = dir.listFiles(mff);
            Random rnd = new Random();
            for (int i = files.length - 1; i >= 0; i--) {
                int j = rnd.nextInt(i + 1);
                File swap = files[i];
                files[i] = files[j];
                files[j] = swap;
            }
            int howfar = files.length * percentage / 100;
            if (mode == TOUCH_MODE_RECREATE || mode == TOUCH_MODE_COPY_TWICE) {
                for (int i = 0; i < howfar; i++) {
                    File f = files[i];
                    if (mode == TOUCH_MODE_COPY_TWICE) {
                        File bf = new File(f.getParent(), "recreate--" + f.getName());
                        recreateFile(f, bf);
                    } else {
                        recreateFile(f, f);
                    }
                }
                if (mode == TOUCH_MODE_COPY_TWICE) {
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    for (int i = 0; i < howfar; i++) {
                        File f = files[i];
                        File bf = new File(f.getParent(), "recreate--" + f.getName());
                        recreateFile(bf, f);
                    }
                }
            } else if (mode == TOUCH_MODE_ACCESS || mode == TOUCH_MODE_MODIFY) {
                for (int i = 0; i < howfar; i++) {
                    RandomAccessFile raf = new RandomAccessFile(files[i], mode == TOUCH_MODE_MODIFY ? "rw" : "r");
                    if (raf.length() > 0) {
                        int pos = rnd.nextInt(Math.max(0, (int) raf.length()));
                        raf.seek(pos);
                        byte byte1 = raf.readByte();
                        if (mode == TOUCH_MODE_MODIFY) {
                            raf.seek(pos);
                            raf.write(byte1);
                        }
                    }
                    raf.close();
                    files[i].setLastModified(LAST_MODIFIED_DATE);
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void recreateFile(File file, File storeTo) throws IOException {
        if (file.length() >= Integer.MAX_VALUE) throw new RuntimeException("Your files are too large!");
        int fileLength = (int) file.length();
        byte[] buff = new byte[fileLength];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(buff);
        dis.close();
        file.delete();
        OutputStream out = new FileOutputStream(storeTo);
        out.write(buff);
        out.close();
        storeTo.setLastModified(LAST_MODIFIED_DATE);
    }

    private MyFileFilter mff = new MyFileFilter();

    private class MyFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            return ca.isFile(pathname) || pathname.getName().equalsIgnoreCase(BLOCKCOUNT_FILE);
        }
    }
}
