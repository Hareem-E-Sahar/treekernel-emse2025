package com.nonesole.persistence.tools;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Properties;

/**
 * File Tools <br>
 * The most different between this class and FileToolkit
 * is file path,in this class all the file path is URI,but
 * in FileToolkit the path is String.
 * @author JACK LEE
 * @version 1.0 - build in 2009-08-06
 */
public class FileHandler {

    /**
	 * LINE_SEPARATOR
	 * */
    public static final String LINE_SEPARATOR = "\r\n";

    private static FileHandler handler;

    static {
        handler = new FileHandler();
    }

    private FileHandler() {
    }

    /**
	 * Get FileHandler Object
	 * @return FileHandler
	 * */
    public static FileHandler getInstance() {
        return handler;
    }

    /**
	 * Create new file
	 * @param uri URI
	 * @return File
	 * @throws IOException 
	 * */
    public File createFile(URI uri) throws IOException {
        if (null == uri) throw new IOException("URI is null.");
        File file = new File(uri);
        if (!file.exists()) {
            createFolder(file.getParentFile().toURI());
            file.createNewFile();
        }
        if (!file.isFile()) throw new IOException(new StringBuilder().append("File \"").append(uri.toString()).append("\" is not exist.").toString());
        return file;
    }

    /**
	 * Read file to File Object
	 * @param uri URI
	 * @return File
	 * @throws IOException 
	 * */
    public File loadFile(URI uri) throws IOException {
        if (null == uri) throw new IOException("URI is null.");
        File file = new File(uri);
        if (!file.exists() || !file.isFile()) throw new IOException(new StringBuilder().append("File \"").append(uri.toString()).append("\" is not exist.").toString());
        return file;
    }

    /**
	 * Create new folder
	 * @param uri URI
	 * @return File(folder)
	 * @throws IOException 
	 * */
    public File createFolder(URI uri) throws IOException {
        if (null == uri) throw new IOException("URI is null.");
        File file = new File(uri);
        if (!file.exists()) file.mkdirs();
        if (!file.isDirectory()) throw new IOException(new StringBuilder().append("Folder \"").append(uri.toString()).append("\" is not exist.").toString());
        return file;
    }

    /**
	 * Read folder
	 * @param uri URI
	 * @return File(folder)
	 * @throws IOException 
	 * */
    public File loadFolder(URI uri) throws IOException {
        if (null == uri) throw new IOException("URI is null.");
        File file = new File(uri);
        if (!file.exists() || !file.isDirectory()) throw new IOException(new StringBuilder().append("Folder \"").append(uri.toString()).append("\" is not exist.").toString());
        return file;
    }

    /**
	 * Read file to FileInputStream
     * @param uri URI
     * @return FileInputStream
     * @throws IOException 
     * */
    public FileInputStream loadFileToInputStream(URI uri) throws IOException {
        return new FileInputStream(loadFile(uri));
    }

    /**
     * Read file to FileReader
     * @param uri URI
     * @return FileReader
     * @throws IOException 
     * */
    public FileReader loadFileToFileReader(URI uri) throws IOException {
        return new FileReader(loadFile(uri));
    }

    /**
     * Read file to InputStreamReader
     * @param uri URI
     * @param charset CharSet
     * @return InputStreamReader
     * @throws IOException 
     * */
    public InputStreamReader loadFileToInputStreamReader(URI uri, String charset) throws IOException {
        return new InputStreamReader(loadFileToInputStream(uri), charset);
    }

    /**
     * Read file to BufferedReader
     * @param uri URI
     * @param charset CharSet
     * @return BufferedReader
     * @throws IOException 
     * */
    public BufferedReader loadFileToBufferedReader(URI uri, String charset) throws IOException {
        return new BufferedReader(loadFileToInputStreamReader(uri, charset));
    }

    /**
     * Read file to LineNumberReader
     * @param uri URI
     * @param charset CharSet
     * @return LineNumberReader
     * @throws IOException 
     * */
    public LineNumberReader loadFileToLineNumberReader(URI uri, String charset) throws IOException {
        return new LineNumberReader(loadFileToInputStreamReader(uri, charset));
    }

    /**
     * Read file to serialization object
     * @param uri URL
     * @return Object
     * @throws IOException 
     * @throws ClassNotFoundException
     * */
    public Object loadFileToObject(URI uri) throws IOException, ClassNotFoundException {
        return new ObjectInputStream(loadFileToInputStream(uri)).readObject();
    }

    /**
     * Read file to Properties
     * @param uri URI
     * @return Properties
     * @throws IOException 
     * */
    public Properties loadFileToProperties(URI uri) throws IOException {
        Properties prop = new Properties();
        prop.load(loadFileToInputStream(uri));
        return prop;
    }

