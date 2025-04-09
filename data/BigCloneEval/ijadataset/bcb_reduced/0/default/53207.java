import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import java.util.Vector;

/**
 * main window for JSlideshow application
 *
 * @author Andreas Ziermann
 *
 */
class JSlideShowAppWindow extends AzApplication {

    static final long serialVersionUID = 6;

    private static JFrame aboutDialog;

    private String aboutIcon;

    private String aboutText;

    private AppKeyboardHandler kHandler;

    private ImageView pic;

    public void openHelpWindow() {
        HelpWindow.open(kHandler, pic.getViewPanel());
    }

    private static final int DEFAULT_DISTANCE = 10;

    public void applicationAbout() {
        if (aboutDialog != null) {
            aboutDialog.toFront();
            return;
        }
        final JFrame f = new JFrame(STR.ABOUT.str() + " " + getApplicationName() + " ...");
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                aboutDialog = null;
            }
        });
        aboutDialog = f;
        final BorderLayout bl = new BorderLayout();
        bl.setVgap(DEFAULT_DISTANCE);
        bl.setHgap(DEFAULT_DISTANCE);
        f.getContentPane().setLayout(bl);
        aboutIcon = "images/kview.png";
        if (aboutIcon != null) {
            final ImageIcon ic = new ImageIcon(aboutIcon);
            f.getContentPane().add(new JLabel(ic), BorderLayout.WEST);
        } else {
            f.getContentPane().add(new JPanel(), BorderLayout.WEST);
        }
        final Border bd1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        final JPanel pText = new JPanel();
        pText.setBorder(bd1);
        if (aboutText == null) {
            aboutText = "<html><h1>" + getApplicationName() + "</h1></html>";
        }
        final JLabel lAboutText = new JLabel(aboutText);
        pText.add(lAboutText);
        f.getContentPane().add(pText, BorderLayout.CENTER);
        final JButton ok = new JButton(STR.OK.str());
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                f.setVisible(false);
                f.dispose();
                aboutDialog = null;
            }
        });
        final JPanel pok = new JPanel();
        pok.add(ok);
        f.getContentPane().add(pok, BorderLayout.SOUTH);
        f.getContentPane().add(new JPanel(), BorderLayout.NORTH);
        f.getContentPane().add(new JPanel(), BorderLayout.EAST);
        f.pack();
        f.setResizable(false);
        final Dimension frameDim = f.getSize();
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation((dim.width - frameDim.width) / 2, (dim.height - frameDim.height) / 2);
        f.setVisible(true);
    }

    private ImageDatabase im;

    public void exit() {
        im.exit();
        super.exit();
    }

    public void toggleWindowState() {
        if (!isUndecorated()) {
            setExtendedState(NORMAL);
            dispose();
            setUndecorated(true);
            setExtendedState(MAXIMIZED_BOTH);
            setVisible(true);
        } else {
            setExtendedState(NORMAL);
            dispose();
            setUndecorated(false);
            setVisible(true);
        }
        repaint();
    }

    String[] askForDirectory() {
        JOptionPane.showMessageDialog(this, "parameters missing:\n[properies-file] <directory1> ...", "JSlideShow", JOptionPane.ERROR_MESSAGE);
        final JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        final File[] fList = fc.getSelectedFiles();
        final Vector<String> ret = new Vector<String>();
        for (File name : fList) {
            try {
                ret.addElement(name.getCanonicalPath());
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return (String[]) ret.toArray();
    }

    JSlideShowAppWindow(String[] args) {
        super(STR.TITLE, RES.TITLE_ICON);
        final int numberOfDirectories = args.length;
        if (numberOfDirectories == 0) {
            final String[] dir = askForDirectory();
            im = new ImageDatabase(dir);
        } else {
            im = new ImageDatabase(args);
        }
        pic = new ImageView(im);
        getContentPane().add(pic);
        setVisible(true);
        kHandler = new AppKeyboardHandler(this, pic);
        addKeyListener(kHandler);
    }
}
