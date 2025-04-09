package com.mindquarry.jcr.jackrabbit;

import java.io.File;
import java.sql.Connection;
import com.mindquarry.dms.xenodot.jackrabbit.XenodotPersistenceManager;

/**
 * This base test class additionally deletes the entire jcr-folder.
 * Deleting the huge amount of files and re-creating the repository is very
 * expensive. So, it is recommended that each test case reverts persistent
 * changes for its own. 
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class JCRTestBase extends JcrSimpleTestBase {

    @Override
    protected void setUp() throws Exception {
        File repoFolder = new File("target/repository");
        removeRepository(repoFolder);
        repoFolder.mkdirs();
        super.setUp();
    }

    private static final String[] xenodotCleanStatements = new String[] { "delete from xenodot.property;", "delete from xenodot.value where reference_id is not null;", "delete from xenodot.node;" };

    protected void cleanXenodotDatabase(XenodotPersistenceManager persistenceManager) throws Exception {
        System.err.println("Start cleaning Xenodot in setUp()");
        Connection connection = persistenceManager.getConnection();
        for (String statement : xenodotCleanStatements) {
            connection.prepareCall(statement).execute();
        }
        System.out.println("Done cleaning Xenodot in setUp()");
    }

    private void removeRepository(File file) {
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        for (File tmp : file.listFiles()) {
            removeRepository(tmp);
        }
        file.delete();
    }
}
