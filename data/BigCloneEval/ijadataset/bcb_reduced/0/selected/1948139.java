package org.compiere.utils;

import java.security.*;
import sun.security.provider.Sun;
import java.io.*;
import sun.misc.*;

/**
 * @author rlemeill
 *
 */
public class DigestOfFile {

    /**
     * @param mode ie MD5
     * @throws Exception
     */
    public DigestOfFile(String mode) throws Exception {
        digestAgent = MessageDigest.getInstance(mode, "SUN");
    }

    /**
     * @param file to hash
     * @return hash
     * @throws Exception
     */
    public synchronized byte[] digestAsByteArray(File file) throws Exception {
        digestAgent.reset();
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        for (int bytesRead = 0; (bytesRead = is.read(buffer)) >= 0; ) {
            digestAgent.update(buffer, 0, bytesRead);
        }
        is.close();
        byte[] digest = digestAgent.digest();
        return digest;
    }

    public synchronized byte[] digestAsByteArray(byte[] input) throws Exception {
        digestAgent.reset();
        byte[] digest = digestAgent.digest(input);
        return digest;
    }

    /**
     * @author rlemeill
     * @param file
     * @return hash (base64 encoded)
     * @throws Exception
     */
    public synchronized String digestAsBase64(File file) throws Exception {
        byte[] digest = digestAsByteArray(file);
        String digestAsBase64 = base64Encoder.encode(digest);
        return digestAsBase64;
    }

    /**
     * @param input
     * @return hash (base64 encoded)
     * @throws Exception
     */
    public synchronized String digestAsBase64(byte[] input) throws Exception {
        byte[] digest = digestAsByteArray(input);
        String digestAsBase64 = base64Encoder.encode(digest);
        return digestAsBase64;
    }

    private MessageDigest digestAgent;

    private BASE64Encoder base64Encoder = new BASE64Encoder();

    private byte[] buffer = new byte[4096];

    /**
     * @author rlemeill
     * @param args file
     */
    public static void main(String[] args) {
        try {
            java.security.Security.addProvider(new Sun());
            DigestOfFile md5DigestAgent = new DigestOfFile("MD5");
            for (int argIndex = 0; argIndex < args.length; argIndex++) {
                {
                    String base64Digest = md5DigestAgent.digestAsBase64(new File(args[argIndex]));
                    System.out.println("Base64 MD5 of " + args[argIndex] + " = [" + base64Digest + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param file1 first file to compare
     * @param file2 second file to compare
     * @return true if files are identic false otherwise
     */
    public static boolean md5localHashCompare(File file1, File file2) {
        String hashFile1;
        String hashFile2;
        java.security.Security.addProvider(new Sun());
        try {
            DigestOfFile md5DigestAgent = new DigestOfFile("MD5");
            hashFile1 = md5DigestAgent.digestAsBase64(file1);
            hashFile2 = md5DigestAgent.digestAsBase64(file2);
            return hashFile1.equals(hashFile2);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param file
     * @return md5 hash null if file is not found or other error
     */
    public static String GetLocalMD5Hash(File file) {
        String hash;
        java.security.Security.addProvider(new Sun());
        try {
            DigestOfFile md5DigestAgent = new DigestOfFile("MD5");
            hash = md5DigestAgent.digestAsBase64(file);
            return hash;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get md5 hash from byte[]
     * @param input
     * @return mdg hash string
     */
    public static String getMD5Hash(byte[] input) {
        String hash;
        java.security.Security.addProvider(new Sun());
        try {
            DigestOfFile md5DigestAgent = new DigestOfFile("MD5");
            hash = md5DigestAgent.digestAsBase64(input);
            return hash;
        } catch (Exception e) {
            return null;
        }
    }
}
