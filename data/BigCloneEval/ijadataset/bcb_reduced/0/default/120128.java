import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.Vector;
import javax.naming.*;
import javax.sql.*;

/**
 * class MessageBoard
 * This class represents a message board for use with the message board
 * service.
 *
 * @author	  $Author: belzecue $
 * @version   $Revision: 1.12 $
 * @requires  jdk1.3+
 * @see       MessageBoardService
 * @see       Message
 *
 */
public class MessageBoard {

    private int currentMessage;

    private int tid;

    private IOServices io;

    public String mb;

    public String topic;

    public Vector messages;

    /**
	 * public MessageBoard
	 * Initialize a message board from the database given it's topic id, name,
	 * topic, and the user's IOServices (useful for printing).
	 *
	 * @author  $Author: belzecue $
	 * @param   id		The message board's topic id in the database
	 * @param   name	The name of the message board
	 * @param   subj	The topic of the message board
	 * @param   ios		The user's IOServices
	 */
    public MessageBoard(int id, String name, String subj, IOServices ios) {
        currentMessage = 0;
        tid = id;
        io = ios;
        mb = name;
        topic = subj;
        messages = new Vector();
    }

    /**
	 * Add this message board to the database.
	 *
	 * @author    $Author: belzecue $
	 */
    public void writeOut() {
        Context initCtx = null;
        Context envCtx = null;
        DataSource ds = null;
        Connection con = null;
        try {
            initCtx = new InitialContext();
            envCtx = (Context) initCtx.lookup("tbbs:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/tbbsDB");
            con = ds.getConnection();
            java.sql.Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT tid FROM topic_tbl;");
            tid = 0;
            while (rs.next()) {
                if (rs.getInt("tid") == tid) ++tid; else break;
            }
            PreparedStatement insMB = con.prepareStatement("INSERT INTO topic_tbl VALUES(?, ?, ?, 1);");
            insMB.setInt(1, tid);
            insMB.setString(2, mb);
            insMB.setString(3, topic);
            insMB.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * Remove this message board from the database.
	 *
	 * @author    $Author: belzecue $
	 */
    public boolean remove() {
        io.clearScreen();
        io.textColor(io.fg_white + io.bg_black);
        io.print("Confirm delete ((Y)es  (N)o)? ");
        char choice = getMenuSelection();
        if (choice == 'N' || choice == 'n') return false;
        Context initCtx = null;
        Context envCtx = null;
        DataSource ds = null;
        Connection con = null;
        try {
            initCtx = new InitialContext();
            envCtx = (Context) initCtx.lookup("tbbs:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/tbbsDB");
            con = ds.getConnection();
            con.setAutoCommit(false);
            java.sql.Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT tid FROM topic_tbl " + "WHERE tid = " + tid + ";");
            if (!rs.next()) return false;
            stmt.executeUpdate("DELETE FROM message_tbl WHERE tid = " + tid + ";");
            stmt.executeUpdate("DELETE FROM topic_tbl WHERE tid = " + tid + ";");
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
	 * loadMessages
	 * Load the contents of this user's mail box from the database.
	 *
	 * @author  $Author: belzecue $
	 */
    private void loadMessages() {
        messages = new Vector();
        Context initCtx = null;
        Context envCtx = null;
        DataSource ds = null;
        Connection con = null;
        try {
            initCtx = new InitialContext();
            envCtx = (Context) initCtx.lookup("tbbs:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/tbbsDB");
            con = ds.getConnection();
            java.sql.Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT mid FROM message_tbl " + "WHERE tid = " + tid + ";");
            while (rs.next()) messages.add(new Message(rs.getInt("mid")));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (currentMessage > messages.size() - 1) currentMessage = messages.size() - 1;
        if (currentMessage < 0) currentMessage = 0;
    }

    /**
	 * addMessage
	 * Add a message to the user's mail box and to the database.
	 *
	 * @author  $Author: belzecue $
	 * @param   m	The message to add
	 */
    private void addMessage(Message m) {
        if (!m.writeOut(tid)) {
            io.println("Unable to add message!!!");
            io.pressAnyKey();
            return;
        }
        messages.add(m);
    }

    /**
	 * delMessage
	 * Delete the current message from the user's mailbox and the database.
	 *
	 * @author  $Author: belzecue $
	 */
    public void delMessage() {
        if (messages.size() <= 0) return;
        io.clearScreen();
        io.textColor(io.fg_white + io.bg_black);
        io.print("Confirm delete ((Y)es  (N)o)? ");
        char choice = getMenuSelection();
        if (choice == 'N' || choice == 'n') return;
        Message m = (Message) messages.elementAt(currentMessage);
        if (!m.remove()) {
            io.println("Unable to delete message!!!");
            io.pressAnyKey();
            return;
        }
        messages.remove(m);
        if (currentMessage > messages.size() - 1) currentMessage = messages.size() - 1;
        if (currentMessage < 0) currentMessage = 0;
    }

    /**
	 * enter
	 * Main entry point into a message board.
	 *
	 * @author  $Author: belzecue $
	 * @param   userName	User's name
	 */
    public void enter(String userName) {
        loadMessages();
        boolean isRunning = true;
        char choice;
        while (isRunning) {
            choice = printMessageBoard();
            if (choice == 'A' || choice == 'a') {
                newMessage(userName);
            } else if (choice == 'R' || choice == 'r' || choice == '\n') {
                printMessage();
            } else if (choice == 'E' || choice == 'e') {
                reply(userName);
            } else if (choice == 'M' || choice == 'm') {
                replyViaEmail(userName);
            } else if (choice == 'D' || choice == 'd') {
                delMessage();
            } else if (choice == 'N' || choice == 'n') {
                nextMessage();
            } else if (choice == 'P' || choice == 'p') {
                prevMessage();
            } else if (choice == 'Q' || choice == 'q') {
                isRunning = false;
            } else {
                io.println("Invalid selection!!!");
                io.pressAnyKey();
            }
        }
    }

    /**
	 * printMessageBoard
	 * Print a list of the messages in this message board (and a nicely
	 * formatted header and footer, too).
	 *
	 * @author  $Author: belzecue $
	 * @return  Character representing user's input
	 */
    private char printMessageBoard() {
        io.clearScreen();
        io.textColor(io.fg_white + io.bg_black);
        io.cursorPut(1, 1);
        io.print("-----[");
        io.print(" " + mb + " ");
        io.println("]-----");
        io.cursorPut(3, 2);
        io.print("Date");
        io.cursorPut(32, 2);
        io.print("From");
        io.cursorPut(47, 2);
        io.println("Subject");
        io.print("-------------------------");
        io.print("-------------------------");
        io.println("-------------------------");
        printMessages();
        io.cursorPut(1, 23);
        io.println("(A)dd  (R)ead  r(E)ply  (M)ail reply  (D)elete  (N)ext  (P)revious  (Q)uit");
        io.print("-------------------------");
        io.print("-------------------------");
        io.println("-------------------------");
        return getMenuSelection();
    }

    /**
	 * printMessages
	 * Format display of messages in user's mail box (print date, author, and
	 * subject, along with a cursor to visually indicate to the user which
	 * message is currently selected)
	 *
	 * @author  $Author: belzecue $
	 */
    private void printMessages() {
        if (messages.size() == 0) {
            io.println("No messages in message board");
            return;
        }
        int lowerBound = currentMessage - (currentMessage % 18);
        int upperBound = currentMessage + 18 - (currentMessage % 18);
        for (; lowerBound < upperBound; ++lowerBound) {
            if (lowerBound < messages.size()) {
                Message m = (Message) messages.elementAt(lowerBound);
                if (lowerBound == currentMessage) {
                    io.cursorPut(1, (lowerBound % 18) + 4);
                    io.print(">");
                }
                io.cursorPut(3, (lowerBound % 18) + 4);
                if (m.date != null) io.print(m.date.toString());
                io.cursorPut(32, (lowerBound % 18) + 4);
                io.print(m.author);
                io.cursorPut(47, (lowerBound % 18) + 4);
                io.println(m.subject.toString());
            }
        }
    }

    /**
	 * newMessage
	 * Create a new message to mail to someone, and verify the recipient as a
	 * registered user.
	 *
	 * @author  $Author: belzecue $
	 * @param   userName	User's name
	 */
    private void newMessage(String userName) {
        Message m = new Message();
        m.author = userName;
        io.clearScreen();
        io.textColor(io.fg_white + io.bg_black);
        io.cursorPut(1, 1);
        io.print("Subject: ");
        m.subject = new StringBuffer(getInputLine(72).trim());
        Message tmp = composeMessage(m);
        if (tmp != null) {
            addMessage(tmp);
        } else io.print("Message aborted");
    }

    /**
	 * composeMessage
	 * Helper function used to control the user inputting the body of the
	 * message.
	 *
	 * @author  $Author: belzecue $
	 * @param   m	The current message this is the body of
	 * @return  Message with body just entered by user
	 */
    private Message composeMessage(Message m) {
        io.clearScreen();
        io.textColor(io.fg_white + io.bg_black);
        io.cursorPut(1, 1);
        io.println("Enter message (enter '.' on a new line to end)");
        io.print("-------------------------");
        io.print("-------------------------");
        io.println("-------------------------");
        String mes = new String();
        while (true) {
            mes = getInputLine(72);
            if (mes.charAt(0) == '.' && (mes.charAt(1) == '\r' || mes.charAt(1) == '\n')) break; else m.message.append(mes);
        }
        while (true) {
            io.cursorPut(1, 24);
            io.textColor(io.fg_white + io.bg_black);
            io.print("Post message ((Y)es  (N)o)? ");
            char choice = getMenuSelection();
            if (choice == 'Y' || choice == 'y' || choice == '\n') return m; else if (choice == 'N' || choice == 'n') return null; else {
                io.println("Invalid selection!!!");
                io.pressAnyKey();
            }
        }
    }

    /**
	 * printMessage
	 * Print the contents of the currently selected message.
	 *
	 * @author  $Author: belzecue $
	 */
    private void printMessage() {
        if (messages.size() <= 0) return;
        Message m = (Message) messages.elementAt(currentMessage);
        printMessage(m, currentMessage);
    }

    private void printMessage(Message m, int i) {
        if (messages.size() <= 0) return;
        io.clearScreen();
        io.textColor(io.fg_white + io.bg_black);
        io.cursorPut(1, 1);
        io.println("-----[ " + i + " ]-----");
        io.println("Date: " + m.date);
        io.println("From: " + m.author);
        io.println("Subject: " + m.subject.toString());
        io.print("-------------------------");
        io.print("-------------------------");
        io.println("-------------------------");
        io.println(m.message.toString());
        io.pressAnyKey();
    }

    /**
	 * reply
	 * Helper message for when a user wishes to reply to a message.  Handles
	 * proper quoting of replied text.
	 *
	 * @author  $Author: belzecue $
	 * @param   userName	User's name
	 */
    private void reply(String userName) {
        if (messages.size() <= 0) return;
        Message orig = (Message) messages.elementAt(currentMessage);
        Message m = new Message();
        m.author = userName;
        if (orig.subject.length() > 0) {
            if (!orig.subject.toString().startsWith("Re:")) m.subject = new StringBuffer("Re: ");
            m.subject.append(orig.subject);
        } else m.subject = new StringBuffer("Re:");
        m.message = orig.message;
        m.message.append("\r\n");
        for (int i = 0; i < m.message.length() - 1; ++i) {
            if (m.message.charAt(i) == '\n' && i != m.message.length() - 1) if (m.message.charAt(i + 1) == '>') m.message.insert(i + 1, '>'); else m.message.insert(i + 1, "> "); else if (i == 0) if (m.message.charAt(i) == '>') m.message.insert(i, '>'); else m.message.insert(i, "> ");
        }
        Message tmp = composeMessage(m);
        if (tmp != null) {
            addMessage(tmp);
        } else io.print("Reply aborted");
    }

    /**
	 * replyViaEmail
	 * Send an email reply to the author of this message.
	 *
	 * @author  $Author: belzecue $
	 * @param   userName	User's name
	 */
    private void replyViaEmail(String userName) {
        if (messages.size() <= 0) return;
        Message orig = (Message) messages.elementAt(currentMessage);
        Message m = new Message();
        m.author = userName;
        if (orig.subject.length() > 0) {
            if (!orig.subject.toString().startsWith("Re:")) m.subject = new StringBuffer("Re: ");
            m.subject.append(orig.subject);
        } else m.subject = new StringBuffer("Re:");
        m.message = orig.message;
        m.message.append("\r\n");
        for (int i = 0; i < m.message.length() - 1; ++i) {
            if (m.message.charAt(i) == '\n' && i != m.message.length() - 1) if (m.message.charAt(i + 1) == '>') m.message.insert(i + 1, '>'); else m.message.insert(i + 1, "> "); else if (i == 0) if (m.message.charAt(i) == '>') m.message.insert(i, '>'); else m.message.insert(i, "> ");
        }
        Message tmp = composeMessage(m);
        if (tmp != null) {
            MailBox mb = new MailBox(orig.author, io);
            mb.addMessage(tmp);
        } else io.print("Reply aborted");
    }

    /**
	 * nextMessage
	 * Increment the currently selected message by 1 (if another message
	 * exists).
	 *
	 * @author  $Author: belzecue $
	 */
    private void nextMessage() {
        ++currentMessage;
        if (currentMessage > messages.size() - 1) currentMessage = messages.size() - 1;
        if (currentMessage < 0) currentMessage = 0;
    }

    /**
	 * prevMessage
	 * Decrement the currently selected message by 1 (if a previous message
	 * exists).
	 *
	 * @author  $Author: belzecue $
	 */
    private void prevMessage() {
        --currentMessage;
        if (currentMessage < 0) currentMessage = 0;
    }

    /**
	 * getMenuSelection
	 * Helper function used to return the next character the user inputs
	 * (useful for making menus more interactive).  Trims trailing whitespace
	 * in case the user also presses enter after making a selection.
	 *
	 * @author  $Author: belzecue $
	 * @return  The next character the user enters
	 */
    private char getMenuSelection() {
        char choice = '"';
        try {
            while (!io.in.ready()) ;
            choice = (char) io.in.read();
            int nil;
            while (io.in.ready()) nil = io.in.read();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        if (choice == '\r') choice = '\n';
        return choice;
    }

    /**
	 * getInputLine
	 * Helper function used for getting a full line of user input; appends
	 * CRLF at the end to ensure proper newlines on both UNIX and Windows.
	 *
	 * @author  $Author: belzecue $
	 * @return  String entered by user, plus CRLF
	 */
    private String getInputLine(int lineLen) {
        return new String(io.inputLine(lineLen) + "\r\n");
    }
}
