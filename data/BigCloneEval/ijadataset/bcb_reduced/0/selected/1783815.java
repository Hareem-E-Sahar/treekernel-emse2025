package net.sourceforge.oracle.jutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import oracle.sql.BLOB;

/**
 *
 * @author asales
 */
public class JdbmsChecksum {

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, null, null, null, null, null, null, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3) throws Exception {
        System.out.println("Using CRC32 for 3 BLOBS overload function.");
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, null, null, null, null, null, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, iBLOB4, null, null, null, null, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4, BLOB iBLOB5) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, iBLOB4, iBLOB5, null, null, null, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4, BLOB iBLOB5, BLOB iBLOB6) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, iBLOB4, iBLOB5, iBLOB6, null, null, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4, BLOB iBLOB5, BLOB iBLOB6, BLOB iBLOB7) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, iBLOB4, iBLOB5, iBLOB6, iBLOB7, null, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4, BLOB iBLOB5, BLOB iBLOB6, BLOB iBLOB7, BLOB iBLOB8) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, iBLOB4, iBLOB5, iBLOB6, iBLOB7, iBLOB8, null, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4, BLOB iBLOB5, BLOB iBLOB6, BLOB iBLOB7, BLOB iBLOB8, BLOB iBLOB9) throws Exception {
        return getCRC32(iBLOB1, iBLOB2, iBLOB3, iBLOB4, iBLOB5, iBLOB6, iBLOB7, iBLOB8, iBLOB9, null);
    }

    public static String getCRC32(BLOB iBLOB1, BLOB iBLOB2, BLOB iBLOB3, BLOB iBLOB4, BLOB iBLOB5, BLOB iBLOB6, BLOB iBLOB7, BLOB iBLOB8, BLOB iBLOB9, BLOB iBLOB10) throws Exception {
        long out;
        try {
            System.out.println("Computing CRC32 on input BLOB(s) ...");
            InputStream stream1 = null;
            InputStream stream2 = null;
            InputStream stream3 = null;
            InputStream stream4 = null;
            InputStream stream5 = null;
            InputStream stream6 = null;
            InputStream stream7 = null;
            InputStream stream8 = null;
            InputStream stream9 = null;
            InputStream stream10 = null;
            if (iBLOB1 == null) {
            } else {
                stream1 = iBLOB1.getBinaryStream();
            }
            if (iBLOB2 == null) {
            } else {
                stream2 = iBLOB2.getBinaryStream();
            }
            if (iBLOB3 == null) {
            } else {
                stream3 = iBLOB3.getBinaryStream();
            }
            if (iBLOB4 == null) {
            } else {
                stream4 = iBLOB4.getBinaryStream();
            }
            if (iBLOB5 == null) {
            } else {
                stream5 = iBLOB5.getBinaryStream();
            }
            if (iBLOB6 == null) {
            } else {
                stream6 = iBLOB6.getBinaryStream();
            }
            if (iBLOB7 == null) {
            } else {
                stream7 = iBLOB7.getBinaryStream();
            }
            if (iBLOB8 == null) {
            } else {
                stream8 = iBLOB8.getBinaryStream();
            }
            if (iBLOB9 == null) {
            } else {
                stream9 = iBLOB9.getBinaryStream();
            }
            if (iBLOB10 == null) {
            } else {
                stream10 = iBLOB10.getBinaryStream();
            }
            out = getCRC32(stream1, stream2, stream3, stream4, stream5, stream6, stream7, stream8, stream9, stream10);
            System.out.println("CRC32 computed.");
            return Long.toHexString(out).toUpperCase();
        } catch (Exception ex) {
            System.err.println("ERROR : Was not able to compute CRC32 :\n" + ex.getMessage());
            System.err.println("Throwing Exception.\nBye.");
            throw ex;
        }
    }

    public static String getCRC32(BLOB iBLOB, int iBlobLength) throws Exception {
        long out;
        System.out.println("About to compute CRC32 from BLOB ...");
        if (iBlobLength == 0) {
            System.out.println("Found NULL or empty BLOB in input. Returning null checksum string");
            return null;
        } else {
            System.out.println("Input BLOB length is : " + iBlobLength);
            System.out.println("Going on with CRC32 computation ...");
        }
        System.out.println("Getting BinaryStream from BLOB ...");
        InputStream inStream = iBLOB.getBinaryStream();
        out = getCRC32(inStream);
        System.out.println("Closing stream ...");
        try {
            inStream.close();
            System.out.println("Stream closed.");
            System.out.println("Returning CRC32 ...");
            System.out.println("Bye.");
            return Long.toHexString(out).toUpperCase();
        } catch (IOException ex) {
            System.err.println("ERROR : Was not able to properly close the stream : " + ex.getMessage());
            System.err.println("Full Stack below :\n");
            ex.printStackTrace();
            System.err.println("Throwing exception ...");
            throw ex;
        }
    }

    public static long getCRC32(InputStream iFile) throws Exception {
        CheckedInputStream cis = null;
        long fileSize = 0;
        cis = new CheckedInputStream(iFile, new CRC32());
        byte[] buf = new byte[128];
        try {
            System.err.println("Fetching bytes from the file ...");
            while (cis.read(buf) >= 0) {
            }
            long checksum = cis.getChecksum().getValue();
            System.out.println("Checksum computed.");
            System.out.println("Returning checksum.");
            System.out.println("Bye.");
            return checksum;
        } catch (IOException ex) {
            System.err.println("ERROR : CRC32 Java computation error : " + ex.getMessage());
            System.err.println("Full Stack below :\n");
            ex.printStackTrace();
            System.err.println("Throwing exception ...");
            throw ex;
        }
    }

    public static byte[] getBytesFromInputStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int BUF_SIZE = 1 << 8;
        byte[] buffer = new byte[BUF_SIZE];
        int bytesRead = -1;
        while ((bytesRead = inStream.read(buffer)) > -1) {
            out.write(buffer, 0, bytesRead);
        }
        inStream.close();
        byte[] outBytes = out.toByteArray();
        return outBytes;
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2) throws IOException {
        return getCRC32(iFile1, iFile2, null, null, null, null, null, null, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, null, null, null, null, null, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, iFile4, null, null, null, null, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4, InputStream iFile5) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, iFile4, iFile5, null, null, null, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4, InputStream iFile5, InputStream iFile6) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, iFile4, iFile5, iFile6, null, null, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4, InputStream iFile5, InputStream iFile6, InputStream iFile7) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, iFile4, iFile5, iFile6, iFile7, null, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4, InputStream iFile5, InputStream iFile6, InputStream iFile7, InputStream iFile8) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, iFile4, iFile5, iFile6, iFile7, iFile8, null, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4, InputStream iFile5, InputStream iFile6, InputStream iFile7, InputStream iFile8, InputStream iFile9) throws IOException {
        return getCRC32(iFile1, iFile2, iFile3, iFile4, iFile5, iFile6, iFile7, iFile8, iFile9, null);
    }

    public static long getCRC32(InputStream iFile1, InputStream iFile2, InputStream iFile3, InputStream iFile4, InputStream iFile5, InputStream iFile6, InputStream iFile7, InputStream iFile8, InputStream iFile9, InputStream iFile10) throws IOException {
        CRC32 crc = new CRC32();
        try {
            if (iFile1 != null) {
                System.out.println("Updating CRC for Stream 1 ...");
                crc.update(getBytesFromInputStream(iFile1));
                System.out.println("Computed.");
            }
            if (iFile2 != null) {
                System.out.println("Updating CRC for Stream 2 ...");
                crc.update(getBytesFromInputStream(iFile2));
                System.out.println("Computed.");
            }
            if (iFile3 != null) {
                System.out.println("Updating CRC for Stream 3 ...");
                crc.update(getBytesFromInputStream(iFile3));
                System.out.println("Computed.");
            }
            if (iFile4 != null) {
                System.out.println("Updating CRC for Stream 4 ...");
                crc.update(getBytesFromInputStream(iFile4));
                System.out.println("Computed.");
            }
            if (iFile5 != null) {
                System.out.println("Updating CRC for Stream 5 ...");
                crc.update(getBytesFromInputStream(iFile5));
                System.out.println("Computed.");
            }
            if (iFile6 != null) {
                System.out.println("Updating CRC for Stream 6 ...");
                crc.update(getBytesFromInputStream(iFile6));
                System.out.println("Computed.");
            }
            if (iFile7 != null) {
                System.out.println("Updating CRC for Stream 7 ...");
                crc.update(getBytesFromInputStream(iFile7));
                System.out.println("Computed.");
            }
            if (iFile8 != null) {
                System.out.println("Updating CRC for Stream 8 ...");
                crc.update(getBytesFromInputStream(iFile8));
                System.out.println("Computed.");
            }
            if (iFile9 != null) {
                System.out.println("Updating CRC for Stream 9 ...");
                crc.update(getBytesFromInputStream(iFile9));
                System.out.println("Computed.");
            }
            if (iFile10 != null) {
                System.out.println("Updating CRC for Stream 10 ...");
                crc.update(getBytesFromInputStream(iFile10));
                System.out.println("Computed.");
            }
            return crc.getValue();
        } catch (IOException ex) {
            System.err.println("ERROR : CRC32 Java computation error : " + ex.getMessage());
            System.err.println("Full Stack below :\n" + ex.getMessage());
            System.err.println("Throwing exception ...\nBye.");
            throw ex;
        }
    }

    public static String getCRC32(String iDirectory, String iFileName) {
        try {
            return getCRC32ThrowsException(iDirectory, iFileName);
        } catch (Exception ex) {
            System.err.println("ERROR : Was unable to compute CRC32 on file : Exception caught.");
            System.err.println("Will return null value in output.");
            System.err.println("Full Stack Trace below :\n");
            ex.printStackTrace();
            return null;
        }
    }

    public static String getCRC32ThrowsException(String iDirectory, String iFileName) throws Exception {
        String iFullFileName = iDirectory + "/" + iFileName;
        System.out.println("About to compute CRC32 checksum on file <" + iFullFileName + ">.");
        try {
            CheckedInputStream cis = null;
            long fileSize = 0;
            try {
                System.out.println("Building CheckedInputStream on input File ...");
                cis = new CheckedInputStream(new FileInputStream(iFullFileName), new CRC32());
                System.out.println("CheckedInputStream built.");
                fileSize = new File(iFullFileName).length();
                System.out.println("File length : " + fileSize);
                System.out.println("Computing CRC32 ...");
                byte[] buf = new byte[128];
                while (cis.read(buf) >= 0) {
                }
                long checksum = cis.getChecksum().getValue();
                System.out.println("CRC32 computed : " + Long.toHexString(checksum));
                System.out.println("Returning Checksum ...");
                System.out.println("Bye.");
                return Long.toHexString(checksum).toUpperCase();
            } catch (FileNotFoundException e) {
                System.err.println("ERROR : CRC32 Java computation error : " + e.getMessage());
                System.err.println("Full Stack below :\n");
                e.printStackTrace();
                System.err.println("Throwing exception ...");
                throw (e);
            }
        } catch (IOException ex) {
            System.err.println("ERROR : CRC32 Java computation error : " + ex.getMessage());
            System.err.println("Full Stack below :\n");
            ex.printStackTrace();
            System.err.println("Throwing exception ...");
            throw ex;
        }
    }

    public static String getCRC32(String iString) {
        String out = null;
        System.out.println("About to compute CRC32 upon the string : <" + iString + ">");
        byte[] bytes = iString.getBytes();
        CRC32 crc = new CRC32();
        crc.update(bytes);
        out = Long.toHexString(crc.getValue()).toUpperCase();
        System.out.println("Computed CRC32 returns : " + out);
        System.out.println("Bye.");
        return out;
    }
}
