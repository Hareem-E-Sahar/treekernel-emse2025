package net.sourceforge.webcompmath.functions;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.*;
import net.sourceforge.webcompmath.data.*;
import net.sourceforge.webcompmath.draw.*;
import net.sourceforge.webcompmath.awt.*;

/**
 * A TableInputFunction is a Panel that can be used to define a TableFunction or
 * to edit an existing TableFunction. To fetch the function currently displayed
 * in the panel, call copyOfCurrentFunction(). To edit a function, call
 * startEdit() to install the function. Then call cancelEdit() or finishEdit()
 * to finish the editing. The panel displays both the graph of the function and
 * a list of points that define the function. The user can drag the points on
 * the graph up and down to change the y-values. Points can be added or modified
 * by typing in their x- and y-coordinates. A point can be deleted by clicking
 * it in the list of points and then clicking on the button labeled "Delete
 * Point".
 */
public class TableFunctionInput extends JPanel implements ItemListener, ActionListener, MouseListener, MouseMotionListener, ListSelectionListener {

    private VariableInput xInput, yInput;

    private DisplayCanvas canvas;

    private JList pointList;

    private DefaultListModel pointListModel;

    private JButton clearButton, deleteButton, addButton;

    private JRadioButton[] styleCheckbox = new JRadioButton[5];

    private ButtonGroup styleGroup;

    private Controller onChange;

    private TableFunction function;

    private TableFunction editFunction;

