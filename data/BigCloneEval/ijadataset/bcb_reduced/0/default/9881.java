import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.tree.*;
import java.io.*;

public class Qus_Sett extends JInternalFrame implements InterInterface {

    public static int p = 0;

    public static int d = 1;

    public static String filepath = null;

    public Qus_Sett() {
        super("Question Setting For Lab Examination", false, true, false, false);
        t.setText("");
    }

    public void Qus_Sett1() {
        this.setBounds(30, 10, 500, 400);
        setVisible(true);
        Container c = getContentPane();
        c.setLayout(null);
        bt11.setBounds(60, 310, 80, 33);
        c.add(bt11);
        bt11.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        bt13.setBounds(160, 310, 80, 33);
        c.add(bt13);
        bt13.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
        bt12.setBounds(260, 310, 80, 33);
        c.add(bt12);
        bt12.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                submitFile();
            }
        });
        bt14.setBounds(360, 310, 80, 33);
        c.add(bt14);
        bt14.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okFile();
            }
        });
        jsp1.setBounds(20, 30, 450, 270);
        c.add(jsp1);
        try {
            UIManager.setLookAndFeel(looks[1].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openFile() {
        JFileChooser filech = new JFileChooser();
        String theLine = "";
        filech.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = filech.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        if (result == JFileChooser.APPROVE_OPTION) {
            File filename = filech.getSelectedFile();
            if (filename == null || filename.getName().equals("")) {
                JOptionPane.showMessageDialog(this, "Invalid File Name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int size = (int) filename.length();
                int char_reads = 0;
                FileReader in = new FileReader(filename);
                char data[] = new char[size];
                while (in.ready()) {
                    char_reads += in.read(data, char_reads, size - char_reads);
                }
                in.close();
                t.setText(new String(data, 0, char_reads));
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Error Openning the file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return;
    }

    private void saveFile() {
        JFileChooser fi = new JFileChooser();
        String theLine = "";
        fi.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fi.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        File fina = fi.getSelectedFile();
        if (fina == null || fina.getName().equals("")) JOptionPane.showMessageDialog(this, "Invalid File Name", "Error", JOptionPane.ERROR_MESSAGE); else {
            try {
                FileWriter out = new FileWriter(fina);
                String text = t.getText();
                out.write(text);
                p = 1;
                out.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error Writting the File", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        filepath = fina.getPath();
    }

    private void okFile() {
        if (t.getText().equals("") || p == 1) {
            this.dispose();
        } else {
            int con = JOptionPane.showConfirmDialog(this, "Do You really want to dispose the \n Question without saving?", "Information", JOptionPane.YES_NO_OPTION);
            if (con == JOptionPane.YES_OPTION) {
                d++;
                t.setText("");
                this.dispose();
            } else if (con == JOptionPane.NO_OPTION) {
                d++;
                System.out.println(d);
            } else return;
        }
    }

    private void submitFile() {
        taf3.setText(filepath);
    }
}
