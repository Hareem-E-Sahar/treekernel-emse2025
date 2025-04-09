package org.expasy.jpl.core.mol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.expasy.jpl.commons.base.io.Parser;

/**
 * This object provides formats for atoms, molecules, monomers and polymers
 * defined in tokens.conf and patterns.conf files.
 * 
 * <p>
 * Ideally would have to be accessible from friends only !
 * </p>
 * 
 * @author nikitin
 * 
 * @version 1.0
 * 
 */
public final class MolecularFormatRegister {

    /** a token for token */
    private static String TOKEN_KEY = "[A-Z_]+_TOKEN";

    /** a token for pattern */
    private static String PATTERN_KEY = "[A-Z_]+_PATTERN";

    /** the assignment operator in config files */
    private static String DELIMITOR = "\\s*:=\\s*";

    /** the operator for concatenation in config files */
    private static String PLUS = "\"\\s*\\+\\s*\"";

    /** the tokens defined for atom and molecule format */
    private static Map<String, String> tokens;

    /** the patterns used for matching formula format */
    private static Map<String, String> patterns;

    /** load tokens and patterns */
    static {
        tokens = new HashMap<String, String>();
        patterns = new HashMap<String, String>();
        ChemicalLanguageParser parser = new ChemicalLanguageParser();
        try {
            parser.parse("tokens.conf");
            parser.parse("patterns.conf");
        } catch (ParseException e) {
            System.err.println("parse exception: " + e.getMessage());
        }
    }

    private MolecularFormatRegister() {
        throw new AssertionError("non instanciable");
    }

    /**
	 * Get a token.
	 * 
	 * @param name the token name.
	 * @return a token.
	 */
    public static String lookupToken(String name) {
        if (!hasToken(name)) {
            throw new IllegalArgumentException("token " + name + " was not found");
        }
        return tokens.get(name);
    }

    /**
	 * Return true if token exists.
	 * 
	 * @param name the token name.
	 * @return true if exists.
	 */
    public static boolean hasToken(String name) {
        return tokens.containsKey(name);
    }

    /**
	 * Get a pattern.
	 * 
	 * @param name the pattern name.
	 * @return a pattern.
	 */
    public static String lookupPattern(String name) {
        if (!hasPattern(name)) {
            throw new IllegalArgumentException("pattern " + name + " was not found");
        }
        return patterns.get(name);
    }

    /**
	 * Return true if pattern exists.
	 * 
	 * @param name the pattern name.
	 * @return true if exists.
	 */
    public static boolean hasPattern(String name) {
        return patterns.containsKey(name);
    }

    /**
	 * @return the set of tokens.
	 */
    public static Set<String> getTokens() {
        return tokens.keySet();
    }

    /**
	 * @return the set of patterns.
	 */
    public static Set<String> getPatterns() {
        return patterns.keySet();
    }

    /**
	 * This parser parses config files and store tokens and patterns.
	 */
    private static class ChemicalLanguageParser implements Parser<String> {

        public void parse(String filename) throws ParseException {
            InputStream in = getClass().getResourceAsStream(filename);
            InputStreamReader isr = new InputStreamReader(in);
            parse(new BufferedReader(isr));
        }

        public void parse(BufferedReader br) throws ParseException {
            try {
                String line = br.readLine();
                while (line != null) {
                    if (!line.matches("^//.+$") && !line.matches("^\\s*$")) {
                        String[] fields = line.split(DELIMITOR);
                        if (fields.length == 2) {
                            if (fields[0].matches("^" + TOKEN_KEY + "$")) {
                                addToken(fields[0], fields[1].substring(1, fields[1].length() - 1));
                            } else if (fields[0].matches("^" + PATTERN_KEY + "$")) {
                                addPattern(fields[0], processValue(fields[1]));
                            }
                        } else {
                            System.err.println("too many fields in " + line);
                        }
                    }
                    line = br.readLine();
                }
            } catch (IOException e) {
                throw new ParseException(e.getMessage(), -1);
            }
        }

        private final void addToken(String key, String value) {
            if (tokens.containsKey(key)) {
                throw new IllegalArgumentException("token " + key + " already exists");
            }
            tokens.put(key, value);
        }

        private final void addPattern(String key, String value) {
            if (patterns.containsKey(key)) {
                throw new IllegalArgumentException("pattern " + key + " already exists");
            }
            patterns.put(key, value);
        }

        /**
		 * Make substitutions of patterns/tokens variable.
		 * 
		 * @param pre string to process.
		 * @return substituted string.
		 */
        private String processValue(String pre) {
            StringBuilder post = new StringBuilder();
            Pattern pat = Pattern.compile("(" + PATTERN_KEY + "|" + TOKEN_KEY + ")");
            Matcher matcher = pat.matcher(pre);
            int from = 0;
            while (matcher.find()) {
                post.append(pre.substring(from, matcher.start()));
                String s = matcher.group(1);
                if (s.matches(PATTERN_KEY)) {
                    post.append("\"" + lookupPattern(s) + "\"");
                } else {
                    post.append("\"" + lookupToken(s) + "\"");
                }
                from = matcher.end();
            }
            post.append(pre.substring(from));
            return catAndRemoveQuotes(post.toString());
        }
    }

    /**
	 * Concatenate string operands and remove enclosed quotes.
	 * 
	 * @param str the string to process.
	 * @return concatenated operands.
	 */
    private static final String catAndRemoveQuotes(String str) {
        String[] strs = str.split(PLUS);
        if (strs.length > 1) {
            StringBuilder post = new StringBuilder();
            String s = strs[0].substring(1);
            post.append(s);
            for (int i = 1; i < strs.length - 1; i++) {
                post.append(strs[i]);
            }
            post.append(strs[strs.length - 1].substring(0, strs[strs.length - 1].length() - 1));
            return post.toString();
        }
        return str.substring(1, str.length() - 1);
    }
}
