import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author  labuser
 */
public class PMCopy extends javax.swing.JFrame implements Runnable {

    /** Creates new form ParameterSolutions */
    public PMCopy() {
        initComponents();
    }

    private void initComponents() {
        narr = new NarrWriter(narrOut);
        openButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        log = new javax.swing.JTextArea();
        npopConf = new javax.swing.JButton();
        sigmaConf = new javax.swing.JButton();
        omegaConf = new javax.swing.JButton();
        driftConf = new javax.swing.JButton();
        runAll = new javax.swing.JButton();
        critSelector = new javax.swing.JComboBox();
        criterion = new javax.swing.JLabel();
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                printResults();
                if (narr != null) narr.close();
                System.exit(0);
            }
        });
        setTitle("Parameter Solutions");
        openButton.setText("Open Fasta File");
        openButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        log.setColumns(20);
        log.setEditable(false);
        log.setRows(5);
        log.setDoubleBuffered(true);
        jScrollPane1.setViewportView(log);
        npopConf.setText("Run Npop Confidence Interval");
        npopConf.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                npopConfActionPerformed(evt);
            }
        });
        sigmaConf.setText("Run Sigma Confidence Interval");
        sigmaConf.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sigmaConfActionPerformed(evt);
            }
        });
        omegaConf.setText("Run Omega Confidence Interval");
        omegaConf.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omegaConfActionPerformed(evt);
            }
        });
        driftConf.setText("Run Drift Confidence Interval");
        driftConf.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                driftConfActionPerformed(evt);
            }
        });
        runAll.setText("Run Everything from a Fasta File");
        runAll.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runAllActionPerformed(evt);
            }
        });
        critSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "auto", "5x", "2x", "1.5x", "1.10x", "1.05x" }));
        criterion.setText("Select Criterion");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(runAll).addGroup(layout.createSequentialGroup().addGap(43, 43, 43).addComponent(openButton)).addGroup(layout.createSequentialGroup().addGap(42, 42, 42).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(10, 10, 10).addComponent(critSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(criterion)))).addGap(53, 53, 53).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(omegaConf).addComponent(sigmaConf).addComponent(npopConf).addComponent(driftConf)))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(omegaConf).addComponent(openButton)).addGap(14, 14, 14).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(sigmaConf).addComponent(runAll)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(15, 15, 15).addComponent(npopConf).addGap(16, 16, 16).addComponent(driftConf)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(criterion).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(critSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))).addGap(19, 19, 19).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(35, 35, 35)));
        pack();
    }

    private void critSelectorActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void runAllActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == runAll) {
            int returnVal = fc.showOpenDialog(PMCopy.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputFile = fc.getSelectedFile();
                if (!BinningFasta.verifyInputFile(inputFile)) {
                    log.append("That is not a valid fasta file, please choose" + " a properly formatted fasta file.\n");
                    return;
                }
                (new Thread() {

                    public void run() {
                        log.append("Opening: " + inputFile.getName() + "\n");
                        narr.println("Opening: " + inputFile.getName() + "\n");
                        File startingFile = removeOutgroup(inputFile);
                        int userChoice = userSortPercentage();
                        values = new BinningAndFred(startingFile, log, narr, userChoice);
                        values.run();
                        hClimbResult = null;
                        try {
                            hClimbResult = hillClimbing(values.getValue());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runNpopConfidenceInterval();
                        runSigmaConfidenceInterval();
                        runOmegaConfidenceInterval();
                        runDriftConfidenceInterval();
                        readyForCI = true;
                        printResults();
                    }
                }).start();
            } else {
                log.append("Dialog Cancelled by User.\n");
            }
        }
    }

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(PMCopy.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputFile = fc.getSelectedFile();
                if (!BinningFasta.verifyInputFile(inputFile)) {
                    log.append("That is not a valid fasta file, please choose" + " a properly formatted fasta file.\n");
                    return;
                }
                thread = new Thread(this);
                thread.start();
            } else {
                log.append("Dialog Cancelled by User.\n");
            }
        }
    }

    private void sigmaConfActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == sigmaConf) {
            if (readyForCI) {
                (new Thread() {

                    public void run() {
                        FredOutVal result = runSigmaConfidenceInterval();
                        if (result != null) {
                            if (userApproval(result)) {
                                try {
                                    readyForCI = false;
                                    hClimbResult = hillClimbing(result);
                                    readyForCI = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            } else log.append("You must run binning and hillclimbing from a " + "fasta file before you can run a confidence interval.\n");
        }
    }

    private void omegaConfActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == omegaConf) {
            if (readyForCI) {
                (new Thread() {

                    public void run() {
                        FredOutVal result = runOmegaConfidenceInterval();
                        if (result != null) {
                            if (userApproval(result)) {
                                try {
                                    readyForCI = false;
                                    hClimbResult = hillClimbing(result);
                                    readyForCI = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            } else log.append("You must run binning and hillclimbing from a " + "fasta file before you can run a confidence interval.\n");
        }
    }

    private void npopConfActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == npopConf) {
            if (readyForCI) {
                (new Thread() {

                    public void run() {
                        FredOutVal result = runNpopConfidenceInterval();
                        if (result != null) {
                            if (userApproval(result)) {
                                try {
                                    readyForCI = false;
                                    hClimbResult = hillClimbing(result);
                                    readyForCI = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            } else log.append("You must run binning and hillclimbing from a " + "fasta file before you can run a confidence interval.\n");
        }
    }

    private void driftConfActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == driftConf) {
            if (readyForCI) {
                (new Thread() {

                    public void run() {
                        FredOutVal result = runDriftConfidenceInterval();
                        if (result != null) {
                            if (userApproval(result)) {
                                try {
                                    readyForCI = false;
                                    hClimbResult = hillClimbing(result);
                                    readyForCI = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            } else log.append("You must run binning and hillclimbing from a " + "fasta file before you can run a confidence interval.\n");
        }
    }

    public void run() {
        log.append("Opening: " + inputFile.getName() + "\n");
        File startingFile = removeOutgroup(inputFile);
        int userChoice = userSortPercentage();
        values = new BinningAndFred(startingFile, log, narr, userChoice);
        values.run();
        hClimbResult = null;
        try {
            hClimbResult = hillClimbing(values.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        readyForCI = true;
    }

    /**
     * Remove the outgroup (the first gene) from the input file
     * Note: all input files must have the first gene be an outgroup! this may
     * be generated by whoever is using the program; if there is no outgroup,
     * the first sequence in the file will be removed
     */
    private File removeOutgroup(File inFile) {
        try {
            BufferedReader input = new BufferedReader(new FileReader(inFile));
            String line = input.readLine();
            ArrayList data = new ArrayList();
            while (line != null) {
                data.add(line);
                line = input.readLine();
            }
            input.close();
            BufferedWriter out = new BufferedWriter(new FileWriter(MasterVariables.NO_OUTGROUP));
            for (int i = 2; i < data.size(); i++) {
                out.write((String) data.get(i));
                out.newLine();
            }
            out.close();
            return MasterVariables.NO_OUTGROUP;
        } catch (IOException e) {
            e.printStackTrace();
            return inFile;
        }
    }

    /**
     * Runs the drift confidence interval
     * @return null if no better value is found, but if during the confidence
     * interval a set of parameters with a better likelihood is found (via)
     * hillclimbing it will return that value
     */
    public FredOutVal runDriftConfidenceInterval() {
        log.append("Starting drift confidence interval: \n");
        narr.println("Starting drift confidence interval: ");
        ArrayList bins = values.getBins();
        int[] sequenceVals = values.getSeqVals();
        File driftIn = new File("driftIn.dat");
        File driftOut = new File("driftOut.dat");
        String cmdDrift = "driftCI.exe";
        DriftConfidence drift = new DriftConfidence(hClimbResult, values, narr, log, driftIn, driftOut, cmdDrift, true);
        FredOutVal driftRes = drift.lowerBound();
        log.append("The result from driftCI lower bound: \n" + driftRes.toString() + "\n");
        narr.println("The result from driftCI:");
        narr.println(driftRes.toString());
        driftConfidenceInterval = 1 / driftRes.getDrift();
        return null;
    }

    /**
     * Runs the omega confidence interval
     * @return null if no value with a better likelihood was found, otherwise
     * it returns the value with that better likelihood
     */
    public FredOutVal runOmegaConfidenceInterval() {
        log.append("Starting omega confidence interval: \n");
        narr.println("Starting omega confidence interval: ");
        ArrayList bins = values.getBins();
        int[] sequenceVals = values.getSeqVals();
        File omegaIn = new File("omegaIn.dat");
        File omegaOut = new File("omegaOut.dat");
        String cmdOmega = "omegaCI.exe";
        OmegaConfidence omega = new OmegaConfidence(hClimbResult, values, narr, log, omegaIn, omegaOut, cmdOmega, true);
        FredOutVal omegaRes = omega.lowerBound();
        log.append("The result from omegaCI lower bound: \n" + omegaRes.toString() + "\n");
        narr.println("The result from omegaCI:");
        narr.println(omegaRes.toString());
        FredOutVal omegaUpRes = omega.upperBound();
        log.append("The result from omegaCI upper bound: \n" + omegaUpRes.toString() + "\n");
        narr.println("The result from omegaCI upper bound:");
        narr.println(omegaUpRes.toString());
        omegaConfidenceInterval = new double[2];
        omegaConfidenceInterval[0] = omegaRes.getOmega();
        omegaConfidenceInterval[1] = omegaUpRes.getOmega();
        return null;
    }

    /**
     * Runs the sigma confidence interval
     * @return null if no value with a better likelihood than the one found
     * in hillclimbing, otherwise it returns that better value
     */
    private FredOutVal runSigmaConfidenceInterval() {
        log.append("Starting sigma confidence interval: \n");
        narr.println("Starting sigma confidence interval: ");
        ArrayList bins = values.getBins();
        int[] sequenceVals = values.getSeqVals();
        File sigmaIn = new File("sigmaIn.dat");
        File sigmaOut = new File("sigmaOut.dat");
        String cmdSigma = "sigmaCI.exe";
        SigmaConfidence sigma = new SigmaConfidence(hClimbResult, values, narr, log, sigmaIn, sigmaOut, cmdSigma, true);
        FredOutVal sigmaRes = sigma.lowerBound();
        log.append("The result from the lower bound of sigmaCI: \n" + sigmaRes.toString() + "\n");
        narr.println("The result from the lower bound of sigmaCI:");
        narr.println(sigmaRes.toString());
        FredOutVal sigmaUpRes = sigma.upperBound();
        if (sigmaUpRes == null) {
            log.append("The upper bound of sigma is greater than 100\n");
            narr.println("The upper bound of sigma is greater than 100");
            sigmaConfidenceInterval = new double[2];
            sigmaConfidenceInterval[0] = sigmaRes.getSigma();
            sigmaConfidenceInterval[1] = 100.0;
            return null;
        }
        log.append("The result from sigmaCI upper bound: \n" + sigmaUpRes.toString() + "\n");
        narr.println("The result from sigmaCI upper bound:");
        narr.println(sigmaUpRes.toString());
        sigmaConfidenceInterval = new double[2];
        sigmaConfidenceInterval[0] = sigmaRes.getSigma();
        sigmaConfidenceInterval[1] = sigmaUpRes.getSigma();
        return null;
    }

    /**
     * Runs the npop confidence interval for the given hillclimbing value
     * @return a FredOutVal with a better likelihood if there is one found
     * during the confidence interval run, otherwise null
     */
    private FredOutVal runNpopConfidenceInterval() {
        log.append("Starting npop confidence interval: \n");
        narr.println("Starting npop confidence interval: ");
        ArrayList bins = values.getBins();
        int[] sequenceVals = values.getSeqVals();
        File npopIn = new File("npopIn.dat");
        File npopOut = new File("npopOut.dat");
        String cmdNpop = "npopCI.exe";
        NpopConfidence npop = new NpopConfidence(hClimbResult, values, narr, log, npopIn, npopOut, cmdNpop, true);
        FredOutVal npopRes = npop.lowerBound();
        log.append("The result from npopCI lower bound: \n" + npopRes.toString() + "\n");
        narr.println("The result from npopCI:");
        narr.println(npopRes.toString());
        FredOutVal npopUpRes = npop.upperBound();
        log.append("The result from npopCI upper bound: \n" + npopUpRes.toString() + "\n");
        narr.println("The result from npopCI upper bound:");
        narr.println(npopUpRes.toString());
        npopConfidenceInterval = new int[2];
        npopConfidenceInterval[0] = npopRes.getNpop();
        npopConfidenceInterval[1] = npopUpRes.getNpop();
        return null;
    }

    /**
     * Gets the user's approval to re-run hillclimbing on a new value before
     * continuing
     * @param newValue the new value to run hillclimbing on
     * @return true if the user selectes "YES" otherwise false
     */
    private boolean userApproval(FredOutVal newValue) {
        String prompt = "The program has found a value from the " + "confidence interval that has a \n better likelihood than the " + "value from hill climbing.\n  The Value is:\n" + "omega: " + newValue.getOmega() + "\nsigma: " + newValue.getSigma() + "\nnpop: " + newValue.getNpop() + "\ndrift: " + newValue.getDrift() + "\nRe-do hillclimbing?";
        JOptionPane useNewVal = new JOptionPane(prompt, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = useNewVal.createDialog(PMCopy.this, "Better parameters found");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.show();
        Object selectedValue = useNewVal.getValue();
        if (((Integer) selectedValue).intValue() == JOptionPane.YES_OPTION) {
            log.append("User elected to re-do hillclimbing on the value:\n");
            log.append(newValue.toString() + "\n");
            narr.println("User elected to re-do hillclimbing on the value:");
            narr.println(newValue.toString());
            return true;
        } else {
            log.append("User elected to continue with the current " + " value from hillclimbing\n");
            narr.println("User elected to continue with the current " + " value from hillclimbing");
            return false;
        }
    }

    /**
     * Runs hillclimbing
     * @param value the value to run hillclimbing on
     * @return The output value - the optimized set of parameters from
     * hillclimbing
     */
    private FredOutVal hillClimbing(FredOutVal value) throws IOException {
        FredOutVal bestFred = value;
        ArrayList bins = values.getBins();
        int[] sequenceVals = values.getSeqVals();
        HillClimbing hillOne = new HillClimbing(bestFred, narr, bins, sequenceVals, MasterVariables.NUM_SUCCESSES);
        long startTime, stopTime, runTime;
        double hourTime;
        log.append("Running hillclimbing... \n");
        narr.println("Running hillclimbing ");
        startTime = System.currentTimeMillis();
        hillOne.run();
        stopTime = System.currentTimeMillis();
        runTime = stopTime - startTime;
        hourTime = (double) runTime / 3600000;
        log.append("Actual runtime was: " + hourTime + "\n");
        FredOutVal hClimbResult = hillOne.getValue();
        log.append("\nThe values from hill climbing: ");
        log.append("\nomega: " + hClimbResult.getOmega());
        log.append("\nsigma: " + hClimbResult.getSigma());
        log.append("\nnpop: " + hClimbResult.getNpop() + "\n");
        narr.println();
        narr.println("The values from hill climbing: ");
        narr.println("omega: " + hClimbResult.getOmega());
        narr.println("sigma: " + hClimbResult.getSigma());
        narr.println("npop: " + hClimbResult.getNpop());
        hClimbResult = values.fullLike(hClimbResult);
        log.append("The full likelihood from hill climbing: \n");
        log.append(hClimbResult.toString() + "\n");
        narr.println("The full likelihood from hill climbing: ");
        narr.println(hClimbResult.toString());
        return hClimbResult;
    }

    /**
     * Print out the final results from hillclimbing and confidence
     * intervals
     */
    private void printResults() {
        if (hClimbResult != null) {
            log.append("Final results\nThe result from hillclimbing was: \n" + hClimbResult.toString() + "\n");
            narr.println("Final results");
            narr.println("The result from hillclimbing was: ");
            narr.println(hClimbResult.toString());
        }
        if (omegaConfidenceInterval != null) {
            String omegaHigh;
            String omegaLow;
            if (omegaConfidenceInterval[1] == 100.0) omegaHigh = ">100"; else omegaHigh = "" + omegaConfidenceInterval[1];
            if (omegaConfidenceInterval[0] < 2e-7) omegaLow = "<2e-7"; else omegaLow = "" + omegaConfidenceInterval[0];
            log.append("omega: " + hClimbResult.getOmega() + " (" + omegaLow + " to " + omegaHigh + ")\n");
            narr.println("omega: " + hClimbResult.getOmega() + " (" + omegaLow + " to " + omegaHigh + ")");
        }
        if (sigmaConfidenceInterval != null) {
            String sigmaLow;
            String sigmaHigh;
            if (sigmaConfidenceInterval[1] == 100.0) sigmaHigh = ">100"; else sigmaHigh = "" + sigmaConfidenceInterval[1];
            if (sigmaConfidenceInterval[0] < 2e-7) sigmaLow = "<1e-7"; else sigmaLow = "" + sigmaConfidenceInterval[0];
            log.append("sigma: " + hClimbResult.getSigma() + " (" + sigmaLow + " to " + sigmaHigh + ")\n");
            narr.println("sigma: " + hClimbResult.getSigma() + " (" + sigmaLow + " to " + sigmaHigh + ")");
        }
        if (npopConfidenceInterval != null) {
            log.append("npop: " + hClimbResult.getNpop() + " (" + npopConfidenceInterval[0] + " to " + npopConfidenceInterval[1] + ")\n");
            narr.println("npop: " + hClimbResult.getNpop() + " (" + npopConfidenceInterval[0] + " to " + npopConfidenceInterval[1] + ")");
        }
        if (driftConfidenceInterval != 0) {
            if ((1 / driftConfidenceInterval) < 2e-7) {
                log.append("drift: 0 (0, >1e7\n)");
                narr.println("drift: 0 (0, >1e7)");
            } else {
                log.append("drift: 0 (0, " + driftConfidenceInterval + ")\n");
                narr.println("drift: 0 (0, " + driftConfidenceInterval + ")");
            }
        }
    }

    /**
     * Returns a user selected criterion for sorting if selected
     * @return -1 if auto is selected, otherwise it returns the sorting
     * percentage that the user has selected
     */
    private int userSortPercentage() {
        String userChoice = (String) critSelector.getSelectedItem();
        if (userChoice.equals("5x")) return 0;
        if (userChoice.equals("2x")) return 1;
        if (userChoice.equals("1.5x")) return 2;
        if (userChoice.equals("1.25x")) return 3;
        if (userChoice.equals("1.10x")) return 4;
        if (userChoice.equals("1.05x")) return 5; else return -1;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ParameterSolutions().setVisible(true);
            }
        });
    }

    private NarrWriter narr;

    /**
     * The narrative output file
     */
    private File narrOut = new File("narrative.txt");

    private double[] omegaConfidenceInterval, sigmaConfidenceInterval;

    private double driftConfidenceInterval;

    private int[] npopConfidenceInterval;

    private BinningAndFred values;

    private FredOutVal hClimbResult;

    private File inputFile;

    private Thread thread;

    private final JFileChooser fc = new JFileChooser();

    private boolean readyForCI = false;

    private javax.swing.JComboBox critSelector;

    private javax.swing.JLabel criterion;

    private javax.swing.JButton driftConf;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextArea log;

    private javax.swing.JButton npopConf;

    private javax.swing.JButton omegaConf;

    private javax.swing.JButton openButton;

    private javax.swing.JButton sigmaConf;

    private javax.swing.JButton runAll;
}
