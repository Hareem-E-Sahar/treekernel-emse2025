package core.compress.zip;

import gui.MainSystray;
import gui.factory.ThreadUpdateProgressableGUI;
import gui.factory.components.AbstractProgressableGUI;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import resources.Messages;
import core.compress.CheckedOutputStreamSplited;
import core.crypto.CryptoOutputStream;
import core.util.IOutils;
import core.util.ThreadBase;

/**
 * This thread compress and split a file using "ZipCompressor" class<br/>
 * Create a Description file <code>FILE.OPEN</code> where the checksums of the
 * generated files are saved.
 *
 * @author Glauber Magalh�es Pires
 * @see core.compress.zip.ZipCompressor
 */
public class ThreadSplit extends ThreadBase {

    private File inFile;

    private File outputFolder;

    private long segmentSize;

    private List<Long> checksums;

    private String password;

    private AbstractProgressableGUI progressableGUI;

    /**
     * Create a thread to compress the file
     *
     * @param inFile The file to be compressed
     * @param outputFolder The folder where the files will be saved.
     * @param segmentSize The maximum size of each file (when reach the maximum
     *            a new file is created).
     * @param password The password of the file
     * @param progressableGUI the GUI that will be updated as the compression
     *            goes.
     */
    public ThreadSplit(File inFile, File outputFolder, long segmentSize, String password, AbstractProgressableGUI progressableGUI) {
        this.inFile = inFile;
        this.outputFolder = outputFolder;
        this.segmentSize = segmentSize;
        this.password = password;
        this.progressableGUI = progressableGUI;
    }

    public void run() {
        try {
            {
                ThreadUpdateProgressableGUI threadUpdateProgressableGUI = new ThreadUpdateProgressableGUI(progressableGUI);
                threadUpdateProgressableGUI.maximumProgress = inFile.length();
                threadUpdateProgressableGUI.start();
                this.checksums = new ArrayList<Long>();
                BufferedOutputStream bufferOut = new BufferedOutputStream(new CheckedOutputStreamSplited(inFile, outputFolder, segmentSize, checksums), IOutils.BUFFER_SIZE);
                ZipOutputStream out;
                if (password != null) out = new ZipOutputStream(new CryptoOutputStream(bufferOut, password)); else out = new ZipOutputStream(bufferOut);
                out.putNextEntry(new ZipEntry(inFile.getName()));
                InputStream in = new BufferedInputStream(new FileInputStream(inFile), IOutils.BUFFER_SIZE);
                byte[] buf = new byte[IOutils.BUFFER_SIZE];
                int len;
                while ((len = in.read(buf)) > 0 && keepAlive) {
                    out.write(buf, 0, len);
                    threadUpdateProgressableGUI.currentProgress += len;
                }
                in.close();
                threadUpdateProgressableGUI.keepAlive = false;
                out.finish();
                out.close();
                MainSystray.guiFactory.getMessageDisplayer().displayInfoMessage(Messages.message.getString("splitMerge.alert.splitCompleted"));
            }
            Properties properties = new Properties();
            properties.setProperty("segment.number", String.valueOf(checksums.size()));
            properties.setProperty("version", "3");
            for (int i = 0; i < checksums.size(); ) {
                Long adler32 = (Long) checksums.get(i++);
                properties.setProperty("segment." + i + ".adler32", adler32.toString());
            }
            if (password == null) properties.storeToXML(new FileOutputStream(new File(outputFolder, inFile.getName() + ".open")), "OpenP2M Splited File Descriptor (compatible with 7-zip decompressor)", "UTF-8"); else properties.storeToXML(new FileOutputStream(new File(outputFolder, inFile.getName() + ".open")), "OpenP2M Splited File Descriptor (NOT compatible with 7-zip decompressor, different encryptation)", "UTF-8");
        } catch (IOException ex) {
            MainSystray.guiFactory.getMessageDisplayer().showException(ex);
        } catch (Exception ex) {
            MainSystray.guiFactory.getMessageDisplayer().showException(ex);
        }
        progressableGUI.setButtonsStatus(true);
    }
}
