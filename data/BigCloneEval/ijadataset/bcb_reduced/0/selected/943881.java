package org.monet.modelling.agents;

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
import java.util.List;

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

    public static void listFiles(String dirIn, int ids, List<String> outFiles) {
        File dir = new File(dirIn);
        File[] files = dir.listFiles();
        files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String tmpFilename = dirIn.substring(ids) + "\\" + files[i].getName();
                outFiles.add(tmpFilename.substring(1));
            } else listFiles(files[i].getAbsolutePath(), files[i].getAbsolutePath().length(), outFiles);
        }
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
        if (oFile.exists()) {
            File[] aFiles = oFile.listFiles();
            for (int iPos = 0; iPos < aFiles.length; iPos++) {
                if (aFiles[iPos].isDirectory()) {
                    AgentFilesystem.removeDir(aFiles[iPos].getAbsolutePath());
                } else {
                    aFiles[iPos].delete();
                }
            }
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
        }
        return false;
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
        }
        return oReader;
    }

    public static InputStream getInputStream(String sFilename) {
        FileInputStream oReader = null;
        try {
            oReader = new FileInputStream(sFilename);
        } catch (IOException oException) {
        }
        return oReader;
    }

    public static byte[] getBytesFromFile(String sFilename) {
        File oFile = new File(sFilename);
        InputStream oStream;
        long lLength;
        byte[] aBytes = null;
        int iOffset, iNumRead;
        try {
            oStream = new FileInputStream(oFile);
            lLength = oFile.length();
            if (lLength > Integer.MAX_VALUE) {
            }
            aBytes = new byte[(int) lLength];
            iOffset = 0;
            iNumRead = 0;
            while (iOffset < aBytes.length && (iNumRead = oStream.read(aBytes, iOffset, aBytes.length - iOffset)) >= 0) {
                iOffset += iNumRead;
            }
            if (iOffset < aBytes.length) {
            }
            oStream.close();
        } catch (IOException oException) {
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
                oContent.append(sLine + "\r\n");
            }
            oInputStreamReader.close();
        } catch (IOException oException) {
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
                sbContent.append(sLine + "\r\n");
            }
        } catch (IOException oException) {
        }
        return sbContent.toString();
    }

    public static Boolean writeFile(String sFilename, String sContent) {
        try {
            OutputStreamWriter oWriter = new OutputStreamWriter(new FileOutputStream(sFilename), "UTF-8");
            oWriter.write(sContent);
            oWriter.close();
        } catch (IOException oException) {
            return false;
        }
        return true;
    }

    public static Writer getWriter(String sFilename) {
        OutputStreamWriter oWriter = null;
        try {
            oWriter = new OutputStreamWriter(new FileOutputStream(sFilename), "UTF-8");
        } catch (IOException oException) {
        }
        return oWriter;
    }

    public static OutputStream getOutputStream(String sFilename) {
        FileOutputStream oStream = null;
        try {
            oStream = new FileOutputStream(sFilename);
        } catch (IOException oException) {
        }
        return oStream;
    }

    public static Boolean appendToFile(String sFilename, String sContent) {
        try {
            FileWriter oFileWriter = new FileWriter(sFilename, true);
            oFileWriter.write(sContent);
            oFileWriter.close();
        } catch (IOException oException) {
        }
        return true;
    }

    public static void copyFile(String sSource, String sDestination) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File oSource = new File(sSource);
            File oDestination = new File(sDestination);
            in = new FileInputStream(oSource);
            File parentDir = oDestination.getParentFile();
            if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
            out = new FileOutputStream(oDestination);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException oException) {
            System.out.println("Error en la copia del archivo. Exception: " + oException.getMessage());
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
