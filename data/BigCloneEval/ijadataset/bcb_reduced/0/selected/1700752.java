package ch.ethz.mxquery.sms.MMimpl;

import ch.ethz.mxquery.bindings.WindowBuffer;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.datamodel.Identifier;
import ch.ethz.mxquery.model.Iterator;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.model.Window;
import ch.ethz.mxquery.sms.interfaces.AppendUpdate;
import ch.ethz.mxquery.sms.interfaces.StreamStoreStatistics;
import ch.ethz.mxquery.datamodel.MXQueryDouble;
import ch.ethz.mxquery.datamodel.MXQueryNumber;
import ch.ethz.mxquery.datamodel.Source;
import ch.ethz.mxquery.datamodel.IdentifierFactory;
import ch.ethz.mxquery.datamodel.types.Type;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.contextConfig.XQStaticContext;
import ch.ethz.mxquery.datamodel.xdm.NamedToken;
import ch.ethz.mxquery.datamodel.xdm.TextToken;
import ch.ethz.mxquery.datamodel.xdm.TokenInterface;
import ch.ethz.mxquery.datamodel.xdm.Token;
import ch.ethz.mxquery.util.Hashtable;
import java.util.Vector;

/**
 * Simple TokenBuffer to materialize a stream. Additional a index on the node id's is created to
 * allow easy jumping between nodes.
 * @author Tim Kraska (<a href="mailto:mail@tim-kraska.de">mail@tim-kraska.de</a>)
 *
 */
public class TokenBufferStore implements AppendUpdate {

    private int initialCapacity = 50;

    public static final int MAX_NODE_ID = Integer.MAX_VALUE - 1;

    private double capacityIncrement = 1.5;

    private int nodeIndexCapacity = 10;

    private double nodeIndexIncrement = 2;

    protected int level;

    protected int tokenI = 0;

    private int nodeI = 0;

    private int[] nodeIndex;

    public TokenInterface[] tokenBuffer;

    private MXQueryNumber[] scores;

    private XDMIterator sourceStream;

    protected XQStaticContext context = null;

    boolean endOfStr = false;

    private int toksDel = 0;

    private int nodesDel = 0;

    public int myId;

    protected Identifier last_identifier = null;

    protected Identifier last_start_tag_identifier = null;

    protected Identifier last_start_document_identifier = null;

    private static int tokenBufferId = 0;

    private Hashtable attributes = new Hashtable();

    private boolean endOfItem = false;

    private Source source = null;

    private boolean sourceSet = false;

    protected boolean generateNodeIds = false;

    public boolean isScoring;

    private int scoreCapacity = nodeIndexCapacity;

    protected static int nodeIdCount = 0;

    protected static int descendantCounter = 0;

    protected WindowBuffer cont;

    public TokenBufferStore(int id, WindowBuffer container) {
        myId = id;
        cont = container;
        tokenBuffer = new TokenInterface[initialCapacity];
        nodeIndex = new int[nodeIndexCapacity];
    }

    public void setIterator(XDMIterator it) {
        this.sourceStream = it;
        cont.setSource(it);
    }

    /**
	 * Creates a new TokenBuffer for a stream with standard paras 
	 * @param sourceStream Source Stream
	 */
    public TokenBufferStore(XDMIterator sourceStream, WindowBuffer container) {
        tokenBufferId++;
        cont = container;
        myId = tokenBufferId;
        this.sourceStream = sourceStream;
        tokenBuffer = new TokenInterface[initialCapacity];
        nodeIndex = new int[nodeIndexCapacity];
    }

    /**
	 * Creates a new TokenBuffer for a stream with standard paras 
	 * @param sourceStream Source Stream
	 */
    public TokenBufferStore(XDMIterator sourceStream, int id, WindowBuffer container) {
        myId = id;
        cont = container;
        this.sourceStream = sourceStream;
        tokenBuffer = new TokenInterface[initialCapacity];
        nodeIndex = new int[nodeIndexCapacity];
    }

