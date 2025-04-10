package nickyb.sqleonardo.common.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Store {

    protected static final int INDEX_DATA = 0;

    protected static final int INDEX_SUBS = 1;

    protected static final int INDEX_JUMP = 2;

    private Hashtable mountpoints;

    private Object[] cmp;

    public Store() {
        mountpoints = new Hashtable();
        cmp = new Object[3];
        cmp[INDEX_DATA] = new ArrayList();
        cmp[INDEX_SUBS] = new Hashtable();
    }

    protected Object[] get(String entry) {
        return (Object[]) mountpoints.get(entry);
    }

    protected void put(String entry, Object[] content) {
        mountpoints.put(entry, content);
    }

    public void home() {
        cmp[INDEX_JUMP] = null;
    }

    public boolean canMount(String entry) {
        return mountpoints.containsKey(entry);
    }

    public ArrayList mount() {
        return (ArrayList) cmp[INDEX_DATA];
    }

    public ArrayList mount(String entry) {
        if (mountpoints.containsKey(entry)) {
            cmp = (Object[]) mountpoints.get(entry);
        } else {
            cmp = new Object[3];
            cmp[INDEX_DATA] = new ArrayList();
            cmp[INDEX_SUBS] = new Hashtable();
            mountpoints.put(entry, cmp);
        }
        return mount();
    }

    public Enumeration mounts() {
        return mountpoints.keys();
    }

    public void umount(String entry) {
        cmp = new Object[3];
        cmp[INDEX_DATA] = new ArrayList();
        cmp[INDEX_SUBS] = new Hashtable();
        mountpoints.remove(entry);
    }

    public boolean canJump(String sub) {
        Hashtable subs = (Hashtable) cmp[INDEX_SUBS];
        return subs.containsKey(sub);
    }

    public ArrayList jump() {
        return (ArrayList) ((Object[]) cmp[INDEX_JUMP])[INDEX_DATA];
    }

    public ArrayList jump(String sub) {
        Hashtable subs = (Hashtable) cmp[INDEX_SUBS];
        if (cmp[INDEX_JUMP] != null) subs = (Hashtable) ((Object[]) cmp[INDEX_JUMP])[INDEX_SUBS];
        if (subs.containsKey(sub)) {
            cmp[INDEX_JUMP] = (Object[]) subs.get(sub);
        } else {
            Object[] jp = new Object[2];
            jp[INDEX_DATA] = new ArrayList();
            jp[INDEX_SUBS] = new Hashtable();
            subs.put(sub, jp);
            cmp[INDEX_JUMP] = jp;
        }
        return jump();
    }

    public ArrayList jump(String[] subs) {
        for (int i = 0; i < subs.length; i++) jump(subs[i]);
        return jump();
    }

    public Enumeration jumps() {
        Hashtable subs = (Hashtable) cmp[INDEX_SUBS];
        if (cmp[INDEX_JUMP] != null) subs = (Hashtable) ((Object[]) cmp[INDEX_JUMP])[INDEX_SUBS];
        return subs.keys();
    }

    public void ujump(String sub) {
        Hashtable subs = (Hashtable) cmp[INDEX_SUBS];
        subs.remove(sub);
        home();
    }

    public void rename(String oldentry, String newentry) {
        Object[] obj = (Object[]) mountpoints.get(oldentry);
        mountpoints.put(newentry, obj);
        mountpoints.remove(oldentry);
    }

    public void reset() {
        mountpoints = new Hashtable();
        cmp = new Object[3];
        cmp[INDEX_DATA] = new ArrayList();
        cmp[INDEX_SUBS] = new Hashtable();
    }

    public void load(String filename) throws IOException, ClassNotFoundException {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(filename));
        for (ZipEntry entry = null; (entry = zin.getNextEntry()) != null; ) {
            Object[] content = new Object[3];
            content[INDEX_DATA] = new ObjectInputStream(zin).readObject();
            content[INDEX_SUBS] = new ObjectInputStream(zin).readObject();
            this.put(entry.getName(), content);
        }
        zin.close();
    }

    public void save(String filename) throws IOException {
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(filename));
        for (Enumeration e = mounts(); e.hasMoreElements(); ) {
            String name = e.nextElement().toString();
            zout.putNextEntry(new ZipEntry(name));
            Object[] content = this.get(name);
            new ObjectOutputStream(zout).writeObject(content[INDEX_DATA]);
            new ObjectOutputStream(zout).writeObject(content[INDEX_SUBS]);
            zout.closeEntry();
        }
        zout.close();
    }

    private void print() {
        Enumeration e = mountpoints.keys();
        while (e.hasMoreElements()) {
            String entry = e.nextElement().toString();
            System.out.println("*** " + entry + " ***");
            print("", (Object[]) mountpoints.get(entry));
        }
    }

    private void print(String indent, Object[] obj) {
        ArrayList al = (ArrayList) obj[INDEX_DATA];
        System.out.print(indent + al.size() + "{");
        for (int i = 0; i < al.size(); i++) System.out.print(al.get(i) + ",");
        System.out.println("}");
        Hashtable h = (Hashtable) obj[INDEX_SUBS];
        Enumeration e = h.keys();
        while (e.hasMoreElements()) {
            String entry = e.nextElement().toString();
            System.out.println(indent + "\t" + entry);
            print(indent + "\t", (Object[]) h.get(entry));
        }
    }

    public static void main(String[] args) {
        Store s = new Store();
        try {
            String filename = System.getProperty("user.home") + "/.sqleonardo";
            System.out.println("### " + filename + " ###");
            s.load(filename);
            s.print();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
