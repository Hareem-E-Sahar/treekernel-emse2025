package com.levigo.jbig2;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.imageio.stream.ImageInputStream;
import org.junit.Ignore;
import org.junit.Test;
import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.util.JBIG2Exception;

@Ignore
public class ChecksumCalculator {

    @Ignore
    @Test
    public void computeChecksum() throws NoSuchAlgorithmException, IOException, JBIG2Exception {
        String filepath = "/images/sampledata_page3.jb2";
        int pageNumber = 1;
        InputStream is = getClass().getResourceAsStream(filepath);
        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream iis = disf.getInputStream(is);
        JBIG2Document doc = new JBIG2Document(iis);
        Bitmap b = doc.getPage(pageNumber).getBitmap();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(b.getByteArray());
        for (byte d : digest) {
            System.out.print(d);
        }
    }
}
