package com.jot.examples;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.json.JSONException;
import org.json.JSONObject;
import com.jot.admin.GetHtmlFromServer;
import com.jot.examples.JavaGameExample.MyGamePanel.Player;

/**
 * An example of a client, in java, for a 'game' that would communicate with an 'agent' on the servers. The agent
 * code is in JavaGameServerExample To run this, just start the main. It will run even if the servers are down. For
 * better results, start a pool by running PoolBringup.main and then run more than one instances of this
 * JavaGameExample.
 * 
 * Click, or type. You should see the changes reflected in all clients.
 * 
 * @author alanwootton
 * 
 */
public class JavaGameExample {

    Socket sock = null;

    String host;

    int port;

    String session = "";

    MyGamePanel gamePanel;

    boolean running = true;

    long timeForNextUpdate = 0;

    long updateInterval = 90 * 1000;

    OutputStream out;

    Socket connectToServer() throws IOException {
        if (1 == 1) {
            System.out.println("sending isup");
            String isup = GetHtmlFromServer.get("localhost", 80, "isup.txt");
            if (isup.length() != 0) {
                host = "localhost";
                port = 8080;
            } else {
                isup = GetHtmlFromServer.get("demo.jotscale.com", 80, "isup.txt");
                if (isup.length() != 0) {
                    host = "demo.jotscale.com";
                    port = 80;
                } else return null;
            }
        } else {
            host = "localhost";
            port = 8000;
        }
        gamePanel.serverStatus = host + ":" + port;
        gamePanel.repaint();
        System.out.println("connecting to " + host + ":" + port);
        sock = new Socket(host, port);
        String connectString = "POST /JavaGameServerExample/post";
        if (session.length() != 0) connectString += "?session=" + session;
        connectString += " HTTP/1.1\r\n";
        connectString += "Cache-Control: no-cache\r\n";
        connectString += "Host: " + host + "\r\n";
        connectString += "Content-Length:0\r\n";
        connectString += "Content-Type: text/javascript\r\n";
        connectString += "\r\n";
        out = sock.getOutputStream();
        out.write(connectString.getBytes());
        return sock;
    }

    void poll() throws IOException, Exception {
        if (sock == null || !sock.isConnected()) {
            sock = null;
            return;
        }
        if (sock.getInputStream().available() > 4) {
            byte[] tmp = new byte[4];
            sock.getInputStream().read(tmp);
            if (tmp[0] == 'H') {
                String err = new String(tmp);
                while (!new String(err).endsWith("\r\n\r\n")) {
                    char c = (char) sock.getInputStream().read();
                    err = err + c;
                }
                System.out.println(err);
                throw new Exception("error message " + err);
            }
            int len = 0;
            len = Integer.parseInt(new String(tmp));
            byte[] bytes = new byte[len];
            int got = sock.getInputStream().read(bytes);
            if (got != bytes.length) throw new Exception("write failed in poll");
            processMessage(new String(bytes));
        }
    }

    /**
     * there is really only one message - json object
     * 
     * @param str
     * @throws JSONException
     */
    void processMessage(String str) throws JSONException {
        System.out.println("received message " + str);
        JSONObject obj = new JSONObject(str);
        if (obj.optString("me", null) != null) {
            session = obj.getString("session");
            if (1 == 2) return; else {
                gamePanel.players.remove("me");
            }
        }
        String userid = obj.getString("session");
        if (obj.optString("x", null) == null) {
            gamePanel.players.remove(userid);
            gamePanel.repaint();
            return;
        }
        Player player = new Player();
        player.x = Integer.parseInt(obj.getString("x"));
        player.y = Integer.parseInt(obj.getString("y"));
        player.latestKey = obj.getString("latestKey").charAt(0);
        gamePanel.players.put(userid, player);
        gamePanel.repaint();
    }

    void send() throws JSONException, IOException {
        if (sock == null || !sock.isConnected()) return;
        if (gamePanel.changed || timeForNextUpdate < System.currentTimeMillis()) {
            gamePanel.changed = false;
            timeForNextUpdate = System.currentTimeMillis() + updateInterval;
            JSONObject msg = new JSONObject();
            msg.put("latestKey", "" + gamePanel.me.latestKey);
            msg.put("x", "" + gamePanel.me.x);
            msg.put("y", "" + gamePanel.me.y);
            System.out.println("client sending " + msg);
            byte[] bmsg = msg.toString().getBytes();
            int len = bmsg.length;
            String tmp = "0000" + len;
            tmp = tmp.substring(tmp.length() - 4);
            out.write(tmp.getBytes());
            out.write(bmsg);
        }
    }

    void runGame() throws Exception {
        gamePanel = startUI();
        sock = connectToServer();
        while (running) {
            send();
            poll();
            try {
                Thread.sleep(100);
                if (sock == null && timeForNextUpdate < System.currentTimeMillis()) sock = connectToServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JavaGameExample example = new JavaGameExample();
        try {
            example.runGame();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static MyGamePanel startUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyGamePanel panel = new MyGamePanel();
        f.getContentPane().add(panel);
        f.setSize(300, 300);
        f.setLocation(100, 100);
        f.setVisible(true);
        f.addKeyListener(panel);
        f.addMouseListener(panel);
        return panel;
    }

    static class MyGamePanel extends JPanel implements KeyListener, MouseListener {

        private static final long serialVersionUID = 1L;

        static class Player {

            int x = 10;

            int y = 10;

            char latestKey = 'A';
        }

        Map<String, Player> players = new HashMap<String, Player>();

        Player me;

        String serverStatus = "not connected";

        Font font;

        public boolean changed = false;

        public MyGamePanel() {
            font = new Font("lucida sans regular", Font.PLAIN, 16);
            Random rand = new Random();
            me = new Player();
            me.x = 10 + rand.nextInt(300 - 20);
            me.y = 10 + rand.nextInt(300 - 20);
            me.latestKey = (char) ('A' + rand.nextInt(26));
            players.put("me", me);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(font);
            for (Player player : players.values()) {
                g2.setColor(Color.blue);
                g2.fillOval(player.x, player.y, 24, 24);
                g2.setColor(Color.white);
                g2.drawString("" + player.latestKey, player.x + 6, player.y + 20);
            }
            g2.setColor(Color.blue);
            g2.drawString(serverStatus, 4, 12);
            g2.drawString("click or type", 4, 26);
        }

        @Override
        public void keyPressed(KeyEvent arg0) {
        }

        @Override
        public void keyReleased(KeyEvent arg0) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            char newchar = e.getKeyChar();
            if (newchar != me.latestKey) {
                me.latestKey = newchar;
                changed = true;
                this.repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            me.x = e.getX() - 10;
            me.y = e.getY() - 10 - 20;
            changed = true;
            this.repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }
}
