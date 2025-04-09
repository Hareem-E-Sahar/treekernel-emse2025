package org.fableStudios.tetris;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.fableStudios.tetris.BaseTetrisPiece.TetrisShape;
import org.fableStudios.tetris.renderables.*;
import org.fableStudios.tetris.renderables.Menu;
import org.fableStudios.tetris.renderables.Menu.Key;
import org.fableStudios.tetris.renderables.Menu.State;
import org.ini4j.Ini;
import java.util.Timer;

@SuppressWarnings("serial")
public class TetrisPanel extends JPanel {

    private static final long EXPIRY_TIME = 1257379200000L;

    private static final String VERSION_STRING = "SUPER TETRIS V1.0.0.2_TETRIS_091106-0949_RELEASE";

    private static final int PIECES_PER_LEVEL = 25;

    private static final long serialVersionUID = -2381053236660886871L;

    private final KeyStroke vkLeftStroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false);

    private final KeyStroke vkRightStroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false);

    private final KeyStroke vkUpStroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false);

    private final KeyStroke vkDownStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false);

    private final KeyStroke vkSpaceStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false);

    private final KeyStroke vkHStroke = KeyStroke.getKeyStroke(KeyEvent.VK_H, 0, false);

    private final KeyStroke vkPStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false);

    private final KeyStroke vkPrintScreenStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false);

    private final Action vkLeftAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Renderables.menu.getState() == State.Setup) Renderables.menu.keyPressed(Key.Left); else tryMove(Settings.currPiece, Settings.curX - 1, Settings.curY);
        }
    };

    private final Action vkRightAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Renderables.menu.getState() == State.Setup) Renderables.menu.keyPressed(Key.Right); else tryMove(Settings.currPiece, Settings.curX + 1, Settings.curY);
        }
    };

    private final Action vkUpAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Renderables.menu.getState() == State.Setup) Renderables.menu.keyPressed(Key.Up); else tryMove(Settings.currPiece.rotateLeft(), Settings.curX, Settings.curY);
        }
    };

    private final Action vkDownAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Renderables.menu.getState() == State.Setup) Renderables.menu.keyPressed(Key.Down); else if (GameSettings.alternateKeys) {
                tryMove(Settings.currPiece.rotateRight(), Settings.curX, Settings.curY);
            } else if (Renderables.menu.getState() != State.Highscore) oneLineDown(false);
        }
    };

    private final Action vkSpaceAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!Renderables.menu.inGame()) {
                start();
                return;
            }
            if (Settings.isPaused) {
                pause();
                return;
            }
            if (Updatables.isBomb) return;
            dropDown();
        }
    };

    private final Action vkHAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            tryHold();
        }
    };

    private final Action vkPAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!Settings.isPaused) pause();
        }
    };

    private final Action vkPrintScreenAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                ImageIO.write(new Robot().createScreenCapture(Main.instance.getBounds()), "png", new File("ss_" + new Date().getTime() + ".png"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private static final String VKLEFT = "vkLeft";

    private static final String VKRIGHT = "vkRight";

    private static final String VKUP = "vkUp";

    private static final String VKDOWN = "vkDown";

    private static final String VKSPACE = "vkSpace";

    private static final String VKH = "vkH";

    private static final String VKP = "vkP";

    private static final String VKPRINTSCREEN = "vkPrintScreen";

    private static class Settings {

        public static boolean isPaused = false;

        public static int boardWidth = 10;

        public static int boardHeight = 22;

        public static int numPiecesDropped = 0;

        public static TetrisShape[] board;

        public static BaseTetrisPiece currPiece;

        public static TetrisPiece nextPiece;

        public static TetrisPiece holdPiece;

        public static int holdsThisTurn = 0;

        public static int curX;

        public static int curY;

        public static int ghostY;

        public static int nextLevel;
    }

    private static class Renderables {

        public static volatile ArrayList<ScoreText> scores;

        public static volatile ArrayList<LineToRemove> linesToRemove;

        public static volatile ArrayList<BombAnimation> bombAnimation;

        public static volatile TextExplode tetris;

        public static volatile TextExplode cTetris;

        public static volatile GhostOutline ghostOutline;

        public static volatile Menu menu;

        public static volatile ArrayList<Point> background;

        static {
            scores = new ArrayList<ScoreText>();
            linesToRemove = new ArrayList<LineToRemove>();
            bombAnimation = new ArrayList<BombAnimation>();
            background = new ArrayList<Point>();
        }
    }

    private static class Updatables {

        public static volatile boolean[] linesNoRender;

        public static volatile boolean isBomb;

        public static volatile boolean isNewPiece;

        public static volatile int tickTime = 0;

        public static volatile int timeoutTime = 0;

        public static volatile boolean doTick;

        public static volatile int nextRotation = 3;

        public static volatile int nextShapeChange = 3;

        public static volatile int pieceDropHeight = 0;

        public static boolean isWaitingAfterPiece = false;

        public static boolean isWaitingAfterLine = false;

        public static Point bgLocation;
    }

    public static class GlobalSettings {

        public static EnumSet<LevelMode> levelModes;

        public static DecimalFormat formatter;

        public static boolean inDevelopment = false;

        /** DEV MODE - DO NOT HAVE AS TRUE WHEN EXPORTING **/
        public static boolean isTester = true;

        /** TESTING MORE - DO NOT HAVE AS TRUE FOR PRODUCTION **/
        public static Font font;

        public static FontMetrics fontMetrics;

        public static boolean tetris = false;

        public static boolean completeTetris = false;

        public static Random generator = new Random();

        public static final int updateTickTime = 15;

        public static final int xyOffset = 20;

        public static SmoothIncrement score;

        public static SmoothIncrement combo;

        public static int level = 1;

        public static int numLinesDropped = 0;

        public static int[] numCombos;

        public static int time;
    }

    public static class GameSettings {

        public static boolean alternateKeys = false;

        public static boolean movingBackground = true;

        public static GameMode gameMode = GameMode.Normal;

        public static int gmTimeout = 40;

        public static int mTimeout = 30;

        public static int eTimeout = 25;

        public static int dTimeout = 20;
    }

    public static class Images {

        public static BufferedImage modes;

        public static BufferedImage pieces;

        public static BufferedImage tetris;

        public static BufferedImage cTetris;

        public static BufferedImage background;
    }

    private Timer timerUpdate;

    private int timeoutTime() {
        int time;
        switch(GameSettings.gameMode) {
            case GrandMaster:
                time = 100 - GlobalSettings.level * GameSettings.gmTimeout;
                if (time < 20) time = 20;
                break;
            case Master:
                time = 400 - GlobalSettings.level * GameSettings.mTimeout;
                if (time < 40) time = 40;
                break;
            case Expert:
                time = 600 - GlobalSettings.level * GameSettings.eTimeout;
                if (time < 60) time = 60;
                break;
            default:
                time = 820 - GlobalSettings.level * GameSettings.dTimeout;
                if (time < 100) time = 100;
                break;
        }
        return time;
    }

    private TetrisShape shapeAt(int x, int y) {
        return Settings.board[y * Settings.boardWidth + x];
    }

    private void setShapeAt(int x, int y, TetrisShape shape) {
        Settings.board[y * Settings.boardWidth + x] = shape;
    }

    public static int squareWidth() {
        return 24;
    }

    public static int squareHeight() {
        return 24;
    }

    public int getBoardWidth() {
        return Settings.boardWidth;
    }

    public int getBoardHeight() {
        return Settings.boardHeight;
    }

    public TetrisPanel() {
        ;
        Renderables.menu = new Menu();
        if (Renderables.menu.getState() != State.Expired) {
            GlobalHotkeyManager hotkeyManager = GlobalHotkeyManager.getInstance();
            hotkeyManager.getInputMap().put(vkLeftStroke, VKLEFT);
            hotkeyManager.getInputMap().put(vkRightStroke, VKRIGHT);
            hotkeyManager.getInputMap().put(vkUpStroke, VKUP);
            hotkeyManager.getInputMap().put(vkDownStroke, VKDOWN);
            hotkeyManager.getInputMap().put(vkSpaceStroke, VKSPACE);
            hotkeyManager.getInputMap().put(vkHStroke, VKH);
            hotkeyManager.getInputMap().put(vkPStroke, VKP);
            hotkeyManager.getInputMap().put(vkPrintScreenStroke, VKPRINTSCREEN);
            hotkeyManager.getActionMap().put(VKLEFT, vkLeftAction);
            hotkeyManager.getActionMap().put(VKRIGHT, vkRightAction);
            hotkeyManager.getActionMap().put(VKUP, vkUpAction);
            hotkeyManager.getActionMap().put(VKDOWN, vkDownAction);
            hotkeyManager.getActionMap().put(VKSPACE, vkSpaceAction);
            hotkeyManager.getActionMap().put(VKH, vkHAction);
            hotkeyManager.getActionMap().put(VKP, vkPAction);
            hotkeyManager.getActionMap().put(VKPRINTSCREEN, vkPrintScreenAction);
        }
        Settings.board = new TetrisShape[Settings.boardWidth * Settings.boardHeight];
        for (int i = 0; i < Settings.board.length; i++) Settings.board[i] = TetrisShape.NoShape;
        this.setDoubleBuffered(true);
        GlobalSettings.score = new SmoothIncrement();
        GlobalSettings.combo = new SmoothIncrement();
        Renderables.ghostOutline = new GhostOutline();
        URL image;
        try {
            image = TetrisPanel.class.getClassLoader().getResource("org/fableStudios/tetris/graphics/modes.png");
            Images.modes = ImageIO.read(image);
            image = TetrisPanel.class.getClassLoader().getResource("org/fableStudios/tetris/graphics/pieces.png");
            Images.pieces = ImageIO.read(image);
            image = TetrisPanel.class.getClassLoader().getResource("org/fableStudios/tetris/graphics/tetris.png");
            Images.tetris = ImageIO.read(image);
            image = TetrisPanel.class.getClassLoader().getResource("org/fableStudios/tetris/graphics/complete_tetris.png");
            Images.cTetris = ImageIO.read(image);
            image = TetrisPanel.class.getClassLoader().getResource("org/fableStudios/tetris/graphics/background_tile.png");
            Images.background = ImageIO.read(image);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        InputStream is = TetrisPanel.class.getClassLoader().getResourceAsStream("org/fableStudios/tetris/data/tetris.ttf");
        try {
            GlobalSettings.font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f).deriveFont(Font.BOLD);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Font not loaded for some obscure reason.");
            GlobalSettings.font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File fIni = new File("conf.ini");
        if (!fIni.exists()) {
            try {
                saveIni(fIni);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadIni(fIni);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        GlobalSettings.numCombos = new int[5];
        Updatables.bgLocation = new Point();
        GlobalSettings.fontMetrics = this.getFontMetrics(GlobalSettings.font);
        Renderables.menu.init();
        timerUpdate = new Timer();
        timerUpdate.scheduleAtFixedRate(new TimerUpdateEvent(), GlobalSettings.updateTickTime, GlobalSettings.updateTickTime);
    }

    public void start() {
        if (Renderables.menu.getState() == State.Highscore) {
            Renderables.menu.setState(State.Setup);
            return;
        }
        if (Settings.isPaused) return;
        try {
            saveIni(new File("conf.ini"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Renderables.menu.setState(State.Game);
        GlobalSettings.numLinesDropped = 0;
        Settings.numPiecesDropped = 0;
        GlobalSettings.level = 1;
        Settings.nextLevel = -1;
        GlobalSettings.score.clear();
        GlobalSettings.combo.clear();
        resetCombo(true);
        GlobalSettings.formatter = new DecimalFormat("###,###");
        GlobalSettings.levelModes = EnumSet.noneOf(LevelMode.class);
        Renderables.tetris = new TextExplode(Images.tetris, this.getWidth(), this.getHeight());
        Renderables.cTetris = new TextExplode(Images.cTetris, this.getWidth(), this.getHeight());
        clearBoard();
        Renderables.scores.clear();
        Renderables.linesToRemove.clear();
        Updatables.linesNoRender = new boolean[Settings.boardHeight];
        Arrays.fill(Updatables.linesNoRender, false);
        Renderables.tetris.start();
        Settings.currPiece = new TetrisPiece();
        Settings.currPiece.setRandomShape();
        Settings.nextPiece = new TetrisPiece();
        Settings.nextPiece.setRandomShape();
        Settings.holdPiece = new TetrisPiece();
        newPiece();
        Updatables.timeoutTime = timeoutTime() / GlobalSettings.updateTickTime;
        Updatables.doTick = true;
        Updatables.pieceDropHeight = 0;
        Updatables.isWaitingAfterLine = false;
        Updatables.isWaitingAfterPiece = false;
        Updatables.isBomb = false;
        Updatables.isNewPiece = false;
        updateBackground();
    }

    private void finish() {
        Renderables.menu.setState(State.Highscore);
        Renderables.scores.clear();
        Renderables.linesToRemove.clear();
        Renderables.bombAnimation.clear();
        Renderables.ghostOutline.clear();
        Renderables.tetris.clear();
        Renderables.cTetris.clear();
    }

    public void pause() {
        if (!Renderables.menu.inGame()) return;
        Settings.isPaused = !Settings.isPaused;
        if (Settings.isPaused) Updatables.doTick = false; else Updatables.doTick = true;
    }

    private class TimerUpdateEvent extends TimerTask {

        private int bombTime = 0;

        private int pieceWait = 500;

        private double backgroundTick = 0;

        @Override
        public void run() {
            if (Renderables.menu.inGame()) {
                if (!Settings.isPaused && GameSettings.movingBackground) {
                    synchronized (Renderables.background) {
                        backgroundTick++;
                        Updatables.bgLocation.x = (int) (Math.sin(backgroundTick / 600) * 150);
                        Updatables.bgLocation.y = (int) (Math.cos(backgroundTick / 600) * 150);
                        updateBackground();
                    }
                }
                if (Updatables.doTick) {
                    GlobalSettings.time++;
                    if (Updatables.isWaitingAfterPiece) {
                        if (pieceWait > 0) pieceWait -= GlobalSettings.updateTickTime; else {
                            Updatables.isWaitingAfterPiece = false;
                            pieceWait = 500;
                        }
                    } else {
                        if (Updatables.tickTime > 0) Updatables.tickTime--; else {
                            Updatables.tickTime = Updatables.timeoutTime;
                            if (GameSettings.gameMode == GameMode.Expert) {
                                if (GlobalSettings.levelModes.contains(LevelMode.RotationChange)) {
                                    Updatables.nextRotation--;
                                    if (Updatables.nextRotation == 0) {
                                        if (!tryMove(Settings.currPiece.rotateLeft(), Settings.curX, Settings.curY)) tryMove(Settings.currPiece.rotateRight(), Settings.curX, Settings.curY);
                                    }
                                }
                                if (GlobalSettings.levelModes.contains(LevelMode.ShapeChange)) {
                                    Updatables.nextShapeChange--;
                                    if (Updatables.nextShapeChange == 0) {
                                        int attempts = 0;
                                        Settings.currPiece.setRandomShape();
                                        while (!tryMove(Settings.currPiece, Settings.curX, Settings.curY) && attempts++ < 7) Settings.currPiece.setRandomShape();
                                        Updatables.nextShapeChange = 3;
                                    }
                                }
                            }
                            oneLineDown(true);
                            Updatables.pieceDropHeight = 0;
                        }
                    }
                }
                if (!Renderables.scores.isEmpty()) {
                    ScoreText score;
                    synchronized (Renderables.scores) {
                        for (Iterator<ScoreText> it = Renderables.scores.iterator(); it.hasNext(); ) {
                            score = it.next();
                            score.update();
                            if (!score.isAlive()) it.remove();
                        }
                    }
                }
                if (!Renderables.linesToRemove.isEmpty() && !Updatables.isBomb) {
                    boolean isClear = true;
                    for (LineToRemove line : Renderables.linesToRemove) {
                        line.update();
                        if (!line.isFinished()) isClear = false;
                    }
                    if (isClear) {
                        GlobalSettings.numLinesDropped += Renderables.linesToRemove.size();
                        Renderables.linesToRemove.clear();
                        for (int i = Settings.boardHeight - 1; i >= 0; i--) {
                            boolean lineIsFull = true;
                            for (int j = 0; j < Settings.boardWidth; ++j) {
                                if (shapeAt(j, i) == TetrisShape.NoShape) {
                                    lineIsFull = false;
                                    break;
                                }
                            }
                            if (lineIsFull) {
                                for (int k = i; k < Settings.boardHeight - 1; k++) for (int j = 0; j < Settings.boardWidth; j++) setShapeAt(j, k, shapeAt(j, k + 1));
                                for (int j = 0; j < Settings.boardWidth; j++) setShapeAt(j, Settings.boardHeight - 1, TetrisShape.NoShape);
                            }
                        }
                        Arrays.fill(Updatables.linesNoRender, false);
                        Updatables.doTick = true;
                        if (Updatables.isWaitingAfterLine) {
                            Updatables.isWaitingAfterLine = false;
                            newPiece();
                            updateGhost();
                            Updatables.doTick = true;
                        }
                    }
                }
                if (!Renderables.bombAnimation.isEmpty()) {
                    BombAnimation anim;
                    bombTime++;
                    if (bombTime % 2 == 0) {
                        for (Iterator<BombAnimation> it = Renderables.bombAnimation.iterator(); it.hasNext(); ) {
                            anim = it.next();
                            anim.update();
                            if (!anim.isAlive()) it.remove();
                        }
                        if (Renderables.bombAnimation.isEmpty()) {
                            for (int i = 0; i < Settings.boardHeight; i++) {
                                if (Updatables.linesNoRender[i] == true) {
                                    for (int j = 0; j < Settings.boardWidth; j++) setShapeAt(j, Settings.boardHeight - i - 1, TetrisShape.__BOMBCLEAR);
                                }
                            }
                            for (int i = Settings.boardHeight - 1; i >= 0; i--) {
                                boolean lineIsFull = true;
                                for (int j = 0; j < Settings.boardWidth; ++j) {
                                    if (shapeAt(j, i) != TetrisShape.__BOMBCLEAR) {
                                        lineIsFull = false;
                                        break;
                                    }
                                }
                                if (lineIsFull) {
                                    for (int k = i; k < Settings.boardHeight - 1; k++) for (int j = 0; j < Settings.boardWidth; j++) setShapeAt(j, k, shapeAt(j, k + 1));
                                    for (int j = 0; j < Settings.boardWidth; j++) setShapeAt(j, Settings.boardHeight - 1, TetrisShape.NoShape);
                                }
                            }
                            Updatables.isBomb = false;
                            Arrays.fill(Updatables.linesNoRender, false);
                            Updatables.doTick = true;
                            Updatables.isWaitingAfterPiece = true;
                            Updatables.isWaitingAfterLine = false;
                            Settings.currPiece = new TetrisPiece();
                            newPiece();
                            updateGhost();
                        }
                    }
                }
                GlobalSettings.score.update();
                GlobalSettings.combo.update();
                if (Renderables.ghostOutline.isStarted()) {
                    Renderables.ghostOutline.update();
                }
                if (GlobalSettings.tetris) {
                    Renderables.tetris.update();
                    if (Renderables.tetris.isFinished()) GlobalSettings.tetris = false;
                }
                if (Settings.nextLevel != -1) {
                    if (Settings.nextLevel < Settings.boardHeight - 1) {
                        Settings.nextLevel++;
                    } else {
                        Settings.nextLevel = -1;
                    }
                }
            }
            if (GlobalSettings.completeTetris) {
                Renderables.cTetris.update();
            }
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        Color gray = Color.lightGray;
        g.setFont(GlobalSettings.font);
        g.setColor(gray);
        g.drawString(VERSION_STRING, 5, 10);
        if (!Renderables.menu.inGame()) {
            Renderables.menu.render(g);
        } else {
            synchronized (Renderables.background) {
                for (Point location : Renderables.background) g.drawImage(Images.background, location.x, location.y, null);
            }
            g.drawString(VERSION_STRING, 5, 10);
            g.setColor(new Color(gray.getRed(), gray.getGreen(), gray.getBlue(), 0x7F));
            g.drawRect(GlobalSettings.xyOffset, GlobalSettings.xyOffset, Settings.boardWidth * squareWidth(), Settings.boardHeight * squareHeight());
            boolean isGhostPieces = GlobalSettings.levelModes.contains(LevelMode.Ghost);
            int ghostUser = -1;
            if (isGhostPieces) {
                switch(GameSettings.gameMode) {
                    case GrandMaster:
                        ghostUser = 0x00;
                        break;
                    case Master:
                        ghostUser = 0x10;
                        break;
                    case Expert:
                        ghostUser = 0x20;
                        break;
                    case Normal:
                        ghostUser = 0x30;
                        break;
                }
            }
            for (int i = 0; i < Settings.boardHeight; ++i) for (int j = 0; j < Settings.boardWidth; ++j) if (shapeAt(j, Settings.boardHeight - i - 1) == TetrisShape.NoShape || Updatables.linesNoRender[i] == true) g.fillRect(getX(j) + squareWidth() / 2 - 1, getY(i) + squareHeight() / 2 - 1, 2, 2);
            for (int i = 0; i < Settings.boardHeight; i++) {
                if (Updatables.linesNoRender[i] == true) continue;
                for (int j = 0; j < Settings.boardWidth; j++) {
                    TetrisShape shape = shapeAt(j, Settings.boardHeight - i - 1);
                    if (shape != TetrisShape.NoShape) drawSquare(g, getX(j), getY(i), shape, ghostUser);
                }
            }
            for (LineToRemove line : Renderables.linesToRemove) {
                line.render(g);
                int opacity;
                int i, j;
                for (i = 0; i < Settings.boardHeight; i++) {
                    if (Updatables.linesNoRender[i] == false) continue;
                    for (j = 0; j < Settings.boardWidth; j++) {
                        TetrisShape shape = shapeAt(j, Settings.boardHeight - i - 1);
                        if ((opacity = line.getSquareOpacity(j)) != 0) drawSquare(g, getX(j), getY(i), shape, opacity);
                    }
                }
            }
            for (BombAnimation anim : Renderables.bombAnimation) {
                int i, j;
                i = anim.x();
                j = anim.y();
                if (anim.DisplayForeTile() && shapeAt(i, j) != TetrisShape.NoShape) drawSquare(g, getX(i), getY(Settings.boardHeight - j - 1), shapeAt(i, j), -1);
                anim.render(g);
            }
            if (Settings.currPiece.shape() != TetrisShape.NoShape && Renderables.linesToRemove.isEmpty() && !Updatables.isBomb) {
                for (int i = 0; i < Settings.currPiece.maxCoords(); i++) {
                    int x = Settings.curX + Settings.currPiece.x(i);
                    int y = Settings.curY + Settings.currPiece.y(i);
                    int gy = Settings.ghostY + Settings.currPiece.y(i);
                    if (GameSettings.gameMode != GameMode.Expert || GlobalSettings.inDevelopment) drawSquare(g, getX(x), getY(Settings.boardHeight - gy - 1), Settings.currPiece.shape(), 0x7F);
                    drawSquare(g, getX(x), getY(Settings.boardHeight - y - 1), Settings.currPiece.shape(), ghostUser);
                }
            }
            if (Renderables.ghostOutline.isStarted()) Renderables.ghostOutline.render(g);
            if (Settings.nextLevel != -1) for (int i = 0; i < Settings.boardWidth; i++) {
                drawSquare(g, i, Settings.nextLevel, Color.white, 0x30);
                drawSquare(g, i, Settings.nextLevel - 1, Color.white, 0x20);
                drawSquare(g, i, Settings.nextLevel - 2, Color.white, 0x10);
            }
            g.setColor(Color.lightGray);
            g.drawString(String.format("SCORE: %12s", GlobalSettings.formatter.format(GlobalSettings.score.intVal())), getX(Settings.boardWidth + 1), getY(1));
            g.drawString(String.format("COMBO: %12s", "x" + GlobalSettings.combo.intVal()), getX(Settings.boardWidth + 1), getY(2));
            g.drawString(String.format("LEVEL: %12d", GlobalSettings.level), getX(Settings.boardWidth + 1), getY(3));
            g.drawString(String.format("LEVEL: %12s", Settings.numPiecesDropped + "/" + (int) (Math.ceil((float) (Settings.numPiecesDropped > 0 ? Settings.numPiecesDropped : 1) / 25) * PIECES_PER_LEVEL)), getX(Settings.boardWidth + 1), getY(4));
            g.drawString(String.format("TIME: %13s", getTime()), getX(Settings.boardWidth + 1), getY(5));
            g.drawString("NEXT PIECE:", getX(Settings.boardWidth + 1), getY(6));
            g.drawRect(getX(Settings.boardWidth + 1), getY(7) - 1, 4 * squareWidth() + 2, 4 * squareHeight() + 2);
            if (Settings.nextPiece.shape() != TetrisShape.NoShape) {
                int dx = getX(Settings.boardWidth + 1) + 1;
                int dy = getY(7);
                int sx = (Settings.nextPiece.shape().ordinal() - 1) * 3 * squareWidth();
                g.drawImage(Images.pieces, dx + squareWidth() / 2, dy, (int) (dx + 3.5 * squareWidth()), dy + 4 * squareHeight(), sx, 0, sx + 3 * squareWidth(), 4 * squareHeight(), null);
            }
            g.setColor(Color.lightGray);
            g.drawString("HOLD PIECE:", getX(Settings.boardWidth + 1), getY(12));
            g.drawRect(getX(Settings.boardWidth + 1), getY(13) - 1, 4 * squareWidth() + 2, 4 * squareHeight() + 2);
            if (Settings.holdPiece.shape() != TetrisShape.NoShape) {
                int dx = getX(Settings.boardWidth + 1) + 1;
                int dy = getY(13);
                int sx = (Settings.holdPiece.shape().ordinal() - 1) * 3 * squareWidth();
                g.drawImage(Images.pieces, dx + squareWidth() / 2, dy, (int) (dx + 3.5 * squareWidth()), dy + 4 * squareHeight(), sx, 0, sx + 3 * squareWidth(), 4 * squareHeight(), null);
            }
            g.setColor(Color.lightGray);
            g.drawString("LEVEL MODES:", getX(Settings.boardWidth + 1), getY(18));
            int i = 0;
            LevelMode mode;
            Iterator<LevelMode> it;
            for (it = GlobalSettings.levelModes.iterator(); it.hasNext(); ) {
                mode = it.next();
                int dx = getX(Settings.boardWidth + 1) + (i++ * 30);
                int dy = getY(19);
                int sx = mode.ordinal() * 24;
                g.drawImage(Images.modes, dx, dy, dx + 24, dy + 24, sx, 0, sx + 24, 24, null);
            }
            synchronized (Renderables.scores) {
                for (ScoreText score : Renderables.scores) score.render(g);
            }
            if (GlobalSettings.completeTetris) Renderables.cTetris.render(g); else if (GlobalSettings.tetris) Renderables.tetris.render(g);
            if (Settings.isPaused) {
                Color colour = Color.darkGray;
                colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 0xBA);
                g.setColor(colour);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.lightGray);
                String message = "PRESS SPACE TO RESUME...";
                int width = GlobalSettings.fontMetrics.stringWidth(message);
                int height = GlobalSettings.fontMetrics.getHeight();
                g.drawString(message, getWidth() / 2 - width / 2, getHeight() / 2 - height / 2);
            }
        }
    }

    public void clearBoard() {
        for (int i = 0; i < Settings.boardHeight * Settings.boardWidth; i++) Settings.board[i] = TetrisShape.NoShape;
    }

    private void updateGhost() {
        int newY = Settings.curY;
        while (newY > 0) {
            if (!isValidMove(Settings.currPiece, Settings.curX, newY - 1)) break;
            newY--;
        }
        Settings.ghostY = newY;
    }

    private void tryHold() {
        if (!Renderables.menu.inGame() || Settings.isPaused || Settings.currPiece.shape() == TetrisShape.NoShape || Renderables.linesToRemove.size() > 0 || Settings.currPiece.shape() == TetrisShape.Bomb) return;
        if (!GlobalSettings.inDevelopment) if (Settings.holdsThisTurn >= 2) return;
        if (Settings.holdPiece.shape() == TetrisShape.NoShape) {
            Settings.holdPiece.setShape(Settings.currPiece.shape());
            newPiece();
        } else {
            TetrisShape shape = Settings.currPiece.shape();
            Settings.currPiece.setShape(Settings.holdPiece.shape());
            Settings.holdPiece.setShape(shape);
            Settings.curX = Settings.boardWidth / 2 + 1;
            Settings.curY = Settings.boardHeight - 2 + Settings.currPiece.minY();
            if (!tryMove(Settings.currPiece, Settings.curX, Settings.curY)) {
                Settings.currPiece.setShape(TetrisShape.NoShape);
                Updatables.doTick = false;
                finish();
            } else updateGhost();
        }
        Settings.holdsThisTurn++;
    }

    private void dropDown() {
        if (Renderables.linesToRemove.size() > 0) return;
        int dropHeight = 0;
        int newY = Settings.curY;
        while (newY > 0) {
            if (!tryMove(Settings.currPiece, Settings.curX, newY - 1)) break;
            newY--;
            dropHeight++;
        }
        pieceDropped(dropHeight + Updatables.pieceDropHeight);
    }

    private void oneLineDown(boolean isOnTick) {
        if (isOnTick) Updatables.isNewPiece = false;
        if (!tryMove(Settings.currPiece, Settings.curX, Settings.curY - 1)) {
            pieceDropped(isOnTick ? 0 : Updatables.pieceDropHeight);
        } else {
            Updatables.pieceDropHeight += 1;
        }
    }

    private void pieceDropped(int dropHeight) {
        for (int i = 0; i < Settings.currPiece.maxCoords(); ++i) {
            int x = Settings.curX + Settings.currPiece.x(i);
            int y = Settings.curY + Settings.currPiece.y(i);
            setShapeAt(x, y, Settings.currPiece.shape());
        }
        Settings.numPiecesDropped++;
        int scoreToAdd = dropHeight * 5;
        if (GlobalSettings.inDevelopment) scoreToAdd = 4000;
        createScore(scoreToAdd, Settings.curX + (Settings.currPiece.maxX() + Settings.currPiece.minX()) / 2, Settings.curY + (Settings.currPiece.maxY() + Settings.currPiece.minY()) / 2);
        if (Settings.currPiece.shape() != TetrisShape.Bomb) removeFullLines();
        Updatables.isWaitingAfterPiece = true;
        if (GlobalSettings.levelModes.contains(LevelMode.BombTetris) && Settings.currPiece.shape() == TetrisShape.Bomb) {
            int landingY = Settings.curY + Settings.currPiece.minY();
            int landingX = Settings.curX + Settings.currPiece.minX();
            int height = Settings.currPiece.maxY() - Settings.currPiece.minY() + 1;
            int width = Settings.currPiece.maxX() - Settings.currPiece.minX() + 1;
            int pause = 0;
            int score = 0;
            int i, j;
            for (i = 0; i < height; i++) {
                for (j = 0; j < Settings.boardWidth; j++) {
                    pause++;
                    if (isIn(j, landingX, landingX + width)) pause = 0; else pause = (j < landingX ? landingX - j : j - landingX - width);
                    if (shapeAt(j, i + landingY) == TetrisShape.NoShape) score = 0; else {
                        score = 10;
                        addCombo(2);
                    }
                    Renderables.bombAnimation.add(new BombAnimation(pause, j, i + landingY, score));
                }
                Updatables.linesNoRender[Settings.boardHeight - i - landingY - 1] = true;
            }
            Updatables.isBomb = true;
            Updatables.isWaitingAfterLine = true;
            Updatables.doTick = false;
        }
        if (Renderables.linesToRemove.size() > 0 || Updatables.isBomb || Updatables.isNewPiece) {
            if (GlobalSettings.tetris) addCombo(6); else if (GlobalSettings.completeTetris) {
                addCombo(10);
                addCombo(GlobalSettings.combo.nextVal());
            } else addCombo(Renderables.linesToRemove.size());
        } else resetCombo(false);
        int minX = Settings.curX;
        int minY = Settings.curY;
        Renderables.ghostOutline.start(minX, minY, Settings.currPiece);
        Updatables.isNewPiece = true;
        if (Settings.numPiecesDropped % PIECES_PER_LEVEL == 0) {
            nextLevel();
        }
        if (!Updatables.isWaitingAfterLine) newPiece();
    }

    private void removeFullLines() {
        int[] scores = new int[] { 10, 20, 35, 65, 100, 400 };
        int TETRIS = 4;
        int COMPLETE_TETRIS = 5;
        int scoreType = 0;
        ArrayList<Integer> lines = new ArrayList<Integer>();
        int numLines = 0;
        for (int i = Settings.boardHeight - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            for (int j = 0; j < Settings.boardWidth; ++j) if (shapeAt(j, i) == TetrisShape.NoShape) {
                lineIsFull = false;
                break;
            }
            if (lineIsFull) {
                lines.add(new Integer(i));
                Updatables.linesNoRender[Settings.boardHeight - i - 1] = true;
                numLines++;
            }
        }
        if (numLines > 0) {
            boolean foundBrick = false;
            if (numLines == 4) {
                foundBrick: for (int i = Settings.boardHeight - 1; i >= 0; i--) {
                    if (Updatables.linesNoRender[Settings.boardHeight - i - 1] == true) continue;
                    for (int j = 0; j < Settings.boardWidth; j++) if (shapeAt(j, i) != TetrisShape.NoShape) {
                        foundBrick = true;
                        break foundBrick;
                    }
                }
                if (!foundBrick) {
                    Renderables.cTetris.start();
                    GlobalSettings.completeTetris = true;
                } else {
                    Renderables.tetris.start();
                    GlobalSettings.tetris = true;
                }
            }
            if (GlobalSettings.completeTetris) GlobalSettings.numCombos[4]++; else GlobalSettings.numCombos[numLines - 1]++;
            for (Integer i : lines) {
                Renderables.linesToRemove.add(new LineToRemove(i.intValue(), numLines < 4 ? scores[scoreType++] : GlobalSettings.completeTetris ? scores[COMPLETE_TETRIS] : scores[TETRIS]));
            }
            Updatables.doTick = false;
            Updatables.isWaitingAfterLine = true;
        }
    }

    public void newPiece() {
        if (GlobalSettings.levelModes.contains(LevelMode.BombTetris)) {
            if (Settings.currPiece.shape() != TetrisShape.Bomb) {
                if (GlobalSettings.generator.nextInt(12) == 0) {
                    Settings.currPiece = new Bomb();
                    Settings.currPiece.setShape(TetrisShape.Bomb);
                } else {
                    Settings.currPiece.setShape(Settings.nextPiece.shape());
                    Settings.nextPiece.setRandomShape();
                }
            }
        } else {
            Settings.currPiece.setShape(Settings.nextPiece.shape());
            Settings.nextPiece.setRandomShape();
        }
        Settings.curX = Settings.boardWidth / 2 - Settings.currPiece.maxX();
        Settings.curY = Settings.boardHeight - 2 + Settings.currPiece.minY();
        Settings.holdsThisTurn = 0;
        if (!tryMove(Settings.currPiece, Settings.curX, Settings.curY)) {
            Settings.currPiece.setShape(TetrisShape.NoShape);
            Updatables.doTick = false;
            finish();
        } else {
            updateGhost();
            Updatables.nextRotation = 3;
            Updatables.nextShapeChange = 3;
        }
    }

    private boolean isValidMove(BaseTetrisPiece currPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + currPiece.x(i);
            int y = newY + currPiece.y(i);
            if (x < 0 || x >= Settings.boardWidth || y < 0 || y >= Settings.boardHeight) return false;
            if (shapeAt(x, y) != TetrisShape.NoShape) return false;
        }
        return true;
    }

    private boolean tryMove(BaseTetrisPiece currPiece, int newX, int newY) {
        if (Settings.isPaused || !Renderables.menu.inGame()) return false;
        boolean result = isValidMove(currPiece, newX, newY);
        if (!result) return false;
        Updatables.tickTime = Updatables.timeoutTime;
        Settings.currPiece = currPiece;
        Settings.curX = newX;
        Settings.curY = newY;
        updateGhost();
        return result;
    }

    private static final Color[] colourTable = { new Color(0x000000), new Color(0xCC6666), new Color(0x66CC66), new Color(0x6666CC), new Color(0xCCCC66), new Color(0xCC66CC), new Color(0x66CCCC), new Color(0xDAAA00), new Color(0xDDDDDD) };

    private BufferedImage MText;

    private int MTextHeight;

    private void drawSquare(Graphics g, int x, int y, TetrisShape shape, int phantom) {
        Color colour = colourTable[shape.ordinal()];
        Color brighter = colour.brighter();
        Color darker = colour.darker();
        if (phantom != -1) {
            colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), phantom);
            brighter = new Color(brighter.getRed(), brighter.getGreen(), brighter.getBlue(), phantom);
            darker = new Color(darker.getRed(), darker.getGreen(), darker.getBlue(), phantom);
        }
        g.setColor(colour);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(brighter);
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(darker);
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
        if (shape == TetrisShape.Bomb) {
            if (MText == null) {
                MTextHeight = GlobalSettings.fontMetrics.getHeight();
                MText = new BufferedImage(MTextHeight, MTextHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = MText.createGraphics();
                g2d.setColor(Color.darkGray);
                g2d.drawString("M", 0, MTextHeight);
                g2d.dispose();
            }
            g.drawImage(MText, x + squareWidth() / 2 - MTextHeight / 2, y + squareHeight() / 2 - MTextHeight / 2, null);
        }
    }

    public static void drawSquare(Graphics g, int x, int y, Color colour, int alpha) {
        Color brighter = colour.brighter();
        Color darker = colour.darker();
        x = getX(x);
        y = getY(Settings.boardHeight - y - 1);
        if (alpha != -1) {
            colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);
            brighter = new Color(brighter.getRed(), brighter.getGreen(), brighter.getBlue(), alpha);
            darker = new Color(darker.getRed(), darker.getGreen(), darker.getBlue(), alpha);
        }
        g.setColor(colour);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(brighter);
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(darker);
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    public void createScore(int score, int x, int y) {
        score *= GlobalSettings.combo.nextVal();
        GlobalSettings.score.add(score);
        Renderables.scores.add(new ScoreText(score, getX(x), getY(Settings.boardHeight - y)));
    }

    public void nextLevel() {
        GlobalSettings.level++;
        Settings.nextLevel = 2;
        GlobalSettings.levelModes.clear();
        switch(GameSettings.gameMode) {
            case GrandMaster:
                if (GlobalSettings.level % 8 == 0) GlobalSettings.levelModes.add(LevelMode.ShapeChange);
            case Master:
                if (GlobalSettings.level % 12 == 0) GlobalSettings.levelModes.add(LevelMode.RotationChange);
            case Expert:
                if (GlobalSettings.level % 3 == 0) GlobalSettings.levelModes.add(LevelMode.BombTetris);
                if (GlobalSettings.level % 6 == 0) GlobalSettings.levelModes.add(LevelMode.EvilMode);
            case Normal:
                if (GlobalSettings.level % 5 == 0) GlobalSettings.levelModes.add(LevelMode.Ghost);
        }
        Updatables.tickTime = Updatables.timeoutTime = (timeoutTime() / 20);
        Updatables.nextRotation = 3;
        Updatables.nextShapeChange = 3;
        Updatables.doTick = true;
    }

    public static int getX(int x) {
        return GlobalSettings.xyOffset + x * squareWidth();
    }

    public static int getX(float x) {
        return (int) (GlobalSettings.xyOffset + x * squareWidth());
    }

    public static int getY(int y) {
        return GlobalSettings.xyOffset + y * squareHeight();
    }

    public static int getY(float y) {
        return (int) (GlobalSettings.xyOffset + y * squareHeight());
    }

    public static boolean isIn(int value, int a, int b) {
        return value >= a && value <= b;
    }

    public void resetCombo(boolean instant) {
        int baseCombo = 1;
        switch(GameSettings.gameMode) {
            case GrandMaster:
                baseCombo = 5;
                break;
            case Master:
                baseCombo = 3;
                break;
            case Expert:
                baseCombo = 2;
                break;
            default:
                baseCombo = 1;
                break;
        }
        if (instant) GlobalSettings.combo.instantSet(baseCombo); else GlobalSettings.combo.set(baseCombo);
    }

    public void addCombo(int amount) {
        if (GameSettings.gameMode != GameMode.Classic) GlobalSettings.combo.add(amount);
    }

    private String getTime() {
        int time = GlobalSettings.time / (1000 / GlobalSettings.updateTickTime);
        int minutes = (int) Math.floor((float) time / 60);
        int seconds = time - minutes * 60;
        return (minutes > 0 ? minutes + "m " : "") + seconds + "";
    }

    private void updateBackground() {
        Renderables.background.clear();
        int x = Updatables.bgLocation.x - 500;
        int y = Updatables.bgLocation.y - 500;
        int i, j;
        for (i = 0; i < 3; i++) for (j = 0; j < 3; j++) Renderables.background.add(new Point(x + i * 500, y + j * 500));
    }

    private void loadIni(File fIni) throws Exception {
        Ini ini = new Ini();
        ini.load(fIni);
        GameSettings.alternateKeys = new Boolean(ini.get("Main", "AlternateKeys")).booleanValue();
        GameSettings.movingBackground = new Boolean(ini.get("Main", "MovingBackground")).booleanValue();
        GameSettings.gameMode = GameMode.valueOf(ini.get("Main", "GameSetting"));
        GameSettings.gmTimeout = new Integer(ini.get("Times", "GrandMaster")).intValue();
        GameSettings.mTimeout = new Integer(ini.get("Times", "Master")).intValue();
        GameSettings.eTimeout = new Integer(ini.get("Times", "Expert")).intValue();
        GameSettings.dTimeout = new Integer(ini.get("Times", "Default")).intValue();
    }

    private void saveIni(File fIni) throws Exception {
        Ini ini = new Ini();
        fIni.createNewFile();
        ini.add("Main", "AlternateKeys", new Boolean(GameSettings.alternateKeys));
        ini.add("Main", "MovingBackground", new Boolean(GameSettings.movingBackground));
        ini.add("Main", "GameSetting", GameSettings.gameMode);
        ini.add("Times", "GrandMaster", GameSettings.gmTimeout);
        ini.add("Times", "Master", GameSettings.mTimeout);
        ini.add("Times", "Expert", GameSettings.eTimeout);
        ini.add("Times", "Default", GameSettings.dTimeout);
        ini.store(fIni);
    }
}
