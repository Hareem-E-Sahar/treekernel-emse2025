package abbot.tester.swt;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import abbot.Log;
import abbot.finder.matchers.swt.ClassMatcher;
import abbot.finder.matchers.swt.CompositeMatcher;
import abbot.finder.matchers.swt.TextMatcher;
import abbot.finder.swt.BasicFinder;
import abbot.finder.swt.Matcher;
import abbot.finder.swt.MultipleWidgetsFoundException;
import abbot.finder.swt.WidgetFinder;
import abbot.finder.swt.WidgetNotFoundException;
import abbot.script.Condition;
import abbot.swt.utilities.ExceptionHelper;
import abbot.swt.utilities.ScreenCapture;

/** An abstract class to facilitate the testing of dialogs in eclipse.
 * 
 *  Currently, this class only supports dialogs which have a title(Shell.getText()).
 *   
 *  protected abstract void invokeDialog() throws Throwable;
 *  protected abstract void doTestDialog() throws Throwable;
 *  protected abstract void doCloseDialog( boolean ok ) throws Throwable;
 *  
 *  Nesting of AbstractDialogTesters IS supported. All dialogs should
 *  be launched from their respective invokeDialog methods.
 *  e.g. 
 *  
 *  AbstractDialogTester one = AbstractDialogTester(firstTitle,_display) {
 *      invokeDialog() throws Throwable {
 *          //invoke first dialog
 *      }
 *  
 *      doTestDialog() throws Throwable {
 *          AbstractDialogTester two = new AbstractDialogTester(secondTitle,_display) {
 *              protected void invokeDialog) throws Throwable {
 *                  //invoke second dialog
 *              }
 *              protected void doTestDialog() throws Throwable { ... }
 *              protected void doCloseDialog(boolean ok) throws Throwable { //close second dialog }
 *          };
 *          //maybe run some tests on first dialog before launching second.
 *          two.runDialog();
 *          //maybe run some tests on first dialog after closing second.
 *      }
 *  
 *      doCloseDialog(boolean ok) throws Throwable { //close first dialog }
 *  }
 *  one.runDialog(); //run all of the above code.
 *  
 *  
 */
public abstract class AbstractDialogTester {

    private static final String FORCE_CLOSE_DIALOG = "force_close_dialog_";

    private static final String FAILURE_TESTING_DIALOG = "failure_testing_dialog_";

    public static final int DEFAULT_TIMEOUT_MINUTES = 5;

    public static final int DEFAULT_TIMEOUT_CLOSE_SECONDS = 25;

    public static final int DEFAULT_TIMEOUT_INVOKEFAILURE_OR_SHELLSHOWING_SECONDS = 60;

    protected Display _display;

    private String _title;

    protected Shell _dialogShell;

    private int _timeoutMinutes;

    private int _timeoutCloseSeconds;

    private int _timeoutForInvokeFailureOrShellShowingSeconds;

    private volatile boolean _done = false;

    protected final WidgetFinder _finder = BasicFinder.getDefault();

    protected final ShellTester _shellTester = new ShellTester();

    protected final ButtonTester _buttonTester = new ButtonTester();

    public AbstractDialogTester(String title, Display display) {
        this(title, display, DEFAULT_TIMEOUT_MINUTES);
    }

    public AbstractDialogTester(String title, Display display, int timeoutMinutes) {
        _display = display;
        _title = title;
        _timeoutMinutes = timeoutMinutes;
        _timeoutCloseSeconds = DEFAULT_TIMEOUT_CLOSE_SECONDS;
        _timeoutForInvokeFailureOrShellShowingSeconds = DEFAULT_TIMEOUT_INVOKEFAILURE_OR_SHELLSHOWING_SECONDS;
    }

    protected void clickButton(Composite root, String label) throws WidgetNotFoundException, MultipleWidgetsFoundException {
        clickButton(root, makeClassAndTextMatcher(Button.class, label));
    }

    protected void clickButton(String label) throws WidgetNotFoundException, MultipleWidgetsFoundException {
        clickButton(_dialogShell, makeClassAndTextMatcher(Button.class, label));
    }

