package passreminder;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import passreminder.action.LanguageAction;
import passreminder.action.OpenAction;
import passreminder.ui.QuickMasterDialog;

public class PassReminder extends ApplicationWindow implements I18Nable {

    public static final boolean DEBUG = false;

    public static final String WEBSITE = "http://eyecanseeyou.free.fr";

    public static final String NAME = "PassReminder";

    public static final String VERSION = "1.0";

    public static final String DATE = "20060718";

    public static final String PLATFORM = "Desktop";

    public static String MEDIA_SUPPORT = "HardDrive";

    public static String PREFERENCE_FILENAME = "passreminder.preferences";

    public Map actionManager = null;

    private Composite mainComposite = null;

    private SashForm mainSashForm = null;

    private Composite statusBarComposite = null;

    private Label messageLabel = null;

    private Label iconLabel = null;

    private Composite folderManagerComposite = null;

    private Tree folderTree = null;

    private Composite itemManagerComposite = null;

    private Table itemTable = null;

    private Composite detailItemComposite = null;

    private TreeViewer folderTreeViewer = null;

    private TableViewer itemTableViewer = null;

    private StyledText itemDetailTextArea = null;

    private GridData itemDetailTextAreaGridData = null;

    public Map itemManager = null;

    static {
        String userhome = "";
        if (System.getProperty("user.home") != null) userhome = System.getProperty("user.home");
        PREFERENCE_FILENAME = userhome + "/passreminder.preferences";
    }

    private static PassReminder me = null;

    static {
        me = new PassReminder();
    }

    public static PassReminder getInstance() {
        return me;
    }

    public PassReminder() {
        super(null);
        itemManager = new HashMap();
    }

    private void createMainComposite(Composite parent) {
        GridLayout mainCompositeGridLayout = new GridLayout();
        mainCompositeGridLayout.numColumns = 1;
        mainCompositeGridLayout.horizontalSpacing = 0;
        mainCompositeGridLayout.verticalSpacing = 0;
        mainCompositeGridLayout.marginWidth = 0;
        mainCompositeGridLayout.marginHeight = 0;
        mainCompositeGridLayout.makeColumnsEqualWidth = false;
        GridData mainCompositeGridData = new GridData();
        mainCompositeGridData.horizontalAlignment = GridData.FILL;
        mainCompositeGridData.grabExcessHorizontalSpace = true;
        mainCompositeGridData.grabExcessVerticalSpace = true;
        mainCompositeGridData.horizontalSpan = 2;
        mainCompositeGridData.verticalAlignment = GridData.FILL;
        mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(mainCompositeGridData);
        createMainSashForm();
        mainComposite.setLayout(mainCompositeGridLayout);
    }

