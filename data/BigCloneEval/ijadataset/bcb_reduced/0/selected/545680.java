package jazsync.jazsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import org.jarsync.ChecksumPair;
import org.jarsync.Configuration;
import org.jarsync.Generator;
import org.jarsync.JarsyncProvider;

/**
 * Target file making class
 * @author Tomáš Hlavnička
 */
public class FileMaker {

    private MetaFileReader mfr;

    private HttpConnection http;

    private ChainingHash hashtable;

    private Configuration config;

    private int bufferOffset;

    private long fileOffset;

    private long[] fileMap;

    private SHA1 sha;

    private int missing;

    private boolean rangeQueue;

    private double complete;

    private String inputFileName;

    private DecimalFormat df = new DecimalFormat("#.##");

    public FileMaker(String[] args) {
        mfr = new MetaFileReader(args);
        hashtable = mfr.getHashtable();
        fileMap = new long[mfr.getBlockCount()];
        Arrays.fill(fileMap, -1);
        fileOffset = 0;
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        df.setRoundingMode(RoundingMode.DOWN);
        inputFileName = mfr.getInputFile();
        if (inputFileName == null) {
            inputFileName = mfr.getFilename();
        }
        if (mfr.FILE_FLAG == 1) {
            mapMatcher();
            if (complete > 0) {
                fileMaker();
            } else {
                getWholeFile();
            }
        } else if (mfr.FILE_FLAG == -1) {
            getWholeFile();
        } else {
            System.out.println("");
            System.exit(1);
        }
    }

    /**
     * Opens a working connection
     */
    private void openConnection() {
        if (mfr.getUrl().startsWith("http://")) {
            http = new HttpConnection(mfr.getUrl());
        } else {
            String url;
            if (mfr.getMetaFileURL().startsWith("http://")) {
                url = urlParser(mfr.getMetaFileURL());
            } else {
                url = urlParser(mfr.getRelativeURL());
            }
            http = new HttpConnection(url);
        }
        http.openConnection();
        if (mfr.getAuthentication()) {
            http.setAuthentication(mfr.getUsername(), mfr.getPassword());
        }
    }

    /**
     * Downloads a whole file in case that there were no relevant data found
     */
    private void getWholeFile() {
        openConnection();
        System.out.println("No relevant data found, downloading whole file.");
        if (new File(mfr.getFilename()).exists()) {
            new File(mfr.getFilename()).delete();
        }
        http.getFile(mfr.getLength(), mfr.getFilename());
        System.out.println("Target 100.0% complete.");
        sha = new SHA1(mfr.getFilename());
        if (sha.SHA1sum().equals(mfr.getSha1())) {
            System.out.println("verifying download...checksum matches OK");
            System.out.println("used 0 local, fetched " + mfr.getLength());
            System.exit(0);
        } else {
            System.out.println("verifying download...checksum don't match");
            System.exit(1);
        }
        http.closeConnection();
    }

    /**
     * URL parser, in case that metafile contains relative path
     * @param link an URL to parse
     * @return Absolute URL
     */
    private String urlParser(String link) {
        String newUrl = null;
        try {
            URL url = new URL(link);
            String host = url.getHost().toString();
            String pathToFile = url.getPath().toString();
            pathToFile = pathToFile.substring(0, pathToFile.lastIndexOf("/"));
            newUrl = "http://" + host + pathToFile + "/" + mfr.getUrl();
        } catch (MalformedURLException ex) {
            System.out.println("URL in malformed format, make sure that" + " metafile contains absolute URL or pass URL of metafile" + " to jazsync by -u parameter.");
            System.exit(1);
        }
        return newUrl;
    }

