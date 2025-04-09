package com.scientist.trautwein.copy2go;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class Copy2go {

    static ResourceBundle bundle = ResourceBundle.getBundle("copy2go");

    private static boolean running;

    Image images[];

    static final int ciCross = 0, ciTool = 1, ciPlus = 2, ciSettingsOrange = 3, ciMug = 4, ciEspresso = 5;

    static final String[] imageLocations = { "16-em-cross.png", "16-tool-a.png", "16-em-plus.png", "24-settings-orange.png", "mug.bmp", "espresso.bmp" };

    static final int[] imageTypes = { SWT.BITMAP, SWT.BITMAP, SWT.BITMAP, SWT.BITMAP, SWT.BITMAP, SWT.BITMAP };

    Shell shell;

    Composite center;

    Composite tabFolderPage;

    Group exampleGroup, textButtonGroup;

    SourceSettingsListener sourceSettingsListener = new SourceSettingsListener();

    SourcePathListener sourcePathListener = new SourcePathListener();

    DestPathListener destPathListener = new DestPathListener();

    private TabFolder tabFolder;

    Button goButton;

    final String SOURCEDIR_NAME = "SOURCEDIR";

    final String DESTDIR_NAME = "DESTDIR";

    final String XRES = "XRES";

    final String YRES = "YRES";

    int targetWidth = 220;

    int targetHeight = 176;

    Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    private Copy2GoPreferences copy2GoPreferences;

    Copy2go(Shell shell) {
        copy2GoPreferences = new Copy2GoPreferences();
        copy2GoPreferences.loadPrefs();
        initResources();
        open(shell);
    }

    /**
	 * Creates the "Example" group.  The "Example" group
	 * is typically the left hand column in the tab.
	 */
    void createExampleGroup() {
        exampleGroup = new Group(tabFolderPage, SWT.NONE);
        exampleGroup.setLayout(new GridLayout());
        exampleGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    class TextPrompter extends Dialog {

        String message = "";

        String result = null;

        Shell dialog;

        Text text;

        public TextPrompter(Shell parent, int style) {
            super(parent, style);
        }

        public TextPrompter(Shell parent) {
            this(parent, SWT.APPLICATION_MODAL);
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String string) {
            message = string;
        }

        public String open() {
            dialog = new Shell(getParent(), getStyle());
            dialog.setText(getText());
            dialog.setLayout(new GridLayout());
            Label label = new Label(dialog, SWT.NONE);
            label.setText(message);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text = new Text(dialog, SWT.SINGLE | SWT.BORDER);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.widthHint = 300;
            text.setLayoutData(data);
            Composite buttons = new Composite(dialog, SWT.NONE);
            GridLayout grid = new GridLayout();
            grid.numColumns = 2;
            buttons.setLayout(grid);
            buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            Button ok = new Button(buttons, SWT.PUSH);
            ok.setText(bundle.getString("OK"));
            data = new GridData();
            data.widthHint = 75;
            ok.setLayoutData(data);
            ok.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    result = text.getText();
                    dialog.dispose();
                }
            });
            Button cancel = new Button(buttons, SWT.PUSH);
            cancel.setText(bundle.getString("Cancel"));
            data = new GridData();
            data.widthHint = 75;
            cancel.setLayoutData(data);
            cancel.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    dialog.dispose();
                }
            });
            dialog.setDefaultButton(ok);
            dialog.pack();
            dialog.open();
            while (!dialog.isDisposed()) {
                if (!dialog.getDisplay().readAndDispatch()) dialog.getDisplay().sleep();
            }
            return result;
        }
    }

    class SourceDescr {

        private static final String prefsName = "sourceDescr";

        private String path;

        private static final String prefsNamePath = "path";

        private Text text;

        private SrcAndDest mySrcAndDest;

        public SourceDescr(Preferences node, SrcAndDest mySrcAndDest) {
            Preferences sourceDescrPrefs = node.node(prefsName);
            path = sourceDescrPrefs.get(prefsNamePath, "");
            this.mySrcAndDest = mySrcAndDest;
        }

        public void savePrefs(Preferences node) {
            Preferences sourceDescrPrefs = node.node(prefsName);
            sourceDescrPrefs.put(prefsNamePath, getPath());
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
            updateText();
        }

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 250;
            text.setLayoutData(gridData);
            text.setEditable(false);
            updateText();
            text.setData(SrcAndDest.class.getName(), mySrcAndDest);
            text.addMouseListener(sourcePathListener);
        }

        private void updateText() {
            if (text != null) {
                text.setText(nicePath(getPath(), 40));
            }
        }

        public void setButton(Button settingsButton) {
            settingsButton.setImage(images[Copy2go.ciTool]);
            settingsButton.setData(SrcAndDest.class.getName(), mySrcAndDest);
            settingsButton.addSelectionListener(sourceSettingsListener);
        }
    }

    class DestDescr {

        public static final String prefsName = "destDescr";

        private String path;

        private static final String prefsNamePath = "path";

        private Text text;

        private SrcAndDest mySrcAndDest;

        public DestDescr(Preferences node, SrcAndDest mySrcAndDest) {
            Preferences destDescrPrefs = node.node(prefsName);
            path = destDescrPrefs.get(prefsNamePath, "");
            this.mySrcAndDest = mySrcAndDest;
        }

        public void savePrefs(Preferences node) {
            Preferences destPrefNode = node.node(prefsName);
            destPrefNode.put(prefsNamePath, getPath());
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
            updateText();
        }

        public void setText(Text text) {
            this.text = text;
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 250;
            text.setLayoutData(gridData);
            text.setEditable(false);
            updateText();
            text.setData(SrcAndDest.class.getName(), mySrcAndDest);
            text.addMouseListener(destPathListener);
        }

        private void updateText() {
            if (text != null) {
                text.setText(nicePath(getPath(), 40));
            }
        }
    }

    class SrcAndDest {

        private static final String prefsName = "srcAndDest";

        private SourceDescr sourceDescr;

        private DestDescr destDescr;

        private String name;

        private boolean compressEnabled = true;

        private final String prefsNameCompressEnabled = "compressEnabled";

        private boolean eraseDestBeforeCopy = false;

        private final String prefsNameEraseDestBeforeCopy = "eraseDestBeforeCopy";

        private boolean deleteFromSourceAfterCopy = false;

        private final String prefsNameDeleteFromSourceAfterCopy = "deleteFromSourceAfterCopy";

        private boolean includeSubdirs = true;

        private final String prefsNameIncludeSubdirs = "includeSubdirs";

        private boolean overwrite = false;

        private final String prefsNameOverwrite = "overwrite";

        private long minfilesize = -1;

        private final String prefsNameMinfilesize = "minfilesize";

        private long maxfilesize = -1;

        private final String prefsNameMaxfilesize = "maxfilesize";

        private long maxSpaceNeededOnDest = -1;

        private final String prefsNameMaxSpaceNeededOnDest = "maxSpaceNeededOnDest";

        private long minSpaceLeft = -1;

        private final String prefsNameMinSpaceLeft = "minSpaceLeft";

        private long youngerThanNDays = -1;

        private final String prefsNameYoungerThanNDays = "youngerThanNDays";

        private long olderThanNDays = -1;

        private final String prefsNameOlderThanNDays = "olderThanNDays";

        private String includes = ".*\\.mp3|.*\\.txt";

        private final String prefsNameIncludes = "includes";

        private String excludes = ".*Sunday.*";

        private final String prefsNameExcludes = "excludes";

        public SrcAndDest(Preferences node, String name) {
            this.name = name;
            Preferences srcAndDestElementPrefs = SrcAndDestPrefsNodeName(node, name);
            sourceDescr = new SourceDescr(srcAndDestElementPrefs, this);
            destDescr = new DestDescr(srcAndDestElementPrefs, this);
            compressEnabled = Boolean.valueOf(srcAndDestElementPrefs.get(prefsNameCompressEnabled, Boolean.TRUE.toString()));
            eraseDestBeforeCopy = Boolean.valueOf(srcAndDestElementPrefs.get(prefsNameEraseDestBeforeCopy, Boolean.FALSE.toString()));
            deleteFromSourceAfterCopy = Boolean.valueOf(srcAndDestElementPrefs.get(prefsNameDeleteFromSourceAfterCopy, Boolean.FALSE.toString()));
            overwrite = Boolean.valueOf(srcAndDestElementPrefs.get(prefsNameOverwrite, Boolean.FALSE.toString()));
            includeSubdirs = Boolean.valueOf(srcAndDestElementPrefs.get(prefsNameIncludeSubdirs, Boolean.TRUE.toString()));
            minfilesize = Integer.valueOf(srcAndDestElementPrefs.get(prefsNameMinfilesize, "-1"));
            maxfilesize = Integer.valueOf(srcAndDestElementPrefs.get(prefsNameMaxfilesize, "-1"));
            maxSpaceNeededOnDest = Integer.valueOf(srcAndDestElementPrefs.get(prefsNameMaxSpaceNeededOnDest, "-1"));
            minSpaceLeft = Integer.valueOf(srcAndDestElementPrefs.get(prefsNameMinSpaceLeft, "-1"));
            youngerThanNDays = Integer.valueOf(srcAndDestElementPrefs.get(prefsNameYoungerThanNDays, "-1"));
            olderThanNDays = Integer.valueOf(srcAndDestElementPrefs.get(prefsNameOlderThanNDays, "-1"));
            includes = srcAndDestElementPrefs.get(prefsNameIncludes, ".*");
            excludes = srcAndDestElementPrefs.get(prefsNameExcludes, "");
        }

        public void savePrefs(Preferences node) {
            Preferences srcAndDestElementPrefs = SrcAndDestPrefsNodeName(node, name);
            sourceDescr.savePrefs(srcAndDestElementPrefs);
            destDescr.savePrefs(srcAndDestElementPrefs);
            srcAndDestElementPrefs.put(prefsNameCompressEnabled, String.valueOf(compressEnabled));
            srcAndDestElementPrefs.put(prefsNameEraseDestBeforeCopy, String.valueOf(eraseDestBeforeCopy));
            srcAndDestElementPrefs.put(prefsNameDeleteFromSourceAfterCopy, String.valueOf(deleteFromSourceAfterCopy));
            srcAndDestElementPrefs.put(prefsNameOverwrite, String.valueOf(overwrite));
            srcAndDestElementPrefs.put(prefsNameIncludeSubdirs, String.valueOf(includeSubdirs));
            srcAndDestElementPrefs.put(prefsNameMinfilesize, String.valueOf(minfilesize));
            srcAndDestElementPrefs.put(prefsNameMaxfilesize, String.valueOf(maxfilesize));
            srcAndDestElementPrefs.put(prefsNameMaxSpaceNeededOnDest, String.valueOf(maxSpaceNeededOnDest));
            srcAndDestElementPrefs.put(prefsNameMinSpaceLeft, String.valueOf(minSpaceLeft));
            srcAndDestElementPrefs.put(prefsNameYoungerThanNDays, String.valueOf(youngerThanNDays));
            srcAndDestElementPrefs.put(prefsNameOlderThanNDays, String.valueOf(olderThanNDays));
            srcAndDestElementPrefs.put(prefsNameIncludes, String.valueOf(includes));
            srcAndDestElementPrefs.put(prefsNameExcludes, String.valueOf(excludes));
        }

        public SourceDescr getSourceDescr() {
            return sourceDescr;
        }

        public void setSourceDescr(SourceDescr sourceDescr) {
            this.sourceDescr = sourceDescr;
        }

        public DestDescr getDestDescr() {
            return destDescr;
        }

        public void setDestDescr(DestDescr destDescr) {
            this.destDescr = destDescr;
        }

        public void copy() {
            if (eraseDestBeforeCopy) {
                File file = new File(destDescr.getPath());
                deleteR(file);
                file.mkdir();
            }
            transferDirContent(sourceDescr.getPath(), destDescr.getPath());
        }

        private void transferDirContent(String srcPath, String destPath) {
            File srcFile = new File(srcPath);
            String[] filename = srcFile.list();
            for (int i = 0; i < filename.length; i++) {
                transfer(srcPath + File.separator + filename[i], destPath);
            }
        }

        private void transfer(String srcPath, String destPath) {
            File srcFile = new File(srcPath);
            File destFile = new File(destPath);
            if (srcFile.isFile()) {
                long fileSize = srcFile.length();
                if (sourceDescr.mySrcAndDest.minfilesize != -1 && fileSize < sourceDescr.mySrcAndDest.minfilesize) {
                    System.out.println(srcPath + " not copied because of minFileSize violation");
                    return;
                }
                if (sourceDescr.mySrcAndDest.maxfilesize != -1 && fileSize > sourceDescr.mySrcAndDest.maxfilesize) {
                    System.out.println(srcPath + " not copied because of maxFileSize violation");
                    return;
                }
                long spaceLeft = destFile.getUsableSpace();
                if (destDescr.mySrcAndDest.minSpaceLeft != -1 && spaceLeft - fileSize < destDescr.mySrcAndDest.minSpaceLeft) {
                    System.out.println(srcPath + " not copied because of minSpaceLeft violation");
                    return;
                }
                long lastModified = srcFile.lastModified();
                long now = Calendar.getInstance().getTimeInMillis();
                long timeFromNow = now - lastModified;
                if (destDescr.mySrcAndDest.olderThanNDays != -1 && timeFromNow / (1000 * 60 * 60 * 24) < destDescr.mySrcAndDest.olderThanNDays) {
                    System.out.println(srcPath + " not copied: not older than violoation");
                    return;
                }
                if (destDescr.mySrcAndDest.youngerThanNDays != -1 && timeFromNow / (1000 * 60 * 60 * 24) > destDescr.mySrcAndDest.youngerThanNDays) {
                    System.out.println(srcPath + " not copied: not younger than violation");
                    return;
                }
                if (!srcPath.matches(sourceDescr.mySrcAndDest.includes)) {
                    System.out.println(srcPath + " not copied: not in includes");
                    return;
                }
                if (srcPath.matches(sourceDescr.mySrcAndDest.excludes)) {
                    System.out.println(srcPath + " not copied: is in excludes");
                    return;
                }
                copyFile(srcFile, destPath);
            } else {
                if (srcFile.isDirectory() & sourceDescr.mySrcAndDest.includeSubdirs) {
                    File destDir = new File(destPath + File.separator + srcFile.getName());
                    destDir.mkdirs();
                    String[] filename = srcFile.list();
                    for (int i = 0; i < filename.length; i++) {
                        transfer(srcPath + File.separator + filename[i], destDir.getPath());
                    }
                }
            }
        }

        private void copyFile(File srcFile, String destPath) {
            int filetype = determineFileType(srcFile.getName());
            if (filetype == SWT.IMAGE_GIF | filetype == SWT.IMAGE_JPEG) {
                BufferedImage bufferedImage;
                try {
                    String targetFormat = "jpg";
                    String newFileName = srcFile.getName();
                    newFileName = newFileName.substring(0, newFileName.lastIndexOf("."));
                    newFileName = newFileName + "." + targetFormat;
                    File destFile = new File(destPath + File.separator + newFileName);
                    if (destFile.exists() && !sourceDescr.mySrcAndDest.overwrite) {
                        return;
                    }
                    bufferedImage = ImageIO.read(srcFile);
                    float xVerhaeltnis = Float.valueOf(bufferedImage.getWidth()) / Float.valueOf(targetWidth);
                    float yVerhaeltnis = Float.valueOf(bufferedImage.getHeight()) / Float.valueOf(targetHeight);
                    int newWidth, newHeigth;
                    float redfaktor;
                    if (xVerhaeltnis > yVerhaeltnis) {
                        redfaktor = Float.valueOf(bufferedImage.getWidth()) / Float.valueOf(targetWidth);
                    } else {
                        redfaktor = Float.valueOf(bufferedImage.getHeight()) / Float.valueOf(targetHeight);
                    }
                    newWidth = (int) (Float.valueOf(bufferedImage.getWidth()) / redfaktor);
                    newHeigth = (int) (Float.valueOf(bufferedImage.getHeight()) / redfaktor);
                    BufferedImage thumbImage = new BufferedImage(newWidth, newHeigth, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2D = thumbImage.createGraphics();
                    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics2D.drawImage(bufferedImage, 0, 0, newWidth, newHeigth, null);
                    ImageIO.write(thumbImage, targetFormat, destFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Done.");
            } else {
                File destFile = new File(destPath + File.separator + srcFile.getName());
                if (destFile.exists() && !sourceDescr.mySrcAndDest.overwrite) {
                    return;
                }
                try {
                    FileCopy.copy(srcFile, destFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void deleteR(File file) {
            if (file.isFile()) {
                file.delete();
            } else {
                if (file.isDirectory()) {
                    String[] filename = file.list();
                    for (int i = 0; i < filename.length; i++) {
                        deleteR(new File(file.getPath() + File.separator + filename[i]));
                    }
                    file.delete();
                }
            }
        }
    }

    public static Preferences SrcAndDestPrefsNodeName(Preferences node, String name) {
        return node.node(SrcAndDest.prefsName + "/" + name);
    }

    class Copy2GoPreferences {

        private static final String srcAndDestPrefName = "srcAndDest";

        DevicePreferences devicePreferences;

        FileTypePreferences fileTypePreferences;

        Hashtable<String, SrcAndDest> srcAndDestTable = new Hashtable<String, SrcAndDest>();

        Hashtable<String, Button> deleteButtonTable = new Hashtable<String, Button>();

        DeleteButtonListener deleteButtonListener = new DeleteButtonListener();

        Copy2GoPreferences() {
            loadPrefs();
        }

        void loadPrefs() {
            devicePreferences = new DevicePreferences(prefs);
            fileTypePreferences = new FileTypePreferences(prefs);
            Preferences sourceAndDestNode = prefs.node(srcAndDestPrefName);
            try {
                String[] children = sourceAndDestNode.childrenNames();
                for (int i = 0; i < children.length; i++) {
                    srcAndDestTable.put(children[i], new SrcAndDest(prefs, children[i]));
                }
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }

        void savePrefs() {
            for (Enumeration<SrcAndDest> e = srcAndDestTable.elements(); e.hasMoreElements(); ) {
                SrcAndDest srcAndDest = e.nextElement();
                srcAndDest.savePrefs(prefs);
            }
            devicePreferences.savePrefs(prefs);
            fileTypePreferences.savePrefs(prefs);
        }

        public void setDeleteButton(String name, Button deleteButton) {
            deleteButtonTable.put(name, deleteButton);
            deleteButton.setImage(images[Copy2go.ciCross]);
            deleteButton.setData("name", name);
            deleteButton.addSelectionListener(deleteButtonListener);
        }

        class DeleteButtonListener implements SelectionListener {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                System.out.println("Deleting");
                Button b = (Button) e.getSource();
                String name = (String) b.getData("name");
                deleteButtonTable.remove(name);
                deleteSrcAndDest(name);
                refreshListOnUI();
            }
        }

        private void deleteSrcAndDest(String name) {
            Preferences prefsNode = SrcAndDestPrefsNodeName(prefs, name);
            try {
                prefsNode.removeNode();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
            srcAndDestTable.remove(name);
        }

        public void addSrcAndDest(Shell shell) {
            int counter = srcAndDestTable.size();
            while (srcAndDestTable.containsKey(Integer.toString(counter))) {
                counter++;
            }
            String name;
            name = Integer.toString(counter);
            SrcAndDest srcAndDest = new SrcAndDest(prefs, name);
            srcAndDestTable.put(name, srcAndDest);
            refreshListOnUI();
        }

        private void refreshListOnUI() {
            final Display display = shell.getDisplay();
            shell.dispose();
            shell = new Shell(display);
            open(shell);
        }
    }

    class DevicePreferences {

        private static final String prefsName = "devicePreferences";

        private int displayWidth;

        private static final String prefsDisplayWidthName = "displayWidth";

        private int displayHeight = 176;

        private static final String prefsDisplayHeightName = "displayHeight";

        public DevicePreferences(Preferences prefs) {
            Preferences devicePrefs = prefs.node(prefsName);
            displayWidth = devicePrefs.getInt(prefsDisplayWidthName, 220);
            displayHeight = devicePrefs.getInt(prefsDisplayHeightName, 176);
        }

        public void savePrefs(Preferences prefs) {
            Preferences prefNode = prefs.node(prefsName);
            prefNode.putInt(prefsDisplayWidthName, displayWidth);
            prefNode.putInt(prefsDisplayHeightName, displayHeight);
        }
    }

    class FileTypePreferences {

        private static final String prefsName = "fileTypePreferences";

        String videoFiles;

        private static final String prefsVideoFileName = "videoFile";

        String audioFiles;

        private static final String prefsAudioFileName = "audioFile";

        int audioFileMaxAge;

        private static final String prefsAudioMaxAgeFileName = "audioMaxAgeFile";

        String photoFiles;

        private static final String prefsPhotoFileName = "photoFile";

        public FileTypePreferences(Preferences prefs) {
            Preferences fileTypePrefs = prefs.node(prefsName);
            videoFiles = fileTypePrefs.get(prefsVideoFileName, "avi mpg mpeg mov");
            audioFiles = fileTypePrefs.get(prefsAudioFileName, "mp3 flac ape");
            audioFileMaxAge = fileTypePrefs.getInt(prefsAudioMaxAgeFileName, 10);
            photoFiles = fileTypePrefs.get(prefsPhotoFileName, "gif jpg jpeg png");
        }

        public void savePrefs(Preferences prefs) {
            Preferences prefNode = prefs.node(prefsName);
            prefNode.put(prefsVideoFileName, videoFiles);
            prefNode.put(prefsAudioFileName, audioFiles);
            prefNode.putInt(prefsAudioMaxAgeFileName, audioFileMaxAge);
            prefNode.put(prefsPhotoFileName, photoFiles);
        }
    }

    class SourcePathListener implements MouseListener {

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
        }

        public void mouseUp(MouseEvent e) {
            DirectoryDialog sourceChooser = new DirectoryDialog(shell, SWT.OPEN);
            String dirname = sourceChooser.open();
            if (dirname != null) {
                Text text = (Text) e.getSource();
                SrcAndDest srcAndDest = (SrcAndDest) text.getData(SrcAndDest.class.getName());
                srcAndDest.getSourceDescr().setPath(dirname);
            }
        }
    }

    class DestPathListener implements MouseListener {

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
        }

        public void mouseUp(MouseEvent e) {
            DirectoryDialog sourceChooser = new DirectoryDialog(shell, SWT.OPEN);
            String dirname = sourceChooser.open();
            if (dirname != null) {
                Text text = (Text) e.getSource();
                SrcAndDest srcAndDest = (SrcAndDest) text.getData(SrcAndDest.class.getName());
                srcAndDest.getDestDescr().setPath(dirname);
            }
        }
    }

    class SourceSettingsListener implements SelectionListener {

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void widgetSelected(SelectionEvent e) {
            Button b = (Button) e.getSource();
            SrcAndDest srcAndDest = (SrcAndDest) b.getData(SrcAndDest.class.getName());
            openShell4SourceDescr(srcAndDest);
        }
    }

    String nicePath(String path, int maxlength) {
        if (path.length() <= maxlength) {
            return path;
        }
        String prefix = "...";
        return prefix + path.substring(path.length() - maxlength - prefix.length());
    }

    public void openShell4SourceDescr(SrcAndDest srcAndDest) {
        SourcePreferencesDialog preferencesDialog = new SourcePreferencesDialog(srcAndDest);
        Display display = shell.getDisplay();
        preferencesDialog.open(display);
    }

    class PreferencesDialog {

        Button save;

        Button cancel;

        Shell dialog;

        Composite deviceTabControl() {
            Composite deviceTabControl = new Composite(tabFolder, SWT.NONE);
            deviceTabControl.setLayout(new GridLayout(2, false));
            propertyRow(deviceTabControl, "displayWidth", new Integer(copy2GoPreferences.devicePreferences.displayWidth).toString());
            propertyRow(deviceTabControl, "displayHeightLabel", new Integer(copy2GoPreferences.devicePreferences.displayHeight).toString());
            return deviceTabControl;
        }

        void propertyRow(Composite control, String labelText, String value) {
            Label label = new Label(control, SWT.NONE);
            label.setText(labelText);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Text text = new Text(control, SWT.SINGLE | SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 200;
            text.setLayoutData(gridData);
            text.setText(value);
        }

        Composite fileTypeTabControl() {
            Composite fileTypeTabControl = new Composite(tabFolder, SWT.NONE);
            fileTypeTabControl.setLayout(new GridLayout(2, false));
            propertyRow(fileTypeTabControl, "audioFiles", copy2GoPreferences.fileTypePreferences.audioFiles);
            propertyRow(fileTypeTabControl, "videoFiles", copy2GoPreferences.fileTypePreferences.videoFiles);
            propertyRow(fileTypeTabControl, "photoFiles", copy2GoPreferences.fileTypePreferences.photoFiles);
            return fileTypeTabControl;
        }

        void createTabFolder(Shell dialog) {
            tabFolder = new TabFolder(dialog, SWT.NONE);
            TabItem deviceTabItem = new TabItem(tabFolder, SWT.NONE);
            deviceTabItem.setText("Device");
            deviceTabItem.setControl(deviceTabControl());
            TabItem fileTypeTabItem = new TabItem(tabFolder, SWT.NONE);
            fileTypeTabItem.setText("Filetype");
            fileTypeTabItem.setControl(fileTypeTabControl());
        }

        void createButtons(Shell dialog) {
            Composite buttonComposite = new Composite(dialog, SWT.NONE);
            buttonComposite.setLayout(new GridLayout(2, false));
            cancel = new Button(buttonComposite, SWT.PUSH);
            cancel.setText(bundle.getString("Cancel"));
            save = new Button(buttonComposite, SWT.PUSH);
            save.setText("Save");
        }

        public Shell open(Display display) {
            dialog = new Shell(display);
            dialog.setText("//TODO getText()");
            createTabFolder(dialog);
            createButtons(dialog);
            save.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    savePreferences();
                    dialog.dispose();
                }
            });
            cancel.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    dialog.dispose();
                }
            });
            dialog.setDefaultButton(save);
            dialog.setLayout(new GridLayout());
            dialog.pack();
            dialog.open();
            return dialog;
        }
    }

    class SourcePreferencesDialog {

        Button save;

        Button cancel;

        Shell dialog;

        private SrcAndDest srcAndDest;

        private Button compressEnabledButton;

        private Button eraseDestBeforeCopyButton;

        private Button deleteFromSourceAfterCopyButton;

        private Button includeSubdirsButton;

        private Button overwriteButton;

        private Text minfilesizeText;

        private Text maxfilesizeText;

        private Text maxSpaceNeededOnDestText;

        private Text minSpaceLeftText;

        private Text youngerThanNDaysText;

        private Text olderThanNDaysText;

        private Text includesText;

        private Text excludesText;

        public SourcePreferencesDialog(SrcAndDest srcAndDest) {
            super();
            this.srcAndDest = srcAndDest;
        }

        String niceValueOfLong(long i) {
            if (i == -1) {
                return "";
            } else {
                return String.valueOf(i);
            }
        }

        Composite sourceTabControl() {
            Composite sourceTabControl = new Composite(tabFolder, SWT.NONE);
            sourceTabControl.setLayout(new GridLayout(2, false));
            compressEnabledButton = booleanRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.compresslabel.text"), srcAndDest.compressEnabled);
            eraseDestBeforeCopyButton = booleanRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.erasedestbeforecopy.text"), srcAndDest.eraseDestBeforeCopy);
            deleteFromSourceAfterCopyButton = booleanRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.deletefromsourceaftercopy.text"), srcAndDest.deleteFromSourceAfterCopy);
            overwriteButton = booleanRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.overwrite.text"), srcAndDest.overwrite);
            includeSubdirsButton = booleanRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.includesubdirs.text"), srcAndDest.includeSubdirs);
            minfilesizeText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.minfilesize.text"), niceValueOfLong(srcAndDest.minfilesize));
            maxfilesizeText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.maxfilesize.text"), niceValueOfLong(srcAndDest.maxfilesize));
            maxSpaceNeededOnDestText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.maxspaceneededondest.text"), niceValueOfLong(srcAndDest.maxSpaceNeededOnDest));
            minSpaceLeftText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.minspaceleft.text"), niceValueOfLong(srcAndDest.minSpaceLeft));
            youngerThanNDaysText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.youngerthan.text"), niceValueOfLong(srcAndDest.youngerThanNDays));
            olderThanNDaysText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.olderthan.text"), niceValueOfLong(srcAndDest.olderThanNDays));
            includesText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.includes.text"), String.valueOf(srcAndDest.includes));
            excludesText = propertyRow(sourceTabControl, getResourceString("sourceDialog.sourcetab.excludes.text"), String.valueOf(srcAndDest.excludes));
            return sourceTabControl;
        }

        Composite deviceTabControl() {
            Composite deviceTabControl = new Composite(tabFolder, SWT.NONE);
            deviceTabControl.setLayout(new GridLayout(2, false));
            propertyRow(deviceTabControl, "displayWidth", new Integer(copy2GoPreferences.devicePreferences.displayWidth).toString());
            propertyRow(deviceTabControl, "displayHeightLabel", new Integer(copy2GoPreferences.devicePreferences.displayHeight).toString());
            return deviceTabControl;
        }

        Text propertyRow(Composite control, String labelText, String value) {
            Label label = new Label(control, SWT.NONE);
            label.setText(labelText);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Text text = new Text(control, SWT.SINGLE | SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 200;
            text.setLayoutData(gridData);
            text.setText(value);
            return text;
        }

        Button booleanRow(Composite control, String labelText, boolean value) {
            Label label = new Label(control, SWT.NONE);
            label.setText(labelText);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Button b;
            b = new Button(control, SWT.CHECK);
            b.setSelection(value);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 200;
            b.setLayoutData(gridData);
            return b;
        }

        Composite fileTypeTabControl() {
            Composite fileTypeTabControl = new Composite(tabFolder, SWT.NONE);
            fileTypeTabControl.setLayout(new GridLayout(2, false));
            propertyRow(fileTypeTabControl, "audioFiles", copy2GoPreferences.fileTypePreferences.audioFiles);
            propertyRow(fileTypeTabControl, "videoFiles", copy2GoPreferences.fileTypePreferences.videoFiles);
            propertyRow(fileTypeTabControl, "photoFiles", copy2GoPreferences.fileTypePreferences.photoFiles);
            return fileTypeTabControl;
        }

        void createTabFolder(Shell dialog) {
            tabFolder = new TabFolder(dialog, SWT.NONE);
            TabItem sourceTabItem = new TabItem(tabFolder, SWT.NONE);
            sourceTabItem.setText(getResourceString("sourceDialog.sourcetab.title.text"));
            sourceTabItem.setControl(sourceTabControl());
            TabItem deviceTabItem = new TabItem(tabFolder, SWT.NONE);
            deviceTabItem.setText("Device");
            deviceTabItem.setControl(deviceTabControl());
            TabItem fileTypeTabItem = new TabItem(tabFolder, SWT.NONE);
            fileTypeTabItem.setText("Filetype");
            fileTypeTabItem.setControl(fileTypeTabControl());
        }

        void createButtons(Shell dialog) {
            Composite buttonComposite = new Composite(dialog, SWT.NONE);
            buttonComposite.setLayout(new GridLayout(2, false));
            cancel = new Button(buttonComposite, SWT.PUSH);
            cancel.setText(bundle.getString("Cancel"));
            save = new Button(buttonComposite, SWT.PUSH);
            save.setText("Save");
        }

        long longValueOf(String s) {
            long l;
            try {
                l = Long.valueOf(s);
            } catch (NumberFormatException e) {
                l = -1;
            }
            return l;
        }

        public Shell open(Display display) {
            dialog = new Shell(display);
            dialog.setText(getResourceString("sourceDialog.title.text"));
            createTabFolder(dialog);
            createButtons(dialog);
            save.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    srcAndDest.compressEnabled = compressEnabledButton.getSelection();
                    srcAndDest.eraseDestBeforeCopy = eraseDestBeforeCopyButton.getSelection();
                    srcAndDest.deleteFromSourceAfterCopy = deleteFromSourceAfterCopyButton.getSelection();
                    srcAndDest.overwrite = overwriteButton.getSelection();
                    srcAndDest.includeSubdirs = includeSubdirsButton.getSelection();
                    srcAndDest.minfilesize = longValueOf(minfilesizeText.getText());
                    srcAndDest.maxfilesize = longValueOf(maxfilesizeText.getText());
                    srcAndDest.maxSpaceNeededOnDest = longValueOf(maxSpaceNeededOnDestText.getText());
                    srcAndDest.minSpaceLeft = longValueOf(minSpaceLeftText.getText());
                    srcAndDest.youngerThanNDays = longValueOf(youngerThanNDaysText.getText());
                    srcAndDest.olderThanNDays = longValueOf(olderThanNDaysText.getText());
                    srcAndDest.includes = includesText.getText();
                    srcAndDest.excludes = excludesText.getText();
                    savePreferences();
                    dialog.dispose();
                }
            });
            cancel.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    dialog.dispose();
                }
            });
            dialog.setDefaultButton(save);
            dialog.setLayout(new GridLayout());
            dialog.pack();
            dialog.open();
            return dialog;
        }
    }

    private void savePreferences() {
        System.out.println("Saving prefs");
    }

    static final void myMain() {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        Copy2go instance = new Copy2go(shell);
        running = true;
        while (running) {
            if (!display.readAndDispatch()) display.sleep();
        }
        instance.copy2GoPreferences.savePrefs();
        instance.dispose();
        display.dispose();
    }

    public static void main(String[] args) {
        myMain();
    }

    /**
	 * Disposes of all resources associated with a particular
	 * instance of Copy2Go.
	 */
    public void dispose() {
        tabFolder = null;
        freeResources();
    }

    /**
	 * Frees the resources
	 */
    void freeResources() {
    }

    public void open(Shell shell) {
        this.shell = shell;
        shell.setText(bundle.getString("Copy2go"));
        shell.addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent event) {
            }
        });
        shell.addShellListener(new ShellAdapter() {

            public void shellClosed(ShellEvent e) {
                e.doit = true;
            }
        });
        createMenuBar();
        createWidgets();
        shell.pack();
        shell.open();
    }

    private void createWidgets() {
        GridLayout gridLayout = new GridLayout();
        shell.setLayout(gridLayout);
        shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        shell.setText("Copy2Go");
        int fontSize = 60;
        int fontFace = 0;
        int fontStyle = SWT.ITALIC;
        Font font = new Font(shell.getDisplay(), getPlatformFontFace(fontFace), fontSize, fontStyle);
        Label headline = new Label(shell, SWT.CENTER);
        headline.setText("Copy2Go");
        headline.setFont(font);
        headline.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        headline.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
        GridLayout topLayout = new GridLayout(3, true);
        topLayout.makeColumnsEqualWidth = true;
        topLayout.marginLeft = 30;
        topLayout.marginRight = 30;
        Composite top = new Composite(shell, SWT.CENTER);
        top.setLayout(topLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        top.setLayoutData(gridData);
        top.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_CYAN));
        Composite leftComp = new Composite(top, SWT.NONE);
        Image mug = images[Copy2go.ciMug];
        Rectangle mugBounds = mug.getBounds();
        Canvas canvas = new Canvas(leftComp, SWT.NONE);
        canvas.setSize(mugBounds.width, mugBounds.height);
        canvas.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                e.gc.drawImage(images[Copy2go.ciMug], 0, 0);
            }
        });
        goButton = new Button(top, SWT.PUSH | SWT.CENTER);
        goButton.setImage(images[Copy2go.ciSettingsOrange]);
        goButton.setText(bundle.getString("Transfer"));
        GridData centerGridData = new GridData();
        centerGridData.horizontalAlignment = GridData.FILL;
        centerGridData.grabExcessHorizontalSpace = true;
        goButton.setLayoutData(centerGridData);
        goButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                transfer(shell);
            }
        });
        Composite rightComp = new Composite(top, SWT.NONE);
        rightComp.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
        Image espresso = images[Copy2go.ciEspresso];
        Rectangle espressoBounds = espresso.getBounds();
        Canvas espressoCanvas = new Canvas(rightComp, SWT.NONE);
        espressoCanvas.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        espressoCanvas.setSize(espressoBounds.width, espressoBounds.height);
        espressoCanvas.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                e.gc.drawImage(images[Copy2go.ciEspresso], 0, 0);
            }
        });
        top.pack();
        center = new Composite(shell, SWT.NONE);
        center.setLayout(new GridLayout(5, false));
        populateCenter(center);
        center.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
        goButton = new Button(shell, SWT.CENTER);
        goButton.setText(bundle.getString("OneMoreRow"));
        goButton.setImage(images[Copy2go.ciPlus]);
        goButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                copy2GoPreferences.addSrcAndDest(shell);
            }
        });
        shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    }

    private void populateCenter(Composite center) {
        center.setLayout(new GridLayout(4, false));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 250;
        for (Enumeration<SrcAndDest> e = copy2GoPreferences.srcAndDestTable.elements(); e.hasMoreElements(); ) {
            SrcAndDest srcAndDest = e.nextElement();
            System.out.println("Adding " + srcAndDest.sourceDescr.path);
            Button fromSelectButton = new Button(center, SWT.FLAT | SWT.CENTER);
            srcAndDest.sourceDescr.setButton(fromSelectButton);
            Text sourceText = new Text(center, SWT.SINGLE | SWT.BORDER);
            srcAndDest.sourceDescr.setText(sourceText);
            Text destText = new Text(center, SWT.SINGLE | SWT.BORDER);
            srcAndDest.destDescr.setText(destText);
            Button deleteButton = new Button(center, SWT.FLAT | SWT.CENTER);
            copy2GoPreferences.setDeleteButton(srcAndDest.name, deleteButton);
        }
        center.pack();
        center.pack(true);
        center.redraw();
        shell.pack();
        shell.redraw();
    }

    Menu createMenuBar() {
        Menu menuBar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);
        createFileMenu(menuBar);
        createEditMenu(menuBar);
        createHelpMenu(menuBar);
        return menuBar;
    }

    void createFileMenu(Menu menuBar) {
        MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
        item.setText(bundle.getString("File"));
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(fileMenu);
        item = new MenuItem(fileMenu, SWT.PUSH);
        item.setText(bundle.getString("Exit"));
        item.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                running = false;
            }
        });
    }

    void createEditMenu(Menu menuBar) {
        MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
        item.setText(bundle.getString("Edit"));
        Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(editMenu);
        item = new MenuItem(editMenu, SWT.PUSH);
        item.setText(bundle.getString("Preferences"));
        item.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                preferences(shell);
            }
        });
    }

    /**
	 * Creates the Help Menu.
	 * 
	 * @param parent the parent menu
	 */
    private void createHelpMenu(Menu parent) {
        Menu menu = new Menu(parent);
        MenuItem header = new MenuItem(parent, SWT.CASCADE);
        header.setText(getResourceString("menu.Help.text"));
        header.setMenu(menu);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(getResourceString("menu.Help.About.text"));
        item.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                box.setText(getResourceString("dialog.About.title"));
                box.setMessage(getResourceString("dialog.About.description", new Object[] { System.getProperty("os.name") }));
                box.open();
            }
        });
    }

    void showErrorDialog(Shell shell, String operation, String filename, Throwable e) {
        MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
        String message = "Leider auskommentiert";
        String errorMessage = "";
        if (e != null) {
            if (e instanceof SWTException) {
                SWTException swte = (SWTException) e;
                errorMessage = swte.getMessage();
                if (swte.throwable != null) {
                    errorMessage += ":\n" + swte.throwable.toString();
                }
            } else if (e instanceof SWTError) {
                SWTError swte = (SWTError) e;
                errorMessage = swte.getMessage();
                if (swte.throwable != null) {
                    errorMessage += ":\n" + swte.throwable.toString();
                }
            } else {
                errorMessage = e.toString();
            }
        }
        box.setMessage(message + errorMessage);
        box.open();
    }

    int determineFileType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        if (ext.equalsIgnoreCase("bmp")) return SWT.IMAGE_BMP;
        if (ext.equalsIgnoreCase("gif")) return SWT.IMAGE_GIF;
        if (ext.equalsIgnoreCase("ico")) return SWT.IMAGE_ICO;
        if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("jfif")) return SWT.IMAGE_JPEG;
        if (ext.equalsIgnoreCase("png")) return SWT.IMAGE_PNG;
        if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) return SWT.IMAGE_TIFF;
        return SWT.IMAGE_UNDEFINED;
    }

    private void preferences(Shell parentShell) {
        PreferencesDialog preferencesDialog = new PreferencesDialog();
        Display display = parentShell.getDisplay();
        preferencesDialog.open(display);
    }

    void transfer(Shell shell) {
        Display display = shell.getDisplay();
        Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
        shell.setCursor(waitCursor);
        System.out.println("Bin in Transfer");
        Hashtable<String, SrcAndDest> srcAndDestTable = this.copy2GoPreferences.srcAndDestTable;
        for (Enumeration<SrcAndDest> e = srcAndDestTable.elements(); e.hasMoreElements(); ) {
            SrcAndDest srcAndDest = e.nextElement();
            System.out.println("Copying from " + srcAndDest.getSourceDescr().getPath() + " to " + srcAndDest.getDestDescr().getPath());
            srcAndDest.copy();
        }
        System.out.println("Transfer done.");
        shell.setCursor(null);
        waitCursor.dispose();
    }

    /**
	 * Returns the name of the font using the specified index.
	 * This method takes into account the resident platform.
	 * 
	 * @param index
	 * 			The index of the font to be used
	 */
    static String getPlatformFontFace(int index) {
        if (SWT.getPlatform() == "win32") {
            return new String[] { "Arial", "Impact", "Times", "Verdana" }[index];
        } else if (SWT.getPlatform() == "motif") {
            return new String[] { "URW Chancery L", "URW Gothic L", "Times", "qub" }[index];
        } else if (SWT.getPlatform() == "gtk") {
            return new String[] { "URW Chancery L", "Baekmuk Batang", "Baekmuk Headline", "KacsTitleL" }[index];
        } else if (SWT.getPlatform() == "carbon") {
            return new String[] { "Arial", "Impact", "Times", "Verdana" }[index];
        } else {
            return new String[] { "Arial", "Impact", "Times", "Verdana" }[index];
        }
    }

    /**
	 * Loads the resources
	 */
    void initResources() {
        final Class<Copy2go> clazz = Copy2go.class;
        if (bundle != null) {
            try {
                if (images == null) {
                    images = new Image[imageLocations.length];
                    for (int i = 0; i < imageLocations.length; ++i) {
                        InputStream sourceStream = clazz.getResourceAsStream(imageLocations[i]);
                        ImageData source = new ImageData(sourceStream);
                        if (imageTypes[i] == SWT.ICON) {
                            ImageData mask = source.getTransparencyMask();
                            images[i] = new Image(null, source, mask);
                        } else {
                            images[i] = new Image(null, source);
                        }
                        try {
                            sourceStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return;
            } catch (Throwable t) {
            }
        }
        String error = (bundle != null) ? getResourceString("error.CouldNotLoadResources") : "Unable to load resources";
        freeResources();
        throw new RuntimeException(error);
    }

    /**
	 * Gets a string from the resource bundle.
	 * We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 */
    static String getResourceString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!";
        }
    }

    /**
	 * Returns a string from the resource bundle and binds it
	 * with the given arguments. If the key is not found,
	 * return the key.
	 */
    static String getResourceString(String key, Object[] args) {
        try {
            return MessageFormat.format(getResourceString(key), args);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!";
        }
    }
}
