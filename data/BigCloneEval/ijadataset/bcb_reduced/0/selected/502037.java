package com.jaeksoft.searchlib.snippet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermPositionVector;
import com.jaeksoft.searchlib.index.term.TermVectorOffsetInfo;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SnippetField extends Field {

    private FragmenterAbstract fragmenterTemplate;

    private String tag;

    private String[] tags;

    private int maxDocChar;

    private String separator;

    private String unescapedSeparator;

    private int maxSnippetSize;

    private String[] searchTerms;

    private Query query;

    private Analyzer analyzer;

    public SnippetField() {
    }

    private SnippetField(String fieldName, String tag, int maxDocChar, String separator, int maxSnippetSize, FragmenterAbstract fragmenterTemplate) {
        super(fieldName);
        this.searchTerms = null;
        setTag(tag);
        this.maxDocChar = maxDocChar;
        setSeparator(separator);
        this.maxSnippetSize = maxSnippetSize;
        this.fragmenterTemplate = fragmenterTemplate;
    }

    public SnippetField(String fieldName) {
        this(fieldName, "em", Integer.MAX_VALUE, "...", 200, FragmenterAbstract.NOFRAGMENTER);
    }

    @Override
    public Field duplicate() {
        return new SnippetField(name, tag, maxDocChar, separator, maxSnippetSize, fragmenterTemplate);
    }

    public String getFragmenter() {
        return fragmenterTemplate.getClass().getSimpleName();
    }

    public void setFragmenter(String fragmenterName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        fragmenterTemplate = FragmenterAbstract.newInstance(fragmenterName);
    }

    /**
	 * @return the tag
	 */
    public String getTag() {
        return tag;
    }

    /**
	 * @param tag
	 *            the tag to set
	 */
    public void setTag(String tag) {
        this.tag = tag;
        if (tag != null && tag.length() > 0) {
            tags = new String[2];
            tags[0] = '<' + tag + '>';
            tags[1] = "</" + tag + '>';
        } else tags = null;
    }

    /**
	 * @return the maxDocChar
	 */
    public int getMaxDocChar() {
        return maxDocChar;
    }

    /**
	 * @param maxDocChar
	 *            the maxDocChar to set
	 */
    public void setMaxDocChar(int maxDocChar) {
        this.maxDocChar = maxDocChar;
    }

    /**
	 * @return the separator
	 */
    public String getSeparator() {
        return separator;
    }

    /**
	 * @param separator
	 *            the separator to set
	 */
    public void setSeparator(String separator) {
        this.separator = separator;
        unescapedSeparator = separator == null ? null : StringEscapeUtils.unescapeHtml(separator);
    }

    /**
	 * @return the maxSnippetSize
	 */
    public int getMaxSnippetSize() {
        return maxSnippetSize;
    }

    /**
	 * @param maxSnippetSize
	 *            the maxSnippetSize to set
	 */
    public void setMaxSnippetSize(int maxSnippetSize) {
        this.maxSnippetSize = maxSnippetSize;
    }

    /**
	 * Retourne la liste des champs "snippet".
	 * 
	 * @param xPath
	 * @param node
	 * @param target
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
    public static void copySnippetFields(Node node, FieldList<SchemaField> source, FieldList<SnippetField> target) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String fieldName = XPathParser.getAttributeString(node, "name");
        String tag = XPathParser.getAttributeString(node, "tag");
        if (tag == null) tag = "em";
        int maxDocChar = XPathParser.getAttributeValue(node, "maxDocBytes");
        if (maxDocChar == 0) XPathParser.getAttributeValue(node, "maxDocChar");
        if (maxDocChar == 0) maxDocChar = Integer.MAX_VALUE;
        int maxSnippetNumber = XPathParser.getAttributeValue(node, "maxSnippetNumber");
        if (maxSnippetNumber == 0) maxSnippetNumber = 1;
        int maxSnippetSize = XPathParser.getAttributeValue(node, "maxSnippetSize");
        if (maxSnippetSize == 0) maxSnippetSize = 200;
        FragmenterAbstract fragmenter = FragmenterAbstract.newInstance(XPathParser.getAttributeString(node, "fragmenterClass"));
        fragmenter.setAttributes(node.getAttributes());
        String separator = XPathParser.getAttributeString(node, "separator");
        if (separator == null) separator = "...";
        SnippetField field = new SnippetField(source.get(fieldName).getName(), tag, maxDocChar, separator, maxSnippetSize, fragmenter);
        target.add(field);
    }

    private Iterator<TermVectorOffsetInfo> extractTermVectorIterator(int docId, ReaderLocal reader) throws IOException, ParseException, SyntaxError, SearchLibException {
        if (searchTerms == null) return null;
        if (searchTerms.length == 0) return null;
        TermPositionVector termVector = (TermPositionVector) reader.getTermFreqVector(docId, name);
        if (termVector == null) return null;
        int[] termsIdx = termVector.indexesOf(searchTerms, 0, searchTerms.length);
        TreeMap<Integer, TermVectorOffsetInfo> mapStart = new TreeMap<Integer, TermVectorOffsetInfo>();
        TreeMap<Integer, TermVectorOffsetInfo> mapEnd = new TreeMap<Integer, TermVectorOffsetInfo>();
        for (int termId : termsIdx) {
            if (termId == -1) continue;
            TermVectorOffsetInfo[] offsets = termVector.getOffsets(termId);
            for (TermVectorOffsetInfo offset : offsets) {
                int start = offset.getStartOffset();
                int end = offset.getEndOffset();
                TermVectorOffsetInfo o = mapStart.get(start);
                if (o == null || o.getEndOffset() < offset.getEndOffset()) mapStart.put(start, offset);
                o = mapEnd.get(end);
                if (o == null || o.getStartOffset() > offset.getStartOffset()) mapEnd.put(end, offset);
            }
        }
        TreeMap<Integer, TermVectorOffsetInfo> finalMap = new TreeMap<Integer, TermVectorOffsetInfo>();
        for (Map.Entry<Integer, TermVectorOffsetInfo> entry : mapStart.entrySet()) if (mapEnd.containsValue(entry.getValue())) finalMap.put(entry.getKey(), entry.getValue());
        return finalMap.values().iterator();
    }

    public void initSearchTerms(SearchRequest searchRequest) throws ParseException, SyntaxError, IOException, SearchLibException {
        synchronized (this) {
            this.query = searchRequest.getSnippetQuery();
            this.analyzer = searchRequest.getAnalyzer();
            Set<Term> terms = new HashSet<Term>();
            query.extractTerms(terms);
            String[] tempTerms = new String[terms.size()];
            int i = 0;
            for (Term term : terms) if (name.equalsIgnoreCase(term.field())) tempTerms[i++] = term.text();
            String[] finalTerms = new String[i];
            for (i = 0; i < finalTerms.length; i++) finalTerms[i] = tempTerms[i];
            searchTerms = finalTerms;
        }
    }

    private final void appendSubString(String text, int start, int end, StringBuffer sb) {
        if (text == null) return;
        int l = text.length();
        if (end > l) end = l;
        if (end < start) return;
        sb.append(text.substring(start, end));
    }

    private final TermVectorOffsetInfo checkValue(TermVectorOffsetInfo currentVector, Iterator<TermVectorOffsetInfo> vectorIterator, int startOffset, Fragment fragment) {
        if (currentVector == null) return null;
        StringBuffer result = new StringBuffer();
        String originalText = fragment.getOriginalText();
        int originalTextLength = originalText.length();
        int endOffset = startOffset + originalTextLength;
        int pos = 0;
        while (currentVector != null) {
            int end = currentVector.getEndOffset() - fragment.vectorOffset;
            if (end > endOffset) break;
            int start = currentVector.getStartOffset() - fragment.vectorOffset;
            if (start >= startOffset) {
                appendSubString(originalText, pos, start - startOffset, result);
                if (tags != null) result.append(tags[0]);
                appendSubString(originalText, start - startOffset, end - startOffset, result);
                if (tags != null) result.append(tags[1]);
                pos = end - startOffset;
            }
            currentVector = vectorIterator.hasNext() ? vectorIterator.next() : null;
        }
        if (result.length() == 0) return currentVector;
        if (pos < originalTextLength) appendSubString(originalText, pos, originalTextLength, result);
        fragment.setHighlightedText(result.toString());
        return currentVector;
    }

    public boolean getSnippets(int docId, ReaderLocal reader, FieldValueItem[] values, List<FieldValueItem> snippets) throws IOException, ParseException, SyntaxError, SearchLibException {
        if (values == null) return false;
        FragmenterAbstract fragmenter = fragmenterTemplate.newInstance();
        TermVectorOffsetInfo currentVector = null;
        Iterator<TermVectorOffsetInfo> vectorIterator = extractTermVectorIterator(docId, reader);
        if (vectorIterator != null) currentVector = vectorIterator.hasNext() ? vectorIterator.next() : null;
        int startOffset = 0;
        FragmentList fragments = new FragmentList();
        int vectorOffset = 0;
        for (FieldValueItem valueItem : values) {
            String value = valueItem.getValue();
            if (value != null) {
                fragmenter.getFragments(value, fragments, vectorOffset++);
                if (fragments.getTotalSize() > maxDocChar) break;
            }
        }
        if (fragments.size() == 0) return false;
        Fragment fragment = fragments.first();
        while (fragment != null) {
            currentVector = checkValue(currentVector, vectorIterator, startOffset, fragment);
            startOffset += fragment.getOriginalText().length();
            fragment = fragment.next();
        }
        Fragment bestScoreFragment = null;
        fragment = Fragment.findNextHighlightedFragment(fragments.first());
        while (fragment != null) {
            fragment.score(name, analyzer, query, maxSnippetSize);
            bestScoreFragment = Fragment.bestScore(bestScoreFragment, fragment);
            fragment = Fragment.findNextHighlightedFragment(fragment.next());
        }
        if (bestScoreFragment != null) {
            StringBuffer snippet = fragments.getSnippet(maxSnippetSize, unescapedSeparator, tags, bestScoreFragment);
            if (snippet != null) if (snippet.length() > 0) snippets.add(new FieldValueItem(snippet.toString()));
            if (snippets.size() > 0) return true;
        }
        StringBuffer snippet = fragments.getSnippet(maxSnippetSize, unescapedSeparator, tags, fragments.first());
        if (snippet != null) if (snippet.length() > 0) snippets.add(new FieldValueItem(snippet.toString()));
        return false;
    }

    @Override
    public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
        xmlWriter.startElement("field", "name", name, "tag", tag, "maxDocChar", Integer.toString(maxDocChar), "separator", separator, "maxSnippetSize", Integer.toString(maxSnippetSize), "fragmenterClass", fragmenterTemplate != null ? fragmenterTemplate.getClass().getSimpleName() : null);
        xmlWriter.endElement();
    }

    @Override
    public int compareTo(Field o) {
        int c = super.compareTo(o);
        if (c != 0) return c;
        SnippetField f = (SnippetField) o;
        if ((c = fragmenterTemplate.getClass().getName().compareTo(f.fragmenterTemplate.getClass().getName())) != 0) return c;
        if ((c = tag.compareTo(f.tag)) != 0) return c;
        if ((c = maxDocChar - f.maxDocChar) != 0) return c;
        if ((c = separator.compareTo(f.separator)) != 0) return c;
        if ((c = maxSnippetSize - f.maxSnippetSize) != 0) return c;
        return 0;
    }
}
