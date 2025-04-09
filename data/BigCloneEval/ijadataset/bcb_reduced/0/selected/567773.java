package uk.ac.kingston.aqurate.author_UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.AbstractButton;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import org.w3c.dom.Document;
import src.Previewer;
import uk.ac.kingston.aqurate.author_controllers.DefaultController;
import uk.ac.kingston.aqurate.author_documents.AssessmentItemDoc;
import uk.ac.kingston.aqurate.util.ContentPackageBuilder;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JDialogNewQuiz extends JDialog implements MouseListener, ActionListener {

    private static final long serialVersionUID = 1L;

    private JButton jButtonCancel = null;

    private JButton jButtonCreate = null;

    private JPanel jContentPane = null;

    private JPanel jPaneDescription = null;

    private JPanel jPanelButtons = null;

    private JPanel jPanelNewQuestion = null;

    private JPanel jPanelTypes = null;

    private JRadioButton jRadioButtonChoice = null;

    private JToolBar jToolBarButtons = null;

    private JLabel labelIllustration = null;

    private String selectedType;

    private JTextArea textarea = null;

    private JRadioButton[] vRadioButtons = new JRadioButton[8];

    private JButton jButtonUp = null;

    private JButton jButtonDown = null;

    private JButton jButtonPreviewQuiz = null;

    private JToolBar jToolBarPanelButtons = null;

    private JPanel jPanePanelButonsOKCANCEL = null;

    private JPanel panelButtonsOKCANCEL = null;

    private JPanel jPanePanelButons = null;

    private JPanel jPanelQuestion = null;

    private JPanel panelButtons = null;

    public static DefaultTableModel tableModelQ = null;

    private JScrollPane jScrollPaneQuestionsPoolQuiz = null;

    private JTable jQuestionPoolIN = null;

    private JTable jQuestionPool = null;

    ImageIcon choiceIcon = null;

    AqurateFramework owner = null;

    JFrame frameButtons = null;

    ListSelectionModel ListSelection = null;

    List<AssessmentItemDoc> documentListQuiz = null;

    private ZipOutputStream cpZipOutputStream = null;

    private String strSource = "";

    private String strTarget = "";

    private static long size = 0;

    private static int numOfFiles = 0;

    /**
	 * @param owner
	 */
    public JDialogNewQuiz(AqurateFramework owner, JTable jQuestionPoolIN, List<AssessmentItemDoc> documentList) {
        super();
        this.owner = owner;
        this.jQuestionPoolIN = jQuestionPoolIN;
        this.documentListQuiz = documentList;
        initialize();
    }

    /**
	 * This method initializes jButtonCancel
	 * 
	 * @return javax.swing.JButton
	 */
    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton("Cancel");
        }
        jButtonCancel.setPreferredSize(new Dimension(70, 25));
        jButtonCancel.addActionListener(this);
        return jButtonCancel;
    }

    /**
	 * This method initializes jButtonCreate
	 * 
	 * @return javax.swing.JButton
	 */
    private JButton getJButtonCreate() {
        if (jButtonCreate == null) {
            jButtonCreate = new JButton("Generate");
        }
        jButtonCreate.setPreferredSize(new Dimension(90, 25));
        jButtonCreate.addActionListener(this);
        return jButtonCreate;
    }

    private JButton getJButtonUp() {
        if (jButtonUp == null) {
            jButtonUp = new JButton("Up");
        }
        jButtonUp.setPreferredSize(new Dimension(60, 25));
        jButtonUp.addActionListener(this);
        return jButtonUp;
    }

    private JButton getJButtonDown() {
        if (jButtonDown == null) {
            jButtonDown = new JButton("Down");
        }
        jButtonDown.setPreferredSize(new Dimension(60, 25));
        jButtonDown.addActionListener(this);
        return jButtonDown;
    }

    private JButton getJButtonPreviewQuiz() {
        if (jButtonPreviewQuiz == null) {
            jButtonPreviewQuiz = new JButton("Preview");
        }
        jButtonPreviewQuiz.setPreferredSize(new Dimension(80, 25));
        jButtonPreviewQuiz.addActionListener(this);
        return jButtonPreviewQuiz;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(jButtonCancel)) {
            this.dispose();
        }
        if (e.getSource().equals(jButtonCreate)) {
            String defaultFileName = "PACKAGEDirectory" + File.separator + "preview.zip";
            File fdir = new File(defaultFileName);
            File absDir = new File(fdir.getAbsolutePath());
            int numberOfQuestions = jQuestionPool.getRowCount();
            int[] selectedQuestions = new int[numberOfQuestions];
            int row = 0;
            for (int rows = 0; rows < numberOfQuestions; rows++) {
                if (jQuestionPool.getValueAt(rows, 1) == null) {
                    tableModelQ.setValueAt(false, rows, 1);
                }
            }
            for (int rows = 0; rows < numberOfQuestions; rows++) {
                if ((Boolean) jQuestionPool.getValueAt(rows, 1)) {
                    selectedQuestions[row] = rows;
                    row++;
                }
            }
            if (row == 1 || row == 0) {
                JOptionPane.showMessageDialog(null, "Test Mode must be at least 2 Questions", "More Questions", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String[] Questions = new String[row];
                for (int j = 0; j < row; j++) {
                    Questions[j] = "Question_" + j;
                }
                ContentPackageBuilder cpBuilder = new ContentPackageBuilder(owner, documentListQuiz, absDir.getParent(), "", selectedQuestions, Questions, row);
                cpBuilder.buildPackage(row);
                String outfile = "PACKAGEDirectory" + File.separator + "htm" + File.separator + "preview.html";
                Previewer myPreviewer = new Previewer(absDir.getAbsolutePath(), outfile, "SAVE");
                myPreviewer.run();
                JFileChooser jFileChooserSaveTest = new JFileChooser();
                int returnVal = jFileChooserSaveTest.showSaveDialog(owner);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = jFileChooserSaveTest.getSelectedFile();
                    String oldPath = file.getAbsolutePath();
                    if (!oldPath.contains(".")) {
                        oldPath = oldPath + ".zip";
                    }
                    String tmpPath = getZipPath();
                    int posDot = tmpPath.lastIndexOf(".");
                    strSource = tmpPath.substring(0, posDot);
                    strTarget = oldPath;
                    zip();
                    this.dispose();
                }
            }
        }
        if (e.getSource().equals(jButtonPreviewQuiz)) {
            String defaultFileName = "PACKAGEDirectory" + File.separator + "preview.zip";
            File fdir = new File(defaultFileName);
            File absDir = new File(fdir.getAbsolutePath());
            int numberOfQuestions = jQuestionPool.getRowCount();
            int[] selectedQuestions = new int[numberOfQuestions];
            int row = 0;
            for (int rows = 0; rows < numberOfQuestions; rows++) {
                if (jQuestionPool.getValueAt(rows, 1) == null) {
                    tableModelQ.setValueAt(false, rows, 1);
                }
            }
            for (int rows = 0; rows < numberOfQuestions; rows++) {
                if ((Boolean) jQuestionPool.getValueAt(rows, 1)) {
                    selectedQuestions[row] = rows;
                    row++;
                }
            }
            if (row == 1 || row == 0) {
                JOptionPane.showMessageDialog(null, "Test Mode must be at least 2 Questions", "More Questions", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String[] Questions = new String[row];
                for (int j = 0; j < row; j++) {
                    Questions[j] = "Question_" + j;
                }
                ContentPackageBuilder cpBuilder = new ContentPackageBuilder(owner, documentListQuiz, absDir.getParent(), "", selectedQuestions, Questions, row);
                cpBuilder.buildPackage(row);
                String outfile = "PACKAGEDirectory" + File.separator + "htm" + File.separator + "preview.html";
                Previewer myPreviewer = new Previewer(absDir.getAbsolutePath(), outfile);
                myPreviewer.run();
            }
        }
        if (e.getSource().equals(jButtonUp)) {
            int rowSelected = jQuestionPool.getSelectedRow();
            AssessmentItemDoc TempDocList = documentListQuiz.get(rowSelected - 1);
            documentListQuiz.set(rowSelected - 1, documentListQuiz.get(rowSelected));
            documentListQuiz.set(rowSelected, TempDocList);
            tableModelQ.moveRow(rowSelected, rowSelected, rowSelected - 1);
            ListSelectionModel ListSelection2 = jQuestionPool.getSelectionModel();
            ListSelection2.setSelectionInterval(rowSelected - 1, rowSelected - 1);
            System.out.println("rowselectedUP : " + rowSelected);
            checkUpDownButtons();
            System.out.println("rowselectedUP : " + rowSelected);
        }
        if (e.getSource().equals(jButtonDown)) {
            int rowSelected = jQuestionPool.getSelectedRow();
            AssessmentItemDoc TempDocList = documentListQuiz.get(rowSelected + 1);
            documentListQuiz.set(rowSelected + 1, documentListQuiz.get(rowSelected));
            documentListQuiz.set(rowSelected, TempDocList);
            tableModelQ.moveRow(rowSelected, rowSelected, rowSelected + 1);
            ListSelectionModel ListSelection2 = jQuestionPool.getSelectionModel();
            ListSelection2.setSelectionInterval(rowSelected + 1, rowSelected + 1);
            System.out.println("rowselectedDOWN : " + rowSelected);
            checkUpDownButtons();
            System.out.println("rowselectedDOWN : " + rowSelected);
        }
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
        }
        jContentPane.setLayout(new BorderLayout());
        jContentPane.add(getJPanePanelButtonsOKCANCEL(), BorderLayout.SOUTH);
        jContentPane.add(getJPanePanelButtons(), BorderLayout.CENTER);
        jContentPane.add(getJTextArea(), BorderLayout.NORTH);
        jContentPane.add(getJPanelQuestions(), BorderLayout.WEST);
        jContentPane.setVisible(true);
        return jContentPane;
    }

    private JPanel getJPanelQuestions() {
        if (jPanelQuestion == null) {
            jPanelQuestion = new JPanel();
            jPanelQuestion.setLayout(new BorderLayout());
            jPanelQuestion.setPreferredSize(new Dimension(460, 390));
            jPanelQuestion.setBorder(new TitledBorder("Question Pool"));
            jPanelQuestion.add(getJScrollPanePool(), BorderLayout.WEST);
        }
        return jPanelQuestion;
    }

    private JScrollPane getJScrollPanePool() {
        if (jScrollPaneQuestionsPoolQuiz == null) {
            jScrollPaneQuestionsPoolQuiz = new JScrollPane();
            jScrollPaneQuestionsPoolQuiz.setViewportView(getJTableQuestionsPool());
        }
        jScrollPaneQuestionsPoolQuiz.setPreferredSize(new Dimension(440, 700));
        return jScrollPaneQuestionsPoolQuiz;
    }

    private JTable getJTableQuestionsPool() {
        if (jQuestionPool == null) {
            jQuestionPool = new JTable(tableModelQ);
            jQuestionPool.setRowHeight(35);
        }
        int question = jQuestionPoolIN.getRowCount();
        String currentRow = "";
        String[] columns = { "Question", "In Quiz" };
        tableModelQ = new DefaultTableModel(columns, 0) {

            private static final long serialVersionUID = 1L;

            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Boolean.class;
                } else {
                    return super.getColumnClass(columnIndex);
                }
            }

            ;
        };
        DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
        mylsmodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jQuestionPool.setSelectionModel(mylsmodel);
        jQuestionPool.setModel(tableModelQ);
        jQuestionPool.getColumn("In Quiz").setMaxWidth(100);
        for (int i = 0; i < question; i++) {
            tableModelQ.addRow(new Object[] { jQuestionPoolIN.getValueAt(i, 0) });
        }
        ListSelection = jQuestionPool.getSelectionModel();
        ListSelection.setSelectionInterval(0, 0);
        jButtonUp.setEnabled(false);
        jQuestionPool.addMouseListener(this);
        return jQuestionPool;
    }

    /**
	 * This method initializes jScrollPaneDescription
	 * 
	 * @return javax.swing.JScrollPane
	 */
    private JPanel getJPanePanelButtons() {
        if (jPanePanelButons == null) {
            jPanePanelButons = new JPanel(new BorderLayout());
            jPanePanelButons.setPreferredSize(new Dimension(80, 200));
            jPanePanelButons.add(getJPanelButtons(), BorderLayout.NORTH);
        }
        return jPanePanelButons;
    }

    private JPanel getJPanelButtons() {
        if (panelButtons == null) {
        }
        JPanel panelButtons = new JPanel(new GridLayout(3, 2));
        Border border = BorderFactory.createTitledBorder("Options");
        panelButtons.setBorder(border);
        panelButtons.add(getJButtonUp());
        panelButtons.add(getJButtonDown());
        panelButtons.add(getJButtonPreviewQuiz());
        panelButtons.setSize(80, 200);
        panelButtons.setVisible(true);
        return panelButtons;
    }

    private JPanel getJPanePanelButtonsOKCANCEL() {
        if (jPanePanelButonsOKCANCEL == null) {
            jPanePanelButonsOKCANCEL = new JPanel(new BorderLayout());
            jPanePanelButonsOKCANCEL.setPreferredSize(new Dimension(140, 25));
            jPanePanelButonsOKCANCEL.add(getJPanelButtonsOKCANCEL(), BorderLayout.EAST);
        }
        return jPanePanelButonsOKCANCEL;
    }

    private JPanel getJPanelButtonsOKCANCEL() {
        if (panelButtonsOKCANCEL == null) {
        }
        panelButtonsOKCANCEL = new JPanel(new GridLayout(1, 2));
        panelButtonsOKCANCEL.add(getJButtonCreate());
        panelButtonsOKCANCEL.add(getJButtonCancel());
        panelButtonsOKCANCEL.setVisible(true);
        return panelButtonsOKCANCEL;
    }

    /**
	 * This method initializes getJTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
    private JTextArea getJTextArea() {
        if (textarea == null) {
            textarea = new JTextArea();
            textarea.setPreferredSize(new Dimension(560, 50));
        }
        textarea.setText("\n        Select the Questions to include in a formative Quiz \n");
        textarea.setBackground(jPanePanelButons.getBackground());
        textarea.setEditable(false);
        return textarea;
    }

    /**
	 * This method initializes jToolBarButtons
	 * 
	 * @return javax.swing.JToolBar
	 */
    private JToolBar getJToolBarButtons() {
        if (jToolBarButtons == null) {
            jToolBarButtons = new JToolBar();
        }
        jToolBarButtons.setPreferredSize(new Dimension(240, 35));
        jToolBarButtons.setFloatable(false);
        jToolBarButtons.setRollover(true);
        jToolBarButtons.add(getJButtonCreate());
        jToolBarButtons.addSeparator(new Dimension(20, 35));
        jToolBarButtons.add(getJButtonCancel());
        return jToolBarButtons;
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setSize(560, 346);
        this.setResizable(false);
        this.setTitle("Generate Quiz");
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);
        if (owner.getClass().getSuperclass().getSimpleName().equals("JFrame")) {
            String userDir = System.getProperty("user.dir");
            System.out.println(userDir);
        }
    }

    public void checkUpDownButtons() {
        int rowSelected = jQuestionPool.getSelectedRow();
        if (rowSelected == 0) {
            jButtonUp.setEnabled(false);
        } else {
            jButtonUp.setEnabled(true);
        }
        if (rowSelected == jQuestionPool.getRowCount() - 1) {
            this.jButtonDown.setEnabled(false);
        } else {
            this.jButtonDown.setEnabled(true);
        }
    }

    public void mouseClicked(MouseEvent e) {
        checkUpDownButtons();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    private void zip() {
        try {
            File cpFile = new File(strSource);
            if (!cpFile.isFile() && !cpFile.isDirectory()) {
                System.out.println("\nSource file/directory Not Found!");
                return;
            }
            FileOutputStream fos = new FileOutputStream(strTarget);
            cpZipOutputStream = new ZipOutputStream(fos);
            cpZipOutputStream.setLevel(9);
            zipFiles(cpFile);
            cpZipOutputStream.finish();
            cpZipOutputStream.close();
            System.out.println("\n Finished creating zip file " + strTarget + " from source " + strSource);
            System.out.println("\n Total of  " + numOfFiles + " files are Zipped ");
            System.out.println("\n Total of  " + size + " bytes are Zipped  ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String zipPath = "";

    public static void setZipPath(String x) {
        zipPath = x;
    }

    public String getZipPath() {
        return zipPath;
    }

    private void zipFiles(File cpFile) {
        int byteCount;
        final int DATA_BLOCK_SIZE = 2048;
        FileInputStream cpFileInputStream;
        if (cpFile.isDirectory()) {
            if (cpFile.getName().equalsIgnoreCase(".metadata")) {
                return;
            }
            File[] fList = cpFile.listFiles();
            for (int i = 0; i < fList.length; i++) {
                zipFiles(fList[i]);
            }
        } else {
            try {
                if (cpFile.getAbsolutePath().equalsIgnoreCase(strTarget)) {
                    return;
                }
                System.out.println("Zipping " + cpFile);
                size += cpFile.length();
                numOfFiles++;
                String strAbsPath = cpFile.getPath();
                String strZipEntryName = strAbsPath.substring(strSource.length() + 1, strAbsPath.length());
                cpFileInputStream = new FileInputStream(cpFile);
                ZipEntry cpZipEntry = new ZipEntry(strZipEntryName);
                cpZipOutputStream.putNextEntry(cpZipEntry);
                byte[] b = new byte[DATA_BLOCK_SIZE];
                while ((byteCount = cpFileInputStream.read(b, 0, DATA_BLOCK_SIZE)) != -1) {
                    cpZipOutputStream.write(b, 0, byteCount);
                }
                cpZipOutputStream.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
