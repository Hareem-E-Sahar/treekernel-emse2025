package org.algoristes.alkwarel.script;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.algoristes.alkwarel.utils.Log;

/**
 * Main script parser,
 * 
 * @author Xavier Gouchet
 * 
 */
public class ScriptParser {

    private static final String PREFIX_STRING = "__string_key__";

    private static final String PREFIX_MACRO = "__macro_block_key__";

    private static final String PREFIX_MATH = "__math_block_key__";

    private static final String PREFIX_EVAL = "__eval_block_key__";

    /** Store for the String definitions **/
    protected static ScriptVariableStore StringsStore;

    /** Store for the Macros defnitions **/
    protected static ScriptVariableStore MacrosStore;

    /** Store for the Math Expressions **/
    protected static ScriptVariableStore MathBlocksStore;

    /** Store for the Evaluation blocks **/
    protected static ScriptVariableStore EvalBlocksStore;

    /** Store for the User variables **/
    protected static ScriptVariableStore VariablesStore;

    /** Table matching string keys to Functions Definitions. **/
    protected static ScriptVariableStore FunctionsStore;

    private List<String> mCommands;

    private int mDepth;

    /**
	 * Default constructor
	 * 
	 * @param depth
	 *            the depth at which the given script is run
	 */
    public ScriptParser(int depth) {
        mCommands = new ArrayList<String>();
        mDepth = depth;
    }

    /**
	 * Parses the given script using a TCL like syntax then evaluate it.
	 * 
	 * @param script
	 *            the script to parse
	 * @return the result of the evaluation
	 */
    public String runScript(String script) {
        parse(script);
        String result = evaluate();
        clearVariables();
        if (mDepth == 0) Log.verbose("Result : " + result);
        return result;
    }

    /**
	 * Parses a script, replace blocks and split it into commands
	 * 
	 * @param script
	 *            the script to parse
	 */
    public void parse(String script) {
        script = script.trim();
        try {
            script = lookupMacroBlocks(script);
            script = lookupEvalBlocks(script);
            script = lookupMathBlocks(script);
            script = lookupStrings(script);
            splitCommands(script);
        } catch (RuntimeException e) {
            Log.severe(e.getMessage());
        }
    }

    /**
	 * Evaluate the commands from a previous parse
	 * 
	 * @return the result of the last command
	 */
    public String evaluate() {
        String result = "";
        try {
            for (String s : mCommands) {
                result = readCommand(s);
            }
        } catch (RuntimeException e) {
            Log.severe(e.getMessage());
            return "0";
        }
        return result;
    }

    /**
	 * Reads a command and evaluates it
	 * 
	 * @param cmd
	 *            the command to evaluate
	 * @return the result of the command
	 */
    public String readCommand(String cmd) {
        String cmdName = "";
        String[] words = null;
        if (cmd.indexOf(' ') == -1) cmdName = cmd; else {
            cmdName = cmd.substring(0, cmd.indexOf(' '));
            cmd = cmd.substring(cmd.indexOf(' ') + 1);
            words = cmd.split(" ");
            for (int i = 0; i < words.length; i++) {
                words[i] = getSubstitute(words[i]);
            }
        }
        return evalCommand(cmdName, words);
    }

    /**
	 * Evaluate the command by looking for a matching function name, either in
	 * the builtin system (set, puts, for, ...), either in the populated known
	 * list
	 * 
	 * @param cmdName
	 *            the command name
	 * @param words
	 *            the list of words in the input order
	 * @return the result of calling the command, or "-1" if the command doesn't
	 *         exist
	 */
    public String evalCommand(String cmdName, String[] words) {
        if (ScriptCommands.isBuiltinCommand(cmdName)) return ScriptCommands.evalBuiltinCommand(cmdName, words, mDepth);
        if (getFunctionsStore().contains(cmdName)) {
            ScriptFunction func = (ScriptFunction) getFunctionsStore().getVariable(cmdName, mDepth);
            return func.evalFunction(words, mDepth);
        }
        return "Unknown command " + cmdName;
    }

