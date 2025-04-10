package test.unit.be.fedict.eid.dss.document.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import be.fedict.eid.dss.document.zip.ZIPSignatureOutputStream;

public class ZIPSignatureOutputStreamTest {

    private static final Log LOG = LogFactory.getLog(ZIPSignatureOutputStreamTest.class);

    @Test
    public void testZIPpackage() throws Exception {
        File zipFile = File.createTempFile("test-", ".zip");
        zipFile.deleteOnExit();
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOutputStream.putNextEntry(new ZipEntry("a.txt"));
        IOUtils.write("hello world", zipOutputStream);
        zipOutputStream.putNextEntry(new ZipEntry("b.txt"));
        IOUtils.write("hello world 2", zipOutputStream);
        zipOutputStream.close();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZIPSignatureOutputStream testedInstance = new ZIPSignatureOutputStream(zipFile, outputStream);
        IOUtils.write("signature data", testedInstance);
        testedInstance.close();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        ZipEntry zipEntry;
        while (null != (zipEntry = zipInputStream.getNextEntry())) {
            LOG.debug("zip entry: " + zipEntry.getName());
        }
    }
}
