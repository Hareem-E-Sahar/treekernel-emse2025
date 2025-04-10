package org.unitils.dbmaintainer.script.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.unitils.thirdparty.org.apache.commons.io.FileUtils.copyDirectory;
import static org.unitils.thirdparty.org.apache.commons.io.FileUtils.copyFile;
import static org.unitils.thirdparty.org.apache.commons.io.FileUtils.forceDeleteOnExit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.core.UnitilsException;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbmaintainer.script.ExecutedScript;
import org.unitils.dbmaintainer.script.Script;
import org.unitils.dbmaintainer.version.Version;

/**
 * Tests the DefaultScriptSource
 *
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class DefaultScriptSourceTest extends UnitilsJUnit4 {

    @TestDataSource
    DataSource dataSource = null;

    DefaultScriptSource scriptSource;

    String scriptsDirName;

    List<ExecutedScript> alreadyExecutedScripts;

    Date executionDate;

    /**
     * Cleans test directory and copies test files to it. Initializes test objects
     */
    @Before
    public void setUp() throws Exception {
        executionDate = new Date();
        scriptsDirName = System.getProperty("java.io.tmpdir") + "DefaultScriptSourceTest";
        forceDeleteOnExit(new File(scriptsDirName));
        copyDirectory(new File(getClass().getResource("DefaultScriptSourceTest").toURI()), new File(scriptsDirName));
        alreadyExecutedScripts = new ArrayList<ExecutedScript>(asList(getExecutedScript("1_scripts/001_scriptA.sql"), getExecutedScript("1_scripts/002_scriptB.sql"), getExecutedScript("2_scripts/002_scriptE.sql"), getExecutedScript("2_scripts/scriptF.sql"), getExecutedScript("2_scripts/subfolder/001_scriptG.sql"), getExecutedScript("2_scripts/subfolder/scriptH.sql"), getExecutedScript("scripts/001_scriptI.sql"), getExecutedScript("scripts/scriptJ.sql")));
        Properties configuration = new Properties();
        String scriptsLocations = scriptsDirName + "/test_scripts";
        configuration.setProperty(DefaultScriptSource.PROPKEY_SCRIPT_LOCATIONS, scriptsLocations);
        configuration.setProperty(DefaultScriptSource.PROPKEY_SCRIPT_EXTENSIONS, "sql");
        configuration.setProperty(DefaultScriptSource.PROPKEY_POSTPROCESSINGSCRIPT_DIRNAME, "postprocessing");
        configuration.setProperty(DefaultScriptSource.PROPKEY_USESCRIPTFILELASTMODIFICATIONDATES, "false");
        scriptSource = new DefaultScriptSource();
        scriptSource.init(configuration);
    }

    private ExecutedScript getExecutedScript(String scriptFileName) throws NoSuchAlgorithmException, IOException {
        return new ExecutedScript(new Script(scriptFileName, 0L, getCheckSum(scriptFileName)), executionDate, true);
    }

    private String getCheckSum(String fileName) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        InputStream is = new DigestInputStream(new FileInputStream(scriptsDirName + "/test_scripts/" + fileName), digest);
        while (is.read() != -1) ;
        return getHexPresentation(digest.digest());
    }

    private String getHexPresentation(byte[] byteArray) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            result.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    /**
     * Tests getting all scripts in the correct order.
     */
    @Test
    public void testGetAllUpdateScripts() {
        List<Script> scripts = scriptSource.getAllUpdateScripts();
        assertEquals("1_scripts/001_scriptA.sql", scripts.get(0).getFileName());
        assertEquals("1_scripts/002_scriptB.sql", scripts.get(1).getFileName());
        assertEquals("1_scripts/scriptD.sql", scripts.get(2).getFileName());
        assertEquals("2_scripts/002_scriptE.sql", scripts.get(3).getFileName());
        assertEquals("2_scripts/scriptF.sql", scripts.get(4).getFileName());
        assertEquals("2_scripts/subfolder/001_scriptG.sql", scripts.get(5).getFileName());
        assertEquals("2_scripts/subfolder/scriptH.sql", scripts.get(6).getFileName());
        assertEquals("scripts/001_scriptI.sql", scripts.get(7).getFileName());
        assertEquals("scripts/scriptJ.sql", scripts.get(8).getFileName());
    }

    @Test
    public void testDuplicateIndex() throws Exception {
        File duplicateIndexScript = null;
        try {
            File scriptA = new File(scriptsDirName + "/test_scripts/1_scripts/001_scriptA.sql");
            duplicateIndexScript = new File(scriptsDirName + "/test_scripts/1_scripts/001_duplicateIndexScript.sql");
            copyFile(scriptA, duplicateIndexScript);
            try {
                scriptSource.getAllUpdateScripts();
                fail("Expected a UnitilsException because of a duplicate script");
            } catch (UnitilsException e) {
            }
        } finally {
            try {
                duplicateIndexScript.delete();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Tests getting all scripts that have an index higher than the highest of the already executed scripts or
     * whose content has changed.
     */
    @Test
    public void testGetNewScripts() {
        alreadyExecutedScripts.set(5, new ExecutedScript(new Script("2_scripts/subfolder/scriptH.sql", 0L, "xxx"), executionDate, true));
        List<Script> scripts = scriptSource.getNewScripts(new Version("2.x.1"), new HashSet<ExecutedScript>(alreadyExecutedScripts));
        assertEquals("1_scripts/scriptD.sql", scripts.get(0).getFileName());
        assertEquals("2_scripts/subfolder/scriptH.sql", scripts.get(1).getFileName());
        assertEquals("scripts/001_scriptI.sql", scripts.get(2).getFileName());
    }

    @Test
    public void testIsExistingScriptsModfied_noModifications() {
        assertFalse(scriptSource.isExistingIndexedScriptModified(new Version("x.x.x"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
    }

    @Test
    public void testIsExistingScriptsModfied_modifiedScript() {
        alreadyExecutedScripts.set(1, new ExecutedScript(new Script("1_scripts/002_scriptB.sql", 0L, "xxx"), executionDate, true));
        assertTrue(scriptSource.isExistingIndexedScriptModified(new Version("x.x.x"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
    }

    @Test
    public void testIsExistingScriptsModfied_scriptAdded() {
        alreadyExecutedScripts.remove(1);
        assertTrue(scriptSource.isExistingIndexedScriptModified(new Version("x.x.x"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
    }

    @Test
    public void testIsExistingScriptsModfied_scriptRemoved() {
        alreadyExecutedScripts.add(new ExecutedScript(new Script("1_scripts/003_scriptB.sql", 0L, "xxx"), executionDate, true));
        assertTrue(scriptSource.isExistingIndexedScriptModified(new Version("x.x.x"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
    }

    @Test
    public void testIsExistingScriptsModfied_newScript() {
        alreadyExecutedScripts.remove(1);
        assertFalse(scriptSource.isExistingIndexedScriptModified(new Version("1.1"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
    }

    @Test
    public void testIsExistingScriptsModfied_higherIndexScriptModified() {
        alreadyExecutedScripts.set(1, new ExecutedScript(new Script("1_scripts/002_scriptB.sql", 0L, "xxx"), executionDate, true));
        assertFalse(scriptSource.isExistingIndexedScriptModified(new Version("1.1"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
        assertTrue(scriptSource.isExistingIndexedScriptModified(new Version("1.2"), new HashSet<ExecutedScript>(alreadyExecutedScripts)));
    }

    /**
     * Test whether an existing script was modified script but all scripts have a higher version than the current version.
     */
    @Test
    public void testIsExistingScriptsModfied_noLowerIndex() {
        boolean result = scriptSource.isExistingIndexedScriptModified(new Version("0"), new HashSet<ExecutedScript>(alreadyExecutedScripts));
        assertFalse(result);
    }

    /**
     * Test getting the post processing scripts.
     */
    @Test
    public void testGetPostProcessingScripts() {
        List<Script> scripts = scriptSource.getPostProcessingScripts();
        assertEquals("postprocessing/post-scriptA.sql", scripts.get(0).getFileName());
        assertEquals("postprocessing/post-scriptB.sql", scripts.get(1).getFileName());
    }
}
