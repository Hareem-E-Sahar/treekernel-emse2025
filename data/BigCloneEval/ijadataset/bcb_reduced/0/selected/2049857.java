package org.matsim.vis.otfvis.data.fileio;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFConnectionManagerFactory;
import org.matsim.vis.otfvis.data.OTFServerQuad2;
import org.matsim.vis.otfvis.data.OTFServerQuadBuilder;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.SnapshotWriter;

/**
 * The OTF has a file Reader and a file Writer part.
 * The writer is in charge of writing mvi data into a file.
 * 
 * @author dstrippgen 
 * @author dgrether
 */
public class OTFFileWriter implements SnapshotWriter {

    private static final Logger log = Logger.getLogger(OTFFileWriter.class);

    private static final int BUFFERSIZE = 300000000;

    private static final int FILE_BUFFERSIZE = 50000000;

    protected OTFServerQuad2 quad = null;

    protected final double interval_s;

    protected double nextTime = -1;

    private ZipOutputStream zos = null;

    private DataOutputStream outFile;

    private boolean isOpen = false;

    private final ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);

    private final OTFConnectionManager connect;

    private String outFileName;

    /**
	 *  minor version increase does not break compatibility
	 */
    public static final int MINORVERSION = 7;

    /**
	 *  the version number should be increased to imply a compatibility break
	 */
    public static final int VERSION = 1;

    public OTFFileWriter(final double intervall_s, OTFServerQuadBuilder quadBuilder, String outfilename, OTFConnectionManagerFactory cmFac) {
        this.interval_s = intervall_s;
        this.connect = cmFac.createConnectionManager();
        this.outFileName = outfilename;
        this.quad = quadBuilder.createAndInitOTFServerQuad(this.connect);
    }

    public void open() {
        this.isOpen = true;
        try {
            this.zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.outFileName), FILE_BUFFERSIZE));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            writeInfos();
            writeQuad();
            writeConstData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeInfos() throws IOException {
        this.zos.putNextEntry(new ZipEntry("info.bin"));
        this.outFile = new DataOutputStream(this.zos);
        this.outFile.writeInt(OTFFileWriter.VERSION);
        this.outFile.writeInt(OTFFileWriter.MINORVERSION);
        this.outFile.writeDouble(this.interval_s);
        this.zos.closeEntry();
    }

    private void writeQuad() throws IOException {
        this.zos.putNextEntry(new ZipEntry("quad.bin"));
        onAdditionalQuadData(this.connect);
        new ObjectOutputStream(this.zos).writeObject(this.quad);
        this.zos.closeEntry();
        this.zos.putNextEntry(new ZipEntry("connect.bin"));
        log.info("writing ConnectionManager to file...");
        this.connect.logEntries();
        new ObjectOutputStream(this.zos).writeObject(this.connect);
        this.zos.closeEntry();
    }

    protected void onAdditionalQuadData(OTFConnectionManager connect) {
    }

    private void writeConstData() throws IOException {
        this.zos.putNextEntry(new ZipEntry("const.bin"));
        this.outFile = new DataOutputStream(this.zos);
        this.buf.position(0);
        this.outFile.writeDouble(-1.);
        this.quad.writeConstData(this.buf);
        this.outFile.writeInt(this.buf.position());
        this.outFile.write(this.buf.array(), 0, this.buf.position());
        this.zos.closeEntry();
    }

    public void close() {
        try {
            this.zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void beginSnapshot(final double time) {
        if (!this.isOpen) open();
        try {
            dump((int) time);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean dump(final int time_s) throws IOException {
        if (time_s >= this.nextTime) {
            writeDynData(time_s);
            this.nextTime = time_s + this.interval_s;
            return true;
        }
        return false;
    }

    private void writeDynData(final int time_s) throws IOException {
        this.zos.putNextEntry(new ZipEntry("step." + time_s + ".bin"));
        this.outFile = new DataOutputStream(this.zos);
        this.buf.position(0);
        this.outFile.writeDouble(time_s);
        this.quad.writeDynData(null, this.buf);
        this.outFile.writeInt(this.buf.position());
        this.outFile.write(this.buf.array(), 0, this.buf.position());
        this.zos.closeEntry();
    }

    public void addAgent(final AgentSnapshotInfo position) {
    }

    public void endSnapshot() {
    }

    public void finish() {
        close();
    }
}