    /**
	 * Creates a new TokenBuffer. The nodeIndexCapacity is going to be initialCapacity / 10 and the
	 * nodeIndexIncrement is going to be capacityIncrement / 10
	 * @param sourceStream Source Stream
	 * @param initialCapacity Start Capacity
	 * @param capacityIncrement Increment
	 */
    public TokenBufferStore(XDMIterator sourceStream, int initialCapacity, int capacityIncrement) {
        tokenBufferId++;
        myId = tokenBufferId;
        if (myId == 2 || myId == 1) {
            System.out.println("2");
        }
        this.sourceStream = sourceStream;
        this.initialCapacity = initialCapacity;
        this.capacityIncrement = capacityIncrement;
        this.nodeIndexCapacity = initialCapacity / 10;
        this.nodeIndexIncrement = capacityIncrement / 10;
        tokenBuffer = new TokenInterface[initialCapacity];
        nodeIndex = new int[nodeIndexCapacity];
    }

    /**
	 * Sets the context of the source stream. 
	 * This is especially necassary if the window iterator is used for external variables
	 * @param context
	 */
    public void setContext(Context context) throws MXQueryException {
        sourceStream.setContext(context, true);
    }

    /**
	 * Returns the position for a given NodePosition. If the node id is higher
	 * than the availabe source nodes the latest position is returned
	 * 
	 * @param node Node Id
	 * @return
	 * @throws MXQueryException
	 */
    public int getTokenIdForNode(int nodeId) throws MXQueryException {
        if (nodeId < nodeI) {
            int arrayId = nodeId - nodesDel;
            return nodeIndex[arrayId];
        } else {
            while (nodeI <= nodeId && !endOfStr) {
                bufferNext();
            }
            if (endOfStr) {
                return tokenI - 1;
            } else {
                int arrayId = nodeId - nodesDel;
                return nodeIndex[arrayId];
            }
        }
    }

    /**
	 * Checks if a node exists
	 * 
	 * @param node
	 * @return
	 * @throws MXQueryException
	 */
    public boolean hasNode(int node) throws MXQueryException {
        get(getTokenIdForNode(node));
        if (node < nodeI) {
            return true;
        } else {
            return false;
        }
    }

    /**
	 * Returns the NodeId for the given Token Id; The first node has the id = 0.
	 * If the position is not reachable, the last node id is returned.
	 * 
	 * @param position
	 * @return
	 * @throws MXQueryException
	 */
    public int getNodeIdFromTokenId(int tokenId) throws MXQueryException {
        return getNodeIdFromTokenId(0, tokenId);
    }

    /**
	 * Like getNodeIdFromPosition(int position) but we give a additional
	 * information that makes the search for the right nodeId faster.
	 * 
	 * @param hasToHigherThan
	 * @param position
	 * @return
	 * @throws MXQueryException
	 */
    public int getNodeIdFromTokenId(int minNodeId, int tokenId) throws MXQueryException {
        get(tokenId);
        if (endOfStr && (tokenId >= tokenI)) {
            return nodeI;
        }
        int i = (minNodeId + 1) - nodesDel;
        if (i < 0) i = 1;
        while (i < (nodeI - nodesDel) && nodeIndex[i] <= tokenId) {
            i++;
        }
        return i - 1;
    }

    protected Identifier createNextTokenId() {
        if (generateNodeIds) {
            last_identifier = IdentifierFactory.createIdentifier(nodeIdCount++, this, last_identifier, (short) (level - 1));
            return last_identifier;
        } else return null;
    }

