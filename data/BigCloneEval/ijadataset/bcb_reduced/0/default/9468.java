import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JCheckBox;
import java.text.DecimalFormat;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

public class MainPanelGUI extends JPanel {

    private String[] proteaseChoices = { "Trypsin", "Chymotrypsin", "Proteinase K", "Thermolysin" };

    private HelpButtonSwingVersion help;

    private AboutButtonSwingVersion about;

    private static JTextArea inputArea;

    private JTextField lowerRange;

    private JTextField upperRange;

    private JComboBox proteaseBox;

    private TandemGraphGUI tandemGraph;

    private JLabel massDisplay;

    private ToggleFragmentButton blueBs;

    private ToggleFragmentButton redYs;

    private OutputGraphGUI outputGraph;

    private Ion ion;

    /**
     * The constructor uses a GridBagLayout to arrange the eight different
     * elements of the GUI- the label explaining the input box, the input box,
     * the label OR, the button to load a sequence, the protease selection
     * drop down box, the info label, the big graph and the small graph.
     */
    public MainPanelGUI() {
        super();
        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        setLayout(grid);
        constraints.insets = new Insets(1, 5, 1, 5);
        JPanel infoButtonsPanel = new JPanel();
        help = new HelpButtonSwingVersion();
        about = new AboutButtonSwingVersion();
        infoButtonsPanel.add(help);
        infoButtonsPanel.add(about);
        constraints.gridx = 0;
        constraints.gridy = 0;
        grid.setConstraints(infoButtonsPanel, constraints);
        add(infoButtonsPanel);
        JLabel inputLabel = new JLabel("Input protein sequence to be analyzed: ");
        constraints.gridy = 1;
        grid.setConstraints(inputLabel, constraints);
        add(inputLabel);
        inputArea = new JTextArea(7, 20);
        inputArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(inputArea);
        constraints.gridy = 2;
        grid.setConstraints(scrollPane, constraints);
        add(scrollPane);
        JLabel orLabel = new JLabel("OR");
        constraints.gridy = 3;
        grid.setConstraints(orLabel, constraints);
        add(orLabel);
        LoadButton loadButton = new LoadButton();
        constraints.gridy = 4;
        grid.setConstraints(loadButton, constraints);
        add(loadButton);
        JLabel proteaseLabel = new JLabel("Select protease: ");
        constraints.gridy = 5;
        grid.setConstraints(proteaseLabel, constraints);
        add(proteaseLabel);
        proteaseBox = new JComboBox(proteaseChoices);
        constraints.gridy = 6;
        grid.setConstraints(proteaseBox, constraints);
        add(proteaseBox);
        JLabel selectRangeLabel = new JLabel("Enter m/e range: ");
        constraints.gridy = 7;
        grid.setConstraints(selectRangeLabel, constraints);
        add(selectRangeLabel);
        JPanel rangeSelectionUpperPanel = new JPanel();
        lowerRange = new JTextField("0", 5);
        JLabel lowerRangeLabel = new JLabel("Lower Limit: ");
        rangeSelectionUpperPanel.add(lowerRangeLabel);
        rangeSelectionUpperPanel.add(lowerRange);
        constraints.gridy = 8;
        grid.setConstraints(rangeSelectionUpperPanel, constraints);
        add(rangeSelectionUpperPanel);
        JPanel rangeSelectionLowerPanel = new JPanel();
        upperRange = new JTextField("3000", 5);
        JLabel upperRangeLabel = new JLabel("Upper Limit: ");
        rangeSelectionLowerPanel.add(upperRangeLabel);
        rangeSelectionLowerPanel.add(upperRange);
        constraints.gridy = 9;
        grid.setConstraints(rangeSelectionLowerPanel, constraints);
        add(rangeSelectionLowerPanel);
        RunButton runButton = new RunButton();
        constraints.gridy = 10;
        grid.setConstraints(runButton, constraints);
        add(runButton);
        massDisplay = new JLabel("<html> Mass: N/A <P>");
        constraints.gridy = 11;
        grid.setConstraints(massDisplay, constraints);
        add(massDisplay);
        blueBs = new ToggleFragmentButton("B fragments", true);
        constraints.gridy = 12;
        grid.setConstraints(blueBs, constraints);
        add(blueBs);
        redYs = new ToggleFragmentButton("Y fragments", true);
        constraints.gridy = 13;
        grid.setConstraints(redYs, constraints);
        add(redYs);
        tandemGraph = new TandemGraphGUI();
        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.gridheight = 8;
        constraints.weighty = 1.0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        grid.setConstraints(tandemGraph, constraints);
        add(tandemGraph);
        outputGraph = new OutputGraphGUI(this);
        constraints.gridy = 8;
        constraints.gridheight = 6;
        grid.setConstraints(outputGraph, constraints);
        add(outputGraph);
    }

    /**
     * runTandem changes the information displayed in the infoScreen Jlabel
     * when a user clicks on a peak in the OutputGraphGUI. It also alerts
     * TandemGraphGUI that there is peptide sequencing to be done.
     *
     * @param ion The ion the user selected for peptide sequencing.
     */
    public void runTandem(Ion selected) {
        ion = selected;
        DecimalFormat massFormat = new DecimalFormat("##.##");
        String mass = massFormat.format(ion.getMass());
        massDisplay.setText("<html> Mass: " + mass);
        tandemGraph.drawSequencePeaks(ion);
    }

