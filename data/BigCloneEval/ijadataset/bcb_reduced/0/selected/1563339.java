package gate.jape.parser;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import gate.Factory;
import gate.util.*;
import gate.jape.*;
import gate.jape.constraint.*;
import gate.event.*;
import org.apache.log4j.Logger;

/**
  * A parser for the CPSL language. Generated using JavaCC.
  * @author Hamish Cunningham
  */
public class ParseCpsl implements JapeConstants, ParseCpslConstants {

    private static final Logger log = Logger.getLogger(ParseCpsl.class);

    /** Construct from a URL and an encoding
    */
    public ParseCpsl(URL url, String encoding) throws IOException {
        this(url, encoding, new HashMap());
    }

    /** Construct from a URL and an encoding
    */
    public ParseCpsl(URL url, String encoding, HashMap existingMacros) throws IOException {
        this(url, encoding, existingMacros, new HashMap());
    }

    public ParseCpsl(URL url, String encoding, HashMap existingMacros, HashMap existingTemplates) throws IOException {
        this(new BomStrippingInputStreamReader(url.openStream(), encoding), existingMacros, existingTemplates);
        baseURL = url;
        this.encoding = encoding;
    }

    public ParseCpsl(java.io.Reader stream, HashMap existingMacros) {
        this(stream, existingMacros, new HashMap());
    }

    public ParseCpsl(java.io.Reader stream, HashMap existingMacros, HashMap existingTemplates) {
        this(stream);
        macrosMap = existingMacros;
        templatesMap = existingTemplates;
    }

    public void addStatusListener(StatusListener listener) {
        myStatusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        myStatusListeners.remove(listener);
    }

    protected void fireStatusChangedEvent(String text) {
        java.util.Iterator listenersIter = myStatusListeners.iterator();
        while (listenersIter.hasNext()) ((StatusListener) listenersIter.next()).statusChanged(text);
    }

