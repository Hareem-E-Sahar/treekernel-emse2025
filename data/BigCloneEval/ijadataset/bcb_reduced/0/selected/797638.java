package ezsudoku.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import com.act365.sudoku.LeastCandidatesHybrid;
import com.act365.sudoku.SuDokuUtils;
import com.act365.sudoku.Grid;
import ezsudoku.GamingOptions;
import ezsudoku.SkinManager;
import ezsudoku.Skin;
import ezsudoku.Play;
import ezsudoku.model.PlateCoords;
import ezsudoku.model.RegionModel;
import ezsudoku.model.PlateModel;
import ezsudoku.model.ItemModel;
import ezsudoku.view.GamingOptionMenu;
import ezsudoku.view.PlateStatusBar;
import ezsudoku.view.ItemPopupMenu;
import ezsudoku.view.CandidateBar;
import ezsudoku.view.PlateView;
import ezsudoku.view.NumberBar;
import ezsudoku.view.ItemView;
import ezsudoku.view.ToolBar;

/**
 * Controls interaction between GUI and model by listening
 * graphical element change and routing item model change to GUI handlers.
 *
 * @author Cedric Chantepie (cchantepie@corsaire.fr)
 */
public final class PlateControler implements ValueFilter {

    /**
     */
    private static final int TIMER_DELAY = 1000;

    /**
     */
    private PlateModel model = null;

    /**
     */
    private PlateView view = null;

    /**
     */
    private Preferences prefs = null;

    /**
     * Play timer.
     */
    private Timer timer = null;

    /**
     */
    private PlayedTimeListener timeListener = null;

    /**
     */
    private HashMap itemCoords = null;

    /**
     */
    private HashSet[] lineItems = null;

    /**
     */
    private HashSet[] colItems = null;

    /**
     */
    private HashSet[][] regionItems = null;

    /**
     */
    private Play play = null;

    /**
     * History of commands executed by this controller.
     */
    private CommandHistory history = null;

    /**
     */
    private StrategyManager strategyManager = null;

    /**
     */
    private ChangeValueActionListener valActionListener = null;

    /**
     */
    CandidateActionListener candiActionListener = null;

    /**
     */
    private ItemRolloverListener itemRollListener = null;

    /**
     */
    private DefaultCandidateListener candidateListener = null;

    /**
     */
    private StrategyDeclListener strategyListener = null;

    /**
     * Current solution.
     * [null]
     */
    private byte[][] solution = null;

    /**
     * {@inheritDocs}
     */
    public void actionPerformed(ActionEvent evt) {
        String commandStr = evt.getActionCommand();
        Request request = new Request(commandStr);
        System.out.println("[PlateControler] actionPerformed" + "#command.actionName=" + request.getActionName());
        if (ChangeValueActionListener.ACTION.equals(request.getActionName())) {
            this.valActionListener.actionPerformed(evt);
        }
    }

    /**
     */
    public boolean accept(Object value, Object userData) {
        if (value == null) {
            return true;
        }
        Integer val = (Integer) value;
        PlateCoords coords = (PlateCoords) userData;
        return this.model.isCandidate(coords, val);
    }

    /**
     * Returns value from solution for |coords|.
     */
    public Integer getSolution(final PlateCoords coords) {
        int value = (int) this.solution[coords.getColumn()][coords.getLine()];
        if (value == 0) {
            return null;
        }
        return new Integer(value);
    }

    /**
     * Get current game play.
     */
    public Play getPlay() {
        return this.play;
    }

    /**
     * Filter candidate whose item has a set value.
     */
    public PlateCoords[] getCandidateCoordinates(final Integer candidate) {
        PlateCoords[] coords = this.model.getCandidateCoordinates(candidate);
        if (coords == null) {
            return new PlateCoords[0];
        }
        ArrayList list = new ArrayList(coords.length);
        for (int i = 0; i < coords.length; i++) {
            if (this.getValue(coords[i]) != null) {
                continue;
            }
            list.add(coords[i]);
        }
        return (PlateCoords[]) list.toArray(new PlateCoords[list.size()]);
    }

    /**
     * Returns value pending to be set.
     */
    public Integer getPendingValue() {
        return this.valActionListener.getValue();
    }

    /**
     */
    public PlateView getView() {
        return this.view;
    }

    /**
     */
    public PlateModel getModel() {
        return this.model;
    }

