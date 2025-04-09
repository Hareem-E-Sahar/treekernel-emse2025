import java.sql.*;
import java.util.Random;
import java.text.DecimalFormat;

public class Main {

    private static String MYSQL_LOGIN = "root";

    private static String MYSQL_PASSWORD = "";

    private static String MYSQL_CONNECTSTRING = "jdbc:mysql://localhost:3306/mysql";

    private static String MYSQL_DATABASE_NAME = "test2";

    private static String MYSQL_TABLE_NAME = "testtable";

    private static Random r = new Random();

    public static void main(String args[]) {
        try {
            boolean createDB = true;
            r.setSeed(System.currentTimeMillis());
            Class.forName("com.mysql.jdbc.Driver");
            String url = MYSQL_CONNECTSTRING;
            Connection con = DriverManager.getConnection(url, MYSQL_LOGIN, MYSQL_PASSWORD);
            Statement stmt = con.createStatement();
            ResultSet rs;
            String sql = "SHOW DATABASES";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if (rs.getString("Database").equals(MYSQL_DATABASE_NAME)) {
                    createDB = false;
                    break;
                }
            }
            if (createDB == true) {
                System.out.println("Creating database " + MYSQL_DATABASE_NAME);
                sql = "CREATE Database " + MYSQL_DATABASE_NAME;
                stmt.executeUpdate(sql);
                System.out.println("Creating table " + MYSQL_TABLE_NAME);
                sql = "CREATE TABLE ";
                sql += MYSQL_DATABASE_NAME + "." + MYSQL_TABLE_NAME;
                sql += "(col1 varchar(16), col2 varchar(16), col3 int)";
                stmt.executeUpdate(sql);
            }
            for (int i = 0; i < 1000; i++) {
                FavoriteKeys.genNextKey();
                sql = "INSERT INTO " + MYSQL_DATABASE_NAME + "." + MYSQL_TABLE_NAME;
                sql += "(col1,col2,col3) ";
                sql += "VALUES('" + FavoriteKeys.key1 + "','";
                sql += FavoriteKeys.key2 + "',";
                sql += FavoriteKeys.value + ")";
                stmt.executeUpdate(sql);
            }
            displayDistributionReport();
            con.close();
        } catch (Exception e) {
            System.out.println("exception: " + e);
        }
    }

    private static class FavoriteKeys {

        static char[] column1Values = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j' };

        static char[] column2Values = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L' };

        static char key1;

        static char key2;

        static int value;

        static int index = 0;

        static int[] distribution1 = new int[10];

        static int[] distribution2 = new int[12];

        static void genNextKey() {
            int index1, index2;
            double r1, r2, thisLog;
            r1 = Math.random() * 147.0;
            thisLog = Math.log(r1 + 1);
            index1 = (int) Math.floor(thisLog * 2);
            distribution1[index1]++;
            r2 = Math.random() * 400.0;
            thisLog = Math.log(r2 + 1);
            index2 = (int) Math.floor(thisLog * 2);
            distribution2[index2]++;
            key1 = column1Values[index1];
            key2 = column2Values[index2];
            value = 3 + r.nextInt(98);
            return;
        }
    }

    private static void displayDistributionReport() {
        DecimalFormat formatter = new DecimalFormat("00.00");
        String report;
        double thisDistribution;
        System.out.println("Value \tCount \tDistribution");
        for (int i = 0; i < 10; i++) {
            thisDistribution = FavoriteKeys.distribution1[i] / 1000.0;
            thisDistribution *= 100;
            report = FavoriteKeys.column1Values[i] + " \t" + FavoriteKeys.distribution1[i];
            report += " \t" + formatter.format(thisDistribution) + "%";
            System.out.println(report);
        }
        System.out.println("\nValue \tCount \tDistribution");
        for (int i = 0; i < 12; i++) {
            thisDistribution = FavoriteKeys.distribution2[i] / 1000.0;
            thisDistribution *= 100;
            report = FavoriteKeys.column2Values[i] + " \t" + FavoriteKeys.distribution2[i];
            report += " \t" + formatter.format(thisDistribution) + "%";
            System.out.println(report);
        }
    }
}