    /**
     * Inner class that loads a protein file selected by the user.
     */
    private class LoadButton extends JButton implements ActionListener {

        /**
         * Constructor passes the String to be displayed on the button to
         * JButton's constructor and registers itself as its own actionListener.
         */
        public LoadButton() {
            super("Load Sequence From File");
            addActionListener(this);
        }

        /**
         * The actionPerformed method is called when the user clicks on the button.
         * It opens a JFileChooser so the user may select which file they would
         * like to obtain sequence information from.
         *
         * @param e Unused.
         */
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("FASTA files", "fasta");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String parsedSequence = FastaParser.parse(chooser.getSelectedFile());
                inputArea.setText(parsedSequence);
            }
        }
    }

    /**
     * Inner class that starts the simulation running once the user clicks on it.
     */
    private class RunButton extends JButton implements ActionListener {

        /**
         * Constructor passes the String to be displayed on the button to
         * JButton's constructor and registers itself as its own actionListener.
         */
        public RunButton() {
            super("Run Spectrum");
            addActionListener(this);
        }

        /**
         * The actionPerformed method is called when the user clicks on the button.
         * It begins the simulation.
         *
         * @param e Unused.
         */
        public void actionPerformed(ActionEvent e) {
            Spectrometer.runAnalysis(inputArea.getText(), outputGraph, proteaseBox.getSelectedItem().toString());
        }
    }

    /**
     * Inner class that repaints the TandemGraphGUI when the user clicks on it.
     */
    private class ToggleFragmentButton extends JCheckBox implements ItemListener {

        /**
         * Constructor passes the String to be displayed on the button to
         * JCheckBox's constructor and registers itself as its own actionListener.
         */
        public ToggleFragmentButton(String text, boolean state) {
            super(text, state);
            addItemListener(this);
        }

        /**
         * The actionPerformed method is called when the user clicks on the button.
         * It repaints TandemGraphGUI.
         *
         * @param e Unused.
         */
        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();
            if (source == blueBs) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    tandemGraph.setBlueBs(false);
                } else {
                    tandemGraph.setBlueBs(true);
                }
            } else {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    tandemGraph.setRedYs(false);
                } else {
                    tandemGraph.setRedYs(true);
                }
            }
            if (ion != null) {
                tandemGraph.drawSequencePeaks(ion);
            }
        }
    }

    /**
     * The ProteinFrame class from the Electro2D simulation of JBioFramework
     * calls this to set the input area's text to the sequence of a protein the
     * user clicked on in the gel canvas.
     *
     * @return The JTextArea that holds a protein's sequence.
     */
    public static JTextArea getInputArea() {
        return inputArea;
    }

    /**
     * Called by OutputGraphGUI's setPeaks method to sort out which ion peaks to
     * display based on the user specified m/e range.
     * 
     * @return The lower limit of the user selected range.
     */
    public double getLowerLimit() {
        double lower;
        if (lowerRange.getText().contains(",")) {
            String noComma = lowerRange.getText();
            int index = noComma.indexOf(",");
            noComma = noComma.substring(0, index) + noComma.substring(index + 1, noComma.length());
            try {
                lower = Double.valueOf(noComma);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Did not recognize Lower Limit as a number. Using default lower limit of 0.");
                lowerRange.setText("0");
                return 0;
            }
        } else {
            try {
                lower = Double.valueOf(lowerRange.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Did not recognize Lower Limit as a number. Using default lower limit of 0.");
                lowerRange.setText("0");
                return 0;
            }
        }
        if (lower < 0 || lower > 20000) {
            lower = 0;
            JOptionPane.showMessageDialog(null, "Lower Limit out of bounds (0 to 20,000). Set to default of 0.");
            lowerRange.setText("0");
        }
        if (lower > getUpperLimit()) {
            lower = 0;
            JOptionPane.showMessageDialog(null, "Lower Limit is higher than Upper Limit. Set to default of 0.");
            lowerRange.setText("0");
        }
        return lower;
    }

    /**
     * Called by OutputGraphGUI's setPeaks method to sort out which ion peaks to
     * display based on the user specified m/e range.
     *
     * @return The upper limit of the user selected range.
     */
    public double getUpperLimit() {
        double upper;
        if (upperRange.getText().contains(",")) {
            String noComma = upperRange.getText();
            int index = noComma.indexOf(",");
            noComma = noComma.substring(0, index) + noComma.substring(index + 1, noComma.length());
            try {
                upper = Double.valueOf(noComma);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Did not recognize Upper Limit as a number. Using default upper limit of 3000.");
                upperRange.setText("3000");
                return 0;
            }
        } else {
            try {
                upper = Double.valueOf(upperRange.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Did not recognize Upper Limit as a number. Using default upper limit of 3000.");
                upperRange.setText("3000");
                return 3000;
            }
        }
        if (upper < 0 || upper > 20000) {
            upper = 3000;
            JOptionPane.showMessageDialog(null, "Upper Limit out of bounds (0 to 20,000). Set to default of 3000.");
            upperRange.setText("3000");
        }
        return upper;
    }
}
