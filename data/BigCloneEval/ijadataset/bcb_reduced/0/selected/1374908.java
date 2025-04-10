package org.archive.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.archive.util.ArchiveUtils;
import com.google.common.io.NullOutputStream;
import com.google.common.primitives.Bytes;

/**
 * Tests for GZIPMembersInputStream
 * @contributor gojomo
 * @version $ $
 */
public class GZIPMembersInputStreamTest extends TestCase {

    byte[] noise1k_gz;

    byte[] noise32k_gz;

    byte[] a_gz;

    byte[] hello_gz;

    byte[] allfour_gz;

    byte[] sixsmall_gz;

    {
        Random rand = new Random(1);
        try {
            byte[] buf = new byte[1024];
            rand.nextBytes(buf);
            noise1k_gz = ArchiveUtils.gzip(buf);
            buf = new byte[32 * 1024];
            rand.nextBytes(buf);
            noise32k_gz = ArchiveUtils.gzip(buf);
            a_gz = ArchiveUtils.gzip("a".getBytes("ASCII"));
            hello_gz = ArchiveUtils.gzip("hello".getBytes("ASCII"));
            allfour_gz = Bytes.concat(noise1k_gz, noise32k_gz, a_gz, hello_gz);
            sixsmall_gz = Bytes.concat(a_gz, hello_gz, a_gz, hello_gz, a_gz, hello_gz);
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(GZIPMembersInputStreamTest.class);
    }

    public void testFullReadAllFour() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        int count = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong length uncompressed data", 1024 + (32 * 1024) + 1 + 5, count);
    }

    public void testFullReadSixSmall() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(sixsmall_gz));
        int count = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong length uncompressed data", 1 + 5 + 1 + 5 + 1 + 5, count);
    }

    public void testReadPerMemberAllFour() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        gzin.setEofEachMember(true);
        int count0 = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong 1k member count", 1024, count0);
        assertEquals("wrong member number", 0, gzin.getMemberNumber());
        assertEquals("wrong member0 start", 0, gzin.getCurrentMemberStart());
        assertEquals("wrong member0 end", noise1k_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count1 = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong 32k member count", (32 * 1024), count1);
        assertEquals("wrong member number", 1, gzin.getMemberNumber());
        assertEquals("wrong member1 start", noise1k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member1 end", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count2 = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong 1-byte member count", 1, count2);
        assertEquals("wrong member number", 2, gzin.getMemberNumber());
        assertEquals("wrong member2 start", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member2 end", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count3 = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong 5-byte member count", 5, count3);
        assertEquals("wrong member number", 3, gzin.getMemberNumber());
        assertEquals("wrong member3 start", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member3 end", noise1k_gz.length + noise32k_gz.length + a_gz.length + hello_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int countEnd = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong eof count", 0, countEnd);
    }

    public void testReadPerMemberSixSmall() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(sixsmall_gz));
        gzin.setEofEachMember(true);
        for (int i = 0; i < 3; i++) {
            int count2 = IOUtils.copy(gzin, new NullOutputStream());
            assertEquals("wrong 1-byte member count", 1, count2);
            gzin.nextMember();
            int count3 = IOUtils.copy(gzin, new NullOutputStream());
            assertEquals("wrong 5-byte member count", 5, count3);
            gzin.nextMember();
        }
        int countEnd = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong eof count", 0, countEnd);
    }

    public void testByteReadPerMember() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        gzin.setEofEachMember(true);
        int count0 = 0;
        while (gzin.read() > -1) count0++;
        assertEquals("wrong 1k member count", 1024, count0);
        assertEquals("wrong member number", 0, gzin.getMemberNumber());
        assertEquals("wrong member0 start", 0, gzin.getCurrentMemberStart());
        assertEquals("wrong member0 end", noise1k_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count1 = 0;
        while (gzin.read() > -1) count1++;
        assertEquals("wrong 32k member count", (32 * 1024), count1);
        assertEquals("wrong member number", 1, gzin.getMemberNumber());
        assertEquals("wrong member1 start", noise1k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member1 end", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count2 = 0;
        while (gzin.read() > -1) count2++;
        assertEquals("wrong 1-byte member count", 1, count2);
        assertEquals("wrong member number", 2, gzin.getMemberNumber());
        assertEquals("wrong member2 start", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member2 end", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count3 = 0;
        while (gzin.read() > -1) count3++;
        assertEquals("wrong 5-byte member count", 5, count3);
        assertEquals("wrong member number", 3, gzin.getMemberNumber());
        assertEquals("wrong member3 start", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member3 end", noise1k_gz.length + noise32k_gz.length + a_gz.length + hello_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int countEnd = 0;
        while (gzin.read() > -1) countEnd++;
        assertEquals("wrong eof count", 0, countEnd);
    }

    public void testMemberSeek() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        gzin.setEofEachMember(true);
        gzin.compressedSeek(noise1k_gz.length + noise32k_gz.length);
        int count2 = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong 1-byte member count", 1, count2);
        assertEquals("wrong Member2 start", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong Member2 end", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int count3 = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong 5-byte member count", 5, count3);
        assertEquals("wrong Member3 start", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong Member3 end", noise1k_gz.length + noise32k_gz.length + a_gz.length + hello_gz.length, gzin.getCurrentMemberEnd());
        gzin.nextMember();
        int countEnd = IOUtils.copy(gzin, new NullOutputStream());
        assertEquals("wrong eof count", 0, countEnd);
    }

    @SuppressWarnings("deprecation")
    public void testMemberIterator() throws IOException {
        GZIPMembersInputStream gzin = new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        Iterator<GZIPMembersInputStream> iter = gzin.memberIterator();
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember0 = iter.next();
        int count0 = IOUtils.copy(gzMember0, new NullOutputStream());
        assertEquals("wrong 1k member count", 1024, count0);
        assertEquals("wrong member number", 0, gzin.getMemberNumber());
        assertEquals("wrong member0 start", 0, gzin.getCurrentMemberStart());
        assertEquals("wrong member0 end", noise1k_gz.length, gzin.getCurrentMemberEnd());
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember1 = iter.next();
        int count1 = IOUtils.copy(gzMember1, new NullOutputStream());
        assertEquals("wrong 32k member count", (32 * 1024), count1);
        assertEquals("wrong member number", 1, gzin.getMemberNumber());
        assertEquals("wrong member1 start", noise1k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member1 end", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberEnd());
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember2 = iter.next();
        int count2 = IOUtils.copy(gzMember2, new NullOutputStream());
        assertEquals("wrong 1-byte member count", 1, count2);
        assertEquals("wrong member number", 2, gzin.getMemberNumber());
        assertEquals("wrong member2 start", noise1k_gz.length + noise32k_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member2 end", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberEnd());
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember3 = iter.next();
        int count3 = IOUtils.copy(gzMember3, new NullOutputStream());
        assertEquals("wrong 5-byte member count", 5, count3);
        assertEquals("wrong member number", 3, gzin.getMemberNumber());
        assertEquals("wrong member3 start", noise1k_gz.length + noise32k_gz.length + a_gz.length, gzin.getCurrentMemberStart());
        assertEquals("wrong member3 end", noise1k_gz.length + noise32k_gz.length + a_gz.length + hello_gz.length, gzin.getCurrentMemberEnd());
        assertFalse(iter.hasNext());
    }
}
