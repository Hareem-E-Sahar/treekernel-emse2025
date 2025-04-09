import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.ImageFilter;
import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import org.apache.log4j.Logger;

/**
 * @author Maha
 *
 */
public class ImageFrame extends JFrame {

    private static final Logger logger = Logger.getLogger(ImageFrame.class);

    private static final long serialVersionUID = 1L;

    private ImagePanel jPanelImage = null;

    private JButton jButton = null;

    private JButton jButtonRuAll = null;

    private JPanel jContentPane = null;

    private String FileName = App.FILE_NAME;

    private String DirName = DataBaseConnector.MADbaseBaseDir;

    private String CurrSelection = "";

    private JButton jButtonBrowse = null;

    private JTextArea jTextAreaFilename = null;

    private JPanel jPanelRadio;

    private int Type = DataBaseConnector.MADBASE;

    private JRadioButton jRadioButtonMADBASe = null;

    private JRadioButton jRadioButtonADBase = null;

    private JRadioButton jRadioButtonMinist = null;

    ButtonGroup DBgroup = null;

    ButtonGroup DBgroup2 = null;

    JPanel jPanelRadio2 = null;

    private JButton jButtonTrain = null;

    private JTextField jTextFieldFilename = null;

    private JButton jButtonFilename = null;

    private JButton jButtonTrainClass = null;

    private JRadioButton jRadioTrain = null;

    private JRadioButton jRadioTest = null;

    private JRadioButton jRadioTrainAndTest = null;

    private JCheckBox jCheckBoxZero = null;

    private JTextField jTextFieldEXefile = null;

    private JButton jButtonBrowseEXE = null;

    private JLabel jLabel = null;

    private JLabel jLabel1 = null;

    private JLabel jLabel2 = null;

    private JLabel jLabel3 = null;

    private JLabel jLabel4 = null;

    /**
	 * @throws HeadlessException
	 */
    public ImageFrame() throws HeadlessException {
        super();
        initialize();
    }

    /**
	 * @param arg0
	 */
    public ImageFrame(GraphicsConfiguration arg0) {
        super(arg0);
        initialize();
    }

    /**
	 * @param arg0
	 * @throws HeadlessException
	 */
    public ImageFrame(String arg0) throws HeadlessException {
        super(arg0);
        initialize();
    }

    /**
	 * @param arg0
	 * @param arg1
	 */
    public ImageFrame(String arg0, GraphicsConfiguration arg1) {
        super(arg0, arg1);
        initialize();
    }

