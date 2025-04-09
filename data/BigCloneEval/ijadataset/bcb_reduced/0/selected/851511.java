package org.wtc.eclipse.platform.internal.helpers.impl;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.util.ScreenCapture;
import junit.framework.TestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.wtc.eclipse.platform.PlatformActivator;
import org.wtc.eclipse.platform.helpers.IUIHelper;
import org.wtc.eclipse.platform.helpers.adapters.HelperImplAdapter;
import java.util.regex.Pattern;

/**
 * Helper that contains simple macros of UI generated input.** THIS CLASS SHOULD NOT BE
 * SAVED AS A DATA MEMBER !! **
 */
public class UIHelperImpl extends HelperImplAdapter implements IUIHelper {

    private static final String OPTION_UI_HELPER_DEBUG = "/logging/uihelper";

    private volatile boolean _thaw = false;

    /**
     * @see  org.wtc.eclipse.platform.helpers.IUIHelper#annoy(com.windowtester.runtime.IUIContext)
     */
    public void annoy(final IUIContext ui) {
        logEntry2();
        final Display d = Display.getDefault();
        d.syncExec(new Runnable() {

            public void run() {
                for (int i = 0; i < 2; i++) {
                    d.beep();
                    ui.pause(500);
                }
                for (int i = 0; i < 3; i++) {
                    d.beep();
                    ui.pause(250);
                }
            }
        });
        logExit2();
    }

    /**
     * @see  org.wtc.eclipse.platform.helpers.IUIHelper#arrowDown(com.windowtester.runtime.IUIContext,
     *       int)
     */
    public void arrowDown(IUIContext ui, int numberOfTimes) {
        logEntry2(Integer.toString(numberOfTimes));
        PlatformActivator.logDebug("UIHelper.arrowDown(" + numberOfTimes + ")", OPTION_UI_HELPER_DEBUG);
        TestCase.assertTrue(numberOfTimes > 0);
        keyPress(ui, SWT.ARROW_DOWN, numberOfTimes);
        logExit2();
    }

    /**
     * @see  org.wtc.eclipse.platform.helpers.IUIHelper#arrowUp(com.windowtester.runtime.IUIContext,
     *       int)
     */
    public void arrowUp(IUIContext ui, int numberOfTimes) {
        logEntry2(Integer.toString(numberOfTimes));
        PlatformActivator.logDebug("UIHelper.arrowUp(" + numberOfTimes + ")", OPTION_UI_HELPER_DEBUG);
        TestCase.assertTrue(numberOfTimes > 0);
        keyPress(ui, SWT.ARROW_UP, numberOfTimes);
        logExit2();
    }

    /**
     * @see  org.wtc.eclipse.platform.helpers.IUIHelper#freeze(com.windowtester.runtime.IUIContext)
     */
    public void freeze(IUIContext ui) {
        logEntry2();
        _thaw = false;
        String value = System.getProperty("prodMode");
        if ((value != null) && (value.equals("dev"))) {
            while (!_thaw) {
                ui.pause(2000);
            }
        } else {
            TestCase.fail("-!!-FREEZE HAS BEEN CALLED IN A NON-DEVELOPMENT ENVIRONMENT-!!-");
        }
        logExit2();
    }

    /**
     * @since 3.8.0
     * @param  ui             - Driver for UI generated input
     * @param  code           - The SWT code to press
     * @param  numberOfTimes  - The number of times to press the key
     */
    private void keyPress(IUIContext ui, int code, int numberOfTimes) {
        TestCase.assertTrue(numberOfTimes > 0);
        for (int i = 0; i < numberOfTimes; i++) {
            ui.keyClick(code);
        }
    }

    /**
     * @see  org.wtc.eclipse.platform.helpers.adapters.HelperImplAdapter#pressEnter(com.windowtester.runtime.IUIContext)
     */
    @Override
    public void pressEnter(IUIContext ui) {
        keyPress(ui, SWT.CR, 1);
    }

    /**
     * @see  org.wtc.eclipse.platform.helpers.IUIHelper#screenshot(com.windowtester.runtime.IUIContext,
     *       java.lang.String)
     */
    public void screenshot(IUIContext ui, String title) {
        logEntry2(title);
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(title);
        TestCase.assertTrue("THE TITLE <" + title + "> IS NOT A VALID FILE NAME", Pattern.matches("[a-zA-Z]*[a-zA-Z0-9\\.]*[a-zA-Z0-9]+", title));
        ScreenCapture.createScreenCapture(title);
        logExit2();
    }

    /**
     * @see  org.wtc.eclipse.platform.helpers.IUIHelper#thaw()
     */
    public void thaw() {
        _thaw = true;
    }
}
