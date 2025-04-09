package spec.lib.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import spec.lib.MathPlus;
import spec.lib.ui.FontChooser;
import spec.lib.ui.ViewerDialog;

/**
 * This class controls the look and feel of axes. 
 * It is designed to be used in conjunction with 
 * the Graph class and Dataset class for plotting 2D graphs.
 *
 * To work with the other classes a system of registration is used.
 * The axes have to be attached to the controlling Graph class
 * and the Dataset's have to be attached to both the Graph class
 * and the Axis class.
 */
public abstract class Axis extends Object {

    /**    Constant flagging Horizontal Axis */
    public static final int HORIZONTAL = 0;

    /** Constant flagging Vertical Axis  */
    public static final int VERTICAL = 1;

    /**    Constant flagging Axis position on the graph. 
   *    Left side => Vertical */
    public static final int LEFT = 2;

    /**    Constant flagging Axis position on the graph. 
   *    Right side => Vertical  */
    public static final int RIGHT = 3;

    /**    Constant flagging Axis position on the graph. 
   *    Top side => Horizontal */
    public static final int TOP = 4;

    /**    Constant flagging Axis position on the graph. 
   *    Bottom side => Horizontal  */
    public static final int BOTTOM = 5;

    /** Stroke style for drawing the tics */
    public static final BasicStroke TIC_STROKE = new BasicStroke(1.0f);

    /** Rectangle occupied by the axis in which it can draw itself */
    protected Rectangle2D.Float rect = new Rectangle2D.Float();

    /**
   * The orientation of the axis. Either Axis.HORIZONTAL or
   * Axis.VERTICAL
   */
    public int orientation;

    /** Handles the interactive setting of the axis attributes */
    public static EditAxis editAxis = new EditAxis();

    /**
   * The position of the axis. Either Axis.LEFT, Axis.RIGHT, Axis.TOP, or
   * Axis.BOTTOM
   */
    public int position;

    /** sets if the axis scale must consist only ot integers */
    public boolean forceInteger;

    /**
   * Vector containing a list of attached Datasets
   */
    protected Vector dataset = new Vector();

    /**
   * array to contain the axis labels
   */
    protected String labelString[] = null;

    /**
   * array to contain the actual values of the axis labels
   */
    protected double labelValue[] = null;

    /** Font in which the labels will be drawn */
    protected Font labelFont = null;

    /** Font in which the title text for the axis will be drawn */
    protected Font defaultAxisTitleFont = new Font("Helvetica", Font.BOLD, 16);

    protected Font axisTitleFont = defaultAxisTitleFont;

    /** Color of the Axis. */
    public Color defaultAxisColor = Color.black;

    public Color axisColor = defaultAxisColor;

    protected boolean underlineAxisTitle = true;

    /**
   * Double values representing the range of the data values of this axis (minimum, maximum).
   * This is the value used to scale data into the data window.
   * This is the value to alter to force a rescaling of the data window.
   */
    protected double dataMin;

    protected double dataMax;

    /** defaults to false. If true, axisRange and DataRange are equal
   * set to true for for XYtoZ Datasets */
    public boolean axisRangeEqualsDataRange = false;

    /** if <i>true</i>, draw minor tic marks */
    public boolean showMinorTics = true;

    /** if we draw minor tics, this defines the number of minor tics to draw btw 2 major tics */
    public int numMinorTics = 1;

    /** this defines the number of major tics to draw */
    public int numMajorTics = 5;

    /**
   * Default value <i>true</i>. Normally never changed. If set <i>false</I>
   * the Axis draw method exits without drawing the axis.
   * @see Axis#drawAxis()
   */
    protected boolean redraw = true;

    /**
   * Size in pixels of the major tick marks
   */
    protected int majorTicSize = 8;

    /** Size in pixels of the minor tick marks */
    protected int minorTicSize = 4;

    /**
   * Double values representing the actual range of the data values of the axis (minimum, maximum).
   * This is the value used to scale data into the data window.
   * This is the value to alter to force a rescaling of the data window.
   */
    protected double axisMin = 0.0;

    protected double axisMax = 10.0;

    /**
   * Double values representing the limits of the axes range (minimum, maximum).
   */
    protected double axisMinLimit = -1.0 * Double.MAX_VALUE;

