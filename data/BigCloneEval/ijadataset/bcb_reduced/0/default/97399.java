import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import Ozymandias.*;

/**
 * This class implements the User object.
 */
public class User extends Ozymandias {

    /**
	 * Returns the number of unread messages in a list for the calling User.
	 */
    public int getUnreadMessages(List list) {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(M_ID) FROM Messages WHERE L_ID='" + list.getLID() + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            int all = results.getInt("COUNT(M_ID)");
            dbQuery = "SELECT COUNT(R.M_ID) FROM ReadMessages R, Messages L WHERE (R.U_ID='" + getUID() + "' AND R.M_ID=L.M_ID AND L_ID='" + list.getLID() + "')";
            results = stmt.executeQuery(dbQuery);
            int read = results.getInt("COUNT(R.M_ID)");
            return (all - read);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return 0;
    }

    /**
	 * Subscribes a User to the given List.
	 */
    public void subscribe(List list) {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "INSERT INTO UserLists (U_ID, L_ID) VALUES ('" + getUID() + "', '" + list.getLID() + "')";
            stmt.executeQuery(dbQuery);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
    }

    /**
	 * Unsubscribes a User from the given List.
	 */
    public void unsubscribe(List list) {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "DELETE FROM UserLists WHERE (U_ID='" + getUID() + "' AND L_ID='" + list.getLID() + "')";
            stmt.executeQuery(dbQuery);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
    }

