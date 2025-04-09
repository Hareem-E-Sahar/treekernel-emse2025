package net.sourceforge.magex.preparation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The Index class creates one map index.
 */
public class Index {

    /** Filename separator for map index data */
    private static final String IDXDATA_SEP = "-";

    /** Filename suffix for map index data */
    private static final String IDXDATA_SUFF = ".x";

    /** Index part size -- power of 2 */
    private static final int PART_LOGSIZE = 16;

    /** Index part size mask */
    private static final int PART_MASK = ~((1 << PART_LOGSIZE) - 1);

    /** Invalid offset value */
    public static final int INVALID_OFFSET = -1;

    /** 
     * Special char, marking the end of the word and telling the result to
     * put the rest at the beginning. Needs to be public, influences IndexInternal.place() .
     */
    public static final char SPEC_CHAR = '\r';

    /** Character that serves as a space when dividing words in an object name */
    private static final char SPACE = ' ';

    /** 
     * A regexp for Polish format special information markings (inserted in the name), that are
     * to be left out from the indexing 
     */
    private static final String SPECIAL_MARKS_REGEXP = "~\\[.*\\]";

    /** Minimal length that a word must have to be indexed separately */
    private static final int MIN_LENGTH_TO_INDEX = 4;

    /** The index root node */
    IndexInternal root;

    /** The ID of this index */
    int id;

    /** 
     * Creates a new, empty index with the given ID.
     */
    public Index(byte id) {
        this.id = id;
        this.root = new IndexInternal('\0');
    }

    /**
     * Adds another object to the index (creates the corresponding nodes).
     *
     * @param object the map object to be indexed
     */
    public void addObject(MapObject object) {
        IndexInternal parent;
        Vector<IndexLeaf> newLeaves;
        String[] nameParts;
        if ("".equals(object.name)) {
            return;
        }
        newLeaves = new Vector<IndexLeaf>();
        nameParts = this.normalize(object.name).split("" + SPACE);
        for (int i = 0; i < nameParts.length; ++i) {
            IndexLeaf newLeaf;
            if (i != 0 && nameParts[i].length() < MIN_LENGTH_TO_INDEX) {
                continue;
            }
            newLeaf = new IndexLeaf(object.labelX, object.labelY, object.zoomLevel, object.dataType, object.objectType);
            newLeaf.text = nameParts[i];
            for (int pos = (i + 1) % nameParts.length; pos != i; pos = (pos + 1) % nameParts.length) {
                newLeaf.text += pos == 0 ? SPEC_CHAR : SPACE;
                newLeaf.text += nameParts[pos];
            }
            newLeaves.add(newLeaf);
        }
        for (int i = 0; i < newLeaves.size(); ++i) {
            this.root.place(newLeaves.elementAt(i));
        }
    }

    /**
     * Writes all the data into a given ZIP output stream, returns the size
     * of the (uncompressed) written data.
     *
     * @param mapId the id of the map for the entry name to be prefixed 
     * @param out the open ZIP output stream to write into
     * @return the total uncompressed size of the index
     */
    public int writeAllData(int mapId, ZipOutputStream out) throws IOException {
        int lastPos = -1;
        LinkedList<IndexInternal> fifo = new LinkedList<IndexInternal>();
        DataOutputStream dataOut = null;
        this.computeFileOffsets();
        fifo.add(this.root);
        while (true) {
            IndexInternal node = fifo.poll();
            Iterator<Character> iterChild;
            IndexLeaf child = null;
            if ((lastPos & PART_MASK) != (node.pos & PART_MASK)) {
                if (dataOut != null) {
                    dataOut.flush();
                }
                dataOut = this.createNewPart(out, mapId, (node.pos & PART_MASK) >> PART_LOGSIZE);
            }
            node.write(dataOut);
            lastPos = node.pos;
            this.addAllInternal(fifo, node);
            for (int i = 0; i < node.childrenNoCont.size(); ++i) {
                child = node.childrenNoCont.elementAt(i);
                if ((lastPos & PART_MASK) != (child.pos & PART_MASK)) {
                    if (dataOut != null) {
                        dataOut.flush();
                    }
                    dataOut = this.createNewPart(out, mapId, (child.pos & PART_MASK) >> PART_LOGSIZE);
                }
                child.write(dataOut);
                lastPos = child.pos;
            }
            iterChild = node.childrenLeaf.keySet().iterator();
            while (iterChild.hasNext()) {
                child = node.childrenLeaf.get(iterChild.next());
                if ((lastPos & PART_MASK) != (child.pos & PART_MASK)) {
                    if (dataOut != null) {
                        dataOut.flush();
                    }
                    dataOut = this.createNewPart(out, mapId, (child.pos & PART_MASK) >> PART_LOGSIZE);
                }
                child.write(dataOut);
                lastPos = child.pos;
            }
            if (fifo.size() == 0) {
                return lastPos + (child != null ? child.computeSize() : node.computeSize());
            }
        }
    }

