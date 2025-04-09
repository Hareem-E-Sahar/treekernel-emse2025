package uk.org.biotext.graphspider.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * The Class PatternRepository.
 */
public class PatternRepository {

    /** The _factory. */
    private final PatternElementFactory _factory;

    /** The _patterns. */
    private final Map<String, Pattern> _patterns;

    /**
     * Instantiates a new pattern repository.
     * 
     * @param factory
     *            the factory
     */
    public PatternRepository(PatternElementFactory factory) {
        if (factory == null) throw new IllegalArgumentException("Pattern element factory cannot be null");
        _factory = factory;
        _patterns = new HashMap<String, Pattern>();
    }

    /**
     * Adds the.
     * 
     * @param patternText
     *            the pattern text
     * 
     * @throws PatternSyntaxException
     *             the pattern syntax exception
     * @throws PatternSemanticsException
     *             the pattern semantics exception
     * @throws UndefinedVariableException
     *             the undefined variable exception
     */
    public void add(String patternText) throws PatternSyntaxException, PatternSemanticsException, UndefinedVariableException {
        if (patternText == null) throw new IllegalArgumentException("Cannot create pattern from null string");
        if (patternText == "") throw new IllegalArgumentException("Cannot create pattern from empty string");
        String canonical = canonicalize(patternText);
        _patterns.put(canonical, new Pattern(canonical, _factory));
    }

    /**
     * Canonicalize.
     * 
     * @param patternText
     *            the pattern text
     * 
     * @return the string
     */
    private String canonicalize(String patternText) {
        if (patternText == null) throw new IllegalArgumentException("Cannot canonicalize null string");
        if (patternText == "") throw new IllegalArgumentException("Cannot canonicalize empty string");
        return patternText.replaceAll("\\s+", " ").trim();
    }

    /**
     * Expand all.
     * 
     * @param rules
     *            the rules
     * 
     * @throws PatternSyntaxException
     *             the pattern syntax exception
     * @throws PatternSemanticsException
     *             the pattern semantics exception
     * @throws UndefinedVariableException
     *             the undefined variable exception
     */
    public void expandAll(ReplacementList rules) throws PatternSyntaxException, PatternSemanticsException, UndefinedVariableException {
        Collection<String> newPatterns = new HashSet<String>();
        for (String patternText : _patterns.keySet()) {
            newPatterns.addAll(getVariants(patternText, rules, 0));
        }
        for (String patternText : newPatterns) {
            String canonical = canonicalize(patternText);
            if (!_patterns.containsKey(canonical)) {
                _patterns.put(canonical, new Pattern(canonical, _factory));
            }
        }
    }

    /**
     * Gets the variants.
     * 
     * @param patternText
     *            the pattern text
     * @param rules
     *            the rules
     * @param firstRule
     *            the first rule
     * 
     * @return the variants
     */
    private Collection<String> getVariants(String patternText, ReplacementList rules, int firstRule) {
        if (patternText == null) throw new IllegalArgumentException("Pattern text cannot be null string");
        if (patternText.equals("")) throw new IllegalArgumentException("Pattern text cannot be empty string");
        if (rules == null) throw new IllegalArgumentException("Replacement list cannot be null string");
        if (firstRule < 0 || firstRule > rules.size() - 1) throw new IllegalArgumentException("There are " + rules.size() + " rules in replacement list, but you have requested we start with rule " + firstRule);
        Collection<String> allVariants = new HashSet<String>();
        for (int i = firstRule; i < rules.size(); i++) {
            Collection<String> variants = new HashSet<String>();
            variants.addAll(getVariants(patternText, rules.getSearchString(i), rules.getReplaceString(i)));
            if (i < rules.size() - 1) {
                for (String newPatternText : variants) {
                    allVariants.addAll(getVariants(newPatternText, rules, i + 1));
                }
            }
            allVariants.addAll(variants);
        }
        return allVariants;
    }

    /**
     * Gets the variants.
     * 
     * @param oldPatternText
     *            the old pattern text
     * @param searchText
     *            the search text
     * @param replaceText
     *            the replace text
     * 
     * @return the variants
     */
    private Collection<String> getVariants(String oldPatternText, String searchText, String replaceText) {
        Collection<String> variants = new HashSet<String>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(searchText, java.util.regex.Pattern.LITERAL);
        Matcher m = p.matcher(oldPatternText);
        List<MatchResult> results = new ArrayList<MatchResult>();
        while (m.find()) {
            results.add(m.toMatchResult());
        }
        int n = results.size();
        for (int patNum = 1; patNum < twoToThe(n); patNum++) {
            String newPatternText = new String(oldPatternText);
            int offset = 0;
            for (int matchNum = 0; matchNum < n; matchNum++) {
                if (isBitSet(patNum, matchNum)) {
                    MatchResult hit = results.get(matchNum);
                    int realStart = hit.start() + offset;
                    int realEnd = hit.end() + offset;
                    Matcher replacer = p.matcher(newPatternText);
                    newPatternText = replaceFirst(replacer, realStart, realEnd, replaceText);
                    int hitLength = hit.end() - hit.start();
                    int growth = replaceText.length() - hitLength;
                    offset += growth;
                    variants.add(newPatternText);
                }
            }
        }
        return variants;
    }

    /**
     * Two to the.
     * 
     * @param power
     *            the power
     * 
     * @return the int
     */
    private int twoToThe(int power) {
        return (int) Math.pow(2, power);
    }

    /**
     * Checks if is bit set.
     * 
     * @param field
     *            the field
     * @param pos
     *            the pos
     * 
     * @return true, if is bit set
     */
    private boolean isBitSet(int field, int pos) {
        int fieldMask = twoToThe(pos);
        int fieldMasked = field & fieldMask;
        return (fieldMasked > 0);
    }

    /**
     * Replace first.
     * 
     * @param m
     *            the m
     * @param startBound
     *            the start bound
     * @param endBound
     *            the end bound
     * @param replacement
     *            the replacement
     * 
     * @return the string
     */
    private String replaceFirst(Matcher m, int startBound, int endBound, String replacement) {
        StringBuffer sb = new StringBuffer();
        m.region(startBound, endBound);
        if (m.find()) m.appendReplacement(sb, replacement);
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Summary.
     * 
     * @return the string
     */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(_patterns.size());
        sb.append(" patterns in pattern repository.\n");
        List<String> keys = new ArrayList<String>(_patterns.keySet());
        Collections.sort(keys);
        for (String patternText : keys) {
            sb.append("\nCompiled pattern: ");
            sb.append(_patterns.get(patternText));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * All.
     * 
     * @return the map< string, pattern>
     */
    public Map<String, Pattern> all() {
        return Collections.unmodifiableMap(_patterns);
    }
}
