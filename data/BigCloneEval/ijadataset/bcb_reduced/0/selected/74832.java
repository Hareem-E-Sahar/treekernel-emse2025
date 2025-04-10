package gov.nasa.worldwind.formats.dted;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.formats.tiff.GeoTiff;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

/**
 * @author Lado Garakanidze
 * @version $Id: DTED.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class DTED {

    protected static final int REC_HEADER_SIZE = 8;

    protected static final int REC_CHKSUM_SIZE = Integer.SIZE / Byte.SIZE;

    protected static final int DTED_UHL_SIZE = 80;

    protected static final int DTED_DSI_SIZE = 648;

    protected static final int DTED_ACC_SIZE = 2700;

    protected static final long DTED_UHL_OFFSET = 0L;

    protected static final long DTED_DSI_OFFSET = DTED_UHL_OFFSET + (long) DTED_UHL_SIZE;

    protected static final long DTED_ACC_OFFSET = DTED_DSI_OFFSET + (long) DTED_DSI_SIZE;

    protected static final long DTED_DATA_OFFSET = DTED_ACC_OFFSET + (long) DTED_ACC_SIZE;

    protected static final int DTED_NODATA_VALUE = -32767;

    protected DTED() {
    }

    protected static RandomAccessFile open(File file) throws IOException, IllegalArgumentException {
        if (null == file) {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!file.exists()) {
            String message = Logging.getMessage("generic.FileNotFound", file.getAbsolutePath());
            Logging.logger().severe(message);
            throw new IOException(message);
        }
        if (!file.canRead()) {
            String message = Logging.getMessage("generic.FileNoReadPermission", file.getAbsolutePath());
            Logging.logger().severe(message);
            throw new IOException(message);
        }
        return new RandomAccessFile(file, "r");
    }

    protected static void close(RandomAccessFile file) {
        if (null != file) {
            try {
                file.close();
            } catch (Exception ex) {
                Logging.logger().finest(ex.getMessage());
            }
        }
    }

    public static AVList readMetadata(File file) throws IOException {
        AVList metadata = null;
        RandomAccessFile sourceFile = null;
        try {
            sourceFile = open(file);
            FileChannel channel = sourceFile.getChannel();
            metadata = new AVListImpl();
            readUHL(channel, DTED_UHL_OFFSET, metadata);
            readDSI(channel, DTED_DSI_OFFSET, metadata);
            readACC(channel, DTED_ACC_OFFSET, metadata);
        } finally {
            close(sourceFile);
        }
        return metadata;
    }

    public static DataRaster read(File file) throws IOException {
        DataRaster raster = null;
        RandomAccessFile sourceFile = null;
        try {
            sourceFile = open(file);
            FileChannel channel = sourceFile.getChannel();
            AVList metadata = new AVListImpl();
            readUHL(channel, DTED_UHL_OFFSET, metadata);
            readDSI(channel, DTED_DSI_OFFSET, metadata);
            readACC(channel, DTED_ACC_OFFSET, metadata);
            raster = readElevations(channel, DTED_DATA_OFFSET, metadata);
        } finally {
            close(sourceFile);
        }
        return raster;
    }

    protected static DataRaster readElevations(FileChannel theChannel, long offset, AVList metadata) throws IOException {
        if (null == theChannel) return null;
        ByteBufferRaster raster = (ByteBufferRaster) ByteBufferRaster.createGeoreferencedRaster(metadata);
        theChannel.position(offset);
        int width = (Integer) metadata.getValue(AVKey.WIDTH);
        int height = (Integer) metadata.getValue(AVKey.HEIGHT);
        int recordSize = REC_HEADER_SIZE + width * Short.SIZE / Byte.SIZE + REC_CHKSUM_SIZE;
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        double nodata = (double) DTED_NODATA_VALUE;
        ByteBuffer bb = ByteBuffer.allocate(recordSize).order(ByteOrder.BIG_ENDIAN);
        for (int x = 0; x < width; x++) {
            theChannel.read(bb);
            bb.flip();
            int dataChkSum = 0;
            for (int i = 0; i < recordSize - REC_CHKSUM_SIZE; i++) {
                dataChkSum += 0xFF & bb.get(i);
            }
            ShortBuffer data = bb.asShortBuffer();
            for (int i = 0; i < height; i++) {
                double elev = (double) data.get(i + 4);
                if (elev != nodata) {
                    min = (elev < min) ? elev : min;
                    max = (elev > max) ? elev : max;
                }
                int y = height - i - 1;
                raster.setDoubleAtPosition(y, x, elev);
            }
            short hi = data.get(width + REC_CHKSUM_SIZE);
            short lo = data.get(width + REC_CHKSUM_SIZE + 1);
            int expectedChkSum = (0xFFFF & hi) << 16 | (0xFFFF & lo);
            if (expectedChkSum != dataChkSum) {
                String message = Logging.getMessage("DTED.DataRecordChecksumError", expectedChkSum, dataChkSum);
                Logging.logger().severe(message);
                throw new IOException(message);
            }
        }
        raster.setValue(AVKey.ELEVATION_MIN, min);
        raster.setValue(AVKey.ELEVATION_MAX, max);
        return raster;
    }

    protected static Angle readAngle(String angle) throws IOException {
        if (null == angle) {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IOException(message);
        }
        StringBuffer sb = new StringBuffer(angle.trim());
        int length = sb.length();
        switch(length) {
            case 7:
                sb.insert(2, " ").insert(5, " ").insert(8, " ");
                return Angle.fromDMS(sb.toString());
            case 8:
                sb.insert(3, " ").insert(6, " ").insert(9, " ");
                return Angle.fromDMS(sb.toString());
            case 9:
                sb.insert(2, " ").insert(5, " ").insert(10, " ");
                sb.delete(8, 10);
                return Angle.fromDMS(sb.toString());
            case 10:
                sb.insert(3, " ").insert(6, " ").insert(11, " ");
                sb.delete(9, 11);
                return Angle.fromDMS(sb.toString());
        }
        return null;
    }

    protected static Integer readLevel(String dtedLevel) {
        if (null == dtedLevel) return null;
        dtedLevel = dtedLevel.trim();
        if (!dtedLevel.startsWith("DTED") || dtedLevel.length() != 5) return null;
        return (dtedLevel.charAt(4) - '0');
    }

    protected static String readClassLevel(String code) {
        if (null != code) {
            code = code.trim();
            if ("U".equalsIgnoreCase(code)) return AVKey.CLASS_LEVEL_UNCLASSIFIED; else if ("R".equalsIgnoreCase(code)) return AVKey.CLASS_LEVEL_RESTRICTED; else if ("C".equalsIgnoreCase(code)) return AVKey.CLASS_LEVEL_CONFIDENTIAL; else if ("S".equalsIgnoreCase(code)) return AVKey.CLASS_LEVEL_SECRET;
        }
        return null;
    }

    protected static void readACC(FileChannel theChannel, long offset, AVList metadata) throws IOException {
        if (null == theChannel) return;
        theChannel.position(offset);
        byte[] acc = new byte[DTED_ACC_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(acc).order(ByteOrder.BIG_ENDIAN);
        theChannel.read(bb);
        bb.flip();
        String id = new String(acc, 0, 3);
        if (!"ACC".equalsIgnoreCase(id)) {
            String reason = Logging.getMessage("DTED.UnexpectedRecordId", id, "ACC");
            String message = Logging.getMessage("DTED.BadFileFormat", reason);
            Logging.logger().severe(message);
            throw new IOException(message);
        }
    }

    protected static void readUHL(FileChannel theChannel, long offset, AVList metadata) throws IOException {
        if (null == theChannel) return;
        theChannel.position(offset);
        byte[] uhl = new byte[DTED_UHL_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(uhl).order(ByteOrder.BIG_ENDIAN);
        theChannel.read(bb);
        bb.flip();
        String id = new String(uhl, 0, 3);
        if (!"UHL".equalsIgnoreCase(id)) {
            String reason = Logging.getMessage("DTED.UnexpectedRecordId", id, "UHL");
            String message = Logging.getMessage("DTED.BadFileFormat", reason);
            Logging.logger().severe(message);
            throw new IOException(message);
        }
        metadata.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);
        metadata.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
        metadata.setValue(AVKey.PROJECTION_EPSG_CODE, GeoTiff.GCS.WGS_84);
        metadata.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
        metadata.setValue(AVKey.DATA_TYPE, AVKey.INT16);
        metadata.setValue(AVKey.ELEVATION_UNIT, AVKey.UNIT_METER);
        metadata.setValue(AVKey.MISSING_DATA_SIGNAL, (double) DTED_NODATA_VALUE);
        metadata.setValue(AVKey.RASTER_PIXEL, AVKey.RASTER_PIXEL_IS_POINT);
        int width = Integer.valueOf(new String(uhl, 47, 4));
        metadata.setValue(AVKey.WIDTH, width);
        int height = Integer.valueOf(new String(uhl, 51, 4));
        metadata.setValue(AVKey.HEIGHT, height);
        double pixelWidth = 1d / ((double) (width - 1));
        metadata.setValue(AVKey.PIXEL_WIDTH, pixelWidth);
        double pixelHeight = 1d / ((double) (height - 1));
        metadata.setValue(AVKey.PIXEL_HEIGHT, pixelHeight);
        Angle lon = readAngle(new String(uhl, 4, 8));
        Angle lat = readAngle(new String(uhl, 12, 8));
        Sector sector = Sector.fromDegrees(lat.degrees - pixelHeight / 2d, lat.degrees + 1d + pixelHeight / 2d, lon.degrees - pixelWidth / 2d, lon.degrees + 1d + pixelWidth / 2d);
        metadata.setValue(AVKey.SECTOR, sector);
        LatLon wwOrigin = LatLon.fromDegrees(sector.getMaxLatitude().degrees, sector.getMinLongitude().degrees);
        metadata.setValue(AVKey.ORIGIN, wwOrigin);
        String classLevel = readClassLevel(new String(uhl, 32, 3));
        if (null != classLevel) metadata.setValue(AVKey.CLASS_LEVEL, classLevel);
    }

    protected static void readDSI(FileChannel theChannel, long offset, AVList metadata) throws IOException {
        if (null == theChannel) return;
        theChannel.position(offset);
        theChannel.position(offset);
        byte[] dsi = new byte[DTED_DSI_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(dsi).order(ByteOrder.BIG_ENDIAN);
        theChannel.read(bb);
        bb.flip();
        String id = new String(dsi, 0, 3);
        if (!"DSI".equalsIgnoreCase(id)) {
            String reason = Logging.getMessage("DTED.UnexpectedRecordId", id, "DSI");
            String message = Logging.getMessage("DTED.BadFileFormat", reason);
            Logging.logger().severe(message);
            throw new IOException(message);
        }
        if (!metadata.hasKey(AVKey.CLASS_LEVEL)) {
            String classLevel = readClassLevel(new String(dsi, 3, 1));
            if (null != classLevel) metadata.setValue(AVKey.CLASS_LEVEL, classLevel);
        }
        Integer level = readLevel(new String(dsi, 59, 5));
        if (null != level) metadata.setValue(AVKey.DTED_LEVEL, level);
    }
}
