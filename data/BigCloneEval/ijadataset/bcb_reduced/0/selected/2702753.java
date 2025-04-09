package org.inigma.migrations;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaInfoTest {

    private static File tempDirectory;

    private static ZipFile tempArchive;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        File zipfile = File.createTempFile("db-", ".zip");
        zipfile.deleteOnExit();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
        out.putNextEntry(new ZipEntry("test/"));
        out.closeEntry();
        File file = File.createTempFile("001_up_", ".sql");
        FileWriter writer = new FileWriter(file);
        String content = "This file has no tokens in it";
        writer.append(content);
        writer.close();
        file.deleteOnExit();
        out.putNextEntry(new ZipEntry("test/" + file.getName()));
        out.write(content.getBytes());
        out.closeEntry();
        file = File.createTempFile("002_up_", ".sql");
        writer = new FileWriter(file);
        content = "This file has #SEVERAL() tokens such as\n #SCHEMA() and #TABLESPACE()\n";
        content += "But also has #SEVERAL() in here #SEVERAL() times on #SEVERAL() lines too.";
        content += "But do not forget about #NUM3R1C4L(6) tokens and #UNDER_SCORE() tokens too!";
        writer.append(content);
        writer.close();
        file.deleteOnExit();
        out.putNextEntry(new ZipEntry("test/" + file.getName()));
        out.write(content.getBytes());
        out.closeEntry();
        for (int i = 3; i < 10; i++) {
            file = File.createTempFile("00" + i + "_up_", ".sql");
            file.deleteOnExit();
            out.putNextEntry(new ZipEntry("test/" + file.getName()));
            out.closeEntry();
            file = File.createTempFile("00" + i + "_down_", ".sql");
            file.deleteOnExit();
            out.putNextEntry(new ZipEntry("test/" + file.getName()));
            out.closeEntry();
        }
        out.close();
        tempDirectory = file.getParentFile();
        tempArchive = new ZipFile(zipfile);
    }

    @Test
    public void testMigrationList() {
        SchemaInfo info = new SchemaInfo(tempDirectory);
        assertNotNull(info);
        Collection<MigrationResource> migrationList = info.getMigrationList(5, 0);
        assertEquals(3, migrationList.size());
        int version = 5;
        for (MigrationResource file : migrationList) {
            assertEquals(version--, file.getVersion());
        }
        migrationList = info.getMigrationList(0, 5);
        assertEquals(5, migrationList.size());
        version = 1;
        for (MigrationResource file : migrationList) {
            assertEquals(version++, file.getVersion());
        }
        migrationList = info.getMigrationList(0, -1);
        assertEquals(9, migrationList.size());
        version = 1;
        for (MigrationResource file : migrationList) {
            assertEquals(version++, file.getVersion());
        }
    }

    @Test
    public void testSchemaInfo() {
        assertTrue(tempDirectory.exists());
        SchemaInfo info = new SchemaInfo(tempDirectory);
        assertNotNull(info);
        assertEquals(tempDirectory.getName(), info.getId());
        assertEquals(5, info.getTokens().size());
    }

    @Test
    public void testZipSchemaInfo() throws MigrationException {
        assertNotNull(tempArchive);
        SchemaInfo info = new SchemaInfo(tempArchive, "test");
        assertNotNull(info);
        assertEquals("test", info.getId());
        assertEquals(5, info.getTokens().size());
    }
}
