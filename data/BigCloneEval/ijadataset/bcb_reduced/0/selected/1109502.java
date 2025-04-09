package org.wits.writer;

import org.wits.writer.solbookwriter.SolChapterWriter;
import org.wits.writer.solbookwriter.SolBookWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.wits.WITSInstance;
import org.wits.WITSProperties;
import org.wits.writer.docbookwriter.DocBookWriter;
import org.wits.writer.docbookwriter.DocChapterWriter;

/**
 *
 * @author FJ
 */
public class WITSFileWriter {

    private File chapterPath = null;

    private File bookPath = null;

    private File debugPath = null;

    private WITSInstance witsInstance = null;

    private WITSProperties props = null;

    /**
     *
     * @param chapterPath
     * @param bookPath
     * @param debugPath
     */
    public WITSFileWriter(WITSInstance witsInstance, File chapterPath, File bookPath, File debugPath, WITSProperties props) {
        this.witsInstance = witsInstance;
        this.chapterPath = chapterPath;
        this.bookPath = bookPath;
        this.debugPath = debugPath;
        this.props = props;
    }

    /**
     *
     * @param debugString
     * @param error
     */
    public void writeErrorInfo(String debugString, String error) {
        try {
            FileWriter fw = new FileWriter(debugPath);
            fw.write(debugString + "\r\n\r\n");
            fw.write(error);
            fw.flush();
            fw.close();
            System.out.println("Writing debug info. to " + debugPath.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error while writing the SGML to: " + debugPath + ". PLease check the path/permission.");
        }
    }

    /**
     *
     * @param debugString
     */
    public void writeDebuggerOutput(String debugString) {
        try {
            FileWriter fw = new FileWriter(debugPath);
            fw.write(debugString);
            fw.flush();
            fw.close();
            System.out.println("Writing debug info. to " + debugPath.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error while writing the SGML to: " + debugPath + ". PLease check the path/permission.");
        }
    }

    /**
     *
     * @param cleanSGML
     */
    public void writeChapterOutput(ZipOutputStream outputStream, String cleanSGML) {
        boolean isCompressedOutput = false;
        if (outputStream != null) {
            isCompressedOutput = true;
        }
        if (isCompressedOutput) {
            if (witsInstance.getOutputType().equals("solbook")) {
                SolChapterWriter cWriter = new SolChapterWriter(cleanSGML, props);
                ZipEntry entry = new ZipEntry(chapterPath.getName());
                try {
                    outputStream.putNextEntry(entry);
                    outputStream.write(cWriter.getChapterBody().getBytes());
                    outputStream.closeEntry();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (witsInstance.getOutputType().equals("docbook")) {
                DocChapterWriter cWriter = new DocChapterWriter(cleanSGML, props);
                ZipEntry entry = new ZipEntry(chapterPath.getName());
                try {
                    outputStream.putNextEntry(entry);
                    outputStream.write(cWriter.getChapterBody().getBytes());
                    outputStream.closeEntry();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            try {
                FileWriter fw = new FileWriter(chapterPath);
                if (witsInstance.getOutputType().equals("solbook")) {
                    SolChapterWriter cWriter = new SolChapterWriter(cleanSGML, props);
                    fw.write(cWriter.getChapterBody());
                    fw.flush();
                    fw.close();
                }
                if (witsInstance.getOutputType().equals("docbook")) {
                    DocChapterWriter cWriter = new DocChapterWriter(cleanSGML, props);
                    fw.write(cWriter.getChapterBody());
                    fw.flush();
                    fw.close();
                }
            } catch (IOException e) {
                System.out.println("Error while writing to: " + chapterPath + ". PLease check the path/permission.");
            }
        }
    }

    /**
     *
     * @param cleanSGML
     */
    public void writeBookOutput(ZipOutputStream outputStream, String cleanSGML) {
        boolean isCompressedOutput = false;
        if (outputStream != null) {
            isCompressedOutput = true;
        }
        try {
            FileWriter fw = new FileWriter(bookPath);
            if (isCompressedOutput) {
                if (witsInstance.getOutputType().equals("solbook")) {
                    SolBookWriter cWriter = new SolBookWriter(witsInstance, cleanSGML, props);
                    ZipEntry entry = new ZipEntry(bookPath.getName());
                    try {
                        outputStream.putNextEntry(entry);
                        outputStream.write(cWriter.getPartialBookBody().getBytes());
                        outputStream.closeEntry();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                if (witsInstance.getOutputType().equals("docbook")) {
                    DocBookWriter cWriter = new DocBookWriter(cleanSGML, props);
                    ZipEntry entry = new ZipEntry(bookPath.getName());
                    try {
                        outputStream.putNextEntry(entry);
                        outputStream.write(cWriter.getPartialBookBody().getBytes());
                        outputStream.closeEntry();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                if (witsInstance.getOutputType().equals("solbook")) {
                    SolBookWriter bWriter = new SolBookWriter(witsInstance, cleanSGML, props);
                    fw.write(bWriter.getPartialBookBody());
                    fw.flush();
                    fw.close();
                }
                if (witsInstance.getOutputType().equals("docbook")) {
                    DocBookWriter bWriter = new DocBookWriter(cleanSGML, props);
                    fw.write(bWriter.getPartialBookBody());
                    fw.flush();
                    fw.close();
                }
            }
        } catch (IOException e) {
            System.out.println("Error while writing to: " + bookPath + ". PLease check the path/permission.");
        }
    }
}
