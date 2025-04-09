package Utilities;

import java.io.*;
import java.util.zip.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author  David
 */
public class PNGfactory {

    int pngWidth;

    int pngHeight;

    byte pngBpp;

    byte pngColourType;

    byte pngCompression;

    byte pngFilter;

    byte pngInterlace;

    byte pngCompressionType;

    DataOutputStream pngFile;

    ByteArrayOutputStream buffer;

    Deflater zlib;

    DeflaterOutputStream stream;

    /** Creates a new instance of PNGfactory */
    public PNGfactory(DataOutputStream out, int width, int height, byte bpp) {
        pngFile = out;
        pngWidth = width;
        pngHeight = height;
        pngBpp = bpp;
        pngColourType = 0;
        pngCompression = 0;
        pngFilter = 0;
        pngInterlace = 0;
        pngCompressionType = 0;
    }

    public void start() throws IOException {
        pngFile.write(createSignature());
        writeHeader(pngFile);
        int bufferSize = pngHeight;
        bufferSize *= pngWidth;
        bufferSize *= 4;
        buffer = new ByteArrayOutputStream(bufferSize);
        zlib = new Deflater(Deflater.BEST_COMPRESSION);
        stream = new DeflaterOutputStream(buffer, zlib, bufferSize);
    }

    ;

    public void end() throws IOException {
        writeEnd(pngFile);
    }

    ;

    private void writeHeader(DataOutputStream out) throws IOException {
        byte[] header = createHeader();
        out.writeInt((header.length - 4));
        out.write(header);
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(header);
        out.writeInt((int) crc.getValue());
    }

    private byte[] int2ByteArray(int oldint) {
        byte[] convert = new byte[4];
        convert[0] = (byte) ((oldint >> 24) & 0xFF);
        convert[1] = (byte) ((oldint >> 16) & 0xFF);
        convert[2] = (byte) ((oldint >> 8) & 0xFF);
        convert[3] = (byte) (oldint & 0xFF);
        return convert;
    }

    private short[] int2ShortArray(int oldint) {
        short[] convert = new short[4];
        convert[1] = (byte) ((oldint >> 8) & 0xFFFF);
        convert[2] = (byte) ((oldint >> 0) & 0xFFFF);
        return convert;
    }

    private byte[] createHeader() {
        byte[] header = new byte[17];
        byte[] convert;
        header[0] = 'I';
        header[1] = 'H';
        header[2] = 'D';
        header[3] = 'R';
        convert = int2ByteArray(pngWidth);
        header[4] = convert[0];
        header[5] = convert[1];
        header[6] = convert[2];
        header[7] = convert[3];
        convert = int2ByteArray(pngHeight);
        header[8] = convert[0];
        header[9] = convert[1];
        header[10] = convert[2];
        header[11] = convert[3];
        header[12] = pngBpp;
        header[13] = pngColourType;
        header[14] = pngCompression;
        header[15] = pngFilter;
        header[16] = pngInterlace;
        return header;
    }

    ;

    private byte[] createSignature() {
        byte[] signature = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };
        return signature;
    }

    ;

    private void writeEnd(DataOutputStream out) throws IOException {
        stream.finish();
        byte[] dataArray = new byte[buffer.size() + 4];
        dataArray[0] = 'I';
        dataArray[1] = 'D';
        dataArray[2] = 'A';
        dataArray[3] = 'T';
        System.arraycopy(buffer.toByteArray(), 0, dataArray, 4, buffer.size());
        out.writeInt(dataArray.length - 4);
        out.write(dataArray);
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(dataArray);
        out.writeInt((int) crc.getValue());
        byte[] end = createEnd();
        out.writeInt(end.length - 4);
        out.write(end);
        crc = new CRC32();
        crc.reset();
        crc.update(end);
        out.writeInt((int) crc.getValue());
        return;
    }

    private byte[] createEnd() {
        byte[] end = new byte[4];
        end[0] = 'I';
        end[1] = 'E';
        end[2] = 'N';
        end[3] = 'D';
        return end;
    }

    ;

    private void createData(byte[] scanLine) {
        byte[] typeArray = { pngCompressionType };
        try {
            stream.write(typeArray);
            stream.write(scanLine);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void createData(short[] scanline) {
        byte[] byteScanline = new byte[(scanline.length * 2) + 1];
        byteScanline[0] = pngCompressionType;
        for (int i = 0; i < scanline.length; i++) {
            byteScanline[(i * 2) + 1] = (byte) ((scanline[i] >> 8) & 0xFF);
            byteScanline[(i * 2) + 2] = (byte) (scanline[i] & 0xFF);
        }
        try {
            stream.write(byteScanline);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void createPNGFromDoubles(ArrayList data) {
        int value, i, j;
        double tmp;
        try {
            start();
            Iterator pngIterator = data.iterator();
            if (pngBpp == 8) {
                byte[] pngData = new byte[pngWidth];
                for (i = 0; i < pngHeight; i++) {
                    for (j = 0; j < pngWidth; j++) {
                        tmp = ((Double) pngIterator.next()).doubleValue();
                        value = (int) (255.0 * tmp);
                        pngData[j] = (byte) (value & 0xFF);
                    }
                    createData(pngData);
                }
            } else {
                short[] pngData = new short[pngWidth];
                for (i = 0; i < pngHeight; i++) {
                    for (j = 0; j < pngWidth; j++) {
                        tmp = ((Double) pngIterator.next()).doubleValue();
                        value = (int) (65535.0 * tmp);
                        pngData[j] = (short) (value & 0xFFFF);
                    }
                    createData(pngData);
                }
            }
            end();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DataOutputStream out;
        try {
            out = new DataOutputStream(new FileOutputStream("d:\\test.png"));
        } catch (FileNotFoundException e) {
            return;
        }
        ;
        PNGfactory png = new PNGfactory(out, (int) 40, (int) 40, (byte) 8);
        byte[] data = { (byte) 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        try {
            png.start();
            for (int i = 0; i < 40; i++) {
                png.createData(data);
            }
            png.end();
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        ;
        return;
    }
}
