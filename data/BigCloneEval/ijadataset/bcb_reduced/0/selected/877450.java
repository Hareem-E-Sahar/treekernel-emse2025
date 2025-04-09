package edu.stanford.math.plex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * The <code>BCPlot</code> class does simple plotting of PersistenceIntervals.
 *
 * @version $Id$
 */
public class BCPlot extends JPanel {

    protected static final int CIRCLE_SIZE = 7;

    protected static final long serialVersionUID = 1L;

    protected boolean plotTypeScatter = false;

    protected Ruler fixedRuler;

    protected static final int FIXED_RULER_HEIGHT = 35;

    protected static final int MAX_GRID_HEIGHT_PER_PAGE = 1500;

    protected static final int MAX_DISPLAYABLE_BARS = MAX_GRID_HEIGHT_PER_PAGE - 1;

    protected static final int DEFAULT_BARS_PER_PAGE = 249;

    protected DrawingPane barcodePane;

    protected Action fileSaveMenuHandler;

    protected Rectangle baseRegionUsedForPlotLegendRulerLabels;

    protected int heightOfBaseRegionUsedForPlotLegendRulerLabels = 75;

    protected int widthOfSideRegionUsedForPlotLegendRulerLabels = 100;

    protected int leftMargin = widthOfSideRegionUsedForPlotLegendRulerLabels / 2;

    protected int rightMargin = widthOfSideRegionUsedForPlotLegendRulerLabels / 2;

    protected String legendMajorString = "", legendMinorString = null;

    protected int fullVisualizationWidth = 1200;

    protected int gridWidth;

    protected int gridHeight = MAX_GRID_HEIGHT_PER_PAGE;

    protected int fullVisualizationHeight;

    protected double[][] plotValue;

    protected double plotUpperBound;

    protected Color gridRulerFontColor, tickUpperBoundColor, gridTickMinorColor, gridTickMajorColor;

    protected Color upperBoundFontColor, labelFontColor, legendMinorFontColor, legendMajorFontColor;

    protected Color fixedRulerBorderColor, fixedRulerColor, barcodeBarColor;

    protected Color fixedRulerFontColor, fixedRulerMinorTickColor, fixedRulerMajorTickColor;

    protected Font labelFont, legendMinorFont, legendMajorFont, gridRulerFont, fixedRulerFont;

    protected FontMetrics legendMinorFontMetrics, legendMajorFontMetrics;

    protected FontMetrics fixedRulerFontMetrics, labelFontMetrics, gridRulerFontMetrics;

    protected int upperMargin;

    protected int barcodeBarHeight, barcodeBarSpacing;

    protected Boolean gridIsVisible;

    protected Boolean plotBeingSaved = false;

    protected double unitsPerPixel;

    protected double pixelsPerUnit;

    protected int tickMajorPlacement;

    protected int tickGap;

    protected BufferedImage bufferedImage;

    protected Boolean thereIsaBackgroundImage;

    protected String backgroundImageName;

    /**
	 * Returns the maximum number of barcodes that can be "plotted".
	 */
    public int getMaxDisplayableBars() {
        return MAX_DISPLAYABLE_BARS;
    }

    /**
	 * Sets the Major Legend
	 */
    protected void setMajorLegend(String s) {
        legendMajorString = s;
    }

    protected BCPlot() {
        throw new IllegalStateException("Do not use");
    }

