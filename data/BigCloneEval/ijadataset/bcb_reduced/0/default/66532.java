import javax.swing.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

/**
 * A class that creates the user interface and communicates between the UI and the game.
 * @author Joni Toivanen (jomiolto@gmail.com)
 */
public class MainWindow extends JFrame implements KeyListener {

    /**
	 * Menu bar of the window.
	 */
    private JMenuBar menubar;

    /**
	 * The text displayed in the input box when asking for the user's name for the top list.
	 */
    private String topListInput = "Please enter your name";

    /**
	 * "File" menu in menu bar.
	 */
    private FileMenu fileMenu;

    /**
	 * "Help" menu in menu bar.
	 */
    private HelpMenu helpMenu;

    /**
	 * "Settings" menu in menu bar.
	 */
    private SettingsMenu settingsMenu;

    /**
	 * Component that shows the game grid.
	 */
    private GridComponent gameGrid;

    /**
	 * Component that shows the top scores.
	 */
    private TopListComponent topList;

    /**
	 * The name of the configuration variable that holds the default window size.
	 */
    public static final String WINDOW_SIZE = "window_size";

    /**
	 * The default width of the game window
	 */
    public static final int WINDOW_DEFAULT_X = 600;

    /**
	 * The default height of the game window
	 */
    public static final int WINDOW_DEFAULT_Y = 400;

    /**
	 * The minimum width of the game window. This should be set to some sane minimum at which the game is
	 * still playable.
	 */
    public static final int WINDOW_MINIMUM_X = 400;

    /**
	 * The minimum height of the game window. This should be set to some sane minimum at which the game is
	 * still playable.
	 */
    public static final int WINDOW_MINIMUM_Y = 300;

    /**
	 * The file name of the quick save file.
	 */
    private final String QUICK_SAVE = "quick.sav";

    /**
	 * The current game mode, see the *_MODE constants below.
	 */
    private int gameMode;

    /**
	 * The number of entries in the top list.
	 */
    private final int topEntries = 10;

    /**
	 * In top score list mode.
	 */
    private final int TOP_LIST_MODE = 1;

    /**
	 * In game mode.
	 */
    private final int GAME_MODE = 2;

    /**
	 * In replay mode.
	 */
    private final int REPLAY_MODE = 3;

    /**
	 * Constructs the window and the user interface.
	 * @title title of the window
	 */
    public MainWindow(String title) {
        super(title);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        URL icon = MainWindow.class.getResource("jlad.png");
        if (icon != null) this.setIconImage(new ImageIcon(icon).getImage());
        menubar = new JMenuBar();
        fileMenu = new FileMenu();
        helpMenu = new HelpMenu();
        settingsMenu = new SettingsMenu();
        menubar.add(fileMenu);
        menubar.add(settingsMenu);
        menubar.add(helpMenu);
        this.setResizable(true);
        this.addKeyListener(this);
        this.addWindowListener(new MainWindowListener());
        this.setJMenuBar(menubar);
        if (!this.checkQuickSave()) this.showTopList();
        this.setMinimumSize(new Dimension(WINDOW_MINIMUM_X, WINDOW_MINIMUM_Y));
        this.setInitialSize();
        this.setVisible(true);
        this.setInitialSize();
    }

    /**
	 * Updates the score list if in top list mode.
	 */
    public void ruleSetChanged() {
        if (!this.inTopList()) return;
        this.showTopList();
    }

    /**
	 * Returns the name of the top list with current rule set.
	 * @return the file name of the rule set
	 */
    public String topListName() {
        return RulesClasses.getCurrentIdentifier() + ".top";
    }

