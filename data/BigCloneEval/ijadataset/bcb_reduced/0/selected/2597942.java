package org.yawlfoundation.yawl.editor.foundations;

import javax.swing.*;
import java.io.*;

public class ResourceLoader {

    public static JLabel getImageAsJLabel(String imageFile) {
        return new JLabel(getImageAsIcon(imageFile));
    }

    /**
   * 
   * @param imageFile
   * @return
   */
    public static ImageIcon getImageAsIcon(String imageFile) {
        try {
            InputStream in = ResourceLoader.class.getResourceAsStream(imageFile);
            final byte[] imageByteBuffer = convertToByteArray(in);
            in.close();
            return new ImageIcon(imageByteBuffer);
        } catch (Exception e) {
            return null;
        }
    }

    public static ImageIcon getExternalImageAsIcon(String imageFile) {
        try {
            FileInputStream in = new FileInputStream(imageFile);
            final byte[] imageByteBuffer = convertToByteArray(in);
            in.close();
            return new ImageIcon(imageByteBuffer);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] convertToByteArray(final InputStream is) throws IOException {
        final int BUF_SIZE = 16384;
        BufferedInputStream inStream = new BufferedInputStream(is);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];
        int bytesRead = 0;
        while ((bytesRead = inStream.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.flush();
        return outStream.toByteArray();
    }
}