    protected BCPlot(double[][] values, double upperBound, boolean forceScatter) {
        validate(values, upperBound);
        gridWidth = fullVisualizationWidth;
        if (forceScatter || (plotValue.length > MAX_DISPLAYABLE_BARS)) {
            plotTypeScatter = true;
            barcodeBarHeight = 0;
            barcodeBarSpacing = 0;
            upperMargin = 0;
            gridWidth = fullVisualizationWidth - rightMargin - leftMargin;
            gridHeight = gridWidth;
            fullVisualizationHeight = gridHeight + heightOfBaseRegionUsedForPlotLegendRulerLabels;
        } else {
            plotTypeScatter = false;
            fullVisualizationWidth += 20;
            if (plotValue.length <= DEFAULT_BARS_PER_PAGE) {
                barcodeBarHeight = 3;
                barcodeBarSpacing = 3;
                upperMargin = 3;
            } else if (plotValue.length <= 299) {
                barcodeBarHeight = 3;
                barcodeBarSpacing = 2;
                upperMargin = 2;
            } else if (plotValue.length <= 374) {
                barcodeBarHeight = 2;
                barcodeBarSpacing = 2;
                upperMargin = 2;
            } else if (plotValue.length <= 499) {
                barcodeBarHeight = 2;
                barcodeBarSpacing = 1;
                upperMargin = 1;
            } else if (plotValue.length <= 749) {
                barcodeBarHeight = 1;
                barcodeBarSpacing = 1;
                upperMargin = 1;
            } else if (plotValue.length < MAX_DISPLAYABLE_BARS) {
                barcodeBarHeight = 1;
                barcodeBarSpacing = 0;
                upperMargin = 1;
            } else if (plotValue.length == MAX_DISPLAYABLE_BARS) {
                barcodeBarHeight = 1;
                barcodeBarSpacing = 0;
                upperMargin = 0;
            }
            gridHeight = upperMargin + ((barcodeBarHeight + barcodeBarSpacing) * plotValue.length);
            fullVisualizationHeight = gridHeight + heightOfBaseRegionUsedForPlotLegendRulerLabels;
        }
        initJFrame();
    }

    protected static String illegal_interval_string(int i, double a, double b) {
        return String.format("Interval %d is [%.8g, %.8g), " + "not a non-empty subset of [0.0, %.8g)", i, a, b, Double.MAX_VALUE);
    }

    /**
	 * Validate the upperBound and the values used for the Barcode Plot
	 */
    protected void validate(double[][] numbers, double upperBound) {
        if (numbers == null) throw new IllegalArgumentException("No plot values");
        if ((upperBound >= Double.MAX_VALUE) || (upperBound <= 0)) {
            throw new IllegalArgumentException("upperBound, " + upperBound + ", must be in the range " + "[0.0, " + Double.MAX_VALUE + ")");
        }
        for (int i = 0; i < numbers.length; i++) {
            if ((numbers[i][0] < 0) || (numbers[i][0] >= Double.MAX_VALUE) || (numbers[i][0] > numbers[i][1]) || (numbers[i][1] > Double.POSITIVE_INFINITY)) throw new IllegalArgumentException(illegal_interval_string(i, numbers[i][0], numbers[i][1]));
        }
        plotUpperBound = upperBound;
        plotValue = numbers;
    }

    /**
	 *
	 */
    protected void initJFrame() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        JPanel instructionPanel = new JPanel(new GridLayout(0, 1));
        instructionPanel.setFocusable(true);
        barcodePane = new DrawingPane();
        barcodePane.setBackground(Color.white);
        barcodePane.setPreferredSize(new Dimension(fullVisualizationWidth, fullVisualizationHeight));
        JScrollPane barcodeScrollPane = new JScrollPane(barcodePane);
        barcodeScrollPane.setPreferredSize(new Dimension(fullVisualizationWidth + 20, fullVisualizationHeight));
        barcodeScrollPane.setViewportBorder(BorderFactory.createLineBorder(fixedRulerBorderColor));
        fixedRuler = new Ruler(barcodePane);
        barcodeScrollPane.setColumnHeaderView(fixedRuler);
        add(barcodeScrollPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fileSaveMenuHandler = new fileSaveMenuHandler("fileSaveMenuHandler", null, "Handles the File Menu Save action event", new Integer(2));
    }

