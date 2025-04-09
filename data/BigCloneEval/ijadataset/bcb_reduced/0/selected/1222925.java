package org.matsim.utils.vis.otfvis.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.matsim.gbl.Gbl;
import org.matsim.mobsim.queuesim.QueueLink;
import org.matsim.mobsim.queuesim.QueueNetwork;
import org.matsim.mobsim.queuesim.QueueNode;
import org.matsim.utils.StringUtils;
import org.matsim.utils.collections.QuadTree.Rect;
import org.matsim.utils.vis.netvis.streaming.SimStateWriterI;
import org.matsim.utils.vis.otfvis.data.OTFConnectionManager;
import org.matsim.utils.vis.otfvis.data.OTFDefaultNetWriterFactoryImpl;
import org.matsim.utils.vis.otfvis.data.OTFNetWriterFactory;
import org.matsim.utils.vis.otfvis.data.OTFServerQuad;
import org.matsim.utils.vis.otfvis.gui.OTFVisConfig;
import org.matsim.utils.vis.otfvis.handler.OTFDefaultNodeHandler;
import org.matsim.utils.vis.otfvis.handler.OTFLinkLanesAgentsNoParkingHandler;
import org.matsim.utils.vis.otfvis.interfaces.OTFServerRemote;
import org.matsim.utils.vis.snapshots.writers.PositionInfo;
import org.matsim.utils.vis.snapshots.writers.SnapshotWriter;

public class OTFQuadFileHandler {

    private static final int BUFFERSIZE = 100000000;

    public static final int VERSION = 1;

    public static final int MINORVERSION = 4;

    public static class Writer implements SimStateWriterI, SnapshotWriter {

        protected final QueueNetwork net;

        protected OTFServerQuad quad = null;

        private final String fileName;

        protected final double interval_s;

        protected double nextTime = -1;

        private ZipOutputStream zos = null;

        private DataOutputStream outFile;

        private boolean isOpen = false;

        private final ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);

        public Writer(final double intervall_s, final QueueNetwork network, final String fileName) {
            this.net = network;
            this.interval_s = intervall_s;
            this.fileName = fileName;
        }

        public boolean dump(final int time_s) throws IOException {
            if (time_s >= this.nextTime) {
                writeDynData(time_s);
                this.nextTime = time_s + this.interval_s;
                return true;
            }
            return false;
        }

        private void writeInfos() throws IOException {
            this.zos.putNextEntry(new ZipEntry("info.bin"));
            this.outFile = new DataOutputStream(this.zos);
            this.outFile.writeInt(VERSION);
            this.outFile.writeInt(MINORVERSION);
            this.outFile.writeDouble(this.interval_s);
            this.zos.closeEntry();
        }

        protected void onAdditionalQuadData() {
        }

        private void writeQuad() throws IOException {
            this.zos.putNextEntry(new ZipEntry("quad.bin"));
            Gbl.startMeasurement();
            this.quad = new OTFServerQuad(this.net);
            System.out.print("build Quad on Server: ");
            Gbl.printElapsedTime();
            onAdditionalQuadData();
            Gbl.startMeasurement();
            OTFConnectionManager connect = new OTFConnectionManager();
            connect.add(QueueLink.class, OTFLinkLanesAgentsNoParkingHandler.Writer.class);
            connect.add(QueueNode.class, OTFDefaultNodeHandler.Writer.class);
            this.quad.fillQuadTree(connect);
            System.out.print("fill writer Quad on Server: ");
            Gbl.printElapsedTime();
            Gbl.startMeasurement();
            new ObjectOutputStream(this.zos).writeObject(this.quad);
            this.zos.closeEntry();
            this.zos.putNextEntry(new ZipEntry("connect.bin"));
            new ObjectOutputStream(this.zos).writeObject(connect);
            this.zos.closeEntry();
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

        public void open() {
            this.isOpen = true;
            try {
                this.zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.fileName), BUFFERSIZE));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            try {
                writeInfos();
                writeQuad();
                writeConstData();
                System.out.print("write to file  Quad on Server: ");
                Gbl.printElapsedTime();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                this.zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void addAgent(final PositionInfo position) {
        }

        public void beginSnapshot(final double time) {
            if (!this.isOpen) open();
            try {
                dump((int) time);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void endSnapshot() {
        }

        public void finish() {
            close();
        }
    }

    public static class Reader implements OTFServerRemote {

        private final String fileName;

        public Reader(final String fname) {
            this.fileName = fname;
            openAndReadInfo();
        }

        private File sourceZipFile = null;

        private DataInputStream inFile;

        protected OTFServerQuad quad = null;

        private final String id = null;

        private byte[] actBuffer = null;

        protected double intervall_s = -1, nextTime = -1;

        TreeMap<Double, Long> timesteps = new TreeMap<Double, Long>();

        private void scanZIPFile(ZipFile zipFile) {
            this.nextTime = -1;
            Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
            System.out.println("Scanning timesteps:");
            Gbl.startMeasurement();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                if (currentEntry.contains("step")) {
                    String[] spliti = StringUtils.explode(currentEntry, '.', 10);
                    double time_s = Double.parseDouble(spliti[1]);
                    if (this.nextTime == -1) this.nextTime = time_s;
                    this.timesteps.put(time_s, entry.getSize());
                    System.out.print(time_s);
                    System.out.print(", ");
                }
            }
            System.out.println("");
            System.out.println("Nr of timesteps: " + this.timesteps.size());
            Gbl.printElapsedTime();
            Gbl.printMemoryUsage();
        }

        private byte[] readTimeStep(final double time_s) throws IOException {
            int time_string = (int) time_s;
            ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
            ZipEntry entry = zipFile.getEntry("step." + time_string + ".bin");
            byte[] buffer = new byte[(int) this.timesteps.get(time_s).longValue()];
            this.inFile = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(entry), 1000000));
            readStateBuffer(buffer);
            zipFile.close();
            return buffer;
        }

