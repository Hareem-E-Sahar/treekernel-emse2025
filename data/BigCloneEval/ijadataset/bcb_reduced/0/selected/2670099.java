package org.apache.lucene.index;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Arrays;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.search.Similarity;

final class DocumentWriter {

    private Analyzer analyzer;

    private Directory directory;

    private Similarity similarity;

    private FieldInfos fieldInfos;

    private int maxFieldLength;

    private int termIndexInterval = IndexWriter.DEFAULT_TERM_INDEX_INTERVAL;

    private PrintStream infoStream;

    /** This ctor used by test code only.
   *
   * @param directory The directory to write the document information to
   * @param analyzer The analyzer to use for the document
   * @param similarity The Similarity function
   * @param maxFieldLength The maximum number of tokens a field may have
   */
    DocumentWriter(Directory directory, Analyzer analyzer, Similarity similarity, int maxFieldLength) {
        this.directory = directory;
        this.analyzer = analyzer;
        this.similarity = similarity;
        this.maxFieldLength = maxFieldLength;
    }

    DocumentWriter(Directory directory, Analyzer analyzer, IndexWriter writer) {
        this.directory = directory;
        this.analyzer = analyzer;
        this.similarity = writer.getSimilarity();
        this.maxFieldLength = writer.getMaxFieldLength();
        this.termIndexInterval = writer.getTermIndexInterval();
    }

    final void addDocument(String segment, Document doc) throws IOException {
        fieldInfos = new FieldInfos();
        fieldInfos.add(doc);
        fieldInfos.write(directory, segment + ".fnm");
        FieldsWriter fieldsWriter = new FieldsWriter(directory, segment, fieldInfos);
        try {
            fieldsWriter.addDocument(doc);
        } finally {
            fieldsWriter.close();
        }
        postingTable.clear();
        fieldLengths = new int[fieldInfos.size()];
        fieldPositions = new int[fieldInfos.size()];
        fieldOffsets = new int[fieldInfos.size()];
        fieldBoosts = new float[fieldInfos.size()];
        Arrays.fill(fieldBoosts, doc.getBoost());
        invertDocument(doc);
        Posting[] postings = sortPostingTable();
        writePostings(postings, segment);
        writeNorms(segment);
    }

    private final Hashtable postingTable = new Hashtable();

    private int[] fieldLengths;

    private int[] fieldPositions;

    private int[] fieldOffsets;

    private float[] fieldBoosts;

