package com.webkreator.qlue.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import com.webkreator.qlue.PageNotFoundException;
import com.webkreator.qlue.TransactionContext;

/**
 * This utility class can send a file from the filesystem, either inline or as
 * an attachment.
 */
public class DownloadUtil {

    /**
	 * Sends file in HTTP response.
	 * 
	 * @param response
	 * @param f
	 * @throws Exception
	 */
    public static void sendFile(TransactionContext context, File f) throws Exception {
        sendFile(context, f, null, null, false);
    }

    /**
	 * Sends file in HTTP response, with C-D header control.
	 * 
	 * @param response
	 * @param f
	 * @throws Exception
	 */
    public static void sendFile(TransactionContext context, File f, String contentType, String name, boolean isAttachment) throws Exception {
        OutputStream os = null;
        BufferedInputStream bis = null;
        try {
            if (contentType == null) {
                int i = f.getName().lastIndexOf(".");
                if (i != -1) {
                    String suffix = f.getName().substring(i + 1);
                    contentType = MimeTypes.getMimeType(suffix);
                }
            }
            if (contentType != null) {
                context.response.setContentType(contentType);
            }
            if (name != null) {
                StringBuffer sb = new StringBuffer();
                CharacterIterator it = new StringCharacterIterator(name);
                for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
                    if (c < 0x20) {
                        throw new SecurityException("Invalid character in filename: " + c);
                    }
                    if ((c == '\\') || (c == '"')) {
                        sb.append('\\');
                        sb.append(c);
                    } else {
                        sb.append(c);
                    }
                }
                String escapedName = sb.toString();
                if (isAttachment) {
                    context.response.setHeader("Content-Disposition", "attachment; filename=\"" + escapedName + "\"");
                } else {
                    context.response.setHeader("Content-Disposition", "inline; filename=\"" + escapedName + "\"");
                }
            }
            String filename = f.getAbsolutePath();
            long length = f.length();
            long lastModified = f.lastModified();
            String eTag = constructHash(filename + "_" + length + "_" + lastModified);
            String ifNoneMatch = context.request.getHeader("If-None-Match");
            if ((ifNoneMatch != null) && ((ifNoneMatch.compareTo("*") == 0) || (ifNoneMatch.compareTo(eTag) == 0))) {
                context.response.setHeader("ETag", eTag);
                context.response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            long ifModifiedSince = context.request.getDateHeader("If-Modified-Since");
            if ((ifNoneMatch == null) && ((ifModifiedSince != -1) && (ifModifiedSince + 1000 > lastModified))) {
                context.response.setHeader("ETag", eTag);
                context.response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            if (length > Integer.MAX_VALUE) {
                throw new RuntimeException("File longer than Integer.MAX_VALUE");
            }
            context.response.setContentLength((int) length);
            context.response.setDateHeader("Last-Modified", lastModified);
            context.response.setHeader("ETag", eTag);
            os = context.response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(f));
            byte b[] = new byte[1024];
            while (bis.read(b) > 0) {
                os.write(b);
            }
        } catch (FileNotFoundException e) {
            throw new PageNotFoundException();
        } finally {
            if (os != null) {
                os.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }

    public static String constructHash(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64URLSafeString(md.digest(input.getBytes()));
    }
}