    /**
	 * Buffers the next token from the stream
	 * 
	 * @throws MXQueryException
	 */
    private void bufferNext() throws MXQueryException {
        TokenInterface token = sourceStream.next();
        endOfItem = false;
        int type = token.getEventType();
        if (Type.isAttribute(type)) type = Type.getAttributeValueType(type);
        if (Type.isTextNode(type)) type = Type.getTextNodeValueType(type);
        switch(type) {
            case Type.START_DOCUMENT:
                if (level == 0) {
                    indexNewNode();
                }
                if (generateNodeIds) {
                    Identifier id = createNextTokenId();
                    token.setNodeId(id);
                }
                level++;
                break;
            case Type.END_DOCUMENT:
                level--;
                if (level == 0) {
                    endOfItem = true;
                }
                if (generateNodeIds) token.setNodeId(null);
                break;
            case Type.START_TAG:
                if (level == 0) {
                    indexNewNode();
                }
                if (generateNodeIds) {
                    Identifier id = createNextTokenId();
                    token.setNodeId(id);
                }
                level++;
                break;
            case Type.END_TAG:
                level--;
                if (level == 0) {
                    endOfItem = true;
                }
                if (generateNodeIds) token.setNodeId(null);
                break;
            case Type.END_SEQUENCE:
                endOfStr = true;
                break;
            case Type.COMMENT:
            case Type.PROCESSING_INSTRUCTION:
                if (level == 0) {
                    indexNewNode();
                    endOfItem = true;
                }
                if (generateNodeIds) token.setNodeId(createNextTokenId());
                break;
        }
        if (Type.isAtomicType(type, Context.getDictionary()) || type == Type.UNTYPED) {
            if (level == 0) {
                indexNewNode();
                endOfItem = true;
            }
            if (generateNodeIds) {
                if ((token instanceof TextToken) || token instanceof NamedToken) {
                    if (token instanceof TextToken || token instanceof NamedToken) {
                        Identifier id = createNextTokenId();
                        token.setNodeId(id);
                    }
                } else {
                    token.setNodeId(createNextTokenId());
                }
            }
        }
        if (endOfItem && isScoring) {
            if ((nodeI - nodesDel - 1) == scores.length) {
                scoreCapacity = (int) (scoreCapacity * nodeIndexIncrement);
                MXQueryNumber[] newIndex = new MXQueryNumber[scoreCapacity];
                System.arraycopy(scores, 0, newIndex, 0, nodeI - nodesDel - 1);
                scores = newIndex;
            }
            try {
                scores[nodeI - nodesDel - 1] = sourceStream.getScore();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Capacity : " + nodeIndexCapacity + " Node i : " + nodeI + " nodes del: " + nodesDel);
                throw new ArrayIndexOutOfBoundsException();
            }
        }
        bufferToken(token);
    }

    public void printBuffer() {
        for (int i = 0; i < tokenI - toksDel; i++) if (tokenBuffer[i].getEventType() == Type.INT) System.out.println(tokenBuffer[i].getLong()); else System.out.println(tokenBuffer[i].getName());
        System.out.println("Token buffer size : " + tokenBuffer.length);
        System.out.println("Items gone : " + nodeI);
        System.out.println("Items left : " + (nodeI - nodesDel));
        System.out.println("tokens gone : " + tokenI);
        System.out.println("tokens left : " + (tokenI - toksDel));
    }

    /**
	 * Returns the Token for a given token id
	 * @param tokenId
	 * @return
	 * @throws MXQueryException
	 */
    public TokenInterface get(int tokenId) throws MXQueryException {
        if (tokenI > tokenId) {
            return tokenBuffer[tokenId - toksDel];
        } else {
            if (endOfStr) {
                return tokenBuffer[tokenI - 1 - toksDel];
            } else {
                while (tokenId >= tokenI && !endOfStr) {
                    bufferNext();
                }
                int i = tokenI - 1 - toksDel;
                return tokenBuffer[i];
            }
        }
    }

    /**
	 * Returns the token for a given tokenId. If the token is corresponds to a higher nodeId than maxNodeId, the 
	 * END_SEQUENCE token is returned. 
	 * 
	 * @param tokenId Token Id
	 * @param maxNodeId Max Token Id
	 * @return
	 * @throws MXQueryException
	 */
    public TokenInterface get(int tokenId, int maxNodeId) throws MXQueryException {
        if ((maxNodeId + 1) < nodeI) {
            if (tokenId == nodeIndex[(maxNodeId + 1) - nodesDel]) {
                return Token.END_SEQUENCE_TOKEN;
            }
        }
        TokenInterface token = null;
        if (tokenI > tokenId) {
            token = tokenBuffer[tokenId - toksDel];
        } else {
            if (endOfStr) {
                token = tokenBuffer[tokenI - 1 - toksDel];
            } else {
                while (tokenId >= tokenI && !endOfStr && (maxNodeId + 1 < nodeI)) {
                    bufferNext();
                }
                if (tokenId >= tokenI && !endOfStr) {
                    while (tokenId >= tokenI && !endOfStr && (!endOfItem || maxNodeId + 1 > nodeI)) {
                        bufferNext();
                    }
                }
                if (tokenId >= tokenI) token = Token.END_SEQUENCE_TOKEN; else token = tokenBuffer[tokenId - toksDel];
            }
        }
        return token;
    }

