import static java.lang.Math.max;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class NetScreen3 implements NetScreenImageChannel {

    static final int DEFAULT_SCAN_X = 1230;

    static final int DEFAULT_SCAN_Y = 4;

    static final int DEFAULT_SCAN_W = 350;

    static final int DEFAULT_SCAN_H = 28;

    static final String DEFAULT_HOST = "188.195.119.8";

    static final int DEFAULT_PORT = 1001;

    static final int DEFAULT_UPDATE_MS = 100;

    static final int DEFAULT_WINDOW_X = 1680;

    static final int DEFAULT_WINDOW_Y = 0;

    static int SCAN_X = DEFAULT_SCAN_X;

    static int SCAN_Y = DEFAULT_SCAN_Y;

    static int SCAN_W = DEFAULT_SCAN_W;

    static int SCAN_H = DEFAULT_SCAN_H;

    static String HOST = DEFAULT_HOST;

    static int PORT = DEFAULT_PORT;

    static int UPDATE_MS = DEFAULT_UPDATE_MS;

    static int WINDOW_X = DEFAULT_WINDOW_X;

    static int WINDOW_Y = DEFAULT_WINDOW_Y;

    static Rectangle scanArea = null;

    static Robot screen = null;

    static JFrame frame = null;

    static final JLabel imagePanel = new JLabel();

    static boolean run = true;

    static NetScreenImageChannel singleton = null;

    public static NetScreenImageChannel connect(final String host, final int port, final String rmiChannelName) throws Exception {
        return (NetScreenImageChannel) LocateRegistry.getRegistry(host, port).lookup(rmiChannelName);
    }

    public static void publish(final Remote remoteObject, final int port, final String rmiChannelName) throws Exception {
        Registry registry;
        registry = LocateRegistry.createRegistry(port);
        Remote exportedObject;
        if (remoteObject instanceof UnicastRemoteObject) {
            exportedObject = remoteObject;
        } else {
            try {
                exportedObject = UnicastRemoteObject.exportObject(remoteObject, port);
            } catch (final Exception e) {
                try {
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (final NoSuchObjectException e1) {
                    e1.printStackTrace();
                }
                throw e;
            }
        }
        Thread.sleep(100);
        try {
            registry.rebind(rmiChannelName, exportedObject);
        } catch (final Exception e) {
            try {
                UnicastRemoteObject.unexportObject(exportedObject, true);
                UnicastRemoteObject.unexportObject(registry, true);
            } catch (final NoSuchObjectException e1) {
                e1.printStackTrace();
            }
            throw e;
        }
    }

    static void printParameterError(final String[] args, final Throwable t) {
        t.printStackTrace();
        System.out.println("Usage: [scan X][scan Y][scan W][scan H][target host][target port][update delay (ms)][window X][window Y]");
        System.out.println("Defaults: " + "[" + DEFAULT_SCAN_X + "]" + "[" + DEFAULT_SCAN_Y + "]" + "[" + DEFAULT_SCAN_W + "]" + "[" + DEFAULT_SCAN_H + "]" + "[" + DEFAULT_HOST + "]" + "[" + DEFAULT_PORT + "]" + "[" + DEFAULT_UPDATE_MS + "]" + "[" + DEFAULT_WINDOW_X + "]" + "[" + DEFAULT_WINDOW_Y + "]");
        System.out.println("Passed: " + Arrays.toString(args));
    }

    static void initializeSettings(final String[] args) throws Throwable {
        System.out.println("Initializing Settings");
        final int parameterCount = Math.min(args.length, 10);
        switch(parameterCount) {
            case 9:
                WINDOW_Y = max(Integer.valueOf(args[8]), 0);
            case 8:
                WINDOW_X = max(Integer.valueOf(args[7]), 0);
            case 7:
                UPDATE_MS = max(Integer.valueOf(args[6]), 10);
            case 6:
                PORT = Integer.valueOf(args[5]);
            case 5:
                HOST = args[4];
            case 4:
                SCAN_H = max(Integer.valueOf(args[3]), 1);
            case 3:
                SCAN_W = max(Integer.valueOf(args[2]), 1);
            case 2:
                SCAN_Y = max(Integer.valueOf(args[1]), 0);
            case 1:
                SCAN_X = max(Integer.valueOf(args[0]), 0);
        }
        screen = new Robot();
        scanArea = new Rectangle(SCAN_X, SCAN_Y, SCAN_W, SCAN_H);
        System.out.println("* Settings initialization complete.");
    }

    static void update(final ImageIcon image) {
        imagePanel.setIcon(image);
        imagePanel.repaint();
        System.gc();
    }

    static ImageIcon scan() {
        return new ImageIcon(screen.createScreenCapture(scanArea));
    }

    public static void main(final String[] args) throws Throwable {
        try {
            initializeSettings(args);
        } catch (final Throwable t) {
            printParameterError(args, t);
            throw t;
        }
        try {
            System.out.print("Connecting to " + HOST + ":" + PORT + " ... ");
            singleton = connect(HOST, PORT, NetScreen3.class.getSimpleName());
            System.out.println("successful.");
        } catch (final Exception e) {
            System.out.println("Host not reachable");
        }
        initializeGUI();
        if (singleton != null) {
            client();
        } else {
            server();
        }
    }

    static void client() throws Exception {
        while (run) {
            Thread.sleep(UPDATE_MS);
            update(singleton.exchange(scan()));
        }
    }

    static void server() throws Exception {
        System.out.println("Creating server on port " + PORT);
        publish(new NetScreen3(), PORT, NetScreen3.class.getSimpleName());
        System.out.println("* Server created. Waiting for client ...");
    }

    static void initializeGUI() {
        System.out.println("Initializing GUI");
        frame = new JFrame(NetScreen3.class.getSimpleName() + " (" + HOST + ":" + PORT + ")");
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                run = false;
            }

            ;
        });
        final JLabel panel = imagePanel;
        frame.getContentPane().add(panel);
        frame.setUndecorated(true);
        frame.setLocation(WINDOW_X, WINDOW_Y + (singleton != null ? 0 : 100));
        frame.setSize(SCAN_W, SCAN_H);
        frame.setVisible(true);
        System.out.println("* GUI initialization complete.");
    }

    @Override
    public ImageIcon exchange(final ImageIcon image) throws RemoteException {
        update(image);
        return scan();
    }
}

interface NetScreenImageChannel extends Remote {

    public ImageIcon exchange(ImageIcon image) throws RemoteException;
}
