package org.jopenray.rfb;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;
import javax.imageio.ImageIO;
import org.jopenray.adapter.RFBAdapter;
import org.jopenray.operation.CopyOperation;
import org.jopenray.server.thinclient.ThinClient;
import org.jopenray.server.thinclient.DisplayMessage;
import org.jopenray.server.thinclient.DisplayWriterThread;

public class VncCanvas extends Canvas implements KeyListener, MouseListener, MouseMotionListener {

    VncViewer viewer;

    RfbProto rfb;

    ColorModel cm8, cm24;

    Color[] colors;

    int bytesPixel;

    int maxWidth = 0, maxHeight = 0;

    int scalingFactor;

    int scaledWidth, scaledHeight;

    Image memImage;

    Graphics memGraphics;

    private Image rawPixelsImage;

    MemoryImageSource pixelsSource;

    byte[] pixels8;

    int[] pixels24;

    long statStartTime;

    int statNumUpdates;

    int statNumTotalRects;

    int statNumPixelRects;

    int statNumRectsTight;

    int statNumRectsTightJPEG;

    int statNumRectsZRLE;

    int statNumRectsHextile;

    int statNumRectsRaw;

    int statNumRectsCopy;

    int statNumBytesEncoded;

    int statNumBytesDecoded;

    byte[] zrleBuf;

    int zrleBufLen = 0;

    byte[] zrleTilePixels8;

    int[] zrleTilePixels24;

    ZlibInStream zrleInStream;

    boolean zrleRecWarningShown = false;

    byte[] zlibBuf;

    int zlibBufLen = 0;

    Inflater zlibInflater;

    static final int tightZlibBufferSize = 512;

    Inflater[] tightInflaters;

    Rectangle jpegRect;

    boolean inputEnabled;

    private ThinClient displayClient;

