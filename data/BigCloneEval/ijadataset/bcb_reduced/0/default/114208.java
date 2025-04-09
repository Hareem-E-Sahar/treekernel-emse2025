import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.io.*;
import java.rmi.*;
import java.net.*;

public class MCQTest extends JInternalFrame implements InterInterface, InternalFrameListener {

    public JPanel pan = new JPanel();

    public JPanel pan1 = new JPanel();

    public LineBorder lb = new LineBorder(Color.blue, 2);

    public LineBorder lb1 = new LineBorder(Color.blue, 2);

    public int i;

    public static int ra;

    public String sa;

    public int raj;

    String str1;

    JLabel plab;

    public MCQTest() {
        super("MCQ Test", false, true, false, false);
        this.setBounds(25, 10, 500, 410);
        setVisible(true);
        Container c = getContentPane();
        c.setLayout(null);
        lab4.setText("\t                 MCQ Test");
        lab4.setFont(new Font("Verdana", Font.BOLD, 16));
        lab4.setEditable(false);
        lab4.setForeground(Color.black);
        lab4.setBackground(Color.gray);
        lab4.setBounds(0, 0, 500, 25);
        c.add(lab4);
        bt26.setBounds(100, 30, 300, 40);
        bt26.setFont(new Font("Verdana", Font.PLAIN, 14));
        c.add(bt26);
        btquick.setBounds(100, 200, 300, 40);
        btquick.setFont(new Font("Verdana", Font.PLAIN, 14));
        c.add(btquick);
        pan.setLayout(null);
        pan.setBounds(20, 75, 450, 120);
        pan.setBackground(Color.gray);
        lab20.setBounds(70, 0, 350, 30);
        lab20.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan.add(lab20);
        txa02.setBounds(70, 30, 100, 20);
        txa02.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan.add(txa02);
        c.add(pan);
        pan1.setLayout(null);
        pan1.setBounds(20, 245, 450, 120);
        c.add(pan1);
        label01.setBounds(70, 0, 350, 30);
        label01.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan1.add(label01);
        tfield01.setBounds(70, 30, 170, 20);
        tfield01.setFont(new Font("Verdana", Font.PLAIN, 13));
        pan1.add(tfield01);
        btbr.setBounds(250, 23, 110, 33);
        btbr.setFont(new Font("Verdana", Font.PLAIN, 13));
        pan1.add(btbr);
        label02.setBounds(70, 50, 140, 30);
        label02.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan1.add(label02);
        tfield02.setBounds(205, 55, 97, 20);
        tfield02.setFont(new Font("Verdana", Font.PLAIN, 13));
        pan1.add(tfield02);
        label03.setBounds(70, 80, 140, 30);
        label03.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan1.add(label03);
        tfield03.setBounds(195, 85, 107, 20);
        tfield03.setFont(new Font("Verdana", Font.PLAIN, 13));
        pan1.add(tfield03);
        startex.setBounds(310, 48, 130, 35);
        startex.setFont(new Font("Verdana", Font.PLAIN, 12));
        pan1.add(startex);
        result.setBounds(310, 78, 130, 35);
        result.setEnabled(false);
        result.setFont(new Font("Verdana", Font.PLAIN, 12));
        pan1.add(result);
        bt26.setEnabled(true);
        startex.setEnabled(true);
        bt27.setEnabled(true);
        startex.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                bt26.setEnabled(false);
                bt27.setEnabled(false);
                if (tfield01.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Please Correctly Configure the Server ..", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Integer.parseInt(tfield02.getText());
                    Integer.parseInt(tfield03.getText());
                    QuickExamSt();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please Correctly Configure the Server ..", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btbr.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                tfield01.setText("");
                tfield01.setEnabled(false);
                openFile();
            }
        });
        result.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                QuickMResult vl = new QuickMResult();
                vl.QuickMResult1();
                desktop.add(vl);
                try {
                    vl.setSelected(true);
                } catch (java.beans.PropertyVetoException p) {
                }
                ;
            }
        });
        pan1.setBackground(Color.gray);
        labelfile.setBounds(70, 50, 120, 30);
        labelfile.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan.add(labelfile);
        txafile.setBounds(70, 80, 150, 20);
        txafile.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan.add(txafile);
        bt27.setBounds(260, 73, 100, 35);
        bt27.setFont(new Font("Verdana", Font.PLAIN, 14));
        pan.add(bt27);
        pan.setBorder(lb);
        pan1.setBorder(lb1);
        pan.setVisible(false);
        pan1.setVisible(false);
        bt26.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pan.setVisible(true);
            }
        });
        btquick.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pan1.setVisible(true);
            }
        });
        bt27.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Integer.parseInt(txa02.getText());
                    if ((txafile.getText()).equals("")) JOptionPane.showMessageDialog(null, "Please Enter your filename", "Error reading filename", JOptionPane.ERROR_MESSAGE); else {
                        mcqSetting();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please Enter the valid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        try {
            UIManager.setLookAndFeel(looks[1].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mcqSetting() {
        MCQSetting mcq = new MCQSetting();
        desktop.add(mcq);
        try {
            mcq.addInternalFrameListener(this);
            mcq.setSelected(true);
        } catch (java.beans.PropertyVetoException p) {
        }
        ;
    }

    private void openFile() {
        JFileChooser filech = new JFileChooser();
        String theLine = "";
        filech.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = filech.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        File filename = filech.getSelectedFile();
        if (filename == null || filename.getName().equals("")) JOptionPane.showMessageDialog(this, "Invalid File Name", "Error", JOptionPane.ERROR_MESSAGE); else {
            tfield01.setText(filename.getPath());
        }
    }

    private void QuickExamSt() {
        try {
            startex.setEnabled(false);
            MainServerImpl msi = new MainServerImpl();
            Naming.rebind("MainServer", msi);
            JOptionPane.showMessageDialog(null, "Exam Start Successfully..", "Thanks", JOptionPane.INFORMATION_MESSAGE);
            startex.setEnabled(false);
            System.out.println("Waiting For Clients");
        } catch (Exception ext) {
            JOptionPane.showMessageDialog(null, "Server Internal Error..\n Contact to the Administrator..", "Error", JOptionPane.ERROR_MESSAGE);
        }
        Thd thr1 = new Thd("Thread1");
        thr1.start();
    }

    public void internalFrameClosing(InternalFrameEvent e) {
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }
}
