import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.io.*;

public class GameFrame extends JFrame implements ActionListener {

    private static Properties gameProperties;

    private static Properties skinProperties;

    private LinkedList<PathNode> heads;

    private LinkedList<PathNode> tempHeads;

    private LinkedList<PointDir> startPoints;

    private char[][] map;

    private char[][] tempMap;

    private boolean[][] traveled;

    private static boolean loadIsGood = true;

    private int width, height;

    private int tWidth, tHeight;

    private int ends;

    private int tEnds;

    private final int GOOD = 0, NOFILE = 1, NOEND = 2, BADDIM = 3, FUNKY = 4;

    private TowerDefense td;

    public static void main(String[] args) {
        initProperties();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    public static void initProperties() {
        gameProperties = new Properties();
        skinProperties = new Properties();
        try {
            gameProperties.load(new FileInputStream("game.config"));
            loadSkinProperties(getGameProperty("skin.active"), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "The game settings file (game.config) is gone. Without this, you are screwed. Please reinstall.", "Error", JOptionPane.ERROR_MESSAGE);
            loadIsGood = false;
        }
    }

    public static void createAndShowGUI() {
        GameFrame frame = new GameFrame("Tower Defense");
    }

    public GameFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        requestFocus();
        setResizable(false);
        setBackground(new Color(124, 64, 0));
        setSize(770, 650);
        if (loadIsGood) {
            JMenuBar bar = new JMenuBar();
            JMenu menu = new JMenu("Game");
            bar.add(menu);
            JMenuItem menuItem = new JMenuItem("New Game", KeyEvent.VK_N);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
            menuItem.addActionListener(this);
            menu.add(menuItem);
            menuItem = new JMenuItem("Load Map", KeyEvent.VK_L);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
            menuItem.addActionListener(this);
            menu.add(menuItem);
            menuItem = new JMenuItem("Load Skin", KeyEvent.VK_S);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
            menuItem.addActionListener(this);
            menu.add(menuItem);
            menu.addSeparator();
            menuItem = new JMenuItem("Quit", KeyEvent.VK_F4);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
            menuItem.addActionListener(this);
            menu.add(menuItem);
            setJMenuBar(bar);
            if (loadMap(true, getGameProperty("map.active"))) startNewGame(); else blueScreenOfDeath();
        } else {
            blueScreenOfDeath();
        }
    }

