package org.magnesia.chalk;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.magnesia.chalk.data.Constants;
import static org.magnesia.misc.Utils.log;

public class Chalk {

    private ServerSocket ss;

    private int port = Constants.PORT;

    private InetAddress ip;

    static String config_file = Constants.CONFIG_FILE;

    final ChalkProperties p;

    private boolean running = true;

    final List<ChalkListener> listeners;

    private Validator v;

    public Chalk() throws IOException {
        p = new ChalkProperties(config_file);
        listeners = new CopyOnWriteArrayList<ChalkListener>();
        try {
            if (p.containsKey("IP")) ip = InetAddress.getByName(p.getProperty("IP"));
        } catch (Exception e) {
            log("Couldn't parse setting for PORT - " + p.getProperty("PORT") + ". Using default " + port);
        }
        try {
            if (p.containsKey("PORT")) port = Integer.parseInt(p.getProperty("PORT"));
        } catch (Exception e) {
            log("Couldn't parse setting for PORT - " + p.getProperty("PORT") + ". Using default " + port);
        }
        if (ip == null) ip = InetAddress.getByName(Constants.IP);
        ss = new ServerSocket();
        ss.bind(new InetSocketAddress(ip, port));
        if (p.containsKey("VALIDATOR") && p.getProperty("VALIDATOR") != null) {
            try {
                Class<?> c = Class.forName(p.getProperty("VALIDATOR"));
                if (Validator.class.isAssignableFrom(c)) {
                    Constructor<?> ctr = c.getConstructor(ChalkProperties.class);
                    v = (Validator) ctr.newInstance(p);
                }
            } catch (Throwable t) {
                log("Failed to create user specified validator");
                t.printStackTrace();
            }
        }
        if (v == null) {
            v = new DefaultValidator(p);
        }
    }

    public Validator getValidator() {
        return v;
    }

    public void addListener(ChalkListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(ChalkListener l) {
        listeners.remove(l);
    }

    public void start() throws IOException {
        try {
            if (!ss.isBound()) {
                log("Unable to bind port! Already in use?");
                System.exit(1);
            }
            log("Chalk started up listening on " + ip + ":" + port);
            while (running) {
                Socket sock = ss.accept();
                log("Incoming connection from " + sock.getInetAddress());
                for (ChalkListener cl : listeners) {
                    cl.clientAdded();
                }
                new Thread(new Connection(sock, this, p)).start();
            }
        } catch (IOException e) {
            running = false;
            throw e;
        }
    }

    public boolean isRunning() {
        return running;
    }

    boolean isValidUsername(String username) {
        if (username.length() >= 4) {
            return v.isUsernameValid(username);
        }
        return false;
    }

    public void stop() throws IOException {
        running = false;
        ss.close();
    }

    public String getBaseDir() {
        return p.getProperty("BASEDIR", "/");
    }

    public void setBaseDir(String base) {
        p.setProperty("BASEDIR", base);
        p.save();
    }

    public int getPort() {
        int port = Constants.PORT;
        try {
            port = Integer.parseInt(p.getProperty("PORT", "" + Constants.PORT));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return port;
    }

    public void setPort(int port) {
        p.setProperty("PORT", "" + port);
        p.save();
    }

    public InetAddress getInetAddress() {
        InetAddress ia = null;
        try {
            ia = InetAddress.getByName(Constants.IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            ia = InetAddress.getByName(p.getProperty("IP", Constants.IP));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ia;
    }

    public void setInetAddress(InetAddress ia) {
        p.setProperty("IP", ia.getHostAddress());
        p.save();
    }

    public static void main(String args[]) {
        try {
            new Chalk().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
