import edu.indiana.cs.b534.torrent.InfoDictionary;
import edu.indiana.cs.b534.torrent.TorrentMetainfo;
import edu.indiana.cs.b534.torrent.Utils;
import edu.indiana.cs.b534.torrent.struct.TorrentMetainfoImpl;
import junit.framework.TestCase;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ReadStructTest extends TestCase {

    public void testTorrentMetaInfoTest() throws Exception {
        File testTorrent = new File("resources/freeculture.Team_6.pdf.torrent");
        TorrentMetainfo meta = TorrentMetainfoImpl.deserialize(new BufferedInputStream(new FileInputStream(testTorrent)));
        InfoDictionary info = meta.getInfo();
        ByteArrayOutputStream infoBytes = new ByteArrayOutputStream();
        info.serialize(infoBytes);
        byte[] sha1 = Utils.computeHash(infoBytes.toByteArray());
        File outTorrent = new File("freeculture.tmp.torrent");
        OutputStream out = new FileOutputStream(outTorrent);
        meta.serialize(out);
        out.close();
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        meta.serialize(baout);
        ByteArrayInputStream abInputStream = new ByteArrayInputStream(baout.toByteArray());
        TorrentMetainfo meta2 = TorrentMetainfoImpl.deserialize(abInputStream);
        info = meta2.getInfo();
        infoBytes = new ByteArrayOutputStream();
        info.serialize(infoBytes);
        byte[] sha2 = Utils.computeHash(infoBytes.toByteArray());
        assertTrue(ByteBuffer.wrap(sha1).compareTo(ByteBuffer.wrap(sha2)) == 0);
    }
}