    protected JMenuBar createMenuBar() {
        JMenuItem menuItem = null;
        JMenuBar menuBar;
        menuBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Save as");
        mainMenu.setMnemonic(KeyEvent.VK_S);
        mainMenu.setMnemonic(KeyEvent.VK_S);
        mainMenu.setIcon(new ImageIcon());
        String[] imageFileFormats = getFormats();
        for (int i = 0; i < imageFileFormats.length; i++) {
            menuItem = new JMenuItem(imageFileFormats[i]);
            menuItem.setIcon(null);
            menuItem.addActionListener(fileSaveMenuHandler);
            mainMenu.add(menuItem);
        }
        menuBar.add(mainMenu);
        return menuBar;
    }

    protected String[] getFormats() {
        String[] formats = ImageIO.getWriterFormatNames();
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }

    protected class fileSaveMenuHandler extends AbstractAction {

        protected static final long serialVersionUID = 1L;

        protected fileSaveMenuHandler(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        /**
		 * Action events handler
		 */
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (Arrays.asList(getFormats()).contains(cmd)) {
                File file = new File("untitledBarcode." + cmd);
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(file);
                int rval = fc.showSaveDialog(BCPlot.this);
                if (rval == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    barcodePane.saveImage(file, cmd);
                }
            }
        }
    }

    /**
	 * The component inside the scroll pane.
	 * This is were the BARCODE PLOT is set up displayed and saved from
	 */
    protected class DrawingPane extends JPanel {

        protected static final long serialVersionUID = 1L;

        public static final int ON_TOP = 0;

        public static final int ON_BOTTOM = 1;

        public static final int ON_LEFT = 3;

        public static final int ON_RIGHT = 4;

