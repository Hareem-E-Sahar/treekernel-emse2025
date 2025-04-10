package org.exist.xquery.modules.fulltext;

import java.util.ArrayList;
import java.util.List;
import org.exist.dom.Match;
import org.exist.dom.NodeProxy;
import org.exist.dom.QName;
import org.exist.dom.TextImpl;
import org.exist.memtree.DocumentBuilderReceiver;
import org.exist.memtree.MemTreeBuilder;
import org.exist.util.FastQSort;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionCall;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionReference;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.xml.sax.SAXException;

public class KWICDisplay extends BasicFunction {

    public static final FunctionSignature signatures[] = { new FunctionSignature(new QName("kwic-display", FulltextModule.NAMESPACE_URI, FulltextModule.PREFIX), "This function takes a sequence of text nodes in $a, containing matches from a fulltext search. " + "It highlights matching strings within those text nodes in the same way as the text:highlight-matches " + "function. However, only a defined portion of the text surrounding the first match (and maybe following matches) " + "is returned. If the text preceding the first match is larger than the width specified in the second argument $b, " + "it will be truncated to fill no more than (width - keyword-length) / 2 characters. Likewise, the text following " + "the match will be truncated in such a way that the whole string sequence fits into width characters. " + "The third parameter $c is a callback function (defined with util:function). $d may contain an additional sequence of " + "values that will be passed to the last parameter of the callback function. Any matching character sequence is reported " + "to the callback function, and the " + "result of the function call is inserted into the resulting node set where the matching sequence occurred. " + "For example, you can use this to mark all matching terms with a <span class=\"highlight\">abc</span>. " + "The callback function should take 3 or 4 arguments: 1) the text sequence corresponding to the match as xs:string, " + "2) the text node to which this match belongs, 3) the sequence passed as last argument to kwic-display. " + "If the callback function accepts 4 arguments, the last argument will contain additional " + "information on the match as a sequence of 4 integers: a) the number of the match if there's more than " + "one match in a text node - the first match will be numbered 1; b) the offset of the match into the original text node " + "string; c) the length of the match as reported by the index.", new SequenceType[] { new SequenceType(Type.TEXT, Cardinality.ZERO_OR_MORE), new SequenceType(Type.POSITIVE_INTEGER, Cardinality.EXACTLY_ONE), new SequenceType(Type.FUNCTION_REFERENCE, Cardinality.EXACTLY_ONE), new SequenceType(Type.ITEM, Cardinality.ZERO_OR_MORE) }, new SequenceType(Type.NODE, Cardinality.ZERO_OR_MORE)), new FunctionSignature(new QName("kwic-display", FulltextModule.NAMESPACE_URI, FulltextModule.PREFIX), "This function takes a sequence of text nodes in $a, containing matches from a fulltext search. " + "It highlights matching strings within those text nodes in the same way as the text:highlight-matches " + "function. However, only a defined portion of the text surrounding the first match (and maybe following matches) " + "is returned. If the text preceding the first match is larger than the width specified in the second argument $b, " + "it will be truncated to fill no more than (width - keyword-length) / 2 characters. Likewise, the text following " + "the match will be truncated in such a way that the whole string sequence fits into width characters. " + "The third parameter $c is a callback function (defined with util:function). $d may contain an additional sequence of " + "values that will be passed to the last parameter of the callback function. Any matching character sequence is reported " + "to the callback function, and the " + "result of the function call is inserted into the resulting node set where the matching sequence occurred. " + "For example, you can use this to mark all matching terms with a <span class=\"highlight\">abc</span>. " + "The callback function should take 3 or 4 arguments: 1) the text sequence corresponding to the match as xs:string, " + "2) the text node to which this match belongs, 3) the sequence passed as last argument to kwic-display. " + "If the callback function accepts 4 arguments, the last argument will contain additional " + "information on the match as a sequence of 4 integers: a) the number of the match if there's more than " + "one match in a text node - the first match will be numbered 1; b) the offset of the match into the original text node " + "string; c) the length of the match as reported by the index.", new SequenceType[] { new SequenceType(Type.TEXT, Cardinality.ZERO_OR_MORE), new SequenceType(Type.POSITIVE_INTEGER, Cardinality.EXACTLY_ONE), new SequenceType(Type.FUNCTION_REFERENCE, Cardinality.EXACTLY_ONE), new SequenceType(Type.FUNCTION_REFERENCE, Cardinality.EXACTLY_ONE), new SequenceType(Type.ITEM, Cardinality.ZERO_OR_MORE) }, new SequenceType(Type.NODE, Cardinality.ZERO_OR_MORE)) };

