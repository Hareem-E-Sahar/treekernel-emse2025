import java.io.*;
import java.sql.*;

public class sqlBean {

    public Connection conn = null;

    public ResultSet rs = null;

    private String DatabaseDriver = "sun.jdbc.odbc.JdbcOdbcDriver";

    private String DatabaseConnStr = "jdbc:odbc:ClassDB2,'limq',123";

    public void setDatabaseDriver(String Driver) {
        this.DatabaseDriver = Driver;
    }

    public String getDatabaseDriver() {
        return (this.DatabaseDriver);
    }

    public void setDatabaseConnStr(String ConnStr) {
        this.DatabaseConnStr = ConnStr;
    }

    public String getDatabaseConnStr() {
        return (this.DatabaseConnStr);
    }

    public sqlBean() {
        try {
            Class.forName(DatabaseDriver);
        } catch (java.lang.ClassNotFoundException e) {
            System.err.println("���������д���:" + e.getMessage());
            System.out.print("ִ�в����д���:" + e.getMessage());
        }
    }

    public int executeInsert(String sql) {
        int num = 0;
        try {
            conn = DriverManager.getConnection("jdbc:odbc:ClassDB", "limq", "123");
            Statement stmt = conn.createStatement();
            num = stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            System.err.println("ִ�в����д���:" + ex.getMessage());
            System.out.print("ִ�в����д���:" + ex.getMessage());
        }
        CloseDataBase();
        return num;
    }

    public ResultSet executeQuery(String sql) {
        rs = null;
        try {
            conn = DriverManager.getConnection("jdbc:odbc:ClassDB", "limq", "123");
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException ex) {
            System.err.println("ִ�в�ѯ�д���:" + ex.getMessage());
            System.out.print("ִ�в�ѯ�д���:" + ex.getMessage());
        }
        return rs;
    }

    public int executeDelete(String sql) {
        int num = 0;
        try {
            conn = DriverManager.getConnection("jdbc:odbc:ClassDB", "limq", "123");
            Statement stmt = conn.createStatement();
            num = stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            System.err.println("ִ��ɾ���д���:" + ex.getMessage());
            System.out.print("ִ��ɾ���д���:" + ex.getMessage());
        }
        CloseDataBase();
        return num;
    }

    public void CloseDataBase() {
        try {
            conn.close();
        } catch (Exception end) {
            System.err.println("ִ�йر�Connection�����д���" + end.getMessage());
            System.out.print("ִ��ִ�йر�Connection�����д����д���:" + end.getMessage());
        }
    }
}
