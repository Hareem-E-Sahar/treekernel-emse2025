import java.util.*;
import java.sql.*;

public class NNTPPersistentSQL implements NNTPPersistent {

    private static final boolean jdbc2_batch_supported = false;

    private Connection dbCon = null;

    private Statement dbQuery;

    private PreparedStatement dbSubQuery, dbMsgQuery, dbGrpQuery, dbFrmQuery;

    private PreparedStatement dbGroupCountQuery, dbGroupMessageQuery;

    private PreparedStatement dbInsertSubject, dbInsertFrom, dbInsertMessage;

    private PreparedStatement dbInsertReferences, dbInsertNewsgroups, dbInsertGroupMsg;

    private PreparedStatement dbGrpAdd;

    public void addNewsgroup(NNTPGroup nng) {
        try {
            dbGrpAdd.clearParameters();
            dbGrpAdd.setString(1, nng.getName());
            dbGrpAdd.setInt(2, nng.getFirst());
            dbGrpAdd.setInt(3, nng.getLast());
            dbGrpAdd.setInt(4, nng.getServerCount());
            dbGrpAdd.setInt(5, nng.getLocalCount());
            dbGrpAdd.setInt(6, nng.getSubscribed() ? 1 : 0);
            dbGrpAdd.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add group" + nng.getName() + "\nException: " + e);
        }
    }

