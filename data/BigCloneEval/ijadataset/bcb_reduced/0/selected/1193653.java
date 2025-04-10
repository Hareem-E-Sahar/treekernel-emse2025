package cz.fi.muni.xkremser.editor.server.DAO;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import cz.fi.muni.xkremser.editor.client.util.Constants;
import cz.fi.muni.xkremser.editor.server.exception.DatabaseException;
import cz.fi.muni.xkremser.editor.shared.rpc.ImageItem;

/**
 * The Class InputQueueItemDAOImpl.
 */
public class ImageResolverDAOImpl extends AbstractDAO implements ImageResolverDAO {

    public static int IMAGE_LIFETIME = 2;

    /** The Constant DELETE_ITEMS_STATEMENT. */
    public static final String DELETE_ITEMS_STATEMENT = "DELETE FROM " + Constants.TABLE_IMAGE_NAME + " WHERE shown < (NOW() - INTERVAL '" + IMAGE_LIFETIME + " day')";

    /** The Constant SELECT_ITEMS_FOR_DELETION_STATEMENT. */
    public static final String SELECT_ITEMS_FOR_DELETION_STATEMENT = "SELECT imageFile FROM " + Constants.TABLE_IMAGE_NAME + " WHERE shown < (NOW() - INTERVAL '" + IMAGE_LIFETIME + " day')";

    /** The Constant SELECT_ITEM_STATEMENT. */
    public static final String SELECT_ITEM_STATEMENT = "SELECT id, imageFile FROM " + Constants.TABLE_IMAGE_NAME + " WHERE old_fs_path = ((?))";

    /** The Constant SELECT_OLD_FS_PATH_STATEMENT. */
    public static final String SELECT_OLD_FS_PATH_STATEMENT = "SELECT old_fs_path FROM " + Constants.TABLE_IMAGE_NAME + " WHERE imagefile LIKE ((?))";

    /** The Constant UPDATE_ITEM_STATEMENT. */
    public static final String UPDATE_ITEM_STATEMENT = "UPDATE " + Constants.TABLE_IMAGE_NAME + " SET shown = CURRENT_TIMESTAMP WHERE id = (?)";

    /** The Constant INSERT_ITEM_STATEMENT. */
    public static final String INSERT_ITEM_STATEMENT = "INSERT INTO " + Constants.TABLE_IMAGE_NAME + " (identifier, imageFile, old_fs_path, shown) VALUES ((?),(?),(?),(CURRENT_TIMESTAMP))";

    /** The Constant INSERT_ITEM_STATEMENT. */
    public static final String DELETE_ITEM_STATEMENT = "DELETE FROM " + Constants.TABLE_IMAGE_NAME + " WHERE identifier = (?)";

    private static final Logger LOGGER = Logger.getLogger(ImageResolverDAOImpl.class);

    /**
     * Gets the item insert statement.
     * 
     * @param item
     *        the item
     * @return the item insert statement
     * @throws DatabaseException
     */
    private PreparedStatement getItemInsertStatement(ImageItem item) throws DatabaseException {
        PreparedStatement itemStmt = null;
        try {
            itemStmt = getConnection().prepareStatement(INSERT_ITEM_STATEMENT);
            itemStmt.setString(1, item.getIdentifier());
            itemStmt.setString(2, item.getJpeg2000FsPath());
            itemStmt.setString(3, item.getJpgFsPath());
        } catch (SQLException ex) {
            LOGGER.error("Could not get insert item statement " + itemStmt, ex);
        }
        return itemStmt;
    }