    /**
	 * Checks if the given word is a variable user defined or declared through a
	 * Macro, String, Math or Eval block, and replaces it whith its value. Else
	 * substitute escaped characters.
	 * 
	 * @param word
	 *            the word to check
	 * @return the substitute value
	 */
    public String getSubstitute(String word) {
        if ((word == null) || (word.length() == 0)) return "";
        if (word.charAt(0) != '$') return word;
        word = word.substring(1);
        if (word.startsWith(PREFIX_STRING)) return getStringsStore().getValue(word, mDepth);
        if (word.startsWith(PREFIX_MACRO)) return getMacroBlocksStore().getValue(word, mDepth);
        if (word.startsWith(PREFIX_MATH)) {
            String math = getMathBlocksStore().getValue(word, mDepth);
            String valid = getMathSubstitute(math);
            MathParser mathParser = new MathParser();
            return mathParser.compute(valid);
        }
        if (word.startsWith(PREFIX_EVAL)) {
            String evalScript = getEvalBlocksStore().getValue(word, mDepth);
            ScriptParser subParser = new ScriptParser(mDepth + 1);
            return subParser.runScript(evalScript);
        }
        return getVariablesStore().getValue(word, mDepth);
    }

    /**
	 * Returns a valid non null String corresponding to the variable name.
	 * 
	 * @param table
	 * @param key
	 * @return the variable substitution
	 */
    public String getValidSubstitute(Hashtable<String, ScriptVariable> table, String key) {
        ScriptVariable var = table.get(key);
        if (var == null) throw new RuntimeException("Unknown variable " + key);
        if (var.getDepth() > mDepth) throw new RuntimeException("Variable used outside its scope" + key);
        return var.mValue;
    }

    /**
	 * Substitute all variables within a math expression. Only user defined
	 * variables are allowed in a math expression (no macro, string or eval
	 * blocks).
	 * 
	 * @param expr
	 *            the expression in which to substitute variables
	 * @return the resulting expression
	 */
    public String getMathSubstitute(String expr) {
        String result = "";
        String variable;
        int start, end = 0;
        Pattern pattern = Pattern.compile("\\$[a-zA-Z_0-9]+");
        Matcher matcher = pattern.matcher(expr);
        while (matcher.find()) {
            start = matcher.start();
            result += expr.substring(end, start);
            end = matcher.end();
            variable = expr.substring(start + 1, end);
            result += "(" + getVariablesStore().getValue(variable, mDepth) + ")";
        }
        result += expr.substring(end);
        return result;
    }

    /**
	 * Splits a script into command. Commands are divided by line breaks and
	 * semicolons. if a command starts by #, it's ignored
	 * 
	 * @param input
	 */
    public void splitCommands(String input) {
        int newLine, semiColon, start, stop;
        String cmd;
        start = 0;
        do {
            newLine = input.indexOf('\n', start);
            semiColon = input.indexOf(';', start);
            if (semiColon == -1) if (newLine == -1) stop = input.length(); else stop = newLine; else if ((newLine != -1) && (newLine < semiColon)) stop = newLine; else stop = semiColon;
            if (start + 1 < stop) {
                cmd = input.substring(start, stop).trim();
                if (cmd.charAt(0) != '#') mCommands.add(cmd); else {
                    if (newLine == -1) stop = input.length(); else stop = newLine;
                }
            }
            start = stop + 1;
        } while (start < input.length());
    }

    /**
	 * Lookup for static blocks in a script (static block are delimited by curly
	 * braces). If static blocks are found they are replaced by variables used
	 * in the static blocks table.
	 * 
	 * @param input
	 *            the input script to analyse
	 * @return the resulting string
	 */
    public String lookupMacroBlocks(String input) {
        int curlyStart, curlyEnd;
        curlyStart = input.indexOf('{');
        curlyEnd = Utils.getMatchingDelimiter(input, curlyStart, '{', '}');
        if (curlyStart != -1) {
            if (curlyEnd != -1) {
                return extractMacroBlock(input, curlyStart, curlyEnd);
            } else {
                throw new RuntimeException("Missing closing curly brace; " + "opening brace found at <" + Utils.getNeighbourHood(input, curlyStart, 14) + ">");
            }
        }
        return input;
    }

