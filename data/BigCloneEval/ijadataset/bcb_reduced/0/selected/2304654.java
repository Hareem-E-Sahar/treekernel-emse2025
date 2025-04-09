package fitlibrary;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fit.Fixture;
import fit.Parse;
import fit.exception.AmbiguousActionException;
import fit.exception.ExtraCellsFailureException;
import fit.exception.FitFailureException;
import fit.exception.FitFailureExceptionWithHelp;
import fit.exception.IgnoredException;
import fit.exception.MissingCellsFailureException;
import fit.exception.MissingMethodException;
import fitlibrary.closure.MethodClosure;
import fitlibrary.closure.MethodTarget;
import fitlibrary.graphic.GraphicValueAdapter;
import fitlibrary.graphic.ObjectDotGraphic;
import fitlibrary.log.Logging;
import fitlibrary.valueAdapter.ValueAdapter;

/** An alternative to fit.ActionFixture
	@author rick mugridge, july 2003
	Updated April 2004 to include not/reject actions.
	Updated August 2004 to handle properties and
	  to automap Object to DoFixture, List, etc to ArrayFixture, etc.
  * 
  * See the FixtureFixture specifications for examples
*/
public class DoFixture extends FlowFixture {

    protected Map map = new HashMap();

    private boolean gatherExpectedForGeneration = false;

    private Object expectedResult = new Boolean(true);

    private SetUpFixture setUpFixture = null;

    private boolean settingUp = true;

    public DoFixture() {
    }

    public DoFixture(Object sut) {
        setSystemUnderTest(sut);
    }

    /** Check that the result of the action in the rest of the row matches
	 *  the expected value in the last cell of the row.
	 */
    public void check(Parse cells) throws Exception {
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureCheck");
        int argCount = cells.size() - 2;
        Parse expectedCell = cells.at(argCount + 1);
        MethodTarget target = findMethodByActionName(cells, argCount);
        if (gatherExpectedForGeneration) expectedResult = target.getResult(expectedCell);
        target.invokeAndCheck(cells.more, expectedCell);
    }

    protected Object getExpectedResult() {
        return expectedResult;
    }

    /** Add a cell containing the result of the rest of the row.
	 *  HTML is not altered, so it can be viewed directly.
	 */
    public void show(Parse cells) throws Exception {
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureShow");
        int argCount = cells.size() - 1;
        MethodTarget target = findMethodByActionName(cells, argCount);
        try {
            Object result = callGivenMethod(target, cells);
            addCell(cells, target.getResultString(result));
        } catch (IgnoredException e) {
        }
    }

    /** Add a cell containing the result of the rest of the row,
	 *  shown as a Dot graphic.
	 */
    public void showDot(Parse cells) throws Exception {
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureShowDot");
        int argCount = cells.size() - 1;
        MethodTarget target = findMethodByActionName(cells, argCount);
        ValueAdapter adapter = new GraphicValueAdapter(ObjectDotGraphic.class, null, null);
        try {
            Object result = callGivenMethod(target, cells);
            addCell(cells, adapter.toString(new ObjectDotGraphic(result)));
        } catch (IgnoredException e) {
        }
    }

    private void addCell(Parse cells, Object result) {
        cells.last().more = new Parse("td", result.toString(), null, null);
    }

    /** Checks that the action in the rest of the row succeeds.
	 *  o If a boolean is returned, it must be true.
	 *  o For other result types, no exception should be thrown.
	 */
    public void ensure(Parse cells) throws Exception {
        Parse ensureCell = cells;
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureEnsure");
        expectedResult = new Boolean(true);
        MethodTarget target = findMethodByActionName(cells, cells.size() - 1);
        try {
            Object result = callGivenMethod(target, cells);
            if (((Boolean) result).booleanValue()) right(ensureCell); else wrong(ensureCell);
        } catch (IgnoredException e) {
        } catch (Exception e) {
            wrong(ensureCell);
        }
    }

    /** Checks that the action in the rest of the row fails.
	 *  o If a boolean is returned, it must be false.
	 *  o For other result types, an exception should be thrown.
	 */
    public void reject(Parse cells) throws Exception {
        not(cells);
    }

