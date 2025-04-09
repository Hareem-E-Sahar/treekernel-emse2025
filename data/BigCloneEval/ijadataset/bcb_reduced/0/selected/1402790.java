package edu.cmu.cs.bungee.servlet;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import JSci.maths.statistics.ChiSq2x2;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import edu.cmu.cs.bungee.dbScripts.ConvertFromRaw;
import edu.cmu.cs.bungee.javaExtensions.JDBCSample;
import edu.cmu.cs.bungee.javaExtensions.MyResultSet;
import edu.cmu.cs.bungee.javaExtensions.Util;
import edu.cmu.cs.bungee.javaExtensions.MyResultSet.Column;

public class Database {

    private static final int SQL_INT_BITS = 32;

    private String dbName;

    private JDBCSample jdbc;

    private String descriptionGetter;

    public Database(String _server, String _db, String _user, String _pass, GenericServlet _servlet) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, ServletException {
        dbName = _db;
        jdbc = new JDBCSample(_server, _db, _user, _pass, _servlet);
        ConvertFromRaw.ensureDBinitted(jdbc);
        String item_id_column_type = jdbc.unsignedTypeForMaxValue(jdbc.SQLqueryInt("SELECT MAX(record_num) FROM item"));
        String facet_id_column_type = jdbc.unsignedTypeForMaxValue(jdbc.SQLqueryInt("SELECT MAX(facet_id) FROM facet"));
        reorderItems(-1);
        String[] createTempTables = { "CREATE TEMPORARY TABLE if not exists onItems (record_num " + item_id_column_type + ", PRIMARY KEY (record_num)) ENGINE=HEAP " + "PACK_KEYS=1 ROW_FORMAT=FIXED", "CREATE TEMPORARY TABLE if not exists restricted (record_num " + item_id_column_type + ", PRIMARY KEY (record_num)) ENGINE=HEAP " + "PACK_KEYS=1 ROW_FORMAT=FIXED", "CREATE TEMPORARY TABLE if not exists relevantFacets (" + "facet_id " + facet_id_column_type + ", " + "PRIMARY KEY USING BTREE (facet_id)) ENGINE=HEAP " + "PACK_KEYS=1 ROW_FORMAT=FIXED" };
        jdbc.SQLupdate(createTempTables);
        filteredCountQuery = jdbc.lookupPS("SELECT f.facet_id, COUNT(*) AS cnt " + "FROM relevantFacets f " + "INNER JOIN item_facet_heap i_f USING (facet_id) " + "INNER JOIN onItems USING (record_num) " + "GROUP BY f.facet_id ORDER BY f.facet_id");
        filteredCountTypeQuery = jdbc.lookupPS("SELECT f.facet_id, COUNT(*) AS cnt " + "FROM item_facet_type_heap f " + "INNER JOIN onItems USING (record_num) " + "GROUP BY f.facet_id ");
        String[] prefetchFROM = { " FROM facet WHERE parent_facet_id = ? ORDER BY facet_id", " FROM (SELECT facet_id, count(restricted.record_num) AS n_items, n_child_facets, first_child_offset, name" + " FROM facet INNER JOIN item_facet_heap USING (facet_id)" + " LEFT JOIN restricted USING (record_num) WHERE parent_facet_id = ?" + " GROUP BY facet_id) foo ORDER BY facet_id" };
        prefetchQuery = jdbc.lookupPS("SELECT n_items, n_child_facets, name " + prefetchFROM[0]);
        prefetchNoCountQuery = jdbc.lookupPS("SELECT n_child_facets, name" + prefetchFROM[0]);
        prefetchNoNameQuery = jdbc.lookupPS("SELECT n_items, n_child_facets" + prefetchFROM[0]);
        prefetchNoCountNoNameQuery = jdbc.lookupPS("SELECT n_child_facets" + prefetchFROM[0]);
        prefetchQueryRestricted = jdbc.lookupPS("SELECT n_items, n_child_facets, name" + prefetchFROM[1]);
        prefetchNoNameQueryRestricted = jdbc.lookupPS("SELECT n_items, n_child_facets" + prefetchFROM[1]);
        getItemInfoQuery = jdbc.prepareStatement("SELECT parent_facet_id, f.facet_id, name, " + "n_child_facets, first_child_offset, n_items" + " FROM item_facet_heap i INNER JOIN facet f USING (facet_id)" + " WHERE record_num = ? ORDER BY f.facet_id", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        getFacetInfoQuery = jdbc.prepareStatement("SELECT parent_facet_id, facet_id, name, " + "n_child_facets, first_child_offset, n_items" + " FROM facet WHERE facet_id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        getFacetInfoQueryRestricted = jdbc.prepareStatement("SELECT parent_facet_id, facet_id, name, " + "n_child_facets, first_child_offset, COUNT(restricted.record_num)" + " FROM facet LEFT JOIN (item_facet_heap" + " INNER JOIN restricted USING (record_num)) USING (facet_id)" + " WHERE facet_id = ? GROUP BY facet_id", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        printUserActionStmt = jdbc.lookupPS("INSERT INTO user_actions VALUES(NOW(), ?, ?, ?, ?, ?, ?)");
        getLetterOffsetsQuery = jdbc.lookupPS("SELECT MIN(SUBSTRING(name, CHAR_LENGTH(?) + 1, 1)) letter, MAX(facet_id) max_facet " + "FROM facet WHERE parent_facet_id = ? AND name LIKE CONCAT(?, '%') " + "GROUP BY SUBSTRING(name, CHAR_LENGTH(?) + 1, 1) ORDER BY max_facet");
    }

    void close() throws SQLException {
        jdbc.close();
        jdbc = null;
    }

    String aboutCollection() throws SQLException {
        return jdbc.SQLqueryString("SELECT aboutURL FROM globals");
    }

    int facetCount() throws SQLException {
        return jdbc.SQLqueryInt("SELECT MAX(facet_id) FROM facet");
    }

    int itemCount() throws SQLException {
        return jdbc.SQLqueryInt("SELECT COUNT(*) FROM item");
    }

    String[] getGlobals() throws SQLException, ServletException {
        String[] result = null;
        ResultSet rs = null;
        try {
            rs = jdbc.SQLquery("SELECT itemDescriptionFields, genericObjectLabel, itemURL, itemURLdoc, isEditable FROM globals");
            if (!rs.next()) error("Can't get globals");
            String itemDescriptionFields = rs.getString(1);
            String[] fields = itemDescriptionFields.split(",");
            String[] nonNullFields2 = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                nonNullFields2[i] = "IF(" + fields[i] + " IS NULL,'', CONCAT('\n" + i + "\n', " + fields[i] + "))";
            }
            descriptionGetter = "CONCAT(" + Util.join(nonNullFields2) + ")";
            imageQuery = "SELECT CONCAT(" + Util.join(nonNullFields2) + ") descript, image, w, h FROM item LEFT JOIN images " + "ON item.record_num = images.record_num " + "WHERE item.record_num = ";
            String itemURLgetter = rs.getString(3);
            if (itemURLgetter != null && itemURLgetter.length() > 0) {
                itemIdPS = jdbc.lookupPS("SELECT " + itemURLgetter + " FROM item WHERE record_num = ?");
                itemURLPS = jdbc.lookupPS("SELECT record_num FROM item WHERE " + itemURLgetter + " = ?");
            }
            String[] resultx = { itemDescriptionFields, rs.getString(2), rs.getString(4), rs.getString(5) };
            result = resultx;
        } finally {
            if (rs != null) jdbc.close(rs);
        }
        return result;
    }

    private PreparedStatement itemIdPS;

    String getItemURL(int item) throws SQLException, ServletException {
        String result = null;
        if (itemIdPS != null) {
            synchronized (itemIdPS) {
                itemIdPS.setInt(1, item);
                result = jdbc.SQLqueryString(itemIdPS);
                if (result == null) {
                    myAssert(false, "Can't find " + jdbc.SQLqueryString("SELECT itemURL FROM globals") + " for record_num " + item);
                }
            }
        }
        return result;
    }

    String sortedResultTable(int i) throws ServletException {
        switch(i) {
            case 0:
                return "item_order_heap";
            case 1:
                return "restricted";
            case 2:
                return "onItems";
        }
        myAssert(false, "Bad table index: " + i);
        return null;
    }

    /**
	 * Update offsetItemsQuery (used by offsetItems) and itemOffsetQuery (1 and
	 * 2) (used by itemOffset) so they return indexes sorted appropriately.
	 * 
	 * The indexes into these two arrays show which table contains the on items:
	 * 
	 * @see #sortedResultTable
	 * 
	 * @param facetType
	 *            the item_order_heap column to sort by -1 means random, 0 means
	 *            ID, else the facet_type_ID
	 * @throws SQLException
	 * @throws ServletException
	 * 
	 */
    void reorderItems(int facetType) throws SQLException, ServletException {
        String columnToSortBy = facetType < 0 ? "random_ID" : facetType == 0 ? "record_num" : "col" + facetType;
        if (offsetItemsQuery == null) {
            offsetItemsQuery = new PreparedStatement[3];
            itemOffsetQuery1 = new PreparedStatement[offsetItemsQuery.length];
            itemOffsetQuery2 = new PreparedStatement[offsetItemsQuery.length];
        }
        synchronized (offsetItemsQuery) {
            for (int i = 1; i < offsetItemsQuery.length; i++) {
                offsetItemsQuery[i] = jdbc.lookupPS("SELECT o.record_num FROM " + sortedResultTable(i) + " o INNER JOIN item_order_heap r USING (record_num)" + " ORDER BY r." + columnToSortBy + " LIMIT ?, ?");
                itemOffsetQuery1[i] = jdbc.lookupPS("SELECT s." + columnToSortBy + " FROM item_order_heap s INNER JOIN " + sortedResultTable(i) + " USING (record_num) WHERE s.record_num = ?");
                itemOffsetQuery2[i] = jdbc.lookupPS("SELECT COUNT(*) FROM item_order_heap r " + "INNER JOIN " + sortedResultTable(i) + " USING (record_num) WHERE r." + columnToSortBy + " < ?");
            }
            offsetItemsQuery[0] = jdbc.lookupPS("SELECT record_num FROM " + "item_order_heap ORDER BY " + columnToSortBy + " LIMIT ?, ?");
            itemOffsetQuery1[0] = jdbc.lookupPS("SELECT s." + columnToSortBy + " FROM item_order_heap s WHERE s.record_num = ?");
            itemOffsetQuery2[0] = jdbc.lookupPS("SELECT COUNT(*)-1 FROM item_order_heap " + " WHERE " + columnToSortBy + " <= ?");
        }
    }

    private PreparedStatement itemURLPS;

    int getItemFromURL(String URL) throws SQLException {
        int result = 0;
        if (itemURLPS != null) {
            synchronized (itemURLPS) {
                itemURLPS.setString(1, URL);
                result = jdbc.SQLqueryInt(itemURLPS);
            }
        }
        return result;
    }

    void getCountsIgnoringFacet(String subQuery, String facet_id, DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet range = jdbc.SQLquery("SELECT min(facet_id), max(facet_id) from facet where parent_facet_id = " + facet_id);
        range.next();
        int minChild = range.getInt(1);
        int maxChild = range.getInt(2);
        range.close();
        ResultSet rs = jdbc.SQLquery("SELECT i_f.facet_id, COUNT(onItemsFake.record_num) AS cnt " + "FROM item_facet_heap i_f " + "INNER JOIN (" + subQuery + ") onItemsFake USING (record_num) " + "WHERE i_f.facet_id >= " + minChild + " AND i_f.facet_id <= " + maxChild + " GROUP BY i_f.facet_id ORDER BY i_f.facet_id");
        sendResultSet(rs, MyResultSet.SINT_PINT, out);
    }

    private PreparedStatement filteredCountQuery;

    private PreparedStatement filteredCountTypeQuery;

    void getFilteredCounts(String perspectivesToAdd, String perspectivesToRemove, DataOutputStream out) throws SQLException, ServletException, IOException {
        updateRelevantFacets(perspectivesToAdd, perspectivesToRemove);
        synchronized (filteredCountQuery) {
            ResultSet rs = jdbc.SQLquery(filteredCountQuery);
            sendResultSet(rs, MyResultSet.SINT_PINT, out);
        }
    }

    void getFilteredCountTypes(DataOutputStream out) throws SQLException, ServletException, IOException {
        synchronized (filteredCountTypeQuery) {
            ResultSet rs = jdbc.SQLquery(filteredCountTypeQuery);
            sendResultSet(rs, MyResultSet.SINT_PINT, out);
        }
    }

    void updateRelevantFacets(String perspectivesToAdd, String perspectivesToRemove) throws SQLException {
        List<String> SQL = new ArrayList<String>();
        if (perspectivesToAdd != null && perspectivesToAdd.length() > 0) {
            SQL.add(updateRelevantFacetsInternal(perspectivesToAdd, false));
            perspectivesToAdd = null;
        }
        if (perspectivesToRemove != null && perspectivesToRemove.length() > 0) {
            SQL.add(updateRelevantFacetsInternal(perspectivesToRemove, true));
            perspectivesToRemove = null;
        }
        if (SQL.size() > 0) {
            if (SQL.size() == 1) {
                jdbc.SQLupdate(SQL.get(0));
            } else {
                jdbc.SQLupdate(SQL.toArray(new String[0]));
            }
        }
    }

    private static String updateRelevantFacetsInternal(String persps, boolean isDelete) {
        StringBuffer buf = new StringBuffer(persps.length() + 80);
        if (isDelete) buf.append("DELETE FROM relevantFacets USING relevantFacets, facet " + "WHERE relevantFacets.facet_id = facet.facet_id " + "AND parent_facet_id IN ("); else buf.append("REPLACE INTO relevantFacets SELECT facet_id FROM facet WHERE parent_facet_id IN (");
        buf.append(persps);
        buf.append(")");
        return buf.toString();
    }

    void initPerspectives(DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = jdbc.SQLquery("SELECT facet.name, descriptionCategory, descriptionPreposition, " + "n_child_facets, first_child_offset, n_items, isOrdered " + "FROM raw_facet_type ft INNER JOIN facet USING (name) " + "WHERE facet.parent_facet_id = 0 " + "ORDER BY facet.facet_id");
        sendResultSet(rs, MyResultSet.STRING_STRING_STRING_INT_INT_INT_INT, out);
    }

    void init(DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = jdbc.SQLquery("SELECT f.n_items as cnt " + "FROM facet f WHERE f.parent_facet_id > 0 AND f.parent_facet_id <= " + ConvertFromRaw.maxFacetTypeID(jdbc) + " ORDER BY f.facet_ID");
        sendResultSet(rs, MyResultSet.INT, out);
    }

    int updateOnItems(String onSQL) throws SQLException {
        jdbc.SQLupdate("TRUNCATE TABLE onItems");
        return jdbc.SQLupdate("INSERT INTO onItems " + onSQL);
    }

    private PreparedStatement prefetchQuery;

    private PreparedStatement prefetchNoCountQuery;

    private PreparedStatement prefetchNoNameQuery;

    private PreparedStatement prefetchNoCountNoNameQuery;

    private PreparedStatement prefetchQueryRestricted;

    private PreparedStatement prefetchNoNameQueryRestricted;

    void prefetch(int facet_id, int args, DataOutputStream out) throws SQLException, ServletException, IOException {
        PreparedStatement ps1 = jdbc.lookupPS("SELECT first_child_offset, is_alphabetic FROM facet WHERE facet_id = ?");
        ps1.setInt(1, facet_id);
        ResultSet rs1 = jdbc.SQLquery(ps1);
        myAssert(MyResultSet.nRows(rs1) == 1, "Bad nRows: " + facet_id + " " + MyResultSet.nRows(rs1));
        rs1.next();
        int children_offset = rs1.getInt(1);
        writeInt(children_offset, out);
        int isAlphabetic = rs1.getInt(2);
        PreparedStatement ps;
        List<Object> types;
        switch(args) {
            case 1:
                ps = prefetchQuery;
                types = MyResultSet.INT_INT_STRING;
                break;
            case 2:
                ps = prefetchNoNameQuery;
                types = MyResultSet.INT_INT;
                break;
            case 3:
                ps = prefetchNoCountQuery;
                types = MyResultSet.INT_STRING;
                break;
            case 4:
                ps = prefetchNoCountNoNameQuery;
                types = MyResultSet.INT;
                break;
            case 5:
                ps = prefetchQueryRestricted;
                types = MyResultSet.INT_INT_STRING;
                break;
            default:
                myAssert(args == 6, "prefetch args=" + args);
                ps = prefetchNoNameQueryRestricted;
                types = MyResultSet.INT_INT;
                break;
        }
        synchronized (ps) {
            ps.setInt(1, facet_id);
            ResultSet rs = jdbc.SQLquery(ps);
            sendResultSet(rs, types, out);
        }
        writeInt(isAlphabetic, out);
    }

    private PreparedStatement getLetterOffsetsQuery;

    void getLetterOffsets(int parentFacetID, String prefix, DataOutputStream out) throws SQLException, IOException {
        synchronized (getLetterOffsetsQuery) {
            getLetterOffsetsQuery.setString(1, prefix);
            getLetterOffsetsQuery.setInt(2, parentFacetID);
            getLetterOffsetsQuery.setString(3, prefix);
            getLetterOffsetsQuery.setString(4, prefix);
            ResultSet rs = jdbc.SQLquery(getLetterOffsetsQuery);
            if (MyResultSet.nRows(rs) == 0) {
                log("Found no offsets for prefix '" + prefix + "' among children of " + parentFacetID);
            }
            try {
                sendResultSet(rs, MyResultSet.STRING_SINT, out);
            } catch (ServletException e) {
                log("Exception in getLetterOffsets - probably mis-alphabetized facet names: '" + prefix + "' " + e);
            }
        }
    }

    void getNames(String facets, DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = jdbc.SQLquery("SELECT name FROM facet WHERE facet_id IN(" + facets + ") ORDER BY facet_id");
        sendResultSet(rs, MyResultSet.STRING, out);
    }

    /**
	 * Return the recordNum's for a range of offsets.
	 */
    private PreparedStatement[] offsetItemsQuery;

    void offsetItems(int minOffset, int maxOffset, int table, DataOutputStream out) throws SQLException, ServletException, IOException {
        synchronized (offsetItemsQuery) {
            int nRows = maxOffset - minOffset;
            PreparedStatement s = offsetItemsQuery[table];
            s.setInt(1, minOffset);
            s.setInt(2, nRows);
            ResultSet rs = jdbc.SQLquery(s);
            if (MyResultSet.nRows(rs) != nRows) {
                int onCount = jdbc.SQLqueryInt("SELECT COUNT(*) FROM " + sortedResultTable(table));
                myAssert(onCount < maxOffset, minOffset + "-" + nRows + " " + MyResultSet.nRows(rs) + " " + onCount + " in " + sortedResultTable(table) + "\n" + s);
            }
            sendResultSet(rs, MyResultSet.INT, out);
        }
    }

    void getThumbs(String items, int imageW, int imageH, int quality, DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = jdbc.SQLquery("SELECT item.record_num, " + descriptionGetter + " description, image, w, h FROM item LEFT JOIN images USING (record_num)" + " WHERE item.record_num IN (" + items + ") ORDER BY item.record_num");
        sendResultSet(rs, MyResultSet.SINT_STRING_IMAGE_INT_INT, imageW, imageH, quality, out);
        rs = jdbc.SQLquery("SELECT * FROM item_facetNtype_heap WHERE record_num IN(" + items + ") ORDER BY record_num");
        sendResultSet(rs, MyResultSet.SNMINT_PINT, out);
    }

    private String imageQuery;

    private PreparedStatement getItemInfoQuery;

    /**
	 * @param item
	 * @param desiredImageW
	 *            -1 means don't retrieve an image
	 * @param desiredImageH
	 * @param quality
	 * @param out
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    void getDescAndImage(int item, int desiredImageW, int desiredImageH, int quality, DataOutputStream out) throws SQLException, ServletException, IOException {
        synchronized (getItemInfoQuery) {
            getItemInfoQuery.setInt(1, item);
            ResultSet rs = jdbc.SQLquery(getItemInfoQuery);
            sendResultSet(rs, MyResultSet.PINT_SINT_STRING_INT_INT_INT, out);
        }
        ResultSet rs = jdbc.SQLquery(imageQuery + item);
        myAssert(MyResultSet.nRows(rs) == 1, "Non-unique result for " + imageQuery + item);
        sendResultSet(rs, MyResultSet.STRING_IMAGE_INT_INT, desiredImageW, desiredImageH, quality, out);
    }

    private PreparedStatement getFacetInfoQuery;

    private PreparedStatement getFacetInfoQueryRestricted;

    void getFacetInfo(int facet, boolean isRestrictedData, DataOutputStream out) throws SQLException, ServletException, IOException {
        PreparedStatement ps = isRestrictedData ? getFacetInfoQueryRestricted : getFacetInfoQuery;
        synchronized (ps) {
            ps.setInt(1, facet);
            ResultSet rs = jdbc.SQLquery(ps);
            sendResultSet(rs, MyResultSet.PINT_SINT_STRING_INT_INT_INT, out);
        }
    }

    /**
	 * Return the ordinal for a single Item.
	 */
    private PreparedStatement[] itemOffsetQuery1;

    /**
	 * Return the offset for that ordinal.
	 */
    private PreparedStatement[] itemOffsetQuery2;

    /**
	 * @param item
	 * @param table
	 *            see reorderItems
	 * @return the offset into the on items table of this item. -1 means not
	 *         found.
	 * @throws SQLException
	 */
    int itemOffset(int item, int table) throws SQLException {
        int offset = -1;
        PreparedStatement s1 = itemOffsetQuery1[table];
        synchronized (s1) {
            s1.setInt(1, item);
            int ordinal = jdbc.SQLqueryInt(s1);
            if (ordinal > 0) {
                PreparedStatement s2 = itemOffsetQuery2[table];
                s2.setInt(1, ordinal);
                offset = jdbc.SQLqueryInt(s2);
            }
        }
        return offset;
    }

    private boolean pairCountTablesCreated = false;

    /**
	 * Given facetIDs="1,2,3" return for each candidate the counts in the 2^3-1
	 * combinations where the candidate is on and at least one other is on. From
	 * that you can reconstruct the 2^4 states from the 2^3 base states and the
	 * candidate's totalCount.
	 * 
	 * @param facetSpecs
	 *            list of facets, or facet ranges, to find co-occurence counts
	 *            among: [f1, f2, f3-f4, f5, ...]
	 * @param isRestrictedData
	 * @param out
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    void onCountMatrix(String facetSpecs, String candidates, boolean isRestrictedData, boolean needBaseCounts, DataOutputStream out) throws SQLException, ServletException, IOException {
        String[] IDs = Util.splitComma(facetSpecs);
        myAssert(IDs.length < SQL_INT_BITS, "Too many facets (" + IDs.length + "): " + facetSpecs);
        if (!pairCountTablesCreated) {
            pairCountTablesCreated = true;
            jdbc.SQLupdate("DROP TABLE IF EXISTS tetrad_facets");
            jdbc.SQLupdate("CREATE TEMPORARY TABLE tetrad_facets(" + "facet_id MEDIUMINT(8) UNSIGNED NOT NULL, " + "bit INT(8) UNSIGNED NOT NULL, " + "PRIMARY KEY (facet_id)) ENGINE=MEMORY");
            jdbc.SQLupdate("CREATE TEMPORARY TABLE tetrad_items(" + "record_num MEDIUMINT(8) UNSIGNED NOT NULL, " + "state INT(8) UNSIGNED NOT NULL, " + "PRIMARY KEY (record_num)) ENGINE=MEMORY");
        } else {
            jdbc.SQLupdate("TRUNCATE TABLE tetrad_facets");
            jdbc.SQLupdate("TRUNCATE TABLE tetrad_items");
        }
        StringBuffer buf = new StringBuffer();
        buf.append("INSERT INTO tetrad_facets VALUES");
        for (int i = 0; i < IDs.length; i++) {
            String[] mexFacets = IDs[i].split("-");
            for (int j = 0; j < mexFacets.length; j++) {
                if (i + j > 0) buf.append(",");
                buf.append("(").append(mexFacets[j]).append(", ").append((1 << i)).append(")");
            }
        }
        jdbc.SQLupdate(buf.toString());
        jdbc.SQLupdate("INSERT INTO tetrad_items " + "SELECT record_num rn, SUM(bit) FROM tetrad_facets f " + "INNER JOIN item_facetNtype_heap ifh USING (facet_id) " + (isRestrictedData ? "INNER JOIN restricted USING (record_num) " : "") + "GROUP BY rn ORDER BY NULL");
        if (needBaseCounts) {
            ResultSet baseCounts = jdbc.SQLquery("SELECT SQL_SMALL_RESULT 0, state, COUNT(*) FROM tetrad_items " + "GROUP BY state ORDER BY NULL");
            sendResultSet(baseCounts, MyResultSet.SNMINT_INT_INT, out);
        }
        if (candidates.length() > 0) {
            ResultSet candidateCounts = jdbc.SQLquery("SELECT SQL_SMALL_RESULT facet_id, state s, COUNT(*) " + "FROM item_facetNtype_heap INNER JOIN tetrad_items USING (record_num) " + "WHERE facet_id IN (" + candidates + ") GROUP BY facet_id, s ORDER BY facet_id");
            sendResultSet(candidateCounts, MyResultSet.SNMINT_INT_INT, out);
        }
    }

    void topCandidates(String perspectiveIDexpr, int n, int table, DataOutputStream out) throws SQLException, ServletException, IOException {
        myAssert(table != 1, "topCandidates needs a second case for restricted queries.");
        ConvertFromRaw.populateCorrelations(jdbc);
        String sql = "SELECT candidateID FROM (" + "SELECT facet1 candidateID, abs(correlation) corr " + "FROM correlations WHERE " + perspectiveIDexpr.replace("?", "facet2") + " UNION ALL " + "SELECT facet2 candidateID, abs(correlation) corr " + "FROM correlations WHERE " + perspectiveIDexpr.replace("?", "facet1") + ") foo GROUP BY candidateID " + "HAVING NOT (" + perspectiveIDexpr.replace("?", "candidateID") + ") ORDER BY POW(SUM(corr),2) - SUM(corr*corr) DESC LIMIT " + n;
        ResultSet rs = jdbc.SQLquery(sql);
        sendResultSet(rs, MyResultSet.INT, out);
    }

    private PreparedStatement printUserActionStmt;

    void printUserAction(String client, int session, int actionIndex, int location, String object, int modifiers) throws SQLException {
        synchronized (printUserActionStmt) {
            printUserActionStmt.setInt(1, actionIndex);
            printUserActionStmt.setInt(2, location);
            printUserActionStmt.setString(3, object);
            printUserActionStmt.setInt(4, modifiers);
            printUserActionStmt.setInt(5, session);
            printUserActionStmt.setString(6, client);
            jdbc.SQLupdate(printUserActionStmt);
        }
    }

    void restrict() throws SQLException {
        jdbc.SQLupdate("TRUNCATE TABLE restricted");
        jdbc.SQLupdate("INSERT INTO restricted SELECT * FROM onItems");
    }

    private boolean facetTableMucked = false;

    private void muckWithFacetTable() throws SQLException {
        jdbc.SQLupdate("CREATE TEMPORARY TABLE IF NOT EXISTS renames" + " (old_facet_id INT, facet_id INT, PRIMARY KEY (facet_id))");
        if (!facetTableMucked) {
            facetTableMucked = true;
            jdbc.SQLupdate("DROP TABLE IF EXISTS facet_map");
            jdbc.SQLupdate("CREATE TEMPORARY TABLE facet_map AS" + " (SELECT facet_id facet, parent_facet_id raw_facet, sort FROM raw_facet LIMIT 1)");
            jdbc.SQLupdate("TRUNCATE TABLE facet_map");
            jdbc.SQLupdate("INSERT INTO facet_map (SELECT f.facet_id facet, rf.facet_type_id raw_facet, rf.sort" + " FROM facet f, raw_facet_type rf" + " WHERE f.parent_facet_id = 0 AND f.name=rf.name)");
            ResultSet rs = jdbc.SQLquery("SELECT facet, raw_facet FROM facet_map");
            while (rs.next()) {
                int facet = rs.getInt(1);
                int raw_facet = rs.getInt(2);
                ensureFacetMapInternal(facet, raw_facet);
            }
            jdbc.SQLupdate("ALTER TABLE facet_map ADD PRIMARY KEY (facet)");
        }
    }

    private void ensureFacetMapInternal(int parent, int raw_parent) throws SQLException {
        PreparedStatement insertPS = jdbc.lookupPS("INSERT INTO facet_map (SELECT f.facet_id facet, rf.facet_id raw_facet, rf.sort" + " FROM facet f, raw_facet rf" + " WHERE f.parent_facet_id = ? AND rf.parent_facet_id = ? AND f.name = rf.name)");
        insertPS.setInt(1, parent);
        insertPS.setInt(2, raw_parent);
        jdbc.SQLupdate(insertPS);
        PreparedStatement findPS = jdbc.lookupPS("SELECT f.facet_id facet, rf.facet_id raw_facet" + " FROM facet f, raw_facet rf" + " WHERE f.parent_facet_id = ? AND rf.parent_facet_id = ? AND f.name = rf.name" + " AND f.n_child_facets > 0");
        findPS.setInt(1, parent);
        findPS.setInt(2, raw_parent);
        ResultSet rs = jdbc.SQLquery(findPS);
        int[][] rsCache = new int[MyResultSet.nRows(rs)][];
        int i = 0;
        while (rs.next()) {
            int facet = rs.getInt(1);
            int raw_facet = rs.getInt(2);
            int[] temp = { facet, raw_facet };
            rsCache[i++] = temp;
        }
        for (int j = 0; j < rsCache.length; j++) {
            int[] is = rsCache[j];
            ensureFacetMapInternal(is[0], is[1]);
        }
    }

    private void changeID(int oldID, int newID) throws SQLException {
        PreparedStatement ps = jdbc.lookupPS("UPDATE facet_map SET facet = ? WHERE facet = ?");
        ps.setInt(1, newID);
        ps.setInt(2, oldID);
        jdbc.SQLupdate(ps);
        ps = jdbc.lookupPS("INSERT INTO renames VALUES(?, ?)");
        ps.setInt(1, oldID);
        ps.setInt(2, newID);
        jdbc.SQLupdate(ps);
        ps = jdbc.lookupPS("UPDATE item_facet_heap SET facet_id = ? WHERE facet_id = ?");
        ps.setInt(1, newID);
        ps.setInt(2, oldID);
        jdbc.SQLupdate(ps);
        ps = jdbc.lookupPS("UPDATE facet SET facet_id = ? WHERE facet_id = ?");
        ps.setInt(1, newID);
        ps.setInt(2, oldID);
        jdbc.SQLupdate(ps);
        ps = jdbc.lookupPS("UPDATE facet SET parent_facet_id = ? WHERE parent_facet_id = ?");
        ps.setInt(1, newID);
        ps.setInt(2, oldID);
        jdbc.SQLupdate(ps);
    }

    /**
	 * Add facet and all its ancestors to item.
	 * 
	 * @param facet
	 * @param item
	 * @param out
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    void addItemFacet(int facet, int item, DataOutputStream out) throws SQLException, ServletException, IOException {
        checkNonNegative(item);
        addItemsFacet(facet, "SELECT " + item + " record_num", out);
    }

    /**
	 * Add facet to all query results (as stored in table)
	 * 
	 * @param facet
	 * @param table
	 * @param out
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    void addItemsFacet(int facet, int table, DataOutputStream out) throws SQLException, ServletException, IOException {
        String tableName = sortedResultTable(table);
        addItemsFacet(facet, "select record_num from " + tableName, out);
    }

    private void addItemsFacet(int facet, String itemExpr, DataOutputStream out) throws SQLException, ServletException, IOException {
        checkPositive(facet);
        for (int ancestor = facet; ancestor > 0; ancestor = parentFacetID(ancestor)) {
            String heapTable = parentFacetID(ancestor) > 0 ? "item_facet_heap" : "item_facet_type_heap";
            jdbc.SQLupdate("REPLACE INTO " + heapTable + " SELECT record_num, " + ancestor + " FROM (" + itemExpr + ") foo");
        }
        updateFacetCounts(facet, out);
    }

    Set<Integer> removeItemsFacet(int facet, int table, DataOutputStream out) throws SQLException, ServletException, IOException {
        checkPositive(facet);
        String tableName = sortedResultTable(table);
        jdbc.SQLupdate("DELETE FROM ifh USING item_facet_heap ifh, " + tableName + " oi " + "WHERE ifh.record_num = oi.record_num AND ifh.facet_id = " + facet);
        ResultSet rs = jdbc.SQLquery("SELECT facet_id FROM facet WHERE parent_facet_id = " + facet);
        Set<Integer> result = new HashSet<Integer>();
        boolean hasChildren = false;
        while (rs.next()) {
            hasChildren = true;
            result.addAll(removeItemsFacet(rs.getInt(1), table, null));
        }
        if (!hasChildren) {
            result.add(facet);
        }
        int grandparent = jdbc.SQLqueryInt("SELECT parent.parent_facet_id FROM facet f " + "INNER JOIN facet parent ON f.parent_facet_id = parent.facet_id " + "WHERE f.facet_id = " + facet);
        checkNonNegative(grandparent);
        if (grandparent == 0) {
            int parent = parentFacetID(facet);
            checkPositive(parent);
            jdbc.SQLupdate("DELETE FROM ifh USING item_facet_type_heap ifh, " + tableName + " oi " + "WHERE ifh.record_num = oi.record_num AND ifh.facet_id = " + parent);
        }
        if (out != null) updateFacetCounts(result, out);
        return result;
    }

    private int parentFacetID(int facet) throws SQLException {
        return jdbc.SQLqueryInt("SELECT parent_facet_id FROM facet WHERE facet_id = " + facet);
    }

    Set<Integer> removeItemFacet(int facet, int item, DataOutputStream out) throws SQLException, ServletException, IOException {
        checkPositive(facet);
        checkPositive(item);
        jdbc.SQLupdate("DELETE FROM item_facet_heap WHERE record_num = " + item + " AND facet_id = " + facet);
        ResultSet rs = jdbc.SQLquery("SELECT facet_id FROM facet WHERE parent_facet_id = " + facet);
        Set<Integer> result = new HashSet<Integer>();
        boolean hasChildren = false;
        while (rs.next()) {
            hasChildren = true;
            result.addAll(removeItemFacet(rs.getInt(1), item, null));
        }
        if (!hasChildren) {
            result.add(facet);
        }
        int grandparent = jdbc.SQLqueryInt("SELECT parent.parent_facet_id FROM facet f " + "INNER JOIN facet parent ON f.parent_facet_id = parent.facet_id " + "WHERE f.facet_id = " + facet);
        checkNonNegative(grandparent);
        if (grandparent == 0) {
            int parent = parentFacetID(facet);
            checkPositive(parent);
            jdbc.SQLupdate("DELETE FROM item_facet_type_heap WHERE record_num = " + item + " AND facet_id = " + parent);
        }
        if (out != null) updateFacetCounts(result, out);
        return result;
    }

    void addChildFacet(int parent, String name, DataOutputStream out) throws SQLException, ServletException, IOException {
        checkNonNegative(parent);
        int child;
        child = jdbc.SQLqueryInt("SELECT MAX(facet_id) + 1 FROM facet");
        checkPositive(child);
        jdbc.SQLupdate("INSERT INTO facet VALUES(" + child + ", '" + name + "', " + parent + ", 1, 0, 0, 1)");
        if (parent > 0) {
            addItemFacet(child, 0, null);
            renumber(parent);
            child = jdbc.SQLqueryInt("SELECT facet_id FROM renames WHERE old_facet_id = " + child);
            checkPositive(child);
            updateFacetCounts(child, out);
        } else {
            int order = jdbc.SQLqueryInt("SELECT MAX(sort) + 1 FROM raw_facet_type");
            int type = jdbc.SQLqueryInt("SELECT MAX(facet_type_id) + 1 FROM raw_facet_type");
            myAssert(order > 0 && order < 127, "Bad order " + order);
            jdbc.SQLupdate("INSERT INTO raw_facet_type VALUES(" + type + ", '" + name + "', 'content', ' that show ; that don\\'t show ', " + order + ", 0, 1)");
            addChildFacet(child, "dummy", out);
        }
    }

    /**
	 * Does not remove instances from old parent, because they might be there
	 * for a different reason.
	 * 
	 * @param parent
	 * @param child
	 * @param out
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    void reparent(int parent, int child, DataOutputStream out) throws SQLException, ServletException, IOException {
        checkNonNegative(parent);
        checkPositive(child);
        int oldParent = parentFacetID(child);
        jdbc.SQLupdate("UPDATE facet SET parent_facet_id = " + parent + " WHERE facet_id = " + child);
        addItemsFacet(parent, "SELECT record_num FROM item_facet_heap WHERE facet_id = " + child, null);
        renumber(oldParent);
        renumber(parent);
        child = jdbc.SQLqueryInt("SELECT facet_id FROM renames WHERE old_facet_id = " + child);
        Set<Integer> leafs = new HashSet<Integer>();
        leafs.add(oldParent);
        leafs.add(child);
        updateFacetCounts(leafs, out);
    }

    /**
	 * Renumber parent's scattered and/or unordered children by moving them to a
	 * fresh set of IDs greater than all existing IDs and sorting by name (since
	 * we don't know sort). Recurse on each child.
	 * 
	 * @param parent
	 * @throws SQLException
	 * @throws ServletException
	 */
    private void renumber(int parent) throws SQLException, ServletException {
        boolean relevant = jdbc.SQLqueryInt("SELECT f.facet_id FROM relevantFacets " + "INNER JOIN facet f USING (facet_id) WHERE parent_facet_id = " + parent + " LIMIT 1") > 0;
        if (relevant) updateRelevantFacets(null, Integer.toString(parent));
        muckWithFacetTable();
        int firstChildID = jdbc.SQLqueryInt("SELECT MAX(facet_id) + 1 FROM facet");
        jdbc.SQLupdate("UPDATE facet SET first_child_offset = " + (firstChildID - 1) + " WHERE facet_id = " + parent);
        PreparedStatement ps = jdbc.lookupPS("SELECT facet_id FROM facet" + " WHERE parent_facet_id = ? ORDER BY name");
        ps.setInt(1, parent);
        ResultSet rs = null;
        int newChildID = firstChildID;
        try {
            rs = jdbc.SQLquery(ps);
            while (rs.next()) {
                int oldChildID = rs.getInt(1);
                changeID(oldChildID, newChildID++);
            }
        } finally {
            rs.close();
        }
        if (relevant) updateRelevantFacets(Integer.toString(parent), null);
        for (int child = firstChildID; child < newChildID; child++) {
            renumber(child);
        }
    }

    private void updateFacetCounts(int facet, DataOutputStream out) throws SQLException, ServletException, IOException {
        Set<Integer> leafs = new HashSet<Integer>();
        leafs.add(facet);
        updateFacetCounts(leafs, out);
    }

    /**
	 * Recompute facet counts for these leafFacets and all their ancestors. Add
	 * a dummy instance if there are no real instances yet.
	 * 
	 * @param leafFacets
	 * @param out
	 *            for all renamed facets and their ancestors, write:
	 * 
	 *            facet_id, old_facet_id, n_items, first_child_offset,
	 *            parent_facet_id
	 * 
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    private void updateFacetCounts(Set<Integer> leafFacets, DataOutputStream out) throws SQLException, ServletException, IOException {
        int[] ancestors = null;
        for (Iterator<Integer> it = leafFacets.iterator(); it.hasNext(); ) {
            int ancestor = it.next().intValue();
            do {
                int nChildren = jdbc.SQLqueryInt("SELECT COUNT(*) FROM facet WHERE parent_facet_id = " + ancestor);
                int nItems = 0;
                int parent = parentFacetID(ancestor);
                if (parent > 0) {
                    nItems = jdbc.SQLqueryInt("SELECT COUNT(*) FROM item_facet_heap WHERE facet_id = " + ancestor);
                } else {
                    nItems = jdbc.SQLqueryInt("SELECT COUNT(DISTINCT record_num) FROM item_facet_heap ifh " + "INNER JOIN facet f USING (facet_id) WHERE parent_facet_id = " + ancestor);
                }
                if (nItems == 0) {
                    addItemFacet(ancestor, 0, null);
                    nItems = 1;
                }
                jdbc.SQLupdate("UPDATE facet f SET n_items = " + nItems + ", " + "n_child_facets = " + nChildren + " WHERE facet_id = " + ancestor);
                ancestors = Util.push(ancestors, ancestor);
                ancestor = parent;
            } while (ancestor > 0 && !Util.isMember(ancestors, ancestor));
        }
        if (out != null) {
            muckWithFacetTable();
            String renamed = jdbc.SQLqueryString("SELECT GROUP_CONCAT(facet_id) FROM renames");
            String updated = Util.join(ancestors) + ", " + renamed;
            ResultSet rs = jdbc.SQLquery("SELECT f.facet_id facet_id, IFNULL(old_facet_id, f.facet_id) old, n_items, " + "first_child_offset, parent_facet_id " + "FROM facet f LEFT JOIN renames r USING (facet_id) " + "WHERE f.facet_id IN (" + updated + ") ORDER BY f.facet_id");
            sendResultSet(rs, MyResultSet.SINT_INT_INT_INT_INT, out);
            jdbc.SQLupdate("DROP TABLE renames");
        }
    }

    void writeBack() throws SQLException {
        if (facetTableMucked) {
            int delta = jdbc.SQLqueryInt("SELECT MAX(facet_id) FROM facet");
            updateFacetIDs(delta);
            recomputeRawFacetType(delta);
            recomputeRawFacet();
            ConvertFromRaw converter = new ConvertFromRaw(jdbc);
            converter.findBrokenLinks(true, 1);
            converter.convert();
        }
    }

    /**
	 * Renumber non-top-level facet.facet_id's to leave a big gap after the
	 * top-level ones
	 * 
	 * @param delta
	 * @throws SQLException
	 */
    private void updateFacetIDs(int delta) throws SQLException {
        jdbc.SQLupdate("UPDATE facet f, facet parent SET f.parent_facet_id = f.parent_facet_id + " + delta + " WHERE f.parent_facet_id = parent.facet_id " + "AND parent.parent_facet_id > 0");
        jdbc.SQLupdate("UPDATE facet SET facet_id = facet_id + " + delta + " WHERE parent_facet_id > 0");
        jdbc.SQLupdate("TRUNCATE TABLE item_facet");
        jdbc.SQLupdate("INSERT INTO item_facet SELECT * FROM item_facet_heap");
        jdbc.SQLupdate("UPDATE item_facet SET facet_id = facet_id + " + delta);
        jdbc.SQLupdate("DELETE FROM item_facet WHERE record_num = 0");
    }

    private void recomputeRawFacetType(int delta) throws SQLException {
        jdbc.SQLupdate("DROP TABLE IF EXISTS rft");
        jdbc.SQLupdate("CREATE TEMPORARY TABLE rft AS" + " SELECT IFNULL(f.facet_id, -1) oldID, COUNT(*) ID, r.name, " + "r.descriptionCategory, r.descriptionPreposition, r.sort, r.isOrdered " + "FROM raw_facet_type r LEFT JOIN facet f USING (name) " + "INNER JOIN raw_facet_type prev ON prev.name <= r.name " + "WHERE f.parent_facet_id = 0 OR f.parent_facet_id IS NULL " + "GROUP BY r.name ORDER BY null");
        jdbc.SQLupdate("UPDATE facet f, rft SET f.parent_facet_id = rft.ID + " + (2 * delta) + " WHERE f.parent_facet_id = rft.oldID");
        jdbc.SQLupdate("UPDATE facet SET parent_facet_id = parent_facet_id - " + (2 * delta) + " WHERE parent_facet_id > " + (2 * delta));
        jdbc.SQLupdate("UPDATE facet f, rft SET f.facet_id = rft.ID + " + (2 * delta) + " WHERE f.facet_id = rft.oldID");
        jdbc.SQLupdate("UPDATE facet SET facet_id = facet_id - " + (2 * delta) + " WHERE facet_id > " + (2 * delta));
        jdbc.SQLupdate("TRUNCATE TABLE raw_facet_type");
        jdbc.SQLupdate("INSERT INTO raw_facet_type SELECT ID, name, " + "descriptionCategory, descriptionPreposition, sort, isOrdered " + "FROM rft");
        jdbc.SQLupdate("DROP TABLE rft");
    }

    private void recomputeRawFacet() throws SQLException {
        jdbc.SQLupdate("TRUNCATE TABLE raw_item_facet");
        jdbc.SQLupdate("INSERT INTO raw_item_facet SELECT * FROM item_facet");
        jdbc.SQLupdate("TRUNCATE TABLE raw_facet");
        jdbc.SQLupdate("INSERT INTO raw_facet SELECT facet_id, name, parent_facet_id, sort FROM facet" + " LEFT JOIN facet_map ON facet = facet_id " + "WHERE parent_facet_id > 0");
    }

    void revert(String dateString) throws SQLException, ServletException {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(dateString);
            File dataDir = new File(jdbc.SQLqueryString("SELECT @@datadir"));
            File dbDir = new File(dataDir, dbName);
            File MySQLdir = dataDir.getParentFile();
            File bkpDir = new File(new File(MySQLdir, "dataBKP"), dbName);
            File restoreFile = new File(bkpDir, "restoreDates.txt");
            String restoreFileString = Util.readFile(restoreFile);
            String[] restoreDates = restoreFileString.split("\n");
            restoreFileString += "\n" + format.format(new Date());
            Util.writeFile(restoreFile, restoreFileString);
            Date from = null;
            for (int i = 0; i < restoreDates.length; i++) {
                Date restored = format.parse(restoreDates[i]);
                if (!restored.after(date)) from = restored; else if (from == null) myAssert(false, "Attempt to revert to " + date + ", which predates the full backup of " + restored); else break;
            }
            StringBuffer logFiles = new StringBuffer();
            File[] logFileNames = dataDir.listFiles(Util.getFilenameFilter(".*bin\\.\\d+"));
            for (int i = 0; i < logFileNames.length; i++) {
                File file = logFileNames[i];
                if (new Date(file.lastModified()).after(from)) logFiles.append(" ").append(file.getName());
            }
            exec("net stop \"MySQL\"");
            String[] corruptTables = { "facet", "raw_facet", "raw_facet_type", "raw_item_facet", "globals", "item", "item_facet", "item_facet_heap", "item_facet_type_heap" };
            for (int i = 0; i < corruptTables.length; i++) {
                exec("del /S /Q \"" + corruptTables[i] + ".*", dbDir);
                exec("copy \"" + bkpDir + "\\" + corruptTables[i] + ".* .", dbDir);
            }
            exec("net start \"MySQL\"");
            Pattern p = Pattern.compile(".*user=(.+?)&.*password=(.+?)&");
            Matcher m = p.matcher(jdbc.toString());
            m.find();
            String user = m.group(1);
            String pass = m.group(2);
            exec("..\\bin\\mysqlbinlog --database=" + dbName + " --start-date=\"" + format.format(from) + "\" --stop-date=\"" + format.format(date) + "\"" + logFiles + " | ..\\bin\\mysql -u " + user + " -p" + pass, dataDir);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exec(String command) throws IOException {
        exec(command, null);
    }

    private void exec(String command, File workingDirectory) throws IOException {
        log(command);
        Process proc = Runtime.getRuntime().exec("cmd.exe /C " + command, null, workingDirectory);
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        String line;
        while ((line = bufferedreader.readLine()) != null) {
            log(line);
        }
        try {
            if (proc.waitFor() != 0) {
                log("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            log(e.toString());
        }
    }

    void rotate(int item, int clockwiseDegrees) throws SQLException, IOException, ServletException {
        ResultSet rs = jdbc.SQLquery("SELECT image, URI FROM images WHERE record_num = " + item);
        if (rs.next()) {
            InputStream in = rs.getBlob(1).getBinaryStream();
            BufferedImage im = Util.readCompatibleImage(in);
            BufferedImage rot = Util.rotate(im, Math.toRadians(clockwiseDegrees));
            int w = rot.getWidth();
            int h = rot.getHeight();
            Util.writeImage(rot, 85, "C:\\temp\\temp.jpg");
            jdbc.SQLupdate("UPDATE images SET image = LOAD_FILE('C:\\\\temp\\\\temp.jpg'), w = " + w + ", h = " + h + "WHERE record_num = " + item);
            String filename = rs.getString(2);
            myAssert(filename.endsWith(".jpg"), "rotate filname=" + filename);
            File f = new File(filename);
            BufferedImage im2 = Util.read(f.toURI().toURL());
            f.renameTo(new File(filename.substring(0, filename.length() - 4) + "_unrotated.jpg"));
            BufferedImage rot2 = Util.rotate(im2, Math.toRadians(clockwiseDegrees));
            Util.writeImage(rot2, 85, filename);
        }
    }

    void rename(int facet, String name) throws SQLException {
        if (jdbc.SQLqueryInt("SELECT parent_facet_id FROM facet WHERE facet_id = " + facet) == 0) {
            String oldName = jdbc.SQLqueryString("SELECT name FROM facet WHERE facet_id = " + facet);
            jdbc.SQLupdate("UPDATE raw_facet_type SET name = '" + name + "' WHERE name = '" + oldName + "'");
        }
        jdbc.SQLupdate("UPDATE facet SET name = '" + name + "' WHERE facet_id = " + facet);
    }

    void cluster(int maxClusters, int maxClusterSize, String facetRestriction, double pValue, DataOutputStream out) throws SQLException, ServletException, IOException {
        if (maxClusterSize > 3) maxClusterSize = 3;
        ConvertFromRaw.populatePairs(jdbc);
        String[] clusterTables = clusterTables(maxClusterSize);
        createClusterTables(clusterTables);
        for (int n = 1; n <= maxClusterSize; n++) {
            pValue = addClusters(n, pValue, maxClusters, facetRestriction, clusterTables);
        }
        extractClustersFromTables(maxClusters, clusterTables, out);
    }

    void createClusterTables(String[] clusterTables) throws SQLException {
        jdbc.SQLupdate("CREATE TEMPORARY TABLE IF NOT EXISTS clusterInfo " + "(cluster_id MEDIUMINT UNSIGNED NOT NULL PRIMARY KEY, " + "nFacets TINYINT UNSIGNED NOT NULL, " + "nOn MEDIUMINT UNSIGNED NOT NULL, " + "nTotal MEDIUMINT UNSIGNED NOT NULL, " + "pValue FLOAT NOT NULL) " + "PACK_KEYS=1 ROW_FORMAT=FIXED;");
        jdbc.SQLupdate("TRUNCATE TABLE clusterInfo");
        jdbc.SQLupdate("CREATE TEMPORARY TABLE IF NOT EXISTS clusterFacetsScratch " + "(cluster_id MEDIUMINT UNSIGNED NOT NULL, " + "facet_id MEDIUMINT UNSIGNED NOT NULL, " + "facet_index TINYINT NOT NULL, " + "INDEX facet_index (facet_index), " + "INDEX cluster (cluster_id), " + "INDEX facet (facet_id, facet_index), " + "PRIMARY KEY (cluster_id, facet_index)) ");
        jdbc.SQLupdate("TRUNCATE TABLE clusterFacetsScratch");
        for (int i = 0; i < clusterTables.length; i++) {
            String tName = clusterTables[i];
            jdbc.SQLupdate("CREATE TEMPORARY TABLE IF NOT EXISTS " + tName + " LIKE clusterFacetsScratch");
            jdbc.SQLupdate("TRUNCATE TABLE " + tName);
        }
    }

    void insertClusterFacets(String[] clusterTables, String SQL) throws SQLException {
        jdbc.SQLupdate("TRUNCATE TABLE clusterFacetsScratch");
        jdbc.SQLupdate("INSERT INTO clusterFacetsScratch " + SQL);
        for (int i = 0; i < clusterTables.length; i++) {
            String tName = clusterTables[i];
            jdbc.SQLupdate("INSERT INTO " + tName + " SELECT * FROM clusterFacetsScratch");
        }
    }

    String[] clusterTables(int maxClusterSize) {
        int nCFtables = Math.max(3, maxClusterSize);
        String[] result = new String[nCFtables * (nCFtables - 1)];
        int index = 0;
        for (int i = 1; i <= nCFtables; i++) {
            for (int j = 1; j <= nCFtables; j++) {
                if (j != i) {
                    String tName = "clusterFacets" + i + j;
                    result[index++] = tName;
                }
            }
        }
        assert index == result.length;
        return result;
    }

    private synchronized double addClusters(int nFacets, double pValue, int maxClusters, String facetRestriction, String[] clusterTables) throws SQLException, ServletException {
        int neededClusters = maxClusters - jdbc.SQLqueryInt("SELECT COUNT(*) FROM clusterInfo");
        if (pValue > 0 || neededClusters > 0) {
            ResultSet rs = null;
            try {
                PreparedStatement addCluster = jdbc.lookupPS("INSERT INTO clusterInfo VALUES(?, ?, ?, ?, ?)");
                int q = jdbc.SQLqueryInt("SELECT COUNT(*) FROM onItems");
                int db = jdbc.SQLqueryInt("SELECT COUNT(*) FROM item");
                int c = jdbc.SQLqueryInt("SELECT MAX(cluster_id) FROM clusterInfo");
                rs = jdbc.SQLquery(clusterQuery(nFacets, facetRestriction));
                while (rs.next() && (pValue > 0 || neededClusters > 0)) {
                    int con = rs.getInt(1);
                    int ctot = rs.getInt(2);
                    if (con * db > q * ctot) {
                        double p = ChiSq2x2.getInstance(this, db, q, ctot, con, "").pvalue();
                        myAssert(p >= 0, "p = " + p);
                        if (p < pValue || (p == pValue && neededClusters > 0)) {
                            addCluster.setInt(1, ++c);
                            addCluster.setInt(2, nFacets);
                            addCluster.setInt(3, con);
                            addCluster.setInt(4, ctot);
                            addCluster.setDouble(5, p);
                            jdbc.SQLupdate(addCluster);
                            for (int i = 0; i < nFacets; i++) {
                                insertClusterFacets(clusterTables, "VALUES(" + c + ", " + rs.getInt(i + 3) + ", " + (i + 1) + ")");
                            }
                            if (--neededClusters < 0) {
                                double newPvalue = jdbc.SQLqueryDouble("SELECT pValue FROM clusterInfo ORDER BY pValue LIMIT " + maxClusters + ", 1");
                                myAssert(newPvalue <= pValue, newPvalue + " should be less than " + pValue);
                                pValue = newPvalue;
                            }
                        }
                    }
                }
            } finally {
                if (rs != null) rs.close();
            }
        }
        return pValue;
    }

    void extractClustersFromTables(int maxClusters, String[] clusterTables, DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = null;
        ResultSet rs1 = null;
        try {
            int nRows = 0;
            int prevRows = 0;
            while ((nRows = jdbc.SQLqueryInt("SELECT COUNT(*) FROM clusterFacets21")) > prevRows) {
                prevRows = nRows;
                insertClusterFacets(clusterTables, "SELECT cf.cluster_id, parent_facet_id, " + "MIN(previ.facet_index) - ABS(MIN(cf.facet_index)) - 1 " + "FROM clusterFacets12 cf " + "INNER JOIN facet f ON cf.facet_id = f.facet_id " + "LEFT JOIN clusterFacets31 previ ON cf.cluster_id = previ.cluster_id " + "LEFT JOIN clusterFacets23 dup ON cf.cluster_id = dup.cluster_id AND dup.facet_id = parent_facet_id " + "WHERE parent_facet_id > 0 " + "AND dup.facet_id IS NULL " + "GROUP BY cf.cluster_id, parent_facet_id ORDER BY NULL");
            }
            rs = jdbc.SQLquery("SELECT cluster_id, pValue, nOn, nTotal FROM clusterInfo " + "ORDER BY pValue, nOn/nTotal DESC, nFacets DESC LIMIT " + maxClusters);
            rs.last();
            nRows = rs.getRow();
            writeInt(nRows, out);
            rs.beforeFirst();
            while (rs.next()) {
                rs1 = jdbc.SQLquery("SELECT f.parent_facet_id, f.facet_id, f.name, f.n_child_facets, f.first_child_offset, " + "f.n_items, cf.facet_index <= 0 isAncestor, " + rs.getDouble(2) + ", " + rs.getInt(3) + ", " + rs.getInt(4) + " FROM clusterfacets21 cf " + "INNER JOIN facet f USING (facet_id) " + "WHERE cf.cluster_id = " + rs.getInt(1) + " AND f.parent_facet_id > 0" + " ORDER BY f.facet_id");
                sendResultSet(rs1, MyResultSet.INT_PINT_STRING_INT_INT_INT_INT_DOUBLE_PINT_PINT, out);
                rs1 = null;
            }
        } finally {
            if (rs != null) rs.close();
            if (rs1 != null) rs1.close();
        }
    }

    private static String clusterQuery(int nFacets, String facetRestriction) throws ServletException {
        myAssert(nFacets > 0, "clusterQuery facets=" + nFacets);
        if (nFacets == 1) return "SELECT COUNT(DISTINCT i.record_num) con, f.n_items ctot, i.facet_id " + "FROM item_facet i " + "INNER JOIN onItems USING (record_num) " + "INNER JOIN facet f USING (facet_id) " + (facetRestriction == null ? "" : facetRestriction) + " GROUP BY i.facet_id " + "HAVING con > 1 ORDER BY null"; else if (nFacets == 2) return "SELECT STRAIGHT_JOIN COUNT(*) con, cnt ctot, pairs.facet1, pairs.facet2 " + "FROM pairs, clusterFacets12, clusterFacets21, onItems, " + "item_facet_heap i1, " + "item_facet_heap i2 " + "WHERE clusterFacets12.facet_id = pairs.facet1 AND clusterFacets21.facet_id = pairs.facet2 " + "AND pairs.facet1 = i1.facet_id AND pairs.facet2 = i2.facet_id " + "AND i1.record_num = i2.record_num AND i1.record_num = onItems.record_num " + "AND clusterFacets12.facet_index = 1 AND clusterFacets21.facet_index = 1 " + "GROUP BY pairs.facet1, pairs.facet2 " + "HAVING con > 1 ORDER BY null"; else return clusterQueryInternal(nFacets);
    }

    private static String clusterQueryInternal(int nFacets) throws ServletException {
        myAssert(nFacets == 3, "clusterQuery facets=" + nFacets);
        String cfExpr = "SELECT STRAIGHT_JOIN " + "COUNT(DISTINCT o.record_num) con, COUNT(DISTINCT i1.record_num) ctot, ";
        int nCfTables = nFacets * (nFacets - 1);
        String[] cfTables = new String[nCfTables];
        String[] ifTables = new String[nFacets];
        String[] ifJoinTables = new String[nFacets + 1];
        String[] constraints = new String[3 * nFacets * (nFacets - 1)];
        int cfTableIndex = 0;
        int constraintIndex = 0;
        for (int cluster = 1; cluster <= nFacets; cluster++) {
            int facetIndex = nFacets - 1;
            String prevTable = null;
            for (int facet = nFacets; facet > 0; facet--) {
                if (facet != cluster) {
                    String table = "clusterFacets" + cluster + facet;
                    cfTables[cfTableIndex++] = table;
                    constraints[constraintIndex++] = table + ".facet_index = " + facetIndex--;
                    if (prevTable != null) constraints[constraintIndex++] = table + ".cluster_id = " + prevTable + ".cluster_id";
                    if (cluster > 1 && (cluster > 2 || facet > 1)) constraints[constraintIndex++] = table + ".facet_id = clusterFacets" + (facet == 1 ? 2 : 1) + facet + ".facet_id";
                    prevTable = table;
                }
            }
            ifJoinTables[cluster] = "i" + cluster;
            ifTables[cluster - 1] = ifJoinTables[cluster] + ".facet_id";
            if (cluster > 1) {
                constraints[constraintIndex++] = "i1.record_num = i" + cluster + ".record_num";
                constraints[constraintIndex++] = ifJoinTables[cluster] + ".facet_id = clusterFacets1" + cluster + ".facet_id";
            } else {
                constraints[constraintIndex++] = ifJoinTables[cluster] + ".facet_id = clusterFacets2" + cluster + ".facet_id";
            }
        }
        constraints[constraintIndex++] = "clusterFacets21.facet_id < clusterFacets12.facet_id";
        cfExpr += Util.join(ifTables, ", ") + " FROM ";
        cfExpr += Util.join(cfTables, " INNER JOIN ");
        ifJoinTables[0] = cfExpr;
        cfExpr = Util.join(ifJoinTables, " INNER JOIN item_facet_heap ");
        cfExpr += " LEFT JOIN onItems o ON i1.record_num = o.record_num WHERE ";
        cfExpr += Util.join(constraints, " AND ");
        cfExpr += " GROUP BY " + Util.join(ifTables, ", ") + " HAVING con > 1 ORDER BY null";
        return cfExpr;
    }

    void caremediaPlayArgs(String items, DataOutputStream out) throws SQLException, ServletException, IOException {
        log("caremediaPlayArgs " + items);
        ResultSet rs = jdbc.SQLquery("SELECT segment.segment_id," + " TIMESTAMPDIFF(SECOND, copyright_date, start_date) start_offset," + " TIMESTAMPDIFF(SECOND, copyright_date, end_date)" + " FROM item INNER JOIN event ON item.record_num = event.event_id" + " INNER JOIN movie USING (movie_id)" + " INNER JOIN segment ON segment.movie_id = movie.movie_id" + " WHERE record_num IN (" + items + ") HAVING start_offset >= 0" + " ORDER BY segment.segment_id");
        printRecords(rs, MyResultSet.SNMINT_INT_INT);
        sendResultSet(rs, MyResultSet.SNMINT_INT_INT, out);
    }

    void caremediaGetItems(String segments, DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = jdbc.SQLquery("SELECT record_num" + " FROM item " + " INNER JOIN movie USING (movie_id)" + " INNER JOIN segment ON segment.movie_id = movie.movie_id" + " WHERE segment_id IN (" + segments + ") ORDER BY record_num");
        sendResultSet(rs, MyResultSet.SINT, out);
    }

    void printRecords(ResultSet result, List<Object> types) {
        log(MyResultSet.valueOfDeep(result, types, 5));
    }

    private void log(String message) {
        jdbc.print(message);
    }

    static void myAssert(boolean condition, String msg) throws ServletException {
        if (!condition) error(msg);
    }

    private static void error(String message) throws ServletException {
        throw (new ServletException(message));
    }

    private static void checkNonNegative(int id) throws ServletException {
        if (id < 0) {
            error("Bad ID: " + id);
        }
    }

    private static void checkPositive(int id) throws ServletException {
        if (id <= 0) {
            error("Bad ID: " + id);
        }
    }

    void setItemDescription(int item, String displayText) throws SQLException {
        String[] values = displayText.split("(?:\\n|\\r)+<\\w+>(?:\\n|\\r)+");
        String fieldNames = jdbc.SQLqueryString("SELECT itemDescriptionFields FROM globals");
        String[] fields = fieldNames.split(",");
        Pattern p = Pattern.compile("(?:\\n|\\r)+<(\\w+)>(?:\\n|\\r)+");
        Matcher m = p.matcher(displayText);
        int valueIndex = 1;
        while (m.find()) {
            String fieldName = m.group(1);
            if (!Util.isMember(fields, fieldName)) {
                fieldNames += "," + fieldName;
                jdbc.SQLupdate("UPDATE globals SET itemDescriptionFields = " + JDBCSample.quote(fieldNames));
                jdbc.SQLupdate("ALTER TABLE item ADD COLUMN `" + fieldName + "` TEXT DEFAULT NULL");
            }
            String value = values[valueIndex++];
            jdbc.SQLupdate("UPDATE item SET " + fieldName + " = " + JDBCSample.quote(value) + " WHERE record_num = " + item);
        }
    }

    void opsSpec(int session, DataOutputStream out) throws SQLException, ServletException, IOException {
        ResultSet rs = jdbc.SQLquery("SELECT CONCAT_WS(','," + " TIMESTAMPDIFF(SECOND, (SELECT MIN(timestamp) FROM user_actions WHERE session = " + session + ")," + " timestamp)," + " location, object, modifiers) FROM user_actions WHERE session = " + session + " ORDER BY action_number, timestamp");
        sendResultSet(rs, MyResultSet.STRING, out);
    }

    void sendResultSet(ResultSet result, List<Object> types, DataOutputStream out) throws ServletException, SQLException, IOException {
        sendResultSet(result, types, -1, -1, -1, out);
    }

    void sendResultSet(ResultSet result, List<Object> types, int imageW, int imageH, int quality, DataOutputStream out) throws ServletException, SQLException, IOException {
        if (result == null) {
            writeInt(0, out);
            jdbc.print("sendResultSet given null result set.");
        } else {
            try {
                result.last();
                int nRows = result.getRow();
                int nCols = types.size();
                writeInt(nRows + 1, out);
                writeInt(nCols, out);
                for (int i = 0; i < nCols; i++) {
                    myAssert(!(types.get(i) == Column.ImageType && (types.get(i + 1) != Column.IntegerType || types.get(i + 2) != Column.IntegerType)), "Images must be followed by width and height");
                    writeCol(result, i + 1, types.get(i), imageW, imageH, quality, out);
                }
            } finally {
                jdbc.close(result);
            }
        }
    }

    private static void writeCol(ResultSet result, int colIndex, Object type, int imageW, int imageH, int quality, DataOutputStream out) throws ServletException, SQLException, IOException {
        result.beforeFirst();
        if (type == Column.SortedIntegerType) {
            writeIntCol(result, colIndex, out, true, true);
        } else if (type == Column.PositiveIntegerType) {
            writeIntCol(result, colIndex, out, false, true);
        } else if (type == MyResultSet.Column.IntegerType) {
            writeIntCol(result, colIndex, out, false, false);
        } else if (type == MyResultSet.Column.SortedNMIntegerType) {
            writeIntCol(result, colIndex, out, true, false);
        } else if (type == MyResultSet.Column.StringType) {
            writeStringCol(result, colIndex, out);
        } else if (type == MyResultSet.Column.DoubleType) {
            writeDoubleCol(result, colIndex, out);
        } else if (type == MyResultSet.Column.ImageType) {
            writeBlobCol(result, colIndex, imageW, imageH, quality, out);
        } else {
            throw (new ServletException("Unknown ColumnType: " + type));
        }
    }

    private static void writeIntCol(ResultSet result, int colIndex, OutputStream out, boolean sorted, boolean positive) throws ServletException, SQLException, IOException {
        int prev = 0;
        int prevValue = -1;
        while (result.next()) {
            int value = result.getInt(colIndex);
            if (sorted) {
                int diff = value - prev;
                myAssert(diff >= 0, "Column " + colIndex + " is not sorted: " + value + " < " + prev);
                myAssert(!positive || diff != 0, "Column " + colIndex + " is not monotonically increasing: " + value + " < " + prev);
                prev = value;
                value = diff;
            }
            myAssert(value >= 0, "Column " + colIndex + ", row " + result.getRow() + " contains the negative integer " + value);
            if (prevValue < 0) {
                prevValue = value;
            } else {
                prevValue = writeIntOrTwo(prevValue, value, out, positive);
            }
        }
        if (prevValue >= 0) writeIntOrTwo(prevValue, -1, out, positive);
    }

    private static void writeStringCol(ResultSet result, int colIndex, DataOutputStream out) throws SQLException, IOException {
        while (result.next()) {
            writeString(result.getString(colIndex), out);
        }
    }

    private static void writeDoubleCol(ResultSet result, int colIndex, DataOutputStream out) throws SQLException, IOException {
        while (result.next()) {
            writeDouble(result.getDouble(colIndex), out);
        }
    }

    private static void writeBlobCol(ResultSet result, int colIndex, int imageW, int imageH, int quality, OutputStream out) throws ServletException, SQLException, IOException {
        while (result.next()) {
            try {
                writeBlob(result.getBlob(colIndex), imageW, imageH, quality, result.getInt(colIndex + 1), result.getInt(colIndex + 2), out);
            } catch (IllegalArgumentException e) {
                writeInt(0, out);
            } catch (Exception e) {
                throw new ServletException("Got exception " + e + " while writing blob on row " + result.getRow() + " col " + colIndex + " " + currentRowToString(result));
            }
        }
    }

    static String currentRowToString(ResultSet rs) throws SQLException {
        StringBuffer buf = new StringBuffer();
        ResultSetMetaData meta = rs.getMetaData();
        for (int col = 1; col <= meta.getColumnCount(); col++) {
            buf.append(" Col ").append(col).append("=");
            int type = meta.getColumnType(col);
            switch(type) {
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                case Types.BIT:
                case Types.BOOLEAN:
                case Types.DECIMAL:
                    buf.append(rs.getInt(col));
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                    buf.append(rs.getString(col));
                    break;
                case Types.CLOB:
                case Types.BLOB:
                    buf.append("<BLOB>");
                    break;
                default:
                    buf.append("<Unhandled Type ").append(type).append(">");
                    break;
            }
            buf.append(";");
        }
        return buf.toString();
    }

    static void writeString(String s, DataOutputStream out) throws IOException {
        if (s == null) s = "";
        out.writeUTF(s);
    }

    private static void writeDouble(double n, DataOutputStream out) throws IOException {
        out.writeDouble(n);
    }

    static int writeInt(int n, OutputStream out) throws ServletException, IOException {
        myAssert(n >= 0, n + " Tried to write a negative int.");
        myAssert(n < 1073741824, n + " Tried to write a too-large int:");
        if (n < 128) out.write(n); else if (n < 16384) {
            out.write((n >> 8) | 128);
            out.write(n);
        } else if (n < 2097152) {
            out.write((n >> 16) | 192);
            out.write(n >> 8);
            out.write(n);
        } else {
            out.write((n >> 24) | 224);
            out.write(n >> 16);
            out.write(n >> 8);
            out.write(n);
        }
        return n;
    }

    private static int writeIntOrTwo(int n, int nextN, OutputStream out, boolean positive) throws ServletException, IOException {
        if (positive) {
            myAssert(nextN != 0 && n != 0, "Tried to write 0 to a PINT: " + n + " " + nextN);
            n--;
            nextN--;
        }
        myAssert(n >= 0, n + " Tried to write a negative int.");
        myAssert(n < 1073741824 && nextN < 1073741824, n + " Tried to write a too-large int:");
        if (n < 8 && nextN >= 0 && nextN < 8) {
            out.write(n << 3 | nextN | 64);
            nextN = -2;
        } else if (n < 64) {
            out.write(n);
        } else if (n < 16384) {
            out.write((n >> 8) | 128);
            out.write(n);
        } else if (n < 2097152) {
            out.write((n >> 16) | 192);
            out.write(n >> 8);
            out.write(n);
        } else {
            out.write((n >> 24) | 224);
            out.write(n >> 16);
            out.write(n >> 8);
            out.write(n);
        }
        if (positive) nextN++;
        return nextN;
    }

    private static void writeBlob(Blob blob, int desiredW, int desiredH, int quality, int actualW, int actualH, OutputStream out) throws ServletException, IOException, SQLException {
        if (blob == null || desiredW < 0) writeInt(0, out); else if (2 * Math.min(desiredW, actualW) * Math.min(desiredH, actualH) < actualW * actualH) {
            resize(blob, desiredW, desiredH, quality, actualW, actualH, out);
        } else {
            int n = (int) blob.length();
            writeInt(n + 1, out);
            InputStream s = null;
            try {
                s = blob.getBinaryStream();
                int x;
                while ((x = s.read()) >= 0) {
                    out.write(x);
                }
            } finally {
                s.close();
            }
        }
    }

    private static void resize(Blob blob, int desiredW, int desiredH, int quality, double actualW, double actualH, OutputStream out) throws SQLException, IOException, ServletException {
        InputStream blobStream = blob.getBinaryStream();
        double ratio = Math.min(desiredW / actualW, desiredH / actualH);
        int newW = (int) Math.round(actualW * ratio);
        int newH = (int) Math.round(actualH * ratio);
        myAssert(newW == desiredW || newH == desiredH, "WARNING: bad resize: " + desiredW + "x" + desiredH + " " + newW + "x" + newH);
        BufferedImage resized = Util.resize(ImageIO.read(blobStream), newW, newH, false);
        ByteArrayOutputStream byteArrayStream = null;
        try {
            byteArrayStream = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(byteArrayStream);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(resized);
            param.setQuality(quality / 100f, false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(resized);
            int len = byteArrayStream.size();
            if (len * 3 < blob.length() * 2) {
                writeInt(len + 1, out);
                byteArrayStream.writeTo(out);
            } else {
                writeBlob(blob, newW, newH, quality, newW, newH, out);
            }
        } finally {
            byteArrayStream.close();
        }
    }

    String dbDescs(String dbNameList) throws SQLException, ServletException {
        myAssert(dbNameList != null && dbNameList.length() > 0, "Empty db name list");
        String[] dbNames = Util.splitComma(dbNameList);
        StringBuffer dbDescs = new StringBuffer();
        for (int i = 0; i < dbNames.length; i++) {
            dbDescsInternal(dbDescs, dbNames[i]);
        }
        if (!Util.isMember(dbNames, dbName)) {
            dbDescsInternal(dbDescs, dbName);
        }
        return dbDescs.toString();
    }

    void dbDescsInternal(StringBuffer dbDescs, String name) throws SQLException {
        String desc = jdbc.SQLqueryString("SELECT description FROM " + name + ".globals");
        if (dbDescs.length() > 0) dbDescs.append(";");
        dbDescs.append(name).append(",").append(desc);
    }
}
