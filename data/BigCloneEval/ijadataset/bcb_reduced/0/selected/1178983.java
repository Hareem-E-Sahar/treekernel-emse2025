package net.sourceforge.xuse;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.xuse.utils.FileUtils;
import net.sourceforge.xuse.utils.Utils;

public abstract class AbstractUpgradeTest extends AbstractCommandLineTest {

    protected File upgradeDir = new File(this.getUpgradeDir());

    protected File requirementsFolder = new File(upgradeDir, "src/requirements");

    protected File resourcesFolder = new File(upgradeDir, "src/resources");

    protected File usecaseFolder = new File(upgradeDir, "src/use-cases");

    protected File usecaseAdminFolder = new File(usecaseFolder, "admin");

    protected File usecaseSecurityFolder = new File(usecaseFolder, "security");

    protected static final File XUSE_00_01_00_FILES = new File("src/integrationtests/net/sourceforge/xuse/testfiles/xuse-00-01-00");

    protected static final boolean NO_UPGRADE = false;

    protected static final boolean UPGRADED = true;

    protected final FileFilter archiveFileFilter = new ArchiveFileFilter();

    public AbstractUpgradeTest() {
        upgradeDir.delete();
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XuseMavenCommandTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        upgradeDir.mkdirs();
        assertTrue(upgradeDir.isDirectory());
        assertTrue(upgradeDir.canWrite());
        assertTrue(XUSE_00_01_00_FILES.exists());
        assertTrue(XUSE_00_01_00_FILES.isDirectory());
        FileUtils.copyDirectory(XUSE_00_01_00_FILES, upgradeDir);
        List fileList = this.getUpgradeDirectoryListing();
        assertFalse("Expected to find some files in working directory", fileList.isEmpty());
        assertTrue(fileList.contains("src"));
        this.doTestsOnRequirementsDir(NO_UPGRADE);
        this.doTestsOnUsecaseDir(NO_UPGRADE);
        this.doTestsOnResourcesDir(NO_UPGRADE);
    }

    protected void tearDown() throws Exception {
        assertTrue("Expected working directory to exist", upgradeDir.exists());
        super.tearDown();
    }

    public void testUpgradeDeclineWarning() throws Exception {
        this.execCommandNoBlock(upgradeDir, this.getUpgradeCommand());
        Thread.sleep(5000);
        this.provideInput("n\n");
        String[] results = this.getResults();
        assertContains("Upgrading your project - It is strongly recommended that you back up your files to a safe location before running this command: continue?(y,n)", this.getNormalString(results));
        assertContains("Upgrade aborted by user....", this.getNormalString(results));
        this.assertBuildFailed(results);
        this.doTestsOnRequirementsDir(NO_UPGRADE);
        this.doTestsOnUsecaseDir(NO_UPGRADE);
        this.doTestsOnResourcesDir(NO_UPGRADE);
        results = this.execCommand(upgradeDir, this.getBuildCommand());
        this.assertBuildFailed(results);
    }

    public void testUpgrade() throws Exception {
        this.execCommandNoBlock(upgradeDir, this.getUpgradeCommand());
        Thread.sleep(5000);
        this.provideInput("y\n");
        String[] results = this.getResults();
        assertContains("Upgrading your project - It is strongly recommended that you back up your files to a safe location before running this command: continue?(y,n)", this.getNormalString(results));
        this.assertSuccessfulBuild(results);
        assertEquals("Java did not return correctly", 0, Integer.parseInt(results[2]));
        this.doTestsOnRequirementsDir(UPGRADED);
        this.doTestsOnUsecaseDir(UPGRADED);
        this.doTestsOnResourcesDir(UPGRADED);
        results = this.execCommand(upgradeDir, this.getBuildCommand());
        this.assertSuccessfulBuild(results);
        this.execCommandNoBlock(upgradeDir, this.getUpgradeCommand());
        Thread.sleep(5000);
        this.provideInput("y\n");
        results = this.getResults();
        assertContains("The requirements repository is not in version 0.1 format", getErrorString(results));
        this.doTestsOnRequirementsDir(UPGRADED);
        this.doTestsOnUsecaseDir(UPGRADED);
        this.doTestsOnResourcesDir(UPGRADED);
        results = this.execCommand(upgradeDir, this.getBuildCommand());
        this.assertSuccessfulBuild(results);
    }

