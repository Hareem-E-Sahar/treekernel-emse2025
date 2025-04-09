import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.awt.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.sql.*;
import java.io.*;

/**
 *
 * @author  Administrator
 */
public class entData extends javax.swing.JFrame {

    JFileChooser fc;

    FileFilter filter1;

    JFrame frame;

    public entData() {
        initComponents();
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                exit2();
            }
        });
    }

    public void exit2() {
        this.dispose();
        frame.enable();
        frame.show();
    }

    private void initComponents() {
        setResizable(false);
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        sSelB = new javax.swing.JButton();
        saveT = new javax.swing.JTextField();
        saveB = new javax.swing.JButton();
        cSelB = new javax.swing.JButton();
        chanT = new javax.swing.JTextField();
        chanB = new javax.swing.JButton();
        saveB.disable();
        chanB.disable();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBounds(new java.awt.Rectangle(110, 100, 0, 0));
        setMinimumSize(new java.awt.Dimension(600, 300));
        JPanel panel = new JPanel() {

            {
                setOpaque(false);
            }

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, Color.green, 200, 180, Color.white, false));
                g2d.fill(new Rectangle2D.Double(0, 0, 600, 500));
            }
        };
        panel.setLayout(null);
        panel.setBounds(0, 0, 0, 0);
        panel.setPreferredSize(new Dimension(600, 300));
        panel.setBackground(new java.awt.Color(217, 255, 217));
        getContentPane().add(panel);
        panel.add(jSeparator1);
        jSeparator1.setBounds(80, 230, 460, 20);
        panel.add(jSeparator2);
        jSeparator2.setBounds(80, 70, 140, 10);
        panel.add(jSeparator3);
        jSeparator3.setBounds(500, 70, 40, 10);
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel1.setForeground(new Color(0, 128, 0));
        jLabel1.setText("������ ����� ������ ������ ��� ����� �������� ��");
        panel.add(jLabel1);
        jLabel1.setBounds(225, 60, 300, 15);
        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        panel.add(jSeparator4);
        jSeparator4.setBounds(540, 70, 10, 160);
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        panel.add(jSeparator5);
        jSeparator5.setBounds(80, 70, 10, 160);
        jSeparator6.setBounds(80, 320, 450, 20);
        jSeparator7.setBounds(80, 220, 300, 10);
        jSeparator8.setBounds(500, 220, 30, 10);
        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel2.setText("تغيير قاعدة البيانات");
        jLabel2.setForeground(new Color(0, 128, 0));
        jLabel2.setBounds(390, 210, 150, 15);
        jSeparator9.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator9.setBounds(530, 220, 10, 100);
        jSeparator10.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator10.setBounds(80, 220, 10, 100);
        sSelB.setIcon(new javax.swing.ImageIcon(getClass().getResource("sel.JPG")));
        sSelB.setBorderPainted(false);
        sSelB.setContentAreaFilled(false);
        sSelB.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("sel2.JPG")));
        sSelB.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("sel2.JPG")));
        sSelB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sSelBActionPerformed(evt);
            }
        });
        panel.add(sSelB);
        sSelB.setBounds(122, 115, 84, 30);
        panel.add(saveT);
        saveT.setBounds(210, 117, 310, 25);
        saveB.setIcon(new javax.swing.ImageIcon(getClass().getResource("saved.JPG")));
        saveB.setBorderPainted(false);
        saveB.setContentAreaFilled(false);
        saveB.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("saved2.JPG")));
        saveB.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("saved2.JPG")));
        saveB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    saveBActionPerformed(evt);
                } catch (IOException ex) {
                    Logger.getLogger(entData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        panel.add(saveB);
        saveB.setBounds(122, 160, 84, 30);
        cSelB.setIcon(new javax.swing.ImageIcon(getClass().getResource("sel.JPG")));
        cSelB.setBorderPainted(false);
        cSelB.setContentAreaFilled(false);
        cSelB.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("sel2.JPG")));
        cSelB.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("sel2.JPG")));
        cSelB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cSelBActionPerformed(evt);
            }
        });
        cSelB.setBounds(122, 240, 84, 30);
        panel.add(chanT);
        chanB.setIcon(new javax.swing.ImageIcon(getClass().getResource("upd.JPG")));
        chanB.setBorderPainted(false);
        chanB.setContentAreaFilled(false);
        chanB.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("upd2.JPG")));
        chanB.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("upd2.JPG")));
        chanB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chanBActionPerformed(evt);
            }
        });
        chanB.setBounds(122, 280, 84, 30);
        pack();
    }

    private void chanBActionPerformed(java.awt.event.ActionEvent evt) {
        String k = chanT.getText();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("entData.txt"));
            out.write(k);
            out.close();
            JOptionPane.showMessageDialog(null, "�� ����� ����� �������� �����");
        } catch (Exception o) {
            JOptionPane.showMessageDialog(null, "����� ���� ����� �� �������");
        }
    }

    private void cSelBActionPerformed(java.awt.event.ActionEvent evt) {
        String ff = OpenFile();
        chanT.setText(ff);
        chanB.enable();
    }

    private void saveBActionPerformed(java.awt.event.ActionEvent evt) throws IOException {
        File file = new File("index.java");
        String path = file.getAbsolutePath();
        path = path.replaceAll("index.java", "mmm.jar");
        String nPath = saveT.getText();
        JOptionPane.showMessageDialog(null, "jar path=" + path + "\ndes=" + nPath);
        extract ext = new extract();
        ext.ext(path, nPath);
        InputStream instream = getClass().getResourceAsStream("database.txt");
        InputStreamReader infile = new InputStreamReader(instream);
        BufferedReader inbuf = new BufferedReader(infile);
        try {
            String file2 = inbuf.readLine();
        } catch (IOException ex) {
            Logger.getLogger(entData.class.getName()).log(Level.SEVERE, null, ex);
        }
        inbuf.close();
    }

    private void sSelBActionPerformed(java.awt.event.ActionEvent evt) {
        String ff = OpenFolder();
        saveT.setText(ff);
        saveB.enable();
    }

    public String OpenFile() {
        filter1 = new ExtensionFileFilter("Microsoft Office Access (*.mdb)", "mdb");
        fc = new JFileChooser();
        fc.setFileFilter(filter1);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            return file.getPath();
        } else {
            return null;
        }
    }

    public String OpenFolder() {
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(filter1);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            return file.getPath();
        } else {
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new entData().setVisible(true);
            }
        });
    }

    private javax.swing.JButton cSelB;

    private javax.swing.JButton chanB;

    private javax.swing.JTextField chanT;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSeparator jSeparator10;

    private javax.swing.JSeparator jSeparator2;

    private javax.swing.JSeparator jSeparator3;

    private javax.swing.JSeparator jSeparator4;

    private javax.swing.JSeparator jSeparator5;

    private javax.swing.JSeparator jSeparator6;

    private javax.swing.JSeparator jSeparator7;

    private javax.swing.JSeparator jSeparator8;

    private javax.swing.JSeparator jSeparator9;

    private javax.swing.JButton sSelB;

    private javax.swing.JButton saveB;

    private javax.swing.JTextField saveT;
}