        protected DrawingPane() {
            super();
            thereIsaBackgroundImage = false;
            gridIsVisible = true;
            barcodeBarColor = Color.black;
            tickUpperBoundColor = new Color(0xff, 0x00, 0x00);
            gridTickMajorColor = new Color(0x0f, 0x85, 0xff);
            gridTickMinorColor = new Color(0xa9, 0xcc, 0xdf);
            fixedRulerColor = new Color(0x97, 0xd8, 0xff);
            fixedRulerMinorTickColor = Color.black;
            fixedRulerMajorTickColor = Color.black;
            fixedRulerBorderColor = Color.black;
            fixedRulerFont = new Font("sansserif", Font.PLAIN, 10);
            fixedRulerFontColor = Color.black;
            fixedRulerFontMetrics = this.getFontMetrics(fixedRulerFont);
            labelFont = new Font("sansserif", Font.BOLD, 14);
            labelFontColor = Color.red;
            labelFontMetrics = this.getFontMetrics(labelFont);
            legendMinorFont = new Font("sansserif", Font.BOLD, 10);
            legendMinorFontColor = Color.red;
            legendMinorFontMetrics = this.getFontMetrics(legendMinorFont);
            legendMajorFont = new Font("sansserif", Font.BOLD, 20);
            legendMajorFontColor = new Color(0x01, 0x01, 0xe5);
            legendMajorFontMetrics = this.getFontMetrics(legendMajorFont);
            gridRulerFont = new Font("sansserif", Font.PLAIN, 10);
            gridRulerFontColor = new Color(0x0f, 0x85, 0xff);
            gridRulerFontMetrics = this.getFontMetrics(gridRulerFont);
            upperBoundFontColor = new Color(0xff, 0x00, 0x00);
            baseRegionUsedForPlotLegendRulerLabels = new Rectangle(0, gridHeight, gridWidth, heightOfBaseRegionUsedForPlotLegendRulerLabels);
            unitsPerPixel = plotUpperBound / gridWidth;
            pixelsPerUnit = (double) gridWidth / plotUpperBound;
            tickMajorPlacement = 5;
            tickGap = (gridWidth / 100);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int xOffset = !plotTypeScatter ? 0 : leftMargin;
            g.setColor(Color.white);
            g.fillRect(0, 0, fullVisualizationWidth, fullVisualizationHeight);
            if (thereIsaBackgroundImage) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
            if (gridIsVisible) {
                drawGridLines(g);
            }
            g.setColor(gridTickMajorColor);
            g.drawLine(0 + xOffset, gridHeight + 1, gridWidth + xOffset, gridHeight + 1);
            drawRuler(g, DrawingPane.ON_BOTTOM);
            if (!plotTypeScatter) {
                int y, barcodeBarLength;
                g.setColor(barcodeBarColor);
                for (int i = 0; i < plotValue.length; i++) {
                    double a = plotValue[i][0];
                    double b = plotValue[i][1];
                    barcodeBarLength = (int) ((b - a) * pixelsPerUnit);
                    y = (i * (barcodeBarSpacing + barcodeBarHeight)) + upperMargin;
                    if (b > plotUpperBound) {
                        barcodeBarLength = (int) ((plotUpperBound - a) * pixelsPerUnit) + 10;
                    }
                    int x = (int) (a * pixelsPerUnit);
                    g.fillRect(x, y, barcodeBarLength, barcodeBarHeight);
                }
            } else {
                g.setColor(barcodeBarColor);
                int aCount = 0, bCount = 0;
                int x = 0, y = 0;
                for (int i = 0; i < plotValue.length; i++) {
                    double a = plotValue[i][0];
                    double b = plotValue[i][1];
                    y = gridWidth - (int) (b * pixelsPerUnit);
                    x = (int) (a * pixelsPerUnit);
                    if (a > plotUpperBound) {
                        aCount++;
                        bCount++;
                    } else if (b > plotUpperBound) {
                        g.setColor(Color.red);
                        g.fillOval(x + xOffset - (CIRCLE_SIZE / 2), -(CIRCLE_SIZE / 2), CIRCLE_SIZE, CIRCLE_SIZE);
                        bCount++;
                    } else {
                        g.setColor(barcodeBarColor);
                        g.fillOval(x + xOffset - (CIRCLE_SIZE / 2), y - (CIRCLE_SIZE / 2), CIRCLE_SIZE, CIRCLE_SIZE);
                    }
                }
                if ((aCount > 0 || bCount > 0) && (!plotBeingSaved)) {
                    String aText = "";
                    if (aCount > 0) {
                        if (aCount > 1) aText = "There are " + aCount + " X,Y points completely off the grid."; else aText = "One of the X,Y points is completely off the grid.";
                    }
                    String bText = "";
                    if ((bCount - aCount) > 0) {
                        if ((bCount - aCount) > 1) bText = "There are " + (bCount - aCount) + " points above the top of the grid."; else bText = "One of the X,Y points is above the top of the grid.";
                    }
                    centerText(aText, labelFont, labelFontMetrics, labelFontColor, bText, labelFont, labelFontMetrics, labelFontColor, g, (int) (gridWidth * 0.75) + xOffset, (int) (gridWidth * 0.75), 60, 40, 0);
                }
            }
            centerText(legendMajorString, legendMajorFont, legendMajorFontMetrics, legendMajorFontColor, legendMinorString, legendMinorFont, legendMinorFontMetrics, legendMinorFontColor, g, 0 + xOffset, gridHeight + (2 * gridRulerFontMetrics.getHeight()) + 3, baseRegionUsedForPlotLegendRulerLabels.width, baseRegionUsedForPlotLegendRulerLabels.height - (2 * gridRulerFontMetrics.getHeight()) - 3, 0);
        }