    public int loadActive(Collection outList) {
        int groupCount = 0;
        ResultSet rs;
        NNTPGroup nng;
        try {
            rs = dbQuery.executeQuery("SELECT newsgroup,firstmsg,lastmsg,servercount,localcount FROM newsgroups");
            if (rs.next()) {
                do {
                    groupCount++;
                    nng = new NNTPGroup(rs.getString("newsgroup"), rs.getInt("firstmsg"), rs.getInt("lastmsg"), rs.getInt("servercount"), rs.getInt("localcount"));
                    outList.add(nng);
                    if ((groupCount % 1000) == 0) {
                        System.out.print("Group: " + groupCount + "\r");
                    }
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.err.println("Error loading active list\nException: " + e);
        }
        return (groupCount);
    }

    public int loadSubscribed(Collection outList) {
        int groupCount = 0;
        ResultSet rs;
        NNTPGroup nng;
        try {
            rs = dbQuery.executeQuery("SELECT newsgroup,firstmsg,lastmsg,servercount,localcount FROM newsgroups WHERE subscribed = 1");
            if (rs.next()) {
                do {
                    groupCount++;
                    nng = new NNTPGroup(rs.getString("newsgroup"), rs.getInt("firstmsg"), rs.getInt("lastmsg"), rs.getInt("servercount"), rs.getInt("localcount"));
                    outList.add(nng);
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.err.println("Error loading active list\nException: " + e);
        }
        return (groupCount);
    }

    public boolean haveArticlesForGroup(String groupname) {
        int localHeaders = 0;
        try {
            localHeaders = getId(dbGroupCountQuery, groupname, "localcount");
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e);
            localHeaders = 0;
        }
        return (localHeaders > 0);
    }

    public int getArticles(Collection loadList, String newsgroup, int startmsg, int endmsg) {
        int msgCount = 0;
        try {
            NNTPThread newThread;
            ResultSet rs;
            dbGroupMessageQuery.clearParameters();
            dbGroupMessageQuery.setString(1, newsgroup);
            dbGroupMessageQuery.setString(2, "msgsubject");
            rs = dbGroupMessageQuery.executeQuery();
            if (rs.next()) {
                do {
                    msgCount++;
                    newThread = new NNTPThread(rs.getString("msgsubject"), rs.getString("msgfrom"), rs.getString("msgdate"), rs.getString("messageid"), rs.getInt("msgbytes"), rs.getInt("msglines"), startmsg + msgCount);
                    loadList.add(newThread);
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.err.println("NNTPPersistentSQL: getArticles SQL exception!\nException: " + e);
        }
        return msgCount;
    }

    public boolean init() {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Yipe: " + e);
        }
        try {
            dbCon = DriverManager.getConnection("jdbc:mysql://localhost/usenet", "cyberfox", "");
            dbQuery = dbCon.createStatement();
            dbFrmQuery = dbCon.prepareStatement("SELECT fromid FROM froms WHERE msgfrom = ?");
            dbMsgQuery = dbCon.prepareStatement("SELECT msgid FROM messages WHERE messageid = ?");
            dbGrpQuery = dbCon.prepareStatement("SELECT groupid FROM newsgroups WHERE newsgroup = ?");
            dbSubQuery = dbCon.prepareStatement("SELECT subjectid FROM subjects WHERE msgsubject = ?");
            dbGroupCountQuery = dbCon.prepareStatement("SELECT localcount,servercount FROM newsgroups WHERE newsgroup = ?");
            dbGroupMessageQuery = dbCon.prepareStatement("SELECT msgsubject, msgfrom, msgdate, messageid, msgbytes, msglines FROM newsgroups NATURAL LEFT JOIN groupmsgs NATURAL LEFT JOIN messages NATURAL LEFT JOIN subjects NATURAL LEFT JOIN froms WHERE (newsgroup = ? && messages.fromid = froms.fromid) ORDER BY ?");
            dbInsertFrom = dbCon.prepareStatement("INSERT INTO froms (msgfrom) values (?)");
            dbInsertSubject = dbCon.prepareStatement("INSERT INTO subjects (msgsubject) values (?)");
            dbInsertNewsgroups = dbCon.prepareStatement("INSERT INTO newsgroups (newsgroup) values (?)");
            dbInsertGroupMsg = dbCon.prepareStatement("INSERT INTO groupmsgs (msgid, groupid) values (?, ?)");
            dbInsertReferences = dbCon.prepareStatement("INSERT INTO msgrefs (msgid, refid, refstr) values (?, ?, ?)");
            dbInsertMessage = dbCon.prepareStatement("INSERT INTO messages (messageid, msgdate, msglines, msgscore, subjectid, fromid) values (?, ?, ?, ?, ?, ?)");
            dbGrpAdd = dbCon.prepareStatement("INSERT INTO newsgroups (newsgroup, firstmsg, lastmsg, servercount, localcount, subscribed) values (?, ?, ?, ?, ?, ?)");
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e);
            return (false);
        }
        return (true);
    }

    public void storeArticle(String newsgroup, NNTPThread curThread) {
        try {
            String curMessageId = curThread.getMessageId();
            int msgid, subjid, fromid, groupid;
            ResultSet rs;
            msgid = getMsgId(curMessageId);
            if (msgid == 0) {
                subjid = findAddSubject(curThread.getSubject());
                fromid = findAddFrom(curThread.getFrom());
                groupid = findAddNewsgroup(newsgroup);
                insertMessage(curThread, subjid, fromid);
                msgid = getMsgId(curMessageId);
                if (msgid == 0) {
                    System.out.println("JNews: DB search for just-added messageid failed!");
                    return;
                }
                insertGroupLink(msgid, groupid);
                insertReferences(msgid, curThread.getReferences());
            } else {
                System.out.println("JNews: Message " + curThread.getMessageId() + " already in db.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e);
        }
    }

    private int getId(PreparedStatement prep, String argument, String fieldname) throws SQLException {
        int outValue = 0;
        ResultSet rs;
        prep.clearParameters();
        prep.setString(1, argument);
        rs = prep.executeQuery();
        if (rs.next()) {
            outValue = rs.getInt(fieldname);
        }
        rs.close();
        return (outValue);
    }

    private int getMsgId(String msgId) throws SQLException {
        return (getId(dbMsgQuery, msgId, "msgid"));
    }

    private void insertReferences(int msgid, String References) throws SQLException {
        Vector allRefs;
        int xrefid;
        dbInsertReferences.setInt(1, msgid);
        allRefs = StringVector.makeVector(References, ' ');
        for (int i = 0; i < allRefs.size(); i++) {
            xrefid = getMsgId((String) allRefs.elementAt(i));
            dbInsertReferences.clearParameters();
            dbInsertReferences.setInt(1, msgid);
            dbInsertReferences.setInt(2, xrefid);
            if (xrefid == 0) {
                dbInsertReferences.setString(3, (String) allRefs.elementAt(i));
            } else {
                dbInsertReferences.setString(3, null);
            }
            if (jdbc2_batch_supported) {
                dbInsertReferences.addBatch();
            } else {
                dbInsertReferences.executeUpdate();
            }
        }
        if (jdbc2_batch_supported) {
            dbInsertReferences.executeBatch();
        }
    }

    private void insertMessage(NNTPThread curThread, int subjid, int fromid) throws SQLException {
        dbInsertMessage.clearParameters();
        dbInsertMessage.setString(1, curThread.getMessageId());
        dbInsertMessage.setString(2, curThread.getDate());
        dbInsertMessage.setInt(3, curThread.getLines());
        dbInsertMessage.setInt(4, curThread.getScore());
        dbInsertMessage.setInt(5, subjid);
        dbInsertMessage.setInt(6, fromid);
        dbInsertMessage.executeUpdate();
    }

    private void insertGroupLink(int msgid, int groupid) throws SQLException {
        dbInsertGroupMsg.clearParameters();
        dbInsertGroupMsg.setInt(1, msgid);
        dbInsertGroupMsg.setInt(2, groupid);
        dbInsertGroupMsg.executeUpdate();
    }

    private int findAdd(PreparedStatement prepQuery, PreparedStatement prepInsert, String searchAdd, String columname) throws SQLException {
        int id = getId(prepQuery, searchAdd, columname);
        if (id == 0) {
            prepInsert.clearParameters();
            prepInsert.setString(1, searchAdd);
            prepInsert.executeUpdate();
            id = getId(prepQuery, searchAdd, columname);
        }
        return (id);
    }

    private int findAddSubject(String subject) throws SQLException {
        return (findAdd(dbSubQuery, dbInsertSubject, subject, "subjectid"));
    }

    private int findAddFrom(String fromAddr) throws SQLException {
        return (findAdd(dbFrmQuery, dbInsertFrom, fromAddr, "fromid"));
    }

    private int findAddNewsgroup(String newsgroup) throws SQLException {
        return (findAdd(dbGrpQuery, dbInsertNewsgroups, newsgroup, "groupid"));
    }
}