    protected double axisMaxLimit = Double.MAX_VALUE;

    /** The Axis Title represented using a TextLayout */
    protected String axisTitleString = "An Axis";

    protected AttributedString attributedAxisTitle;

    protected TextLayout axisTitle;

    /** flags to decide whether or not to show the axis title  axis labels */
    public boolean showAxisTitle = true;

    public boolean showAxisLabels = true;

    protected boolean isAttributeSet = false;

    /**
   * Defines if axisRange and dataRange are equal
   */
    public Axis(boolean axisRangeEqualsDataRange) {
        super();
        this.axisRangeEqualsDataRange = axisRangeEqualsDataRange;
    }

    /**
   * Attach a Dataset for the Axis to manage.
   * @param d dataset to attach
   * @see graph.Dataset
   */
    public abstract void attachDataset(Dataset d);

    /**
   * Detach an attached Dataset
   * @param d dataset to detach
   * @see graph.Dataset
   */
    public abstract void detachDataset(Dataset d);

    /**
   * Detach All attached datasets.
   */
    public abstract void detachAll();

    /**
   * Set the axis position.
   * @param p Must be one of Axis.BOTTOM,
   * Axis.TOP, Axis.LEFT, Axis.RIGHT, Axis.HORIZONTAL or Axis.VERTICAL.
   * If one of the latter two are used then Axis.BOTTOM or 
   * Axis.LEFT is assumed.
   */
    public abstract void setPosition(int p);

    /**
   * Draw the axis ... just set its attributes
   * the passes rectangle will be the rectangle defining the bounds of the axis in the
   * graphAxisData it is painted in
   */
    public abstract void draw(Rectangle2D aRect);

    /** actually paints the axes on the component it is to be painted on, using the bounds
   * of its bounding "rect" member
   */
    public abstract void paint(Graphics g_orig);

    /** quick method to determine if the axis is vertical (position is Axis.LEFT, Axis.RIGHT or Axis.VERTICAL.)
   * ... or horizontal (Axis.BOTTOM, Axis.TOP or Axis.HORIZONTAL)
   */
    public boolean isVertical() {
        if (position == Axis.LEFT || position == Axis.RIGHT || position == Axis.VERTICAL) return true; else return false;
    }

    /** sets the axis title string and performs all the adjustment to make it take effect */
    public void setAxisTitleString(String str) {
        str = str.trim();
        if (str.length() < 1) str = "-";
        axisTitleString = str;
    }

    /** gets the "rect" that defines the bounds of this axis in GraphAxisData */
    public Rectangle2D.Float getBounds() {
        return rect;
    }

    /** get the component modifying the axis */
    public static EditAxis getAxisModifier() {
        return editAxis;
    }

    /**
   * Return the minimum value of All datasets attached to the axis.
   * @return Data minimum
   */
    public abstract double getDataMin();

    /**
   * Return the maximum value of All datasets attached to the axis.
   * @return Data maximum
   */
    public abstract double getDataMax();

    /**
   * Return the pixel equivalent of the parsed, passed data value relative to "rect"
   * Using the position of the axis and the maximum and minimum values,
   * convert the data value position into a pixel position
   * e.g the caller of this function, if an x axis, corresponds for the space
   * taken by the y axis before passing the value to this function
   * @param v data value to convert
   * @return equivalent pixel value
   * @see graph.Axis#getDouble( )
   */
    protected abstract int getPixel(double v);

    /**
   * Return the data value equivalent of the parsed, passed point relative to "rect"
   * Using the position of the axis and the maximum and minimum values,
   * convert the pixel position into a data value
   * e.g the caller of this function, if an x axis, corresponds for the space
   * taken by the y axis before passing the value to this function
   * @param i pixel value
   * @return equivalent data value
   * @see graph.Axis#getPixel( )
   * @throws an exception in case a double value which is outside the range of the
   * ... axis is computed to be returned
   */
    protected abstract double getDouble(int i) throws Exception;

    /**
   * Reset the range of the data (the minimum and maximum values) to the
   * default data values.
   */
    public void setDataRange() {
        dataMin = getDataMin();
        dataMax = getDataMax();
    }

    /** set the minimum value of the axis to the passed data value. */
    public void setAxisMin(double min) {
        if (min >= axisMinLimit) axisMin = min; else axisMin = axisMinLimit;
        if (forceInteger) axisMin = Math.floor(axisMin);
    }