    /**
	 * Lookup for static blocks in a script (static block are delimited by curly
	 * braces). If static blocks are found they are replaced by variables used
	 * in the static blocks table.
	 * 
	 * @param input
	 *            the input script to analyse
	 * @return the resulting string
	 */
    public String lookupEvalBlocks(String input) {
        int squareStart, squareEnd;
        squareStart = input.indexOf('[');
        squareEnd = Utils.getMatchingDelimiter(input, squareStart, '[', ']');
        if (squareStart != -1) {
            if (squareEnd != -1) {
                return extractEvalBlock(input, squareStart, squareEnd);
            } else {
                throw new RuntimeException("Missing closing square bracket; " + "opening bracket found at <" + Utils.getNeighbourHood(input, squareStart, 14) + ">");
            }
        }
        return input;
    }

    /**
	 * Lookup for math blocks in a script (math block are delimited by
	 * parenthesis). If math blocks are found they are replaced by variables
	 * used in the math blocks table.
	 * 
	 * @param input
	 *            the input script to analyse
	 * @return the resulting string
	 */
    public String lookupMathBlocks(String input) {
        int parenthesisStart, parenthesisEnd;
        parenthesisStart = input.indexOf('(');
        parenthesisEnd = Utils.getMatchingDelimiter(input, parenthesisStart, '(', ')');
        if (parenthesisStart != -1) {
            if (parenthesisEnd != -1) {
                return extractMathBlock(input, parenthesisStart, parenthesisEnd);
            } else {
                throw new RuntimeException("Missing closing parenthesis; " + "opening parenthesis found at <" + Utils.getNeighbourHood(input, parenthesisStart, 14) + ">");
            }
        }
        return input;
    }

    /**
	 * Lookup for strings in a script (scripts are delimited by double quotes).
	 * If strings are found they are replaced by variables used in the String
	 * table.
	 * 
	 * @param input
	 *            the input script to analyse
	 * @return the resulting string
	 */
    public String lookupStrings(String input) {
        int dblQtStart, dblQtEnd;
        dblQtStart = Utils.getIndexOfUnescapedDoubleQuote(input, 0);
        dblQtEnd = Utils.getIndexOfUnescapedDoubleQuote(input, dblQtStart + 1);
        if (dblQtStart != -1) {
            if (dblQtEnd != -1) {
                return extractString(input, dblQtStart, dblQtEnd);
            } else {
                throw new RuntimeException("Missing ending double quote; " + "starting quote found at <" + Utils.getNeighbourHood(input, dblQtStart, 14) + ">");
            }
        }
        return input;
    }

    /**
	 * Extracts a string between start and end index and store it into the
	 * static blocks table. Replaces the extracted string by a variable.
	 * 
	 * @param input
	 *            the input text to modify
	 * @param start
	 *            the index at which the string to extract starts
	 * @param end
	 *            the index at which the string to extract ends
	 * @return the resulting string
	 */
    public String extractMacroBlock(String input, int start, int end) {
        String head, string, tail, key;
        head = input.substring(0, start);
        string = input.substring(start + 1, end);
        key = PREFIX_MACRO + mDepth + "_" + getMacroBlocksStore().size();
        getMacroBlocksStore().put(new ScriptVariable(key, string, mDepth));
        tail = lookupMacroBlocks(input.substring(end + 1));
        return head + "$" + key + tail;
    }