    protected void clickButton(final Composite root, final Matcher m) throws WidgetNotFoundException, MultipleWidgetsFoundException {
        final Composite searchRoot = (root == null) ? _dialogShell : root;
        final Button btn = (Button) _finder.find(searchRoot, m);
        final String label = _buttonTester.getText(btn);
        Log.debug(_title + ": Clicking Button..." + label);
        final ButtonTester tester = ButtonTester.getButtonTester();
        Robot.wait(new Condition() {

            public boolean test() {
                return tester.getEnabled(btn);
            }

            public String toString() {
                return label + " to be enabled";
            }
        });
        _buttonTester.actionClick(btn);
    }

    protected static Matcher makeClassAndTextMatcher(Class clazz, String text) {
        TextMatcher textMatcher = new TextMatcher(text);
        ClassMatcher classMatcher = new ClassMatcher(clazz);
        CompositeMatcher compMatcher = new CompositeMatcher(new Matcher[] { textMatcher, classMatcher });
        return compMatcher;
    }

    /**
     * This wait is for the Condition that waits on doCloseDialog(). 
     */
    public void setTimeoutCloseSeconds(int timeoutSeconds) {
        _timeoutCloseSeconds = timeoutSeconds;
    }

    /**
     * This wait is for the Condition that waits on doTestDialog() 
     */
    public void setTimeoutMinutes(int timeoutMinutes) {
        _timeoutMinutes = timeoutMinutes;
    }

    /**
     * This wait is for the Condition that waits on invokeDialog() 
     */
    public void setTimeoutForInvokeFailureOrShellShowingSeconds(int timeoutSeconds) {
        _timeoutForInvokeFailureOrShellShowingSeconds = timeoutSeconds;
    }

    /** 
     * This method should ONLY invoke the dialog under test.
     * 
     * @throws Throwable
     */
    protected abstract void invokeDialog() throws Throwable;

    /** 
     *  This method will launch, test, and close the dialog.
     */
    public void runDialog() {
        final List throwables = new ArrayList();
        final TestDialogThread th = new TestDialogThread(_title + " TestDialogThread") {

            public void run() {
                try {
                    Robot.delay(1000);
                    waitForInvokeDialogFailureOrShellShowing(_title, this);
                    if (!_invokeDialogFailed) {
                        waitForDialogShowing();
                        testDialog();
                    }
                } catch (Throwable t) {
                    Log.log(t);
                    throwables.add(t);
                } finally {
                    _done = true;
                }
            }
        };
        th.start();
        try {
            invokeDialog();
        } catch (Throwable t) {
            Log.log(t);
            th.setInvokeFailed();
            AssertionError ae = new AssertionError("Failed to invoke Dialog: " + _title);
            ae.initCause(t);
            throw ae;
        }
        try {
            Robot.wait(new Condition() {

                public boolean test() {
                    return _done;
                }

                public String toString() {
                    return _title + " did not complete its doTestDialog() method within " + _timeoutMinutes + " minutes.";
                }
            }, _timeoutMinutes * 60000, 1000);
        } catch (Throwable t) {
            Log.log(t);
            throwables.add(t);
            attemptCloseDialog(throwables);
        } finally {
            if (!throwables.isEmpty()) {
                AssertionError ae = new AssertionError("Failed while testing dialog: " + _title);
                ae.initCause(ExceptionHelper.chainThrowables(throwables));
                throw ae;
            }
        }
    }

    /** 
     * Convenience wait for a shell to be displayed.  This method is like 
     * waitForFrameShowing, only searches for top-level shells
     */
    public static void waitForShellShowing(final String title) {
        final WidgetTester w = new WidgetTester();
        Robot.wait(new Condition() {

            public boolean test() {
                return w.assertDecorationsShowing(title, true);
            }

            public String toString() {
                return title + " to show";
            }
        }, 60000);
    }

