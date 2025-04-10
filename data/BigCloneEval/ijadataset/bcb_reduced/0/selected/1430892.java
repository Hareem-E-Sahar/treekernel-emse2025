package org.pwsafe.passwordsafeswt;

import static org.pwsafe.passwordsafeswt.preference.JpwPreferenceConstants.DISPLAY_AS_LIST_PREF;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pwsafe.lib.datastore.PwsEntryBean;
import org.pwsafe.lib.datastore.PwsEntryStore;
import org.pwsafe.lib.exception.PasswordSafeException;
import org.pwsafe.lib.file.PwsFieldTypeV1;
import org.pwsafe.lib.file.PwsFieldTypeV2;
import org.pwsafe.lib.file.PwsFieldTypeV3;
import org.pwsafe.lib.file.PwsFile;
import org.pwsafe.lib.file.PwsFileFactory;
import org.pwsafe.lib.file.PwsFileStorage;
import org.pwsafe.lib.file.PwsRecord;
import org.pwsafe.lib.file.PwsRecordV1;
import org.pwsafe.lib.file.PwsRecordV2;
import org.pwsafe.passwordsafeswt.action.AboutAction;
import org.pwsafe.passwordsafeswt.action.AddRecordAction;
import org.pwsafe.passwordsafeswt.action.ChangeSafeCombinationAction;
import org.pwsafe.passwordsafeswt.action.ClearClipboardAction;
import org.pwsafe.passwordsafeswt.action.CopyPasswordAction;
import org.pwsafe.passwordsafeswt.action.CopyURLAction;
import org.pwsafe.passwordsafeswt.action.CopyUsernameAction;
import org.pwsafe.passwordsafeswt.action.DeleteRecordAction;
import org.pwsafe.passwordsafeswt.action.EditRecordAction;
import org.pwsafe.passwordsafeswt.action.ExitAppAction;
import org.pwsafe.passwordsafeswt.action.ExportToTextAction;
import org.pwsafe.passwordsafeswt.action.ExportToXMLAction;
import org.pwsafe.passwordsafeswt.action.HelpAction;
import org.pwsafe.passwordsafeswt.action.ImportFromTextAction;
import org.pwsafe.passwordsafeswt.action.ImportFromXMLAction;
import org.pwsafe.passwordsafeswt.action.LockDbAction;
import org.pwsafe.passwordsafeswt.action.MRUFileAction;
import org.pwsafe.passwordsafeswt.action.NewFileAction;
import org.pwsafe.passwordsafeswt.action.OpenFileAction;
import org.pwsafe.passwordsafeswt.action.OpenFileReadWriteToggleAction;
import org.pwsafe.passwordsafeswt.action.OpenUrlAction;
import org.pwsafe.passwordsafeswt.action.OptionsAction;
import org.pwsafe.passwordsafeswt.action.SaveFileAction;
import org.pwsafe.passwordsafeswt.action.SaveFileAsAction;
import org.pwsafe.passwordsafeswt.action.UnlockDbAction;
import org.pwsafe.passwordsafeswt.action.ViewAsListAction;
import org.pwsafe.passwordsafeswt.action.ViewAsTreeAction;
import org.pwsafe.passwordsafeswt.action.VisitPasswordSafeWebsiteAction;
import org.pwsafe.passwordsafeswt.dialog.StartupDialog;
import org.pwsafe.passwordsafeswt.listener.TableColumnSelectionAdaptor;
import org.pwsafe.passwordsafeswt.listener.ViewerDoubleClickListener;
import org.pwsafe.passwordsafeswt.model.PasswordTableContentProvider;
import org.pwsafe.passwordsafeswt.model.PasswordTableLabelProvider;
import org.pwsafe.passwordsafeswt.model.PasswordTableSorter;
import org.pwsafe.passwordsafeswt.model.PasswordTreeContentProvider;
import org.pwsafe.passwordsafeswt.model.PasswordTreeLabelProvider;
import org.pwsafe.passwordsafeswt.preference.JpwPreferenceConstants;
import org.pwsafe.passwordsafeswt.preference.WidgetPreferences;
import org.pwsafe.passwordsafeswt.state.LockState;
import org.pwsafe.passwordsafeswt.util.IOUtils;
import org.pwsafe.passwordsafeswt.util.UserPreferences;
import org.pwsafe.passwordsafeswt.xml.XMLDataParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * A port of PasswordSafe to JFace. This is the main class that is launched from
 * the commandline.
 *
 * @author Glen Smith
 */
public class PasswordSafeJFace extends ApplicationWindow {

    private ImportFromXMLAction importFromXMLAction;

    private ExportToXMLAction exportToXMLAction;

    protected OptionsAction optionsAction;

    private TreeViewer treeViewer;

    private Tree tree;

    private HelpAction helpAction;

    protected AboutAction aboutAction;

    private ChangeSafeCombinationAction changeSafeCombinationAction;

    private VisitPasswordSafeWebsiteAction visitPasswordSafeWebsiteAction;

    protected ExitAppAction exitAppAction;

    private TableViewer tableViewer;

    private NewFileAction newFileAction;

    private DeleteRecordAction deleteRecordAction;

    private EditRecordAction editRecordAction;

    private AddRecordAction addRecordAction;

    private ClearClipboardAction clearClipboardAction;

    private CopyPasswordAction copyPasswordAction;

    private CopyUsernameAction copyUsernameAction;

    private CopyURLAction copyURLAction;

    private SaveFileAsAction saveFileAsAction;

    private SaveFileAction saveFileAction;

    private OpenFileAction openFileAction;

    private OpenFileReadWriteToggleAction openFileReadWriteToggleAction;

    private OpenUrlAction openUrlAction;

    private ViewAsListAction viewAsListAction;

    private ViewAsTreeAction viewAsTreeAction;

    private ExportToTextAction exportToTextAction;

