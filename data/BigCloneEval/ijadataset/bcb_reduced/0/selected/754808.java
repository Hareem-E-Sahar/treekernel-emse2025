package net.sourceforge.javautil.database.encryption;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.javautil.common.BackupUtil;
import net.sourceforge.javautil.common.IOUtil;
import net.sourceforge.javautil.common.encryption.EncryptionIOHandler;
import net.sourceforge.javautil.common.encryption.IEncryptionProvider;
import net.sourceforge.javautil.common.exception.ThrowableManagerRegistry;
import net.sourceforge.javautil.common.io.IVirtualDirectory;
import net.sourceforge.javautil.common.io.IVirtualFile;

/**
 * This will provide encryption/decryption for tables.
 *
 * @author elponderador
 * @author $Author: ponderator $
 * @version $Id: EncryptedMemoryTableManager.java 1149 2009-10-21 21:34:58Z ponderator $
 */
public abstract class EncryptedTableManager {

    protected static Logger log = LoggerFactory.getLogger(EncryptedTableManager.class);

    protected IEncryptionProvider provider;

    protected DataSource dataSource;

    protected IVirtualDirectory storageDirectory;

    public EncryptedTableManager(IEncryptionProvider provider, DataSource dataSource, IVirtualDirectory storageDirectory) {
        this.provider = provider;
        this.dataSource = dataSource;
        this.storageDirectory = storageDirectory;
    }

    /**
	 * This will get the list of tables for a connection and tag all those that need to be encrypted/unencrypted.
	 * 
	 * @param connection The connection for which to detect the tables
	 * @throws SQLException
	 */
    public abstract List<EncryptedTable> getEncryptedTables(Connection connection);

    /**
	 * Save all MEMORY tables to encrypted storage.
	 */
    public void save() {
        Connection connection = null;
        ZipOutputStream zip = null;
        try {
            connection = dataSource.getConnection();
            IVirtualFile storage = BackupUtil.rotate(this.storageDirectory, "backup", "dat", 20);
            storage.setIOHandler(new EncryptionIOHandler(provider));
            zip = new ZipOutputStream(storage.getOutputStream());
            PrintWriter writer = new PrintWriter(zip, true);
            for (EncryptedTable table : getEncryptedTables(connection)) {
                ZipEntry entry = new ZipEntry(table.tableName + ".dat");
                zip.putNextEntry(entry);
                table.save(connection, writer);
                writer.flush();
                zip.closeEntry();
            }
            zip.flush();
            zip.close();
        } catch (Exception e) {
            if (zip != null) {
                try {
                    zip.close();
                    BackupUtil.reverseRotation(this.storageDirectory, "backup", "dat", 20);
                } catch (Exception ee) {
                    throw ThrowableManagerRegistry.caught(ee);
                }
            }
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                ThrowableManagerRegistry.caught(e);
            }
        }
    }

    /**
	 * Clear and load all MEMORY tables from latest storage.
	 */
    public void load() {
        Connection connection = null;
        ZipInputStream is = null;
        try {
            connection = dataSource.getConnection();
            for (int i = 1; i <= 20; i++) {
                try {
                    IVirtualFile storage = this.storageDirectory.getFile(BackupUtil.getRotatedFileName("backup", "dat", 1));
                    if (storage == null) return;
                    storage.setIOHandler(new EncryptionIOHandler(provider));
                    is = new ZipInputStream(storage.getInputStream());
                    ZipEntry entry = null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while ((entry = is.getNextEntry()) != null) {
                        EncryptedTable found = null;
                        for (EncryptedTable table : getEncryptedTables(connection)) {
                            if (entry.getName().equals(table.tableName + ".dat")) {
                                found = table;
                                break;
                            }
                        }
                        if (found == null) log.warn("Could not find table for entry: " + entry.getName()); else {
                            found.load(connection, reader);
                            is.closeEntry();
                        }
                    }
                    is.close();
                    return;
                } catch (Exception e) {
                    if (is != null) try {
                        is.close();
                    } catch (IOException ee) {
                        ThrowableManagerRegistry.caught(ee);
                    }
                    log.warn("Backup " + i + " failed.", e);
                    BackupUtil.reverseRotation(this.storageDirectory, "backup", "dat", 20);
                }
            }
            throw new RuntimeException("Could not restore encrypted memory tables");
        } catch (SQLException e) {
            throw ThrowableManagerRegistry.caught(e);
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                ThrowableManagerRegistry.caught(e);
            }
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                ThrowableManagerRegistry.caught(e);
            }
        }
    }
}
