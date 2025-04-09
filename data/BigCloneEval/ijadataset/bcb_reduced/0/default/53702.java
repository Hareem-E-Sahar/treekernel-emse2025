import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.*;
import javax.swing.JLabel;

class Zima implements Serializable {

    protected String zi, ma;

    Zima() {
        zi = null;
        ma = null;
    }

    Zima(String z, String m) {
        zi = z;
        ma = m;
    }

    public void showZi(JLabel jl) {
        jl.setIcon(null);
        jl.setText(zi);
    }

    public void showWrong(JLabel jl) {
        jl.setText("##" + zi + "##");
    }

    public void showAnswer(JLabel jl) {
        jl.setIcon(null);
        jl.setText(zi + "  " + ma);
    }

    public String getMa() {
        return ma;
    }
}

public class Database {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("参数错误");
            return;
        }
        Vector table = new Vector();
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql:liudb", "liu", null);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT 字, 码 FROM " + args[0]);
            while (rs.next()) {
                table.addElement(new Zima(rs.getString(1), rs.getString(2)));
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(args[1]));
            out.writeObject(table);
            out.close();
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            return;
        } catch (FileNotFoundException f) {
            System.out.println(f.getMessage());
            return;
        } catch (IOException i) {
            System.out.println(i.getMessage());
            return;
        }
    }
}
