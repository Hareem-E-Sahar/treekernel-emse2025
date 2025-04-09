package com.scatter.useCodeGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.collections.ExtendedProperties;
import com.scatter.model.metamodel.Classe;

public class FileHelper {

    public static void copyDirectory(File srcPath, File dstPath) throws IOException {
        if (srcPath.getName().contains("svn") || srcPath.getName().contains("backup")) return;
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdirs();
            }
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
            }
        } else {
            if (!srcPath.exists()) {
                System.out.println("Arquivo ou diret�rio n�o existe.");
                System.exit(0);
            } else {
                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
        System.out.println("Directory copied.");
    }

    /**
	 * Copia arquivos fisicamente
	 * 
	 * @param inFile
	 *            Caminho completo do arquivo de origem
	 * @param outFile
	 *            Caminho completo do arquivo de destino
	 * @param fileName
	 * @return true se a c�pia do arquivo for realizada com sucesso
	 */
    public static boolean copyFile(String inFile, String outFile) {
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer;
        boolean success = true;
        try {
            createDir(outFile.substring(0, outFile.lastIndexOf(System.getProperty("file.separator"))));
            is = new FileInputStream(inFile);
            os = new FileOutputStream(outFile);
            buffer = new byte[is.available()];
            is.read(buffer);
            os.write(buffer);
        } catch (IOException e) {
            success = false;
        } catch (OutOfMemoryError e) {
            success = false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            }
        }
        return success;
    }

    /**
	 * Copia arquivos fisicamente
	 * 
	 * @param inFile
	 *            Caminho completo do arquivo de origem
	 * @param outFile
	 *            Caminho completo do arquivo de destino
	 * @param fileName
	 * @return true se a c�pia do arquivo for realizada com sucesso
	 */
    public static boolean copyFile(String inFile, String outFile, String fileName) {
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer;
        boolean success = true;
        try {
            createDir(outFile);
            is = new FileInputStream(inFile + System.getProperty("file.separator") + fileName);
            os = new FileOutputStream(outFile + System.getProperty("file.separator") + fileName);
            buffer = new byte[is.available()];
            is.read(buffer);
            os.write(buffer);
        } catch (IOException e) {
            success = false;
        } catch (OutOfMemoryError e) {
            success = false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            }
        }
        return success;
    }

    public static void createDir(String path) throws IOException {
        File dir = new File(path);
        dir.mkdirs();
    }

    public static String createDir(String appPath, Package pack) throws IOException {
        File dir = new File(appPath + pack.getName().replaceAll("[.]", "/"));
        dir.mkdirs();
        return appPath + pack.getName().replaceAll("[.]", "/");
    }
}
