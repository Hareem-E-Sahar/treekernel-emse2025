package org.mitre.rt.server.database.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.mitre.rt.client.properties.RTClientProperties;
import org.mitre.rt.common.FileUpdateResponse;
import org.mitre.rt.common.UpdateResponse;
import org.mitre.rt.common.UpdateResponseCode;
import org.mitre.rt.rtclient.FileType;
import org.mitre.rt.rtclient.VersionedItemType;
import org.mitre.rt.server.database.DatabaseManager;
import org.mitre.rt.server.exceptions.DatabaseException;
import org.mitre.rt.server.exceptions.RTServerException;
import org.mitre.rt.server.exceptions.SyncException;
import org.mitre.rt.server.exceptions.UniquePropertyException;

/**
 *
 * @author JWINSTON
 */
public class FileTypeDAO extends AppComponentIdedVersionedItemTypeDAO {

    private static final Logger logger = Logger.getLogger(FileTypeDAO.class.getPackage().getName());

    public FileTypeDAO() {
        super("File", FileType.class);
    }

    @Override
    public boolean checkReferences(Connection conn, String applicationId, VersionedItemType item) throws DatabaseException, SyncException, SQLException {
        return checkApplicationReference(conn, applicationId);
    }

    @Override
    public boolean isReferenced(Connection conn, String applicationId, VersionedItemType item) throws DatabaseException, SQLException {
        String referenceTables[] = { "Reference_File", "Compliance_Check_File" };
        FileType file = (FileType) item;
        boolean referenced = false;
        Statement s = conn.createStatement();
        for (String table : referenceTables) {
            String sql = "SELECT file_id FROM " + table + " WHERE application_id = '" + StringEscapeUtils.escapeSql(applicationId) + "'" + " AND file_id = '" + StringEscapeUtils.escapeSql(file.getId()) + "'";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                referenced = true;
                logger.info("isReferenced - File id=" + file.getId() + " is referenced in table " + table);
                break;
            }
        }
        s.close();
        return referenced;
    }

    @Override
    public VersionedItemType insert(Connection conn, String applicationId, VersionedItemType item) throws DatabaseException, UniquePropertyException, SQLException, Exception {
        FileType fileIn = (FileType) item;
        FileType file = FileType.Factory.newInstance();
        file.set(fileIn);
        this.incrementVersion(file);
        this.resetChangeType(file);
        String filePath = RTClientProperties.instance().getFilesDir();
        if (file.getPath().endsWith(File.separator)) {
            filePath = filePath + file.getPath() + File.separator + file.getFileName();
        } else {
            filePath = filePath + File.separator + file.getPath() + File.separator + file.getFileName();
        }
        logger.debug("Saving file: " + filePath);
        File uploadFile = new File(filePath);
        InputStream in = new FileInputStream(uploadFile);
        String sql = "INSERT INTO " + StringEscapeUtils.escapeSql(this.getTableName()) + " (id, application_id, xml, version, file_content, db_deleted, db_created, db_modified) " + " VALUES (?, ?, ?, ?, ?, ?, now(), now()) " + " ON DUPLICATE KEY UPDATE file_content = VALUES(file_content), xml = VALUES(xml), version = VALUES(version), db_modified = NOW() ";
        PreparedStatement ps = conn.prepareStatement(sql);
        int i = 0;
        ps.setString(++i, file.getId());
        ps.setString(++i, applicationId);
        ps.setString(++i, this.getDBStringForXmlObject(file));
        ps.setInt(++i, file.getVersion().intValue());
        ps.setBinaryStream(++i, in, in.available());
        ps.setBoolean(++i, false);
        int updateCnt = ps.executeUpdate();
        ps.close();
        if (updateCnt == 1) {
            logger.debug("Inserted File: " + file.getId());
        } else if (updateCnt >= 2) {
            logger.debug("Updated existing File in database: " + file.getId());
        } else {
            throw new DatabaseException("Unable to insert item. Executed query and " + updateCnt + " items were updated.");
        }
        return file;
    }

    @Override
    public VersionedItemType update(Connection conn, String applicationId, VersionedItemType item) throws SQLException, DatabaseException, SyncException, Exception {
        return this.insert(conn, applicationId, item);
    }

    @Override
    public List<UpdateResponse> select(Connection conn, Date lastUpdate, String applicationId) throws SQLException, DatabaseException, RTServerException, Exception {
        ArrayList<UpdateResponse> urs = new ArrayList<UpdateResponse>();
        XmlOptions parseOptions = getDbParseOptions();
        Statement s = conn.createStatement();
        String modDate = AbsDAO.dbDateFormat.format(lastUpdate);
        String sql = "SELECT xml, file_content, db_deleted, db_created FROM " + StringEscapeUtils.escapeSql(this.getTableName()) + " " + "WHERE application_id = '" + StringEscapeUtils.escapeSql(applicationId) + "' " + "AND db_modified > '" + StringEscapeUtils.escapeSql(modDate) + "'";
        ResultSet rs = s.executeQuery(sql);
        urs.ensureCapacity(rs.getFetchSize());
        FileType item = null;
        while (rs.next()) {
            int i = 1;
            item = FileType.Factory.parse(rs.getString(i++), parseOptions);
            Blob fileBlob = rs.getBlob(i++);
            File file = File.createTempFile("tmp.rt.server.FileType.file", "", new File(RTClientProperties.instance().getTempDir()));
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBlob.getBytes(1, (int) fileBlob.length()));
            fos.close();
            boolean isDeleted = rs.getBoolean(i++);
            java.sql.Timestamp createDate = rs.getTimestamp(i++);
            UpdateResponseCode urCode;
            if (isDeleted) {
                urCode = UpdateResponseCode.deletedItem;
            } else if (createDate.getTime() > lastUpdate.getTime()) {
                urCode = UpdateResponseCode.newItem;
            } else {
                urCode = UpdateResponseCode.modifiedItem;
            }
            FileUpdateResponse ur = new FileUpdateResponse(urCode, item);
            ur.setResponeFile(file);
            urs.add(ur);
        }
        s.close();
        return urs;
    }

    @Override
    public VersionedItemType select(Connection conn, String applicationId, String itemId) throws SQLException, DatabaseException, Exception {
        FileType item = null;
        XmlOptions parseOptions = getDbParseOptions();
        Statement s = conn.createStatement();
        String sql = "SELECT xml, file_content FROM " + StringEscapeUtils.escapeSql(this.getTableName()) + " " + "WHERE application_id = '" + StringEscapeUtils.escapeSql(applicationId) + "' " + "AND id = '" + StringEscapeUtils.escapeSql(itemId) + "'";
        ResultSet rs = s.executeQuery(sql);
        if (rs.next()) {
            int i = 1;
            item = FileType.Factory.parse(rs.getString(i++), parseOptions);
            Blob fileBlob = rs.getBlob(i++);
            File file = File.createTempFile("tmp.rt.server.FileType.file", "", new File(RTClientProperties.instance().getTempDir()));
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBlob.getBytes(1, (int) fileBlob.length()));
            fos.close();
        }
        s.close();
        return item;
    }

    /**
     * Retrieves the file infomraiton from the database and writes the file 
     * contents field to a temporary file. 
     * the xml 
     * @param applicationId - The id of the application the file is a component of.
     * @param fileId - The id of the file to get.
     * @return a FileUpdateResponse with the FileType item and a temporary file containing the file contents field of from the File table. May be null.
     * @throws org.mitre.rt.server.exceptions.DatabaseException
     * @throws org.mitre.rt.server.exceptions.RTServerException
     */
    public FileUpdateResponse getFile(Connection conn, String applicationId, String fileId) throws DatabaseException, RTServerException, SQLException, Exception {
        FileUpdateResponse ur = null;
        XmlOptions parseOptions = getDbParseOptions();
        Statement s = conn.createStatement();
        String sql = "SELECT xml, file_content, db_deleted, db_created FROM " + StringEscapeUtils.escapeSql(this.getTableName()) + " " + "WHERE application_id = '" + StringEscapeUtils.escapeSql(applicationId) + "' AND id = '" + StringEscapeUtils.escapeSql(fileId) + "'";
        ResultSet rs = s.executeQuery(sql);
        if (rs.next()) {
            int i = 1;
            FileType item = FileType.Factory.parse(rs.getString(i++), parseOptions);
            Blob fileBlob = rs.getBlob(i++);
            File file = File.createTempFile("tmp.rt.server.FileType.file", "", new File(RTClientProperties.instance().getTempDir()));
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBlob.getBytes(1, (int) fileBlob.length()));
            fos.close();
            boolean isDeleted = rs.getBoolean(i++);
            java.sql.Timestamp createDate = rs.getTimestamp(i++);
            UpdateResponseCode urCode;
            if (isDeleted) {
                urCode = UpdateResponseCode.deletedItem;
            } else {
                urCode = UpdateResponseCode.modifiedItem;
            }
            ur = new FileUpdateResponse(urCode, item);
            ur.setResponeFile(file);
        }
        s.close();
        return ur;
    }
}