    private ImportFromTextAction importFromTextAction;

    private LockDbAction lockDbAction;

    private UnlockDbAction unlockDbAction;

    private Table table;

    private SysTray systemTray;

    private Composite composite;

    private final LockState lockState = new LockState();

    private boolean readOnly = false;

    private final Timer lockTimer = new Timer("SWTPassword lock timer", true);

    private TimerTask lockTask;

    protected static final Log log = LogFactory.getLog(PasswordSafeJFace.class);

    public static final String APP_NAME = "PasswordSafeSWT";

    public static final String JPW_ICON = "jpwIcon";

    private static PasswordSafeJFace app;

    private PwsFile pwsFile;

    private PwsEntryStore dataStore;

    private static final String V1_GROUP_PLACEHOLDER = Messages.getString("PasswordSafeJFace.V1GroupPlaceholder");

    /**
	 * Constructor.
	 */
    public PasswordSafeJFace() {
        super(null);
        app = this;
        createActions();
        addToolBar(SWT.FLAT | SWT.WRAP);
        addMenuBar();
        addStatusLine();
        setEditMenusEnabled(false);
    }

    private void displayOpeningDialog() {
        IPreferenceStore prefs = JFacePreferences.getPreferenceStore();
        boolean openReadOnly = prefs.getBoolean(JpwPreferenceConstants.DEFAULT_OPEN_READ_ONLY);
        StartupDialog sd = new StartupDialog(getShell(), UserPreferences.getInstance().getMRUFiles(), openReadOnly);
        boolean allDone = false;
        while (!allDone) {
            String result = sd.open();
            if (result == StartupDialog.CANCEL || result == null) {
                exitApplication();
                return;
            } else if (result == StartupDialog.OPEN_FILE) {
                try {
                    openFile(sd.getFilename(), sd.getPassword());
                    this.setReadOnly(sd.getReadonly());
                    allDone = true;
                } catch (FileNotFoundException anEx) {
                    log.warn("File + " + sd.getFilename() + " not found.");
                    displayErrorDialog(Messages.getString("PasswordSafeJFace.OpenError.Title"), Messages.getString("PasswordSafeJFace.OpenError.NoFileFoundMessage") + sd.getFilename() + "]", anEx);
                    UserPreferences.getInstance().removeMRUFile(sd.getFilename());
                    allDone = false;
                } catch (Exception anEx) {
                    log.error("Exception on opening file + " + sd.getFilename(), anEx);
                    UserPreferences.getInstance().setMostRecentFilename(sd.getFilename());
                    displayErrorDialog(Messages.getString("PasswordSafeJFace.OpenError.Title"), Messages.getString("PasswordSafeJFace.OpenError.Message"), anEx);
                    allDone = false;
                }
            } else if (result == StartupDialog.NEW_FILE) {
                newFileAction.run();
                if (getPwsFile() != null) allDone = true;
            } else if (result == StartupDialog.OPEN_OTHER) {
                openFileAction.run();
                allDone = true;
            }
        }
        setupStatusMessage();
    }

    private void startOpeningDialogThread(final Shell shell) {
        shell.getDisplay().asyncExec(new Runnable() {

            public void run() {
                displayOpeningDialog();
            }
        });
    }

    /**
	 * Returns a singleton handle to the app for making business logic calls.
	 * 
	 * @return a handle to a singleton of this class
	 */
    public static PasswordSafeJFace getApp() {
        return app;
    }

    /**
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.VERTICAL));
        composite = new Composite(container, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.VERTICAL));
        final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
        if (thePrefs.getBoolean(DISPLAY_AS_LIST_PREF)) {
            addTableView(composite);
            viewAsListAction.setChecked(true);
            viewAsTreeAction.setChecked(false);
        } else {
            addTreeView(composite);
            viewAsTreeAction.setChecked(true);
            viewAsListAction.setChecked(false);
        }
        SysTray tray = new SysTray();
        final boolean isAvailable = tray.init(null);
        if (isAvailable) {
            systemTray = tray;
        }
        return container;
    }

    /**
	 * Creates the JFace actions that will be used for menus and toolbars.
	 */
    private void createActions() {
        newFileAction = new NewFileAction();
        openFileAction = new OpenFileAction();
        openFileReadWriteToggleAction = new OpenFileReadWriteToggleAction();
        saveFileAction = new SaveFileAction();
        saveFileAsAction = new SaveFileAsAction();
        exportToTextAction = new ExportToTextAction();
        importFromTextAction = new ImportFromTextAction();
        exitAppAction = new ExitAppAction();
        copyUsernameAction = new CopyUsernameAction();
        copyPasswordAction = new CopyPasswordAction();
        copyURLAction = new CopyURLAction();
        clearClipboardAction = new ClearClipboardAction();
        addRecordAction = new AddRecordAction();
        editRecordAction = new EditRecordAction();
        deleteRecordAction = new DeleteRecordAction();
        viewAsListAction = new ViewAsListAction();
        viewAsTreeAction = new ViewAsTreeAction();
        visitPasswordSafeWebsiteAction = new VisitPasswordSafeWebsiteAction();
        changeSafeCombinationAction = new ChangeSafeCombinationAction();
        aboutAction = new AboutAction();
        helpAction = new HelpAction();
        optionsAction = new OptionsAction();
        exportToXMLAction = new ExportToXMLAction();
        importFromXMLAction = new ImportFromXMLAction();
        lockDbAction = new LockDbAction();
        openUrlAction = new OpenUrlAction();
        unlockDbAction = new UnlockDbAction();
    }

