package com.esri.gpt.catalog.arcims;

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages ArcIMS Metadata Server like tables in the absence of the metadata server.
 */
class ImsMetadataProxyDao extends BaseDao {

    /** class variables ========================================================= */
    private static final Logger LOGGER = Logger.getLogger(ImsMetadataProxyDao.class.getName());

    /** instance variables ====================================================== */
    private Publisher publisher;

    /**
   * Constructs with an associated request context.
   * @param requestContext the request context
   */
    protected ImsMetadataProxyDao(RequestContext requestContext, Publisher publisher) {
        super(requestContext);
        this.publisher = publisher;
    }

    /** properties ============================================================== */
    private String getResourceTableName() {
        return getRequestContext().getCatalogConfiguration().getResourceTableName();
    }

    private String getResourceDataTableName() {
        return getRequestContext().getCatalogConfiguration().getResourceDataTableName();
    }

    /**
   * Authorizes a request.
   * @param request the underlying request
   * @param the UUID of the subject record
   * @throws ImsServiceException typically related to an authorization related exception
   * @throws SQLException if a database exception occurs
   */
    private void authorize(ImsRequest request, String uuid) throws ImsServiceException, SQLException {
        boolean checkOwner = false;
        if (request instanceof PutMetadataRequest) {
            checkOwner = true;
        } else if (request instanceof GetDocumentRequest) {
            if (!this.publisher.getIsAdministrator()) {
                checkOwner = true;
            }
        } else if (request instanceof DeleteMetadataRequest) {
            if (!this.publisher.getIsAdministrator()) {
                checkOwner = true;
            }
        } else if (request instanceof TransferOwnershipRequest) {
            if (!this.publisher.getIsAdministrator()) {
                throw new ImsServiceException("TransferOwnershipRequest: not authorized.");
            }
        }
        if (checkOwner) {
            int ownerID = this.queryOwnerByUuid(uuid);
            if ((ownerID != -1) && (ownerID != this.publisher.getLocalID())) {
                String username = this.queryUsernameByID(ownerID);
                String msg = "The document is owned by another user: " + username + ", " + uuid;
                throw new ImsServiceException(msg);
            }
        }
    }

    /**
   * Deletes a record from the proxied ArcIMS metadata table.
   * @param request the underlying request
   * @param uuid the UUID for the record to delete
   * @return the number of rows affected
   * @throws ImsServiceException typically related to an authorization related exception
   * @throws SQLException if a database exception occurs
   */
    protected int deleteRecord(DeleteMetadataRequest request, String uuid) throws ImsServiceException, SQLException {
        Connection con = null;
        boolean autoCommit = true;
        PreparedStatement st = null;
        int nRows = 0;
        try {
            this.authorize(request, uuid);
            ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(getRequestContext());
            nRows = adminDao.deleteRecord(uuid);
        } catch (CatalogIndexException ex) {
            if (con != null) {
                con.rollback();
            }
            throw new ImsServiceException(ex.getMessage(), ex);
        } catch (ImsServiceException ex) {
            if (con != null) {
                con.rollback();
            }
            throw ex;
        } catch (SQLException ex) {
            if (con != null) {
                con.rollback();
            }
            throw ex;
        } finally {
            closeStatement(st);
            if (con != null) {
                con.setAutoCommit(autoCommit);
            }
        }
        return nRows;
    }

