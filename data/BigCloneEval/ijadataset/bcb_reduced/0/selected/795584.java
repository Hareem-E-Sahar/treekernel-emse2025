package de.knowwe.core.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;

/**
 * Renders all actions for the Renaming Tool.
 * 
 * @author Johannes Dienst
 * 
 */
public class WordBasedRenamingAction extends AbstractAction {

    private ResourceBundle rb;

    private boolean caseSensitive;

    public static final String TXT_SEPERATOR = ":";

    @Override
    public void execute(UserActionContext context) throws IOException {
        context.setContentType("text/html; charset=UTF-8");
        Map<String, String> parameterMap = context.getParameters();
        rb = Messages.getMessageBundle(context);
        String[] sections = null;
        if (parameterMap.containsKey("SelectedSections")) {
            String s = parameterMap.get("SelectedSections");
            Gson gson = new Gson();
            sections = gson.fromJson(s, String[].class);
        }
        String queryString = parameterMap.get(Attributes.TARGET);
        String queryContextPrevious = parameterMap.get(Attributes.CONTEXT_PREVIOUS);
        String queryContextAfter = parameterMap.get(Attributes.CONTEXT_AFTER);
        String atmUrl = parameterMap.get(Attributes.ATM_URL);
        setCaseSensitive(Boolean.parseBoolean(parameterMap.get(Attributes.CASE_SENSITIVE)));
        String queryContext = "";
        queryContextAfter = (!queryContextAfter.equals("")) ? " " + queryContextAfter : "";
        queryContextPrevious = (!queryContextPrevious.equals("")) ? queryContextPrevious + " " : "";
        queryContext = queryContextPrevious + queryString + queryContextAfter;
        String replacement = parameterMap.get(Attributes.FOCUSED_TERM);
        String web = context.getWeb();
        if (web == null) {
            web = Environment.DEFAULT_WEB;
        }
        if (atmUrl != null) {
            context.getWriter().write(getAdditionalMatchText(atmUrl, web, queryString));
            return;
        }
        Map<Article, Collection<WordBasedRenameFinding>> findings = scanForFindings(web, queryContext, queryContextPrevious.length(), sections);
        context.getWriter().write(renderFindingsSelectionMask(findings, queryString, replacement));
    }

    /**
	 * Returns an additional text passage around the search result. So the user
	 * can view the result in a larger context.
	 * 
	 * @param amtURL additional text parameters
	 * @param web
	 * @param query user query string
	 * @return additional text area
	 */
    private String getAdditionalMatchText(String atmURL, String web, String query) {
        String[] params = atmURL.split(WordBasedRenamingAction.TXT_SEPERATOR);
        String articleTitle = params[0];
        String sectionID = params[1];
        int pos = Integer.parseInt(params[2]);
        int curWords = Integer.parseInt(params[3]);
        String direction = params[4];
        String additionalText = "";
        Iterator<Article> iter = Environment.getInstance().getArticleManager(web).getArticleIterator();
        while (iter.hasNext()) {
            Article article = iter.next();
            if (article.getTitle().equals(articleTitle)) {
                Section<?> section = Sections.getSection(sectionID);
                String context = WordBasedRenameFinding.getAdditionalContext(pos, direction, curWords, query.length(), section.getText());
                if (context == null) {
                    context = WordBasedRenameFinding.getAdditionalContext(pos, direction, curWords - 1, query.length(), section.getText());
                    curWords = WordBasedRenameFinding.MAX_WORDS + 1;
                }
                String span = createAdditionalMatchingTextSpan(article, pos, sectionID, curWords + 1, direction.charAt(0), false);
                if (direction.charAt(0) == 'a') {
                    additionalText = context + span;
                } else {
                    additionalText = span + context;
                }
            }
        }
        return additionalText;
    }

