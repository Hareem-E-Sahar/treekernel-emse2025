import java.io.*;
import java.sql.*;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.http.*;

public class AddCategory extends HttpServlet {

    private String url, sql;

    private Connection conn;

    private PreparedStatement ps;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.print("<html><body>");
        out.print("<html><body>");
        out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />");
        out.print("<div id=\"outer\">" + " <div id=\"wrapper\">" + " <div id=\"nav\">" + " <div id=\"nav-left\">" + " <div id=\"nav-right\">");
        out.print("<ul>" + "<li><a href=\"http://localhost:8080/ForFinals/AddItem\">Add New Item</a</li>" + "<li><a href=\"http://localhost:8080/ForFinals/AddCategory\">Add New Category</a></li>" + "<li><a href=\"http://localhost:8080/ForFinals/SearchItem\">Search</a></li>" + " </ul>");
        out.print("</div>");
        out.print("</div>");
        out.print("<div class=\"clear\"></div>");
        out.print("</div>");
        out.print("<div id=\"head-2\"></div>");
        out.print("<form action=\"");
        out.print(req.getRequestURI());
        out.print("\" method=\"post\">");
        out.print("Category Code :");
        out.print("<input type=\"text\" name=\"categoryCode\"><br>");
        out.print("Category Name :");
        out.print("<input type=\"text\" name=\"categoryName\"><br>");
        out.print("Sub Category Code :");
        out.print("<input type=\"text\" name=\"subCategoryCode\"><br>");
        out.print("<br><br><input type=\"submit\" value=\"Add Category\">");
        out.print("<input type=\"reset\" value=\"Clear\">");
        out.print("</body></html>");
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.print("<html><body>");
        String cCode = req.getParameter("categoryCode").trim();
        String cName = req.getParameter("categoryName").trim();
        String subCatCode = req.getParameter("subCategoryCode").trim();
        boolean proceed = false;
        if (cCode != null && cName != null && subCatCode != null) if (cCode.length() > 0 && cName.length() > 0 && subCatCode != null) proceed = true;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            url = "jdbc:mysql://localhost/inventory";
            conn = DriverManager.getConnection(url, "root", "");
            sql = "INSERT INTO category(category_code, category_name, subcategory_code) VALUES(?, ?, ?)";
            ps = conn.prepareStatement(sql);
            if (proceed) {
                ps.setString(1, cCode);
                ps.setString(2, cName);
                ps.setString(3, subCatCode);
                ps.executeUpdate();
            }
            out.print("You have added a new category named ");
            out.print(cName);
        } catch (ClassNotFoundException cnfe) {
            out.println("" + cnfe);
        } catch (SQLException sqle) {
            out.println("" + sqle);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                out.println("" + sqle);
            }
        }
        out.print("</body></html>");
        out.close();
    }
}
