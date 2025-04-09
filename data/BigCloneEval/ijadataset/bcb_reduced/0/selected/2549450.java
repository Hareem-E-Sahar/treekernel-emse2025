package javazoom.jlgui.player.amp.playlist;

import java.lang.reflect.Constructor;
import javazoom.jlgui.player.amp.util.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PlaylistFactory.
 */
public class PlaylistFactory {

    private static PlaylistFactory _instance = null;

    private Playlist _playlistInstance = null;

    private Config _config = null;

    private static Log log = LogFactory.getLog(PlaylistFactory.class);

    /**
     * Constructor.
     */
    private PlaylistFactory() {
        _config = Config.getInstance();
    }

    /**
     * Returns instance of PlaylistFactory.
     */
    public static synchronized PlaylistFactory getInstance() {
        if (_instance == null) {
            _instance = new PlaylistFactory();
        }
        return _instance;
    }

    /**
     * Returns Playlist instantied from full qualified class name.
     */
    public Playlist getPlaylist() {
        if (_playlistInstance == null) {
            String classname = _config.getPlaylistClassName();
            boolean interfaceFound = false;
            try {
                Class aClass = Class.forName(classname);
                Class superClass = aClass;
                while (superClass != null) {
                    Class[] interfaces = superClass.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++) {
                        if ((interfaces[i].getName()).equals("javazoom.jlgui.player.amp.playlist.Playlist")) {
                            interfaceFound = true;
                            break;
                        }
                    }
                    if (interfaceFound == true) break;
                    superClass = superClass.getSuperclass();
                }
                if (interfaceFound == false) {
                    log.error("Error : Playlist implementation not found in " + classname + " hierarchy");
                } else {
                    Class[] argsClass = new Class[] {};
                    Constructor c = aClass.getConstructor(argsClass);
                    _playlistInstance = (Playlist) (c.newInstance(null));
                    log.info(classname + " loaded");
                }
            } catch (Exception e) {
                log.error("Error : " + classname + " : " + e.getMessage());
            }
        }
        return _playlistInstance;
    }
}
