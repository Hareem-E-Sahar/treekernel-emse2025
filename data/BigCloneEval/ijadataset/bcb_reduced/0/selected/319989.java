package edu.ucla.stat.SOCR.core;

import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/** This class implements the main interface for SOCRDistributions PDF/CDF/MGF's */
public class SOCRDistributionFunctors extends SOCRApplet3 implements ActionListener, Observer {

    private GraphPanels graphPanel;

    private SOCRTextArea statusTextArea = new SOCRTextArea();

    JSplitPane container;

    protected Distribution dist;

    public static String ABOUT = "About";

    public static String HELP = "Help";

    public static String SNAPSHOT = "Snapshot";

    public JTextField leftCutOff, rightCutOff;

    public JLabel leftCutOffLabel;

    public JLabel rightCutOffLabel;

    private JFileChooser jfc;

    public Object getCurrentItem() {
        return dist;
    }

    public void initGUI() {
        controlPanelTitle = "SOCR Distribution Functors";
        implementedFunctor = "implementedFunctors.txt";
        addButton(ABOUT, "Learn More About This Distribution!", this);
        addButton(HELP, "How to Use the Distribution Applets?", this);
        addButton(SNAPSHOT, "Take a Snapshot and save this Applet as JPG image", this);
        leftCutOff = new JTextField(14);
        leftCutOffLabel = new JLabel("Left Cut Off");
        leftCutOffLabel.setLabelFor(leftCutOff);
        leftCutOff.setToolTipText("Left Cut Off for Computing Probability");
        leftCutOff.setActionCommand("leftCutOff");
        leftCutOff.addActionListener(this);
        addJTextField(leftCutOff, leftCutOffLabel);
        rightCutOff = new JTextField(14);
        rightCutOffLabel = new JLabel("Right Cut Off");
        rightCutOffLabel.setLabelFor(rightCutOff);
        rightCutOff.setToolTipText("Right Cut Off for Computing Probability");
        rightCutOff.setActionCommand("rightCutOff");
        rightCutOff.addActionListener(this);
        addJTextField(rightCutOff, rightCutOffLabel);
    }

    public void start() {
        container.setDividerLocation(0.6);
    }

    protected void itemChanged(String className) {
        try {
            dist = Distribution.getInstance(className);
            dist.addObserver(this);
            dist.initialize();
            graphPanel.setDistribution(dist);
            try {
                ValueSetter vsetter = dist.getValueSetter(0);
                if (vsetter != null) vsetter.setValue(vsetter.getValue());
            } catch (Exception e) {
            }
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(this, "Sorry, not implemented yet");
            e.printStackTrace();
        }
    }

    protected void functorChanged(String className) {
        try {
            if (className.equals("edu.ucla.stat.SOCR.core.DistributionGraphPanel")) graphPanel = new DistributionGraphPanel(this);
            if (className.equals("edu.ucla.stat.SOCR.core.MGFGraphPanel")) graphPanel = new MGFGraphPanel(this);
            if (className.equals("edu.ucla.stat.SOCR.core.PGFGraphPanel")) graphPanel = new PGFGraphPanel(this);
            if (container != null) {
                container.setDividerLocation(0.6);
                container.setLeftComponent(graphPanel);
            } else {
                container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, new JScrollPane(statusTextArea));
                fPresentPanel.setViewportView(container);
            }
            implementedFile = graphPanel.getPanelFile();
        } catch (Throwable e) {
            System.out.println("Error in DistributionFunctors");
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(ABOUT)) {
            try {
                getAppletContext().showDocument(new java.net.URL(dist.getOnlineDescription()), "SOCR: Distribution Online Help (Mathematica)");
            } catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
                e.printStackTrace();
            }
        } else if (evt.getActionCommand().equals(HELP)) {
            try {
                JOptionPane.showMessageDialog(this, dist.getLocalHelp());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
                e.printStackTrace();
            }
        } else if (evt.getActionCommand().equals("leftCutOff")) {
            try {
                double left = (new Double(leftCutOff.getText())).doubleValue();
                System.out.println("LeftCutOff set at: " + left);
                graphPanel.setLeftCutOff(left);
            } catch (NumberFormatException e) {
                double left = graphPanel.getLeftCutOff();
                leftCutOff.setText((new Double(left)).toString());
                JOptionPane.showMessageDialog(this, "You must enter a Double numeric value!!!");
                e.printStackTrace();
            }
        } else if (evt.getActionCommand().equals("rightCutOff")) {
            try {
                double right = (new Double(rightCutOff.getText())).doubleValue();
                System.out.println("RightCutOff set at: " + right);
                graphPanel.setRightCutOff(right);
            } catch (NumberFormatException e) {
                double right = graphPanel.getRightCutOff();
                rightCutOff.setText((new Double(right)).toString());
                JOptionPane.showMessageDialog(this, "You must enter a Double numeric value!!!");
                e.printStackTrace();
            }
        } else if (evt.getActionCommand().equals(SNAPSHOT)) {
            SwingUtilities.invokeLater(new Runnable() {

                java.awt.image.BufferedImage image;

                java.io.File f;

                String type;

                public void run() {
                    image = capture();
                    if (jfc == null) jfc = new JFileChooser(); else jfc.setVisible(true);
                    int option = jfc.showSaveDialog(null);
                    f = jfc.getSelectedFile();
                    jfc.setVisible(false);
                    if (!f.getName().endsWith(".jpg")) f = new java.io.File(f.getAbsolutePath() + ".jpg");
                    type = f.getName().substring(f.getName().lastIndexOf('.') + 1);
                    System.out.println("type " + type);
                    try {
                        javax.imageio.ImageIO.write(image, type, f);
                    } catch (java.io.IOException ioe) {
                        ioe.printStackTrace();
                        JOptionPane.showMessageDialog(null, ioe, "Error Writing File", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
    }

    /** This is a method used to capture the images of the applet for saving as JPG
	 */
    private java.awt.image.BufferedImage capture() {
        java.awt.Robot robot;
        try {
            robot = new java.awt.Robot();
        } catch (java.awt.AWTException e) {
            throw new RuntimeException(e);
        }
        java.awt.Rectangle screen = this.getContentPane().getBounds();
        java.awt.Point loc = screen.getLocation();
        SwingUtilities.convertPointToScreen(loc, this.getContentPane());
        screen.setLocation(loc);
        return robot.createScreenCapture(screen);
    }

    public void update(Observable o, Object arg) {
        graphPanel.setDistribution(dist);
    }

    /** updates the collected information of distribution */
    public void updateStatus() {
        if (dist == null) return;
        String mean = "Mean: " + format(dist.getMean());
        String median = "Median: " + format(dist.getMedian());
        String variance = "Variance: " + format(dist.getVariance());
        String std = "Standard Deviation: " + format(dist.getSD());
        String maxDensity = "Max Density: " + format(dist.getMaxDensity());
        String left = "Left: " + format(graphPanel.getLeftCDF());
        String right = "Right: " + format(graphPanel.getRightCDF());
        String between = "Between: " + format(graphPanel.getBetweenCDF());
        statusTextArea.setText(dist.getName() + "\n" + mean + "\t\t\t" + left + '\n' + median + "\t\t\t" + between + '\n' + variance + "\t\t\t" + right + '\n' + std + "\t\t\t" + maxDensity);
    }
}
