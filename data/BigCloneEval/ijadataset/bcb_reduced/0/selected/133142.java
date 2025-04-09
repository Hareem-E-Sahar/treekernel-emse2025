package org.fressia.tap.parser;

import org.fressia.tap.parser.entries.BailOutLine;
import org.fressia.tap.parser.entries.DiagnosticLine;
import org.fressia.tap.parser.entries.Directive;
import org.fressia.tap.parser.entries.PlanLine;
import org.fressia.tap.parser.entries.TapLine;
import org.fressia.tap.parser.entries.TestLine;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse
 * <a href="http://testanything.org/wiki/index.php/Main_Page" target="_blank">Test Anything Protocol</a>
 * input. Line syntax is checked with regular expressions and global
 * syntax is checked using a finite sate machine ({@link GlobalSyntaxChecker}).
 *
 * @author Alvaro Egana
 *
 */
public class TapParser {

    private static final String SKIP = "((s|S)(k|K)(i|I)(p|P))";

    private static final String TODO = "((t|T)(o|O)(d|D)(o|O))";

    private static final String OK = "((o|O)(k|K))";

    private static final String NOT = "((n|N)(o|O)(t|T))";

    private static final String BAIL_OUT = "((b|B)(a|A)(i|I)(l|L)\\p{Space}*(o|O)(u|U)(t|T)!)";

    private static final String RANGE_SEP = "\\.\\.";

    private static final String SHARP = "\\p{Space}*#\\p{Space}*";

    private static final String TEXT = "\\p{Print}*";

    private static final String SKIP_OR_TODO = "(" + SKIP + "|" + TODO + ")";

    private static final String DIRECTIVE = "(" + SHARP + SKIP_OR_TODO + "\\p{Space}*" + TEXT + ")";

    private static final String SKIP_DIRECTIVE = "(" + SHARP + SKIP + "\\p{Space}*" + TEXT + ")";

    private static final String DESCRIPTION = "(\\p{Space}*" + TEXT + ")";

    private static final String DIGITS_RANGE = "\\p{Space}*\\d+\\p{Space}*" + RANGE_SEP + "\\p{Space}*\\d+";

    private static final String TEST_STATUS = NOT + "?\\p{Space}*" + OK + "\\p{Space}*";

    private static final String TEST_NUMBER = "(\\d+\\p{Space}*)";

    private static final String PLAN_LINE = DIGITS_RANGE + SKIP_DIRECTIVE + "?";

    private static final String TEST_LINE = TEST_STATUS + TEST_NUMBER + "?" + DESCRIPTION + "?" + DIRECTIVE + "?";

    private static final String DIAGNOSTIC_LINE = SHARP + TEXT + "?";

    private static final String BAIL_OUT_LINE = BAIL_OUT + TEXT + "?";

    /** The reader used to go around the input */
    private BufferedReader reader;

    private boolean isStrictLineParsing = true;

    private int lineNumber = 0;

    private GlobalSyntaxChecker gsChecker;

    @SuppressWarnings("unchecked")
    private TapStreamParserVisitor visitor;

    /**
     * Builds the parser starting from a String
     * input.
     *
     * @param input The tap source.
     * @param isStrictParsing If <code>true</code> the input <u>must</u>
     * consist of TAP lines <u>only</u>. If <code>false</code> the input can contain
     * non-TAP lines - that will be discarded in order to parse TAP lines correctly.
     */
    public TapParser(String input, boolean isStrictParsing) {
        byte[] inputArray = null;
        try {
            inputArray = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            inputArray = input.getBytes();
        }
        init(new ByteArrayInputStream(inputArray), isStrictParsing);
    }

    /**
     * Builds the parser starting from an
     * input stream.
     *
     * @param input The tap source.
     * @param isStrictParsing If <code>true</code> the input <u>must</u>
     * consist of TAP lines <u>only</u>. If <code>false</code> the input can contain
     * non-TAP lines - that will be discarded in order to parse TAP lines correctly.
     */
    public TapParser(InputStream stream, boolean isStrictParsing) {
        init(stream, isStrictParsing);
    }

    private void init(InputStream stream, boolean isStrictParsing) {
        InputStreamReader isr = new InputStreamReader(stream);
        reader = new BufferedReader(isr);
        this.isStrictLineParsing = isStrictParsing;
        gsChecker = new GlobalSyntaxChecker();
    }

