import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import junit.framework.Assert;

public class MockZipOutputStream extends ZipOutputStream {

    private List<byte[]> expectedList = new ArrayList<byte[]>();

    private List<byte[]> actualList = new ArrayList<byte[]>();

    private List<String> expectedFileList = new ArrayList<String>();

    private List<String> actualFileList = new ArrayList<String>();

    public MockZipOutputStream(FileOutputStream zipAdi) {
        super(zipAdi);
    }

    public MockZipOutputStream() {
        super(null);
    }

    public void addExpectedFileLine(String beklenen) {
        expectedFileList.add(beklenen);
    }

    public void addExpectedLine(byte[] beklenen) {
        expectedList.add(beklenen);
    }

    public void verify() {
        if (expectedList.size() != actualList.size()) Assert.fail("Beklenen deger sayisi:" + expectedList.size() + "Olusan deger sayisi:" + actualList.size());
        if (expectedFileList.size() != actualFileList.size()) Assert.fail("Beklenen deger sayisi:" + expectedFileList.size() + "Olusan deger sayisi:" + actualFileList.size());
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void putNextEntry(ZipEntry e) throws IOException {
        if (expectedFileList.size() <= actualFileList.size()) Assert.fail("beklenen deger fazla");
        actualFileList.add(e.getName());
    }

    @Override
    public synchronized void write(byte[] b, int baslangic, int uzunluk) throws IOException {
        if (expectedList.size() <= actualList.size()) Assert.fail("beklenen deger fazla");
        int index = actualList.size();
        byte[] beklenenSatir = expectedList.get(index);
        System.out.println("beklenen :" + beklenenSatir);
        System.out.println("gelen :" + b[0] + b[1] + b[2] + b[3] + b[4]);
        Assert.assertTrue(beklenenSatir.equals(b));
        actualList.add(b);
    }

    @Override
    public void closeEntry() throws IOException {
    }
}