    public KWICDisplay(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        if (args[0].isEmpty()) return Sequence.EMPTY_SEQUENCE;
        FunctionReference func = (FunctionReference) args[2].itemAt(0);
        FunctionCall call = func.getFunctionCall();
        FunctionCall resultCallback = null;
        if (getArgumentCount() == 5) {
            func = (FunctionReference) args[3].itemAt(0);
            resultCallback = func.getFunctionCall();
        }
        int width = ((IntegerValue) args[1].itemAt(0)).getInt();
        context.pushDocumentContext();
        MemTreeBuilder builder = context.getDocumentBuilder();
        Sequence result = processText(builder, args[0], width, call, resultCallback, args[getArgumentCount() - 1]);
        context.popDocumentContext();
        return result;
    }

    private final Sequence processText(MemTreeBuilder builder, Sequence nodes, int width, FunctionCall callback, FunctionCall resultCallback, Sequence extraArgs) throws XPathException {
        StringBuffer str = new StringBuffer();
        NodeValue node;
        List offsets = null;
        NodeProxy firstProxy = null;
        for (SequenceIterator i = nodes.iterate(); i.hasNext(); ) {
            node = (NodeValue) i.nextItem();
            if (node.getImplementationType() == NodeValue.IN_MEMORY_NODE) throw new XPathException(getASTNode(), "Function kwic-display" + " can not be invoked on constructed nodes");
            NodeProxy proxy = (NodeProxy) node;
            if (firstProxy == null) firstProxy = proxy;
            TextImpl text = (TextImpl) proxy.getNode();
            Match next = proxy.getMatches();
            while (next != null) {
                if (next.getNodeId().equals(text.getNodeId())) {
                    if (offsets == null) offsets = new ArrayList();
                    int freq = next.getFrequency();
                    for (int j = 0; j < freq; j++) {
                        Match.Offset offset = next.getOffset(j);
                        offset.setOffset(str.length() + offset.getOffset());
                        offsets.add(offset);
                    }
                }
                next = next.getNextMatch();
            }
            str.append(text.getData());
        }
        ValueSequence result = new ValueSequence();
        DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(builder);
        int nodeNr;
        int currentWidth = 0;
        if (offsets == null) {
            if (width > str.length()) width = str.length();
            nodeNr = builder.characters(str.substring(0, width));
            result.add(builder.getDocument().getNode(nodeNr));
            currentWidth += width;
        } else {
            FastQSort.sort(offsets, 0, offsets.size() - 1);
            int nextOffset = 0;
            int pos = 0;
            int lastNodeNr = -1;
            Sequence params[] = new Sequence[callback.getSignature().getArgumentCount()];
            params[1] = firstProxy;
            params[2] = extraArgs;
            if (str.length() > width) {
                Match.Offset firstMatch = (Match.Offset) offsets.get(nextOffset++);
                if (firstMatch.getOffset() > 0) {
                    int leftWidth = (width - firstMatch.getLength()) / 2;
                    if (firstMatch.getOffset() > leftWidth) {
                        pos = truncateStart(str, firstMatch.getOffset() - leftWidth, firstMatch.getOffset());
                        leftWidth = firstMatch.getOffset() - pos;
                    } else leftWidth = firstMatch.getOffset();
                    nodeNr = builder.characters(str.substring(pos, pos + leftWidth));
                    if (lastNodeNr != nodeNr) result.add(builder.getDocument().getNode(nodeNr));
                    lastNodeNr = nodeNr;
                    currentWidth += leftWidth;
                    pos += leftWidth;
                }
                params[0] = new StringValue(str.substring(firstMatch.getOffset(), firstMatch.getOffset() + firstMatch.getLength()));
                if (callback.getSignature().getArgumentCount() == 4) {
                    params[3] = new ValueSequence();
                    params[3].add(new IntegerValue(nextOffset - 1));
                    params[3].add(new IntegerValue(firstMatch.getOffset()));
                    params[3].add(new IntegerValue(firstMatch.getLength()));
                }
                Sequence callbackResult = callback.evalFunction(null, null, params);
                for (SequenceIterator iter = callbackResult.iterate(); iter.hasNext(); ) {
                    Item next = iter.nextItem();
                    if (Type.subTypeOf(next.getType(), Type.NODE)) {
                        nodeNr = builder.getDocument().getLastNode();
                        try {
                            next.copyTo(context.getBroker(), receiver);
                            result.add(builder.getDocument().getNode(++nodeNr));
                            lastNodeNr = nodeNr;
                        } catch (SAXException e) {
                            throw new XPathException(getASTNode(), "Internal error while copying nodes: " + e.getMessage(), e);
                        }
                    }
                }
                currentWidth += firstMatch.getLength();
                pos += firstMatch.getLength();
            } else width = str.length();
            Match.Offset offset;
            for (int i = nextOffset; i < offsets.size() && currentWidth < width; i++) {
                offset = (Match.Offset) offsets.get(i);
                if (offset.getOffset() > pos) {
                    int len = offset.getOffset() - pos;
                    if (currentWidth + len > width) len = width - currentWidth;
                    nodeNr = builder.characters(str.substring(pos, pos + len));
                    if (lastNodeNr != nodeNr) result.add(builder.getDocument().getNode(nodeNr));
                    currentWidth += len;
                    pos += len;
                }
                if (currentWidth + offset.getLength() < width) {
                    params[0] = new StringValue(str.substring(offset.getOffset(), offset.getOffset() + offset.getLength()));
                    if (callback.getSignature().getArgumentCount() == 4) {
                        params[3] = new ValueSequence();
                        params[3].add(new IntegerValue(i));
                        params[3].add(new IntegerValue(offset.getOffset()));
                        params[3].add(new IntegerValue(offset.getLength()));
                    }
                    Sequence callbackResult = callback.evalFunction(null, null, params);
                    for (SequenceIterator iter = callbackResult.iterate(); iter.hasNext(); ) {
                        Item next = iter.nextItem();
                        if (Type.subTypeOf(next.getType(), Type.NODE)) {
                            nodeNr = builder.getDocument().getLastNode();
                            try {
                                next.copyTo(context.getBroker(), receiver);
                                result.add(builder.getDocument().getNode(++nodeNr));
                                lastNodeNr = nodeNr;
                            } catch (SAXException e) {
                                throw new XPathException(getASTNode(), "Internal error while copying nodes: " + e.getMessage(), e);
                            }
                        }
                    }
                    currentWidth += offset.getLength();
                    pos += offset.getLength();
                } else break;
            }
            if (currentWidth < width && pos < str.length()) {
                boolean truncated = false;
                int len = str.length() - pos;
                if (len > width - currentWidth) {
                    truncated = true;
                    len = width - currentWidth;
                }
                nodeNr = builder.characters(str.substring(pos, pos + len));
                if (lastNodeNr != nodeNr) result.add(builder.getDocument().getNode(nodeNr));
                lastNodeNr = nodeNr;
                currentWidth += len;
                if (truncated) {
                    nodeNr = builder.characters(" ...");
                    if (lastNodeNr != nodeNr) result.add(builder.getDocument().getNode(nodeNr));
                    lastNodeNr = nodeNr;
                }
            }
        }
        if (resultCallback != null) {
            Sequence params[] = new Sequence[3];
            params[0] = result;
            params[1] = new IntegerValue(currentWidth);
            params[2] = extraArgs;
            return resultCallback.evalFunction(null, null, params);
        } else return result;
    }

    private static final int truncateStart(StringBuffer buf, int start, int end) {
        if (start > 0 && !Character.isLetterOrDigit(buf.charAt(start - 1))) return start;
        while (start < end && Character.isLetterOrDigit(buf.charAt(start))) {
            start++;
        }
        while (start < end && !Character.isLetterOrDigit(buf.charAt(start))) {
            start++;
        }
        return start;
    }
}
