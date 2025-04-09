package com.spoledge.audao.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This is output as a zipped stream.
 */
public class ZipStreamOutput implements Output {

    private ZipOutputStream zipOutputStream;

    private ZipEntry zipEntry;

    private OutputStreamWriter streamWriter;

    public ZipStreamOutput(OutputStream os) throws IOException {
        this(new ZipOutputStream(os));
    }

    public ZipStreamOutput(ZipOutputStream zos) throws IOException {
        this.zipOutputStream = zos;
        this.streamWriter = new OutputStreamWriter(zos, "UTF-8");
    }

    /**
     * Adds a XSLT result.
     */
    public Result addResult(String resultName) throws IOException {
        newEntry(resultName);
        return new StreamResult(streamWriter);
    }

    /**
     * Adds a plain stream.
     */
    public OutputStream addStream(String resultName) throws IOException {
        newEntry(resultName);
        return zipOutputStream;
    }

    /**
     * Finishes output.
     */
    public void finish() throws IOException {
        if (zipEntry != null) {
            streamWriter.flush();
            zipOutputStream.closeEntry();
        }
        zipOutputStream.finish();
    }

    private void newEntry(String resultName) throws IOException {
        if (zipEntry != null) {
            streamWriter.flush();
            zipOutputStream.closeEntry();
        }
        zipEntry = new ZipEntry(resultName);
        zipOutputStream.putNextEntry(zipEntry);
    }
}
