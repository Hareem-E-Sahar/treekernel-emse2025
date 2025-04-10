package net.sourceforge.plantuml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.sourceforge.plantuml.code.AsciiEncoder;

public class SignatureUtils {

    public static String getSignature(String s) {
        try {
            final AsciiEncoder coder = new AsciiEncoder();
            final MessageDigest msgDigest = MessageDigest.getInstance("MD5");
            msgDigest.update(s.getBytes("UTF-8"));
            final byte[] digest = msgDigest.digest();
            return coder.encode(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    public static String getSignatureWithoutImgSrc(String s) {
        s = getSignature(purge(s));
        return s;
    }

    public static String purge(String s) {
        final String regex = "(?i)\\<img\\s+src=\"(?:[^\"]+[/\\\\])?([^/\\\\\\d.]+)\\d*(\\.\\w+)\"/\\>";
        s = s.replaceAll(regex, "<img src=\"$1$2\"/>");
        final String regex2 = "(?i)image=\"(?:[^\"]+[/\\\\])?([^/\\\\\\d.]+)\\d*(\\.\\w+)\"";
        s = s.replaceAll(regex2, "image=\"$1$2\"");
        return s;
    }

    public static String getSignature(File f) throws IOException {
        try {
            final AsciiEncoder coder = new AsciiEncoder();
            final MessageDigest msgDigest = MessageDigest.getInstance("MD5");
            final FileInputStream is = new FileInputStream(f);
            int read = -1;
            while ((read = is.read()) != -1) {
                msgDigest.update((byte) read);
            }
            is.close();
            final byte[] digest = msgDigest.digest();
            return coder.encode(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }
}