    /**
     */
    protected void setModel(final PlateModel model) {
        if (model == null) {
            this.model = null;
            this.solution = null;
            return;
        }
        this.model = model;
        PlateCoords coords;
        Integer val;
        String plate = null;
        byte[][] data = new byte[PlateModel.LINE_NUMBER][PlateModel.COLUMN_NUMBER];
        int boxh = (int) Math.sqrt(PlateModel.COLUMN_NUMBER);
        int boxl = (int) Math.sqrt(PlateModel.LINE_NUMBER);
        Grid grid = new Grid(boxh, boxl);
        for (int l = 0, c = 0; l < PlateModel.LINE_NUMBER; l++) {
            for (; c < PlateModel.COLUMN_NUMBER; c++) {
                coords = new PlateCoords(c, l);
                val = this.model.getValue(coords);
                data[l][c] = (val == null) ? 0 : (byte) val.intValue();
            }
            c = 0;
        }
        plate = SuDokuUtils.toString(data, boxh, PlateModel.COLUMN_NUMBER * PlateModel.LINE_NUMBER, SuDokuUtils.NUMERIC);
        grid.populate(plate);
        LeastCandidatesHybrid strat = new LeastCandidatesHybrid(true, true);
        int sol = -1;
        int total = PlateModel.LINE_NUMBER * PlateModel.COLUMN_NUMBER;
        while (true) {
            sol = grid.solve(strat, 128);
            if (grid.countFilledCells() == total) {
                break;
            } else {
                grid.populate(plate);
            }
        }
        if (sol > 1) {
            System.err.println("Multiple solution");
        }
        SuDokuUtils.populate(data, grid.toString());
        this.solution = new byte[PlateModel.COLUMN_NUMBER][PlateModel.LINE_NUMBER];
        for (int l = 0; l < PlateModel.LINE_NUMBER; l++) {
            for (int c = 0; c < PlateModel.COLUMN_NUMBER; c++) {
                this.solution[c][l] = data[l][c];
            }
        }
        this.strategyManager.assertModel(this.model);
    }

    /**
     */
    public StrategyManager getStrategyManager() {
        return this.strategyManager;
    }

    /**
     */
    public Integer getValue(final PlateCoords coords) {
        return this.model.getValue(coords);
    }

    /**
     * Set |value| for item at |coords| in the model.
     */
    public Integer setValue(final PlateCoords coords, final Integer value) {
        Integer oldValue = this.model.setValue(coords, value);
        return oldValue;
    }

    /**
     * Get line in which is |item|.
     */
    public ItemView[] getLine(final ItemView view) {
        PlateCoords coords = this.getCoordinates(view);
        int line = coords.getLine();
        return (ItemView[]) this.lineItems[line].toArray(new ItemView[this.lineItems[line].size()]);
    }

    /**
     * Get line in which is |item|.
     */
    public ItemView[] getColumn(final ItemView view) {
        PlateCoords coords = this.getCoordinates(view);
        int col = coords.getColumn();
        return (ItemView[]) this.colItems[col].toArray(new ItemView[this.colItems[col].size()]);
    }

    /**
     * Get region corresponding to |regionCol| and |regionLine|.
     */
    public ItemView[] getRegion(int regionCol, int regionLine) {
        HashSet region = this.regionItems[regionCol][regionLine];
        return (ItemView[]) region.toArray(new ItemView[region.size()]);
    }

    /**
     */
    public PlateCoords getCoordinates(final ItemView item) {
        return (PlateCoords) this.itemCoords.get(item);
    }

    /**
     */
    public GamingOptions getOptions() {
        return this.play.getOptions();
    }

    /**
     * @see ezsudoku.view.PlateView#getItemView(PlateCoords)
     */
    public ItemView getItemView(final PlateCoords coords) {
        if (this.view == null) {
            return null;
        }
        return this.view.getItemView(coords);
    }

    /**
     * {@inheritDocs}
     */
    public boolean execute(final Command command) {
        command.execute();
        this.history.add(command);
        return true;
    }

