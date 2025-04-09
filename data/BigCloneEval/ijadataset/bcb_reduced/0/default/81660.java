import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class SystemReliabilityFrame {

    /**
	 * @param args
	 */
    public static final String MY_WORKSPACE_PATH = "C:\\Users\\kanna\\workspace\\SCS\\src\\";

    public static String SYS_ALGEBRA_EXP = "";

    public static String temp = "";

    public static JFrame reliabilityFrame;

    public static JTextField sysAlgebra;

    public static JTable componentTable;

    public static JScrollPane scrollpane;

    public static JButton getRel;

    public static JButton getFromFile;

    public static JLabel finalReliability;

    public static JTextField finalReliabilityValue;

    public static void main(String[] args) {
    }

    public static void reliability() {
        final String fileName = "Temp.txt";
        reliabilityFrame = new JFrame("System Reliability");
        JLabel sysAlgebraLabel = new JLabel("Enter System Algebra");
        sysAlgebraLabel.setBounds(170, 70, 200, 25);
        sysAlgebra = new JTextField(100);
        sysAlgebra.setBounds(350, 70, 250, 25);
        getFromFile = new JButton("From File");
        getFromFile.setBounds(610, 70, 100, 20);
        getFromFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    final JFileChooser fc = new JFileChooser();
                    fc.showOpenDialog(reliabilityFrame);
                    try {
                        File file = fc.getSelectedFile();
                        if (file != null) {
                            FileReader reader = new FileReader(file);
                            BufferedReader breader = new BufferedReader(reader);
                            String line = breader.readLine();
                            if (line != null) {
                                line = line.replaceAll("X", "*");
                                line = line.replaceAll(" ", "");
                                sysAlgebra.setText(line);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        JButton genButton = new JButton("Enter");
        genButton.setBounds(350, 130, 200, 25);
        genButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                getRel.setVisible(true);
                generateAlgebra();
            }
        });
        finalReliability = new JLabel("System Reliability = ");
        finalReliability.setBounds(200, 500, 150, 30);
        finalReliability.setVisible(false);
        reliabilityFrame.getContentPane().add(finalReliability);
        finalReliabilityValue = new JTextField(100);
        finalReliabilityValue.setBounds(310, 500, 150, 30);
        finalReliabilityValue.setEditable(false);
        finalReliabilityValue.setVisible(false);
        reliabilityFrame.getContentPane().add(finalReliabilityValue);
        reliabilityFrame.getContentPane().add(getFromFile);
        reliabilityFrame.getContentPane().add(sysAlgebra);
        reliabilityFrame.getContentPane().add(sysAlgebraLabel);
        reliabilityFrame.getContentPane().add(genButton);
        getRel = new JButton("Get Reliability");
        getRel.setBounds(620, 170, 130, 30);
        getRel.setVisible(false);
        reliabilityFrame.getContentPane().add(getRel);
        reliabilityFrame.setSize(800, 600);
        reliabilityFrame.setLayout(null);
        reliabilityFrame.setVisible(true);
    }

    public static boolean isCellEditable(int r, int c) {
        return false;
    }

    public static void generateReliability(String[] subsytems) {
        SYS_ALGEBRA_EXP = temp;
        ArrayList<String> reliabilityList = new ArrayList<String>();
        System.out.println(componentTable.getValueAt(0, 1).toString());
        String[] subSysReliabilityValues = new String[subsytems.length];
        double[] ReliabilityValues = new double[subsytems.length];
        for (int i = 0; i < subsytems.length; i++) {
            subSysReliabilityValues[i] = componentTable.getValueAt(i, 1).toString();
            ReliabilityValues[i] = Double.parseDouble(subSysReliabilityValues[i]);
            System.out.println(subSysReliabilityValues[i]);
        }
        for (int i = 0; i < subsytems.length; i++) {
            SYS_ALGEBRA_EXP = SYS_ALGEBRA_EXP.replaceAll(subsytems[i], subSysReliabilityValues[i]);
        }
        Double tempReliability;
        String[] reliability = SYS_ALGEBRA_EXP.split(" ");
        for (int i = 0; i < reliability.length; i++) {
            reliabilityList.add(reliability[i]);
        }
        System.out.println(SYS_ALGEBRA_EXP);
        System.out.println(reliabilityList.lastIndexOf("("));
        while (reliabilityList.size() > 1) {
            System.out.println(reliabilityList);
            for (int i = (reliabilityList.lastIndexOf("(") + 1); i < reliabilityList.size(); i++) {
                if (!(reliabilityList.get(i).equals(")"))) {
                    if ((reliabilityList.get(i).equals("*"))) {
                        tempReliability = 1 - ((1 - Double.parseDouble(reliabilityList.get(i - 1))) * (1 - Double.parseDouble(reliabilityList.get(i + 1))));
                        System.out.println(tempReliability);
                        reliabilityList.set(i - 1, tempReliability.toString());
                        reliabilityList.remove(i + 1);
                        reliabilityList.remove(i);
                        if (reliabilityList.get(i).equals(")")) {
                            reliabilityList.remove(i);
                            reliabilityList.remove(reliabilityList.lastIndexOf("("));
                        }
                    } else if (reliabilityList.get(i).equals("+")) {
                        tempReliability = Double.parseDouble(reliabilityList.get(i - 1)) * Double.parseDouble(reliabilityList.get(i + 1));
                        reliabilityList.set(i - 1, tempReliability.toString());
                        reliabilityList.remove(i + 1);
                        reliabilityList.remove(i);
                        if (reliabilityList.get(i).equals(")")) {
                            reliabilityList.remove(i);
                            reliabilityList.remove(reliabilityList.lastIndexOf("("));
                        }
                    }
                } else {
                    break;
                }
            }
        }
        finalReliabilityValue.setVisible(true);
        finalReliabilityValue.setText(reliabilityList.get(0));
    }

    public static void generateAlgebra() {
        String sysExp = readFile(sysAlgebra.getText());
        final String[] subsytems = sysExp.split(" ");
        String[][] data = new String[subsytems.length][2];
        String[] columnNames = { "Component", "Reliability" };
        componentTable = new JTable(data, columnNames);
        for (int i = 0; i < subsytems.length; i++) {
            componentTable.setValueAt(subsytems[i], i, 0);
            componentTable.isCellEditable(i, 0);
        }
        scrollpane = new JScrollPane(componentTable);
        scrollpane.setBounds(170, 170, 400, 300);
        reliabilityFrame.getContentPane().add(scrollpane);
        getRel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    finalReliability.setVisible(true);
                    generateReliability(subsytems);
                } catch (NullPointerException nex) {
                    JOptionPane.showMessageDialog(null, "Press Enter after entering value in cell");
                }
            }
        });
    }

    public static String removeSpaces(String s) {
        StringTokenizer st = new StringTokenizer(s, " ", false);
        String t = "";
        while (st.hasMoreElements()) t += st.nextElement();
        return t;
    }

    public static String readFile(String fileName) {
        String sysAlgebraExp1 = "";
        try {
            String line = fileName;
            Pattern regExp1 = Pattern.compile("\\^[0-9]");
            Matcher action1 = regExp1.matcher(line);
            sysAlgebraExp1 = action1.replaceAll("");
            temp = sysAlgebraExp1;
            sysAlgebraExp1 = sysAlgebraExp1.replaceAll("\\+", " ");
            sysAlgebraExp1 = sysAlgebraExp1.replaceAll("\\*", " ");
            sysAlgebraExp1 = sysAlgebraExp1.replaceAll("\\(", "");
            sysAlgebraExp1 = sysAlgebraExp1.replaceAll("\\)", "");
            temp = removeSpaces(temp);
            temp = temp.replaceAll("\\+", " + ");
            temp = temp.replaceAll("\\*", " * ");
            temp = temp.replaceAll("\\(", "( ");
            temp = temp.replaceAll("\\)", " )");
            SYS_ALGEBRA_EXP = temp;
            System.out.println(SYS_ALGEBRA_EXP);
            sysAlgebraExp1 = sysAlgebraExp1.replaceAll("\\b\\s{2,}\\b", " ");
            System.out.println(sysAlgebraExp1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        return sysAlgebraExp1;
    }
}
