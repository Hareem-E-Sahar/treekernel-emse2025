package uk.org.dataforce.g15;

import uk.org.dataforce.g15.fonts.G15Font;
import java.awt.Point;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.geom.Rectangle2D;
import java.awt.FontFormatException;
import java.awt.font.LineMetrics;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * G15Daemon Wrapper for LCD Drawing.
 * This class relies on g15daemon
 */
public class G15DaemonWrapper extends G15Wrapper implements Runnable {

    /** Map Input from G15Daemon Socket. */
    private class G15Key {

        /** The Command for this key. */
        private String myCommand;

        /** The value of this key. */
        private int myValue;

        /** Create a new G15Key */
        public G15Key(final String command, final int value) {
            myCommand = command;
            myValue = value;
        }

        /** Get the Command for this key. */
        public String getCommand() {
            return myCommand;
        }

        /** Get the value of this key. */
        public int getValue() {
            return myValue;
        }
    }

    /** This List stores all the known keys */
    private ArrayList<G15Key> keycodes = new ArrayList<G15Key>();

    /** This int stores the last keypress data */
    private int lastKeyPress = 0;

    /** This is the socket used for reading from/writing to the Daemon. */
    private Socket socket;

    /** Used for writing to the daemon. */
    private PrintWriter out;

    /** Used for reading from the daemon. */
    private DataInputStream in;

    /** Used for reading from the daemon. */
    private volatile Thread myThread = null;

    /** Image used to draw on. */
    private BufferedImage image = new BufferedImage(LCD_WIDTH, LCD_HEIGHT, BufferedImage.TYPE_BYTE_BINARY);

    /** Graphics for the image */
    private Graphics2D graphicsArea = image.createGraphics();

    /** FontSlot hashmap */
    private HashMap<String, Font> fontSlots = new HashMap<String, Font>();

    /** Status of LEDs */
    private boolean[] ledState = new boolean[3];

    /**
	 * Are we emulating G15Composer?
	 * Enabling this mode makes us copy some of G15Composers quirks, such as
	 * - Centered text ignoring the X value for the position.
	 * - Needing to load fonts for each size you want to use (in non-composer mode
	 *   we ignore the size in loadFont)
	 */
    private boolean emulateComposer = true;

    /** Are we connecting to a WIP version of G15Daemon? */
    public static boolean isWIP = false;

    /** Should new instances start in Debugging Mode? */
    public static boolean debug = false;

    /** What scaled size should new graphics areas be in debugging mode?  */
    public static int debugScale = 4;

    /** Area to draw on in debugging mode. */
    private Graphics2D debugDrawingArea = null;

    /** Scaled Size of graphics area.  */
    private final int myScale;

    /**
	 * Create a new G15Daemon Wrapper
	 */
    public G15DaemonWrapper() {
        myScale = debugScale;
        if (debug) {
            javax.swing.JDialog d = new javax.swing.JDialog();
            javax.swing.JPanel p = new javax.swing.JPanel();
            d.setTitle("G15 LCD (Scaled " + myScale + " times)");
            Dimension panelSize = new Dimension(LCD_WIDTH * myScale, LCD_HEIGHT * myScale);
            p.setSize(panelSize);
            p.setMinimumSize(panelSize);
            d.add(p);
            d.pack();
            java.awt.Insets insets = d.getInsets();
            Dimension dialogSize = new Dimension(panelSize.width + insets.left + insets.right, panelSize.height + insets.top + insets.bottom);
            d.setSize(dialogSize);
            d.setMinimumSize(dialogSize);
            d.addWindowListener(new java.awt.event.WindowAdapter() {

                public void windowClosing(final java.awt.event.WindowEvent event) {
                    System.exit(0);
                }
            });
            d.setResizable(false);
            d.setVisible(true);
            debugDrawingArea = (Graphics2D) p.getGraphics();
        } else {
            try {
                socket = new Socket("127.0.0.1", 15550);
                socket.setOOBInline(true);
                out = new PrintWriter(socket.getOutputStream(), false);
                in = new DataInputStream(socket.getInputStream());
                byte[] inByte = new byte[16];
                if (in.read(inByte) != inByte.length) {
                    System.out.println("Not a G15Daemon? Ignoring KeyPresses");
                } else {
                    setKeyCodes();
                    if (!isWIP) {
                        try {
                            socket.sendUrgentData(0x10);
                        } catch (IOException ioe) {
                        }
                    }
                    setMXLight(0, false);
                }
                myThread = new Thread(this);
                myThread.start();
                out.print("GBUF");
                clearScreen(false);
            } catch (Exception e) {
                throw new G15NotFoundException("Unable to open socket to G15Daemon");
            }
        }
    }

