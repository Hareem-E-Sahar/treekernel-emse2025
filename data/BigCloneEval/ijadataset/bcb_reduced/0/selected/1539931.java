package kello.teacher.rfb.server;

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class JRfbServer {

    private BufferedImage image;

    private ServerSocket socket;

    private int display;

    private String title;

    public static final int FRAMEBUFFER_UPDATE = 0;

    public static final int SET_COLOUR_MAP_ENTRY = 1;

    public static final int SET_PIXEL_FORMAT = 0;

    public static final int SET_ENCODINGS = 2;

    public static final int FRAMEBUFFER_UPDATE_REQUEST = 3;

    public static final int KEY_EVENT = 4;

    public static final int POINTER_EVENT = 5;

    public static final int CLIENT_CUT_TEXT = 6;

    private static final int ENCODING_RAW = 0;

    private HashMap<ColorModel, BufferedImage> images = new HashMap<ColorModel, BufferedImage>();

    private HashMap<ColorModel, HashSet<ClientThread>> clients = new HashMap<ColorModel, HashSet<ClientThread>>();

    public JRfbServer(int display, int width, int height, String title) {
        this.display = display;
        this.title = title;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void start() throws IOException {
        socket = new ServerSocket(5900 + display);
        new AcceptThread().start();
    }

    private synchronized void setColorModelForClient(ClientThread ct, ColorModel oldModel, ColorModel newModel) {
        if (oldModel != null) {
            clients.get(oldModel).remove(ct);
            if (clients.get(oldModel).size() == 0) {
                clients.remove(oldModel);
                images.remove(oldModel);
            }
        }
        if (newModel != null) {
            HashSet<ClientThread> clientsForModel = clients.get(newModel);
            if (clientsForModel == null) {
                clientsForModel = new HashSet<JRfbServer.ClientThread>();
                clients.put(newModel, clientsForModel);
                if (newModel.isCompatibleRaster(image.getRaster())) {
                    images.put(newModel, image);
                } else {
                    WritableRaster raster = newModel.createCompatibleWritableRaster(image.getWidth(), image.getHeight());
                    images.put(newModel, new BufferedImage(newModel, raster, true, null));
                    updateRaster(newModel);
                }
            }
            clientsForModel.add(ct);
        }
    }

    private synchronized BufferedImage getBufferedImage(ColorModel cm2) {
        return images.get(cm2);
    }

    private synchronized void updateRaster(ColorModel cm) {
        if (images.get(cm) != image) {
            Graphics g = images.get(cm).createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            for (ClientThread t : clients.get(cm)) {
                t.updated();
            }
        }
    }

    public synchronized void imageUpdated() {
        for (ColorModel model : images.keySet()) {
            updateRaster(model);
        }
    }

    public Graphics getGraphics() {
        return image.getGraphics();
    }

    private class AcceptThread extends Thread {

        @Override
        public void run() {
            try {
                while (!interrupted()) {
                    Socket s = socket.accept();
                    if (s == null) continue;
                    ClientThread c = new ClientThread(s);
                    c.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientThread extends Thread {

        private Socket client;

        private ColorModel cm;

        private boolean updated = true;

        public ClientThread(Socket client) {
            this.client = client;
            this.cm = new DirectColorModel(24, 0xFF0000, 0x00FF00, 0x0000FF);
            setColorModelForClient(this, null, this.cm);
        }

        public void updated() {
            updated = true;
        }

        @Override
        public void run() {
            try {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream());
                out.writeBytes("RFB 003.003\n");
                byte[] clientVersionArr = new byte[12];
                in.readFully(clientVersionArr);
                out.writeInt(1);
                boolean shared = in.read() != 0;
                out.writeShort((short) image.getWidth());
                out.writeShort((short) image.getHeight());
                out.write(32);
                out.write(24);
                out.write(1);
                out.write(1);
                out.write(new byte[] { 0x00, (byte) 0xFF });
                out.write(new byte[] { 0x00, (byte) 0xFF });
                out.write(new byte[] { 0x00, (byte) 0xFF });
                out.write(16);
                out.write(8);
                out.write(0);
                out.write(new byte[3]);
                out.writeInt(title.length());
                out.writeBytes(title);
                while (true) {
                    int msgType = in.read();
                    switch(msgType) {
                        case SET_PIXEL_FORMAT:
                            in.readFully(new byte[3]);
                            byte[] pixelFormat = new byte[16];
                            in.readFully(pixelFormat);
                            int bitsPerPixel = ((int) pixelFormat[0]) & 0xFF;
                            int depth = ((int) pixelFormat[1]) & 0xFF;
                            boolean bigEndian = pixelFormat[2] != 0;
                            boolean trueColor = pixelFormat[3] != 0;
                            int redMax = ((((int) pixelFormat[4]) & 0xFF) << 8) + (((int) pixelFormat[5]) & 0xFF);
                            int greenMax = ((((int) pixelFormat[6]) & 0xFF) << 8) + (((int) pixelFormat[7]) & 0xFF);
                            int blueMax = ((((int) pixelFormat[8]) & 0xFF) << 8) + (((int) pixelFormat[9]) & 0xFF);
                            int redShift = ((int) pixelFormat[10]) & 0xFF;
                            int greenShift = ((int) pixelFormat[11]) & 0xFF;
                            int blueShift = ((int) pixelFormat[12]) & 0xFF;
                            if (trueColor) {
                                cm = new DirectColorModel(bitsPerPixel, redMax << redShift, greenMax << greenShift, blueMax << blueShift);
                                if (!bigEndian && bitsPerPixel > 8) {
                                    redShift = bitsPerPixel - redShift - cm.getComponentSize(0);
                                    greenShift = bitsPerPixel - greenShift - cm.getComponentSize(1);
                                    blueShift = bitsPerPixel - blueShift - cm.getComponentSize(2);
                                }
                                cm = new DirectColorModel(bitsPerPixel, redMax << redShift, greenMax << greenShift, blueMax << blueShift);
                            } else {
                                int colors = 1 << bitsPerPixel;
                                byte colorsValues[][] = new byte[3][colors];
                                System.out.println(colors);
                                int idx = 0;
                                int levels = 1 << (bitsPerPixel / 3);
                                for (int i = 0; i <= levels; i++) {
                                    for (int j = 0; j <= levels; j++) {
                                        for (int k = 0; k <= levels; k++) {
                                            colorsValues[0][idx] = (byte) (i * 255 * 255 / colors);
                                            colorsValues[1][idx] = (byte) (j * 255 * 255 / colors);
                                            colorsValues[2][idx] = (byte) (k * 255 * 255 / colors);
                                            idx++;
                                        }
                                    }
                                }
                                cm = new IndexColorModel(bitsPerPixel, colors, colorsValues[0], colorsValues[1], colorsValues[2]);
                                sendColorMap(out, colorsValues);
                            }
                            setColorModelForClient(this, null, this.cm);
                            System.out.println("Updating color model" + cm);
                            break;
                        case SET_ENCODINGS:
                            in.read();
                            int encodingsCount = ((int) in.readShort()) & 0xFFFF;
                            for (int i = 0; i < encodingsCount; i++) {
                                in.readInt();
                            }
                            break;
                        case FRAMEBUFFER_UPDATE_REQUEST:
                            boolean incremental = in.read() != 0;
                            int xPos = ((int) in.readShort()) & 0xFFFF;
                            int yPos = ((int) in.readShort()) & 0xFFFF;
                            int width = ((int) in.readShort()) & 0xFFFF;
                            int height = ((int) in.readShort()) & 0xFFFF;
                            sendUpdate(out, xPos, yPos, width, height, incremental);
                            break;
                        case KEY_EVENT:
                            in.readFully(new byte[7]);
                            break;
                        case POINTER_EVENT:
                            in.readFully(new byte[5]);
                            break;
                        case CLIENT_CUT_TEXT:
                            in.readFully(new byte[3]);
                            int len = in.readInt();
                            in.readFully(new byte[len]);
                            break;
                        default:
                            throw new IOException("Received unknown message " + msgType);
                    }
                }
            } catch (IOException ex) {
                System.out.println("Client disconnected");
            } finally {
                try {
                    if (!client.isClosed()) client.close();
                    setColorModelForClient(this, cm, null);
                } catch (IOException e) {
                }
            }
        }

        private void sendColorMap(DataOutputStream out, byte[][] colorsValues) throws IOException {
            out.write(SET_COLOUR_MAP_ENTRY);
            out.write(0);
            out.writeShort(0);
            out.writeShort(colorsValues[0].length);
            for (int i = 0; i < colorsValues[0].length; i++) {
                out.writeShort(colorsValues[0][i]);
                out.writeShort(colorsValues[1][i]);
                out.writeShort(colorsValues[2][i]);
            }
        }

        private void sendUpdate(DataOutputStream out, int xPos, int yPos, int width, int height, boolean incremental) throws IOException {
            out.write(FRAMEBUFFER_UPDATE);
            out.write(0);
            if (incremental && !updated) {
                out.writeShort(0);
                return;
            }
            System.out.println("Send");
            out.writeShort(1);
            Rectangle requestedRect = new Rectangle(xPos, yPos, width, height);
            Rectangle sentRect = new Rectangle(image.getWidth(), image.getHeight()).intersection(requestedRect);
            out.writeShort(sentRect.x);
            out.writeShort(sentRect.y);
            out.writeShort(sentRect.width);
            out.writeShort(sentRect.height);
            out.writeInt(ENCODING_RAW);
            BufferedImage localImage = getBufferedImage(cm);
            SampleModel sampleModel = localImage.getSampleModel();
            Object data = sampleModel.getDataElements(sentRect.x, sentRect.y, sentRect.width, sentRect.height, null, localImage.getRaster().getDataBuffer());
            switch(cm.getTransferType()) {
                case DataBuffer.TYPE_BYTE:
                    byte[] dataB = (byte[]) data;
                    out.write(dataB);
                    break;
                case DataBuffer.TYPE_USHORT:
                    short[] dataS = (short[]) data;
                    for (int i = 0; i < dataS.length; i++) {
                        out.writeShort(dataS[i]);
                    }
                    break;
                case DataBuffer.TYPE_INT:
                    int[] dataI = (int[]) data;
                    for (int i = 0; i < dataI.length; i++) {
                        out.writeInt(dataI[i]);
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported color model");
            }
            updated = false;
        }
    }

    public static void main(String[] args) throws IOException, AWTException, InterruptedException {
        JRfbServer server = new JRfbServer(10, 400, 400, "Robot");
        server.start();
        Graphics g = server.getGraphics();
        Robot robot = new Robot();
        while (true) {
            BufferedImage img = robot.createScreenCapture(new Rectangle(400, 400));
            g.drawImage(img, 0, 0, null);
            PointerInfo a = MouseInfo.getPointerInfo();
            Point b = a.getLocation();
            int x = (int) b.getX();
            int y = (int) b.getY();
            g.fillRect(x, y, 10, 10);
            server.imageUpdated();
            Thread.sleep(1000);
        }
    }
}
