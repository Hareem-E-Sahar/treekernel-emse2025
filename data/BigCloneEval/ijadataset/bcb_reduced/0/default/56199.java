import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.rms.RecordStore;
import java.util.Date;
import java.util.Random;

class Othello extends Canvas implements Runnable {

    public static final int BGCOLOR = 0x6c6c8c;

    public static final int CONTROL_LEFT = 0;

    public static final int CONTROL_RIGHT = 1;

    public static final int MENU_NONE = 0;

    public static final int MENU_GAME = 1;

    public static final int MENU_SETTINGS = 2;

    public static final int MENUITEM_SEP = 0;

    public static final int MENUITEM_NEW = 1;

    public static final int MENUITEM_LOAD = 2;

    public static final int MENUITEM_SAVE = 3;

    public static final int MENUITEM_SETTINGS = 4;

    public static final int MENUITEM_LEVEL_EASY = 5;

    public static final int MENUITEM_LEVEL_MEDIUM = 6;

    public static final int MENUITEM_LEVEL_HARD = 7;

    public static final int MENUITEM_BLACK_HUMAN = 8;

    public static final int MENUITEM_BLACK_AI = 9;

    public static final int MENUITEM_WHITE_HUMAN = 10;

    public static final int MENUITEM_WHITE_AI = 11;

    public static final int MENUITEM_UNDO = 12;

    public static final int MENUITEM_PASS = 13;

    public static final int MENUITEM_RESIGN = 14;

    public static final int MENUITEM_EXIT = 15;

    public static final int LEVEL_EASY = 0;

    public static final int LEVEL_MEDIUM = 1;

    public static final int LEVEL_HARD = 2;

    public static final int PLAYER_HUMAN = 0;

    public static final int PLAYER_AI = 1;

    public static final byte BLANK = -1;

    public static final byte BLACK = 0;

    public static final byte WHITE = 1;

    public static final int HISTORY = 120;

    public static final int menus[][] = { null, { MENUITEM_NEW, MENUITEM_LOAD, MENUITEM_SAVE, MENUITEM_SEP, MENUITEM_SETTINGS, MENUITEM_SEP, MENUITEM_UNDO, MENUITEM_PASS, MENUITEM_RESIGN, MENUITEM_SEP, MENUITEM_EXIT }, { MENUITEM_LEVEL_EASY, MENUITEM_BLACK_HUMAN, MENUITEM_WHITE_HUMAN } };

    /**
	 * Table of heuristic values.
	 */
    public static final int[] weight_table = { 65, -3, 6, 4, 4, 6, -3, 65, -3, -29, 3, 1, 1, 3, -29, -3, 6, 3, 5, 3, 3, 5, 3, 6, 4, 1, 3, 1, 1, 3, 1, 4, 4, 1, 3, 1, 1, 3, 1, 4, 6, 3, 5, 3, 3, 5, 3, 6, -3, -29, 3, 1, 1, 3, -29, -3, 65, -3, 6, 4, 4, 6, -3, 65 };

    private Image image;

    private Graphics graphics;

    private Font font;

    private OthelloMIDlet midlet;

    private FlashMessage flash;

    private int width;

    private int height;

    public Image img_board, img_score, img_turns, img_digits, img_controls, img_menu, img_cursor, img_marker, img_dot, img_draw;

    public Image img_turn[] = new Image[2];

    public Image img_coin_score[] = new Image[2];

    public Image img_coin[] = new Image[2];

    public Image img_pass[] = new Image[2];

    public Image img_win[] = new Image[2];

    public int player[] = new int[2];

    private int level;

    public int turn;

    public byte work[], board[][];

    public byte history[][];

    public byte history_x[];

    public byte history_y[];

    public byte history_color[];

    private byte row[] = new byte[8];

    private int x, y;

    private int marker_x, marker_y;

    public byte color;

    public int blankcount, blackcount, whitecount;

    public boolean locked;

    private boolean gameover;

    public int menu;

    public int menu_index[] = new int[3];

    private Thread thread;

    /**
	 * Comment out if you don't want a lot of printouts.
	 */
    public static void println(String s) {
    }

    /**
	 * Comment out if you don't want a lot of printouts.
	 */
    public static void print(String s) {
    }

