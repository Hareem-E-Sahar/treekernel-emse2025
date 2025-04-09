package de.unkrig.diff;

import static java.util.logging.Level.INFO;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.incava.util.diff.Difference;
import de.unkrig.commons.io.ByteFilterInputStream;
import de.unkrig.commons.io.UnclosableInputStream;
import de.unkrig.commons.lang.ExceptionUtil;
import de.unkrig.commons.util.ConsumerUtil;
import de.unkrig.commons.util.ConsumerWhichThrows;
import de.unkrig.commons.util.Predicate;
import de.unkrig.commons.util.logging.LogUtil;
import de.unkrig.diff.TreeComparator.LeafNode;
import de.unkrig.diff.TreeComparator.Node;

public class Diff {

    static final Logger LOGGER = Logger.getLogger(Diff.class.getName());

    private static final String ZIP_SEPARATOR = "!";

    static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static class LineEquivalence {

        public final Predicate<String> namePattern;

        public final Pattern lineRegex;

        public LineEquivalence(Predicate<String> namePattern, Pattern lineRegex) {
            this.namePattern = namePattern;
            this.lineRegex = lineRegex;
        }
    }

    public enum DiffMode {

        QUIET, NORMAL, CONTEXT, UNIFIED
    }

    private final List<Predicate<String>> excludedNames = new ArrayList<Predicate<String>>();

    private final List<Pattern> equivalentNames = new ArrayList<Pattern>();

    private final Collection<LineEquivalence> equivalentLines = new ArrayList<LineEquivalence>();

    boolean ignoreWhitespace;

    private boolean zip;

    private boolean disassembleClassFiles;

    private boolean disassembleClassFilesButHideLines;

    private boolean disassembleClassFilesButHideVars;

    private String optionalEncoding;

    DiffMode diffMode = DiffMode.NORMAL;

    int contextSize = 3;

    File file1 = null;

    File file2 = null;

    private ConsumerWhichThrows<String, IOException> out = ConsumerUtil.cast(LogUtil.logConsumer(LOGGER, INFO, null));

    int differenceCount = 0;

    public void setIgnoreWhitespace(boolean value) {
        this.ignoreWhitespace = value;
    }

    public void setZip(boolean value) {
        this.zip = value;
    }

    public void setDisassembleClassFiles(boolean value) {
        this.disassembleClassFiles = value;
    }

    public void setDisassembleClassFilesButHideLines(boolean value) {
        this.disassembleClassFilesButHideLines = value;
    }

    public void setDisassembleClassFilesButHideVars(boolean value) {
        this.disassembleClassFilesButHideVars = value;
    }

    public void setEncoding(String value) {
        this.optionalEncoding = value;
    }

    public void setDiffMode(DiffMode value) {
        this.diffMode = value;
    }

    public void setContextSize(int value) {
        this.contextSize = value;
    }

    public void setOut(File value) throws IOException {
        this.out = ConsumerUtil.lineConsumer(value, false);
    }

    public void setFile1(File file) {
        this.file1 = file;
    }

    public void setFile2(File file) {
        this.file2 = file;
    }

    public void addExcludedName(Predicate<String> name) {
        this.excludedNames.add(name);
    }

    public void addEquivalentName(Pattern name) {
        this.equivalentNames.add(name);
    }

    public void addEquivalentLine(LineEquivalence lineEquivalence) {
        this.equivalentLines.add(lineEquivalence);
    }