    private void createMainSashForm() {
        GridData mainSashGridData = new GridData();
        mainSashGridData.horizontalAlignment = GridData.FILL;
        mainSashGridData.grabExcessHorizontalSpace = true;
        mainSashGridData.grabExcessVerticalSpace = true;
        mainSashGridData.verticalAlignment = GridData.FILL;
        mainSashForm = new SashForm(mainComposite, SWT.NONE | SWT.SMOOTH);
        mainSashForm.setLayoutData(mainSashGridData);
        createFolderManagerComposite();
        createItemManagerComposite();
        mainSashForm.setWeights(new int[] { PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_LEFTSIDE), PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_RIGHTSIDE) });
    }

    private void createStatusBarComposite(Composite parent) {
        GridData gridData10 = new GridData();
        gridData10.widthHint = 0;
        GridData gridData2 = new GridData();
        gridData2.horizontalSpan = 2;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        statusBarComposite = new Composite(parent, SWT.NONE);
        statusBarComposite.setLayout(gridLayout2);
        statusBarComposite.setLayoutData(gridData2);
        iconLabel = new Label(statusBarComposite, SWT.NONE);
        iconLabel.setText("");
        iconLabel.setLayoutData(gridData10);
        messageLabel = new Label(statusBarComposite, SWT.NONE);
        messageLabel.setText("Bienvenue dans l'application PassReminder !");
    }

    private void createFolderManagerComposite() {
        GridData folderActionTextGridData = new GridData();
        folderActionTextGridData.horizontalAlignment = GridData.FILL;
        folderActionTextGridData.grabExcessHorizontalSpace = true;
        folderActionTextGridData.verticalAlignment = GridData.CENTER;
        GridLayout folderManagerGridLayout = new GridLayout();
        folderManagerGridLayout.horizontalSpacing = 0;
        folderManagerGridLayout.marginWidth = 0;
        folderManagerGridLayout.marginHeight = 0;
        folderManagerGridLayout.numColumns = 3;
        folderManagerGridLayout.makeColumnsEqualWidth = false;
        folderManagerGridLayout.verticalSpacing = 0;
        GridData folderTreeGridData = new GridData();
        folderTreeGridData.horizontalAlignment = GridData.FILL;
        folderTreeGridData.grabExcessHorizontalSpace = true;
        folderTreeGridData.grabExcessVerticalSpace = true;
        folderTreeGridData.horizontalSpan = 3;
        folderTreeGridData.verticalAlignment = GridData.FILL;
        folderManagerComposite = new Composite(mainSashForm, SWT.NONE);
        folderManagerComposite.setLayout(folderManagerGridLayout);
        folderTree = new Tree(folderManagerComposite, SWT.BORDER | SWT.MULTI);
        folderTree.setLayoutData(folderTreeGridData);
        folderTreeViewer = new TreeViewer(folderTree);
    }

    private void createItemManagerComposite() {
        GridData itemTableGridData = new GridData();
        itemTableGridData.horizontalAlignment = GridData.FILL;
        itemTableGridData.grabExcessHorizontalSpace = true;
        itemTableGridData.grabExcessVerticalSpace = true;
        itemTableGridData.verticalAlignment = GridData.FILL;
        GridLayout gridLayout4 = new GridLayout();
        gridLayout4.horizontalSpacing = 0;
        gridLayout4.marginWidth = 0;
        gridLayout4.marginHeight = 0;
        gridLayout4.numColumns = 1;
        gridLayout4.verticalSpacing = 0;
        itemManagerComposite = new Composite(mainSashForm, SWT.NONE);
        itemManagerComposite.setLayout(gridLayout4);
        itemTable = new Table(itemManagerComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        itemTable.setHeaderVisible(true);
        itemTable.setLayoutData(itemTableGridData);
        itemTable.setLinesVisible(true);
        itemTableViewer = new TableViewer(itemTable);
        final TableColumn column1 = new TableColumn(itemTable, SWT.NONE);
        itemManager.put("service", column1);
        column1.setWidth(PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_COLUMN_SERVICE));
        column1.setText(Messages.getString("service"));
        column1.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int sortDir = itemTableViewer.getTable().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                itemTableViewer.setSorter(new ItemTableSorter(UIManager.COLUMN_POS_SERVICE, sortDir));
                itemTableViewer.getTable().setSortColumn(column1);
                itemTableViewer.getTable().setSortDirection(sortDir);
            }
        });
        UIManager.getInstance().listenColumnResize(column1);
        final TableColumn column2 = new TableColumn(itemTable, SWT.NONE);
        itemManager.put("user", column2);
        column2.setWidth(PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_COLUMN_USER));
        column2.setText(Messages.getString("user"));
        column2.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int sortDir = itemTableViewer.getTable().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                itemTableViewer.setSorter(new ItemTableSorter(UIManager.COLUMN_POS_USER, sortDir));
                itemTableViewer.getTable().setSortColumn(column2);
                itemTableViewer.getTable().setSortDirection(sortDir);
            }
        });
        UIManager.getInstance().listenColumnResize(column2);
        TableColumn column = new TableColumn(itemTable, SWT.NONE);
        itemManager.put("password", column);
        column.setWidth(PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_COLUMN_PASSWORD));
        column.setText(Messages.getString("password"));
        UIManager.getInstance().listenColumnResize(column);
        final TableColumn column4 = new TableColumn(itemTable, SWT.NONE);
        itemManager.put("url", column4);
        column4.setWidth(PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_COLUMN_COMMAND));
        column4.setText(Messages.getString("url"));
        column4.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int sortDir = itemTableViewer.getTable().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                itemTableViewer.setSorter(new ItemTableSorter(UIManager.COLUMN_POS_COMMAND, sortDir));
                itemTableViewer.getTable().setSortColumn(column4);
                itemTableViewer.getTable().setSortDirection(sortDir);
            }
        });
        UIManager.getInstance().listenColumnResize(column4);
        final TableColumn column5 = new TableColumn(itemTable, SWT.NONE);
        itemManager.put("group", column5);
        column5.setWidth(0);
        column5.setText(Messages.getString("group"));
        column5.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int sortDir = itemTableViewer.getTable().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                itemTableViewer.setSorter(new ItemTableSorter(UIManager.COLUMN_POS_GROUP, sortDir));
                itemTableViewer.getTable().setSortColumn(column5);
                itemTableViewer.getTable().setSortDirection(sortDir);
            }
        });
        UIManager.getInstance().listenColumnResize(column5);
        createDetailItemComposite();
    }

    private void createDetailItemComposite() {
        itemDetailTextAreaGridData = new GridData();
        itemDetailTextAreaGridData.horizontalAlignment = GridData.FILL;
        itemDetailTextAreaGridData.grabExcessHorizontalSpace = true;
        itemDetailTextAreaGridData.heightHint = 53;
        itemDetailTextAreaGridData.verticalAlignment = GridData.CENTER;
        GridLayout gridLayout5 = new GridLayout();
        gridLayout5.numColumns = 1;
        gridLayout5.verticalSpacing = 0;
        gridLayout5.marginWidth = 0;
        gridLayout5.marginHeight = 0;
        gridLayout5.horizontalSpacing = 0;
        GridData gridData9 = new GridData();
        gridData9.horizontalAlignment = GridData.FILL;
        gridData9.verticalAlignment = GridData.FILL;
        detailItemComposite = new Composite(itemManagerComposite, SWT.NONE);
        detailItemComposite.setLayoutData(gridData9);
        detailItemComposite.setLayout(gridLayout5);
        itemDetailTextArea = new StyledText(detailItemComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
        itemDetailTextArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        itemDetailTextArea.setLayoutData(itemDetailTextAreaGridData);
    }

    protected Control createContents(Composite parent) {
        createMainComposite(parent);
        createStatusBarComposite(parent);
        createHandle();
        int width = PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_WIDTH);
        int height = PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_HEIGHT);
        if ((PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_X) != 0) && (PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_Y) != 0) && PreferenceManager.getInstance().getPreferenceStore().getBoolean(PreferenceConstants.WINDOW_GEOMETRY)) getShell().setLocation(PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_X), PreferenceManager.getInstance().getPreferenceStore().getInt(PreferenceConstants.WINDOW_Y));
        if (width != 0 && height != 0 && PreferenceManager.getInstance().getPreferenceStore().getBoolean(PreferenceConstants.WINDOW_GEOMETRY)) getShell().setSize(width, height); else getShell().setSize(540, 500);
        return folderTreeViewer.getTree();
    }

    public void createHandle() {
        ModelManager.groupData.name = Messages.getString("tree.mydata");
        ModelManager.groupTrash.name = Messages.getString("tree.trash");
        ModelManager.groupSearch.name = Messages.getString("tree.search");
        DBManager.getInstance().gListMain.add(ModelManager.groupData);
        DBManager.getInstance().gListMain.add(ModelManager.groupTrash);
        DBManager.getInstance().gListMain.add(ModelManager.groupSearch);
        UIManager.getInstance().itemDetailTextArea = itemDetailTextArea;
        folderTreeViewer.setContentProvider(new FolderTreeContentProvider());
        folderTreeViewer.setLabelProvider(new FolderTreeLabelProvider());
        folderTreeViewer.setInput(DBManager.getInstance().gListMain);
        folderTreeViewer.setSorter(new FolderTreeSorter());
        UIManager.getInstance().folderTreeViewer = folderTreeViewer;
        folderTreeViewer.getTree().setMenu(createTreeContextMenuManager().createContextMenu(folderTreeViewer.getTree()));
        itemTableViewer.setContentProvider(new ItemTableContentProvider());
        itemTableViewer.setLabelProvider(new ItemTableLabelProvider());
        itemTableViewer.addFilter(new AllowOnlyGroupFilter());
        itemTableViewer.setInput(DBManager.getInstance().iListMain);
        itemTableViewer.setSorter(new ItemTableSorter(UIManager.COLUMN_POS_SERVICE, SWT.UP));
        itemTableViewer.getTable().setSortColumn(itemTableViewer.getTable().getColumn(UIManager.COLUMN_POS_SERVICE));
        itemTableViewer.getTable().setSortDirection(SWT.UP);
        UIManager.getInstance().itemTableViewer = itemTableViewer;
        itemTableViewer.getTable().setMenu(createTableContextMenuManager().createContextMenu(itemTableViewer.getTable()));
    }

    protected MenuManager createTableContextMenuManager() {
        MenuManager bar = new MenuManager("");
        bar.add((Action) actionManager.get("addElement"));
        bar.add((Action) actionManager.get("viewElement"));
        bar.add((Action) actionManager.get("editElement"));
        bar.add((Action) actionManager.get("removeElement"));
        bar.add((Action) actionManager.get("duplicate"));
        bar.add((Action) actionManager.get("restoreItem"));
        bar.add(PRMenuToolbar.getInstance().moveToGroupMenu);
        bar.add(new Separator());
        bar.add((Action) actionManager.get("search"));
        bar.add(new Separator());
        bar.add((Action) actionManager.get("smartCopy"));
        bar.add((Action) actionManager.get("copyPassword"));
        bar.add((Action) actionManager.get("copyUser"));
        bar.add(new Separator());
        bar.add((Action) actionManager.get("passwordColumn"));
        bar.add(new Separator());
        bar.add((Action) actionManager.get("refresh"));
        return bar;
    }

    protected MenuManager createTreeContextMenuManager() {
        MenuManager bar = new MenuManager("");
        bar.add((Action) actionManager.get("addGroup"));
        bar.add((Action) actionManager.get("editGroup"));
        bar.add((Action) actionManager.get("deleteGroup"));
        bar.add(PRMenuToolbar.getInstance().moveGroupToGroupMenu);
        bar.add(new Separator());
        bar.add((Action) actionManager.get("refresh"));
        return bar;
    }

    protected MenuManager createMenuManager() {
        MenuManager bar = new MenuManager("");
        PRMenuToolbar.getInstance().fileMenu = new MenuManager(Messages.getString("item.file"), "filemenu");
        MenuManager editMenu = new MenuManager(Messages.getString("item.edit"), "editmenu");
        MenuManager winMenu = new MenuManager(Messages.getString("item.window"), "winmenu");
        MenuManager helpMenu = new MenuManager(Messages.getString("item.help"), "helpmenu");
        bar.add(PRMenuToolbar.getInstance().fileMenu);
        bar.add(editMenu);
        bar.add(winMenu);
        bar.add(helpMenu);
        PRMenuToolbar.getInstance().updateRecents();
        editMenu.add((Action) actionManager.get("addElement"));
        editMenu.add((Action) actionManager.get("viewElement"));
        editMenu.add((Action) actionManager.get("editElement"));
        editMenu.add((Action) actionManager.get("removeElement"));
        editMenu.add((Action) actionManager.get("duplicate"));
        editMenu.add((Action) actionManager.get("restoreItem"));
        editMenu.add(PRMenuToolbar.getInstance().moveToGroupMenu);
        editMenu.add(PRMenuToolbar.getInstance().moveGroupToGroupMenu);
        editMenu.add(new Separator());
        editMenu.add((Action) actionManager.get("addGroup"));
        editMenu.add((Action) actionManager.get("editGroup"));
        editMenu.add((Action) actionManager.get("deleteGroup"));
        editMenu.add(new Separator());
        editMenu.add((Action) actionManager.get("search"));
        editMenu.add(new Separator());
        editMenu.add((Action) actionManager.get("smartCopy"));
        editMenu.add((Action) actionManager.get("copyPassword"));
        editMenu.add((Action) actionManager.get("copyUser"));
        editMenu.add(new Separator());
        editMenu.add((Action) actionManager.get("clearCb"));
        winMenu.add((Action) actionManager.get("passwordColumn"));
        winMenu.add((Action) actionManager.get("iconify"));
        winMenu.add(new Separator());
        MenuManager passlistSubmenu = new MenuManager(Messages.getString("item.passlist"));
        winMenu.add(passlistSubmenu);
        winMenu.add(new Separator());
        winMenu.add((Action) actionManager.get("preferences"));
        File[] fileList = new File(LanguageAction.LANGUAGE_DIRECTORY).listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].getName().startsWith("messages") && !fileList[i].isDirectory()) {
                try {
                    String[] fileItem = fileList[i].getName().split("_");
                    if (fileItem.length == 3) {
                        Locale locale = new Locale(fileItem[1], fileItem[2].replaceAll(".properties", ""));
                        Action langaction = new LanguageAction(locale.getLanguage() + "-" + locale.getCountry(), locale.getLanguage(), locale.getCountry(), actionManager);
                        langaction.setChecked(PreferenceManager.getInstance().getPreferenceStore().getString(PreferenceConstants.LANG).equals(locale.getLanguage()) && PreferenceManager.getInstance().getPreferenceStore().getString(PreferenceConstants.COUNTRY).equals(locale.getCountry()));
                        helpMenu.add(langaction);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        helpMenu.add(new Separator());
        if (new File("help").exists()) helpMenu.add((Action) actionManager.get("help"));
        helpMenu.add((Action) actionManager.get("website"));
        helpMenu.add(new Separator());
        helpMenu.add((Action) actionManager.get("about"));
        return bar;
    }

    protected CoolBarManager createCoolBarManager(int style) {
        CoolBarManager manager = super.createCoolBarManager(style);
        manager.add(getSaveCoolBar(style));
        manager.add(getCopyCoolBar(style));
        manager.add(getGroupCoolBar(style));
        manager.add(getEntryCoolBar(style));
        manager.add(getHelpCoolBar(style));
        return manager;
    }

    private ToolBarContributionItem getSaveCoolBar(int style) {
        final ToolBarManager bar = new ToolBarManager(style);
        bar.add((Action) actionManager.get("new"));
        bar.add((Action) actionManager.get("clearAndOpen"));
        bar.add((Action) actionManager.get("save"));
        ToolBarContributionItem coolbar = new ToolBarContributionItem(bar, "fileio");
        return coolbar;
    }

    private ToolBarContributionItem getCopyCoolBar(int style) {
        final ToolBarManager bar = new ToolBarManager(style);
        bar.add((Action) actionManager.get("smartCopy"));
        bar.add((Action) actionManager.get("copyUser"));
        bar.add((Action) actionManager.get("copyPassword"));
        bar.add((Action) actionManager.get("clearCb"));
        ToolBarContributionItem coolbar = new ToolBarContributionItem(bar, "copyio");
        return coolbar;
    }

    private ToolBarContributionItem getEntryCoolBar(int style) {
        final ToolBarManager bar = new ToolBarManager(style);
        bar.add((Action) actionManager.get("addElement"));
        bar.add((Action) actionManager.get("viewElement"));
        bar.add((Action) actionManager.get("editElement"));
        bar.add((Action) actionManager.get("removeElement"));
        bar.add((Action) actionManager.get("duplicate"));
        ToolBarContributionItem coolbar = new ToolBarContributionItem(bar, "entry");
        return coolbar;
    }

    private ToolBarContributionItem getGroupCoolBar(int style) {
        final ToolBarManager bar = new ToolBarManager(style);
        bar.add((Action) actionManager.get("addGroup"));
        bar.add((Action) actionManager.get("editGroup"));
        bar.add((Action) actionManager.get("deleteGroup"));
        ToolBarContributionItem coolbar = new ToolBarContributionItem(bar, "group");
        return coolbar;
    }

    private ToolBarContributionItem getHelpCoolBar(int style) {
        final ToolBarManager bar = new ToolBarManager(style);
        bar.add((Action) actionManager.get("help"));
        ToolBarContributionItem coolbar = new ToolBarContributionItem(bar, "help");
        return coolbar;
    }

    public void updateLanguage() {
        refreshTitle();
        ModelManager.groupData.name = Messages.getString("tree.mydata");
        ModelManager.groupTrash.name = Messages.getString("tree.trash");
        ModelManager.groupSearch.name = Messages.getString("tree.search");
        getMenuBarManager().getMenu().getItem(0).setText(Messages.getString("item.file"));
        getMenuBarManager().getMenu().getItem(1).setText(Messages.getString("item.edit"));
        getMenuBarManager().getMenu().getItem(2).setText(Messages.getString("item.window"));
        getMenuBarManager().getMenu().getItem(3).setText(Messages.getString("item.help"));
        Iterator ite = itemManager.keySet().iterator();
        while (ite.hasNext()) {
            String key = (String) ite.next();
            Item item = (Item) itemManager.get(key);
            item.setText(Messages.getString(key));
        }
        passreminder.model.Item item = (passreminder.model.Item) (UIManager.getInstance().itemTableViewer.getElementAt(UIManager.getInstance().itemTableViewer.getTable().getSelectionIndex()));
        UIManager.getInstance().refreshItem(item, false);
    }

    public void refreshTitle() {
        String f = !DBManager.getInstance().isDBOpened() ? Messages.getString("untitled") : Messages.getString("title.file", DBManager.getInstance().dbFile);
        getShell().setText(PassReminder.NAME + (MEDIA_SUPPORT.equals("MEMORY_STICK") ? " MS " : "") + VERSION + " " + f);
    }

    public void setStatus(String message) {
        getInstance().getStatusLineManager().setErrorMessage(null);
        getInstance().getStatusLineManager().setMessage(message);
    }

    public void setError(String message) {
        getInstance().getStatusLineManager().setErrorMessage(Util.getImageRegistry().get("warning"), message);
    }

    public boolean close() {
        return false;
    }

    public boolean exit() {
        PreferenceManager.getInstance().getPreferenceStore().setValue(PreferenceConstants.WINDOW_X, getShell().getLocation().x);
        PreferenceManager.getInstance().getPreferenceStore().setValue(PreferenceConstants.WINDOW_Y, getShell().getLocation().y);
        PreferenceManager.getInstance().getPreferenceStore().setValue(PreferenceConstants.WINDOW_WIDTH, getShell().getSize().x);
        PreferenceManager.getInstance().getPreferenceStore().setValue(PreferenceConstants.WINDOW_HEIGHT, getShell().getSize().y);
        PreferenceManager.getInstance().getPreferenceStore().setValue(PreferenceConstants.WINDOW_LEFTSIDE, mainSashForm.getWeights()[0]);
        PreferenceManager.getInstance().getPreferenceStore().setValue(PreferenceConstants.WINDOW_RIGHTSIDE, mainSashForm.getWeights()[1]);
        UIManager.getInstance().saveFolderTreeSate();
        try {
            PreferenceManager.getInstance().getPreferenceStore().save();
        } catch (Exception e) {
        }
        try {
            if (!DBManager.getInstance().isMetadataSaved && PreferenceManager.getInstance().getPreferenceStore().getBoolean(PreferenceConstants.APPLICATION_SAVE_META_ONEXIT)) {
                if (QuickMasterDialog.showQuestion(Messages.getString("title.be_careful"), Messages.getString("question.ui.save_changes")) == SWT.YES) DBManager.getInstance().saveMetadata();
            }
        } catch (Exception e) {
            e.printStackTrace();
            QuickMasterDialog.showError(Messages.getString("title.error"), e.getMessage());
        }
        return super.close();
    }

    public static void main(String[] args) {
        String _f = null;
        if (args.length > 0) _f = args[0];
        if (System.getProperty("passreminder.ms") != null && System.getProperty("passreminder.ms").equals("MS")) {
            MEDIA_SUPPORT = "MEMORY_STICK";
            PREFERENCE_FILENAME = "passreminder.preferences";
        }
        Messages.update(PreferenceManager.getInstance().getPreferenceStore().getString(PreferenceConstants.LANG), PreferenceManager.getInstance().getPreferenceStore().getString(PreferenceConstants.COUNTRY));
        getInstance().actionManager = new HashMap();
        PRMenuToolbar.getInstance().createAction();
        getInstance().addMenuBar();
        getInstance().addCoolBar(SWT.FLAT | SWT.WRAP);
        getInstance().addStatusLine();
        int bitwin = SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE;
        if (PreferenceManager.getInstance().getPreferenceStore().getBoolean(PreferenceConstants.ALWAYS_ON_TOP)) bitwin |= SWT.ON_TOP;
        getInstance().setShellStyle(bitwin);
        try {
            getInstance().create();
            getInstance().refreshTitle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getInstance().getShell().setVisible(true);
        getInstance().getShell().setImage(Util.getImageRegistry().get("logo"));
        PRMenuToolbar.getInstance().createTray();
        getInstance().setStatus(Messages.getString("password_prompt"));
        ((OpenAction) getInstance().actionManager.get("clearAndOpen")).openFileAtStart(_f);
        getInstance().activateHandle();
        UIManager.getInstance().setTreeFocus();
        UIManager.getInstance().restoreFolderTreeSate();
        getInstance().setBlockOnOpen(true);
        getInstance().open();
        Display.getCurrent().dispose();
    }

    private void activateHandle() {
        ShellListener.install(getShell());
        ElementsListener.install();
        installFolderDragAndDrop();
        installItemToFolderDragAndDrop();
        installEasyTreeEdition();
        installEasyTableEdition();
        installItemTableSorting();
        installFolderTreeSorting();
    }

    /**
	 * Move a folder into another one
	 */
    private void installFolderDragAndDrop() {
    }

    /**
	 * Move an item or several items into another folder.
	 * 
	 */
    private void installItemToFolderDragAndDrop() {
    }

    /**
	 * Change the folder name without prompting a dialog. Directly in the tree.
	 */
    private void installEasyTreeEdition() {
    }

    /**
	 * Change the service, user, password and url without opening the edit entry
	 * dialog.
	 */
    private void installEasyTableEdition() {
    }

    /**
	 * Sort the item list, without the column selected.
	 */
    private void installItemTableSorting() {
    }

    /**
	 * Sort the tree by folder name.
	 * 
	 */
    private void installFolderTreeSorting() {
    }
}
