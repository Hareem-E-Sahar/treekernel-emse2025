package com.limegroup.gnutella.routing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.limewire.collection.BitSet;
import org.limewire.io.IOUtils;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.util.Utilities;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/**
 * A list of query keywords that a connection can respond to, as well as the
 * minimum TTL for a response.  More formally, a QueryRouteTable is a (possibly
 * infinite!) list of keyword TTL pairs, [ &#60;keyword_1, ttl_1&#62;, ...,
 * &#60;keywordN, ttl_N&#62; ]  <p>
 *
 * 10/08/2002 - A day after Susheel's birthday, he decided to change this class
 * for the heck of it.  Kidding.  Functionality has been changed so that keyword
 * depth is 'constant' - meaning that if a keyword is added, then any contains
 * query regarding that keyword will return true.  This is because this general
 * idea of QRTs is only used in a specialized way in LW - namely, UPs use it for
 * their leaves ONLY, so the depth is always 1.  If you looking for a keyword
 * and it is in the table, a leaf MAY have it, so return true.  This only
 * needed a one line change.
 *
 * 12/05/2003 - Two months after Susheel's birthday, this class was changed to
 * once again accept variable infinity values.  Over time, optimizations had
 * removed the ability for a QueryRouteTable to have an infinity that wasn't
 * 7.  However, nothing outright checked that, so patch messages that were
 * based on a non-7 infinity were silently failing (always stayed empty).
 * In practice, we could probably even change the infinity to 2, and change
 * change the number of entryBits to 2, with the keywordPresent and
 * keywordAbsent values going to 1 and -1, cutting the size of our patch
 * messages further in half (a quarter of the original size).  This would
 * probably require upgrading the X-Query-Routing to another version.
 *
 * <b>This class is NOT synchronized.</b>
 */
public class QueryRouteTable {

    /** 
     * The suggested default max table TTL.
     */
    public static final byte DEFAULT_INFINITY = (byte) 7;

    /** What should come across the wire if a keyword status is unchanged. */
    public static final byte KEYWORD_NO_CHANGE = (byte) 0;

    /** The maximum size of patch messages, in bytes. */
    public static final int MAX_PATCH_SIZE = 1 << 12;

    private static final AtomicInteger DEFAULT_SIZE = new AtomicInteger(-1);

    /**
     * The current infinity this table is using.  Necessary for creating
     * ResetTableMessages with the correct infinity.
     */
    private byte infinity;

    /**
     * What should come across the wire if a keyword is present.
     * The nature of this value is dependent on the infinity of the
     * ResetTableMessage.
     */
    private byte keywordPresent;

    /**
     * What should come across the wire if a keyword is absent.
     * The nature of thsi value is dependent on the infinity of the
     * ResetTableMessage.
     */
    private byte keywordAbsent;

    /** The *new* table implementation.  The table of keywords - each value in
     *  the BitSet is either 'true' or 'false' - 'true' signifies that a keyword
     *  match MAY be at a leaf 1 hop away, whereas 'false' signifies it isn't.
     *  QRP is really not used in full by the Gnutella Ultrapeer protocol, hence
     *  the easy optimization of only using BitSets.
     */
    private BitSet bitTable;

    /**
     * The cached resized QRT.
     */
    private QueryRouteTable resizedQRT = null;

    /** The 'logical' length of the BitSet.  Needed because the BitSet accessor
     *  methods don't seem to offer what is needed.
     */
    private int bitTableLength;

    /** The last message received of current sequence, or -1 if none. */
    private int sequenceNumber;

    /** The size of the current sequence, or -1 if none. */
    private int sequenceSize;

    /** The index of the next table entry to patch. */
    private int nextPatch;

    /** The uncompressor. This state must be maintained to implement chunked
     *  PATCH messages.  (You may need data from message N-1 to apply the patch
     *  in message N.) */
    private volatile Inflater uncompressor;

    /** Creates a QueryRouteTable with default sizes. */
    public QueryRouteTable() {
        DEFAULT_SIZE.compareAndSet(-1, (int) ConnectionSettings.QRT_SIZE_IN_KIBI_ENTRIES.getValue());
        long byteCount = 1024 * DEFAULT_SIZE.get();
        if (byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Default QRT size cannot be expressed as an int.");
        }
        initialize((int) byteCount, DEFAULT_INFINITY);
    }

    /**
     * Creates a new <tt>QueryRouteTable</tt> instance with the specified
     * size.  This <tt>QueryRouteTable</tt> will be completely empty with
     * no keywords -- no queries will have hits in this route table until
     * patch messages are received.
     *
     * @param size the size of the query routing table
     */
    public QueryRouteTable(int size) {
        this(size, DEFAULT_INFINITY);
    }