    /** Close socket on destroy. */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /** Close socket. */
    public void close() {
        if (debugDrawingArea == null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not close socket");
            }
            Thread tmpThread = myThread;
            myThread = null;
            if (tmpThread != null) {
                tmpThread.interrupt();
            }
        }
    }

    /** Read input from G15Daemon */
    public void run() {
        int inData = 0;
        Thread thisThread = Thread.currentThread();
        while (myThread == thisThread) {
            try {
                byte[] inByte = new byte[4];
                if (in.read(inByte) == 4) {
                    inData = (((inByte[0] & 0xFF) << 24) | ((inByte[1] & 0xFF) << 16) | ((inByte[2] & 0xFF) << 8) | (inByte[3] & 0xFF));
                    for (G15Key key : keycodes) {
                        if ((key.getValue() & inData) != 0 && (key.getValue() & lastKeyPress) == 0) {
                            RemoteControl.getRemoteControl().addCommand(key.getCommand());
                        }
                    }
                    lastKeyPress = inData;
                }
            } catch (IOException ioe) {
                break;
            }
        }
        myThread = null;
    }

    private void setKeyCodes() {
        keycodes.add(new G15Key("BUTTON G1", 16777216));
        keycodes.add(new G15Key("BUTTON G2", 33554432));
        keycodes.add(new G15Key("BUTTON G3", 67108864));
        keycodes.add(new G15Key("BUTTON G4", 134217728));
        keycodes.add(new G15Key("BUTTON G5", 268435456));
        keycodes.add(new G15Key("BUTTON G6", 536870912));
        keycodes.add(new G15Key("BUTTON G7", 1073741824));
        keycodes.add(new G15Key("BUTTON G8", -2147483648));
        keycodes.add(new G15Key("BUTTON G9", 65536));
        keycodes.add(new G15Key("BUTTON G10", 131072));
        keycodes.add(new G15Key("BUTTON G11", 262144));
        keycodes.add(new G15Key("BUTTON G12", 524288));
        keycodes.add(new G15Key("BUTTON G13", 1048576));
        keycodes.add(new G15Key("BUTTON G14", 2097152));
        keycodes.add(new G15Key("BUTTON G15", 4194304));
        keycodes.add(new G15Key("BUTTON G16", 8388608));
        keycodes.add(new G15Key("BUTTON G17", 256));
        keycodes.add(new G15Key("BUTTON G18", 512));
        keycodes.add(new G15Key("BUTTON M1", 1024));
        keycodes.add(new G15Key("BUTTON M2", 2048));
        keycodes.add(new G15Key("BUTTON M3", 4096));
        keycodes.add(new G15Key("BUTTON CHG", 16384));
        keycodes.add(new G15Key("BUTTON LCD1", 32768));
        keycodes.add(new G15Key("BUTTON LCD2", 1));
        keycodes.add(new G15Key("BUTTON LCD3", 2));
        keycodes.add(new G15Key("BUTTON LCD4", 4));
    }

    /**
	 * Convert a boolean into a char
	 *
	 * @param bool Boolean to convert
	 * @return white for false, black for true.
	 */
    private Color convertBoolean(boolean bool) {
        if (bool == true) {
            return Color.black;
        } else {
            return Color.white;
        }
    }

    /**
	 * Get the G15-Char for the given RGB value.
	 *
	 * @param rgb RGB Value (from getRGB)
	 * @return 0 for white, else 1.
	 */
    public char getChar(final int rgb) {
        if (rgb == Color.white.getRGB()) {
            return (char) 0;
        } else {
            return (char) 1;
        }
    }

    /**
	 * Get the RGB value for the given char
	 *
	 * @param in Char Value (from getChar or so)
	 * @return rgb for char. (white for 0, else black)
	 */
    public int getRGB(final char in) {
        if (in == 0 || in == '0') {
            return Color.white.getRGB();
        } else {
            return Color.black.getRGB();
        }
    }

    /**
	 * Draw to the screen
	 *
	 * @throws java.io.IOException Throws this if the socket is not able to be written to
	 */
    public void draw() throws IOException {
        synchronized (image) {
            if (debugDrawingArea == null) {
                for (int y = 0; y < LCD_HEIGHT; ++y) {
                    for (int x = 0; x < LCD_WIDTH; ++x) {
                        out.print(getChar(image.getRGB(x, y)));
                    }
                }
                out.flush();
            } else {
                debugDrawingArea.drawImage(image, 0, 0, LCD_WIDTH * myScale, LCD_HEIGHT * myScale, null);
            }
        }
    }

    /**
	 * Draw to the screen without throwing an exception.
	 *
	 * @return true if draw was successful, else false
	 */
    public boolean silentDraw() {
        try {
            draw();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
	 * Draw a line of text in the default position.
	 *
	 * @param size FontSize of text to draw.
	 * @param text String[] of lines to draw
	 */
    public void drawText(FontSize size, String[] text) {
        int height = 0;
        for (String line : text) {
            drawText(size, new Point(0, height), G15Position.LEFT, line);
            height = height + size.getFont().getSize().height;
        }
    }

    /**
	 * Draw a line of text to a specific position.
	 *
	 * @param size FontSize of text to draw.
	 * @param point Location to draw text
	 * @param position G15Position to draw text (left, right, center)
	 * @param text String[] of lines to draw
	 */
    public void drawText(FontSize size, Point point, G15Position position, String[] text) {
        int height = point.y;
        for (String line : text) {
            drawText(size, new Point(point.x, height), position, line);
            height = height + size.getFont().getSize().height;
        }
    }

    /**
	 * Draw a line of text in the default position.
	 *
	 * @param size FontSize of text to draw.
	 * @param text line to draw
	 */
    public void drawText(FontSize size, String text) {
        drawText(size, new Point(0, 0), G15Position.LEFT, text);
    }

    /**
	 * Draw a line of text to a specific position.
	 *
	 * @param size FontSize of text to draw.
	 * @param point Location to draw text
	 * @param position G15Position to draw text (left, right, center)
	 * @param text line to draw
	 */
    public void drawText(FontSize size, Point point, G15Position position, String text) {
        synchronized (image) {
            G15Font font = size.getFont();
            if (font != null) {
                int startPos = point.x;
                int fontWidth = text.length() * font.getSize().width;
                if (position == G15Position.CENTER) {
                    if (emulateComposer) {
                        startPos = 0;
                    }
                    int midpoint = startPos + (LCD_WIDTH - startPos) / 2;
                    startPos = midpoint - (fontWidth / 2);
                } else if (position == G15Position.RIGHT) {
                    startPos = LCD_WIDTH - fontWidth;
                }
                for (int i = 0; i < text.length(); i++) {
                    for (int x = 0; x < font.getSize().width; x++) {
                        for (int y = 0; y < font.getSize().height; y++) {
                            int ypos = point.y + y;
                            int xpos = startPos + x + (font.getSize().width * i);
                            if (xpos >= 0 && xpos < LCD_WIDTH && ypos >= 0 && ypos < LCD_HEIGHT) {
                                image.setRGB(xpos, ypos, font.getPixelColor(text.charAt(i), x, y).getRGB());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
	 * Loads a font into a font slot.
	 *
	 * @param fontSlot Which slot to load font into
	 * @param size Size of font
	 * @param pathToFont Path to font file
	 */
    public void loadFont(int fontSlot, int size, String pathToFont) {
        final String slotName = "Font-" + fontSlot + ((emulateComposer) ? "-" + size : "");
        if (fontSlots.containsKey(slotName)) {
            fontSlots.remove(fontSlot);
        }
        final File fontFile = new File(pathToFont);
        if (fontFile.exists() && fontFile.isFile()) {
            try {
                final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                fontSlots.put(slotName, font);
            } catch (FontFormatException ffe) {
            } catch (IOException ioe) {
            }
        }
    }

    /**
	 * Draw text using specified font
	 *
	 * @param fontSlot Font slot for font
	 * @param size size of font
	 * @param point location to draw text at
	 * @param isBlack True for bacl text, false for white
	 * @param position Position for text
	 * @param text Text to output
	 */
    public void drawFont(int fontSlot, int size, Point point, boolean isBlack, G15Position position, String text) {
        drawFont(fontSlot, size, point, isBlack, position, new String[] { text });
    }

    /**
	 * Draw text using specified font
	 *
	 * @param fontSlot Font slot for font
	 * @param size size of font
	 * @param point location to draw text at
	 * @param isBlack True for bacl text, false for white
	 * @param position Position for text
	 * @param text Text to output
	 */
    public void drawFont(int fontSlot, int size, Point point, boolean isBlack, G15Position position, String[] text) {
        synchronized (image) {
            final String slotName = "Font-" + fontSlot + ((emulateComposer) ? "-" + size : "");
            if (!fontSlots.containsKey(slotName)) {
                return;
            }
            final Font font = fontSlots.get(slotName);
            graphicsArea.setFont(font);
            final Rectangle2D[] bounds = new Rectangle2D[text.length];
            final LineMetrics[] metrics = new LineMetrics[text.length];
            for (int i = 0; i < text.length; ++i) {
                bounds[i] = font.getStringBounds(text[i], graphicsArea.getFontRenderContext());
                metrics[i] = font.getLineMetrics(text[i], graphicsArea.getFontRenderContext());
            }
            int currentTop = point.y;
            for (int i = 0; i < text.length; ++i) {
                currentTop += bounds[i].getHeight();
                int y = Math.round(currentTop - metrics[i].getDescent());
                int x = point.x;
                int fontWidth = (int) bounds[i].getWidth();
                if (position == G15Position.CENTER) {
                    if (emulateComposer) {
                        x = 0;
                    }
                    int midpoint = x + (LCD_WIDTH - x) / 2;
                    x = midpoint - (fontWidth / 2);
                } else if (position == G15Position.RIGHT) {
                    x = LCD_WIDTH - fontWidth;
                }
                graphicsArea.drawString(text[i], x, y);
            }
        }
    }

    /**
	 * Draws a pixel image of the given Width and Height at the given point
	 *
	 * @param point Location to draw image
	 * @param width Width of image
	 * @param height height of image
	 * @param pixels String containing image as a string of 0's and 1's (0 = white, 1 = black)
	 */
    public void drawPixels(Point point, int width, int height, String pixels) {
        if (pixels.length() < (width * height)) {
            System.out.println("[drawPixels] Not recieved enough pixels. Not drawing. (Wanted: " + (width * height) + " Got: " + pixels.length() + ")");
            return;
        } else if (pixels.length() > (width * height)) {
            System.out.println("[drawPixels] Recieved more pixels than space to draw. This may look wrong!");
        }
        synchronized (image) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    if (x + point.x < LCD_WIDTH && y + point.y < LCD_HEIGHT) {
                        image.setRGB(x + point.x, y + point.y, getRGB(pixels.charAt((y * width) + x)));
                    }
                }
            }
        }
    }

    /**
	 * Draws a pixel image of the given PixelImage
	 *
	 * @param point Location to draw image
	 * @param PixelImage PixelImage to draw.
	 */
    public void drawPixels(Point point, PixelImage image) {
        drawPixels(point, image.width(), image.height(), image.toString());
    }

    /**
	 * Set the colour of the pixel at a given point
	 *
	 * @param point Point to set pixel at
	 * @param isBlack True to set to black, false to set to white
	 */
    public void setPixelColour(Point point, boolean isBlack) {
        synchronized (image) {
            image.setRGB(point.x, point.y, convertBoolean(isBlack).getRGB());
        }
    }

    /**
	 * Clear the screen and set it all to a specified colour
	 *
	 * @param isBlack True to set to black, false to set to white
	 */
    public void clearScreen(boolean isBlack) {
        synchronized (image) {
            graphicsArea.setColor(convertBoolean(isBlack));
            graphicsArea.fillRect(0, 0, LCD_WIDTH + 1, LCD_HEIGHT + 1);
        }
    }

    /**
	 * Fill a specified area in either white or black
	 *
	 * @param point1 Point to start at for area
	 * @param point2 Point to finish at for area
	 * @param isBlack True to set to black, false to set to white
	 */
    public void fillArea(Point point1, Point point2, boolean isBlack) {
        synchronized (image) {
            graphicsArea.setColor(convertBoolean(isBlack));
            final int x = (point2.x - point1.x > 0) ? point1.x : point2.x;
            final int y = (point2.y - point1.y > 0) ? point1.y : point2.y;
            graphicsArea.fillRect(x, y, Math.abs(point2.x - point1.x), Math.abs(point2.y - point1.y + 1));
        }
    }

    /**
	 * Reverse all the pixels in a specific area
	 *
	 * @param point1 Point to start at for area
	 * @param point2 Point to finish at for area
	 */
    public void reversePixels(Point point1, Point point2) {
        synchronized (image) {
            for (int x = point1.x; x <= point2.x; ++x) {
                for (int y = point1.y; y <= point2.y; ++y) {
                    setPixelColour(new Point(x, y), (image.getRGB(x, y) == Color.white.getRGB()));
                }
            }
        }
    }

    /**
	 * Draw a box
	 *
	 * @param point1 Point to start at for area
	 * @param point2 Point to finish at for area
	 * @param isBlack True to set to black, false to set to white
	 * @param thickness Thickness of line
	 */
    public void drawBox(Point point1, Point point2, boolean isBlack, int thickness) {
        synchronized (image) {
            Stroke oldStroke = graphicsArea.getStroke();
            graphicsArea.setStroke(new BasicStroke(thickness));
            graphicsArea.setColor(convertBoolean(isBlack));
            final int x = (point2.x - point1.x > 0) ? point1.x : point2.x;
            final int y = (point2.y - point1.y > 0) ? point1.y : point2.y;
            graphicsArea.drawRect(x, y, Math.abs(point2.x - point1.x), Math.abs(point2.y - point1.y + 1));
            graphicsArea.setStroke(oldStroke);
        }
    }

    /**
	 * Draw a line
	 *
	 * @param point1 Point to start at for line
	 * @param point2 Point to finish at for line
	 * @param isBlack True to set to black, false to set to white
	 */
    public void drawLine(Point point1, Point point2, boolean isBlack) {
        synchronized (image) {
            graphicsArea.setColor(convertBoolean(isBlack));
            graphicsArea.drawLine(point1.x, point1.y, point2.x, point2.y);
        }
    }

    /**
	 * Draw a circle
	 *
	 * @param center Center Point
	 * @param radius Radius of circle
	 * @param isBlack True to set to black, false to set to white
	 * @param filled Is the circle filled or not
	 */
    public void drawCircle(Point center, int radius, boolean isBlack, boolean filled) {
        synchronized (image) {
            graphicsArea.setColor(convertBoolean(isBlack));
            graphicsArea.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
            if (filled) {
                graphicsArea.fillOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
            }
        }
    }

    /**
	 * Draw a rounded box
	 *
	 * @param point1 Point to start at for area
	 * @param point2 Point to finish at for area
	 * @param isBlack True to set to black, false to set to white
	 * @param filled Is the box filled or not
	 */
    public void drawRoundedBox(Point point1, Point point2, boolean isBlack, boolean filled) {
        synchronized (image) {
            graphicsArea.setColor(convertBoolean(isBlack));
            graphicsArea.drawRoundRect(point1.x, point1.y, point2.x - point1.x, point2.y - point1.y, 8, 8);
        }
    }

    /**
	 * Draw a progress bar
	 *
	 * @param point1 Point to start at
	 * @param point2 point to finish at
	 * @param isBlack True to set to black, false to set to white
	 * @param position Position of the bar
	 * @param maxPosition Max Position of the bar
	 * @param barType Type of progress bar
	 */
    public void drawProgressBar(Point point1, Point point2, boolean isBlack, int position, int maxPosition, ProgressBarType barType) {
        synchronized (image) {
            final Color background = (isBlack) ? Color.white : Color.black;
            final Color foreground = (isBlack) ? Color.black : Color.white;
            final double percent = (100.0 / maxPosition) * position;
            final int length = (int) Math.round(0.01d + ((point2.x - (point1.x + 1)) / 100.0) * percent);
            if (emulateComposer && ((point2.x - point1.x) < 0 || (point2.y - point1.y) < 0)) {
                drawBox(new Point(point1.x, point1.y), new Point(point1.x + 1 + length, point2.y - 1), isBlack, 1);
            } else {
                fillArea(new Point(point1.x, point1.y), new Point(point1.x + 1 + length, point2.y), isBlack);
            }
            drawBox(new Point(point1.x, point1.y - 1), new Point(point2.x, point2.y), isBlack, 1);
        }
    }

    /**
	 * Set the MX Light on/off.
	 *
	 * @param light Which light to set (0 = all, 1,2,3 = M1 M2 M3)
	 * @param setOn true to turn on, false to turn off.
	 */
    public boolean setMXLight(int light, boolean setOn) {
        if (debugDrawingArea != null) {
            return false;
        }
        int data = 0x20;
        for (int i = 0; i < ledState.length; i++) {
            if (light == 0) {
                ledState[i] = setOn;
            } else if (light == (i + 1)) {
                ledState[i] = setOn;
            }
            if (ledState[i]) {
                data = data | 1 << i;
            }
        }
        try {
            socket.sendUrgentData(data);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
	 * Set LCD Contrast Level.
	 *
	 * @param level Contrast level, (0 1 or 2)
	 */
    public boolean setContrastLevel(int level) {
        if (debugDrawingArea != null) {
            return false;
        }
        try {
            socket.sendUrgentData(0x40 | level);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
	 * Set LCD Brightness Level.
	 *
	 * @param level Brightness level, (0 1 or 2)
	 */
    public boolean setBrightnessLevel(int level) {
        if (debugDrawingArea != null) {
            return false;
        }
        try {
            socket.sendUrgentData(0x80 | level);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
	 * Set screen to foreground or background
	 *
	 * @param position Position of screen
	 */
    public void screenPosition(G15ScreenPosition position) {
    }
}