        private void openAndReadInfo() {
            try {
                sourceZipFile = new File(this.fileName);
                ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
                ZipEntry infoEntry = zipFile.getEntry("info.bin");
                this.inFile = new DataInputStream(zipFile.getInputStream(infoEntry));
                int version = this.inFile.readInt();
                int minorversion = this.inFile.readInt();
                this.intervall_s = this.inFile.readDouble();
                OTFVisConfig config = (OTFVisConfig) Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);
                config.setFileVersion(version);
                config.setFileMinorVersion(minorversion);
                scanZIPFile(zipFile);
                zipFile.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        static class OTFObjectInputStream extends ObjectInputStream {

            public OTFObjectInputStream(final InputStream in) throws IOException {
                super(in);
            }

            @Override
            protected Class resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                String name = desc.getName();
                System.out.println("try to resolve " + name);
                if (name.equals("playground.david.vis.data.OTFServerQuad")) return OTFServerQuad.class; else if (name.startsWith("playground.david.vis")) {
                    name = name.replaceFirst("playground.david.vis", "org.matsim.utils.vis.otfvis");
                    return Class.forName(name);
                } else if (name.startsWith("org.matsim.utils.vis.otfivs")) {
                    name = name.replaceFirst("org.matsim.utils.vis.otfivs", "org.matsim.utils.vis.otfvis");
                    return Class.forName(name);
                }
                return super.resolveClass(desc);
            }
        }

        private void readQuad() {
            try {
                ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
                ZipEntry quadEntry = zipFile.getEntry("quad.bin");
                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(quadEntry));
                try {
                    this.quad = (OTFServerQuad) new OTFObjectInputStream(is).readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                zipFile.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readConnect(OTFConnectionManager connect) {
            try {
                ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
                ZipEntry connectEntry = zipFile.getEntry("connect.bin");
                if (connectEntry != null) {
                    BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(connectEntry));
                    try {
                        OTFConnectionManager connect2 = (OTFConnectionManager) new OTFObjectInputStream(is).readObject();
                        connect.updateEntries(connect2);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                zipFile.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getLocalTime() throws RemoteException {
            return (int) this.nextTime;
        }

        public void readStateBuffer(final byte[] result) {
            int size = 0;
            try {
                double timenextTime = this.inFile.readDouble();
                size = this.inFile.readInt();
                int offset = 0;
                int remain = size;
                int read = 0;
                while ((remain > 0) && (read != -1)) {
                    read = this.inFile.read(result, offset, remain);
                    remain -= read;
                    offset += read;
                }
                if (offset != size) {
                    throw new IOException("READ SIZE did not fit! File corrupted! in second " + timenextTime);
                }
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }

        public boolean isLive() {
            return false;
        }

        public OTFServerQuad getQuad(final String id, final OTFConnectionManager connect) throws RemoteException {
            if (this.id == null) readQuad();
            if ((id != null) && !id.equals(this.id)) throw new RemoteException("id does not match, set id to NULL will match ALL!");
            return this.quad;
        }

        public byte[] getQuadConstStateBuffer(final String id) throws RemoteException {
            byte[] buffer = null;
            try {
                ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
                ZipEntry entry = zipFile.getEntry("const.bin");
                buffer = new byte[(int) entry.getSize()];
                this.inFile = new DataInputStream(zipFile.getInputStream(entry));
                readStateBuffer(buffer);
                this.inFile.close();
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer;
        }

        public byte[] getQuadDynStateBuffer(final String id, final Rect bounds) throws RemoteException {
            if (this.actBuffer == null) this.actBuffer = getStateBuffer();
            return this.actBuffer;
        }

        public byte[] getStateBuffer() {
            byte[] buffer = null;
            try {
                buffer = readTimeStep(this.nextTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer;
        }

        public boolean requestNewTime(final int time, final TimePreference searchDirection) throws RemoteException {
            double lastTime = -1;
            double foundTime = -1;
            for (Double timestep : this.timesteps.keySet()) {
                if (searchDirection == TimePreference.EARLIER) {
                    if (timestep >= time) {
                        foundTime = lastTime;
                        break;
                    }
                } else if (timestep >= time) {
                    foundTime = timestep;
                    break;
                }
                lastTime = timestep;
            }
            if (foundTime == -1) return false;
            this.nextTime = foundTime;
            this.actBuffer = null;
            return true;
        }

        public Collection<Double> getTimeSteps() {
            return this.timesteps.keySet();
        }
    }
}
