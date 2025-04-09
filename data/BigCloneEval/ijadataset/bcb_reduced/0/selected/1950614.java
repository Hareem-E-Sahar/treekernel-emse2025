package net.jetrix.tools.patcher;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

/**
 * Jetrix Update - downloads new files from the patch server.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 588 $, $Date: 2005-01-12 13:56:30 -0500 (Wed, 12 Jan 2005) $
 */
public class JetrixUpdate {

    private List<String> update = new ArrayList<String>();

    private String basedir = "http://tetrinet.lfjr.net/jetrix/autoupdate/";

    private String newsFileName = "news.txt";

    private boolean downloadFailed = false;

    private boolean displayNews = false;

    public static void main(String[] argv) throws Exception {
        JetrixUpdate jetrixUpdate = new JetrixUpdate();
        jetrixUpdate.run();
    }

    public void run() throws Exception {
        getUpdate();
        for (int i = 0; i < update.size(); i++) {
            StringTokenizer st = new StringTokenizer(update.get(i), " \t");
            String fileName = st.nextToken();
            long chksum = Long.parseLong(st.nextToken());
            long localSum = getFileCRC32(fileName);
            if (chksum != localSum) {
                downloadFile(fileName, chksum);
                if (fileName.equals(newsFileName)) {
                    displayNews = true;
                }
            }
        }
        if (displayNews) {
            displayNews();
        }
        if (downloadFailed) {
            System.out.println("\nDownload failed. Please run again this update.\nContact the update server administrator if the problem persists.");
        } else {
            System.out.println("\nUpdate completed");
        }
    }

    private void getUpdate() throws IOException {
        URL updateList = new URL(basedir + "update.crc");
        System.out.println("Connecting to update server...");
        HttpURLConnection conn = (HttpURLConnection) updateList.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        System.out.println("Reading update file...");
        String line = br.readLine();
        while (line != null) {
            update.add(line);
            line = br.readLine();
        }
    }

    private void downloadFile(String filename, long remoteFileCRC) throws IOException {
        URL updateList = new URL(basedir + filename.replace('\\', '/'));
        HttpURLConnection conn = (HttpURLConnection) updateList.openConnection();
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
        File localFile = new File(filename + ".new");
        File parent = localFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(localFile);
        System.out.print("Receiving " + filename);
        int b = bis.read();
        while (b != -1) {
            fos.write(b);
            b = bis.read();
        }
        fos.close();
        bis.close();
        long downloadedFileCRC = getFileCRC32(localFile);
        String blank = new String(new char[50 - filename.length()]);
        if (downloadedFileCRC != remoteFileCRC) {
            System.out.println(blank + "  [FAILED]");
            downloadFailed = true;
            localFile.delete();
        } else {
            System.out.println(blank + "  [  OK  ]");
            localFile.renameTo(new File(filename));
        }
    }

    /**
     * Return the CRC32 value of the specified file.
     */
    private long getFileCRC32(File f) throws IOException {
        if (f.exists() && f.isFile()) {
            FileInputStream fis = new FileInputStream(f);
            CRC32 check = new CRC32();
            int b = fis.read();
            while (b != -1) {
                b = fis.read();
                check.update(b);
            }
            fis.close();
            return check.getValue();
        } else {
            return 0;
        }
    }

    /**
     * Return the CRC32 value of the specified file.
     */
    private long getFileCRC32(String filename) throws IOException {
        File f = new File(filename);
        return getFileCRC32(f);
    }

    /**
     * Display the content of the news file.
     */
    private void displayNews() throws IOException {
        System.out.println("\n");
        BufferedReader br = new BufferedReader(new FileReader(newsFileName));
        String line = br.readLine();
        while (line != null) {
            System.out.println(line);
            line = br.readLine();
        }
        br.close();
    }
}
