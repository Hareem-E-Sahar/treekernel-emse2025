import java.util.*;
import javax.swing.*;
import jdsl.core.api.Tree;
import jdsl.core.api.Position;
import jdsl.core.api.PositionIterator;
import java.io.File;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author  Carlo Francisco
 */
public class DemarcationsAuto extends javax.swing.JFrame {

    /** Creates new form Demarcations */
    public DemarcationsAuto(FredOutVal hClimbResult, BinningAndFred values, File inputFile, File treeFile, NarrWriter narr, Execs execs) {
        recombs = new ArrayList<String>();
        ecotypes = new ArrayList<ArrayList<String>>();
        this.narr = narr;
        this.log = new javax.swing.JTextArea();
        this.execs = execs;
        this.hClimbResult = hClimbResult;
        input = new File("oldNpopIn.dat");
        output = new File("oldNpopOut.dat");
        fastaOutput = new File("fasta.dat");
        numbers = new File("numbers.dat");
        this.values = new BinningAndFred(log, narr, execs, values.getSeqVals(), values.getBins());
        this.inputFile = inputFile;
        try {
            BufferedReader input = new BufferedReader(new FileReader(this.inputFile));
            Scanner scOut = new Scanner(input.readLine().substring(1));
            outgroup = scOut.next();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BinningFasta.readFile(inputFile, fastaOutput, numbers);
            fastaRGCopy = new File("fastaRGCopy.dat");
            copyFasta();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean generateTree = false;
        if (treeFile == null) {
            generateTree = true;
            treeFile = runTree();
        }
        try {
            Scanner sc = new Scanner(treeFile);
            String s = "";
            while (sc.hasNextLine()) {
                CharacterIterator ci = new StringCharacterIterator(sc.nextLine());
                for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
                    if (c == ':') {
                        while ((c != ',') && (c != '(') && (c != ')')) {
                            c = ci.next();
                        }
                    }
                    while ((c == ')') && (c != ';')) {
                        s += c;
                        c = ci.next();
                        while ((c != ',') && (c != ')') && (c != ';')) {
                            c = ci.next();
                        }
                    }
                    if (c != ';') s += c;
                }
            }
            Tree treeGen = (new TreeGen(s.substring(1, s.length() - 1))).getTree();
            if (generateTree == false) {
                findRecombs(treeGen);
            }
            getEcotypes(treeGen);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initComponents();
        Iterator iter = ecotypes.iterator();
        for (int i = 1; iter.hasNext(); i++) {
            log.append("Ecotype " + i + ": " + iter.next() + "\n");
        }
        if (recombs.size() > 0) {
            log.append("\nThe following were found to be recombinants and thus ignored: ");
            Iterator recombIter = recombs.iterator();
            log.append("" + recombIter.next());
            while (recombIter.hasNext()) log.append(", " + recombIter.next());
        }
        log.append("\nThe sequence " + outgroup + " was assumed to be the outgroup, and so it was ignored.");
    }

    private void initComponents() {
        jScrollPane3 = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                if (narr != null) {
                    narr.close();
                }
                dispose();
            }
        });
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Demarcations");
        log.setColumns(20);
        log.setEditable(false);
        log.setRows(5);
        log.setDoubleBuffered(true);
        jScrollPane3.setViewportView(log);
        fileMenu.setText("File");
        exitItem.setText("Close");
        exitItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);
        jMenuBar1.add(fileMenu);
        jMenu1.setText("Export");
        jMenuItem1.setText("To CSV");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);
        jMenuBar1.add(jMenu1);
        setJMenuBar(jMenuBar1);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE).addContainerGap()));
        pack();
    }

    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == exitItem) {
            dispose();
        }
    }

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        int returnVal = fc.showSaveDialog(DemarcationsAuto.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File userFile = fc.getSelectedFile();
            String path = userFile.getPath();
            if (!((path.substring(path.length() - 4, path.length())).equals(".csv"))) {
                userFile = new File(userFile.getPath() + ".csv");
            }
            narr.println("Saving to: " + userFile.getName());
            try {
                FileWriter writer = new FileWriter(userFile);
                writer.append("Ecotype Number");
                for (int i = 1; i <= highestSequenceNum(); i++) {
                    writer.append(',' + "Sequence " + i);
                }
                Iterator<ArrayList<String>> ecotypesIterator = ecotypes.iterator();
                for (int j = 1; ecotypesIterator.hasNext(); j++) {
                    writer.append('\n');
                    writer.append("" + j);
                    ArrayList<String> currentEcotype = ecotypesIterator.next();
                    Iterator seqIterator = currentEcotype.iterator();
                    while (seqIterator.hasNext()) {
                        writer.append("," + seqIterator.next());
                    }
                }
                writer.append('\n');
                writer.append("Outgroup");
                writer.append(outgroup);
                writer.append('\n');
                writer.append("Recombinants");
                Iterator<String> iter = recombs.iterator();
                while (iter.hasNext()) {
                    writer.append("," + iter.next());
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int highestSequenceNum() {
        int n = 0;
        Iterator<ArrayList<String>> iter = ecotypes.iterator();
        while (iter.hasNext()) {
            ArrayList<String> current = iter.next();
            if (current.size() > n) n = current.size();
        }
        return n;
    }

    private File runTree() {
        File treeFile = new File("outtree");
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(treeFile));
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String type = "nj";
        TreeFinder tf = new TreeFinder(inputFile, type, execs);
        return treeFile;
    }

    private void copyFasta() {
        try {
            BufferedReader input = new BufferedReader(new FileReader("fasta.dat"));
            BufferedWriter output = new BufferedWriter(new FileWriter(fastaRGCopy));
            String line = input.readLine();
            while (line != null) {
                output.write(line);
                output.newLine();
                line = input.readLine();
            }
            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findRecombs(Tree tree) {
        ArrayList<String> seqsInFastaFile = new ArrayList<String>();
        try {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            Scanner sc = new Scanner(input);
            String line;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.charAt(0) == '>') {
                    Scanner sc1 = new Scanner(line.substring(1, line.length()));
                    seqsInFastaFile.add(sc1.next());
                }
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> seqsInTree = new ArrayList<String>();
        PositionIterator iter = tree.positions();
        while (iter.hasNext()) {
            String sequence = iter.nextPosition().toString();
            if (!sequence.equals("Position with element null")) {
                seqsInTree.add(sequence.substring(22, sequence.length()));
            }
        }
        seqsInTree.removeAll(seqsInFastaFile);
        recombs = seqsInTree;
    }

    private void getEcotypes(Tree tree) {
        ArrayList<String> sequences = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(tree.toString());
        if (st.countTokens() == 1) {
            String singleSequence = st.nextToken();
            if ((!singleSequence.equals(outgroup)) && (!recombs.contains(singleSequence))) {
                sequences.add(singleSequence);
                ecotypes.add(sequences);
            }
        } else {
            st.nextToken();
            while (st.hasMoreTokens()) {
                String sequence = st.nextToken();
                if ((!sequence.equals("null")) && (!sequence.equals(outgroup)) && (!recombs.contains(sequence))) {
                    sequences.add(sequence);
                }
            }
            if (sequences.size() == 0) {
                return;
            }
            SelectSeqBins selector = new SelectSeqBins(fastaRGCopy, sequences, narr, execs);
            ArrayList<String> bins = selector.getBins();
            values.setBins(bins);
            values.setSeqVals(selector.getSeqVals());
            DemarcationConfidence demarcConf = new DemarcationConfidence(hClimbResult, values, narr, log, execs, input, output, false);
            if (demarcConf.demarcations()[1] == 1) {
                ecotypes.add(sequences);
            } else {
                PositionIterator children = tree.children(tree.root());
                while (children.hasNext()) {
                    Position currentChild = children.nextPosition();
                    Tree currentTree = tree.cut(currentChild);
                    getEcotypes(currentTree);
                }
            }
        }
    }

    public ArrayList<ArrayList<String>> getEcotypes() {
        return ecotypes;
    }

    public ArrayList<String> getRecombs() {
        return recombs;
    }

    private Execs execs;

    private ArrayList<String> recombs;

    private String outgroup;

    private HashMap clades = new HashMap();

    private ArrayList<ArrayList<String>> ecotypes;

    private NarrWriter narr;

    private BinningAndFred values;

    private String cmd;

    private File input;

    private static File output;

    private File fastaRGCopy;

    private TreeFinder tf;

    private File inputFile;

    private File fastaOutput;

    private File numbers;

    private File narrOut = new File("narrDemarcAuto.txt");

    private FredOutVal hClimbResult;

    private final JFileChooser fc = new JFileChooser();

    private javax.swing.JMenuItem exitItem;

    private javax.swing.JMenu fileMenu;

    private javax.swing.JMenu jMenu1;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JTextArea log;
}
