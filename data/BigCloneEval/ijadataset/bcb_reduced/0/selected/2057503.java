package org.das2.util.filesystem;

import org.das2.util.monitor.ProgressMonitor;
import org.das2.util.monitor.NullProgressMonitor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * This original FtpFileSystem implementation is based on Java's FTP client.  Note, Autoplot uses a third-party library because
 * of limitations of the Java implementation.  See DataSource project of Autoplot.  TODO: like what?
 * 
 * @author Jeremy
 */
public class FTPFileSystem extends WebFileSystem {

    FTPFileSystem(URI root) {
        super(root, localRoot(root));
    }

    public boolean isDirectory(String filename) {
        return filename.endsWith("/");
    }

    private String[] parseLsl(String dir, File listing) throws IOException {
        InputStream in = new FileInputStream(listing);
        BufferedReader reader = null;
        List result = new ArrayList(20);
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String aline = reader.readLine();
            boolean done = aline == null;
            String types = "d-";
            long bytesRead = 0;
            long totalSize;
            long sumSize = 0;
            while (!done) {
                bytesRead = bytesRead + aline.length() + 1;
                aline = aline.trim();
                if (aline.length() == 0) {
                    done = true;
                } else {
                    char type = aline.charAt(0);
                    if (type == 't') {
                        if (aline.indexOf("total") == 0) {
                        }
                    }
                    if (types.indexOf(type) != -1) {
                        int i = aline.lastIndexOf(' ');
                        String name = aline.substring(i + 1);
                        boolean isFolder = type == 'd';
                        result.add(name + (isFolder ? "/" : ""));
                    }
                    aline = reader.readLine();
                    done = aline == null;
                }
            }
        } finally {
            if (reader != null) reader.close();
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public String[] listDirectory(String directory) {
        directory = toCanonicalFolderName(directory);
        try {
            File f = new File(localRoot, directory);
            try {
                FileSystemUtil.maybeMkdirs(f);
            } catch (IOException ex) {
                throw new IllegalArgumentException("unable to mkdirs " + f);
            }
            File listing = new File(localRoot, directory + ".listing");
            if (!listing.canRead()) {
                File partFile = listing;
                downloadFile(directory, listing, partFile, new NullProgressMonitor());
            }
            listing.deleteOnExit();
            return parseLsl(directory, listing);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void downloadFile(String filename, java.io.File targetFile, File partFile, ProgressMonitor monitor) throws java.io.IOException {
        FileOutputStream out = null;
        InputStream is = null;
        try {
            filename = toCanonicalFilename(filename);
            URL url = new URL(root + filename.substring(1));
            URLConnection urlc = url.openConnection();
            int i = urlc.getContentLength();
            monitor.setTaskSize(i);
            out = new FileOutputStream(partFile);
            is = urlc.getInputStream();
            monitor.started();
            copyStream(is, out, monitor);
            monitor.finished();
            out.close();
            is.close();
            if (!partFile.renameTo(targetFile)) {
                throw new IllegalArgumentException("unable to rename " + partFile + " to " + targetFile);
            }
        } catch (IOException e) {
            if (out != null) out.close();
            if (is != null) is.close();
            if (partFile.exists() && !partFile.delete()) {
                throw new IllegalArgumentException("unable to delete " + partFile);
            }
            throw e;
        }
    }
}
