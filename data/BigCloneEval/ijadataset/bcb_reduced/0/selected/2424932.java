package proxypedia.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wikimodel.wem.IWemListener;
import org.wikimodel.wem.WikiParserException;
import org.wikimodel.wem.WikiPrinter;
import org.wikimodel.wem.mediawiki.MediaWikiParser;

public class WikiMediaToCreoleConverter {

    private StringBuffer s;

    private UP2PediaListener output;

    public WikiMediaToCreoleConverter() {
        s = new StringBuffer();
        output = new UP2PediaListener(new WikiPrinter(s));
    }

    public List<String> getImageLinks() {
        return output.getAttachmentList();
    }

    /** convert an input text using MediaWiki syntax to simplified Creole Syntax.
	 * Note : some content will not be rendered, this is a lossy process !
	 * @param inreader a reader to access the input text
	 * @return the result as a String
	 * @throws IOException if there is a problem opening the input
	 * @throws WikiParserException if an exception occurs while parsing the input text
	 */
    public String convert(Reader inreader) throws IOException, WikiParserException {
        BufferedReader br = new BufferedReader(inreader);
        MediaWikiParser newParser = new MediaWikiParser();
        newParser.parse(br, output);
        return s.toString();
    }

    /**
	 * additional manual processing to work around the used parser's shortcomings
	 * @param input
	 * @return
	 */
    public String preprocess(String wikitext) {
        Pattern linkInALink = Pattern.compile("\\[\\[[^\\]]*(\\[\\[.*\\]\\]).*\\]\\]");
        Pattern innerlink = Pattern.compile("\\[\\[[^\\[]*?\\]\\]");
        Matcher m = linkInALink.matcher(wikitext);
        String processed = new String("");
        int mindex = 0;
        while (m.find()) {
            String toreplace = m.group();
            Matcher m2 = innerlink.matcher(toreplace);
            processed = processed + wikitext.substring(mindex, m.start());
            int currentindex = 0;
            String replaced = new String("");
            while (m2.find()) {
                String linktext = m2.group().substring(2, m2.group().length() - 2);
                String[] sp = linktext.split("\\|");
                if (sp.length == 2) {
                    linktext = sp[1];
                }
                replaced = replaced + toreplace.substring(currentindex, m2.start()) + linktext;
                currentindex = m2.end();
            }
            replaced = replaced + toreplace.substring(currentindex);
            processed = processed + replaced;
            mindex = m.end();
        }
        processed = processed + wikitext.substring(mindex);
        Pattern linksequence = Pattern.compile("(\\[\\[[^\\[\\]]*\\]\\][\r\n]*)+");
        Matcher fm = linksequence.matcher(wikitext.substring(mindex));
        int tail = 0;
        while (fm.find()) {
            if (fm.hitEnd()) {
                tail = fm.group().length();
            }
        }
        processed = processed.substring(0, processed.length() - tail);
        return processed;
    }
}