    /**
	 * This method initializes jPanelImage	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private ImagePanel getJPanelImage() {
        if (jPanelImage == null) {
            jPanelImage = new ImagePanel();
            jPanelImage.setLayout(null);
            jPanelImage.setBounds(37, 159, 196, 186);
        }
        return jPanelImage;
    }

    private JPanel getJPanelRadio() {
        if (jPanelRadio == null) {
            jPanelRadio = new JPanel();
            jPanelRadio.setLayout(null);
            jPanelRadio.setBounds(new Rectangle(50, 105, 352, 36));
            jPanelRadio.add(getJRadioButtonMADBASe(), null);
            jPanelRadio.add(getJRadioButtonADBase(), null);
            jPanelRadio.add(getJRadioButtonMinist(), null);
            DBgroup = new ButtonGroup();
            DBgroup.add(getJRadioButtonMADBASe());
            DBgroup.add(getJRadioButtonADBase());
            DBgroup.add(getJRadioButtonMinist());
        }
        return jPanelRadio;
    }

    private JPanel getJPanelRadio2() {
        if (jPanelRadio2 == null) {
            jPanelRadio2 = new JPanel();
            jPanelRadio2.setLayout(null);
            jPanelRadio2.setBounds(new Rectangle(268, 183, 79, 113));
            jPanelRadio2.add(getJRadioTrain(), null);
            jPanelRadio2.add(getJRadioTest(), null);
            jPanelRadio2.add(getJRadioTrainAndTest(), null);
            DBgroup2 = new ButtonGroup();
            DBgroup2.add(getJRadioTrain());
            DBgroup2.add(getJRadioTest());
            DBgroup2.add(getJRadioTrainAndTest());
        }
        return jPanelRadio;
    }

    /**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("Compute Feature (Only Selected file)");
            jButton.setBounds(new Rectangle(416, 71, 247, 24));
            jButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.out.println("actionPerformed()");
                    getJPanelImage().setImage(App.RunFile(FileName));
                    repaint();
                }
            });
        }
        return jButton;
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setLayout(null);
        this.setSize(689, 433);
        this.setContentPane(getJContentPane());
        this.getContentPane().add(getJButtonRuAll());
        this.setTitle("Frame");
        this.getContentPane().add(getJPanelImage(), null);
        this.getContentPane().add(getJButton(), null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
	 * This method initializes jButtonRuAll	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButtonRuAll() {
        if (jButtonRuAll == null) {
            jButtonRuAll = new JButton();
            jButtonRuAll.setSize(new Dimension(249, 22));
            jButtonRuAll.setText("Compute Statistics (All DB)");
            jButtonRuAll.setLocation(new Point(419, 107));
            jButtonRuAll.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.RunAll(DirName, Type);
                }
            });
        }
        return jButtonRuAll;
    }

    /**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jLabel4 = new JLabel();
            jLabel4.setBounds(new Rectangle(13, 369, 216, 21));
            jLabel4.setText("Write Image ID or Filename");
            jLabel3 = new JLabel();
            jLabel3.setBounds(new Rectangle(7, 79, 201, 18));
            jLabel3.setText("Choose Database :");
            jLabel2 = new JLabel();
            jLabel2.setBounds(new Rectangle(247, 155, 108, 22));
            jLabel2.setText("Data Type :");
            jLabel1 = new JLabel();
            jLabel1.setBounds(new Rectangle(449, 175, 174, 18));
            jLabel1.setText("Select The Classifier EXE");
            jLabel = new JLabel();
            jLabel.setBounds(new Rectangle(47, 4, 425, 18));
            jLabel.setText("Select the Base folder for the database OR  the image file");
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getJButtonRuAll());
            jContentPane.add(getJPanelImage());
            jContentPane.add(getJButton());
            jContentPane.add(getJButtonBrowse(), null);
            jContentPane.add(getJTextAreaFilename(), null);
            jContentPane.add(getJPanelRadio(), null);
            jContentPane.add(getJPanelRadio2(), null);
            jContentPane.add(getJButtonTrain(), null);
            jContentPane.add(getJButtonFilename(), null);
            jContentPane.add(getJButtonTrainClass(), null);
            jContentPane.add(getJTextFieldFilename(), null);
            jContentPane.add(jPanelRadio2, null);
            jContentPane.add(getJCheckBoxZero(), null);
            jContentPane.add(getJTextFieldEXefile(), null);
            jContentPane.add(getJButtonBrowseEXE(), null);
            jContentPane.add(jLabel, null);
            jContentPane.add(jLabel1, null);
            jContentPane.add(jLabel2, null);
            jContentPane.add(jLabel3, null);
            jContentPane.add(jLabel4, null);
        }
        return jContentPane;
    }

    /**
	 * This method initializes jButtonBrowse	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButtonBrowse() {
        if (jButtonBrowse == null) {
            jButtonBrowse = new JButton();
            jButtonBrowse.setBounds(new Rectangle(571, 29, 18, 38));
            jButtonBrowse.setText(".....");
            jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fc = new JFileChooser(new File(DirName));
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    if (fc.showOpenDialog(getJContentPane().getParent()) == JFileChooser.APPROVE_OPTION) {
                        if (fc.getSelectedFile().isFile()) {
                            FileName = fc.getSelectedFile().getAbsolutePath();
                            getJTextAreaFilename().setText(FileName);
                        }
                        DirName = fc.getCurrentDirectory().getAbsolutePath();
                        logger.trace(" setting directory .................." + DirName);
                    }
                }
            });
        }
        return jButtonBrowse;
    }

    public class ImageFileFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File file) {
            return file.getName().toLowerCase().endsWith(".bmp") || file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".jpg");
        }

        public String getDescription() {
            return "Image Files (*.bmp, *.png,*.jpg )";
        }
    }

    /**
	 * This method initializes jTextAreaFilename	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
    private JTextArea getJTextAreaFilename() {
        if (jTextAreaFilename == null) {
            jTextAreaFilename = new JTextArea();
            jTextAreaFilename.setBounds(new Rectangle(48, 27, 516, 41));
            jTextAreaFilename.setEnabled(true);
            jTextAreaFilename.setEditable(false);
            jTextAreaFilename.setRows(2);
            jTextAreaFilename.setLineWrap(true);
            jTextAreaFilename.setText(DirName);
        }
        return jTextAreaFilename;
    }

    /**
	 * This method initializes jRadioButtonMADBASe	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
    private JRadioButton getJRadioButtonMADBASe() {
        if (jRadioButtonMADBASe == null) {
            jRadioButtonMADBASe = new JRadioButton();
            jRadioButtonMADBASe.setBounds(new Rectangle(5, 9, 83, 21));
            jRadioButtonMADBASe.setText("MADBASE");
            jRadioButtonMADBASe.setSelected(true);
            jRadioButtonMADBASe.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (getJRadioButtonMADBASe().isSelected()) DataBaseChanged(DataBaseConnector.MADBASE);
                }
            });
        }
        return jRadioButtonMADBASe;
    }

    private void DataBaseChanged(int t) {
        this.Type = t;
    }

    /**
	 * This method initializes jRadioButtonADBase	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
    private JRadioButton getJRadioButtonADBase() {
        if (jRadioButtonADBase == null) {
            jRadioButtonADBase = new JRadioButton();
            jRadioButtonADBase.setBounds(new Rectangle(137, 10, 73, 21));
            jRadioButtonADBase.setText("ADBASE");
            jRadioButtonADBase.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (getJRadioButtonADBase().isSelected()) Type = DataBaseConnector.ADBASE;
                }
            });
        }
        return jRadioButtonADBase;
    }

    /**
	 * This method initializes jRadioButtonMinist	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
    private JRadioButton getJRadioButtonMinist() {
        if (jRadioButtonMinist == null) {
            jRadioButtonMinist = new JRadioButton();
            jRadioButtonMinist.setBounds(new Rectangle(250, 9, 60, 21));
            jRadioButtonMinist.setText("MInist");
            jRadioButtonMinist.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (getJRadioButtonMinist().isSelected()) Type = DataBaseConnector.MNIST;
                }
            });
        }
        return jRadioButtonMinist;
    }

    /**
	 * This method initializes jButtonTrain	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButtonTrain() {
        if (jButtonTrain == null) {
            jButtonTrain = new JButton();
            jButtonTrain.setBounds(new Rectangle(420, 139, 243, 24));
            jButtonTrain.setText("Generate Data For Classifiers");
            jButtonTrain.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.RunTrain(DirName, Type);
                }
            });
        }
        return jButtonTrain;
    }

    /**
	 * This method initializes jTextFieldFilename	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getJTextFieldFilename() {
        if (jTextFieldFilename == null) {
            jTextFieldFilename = new JTextField();
            jTextFieldFilename.setBounds(new Rectangle(255, 362, 243, 27));
            jTextFieldFilename.setText("Digit Image");
        }
        return jTextFieldFilename;
    }

    /**
	 * This method initializes jButtonFilename	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButtonFilename() {
        if (jButtonFilename == null) {
            jButtonFilename = new JButton();
            jButtonFilename.setBounds(new Rectangle(514, 361, 130, 26));
            jButtonFilename.setText("Display Image");
            jButtonFilename.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJPanelImage().setImage(App.RunGetFile(getJTextFieldFilename().getText(), DirName, Type, FileName));
                    repaint();
                }
            });
        }
        return jButtonFilename;
    }

    /**
	 * This method initializes jButtonTrainClass	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButtonTrainClass() {
        if (jButtonTrainClass == null) {
            jButtonTrainClass = new JButton();
            jButtonTrainClass.setBounds(new Rectangle(459, 238, 203, 47));
            jButtonTrainClass.setText("Train Classifiers");
            jButtonTrainClass.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.StartTrainingClassifers();
                }
            });
        }
        return jButtonTrainClass;
    }

    /**
	 * This method initializes jRadioTrain	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
    private JRadioButton getJRadioTrain() {
        if (jRadioTrain == null) {
            jRadioTrain = new JRadioButton();
            jRadioTrain.setBounds(new Rectangle(7, 20, 79, 21));
            jRadioTrain.setText("Train");
            jRadioTrain.setSelected(true);
            jRadioTrain.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.State = App.TRAIN;
                }
            });
        }
        return jRadioTrain;
    }

    /**
	 * This method initializes jRadioTest	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
    private JRadioButton getJRadioTest() {
        if (jRadioTest == null) {
            jRadioTest = new JRadioButton();
            jRadioTest.setBounds(new Rectangle(7, 48, 62, 21));
            jRadioTest.setText("Test");
            jRadioTest.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.State = App.TEST;
                }
            });
        }
        return jRadioTest;
    }

    /**
	 * This method initializes jRadioTrainAndTest	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
    private JRadioButton getJRadioTrainAndTest() {
        if (jRadioTrainAndTest == null) {
            jRadioTrainAndTest = new JRadioButton();
            jRadioTrainAndTest.setBounds(new Rectangle(8, 72, 62, 21));
            jRadioTrainAndTest.setText("Both");
            jRadioTrainAndTest.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.State = App.TRAIN_THEN_TEST;
                }
            });
        }
        return jRadioTrainAndTest;
    }

    /**
	 * This method initializes jCheckBoxZero	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
    private JCheckBox getJCheckBoxZero() {
        if (jCheckBoxZero == null) {
            jCheckBoxZero = new JCheckBox();
            jCheckBoxZero.setBounds(new Rectangle(264, 309, 117, 21));
            jCheckBoxZero.setText("Use Zero");
            jCheckBoxZero.setSelected(true);
            jCheckBoxZero.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    App.UseZero = jCheckBoxZero.isSelected();
                }
            });
        }
        return jCheckBoxZero;
    }

    /**
	 * This method initializes jTextFieldEXefile	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getJTextFieldEXefile() {
        if (jTextFieldEXefile == null) {
            jTextFieldEXefile = new JTextField();
            jTextFieldEXefile.setBounds(new Rectangle(459, 196, 153, 22));
            jTextFieldEXefile.setText(App.ProgramFile);
            jTextFieldEXefile.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.out.println("actionPerformed()");
                    App.ProgramFile = jTextFieldEXefile.getText();
                    System.out.println(App.ProgramFile);
                }
            });
            jTextFieldEXefile.addFocusListener(new java.awt.event.FocusAdapter() {

                public void focusLost(java.awt.event.FocusEvent e) {
                    System.out.println("actionPerformed()");
                    App.ProgramFile = jTextFieldEXefile.getText();
                    System.out.println(App.ProgramFile);
                }
            });
        }
        return jTextFieldEXefile;
    }

    /**
	 * This method initializes jButtonBrowseEXE	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButtonBrowseEXE() {
        if (jButtonBrowseEXE == null) {
            jButtonBrowseEXE = new JButton();
            jButtonBrowseEXE.setBounds(new Rectangle(645, 193, 21, 20));
            jButtonBrowseEXE.setText("...");
            jButtonBrowseEXE.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fc = new JFileChooser(new File("."));
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    if (fc.showOpenDialog(getJContentPane().getParent()) == JFileChooser.APPROVE_OPTION) {
                        getJTextFieldEXefile().setText(fc.getSelectedFile().getAbsolutePath());
                        App.ProgramFile = fc.getSelectedFile().getAbsolutePath();
                    }
                }
            });
        }
        return jButtonBrowseEXE;
    }
}