    protected SinglePhaseTransducer createSinglePhaseTransducer(String name) {
        try {
            Constructor<? extends SinglePhaseTransducer> c = sptClass.getConstructor(String.class);
            return (SinglePhaseTransducer) c.newInstance(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected ParseCpsl spawn(URL sptURL) throws IOException {
        ParseCpsl newParser = new ParseCpsl(sptURL, encoding, macrosMap, templatesMap);
        newParser.setSptClass(this.sptClass);
        return newParser;
    }

    protected void finishSPT(SinglePhaseTransducer t) throws ParseException {
        if (ruleNumber == 0) throw (new ParseException("no rules defined in transducer " + t.getName()));
        t.setBaseURL(baseURL);
    }

    protected void finishBPE(BasicPatternElement bpe) {
    }

    /**
   * Attempt to parse a multi phase transducer from the current file.  This
   * method ensures that the JAPE file reader is properly closed when the
   * method completes, whether it completes successfully or throws an
   * exception.
   */
    public MultiPhaseTransducer MultiPhaseTransducer() throws ParseException {
        try {
            return _MultiPhaseTransducer();
        } finally {
            if (jj_input_stream.inputStream != null) {
                try {
                    jj_input_stream.inputStream.close();
                } catch (IOException e) {
                    log.warn("Couldn't close input stream while parsing " + baseURL, e);
                }
            }
        }
    }

    /**
   * Append the given string to the end of the given buffer as a Java string
   * literal.  If <code>str</code> is <code>null</code>, we append the four
   * characters n, u, l, l.  Otherwise, we append the contents of str surrounded
   * by double quotes, except that characters in str are escaped as necessary
   * to be a legal Java string literal: backspace, formfeed, tab, newline and
   * return are replaced by their escape sequences \b, \f, etc.; single and double
   * quote and backslash are preceded by an extra backslash; other non-ASCII
   * and non-printing characters are rendered as Unicode escapes (backslash-u
   * followed by four hex digits).
   */
    protected void appendJavaStringLiteral(StringBuffer buf, String str) {
        if (str == null) {
            buf.append("null");
        } else {
            Formatter formatter = null;
            buf.append("\"");
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch(c) {
                    case '\b':
                        buf.append("\\b");
                        break;
                    case '\f':
                        buf.append("\\f");
                        break;
                    case '\n':
                        buf.append("\\n");
                        break;
                    case '\r':
                        buf.append("\\r");
                        break;
                    case '\t':
                        buf.append("\\t");
                        break;
                    case '\"':
                        buf.append("\\\"");
                        break;
                    case '\'':
                        buf.append("\\\'");
                        break;
                    case '\\':
                        buf.append("\\\\");
                        break;
                    default:
                        if (c < 32 || c > 127) {
                            if (formatter == null) formatter = new Formatter(buf);
                            formatter.format("\\u%04X", Integer.valueOf(c));
                        } else {
                            buf.append(c);
                        }
                        break;
                }
            }
            buf.append("\"");
        }
    }

    protected void appendAnnotationAdd(StringBuffer blockBuffer, String newAnnotType, String annotSetName) {
        String nl = Strings.getNl();
        blockBuffer.append("      if(outputAS == inputAS) { // use nodes directly" + nl);
        blockBuffer.append("        outputAS.add(" + nl);
        blockBuffer.append("          " + annotSetName + ".firstNode(), ");
        blockBuffer.append(annotSetName + ".lastNode(), " + nl);
        blockBuffer.append("          ");
        appendJavaStringLiteral(blockBuffer, newAnnotType);
        blockBuffer.append(", features" + nl);
        blockBuffer.append("        );" + nl);
        blockBuffer.append("      }" + nl);
        blockBuffer.append("      else { // use offsets" + nl);
        blockBuffer.append("        try {" + nl);
        blockBuffer.append("          outputAS.add(" + nl);
        blockBuffer.append("            " + annotSetName + ".firstNode().getOffset(), ");
        blockBuffer.append(annotSetName + ".lastNode().getOffset(), " + nl);
        blockBuffer.append("            ");
        appendJavaStringLiteral(blockBuffer, newAnnotType);
        blockBuffer.append(", features" + nl);
        blockBuffer.append("          );" + nl);
        blockBuffer.append("        }" + nl);
        blockBuffer.append("        catch(gate.util.InvalidOffsetException ioe) {" + nl);
        blockBuffer.append("          throw new LuckyException(\"Invalid offset exception generated \" +" + nl);
        blockBuffer.append("               \"from offsets taken from same document!\");" + nl);
        blockBuffer.append("        }" + nl);
        blockBuffer.append("      }" + nl);
        blockBuffer.append("      // end of RHS assignment block");
    }

    /**
   * Takes a string containing ${key} placeholders and substitutes
   * in the corresponding values from the given map.  If there is
   * no value in the map for a particular placeholder it is left
   * un-resolved, i.e. given a template of "${key1}/${key2}" and
   * a values map of just [key1: "hello"], this method would return
   * "hello/${key2}".
   */
    protected Pair substituteTemplate(Token templateNameTok, Map<String, Object> values) throws ParseException {
        Pair template = (Pair) templatesMap.get(templateNameTok.image);
        if (template == null) {
            throw new ParseException(errorMsgPrefix(templateNameTok) + "unknown template name " + templateNameTok.image);
        }
        Pair returnVal = null;
        Set<String> unusedParams = new HashSet<String>(values.keySet());
        if (((Integer) template.first).intValue() == string) {
            log.debug("Substituting template " + templateNameTok.image + " with map " + values + ". Template is " + template);
            StringBuffer buf = new StringBuffer();
            Matcher mat = Pattern.compile("\\$\\{([^\\}]+)\\}").matcher((String) template.second);
            while (mat.find()) {
                String key = mat.group(1);
                if (values.containsKey(key)) {
                    mat.appendReplacement(buf, Matcher.quoteReplacement(String.valueOf(values.get(key))));
                    unusedParams.remove(key);
                } else {
                    mat.appendReplacement(buf, "\\${");
                    buf.append(key);
                    buf.append("}");
                }
            }
            mat.appendTail(buf);
            returnVal = new Pair();
            returnVal.first = Integer.valueOf(string);
            returnVal.second = buf.toString();
            log.debug("Template substitution produced " + returnVal.second);
        } else {
            returnVal = template;
        }
        if (!unusedParams.isEmpty()) {
            throw new ParseException(errorMsgPrefix(templateNameTok) + "invalid parameters " + unusedParams + " for template " + templateNameTok.image);
        } else {
            return returnVal;
        }
    }

    public void setBaseURL(URL newURL) {
        baseURL = newURL;
    }

    public void setEncoding(String newEncoding) {
        encoding = newEncoding;
    }

    public void setSptClass(Class<? extends SinglePhaseTransducer> sptClass) {
        this.sptClass = sptClass;
    }

    private String errorMsgPrefix(Token t) {
        return ((baseURL != null) ? baseURL.toExternalForm() : "(No URL)") + ((t == null) ? " " : ":" + t.beginLine + ":" + t.beginColumn + ": ");
    }

    private transient java.util.List myStatusListeners = new java.util.LinkedList();

    /** Position of the current rule */
    private int ruleNumber;

    /** A list of all the bindings we made this time, for checking
    * the RHS during parsing.
    */
    private HashSet bindingNameSet = null;

    /** A table of macro definitions. */
    protected HashMap macrosMap;

    /**
   * A table of template definitions. Keys are template names,
   * values are Pairs of token kind and value, as returned by
   * AttrVal.
   */
    protected HashMap templatesMap;

    protected URL baseURL;

    protected String encoding;

    protected Class<? extends SinglePhaseTransducer> sptClass = SinglePhaseTransducer.class;

    protected SinglePhaseTransducer curSPT;

    public final MultiPhaseTransducer _MultiPhaseTransducer() throws ParseException {
        SinglePhaseTransducer s = null;
        MultiPhaseTransducer m = new MultiPhaseTransducer();
        m.setBaseURL(baseURL);
        Token mptNameTok = null;
        Token phaseNameTok = null;
        String javaimportblock = null;
        String controllerstartedblock = null;
        String controllerfinishedblock = null;
        String controllerabortedblock = null;
        boolean haveControllerStartedBlock = false;
        boolean haveControllerFinishedBlock = false;
        boolean haveControllerAbortedBlock = false;
        switch(jj_nt.kind) {
            case multiphase:
                jj_consume_token(multiphase);
                mptNameTok = jj_consume_token(ident);
                m.setName(mptNameTok.image);
                break;
            default:
                jj_la1[0] = jj_gen;
                ;
        }
        switch(jj_nt.kind) {
            case javaimport:
            case controllerstarted:
            case controllerfinished:
            case controlleraborted:
            case phase:
                javaimportblock = JavaImportBlock();
                label_1: while (true) {
                    switch(jj_nt.kind) {
                        case controllerstarted:
                        case controllerfinished:
                        case controlleraborted:
                            ;
                            break;
                        default:
                            jj_la1[1] = jj_gen;
                            break label_1;
                    }
                    switch(jj_nt.kind) {
                        case controllerstarted:
                            controllerstartedblock = ControllerStartedBlock();
                            if (haveControllerStartedBlock) {
                                if (true) throw new ParseException("Only one ControllerStarted block allowed");
                            } else haveControllerStartedBlock = true;
                            break;
                        case controllerfinished:
                            controllerfinishedblock = ControllerFinishedBlock();
                            if (haveControllerFinishedBlock) {
                                if (true) throw new ParseException("Only one ControllerFinished block allowed");
                            } else haveControllerFinishedBlock = true;
                            break;
                        case controlleraborted:
                            controllerabortedblock = ControllerAbortedBlock();
                            if (haveControllerAbortedBlock) {
                                if (true) throw new ParseException("Only one ControllerAborted block allowed");
                            } else haveControllerAbortedBlock = true;
                            break;
                        default:
                            jj_la1[2] = jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                }
                label_2: while (true) {
                    try {
                        s = SinglePhaseTransducer(javaimportblock);
                        m.addPhase(s.getName(), s);
                        s.setBaseURL(baseURL);
                        s.setControllerEventBlocks(controllerstartedblock, controllerfinishedblock, controllerabortedblock, javaimportblock);
                        controllerstartedblock = null;
                        controllerfinishedblock = null;
                        controllerabortedblock = null;
                    } catch (Throwable e) {
                        {
                            if (true) throw (new ParseException("Cannot parse a phase in " + baseURL + ": " + e.getMessage()));
                        }
                    }
                    switch(jj_nt.kind) {
                        case phase:
                            ;
                            break;
                        default:
                            jj_la1[3] = jj_gen;
                            break label_2;
                    }
                }
                break;
            case phases:
                jj_consume_token(phases);
                label_3: while (true) {
                    phaseNameTok = jj_consume_token(path);
                    ParseCpsl parser = null;
                    String sptPath = phaseNameTok.image + ".jape";
                    URL sptURL = null;
                    try {
                        sptURL = new URL(baseURL, sptPath);
                    } catch (MalformedURLException mue) {
                        {
                            if (true) throw (new ParseException(errorMsgPrefix(phaseNameTok) + "Read error " + mue.toString()));
                        }
                    }
                    if (sptURL == null) {
                        {
                            if (true) throw (new ParseException(errorMsgPrefix(phaseNameTok) + "Resource not found: base = " + baseURL.toString() + " path = " + sptPath));
                        }
                    }
                    fireStatusChangedEvent("Reading " + phaseNameTok.image + "...");
                    try {
                        parser = spawn(sptURL);
                    } catch (IOException e) {
                        {
                            if (true) throw (new ParseException(errorMsgPrefix(phaseNameTok) + "Cannot open URL " + sptURL.toExternalForm()));
                        }
                    }
                    if (parser != null) {
                        ArrayList phases = parser.MultiPhaseTransducer().getPhases();
                        if (phases != null) {
                            for (int i = 0; i < phases.size(); i++) {
                                m.addPhase(((Transducer) phases.get(i)).getName(), (Transducer) phases.get(i));
                            }
                        }
                    }
                    switch(jj_nt.kind) {
                        case path:
                            ;
                            break;
                        default:
                            jj_la1[4] = jj_gen;
                            break label_3;
                    }
                }
                break;
            default:
                jj_la1[5] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(0);
        {
            if (true) return m;
        }
        throw new Error("Missing return statement in function");
    }

    public final SinglePhaseTransducer SinglePhaseTransducer(String javaimportblock) throws ParseException {
        ruleNumber = 0;
        Token phaseNameTok = null;
        Token inputTok = null;
        SinglePhaseTransducer t = null;
        Rule newRule = null;
        bindingNameSet = new HashSet();
        Token optionNameTok = null;
        Token optionValueTok = null;
        jj_consume_token(phase);
        phaseNameTok = jj_consume_token(ident);
        t = createSinglePhaseTransducer(phaseNameTok.image);
        curSPT = t;
        label_4: while (true) {
            switch(jj_nt.kind) {
                case input:
                case option:
                    ;
                    break;
                default:
                    jj_la1[6] = jj_gen;
                    break label_4;
            }
            switch(jj_nt.kind) {
                case input:
                    jj_consume_token(input);
                    label_5: while (true) {
                        switch(jj_nt.kind) {
                            case ident:
                                ;
                                break;
                            default:
                                jj_la1[7] = jj_gen;
                                break label_5;
                        }
                        inputTok = jj_consume_token(ident);
                        t.addInput(inputTok.image);
                    }
                    break;
                case option:
                    jj_consume_token(option);
                    label_6: while (true) {
                        switch(jj_nt.kind) {
                            case ident:
                                ;
                                break;
                            default:
                                jj_la1[8] = jj_gen;
                                break label_6;
                        }
                        optionNameTok = jj_consume_token(ident);
                        jj_consume_token(assign);
                        switch(jj_nt.kind) {
                            case ident:
                                optionValueTok = jj_consume_token(ident);
                                break;
                            case bool:
                                optionValueTok = jj_consume_token(bool);
                                break;
                            default:
                                jj_la1[9] = jj_gen;
                                jj_consume_token(-1);
                                throw new ParseException();
                        }
                        t.setOption(optionNameTok.image, optionValueTok.image);
                        if (optionNameTok.image.equalsIgnoreCase("control")) {
                            if (optionValueTok.image.equalsIgnoreCase("appelt")) t.setRuleApplicationStyle(APPELT_STYLE); else if (optionValueTok.image.equalsIgnoreCase("first")) t.setRuleApplicationStyle(FIRST_STYLE); else if (optionValueTok.image.equalsIgnoreCase("brill")) t.setRuleApplicationStyle(BRILL_STYLE); else if (optionValueTok.image.equalsIgnoreCase("once")) t.setRuleApplicationStyle(ONCE_STYLE); else if (optionValueTok.image.equalsIgnoreCase("all")) t.setRuleApplicationStyle(ALL_STYLE); else System.err.println(errorMsgPrefix(optionValueTok) + "ignoring unknown control strategy " + option + " (should be brill, appelt, first, once or all)");
                        } else if (optionNameTok.image.equalsIgnoreCase("debug")) {
                            if (optionValueTok.image.equalsIgnoreCase("true") || optionValueTok.image.equalsIgnoreCase("yes") || optionValueTok.image.equalsIgnoreCase("y")) t.setDebugMode(true); else t.setDebugMode(false);
                        } else if (optionNameTok.image.equalsIgnoreCase("matchGroup")) {
                            if (optionValueTok.image.equalsIgnoreCase("true") || optionValueTok.image.equalsIgnoreCase("yes") || optionValueTok.image.equalsIgnoreCase("y")) t.setMatchGroupMode(true); else t.setMatchGroupMode(false);
                        } else if (optionNameTok.image.equalsIgnoreCase("negationGrouping")) {
                            if (optionValueTok.image.equalsIgnoreCase("false") || optionValueTok.image.equalsIgnoreCase("no") || optionValueTok.image.equalsIgnoreCase("n")) t.setNegationCompatMode(true); else t.setNegationCompatMode(false);
                        }
                    }
                    break;
                default:
                    jj_la1[10] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
        label_7: while (true) {
            switch(jj_nt.kind) {
                case rule:
                case macro:
                case template:
                    ;
                    break;
                default:
                    jj_la1[11] = jj_gen;
                    break label_7;
            }
            switch(jj_nt.kind) {
                case rule:
                    newRule = Rule(phaseNameTok.image, javaimportblock);
                    t.addRule(newRule);
                    break;
                case macro:
                    MacroDef();
                    break;
                case template:
                    TemplateDef();
                    break;
                default:
                    jj_la1[12] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
        finishSPT(t);
        {
            if (true) return t;
        }
        throw new Error("Missing return statement in function");
    }

    public final String JavaImportBlock() throws ParseException {
        String defaultimportblock = "import java.io.*;\n" + "import java.util.*;\n" + "import gate.*;\n" + "import gate.jape.*;\n" + "import gate.creole.ontology.*;\n" + "import gate.annotation.*;\n" + "import gate.util.*;\n";
        String importblock = null;
        switch(jj_nt.kind) {
            case javaimport:
                jj_consume_token(javaimport);
                jj_consume_token(leftBrace);
                importblock = ConsumeBlock();
                break;
            default:
                jj_la1[13] = jj_gen;
                ;
        }
        if (importblock != null) {
            {
                if (true) return defaultimportblock + importblock;
            }
        } else {
            {
                if (true) return defaultimportblock;
            }
        }
        throw new Error("Missing return statement in function");
    }

    public final String ControllerStartedBlock() throws ParseException {
        String block = null;
        jj_consume_token(controllerstarted);
        jj_consume_token(leftBrace);
        block = ConsumeBlock();
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    public final String ControllerFinishedBlock() throws ParseException {
        String block = null;
        jj_consume_token(controllerfinished);
        jj_consume_token(leftBrace);
        block = ConsumeBlock();
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    public final String ControllerAbortedBlock() throws ParseException {
        String block = null;
        jj_consume_token(controlleraborted);
        jj_consume_token(leftBrace);
        block = ConsumeBlock();
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    public final Rule Rule(String phaseName, String currentImports) throws ParseException {
        Token ruleNameTok = null;
        String ruleName = null;
        Token priorityTok = null;
        int rulePriority = 0;
        LeftHandSide lhs = null;
        RightHandSide rhs = null;
        Rule newRule = null;
        bindingNameSet.clear();
        jj_consume_token(rule);
        ruleNameTok = jj_consume_token(ident);
        ruleName = ruleNameTok.image;
        switch(jj_nt.kind) {
            case priority:
                jj_consume_token(priority);
                priorityTok = jj_consume_token(integer);
                try {
                    rulePriority = Integer.parseInt(priorityTok.image);
                } catch (NumberFormatException e) {
                    System.err.println(errorMsgPrefix(priorityTok) + "bad priority spec(" + priorityTok.image + "), rule(" + ruleName + ") - treating as 0");
                    rulePriority = 0;
                }
                break;
            default:
                jj_la1[14] = jj_gen;
                ;
        }
        lhs = LeftHandSide();
        jj_consume_token(72);
        rhs = RightHandSide(phaseName, ruleName, lhs, currentImports);
        try {
            rhs.createActionClass();
        } catch (JapeException e) {
            {
                if (true) throw new ParseException(errorMsgPrefix(null) + "couldn't create rule RHS: " + e.toString());
            }
        }
        newRule = new Rule(ruleName, ruleNumber, rulePriority, lhs, rhs);
        if (curSPT.isInputRestricted()) {
            HashSet<String> set = new HashSet<String>();
            lhs.getConstraintGroup().getContainedAnnotationTypes(set);
            for (String type : set) {
                if (!curSPT.hasInput(type)) {
                    System.err.println(errorMsgPrefix(null) + "Rule " + ruleName + " contains unlisted annotation type " + type);
                }
            }
        }
        ruleNumber++;
        {
            if (true) return newRule;
        }
        throw new Error("Missing return statement in function");
    }

    public final void MacroDef() throws ParseException {
        Token macroNameTok = null;
        Object body = null;
        jj_consume_token(macro);
        macroNameTok = jj_consume_token(ident);
        if (jj_2_1(2)) {
            body = PatternElement();
        } else {
            switch(jj_nt.kind) {
                case ident:
                case colon:
                case leftBrace:
                case colonplus:
                    body = Action(false);
                    break;
                default:
                    jj_la1[15] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
        macrosMap.put(macroNameTok.image, body);
    }

    public final void TemplateDef() throws ParseException {
        Token templateNameTok = null;
        Pair value = null;
        jj_consume_token(template);
        templateNameTok = jj_consume_token(ident);
        jj_consume_token(assign);
        value = AttrVal();
        templatesMap.put(templateNameTok.image, value);
    }

    public final LeftHandSide LeftHandSide() throws ParseException {
        ConstraintGroup cg = new ConstraintGroup();
        ConstraintGroup(cg);
        LeftHandSide lhs = new LeftHandSide(cg);
        Iterator<ComplexPatternElement> boundCPEs = cg.getCPEs();
        while (boundCPEs.hasNext()) {
            ComplexPatternElement cpe = boundCPEs.next();
            String bindingName = cpe.getBindingName();
            if (bindingName != null) {
                try {
                    lhs.addBinding(bindingName, cpe, bindingNameSet);
                } catch (JapeException e) {
                    System.err.println(errorMsgPrefix(null) + "duplicate binding name " + bindingName + " - ignoring this binding! exception was: " + e.toString());
                }
            }
        }
        {
            if (true) return lhs;
        }
        throw new Error("Missing return statement in function");
    }

    public final void ConstraintGroup(ConstraintGroup cg) throws ParseException {
        PatternElement pat = null;
        label_8: while (true) {
            pat = PatternElement();
            cg.addPatternElement(pat);
            switch(jj_nt.kind) {
                case string:
                case ident:
                case leftBrace:
                case leftBracket:
                    ;
                    break;
                default:
                    jj_la1[16] = jj_gen;
                    break label_8;
            }
        }
        label_9: while (true) {
            switch(jj_nt.kind) {
                case bar:
                    ;
                    break;
                default:
                    jj_la1[17] = jj_gen;
                    break label_9;
            }
            jj_consume_token(bar);
            cg.createDisjunction();
            label_10: while (true) {
                pat = PatternElement();
                cg.addPatternElement(pat);
                switch(jj_nt.kind) {
                    case string:
                    case ident:
                    case leftBrace:
                    case leftBracket:
                        ;
                        break;
                    default:
                        jj_la1[18] = jj_gen;
                        break label_10;
                }
            }
        }
    }

    public final PatternElement PatternElement() throws ParseException {
        PatternElement pat = null;
        Token macroRefTok = null;
        switch(jj_nt.kind) {
            case ident:
                macroRefTok = jj_consume_token(ident);
                Object macro = macrosMap.get(macroRefTok.image);
                if (macro == null) {
                    if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "unknown macro name " + macroRefTok.image));
                } else if (macro instanceof String[]) {
                    if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "macro " + macroRefTok.image + " references an Action, not a PatternElement"));
                } else if (!(macro instanceof PatternElement)) {
                    if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "macro " + macroRefTok.image + " doesn't reference a PatternElement!"));
                } else {
                    pat = (PatternElement) ((PatternElement) macro).clone();
                }
                break;
            case string:
            case leftBrace:
                pat = BasicPatternElement();
                break;
            case leftBracket:
                pat = ComplexPatternElement();
                break;
            default:
                jj_la1[19] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true) return pat;
        }
        throw new Error("Missing return statement in function");
    }

    public final BasicPatternElement BasicPatternElement() throws ParseException {
        Token shortTok = null;
        Constraint c = null;
        BasicPatternElement bpe = new BasicPatternElement(curSPT);
        switch(jj_nt.kind) {
            case leftBrace:
                jj_consume_token(leftBrace);
                c = Constraint();
                bpe.addConstraint(c);
                label_11: while (true) {
                    switch(jj_nt.kind) {
                        case comma:
                            ;
                            break;
                        default:
                            jj_la1[20] = jj_gen;
                            break label_11;
                    }
                    jj_consume_token(comma);
                    c = Constraint();
                    bpe.addConstraint(c);
                }
                jj_consume_token(rightBrace);
                break;
            case string:
                shortTok = jj_consume_token(string);
                System.err.println(errorMsgPrefix(shortTok) + "string shorthand not supported yet, ignoring: " + shortTok.image);
                break;
            default:
                jj_la1[21] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        finishBPE(bpe);
        {
            if (true) return bpe;
        }
        throw new Error("Missing return statement in function");
    }

    public final ComplexPatternElement ComplexPatternElement() throws ParseException {
        KleeneOperator kleeneOperator = null;
        Token bindingNameTok = null;
        ConstraintGroup cg = new ConstraintGroup();
        jj_consume_token(leftBracket);
        ConstraintGroup(cg);
        jj_consume_token(rightBracket);
        switch(jj_nt.kind) {
            case kleeneOp:
            case leftSquare:
                kleeneOperator = KleeneOperator();
                break;
            default:
                jj_la1[22] = jj_gen;
                ;
        }
        switch(jj_nt.kind) {
            case colon:
                jj_consume_token(colon);
                switch(jj_nt.kind) {
                    case ident:
                        bindingNameTok = jj_consume_token(ident);
                        break;
                    case integer:
                        bindingNameTok = jj_consume_token(integer);
                        break;
                    default:
                        jj_la1[23] = jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
                break;
            default:
                jj_la1[24] = jj_gen;
                ;
        }
        String bindingName = null;
        if (bindingNameTok != null) bindingName = bindingNameTok.image;
        {
            if (true) return new ComplexPatternElement(cg, kleeneOperator, bindingName);
        }
        throw new Error("Missing return statement in function");
    }

    public final KleeneOperator KleeneOperator() throws ParseException {
        Token kleeneOpTok = null;
        Token minTok = null;
        Token maxTok = null;
        Integer min = null;
        Integer max = null;
        switch(jj_nt.kind) {
            case kleeneOp:
                kleeneOpTok = jj_consume_token(kleeneOp);
                if (kleeneOpTok == null) {
                    {
                        if (true) return new KleeneOperator(KleeneOperator.Type.SINGLE);
                    }
                }
                KleeneOperator.Type type = KleeneOperator.Type.getFromSymbol(kleeneOpTok.image);
                if (type != null) {
                    if (true) return new KleeneOperator(type);
                } else {
                    System.err.println(errorMsgPrefix(kleeneOpTok) + "ignoring uninterpretable Kleene op " + kleeneOpTok.image);
                    {
                        if (true) return new KleeneOperator(KleeneOperator.Type.SINGLE);
                    }
                }
                break;
            case leftSquare:
                jj_consume_token(leftSquare);
                minTok = jj_consume_token(integer);
                switch(jj_nt.kind) {
                    case comma:
                        jj_consume_token(comma);
                        maxTok = jj_consume_token(integer);
                        break;
                    default:
                        jj_la1[25] = jj_gen;
                        ;
                }
                jj_consume_token(rightSquare);
                if (minTok != null) min = new Integer(minTok.image);
                if (maxTok != null) max = new Integer(maxTok.image); else max = min;
                {
                    if (true) return new KleeneOperator(min, max);
                }
                break;
            default:
                jj_la1[26] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        throw new Error("Missing return statement in function");
    }

    public final Constraint Constraint() throws ParseException {
        Token annotTypeTok = null;
        Token metaPropertyTok = null;
        AnnotationAccessor accessor = null;
        Token opTok = null;
        Object attrValObj = null;
        Pair attrValPair = null;
        boolean negate = false;
        Constraint c = null;
        Constraint embeddedConstraint = null;
        String opString = null;
        switch(jj_nt.kind) {
            case pling:
                jj_consume_token(pling);
                negate = true;
                break;
            default:
                jj_la1[27] = jj_gen;
                ;
        }
        annotTypeTok = jj_consume_token(ident);
        c = Factory.getConstraintFactory().createConstraint(annotTypeTok.image);
        if (negate) c.negate();
        switch(jj_nt.kind) {
            case metaPropOp:
            case ident:
            case period:
                switch(jj_nt.kind) {
                    case period:
                        accessor = FeatureAccessor();
                        opTok = jj_consume_token(attrOp);
                        attrValPair = AttrVal();
                        opString = opTok.image;
                        c.addAttribute(Factory.getConstraintFactory().createPredicate(opString, accessor, attrValPair.second));
                        break;
                    case metaPropOp:
                        jj_consume_token(metaPropOp);
                        metaPropertyTok = jj_consume_token(ident);
                        opTok = jj_consume_token(attrOp);
                        attrValPair = AttrVal();
                        accessor = Factory.getConstraintFactory().createMetaPropertyAccessor(metaPropertyTok.image);
                        opString = opTok.image;
                        c.addAttribute(Factory.getConstraintFactory().createPredicate(opString, accessor, attrValPair.second));
                        break;
                    case ident:
                        opTok = jj_consume_token(ident);
                        switch(jj_nt.kind) {
                            case leftBrace:
                                jj_consume_token(leftBrace);
                                embeddedConstraint = Constraint();
                                jj_consume_token(rightBrace);
                                break;
                            case pling:
                            case ident:
                                embeddedConstraint = Constraint();
                                break;
                            default:
                                jj_la1[28] = jj_gen;
                                jj_consume_token(-1);
                                throw new ParseException();
                        }
                        opString = opTok.image;
                        accessor = new SimpleAnnotationAccessor();
                        c.addAttribute(Factory.getConstraintFactory().createPredicate(opString, accessor, embeddedConstraint));
                        break;
                    default:
                        jj_la1[29] = jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
                break;
            default:
                jj_la1[30] = jj_gen;
                ;
        }
        {
            if (true) return c;
        }
        throw new Error("Missing return statement in function");
    }

    public final AnnotationAccessor FeatureAccessor() throws ParseException {
        Token attrNameTok = null;
        AnnotationAccessor accessor = null;
        jj_consume_token(period);
        attrNameTok = jj_consume_token(ident);
        accessor = Factory.getConstraintFactory().createDefaultAccessor(attrNameTok.image);
        {
            if (true) return accessor;
        }
        throw new Error("Missing return statement in function");
    }

    public final Pair AttrVal() throws ParseException {
        Token attrValTok = null;
        String attrValString = null;
        Pair val = new Pair();
        switch(jj_nt.kind) {
            case integer:
            case string:
            case bool:
            case ident:
            case floatingPoint:
                switch(jj_nt.kind) {
                    case string:
                        attrValTok = jj_consume_token(string);
                        break;
                    case ident:
                        attrValTok = jj_consume_token(ident);
                        break;
                    case integer:
                        attrValTok = jj_consume_token(integer);
                        break;
                    case floatingPoint:
                        attrValTok = jj_consume_token(floatingPoint);
                        break;
                    case bool:
                        attrValTok = jj_consume_token(bool);
                        break;
                    default:
                        jj_la1[31] = jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
                val.first = new Integer(attrValTok.kind);
                switch(attrValTok.kind) {
                    case string:
                        val.second = attrValTok.image.substring(1, attrValTok.image.length() - 1);
                        break;
                    case integer:
                        try {
                            val.second = Long.valueOf(attrValTok.image);
                        } catch (NumberFormatException e) {
                            System.err.println(errorMsgPrefix(attrValTok) + "couldn't parse integer " + attrValTok.image + " - treating as 0");
                            val.second = new Long(0);
                        }
                        break;
                    case ident:
                        val.second = new String(attrValTok.image);
                        break;
                    case bool:
                        val.second = Boolean.valueOf(attrValTok.image);
                        break;
                    case floatingPoint:
                        try {
                            val.second = Double.valueOf(attrValTok.image);
                        } catch (NumberFormatException e) {
                            System.err.println(errorMsgPrefix(attrValTok) + "couldn't parse float " + attrValTok.image + " - treating as 0.0");
                            val.second = new Double(0.0);
                        }
                        break;
                    default:
                        System.err.println(errorMsgPrefix(attrValTok) + "didn't understand type of " + attrValTok.image + ": ignoring");
                        val.second = new String("");
                        break;
                }
                {
                    if (true) return val;
                }
                break;
            case leftSquare:
                val = TemplateCall();
                {
                    if (true) return val;
                }
                break;
            default:
                jj_la1[32] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        throw new Error("Missing return statement in function");
    }

    public final Pair TemplateCall() throws ParseException {
        Token templateNameTok = null;
        Token attrNameTok = null;
        Pair attrVal = null;
        Map<String, Object> placeholders = new HashMap<String, Object>();
        jj_consume_token(leftSquare);
        templateNameTok = jj_consume_token(ident);
        label_12: while (true) {
            switch(jj_nt.kind) {
                case ident:
                    ;
                    break;
                default:
                    jj_la1[33] = jj_gen;
                    break label_12;
            }
            attrNameTok = jj_consume_token(ident);
            jj_consume_token(assign);
            attrVal = AttrVal();
            placeholders.put(attrNameTok.image, attrVal.second);
            switch(jj_nt.kind) {
                case comma:
                    jj_consume_token(comma);
                    break;
                default:
                    jj_la1[34] = jj_gen;
                    ;
            }
        }
        jj_consume_token(rightSquare);
        {
            if (true) return substituteTemplate(templateNameTok, placeholders);
        }
        throw new Error("Missing return statement in function");
    }

    public final RightHandSide RightHandSide(String phaseName, String ruleName, LeftHandSide lhs, String imports) throws ParseException {
        String[] block = new String[2];
        RightHandSide rhs = new RightHandSide(phaseName, ruleName, lhs, imports);
        block = Action(true);
        rhs.addBlock(block[0], block[1]);
        label_13: while (true) {
            switch(jj_nt.kind) {
                case comma:
                    ;
                    break;
                default:
                    jj_la1[35] = jj_gen;
                    break label_13;
            }
            jj_consume_token(comma);
            block = Action(true);
            rhs.addBlock(block[0], block[1]);
        }
        {
            if (true) return rhs;
        }
        throw new Error("Missing return statement in function");
    }

    public final String[] Action(boolean checkLabel) throws ParseException {
        String[] block = new String[2];
        Token macroRefTok = null;
        if (jj_2_2(3)) {
            block = NamedJavaBlock(checkLabel);
        } else {
            switch(jj_nt.kind) {
                case leftBrace:
                    block = AnonymousJavaBlock();
                    break;
                case colon:
                case colonplus:
                    block = AssignmentExpression(checkLabel);
                    break;
                case ident:
                    macroRefTok = jj_consume_token(ident);
                    Object macro = macrosMap.get(macroRefTok.image);
                    if (macro == null) {
                        if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "unknown macro name " + macroRefTok.image));
                    } else if (macro instanceof PatternElement) {
                        if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "macro " + macroRefTok.image + " references a PatternElement, not an Action"));
                    } else if (!(macro instanceof String[])) {
                        if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "macro " + macroRefTok.image + " doesn't reference an Action!"));
                    } else {
                        block = (String[]) macro;
                        if (block[0] != null && !bindingNameSet.contains(block[0])) {
                            {
                                if (true) throw (new ParseException(errorMsgPrefix(macroRefTok) + "RHS macro reference " + macroRefTok.image + " refers to unknown label: " + block[0]));
                            }
                        }
                    }
                    break;
                default:
                    jj_la1[36] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    public final String[] NamedJavaBlock(boolean checkLabel) throws ParseException {
        String[] block = new String[2];
        Token nameTok = null;
        jj_consume_token(colon);
        nameTok = jj_consume_token(ident);
        block[0] = nameTok.image;
        if (checkLabel && block[0] != null) if (!bindingNameSet.contains(block[0])) {
            {
                if (true) throw (new ParseException(errorMsgPrefix(nameTok) + "unknown label in RHS action: " + block[0]));
            }
        }
        jj_consume_token(leftBrace);
        block[1] = ConsumeBlock();
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    public final String[] AnonymousJavaBlock() throws ParseException {
        String[] block = new String[2];
        block[0] = null;
        jj_consume_token(leftBrace);
        block[1] = ConsumeBlock();
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    public final String[] AssignmentExpression(boolean checkLabel) throws ParseException {
        String[] block = new String[2];
        StringBuffer blockBuffer = new StringBuffer();
        Token nameTok = null;
        Token opTok = null;
        String newAnnotType = null;
        String newAttrName = null;
        String nl = Strings.getNl();
        String annotSetName = null;
        Pair attrVal = null;
        String existingAnnotSetName = null;
        String existingAnnotType = null;
        String existingAttrName = null;
        String opName = null;
        blockBuffer.append("// RHS assignment block" + nl);
        blockBuffer.append("      FeatureMap features = Factory.newFeatureMap();" + nl);
        switch(jj_nt.kind) {
            case colon:
                jj_consume_token(colon);
                break;
            case colonplus:
                jj_consume_token(colonplus);
                {
                    if (true) throw new ParseException(":+ not a legal operator (no multi-span annots)");
                }
                break;
            default:
                jj_la1[37] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        nameTok = jj_consume_token(ident);
        block[0] = nameTok.image;
        if (checkLabel && block[0] != null) if (!bindingNameSet.contains(block[0])) {
            {
                if (true) throw (new ParseException(errorMsgPrefix(nameTok) + "unknown label in RHS action: " + block[0]));
            }
        }
        annotSetName = block[0] + "Annots";
        jj_consume_token(period);
        nameTok = jj_consume_token(ident);
        newAnnotType = nameTok.image;
        blockBuffer.append("      Object val = null;" + nl);
        jj_consume_token(assign);
        jj_consume_token(leftBrace);
        label_14: while (true) {
            switch(jj_nt.kind) {
                case ident:
                    ;
                    break;
                default:
                    jj_la1[38] = jj_gen;
                    break label_14;
            }
            nameTok = jj_consume_token(ident);
            jj_consume_token(assign);
            newAttrName = nameTok.image;
            switch(jj_nt.kind) {
                case integer:
                case string:
                case bool:
                case ident:
                case floatingPoint:
                case leftSquare:
                    attrVal = AttrVal();
                    switch(((Integer) attrVal.first).intValue()) {
                        case string:
                            blockBuffer.append("      val = ");
                            appendJavaStringLiteral(blockBuffer, attrVal.second.toString());
                            blockBuffer.append(";" + nl);
                            break;
                        case integer:
                            blockBuffer.append("      try { " + "val = Long.valueOf(");
                            appendJavaStringLiteral(blockBuffer, attrVal.second.toString());
                            blockBuffer.append("); }" + nl + "      catch(NumberFormatException e) { }" + nl);
                            break;
                        case ident:
                            blockBuffer.append("      val = ");
                            appendJavaStringLiteral(blockBuffer, attrVal.second.toString());
                            blockBuffer.append(";" + nl);
                            break;
                        case bool:
                            blockBuffer.append("      val = Boolean.valueOf(");
                            appendJavaStringLiteral(blockBuffer, attrVal.second.toString());
                            blockBuffer.append(");" + nl);
                            break;
                        case floatingPoint:
                            blockBuffer.append("      try { " + "val = Double.valueOf(");
                            appendJavaStringLiteral(blockBuffer, attrVal.second.toString());
                            blockBuffer.append("); }" + nl + "      catch(NumberFormatException e) { }" + nl);
                            break;
                        default:
                            blockBuffer.append("      val = \"\";" + nl);
                            break;
                    }
                    blockBuffer.append("      features.put(");
                    appendJavaStringLiteral(blockBuffer, newAttrName);
                    blockBuffer.append(", val);");
                    blockBuffer.append(nl);
                    break;
                case colon:
                    jj_consume_token(colon);
                    nameTok = jj_consume_token(ident);
                    existingAnnotSetName = nameTok.image + "ExistingAnnots";
                    if (checkLabel && !bindingNameSet.contains(nameTok.image)) {
                        if (true) throw (new ParseException(errorMsgPrefix(nameTok) + "unknown label in RHS action(2): " + nameTok.image));
                    }
                    blockBuffer.append("      { // need a block for the existing annot set" + nl + "        gate.AnnotationSet " + existingAnnotSetName + " = (gate.AnnotationSet)bindings.get(");
                    appendJavaStringLiteral(blockBuffer, nameTok.image);
                    blockBuffer.append("); " + nl + "        Object existingFeatureValue;" + nl);
                    switch(jj_nt.kind) {
                        case period:
                            jj_consume_token(period);
                            nameTok = jj_consume_token(ident);
                            existingAnnotType = nameTok.image;
                            switch(jj_nt.kind) {
                                case period:
                                    opTok = jj_consume_token(period);
                                    break;
                                case metaPropOp:
                                    opTok = jj_consume_token(metaPropOp);
                                    break;
                                default:
                                    jj_la1[39] = jj_gen;
                                    jj_consume_token(-1);
                                    throw new ParseException();
                            }
                            nameTok = jj_consume_token(ident);
                            opName = opTok.image;
                            existingAttrName = nameTok.image;
                            blockBuffer.append("        if (" + existingAnnotSetName + " != null) {" + nl + "          gate.AnnotationSet existingAnnots = " + nl + "          " + existingAnnotSetName + ".get(");
                            appendJavaStringLiteral(blockBuffer, existingAnnotType);
                            blockBuffer.append(");" + nl + "          if (existingAnnots != null) {" + nl + "            java.util.Iterator iter = existingAnnots.iterator();" + nl + "            while(iter.hasNext()) {" + nl + "              gate.Annotation existingA = (gate.Annotation) iter.next();" + nl);
                            if (opName.equals("@") && (existingAttrName.equals("string") || existingAttrName.equals("cleanString") || existingAttrName.equals("length"))) {
                                blockBuffer.append("              int from = existingA.getStartNode().getOffset().intValue();" + nl + "              int to   = existingA.getEndNode().getOffset().intValue();" + nl + "              existingFeatureValue = doc.getContent().toString().substring(from,to);" + nl);
                                if (existingAttrName.equals("cleanString")) {
                                    blockBuffer.append("                 existingFeatureValue = gate.Utils.cleanString((String)existingFeatureValue);" + nl);
                                }
                                if (existingAttrName.equals("length")) {
                                    blockBuffer.append("                 existingFeatureValue = (long)to - (long)from;" + nl);
                                }
                            } else {
                                blockBuffer.append("existingFeatureValue = existingA.getFeatures().get(");
                                appendJavaStringLiteral(blockBuffer, existingAttrName);
                                blockBuffer.append(");" + nl);
                            }
                            blockBuffer.append("              if(existingFeatureValue != null) {" + nl + "                features.put(");
                            appendJavaStringLiteral(blockBuffer, newAttrName);
                            blockBuffer.append(", existingFeatureValue);" + nl + "                break;" + nl + "              }" + nl + "            } // while" + nl + "          } // if not null" + nl + "        } // if not null" + nl);
                            break;
                        case metaPropOp:
                            opTok = jj_consume_token(metaPropOp);
                            nameTok = jj_consume_token(ident);
                            opName = opTok.image;
                            existingAttrName = nameTok.image;
                            if (opName.equals("@") && (existingAttrName.equals("string") || existingAttrName.equals("cleanString") || existingAttrName.equals("length"))) {
                                blockBuffer.append("        if (" + existingAnnotSetName + " != null) {" + nl + "          int from = " + existingAnnotSetName + ".firstNode().getOffset().intValue();" + nl + "          int to   = " + existingAnnotSetName + ".lastNode().getOffset().intValue();" + nl + "          existingFeatureValue = doc.getContent().toString().substring(from,to);" + nl);
                                if (existingAttrName.equals("cleanString")) {
                                    blockBuffer.append("                 existingFeatureValue = ((String)existingFeatureValue).replaceAll(\"\\\\s+\", \" \").trim();" + nl);
                                }
                                if (existingAttrName.equals("length")) {
                                    blockBuffer.append("                 existingFeatureValue = (long)to - (long)from;" + nl);
                                }
                                blockBuffer.append("          if(existingFeatureValue != null) {" + nl + "            features.put(");
                                appendJavaStringLiteral(blockBuffer, newAttrName);
                                blockBuffer.append(", existingFeatureValue);" + nl + "          }" + nl + "        } // if not null" + nl);
                            } else {
                                {
                                    if (true) throw new ParseException(errorMsgPrefix(nameTok) + "Unsupported RHS meta-property " + nameTok.image);
                                }
                            }
                            break;
                        default:
                            jj_la1[40] = jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                    blockBuffer.append("      } // block for existing annots" + nl);
                    break;
                default:
                    jj_la1[41] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            switch(jj_nt.kind) {
                case comma:
                    jj_consume_token(comma);
                    break;
                default:
                    jj_la1[42] = jj_gen;
                    ;
            }
        }
        jj_consume_token(rightBrace);
        appendAnnotationAdd(blockBuffer, newAnnotType, annotSetName);
        block[1] = blockBuffer.toString();
        {
            if (true) return block;
        }
        throw new Error("Missing return statement in function");
    }

    void appendSpecials(Token tok, StringBuffer block) throws ParseException {
        if (tok != null) {
            appendSpecials(tok.specialToken, block);
            block.append(tok.image);
        }
    }

    String ConsumeBlock() throws ParseException, ParseException {
        StringBuffer block = new StringBuffer();
        int nesting = 1;
        Token nextTok = getNextToken();
        if (nextTok.kind == EOF) {
            throw new ParseException(errorMsgPrefix(nextTok) + "Unexpected EOF in Java block");
        }
        Token blockStart = nextTok;
        while (blockStart.specialToken != null) {
            blockStart = blockStart.specialToken;
        }
        block.append("  // JAPE Source: " + baseURL + ":" + blockStart.beginLine + "\n");
        while (nesting != 0) {
            appendSpecials(nextTok.specialToken, block);
            if (nextTok.image.equals("{")) {
                nesting++;
            } else if (nextTok.image.equals("}")) {
                nesting--;
            }
            if (nesting > 0) {
                if (nextTok.kind == string) {
                    appendJavaStringLiteral(block, nextTok.image.substring(1, nextTok.image.length() - 1));
                } else {
                    block.append(nextTok.image);
                }
            }
            if (nesting != 0) {
                nextTok = getNextToken();
                if (nextTok.kind == EOF) {
                    throw new ParseException(errorMsgPrefix(nextTok) + "Unexpected EOF in Java block");
                }
            }
        }
        return block.toString();
    }

    private boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_1();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(0, xla);
        }
    }

    private boolean jj_2_2(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_2();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(1, xla);
        }
    }

    private boolean jj_3R_27() {
        if (jj_scan_token(pling)) return true;
        return false;
    }

    private boolean jj_3R_26() {
        if (jj_3R_15()) return true;
        return false;
    }

    private boolean jj_3R_25() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_27()) jj_scanpos = xsp;
        if (jj_scan_token(ident)) return true;
        return false;
    }

    private boolean jj_3R_23() {
        if (jj_scan_token(string)) return true;
        return false;
    }

    private boolean jj_3R_21() {
        if (jj_scan_token(leftBracket)) return true;
        if (jj_3R_24()) return true;
        return false;
    }

    private boolean jj_3R_24() {
        Token xsp;
        if (jj_3R_26()) return true;
        while (true) {
            xsp = jj_scanpos;
            if (jj_3R_26()) {
                jj_scanpos = xsp;
                break;
            }
        }
        return false;
    }

    private boolean jj_3R_19() {
        if (jj_3R_21()) return true;
        return false;
    }

    private boolean jj_3R_16() {
        if (jj_scan_token(colon)) return true;
        if (jj_scan_token(ident)) return true;
        if (jj_scan_token(leftBrace)) return true;
        return false;
    }

    private boolean jj_3R_18() {
        if (jj_3R_20()) return true;
        return false;
    }

    private boolean jj_3R_17() {
        if (jj_scan_token(ident)) return true;
        return false;
    }

    private boolean jj_3_2() {
        if (jj_3R_16()) return true;
        return false;
    }

    private boolean jj_3R_22() {
        if (jj_scan_token(leftBrace)) return true;
        if (jj_3R_25()) return true;
        return false;
    }

    private boolean jj_3R_15() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_17()) {
            jj_scanpos = xsp;
            if (jj_3R_18()) {
                jj_scanpos = xsp;
                if (jj_3R_19()) return true;
            }
        }
        return false;
    }

    private boolean jj_3R_20() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_22()) {
            jj_scanpos = xsp;
            if (jj_3R_23()) return true;
        }
        return false;
    }

    private boolean jj_3_1() {
        if (jj_3R_15()) return true;
        return false;
    }

    /** Generated Token Manager. */
    public ParseCpslTokenManager token_source;

    SimpleCharStream jj_input_stream;

    /** Current token. */
    public Token token;

    /** Next token. */
    public Token jj_nt;

    private Token jj_scanpos, jj_lastpos;

    private int jj_la;

    private int jj_gen;

    private final int[] jj_la1 = new int[43];

    private static int[] jj_la1_0;

    private static int[] jj_la1_1;

    private static int[] jj_la1_2;

    static {
        jj_la1_init_0();
        jj_la1_init_1();
        jj_la1_init_2();
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[] { 0x800, 0xe00000, 0xe00000, 0x1000000, 0x2000, 0x1f01000, 0x6000000, 0x0, 0x0, 0x0, 0x6000000, 0x38000000, 0x38000000, 0x100000, 0x40000000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x80000000, 0x80000000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };
    }

    private static void jj_la1_init_1() {
        jj_la1_1 = new int[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x20000, 0x20000, 0x30000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2120000, 0xa028000, 0x800000, 0xa028000, 0xa028000, 0x1000000, 0x2008000, 0x20000001, 0x20008, 0x100000, 0x1000000, 0x20000001, 0x0, 0x2020000, 0x420004, 0x420004, 0x78008, 0x20078008, 0x20000, 0x1000000, 0x1000000, 0x2120000, 0x100000, 0x20000, 0x400004, 0x400004, 0x20178008, 0x1000000 };
    }

    private static void jj_la1_init_2() {
        jj_la1_2 = new int[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1, 0x1, 0x0, 0x0, 0x0, 0x0, 0x0 };
    }

    private final JJCalls[] jj_2_rtns = new JJCalls[2];

    private boolean jj_rescan = false;

    private int jj_gc = 0;

    /** Constructor with InputStream. */
    public ParseCpsl(java.io.InputStream stream) {
        this(stream, null);
    }

    /** Constructor with InputStream and supplied encoding */
    public ParseCpsl(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new ParseCpslTokenManager(jj_input_stream);
        token = new Token();
        token.next = jj_nt = token_source.getNextToken();
        jj_gen = 0;
        for (int i = 0; i < 43; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Reinitialise. */
    public void ReInit(java.io.InputStream stream) {
        ReInit(stream, null);
    }

    /** Reinitialise. */
    public void ReInit(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream.ReInit(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        token.next = jj_nt = token_source.getNextToken();
        jj_gen = 0;
        for (int i = 0; i < 43; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Constructor. */
    public ParseCpsl(java.io.Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new ParseCpslTokenManager(jj_input_stream);
        token = new Token();
        token.next = jj_nt = token_source.getNextToken();
        jj_gen = 0;
        for (int i = 0; i < 43; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Reinitialise. */
    public void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        token.next = jj_nt = token_source.getNextToken();
        jj_gen = 0;
        for (int i = 0; i < 43; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Constructor with generated Token Manager. */
    public ParseCpsl(ParseCpslTokenManager tm) {
        token_source = tm;
        token = new Token();
        token.next = jj_nt = token_source.getNextToken();
        jj_gen = 0;
        for (int i = 0; i < 43; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Reinitialise. */
    public void ReInit(ParseCpslTokenManager tm) {
        token_source = tm;
        token = new Token();
        token.next = jj_nt = token_source.getNextToken();
        jj_gen = 0;
        for (int i = 0; i < 43; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken = token;
        if ((token = jj_nt).next != null) jj_nt = jj_nt.next; else jj_nt = jj_nt.next = token_source.getNextToken();
        if (token.kind == kind) {
            jj_gen++;
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen) c.first = null;
                        c = c.next;
                    }
                }
            }
            return token;
        }
        jj_nt = token;
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    private static final class LookaheadSuccess extends java.lang.Error {
    }

    private final LookaheadSuccess jj_ls = new LookaheadSuccess();

    private boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            } else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        } else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) jj_add_error_token(kind, i);
        }
        if (jj_scanpos.kind != kind) return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
        return false;
    }

    /** Get the next Token. */
    public final Token getNextToken() {
        if ((token = jj_nt).next != null) jj_nt = jj_nt.next; else jj_nt = jj_nt.next = token_source.getNextToken();
        jj_gen++;
        return token;
    }

    /** Get the specific Token. */
    public final Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) t = t.next; else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();

    private int[] jj_expentry;

    private int jj_kind = -1;

    private int[] jj_lasttokens = new int[100];

    private int jj_endpos;

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) return;
        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        } else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];
            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }
            jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext(); ) {
                int[] oldentry = (int[]) (it.next());
                if (oldentry.length == jj_expentry.length) {
                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            continue jj_entries_loop;
                        }
                    }
                    jj_expentries.add(jj_expentry);
                    break jj_entries_loop;
                }
            }
            if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
        }
    }

    /** Generate ParseException. */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[73];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 43; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                    if ((jj_la1_2[i] & (1 << j)) != 0) {
                        la1tokens[64 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 73; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /** Enable tracing. */
    public final void enable_tracing() {
    }

    /** Disable tracing. */
    public final void disable_tracing() {
    }

    private void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 2; i++) {
            try {
                JJCalls p = jj_2_rtns[i];
                do {
                    if (p.gen > jj_gen) {
                        jj_la = p.arg;
                        jj_lastpos = jj_scanpos = p.first;
                        switch(i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);
            } catch (LookaheadSuccess ls) {
            }
        }
        jj_rescan = false;
    }

    private void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }

    static final class JJCalls {

        int gen;

        Token first;

        int arg;

        JJCalls next;
    }
}
