package com.android.email.mail.internet;

import com.android.email.Email;
import com.android.email.mail.Body;
import com.android.email.mail.BodyPart;
import com.android.email.mail.Message;
import com.android.email.mail.MessagingException;
import com.android.email.mail.Multipart;
import com.android.email.mail.Part;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.decoder.Base64InputStream;
import org.apache.james.mime4j.decoder.DecoderUtil;
import org.apache.james.mime4j.decoder.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.CharsetUtil;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeUtility {

    private static final Pattern PATTERN_CR_OR_LF = Pattern.compile("\r|\n");

    /**
     * Replace sequences of CRLF+WSP with WSP.  Tries to preserve original string
     * object whenever possible.
     */
    public static String unfold(String s) {
        if (s == null) {
            return null;
        }
        Matcher patternMatcher = PATTERN_CR_OR_LF.matcher(s);
        if (patternMatcher.find()) {
            patternMatcher.reset();
            s = patternMatcher.replaceAll("");
        }
        return s;
    }

    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        return DecoderUtil.decodeEncodedWords(s);
    }

    public static String unfoldAndDecode(String s) {
        return decode(unfold(s));
    }

    public static String foldAndEncode(String s) {
        return s;
    }

    /**
     * INTERIM version of foldAndEncode that will be used only by Subject: headers.
     * This is safer than implementing foldAndEncode() (see above) and risking unknown damage
     * to other headers.
     * 
     * TODO: Copy this code to foldAndEncode(), get rid of this function, confirm all working OK.
     * 
     * @param s original string to encode and fold
     * @param usedCharacters number of characters already used up by header name

     * @return the String ready to be transmitted
     */
    public static String foldAndEncode2(String s, int usedCharacters) {
        String encoded = EncoderUtil.encodeIfNecessary(s, EncoderUtil.Usage.TEXT_TOKEN, usedCharacters);
        return fold(encoded, usedCharacters);
    }

    /**
     * INTERIM:  From newer version of org.apache.james (but we don't want to import
     * the entire MimeUtil class).
     * 
     * Splits the specified string into a multiple-line representation with
     * lines no longer than 76 characters (because the line might contain
     * encoded words; see <a href='http://www.faqs.org/rfcs/rfc2047.html'>RFC
     * 2047</a> section 2). If the string contains non-whitespace sequences
     * longer than 76 characters a line break is inserted at the whitespace
     * character following the sequence resulting in a line longer than 76
     * characters.
     * 
     * @param s
     *            string to split.
     * @param usedCharacters
     *            number of characters already used up. Usually the number of
     *            characters for header field name plus colon and one space.
     * @return a multiple-line representation of the given string.
     */
    public static String fold(String s, int usedCharacters) {
        final int maxCharacters = 76;
        final int length = s.length();
        if (usedCharacters + length <= maxCharacters) return s;
        StringBuilder sb = new StringBuilder();
        int lastLineBreak = -usedCharacters;
        int wspIdx = indexOfWsp(s, 0);
        while (true) {
            if (wspIdx == length) {
                sb.append(s.substring(Math.max(0, lastLineBreak)));
                return sb.toString();
            }
            int nextWspIdx = indexOfWsp(s, wspIdx + 1);
            if (nextWspIdx - lastLineBreak > maxCharacters) {
                sb.append(s.substring(Math.max(0, lastLineBreak), wspIdx));
                sb.append("\r\n");
                lastLineBreak = wspIdx;
            }
            wspIdx = nextWspIdx;
        }
    }

    /**
     * INTERIM:  From newer version of org.apache.james (but we don't want to import
     * the entire MimeUtil class).
     * 
     * Search for whitespace.
     */
    private static int indexOfWsp(String s, int fromIndex) {
        final int len = s.length();
        for (int index = fromIndex; index < len; index++) {
            char c = s.charAt(index);
            if (c == ' ' || c == '\t') return index;
        }
        return len;
    }

    /**
     * Returns the named parameter of a header field. If name is null the first
     * parameter is returned, or if there are no additional parameters in the
     * field the entire field is returned. Otherwise the named parameter is
     * searched for in a case insensitive fashion and returned. If the parameter
     * cannot be found the method returns null.
     * 
     * TODO: quite inefficient with the inner trimming & splitting.
     * TODO: Also has a latent bug: uses "startsWith" to match the name, which can false-positive.
     * TODO: The doc says that for a null name you get the first param, but you get the header. 
     *    Should probably just fix the doc, but if other code assumes that behavior, fix the code.
     *
     * @param header
     * @param name
     * @return
     */
    public static String getHeaderParameter(String header, String name) {
        if (header == null) {
            return null;
        }
        String[] parts = unfold(header).split(";");
        if (name == null) {
            return parts[0].trim();
        }
        String lowerCaseName = name.toLowerCase();
        for (String part : parts) {
            if (part.trim().toLowerCase().startsWith(lowerCaseName)) {
                String parameter = part.split("=", 2)[1].trim();
                if (parameter.startsWith("\"") && parameter.endsWith("\"")) {
                    return parameter.substring(1, parameter.length() - 1);
                } else {
                    return parameter;
                }
            }
        }
        return null;
    }

    public static Part findFirstPartByMimeType(Part part, String mimeType) throws MessagingException {
        if (part.getBody() instanceof Multipart) {
            Multipart multipart = (Multipart) part.getBody();
            for (int i = 0, count = multipart.getCount(); i < count; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                Part ret = findFirstPartByMimeType(bodyPart, mimeType);
                if (ret != null) {
                    return ret;
                }
            }
        } else if (part.getMimeType().equalsIgnoreCase(mimeType)) {
            return part;
        }
        return null;
    }

    public static Part findPartByContentId(Part part, String contentId) throws Exception {
        if (part.getBody() instanceof Multipart) {
            Multipart multipart = (Multipart) part.getBody();
            for (int i = 0, count = multipart.getCount(); i < count; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                Part ret = findPartByContentId(bodyPart, contentId);
                if (ret != null) {
                    return ret;
                }
            }
        }
        String cid = part.getContentId();
        if (contentId.equals(cid)) {
            return part;
        }
        return null;
    }

    /**
     * Reads the Part's body and returns a String based on any charset conversion that needed
     * to be done.
     * @param part The part containing a body
     * @return a String containing the converted text in the body, or null if there was no text
     * or an error during conversion.
     */
    public static String getTextFromPart(Part part) {
        try {
            if (part != null && part.getBody() != null) {
                InputStream in = part.getBody().getInputStream();
                String mimeType = part.getMimeType();
                if (mimeType != null && MimeUtility.mimeTypeMatches(mimeType, "text/*")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(in, out);
                    in.close();
                    in = null;
                    String charset = getHeaderParameter(part.getContentType(), "charset");
                    if (charset != null) {
                        charset = CharsetUtil.toJavaCharset(charset);
                    }
                    if (charset == null) {
                        charset = "ASCII";
                    }
                    String result = out.toString(charset);
                    out.close();
                    return result;
                }
            }
        } catch (OutOfMemoryError oom) {
            Log.e(Email.LOG_TAG, "Unable to getTextFromPart " + oom.toString());
        } catch (Exception e) {
            Log.e(Email.LOG_TAG, "Unable to getTextFromPart " + e.toString());
        }
        return null;
    }

    /**
     * Returns true if the given mimeType matches the matchAgainst specification.  The comparison
     * ignores case and the matchAgainst string may include "*" for a wildcard (e.g. "image/*").
     * 
     * @param mimeType A MIME type to check.
     * @param matchAgainst A MIME type to check against. May include wildcards.
     * @return true if the mimeType matches
     */
    public static boolean mimeTypeMatches(String mimeType, String matchAgainst) {
        Pattern p = Pattern.compile(matchAgainst.replaceAll("\\*", "\\.\\*"), Pattern.CASE_INSENSITIVE);
        return p.matcher(mimeType).matches();
    }

    /**
     * Returns true if the given mimeType matches any of the matchAgainst specifications.  The 
     * comparison ignores case and the matchAgainst strings may include "*" for a wildcard 
     * (e.g. "image/*").
     * 
     * @param mimeType A MIME type to check.
     * @param matchAgainst An array of MIME types to check against. May include wildcards.
     * @return true if the mimeType matches any of the matchAgainst strings
     */
    public static boolean mimeTypeMatches(String mimeType, String[] matchAgainst) {
        for (String matchType : matchAgainst) {
            if (mimeTypeMatches(mimeType, matchType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes any content transfer encoding from the stream and returns a Body.
     */
    public static Body decodeBody(InputStream in, String contentTransferEncoding) throws IOException {
        if (contentTransferEncoding != null) {
            contentTransferEncoding = MimeUtility.getHeaderParameter(contentTransferEncoding, null);
            if ("quoted-printable".equalsIgnoreCase(contentTransferEncoding)) {
                in = new QuotedPrintableInputStream(in);
            } else if ("base64".equalsIgnoreCase(contentTransferEncoding)) {
                in = new Base64InputStream(in);
            }
        }
        BinaryTempFileBody tempBody = new BinaryTempFileBody();
        OutputStream out = tempBody.getOutputStream();
        IOUtils.copy(in, out);
        out.close();
        return tempBody;
    }

    /**
     * An unfortunately named method that makes decisions about a Part (usually a Message)
     * as to which of it's children will be "viewable" and which will be attachments.
     * The method recursively sorts the viewables and attachments into seperate
     * lists for further processing.
     * @param part
     * @param viewables
     * @param attachments
     * @throws MessagingException
     */
    public static void collectParts(Part part, ArrayList<Part> viewables, ArrayList<Part> attachments) throws MessagingException {
        String disposition = part.getDisposition();
        String dispositionType = null;
        String dispositionFilename = null;
        if (disposition != null) {
            dispositionType = MimeUtility.getHeaderParameter(disposition, null);
            dispositionFilename = MimeUtility.getHeaderParameter(disposition, "filename");
        }
        boolean attachment = ("attachment".equalsIgnoreCase(dispositionType)) || (dispositionFilename != null) && (!"inline".equalsIgnoreCase(dispositionType));
        if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart) part.getBody();
            for (int i = 0; i < mp.getCount(); i++) {
                collectParts(mp.getBodyPart(i), viewables, attachments);
            }
        } else if (part.getBody() instanceof Message) {
            Message message = (Message) part.getBody();
            collectParts(message, viewables, attachments);
        } else if ((!attachment) && (part.getMimeType().equalsIgnoreCase("text/html"))) {
            viewables.add(part);
        } else if ((!attachment) && (part.getMimeType().equalsIgnoreCase("text/plain"))) {
            viewables.add(part);
        } else {
            attachments.add(part);
        }
    }
}
