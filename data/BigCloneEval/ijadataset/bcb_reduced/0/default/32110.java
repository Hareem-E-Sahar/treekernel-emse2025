import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.*;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

class Genma extends Zima {

    protected ImageIcon gen;

    Genma(ImageIcon g, String m) {
        super();
        gen = g;
        ma = m;
    }

    public void showZi(JLabel jl) {
        jl.setText(null);
        jl.setIcon(gen);
    }

    public void showWrong(JLabel jl) {
        jl.setIcon(gen);
        jl.setText("##");
    }

    public void showAnswer(JLabel jl) {
        jl.setText(null);
        jl.setIcon(gen);
        jl.setText(ma);
    }
}

public class Collectzg {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("参数错误");
            return;
        }
        Vector table = new Vector();
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql:liudb", "liu", null);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT 目录, 字文件, 码 FROM " + args[0]);
            String ml = "";
            while (rs.next()) {
                ml = rs.getString(1);
                if (ml == null) {
                    table.addElement(new Zima(rs.getString(2), rs.getString(3)));
                } else {
                    table.addElement(new Genma((new ImageIcon(ml + "/" + rs.getString(2))), rs.getString(3)));
                }
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
