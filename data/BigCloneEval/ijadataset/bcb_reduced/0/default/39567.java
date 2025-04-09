import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class CommonService {

    class retData {

        String rowFirstcol;

        Integer rowSecondcol;

        public retData(String rF, int rS) {
            rowFirstcol = rF;
            rowSecondcol = rS;
        }
    }

    Connection con;

    String url;

    String userName;

    String password;

    ResultSet results;

    public Connection initiateCon() {
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            url = "jdbc:mysql://localhost:3306/os";
            connection = DriverManager.getConnection(url, "root", "vkmohan123");
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeCon(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
        }
        connection = null;
    }

    public ResultSet getResultSet(Connection connection, String query1) {
        ResultSet res;
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            res = statement.executeQuery(query1);
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public void sqlCon2(ResultSet rs2) {
        try {
            JFrame frame = new JFrame("List");
            JPanel panel = new JPanel();
            ResultSetMetaData metadata1 = rs2.getMetaData();
            int numcols = metadata1.getColumnCount();
            rs2.last();
            int numrows = rs2.getRow();
            rs2.first();
            String[][] data = new String[numrows][numcols];
            int r = 0;
            int c = 0;
            int o = 0;
            String str;
            while (true) {
                for (c = 0; c < numcols; c++) {
                    o = c + 1;
                    str = rs2.getString(o);
                    data[r][c] = str;
                }
                r++;
                if (!rs2.next()) break;
            }
            String col[] = new String[numcols];
            for (int x = 1; x <= numcols; x++) {
                col[x - 1] = metadata1.getColumnLabel(x);
            }
            JTable table1 = new JTable(data, col) {

                public boolean isCellEditable(int rowIndex, int colIndex) {
                    return false;
                }
            };
            JTableHeader header = table1.getTableHeader();
            header.setBackground(Color.yellow);
            JScrollPane pane = new JScrollPane(table1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            frame.add(pane);
            frame.setBounds(100, 100, 600, 600);
            frame.setVisible(true);
        } catch (Exception e) {
            CommonService x2 = new CommonService();
            x2.errorLog("" + 5, "", "", "SQLCON:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public String[][] Serlist(String q1) {
        try {
            CommonService x1 = new CommonService();
            Connection c1 = x1.initiateCon();
            System.out.println("Query" + q1);
            ResultSet rs1;
            rs1 = x1.getResultSet(c1, q1);
            System.out.println("After Serlist rs1");
            ResultSetMetaData metadata1 = rs1.getMetaData();
            int numcols = metadata1.getColumnCount();
            rs1.last();
            int numrows = rs1.getRow();
            rs1.first();
            String[][] data = new String[numrows][numcols];
            int r = 0;
            int c = 0;
            int o = 0;
            String str;
            while (true) {
                System.out.println("NSQLCON2C:" + numcols + "R:" + numrows);
                for (c = 0; c < numcols; c++) {
                    o = c + 1;
                    str = rs1.getString(o);
                    data[r][c] = str;
                }
                r++;
                if (!rs1.next()) break;
            }
            x1.closeCon(c1);
            return data;
        } catch (Exception e) {
            String data1[][] = new String[1][1];
            data1[0][0] = "-1";
            return data1;
        }
    }

    public String[][] Rptlist(String q1) {
        try {
            CommonService x1 = new CommonService();
            Connection c1 = x1.initiateCon();
            ResultSet rs1;
            rs1 = x1.getResultSet(c1, q1);
            ResultSetMetaData metadata1 = rs1.getMetaData();
            int numcols = metadata1.getColumnCount();
            rs1.last();
            int numrows = rs1.getRow();
            rs1.first();
            String[][] data = new String[numrows + 1][numcols];
            int r = 0;
            int c = 0;
            int o = 0;
            String str;
            for (int x = 1; x <= numcols; x++) {
                data[r][x - 1] = metadata1.getColumnLabel(x);
            }
            r++;
            while (true) {
                for (c = 0; c < numcols; c++) {
                    o = c + 1;
                    str = rs1.getString(o);
                    data[r][c] = str;
                }
                r++;
                if (!rs1.next()) break;
            }
            x1.closeCon(c1);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int sequence(String type) {
        try {
            CommonService x1 = new CommonService();
            Connection c1 = x1.initiateCon();
            ResultSet rs1;
            String q1, sql;
            q1 = "Select requestId from os.Sequences";
            sql = "UPDATE os.Sequences SET requestId=requestId+1";
            rs1 = x1.getResultSet(c1, q1);
            rs1.first();
            int seqId = rs1.getInt(1);
            PreparedStatement prest = c1.prepareStatement(sql);
            int count = prest.executeUpdate();
            x1.closeCon(c1);
            return seqId + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void backup(CommonService ser, String tableName) {
        String q1 = "select * from " + tableName;
        String data[][];
        data = ser.Rptlist(q1);
        Calendar cal = new GregorianCalendar();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String fN = tableName + "_" + day + "_" + (month + 1) + "_" + year + ".txt";
        ser.writeFile(data, fN);
    }

    public void writeFile(String[][] data, String fN) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fN));
            String writeString = null;
            int i, j;
            for (i = 0; i < data.length; i++) {
                for (j = 0; j < (data[0].length - 1); j++) {
                    writeString = data[i][j] + "|";
                    out.write(writeString);
                }
                writeString = data[i][j] + " ";
                out.write(writeString);
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            CommonService x2 = new CommonService();
            x2.errorLog("" + 5, "", "", "WRITEFILE:" + e.getMessage());
        }
    }

    public void report(CommonService ser, String rptName, String q1) {
        String data[][];
        data = ser.Rptlist(q1);
        Calendar cal = new GregorianCalendar();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String fN = rptName + "_" + day + "_" + (month + 1) + "_" + year + ".txt";
        ser.writeFile(data, fN);
    }

    public void emptyReport(String rptName) {
        String data[][] = { { "Empty Report" } };
        Calendar cal = new GregorianCalendar();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String fN = rptName + "_" + day + "_" + (month + 1) + "_" + year + ".txt";
        writeFile(data, fN);
    }

    public boolean fieldLength(String str, Integer strlen) {
        if (str.length() > strlen) return false; else return true;
    }

    public void errorLog(String entity, String entityId, String createdBy, String mes) {
        try {
            CommonService x1 = new CommonService();
            Connection c1 = x1.initiateCon();
            String q1 = "insert into os.ErrorLog values(now(),?,?,?,?)";
            PreparedStatement prest = c1.prepareStatement(q1);
            prest.setString(1, entity);
            prest.setString(2, entityId);
            prest.setString(3, createdBy);
            prest.setString(4, mes);
            int count = prest.executeUpdate();
            x1.closeCon(c1);
        } catch (Exception e) {
            CommonService x2 = new CommonService();
            x2.errorLog("" + 5, "", "", "ERRORLOG:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