    /**
     * Creates a new <tt>QueryRouteTable</tt> instance with the specified
     * size and infinity.  This <tt>QueryRouteTable</tt> will be completely 
     * empty with no keywords -- no queries will have hits in this route 
     * table until patch messages are received.
     *
     * @param size the size of the query routing table
     * @param infinity the infinity to use
     */
    public QueryRouteTable(int size, byte infinity) {
        initialize(size, infinity);
    }

    /**
     * Initializes this <tt>QueryRouteTable</tt> to the specified size.
     * This table will be empty until patch messages are received.
     *
     * @param size the size of the query route table
     */
    private void initialize(int size, byte infinity) {
        this.bitTableLength = size;
        this.bitTable = new BitSet();
        this.sequenceNumber = -1;
        this.sequenceSize = -1;
        this.nextPatch = 0;
        this.keywordPresent = (byte) (1 - infinity);
        this.keywordAbsent = (byte) (infinity - 1);
        this.infinity = infinity;
    }

    /**
     * Returns the size of this QueryRouteTable.
     */
    public int getSize() {
        return bitTableLength;
    }

    /**
     * Returns the percentage of slots used in this QueryRouteTable's BitTable.
     * The return value is from 0 to 100.
     */
    public double getPercentFull() {
        double set = bitTable.cardinality();
        return (set / bitTableLength) * 100.0;
    }

    /**
	 * Returns the number of empty elements in the table.
	 */
    public int getEmptyUnits() {
        return bitTable.unusedUnits();
    }

    /**
	 * Returns the total number of units allocated for storage.
	 */
    public int getUnitsInUse() {
        return bitTable.getUnitsInUse();
    }

    /**
     * Returns true if a response could be generated for qr.  Note that a return
     * value of true does not necessarily mean that a response will be
     * generated--just that it could.  It is assumed that qr's TTL has already
     * been decremented, i.e., is the outbound not inbound TTL.  
     */
    public boolean contains(QueryRequest qr) {
        byte bits = Utilities.log2(bitTableLength);
        String query = qr.getQuery();
        LimeXMLDocument richQuery = qr.getRichQuery();
        if (query.length() == 0 && richQuery == null && !qr.hasQueryUrns()) {
            return false;
        }
        if (qr.hasQueryUrns()) {
            Set<URN> urns = qr.getQueryUrns();
            for (URN qurn : urns) {
                int hash = HashFunction.hash(qurn.toString(), bits);
                if (contains(hash)) {
                    return true;
                }
            }
            return false;
        }
        for (int i = 0; ; ) {
            int j = HashFunction.keywordStart(query, i);
            if (j < 0) break;
            int k = HashFunction.keywordEnd(query, j);
            int hash = HashFunction.hash(query, j, k, bits);
            if (!contains(hash)) return false;
            i = k + 1;
        }
        if (richQuery == null) return true;
        String docSchemaURI = richQuery.getSchemaURI();
        int hash = HashFunction.hash(docSchemaURI, bits);
        if (!contains(hash)) return false;
        int wordCount = 0;
        int matchCount = 0;
        for (String words : richQuery.getKeyWords()) {
            for (int i = 0; ; ) {
                int j = HashFunction.keywordStart(words, i);
                if (j < 0) break;
                int k = HashFunction.keywordEnd(words, j);
                int wordHash = HashFunction.hash(words, j, k, bits);
                if (contains(wordHash)) matchCount++;
                wordCount++;
                i = k + 1;
            }
        }
        for (String str : richQuery.getKeyWordsIndivisible()) {
            hash = HashFunction.hash(str, bits);
            if (contains(hash)) matchCount++;
            wordCount++;
        }
        if (wordCount < 3) return wordCount == matchCount; else return ((float) matchCount / (float) wordCount) > 0.67;
    }

    private final boolean contains(int hash) {
        return bitTable.get(hash);
    }

    /**
     * For all keywords k in filename, adds <k> to this.
     */
    public void add(String filePath) {
        addBTInternal(filePath);
    }

    private void addBTInternal(String filePath) {
        String[] words = HashFunction.keywords(filePath);
        String[] keywords = HashFunction.getPrefixes(words);
        byte log2 = Utilities.log2(bitTableLength);
        for (int i = 0; i < keywords.length; i++) {
            int hash = HashFunction.hash(keywords[i], log2);
            if (!bitTable.get(hash)) {
                resizedQRT = null;
                bitTable.set(hash);
            }
        }
    }

    public void addIndivisible(String iString) {
        final int hash = HashFunction.hash(iString, Utilities.log2(bitTableLength));
        if (!bitTable.get(hash)) {
            resizedQRT = null;
            bitTable.set(hash);
        }
    }