    /**
   * Determines if a document UUID exists within the proxied ArcIMS metadata table.
   * @param con the JDBC connection
   * @param uuid the document UUID to check
   * @return true if the document UUID exists
   * @throws SQLException if a database exception occurs
   */
    private long doesRecordExist(String table, String uuid) throws SQLException {
        long id = -1;
        PreparedStatement st = null;
        try {
            Connection con = returnConnection().getJdbcConnection();
            String sSql = "SELECT ID FROM " + table + " WHERE DOCUUID=?";
            logExpression(sSql);
            st = con.prepareStatement(sSql);
            st.setString(1, uuid);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                id = rs.getLong(1);
            }
        } finally {
            closeStatement(st);
        }
        return id;
    }

    /**
   * Inserts or updates a record within the proxied ArcIMS metadata table.
   * @param request the underlying request
   * @param info the information for the document to be inserted
   * @return the number of rows affected
   * @throws ImsServiceException typically related to an authorization related exception
   * @throws SQLException if a database exception occurs
   */
    protected int insertRecord(PutMetadataRequest request, PutMetadataInfo info) throws ImsServiceException, SQLException {
        Connection con = null;
        boolean autoCommit = true;
        PreparedStatement st = null;
        ResultSet rs = null;
        int nRows = 0;
        String sXml = info.getXml();
        String sUuid = info.getUuid();
        String sName = info.getName();
        String sThumbnailBinary = info.getThumbnailBinary();
        String sTable = this.getResourceTableName();
        String sDataTable = this.getResourceDataTableName();
        long id = doesRecordExist(sTable, sUuid);
        try {
            ManagedConnection mc = returnConnection();
            con = mc.getJdbcConnection();
            autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            if (id < 0) {
                StringBuffer sql = new StringBuffer();
                sql.append("INSERT INTO ").append(sTable);
                sql.append(" (");
                sql.append("DOCUUID,");
                sql.append("TITLE,");
                sql.append("OWNER");
                sql.append(")");
                sql.append(" VALUES(?,?,?)");
                logExpression(sql.toString());
                st = con.prepareStatement(sql.toString());
                int n = 1;
                st.setString(n++, sUuid);
                st.setString(n++, sName);
                st.setInt(n++, this.publisher.getLocalID());
                nRows = st.executeUpdate();
                closeStatement(st);
                if (nRows > 0) {
                    if (getIsDbCaseSensitive(this.getRequestContext())) {
                        st = con.prepareStatement("SELECT id FROM " + sTable + " WHERE UPPER(docuuid)=?");
                    } else {
                        st = con.prepareStatement("SELECT id FROM " + sTable + " WHERE docuuid=?");
                    }
                    st.setString(1, sUuid.toUpperCase());
                    rs = st.executeQuery();
                    rs.next();
                    id = rs.getLong(1);
                    closeStatement(st);
                    request.setActionStatus(ImsRequest.ACTION_STATUS_OK);
                    sql = new StringBuffer();
                    sql.append("INSERT INTO ").append(sDataTable);
                    sql.append(" (DOCUUID,ID,XML)");
                    sql.append(" VALUES(?,?,?)");
                    logExpression(sql.toString());
                    st = con.prepareStatement(sql.toString());
                    st.setString(1, sUuid);
                    st.setLong(2, id);
                    st.setString(3, sXml);
                    st.executeUpdate();
                }
            } else {
                this.authorize(request, sUuid);
                StringBuffer sql = new StringBuffer();
                sql.append("UPDATE ").append(sTable);
                sql.append(" SET ");
                if (!request.getLockTitle()) {
                    sql.append("TITLE=?, ");
                }
                sql.append("OWNER=?, ");
                sql.append("UPDATEDATE=?");
                sql.append(" WHERE DOCUUID=?");
                logExpression(sql.toString());
                st = con.prepareStatement(sql.toString());
                int n = 1;
                if (!request.getLockTitle()) {
                    st.setString(n++, sName);
                }
                st.setInt(n++, this.publisher.getLocalID());
                st.setTimestamp(n++, new Timestamp(System.currentTimeMillis()));
                st.setString(n++, sUuid);
                nRows = st.executeUpdate();
                if (nRows > 0) {
                    request.setActionStatus(ImsRequest.ACTION_STATUS_REPLACED);
                }
                closeStatement(st);
                sql = new StringBuffer();
                if (doesRecordExist(sDataTable, sUuid) >= 0) {
                    sql.append("UPDATE ").append(sDataTable);
                    sql.append(" SET DOCUUID=?, XML=?, THUMBNAIL=?");
                    sql.append(" WHERE ID=?");
                } else {
                    sql.append("INSERT INTO ").append(sDataTable);
                    sql.append(" (DOCUUID, XML,THUMBNAIL,ID)");
                    sql.append(" VALUES(?,?,?,?)");
                }
                logExpression(sql.toString());
                st = con.prepareStatement(sql.toString());
                st.setString(1, sUuid);
                st.setString(2, sXml);
                st.setBytes(3, null);
                st.setLong(4, id);
                st.executeUpdate();
            }
            con.commit();
        } catch (ImsServiceException ex) {
            if (con != null) {
                con.rollback();
            }
            throw ex;
        } catch (SQLException ex) {
            if (con != null) {
                con.rollback();
            }
            throw ex;
        } finally {
            closeResultSet(rs);
            closeStatement(st);
            if (con != null) {
                con.setAutoCommit(autoCommit);
            }
        }
        if ((sThumbnailBinary != null) && (sThumbnailBinary.length() > 0)) {
            this.updateThumbnail(sThumbnailBinary, sUuid);
        }
        return nRows;
    }

    /**
   * Queries the owner id associated with a document UUID.
   * @param uuid the document UUID
   * @return the owner name (empty string if not found)
   * @throws SQLException if a database exception occurs
   */
    private int queryOwnerByUsername(String username) throws SQLException {
        int ownerID = -1;
        PreparedStatement st = null;
        try {
            username = Val.chkStr(username);
            String table = this.getRequestContext().getCatalogConfiguration().getUserTableName();
            if (username.length() > 0) {
                Connection con = returnConnection().getJdbcConnection();
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT USERID FROM ").append(table);
                if (getIsDbCaseSensitive(this.getRequestContext())) {
                    sql.append(" WHERE UPPER(USERNAME)=?");
                } else {
                    sql.append(" WHERE USERNAME=?");
                }
                logExpression(sql.toString());
                st = con.prepareStatement(sql.toString());
                st.setString(1, username.toUpperCase());
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    ownerID = rs.getInt(1);
                }
            }
        } finally {
            closeStatement(st);
        }
        return ownerID;
    }

    /**
   * Queries the owner id associated with a document UUID.
   * @param uuid the document UUID
   * @return the owner name (empty string if not found)
   * @throws SQLException if a database exception occurs
   */
    private int queryOwnerByUuid(String uuid) throws SQLException {
        int ownerID = -1;
        PreparedStatement st = null;
        try {
            uuid = Val.chkStr(uuid);
            if (uuid.length() > 0) {
                Connection con = returnConnection().getJdbcConnection();
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT OWNER FROM ").append(getResourceTableName());
                sql.append(" WHERE DOCUUID=?");
                logExpression(sql.toString());
                st = con.prepareStatement(sql.toString());
                st.setString(1, uuid);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    ownerID = rs.getInt(1);
                }
            }
        } finally {
            closeStatement(st);
        }
        return ownerID;
    }

    /**
   * Queries the owner id associated with a document UUID.
   * @param uuid the document UUID
   * @return the owner name (empty string if not found)
   * @throws SQLException if a database exception occurs
   */
    private String queryUsernameByID(int userID) throws SQLException {
        String username = "";
        PreparedStatement st = null;
        try {
            String table = this.getRequestContext().getCatalogConfiguration().getUserTableName();
            Connection con = returnConnection().getJdbcConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT USERNAME FROM ").append(table);
            sql.append(" WHERE USERID=?");
            logExpression(sql.toString());
            st = con.prepareStatement(sql.toString());
            st.setInt(1, userID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                username = rs.getString(1);
            }
        } finally {
            closeStatement(st);
        }
        return username;
    }

    /**
   * Reads a record from the proxied ArcIMS metadata table.
   * @param request the underlying request
   * @param uuid the UUID for the record to read
   * @throws ImsServiceException typically related to an authorization related exception
   * @throws SQLException if a database exception occurs
   */
    protected void readRecord(GetDocumentRequest request, String uuid) throws ImsServiceException, SQLException {
        PreparedStatement st = null;
        try {
            this.authorize(request, uuid);
            ManagedConnection mc = returnConnection();
            Connection con = mc.getJdbcConnection();
            IClobMutator cm = mc.getClobMutator();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT UPDATEDATE");
            sql.append(" FROM ").append(getResourceTableName()).append(" WHERE DOCUUID=?");
            logExpression(sql.toString());
            st = con.prepareStatement(sql.toString());
            st.setString(1, uuid);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                request.setUpdateTimestamp(rs.getTimestamp(1));
                request.setActionStatus(ImsRequest.ACTION_STATUS_OK);
                closeStatement(st);
                sql = new StringBuffer();
                sql.append("SELECT XML");
                sql.append(" FROM ").append(getResourceDataTableName()).append(" WHERE DOCUUID=?");
                st = con.prepareStatement(sql.toString());
                st.setString(1, uuid);
                rs = st.executeQuery();
                if (rs.next()) {
                    request.setXml(cm.get(rs, 1));
                }
            }
        } finally {
            closeStatement(st);
        }
    }

    /**
   * Transfers ownership for a record in the proxied ArcIMS metadata table.
   * @param request the underlying request
   * @param uuid the UUID for the record to read
   * @return the number of rows affected
   * @throws ImsServiceException typically related to an authorization related exception
   * @throws SQLException if a database exception occurs
   */
    protected int transferOwnership(TransferOwnershipRequest request, String uuid, String newOwner) throws ImsServiceException, SQLException {
        PreparedStatement st = null;
        try {
            this.authorize(request, uuid);
            int ownerID = this.queryOwnerByUsername(newOwner);
            if (ownerID == -1) {
                throw new ImsServiceException("Unrecognized publisher: " + newOwner);
            }
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(this.getResourceTableName());
            sql.append(" SET OWNER=? WHERE DOCUUID=?");
            logExpression(sql.toString());
            Connection con = returnConnection().getJdbcConnection();
            st = con.prepareStatement(sql.toString());
            st.setInt(1, ownerID);
            st.setString(2, uuid);
            int nRows = st.executeUpdate();
            if (nRows > 0) {
                request.setActionStatus(ImsRequest.ACTION_STATUS_REPLACED);
            }
            return nRows;
        } finally {
            closeStatement(st);
        }
    }

    private void updateThumbnail(String base64Thumbnail, String uuid) {
        PreparedStatement st = null;
        try {
            byte[] bytes = (new sun.misc.BASE64Decoder()).decodeBuffer(base64Thumbnail);
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(this.getResourceDataTableName());
            sql.append(" SET THUMBNAIL=? WHERE DOCUUID=?");
            logExpression(sql.toString());
            Connection con = returnConnection().getJdbcConnection();
            st = con.prepareStatement(sql.toString());
            st.setBytes(1, bytes);
            st.setString(2, uuid);
            st.executeUpdate();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error converting base64 thumbnail to bytes.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving thumbnail blob to database.", e);
        } finally {
            closeStatement(st);
        }
    }
}