    /**
     * Register |view|.
     */
    public void setView(final PlateView view) {
        PlateCoords coords = null;
        ItemView itemView = null;
        Integer value = null;
        this.view = view;
        this.view.setSkin(SkinManager.getInstance().getSkin(this.prefs.getSkin()));
        int regionCol = -1;
        int regionLine = -1;
        for (int c = 0; c < PlateModel.COLUMN_NUMBER; c++) {
            for (int l = 0; l < PlateModel.LINE_NUMBER; l++) {
                coords = new PlateCoords(c, l);
                value = this.getValue(coords);
                itemView = this.view.getItemView(coords);
                itemView.setMutable(true);
                itemView.setValue(value);
                itemView.highlightDeadEnd(false);
                this.itemCoords.put(itemView, coords);
                if (this.lineItems[l] == null) {
                    this.lineItems[l] = new HashSet();
                }
                this.lineItems[l].add(itemView);
                if (this.colItems[c] == null) {
                    this.colItems[c] = new HashSet();
                }
                this.colItems[c].add(itemView);
                regionCol = c / RegionModel.HOR_LENGTH;
                regionLine = l / RegionModel.VERT_LENGTH;
                if (this.regionItems[regionCol][regionLine] == null) {
                    this.regionItems[regionCol][regionLine] = new HashSet();
                }
                this.regionItems[regionCol][regionLine].add(itemView);
                if (!this.model.isMutable(coords)) {
                    itemView.setMutable(false);
                } else {
                    itemView.setCandidates(this.model.getCandidates(coords));
                }
                itemView.clearMouseListeners();
                itemView.addMouseListener(new ItemMouseListener(coords));
            }
        }
        GamingOptionMenu optionMenu = view.getOptionMenu();
        GamingOptions options = this.play.getOptions();
        String[] strategyNames = this.strategyListener.getStrategyNames();
        optionMenu.setAutoCandidate(options.isAutoCandidate());
        optionMenu.setDisplayDeadEnd(options.isDisplayDeadEnd());
        for (int i = 0; i < strategyNames.length; i++) {
            optionMenu.addStrategyItem(strategyNames[i]);
        }
        optionMenu.setStrategyActionListener(new StrategyActionListener(this));
        SkinManager skinManager = SkinManager.getInstance();
        Skin[] skins = skinManager.getSkins();
        String name;
        String displayName;
        for (int i = 0; i < skins.length; i++) {
            name = skins[i].getName();
            displayName = skins[i].getDisplayName();
            view.addSkin(name, displayName);
        }
        view.selectCurrentSkin();
        NumberBar topBar = view.getTopToolbar();
        ToolBar toolbar = view.getToolBar();
        CandidateBar candidateBar = view.getCandidateBar();
        ItemPopupMenu popup = view.getItemPopupMenu();
        topBar.setMouseListener(new NumberBarMouseListener());
        topBar.clearActionListeners();
        topBar.addActionListener(this.valActionListener);
        topBar.addActionListener(this.candiActionListener);
        popup.clearValueActionListeners();
        popup.clearValueMouseListeners();
        popup.addValueActionListener(this.valActionListener);
        popup.addValueMouseListener(new PopupMouseListener(this));
        view.setUndoAction(new UndoAction(this.history));
        view.setRedoAction(new RedoAction(this.history));
        view.setOpenAction(new OpenAction(this));
        view.setSaveAction(new SaveAction(this));
        view.setNewAction(new NewAction(this));
        view.setQuitAction(new QuitAction(this));
        view.setAutoCandidateAction(new SetAutoCandidateAction(this));
        view.setDisplayDeadEndAction(new SetDisplayDeadEndAction(this));
        view.setHelpAction(new HelpAction(this));
        view.setSolveAction(new SolveAction(this));
        view.setRollAction(new RollStepAction(this.history));
        view.setStepAction(new MarkStepAction(this.history));
        view.setHintAction(new HintAction(this));
        view.setSelectSkinAction(new SelectSkinAction(this));
        view.setAddSkinAction(new AddSkinAction(this));
        resetStatusBar();
    }

    /**
     * Solve all plate items.
     * @todo suspend StrategyManager
     */
    public void solveAll() {
        ItemView itemView;
        PlateCoords coords;
        Integer curval;
        Integer val;
        Command cmd;
        this.strategyManager.suspend();
        this.view.clearSpotlights();
        for (int c = 0; c < PlateModel.COLUMN_NUMBER; c++) {
            for (int l = 0; l < PlateModel.LINE_NUMBER; l++) {
                coords = new PlateCoords(c, l);
                itemView = this.view.getItemView(coords);
                if (!itemView.isMutable()) {
                    continue;
                }
                val = new Integer((int) this.solution[c][l]);
                curval = this.getValue(coords);
                cmd = new ChangeValueCommand(this, coords, curval, val);
                this.execute(cmd);
            }
        }
        this.view.refreshSpotlights();
        this.strategyManager.restart();
    }

    /**
     * Set model for |items|.
     */
    public void setItems(ItemModel[][] items) {
        setModel(new PlateModel(items, this.candidateListener));
        this.itemCoords = new HashMap();
    }

    /**
     */
    private void resetStatusBar() {
        PlateStatusBar statusbar = this.view.getStatusBar();
        statusbar.setMutableNumber(this.model.getMutableNumber());
        statusbar.setFullItemNumber(this.model.getFilledItemNumber());
    }

    /**
     */
    private void prepareView() {
        this.valActionListener = new ChangeValueActionListener(this);
        this.candiActionListener = new CandidateActionListener(this);
        this.itemRollListener = new ItemRolloverListener(this);
        this.candidateListener = new DefaultCandidateListener(this);
        this.lineItems = new HashSet[PlateModel.LINE_NUMBER];
        this.colItems = new HashSet[PlateModel.COLUMN_NUMBER];
        this.regionItems = new HashSet[PlateModel.HOR_LENGTH][PlateModel.VERT_LENGTH];
        this.history = new CommandHistory();
    }

