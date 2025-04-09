package rpg;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import rpg.entities.factories.SpriteFactory;
import rpg.gfx.FadeEffect;
import rpg.gfx.Overlay;
import rpg.gfx.Screen;

/**
 * rothens.tumblr.com
 *
 * @author Rothens
 */
public class RPG extends Canvas implements Runnable {

    private boolean running;

    private int FPS = 0;

    private BufferedImage gameLayer;

    private int[] glPixels;

    private BufferedImage overLayer;

    private int[] olPixels;

    private Screen screen;

    private Overlay overlay;

    private Game game;

    private static int WINDOW_WIDTH = 640;

    private static int WINDOW_HEIGHT = 480;

    private static JFrame frame;

    public static final String version = "0.0.11";

    public static RPG rpg;

    public static KeyboardListener listener = new KeyboardListener();

    private Thread thread;

    public RPG() {
        Dimension size = new Dimension(630, 470);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);
        setFocusable(false);
        screen = new Screen(WINDOW_WIDTH, WINDOW_HEIGHT);
        gameLayer = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        glPixels = ((DataBufferInt) gameLayer.getRaster().getDataBuffer()).getData();
        overlay = new Overlay(WINDOW_WIDTH, WINDOW_HEIGHT);
        overLayer = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        olPixels = ((DataBufferInt) overLayer.getRaster().getDataBuffer()).getData();
    }

    public static void main(String[] args) {
        rpg = new RPG();
        Game.newInstance();
        frame = new JFrame("Rothens RPG " + version);
        frame.setLayout(new GridLayout());
        frame.getContentPane().add(rpg);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(listener);
        frame.setVisible(true);
        rpg.start();
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        double upSec = 0;
        long lastTime = System.nanoTime();
        double secPerTick = 1 / 60.0;
        int cnt = 0;
        int frames = 0;
        overlay.addOverlay(new FadeEffect(640, 480, true, 10));
        while (running) {
            long now = System.nanoTime();
            long passed = now - lastTime;
            lastTime = now;
            if (passed < 0) {
                passed = 0;
            }
            if (passed > 100000000) {
                passed = 100000000;
            }
            upSec += passed / 1000000000.0;
            boolean ticked = false;
            while (upSec > secPerTick) {
                tick();
                upSec -= secPerTick;
                ticked = true;
                cnt++;
                if (cnt % 60 == 0) {
                    FPS = frames;
                    lastTime += 1000;
                    frames = 0;
                }
            }
            if (ticked) {
                render();
                frames++;
            }
        }
    }

    private void tick() {
        Game.getInstance().tick();
    }

    private void render() {
        BufferStrategy bfs = this.getBufferStrategy();
        if (bfs == null) {
            createBufferStrategy(2);
            return;
        }
        screen.render(game);
        overlay.render();
        System.arraycopy(screen.pixels, 0, glPixels, 0, WINDOW_WIDTH * WINDOW_HEIGHT);
        System.arraycopy(overlay.pixels, 0, olPixels, 0, WINDOW_WIDTH * WINDOW_HEIGHT);
        Graphics g = bfs.getDrawGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, 640, 480);
        g.drawImage(gameLayer, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
        g.drawImage(overLayer, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
        g.setColor(Color.red);
        g.drawString(FPS + " FPS", 10, 10);
        g.dispose();
        bfs.show();
    }

    public void screenShot() {
        try {
            File f = new File("screenshot");
            f.mkdir();
            Rectangle rectangle = frame.getBounds();
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(rectangle);
            File file = getPossiblePNG();
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTimeString() {
        Calendar c = Calendar.getInstance();
        String ret = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY) + "." + c.get(Calendar.MINUTE) + "." + c.get(Calendar.SECOND);
        return ret;
    }

    public File getPossiblePNG() {
        String s = getTimeString();
        File f = new File("screenshot" + File.separator + s + ".png");
        int cnt = 1;
        while (f.exists()) {
            f = new File("screenshot" + File.separator + s + "(" + cnt + ").png");
            ++cnt;
        }
        return f;
    }
}
