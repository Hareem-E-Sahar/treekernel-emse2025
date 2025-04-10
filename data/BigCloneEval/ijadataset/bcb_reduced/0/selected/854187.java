package com.l2jserver.accountmanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.l2jserver.Base64;
import com.l2jserver.Config;
import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.Server;
import javolution.util.FastList;

/**
 * This class SQL Account Manager
 *
 * @author netimperia
 * @version $Revision: 2.3.2.1.2.3 $ $Date: 2005/08/08 22:47:12 $
 */
public class SQLAccountManager {

    private static String _uname = "";

    private static String _pass = "";

    private static String _level = "";

    private static String _mode = "";

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException {
        Server.serverMode = Server.MODE_LOGINSERVER;
        Config.load();
        while (true) {
            System.out.println("Please choose an option:");
            System.out.println("");
            System.out.println("1 - Create new account or update existing one (change pass and access level).");
            System.out.println("2 - Change access level.");
            System.out.println("3 - Delete existing account.");
            System.out.println("4 - List accounts & access levels.");
            System.out.println("5 - Exit.");
            LineNumberReader _in = new LineNumberReader(new InputStreamReader(System.in));
            while (!(_mode.equals("1") || _mode.equals("2") || _mode.equals("3") || _mode.equals("4") || _mode.equals("5"))) {
                System.out.print("Your choice: ");
                _mode = _in.readLine();
            }
            if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3")) {
                if (_mode.equals("1") || _mode.equals("2")) {
                    while (_uname.trim().length() == 0) {
                        System.out.print("Username: ");
                        _uname = _in.readLine().toLowerCase();
                    }
                } else if (_mode.equals("3")) {
                    while (_uname.trim().length() == 0) {
                        System.out.print("Account name: ");
                        _uname = _in.readLine().toLowerCase();
                    }
                }
                if (_mode.equals("1")) {
                    while (_pass.trim().length() == 0) {
                        System.out.print("Password: ");
                        _pass = _in.readLine();
                    }
                }
                if (_mode.equals("1") || _mode.equals("2")) {
                    while (_level.trim().length() == 0) {
                        System.out.print("Access level: ");
                        _level = _in.readLine();
                    }
                }
            }
            if (_mode.equals("1")) {
                addOrUpdateAccount(_uname.trim(), _pass.trim(), _level.trim());
            } else if (_mode.equals("2")) {
                changeAccountLevel(_uname.trim(), _level.trim());
            } else if (_mode.equals("3")) {
                System.out.print("Do you really want to delete this account ? Y/N : ");
                String yesno = _in.readLine();
                if (yesno.equalsIgnoreCase("Y")) deleteAccount(_uname.trim()); else System.out.println("Deletion cancelled");
            } else if (_mode.equals("4")) {
                _mode = "";
                System.out.println("");
                System.out.println("Please choose a listing mode:");
                System.out.println("");
                System.out.println("1 - Banned accounts only (accessLevel < 0)");
                System.out.println("2 - GM/privileged accounts (accessLevel > 0)");
                System.out.println("3 - Regular accounts only (accessLevel = 0)");
                System.out.println("4 - List all");
                while (!(_mode.equals("1") || _mode.equals("2") || _mode.equals("3") || _mode.equals("4"))) {
                    System.out.print("Your choice: ");
                    _mode = _in.readLine();
                }
                System.out.println("");
                printAccInfo(_mode);
            } else if (_mode.equals("5")) {
                System.exit(0);
            }
            _uname = "";
            _pass = "";
            _level = "";
            _mode = "";
            System.out.println();
        }
    }

    private static void printAccInfo(String m) throws SQLException {
        int count = 0;
        Connection con = null;
        con = L2DatabaseFactory.getInstance().getConnection();
        String q = "SELECT login, accessLevel FROM accounts ";
        if (m.equals("1")) q = q.concat("WHERE accessLevel < 0"); else if (m.equals("2")) q = q.concat("WHERE accessLevel > 0"); else if (m.equals("3")) q = q.concat("WHERE accessLevel = 0");
        q = q.concat(" ORDER BY login ASC");
        PreparedStatement statement = con.prepareStatement(q);
        ResultSet rset = statement.executeQuery();
        while (rset.next()) {
            System.out.println(rset.getString("login") + " -> " + rset.getInt("accessLevel"));
            count++;
        }
        rset.close();
        statement.close();
        L2DatabaseFactory.close(con);
        System.out.println("Displayed accounts: " + count + ".");
    }

    private static void addOrUpdateAccount(String account, String password, String level) throws IOException, SQLException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte[] newpass;
        newpass = password.getBytes("UTF-8");
        newpass = md.digest(newpass);
        Connection con = null;
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("REPLACE accounts (login, password, accessLevel) VALUES (?,?,?)");
        statement.setString(1, account);
        statement.setString(2, Base64.encodeBytes(newpass));
        statement.setString(3, level);
        statement.executeUpdate();
        statement.close();
        L2DatabaseFactory.close(con);
    }

    private static void changeAccountLevel(String account, String level) throws SQLException {
        Connection con = null;
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE login=?;");
        statement.setString(1, account);
        ResultSet rset = statement.executeQuery();
        if (!rset.next()) {
            System.out.println("False");
        } else if (rset.getInt(1) > 0) {
            statement = con.prepareStatement("UPDATE accounts SET accessLevel=? WHERE login=?;");
            statement.setEscapeProcessing(true);
            statement.setString(1, level);
            statement.setString(2, account);
            statement.executeUpdate();
            System.out.println("Account " + account + " has been updated.");
        } else {
            System.out.println("Account " + account + " does not exist.");
        }
        rset.close();
        statement.close();
        L2DatabaseFactory.close(con);
    }

    private static void deleteAccount(String account) throws SQLException {
        Connection con = null;
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE login=?;");
        statement.setString(1, account);
        ResultSet rset = statement.executeQuery();
        if (!rset.next()) {
            System.out.println("False");
            rset.close();
        } else if (rset.getInt(1) > 0) {
            rset.close();
            ResultSet rcln;
            statement = con.prepareStatement("SELECT charId, char_name, clanid FROM characters WHERE account_name=?;");
            statement.setEscapeProcessing(true);
            statement.setString(1, account);
            rset = statement.executeQuery();
            FastList<String> objIds = new FastList<String>();
            FastList<String> charNames = new FastList<String>();
            FastList<String> clanIds = new FastList<String>();
            while (rset.next()) {
                objIds.add(rset.getString("charId"));
                charNames.add(rset.getString("char_name"));
                clanIds.add(rset.getString("clanid"));
            }
            rset.close();
            for (int index = 0; index < objIds.size(); index++) {
                System.out.println("Deleting character " + charNames.get(index) + ".");
                statement.close();
                statement = con.prepareStatement("SELECT COUNT(*) FROM clan_data WHERE leader_id=?;");
                statement.setString(1, clanIds.get(index));
                rcln = statement.executeQuery();
                rcln.next();
                if (rcln.getInt(1) > 0) {
                    rcln.close();
                    statement.close();
                    statement = con.prepareStatement("SELECT clan_name FROM clan_data WHERE leader_id=?;");
                    statement.setString(1, clanIds.get(index));
                    rcln = statement.executeQuery();
                    rcln.next();
                    String clanName = rcln.getString("clan_name");
                    System.out.println("Deleting clan " + clanName + ".");
                    statement.close();
                    statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?;");
                    statement.setEscapeProcessing(true);
                    statement.setString(1, clanName);
                    statement.setString(2, clanName);
                    statement.executeUpdate();
                    rcln.close();
                    statement.close();
                    statement = con.prepareStatement("UPDATE characters SET clanid=0 WHERE clanid=?;");
                    statement.setString(1, clanIds.get(index));
                    statement.executeUpdate();
                    statement.close();
                    statement = con.prepareStatement("UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE ownerId=?;");
                    statement.setString(1, clanIds.get(index));
                    statement.executeUpdate();
                    statement.close();
                    statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?;");
                    statement.setString(1, clanIds.get(index));
                    statement.executeUpdate();
                    statement.close();
                    statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?;");
                    statement.setString(1, clanIds.get(index));
                    statement.executeUpdate();
                    statement.close();
                    statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?;");
                    statement.setString(1, clanIds.get(index));
                    statement.executeUpdate();
                    statement.close();
                    statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?;");
                    statement.setString(1, clanIds.get(index));
                    statement.executeUpdate();
                } else rcln.close();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM boxaccess WHERE charname=?;");
                statement.setString(1, charNames.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_recommends WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_ui_categories WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM character_ui_keys WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
                statement.close();
                statement = con.prepareStatement("DELETE FROM characters WHERE charId=?;");
                statement.setString(1, objIds.get(index));
                statement.executeUpdate();
            }
            statement.close();
            statement = con.prepareStatement("DELETE FROM accounts WHERE login=?;");
            statement.setEscapeProcessing(true);
            statement.setString(1, account);
            statement.executeUpdate();
            System.out.println("Account " + account + " has been deleted.");
        } else {
            System.out.println("Account " + account + " does not exist.");
        }
        statement.close();
        L2DatabaseFactory.close(con);
    }
}
