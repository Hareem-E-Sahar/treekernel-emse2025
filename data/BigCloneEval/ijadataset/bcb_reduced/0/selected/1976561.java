package org.pubcurator.analyzers.maxmatcher.annotators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.pubcurator.analyzers.maxmatcher.misc.MaxMatcher;
import org.pubcurator.analyzers.maxmatcher.misc.MaxMatcherCategoryMapper;
import org.pubcurator.uima.annotator.AnalyzerAnnotator;
import org.pubcurator.uima.config.Category;
import org.pubcurator.uima.definitions.PredefinedIdentifierTypes;
import org.pubcurator.uima.ts.PubTerm;
import dragon.nlp.Concept;
import dragon.nlp.Word;

/**
 * @author Kai Schlamp (schlamp@gmx.de)
 *
 */
public class MaxMatcherAnnotator extends AnalyzerAnnotator {

    public static final String DRAGON_HOME_PATH_PARAMETER = "dragon_home_path";

    public static final String CONFIGURE_PATH_PARAMETER = "configure_path";

    private String dragonHomePath;

    private String configurePath;

    private MaxMatcherCategoryMapper categoryMapper;

    private MaxMatcher maxMatcher;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        initParameters();
        categoryMapper = new MaxMatcherCategoryMapper();
        maxMatcher = new MaxMatcher(dragonHomePath, configurePath);
    }

    private void initParameters() {
        dragonHomePath = (String) getContext().getConfigParameterValue(DRAGON_HOME_PATH_PARAMETER);
        configurePath = (String) getContext().getConfigParameterValue(CONFIGURE_PATH_PARAMETER);
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        String text = jcas.getDocumentText();
        List<PubTerm> addedTerms = new ArrayList<PubTerm>();
        List<Concept> concepts = maxMatcher.extract(jcas.getDocumentText());
        for (Concept concept : concepts) {
            Word startWord = concept.getStartingWord();
            int startWordPos = startWord.getPosInSentence();
            Word endWord = concept.getEndingWord();
            int endWordPos = endWord.getPosInSentence();
            String searchString = null;
            if (startWord.getContent().equals(endWord.getContent()) && startWordPos == endWordPos) {
                searchString = startWord.getContent();
            } else {
                if (startWordPos < endWordPos) {
                    StringBuffer buffer = new StringBuffer();
                    Word word = startWord;
                    for (int i = startWordPos; i <= endWordPos; i++) {
                        if (buffer.length() != 0) {
                            buffer.append("\\W");
                        }
                        buffer.append(word.getContent());
                        word = word.next;
                    }
                    searchString = buffer.toString();
                } else {
                    getContext().getLogger().log(Level.WARNING, "Internal Error. Word start position greater than end position.");
                    continue;
                }
            }
            if (searchString == null) {
                getContext().getLogger().log(Level.WARNING, "Internal Error. Search string of concept is null.");
                continue;
            }
            Pattern pattern = Pattern.compile("\\b" + searchString + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                List<Category> categories = categoryMapper.getCategories(concept.getSemanticType());
                for (Category category : categories) {
                    boolean alreadyAdded = false;
                    for (PubTerm addedTerm : addedTerms) {
                        if (addedTerm.getBegin() == matcher.start() && addedTerm.getEnd() == matcher.end() && addedTerm.getCategoryName().equals(category.getName())) {
                            alreadyAdded = true;
                        }
                    }
                    if (!alreadyAdded) {
                        PubTerm term = createTerm(jcas, matcher.start(), matcher.end(), category.getName(), 800, true);
                        FSArray identifiers = new FSArray(jcas, 1);
                        identifiers.set(0, createIdentifier(jcas, PredefinedIdentifierTypes.UMLS_CUI, concept.getEntryID(), null));
                        term.setIdentifiers(identifiers);
                        addedTerms.add(term);
                    }
                }
            }
        }
    }
}
