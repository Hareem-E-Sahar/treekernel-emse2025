package org.ujorm.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * The default sequence provider.
 * A result value is recieved from a special database table.
 * @author Pavel Ponec
 */
public class UjoSequencer {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoSequencer.class);

    /** Basic table. */
    protected final MetaTable table;

    /** Current sequence value */
    protected long sequence = 0;

    /** Buffer limit */
    protected long seqLimit = 0;

    /** Total limit, zero means no restriction */
    protected long maxValue = 0;

    public UjoSequencer(MetaTable table) {
        this.table = table;
    }

    /** Returns the <strong>next sequence value</strong> by a synchronized method. */
    public synchronized long nextValue(final Session session) {
        if (sequence < seqLimit) {
            return ++sequence;
        } else {
            final MetaDatabase db = MetaTable.DATABASE.of(table);
            Connection connection = null;
            ResultSet res = null;
            String sql = null;
            PreparedStatement statement = null;
            StringBuilder out = new StringBuilder(64);
            try {
                connection = session.getSeqConnection(db);
                String tableName = db.getDialect().printFullTableName(getTable(), true, out).toString();
                out.setLength(0);
                out.setLength(0);
                sql = db.getDialect().printSequenceNextValue(this, out).toString();
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, sql + "; [" + tableName + ']');
                }
                statement = connection.prepareStatement(sql);
                statement.setString(1, tableName);
                int i = statement.executeUpdate();
                if (i == 0) {
                    out.setLength(0);
                    sql = db.getDialect().printSequenceInit(this, out).toString();
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.log(Level.INFO, sql + "; [" + tableName + ']');
                    }
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, tableName);
                    statement.executeUpdate();
                }
                out.setLength(0);
                sql = db.getDialect().printSequenceCurrentValue(this, out).toString();
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, sql + "; [" + tableName + ']');
                }
                statement = connection.prepareStatement(sql);
                statement.setString(1, tableName);
                res = statement.executeQuery();
                res.next();
                seqLimit = res.getLong(1);
                int step = res.getInt(2);
                maxValue = res.getLong(3);
                sequence = (seqLimit - step) + 1;
                if (maxValue != 0L) {
                    if (seqLimit > maxValue) {
                        seqLimit = maxValue;
                        if (sequence > maxValue) {
                            String msg = "The sequence '" + tableName + "' needs to raise the maximum value: " + maxValue;
                            throw new IllegalStateException(msg);
                        }
                        statement.close();
                        sql = db.getDialect().printSetMaxSequence(this, out).toString();
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.log(Level.INFO, sql + "; [" + tableName + ']');
                        }
                        statement = connection.prepareStatement(sql);
                        statement.setString(1, tableName);
                        statement.execute();
                    }
                    if (maxValue > Long.MAX_VALUE - step) {
                        String msg = "The sequence attribute '" + tableName + ".maxValue' is too hight," + " the recommended maximal value is: " + (Long.MAX_VALUE - step) + " (Long.MAX_VALUE-step)";
                        LOGGER.log(Level.WARNING, msg);
                    }
                }
                connection.commit();
            } catch (Throwable e) {
                if (connection != null) try {
                    connection.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Rollback fails");
                }
                IllegalStateException exception = e instanceof IllegalStateException ? (IllegalStateException) e : new IllegalStateException("ILLEGAL SQL: " + sql, e);
                throw exception;
            } finally {
                MetaDatabase.close(null, statement, res, true);
            }
            return sequence;
        }
    }

    /** Returns the database schema */
    public String getDatabaseSchema() {
        return MetaDatabase.SCHEMA.of(getDatabase());
    }

    /** The UJO cache is the number of pre-allocated numbers inside the OrmUjo framework. */
    public int getIncrement() {
        final int result = MetaParams.SEQUENCE_CACHE.of(table.getDatabase().getParams());
        return result;
    }

    /** The cache of a database sequence is zero by default. */
    public int getInitDbCache() {
        return 1;
    }

    /** Returns model of the database */
    public MetaDatabase getDatabase() {
        return MetaTable.DATABASE.of(table);
    }

    /** Returns a related table or null if sequence is general for the all MetaDatabase space */
    public MetaTable getTable() {
        return table;
    }

    /** Method returns true because the internal table 'ujorm_pk_support' is required to get a next sequence value.
     * In case you have a different imlementation, there is possible overwrite this method and return an another value. */
    public boolean isSequenceTableRequired() {
        return true;
    }
}
