import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class PicToSliceGUI extends JFrame implements ActionListener {

    JMenuBar menuBar;

    JMenu menu;

    JMenuItem menuItem;

    JTextField directoryFirst;

    JButton browseFirstB;

    JTextField directoryLast;

    JButton browseLastB;

    JButton continueB;

    JCheckBox changeCOM;

    JCheckBox hollowOutCB;

    JCheckBox errorCB;

    JSlider shellThickness;

    String userDir = System.getProperties().getProperty("user.dir");

    String firstDirectory;

    String lastDirectory;

    String firstBaseName;

    String lastBaseName;

    int firstNum;

    int lastNum;

    LConstructCellular mainProg;

    boolean finished = false;

    /**
	 * Default constructer
	 */
    public PicToSliceGUI(LConstructCellular prog) {
        mainProg = prog;
        this.setTitle("Input: Select first and last input layers");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        this.setSize(500, 450);
        this.setResizable(false);
        JPanel pane = (JPanel) this.getContentPane();
        createMenuBar();
        JLabel mainL = new JLabel("Specify the location of the images/slices representing ");
        mainL.setFont(new Font("Arial", Font.PLAIN, 12));
        mainL.setBounds(10, 10, 400, 30);
        pane.add(mainL);
        JLabel mainL2 = new JLabel("the first and last layers of the 3D object.");
        mainL2.setFont(new Font("Arial", Font.PLAIN, 12));
        mainL2.setBounds(10, 25, 400, 30);
        pane.add(mainL2);
        JLabel first = new JLabel("First layer location:");
        first.setFont(new Font("Arial", Font.BOLD, 12));
        first.setBounds(10, 60, 400, 30);
        pane.add(first);
        directoryFirst = new JTextField();
        directoryFirst.setBounds(10, 90, 350, 20);
        pane.add(directoryFirst);
        browseFirstB = new JButton("Browse");
        browseFirstB.setBounds(380, 90, 100, 20);
        browseFirstB.addActionListener(this);
        pane.add(browseFirstB);
        JLabel last = new JLabel("Last layer location:");
        last.setFont(new Font("Arial", Font.BOLD, 12));
        last.setBounds(10, 120, 400, 30);
        pane.add(last);
        directoryLast = new JTextField();
        directoryLast.setBounds(10, 150, 350, 20);
        pane.add(directoryLast);
        browseLastB = new JButton("Browse");
        browseLastB.setBounds(380, 150, 100, 20);
        browseLastB.addActionListener(this);
        pane.add(browseLastB);
        continueB = new JButton("Continue");
        continueB.setBounds(380, 190, 100, 20);
        continueB.addActionListener(this);
        pane.add(continueB);
        changeCOM = new JCheckBox("Allow altering inside of sculpture to improve stability", false);
        changeCOM.addActionListener(this);
        changeCOM.setBounds(10, 220, 300, 20);
        pane.add(changeCOM);
        hollowOutCB = new JCheckBox("Hollow out 3D object", true);
        hollowOutCB.addActionListener(this);
        hollowOutCB.setBounds(10, 250, 200, 20);
        pane.add(hollowOutCB);
        JLabel edgeThick = new JLabel("Minimum edge thickness:");
        edgeThick.setFont(new Font("Arial", Font.BOLD, 12));
        edgeThick.setBounds(10, 280, 300, 40);
        pane.add(edgeThick);
        shellThickness = new JSlider(2, 8, 4);
        shellThickness.createStandardLabels(1);
        shellThickness.setSnapToTicks(true);
        shellThickness.setPaintTicks(true);
        shellThickness.setPaintTrack(true);
        shellThickness.setPaintLabels(true);
        shellThickness.setMajorTickSpacing(1);
        shellThickness.setMinorTickSpacing(1);
        shellThickness.setBounds(10, 320, 300, 40);
        pane.add(shellThickness);
        this.rootPane.setDefaultButton(continueB);
        this.setVisible(true);
    }

    /**
	 * The function creates a simple menu bar for the PicToSlice GUI presented to the user.
	 * It gives the user option to select help and About menu. 
	 */
    public void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.add(Box.createHorizontalGlue());
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);
        menuItem = new JMenuItem("Help contents", KeyEvent.VK_I);
        menuItem.setIcon(new ImageIcon("images\\helpIcon.gif"));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu.addSeparator();
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        menuItem = new JMenuItem("About", KeyEvent.VK_A);
        menuItem.addActionListener(this);
        menuItem.setIcon(new ImageIcon("images\\aboutIcon.gif"));
        menu.add(menuItem);
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        setJMenuBar(menuBar);
    }

    /**
	 * This function handles all user interaction with the GUI.
	 */
    public void actionPerformed(ActionEvent action) {
        String name = action.getActionCommand();
        if (action.getSource() == browseFirstB) {
            JFileChooser chooser = new JFileChooser(userDir);
            chooser.addChoosableFileFilter(new modelFileTypeFilter("bmp", "Windows Bitmap image"));
            chooser.addChoosableFileFilter(new modelFileTypeFilter("sl2", "Slice file (colour)"));
            chooser.addChoosableFileFilter(new modelFileTypeFilter("sl", "Slice file"));
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String extension = modelFileTypeFilter.getExtension(chooser.getSelectedFile());
                if (extension.equals("bmp") || extension.equals("sl") || extension.equals("sl2")) {
                    if (Character.isDigit(chooser.getSelectedFile().getAbsolutePath().charAt(0))) {
                        JOptionPane.showMessageDialog(null, "The filename must start with a letter.", "Invalid filename", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (extension.equals("bmp") || extension.equals("sl2")) {
                        if (!Character.isDigit(chooser.getSelectedFile().getAbsolutePath().charAt(chooser.getSelectedFile().getAbsolutePath().length() - 5))) {
                            JOptionPane.showMessageDialog(null, "The filename must end with a digit indicating which layer it is.", "Invalid filename", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        if (!Character.isDigit(chooser.getSelectedFile().getAbsolutePath().charAt(chooser.getSelectedFile().getAbsolutePath().length() - 4))) {
                            JOptionPane.showMessageDialog(null, "The filename must end with a digit indicating which layer it is.", "Invalid filename", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    int lastSlash = chooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\");
                    if (lastSlash == -1) {
                        lastSlash = chooser.getSelectedFile().getAbsolutePath().lastIndexOf("/");
                    }
                    userDir = chooser.getSelectedFile().getAbsolutePath().substring(0, lastSlash + 1);
                    directoryFirst.setText(chooser.getSelectedFile().getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(null, "The file does not have a valid .bmp or .sl extension.", "Invalid file extension", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (action.getSource() == browseLastB) {
            JFileChooser chooser = new JFileChooser(userDir);
            chooser.addChoosableFileFilter(new modelFileTypeFilter("bmp", "Windows Bitmap image"));
            chooser.addChoosableFileFilter(new modelFileTypeFilter("sl2", "Slice file (colour)"));
            chooser.addChoosableFileFilter(new modelFileTypeFilter("sl", "Slice file"));
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String extension = modelFileTypeFilter.getExtension(chooser.getSelectedFile());
                if (extension.equals("bmp") || extension.equals("sl") || extension.equals("sl2")) {
                    if (Character.isDigit(chooser.getSelectedFile().getAbsolutePath().charAt(0))) {
                        JOptionPane.showMessageDialog(null, "The filename must start with a letter.", "Invalid Bitmap filename", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (extension.equals("bmp") || (extension.equals("sl2"))) {
                        if (!Character.isDigit(chooser.getSelectedFile().getAbsolutePath().charAt(chooser.getSelectedFile().getAbsolutePath().length() - 5))) {
                            JOptionPane.showMessageDialog(null, "The filename must end with a digit indicating which layer it is.", "Invalid filename", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        if (!Character.isDigit(chooser.getSelectedFile().getAbsolutePath().charAt(chooser.getSelectedFile().getAbsolutePath().length() - 4))) {
                            JOptionPane.showMessageDialog(null, "The filename must end with a digit indicating which layer it is.", "Invalid filename", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    int lastSlash = chooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\");
                    if (lastSlash == -1) {
                        lastSlash = chooser.getSelectedFile().getAbsolutePath().lastIndexOf("/");
                    }
                    userDir = chooser.getSelectedFile().getAbsolutePath().substring(0, lastSlash + 1);
                    directoryLast.setText(chooser.getSelectedFile().getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(null, "The file does not have a valid bmp extension.", "Invalid Bitmap extension", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (action.getSource() == continueB) {
            if ((directoryFirst.getText().equals("")) || (directoryLast.getText().equals(""))) {
                JOptionPane.showMessageDialog(null, "You must give both the first and last layer locations", "Layer location expected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int lastSlash;
            int index;
            String extension;
            lastSlash = directoryFirst.getText().lastIndexOf("\\");
            if (lastSlash == -1) {
                lastSlash = directoryFirst.getText().lastIndexOf("/");
            }
            firstDirectory = directoryFirst.getText().substring(0, lastSlash + 1);
            firstBaseName = directoryFirst.getText().substring(lastSlash + 1);
            if (firstBaseName.contains(".sl2")) {
                extension = ".sl2";
                firstBaseName = firstBaseName.replaceAll(".sl2", "");
            } else if (firstBaseName.contains(".sl")) {
                extension = ".sl";
                firstBaseName = firstBaseName.replaceAll(".sl", "");
            } else {
                extension = ".bmp";
                firstBaseName = firstBaseName.replaceAll(".bmp", "");
            }
            index = firstBaseName.length() - 1;
            while ((index >= 0) && (Character.isDigit(firstBaseName.charAt(index)))) {
                index--;
            }
            firstNum = Integer.valueOf(firstBaseName.substring(index + 1));
            firstBaseName = firstBaseName.substring(0, index + 1);
            lastSlash = directoryLast.getText().lastIndexOf("\\");
            if (lastSlash == -1) {
                lastSlash = directoryLast.getText().lastIndexOf("/");
            }
            lastDirectory = directoryLast.getText().substring(0, lastSlash + 1);
            lastBaseName = directoryLast.getText().substring(lastSlash + 1);
            if (lastBaseName.contains(".sl2")) {
                if (!extension.equals(".sl2")) {
                    JOptionPane.showMessageDialog(null, "Both files must be of the same type. Either .bmp, .sl or .sl2.", "File types differ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                lastBaseName = lastBaseName.replaceAll(".sl2", "");
            } else if (lastBaseName.contains(".sl")) {
                if (!extension.equals(".sl")) {
                    JOptionPane.showMessageDialog(null, "Both files must be of the same type. Either .bmp, .sl or .sl2.", "File types differ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                lastBaseName = lastBaseName.replaceAll(".sl", "");
            } else {
                if (!extension.equals(".bmp")) {
                    JOptionPane.showMessageDialog(null, "Both files must be of the same type. Either .bmp, .sl or .sl2.", "File types differ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                lastBaseName = lastBaseName.replaceAll(".bmp", "");
            }
            index = lastBaseName.length() - 1;
            while ((index >= 0) && (Character.isDigit(lastBaseName.charAt(index)))) {
                index--;
            }
            lastNum = Integer.valueOf(lastBaseName.substring(index + 1));
            lastBaseName = lastBaseName.substring(0, index + 1);
            if (!firstDirectory.equals(lastDirectory)) {
                JOptionPane.showMessageDialog(null, "The locations of the images must be in the same directory.", "Directories differ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!firstBaseName.equals(lastBaseName)) {
                JOptionPane.showMessageDialog(null, "The base filenames of the images must be the same.", "Base filenames of images differ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (lastNum < firstNum) {
                JOptionPane.showMessageDialog(null, "The number of the last image is smaller that the first image.", "Images must be in increasing order.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            mainProg.directory = firstDirectory;
            mainProg.filenameBase = firstBaseName;
            mainProg.fileExtension = extension;
            mainProg.startIndex = firstNum;
            mainProg.stopIndex = lastNum;
            mainProg.hollowOutModel = hollowOutCB.isSelected();
            mainProg.shellThickness = shellThickness.getValue();
            mainProg.numLayers = (lastNum + 1 - firstNum);
            mainProg.alterCenterOfMass = changeCOM.isSelected();
            finished = true;
        } else if (action.getSource() == hollowOutCB) {
            if (hollowOutCB.isSelected()) {
                shellThickness.setEnabled(true);
            } else {
                shellThickness.setEnabled(false);
            }
        } else if (name.equals("About")) {
            JOptionPane.showMessageDialog(null, "LSculpturer: LConstruct Cellular Automata\n Automated Brick Sculpture Construction Application\nVersion 1.0 \nAuthor : Eugene Smal \nContact: eugene.smal@gmail.com \nStellenbosch University Master's Student" + " \n2008 \n", "About LSculpturer: LConstruct", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("images\\about.gif"));
        } else if (name.equals("Help contents")) {
            helpGUI helpWindow = new helpGUI();
        }
    }
}
