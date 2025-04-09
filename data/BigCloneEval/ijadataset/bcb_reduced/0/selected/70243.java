package org.pubcurator.analyzers.string.annotators;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.pubcurator.core.utils.StringUtil;
import org.pubcurator.uima.annotator.AnalyzerAnnotator;

/**
 * @author Kai Schlamp (schlamp@gmx.de)
 *
 */
public class StringAnnotator extends AnalyzerAnnotator {

    public static final String SEARCH_PARAMETER = "search";

    public static final String REGULAR_EXPRESSION_PARAMETER = "regularExpression";

    public static final String CASE_SENSITIVE_PARAMETER = "caseSensitive";

    public static final String CATEGORY_NAME_PARAMETER = "categoryName";

    private String search;

    private boolean regularExpression = false;

    private boolean caseSensitive = false;

    private String categoryName;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        search = (String) getContext().getConfigParameterValue(SEARCH_PARAMETER);
        if (search == null) {
            search = "";
        }
        regularExpression = (Boolean) getContext().getConfigParameterValue(REGULAR_EXPRESSION_PARAMETER);
        caseSensitive = (Boolean) getContext().getConfigParameterValue(CASE_SENSITIVE_PARAMETER);
        categoryName = (String) getContext().getConfigParameterValue(CATEGORY_NAME_PARAMETER);
        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = "unknown";
        }
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        String text = jcas.getDocumentText();
        if (!regularExpression) {
            List<String> searchTerms = StringUtil.splitString(search, ' ', '"');
            for (String searchTerm : searchTerms) {
                int i = 0;
                while (true) {
                    int index = -1;
                    if (caseSensitive) {
                        index = text.indexOf(searchTerm, i);
                    } else {
                        index = text.toLowerCase().indexOf(searchTerm.toLowerCase(), i);
                    }
                    if (index == -1) {
                        break;
                    }
                    createTerm(jcas, index, index + searchTerm.length(), categoryName, 1000, true);
                    i = index + 1;
                }
            }
        } else {
            try {
                Pattern pattern = Pattern.compile(search);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    createTerm(jcas, start, end, categoryName, 1000, true);
                }
            } catch (Exception e) {
                getContext().getLogger().log(Level.INFO, "Invalid regular expression: " + search);
            }
        }
    }
}
