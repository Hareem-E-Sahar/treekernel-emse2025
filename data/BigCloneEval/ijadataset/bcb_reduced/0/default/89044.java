import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.swing.JOptionPane;

public class Jeff extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Connection con;
        String dbsource = "jdbc:odbc:jdbctest";
        String drv = "sun.jdbc.odbc.JdbcOdbcDriver";
        String duser = "root";
        String dpass = "";
        String defaultquery = "select * from student";
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<html><head><title>Student File</title></head><body bgcolor=\"turquoise\"><h3>Student File</h3>");
        try {
            con = DriverManager.getConnection(dbsource, duser, dpass);
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(defaultquery);
            out.println("<form method=\"POST\"><input type=\"submit\" value=\"Add Record\" name=\"add\"></form><center><hr>");
            out.println("<table border=\"1\">");
            out.println("<th>Name</th>");
            out.println("<th>Address</th>");
            out.println("<th>Phone</th>");
            out.println("<th></th>");
            while (rs.next()) {
                String val1 = rs.getString(1);
                String val2 = rs.getString(2);
                String val3 = rs.getString(3);
                out.println("<tr>");
                out.println("<td>" + val1 + "</td>");
                out.println("<td>" + val2 + "</td>");
                out.println("<td>" + val3 + "</td>");
                out.println("<td><form method=\"POST\"><input type=\"hidden\" name=\"name\" value=\"" + val1 + "\"><input type=\"hidden\" name=\"address\" value=\"" + val2 + "\"><input type=\"hidden\" name=\"phone\" value=\"" + val3 + "\"><input type=\"submit\" value=\"Update\" name=\"update\"><input type=\"submit\" value=\"Delete\" name=\"delete\"></form></td>");
                out.println("</tr>");
            }
            out.println("</table>");
            con.close();
        } catch (SQLException ex) {
            System.out.println("SQL EXCEPTION:" + ex.getMessage());
        }
        out.println("</center></body></html>");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<html><head><title>Student File</title></head><body bgcolor=\"turquoise\">");
        int err = 0;
        if (req.getParameter("add") != null) {
            out.println("<center><h1>Student</h1></center><hr>");
            out.println("<b>Add Record</b><br><br><form method=\"POST\">");
            out.println("Name: <input type=\"text\" name=\"name\">");
            out.println("Address: <input type=\"text\" name=\"address\">");
            out.println("Phone: <input type=\"text\" name=\"phone\">");
            out.println("<br><br><input type=\"submit\" name=\"addrecord\" value=\"Addrecord\">");
            out.println("<input type=\"submit\" name=\"cancel\" value=\"Cancel\">");
            out.println("</form>");
        } else if (req.getParameter("cancel") != null) {
            res.sendRedirect("http://localhost:8080/Jeff");
        } else if (req.getParameter("addrecord") != null) {
            Connection con;
            String dbsource = "jdbc:odbc:jdbctest";
            String drv = "sun.jdbc.odbc.JdbcOdbcDriver";
            String duser = "root";
            String dpass = "";
            String defaultquery = "insert into student values('" + req.getParameter("name") + "','" + req.getParameter("address") + "','" + req.getParameter("phone") + "')";
            try {
                con = DriverManager.getConnection(dbsource, duser, dpass);
                Statement stm = con.createStatement();
                int a = stm.executeUpdate(defaultquery);
                con.close();
            } catch (SQLException ex) {
                System.out.println("SQL EXCEPTION:" + ex.getMessage());
                out.println("ERROR");
                err++;
            } finally {
                if (err == 0) {
                    JOptionPane.showMessageDialog(null, "RECORD ADDED");
                    res.sendRedirect("http://localhost:8080/Exercise2/Student");
                }
            }
        } else if (req.getParameter("update") != null) {
            out.println("<center><h1>Student</h1></center><hr>");
            out.println("<b>Update Record</b><br><br><form method=\"POST\">");
            out.println("Name: <input type=\"text\" name=\"name\" value=\"" + req.getParameter("name") + "\">");
            out.println("<input type=\"hidden\" name=\"pname\" value=\"" + req.getParameter("name") + "\">");
            out.println("Address: <input type=\"text\" name=\"address\" value=\"" + req.getParameter("address") + "\">");
            out.println("<input type=\"hidden\" name=\"paddress\" value=\"" + req.getParameter("address") + "\">");
            out.println("Phone: <input type=\"text\" name=\"phone\"value=\"" + req.getParameter("phone") + "\">");
            out.println("<input type=\"hidden\" name=\"pphone\"value=\"" + req.getParameter("phone") + "\">");
            out.println("<br><br><input type=\"submit\" name=\"uprecord\" value=\"Save Changes\">");
            out.println("<input type=\"submit\" name=\"cancel\" value=\"Cancel\">");
            out.println("</form>");
        } else if (req.getParameter("uprecord") != null) {
            Connection con;
            String dbsource = "jdbc:odbc:jdbctest";
            String drv = "sun.jdbc.odbc.JdbcOdbcDriver";
            String duser = "root";
            String dpass = "";
            String defaultquery = "update student set name='" + req.getParameter("name") + "', address='" + req.getParameter("address") + "', phone='" + req.getParameter("phone") + "'" + " where name='" + req.getParameter("pname") + "'and address='" + req.getParameter("paddress") + "'and phone='" + req.getParameter("pphone") + "'";
            try {
                con = DriverManager.getConnection(dbsource, duser, dpass);
                Statement stm = con.createStatement();
                int a = stm.executeUpdate(defaultquery);
                con.close();
            } catch (SQLException ex) {
                System.out.println("SQL EXCEPTION:" + ex.getMessage());
                out.println("ERROR");
                err++;
            } finally {
                if (err == 0) {
                    JOptionPane.showMessageDialog(null, "RECORD CHANGED");
                    res.sendRedirect("http://localhost:8080/Exercise2/Student");
                }
            }
        } else if (req.getParameter("delete") != null) {
            int del = JOptionPane.showConfirmDialog(null, "Do you want to delete this record?");
            if (del == 0) {
                Connection con;
                String dbsource = "jdbc:odbc:jdbctest";
                String drv = "sun.jdbc.odbc.JdbcOdbcDriver";
                String duser = "root";
                String dpass = "";
                String defaultquery = "delete from student  where name='" + req.getParameter("name") + "'and address='" + req.getParameter("address") + "'and phone='" + req.getParameter("phone") + "'";
                try {
                    con = DriverManager.getConnection(dbsource, duser, dpass);
                    Statement stm = con.createStatement();
                    int a = stm.executeUpdate(defaultquery);
                    con.close();
                } catch (SQLException ex) {
                    System.out.println("SQL EXCEPTION:" + ex.getMessage());
                    out.println("ERROR");
                    err++;
                } finally {
                    if (err == 0) {
                        JOptionPane.showMessageDialog(null, "RECORD DELETED");
                        res.sendRedirect("http://localhost:8080/Exercise2/Student");
                    }
                }
            } else {
                res.sendRedirect("http://localhost:8080/Exercise2/Student");
            }
        }
        out.println("</body></html>");
    }
}
