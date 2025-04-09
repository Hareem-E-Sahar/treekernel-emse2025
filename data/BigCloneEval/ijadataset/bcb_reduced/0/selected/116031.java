package org.wtc.eclipse.draw2d.helpers;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.condition.ICondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.util.ScreenCapture;
import junit.framework.TestCase;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerFigure;
import org.wtc.eclipse.draw2d.Draw2DActivator;
import org.wtc.eclipse.draw2d.exceptions.FigureNotFoundException;
import org.wtc.eclipse.draw2d.exceptions.MultipleFiguresFoundException;
import org.wtc.eclipse.draw2d.tester.FigureTester;
import org.wtc.eclipse.platform.helpers.adapters.HelperImplAdapter;

/**
 * Helper delegate for finding and selecting palette items.
 * 
 * @since 3.8.0
 */
public class GenericPaletteHelper extends HelperImplAdapter {

    /**
     * clickCategoryItem - Utility to find a palette item by its label, then click it.
     * This method will issue a test case failure if the palette item could not be found
     * for any reason
     *
     * @since 3.8.0
     * @param  ui             - driver for ui generated events
     * @param  rootCanvas     - the root FigureCanvas
     * @param  categoryLabel  - The category to click
     */
    public void clickCategoryItem(IUIContext ui, FigureCanvas rootCanvas, String categoryLabel) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(rootCanvas);
        TestCase.assertNotNull(categoryLabel);
        Draw2DActivator.logDebug("Searching for palette item: " + "category: " + categoryLabel);
        IFigure paletteItem = null;
        try {
            paletteItem = findCategoryByLabel(rootCanvas, categoryLabel);
        } catch (FigureNotFoundException fnfe) {
            Draw2DActivator.logException(fnfe);
            TestCase.fail(fnfe.getLocalizedMessage());
        } catch (MultipleFiguresFoundException mffe) {
            Draw2DActivator.logException(mffe);
            TestCase.fail(mffe.getLocalizedMessage());
        }
        Draw2DActivator.logDebug("Found palette item: " + categoryLabel);
        FigureTester figureTester = new FigureTester();
        figureTester.clickFigure(ui, rootCanvas, paletteItem);
    }

    /**
     * doubleClickPaletteItem - Utility to find a palette item by its category and label,
     * then double click it. This method will issue a test case failure if the palette
     * item could not be found for any reason
     *
     * @since 3.8.0
     * @param  ui             - Driver for UI generated input
     * @param  rootCanvas     - The parent canvas for a design palette
     * @param  locator        - With Window Tester 2.0, the widget locator is also the
     *                        manager of how a widget will be selected (for example, the
     *                        ButtonLocator will handle the button.click() and verify the
     *                        widget has been properly received the click event). The
     *                        locator also handles the waitForIdle() logic to make sure
     *                        the UI is free to return from this UI operation. For some UI
     *                        operations (like launching Swing dialogs), the wait for idle
     *                        must be a loose interpretation (rather then the classic wait
     *                        for a syncExec ping to return). Use this parameter to
     *                        override the selection and/or waitForIdle logic or provide
     *                        <code>null</code> if the default selection & waitForIdle
     *                        logic is to be used.
     * @param  categoryLabel  - The label of the category whose expansion child item is to
     *                        be clicked
     * @param  item           - The label of the item to be double clicked
     */
    public void doubleClickPaletteItem(IUIContext ui, FigureCanvas rootCanvas, IWidgetLocator locator, String categoryLabel, String item) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(rootCanvas);
        TestCase.assertNotNull(categoryLabel);
        TestCase.assertNotNull(item);
        Draw2DActivator.logDebug("Searching for palette item: " + item + " in category: " + categoryLabel);
        IFigure paletteItem = null;
        try {
            paletteItem = findPaletteItemForCategory(ui, rootCanvas, categoryLabel, item);
        } catch (FigureNotFoundException fnfe) {
            ScreenCapture.createScreenCapture("doubleClickPaletteItem_" + categoryLabel);
            Draw2DActivator.logException(fnfe);
            TestCase.fail(fnfe.getLocalizedMessage());
        } catch (MultipleFiguresFoundException mffe) {
            ScreenCapture.createScreenCapture("doubleClickPaletteItem_" + categoryLabel);
            Draw2DActivator.logException(mffe);
            TestCase.fail(mffe.getLocalizedMessage());
        }
        Draw2DActivator.logDebug("Found palette item: " + item);
        FigureTester figureTester = new FigureTester();
        figureTester.doubleClickFigure(ui, rootCanvas, locator, paletteItem);
    }

    /**
     * find a palette item in the palette and drag and drop it onto the target IFigure.
     *
     * @since 3.8.0
     * @param  ui             - driver for ui generated events
     * @param  sourceCanvas   - the root FigureCanvas of the palette
     * @param  categoryLabel  - the palette category containing the item
     * @param  itemText       - the text of the item
     * @param  targetCanvas   - the root FigureCanvas of the target figure
     * @param  target         - the IFigure on which to drop the palette item
     */
    public void dragAndDropPaletteItem(IUIContext ui, FigureCanvas sourceCanvas, String categoryLabel, String itemText, FigureCanvas targetCanvas, IFigure target) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(sourceCanvas);
        TestCase.assertNotNull(categoryLabel);
        TestCase.assertNotNull(itemText);
        TestCase.assertNotNull(targetCanvas);
        TestCase.assertNotNull(target);
        IFigure paletteItem = null;
        try {
            paletteItem = findPaletteItemForCategory(ui, sourceCanvas, categoryLabel, itemText);
        } catch (FigureNotFoundException fnfe) {
            Draw2DActivator.logException(fnfe);
            TestCase.fail(fnfe.getLocalizedMessage());
        } catch (MultipleFiguresFoundException mffe) {
            Draw2DActivator.logException(mffe);
            TestCase.fail(mffe.getLocalizedMessage());
        }
        DragAndDropHelperImpl dndHelper = new DragAndDropHelperImpl();
        dndHelper.dragAndDrop(ui, sourceCanvas, paletteItem, targetCanvas, target);
    }

    /**
     * findAndExpandCategory - Search for the design palette category in the given design
     * palette and with the given text, check its expanded state, and (if the expanded
     * state is not the expected state) toggle the category's expanded state.
     *
     * @since 3.8.0
     * @param  ui                - Driver for UI generated input
     * @param  parentCanvas      - The parent canvas for a design palette
     * @param  categoryLabel     - The label of the category whose expansion is to be
     *                           verified
     * @param  shouldBeExpanded  - True if the category should be expanded; False
     *                           otherwise
     */
    private void findAndExpandCategory(IUIContext ui, FigureCanvas rootCanvas, String categoryLabel, boolean shouldBeExpanded) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(rootCanvas);
        TestCase.assertNotNull(categoryLabel);
        try {
            if (isCategoryExpanded(rootCanvas, categoryLabel) != shouldBeExpanded) {
                IFigure category = findCategoryByLabel(rootCanvas, categoryLabel);
                toggleCategoryExpansion(ui, rootCanvas, category);
            }
        } catch (MultipleFiguresFoundException e) {
            Draw2DActivator.logException(e);
            TestCase.fail(e.getLocalizedMessage());
        } catch (FigureNotFoundException e) {
            Draw2DActivator.logException(e);
            TestCase.fail(e.getLocalizedMessage());
        }
    }

    /**
     * findCategoryByLabel - Given a parent IFigure, scan recursively for a DrawerFigure
     * for a given label. This method is useful for finding root category figures in the
     * design palette
     *
     * @throws  MultipleFiguresFoundException
     * @throws  FigureNotFoundException
     */
    public IFigure findCategoryByLabel(FigureCanvas rootCanvas, String labelText) throws FigureNotFoundException, MultipleFiguresFoundException {
        TestCase.assertNotNull(rootCanvas);
        TestCase.assertNotNull(labelText);
        IFigure rootFigure = rootCanvas.getContents();
        TestCase.assertNotNull(rootFigure);
        return findCategoryByLabel(rootFigure, labelText);
    }

    /**
     * findCategoryByLabel - Given a parent IFigure, scan recursively for a DrawerFigure
     * for a given label. This method is useful for finding root category figures in the
     * design palette
     *
     * @throws  MultipleFiguresFoundException
     * @throws  FigureNotFoundException
     */
    public IFigure findCategoryByLabel(IFigure rootFigure, String labelText) throws FigureNotFoundException, MultipleFiguresFoundException {
        TestCase.assertNotNull(rootFigure);
        TestCase.assertNotNull(labelText);
        FigureHelper figureHelper = new FigureHelper();
        IFigure label = figureHelper.findFigureByLabel(rootFigure, labelText);
        IFigure category = label.getParent().getParent().getParent();
        return category;
    }

    /**
     * Find and return the IFigure for the palette item with the given label under the
     * given catagory.
     *
     * @param   ui
     * @param   rootCanvas
     * @param   categoryLabel
     * @param   itemText
     * @return  the IFigure for the palette item
     */
    public IFigure findPaletteItemForCategory(IUIContext ui, FigureCanvas rootCanvas, String categoryLabel, String itemText) throws FigureNotFoundException, MultipleFiguresFoundException {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(rootCanvas);
        TestCase.assertNotNull(categoryLabel);
        TestCase.assertNotNull(itemText);
        IFigure categoryRoot = findCategoryByLabel(rootCanvas, categoryLabel);
        findAndExpandCategory(ui, rootCanvas, categoryLabel, true);
        FigureHelper figureHelper = new FigureHelper();
        IFigure paletteItemFigure = figureHelper.findFigureByLabel(categoryRoot, itemText);
        TestCase.assertTrue(paletteItemFigure instanceof TextFlow);
        TextFlow tf = (TextFlow) paletteItemFigure;
        TestCase.assertTrue(tf.getParent().getParent().getParent() instanceof Figure);
        return paletteItemFigure;
    }

    /**
     * isCategoryExpanded - Find the palette category in the given canvas with the given
     * heading and check its expanded state.
     *
     * @param   rootCanvas     - A design palette draw 2d container
     * @param   categoryLabel  - The text associated with the category to check
     * @return  boolean - True if the category is found and expanded; False if the
     *          category is found and collapsed
     * @throws  FigureNotFoundException
     * @throws  MultipleFiguresFoundException
     */
    protected boolean isCategoryExpanded(FigureCanvas rootCanvas, String categoryLabel) throws MultipleFiguresFoundException, FigureNotFoundException {
        boolean expanded = false;
        IFigure categoryRoot = findCategoryByLabel(rootCanvas, categoryLabel);
        if (categoryRoot instanceof DrawerFigure) {
            expanded = ((DrawerFigure) categoryRoot).isExpanded();
        }
        return expanded;
    }

    /**
     * toggleCategoryExpansion - Toggle the expanded state of the given design palette
     * category.
     *
     * @param   ui              - Driver for UI generated input
     * @param   parentCanvas    - The parent canvas for a design palette
     * @param   categoryFigure  - draw2d figure for a design palette category
     * @throws  FigureNotFoundException
     * @throws  MultipleFiguresFoundException
     */
    private void toggleCategoryExpansion(IUIContext ui, FigureCanvas parentCanvas, IFigure categoryFigure) throws FigureNotFoundException, MultipleFiguresFoundException {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(categoryFigure);
        FigureTester figureTester = new FigureTester();
        figureTester.clickFigure(ui, parentCanvas, categoryFigure);
    }

    /**
     * verifyCategoryExists - Open the palette, and verify the category with the given
     * label exists or doesn't exist based on the provided boolean.
     *
     * @since 3.8.0
     * @param  ui             - Driver for UI generated input
     * @param  rootCanvas     - The root FigureCanvas
     * @param  categoryLabel  - The screen label of the palette category to search for
     * @param  exists         - True if the category should exist
     */
    public void verifyCategoryExists(final IUIContext ui, final FigureCanvas rootCanvas, final String categoryLabel, final boolean exists) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(categoryLabel);
        ui.wait(new ICondition() {

            public boolean test() {
                try {
                    findCategoryByLabel(rootCanvas, categoryLabel);
                    return exists;
                } catch (FigureNotFoundException e) {
                    return !exists;
                } catch (MultipleFiguresFoundException e) {
                    return !exists;
                }
            }

            @Override
            public String toString() {
                String message = "The category <" + categoryLabel + "> was " + ((exists) ? "not" : "") + "found";
                return message;
            }
        });
    }
}
