package cz.cuni.mff.ufal.volk.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import cz.cuni.mff.ufal.volk.InitializationException;
import alice.tuprolog.Int;
import alice.tuprolog.InvalidLibraryException;
import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.UnknownVarException;
import alice.tuprolog.Var;

public class Pattern {

    private Prolog prolog = new Prolog();

    /**
	 * Creates new compiled pattern. The constructor uses the empty language model.
	 */
    public Pattern(String pattern) {
        this(LanguageDescription.EMPTY, pattern);
    }

    /**
	 * Creates new compiled pattern.
	 *
	 * @param pattern
	 *          the pattern to use; the syntax is described TODO
	 * @throws InvalidTheoryException
	 */
    public Pattern(LanguageDescription languageDescription, String pattern) {
        languageDescription.getClass();
        pattern.getClass();
        try {
            prolog.loadLibrary(new PatternLibrary());
        } catch (InvalidLibraryException e) {
            throw new InitializationException("An InvalidLibraryException occurred while trying to initialize pattern", e);
        }
        this.languageDescription = languageDescription;
        String[] ps = pattern.split(SPLIT_REGEX, -1);
        boolean ct = false;
        java.util.regex.Pattern varpat = java.util.regex.Pattern.compile(VAR_REGEX);
        for (String p : ps) {
            if (ct) {
                try {
                    Theory theory = new Theory(p + NEWLINE);
                    patternTheories.add(theory);
                } catch (InvalidTheoryException e) {
                    throw new InitializationException("An InvalidTheoryException occurred when parsing the pattern, see cause for details", e);
                }
            } else {
                Matcher m = varpat.matcher(p);
                int first = 0;
                while (m.find()) {
                    String raw = p.substring(first, m.start());
                    parts.add(processRawString(raw));
                    parts.add(new Variable(this, p.substring(m.start() + 1, m.end() - 1)));
                    first = m.end();
                }
                if (first < p.length()) {
                    String raw = p.substring(first);
                    parts.add(processRawString(raw));
                }
            }
            ct = !ct;
        }
        if (patternTheories.size() == 0) try {
            patternTheories.add(new Theory("cond([]). "));
        } catch (InvalidTheoryException e) {
            throw new InitializationException("An InvalidTheoryException occurred", e);
        }
    }

    private String processRawString(String raw) {
        StringBuilder sb = new StringBuilder();
        int beginIndex = 0;
        boolean rawMode = true;
        for (int i = 0; i < raw.length(); i++) {
            if (rawMode) {
                if (raw.charAt(i) == '\\') {
                    if (beginIndex < i) sb.append(raw.substring(beginIndex, i));
                    rawMode = false;
                }
            } else {
                sb.append(raw.charAt(i));
                beginIndex = i + 1;
                rawMode = true;
            }
        }
        if (beginIndex < raw.length()) sb.append(raw.substring(beginIndex));
        return sb.toString();
    }

    public String evaluate(String... vars) {
        try {
            languageDescription.getLanguageStream().reset();
            Theory theory = new Theory(languageDescription.getLanguageStream());
            prolog.setTheory(theory);
            for (Theory th : patternTheories) prolog.addTheory(th);
            StringBuilder literals = new StringBuilder();
            StringBuilder indices = new StringBuilder();
            for (int i = 0; i < vars.length; i += 2) {
                String varName = vars[i];
                String termName = vars[i + 1];
                boolean isIndex = termName.startsWith("@") && !termName.startsWith("@@");
                if (isIndex) {
                    if (i > 0) indices.append(",");
                    indices.append(String.format("(%s := %s)", varName, termName.substring(1)));
                } else {
                    if (i > 0) literals.append(",");
                    if (termName.startsWith("@@")) termName = termName.substring(1);
                    if (termName.length() > 0 && termName.matches("[\\d]*(\\.[\\d]*)?")) literals.append(String.format("(%s := %s)", varName, termName)); else literals.append(String.format("(%s := '%s')", varName, termName.replace("\\", "\\\\").replace("'", "\\'")));
                }
            }
            Theory th = new Theory(String.format("nlpat_concretize([%s], [%s]). ", literals.toString(), indices.toString()));
            prolog.addTheory(th);
            SolveInfo solve = prolog.solve("nlpat_solve(Solution).");
            if (solve.isSuccess()) {
                variables = new HashMap<String, String>();
                Term solution = solve.getTerm("Solution");
                if (solution.isList()) {
                    Struct list = (Struct) solution;
                    while (!list.isEmptyList()) {
                        Struct head = (Struct) list.listHead();
                        String varName = ((Struct) head.getArg(0).getTerm()).getName();
                        Term varTerm = head.getArg(1).getTerm();
                        String varValue;
                        if (varTerm instanceof Int) {
                            varValue = Integer.toString(((Int) varTerm).intValue());
                        } else if (varTerm instanceof Var) {
                            varValue = "?Var";
                        } else if (varTerm instanceof Struct) {
                            varValue = ((Struct) varTerm).getName();
                        } else {
                            varValue = "?Cast";
                        }
                        list = list.listTail();
                        variables.put(varName, varValue);
                    }
                } else {
                    variables = null;
                }
            } else {
                variables = null;
            }
            StringBuilder sb = new StringBuilder();
            for (Object part : parts) sb.append(part.toString());
            return sb.toString();
        } catch (IOException e) {
            throw new PatternEvaluationException("Could not evaluate the pattern", e);
        } catch (InvalidTheoryException e) {
            throw new PatternEvaluationException("An InvalidTheoryException occurred", e);
        } catch (MalformedGoalException e) {
            throw new PatternEvaluationException("A MalformedGoalException occurred", e);
        } catch (NoSolutionException e) {
            throw new PatternEvaluationException("A NoSolutionException occurred", e);
        } catch (UnknownVarException e) {
            throw new PatternEvaluationException("An UnknownVarException occurred", e);
        } finally {
            prolog.clearTheory();
        }
    }

    private Map<String, String> variables;

    private static final String SPLIT_REGEX = "(^#)|(?<=([^\\\\](\\\\\\\\){0,100000}))#";

    private static final String VAR_REGEX = "((^=)|((?<=([^\\\\](\\\\\\\\){0,100000}))=))[A-Za-z_][A-Za-z_\\d]*=";

    public static final String NEWLINE = System.getProperty("line.separator");

    private LanguageDescription languageDescription;

    private List<Object> parts = new ArrayList<Object>();

    private List<Theory> patternTheories = new ArrayList<Theory>();

    private String evaluateVariable(String name) {
        if (variables != null) {
            String res = variables.get(name);
            return res == null ? "?Null" : res;
        } else return "?null";
    }

    private static class Variable {

        Variable(Pattern parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        Pattern parent;

        String name;

        @Override
        public String toString() {
            return parent.evaluateVariable(name);
        }
    }
}
