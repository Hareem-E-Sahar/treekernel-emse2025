package org.monet.kernel.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import org.monet.kernel.constants.Strings;
import org.monet.kernel.exceptions.FilesystemException;

public class AgentFilesystem {

    protected AgentFilesystem() {
    }

    public static String[] listDir(String sDirname) {
        FilenameFilter oFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        };
        return new File(sDirname).list(oFilter);
    }

    public static String[] listFiles(String sDirname) {
        File[] aFiles = null;
        ArrayList<String> alResult = new ArrayList<String>();
        String[] aResult;
        FilenameFilter oFilter;
        Integer iPos;
        oFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        };
        aFiles = new File(sDirname).listFiles(oFilter);
        for (iPos = 0; iPos < aFiles.length; iPos++) {
            if (aFiles[iPos].isDirectory()) continue;
            alResult.add(aFiles[iPos].getName());
        }
        aResult = new String[alResult.size()];
        return (String[]) alResult.toArray(aResult);
    }

    public static Boolean createDir(String sDirname) {
        return new File(sDirname).mkdir();
    }

    public static Boolean renameDir(String sSource, String sDestination) {
        File oDestination = new File(sDestination);
        return new File(sSource).renameTo(oDestination);
    }

    public static Boolean removeDir(String sDirname) {
        File oFile = new File(sDirname);
        return removeDir(oFile);
    }

    public static Boolean removeDir(File oFile) {
        if (oFile.exists()) {
            File[] aFiles = oFile.listFiles();
            for (int iPos = 0; iPos < aFiles.length; iPos++) {
                if (aFiles[iPos].isDirectory()) {
                    AgentFilesystem.removeDir(aFiles[iPos].getAbsolutePath());
                } else {
                    aFiles[iPos].delete();
                }
            }
        } else {
            return true;
        }
        return (oFile.delete());
    }

    public static Boolean copyDir(String sSource, String sDestination) {
        File oSource = new File(sSource);
        File oDestination = new File(sDestination);
        return copyDir(oSource, oDestination);
    }

    public static Boolean copyDir(File oSource, File oDestination) {
        try {
            if (oSource.exists()) {
                if (oSource.isDirectory()) {
                    if (!oDestination.exists()) {
                        oDestination.mkdir();
                    }
                    String[] children = oSource.list();
                    for (int i = 0; i < children.length; i++) {
                        copyDir(new File(oSource, children[i]), new File(oDestination, children[i]));
                    }
                } else {
                    InputStream in = new FileInputStream(oSource);
                    OutputStream out = new FileOutputStream(oDestination);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
                return true;
            }
        } catch (IOException oException) {
            throw new FilesystemException(oException.getMessage(), oSource.getName(), oException);
        }
        return false;
    }

    public static Boolean moveDir(File oSource, File oDestination) {
        if (oDestination.exists()) {
            boolean result = true;
            String[] children = oSource.list();
            for (int i = 0; i < children.length; i++) {
                File sourceChild = new File(oSource, children[i]);
                File destinationChild = new File(oDestination, children[i]);
                if (sourceChild.isDirectory()) {
                    if (!moveDir(sourceChild, destinationChild)) result = false;
                } else {
                    if (destinationChild.exists()) destinationChild.delete();
                    if (!sourceChild.renameTo(destinationChild)) result = false;
                }
            }
            return result;
        } else {
            return oSource.renameTo(oDestination);
        }
    }

    public static Boolean forceDir(String sDirname) {
        return new File(sDirname).mkdirs();
    }

    public static Boolean existFile(String sFilename) {
        return new File(sFilename).exists();
    }

    public static Boolean createFile(String sFilename) {
        try {
            new File(sFilename).createNewFile();
        } catch (IOException ex) {
            AgentLogger.getInstance().error(ex);
            return false;
        }
        return true;
    }

    public static Boolean copyFile(String source, String destination) {
        return copyFile(new File(source), new File(destination));
    }

    public static Boolean copyFile(File source, File destination) {
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            AgentLogger.getInstance().error(ex);
            return false;
        }
        return true;
    }

    public static Boolean renameFile(String sSource, String sDestination) {
        File oDestination = new File(sDestination);
        return new File(sSource).renameTo(oDestination);
    }

    public static Boolean removeFile(String sFilename) {
        return new File(sFilename).delete();
    }

    public static Reader getReader(String sFilename) {
        InputStreamReader oReader = null;
        try {
            oReader = new InputStreamReader(new FileInputStream(sFilename), "UTF-8");
        } catch (IOException oException) {
            throw new FilesystemException("Could not read file", sFilename, oException);
        }
        return oReader;
    }

    public static InputStream getInputStream(String sFilename) {
        FileInputStream oReader = null;
        try {
            oReader = new FileInputStream(sFilename);
        } catch (IOException oException) {
            throw new FilesystemException("Could not read file", sFilename, oException);
        }
        return oReader;
    }

    public static byte[] getBytesFromFile(String sFilename) {
        File oFile = new File(sFilename);
        InputStream oStream;
        long lLength;
        byte[] aBytes;
        int iOffset, iNumRead;
        try {
            oStream = new FileInputStream(oFile);
            lLength = oFile.length();
            if (lLength > Integer.MAX_VALUE) {
                throw new FilesystemException("File is too large", sFilename);
            }
            aBytes = new byte[(int) lLength];
            iOffset = 0;
            iNumRead = 0;
            while (iOffset < aBytes.length && (iNumRead = oStream.read(aBytes, iOffset, aBytes.length - iOffset)) >= 0) {
                iOffset += iNumRead;
            }
            if (iOffset < aBytes.length) {
                throw new FilesystemException("Could not completely read file", sFilename);
            }
            oStream.close();
        } catch (IOException oException) {
            throw new FilesystemException("Could not get bytes from file", sFilename, oException);
        }
        return aBytes;
    }

    public static String readFile(String sFilename, String Mode) {
        char[] sContent = null;
        try {
            File oFile = new File(sFilename);
            InputStreamReader oInput = new InputStreamReader(new FileInputStream(oFile), "UTF-8");
            oInput.read(sContent);
            oInput.close();
        } catch (IOException oException) {
            throw new FilesystemException("Could not read file", sFilename, oException);
        }
        return new String(sContent);
    }

    public static String readFile(String sFilename) {
        StringBuffer oContent = new StringBuffer();
        InputStreamReader oInputStreamReader;
        BufferedReader oBufferedReader;
        String sLine;
        try {
            oInputStreamReader = new InputStreamReader(new FileInputStream(sFilename), "UTF-8");
            oBufferedReader = new BufferedReader(oInputStreamReader);
            while ((sLine = oBufferedReader.readLine()) != null) {
                oContent.append(sLine + Strings.CRLF);
            }
            oInputStreamReader.close();
        } catch (IOException oException) {
            throw new FilesystemException("Could not read file", sFilename, oException);
        }
        return oContent.toString();
    }

    public static String getReaderContent(Reader oReader) {
        StringBuffer sbContent = new StringBuffer();
        BufferedReader oBufferedReader;
        String sLine;
        try {
            oBufferedReader = new BufferedReader(oReader);
            while ((sLine = oBufferedReader.readLine()) != null) {
                sbContent.append(sLine + Strings.CRLF);
            }
        } catch (IOException oException) {
            throw new FilesystemException("Could not get content from reader", null, oException);
        }
        return sbContent.toString();
    }

    public static Boolean writeFile(String sFilename, String sContent) {
        try {
            OutputStreamWriter oWriter = new OutputStreamWriter(new FileOutputStream(sFilename), "UTF-8");
            oWriter.write(sContent);
            oWriter.close();
        } catch (IOException oException) {
            throw new FilesystemException("Could not write file", sFilename, oException);
        }
        return true;
    }

    public static Writer getWriter(String sFilename) {
        OutputStreamWriter oWriter = null;
        try {
            oWriter = new OutputStreamWriter(new FileOutputStream(sFilename), "UTF-8");
        } catch (IOException oException) {
            throw new FilesystemException("Could not read file", sFilename, oException);
        }
        return oWriter;
    }

    public static OutputStream getOutputStream(String sFilename) {
        FileOutputStream oStream = null;
        try {
            oStream = new FileOutputStream(sFilename);
        } catch (IOException oException) {
            throw new FilesystemException("Could not read file", sFilename, oException);
        }
        return oStream;
    }

    public static Boolean appendToFile(String sFilename, String sContent) {
        try {
            FileWriter oFileWriter = new FileWriter(sFilename, true);
            oFileWriter.write(sContent);
            oFileWriter.close();
        } catch (IOException oException) {
            throw new FilesystemException("Could not write file", sFilename, oException);
        }
        return true;
    }
}