    /**
     * Read file to String
     * @param uri URI
     * @return String
     * @throws IOException 
     * */
    public String loadFileToString(URI uri, String charset) throws IOException {
        BufferedReader br = loadFileToBufferedReader(uri, charset);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while (null != (line = br.readLine())) sb.append(line).append(FileHandler.LINE_SEPARATOR);
        return sb.toString();
    }

    /**
	 * Read file to XML object
	 * @param uri URI
	 * @return XML Object
	 * @throws IOException 
	 * */
    public Object loadFileToXML(URI uri) throws IOException {
        return new XMLDecoder(new BufferedInputStream(loadFileToInputStream(uri))).readObject();
    }

    /**
	 * Read file to byte[]
     * @param uri URI
     * @return byte[]
     * @throws IOException 
     * */
    public byte[] loadFileToByteArray(URI uri) throws IOException {
        File file = loadFile(uri);
        FileInputStream fis = new FileInputStream(file);
        long size = file.length();
        byte[] bt = null;
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File size is greater than array upper limit(=Integer.MAX_VALUE).");
        } else {
            bt = new byte[(int) size];
            fis.read(bt);
        }
        return bt;
    }

    /**  
     * 
     * Read file to OutputStreamWriter<br>
     * @param uri URI  
     * @param append - true means append content at end of file
     * @return  OutputStreamWriter
     * @throws IOException 
     */
    public OutputStreamWriter loadFileToOutputStreamWriter(URI uri, boolean append, String charset) throws IOException {
        return new OutputStreamWriter(loadFileToOutputStream(uri, append), charset);
    }

    /**  
     * Read file to PrintWriter<br>
     * @param uri URI  
     * @param append - true means append content at end of file
     * @return  PrintWriter
     * @throws IOException 
     */
    public PrintWriter loadFileToPrintWriter(URI uri, boolean append, String charset) throws IOException {
        return new PrintWriter(loadFileToOutputStreamWriter(uri, append, charset));
    }

    /**  
     * Read file to BufferedWriter<br>
     * @param uri URI  
     * @param append - true means append content at the end of file
     * @return  BufferedWriter
     * @throws IOException 
     */
    public BufferedWriter loadFileToBufferedWriter(URI uri, boolean append, String charset) throws IOException {
        return new BufferedWriter(loadFileToOutputStreamWriter(uri, append, charset));
    }

    /**  
     * Read file to FileWriter<br>
     * @param uri URI  
     * @param append - true means append content at the end of file
     * @return  FileWriter
     * @throws IOException 
     */
    public FileWriter loadFileToFileWriter(URI uri, boolean append) throws IOException {
        return new FileWriter(loadFile(uri), append);
    }

    /**  
     * insert content at the end of file  
     * @param uri URI
     * @param content
     * @throws IOException 
     */
    public void insertContentByPrintWriter(URI uri, String content, String charset) throws IOException {
        PrintWriter out = loadFileToPrintWriter(uri, true, charset);
        out.write(content);
        out.close();
        out = null;
    }

    /** 
	 * insert content at the end of file   
     * @param uri  URI
     * @param content  
	 * @throws IOException 
     */
    public void insertContentByBufferWriter(URI uri, String content, String charset) throws IOException {
        BufferedWriter out = loadFileToBufferedWriter(uri, true, charset);
        out.write(content);
        out.close();
        out = null;
    }

    /**  
     * insert content at the end of file 
     * @param uri  URI
     * @param content  
     * @throws IOException 
     */
    public void insertContentByFileWriter(URI uri, String content) throws IOException {
        FileWriter out = loadFileToFileWriter(uri, true);
        out.write(content);
        out.close();
        out = null;
    }

    /**  
     * insert content at the end of file 
     * @param uri  URI  
     * @param content    
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void insertContentByRondomAccessFile(URI uri, String content) throws FileNotFoundException, IOException {
        RandomAccessFile randomFile = new RandomAccessFile(loadFile(uri), "rw");
        long fileLength = randomFile.length();
        randomFile.seek(fileLength);
        randomFile.writeBytes(content);
        randomFile.close();
        randomFile = null;
    }

    /**  
     * insert content at the end of file 
     * @param uri  URL  
     * @param b byte[]    
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void insertContentFromByteArray(URI uri, byte[] b) throws FileNotFoundException, IOException {
        ByteArrayOutputStream byteOut = getDefaultByteArrayOutputStream();
        FileOutputStream fOut = new FileOutputStream(createFile(uri));
        byteOut.write(b);
        byteOut.writeTo(fOut);
        byteOut.close();
        byteOut = null;
    }

    /**  
     * insert object at the end of file 
     * @param uri  URL  
     * @param obj Object   
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void insertContentFromObject(URI uri, Object obj) throws FileNotFoundException, IOException {
        ObjectOutputStream out = new ObjectOutputStream(loadFileToOutputStream(uri, true));
        out.writeObject(obj);
        out.close();
        out = null;
    }

    /**  
     * insert object at the end of file 
     * @param uri  URL  
     * @param in InputStream    
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void insertContentFromInputStream(URI uri, InputStream in) throws FileNotFoundException, IOException {
        ByteArrayOutputStream byteOut = getDefaultByteArrayOutputStream();
        byte[] b = new byte[1024];
        int byteSize = in.read(b);
        while (byteSize > 0) {
            byteOut.write(b, 0, byteSize);
            byteSize = in.read(b);
        }
        byteOut.writeTo(loadFileToOutputStream(uri, true));
        byteOut.close();
        byteOut = null;
    }

    /**  
     * Read file to OutputStream<br>
     * @param uri URI  
     * @param append - true means append content at the end of file
     * @return  OutputStream�����
     * @throws FileNotFoundException 
     * @throws IOException 
     */
    public OutputStream loadFileToOutputStream(URI uri, boolean append) throws FileNotFoundException, IOException {
        return new FileOutputStream(loadFile(uri), append);
    }

    /**  
     * Read file to DataOutputStream<br>
     * @param uri URI  
     * @param append - true means append content at the end of file
     * @return  OutputStream
     * @throws FileNotFoundException 
     * @throws IOException 
     */
    public DataOutputStream loadFileToDataOutputStream(URI uri, boolean append) throws FileNotFoundException, IOException {
        return new DataOutputStream(loadFileToOutputStream(uri, append));
    }

    /**  
     * Get default ByteArrayOutputStream object whose cache is 1024<br>
     * @return ByteArrayOutputStream 
     */
    public ByteArrayOutputStream getDefaultByteArrayOutputStream() {
        return new ByteArrayOutputStream(1024);
    }

    /**  
     * Save file 
     * @param uri  URI  
     * @param content  String 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void saveFileFromString(URI uri, String content) throws FileNotFoundException, IOException {
        RandomAccessFile randomFile = new RandomAccessFile(createFile(uri), "rw");
        randomFile.seek(0);
        randomFile.writeBytes(content);
        randomFile.close();
        randomFile = null;
    }

    /**  
     * Save file
     * @param uri  URL  
     * @param b  byte array  
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void saveFileFromByteArray(URI uri, byte[] b) throws FileNotFoundException, IOException {
        ByteArrayOutputStream byteOut = getDefaultByteArrayOutputStream();
        FileOutputStream fOut = new FileOutputStream(createFile(uri));
        byteOut.write(b);
        byteOut.writeTo(fOut);
        byteOut.close();
        byteOut = null;
    }

    /**  
     * Save file  
     * @param uri  URL  
     * @param obj Object    
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void saveFileFromObject(URI uri, Object obj) throws FileNotFoundException, IOException {
        createFile(uri);
        new ObjectOutputStream(loadFileToOutputStream(uri, false)).writeObject(obj);
    }

    /**  
     * Save file 
     * @param uri  URL  
     * @param in  InputStreamReader 
     * @param charset String
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void saveFileFromInputStreamReader(URI uri, InputStreamReader in, String charset) throws FileNotFoundException, IOException {
        OutputStreamWriter out = loadFileToOutputStreamWriter(createFile(uri).toURI(), false, charset);
        char[] c = new char[1024];
        int byteSize = in.read(c);
        while (true) {
            if (byteSize > 0) {
                out.write(c, 0, byteSize);
                byteSize = in.read(c);
            } else break;
        }
        out.close();
        out = null;
    }

    /**  
     * Save file
     * @param uri  URL  
     * @param in  InputStream
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void saveFileFromInputStream(URI uri, InputStream in) throws FileNotFoundException, IOException {
        ByteArrayOutputStream byteOut = getDefaultByteArrayOutputStream();
        OutputStream out = loadFileToOutputStream(createFile(uri).toURI(), false);
        byte[] b = new byte[1024];
        int byteSize = in.read(b);
        while (true) {
            if (byteSize > 0) {
                byteOut.write(b, 0, byteSize);
                byteSize = in.read(b);
            } else break;
        }
        byteOut.writeTo(out);
        byteOut.close();
        out.close();
        byteOut = null;
    }

    /**
	 * Save XML File
	 * @param obj JavaBean
	 * @param uri URI
     * @throws IOException 
	 * */
    public void saveXMLFileFromObject(Object obj, URI uri) throws IOException {
        createFile(uri);
        XMLEncoder e = new XMLEncoder(loadFileToOutputStream(uri, false));
        e.writeObject(obj);
        e.close();
        e = null;
    }

    /**
	 * Copy file
	 * @param sourceURI URI of source file
	 * @param targetURI URI of target file
     * @throws IOException 
	 * */
    public void copyFile(URI sourceURI, URI targetURI) throws IOException {
        saveFileFromInputStream(targetURI, loadFileToInputStream(sourceURI));
    }
}
