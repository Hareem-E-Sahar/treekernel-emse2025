package langs;

import java.io.*;
import java.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

class ShortKeyword {

    private String shortKeyword;

    boolean used;

    public ShortKeyword(String shortKeyword) {
        this.shortKeyword = shortKeyword;
        used = false;
    }

    public String getShortKeyword() {
        return shortKeyword;
    }

    public void wasUsed() {
        used = true;
    }

    public boolean getUsed() {
        return used;
    }
}

public class LangsTask extends Task {

    private String languages, inDir, outDir, srcDir, idealLang;

    private static Hashtable shortKeys = new Hashtable();

    private static Vector warnings = new Vector();

    private static char[] shortKeyElems = { '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    private static int shortKeywordCounter = 0;

    static boolean isSpecialKey(String keyword) {
        return ((keyword.indexOf("error_") == 0) || (keyword.indexOf("lang_") == 0));
    }

    static String[] readFile(String fileName) throws Exception {
        Vector lines = new Vector();
        String line;
        InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        while ((line = reader.readLine()) != null) lines.add(line);
        reader.close();
        isr.close();
        String[] result = new String[lines.size()];
        lines.copyInto(result);
        return result;
    }

    static String keyToShortKey(String keyword) {
        if (isSpecialKey(keyword)) return keyword;
        ShortKeyword tableKeyword = (ShortKeyword) shortKeys.get(keyword);
        if (tableKeyword != null) return tableKeyword.getShortKeyword();
        StringBuffer shortKeyword = new StringBuffer();
        int value = shortKeywordCounter;
        do {
            shortKeyword.append(shortKeyElems[value % shortKeyElems.length]);
            value /= shortKeyElems.length;
        } while (value > 0);
        String newKey = shortKeyword.toString();
        shortKeys.put(keyword, new ShortKeyword(newKey));
        shortKeywordCounter++;
        return newKey;
    }

    private static String[] devideToWords(String text) throws Exception {
        String[] resultArray = new String[2];
        try {
            resultArray[0] = text.substring(1, text.indexOf('"', 1));
            resultArray[1] = text.substring(text.indexOf('"', text.indexOf('"', 1) + 1) + 1, text.lastIndexOf('"'));
        } catch (Exception e) {
            throw new Exception();
        }
        return resultArray;
    }

    private static Vector readLangFile(String inFile) {
        Vector data = new Vector();
        String line;
        try {
            File srcFile = new File(inFile);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(srcFile), "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                if (line.charAt(0) == '/') continue;
                StringBuffer strBuf = new StringBuffer();
                int lineLen = line.length();
                for (int i = 0; i < lineLen; i++) {
                    char chr = line.charAt(i);
                    String addText = null;
                    if ((chr == '\\') && (i != (lineLen - 1))) {
                        char chr2 = line.charAt(i + 1);
                        if (chr2 == 'n') addText = "\n";
                        if (chr2 == 'r') addText = "\r";
                        if (chr2 == 't') addText = "\t";
                        if (chr2 == '"') addText = "\"";
                    }
                    if (addText != null) {
                        strBuf.append(addText);
                        i++;
                    } else {
                        if (chr < ' ') chr = ' ';
                        strBuf.append(chr);
                    }
                }
                String[] words;
                try {
                    words = devideToWords(strBuf.toString());
                } catch (Exception e) {
                    throw new BuildException("Wrong lang line '" + line + "' in file '" + inFile + "'");
                }
                data.addElement(words);
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
        return data;
    }

    private static void addKeysToSpecialKeys(Vector pairs) {
        int size = pairs.size();
        for (int i = 0; i < size; i++) {
            String[] pair = (String[]) pairs.elementAt(i);
            keyToShortKey(pair[0]);
        }
    }

    private static void writeLngFile(Vector pairs, String outFile, boolean onelang) throws BuildException {
        try {
            FileOutputStream ostream = new FileOutputStream(outFile);
            DataOutputStream dos = new DataOutputStream(ostream);
            int size = pairs.size();
            dos.writeShort(size);
            for (int i = 0; i < size; i++) {
                String[] pair = (String[]) pairs.elementAt(i);
                dos.writeUTF(keyToShortKey(pair[0]));
                dos.writeUTF(pair[1]);
            }
            dos.flush();
            ostream.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private static String[] scanDir(File srcDir, String srcDirExt) {
        Vector filenames = new Vector();
        File[] files = (new File(srcDir, srcDirExt)).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && (files[i].getName().endsWith(".java"))) {
                filenames.add(srcDir + srcDirExt + File.separator + files[i].getName());
            } else if (files[i].isDirectory()) {
                filenames.addAll(Arrays.asList(scanDir(srcDir, srcDirExt + File.separator + files[i].getName())));
            }
        }
        String[] ret = new String[filenames.size()];
        filenames.copyInto(ret);
        return (ret);
    }

    private static void correctToIdealLang(String langName, Vector pairs, Hashtable idealPairs) {
        Hashtable normalData = pairsToTable(pairs);
        Enumeration idealKeys = idealPairs.keys();
        while (idealKeys.hasMoreElements()) {
            String idealKey = (String) idealKeys.nextElement();
            if (!normalData.containsKey(idealKey)) {
                String value = (String) idealPairs.get(idealKey);
                normalData.put(idealKey, value);
                warnings.add(langName + ".lang: missed \"" + idealKey + "\" (" + value + ")");
            }
        }
        Enumeration normalKeys = normalData.keys();
        while (normalKeys.hasMoreElements()) {
            String normalKey = (String) normalKeys.nextElement();
            if (!idealPairs.containsKey(normalKey)) {
                warnings.add(langName + ".lang: unnecessary \"" + normalKey + "\"");
                normalData.remove(normalKey);
                normalKeys = normalData.keys();
            }
        }
        normalKeys = normalData.keys();
        while (normalKeys.hasMoreElements()) {
            String normalKey = (String) normalKeys.nextElement();
            ShortKeyword keyObj = (ShortKeyword) shortKeys.get(normalKey);
            if (keyObj == null) continue;
            if (!keyObj.getUsed()) {
                warnings.add(langName + ".lang: not present at sources \"" + normalKey + "\"");
                normalData.remove(normalKey);
                normalKeys = normalData.keys();
            }
        }
        pairs.clear();
        normalKeys = normalData.keys();
        while (normalKeys.hasMoreElements()) {
            String normalKey = (String) normalKeys.nextElement();
            pairs.add(new String[] { normalKey, (String) normalData.get(normalKey) });
        }
    }

    private static String replaceString(String str, String from, String to) {
        for (; ; ) {
            int index = str.indexOf(from);
            if (index == -1) break;
            str = str.substring(0, index) + to + str.substring(index + from.length(), str.length());
        }
        return str;
    }

    private static void replaceLangKeysInSources(String fileName) {
        String line;
        try {
            line = readAndPrepareSrcFile(fileName);
            Enumeration allKeys = shortKeys.keys();
            while (allKeys.hasMoreElements()) {
                String oldKey = (String) allKeys.nextElement();
                ShortKeyword shortKeyword = (ShortKeyword) shortKeys.get(oldKey);
                String newKey = shortKeyword.getShortKeyword();
                String textToReplace = "\"" + oldKey + "\"";
                boolean contains = (line.indexOf(textToReplace) != -1);
                if (contains) shortKeyword.wasUsed();
                line = replaceString(line, textToReplace, "\"" + newKey + "\"");
            }
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            BufferedWriter writer = new BufferedWriter(osw);
            writer.write(line);
            writer.newLine();
            writer.close();
            osw.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private static final int PP_STATE_DIRECT_COPY = 1;

    private static final int PP_STATE_WAIT_FOR_COMMENT = 2;

    private static final int PP_STATE_SIMPLE_COMMENT = 3;

    private static final int PP_STATE_MULTILINE_COMMENT = 4;

    private static final int PP_STATE_WAIT_FOR_MLC_END = 5;

    private static final int PP_STATE_STRING = 6;

    static String readAndPrepareSrcFile(String fileName) throws BuildException {
        StringBuffer buffer = new StringBuffer();
        int state = PP_STATE_DIRECT_COPY;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(isr);
            for (; ; ) {
                int curByte = reader.read();
                if (curByte == -1) break;
                switch(state) {
                    case PP_STATE_DIRECT_COPY:
                        if (curByte == '/') state = PP_STATE_WAIT_FOR_COMMENT; else if (curByte == '\"') {
                            state = PP_STATE_STRING;
                            buffer.append((char) curByte);
                        } else buffer.append((char) curByte);
                        break;
                    case PP_STATE_WAIT_FOR_COMMENT:
                        if (curByte == '*') state = PP_STATE_MULTILINE_COMMENT; else if (curByte == '/') state = PP_STATE_SIMPLE_COMMENT; else {
                            buffer.append('/');
                            buffer.append((char) curByte);
                            state = PP_STATE_DIRECT_COPY;
                        }
                        break;
                    case PP_STATE_SIMPLE_COMMENT:
                        if ((curByte == '\r') || (curByte == '\n')) state = PP_STATE_DIRECT_COPY;
                        break;
                    case PP_STATE_MULTILINE_COMMENT:
                        if (curByte == '*') state = PP_STATE_WAIT_FOR_MLC_END;
                        break;
                    case PP_STATE_WAIT_FOR_MLC_END:
                        if (curByte == '/') state = PP_STATE_DIRECT_COPY; else if (curByte == '*') state = PP_STATE_WAIT_FOR_MLC_END; else state = PP_STATE_MULTILINE_COMMENT;
                        break;
                    case PP_STATE_STRING:
                        buffer.append((char) curByte);
                        if (curByte == '\"') state = PP_STATE_DIRECT_COPY;
                        break;
                }
            }
            reader.close();
            isr.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
        return buffer.toString();
    }

    private static void showWarnings() {
        int size = warnings.size();
        if (size == 0) return;
        System.out.println("*** WARNINGS ***");
        for (int i = 0; i < size; i++) System.out.println((String) warnings.elementAt(i));
        warnings.clear();
    }

    private static Hashtable pairsToTable(Vector pairs) {
        Hashtable result = new Hashtable();
        for (int i = 0; i < pairs.size(); i++) {
            String[] pair = (String[]) pairs.elementAt(i);
            result.put(pair[0], pair[1]);
        }
        return result;
    }

    public void setLanguages(String value) {
        languages = value;
    }

    public void setInDir(String value) {
        inDir = value;
    }

    public void setOutDir(String value) {
        outDir = value;
    }

    public void setSrcDir(String value) {
        srcDir = value;
    }

    public void setIdealLang(String value) {
        idealLang = value;
    }

    public void execute() throws BuildException {
        Hashtable idealHashTable = null;
        shortKeys.clear();
        warnings.clear();
        Vector langs = new Vector();
        StringTokenizer strTok = new StringTokenizer(languages, ",");
        while (strTok.hasMoreTokens()) {
            String langName = strTok.nextToken();
            langs.add(langName);
        }
        int size = langs.size();
        if (size == 0) new BuildException("No language specified");
        System.out.println("Loading lang file[s]... ");
        Vector pairs, idealPairs = null;
        if (idealLang != null) {
            idealPairs = readLangFile(inDir + "/" + idealLang + ".lang");
            idealHashTable = pairsToTable(idealPairs);
        }
        for (int i = 0; i < size; i++) {
            String langName = (String) langs.elementAt(i);
            pairs = readLangFile(inDir + "/" + langName + ".lang");
            addKeysToSpecialKeys(pairs);
        }
        try {
            FileOutputStream ostream = new FileOutputStream(outDir + "/langlist.lng");
            DataOutputStream dos = new DataOutputStream(ostream);
            dos.writeShort(langs.size());
            for (int i = 0; i < langs.size(); i++) dos.writeUTF((String) langs.elementAt(i));
            dos.flush();
            ostream.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
        showWarnings();
        System.out.println("Preprocessing java sources... ");
        String[] files = scanDir(new File(srcDir), "");
        for (int i = 0; i < files.length; i++) replaceLangKeysInSources(files[i]);
        System.out.println("Saving lng file[s]... ");
        size = langs.size();
        for (int i = 0; i < size; i++) {
            String langName = (String) langs.elementAt(i);
            pairs = readLangFile(inDir + "/" + langName + ".lang");
            correctToIdealLang(langName, pairs, idealHashTable);
            writeLngFile(pairs, outDir + "/" + langName + ".lng", size == 1);
        }
        showWarnings();
    }
}