    /**
	 * Extracts a string between start and end index and store it into the eval
	 * blocks table. Replaces the extracted string by a variable.
	 * 
	 * @param input
	 *            the input text to modify
	 * @param start
	 *            the index at which the string to extract starts
	 * @param end
	 *            the index at which the string to extract ends
	 * @return the resulting string
	 */
    public String extractEvalBlock(String input, int start, int end) {
        String head, string, tail, key;
        head = input.substring(0, start);
        string = input.substring(start + 1, end);
        key = PREFIX_EVAL + mDepth + "_" + getEvalBlocksStore().size();
        getEvalBlocksStore().put(new ScriptVariable(key, string, mDepth));
        tail = lookupEvalBlocks(input.substring(end + 1));
        return head + "$" + key + tail;
    }

    /**
	 * Extracts a string between start and end index and store it into the math
	 * blocks table. Replaces the extracted string by a variable.
	 * 
	 * @param input
	 *            the input text to modify
	 * @param start
	 *            the index at which the string to extract starts
	 * @param end
	 *            the index at which the string to extract ends
	 * @return the resulting string
	 */
    public String extractMathBlock(String input, int start, int end) {
        String head, string, tail, key;
        head = input.substring(0, start);
        string = input.substring(start + 1, end);
        key = PREFIX_MATH + mDepth + "_" + getMathBlocksStore().size();
        getMathBlocksStore().put(new ScriptVariable(key, string, mDepth));
        tail = lookupMathBlocks(input.substring(end + 1));
        return head + "$" + key + tail;
    }

    /**
	 * Extracts a string between start and end index and store it into the
	 * string table. Replaces the extracted string by a variable.
	 * 
	 * @param input
	 *            the input text to modify
	 * @param start
	 *            the index at which the string to extract starts
	 * @param end
	 *            the index at which the string to extract ends
	 * @return the resulting string
	 */
    public String extractString(String input, int start, int end) {
        String head, string, tail, key;
        head = input.substring(0, start);
        string = input.substring(start + 1, end);
        key = PREFIX_STRING + mDepth + "_" + getStringsStore().size();
        getStringsStore().put(new ScriptVariable(key, string, mDepth));
        tail = lookupStrings(input.substring(end + 1));
        return head + "$" + key + tail;
    }

    /**
	 * Clears the unused variables from
	 */
    public void clearVariables() {
        getStringsStore().cleanVariables(mDepth);
        getMacroBlocksStore().cleanVariables(mDepth);
        getEvalBlocksStore().cleanVariables(mDepth);
        getMathBlocksStore().cleanVariables(mDepth);
        getVariablesStore().cleanVariables(mDepth);
    }

    /**
	 * Prints the content of the tables
	 */
    public static void printTables() {
        System.out.println("Strings Table");
        getStringsStore().print();
    }

    /**
	 * @return the Strings store
	 */
    public static ScriptVariableStore getStringsStore() {
        if (StringsStore == null) StringsStore = new ScriptVariableStore();
        return StringsStore;
    }

    /**
	 * @return the Macros definitions store
	 */
    public static ScriptVariableStore getMacroBlocksStore() {
        if (MacrosStore == null) MacrosStore = new ScriptVariableStore();
        return MacrosStore;
    }

    /**
	 * @return the Eval Blocks store
	 */
    public static ScriptVariableStore getEvalBlocksStore() {
        if (EvalBlocksStore == null) EvalBlocksStore = new ScriptVariableStore();
        return EvalBlocksStore;
    }

    /**
	 * @return the Math Expressions store
	 */
    public static ScriptVariableStore getMathBlocksStore() {
        if (MathBlocksStore == null) MathBlocksStore = new ScriptVariableStore();
        return MathBlocksStore;
    }

    /**
	 * @return the Variables Store
	 */
    public static ScriptVariableStore getVariablesStore() {
        if (VariablesStore == null) VariablesStore = new ScriptVariableStore();
        return VariablesStore;
    }

    /**
	 * @return the Functions Table
	 */
    public static ScriptVariableStore getFunctionsStore() {
        if (FunctionsStore == null) FunctionsStore = new ScriptVariableStore();
        return FunctionsStore;
    }
}
