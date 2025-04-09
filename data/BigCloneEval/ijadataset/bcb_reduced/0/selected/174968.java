package net.sf.ulmac.ui.preferences;

import java.awt.SystemTray;
import java.io.File;
import net.sf.ulmac.core.ids.IPreferenceIds;
import net.sf.ulmac.core.managers.PrefManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class InputDirectoriesPreferencePage extends AbstractPreferencePage implements IWorkbenchPreferencePage {

    public static final String ID = "net.sf.ulmac.ui.preferences.inputDirectoriesPreferencePage";

    private Composite fContainer;

    private Text fTxtInputDirectoriesInclude;

    private Text fTxtLookbackValue;

    private Combo fCboLookbackUnits;

    private Text fTxtInputDirectoriesExclude;

    private boolean fIsInitializing;

    private Button fBtnPromptForValue;

    private Button fBtnScanOnStartup;

    private Button fBtnAutomaticScanning;

    private Text fTxtAutomaticScanningValue;

    private Combo fCboAutomaticScanningUnits;

    private Text fTxtFileExtensionsExclude;

    private Button fBtnAutomaticProcessing;

    private Button fBtnCopyMP3s;

    private Text fTxtAutomaticProcessingCountdown;

    public InputDirectoriesPreferencePage() {
        super();
    }

    @Override
    public Control createContents(final Composite parent) {
        fIsInitializing = true;
        fContainer = new Composite(parent, SWT.NULL);
        fContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fContainer.setLayout(new GridLayout(1, false));
        Composite inputDirectoriesContainer = new Composite(fContainer, SWT.NULL);
        inputDirectoriesContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        inputDirectoriesContainer.setLayout(new GridLayout(2, false));
        Label lblInputDirectories = new Label(inputDirectoriesContainer, SWT.NONE);
        lblInputDirectories.setText("Enter all directories to include in scan:");
        lblInputDirectories.pack();
        lblInputDirectories.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fTxtInputDirectoriesInclude = new Text(inputDirectoriesContainer, SWT.BORDER);
        fTxtInputDirectoriesInclude.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        Button btnAddDirectory = new Button(inputDirectoriesContainer, SWT.PUSH);
        Image imgAdd = new Image(PlatformUI.getWorkbench().getDisplay(), getClass().getResourceAsStream("/icons/add.gif"));
        btnAddDirectory.setImage(imgAdd);
        btnAddDirectory.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent().getActiveShell(), SWT.OPEN | SWT.MULTI);
                dialog.setText("Add Directory");
                String path = dialog.open();
                if (path != null && !path.equals("")) {
                    File file = new File(path);
                    if (file != null && file.isDirectory()) {
                        fTxtInputDirectoriesInclude.setText(fTxtInputDirectoriesInclude.getText() + path + ";");
                    }
                }
            }
        });
        Composite inputDirectoriesExcludeContainer = new Composite(fContainer, SWT.NULL);
        inputDirectoriesExcludeContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        inputDirectoriesExcludeContainer.setLayout(new GridLayout(2, false));
        Label lblInputDirectoriesExclude = new Label(inputDirectoriesExcludeContainer, SWT.NONE);
        lblInputDirectoriesExclude.setText("Enter all directories to exclude in scan:");
        lblInputDirectoriesExclude.pack();
        lblInputDirectoriesExclude.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fTxtInputDirectoriesExclude = new Text(inputDirectoriesExcludeContainer, SWT.BORDER);
        fTxtInputDirectoriesExclude.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        Button btnAddDirectoryExclude = new Button(inputDirectoriesExcludeContainer, SWT.PUSH);
        btnAddDirectoryExclude.setImage(imgAdd);
        btnAddDirectoryExclude.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent().getActiveShell(), SWT.OPEN | SWT.MULTI);
                dialog.setText("Add Directory");
                String path = dialog.open();
                if (path != null && !path.equals("")) {
                    File file = new File(path);
                    if (file != null && file.isDirectory()) {
                        fTxtInputDirectoriesExclude.setText(fTxtInputDirectoriesExclude.getText() + path + ";");
                    }
                }
            }
        });
        Composite fileExtensionsExcludeContainer = new Composite(fContainer, SWT.NULL);
        fileExtensionsExcludeContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fileExtensionsExcludeContainer.setLayout(new GridLayout(2, false));
        Label lblFileExtensionsExclude = new Label(fileExtensionsExcludeContainer, SWT.NONE);
        lblFileExtensionsExclude.setText("Enter all file extensions to exclude in scan:");
        lblFileExtensionsExclude.pack();
        lblFileExtensionsExclude.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fTxtFileExtensionsExclude = new Text(fileExtensionsExcludeContainer, SWT.BORDER);
        fTxtFileExtensionsExclude.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        Composite lookBackContainer = new Composite(fContainer, SWT.NULL);
        lookBackContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        lookBackContainer.setLayout(new GridLayout(2, false));
        Label lblLookback = new Label(lookBackContainer, SWT.NONE);
        lblLookback.setText("Specify how far back to include directories:");
        lblLookback.pack();
        lblLookback.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fTxtLookbackValue = new Text(lookBackContainer, SWT.BORDER);
        fTxtLookbackValue.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                if (!fIsInitializing) {
                    try {
                        Long.parseLong(fTxtLookbackValue.getText().trim());
                    } catch (NumberFormatException e) {
                        fTxtLookbackValue.setText("");
                    }
                }
            }
        });
        fCboLookbackUnits = new Combo(lookBackContainer, SWT.READ_ONLY);
        fCboLookbackUnits.add("day(s)");
        fCboLookbackUnits.add("hour(s)");
        Group grpAutomaticScanning = new Group(fContainer, SWT.NULL);
        grpAutomaticScanning.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        grpAutomaticScanning.setLayout(new GridLayout(2, false));
        grpAutomaticScanning.setText("Automatic Scanning");
        fBtnAutomaticScanning = new Button(grpAutomaticScanning, SWT.CHECK);
        fBtnAutomaticScanning.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fBtnAutomaticScanning.setText("Enable automatic scanning of input directories");
        fBtnAutomaticScanning.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                if (fBtnAutomaticScanning.getSelection() && SystemTray.isSupported() && !PrefManager.showApplicationInSystemTray()) {
                    if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Enable Close To System Tray?", "This preference is made even more powerful when combined with the 'Close to the system tray' preference.  " + "With both preference enabled Ulmac will be able to scan your input directories for new files without taking up any screen real estate.\n\n" + "Would you like to turn the 'Close to the system tray' preference on?")) {
                        getPreferenceStore().putValue(IPreferenceIds.GENERAL_SHOW_IN_SYSTEM_TRAY, "true");
                    }
                }
            }
        });
        Label lblAutomaticScanning = new Label(grpAutomaticScanning, SWT.NONE);
        lblAutomaticScanning.setText("Specify how often the automatic scan should run:");
        lblAutomaticScanning.pack();
        lblAutomaticScanning.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fTxtAutomaticScanningValue = new Text(grpAutomaticScanning, SWT.BORDER);
        fTxtAutomaticScanningValue.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                if (!fIsInitializing) {
                    try {
                        Long.parseLong(fTxtAutomaticScanningValue.getText().trim());
                    } catch (NumberFormatException e) {
                        fTxtAutomaticScanningValue.setText("");
                    }
                }
            }
        });
        fCboAutomaticScanningUnits = new Combo(grpAutomaticScanning, SWT.READ_ONLY);
        fCboAutomaticScanningUnits.add("hour(s)");
        fCboAutomaticScanningUnits.add("minute(s)");
        Composite bottomContainer = new Composite(fContainer, SWT.NULL);
        bottomContainer.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        bottomContainer.setLayout(new GridLayout(1, false));
        fBtnPromptForValue = new Button(bottomContainer, SWT.CHECK);
        fBtnPromptForValue.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fBtnPromptForValue.setText("Prompt for look back value");
        fBtnScanOnStartup = new Button(bottomContainer, SWT.CHECK);
        fBtnScanOnStartup.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fBtnScanOnStartup.setText("Perform scan of input directories on startup");
        fBtnCopyMP3s = new Button(bottomContainer, SWT.CHECK);
        fBtnCopyMP3s.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fBtnCopyMP3s.setText("Disable tagging on MP3 files (files will be copied to the default output directory)");
        fBtnCopyMP3s.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                if (fBtnCopyMP3s.getSelection() && (fTxtFileExtensionsExclude.getText().contains("mp3") || fTxtFileExtensionsExclude.getText().contains("MP3"))) {
                    if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Remove MP3 from Exclude File Option?", "In order for MP3s to be copied with this preference the exclude file options will need to be modified.\n\n" + "Would you like to remove MP3 from this option?")) {
                        String oldFileExtensionsExclude = fTxtFileExtensionsExclude.getText();
                        if (oldFileExtensionsExclude.contains("MP3;")) {
                            fTxtFileExtensionsExclude.setText(oldFileExtensionsExclude.replace("MP3;", ""));
                        } else if (oldFileExtensionsExclude.contains("mp3;")) {
                            fTxtFileExtensionsExclude.setText(oldFileExtensionsExclude.replace("mp3;", ""));
                        } else if (oldFileExtensionsExclude.contains("MP3")) {
                            fTxtFileExtensionsExclude.setText(oldFileExtensionsExclude.replace("MP3", ""));
                        } else if (oldFileExtensionsExclude.contains("mp3")) {
                            fTxtFileExtensionsExclude.setText(oldFileExtensionsExclude.replace("mp3", ""));
                        }
                    }
                }
            }
        });
        Group grpAutomaticProcessing = new Group(fContainer, SWT.NULL);
        grpAutomaticProcessing.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        grpAutomaticProcessing.setLayout(new GridLayout(2, false));
        grpAutomaticProcessing.setText("Automatic Processing");
        fBtnAutomaticProcessing = new Button(grpAutomaticProcessing, SWT.CHECK);
        fBtnAutomaticProcessing.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fBtnAutomaticProcessing.setText("Automatically process new files using default settings");
        Label lblAutomaticProcessingCountdown = new Label(grpAutomaticProcessing, SWT.NONE);
        lblAutomaticProcessingCountdown.setText("Specify how many seconds to countdown before automatic processing begins:");
        lblAutomaticProcessingCountdown.pack();
        lblAutomaticProcessingCountdown.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
        fTxtAutomaticProcessingCountdown = new Text(grpAutomaticProcessing, SWT.BORDER);
        fTxtAutomaticProcessingCountdown.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                if (!fIsInitializing) {
                    try {
                        Long.parseLong(fTxtAutomaticProcessingCountdown.getText().trim());
                    } catch (NumberFormatException e) {
                        fTxtAutomaticProcessingCountdown.setText("");
                    }
                }
            }
        });
        initControls();
        fIsInitializing = false;
        return fContainer;
    }

    @Override
    protected String getPageID() {
        return ID;
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(PlatformUI.getPreferenceStore());
    }

    private void initControls() {
        String includeDirectories = getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_INCLUDE);
        if (includeDirectories != null && includeDirectories.length() > 0) {
            fTxtInputDirectoriesInclude.setText(includeDirectories);
        }
        String excludeDirectories = getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_EXCLUDE);
        if (excludeDirectories != null && excludeDirectories.length() > 0) {
            fTxtInputDirectoriesExclude.setText(excludeDirectories);
        }
        String excludeFileExtensions = getPreferenceStore().getString(IPreferenceIds.FILE_EXTENSIONS_EXCLUDE);
        if (excludeFileExtensions != null && excludeFileExtensions.length() > 0) {
            fTxtFileExtensionsExclude.setText(excludeFileExtensions);
        }
        fTxtLookbackValue.setText(getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_LOOKBACK_VALUE) + "   ");
        String lookbackUnit = getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_LOOKBACK_UNIT);
        if (lookbackUnit != null && lookbackUnit.length() > 0) {
            fCboLookbackUnits.setText(lookbackUnit);
        }
        fTxtAutomaticScanningValue.setText(getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING_VALUE) + "   ");
        String automaticScanningUnit = getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING_UNIT);
        if (automaticScanningUnit != null && automaticScanningUnit.length() > 0) {
            fCboAutomaticScanningUnits.setText(automaticScanningUnit);
        }
        fBtnScanOnStartup.setSelection(getPreferenceStore().getBoolean(IPreferenceIds.INPUT_DIRECTORIES_SCAN_ON_STARTUP));
        fBtnPromptForValue.setSelection(getPreferenceStore().getBoolean(IPreferenceIds.INPUT_DIRECTORIES_PROMPT_FOR_LOOKBACK));
        fBtnAutomaticScanning.setSelection(getPreferenceStore().getBoolean(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING));
        fBtnCopyMP3s.setSelection(getPreferenceStore().getBoolean(IPreferenceIds.INPUT_DIRECTORIES_ONLY_COPY_FOR_MP3s));
        fBtnAutomaticProcessing.setSelection(getPreferenceStore().getBoolean(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_PROCESSING));
        fTxtAutomaticProcessingCountdown.setText(getPreferenceStore().getString(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_PROCESSING_COUNTDOWN) + "   ");
        fContainer.pack();
    }

    /**
	 * Performs special processing when this page's Restore Defaults button has
	 * been pressed.
	 */
    @Override
    protected void performDefaults() {
        if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Restore Defaults", "Are you sure you want to overwrite existing preferences?")) {
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_INCLUDE);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_LOOKBACK_VALUE);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_LOOKBACK_UNIT);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_EXCLUDE);
            getPreferenceStore().setToDefault(IPreferenceIds.FILE_EXTENSIONS_EXCLUDE);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_PROMPT_FOR_LOOKBACK);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_SCAN_ON_STARTUP);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING_VALUE);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING_UNIT);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_PROCESSING);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_ONLY_COPY_FOR_MP3s);
            getPreferenceStore().setToDefault(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_PROCESSING_COUNTDOWN);
            initControls();
        }
    }

    @Override
    public boolean performOk() {
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_INCLUDE, fTxtInputDirectoriesInclude.getText().trim());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_LOOKBACK_VALUE, fTxtLookbackValue.getText().trim());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_LOOKBACK_UNIT, fCboLookbackUnits.getText().trim());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_EXCLUDE, fTxtInputDirectoriesExclude.getText().trim());
        savePrefence(IPreferenceIds.FILE_EXTENSIONS_EXCLUDE, fTxtFileExtensionsExclude.getText().trim());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_SCAN_ON_STARTUP, fBtnScanOnStartup.getSelection());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_PROMPT_FOR_LOOKBACK, fBtnPromptForValue.getSelection());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING, fBtnAutomaticScanning.getSelection());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING_VALUE, fTxtAutomaticScanningValue.getText().trim());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_SCANNING_UNIT, fCboAutomaticScanningUnits.getText().trim());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_PROCESSING, fBtnAutomaticProcessing.getSelection());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_ONLY_COPY_FOR_MP3s, fBtnCopyMP3s.getSelection());
        savePrefence(IPreferenceIds.INPUT_DIRECTORIES_AUTOMATIC_PROCESSING_COUNTDOWN, fTxtAutomaticProcessingCountdown.getText().trim());
        return super.performOk();
    }
}
