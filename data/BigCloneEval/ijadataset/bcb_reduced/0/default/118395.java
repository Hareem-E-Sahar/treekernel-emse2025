import java.util.*;
import java.io.*;
import java.sql.*;

public class GameRoleList {

    private Vector _gameRoleList = new Vector();

    public GameRoleList() {
    }

    public void addRole(Role role) {
        _gameRoleList.addElement(role);
    }

    public void removeRole(Role role) {
        _gameRoleList.remove(role);
    }

    public Vector getGameRoleList() {
        return _gameRoleList;
    }

    public String toString() {
        String gameRoles = "Role list: ";
        int counter = 0;
        while (counter < _gameRoleList.size()) {
            Role role = (Role) _gameRoleList.elementAt(counter);
            gameRoles = gameRoles + "\n" + role;
            counter++;
        }
        return gameRoles;
    }

    public static GameRoleList load() {
        GameRoleList gameRoleList = new GameRoleList();
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
            String sql = "select * from role";
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                Role role = (Role) res.getObject("role");
                gameRoleList.addRole(role);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return gameRoleList;
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
            String delsql = "delete from role";
            Statement delstmt = conn.createStatement();
            delstmt.executeQuery(delsql);
            delstmt.close();
            int counter = 0;
            while (counter < _gameRoleList.size()) {
                Object role = _gameRoleList.elementAt(counter);
                PreparedStatement stmt = conn.prepareStatement("insert into role (role) values (?)");
                stmt.setObject(1, role);
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
        return _gameRoleList.size();
    }
}