    /**
	 * Renders a table with the results of the search in it.
	 * 
	 * @param findings a map with all found text passages in it
	 * @param query the users query string
	 * @param replacement the replacement string for the query string
	 * @return a HTML formatted table witch list all the findings in it
	 */
    private String renderFindingsSelectionMask(Map<Article, Collection<WordBasedRenameFinding>> findings, String query, String replacement) {
        StringBuffer mask = new StringBuffer();
        mask.append("<form method='post' action=''><fieldset><legend>" + rb.getString("KnowWE.renamingtool.searchresult") + " '" + query + "'</legend>");
        mask.append("<table id='sortable1'><colgroup><col class='match' /><col class='section' />");
        mask.append("<col class='replace' /><col class='preview' /></colgroup>");
        mask.append("<thead><tr><th scope='col'>" + rb.getString("KnowWE.renamingtool.clmn.match") + "</th><th scope='col'>" + rb.getString("KnowWE.renamingtool.clmn.section") + "</th>");
        mask.append("<th scope='col'>" + rb.getString("KnowWE.renamingtool.clmn.replace") + "</th><th scope='col'>" + rb.getString("KnowWE.renamingtool.clmn.preview") + "</th></tr></thead>" + "<thead>" + "<tr><td></td><td></td><td>" + "<input class='check-select check' value='' type='button'  title='Select all checkboxes' rel='{section: undefined}'/>" + "<input class='check-deselect check' value='' type='button' title='Deselect all checkboxes' rel='{section: undefined}'/>" + "</td><td></td></tr>" + "</thead>");
        for (Entry<Article, Collection<WordBasedRenameFinding>> entry : findings.entrySet()) {
            Article article = entry.getKey();
            Collection<WordBasedRenameFinding> findingsInArticle = entry.getValue();
            if (findingsInArticle.size() > 0) {
                mask.append("<thead>");
                mask.append("<tr><td>");
                mask.append("<strong>" + rb.getString("KnowWE.renamingtool.article") + ": " + article.getTitle() + "</strong>");
                mask.append("</td><td></td><td>");
                mask.append("<input class='check-select check' value='' type='button'  title='Select all checkboxes' rel='{section: \"" + article.getTitle() + "\"}'/>");
                mask.append("<input class='check-deselect check' value='' type='button' title='Deselect all checkboxes' rel='{section: \"" + article.getTitle() + "\"}'/>");
                mask.append("</thead>");
            }
            mask.append("<tbody>");
            for (WordBasedRenameFinding WordBasedRenameFinding : findingsInArticle) {
                String text = WordBasedRenameFinding.contextText();
                text = highlightQueryResult(text, query);
                String checkBoxID = "replaceBox_" + article.getTitle() + TXT_SEPERATOR + WordBasedRenameFinding.getSec().getID() + TXT_SEPERATOR + WordBasedRenameFinding.getStart();
                mask.append("<tr>");
                mask.append("<td>" + createAdditionalMatchingTextSpan(article, WordBasedRenameFinding.getStart(), WordBasedRenameFinding.getSec().getID(), 0, 'p', true));
                mask.append(" " + text + " ");
                mask.append(createAdditionalMatchingTextSpan(article, WordBasedRenameFinding.getStart(), WordBasedRenameFinding.getSec().getID(), 0, 'a', true));
                mask.append("</td>");
                mask.append("<td><i>" + WordBasedRenameFinding.getSec().get().getName() + "</i></td>");
                mask.append("<td><input type='checkbox' id='" + checkBoxID + "'></td>");
                mask.append("<td>" + replacePreview(text, query, replacement) + "</td>");
                mask.append("</tr>");
            }
            mask.append("</tbody>");
        }
        mask.append("<tfoot>");
        mask.append("<tr><td></td><td></td>");
        mask.append("<td><input id='renaming-replace' value='" + rb.getString("KnowWE.renamingtool.bttn.replace") + "' type='button' class='button'" + " title='Begriff in ausgewählten Stellen ersetzen'/></td>");
        mask.append("<td></td></tr>");
        mask.append("</tfoot>");
        mask.append("</table></fieldset></form>");
        return mask.toString();
    }

