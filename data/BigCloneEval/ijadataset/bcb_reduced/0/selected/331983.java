package com.lucene.index;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Enumeration;
import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.analysis.Analyzer;
import com.lucene.analysis.TokenStream;
import com.lucene.analysis.Token;
import com.lucene.store.Directory;
import com.lucene.store.OutputStream;
import com.lucene.search.Similarity;

final class DocumentWriter {

    private Analyzer analyzer;

    private Directory directory;

    private FieldInfos fieldInfos;

    private int maxFieldLength;

    DocumentWriter(Directory d, Analyzer a, int mfl) {
        directory = d;
        analyzer = a;
        maxFieldLength = mfl;
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
        invertDocument(doc);
        Posting[] postings = sortPostingTable();
        writePostings(postings, segment);
        writeNorms(doc, segment);
    }

    private final Hashtable postingTable = new Hashtable();

    private int[] fieldLengths;

    private final void invertDocument(Document doc) throws IOException {
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            String fieldName = field.name();
            int fieldNumber = fieldInfos.fieldNumber(fieldName);
            int position = fieldLengths[fieldNumber];
            if (field.isIndexed()) {
                if (!field.isTokenized()) {
                    addPosition(fieldName, field.stringValue(), position++);
                } else {
                    Reader reader;
                    if (field.readerValue() != null) reader = field.readerValue(); else if (field.stringValue() != null) reader = new StringReader(field.stringValue()); else throw new IllegalArgumentException("field must have either String or Reader value");
                    TokenStream stream = analyzer.tokenStream(reader);
                    try {
                        for (Token t = stream.next(); t != null; t = stream.next()) {
                            addPosition(fieldName, t.termText(), position++);
                            if (position > maxFieldLength) break;
                        }
                    } finally {
                        stream.close();
                    }
                }
                fieldLengths[fieldNumber] = position;
            }
        }
    }

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
            Term term = new Term(field, text, false);
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
        try {
            freq = directory.createFile(segment + ".frq");
            prox = directory.createFile(segment + ".prx");
            tis = new TermInfosWriter(directory, segment, fieldInfos);
            TermInfo ti = new TermInfo();
            for (int i = 0; i < postings.length; i++) {
                Posting posting = postings[i];
                ti.set(1, freq.getFilePointer(), prox.getFilePointer());
                tis.add(posting.term, ti);
                int f = posting.freq;
                if (f == 1) freq.writeVInt(1); else {
                    freq.writeVInt(0);
                    freq.writeVInt(f);
                }
                int lastPosition = 0;
                for (int j = 0; j < f; j++) prox.writeVInt(posting.positions[j] - lastPosition);
            }
        } finally {
            if (freq != null) freq.close();
            if (prox != null) prox.close();
            if (tis != null) tis.close();
        }
    }

    private final void writeNorms(Document doc, String segment) throws IOException {
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            if (field.isIndexed()) {
                int fieldNumber = fieldInfos.fieldNumber(field.name());
                OutputStream norm = directory.createFile(segment + ".f" + fieldNumber);
                try {
                    norm.writeByte(Similarity.norm(fieldLengths[fieldNumber]));
                } finally {
                    norm.close();
                }
            }
        }
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