    public VncCanvas(ThinClient client, VncViewer v, int maxWidth_, int maxHeight_) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("null client");
        }
        this.displayClient = client;
        try {
            Class[] argClasses = { Boolean.TYPE };
            java.lang.reflect.Method method = getClass().getMethod("setFocusTraversalKeysEnabled", argClasses);
            Object[] argObjects = { new Boolean(false) };
            method.invoke(this, argObjects);
        } catch (Exception e) {
        }
        viewer = v;
        maxWidth = maxWidth_;
        maxHeight = maxHeight_;
        rfb = viewer.rfb;
        scalingFactor = viewer.options.scalingFactor;
        tightInflaters = new Inflater[4];
        cm8 = new DirectColorModel(8, 7, (7 << 3), (3 << 6));
        cm24 = new DirectColorModel(24, 0xFF0000, 0x00FF00, 0x0000FF);
        colors = new Color[256];
        for (int i = 0; i < 256; i++) colors[i] = new Color(cm8.getRGB(i));
        setPixelFormat();
        inputEnabled = false;
        if (!viewer.options.viewOnly) enableInput(true);
        addKeyListener(this);
    }

    public VncCanvas(VncViewer v) throws IOException {
        this(null, v, 0, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(scaledWidth, scaledHeight);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(scaledWidth, scaledHeight);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(scaledWidth, scaledHeight);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        synchronized (memImage) {
            if (rfb.framebufferWidth == scaledWidth) {
                g.drawImage(memImage, 0, 0, null);
            } else {
                paintScaledFrameBuffer(g);
            }
        }
        if (showSoftCursor) {
            int x0 = cursorX - hotX, y0 = cursorY - hotY;
            Rectangle r = new Rectangle(x0, y0, cursorWidth, cursorHeight);
            if (r.intersects(g.getClipBounds())) {
                g.drawImage(softCursor, x0, y0, null);
            }
        }
    }

    public void paintScaledFrameBuffer(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(memImage, 0, 0, scaledWidth, scaledHeight, null);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        if ((infoflags & (ALLBITS | ABORT)) == 0) {
            return true;
        } else {
            if ((infoflags & ALLBITS) != 0) {
                if (jpegRect != null) {
                    synchronized (jpegRect) {
                        memGraphics.drawImage(img, jpegRect.x, jpegRect.y, null);
                        scheduleRepaint(jpegRect.x, jpegRect.y, jpegRect.width, jpegRect.height);
                        jpegRect.notify();
                    }
                }
            }
            return false;
        }
    }

    public synchronized void enableInput(boolean enable) {
        if (enable && !inputEnabled) {
            inputEnabled = true;
            addMouseListener(this);
            addMouseMotionListener(this);
            if (viewer.showControls) {
                viewer.buttonPanel.enableRemoteAccessControls(true);
            }
            createSoftCursor();
        } else if (!enable && inputEnabled) {
            inputEnabled = false;
            removeMouseListener(this);
            removeMouseMotionListener(this);
            if (viewer.showControls) {
                viewer.buttonPanel.enableRemoteAccessControls(false);
            }
            createSoftCursor();
        }
    }

    public void setPixelFormat() throws IOException {
        if (viewer.options.eightBitColors) {
            rfb.writeSetPixelFormat(8, 8, false, true, 7, 7, 3, 0, 3, 6);
            bytesPixel = 1;
        } else {
            rfb.writeSetPixelFormat(32, 24, false, true, 255, 255, 255, 16, 8, 0);
            bytesPixel = 4;
        }
        updateFramebufferSize();
    }

    void updateFramebufferSize() {
        int fbWidth = rfb.framebufferWidth;
        int fbHeight = rfb.framebufferHeight;
        if (maxWidth > 0 && maxHeight > 0) {
            int f1 = maxWidth * 100 / fbWidth;
            int f2 = maxHeight * 100 / fbHeight;
            scalingFactor = Math.min(f1, f2);
            if (scalingFactor > 100) scalingFactor = 100;
            System.out.println("Scaling desktop at " + scalingFactor + "%");
        }
        scaledWidth = (fbWidth * scalingFactor + 50) / 100;
        scaledHeight = (fbHeight * scalingFactor + 50) / 100;
        if (memImage == null) {
            memImage = viewer.vncContainer.createImage(fbWidth, fbHeight);
            memGraphics = memImage.getGraphics();
        } else if (memImage.getWidth(null) != fbWidth || memImage.getHeight(null) != fbHeight) {
            synchronized (memImage) {
                memImage = viewer.vncContainer.createImage(fbWidth, fbHeight);
                memGraphics = memImage.getGraphics();
            }
        }
        if (bytesPixel == 1) {
            pixels24 = null;
            pixels8 = new byte[fbWidth * fbHeight];
            pixelsSource = new MemoryImageSource(fbWidth, fbHeight, cm8, pixels8, 0, fbWidth);
            zrleTilePixels24 = null;
            zrleTilePixels8 = new byte[64 * 64];
        } else {
            pixels8 = null;
            pixels24 = new int[fbWidth * fbHeight];
            pixelsSource = new MemoryImageSource(fbWidth, fbHeight, cm24, pixels24, 0, fbWidth);
            zrleTilePixels8 = null;
            zrleTilePixels24 = new int[64 * 64];
        }
        pixelsSource.setAnimated(true);
        rawPixelsImage = Toolkit.getDefaultToolkit().createImage(pixelsSource);
        if (viewer.inSeparateFrame) {
            if (viewer.desktopScrollPane != null) resizeDesktopFrame();
        } else {
            setSize(scaledWidth, scaledHeight);
        }
        viewer.moveFocusToDesktop();
    }

    void resizeDesktopFrame() {
        setSize(scaledWidth, scaledHeight);
        Insets insets = viewer.desktopScrollPane.getInsets();
        viewer.desktopScrollPane.setSize(scaledWidth + 2 * Math.min(insets.left, insets.right), scaledHeight + 2 * Math.min(insets.top, insets.bottom));
        viewer.vncFrame.pack();
        Dimension screenSize = viewer.vncFrame.getToolkit().getScreenSize();
        Dimension frameSize = viewer.vncFrame.getSize();
        Dimension newSize = frameSize;
        screenSize.height -= 30;
        screenSize.width -= 30;
        boolean needToResizeFrame = false;
        if (frameSize.height > screenSize.height) {
            newSize.height = screenSize.height;
            needToResizeFrame = true;
        }
        if (frameSize.width > screenSize.width) {
            newSize.width = screenSize.width;
            needToResizeFrame = true;
        }
        if (needToResizeFrame) {
            viewer.vncFrame.setSize(newSize);
        }
        viewer.desktopScrollPane.doLayout();
    }

    public void processNormalProtocol() throws Exception {
        viewer.checkRecordingStatus();
        rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth, rfb.framebufferHeight, false);
        resetStats();
        boolean statsRestarted = false;
        while (true) {
            int msgType = rfb.readServerMessageType();
            switch(msgType) {
                case RfbProto.FramebufferUpdate:
                    if (statNumUpdates == viewer.debugStatsExcludeUpdates && !statsRestarted) {
                        resetStats();
                        statsRestarted = true;
                    } else if (statNumUpdates == viewer.debugStatsMeasureUpdates && statsRestarted) {
                        viewer.disconnect();
                    }
                    rfb.readFramebufferUpdate();
                    statNumUpdates++;
                    boolean cursorPosReceived = false;
                    for (int i = 0; i < rfb.updateNRects; i++) {
                        rfb.readFramebufferUpdateRectHdr();
                        statNumTotalRects++;
                        int rx = rfb.updateRectX, ry = rfb.updateRectY;
                        int rw = rfb.updateRectW, rh = rfb.updateRectH;
                        if (rfb.updateRectEncoding == RfbProto.EncodingLastRect) break;
                        if (rfb.updateRectEncoding == RfbProto.EncodingNewFBSize) {
                            rfb.setFramebufferSize(rw, rh);
                            updateFramebufferSize();
                            break;
                        }
                        if (rfb.updateRectEncoding == RfbProto.EncodingXCursor || rfb.updateRectEncoding == RfbProto.EncodingRichCursor) {
                            handleCursorShapeUpdate(rfb.updateRectEncoding, rx, ry, rw, rh);
                            continue;
                        }
                        if (rfb.updateRectEncoding == RfbProto.EncodingPointerPos) {
                            softCursorMove(rx, ry);
                            cursorPosReceived = true;
                            continue;
                        }
                        long numBytesReadBefore = rfb.getNumBytesRead();
                        rfb.startTiming();
                        switch(rfb.updateRectEncoding) {
                            case RfbProto.EncodingRaw:
                                statNumRectsRaw++;
                                handleRawRect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingCopyRect:
                                statNumRectsCopy++;
                                handleCopyRect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingRRE:
                                handleRRERect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingCoRRE:
                                handleCoRRERect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingHextile:
                                statNumRectsHextile++;
                                handleHextileRect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingZRLE:
                                statNumRectsZRLE++;
                                handleZRLERect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingZlib:
                                handleZlibRect(rx, ry, rw, rh);
                                break;
                            case RfbProto.EncodingTight:
                                statNumRectsTight++;
                                handleTightRect(rx, ry, rw, rh);
                                break;
                            default:
                                throw new Exception("Unknown RFB rectangle encoding " + rfb.updateRectEncoding);
                        }
                        rfb.stopTiming();
                        statNumPixelRects++;
                        statNumBytesDecoded += rw * rh * bytesPixel;
                        statNumBytesEncoded += (int) (rfb.getNumBytesRead() - numBytesReadBefore);
                    }
                    boolean fullUpdateNeeded = false;
                    if (viewer.checkRecordingStatus()) fullUpdateNeeded = true;
                    if (viewer.deferUpdateRequests > 0 && rfb.available() == 0 && !cursorPosReceived) {
                        synchronized (rfb) {
                            try {
                                rfb.wait(viewer.deferUpdateRequests);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    viewer.autoSelectEncodings();
                    if (viewer.options.eightBitColors != (bytesPixel == 1)) {
                        setPixelFormat();
                        fullUpdateNeeded = true;
                    }
                    int w = rfb.framebufferWidth;
                    int h = rfb.framebufferHeight;
                    rfb.writeFramebufferUpdateRequest(0, 0, w, h, !fullUpdateNeeded);
                    break;
                case RfbProto.SetColourMapEntries:
                    throw new Exception("Can't handle SetColourMapEntries message");
                case RfbProto.Bell:
                    Toolkit.getDefaultToolkit().beep();
                    break;
                case RfbProto.ServerCutText:
                    String s = rfb.readServerCutText();
                    viewer.clipboard.setCutText(s);
                    break;
                default:
                    throw new Exception("Unknown RFB message type " + msgType);
            }
        }
    }

    void handleRawRect(int x, int y, int w, int h) throws IOException {
        handleRawRect(x, y, w, h, true);
    }

    void handleRawRect(int x, int y, int w, int h, boolean paint) throws IOException {
        if (bytesPixel == 1) {
            for (int dy = y; dy < y + h; dy++) {
                rfb.readFully(pixels8, dy * rfb.framebufferWidth + x, w);
                if (rfb.rec != null) {
                    rfb.rec.write(pixels8, dy * rfb.framebufferWidth + x, w);
                }
            }
        } else {
            byte[] buf = new byte[w * 4];
            int i, offset;
            for (int dy = y; dy < y + h; dy++) {
                rfb.readFully(buf);
                if (rfb.rec != null) {
                    rfb.rec.write(buf);
                }
                offset = dy * rfb.framebufferWidth + x;
                for (i = 0; i < w; i++) {
                    pixels24[offset + i] = (buf[i * 4 + 2] & 0xFF) << 16 | (buf[i * 4 + 1] & 0xFF) << 8 | (buf[i * 4] & 0xFF);
                }
            }
        }
        handleUpdatedPixels(x, y, w, h);
        if (paint) scheduleRepaint(x, y, w, h);
    }

    void handleCopyRect(int x, int y, int w, int h) throws IOException {
        rfb.readCopyRect();
        memGraphics.copyArea(rfb.copyRectSrcX, rfb.copyRectSrcY, w, h, x - rfb.copyRectSrcX, y - rfb.copyRectSrcY);
        System.err.println("Blit:" + x + "," + y + " " + w + "x" + h + " from " + rfb.copyRectSrcX + "," + rfb.copyRectSrcY);
        DisplayMessage m = new DisplayMessage(this.displayClient.getWriter());
        m.addOperation(new CopyOperation(x, y, w, h, rfb.copyRectSrcX, rfb.copyRectSrcY));
        this.displayClient.getWriter().addMessage(m);
    }

    void handleRRERect(int x, int y, int w, int h) throws IOException {
        int nSubrects = rfb.readU32();
        byte[] bg_buf = new byte[bytesPixel];
        rfb.readFully(bg_buf);
        Color pixel;
        if (bytesPixel == 1) {
            pixel = colors[bg_buf[0] & 0xFF];
        } else {
            pixel = new Color(bg_buf[2] & 0xFF, bg_buf[1] & 0xFF, bg_buf[0] & 0xFF);
        }
        memGraphics.setColor(pixel);
        memGraphics.fillRect(x, y, w, h);
        byte[] buf = new byte[nSubrects * (bytesPixel + 8)];
        rfb.readFully(buf);
        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(buf));
        if (rfb.rec != null) {
            rfb.rec.writeIntBE(nSubrects);
            rfb.rec.write(bg_buf);
            rfb.rec.write(buf);
        }
        int sx, sy, sw, sh;
        for (int j = 0; j < nSubrects; j++) {
            if (bytesPixel == 1) {
                pixel = colors[ds.readUnsignedByte()];
            } else {
                ds.skip(4);
                pixel = new Color(buf[j * 12 + 2] & 0xFF, buf[j * 12 + 1] & 0xFF, buf[j * 12] & 0xFF);
            }
            sx = x + ds.readUnsignedShort();
            sy = y + ds.readUnsignedShort();
            sw = ds.readUnsignedShort();
            sh = ds.readUnsignedShort();
            memGraphics.setColor(pixel);
            memGraphics.fillRect(sx, sy, sw, sh);
        }
        scheduleRepaint(x, y, w, h);
    }

    void handleCoRRERect(int x, int y, int w, int h) throws IOException {
        int nSubrects = rfb.readU32();
        byte[] bg_buf = new byte[bytesPixel];
        rfb.readFully(bg_buf);
        Color pixel;
        if (bytesPixel == 1) {
            pixel = colors[bg_buf[0] & 0xFF];
        } else {
            pixel = new Color(bg_buf[2] & 0xFF, bg_buf[1] & 0xFF, bg_buf[0] & 0xFF);
        }
        memGraphics.setColor(pixel);
        memGraphics.fillRect(x, y, w, h);
        byte[] buf = new byte[nSubrects * (bytesPixel + 4)];
        rfb.readFully(buf);
        if (rfb.rec != null) {
            rfb.rec.writeIntBE(nSubrects);
            rfb.rec.write(bg_buf);
            rfb.rec.write(buf);
        }
        int sx, sy, sw, sh;
        int i = 0;
        for (int j = 0; j < nSubrects; j++) {
            if (bytesPixel == 1) {
                pixel = colors[buf[i++] & 0xFF];
            } else {
                pixel = new Color(buf[i + 2] & 0xFF, buf[i + 1] & 0xFF, buf[i] & 0xFF);
                i += 4;
            }
            sx = x + (buf[i++] & 0xFF);
            sy = y + (buf[i++] & 0xFF);
            sw = buf[i++] & 0xFF;
            sh = buf[i++] & 0xFF;
            memGraphics.setColor(pixel);
            memGraphics.fillRect(sx, sy, sw, sh);
        }
        scheduleRepaint(x, y, w, h);
    }

    private Color hextile_bg, hextile_fg;

    void handleHextileRect(int x, int y, int w, int h) throws IOException {
        hextile_bg = new Color(0);
        hextile_fg = new Color(0);
        for (int ty = y; ty < y + h; ty += 16) {
            int th = 16;
            if (y + h - ty < 16) th = y + h - ty;
            for (int tx = x; tx < x + w; tx += 16) {
                int tw = 16;
                if (x + w - tx < 16) tw = x + w - tx;
                handleHextileSubrect(tx, ty, tw, th);
            }
            scheduleRepaint(x, y, w, h);
        }
    }

    void handleHextileSubrect(int tx, int ty, int tw, int th) throws IOException {
        int subencoding = rfb.readU8();
        if (rfb.rec != null) {
            rfb.rec.writeByte(subencoding);
        }
        if ((subencoding & RfbProto.HextileRaw) != 0) {
            handleRawRect(tx, ty, tw, th, false);
            return;
        }
        byte[] cbuf = new byte[bytesPixel];
        if ((subencoding & RfbProto.HextileBackgroundSpecified) != 0) {
            rfb.readFully(cbuf);
            if (bytesPixel == 1) {
                hextile_bg = colors[cbuf[0] & 0xFF];
            } else {
                hextile_bg = new Color(cbuf[2] & 0xFF, cbuf[1] & 0xFF, cbuf[0] & 0xFF);
            }
            if (rfb.rec != null) {
                rfb.rec.write(cbuf);
            }
        }
        memGraphics.setColor(hextile_bg);
        memGraphics.fillRect(tx, ty, tw, th);
        if ((subencoding & RfbProto.HextileForegroundSpecified) != 0) {
            rfb.readFully(cbuf);
            if (bytesPixel == 1) {
                hextile_fg = colors[cbuf[0] & 0xFF];
            } else {
                hextile_fg = new Color(cbuf[2] & 0xFF, cbuf[1] & 0xFF, cbuf[0] & 0xFF);
            }
            if (rfb.rec != null) {
                rfb.rec.write(cbuf);
            }
        }
        if ((subencoding & RfbProto.HextileAnySubrects) == 0) return;
        int nSubrects = rfb.readU8();
        int bufsize = nSubrects * 2;
        if ((subencoding & RfbProto.HextileSubrectsColoured) != 0) {
            bufsize += nSubrects * bytesPixel;
        }
        byte[] buf = new byte[bufsize];
        rfb.readFully(buf);
        if (rfb.rec != null) {
            rfb.rec.writeByte(nSubrects);
            rfb.rec.write(buf);
        }
        int b1, b2, sx, sy, sw, sh;
        int i = 0;
        if ((subencoding & RfbProto.HextileSubrectsColoured) == 0) {
            memGraphics.setColor(hextile_fg);
            for (int j = 0; j < nSubrects; j++) {
                b1 = buf[i++] & 0xFF;
                b2 = buf[i++] & 0xFF;
                sx = tx + (b1 >> 4);
                sy = ty + (b1 & 0xf);
                sw = (b2 >> 4) + 1;
                sh = (b2 & 0xf) + 1;
                memGraphics.fillRect(sx, sy, sw, sh);
            }
        } else if (bytesPixel == 1) {
            for (int j = 0; j < nSubrects; j++) {
                hextile_fg = colors[buf[i++] & 0xFF];
                b1 = buf[i++] & 0xFF;
                b2 = buf[i++] & 0xFF;
                sx = tx + (b1 >> 4);
                sy = ty + (b1 & 0xf);
                sw = (b2 >> 4) + 1;
                sh = (b2 & 0xf) + 1;
                memGraphics.setColor(hextile_fg);
                memGraphics.fillRect(sx, sy, sw, sh);
            }
        } else {
            for (int j = 0; j < nSubrects; j++) {
                hextile_fg = new Color(buf[i + 2] & 0xFF, buf[i + 1] & 0xFF, buf[i] & 0xFF);
                i += 4;
                b1 = buf[i++] & 0xFF;
                b2 = buf[i++] & 0xFF;
                sx = tx + (b1 >> 4);
                sy = ty + (b1 & 0xf);
                sw = (b2 >> 4) + 1;
                sh = (b2 & 0xf) + 1;
                memGraphics.setColor(hextile_fg);
                memGraphics.fillRect(sx, sy, sw, sh);
            }
        }
    }

    void handleZRLERect(int x, int y, int w, int h) throws Exception {
        if (zrleInStream == null) zrleInStream = new ZlibInStream();
        int nBytes = rfb.readU32();
        if (nBytes > 64 * 1024 * 1024) throw new Exception("ZRLE decoder: illegal compressed data size");
        if (zrleBuf == null || zrleBufLen < nBytes) {
            zrleBufLen = nBytes + 4096;
            zrleBuf = new byte[zrleBufLen];
        }
        rfb.readFully(zrleBuf, 0, nBytes);
        if (rfb.rec != null) {
            if (rfb.recordFromBeginning) {
                rfb.rec.writeIntBE(nBytes);
                rfb.rec.write(zrleBuf, 0, nBytes);
            } else if (!zrleRecWarningShown) {
                System.out.println("Warning: ZRLE session can be recorded" + " only from the beginning");
                System.out.println("Warning: Recorded file may be corrupted");
                zrleRecWarningShown = true;
            }
        }
        zrleInStream.setUnderlying(new MemInStream(zrleBuf, 0, nBytes), nBytes);
        for (int ty = y; ty < y + h; ty += 64) {
            int th = Math.min(y + h - ty, 64);
            for (int tx = x; tx < x + w; tx += 64) {
                int tw = Math.min(x + w - tx, 64);
                int mode = zrleInStream.readU8();
                boolean rle = (mode & 128) != 0;
                int palSize = mode & 127;
                int[] palette = new int[128];
                readZrlePalette(palette, palSize);
                if (palSize == 1) {
                    int pix = palette[0];
                    Color c = (bytesPixel == 1) ? colors[pix] : new Color(0xFF000000 | pix);
                    memGraphics.setColor(c);
                    memGraphics.fillRect(tx, ty, tw, th);
                    continue;
                }
                if (!rle) {
                    if (palSize == 0) {
                        readZrleRawPixels(tw, th);
                    } else {
                        readZrlePackedPixels(tw, th, palette, palSize);
                    }
                } else {
                    if (palSize == 0) {
                        readZrlePlainRLEPixels(tw, th);
                    } else {
                        readZrlePackedRLEPixels(tw, th, palette);
                    }
                }
                handleUpdatedZrleTile(tx, ty, tw, th);
            }
        }
        zrleInStream.reset();
        scheduleRepaint(x, y, w, h);
    }

    int readPixel(InStream is) throws Exception {
        int pix;
        if (bytesPixel == 1) {
            pix = is.readU8();
        } else {
            int p1 = is.readU8();
            int p2 = is.readU8();
            int p3 = is.readU8();
            pix = (p3 & 0xFF) << 16 | (p2 & 0xFF) << 8 | (p1 & 0xFF);
        }
        return pix;
    }

    void readPixels(InStream is, int[] dst, int count) throws Exception {
        if (bytesPixel == 1) {
            byte[] buf = new byte[count];
            is.readBytes(buf, 0, count);
            for (int i = 0; i < count; i++) {
                dst[i] = buf[i] & 0xFF;
            }
        } else {
            byte[] buf = new byte[count * 3];
            is.readBytes(buf, 0, count * 3);
            for (int i = 0; i < count; i++) {
                dst[i] = ((buf[i * 3 + 2] & 0xFF) << 16 | (buf[i * 3 + 1] & 0xFF) << 8 | (buf[i * 3] & 0xFF));
            }
        }
    }

    void readZrlePalette(int[] palette, int palSize) throws Exception {
        readPixels(zrleInStream, palette, palSize);
    }

    void readZrleRawPixels(int tw, int th) throws Exception {
        if (bytesPixel == 1) {
            zrleInStream.readBytes(zrleTilePixels8, 0, tw * th);
        } else {
            readPixels(zrleInStream, zrleTilePixels24, tw * th);
        }
    }

    void readZrlePackedPixels(int tw, int th, int[] palette, int palSize) throws Exception {
        int bppp = ((palSize > 16) ? 8 : ((palSize > 4) ? 4 : ((palSize > 2) ? 2 : 1)));
        int ptr = 0;
        for (int i = 0; i < th; i++) {
            int eol = ptr + tw;
            int b = 0;
            int nbits = 0;
            while (ptr < eol) {
                if (nbits == 0) {
                    b = zrleInStream.readU8();
                    nbits = 8;
                }
                nbits -= bppp;
                int index = (b >> nbits) & ((1 << bppp) - 1) & 127;
                if (bytesPixel == 1) {
                    zrleTilePixels8[ptr++] = (byte) palette[index];
                } else {
                    zrleTilePixels24[ptr++] = palette[index];
                }
            }
        }
    }

    void readZrlePlainRLEPixels(int tw, int th) throws Exception {
        int ptr = 0;
        int end = ptr + tw * th;
        while (ptr < end) {
            int pix = readPixel(zrleInStream);
            int len = 1;
            int b;
            do {
                b = zrleInStream.readU8();
                len += b;
            } while (b == 255);
            if (!(len <= end - ptr)) throw new Exception("ZRLE decoder: assertion failed" + " (len <= end-ptr)");
            if (bytesPixel == 1) {
                while (len-- > 0) zrleTilePixels8[ptr++] = (byte) pix;
            } else {
                while (len-- > 0) zrleTilePixels24[ptr++] = pix;
            }
        }
    }

    void readZrlePackedRLEPixels(int tw, int th, int[] palette) throws Exception {
        int ptr = 0;
        int end = ptr + tw * th;
        while (ptr < end) {
            int index = zrleInStream.readU8();
            int len = 1;
            if ((index & 128) != 0) {
                int b;
                do {
                    b = zrleInStream.readU8();
                    len += b;
                } while (b == 255);
                if (!(len <= end - ptr)) throw new Exception("ZRLE decoder: assertion failed" + " (len <= end - ptr)");
            }
            index &= 127;
            int pix = palette[index];
            if (bytesPixel == 1) {
                while (len-- > 0) zrleTilePixels8[ptr++] = (byte) pix;
            } else {
                while (len-- > 0) zrleTilePixels24[ptr++] = pix;
            }
        }
    }

    void handleUpdatedZrleTile(int x, int y, int w, int h) {
        Object src, dst;
        if (bytesPixel == 1) {
            src = zrleTilePixels8;
            dst = pixels8;
        } else {
            src = zrleTilePixels24;
            dst = pixels24;
        }
        int offsetSrc = 0;
        int offsetDst = (y * rfb.framebufferWidth + x);
        for (int j = 0; j < h; j++) {
            System.arraycopy(src, offsetSrc, dst, offsetDst, w);
            offsetSrc += w;
            offsetDst += rfb.framebufferWidth;
        }
        handleUpdatedPixels(x, y, w, h);
    }

    void handleZlibRect(int x, int y, int w, int h) throws Exception {
        int nBytes = rfb.readU32();
        if (zlibBuf == null || zlibBufLen < nBytes) {
            zlibBufLen = nBytes * 2;
            zlibBuf = new byte[zlibBufLen];
        }
        rfb.readFully(zlibBuf, 0, nBytes);
        if (rfb.rec != null && rfb.recordFromBeginning) {
            rfb.rec.writeIntBE(nBytes);
            rfb.rec.write(zlibBuf, 0, nBytes);
        }
        if (zlibInflater == null) {
            zlibInflater = new Inflater();
        }
        zlibInflater.setInput(zlibBuf, 0, nBytes);
        if (bytesPixel == 1) {
            for (int dy = y; dy < y + h; dy++) {
                zlibInflater.inflate(pixels8, dy * rfb.framebufferWidth + x, w);
                if (rfb.rec != null && !rfb.recordFromBeginning) rfb.rec.write(pixels8, dy * rfb.framebufferWidth + x, w);
            }
        } else {
            byte[] buf = new byte[w * 4];
            int i, offset;
            for (int dy = y; dy < y + h; dy++) {
                zlibInflater.inflate(buf);
                offset = dy * rfb.framebufferWidth + x;
                for (i = 0; i < w; i++) {
                    pixels24[offset + i] = (buf[i * 4 + 2] & 0xFF) << 16 | (buf[i * 4 + 1] & 0xFF) << 8 | (buf[i * 4] & 0xFF);
                }
                if (rfb.rec != null && !rfb.recordFromBeginning) rfb.rec.write(buf);
            }
        }
        handleUpdatedPixels(x, y, w, h);
        scheduleRepaint(x, y, w, h);
    }

    void handleTightRect(int x, int y, int w, int h) throws Exception {
        int comp_ctl = rfb.readU8();
        if (rfb.rec != null) {
            if (rfb.recordFromBeginning || comp_ctl == (RfbProto.TightFill << 4) || comp_ctl == (RfbProto.TightJpeg << 4)) {
                rfb.rec.writeByte(comp_ctl);
            } else {
                rfb.rec.writeByte(comp_ctl | 0x0F);
            }
        }
        for (int stream_id = 0; stream_id < 4; stream_id++) {
            if ((comp_ctl & 1) != 0 && tightInflaters[stream_id] != null) {
                tightInflaters[stream_id] = null;
            }
            comp_ctl >>= 1;
        }
        if (comp_ctl > RfbProto.TightMaxSubencoding) {
            throw new Exception("Incorrect tight subencoding: " + comp_ctl);
        }
        if (comp_ctl == RfbProto.TightFill) {
            if (bytesPixel == 1) {
                int idx = rfb.readU8();
                memGraphics.setColor(colors[idx]);
                if (rfb.rec != null) {
                    rfb.rec.writeByte(idx);
                }
            } else {
                byte[] buf = new byte[3];
                rfb.readFully(buf);
                if (rfb.rec != null) {
                    rfb.rec.write(buf);
                }
                Color bg = new Color(0xFF000000 | (buf[0] & 0xFF) << 16 | (buf[1] & 0xFF) << 8 | (buf[2] & 0xFF));
                memGraphics.setColor(bg);
            }
            memGraphics.fillRect(x, y, w, h);
            scheduleRepaint(x, y, w, h);
            return;
        }
        if (comp_ctl == RfbProto.TightJpeg) {
            statNumRectsTightJPEG++;
            byte[] jpegData = new byte[rfb.readCompactLen()];
            rfb.readFully(jpegData);
            if (rfb.rec != null) {
                if (!rfb.recordFromBeginning) {
                    rfb.recordCompactLen(jpegData.length);
                }
                rfb.rec.write(jpegData);
            }
            Image jpegImage = Toolkit.getDefaultToolkit().createImage(jpegData);
            jpegRect = new Rectangle(x, y, w, h);
            synchronized (jpegRect) {
                Toolkit.getDefaultToolkit().prepareImage(jpegImage, -1, -1, this);
                try {
                    jpegRect.wait(3000);
                } catch (InterruptedException e) {
                    throw new Exception("Interrupted while decoding JPEG image");
                }
            }
            jpegRect = null;
            return;
        }
        int numColors = 0, rowSize = w;
        byte[] palette8 = new byte[2];
        int[] palette24 = new int[256];
        boolean useGradient = false;
        if ((comp_ctl & RfbProto.TightExplicitFilter) != 0) {
            int filter_id = rfb.readU8();
            if (rfb.rec != null) {
                rfb.rec.writeByte(filter_id);
            }
            if (filter_id == RfbProto.TightFilterPalette) {
                numColors = rfb.readU8() + 1;
                if (rfb.rec != null) {
                    rfb.rec.writeByte(numColors - 1);
                }
                if (bytesPixel == 1) {
                    if (numColors != 2) {
                        throw new Exception("Incorrect tight palette size: " + numColors);
                    }
                    rfb.readFully(palette8);
                    if (rfb.rec != null) {
                        rfb.rec.write(palette8);
                    }
                } else {
                    byte[] buf = new byte[numColors * 3];
                    rfb.readFully(buf);
                    if (rfb.rec != null) {
                        rfb.rec.write(buf);
                    }
                    for (int i = 0; i < numColors; i++) {
                        palette24[i] = ((buf[i * 3] & 0xFF) << 16 | (buf[i * 3 + 1] & 0xFF) << 8 | (buf[i * 3 + 2] & 0xFF));
                    }
                }
                if (numColors == 2) rowSize = (w + 7) / 8;
            } else if (filter_id == RfbProto.TightFilterGradient) {
                useGradient = true;
            } else if (filter_id != RfbProto.TightFilterCopy) {
                throw new Exception("Incorrect tight filter id: " + filter_id);
            }
        }
        if (numColors == 0 && bytesPixel == 4) rowSize *= 3;
        int dataSize = h * rowSize;
        if (dataSize < RfbProto.TightMinToCompress) {
            if (numColors != 0) {
                byte[] indexedData = new byte[dataSize];
                rfb.readFully(indexedData);
                if (rfb.rec != null) {
                    rfb.rec.write(indexedData);
                }
                if (numColors == 2) {
                    if (bytesPixel == 1) {
                        decodeMonoData(x, y, w, h, indexedData, palette8);
                    } else {
                        decodeMonoData(x, y, w, h, indexedData, palette24);
                    }
                } else {
                    int i = 0;
                    for (int dy = y; dy < y + h; dy++) {
                        for (int dx = x; dx < x + w; dx++) {
                            pixels24[dy * rfb.framebufferWidth + dx] = palette24[indexedData[i++] & 0xFF];
                        }
                    }
                }
            } else if (useGradient) {
                byte[] buf = new byte[w * h * 3];
                rfb.readFully(buf);
                if (rfb.rec != null) {
                    rfb.rec.write(buf);
                }
                decodeGradientData(x, y, w, h, buf);
            } else {
                if (bytesPixel == 1) {
                    for (int dy = y; dy < y + h; dy++) {
                        rfb.readFully(pixels8, dy * rfb.framebufferWidth + x, w);
                        if (rfb.rec != null) {
                            rfb.rec.write(pixels8, dy * rfb.framebufferWidth + x, w);
                        }
                    }
                } else {
                    byte[] buf = new byte[w * 3];
                    int i, offset;
                    for (int dy = y; dy < y + h; dy++) {
                        rfb.readFully(buf);
                        if (rfb.rec != null) {
                            rfb.rec.write(buf);
                        }
                        offset = dy * rfb.framebufferWidth + x;
                        for (i = 0; i < w; i++) {
                            pixels24[offset + i] = (buf[i * 3] & 0xFF) << 16 | (buf[i * 3 + 1] & 0xFF) << 8 | (buf[i * 3 + 2] & 0xFF);
                        }
                    }
                }
            }
        } else {
            int zlibDataLen = rfb.readCompactLen();
            byte[] zlibData = new byte[zlibDataLen];
            rfb.readFully(zlibData);
            if (rfb.rec != null && rfb.recordFromBeginning) {
                rfb.rec.write(zlibData);
            }
            int stream_id = comp_ctl & 0x03;
            if (tightInflaters[stream_id] == null) {
                tightInflaters[stream_id] = new Inflater();
            }
            Inflater myInflater = tightInflaters[stream_id];
            myInflater.setInput(zlibData);
            byte[] buf = new byte[dataSize];
            myInflater.inflate(buf);
            if (rfb.rec != null && !rfb.recordFromBeginning) {
                rfb.recordCompressedData(buf);
            }
            if (numColors != 0) {
                if (numColors == 2) {
                    if (bytesPixel == 1) {
                        decodeMonoData(x, y, w, h, buf, palette8);
                    } else {
                        decodeMonoData(x, y, w, h, buf, palette24);
                    }
                } else {
                    int i = 0;
                    for (int dy = y; dy < y + h; dy++) {
                        for (int dx = x; dx < x + w; dx++) {
                            pixels24[dy * rfb.framebufferWidth + dx] = palette24[buf[i++] & 0xFF];
                        }
                    }
                }
            } else if (useGradient) {
                decodeGradientData(x, y, w, h, buf);
            } else {
                if (bytesPixel == 1) {
                    int destOffset = y * rfb.framebufferWidth + x;
                    for (int dy = 0; dy < h; dy++) {
                        System.arraycopy(buf, dy * w, pixels8, destOffset, w);
                        destOffset += rfb.framebufferWidth;
                    }
                } else {
                    int srcOffset = 0;
                    int destOffset, i;
                    for (int dy = 0; dy < h; dy++) {
                        myInflater.inflate(buf);
                        destOffset = (y + dy) * rfb.framebufferWidth + x;
                        for (i = 0; i < w; i++) {
                            pixels24[destOffset + i] = (buf[srcOffset] & 0xFF) << 16 | (buf[srcOffset + 1] & 0xFF) << 8 | (buf[srcOffset + 2] & 0xFF);
                            srcOffset += 3;
                        }
                    }
                }
            }
        }
        handleUpdatedPixels(x, y, w, h);
        scheduleRepaint(x, y, w, h);
    }

    void decodeMonoData(int x, int y, int w, int h, byte[] src, byte[] palette) {
        int dx, dy, n;
        int i = y * rfb.framebufferWidth + x;
        int rowBytes = (w + 7) / 8;
        byte b;
        for (dy = 0; dy < h; dy++) {
            for (dx = 0; dx < w / 8; dx++) {
                b = src[dy * rowBytes + dx];
                for (n = 7; n >= 0; n--) pixels8[i++] = palette[b >> n & 1];
            }
            for (n = 7; n >= 8 - w % 8; n--) {
                pixels8[i++] = palette[src[dy * rowBytes + dx] >> n & 1];
            }
            i += (rfb.framebufferWidth - w);
        }
    }

    void decodeMonoData(int x, int y, int w, int h, byte[] src, int[] palette) {
        int dx, dy, n;
        int i = y * rfb.framebufferWidth + x;
        int rowBytes = (w + 7) / 8;
        byte b;
        for (dy = 0; dy < h; dy++) {
            for (dx = 0; dx < w / 8; dx++) {
                b = src[dy * rowBytes + dx];
                for (n = 7; n >= 0; n--) pixels24[i++] = palette[b >> n & 1];
            }
            for (n = 7; n >= 8 - w % 8; n--) {
                pixels24[i++] = palette[src[dy * rowBytes + dx] >> n & 1];
            }
            i += (rfb.framebufferWidth - w);
        }
    }

    void decodeGradientData(int x, int y, int w, int h, byte[] buf) {
        int dx, dy, c;
        byte[] prevRow = new byte[w * 3];
        byte[] thisRow = new byte[w * 3];
        byte[] pix = new byte[3];
        int[] est = new int[3];
        int offset = y * rfb.framebufferWidth + x;
        for (dy = 0; dy < h; dy++) {
            for (c = 0; c < 3; c++) {
                pix[c] = (byte) (prevRow[c] + buf[dy * w * 3 + c]);
                thisRow[c] = pix[c];
            }
            pixels24[offset++] = (pix[0] & 0xFF) << 16 | (pix[1] & 0xFF) << 8 | (pix[2] & 0xFF);
            for (dx = 1; dx < w; dx++) {
                for (c = 0; c < 3; c++) {
                    est[c] = ((prevRow[dx * 3 + c] & 0xFF) + (pix[c] & 0xFF) - (prevRow[(dx - 1) * 3 + c] & 0xFF));
                    if (est[c] > 0xFF) {
                        est[c] = 0xFF;
                    } else if (est[c] < 0x00) {
                        est[c] = 0x00;
                    }
                    pix[c] = (byte) (est[c] + buf[(dy * w + dx) * 3 + c]);
                    thisRow[dx * 3 + c] = pix[c];
                }
                pixels24[offset++] = (pix[0] & 0xFF) << 16 | (pix[1] & 0xFF) << 8 | (pix[2] & 0xFF);
            }
            System.arraycopy(thisRow, 0, prevRow, 0, w * 3);
            offset += (rfb.framebufferWidth - w);
        }
    }

    void handleUpdatedPixels(int x, int y, int w, int h) {
        pixelsSource.newPixels(x, y, w, h);
        memGraphics.setClip(x, y, w, h);
        memGraphics.drawImage(rawPixelsImage, 0, 0, null);
        memGraphics.setClip(0, 0, rfb.framebufferWidth, rfb.framebufferHeight);
    }

    void scheduleRepaint(int x, int y, int w, int h) {
        if (rfb.framebufferWidth == scaledWidth) {
            repaint(viewer.deferScreenUpdates, x, y, w, h);
        } else {
            int sx = x * scalingFactor / 100;
            int sy = y * scalingFactor / 100;
            int sw = ((x + w) * scalingFactor + 49) / 100 - sx + 1;
            int sh = ((y + h) * scalingFactor + 49) / 100 - sy + 1;
            repaint(viewer.deferScreenUpdates, sx, sy, sw, sh);
        }
    }

    int count = 1;

    @Override
    public void repaint(long tm, final int x, final int y, int w, int h) {
        super.repaint(tm, x, y, w, h);
        final DisplayWriterThread writer = displayClient.getWriter();
        System.err.println(writer);
        final BufferedImage bIm = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics gDisplay = bIm.getGraphics();
        gDisplay.drawImage(memImage, 0, 0, w, h, x, y, x + w, y + h, null);
        writer.sendImage(bIm, x, y);
        gDisplay.dispose();
        count++;
    }

    public void keyPressed(KeyEvent evt) {
        processLocalKeyEvent(evt);
    }

    public void keyReleased(KeyEvent evt) {
        processLocalKeyEvent(evt);
    }

    public void keyTyped(KeyEvent evt) {
        evt.consume();
    }

    public void mousePressed(MouseEvent evt) {
        processLocalMouseEvent(evt, false);
    }

    public void mouseReleased(MouseEvent evt) {
        processLocalMouseEvent(evt, false);
    }

    public void mouseMoved(MouseEvent evt) {
        processLocalMouseEvent(evt, true);
    }

    public void mouseDragged(MouseEvent evt) {
        processLocalMouseEvent(evt, true);
    }

    public void processLocalKeyEvent(KeyEvent evt) {
        if (viewer.rfb != null && rfb.inNormalProtocol) {
            if (!inputEnabled) {
                if ((evt.getKeyChar() == 'r' || evt.getKeyChar() == 'R') && evt.getID() == KeyEvent.KEY_PRESSED) {
                    try {
                        rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth, rfb.framebufferHeight, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                synchronized (rfb) {
                    try {
                        rfb.writeKeyEvent(evt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rfb.notify();
                }
            }
        }
        evt.consume();
    }

    public void processLocalMouseEvent(MouseEvent evt, boolean moved) {
        if (viewer.rfb != null && rfb.inNormalProtocol) {
            if (moved) {
                softCursorMove(evt.getX(), evt.getY());
            }
            if (rfb.framebufferWidth != scaledWidth) {
                int sx = (evt.getX() * 100 + scalingFactor / 2) / scalingFactor;
                int sy = (evt.getY() * 100 + scalingFactor / 2) / scalingFactor;
                evt.translatePoint(sx - evt.getX(), sy - evt.getY());
            }
            synchronized (rfb) {
                try {
                    rfb.writePointerEvent(evt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rfb.notify();
            }
        }
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    void resetStats() {
        statStartTime = System.currentTimeMillis();
        statNumUpdates = 0;
        statNumTotalRects = 0;
        statNumPixelRects = 0;
        statNumRectsTight = 0;
        statNumRectsTightJPEG = 0;
        statNumRectsZRLE = 0;
        statNumRectsHextile = 0;
        statNumRectsRaw = 0;
        statNumRectsCopy = 0;
        statNumBytesEncoded = 0;
        statNumBytesDecoded = 0;
    }

    boolean showSoftCursor = false;

    MemoryImageSource softCursorSource;

    Image softCursor;

    int cursorX = 0, cursorY = 0;

    int cursorWidth, cursorHeight;

    int origCursorWidth, origCursorHeight;

    int hotX, hotY;

    int origHotX, origHotY;

    synchronized void handleCursorShapeUpdate(int encodingType, int xhot, int yhot, int width, int height) throws IOException {
        softCursorFree();
        if (width * height == 0) return;
        if (viewer.options.ignoreCursorUpdates) {
            int bytesPerRow = (width + 7) / 8;
            int bytesMaskData = bytesPerRow * height;
            if (encodingType == RfbProto.EncodingXCursor) {
                rfb.skipBytes(6 + bytesMaskData * 2);
            } else {
                rfb.skipBytes(width * height * bytesPixel + bytesMaskData);
            }
            return;
        }
        softCursorSource = decodeCursorShape(encodingType, width, height);
        origCursorWidth = width;
        origCursorHeight = height;
        origHotX = xhot;
        origHotY = yhot;
        createSoftCursor();
        showSoftCursor = true;
        repaint(viewer.deferCursorUpdates, cursorX - hotX, cursorY - hotY, cursorWidth, cursorHeight);
    }

    synchronized MemoryImageSource decodeCursorShape(int encodingType, int width, int height) throws IOException {
        int bytesPerRow = (width + 7) / 8;
        int bytesMaskData = bytesPerRow * height;
        int[] softCursorPixels = new int[width * height];
        if (encodingType == RfbProto.EncodingXCursor) {
            byte[] rgb = new byte[6];
            rfb.readFully(rgb);
            int[] colors = { (0xFF000000 | (rgb[3] & 0xFF) << 16 | (rgb[4] & 0xFF) << 8 | (rgb[5] & 0xFF)), (0xFF000000 | (rgb[0] & 0xFF) << 16 | (rgb[1] & 0xFF) << 8 | (rgb[2] & 0xFF)) };
            byte[] pixBuf = new byte[bytesMaskData];
            rfb.readFully(pixBuf);
            byte[] maskBuf = new byte[bytesMaskData];
            rfb.readFully(maskBuf);
            byte pixByte, maskByte;
            int x, y, n, result;
            int i = 0;
            for (y = 0; y < height; y++) {
                for (x = 0; x < width / 8; x++) {
                    pixByte = pixBuf[y * bytesPerRow + x];
                    maskByte = maskBuf[y * bytesPerRow + x];
                    for (n = 7; n >= 0; n--) {
                        if ((maskByte >> n & 1) != 0) {
                            result = colors[pixByte >> n & 1];
                        } else {
                            result = 0;
                        }
                        softCursorPixels[i++] = result;
                    }
                }
                for (n = 7; n >= 8 - width % 8; n--) {
                    if ((maskBuf[y * bytesPerRow + x] >> n & 1) != 0) {
                        result = colors[pixBuf[y * bytesPerRow + x] >> n & 1];
                    } else {
                        result = 0;
                    }
                    softCursorPixels[i++] = result;
                }
            }
        } else {
            byte[] pixBuf = new byte[width * height * bytesPixel];
            rfb.readFully(pixBuf);
            byte[] maskBuf = new byte[bytesMaskData];
            rfb.readFully(maskBuf);
            byte maskByte;
            int x, y, n, result;
            int i = 0;
            for (y = 0; y < height; y++) {
                for (x = 0; x < width / 8; x++) {
                    maskByte = maskBuf[y * bytesPerRow + x];
                    for (n = 7; n >= 0; n--) {
                        if ((maskByte >> n & 1) != 0) {
                            if (bytesPixel == 1) {
                                result = cm8.getRGB(pixBuf[i]);
                            } else {
                                result = 0xFF000000 | (pixBuf[i * 4 + 2] & 0xFF) << 16 | (pixBuf[i * 4 + 1] & 0xFF) << 8 | (pixBuf[i * 4] & 0xFF);
                            }
                        } else {
                            result = 0;
                        }
                        softCursorPixels[i++] = result;
                    }
                }
                for (n = 7; n >= 8 - width % 8; n--) {
                    if ((maskBuf[y * bytesPerRow + x] >> n & 1) != 0) {
                        if (bytesPixel == 1) {
                            result = cm8.getRGB(pixBuf[i]);
                        } else {
                            result = 0xFF000000 | (pixBuf[i * 4 + 2] & 0xFF) << 16 | (pixBuf[i * 4 + 1] & 0xFF) << 8 | (pixBuf[i * 4] & 0xFF);
                        }
                    } else {
                        result = 0;
                    }
                    softCursorPixels[i++] = result;
                }
            }
        }
        return new MemoryImageSource(width, height, softCursorPixels, 0, width);
    }

    synchronized void createSoftCursor() {
        if (softCursorSource == null) return;
        int scaleCursor = viewer.options.scaleCursor;
        if (scaleCursor == 0 || !inputEnabled) scaleCursor = 100;
        int x = cursorX - hotX;
        int y = cursorY - hotY;
        int w = cursorWidth;
        int h = cursorHeight;
        cursorWidth = (origCursorWidth * scaleCursor + 50) / 100;
        cursorHeight = (origCursorHeight * scaleCursor + 50) / 100;
        hotX = (origHotX * scaleCursor + 50) / 100;
        hotY = (origHotY * scaleCursor + 50) / 100;
        softCursor = Toolkit.getDefaultToolkit().createImage(softCursorSource);
        if (scaleCursor != 100) {
            softCursor = softCursor.getScaledInstance(cursorWidth, cursorHeight, Image.SCALE_SMOOTH);
        }
        if (showSoftCursor) {
            x = Math.min(x, cursorX - hotX);
            y = Math.min(y, cursorY - hotY);
            w = Math.max(w, cursorWidth);
            h = Math.max(h, cursorHeight);
            repaint(viewer.deferCursorUpdates, x, y, w, h);
        }
    }

    synchronized void softCursorMove(int x, int y) {
        int oldX = cursorX;
        int oldY = cursorY;
        cursorX = x;
        cursorY = y;
        if (showSoftCursor) {
            repaint(viewer.deferCursorUpdates, oldX - hotX, oldY - hotY, cursorWidth, cursorHeight);
            repaint(viewer.deferCursorUpdates, cursorX - hotX, cursorY - hotY, cursorWidth, cursorHeight);
        }
    }

    synchronized void softCursorFree() {
        if (showSoftCursor) {
            showSoftCursor = false;
            softCursor = null;
            softCursorSource = null;
            repaint(viewer.deferCursorUpdates, cursorX - hotX, cursorY - hotY, cursorWidth, cursorHeight);
        }
    }
}
