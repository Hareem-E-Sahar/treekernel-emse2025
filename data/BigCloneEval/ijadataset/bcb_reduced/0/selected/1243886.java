package net.sf.croputils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.codec.binary.Base64;

public class GenerateAppletDemoData {

    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("src/test/testimages/img1.jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CropUtil.fastDownscaleJpeg(fis, baos, 300, -1, 0.8f);
            String b64 = new String(Base64.encodeBase64(baos.toByteArray()));
            System.out.println(b64);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
