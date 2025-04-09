package net.cattaka.rdbassistant.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.cattaka.util.ExceptionHandler;

public class DocumentUtil {

    private static ArrayList<int[]> cache = new ArrayList<int[]>();

    public static int[][] getWordIndexs(Document document, int offset, int length, int margin) {
        cache.clear();
        int start = offset - margin;
        int end = offset + length + margin;
        if (start < 0) {
            start = 0;
        }
        if (end >= document.getLength()) {
            end = document.getLength();
        }
        try {
            String tmpStr = document.getText(start, end - start);
            Pattern pattern = Pattern.compile("[^\\s]+");
            Matcher mr = pattern.matcher(tmpStr);
            while (mr.find()) {
                int[] t = new int[] { start + mr.start(), start + mr.end() };
                cache.add(t);
            }
            if (start > 0) {
                cache.remove(0);
            }
            if (end < document.getLength()) {
                cache.remove(cache.size() - 1);
            }
        } catch (BadLocationException e) {
            ExceptionHandler.error(e);
        }
        return cache.toArray(new int[cache.size()][]);
    }
}
