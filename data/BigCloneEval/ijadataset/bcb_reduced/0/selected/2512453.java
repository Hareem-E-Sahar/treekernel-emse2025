package org.openremote.controller.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Assert;

/**
 * State-testing mockup agent, to check that the update-controller command does what
 * we want.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class UpdateMockupAgent extends MockupAgent {

    enum State {

        GET_COMMAND, DOWNLOAD_UPDATE, SHUTDOWN_TOMCAT, BACKUP_WAR, DELETE_PREVIOUS_WAR, INSTALL_WAR, START_TOMCAT, ACK_COMMAND, SUCCESS
    }

    UpdateMockupAgent.State state = State.GET_COMMAND;

    public UpdateMockupAgent() throws AgentException {
        makeMockDir(deployPath);
    }

    @Override
    protected RESTCall makeRESTCall(String method, String url) {
        switch(state) {
            case ACK_COMMAND:
                Assert.assertEquals("DELETE", method);
                Assert.assertEquals("http://fake-backend/beehive/rest/command-queue/1", url);
                state = State.SUCCESS;
                return new MockRESTCall();
        }
        throw new AssertionError("Invalid state: " + state);
    }

    @Override
    protected RESTCall makeRESTCall(String url) throws AgentException {
        switch(state) {
            case GET_COMMAND:
                Assert.assertEquals("http://fake-backend/beehive/rest/user/user/command-queue", url);
                state = State.DOWNLOAD_UPDATE;
                return new MockRESTCall("{'commands':{'command':{'@resource':'http://fake-backend/beehive/rest/user/stef/resources/update-1','@type':'update-controller','id':1}}}");
            case DOWNLOAD_UPDATE:
                Assert.assertEquals("http://fake-backend/beehive/rest/user/stef/resources/update-1", url);
                state = State.SHUTDOWN_TOMCAT;
                File war = makeMockWar();
                return new MockRESTCall(war);
        }
        throw new AssertionError("Invalid state: " + state);
    }

    private File makeMockWar() {
        try {
            File war = File.createTempFile("test", ".war");
            war.deleteOnExit();
            ZipOutputStream os = new ZipOutputStream(new FileOutputStream(war));
            ZipEntry entry = new ZipEntry("file1");
            os.putNextEntry(entry);
            os.write(new byte[] { 0, 1, 2 });
            entry = new ZipEntry("dir/file2");
            os.putNextEntry(entry);
            os.write(new byte[] { 0, 1, 2, 3 });
            os.flush();
            os.close();
            return war;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void shutdownTomcat() throws AgentException {
        if (state != State.SHUTDOWN_TOMCAT) throw new AssertionError("Invalid state: " + state);
        state = State.BACKUP_WAR;
    }

    @Override
    protected void startTomcat() throws AgentException {
        if (state != State.START_TOMCAT) throw new AssertionError("Invalid state: " + state);
        state = State.ACK_COMMAND;
    }

    @Override
    protected void backupPreviousWar() throws AgentException {
        if (state != State.BACKUP_WAR) throw new AssertionError("Invalid state: " + state);
        state = State.DELETE_PREVIOUS_WAR;
        super.backupPreviousWar();
        File backupDir = new File(backupPath);
        Assert.assertEquals(1, backupDir.listFiles().length);
        File backupWar = backupDir.listFiles()[0];
        checkZippedMockDir(backupWar);
    }

    @Override
    protected void deletePreviousWar() throws AgentException {
        if (state != State.DELETE_PREVIOUS_WAR) throw new AssertionError("Invalid state: " + state);
        state = State.INSTALL_WAR;
        super.deletePreviousWar();
        File deployDir = new File(deployPath);
        Assert.assertEquals(0, deployDir.listFiles().length);
    }

    @Override
    protected void installWar(File war) throws AgentException {
        if (state != State.INSTALL_WAR) throw new AssertionError("Invalid state: " + state);
        state = State.START_TOMCAT;
        super.installWar(war);
        File deployDir = new File(deployPath);
        Assert.assertEquals(2, deployDir.listFiles().length);
        File entry = new File(deployDir, "file1");
        Assert.assertNotNull(entry);
        Assert.assertEquals(3, entry.length());
        entry = new File(deployDir, "dir/file2");
        Assert.assertNotNull(entry);
        Assert.assertEquals(4, entry.length());
    }
}
