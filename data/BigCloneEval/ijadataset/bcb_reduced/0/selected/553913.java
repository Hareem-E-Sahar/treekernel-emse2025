package org.retro.gis;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The Jar Loader system is used for loading bot modules, any
 * jar should work as long as the PircBotInterface is included.
 *
 * @author Berlin Brown
 * @see PircBotInterface 
 *
 */
public class DefaultJarLoader {

    private static String _endl = "\n";

    private String mainFileName = null;

    private String mainRootDir = null;

    private DefaultClassLoader classLoader = null;

    public DefaultJarLoader() {
        classLoader = null;
        mainRootDir = null;
        mainFileName = null;
    }

    public void setupLoader(String rootDir, String _jarfile) throws IOException {
        mainRootDir = rootDir;
        mainFileName = _jarfile;
        File _chk = new File(mainRootDir + System.getProperty("file.separator") + mainFileName);
        if (!_chk.exists()) throw new IOException(" [ Bot-Error ]: The JAR File doesnot exist, make sure you have the right directory.");
    }

    public Class getClass(String cName) throws ClassNotFoundException {
        return classLoader.loadClass(cName);
    }

    public void load() throws IOException {
        ZipInputStream _zip = null;
        InputStream _inStream = null;
        BufferedInputStream _bufStream = null;
        byte[] _buf = new byte[1024];
        boolean isJarEmpty = true;
        try {
            ZipEntry zipEntry = null;
            _inStream = new FileInputStream(mainRootDir + System.getProperty("file.separator") + mainFileName);
            _bufStream = new BufferedInputStream(_inStream);
            _zip = new ZipInputStream(_bufStream);
            classLoader = new DefaultClassLoader();
            System.out.println("......................................................");
            System.out.println(" | " + "[ JarLoader ] - Current Class List");
            while ((zipEntry = _zip.getNextEntry()) != null) {
                isJarEmpty = false;
                String _name = zipEntry.getName();
                String chkClass = null;
                if (_name == null) {
                } else {
                    int nLen = _name.length();
                    if (nLen > 6) {
                        chkClass = _name.substring((nLen - 5), nLen);
                        if (chkClass.equalsIgnoreCase("class")) {
                            System.out.println(" | " + "[jar-loader] " + _name);
                            ByteArrayOutputStream _byteStream = new ByteArrayOutputStream();
                            int byteCount = -1;
                            while ((byteCount = _zip.read(_buf)) != -1) {
                                _byteStream.write(_buf, 0, byteCount);
                            }
                            byte[] _fullClassBytes = _byteStream.toByteArray();
                            _name = _name.substring(0, _name.length() - 6);
                            String finClassName = _name.replace('/', '.');
                            classLoader.getByteCode(finClassName, _fullClassBytes);
                            classLoader.saveInternalList(finClassName);
                        }
                    }
                }
            }
            System.out.println("......................................................");
            if (classLoader != null) {
                classLoader.runDefineClasses();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(" [ Bot-Error ]: Error occurred while loading the JAR file; \n >>> Root-Dir: {" + mainRootDir + "} \n >>> " + mainFileName + "; " + e.getMessage());
        } finally {
            if (_zip != null) {
                try {
                    _zip.close();
                } catch (Exception e) {
                }
            }
        }
        if (isJarEmpty) {
            throw new IOException(" [ Bot-Error ]: This file is an invalid JAR file. ");
        }
    }
}
