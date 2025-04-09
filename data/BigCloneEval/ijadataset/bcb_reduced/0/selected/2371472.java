package pt.utl.ist.lucene.utils;

import org.apache.log4j.Logger;
import java.io.*;
import java.util.HashSet;

/**
 * @author Jorge Machado
 * @date 17/Ago/2008
 * @see pt.utl.ist.lucene.utils
 */
public class Files {

    private static final Logger logger = Logger.getLogger(Files.class);

    private static Files instance;

    private Files() {
    }

    static {
        instance = new Files();
    }

    public static Files getInstance() {
        return instance;
    }

    /**
     * Delete a dir and all internal files and dir's
     *
     * @param startPath to delete
     * @return 0 if everything was deleted and -1 if something was not deleted
     */
    public static int delDirsE(String startPath) {
        int result = 0;
        File f = new File(startPath);
        if (!f.exists()) {
            return -1;
        } else {
            File aux;
            File[] files = f.listFiles();
            for (File file : files) {
                aux = file;
                if (aux.list() == null) {
                    logger.info("Deleting file: " + startPath + "/" + aux.getName());
                    if (!aux.delete()) {
                        result = -1;
                        logger.info("Can't deleting file: " + startPath + "/" + aux.getName());
                        aux.deleteOnExit();
                    }
                } else {
                    if (result >= 0) result = delDirsE(startPath + "/" + aux.getName()); else delDirsE(startPath + "/" + aux.getName());
                }
            }
            logger.warn("Deleting Dir: " + startPath + "/" + f.getName());
            if (!f.delete()) {
                logger.info("Can't deleting file: " + startPath);
                f.deleteOnExit();
                result = -1;
            }
        }
        return result;
    }

    public static boolean copyDirectory(File srcPath, File dstPath) throws IOException {
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }
            String files[] = srcPath.list();
            for (String file : files) {
                copyDirectory(new File(srcPath, file), new File(dstPath, file));
            }
        } else {
            if (!srcPath.exists()) {
                logger.error("File or directory does not exist.");
                return false;
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
        logger.info("Directory " + srcPath + " copied to " + dstPath);
        return true;
    }

    public HashSet<String> readWords(String path) throws IOException {
        InputStream file = getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file));
        String line;
        HashSet<String> set = new HashSet();
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0) {
                set.add(line);
            }
        }
        return set;
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) return ""; else if (fileName.length() - lastDot <= 1) return ""; else return fileName.substring(lastDot + 1);
    }

    public static String getFileWithoutExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) return fileName; else return fileName.substring(0, lastDot);
    }

    public static String getText(File file, String encoding) throws IOException {
        StringBuffer contentBuffer = new StringBuffer();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader is = new InputStreamReader(fileInputStream, encoding);
            BufferedReader reader = new BufferedReader(is);
            boolean more = true;
            int totalReaded = 0;
            while (more) {
                String line = reader.readLine();
                if (line == null) {
                    more = false;
                } else {
                    contentBuffer.append(line);
                    contentBuffer.append("\n");
                    totalReaded += line.length();
                }
            }
            fileInputStream.close();
            is.close();
            reader.close();
        } catch (IOException e) {
            throw e;
        }
        return contentBuffer.toString();
    }

    public static String getMaxText(File file, long maxLenght, String encoding) throws Exception {
        StringBuffer contentBuffer = new StringBuffer();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader is = new InputStreamReader(fileInputStream, encoding);
            BufferedReader reader = new BufferedReader(is);
            boolean more = true;
            int totalReaded = 0;
            while (more) {
                String line = reader.readLine();
                if (line == null) {
                    more = false;
                } else {
                    contentBuffer.append(line);
                    contentBuffer.append("\n");
                    totalReaded += line.length();
                    if (totalReaded > maxLenght) more = false;
                }
            }
            fileInputStream.close();
            is.close();
            reader.close();
        } catch (Exception e) {
            throw e;
        }
        return contentBuffer.toString();
    }

    public static String normalizePathSeparators(String path) {
        return path.replace("\\", File.separator).replace("/", File.separator);
    }
}