        protected void drawGridLines(Graphics g) {
            int xOffset = !plotTypeScatter ? 0 : leftMargin;
            int tickMinorCount = 0;
            for (int i = 0; i <= gridWidth; i += tickGap) {
                if (tickMinorCount % tickMajorPlacement == 0) {
                    if (i == gridWidth) {
                        g.setColor(tickUpperBoundColor);
                    } else {
                        g.setColor(gridTickMajorColor);
                    }
                } else {
                    g.setColor(gridTickMinorColor);
                }
                g.drawLine(i + xOffset, 0, i + xOffset, gridHeight);
                tickMinorCount++;
            }
            if (plotTypeScatter) {
                tickMinorCount = 0;
                for (int i = 0; i <= gridWidth; i += tickGap) {
                    if (tickMinorCount % tickMajorPlacement == 0) {
                        if (i == 0) {
                            g.setColor(tickUpperBoundColor);
                        } else {
                            g.setColor(gridTickMajorColor);
                        }
                    } else {
                        g.setColor(gridTickMinorColor);
                    }
                    g.drawLine(0 + xOffset, i, gridWidth + xOffset, i);
                    tickMinorCount++;
                }
                g.setColor(tickUpperBoundColor);
                g.drawLine(0 + xOffset, gridWidth, gridWidth + xOffset, 0);
            }
        }

        protected void drawRuler(Graphics g, int position) {
            int xOffset = !plotTypeScatter ? 0 : leftMargin;
            Font rf;
            FontMetrics rfm;
            Color rfc;
            int tickMajorLength;
            int tickMinorLength;
            int toggleLength;
            int rulerY;
            Color tMajorC, tMinorC;
            String text = null;
            boolean itShouldBePlacedHigh = true;
            int tickLineLength = 0;
            int fromY = 0, toY = 0, atY = 0, fromX = 0, toX = 0, atX = 0;
            double sr = 0;
            switch(position) {
                case ON_TOP:
                    rf = fixedRulerFont;
                    rfm = fixedRulerFontMetrics;
                    rfc = fixedRulerFontColor;
                    tickMajorLength = 5;
                    tickMinorLength = 1;
                    rulerY = 0;
                    tMinorC = fixedRulerMinorTickColor;
                    tMajorC = fixedRulerMajorTickColor;
                    toggleLength = 10;
                    break;
                case ON_BOTTOM:
                    rf = gridRulerFont;
                    rfm = gridRulerFontMetrics;
                    rfc = gridRulerFontColor;
                    tickMajorLength = 5;
                    tickMinorLength = 1;
                    rulerY = gridHeight + 1;
                    tMajorC = gridTickMajorColor;
                    tMinorC = gridTickMinorColor;
                    toggleLength = 10;
                    break;
                case ON_LEFT:
                    rf = gridRulerFont;
                    rfm = gridRulerFontMetrics;
                    rfc = gridRulerFontColor;
                    tickMajorLength = 5;
                    tickMinorLength = 1;
                    rulerY = gridHeight + 1;
                    tMajorC = gridTickMajorColor;
                    tMinorC = gridTickMinorColor;
                    toggleLength = 10;
                    break;
                case ON_RIGHT:
                default:
                    rf = fixedRulerFont;
                    rfm = fixedRulerFontMetrics;
                    rfc = fixedRulerFontColor;
                    tickMajorLength = 5;
                    tickMinorLength = 1;
                    rulerY = 0;
                    tMinorC = fixedRulerMinorTickColor;
                    tMajorC = fixedRulerMajorTickColor;
                    toggleLength = 10;
                    break;
            }
            int i = 0;
            for (; i <= gridWidth; i += tickGap) {
                if (i % tickMajorPlacement == 0) {
                    if (itShouldBePlacedHigh) {
                        tickLineLength = tickMajorLength + toggleLength;
                        itShouldBePlacedHigh = false;
                    } else {
                        tickLineLength = tickMajorLength;
                        itShouldBePlacedHigh = true;
                    }
                    switch(position) {
                        case ON_TOP:
                        case ON_BOTTOM:
                            text = String.format("%.6g", i * unitsPerPixel);
                            if (i != gridWidth) {
                                g.setColor(tMajorC);
                            } else {
                                g.setColor(tickUpperBoundColor);
                                rfc = upperBoundFontColor;
                            }
                            break;
                        case ON_LEFT:
                        case ON_RIGHT:
                            text = String.format("%.6g", (gridWidth - i) * unitsPerPixel);
                            if (i != 0) {
                                g.setColor(tMajorC);
                            } else {
                                g.setColor(tickUpperBoundColor);
                                rfc = upperBoundFontColor;
                            }
                            break;
                        default:
                            break;
                    }
                    if (text.indexOf('.') > -1) {
                        while (text.endsWith("0")) {
                            text = text.substring(0, text.length() - 1);
                        }
                        if (text.endsWith(".")) {
                            text = text.substring(0, text.length() - 1);
                        }
                    }
                } else {
                    g.setColor(tMinorC);
                    text = null;
                    tickLineLength = tickMinorLength;
                }
                if (tickLineLength != 0) {
                    switch(position) {
                        case ON_TOP:
                            fromY = FIXED_RULER_HEIGHT - 1;
                            toY = FIXED_RULER_HEIGHT - tickLineLength - 1;
                            atY = toY + 1 - rfm.getHeight();
                            fromX = i + xOffset + 1;
                            toX = i + xOffset + 1;
                            atX = toX;
                            sr = 0;
                            if (i == gridWidth) atX = atX - rfm.stringWidth(text) / 2;
                            break;
                        case ON_BOTTOM:
                            fromY = rulerY + 1;
                            toY = rulerY + tickLineLength;
                            atY = toY + 1;
                            fromX = i + xOffset;
                            toX = i + xOffset;
                            atX = toX;
                            sr = 0;
                            if (i == gridWidth) atX = atX - rfm.stringWidth(text) / 2;
                            break;
                        case ON_LEFT:
                            fromY = i;
                            toY = i;
                            atY = toY + 1;
                            fromX = xOffset;
                            toX = xOffset - tickLineLength;
                            atX = toX - 1;
                            sr = 1.5;
                            if (i == 0) atY = atY + rfm.stringWidth(text) / 2;
                            break;
                        case ON_RIGHT:
                        default:
                            break;
                    }
                    g.drawLine(fromX, fromY, toX, toY);
                    if (text != null) centerText(text, rf, rfm, rfc, null, null, null, null, g, atX, atY, 3, 3, sr);
                }
            }
        }

