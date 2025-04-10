package com.g2inc.scap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class OfficalCPEDictionaryGrabberTask {

    private static final String CONTENT_DIR = "data_feeds";

    private static final String CONTENT_URL = "http://static.nvd.nist.gov/feeds/xml/cpe/dictionary/official-cpe-dictionary_v2.2.xml";

    /**
	 * Attempt to grab the latest offical CPE dictionary file and store it locally
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        OfficalCPEDictionaryGrabberTask task = new OfficalCPEDictionaryGrabberTask();
        task.work(args);
    }

    private void work(String[] args) throws Exception {
        String dictLocation = CONTENT_URL;
        String cpeContentDirName = CONTENT_DIR;
        String fn = dictLocation.substring(dictLocation.lastIndexOf("/") + 1);
        File destFile = new File(cpeContentDirName + File.separator + fn);
        URL url = new URL(dictLocation);
        URLConnection conn = url.openConnection();
        conn.connect();
        long lmodifiedRemote = conn.getLastModified();
        boolean needToDownload = false;
        if (destFile.exists()) {
            System.out.println(destFile.getAbsolutePath() + " exists, check modification time");
            long lmodifiedLocal = destFile.lastModified();
            if (lmodifiedRemote > lmodifiedLocal) {
                System.out.println("Server file is newer, need to download");
                needToDownload = true;
            } else {
                System.out.println("Local version is newer, no need to download");
            }
        } else {
            System.out.println("Local version doesn't exist, need to download");
            needToDownload = true;
        }
        if (needToDownload) {
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buff = new byte[8192];
            int read = 0;
            while ((read = is.read(buff)) > 0) {
                fos.write(buff, 0, read);
            }
            fos.flush();
            fos.close();
            is.close();
        }
    }
}
