package org.exist.validation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.exist.storage.BrokerPool;
import org.exist.validation.internal.DatabaseResources;

/**
 *
 * @author wessels
 */
public class DatabaseTools {

    private DatabaseResources dr = null;

    private BrokerPool brokerPool = null;

    /** Local logger */
    private static final Logger logger = Logger.getLogger(DatabaseResources.class);

    /** Creates a new instance of DatabaseTools */
    public DatabaseTools(BrokerPool pool) {
        this.brokerPool = pool;
        dr = new DatabaseResources(pool);
    }

    public byte[] readFile(File file) {
        byte result[] = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            fis.close();
            baos.close();
            result = baos.toByteArray();
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        return result;
    }
}
