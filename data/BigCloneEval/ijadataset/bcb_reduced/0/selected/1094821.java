package br.ufal.tci.nexos.arcolive.beans;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.media.Buffer;
import javax.media.Control;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

public class LiveStream implements PullBufferStream {

    private int seqNo = 0;

    private int x = 0;

    private int y = 0;

    private int width;

    private int height;

    private int maxDataLength;

    private ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW);

    private Dimension size;

    private Format rgbFormat;

    private float frameRate = 1f;

    private Control[] controls = new Control[0];

    private Robot robot = null;

    public LiveStream(int width, int height, int frameRate) {
        this.width = width;
        this.height = height;
        size = new Dimension(width, height);
        try {
            robot = new Robot();
        } catch (AWTException awe) {
            throw new RuntimeException("");
        }
        maxDataLength = size.width * size.height * 3;
        rgbFormat = new RGBFormat(size, maxDataLength, Format.intArray, frameRate, 32, 0xFF0000, 0xFF00, 0xFF, 1, size.width, VideoFormat.FALSE, Format.NOT_SPECIFIED);
    }

    public ContentDescriptor getContentDescriptor() {
        return cd;
    }

    public long getContentLength() {
        return LENGTH_UNKNOWN;
    }

    public boolean endOfStream() {
        return false;
    }

    public Format getFormat() {
        return rgbFormat;
    }

    public void read(Buffer buffer) throws IOException {
        synchronized (this) {
            Object outdata = buffer.getData();
            if (outdata == null || !(outdata.getClass() == Format.intArray) || ((int[]) outdata).length < maxDataLength) {
                outdata = new int[maxDataLength];
                buffer.setData(outdata);
            }
            buffer.setFormat(rgbFormat);
            buffer.setTimeStamp((long) (seqNo * (1000 / frameRate) * 1000000));
            BufferedImage bi = robot.createScreenCapture(new Rectangle(x, y, width, height));
            bi.getRGB(0, 0, width, height, (int[]) outdata, 0, width);
            buffer.setSequenceNumber(seqNo);
            buffer.setLength(maxDataLength);
            buffer.setFlags(Buffer.FLAG_KEY_FRAME);
            buffer.setHeader(null);
            seqNo++;
        }
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        synchronized (this) {
            notifyAll();
        }
    }

    public Object[] getControls() {
        return controls;
    }

    public Object getControl(String controlType) {
        try {
            Class cls = Class.forName(controlType);
            Object cs[] = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) return cs[i];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean willReadBlock() {
        return false;
    }
}
