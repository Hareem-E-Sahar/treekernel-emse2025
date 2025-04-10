package org.esb.hive;

import junit.framework.TestCase;
import org.esb.BaseTest;
import org.esb.dao.MediaFilesDao;
import org.esb.jmx.JHiveRegistryException;
import org.esb.model.MediaFile;
import org.esb.model.MediaStream;

public class FileImporterTest extends BaseTest {

    public FileImporterTest(String testName) {
        super(testName);
    }

    public void testSingleFileImport() throws JHiveRegistryException {
        MediaFile file = new MediaFile(TEST_VIDEO_XUGGLE_PATH, TEST_VIDEO_FLV);
        int id = FileImporter.importFile(file);
        assertTrue(id > 0);
        assertEquals(file.getId(), id);
        assertEquals(2, file.getStreamCount());
        {
            assertEquals(TEST_VIDEO_FLV, file.getFilename());
            assertTrue(file.getPath().endsWith(TEST_VIDEO_XUGGLE_PATH));
            assertTrue(file.getSize() > 0);
            assertTrue(file.getDuration() > 0);
            assertEquals(id, file.getId());
            assertEquals(2, file.getStreamCount());
            MediaStream s1 = file.getStreams().get(0);
            assertNotNull(s1);
            assertEquals(0, s1.getStreamIndex());
            assertEquals(0, s1.getStreamType());
            assertEquals(22, s1.getCodecId());
            assertEquals(0, s1.getCodecType());
            assertEquals(424, s1.getWidth());
            assertEquals(176, s1.getHeight());
            assertEquals(0, s1.getBitrate());
            assertEquals(15, s1.getFrameRateNum());
            assertEquals(1, s1.getFrameRateDen());
            assertEquals(0, s1.getSampleRate());
            assertEquals(0, s1.getChannels());
            assertEquals(Long.MIN_VALUE, s1.getDuration().longValue());
            MediaStream s2 = file.getStreams().get(1);
            assertNotNull(s2);
            assertEquals(1, s2.getStreamIndex());
            assertEquals(1, s2.getStreamType());
            assertEquals(86017, s2.getCodecId());
            assertEquals(1, s2.getCodecType());
            assertEquals(0, s2.getWidth());
            assertEquals(0, s2.getHeight());
            assertEquals(64000, s2.getBitrate());
            assertEquals(0, s2.getFrameRateNum());
            assertEquals(0, s2.getFrameRateDen());
            assertEquals(22050, s2.getSampleRate());
            assertEquals(1, s2.getChannels());
            assertEquals(Long.MIN_VALUE, s2.getDuration().longValue());
        }
        MediaFile outfile = MediaFilesDao.getMediaFile(id);
        {
            assertEquals(file.getFilename(), outfile.getFilename());
            assertEquals(file.getPath(), outfile.getPath());
            assertTrue(file.getSize() > 0);
            assertTrue(file.getDuration() > 0);
            assertEquals(id, outfile.getId());
            assertEquals(2, outfile.getStreamCount());
            MediaStream s1 = outfile.getStreams().get(0);
            assertNotNull(s1);
            assertEquals(0, s1.getStreamIndex());
            assertEquals(0, s1.getStreamType());
            assertEquals(22, s1.getCodecId());
            assertEquals(0, s1.getCodecType());
            assertEquals(424, s1.getWidth());
            assertEquals(176, s1.getHeight());
            assertEquals(0, s1.getBitrate());
            assertEquals(15, s1.getFrameRateNum());
            assertEquals(1, s1.getFrameRateDen());
            assertEquals(0, s1.getSampleRate());
            assertEquals(0, s1.getChannels());
            assertEquals(Long.MIN_VALUE, s1.getDuration().longValue());
            MediaStream s2 = outfile.getStreams().get(1);
            assertNotNull(s2);
            assertEquals(1, s2.getStreamIndex());
            assertEquals(1, s2.getStreamType());
            assertEquals(86017, s2.getCodecId());
            assertEquals(1, s2.getCodecType());
            assertEquals(0, s2.getWidth());
            assertEquals(0, s2.getHeight());
            assertEquals(64000, s2.getBitrate());
            assertEquals(0, s2.getFrameRateNum());
            assertEquals(0, s2.getFrameRateDen());
            assertEquals(22050, s2.getSampleRate());
            assertEquals(1, s2.getChannels());
            assertEquals(Long.MIN_VALUE, s2.getDuration().longValue());
        }
        {
            assertEquals(file.getFilename(), outfile.getFilename());
            assertEquals(file.getPath(), outfile.getPath());
            assertEquals(file.getSize(), outfile.getSize());
            assertEquals(file.getDuration(), outfile.getDuration());
            assertEquals(file.getId(), outfile.getId());
            assertEquals(file.getStreamCount(), outfile.getStreamCount());
            MediaStream s1 = outfile.getStreams().get(0);
            MediaStream is1 = file.getStreams().get(0);
            assertNotNull(s1);
            assertNotNull(is1);
            assertEquals(is1.getStreamIndex(), s1.getStreamIndex());
            assertEquals(is1.getStreamType(), s1.getStreamType());
            assertEquals(is1.getCodecId(), s1.getCodecId());
            assertEquals(is1.getCodecType(), s1.getCodecType());
            assertEquals(is1.getWidth(), s1.getWidth());
            assertEquals(is1.getHeight(), s1.getHeight());
            assertEquals(is1.getBitrate(), s1.getBitrate());
            assertEquals(is1.getFrameRateNum(), s1.getFrameRateNum());
            assertEquals(is1.getFrameRateDen(), s1.getFrameRateDen());
            assertEquals(is1.getSampleRate(), s1.getSampleRate());
            assertEquals(is1.getSampleFormat(), s1.getSampleFormat());
            assertEquals(is1.getPixelFormat(), s1.getPixelFormat());
            assertEquals(is1.getChannels(), s1.getChannels());
            assertEquals(is1.getDuration().longValue(), s1.getDuration().longValue());
            MediaStream s2 = outfile.getStreams().get(1);
            MediaStream is2 = file.getStreams().get(1);
            assertNotNull(s2);
            assertNotNull(is2);
            assertEquals(is2.getStreamIndex(), s2.getStreamIndex());
            assertEquals(is2.getStreamType(), s2.getStreamType());
            assertEquals(is2.getCodecId(), s2.getCodecId());
            assertEquals(is2.getCodecType(), s2.getCodecType());
            assertEquals(is2.getWidth(), s2.getWidth());
            assertEquals(is2.getHeight(), s2.getHeight());
            assertEquals(is2.getBitrate(), s2.getBitrate());
            assertEquals(is2.getFrameRateNum(), s2.getFrameRateNum());
            assertEquals(is2.getFrameRateDen(), s2.getFrameRateDen());
            assertEquals(is2.getSampleRate(), s2.getSampleRate());
            assertEquals(is2.getChannels(), s2.getChannels());
            assertEquals(is2.getDuration().longValue(), s2.getDuration().longValue());
        }
    }
}
