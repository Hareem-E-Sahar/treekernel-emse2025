import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import edu.indiana.cs.b534.torrent.InfoDictionary;
import edu.indiana.cs.b534.torrent.TorrentMetainfo;
import edu.indiana.cs.b534.torrent.Utils;
import edu.indiana.cs.b534.torrent.struct.TorrentMetainfoImpl;

/**
 * This class can be used to test your serialization and deserialization of 
 * a torrent metainfo file. It reads (deserializes) a torrent file into a
 * TorrentMetainfo structure, computes the hash of its serialized info section,
 * writes (serializes) the TorrentMetainfo back into another file, reaks it 
 * back again, computes the hash of the info section this second time and 
 * verifies that the two hashes are the same. 
 * 
 */
public class TestRoundTrip {

    public static final boolean DEBUG = true;

    public static void main(String args[]) throws Exception {
        File testTorrent = new File("resources/freeculture.Team_6.pdf.torrent");
        TorrentMetainfo meta = TorrentMetainfoImpl.deserialize(new BufferedInputStream(new FileInputStream(testTorrent)));
        if (DEBUG) System.out.println(meta);
        InfoDictionary info = meta.getInfo();
        ByteArrayOutputStream infoBytes = new ByteArrayOutputStream();
        info.serialize(infoBytes);
        byte[] sha1 = Utils.computeHash(infoBytes.toByteArray());
        if (DEBUG) {
            for (byte b : sha1) System.out.print(Integer.toHexString(b) + " ");
            System.out.println();
        }
        File outTorrent = new File("freeculture.tmp.torrent");
        OutputStream out = new FileOutputStream(outTorrent);
        meta.serialize(out);
        out.close();
        TorrentMetainfo meta2 = TorrentMetainfoImpl.deserialize(new ByteArrayInputStream(((ByteArrayOutputStream) meta.serialize(new ByteArrayOutputStream())).toByteArray()));
        if (DEBUG) System.out.println(meta);
        info = meta2.getInfo();
        infoBytes = new ByteArrayOutputStream();
        info.serialize(infoBytes);
        byte[] sha2 = Utils.computeHash(infoBytes.toByteArray());
        if (DEBUG) {
            for (byte b : sha2) System.out.print(Integer.toHexString(b) + " ");
            System.out.println();
        }
        System.out.println("Was the SHA has of the two files the same? " + (ByteBuffer.wrap(sha1).compareTo(ByteBuffer.wrap(sha2)) == 0));
    }
}
