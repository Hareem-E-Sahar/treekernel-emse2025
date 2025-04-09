package dplayer.gui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import dplayer.gui.icons.Icons;

/**
 * Template for dialogs used within project.
 */
abstract class Dialog {

    private static final Logger logger = Logger.getLogger(Dialog.class);

    /**
     * Show selected dialog in modal mode.
     * @param display Parent of dialog.
     */
    static void show(final Class clazz, final Display display) {
        assert clazz != null;
        assert display != null;
        try {
            final Constructor<Dialog> c = clazz.getConstructor(new Class[] { Display.class });
            final Dialog d = c.newInstance(new Object[] { display });
            d.runModal();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Shell of dialog.
     */
    protected Shell mShell;

    private Composite mHeaderComposite;

    private Label mHeaderImage;

    private Link mHeaderText;

    protected void addHeader(final String[] text) {
        assert text != null;
        mHeaderComposite = new Composite(mShell, SWT.NONE);
        final RowLayout topLayout = new RowLayout();
        topLayout.marginHeight = 6;
        topLayout.marginWidth = 6;
        topLayout.spacing = 12;
        mHeaderComposite.setLayout(topLayout);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        mHeaderComposite.setLayoutData(gd);
        mHeaderImage = new Label(mHeaderComposite, SWT.NONE);
        mHeaderImage.setImage(Icons.APP);
        mHeaderText = new Link(mHeaderComposite, SWT.NONE);
        final StringBuilder sb = new StringBuilder();
        for (final String line : text) {
            sb.append(line).append('\n');
        }
        mHeaderText.setText(sb.toString());
        mHeaderText.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + event.text);
                } catch (IOException e) {
                }
            }
        });
    }

    protected void setMoveable() {
        mHeaderComposite.addMouseListener(new MoveDialogAdapter(mShell, mHeaderComposite));
        mHeaderImage.addMouseListener(new MoveDialogAdapter(mShell, mHeaderImage));
        mHeaderText.addMouseListener(new MoveDialogAdapter(mShell, mHeaderText));
    }

    protected void setSimpleClose() {
        final CloseCommand close = new CloseCommand();
        mHeaderComposite.addMouseListener(close);
        mHeaderImage.addMouseListener(close);
        mHeaderText.addMouseListener(close);
    }

    private static final class MoveDialogAdapter extends MouseAdapter {

        private Point start;

        private MoveDialogAdapter(final Shell shell, final Control control) {
            control.addMouseMoveListener(new MouseMoveListener() {

                public void mouseMove(final MouseEvent event) {
                    if (start != null) {
                        final int dx = event.x - start.x;
                        final int dy = event.y - start.y;
                        final Point loc = shell.getLocation();
                        shell.setLocation(loc.x + dx, loc.y + dy);
                    }
                }
            });
        }

        @Override
        public void mouseDown(final MouseEvent event) {
            if (event.button == 1) {
                start = new Point(event.x, event.y);
            }
        }

        @Override
        public void mouseUp(final MouseEvent event) {
            if (event.button == 1) {
                start = null;
            }
        }
    }

    /**
     * Set painted region of given shell. The four corners are rounded according
     * to the specified subtraction array (biggest subtraction first). 
     * @param shell Shell to paint with rounded corners.
     * @param subst Subtraction array (biggest first).
     */
    protected void setRoundedBorder(final int[] subst, final int borderWidth) {
        final Point size = mShell.getSize();
        final Image image = new Image(mShell.getDisplay(), size.x, size.y);
        final GC gc = new GC(image);
        gc.setBackground(mShell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER));
        gc.fillRoundRectangle(0, 0, size.x, size.y, 20, 20);
        gc.setBackground(mShell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        gc.fillRoundRectangle(borderWidth, borderWidth, size.x - (2 * borderWidth), size.y - (2 * borderWidth), 20, 20);
        gc.dispose();
        final Region region = new Region();
        region.add(0, 0, size.x, size.y);
        for (int y = 0; y < subst.length; y++) {
            region.subtract(0, y, subst[y], 1);
            region.subtract(size.x - subst[y], y, subst[y], 1);
        }
        for (int y = 0; y < subst.length; y++) {
            region.subtract(0, size.y - y - 1, subst[y], 1);
            region.subtract(size.x - subst[y], size.y - y - 1, subst[y], 1);
        }
        mShell.setRegion(region);
        mShell.addPaintListener(new PaintListener() {

            public void paintControl(final PaintEvent e) {
                e.gc.drawImage(image, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
            }
        });
    }

    /**
     * Run event queue of previously opened dialog.
     */
    protected void runModal() {
        final Display display = mShell.getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Close dialog in case of (almost) any mouse clicks within dialog window.
     */
    protected class CloseCommand extends MouseAdapter implements KeyListener {

        /** {@inheritDoc} */
        @Override
        public void mouseUp(final MouseEvent event) {
            mShell.dispose();
        }

        /** {@inheritDoc} */
        public void keyPressed(final KeyEvent event) {
        }

        /** {@inheritDoc} */
        public void keyReleased(final KeyEvent event) {
            if (event.keyCode == SWT.ESC || (event.stateMask == SWT.ALT && event.keyCode == SWT.F4)) {
                mShell.dispose();
            }
        }
    }
}
