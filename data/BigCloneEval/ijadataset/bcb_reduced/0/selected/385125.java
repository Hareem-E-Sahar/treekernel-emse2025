package gnu.saw.server.graphicsmode;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.util.Arrays;
import gnu.saw.SAW;
import gnu.saw.graphics.codec.SAWFrameDifferenceCodecV4;
import gnu.saw.graphics.image.SAWImageIO;
import gnu.saw.graphics.screencapture.SAWAWTScreenCaptureProvider;
import gnu.saw.server.connection.SAWServerConnection;
import gnu.saw.stream.SAWByteArrayOutputStream;

public class SAWGraphicsModeServerWriter implements Runnable {

    private volatile boolean stopped;

    private volatile boolean needRefresh;

    private volatile boolean open;

    private volatile boolean drawPointer;

    private volatile boolean completeRefresh;

    private volatile boolean refreshInterrupted;

    private volatile boolean screenCaptureModeComplete;

    private volatile boolean clearRequested;

    private volatile boolean dynamicCoding;

    private volatile int screenCaptureInterval;

    private int lastWidth;

    private int lastHeight;

    private int lastDepth;

    private int lastDataType;

    private int interruptedLastWidth;

    private int interruptedLastHeight;

    private byte[] lastImageBufferByte;

    private byte[] previousImageBufferByte;

    private short[] lastImageBufferUShort;

    private short[] previousImageBufferUShort;

    private int[] lastImageBufferInt;

    private int[] previousImageBufferInt;

    private Rectangle captureArea;

    private BufferedImage imageDataBuffer;

    private volatile GraphicsDevice nextDevice;

    private volatile GraphicsDevice currentDevice;

    private SAWByteArrayOutputStream imageOutputBuffer;

    private SAWAWTScreenCaptureProvider viewProvider;

    private SAWServerConnection connection;

    private SAWGraphicsModeServerSession session;

    private SAWFrameDifferenceCodecV4 codec;

    private SAWImageIO imageIO;

    private Object screenCaptureIntervalSynchronizer;

    public SAWGraphicsModeServerWriter(SAWGraphicsModeServerSession session) {
        this.session = session;
        this.connection = session.getSession().getConnection();
        this.viewProvider = session.getSession().getViewProvider();
        this.drawPointer = true;
        this.screenCaptureInterval = 500;
        this.viewProvider.setColorQuality(SAWAWTScreenCaptureProvider.SAW_COLOR_QUALITY_LOW);
        this.completeRefresh = false;
        this.refreshInterrupted = false;
        this.screenCaptureModeComplete = false;
        this.clearRequested = false;
        this.dynamicCoding = false;
        this.screenCaptureIntervalSynchronizer = new Object();
        this.imageIO = new SAWImageIO();
        this.currentDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.nextDevice = currentDevice;
    }