    /** Same as reject()
	 */
    public void not(Parse cells) throws Exception {
        Parse notCell = cells;
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureNot");
        expectedResult = new Boolean(false);
        MethodTarget target = findMethodByActionName(cells, cells.size() - 1);
        try {
            Object result = callGivenMethod(target, cells);
            if (!(result instanceof Boolean)) exception(notCell, "Was not rejected"); else if (((Boolean) result).booleanValue()) wrong(notCell); else right(notCell);
        } catch (IgnoredException e) {
        } catch (Exception e) {
            right(notCell);
        }
    }

    /** The rest of the row is ignored. 
	 */
    public void note(Parse cells) throws Exception {
    }

    /** The rest of the table is ignored (and not coloured)
	 */
    public CommentFixture comment(Parse cells) {
        return new CommentFixture();
    }

    /** The rest of the table is ignored (and is coloured as ignored)
	 */
    public Fixture ignored(Parse cells) {
        return new Fixture();
    }

    /** An experimental feature, which may be changed or removed. */
    public void name(Parse cells) throws Exception {
        cells = cells.more;
        if (cells == null || cells.more == null) throw new MissingCellsFailureException("DoFixtureName");
        String name = cells.text();
        Parse methodCells = cells.more;
        int argCount = methodCells.size() - 1;
        MethodTarget target = findMethodByActionName(methodCells, argCount);
        Object result = target.invokeAndWrap(methodCells.more);
        if (result instanceof Fixture) {
            map.put(name, result);
            right(cells);
        } else throw new FitFailureException("Must return an object.");
    }

    /** An experimental feature, which may be changed or removed. */
    public Fixture use(Parse cells) {
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureUse");
        String name = cells.text();
        Object object = getMapper(cells.more).map.get(name);
        if (object instanceof Fixture) return (Fixture) object;
        throw new FitFailureException("Unknown name: " + name);
    }

    private DoFixture getMapper(Parse cells) {
        if (cells == null) return this;
        if (!cells.text().equals("of")) throw new FitFailureException("Missing 'of'.");
        if (cells.more != null) {
            cells = cells.more;
            String name = cells.text();
            Object object = getMapper(cells.more).map.get(name);
            if (object instanceof DoFixture) return (DoFixture) object;
            throw new FitFailureException("Unknown name: " + name);
        }
        throw new FitFailureException("Missing name.");
    }

    /** To allow for DoFixture to be used without writing any fixtures.
	 */
    public void start(Parse cells) {
        cells = cells.more;
        if (cells == null) throw new MissingCellsFailureException("DoFixtureStart");
        if (cells.more != null) throw new ExtraCellsFailureException("DoFixtureStart");
        String className = cells.text();
        try {
            setSystemUnderTest(Class.forName(className).newInstance());
        } catch (Exception e) {
            throw new FitFailureExceptionWithHelp("Unknown class: " + className, "UnknownClass.DoFixtureStart");
        }
    }

    /** To allow for a CalculateFixture to be used for the rest of the table.
	 *  This is intended for use for teaching, where no fixtures need to be
	 *  written.
	 */
    public Fixture calculate(Parse cells) {
        if (systemUnderTest == null) return new CalculateFixture(this);
        return new CalculateFixture(systemUnderTest);
    }

    protected Object interpretCells(final Parse cells, Fixture namedFixture) {
        expectedResult = new Boolean(true);
        String methodName = cells.text();
        try {
            Method parseMethod = switchSetUp().parseMethod(cells);
            checkForAmbiguity(methodName, namedFixture, parseMethod, null);
            try {
                MethodTarget target = switchSetUp().findMethodByActionName(cells, cells.size() - 1);
                checkForAmbiguity(methodName, namedFixture, parseMethod, target);
                Object result = target.invokeAndWrap(cells.more);
                if (result instanceof Boolean) target.color(cells, ((Boolean) result).booleanValue());
                return result;
            } catch (MissingMethodException e) {
                if (parseMethod == null && namedFixture == null) throw e;
                if (namedFixture != null) return namedFixture;
                return switchSetUp().callParseMethod(parseMethod, cells);
            }
        } catch (IgnoredException ex) {
        } catch (Exception ex) {
            exception(cells, ex);
        }
        return null;
    }

    private void checkForAmbiguity(String methodName, Fixture namedFixture, Method parseMethod, MethodTarget target) {
        String methodDetails = "method " + methodName + "()";
        String parseMethodDetails = "method " + methodName + "(Parse)";
        String fixtureDetails = methodName + "-Fixture";
        if (target != null && parseMethod != null) throw new AmbiguousActionException(methodDetails, parseMethodDetails);
        if (target != null && namedFixture != null) throw new AmbiguousActionException(methodDetails, fixtureDetails);
        if (parseMethod != null && namedFixture != null) throw new AmbiguousActionException(parseMethodDetails, fixtureDetails);
    }

