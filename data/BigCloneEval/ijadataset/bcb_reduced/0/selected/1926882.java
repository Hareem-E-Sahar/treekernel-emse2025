package com.hp.hpl.guess;

import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.python.core.PySystemState;
import com.hp.hpl.guess.storage.StorageFactory;
import com.hp.hpl.guess.ui.ExceptionWindow;
import com.hp.hpl.guess.ui.VisFactory;

public class GuessApplet extends Applet {

    private static final long serialVersionUID = -8323177849186174338L;

    public void init() {
        try {
            PySystemState.initializeApplet();
            Guess.setAppletMode(true, getAppletContext());
            Guess.configureUI();
            Guess.useFakeInterpreter(true);
            System.out.println("init...");
            String db_url = getParameter("URL");
            if ((db_url != null) && (!db_url.equals(""))) {
                try {
                    URLConnection conn = null;
                    DataInputStream data = null;
                    String line;
                    StringBuffer buf = new StringBuffer();
                    URL theURL = new URL(db_url);
                    conn = theURL.openConnection();
                    conn.connect();
                    data = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
                    while ((line = data.readLine()) != null) {
                        buf.append(line + "\n");
                    }
                    data.close();
                    StorageFactory.useDBServer();
                    StorageFactory.loadFromText(buf.toString());
                } catch (MalformedURLException e) {
                    System.out.println("Bad URL: " + db_url);
                } catch (IOException e) {
                    System.out.println("IO Error:" + e.getMessage());
                }
            } else {
                String applet_db = getParameter("DB");
                applet_db = applet_db.replace(';', '\n');
                StorageFactory.useDBServer();
                StorageFactory.loadFromText(applet_db);
            }
            String mode = getParameter("VISMODE");
            if (mode == null) {
                mode = "PICCOLO";
            }
            if (mode.equalsIgnoreCase("PICCOLO")) {
                Guess.init(VisFactory.PICCOLO, false, false);
            } else if (mode.equalsIgnoreCase("PREFUSE")) {
                Guess.init(VisFactory.PREFUSE, false, false);
            } else if (mode.equalsIgnoreCase("TOUCHGRAPH")) {
                Guess.init(VisFactory.TOUCHGRAPH, false, false);
            }
            String toolbar = getParameter("TOOLBAR");
            if ((toolbar != null) && (!toolbar.equals(""))) {
                Class tb = Class.forName(toolbar);
                Constructor tcon = tb.getConstructor((Class[]) null);
                Object o = tcon.newInstance((Object[]) null);
            }
        } catch (Exception e) {
            ExceptionWindow.getExceptionWindow(e);
            e.printStackTrace();
        }
    }

    public void stop() {
        destory();
        super.stop();
    }

    public void destory() {
        Guess.shutdown();
        super.destroy();
    }
}
