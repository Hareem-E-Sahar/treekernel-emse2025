import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.griffin.coushe.Color;
import com.griffin.coushe.ColorScheme;
import com.griffin.coushe.ColorSet;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import javax.swing.border.*;

public class CousheApp extends javax.swing.JFrame {

    JPanel jPanelColor;

    JCheckBox jCheckTransparent;

    JTextField jTextBlue;

    JLabel jLabelBlue;

    JTextField jTextGreen;

    JLabel jLabelGreen;

    JTextField jTextRed;

    JLabel jLabelRed;

    JList jListColors;

    JScrollPane jScrollPane;

    JLabel jLabelName;

    JTextField jTextName;

    JPanel jPanelRight;

    JPanel jPanelLeft;

    JMenuItem helpMenuItem;

    JMenu jMenu5;

    JMenuItem deleteMenuItem;

    JSeparator jSeparator1;

    JMenuItem pasteMenuItem;

    JMenuItem copyMenuItem;

    JMenuItem cutMenuItem;

    JMenu jMenu4;

    JMenuItem exitMenuItem;

    JSeparator jSeparator2;

    JMenuItem closeFileMenuItem;

    JMenuItem saveAsMenuItem;

    JMenuItem saveMenuItem;

    JMenuItem openFileMenuItem;

    JMenuItem newFileMenuItem;

    JMenu jMenu3;

    JMenuBar jMenuBar1;

    private String asLastPressed = "";

    public static final String MENU_FILES_NEW = "FILES_NEW";

    public static final String MENU_FILES_EXIT = "FILES_EXIT";

    public static final String MENU_FILES_OPEN = "FILES_OPEN";

    public static final String MENU_FILES_SAVE = "FILES_SAVE";

    public static final String MENU_FILES_SAVEAS = "FILES_SAVEAS";

    public static final String MENU_HELP_ABOUT = "HELP_ABOUT";

    private static String lastAccessedFilename = null;

    private ColorScheme aoColorScheme = new ColorScheme();

    private short aCurrentColor = 0;

    public CousheApp() {
        initGUI();
        refreshListColors();
        refreshAll();
        jListColors.setSelectedIndex(this.aCurrentColor);
    }

