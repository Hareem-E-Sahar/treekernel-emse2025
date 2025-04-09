import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

public class MainWindow extends JFrame implements ActionListener {

    protected JToolBar tbar;

    protected JButton scanButton, loadButton, beamButton;

    protected DisplayPane dpane;

    protected JFileChooser fc = new JFileChooser();

    public MainWindow() {
        setTitle("BarColdReeder - A bar code reader");
        setSize(640, 480);
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        tbar = new JToolBar();
        tbar.add(scanButton = new JButton("Scan"));
        tbar.add(loadButton = new JButton("Load"));
        tbar.add(beamButton = new JButton("Beam"));
        cp.add(tbar, BorderLayout.NORTH);
        dpane = new DisplayPane();
        cp.add(dpane, BorderLayout.CENTER);
        scanButton.addActionListener(this);
        loadButton.addActionListener(this);
        beamButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == scanButton) {
            JOptionPane.showMessageDialog(this, "Not implemented yet. Sorry!", "Scanning not implemented", JOptionPane.WARNING_MESSAGE);
        } else if (e.getSource() == loadButton) {
            int returnval = fc.showOpenDialog(this);
            if (returnval == JFileChooser.APPROVE_OPTION) dpane.loadFile(fc.getSelectedFile());
        } else if (e.getSource() == beamButton) {
            dpane.applyBeam();
        }
    }
}
