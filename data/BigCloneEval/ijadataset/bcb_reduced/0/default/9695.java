import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

class CConnectionWindow extends JInternalFrame implements ActionListener {

    public CConnectionWindow(JDesktopPane desk, JApplet app, JMenuItem conn, CConfigFile configFile) {
        super("Tobedone - Login");
        desktop = desk;
        applet = app;
        connect = conn;
        setSize(300, 200);
        username = new JTextField();
        password = new JPasswordField();
        stddb = configFile.conf_configdb;
        getContentPane().setLayout(new BorderLayout());
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        main.add(new JLabel(createImage("connect.png")), BorderLayout.NORTH);
        JPanel text = new JPanel();
        text.setLayout(new GridLayout(3, 2));
        host = new JTextField(configFile.conf_host);
        text.add(new JLabel("Host"));
        text.add(host);
        text.add(new JLabel("Username"));
        text.add(username);
        text.add(new JLabel("Password"));
        text.add(password);
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 2));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel");
        JButton ok = new JButton("OK");
        ok.addActionListener(this);
        ok.setActionCommand("ok");
        getRootPane().setDefaultButton(ok);
        buttons.add(cancel);
        buttons.add(ok);
        getContentPane().add(main, BorderLayout.NORTH);
        getContentPane().add(text, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        text.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        setVisible(true);
    }

    private ImageIcon createImage(String filename) {
        String path = "/data/" + filename;
        return new ImageIcon(getClass().getResource(path));
    }

    public void actionPerformed(ActionEvent E) {
        String com = E.getActionCommand();
        if (com.equals("ok")) {
            if (login()) {
                new CSelWindow(C, desktop, connect, username.getText(), stddb);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login was not successful!");
            }
        } else if (com.equals("cancel")) {
            dispose();
            connect.setEnabled(true);
        }
    }

    private boolean login() {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            String url = "jdbc:mysql://" + host.getText() + "/" + stddb;
            C = DriverManager.getConnection(url, username.getText(), new String(password.getPassword()));
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Exception :\n" + e.getMessage());
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Couldn't load " + "JDBC Driver :\n" + e);
        }
        return false;
    }

    private Connection C;

    private JDesktopPane desktop;

    private JMenuItem connect;

    private JApplet applet;

    private JTextField username;

    private JPasswordField password;

    private JTextField host;

    private String stddb;
}
