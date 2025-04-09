package org.antlr.gunit;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

public class gUnitExecutor {

    public GrammarInfo grammarInfo;

    private final ClassLoader grammarClassLoader;

    public int numOfTest;

    public int numOfSuccess;

    public int numOfFailure;

    private String title;

    public int numOfInvalidInput;

    private String parserName;

    private String lexerName;

    public List<AbstractTest> failures;

    public List<AbstractTest> invalids;

    private PrintStream console = System.out;

    private PrintStream consoleErr = System.err;

    public gUnitExecutor(GrammarInfo grammarInfo) {
        this(grammarInfo, determineClassLoader());
    }

    private static ClassLoader determineClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = gUnitExecutor.class.getClassLoader();
        }
        return classLoader;
    }

    public gUnitExecutor(GrammarInfo grammarInfo, ClassLoader grammarClassLoader) {
        this.grammarInfo = grammarInfo;
        this.grammarClassLoader = grammarClassLoader;
        numOfTest = 0;
        numOfSuccess = 0;
        numOfFailure = 0;
        numOfInvalidInput = 0;
        failures = new ArrayList<AbstractTest>();
        invalids = new ArrayList<AbstractTest>();
    }

    protected ClassLoader getGrammarClassLoader() {
        return grammarClassLoader;
    }

    protected final Class classForName(String name) throws ClassNotFoundException {
        return getGrammarClassLoader().loadClass(name);
    }

    public String execTest() throws IOException {
        StringTemplate testResultST = getTemplateGroup().getInstanceOf("testResult");
        try {
            if (grammarInfo.getHeader() != null) {
                parserName = grammarInfo.getHeader() + "." + grammarInfo.getGrammarName() + "Parser";
                lexerName = grammarInfo.getHeader() + "." + grammarInfo.getGrammarName() + "Lexer";
            } else {
                parserName = grammarInfo.getGrammarName() + "Parser";
                lexerName = grammarInfo.getGrammarName() + "Lexer";
            }
            if (grammarInfo.getTreeGrammarName() != null) {
                title = "executing testsuite for tree grammar:" + grammarInfo.getTreeGrammarName() + " walks " + parserName;
            } else {
                title = "executing testsuite for grammar:" + grammarInfo.getGrammarName();
            }
            executeTests();
            testResultST.setAttribute("title", title);
            testResultST.setAttribute("num_of_test", numOfTest);
            testResultST.setAttribute("num_of_failure", numOfFailure);
            if (numOfFailure > 0) {
                testResultST.setAttribute("failure", failures);
            }
            if (numOfInvalidInput > 0) {
                testResultST.setAttribute("has_invalid", true);
                testResultST.setAttribute("num_of_invalid", numOfInvalidInput);
                testResultST.setAttribute("invalid", invalids);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return testResultST.toString();
    }

    private StringTemplateGroup getTemplateGroup() {
        StringTemplateGroupLoader loader = new CommonGroupLoader("org/antlr/gunit", null);
        StringTemplateGroup.registerGroupLoader(loader);
        StringTemplateGroup.registerDefaultLexer(AngleBracketTemplateLexer.class);
        StringTemplateGroup group = StringTemplateGroup.loadGroup("gUnitTestResult");
        return group;
    }

    private gUnitTestResult runCorrectParser(String parserName, String lexerName, String rule, String lexicalRule, String treeRule, gUnitTestInput input) throws Exception {
        if (lexicalRule != null) return runLexer(lexerName, lexicalRule, input); else if (treeRule != null) return runTreeParser(parserName, lexerName, rule, treeRule, input); else return runParser(parserName, lexerName, rule, input);
    }

    private void executeTests() throws Exception {
        for (gUnitTestSuite ts : grammarInfo.getRuleTestSuites()) {
            String rule = ts.getRuleName();
            String lexicalRule = ts.getLexicalRuleName();
            String treeRule = ts.getTreeRuleName();
            for (gUnitTestInput input : ts.testSuites.keySet()) {
                numOfTest++;
                gUnitTestResult result = null;
                AbstractTest test = ts.testSuites.get(input);
                try {
                    result = runCorrectParser(parserName, lexerName, rule, lexicalRule, treeRule, input);
                } catch (InvalidInputException e) {
                    numOfInvalidInput++;
                    test.setHeader(rule, lexicalRule, treeRule, numOfTest, input.getLine());
                    test.setActual(input.testInput);
                    invalids.add(test);
                    continue;
                }
                String expected = test.getExpected();
                String actual = test.getResult(result);
                test.setActual(actual);
                if (actual == null) {
                    numOfFailure++;
                    test.setHeader(rule, lexicalRule, treeRule, numOfTest, input.getLine());
                    test.setActual("null");
                    failures.add(test);
                } else if (expected.equals(actual) || (expected.equals("FAIL") && !actual.equals("OK"))) {
                    numOfSuccess++;
                } else if (ts.testSuites.get(input).getType() == gUnitParser.ACTION) {
                    numOfFailure++;
                    test.setHeader(rule, lexicalRule, treeRule, numOfTest, input.getLine());
                    test.setActual("\t" + "{ACTION} is not supported in the grammarInfo yet...");
                    failures.add(test);
                } else {
                    numOfFailure++;
                    test.setHeader(rule, lexicalRule, treeRule, numOfTest, input.getLine());
                    failures.add(test);
                }
            }
        }
    }

    protected gUnitTestResult runLexer(String lexerName, String testRuleName, gUnitTestInput testInput) throws Exception {
        CharStream input;
        Class lexer = null;
        PrintStream ps = null;
        PrintStream ps2 = null;
        try {
            if (testInput.inputIsFile) {
                String filePath = testInput.testInput;
                File testInputFile = new File(filePath);
                if (!testInputFile.exists() && grammarInfo.getHeader() != null) {
                    testInputFile = new File("./" + grammarInfo.getHeader().replace('.', '/'), testInput.testInput);
                    if (testInputFile.exists()) filePath = testInputFile.getCanonicalPath();
                }
                input = new ANTLRFileStream(filePath);
            } else {
                input = new ANTLRStringStream(testInput.testInput);
            }
            lexer = classForName(lexerName);
            Class[] lexArgTypes = new Class[] { CharStream.class };
            Constructor lexConstructor = lexer.getConstructor(lexArgTypes);
            Object[] lexArgs = new Object[] { input };
            Object lexObj = lexConstructor.newInstance(lexArgs);
            Method ruleName = lexer.getMethod("m" + testRuleName, new Class[0]);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ps = new PrintStream(out);
            ps2 = new PrintStream(err);
            System.setOut(ps);
            System.setErr(ps2);
            ruleName.invoke(lexObj, new Object[0]);
            Method ruleName2 = lexer.getMethod("getCharIndex", new Class[0]);
            int currentIndex = (Integer) ruleName2.invoke(lexObj, new Object[0]);
            if (currentIndex != input.size()) {
                ps2.print("extra text found, '" + input.substring(currentIndex, input.size() - 1) + "'");
            }
            if (err.toString().length() > 0) {
                gUnitTestResult testResult = new gUnitTestResult(false, err.toString(), true);
                testResult.setError(err.toString());
                return testResult;
            }
            String stdout = null;
            if (out.toString().length() > 0) {
                stdout = out.toString();
            }
            return new gUnitTestResult(true, stdout, true);
        } catch (IOException e) {
            return getTestExceptionResult(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            return getTestExceptionResult(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (ps2 != null) ps2.close();
                System.setOut(console);
                System.setErr(consoleErr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new Exception("This should be unreachable?");
    }

    protected gUnitTestResult runParser(String parserName, String lexerName, String testRuleName, gUnitTestInput testInput) throws Exception {
        CharStream input;
        Class lexer = null;
        Class parser = null;
        PrintStream ps = null;
        PrintStream ps2 = null;
        try {
            if (testInput.inputIsFile) {
                String filePath = testInput.testInput;
                File testInputFile = new File(filePath);
                if (!testInputFile.exists() && grammarInfo.getHeader() != null) {
                    testInputFile = new File("./" + grammarInfo.getHeader().replace('.', '/'), testInput.testInput);
                    if (testInputFile.exists()) filePath = testInputFile.getCanonicalPath();
                }
                input = new ANTLRFileStream(filePath);
            } else {
                input = new ANTLRStringStream(testInput.testInput);
            }
            lexer = classForName(lexerName);
            Class[] lexArgTypes = new Class[] { CharStream.class };
            Constructor lexConstructor = lexer.getConstructor(lexArgTypes);
            Object[] lexArgs = new Object[] { input };
            Object lexObj = lexConstructor.newInstance(lexArgs);
            CommonTokenStream tokens = new CommonTokenStream((Lexer) lexObj);
            parser = classForName(parserName);
            Class[] parArgTypes = new Class[] { TokenStream.class };
            Constructor parConstructor = parser.getConstructor(parArgTypes);
            Object[] parArgs = new Object[] { tokens };
            Object parObj = parConstructor.newInstance(parArgs);
            Method ruleName = parser.getMethod(testRuleName);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ps = new PrintStream(out);
            ps2 = new PrintStream(err);
            System.setOut(ps);
            System.setErr(ps2);
            Object ruleReturn = ruleName.invoke(parObj);
            String astString = null;
            String stString = null;
            if (ruleReturn != null) {
                if (ruleReturn.getClass().toString().indexOf(testRuleName + "_return") > 0) {
                    try {
                        Class _return = classForName(parserName + "$" + testRuleName + "_return");
                        Method[] methods = _return.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("getTree")) {
                                Method returnName = _return.getMethod("getTree");
                                CommonTree tree = (CommonTree) returnName.invoke(ruleReturn);
                                astString = tree.toStringTree();
                            } else if (method.getName().equals("getTemplate")) {
                                Method returnName = _return.getMethod("getTemplate");
                                StringTemplate st = (StringTemplate) returnName.invoke(ruleReturn);
                                stString = st.toString();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            }
            if (tokens.index() != tokens.size()) {
                throw new InvalidInputException();
            }
            if (err.toString().length() > 0) {
                gUnitTestResult testResult = new gUnitTestResult(false, err.toString());
                testResult.setError(err.toString());
                return testResult;
            }
            String stdout = null;
            if (out.toString().length() > 0) {
                stdout = out.toString();
            }
            if (astString != null) {
                return new gUnitTestResult(true, stdout, astString);
            } else if (stString != null) {
                return new gUnitTestResult(true, stdout, stString);
            }
            if (ruleReturn != null) {
                return new gUnitTestResult(true, stdout, String.valueOf(ruleReturn));
            }
            return new gUnitTestResult(true, stdout, stdout);
        } catch (IOException e) {
            return getTestExceptionResult(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            return getTestExceptionResult(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (ps2 != null) ps2.close();
                System.setOut(console);
                System.setErr(consoleErr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new Exception("This should be unreachable?");
    }

    protected gUnitTestResult runTreeParser(String parserName, String lexerName, String testRuleName, String testTreeRuleName, gUnitTestInput testInput) throws Exception {
        CharStream input;
        String treeParserPath;
        Class lexer = null;
        Class parser = null;
        Class treeParser = null;
        PrintStream ps = null;
        PrintStream ps2 = null;
        try {
            if (testInput.inputIsFile) {
                String filePath = testInput.testInput;
                File testInputFile = new File(filePath);
                if (!testInputFile.exists() && grammarInfo.getHeader() != null) {
                    testInputFile = new File("./" + grammarInfo.getHeader().replace('.', '/'), testInput.testInput);
                    if (testInputFile.exists()) filePath = testInputFile.getCanonicalPath();
                }
                input = new ANTLRFileStream(filePath);
            } else {
                input = new ANTLRStringStream(testInput.testInput);
            }
            if (grammarInfo.getHeader() != null) {
                treeParserPath = grammarInfo.getHeader() + "." + grammarInfo.getTreeGrammarName();
            } else {
                treeParserPath = grammarInfo.getTreeGrammarName();
            }
            lexer = classForName(lexerName);
            Class[] lexArgTypes = new Class[] { CharStream.class };
            Constructor lexConstructor = lexer.getConstructor(lexArgTypes);
            Object[] lexArgs = new Object[] { input };
            Object lexObj = lexConstructor.newInstance(lexArgs);
            CommonTokenStream tokens = new CommonTokenStream((Lexer) lexObj);
            parser = classForName(parserName);
            Class[] parArgTypes = new Class[] { TokenStream.class };
            Constructor parConstructor = parser.getConstructor(parArgTypes);
            Object[] parArgs = new Object[] { tokens };
            Object parObj = parConstructor.newInstance(parArgs);
            Method ruleName = parser.getMethod(testRuleName);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ps = new PrintStream(out);
            ps2 = new PrintStream(err);
            System.setOut(ps);
            System.setErr(ps2);
            Object ruleReturn = ruleName.invoke(parObj);
            Class _return = classForName(parserName + "$" + testRuleName + "_return");
            Method returnName = _return.getMethod("getTree");
            CommonTree tree = (CommonTree) returnName.invoke(ruleReturn);
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
            nodes.setTokenStream(tokens);
            treeParser = classForName(treeParserPath);
            Class[] treeParArgTypes = new Class[] { TreeNodeStream.class };
            Constructor treeParConstructor = treeParser.getConstructor(treeParArgTypes);
            Object[] treeParArgs = new Object[] { nodes };
            Object treeParObj = treeParConstructor.newInstance(treeParArgs);
            Method treeRuleName = treeParser.getMethod(testTreeRuleName);
            Object treeRuleReturn = treeRuleName.invoke(treeParObj);
            String astString = null;
            String stString = null;
            if (treeRuleReturn != null) {
                if (treeRuleReturn.getClass().toString().indexOf(testTreeRuleName + "_return") > 0) {
                    try {
                        Class _treeReturn = classForName(treeParserPath + "$" + testTreeRuleName + "_return");
                        Method[] methods = _treeReturn.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("getTree")) {
                                Method treeReturnName = _treeReturn.getMethod("getTree");
                                CommonTree returnTree = (CommonTree) treeReturnName.invoke(treeRuleReturn);
                                astString = returnTree.toStringTree();
                            } else if (method.getName().equals("getTemplate")) {
                                Method treeReturnName = _return.getMethod("getTemplate");
                                StringTemplate st = (StringTemplate) treeReturnName.invoke(treeRuleReturn);
                                stString = st.toString();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            }
            if (tokens.index() != tokens.size()) {
                throw new InvalidInputException();
            }
            if (err.toString().length() > 0) {
                gUnitTestResult testResult = new gUnitTestResult(false, err.toString());
                testResult.setError(err.toString());
                return testResult;
            }
            String stdout = null;
            if (out.toString().length() > 0) {
                stdout = out.toString();
            }
            if (astString != null) {
                return new gUnitTestResult(true, stdout, astString);
            } else if (stString != null) {
                return new gUnitTestResult(true, stdout, stString);
            }
            if (treeRuleReturn != null) {
                return new gUnitTestResult(true, stdout, String.valueOf(treeRuleReturn));
            }
            return new gUnitTestResult(true, stdout, stdout);
        } catch (IOException e) {
            return getTestExceptionResult(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            return getTestExceptionResult(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (ps2 != null) ps2.close();
                System.setOut(console);
                System.setErr(consoleErr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new Exception("Should not be reachable?");
    }

    private gUnitTestResult getTestExceptionResult(Exception e) {
        gUnitTestResult testResult;
        if (e.getCause() != null) {
            testResult = new gUnitTestResult(false, e.getCause().toString(), true);
            testResult.setError(e.getCause().toString());
        } else {
            testResult = new gUnitTestResult(false, e.toString(), true);
            testResult.setError(e.toString());
        }
        return testResult;
    }
}
