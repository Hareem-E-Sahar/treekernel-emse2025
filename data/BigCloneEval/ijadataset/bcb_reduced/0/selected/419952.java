package net.sf.regain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;

/**
 * Enth�lt Hilfsmethoden, die sowohl vom Crawler als auch von der Suchmaske
 * genutzt werden.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RegainToolkit {

    /** The encoding used for storing URLs in the index */
    public static final String INDEX_ENCODING = "UTF-8";

    /**
   * Gibt an, ob die Worte, die der Analyzer identifiziert ausgegeben werden
   * sollen.
   */
    private static final boolean ANALYSE_ANALYZER = false;

    /** The number of bytes in a kB (kilo byte). */
    private static final int SIZE_KB = 1024;

    /** The number of bytes in a MB (mega byte). */
    private static final int SIZE_MB = 1024 * 1024;

    /** The number of bytes in a GB (giga byte). */
    private static final int SIZE_GB = 1024 * 1024 * 1024;

    /** The cached system's default encoding. */
    private static String mSystemDefaultEncoding;

    /** Der gecachte, systemspeziefische Zeilenumbruch. */
    private static String mLineSeparator;

    /**
   * L�scht ein Verzeichnis mit allen Unterverzeichnissen und -dateien.
   *
   * @param dir Das zu l�schende Verzeichnis.
   *
   * @throws RegainException Wenn das L�schen fehl schlug.
   */
    public static void deleteDirectory(File dir) throws RegainException {
        if (!dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].isDirectory()) {
                    deleteDirectory(children[i]);
                } else {
                    if (!children[i].delete()) {
                        throw new RegainException("Deleting " + children[i].getAbsolutePath() + " failed!");
                    }
                }
            }
        }
        if (!dir.delete()) {
            throw new RegainException("Deleting " + dir.getAbsolutePath() + " failed!");
        }
    }

    /**
   * Writes all data from the reader to the writer.
   * <p>
   * Neither the reader nor the writer will be closed. This has to be done by
   * the caller!
   *
   * @param reader The reader that provides the data.
   * @param writer The writer where to write the data.
   *
   * @throws IOException If reading or writing failed.
   */
    public static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[10240];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
    }

    /**
   * Schreibt alle Daten, die der InputStream liefert in den OutputStream.
   * <p>
   * Weder der InputStream noch der OutputStream werden dabei geschlossen. Dies
   * muss die aufrufende Methode �bernehmen!
   *
   * @param in Der InputStream, der die Daten liefert.
   * @param out Der OutputStream auf den die Daten geschrieben werden sollen.
   *
   * @throws IOException Wenn Lesen oder Schreiben fehl schlug.
   */
    public static void pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[10240];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    /**
   * Copies a file.
   *
   * @param from The source file.
   * @param to The target file.
   * @throws RegainException If copying failed.
   */
    public static void copyFile(File from, File to) throws RegainException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            RegainToolkit.pipe(in, out);
        } catch (IOException exc) {
            throw new RegainException("Copying file from " + from.getAbsolutePath() + " to " + to.getAbsolutePath() + " failed", exc);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException exc) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
   * Copies a directory.
   *
   * @param fromDir The source directory.
   * @param toDir The target directory.
   * @param copySubDirs Specifies whether to copy sub directories.
   * @param excludeExtension The file extension to exclude.
   * @throws RegainException If copying the index failed.
   */
    public static void copyDirectory(File fromDir, File toDir, boolean copySubDirs, String excludeExtension) throws RegainException {
        File[] indexFiles = fromDir.listFiles();
        for (int i = 0; i < indexFiles.length; i++) {
            String fileName = indexFiles[i].getName();
            File targetFile = new File(toDir, fileName);
            if (indexFiles[i].isDirectory()) {
                if (copySubDirs) {
                    targetFile.mkdir();
                    copyDirectory(indexFiles[i], targetFile, copySubDirs, excludeExtension);
                }
            } else if ((excludeExtension == null) || (!fileName.endsWith(excludeExtension))) {
                RegainToolkit.copyFile(indexFiles[i], targetFile);
            }
        }
    }

    /**
   * Copies a directory.
   *
   * @param fromDir The source directory.
   * @param toDir The target directory.
   * @param copySubDirs Specifies whether to copy sub directories.
   * @throws RegainException If copying the index failed.
   */
    public static void copyDirectory(File fromDir, File toDir, boolean copySubDirs) throws RegainException {
        copyDirectory(fromDir, toDir, copySubDirs, null);
    }

    /**
   * Reads a String from a stream.
   *
   * @param stream The stream to read the String from
   * @param charsetName The name of the charset to use.
   * @return The stream content as String.
   * @throws RegainException If reading the String failed.
   */
    public static String readStringFromStream(InputStream stream, String charsetName) throws RegainException {
        InputStreamReader reader = null;
        try {
            if (charsetName == null) {
                reader = new InputStreamReader(stream);
            } else {
                reader = new InputStreamReader(stream, charsetName);
            }
            StringWriter writer = new StringWriter();
            RegainToolkit.pipe(reader, writer);
            reader.close();
            writer.close();
            return writer.toString();
        } catch (IOException exc) {
            throw new RegainException("Reading String from stream failed", exc);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
   * Reads a String from a stream.
   *
   * @param stream The stream to read the String from
   * @return The stream content as String.
   * @throws RegainException If reading the String failed.
   */
    public static String readStringFromStream(InputStream stream) throws RegainException {
        return readStringFromStream(stream, null);
    }

    /**
   * Liest einen String aus einer Datei.
   *
   * @param file Die Datei aus der der String gelesen werden soll.
   *
   * @return Der Inhalt der Datei als String oder <code>null</code>, wenn die
   *         Datei nicht existiert.
   * @throws RegainException Wenn das Lesen fehl schlug.
   */
    public static String readStringFromFile(File file) throws RegainException {
        if (!file.exists()) {
            return null;
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return readStringFromStream(stream);
        } catch (IOException exc) {
            throw new RegainException("Reading String from " + file.getAbsolutePath() + "failed", exc);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
   * Reads a word list from a file.
   *
   * @param file The file to read the list from.
   *
   * @return The lines of the file.
   * @throws RegainException If reading failed.
   */
    public static String[] readListFromFile(File file) throws RegainException {
        if (!file.exists()) {
            return null;
        }
        FileReader reader = null;
        BufferedReader buffReader = null;
        try {
            reader = new FileReader(file);
            buffReader = new BufferedReader(reader);
            ArrayList list = new ArrayList();
            String line;
            while ((line = buffReader.readLine()) != null) {
                list.add(line);
            }
            String[] asArr = new String[list.size()];
            list.toArray(asArr);
            return asArr;
        } catch (IOException exc) {
            throw new RegainException("Reading word list from " + file.getAbsolutePath() + "failed", exc);
        } finally {
            if (buffReader != null) {
                try {
                    buffReader.close();
                } catch (IOException exc) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
   * Writes data to a file
   *
   * @param data The data
   * @param file The file to write to
   *
   * @throws RegainException When writing failed
   */
    public static void writeToFile(byte[] data, File file) throws RegainException {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(data);
            stream.close();
        } catch (IOException exc) {
            throw new RegainException("Writing file failed: " + file.getAbsolutePath(), exc);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
   * Writes a String into a file.
   *
   * @param text The string.
   * @param file The file to write to.
   *
   * @throws RegainException If writing failed.
   */
    public static void writeToFile(String text, File file) throws RegainException {
        writeListToFile(new String[] { text }, file);
    }

    /**
   * Writes a word list in a file. Each item of the list will be written in a
   * line.
   *
   * @param wordList The word list.
   * @param file The file to write to.
   *
   * @throws RegainException If writing failed.
   */
    public static void writeListToFile(String[] wordList, File file) throws RegainException {
        if ((wordList == null) || (wordList.length == 0)) {
            return;
        }
        FileOutputStream stream = null;
        PrintStream printer = null;
        try {
            stream = new FileOutputStream(file);
            printer = new PrintStream(stream);
            for (int i = 0; i < wordList.length; i++) {
                printer.println(wordList[i]);
            }
        } catch (IOException exc) {
            throw new RegainException("Writing word list to " + file.getAbsolutePath() + " failed", exc);
        } finally {
            if (printer != null) {
                printer.close();
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
   * Gets the size of a directory with all files.
   *
   * @param dir The directory to get the size for.
   * @return The size of the directory.
   */
    public static long getDirectorySize(File dir) {
        File[] childArr = dir.listFiles();
        long size = 0;
        if (childArr != null) {
            for (int i = 0; i < childArr.length; i++) {
                if (childArr[i].isDirectory()) {
                    size += getDirectorySize(childArr[i]);
                } else {
                    size += childArr[i].length();
                }
            }
        }
        return size;
    }

    /**
   * Returns the destinct values of one or more fields.
   * <p>
   * If an index directory is provided, then the values will be read from there.
   * They will be extracted from the search index if there are no matching
   * cache files. After extracting the cache files will be created, so the next
   * call will be faster.
   *
   * @param indexReader The index reader to use for reading the field values.
   * @param fieldNameArr The names of the fields to read the destinct values for.
   * @param indexDir The index directory where to read or write the cached
   *        destinct values. May be null.
   * @return A hashmap containing for a field name (key, String) the sorted
   *         array of destinct values (value, String[]).
   * @throws RegainException If reading from the index failed. Or if reading or
   *         writing a cache file failed.
   */
    public static HashMap readFieldValues(IndexReader indexReader, String[] fieldNameArr, File indexDir) throws RegainException {
        HashMap resultMap = new HashMap();
        HashSet fieldsToReadSet = new HashSet();
        for (int i = 0; i < fieldNameArr.length; i++) {
            String field = fieldNameArr[i];
            String[] fieldValueArr = null;
            if (indexDir != null) {
                File fieldFile = new File(indexDir, "field_values_" + field + ".txt");
                fieldValueArr = readListFromFile(fieldFile);
            }
            if (fieldValueArr != null) {
                resultMap.put(field, fieldValueArr);
            } else {
                fieldsToReadSet.add(field);
                resultMap.put(field, new ArrayList());
            }
        }
        fieldNameArr = null;
        if (!fieldsToReadSet.isEmpty()) {
            try {
                TermEnum termEnum = indexReader.terms();
                while (termEnum.next()) {
                    Term term = termEnum.term();
                    String field = term.field();
                    if (fieldsToReadSet.contains(field)) {
                        ArrayList valueList = (ArrayList) resultMap.get(field);
                        valueList.add(term.text());
                    }
                }
            } catch (IOException exc) {
                throw new RegainException("Reading terms from index failed", exc);
            }
        }
        Iterator readFieldIter = fieldsToReadSet.iterator();
        while (readFieldIter.hasNext()) {
            String field = (String) readFieldIter.next();
            ArrayList valueList = (ArrayList) resultMap.get(field);
            String[] valueArr = new String[valueList.size()];
            valueList.toArray(valueArr);
            Arrays.sort(valueArr);
            resultMap.put(field, valueArr);
            if (indexDir != null) {
                File fieldFile = new File(indexDir, "field_values_" + field + ".txt");
                writeListToFile(valueArr, fieldFile);
            }
        }
        return resultMap;
    }

    /**
   * Creates an analyzer that is used both from the crawler and the search mask.
   * It is important that both use the same analyzer which is the reason for
   * this method.
   *
   * @param analyzerType The type of the analyzer to create. Either a classname
   *        or "english" or "german".
   * @param stopWordList All words that should not be indexed.
   * @param exclusionList All words that shouldn't be changed by the analyzer.
   * @param untokenizedFieldNames The names of the fields that should not be
   *        tokenized.
   * @return The analyzer.
   * @throws RegainException If the creation failed.
   */
    public static Analyzer createAnalyzer(String analyzerType, String[] stopWordList, String[] exclusionList, String[] untokenizedFieldNames) throws RegainException {
        if (analyzerType == null) {
            throw new RegainException("No analyzer type specified!");
        }
        analyzerType = analyzerType.trim();
        String analyzerClassName = analyzerType;
        if (analyzerType.equalsIgnoreCase("english")) {
            analyzerClassName = StandardAnalyzer.class.getName();
        } else if (analyzerType.equalsIgnoreCase("german")) {
            analyzerClassName = GermanAnalyzer.class.getName();
        }
        Class analyzerClass;
        try {
            analyzerClass = Class.forName(analyzerClassName);
        } catch (ClassNotFoundException exc) {
            throw new RegainException("Analyzer class not found: " + analyzerClassName, exc);
        }
        Analyzer analyzer;
        if ((stopWordList != null) && (stopWordList.length != 0)) {
            Constructor ctor;
            try {
                ctor = analyzerClass.getConstructor(new Class[] { stopWordList.getClass() });
            } catch (Throwable thr) {
                throw new RegainException("Analyzer " + analyzerType + " does not support stop words");
            }
            try {
                analyzer = (Analyzer) ctor.newInstance(new Object[] { stopWordList });
            } catch (Throwable thr) {
                throw new RegainException("Creating analyzer instance failed", thr);
            }
        } else {
            try {
                analyzer = (Analyzer) analyzerClass.newInstance();
            } catch (Throwable thr) {
                throw new RegainException("Creating analyzer instance failed", thr);
            }
        }
        if ((exclusionList != null) && (exclusionList.length != 0)) {
            Method setter;
            try {
                setter = analyzerClass.getMethod("setStemExclusionTable", new Class[] { exclusionList.getClass() });
            } catch (Throwable thr) {
                throw new RegainException("Analyzer " + analyzerType + " does not support exclusion lists");
            }
            try {
                setter.invoke(analyzer, new Object[] { exclusionList });
            } catch (Throwable thr) {
                throw new RegainException("Applying exclusion list failed.", thr);
            }
        }
        analyzer = new WrapperAnalyzer(analyzer, untokenizedFieldNames);
        if (ANALYSE_ANALYZER) {
            return createAnalysingAnalyzer(analyzer);
        }
        return analyzer;
    }

    /**
   * Erzeugt einen Analyzer, der die Aufrufe an einen eingebetteten Analyzer
   * analysiert.
   * <p>
   * Dies ist beim Debugging hilfreich, wenn man pr�fen will, was ein Analyzer
   * bei bestimmten Anfragen ausgibt.
   *
   * @param nestedAnalyzer The nested Analyzer that should
   * @return Ein Analyzer, der die Aufrufe an einen eingebetteten Analyzer
   *         analysiert.
   */
    private static Analyzer createAnalysingAnalyzer(final Analyzer nestedAnalyzer) {
        return new Analyzer() {

            public TokenStream tokenStream(String fieldName, Reader reader) {
                try {
                    StringWriter writer = new java.io.StringWriter();
                    pipe(reader, writer);
                    String asString = writer.toString();
                    TokenStream stream = nestedAnalyzer.tokenStream(fieldName, new StringReader(asString));
                    System.out.println("Tokens for '" + asString + "':");
                    Token token;
                    while ((token = stream.next()) != null) {
                        System.out.println("  '" + token.termText() + "'");
                    }
                    return nestedAnalyzer.tokenStream(fieldName, new StringReader(asString));
                } catch (IOException exc) {
                    System.out.println("exc: " + exc);
                    return null;
                }
            }
        };
    }

    /**
   * Replaces in a string all occurences of <code>pattern</code> with
   * <code>replacement</code>.
   * <p>
   * Note: <code>pattern</code> may be a substring of <code>replacement</code>.
   *
   * @param source The string to search in
   * @param pattern The pattern to be replaced
   * @param replacement The replacement for each occurence of the pattern.
   *
   * @return A string where all occurences of <code>pattern</code> are replaced
   *         by <code>replacement</code>.
   */
    public static String replace(String source, String pattern, String replacement) {
        int pos = source.indexOf(pattern);
        if (pos == -1) {
            return source;
        }
        StringBuffer target = new StringBuffer(source.length());
        int start = 0;
        do {
            target.append(source.substring(start, pos));
            target.append(replacement);
            start = pos + pattern.length();
        } while ((pos = source.indexOf(pattern, start)) != -1);
        target.append(source.substring(start, source.length()));
        return target.toString();
    }

    /**
   * Replaces in a string all occurences of a list of patterns with replacements.
   * <p>
   * Note: The string is searched left to right. So any pattern matching earlier
   * in the string will be replaced.
   * Example: replace("abcd", { "bc", "ab", "cd" }, { "x", "1", "2" }) will
   * return "12" (the pattern "bc" won't be applied, since "ab" matches before).
   * <p>
   * Note: If two patterns match at the same position, then the first one
   * defined will be applied.
   * Example: replace("abcd", { "ab", "abc" }, { "1", "2" }) will return "1cd".
   *
   * @param source The string to search in
   * @param patternArr The pattern to be replaced
   * @param replacementArr The replacement for each occurence of the pattern.
   *
   * @return A string where all occurences of <code>pattern</code> are replaced
   *         by <code>replacement</code>.
   */
    public static String replace(String source, String[] patternArr, String[] replacementArr) {
        if (patternArr.length != replacementArr.length) {
            throw new IllegalArgumentException("patternArr and replacementArr must " + "have the same length: " + patternArr.length + " != " + replacementArr.length);
        }
        int[] posArr = new int[patternArr.length];
        int minPos = Integer.MAX_VALUE;
        int minPosIdx = -1;
        for (int i = 0; i < posArr.length; i++) {
            posArr[i] = source.indexOf(patternArr[i]);
            if (posArr[i] != -1 && posArr[i] < minPos) {
                minPos = posArr[i];
                minPosIdx = i;
            }
        }
        if (minPosIdx == -1) {
            return source;
        }
        StringBuffer target = new StringBuffer(source.length());
        int start = 0;
        do {
            target.append(source.substring(start, minPos));
            target.append(replacementArr[minPosIdx]);
            start = minPos + patternArr[minPosIdx].length();
            minPos = Integer.MAX_VALUE;
            minPosIdx = -1;
            for (int i = 0; i < posArr.length; i++) {
                if (posArr[i] < start) {
                    posArr[i] = source.indexOf(patternArr[i], start);
                }
                if (posArr[i] != -1 && posArr[i] < minPos) {
                    minPos = posArr[i];
                    minPosIdx = i;
                }
            }
        } while (minPosIdx != -1);
        target.append(source.substring(start, source.length()));
        return target.toString();
    }

    /**
   * Gibt einen Wert in Prozent mit zwei Nachkommastellen zur�ck.
   *
   * @param value Der Wert. (Zwischen 0 und 1)
   * @return Der Wert in Prozent.
   */
    public static String toPercentString(double value) {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }

    /**
   * Gibt einen f�r den Menschen gut lesbaren String f�r eine Anzahl Bytes
   * zur�ck.
   *
   * @param bytes Die Anzahl Bytes
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
    public static String bytesToString(long bytes) {
        return bytesToString(bytes, Locale.ENGLISH);
    }

    /**
   * Gibt einen f�r den Menschen gut lesbaren String f�r eine Anzahl Bytes
   * zur�ck.
   *
   * @param bytes Die Anzahl Bytes
   * @param locale The locale to use for formatting the numbers.
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
    public static String bytesToString(long bytes, Locale locale) {
        return bytesToString(bytes, 2, locale);
    }

    /**
   * Gibt einen f�r den Menschen gut lesbaren String f�r eine Anzahl Bytes
   * zur�ck.
   *
   * @param bytes Die Anzahl Bytes
   * @param fractionDigits Die Anzahl der Nachkommastellen
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
    public static String bytesToString(long bytes, int fractionDigits) {
        return bytesToString(bytes, fractionDigits, Locale.ENGLISH);
    }

    /**
   * Gibt einen f�r den Menschen gut lesbaren String f�r eine Anzahl Bytes
   * zur�ck.
   *
   * @param bytes Die Anzahl Bytes
   * @param fractionDigits Die Anzahl der Nachkommastellen
   * @param locale The locale to use for formatting the numbers.
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
    public static String bytesToString(long bytes, int fractionDigits, Locale locale) {
        int factor;
        String unit;
        if (bytes > SIZE_GB) {
            factor = SIZE_GB;
            unit = "GB";
        } else if (bytes > SIZE_MB) {
            factor = SIZE_MB;
            unit = "MB";
        } else if (bytes > SIZE_KB) {
            factor = SIZE_KB;
            unit = "kB";
        } else {
            return bytes + " Byte";
        }
        NumberFormat format = NumberFormat.getInstance(locale);
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        String asString = format.format((double) bytes / (double) factor);
        return asString + " " + unit;
    }

    /**
   * Gets a human readable String for a time.
   *
   * @param time The time in milliseconds.
   * @return The time as String.
   */
    public static String toTimeString(long time) {
        if (time == -1) {
            return "?";
        }
        long millis = time % 1000;
        time /= 1000;
        long secs = time % 60;
        time /= 60;
        long mins = time % 60;
        time /= 60;
        long hours = time;
        if (hours != 0) {
            return hours + ":" + ((mins > 9) ? "" : "0") + mins + ":" + ((secs > 9) ? "" : "0") + secs + " h";
        } else if (mins != 0) {
            return mins + ":" + ((secs > 9) ? "" : "0") + secs + " min";
        } else if (secs != 0) {
            NumberFormat format = NumberFormat.getInstance();
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            String asString = format.format(secs + millis / 1000.0);
            return asString + " sec";
        } else {
            return millis + " millis";
        }
    }

    /**
   * Konvertiert ein Date-Objekt in einen String mit dem Format
   * "YYYY-MM-DD HH:MM". Das ist n�tig, um ein eindeutiges und vom Menschen
   * lesbares Format zu haben.
   * <p>
   * Dieses Format ist mit Absicht nicht lokalisiert, um die Eindeutigkeit zu
   * gew�hrleisten. Die Lokalisierung muss die Suchmaske �bernehmen.
   *
   * @param lastModified Das zu konvertiernende Date-Objekt
   * @return Ein String mit dem Format "YYYY-MM-DD HH:MM"
   * @see #stringToLastModified(String)
   */
    public static String lastModifiedToString(Date lastModified) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastModified);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        StringBuffer buffer = new StringBuffer(16);
        buffer.append(year);
        buffer.append('-');
        if (month < 10) {
            buffer.append('0');
        }
        buffer.append(month);
        buffer.append('-');
        if (day < 10) {
            buffer.append('0');
        }
        buffer.append(day);
        buffer.append(' ');
        if (hour < 10) {
            buffer.append('0');
        }
        buffer.append(hour);
        buffer.append(':');
        if (minute < 10) {
            buffer.append('0');
        }
        buffer.append(minute);
        return buffer.toString();
    }

    /**
   * Konvertiert einen String mit dem Format "YYYY-MM-DD HH:MM" in ein
   * Date-Objekt.
   *
   * @param asString Der zu konvertierende String
   * @return Das konvertierte Date-Objekt.
   * @throws RegainException Wenn der String ein falsches Format hat.
   * @see #lastModifiedToString(Date)
   */
    public static Date stringToLastModified(String asString) throws RegainException {
        Calendar cal = Calendar.getInstance();
        try {
            int year = Integer.parseInt(asString.substring(0, 4));
            cal.set(Calendar.YEAR, year);
            int month = Integer.parseInt(asString.substring(5, 7));
            cal.set(Calendar.MONTH, month - 1);
            int day = Integer.parseInt(asString.substring(8, 10));
            cal.set(Calendar.DAY_OF_MONTH, day);
            int hour = Integer.parseInt(asString.substring(11, 13));
            cal.set(Calendar.HOUR_OF_DAY, hour);
            int minute = Integer.parseInt(asString.substring(14, 16));
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
        } catch (Throwable thr) {
            throw new RegainException("Last-modified-string has not the format" + "'YYYY-MM-DD HH:MM': " + asString, thr);
        }
        return cal.getTime();
    }

    /**
   * Splits a String into a string array.
   *
   * @param str The String to split.
   * @param delim The String that separates the items to split
   * @return An array the items.
   */
    public static String[] splitString(String str, String delim) {
        return splitString(str, delim, false);
    }

    /**
   * Splits a String into a string array.
   *
   * @param str The String to split.
   * @param delim The String that separates the items to split
   * @param trimSplits Specifies whether {@link String#trim()} should be called
   *        for every split.
   * @return An array the items.
   */
    public static String[] splitString(String str, String delim, boolean trimSplits) {
        StringTokenizer tokenizer = new StringTokenizer(str, delim);
        String[] searchFieldArr = new String[tokenizer.countTokens()];
        for (int i = 0; i < searchFieldArr.length; i++) {
            searchFieldArr[i] = tokenizer.nextToken();
            if (trimSplits) {
                searchFieldArr[i] = searchFieldArr[i].trim();
            }
        }
        return searchFieldArr;
    }

    /**
   * Gibt den systemspeziefischen Zeilenumbruch zur�ck.
   *
   * @return Der Zeilenumbruch.
   */
    public static String getLineSeparator() {
        if (mLineSeparator == null) {
            mLineSeparator = System.getProperty("line.separator");
        }
        return mLineSeparator;
    }

    /**
   * Returns the system's default encoding.
   *
   * @return the system's default encoding.
   */
    public static String getSystemDefaultEncoding() {
        if (mSystemDefaultEncoding == null) {
            mSystemDefaultEncoding = new InputStreamReader(System.in).getEncoding();
        }
        return mSystemDefaultEncoding;
    }

    /**
   * Checks whether the given String contains whitespace.
   *
   * @param str The String to check.
   * @return Whether the given String contains whitespace.
   */
    public static boolean containsWhitespace(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
   * Checks an array of group names.
   *
   * @param accessController The access controller that returned the array of
   *        group names.
   * @param groupArr The array of group names to check.
   * @throws RegainException If the array of group names is not valid.
   */
    public static void checkGroupArray(Object accessController, String[] groupArr) throws RegainException {
        if (groupArr == null) {
            throw new RegainException("Access controller " + accessController.getClass().getName() + " returned illegal " + "group array: null");
        } else {
            for (int i = 0; i < groupArr.length; i++) {
                if (RegainToolkit.containsWhitespace(groupArr[i])) {
                    throw new RegainException("Access controller " + accessController.getClass().getName() + " returned illegal " + "group name containing whitespace: '" + groupArr[i] + "'");
                }
            }
        }
    }

    /**
   * Loads a class and creates an instance.
   *
   * @param className The name of the class to load and create an instance of.
   * @param superClass The super class the class must extend.
   * @param classLoader The class loader to use for loading the class. May be
   *        <code>null</code>
   * @return An object of the class.
   * @throws RegainException If loading the class or creating the instance
   *         failed or if the class is no instance of the given super class.
   */
    public static Object createClassInstance(String className, Class superClass, ClassLoader classLoader) throws RegainException {
        Class clazz;
        try {
            if (classLoader == null) {
                clazz = Class.forName(className);
            } else {
                clazz = classLoader.loadClass(className);
            }
        } catch (ClassNotFoundException exc) {
            throw new RegainException("The class '" + className + "' does not exist", exc);
        }
        Object obj;
        try {
            obj = clazz.newInstance();
        } catch (Exception exc) {
            throw new RegainException("Error creating instance of class " + className, exc);
        }
        if (!superClass.isInstance(obj)) {
            throw new RegainException("The class " + className + " does not " + "implement " + superClass.getName());
        }
        return obj;
    }

    /**
   * Loads a class and creates an instance.
   *
   * @param className The name of the class to load and create an instance of.
   * @param superClass The super class the class must extend.
   * @param jarFileName The name of the jar file to load the class from.
   *        May be <code>null</code>.
   * @return An object of the class.
   * @throws RegainException If loading the class or creating the instance
   *         failed or if the class is no instance of the given super class.
   */
    public static Object createClassInstance(String className, Class superClass, String jarFileName) throws RegainException {
        ClassLoader classLoader = null;
        if (jarFileName != null) {
            File jarFile = new File(jarFileName);
            if (!jarFile.exists()) {
                throw new RegainException("Jar file does not exist: " + jarFile.getAbsolutePath());
            }
            try {
                classLoader = new URLClassLoader(new URL[] { jarFile.toURL() }, superClass.getClassLoader());
            } catch (MalformedURLException exc) {
                throw new RegainException("Creating class loader for " + "jar file failed: " + jarFile.getAbsolutePath(), exc);
            }
        }
        return createClassInstance(className, superClass, classLoader);
    }

    /**
   * Gets the file name that is described by a URL with the <code>file://</code>
   * protocol.
   *
   * @param url The URL to get the file name for.
   * @return The file name that matches the URL.
   * @throws RegainException If the URL's protocol isn't <code>file://</code>.
   */
    public static String urlToFileName(String url) throws RegainException {
        if (!url.startsWith("file://")) {
            throw new RegainException("URL must have the file:// protocol to get a " + "File for it");
        }
        String fileName = url.substring(7);
        return urlDecode(fileName, INDEX_ENCODING);
    }

    /**
   * Gets the file that is described by a URL with the <code>file://</code>
   * protocol.
   *
   * @param url The URL to get the file for.
   * @return The file that matches the URL.
   * @throws RegainException If the URL's protocol isn't <code>file://</code>.
   */
    public static File urlToFile(String url) throws RegainException {
        return new File(urlToFileName(url));
    }

    public static String urlToRealFileName(String url) throws RegainException {
        String metaFileStr = url.replaceAll("file://", "") + ".xml";
        File metaFile = new File(metaFileStr);
        if (metaFile.exists()) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Element metaDocElm = builder.parse(metaFile).getDocumentElement();
                NodeList nodes = metaDocElm.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node nextNode = nodes.item(i);
                    if (nextNode.getNodeType() == Node.ELEMENT_NODE && ((Element) nextNode).getNodeName().equals("fileName")) {
                        return nextNode.getFirstChild().getNodeValue();
                    }
                }
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (FactoryConfigurationError ex) {
                ex.printStackTrace();
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Not found file " + metaFileStr);
        }
        return null;
    }

    /**
   * Returns the URL of a file name.
   *
   * @param fileName The file name to get the URL for
   * @return The URL of the file.
   * @throws RegainException If URL-encoding failed.
   */
    public static String fileNameToUrl(String fileName) throws RegainException {
        fileName = urlEncode(fileName, INDEX_ENCODING);
        fileName = replace(fileName, "%2F", "/");
        fileName = replace(fileName, "%5C", "/");
        return "file://" + fileName;
    }

    /**
   * Returns the URL of a file.
   *
   * @param file The file to get the URL for
   * @return The URL of the file.
   * @throws RegainException If URL-encoding failed.
   */
    public static String fileToUrl(File file) throws RegainException {
        return fileNameToUrl(file.getAbsolutePath());
    }

    /**
   * Gets the canonical URL of a file (no symbolic links, normalised names etc).
   * Symbolic link detection may fail in certain situations, like for NFS file systems
   *
   * @param file The file to get the canonical URL for
   * @return The URL of the file.
   * @throws RegainException If URL-encoding failed.
   */
    public static String fileToCanonicalUrl(File file) throws RegainException {
        String canUrl = null;
        try {
            canUrl = file.getCanonicalPath();
        } catch (Exception e) {
            return null;
        }
        int pos = canUrl.indexOf(':') + 1;
        if (pos > 0 && pos < canUrl.length()) {
            canUrl = canUrl.substring(pos);
        }
        return fileNameToUrl(canUrl);
    }

    /**
   * URL-encodes a String.
   *
   * @param text The String to URL-encode.
   * @param encoding The encoding to use.
   * @return The URL-encoded String.
   * @throws RegainException If URL-encoding failed.
   */
    public static String urlEncode(String text, String encoding) throws RegainException {
        try {
            return URLEncoder.encode(text, encoding);
        } catch (UnsupportedEncodingException exc) {
            throw new RegainException("URL-encoding failed: '" + text + "'", exc);
        }
    }

    /**
   * URL-decodes a String.
   *
   * @param text The String to URL-decode.
   * @param encoding The encoding to use.
   * @return The URL-decoded String.
   * @throws RegainException If URL-decoding failed.
   */
    public static String urlDecode(String text, String encoding) throws RegainException {
        try {
            return URLDecoder.decode(text, encoding);
        } catch (UnsupportedEncodingException exc) {
            throw new RegainException("URL-decoding failed: '" + text + "'", exc);
        }
    }

    /**
   * An analyzer that changes a document in lowercase before delivering
   * it to a nested analyzer. For the field "groups" an analyzer is used that
   * only tokenizes the input without stemming the tokens.
   */
    private static class WrapperAnalyzer extends Analyzer {

        /** The analyzer to use for a field that shouldn't be stemmed. */
        private Analyzer mNoStemmingAnalyzer;

        /** The nested analyzer. */
        private Analyzer mNestedAnalyzer;

        /** The names of the fields that should not be tokenized. */
        private HashSet mUntokenizedFieldNames;

        /**
     * Creates a new instance of WrapperAnalyzer.
     *
     * @param nestedAnalyzer The nested analyzer.
     * @param untokenizedFieldNames The names of the fields that should not be
     *        tokenized.
     */
        public WrapperAnalyzer(Analyzer nestedAnalyzer, String[] untokenizedFieldNames) {
            mNoStemmingAnalyzer = new WhitespaceAnalyzer();
            mNestedAnalyzer = nestedAnalyzer;
            mUntokenizedFieldNames = new HashSet();
            for (int i = 0; i < untokenizedFieldNames.length; i++) {
                mUntokenizedFieldNames.add(untokenizedFieldNames[i]);
            }
        }

        /**
     * Creates a TokenStream which tokenizes all the text in the provided
     * Reader.
     */
        public TokenStream tokenStream(String fieldName, Reader reader) {
            boolean useStemming = true;
            if (fieldName.equals("groups") || mUntokenizedFieldNames.contains(fieldName)) {
                useStemming = false;
            }
            if (useStemming) {
                Reader lowercasingReader = new LowercasingReader(reader);
                return mNestedAnalyzer.tokenStream(fieldName, lowercasingReader);
            } else {
                return mNoStemmingAnalyzer.tokenStream(fieldName, reader);
            }
        }
    }

    /**
   * Liest alle Zeichen von einem eingebetteten Reader in Kleinschreibung.
   *
   * @author Til Schneider, www.murfman.de
   */
    private static class LowercasingReader extends Reader {

        /** Der eingebettete Reader. */
        private Reader mNestedReader;

        /**
     * Erzeugt eine neue LowercasingReader-Instanz.
     *
     * @param nestedReader Der Reader, von dem die Daten kommen, die in
     *        Kleinschreibung gewandelt werden sollen.
     */
        public LowercasingReader(Reader nestedReader) {
            mNestedReader = nestedReader;
        }

        /**
     * Schlie�t den eingebetteten Reader.
     *
     * @throws IOException Wenn der eingebettete Reader nicht geschlossen werden
     *         konnte.
     */
        public void close() throws IOException {
            mNestedReader.close();
        }

        /**
     * Liest Daten vom eingebetteten Reader und wandelt sie in Kleinschreibung.
     *
     * @param cbuf Der Puffer, in den die gelesenen Daten geschrieben werden
     *        sollen
     * @param off Der Offset im Puffer, ab dem geschreiben werden soll.
     * @param len Die max. Anzahl von Zeichen, die geschrieben werden soll.
     * @return Die Anzahl von Zeichen, die tats�chlich geschrieben wurde, bzw.
     *         <code>-1</code>, wenn keine Daten mehr verf�gbar sind.
     * @throws IOException Wenn nicht vom eingebetteten Reader gelesen werden
     *         konnte.
     */
        public int read(char[] cbuf, int off, int len) throws IOException {
            int charCount = mNestedReader.read(cbuf, off, len);
            if (charCount != -1) {
                for (int i = off; i < off + charCount; i++) {
                    cbuf[i] = Character.toLowerCase(cbuf[i]);
                }
            }
            return charCount;
        }
    }
}