    private final void invertDocument(Document doc) throws IOException {
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            String fieldName = field.name();
            int fieldNumber = fieldInfos.fieldNumber(fieldName);
            int length = fieldLengths[fieldNumber];
            int position = fieldPositions[fieldNumber];
            if (length > 0) position += analyzer.getPositionIncrementGap(fieldName);
            int offset = fieldOffsets[fieldNumber];
            if (field.isIndexed()) {
                if (!field.isTokenized()) {
                    String stringValue = field.stringValue();
                    if (field.isStoreOffsetWithTermVector()) addPosition(fieldName, stringValue, position++, new TermVectorOffsetInfo(offset, offset + stringValue.length())); else addPosition(fieldName, stringValue, position++, null);
                    offset += stringValue.length();
                    length++;
                } else {
                    Reader reader;
                    if (field.readerValue() != null) reader = field.readerValue(); else if (field.stringValue() != null) reader = new StringReader(field.stringValue()); else throw new IllegalArgumentException("field must have either String or Reader value");
                    TokenStream stream = analyzer.tokenStream(fieldName, reader);
                    try {
                        Token lastToken = null;
                        for (Token t = stream.next(); t != null; t = stream.next()) {
                            position += (t.getPositionIncrement() - 1);
                            if (field.isStoreOffsetWithTermVector()) addPosition(fieldName, t.termText(), position++, new TermVectorOffsetInfo(offset + t.startOffset(), offset + t.endOffset())); else addPosition(fieldName, t.termText(), position++, null);
                            lastToken = t;
                            if (++length > maxFieldLength) {
                                if (infoStream != null) infoStream.println("maxFieldLength " + maxFieldLength + " reached, ignoring following tokens");
                                break;
                            }
                        }
                        if (lastToken != null) offset += lastToken.endOffset() + 1;
                    } finally {
                        stream.close();
                    }
                }
                fieldLengths[fieldNumber] = length;
                fieldPositions[fieldNumber] = position;
                fieldBoosts[fieldNumber] *= field.getBoost();
                fieldOffsets[fieldNumber] = offset;
            }
        }
    }

    private final Term termBuffer = new Term("", "");

    private final void addPosition(String field, String text, int position, TermVectorOffsetInfo offset) {
        termBuffer.set(field, text);
        Posting ti = (Posting) postingTable.get(termBuffer);
        if (ti != null) {
            int freq = ti.freq;
            if (ti.positions.length == freq) {
                int[] newPositions = new int[freq * 2];
                int[] positions = ti.positions;
                for (int i = 0; i < freq; i++) newPositions[i] = positions[i];
                ti.positions = newPositions;
            }
            ti.positions[freq] = position;
            if (offset != null) {
                if (ti.offsets.length == freq) {
                    TermVectorOffsetInfo[] newOffsets = new TermVectorOffsetInfo[freq * 2];
                    TermVectorOffsetInfo[] offsets = ti.offsets;
                    for (int i = 0; i < freq; i++) {
                        newOffsets[i] = offsets[i];
                    }
                    ti.offsets = newOffsets;
                }
                ti.offsets[freq] = offset;
            }
            ti.freq = freq + 1;
        } else {
            Term term = new Term(field, text, false);
            postingTable.put(term, new Posting(term, position, offset));
        }
    }

    private final Posting[] sortPostingTable() {
        Posting[] array = new Posting[postingTable.size()];
        Enumeration postings = postingTable.elements();
        for (int i = 0; postings.hasMoreElements(); i++) array[i] = (Posting) postings.nextElement();
        quickSort(array, 0, array.length - 1);
        return array;
    }

    private static final void quickSort(Posting[] postings, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        if (postings[lo].term.compareTo(postings[mid].term) > 0) {
            Posting tmp = postings[lo];
            postings[lo] = postings[mid];
            postings[mid] = tmp;
        }
        if (postings[mid].term.compareTo(postings[hi].term) > 0) {
            Posting tmp = postings[mid];
            postings[mid] = postings[hi];
            postings[hi] = tmp;
            if (postings[lo].term.compareTo(postings[mid].term) > 0) {
                Posting tmp2 = postings[lo];
                postings[lo] = postings[mid];
                postings[mid] = tmp2;
            }
        }
        int left = lo + 1;
        int right = hi - 1;
        if (left >= right) return;
        Term partition = postings[mid].term;
        for (; ; ) {
            while (postings[right].term.compareTo(partition) > 0) --right;
            while (left < right && postings[left].term.compareTo(partition) <= 0) ++left;
            if (left < right) {
                Posting tmp = postings[left];
                postings[left] = postings[right];
                postings[right] = tmp;
                --right;
            } else {
                break;
            }
        }
        quickSort(postings, lo, left);
        quickSort(postings, left + 1, hi);
    }

    private final void writePostings(Posting[] postings, String segment) throws IOException {
        IndexOutput freq = null, prox = null;
        TermInfosWriter tis = null;
        TermVectorsWriter termVectorWriter = null;
        try {
            freq = directory.createOutput(segment + ".frq");
            prox = directory.createOutput(segment + ".prx");
            tis = new TermInfosWriter(directory, segment, fieldInfos, termIndexInterval);
            TermInfo ti = new TermInfo();
            String currentField = null;
            for (int i = 0; i < postings.length; i++) {
                Posting posting = postings[i];
                ti.set(1, freq.getFilePointer(), prox.getFilePointer(), -1);
                tis.add(posting.term, ti);
                int postingFreq = posting.freq;
                if (postingFreq == 1) freq.writeVInt(1); else {
                    freq.writeVInt(0);
                    freq.writeVInt(postingFreq);
                }
                int lastPosition = 0;
                int[] positions = posting.positions;
                for (int j = 0; j < postingFreq; j++) {
                    int position = positions[j];
                    prox.writeVInt(position - lastPosition);
                    lastPosition = position;
                }
                String termField = posting.term.field();
                if (currentField != termField) {
                    currentField = termField;
                    FieldInfo fi = fieldInfos.fieldInfo(currentField);
                    if (fi.storeTermVector) {
                        if (termVectorWriter == null) {
                            termVectorWriter = new TermVectorsWriter(directory, segment, fieldInfos);
                            termVectorWriter.openDocument();
                        }
                        termVectorWriter.openField(currentField);
                    } else if (termVectorWriter != null) {
                        termVectorWriter.closeField();
                    }
                }
                if (termVectorWriter != null && termVectorWriter.isFieldOpen()) {
                    termVectorWriter.addTerm(posting.term.text(), postingFreq, posting.positions, posting.offsets);
                }
            }
            if (termVectorWriter != null) termVectorWriter.closeDocument();
        } finally {
            IOException keep = null;
            if (freq != null) try {
                freq.close();
            } catch (IOException e) {
                if (keep == null) keep = e;
            }
            if (prox != null) try {
                prox.close();
            } catch (IOException e) {
                if (keep == null) keep = e;
            }
            if (tis != null) try {
                tis.close();
            } catch (IOException e) {
                if (keep == null) keep = e;
            }
            if (termVectorWriter != null) try {
                termVectorWriter.close();
            } catch (IOException e) {
                if (keep == null) keep = e;
            }
            if (keep != null) throw (IOException) keep.fillInStackTrace();
        }
    }

    private final void writeNorms(String segment) throws IOException {
        for (int n = 0; n < fieldInfos.size(); n++) {
            FieldInfo fi = fieldInfos.fieldInfo(n);
            if (fi.isIndexed && !fi.omitNorms) {
                float norm = fieldBoosts[n] * similarity.lengthNorm(fi.name, fieldLengths[n]);
                IndexOutput norms = directory.createOutput(segment + ".f" + n);
                try {
                    norms.writeByte(Similarity.encodeNorm(norm));
                } finally {
                    norms.close();
                }
            }
        }
    }

    /** If non-null, a message will be printed to this if maxFieldLength is reached.
   */
    void setInfoStream(PrintStream infoStream) {
        this.infoStream = infoStream;
    }
}

final class Posting {

    Term term;

    int freq;

    int[] positions;

    TermVectorOffsetInfo[] offsets;

    Posting(Term t, int position, TermVectorOffsetInfo offset) {
        term = t;
        freq = 1;
        positions = new int[1];
        positions[0] = position;
        if (offset != null) {
            offsets = new TermVectorOffsetInfo[1];
            offsets[0] = offset;
        } else offsets = null;
    }
}
