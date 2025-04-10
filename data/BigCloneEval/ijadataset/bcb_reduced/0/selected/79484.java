package org.jmol.adapter.smarter;

import org.jmol.api.JmolAdapter;
import org.jmol.viewer.JmolConstants;
import java.io.BufferedReader;
import java.lang.reflect.Array;

abstract class AtomSetCollectionReader {

    AtomSetCollection atomSetCollection;

    JmolAdapter.Logger logger;

    static final float ANGSTROMS_PER_BOHR = 0.5291772f;

    void setLogger(JmolAdapter.Logger logger) {
        this.logger = logger;
    }

    void initialize() {
    }

    abstract AtomSetCollection readAtomSetCollection(BufferedReader reader) throws Exception;

    AtomSetCollection readAtomSetCollectionFromDOM(Object DOMNode) throws Exception {
        return null;
    }

    int ichNextParse;

    float parseFloat(String str) {
        return parseFloatChecked(str, 0, str.length());
    }

    float parseFloat(String str, int ich) {
        int cch = str.length();
        if (ich >= cch) return Float.NaN;
        return parseFloatChecked(str, ich, cch);
    }

    float parseFloat(String str, int ichStart, int ichMax) {
        int cch = str.length();
        if (ichMax > cch) ichMax = cch;
        if (ichStart >= ichMax) return Float.NaN;
        return parseFloatChecked(str, ichStart, ichMax);
    }

    static final float[] decimalScale = { 0.1f, 0.01f, 0.001f, 0.0001f, 0.00001f, 0.000001f, 0.0000001f, 0.00000001f };

    static final float[] tensScale = { 10, 100, 1000, 10000, 100000, 1000000 };

    float parseFloatChecked(String str, int ichStart, int ichMax) {
        boolean digitSeen = false;
        float value = 0;
        int ich = ichStart;
        char ch;
        while (ich < ichMax && ((ch = str.charAt(ich)) == ' ' || ch == '\t')) ++ich;
        boolean negative = false;
        if (ich < ichMax && str.charAt(ich) == '-') {
            ++ich;
            negative = true;
        }
        ch = 0;
        while (ich < ichMax && (ch = str.charAt(ich)) >= '0' && ch <= '9') {
            value = value * 10 + (ch - '0');
            ++ich;
            digitSeen = true;
        }
        if (ch == '.') {
            int iscale = 0;
            while (++ich < ichMax && (ch = str.charAt(ich)) >= '0' && ch <= '9') {
                if (iscale < decimalScale.length) value += (ch - '0') * decimalScale[iscale];
                ++iscale;
                digitSeen = true;
            }
        }
        if (!digitSeen) value = Float.NaN; else if (negative) value = -value;
        if (ich < ichMax && (ch == 'E' || ch == 'e')) {
            if (++ich >= ichMax) return Float.NaN;
            ch = str.charAt(ich);
            if ((ch == '+') && (++ich >= ichMax)) return Float.NaN;
            int exponent = parseIntChecked(str, ich, ichMax);
            if (exponent == Integer.MIN_VALUE) return Float.NaN;
            if (exponent > 0) value *= ((exponent < tensScale.length) ? tensScale[exponent - 1] : Math.pow(10, exponent)); else if (exponent < 0) value *= ((-exponent < decimalScale.length) ? decimalScale[-exponent - 1] : Math.pow(10, exponent));
        } else {
            ichNextParse = ich;
        }
        return value;
    }

    int parseInt(String str) {
        return parseIntChecked(str, 0, str.length());
    }

    int parseInt(String str, int ich) {
        int cch = str.length();
        if (ich >= cch) return Integer.MIN_VALUE;
        return parseIntChecked(str, ich, cch);
    }

    int parseInt(String str, int ichStart, int ichMax) {
        int cch = str.length();
        if (ichMax > cch) ichMax = cch;
        if (ichStart >= ichMax) return Integer.MIN_VALUE;
        return parseIntChecked(str, ichStart, ichMax);
    }

    int parseIntChecked(String str, int ichStart, int ichMax) {
        boolean digitSeen = false;
        int value = 0;
        int ich = ichStart;
        char ch;
        while (ich < ichMax && ((ch = str.charAt(ich)) == ' ' || ch == '\t')) ++ich;
        boolean negative = false;
        if (ich < ichMax && str.charAt(ich) == '-') {
            negative = true;
            ++ich;
        }
        while (ich < ichMax && (ch = str.charAt(ich)) >= '0' && ch <= '9') {
            value = value * 10 + (ch - '0');
            digitSeen = true;
            ++ich;
        }
        if (!digitSeen) value = Integer.MIN_VALUE; else if (negative) value = -value;
        ichNextParse = ich;
        return value;
    }

    String[] getTokens(String line) {
        return getTokens(line, 0);
    }