    /**
	 * Returns the current materialized nodeId
	 * @return
	 */
    public int getMaxNodeId() {
        return nodeI - 1;
    }

    /**
	 * Returns the current materialized tokenId
	 * @return
	 */
    public int getMaxTokenId() {
        return tokenI - 1;
    }

    /**
	 * Index a new Node
	 *
	 */
    private void indexNewNode() {
        if ((nodeI - nodesDel) == nodeIndex.length) {
            nodeIndexCapacity = (int) (nodeIndexCapacity * nodeIndexIncrement);
            int[] newIndex = new int[nodeIndexCapacity];
            System.arraycopy(nodeIndex, 0, newIndex, 0, nodeI - nodesDel);
            nodeIndex = newIndex;
        }
        nodeIndex[nodeI - nodesDel] = tokenI;
        nodeI++;
    }

    public void buffer(TokenInterface tok, int event, boolean isEndOfItem) {
    }

    /**
	 * Buffer a new token. 
	 * @param token
	 */
    private void bufferToken(TokenInterface token) {
        if ((tokenI - toksDel) == tokenBuffer.length) {
            initialCapacity = (int) (initialCapacity * capacityIncrement);
            TokenInterface[] newTokenBuffer = new TokenInterface[initialCapacity];
            System.arraycopy(tokenBuffer, 0, newTokenBuffer, 0, tokenI - toksDel);
            tokenBuffer = newTokenBuffer;
        }
        tokenBuffer[tokenI - toksDel] = token;
        tokenI++;
    }

    public int getSize() {
        return tokenBuffer.length;
    }

    /**
	 * Simple garbage collection; deletes all nodes that are older than the parameter
	 * @param olderThanItemId
	 */
    public void deleteItems(int olderThanItemId) {
        if (nodeI == 0) return;
        if (olderThanItemId <= 1 || olderThanItemId <= nodesDel) {
            return;
        }
        try {
            int nextTokId = getTokenIdForNode(olderThanItemId);
            System.out.println(this.myId + ": Delete until: " + olderThanItemId + ", Current-Node-Id:" + tokenI + ", Remaining:" + (tokenI - nextTokId));
            initialCapacity = tokenI - nextTokId;
            TokenInterface[] newTokenBuffer = new TokenInterface[initialCapacity];
            System.arraycopy(tokenBuffer, nextTokId - toksDel, newTokenBuffer, 0, tokenI - nextTokId);
            tokenBuffer = newTokenBuffer;
            nodeIndexCapacity = nodeI - olderThanItemId;
            int[] newIndex = new int[nodeIndexCapacity];
            System.arraycopy(nodeIndex, olderThanItemId - nodesDel, newIndex, 0, nodeI - olderThanItemId);
            nodeIndex = null;
            nodeIndex = newIndex;
            if (scores != null) {
                scoreCapacity = nodeI - olderThanItemId;
                MXQueryNumber[] newScores = new MXQueryNumber[scoreCapacity];
                System.arraycopy(scores, olderThanItemId - nodesDel, newScores, 0, nodeI - olderThanItemId);
                scores = newScores;
            }
            toksDel = nextTokId;
            nodesDel = olderThanItemId;
        } catch (MXQueryException e) {
            e.printStackTrace();
        }
    }

    public int getAttributePosFromTokenId(String attrName, int activeTokenId) throws MXQueryException {
        if (!attributes.containsKey(attrName)) return -1;
        int offset = ((Integer) attributes.get(attrName)).intValue();
        int node = getNodeIdFromTokenId(0, activeTokenId);
        int tokenId = getTokenIdForNode(node);
        if (tokenId + offset >= tokenI) return -1;
        return tokenId + offset;
    }

    public int getAttributePosFromNodeId(String attrName, int nodeId) throws MXQueryException {
        if (!attributes.containsKey(attrName)) return -1;
        int offset = ((Integer) attributes.get(attrName)).intValue();
        int tokenId = getTokenIdForNode(nodeId);
        TokenInterface tok = get(tokenId + offset, nodeId);
        if (tok == Token.END_SEQUENCE_TOKEN) return -1;
        if (tokenId + offset >= tokenI) return -1;
        return tokenId + offset;
    }

    public int getMyId() {
        return myId;
    }

    public void newItem() {
    }

    public void endStream() {
    }