    /**
	 * Returns the current top list.
	 * @return an instance of TopList class that represents the current top list
	 */
    public TopList currentTopList() {
        TopList list = new TopList(this.topListName(), this.topEntries);
        boolean errors = false;
        for (int c = 0; c < this.topEntries; c++) {
            if (list.getScore(c) == 0) break;
            if (list.getScore(c) == Replay.validate(list.getValidation(c))) continue;
            Debug.msg("Score not valid! (\"" + list.getValidation(c) + "\", " + list.getScore(c) + ")\n");
            list.removeScore(c);
            c--;
            if (!errors) {
                Dialogues.warning("Incorrect score in top-10 found!\n" + "If you're trying to hack the score file then I'm sorry,\n" + "it's not that easy.\nIf you haven't tampered with\n" + "the scores, then there was an error...");
                errors = true;
            }
        }
        return list;
    }

    /**
	 * Returns a component that shows the current top list.
	 * @return a component that shows the top list for the currently selected rule set
	 */
    public TopListComponent currentTopListComponent() {
        TopListComponent list = new TopListComponent(this.currentTopList());
        list.setHeader("Top-10 for the " + RulesClasses.getCurrentName() + " rule set:");
        return list;
    }

    /**
	 * Set the size of the window to its initial value (the one read from the configuration file, or, if that
	 * is not available, then the default window size).
	 */
    public void setInitialSize() {
        String size = JLad.config.getString(WINDOW_SIZE, "");
        if (size.compareToIgnoreCase("maximized") == 0) {
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            return;
        }
        if (size.length() > 0) {
            Dimension d = JLad.config.parseResolution(size);
            if (d != null) {
                this.setSize(d);
                return;
            } else Debug.msg("Error, couldn't parse " + WINDOW_SIZE + " variable!\n");
        }
        this.changeSize(WINDOW_DEFAULT_X, WINDOW_DEFAULT_Y);
    }

