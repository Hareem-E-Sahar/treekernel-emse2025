package com.core.util;

import java.util.*;
import java.io.*;
import java.util.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 文件操作辅助类
 * 
 */
public class FileUtil {

    private static Log log = LogFactory.getLog(FileUtil.class);

    /**
	 * 文件目录分隔符
	 */
    public static final String SEPRATOR = "/";

    public static final String UTF8 = "utf-8";

    /**
	 * 删除单个文件
	 * 
	 * @param fileName
	 *            删除文件完整路径与文件名
	 * @return
	 */
    public static boolean deleteFile(String fileName) {
        boolean ret = false;
        try {
            if (fileName == null || fileName.equals("")) {
                return ret;
            }
            File file = new File(fileName);
            ret = file.delete();
            file = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 删除多个文件
	 * 
	 * @param fileNames
	 *            删除文件完整路径与文件名列表
	 * @return
	 */
    public static boolean deleteFile(List<String> fileNames) {
        boolean ret = false;
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return ret;
            }
            File file = null;
            for (String fileName : fileNames) {
                file = new File(fileName);
                file.delete();
            }
            file = null;
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 删除目录下的全部文件
	 * 
	 * @param folder
	 *            删除文件目录完整路径
	 * @return
	 */
    public static boolean deleteFiles(String folder) {
        boolean ret = false;
        try {
            ret = deleteFiles(folder, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 删除目录下的文件，标志 flag 为真，将目录一并删除
	 * 
	 * @param folder
	 * @param flag
	 *            标志，true 将文件目录删除
	 * @return
	 */
    public static boolean deleteFiles(String folder, boolean flag) {
        boolean ret = false;
        try {
            if (folder == null || folder.equals("")) {
                return ret;
            }
            File directory = new File(folder);
            if (!directory.isDirectory()) {
                return ret;
            }
            String[] fileNames = directory.list();
            if (fileNames == null || fileNames.length <= 0) {
                return ret;
            }
            File file = null;
            for (String fileName : fileNames) {
                file = new File(folder + SEPRATOR + fileName);
                file.delete();
            }
            file = null;
            if (flag) {
                directory.delete();
            }
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 应用当前日期信息作为参数生成日期格式层次目录，返回参数无根目录路径
	 * 
	 * @param folder
	 * @return
	 */
    public static String createDirectoryAutoManage(String folder) {
        String ret = null;
        try {
            Calendar calendar = Calendar.getInstance();
            StringBuffer directory = new StringBuffer();
            directory.append(SEPRATOR).append(calendar.get(Calendar.YEAR)).append(SEPRATOR).append(calendar.get(Calendar.MONTH) + 1).append(SEPRATOR).append(calendar.get(Calendar.DAY_OF_MONTH));
            if (createDirectory(folder + directory.toString())) {
                ret = directory.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 通过目录信息生成目录
	 * 
	 * @param folder
	 * @return
	 */
    public static boolean createDirectory(String folder) {
        boolean ret = false;
        try {
            if (folder == null || folder.equals("")) {
                return ret;
            }
            File directory = new File(folder);
            if (directory.exists()) {
                return true;
            }
            directory.mkdirs();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 上传文件，在上传文件根目录下自动生成日期格式层级目录，并对文件进行重命名操作，返回参数无根目录路径
	 * 
	 * @param file
	 * @param folder
	 * @param fileName
	 * @return
	 */
    public static String uploadFileAutoManage(File file, String folder, String fileName) {
        String ret = null;
        try {
            ret = uploadFileAutoManage(file, folder, fileName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 上传文件，在上传文件根目录下自动生成日期格式层级目录，并对文件进行重命名操作，并限制上传文件的大小，返回参数无根目录路径
	 * 
	 * @param file
	 * @param folder
	 * @param fileName
	 * @param length
	 * @return
	 */
    public static String uploadFileAutoManage(File file, String folder, String fileName, long length) {
        String ret = null;
        try {
            if (file == null || fileName == null || fileName.equals("")) {
                return ret;
            }
            ret = createDirectoryAutoManage(folder);
            if (ret == null) {
                return ret;
            }
            fileName = renameFileName(fileName);
            if (fileName == null) {
                return null;
            }
            ret += SEPRATOR + fileName;
            if (length > 0 && file.length() > length) {
                return null;
            }
            if (!uploadFile(file, folder + ret)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 上传文件，若上传文件目录不存在，自动生成上传目录
	 * 
	 * @param file
	 * @param folder
	 * @param fileName
	 * @return
	 */
    public static boolean uploadFile(File file, String folder, String fileName) {
        boolean ret = false;
        try {
            ret = uploadFile(file, folder, fileName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 上传文件，若上传文件目录不存在，自动生成上传目录，并限制上传文件的大小
	 * 
	 * @param file
	 * @param folder
	 * @param fileName
	 * @param length
	 *            文件长度，单位为字节，例如：1K=1024 bytes
	 * @return
	 */
    public static boolean uploadFile(File file, String folder, String fileName, long length) {
        boolean ret = false;
        try {
            if (file == null || fileName == null || fileName.equals("")) {
                return ret;
            }
            if (!createDirectory(folder)) {
                return ret;
            }
            if (length > 0 && file.length() > length) {
                return ret;
            }
            ret = uploadFile(file, folder + SEPRATOR + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 上传文件至上传目录
	 * 
	 * @param file
	 * @param target
	 *            上传文件完整路径，包括文件名
	 * @return
	 */
    public static boolean uploadFile(File file, String target) {
        boolean ret = false;
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            if (file == null || target == null || target.equals("")) {
                return ret;
            }
            fileOutputStream = new FileOutputStream(target);
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
                fileOutputStream.close();
            } catch (Exception e) {
                fileInputStream = null;
                fileOutputStream = null;
            }
        }
        return ret;
    }

    /**
	 * 根据当前日期信息作为参数进行文件重命名操作
	 * 
	 * @param fileName
	 * @return
	 */
    public static String renameFileName(String fileName) {
        String ret = null;
        try {
            if (fileName == null || fileName.equals("")) {
                return ret;
            }
            StringBuffer tmpName = new StringBuffer();
            Calendar calendar = Calendar.getInstance();
            tmpName.append(calendar.get(Calendar.YEAR)).append(calendar.get(Calendar.MONTH)).append(calendar.get(Calendar.DAY_OF_MONTH)).append(calendar.get(Calendar.HOUR_OF_DAY)).append(calendar.get(Calendar.MINUTE)).append(calendar.get(Calendar.SECOND)).append(calendar.get(Calendar.MILLISECOND)).append(getExtension(fileName));
            ret = tmpName.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 获取文件名，不包括文件后缀名（即文件类型）以及文件目录前缀
	 * 
	 * @param fileName
	 * @return
	 */
    public static String getFileName(String fileName) {
        String ret = null;
        try {
            if (fileName == null || fileName.equals("")) {
                return ret;
            }
            int start = fileName.lastIndexOf("/");
            if (start == -1) {
                start = 0;
            }
            int position = fileName.lastIndexOf(".");
            if (position == -1) {
                position = 0;
            }
            ret = fileName.substring(start, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 获取文件名，不包括文件后缀名（即文件类型）
	 * 
	 * @param fileName
	 * @return
	 */
    public static String getDirectoryFileName(String fileName) {
        String ret = null;
        try {
            if (fileName == null || fileName.equals("")) {
                return ret;
            }
            int position = fileName.lastIndexOf(".");
            if (position == -1) {
                position = 0;
            }
            ret = fileName.substring(0, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 获取文件后缀名（即文件类型）
	 * 
	 * @param fileName
	 * @return
	 */
    public static String getExtension(String fileName) {
        String ret = null;
        try {
            if (fileName == null || fileName.equals("")) {
                return ret;
            }
            int position = fileName.lastIndexOf(".");
            if (position == -1) {
                position = 0;
            }
            ret = fileName.substring(position);
            ret = ret.toLowerCase();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    /**
	 * 写文件，将文件内容写入到根据文件名重命名文件中，上传至根据文件路径自动生成日期格式层次目录中
	 * 
	 * @param fileContent
	 *            文件内容
	 * @param folder
	 * @param fileName
	 * @return
	 */
    public static String writeFile(String fileContent, String folder, String fileName) {
        String ret = null;
        try {
            if (fileContent == null || folder == null || fileName == null) {
                return ret;
            }
            ret = createDirectoryAutoManage(folder);
            if (ret == null) {
                return ret;
            }
            fileName = renameFileName(fileName);
            if (fileName == null) {
                return ret;
            }
            ret += SEPRATOR + fileName;
            if (!writeFile(folder + ret, fileContent)) {
                ret = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 写入文件
	 * 
	 * @param directoryFileName
	 * @param fileContent
	 * @return
	 */
    public static boolean writeFile(String folderName, String fileContent) {
        boolean ret = false;
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new FileOutputStream(folderName));
            printWriter.println(fileContent);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        ret = true;
        return ret;
    }

    public static boolean writeFileEncoding(String folderName, String fileContent, String encoding) {
        boolean ret = false;
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(folderName), encoding));
            printWriter.println(fileContent);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        ret = true;
        return ret;
    }

    /**
	 * 获取文件的字节数组
	 * 
	 * @param file
	 * @return
	 */
    public static byte[] getBytesFromFile(File file) {
        byte[] ret = null;
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            if (file == null) {
                return ret;
            }
            fileInputStream = new FileInputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] tmpByte = new byte[1024];
            int n = -1;
            while ((n = fileInputStream.read(tmpByte)) != -1) {
                byteArrayOutputStream.write(tmpByte, 0, n);
            }
            ret = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return ret;
    }

    /**
	 * 将字节数组获取的文件写入到设置目录文件中
	 * 
	 * @param bytes
	 * @param folder
	 * @return
	 */
    public static File getFileFromBytes(byte[] bytes, String folder) {
        File ret = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (bytes == null || bytes.length <= 0 || folder == null || folder.equals("")) {
                return ret;
            }
            ret = new File(folder);
            fileOutputStream = new FileOutputStream(ret);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(bytes);
        } catch (Exception e) {
            ret = null;
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return ret;
    }

    /**
	 * 获取字节数组的InputStrean对象
	 * 
	 * @param bytes
	 * @return
	 */
    public static InputStream getInputStreamFromBytes(byte[] bytes) {
        InputStream ret = null;
        try {
            if (bytes == null || bytes.length <= 0) {
                return ret;
            }
            ret = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void main(String[] args) {
    }
}
