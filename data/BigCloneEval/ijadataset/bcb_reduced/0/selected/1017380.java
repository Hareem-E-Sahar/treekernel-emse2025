package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;
import frost.boards.*;
import frost.gui.*;
import frost.storage.database.*;

public class KnownBoardsDatabaseTable extends AbstractDatabaseTable {

    private static final Logger logger = Logger.getLogger(KnownBoardsDatabaseTable.class.getName());

    private static final String SQL_DDL = "CREATE TABLE IF NOT EXISTS KNOWNBOARDS (" + "primkey BIGINT DEFAULT UNIQUEKEY('KNOWNBOARDS') NOT NULL," + "boardname VARCHAR NOT NULL," + "publickey VARCHAR NOT NULL," + "privatekey VARCHAR NOT NULL," + "description VARCHAR," + "CONSTRAINT kb_pk PRIMARY KEY (primkey)," + "CONSTRAINT KNOWNBOARDS_1 UNIQUE (boardname,publickey,privatekey) )";

    private static final String SQL_DDL2 = "CREATE TABLE IF NOT EXISTS HIDDENBOARDNAMES (boardname VARCHAR NOT NULL)";

    public List<String> getTableDDL() {
        ArrayList<String> lst = new ArrayList<String>(2);
        lst.add(SQL_DDL);
        lst.add(SQL_DDL2);
        return lst;
    }

    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE KNOWNBOARDS");
        stmt.executeUpdate("COMPACT TABLE HIDDENBOARDNAMES");
        return true;
    }

    /**
     * Load all hidden board names.
     */
    public HashSet<String> loadHiddenNames() throws SQLException {
        HashSet<String> names = new HashSet<String>();
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        Statement stmt = db.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT boardname FROM HIDDENBOARDNAMES");
        while (rs.next()) {
            String bName = rs.getString(1);
            names.add(bName);
        }
        rs.close();
        stmt.close();
        return names;
    }

    /**
     * Clear table and save all hidden board names.
     */
    public void saveHiddenNames(HashSet names) throws SQLException {
        Connection conn = AppLayerDatabase.getInstance().getPooledConnection();
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM HIDDENBOARDNAMES");
            stmt.close();
            stmt = null;
            ps = conn.prepareStatement("INSERT INTO HIDDENBOARDNAMES (boardname) VALUES (?)");
            for (Iterator i = names.iterator(); i.hasNext(); ) {
                String bName = (String) i.next();
                ps.setString(1, bName);
                ps.executeUpdate();
            }
            ps.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception during save", t);
            try {
                conn.rollback();
            } catch (Throwable t1) {
                logger.log(Level.SEVERE, "Exception during rollback", t1);
            }
            try {
                conn.setAutoCommit(true);
            } catch (Throwable t1) {
            }
        } finally {
            AppLayerDatabase.getInstance().givePooledConnection(conn);
            try {
                if (ps != null) ps.close();
            } catch (Throwable t1) {
            }
            ;
        }
    }

    private boolean insertKnownBoard(Board board) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepareStatement("INSERT INTO KNOWNBOARDS " + "(boardname,publickey,privatekey,description) VALUES (?,?,?,?)");
        ps.setString(1, board.getName());
        ps.setString(2, (board.getPublicKey() == null ? "" : board.getPublicKey()));
        ps.setString(3, (board.getPrivateKey() == null ? "" : board.getPrivateKey()));
        ps.setString(4, board.getDescription());
        boolean insertWasOk = false;
        try {
            insertWasOk = (ps.executeUpdate() == 1);
        } finally {
            ps.close();
        }
        return insertWasOk;
    }

    /**
     * @return  List of KnownBoard
     */
    public List<KnownBoard> getKnownBoards() throws SQLException {
        LinkedList<KnownBoard> knownBoards = new LinkedList<KnownBoard>();
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepareStatement("SELECT boardname,publickey,privatekey,description FROM KNOWNBOARDS ORDER BY boardname");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String tmp;
            String boardname = rs.getString(1);
            tmp = rs.getString(2);
            String publickey = (tmp.length() == 0 ? null : tmp);
            tmp = rs.getString(3);
            String privatekey = (tmp.length() == 0 ? null : tmp);
            String description = rs.getString(4);
            KnownBoard b = new KnownBoard(boardname, publickey, privatekey, description);
            knownBoards.add(b);
        }
        rs.close();
        ps.close();
        return knownBoards;
    }

    public boolean deleteKnownBoard(Board b) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepareStatement("DELETE FROM KNOWNBOARDS WHERE boardname=? AND publickey=? AND privatekey=?");
        ps.setString(1, b.getName());
        ps.setString(2, (b.getPublicKey() == null ? "" : b.getPublicKey()));
        ps.setString(3, (b.getPrivateKey() == null ? "" : b.getPrivateKey()));
        boolean deleteWasOk = false;
        try {
            deleteWasOk = (ps.executeUpdate() == 1);
        } finally {
            ps.close();
        }
        return deleteWasOk;
    }

    /**
     * Called with a list of Board, should add all boards that are not contained already
     * @param lst  List of Board
     */
    public int addNewKnownBoards(List<Board> lst) {
        if (lst == null || lst.size() == 0) {
            return 0;
        }
        Iterator i = lst.iterator();
        int added = 0;
        while (i.hasNext()) {
            Board newb = (Board) i.next();
            try {
                insertKnownBoard(newb);
                added++;
            } catch (SQLException e) {
            }
        }
        return added;
    }
}
