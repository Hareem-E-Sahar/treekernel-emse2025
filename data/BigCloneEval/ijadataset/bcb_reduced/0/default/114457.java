import java.util.*;
import java.io.*;
import java.sql.*;

public class GameBattlePlanTmplList {

    private Vector _gameBattlePlanTmplList = new Vector();

    public GameBattlePlanTmplList() {
    }

    public void addBattlePlanTmpl(BattlePlanTmpl battlePlanTmpl) {
        _gameBattlePlanTmplList.addElement(battlePlanTmpl);
    }

    public void removeBattlePlanTmpl(BattlePlanTmpl battlePlanTmpl) {
        _gameBattlePlanTmplList.remove(battlePlanTmpl);
    }

    public Vector getGameBattlePlanTmplList() {
        return _gameBattlePlanTmplList;
    }

    public String toString() {
        String gameBattlePlanTmpl = "BattlePlanTmpl list: ";
        int counter = 0;
        while (counter < _gameBattlePlanTmplList.size()) {
            BattlePlanTmpl battlePlanTmpl = (BattlePlanTmpl) _gameBattlePlanTmplList.elementAt(counter);
            gameBattlePlanTmpl = gameBattlePlanTmpl + "\n" + battlePlanTmpl;
            counter++;
        }
        return gameBattlePlanTmpl;
    }

    public static GameBattlePlanTmplList load() {
        GameBattlePlanTmplList gameBattlePlanTmplList = new GameBattlePlanTmplList();
        Connection conn = null;
        try {
            String dbURL = "jdbc:hsqldb:/tmp/yacht";
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection(dbURL, "sa", "");
        } catch (Exception e) {
            System.out.println("ERROR: Could not connect to database");
            System.out.print(e);
        }
        try {
            String sql = "select * from battleplan";
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                BattlePlanTmpl battlePlan = (BattlePlanTmpl) res.getObject("battleplan");
                gameBattlePlanTmplList.addBattlePlanTmpl(battlePlan);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return gameBattlePlanTmplList;
    }

    public void save() {
        Connection conn = null;
        try {
            String dbURL = "jdbc:hsqldb:/tmp/yacht";
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection(dbURL, "sa", "");
        } catch (Exception e) {
            System.out.println("ERROR: Could not connect to database");
            System.out.print(e);
        }
        try {
            String delsql = "delete from battleplan";
            Statement delstmt = conn.createStatement();
            delstmt.executeQuery(delsql);
            delstmt.close();
            int counter = 0;
            while (counter < _gameBattlePlanTmplList.size()) {
                Object battlePlan = _gameBattlePlanTmplList.elementAt(counter);
                PreparedStatement stmt = conn.prepareStatement("insert into battleplan (battleplan) values (?)");
                stmt.setObject(1, battlePlan);
                stmt.executeUpdate();
                stmt.close();
                counter++;
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int size() {
        return _gameBattlePlanTmplList.size();
    }
}
