import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class UPBP extends Applet implements Runnable, WindowListener {

    int nplayers;

    int pframe = 0;

    int memPFrame = 0;

    int subfr = 0;

    boolean running = true;

    boolean cont = false;

    boolean loop = false;

    boolean rev = false;

    int numObjects = 23;

    Player players[] = new Player[numObjects];

    Game game = new Game();

    Thread relaxer;

    FieldPanel panel;

    UPBPControls controlPanel;

    UPBPComment comment;

    static Frame frame;

    public UPBP(String url) {
        loadGame(url);
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(3, " ");
        addPlayer(1, "1");
        addPlayer(1, "2");
        addPlayer(1, "3");
        addPlayer(1, "4");
        addPlayer(1, "5");
        addPlayer(1, "6");
        addPlayer(1, "7");
        addPlayer(2, "1");
        addPlayer(2, "2");
        addPlayer(2, "3");
        addPlayer(2, "4");
        addPlayer(2, "5");
        addPlayer(2, "6");
        addPlayer(2, "7");
        addPlayer(4, " ");
        setLayout(null);
        panel = new FieldPanel();
        add(panel);
        panel.setBounds(10, 10, 611, 235);
        controlPanel = new UPBPControls();
        add(controlPanel);
        controlPanel.setBounds(10, 245, 611, 35);
        comment = new UPBPComment();
        add(comment);
        comment.setBounds(10, 280, 611, 80);
        panel.repaint();
        controlPanel.repaint();
        comment.repaint();
        running = true;
    }

    public void destroy() {
        remove(panel);
        remove(controlPanel);
    }

    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }

    public void stop() {
        relaxer = null;
    }

    public void loadGame(String name) {
        String playfile = "";
        URL theURL;
        BufferedReader in;
        boolean weregood = false;
        boolean malformed = false;
        try {
            theURL = new URL(name);
            URLConnection conn = null;
            BufferedReader data = null;
            String line;
            StringBuffer buf = new StringBuffer();
            try {
                conn = theURL.openConnection();
                conn.connect();
                data = new BufferedReader(new InputStreamReader(new BufferedInputStream(conn.getInputStream())));
                while ((line = data.readLine()) != null) {
                    buf.append(line);
                }
                playfile = buf.toString();
                data.close();
                weregood = true;
            } catch (IOException e) {
                System.out.println("IO Error:" + e.getMessage());
            }
        } catch (MalformedURLException e) {
            System.out.println("Bad URL: " + name);
            malformed = true;
        }
        if (malformed) {
            System.out.println("trying file: " + name);
            try {
                File file = new File(name);
                if (file != null && file.exists()) {
                    long inlen = (file.length());
                    in = new BufferedReader(new FileReader((File) file));
                    if (in != null) {
                        try {
                            int linecount = 0;
                            while ((playfile.length() + linecount) < inlen) {
                                playfile = playfile.concat(in.readLine());
                                linecount++;
                            }
                            weregood = true;
                        } catch (IOException io) {
                            System.out.println(io.toString());
                            weregood = false;
                        }
                    }
                }
            } catch (SecurityException ex) {
                System.out.println(ex.toString());
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
        if (weregood) {
        } else {
            playfile = "[[BeginPlaybookFile 2.0 1~3000:260,100`3001:260,120`3002:260,140`3003:260,160`3004:260,180`3005:260,200`3006:260,220`3007:260,240`1000:260,260`1001:260,280`1002:260,300`1003:260,320`1004:260,340`1005:260,360`1006:260,380`2000:260,400`2001:260,420`2002:260,440`2003:260,460`2004:260,480`2005:260,500`2006:260,520`4000:160,540`~100~couldn't load url or file!| EndPlaybookFile]]";
            System.out.println("couldn't load url or file; using fallback example instead");
        }
        weregood = false;
        String header = playfile.substring(0, 23);
        if (header.equals("[[BeginPlaybookFile 2.0")) {
            weregood = true;
        } else if (header.equals("[[BeginPlaybookFile 1.0")) {
            System.out.println("UltiamtePlayBook 1.0 files not supported anymore!");
        } else {
            System.out.println("couldn't parse file; Not a UltiamtePlayBook 2.0 file!");
        }
        if (weregood) {
            String pframes = playfile.substring(23, playfile.lastIndexOf('|'));
            for (StringTokenizer t = new StringTokenizer(pframes, "|"); t.hasMoreTokens(); ) {
                String pframeraw = t.nextToken();
                PFrame apframe = new PFrame();
                int lag = Integer.valueOf(pframeraw.substring(pframeraw.indexOf('~', 10) + 1, pframeraw.lastIndexOf('~'))).intValue();
                apframe.setLag(lag);
                String fcomment = pframeraw.substring(pframeraw.lastIndexOf('~') + 1, pframeraw.length());
                apframe.setComment(fcomment);
                int i = pframeraw.indexOf('~') + 1;
                int endi = pframeraw.indexOf('~', 10);
                String pframe = pframeraw.substring(i, endi);
                for (StringTokenizer tk = new StringTokenizer(pframe, "`"); tk.hasMoreTokens(); ) {
                    String player = tk.nextToken();
                    Position pos = new Position();
                    int si = player.indexOf(':');
                    int ci = player.indexOf(',');
                    int ei = player.length();
                    pos.x = Integer.valueOf(player.substring(si + 1, ci)).intValue();
                    pos.y = Integer.valueOf(player.substring(ci + 1, ei)).intValue();
                    apframe.addPosition(pos);
                }
                game.addPFrame(apframe);
            }
        } else {
            System.out.println("Try another file!");
        }
    }

    int addPlayer(int team, String lbl) {
        PFrame pframe0 = game.pframes[0];
        Player n = new Player();
        n.x = pframe0.positions[nplayers].y;
        n.y = pframe0.positions[nplayers].x;
        n.lbl = lbl;
        n.team = team;
        players[nplayers] = n;
        return nplayers++;
    }

    public void run() {
        Thread me = Thread.currentThread();
        while (relaxer == me) {
            if (running) {
                relax();
            }
            try {
                Thread.sleep(17);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    synchronized void relax() {
        PFrame fromPFrame = game.pframes[memPFrame];
        PFrame toPFrame = game.pframes[pframe];
        if (subfr == 0) {
            comment.setCommentArea(toPFrame.getComment());
        }
        for (int i = 0; i < numObjects; i++) {
            Player n = players[i];
            n.x = n.x + (toPFrame.positions[i].y - fromPFrame.positions[i].y) / (toPFrame.lag + 1);
            n.y = n.y + (toPFrame.positions[i].x - fromPFrame.positions[i].x) / (toPFrame.lag + 1);
        }
        panel.repaint();
        comment.repaint();
        subfr++;
        if (subfr < (toPFrame.lag + 1)) {
            running = true;
        } else {
            if (!cont) {
                running = false;
            } else {
                go();
                running = true;
            }
        }
    }

    public void go() {
        memPFrame = pframe;
        subfr = 0;
        if (!rev) {
            if (pframe < (game.npframes - 1)) {
                pframe++;
            } else {
                if (loop) {
                    pframe = 0;
                } else {
                    pframe = game.npframes - 1;
                }
            }
        } else {
            if (pframe > 0) {
                pframe--;
            } else {
                if (loop) pframe = game.npframes - 1; else {
                    pframe = 0;
                }
            }
        }
    }

    public void reset() {
        PFrame pframe0 = game.pframes[0];
        for (int i = 0; i < numObjects; i++) {
            Player n = players[i];
            n.x = pframe0.positions[i].y;
            n.y = pframe0.positions[i].x;
        }
        pframe = 0;
        memPFrame = 0;
        subfr = 0;
        running = true;
        cont = false;
    }

    class FieldPanel extends Panel {

        Image offscreen;

        Dimension offscreensize;

        Graphics offgraphics;

        public FieldPanel() {
        }

        public void paintPlayer(Graphics g, Player n, FontMetrics fm) {
            int x = (int) n.x;
            int y = (int) n.y;
            char ch = (char) n.lbl.charAt(0);
            boolean dig = Character.isDigit(ch);
            Polygon cone = new Polygon();
            cone.addPoint(x - 4, y + 4);
            cone.addPoint(x + 4, y + 4);
            cone.addPoint(x, y - 5);
            if (n.team == 3 || n.team == 4) {
                if (n.team == 3) {
                    g.setColor(Color.yellow);
                    g.fillPolygon(cone);
                } else {
                    g.setColor(Color.black);
                    g.fillOval(x - 5, y - 5, 10, 10);
                    g.setColor(Color.white);
                    g.fillOval(x - 3, y - 3, 6, 6);
                }
            } else {
                if (n.team == 1) {
                    g.setColor(Color.magenta);
                } else {
                    g.setColor(Color.blue);
                }
                int w = 10;
                int h = w;
                g.fillOval(x - w / 2, y - h / 2, w, h);
                g.setColor(Color.white);
                g.drawString(n.lbl, x - (w - 5) / 2, (y - h / 2 + fm.getAscent()));
            }
        }

        public synchronized void update(Graphics g) {
            Dimension d = getSize();
            Font font = new Font("Monaco", 1, 8);
            Color fieldgreen = new Color(50, 205, 50);
            if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
                offscreen = createImage(d.width, d.height);
                offscreensize = d;
                if (offgraphics != null) {
                    offgraphics.dispose();
                }
                offgraphics = offscreen.getGraphics();
                offgraphics.setFont(font);
            }
            offgraphics.setColor(Color.white);
            offgraphics.fillRect(0, 0, d.width, d.height);
            offgraphics.setColor(fieldgreen);
            offgraphics.fillRect(5, 5, 600, 222);
            offgraphics.setColor(Color.black);
            offgraphics.drawRect(5, 5, 600, 222);
            offgraphics.drawRect(5 + 108, 5, 384, 222);
            FontMetrics fm = offgraphics.getFontMetrics();
            for (int i = 0; i < nplayers; i++) {
                paintPlayer(offgraphics, players[i], fm);
            }
            g.drawImage(offscreen, 0, 0, null);
        }
    }

    class UPBPControls extends Panel implements ActionListener, ItemListener {

        Button playb;

        Button stopb;

        Button stepb;

        Checkbox loopb;

        Checkbox revb;

        Button resetb;

        Button exitb;

        public UPBPControls() {
            playb = new Button("play");
            stopb = new Button("stop");
            stepb = new Button("step");
            loopb = new Checkbox("loop");
            revb = new Checkbox("revs");
            resetb = new Button("reset");
            exitb = new Button("exit");
            add(playb);
            playb.addActionListener(this);
            add(stopb);
            stopb.addActionListener(this);
            add(stepb);
            stepb.addActionListener(this);
            add(loopb);
            loopb.addItemListener(this);
            add(revb);
            revb.addItemListener(this);
            add(resetb);
            resetb.addActionListener(this);
            add(exitb);
            exitb.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == playb && !running) {
                cont = true;
                go();
                running = true;
            } else if (src == stopb) {
                cont = false;
            } else if (src == stepb && !running) {
                cont = false;
                go();
                running = true;
            } else if (src == resetb) {
                reset();
            } else if (src == exitb) {
                System.exit(0);
            }
        }

        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            boolean on = e.getStateChange() == ItemEvent.SELECTED;
            if (src == loopb) loop = on; else if (src == revb) rev = on;
        }
    }

    public class UPBPComment extends Panel {

        TextArea commentArea;

        public UPBPComment() {
            commentArea = new TextArea("", 3, 55, TextArea.SCROLLBARS_NONE);
            commentArea.setEditable(false);
            commentArea.setFont(new Font("Monaco", 1, 9));
            add(commentArea);
        }

        void setCommentArea(String argComment) {
            commentArea.setText(argComment);
            commentArea.repaint();
        }
    }

    public class Player {

        double x;

        double y;

        int team;

        String lbl;

        boolean fixed;
    }

    class Position {

        double x;

        double y;
    }

    class PFrame {

        Position positions[] = new Position[numObjects];

        int npositions = 0;

        int lag = 100;

        String comment = "";

        int addPosition(Position position) {
            positions[npositions] = position;
            return npositions++;
        }

        int addPosition(double x, double y) {
            Position pos = new Position();
            pos.x = x;
            pos.y = y;
            positions[npositions] = pos;
            return npositions++;
        }

        void setLag(int argLag) {
            lag = argLag;
        }

        void setComment(String argComment) {
            comment = argComment;
        }

        String getComment() {
            return comment;
        }
    }

    class Game {

        int npframes = 0;

        PFrame pframes[] = new PFrame[50];

        int addPFrame(PFrame pframe) {
            pframes[npframes] = pframe;
            return npframes++;
        }
    }

    public static void main(String args[]) {
        String play = "./test.upf";
        frame = new Frame("UPBPlayer");
        final UPBP upbp = new UPBP(args.length == 0 ? play : args[0]);
        upbp.start();
        frame.add("Center", upbp);
        frame.setSize(631, 380);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.addWindowListener(upbp);
    }

    public String getAppletInfo() {
        return "Title: UPBP\nAuthor: Heiko Goelzer\nPlayer for UltimatePlayBook files.";
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
        frame.repaint();
        panel.repaint();
    }

    public void windowActivated(WindowEvent e) {
        frame.repaint();
        panel.repaint();
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
