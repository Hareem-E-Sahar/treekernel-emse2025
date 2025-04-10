package org.jcvi.common.core.assembly.ace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jcvi.assembly.ace.Ace2Contig;
import org.jcvi.common.core.assembly.Contig;
import org.jcvi.common.core.assembly.PlacedRead;
import org.jcvi.common.core.assembly.ace.AceContig;
import org.jcvi.common.core.assembly.ace.AceContigDataStore;
import org.jcvi.common.core.assembly.ace.AcePlacedRead;
import org.jcvi.common.core.assembly.ace.AllAceUnitTests;
import org.jcvi.common.core.assembly.ace.DefaultAceFileDataStore;
import org.jcvi.common.core.assembly.ctg.DefaultContigFileDataStore;
import org.jcvi.common.core.datastore.DataStoreException;
import org.jcvi.common.core.io.IOUtil;
import org.jcvi.common.core.util.iter.CloseableIterator;
import org.jcvi.common.io.fileServer.ResourceFileServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/**
 * @author dkatzel
 *
 *
 */
public class TestAce2Contig {

    private ResourceFileServer resources;

    File actualContigFile;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        resources = new ResourceFileServer(AllAceUnitTests.class);
        actualContigFile = folder.newFile("actual.contig");
    }

    @Test
    public void parseAllContigs() throws IOException, DataStoreException {
        File aceFile = resources.getFile("files/sample.ace");
        File expectedContigFile = resources.getFile("files/sample.contig");
        Ace2Contig.main(new String[] { "-a", aceFile.getAbsolutePath(), "-c", actualContigFile.getAbsolutePath() });
        FileInputStream actualStream = new FileInputStream(actualContigFile);
        IOUtil.closeAndIgnoreErrors(actualStream);
        DefaultContigFileDataStore contigFileDataStore = new DefaultContigFileDataStore(actualContigFile);
        Contig<PlacedRead> contig = contigFileDataStore.get("Contig1");
        AceContigDataStore aceContigDataStore = DefaultAceFileDataStore.create(aceFile);
        AceContig aceContig = aceContigDataStore.get("Contig1");
        assertEquals(aceContig.getConsensus().asList(), contig.getConsensus().asList());
        CloseableIterator<AcePlacedRead> iter = null;
        try {
            iter = aceContig.getReadIterator();
            while (iter.hasNext()) {
                AcePlacedRead expectedRead = iter.next();
                PlacedRead actualRead = contig.getRead(expectedRead.getId());
                assertEquals(expectedRead.getNucleotideSequence().asList(), actualRead.getNucleotideSequence().asList());
                assertEquals(expectedRead.asRange(), actualRead.asRange());
            }
        } finally {
            IOUtil.closeAndIgnoreErrors(iter);
        }
        assertContentEquals(expectedContigFile, actualContigFile);
    }

    private void assertContentEquals(File expected, File actual) throws IOException {
        assertEquals(expected.length(), actual.length());
        InputStream expectedStream = new FileInputStream(expected);
        InputStream actualStream = new FileInputStream(actual);
        try {
            assertArrayEquals(IOUtil.toByteArray(expectedStream), IOUtil.toByteArray(actualStream));
        } finally {
            IOUtil.closeAndIgnoreErrors(expectedStream, actualStream);
        }
    }
}
