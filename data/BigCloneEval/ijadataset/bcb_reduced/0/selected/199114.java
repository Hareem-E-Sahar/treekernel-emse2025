package org.tranche.flatfile;

import org.tranche.commons.RandomUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.tranche.annotations.Todo;
import org.tranche.configuration.ConfigKeys;
import org.tranche.configuration.Configuration;
import org.tranche.flatfile.logs.DataBlockUtilLog;
import org.tranche.hash.*;
import org.tranche.meta.MetaData;
import org.tranche.meta.MetaDataUtil;
import org.tranche.users.UserZipFile;
import org.tranche.util.*;

/**
 * Tests that the DataBlock object correctly stores and retrieves data.
 * @author Bryan Smith - bryanesmith@gmail.com
 */
public class DataBlockTest extends TrancheTestCase {

    public void testLogDeletions() throws Exception {
        TestUtil.printTitle("DataBlockTest:testLogDeletions()");
        testLogDeletions(true);
    }

    public void testNoLogDeletions() throws Exception {
        TestUtil.printTitle("DataBlockTest:testNoLogDeletions()");
        testLogDeletions(false);
    }

    public void testLogDeletions(boolean shouldLog) throws Exception {
        FlatFileTrancheServer ffts = null;
        File dataDir = null;
        try {
            dataDir = TempFileUtil.createTemporaryDirectory("testLogDeletions" + (shouldLog ? "Logging" : "NotLogging"));
            ffts = new FlatFileTrancheServer(dataDir);
            ffts.setConfigurationCacheTime(0);
            ffts.getConfiguration().setValue(ConfigKeys.DATABLOCK_LOG_DATA_CHUNK_DELETIONS, String.valueOf(shouldLog));
            ffts.getConfiguration().setValue(ConfigKeys.DATABLOCK_LOG_META_DATA_CHUNK_DELETIONS, String.valueOf(shouldLog));
            DataBlockUtil dbu = ffts.getDataBlockUtil();
            int totalDataToAdd = 10;
            int totalMetaDataToAdd = 10;
            Set<BigHash> dataChunksKept = new HashSet();
            Set<BigHash> dataChunksDeleted = new HashSet();
            Set<BigHash> metaDataChunksKept = new HashSet();
            Set<BigHash> metaDataChunksDeleted = new HashSet();
            for (int i = 0; i < totalDataToAdd; i++) {
                byte[] bytes = DevUtil.createRandomDataChunkVariableSize();
                BigHash hash = new BigHash(bytes);
                if (i % 2 == 0) {
                    if (dataChunksKept.contains(hash)) {
                        i--;
                        continue;
                    }
                    dbu.addData(hash, bytes);
                    dataChunksKept.add(hash);
                } else {
                    if (dataChunksDeleted.contains(hash)) {
                        i--;
                        continue;
                    }
                    dbu.addData(hash, bytes);
                    dataChunksDeleted.add(hash);
                }
            }
            for (int i = 0; i < totalMetaDataToAdd; i++) {
                byte[] bytes = DevUtil.createRandomMetaDataChunk();
                BigHash hash = DevUtil.getRandomBigHash();
                if (i % 2 == 0) {
                    if (metaDataChunksKept.contains(hash)) {
                        i--;
                        continue;
                    }
                    dbu.addMetaData(hash, bytes);
                    metaDataChunksKept.add(hash);
                } else {
                    if (metaDataChunksDeleted.contains(hash)) {
                        i--;
                        continue;
                    }
                    dbu.addMetaData(hash, bytes);
                    metaDataChunksDeleted.add(hash);
                }
            }
            for (BigHash h : dataChunksDeleted) {
                dbu.deleteData(h, "test");
            }
            for (BigHash h : metaDataChunksDeleted) {
                dbu.deleteMetaData(h, "test");
            }
            for (BigHash h : dataChunksKept) {
                assertTrue("Should have data chunk.", dbu.hasData(h));
            }
            for (BigHash h : dataChunksDeleted) {
                assertFalse("Should not have data chunk.", dbu.hasData(h));
            }
            for (BigHash h : metaDataChunksKept) {
                assertTrue("Should have meta data chunk.", dbu.hasMetaData(h));
            }
            for (BigHash h : metaDataChunksDeleted) {
                assertFalse("Should not have meta data chunk.", dbu.hasMetaData(h));
            }
            File dataLog = dbu.getDataDeletionLog();
            File metaDataLog = dbu.getMetaDataDeletionLog();
            IOUtil.safeClose(ffts);
            if (!shouldLog) {
                assertFalse("Data log should not exist; not logging.", dataLog.exists());
                assertFalse("Meta data log should not exist; not logging.", metaDataLog.exists());
            } else {
                assertTrue("Data log should exist.", dataLog.exists());
                assertTrue("Data log should exist.", metaDataLog.exists());
                Set<BigHash> foundDeletedDataChunks = new HashSet();
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new FileReader(dataLog));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        String[] tokens = line.split(",");
                        if (tokens.length == 0) {
                            continue;
                        }
                        BigHash hash = BigHash.createHashFromString(tokens[tokens.length - 1]);
                        foundDeletedDataChunks.add(hash);
                    }
                } finally {
                    IOUtil.safeClose(in);
                }
                for (BigHash h : dataChunksKept) {
                    assertFalse("Log should not contain any reference to non-deleted chunk.", foundDeletedDataChunks.contains(h));
                }
                for (BigHash h : dataChunksDeleted) {
                    assertTrue("Log should contain reference to deleted chunk.", foundDeletedDataChunks.contains(h));
                }
                Set<BigHash> foundDeletedMetaDataChunks = new HashSet();
                try {
                    in = new BufferedReader(new FileReader(metaDataLog));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        String[] tokens = line.split(",");
                        if (tokens.length == 0) {
                            continue;
                        }
                        BigHash hash = BigHash.createHashFromString(tokens[tokens.length - 1]);
                        foundDeletedMetaDataChunks.add(hash);
                    }
                } finally {
                    IOUtil.safeClose(in);
                }
                for (BigHash h : metaDataChunksKept) {
                    assertFalse("Log should not contain any reference to non-deleted chunk.", foundDeletedMetaDataChunks.contains(h));
                }
                for (BigHash h : metaDataChunksDeleted) {
                    assertTrue("Log should contain reference to deleted chunk.", foundDeletedMetaDataChunks.contains(h));
                }
            }
        } finally {
            IOUtil.safeClose(ffts);
            IOUtil.recursiveDelete(dataDir);
        }
    }

    /**
     * <p>Shows that chunk of zero bytes doesn't mess up data block.</p>
     */
    public void testZeroBytesChunk() throws Exception {
        TestUtil.printTitle("DataBlockTest:testZeroBytesChunk()");
        File dir = null;
        FlatFileTrancheServer ffts = null;
        try {
            dir = TempFileUtil.createTemporaryDirectory("testZeroBytesChunk");
            ffts = new FlatFileTrancheServer(dir);
            DataBlockUtil dbu = ffts.getDataBlockUtil();
            byte[] emptyChunk = new byte[0];
            BigHash emptyChunkHash = new BigHash(emptyChunk);
            String startsWith = emptyChunkHash.toString().substring(0, 2);
            Set<BigHash> hashes = new HashSet();
            for (int i = 0; i < 15; i++) {
                byte[] dataChunk = DevUtil.createRandomDataChunkStartsWith(startsWith);
                BigHash dataChunkHash = new BigHash(dataChunk);
                if (hashes.contains(dataChunkHash)) {
                    i--;
                    continue;
                }
                dbu.addData(dataChunkHash, dataChunk);
                hashes.add(dataChunkHash);
            }
            assertFalse("Better not contain empty chunk already.", dbu.hasData(emptyChunkHash));
            dbu.addData(emptyChunkHash, emptyChunk);
            hashes.add(emptyChunkHash);
            assertTrue("Better contain empty chunk.", dbu.hasData(emptyChunkHash));
            byte[] emptyChunkVerify = dbu.getData(emptyChunkHash);
            assertEquals("Chunk better be zero bytes.", 0, emptyChunkVerify.length);
            for (int i = 0; i < 15; i++) {
                byte[] dataChunk = DevUtil.createRandomDataChunkStartsWith(startsWith);
                BigHash dataChunkHash = new BigHash(dataChunk);
                if (hashes.contains(dataChunkHash)) {
                    i--;
                    continue;
                }
                dbu.addData(dataChunkHash, dataChunk);
                hashes.add(dataChunkHash);
            }
            assertTrue("Better contain empty chunk.", dbu.hasData(emptyChunkHash));
            emptyChunkVerify = dbu.getData(emptyChunkHash);
            assertEquals("Chunk better be zero bytes.", 0, emptyChunkVerify.length);
            for (BigHash h : hashes) {
                assertTrue("Better have data chunk: " + h, dbu.hasData(h));
                byte[] dataChunk = dbu.getData(h);
                BigHash hash = new BigHash(dataChunk);
                assertEquals("Hashes should match for: " + h, h, hash);
            }
            DataBlock db = dbu.getDataBlockToGetChunk(emptyChunkHash, false);
            db.cleanUpDataBlock(false);
            Thread.sleep(1000);
            TestUtil.printRecursiveDirectoryStructure(dir);
            assertTrue("Better contain empty chunk.", dbu.hasData(emptyChunkHash));
            emptyChunkVerify = dbu.getData(emptyChunkHash);
            assertEquals("Chunk better be zero bytes.", 0, emptyChunkVerify.length);
            for (BigHash h : hashes) {
                assertTrue("Better have data chunk: " + h, dbu.hasData(h));
                byte[] dataChunk = dbu.getData(h);
                BigHash hash = new BigHash(dataChunk);
                assertEquals("Hashes should match for: " + h, h, hash);
            }
        } finally {
            IOUtil.safeClose(ffts);
            IOUtil.recursiveDelete(dir);
        }
    }

    /**
     * Tests general read/write of the data block.
     */
    public void testNonSplittingWriteAndRead() throws Exception {
        TestUtil.printTitle("DataBlockTest:testNonSplittingWriteAndRead()");
        File dir = TempFileUtil.createTemporaryDirectory();
        try {
            ArrayList<byte[]> randomData = new ArrayList();
            for (int i = 0; i < 10; i++) {
                randomData.add(Utils.makeRandomData((int) (Math.random() * DataBlockUtil.getMaxChunkSize())));
            }
            ArrayList<byte[]> randomMetaData = new ArrayList();
            for (int i = 0; i < 10; i++) {
                randomMetaData.add(Utils.makeRandomData((int) (Math.random() * 2024)));
            }
            randomMetaData.add(DevUtil.createRandomBigMetaDataChunk());
            DataDirectoryConfiguration ddc1 = new DataDirectoryConfiguration(dir.getAbsolutePath() + File.separator + "1", Long.MAX_VALUE);
            DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dir.getAbsolutePath() + File.separator + "2", Long.MAX_VALUE);
            DataDirectoryConfiguration ddc3 = new DataDirectoryConfiguration(dir.getAbsolutePath() + File.separator + "3", Long.MAX_VALUE);
            DataBlockUtil dbu = new DataBlockUtil();
            dbu.add(ddc1);
            dbu.add(ddc2);
            dbu.add(ddc3);
            for (byte[] data : randomData) {
                BigHash hash = new BigHash(data);
                dbu.addData(hash, data);
                assertTrue("Better have data", dbu.hasData(hash));
            }
            for (byte[] data : randomMetaData) {
                BigHash hash = new BigHash(data);
                dbu.addMetaData(hash, data);
                assertTrue("Better have meta data", dbu.hasMetaData(hash));
            }
            for (byte[] data : randomData) {
                BigHash hash = new BigHash(data);
                byte[] readData = dbu.getData(hash);
                BigHash check = new BigHash(readData);
                assertEquals("Expected data to be of same size.", data.length, readData.length);
                assertEquals(hash.toString(), check.toString());
            }
            for (byte[] data : randomMetaData) {
                BigHash hash = new BigHash(data);
                byte[] readData = dbu.getMetaData(hash);
                BigHash check = new BigHash(readData);
                assertEquals("Expected data to be of same size.", data.length, readData.length);
                assertEquals(hash.toString(), check.toString());
            }
            {
                DataBlockUtil dbu2 = new DataBlockUtil();
                dbu2.add(ddc2);
                dbu2.add(ddc1);
                dbu2.add(ddc3);
                for (byte[] data : randomData) {
                    BigHash hash = new BigHash(data);
                    byte[] readData = dbu2.getData(hash);
                    BigHash check = new BigHash(readData);
                    assertEquals("Expected data to be of same size.", data.length, readData.length);
                    assertEquals(hash.toString(), check.toString());
                }
                for (byte[] data : randomMetaData) {
                    BigHash hash = new BigHash(data);
                    byte[] readData = dbu2.getMetaData(hash);
                    BigHash check = new BigHash(readData);
                    assertEquals("Expected data to be of same size.", data.length, readData.length);
                    assertEquals(hash.toString(), check.toString());
                }
            }
            {
                File dirToDup = ddc1.getDirectoryFile();
                for (File temp : dirToDup.listFiles()) {
                    if (temp.isFile()) {
                        byte[] data = IOUtil.getBytes(temp);
                        FileOutputStream fos = new FileOutputStream(new File(ddc2.getDirectory(), temp.getName()));
                        fos.write(data);
                        fos.flush();
                        fos.close();
                        break;
                    }
                }
                DataBlockUtil dbu2 = new DataBlockUtil();
                dbu2.add(ddc2);
                dbu2.add(ddc1);
                dbu2.add(ddc3);
                for (byte[] data : randomData) {
                    BigHash hash = new BigHash(data);
                    byte[] readData = dbu2.getData(hash);
                    BigHash check = new BigHash(readData);
                    assertEquals("Expected data to be of same size.", data.length, readData.length);
                    assertEquals(hash.toString(), check.toString());
                }
                for (byte[] data : randomMetaData) {
                    BigHash hash = new BigHash(data);
                    byte[] readData = dbu2.getMetaData(hash);
                    BigHash check = new BigHash(readData);
                    assertEquals("Expected data to be of same size.", data.length, readData.length);
                    assertEquals(hash.toString(), check.toString());
                }
            }
        } finally {
            IOUtil.recursiveDelete(dir);
        }
    }

    /**
     * Tests that a same named data/meta-data collision will properly keep both entries. It would cause problems if either were deleted.
     */
    public void testSameHasMetaDataAndData() throws Exception {
        TestUtil.printTitle("DataBlockTest:testSameHasMetaDataAndData()");
        File dir = TempFileUtil.createTemporaryDirectory();
        try {
            byte[] data = Utils.makeRandomData((int) (Math.random() * DataBlockUtil.getMaxChunkSize()));
            byte[] metaData = Utils.makeRandomData((int) (Math.random() * 2024));
            DataDirectoryConfiguration ddc = new DataDirectoryConfiguration(dir.getAbsolutePath(), Long.MAX_VALUE);
            DataBlockUtil dbu = new DataBlockUtil();
            dbu.add(ddc);
            BigHash sameHash = new BigHash(data);
            dbu.addData(sameHash, data);
            dbu.addMetaData(sameHash, metaData);
            byte[] dataCheck = dbu.getData(sameHash);
            BigHash dataCheckHash = new BigHash(dataCheck);
            assertEquals("Expected same size.", data.length, dataCheck.length);
            assertEquals("Expected same hash.", sameHash.toString(), dataCheckHash.toString());
            byte[] metaDataCheck = dbu.getMetaData(sameHash);
            BigHash metaDataHash = new BigHash(metaData);
            BigHash metaDataCheckHash = new BigHash(metaDataCheck);
            assertEquals("Expected same size.", metaData.length, metaDataCheck.length);
            assertEquals("Expected same hash.", metaDataHash.toString(), metaDataCheckHash.toString());
        } finally {
            IOUtil.recursiveDelete(dir);
        }
    }

    /**
     * Test that the delete functionality works and files are still accessible. Also tests the clean up code that saves space after lots of deletes occur.
     */
    public void testDelete() throws Exception {
        TestUtil.printTitle("DataBlockTest:testDelete()");
        File dir = TempFileUtil.createTemporaryDirectory();
        try {
            ArrayList<byte[]> randomData = new ArrayList();
            ArrayList<byte[]> randomDataToDelete = new ArrayList();
            for (int i = 0; i < 10; i++) {
                randomData.add(Utils.makeRandomData((int) (Math.random() * DataBlockUtil.getMaxChunkSize())));
                randomDataToDelete.add(Utils.makeRandomData((int) (Math.random() * DataBlockUtil.getMaxChunkSize())));
            }
            DataDirectoryConfiguration ddc = new DataDirectoryConfiguration(dir.getAbsolutePath(), Long.MAX_VALUE);
            DataBlockUtil dbu = new DataBlockUtil();
            dbu.add(ddc);
            for (int i = 0; i < randomData.size(); i++) {
                BigHash hash = new BigHash(randomData.get(i));
                dbu.addData(hash, randomData.get(i));
            }
            for (int i = 0; i < randomDataToDelete.size(); i++) {
                BigHash hash = new BigHash(randomDataToDelete.get(i));
                dbu.addData(hash, randomDataToDelete.get(i));
            }
            for (byte[] data : randomData) {
                BigHash hash = new BigHash(data);
                DataBlock block = dbu.getDataBlockToAddChunk(hash);
                byte[] readData = block.getBytes(hash, false);
                BigHash check = new BigHash(readData);
                assertEquals("Expected data to be of same size.", data.length, readData.length);
                assertEquals(hash.toString(), check.toString());
            }
            for (byte[] data : randomDataToDelete) {
                BigHash hash = new BigHash(data);
                DataBlock block = dbu.getDataBlockToAddChunk(hash);
                byte[] readData = block.getBytes(hash, false);
                BigHash check = new BigHash(readData);
                assertEquals("Expected data to be of same size.", data.length, readData.length);
                assertEquals(hash.toString(), check.toString());
            }
            for (byte[] data : randomDataToDelete) {
                BigHash hash = new BigHash(data);
                DataBlock block = dbu.getDataBlockToAddChunk(hash);
                block.deleteBytes(hash, false);
                try {
                    byte[] readData = block.getBytes(hash, false);
                    fail("Expected bytes not to be found.");
                } catch (Exception e) {
                }
            }
            long preCleanupSize = 0;
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    fail("Expected no data block splits!");
                }
                preCleanupSize += file.length();
            }
            for (byte[] data : randomDataToDelete) {
                BigHash hash = new BigHash(data);
                DataBlock block = dbu.getDataBlockToAddChunk(hash);
                block.cleanUpDataBlock(true);
            }
            for (byte[] data : randomDataToDelete) {
                BigHash hash = new BigHash(data);
                DataBlock block = dbu.getDataBlockToAddChunk(hash);
                try {
                    byte[] readData = block.getBytes(hash, false);
                    fail("Expected bytes not to be found.");
                } catch (Exception e) {
                }
            }
            for (byte[] data : randomData) {
                BigHash hash = new BigHash(data);
                DataBlock block = dbu.getDataBlockToAddChunk(hash);
                byte[] readData = block.getBytes(hash, false);
                BigHash check = new BigHash(readData);
                assertEquals("Expected data to be of same size.", data.length, readData.length);
                assertEquals(hash.toString(), check.toString());
            }
            long postCleanupSize = 0;
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    fail("Expected no data block splits!");
                }
                postCleanupSize += file.length();
            }
            assertTrue("Expected cleanup to save space.", postCleanupSize < preCleanupSize);
        } finally {
            IOUtil.recursiveDelete(dir);
        }
    }

    /**
     * Tests the code that handles file splitting.
     */
    public void testSplitAndAutoFix() throws Exception {
        TestUtil.printTitle("DataBlockTest:testSplitAndAutoFix()");
        File dir = TempFileUtil.createTemporaryDirectory();
        DataBlockUtilLog logger = null;
        try {
            DataDirectoryConfiguration ddc = new DataDirectoryConfiguration(dir.getAbsolutePath(), Long.MAX_VALUE);
            DataBlockUtil dbu = new DataBlockUtil();
            dbu.add(ddc);
            DataBlockUtil.setIsLogging(true);
            logger = DataBlockUtil.getLogger();
            ArrayList<BigHash> contrivedHashes = new ArrayList();
            ArrayList<BigHash> realHashes = new ArrayList();
            int bytesWritten = 0;
            int maxFileSize = 100 * 1024;
            while (bytesWritten <= DataBlock.getMaxBlockSize() * 2) {
                byte[] randomData = Utils.makeRandomData((int) (Math.random() * maxFileSize));
                BigHash hash = new BigHash(randomData);
                byte[] hashBytes = hash.toByteArray();
                byte[] copy = new byte[hashBytes.length];
                System.arraycopy(hashBytes, 0, copy, 0, copy.length);
                copy[0] = 0;
                copy[1] = 1;
                BigHash fakeHash = BigHash.createFromBytes(copy);
                dbu.addData(fakeHash, randomData);
                bytesWritten += randomData.length;
                contrivedHashes.add(fakeHash);
                realHashes.add(hash);
            }
            String[] checkForDir = dir.list();
            assertTrue(2 == checkForDir.length || 1 == checkForDir.length);
            boolean atLeastOneDirectory = false;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    atLeastOneDirectory = true;
                }
            }
            assertTrue(atLeastOneDirectory);
            {
                int fileCount = 0;
                ArrayList<File> filesToCount = new ArrayList();
                filesToCount.add(dir);
                while (!filesToCount.isEmpty()) {
                    File fileToCount = filesToCount.remove(0);
                    if (fileToCount.isDirectory()) {
                        File[] files = fileToCount.listFiles();
                        for (File file : files) {
                            filesToCount.add(file);
                        }
                    } else {
                        fileCount++;
                    }
                }
                assertTrue("Expected more than one split block files.", 1 < fileCount);
            }
            for (int i = 0; i < contrivedHashes.size(); i++) {
                byte[] data = dbu.getData(contrivedHashes.get(i));
                BigHash check = new BigHash(data);
                assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
            }
            {
                DataBlock block = null;
                for (int i = 0; i < contrivedHashes.size(); i++) {
                    block = dbu.getDataBlockToAddChunk(contrivedHashes.get(i));
                    if (block.getHashes(false).size() > 0) {
                        break;
                    }
                }
                dbu.purposelyFailMerge = 0;
                try {
                    block.cleanUpDataBlock(true);
                    fail("Expected a failure!");
                } catch (Exception e) {
                }
                dbu.purposelyFailMerge = Integer.MAX_VALUE;
            }
            ArrayList<BigHash> missingData = new ArrayList();
            for (int i = 0; i < contrivedHashes.size(); i++) {
                try {
                    dbu.getData(contrivedHashes.get(i));
                } catch (FileNotFoundException e) {
                    missingData.add(contrivedHashes.get(i));
                }
            }
            assertTrue("Expected at least one hash to be missing from the tree.", missingData.size() > 0);
            File toAddFile = null;
            ArrayList<File> filesToCount = new ArrayList();
            filesToCount.add(dir);
            while (!filesToCount.isEmpty()) {
                File fileToCount = filesToCount.remove(0);
                if (fileToCount.isDirectory()) {
                    File[] files = fileToCount.listFiles();
                    for (File file : files) {
                        filesToCount.add(file);
                    }
                } else {
                    if (fileToCount.getName().endsWith(".backup")) {
                        if (toAddFile != null) {
                            fail("More than one 'toadd' file was found!?");
                        }
                        toAddFile = fileToCount;
                    }
                }
            }
            assertNotNull(toAddFile);
            dbu.mergeOldDataBlock(toAddFile);
            for (int i = 0; i < contrivedHashes.size(); i++) {
                byte[] data = dbu.getData(contrivedHashes.get(i));
                BigHash check = new BigHash(data);
                assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
            }
            assertTrue("Should be some run time.", logger.getRuntime() > 0);
            assertTrue("Should be some time spent merging.", logger.getTimeSpentMerging() > 0);
            assertTrue("Time spent merging should be less than total log time.", logger.getRuntime() > logger.getTimeSpentMerging());
        } finally {
            IOUtil.recursiveDelete(dir);
            if (logger != null) {
                logger.close();
            }
            DataBlockUtil.setIsLogging(false);
        }
    }

    /**
     * Makes a big b-tree across several directories and tests that the merging functionality works.
     */
    public void testAutoFixWithMultipleDirectories() throws Exception {
        TestUtil.printTitle("DataBlockTest:testAutoFixWithMultipleDirectories()");
        File dir = TempFileUtil.createTemporaryDirectory();
        try {
            File[] dataDirectories = new File[4];
            for (int i = 0; i < dataDirectories.length; i++) {
                dataDirectories[i] = new File(dir, Integer.toString(i));
            }
            DataDirectoryConfiguration[] configs = new DataDirectoryConfiguration[dataDirectories.length];
            for (int i = 0; i < configs.length; i++) {
                configs[i] = new DataDirectoryConfiguration(dataDirectories[i].getAbsolutePath(), Long.MAX_VALUE);
            }
            DataBlockUtil dbu = new DataBlockUtil();
            for (int i = 0; i < configs.length; i++) {
                dbu.add(configs[i]);
            }
            ArrayList<BigHash> contrivedHashes = new ArrayList();
            ArrayList<BigHash> realHashes = new ArrayList();
            long totalEntries = DataBlock.getHeadersPerFile() * 10;
            for (int i = 0; i < totalEntries; i++) {
                byte[] randomData = Utils.makeRandomData((int) (Math.random() * 10 * 1024));
                BigHash hash = new BigHash(randomData);
                byte[] hashBytes = hash.toByteArray();
                byte[] copy = new byte[hashBytes.length];
                System.arraycopy(hashBytes, 0, copy, 0, copy.length);
                copy[0] = 0;
                copy[1] = (byte) (i % 2);
                BigHash fakeHash = BigHash.createFromBytes(copy);
                dbu.addData(fakeHash, randomData);
                contrivedHashes.add(fakeHash);
                realHashes.add(hash);
            }
            {
                int fileCount = 0;
                ArrayList<File> filesToCount = new ArrayList();
                for (int i = 0; i < dataDirectories.length; i++) {
                    filesToCount.add(dataDirectories[i]);
                }
                while (!filesToCount.isEmpty()) {
                    File fileToCount = filesToCount.remove(0);
                    if (fileToCount.isDirectory()) {
                        File[] files = fileToCount.listFiles();
                        for (File file : files) {
                            filesToCount.add(file);
                        }
                    } else {
                        fileCount++;
                    }
                }
                assertTrue("Expected more than one split block files.", 1 < fileCount);
            }
            for (int i = 0; i < contrivedHashes.size(); i++) {
                byte[] data = dbu.getData(contrivedHashes.get(i));
                BigHash check = new BigHash(data);
                assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
            }
            DataBlockUtil dbu2 = new DataBlockUtil();
            for (int i = 0; i < configs.length; i++) {
                dbu2.add(configs[i]);
            }
            for (int i = 0; i < contrivedHashes.size(); i++) {
                byte[] data = dbu2.getData(contrivedHashes.get(i));
                BigHash check = new BigHash(data);
                assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
            }
        } finally {
            IOUtil.recursiveDelete(dir);
        }
    }

    @Todo(desc = "Why duplicate test?", day = 0, month = 0, year = 0, author = "Unknown")
    public void testAutoFixWithMultipleDirectories2() throws Exception {
        TestUtil.printTitle("DataBlockTest:testAutoFixWithMultipleDirectories2()");
        File dir = TempFileUtil.createTemporaryDirectory();
        Random random = new Random(1);
        try {
            File[] dataDirectories = new File[4];
            for (int i = 0; i < dataDirectories.length; i++) {
                dataDirectories[i] = new File(dir, Integer.toString(i));
            }
            FlatFileTrancheServer ffts = new FlatFileTrancheServer(dir);
            Configuration config = ffts.getConfiguration();
            Set<DataDirectoryConfiguration> ddcs = config.getDataDirectories();
            for (int i = 0; i < dataDirectories.length; i++) {
                ddcs.add(new DataDirectoryConfiguration(dataDirectories[i].getAbsolutePath(), Long.MAX_VALUE));
            }
            UserZipFile uzf = DevUtil.createUser("foo", "bar", new File(dir, "user.temp").getAbsolutePath(), true, false);
            config.addUser(uzf);
            ArrayList<BigHash> contrivedHashes = new ArrayList();
            ArrayList<BigHash> realHashes = new ArrayList();
            long totalEntries = DataBlock.getHeadersPerFile() * 10;
            for (int i = 0; i < totalEntries; i++) {
                byte[] randomData = new byte[(int) (Math.random() * 10 * 1024)];
                random.nextBytes(randomData);
                BigHash hash = new BigHash(randomData);
                byte[] hashBytes = hash.toByteArray();
                byte[] copy = new byte[hashBytes.length];
                System.arraycopy(hashBytes, 0, copy, 0, copy.length);
                copy[0] = 0;
                copy[1] = (byte) (i % 2);
                BigHash fakeHash = BigHash.createFromBytes(copy);
                IOUtil.setData(ffts, uzf.getCertificate(), uzf.getPrivateKey(), fakeHash, randomData);
                contrivedHashes.add(fakeHash);
                realHashes.add(hash);
            }
            {
                System.out.println("B-tree structure.");
                int fileCount = 0;
                ArrayList<File> filesToCount = new ArrayList();
                for (int i = 0; i < dataDirectories.length; i++) {
                    filesToCount.add(dataDirectories[i]);
                }
                while (!filesToCount.isEmpty()) {
                    File fileToCount = filesToCount.remove(0);
                    if (fileToCount.isDirectory()) {
                        File[] files = fileToCount.listFiles();
                        for (File file : files) {
                            filesToCount.add(file);
                        }
                    } else {
                        fileCount++;
                    }
                }
                assertTrue("Expected more than one split block files.", 1 < fileCount);
            }
            for (int i = 0; i < contrivedHashes.size(); i++) {
                Object o = IOUtil.getData(ffts, contrivedHashes.get(i), false).getReturnValueObject();
                byte[] data = null;
                if (o instanceof byte[]) {
                    data = (byte[]) o;
                } else if (o instanceof byte[][]) {
                    data = ((byte[][]) o)[0];
                } else {
                    fail("Expected return object to be type byte[] or byte[][], but wasn't.");
                }
                BigHash check = new BigHash(data);
                assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
            }
            ffts.close();
            FlatFileTrancheServer ffts2 = new FlatFileTrancheServer(dir);
            ffts2.waitToLoadExistingDataBlocks();
            for (int i = 0; i < contrivedHashes.size(); i++) {
                byte[] data = (byte[]) IOUtil.getData(ffts2, contrivedHashes.get(i), false).getReturnValueObject();
                BigHash check = new BigHash(data);
                assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
            }
            ffts2.close();
            FlatFileTrancheServer ffts3 = new FlatFileTrancheServer(dir);
            for (int i = 0; i < contrivedHashes.size(); i++) {
                try {
                    byte[] data = (byte[]) IOUtil.getData(ffts3, contrivedHashes.get(i), false).getReturnValueObject();
                    BigHash check = new BigHash(data);
                    assertTrue("Expected the same hash.", check.equals(realHashes.get(i)));
                } catch (FileNotFoundException e) {
                    byte[] data = (byte[]) IOUtil.getData(ffts3, contrivedHashes.get(i), false).getReturnValueObject();
                }
            }
        } finally {
            IOUtil.recursiveDelete(dir);
        }
    }

    /**
     * <p>Want to test that appropriately has/gets data and meta data. Also negatively test.</p>
     */
    public void testHasAndGetDataAndMetaData() throws Exception {
        TestUtil.printTitle("DataBlockTest:testHasAndGetDataAndMetaData()");
        Set<BigHash> dataHashes = new HashSet<BigHash>(), metaHashes = new HashSet<BigHash>();
        FlatFileTrancheServer ffserver = null;
        UserZipFile uzf = null;
        File topDir = null, oneDir = null, twoDir = null, threeDir = null;
        DataBlockUtil dbu = null;
        try {
            topDir = TempFileUtil.createTemporaryDirectory("testHasAndGetDataAndMetaData");
            oneDir = new File(topDir, "one");
            oneDir.mkdirs();
            twoDir = new File(topDir, "two");
            twoDir.mkdirs();
            threeDir = new File(topDir, "three");
            threeDir.mkdirs();
            assertTrue("Directory should exist: one", oneDir.exists());
            assertTrue("Directory should exist: two", twoDir.exists());
            assertTrue("Directory should exist: three", threeDir.exists());
            uzf = DevUtil.createUser("foo", "bar", TempFileUtil.createTemporaryFile(".zip.encrypted").getAbsolutePath(), true, false);
            ffserver = new FlatFileTrancheServer(topDir);
            Configuration config = ffserver.getConfiguration();
            config.addUser(uzf);
            Set<DataDirectoryConfiguration> ddcs = config.getDataDirectories();
            ddcs.add(new DataDirectoryConfiguration(oneDir.getAbsolutePath(), Long.MAX_VALUE));
            ddcs.add(new DataDirectoryConfiguration(twoDir.getAbsolutePath(), Long.MAX_VALUE));
            ddcs.add(new DataDirectoryConfiguration(threeDir.getAbsolutePath(), Long.MAX_VALUE));
            assertEquals("Expecting 4 ddcs: one default + 3 added", 4, config.getDataDirectories().size());
            dbu = ffserver.getDataBlockUtil();
            assertNotNull("Should return a DataBlockUtil without problems, but null!", dbu);
            int size = RandomUtil.getInt(1024 * 512) + 1024;
            for (int i = 0; i < 100; i++) {
                byte[] meta = new byte[size];
                RandomUtil.getBytes(meta);
                BigHash h = new BigHash(meta);
                if (metaHashes.contains(h)) {
                    i--;
                    continue;
                }
                metaHashes.add(h);
                assertFalse("Should not have meta data!", dbu.hasMetaData(h));
                dbu.addMetaData(h, meta);
                assertTrue("Should have meta data!", dbu.hasMetaData(h));
                byte[] getMeta = dbu.getMetaData(h);
                assertNotNull("Should not have trouble retrieving meta data.", getMeta);
                assertEquals("Meta data should be the same length.", meta.length, getMeta.length);
                for (int j = 0; j < meta.length; j++) {
                    assertEquals("Meta data should be the same.", meta[j], getMeta[j]);
                }
            }
            for (int i = 0; i < 100; i++) {
                byte[] data = new byte[size];
                RandomUtil.getBytes(data);
                BigHash h = new BigHash(data);
                if (dataHashes.contains(h)) {
                    i--;
                    continue;
                }
                dataHashes.add(h);
                assertFalse("Should not have data!", dbu.hasData(h));
                dbu.addData(h, data);
                assertTrue("Should have data!", dbu.hasData(h));
                assertNotNull("Should not have trouble retrieving data!", dbu.getData(h));
            }
            for (BigHash h : metaHashes) {
                assertTrue("Should have meta data!", dbu.hasMetaData(h));
                assertNotNull("Should not have trouble retrieving meta data!", dbu.getMetaData(h));
            }
            for (BigHash h : dataHashes) {
                assertTrue("Should have data!", dbu.hasData(h));
                assertNotNull("Should not have trouble retrieving data!", dbu.getData(h));
            }
        } finally {
            IOUtil.safeClose(ffserver);
            IOUtil.safeDelete(uzf.getFile());
            IOUtil.recursiveDelete(topDir);
            dbu.close();
        }
    }

    public void testBadChunksEmpty1MBSmallBatch() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksEmpty1MBSmallBatch()");
        testDataBlockHandlesBadChunks(50, 50, true, true, false);
    }

    public void testBadChunksRandom1MBSmallBatch() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksRandom1MBSmallBatch()");
        testDataBlockHandlesBadChunks(50, 50, true, false, false);
    }

    public void testBadChunksEmptySmallBatch() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksEmptySmallBatch()");
        testDataBlockHandlesBadChunks(50, 50, false, true, false);
    }

    public void testBadChunksRandomSmallBatch() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksRandomSmallBatch()");
        testDataBlockHandlesBadChunks(50, 50, false, false, false);
    }

    public void testBadChunksEmpty1MBSmallBatchWithFalseSizes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksEmpty1MBSmallBatchWithFalseSizes()");
        testDataBlockHandlesBadChunks(50, 50, true, true, true);
    }

    public void testBadChunksRandom1MBSmallBatchWithFalseSizes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksRandom1MBSmallBatchWithFalseSizes()");
        testDataBlockHandlesBadChunks(50, 50, true, false, true);
    }

    public void testBadChunksEmptySmallBatchWithFalseSizes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksEmptySmallBatchWithFalseSizes()");
        testDataBlockHandlesBadChunks(50, 50, false, true, true);
    }

    public void testBadChunksRandomSmallBatchWithFalseSizes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBadChunksRandomSmallBatchWithFalseSizes()");
        testDataBlockHandlesBadChunks(50, 50, false, false, true);
    }

    /**
     * <p>This helper method allows many permutations. Pay attention to the parameters.</p>
     * <p>This test has three DataBlockUtil's. One will have only good meta and data chunks. The others will have some good and some bad.</p>
     * <p>Bad chunks mean that they don't match their hash if data or are not valid meta data chunks. There are two type of bad chunks. See isUseEmptyBytes.</p>
     * @param numDataChunks Number of data chunks to test.
     * @param numMetaChunks Number of meta chunks to test.
     * @param useExactly1MBChunks If true, every test chunk is exactly 1MB. Other, randomly between 1 byte and 1MB, inclusive.
     * @param isUseEmptyBytes If true, the bad chunk will be an initialized but untouched byte array (should be all zeros). Others might be random bytes. This will help simulate different IO error possibilities.
     * @param isTestFalseSizes will change the size of the bad chunk before submitting.
     */
    private void testDataBlockHandlesBadChunks(int numDataChunks, int numMetaChunks, boolean useExactly1MBChunks, boolean isUseEmptyBytes, boolean isTestFalseSizes) throws Exception {
        File dataDir1 = null, dataDir2 = null, dataDir3 = null;
        DataBlockUtil dbu1 = new DataBlockUtil();
        DataBlockUtil dbu2 = new DataBlockUtil();
        DataBlockUtil dbu3 = new DataBlockUtil();
        try {
            dataDir1 = TempFileUtil.createTemporaryDirectory();
            dataDir2 = TempFileUtil.createTemporaryDirectory();
            dataDir3 = TempFileUtil.createTemporaryDirectory();
            assertTrue("Directory should exist.", dataDir1.exists());
            assertTrue("Directory should exist.", dataDir2.exists());
            assertTrue("Directory should exist.", dataDir3.exists());
            assertFalse("Should be different directories.", dataDir1.getAbsolutePath().equals(dataDir2.getAbsolutePath()));
            assertFalse("Should be different directories.", dataDir2.getAbsolutePath().equals(dataDir3.getAbsolutePath()));
            assertFalse("Should be different directories.", dataDir3.getAbsolutePath().equals(dataDir1.getAbsolutePath()));
            DataDirectoryConfiguration ddc1 = new DataDirectoryConfiguration(dataDir1.getAbsolutePath(), Long.MAX_VALUE);
            DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dataDir2.getAbsolutePath(), Long.MAX_VALUE);
            DataDirectoryConfiguration ddc3 = new DataDirectoryConfiguration(dataDir3.getAbsolutePath(), Long.MAX_VALUE);
            dbu1.add(ddc1);
            dbu2.add(ddc2);
            dbu3.add(ddc3);
            List<BigHash> dataChunks = new ArrayList<BigHash>();
            List<BigHash> metaChunks = new ArrayList<BigHash>();
            for (int i = 0; i < numDataChunks; i++) {
                byte[] goodChunk = null, badChunk = null;
                if (useExactly1MBChunks) {
                    goodChunk = new byte[1024 * 1024];
                    badChunk = new byte[1024 * 1024];
                } else {
                    int size = RandomUtil.getInt(1024 * 1024 - 1) + 1;
                    assertTrue("Size should be greater than zero.", size > 0);
                    assertTrue("Size should be 1MB or less.", size <= 1024 * 1024);
                    goodChunk = new byte[size];
                    badChunk = new byte[size];
                }
                RandomUtil.getBytes(goodChunk);
                BigHash hash = new BigHash(goodChunk);
                if (dataChunks.contains(hash)) {
                    i--;
                    continue;
                }
                if (isTestFalseSizes) {
                    int size = goodChunk.length;
                    int attempts = 0;
                    while (size > 1024 * 1024 || size == goodChunk.length) {
                        if (RandomUtil.getBoolean()) {
                            size = Math.abs(goodChunk.length - RandomUtil.getInt(goodChunk.length));
                        } else {
                            size = goodChunk.length + RandomUtil.getInt(1024 * 1024 - goodChunk.length + 1);
                        }
                        attempts++;
                        if (attempts > 1000000) {
                            fail("Couldn't change size. Why!?!");
                        }
                    }
                    badChunk = new byte[size];
                }
                if (!isUseEmptyBytes) {
                    RandomUtil.getBytes(badChunk);
                }
                if (badChunk.length == goodChunk.length) {
                    boolean difference = false;
                    for (int j = 0; j < badChunk.length; j++) {
                        if (badChunk[j] != goodChunk[j]) {
                            difference = true;
                            break;
                        }
                    }
                    assertTrue("Better be a difference!", difference);
                }
                dataChunks.add(hash);
                dbu1.addData(hash, goodChunk);
                if (RandomUtil.getBoolean()) {
                    dbu2.addData(hash, goodChunk);
                    dbu3.addData(hash, badChunk);
                } else {
                    dbu2.addData(hash, badChunk);
                    dbu3.addData(hash, goodChunk);
                }
            }
            for (int i = 0; i < numMetaChunks; i++) {
                byte[] goodChunk = null, badChunk = null;
                if (useExactly1MBChunks) {
                    goodChunk = DevUtil.createRandomMetaDataChunk();
                    badChunk = new byte[1024 * 1024];
                } else {
                    goodChunk = DevUtil.createRandomMetaDataChunk();
                    badChunk = new byte[goodChunk.length];
                }
                BigHash hash = new BigHash(goodChunk);
                if (metaChunks.contains(hash)) {
                    i--;
                    continue;
                }
                if (isTestFalseSizes) {
                    int size = goodChunk.length;
                    int attempts = 0;
                    while (size > 1024 * 1024 || size == goodChunk.length) {
                        if (RandomUtil.getBoolean()) {
                            size = Math.abs(goodChunk.length - RandomUtil.getInt(goodChunk.length));
                        } else {
                            size = goodChunk.length + RandomUtil.getInt(1024 * 1024 - goodChunk.length + 1);
                        }
                        attempts++;
                        if (attempts > 1000000) {
                            fail("Couldn't change size. Why!?!");
                        }
                    }
                    badChunk = new byte[size];
                }
                if (!isUseEmptyBytes) {
                    RandomUtil.getBytes(badChunk);
                }
                if (badChunk.length == goodChunk.length) {
                    boolean difference = false;
                    for (int j = 0; j < badChunk.length; j++) {
                        if (badChunk[j] != goodChunk[j]) {
                            difference = true;
                            break;
                        }
                    }
                    assertTrue("Better be a difference!", difference);
                }
                metaChunks.add(hash);
                dbu1.addMetaData(hash, goodChunk);
                if (RandomUtil.getBoolean()) {
                    dbu2.addMetaData(hash, goodChunk);
                    dbu3.addMetaData(hash, badChunk);
                } else {
                    dbu2.addMetaData(hash, badChunk);
                    dbu3.addMetaData(hash, goodChunk);
                }
            }
            long numDataChunksReported1 = dbu1.dataHashes.size();
            long numMetaChunksReported1 = dbu1.metaDataHashes.size();
            long numDataChunksReported2 = dbu2.dataHashes.size();
            long numMetaChunksReported2 = dbu2.metaDataHashes.size();
            long numDataChunksReported3 = dbu3.dataHashes.size();
            long numMetaChunksReported3 = dbu3.metaDataHashes.size();
            System.out.println("DBU #1 reports " + numDataChunksReported1 + " data chunks, " + numMetaChunksReported1 + " meta data chunks.");
            System.out.println("DBU #2 reports " + numDataChunksReported2 + " data chunks, " + numMetaChunksReported2 + " meta data chunks.");
            System.out.println("DBU #3 reports " + numDataChunksReported3 + " data chunks, " + numMetaChunksReported3 + " meta data chunks.");
            long numDataChunksCounted1 = 0;
            long numMetaChunksCounted1 = 0;
            long numDataChunksVerified1 = 0;
            long numMetaChunksVerified1 = 0;
            long numDataChunksCounted2 = 0;
            long numMetaChunksCounted2 = 0;
            long numDataChunksVerified2 = 0;
            long numMetaChunksVerified2 = 0;
            long numDataChunksCounted3 = 0;
            long numMetaChunksCounted3 = 0;
            long numDataChunksVerified3 = 0;
            long numMetaChunksVerified3 = 0;
            for (BigHash h : dataChunks) {
                if (dbu1.hasData(h)) {
                    numDataChunksCounted1++;
                    byte[] data = dbu1.getData(h);
                    try {
                        BigHash verifyHash = new BigHash(data);
                        if (verifyHash.equals(h)) {
                            numDataChunksVerified1++;
                        }
                    } catch (Exception ex) {
                    }
                }
                if (dbu2.hasData(h)) {
                    numDataChunksCounted2++;
                    byte[] data = dbu2.getData(h);
                    try {
                        BigHash verifyHash = new BigHash(data);
                        if (verifyHash.equals(h)) {
                            numDataChunksVerified2++;
                        }
                    } catch (Exception ex) {
                    }
                }
                if (dbu3.hasData(h)) {
                    numDataChunksCounted3++;
                    byte[] data = dbu3.getData(h);
                    try {
                        BigHash verifyHash = new BigHash(data);
                        if (verifyHash.equals(h)) {
                            numDataChunksVerified3++;
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            for (BigHash h : metaChunks) {
                if (dbu1.hasMetaData(h)) {
                    numMetaChunksCounted1++;
                    byte[] meta = dbu1.getMetaData(h);
                    try {
                        MetaData md = MetaDataUtil.read(new ByteArrayInputStream(meta));
                        if (md != null) {
                            numMetaChunksVerified1++;
                        }
                    } catch (Exception ex) {
                    }
                }
                if (dbu2.hasMetaData(h)) {
                    numMetaChunksCounted2++;
                    byte[] meta = dbu2.getMetaData(h);
                    try {
                        MetaData md = MetaDataUtil.read(new ByteArrayInputStream(meta));
                        if (md != null) {
                            numMetaChunksVerified2++;
                        }
                    } catch (Exception ex) {
                    }
                }
                if (dbu3.hasMetaData(h)) {
                    numMetaChunksCounted3++;
                    byte[] meta = dbu3.getMetaData(h);
                    try {
                        MetaData md = MetaDataUtil.read(new ByteArrayInputStream(meta));
                        if (md != null) {
                            numMetaChunksVerified3++;
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            System.out.println("Found DBU #1 " + numDataChunksCounted1 + " data chunks, " + numMetaChunksCounted1 + " meta data chunks.");
            System.out.println("Found DBU #2 " + numDataChunksCounted2 + " data chunks, " + numMetaChunksCounted2 + " meta data chunks.");
            System.out.println("Found DBU #3 " + numDataChunksCounted3 + " data chunks, " + numMetaChunksCounted3 + " meta data chunks.");
            System.out.println("Verified DBU #1 " + numDataChunksVerified1 + " data chunks, " + numMetaChunksVerified1 + " meta data chunks.");
            System.out.println("Verified DBU #2 " + numDataChunksVerified2 + " data chunks, " + numMetaChunksVerified2 + " meta data chunks.");
            System.out.println("Verified DBU #3 " + numDataChunksVerified3 + " data chunks, " + numMetaChunksVerified3 + " meta data chunks.");
            assertEquals("Expecting certain reported data chunks for DBU #1", dataChunks.size(), numDataChunksReported1);
            assertEquals("Expecting certain counted data chunks for DBU #1", dataChunks.size(), numDataChunksCounted1);
            assertEquals("Expecting certain reported meta chunks for DBU #1", metaChunks.size(), numMetaChunksReported1);
            assertEquals("Expecting certain counted meta chunks for DBU #1", metaChunks.size(), numMetaChunksCounted1);
            assertEquals("Expecting certain reported data chunks for DBU #2", dataChunks.size(), numDataChunksReported2);
            assertEquals("Expecting certain counted data chunks for DBU #2", dataChunks.size(), numDataChunksCounted2);
            assertEquals("Expecting certain reported meta chunks for DBU #2", metaChunks.size(), numMetaChunksReported2);
            assertEquals("Expecting certain counted meta chunks for DBU #2", metaChunks.size(), numMetaChunksCounted2);
            assertEquals("Expecting certain reported data chunks for DBU #3", dataChunks.size(), numDataChunksReported3);
            assertEquals("Expecting certain counted data chunks for DBU #3", dataChunks.size(), numDataChunksCounted3);
            assertEquals("Expecting certain reported meta chunks for DBU #3", metaChunks.size(), numMetaChunksReported3);
            assertEquals("Expecting certain counted meta chunks for DBU #3", metaChunks.size(), numMetaChunksCounted3);
            assertEquals("Expecting same number of veriable data chunks b/w DBU #1 and comb. of #2 and #3.", numDataChunksVerified1, numDataChunksVerified2 + numDataChunksVerified3);
            assertEquals("Expecting same number of veriable meta chunks b/w DBU #1 and comb. of #2 and #3.", numMetaChunksVerified1, numMetaChunksVerified2 + numMetaChunksVerified3);
        } finally {
            IOUtil.recursiveDeleteWithWarning(dataDir1);
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            IOUtil.recursiveDeleteWithWarning(dataDir3);
            dbu1.close();
            dbu2.close();
            dbu3.close();
        }
    }

    /**
     * This test does add, has, get, and delete operations with a meta data larger than 1MB
     */
    public void testBigMetaData() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBigMetaData()");
        File home = null;
        DataBlockUtil dbu = null;
        try {
            home = TempFileUtil.createTemporaryDirectory();
            DataDirectoryConfiguration ddc = new DataDirectoryConfiguration(home.getAbsolutePath(), Long.MAX_VALUE);
            dbu = new DataBlockUtil();
            dbu.add(ddc);
            byte[] metaBytes = DevUtil.createRandomBigMetaDataChunk();
            assertTrue("Expect meta data to be longer than 1MB.", metaBytes.length > DataBlockUtil.getMaxChunkSize());
            BigHash metaHash = new BigHash(metaBytes);
            assertFalse("Expecting meta data to not yet exist.", dbu.hasMetaData(metaHash));
            dbu.addMetaData(metaHash, metaBytes);
            assertTrue("Expecting meta data to exist.", dbu.hasMetaData(metaHash));
            assertNotNull("Expecting meta data to exist.", dbu.getMetaData(metaHash));
            dbu.deleteMetaData(metaHash, "testing");
            assertFalse("Expecting meta data to not exist anymore.", dbu.hasMetaData(metaHash));
        } finally {
            IOUtil.recursiveDeleteWithWarning(home);
            dbu.close();
        }
    }

    public void testDeleteAndReAddWithDifferentBytes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testDeleteAndReAddWithDifferentBytes()");
        testDeleteAndReAdd(true);
    }

    public void testDeleteAndReAddWithSameBytes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testDeleteAndReAddWithSameBytes()");
        testDeleteAndReAdd(false);
    }

    /**
     * <p>Want to make sure that can delete and readd a chunk without problems.</p>
     */
    public void testDeleteAndReAdd(boolean useDifferentBytesWhenReAdding) throws Exception {
        String prefix = RandomUtil.getString(4);
        BigHash toDeleteAndReAddData = DevUtil.getRandomBigHashStartsWith(prefix);
        BigHash toDeleteAndReAddMeta = DevUtil.getRandomBigHashStartsWith(prefix);
        while (toDeleteAndReAddData.equals(toDeleteAndReAddMeta)) {
            toDeleteAndReAddMeta = DevUtil.getRandomBigHashStartsWith(prefix);
        }
        DataBlockUtil dbu = new DataBlockUtil();
        File home = null;
        try {
            home = TempFileUtil.createTemporaryDirectory();
            DataDirectoryConfiguration ddc = new DataDirectoryConfiguration(home.getAbsolutePath(), Long.MAX_VALUE);
            dbu.add(ddc);
            byte[] dataBytes = DevUtil.createRandomDataChunk(1024 * 1024);
            byte[] metaBytes = DevUtil.createRandomMetaDataChunk();
            dbu.addData(toDeleteAndReAddData, dataBytes);
            dbu.addMetaData(toDeleteAndReAddMeta, metaBytes);
            assertTrue("Expecting data to exist.", dbu.hasData(toDeleteAndReAddData));
            assertNotNull("Expecting data to exist.", dbu.getData(toDeleteAndReAddData));
            assertTrue("Expecting meta data to exist.", dbu.hasMetaData(toDeleteAndReAddMeta));
            assertNotNull("Expecting meta data to exist.", dbu.getMetaData(toDeleteAndReAddMeta));
            dbu.deleteData(toDeleteAndReAddData, "testing");
            dbu.deleteMetaData(toDeleteAndReAddMeta, "testing");
            assertFalse("Expecting data to not exist.", dbu.hasData(toDeleteAndReAddData));
            assertFalse("Expecting meta data to exist.", dbu.hasMetaData(toDeleteAndReAddMeta));
            try {
                byte[] data = dbu.getData(toDeleteAndReAddData);
                if (data != null) {
                    fail("Expected data to not be available");
                }
            } catch (Exception ex) {
            }
            try {
                byte[] data = dbu.getMetaData(toDeleteAndReAddMeta);
                if (data != null) {
                    fail("Expected data to not be available");
                }
            } catch (Exception ex) {
            }
            if (useDifferentBytesWhenReAdding) {
                dataBytes = DevUtil.createRandomDataChunk(1024 * 1024);
                metaBytes = DevUtil.createRandomMetaDataChunk();
            }
            dbu.addData(toDeleteAndReAddData, dataBytes);
            dbu.addMetaData(toDeleteAndReAddMeta, metaBytes);
            assertTrue("Expecting data to exist.", dbu.hasData(toDeleteAndReAddData));
            assertNotNull("Expecting data to exist.", dbu.getData(toDeleteAndReAddData));
            assertTrue("Expecting meta data to exist.", dbu.hasMetaData(toDeleteAndReAddMeta));
            assertNotNull("Expecting meta data to exist.", dbu.getMetaData(toDeleteAndReAddMeta));
            dbu.deleteData(toDeleteAndReAddData, "testing");
            dbu.deleteMetaData(toDeleteAndReAddMeta, "testing");
            assertFalse("Expecting data to not exist.", dbu.hasData(toDeleteAndReAddData));
            assertFalse("Expecting meta data to exist.", dbu.hasMetaData(toDeleteAndReAddMeta));
            try {
                byte[] data = dbu.getData(toDeleteAndReAddData);
                if (data != null) {
                    fail("Expected data to not be available");
                }
            } catch (Exception ex) {
            }
            try {
                byte[] data = dbu.getMetaData(toDeleteAndReAddMeta);
                if (data != null) {
                    fail("Expected data to not be available");
                }
            } catch (Exception ex) {
            }
            if (useDifferentBytesWhenReAdding) {
                dataBytes = DevUtil.createRandomDataChunk(1024 * 1024);
                metaBytes = DevUtil.createRandomMetaDataChunk();
            }
            dbu.addData(toDeleteAndReAddData, dataBytes);
            dbu.addMetaData(toDeleteAndReAddMeta, metaBytes);
            assertTrue("Expecting data to exist.", dbu.hasData(toDeleteAndReAddData));
            assertNotNull("Expecting data to exist.", dbu.getData(toDeleteAndReAddData));
            assertTrue("Expecting meta data to exist.", dbu.hasMetaData(toDeleteAndReAddMeta));
            assertNotNull("Expecting meta data to exist.", dbu.getMetaData(toDeleteAndReAddMeta));
            dbu.deleteData(toDeleteAndReAddData, "testing");
            dbu.deleteMetaData(toDeleteAndReAddMeta, "testing");
            assertFalse("Expecting data to not exist.", dbu.hasData(toDeleteAndReAddData));
            assertFalse("Expecting meta data to exist.", dbu.hasMetaData(toDeleteAndReAddMeta));
            try {
                byte[] data = dbu.getData(toDeleteAndReAddData);
                if (data != null) {
                    fail("Expected data to not be available");
                }
            } catch (Exception ex) {
            }
            try {
                byte[] data = dbu.getMetaData(toDeleteAndReAddMeta);
                if (data != null) {
                    fail("Expected data to not be available");
                }
            } catch (Exception ex) {
            }
        } finally {
            if (home != null) {
                IOUtil.recursiveDelete(home);
            }
            dbu.close();
        }
    }

    /**
     * <p>Demonstrates DataBlock.moveToDataDirectoryConfiguration works.</p>
     */
    public void testMoveDataBlockToNewDataDirectoryConfiguration() throws Exception {
        TestUtil.printTitle("DataBlockTest:testMoveDataBlockToNewDataDirectoryConfiguration()");
        final DataBlockUtil dbu = new DataBlockUtil();
        File dataDir1 = null, dataDir2 = null;
        try {
            dataDir1 = TempFileUtil.createTemporaryDirectory();
            dataDir2 = TempFileUtil.createTemporaryDirectory();
            assertFalse("Directories should not be equal.", dataDir1.getAbsolutePath().equals(dataDir2.getAbsolutePath()));
            final DataDirectoryConfiguration ddc1 = new DataDirectoryConfiguration(dataDir1.getAbsolutePath(), Long.MAX_VALUE);
            final DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dataDir2.getAbsolutePath(), Long.MAX_VALUE);
            assertEquals("Expecting accurate bytes.", 0, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", 0, ddc2.getActualSize());
            dbu.add(ddc1);
            assertEquals("Expecting one data directory configuration.", 1, dbu.getDataDirectoryConfigurations().size());
            assertEquals("Expecting data directory to be empty.", 0, ddc1.getDirectoryFile().list().length);
            final byte[] dataChunk = DevUtil.createRandomDataChunkStartsWith("ab");
            final BigHash dataHash = new BigHash(dataChunk);
            final byte[] metaChunk = DevUtil.createRandomMetaDataChunk();
            final BigHash metaHash = DevUtil.getRandomBigHashStartsWith("ab");
            dbu.addData(dataHash, dataChunk);
            dbu.addMetaData(metaHash, metaChunk);
            assertTrue("Should have data hash.", dbu.hasData(dataHash));
            assertTrue("Should have meta hash.", dbu.hasMetaData(metaHash));
            assertEquals("Expecting one data block in data directory.", 1, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc1.getActualSize());
            dbu.add(ddc2);
            assertEquals("Expecting two data directory configurations.", 2, dbu.getDataDirectoryConfigurations().size());
            assertEquals("Expecting data directory to be empty.", 0, ddc2.getDirectoryFile().list().length);
            final DataBlock db = dbu.getDataBlockToAddChunk(dataHash);
            assertEquals("Data block should know it is in DDC #1.", ddc1, db.ddc);
            final boolean moved = db.moveToDataDirectoryConfiguration(ddc2);
            assertTrue("Expecting data directory to be moved.", moved);
            assertEquals("Data block should know it is in DDC #2 now.", ddc2, db.ddc);
            assertEquals("Expecting data directory to be empty.", 0, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting one data block in data directory.", 1, ddc2.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", 0, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc2.getActualSize());
        } finally {
            IOUtil.recursiveDeleteWithWarning(dataDir1);
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            dbu.close();
        }
    }

    /**
     * <p>Demonstrates DataBlock.moveToDataDirectoryConfiguration fails if DataDirectoryConfiguration is full.</p>
     * @throws java.lang.Exception
     */
    public void testMoveDataBlockToNewDataDirectoryConfigurationFailsIfFull() throws Exception {
        TestUtil.printTitle("DataBlockTest:testMoveDataBlockToNewDataDirectoryConfigurationFailsIfFull()");
        final DataBlockUtil dbu = new DataBlockUtil();
        File dataDir1 = null, dataDir2 = null;
        try {
            dataDir1 = TempFileUtil.createTemporaryDirectory();
            dataDir2 = TempFileUtil.createTemporaryDirectory();
            assertFalse("Directories should not be equal.", dataDir1.getAbsolutePath().equals(dataDir2.getAbsolutePath()));
            final DataDirectoryConfiguration ddc1 = new DataDirectoryConfiguration(dataDir1.getAbsolutePath(), 2 * 1024 * 1024);
            final DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dataDir2.getAbsolutePath(), 2 * 1024 * 1024);
            assertEquals("Expecting accurate bytes.", 0, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", 0, ddc2.getActualSize());
            dbu.add(ddc1);
            assertEquals("Expecting one data directory configuration.", 1, dbu.getDataDirectoryConfigurations().size());
            assertEquals("Expecting data directory to be empty.", 0, ddc1.getDirectoryFile().list().length);
            final byte[] dataChunk = DevUtil.createRandomDataChunkStartsWith("ab");
            final BigHash dataHash = new BigHash(dataChunk);
            final byte[] metaChunk = DevUtil.createRandomMetaDataChunk();
            final BigHash metaHash = DevUtil.getRandomBigHashStartsWith("ab");
            dbu.addData(dataHash, dataChunk);
            dbu.addMetaData(metaHash, metaChunk);
            assertTrue("Should have data hash.", dbu.hasData(dataHash));
            assertTrue("Should have meta hash.", dbu.hasMetaData(metaHash));
            assertEquals("Expecting one data block in data directory.", 1, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc1.getActualSize());
            ddc2.adjustUsedSpace(1024 * 1024 * 2);
            dbu.add(ddc2);
            assertEquals("Expecting two data directory configurations.", 2, dbu.getDataDirectoryConfigurations().size());
            assertEquals("Expecting data directory to be empty.", 0, ddc2.getDirectoryFile().list().length);
            final DataBlock db = dbu.getDataBlockToAddChunk(dataHash);
            assertEquals("Data block should know it is in DDC #1.", ddc1, db.ddc);
            final boolean moved = db.moveToDataDirectoryConfiguration(ddc2);
            assertFalse("Should not move: DDC is at its limit.", moved);
            assertEquals("Data block should know it is still in DDC #1.", ddc1, db.ddc);
            assertEquals("Expecting data directory to be empty.", 0, ddc2.getDirectoryFile().list().length);
            assertEquals("Expecting one data block in data directory.", 1, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc1.getActualSize());
        } finally {
            IOUtil.recursiveDeleteWithWarning(dataDir1);
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            dbu.close();
        }
    }

    /**
     * <p>Demonstrates DataBlock.moveToDataDirectoryConfiguration creates the destination data directory if not exist.</p>
     */
    public void testMoveDataBlockToNewDataDirectoryConfigurationCreatesDirectory() throws Exception {
        TestUtil.printTitle("DataBlockTest:testMoveDataBlockToNewDataDirectoryConfigurationCreatesDirectory()");
        final DataBlockUtil dbu = new DataBlockUtil();
        File dataDir1 = null, dataDir2 = null;
        try {
            dataDir1 = TempFileUtil.createTemporaryDirectory();
            dataDir2 = TempFileUtil.createTemporaryDirectory();
            assertFalse("Directories should not be equal.", dataDir1.getAbsolutePath().equals(dataDir2.getAbsolutePath()));
            final DataDirectoryConfiguration ddc1 = new DataDirectoryConfiguration(dataDir1.getAbsolutePath(), Long.MAX_VALUE);
            final DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dataDir2.getAbsolutePath(), Long.MAX_VALUE);
            assertEquals("Expecting accurate bytes.", 0, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", 0, ddc2.getActualSize());
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            assertFalse("Data directory #2 should not longer exist.", dataDir2.exists());
            dbu.add(ddc1);
            assertEquals("Expecting one data directory configuration.", 1, dbu.getDataDirectoryConfigurations().size());
            assertEquals("Expecting data directory to be empty.", 0, ddc1.getDirectoryFile().list().length);
            final byte[] dataChunk = DevUtil.createRandomDataChunkStartsWith("ab");
            final BigHash dataHash = new BigHash(dataChunk);
            final byte[] metaChunk = DevUtil.createRandomMetaDataChunk();
            final BigHash metaHash = DevUtil.getRandomBigHashStartsWith("ab");
            dbu.addData(dataHash, dataChunk);
            dbu.addMetaData(metaHash, metaChunk);
            assertTrue("Should have data hash.", dbu.hasData(dataHash));
            assertTrue("Should have meta hash.", dbu.hasMetaData(metaHash));
            assertEquals("Expecting one data block in data directory.", 1, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc1.getActualSize());
            dbu.add(ddc2);
            assertEquals("Expecting two data directory configurations.", 2, dbu.getDataDirectoryConfigurations().size());
            final DataBlock db = dbu.getDataBlockToAddChunk(dataHash);
            assertEquals("Data block should know it is in DDC #1.", ddc1, db.ddc);
            final boolean moved = db.moveToDataDirectoryConfiguration(ddc2);
            assertTrue("Expecting data directory to be moved.", moved);
            assertEquals("Data block should know it is in DDC #2 now.", ddc2, db.ddc);
            assertEquals("Expecting data directory to be empty.", 0, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting one data block in data directory.", 1, ddc2.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", 0, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc2.getActualSize());
        } finally {
            IOUtil.recursiveDeleteWithWarning(dataDir1);
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            dbu.close();
        }
    }

    /**
     * <p>Demonstrates DataBlock.moveToDataDirectoryConfiguration fails if cannot use DDC.</p>
     * @throws java.lang.Exception
     */
    public void testMoveDataBlockToNewDataDirectoryConfigurationFailsIfCannotUseDDC() throws Exception {
        TestUtil.printTitle("DataBlockTest:testMoveDataBlockToNewDataDirectoryConfigurationFailsIfCannotUseDDC()");
        final DataBlockUtil dbu = new DataBlockUtil();
        File dataDir1 = null, dataDir2 = null;
        try {
            dataDir1 = TempFileUtil.createTemporaryDirectory();
            dataDir2 = TempFileUtil.createTemporaryDirectory();
            assertFalse("Directories should not be equal.", dataDir1.getAbsolutePath().equals(dataDir2.getAbsolutePath()));
            final DataDirectoryConfiguration ddc1 = new DataDirectoryConfiguration(dataDir1.getAbsolutePath(), Long.MAX_VALUE);
            final DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dataDir2.getAbsolutePath(), Long.MAX_VALUE);
            assertEquals("Expecting accurate bytes.", 0, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", 0, ddc2.getActualSize());
            dbu.add(ddc1);
            assertEquals("Expecting one data directory configuration.", 1, dbu.getDataDirectoryConfigurations().size());
            assertEquals("Expecting data directory to be empty.", 0, ddc1.getDirectoryFile().list().length);
            final byte[] dataChunk = DevUtil.createRandomDataChunkStartsWith("ab");
            final BigHash dataHash = new BigHash(dataChunk);
            final byte[] metaChunk = DevUtil.createRandomMetaDataChunk();
            final BigHash metaHash = DevUtil.getRandomBigHashStartsWith("ab");
            dbu.addData(dataHash, dataChunk);
            dbu.addMetaData(metaHash, metaChunk);
            assertTrue("Should have data hash.", dbu.hasData(dataHash));
            assertTrue("Should have meta hash.", dbu.hasMetaData(metaHash));
            assertEquals("Expecting one data block in data directory.", 1, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc1.getActualSize());
            dbu.add(ddc2);
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            assertFalse("Data directory #2 should not longer exist.", dataDir2.exists());
            final boolean created = dataDir2.createNewFile();
            assertTrue("Should have created file.", created);
            assertTrue("Data directory should actually be a file now.", dataDir2.isFile());
            assertEquals("Expecting two data directory configurations.", 2, dbu.getDataDirectoryConfigurations().size());
            final DataBlock db = dbu.getDataBlockToAddChunk(dataHash);
            assertEquals("Data block should know it is in DDC #1.", ddc1, db.ddc);
            try {
                final boolean moved = db.moveToDataDirectoryConfiguration(ddc2);
                fail("Should have throw exception, data directory is actually a file. (Was move successful?: " + moved + ")");
            } catch (Exception ex) {
            }
            assertEquals("Data block should know it is in DDC #1 still.", ddc1, db.ddc);
            assertEquals("Expecting one data block in data directory.", 1, ddc1.getDirectoryFile().list().length);
            assertEquals("Expecting accurate bytes.", dataChunk.length + metaChunk.length + DataBlock.getHeadersPerFile() * DataBlock.bytesPerEntry, ddc1.getActualSize());
            assertEquals("Expecting accurate bytes.", 0, ddc2.getActualSize());
        } finally {
            IOUtil.recursiveDeleteWithWarning(dataDir1);
            IOUtil.recursiveDeleteWithWarning(dataDir2);
            dbu.close();
        }
    }

    public void testDeleteDataChunksReturnsCorrectHashes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testDeleteDataChunksReturnsCorrectHashes()");
        testDeleteChunksReturnsCorrectHashes(false);
    }

    public void testDeleteMetaDataChunksReturnsCorrectHashes() throws Exception {
        TestUtil.printTitle("DataBlockTest:testDeleteMetaDataChunksReturnsCorrectHashes()");
        testDeleteChunksReturnsCorrectHashes(true);
    }

    /**
     * <p>Emulating a server that deletes a lot of its data, perhaps because doesn't have hash span. Want to make sure it reports correct hashes it has.</p>
     * @param isMetaData
     * @throws java.lang.Exception
     */
    private void testDeleteChunksReturnsCorrectHashes(boolean isMetaData) throws Exception {
        int hashesToUse = 226;
        int hashesToBufferInMemory = 50;
        FlatFileTrancheServer ffts = null;
        File dataDir = null;
        try {
            dataDir = TempFileUtil.createTemporaryDirectory("testDeleteChunksReturnsCorrectHashesFor" + (isMetaData ? "MetaData" : "Data"));
            ffts = new FlatFileTrancheServer(dataDir);
            ffts.getConfiguration().getUsers().add(DevUtil.getDevUser());
            DataBlockUtil dbu = ffts.getDataBlockUtil();
            dbu.dataHashes.setTestBufferSize(hashesToBufferInMemory);
            dbu.metaDataHashes.setTestBufferSize(hashesToBufferInMemory);
            List<BigHash> hashesAdded = new ArrayList();
            List<BigHash> hashesKept = new ArrayList();
            for (int count = 0; count < hashesToUse; count++) {
                if (!isMetaData) {
                    byte[] chunk = DevUtil.createRandomDataChunkVariableSize();
                    BigHash hash = new BigHash(chunk);
                    if (hashesAdded.contains(hash)) {
                        count--;
                        continue;
                    }
                    IOUtil.setData(ffts, DevUtil.getDevAuthority(), DevUtil.getDevPrivateKey(), hash, chunk);
                    hashesAdded.add(hash);
                    hashesKept.add(hash);
                } else {
                    byte[] chunk = DevUtil.createRandomMetaDataChunk();
                    BigHash hash = DevUtil.getRandomBigHash();
                    if (hashesAdded.contains(hash)) {
                        count--;
                        continue;
                    }
                    IOUtil.setMetaData(ffts, DevUtil.getDevAuthority(), DevUtil.getDevPrivateKey(), false, hash, chunk);
                    hashesAdded.add(hash);
                    hashesKept.add(hash);
                }
            }
            assertEquals("Should have added correct number of hashes.", hashesToUse, hashesAdded.size());
            for (BigHash h : hashesAdded) {
                if (!isMetaData) {
                    assertTrue("Should have data chunk.", IOUtil.hasData(ffts, h));
                } else {
                    assertTrue("Should have meta data chunk.", IOUtil.hasMetaData(ffts, h));
                }
            }
            {
                BigHash[] hashesReported = null;
                if (!isMetaData) {
                    hashesReported = ffts.getDataHashes(BigInteger.ZERO, BigInteger.valueOf(hashesToUse + 50));
                } else {
                    hashesReported = ffts.getMetaDataHashes(BigInteger.ZERO, BigInteger.valueOf(hashesToUse + 50));
                }
                assertEquals("Expecting server to report correct number of hashes.", hashesToUse, hashesReported.length);
            }
            boolean delete = true;
            for (BigHash h : hashesAdded) {
                delete = !delete;
                if (!delete) {
                    continue;
                }
                if (!isMetaData) {
                    dbu.deleteData(h, "testing");
                } else {
                    dbu.deleteMetaData(h, "testing");
                }
                hashesKept.remove(h);
            }
            assertEquals("Expecting exactly half hashes deleted.", hashesAdded.size(), hashesKept.size() * 2);
            if (!isMetaData) {
                assertEquals("Expecting a certain number of hashes.", hashesKept.size(), dbu.getDataHashes(0, hashesToUse + 50).length);
            } else {
                assertEquals("Expecting a certain number of hashes.", hashesKept.size(), dbu.getMetaDataHashes(0, hashesToUse + 50).length);
            }
            for (BigHash h : hashesAdded) {
                if (hashesKept.contains(h)) {
                    if (!isMetaData) {
                        assertTrue("Should have data chunk still.", IOUtil.hasData(ffts, h));
                    } else {
                        assertTrue("Should have meta data chunk still.", IOUtil.hasMetaData(ffts, h));
                    }
                } else {
                    if (!isMetaData) {
                        assertFalse("Should not have data chunk any longer.", IOUtil.hasData(ffts, h));
                    } else {
                        assertFalse("Should not have meta data chunk any longer.", IOUtil.hasMetaData(ffts, h));
                    }
                }
            }
        } finally {
            IOUtil.safeClose(ffts);
            IOUtil.recursiveDelete(dataDir);
        }
    }

    /**
     * <p>There was a bug: anything that was balanced no longer "seen" by data block util. Need to fix.</p>
     * @throws java.lang.Exception
     */
    public void testBalancedDataBlocksCanStillBeFound() throws Exception {
        TestUtil.printTitle("DataBlockTest:testBalancedDataBlocksCanStillBeFound()");
        FlatFileTrancheServer ffts = null;
        File dataDir1 = null, dataDir2 = null;
        final boolean wasTestingHashSpanFixingThread = TestUtil.isTestingHashSpanFixingThread();
        final boolean wasTesting = TestUtil.isTesting();
        try {
            TestUtil.setTesting(true);
            TestUtil.setTestingHashSpanFixingThread(false);
            dataDir1 = TempFileUtil.createTemporaryDirectory("testBalancedDataBlocksCanStillBeFound-1");
            dataDir2 = TempFileUtil.createTemporaryDirectory("testBalancedDataBlocksCanStillBeFound-2");
            ffts = new FlatFileTrancheServer(dataDir1);
            ffts.setConfigurationCacheTime(0);
            ffts.getConfiguration().getUsers().add(DevUtil.getDevUser());
            assertEquals("Expecting flat file server to start with 1 DDC.", 1, ffts.getConfiguration().getDataDirectories().size());
            long maxSize = 6 * 1024 * 1024;
            DataDirectoryConfiguration ddc1 = ffts.getConfiguration().getDataDirectories().toArray(new DataDirectoryConfiguration[0])[0];
            ddc1.setSizeLimit(maxSize);
            Set<BigHash> dataChunks = new HashSet();
            Set<BigHash> metaDataChunks = new HashSet();
            DATA: for (int i = 0; i < 5; i++) {
                byte[] dataChunk = DevUtil.createRandomDataChunk1MB();
                BigHash dataHash = new BigHash(dataChunk);
                assertEquals("Expecting data chunk to be exactly 1MB", 1024 * 1024, dataChunk.length);
                if (dataChunks.contains(dataHash)) {
                    i--;
                    continue DATA;
                }
                ffts.getDataBlockUtil().addData(dataHash, dataChunk);
                dataChunks.add(dataHash);
            }
            META_DATA: for (int i = 0; i < 5; i++) {
                byte[] metaDataChunk = DevUtil.createRandomMetaDataChunk();
                BigHash metaDataHash = DevUtil.getRandomBigHash();
                while (metaDataChunks.contains(metaDataHash)) {
                    metaDataHash = DevUtil.getRandomBigHash();
                }
                ffts.getDataBlockUtil().addMetaData(metaDataHash, metaDataChunk);
                metaDataChunks.add(metaDataHash);
            }
            for (BigHash hash : dataChunks) {
                assertTrue("Better have data; just added it", IOUtil.hasData(ffts, hash));
            }
            for (BigHash hash : metaDataChunks) {
                assertTrue("Better have meta data; just added it", IOUtil.hasMetaData(ffts, hash));
            }
            DataDirectoryConfiguration ddc2 = new DataDirectoryConfiguration(dataDir2.getAbsolutePath(), maxSize);
            ffts.getConfiguration().getDataDirectories().add(ddc2);
            assertEquals("Should have two data directory configurations now.", 2, ffts.getConfiguration().getDataDirectories().size());
            for (DataDirectoryConfiguration ddc : ffts.getConfiguration().getDataDirectories()) {
                assertEquals("Each ddc should have the same size limit.", maxSize, ddc.getSizeLimit());
            }
            ffts.getConfiguration().setValue(ConfigKeys.HASHSPANFIX_SHOULD_HEALING_THREAD_BALANCE, String.valueOf(true));
            Configuration config = ffts.getConfiguration();
            IOUtil.setConfiguration(ffts, config, DevUtil.getDevAuthority(), DevUtil.getDevPrivateKey());
            long timeToSleep = 1000;
            System.out.println("Sleeping: " + timeToSleep);
            Thread.sleep(timeToSleep);
            for (BigHash hash : dataChunks) {
                assertTrue("Better have data; added second ddc, but not balanced yet", IOUtil.hasData(ffts, hash));
                Object o = IOUtil.getData(ffts, hash, false).getReturnValueObject();
                byte[] data = null;
                if (o instanceof byte[]) {
                    data = (byte[]) o;
                } else if (o instanceof byte[][]) {
                    data = ((byte[][]) o)[0];
                } else {
                    fail("Expected return object to be type byte[] or byte[][], but wasn't.");
                }
                assertNotNull(data);
            }
            for (BigHash hash : metaDataChunks) {
                assertTrue("Better have meta data; added second ddc, but not balanced yet", IOUtil.hasMetaData(ffts, hash));
                byte[] bytes = (byte[]) IOUtil.getMetaData(ffts, hash, false).getReturnValueObject();
                assertNotNull(bytes);
            }
            ffts.getDataBlockUtil().setMinSizeAvailableInTargetDataBlockBeforeBalance(1024 * 1024);
            for (int i = 0; i < 2; i++) {
                boolean wasBalanced = ffts.getDataBlockUtil().balanceDataDirectoryConfigurations();
                if (!wasBalanced) {
                    Thread.sleep(250);
                }
            }
            for (DataDirectoryConfiguration ddc : ffts.getConfiguration().getDataDirectories()) {
                System.out.println("After balancing, " + ddc.getDirectory() + ": " + ddc.getActualSize());
            }
            for (BigHash hash : dataChunks) {
                assertTrue("Better have data; just balanced, should still be available", IOUtil.hasData(ffts, hash));
                byte[] bytes = (byte[]) IOUtil.getData(ffts, hash, false).getReturnValueObject();
                assertNotNull(bytes);
            }
            for (BigHash hash : metaDataChunks) {
                assertTrue("Better have meta data; just balanced, should still be available", IOUtil.hasMetaData(ffts, hash));
                byte[] bytes = (byte[]) IOUtil.getMetaData(ffts, hash, false).getReturnValueObject();
                assertNotNull(bytes);
            }
        } finally {
            IOUtil.safeClose(ffts);
            IOUtil.recursiveDelete(dataDir1);
            IOUtil.recursiveDelete(dataDir2);
            TestUtil.setTesting(wasTesting);
            TestUtil.setTestingHashSpanFixingThread(wasTestingHashSpanFixingThread);
        }
    }
}
