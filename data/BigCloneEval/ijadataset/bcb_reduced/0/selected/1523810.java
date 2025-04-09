package smu.mm;

import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class CS_Search extends Frame implements ActionListener {

    Button course, prof, sem, year;

    public CS_Search() {
        super("CS_Search");
        setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        Panel center = new Panel();
        Panel pcourse = new Panel();
        pcourse.add(course = new Button("StudentID ��"));
        course.addActionListener(this);
        Panel pro = new Panel();
        pro.add(prof = new Button("�߰� ���� ��"));
        prof.addActionListener(this);
        Panel pse = new Panel();
        pse.add(sem = new Button("�⸻ ���� ��"));
        sem.addActionListener(this);
        Panel pyear = new Panel();
        pyear.add(year = new Button("�� ���� ��"));
        year.addActionListener(this);
        center.add(pcourse);
        center.add(pro);
        center.add(pse);
        center.add(pyear);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                setVisible(false);
                dispose();
            }
        });
        add("Center", center);
    }

    public void actionPerformed(ActionEvent e) {
        Button c = (Button) e.getSource();
        try {
            if (c == course) {
                testinfo.rs = testinfo.stat.executeQuery("select * from Student_management order by StudentID");
                testinfo.display.setText("===================================================================================================================================" + "\n");
                testinfo.display.append("\t�й� \t\t �̸� \t\t ���� \t\t ������ \t\t\t�߰� ���� \t�⸻ ���� \t �� ����\n");
                testinfo.display.append("===================================================================================================================================" + "\n");
                while (testinfo.rs.next()) {
                    String id_string = testinfo.rs.getString(1);
                    String name = testinfo.rs.getString(2);
                    String sex = testinfo.rs.getString(3);
                    String year = testinfo.rs.getString(4);
                    String mon = testinfo.rs.getString(5);
                    String day = testinfo.rs.getString(6);
                    int mi = testinfo.rs.getInt(7);
                    int fi = testinfo.rs.getInt(8);
                    int full = (mi + fi) / 2;
                    testinfo.display.append("\t" + id_string + " \t\t" + name + "\t\t" + sex + "\t\t" + year + "." + mon + "." + day + "" + "\t " + mi + "\t" + fi + "\t" + full + "\n");
                    testinfo.display.append("-----------------------------------------------------------------------------------------------------------------------------------\n");
                    testinfo.cmd = testinfo.NONE;
                }
                this.dispose();
            } else if (c == prof) {
                testinfo.rs = testinfo.stat.executeQuery("select * from Student_management order by Midterm");
                testinfo.display.setText("===================================================================================================================================" + "\n");
                testinfo.display.append("\t�й� \t\t �̸� \t\t ���� \t\t ������ \t\t\t�߰� ���� \t�⸻ ���� \t �� ����\n");
                testinfo.display.append("===================================================================================================================================" + "\n");
                while (testinfo.rs.next()) {
                    String id_string = testinfo.rs.getString(1);
                    String name = testinfo.rs.getString(2);
                    String sex = testinfo.rs.getString(3);
                    String year = testinfo.rs.getString(4);
                    String mon = testinfo.rs.getString(5);
                    String day = testinfo.rs.getString(6);
                    int mi = testinfo.rs.getInt(7);
                    int fi = testinfo.rs.getInt(8);
                    int full = (mi + fi) / 2;
                    testinfo.display.append("\t" + id_string + " \t\t" + name + "\t\t" + sex + "\t\t" + year + "." + mon + "." + day + "" + "\t " + mi + "\t" + fi + "\t" + full + "\n");
                    testinfo.display.append("-----------------------------------------------------------------------------------------------------------------------------------\n");
                    testinfo.cmd = testinfo.NONE;
                }
                this.dispose();
            } else if (c == sem) {
                testinfo.rs = testinfo.stat.executeQuery("select * from Student_management order by Final");
                testinfo.display.setText("===================================================================================================================================" + "\n");
                testinfo.display.append("\t�й� \t\t �̸� \t\t ���� \t\t ������ \t\t\t�߰� ���� \t�⸻ ���� \t �� ����\n");
                testinfo.display.append("===================================================================================================================================" + "\n");
                while (testinfo.rs.next()) {
                    String id_string = testinfo.rs.getString(1);
                    String name = testinfo.rs.getString(2);
                    String sex = testinfo.rs.getString(3);
                    String year = testinfo.rs.getString(4);
                    String mon = testinfo.rs.getString(5);
                    String day = testinfo.rs.getString(6);
                    int mi = testinfo.rs.getInt(7);
                    int fi = testinfo.rs.getInt(8);
                    int full = (mi + fi) / 2;
                    testinfo.display.append("\t" + id_string + " \t\t" + name + "\t\t" + sex + "\t\t" + year + "." + mon + "." + day + "" + "\t " + mi + "\t" + fi + "\t" + full + "\n");
                    testinfo.display.append("-----------------------------------------------------------------------------------------------------------------------------------\n");
                    testinfo.cmd = testinfo.NONE;
                }
                this.dispose();
            } else if (c == year) {
                testinfo.rs = testinfo.stat.executeQuery("select * from Student_management order by (Midterm+Final)/2");
                testinfo.display.setText("===================================================================================================================================" + "\n");
                testinfo.display.append("\t�й� \t\t �̸� \t\t ���� \t\t ������ \t\t\t�߰� ���� \t�⸻ ���� \t �� ����\n");
                testinfo.display.append("===================================================================================================================================" + "\n");
                while (testinfo.rs.next()) {
                    String id_string = testinfo.rs.getString(1);
                    String name = testinfo.rs.getString(2);
                    String sex = testinfo.rs.getString(3);
                    String year = testinfo.rs.getString(4);
                    String mon = testinfo.rs.getString(5);
                    String day = testinfo.rs.getString(6);
                    int mi = testinfo.rs.getInt(7);
                    int fi = testinfo.rs.getInt(8);
                    int full = (mi + fi) / 2;
                    testinfo.display.append("\t" + id_string + " \t\t" + name + "\t\t" + sex + "\t\t" + year + "." + mon + "." + day + "" + "\t " + mi + "\t" + fi + "\t" + full + "\n");
                    testinfo.display.append("-----------------------------------------------------------------------------------------------------------------------------------\n");
                    testinfo.cmd = testinfo.NONE;
                }
                this.dispose();
            }
            testinfo.list.setEnabled(true);
            testinfo.avg.setEnabled(true);
            testinfo.infos.setEnabled(true);
            testinfo.infof.setEnabled(true);
            testinfo.stuid.setEditable(false);
        } catch (Exception ex) {
        }
        return;
    }
}