    /**
	 * Saves the window size to the configuration variables, so that it will be written to the config file and
	 * can be restored the next time the MainWindow is created.
	 */
    public void saveWindowSize() {
        if ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            JLad.config.addVariable(WINDOW_SIZE, "maximized");
            return;
        }
        JLad.config.addResolution(WINDOW_SIZE, this.getWidth(), this.getHeight());
    }

    /**
	 * Changes the size of the window.
	 * @param w the new width of the window
	 * @param h the new height of the window
	 */
    public void changeSize(int w, int h) {
        JLad.config.addResolution(WINDOW_SIZE, w, h);
        this.setSize(new Dimension(w, h));
    }

    /**
	 * Checks if there is a quick save and if the user wants to continue from it.
	 * @return true if there was a quick save and it was loaded
	 */
    public boolean checkQuickSave() {
        String quick = PathHelper.getHome(this.QUICK_SAVE);
        if (quick == null) return false;
        String msg = "Do you want to load a saved game?";
        String title = "Quick save found";
        int type = JOptionPane.YES_NO_OPTION;
        int choice = JOptionPane.showConfirmDialog(this, msg, title, type);
        if (choice != JOptionPane.YES_OPTION) return false;
        this.loadGame(quick);
        return true;
    }

    /**
	 * Tells whether we are in top score list or not.
	 * @return true if the game is currently showing the top score list
	 */
    public boolean inTopList() {
        if (this.gameMode == this.TOP_LIST_MODE) return true;
        return false;
    }

    /**
	 * Tells whether a game is in progress. Note that this returns false if a game has just finished, even if
	 * the game is still shown on the screen!
	 * @return true if a game is in progress
	 */
    public boolean inGame() {
        if (this.gameMode == this.GAME_MODE && !this.gameGrid.hasGameEnded()) return true;
        return false;
    }

    /**
	 * Tells whether a replay is in progress.
	 * @return true if currently a replay is shown
	 */
    public boolean inReplay() {
        if (this.gameMode == this.REPLAY_MODE) return true;
        return false;
    }

    /**
	 * Show a hint to the player.
	 */
    public void hint() {
        if (this.inGame()) this.gameGrid.toggleHint();
    }

    /**
	 * Save a screenshot.
	 */
    public void screenshot() {
        JFileChooser chooser = new JFileChooser(Preferences.getScreenshotPath());
        String fn;
        chooser.setFileFilter(new PNGFileFilter());
        int r = chooser.showSaveDialog(JLad.getMainWindow());
        if (r != JFileChooser.APPROVE_OPTION) return;
        try {
            fn = chooser.getSelectedFile().getCanonicalPath();
            Preferences.setScreenshotPath(chooser.getCurrentDirectory().getAbsolutePath());
        } catch (IOException e) {
            Debug.msg("IOException while saving a screenshot: " + e.getMessage() + "\n");
            Dialogues.error("Could not save the screenshot!");
            return;
        } catch (SecurityException e) {
            Debug.msg("SecurityException while saving a screenshot: " + e.getMessage() + "\n");
            Dialogues.error("Could not save the screenshot!");
            return;
        }
        if (!fn.endsWith(".png")) fn = fn.concat(".png");
        File file = new File(fn);
        int type = BufferedImage.TYPE_INT_RGB;
        Component c;
        if (this.gameGrid != null) c = this.gameGrid; else if (this.topList != null) c = this.topList; else {
            Debug.msg("Something weird going on in MainWindow.screenshot()\n");
            Dialogues.error("Could not save the screenshot!");
            return;
        }
        int w = c.getWidth();
        int h = c.getHeight();
        try {
            BufferedImage image = new BufferedImage(w, h, type);
            c.paint(image.getGraphics());
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            Debug.msg("IOExceptien while saving a screenshot: " + e.getMessage() + "\n");
            Dialogues.error("Could not save the screenshot!");
        }
    }

    /**
	 * Tells if a game has just ended.
	 * @return true if a game has just ended
	 */
    public boolean hasGameEnded() {
        if (!this.inGame()) return false;
        return this.gameGrid.hasGameEnded();
    }

    /**
	 * Starts a new game.
	 */
    public void startGame() {
        if (this.inGame()) return;
        this.initMode(this.GAME_MODE, new GridComponent(RulesClasses.getRulesClass(), false));
    }

    /**
	 * Checks if the given score is enough for top list, and if it is, adds it to the list (prompts the user
	 * for a name).
	 * @param score the score to check
	 * @return true if the score was added
	 */
    public boolean checkTopScore(int score) {
        TopList topScores = this.currentTopList();
        if (topScores.checkScore(score)) {
            JFrame frame = JLad.getMainWindow();
            String msg = "You made it to the top-10!";
            String title = "Please enter your name";
            int mtype = JOptionPane.QUESTION_MESSAGE;
            this.topListInput = (String) JOptionPane.showInputDialog(frame, msg, title, mtype, null, null, this.topListInput);
            if (this.topListInput != null) {
                topScores.addScore(score, this.topListInput, this.gameGrid.getRules().toString());
                topScores.save();
                topScores = null;
                return true;
            }
        }
        topScores = null;
        return false;
    }

    /**
	 * Show the top score list.
	 */
    public void showTopList() {
        this.initMode(this.TOP_LIST_MODE, this.currentTopListComponent());
    }

    /**
	 * Inits the given mode.
	 * @param mode the mode to initialize
	 * @param component the component that displays the mode
	 */
    public void initMode(int mode, Component component) {
        if (mode != this.TOP_LIST_MODE && mode != this.GAME_MODE && mode != this.REPLAY_MODE) {
            Debug.msg("Trying to init bad mode \"" + mode + "\" in MainWindow.initMode()\n");
            return;
        }
        if (mode == this.TOP_LIST_MODE && !(component instanceof TopListComponent)) Debug.msg("Bad component in MainWindow.initMode(), TopListComponent expected!\n"); else if ((mode == this.GAME_MODE || mode == this.REPLAY_MODE) && !(component instanceof GridComponent)) Debug.msg("Bad component in MainWindow.initMode(), GridComponent expected!\n");
        if (this.gameGrid != null) {
            this.getContentPane().remove(this.gameGrid);
            this.gameGrid = null;
        }
        if (this.topList != null) {
            this.getContentPane().remove(this.topList);
            this.topList = null;
        }
        if (mode == this.TOP_LIST_MODE) {
            this.topList = (TopListComponent) component;
            this.getContentPane().add(component);
        } else if (mode == this.GAME_MODE || mode == this.REPLAY_MODE) {
            this.gameGrid = (GridComponent) component;
            this.getContentPane().add(component);
        }
        this.gameMode = mode;
        if (mode == this.TOP_LIST_MODE) this.requestFocus(); else this.gameGrid.requestFocus();
        this.validate();
    }

    /**
	 * Checks if in game mode.
	 * @return true if in game mode
	 */
    public boolean inGameMode() {
        if (this.gameMode == this.GAME_MODE) return true;
        return false;
    }

    /**
	 * Called when a game is ending and does all the necessary checks.
	 */
    public void gameEnding() {
        if (!this.inGameMode()) return;
        this.checkTopScore(this.gameGrid.getScore());
        this.askSaveReplay();
    }

    /**
	 * Ask if the user wants to save a replay of the game they just played.
	 * @return true if the player saved a replay
	 */
    public boolean askSaveReplay() {
        JFrame frame = JLad.getMainWindow();
        String msg = "Do you want to save a replay of this game?";
        String title = "Save a replay?";
        int otype = JOptionPane.YES_NO_OPTION;
        int a = JOptionPane.showConfirmDialog(frame, msg, title, otype);
        if (a != JOptionPane.YES_OPTION) return false;
        JFileChooser chooser = new JFileChooser(Preferences.getReplayPath());
        ReplayFileFilter filter = new ReplayFileFilter();
        chooser.setFileFilter(filter);
        int v = chooser.showSaveDialog(this);
        if (v == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String fn = file.toString();
            if (!fn.endsWith(".rpl")) {
                fn = fn.concat(".rpl");
                file = new File(fn);
            }
            Preferences.setReplayPath(chooser.getCurrentDirectory().getAbsolutePath());
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                this.gameGrid.saveReplay(writer);
                writer.close();
            } catch (IOException e) {
                Debug.msg("IOException while saving a replay: " + e.getMessage() + "\n");
                Dialogues.error("Could not save the replay!");
                return false;
            }
        }
        return true;
    }

    /**
	 * Ends the game.
	 */
    public void endGame() {
        if (!this.inGame() && !this.inReplay()) return;
        if (this.inGame()) {
            this.gameEnding();
            this.showTopList();
        }
    }

    /**
	 * Called when the user requested to end the game, checks that it's what the user really wants before
	 * actually ending the game.
	 */
    public void endGameRequested() {
        if (this.inReplay()) {
            this.showTopList();
            return;
        }
        if (!this.inGame()) return;
        JFrame frame = JLad.getMainWindow();
        String question = "Are you sure you want to end the current game?";
        String title = "End game?";
        int otype = JOptionPane.YES_NO_OPTION;
        int mtype = JOptionPane.QUESTION_MESSAGE;
        int a = JOptionPane.showOptionDialog(frame, question, title, otype, mtype, null, null, null);
        if (a != JOptionPane.YES_OPTION) return;
        JLad.getMainWindow().endGame();
    }

    /**
	 * Loads a game from the given file.
	 * @param fn the full path name of the save file to load
	 */
    public void loadGame(String fn) {
        Rules rules = null;
        try {
            BufferedReader br = new BufferedReader((new FileReader(fn)));
            String line = br.readLine();
            br.close();
            rules = GameLoader.loadGame(line);
            File file = new File(fn);
            file.delete();
        } catch (IOException e) {
            Debug.msg("IOException while loading a game: " + e.getMessage() + "\n");
            Dialogues.error("Could not load a saved game, read error!");
            return;
        } catch (BadReplayException e) {
            Debug.msg("BadReplayException while loading a game: " + e.getMessage() + "\n");
            Dialogues.error("Could not load a saved game, invalid replay file!");
            return;
        } catch (SecurityException e) {
            Debug.msg("SecurityException while loading a game: " + e.getMessage() + "\n");
            Dialogues.error("Could not load the saved game, security exception!");
            return;
        }
        if (rules == null) {
            Debug.msg("Something weird going on in MainWindow.loadGame()\n");
            return;
        }
        this.initMode(this.GAME_MODE, new GridComponent(rules, false));
    }

    /**
	 * Inits a replay.
	 */
    public void showReplay() {
        if (this.inGame()) return;
        ReplayFileFilter filter = new ReplayFileFilter();
        String path = Preferences.getReplayPath();
        JFileChooser fileChooser = new JFileChooser();
        if (path != null) fileChooser = new JFileChooser(path); else fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        int value = fileChooser.showOpenDialog(JLad.getMainWindow());
        if (value != JFileChooser.APPROVE_OPTION) return;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
            Preferences.setReplayPath(fileChooser.getCurrentDirectory().getAbsolutePath());
            String line = br.readLine();
            br.close();
            this.initMode(this.REPLAY_MODE, new GridComponent(line));
        } catch (IOException e) {
            Debug.msg("IOException while loading a replay: " + e.getMessage() + "\n");
            Dialogues.error("Could not load the replay, read error!");
        } catch (BadReplayException e) {
            Debug.msg("BadReplayExceptin while loading a replay: " + e.getMessage() + "\n");
            Dialogues.error("Could not load the replay, parse error!");
        }
    }

    /**
	 * Called when user tries to exit the game.
	 */
    public void exitGame() {
        this.saveWindowSize();
        if (!this.inGame()) JLad.quit();
        JFrame frame = JLad.getMainWindow();
        String msg = "Save the currect game before exiting?";
        String title = "Quitting the game...";
        int mtype = JOptionPane.QUESTION_MESSAGE;
        int otype = JOptionPane.YES_NO_CANCEL_OPTION;
        int a = JOptionPane.showConfirmDialog(frame, msg, title, otype, mtype);
        if (a == JOptionPane.CANCEL_OPTION || a == JOptionPane.CLOSED_OPTION) return;
        if (a == JOptionPane.YES_OPTION) this.gameGrid.saveGame(PathHelper.getHome(QUICK_SAVE, false));
        JLad.quit();
    }

    /**
	 * A class for handling the window events.
	 */
    private class MainWindowListener extends WindowAdapter {

        /**
		 * The window closed, exit the program.
		 * @param e the window event (not used)
		 */
        public void windowClosed(WindowEvent e) {
            MainWindow.this.saveWindowSize();
            JLad.quit();
        }

        /**
		 * The window is closing (user pressed the close button?).
		 * @param e the window event (not used)
		 */
        public void windowClosing(WindowEvent e) {
            MainWindow.this.saveWindowSize();
            MainWindow.this.exitGame();
        }
    }

    /**
	 * Key was typed.
	 * @param e the key event
	 */
    public void keyTyped(KeyEvent e) {
        if (this.inGame() || this.inReplay()) {
            this.gameGrid.keyTyped(e);
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_N || e.getKeyChar() == 'n' || e.getKeyCode() == KeyEvent.VK_F2) {
            this.startGame();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_Q || e.getKeyChar() == 'q') {
            this.exitGame();
            return;
        }
    }

    /**
	 * Key was pressed.
	 * @param e the key event
	 */
    public void keyPressed(KeyEvent e) {
        if (this.inGame() || this.inReplay()) {
            this.gameGrid.keyPressed(e);
            return;
        }
    }

    /**
	 * Key was released.
	 * @param e the key event
	 */
    public void keyReleased(KeyEvent e) {
        if (this.inGame() || this.inReplay()) {
            this.gameGrid.keyReleased(e);
            return;
        }
    }
}
