import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

/**
 * This is a simple GUI based application for Search/Replace using regex
 * @author Hitesh Viseria, maito:hviseria@yahoo.com
 * @version 0.1
 */
public class RegExSearchReplace extends javax.swing.JFrame {

    private JMenuItem helpMenuItem;

    private JMenuItem readMeMenuItem;

    private JMenu jMenu5;

    private JMenuItem deleteMenuItem;

    private JSeparator jSeparator1;

    private JMenuItem pasteMenuItem;

    private JLabel jLabel1;

    private JTextField FolderName;

    private JButton Select;

    private JLabel Folder;

    private JTextField SearchExp;

    private JLabel Search;

    private JPanel jPanel1;

    private JMenuItem copyMenuItem;

    private JMenuItem cutMenuItem;

    private JMenu jMenu4;

    private JMenuItem exitMenuItem;

    private JSeparator jSeparator2;

    private JLabel jLabel4;

    private JLabel jLabel5;

    private JButton Replace;

    private JButton preview;

    private JTextField replaceField;

    private JLabel jLabel2;

    private JScrollPane jScrollPane1;

    private JButton Find;

    private JEditorPane SearchResults;

    private JButton SelectFile;

    private JTextField FileFilter;

    private JMenuItem closeFileMenuItem;

    private JMenuItem saveAsMenuItem;

    private JMenuItem saveMenuItem;

    private JMenuItem openFileMenuItem;

    private JMenuItem newFileMenuItem;

    private JMenu jMenu3;

    private JMenuBar jMenuBar1;

    /**
	* Main method to display this JFrame
	*/
    public static void main(String[] args) {
        RegExSearchReplace inst = new RegExSearchReplace();
        inst.setVisible(true);
    }

