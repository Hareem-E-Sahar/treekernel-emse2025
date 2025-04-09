import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.tree.*;
import java.io.*;

public class QuickMResult extends JInternalFrame implements InterInterface {

    public QuickMResult() {
        super("MCQ Result Sheet", false, true, false, false);
    }

    public void QuickMResult1() {
        this.setBounds(30, 10, 500, 400);
        setVisible(true);
        Container c = getContentPane();
        c.setLayout(null);
        jsp9.setBounds(30, 40, 430, 300);
        c.add(jsp9);
        bt32.setBounds(200, 02, 100, 35);
        c.add(bt32);
        bt32.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
        try {
            UIManager.setLookAndFeel(looks[1].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
                String text = t9.getText();
                out.write(text);
                out.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error Writting the File", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