    /**
	 * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
	 */
    @Override
    protected MenuManager createMenuManager() {
        MenuManager result = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.Menu"));
        final MenuManager menuManagerFile = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.File"));
        result.add(menuManagerFile);
        menuManagerFile.add(newFileAction);
        menuManagerFile.add(openFileAction);
        menuManagerFile.add(openFileReadWriteToggleAction);
        menuManagerFile.add(new Separator());
        List<String> mruFiles = UserPreferences.getInstance().getMRUFiles();
        if (mruFiles != null && mruFiles.size() > 0) {
            for (int i = 0; i < mruFiles.size(); i++) {
                String fileName = mruFiles.get(i);
                String menuItem = "&" + (i + 1) + " " + new File(fileName).getName();
                IAction nextMRUAction = new MRUFileAction(fileName, menuItem);
                menuManagerFile.add(nextMRUAction);
            }
            menuManagerFile.add(new Separator());
        }
        menuManagerFile.add(saveFileAction);
        menuManagerFile.add(saveFileAsAction);
        menuManagerFile.add(new Separator());
        final MenuManager menuManagerExportTo = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.ExportTo"));
        menuManagerFile.add(menuManagerExportTo);
        menuManagerExportTo.add(exportToTextAction);
        menuManagerExportTo.add(exportToXMLAction);
        final MenuManager menuManagerImportFrom = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.ImportFrom"));
        menuManagerFile.add(menuManagerImportFrom);
        menuManagerImportFrom.add(importFromTextAction);
        menuManagerImportFrom.add(importFromXMLAction);
        menuManagerFile.add(new Separator());
        menuManagerFile.add(exitAppAction);
        final MenuManager menuManagerEdit = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.Edit"));
        result.add(menuManagerEdit);
        menuManagerEdit.add(addRecordAction);
        menuManagerEdit.add(editRecordAction);
        menuManagerEdit.add(deleteRecordAction);
        menuManagerEdit.add(new Separator());
        menuManagerEdit.add(copyPasswordAction);
        menuManagerEdit.add(copyUsernameAction);
        menuManagerEdit.add(copyURLAction);
        menuManagerEdit.add(openUrlAction);
        menuManagerEdit.add(clearClipboardAction);
        final MenuManager menuManagerView = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.View"));
        result.add(menuManagerView);
        menuManagerView.add(viewAsListAction);
        menuManagerView.add(viewAsTreeAction);
        final MenuManager menuManagerManage = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.Manage"));
        result.add(menuManagerManage);
        menuManagerManage.add(changeSafeCombinationAction);
        menuManagerManage.add(new Separator());
        menuManagerManage.add(optionsAction);
        final MenuManager menuManagerHelp = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.Help"));
        result.add(menuManagerHelp);
        menuManagerHelp.add(helpAction);
        menuManagerHelp.add(new Separator());
        menuManagerHelp.add(visitPasswordSafeWebsiteAction);
        menuManagerHelp.add(new Separator());
        menuManagerHelp.add(aboutAction);
        return result;
    }

    /**
	 * Creates the popup (right click) menu for the given control.
	 * 
	 * @param ctl
	 *            the control to add the popup to
	 * @return the created menu
	 */
    protected Menu createPopupMenu(Control ctl) {
        final MenuManager menuListPopup = new MenuManager(Messages.getString("PasswordSafeJFace.Menu.ListPopup"));
        menuListPopup.createContextMenu(ctl);
        menuListPopup.add(copyPasswordAction);
        menuListPopup.add(copyUsernameAction);
        menuListPopup.add(copyURLAction);
        menuListPopup.add(openUrlAction);
        menuListPopup.add(new Separator());
        menuListPopup.add(addRecordAction);
        menuListPopup.add(editRecordAction);
        menuListPopup.add(deleteRecordAction);
        return menuListPopup.getMenu();
    }