    /**
     * Method for completing file
     */
    private void fileMaker() {
        try {
            long allData = 0;
            double a = 10;
            int range = 0;
            int blockLength = 0;
            File newFile = new File(mfr.getFilename() + ".part");
            if (newFile.exists()) {
                newFile.delete();
            }
            ArrayList<DataRange> rangeList = null;
            byte[] data = null;
            newFile.createNewFile();
            ByteBuffer buffer = ByteBuffer.allocate(mfr.getBlocksize());
            FileChannel rChannel = new FileInputStream(inputFileName).getChannel();
            FileChannel wChannel = new FileOutputStream(newFile, true).getChannel();
            System.out.println();
            System.out.print("File completion: ");
            System.out.print("|----------|");
            openConnection();
            http.getResponseHeader();
            for (int i = 0; i < fileMap.length; i++) {
                fileOffset = fileMap[i];
                if (fileOffset != -1) {
                    rChannel.read(buffer, fileOffset);
                    buffer.flip();
                    wChannel.write(buffer);
                    buffer.clear();
                } else {
                    if (!rangeQueue) {
                        rangeList = rangeLookUp(i);
                        range = rangeList.size();
                        openConnection();
                        http.setRangesRequest(rangeList);
                        http.sendRequest();
                        http.getResponseHeader();
                        data = http.getResponseBody(mfr.getBlocksize());
                        allData += http.getAllTransferedDataLength();
                    }
                    if ((i * mfr.getBlocksize() + mfr.getBlocksize()) < mfr.getLength()) {
                        blockLength = mfr.getBlocksize();
                    } else {
                        blockLength = (int) ((int) (mfr.getBlocksize()) + (mfr.getLength() - (i * mfr.getBlocksize() + mfr.getBlocksize())));
                    }
                    buffer.put(data, (range - rangeList.size()) * mfr.getBlocksize(), blockLength);
                    buffer.flip();
                    wChannel.write(buffer);
                    buffer.clear();
                    rangeList.remove(0);
                    if (rangeList.isEmpty()) {
                        rangeQueue = false;
                    }
                }
                if ((((double) i / ((double) fileMap.length - 1)) * 100) >= a) {
                    progressBar(((double) i / ((double) fileMap.length - 1)) * 100);
                    a += 10;
                }
            }
            newFile.setLastModified(getMTime());
            sha = new SHA1(newFile);
            if (sha.SHA1sum().equals(mfr.getSha1())) {
                System.out.println("\nverifying download...checksum matches OK");
                System.out.println("used " + (mfr.getLength() - (mfr.getBlocksize() * missing)) + " " + "local, fetched " + (mfr.getBlocksize() * missing));
                new File(mfr.getFilename()).renameTo(new File(mfr.getFilename() + ".zs-old"));
                newFile.renameTo(new File(mfr.getFilename()));
                allData += mfr.getLengthOfMetafile();
                System.out.println("really downloaded " + allData);
                double overhead = ((double) (allData - (mfr.getBlocksize() * missing)) / ((double) (mfr.getBlocksize() * missing))) * 100;
                System.out.println("overhead: " + df.format(overhead) + "%");
            } else {
                System.out.println("\nverifying download...checksum don't match");
                System.out.println("Deleting temporary file");
                newFile.delete();
                System.exit(1);
            }
        } catch (IOException ex) {
            System.out.println("Can't read or write, check your permissions.");
            System.exit(1);
        }
    }

    /**
     * Instead of downloading single blocks, we can look into fieMap and collect
     * amount of missing blocks or end of map accurs. Single ranges are stored in
     * ArrayList
     * @param i Offset in fileMap where to start looking
     * @return ArrayList with ranges for requesting
     */
    private ArrayList<DataRange> rangeLookUp(int i) {
        ArrayList<DataRange> ranges = new ArrayList<DataRange>();
        for (; i < fileMap.length; i++) {
            if (fileMap[i] == -1) {
                ranges.add(new DataRange(i * mfr.getBlocksize(), (i * mfr.getBlocksize()) + mfr.getBlocksize()));
            }
            if (ranges.size() == mfr.getRangesNumber()) {
                break;
            }
        }
        if (!ranges.isEmpty()) {
            rangeQueue = true;
        }
        return ranges;
    }

