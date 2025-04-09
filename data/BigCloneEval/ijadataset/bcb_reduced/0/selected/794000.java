package scripting;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import ui.composites.OptionCompositeContainer;
import ui.composites.ScriptingComposite;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * The Class ScriptWatcher.
 */
public final class ScriptWatcher implements Runnable {

    /** The watcher. */
    private WatchService watcher;

    /** The keys. */
    private Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();

    /**
	 * Instantiates a new script watcher.
	 */
    public ScriptWatcher() {
        File f = new File("./scripts/");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            register(Paths.get("./scripts/"));
        } catch (IOException e) {
            org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scriptwatcher");
            fLog.error("ScriptWatcher was unable to initialize.", e);
        }
    }

    /**
	 * Register the script directory.
	 *
	 * @param dir the dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
        for (File f : dir.toFile().listFiles()) {
            if (f.isDirectory() || (!f.toString().contains(".js") && !f.toString().contains(".rb") && !f.toString().contains(".py") && !f.toString().contains(".lua"))) continue;
            ScriptManager.addScript(f);
        }
    }

    /**
	 * Update the files as they get modified.
	 *
	 * @param event the event
	 * @param file the file
	 */
    private void updateFile(WatchEvent<?> event, File file) {
        WatchEvent.Kind<?> eventType = event.kind();
        if (eventType == ENTRY_MODIFY) {
            ScriptManager.modifyScript(file);
        } else if (eventType == ENTRY_CREATE) {
            ScriptManager.addScript(file);
        } else if (eventType == ENTRY_DELETE) {
            ScriptManager.removeScript(file);
        }
        if (OptionCompositeContainer.getCurrentComposite() instanceof ScriptingComposite) {
            ScriptingComposite c = (ScriptingComposite) OptionCompositeContainer.getCurrentComposite();
            c.updateTreeItems();
        }
    }

    @Override
    public void run() {
        while (true) {
            WatchKey key = null;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scriptwatcher");
                fLog.error("Key taking intrrupted.", x);
            }
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("Unrecognized key: " + key);
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name);
                File f = child.toFile();
                updateFile(event, f);
            }
            key.reset();
        }
    }
}