    public Iterator getIterator(Context ctx) throws MXQueryException {
        Window wnd = cont.getNewWindowIterator(1, Window.END_OF_STREAM_POSITION);
        wnd.setContext(ctx, false);
        return wnd;
    }

    public int compare(Source store) {
        return -1;
    }

    /**
	 * Returns the URI of the source.
	 * 
	 * @return
	 */
    public String getURI() {
        return "";
    }

    public Source copySource(Context ctx, Vector nestedPredCtxStack) throws MXQueryException {
        return new TokenBufferStore(this.sourceStream, cont);
    }

    public void setContainer(WindowBuffer buf) {
        cont = buf;
    }

    public MXQueryNumber getScoreForItem(int nodeId) throws MXQueryException {
        if (!isScoring) return new MXQueryDouble(0);
        if (nodeId < nodeI) {
            int arrayId = nodeId - nodesDel;
            if (arrayId < 0) {
            }
            return scores[arrayId];
        } else {
            while (nodeI <= nodeId && !endOfStr) {
                bufferNext();
            }
            if (endOfStr) {
                return new MXQueryDouble(0);
            } else {
                int arrayId = nodeId - nodesDel;
                return scores[arrayId];
            }
        }
    }

    public void setScoring(boolean b) {
        isScoring = b;
        if (b) scores = new MXQueryNumber[scoreCapacity]; else scores = null;
    }

    public boolean isScoring() {
        return isScoring;
    }

    /**
	 * Find the token position of the root/start token associated with the node identified 
	 * @param xdmId the XDM node identifier of the node to search for
	 * @param onlyWhenFound true: only return a position if an exactly matching is found, otherwise return token position to the "left"
	 * @param peerToMatch If not found, check if a candidate in the remaining sequence is the 
	 * @return -1 if not found and strict match, the exact or possible
	 */
    protected int getTokenIndexForNodeIndentifier(Identifier xdmId, boolean onlyWhenFound, TokenInterface peerToMatch) {
        int lPos = 0;
        int rPos = tokenI - 1;
        int midPos;
        do {
            midPos = (lPos + rPos) / 2;
            Identifier mid = tokenBuffer[midPos].getNodeId();
            while (mid == null && midPos > lPos + 1) {
                midPos = midPos - 1;
                mid = tokenBuffer[midPos].getNodeId();
            }
            while (mid == null && midPos < rPos - 1) {
                midPos = midPos + 1;
                mid = tokenBuffer[midPos].getNodeId();
            }
            if (mid == null) break;
            int cmpRes = xdmId.compare(mid);
            switch(cmpRes) {
                case 0:
                    return midPos;
                case 1:
                    rPos = midPos;
                    break;
                case -1:
                    lPos = midPos;
                    break;
                default:
                    return -1;
            }
        } while (rPos - lPos > 1);
        if (xdmId.compare(tokenBuffer[lPos].getNodeId()) == 0) return lPos;
        if (xdmId.compare(tokenBuffer[rPos].getNodeId()) == 0) return rPos;
        if (onlyWhenFound) return -1; else for (int curPos = lPos; curPos <= rPos; curPos++) {
            if (peerToMatch != null) {
                if (peerToMatch.getEventType() == Type.START_DOCUMENT && tokenBuffer[curPos].getEventType() == Type.END_DOCUMENT) return curPos;
                if (peerToMatch.getEventType() == Type.START_TAG && tokenBuffer[curPos].getEventType() == Type.END_TAG) {
                    NamedToken tokStart = (NamedToken) peerToMatch;
                    NamedToken tokEnd = (NamedToken) tokenBuffer[curPos];
                    if (tokEnd.getName().equals(tokStart.getName()) && ((tokEnd.getNS() == null && tokStart.getNS() == null) || (tokEnd.getNS() != null && tokEnd.getNS().equals(tokStart.getNS())))) return curPos;
                }
                if (peerToMatch.getEventType() != Type.START_DOCUMENT && peerToMatch.getEventType() != Type.START_TAG) return curPos;
            }
            if (xdmId.compare(tokenBuffer[curPos].getNodeId()) == 1) return curPos - 1;
        }
        return rPos;
    }

    public boolean areOptionsSupported(int requiredOptions) {
        return false;
    }

    public StreamStoreStatistics getStoreStatistics() {
        return null;
    }

    public void resetVariantStatistics() {
    }
}
