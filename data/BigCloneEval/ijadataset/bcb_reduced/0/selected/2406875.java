package org.openmeetings.webstart.screen;

import java.awt.*;
import java.io.*;
import java.util.Date;
import java.net.URLConnection;
import java.net.URL;
import java.awt.image.BufferedImage;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import org.openmeetings.webstart.beans.ConnectionBean;
import org.openmeetings.webstart.beans.VirtualScreenBean;
import org.openmeetings.webstart.gui.StartScreen;

public class CaptureScreen {

    private Date startDate;

    public static void main(String[] args) {
        new CaptureScreen("http://192.168.2.103:5080/xmlcrm/ScreenServlet", "2010", "1", "public", "", "");
    }

    public CaptureScreen(String url, String SID, String room, String domain, String publicSID, String record) {
        try {
            System.err.println("captureScreenStart");
            this.startDate = new Date();
            ConnectionBean.isloading = true;
            this.captureScreen(url + "?sid=" + SID + "&room=" + room + "&domain=" + domain + "&publicSID=" + publicSID + "&record=" + record, "myscreenRemote.jpg");
        } catch (Exception io) {
            System.err.println(io);
            System.out.println(io);
        }
    }

    public void captureScreen(String url, String fileName) throws Exception {
        this.sendJpegToUrl(this.bufferImage(), url, fileName);
    }

    public byte[] bufferImage() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rectangle screenRectangle = new Rectangle(VirtualScreenBean.vScreenSpinnerX, VirtualScreenBean.vScreenSpinnerY, VirtualScreenBean.vScreenSpinnerWidth, VirtualScreenBean.vScreenSpinnerHeight);
            Robot robot = VirtualScreenBean.robot;
            if (robot == null) robot = new Robot();
            BufferedImage imageScreen = robot.createScreenCapture(screenRectangle);
            double width = imageScreen.getWidth();
            double height = imageScreen.getHeight();
            Date endTime = new Date();
            long timeInSeconds = (endTime.getTime() - this.startDate.getTime()) / 1000;
            System.out.println("1 buffer start , end , delta " + this.startDate + " " + endTime + " :timeInSeconds: " + timeInSeconds);
            double thumbWidth = 600;
            double thumbHeight = 600;
            BufferedImage image = null;
            Image img = null;
            double div = width / thumbWidth;
            System.out.println(" height:" + height + " width: " + width);
            height = height / div;
            System.out.println("div: " + div + " newheight:" + height);
            if (height > thumbHeight) {
                double divHeight = height / thumbHeight;
                thumbWidth = thumbWidth / divHeight;
                height = thumbHeight;
            }
            System.out.println("final height:" + height + " width: " + thumbWidth);
            img = imageScreen.getScaledInstance(Double.valueOf(thumbWidth).intValue(), Double.valueOf(height).intValue(), Image.SCALE_SMOOTH);
            image = new BufferedImage(Double.valueOf(thumbWidth).intValue(), Double.valueOf(height).intValue(), BufferedImage.TYPE_INT_RGB);
            Graphics2D biContext = image.createGraphics();
            biContext.drawImage(img, 0, 0, null);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam encpar = encoder.getDefaultJPEGEncodeParam(image);
            encpar.setQuality(ConnectionBean.imgQuality, false);
            encoder.setJPEGEncodeParam(encpar);
            encoder.encode(image);
            imageScreen.flush();
            image.flush();
            if (img != null) img.flush();
            return out.toByteArray();
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (IOException ioe) {
            System.out.println(ioe);
            ioe.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return null;
    }

    public void sendJpegToUrl(byte[] imageAsBytes, String url, String fileName) {
        try {
            System.out.println("sendJpegToUrl url  " + url);
            Date endTime = new Date();
            long timeInSeconds = (endTime.getTime() - this.startDate.getTime()) / 1000;
            System.out.println("2  scaled , end , delta " + this.startDate + " " + endTime + " :timeInSeconds: " + timeInSeconds);
            URL u = new URL(url);
            URLConnection c = u.openConnection();
            c.setDoOutput(true);
            c.setDoInput(true);
            c.setUseCaches(false);
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=AXi93A");
            DataOutputStream dstream = new DataOutputStream(c.getOutputStream());
            dstream.writeBytes("--AXi93A\r\n");
            dstream.writeBytes("Content-Disposition: form-data; name=\"Filedata\"; filename=\"" + fileName + "\" \r\nContent-Type: image/jpeg\r\nContent-Transfer-Encoding: binary\r\n");
            dstream.write(imageAsBytes, 0, imageAsBytes.length);
            dstream.writeBytes("\r\n--AXi93A--\r\n\r\n");
            dstream.flush();
            dstream.close();
            System.out.println("sendJpegToUrl complete ");
            StartScreen.instance.showBandwidthWarning("send complete ");
            DataInputStream in = new DataInputStream(new BufferedInputStream(c.getInputStream()));
            String sIn = in.readLine();
            while (sIn != null) {
                if (sIn != null) {
                    System.out.println(sIn);
                }
                sIn += in.readLine();
            }
            StartScreen.instance.showBandwidthWarning("Input Stream: " + sIn);
            Date endTime2 = new Date();
            long timeInSeconds2 = (endTime2.getTime() - this.startDate.getTime()) / 1000;
            System.out.println("3 send start , end , delta " + this.startDate + " " + endTime2 + " :timeInSeconds: " + timeInSeconds2);
            ConnectionBean.isloading = false;
        } catch (Exception e) {
            StartScreen.instance.showBandwidthWarning("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
