package saadadb.relationship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;

/**
 * This class is a simple table with a dichotomic access. It is used to stored OIDs in matchpattern processing
 * in replacement of Set which although more efficient require too much memory
 * Typical access time for 1000000 entries: 0.0014 ms/entry, about 15% slower tha TreeSet: I buy it
 * @author michel
 * * @version $Id: KeySet.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class KeySet implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    protected String name;

    protected String pathname;

    protected long[] keys;

    protected int size;

    /**
	 * @param size
	 */
    public KeySet(int size) {
        this.size = size;
        this.keys = new long[size];
        this.name = "noName";
        this.pathname = "noName.sri";
    }

    /**
	 * @param name
	 * @param path
	 * @param extension
	 */
    public KeySet(String name, String path, String extension) {
        this.name = name + "." + extension + ".sri";
        this.pathname = path + Database.getSepar() + this.name;
    }

    /**
	 * @param keys2
	 */
    public KeySet(long[] keys2) {
        this.size = keys2.length;
        this.keys = new long[size];
        for (int i = 0; i < keys2.length; i++) {
            this.keys[i] = keys2[i];
        }
        this.name = "noName";
        this.pathname = "noName.sri";
    }

    /**
	 * @param keys2
	 * @throws FatalException 
	 * @throws SQLException 
	 */
    public KeySet(SaadaQLResultSet srs) throws FatalException {
        if (Database.getWrapper().forwardOnly) {
            ArrayList<Long> alk = new ArrayList<Long>();
            int i = 0;
            try {
                while (srs.next()) {
                    alk.add(srs.getOid());
                    i++;
                }
            } catch (SQLException e) {
                FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
            }
            this.size = i;
            this.keys = new long[size];
            i = 0;
            for (Long l : alk) {
                this.keys[i] = l;
                i++;
            }
        } else {
            this.size = srs.getSize();
            this.keys = new long[this.size];
            int i = 0;
            try {
                while (srs.next()) {
                    keys[i] = srs.getOid();
                    i++;
                }
            } catch (SQLException e) {
                FatalException.throwNewException(SaadaException.DB_ERROR, e);
            }
        }
        this.name = "noName";
        this.pathname = "noName.sri";
    }

    /**
	 * @param keys2
	 */
    public KeySet(Set<Long> keys2) {
        this.size = keys2.size();
        this.keys = new long[size];
        int i = 0;
        for (long v : keys2) {
            this.keys[i] = v;
            i++;
        }
        Arrays.sort(this.keys);
        this.name = "noName";
        this.pathname = "noName.sri";
    }

    /**
	 * @return
	 */
    public String getName() {
        return this.name;
    }

    /**
	 * @return
	 */
    public String getPathname() {
        return this.pathname;
    }

    /**
	 * @param sourcemap
	 */
    public void buildIndex(HashMap<Long, ArrayList<Object>> sourcemap) {
        initKeys(sourcemap.keySet());
    }

    /**
	 * @param keyset
	 * @param size
	 */
    private void initKeys(Set<Long> keyset) {
        this.size = keyset.size();
        this.keys = new long[this.size];
        int i = 0;
        for (long v : keyset) {
            this.keys[i] = v;
            i++;
        }
    }

    /**
	 * @return
	 */
    public int getSize() {
        return this.size;
    }

    /**
	 * @param ind
	 * @return
	 */
    public long getKey(int ind) {
        if (ind >= 0 && ind < this.size) {
            return keys[ind];
        }
        return -1;
    }

    /**
	 * @return
	 */
    public TreeSet<Long> getKeySet() {
        TreeSet<Long> retour = new TreeSet<Long>();
        for (int i = 0; i < this.size; i++) {
            retour.add(new Long(this.keys[i]));
        }
        return retour;
    }

    /**
	 * @param key
	 * @return
	 */
    public int hasKey(long key) {
        long l = -1;
        int i;
        for (i = 0; i < keys.length; i++) {
            if ((l = keys[i]) >= key) {
                break;
            }
        }
        if (l == key) {
            return i;
        } else {
            return -1;
        }
    }

    /**
	 * @param key
	 * @param g
	 * @param d
	 * @return
	 */
    protected int dichotoLook(long key, int g, int d) {
        if (this.keys.length == 0) {
            return -1;
        }
        if (g >= d) {
            if (key == this.keys[g]) {
                return g;
            }
            if (g > 0 && key == keys[g - 1]) {
                return g - 1;
            }
            return -1;
        }
        int m = (d + g) / 2;
        if (key <= keys[m]) {
            return dichotoLook(key, g, m);
        }
        return dichotoLook(key, m + 1, d);
    }

    /**
	 * @param key
	 * @param g
	 * @param d
	 * @return
	 */
    protected int dichotoLookAfter(long key, int g, int d) {
        if (g >= d) {
            if (key == this.keys[g]) {
                return g;
            }
            if (g > 0 && key == keys[g - 1]) {
                return g - 1;
            }
            return g;
        }
        int m = (d + g) / 2;
        if (key <= keys[m]) {
            return dichotoLookAfter(key, g, m);
        }
        return dichotoLookAfter(key, m + 1, d);
    }

    /**
	 * @param key
	 * @param g
	 * @param d
	 * @return
	 */
    protected int dichotoLookBefore(long key, int g, int d) {
        if (g >= d) {
            if (key == this.keys[g]) {
                return g;
            }
            if (g > 0 && key == keys[g - 1]) {
                return g - 1;
            }
            return (g == 0) ? 0 : (g - 1);
        }
        int m = (d + g) / 2;
        if (key <= keys[m]) {
            return dichotoLookBefore(key, g, m);
        }
        return dichotoLookBefore(key, m + 1, d);
    }

    /**
	 * Returns the position of the key equals to "key" or -1
	 * @param key
	 * @return
	 */
    public int hasDichotoKey(long key) {
        return dichotoLook(key, 0, this.size - 1);
    }

    /**
	 * @param key
	 * @return
	 */
    public boolean contains(long key) {
        if (dichotoLook(key, 0, this.size - 1) == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
	 * Returns the position of the smallest key higher or equals to "key" 
	 * @param key
	 * @return
	 */
    public int hasDichotoKeyAfter(long key) {
        return dichotoLookAfter(key, 0, this.size - 1);
    }

    /**
	 * Returns the position of the greatest key lower or equals to "key" 
	 * @param key
	 * @return
	 */
    public int hasDichotoKeyBefore(long key) {
        return dichotoLookBefore(key, 0, this.size - 1);
    }

    /**
	 * 
	 */
    public void scan() {
        scan(-1);
    }

    /**
	 * 
	 */
    public void scan(int lgmax) {
        for (int i = 0; i < size; i++) {
            System.out.println("[" + i + "] " + Long.toHexString(this.keys[i]));
            if (lgmax > 0 && i >= lgmax) {
                System.out.println("truncated..");
                break;
            }
        }
        System.out.println("");
    }

    /**
	 * @param cp_length
	 */
    public void dummyFill(int cp_length) {
        keys[0] = 1;
        for (int i = 1; i < size; i++) {
            keys[i] = keys[i - 1] + 4;
        }
    }

    public static void testDicho() {
        for (int size = 1; size < 1000000; ) {
            KeyIndex ki = new KeyIndex("", "", size);
            ki.dummyFill(0);
            long v0 = ki.keys[0] - 100;
            long v1 = ki.keys[ki.getSize() - 1] + 100;
            int faux = 0, vrai = 0;
            System.out.println("*** size = " + size);
            for (long i = v0; i <= v1; i++) {
                int ret = ki.hasDichotoKey(i, false);
                if (ret == -1) {
                    faux++;
                } else {
                    if (ki.keys[ret] != i) {
                        System.out.println("ERREUR a i=" + i + " : " + ki.keys[ret] + " !=  " + i);
                        System.exit(1);
                    }
                    vrai++;
                }
            }
            long delta = (10 * size) / 100;
            if (delta == 0) {
                delta = 1;
            }
            size += delta;
            System.out.println(" keys found (" + vrai + "/" + faux + ") ");
        }
    }

    public static void testDichoApprox() {
        for (int size = 10; size < 11; ) {
            KeyIndex ki = new KeyIndex("", "", size);
            ki.dummyFill(0);
            ki.scan(-1);
            long v0 = ki.keys[0] - 2;
            long v1 = ki.keys[ki.getSize() - 1] + 2;
            int faux = 0, vrai = 0;
            System.out.println("*** size = " + size);
            for (long i = v0; i <= v1; i++) {
                int ret = ki.hasDichotoKeyAfter(i, false);
                int ret2 = ki.hasDichotoKeyBefore(i, false);
                if (ret == -1) {
                    faux++;
                } else {
                    if (ret != 0 && ki.keys[ret] < i) {
                        System.out.println("ERREUR a i=" + i + " : " + ki.keys[ret] + " !=  " + i);
                        System.exit(1);
                    }
                    if (ret2 != 0 && ki.keys[ret2] > i) {
                        System.out.println("ERREUR a i=" + i + " : " + ki.keys[ret2] + " !=  " + i);
                        System.exit(1);
                    }
                    vrai++;
                }
                System.out.print("cherche " + i + ": between key[" + ret2 + "] = " + ki.keys[ret2]);
                System.out.println(" and  key[" + ret + "] = " + ki.keys[ret]);
            }
            long delta = (10 * size) / 100;
            if (delta == 0) {
                delta = 1;
            }
            size += delta;
            System.out.println(" keys found (" + vrai + "/" + faux + ") ");
        }
    }

    /**
	 * @param args
	 * @throws Exception 
	 */
    public static void mainXX(String[] args) throws Exception {
        KeyIndex ind;
        KeyIndex.testDichoApprox();
        System.exit(0);
        long t0 = (new Date()).getTime();
        int size = 100000;
        ind = new KeyIndex("", "", size);
        System.out.println("Creation in " + ((new Date()).getTime() - t0) + " ms");
        t0 = (new Date()).getTime();
        ind.save();
        System.out.println("saved in " + ((new Date()).getTime() - t0) + " ms");
        t0 = (new Date()).getTime();
        ind.load();
        System.out.println("loaded in " + ((new Date()).getTime() - t0) + " ms");
        t0 = (new Date()).getTime();
        int i = 0, faux = 0, vrai = 0;
        long[] keys = new long[1000];
        for (i = 0; i < 1000; i++) {
            keys[i] = (long) (Math.random() * size * 1.5);
            keys[0] = 0;
            keys[999] = size - 1;
            for (i = 0; i < 1000; i++) {
                int ret = ind.hasKey(keys[i], false);
                if (ret == -1) {
                    faux++;
                } else {
                    vrai++;
                }
            }
            System.out.println(i + " keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
            t0 = (new Date()).getTime();
            faux = 0;
            vrai = 0;
            for (i = 0; i < 1000; i++) {
                int ret = ind.hasDichotoKey(keys[i], false);
                if (ret == -1) {
                    faux++;
                } else {
                    vrai++;
                }
            }
            System.out.println(i + " keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
            t0 = (new Date()).getTime();
            int ret = ind.selectCPMatchingList(new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, false);
            System.out.println(ret + " counterpars tagged  in " + ((new Date()).getTime() - t0) + " ms");
            System.exit(1);
            t0 = (new Date()).getTime();
            int free = (ind.getFREEKeys()).length;
            System.out.println(free + " free keys found  in " + ((new Date()).getTime() - t0) + " ms");
            long[] rkeys = new long[1000];
            for (i = 0; i < rkeys.length; i++) {
                rkeys[i] = 2 * i;
            }
            t0 = (new Date()).getTime();
            ind.selectKeys(rkeys);
            ind.getSELECTEDKeys();
            System.out.println(rkeys.length + " keys selected  in " + ((new Date()).getTime() - t0) + " ms");
            t0 = (new Date()).getTime();
            i = 0;
            faux = 0;
            vrai = 0;
            for (i = 0; i < 1000; i++) {
                long key = (long) (Math.random() * size * 1.5);
                ret = ind.hasKey(key, false);
                if (ret == -1) {
                    faux++;
                } else {
                    vrai++;
                }
            }
            System.out.println(i + " keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
            t0 = (new Date()).getTime();
            i = 0;
            faux = 0;
            vrai = 0;
            long[] rcp = new long[1000];
            for (i = 0; i < 1000; i++) {
                rcp[i] = ((long) (Math.random() * size * 1.5) << 32) + 1;
            }
            ind.selectKeysMatchingCPList(rcp);
            i = ind.getSELECTEDKeys().length;
            System.out.println(i + " keys found  in " + ((new Date()).getTime() - t0) + " ms");
            t0 = (new Date()).getTime();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File("/home/michel/Desktop/index.dump"))));
                out.writeObject(ind);
                out.close();
            } catch (FileNotFoundException e) {
                Messenger.printStackTrace(e);
            } catch (IOException e) {
                Messenger.printStackTrace(e);
            }
            System.out.println("serializable saved in " + ((new Date()).getTime() - t0) + " ms");
            t0 = (new Date()).getTime();
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File("/home/michel/Desktop/index.dump"))));
                ind = (KeyIndex) (in.readObject());
                in.close();
            } catch (FileNotFoundException e) {
                Messenger.printStackTrace(e);
            } catch (IOException e) {
                Messenger.printStackTrace(e);
            } catch (ClassNotFoundException e) {
                Messenger.printStackTrace(e);
            }
            System.out.println("serializable loaded in " + ((new Date()).getTime() - t0) + " ms");
        }
    }

    /**
	 * @return Returns the keys.
	 */
    public long[] getKeys() {
        return keys;
    }

    public static void main(String[] args) {
        int size = 1000000;
        KeySet ks = new KeySet(size);
        for (int i = 0; i < size; i++) {
            ks.keys[i] = i;
        }
        long[] sample = new long[1000];
        for (int i = 0; i < 1000; i++) {
            sample[i] = (long) (Math.random() * size * 1.5);
        }
        long t0 = (new Date()).getTime();
        int faux = 0, vrai = 0;
        for (int i = 0; i < 10000; i++) {
            int ret = ks.hasKey(sample[i % 1000]);
            if (ret == -1) {
                faux++;
            } else {
                vrai++;
            }
        }
        System.out.println(" keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
        t0 = (new Date()).getTime();
        faux = 0;
        vrai = 0;
        for (int i = 0; i < 10000; i++) {
            int ret = ks.hasDichotoKey(sample[i % 1000]);
            if (ret == -1) {
                faux++;
            } else {
                vrai++;
            }
        }
        System.out.println(" keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
    }
}
