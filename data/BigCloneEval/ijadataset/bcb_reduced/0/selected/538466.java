package ants.p2p.utils.donkey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DonkeyHashFile implements DonkeyPacketConstants {

    static Logger log = Logger.getLogger(DonkeyHashFile.class.getName());

    public static void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public static void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    public static PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(new Object());

    /** Creates a edonkey2000 hashset of a file.
     *  Returned DonkeyFileHashSet object: 1st edonkeyhash of file, next are partial hashes.
     * @param file *complete* file to hash
     * @return the hashes object or null if the file was not found or an IOException ocurred
     */
    public static byte[][] doHash(File file) {
        byte[][] digest = null;
        try {
            propertyChangeSupport.firePropertyChange("fileIndexingInProgress", "[ED2K] " + file.getName(), new Integer(0));
            log.fine("Hashing " + file.toString());
            FileInputStream fis = new FileInputStream(file);
            digest = digest(file);
            fis.close();
        } catch (FileNotFoundException e) {
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
        return digest;
    }

    /** Creates a edonkey2000 hashset of a filestream.
      *  Returnes Array of byte[] with the hashes: 1st edonkeyhash of file, next are partial hashes.
      * @param fis is used to get a FileChannel which is read from its intial position till its limit
      * @return the hashes or null if an IOException ocurred
      */
    public static byte[][] digest(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            int i;
            int c;
            ByteBuffer hashset;
            FileChannel fc = fis.getChannel();
            MD4 msgDigest = new MD4();
            ByteBuffer bb = ByteBuffer.allocateDirect(16384).order(ByteOrder.LITTLE_ENDIAN);
            ByteBuffer di = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
            log.finest("pos:" + fc.position());
            c = (int) ((fc.size() + PARTSIZE - 1) / PARTSIZE);
            log.finer("hash " + c + " EdonkeyParts");
            hashset = ByteBuffer.allocate(16 * (c > 0 ? c : 1)).order(ByteOrder.LITTLE_ENDIAN);
            for (i = 1; i < c; i++) {
                log.finest("filechannel pos:" + fc.position() + " part: " + i + " of " + c);
                while (fc.position() <= (i * PARTSIZE - bb.capacity())) {
                    fc.read(bb);
                    bb.flip();
                    msgDigest.update(bb);
                    bb.rewind();
                    try {
                        Thread.currentThread().sleep(5);
                    } catch (InterruptedException ex) {
                    }
                }
                log.finest("filechannel pos:" + fc.position() + " part: " + i + " of " + c);
                if (fc.position() < (i * PARTSIZE)) {
                    bb.limit((int) ((i * PARTSIZE) - fc.position()));
                    fc.read(bb);
                    bb.flip();
                    msgDigest.update(bb);
                    bb.rewind();
                    try {
                        Thread.currentThread().sleep(5);
                    } catch (InterruptedException ex) {
                    }
                }
                hashset.limit(16 * i);
                msgDigest.finalDigest(hashset);
                int progress = (int) Math.floor(i * 100.0 / c);
                propertyChangeSupport.firePropertyChange("fileIndexingInProgress", "[ED2K] " + file.getName(), new Integer(progress));
            }
            if (c > 0) {
                while (fc.position() < (fc.size())) {
                    fc.read(bb);
                    bb.flip();
                    msgDigest.update(bb);
                    bb.rewind();
                    try {
                        Thread.currentThread().sleep(5);
                    } catch (InterruptedException ex) {
                    }
                }
                hashset.limit(16 * i);
            }
            msgDigest.finalDigest(hashset);
            hashset.flip();
            if (c > 1) {
                msgDigest.update(hashset);
                msgDigest.finalDigest(di);
            } else {
                di.put(hashset);
            }
            di.rewind();
            hashset.rewind();
            byte[][] hashes = new byte[(c != 1) ? (c + 1) : 1][16];
            di.get(hashes[0]);
            for (int j = 1; j < hashes.length; j++) {
                hashset.get(hashes[j]);
            }
            hashset.rewind();
            log.finer("Hash: " + Convert.bytesToHexString(hashes[0]));
            for (int j = 1; j < hashes.length; j++) {
                log.finer("partial hash of part " + j + " is " + Convert.bytesToHexString(hashes[j]));
            }
            log.fine("ed2k: file done");
            return hashes;
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
        return null;
    }

    public static boolean isValidHashset(byte[][] hashes) {
        MD4 msgDigest = new MD4();
        for (int i = 1; i < hashes.length; i++) {
            msgDigest.update(ByteBuffer.wrap(hashes[i]).order(ByteOrder.LITTLE_ENDIAN));
        }
        if (hashes.length > 1) {
            byte[] hash = new byte[16];
            msgDigest.finalDigest(ByteBuffer.wrap(hash).order(ByteOrder.LITTLE_ENDIAN));
            if (Arrays.equals(hash, hashes[0])) {
                log.fine("hashset ok");
                return true;
            } else {
                log.fine("hashset NOT ok");
                return false;
            }
        }
        log.fine("dummy hashset");
        return true;
    }
}