    /**
     * Gets the item delete statement.
     * 
     * @param identifier
     *        the identifier
     * @return the item delete statement
     * @throws DatabaseException
     */
    private PreparedStatement getItemDeleteStatement(String identifier) throws DatabaseException {
        PreparedStatement deleteItemStmt = null;
        try {
            deleteItemStmt = getConnection().prepareStatement(DELETE_ITEM_STATEMENT);
            deleteItemStmt.setString(1, identifier);
        } catch (SQLException ex) {
            LOGGER.error("Could not get delete item statement " + deleteItemStmt, ex);
        }
        return deleteItemStmt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertItems(List<ImageItem> toInsert) throws DatabaseException {
        if (toInsert == null) throw new NullPointerException("toInsert");
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.warn("Unable to set autocommit off", e);
        }
        try {
            int updated = 0;
            for (ImageItem item : toInsert) {
                getItemDeleteStatement(item.getIdentifier()).executeUpdate();
                updated += getItemInsertStatement(item).executeUpdate();
            }
            if (updated == toInsert.size()) {
                getConnection().commit();
                LOGGER.debug("DB has been updated.");
            } else {
                getConnection().rollback();
                LOGGER.error("DB has not been updated -> rollback!");
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        } finally {
            closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<String> resolveItems(List<String> oldJpgFsPaths) throws DatabaseException {
        if (oldJpgFsPaths == null) throw new NullPointerException("oldJpgFsPaths");
        ArrayList<String> ret = new ArrayList<String>(oldJpgFsPaths.size());
        for (String oldJpgFsPath : oldJpgFsPaths) {
            ret.add(resolveItem(oldJpgFsPath));
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveItem(String oldJpgFsPath) throws DatabaseException {
        if (oldJpgFsPath == null || "".equals(oldJpgFsPath)) throw new NullPointerException("oldJpgFsPath");
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.warn("Unable to set autocommit off", e);
        }
        PreparedStatement statement = null;
        String ret = null;
        try {
            statement = getConnection().prepareStatement(SELECT_ITEM_STATEMENT);
            statement.setString(1, oldJpgFsPath);
            ResultSet rs = statement.executeQuery();
            int i = 0;
            int id = -1;
            int rowsAffected = 0;
            while (rs.next()) {
                id = rs.getInt("id");
                ret = rs.getString("imageFile");
                i++;
            }
            if (id != -1 && new File(ret).exists()) {
                statement = getConnection().prepareStatement(UPDATE_ITEM_STATEMENT);
                statement.setInt(1, id);
                rowsAffected = statement.executeUpdate();
            } else {
                return null;
            }
            if (rowsAffected == 1) {
                getConnection().commit();
                LOGGER.debug("DB has been updated.");
            } else {
                getConnection().rollback();
                LOGGER.error("DB has not been updated -> rollback!");
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        } finally {
            closeConnection();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<String> cacheAgeingProcess(int numberOfDays) throws DatabaseException {
        IMAGE_LIFETIME = numberOfDays;
        PreparedStatement statement = null;
        ArrayList<String> ret = new ArrayList<String>();
        try {
            statement = getConnection().prepareStatement(SELECT_ITEMS_FOR_DELETION_STATEMENT);
            ResultSet rs = statement.executeQuery();
            int i = 0;
            int rowsAffected = 0;
            while (rs.next()) {
                ret.add(rs.getString("imageFile"));
                i++;
            }
            if (i > 0) {
                statement = getConnection().prepareStatement(DELETE_ITEMS_STATEMENT);
                rowsAffected = statement.executeUpdate();
            }
            if (rowsAffected == i) {
                getConnection().commit();
                LOGGER.debug("DB has been updated.");
                LOGGER.debug(i + " images are going to be removed.");
            } else {
                getConnection().rollback();
                LOGGER.error("DB has not been updated -> rollback!");
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        } finally {
            closeConnection();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOldJpgFsPath(String imageFile) throws DatabaseException {
        if (imageFile == null || "".equals(imageFile)) throw new NullPointerException("imageFile");
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.warn("Unable to set autocommit off", e);
        }
        PreparedStatement statement = null;
        String oldJpgFsPath = null;
        try {
            statement = getConnection().prepareStatement(SELECT_OLD_FS_PATH_STATEMENT);
            String s = "%" + imageFile + "%";
            statement.setString(1, s);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                oldJpgFsPath = rs.getString("old_fs_path");
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        } finally {
            closeConnection();
        }
        return oldJpgFsPath;
    }
}
