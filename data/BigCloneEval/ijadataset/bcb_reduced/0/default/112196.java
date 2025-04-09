import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.*;
import java.net.*;
import java.io.IOException;
import java.net.SocketException;

public class LoadId implements LoadIdN {

    static Connection con = null;

    static String url = "jdbc:mysql://192.168.15.110:3306/";

    static String dbName = "os";

    static String driver = "com.mysql.jdbc.Driver";

    static String userName = "root";

    static String password = "vkmohan123";

    static Statement st;

    LoadId() {
    }

    public static void main(String[] args) {
        int pid = 0;
        String pidStr = "";
        String linuxPath = "/home/mohan/OSProject_05032009/";
        try {
            System.out.println("Into Process check:");
            Process P = Runtime.getRuntime().exec(linuxPath + "/Pid.sh");
            StringBuffer strBuf = new StringBuffer();
            String strLine = "";
            BufferedReader outCommand = new BufferedReader(new InputStreamReader(P.getInputStream()));
            while ((strLine = outCommand.readLine()) != null) {
                pidStr = strLine;
            }
            P.waitFor();
            pid = Integer.parseInt(pidStr);
            System.out.println("at Process check:" + pid);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("after Process check:" + pid);
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(url + dbName, userName, password);
            st = con.createStatement();
            int in1 = pid;
            String sql123 = "update os.LoadRequestResponse set processId=" + pid + " where requestId=" + args[0];
            int update = st.executeUpdate(sql123);
            System.out.println("into actual Load");
            int ins;
            int i;
            int j = 0;
            int k = 0;
            LoadId Obj = new LoadId();
            for (i = 0; i < 10000000; i++) {
                for (int l = 0; l < 20; l++) {
                    j = i + 1;
                    k = j - 1;
                    sql123 = "insert into os.Test(requestId,cnt) values (" + args[0] + "," + i + ")";
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void execLoad1(String sql123) {
        try {
            st.executeUpdate(sql123);
        } catch (SQLException e) {
            System.out.println("SAD");
            System.out.println(e.getMessage());
            e.printStackTrace();
            try {
                Class.forName(driver).newInstance();
                con = DriverManager.getConnection(url + dbName, userName, password);
                st = con.createStatement();
            } catch (Exception ex1) {
                System.out.println(ex1.getMessage());
                ex1.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("SADDDD");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
