package com.colloquial.arithcode;

/** Provides a cumulative, adaptive byte model implementing
 * prediction by partial matching up to a specified maximum context size.
 * Uses Method C for estimation.  
 * 
 * Constants that control behavior include the maximum total count before
 * rescaling, and the minimum count to retain after rescaling (an escape
 * is always maintained with a count of at least <code>1</code>).
 * <P>
 * For more details, see <a href="../../../tutorial.html">The Arithemtic Coding Tutorial</a>.
 *
 * @author <a href="http://www.colloquial.com/carp/">Bob Carpenter</a>
 * @version 1.1
 * @since 1.0
 */
public final class PPMModel implements ArithCodeModel {

    /** Construct a new model with the specified maximum length of
     * context to use for prediction.
     * @param maxContextLength Maximum length of context to use for prediction.
     */
    public PPMModel(int maxContextLength) {
        _maxContextLength = maxContextLength;
        _buffer = new ByteBuffer(maxContextLength + 1);
    }

    public boolean escaped(int symbol) {
        return (_contextNode != null && (symbol == ArithCodeModel.EOF || !_contextNode.hasDaughter(symbol)));
    }

    public void exclude(int i) {
        _excludedBytes.add(i);
    }

    public void interval(int symbol, int[] result) {
        if (symbol == ArithCodeModel.EOF) _backoffModel.interval(EOF, result, _excludedBytes); else if (symbol == ArithCodeModel.ESCAPE) intervalEscape(result); else intervalByte(symbol, result);
    }

    public int pointToSymbol(int count) {
        if (_contextNode != null) return _contextNode.pointToSymbol(count, _excludedBytes);
        return _backoffModel.pointToSymbol(count, _excludedBytes);
    }

    public int totalCount() {
        if (_contextNode == null) return _backoffModel.totalCount(_excludedBytes);
        return _contextNode.totalCount(_excludedBytes);
    }

    public void increment(int i) {
        increment(Converter.integerToByte(i));
    }

    /** Exclude all of the bytes in the specified byte set.
     * @param bytesToExclude Set of bytes to exclude from outcome.
     * @since 1.1
     */
    public void exclude(ByteSet bytesToExclude) {
        _excludedBytes.add(bytesToExclude);
    }

    /** Model to use for short contexts. */
    private final ExcludingAdaptiveUnigramModel _backoffModel = new ExcludingAdaptiveUnigramModel();

    /** Nodes at depth 1 in the model. All order 0 nodes are included in the unigram
     */
    private final PPMNode[] _contexts = new PPMNode[256];

    /** Maximum context length to search in trie.  Maximum count will
     * be for maximum context length plus one.
     */
    private final int _maxContextLength;

    /** Root of the trie structure of counts.  Dummy byte as symbol.
     */
    private final PPMNode _rootNode = new PPMNode((byte) '.');

    /** Current context length.
     */
    private int _contextLength;

    /** Current context node.
     */
    private PPMNode _contextNode;

    /** Bytes buffered for use as context.
     */
    private final ByteBuffer _buffer;

    /** Storage for the excluded bytes
     */
    private final ByteSet _excludedBytes = new ByteSet();

    /** Returns interval for byte specified as an integer in 0 to 255 range.
     * @param i Integer specification of byte in 0 to 255 range.
     * @param result Array specifying cumulative probability for byte i.
     */
    private void intervalByte(int i, int[] result) {
        if (_contextNode != null) _contextNode.interval(i, _excludedBytes, result); else _backoffModel.interval(i, result, _excludedBytes);
        increment(i);
    }

    /** Returns interval for escape in current context. 
     * @param result Array for specifying cumulative probability for escape symbol in current context.
     */
    private void intervalEscape(int[] result) {
        _contextNode.intervalEscape(_excludedBytes, result);
        if (_contextLength >= MIN_CONTEXT_LENGTH) for (PPMNode child = _contextNode._firstChild; child != null; child = child._nextSibling) _excludedBytes.add(child._byte);
        --_contextLength;
        getContextNodeLongToShort();
    }

