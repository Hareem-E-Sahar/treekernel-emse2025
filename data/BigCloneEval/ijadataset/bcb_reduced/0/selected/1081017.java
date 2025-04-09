package org.jcryptool.analysis.friedman.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jcryptool.analysis.friedman.calc.FriedmanCalc;

public class CustomFriedmanGraph implements PaintListener {

    private Composite mycomp;

    private Canvas canv;

    private Text helptext;

    private FriedmanCalc myAnalysis;

    String hint = "";

    private int helpWidth, helpHeight;

    double currentBegin = 0, currentEnde = 1, widthZoomRect, XZoomRect, currentZoomRectLeft, currentZoomRectRight, zoomValue = 0.60;

    double[] friedmanresults;

    public void setAnalysis(FriedmanCalc in) {
        myAnalysis = in;
        friedmanresults = myAnalysis.analysis;
    }

    public CustomFriedmanGraph(Composite comp) {
        mycomp = comp;
        helpHeight = 80 + 9000;
        helpWidth = 500 + 9000;
        helptext = new Text(mycomp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.CENTER);
        helptext.setText("");
        helptext.setVisible(false);
        helptext.setSize(helpWidth, helpHeight);
        helptext.setEditable(false);
        canv = new Canvas(mycomp, SWT.NONE);
        canv.setLayout(null);
        comp.setLayout(null);
        canv.setBackground(canv.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        int width = 0, height = 0;
        width = mycomp.getSize().x;
        height = mycomp.getSize().y;
        canv.setSize(width, height);
        helptext.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent evt) {
                helptextPaintControl(evt);
            }
        });
        canv.addPaintListener(this);
        canv.addMouseMoveListener(new MouseMoveListener() {

            public void mouseMove(MouseEvent evt) {
                canvMouseMove(evt);
            }
        });
        canv.addMouseListener(new MouseAdapter() {

            public void mouseDown(MouseEvent evt) {
                canvMouseDown(evt);
            }

            public void mouseUp(MouseEvent evt) {
                canvMouseUp(evt);
            }
        });
        canv.addMouseTrackListener(new MouseTrackAdapter() {

            public void mouseExit(MouseEvent evt) {
                canvMouseExit(evt);
            }
        });
    }

    public void setOffset(int myOffset) {
    }

    public void redraw() {
        canv.redraw();
    }

    public static int max(int[] t) {
        int maximum = t[0];
        for (int i = 1; i < t.length; i++) {
            if (t[i] > maximum) {
                maximum = t[i];
            }
        }
        return maximum;
    }

    /**
	 * returns the int value of a char which was set off by a specified value (from A=65 to Z = 90)
	 * @param character the character to be set off
	 * @param offset the offset
	 */
    public int offsetChar(int character, int offset) {
        return 65 + (character + offset - 65) % 26;
    }

    private void drawBar(int X, int Y, int hoehe, int breite, int kappenstaerke, GC gc, boolean alignMid) {
        int xleft, xright;
        if (alignMid) xleft = X - breite / 2; else xleft = X;
        xright = xleft + breite;
        MColor bar_border_real = new MColor("CC4A00", 255);
        MColor bar_fill_real = new MColor("FF0E08", 165);
        bar_border_real.setColor(gc);
        bar_fill_real.setBGColor(gc);
        gc.fillRectangle(xleft, Y, xright - xleft, -hoehe);
    }

    /**
	 * Method evoked with paint events from the registered Listener
	 */
    public void paintControl(PaintEvent e) {
        int width = 0, height = 0;
        int rightOffset = 0, bottomOffset = 0;
        width = Math.max(mycomp.getSize().x - rightOffset, 1);
        height = Math.max(mycomp.getSize().y - bottomOffset, 1);
        canv.setSize(width, height);
        helptext.setBounds((width - Math.min(width, helpWidth)) / 2, (height - Math.min(height, helpHeight)) / 2, Math.min(width, helpWidth), Math.min(height, helpHeight));
        GC gc = e.gc;
        if (friedmanresults != null) {
            int X, Y, XNext, YNext, XText, innerImageHoehe, balkenstaerke, kappenstaerke, marginLeft, marginBottom, abstandText, roundedIndexBegin, roundedIndexEnde;
            double indexBegin, verhaeltnisBeginn, verschiebungBeginn, verhaeltnisEnde, verschiebungEnde, indexEnde, balkenPerPixel, massstabX, massstabY, highestValue, laenge;
            marginLeft = 0;
            marginBottom = 0;
            MColor FarbeFormBG = new MColor(181, 205, 229, 255);
            MColor imageBG = new MColor(191, 215, 239, 255);
            imageBG.setBGColor(gc);
            FarbeFormBG.setColor(gc);
            gc.drawRectangle(-1, -1, width + 2, height + 2);
            int innerWidth = width - marginLeft;
            int innerHeight = height - marginBottom;
            innerImageHoehe = height - marginBottom;
            laenge = currentEnde - currentBegin;
            indexBegin = currentBegin * friedmanresults.length;
            roundedIndexBegin = (int) Math.floor(indexBegin);
            verhaeltnisBeginn = indexBegin - roundedIndexBegin;
            indexEnde = currentEnde * (friedmanresults.length - 1);
            if ((indexEnde - (int) Math.floor(indexEnde)) > 0.00001) roundedIndexEnde = (int) Math.ceil(indexEnde); else roundedIndexEnde = (int) Math.floor(indexEnde);
            verhaeltnisEnde = roundedIndexEnde - indexEnde;
            massstabX = innerWidth / (friedmanresults.length * laenge);
            verschiebungBeginn = verhaeltnisBeginn * massstabX;
            verschiebungEnde = verhaeltnisEnde * massstabX;
            balkenPerPixel = (roundedIndexEnde - roundedIndexBegin) / (width + verschiebungBeginn + verschiebungEnde);
            if (balkenPerPixel < 0.0000001) balkenPerPixel = 0.0000001;
            balkenstaerke = (int) Math.round((double) ((double) 1 / (double) balkenPerPixel) * (double) 2 / (double) 5);
            if (balkenPerPixel < 0.01) balkenstaerke = 40;
            if (balkenPerPixel > 0.4) balkenstaerke = 1;
            kappenstaerke = (int) Math.round(((double) 1 / (double) balkenPerPixel) * (double) 1 / (double) 20);
            if (balkenPerPixel < 0.01) kappenstaerke = 5;
            if (balkenPerPixel > 0.05) kappenstaerke = 1;
            kappenstaerke++;
            abstandText = (int) Math.ceil(30 * balkenPerPixel);
            if (roundedIndexBegin < 0) roundedIndexBegin = 0;
            if (roundedIndexBegin > friedmanresults.length - 1) roundedIndexBegin = friedmanresults.length - 1;
            if (roundedIndexEnde < 0) roundedIndexEnde = 0;
            if (roundedIndexEnde > friedmanresults.length - 1) roundedIndexEnde = friedmanresults.length - 1;
            highestValue = 0;
            for (int i = 1; i < friedmanresults.length; i++) if (friedmanresults[i] > highestValue) highestValue = friedmanresults[i];
            if (highestValue == 0) highestValue = 0.0001;
            massstabY = (innerHeight - 15) / highestValue;
            double coincidenceGerman = 0.65;
            double coincidenceEnglish = 0.75;
            Y = (int) Math.round(coincidenceGerman * massstabY);
            gc.drawLine(0, innerImageHoehe - Y, innerWidth, innerImageHoehe - Y);
            Y = (int) Math.round(coincidenceEnglish * massstabY);
            gc.drawLine(0, innerImageHoehe - Y, innerWidth, innerImageHoehe - Y);
            for (int i = roundedIndexBegin; i <= roundedIndexEnde; i++) {
                X = (int) Math.round((i - roundedIndexBegin - verhaeltnisBeginn) * massstabX);
                Y = (int) Math.round(friedmanresults[i] * massstabY);
                XText = marginLeft + X + balkenstaerke / 2 - (((String) ("" + (int) (i + 1))).length() * gc.getFontMetrics().getAverageCharWidth()) / 2;
                new MColor(255, 50, 20, 255, gc);
                drawBar(X, innerImageHoehe, Y, balkenstaerke, kappenstaerke, gc, false);
                if (((i - roundedIndexBegin) % abstandText == 0) && (XText > marginLeft - 5)) gc.drawText("" + (i + 1), XText, 5, true);
                if (i < roundedIndexEnde) {
                    XNext = (int) Math.round(((i + 1) - roundedIndexBegin - verhaeltnisBeginn) * massstabX);
                    YNext = (int) Math.round(friedmanresults[i + 1] * massstabY);
                    gc.drawLine(X + balkenstaerke, innerImageHoehe - Y, XNext, innerImageHoehe - YNext);
                }
            }
            if (!dragging) {
                MColor barBottom = new MColor("080818", 210);
                barBottom.setBGColor(gc);
                gc.fillRectangle(0, innerImageHoehe - 20, width, 20);
                MColor fontBG = new MColor("000000", 255);
                fontBG.setBGColor(gc);
                MColor fontColor = new MColor("ffffff", 255);
                fontColor.setColor(gc);
                for (int i = roundedIndexBegin; i <= roundedIndexEnde; i++) {
                    X = (int) Math.round((i - roundedIndexBegin - verhaeltnisBeginn) * massstabX);
                    XText = marginLeft + X + balkenstaerke / 2 - (((String) ("" + (int) (i + 1))).length() * gc.getFontMetrics().getAverageCharWidth()) / 2;
                    if (((i - roundedIndexBegin) % abstandText == 0) && (XText > marginLeft - 5)) gc.drawText("" + (i + 1), XText, innerImageHoehe - 16, true);
                }
            }
        }
        gc.dispose();
    }

    public void resetzoomandshift() {
        currentBegin = 0;
        currentEnde = 1;
    }

    public void showFirstXBars(double X) {
        currentBegin = 0;
        currentEnde = X / friedmanresults.length;
    }

    private void zoomin() {
        currentBegin = currentZoomRectLeft;
        currentEnde = currentZoomRectRight;
        redraw();
    }

    private void zoomout() {
        double gesamtlaenge, mitte, neuBegin, neuEnde;
        gesamtlaenge = currentEnde - currentBegin;
        mitte = (currentEnde + currentBegin) / 2;
        neuBegin = mitte - (gesamtlaenge / (2 * zoomValue));
        neuEnde = mitte + (gesamtlaenge / (2 * zoomValue));
        if (neuBegin < 0) neuBegin = 0;
        if (neuEnde > 1) neuEnde = 1;
        currentBegin = neuBegin;
        currentEnde = neuEnde;
        redraw();
    }

    private void setZoomLimits(int mX, int mY) {
        widthZoomRect = (double) zoomValue * (double) canv.getSize().x;
        XZoomRect = (double) mX - (double) (widthZoomRect) / (double) 2;
        if (XZoomRect < 0) XZoomRect = 0;
        if (XZoomRect > canv.getSize().x - widthZoomRect) XZoomRect = canv.getSize().x - widthZoomRect;
        currentZoomRectLeft = (double) ((double) XZoomRect / (double) canv.getSize().x) * (double) (currentEnde - currentBegin) + currentBegin;
        currentZoomRectRight = (double) ((double) (XZoomRect + widthZoomRect) / (double) canv.getSize().x) * (double) (currentEnde - currentBegin) + currentBegin;
    }

    boolean mouseDown = false, dragging = false;

    int mDownX = 0, mDownY = 0, mDownBtn = 0;

    double dragLimitLeft0, dragLimitRight0;

    private void canvMouseMove(MouseEvent evt) {
        setZoomLimits(evt.x, evt.y);
        if (mouseDown) doDrag(mDownX, mDownY, evt.x, evt.y, mDownBtn);
    }

    private void canvMouseDown(MouseEvent evt) {
        mouseDown = true;
        mDownX = evt.x;
        mDownY = evt.y;
        mDownBtn = evt.button;
        dragLimitLeft0 = currentBegin;
        dragLimitRight0 = currentEnde;
    }

    private void canvMouseUp(MouseEvent evt) {
        mouseDown = false;
        if (evt.x == mDownX && evt.y == mDownY) doClick(evt.x, evt.y, evt.button);
        dragging = false;
        redraw();
    }

    private void canvMouseExit(MouseEvent evt) {
        mouseDown = false;
    }

    private void doClick(int x, int y, int button) {
        if (button == 1) zoomin();
        if (button == 3) zoomout();
        redraw();
        setZoomLimits(x, y);
    }

    private void doDrag(int x0, int y0, int x, int y, int button) {
        if (button == 1) {
            double realShift, shift = 0;
            shift = -(double) ((double) (x - x0) / (double) canv.getSize().x) * ((double) (dragLimitRight0 - dragLimitLeft0));
            realShift = shift;
            if (dragLimitLeft0 + realShift < 0) realShift = -dragLimitLeft0;
            if (dragLimitRight0 + realShift > 1) realShift = 1 - dragLimitRight0;
            currentBegin = dragLimitLeft0 + realShift;
            currentEnde = dragLimitRight0 + realShift;
            dragging = true;
            if (Math.abs(realShift) > 0.0000) {
                redraw();
            }
        }
    }

    private void helptextPaintControl(PaintEvent evt) {
        int width = Math.max(mycomp.getSize().x, 1);
        int height = Math.max(mycomp.getSize().y, 1);
        helptext.setBounds((width - Math.min(width, helpWidth)) / 2, (height - Math.min(height, helpHeight)) / 2, Math.min(width, helpWidth), Math.min(height, helpHeight));
    }

    public void setHint(String pHint) {
        hint = pHint;
        helptext.setText(hint);
    }

    public void enableHelp(boolean enable) {
        helptext.setVisible(enable);
    }
}
