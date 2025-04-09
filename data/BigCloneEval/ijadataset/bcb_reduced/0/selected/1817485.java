package org.monet.modelling.ide.library;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

public class Filesystem {

    public static final String NEW_LINE = System.getProperty("line.separator");

    public static final String FILE_SEPRATOR = System.getProperty("file.separator");

    public String[] listDir(String sDirname) {
        FilenameFilter oFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        };
        return new File(sDirname).list(oFilter);
    }

    public String[] listFiles(String sDirname) {
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
            alResult.add(aFiles[iPos].getAbsolutePath());
        }
        aResult = new String[alResult.size()];
        return (String[]) alResult.toArray(aResult);
    }

    public String[] listFiles(String sDirname, FilenameFilter filter) {
        File[] aFiles = new File[0];
        ArrayList<String> alResult = new ArrayList<String>();
        String[] aResult;
        Integer iPos;
        aFiles = new File(sDirname).listFiles(filter);
        if (aFiles == null) return new String[0];
        for (iPos = 0; iPos < aFiles.length; iPos++) {
            if (aFiles[iPos].isDirectory()) continue;
            alResult.add(aFiles[iPos].getAbsolutePath());
        }
        aResult = new String[alResult.size()];
        return (String[]) alResult.toArray(aResult);
    }

    public boolean createDir(String sDirname) {
        return new File(sDirname).mkdir();
    }

    public boolean renameDir(String sSource, String sDestination) {
        File oDestination = new File(sDestination);
        return new File(sSource).renameTo(oDestination);
    }

    public boolean removeDir(String sDirname) {
        File oFile = new File(sDirname);
        if (oFile.exists()) {
            File[] aFiles = oFile.listFiles();
            for (int iPos = 0; iPos < aFiles.length; iPos++) {
                if (aFiles[iPos].isDirectory()) {
                    removeDir(aFiles[iPos].getAbsolutePath());
                } else {
                    aFiles[iPos].delete();
                }
            }
        }
        return (oFile.delete());
    }

    public boolean copyDir(String sSource, String sDestination) {
        File oSource = new File(sSource);
        File oDestination = new File(sDestination);
        return copyDir(oSource, oDestination);
    }

    public boolean copyDir(File oSource, File oDestination) {
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
        } catch (IOException ex) {
            throw new FilesystemException(ex.getMessage() + " " + oSource.getName(), ex);
        }
        return false;
    }

    public boolean mkdirs(String sDirname) {
        return new File(sDirname).mkdirs();
    }

    public boolean existFile(String sFilename) {
        return new File(sFilename).exists();
    }

    public boolean createFile(String sFilename) {
        try {
            new File(sFilename).createNewFile();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public boolean renameFile(String sSource, String sDestination) {
        File oDestination = new File(sDestination);
        return new File(sSource).renameTo(oDestination);
    }

    public boolean removeFile(String sFilename) {
        return new File(sFilename).delete();
    }

    public Reader getReader(String sFilename) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(sFilename), "UTF-8");
        } catch (IOException ex) {
            throw new FilesystemException("Error, no se ha podido leer el archivo " + sFilename, ex);
        }
        return reader;
    }

    public InputStream getInputStream(String sFilename) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sFilename);
        } catch (IOException ex) {
            throw new FilesystemException("Error, no se ha podido leer el archivo " + sFilename, ex);
        }
        return inputStream;
    }

    public InputStream getInputStreamFrom(String content) {
        byte[] bytes;
        ByteArrayInputStream stream = null;
        try {
            bytes = content.getBytes("UTF-8");
            stream = new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stream;
    }

    public OutputStream getOutputStream(String sFilename) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(sFilename);
        } catch (IOException oException) {
            throw new FilesystemException("Could Error, not read file " + sFilename, oException);
        }
        return outputStream;
    }

    public byte[] getBytesFromFile(String sFilename) {
        File oFile = new File(sFilename);
        InputStream oStream;
        long lLength;
        byte[] aBytes;
        int iOffset, iNumRead;
        try {
            oStream = new FileInputStream(oFile);
            lLength = oFile.length();
            if (lLength > Integer.MAX_VALUE) {
                throw new FilesystemException("File is too large " + sFilename);
            }
            aBytes = new byte[(int) lLength];
            iOffset = 0;
            iNumRead = 0;
            while (iOffset < aBytes.length && (iNumRead = oStream.read(aBytes, iOffset, aBytes.length - iOffset)) >= 0) {
                iOffset += iNumRead;
            }
            if (iOffset < aBytes.length) {
                throw new FilesystemException("Error, no se ha podido completar la lectura del archivo " + sFilename);
            }
            oStream.close();
        } catch (IOException oException) {
            throw new FilesystemException("Error, no se ha podido completar la lectura del archivo " + sFilename, oException);
        }
        return aBytes;
    }

    public String readFile(String sFilename, String Mode) {
        char[] sContent = null;
        try {
            File oFile = new File(sFilename);
            InputStreamReader oInput = new InputStreamReader(new FileInputStream(oFile), "UTF-8");
            oInput.read(sContent);
            oInput.close();
        } catch (IOException oException) {
            throw new FilesystemException("Error, no se ha podido leer el archivo " + sFilename, oException);
        }
        return new String(sContent);
    }

    public String readFile(String sFilename) {
        StringBuffer oContent = new StringBuffer();
        InputStreamReader oInputStreamReader;
        BufferedReader oBufferedReader;
        String sLine;
        try {
            oInputStreamReader = new InputStreamReader(new FileInputStream(sFilename), "UTF-8");
            oBufferedReader = new BufferedReader(oInputStreamReader);
            while ((sLine = oBufferedReader.readLine()) != null) {
                oContent.append(sLine + NEW_LINE);
            }
            oInputStreamReader.close();
        } catch (IOException oException) {
            throw new FilesystemException("Error, no se ha podido leer el archivo " + sFilename, oException);
        }
        return oContent.toString();
    }

    public String getReaderContent(Reader oReader) {
        StringBuffer content = new StringBuffer();
        BufferedReader oBufferedReader;
        String sLine;
        try {
            oBufferedReader = new BufferedReader(oReader);
            while ((sLine = oBufferedReader.readLine()) != null) {
                content.append(sLine + NEW_LINE);
            }
        } catch (IOException oException) {
            throw new FilesystemException("Error, no se ha podido obtener el contenido del reader", null);
        }
        return content.toString();
    }

    public Writer getWriter(String sFilename) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(sFilename), "UTF-8");
        } catch (IOException oException) {
            throw new FilesystemException("Error, no se ha podido leer el archivo " + sFilename, oException);
        }
        return writer;
    }

    public void writeFile(String sFilename, String sContent) {
        try {
            OutputStreamWriter oWriter = new OutputStreamWriter(new FileOutputStream(sFilename), "UTF-8");
            oWriter.write(sContent);
            oWriter.close();
        } catch (IOException ex) {
            throw new FilesystemException("Error, no se ha podido escribir el archivo " + sFilename, ex);
        }
    }

    public void appendToFile(String sFilename, String sContent) {
        try {
            FileWriter oFileWriter = new FileWriter(sFilename, true);
            oFileWriter.write(sContent);
            oFileWriter.close();
        } catch (IOException ex) {
            throw new FilesystemException("Error, no se ha podido escribir el archivo " + sFilename, ex);
        }
    }

    public String getFilename(String filename) {
        int dotPosition = filename.lastIndexOf(".");
        return filename.substring(0, dotPosition - 1);
    }
}