    /**
	 * Constructor. Load images and create objects.
	 */
    public Othello(OthelloMIDlet m) throws IOException {
        super();
        this.setFullScreenMode(true);
        midlet = m;
        flash = new FlashMessage(this);
        Date d = new Date();
        Random r = new Random(d.getTime());
        Result.random = r.nextInt() % 100;
        width = getWidth();
        height = getHeight();
        image = Image.createImage(width, height);
        graphics = image.getGraphics();
        font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        graphics.setFont(font);
        img_board = Image.createImage("/board.png");
        img_score = Image.createImage("/score.png");
        img_turns = Image.createImage("/turns.png");
        img_digits = Image.createImage("/digits.png");
        img_controls = Image.createImage("/controls.png");
        img_menu = Image.createImage("/menu.png");
        img_cursor = Image.createImage("/cursor.png");
        img_marker = Image.createImage("/marker.png");
        img_dot = Image.createImage("/dot.png");
        img_draw = Image.createImage("/draw.png");
        img_turn[BLACK] = Image.createImage("/turn_black.png");
        img_turn[WHITE] = Image.createImage("/turn_white.png");
        img_coin_score[BLACK] = Image.createImage("/score_black.png");
        img_coin_score[WHITE] = Image.createImage("/score_white.png");
        img_coin[BLACK] = Image.createImage("/coin_black.png");
        img_coin[WHITE] = Image.createImage("/coin_white.png");
        img_pass[BLACK] = Image.createImage("/pass_black.png");
        img_pass[WHITE] = Image.createImage("/pass_white.png");
        img_win[BLACK] = Image.createImage("/win_black.png");
        img_win[WHITE] = Image.createImage("/win_white.png");
        player[BLACK] = PLAYER_HUMAN;
        player[WHITE] = PLAYER_AI;
        level = LEVEL_MEDIUM;
        history = new byte[HISTORY][64];
        history_x = new byte[HISTORY];
        history_y = new byte[HISTORY];
        history_color = new byte[HISTORY];
        work = null;
        board = new byte[17][64];
        color = BLANK;
        menu = MENU_NONE;
        menu_index[MENU_GAME] = 0;
        menu_index[MENU_SETTINGS] = 0;
        locked = false;
        restart();
    }

    /**
	 * Copy a board.
	 */
    public void copy(byte d[], byte s[]) {
        for (int i = 0; i < 64; i++) d[i] = s[i];
    }

    /**
	 * Lock events during flashing messages.
	 */
    public void lock() {
        locked = true;
    }

    /**
	 * Unlock events after flashing messages is done.
	 */
    public void unlock() {
        locked = false;
    }

    /**
	 * Load the game.
	 */
    public void load() {
        try {
            int i, j, n = 0;
            byte saved_game[] = null;
            RecordStore r = RecordStore.openRecordStore("mobileothello", false, RecordStore.AUTHMODE_PRIVATE, false);
            saved_game = r.getRecord(1);
            player[0] = (int) saved_game[n++];
            player[1] = (int) saved_game[n++];
            level = (int) saved_game[n++];
            turn = (int) saved_game[n++];
            for (i = 0, work = board[0]; i < 64; i++) work[i] = saved_game[n++];
            for (i = 0; i <= turn; i++) {
                for (j = 0, work = history[i]; j < 64; j++) work[j] = saved_game[n++];
                history_x[i] = saved_game[n++];
                history_y[i] = saved_game[n++];
                history_color[i] = saved_game[n++];
            }
            x = (int) saved_game[n++];
            y = (int) saved_game[n++];
            marker_x = (int) saved_game[n++];
            marker_y = (int) saved_game[n++];
            color = saved_game[n++];
            gameover = saved_game[n++] == 1;
            work = board[0];
            r.closeRecordStore();
            if (!gameover) start();
        } catch (Exception ex) {
            restart();
        }
    }