    /**
	* Auto-generated code - any changes you make will disappear!!!
	*/
    public void initGUI() {
        try {
            jPanelLeft = new JPanel();
            jLabelName = new JLabel();
            jTextName = new JTextField();
            jScrollPane = new JScrollPane();
            jListColors = new JList();
            jPanelRight = new JPanel();
            jLabelRed = new JLabel();
            jTextRed = new JTextField();
            jLabelGreen = new JLabel();
            jTextGreen = new JTextField();
            jLabelBlue = new JLabel();
            jTextBlue = new JTextField();
            jCheckTransparent = new JCheckBox();
            jPanelColor = new JPanel();
            BoxLayout thisLayout = new BoxLayout(this.getContentPane(), 0);
            this.getContentPane().setLayout(thisLayout);
            this.setDefaultCloseOperation(0);
            this.setResizable(false);
            this.setTitle("Coushe v0.3.0");
            this.setUndecorated(false);
            this.setMaximizedBounds(new java.awt.Rectangle(0, 0, 600, 500));
            this.getContentPane().setSize(new java.awt.Dimension(457, 399));
            this.setLocation(new java.awt.Point(410, 400));
            this.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent evt) {
                    SwingAppWindowClosing(evt);
                }
            });
            FlowLayout jPanelLeftLayout = new FlowLayout();
            jPanelLeft.setLayout(jPanelLeftLayout);
            jPanelLeftLayout.setAlignment(0);
            jPanelLeftLayout.setHgap(5);
            jPanelLeftLayout.setVgap(5);
            jPanelLeft.setVisible(true);
            jPanelLeft.setPreferredSize(new java.awt.Dimension(250, 372));
            jPanelLeft.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            jPanelLeft.setLocation(new java.awt.Point(1, 1));
            jPanelLeft.setBounds(new java.awt.Rectangle(0, 0, 267, 372));
            jPanelLeft.setIgnoreRepaint(false);
            this.getContentPane().add(jPanelLeft);
            jLabelName.setLayout(null);
            jLabelName.setText("Color scheme name:");
            jLabelName.setVisible(true);
            jLabelName.setPreferredSize(new java.awt.Dimension(224, 16));
            jLabelName.setBounds(new java.awt.Rectangle(5, 5, 224, 16));
            jPanelLeft.add(jLabelName);
            jTextName.setText("jTextName");
            jTextName.setVisible(true);
            jTextName.setPreferredSize(new java.awt.Dimension(136, 20));
            jTextName.setBounds(new java.awt.Rectangle(127, 5, 136, 20));
            jPanelLeft.add(jTextName);
            jScrollPane.setHorizontalScrollBarPolicy(30);
            jScrollPane.setVisible(true);
            jScrollPane.setPreferredSize(new java.awt.Dimension(228, 293));
            jScrollPane.setBounds(new java.awt.Rectangle(5, 51, 228, 293));
            jPanelLeft.add(jScrollPane);
            jListColors.setLayout(null);
            jListColors.setVisible(true);
            jScrollPane.add(jListColors);
            jScrollPane.setViewportView(jListColors);
            jListColors.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent evt) {
                    jListColorsValueChanged(evt);
                }
            });
            FlowLayout jPanelRightLayout = new FlowLayout();
            jPanelRight.setLayout(jPanelRightLayout);
            jPanelRightLayout.setAlignment(1);
            jPanelRightLayout.setHgap(5);
            jPanelRightLayout.setVgap(5);
            jPanelRight.setVisible(true);
            jPanelRight.setForeground(new java.awt.Color(0, 0, 0));
            jPanelRight.setPreferredSize(new java.awt.Dimension(165, 372));
            jPanelRight.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            jPanelRight.setBounds(new java.awt.Rectangle(267, 0, 182, 372));
            this.getContentPane().add(jPanelRight);
            jLabelRed.setLayout(null);
            jLabelRed.setText("Red:");
            jLabelRed.setVisible(true);
            jLabelRed.setPreferredSize(new java.awt.Dimension(60, 20));
            jPanelRight.add(jLabelRed);
            jTextRed.setText("");
            jTextRed.setVisible(true);
            jTextRed.setPreferredSize(new java.awt.Dimension(60, 20));
            jPanelRight.add(jTextRed);
            jTextRed.addFocusListener(new FocusAdapter() {

                public void focusLost(FocusEvent evt) {
                    jTextRedFocusLost(evt);
                }
            });
            jTextRed.addKeyListener(new KeyAdapter() {

                public void keyTyped(KeyEvent evt) {
                    jTextRedKeyTyped(evt);
                }
            });
            jLabelGreen.setLayout(null);
            jLabelGreen.setText("Green:");
            jLabelGreen.setVisible(true);
            jLabelGreen.setPreferredSize(new java.awt.Dimension(60, 20));
            jLabelGreen.setBounds(new java.awt.Rectangle(28, 30, 60, 20));
            jPanelRight.add(jLabelGreen);
            jTextGreen.setText("");
            jTextGreen.setVisible(true);
            jTextGreen.setPreferredSize(new java.awt.Dimension(60, 20));
            jPanelRight.add(jTextGreen);
            jTextGreen.addFocusListener(new FocusAdapter() {

                public void focusLost(FocusEvent evt) {
                    jTextGreenFocusLost(evt);
                }
            });
            jTextGreen.addKeyListener(new KeyAdapter() {

                public void keyTyped(KeyEvent evt) {
                    jTextGreenKeyTyped(evt);
                }
            });
            jLabelBlue.setLayout(null);
            jLabelBlue.setText("Blue:");
            jLabelBlue.setVisible(true);
            jLabelBlue.setPreferredSize(new java.awt.Dimension(60, 20));
            jPanelRight.add(jLabelBlue);
            jTextBlue.setText("");
            jTextBlue.setVisible(true);
            jTextBlue.setPreferredSize(new java.awt.Dimension(60, 20));
            jTextBlue.setBounds(new java.awt.Rectangle(103, 55, 60, 20));
            jPanelRight.add(jTextBlue);
            jTextBlue.addFocusListener(new FocusAdapter() {

                public void focusLost(FocusEvent evt) {
                    jTextBlueFocusLost(evt);
                }
            });
            jTextBlue.addKeyListener(new KeyAdapter() {

                public void keyTyped(KeyEvent evt) {
                    jTextBlueKeyTyped(evt);
                }
            });
            jCheckTransparent.setText("Transparent");
            jCheckTransparent.setVisible(true);
            jPanelRight.add(jCheckTransparent);
            jCheckTransparent.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent evt) {
                    jCheckTransparentStateChanged(evt);
                }
            });
            FlowLayout jPanelColorLayout = new FlowLayout();
            jPanelColor.setLayout(jPanelColorLayout);
            jPanelColor.setVisible(true);
            jPanelColor.setPreferredSize(new java.awt.Dimension(80, 80));
            jPanelColor.setBorder(new EtchedBorder(1, null, null));
            jPanelRight.add(jPanelColor);
            jMenuBar1 = new JMenuBar();
            jMenu3 = new JMenu();
            newFileMenuItem = new JMenuItem();
            openFileMenuItem = new JMenuItem();
            saveMenuItem = new JMenuItem();
            saveAsMenuItem = new JMenuItem();
            closeFileMenuItem = new JMenuItem();
            jSeparator2 = new JSeparator();
            exitMenuItem = new JMenuItem();
            jMenu5 = new JMenu();
            helpMenuItem = new JMenuItem();
            setJMenuBar(jMenuBar1);
            jMenu3.setText("File");
            jMenu3.setVisible(true);
            jMenuBar1.add(jMenu3);
            newFileMenuItem.setText("New");
            newFileMenuItem.setVisible(true);
            newFileMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(newFileMenuItem);
            newFileMenuItem.addMouseListener(new MouseAdapter() {

                public void mouseReleased(MouseEvent evt) {
                    newFileMenuItemMouseReleased(evt);
                }

                public void mousePressed(MouseEvent evt) {
                    newFileMenuItemMousePressed(evt);
                }
            });
            openFileMenuItem.setText("Open");
            openFileMenuItem.setVisible(true);
            openFileMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(openFileMenuItem);
            openFileMenuItem.addMouseListener(new MouseAdapter() {

                public void mouseReleased(MouseEvent evt) {
                    openFileMenuItemMouseReleased(evt);
                }

                public void mousePressed(MouseEvent evt) {
                    openFileMenuItemMousePressed(evt);
                }
            });
            saveMenuItem.setText("Save");
            saveMenuItem.setVisible(true);
            saveMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(saveMenuItem);
            saveMenuItem.addMouseListener(new MouseAdapter() {

                public void mouseReleased(MouseEvent evt) {
                    saveMenuItemMouseReleased(evt);
                }

                public void mousePressed(MouseEvent evt) {
                    saveMenuItemMousePressed(evt);
                }
            });
            saveAsMenuItem.setText("Save As ...");
            saveAsMenuItem.setVisible(true);
            saveAsMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(saveAsMenuItem);
            saveAsMenuItem.addMouseListener(new MouseAdapter() {

                public void mouseReleased(MouseEvent evt) {
                    saveAsMenuItemMouseReleased(evt);
                }

                public void mousePressed(MouseEvent evt) {
                    saveAsMenuItemMousePressed(evt);
                }
            });
            closeFileMenuItem.setText("Close");
            closeFileMenuItem.setVisible(true);
            closeFileMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(closeFileMenuItem);
            jSeparator2.setLayout(null);
            jSeparator2.setVisible(true);
            jSeparator2.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(jSeparator2);
            exitMenuItem.setText("Exit");
            exitMenuItem.setVisible(true);
            exitMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu3.add(exitMenuItem);
            exitMenuItem.addMouseListener(new MouseAdapter() {

                public void mouseReleased(MouseEvent evt) {
                    exitMenuItemMouseReleased(evt);
                }

                public void mousePressed(MouseEvent evt) {
                    exitMenuItemMousePressed(evt);
                }
            });
            jMenu5.setText("Help");
            jMenu5.setVisible(true);
            jMenuBar1.add(jMenu5);
            helpMenuItem.setText("Help");
            helpMenuItem.setVisible(true);
            helpMenuItem.setBounds(new java.awt.Rectangle(5, 5, 60, 30));
            jMenu5.add(helpMenuItem);
            helpMenuItem.addMouseListener(new MouseAdapter() {

                public void mouseReleased(MouseEvent evt) {
                    helpMenuItemMouseReleased(evt);
                }

                public void mousePressed(MouseEvent evt) {
                    helpMenuItemMousePressed(evt);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
										 * Ask user for quit confirmation
										 * @return <code>true</code> if user approves quiting, or <code>false</code> if user refuse
										 */
    private boolean exitApproved() {
        int iUserChoice = JOptionPane.showOptionDialog(this, "Quit?  Now?!", "Exit confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        return iUserChoice == JOptionPane.YES_OPTION;
    }

    /** Auto-generated main method */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            CousheApp inst = new CousheApp();
            inst.setSize(300, 200);
            inst.pack();
            inst.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	* Auto-generated code - any changes you make will disappear!!!
	* This static method creates a new instance of this class and shows
	* it inside a new JFrame, (unless it is already a JFrame).
	*/
    public static void showGUI() {
        try {
            CousheApp inst = new CousheApp();
            inst.pack();
            inst.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Auto-generated event handler method */
    public void exitMenuItemMousePressed(MouseEvent evt) {
        this.asLastPressed = MENU_FILES_EXIT;
    }

    /** Auto-generated event handler method */
    public void exitMenuItemMouseReleased(MouseEvent evt) {
        if (!MENU_FILES_EXIT.equals(this.asLastPressed)) return;
        if (this.exitApproved()) {
            System.exit(0);
        }
    }

    /** Auto-generated event handler method */
    public void SwingAppWindowClosing(WindowEvent evt) {
        if (this.exitApproved()) {
            System.exit(0);
        }
    }

    protected void refreshListColors() {
        jListColors.setModel(new AbstractListModel() {

            public Object getElementAt(int i) {
                String header = com.griffin.coushe.Color.COLORVALUE_FORMATTER.format(i + 1) + " - ";
                return header + ColorScheme.getColorDescription((short) i);
            }

            ;

            public int getSize() {
                return ColorSet.COLORSET_SIZE;
            }

            ;
        });
        jScrollPane.remove(jListColors);
        int width = 160;
        int height = jListColors.getModel().getSize() * 17;
        jListColors.setPreferredSize(new Dimension(width, height));
        jScrollPane.add(jListColors);
        jScrollPane.setViewportView(jListColors);
    }

    private void refreshAll() {
        this.jTextName.setText(aoColorScheme.getName());
        this.refreshValues();
        this.refreshColorPanel();
    }

    /**
			 * Refresh values of the RGBA components
			 *
			 */
    public void refreshValues() {
        com.griffin.coushe.Color loCurColor = this.aoColorScheme.getColorSet().getColor(this.aCurrentColor);
        this.jTextRed.setText(Short.toString(loCurColor.getRed()));
        this.jTextGreen.setText(Short.toString(loCurColor.getGreen()));
        this.jTextBlue.setText(Short.toString(loCurColor.getBlue()));
        this.jCheckTransparent.setSelected(loCurColor.isTransparent());
    }

    /** Auto-generated event handler method */
    public void jListColorsValueChanged(ListSelectionEvent evt) {
        short selectedIndex = (short) this.jListColors.getSelectedIndex();
        if (evt.getValueIsAdjusting()) {
            return;
        }
        if (selectedIndex == this.aCurrentColor) {
            return;
        }
        this.aCurrentColor = selectedIndex;
        this.refreshValues();
        this.refreshColorPanel();
    }

    /** Auto-generated event handler method */
    public void openFileMenuItemMousePressed(MouseEvent evt) {
        this.asLastPressed = MENU_FILES_OPEN;
    }

    /** Auto-generated event handler method */
    public void openFileMenuItemMouseReleased(MouseEvent evt) {
        if (!MENU_FILES_OPEN.equals(this.asLastPressed)) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File file) {
                String filename = file.getName();
                if (file.isDirectory()) return true;
                return filename.endsWith(".col");
            }

            public String getDescription() {
                return "(*.col) ColorScheme files";
            }
        });
        int result = fileChooser.showOpenDialog(this);
        switch(result) {
            case JFileChooser.CANCEL_OPTION:
                System.out.println("openFileMenuItemMouseReleased() Cancel");
                break;
            case JFileChooser.ERROR_OPTION:
                System.out.println("openFileMenuItemMouseReleased() Error");
                break;
            case JFileChooser.APPROVE_OPTION:
                String selFile = fileChooser.getSelectedFile().getAbsolutePath();
                CousheApp.lastAccessedFilename = selFile;
                this.aoColorScheme.loadColorScheme(selFile);
                this.refreshAll();
        }
    }

    /** Auto-generated event handler method */
    public void saveAsMenuItemMousePressed(MouseEvent evt) {
        this.asLastPressed = MENU_FILES_SAVEAS;
    }

    /** Auto-generated event handler method */
    public void saveAsMenuItemMouseReleased(MouseEvent evt) {
        if (!MENU_FILES_SAVEAS.equals(this.asLastPressed)) return;
        this.doSaveAs();
    }

    protected void doSaveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File file) {
                String filename = file.getName();
                if (file.isDirectory()) return true;
                return filename.endsWith(".col");
            }

            public String getDescription() {
                return "(*.col) ColorScheme files";
            }
        });
        int result = fileChooser.showSaveDialog(this);
        switch(result) {
            case JFileChooser.CANCEL_OPTION:
                System.out.println("openFileMenuItemMouseReleased() Cancel");
                break;
            case JFileChooser.ERROR_OPTION:
                System.out.println("openFileMenuItemMouseReleased() Error");
                break;
            case JFileChooser.APPROVE_OPTION:
                String selFile = fileChooser.getSelectedFile().getAbsolutePath();
                if (!selFile.toLowerCase().endsWith(".col")) {
                    selFile = selFile + ".col";
                }
                CousheApp.lastAccessedFilename = selFile;
                this.aoColorScheme.saveColorScheme(selFile);
                this.refreshAll();
        }
    }

    /** Auto-generated event handler method */
    public void saveMenuItemMousePressed(MouseEvent evt) {
        this.asLastPressed = MENU_FILES_SAVE;
    }

    /** Auto-generated event handler method */
    public void saveMenuItemMouseReleased(MouseEvent evt) {
        if (!MENU_FILES_SAVE.equals(this.asLastPressed)) return;
        if (null == CousheApp.lastAccessedFilename) {
            this.doSaveAs();
        } else {
            this.aoColorScheme.saveColorScheme(CousheApp.lastAccessedFilename);
            this.refreshAll();
        }
    }

    /**
			 * Validates a input from the keyboard for a textfield - accepts up to 3
			 * digits and some special keys like backspace, arrows etc.
			 * @param evt
			 */
    private void processTypedKey(KeyEvent evt) {
        char keytyped = evt.getKeyChar();
        if (keytyped == KeyEvent.VK_BACK_SPACE) {
            return;
        }
        if (keytyped < KeyEvent.VK_0 || keytyped > KeyEvent.VK_9) {
            evt.consume();
            return;
        }
        JTextField loTextField = (JTextField) evt.getComponent();
        if ((loTextField.getText().length() >= 3) && (loTextField.getSelectedText() == null)) {
            evt.consume();
            return;
        }
    }

    /** Auto-generated event handler method */
    public void jTextRedKeyTyped(KeyEvent evt) {
        processTypedKey(evt);
    }

    /** Auto-generated event handler method */
    public void jTextGreenKeyTyped(KeyEvent evt) {
        processTypedKey(evt);
    }

    /** Auto-generated event handler method */
    public void jTextBlueKeyTyped(KeyEvent evt) {
        processTypedKey(evt);
    }

    /**
		 * Validates the text typed in a text field. The given value is bounded to 255,
		 * if it is greater, the text is changed. 
		 * @param evt
		 * @return The validated value [0-255].
		 */
    private short processLostFocus(FocusEvent evt) {
        JTextField loTextField = (JTextField) evt.getComponent();
        String lsText = loTextField.getText();
        if (lsText.length() == 0) {
            return 0;
        }
        short value = Short.parseShort(lsText);
        if (value > 255) {
            value = 255;
            loTextField.setText("255");
        }
        return value;
    }

    /**
		 *  Sets a new value to the color's component
		 */
    public void jTextRedFocusLost(FocusEvent evt) {
        Color loCurColor = this.aoColorScheme.getColorSet().getColor(this.aCurrentColor);
        short value = processLostFocus(evt);
        loCurColor.setRed(value);
        this.refreshColorPanel();
    }

    /**
	 *  Sets a new value to the color's component
	 */
    public void jTextGreenFocusLost(FocusEvent evt) {
        Color loCurColor = this.aoColorScheme.getColorSet().getColor(this.aCurrentColor);
        short value = processLostFocus(evt);
        loCurColor.setGreen(value);
        this.refreshColorPanel();
    }

    /**
	 *  Sets a new value to the color's component
	 */
    public void jTextBlueFocusLost(FocusEvent evt) {
        Color loCurColor = this.aoColorScheme.getColorSet().getColor(this.aCurrentColor);
        short value = processLostFocus(evt);
        loCurColor.setBlue(value);
        this.refreshColorPanel();
    }

    /**
	 * Sets a new state to the color's transparantion
	 */
    public void jCheckTransparentStateChanged(ChangeEvent evt) {
        Color loCurColor = this.aoColorScheme.getColorSet().getColor(this.aCurrentColor);
        loCurColor.setTransparent(jCheckTransparent.isSelected());
    }

    /** Auto-generated event handler method */
    public void newFileMenuItemMousePressed(MouseEvent evt) {
        this.asLastPressed = MENU_FILES_NEW;
    }

    /** Auto-generated event handler method */
    public void newFileMenuItemMouseReleased(MouseEvent evt) {
        if (!MENU_FILES_NEW.equals(this.asLastPressed)) return;
        System.out.println("New color scheme: Not implemented yet, please be patient.");
    }

    /** Auto-generated event handler method */
    public void helpMenuItemMousePressed(MouseEvent evt) {
        this.asLastPressed = MENU_HELP_ABOUT;
    }

    /** Auto-generated event handler method */
    public void helpMenuItemMouseReleased(MouseEvent evt) {
        if (!MENU_HELP_ABOUT.equals(this.asLastPressed)) return;
        System.out.println("======================================================================");
        System.out.println("  Coushe - The ColorScheme Editor, Copyright (C) 2004 Duncan Griffin");
        System.out.println("======================================================================");
    }

    /**
	 * Sets a new background to the color panel with the currently
	 * selected color.
	 */
    public void refreshColorPanel() {
        Color loCurColor = this.aoColorScheme.getColorSet().getColor(this.aCurrentColor);
        java.awt.Color loAWT_Color = new java.awt.Color(loCurColor.getRed(), loCurColor.getGreen(), loCurColor.getBlue());
        this.jPanelColor.setBackground(loAWT_Color);
    }
}