    /**
	 * Returns an array of ints that references all users sorted in ascending order by
	 * username.
	 */
    public static int[] getAllUsers() {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(U_ID) FROM Users WHERE U_ID > 0";
            ResultSet results = stmt.executeQuery(dbQuery);
            int[] allUsers = new int[results.getInt("COUNT(U_ID)")];
            dbQuery = "SELECT U_ID FROM Users WHERE U_ID > 0 ORDER BY Username";
            results = stmt.executeQuery(dbQuery);
            for (int i = 0; results.next(); i++) {
                allUsers[i] = results.getInt("U_ID");
            }
            return allUsers;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
	 * Marks the given ListMessage as having been read by the User.
	 */
    public void markRead(ListMessage msg) {
        if (readMessage(msg)) return;
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "INSERT INTO ReadMessages (U_ID, M_ID) VALUES ('" + getUID() + "', '" + msg.getMID() + "')";
            stmt.executeQuery(dbQuery);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
    }

    /**
	 * Checks to determine if the given ListMessage has been read by the User.
	 */
    public boolean readMessage(ListMessage msg) {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(M_ID) FROM ReadMessages WHERE (U_ID='" + getUID() + "' AND M_ID='" + msg.getMID() + "')";
            ResultSet results = stmt.executeQuery(dbQuery);
            int count = results.getInt("COUNT(M_ID)");
            if (count > 0) return true;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Returns an array of ints that references all Lists that a User is subscribed to.
	 */
    public int[] getMyLists() {
        int[] myLists = { 1 };
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(L_ID) FROM UserLists WHERE U_ID='" + userUID + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            myLists = new int[results.getInt("COUNT(L_ID)")];
            dbQuery = "SELECT L_ID FROM UserLists WHERE U_ID='" + getUID() + "'";
            results = stmt.executeQuery(dbQuery);
            for (int i = 0; results.next(); i++) {
                myLists[i] = results.getInt("L_ID");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return myLists;
    }

    /**
           * This function checks to see if another instance of an e-mail address exists in the
           * database and returns true if it does.
           */
    public static boolean checkEmail(String email) {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(U_ID) FROM Users WHERE Email='" + email + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            if (results.getInt("COUNT(U_ID)") != 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * This function checks to see if another instance of a username exists in the 
	 * database and returns true if it does.
	 */
    public static boolean checkUsername(String username) {
        try {
            Connection conn = Ozymandias.getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(U_ID) FROM Users WHERE Username='" + username + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            if (results.getInt("COUNT(U_ID)") != 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * This function returns an array if ints that references all
	 * messages associated with the calling User object.
	 */
    public int[] getMessages() {
        int[] allMessages = { 0 };
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(M_ID) FROM Messages WHERE FromAddress='" + getEmail() + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            allMessages = new int[results.getInt("COUNT(M_ID)")];
            dbQuery = "SELECT M_ID FROM Messages WHERE FromAddress='" + getEmail() + "' ORDER BY Received";
            results = stmt.executeQuery(dbQuery);
            for (int i = 0; results.next(); i++) {
                allMessages[i] = results.getInt("M_ID");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return allMessages;
    }

    /**
	 * Commits a User object to the database.
	 */
    public boolean addNewUser() {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "INSERT INTO Users (Email, FirstName, LastName, Username) VALUES ('" + getEmail() + "', '" + getFirstName() + "', '" + getLastName() + "', '" + getUsername() + "')";
            stmt.executeQuery(dbQuery);
            dbQuery = "SELECT COUNT(U_ID) FROM Users WHERE Email='" + getEmail() + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            int numRows = results.getInt("COUNT(U_ID)");
            if (numRows > 0) return true; else return false;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Updates the database with the calling User object.
	 */
    public boolean updateUser() {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "Update Users SET Email='" + getEmail() + "' WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "Update Users SET FirstName='" + getFirstName() + "' WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "Update Users SET LastName='" + getLastName() + "' WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            return true;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Resets the cookie value, in effect logging a user off.
	 */
    public void resetCookie() {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "Update Users SET Cookie='' WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
    }

    /**
	 * Checks to determine if a user has the correct cookie to stay logged on.
	 */
    public static boolean checkLoginCookie(Cookie[] cookies) {
        if (cookies == null) return false;
        for (int i = 0; i < cookies.length; i++) {
            if (User.checkCookie(cookies[i])) return true;
        }
        return false;
    }

    /**
	 * Returns a username based upon the cookies stored upon the browser.
	 */
    public static String getUsernameFromCookie(Cookie[] cookies) {
        for (int i = 0; i < cookies.length; i++) {
            if (User.checkCookie(cookies[i])) return cookies[i].getName();
        }
        return "";
    }

    /**
	 * Deletes the calling User object from the database.
	 */
    public boolean delete() {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "DELETE FROM Admins WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "DELETE FROM UserLists WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "DELETE FROM ReadMessages WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "DELETE FROM Users WHERE U_ID='" + getUID() + "'";
            stmt.executeQuery(dbQuery);
            return true;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Default constructor.
	 */
    public User() {
        userUID = -1;
        userEmail = null;
        userFirstName = null;
        userLastName = null;
        userUsername = null;
        userPasswd = null;
        userCookie = null;
        userLastLogin = null;
    }

    /**
	 * Alternative constructor.
	 */
    public User(String email, String firstName, String lastName, String username) {
        userUID = -1;
        userEmail = email;
        userFirstName = firstName;
        userLastName = lastName;
        userUsername = username;
        userPasswd = null;
        userCookie = null;
        userLastLogin = null;
    }

    /**
	 * Loads values in calling User object from database.
	 */
    public void getUser(int uid) {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT * FROM Users WHERE U_ID='" + uid + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            userUID = results.getInt("U_ID");
            userEmail = results.getString("Email");
            userFirstName = results.getString("FirstName");
            userLastName = results.getString("LastName");
            userUsername = results.getString("Username");
            userCookie = results.getString("Cookie");
            userLastLogin = results.getString("LastLogin");
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
    }

    /**
	 * Loads values in calling User object from database.
	 */
    public void getUser(String username) {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT * FROM Users WHERE Username='" + username + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            userUID = results.getInt("U_ID");
            userEmail = results.getString("Email");
            userFirstName = results.getString("FirstName");
            userLastName = results.getString("LastName");
            userUsername = results.getString("Username");
            userCookie = results.getString("Cookie");
            userLastLogin = results.getString("LastLogin");
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
    }

    /**
	 * Loads a User from the database associated with an e-mail address. Returns 'true' if successful.
	 */
    public boolean getUserByEmail(String email) {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(U_ID) FROM Users WHERE Email='" + email + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            if (results.getInt("COUNT(U_ID)") == 0) return false;
            dbQuery = "SELECT U_ID FROM Users WHERE Email='" + email + "'";
            results = stmt.executeQuery(dbQuery);
            int uid = results.getInt("U_ID");
            getUser(uid);
            return true;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Sets e-mail address.
	 */
    public String setEmail(String email) {
        userEmail = email;
        return userEmail;
    }

    /**
	 * Sets first name.
	 */
    public String setFirstName(String firstName) {
        userFirstName = firstName;
        return userFirstName;
    }

    /**
	 * Sets last name.
	 */
    public String setLastName(String lastName) {
        userLastName = lastName;
        return userLastName;
    }

    /**
	 * Sets username.
	 */
    public String setUsername(String username) {
        userUsername = username;
        return userUsername;
    }

    /**
	 * Sets the current session cookie.
	 */
    public String setCookie() {
        try {
            Random rand = new Random();
            Long cookie = new Long(rand.nextLong());
            userCookie = cookie.toString().substring(0, 8);
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "UPDATE Users Set Cookie='" + userCookie + "' WHERE Username='" + userUsername + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "UPDATE Users Set LastLogin=NOW() WHERE Username='" + userUsername + "'";
            stmt.executeQuery(dbQuery);
            dbQuery = "SELECT LastLogin FROM Users WHERE Username='" + userUsername + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            userLastLogin = results.getString("LastLogin");
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return userCookie;
    }

    /**
	 * Get user ID.
	 */
    public int getUID() {
        return userUID;
    }

    /**
	 * Get username.
	 */
    public String getUsername() {
        return userUsername;
    }

    /**
	 * Get last name.
	 */
    public String getLastName() {
        return userLastName;
    }

    /**
	 * Get first name.
	 */
    public String getFirstName() {
        return userFirstName;
    }

    /**
	 * Get e-mail.
	 */
    public String getEmail() {
        return userEmail;
    }

    /**
	 * Returns date of last login.
	 */
    public String getLastLogin() {
        return userLastLogin;
    }

    /** 
	 * Returns the current cookie value associated with a User.
	 */
    public String getCookie() {
        return userCookie;
    }

    /**
	 * Checks user password for validity, and returns true if password is correct.
	 */
    public boolean checkPasswd(String guess) {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(U_ID) FROM Users WHERE (U_ID='" + getUID() + "' AND Passwd=password('" + guess + "'))";
            ResultSet results = stmt.executeQuery(dbQuery);
            if (results.getInt("COUNT(U_ID)") == 1) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Verifies session cookie.
	 */
    public static boolean checkCookie(Cookie cookie) {
        try {
            User newUser = new User();
            newUser.getUser(cookie.getName());
            if (newUser.getCookie().equals(cookie.getValue())) return true; else return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void newRandomPasswd() {
        Random rand = new Random();
        String passwd = new String("");
        for (int i = 0; i < 8; i++) {
            int number = (rand.nextInt() % 26);
            char c = 'a';
            for (int j = 0; j < number; j++) {
                c++;
            }
            Character schar = new Character(c);
            passwd = passwd.concat(schar.toString());
        }
        userPasswd = setPasswd(passwd);
    }

    /**
	 * Sets initial password and mails the user a message about their new membership.
	 */
    public void sendIntroPackage() {
        newRandomPasswd();
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "localhost");
            Session mailsession = Session.getDefaultInstance(props, null);
            Message email = new MimeMessage(mailsession);
            email.setFrom(new InternetAddress("ozymandias-admin@aetherial.net"));
            InternetAddress[] address = { new InternetAddress(userEmail) };
            email.setRecipients(Message.RecipientType.TO, address);
            email.setSubject("Ozymandias Registration");
            String content = new String();
            content = content + "Welcome to the Ozymandias Internet Mailing List Archive!\n\n";
            content = content + "Your account has been successfully created and you may log on and personalize\n";
            content = content + "the service at (http://fire.aetherial.net:8081/ozymandias/login).\n\n";
            content = content + "Your password has been initialized to " + userPasswd + ". Please change this password\n";
            content = content + "upon logging in.\n\n";
            content = content + "If you have any questions about the service, please check the online documentation\n";
            content = content + "at (http://fire.aetherial.net:8081/ozymandias/help). If you have any questions\n";
            content = content + "about the Terms of Service, it can be viewed at\n";
            content = content + "(http://fire.aetherial.net:8081/ozymandias/signup?action=tos)\n\n";
            content = content + "Once more, thank you for using this product for your mailing list needs.\n\n";
            content = content + "-Ozymandias Staff";
            email.setText(content);
            Transport.send(email);
        } catch (MessagingException e) {
            System.out.println(e);
        }
    }

    /**
	 * Sets a user's passsword to the String provided.
	 */
    public String setPasswd(String passwd) {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "UPDATE Users SET Passwd=password('" + passwd + "') WHERE Username='" + userUsername + "'";
            stmt.executeQuery(dbQuery);
            return passwd;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
	 * Chescks to determine if a user is a site administrator.
	 */
    public boolean isAdmin() {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(A_ID) FROM Admins WHERE U_ID='" + getUID() + "'";
            ResultSet results = stmt.executeQuery(dbQuery);
            if (results.getInt("COUNT(A_ID)") == 1) return true; else return false;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
	 * Checks to determine if a User is a member of a List.
	 */
    public boolean isMember(List list) {
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(U_ID) FROM UserLists WHERE (U_ID='" + getUID() + "' AND L_ID='" + list.getLID() + "')";
            ResultSet results = stmt.executeQuery(dbQuery);
            if (results.getInt("COUNT(U_ID)") == 1) return true; else return false;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return false;
    }

    private String userEmail;

    private String userFirstName;

    private String userLastName;

    private String userUsername;

    private String userPasswd;

    private String userCookie;

    private String userLastLogin;

    private int userUID;
}
