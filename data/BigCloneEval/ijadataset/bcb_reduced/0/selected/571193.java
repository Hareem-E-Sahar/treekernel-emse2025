package org.opendedup.sdfs.filestore;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.opendedup.sdfs.Main;
import org.opendedup.util.CompressionUtils;
import org.opendedup.util.EncryptUtils;
import org.opendedup.util.SDFSLogger;
import org.opendedup.util.StringUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author Sam Silverberg The S3 chunk store implements the AbstractChunkStore
 *         and is used to store deduped chunks to AWS S3 data storage. It is
 *         used if the aws tag is used within the chunk store config file. It is
 *         important to make the chunk size very large on the client when using
 *         this chunk store since S3 charges per http request.
 * 
 */
public class S3ChunkStore implements AbstractChunkStore {

    private static AWSCredentials awsCredentials = null;

    private String name;

    private S3ServicePool pool = null;

    private boolean closed = false;

    private long currentLength = 0L;

    private RandomAccessFile posRaf = null;

    private static File chunk_location = new File(Main.chunkStore);

    boolean compress = false;

    boolean encrypt = false;

    static {
        try {
            awsCredentials = new AWSCredentials(Main.awsAccessKey, Main.awsSecretKey);
        } catch (Exception e) {
            SDFSLogger.getLog().fatal("Unable to authenticate to AWS", e);
            System.exit(-1);
        }
    }

    public static boolean checkAuth(String awsAccessKey, String awsSecretKey) {
        AWSCredentials creds = null;
        try {
            creds = new AWSCredentials(awsAccessKey, awsSecretKey);
            RestS3Service s3Service = new RestS3Service(creds);
            s3Service.listAllBuckets();
            return true;
        } catch (Exception e) {
            SDFSLogger.getLog().fatal("Unable to authenticate to AWS", e);
            return false;
        }
    }

    public static boolean checkBucketUnique(String awsAccessKey, String awsSecretKey, String bucketName) {
        AWSCredentials creds = null;
        try {
            creds = new AWSCredentials(awsAccessKey, awsSecretKey);
            RestS3Service s3Service = new RestS3Service(creds);
            S3Bucket s3Bucket = s3Service.getBucket(bucketName);
            if (s3Bucket == null) {
                s3Bucket = s3Service.createBucket(bucketName);
            }
            return true;
        } catch (Exception e) {
            SDFSLogger.getLog().fatal("Unable to create aws bucket", e);
            return false;
        }
    }

    public S3ChunkStore() {
    }

    private void openPosFile() throws IOException {
        File posFile = new File(chunk_location + File.separator + name + ".pos");
        boolean newPos = true;
        if (posFile.exists()) newPos = false; else {
            posFile.getParentFile().mkdirs();
        }
        posRaf = new RandomAccessFile(posFile, "rw");
        if (!newPos) {
            posRaf.seek(0);
            this.currentLength = posRaf.readLong();
        } else {
            posRaf.seek(0);
            posRaf.writeLong(currentLength);
        }
    }

    public long bytesRead() {
        return 0;
    }

    public long bytesWritten() {
        return 0;
    }

    public void close() {
    }

    public void expandFile(long length) throws IOException {
    }

    public byte[] getChunk(byte[] hash, long start, int len) throws IOException {
        String hashString = this.getHashName(hash);
        RestS3Service s3Service = null;
        try {
            s3Service = pool.borrowObject();
            S3Object obj = s3Service.getObject(this.name, hashString);
            byte[] data = new byte[(int) obj.getContentLength()];
            DataInputStream in = new DataInputStream(obj.getDataInputStream());
            in.readFully(data);
            obj.closeDataInputStream();
            if (this.encrypt) data = EncryptUtils.decrypt(data);
            if (this.compress) data = CompressionUtils.decompressZLIB(data);
            return data;
        } catch (Exception e) {
            SDFSLogger.getLog().error("unable to fetch block [" + hash + "]", e);
            throw new IOException("unable to read " + hashString);
        } finally {
            pool.returnObject(s3Service);
        }
    }

    public String getName() {
        return this.name;
    }

    public long reserveWritePosition(int len) throws IOException {
        if (this.closed) throw new IOException("ChunkStore is closed");
        return 1;
    }

    public void setName(String name) {
    }

    public long size() {
        return 0;
    }