        /**
		 * Center text to be displayed within a given rectangle
		 */
        protected void centerText(String s1, Font f1, FontMetrics m1, Color color1, String s2, Font f2, FontMetrics m2, Color color2, Graphics g, int x, int y, int regionWidth, int regionHeight, double stringRotationPiRadians) {
            int stringWidth1 = 0, stringWidth2 = 0, x0 = 0, x1 = 0, y0 = 0, y1 = 0, fontAscent1 = 0, fontHeight1 = 0, fontHeight2 = 0;
            fontHeight1 = m1.getHeight();
            stringWidth1 = m1.stringWidth(s1);
            fontAscent1 = m1.getAscent();
            if (s2 != null) {
                fontHeight2 = m2.getHeight();
                stringWidth2 = m2.stringWidth(s2);
            }
            if ((stringRotationPiRadians == 0.5) || (stringRotationPiRadians == 1.5)) {
                y0 = y - (regionHeight - stringWidth1) / 2;
                y1 = y - (regionHeight - stringWidth2) / 2;
                if (s2 == null) {
                    x0 = x + (regionWidth - fontHeight1) / 2;
                } else {
                    x0 = x + ((regionWidth - (int) (fontHeight1 + (fontHeight2 * 1.2))) / 2);
                    x1 = x0 + (int) (fontHeight2 * 1.2);
                }
            } else {
                x0 = x + (regionWidth - stringWidth1) / 2;
                x1 = x + (regionWidth - stringWidth2) / 2;
                if (s2 == null) {
                    y0 = y + (regionHeight - fontHeight1) / 2 + fontAscent1;
                } else {
                    y0 = y + ((regionHeight - (int) (fontHeight1 + (fontHeight2 * 1.2))) / 2) + fontAscent1;
                    y1 = y0 + (int) (fontHeight2 * 1.2);
                }
            }
            if (g instanceof Graphics2D) {
                Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ((Graphics2D) g).addRenderingHints(hints);
            }
            Font f3 = f1.deriveFont(AffineTransform.getRotateInstance(stringRotationPiRadians * Math.PI));
            g.setFont(f3);
            g.setColor(color1);
            g.drawString(s1, x0, y0);
            if (s2 != null) {
                Font f4 = f2.deriveFont(AffineTransform.getRotateInstance(stringRotationPiRadians * Math.PI));
                g.setFont(f4);
                g.setColor(color2);
                g.drawString(s2, x1, y1);
            }
        }

