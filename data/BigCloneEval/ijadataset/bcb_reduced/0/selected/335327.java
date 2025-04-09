package n2hell.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import n2hell.SystemMessages;
import n2hell.torrent.FileInfo;
import n2hell.torrent.RTorrent;
import n2hell.torrent.TorrentInfo;
import n2hell.xmlrpc.SshFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redstone.xmlrpc.XmlRpcFault;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;

public class TorrentOpener {

    private final Log log = LogFactory.getLog(TorrentOpener.class);

    private Set<ScpCopy> jobs = new HashSet<ScpCopy>();

    private final String sysOpenCommand;

    private final RTorrent rtorrent;

    private final boolean isClientLocal;

    private final String zipServletPath;

    public TorrentOpener(RTorrent rtorrent, boolean isClientLocal, String zipServletPath) {
        this.rtorrent = rtorrent;
        this.isClientLocal = isClientLocal;
        this.zipServletPath = zipServletPath;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") == 0) sysOpenCommand = "explorer"; else if (os.indexOf("mac") == 0) sysOpenCommand = "open"; else {
            sysOpenCommand = null;
            log.warn("unknown OS " + os);
        }
    }

    /**
	 * returns supported operations for torrent
	 * @param hash
	 * 	torrent hash
	 * @return
	 */
    public Supported[] getSupported(Object[] hashes) {
        Supported[] res = new Supported[hashes.length];
        for (int i = 0; i < hashes.length; i++) {
            String hash = (String) hashes[i];
            Supported s = new Supported();
            s.setId(hash);
            if (isClientLocal) {
                if (rtorrent.getBoxConfig().getSsh().getEnabled()) s.setScp(true); else if (sysOpenCommand != null) s.setOpen(true); else s.setDown(true);
            } else s.setDown(true);
            res[i] = s;
        }
        return res;
    }

    /**
	 * returns url for zip stream
	 * @param hash
	 * 	torrent's hash
	 * @return
	 */
    public String[] getDown(Object[] hashes) {
        String[] res = new String[hashes.length];
        for (int i = 0; i < hashes.length; i++) {
            res[i] = zipServletPath + "?hash=" + hashes[i];
        }
        return res;
    }

    /**
	 * SCP torrent to machine
	 * @param hash
	 * 	torrent's hash
	 * @throws IOException 
	 */
    public void getSCP(Object[] hashes, String localPath) throws IOException {
        for (Object hash : hashes) {
            ScpCopy scpCopy = new ScpCopy((String) hash, localPath, rtorrent);
            scpCopy.start();
            jobs.add(scpCopy);
        }
    }

    /**
	 * opens torrent
	 * @param hash
	 *  torrent's hash
	 * @throws IOException 
	 */
    public void getOpen(Object[] hashes) throws IOException {
        try {
            for (Object hash : hashes) {
                TorrentInfo info = rtorrent.getTorrentInfo((String) hash);
                String dir = info.getDirectory();
                String baseDir = info.getBaseDirectory();
                if (dir != null) baseDir += File.separatorChar + dir; else if (info.getFiles().length == 1) baseDir += File.separatorChar + info.getFiles()[0].getPath();
                Runtime.getRuntime().exec(new String[] { sysOpenCommand, baseDir });
            }
        } catch (XmlRpcFault e) {
            throw new IOException(e.getMessage());
        }
    }

    public TorrentStream getTorrentStream(String hash) throws IOException, XmlRpcFault {
        return new TorrentStream(hash, rtorrent);
    }

    public class Supported {

        private String id;

        private Boolean open;

        private Boolean scp;

        private Boolean down;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Boolean getOpen() {
            return open;
        }

        public void setOpen(Boolean open) {
            this.open = open;
        }

        public Boolean getScp() {
            return scp;
        }

        public void setScp(Boolean scp) {
            this.scp = scp;
        }

        public Boolean getDown() {
            return down;
        }

        public void setDown(Boolean down) {
            this.down = down;
        }
    }

    public class TorrentStream {

        private TorrentInfo info;

        private SCPClient scp;

        private static final int BUFFER_SIZE = 2048;

        TorrentStream(String hash, RTorrent rtorrent) throws XmlRpcFault, IOException {
            info = rtorrent.getTorrentInfo(hash);
            if (rtorrent.getBoxConfig().getSsh().getEnabled()) {
                Connection connection = SshFactory.getConnection(rtorrent.getBoxConfig().getSsh());
                scp = connection.createSCPClient();
            } else scp = null;
        }

        public String getFileName() {
            String dir = info.getDirectory();
            if (dir == null && info.getFiles().length == 1) return info.getFiles()[0].getPath(); else return info.getName() + ".zip";
        }

        public void write(OutputStream out) throws IOException {
            String dir = info.getDirectory();
            if (dir == null && info.getFiles().length == 1) {
                String path = info.getBaseDirectory() + "/" + info.getFiles()[0].getPath();
                writeFile(path, out);
            } else {
                ZipOutputStream zipOut = new ZipOutputStream(out);
                zipOut.setLevel(0);
                zipOut.setComment("name: " + info.getName() + info.getComment() != null ? "\n comment:" + info.getComment() : "" + info.getTag() != null ? "\n tag:" + info.getTag() : "");
                FileInfo[] files = info.getFiles();
                char separator = rtorrent.getFileSeparatoChar();
                String remoteBaseDir = info.getBaseDirectory();
                String remoteBasePath = remoteBaseDir + separator + dir;
                for (FileInfo fileInfo : files) {
                    String remotePath = '\'' + remoteBasePath + separator + fileInfo.getPath() + '\'';
                    ZipEntry entry = new ZipEntry(dir + "\\" + fileInfo.getPath().replace(separator, '\\'));
                    zipOut.putNextEntry(entry);
                    writeFile(remotePath, zipOut);
                }
                zipOut.finish();
            }
        }

        private void writeFile(String path, OutputStream out) throws IOException {
            byte data[] = new byte[BUFFER_SIZE];
            if (scp != null) {
                scp.get(path, out);
            } else {
                int count;
                InputStream is = new FileInputStream(new File(path));
                while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
            }
        }
    }

    private class ScpCopy extends Thread {

        private final RTorrent rtorrent;

        private final String hash;

        private final String localPath;

        private Throwable error = null;

        private boolean finished;

        ScpCopy(String hash, String localPath, RTorrent rtorrent) {
            this.localPath = localPath;
            this.rtorrent = rtorrent;
            this.hash = hash;
            finished = false;
        }

        @Override
        public void run() {
            finished = false;
            try {
                TorrentInfo info = rtorrent.getTorrentInfo(hash);
                FileInfo[] files = info.getFiles();
                String remoteDir = info.getDirectory();
                String remoteBaseDir = info.getBaseDirectory();
                String remoteBasePath = remoteBaseDir;
                File localDir;
                if (remoteDir == null) localDir = new File(localPath); else {
                    localDir = new File(localPath, remoteDir);
                    remoteBasePath += "/" + remoteDir;
                    localDir.mkdirs();
                }
                String sLocalDir = localDir.getAbsolutePath();
                Connection connection = SshFactory.getConnection(rtorrent.getBoxConfig().getSsh());
                SCPClient scp = connection.createSCPClient();
                char separator = rtorrent.getFileSeparatoChar();
                for (FileInfo fileInfo : files) {
                    String filePath = fileInfo.getPath();
                    String remotePath = '\'' + remoteBasePath + separator + filePath + '\'';
                    String localPath = sLocalDir;
                    int p = filePath.lastIndexOf(separator);
                    if (p != -1) {
                        localPath += (localPath.charAt(localPath.length() - 1) == File.separatorChar ? "" : File.separatorChar) + filePath.substring(0, p).replace(separator, File.separatorChar);
                        (new File(localPath)).mkdirs();
                    }
                    scp.get(remotePath, localPath);
                }
                finished = true;
                SystemMessages.info("Secure copy completed: " + info.getName());
                jobs.remove(this);
            } catch (Throwable e) {
                SystemMessages.error("Secure copy failed: " + e.getMessage());
                log.error(e.getMessage(), e);
                error = e;
            }
        }

        Throwable getError() {
            return error;
        }

        boolean isFinished() {
            return finished;
        }
    }
}
