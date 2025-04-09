package edu.mit.lcs.haystack.lucene.index;

import edu.mit.lcs.haystack.lucene.document.Document;
import edu.mit.lcs.haystack.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.OutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

final class DocumentWriter {

    private Analyzer analyzer;

    private Directory directory;

    private Similarity similarity = Similarity.getDefault();

    private FieldInfos fieldInfos;

    private int maxFieldLength;

    /**
     * 
     * @param directory
     *            The directory to write the document information to
     * @param analyzer
     *            The analyzer to use for the document
     * @param similarity
     *            The Similarity function
     * @param maxFieldLength
     *            The maximum number of tokens a field may have
     */
    DocumentWriter(Directory directory, Analyzer analyzer, Similarity similarity, int maxFieldLength) {
        this.directory = directory;
        this.analyzer = analyzer;
        this.similarity = similarity;
        this.maxFieldLength = maxFieldLength;
    }

    final void addDocument(String segment, Document doc) throws IOException {
        fieldInfos = new FieldInfos();
        fieldInfos.add(doc);
        fieldInfos.write(directory, segment + ".fnm");
        FieldsWriter fieldsWriter = new FieldsWriter(directory, segment, fieldInfos);
        try {
            fieldsWriter.addDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fieldsWriter.close();
        }
        postingTable.clear();
        fieldLengths = new int[fieldInfos.size()];
        fieldPositions = new int[fieldInfos.size()];
        fieldBoosts = new float[fieldInfos.size()];
        Arrays.fill(fieldBoosts, doc.getBoost());
        invertDocument(doc, segment);
        Posting[] postings = sortPostingTable();
        writePostings(postings, segment);
        writeNorms(doc, segment);
    }

    private final Hashtable postingTable = new Hashtable();

    private int[] fieldLengths;

    private int[] fieldPositions;

    private float[] fieldBoosts;

    private final Term termBuffer = new Term("", "");

    private final void addPosition(String field, String text, int position) {
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
            ti.freq = freq + 1;
        } else {
            Term term = new Term(field, text);
            postingTable.put(term, new Posting(term, position));
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
        OutputStream freq = null, prox = null;
        TermInfosWriter tis = null;
        TermVectorsWriter termVectorWriter = null;
        try {
            freq = directory.createFile(segment + ".frq");
            prox = directory.createFile(segment + ".prx");
            tis = new TermInfosWriter(directory, segment, fieldInfos);
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
                    termVectorWriter.addTerm(posting.term.text(), postingFreq);
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

    private final void writeNorms(Document doc, String segment) throws IOException {
        for (int n = 0; n < fieldInfos.size(); n++) {
            FieldInfo fi = fieldInfos.fieldInfo(n);
            if (fi.isIndexed) {
                float norm = fieldBoosts[n] * similarity.lengthNorm(fi.name, fieldLengths[n]);
                OutputStream norms = directory.createFile(segment + ".f" + n);
                try {
                    norms.writeByte(Similarity.encodeNorm(norm));
                } finally {
                    norms.close();
                }
            }
        }
    }

    private final void invertDocument(Document doc, String segment) throws IOException {
        Hashtable hashFields = new Hashtable();
        String primaryFieldValue = null;
        try {
            Enumeration fields = doc.fields();
            while (fields.hasMoreElements()) {
                Hashtable hashWords = null;
                Field field = (Field) fields.nextElement();
                String fieldName = field.name();
                int fieldNumber = fieldInfos.fieldNumber(fieldName);
                if (bForward) {
                    hashWords = (Hashtable) hashFields.get(fieldName);
                    if (hashWords == null) {
                        hashWords = new Hashtable();
                    }
                }
                int length = fieldLengths[fieldNumber];
                int position = fieldPositions[fieldNumber];
                if (field.isIndexed()) {
                    if (fieldName.equals(primaryField)) {
                        if (primaryFieldValue != null) throw new RuntimeException("primaryField has appeared twice!");
                        primaryFieldValue = field.stringValue();
                    }
                    if (!field.isTokenized()) {
                        addPosition(fieldName, field.stringValue(), position++);
                        length++;
                        if (bForward) {
                            hashWords.put(field.stringValue(), new Integer(1));
                        }
                    } else {
                        Reader reader;
                        if (field.readerValue() != null) reader = field.readerValue(); else if (field.stringValue() != null) reader = new StringReader(field.stringValue()); else throw new IllegalArgumentException("field must have either String or Reader value");
                        TokenStream stream = analyzer.tokenStream(fieldName, reader);
                        try {
                            for (Token t = stream.next(); t != null; t = stream.next()) {
                                if (bForward) {
                                    Integer i = (Integer) hashWords.get(t.termText());
                                    if (i == null) {
                                        hashWords.put(t.termText(), new Integer(1));
                                    } else {
                                        hashWords.put(t.termText(), new Integer(i.intValue() + 1));
                                    }
                                }
                                position += (t.getPositionIncrement() - 1);
                                addPosition(fieldName, t.termText(), position++);
                                if (++length > maxFieldLength) break;
                            }
                        } finally {
                            stream.close();
                        }
                    }
                    fieldLengths[fieldNumber] = length;
                    fieldPositions[fieldNumber] = position;
                    fieldBoosts[fieldNumber] *= field.getBoost();
                }
                if (bForward) {
                    hashFields.put(fieldName, hashWords);
                }
            }
        } finally {
            if (bForward && (primaryFieldValue != null)) {
                ForwardWriter fw = new ForwardWriter(directory, segment, primaryField);
                fw.writeDocument(new FrequencyMap(hashFields, primaryFieldValue));
                fw.close();
            }
        }
    }

    private String primaryField = null;

    private boolean bForward = false;

    public DocumentWriter(boolean bForward, String primaryField, Directory d, Analyzer a, int mfl) {
        directory = d;
        analyzer = a;
        maxFieldLength = mfl;
        this.primaryField = primaryField;
        this.bForward = bForward;
    }
}

final class Posting {

    Term term;

    int freq;

    int[] positions;

    Posting(Term t, int position) {
        term = t;
        freq = 1;
        positions = new int[1];
        positions[0] = position;
    }
}