    private DoFixture switchSetUp() {
        if (settingUp && setUpFixture != null) return setUpFixture;
        return this;
    }

    protected Object calledParseMethod(final Parse cells) throws Exception {
        try {
            String name = cells.text().trim();
            if (name.equals("")) return null;
            name = camel(name);
            Method parseMethod = getClass().getMethod(name, new Class[] { Parse.class });
            MethodClosure closure = new MethodClosure(this, parseMethod);
            MethodTarget target = new MethodTarget(closure, this);
            Object result = target.invoke(new Object[] { cells });
            if (result == null) result = "";
            return result;
        } catch (NoSuchMethodException ex) {
        }
        return null;
    }

    private Method parseMethod(final Parse cells) {
        try {
            String name = cells.text().trim();
            if (name.equals("")) return null;
            return getClass().getMethod(camel(name), new Class[] { Parse.class });
        } catch (NoSuchMethodException ex) {
        }
        return null;
    }

    private Object callParseMethod(Method parseMethod, final Parse cells) throws Exception {
        MethodClosure closure = new MethodClosure(this, parseMethod);
        MethodTarget target = new MethodTarget(closure, this);
        return target.invoke(new Object[] { cells });
    }

    /** Is overridden in subclass SequenceFixture to process arguments differently
	 */
    protected MethodTarget findMethodByActionName(Parse cells, int allArgs) throws Exception {
        int parms = allArgs / 2 + 1;
        int argCount = (allArgs + 1) / 2;
        String name = cells.text();
        for (int i = 1; i < parms; i++) name += " " + cells.at(i * 2).text();
        MethodTarget target = findMethod(ExtendedCamelCase.camel(name), argCount);
        target.setEverySecond(true);
        return target;
    }

    protected MethodTarget findMethod(String name, int argCount) {
        String[] args = new String[argCount];
        for (int i = 0; i < argCount; i++) args[i] = "arg" + (i + 1);
        return findMethod(name, args, "TypeOfResult");
    }

    protected MethodTarget findMethod(String name, List args) {
        return findMethod(name, args, "TypeOfResult");
    }

    protected MethodTarget findMethod(String name, List args, String returnType) {
        String[] arguments = new String[args.size()];
        for (int i = 0; i < arguments.length; i++) arguments[i] = (String) args.get(i);
        return findMethod(name, arguments, returnType);
    }

    private MethodTarget findMethod(String name, String[] args, String returnType) {
        String signature = "";
        if (args.length == 0) signature = "public " + returnType + " get" + name.substring(0, 1).toUpperCase() + name.substring(1) + "() { } OR: ";
        signature += "public " + returnType + " " + name + "(";
        for (int i = 0; i < args.length; i++) {
            if (i > 0) signature += ", ";
            signature += "Type" + (i + 1) + " " + args[i];
        }
        return findMethod(name, args.length, signature + ") { }");
    }

    private MethodTarget findMethod(String name, int argCount, String signature) {
        MethodTarget result = MethodTarget.findSpecificMethod(name, argCount, this, this);
        if (result != null) return result;
        Object target = systemUnderTest;
        while (target != null) {
            result = MethodTarget.findSpecificMethod(name, argCount, target, this);
            if (result != null) return result;
            if (target instanceof DoFixture) target = ((DoFixture) target).systemUnderTest; else target = null;
        }
        throw new MissingMethodException(this, signature, "DoFixture");
    }

    protected Object callGivenMethod(MethodTarget target, final Parse rowCells) throws Exception {
        return target.invoke(rowCells.more);
    }

    protected void tearDown() throws Exception {
        systemUnderTest = null;
    }

    protected void setGatherExpectedForGeneration(boolean gatherExpectedForGeneration) {
        this.gatherExpectedForGeneration = gatherExpectedForGeneration;
    }

    public void setSetUpFixture(SetUpFixture setUpFixture) {
        this.setUpFixture = setUpFixture;
        setUpFixture.setOuterContext(this);
    }

    void finishSettingUp() {
        Logging.log("Finish setting up class " + getClass().getName());
        this.settingUp = false;
    }
}