    /**
     * Load |play|.
     * 
     * @param play Game play
     */
    public void load(final Play play) {
        setModel(play.getModel());
        this.play = play;
        this.candidateListener = (DefaultCandidateListener) model.getCandidateListener();
        this.candidateListener.controller = this;
        PlayedTimeListener l = new PlayedTimeListener(this, TIMER_DELAY);
        if (this.timer != null) {
            this.timer.stop();
            this.timer.removeActionListener(this.timeListener);
            this.timer.addActionListener(l);
            this.timer.restart();
        } else {
            this.timer = new Timer(TIMER_DELAY, l);
            this.timer.start();
        }
        this.timeListener = l;
        this.history.clear();
        resetStatusBar();
        this.setView(this.view);
    }

    /**
     */
    private void startStrategyManager() {
        this.strategyManager = new StrategyManager(this);
        StrategyList list = this.strategyManager.getStrategyNames();
        this.strategyListener = new StrategyDeclListener(this);
        list.addPropertyChangeListener(this.strategyListener);
    }

    /**
     */
    protected void setPref(final String prefName, final String prefValue) {
        this.prefs.setProperty(prefName, prefValue);
    }

    /**
     */
    private void loadPrefs() {
        File repository = new File(System.getProperty("user.home") + File.separator + ".ezsudoku");
        File prefFile = new File(repository, File.separator + "prefs.properties");
        if (!repository.exists()) {
            repository.mkdirs();
        }
        if (!prefFile.exists()) {
            this.prefs = new Preferences();
            this.prefs.setSkin("default");
            savePrefs(prefFile);
            return;
        }
        FileInputStream in = null;
        try {
            this.prefs = new Preferences();
            in = new FileInputStream(prefFile);
            this.prefs.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     */
    protected void savePrefs() {
        File repository = new File(System.getProperty("user.home") + File.separator + ".ezsudoku");
        File prefFile = new File(repository, File.separator + "prefs.properties");
        if (!repository.exists()) {
            repository.mkdirs();
        }
        savePrefs(prefFile);
    }

    /**
     */
    private void savePrefs(final File prefFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(prefFile);
            this.prefs.store(out, "Easy sudoku preferences");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sole constructor.
     */
    public PlateControler() {
        loadPrefs();
        prepareView();
        startStrategyManager();
    }

    /**
     */
    public PlateControler(GamingOptions options, ItemModel[][] items) {
        loadPrefs();
        prepareView();
        startStrategyManager();
        this.setItems(items);
        this.play = new Play(this.getModel(), options);
        this.timeListener = new PlayedTimeListener(this, TIMER_DELAY);
        this.timer = new Timer(TIMER_DELAY, timeListener);
        timer.start();
    }

    /**
     * @author Cedric Chantepie (cchantepie@corsaire.fr)
     */
    private class ItemMouseListener extends MouseAdapter {

        /**
	 */
        private PlateCoords coords = null;

        /**
	 */
        public void mouseClicked(MouseEvent evt) {
            Object src = evt.getSource();
            boolean rightClick = (evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0;
            boolean ctrlPressed = (evt.getModifiers() & InputEvent.CTRL_MASK) != 0;
            boolean popupTrigger = !rightClick || ctrlPressed;
            if (src instanceof ItemView) {
                if (!popupTrigger) {
                    ActionEvent actionEvt = new ActionEvent(this.coords, -1, "ItemView:setItemValue");
                    valActionListener.actionPerformed(actionEvt);
                } else {
                    ItemView item = (ItemView) src;
                    Integer[] candidates = null;
                    ItemPopupMenu popup = view.getItemPopupMenu();
                    if (play.getOptions().isAutoCandidate()) {
                        PlateCoords coords = getCoordinates(item);
                        candidates = model.getCandidates(coords);
                    } else {
                        candidates = new Integer[ItemModel.INITIAL_CANDIDATES.length];
                        for (int i = 0; i < candidates.length; i++) {
                            candidates[i] = new Integer(ItemModel.INITIAL_CANDIDATES[i]);
                        }
                    }
                    view.showItemPopupMenu(item, evt.getX(), evt.getY());
                    popup.setCandidates(candidates);
                }
            }
            if (!popupTrigger) {
                view.hideItemPopupMenu();
            }
        }

        /**
	 * {@inheritDoc}
	 */
        public void mouseEntered(MouseEvent evt) {
            Object src = evt.getSource();
            if (src instanceof ItemView) {
                itemRollListener.mouseEntered(evt);
            }
        }

        /**
	 * {@inheritDoc}
	 */
        public void mouseExited(MouseEvent evt) {
            Object src = evt.getSource();
            if (src instanceof ItemView) {
                itemRollListener.mouseExited(evt);
            }
        }

        /**
	 * @param coords Coordinates of manitored item.
	 */
        public ItemMouseListener(PlateCoords coords) {
            this.coords = coords;
        }
    }
}