    /** set the maximum value of the axis to the passed data value. */
    public void setAxisMax(double max) {
        if (max <= axisMaxLimit) axisMax = max; else axisMax = axisMaxLimit;
        if (forceInteger) axisMin = Math.ceil(axisMax);
    }

    /** set the minimum value of the axis to the passed data value. */
    protected void setAxisMinLimit(double minLimit) {
        if (axisRangeEqualsDataRange) {
            axisMinLimit = dataMin;
            return;
        }
        axisMinLimit = minLimit;
    }

    /** set the maximum value of the axis to the passed data value. */
    protected void setAxisMaxLimit(double maxLimit) {
        if (axisRangeEqualsDataRange) {
            axisMaxLimit = dataMax;
            return;
        }
        axisMaxLimit = maxLimit;
    }

    /** arbitrarily set the minimum value of the axis */
    protected void setAxisMinLimit() {
        if (axisRangeEqualsDataRange) {
            axisMinLimit = dataMin;
            return;
        }
        axisMinLimit = -1.0 * Double.MAX_VALUE;
    }

    /** arbitrarily set the maximum value of the axis */
    protected void setAxisMaxLimit() {
        if (axisRangeEqualsDataRange) {
            axisMaxLimit = dataMax;
            return;
        }
        axisMaxLimit = Double.MAX_VALUE;
    }

    /**
   * Reset the range of the axis (the minimum and maximum values) to the
   * passed data values.
   * ... still have to fix for a negative dataMax or dataMin
   */
    public void setAxisRange() {
        if (axisRangeEqualsDataRange) {
            setAxisMax(dataMax);
            setAxisMin(dataMin);
            return;
        }
        if (dataMax == 0.0) {
            axisMax = 0.0;
        } else {
            double maxExponent = (Math.floor(MathPlus.log10(Math.abs(dataMax))));
            double axisMaxInt = dataMax * Math.pow(10.0, (-1) * maxExponent);
            if (axisMaxInt >= 1.0 && axisMaxInt <= 2.0) axisMax = 2.0 * Math.pow(10.0, maxExponent); else if (axisMaxInt > 2 && axisMaxInt <= 5) axisMax = 5.0 * Math.pow(10.0, maxExponent); else if (axisMaxInt > 5 && axisMaxInt <= 10) axisMax = 10.0 * Math.pow(10.0, maxExponent);
        }
        if (dataMin == 0.0) {
            axisMin = 0.0;
        } else {
            double minExponent = (Math.floor(MathPlus.log10(Math.abs(dataMin))));
            double axisMinInt = dataMin * Math.pow(10.0, (-1) * minExponent);
            if (axisMinInt >= 1 && axisMinInt <= 2) axisMin = 1.0 * Math.pow(10.0, minExponent); else if (axisMinInt > 2 && axisMinInt <= 5) axisMin = 2.0 * Math.pow(10.0, minExponent); else if (axisMinInt > 5 && axisMinInt <= 10) axisMin = 5.0 * Math.pow(10.0, minExponent);
        }
        if (axisMin != 0) {
            double axisQuotient = axisMax / axisMin;
            if (axisQuotient > 10) {
                axisMin = 0;
            } else if (axisQuotient >= 1 && axisQuotient <= 10) {
            } else if (axisQuotient > 0 && axisQuotient < 1) {
                axisMax = 0;
            } else if (axisQuotient <= 0) {
                double axisRange = axisMax - axisMin;
                double axisRangeExponent = (Math.floor(MathPlus.log10(axisRange)));
                double axisRangeInt = axisRange * Math.pow(10.0, (-1) * axisRangeExponent);
                axisRange = Math.ceil(axisRangeInt) * Math.pow(10.0, axisRangeExponent);
                axisMin = axisMax - axisRange;
            }
        }
        if ((axisMin == 0.0) && (axisMax == 0.0)) {
            axisMin = -1.0;
            axisMax = 1.0;
        }
        setAxisMax(axisMax);
        setAxisMin(axisMin);
    }

    /**
   * Return the position of the Axis.
   * @return One of Axis.LEFT, Axis.RIGHT, Axis.TOP, or Axis.BOTTOM.
   */
    public int getAxisPos() {
        return position;
    }