    private void blueScreenOfDeath() {
        Container con = getContentPane();
        con.setBackground(new Color(0, 0, 255));
        JPanel blah = new JPanel();
        JLabel title = new JLabel("jDefense");
        title.setForeground(new Color(0, 0, 255));
        blah.setBackground(new Color(170, 170, 170));
        blah.setBounds(340, 100, 60, 28);
        blah.add(title);
        JPanel blah2 = new JPanel();
        JLabel message = new JLabel("This program has encountered a fatal error n00b.");
        message.setForeground(new Color(170, 170, 170));
        blah2.setBounds(500, 200, 100, 100);
        blah2.setOpaque(false);
        blah2.add(message);
        con.add(blah);
        con.add(blah2);
        con.add(message);
        con.validate();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("New Game")) {
            if (heads != null) startNewGame();
        }
        if (e.getActionCommand().equals("Load Skin")) {
            String temp = getFile(0);
            if (temp != null) {
                if (loadSkinProperties(temp, false)) {
                    Images.reset();
                    if (td != null) td.renderMap();
                }
            }
        }
        if (e.getActionCommand().equals("Load Map")) {
            String temp = getFile(1);
            if (temp != null) {
                loadMap(false, temp);
            }
        }
        if (e.getActionCommand().equals("Quit")) System.exit(0);
    }

    public void startNewGame() {
        td = new TowerDefense(map, heads, width, height, ends);
        setContentPane(td);
        validate();
    }

    public boolean loadMap(boolean first, String file) {
        int code = 0;
        if (first) code = parseMap(0, file); else code = parseMap(10, file);
        if (code == 0) {
            setGameProperty("map.active", file);
            heads = tempHeads;
            map = tempMap;
            height = tHeight;
            width = tWidth;
            ends = tEnds;
            startNewGame();
            return true;
        } else switch(code) {
            case NOFILE:
                JOptionPane.showMessageDialog(null, "The map file cannot be loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case NOEND:
                JOptionPane.showMessageDialog(null, "The map you loaded has discontinuities in the path.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case BADDIM:
                JOptionPane.showMessageDialog(null, "The map you loaded has erroneous dimensions.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case FUNKY:
                JOptionPane.showMessageDialog(null, "Something funky happened and you cannot load this.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            default:
                System.out.println(code);
        }
        return false;
    }

    public String getFile(int type) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter;
        if (type == 0) {
            chooser.setCurrentDirectory(new File(gameProperties.getProperty("skin.folder") + "\\"));
            filter = new FileNameExtensionFilter("config files", "config");
        } else {
            chooser.setCurrentDirectory(new File(gameProperties.getProperty("map.folder") + "\\"));
            filter = new FileNameExtensionFilter("map files", "map");
        }
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getCurrentDirectory().toString() + "\\" + chooser.getSelectedFile().getName();
        }
        return null;
    }

    private int parseMap(int code, String file) {
        FileReader almostIn = null;
        try {
            almostIn = new FileReader(file);
        } catch (IOException e) {
            if (code == 0) {
                setGameProperty("map.active", "maps\\default.map");
                try {
                    almostIn = new FileReader(getGameProperty("map.active"));
                } catch (IOException ex) {
                    return NOFILE;
                }
            } else {
                return NOFILE;
            }
        }
        code = 0;
        BufferedReader in = new BufferedReader(almostIn);
        try {
            tWidth = Integer.parseInt(in.readLine());
            tHeight = Integer.parseInt(in.readLine());
            tEnds = 0;
            tempMap = new char[tWidth][tHeight];
            tempHeads = new LinkedList<PathNode>();
            startPoints = new LinkedList<PointDir>();
            StringTokenizer st;
            char tempChar;
            for (int y = 0; y < tHeight; y++) {
                st = new StringTokenizer(in.readLine());
                for (int x = 0; x < tWidth; x++) {
                    tempChar = st.nextToken().charAt(0);
                    switch(tempChar) {
                        case 'S':
                            tempMap[x][y] = 'S';
                            startPoints.add(new PointDir(x, y, 5));
                            break;
                        default:
                            tempMap[x][y] = tempChar;
                            break;
                    }
                }
            }
            code = 0;
            Iterator<PointDir> iter = startPoints.iterator();
            PointDir curPoint;
            for (int x = 0; x < startPoints.size() && code == 0; x++) {
                curPoint = iter.next();
                PathNode start = new PathNode(new PointDir(curPoint.x, curPoint.y, PointDir.WEST));
                tempHeads.add(start);
                boolean good = false;
                if (curPoint.x > 0 && tempMap[curPoint.x - 1][curPoint.y] != '~' && tempMap[curPoint.x - 1][curPoint.y] != 'S') {
                    traveled = new boolean[tWidth][tHeight];
                    PathNode pn = new PathNode(new PointDir(curPoint.x, curPoint.y, PointDir.WEST));
                    start.setNext(pn);
                    code = findPath(curPoint.x - 1, curPoint.y, curPoint.x, curPoint.y, pn);
                    good = true;
                }
                if (curPoint.x < tWidth - 1 && tempMap[curPoint.x + 1][curPoint.y] != '~' && tempMap[curPoint.x + 1][curPoint.y] != 'S') {
                    traveled = new boolean[tWidth][tHeight];
                    PathNode pn = new PathNode(new PointDir(curPoint.x, curPoint.y, PointDir.EAST));
                    start.setNext(pn);
                    code = findPath(curPoint.x + 1, curPoint.y, curPoint.x, curPoint.y, pn);
                    good = true;
                }
                if (curPoint.y > 0 && tempMap[curPoint.x][curPoint.y - 1] != '~' && tempMap[curPoint.x][curPoint.y - 1] != 'S') {
                    traveled = new boolean[tWidth][tHeight];
                    PathNode pn = new PathNode(new PointDir(curPoint.x, curPoint.y, PointDir.NORTH));
                    start.setNext(pn);
                    code = findPath(curPoint.x, curPoint.y - 1, curPoint.x, curPoint.y, pn);
                    good = true;
                }
                if (curPoint.y < tHeight - 1 && tempMap[curPoint.x][curPoint.y + 1] != '~' && tempMap[curPoint.x][curPoint.y + 1] != 'S') {
                    traveled = new boolean[tWidth][tHeight];
                    PathNode pn = new PathNode(new PointDir(curPoint.x, curPoint.y, PointDir.SOUTH));
                    start.setNext(pn);
                    code = findPath(curPoint.x, curPoint.y + 1, curPoint.x, curPoint.y, pn);
                    good = true;
                }
                if (!good) code = NOEND;
            }
        } catch (NullPointerException e) {
            code = BADDIM;
            e.printStackTrace();
        } catch (NumberFormatException e) {
            code = BADDIM;
            e.printStackTrace();
        } catch (Exception e) {
            code = FUNKY;
        }
        return code;
    }

    private int findPath(int x, int y, int previousX, int previousY, PathNode pNode) {
        if (x < 0 || x >= tWidth || y < 0 || y >= tHeight || tempMap[x][y] == '~') return NOEND;
        if (tempMap[x][y] == 'E') {
            pNode.setNext(new PathNode(new PointDir(x, y, PointDir.END)));
            tEnds++;
            return GOOD;
        } else if (tempMap[x][y] == 'Y') {
            PointDir test = new PointDir(x, y, 0);
            Iterator<PathNode> iter = tempHeads.iterator();
            for (int z = 0; z < tempHeads.size(); z++) {
                PathNode path = iter.next();
                PathNode next = path.contains(test);
                if (next != null) {
                    pNode.setNext(next);
                    return GOOD;
                }
            }
            int code = NOEND;
            boolean first = true;
            if (x > 0 && tempMap[x - 1][y] != '~' && !traveled[x - 1][y] && x - 1 != previousX) {
                PathNode pn = new PathNode(new PointDir(x, y, PointDir.WEST));
                traveled[x - 1][y] = true;
                if (findPath(x - 1, y, x, y, pn) == GOOD) {
                    pNode.setNext(pn);
                    code = GOOD;
                }
            }
            if (x < tWidth - 1 && tempMap[x + 1][y] != '~' && !traveled[x + 1][y] && x + 1 != previousX) {
                PathNode pn = new PathNode(new PointDir(x, y, PointDir.EAST));
                traveled[x + 1][y] = true;
                if (findPath(x + 1, y, x, y, pn) == GOOD) {
                    pNode.setNext(pn);
                    code = GOOD;
                }
            }
            if (y > 0 && tempMap[x][y - 1] != '~' && !traveled[x][y - 1] && y - 1 != previousY) {
                PathNode pn = new PathNode(new PointDir(x, y, PointDir.NORTH));
                traveled[x][y - 1] = true;
                if (findPath(x, y - 1, x, y, pn) == GOOD) {
                    pNode.setNext(pn);
                    code = GOOD;
                }
            }
            if (y < tHeight - 1 && tempMap[x][y + 1] != '~' && !traveled[x][y + 1] && y + 1 != previousY) {
                PathNode pn = new PathNode(new PointDir(x, y, PointDir.SOUTH));
                traveled[x][y + 1] = true;
                if (findPath(x, y + 1, x, y, pn) == GOOD) {
                    pNode.setNext(pn);
                    code = GOOD;
                }
            }
            if (code == NOEND) traveled[x - (x - previousX)][y - (y - previousY)] = false;
            return code;
        } else if (tempMap[x][y] == '=' || tempMap[x][y] == '|' || tempMap[x][y] == '#') return findPath(x + (x - previousX), y + (y - previousY), x, y, pNode); else if (tempMap[x][y] == 'J') {
            if (previousX == x - 1) {
                return findPath(x, y - 1, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.NORTH))));
            }
            if (previousY == y - 1) {
                return findPath(x - 1, y, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.WEST))));
            }
            return findPath(x + (x - previousX), y + (y - previousY), x, y, pNode);
        }
        if (tempMap[x][y] == 'L') {
            if (previousX == x + 1) {
                return findPath(x, y - 1, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.NORTH))));
            }
            if (previousY == y - 1) {
                return findPath(x + 1, y, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.EAST))));
            }
            return findPath(x + (x - previousX), y + (y - previousY), x, y, pNode);
        }
        if (tempMap[x][y] == '7') {
            if (previousX == x - 1) {
                return findPath(x, y + 1, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.SOUTH))));
            }
            if (previousY == y + 1) {
                return findPath(x - 1, y, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.WEST))));
            }
            return findPath(x + (x - previousX), y + (y - previousY), x, y, pNode);
        }
        if (tempMap[x][y] == 'r') {
            if (previousX == x + 1) {
                return findPath(x, y + 1, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.SOUTH))));
            }
            if (previousY == y + 1) {
                return findPath(x + 1, y, x, y, pNode.setNext(new PathNode(new PointDir(x, y, PointDir.EAST))));
            }
            return findPath(x + (x - previousX), y + (y - previousY), x, y, pNode);
        }
        return NOEND;
    }

    public static String getSkinProperty(String propertyName) {
        return skinProperties.getProperty(propertyName);
    }

    public static String getGameProperty(String propertyName) {
        return gameProperties.getProperty(propertyName);
    }

    public static int getIntGameProperty(String propertyName) {
        return Integer.parseInt(gameProperties.getProperty(propertyName));
    }

    public static String getSkinPath() {
        return getGameProperty("skin.folder");
    }

    public static void setGameProperty(String propertyName, String propertyValue) {
        gameProperties.setProperty(propertyName, propertyValue);
        try {
            gameProperties.store(new FileOutputStream("game.config"), null);
        } catch (IOException e) {
            System.out.println("Can't write to game.config! Make sure it exists");
        }
    }

    public static boolean loadSkinProperties(String path, boolean firstRun) {
        try {
            skinProperties.load(new FileInputStream(path));
            setGameProperty("skin.active", path);
            int temp = 0;
            while (path.indexOf("\\", temp + 1) >= 0) {
                temp = path.indexOf("\\", temp + 1);
            }
            setGameProperty("skin.folder", path.substring(0, temp) + "\\");
            return true;
        } catch (IOException e) {
            if (firstRun) {
                JOptionPane.showMessageDialog(null, "The skin settings file is gone. Using default", "Error", JOptionPane.ERROR_MESSAGE);
                setGameProperty("skin.active", "skins\\space\\skin.config");
                setGameProperty("skin.folder", "skins\\");
                try {
                    skinProperties.load(new FileInputStream("skins\\space\\skin.config"));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "The default skin settings file is gone. You are screwed. Reinstall! X|", "Error", JOptionPane.ERROR_MESSAGE);
                    loadIsGood = false;
                }
            } else JOptionPane.showMessageDialog(null, "This skin cannot be loaded", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}
