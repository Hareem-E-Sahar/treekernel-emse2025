package ch.uzh.ifi.attempto.aceview.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import com.google.common.collect.Maps;
import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACEToken;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexiconEntry;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;

/**
 * <p>Saves the given ACE text in AceWiki internal format. Each OWL entity gets a
 * corresponding article that is filled with every snippet that contains a reference
 * to this entity. This means that the same snippet shows up in different articles
 * (which is not how things are meant to be normally in an AceWiki). This will change
 * once ACE View offers something similar to the article-category offered
 * by AceWiki, e.g. ACE View could have a possibility to tag snippets with "article tags".</p>
 * 
 * <p>{@link #render()} generates an output which looks something like this.</p>
 * 
 * <pre>
 * ID 1
 * type:propername
 * words:the Attempto_project;Attempto_project;the Attempto_project;Attempto_project;
 * | <1,0> is a <2,0> that is a <8,0> <9,0> .
 * | <1,0> is <4,0> <5,0> and is <4,0> <6,0> .
 * | <1,0> is <10,0> <11,0> that is a <12,0> .
 * 
 * ID 97
 * type:noun
 * words:screencast;screencasts;
 * | Every <97,0> is a <98,0> .
 * 
 * ID 100
 * type:trverb
 * words:shows;show;shown by;
 * </pre>
 * 
 * <p>If {@link #createZipFile(String, String)} is called, then each article is saved into a separate
 * file and there is no need for the "ID" lines.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class AceWikiRenderer {

    private final ACEText<OWLEntity, OWLLogicalAxiom> acetext;

    private final ACELexicon<OWLEntity> lexicon;

    private final Map<OWLEntity, Integer> seen = Maps.newHashMap();

    private int counter = 0;

    public AceWikiRenderer(ACEText<OWLEntity, OWLLogicalAxiom> acetext) {
        this.acetext = acetext;
        this.lexicon = acetext.getACELexicon();
    }

    /**
	 * <p>Returns a string that represents a non-standard AceWiki format. Non-standard because
	 * multiple articles are merged into one string and separated by "ID"-lines. AceWiki does not
	 * support this format. It is generated to allow for quick checking in a ViewComponent.</p>
	 * 
	 * @return String that represents that complete AceWiki content
	 */
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<OWLEntity, Set<ACESnippet>> entry : acetext.getEntitySnippetSetPairs()) {
            OWLEntity entity = entry.getKey();
            ACELexiconEntry lexiconEntry = lexicon.getEntry(entity);
            if (lexiconEntry == null || lexiconEntry.isPartial()) {
                continue;
            }
            sb.append("ID ");
            sb.append(getLemmaIndex(entity));
            sb.append('\n');
            sb.append(getAceWikiEntry(entity, lexiconEntry, entry.getValue()));
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
	 * <p>Creates a zip-file that contains a single directory that contains all
	 * the AceWiki articles.</p>
	 * 
	 * @param zipFileName Name of the zip-file
	 * @param directoryName Name of the single directory in the zip-file
	 * @throws IOException
	 */
    public void createZipFile(String zipFileName, String directoryName) throws IOException {
        String directoryNameWithSlash = directoryName + "/";
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFileName));
        zip.putNextEntry(new ZipEntry(directoryNameWithSlash));
        for (Map.Entry<OWLEntity, Set<ACESnippet>> entry : acetext.getEntitySnippetSetPairs()) {
            OWLEntity entity = entry.getKey();
            ACELexiconEntry lexiconEntry = lexicon.getEntry(entity);
            if (lexiconEntry == null || lexiconEntry.isPartial()) {
                continue;
            }
            zip.putNextEntry(new ZipEntry(directoryNameWithSlash + getLemmaIndex(entity)));
            String str = getAceWikiEntry(entity, lexiconEntry, entry.getValue());
            zip.write(str.getBytes(), 0, str.length());
            zip.closeEntry();
        }
        zip.closeEntry();
        zip.close();
    }

    private String snippetToAceWiki(ACESnippet snippet) {
        StringBuilder sb = new StringBuilder();
        int lastIndex = snippet.getSentences().size() - 1;
        for (int i = 0; i <= lastIndex; i++) {
            sb.append(sentenceToAceWiki(snippet.getSentences().get(i)));
            if (i != lastIndex) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private String sentenceToAceWiki(ACESentence sentence) {
        StringBuilder sb = new StringBuilder();
        int lastIndex = sentence.size() - 1;
        int counter = 0;
        for (ACEToken token : sentence.getTokens()) {
            if (token.isFunctionWord()) {
                sb.append(token);
            } else {
                String wordform = token.toString();
                OWLEntity entity = lexicon.getWordformEntity(wordform);
                long lemmaIndex = 0;
                int formIndex = 0;
                if (entity != null) {
                    lemmaIndex = getLemmaIndex(entity);
                    formIndex = lexicon.getEntry(entity).getFieldType(wordform).ordinal();
                }
                sb.append('<');
                sb.append(lemmaIndex);
                sb.append(',');
                sb.append(formIndex);
                sb.append('>');
            }
            if (counter < lastIndex) {
                sb.append(' ');
            }
            counter++;
        }
        return sb.toString();
    }

    private long getLemmaIndex(OWLEntity entity) {
        if (seen.containsKey(entity)) {
            return seen.get(entity);
        }
        counter++;
        seen.put(entity, counter);
        return counter;
    }

    /**
	 * <p>Ignores snippets which have no ACE content,
	 * e.g. verbalization of an OWL axiom has failed
	 * and the snippet is empty (has 0 sentences) although it contains an OWL axiom.</p>
	 * 
	 * @param entity OWL entity
	 * @param lexiconEntry ACE lexicon entry
	 * @param snippets Set of ACE snippets
	 * @return AceWiki article rendering
	 */
    private String getAceWikiEntry(OWLEntity entity, ACELexiconEntry lexiconEntry, Set<ACESnippet> snippets) {
        StringBuilder sb = new StringBuilder();
        sb.append("type:");
        sb.append(LexiconUtils.getLexiconEntryType(entity).toAceWikiType());
        sb.append('\n');
        sb.append("words:");
        sb.append(lexiconEntry.toAceWikiFormat());
        sb.append('\n');
        for (ACESnippet snippet : snippets) {
            if (!snippet.isEmpty()) {
                sb.append("| ");
                sb.append(snippetToAceWiki(snippet));
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