    /**
	 * @see org.eclipse.jface.window.ApplicationWindow#createToolBarManager(int)
	 */
    @Override
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager toolBarManager = new ToolBarManager(style);
        toolBarManager.add(newFileAction);
        toolBarManager.add(openFileAction);
        toolBarManager.add(saveFileAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(copyUsernameAction);
        toolBarManager.add(copyPasswordAction);
        toolBarManager.add(copyURLAction);
        toolBarManager.add(openUrlAction);
        toolBarManager.add(clearClipboardAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(addRecordAction);
        toolBarManager.add(editRecordAction);
        toolBarManager.add(deleteRecordAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(helpAction);
        return toolBarManager;
    }

    /**
	 * @see org.eclipse.jface.window.ApplicationWindow#createStatusLineManager()
	 */
    @Override
    protected StatusLineManager createStatusLineManager() {
        StatusLineManager statusLineManager = new StatusLineManager();
        statusLineManager.setMessage("http://jpwsafe.sf.net");
        return statusLineManager;
    }

    public void setupStatusMessage() {
        final PwsFile pwsf = getPwsFile();
        if (pwsf != null && pwsf.getRecordCount() > 0) {
            final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
            if (thePrefs.getBoolean(JpwPreferenceConstants.DOUBLE_CLICK_COPIES_TO_CLIPBOARD)) {
                setStatus(Messages.getString("PasswordSafeJFace.Status.DoubleClickToCopy"));
            } else {
                setStatus(Messages.getString("PasswordSafeJFace.Status.DoubleClickToEdit"));
            }
        } else {
            setStatus("http://jpwsafe.sf.net");
        }
    }

    /**
	 * Main program entry point.
	 * 
	 * @param args
	 *            commandline args that are passed in
	 */
    public static void main(String args[]) {
        log.info("PasswordSafe starting...");
        log.info("java.library.path is: [" + System.getProperty("java.library.path") + "]");
        log.info("log: " + log.getClass().getName());
        try {
            PasswordSafeJFace window = new PasswordSafeJFace();
            window.setBlockOnOpen(true);
            window.open();
        } catch (Exception e) {
            log.error("Error Starting PasswordSafe", e);
        } finally {
            try {
                Display.getCurrent().dispose();
            } catch (Exception anEx1) {
            }
        }
        log.info("PasswordSafe terminating...");
    }

    /**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(PasswordSafeJFace.APP_NAME);
        Image jpwIcon = IOUtils.getImage(PasswordSafeJFace.class, "/org/pwsafe/passwordsafeswt/images/cpane.ico");
        newShell.setImage(jpwIcon);
        JFaceResources.getImageRegistry().put(JPW_ICON, jpwIcon);
        Window.setDefaultImage(jpwIcon);
        WidgetPreferences.tuneShell(newShell, getClass());
        startOpeningDialogThread(newShell);
    }

    /**
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
    @Override
    protected Point getInitialSize() {
        return new Point(425, 375);
    }

    /**
	 * Create a brand new empty safe.
	 * 
	 * @param password the password for the new safe
	 */
    public void newFile(final StringBuilder password) {
        getShell().setText(PasswordSafeJFace.APP_NAME + Messages.getString("PasswordSafeJFace.AppTitle.Default"));
        PwsFile newFile = PwsFileFactory.newFile();
        newFile.setPassphrase(password);
        this.setPwsFile(newFile);
        this.setReadOnly(false);
    }

    /**
	 * Opens a password safe from the file system.
	 * Readonly status stays unchanged.
	 * 
	 * @param fileName
	 * @param password
	 * @throws Exception
	 *             if bad things happen during open
	 */
    public void openFile(final String fileName, final StringBuilder password) throws Exception {
        this.openFile(fileName, password, isReadOnly());
    }

    /**
	 * Opens a password safe from the file system.
	 * 
	 * @param fileName
	 * @param password
	 * @param forReadOnly
	 * @throws Exception
	 *             if bad things happen during open
	 */
    public void openFile(final String fileName, final StringBuilder password, final boolean forReadOnly) throws Exception {
        final PwsFile file = PwsFileFactory.loadFile(fileName, password);
        getShell().setText(PasswordSafeJFace.APP_NAME + " - " + fileName);
        setPwsFile(file);
        setReadOnly(forReadOnly);
        if (true) UserPreferences.getInstance().setMostRecentFilename(fileName);
    }

    /**
	 * Save the current safe.
	 * 
	 * @throws IOException
	 *             if bad things happen during save
	 * @throws NoSuchAlgorithmException 
	 *             if SHA-1 implementation not found
	 */
    public void saveFile() throws IOException, NoSuchAlgorithmException {
        PwsFile file = getPwsFile();
        if (file.getStorage() == null) {
            saveFileAsAction.run();
        } else {
            file.save();
        }
    }

    /**
	 * Saves the current safe under a new filename.
	 * 
	 * @param newFilename the new name to save the file as
	 * @throws IOException if something went wrong while trying to save the file
	 * @throws NoSuchAlgorithmException
	 *             if SHA-1 implementation not found
	 */
    public void saveFileAs(String newFilename) throws IOException, NoSuchAlgorithmException {
        PwsFileStorage s = new PwsFileStorage(newFilename);
        getPwsFile().setStorage(s);
        getPwsFile().save();
        getShell().setText(PasswordSafeJFace.APP_NAME + " - " + newFilename);
        UserPreferences.getInstance().setMostRecentFilename(newFilename);
    }

    /**
	 * Copy field to clipboard.
	 * 
	 * @param cb
	 *            handle to the clipboard
	 * @param valueToCopy
	 *            the value to copy to clipboard
	 */
    public void copyToClipboard(final Clipboard cb, final String valueToCopy) {
        if (valueToCopy != null) {
            cb.setContents(new Object[] { valueToCopy }, new Transfer[] { TextTransfer.getInstance() });
            log.debug("Copied to clipboard");
        }
    }

    /**
	 * Update the access time of an entry.
	 * This method should be moved to PwsDatastore in V0.9.
	 * @param anEntry to update the access time, may be sparse
	 */
    public void updateAccessTime(PwsEntryBean anEntry) {
        if (anEntry != null) {
            if (!isReadOnly() && "3".equals(anEntry.getVersion())) {
                if (anEntry.isSparse()) {
                    anEntry = getPwsDataStore().getEntry(anEntry.getStoreIndex());
                }
                anEntry.setLastAccess(new Date());
                dataStore.updateEntry(anEntry);
            }
        }
    }

    /**
	 * Locates the selected record in the underlying tree or table control.
	 * 
	 * @return the selected record
	 */
    public PwsEntryBean getSelectedRecord() {
        PwsEntryBean recordToCopy = null;
        if (isTreeViewShowing()) {
            if (tree.getSelectionCount() == 1) {
                TreeItem ti = tree.getSelection()[0];
                Object treeData = ti.getData();
                if (treeData != null && treeData instanceof PwsEntryBean) {
                    recordToCopy = (PwsEntryBean) treeData;
                }
            }
        } else {
            if (table != null && table.getSelectionCount() == 1) {
                TableItem ti = table.getSelection()[0];
                recordToCopy = (PwsEntryBean) ti.getData();
            }
        }
        return recordToCopy;
    }

    /**
	 * Updates an existing record.
	 * @param newEntry the entry's new values
	 */
    public void updateRecord(PwsEntryBean newEntry) {
        if (log.isDebugEnabled()) log.debug("Dialog has been edited, updating safe");
        getPwsDataStore().updateEntry(newEntry);
        if (isDirty()) {
            saveOnUpdateOrEditCheck();
        }
        updateViewers();
    }

    /**
	 * Adds a new record to the safe in a new dialog.
     * @param newEntry the entry to add
     */
    public void addRecord(PwsEntryBean newEntry) {
        if (log.isDebugEnabled()) log.debug("Dialog has created new record, updating safe");
        try {
            getPwsDataStore().addEntry(newEntry);
            saveOnUpdateOrEditCheck();
        } catch (PasswordSafeException e) {
            displayErrorDialog(Messages.getString("PasswordSafeJFace.AddEntryError.Title"), Messages.getString("PasswordSafeJFace.AddEntryError.Message"), e);
        }
        updateViewers();
    }

    /**
	 * If the user has set "Save on Update or Edit", we save the file
	 * immediately.
	 * 
	 */
    private void saveOnUpdateOrEditCheck() {
        final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
        if (thePrefs.getBoolean(JpwPreferenceConstants.SAVE_IMMEDIATELY_ON_EDIT)) {
            if (log.isDebugEnabled()) log.debug("Save on Edit option active. Saving database.");
            saveFileAction.run();
        }
    }

    /**
     * Prompts the user to save the current file, if it has been modified.
     * @return <code>true</code> if the user cancels the action which triggered the
     * call to this method, <code>false</code> if the save was successful or ignored.
     */
    public boolean saveAppIfDirty() {
        boolean cancelled = false;
        if (isDirty()) {
            int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL;
            MessageBox messageBox = new MessageBox(getShell(), style);
            messageBox.setText(Messages.getString("PasswordSafeJFace.SaveChanges.Title"));
            messageBox.setMessage(Messages.getString("PasswordSafeJFace.SaveChanges.Message"));
            int result = messageBox.open();
            if (result == SWT.YES) {
                try {
                    saveFile();
                } catch (IOException e1) {
                    displayErrorDialog(Messages.getString("PasswordSafeJFace.SaveSafeError.Title"), e1.getMessage(), e1);
                    cancelled = true;
                } catch (NoSuchAlgorithmException e) {
                    displayErrorDialog(Messages.getString("PasswordSafeJFace.SaveSafeError.Title"), e.getMessage(), e);
                    cancelled = true;
                }
            } else if (result == SWT.CANCEL) {
                cancelled = true;
            }
        }
        return cancelled;
    }

    /**
	 * Deletes the selected record in the tree or table.
	 * 
	 */
    public void deleteRecord() {
        PwsEntryBean selectedRec = getSelectedRecord();
        getPwsDataStore().removeEntry(selectedRec);
        saveOnUpdateOrEditCheck();
        updateViewers();
    }

    /**
	 * Has the current safe been modified.
	 * 
	 * @return true if the safe has been modified
	 */
    public boolean isDirty() {
        if (getPwsFile() != null) {
            return getPwsFile().isModified();
        }
        return false;
    }

    /**
	 * Is the Application in locked mode.
	 * 
	 * @return true if the application has been locked
	 */
    public boolean isLocked() {
        return lockState.isLocked();
    }

    /**
	 * Sets the Application in locked mode.
	 * 
	 * @param sets the locked mode
	 */
    public void setLocked(final boolean isLocked) {
        lockState.setLocked(isLocked);
    }

    public LockState getLockStatus() {
        return lockState;
    }

    /**
	 * Is the Application in read only mode.
	 * 
	 * @return true if the safe is opened read only
	 */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
	 * Sets the Application in read only mode.
	 * 
	 * @param sets the read only mode testtesttest@
	 */
    public void setReadOnly(final boolean isReadOnly) {
        getPwsFile().setReadOnly(isReadOnly);
        setEditMenusEnabled(!isReadOnly);
        readOnly = isReadOnly;
    }

    /**
	 * Returns the currently loaded pwsafe file.
	 * 
	 * @return Returns the pwsFile.
	 */
    public PwsFile getPwsFile() {
        return pwsFile;
    }

    /**
	 * Returns the currently loaded pwsafe file.
	 * 
	 * @return Returns the pwsFile.
	 */
    public PwsEntryStore getPwsDataStore() {
        return dataStore;
    }

    /**
	 * Sets the currently load pwsafe file.
	 * 
	 * @param pwsFile
	 *            The pwsFile to set.
	 */
    public void setPwsFile(final PwsFile pwsFile) {
        this.pwsFile = pwsFile;
        this.dataStore = PwsFileFactory.getStore(pwsFile);
        updateViewers();
    }

    /**
	 * Perform necessary shutdown operations, regardless of how the user
	 * exited the application.
	 *
	 */
    private void tidyUpOnExit() {
        final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
        if (thePrefs.getBoolean(JpwPreferenceConstants.CLEAR_CLIPBOARD_ON_MIN)) {
            clearClipboardAction.run();
        }
        thePrefs.setValue(DISPLAY_AS_LIST_PREF, !isTreeViewShowing());
        try {
            UserPreferences.getInstance().savePreferences();
        } catch (IOException e) {
            displayErrorDialog(Messages.getString("PasswordSafeJFace.SavePrefsError.Title"), Messages.getString("PasswordSafeJFace.SavePrefsError.Message") + e.getMessage(), e);
        }
    }

    /**
	 * Exit the app.
	 * 
	 */
    public void exitApplication() {
        tidyUpOnExit();
        if (systemTray != null) {
            systemTray.dispose();
            systemTray = null;
        }
        getShell().close();
        if (getShell() != null && (!getShell().isDisposed())) getShell().dispose();
    }

    /**
	 * Set the list view as the topmost view.
	 */
    public void showListView() {
        if (table != null) return;
        if (tree != null) {
            tree.setVisible(false);
            tree.clearAll(false);
            tree.dispose();
            tree = null;
            treeViewer = null;
        }
        addTableView(composite);
        composite.layout();
    }

    protected void addTableView(Composite aComposite) {
        tableViewer = new TableViewer(aComposite, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer.addDoubleClickListener(new ViewerDoubleClickListener());
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setMenu(createPopupMenu(table));
        tableViewer.setContentProvider(new PasswordTableContentProvider());
        tableViewer.setLabelProvider(new PasswordTableLabelProvider());
        tableViewer.setInput(new Object());
        tableViewer.setSorter(new PasswordTableSorter());
        final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setWidth(100);
        tableColumn.setText(Messages.getString("PasswordSafeJFace.Column.Title"));
        tableColumn.addSelectionListener(new TableColumnSelectionAdaptor(tableViewer, 1));
        WidgetPreferences.tuneTableColumn(tableColumn, getClass(), "table/title");
        final TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);
        tableColumn_1.setWidth(100);
        tableColumn_1.setText(Messages.getString("PasswordSafeJFace.Column.UserName"));
        tableColumn_1.addSelectionListener(new TableColumnSelectionAdaptor(tableViewer, 2));
        WidgetPreferences.tuneTableColumn(tableColumn_1, getClass(), "table/userName");
        final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
        if (thePrefs.getBoolean(JpwPreferenceConstants.SHOW_NOTES_IN_LIST)) {
            final TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);
            tableColumn_2.setWidth(100);
            tableColumn_2.setText(Messages.getString("PasswordSafeJFace.Column.Notes"));
            tableColumn_2.addSelectionListener(new TableColumnSelectionAdaptor(tableViewer, 3));
            WidgetPreferences.tuneTableColumn(tableColumn_2, getClass(), "table/notes");
        }
        PasswordTableSorter pts = (PasswordTableSorter) tableViewer.getSorter();
        pts.sortOnColumn(1);
    }

    /**
	 * Set the table view as the topmost view.
	 */
    public void showTreeView() {
        if (isTreeViewShowing()) return;
        if (table != null) {
            table.setVisible(false);
            table.clearAll();
            table.dispose();
            table = null;
            tableViewer = null;
        }
        addTreeView(composite);
        composite.layout();
    }

    protected void addTreeView(Composite aComposite) {
        treeViewer = new TreeViewer(aComposite, SWT.BORDER);
        treeViewer.setLabelProvider(new PasswordTreeLabelProvider());
        treeViewer.setContentProvider(new PasswordTreeContentProvider());
        treeViewer.setSorter(new ViewerSorter());
        treeViewer.addDoubleClickListener(new ViewerDoubleClickListener());
        treeViewer.setComparer(new IElementComparer() {

            public boolean equals(Object a, Object b) {
                if (a instanceof PwsEntryBean && b instanceof PwsEntryBean) return ((PwsEntryBean) a).getStoreIndex() == (((PwsEntryBean) b).getStoreIndex()); else return a.equals(b);
            }

            public int hashCode(Object element) {
                if (element instanceof PwsEntryBean) return ((PwsEntryBean) element).getStoreIndex(); else return element.hashCode();
            }
        });
        tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setMenu(createPopupMenu(tree));
        treeViewer.setInput(new Object());
        final TreeColumn treeColumn = new TreeColumn(tree, SWT.CENTER);
        treeColumn.setText(Messages.getString("PasswordSafeJFace.Column.Title"));
        treeColumn.setWidth(100);
        WidgetPreferences.tuneTreeColumn(treeColumn, getClass(), "tree/title");
        final TreeColumn treeColumn_1 = new TreeColumn(tree, SWT.LEFT);
        treeColumn_1.setText(Messages.getString("PasswordSafeJFace.Column.UserName"));
        treeColumn_1.setWidth(100);
        WidgetPreferences.tuneTreeColumn(treeColumn_1, getClass(), "tree/userName");
        final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
        if (thePrefs.getBoolean(JpwPreferenceConstants.SHOW_NOTES_IN_LIST)) {
            final TreeColumn treeColumn_2 = new TreeColumn(tree, SWT.LEFT);
            treeColumn_2.setText(Messages.getString("PasswordSafeJFace.Column.Notes"));
            treeColumn_2.setWidth(100);
            WidgetPreferences.tuneTreeColumn(treeColumn_2, getClass(), "tree/notes");
        }
        TreeColumn[] columns = tree.getColumns();
        for (int i = 0; i < columns.length; i++) {
            columns[i].setMoveable(true);
        }
    }

    /**
	 * Returns whether the treeview is the active view.
	 * 
	 * @return true if the treeview is showing, false otherwise
	 */
    public boolean isTreeViewShowing() {
        return (tree != null);
    }

    public String getSelectedTreeGroupPath() {
        StringBuilder theGroupPath = new StringBuilder();
        if (isTreeViewShowing() && tree.getSelectionCount() == 1) {
            TreeItem ti = tree.getSelection()[0];
            if (ti.getItemCount() <= 0) {
                ti = ti.getParentItem();
            }
            while (ti != null) {
                theGroupPath.insert(0, ti.getText());
                ti = ti.getParentItem();
                if (ti != null) theGroupPath.insert(0, '.');
            }
            log.debug("Group path constructed *" + theGroupPath + "*");
        }
        return theGroupPath.toString();
    }

    /**
	 * Refresh the tree and table.
	 * 
	 */
    public void updateViewers() {
        if (isTreeViewShowing()) {
            Object[] currentExpansions = treeViewer.getExpandedElements();
            treeViewer.setInput(getPwsDataStore());
            treeViewer.refresh();
            treeViewer.setExpandedElements(currentExpansions);
        } else {
            tableViewer.setInput(getPwsDataStore());
        }
    }

    /**
	 * Turns on or off the edit record menus (add/edit/delete) and save/as
	 * menus.
	 * 
	 * @param enabled
	 *            true to enable the menus, false otherwise
	 */
    private void setEditMenusEnabled(final boolean enabled) {
        addRecordAction.setEnabled(enabled);
        deleteRecordAction.setEnabled(enabled);
        saveFileAction.setEnabled(enabled);
        saveFileAsAction.setEnabled(enabled);
        openFileReadWriteToggleAction.setOpenReadOnlyMode(enabled);
    }

    /**
	 * TODO: Refactor this to one or several external Listeners.
	 * The URL copy action should only be enabled when an URL is available.
	 * @param enabled
	 */
    public void setUrlCopyEnabled(boolean enabled) {
        copyURLAction.setEnabled(enabled);
    }

    /**
	 * Exports the contents of the password safe to tab separated file.
	 * 
	 * @param filename
	 *            full path to output file
	 * @throws FileNotFoundException if the password file was not found
	 */
    public void exportToText(String filename) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(filename);
            Iterator<? extends PwsRecord> iter = getPwsFile().getRecords();
            CSVWriter csvWriter = new CSVWriter(fw, '\t');
            while (iter.hasNext()) {
                PwsRecord nextRecord = iter.next();
                List<String> nextEntry = new ArrayList<String>();
                if (nextRecord instanceof PwsRecordV1) {
                    nextEntry.add(V1_GROUP_PLACEHOLDER);
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV1.TITLE));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV1.USERNAME));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV1.PASSWORD));
                } else if (nextRecord instanceof PwsRecordV2) {
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV2.GROUP));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV2.TITLE));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV2.USERNAME));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV2.PASSWORD));
                } else {
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV3.GROUP));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV3.TITLE));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV3.USERNAME));
                    nextEntry.add(PwsEntryBean.getSafeValue(nextRecord, PwsFieldTypeV3.PASSWORD));
                }
                String[] nextLine = nextEntry.toArray(new String[0]);
                csvWriter.writeNext(nextLine);
            }
        } catch (IOException ioe) {
            displayErrorDialog(Messages.getString("PasswordSafeJFace.ExportError.Title"), Messages.getString("PasswordSafeJFace.ExportError.Message"), ioe);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    log.warn("Could not close output text file", e);
                }
            }
        }
    }

    public void importFromText(String filename) {
        File theFile = new File(filename);
        if (!theFile.exists()) {
            throw new RuntimeException(Messages.getString("PasswordSafeJFace.ImportCSV.NoFileFound") + filename + "]");
        }
        Reader fileReader = null;
        try {
            fileReader = new FileReader(theFile);
            CSVReader csvReader = new CSVReader(fileReader, '\t');
            String[] nextLine;
            PwsFile pwsFile = getPwsFile();
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length < 2) continue;
                PwsEntryBean entry = PwsEntryBean.fromPwsRecord(getPwsFile().newRecord());
                entry.setSparse(false);
                if (!nextLine[0].equals(V1_GROUP_PLACEHOLDER)) {
                    entry.setGroup(nextLine[0]);
                }
                entry.setTitle(nextLine[1]);
                entry.setUsername(nextLine[2]);
                entry.setPassword(new StringBuilder(nextLine[3]));
                getPwsDataStore().addEntry(entry);
            }
        } catch (Exception e) {
            throw new RuntimeException(Messages.getString("PasswordSafeJFace.ImportCSV.ProcessingError") + filename + "]", e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    log.warn("Could not close import text file", e);
                }
            }
        }
        this.updateViewers();
    }

    /**
	 * Import an XML file into the current passwordsafe.
	 * 
	 * @param filename
	 *            the name of the file
	 * @throws PasswordSafeException
	 */
    public void importFromXML(String filename) throws PasswordSafeException {
        File theFile = new File(filename);
        if (!theFile.exists()) {
            throw new RuntimeException(Messages.getString("PasswordSafeJFace.ImportXML.NoFileFound") + filename + "]");
        }
        BufferedInputStream bis;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bis = new BufferedInputStream(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(Messages.getString("PasswordSafeJFace.ImportXML.CantOpen"), e);
        }
        String utf8String;
        try {
            int c;
            while ((c = bis.read()) != -1) {
                baos.write(c);
            }
            byte[] utf8Bytes = baos.toByteArray();
            utf8String = new String(utf8Bytes, "UTF-8");
            char header = utf8String.charAt(0);
            if (header > 255) {
                utf8String = utf8String.substring(1);
            }
        } catch (Exception ioe) {
            throw new RuntimeException(Messages.getString("PasswordSafeJFace.ImportXML.IOException"), ioe);
        } finally {
            IOUtils.closeQuietly(bis);
        }
        XMLDataParser xdp = new XMLDataParser();
        PwsEntryBean[] entries = xdp.parse(utf8String);
        if (entries != null && entries.length > 0) {
            PwsFile pwsFile = getPwsFile();
            for (PwsEntryBean entry : entries) {
                entry.setSparse(false);
                dataStore.addEntry(entry);
            }
            this.updateViewers();
        }
    }

    /**
	 * Export the current safe to the name file in XML format.
	 * 
	 * @param filename
	 *            the filename to export to
	 * @throws IOException if there was a problem with the export to the XML file
	 */
    public void exportToXML(String filename) throws IOException {
        PwsFile pwsFile = getPwsFile();
        if (pwsFile != null) {
            List<PwsEntryBean> sparseEntries = dataStore.getSparseEntries();
            List<PwsEntryBean> entryList = new ArrayList<PwsEntryBean>(sparseEntries.size());
            for (PwsEntryBean sparseEntry : sparseEntries) {
                PwsEntryBean nextDTO = dataStore.getEntry(sparseEntry.getStoreIndex());
                entryList.add(nextDTO);
            }
            XMLDataParser xdp = new XMLDataParser();
            String xmlOutput = xdp.convertToXML(entryList);
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
            dos.write(239);
            dos.write(187);
            dos.write(191);
            dos.write(xmlOutput.getBytes("UTF-8"));
            dos.close();
        }
    }

    /**
	 * Display error dialog and log error.
	 * 
	 * @param title
	 *            the title of the dialog
	 * @param message
	 *            the message to display
	 * @param e
	 *            the exception that caused the problem.
	 */
    public void displayErrorDialog(String title, String message, Exception e) {
        MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
        mb.setText(title);
        mb.setMessage(message);
        mb.open();
        log.error(message, e);
    }

    /**
	 * Returns a shell listener. This shell listener gets registered with this
	 * window's shell.
	 * <p>
	 * The default implementation of this framework method returns a new
	 * listener that makes this window the active window for its window manager
	 * (if it has one) when the shell is activated, and calls the framework
	 * method <code>handleShellCloseEvent</code> when the shell is closed.
	 * Subclasses may extend or reimplement.
	 * </p>
	 * 
	 * @return a shell listener
	 */
    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {

            private volatile boolean unlocking = false;

            @Override
            public void shellClosed(ShellEvent event) {
                log.trace("PWSJface shell listener enter 'Closed'");
                event.doit = false;
                final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
                if (thePrefs.getBoolean(JpwPreferenceConstants.SHOW_ICON_IN_SYSTEM_TRAY) && systemTray != null) {
                    getShell().setMinimized(true);
                    setReturnCode(OK);
                    return;
                }
                boolean cancelled = saveAppIfDirty();
                if (cancelled) {
                    setReturnCode(OK);
                } else {
                    tidyUpOnExit();
                    handleShellCloseEvent();
                }
                log.trace("PWSJface shell listener leave 'Closed'");
            }

            /**
			 * Sent when a shell becomes the active window.
			 * The default behavior is to do nothing.
			 *
			 * @param e an event containing information about the activation
			 */
            @Override
            public void shellActivated(ShellEvent e) {
                log.trace("PWSJface shell listener enter 'Activated'");
                if (unlocking) {
                    log.info("PWSJface shell listener 'Activated' - aborted because unlocking");
                    e.doit = false;
                    return;
                }
                unlocking = true;
                try {
                    if (lockTask != null) {
                        lockTask.cancel();
                        lockTask = null;
                    }
                    if (lockState.isLocked()) {
                        log.info("PWSJface shell listener 'Activated' - unlocking");
                        unlockDbAction.run();
                    }
                } finally {
                    unlocking = false;
                }
                log.trace("PWSJface shell listener leave 'Activated'");
            }

            /**
			 * Sent when a shell stops being the active window.
			 * The default behavior is to do nothing.
			 *
			 * @param e an event containing information about the deactivation
			 */
            @Override
            public void shellDeactivated(ShellEvent e) {
                log.trace("PWSJface shell listener enter 'Deactivated'");
                startLockTimer();
                log.trace("PWSJface shell listener leave 'Deactivated'");
            }

            /**
			 * In <em>theory</em>, sent when a shell is un-minimized.
			 * On windows sent when the shell is set visible. 
			 * Do <strong>not</strong> call setVisible(true)
			 * within this method, danger of endless loop.
			 *
			 * @param e an event containing information about the un-minimization
			 */
            @Override
            public void shellDeiconified(ShellEvent e) {
                log.trace("PWSJface shell listener enter 'Deiconified'");
                if (unlocking) {
                    log.info("PWSJface shell listener deiconify - abort because of unlocking");
                    e.doit = false;
                    return;
                }
                if (lockTask != null) {
                    lockTask.cancel();
                    lockTask = null;
                }
                if (lockState.isLocked()) {
                    unlocking = true;
                    try {
                        log.info("PWSJface shell listener deiconify - unlocking");
                        e.doit = unlockDbAction.performUnlock();
                    } finally {
                        unlocking = false;
                    }
                }
                log.trace("PWSJface shell listener leave 'Deiconified'");
            }

            /**
			 * Sent when a shell is minimized.
			 * The default behavior is to do nothing.
			 * <br>
			 * On Mac Carbon, this is called the first time when disposing 
			 * the password dialogue when reactivating from Systray.
			 * The second time it is not called. Still no good, therefore 
			 * Systray on Mac is blocked - see PasswordSafeMacOSX.main.
			 *
			 * @param e an event containing information about the minimization
			 */
            @Override
            public void shellIconified(ShellEvent e) {
                log.trace("PWSJface shell listener enter 'Iconified'");
                final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
                if (thePrefs.getBoolean(JpwPreferenceConstants.CLEAR_CLIPBOARD_ON_MIN)) {
                    clearClipboardAction.run();
                }
                if (isDirty()) {
                    if (thePrefs.getBoolean(JpwPreferenceConstants.CONFIRM_SAVE_ON_MIN)) {
                        saveFileAction.run();
                    } else {
                        saveFileAction.run();
                    }
                }
                if (systemTray != null && thePrefs.getBoolean(JpwPreferenceConstants.SHOW_ICON_IN_SYSTEM_TRAY)) {
                    if (log.isDebugEnabled()) log.debug("Shrinking to tray");
                    getShell().setVisible(false);
                }
                if (thePrefs.getBoolean(JpwPreferenceConstants.LOCK_DB_ON_MIN)) {
                    getShell().getDisplay().asyncExec(lockDbAction);
                }
                startLockTimer();
                log.trace("PWSJface shell listener leave 'Iconified'");
            }

            private void startLockTimer() {
                final IPreferenceStore thePrefs = JFacePreferences.getPreferenceStore();
                if (pwsFile != null && thePrefs.getBoolean(JpwPreferenceConstants.LOCK_ON_IDLE)) {
                    if (lockTask != null) {
                        lockTask.cancel();
                    }
                    String idleMins = thePrefs.getString(JpwPreferenceConstants.LOCK_ON_IDLE_MINS);
                    try {
                        int mins = Integer.parseInt(idleMins);
                        lockTask = lockDbAction.createTaskTimer();
                        lockTimer.schedule(lockTask, mins * 60 * 1000);
                    } catch (NumberFormatException anEx) {
                        log.warn("Unable to set lock timer to an amount of " + idleMins + " minutes");
                    }
                }
            }
        };
    }

    /**
	 * Clears the current view of all data.
	 */
    public void clearView() {
        if (isTreeViewShowing()) tree.clearAll(true); else table.clearAll();
    }
}
