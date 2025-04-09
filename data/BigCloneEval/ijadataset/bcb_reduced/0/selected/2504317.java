package client;

import general.FileFilter;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import audio.player.jlp;

/**
 * starts the client
 * 
 * @author ingo breuer
 */
public class CRun {

    private static final long serialVersionUID = 7403799213129951450L;

    private CImagePaths imP = new CImagePaths();

    private JLabel stat = new JLabel("Status: Disconnected");

    public GUI gui;

    /**
         * Clientdata [0] Url, [1] Port, [2] Name, [3] Id
         */
    private String[] client = new String[4];

    /**
         * LevelField
         */
    private char[][] field;

    /**
         * The Sound
         */
    private jlp sound = new jlp();

    /**
         * The Network
         */
    private CNetwork net;

    /**
         * To use a skill targeting is required
         */
    private boolean target = false;

    public class GUI extends JFrame implements KeyListener, MouseMotionListener, MouseListener, ActionListener {

        private static final long serialVersionUID = 1L;

        private Areas areas = new Areas();

        /**
         * Adds a border with a title to a JComponent
         * 
         * @param J
         * @param title
         */
        public void setBorder(JComponent J, String title) {
            J.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title), BorderFactory.createBevelBorder(0)));
        }

        public GUI() {
            super("Diamaond Search");
            try {
                JFrame.setDefaultLookAndFeelDecorated(true);
                addMenuBar(this);
                this.setResizable(false);
                this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                this.setBorder(areas.ca, "");
                this.areas.ca.setBackground(Color.GRAY);
                this.areas.ca.setFocusable(false);
                this.add("North", areas.na);
                this.add("South", areas.sa);
                this.add("Center", areas.ca);
                this.add("West", areas.wa);
                this.add("East", areas.ea);
                try {
                    String path = "/images/style1/obj_diamond_small.gif";
                    Image iconI = null;
                    URL iconURL = getClass().getResource(path);
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    iconI = tk.getImage(iconURL);
                    this.setIconImage(iconI);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                this.areas.ca.setPreferredSize(new Dimension(500, 400));
                this.pack();
                this.setVisible(true);
            } catch (Exception e) {
                new CException(e, true, "Error: Trying to create Main JFrame");
            }
        }

        public void addMenuBar(JFrame frame) {
            String[] menuMain = { "Menu", "Connect", "Disconnect", "Screenshot", "FrameShot", "Quit" };
            String[] menuHelp = { "?", "Help", "BugReport", "About" };
            JMenuBar menuBar = new JMenuBar();
            menuBar.setLayout(new BorderLayout());
            menuBar.add("West", createMenu(menuMain));
            menuBar.add("East", createMenu(menuHelp));
            frame.setJMenuBar(menuBar);
        }

        public JMenu createMenu(String[] itemNames) {
            JMenu menu = new JMenu(itemNames[0]);
            for (int i = 1; i < itemNames.length; i++) {
                JMenuItem item = menuItem(itemNames[i], itemNames[i]);
                menu.add(item);
            }
            return menu;
        }

        public JMenuItem menuItem(String label, String command) {
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(this);
            item.setActionCommand(command);
            return item;
        }

        public class Areas {

            private static final long serialVersionUID = 1L;

            /**
                 * @param gui
                 */
            private Center ca = new Center();

            private North na = new North();

            private South sa = new South();

            private West wa = new West();

            private East ea = new East();

            public class Center extends JPanel {

                private static final long serialVersionUID = -2356661171879301096L;

                private Image image = null;

                private int[] newPlayerPos = { 0, 0 };

                private int[] viewPos = { 0, 0 };

                /**
                 * The Level charSet to be drawn
                 */
                private char[][] level;

                /**
                 * The Map, where the images are beeing stored
                 */
                private HashMap<String, Image> imageMap = new HashMap<String, Image>();

                /**
                 * Tell the paintComponent() to only load the images once.
                 */
                private boolean loadimg;

                /**
                 * Loads the Images into a HashMap
                 */
                private void loadimages() {
                    CImagePaths imp = new CImagePaths();
                    this.setDoubleBuffered(true);
                    loadimg = true;
                    for (int i = 0; i < imp.images.length; i++) {
                        String name = imp.path + imp.images[i] + ".gif";
                        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(name));
                        this.imageMap.put(imp.images[i], image);
                    }
                }

                /**
                 * Loads and Paints the Graphic Elements
                 */
                public void paint(Graphics g) {
                    super.paint(g);
                    if (level == null) return;
                    if (!loadimg) loadimages();
                    int height = level.length;
                    int width = level[0].length;
                    int image_width = this.imageMap.get("obj_empty").getWidth(null);
                    int image_height = this.imageMap.get("obj_empty").getHeight(null);
                    int dwidth = Math.abs(this.getWidth() / image_width);
                    int dheight = Math.abs(this.getHeight() / image_height);
                    do {
                        if (dwidth > width) {
                            dwidth = width;
                            viewPos[0] = 0;
                        } else {
                            if (newPlayerPos[0] + 3 > viewPos[0] + dwidth) {
                                viewPos[0] += 1;
                            }
                            if (newPlayerPos[0] - 2 < viewPos[0]) {
                                viewPos[0] -= 1;
                            }
                            if (viewPos[0] < 0) viewPos[0] = 0;
                            if (viewPos[0] + dwidth > width) viewPos[0] = width - dwidth;
                        }
                        if (dheight > height) {
                            dheight = height;
                            viewPos[1] = 0;
                        } else {
                            if (newPlayerPos[1] + 3 > viewPos[1] + dheight) {
                                viewPos[1] += 1;
                            }
                            if (newPlayerPos[1] - 2 < viewPos[1]) {
                                viewPos[1] -= 1;
                            }
                            if (viewPos[1] < 0) viewPos[1] = 0;
                            if (viewPos[1] + dheight > height) viewPos[1] = height - dheight;
                        }
                    } while (newPlayerPos[0] < viewPos[0] || newPlayerPos[0] >= viewPos[0] + dwidth || newPlayerPos[1] < viewPos[1] || newPlayerPos[1] > viewPos[1] + dheight);
                    try {
                        for (int i = viewPos[1]; i < viewPos[1] + dheight; i++) {
                            for (int j = viewPos[0]; j < viewPos[0] + dwidth; j++) {
                                switch(level[i][j]) {
                                    default:
                                        image = imageMap.get("obj_wall_static");
                                        break;
                                    case '#':
                                        image = imageMap.get("obj_wall_static");
                                        break;
                                    case '~':
                                        image = imageMap.get("obj_wall_red");
                                        break;
                                    case '*':
                                        image = imageMap.get("obj_wall_blue");
                                        break;
                                    case 'g':
                                        image = imageMap.get("obj_wall_blue_broke");
                                        break;
                                    case 'Y':
                                        image = imageMap.get("obj_wall_blue_shake");
                                        break;
                                    case 'i':
                                        image = imageMap.get("obj_ice_static");
                                        break;
                                    case 'l':
                                        image = imageMap.get("obj_ice_left");
                                        break;
                                    case 'r':
                                        image = imageMap.get("obj_ice_right");
                                        break;
                                    case 'o':
                                        image = imageMap.get("obj_ice_up");
                                        break;
                                    case 'u':
                                        image = imageMap.get("obj_ice_down");
                                        break;
                                    case 'K':
                                        image = imageMap.get("obj_key_red");
                                        break;
                                    case 'Z':
                                        image = imageMap.get("obj_key_blue");
                                        break;
                                    case 'P':
                                        image = imageMap.get("obj_key_green");
                                        break;
                                    case 'Q':
                                        image = imageMap.get("obj_key_yellow");
                                        break;
                                    case 'z':
                                        image = imageMap.get("obj_fire_static");
                                        break;
                                    case '<':
                                        image = imageMap.get("obj_fire_left");
                                        break;
                                    case '>':
                                        image = imageMap.get("obj_fire_right");
                                        break;
                                    case '^':
                                        image = imageMap.get("obj_fire_up");
                                        break;
                                    case 'v':
                                        image = imageMap.get("obj_fire_down");
                                        break;
                                    case 'O':
                                        image = imageMap.get("obj_rock_static");
                                        break;
                                    case '(':
                                        image = imageMap.get("obj_rock_static");
                                        break;
                                    case ')':
                                        image = imageMap.get("obj_rock_static");
                                        break;
                                    case 'U':
                                        image = imageMap.get("obj_rock_static");
                                        break;
                                    case 'R':
                                        image = imageMap.get("obj_rock_static");
                                        break;
                                    case 'T':
                                        image = imageMap.get("obj_portal_animation");
                                        break;
                                    case 't':
                                        image = imageMap.get("obj_portal_static");
                                        break;
                                    case 'A':
                                        image = imageMap.get("obj_exit_closed");
                                        break;
                                    case 'a':
                                        image = imageMap.get("obj_exit_open");
                                        break;
                                    case '1':
                                        image = imageMap.get("obj_player1_static");
                                        break;
                                    case '2':
                                        image = imageMap.get("obj_player2_static");
                                        break;
                                    case '3':
                                        image = imageMap.get("obj_player3_static");
                                        break;
                                    case '4':
                                        image = imageMap.get("obj_player4_static");
                                        break;
                                    case '5':
                                        image = imageMap.get("obj_player1_animation");
                                        break;
                                    case '6':
                                        image = imageMap.get("obj_player2_animation");
                                        break;
                                    case '7':
                                        image = imageMap.get("obj_player3_animation");
                                        break;
                                    case '8':
                                        image = imageMap.get("obj_player4_animation");
                                        break;
                                    case 'E':
                                        image = imageMap.get("obj_enemy_fire");
                                        break;
                                    case 'e':
                                        image = imageMap.get("obj_enemy");
                                        break;
                                    case ' ':
                                        image = imageMap.get("obj_empty");
                                        break;
                                    case 'k':
                                        image = imageMap.get("obj_diamond");
                                        break;
                                    case 'S':
                                        image = imageMap.get("obj_surprise");
                                        break;
                                    case 'd':
                                        image = imageMap.get("obj_grave");
                                        break;
                                }
                                g.drawImage(image, (j - viewPos[0]) * image_width, (i - viewPos[1]) * image_height, this);
                            }
                        }
                    } catch (Exception e) {
                        new CException(e, false, "Error while repainting the Level");
                    }
                }

                protected String id;

                /**
                 * Sets the char[][] chars that defines which
                 * 
                 * @param lvl
                 * @param string
                 */
                private void setLevel(char[][] lvl, int[] is, String string) {
                    this.id = string;
                    level = lvl;
                    newPlayerPos = is;
                    repaint();
                }

                public void update(Graphics g) {
                    paint(g);
                }
            }

            public class North extends JPanel implements FocusListener {

                private static final long serialVersionUID = 1L;

                private JLabel level = new JLabel();

                private JLabel name = new JLabel();

                private JProgressBar timer = new JProgressBar();

                private JTextArea msg = new JTextArea(4, 1);

                private JTextArea info = new JTextArea(3, 1);

                private JProgressBar pow = new JProgressBar();

                private JLabel points = new JLabel();

                private JScrollPane scrollmsg = new JScrollPane(msg);

                private JTextField send = new JTextField("");

                private North() {
                    super();
                    this.setDoubleBuffered(true);
                    this.setFocusable(false);
                    Center blub = new Center();
                    this.info.add(blub);
                    this.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.weightx = 0.5;
                    c.weighty = 0.5;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.gridwidth = 1;
                    this.add(name, c);
                    c.gridx = 1;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.gridwidth = 1;
                    c.ipadx = 10;
                    this.add(level, c);
                    c.gridx = 2;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.ipadx = 10;
                    this.add(points, c);
                    c.gridx = 3;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.gridwidth = 3;
                    this.add(pow, c);
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 4;
                    this.add(scrollmsg, c);
                    c.gridx = 4;
                    c.gridy = 1;
                    c.gridheight = 2;
                    this.add(info, c);
                    c.gridx = 0;
                    c.gridy = 2;
                    c.gridheight = 1;
                    c.gridwidth = 5;
                    this.add(send, c);
                    this.info.setSelectedTextColor(Color.BLUE);
                    this.pow.setStringPainted(true);
                    this.pow.setString("P O W E R");
                    GUI.this.setBorder(name, "Name");
                    GUI.this.setBorder(pow, "Power");
                    GUI.this.setBorder(points, "Points");
                    GUI.this.setBorder(level, "Level");
                    GUI.this.setBorder(scrollmsg, "Chat");
                    GUI.this.setBorder(timer, "Time left");
                    GUI.this.setBorder(send, "Your Message");
                    GUI.this.setBorder(info, "Players-Infotable:");
                    this.send.setFocusable(true);
                    this.info.setEditable(false);
                    this.info.setLineWrap(false);
                    this.msg.setEditable(false);
                    this.msg.setLineWrap(true);
                    this.msg.setFocusable(false);
                    this.info.setFocusable(false);
                    this.scrollmsg.setFocusable(false);
                    this.send.addFocusListener(this);
                }

                public void focusGained(FocusEvent arg0) {
                    send.setText("");
                }

                public void focusLost(FocusEvent arg0) {
                    String[] sendmsg = { "Your Window to the World", "Maybe you should tipp something here.", "Your Message on this line", "Anything new?", "What's up?", "Got something to say?", "To be or not to be...", "Keep a straight face", "Your a Genius", "Use TAB to switch to 'this' Line", "I am your psychologist", "Don't worry, be happy", "Sometimes ... just sometimes I wish I could fly", "Errors are natural => Kant was wrong", "Nothing to say", "Lack of interesst can be ignored" };
                    int a = (int) (Math.random() * 100) % sendmsg.length;
                    areas.na.send.setText(sendmsg[a]);
                }
            }

            public class East extends JPanel {

                private static final long serialVersionUID = 1L;

                /**
                 * Red Key
                 */
                private JLabel rkey = new JLabel();

                /**
                 * Blue Key
                 */
                private JLabel bkey = new JLabel();

                /**
                 * Green Key
                 */
                private JLabel gkey = new JLabel();

                /**
                 * Yellow Key
                 */
                private JLabel ykey = new JLabel();

                private East() {
                    super();
                    this.setDoubleBuffered(true);
                    GUI.this.setBorder(this, "Keys");
                    GUI.this.setBorder(rkey, "");
                    GUI.this.setBorder(bkey, "");
                    GUI.this.setBorder(gkey, "");
                    GUI.this.setBorder(ykey, "");
                    this.rkey.setIcon(getImage(imP.path + "obj_key_red.gif"));
                    this.bkey.setIcon(getImage(imP.path + "obj_key_blue.gif"));
                    this.gkey.setIcon(getImage(imP.path + "obj_key_green.gif"));
                    this.ykey.setIcon(getImage(imP.path + "obj_key_yellow.gif"));
                    this.rkey.setEnabled(false);
                    this.bkey.setEnabled(false);
                    this.gkey.setEnabled(false);
                    this.ykey.setEnabled(false);
                    this.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.weightx = 0.5;
                    c.weighty = 0.5;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = 1;
                    c.gridwidth = 1;
                    this.add(rkey, c);
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridheight = 1;
                    c.gridwidth = 1;
                    this.add(bkey, c);
                    c.gridx = 0;
                    c.gridy = 2;
                    c.gridheight = 1;
                    c.gridwidth = 1;
                    this.add(gkey, c);
                    c.gridx = 0;
                    c.gridy = 3;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(ykey, c);
                    this.rkey.setBackground(Color.WHITE);
                    this.bkey.setBackground(Color.WHITE);
                    this.gkey.setBackground(Color.WHITE);
                    this.ykey.setBackground(Color.WHITE);
                    this.setFocusable(false);
                }
            }

            /**
                 * 
                 */
            public class South extends JPanel {

                private static final long serialVersionUID = 1L;

                /**
                 * Shows Time to finish this Level
                 */
                private JProgressBar timer = new JProgressBar();

                /**
                 * Shows if the Client is still connected
                 */
                private South() {
                    this.setDoubleBuffered(true);
                    this.setLayout(new GridLayout(0, 1));
                    this.add("North", timer);
                    this.add("South", stat);
                    timer.setString("Seconds");
                    timer.setStringPainted(true);
                    GUI.this.setBorder(timer, "Timer");
                    GUI.this.setBorder(stat, "");
                    this.setFocusable(false);
                    this.setFocusable(false);
                }
            }

            public class West extends JPanel {

                private static final long serialVersionUID = 1L;

                /**
                 * Hercules Surprise Label
                 */
                private JLabel herc = new JLabel();

                /**
                 * Zeus Surprise Label
                 */
                private JLabel zeus = new JLabel();

                /**
                 * ice Surprise Label
                 */
                private JLabel cold = new JLabel();

                /**
                 * rock Surprise Label
                 */
                private JLabel rock = new JLabel();

                /**
                 * hades Surprise Label
                 */
                private JLabel fire = new JLabel();

                /**
                 * hades Surprise Label
                 */
                private JLabel slow = new JLabel();

                private West() {
                    super();
                    this.setDoubleBuffered(true);
                    GUI.this.setBorder(this, "Skills");
                    setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.weightx = 0.5;
                    c.weighty = 0.5;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(zeus, c);
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(herc, c);
                    c.gridx = 0;
                    c.gridy = 2;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(cold, c);
                    c.gridx = 0;
                    c.gridy = 3;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(fire, c);
                    c.gridx = 0;
                    c.gridy = 4;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(rock, c);
                    c.gridx = 0;
                    c.gridy = 5;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    this.add(slow, c);
                    GUI.this.setBorder(herc, "");
                    GUI.this.setBorder(zeus, "");
                    GUI.this.setBorder(rock, "");
                    GUI.this.setBorder(fire, "");
                    GUI.this.setBorder(cold, "");
                    GUI.this.setBorder(slow, "");
                    fire.setIcon(getImage(imP.path + "surprise_fire.gif"));
                    herc.setIcon(getImage(imP.path + "surprise_hercules.gif"));
                    cold.setIcon(getImage(imP.path + "surprise_ice.gif"));
                    slow.setIcon(getImage(imP.path + "surprise_motion.gif"));
                    rock.setIcon(getImage(imP.path + "surprise_rock.gif"));
                    zeus.setIcon(getImage(imP.path + "surprise_zeus.gif"));
                    herc.setEnabled(false);
                    zeus.setEnabled(false);
                    fire.setEnabled(false);
                    rock.setEnabled(false);
                    cold.setEnabled(false);
                    slow.setEnabled(false);
                    herc.setBackground(Color.WHITE);
                    zeus.setBackground(Color.WHITE);
                    fire.setBackground(Color.WHITE);
                    rock.setBackground(Color.WHITE);
                    setFocusable(false);
                    zeus.setFocusable(false);
                    herc.setFocusable(false);
                    fire.setFocusable(false);
                }
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            String key = "";
            switch(e.getKeyCode()) {
                case 10:
                    key = "Enter";
                    break;
                case 27:
                    key = "Escape";
                    break;
                case 37:
                    key = "Left";
                    break;
                case 38:
                    key = "Up";
                    break;
                case 39:
                    key = "Right";
                    break;
                case 40:
                    key = "Down";
                    break;
                case 32:
                    key = "Space";
                    break;
                default:
                    return;
            }
            if (target) {
                sendToServer('2', key);
                target = false;
            } else if (key.equals("Space")) {
                target = true;
            } else if (key.equals("Enter")) {
                if (GUI.this.areas.na.send.getText().matches("[ ]*")) {
                    String miau = "Every time a blank lines is send over the NET a little kitten dies.";
                    sendToServer('3', "<" + client[2] + "> " + miau);
                    this.requestFocus();
                } else {
                    sendToServer('3', "<" + client[2] + "> " + gui.areas.na.send.getText());
                    gui.areas.na.send.setText("");
                    this.requestFocus();
                }
            } else if (key.equals("Escape")) {
                reallyquit();
            } else if (key != "") {
                sendToServer('1', key);
                this.requestFocus();
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if (delta > 0) {
                delta--;
                return;
            }
            if (pos[0] == 0 && pos[1] == 0) {
                pos[0] = e.getX();
                pos[1] = e.getY();
            } else {
                int dx = e.getX() - pos[0];
                int dy = e.getY() - pos[1];
                String key = "";
                if (dy == 0) {
                    key = (dx > 0) ? "Right" : "Left";
                }
                if (dx == 0) {
                    key = (dy > 0) ? "Down" : "Up";
                }
                if (target) {
                    sendToServer('2', key);
                    target = false;
                    return;
                }
                sendToServer('1', key);
                pos[0] = 0;
                pos[1] = 0;
            }
            delta = 1;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            target = true;
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals("Connect")) {
                UserLogin();
            }
            if (cmd.equals("Disconnect")) {
                net.stopit();
            }
            if (cmd.equals("Screenshot")) {
                screenShot();
            }
            if (cmd.equals("FrameShot")) {
                frameShot(gui);
            }
            if (cmd.equals("Quit")) {
                reallyquit();
            }
            if (cmd.equals("Help")) {
            }
            if (cmd.equals("BugReport")) {
            }
            if (cmd.equals("About")) {
            }
        }
    }

    /**
         * 
         */
    private void screenShot() {
        try {
            BufferedImage screencapture = null;
            screencapture = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            JFileChooser chooser2 = new JFileChooser(".");
            File deffile = new File("screenshot.jpg");
            chooser2.setFileFilter(new FileFilter("jpg"));
            chooser2.setSelectedFile(deffile);
            int result2 = chooser2.showSaveDialog(gui);
            String selecfile = null;
            if (result2 == JFileChooser.APPROVE_OPTION) {
                selecfile = chooser2.getSelectedFile().getPath();
                File file = new File(selecfile);
                ImageIO.write(screencapture, "jpg", file);
                JOptionPane.showMessageDialog(null, "Screenimage saved as " + file.getName(), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Error: Could not Save Screenimage", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
         * 
         * @param f
         */
    private void frameShot(JFrame f) {
        try {
            BufferedImage screencapture = null;
            screencapture = new Robot().createScreenCapture(new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight()));
            JFileChooser chooser2 = new JFileChooser(".");
            File deffile = new File("frameshot.jpg");
            chooser2.setSelectedFile(deffile);
            chooser2.setFileFilter(new FileFilter("jpg"));
            int result2 = chooser2.showSaveDialog(gui);
            String selecfile = null;
            if (result2 == JFileChooser.APPROVE_OPTION) {
                selecfile = chooser2.getSelectedFile().getPath();
                File file = new File(selecfile);
                ImageIO.write(screencapture, "jpg", file);
                JOptionPane.showMessageDialog(null, "Frameimage saved as " + file.getName(), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Error: Could not Save Frameimage", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
         * Generates the Window,Panel and adds KeyListener
         */
    public CRun() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        gui = new GUI();
    }

    /**
         * Gets ImageIcons
         * 
         * @param path
         * @return img
         */
    private ImageIcon getImage(String path) {
        ImageIcon img = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(path)));
        return img;
    }

    /**
         * Sets the JFrame visible, on the Fist connection with the Server
         */
    private boolean visible;

    /***********************************************************************
         * Playerid
         */
    private String id = "";

    /**
         * Sets data according to the command
         * 
         * @param stream
         */
    protected void recieveFromServer(String stream) {
        try {
            String[] split = stream.split("-");
            if (!gui.areas.na.points.getText().equals(split[2])) {
                sound.playWav("sounds/powerUp.wav", false);
            }
            gui.areas.na.pow.setValue(Integer.parseInt(split[1]));
            gui.areas.na.points.setText(split[2]);
            char command = split[3].charAt(0);
            switch(command) {
                case '1':
                    if (!visible) {
                        this.gui.setVisible(true);
                    }
                    String[] split2 = split[4].split("/");
                    int fieldh = Integer.parseInt(split2[2]);
                    int fieldw = Integer.parseInt(split2[3]);
                    int k = 0;
                    field = new char[fieldh][fieldw];
                    for (int i = 0; i < fieldh; i++) {
                        for (int j = 0; j < fieldw; j++) {
                            field[i][j] = split2[0].charAt(k);
                            k++;
                        }
                    }
                    gui.areas.wa.zeus.setEnabled(split[5].equals("true") ? true : false);
                    gui.areas.wa.herc.setEnabled(split[6].equals("true") ? true : false);
                    gui.areas.wa.fire.setEnabled(split[7].equals("true") ? true : false);
                    gui.areas.wa.rock.setEnabled(split[8].equals("true") ? true : false);
                    gui.areas.wa.cold.setEnabled(split[9].equals("true") ? true : false);
                    gui.areas.wa.slow.setEnabled(split[10].equals("true") ? true : false);
                    gui.areas.ea.rkey.setEnabled(split[11].equals("true") ? true : false);
                    gui.areas.ea.bkey.setEnabled(split[12].equals("true") ? true : false);
                    gui.areas.ea.gkey.setEnabled(split[13].equals("true") ? true : false);
                    gui.areas.ea.ykey.setEnabled(split[14].equals("true") ? true : false);
                    gui.areas.na.info.setText(null);
                    for (int i = 17; i < split.length - 1; i = i + 4) {
                        gui.areas.na.info.setText(gui.areas.na.info.getText() + split[i] + " Power: " + split[i + 1] + " Points: " + split[i + 2] + "\n");
                    }
                    gui.areas.na.level.setText(split[split.length - 1]);
                    drawlvl();
                    break;
                case '3':
                    String txt = gui.areas.na.msg.getText();
                    gui.areas.na.msg.setText(split[4] + "\n" + txt);
                    break;
                case '4':
                    if (!visible) {
                        client[3] = split[0];
                        id = client[3];
                        String micon = "";
                        switch(Integer.parseInt(client[3]) + 1) {
                            case 1:
                                micon = imP.path + "obj_player1_static.gif";
                                break;
                            case 2:
                                micon = imP.path + "obj_player2_static.gif";
                                break;
                            case 3:
                                micon = imP.path + "obj_player3_static.gif";
                                break;
                            case 4:
                                micon = imP.path + "obj_player4_static.gif";
                                break;
                        }
                        gui.areas.na.name.setIcon(getImage(micon));
                        Image image = Toolkit.getDefaultToolkit().getImage(micon.substring(1));
                        gui.setIconImage(image);
                        sound.loopMp3(songPath, false);
                        System.out.println("Playing: " + songPath);
                        gui.areas.sa.timer.setStringPainted(true);
                        gui.areas.na.send.addKeyListener(gui);
                        gui.areas.ca.addMouseMotionListener(gui);
                        gui.areas.ca.addMouseListener(gui);
                        gui.addKeyListener(gui);
                        gui.requestFocus();
                        visible = true;
                    }
                    int time = Integer.parseInt(split[4]);
                    gui.areas.sa.timer.setValue(time);
                    gui.areas.sa.timer.setString(split[4] + " sec");
                    break;
                case '6':
                    sound.stop();
                    sound.playMp3("sounds/cheering.wav", false);
                    showHS(split[4]);
                    gui.setVisible(false);
                    sound.stop();
                    System.exit(0);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }
    }

    /**
         * Draws the Level onto the Panel Sets JFrame Size
         */
    private void drawlvl() {
        try {
            gui.areas.ca.setLevel(field, getPlayerPos(), client[3]);
            gui.repaint();
        } catch (Exception e) {
            new CException(e, false, "Error: Trying to redraw Playground");
        }
    }

    private int[] getPlayerPos() {
        int w = field[0].length;
        int h = field.length;
        if (id.length() == 0) {
            int[] pos = { 0, 0 };
            return pos;
        }
        int x1 = 0;
        int y1 = 0;
        for (int i = 0; i < h; i++) for (int j = 0; j < w; j++) if (field[i][j] == (char) (id.charAt(0) + 1) || field[i][j] == (char) (id.charAt(0) + 5)) {
            x1 = j;
            y1 = i;
        }
        int[] pos = { x1, y1 };
        return pos;
    }

    /**
         * The Login Process.
         */
    private String songPath = "";

    private void UserLogin() {
        sound.loopMp3("sounds/lullaby.mp3", false);
        JOptionPane.showMessageDialog(gui, "Choose your Favorit MP3", "Mp3-Chooser", JOptionPane.INFORMATION_MESSAGE, null);
        JFileChooser chooser = new JFileChooser(".");
        chooser.addChoosableFileFilter(new FileFilter("mp3"));
        int result = chooser.showDialog(gui, null);
        if (result == JFileChooser.APPROVE_OPTION) songPath = chooser.getSelectedFile().getPath();
        for (; ; ) {
            client[0] = (String) JOptionPane.showInputDialog(gui, "What is the ServerURL/IP.", null, JOptionPane.QUESTION_MESSAGE, null, null, "localhost");
            if (client[0] == null) {
                return;
            }
            client[1] = (String) JOptionPane.showInputDialog(gui, "And which Port?", null, JOptionPane.QUESTION_MESSAGE, null, null, "4454");
            if (client[1] == null) {
                return;
            }
            client[2] = "";
            while (client[2].trim().equals("") || (client[2].length() >= 10 && client[2].length() > 0)) {
                client[2] = (String) JOptionPane.showInputDialog(gui, "Please enter your Username.", null, JOptionPane.QUESTION_MESSAGE, null, null, "Your Name");
                if (client[2] == null) {
                    return;
                }
                if (client[2].length() == 0) {
                    JOptionPane.showMessageDialog(gui, "The Username is to short. At Least 1 Character is required" + "\n Please try a again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                if (client[2].length() >= 10) {
                    JOptionPane.showMessageDialog(gui, "The Username is to long. At Most 10 Character are permitted" + "\n Please try a again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            gui.areas.na.name.setText(client[2]);
            String url = client[0];
            int port = Integer.parseInt(client[1]);
            net = new CNetwork(this, url, port, stat);
            net.start();
            sendToServer('0', client[2]);
            break;
        }
        sound.stop();
    }

    /**
         * Ask the User if he wants to exit the Programm
         */
    private void reallyquit() {
        Object[] options = { "Quit", "BACK" };
        sound.loopMp3("sounds/boo.wav", false);
        if (JOptionPane.showOptionDialog(gui, "Really Quit?", "Question", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]) == 0) {
            System.exit(0);
        }
        sound.stop();
    }

    /**
         * Formates and shows the HighScore String
         * 
         * @param list
         */
    private void showHS(String list) {
        String split = "/";
        String split2 = " - ";
        String output = "";
        String[] temp = list.split(split);
        for (int i = 0; i < temp.length && i < 20; i += 2) {
            output = output + temp[i] + split2 + temp[i + 1] + "\n\n";
        }
        JOptionPane.showMessageDialog(null, "Points - Player\n" + "----------------\n" + output, "H i g h S c o r e", JOptionPane.INFORMATION_MESSAGE);
        sound.stop();
    }

    /**
         * Sends commands and actions to the Server
         */
    private void sendToServer(char command, String action) {
        net.send(command + action);
    }

    private int delta = 1;

    private int[] pos = { 0, 0 };
}