    /**
     * Create a TableFunctionInput panel. Initially, the function in the panel
     * has no points and so is undefined everywhere. The panel needs to be
     * fairly large!
     */
    public TableFunctionInput() {
        xInput = new VariableInput();
        xInput.addActionListener(this);
        yInput = new VariableInput();
        yInput.addActionListener(this);
        pointListModel = new DefaultListModel();
        pointList = new JList(pointListModel);
        pointList.setBackground(Color.white);
        pointList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pointList.addListSelectionListener(this);
        clearButton = new JButton("Remove All Points");
        clearButton.addActionListener(this);
        deleteButton = new JButton("Delete Point");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(this);
        addButton = new JButton("Add/Modify Point");
        addButton.addActionListener(this);
        styleGroup = new ButtonGroup();
        styleCheckbox[0] = new JRadioButton("Smooth", true);
        styleCheckbox[1] = new JRadioButton("Piecewise Linear", false);
        styleCheckbox[2] = new JRadioButton("Step (nearest value)", false);
        styleCheckbox[3] = new JRadioButton("Step (value from left)", false);
        styleCheckbox[4] = new JRadioButton("Step (value from right)", false);
        for (int i = 0; i < 5; i++) {
            styleCheckbox[i].addItemListener(this);
            styleGroup.add(styleCheckbox[i]);
        }
        canvas = new DisplayCanvas(new CoordinateRect(-1, 1, -1, 1));
        canvas.add(new WcmAxes());
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        function = new TableFunction();
        canvas.add(new Draw());
        Font labelFont = new Font("Serif", Font.BOLD, 12);
        JLabel lab1 = new JLabel("Input Area");
        lab1.setForeground(Color.red);
        lab1.setFont(labelFont);
        JLabel lab2 = new JLabel("Type of Function", JLabel.CENTER);
        lab2.setForeground(Color.red);
        lab2.setFont(labelFont);
        JLabel lab3 = new JLabel("Table of Values", JLabel.CENTER);
        lab3.setForeground(Color.red);
        lab3.setFont(labelFont);
        JPanel topLeft = new JPanel();
        topLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 10000, 3));
        JPanel topRight = new JPanel();
        topRight.setLayout(new GridLayout(6, 1, 3, 3));
        JPanel bottomLeft = new JPanel();
        bottomLeft.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout(3, 3));
        top.add(topLeft, BorderLayout.CENTER);
        top.add(topRight, BorderLayout.EAST);
        setLayout(new BorderLayout(3, 3));
        add(top, BorderLayout.NORTH);
        add(bottomLeft, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);
        setBackground(Color.darkGray);
        topLeft.setBackground(Color.lightGray);
        topRight.setBackground(Color.lightGray);
        bottomLeft.setBackground(Color.lightGray);
        JPanel inputBar = new JPanel();
        inputBar.add(new JLabel("x = "));
        inputBar.add(xInput);
        inputBar.add(new JLabel(" y = "));
        inputBar.add(yInput);
        JPanel buttonBar = new JPanel();
        buttonBar.setLayout(new GridLayout(1, 2, 3, 3));
        buttonBar.add(deleteButton);
        buttonBar.add(clearButton);
        topLeft.add(lab1);
        topLeft.add(inputBar);
        topLeft.add(addButton);
        topLeft.add(new JLabel("(Press RETURN in X to move to Y)"));
        topLeft.add(new JLabel("(Press RETURN in Y to add/modify point)"));
        topRight.add(lab2);
        for (int i = 0; i < 5; i++) topRight.add(styleCheckbox[i]);
        bottomLeft.add(pointList, BorderLayout.CENTER);
        bottomLeft.add(lab3, BorderLayout.NORTH);
        bottomLeft.add(buttonBar, BorderLayout.SOUTH);
    }

    /**
     * Install a function to be edited. The data from the function is copied
     * into the panel, and a pointer to the function is retained. Note that the
     * function itself is not changed by any editing that the user does. To
     * commit the changes made by the user to the actual function, you must call
     * finishEdit(). If f is null, the effect is to start with a new, empty
     * function.
     * 
     * @param f
     *            table function to be edited
     */
    public void startEdit(TableFunction f) {
        editFunction = f;
        revertEditFunction();
    }

    /**
     * If a function has been specified using startEdit(), and neither
     * finishEdit() nor cancelEdit have been called, then this method will
     * discard the current data in and replace it with data from the edit
     * function. If there is no edit function, then the data is simply
     * discarded. (That is, the data reverts to an empty point list.)
     */
    public void revertEditFunction() {
        if (editFunction == null) {
            clearAllPoints();
            return;
        }
        function.copyDataFrom(editFunction);
        pointListModel.removeAllElements();
        int pointCt = function.getPointCount();
        for (int i = 0; i < pointCt; i++) pointListModel.addElement(makePointString(function.getX(i), function.getY(i)));
        styleCheckbox[function.getStyle()].setSelected(true);
        checkCanvas();
        if (onChange != null) onChange.compute();
    }

    /**
     * If an edit function has been specified (by startEdit()), this function
     * copies the data from the TableFunctionInput into that function, and
     * returns a pointer to that function. This ends the edit session, and the
     * internally stored pointer to the edit function is discarded. If no edit
     * function has been specified then a new TableFunction is created with the
     * data from the panel, and a pointer to the new function is returned. This
     * does not clear the data in the TableFunctionInput panel.
     * 
     * @return the edit function
     */
    public TableFunction finishEdit() {
        TableFunction func;
        if (editFunction == null) func = copyOfCurrentFunction(); else {
            editFunction.copyDataFrom(function);
            func = editFunction;
            editFunction = null;
        }
        return func;
    }

    /**
     * Discards the internal pointer to the edit function (specified by
     * startEdit()), if any. This does not clear the data in the
     * TableFunctionInput panel.
     */
    public void cancelEdit() {
        editFunction = null;
    }

    /**
     * Create a new TableFunction containing the data that is currently in the
     * TableFunctionInput panel, and return a pointer to that new function.
     * 
     * @return new table function
     */
    public TableFunction copyOfCurrentFunction() {
        TableFunction copy = new TableFunction();
        copy.copyDataFrom(function);
        copy.setName(function.getName());
        return copy;
    }

    /**
     * Specify a controller whose compute() method will be called whenever the
     * user edits the data in this TableFunctionInput panel. (Note that when the
     * user edits the function by dragging a point, the Controller is only
     * called once at the end of the drag.) If the specified Controller is null,
     * then no notification takes place.
     * 
     * @param c
     *            controller to use
     */
    public void setOnChange(Controller c) {
        onChange = c;
    }

    /**
     * Get the Controller that is notified when the user edits the data in this
     * panel. The return value can be null (the default), indicating that no
     * notification takes place.
     * 
     * @return controller that is notified
     */
    public Controller getOnChange() {
        return onChange;
    }

    private void deletePoint() {
        int index = pointList.getSelectedIndex();
        if (index >= 0) {
            pointListModel.removeElementAt(index);
            function.removePointAt(index);
            checkCanvas();
            if (onChange != null) onChange.compute();
        }
        deleteButton.setEnabled(false);
    }

    private void clearAllPoints() {
        function.removeAllPoints();
        pointListModel.removeAllElements();
        deleteButton.setEnabled(false);
        if (onChange != null) onChange.compute();
        checkCanvas();
    }

    private void addPoint() {
        double x, y;
        try {
            xInput.checkInput();
            x = xInput.getVal();
        } catch (WcmError e) {
            canvas.setErrorMessage(null, "The input for x does is not a legal real number.");
            xInput.requestFocus();
            xInput.selectAll();
            return;
        }
        try {
            yInput.checkInput();
            y = yInput.getVal();
        } catch (WcmError e) {
            canvas.setErrorMessage(null, "The input for y does is not a legal real number.");
            yInput.requestFocus();
            yInput.selectAll();
            return;
        }
        String str = makePointString(x, y);
        int index = function.findPoint(x);
        if (index >= 0 && y == function.getY(index)) {
            xInput.requestFocus();
            xInput.selectAll();
            return;
        }
        int newindex = function.addPoint(x, y);
        if (index >= 0) pointListModel.setElementAt(str, index); else pointListModel.add(newindex, str);
        deleteButton.setEnabled(pointList.getSelectedIndex() != -1);
        checkCanvas();
        if (onChange != null) onChange.compute();
        xInput.requestFocus();
        xInput.selectAll();
    }

    private String makePointString(double x, double y) {
        String X = NumUtils.realToString(x);
        String Y = NumUtils.realToString(y);
        if (X.length() < 11) X = "            ".substring(0, 11 - X.length()) + X;
        if (Y.length() < 11) Y = "            ".substring(0, 11 - Y.length()) + Y;
        return X + " " + Y;
    }

    private void selectPoint() {
        int index = pointList.getSelectedIndex();
        if (index >= 0) {
            xInput.setVal(function.getX(index));
            yInput.setVal(function.getY(index));
            yInput.requestFocus();
            yInput.selectAll();
        }
        deleteButton.setEnabled(index >= 0);
    }

    private void changeStyle() {
        int newstyle = 0;
        for (int i = 1; i < 5; i++) if (styleCheckbox[i].isSelected()) newstyle = i;
        if (function.getStyle() == newstyle) return;
        function.setStyle(newstyle);
        canvas.doRedraw();
        if (onChange != null) onChange.compute();
    }

    private void checkCanvas() {
        int ct = function.getPointCount();
        double newXmin = -1, newXmax = 1, newYmin = -1, newYmax = 1;
        if (ct > 0) {
            if (ct == 1) {
                newXmin = function.getX(0);
                if (Math.abs(newXmin) < 10000) {
                    newXmax = newXmin + 1;
                    newXmin -= 1;
                } else {
                    newXmax = newXmin - Math.abs(newXmin) / 10;
                    newXmin -= Math.abs(newXmin) / 10;
                }
            } else {
                newXmin = function.getX(0);
                newXmax = function.getX(ct - 1);
            }
            newYmin = function.getY(0);
            newYmax = newYmin;
            for (int i = 1; i < ct; i++) {
                double y = function.getY(i);
                if (y < newYmin) newYmin = y; else if (y > newYmax) newYmax = y;
            }
            double size = Math.abs(newYmin - newYmax);
            if (size < 1e-10 && Math.abs(newYmin) < 10000 && Math.abs(newYmax) < 10000) {
                newYmax += 1;
                newYmin -= 1;
            } else {
                newYmax += size * 0.15;
                newYmin -= size * 0.15;
            }
        }
        CoordinateRect coords = canvas.getCoordinateRect(0);
        double curSize = Math.abs(coords.getYmin() - coords.getYmax());
        double newSize = Math.abs(newYmax - newYmin);
        if (newXmax != coords.getXmax() || newXmin != coords.getXmin() || newSize > 1.3 * curSize || newSize < 0.5 * curSize || newYmax > coords.getYmax() - 0.1 * curSize || newYmin < coords.getYmin() + 0.1 * curSize) coords.setLimits(newXmin, newXmax, newYmin, newYmax);
        canvas.doRedraw();
    }

    /**
     * Leave a 3-pixel gap around the edges of the panel. Not meant to be called
     * directly.
     * 
     * @return inset
     */
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }

    /**
     * React when user clicks one of the buttons or presses return in one of the
     * input boxes. Not meant to be called directly.
     * 
     * @param evt
     *            event created when user clicks a button
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == deleteButton) deletePoint(); else if (source == clearButton) clearAllPoints(); else if (source == xInput) {
            yInput.requestFocus();
            yInput.selectAll();
        } else addPoint();
    }

    /**
     * React when user clicks on a point in the list of points or clicks one of
     * the radio buttons for specifying the style of the function. Not meant to
     * be called directly.
     * 
     * @param evt
     *            event created when user makes a selection
     */
    public void itemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) changeStyle();
    }

    /**
     * React when user clicks on a point in the list. Not meant to be called
     * directly
     * 
     * @param evt
     *            event created when list value changes
     */
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getSource() == pointList) selectPoint();
    }

    private int dragPoint = -1;

    private int startX, startY;

    private int prevY;

    private boolean moved;

    /**
     * Method required by the MouseListener interface. Defined here to support
     * dragging of points on the function's graph. Not meant to be called
     * directly.
     * 
     * @param evt
     *            event created when user presses mouse button
     */
    public void mousePressed(MouseEvent evt) {
        dragPoint = -1;
        moved = false;
        int ct = function.getPointCount();
        CoordinateRect coords = canvas.getCoordinateRect(0);
        for (int i = 0; i < ct; i++) {
            int x = coords.xToPixel(function.getX(i));
            int y = coords.yToPixel(function.getY(i));
            if (evt.getX() >= x - 3 && evt.getX() <= x + 3 && evt.getY() >= y - 3 && evt.getY() <= y + 3) {
                startX = evt.getX();
                prevY = startY = evt.getY();
                pointList.setSelectedIndex(i);
                selectPoint();
                dragPoint = i;
                return;
            }
        }
    }

    /**
     * Method required by the MouseListener interface. Defined here to support
     * dragging of points on the function's graph. Not meant to be called
     * directly.
     * 
     * @param evt
     *            event created when user releases mouse button
     */
    public void mouseReleased(MouseEvent evt) {
        if (dragPoint == -1) return;
        if (!moved) {
            dragPoint = -1;
            return;
        }
        mouseDragged(evt);
        pointListModel.setElementAt(makePointString(function.getX(dragPoint), function.getY(dragPoint)), dragPoint);
        pointList.setSelectedIndex(dragPoint);
        dragPoint = -1;
        if (onChange != null) onChange.compute();
    }

    /**
     * Method required by the MouseListener interface. Defined here to support
     * dragging of points on the function's graph. Not meant to be called
     * directly.
     * 
     * @param evt
     *            event created when user drags mouse
     */
    public void mouseDragged(MouseEvent evt) {
        if (dragPoint == -1 || prevY == evt.getY()) return;
        if (!moved && Math.abs(evt.getY() - startY) < 3) return;
        moved = true;
        int y = evt.getY();
        CoordinateRect coords = canvas.getCoordinateRect(0);
        if (y < coords.getTop() + 4) y = coords.getTop() + 4; else if (y > coords.getTop() + coords.getHeight() - 4) y = coords.getTop() + coords.getHeight() - 4;
        if (Math.abs(evt.getX() - startX) > 72) y = startY;
        if (y == prevY) return;
        prevY = y;
        function.setY(dragPoint, coords.pixelToY(prevY));
        yInput.setVal(function.getY(dragPoint));
        canvas.doRedraw();
    }

    /**
     * Empty method, required by the MouseListener interface.
     * 
     * @param evt
     *            the event
     */
    public void mouseClicked(MouseEvent evt) {
    }

    /**
     * Empty method, required by the MouseMotionListener interface.
     * 
     * @param evt
     *            the event
     */
    public void mouseEntered(MouseEvent evt) {
    }

    /**
     * Empty method, required by the MouseMotionListener interface.
     * 
     * @param evt
     *            the event
     */
    public void mouseExited(MouseEvent evt) {
    }

    /**
     * Empty method, required by the MouseMotionListener interface.
     * 
     * @param evt
     *            the event
     */
    public void mouseMoved(MouseEvent evt) {
    }

    private class Draw extends Drawable {

        public void draw(Graphics g, boolean coordsChanged) {
            int ct = function.getPointCount();
            if (ct == 0) return;
            g.setColor(Color.magenta);
            int xInt, yInt, aInt, bInt;
            double x, y, a, b;
            switch(function.getStyle()) {
                case TableFunction.SMOOTH:
                    {
                        if (ct > 1) {
                            try {
                                x = function.getX(0);
                                y = function.getVal(x);
                                xInt = coords.xToPixel(x);
                                yInt = coords.yToPixel(y);
                                int limit = coords.xToPixel(function.getX(ct - 1));
                                aInt = xInt;
                                while (aInt < limit) {
                                    aInt += 3;
                                    if (aInt > limit) aInt = limit;
                                    a = coords.pixelToX(aInt);
                                    b = function.getVal(a);
                                    bInt = coords.yToPixel(b);
                                    g.drawLine(xInt, yInt, aInt, bInt);
                                    xInt = aInt;
                                    yInt = bInt;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                case TableFunction.PIECEWISE_LINEAR:
                    {
                        x = function.getX(0);
                        xInt = coords.xToPixel(x);
                        y = function.getY(0);
                        yInt = coords.yToPixel(y);
                        for (int i = 1; i < ct; i++) {
                            a = function.getX(i);
                            aInt = coords.xToPixel(a);
                            b = function.getY(i);
                            bInt = coords.yToPixel(b);
                            g.drawLine(xInt, yInt, aInt, bInt);
                            xInt = aInt;
                            yInt = bInt;
                        }
                        break;
                    }
                case TableFunction.STEP:
                    {
                        x = function.getX(0);
                        xInt = coords.xToPixel(x);
                        for (int i = 0; i < ct; i++) {
                            if (i < ct - 1) {
                                double nextX = function.getX(i + 1);
                                a = (x + nextX) / 2;
                                x = nextX;
                            } else a = x;
                            aInt = coords.xToPixel(a);
                            y = function.getY(i);
                            yInt = coords.yToPixel(y);
                            g.drawLine(xInt, yInt, aInt, yInt);
                            xInt = aInt;
                        }
                        break;
                    }
                case TableFunction.STEP_LEFT:
                    {
                        x = function.getX(0);
                        xInt = coords.xToPixel(x);
                        for (int i = 1; i < ct; i++) {
                            a = function.getX(i);
                            aInt = coords.xToPixel(a);
                            y = function.getY(i - 1);
                            yInt = coords.yToPixel(y);
                            g.drawLine(xInt, yInt, aInt, yInt);
                            xInt = aInt;
                        }
                        break;
                    }
                case TableFunction.STEP_RIGHT:
                    {
                        x = function.getX(0);
                        xInt = coords.xToPixel(x);
                        for (int i = 1; i < ct; i++) {
                            a = function.getX(i);
                            aInt = coords.xToPixel(a);
                            y = function.getY(i);
                            yInt = coords.yToPixel(y);
                            g.drawLine(xInt, yInt, aInt, yInt);
                            xInt = aInt;
                        }
                        break;
                    }
            }
            for (int i = 0; i < ct; i++) {
                x = function.getX(i);
                y = function.getY(i);
                xInt = coords.xToPixel(x);
                yInt = coords.yToPixel(y);
                g.fillOval(xInt - 2, yInt - 2, 5, 5);
            }
        }
    }
}