    public void dispose() {
        stopped = true;
        open = false;
        drawPointer = true;
        needRefresh = false;
        screenCaptureInterval = 500;
        viewProvider.setColorQuality(SAWAWTScreenCaptureProvider.SAW_COLOR_QUALITY_LOW);
        completeRefresh = false;
        refreshInterrupted = false;
        screenCaptureModeComplete = false;
        clearRequested = false;
        dynamicCoding = false;
        lastWidth = 0;
        lastHeight = 0;
        lastDepth = 0;
        interruptedLastWidth = 0;
        interruptedLastHeight = 0;
        imageDataBuffer = null;
        imageOutputBuffer = null;
        if (codec != null) {
            codec.dispose();
        }
        if (viewProvider != null) {
            viewProvider.dispose();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
        if (stopped) {
            synchronized (screenCaptureIntervalSynchronizer) {
                screenCaptureIntervalSynchronizer.notify();
            }
            synchronized (this) {
                notify();
            }
            try {
                connection.getGraphicsImageOutputStream().close();
            } catch (Exception e) {
            }
            try {
                connection.getGraphicsClipboardOutputStream().close();
            } catch (Exception e) {
            }
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setDrawPointer(boolean drawPointer) {
        this.drawPointer = drawPointer;
    }

    public void setColorQuality(int colorQuality) {
        this.viewProvider.setColorQuality(colorQuality);
    }

    public void setCompleteRefresh(boolean completeRefresh) {
        this.refreshInterrupted = false;
        this.completeRefresh = completeRefresh;
    }

    public void setRefreshInterrupted(boolean refreshInterrupted) {
        this.refreshInterrupted = refreshInterrupted;
    }

    public void setScreenCaptureInterval(int screenCaptureInterval) {
        this.screenCaptureInterval = screenCaptureInterval;
        synchronized (screenCaptureIntervalSynchronizer) {
            screenCaptureIntervalSynchronizer.notify();
        }
    }

    public void setNextDevice(GraphicsDevice nextDevice) {
        if (!nextDevice.getIDstring().equals(currentDevice.getIDstring())) {
            this.nextDevice = nextDevice;
        }
    }

    public void setScreenCaptureModeComplete(boolean complete) {
        this.screenCaptureModeComplete = complete;
    }

    public void setDynamicCoding(boolean dynamicCoding) {
        this.dynamicCoding = dynamicCoding;
    }

    public void setCaptureArea(Rectangle captureArea) {
        this.captureArea = captureArea;
    }

    public void requestClear() {
        clearRequested = true;
    }

    public void requestRefresh() {
        needRefresh = true;
        synchronized (this) {
            notify();
        }
    }

    public void sendRemoteInterfaceAreaChange(int width, int height) throws IOException {
        connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE);
        connection.getGraphicsControlDataOutputStream().writeInt(width);
        connection.getGraphicsControlDataOutputStream().writeInt(height);
        connection.getGraphicsControlDataOutputStream().flush();
    }

    public void sendDifference() throws IOException {
        if (!completeRefresh) {
            connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_PARTIAL);
        } else {
            connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_COMPLETE);
            connection.getGraphicsControlDataOutputStream().writeInt(codec.getBufferedFrameDataSize());
        }
        connection.getGraphicsControlDataOutputStream().flush();
        needRefresh = false;
        codec.encodeBufferedFrame(connection.getGraphicsImageDataOutputStream());
        connection.getGraphicsImageDataOutputStream().flush();
    }