    /** Adds counts for given byte to model in current context and then updates the current context. 
     * Rescales counts if necessary.  Called by both encoding and deocding.
     * @param b Byte to add to model. 
     */
    private void increment(byte b) {
        _buffer.buffer(b);
        byte firstByte = _buffer.bytes()[_buffer.offset()];
        if (_contexts[Converter.byteToInteger(firstByte)] == null) _contexts[Converter.byteToInteger(firstByte)] = new PPMNode(firstByte);
        if (_buffer.length() > 1) _contexts[Converter.byteToInteger(firstByte)].increment(_buffer.bytes(), _buffer.offset() + 1, _buffer.length() - 1);
        _contextLength = Math.min(_maxContextLength, _buffer.length());
        getContextNodeBinarySearch();
        _excludedBytes.clear();
    }

    /** Use binary search to set the context node up to the currently
     * specified context length.  May set it to <code>null</code> if
     * not found.
     */
    private void getContextNodeBinarySearch() {
        int low = MIN_CONTEXT_LENGTH;
        int high = _contextLength;
        _contextLength = MIN_CONTEXT_LENGTH - 1;
        _contextNode = null;
        boolean isDeterministic = false;
        while (high >= low) {
            int contextLength = (high + low) / 2;
            PPMNode contextNode = lookupNode(contextLength);
            if (contextNode == null || contextNode.isChildless(_excludedBytes)) {
                if (contextLength < high) high = contextLength; else --high;
            } else if (contextNode.isDeterministic(_excludedBytes)) {
                _contextLength = contextLength;
                _contextNode = contextNode;
                isDeterministic = true;
                if (contextLength < high) high = contextLength; else --high;
            } else if (!isDeterministic) {
                _contextLength = contextLength;
                _contextNode = contextNode;
                if (contextLength > low) low = contextLength; else ++low;
            } else {
                if (contextLength > low) low = contextLength; else ++low;
            }
        }
    }

    /** Starting at the longest context, count down in length to set
     * a valid context or give up.  This version finds the shortest deterministic
     * context <= in length to the current context length, but if there is
     * no deterministic context, returns longest context length that exists
     * that is <= in length to the current context.
     * Could also implement this in short to long order
     */
    private void getContextNodeLongToShort() {
        while (_contextLength >= MIN_CONTEXT_LENGTH) {
            _contextNode = lookupNode(_contextLength);
            if (_contextNode == null || _contextNode.isChildless(_excludedBytes)) {
                --_contextLength;
                continue;
            }
            while (_contextLength > MIN_CONTEXT_LENGTH && _contextNode.isDeterministic(_excludedBytes)) {
                PPMNode backoffNode = lookupNode(_contextLength - 1);
                if (backoffNode == null || !backoffNode.isDeterministic(_excludedBytes)) return;
                _contextNode = backoffNode;
                --_contextLength;
            }
            return;
        }
        _contextNode = null;
    }

    /** Returns node from the current byte buffer of
     * the specified context length, or null if there isn't one.
     * @param contextLength Number of bytes of context used.
     * @return Node found at that context.
     */
    private PPMNode lookupNode(int contextLength) {
        PPMNode node = _contexts[Converter.byteToInteger(_buffer.bytes()[_buffer.offset() + _buffer.length() - contextLength])];
        if (node == null) return (PPMNode) null;
        return lookup(node, _buffer.bytes(), _buffer.offset() + _buffer.length() - contextLength + 1, contextLength - 1);
    }

    /** Looks up a node from the given bytes, offset and length starting
     * from the specified node.
     * @param node Node from which to search.
     * @param bytes Sequence of bytes to search.
     * @param offset Offset into sequence of bytes of the first byte.
     * @param length Number of bytes to look up.
     * @return Node found for the given bytes.
     */
    private static PPMNode lookup(PPMNode node, byte[] bytes, int offset, int length) {
        if (length == 0) return node;
        for (PPMNode child = node._firstChild; length > 0 && child != null; ) {
            if (bytes[offset] == child._byte) {
                if (length == 1) return child;
                node = child;
                child = child._firstChild;
                ++offset;
                --length;
            } else {
                child = child._nextSibling;
            }
        }
        return (PPMNode) null;
    }

    /** Minimum context length to look down sequence of nodes.
     * Shorter contexts use backoff model.
     */
    private static final int MIN_CONTEXT_LENGTH = 1;
}
