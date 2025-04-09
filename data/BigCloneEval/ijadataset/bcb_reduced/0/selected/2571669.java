package myutils;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

/**
 *
 * @author Diez
 */
public class Misc {

    public static String readHtmlResourceAsString(URL resourceUrl) {
        String htmlText = "";
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(resourceUrl.openStream()));
            StringBuilder str = new StringBuilder();
            for (; ; ) {
                String inputLine = in.readLine();
                if (inputLine == null) break;
                str.append(inputLine);
                str.append('\n');
            }
            in.close();
            htmlText = str.toString();
            htmlText = htmlText.replace("&ldquo;", "&#x201c;");
            htmlText = htmlText.replace("&rdquo;", "&#x201d;");
        } catch (IOException ex) {
            throw ErrUtils.asRuntimeException(ex, "Error reading HTML resource: ");
        }
        return htmlText;
    }

    public static String removeCharFromStr(String str, char charToRemove) {
        char[] buffer = new char[str.length()];
        int destIndex = 0;
        for (int srcIndex = 0; srcIndex < str.length(); ++srcIndex) {
            char c = str.charAt(srcIndex);
            if (c != charToRemove) {
                buffer[destIndex] = c;
                ++destIndex;
            }
        }
        return new String(buffer, 0, destIndex);
    }

    public static boolean isPositiveInteger(String str) {
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public static boolean isInteger(String str) {
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (!Character.isDigit(c)) {
                if (i != 0) return false;
                if (c != '-') return false;
            }
        }
        return true;
    }

    public static long parsePositiveLong(String str, Locale locale) {
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(locale);
            if (!isPositiveInteger(str)) throw new RuntimeException("Invalid (positive) integer number.");
            Number result = nf.parse(str);
            if (result == null) throw new RuntimeException("Invalid (positive) integer number.");
            return result.longValue();
        } catch (Throwable ex) {
            throw ErrUtils.asRuntimeException(ex, String.format("Error parsing \"%s\": ", str));
        }
    }

    public static long parseLong(String str, Locale locale) {
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(locale);
            if (!isInteger(str)) throw new RuntimeException("Invalid integer number.");
            Number result = nf.parse(str);
            if (result == null) throw new RuntimeException("Invalid integer number.");
            return result.longValue();
        } catch (Throwable ex) {
            throw ErrUtils.asRuntimeException(ex, String.format("Error parsing \"%s\": ", str));
        }
    }

    public static double parseDouble(String str, Locale locale) {
        try {
            if (str.length() > 0 && Character.isSpaceChar(str.charAt(0))) {
                throw new RuntimeException("Invalid floating point number.");
            }
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(locale);
            java.text.ParsePosition pos = new java.text.ParsePosition(0);
            Number parsedNumber = nf.parse(str, pos);
            if (parsedNumber == null || pos.getIndex() != str.length()) throw new RuntimeException("Invalid floating point number.");
            return parsedNumber.doubleValue();
        } catch (Throwable ex) {
            throw ErrUtils.asRuntimeException(ex, String.format("Error parsing \"%s\": ", str));
        }
    }

    public static int compareLong(final long left, final long right) {
        if (left < right) return -1;
        if (left > right) return +1;
        return 0;
    }

    public static int byteArrayIndexOf(byte[] searchedIn, int fromIndex, byte[] searchedFor) {
        if (fromIndex < 0 || searchedIn.length <= 0 || searchedFor.length <= 0) {
            assert false : ErrUtils.assertionFailed();
            return -1;
        }
        for (int i = fromIndex; i <= searchedIn.length - searchedFor.length; ++i) {
            int j;
            for (j = 0; j < searchedFor.length; ++j) {
                if (searchedIn[i + j] != searchedFor[j]) {
                    break;
                }
            }
            if (j == searchedFor.length) return i;
        }
        return -1;
    }

    public static void desktopOpenAction(Frame fr, File filename) {
        Cursor oldCursor = fr.getCursor();
        fr.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (!Desktop.isDesktopSupported()) return;
            Desktop desktop = Desktop.getDesktop();
            File absPath = filename.getCanonicalFile();
            desktop.open(absPath);
            fr.setCursor(oldCursor);
        } catch (Throwable ex) {
            fr.setCursor(oldCursor);
            ErrDialog.errorDialog(fr, ErrUtils.getExceptionMessage(ex));
        }
    }
}
