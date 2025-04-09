package net.sourceforge.oracle.jutils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import oracle.sql.BLOB;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 *
 * @author asales
 */
public class JdbmsCompress extends InternalConnectionManager {

    public static final int BUFFER = 2048;

    public static int DEFAULT_ZIP_COMPRESS_LEVEL = Deflater.DEFAULT_COMPRESSION;

    public static String DEFAULT_ENTRY_NAME_TEMPLATE = "oracle_jutils_jdbms_compress_zip_blob_";

    public static String BZIP2_ALGO_NAME = "bzip2";

    public static String BZIP2_FILENAME_EXTENSION = "bz2";

    public static String TAR_EXTENSION = "tar";

    public static BLOB zipBlob(BLOB iBlob1, String iComment1, String iEntryName1, BLOB iBlob2, String iComment2, String iEntryName2, BLOB iBlob3, String iComment3, String iEntryName3, BLOB iBlob4, String iComment4, String iEntryName4, BLOB iBlob5, String iComment5, String iEntryName5, BLOB iBlob6, String iComment6, String iEntryName6, BLOB iBlob7, String iComment7, String iEntryName7, BLOB iBlob8, String iComment8, String iEntryName8, BLOB iBlob9, String iComment9, String iEntryName9, BLOB iBlob10, String iComment10, String iEntryName10, int iLevel) throws Exception {
        byte data1[] = new byte[BUFFER];
        byte data2[] = new byte[BUFFER];
        byte data3[] = new byte[BUFFER];
        byte data4[] = new byte[BUFFER];
        byte data5[] = new byte[BUFFER];
        byte data6[] = new byte[BUFFER];
        byte data7[] = new byte[BUFFER];
        byte data8[] = new byte[BUFFER];
        byte data9[] = new byte[BUFFER];
        byte data10[] = new byte[BUFFER];
        BLOB out = null;
        String lComment = null;
        int lLevel = DEFAULT_ZIP_COMPRESS_LEVEL;
        String lEntryName1 = null;
        String lEntryName2 = null;
        String lEntryName3 = null;
        String lEntryName4 = null;
        String lEntryName5 = null;
        String lEntryName6 = null;
        String lEntryName7 = null;
        String lEntryName8 = null;
        String lEntryName9 = null;
        String lEntryName10 = null;
        String lComment1 = null;
        String lComment2 = null;
        String lComment3 = null;
        String lComment4 = null;
        String lComment5 = null;
        String lComment6 = null;
        String lComment7 = null;
        String lComment8 = null;
        String lComment9 = null;
        String lComment10 = null;
        if ((iLevel < 0) || (iLevel > 9)) {
            System.err.println("WARNING : Zip Compression Level values must be between 0 and 9 : [0..9]");
            lLevel = DEFAULT_ZIP_COMPRESS_LEVEL;
            System.err.println("WARNING : input Compression Level was <" + iLevel + "> and has been reset to <" + lLevel + ">");
        } else {
            lLevel = iLevel;
            System.out.println("Compression Level set to <" + lLevel + ">");
        }
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            if (iBlob1 != null) {
                if (iEntryName1 == null) {
                    lEntryName1 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName1 + ">");
                } else {
                    lEntryName1 = iEntryName1;
                }
                if (iComment1 == null) {
                    lComment1 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment1 + ">");
                } else {
                    lComment1 = iComment1;
                }
            } else {
            }
            if (iBlob2 != null) {
                if (iEntryName2 == null) {
                    lEntryName2 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName2 + ">");
                } else {
                    lEntryName2 = iEntryName2;
                }
                if (iComment2 == null) {
                    lComment2 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment2 + ">");
                } else {
                    lComment2 = iComment2;
                }
            } else {
            }
            if (iBlob3 != null) {
                if (iEntryName3 == null) {
                    lEntryName3 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName3 + ">");
                } else {
                    lEntryName3 = iEntryName3;
                }
                if (iComment3 == null) {
                    lComment3 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment3 + ">");
                } else {
                    lComment3 = iComment3;
                }
            } else {
            }
            if (iBlob4 != null) {
                if (iEntryName4 == null) {
                    lEntryName4 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName4 + ">");
                } else {
                    lEntryName4 = iEntryName4;
                }
                if (iComment4 == null) {
                    lComment4 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment4 + ">");
                } else {
                    lComment4 = iComment4;
                }
            } else {
            }
            if (iBlob5 != null) {
                if (iEntryName5 == null) {
                    lEntryName5 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName5 + ">");
                } else {
                    lEntryName5 = iEntryName5;
                }
                if (iComment5 == null) {
                    lComment5 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment5 + ">");
                } else {
                    lComment5 = iComment5;
                }
            } else {
            }
            if (iBlob6 != null) {
                if (iEntryName6 == null) {
                    lEntryName6 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName6 + ">");
                } else {
                    lEntryName6 = iEntryName6;
                }
                if (iComment6 == null) {
                    lComment6 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment6 + ">");
                } else {
                    lComment6 = iComment6;
                }
            } else {
            }
            if (iBlob7 != null) {
                if (iEntryName7 == null) {
                    lEntryName7 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName7 + ">");
                } else {
                    lEntryName7 = iEntryName7;
                }
                if (iComment7 == null) {
                    lComment7 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment7 + ">");
                } else {
                    lComment7 = iComment7;
                }
            } else {
            }
            if (iBlob8 != null) {
                if (iEntryName8 == null) {
                    lEntryName8 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName8 + ">");
                } else {
                    lEntryName8 = iEntryName8;
                }
                if (iComment8 == null) {
                    lComment8 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment8 + ">");
                } else {
                    lComment8 = iComment8;
                }
            } else {
            }
            if (iBlob9 != null) {
                if (iEntryName9 == null) {
                    lEntryName9 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName9 + ">");
                } else {
                    lEntryName9 = iEntryName9;
                }
                if (iComment9 == null) {
                    lComment9 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment9 + ">");
                } else {
                    lComment9 = iComment9;
                }
            } else {
            }
            if (iBlob10 != null) {
                if (iEntryName10 == null) {
                    lEntryName10 = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
                    System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName10 + ">");
                } else {
                    lEntryName10 = iEntryName10;
                }
                if (iComment10 == null) {
                    lComment10 = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
                    System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment10 + ">");
                } else {
                    lComment10 = iComment10;
                }
            } else {
            }
            System.out.println("Creating ZipOutputStream ...");
            ZipOutputStream zout = new ZipOutputStream(out.getBinaryOutputStream());
            zout.setMethod(ZipOutputStream.DEFLATED);
            zout.setLevel(lLevel);
            System.out.println("ZipOutputStream created.");
            if (iBlob1 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi1 = new BufferedInputStream(iBlob1.getBinaryStream());
                ZipEntry entry1 = new ZipEntry(lEntryName1);
                zout.putNextEntry(entry1);
                System.out.println("Adding entry 1 <" + lEntryName1 + "> to the zip file ...");
                int count;
                while ((count = buffi1.read(data1, 0, BUFFER)) != -1) {
                    zout.write(data1, 0, count);
                    System.out.println("Read " + count + " more bytes.");
                }
                buffi1.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob2 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi2 = new BufferedInputStream(iBlob2.getBinaryStream());
                ZipEntry entry2 = new ZipEntry(lEntryName2);
                zout.putNextEntry(entry2);
                System.out.println("Adding entry 2 <" + lEntryName2 + "> to the zip file ...");
                int count;
                while ((count = buffi2.read(data2, 0, BUFFER)) != -1) {
                    zout.write(data2, 0, count);
                    System.out.println("Read " + count + " more bytes.");
                }
                buffi2.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob3 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi3 = new BufferedInputStream(iBlob3.getBinaryStream());
                ZipEntry entry3 = new ZipEntry(lEntryName3);
                zout.putNextEntry(entry3);
                System.out.println("Adding entry 3 <" + lEntryName3 + "> to the zip file ...");
                int count;
                while ((count = buffi3.read(data3, 0, BUFFER)) != -1) {
                    zout.write(data3, 0, count);
                }
                buffi3.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob4 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi4 = new BufferedInputStream(iBlob4.getBinaryStream());
                ZipEntry entry4 = new ZipEntry(lEntryName4);
                zout.putNextEntry(entry4);
                System.out.println("Adding entry 4 <" + lEntryName4 + "> to the zip file ...");
                int count;
                while ((count = buffi4.read(data4, 0, BUFFER)) != -1) {
                    zout.write(data4, 0, count);
                }
                buffi4.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob5 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi5 = new BufferedInputStream(iBlob5.getBinaryStream());
                ZipEntry entry5 = new ZipEntry(lEntryName5);
                zout.putNextEntry(entry5);
                System.out.println("Adding entry 5 <" + lEntryName5 + "> to the zip file ...");
                int count;
                while ((count = buffi5.read(data5, 0, BUFFER)) != -1) {
                    zout.write(data5, 0, count);
                }
                buffi5.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob6 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi6 = new BufferedInputStream(iBlob6.getBinaryStream());
                ZipEntry entry6 = new ZipEntry(lEntryName6);
                zout.putNextEntry(entry6);
                System.out.println("Adding entry 6 <" + lEntryName6 + "> to the zip file ...");
                int count;
                while ((count = buffi6.read(data6, 0, BUFFER)) != -1) {
                    zout.write(data6, 0, count);
                }
                buffi6.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob7 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi7 = new BufferedInputStream(iBlob7.getBinaryStream());
                ZipEntry entry7 = new ZipEntry(lEntryName7);
                zout.putNextEntry(entry7);
                System.out.println("Adding entry 7 <" + lEntryName7 + "> to the zip file ...");
                int count;
                while ((count = buffi7.read(data7, 0, BUFFER)) != -1) {
                    zout.write(data7, 0, count);
                }
                buffi7.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob8 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi8 = new BufferedInputStream(iBlob8.getBinaryStream());
                ZipEntry entry8 = new ZipEntry(lEntryName8);
                zout.putNextEntry(entry8);
                System.out.println("Adding entry 8 <" + lEntryName8 + "> to the zip file ...");
                int count;
                while ((count = buffi8.read(data8, 0, BUFFER)) != -1) {
                    zout.write(data8, 0, count);
                }
                buffi8.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob9 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi9 = new BufferedInputStream(iBlob9.getBinaryStream());
                ZipEntry entry9 = new ZipEntry(lEntryName9);
                zout.putNextEntry(entry9);
                System.out.println("Adding entry 9 <" + lEntryName9 + "> to the zip file ...");
                int count;
                while ((count = buffi9.read(data9, 0, BUFFER)) != -1) {
                    zout.write(data9, 0, count);
                }
                buffi9.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            if (iBlob10 != null) {
                zout.setComment(lComment);
                BufferedInputStream buffi10 = new BufferedInputStream(iBlob10.getBinaryStream());
                ZipEntry entry10 = new ZipEntry(lEntryName10);
                zout.putNextEntry(entry10);
                System.out.println("Adding entry 10 <" + lEntryName10 + "> to the zip file ...");
                int count;
                while ((count = buffi10.read(data10, 0, BUFFER)) != -1) {
                    zout.write(data10, 0, count);
                }
                buffi10.close();
            } else {
                System.out.println("Found null BLOB : skipped");
            }
            zout.close();
            System.out.println("Closing ZipOutputStream ...");
            System.out.println("Sending back zipped BLOB ...");
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : I/O While dealing with binary streams on zip output : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }

    public static BLOB unzipBlob(BLOB iBlob) throws Exception {
        BLOB out = null;
        byte data[] = new byte[BUFFER];
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            BufferedInputStream buffi = new BufferedInputStream(iBlob.getBinaryStream());
            ZipInputStream zis = new ZipInputStream(buffi);
            ZipEntry entry;
            int nbEntries = 0;
            int count;
            while ((entry = zis.getNextEntry()) != null) {
                nbEntries++;
                BufferedOutputStream dest = new BufferedOutputStream(out.getBinaryOutputStream());
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                if (nbEntries > 1) {
                    Exception ex = new Exception("JDBMS_COMPRESS.UNZIP_BLOB->BLOB Exception : Cannot unzip a BLOB that contains a zip with more than 1 entry !");
                    throw ex;
                }
            }
            zis.close();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : I/O While dealing with binary streams on zip output : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }

    public static BLOB zipBlob(BLOB iBlob, String iComment, int iLevel, String iEntryName) throws Exception {
        byte data[] = new byte[BUFFER];
        BLOB out = null;
        String lComment = null;
        int lLevel = DEFAULT_ZIP_COMPRESS_LEVEL;
        String lEntryName = null;
        if (iComment == null) {
            lComment = "Compressed by oracle-jutils JDBMS_COMPRESS on " + (new Date());
            System.out.println("WARNING : No comment has been provided. The following one has been auto-generated : <" + lComment + ">");
        } else {
            lComment = iComment;
        }
        if ((iLevel < 0) || (iLevel > 9)) {
            System.err.println("WARNING : Zip Compression Level values must be between 0 and 9 : [0..9]");
            lLevel = DEFAULT_ZIP_COMPRESS_LEVEL;
            System.err.println("WARNING : input Compression Level was <" + iLevel + "> and has been reset to <" + lLevel + ">");
        } else {
            lLevel = iLevel;
            System.out.println("Compression Level set to <" + lLevel + ">");
        }
        if (iEntryName == null) {
            lEntryName = DEFAULT_ENTRY_NAME_TEMPLATE + UUID.randomUUID() + "." + JdbmsLob.BLOB_TO_FILE_EXTENSION;
            System.out.println("WARNING : No Entry Name has been provided, generating one : <" + lEntryName + ">");
        } else {
            lEntryName = iEntryName;
        }
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            System.out.println("Creating ZipOutputStream ...");
            ZipOutputStream zout = new ZipOutputStream(out.getBinaryOutputStream());
            System.out.println("ZipOutputStream created.");
            zout.setComment(lComment);
            zout.setMethod(ZipOutputStream.DEFLATED);
            zout.setLevel(lLevel);
            BufferedInputStream buffi = new BufferedInputStream(iBlob.getBinaryStream());
            ZipEntry entry = new ZipEntry(lEntryName);
            zout.putNextEntry(entry);
            System.out.println("Adding entry <" + lEntryName + "> to the zip file ...");
            int count;
            while ((count = buffi.read(data, 0, BUFFER)) != -1) {
                zout.write(data, 0, count);
            }
            zout.close();
            System.out.println("Closing ZipOutputStream ...");
            buffi.close();
            System.out.println("Sending back zipped BLOB ...");
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : I/O While dealing with binary streams on zip output : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }

    public static BLOB unzipBlobZipEntryToBlob(BLOB iBlob, String iEntryName) throws Exception {
        BLOB out = null;
        byte data[] = new byte[BUFFER];
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            BufferedInputStream buffi = new BufferedInputStream(iBlob.getBinaryStream());
            ZipInputStream zis = new ZipInputStream(buffi);
            ZipEntry entry;
            int nbEntries = 0;
            System.out.println("Will now try to extract the Entry <" + iEntryName + "> from input BLOB ...");
            boolean found = false;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().compareTo(iEntryName) == 0) {
                    found = true;
                    System.out.println("Found requested Entry : " + iEntryName);
                    BufferedOutputStream dest = new BufferedOutputStream(out.getBinaryOutputStream(), BUFFER);
                    int count;
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    continue;
                }
            }
            if (!found) {
                zis.close();
                System.err.println("ERROR : Could not find requestd ZipENtry in the provided BLOB !\n");
                System.err.println("Won't be able to Extract the Entry.\nThrowing Exception...\nBye.");
                throw new Exception("Could not find entry named <" + iEntryName + "> !!!");
            }
            zis.close();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : I/O While dealing with binary streams on zip output : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }

    public static BLOB lzma(BLOB iBLOB, int iBlobLength, String iCmdLine, String iWorkingDir) throws Exception {
        BLOB out = null;
        if (iWorkingDir == null) {
            Exception ex = new Exception("ERROR : provided WOrkind dirrectory is null !");
            throw ex;
        }
        String uniqueId = UUID.randomUUID().toString();
        String iFilename = uniqueId + ".in";
        String inFilename = iWorkingDir + "/" + iFilename;
        String oFilename = uniqueId + ".out";
        String outFilename = iWorkingDir + "/" + oFilename;
        try {
            System.out.println("Dumping BLOB on filesystem working directory...");
            JdbmsLob.blobToFile(iBLOB, iWorkingDir, iFilename, null, iBlobLength);
            System.out.println("BLOB dumped.");
            File inFile = new File(inFilename);
            String cmdLine = iCmdLine + " " + inFilename + " " + outFilename;
            System.out.println("Will use following command line :\n" + cmdLine);
            LZMAWrapper wrap = new LZMAWrapper();
            try {
                System.out.println("Processing temporary working file ...");
                wrap.compress(cmdLine);
                System.out.println("Temporary working file processed.");
                File outFile = new File(outFilename);
                System.out.println("Putting processed lzma stream into BLOB...");
                out = JdbmsLob.fileToBlob(iWorkingDir, oFilename);
                try {
                    System.out.println("Deleting working files ...");
                    inFile.delete();
                    outFile.delete();
                    System.out.println("Working files deleted.");
                    return out;
                } catch (Exception ex) {
                    System.err.println("ERROR : Was not able to delete working files :\n" + ex.getMessage());
                    System.err.println("Could not perform all operations successfully, \noperation will be invalidated.\nBye.");
                    throw ex;
                }
            } catch (Exception ex) {
                System.err.println("ERROR : Unable to process working file :\n" + ex.getMessage());
                System.err.println("Won't be able to transform BLOB with lzma.\n\nBye.");
                throw ex;
            }
        } catch (Exception ex) {
            System.err.println("ERROR : unable to dump BLOB on working directory :\n" + ex.getMessage());
            System.err.println("Won't be able to perform lzma operation on BLOB.\nBye");
            throw ex;
        }
    }

    public static BLOB lzmaBlob(BLOB iBlob) throws Exception {
        byte data[] = new byte[BUFFER];
        BLOB out = null;
        try {
            String iCmdLine = "e -d21 ";
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            BufferedInputStream inStream = new BufferedInputStream(iBlob.getBinaryStream());
            BufferedOutputStream outStream = new BufferedOutputStream(out.getBinaryOutputStream());
            LZMAWrapper wrap = new LZMAWrapper();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }

    public static BLOB unLzmaBlob(BLOB iBlob) throws Exception {
        return null;
    }

    public static BLOB gZipBlob(BLOB iBlob, int bufferSize) throws Exception {
        BLOB out = null;
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            GZIPOutputStream gzos = new GZIPOutputStream(out.getBinaryOutputStream());
            BufferedInputStream in = new BufferedInputStream(iBlob.getBinaryStream());
            byte[] buffer = new byte[bufferSize];
            int i;
            while ((i = in.read(buffer)) >= 0) {
                gzos.write(buffer, 0, i);
            }
            in.close();
            gzos.close();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : SQL Exception caught : " + ex.getMessage());
            System.err.println("Throwing Exception.Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : IOException : " + ex.getMessage());
            System.err.println("Throwing Exception.Bye.");
            throw ex;
        }
    }

    public static BLOB gUnZipBlob(BLOB iBlob, int bufferSize) throws Exception {
        BLOB out = null;
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            GZIPInputStream gzin = new GZIPInputStream(iBlob.getBinaryStream());
            BufferedOutputStream os = new BufferedOutputStream(out.getBinaryOutputStream());
            byte[] buf = new byte[bufferSize];
            int len;
            while ((len = gzin.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            gzin.close();
            os.close();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : SQL Exception caught : " + ex.getMessage());
            System.err.println("Throwing Exception.Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : IOException : " + ex.getMessage());
            System.err.println("Throwing Exception.Bye.");
            throw ex;
        }
    }

    public static BLOB tarDirRecursive(String fileName) throws IOException, FileNotFoundException, Exception {
        BLOB out = null;
        byte data[] = new byte[BUFFER];
        try {
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            File rootDir = new File(fileName);
            TarArchiveOutputStream tos = new TarArchiveOutputStream(out.getBinaryOutputStream());
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            recurseAndTarFiles(rootDir, tos);
            tos.close();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : I/O While dealing with binary streams on zip output : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }

    private static void recurseAndTarFiles(File file, TarArchiveOutputStream tos) throws IOException, FileNotFoundException {
        if (file.isDirectory()) {
            String[] fileNames = file.list();
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    recurseAndTarFiles(new File(file, fileNames[i]), tos);
                    System.out.println(fileNames[i]);
                }
            }
        } else if (!file.getName().endsWith(TAR_EXTENSION)) {
            byte[] buf = new byte[1024];
            int len;
            TarArchiveEntry tarEntry = new TarArchiveEntry(file.toString());
            System.out.println(file.toString());
            tarEntry.setSize(file.length());
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fin);
            tos.putArchiveEntry(tarEntry);
            while ((len = in.read(buf)) != -1) {
                tos.write(buf, 0, len);
            }
            in.close();
            tos.closeArchiveEntry();
        }
    }

    public static BLOB tarDirectory(String iDirectoryName, final String iRegExp) throws IOException, Exception {
        BLOB out = null;
        byte data[] = new byte[BUFFER];
        try {
            File fileDir = new File(iDirectoryName);
            File[] arrFile = fileDir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return (name.toLowerCase().matches(iRegExp));
                }
            });
            System.out.println("Found " + arrFile.length + " files.");
            System.out.println("Trying to get an internal Oracle Connection ...");
            openOracleConnection();
            System.out.println("Internally connected.");
            System.out.println("Internally connected.");
            System.out.println("Creating and opening session temporary BLOB in READ/WRITE mode ...");
            out = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
            out.open(BLOB.MODE_READWRITE);
            System.out.println("Session temporary BLOB opened.");
            TarArchiveOutputStream tos = new TarArchiveOutputStream(out.getBinaryOutputStream());
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            for (int i = 0; i < arrFile.length; i++) {
                File lFile = arrFile[i];
                if (!lFile.isDirectory()) {
                    if (!lFile.getName().endsWith(TAR_EXTENSION)) {
                        byte[] buf = new byte[1024];
                        int len;
                        TarArchiveEntry tarEntry = new TarArchiveEntry(lFile.toString());
                        tarEntry.setSize(lFile.length());
                        FileInputStream fin = new FileInputStream(lFile);
                        BufferedInputStream in = new BufferedInputStream(fin);
                        System.out.println("Adding " + lFile + " entry...");
                        tos.putArchiveEntry(tarEntry);
                        while ((len = in.read(buf)) != -1) {
                            tos.write(buf, 0, len);
                        }
                        in.close();
                        tos.closeArchiveEntry();
                        System.out.println("Entry added.");
                    } else {
                    }
                }
            }
            tos.close();
            return out;
        } catch (SQLException ex) {
            System.err.println("ERROR : Error while working on BLOB : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        } catch (IOException ex) {
            System.err.println("ERROR : I/O While dealing with binary streams on zip output : " + ex.getMessage());
            System.err.println("ERROR : throwing Exception ...");
            System.err.println("ERROR : Bye.");
            throw ex;
        }
    }
}