    public void execute() throws IOException {
        try {
            LOGGER.config("Scanning '" + this.file1 + "'...");
            Node<String> node1 = this.newNode("", this.file1);
            LOGGER.config("Scanning '" + this.file2 + "'...");
            Node<String> node2 = this.newNode("", this.file2);
            LOGGER.config("Computing differences...");
            this.diff(node1, node2);
            if (this.differenceCount == 0) {
                LOGGER.config("No differences found.");
            } else {
                LOGGER.config(this.differenceCount + " differences found.");
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, null, ioe);
            throw ioe;
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, null, re);
            throw re;
        }
    }

    private String normalize(String name) {
        for (Pattern nameEquivalence : Diff.this.equivalentNames) {
            Matcher matcher = nameEquivalence.matcher(name.substring(1));
            if (matcher.matches()) {
                name = name.substring(0, 1);
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    name += matcher.group(i);
                }
            }
        }
        return name;
    }

    /**
     * Print the differences between the two entry sets to STDOUT.
     */
    private void diff(Node<String> node1, Node<String> node2) throws IOException {
        final EntryFinder entryFinder1 = new EntryFinder();
        final EntryFinder entryFinder2 = new EntryFinder();
        TreeComparator<String> treeComparator = new TreeComparator<String>() {

            @Override
            protected void nodeAdded(Node<String> node) {
                ++Diff.this.differenceCount;
                if (Diff.this.diffMode == DiffMode.QUIET) {
                    out("+ " + node.toString().substring(1));
                } else {
                    out("File added:   " + node.toString().substring(1));
                }
            }

            @Override
            protected void nodeDeleted(Node<String> node) {
                ++Diff.this.differenceCount;
                if (Diff.this.diffMode == DiffMode.QUIET) {
                    out("- " + node.toString().substring(1));
                } else {
                    out("File deleted: " + node.toString().substring(1));
                }
            }

            @Override
            protected void nonLeafNodeChangedToLeafNode(Node<String> node1, Node<String> node2) {
                ++Diff.this.differenceCount;
                if (Diff.this.diffMode == DiffMode.QUIET) {
                    out("! " + node2.toString().substring(1));
                } else {
                    out("File type:    " + node2.toString().substring(1));
                }
            }

            @Override
            protected void leafNodeChangedToNonLeafNode(Node<String> node1, Node<String> node2) {
                ++Diff.this.differenceCount;
                if (Diff.this.diffMode == DiffMode.QUIET) {
                    out("! " + node2.toString().substring(1));
                } else {
                    out("File type:    " + node2.toString().substring(1));
                }
            }

            @Override
            protected void leafNodeUnchanged(Node<String> node1, Node<String> node2) {
                if (!node1.toString().equals(node2.toString())) {
                    LOGGER.config("Entry renamed from '" + node1 + "' to '" + node2 + "'");
                } else {
                    LOGGER.fine("Entry '" + node1 + "' have identical size and CRC");
                }
            }

            @Override
            protected void leafNodeChanged(Node<String> node1, Node<String> node2) throws IOException {
                LOGGER.fine("'" + node1.getName() + "' vs. '" + node2.getName() + "'");
                String name1 = node1.getName();
                String name2 = node2.getName();
                Line[] lines1 = readAllLines(entryFinder1, Diff.this.file1 + name1);
                Line[] lines2 = readAllLines(entryFinder2, Diff.this.file2 + name2);
                List<Difference> diffs = new org.incava.util.diff.Diff<Line>(lines1, lines2).diff();
                if (diffs.isEmpty()) {
                    LOGGER.fine("'" + name1.substring(1) + "' has equivalent contents");
                    return;
                }
                ++Diff.this.differenceCount;
                switch(Diff.this.diffMode) {
                    case QUIET:
                        out(name1.length() == 0 ? "Files differ" : "! " + name2.substring(1));
                        break;
                    case NORMAL:
                        if (name1.length() > 0) out("File changed: " + name2.substring(1));
                        Diff.this.normalDiff(lines1, lines2, diffs);
                        break;
                    case CONTEXT:
                        if (name1.length() > 0) out("File changed: " + name2.substring(1));
                        out("*** " + Diff.this.file1 + node1);
                        out("--- " + Diff.this.file2 + node2);
                        Diff.this.contextDiff(lines1, lines2, diffs);
                        break;
                    case UNIFIED:
                        if (name1.length() > 0) out("File changed: " + name2.substring(1));
                        out("--- " + Diff.this.file1 + node1);
                        out("+++ " + Diff.this.file2 + node2);
                        Diff.this.unifiedDiff(lines1, lines2, diffs);
                        break;
                }
            }
        };
        try {
            treeComparator.compare(node1, node2);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof IOException) throw (IOException) targetException;
            if (targetException instanceof RuntimeException) throw (RuntimeException) targetException;
            if (targetException instanceof Error) throw (Error) targetException;
            throw new IllegalStateException(e);
        }
    }

    void out(String text) {
        try {
            this.out.consume(text);
        } catch (IOException ioe) {
            throw new RuntimeException(null, ioe);
        }
    }

    public static class EntryFinder {

        private NestedZipFileStream nzfs;

        private String currentZipFileName;

        /**
         * Opens a file or an entry of a (possible nested) ZIP archive. The returned {@link InputStream} must be closed
         * be the caller.
         *
         * @param name                   E.g. 'c:/dir/file.zip!dir/file.zip/dir/file'
         * @throws FileNotFoundException
         */
        public InputStream open(String name) throws IOException {
            String[] entryNames = name.split(ZIP_SEPARATOR);
            if (entryNames.length == 1) return new FileInputStream(entryNames[0]);
            if (this.nzfs != null && !entryNames[0].equals(this.currentZipFileName)) {
                this.nzfs.close();
                this.nzfs = null;
                this.currentZipFileName = null;
            }
            if (this.nzfs == null) {
                this.nzfs = new NestedZipFileStream(new File(entryNames[0]));
                this.currentZipFileName = entryNames[0];
            }
            {
                String[] tmp = new String[entryNames.length - 1];
                System.arraycopy(entryNames, 1, tmp, 0, tmp.length);
                entryNames = tmp;
            }
            if (this.nzfs.getEntry(entryNames) == null) {
                throw new FileNotFoundException(name);
            }
            return new FilterInputStream(this.nzfs) {

                @Override
                public void close() {
                }
            };
        }

        public void close() throws IOException {
            if (this.nzfs != null) {
                this.nzfs.close();
                this.nzfs = null;
                this.currentZipFileName = null;
            }
        }
    }

    /**
     * Format a list of {@link Difference}s in "normal diff style".
     */
    void normalDiff(Line[] lines1, Line[] lines2, List<Difference> diffs) {
        for (Difference diff : diffs) {
            int delStart = diff.getDeletedStart();
            int delEnd = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd = diff.getAddedEnd();
            out(toString(delStart, delEnd) + (delEnd == Difference.NONE ? "a" : addEnd == Difference.NONE ? "d" : "c") + toString(addStart, addEnd));
            if (delEnd != Difference.NONE) {
                printLines(delStart, delEnd, "< ", lines1);
                if (addEnd != Difference.NONE) out("---");
            }
            if (addEnd != Difference.NONE) {
                printLines(addStart, addEnd, "> ", lines2);
            }
        }
    }

    /**
     * Format a list of {@link Difference}s in "context diff" style.
     */
    void contextDiff(final Line[] lines1, final Line[] lines2, List<Difference> diffs) {
        chunkedDiff(diffs, new ChunkPrinter() {

            public void print(List<Difference> chunk) {
                out("***************");
                Difference firstDifference = chunk.get(0);
                Difference lastDifference = chunk.get(chunk.size() - 1);
                {
                    int boc1 = Math.max(0, firstDifference.getDeletedStart() - Diff.this.contextSize);
                    int eoc1 = Math.min((lastDifference.getDeletedEnd() == Difference.NONE ? lastDifference.getDeletedStart() + Diff.this.contextSize - 1 : lastDifference.getDeletedEnd() + Diff.this.contextSize), lines1.length - 1);
                    out("*** " + Diff.toString(boc1, eoc1) + " ****");
                    for (Difference d : chunk) {
                        printLines(boc1, d.getDeletedStart() - 1, "  ", lines1);
                        if (d.getDeletedEnd() == Difference.NONE) {
                            boc1 = d.getDeletedStart();
                        } else {
                            printLines(d.getDeletedStart(), d.getDeletedEnd(), d.getAddedEnd() == Difference.NONE ? "- " : "! ", lines1);
                            boc1 = d.getDeletedEnd() + 1;
                        }
                    }
                    printLines(boc1, eoc1, "  ", lines1);
                }
                {
                    int boc2 = Math.max(0, firstDifference.getAddedStart() - Diff.this.contextSize);
                    int eoc2 = Math.min((lastDifference.getAddedEnd() == Difference.NONE ? lastDifference.getAddedStart() + Diff.this.contextSize - 1 : lastDifference.getAddedEnd() + Diff.this.contextSize), lines2.length - 1);
                    out("--- " + Diff.toString(boc2, eoc2) + " ----");
                    for (Difference d : chunk) {
                        printLines(boc2, d.getAddedStart() - 1, "  ", lines2);
                        if (d.getAddedEnd() == Difference.NONE) {
                            boc2 = d.getAddedStart();
                        } else {
                            printLines(d.getAddedStart(), d.getAddedEnd(), d.getDeletedEnd() == Difference.NONE ? "+ " : "! ", lines2);
                            boc2 = d.getAddedEnd() + 1;
                        }
                    }
                    printLines(boc2, eoc2, "  ", lines2);
                }
            }
        });
    }

    /**
     * Format a list of {@link Difference}s in "context diff" style.
     */
    void unifiedDiff(final Line[] lines1, final Line[] lines2, List<Difference> diffs) {
        chunkedDiff(diffs, new ChunkPrinter() {

            public void print(List<Difference> chunk) {
                Difference firstDifference = chunk.get(0);
                Difference lastDifference = chunk.get(chunk.size() - 1);
                int boc1 = Math.max(0, firstDifference.getDeletedStart() - Diff.this.contextSize);
                int eoc1 = Math.min((lastDifference.getDeletedEnd() == Difference.NONE ? lastDifference.getDeletedStart() + Diff.this.contextSize - 1 : lastDifference.getDeletedEnd() + Diff.this.contextSize), lines1.length - 1);
                int boc2 = Math.max(0, firstDifference.getAddedStart() - Diff.this.contextSize);
                int eoc2 = Math.min((lastDifference.getAddedEnd() == Difference.NONE ? lastDifference.getAddedStart() + Diff.this.contextSize - 1 : lastDifference.getAddedEnd() + Diff.this.contextSize), lines2.length - 1);
                out("@@ -" + (boc1 + 1) + "," + (eoc1 - boc1 + 1) + " +" + (boc2 + 1) + "," + (eoc2 - boc2 + 1) + " @@");
                for (Difference d : chunk) {
                    printLines(boc1, d.getDeletedStart() - 1, " ", lines1);
                    if (d.getDeletedEnd() == Difference.NONE) {
                        boc1 = d.getDeletedStart();
                    } else {
                        printLines(d.getDeletedStart(), d.getDeletedEnd(), "-", lines1);
                        boc1 = d.getDeletedEnd() + 1;
                    }
                    if (d.getAddedEnd() == Difference.NONE) {
                        boc2 = d.getAddedStart();
                    } else {
                        printLines(d.getAddedStart(), d.getAddedEnd(), "+", lines2);
                        boc2 = d.getAddedEnd() + 1;
                    }
                }
                printLines(boc1, eoc1, " ", lines1);
            }
        });
    }

    private void chunkedDiff(List<Difference> diffs, ChunkPrinter chunkPrinter) {
        Iterator<Difference> it = diffs.iterator();
        Difference lookahead = it.hasNext() ? it.next() : null;
        while (lookahead != null) {
            List<Difference> agg = new ArrayList<Difference>();
            agg.add(lookahead);
            for (; ; ) {
                int afterDeletion = (lookahead.getDeletedEnd() == Difference.NONE ? lookahead.getDeletedStart() : lookahead.getDeletedEnd() + 1);
                lookahead = it.hasNext() ? it.next() : null;
                if (lookahead == null) break;
                if (lookahead.getDeletedStart() - afterDeletion <= 2 * Diff.this.contextSize) {
                    agg.add(lookahead);
                } else {
                    break;
                }
            }
            chunkPrinter.print(agg);
        }
    }

    interface ChunkPrinter {

        void print(List<Difference> chunk);
    }

    static String toString(int start, int end) {
        StringBuilder sb = new StringBuilder();
        sb.append(end == Difference.NONE ? start : start + 1);
        if (end != Difference.NONE && start != end) {
            sb.append(",").append(end + 1);
        }
        return sb.toString();
    }

    void printLines(int start, int end, String indicator, Line[] lines) {
        for (int lnum = start; lnum <= end; ++lnum) {
            out(indicator + lines[lnum]);
        }
    }

    /**
     * Reads the contents of the entry with the given {@code name} and transforms it to an array of {@link Line}s.
     * Honors {@link #disassembleClassFiles}, {@link #equivalentLines} and {@link #ignoreWhitespace}.
     *
     * @param name E.g. ".class" files are filtered through a bytecode disassembler
     */
    Line[] readAllLines(EntryFinder entryFinder, String name) throws IOException {
        InputStream is = entryFinder.open(name);
        try {
            return readAllLines(is, name);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Reads the contents of the given {@link InputStream} and transforms it to an array of {@link Line}s. The
     * {@link InputStream} is not closed. Honors {@link #disassembleClassFiles}, {@link #equivalentLines} and {@link
     * #ignoreWhitespace}.
     *
     * @param name E.g. ".class" files are filtered through a bytecode disassembler
     */
    private Line[] readAllLines(InputStream is, String name) throws IOException {
        if (this.disassembleClassFiles && name.endsWith(".class")) {
            DisassemblerByteFilter disassemblerByteFilter = new DisassemblerByteFilter();
            disassemblerByteFilter.setHideLines(this.disassembleClassFilesButHideLines);
            disassemblerByteFilter.setHideVars(this.disassembleClassFilesButHideVars);
            is = new ByteFilterInputStream(is, disassemblerByteFilter);
        }
        BufferedReader br = new BufferedReader(this.optionalEncoding == null ? new InputStreamReader(is) : new InputStreamReader(is, this.optionalEncoding));
        Collection<Pattern> equivalences = new ArrayList<Pattern>();
        for (LineEquivalence le : this.equivalentLines) {
            if (le.namePattern.evaluate(name)) {
                equivalences.add(le.lineRegex);
            }
        }
        List<Line> contents = new ArrayList<Line>();
        try {
            for (; ; ) {
                Line line = this.readLine(br, equivalences);
                if (line == null) break;
                contents.add(line);
            }
        } catch (IOException ioe) {
            throw ExceptionUtil.wrap("Reading '" + name + "'", ioe);
        } catch (RuntimeException re) {
            throw ExceptionUtil.wrap("Reading '" + name + "'", re);
        }
        return contents.toArray(new Line[contents.size()]);
    }

    /**
     * @return {@code null} on end-of-input
     */
    private Line readLine(BufferedReader br, Collection<Pattern> equivalences) throws IOException {
        final String line = br.readLine();
        if (line == null) return null;
        return new Line(line, equivalences);
    }

    interface Checksummable {

        /**
         * Updates the given {@link Checksum} from this object.
         */
        void update(Checksum checksum);
    }

    /**
     * Representation of a line read from a stream. Honors {@link Diff#ignoreRegexes} and {@link
     * Diff#ignoreWhitespace}.
     */
    class Line implements Checksummable {

        private final String text;

        private byte[] value;

        public Line(String text, Collection<Pattern> equivalences) {
            try {
                this.text = text;
                if (Diff.this.ignoreWhitespace) {
                    text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
                }
                for (Pattern p : equivalences) {
                    Matcher matcher = p.matcher(text);
                    if (matcher.find()) {
                        if (matcher.groupCount() == 0) {
                            this.value = IGNORED_LINE;
                            return;
                        }
                        StringBuffer sb = new StringBuffer();
                        do {
                            String replacement = "";
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                replacement += "$" + i;
                            }
                            matcher.appendReplacement(sb, replacement);
                        } while (matcher.find());
                        matcher.appendTail(sb);
                        text = sb.toString();
                    }
                }
                this.value = text.getBytes("UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Line)) return false;
            Line that = (Line) o;
            return this == that || Arrays.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.value);
        }

        /**
         * Returns the line's text.
         */
        @Override
        public String toString() {
            return this.text;
        }

        /**
         * Updates the given {@link Checksum} from this object's value.
         */
        public void update(Checksum checksum) {
            checksum.update(this.value, 0, this.value.length);
        }
    }

    static final byte[] IGNORED_LINE = { 0x7f, 0x2f, 0x19 };

    /**
     * If the {@code file} is a directory, a {@link NonLeafNode} is created, populated from the directory's members,
     * and returned.
     * <p>
     * If the {@code file} is in ZIP format, a {@link NonLeafNode} is created, populated from the ZIP file's entries,
     * and returned.
     * <p>
     * If the {@code file} is not in ZIP format, a {@link PlainFileNode} is created and returned.
     *
     * @param name E.g. "", "/dir", "/dir/file", "!dir/file", "!dir/file.zip!dir/file"
     */
    private Node<String> newNode(String name, File file) throws IOException {
        if (file.isDirectory()) {
            final SortedMap<String, Node<String>> children = new TreeMap<String, Node<String>>();
            MEMBERS: for (File member : file.listFiles()) {
                String memberName = name + '/' + member.getName();
                for (Predicate<String> glob : this.excludedNames) {
                    if (glob.evaluate(memberName.substring(1))) continue MEMBERS;
                }
                children.put(normalize(memberName), this.newNode(memberName, member));
            }
            return new NonLeafNode(name, children);
        }
        TRY_ZIP: if (this.zip) {
            ZipFile zipFile;
            try {
                zipFile = new ZipFile(file);
            } catch (ZipException ze) {
                break TRY_ZIP;
            }
            try {
                return new NonLeafNode(name, this.getNodes(name, zipFile));
            } catch (IOException ioe) {
                throw ExceptionUtil.wrap("ZIP file '" + file + "'", ioe);
            } catch (RuntimeException re) {
                throw ExceptionUtil.wrap("ZIP file '" + file + "'", re);
            } finally {
                zipFile.close();
            }
        }
        return new PlainFileNode(name, file);
    }

    private class NonLeafNode implements Node<String> {

        private final String name;

        private final SortedMap<String, Node<String>> children;

        NonLeafNode(String name, SortedMap<String, Node<String>> children) {
            this.name = name;
            this.children = children;
        }

        public SortedMap<String, Node<String>> children() {
            return this.children;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Equality is defined in terms of contents equality.
     */
    private class PlainFileNode extends LeafNode<String> {

        private final String name;

        private final long size;

        private final long crc32;

        private final long modificationTime;

        PlainFileNode(String name, long size, long crc32, long modificationTime) {
            this.name = name;
            this.size = size;
            this.crc32 = crc32;
            this.modificationTime = modificationTime;
        }

        PlainFileNode(String name, File file) throws IOException {
            this.name = name;
            FileInputStream is = new FileInputStream(file);
            try {
                CRC32 crc32 = new CRC32();
                long size = 0L;
                byte[] buffer = new byte[8192];
                for (; ; ) {
                    int count = is.read(buffer);
                    if (count == -1) break;
                    crc32.update(buffer, 0, count);
                    size += count;
                }
                this.size = size;
                this.crc32 = (int) crc32.getValue();
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
            this.modificationTime = file.lastModified();
        }

        PlainFileNode(String name, InputStream is, long modificationTime) throws IOException {
            this.name = name;
            this.modificationTime = modificationTime;
            CRC32 crc32 = new CRC32();
            long size = 0L;
            byte[] buffer = new byte[8192];
            for (; ; ) {
                int count = is.read(buffer);
                if (count == -1) break;
                crc32.update(buffer, 0, count);
                size += count;
            }
            this.size = size;
            this.crc32 = (int) crc32.getValue();
        }

        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PlainFileNode)) return false;
            PlainFileNode that = (PlainFileNode) o;
            return this.size == that.size && this.crc32 == that.crc32;
        }

        @Override
        public int hashCode() {
            return (int) this.size ^ (int) this.crc32;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Gets nodes for all entries of the given {@link ZipFile}.
     *
     * @param name Name of this ZIP file, e.g. "" (top-level ZIP file), "/file.zip", "/dir/file.zip"
     */
    private SortedMap<String, Node<String>> getNodes(String name, ZipFile zipFile) throws IOException {
        final SortedMap<String, Node<String>> nodes = new TreeMap<String, Node<String>>();
        ENTRIES: for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements(); ) {
            final ZipEntry zipEntry = en.nextElement();
            if (zipEntry.isDirectory()) continue;
            String entryName = name + ZIP_SEPARATOR + zipEntry.getName();
            for (Predicate<String> glob : this.excludedNames) {
                if (glob.evaluate(entryName.substring(1))) continue ENTRIES;
            }
            {
                ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(zipEntry));
                try {
                    final SortedMap<String, Node<String>> children = getNodes(entryName, zis);
                    if (children != null) {
                        nodes.put(normalize(entryName), new NonLeafNode(entryName, children));
                        continue;
                    }
                } catch (IOException ioe) {
                    throw ExceptionUtil.wrap("Calculating footprint of '" + entryName + "'", ioe);
                } catch (RuntimeException re) {
                    throw new RuntimeException("Calculating footprint of '" + entryName + "': " + re.getMessage(), re);
                } finally {
                    zis.close();
                }
            }
            Node<String> node;
            if (zipEntry.getSize() != -1L && zipEntry.getCrc() != -1L) {
                node = new PlainFileNode(entryName, zipEntry.getSize(), zipEntry.getCrc(), zipEntry.getTime());
            } else {
                InputStream is = zipFile.getInputStream(zipEntry);
                try {
                    node = new PlainFileNode(entryName, is, zipEntry.getTime());
                } finally {
                    is.close();
                }
            }
            put(normalize(entryName), node, nodes);
        }
        return nodes;
    }

    /**
     * Gets the nodes for all entries of the given {@link ZipInputStream}.
     *
     * @param name Name associated with this {@link ZipInputStream}, e.g. "!dir/file.zip", "!dir/file.zip!dir/file"
     * @return     {@code null} iff the stream is not in ZIP format
     */
    private SortedMap<String, Node<String>> getNodes(String name, ZipInputStream zis) throws IOException {
        ZipEntry zipEntry = zis.getNextEntry();
        if (zipEntry == null) return null;
        final SortedMap<String, Node<String>> nodes = new TreeMap<String, Node<String>>();
        ENTRIES: for (; zipEntry != null; zipEntry = zis.getNextEntry()) {
            String entryName = name + ZIP_SEPARATOR + zipEntry.getName();
            try {
                if (zipEntry.isDirectory()) continue;
                for (Predicate<String> glob : this.excludedNames) {
                    if (glob.evaluate(entryName.substring(1))) continue ENTRIES;
                }
                InputStream eis = zis;
                if (!eis.markSupported()) eis = new BufferedInputStream(eis);
                eis.mark(30);
                final SortedMap<String, Node<String>> children;
                {
                    ZipInputStream subzis = new ZipInputStream(new UnclosableInputStream(eis));
                    try {
                        children = this.getNodes(entryName, subzis);
                    } finally {
                        subzis.close();
                    }
                }
                if (children != null) {
                    nodes.put(normalize(entryName), new NonLeafNode(entryName, children));
                    continue;
                }
                Node<String> node;
                if (zipEntry.getSize() != -1L && zipEntry.getCrc() != -1L) {
                    node = new PlainFileNode(entryName, zipEntry.getSize(), zipEntry.getCrc(), zipEntry.getTime());
                } else {
                    eis.reset();
                    node = new PlainFileNode(entryName, eis, zipEntry.getTime());
                }
                put(normalize(entryName), node, nodes);
            } catch (IOException ioe) {
                throw ExceptionUtil.wrap("Processing '" + entryName + "'", ioe);
            } catch (RuntimeException re) {
                throw ExceptionUtil.wrap("Processing '" + entryName + "'", re);
            }
        }
        return nodes;
    }

    private static void put(String key, Node<String> node, Map<String, Node<String>> nodes_out) throws IOException {
        Node<String> oldNode = nodes_out.get(key);
        if (oldNode != null) {
            if (oldNode.toString().equals(node.toString()) && oldNode.toString().contains("!")) {
                ;
            } else {
                throw new IOException("Two or more entries have equivalent names: '" + oldNode + "' and '" + node + "'");
            }
        }
        nodes_out.put(key, node);
    }
}
