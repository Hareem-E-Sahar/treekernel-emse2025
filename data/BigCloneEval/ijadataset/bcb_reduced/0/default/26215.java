import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class InventoryList extends HttpServlet {

    Connection con;

    String dbsource = "jdbc:mysql://localhost/inventory";

    String drv = "com.mysql.jdbc.Driver";

    String defaultquery = "select * from item";

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Inventory(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (req.getParameter("delete") != null) {
            deletePage(req, res);
        }
        if (req.getParameter("deleteval") != null) {
            try {
                PrintWriter o = res.getWriter();
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://localhost/inventory";
                con = DriverManager.getConnection(url, "root", "");
                String delename = req.getParameter("delname");
                PreparedStatement st = con.prepareStatement("delete from student where name='" + delename + "'");
                int pe = st.executeUpdate();
                res.setContentType("text/html");
                res.setHeader("pragma", "no-cache");
                o.print("<HTML><HEAD><TITLE>Exercise2.1</TITLE></HEAD><BODY>" + "<h2>Deleted record!!</h2>");
            } catch (Exception e) {
                PrintWriter o = res.getWriter();
                o.println("Error statement");
                e.printStackTrace();
            }
        }
    }

    protected void Inventory(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        String s = null;
        try {
            Class.forName(drv);
            Connection con = DriverManager.getConnection(dbsource, "root", "");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(defaultquery);
            out.println("<html><head><title>JDBC Testing using SERVLET</title></head>" + "<body background bgcolor=gray>");
            while (rs.next()) {
                s = rs.getString(1);
                out.println("Item Code: " + s + "<br/>");
                out.println("Item Name: " + rs.getString(2) + "<br/>");
                out.println("Item Detail #: " + rs.getString(3) + "<br/>");
                out.println("Category Code: " + rs.getString(4) + "<br/>");
                out.println("Quantity: " + rs.getString(5) + "<br/>");
                out.println("<a href=\"http://localhost:8080/finals/UpdateItem?itemcnt=" + s + "\">update</a>");
                out.println("<br/>");
            }
            out.print("<form method=POST><INPUT TYPE=SUBMIT NAME=\"delete\" VALUE=\"delete\"> </form>" + "</body></html>");
        } catch (Exception e) {
            out.println("<html><body background color=red>" + e + "</body></html>");
        }
    }

    private void deletePage(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        res.setHeader("pragma", "no-cache");
        PrintWriter o = res.getWriter();
        o.println("<HTML><HEAD><TITLE>Exercise2.1</TITLE></HEAD><BODY>" + "<FORM METHOD=POST>" + "<b>Name</b> <INPUT TYPE=TEXT NAME=\"delname\">" + "<INPUT TYPE=SUBMIT NAME=\"deleteval\" VALUE=\"deleteval\">" + "</FORM></BODY></HTML>");
    }
}
