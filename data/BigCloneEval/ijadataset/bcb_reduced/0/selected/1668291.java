package net.jalbum.editor;

import edu.stanford.ejalbert.BrowserLauncher;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.BasicCompletion;
import se.datadosen.component.ControlPanel;
import se.datadosen.io.ChainedDirectory;
import se.datadosen.jalbum.Config;
import se.datadosen.jalbum.JAlbumFrame;
import se.datadosen.jalbum.JAlbumPlugin;
import se.datadosen.jalbum.PluginContext;
import se.datadosen.jalbum.TextEditor;
import se.datadosen.jalbum.Tracer;
import se.datadosen.util.IO;
import se.datadosen.util.PropertyBinder;

/**
 * Glue between jAlbum and JTextPad.
 * Installs JTextPad as a menu option under jAlbum's Tools menu
 * Also feeds skin specific variables to JTextPad's autocompleter
 * @author david
 */
public class JTextPadPlugin implements JAlbumPlugin, TextEditor.Editor {

    private PluginContext context;

    private JAlbumFrame window;

    private Set<String> ignoreNames = new HashSet<String>();

    {
        ignoreNames.add("Thumbs.db");
        ignoreNames.add(".DS_Store");
        ignoreNames.add(".svn");
        ignoreNames.add(".cvs");
    }

