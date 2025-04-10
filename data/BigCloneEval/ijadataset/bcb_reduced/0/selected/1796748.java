package org.dbmaintain.script.repository.impl;

import org.dbmaintain.script.Script;
import org.dbmaintain.script.qualifier.Qualifier;
import org.dbmaintain.script.repository.ScriptLocation;
import org.dbmaintain.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;
import static java.io.File.createTempFile;
import static java.util.Collections.singleton;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.io.IOUtils.contentEquals;
import static org.dbmaintain.util.CollectionUtils.asSet;
import static org.dbmaintain.util.CollectionUtils.asSortedSet;

/**
 * @author Filip Neven
 * @author Tim Ducheyne
 */
public class JarScriptLocationTest {

    private SortedSet<Script> scripts;

    private File jarFile;

    @Before
    public void init() throws IOException {
        Script script1 = TestUtils.createScript("folder1/script1.sql", "Script 1 content");
        Script script2 = TestUtils.createScript("folder1/script2.sql", "Script 2 content");
        scripts = asSortedSet(script1, script2);
        jarFile = createTempFile("scriptjar", ".jar");
    }

    @Test
    public void writeToJarThenRereadFromJarAndEnsureContentIsEqual() throws IOException {
        ArchiveScriptLocation originalScriptArchive = new ArchiveScriptLocation(scripts, "ISO-8859-1", "postprocessing", asSet(new Qualifier("qualifier1"), new Qualifier("qualifier2")), singleton(new Qualifier("patch")), "#", "@", asSet("sql", "ddl"), null, false);
        originalScriptArchive.writeToJarFile(jarFile);
        ArchiveScriptLocation scriptArchiveFromFile = new ArchiveScriptLocation(jarFile, "ISO-8859-1", "postprocessing", asSet(new Qualifier("qualifier1"), new Qualifier("qualifier2")), singleton(new Qualifier("patch")), "#", "@", asSet("sql", "ddl"), null, false);
        assertEqualProperties(originalScriptArchive, scriptArchiveFromFile);
        assertEqualScripts(originalScriptArchive.getScripts(), scriptArchiveFromFile.getScripts());
    }

    private void assertEqualScripts(SortedSet<Script> originalScripts, SortedSet<Script> scriptsFromFile) throws IOException {
        Iterator<Script> scriptsFromFileIterator = scriptsFromFile.iterator();
        for (Script originalScript : originalScripts) {
            assertEqualScripts(originalScript, scriptsFromFileIterator.next());
        }
    }

    private void assertEqualScripts(Script originalScript, Script scriptFromFile) throws IOException {
        assertEquals(originalScript.getFileName(), scriptFromFile.getFileName());
        assertTrue(originalScript.getFileLastModifiedAt() - scriptFromFile.getFileLastModifiedAt() < 2000);
        assertTrue(contentEquals(originalScript.getScriptContentHandle().openScriptContentReader(), scriptFromFile.getScriptContentHandle().openScriptContentReader()));
        assertTrue(originalScript.isScriptContentEqualTo(scriptFromFile, true));
    }

    private void assertEqualProperties(ScriptLocation originalScriptJar, ScriptLocation scriptJarFromFile) {
        assertEquals(originalScriptJar.getScriptFileExtensions(), scriptJarFromFile.getScriptFileExtensions());
        assertEquals(originalScriptJar.getTargetDatabasePrefix(), scriptJarFromFile.getTargetDatabasePrefix());
        assertEquals(originalScriptJar.getQualifierPrefix(), scriptJarFromFile.getQualifierPrefix());
        assertEquals(originalScriptJar.getRegisteredQualifiers(), scriptJarFromFile.getRegisteredQualifiers());
        assertEquals(originalScriptJar.getPatchQualifiers(), scriptJarFromFile.getPatchQualifiers());
        assertEquals(originalScriptJar.getPostProcessingScriptDirName(), scriptJarFromFile.getPostProcessingScriptDirName());
        assertEquals(originalScriptJar.getScriptEncoding(), scriptJarFromFile.getScriptEncoding());
    }
}