    /*************************************************************************
     * Similar to a JUnit test tearDown method, this method is responsible
     *     for backing out any changes (if the test requires) and closing
     *     the dialog.
     *     
     * @param boolean ok representing whether everything is ok up to this point or not
     * 
     *        - if everything is ok, subclass implementations should close the dialog normally
     *        e.g. by clicking OK or Finish, etc.
     *        
     *        - if everything is NOT ok at this point, subclass implementations should close 
     *        the dialog however they can 
     *        e.g. by clicking cancel, pressing the ESC key, etc.
     *                  
     * @throws Throwable
     ************************************************************************/
    protected abstract void doCloseDialog(boolean ok) throws Throwable;

    /**
     * All 'testing' or dialog manipulation should be done in this method.
     * 
     * @throws Throwable
     */
    protected abstract void doTestDialog() throws Throwable;

    protected final void testDialog() throws Throwable {
        List throwables = new ArrayList();
        try {
            doTestDialog();
        } catch (Throwable t) {
            t.printStackTrace();
            throwables.add(t);
        } finally {
            attemptCloseDialog(throwables);
            if (!throwables.isEmpty()) {
                throw ExceptionHelper.chainThrowables(throwables);
            }
        }
    }

    private void attemptCloseDialog(List throwables) {
        try {
            if (!throwables.isEmpty()) {
                try {
                    Log.warn("Taking screen capture b/c of failure while testing the " + _title + " dialog.");
                    ScreenCapture.createScreenCapture(FAILURE_TESTING_DIALOG);
                } catch (Throwable t) {
                    Log.log(t);
                }
            }
            doCloseDialog(throwables.isEmpty());
        } catch (Throwable t) {
            Log.log(t);
            throwables.add(t);
        } finally {
            try {
                Robot.wait(new Condition() {

                    public boolean test() {
                        return (_dialogShell != null && _dialogShell.isDisposed());
                    }

                    public String toString() {
                        return _title + " failed to close within " + _timeoutCloseSeconds + " seconds.";
                    }
                }, _timeoutCloseSeconds * 1000, 1000);
            } catch (Throwable t) {
                Log.log(t);
                throwables.add(t);
            } finally {
                if (_dialogShell != null && !_dialogShell.isDisposed()) {
                    Log.warn("trying to brute force close dialog with title: " + _title);
                    try {
                        Log.warn("Taking screen capture b/c of failure while closing the " + _title + " dialog.");
                        ScreenCapture.createScreenCapture(FORCE_CLOSE_DIALOG);
                    } catch (Throwable t) {
                        Log.log(t);
                    }
                    if (_dialogShell.getDisplay() != null) {
                        Robot.syncExec(_dialogShell.getDisplay(), this, new Runnable() {

                            public void run() {
                                if (!_dialogShell.isDisposed()) _dialogShell.close();
                            }
                        });
                    }
                }
            }
        }
    }

    protected void waitForDialogShowing() throws WidgetNotFoundException, MultipleWidgetsFoundException {
        Robot.waitForIdle(_display);
        waitForShellShowing(_title);
        _dialogShell = (Shell) _finder.find(makeClassAndTextMatcher(Shell.class, _title));
        TestCase.assertNotNull("Dialog shell is showing, but was not found: " + _title, _dialogShell);
        _shellTester.actionFocus(_dialogShell);
    }

    private void waitForInvokeDialogFailureOrShellShowing(final String title, final TestDialogThread runner) {
        final WidgetTester w = new WidgetTester();
        Robot.wait(new Condition() {

            public boolean test() {
                if (runner != null && runner._invokeDialogFailed) {
                    return true;
                } else {
                    return w.assertDecorationsShowing(title, true);
                }
            }

            public String toString() {
                return title + " to show";
            }
        }, _timeoutForInvokeFailureOrShellShowingSeconds * 1000, 1000);
    }

    private class TestDialogThread extends Thread {

        protected boolean _invokeDialogFailed = false;

        public TestDialogThread(String name) {
            super(name);
        }

        public void setInvokeFailed() {
            System.err.println("invokeDialog() FAILED for: " + _title);
            _invokeDialogFailed = true;
        }
    }
}