        protected void saveImage(File file, String format) {
            Graphics g = null;
            if (thereIsaBackgroundImage) {
                try {
                    bufferedImage = ImageIO.read(new File(backgroundImageName));
                    g = bufferedImage.getGraphics();
                    g.drawImage(bufferedImage, 0, 0, null);
                } catch (IOException e1) {
                    bufferedImage = new BufferedImage(fullVisualizationWidth, fullVisualizationHeight, BufferedImage.TYPE_INT_BGR);
                    g = bufferedImage.createGraphics();
                }
            } else {
                bufferedImage = new BufferedImage(fullVisualizationWidth, fullVisualizationHeight, BufferedImage.TYPE_INT_BGR);
                g = bufferedImage.createGraphics();
            }
            plotBeingSaved = true;
            paintComponent(g);
            plotBeingSaved = false;
            try {
                ImageIO.write(bufferedImage, format, file);
            } catch (IOException e1) {
            }
        }
    }

    protected class Ruler extends JComponent {

        protected static final long serialVersionUID = 1L;

        protected DrawingPane dp;

        protected Ruler(DrawingPane bc) {
            dp = bc;
            setPreferredSize(new Dimension(gridWidth, FIXED_RULER_HEIGHT));
        }

        protected void paintComponent(Graphics g) {
            Rectangle graphicRegion = g.getClipBounds();
            g.setColor(fixedRulerColor);
            g.fillRect(graphicRegion.x, graphicRegion.y, graphicRegion.width, graphicRegion.height);
            dp.drawRuler(g, DrawingPane.ON_TOP);
        }
    }

    /**
	 * Create the plot window and display results. For thread safety, this
	 * method should be invoked from the event-dispatching thread.
	 */
    protected static void plot(String legend, double[][] values, double upperBound, boolean forceScatter) {
        JFrame frame = new JFrame(legend);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (values == null) throw new IllegalArgumentException("No barcodes to plot");
        BCPlot bcplot = new BCPlot(values, upperBound, forceScatter);
        bcplot.setMajorLegend(legend);
        frame.setJMenuBar(bcplot.createMenuBar());
        bcplot.setOpaque(true);
        frame.setContentPane(bcplot);
        frame.pack();
        frame.setVisible(true);
    }

    /**
	 * Make a barcode plot -- call via Plex.plot().
	 *
	 * <p>
	 *
	 * @param      label Window label.
	 * @param      intervals An array of double[2] of [x,y] intervals to plot.
	 * @param      upperBound Display the range from 0 to upperBound.
	 *
	 * @see        edu.stanford.math.plex.Plex#plot
	 */
    public static void doPlot(String label, double[][] intervals, double upperBound) {
        final double[][] values = intervals;
        final double ubound = upperBound;
        final String legend = label;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                plot(legend, values, ubound, false);
            }
        });
    }

    /**
	 * Make a barcode scatterplot -- call via Plex.scatter().
	 *
	 * <p>
	 *
	 * @param      label Window label.
	 * @param      intervals An array of double[2] of x,y points to plot.
	 * @param      upperBound Display the square from 0 to upperBound.
	 *
	 * @see        edu.stanford.math.plex.Plex#scatter
	 */
    public static void doScatter(String label, double[][] intervals, double upperBound) {
        final double[][] values = intervals;
        final double ubound = upperBound;
        final String legend = label;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                plot(legend, values, ubound, true);
            }
        });
    }
}
