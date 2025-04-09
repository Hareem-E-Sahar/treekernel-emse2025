package net.hawk.digiextractor.GUI;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.hawk.digiextractor.digic.AbstractPVRFileSystem;
import net.hawk.digiextractor.digic.FileSystemFactory;

/**
 * The Class SaveClusters.
 * Implements a simple application that can be used to create images of parts
 * of a DigiCorder medium. For Debugging purposes.
 */
public final class SaveClusters implements ActionListener {

    /** The number of columns in the GUI. */
    private static final int COLUMNS = 2;

    /** The number of rows in the GUI. */
    private static final int ROWS = 3;

    /** The HEXADECIMAL is base 16. */
    private static final int HEXADECIMAL_BASE = 16;

    /** The Constant CLUSTER_SIZE. */
    private static final int CLUSTER_SIZE = 0x10000;

    /** The application frame. */
    private JFrame frame;

    /** The main panel. */
    private JPanel mainPanel;

    /** The image to dump sectors from. */
    private AbstractPVRFileSystem image;

    /** The text field for the start cluster. */
    private JTextField start;

    /** The text field for the end cluster. */
    private JTextField stop;

    /**
	 * Instantiates a new save clusters.
	 */
    private SaveClusters() {
        frame = new JFrame("SaveClusters");
        image = FileSystemFactory.autodetect();
        if (image == null) {
            JOptionPane.showMessageDialog(frame, "Kein DigiCorder Medium gefunden");
            System.exit(1);
        }
        try {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(ROWS, COLUMNS));
            jbInit();
            frame.add(mainPanel);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        frame.pack();
        frame.validate();
        frame.setVisible(true);
    }

    /**
	 * Initialize the GUI elements.
	 */
    private void jbInit() {
        mainPanel.add(new JLabel("Startcluster (hex):"));
        start = new JTextField();
        mainPanel.add(start);
        mainPanel.add(new JLabel("Endcluster (hex):"));
        stop = new JTextField();
        mainPanel.add(stop);
        JButton ok = new JButton("OK");
        ok.setActionCommand("ok");
        ok.addActionListener(this);
        JButton cancel = new JButton("Abbrechen");
        cancel.setActionCommand("cancel");
        cancel.addActionListener(this);
        mainPanel.add(ok);
        mainPanel.add(cancel);
    }

    /**
	 * The main method.
	 *
	 * @param args the arguments
	 */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                new SaveClusters();
            }
        });
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        if (arg0.getActionCommand().equals("ok")) {
            try {
                long begin = Long.parseLong(start.getText(), HEXADECIMAL_BASE);
                long end = Long.parseLong(stop.getText(), HEXADECIMAL_BASE);
                final JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("output.zip"));
                int returnVal = fc.showOpenDialog(frame);
                if (returnVal != JFileChooser.APPROVE_OPTION) {
                    System.exit(0);
                }
                write(begin, end, fc.getSelectedFile());
                JOptionPane.showMessageDialog(frame, "Datei erfolgreich erzeugt");
                System.exit(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Werte �berpr�fen: " + e.getMessage());
            }
        } else {
            System.exit(0);
        }
    }

    /**
	 * Write.
	 * Write the given cluster range into the file provided as an argument.
	 *
	 * @param begin the first cluster to write to file.
	 * @param end the last cluster to write to file.
	 * @param out the output file
	 * @throws Exception if an error occurred.
	 */
    private void write(final long begin, final long end, final File out) throws Exception {
        byte[] array = new byte[CLUSTER_SIZE];
        ByteBuffer buf;
        ZipOutputStream zipfile = new ZipOutputStream(new FileOutputStream(out));
        zipfile.putNextEntry(new ZipEntry("clusters_" + Long.toHexString(begin) + "_" + Long.toHexString(end) + ".dat"));
        for (long i = begin; i <= end; ++i) {
            buf = image.readClusterIntoBuffer(i, false);
            buf.get(array);
            zipfile.write(array);
        }
        zipfile.closeEntry();
        zipfile.close();
    }
}
