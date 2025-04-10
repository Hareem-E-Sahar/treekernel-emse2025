package org.dita.dost.util;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.dita.dost.TestUtils;
import org.dita.dost.util.DITAOTCopy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDITAOTCopy {

    private static final File resourceDir = new File("test-stub", TestDITAOTCopy.class.getSimpleName());

    private static final File srcDir = new File(resourceDir, "src");

    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(TestDITAOTCopy.class);
    }

    @Test
    public void testexecute() throws BuildException, IOException {
        final File myFile = new File(tempDir, "testbuild.xml");
        FileUtils.copyFile(new File(srcDir, "testbuild.xml"), myFile);
        final File mydestFile = new File(tempDir, "testbuildaaa.xml");
        final DITAOTCopy ditaotcopy = new DITAOTCopy();
        ditaotcopy.setProject(new Project());
        ditaotcopy.setIncludes(myFile.getPath());
        ditaotcopy.setTodir(tempDir.getPath());
        ditaotcopy.setRelativePaths(mydestFile.getName());
        ditaotcopy.execute();
        assertEquals(TestUtils.readFileToString(myFile), TestUtils.readFileToString(mydestFile));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
}