    String[] getTokens(String line, int ich) {
        if (line == null) return null;
        int cchLine = line.length();
        if (ich > cchLine) return null;
        int tokenCount = countTokens(line, ich);
        String[] tokens = new String[tokenCount];
        ichNextParse = ich;
        for (int i = 0; i < tokenCount; ++i) tokens[i] = parseTokenChecked(line, ichNextParse, cchLine);
        return tokens;
    }

    int countTokens(String line, int ich) {
        int tokenCount = 0;
        if (line != null) {
            int ichMax = line.length();
            char ch;
            while (true) {
                while (ich < ichMax && ((ch = line.charAt(ich)) == ' ' || ch == '\t')) ++ich;
                if (ich == ichMax) break;
                ++tokenCount;
                do {
                    ++ich;
                } while (ich < ichMax && ((ch = line.charAt(ich)) != ' ' && ch != '\t'));
            }
        }
        return tokenCount;
    }

    String parseToken(String str) {
        return parseTokenChecked(str, 0, str.length());
    }

    String parseToken(String str, int ich) {
        int cch = str.length();
        if (ich >= cch) return null;
        return parseTokenChecked(str, ich, cch);
    }

    String parseToken(String str, int ichStart, int ichMax) {
        int cch = str.length();
        if (ichMax > cch) ichMax = cch;
        if (ichStart >= ichMax) return null;
        return parseTokenChecked(str, ichStart, ichMax);
    }

    String parseTokenChecked(String str, int ichStart, int ichMax) {
        int ich = ichStart;
        char ch;
        while (ich < ichMax && ((ch = str.charAt(ich)) == ' ' || ch == '\t')) ++ich;
        int ichNonWhite = ich;
        while (ich < ichMax && ((ch = str.charAt(ich)) != ' ' && ch != '\t')) ++ich;
        ichNextParse = ich;
        if (ichNonWhite == ich) return null;
        return str.substring(ichNonWhite, ich);
    }

    String parseTrimmed(String str) {
        return parseTrimmedChecked(str, 0, str.length());
    }

    String parseTrimmed(String str, int ich) {
        int cch = str.length();
        if (ich >= cch) return null;
        return parseTrimmedChecked(str, ich, cch);
    }

    String parseTrimmed(String str, int ichStart, int ichMax) {
        int cch = str.length();
        if (ichMax > cch) ichMax = cch;
        if (ichStart >= ichMax) return null;
        return parseTrimmedChecked(str, ichStart, ichMax);
    }

    String parseTrimmedChecked(String str, int ichStart, int ichMax) {
        int ich = ichStart;
        char ch;
        while (ich < ichMax && ((ch = str.charAt(ich)) == ' ' || ch == '\t')) ++ich;
        int ichLast = ichMax - 1;
        while (ichLast >= ich && ((ch = str.charAt(ichLast)) == ' ' || ch == '\t')) --ichLast;
        if (ichLast < ich) return null;
        ichNextParse = ichLast + 1;
        return str.substring(ich, ichLast + 1);
    }

    static int[] doubleLength(int[] array) {
        return setLength(array, array.length * 2);
    }

    static String[] doubleLength(String[] array) {
        return setLength(array, array.length * 2);
    }

    static Object doubleLength(Object[] array) {
        return setLength(array, array.length * 2);
    }

    static Object setLength(Object array, int newLength) {
        Object t = Array.newInstance(array.getClass().getComponentType(), newLength);
        int oldLength = Array.getLength(array);
        System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        return t;
    }

    static String[] setLength(String[] array, int newLength) {
        String[] t = new String[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static int[] setLength(int[] array, int newLength) {
        int oldLength = array.length;
        int[] t = new int[newLength];
        System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        return t;
    }

    static float[] setLength(float[] array, int newLength) {
        int oldLength = array.length;
        float[] t = new float[newLength];
        System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        return t;
    }

    void discardLines(BufferedReader reader, int nLines) throws Exception {
        for (int i = nLines; --i >= 0; ) reader.readLine();
    }

    String discardLinesUntilStartsWith(BufferedReader reader, String startsWith) throws Exception {
        String line;
        while ((line = reader.readLine()) != null && !line.startsWith(startsWith)) {
        }
        return line;
    }

    String discardLinesUntilContains(BufferedReader reader, String containsMatch) throws Exception {
        String line;
        while ((line = reader.readLine()) != null && line.indexOf(containsMatch) < 0) {
        }
        return line;
    }

    void discardLinesUntilBlank(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null && line.length() != 0) {
        }
    }

    String discardLinesUntilNonBlank(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null && line.length() == 0) {
        }
        return line;
    }

    static String getElementSymbol(int elementNumber) {
        if (elementNumber < 0 || elementNumber >= JmolConstants.elementSymbols.length) elementNumber = 0;
        return JmolConstants.elementSymbols[elementNumber];
    }

    String getString(String line, String strQuote) {
        int i = line.indexOf(strQuote);
        int j = line.lastIndexOf(strQuote);
        return (j == i ? "" : line.substring(i + 1, j));
    }
}