    /**
     * For all <keyword_i> in qrt, adds <keyword_i> to this.
     * (This is useful for unioning lots of route tables for propoagation.)
     *
     *    @modifies this
     */
    public void addAll(QueryRouteTable qrt) {
        this.bitTable.or(qrt.resize(this.bitTableLength));
    }

    /**
     * Scales the internal cached BitSet to size 'newSize'
     */
    private BitSet resize(int newSize) {
        if (bitTableLength == newSize) return bitTable;
        if (resizedQRT != null && resizedQRT.bitTableLength == newSize) return resizedQRT.bitTable;
        resizedQRT = new QueryRouteTable(newSize);
        final int m = this.bitTableLength;
        final int m2 = resizedQRT.bitTableLength;
        for (int i = this.bitTable.nextSetBit(0); i >= 0; i = this.bitTable.nextSetBit(i + 1)) {
            final int firstSet = (int) (((long) i * m2) / m);
            i = this.bitTable.nextClearBit(i + 1);
            final int lastNotSet = (int) (((long) i * m2 - 1) / m + 1);
            resizedQRT.bitTable.set(firstSet, lastNotSet);
        }
        return resizedQRT.bitTable;
    }

    /** True if o is a QueryRouteTable with the same entries of this. */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryRouteTable)) return false;
        QueryRouteTable other = (QueryRouteTable) o;
        if (this.bitTableLength != other.bitTableLength) return false;
        if (!this.bitTable.equals(other.bitTable)) return false;
        return true;
    }

    public int hashCode() {
        return bitTable.hashCode() * 17;
    }

    public String toString() {
        return "QueryRouteTable : " + bitTable.toString();
    }

    /**
     * Resets this <tt>QueryRouteTable</tt> to the specified size with
     * no data.  This is done when a RESET message is received.
     *
     * @param rtm the <tt>ResetTableMessage</tt> containing the size
     *  to reset the table to
     */
    public void reset(ResetTableMessage rtm) {
        initialize(rtm.getTableSize(), rtm.getInfinity());
    }

    /**
     * Adds the specified patch message to this query routing table.
     *
     * @param patch the <tt>PatchTableMessage</tt> containing the new
     *  data to add
     * @throws <tt>BadPacketException</tt> if the sequence number or size
     *  is incorrect
     */
    public void patch(PatchTableMessage patch) throws BadPacketException {
        handlePatch(patch);
    }

    private void handlePatch(PatchTableMessage m) throws BadPacketException {
        if (sequenceSize != -1 && sequenceSize != m.getSequenceSize()) throw new BadPacketException("Inconsistent seq size: " + m.getSequenceSize() + " vs. " + sequenceSize);
        if (sequenceNumber == -1 ? m.getSequenceNumber() != 1 : sequenceNumber + 1 != m.getSequenceNumber()) throw new BadPacketException("Inconsistent seq number: " + m.getSequenceNumber() + " vs. " + sequenceNumber);
        byte[] data = m.getData();
        if (m.getCompressor() == PatchTableMessage.COMPRESSOR_DEFLATE) {
            try {
                if (m.getSequenceNumber() == 1) {
                    uncompressor = new Inflater();
                }
                assert uncompressor != null : "Null uncompressor.  Sequence: " + m.getSequenceNumber();
                data = uncompress(data);
            } catch (IOException e) {
                throw new BadPacketException("Couldn't uncompress data: " + e);
            }
        } else if (m.getCompressor() != PatchTableMessage.COMPRESSOR_NONE) {
            throw new BadPacketException("Unknown compressor");
        }
        if (m.getEntryBits() == 4) data = unhalve(data); else if (m.getEntryBits() != 8) throw new BadPacketException("Unknown value for entry bits");
        for (int i = 0; i < data.length; i++) {
            if (nextPatch >= bitTableLength) throw new BadPacketException("Tried to patch " + nextPatch + " on a bitTable of size " + bitTableLength);
            if (data[i] < 0) {
                bitTable.set(nextPatch);
                resizedQRT = null;
            } else if (data[i] > 0) {
                bitTable.clear(nextPatch);
                resizedQRT = null;
            }
            nextPatch++;
        }
        bitTable.compact();
        this.sequenceSize = m.getSequenceSize();
        if (m.getSequenceNumber() != m.getSequenceSize()) {
            this.sequenceNumber = m.getSequenceNumber();
        } else {
            this.sequenceNumber = -1;
            this.sequenceSize = -1;
            this.nextPatch = 0;
            if (this.uncompressor != null) {
                IOUtils.close(uncompressor);
                this.uncompressor = null;
            }
        }
    }

    /**
     * Stub for calling encode(QueryRouteTable, true).
     */
    public List<RouteTableMessage> encode(QueryRouteTable prev) {
        return encode(prev, true);
    }

    /**
     * Returns an List of RouteTableMessage that will convey the state of
     * this.  If that is null, this will include a reset.  Otherwise it will
     * include only those messages needed to to convert that to this.  More
     * formally, for any non-null QueryRouteTable's m and that, the following 
     * holds:
     *
     * <pre>
     * for (Iterator iter=m.encode(); iter.hasNext(); ) 
     *    prev.update((RouteTableUpdate)iter.next());
     * Assert.that(prev.equals(m)); 
     * </pre> 
     */
    public List<RouteTableMessage> encode(QueryRouteTable prev, boolean allowCompression) {
        List<RouteTableMessage> buf = new LinkedList<RouteTableMessage>();
        if (prev == null) buf.add(new ResetTableMessage(bitTableLength, infinity)); else assert prev.bitTableLength == this.bitTableLength : "TODO: can't deal with tables of different lengths";
        byte[] data = new byte[bitTableLength];
        Utilities.fill(data, 0, bitTableLength, KEYWORD_NO_CHANGE);
        boolean needsPatch = false;
        if (prev != null) {
            if (!this.bitTable.equals(prev.bitTable)) {
                BitSet xOr = (BitSet) this.bitTable.clone();
                xOr.xor(prev.bitTable);
                for (int i = xOr.nextSetBit(0); i >= 0; i = xOr.nextSetBit(i + 1)) {
                    data[i] = this.bitTable.get(i) ? keywordPresent : keywordAbsent;
                    needsPatch = true;
                }
            }
        } else {
            for (int i = bitTable.nextSetBit(0); i >= 0; i = bitTable.nextSetBit(i + 1)) {
                data[i] = keywordPresent;
                needsPatch = true;
            }
        }
        if (!needsPatch) {
            return buf;
        }
        byte bits = 8;
        if (keywordPresent >= -8 && keywordAbsent <= 7) {
            bits = 4;
            data = halve(data);
        }
        byte compression = PatchTableMessage.COMPRESSOR_NONE;
        if (allowCompression) {
            byte[] patchCompressed = IOUtils.deflate(data);
            if (patchCompressed.length < data.length) {
                data = patchCompressed;
                compression = PatchTableMessage.COMPRESSOR_DEFLATE;
            }
        }
        final int chunks = (int) Math.ceil((float) data.length / (float) MAX_PATCH_SIZE);
        int chunk = 1;
        for (int i = 0; i < data.length; i += MAX_PATCH_SIZE) {
            int stop = Math.min(i + MAX_PATCH_SIZE, data.length);
            buf.add(new PatchTableMessage((short) chunk, (short) chunks, compression, bits, data, i, stop));
            chunk++;
        }
        return buf;
    }

    /** Returns the uncompressed version of the given defalted bytes, using
     *  any dictionaries in uncompressor.  Throws IOException if the data is
     *  corrupt.
     *      @requires inflater initialized 
     *      @modifies inflater */
    private byte[] uncompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        uncompressor.setInput(data);
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int read = uncompressor.inflate(buf);
                if (read == 0) break;
                baos.write(buf, 0, read);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException("Bad deflate format");
        }
    }

    /**
     * @return a byte[] copy of this routing table.  
     */
    public byte[] getRawDump() {
        byte[] ret = new byte[bitTableLength / 8];
        for (int i = bitTable.nextSetBit(0); i >= 0; i = bitTable.nextSetBit(i + 1)) ret[i / 8] = (byte) (ret[i / 8] | (1 << (7 - i % 8)));
        return ret;
    }

    /** Returns an array R of length array.length/2, where R[i] consists of the
     *  low nibble of array[2i] concatentated with the low nibble of array[2i+1].
     *  Note that unhalve(halve(array))=array if all elements of array fit can 
     *  fit in four signed bits.
     *      @requires array.length is a multiple of two */
    static byte[] halve(byte[] array) {
        byte[] ret = new byte[array.length / 2];
        for (int i = 0; i < ret.length; i++) ret[i] = (byte) ((array[2 * i] << 4) | (array[2 * i + 1] & 0xF));
        return ret;
    }

    /** Returns an array of R of length array.length*2, where R[i] is the the
     *  sign-extended high nibble of floor(i/2) if i even, or the sign-extended
     *  low nibble of floor(i/2) if i odd. */
    static byte[] unhalve(byte[] array) {
        byte[] ret = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            ret[2 * i] = (byte) (array[i] >> 4);
            ret[2 * i + 1] = extendNibble((byte) (array[i] & 0xF));
        }
        return ret;
    }

    /** Sign-extends the low nibble of b, i.e., 
     *  returns (from MSB to LSB) b[3]b[3]b[3]b[3]b[3]b[2]b[1]b[0]. */
    static byte extendNibble(byte b) {
        if ((b & 0x8) != 0) return (byte) (0xF0 | b); else return b;
    }
}