    /**
   * Set Label Attributes including:
   * showAxisLabels, labelString[], labelValues[],
   * showMinorTics, numMinorTics, showAxisTitle.
   * **** NOTE: Nov 3, 1998: **** axisTitle is no more set here ... only in paint
   * ... This removes the need for a graphics context here **** 
   * Uses (70, 60, 45, 30) or (70, 55, 40, 20) algorithm
   * for vertical or horizontal axes
   * i.e. ...
   * ... HAVE TO FIX FOR LOG OF A -VE # ... E.G WHEN AXISMAX IS -VE
   */
    public void setAxisAttributes(float axisDepth, float axisLength) {
        double labelStart, labelStep;
        int exponent;
        setNumMajorTics(axisLength);
        showMinorTics = true;
        numMinorTics = 1;
        if (axisMax == 0.0) exponent = 0; else exponent = (int) (Math.floor(MathPlus.log10(Math.max(Math.abs(axisMax), Math.abs(axisMin)))));
        if ((exponent >= -2) && (exponent <= 3)) {
            exponent = 0;
        }
        String exponentString = String.valueOf(exponent);
        int exponentStringLength = exponentString.length();
        int axisTitleStringLength = axisTitleString.length();
        labelStart = axisMin * Math.pow(10, -1.0 * exponent);
        labelStep = (axisMax - axisMin) / (numMajorTics - 1) * Math.pow(10, -1.0 * exponent);
        labelString = new String[numMajorTics];
        labelValue = new double[numMajorTics];
        for (int i = 0; i < numMajorTics; i++) {
            labelValue[i] = labelStart + (i * labelStep);
            labelString[i] = String.valueOf(labelValue[i]);
            if (labelString[i].length() > 4) labelString[i] = labelString[i].substring(0, 4);
        }
        Font titleFont = axisTitleFont;
        if (axisDepth > 50) {
            showAxisLabels = true;
            showAxisTitle = true;
            labelFont = new Font("LucidaTypeWriter", Font.PLAIN, 12);
            if (titleFont == null) titleFont = new Font("Helvetica", Font.BOLD, 16);
            if (exponent == 0) {
                attributedAxisTitle = new AttributedString(axisTitleString);
                attributedAxisTitle.addAttribute(TextAttribute.FONT, titleFont, 0, axisTitleStringLength);
                if (underlineAxisTitle) attributedAxisTitle.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, axisTitleStringLength);
            } else {
                attributedAxisTitle = new AttributedString(axisTitleString + "( X 10 " + exponentString + " )");
                attributedAxisTitle.addAttribute(TextAttribute.FONT, titleFont, 0, axisTitleStringLength + 7);
                attributedAxisTitle.addAttribute(TextAttribute.FONT, new Font("Helvetica", Font.BOLD + Font.ITALIC, 10), axisTitleStringLength + 7, axisTitleStringLength + 7 + exponentStringLength);
                attributedAxisTitle.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, axisTitleStringLength + 7, axisTitleStringLength + 7 + exponentStringLength);
                attributedAxisTitle.addAttribute(TextAttribute.FONT, titleFont, axisTitleStringLength + 7 + exponentStringLength, axisTitleStringLength + 7 + exponentStringLength + 2);
                if (underlineAxisTitle) attributedAxisTitle.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, axisTitleStringLength + 7 + exponentStringLength + 2);
            }
        } else if (axisDepth > 40) {
            showAxisLabels = true;
            showAxisTitle = true;
            labelFont = new Font("LucidaTypeWriter", Font.PLAIN, 12);
            if (titleFont == null) titleFont = new Font("Helvetica", Font.BOLD, 12);
            if (exponent == 0) {
                attributedAxisTitle = new AttributedString(axisTitleString);
                attributedAxisTitle.addAttribute(TextAttribute.FONT, titleFont, 0, axisTitleStringLength);
                if (underlineAxisTitle) attributedAxisTitle.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, axisTitleStringLength);
            } else {
                attributedAxisTitle = new AttributedString(axisTitleString + "( X 10 " + exponentString + " )");
                attributedAxisTitle.addAttribute(TextAttribute.FONT, titleFont, 0, axisTitleStringLength + 7);
                attributedAxisTitle.addAttribute(TextAttribute.FONT, new Font("Helvetica", Font.BOLD + Font.ITALIC, 8), axisTitleStringLength + 7, axisTitleStringLength + 7 + exponentStringLength);
                attributedAxisTitle.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, axisTitleStringLength + 7, axisTitleStringLength + 7 + exponentStringLength);
                attributedAxisTitle.addAttribute(TextAttribute.FONT, titleFont, axisTitleStringLength + 7 + exponentStringLength, axisTitleStringLength + 7 + exponentStringLength + 2);
                if (underlineAxisTitle) attributedAxisTitle.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, axisTitleStringLength + 7 + exponentStringLength + 2);
            }
        } else if (axisDepth > 30) {
            showAxisLabels = true;
            showAxisTitle = false;
            labelFont = new Font("LucidaTypeWriter", Font.PLAIN, 8);
        } else {
            showAxisLabels = false;
            showAxisTitle = false;
            labelFont = new Font("LucidaTypeWriter", Font.PLAIN, 8);
        }
        isAttributeSet = true;
    }

    /**
   * sets the number of major tics
   */
    private void setNumMajorTics(float axisLength) {
        int suggestedNumMajorTics = (int) (axisLength / 40 + 1);
        double axisRange = axisMax - axisMin;
        if (axisRangeEqualsDataRange) {
            if ((int) axisRange % 10 == 0) numMajorTics = 10 + 1; else if ((int) axisRange % 5 == 0) numMajorTics = 5 + 1; else if ((int) axisRange % 2 == 0) numMajorTics = 2 + 1;
        } else {
            if (axisRange >= 0 && axisRange <= 1) {
                double axisRangeExponent = (Math.floor(MathPlus.log10(axisRange)));
                double axisRangeInt = axisRange * Math.pow(10.0, (-1) * axisRangeExponent);
                numMajorTics = (int) axisRangeInt + 1;
            } else if (axisRange >= 1 && axisRange <= 10) {
                numMajorTics = (int) axisRange + 1;
            } else if (axisRange > 10) {
                double axisRangeExponent = (Math.floor(MathPlus.log10(axisRange)));
                double axisRangeInt = axisRange * Math.pow(10.0, (-1) * axisRangeExponent);
                numMajorTics = (int) axisRangeInt + 1;
            }
        }
        while ((2 * numMajorTics - 1) <= suggestedNumMajorTics) {
            numMajorTics = numMajorTics * 2 - 1;
        }
        while (numMajorTics > suggestedNumMajorTics) {
            numMajorTics = (numMajorTics + 1) / 2;
        }
    }

    /**
   * Protected class to handle showing the dialog and
   * modifying the axis attributes
   */
    protected static class EditAxis implements ActionListener {

        protected JPanel contentPane = new JPanel();

        protected JLabel rangeLabel = new JLabel();

        protected JTextField minField = new JTextField();

        protected JTextField maxField = new JTextField();

        protected JTextField titleField = new JTextField();

        protected JTextField fontField = new JTextField();

        protected JCheckBox underlineChkBox = new JCheckBox("Underline");

        protected JButton colorButton = new JButton("Choose color and font");

        protected ImageIcon colorImageIcon = new ImageIcon();

        protected BufferedImage colorImage;

        protected Color selectedColor = null;

        protected Font selectedFont = null;

        protected Graphics colorGraphics;

        protected FontChooser fontChooser = Graph.fontChooser;

        private Axis currentAxis = null;

        public EditAxis() {
            super();
            fontField.setEnabled(false);
            colorImage = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
            colorImageIcon.setImage(colorImage);
            colorButton.setIcon(colorImageIcon);
            colorButton.addActionListener(this);
            updateLayout();
        }

        /** pops a dialog showing these components with the values initialized to the current values */
        public void show(Axis axis) {
            if (axis == null) return;
            currentAxis = axis;
            getValues();
            int selection = ViewerDialog.showOkCancelDialog(contentPane, "Edit the axis attributes");
            switch(selection) {
                case ViewerDialog.OK_OPTION:
                    setValues();
                    break;
                case ViewerDialog.CANCEL_OPTION:
                default:
                    break;
            }
            currentAxis = null;
        }

        private void getValues() {
            if (currentAxis == null) return;
            selectedColor = currentAxis.axisColor;
            selectedFont = currentAxis.axisTitleFont;
            underlineChkBox.setSelected(currentAxis.underlineAxisTitle);
            rangeLabel.setText(currentAxis.axisMinLimit + " to " + currentAxis.axisMaxLimit);
            minField.setText(String.valueOf(currentAxis.axisMin));
            maxField.setText(String.valueOf(currentAxis.axisMax));
            titleField.setText(currentAxis.axisTitleString);
            colorGraphics = colorImage.createGraphics();
            colorGraphics.setColor(currentAxis.axisColor);
            colorGraphics.fillRect(0, 0, colorImage.getWidth(), colorImage.getHeight());
            fontChooser.setToNull();
            selectedFont = currentAxis.axisTitleFont;
            if (selectedFont != null) {
                String fontAsString = selectedFont.getName() + " (" + selectedFont.getFontName() + ") " + selectedFont.getSize();
                fontField.setText(fontAsString);
            }
        }

        /** should not be called by other classes ... they should call show */
        private void setValues() {
            if (currentAxis == null) return;
            double min = 0.0, max = 0.0;
            try {
                min = (Double.valueOf(minField.getText())).doubleValue();
                max = (Double.valueOf(maxField.getText())).doubleValue();
                if (min >= max) throw new Exception("The axis minimum must be less than the axis maximum");
                currentAxis.setAxisMin(min);
                currentAxis.setAxisMax(max);
                currentAxis.axisTitleString = titleField.getText();
                if (selectedColor != null) currentAxis.axisColor = selectedColor;
                if (selectedFont != null) currentAxis.axisTitleFont = selectedFont;
                currentAxis.underlineAxisTitle = underlineChkBox.isSelected();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "The axis minimum and/or maximum must be numeric ", "Error changing axis attributes", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(null, e2.getMessage(), "Error changing axis attributes", JOptionPane.ERROR_MESSAGE);
            }
        }

        public JPanel getContentPanel() {
            return contentPane;
        }

        /** handles updating the layout for the contentPane */
        private void updateLayout() {
            contentPane.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(5, 5, 5, 5);
            c.gridx = c.gridy = 0;
            c.gridwidth = 2;
            c.gridheight = 1;
            c.fill = GridBagConstraints.CENTER;
            contentPane.add(new JLabel("The axis must be in the range: "), c);
            c.gridy = 1;
            contentPane.add(rangeLabel, c);
            c.gridwidth = c.gridheight = 1;
            c.gridx = 0;
            c.fill = GridBagConstraints.NONE;
            c.gridy = 2;
            contentPane.add(new JLabel("Minimum value"), c);
            c.gridy = 3;
            contentPane.add(new JLabel("Maximum value"), c);
            c.gridy = 4;
            contentPane.add(new JLabel("Axis Label"), c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy = 2;
            contentPane.add(minField, c);
            c.gridy = 3;
            contentPane.add(maxField, c);
            c.gridy = 4;
            contentPane.add(titleField, c);
            c.gridy = 5;
            c.gridx = 0;
            c.fill = GridBagConstraints.NONE;
            contentPane.add(new JLabel("Selected Font"), c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            contentPane.add(fontField, c);
            c.gridwidth = 1;
            c.gridy = 6;
            c.gridx = 0;
            c.fill = GridBagConstraints.NONE;
            contentPane.add(underlineChkBox, c);
            c.gridwidth = 1;
            c.gridy = 6;
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            contentPane.add(colorButton, c);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == colorButton) {
                if (currentAxis == null) return;
                fontChooser.showDialog();
                selectedFont = fontChooser.getNewFont();
                selectedColor = fontChooser.getNewColor();
                if (selectedColor == null) selectedColor = currentAxis.axisColor;
                if (selectedFont == null) selectedFont = currentAxis.axisTitleFont;
                colorGraphics = colorImage.createGraphics();
                colorGraphics.setColor(selectedColor);
                colorGraphics.fillRect(0, 0, colorImage.getWidth(), colorImage.getHeight());
                if (selectedFont != null) {
                    String fontAsString = selectedFont.getName() + " (" + selectedFont.getFontName() + ") " + selectedFont.getSize();
                    fontField.setText(fontAsString);
                }
            }
        }
    }
}
