import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Summary description for BindConfig
 *
 */
public class BindConfig extends JFrame {

    private JLabel jLabel1;

    private JLabel jLabel2;

    private JLabel jLabel3;

    private JLabel jLabel4;

    private JTextField jTextField1;

    private JTextField jTextField2;

    private JTextField jTextField3;

    private JTextField jTextField4;

    private JButton OK;

    private JButton back;

    private JPanel contentPane;

    public BindConfig() {
        super();
        initializeComponent();
        this.setVisible(true);
    }

    /**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always regenerated
	 * by the Windows Form Designer. Otherwise, retrieving design might not work properly.
	 * Tip: If you must revise this method, please backup this GUI file for JFrameBuilder
	 * to retrieve your design properly in future, before revising this method.
	 */
    private void initializeComponent() {
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jTextField4 = new JTextField();
        jTextField1 = new JTextField();
        jTextField2 = new JTextField();
        jTextField3 = new JTextField();
        OK = new JButton();
        back = new JButton();
        contentPane = (JPanel) this.getContentPane();
        jLabel1.setText("Enter the IP Address :");
        jLabel2.setText("Enter the Active Server IP :");
        jLabel3.setText("Enter Domain Name :");
        jLabel4.setText("Enter the Secondary server IP :");
        jTextField1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jTextField1_actionPerformed(e);
            }
        });
        jTextField4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jTextField4_actionPerformed(e);
            }
        });
        jTextField3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jTextField3_actionPerformed(e);
            }
        });
        jTextField2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jTextField2_actionPerformed(e);
            }
        });
        OK.setText("OK");
        OK.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                OK_actionPerformed(e);
            }
        });
        back.setText("Back");
        back.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                back_actionPerformed(e);
            }
        });
        contentPane.setLayout(null);
        addComponent(contentPane, jLabel1, 30, 30, 160, 18);
        addComponent(contentPane, jLabel2, 30, 71, 180, 18);
        addComponent(contentPane, jLabel3, 30, 170, 180, 18);
        addComponent(contentPane, jLabel4, 30, 123, 200, 18);
        addComponent(contentPane, jTextField2, 220, 70, 100, 22);
        addComponent(contentPane, jTextField4, 220, 120, 100, 22);
        addComponent(contentPane, jTextField3, 220, 170, 100, 22);
        addComponent(contentPane, jTextField1, 220, 30, 100, 22);
        addComponent(contentPane, OK, 255, 204, 83, 28);
        addComponent(contentPane, back, 56, 206, 83, 28);
        this.setTitle("Terminal Server Configuration");
        this.setLocation(new Point(152, 101));
        this.setSize(new Dimension(390, 300));
    }

    /** Add Component Without a Layout Manager (Absolute Positioning) */
    private void addComponent(Container container, Component c, int x, int y, int width, int height) {
        c.setBounds(x, y, width, height);
        container.add(c);
    }

    private void jTextField1_actionPerformed(ActionEvent e) {
        System.out.println("\njTextField1_actionPerformed(ActionEvent e) called.");
    }

    private void jTextField3_actionPerformed(ActionEvent e) {
        System.out.println("\njTextField1_actionPerformed(ActionEvent e) called.");
    }

    private void jTextField4_actionPerformed(ActionEvent e) {
        System.out.println("\njTextField4_actionPerformed(ActionEvent e) called.");
    }

    private void jTextField2_actionPerformed(ActionEvent e) {
        System.out.println("\njTextField2_actionPerformed(ActionEvent e) called.");
    }

    private void OK_actionPerformed(ActionEvent e) {
        System.out.println("\nOK_actionPerformed(ActionEvent e) called.");
        String[] runstring = new String[2];
        try {
            FileWriter sh_ping = new FileWriter("ping.txt", false);
            BufferedWriter out_ping = new BufferedWriter(sh_ping);
            String primary_serv = jTextField2.getText();
            String secondary_serv = jTextField4.getText();
            primary_serv = primary_serv.replace(".", ",");
            secondary_serv = secondary_serv.replace(".", ",");
            out_ping.write(primary_serv + "\n");
            out_ping.write(secondary_serv + "\n");
            out_ping.close();
            System.out.println("ping.txt COMPLETE");
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("mkdir /etc/bind/zones");
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            int exitVal = pr.waitFor();
            System.out.println("Exited with error code " + exitVal);
            FileWriter sh = new FileWriter("/etc/bind/named.conf.local", true);
            BufferedWriter out = new BufferedWriter(sh);
            String zone = "zone " + "\"" + jTextField3.getText() + "\" {";
            String file1 = "file " + "\"/etc/bind/zones/" + jTextField3.getText() + ".db\";";
            out.write(zone + "\n");
            out.write("type master;" + "\n");
            out.write(file1 + "\n");
            out.write("};" + "\n");
            out.close();
            System.out.println("named.conf.local COMPLETE");
            FileWriter sh1 = new FileWriter("/etc/resolv.conf", true);
            BufferedWriter out1 = new BufferedWriter(sh1);
            String nameserver = "nameserver " + jTextField1.getText();
            out1.write(nameserver + "\n");
            out1.close();
            System.out.println("resolv.conf COMPLETE");
            String filename = "/etc/bind/zones/" + jTextField3.getText() + ".db";
            FileWriter sh2 = new FileWriter(filename, false);
            FileWriter sh3 = new FileWriter("/etc/bind/zones/hello.txt", false);
            BufferedWriter out2 = new BufferedWriter(sh2);
            BufferedWriter out3 = new BufferedWriter(sh3);
            String origin = "$ORIGIN " + jTextField3.getText() + ".";
            String first = "@	IN	SOA ns." + jTextField3.getText() + ".   root." + jTextField3.getText() + ". (";
            String second = "@	IN	NS  ns." + jTextField3.getText() + ".";
            String third = "@	IN	A   " + jTextField1.getText();
            String fourth = "www     IN      A     " + jTextField2.getText();
            out2.write(";" + "\n");
            out2.write("; BIND data file for local loopback interface" + "\n");
            out2.write(";" + "\n");
            out2.write("$TTL	604800" + "\n");
            out2.write(origin + "\n");
            out2.write(first + "\n");
            out2.write("2		; Serial" + "\n");
            out2.write("604800		; Refresh" + "\n");
            out2.write("86400		; Retry" + "\n");
            out2.write("2419200		; Expire" + "\n");
            out2.write("604800 )	; Negative Cache TTL" + "\n");
            out2.write(";" + "\n");
            out2.write(second + "\n");
            out2.write(third + "\n");
            out2.write(fourth + "\n");
            out3.write(";" + "\n");
            out3.write("; BIND data file for local loopback interface" + "\n");
            out3.write(";" + "\n");
            out3.write("$TTL	604800" + "\n");
            out3.write(origin + "\n");
            out3.write(first + "\n");
            out3.write("2		; Serial" + "\n");
            out3.write("604800		; Refresh" + "\n");
            out3.write("86400		; Retry" + "\n");
            out3.write("2419200		; Expire" + "\n");
            out3.write("604800 )	; Negative Cache TTL" + "\n");
            out3.write(";" + "\n");
            out3.write(second + "\n");
            out3.write(third + "\n");
            out2.close();
            out3.close();
            System.out.println("hello.txt & db file COMPLETE");
            Runtime rt1 = Runtime.getRuntime();
            Process pr1 = rt1.exec("/etc/init.d/bind9 restart");
            input = new BufferedReader(new InputStreamReader(pr1.getInputStream()));
            line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            exitVal = pr1.waitFor();
            System.out.println("Exited with error code " + exitVal);
            ViewPrimary next_step = new ViewPrimary();
            next_step.show();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }

    private void back_actionPerformed(ActionEvent e) {
        System.out.println("\nback_actionPerformed(ActionEvent e) called.");
        this.dispose();
        Terminal server = new Terminal();
        server.show();
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            System.out.println("Failed loading L&F: ");
            System.out.println(ex);
        }
        new BindConfig();
    }
}
