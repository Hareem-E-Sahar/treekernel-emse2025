import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.*;

public class Chooser extends JFrame {

    public static void main(String[] args) {
        new Chooser();
    }

    JButton fileButton;

    File file;

    public Chooser() {
        this.setSize(275, 225);
        this.setTitle("List Model Demo");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ButtonListener bl = new ButtonListener();
        JPanel panel1 = new JPanel();
        fileButton = new JButton("Open");
        fileButton.addActionListener(bl);
        panel1.add(fileButton);
        this.add(panel1);
        this.setVisible(true);
    }

    private File getFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.addChoosableFileFilter(new javaFilter());
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(null);
        File file = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            System.out.println(file.getName());
        }
        return file;
    }

    private class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == fileButton) {
                file = getFile();
            }
        }
    }

    private class javaFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String name = f.getName();
            if (name.matches(".*\\.java")) return true; else return false;
        }

        public String getDescription() {
            return "Java files (*.java)";
        }
    }
}