    public void sendRefresh() throws IOException {
        if (!completeRefresh) {
            connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_PARTIAL);
        } else {
            connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_COMPLETE);
            connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
        }
        connection.getGraphicsControlDataOutputStream().flush();
        needRefresh = false;
        imageOutputBuffer.writeTo(connection.getGraphicsImageDataOutputStream());
        connection.getGraphicsImageDataOutputStream().flush();
    }

    public void sendRefreshNotNeeded() throws IOException {
        connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED);
        connection.getGraphicsControlDataOutputStream().flush();
        needRefresh = false;
    }

    public void sendSessionEnding() {
        try {
            connection.getGraphicsControlDataOutputStream().write(SAW.SAW_GRAPHICS_MODE_SESSION_ENDING);
            connection.getGraphicsControlDataOutputStream().flush();
        } catch (Exception e) {
            stopped = true;
            return;
        }
    }

    public void run() {
        imageOutputBuffer = new SAWByteArrayOutputStream();
        codec = new SAWFrameDifferenceCodecV4();
        codec.setMacroblockDataBuffer(imageOutputBuffer);
        while (!stopped) {
            try {
                synchronized (this) {
                    while (!stopped && !needRefresh) {
                        wait();
                    }
                }
                if (!stopped) {
                    if (currentDevice != nextDevice) {
                        currentDevice = nextDevice;
                        viewProvider.setGraphicsDevice(currentDevice);
                    }
                    if (!refreshInterrupted) {
                        if (clearRequested) {
                            if (imageDataBuffer != null) {
                                SAWImageIO.clearImage(imageDataBuffer);
                            }
                            lastWidth = -1;
                            lastHeight = -1;
                            lastDepth = -1;
                            lastDataType = -1;
                            clearRequested = false;
                        } else if (!screenCaptureModeComplete && captureArea != null && captureArea.width > 0 && captureArea.height > 0) {
                            imageDataBuffer = viewProvider.createScreenCapture(drawPointer, captureArea);
                        } else if (screenCaptureModeComplete) {
                            imageDataBuffer = viewProvider.createScreenCapture(drawPointer);
                        }
                        if (imageDataBuffer != null) {
                            if (imageDataBuffer.getWidth() == lastWidth && imageDataBuffer.getHeight() == lastHeight && imageDataBuffer.getColorModel().getPixelSize() == lastDepth && imageDataBuffer.getRaster().getDataBuffer().getDataType() == lastDataType) {
                                if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
                                    if (!Arrays.equals(lastImageBufferByte, previousImageBufferByte)) {
                                        codec.makeFrameDifference(previousImageBufferByte, 0, lastImageBufferByte, 0, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), imageDataBuffer.getColorModel().getPixelSize(), dynamicCoding);
                                        sendDifference();
                                    } else {
                                        sendRefreshNotNeeded();
                                    }
                                } else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT) {
                                    if (!Arrays.equals(lastImageBufferUShort, previousImageBufferUShort)) {
                                        codec.makeFrameDifference(previousImageBufferUShort, 0, lastImageBufferUShort, 0, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), imageDataBuffer.getColorModel().getPixelSize(), dynamicCoding);
                                        sendDifference();
                                    } else {
                                        sendRefreshNotNeeded();
                                    }
                                } else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT) {
                                    if (!Arrays.equals(lastImageBufferInt, previousImageBufferInt)) {
                                        codec.makeFrameDifference(previousImageBufferInt, 0, lastImageBufferInt, 0, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), imageDataBuffer.getColorModel().getPixelSize(), dynamicCoding);
                                        sendDifference();
                                    } else {
                                        sendRefreshNotNeeded();
                                    }
                                }
                            } else {
                                lastWidth = imageDataBuffer.getWidth();
                                lastHeight = imageDataBuffer.getHeight();
                                lastDepth = imageDataBuffer.getColorModel().getPixelSize();
                                lastDataType = imageDataBuffer.getRaster().getDataBuffer().getDataType();
                                imageOutputBuffer.reset();
                                imageIO.write(imageDataBuffer, imageOutputBuffer);
                                sendRefresh();
                                imageOutputBuffer.reset();
                                if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
                                    lastImageBufferByte = ((DataBufferByte) imageDataBuffer.getRaster().getDataBuffer()).getData();
                                    previousImageBufferByte = new byte[lastImageBufferByte.length];
                                    lastImageBufferUShort = null;
                                    previousImageBufferUShort = null;
                                    lastImageBufferInt = null;
                                    previousImageBufferInt = null;
                                    System.arraycopy(lastImageBufferByte, 0, previousImageBufferByte, 0, lastImageBufferByte.length);
                                } else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT) {
                                    lastImageBufferByte = null;
                                    previousImageBufferByte = null;
                                    lastImageBufferUShort = ((DataBufferUShort) imageDataBuffer.getRaster().getDataBuffer()).getData();
                                    previousImageBufferUShort = new short[lastImageBufferUShort.length];
                                    lastImageBufferInt = null;
                                    previousImageBufferInt = null;
                                    System.arraycopy(lastImageBufferUShort, 0, previousImageBufferUShort, 0, lastImageBufferUShort.length);
                                } else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT) {
                                    lastImageBufferByte = null;
                                    previousImageBufferByte = null;
                                    lastImageBufferUShort = null;
                                    previousImageBufferUShort = null;
                                    lastImageBufferInt = ((DataBufferInt) imageDataBuffer.getRaster().getDataBuffer()).getData();
                                    previousImageBufferInt = new int[lastImageBufferInt.length];
                                    System.arraycopy(lastImageBufferInt, 0, previousImageBufferInt, 0, lastImageBufferInt.length);
                                }
                            }
                        }
                        synchronized (screenCaptureIntervalSynchronizer) {
                            if (screenCaptureInterval > 0) {
                                screenCaptureIntervalSynchronizer.wait(screenCaptureInterval);
                            }
                        }
                    } else {
                        Dimension screenSize = viewProvider.getCurrentScreenSize();
                        if (screenSize != null && screenSize.width != interruptedLastWidth || screenSize.height != interruptedLastHeight) {
                            interruptedLastWidth = screenSize.width;
                            interruptedLastHeight = screenSize.height;
                            sendRemoteInterfaceAreaChange(interruptedLastWidth, interruptedLastHeight);
                        }
                        synchronized (screenCaptureIntervalSynchronizer) {
                            screenCaptureIntervalSynchronizer.wait(125);
                        }
                    }
                }
            } catch (Exception e) {
                stopped = true;
                break;
            }
        }
        sendSessionEnding();
        synchronized (session) {
            session.notify();
        }
    }
}
