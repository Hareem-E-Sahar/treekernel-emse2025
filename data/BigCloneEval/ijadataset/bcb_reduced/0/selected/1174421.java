package net.sf.immc.util.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MD5工具类
 * 
 * @author <b>oxidy</b>, Copyright &#169; 2007-2010
 * @version 0.1, 2009/12/22
 */
public class MD5Utils {

    private static Logger logger = LoggerFactory.getLogger(MD5Utils.class);

    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    protected static MessageDigest messagedigest = null;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            logger.error(MD5Utils.class.getName() + "初始化失败，MessageDigest不支持UtilsMD5。");
            nsaex.printStackTrace();
        }
    }

    /**
     * 生成字符串的md5校验值
     * 
     * @param str
     *            要生成md5校验值的字符串
     * @return md5校验值
     */
    public static String getMD5String(String str) {
        return getMD5String(str.getBytes());
    }

    /**
     * 判断字符串sourceStr的md5校验值是否与一个已知的md5校验值md5Str相匹配
     * 
     * @param sourceStr
     *            要校验的字符串
     * @param md5Str
     *            已知的md5校验值
     * @return
     */
    public static boolean checkMD5(String sourceStr, String md5Str) {
        String s = getMD5String(sourceStr);
        return s.equals(md5Str);
    }

    /**
     * 生成文件的md5校验值
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException {
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, numRead);
        }
        fis.close();
        return bufferToHex(messagedigest.digest());
    }

    /**
     * JDK1.4中不支持以MappedByteBuffer类型为参数update方法，并且网上有讨论要慎用MappedByteBuffer， 原因是当使用 FileChannel.map 方法时，MappedByteBuffer
     * 已经在系统内占用了一个句柄， 而使用 FileChannel.close 方法是无法释放这个句柄的，且FileChannel有没有提供类似 unmap 的方法， 因此会出现无法删除文件的情况。 不推荐使用
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String_old(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        messagedigest.update(byteBuffer);
        return bufferToHex(messagedigest.digest());
    }

    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    /**
     * main方法测试
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        long begin = System.currentTimeMillis();
        File file = new File("C:/12345.txt");
        String md5 = getFileMD5String(file);
        long end = System.currentTimeMillis();
        System.out.println("md5:" + md5 + " time:" + ((end - begin) / 1000) + "s");
    }
}
