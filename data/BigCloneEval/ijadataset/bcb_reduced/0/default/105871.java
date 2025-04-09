import java.sql.*;

public class bbcjdbc {

    public Connection getSQLConnection() throws Exception {
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        String url = "jdbc:odbc:bbs";
        Connection con = DriverManager.getConnection(url);
        System.out.println("MySQL ODBC ���Դ���ӳɹ�...");
        return con;
    }

    public void querySQL(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from bbs");
        System.out.println("--title--" + "-author");
        while (rs.next()) {
            System.out.println(rs.getString("title"));
        }
        stmt.close();
        con.close();
    }

    public int insertOne(Connection conn) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from bbs");
        String str = "insert into bbs (title,author,date,content) values('tcd','Bk','1990','2')";
        return stmt.executeUpdate(str);
    }

    public void getInformation(Connection con) {
        try {
            DatabaseMetaData dm = con.getMetaData();
            System.out.println("������Ϣ:");
            System.out.println("\tDriver Name: " + dm.getDriverVersion());
            System.out.println("��ݿ���Ϣ");
            System.out.println("\tDataBase Name" + dm.getDatabaseProductName());
            System.out.println("\tDataBase Version" + dm.getDatabaseProductVersion());
            System.out.println("��ݿ��б�");
            ResultSet rs = dm.getCatalogs();
            while (rs.next()) {
                System.out.println("\t" + rs.getString(1));
            }
            rs.close();
            rs = null;
            dm = null;
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        bbcjdbc mysql = new bbcjdbc();
        mysql.insertOne(mysql.getSQLConnection());
    }
}