    Action reloadSkin = new AbstractAction("Reload skin") {

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, JTextPad.menuShortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            window.mainSettingsPanel.reloadSkin();
        }
    };

    Action editSkinFiles = new AbstractAction("Edit skin files") {

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, JTextPad.menuShortcutKeyMask | KeyEvent.SHIFT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            Tracer.getInstance().trace("edit skin files");
            final JTextPad textpad = JTextPad.getLastUsedInstance();
            final File skinDir = context.getJAlbumContext().getSkinDir();
            final File[] files = skinDir.listFiles();
            new Thread("loader thread") {

                @Override
                public void run() {
                    boolean first = true;
                    for (File f : files) {
                        String ext = IO.extensionOf(f).toLowerCase();
                        if (JTextPad.fileTypes.containsKey(ext)) {
                            JTextPadDocument document = getConfiguredDocument(textpad, f);
                            if (first) {
                                textpad.setCurrentDocument(document);
                                textpad.toFront();
                                first = false;
                            }
                        }
                    }
                }
            }.start();
        }
    };

    Action editSkinProps = new AbstractAction("Edit skin properties") {

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            new nl.tomcee.skinprops.SkinPropsEditor();
        }
    };

    Action openEmptyEditor = new AbstractAction("Open empty editor") {

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, JTextPad.menuShortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            Tracer.getInstance().trace("open empty editor");
            JTextPad textpad = JTextPad.getLastUsedInstance();
            if (textpad.isVisible()) {
                textpad.toFront();
                return;
            }
            File skinDir = context.getJAlbumContext().getSkinDir();
            JTextPadDocument document = getConfiguredDocument(textpad, skinDir);
            textpad.setCurrentDocument(document);
        }
    };

    Action createNewSkin = new AbstractAction("Create new skin...") {

        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(window, "Please enter name for new skin");
            if (name == null) {
                return;
            }
            ChainedDirectory chainedSkinsDir = Config.getConfig().chainedSkinsDir;
            File newSkinDir = new File(chainedSkinsDir.getDirectory(), name);
            if (newSkinDir.exists()) {
                JOptionPane.showMessageDialog(window, "Skin " + name + " already exists", "Create new skin", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File sourceSkinDir = new File(Config.getConfig().skinsDir, "Minimal");
            if (!sourceSkinDir.exists()) {
                JOptionPane.showMessageDialog(window, "Can't find skin Minimal to use as template for new skin creation", "Create new skin", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                newSkinDir.mkdir();
                IO.copyFile(sourceSkinDir, newSkinDir);
                window.mainSettingsPanel.scanSkins();
                window.ui2Engine();
                context.getJAlbumContext().getEngine().setSkin(name);
                window.engine2UI();
                int answer = JOptionPane.showConfirmDialog(window, "Skin " + name + " created ok with Minimal skin as base.\nDo you want to edit it now?", "Create new skin", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    editSkinFiles.actionPerformed(null);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Error creating new skin: " + ex.getMessage(), "Create new skin", JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    Action packSkin = new AbstractAction("Pack as .jaskin file") {

        public void actionPerformed(ActionEvent e) {
            final String name = context.getJAlbumContext().getEngine().getSkin();
            final String packedName = name + ".jaskin";
            window.statusBar.setText("Packing skin " + name + " to " + packedName + "...");
            new Thread("Skin packer") {

                @Override
                public void run() {
                    ChainedDirectory chainedSkinsDir = Config.getConfig().chainedSkinsDir;
                    File skinDir = chainedSkinsDir.getFile(name);
                    try {
                        File skinFile = new File(skinDir.getParentFile(), packedName);
                        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(skinFile)));
                        packDir(skinDir, skinDir.getParentFile(), zout);
                        zout.finish();
                        zout.close();
                        window.statusBar.setText("Skin " + name + " packed as " + packedName);
                        BrowserLauncher.showInFileSystem(skinFile);
                    } catch (IOException ex) {
                        window.statusBar.setText("Skin " + name + " packing aborted due to error");
                        JOptionPane.showMessageDialog(window, "Error packing skin: " + ex.getMessage(), "Pack skin", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.start();
        }

        private void packDir(File dir, File rel, ZipOutputStream zout) throws IOException {
            byte[] buffer = new byte[65536];
            ZipEntry e = new ZipEntry(IO.relativePath(dir, rel) + '/');
            e.setTime(dir.lastModified());
            zout.putNextEntry(e);
            for (File f : dir.listFiles(new FileFilter() {

                public boolean accept(File test) {
                    String s = test.getName();
                    return !s.equals(".DS_Store") && !s.toLowerCase().equals(".svn") && !s.toLowerCase().equals(".cvs");
                }
            })) {
                if (f.isDirectory()) {
                    packDir(f, rel, zout);
                } else {
                    e = new ZipEntry(IO.relativePath(f, rel));
                    e.setTime(f.lastModified());
                    zout.putNextEntry(e);
                    InputStream in = new BufferedInputStream(new FileInputStream(f));
                    try {
                        int bytes;
                        while ((bytes = in.read(buffer)) >= 0) {
                            zout.write(buffer, 0, bytes);
                        }
                        zout.closeEntry();
                    } finally {
                        in.close();
                    }
                }
            }
        }
    };

    Action prepareForOnlineUse = new AbstractAction("Prepare for online and console use...") {

        public void actionPerformed(ActionEvent e) {
            window.ui2Engine();
            String name = context.getJAlbumContext().getEngine().getSkin();
            int answer = JOptionPane.showConfirmDialog(window, "This will make this skin also work in situations where jAlbum's window isn't present\n" + "like console mode and online use (embedded inside a web server)\n\n" + "A property file called 'headless-properties.jap' is stored in the skin's directory.\n" + "This file contains all current skin settings. It will seed the album engine\n" + "with all required variables, which are normally seeded from the user interface.\n\n" + "Do you want to prepare skin?", "Prepare for online and console use", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
            ChainedDirectory chainedSkinsDir = Config.getConfig().chainedSkinsDir;
            File skinDir = chainedSkinsDir.getFile(name);
            ControlPanel skinUI = window.getSkinUI();
            if (skinUI != null) {
                Properties props = new Properties();
                Iterator it = PropertyBinder.getProperties(skinUI).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry item = (Map.Entry) it.next();
                    props.setProperty("skin." + item.getKey(), item.getValue().toString());
                }
                File headlessFile = new File(skinDir, "headless-settings.jap");
                try {
                    FileOutputStream fout = new FileOutputStream(headlessFile);
                    props.store(fout, "These settings are applied when running jAlbum without its window");
                    fout.close();
                    window.statusBar.setText("Skin settings saved to " + headlessFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    };

    Action deleteSkin = new AbstractAction("Delete selected skin...") {

        public void actionPerformed(ActionEvent e) {
            window.ui2Engine();
            String name = context.getJAlbumContext().getEngine().getSkin();
            int answer = JOptionPane.showConfirmDialog(window, "Are you sure you wish to delete the " + name + " skin?", "Delete selected skin", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
            ChainedDirectory chainedSkinsDir = Config.getConfig().chainedSkinsDir;
            File skinDir = chainedSkinsDir.getFile(name);
            context.getJAlbumContext().getEngine().unloadCurrentSkin();
            context.getJAlbumContext().getEngine().setSkin("");
            System.out.println("Skin is " + context.getJAlbumContext().getEngine().getSkin());
            IO.recycle(skinDir);
            window.mainSettingsPanel.scanSkins();
        }
    };

    private JTextPadDocument getConfiguredDocument(JTextPad textpad, File f) {
        JTextPadDocument document = textpad.getOpenDocumentFor(f);
        if (document == null) {
            document = textpad.openDocument(f);
            configureDocument(document);
        }
        return document;
    }

    public void init(final PluginContext context) {
        this.context = context;
        this.window = context.getJAlbumContext().getFrame();
        TextEditor.install(this);
        JMenu menu = new JMenu("Skin developer");
        menu.add(new JMenuItem(editSkinFiles));
        menu.add(new JMenuItem(editSkinProps));
        menu.add(new JMenuItem(openEmptyEditor));
        menu.addSeparator();
        menu.add(new JMenuItem(reloadSkin));
        menu.addSeparator();
        menu.add(new JMenuItem(createNewSkin));
        menu.add(new JMenuItem(prepareForOnlineUse));
        menu.add(new JMenuItem(packSkin));
        menu.add(new JMenuItem(deleteSkin));
        context.addToolsMenuItem(menu);
    }

    /**
     * Add skin specific and user variables to the editor
     */
    public void configureDocument(JTextPadDocument document) {
        AbstractCompletionProvider provider = document.getCompletionProvider();
        context.getJAlbumContext().getFrame().ui2Engine();
        List list = new LinkedList();
        Map skinVars = context.getJAlbumContext().getEngine().getSkinVariables();
        Set<Entry<Object, Object>> entries = skinVars.entrySet();
        for (Entry<Object, Object> e : entries) {
            BasicCompletion cpl = new BasicCompletion(provider, e.getKey().toString(), context.getJAlbumContext().getEngine().getSkin(), "Sample value: " + e.getValue());
            list.add(cpl);
        }
        Map userVars = context.getJAlbumContext().getEngine().getUserVariables();
        entries = userVars.entrySet();
        for (Entry<Object, Object> e : entries) {
            BasicCompletion cpl = new BasicCompletion(provider, e.getKey().toString(), "User variable", "Sample value: " + e.getValue());
            list.add(cpl);
        }
        provider.addCompletions(list);
    }

    public boolean onExit() {
        try {
            LinkedList<JTextPad> editorsClone = new LinkedList<JTextPad>(JTextPad.editors);
            for (JTextPad textpad : editorsClone) {
                textpad.exitAction.actionPerformed(null);
            }
            return true;
        } catch (OperationCanceledException ex) {
        }
        return false;
    }

    /**
     * For TextEditor.Editor interface
     */
    public void openDocument(File f) {
        JTextPad editor = JTextPad.getLastUsedInstance();
        if (editor.isVisible()) {
            editor.toFront();
        }
        JTextPadDocument document = getConfiguredDocument(editor, f);
        editor.setCurrentDocument(document);
    }

    public JTextComponent getCurrentEditorComponent() {
        JTextPad editor = JTextPad.getLastUsedInstance();
        return editor.getCurrentDocument().getTextArea();
    }

    public JTextComponent newDocument() {
        JTextPad editor = JTextPad.getLastUsedInstance();
        JTextPadDocument document = editor.openDocument(null);
        editor.setCurrentDocument(document);
        return document.getTextArea();
    }
}