    /**
	 * Scans all articles for the query expressions. If the expressions is found
	 * a <code>RenameFinding</code> object is created.
	 * 
	 * @param web
	 * @param query any string the user is looking for
	 * @param previousMatchLength
	 * @param sections just add findings to the map which have a corresponding
	 *        section type - if null, all findings are added
	 * @return a map containing all findings of the string <code>query<code>
	 */
    public Map<Article, Collection<WordBasedRenameFinding>> scanForFindings(String web, String query, int previousMatchLength, String[] sections) {
        Set<String> sectionSet = null;
        if (sections != null) {
            sectionSet = new HashSet<String>(Arrays.asList(sections));
        }
        Map<Article, Collection<WordBasedRenameFinding>> map = new HashMap<Article, Collection<WordBasedRenameFinding>>();
        Iterator<Article> iter = Environment.getInstance().getArticleManager(web).getArticleIterator();
        Pattern p;
        if (getCaseSensitive()) {
            p = Pattern.compile(Pattern.quote(query));
        } else {
            p = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        }
        while (iter.hasNext()) {
            Article article = iter.next();
            map.put(article, new HashSet<WordBasedRenameFinding>());
            String text = article.getRootSection().getText();
            Matcher m = p.matcher(text);
            while (m.find()) {
                int start = m.start() + previousMatchLength;
                int end = start + query.length();
                Section<?> sec = Sections.findSmallestNodeContaining(article.getRootSection(), start, end);
                int startInSec = start - sec.getAbsolutePositionStartInArticle();
                WordBasedRenameFinding f = new WordBasedRenameFinding(startInSec, startInSec + query.length(), WordBasedRenameFinding.getContext(startInSec, sec, text, query.length()), sec);
                if (sections == null || sectionSet.contains(sec.get().getName())) {
                    map.get(article).add(f);
                }
            }
        }
        return map;
    }

    /**
	 * Creates the buttons to navigate through the context.
	 * 
	 * @param article the article containing section
	 * @param section where the query was found
	 * @param sectionIndex position of the section in the article
	 * @param curWords currently amount of words displayed
	 * @param direction [p]revious [a]fter
	 * @param span
	 * @return
	 */
    private String createAdditionalMatchingTextSpan(Article article, int sectionIndex, String sectionId, int curWords, char direction, boolean span) {
        StringBuilder html = new StringBuilder();
        String arrowLeft = "KnowWEExtension/images/arrow_left.png";
        String arrowRight = "KnowWEExtension/images/arrow_right.png";
        String img;
        switch(direction) {
            case 'a':
                img = arrowRight;
                if (curWords > WordBasedRenameFinding.MAX_WORDS) {
                    img = arrowLeft;
                    curWords = 0;
                }
                break;
            default:
                img = arrowLeft;
                if (curWords > WordBasedRenameFinding.MAX_WORDS) {
                    img = arrowRight;
                    curWords = 0;
                }
                break;
        }
        String atmUrl = "{article: '" + article.getTitle() + "'," + "section: '" + sectionId + "'," + "index: " + sectionIndex + ", " + "words: " + curWords + ", " + "direction: '" + direction + "'}";
        if (span) {
            html.append("<span id='" + direction + sectionIndex + "' class='short' style='display: inline;'>");
        }
        html.append("<img width=\"12\" height=\"12\" border=\"0\" src=\"" + img + "\" alt=\"more\" rel=\"" + atmUrl + "\" class=\"show-additional-text-renaming\"/>");
        if (span) {
            html.append("</span>");
        }
        return html.toString();
    }

    /**
	 * Searches the query string and highlights it. Works case-sensitive.
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
    private String highlightQueryResult(String text, String query) {
        StringTokenizer tokenizer = new StringTokenizer(text, "; .,\n\r[](){}?!/|:'<>", true);
        StringBuilder result = new StringBuilder();
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (token.toLowerCase().contains(query.toLowerCase())) {
                Pattern p;
                if (getCaseSensitive()) {
                    p = Pattern.compile(query);
                } else {
                    p = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
                }
                Matcher m = p.matcher(token);
                while (m.find()) {
                    result.append(token.substring(0, m.start()) + "<strong>" + token.substring(m.start(), m.end()) + "</strong>" + token.substring(m.end(), token.length()));
                }
            } else {
                result.append(token);
            }
        }
        return result.toString();
    }

    /**
	 * Replaces the query string with the given replacement. Works
	 * case-sensitive.
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
    private String replacePreview(String text, String query, String replacement) {
        StringTokenizer tokenizer = new StringTokenizer(text, "; .,\n\r[](){}?!/|:'<>", true);
        StringBuilder result = new StringBuilder();
        if (replacement == null) replacement = "";
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (query.equalsIgnoreCase(token)) {
                result.append(replacement);
            } else {
                result.append(token);
            }
        }
        return result.toString();
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    private String verbalizeModul(String type) {
        int i = type.lastIndexOf(".");
        String modName = type;
        if (i >= 0) {
            modName = type.substring(i + 1);
        }
        String name = null;
        try {
            name = rb.getString("KnowWE.sectionfinder." + modName);
        } catch (Exception e) {
        }
        if (name != null) return name;
        return type;
    }
}
