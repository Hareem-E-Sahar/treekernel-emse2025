package org.skunk.dav.client.gui.editor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import org.skunk.config.Configurator;
import org.skunk.dav.client.DAVFile;
import org.skunk.dav.client.gui.ExplorerApp;
import org.skunk.swing.text.syntax.FileMode;
import org.skunk.swing.text.syntax.SyntaxStyle;

public abstract class DAVEditorFactory {

    static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DAVEditorFactory.class);

    public static Properties editorProps;

    public static final String EDITOR_PROPERTIES = "editor.properties";

    public static final String EXTENSION_PREFIX = "extension-";

    public static final String EXTENSION_SEPARATOR = "/";

    private static HashMap extensionHash;

    private static Configurator configurator;

    static {
        editorProps = new Properties();
        extensionHash = new HashMap();
        try {
            editorProps.load(DAVEditorFactory.class.getResourceAsStream(EDITOR_PROPERTIES));
            if (log.isDebugEnabled()) log.trace(editorProps.toString());
            makeExtensionHash();
        } catch (IOException oyVeh) {
            log.error("Exception", oyVeh);
        }
        configurator = ExplorerApp.getAppContext().getConfigurator();
        configurator.configure(FileMode.getModeMapContainer());
        configurator.configure(SyntaxStyle.getDefaultStyleSet());
    }

    private static void makeExtensionHash() {
        for (Enumeration eenum = editorProps.keys(); eenum.hasMoreElements(); ) {
            String key = eenum.nextElement().toString();
            if (log.isDebugEnabled()) log.trace("processing key: " + key);
            if (key.startsWith(EXTENSION_PREFIX)) {
                StringTokenizer st = new StringTokenizer(key.substring(EXTENSION_PREFIX.length()), EXTENSION_SEPARATOR);
                String classStr = editorProps.getProperty(key);
                while (st.hasMoreTokens()) {
                    extensionHash.put(st.nextToken().toLowerCase().trim(), classStr);
                }
            }
        }
        if (log.isDebugEnabled()) log.trace("extensionHash initialized: {0}", extensionHash);
    }

    public static final DAVEditor getEditor(DAVFile file) {
        String filename = file.getFileName();
        int lastDot = filename.lastIndexOf(".") + 1;
        if (lastDot > 0 && (lastDot) < filename.length()) {
            String extension = filename.substring(lastDot).toLowerCase().trim();
            if (extensionHash.containsKey(extension)) {
                String editorName = extensionHash.get(extension).toString();
                try {
                    Class editorClass = Class.forName(editorName);
                    Constructor editorConstructor = editorClass.getConstructor(new Class[] { DAVFile.class });
                    Object editorObject = editorConstructor.newInstance(new Object[] { file });
                    if (editorObject instanceof DAVEditor) {
                        configurator.configure(editorObject);
                        return (DAVEditor) editorObject;
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }
        return getDefaultEditor(file);
    }

    public static final DAVEditor getDefaultEditor(DAVFile file) {
        DAVEditor ed = new SimpleTextEditor(file);
        configurator.configure(ed);
        return ed;
    }
}
