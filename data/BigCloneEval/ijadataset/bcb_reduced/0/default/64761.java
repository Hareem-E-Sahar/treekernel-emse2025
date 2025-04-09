import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

public class act_TablaRehm {

    public static void main(String args[]) throws SQLException {
        Connection con = null;
        String url = "jdbc:mysql://192.168.118.16:3306/";
        String db = "rehm";
        String driver = "org.gjt.mm.mysql.Driver";
        String user = "gabriel";
        String pass = "";
        try {
            Class.forName(driver).newInstance();
            con = (Connection) DriverManager.getConnection(url + db, user, pass);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        ResultSet resultcie10 = null;
        ResultSet resultcie9 = null;
        try {
            resultcie10 = (ResultSet) con.createStatement().executeQuery("SELECT * from Cie10Bean");
            resultcie9 = (ResultSet) con.createStatement().executeQuery("SELECT * from Cie9Bean");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        while (resultcie10.next()) {
            String codigocie = resultcie10.getString("cod");
            String descrip = resultcie10.getString("descrip");
            updateTabla(codigocie, descrip, con);
        }
        while (resultcie9.next()) {
            String codigocie = resultcie9.getString("cod");
            String descrip = resultcie9.getString("descrip");
            updateTabla(codigocie, descrip, con);
        }
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTabla(String codigocie, String descrip, Connection con) {
        try {
            String fila = ("UPDATE ItemTable SET desc_codigo='" + descrip + "' where codigo='" + codigocie + "'");
            java.sql.Statement s = con.createStatement();
            int r = s.executeUpdate(fila);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