    /**
     * Sets the the stats collector
     * @param statsCollector
     */
    public void setStatsCollector(TapStatsCollector statsCollector) {
        gsChecker.setStatsCollector(statsCollector);
    }

    /** Gets the current line */
    @SuppressWarnings("unchecked")
    public TapStreamParserVisitor getVisitor() {
        return visitor;
    }

    /**
     * Sets this parser the visitor.
     *
     * @param visitor
     */
    @SuppressWarnings("unchecked")
    public void setVisitor(TapStreamParserVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Processes the tap input.
     *
     * @throws LineSyntaxException
     * @throws GoblalSyntaxException
     * @throws BailOutReachedException
     * @throws SkippingAllTestsException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void process() throws LineSyntaxException, GoblalSyntaxException, BailOutReachedException, SkippingAllTestsException, IOException {
        TapLine tapLine = null;
        try {
            while ((tapLine = readLine()) != null) {
                gsChecker.addLine(tapLine, lineNumber);
                if (visitor != null) {
                    visitor.visit(tapLine);
                }
            }
            gsChecker.addLine(null, lineNumber);
        } catch (LineSyntaxException e) {
            visitAndThrowException(e.getMessage(), LineSyntaxException.class, tapLine);
        } catch (GoblalSyntaxException e) {
            visitAndThrowException(e.getMessage(), GoblalSyntaxException.class, tapLine);
        } catch (BailOutReachedException e) {
            visitAndThrowException(e.getMessage(), BailOutReachedException.class, tapLine);
        } catch (SkippingAllTestsException e) {
            visitAndThrowException(e.getMessage(), SkippingAllTestsException.class, tapLine);
        } catch (IOException e) {
            visitAndThrowException(e.getMessage(), IOException.class, tapLine);
        }
    }

    private TapLine readLine() throws IOException, LineSyntaxException {
        String line = reader.readLine();
        lineNumber++;
        try {
            return (line == null) ? null : parse(line);
        } catch (LineSyntaxException e) {
            if (isStrictLineParsing) {
                throw e;
            } else {
                if (visitor != null) {
                    visitor.visit("DISCARDED: " + line + " --> " + e.getMessage());
                }
                TapLine discarded = new TapLine();
                discarded.setDiscarded(true);
                return discarded;
            }
        }
    }

    private TapLine parse(String line) throws LineSyntaxException {
        if (line.matches(TEST_LINE)) {
            return buildTestLine(line);
        }
        if (line.matches(DIAGNOSTIC_LINE)) {
            return buildDiagnosticLine(line);
        }
        if (line.matches(PLAN_LINE)) {
            return buildPlanLine(line);
        }
        if (line.matches(BAIL_OUT_LINE)) {
            return buildBailOutLine(line);
        }
        throwException(lineNumber, "'" + line + "' is not a valid TAP line.", LineSyntaxException.class);
        return null;
    }

    private String extract(String str, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        boolean found = matcher.find();
        return found ? matcher.group() : "";
    }

    private int lastIndexOf(String str, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        boolean found = matcher.find();
        return found ? matcher.end() : (-1);
    }

    @SuppressWarnings("unused")
    private int indexOf(String str, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        boolean found = matcher.find();
        return found ? matcher.start() : (-1);
    }

    private int[] boundsOf(String str, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        boolean found = matcher.find();
        if (found) {
            return new int[] { matcher.start(), matcher.end() };
        } else {
            return new int[0];
        }
    }

    private PlanLine buildPlanLine(String strLine) throws LineSyntaxException {
        String range = extract(strLine, DIGITS_RANGE);
        String[] digits = range.split(RANGE_SEP);
        int start = Integer.valueOf(digits[0].trim());
        int end = Integer.valueOf(digits[1].trim());
        if (end < start) {
            throwException(lineNumber, "'" + range + "' is not a valid range.", LineSyntaxException.class);
        }
        PlanLine line = new PlanLine(start, end);
        String dir = extract(strLine, DIRECTIVE);
        if (!"".equals(dir)) {
            line.setDirective(buildDirective(dir));
        }
        return line;
    }

    private TestLine buildTestLine(String strLine) {
        int ls = lastIndexOf(strLine, TEST_STATUS);
        String body = strLine.substring(ls).trim();
        int[] bounds = boundsOf(body, TEST_NUMBER);
        boolean hasNumber;
        if (bounds.length < 2) {
            hasNumber = false;
        } else {
            hasNumber = bounds[0] == 0;
        }
        int testNumber;
        if (hasNumber) {
            testNumber = Integer.valueOf(extract(body.substring(bounds[0], bounds[1]), "\\d+").trim());
            body = body.substring(bounds[1]);
        } else {
            testNumber = TestLine.NO_TEST_NUMBER;
        }
        TestLine line = new TestLine(strLine.substring(0, ls).trim(), testNumber);
        bounds = boundsOf(body, DIRECTIVE);
        if (bounds.length == 2) {
            line.setDirective(buildDirective(body.substring(bounds[0], bounds[1])));
            body = body.substring(0, bounds[0]);
        }
        line.setDescription(body);
        return line;
    }

    private DiagnosticLine buildDiagnosticLine(String strLine) {
        int ls = lastIndexOf(strLine, SHARP);
        return new DiagnosticLine(strLine.substring(ls));
    }

    private BailOutLine buildBailOutLine(String strLine) {
        int ls = lastIndexOf(strLine, BAIL_OUT);
        return new BailOutLine(strLine.substring(ls));
    }

    private Directive buildDirective(String directive) {
        int[] bounds = boundsOf(directive, SKIP_OR_TODO);
        return new Directive(directive.substring(bounds[0], bounds[1]).trim(), directive.substring(bounds[1]).trim());
    }

    private static final <T extends Throwable> void throwException(int lineNumber, String msg, Class<T> type) throws T {
        throw getThrowable(type, getNumberedMsg(msg, lineNumber));
    }

    @SuppressWarnings("unchecked")
    private <T extends Exception> void visitAndThrowException(String msg, Class<T> type, TapLine tapLine) throws T {
        if (visitor != null) {
            visitor.visit(tapLine);
        }
        throw getThrowable(type, msg);
    }

    private static <T extends Throwable> T getThrowable(Class<T> type, String msg) {
        Constructor<T> cons = null;
        T ex = null;
        try {
            cons = type.getConstructor(new Class[] { String.class });
            ex = cons.newInstance(new Object[] { msg });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ex;
    }

    private static String getNumberedMsg(String msg, int lineNumber) {
        return "Line " + lineNumber + ": " + msg;
    }

    /**
     * Finite state machine to the check global syntax of a
     * <a href="http://testanything.org/wiki/index.php/Main_Page" target="_blank">Test Anything Protocol</a>
     * input.
     *
     * @author Alvaro Egana
     *
     */
    private class GlobalSyntaxChecker {

        private static final int INITIAL_PLAN = 1;

        private static final int TESTS = 2;

        private static final int TESTS_FIRST = 3;

        private static final int FINAL_PLAN = 4;

        private static final int FINAL = 5;

        private int currentState = INITIAL_PLAN;

        private int planStart;

        private int planEnd;

        private int currentTestNumber;

        private int checkedTestsNumber = 0;

        private int lineNumber;

        private TapStatsCollector statsCollector;

        /**
         * Sets the stats collector.
         *
         * @param statsCollector
         */
        public void setStatsCollector(TapStatsCollector statsCollector) {
            this.statsCollector = statsCollector;
        }

        /**
         * Gets the stats collector.
         *
         * @return
         */
        public TapStatsCollector getStatsCollector() {
            return statsCollector;
        }

        /**
         * Adds a new line to be checked regarding to the
         * global syntax.
         *
         * @param line The line.
         * @param lineNumber The line number.
         * @throws GoblalSyntaxException
         * @throws BailOutReachedException
         * @throws SkippingAllTestsException If the 'plan' line contains
         * a skip-all-tests directive.
         */
        public void addLine(TapLine line, int lineNumber) throws GoblalSyntaxException, BailOutReachedException, SkippingAllTestsException {
            this.lineNumber = lineNumber;
            makeTransition(line);
        }

        private void makeTransition(TapLine line) throws GoblalSyntaxException, BailOutReachedException, SkippingAllTestsException {
            switch(currentState) {
                case INITIAL_PLAN:
                    if (line == null) {
                        throwException(lineNumber, "Unexpected EOF received.", GoblalSyntaxException.class);
                    }
                    if (line instanceof PlanLine) {
                        currentState = TESTS;
                        planStart = ((PlanLine) line).getStart();
                        planEnd = ((PlanLine) line).getEnd();
                        currentTestNumber = ((PlanLine) line).getStart() - 1;
                        collect(line);
                        if (((PlanLine) line).hasDirective()) {
                            Directive dir = ((PlanLine) line).getDirective();
                            String text = dir.getText();
                            if (Directive.SKIP.equals(dir.getName())) {
                                if (statsCollector != null) {
                                    statsCollector.setSkippedTests(planEnd - planStart + 1);
                                }
                                throwException(lineNumber, "'Skip' plan directive received" + ((!"".equals(text)) ? (": " + text) : ""), SkippingAllTestsException.class);
                            }
                        }
                    }
                    if (line instanceof TestLine) {
                        checkedTestsNumber++;
                        currentTestNumber = 1;
                        planStart = 1;
                        if (((TestLine) line).getNumber() == TestLine.NO_TEST_NUMBER) {
                            ((TestLine) line).setNumber(currentTestNumber);
                        }
                        currentState = TESTS_FIRST;
                        collect(line);
                    }
                    if (line instanceof BailOutLine) {
                        throwBailOutException((BailOutLine) line);
                    }
                    break;
                case TESTS:
                    if (line == null) {
                        checkPlan();
                        currentState = FINAL;
                    }
                    if (line instanceof TestLine) {
                        checkedTestsNumber++;
                        checkTestNumber(line);
                        collect(line);
                    }
                    if (line instanceof BailOutLine) {
                        throwBailOutException((BailOutLine) line);
                    }
                    if (line instanceof PlanLine) {
                        throwException(lineNumber, "Another test plan line received after initial plan.", GoblalSyntaxException.class);
                    }
                    break;
                case TESTS_FIRST:
                    if (line == null) {
                        throwException(lineNumber, "Unexpected EOF received.", GoblalSyntaxException.class);
                    }
                    if (line instanceof TestLine) {
                        checkedTestsNumber++;
                        checkTestNumber(line);
                        collect(line);
                    }
                    if (line instanceof PlanLine) {
                        currentState = FINAL_PLAN;
                        planStart = ((PlanLine) line).getStart();
                        planEnd = ((PlanLine) line).getEnd();
                        collect(line);
                    }
                    if (line instanceof BailOutLine) {
                        throwBailOutException((BailOutLine) line);
                    }
                    break;
                case FINAL_PLAN:
                    if (line == null) {
                        checkPlan();
                        currentState = FINAL;
                    }
                    if (line instanceof PlanLine) {
                        throwException(lineNumber, "Another test plan line received after final plan.", GoblalSyntaxException.class);
                    }
                    if (line instanceof TestLine) {
                        throwException(lineNumber, "Test line received after final plan.", GoblalSyntaxException.class);
                    }
                    if (line instanceof BailOutLine) {
                        throwException(lineNumber, "Bail out line received after final plan.", GoblalSyntaxException.class);
                    }
                    break;
            }
        }

        private void checkPlan() throws GoblalSyntaxException {
            if (checkedTestsNumber != (planEnd - planStart + 1)) {
                throwException(lineNumber, "Plan was '" + planStart + ".." + planEnd + "' but checked " + checkedTestsNumber + ".", GoblalSyntaxException.class);
            }
        }

        private void checkTestNumber(TapLine line) throws GoblalSyntaxException {
            if (((TestLine) line).getNumber() == TestLine.NO_TEST_NUMBER) {
                ((TestLine) line).setNumber(++currentTestNumber);
            }
            currentTestNumber = ((TestLine) line).getNumber();
            if ((currentTestNumber - planStart + 1) != checkedTestsNumber) {
                throwException(lineNumber, "Wrong test number (" + currentTestNumber + "). " + "It should be " + ((checkedTestsNumber + planStart) - 1 + "."), GoblalSyntaxException.class);
            }
        }

        public <T extends TapLine> void collect(T line) {
            if (statsCollector != null) {
                statsCollector.collect(line);
            }
        }

        private void throwBailOutException(BailOutLine line) throws BailOutReachedException {
            String msg = ((BailOutLine) line).getBailOutMessage();
            throwException(lineNumber, "Bail out reached" + ((!"".equals(msg)) ? (": " + msg) : ""), BailOutReachedException.class);
        }
    }
}