    /**
     * Parsing out date from metafile into long value
     * @return Time as long value in milliseconds passed since 1.1.1970
     */
    private long getMTime() {
        long mtime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
            Date date = sdf.parse(mfr.getMtime());
            mtime = date.getTime();
        } catch (ParseException e) {
            System.out.println("Metafile is containing a wrong time format. " + "Using today's date.");
            Date today = new Date();
            mtime = today.getTime();
        }
        return mtime;
    }

    /**
     * Reads file and map it's data into the fileMap.
     */
    private void mapMatcher() {
        InputStream is = null;
        try {
            Security.addProvider(new JarsyncProvider());
            config = new Configuration();
            config.strongSum = MessageDigest.getInstance("MD4");
            config.weakSum = new Rsum();
            config.blockLength = mfr.getBlocksize();
            config.strongSumLength = mfr.getChecksumBytes();
            Generator gen = new Generator(config);
            int weakSum;
            byte[] strongSum;
            byte[] backBuffer = new byte[mfr.getBlocksize()];
            byte[] blockBuffer = new byte[mfr.getBlocksize()];
            byte[] fileBuffer;
            int mebiByte = 1048576;
            if (mfr.getLength() < mebiByte && mfr.getBlocksize() < mfr.getLength()) {
                fileBuffer = new byte[(int) mfr.getLength()];
            } else if (mfr.getBlocksize() > mfr.getLength() || mfr.getBlocksize() > mebiByte) {
                fileBuffer = new byte[mfr.getBlocksize()];
            } else {
                fileBuffer = new byte[mebiByte];
            }
            is = new FileInputStream(inputFileName);
            File test = new File(inputFileName);
            long fileLength = test.length();
            int n;
            byte newByte;
            boolean firstBlock = true;
            int len = fileBuffer.length;
            boolean end = false;
            System.out.print("Reading " + inputFileName + ": ");
            System.out.print("|----------|");
            double a = 10;
            boolean found = false;
            int skip = 0;
            while (true) {
                n = is.read(fileBuffer, 0, len);
                if (firstBlock) {
                    weakSum = gen.generateWeakSum(fileBuffer, 0);
                    bufferOffset = mfr.getBlocksize();
                    if (hashLookUp(updateWeakSum(weakSum), null)) {
                        strongSum = gen.generateStrongSum(fileBuffer, 0, mfr.getBlocksize());
                        found = hashLookUp(updateWeakSum(weakSum), strongSum);
                        if (found) {
                            skip = mfr.getBlocksize() - 1;
                            found = false;
                        }
                    }
                    fileOffset++;
                    firstBlock = false;
                }
                for (; bufferOffset < fileBuffer.length; bufferOffset++) {
                    newByte = fileBuffer[bufferOffset];
                    if (fileOffset + mfr.getBlocksize() > fileLength) {
                        newByte = 0;
                    }
                    weakSum = gen.generateRollSum(newByte);
                    if (skip == 0) {
                        if (hashLookUp(updateWeakSum(weakSum), null)) {
                            if (fileOffset + mfr.getBlocksize() > fileLength) {
                                if (n > 0) {
                                    Arrays.fill(fileBuffer, n, fileBuffer.length, (byte) 0);
                                } else {
                                    int offset = fileBuffer.length - mfr.getBlocksize() + bufferOffset + 1;
                                    System.arraycopy(fileBuffer, offset, blockBuffer, 0, fileBuffer.length - offset);
                                    Arrays.fill(blockBuffer, fileBuffer.length - offset, blockBuffer.length, (byte) 0);
                                }
                            }
                            if ((bufferOffset - mfr.getBlocksize() + 1) < 0) {
                                if (n > 0) {
                                    System.arraycopy(backBuffer, backBuffer.length + bufferOffset - mfr.getBlocksize() + 1, blockBuffer, 0, mfr.getBlocksize() - bufferOffset - 1);
                                    System.arraycopy(fileBuffer, 0, blockBuffer, mfr.getBlocksize() - bufferOffset - 1, bufferOffset + 1);
                                }
                                strongSum = gen.generateStrongSum(blockBuffer, 0, mfr.getBlocksize());
                                found = hashLookUp(updateWeakSum(weakSum), strongSum);
                            } else {
                                strongSum = gen.generateStrongSum(fileBuffer, bufferOffset - mfr.getBlocksize() + 1, mfr.getBlocksize());
                                found = hashLookUp(updateWeakSum(weakSum), strongSum);
                            }
                            if (found) {
                                skip = mfr.getBlocksize() - 1;
                                found = false;
                            }
                        }
                    } else {
                        skip--;
                    }
                    fileOffset++;
                    if ((((double) fileOffset / (double) fileLength) * 100) >= a) {
                        progressBar(((double) fileOffset / (double) fileLength) * 100);
                        a += 10;
                    }
                    if (fileOffset == fileLength) {
                        end = true;
                        break;
                    }
                }
                System.arraycopy(fileBuffer, fileBuffer.length - mfr.getBlocksize(), backBuffer, 0, mfr.getBlocksize());
                bufferOffset = 0;
                if (end) {
                    break;
                }
            }
            System.out.println();
            complete = matchControl();
            System.out.println("Target " + df.format(complete) + "% complete.");
            fileMap[fileMap.length - 1] = -1;
            is.close();
        } catch (IOException ex) {
            System.out.println("Can't read seed file, check your permissions");
            System.exit(1);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Problem with MD4 checksum");
            System.exit(1);
        }
    }

    /**
     * Shorten the calculated weakSum according to variable length of weaksum
     * @param weak Generated full weakSum
     * @return Shortened weakSum
     */
    private int updateWeakSum(int weak) {
        byte[] rsum;
        switch(mfr.getRsumBytes()) {
            case 2:
                rsum = new byte[] { (byte) 0, (byte) 0, (byte) (weak >> 24), (byte) ((weak << 8) >> 24) };
                break;
            case 3:
                rsum = new byte[] { (byte) ((weak << 8) >> 24), (byte) 0, (byte) ((weak << 24) >> 24), (byte) (weak >> 24) };
                break;
            case 4:
                rsum = new byte[] { (byte) (weak >> 24), (byte) ((weak << 8) >> 24), (byte) ((weak << 16) >> 24), (byte) ((weak << 24) >> 24) };
                break;
            default:
                rsum = new byte[4];
        }
        int weakSum = 0;
        weakSum += (rsum[0] & 0x000000FF) << 24;
        weakSum += (rsum[1] & 0x000000FF) << 16;
        weakSum += (rsum[2] & 0x000000FF) << 8;
        weakSum += (rsum[3] & 0x000000FF);
        return weakSum;
    }

    /**
     * Method is used to draw a progress bar of
     * how far we are in file.
     * @param i How much data we already progressed (value in percents)
     */
    private void progressBar(double i) {
        if (i >= 10) {
            for (int b = 0; b < 11; b++) {
                System.out.print("\b");
            }
        }
        if (i >= 10 && i < 20) {
            System.out.print("#---------|");
        } else if (i >= 20 && i < 30) {
            System.out.print("##--------|");
        } else if (i >= 30 && i < 40) {
            System.out.print("###-------|");
        } else if (i >= 40 && i < 50) {
            System.out.print("####------|");
        } else if (i >= 50 && i < 60) {
            System.out.print("#####-----|");
        } else if (i >= 60 && i < 70) {
            System.out.print("######----|");
        } else if (i >= 70 && i < 80) {
            System.out.print("#######---|");
        } else if (i >= 80 && i < 90) {
            System.out.print("########--|");
        } else if (i >= 90 & i < 100) {
            System.out.print("#########-|");
        } else if (i >= 100) {
            System.out.print("##########|");
        }
    }

    /**
     * Clears non-matching blocks and returns percentage
     * value of how complete is our file
     * @return How many percent of file we have already
     */
    private double matchControl() {
        missing = 0;
        for (int i = 0; i < fileMap.length; i++) {
            if (mfr.getSeqNum() == 2) {
                if (i > 0 && i < fileMap.length - 1) {
                    if (fileMap[i - 1] == -1 && fileMap[i] != -1 && fileMap[i + 1] == -1) {
                        fileMap[i] = -1;
                    }
                } else if (i == 0) {
                    if (fileMap[i] != -1 && fileMap[i + 1] == -1) {
                        fileMap[i] = -1;
                    }
                } else if (i == fileMap.length - 1) {
                    if (fileMap[i] != -1 && fileMap[i - 1] == -1) {
                        fileMap[i] = -1;
                    }
                }
            }
            if (fileMap[i] == -1) {
                missing++;
            }
        }
        return ((((double) fileMap.length - missing) / (double) fileMap.length) * 100);
    }

    /**
     * Looks into hash table and check if got a hit
     * @param weakSum Weak rolling checksum
     * @param strongSum Strong MD4 checksum
     * @return True if we got a hit
     */
    private boolean hashLookUp(int weakSum, byte[] strongSum) {
        ChecksumPair p;
        if (strongSum == null) {
            p = new ChecksumPair(weakSum);
            ChecksumPair link = hashtable.find(p);
            if (link != null) {
                return true;
            }
        } else {
            p = new ChecksumPair(weakSum, strongSum);
            ChecksumPair link = hashtable.findMatch(p);
            int seq;
            if (link != null) {
                seq = link.getSequence();
                fileMap[seq] = fileOffset;
                hashtable.delete(new ChecksumPair(weakSum, strongSum, mfr.getBlocksize() * seq, mfr.getBlocksize(), seq));
                return true;
            }
        }
        return false;
    }
}
