package smu.mm;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

@SuppressWarnings("serial")
public class testinfo extends Frame implements ActionListener {

    public static final int NONE = 0;

    public static final int LIST = 1;

    public static final int AVG = 2;

    public static final int INFOS = 3;

    public static final int INFOF = 4;

    public static TextArea display;

    public static TextField stuid;

    public static Button list, avg, infos, infof, cancel;

    public static Connection conn;

    public static Statement stat;

    public static int cmd;

    public static ResultSet rs = null, rs2 = null;

    public testinfo() {
        super("Seoyoung");
        setLayout(new BorderLayout());
        display = new TextArea();
        display.setEditable(false);
        Panel left = new Panel();
        left.setLayout(new GridLayout(5, 1));
        Panel course = new Panel();
        course.add(new Label("Student ID"));
        course.add(stuid = new TextField(7));
        left.add(course);
        Panel bottom = new Panel();
        bottom.add(list = new Button("�л� ����Ʈ"));
        list.addActionListener(this);
        bottom.add(avg = new Button("��� ����"));
        avg.addActionListener(this);
        bottom.add(infos = new Button("Ư�� �л� ����"));
        infos.addActionListener(this);
        bottom.add(infof = new Button("���� ����"));
        infof.addActionListener(this);
        bottom.add(cancel = new Button("Cancle"));
        cancel.addActionListener(this);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                destroy();
                setVisible(false);
                dispose();
                manage seoyoung = new manage();
                seoyoung.setSize(1000, 850);
                seoyoung.setVisible(true);
            }
        });
        add("Center", display);
        add("West", left);
        add("South", bottom);
        cmd = NONE;
        init();
    }

    private void init() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ce) {
            System.out.println(ce);
        }
        try {
            conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Student_record_management_system", "sa", "0328");
            System.out.println("You access DBMS because of management");
            stat = conn.createStatement();
        } catch (SQLException se) {
            System.out.println(se);
        } finally {
        }
        initialize();
    }

    public void initialize() {
        stuid.setEditable(false);
        stuid.setText("1st, Fit for purpose and press the button.");
    }

    private void destroy() {
        try {
            if (stat != null) {
                stat.close();
            }
            if (conn != null) {
                conn.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception ex) {
        }
    }

    public static void setEnable(int n) {
    }

    public void clear() {
        stuid.setText("");
    }

    public void actionPerformed(ActionEvent e) {
        Component c = (Component) e.getSource();
        try {
            if (c == list) {
                if (cmd != LIST) {
                    avg.setEnabled(false);
                    infos.setEnabled(false);
                    infof.setEnabled(false);
                    cmd = LIST;
                    CS_Search css = new CS_Search();
                    css.setSize(500, 100);
                    css.setVisible(true);
                } else {
                    list.setEnabled(true);
                    avg.setEnabled(true);
                    infos.setEnabled(true);
                    infof.setEnabled(true);
                    stuid.setEditable(false);
                    cmd = NONE;
                }
            } else if (c == avg) {
                rs = stat.executeQuery("SELECT AVG(Midterm) FROM Student_management");
                display.setText("========================================================\n");
                display.append("\t\t�߰���� ��� ����\n");
                display.append("========================================================\n");
                while (rs.next()) {
                    int val = rs.getInt(1);
                    display.append("\t\t\t" + val + "\n\n");
                }
                rs = stat.executeQuery("SELECT AVG(Final) FROM Student_management");
                display.append("========================================================\n");
                display.append("\t\t�⸻��� ��� ����\n");
                display.append("========================================================\n");
                while (rs.next()) {
                    int val = rs.getInt(1);
                    display.append("\t\t\t" + val + "\n\n");
                }
                rs = stat.executeQuery("SELECT AVG((Midterm+Final)/2) FROM Student_management");
                display.append("========================================================\n");
                display.append("\t\t�߰��⸻ ��� ����\n");
                display.append("========================================================\n");
                while (rs.next()) {
                    int val = rs.getInt(1);
                    display.append("\t\t\t" + val + "");
                }
                cmd = NONE;
                initialize();
            } else if (c == infos) {
                if (cmd != INFOS) {
                    list.setEnabled(false);
                    avg.setEnabled(false);
                    infof.setEnabled(false);
                    stuid.setEditable(true);
                    cmd = INFOS;
                } else {
                    String id_string = stuid.getText().trim();
                    rs = stat.executeQuery("select * from Student_management where StudentID = '" + id_string + "';");
                    display.setText("===================================================================================================================================" + "\n");
                    display.append("\t�й� \t\t �̸� \t\t ���� \t\t ������ \t\t\t�߰� ���� \t�⸻ ���� \t �� ����\n");
                    display.append("===================================================================================================================================" + "\n");
                    while (rs.next()) {
                        id_string = rs.getString(1);
                        String name = rs.getString(2);
                        String sex = rs.getString(3);
                        String year = rs.getString(4);
                        String mon = rs.getString(5);
                        String day = rs.getString(6);
                        int mi = rs.getInt(7);
                        int fi = rs.getInt(8);
                        int full = (mi + fi) / 2;
                        display.append("\t" + id_string + " \t\t" + name + "\t\t" + sex + "\t\t" + year + "." + mon + "." + day + "" + "\t " + mi + "\t" + fi + "\t" + full + "\n");
                        display.append("-----------------------------------------------------------------------------------------------------------------------------------\n");
                        cmd = NONE;
                    }
                    list.setEnabled(true);
                    avg.setEnabled(true);
                    infos.setEnabled(true);
                    infof.setEnabled(true);
                    stuid.setEditable(false);
                    cmd = NONE;
                }
            } else if (c == infof) {
                rs = stat.executeQuery("select * from Professor_management;");
                display.setText("=======================================================================================" + "\n");
                display.append("�̸� \t\t ���� \t ��� \t\t �� \t\t �� \n");
                display.append("=======================================================================================" + "\n");
                while (rs.next()) {
                    String pf_name = rs.getString(1);
                    String gender = rs.getString(2);
                    String year = rs.getString(3);
                    String month = rs.getString(4);
                    String day = rs.getString(5);
                    display.append(pf_name + "\t\t" + gender + "\t" + year + "\t\t" + month + "\t\t" + day + "\n");
                }
                initialize();
            } else if (c == cancel) {
                list.setEnabled(true);
                avg.setEnabled(true);
                infos.setEnabled(true);
                infof.setEnabled(true);
                stuid.setEditable(false);
                cmd = NONE;
            }
        } catch (Exception ex) {
        }
        return;
    }
}