    protected void assertSuccessfulBuild(String[] results) throws Exception {
        assertContains("BUILD SUCCESSFUL", getErrorString(results));
    }

    protected void assertBuildFailed(String[] results) throws Exception {
        assertDoesNotContain("BUILD SUCCESSFUL", getErrorString(results));
    }

    private List getDirectoryListing(File dir) throws Exception {
        String[] files = dir.list();
        return Arrays.asList(files);
    }

    protected List getUpgradeDirectoryListing() throws Exception {
        return this.getDirectoryListing(this.upgradeDir);
    }

    protected String getNormalString(String[] results) {
        return results[1];
    }

    protected String getErrorString(String[] results) {
        return results[0];
    }

    protected abstract String getUpgradeCommand();

    protected abstract String getBuildCommand();

    protected String getUpgradeDir() {
        return UPGRADE_DIR_PATH + "_" + Utils.substringAfter(this.getClass().getName(), this.getClass().getPackage().getName() + ".") + "_" + System.currentTimeMillis();
    }

    protected void doTestsOnRequirementsDir(boolean expectUpgrade) throws Exception {
        String[] requirementsFolderList = requirementsFolder.list();
        List listing = Arrays.asList(requirementsFolderList);
        assertTrue("Expected to find stakeholder.xml template file", listing.contains("xuse-requirements.xml"));
        File[] oldFiles = requirementsFolder.listFiles(archiveFileFilter);
        if (expectUpgrade) {
            assertTrue("Expected to find stakeholder.xml template file", listing.contains("stakeholders.xml"));
            assertEquals(1, oldFiles.length);
            assertContains("xuse-requirements", oldFiles[0].getName());
        } else {
            assertFalse("Expected not to find stakeholder.xml template file", listing.contains("stakeholders.xml"));
            assertEquals("Did not expect to find any files, but found some", 0, oldFiles.length);
        }
    }

    protected void doTestsOnUsecaseDir(boolean expectUpgrade) throws Exception {
        String[] usecaseAdminFolderList = usecaseAdminFolder.list();
        String[] usecaseSecurityFolderList = usecaseSecurityFolder.list();
        List listing1 = Arrays.asList(usecaseAdminFolderList);
        List listing2 = Arrays.asList(usecaseSecurityFolderList);
        assertTrue("Expected to find change-personal-details.xml use-case file", listing1.contains("change-personal-details.xml"));
        assertTrue("Expected to find logon.xml use-case file", listing2.contains("logon.xml"));
        File[] oldFiles1 = usecaseAdminFolder.listFiles(archiveFileFilter);
        File[] oldFiles2 = usecaseSecurityFolder.listFiles(archiveFileFilter);
        if (expectUpgrade) {
            assertEquals(1, oldFiles1.length);
            assertContains("change-personal-details", oldFiles1[0].getName());
            assertEquals(1, oldFiles2.length);
            assertContains("logon", oldFiles2[0].getName());
        } else {
            assertEquals("Did not expect to find any files, but found some", 0, oldFiles1.length);
            assertEquals("Did not expect to find any files, but found some", 0, oldFiles2.length);
        }
    }

    protected void doTestsOnResourcesDir(boolean expectUpgrade) throws Exception {
        if (expectUpgrade) {
            assertTrue("Expected to find newly created resources directory", resourcesFolder.exists() && resourcesFolder.isDirectory());
            String[] resourcesList = resourcesFolder.list();
            List listing = Arrays.asList(resourcesList);
            assertTrue("Expected to find glossary.xml use-case file", listing.contains("glossary.xml"));
        } else {
            assertFalse("Did not expect to find resources directory", resourcesFolder.exists());
        }
    }

    private class ArchiveFileFilter implements FileFilter {

        public boolean accept(File file) {
            if (file != null) {
                if (file.isDirectory()) {
                    return false;
                }
                String fileName = file.getName();
                int fileNameLength = fileName.length();
                String extension = fileName.substring(fileNameLength - 4);
                return fileNameLength > 4 && (extension.equalsIgnoreCase(".old"));
            }
            return false;
        }
    }
}
