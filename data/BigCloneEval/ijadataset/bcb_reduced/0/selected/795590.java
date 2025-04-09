package org.vosao.business.impl.imex.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vosao.business.Business;
import org.vosao.business.imex.task.TaskTimeoutException;
import org.vosao.business.imex.task.ZipOutStreamTaskAdapter;

public class ZipOutStreamTaskAdapterImpl implements ZipOutStreamTaskAdapter {

    protected static final Log logger = LogFactory.getLog(ZipOutStreamTaskAdapterImpl.class);

    private static final int TASK_DURATION = 60 * 9;

    private Business business;

    private ByteArrayOutputStream outData;

    private ZipOutputStream outStream;

    private String startFile;

    private String currentFile;

    private boolean started;

    private boolean entryOpen;

    private int fileCounter;

    public ZipOutStreamTaskAdapterImpl(Business aBusiness, ZipOutputStream out, ByteArrayOutputStream anOutData) {
        this(aBusiness);
        out = outStream;
        outData = anOutData;
    }

    public ZipOutStreamTaskAdapterImpl(Business aBusiness) {
        business = aBusiness;
        started = false;
        entryOpen = false;
        fileCounter = 0;
    }

    @Override
    public ZipOutputStream getOutStream() {
        return outStream;
    }

    @Override
    public void setOutStream(ZipOutputStream out) {
        this.outStream = out;
    }

    @Override
    public String getStartFile() {
        return startFile;
    }

    @Override
    public void setStartFile(String startFile) {
        this.startFile = startFile;
        if (startFile == null) {
            started = true;
        }
    }

    @Override
    public String getCurrentFile() {
        return currentFile;
    }

    @Override
    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    @Override
    public boolean isSkip(String filePath) {
        if (started) {
            return false;
        }
        if (startFile == null) {
            started = true;
            return false;
        }
        if (filePath.equals(startFile)) {
            started = true;
            return false;
        }
        return true;
    }

    private void checkTimeout() throws TaskTimeoutException {
        if (business.getSystemService().getRequestCPUTimeSeconds() > TASK_DURATION) {
            throw new TaskTimeoutException();
        }
    }

    @Override
    public void putNextEntry(ZipEntry entry) throws IOException, TaskTimeoutException {
        currentFile = entry.getName();
        if (!isSkip(entry.getName())) {
            checkTimeout();
            getOutStream().putNextEntry(entry);
            entryOpen = true;
        }
    }

    @Override
    public void closeEntry() throws IOException {
        if (started && entryOpen) {
            getOutStream().closeEntry();
            entryOpen = false;
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        if (started) {
            getOutStream().write(data);
        }
    }

    @Override
    public ByteArrayOutputStream getOutData() {
        return outData;
    }

    @Override
    public void setOutData(ByteArrayOutputStream outData) {
        this.outData = outData;
    }

    public int getFileCounter() {
        return fileCounter;
    }

    public void setFileCounter(int fileCounter) {
        this.fileCounter = fileCounter;
    }

    public void nextFile() {
        fileCounter++;
    }
}
