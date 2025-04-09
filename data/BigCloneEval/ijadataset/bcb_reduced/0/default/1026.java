import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * dialog opened when a file should be moved to another location
 *
 * @author Andreas Ziermann
 *
 */
class MoveToDlg extends AzDialog {

    static final long serialVersionUID = 6;

    private static JDialog dlg;

    static final int WEST = GridBagConstraints.WEST;

    static final int EAST = GridBagConstraints.EAST;

    static final int NONE = GridBagConstraints.NONE;

    static final int BOTH = GridBagConstraints.BOTH;

    private JTextField destinationPath;

    private JLabel sourceFile;

    void openFileChooser() {
        String where = destinationPath.getText();
        final JFileChooser fc = new JFileChooser(where);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            where = fc.getSelectedFile().getAbsolutePath();
            destinationPath.setText(where);
            ApplicationProperties.PROP.setProperty("moveToFolder", where);
        }
    }

    void moveFileTo() {
        final String what = sourceFile.getText();
        final String where = destinationPath.getText();
        final File f = new File(what);
        final File dest = new File(where + File.separatorChar + f.getName());
        if (!f.renameTo(dest)) {
            JOptionPane.showMessageDialog(this, "Die Datei \"" + what + "\" konnte nicht verschoben werden.", "Verschieben", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final int DEFAULT_SPACE = 5;

    private static final int DLG_SIZE = 500;

    void addComponent(Container c, GridBagLayout gbl, JComponent co, int align, int x, int y, int w, int right_border, int bottom_border, int fill) {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = align;
        gbc.gridwidth = w;
        gbc.ipadx = 2;
        gbc.ipady = 2;
        gbc.fill = fill;
        gbc.insets = new Insets(DEFAULT_SPACE, DEFAULT_SPACE, bottom_border, right_border);
        gbl.setConstraints(co, gbc);
        c.add(co);
    }

    MoveToDlg(String what) {
        super(STR.MOVETODLG, RES.TITLE_ICON, "MoveToDlg", AzApplication.getApplication(), true);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                dlg = null;
            }

            public void windowClosed(WindowEvent e) {
                dlg = null;
            }
        });
        final Container cp = getContentPane();
        final JPanel c = new JPanel();
        cp.add(c);
        final GridBagLayout gbl = new GridBagLayout();
        c.setLayout(gbl);
        addComponent(c, gbl, new JLabel("Quelle:"), WEST, 0, 0, 1, 0, 0, NONE);
        sourceFile = new JLabel(what);
        addComponent(c, gbl, sourceFile, WEST, 1, 0, 1, 0, 0, BOTH);
        addComponent(c, gbl, new JLabel("Ziel:"), WEST, 0, 1, 1, 0, 0, NONE);
        final String where = ApplicationProperties.PROP.getProperty("moveToFolder");
        destinationPath = new JTextField(where);
        if (where == null) {
            openFileChooser();
        }
        final Dimension d = destinationPath.getPreferredSize();
        d.width = DLG_SIZE;
        sourceFile.setPreferredSize(d);
        destinationPath.setPreferredSize(d);
        addComponent(c, gbl, destinationPath, WEST, 1, 1, 1, 0, 0, BOTH);
        final JButton openFileChooser = new JButton("...");
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        };
        openFileChooser.addActionListener(al);
        addComponent(c, gbl, openFileChooser, EAST, 2, 1, 1, DEFAULT_SPACE, 0, NONE);
        final JPanel buttons = new JPanel();
        final GridBagLayout gbl2 = new GridBagLayout();
        buttons.setLayout(gbl2);
        final JButton ok = new JButton("Ok");
        al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveFileTo();
                dispose();
            }
        };
        ok.addActionListener(al);
        addComponent(buttons, gbl2, ok, EAST, 0, 0, 1, 0, 0, NONE);
        final JButton cancel = new JButton("Abbrechen");
        al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        cancel.addActionListener(al);
        addComponent(buttons, gbl2, cancel, EAST, 1, 0, 1, 0, 0, NONE);
        addComponent(c, gbl, buttons, EAST, 1, 2, 2, DEFAULT_SPACE, DEFAULT_SPACE, NONE);
        getRootPane().setDefaultButton(ok);
        pack();
        setResizable(false);
        center();
        dlg = this;
        setVisible(true);
    }

    static void open(String name) {
        if (dlg != null) {
            MoveToDlg.dlg.toFront();
            return;
        }
        new MoveToDlg(name);
    }
}