    /**
	 * Save game.
	 */
    public void save() {
        try {
            int i, j, n = 0;
            byte saved_game[] = new byte[4 + 64 + (turn + 1) * 67 + 6];
            RecordStore r = RecordStore.openRecordStore("mobileothello", true, RecordStore.AUTHMODE_PRIVATE, true);
            saved_game[n++] = (byte) player[0];
            saved_game[n++] = (byte) player[1];
            saved_game[n++] = (byte) level;
            saved_game[n++] = (byte) turn;
            for (i = 0, work = board[0]; i < 64; i++) saved_game[n++] = work[i];
            for (i = 0; i <= turn; i++) {
                for (j = 0, work = history[i]; j < 64; j++) saved_game[n++] = work[j];
                saved_game[n++] = history_x[i];
                saved_game[n++] = history_y[i];
                saved_game[n++] = history_color[i];
            }
            saved_game[n++] = (byte) x;
            saved_game[n++] = (byte) y;
            saved_game[n++] = (byte) marker_x;
            saved_game[n++] = (byte) marker_y;
            saved_game[n++] = color;
            saved_game[n++] = (byte) (gameover ? 1 : 0);
            work = board[0];
            if (r.getNumRecords() == 0) r.addRecord(saved_game, 0, saved_game.length); else r.setRecord(1, saved_game, 0, saved_game.length);
            r.closeRecordStore();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * Restart the game.
	 */
    private void restart() {
        int i;
        turn = -1;
        work = board[0];
        for (i = 0; i < 64; i++) work[i] = BLANK;
        work[27] = WHITE;
        work[28] = BLACK;
        work[35] = BLACK;
        work[36] = WHITE;
        color = BLANK;
        blankcount = 60;
        blackcount = 2;
        whitecount = 2;
        gameover = false;
        marker_x = -1;
        marker_y = -1;
        nextPlayer();
        start();
    }

    /**
	 * Start the AI-thread.
	 */
    public void start() {
        if (thread != null) return;
        thread = new Thread(this);
        thread.start();
    }

    /**
	 * Stop the AI-thread.
	 */
    public void stop() {
        thread = null;
    }

    /**
	 * Run the AI-thread.
	 */
    public void run() {
        long t;
        while (thread != null) {
            if (!locked && !gameover && player[color] == PLAYER_AI) {
                t = new Date().getTime();
                try {
                    ai();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.gc();
                t = new Date().getTime() - t;
                println("ai(t=" + t + ")");
                if (t < 500) try {
                    Thread.sleep(500 - t);
                } catch (Exception ex) {
                }
                repaint();
            } else try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }
        }
    }

    /**
	 * Game's AI makes a move. This function is synchronized so that it's not called when
	 * a key is pressed or the other way around.
	 */
    public synchronized void ai() {
        Result r = calculateBestMove((level + 1) * 2, color);
        marker_x = -1;
        marker_y = -1;
        if (r == null) pass(); else {
            set(r.x, r.y, color);
            nextPlayer();
        }
    }

    /**
	 * Undo a move. Note that this is called twice if the previous player is the AI.
	 */
    public boolean undo() {
        if (turn == 0) return false;
        turn--;
        if (turn >= HISTORY) turn = HISTORY - 1;
        copy(work, history[turn]);
        marker_x = (int) history_x[turn];
        marker_y = (int) history_y[turn];
        color = history_color[turn];
        statistics();
        return true;
    }

    /**
	 * Pass this turn for the next player.
	 */
    public void pass() {
        if (gameover) return;
        flash.setMessage(img_pass[color], 1);
        marker_x = -1;
        marker_y = -1;
        nextPlayer();
    }

    /**
	 * When a players has moved or made a pass, it's the next player's turn.
	 */
    public void nextPlayer() {
        while (locked) try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        color = color == BLACK ? WHITE : BLACK;
        statistics();
        if (blankcount == 0 || blackcount == 0 || whitecount == 0 || (mustPass(BLACK) && mustPass(WHITE))) {
            if (blackcount > whitecount) gameOver(BLACK); else if (whitecount > blackcount) gameOver(WHITE); else gameOver(BLANK);
        }
        turn++;
        if (turn < HISTORY) {
            copy(history[turn], work);
            history_x[turn] = (byte) marker_x;
            history_y[turn] = (byte) marker_y;
            history_color[turn] = color;
        }
        if (player[color] == PLAYER_HUMAN) {
            calculateBestMove(0, color);
            System.gc();
        }
    }

    /**
	 * Sum up the coins.
	 */
    public void statistics() {
        blankcount = 0;
        whitecount = 0;
        blackcount = 0;
        for (int i = 0; i < 64; i++) {
            if (work[i] == BLACK) blackcount++; else if (work[i] == WHITE) whitecount++; else blankcount++;
        }
    }

    /**
	 * Called when either the board is filled or none of the players has any more moves.
	 */
    public void gameOver(byte winner) {
        stop();
        gameover = true;
        flash.setMessage(winner == BLANK ? img_draw : img_win[winner], 5);
    }

    /**
	 * Exit the application.
	 */
    public void exit() {
        stop();
        midlet.destroyApp(true);
        midlet.notifyDestroyed();
    }

    /**
	 * To handle controlkey events.
	 */
    public void controlAction(int event) {
        if (event == CONTROL_LEFT) {
            if (menu == MENU_NONE) handleEvent(MENUITEM_EXIT);
            return;
        } else if (event == CONTROL_RIGHT) {
            switch(menu) {
                case MENU_NONE:
                case MENU_SETTINGS:
                    menu = MENU_GAME;
                    break;
                case MENU_GAME:
                    menu = MENU_NONE;
                    break;
            }
        }
        repaint();
    }

    /**
	 * To handle any keypresses or other events.
	 */
    protected void handleEvent(int event) {
        switch(event) {
            case MENUITEM_NEW:
                menu = MENU_NONE;
                restart();
                break;
            case MENUITEM_LOAD:
                menu = MENU_NONE;
                load();
                break;
            case MENUITEM_SAVE:
                menu = MENU_NONE;
                save();
                break;
            case MENUITEM_SETTINGS:
                menu = MENU_SETTINGS;
                break;
            case MENUITEM_LEVEL_EASY:
            case MENUITEM_LEVEL_MEDIUM:
            case MENUITEM_LEVEL_HARD:
                level = (level + 1) % 3;
                break;
            case MENUITEM_BLACK_HUMAN:
            case MENUITEM_BLACK_AI:
            case MENUITEM_WHITE_HUMAN:
            case MENUITEM_WHITE_AI:
                {
                    byte p = event == MENUITEM_BLACK_HUMAN || event == MENUITEM_BLACK_AI ? BLACK : WHITE;
                    player[p] = (player[p] + 1) % 2;
                    break;
                }
            case MENUITEM_UNDO:
                {
                    menu = MENU_NONE;
                    if (player[color] != PLAYER_HUMAN) break;
                    if (!undo()) break;
                    if (player[color] == PLAYER_AI) undo();
                    if (gameover) {
                        gameover = false;
                        start();
                    }
                    break;
                }
            case MENUITEM_PASS:
                menu = MENU_NONE;
                if (mustPass(color)) pass(); else calculateBestMove(0, color);
                break;
            case MENUITEM_RESIGN:
                break;
            case MENUITEM_EXIT:
                exit();
                return;
        }
        repaint();
    }

    /**
	 * The keyPressed-function.
	 */
    protected synchronized void keyPressed(int code) {
        int game = getGameAction(code);
        if (locked) return;
        try {
            if (code == -6) controlAction(CONTROL_LEFT); else if (code == -7) controlAction(CONTROL_RIGHT); else if (menu != MENU_NONE) {
                switch(code) {
                    case Canvas.KEY_STAR:
                        handleEvent(MENUITEM_UNDO);
                        return;
                    case Canvas.KEY_NUM0:
                        handleEvent(MENUITEM_PASS);
                        return;
                    case Canvas.KEY_POUND:
                        menu = menu == MENU_SETTINGS ? MENU_NONE : MENU_SETTINGS;
                        break;
                    default:
                        switch(game) {
                            case Canvas.UP:
                                while (--menu_index[menu] >= 0 && menus[menu][menu_index[menu]] == MENUITEM_SEP) ;
                                if (menu_index[menu] < 0) menu_index[menu] = menus[menu].length - 1;
                                break;
                            case Canvas.DOWN:
                                while (++menu_index[menu] < menus[menu].length && menus[menu][menu_index[menu]] == MENUITEM_SEP) ;
                                if (menu_index[menu] >= menus[menu].length) menu_index[menu] = 0;
                                break;
                            case Canvas.FIRE:
                                handleEvent(menus[menu][menu_index[menu]]);
                            default:
                                return;
                        }
                        break;
                }
                repaint();
            } else {
                switch(code) {
                    case Canvas.KEY_STAR:
                        handleEvent(MENUITEM_UNDO);
                        return;
                    case Canvas.KEY_NUM0:
                        handleEvent(MENUITEM_PASS);
                        return;
                    case Canvas.KEY_POUND:
                        menu = menu == MENU_SETTINGS ? MENU_NONE : MENU_SETTINGS;
                        break;
                    case Canvas.KEY_NUM1:
                        y--;
                        x--;
                        break;
                    case Canvas.KEY_NUM2:
                        y--;
                        break;
                    case Canvas.KEY_NUM3:
                        x++;
                        y--;
                        break;
                    case Canvas.KEY_NUM4:
                        x--;
                        break;
                    case Canvas.KEY_NUM6:
                        x++;
                        break;
                    case Canvas.KEY_NUM7:
                        x--;
                        y++;
                        break;
                    case Canvas.KEY_NUM8:
                        y++;
                        break;
                    case Canvas.KEY_NUM9:
                        x++;
                        y++;
                        break;
                    default:
                        switch(game) {
                            case Canvas.UP:
                                y--;
                                break;
                            case Canvas.LEFT:
                                x--;
                                break;
                            case Canvas.RIGHT:
                                x++;
                                break;
                            case Canvas.DOWN:
                                y++;
                                break;
                            case Canvas.FIRE:
                                if (player[color] != PLAYER_HUMAN) return;
                                if (check(x, y, color)) {
                                    set(x, y, color);
                                    nextPlayer();
                                    break;
                                }
                            default:
                                return;
                        }
                        break;
                }
                if (x < 0) x += 8; else if (x > 7) x -= 8;
                if (y < 0) y += 8; else if (y > 7) y -= 8;
                repaint();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * The paint-function.
	 */
    public synchronized void paint(Graphics g) {
        int i, j;
        int bw = img_board.getWidth(), bh = img_board.getHeight();
        int bx = (width - bw) / 2, by = (height - (bh + 2 + img_score.getHeight())) / 2;
        int sw = img_score.getWidth(), sh = img_score.getHeight();
        int tx = (width - img_turns.getWidth()) / 2;
        int cw = img_controls.getWidth(), cw2 = cw / 2, ch = img_controls.getHeight();
        graphics.setClip(0, 0, width, height);
        graphics.setColor(BGCOLOR);
        graphics.fillRect(0, 0, width, height);
        graphics.drawImage(img_board, bx, by, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(img_score, bx + 3, by + bh + 2, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(img_score, bx + bw - sw - 3, by + bh + 2, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(img_coin_score[0], bx + 3 + sw + 3, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(img_coin_score[1], bx + bw - sw - 6 - img_coin_score[1].getWidth(), by + bh + 4, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(img_turns, tx, by + bh + 2, Graphics.TOP | Graphics.LEFT);
        for (j = 0; j < 8; j++) for (i = 0; i < 8; i++) {
            if (work[i + j * 8] != BLANK) graphics.drawImage(img_coin[work[i + j * 8]], bx + 6 + i * 20, by + 6 + j * 20, Graphics.TOP | Graphics.LEFT); else if (player[color] == PLAYER_HUMAN && check(i, j, color)) graphics.drawImage(img_dot, bx + 12 + i * 20, by + 12 + j * 20, Graphics.TOP | Graphics.LEFT);
        }
        graphics.drawImage(img_cursor, bx + 6 + x * 20, by + 6 + y * 20, Graphics.TOP | Graphics.LEFT);
        if (marker_x != -1 && marker_y != -1) graphics.drawImage(img_marker, bx + 9 + marker_x * 20, by + 9 + marker_y * 20, Graphics.TOP | Graphics.LEFT);
        if (blackcount >= 10) graphics.drawRegion(img_digits, (blackcount / 10) * 12, 0, 12, 21, Sprite.TRANS_NONE, bx + 5, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        graphics.drawRegion(img_digits, (blackcount % 10) * 12, 0, 12, 21, Sprite.TRANS_NONE, bx + 17, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        if (whitecount >= 10) graphics.drawRegion(img_digits, (whitecount / 10) * 12, 0, 12, 21, Sprite.TRANS_NONE, bx + bw - sw + 1, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        graphics.drawRegion(img_digits, (whitecount % 10) * 12, 0, 12, 21, Sprite.TRANS_NONE, bx + bw - sw + 13, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        i = (turn + 1) / 2;
        if (turn >= 100) graphics.drawRegion(img_digits, (i / 100) * 12, 0, 12, 21, Sprite.TRANS_NONE, tx + 2, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        if (turn >= 10) graphics.drawRegion(img_digits, ((i / 10) % 10) * 12, 0, 12, 21, Sprite.TRANS_NONE, tx + 14, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        graphics.drawRegion(img_digits, (i % 10) * 12, 0, 12, 21, Sprite.TRANS_NONE, tx + 26, by + bh + 4, Graphics.TOP | Graphics.LEFT);
        if (menu != MENU_NONE) {
            int mx = width - 107 - 12, my = height - ch - 12 - menus[menu].length * 11;
            int mw = img_menu.getWidth(), mh = img_menu.getHeight();
            graphics.drawRegion(img_menu, 0, 0, 113, 6, Sprite.TRANS_NONE, mx, my, Graphics.TOP | Graphics.LEFT);
            graphics.drawRegion(img_menu, mw - 6, 0, 6, height - my - ch - 6, Sprite.TRANS_NONE, width - 6, my, Graphics.TOP | Graphics.LEFT);
            graphics.drawRegion(img_menu, 0, mh - (height - my - ch - 6), 6, height - my - ch - 6, Sprite.TRANS_NONE, mx, my + 6, Graphics.TOP | Graphics.LEFT);
            graphics.drawRegion(img_menu, mw - 113, mh - 6, 113, 6, Sprite.TRANS_NONE, mx + 6, height - ch - 6, Graphics.TOP | Graphics.LEFT);
            for (i = 0; i < menus[menu].length; i++) {
                j = menus[menu][i];
                if (j == MENUITEM_LEVEL_EASY) j += level; else if (j == MENUITEM_BLACK_HUMAN) j += player[BLACK]; else if (j == MENUITEM_WHITE_HUMAN) j += player[WHITE];
                graphics.drawRegion(img_menu, menu_index[menu] != i ? 6 : 113, 6 + j * 11, 107, 11, Sprite.TRANS_NONE, mx + 6, my + 6 + i * 11, Graphics.TOP | Graphics.LEFT);
            }
        }
        graphics.drawImage(img_turn[color], (width - img_turn[color].getWidth()) / 2, 0, Graphics.TOP | Graphics.LEFT);
        if (flash.showMessage()) graphics.drawImage(flash.image, (width - flash.image.getWidth()) / 2, (height - flash.image.getHeight()) / 2, Graphics.TOP | Graphics.LEFT); else if (flash.counter == 0) {
            graphics.drawRegion(img_controls, 0, 0, cw2, ch, Sprite.TRANS_NONE, 0, height - ch, Graphics.TOP | Graphics.LEFT);
            graphics.drawRegion(img_controls, cw2, 0, cw2, ch, Sprite.TRANS_NONE, width - cw2, height - ch, Graphics.TOP | Graphics.LEFT);
        }
        g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
    }

    /**
	 * Returns true if the player must make a pass because of no options.
	 */
    public boolean mustPass(byte color) {
        int i, j, n = 0;
        for (j = 0; j < 8; j++) for (i = 0; i < 8; i++) if (check(i, j, color)) n++;
        return n == 0;
    }

    private int iterations;

    /**
	 * This method calculates the best move and returns the evaluated result.
	 */
    public Result calculateBestMove(int level, byte color) {
        if (mustPass(color)) return null;
        int i, l = blankcount <= 8 && player[color] == PLAYER_AI ? blankcount * 2 + 1 : level + 1;
        Result r[] = new Result[l];
        for (i = 0; i < l; i++) r[i] = new Result((i & 1) == 0, color);
        iterations = 0;
        try {
            if (blankcount <= 8 && player[color] == PLAYER_AI) evaluateEnd(l - 1, 0, r, ""); else evaluateMove(level, 0, -1000000, r, "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        x = r[0].x;
        y = r[0].y;
        return r[0];
    }

    /**
	 * Weight is evaluated relative to mobility.
	 */
    private int evaluateWeight(int depth, int moves, int weight) {
        if (moves == 0) moves = -5;
        if (depth == 3) {
            return moves + weight;
        } else if (depth == 2) {
            return moves * 4 + weight;
        } else if (depth == 1) {
            return moves + weight;
        } else if (depth == 0) {
            return moves * 4 + weight;
        }
        return 0;
    }

    /**
	 * This is the most important function for the AI. It makes a recursive search for the best move.
	 * The critical point of this algorithm is to make a relevant heuristic weighing of each step.
	 * The step that accumulates most weight will be chosen. Notice also that the weight of every second
	 * step is subtracted from the accumulated weight so that the step that generates most weight for the own
	 * player and the less weight for the other player is chosen.
	 */
    public void evaluateMove(int level, int depth, int prune, Result r[], String ind) {
        int i, j, weight;
        Result r0 = depth > 0 ? r[depth - 1] : null, r1 = r[depth], r2 = depth == level ? null : r[depth + 1];
        r1.resetWeight();
        iterations++;
        for (j = 0; j < 8; j++) for (i = 0; i < 8; i++) {
            calculate_weight(i, j, r1);
            if (r1.count > 1) {
                r1.addMove(i, j);
            }
        }
        if (r1.moves > 0 || (depth > 0 && depth < level)) {
            for (i = r1.moves == 0 ? 0 : r1.moves - 1; i >= 0; i--) {
                weight = r1.moves_point[i] + r1.moves_capture[i];
                if (depth < level) {
                    if (depth > 0 && r0.max_weight >= 0 && prune - evaluateWeight(depth - 1, r1.moves, weight) < r0.moves_weight[r0.max_weight]) ; else {
                        work = board[depth + 1];
                        copy(work, board[depth]);
                        if (i < r1.moves) set(r1.moves_x[i], r1.moves_y[i], r1.color);
                        evaluateMove(level, depth + 1, weight, r, ind + "   ");
                        r1.setMoves(i, r2.moves);
                        work = board[depth];
                        weight -= evaluateWeight(depth, r2.moves, r2.weight);
                    }
                }
                r1.setWeight(i, weight);
            }
        }
        r1.setAverageWeight();
    }

    /**
	 * This function calculates the endgame and searches for the best average result for the last few moves.
	 * 
	 * No need for alphabeta-pruning because it searches for all possible endresults.
	 */
    public void evaluateEnd(int level, int depth, Result r[], String ind) {
        int i, j, blank, count;
        Result r1 = r[depth], r2 = depth == level ? null : r[depth + 1];
        r1.resetCount();
        iterations++;
        for (j = 0, blank = 0; j < 8; j++) for (i = 0; i < 8; i++) {
            if (work[i + j * 8] == BLANK) blank++;
            calculate_weight(i, j, r1);
            if (r1.count > 1) {
                r1.addMove(i, j);
            }
        }
        if (blank == 0 || (r1.moves == 0 && r[depth - 1].moves == 0)) {
            for (i = 0, r1.count = 0, r1.color = r[0].color; i < 64; i++) if (work[i] == r1.color) r1.count++;
            r1.setCount(0, r1.count);
        } else if (blank > 0 && depth < level && (r1.moves > 0 || depth > 0)) {
            for (i = r1.moves == 0 ? 0 : r1.moves - 1; i >= 0; i--) {
                work = board[depth + 1];
                copy(work, board[depth]);
                if (i < r1.moves) set(r1.moves_x[i], r1.moves_y[i], r1.color);
                evaluateEnd(level, depth + 1, r, ind + "   ");
                r1.setMoves(i, r2.moves);
                work = board[depth];
                r1.setCount(i, r2.count);
            }
        } else r1.setCount(0, 0);
        r1.setAverageCount();
    }

    /**
	 * Calculates the strategic weight of a point in the result. This mean that a certain
	 * point can have more strategic value if it's surrounded by the opponent than if
	 * surrounded by blanks.
	 */
    private int strategicWeight(int x, int y, byte color) {
        if ((x == 0 && y > 0 && y < 7) || (y == 0 && x > 0 && x < 7) || (x == 7 && y > 0 && y < 7) || (y == 7 && x > 0 && x < 7)) {
            int i;
            byte c = color == BLACK ? WHITE : BLACK;
            if (x == 0) for (i = 0, x = y; i < 8; i++) row[i] = work[i * 8]; else if (y == 0) for (i = 0; i < 8; i++) row[i] = work[i]; else if (x == 7) for (i = 0, x = y; i < 8; i++) row[i] = work[7 + i * 8]; else for (i = 0; i < 8; i++) row[i] = work[56 + i];
            if (row[x - 1] == c && row[x + 1] == c) return 4; else if (row[x - 1] == c) {
                int x1, x2;
                for (x1 = x - 2; x1 >= 0; x1--) if (row[x1] != c) break;
                for (x2 = x + 1; x2 <= 7; x2++) if (row[x2] != color) break;
                if (x1 < 0 || row[x1] == BLANK || (x1 > 0 && row[x1 - 1] == c)) return x2 > 7 ? 0 : (row[x2] == BLANK ? -32 : 16); else if (x1 > 0 && row[x1 - 1] == BLANK) return x2 > 7 ? 0 : (row[x2] == BLANK ? 16 : -32);
                return 8;
            } else if (row[x + 1] == c) {
                int x1, x2;
                for (x1 = x + 2; x1 <= 7; x1++) if (row[x1] != c) break;
                for (x2 = x - 1; x2 >= 0; x2--) if (row[x2] != color) break;
                if (x1 > 7 || row[x1] == BLANK || (x1 < 7 && row[x1 + 1] == c)) return x2 < 0 ? 0 : (row[x2] == BLANK ? -32 : 16); else if (x1 < 7 && row[x1 + 1] == BLANK) return x2 < 0 ? 0 : (row[x2] == BLANK ? 16 : -32);
                return 8;
            }
            return 0;
        }
        return 0;
    }

    /**
	 * Set location [x,y] to [color].
	 */
    public void set(int x, int y, byte color) {
        if (work[x + y * 8] != BLANK) return;
        byte c = color == BLACK ? WHITE : BLACK;
        marker_x = x;
        marker_y = y;
        if (y > 0) {
            if (x > 0 && work[x - 1 + (y - 1) * 8] == c) set_dir(x, y, -1, -1, color);
            if (work[x + (y - 1) * 8] == c) set_dir(x, y, 0, -1, color);
            if (x < 7 && work[x + 1 + (y - 1) * 8] == c) set_dir(x, y, 1, -1, color);
        }
        if (x > 0 && work[x - 1 + y * 8] == c) set_dir(x, y, -1, 0, color);
        if (x < 7 && work[x + 1 + y * 8] == c) set_dir(x, y, 1, 0, color);
        if (y < 7) {
            if (x > 0 && work[x - 1 + (y + 1) * 8] == c) set_dir(x, y, -1, 1, color);
            if (work[x + (y + 1) * 8] == c) set_dir(x, y, 0, 1, color);
            if (x < 7 && work[x + 1 + (y + 1) * 8] == c) set_dir(x, y, 1, 1, color);
        }
        work[x + y * 8] = color;
    }

    /**
	 * Set the direction [ix,iy] at location [x,y] to [color].
	 */
    private void set_dir(int x, int y, int ix, int iy, byte color) {
        int n = 0, i, j, bx = ix == -1 ? -1 : 8, by = iy == -1 ? -8 : 64;
        byte c;
        iy *= 8;
        for (i = x + ix, j = y * 8 + iy; true; i += ix, j += iy) {
            if (i == bx || j == by || (c = work[i + j]) == BLANK) return;
            if (c == color) break;
            n++;
        }
        if (n == 0) return;
        for (i = x + ix, j = y * 8 + iy; true; i += ix, j += iy) {
            if (work[i + j] == color) break;
            work[i + j] = color;
        }
    }

    /**
	 * Check if the location [x,y] accept a coin of [color].
	 */
    public boolean check(int x, int y, byte color) {
        if (work[x + y * 8] != BLANK) return false;
        byte c = color == BLACK ? WHITE : BLACK;
        if (y > 0) {
            if ((x > 0 && work[x - 1 + (y - 1) * 8] == c && check_dir(x, y, -1, -1, color)) || (work[x + (y - 1) * 8] == c && check_dir(x, y, 0, -1, color)) || (x < 7 && work[x + 1 + (y - 1) * 8] == c && check_dir(x, y, 1, -1, color))) return true;
        }
        if ((x > 0 && work[x - 1 + y * 8] == c && check_dir(x, y, -1, 0, color)) || (x < 7 && work[x + 1 + y * 8] == c && check_dir(x, y, 1, 0, color))) return true;
        if (y < 7) {
            if ((x > 0 && work[x - 1 + (y + 1) * 8] == c && check_dir(x, y, -1, 1, color)) || (work[x + (y + 1) * 8] == c && check_dir(x, y, 0, 1, color)) || (x < 7 && work[x + 1 + (y + 1) * 8] == c && check_dir(x, y, 1, 1, color))) return true;
        }
        return false;
    }

    /**
	 * Check if the direction [ix,iy] at location [x,y] accept a coin of [color].
	 */
    private boolean check_dir(int x, int y, int ix, int iy, byte color) {
        int n = 0, i, j, bx = ix == -1 ? -1 : 8, by = iy == -1 ? -8 : 64;
        byte c;
        iy *= 8;
        for (i = x + ix, j = y * 8 + iy; true; i += ix, j += iy) {
            if (i == bx || j == by || (c = work[i + j]) == BLANK) return false;
            if (c == color) break;
            n++;
        }
        return n > 0;
    }

    /**
	 * Calculate weight at location [x,y] for [r.color].
	 */
    public void calculate_weight(int x, int y, Result r) {
        r.count = 0;
        r.point = 0;
        r.capture = 0;
        if (work[x + y * 8] != BLANK) return;
        byte c = r.color == BLACK ? WHITE : BLACK;
        if (y > 0) {
            if (x > 0 && work[x - 1 + (y - 1) * 8] == c) calculate_weight_dir(x, y, -1, -1, r);
            if (work[x + (y - 1) * 8] == c) calculate_weight_dir(x, y, 0, -1, r);
            if (x < 7 && work[x + 1 + (y - 1) * 8] == c) calculate_weight_dir(x, y, 1, -1, r);
        }
        if (x > 0 && work[x - 1 + y * 8] == c) calculate_weight_dir(x, y, -1, 0, r);
        if (x < 7 && work[x + 1 + y * 8] == c) calculate_weight_dir(x, y, 1, 0, r);
        if (y < 7) {
            if (x > 0 && work[x - 1 + (y + 1) * 8] == c) calculate_weight_dir(x, y, -1, 1, r);
            if (work[x + (y + 1) * 8] == c) calculate_weight_dir(x, y, 0, 1, r);
            if (x < 7 && work[x + 1 + (y + 1) * 8] == c) calculate_weight_dir(x, y, 1, 1, r);
        }
        r.count++;
        r.point = weight_table[x + y * 8] + strategicWeight(x, y, r.color);
    }

    /**
	 * Calculate weight for direction [ix,iy] at location [x,y] for [r.color].
	 */
    private void calculate_weight_dir(int x, int y, int ix, int iy, Result r) {
        int i, j, n = 0, w = 0, bx = ix == -1 ? -1 : 8, by = iy == -1 ? -8 : 64;
        byte c;
        iy *= 8;
        for (i = x + ix, j = y * 8 + iy; true; i += ix, j += iy) {
            if (i == bx || j == by || (c = work[i + j]) == BLANK) return;
            if (c == r.color) break;
            n++;
            w += weight_table[i + j];
        }
        if (n > 0) {
            r.count += n;
            r.capture += w;
        }
    }
}
