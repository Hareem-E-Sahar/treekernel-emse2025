package edu.ucla.stat.SOCR.core;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.Observable;
import java.util.Observer;

/** This class implements the main interface for SOCRDistributions */
public class SOCRDistributions extends SOCRApplet implements ActionListener, DocumentListener, Observer {

    private GraphPanel graphPanel;

    private SOCRTextArea statusTextArea = new SOCRTextArea();

    JSplitPane container;

    protected Distribution dist;

    private String defaultSelectedDistribution = "Normal Distribution";

    public boolean showAboutButtons = true;

    public static String ABOUT = "About";

    public static String HELP = "Help";

    public static String SNAPSHOT = "Snapshot";

    public JTextField leftCutOff, rightCutOff;

    public JLabel leftCutOffLabel;

    public JLabel rightCutOffLabel;

    private JFileChooser jfc;

    private Observable observable = new Observable() {

        public void notifyObservers() {
            setChanged();
            super.notifyObservers(SOCRDistributions.this);
        }
    };

    public Object getCurrentItem() {
        return dist;
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void update(Observable o, Object arg) {
        graphPanel.setDistribution(dist);
        valueChanged(o, arg);
    }

    public void valueChanged() {
    }

    public void valueChanged(Observable o, Object arg) {
        valueChanged();
    }

    public void initGUI() {
        controlPanelTitle = "SOCR Distributions";
        implementedFile = "implementedDistributions.txt";
        if (showAboutButtons) {
            addButton(ABOUT, "Learn More About This Distribution!", this);
            addButton(HELP, "How to Use the Distribution Applets?", this);
            addButton(SNAPSHOT, "Take a Snapshot and save this Applet as JPG image", this);
        }
        graphPanel = new GraphPanel(this);
        container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, new JScrollPane(statusTextArea));
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
        fPresentPanel.setViewportView(container);
        setSelectedApplication(defaultSelectedDistribution);
        statusTextArea.setToolTipText("The (red-shaded) BETWEEN, and (unshaded) LEFT and " + " RIGHT areas show the probabilities P(LEFT<X<=RIGHT), P(X<LEFT), " + "P(X>=RIGHT), respectively.");
        fControlPanel.setToolTipText("Select a Probability Distribution (drop-down list)" + ", if appropriate choose distribution parameters.");
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
            observable.notifyObservers();
            try {
                ValueSetter vsetter = dist.getValueSetter(0);
                if (vsetter != null) vsetter.setValue(vsetter.getValue());
            } catch (Exception e) {
            }
            leftCutOff.setText(null);
            rightCutOff.setText(null);
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(this, "Sorry, not implemented yet");
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
        String between = "Between (Red-Shaded): " + format(graphPanel.getBetweenCDF());
        statusTextArea.setText("Distribution Properties" + "\t\t\t Probabilities\n" + dist.getName() + "\n" + mean + "\t\t\t" + left + '\n' + median + "\t\t\t" + between + '\n' + variance + "\t\t\t" + right + '\n' + std + "\n" + maxDensity);
    }

    public void setDefaultSelectedDistribution(String distName) {
        defaultSelectedDistribution = distName;
    }

    public String getDefaultSelectedDistribution() {
        return defaultSelectedDistribution;
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void textChanged(DocumentEvent evt) {
        if (evt.getDocument().equals(leftCutOff.getDocument())) {
            try {
                if (leftCutOff.getText().equals("") || leftCutOff.getText().equals("-")) return;
                double left = (new Double(leftCutOff.getText())).doubleValue();
                graphPanel.setLeftCutOff(left);
            } catch (NumberFormatException e) {
                double left = graphPanel.getLeftCutOff();
                JOptionPane.showMessageDialog(this, "You must enter a Double numeric value!!!");
                e.printStackTrace();
            }
        } else if (evt.getDocument().equals(rightCutOff.getDocument())) {
            try {
                if (rightCutOff.getText().equals("") || rightCutOff.getText().equals("-")) return;
                double right = (new Double(rightCutOff.getText())).doubleValue();
                graphPanel.setRightCutOff(right);
            } catch (NumberFormatException e) {
                double right = graphPanel.getRightCutOff();
                JOptionPane.showMessageDialog(this, "You must enter a Double numeric value!!!");
                e.printStackTrace();
            }
        }
    }
}