    public void writeChunk(byte[] hash, byte[] chunk, int len, long start) throws IOException {
        String hashString = this.getHashName(hash);
        S3Object s3Object = new S3Object(hashString);
        if (Main.awsCompress) {
            chunk = CompressionUtils.compressZLIB(chunk);
            s3Object.addMetadata("compress", "true");
        } else {
            s3Object.addMetadata("compress", "false");
        }
        if (Main.chunkStoreEncryptionEnabled) {
            chunk = EncryptUtils.encrypt(chunk);
            s3Object.addMetadata("encrypt", "true");
        } else {
            s3Object.addMetadata("encrypt", "false");
        }
        ByteArrayInputStream s3IS = new ByteArrayInputStream(chunk);
        s3Object.setDataInputStream(s3IS);
        s3Object.setContentType("binary/octet-stream");
        s3Object.setContentLength(s3IS.available());
        RestS3Service s3Service = null;
        try {
            s3Service = pool.borrowObject();
            s3Service.putObject(this.name, s3Object);
        } catch (Exception e) {
            SDFSLogger.getLog().fatal("unable to upload " + hashString, e);
            throw new IOException(e);
        } finally {
            pool.returnObject(s3Service);
            s3IS.close();
        }
    }

    public static void main(String[] args) throws IOException {
    }

    @Override
    public void deleteChunk(byte[] hash, long start, int len) throws IOException {
        String hashString = this.getHashName(hash);
        RestS3Service s3Service = null;
        try {
            s3Service = pool.borrowObject();
            s3Service.deleteObject(this.name, hashString);
        } catch (Exception e) {
            SDFSLogger.getLog().warn("Unable to delete object " + hashString, e);
        } finally {
            pool.returnObject(s3Service);
        }
    }

    public static void deleteBucket(String bucketName, String awsAccessKey, String awsSecretKey) {
        try {
            System.out.println("");
            System.out.print("Deleting Bucket [" + bucketName + "]");
            AWSCredentials bawsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);
            S3Service bs3Service = new RestS3Service(bawsCredentials);
            S3Object[] obj = bs3Service.listObjects(bucketName);
            for (int i = 0; i < obj.length; i++) {
                bs3Service.deleteObject(bucketName, obj[i].getKey());
                System.out.print(".");
            }
            bs3Service.deleteBucket(bucketName);
            SDFSLogger.getLog().info("Bucket [" + bucketName + "] deleted");
            System.out.println("Bucket [" + bucketName + "] deleted");
        } catch (ServiceException e) {
            SDFSLogger.getLog().warn("Unable to delete bucket " + bucketName, e);
        }
    }

    private String getHashName(byte[] hash) throws IOException {
        if (Main.chunkStoreEncryptionEnabled) {
            byte[] encH = EncryptUtils.encrypt(hash);
            return StringUtils.getHexString(encH);
        } else {
            return StringUtils.getHexString(hash);
        }
    }

    @Override
    public void addChunkStoreListener(AbstractChunkStoreListener listener) {
    }

    @Override
    public void claimChunk(byte[] hash, long start) throws IOException {
    }

    @Override
    public boolean moveChunk(byte[] hash, long origLoc, long newLoc) throws IOException {
        return false;
    }

    @Override
    public void init(Element config) throws IOException {
        this.name = Main.awsBucket;
        try {
            pool = new S3ServicePool(S3ChunkStore.awsCredentials, Main.writeThreads * 2);
            RestS3Service s3Service = pool.borrowObject();
            S3Bucket s3Bucket = s3Service.getBucket(this.name);
            if (s3Bucket == null) {
                s3Bucket = s3Service.createBucket(this.name);
                if (Main.awsCompress) {
                    s3Bucket.addMetadata("compress", "true");
                } else {
                    s3Bucket.addMetadata("compress", "false");
                }
                if (Main.chunkStoreEncryptionEnabled) {
                    s3Bucket.addMetadata("encrypt", "true");
                } else {
                    s3Bucket.addMetadata("encrypt", "false");
                }
                SDFSLogger.getLog().info("created new store " + name);
            }
            if (s3Bucket.containsMetadata("compress")) this.compress = Boolean.parseBoolean((String) s3Bucket.getMetadata("compress")); else this.compress = Main.awsCompress;
            if (s3Bucket.containsMetadata("encrypt")) this.encrypt = Boolean.parseBoolean((String) s3Bucket.getMetadata("encrypt")); else this.encrypt = Main.chunkStoreEncryptionEnabled;
            this.openPosFile();
            pool.returnObject(s3Service);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setSize(long size) {
    }

    @Override
    public ChunkData getNextChunck() throws IOException {
        return null;
    }

    @Override
    public void iterationInit() {
    }
}