    public RegExSearchReplace() {
        super();
        initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            this.getContentPane().setLayout(thisLayout);
            this.addWindowListener(new WindowAdapter() {

                public void windowClosed(WindowEvent evt) {
                    rootWindowClosed(evt);
                }

                public void windowClosing(WindowEvent evt) {
                    rootWindowClosed(evt);
                }
            });
            this.setSize((int) (getMaximumSize().width * 0.5), (int) (getMaximumSize().height * 0.5));
            this.setTitle("RegExSearchReplace - Regular Expression Search/Replace Tool ........ by Hitesh Viseria");
            {
                jMenuBar1 = new JMenuBar();
                setJMenuBar(jMenuBar1);
                {
                }
                {
                }
                {
                    jMenu5 = new JMenu();
                    jMenuBar1.add(jMenu5);
                    jMenu5.setText("Help");
                    {
                        {
                            jPanel1 = new JPanel();
                            this.getContentPane().add(jPanel1, BorderLayout.NORTH);
                            GridBagLayout jPanel1Layout = new GridBagLayout();
                            jPanel1.setLayout(jPanel1Layout);
                            jPanel1.setPreferredSize(new java.awt.Dimension(709, 139));
                            {
                                Search = new JLabel();
                                jPanel1.add(Search, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                Search.setText("Search    : ");
                            }
                            {
                                SearchExp = new JTextField(140);
                                jPanel1.add(SearchExp, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                SearchExp.setPreferredSize(new java.awt.Dimension(140, 20));
                                SearchExp.setText("");
                            }
                            {
                                Folder = new JLabel();
                                jPanel1.add(Folder, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                Folder.setText("Folder      : ");
                            }
                            {
                                Select = new JButton();
                                jPanel1.add(Select, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                Select.setText("Select  ");
                                Select.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        SelectActionPerformed(evt);
                                    }
                                });
                            }
                            {
                                FolderName = new JTextField();
                                jPanel1.add(FolderName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                FolderName.setText("Select Folder Name            ");
                            }
                            {
                                jLabel1 = new JLabel();
                                jPanel1.add(jLabel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                jLabel1.setText("File Name : ");
                            }
                            {
                                FileFilter = new JTextField();
                                jPanel1.add(FileFilter, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                FileFilter.setText("Enter fileName or file filter");
                            }
                            {
                                SelectFile = new JButton();
                                jPanel1.add(SelectFile, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                SelectFile.setText("Select  ");
                                SelectFile.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        SelectFileActionPerformed(evt);
                                    }
                                });
                            }
                            {
                                Find = new JButton();
                                jPanel1.add(Find, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                                Find.setText("Search");
                                Find.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        FindActionPerformed(evt, 0);
                                    }
                                });
                            }
                            {
                                jLabel2 = new JLabel();
                                jPanel1.add(jLabel2, new GridBagConstraints(11, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                                jLabel2.setText("Replace");
                            }
                            {
                                replaceField = new JTextField(60);
                                replaceField.setPreferredSize(new java.awt.Dimension(140, 20));
                                replaceField.setText("Enter Text To Replace");
                                jPanel1.add(replaceField, new GridBagConstraints(12, 0, 1, 1, 0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            }
                            {
                                preview = new JButton();
                                jPanel1.add(preview, new GridBagConstraints(13, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                                preview.setText("Preview");
                                preview.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        System.out.println("preview.actionPerformed, event=" + evt);
                                        FindActionPerformed(evt, 1);
                                    }
                                });
                            }
                            {
                                Replace = new JButton();
                                jPanel1.add(Replace, new GridBagConstraints(13, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                                Replace.setText("Replace");
                                Replace.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        System.out.println("Replace.actionPerformed, event=" + evt);
                                        FindActionPerformed(evt, 2);
                                    }
                                });
                            }
                            {
                                jLabel4 = new JLabel();
                                jPanel1.add(jLabel4, new GridBagConstraints(11, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                                jLabel4.setText("                               ");
                            }
                            {
                                jLabel5 = new JLabel();
                                jPanel1.add(jLabel5, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                                jLabel5.setText("                                                       ");
                            }
                        }
                        {
                            jScrollPane1 = new JScrollPane();
                            {
                                SearchResults = new JEditorPane();
                                jScrollPane1.setViewportView(SearchResults);
                                SearchResults.setText("Search Results");
                            }
                            this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
                        }
                        helpMenuItem = new JMenuItem();
                        jMenu5.add(helpMenuItem);
                        helpMenuItem.setText("About");
                        helpMenuItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent evt) {
                                System.out.println("about.actionPerformed, event=" + evt);
                                displayAbout(evt, 1);
                            }
                        });
                        readMeMenuItem = new JMenuItem();
                        jMenu5.add(readMeMenuItem);
                        readMeMenuItem.setText("ReadMe");
                        readMeMenuItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent evt) {
                                System.out.println("about.actionPerformed, event=" + evt);
                                displayReadMe(evt, 1);
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SelectActionPerformed(ActionEvent evt) {
        System.out.println("Select.actionPerformed, event=" + evt);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
            FolderName.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void SelectFileActionPerformed(ActionEvent evt) {
        System.out.println("SelectFile.actionPerformed, event=" + evt);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(new JPanel());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
            FileFilter.setText(chooser.getSelectedFile().getAbsolutePath());
            FolderName.setText(chooser.getSelectedFile().getParentFile().getAbsolutePath());
        }
    }

    private void FindActionPerformed(ActionEvent evt, int search) {
        try {
            List fileList = new ArrayList(0);
            String filter = FileFilter.getText();
            if (filter != null && filter.trim().length() > 0) {
                File tmpFile = new File(FileFilter.getText());
                if ((tmpFile).exists()) {
                    filter = (tmpFile).getName();
                }
            } else {
                filter = "*.*";
            }
            FileUtils.listFiles(new File(FolderName.getText()), fileList, filter);
            int noOfFiles = fileList.size();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            int errorFiles = 0;
            String op = null;
            String fcontent = null;
            pw.print("<html><body>");
            FileWriter fw = null;
            for (int i = 0; i < noOfFiles; i++) {
                if (search == 0) {
                    op = SearchEngine.find(SearchExp.getText(), new FileReader((File) fileList.get(i)), true);
                } else if (search == 1) {
                    op = ReplaceEngine.preview(SearchExp.getText(), new FileReader((File) fileList.get(i)), true, replaceField.getText());
                } else if (search == 2) {
                    try {
                        File org = (File) fileList.get(i);
                        File nf = new File(org.getAbsolutePath() + ".bak");
                        fw = new FileWriter(nf);
                        fw.write(ReplaceEngine.replace(SearchExp.getText(), replaceField.getText(), FileUtils.fileToString((File) fileList.get(i)), true));
                        fw.close();
                        nf.renameTo(new File(org.getAbsolutePath() + ".bak1"));
                        String orgName = org.getAbsolutePath();
                        org.renameTo(new File(org.getAbsolutePath() + ".bak"));
                        nf.renameTo(new File(orgName));
                    } catch (Exception ee) {
                        errorFiles++;
                        ee.printStackTrace();
                    }
                }
                if (op != null && op.trim().length() > 0) {
                    pw.println("File:" + ((File) fileList.get(i)).getAbsolutePath() + "/" + ((File) fileList.get(i)).getName());
                    pw.println(op);
                    errorFiles++;
                    op = null;
                }
            }
            pw.println("<br><br><b>Total Files with matches :" + errorFiles + "</b>");
            pw.print("</body></html>");
            if (search != 2) {
                SearchResults.setContentType("text/html");
                SearchResults.setText(sw.toString());
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        System.out.println("Find.actionPerformed, event=" + evt);
    }

    private void rootWindowClosed(WindowEvent evt) {
        System.out.println("this.windowClosed, event=" + evt);
        System.exit(0);
    }

    /**
     * Method getHtmlEquivalentStr alters the special characters into html equivalents
     * @param saInput
     * @return String
     */
    public static String getHtmlEquivalentStr(String saInput) {
        String returnVal = null;
        if (null != saInput) {
            try {
                String slInputTmp = saInput;
                for (int ilCounter = 0; ilCounter < scSpecialChars.length; ilCounter++) {
                    slInputTmp = replaceAllOccurrences(slInputTmp, scSpecialChars[ilCounter], scReplacement[ilCounter]);
                }
                return slInputTmp;
            } catch (Exception e) {
                returnVal = "";
            }
        }
        return returnVal;
    }

    /**
     * This method replaces all occurrences of matched pattern with the replacement string
     * @param saInput
     * @param saMatchPattern
     * @param saReplaceString
     * @return
     */
    public static String replaceAllOccurrences(String saInput, String saMatchPattern, String saReplaceString) {
        if ((null == saInput) || (saInput.indexOf(saMatchPattern) == -1) || (null == saMatchPattern) || (saMatchPattern.length() <= 0)) {
            return saInput;
        }
        String slInput = saInput;
        StringBuffer sblTemp = new StringBuffer();
        int ilIndex = slInput.indexOf(saMatchPattern);
        sblTemp = new StringBuffer(slInput);
        ilIndex = sblTemp.toString().indexOf(saMatchPattern);
        while (ilIndex >= 0) {
            sblTemp = sblTemp.delete(ilIndex, saMatchPattern.length() + ilIndex);
            sblTemp = sblTemp.insert(ilIndex, saReplaceString);
            ilIndex = sblTemp.toString().indexOf(saMatchPattern);
        }
        return sblTemp.toString();
    }

    private static String[] scSpecialChars = { ">", "\"", "<", "'" };

    private static String[] scReplacement = { "&gt;", "&quot;", "&lt;", "&#39;" };

    private static String aboutMess = null;

    static {
        try {
            aboutMess = FileUtils.fileToString(new File("about.txt"));
        } catch (Exception ee) {
            ee.printStackTrace();
            aboutMess = ("RegExSearchReplace \n\nAuthor: Hitesh Viseria (hviseria@yahoo.com)");
        }
    }

    private static String readMeMess = null;

    static {
        try {
            readMeMess = FileUtils.fileToString(new File("ReadMe.txt"));
        } catch (Exception ee) {
            ee.printStackTrace();
            readMeMess = ("RegExSearchReplace \n\nAuthor: Hitesh Viseria (hviseria@yahoo.com)");
        }
    }

    private void displayAbout(ActionEvent evt, int search) {
        JOptionPane.showMessageDialog(this, aboutMess, "About", JOptionPane.PLAIN_MESSAGE);
    }

    private void displayReadMe(ActionEvent evt, int search) {
        JOptionPane.showMessageDialog(this, readMeMess, "Read Me", JOptionPane.PLAIN_MESSAGE);
    }
}