    /**
     * "Normalize" a string -- i.e.&nbsp;make it uppercase and get rid of any
     * diacritics and Polish format special information (using the Java Normalizer class). 
     * Needed for reasonable character comparison upon search.
     * 
     * @param str the string to be noDiacritics
     * @return the noDiacritics version of the given string
     */
    private String normalize(String str) {
        String normalized;
        StringBuilder noDiacritics = new StringBuilder();
        normalized = Normalizer.normalize(str.replaceAll(SPECIAL_MARKS_REGEXP, ""), Normalizer.Form.NFD);
        for (char ch : normalized.toCharArray()) {
            if (Character.getType(ch) != Character.NON_SPACING_MARK) {
                noDiacritics.append(ch);
            }
        }
        return noDiacritics.toString().toUpperCase(Locale.ENGLISH);
    }

    /**
     * Creates a new part of the index, i.e.&nbsp;a new entry in the output ZIP file.
     *
     * @param out the zip output stream
     * @param the id of the map to be created
     * @param the new part number
     * @return an open DataOutputStream for this ZIP file entry
     */
    private DataOutputStream createNewPart(ZipOutputStream out, int mapId, int partNo) throws IOException {
        try {
            out.closeEntry();
        } catch (Exception e) {
        }
        out.putNextEntry(new ZipEntry(Process.MAP_DIR_PREFIX + Integer.toString(mapId, 16) + Process.MAP_DIR_SEP + Integer.toString(this.id, 16) + IDXDATA_SEP + Integer.toString(partNo, 16) + IDXDATA_SUFF));
        return new DataOutputStream(out);
    }

    /**
     * Computes the offsets in the output file for all the nodes.
     */
    private void computeFileOffsets() {
        int curPos = 0;
        LinkedList<IndexInternal> fifo = new LinkedList<IndexInternal>();
        DataOutputStream dataOut = null;
        fifo.add(this.root);
        while (fifo.size() > 0) {
            IndexInternal node = fifo.poll();
            Iterator<Character> iterChild;
            IndexLeaf child;
            if ((curPos & PART_MASK) != ((curPos + node.computeSize()) & PART_MASK)) {
                curPos = (curPos + node.computeSize()) & PART_MASK;
            }
            node.pos = curPos;
            curPos += node.computeSize();
            this.addAllInternal(fifo, node);
            for (int i = 0; i < node.childrenNoCont.size(); ++i) {
                if ((curPos & PART_MASK) != ((curPos + node.childrenNoCont.elementAt(i).computeSize()) & PART_MASK)) {
                    curPos = (curPos + node.childrenNoCont.elementAt(i).computeSize()) & PART_MASK;
                }
                node.childrenNoCont.elementAt(i).pos = curPos;
                curPos += node.childrenNoCont.elementAt(i).computeSize();
            }
            iterChild = node.childrenLeaf.keySet().iterator();
            while (iterChild.hasNext()) {
                child = node.childrenLeaf.get(iterChild.next());
                if ((curPos & PART_MASK) != ((curPos + child.computeSize()) & PART_MASK)) {
                    curPos = (curPos + child.computeSize()) & PART_MASK;
                }
                child.pos = curPos;
                curPos += child.computeSize();
            }
        }
    }

    /**
     * Adds all internal child nodes of the specified node into the specified queue
     * for processing, does this in the alphabetical order.
     *
     * @param fifo the queue to add the child nodes to
     * @param node the parent node
     */
    private void addAllInternal(LinkedList fifo, IndexInternal node) {
        Iterator<Character> iter = node.childrenInternal.keySet().iterator();
        while (iter.hasNext()) {
            fifo.add(node.childrenInternal.get(iter.next()));
        }
    }
}
