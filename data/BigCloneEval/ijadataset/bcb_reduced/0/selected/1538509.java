package org.webstrips.core;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.coffeeshop.io.Files;
import org.coffeeshop.log.Logger;
import org.coffeeshop.settings.Settings;
import org.coffeeshop.settings.SettingsNotFoundException;
import org.webstrips.LoadingException;
import org.webstrips.WebStrips;
import org.webstrips.core.UserInterface.StatusMessageType;
import org.webstrips.core.bundle.ComicBundle;
import org.webstrips.core.comic.Archive;
import org.webstrips.core.comic.ArchiveEntry;
import org.webstrips.core.comic.Comic;
import org.webstrips.core.comic.ComicCapabilities;
import org.webstrips.core.comic.ComicDescription;
import org.webstrips.core.comic.ComicException;
import org.webstrips.core.comic.ComicStrip;
import org.webstrips.core.comic.Links;
import org.webstrips.core.comic.Navigation;
import org.webstrips.core.comic.ComicDescription.EngineType;
import org.webstrips.core.comic.javascript.JavaScriptComic;
import org.webstrips.core.data.Cache;
import org.webstrips.core.data.ContentProvider;
import org.webstrips.core.data.DataCache;
import org.webstrips.core.data.ObjectCache;
import org.webstrips.gui.StripImage;
import org.webstrips.navigator.SimplePreloader;

/**
 * 
 * @author luka
 * @since WebStrips 0.3.1
 */
public class ComicDriver implements ComicInformationProvider {

    public static enum State {

        IDLE, UPDATING, OFFLINE, DELETED
    }

    private static enum NavigationOperation {

        NEXT, PREVIOUS, ANCHOR
    }

    ;

    /**
	 * Size of chache buffer
	 */
    public static final int CACHE_SIZE = 10;

    private static final String SETTINGS_FILE = "settings.ini";

    private static Cache<byte[]> imageCache = null;

    private static Cache<byte[]> getImageCache() {
        if (imageCache != null) return imageCache;
        try {
            imageCache = new DataCache(Long.parseLong(System.getProperty("org.webstrips.memory")), Long.parseLong(System.getProperty("org.webstrips.cache")));
        } catch (IOException e) {
            WebStrips.getApplicationLogger().report(Logger.WARNING, "Unable to create disk cache because of: ", e);
            WebStrips.getApplicationLogger().report(Logger.WARNING, "Caching performance will be limited.");
            imageCache = new ObjectCache<byte[]>(10);
        }
        return imageCache;
    }

    private Comic comicEngine;

    private Settings comicSettings;

    private ComicDescription description;

    private ComicArchive archive;

    private ContentProvider contentProvider;

    private ComicStrip firstStrip, newestStrip;

    private Preloader preloader;

    private State state = State.IDLE;

    private Vector<ComicDriverListener> listeners = new Vector<ComicDriverListener>();

    private class NavigationAsynchronousOperation extends ComicDriverAsynchronousOperation {

        private ComicStrip previous, current, next;

        private NavigationOperation op;

        private ComicStripDescriptor origin;

        private URL image = null;

        private String title = null, link = null;

        public NavigationAsynchronousOperation(ComicStripDescriptor origin, NavigationOperation o, UserInterface ui) {
            super(getSelf(), ui, new ObjectCache<byte[]>(3));
            this.op = o;
            this.origin = origin;
            beginTransaction();
        }

        private synchronized void beginTransaction() {
            previous = (origin == null) ? null : origin.previous();
            current = (origin == null) ? null : origin.current();
            next = (origin == null) ? null : origin.next();
        }

        @Override
        protected void performOperation() throws Exception {
            if (state == State.DELETED) return;
            switch(op) {
                case PREVIOUS:
                    {
                        if (previous == null) return;
                        if (!queryCache(previous)) {
                            next = current;
                            current = previous;
                            previous = getPrevious(current);
                        }
                        break;
                    }
                case NEXT:
                    {
                        if (next == null && current.equals(getNewest())) return;
                        if (!queryCache(next)) {
                            previous = current;
                            current = next;
                            next = getNext(current);
                        }
                        break;
                    }
                case ANCHOR:
                    {
                        if (!queryCache(current)) {
                            previous = getPrevious(current);
                            next = getNext(current);
                        }
                        break;
                    }
            }
            if (next == null && !current.equals(getNewest())) next = getNext(current);
            String url = comicEngine.stripImageUrl(current);
            if (image == null) {
                try {
                    image = new URL(url);
                } catch (MalformedURLException e) {
                    WebStrips.getApplicationLogger().report(Logger.ERROR, "Malformed URL (%s): %s", getComicName(), url);
                    return;
                }
            }
            if (title == null) title = ArchiveEntry.class.isInstance(current) ? ((ArchiveEntry) current).getTitle() : comicEngine.stripTitle(current);
            ComicCapabilities cc = comicEngine.getCapabilities();
            if (cc.hasCapability(Links.LINKS) && link == null) link = ((Links) comicEngine).link(current);
            ComicStripDescriptor d = new ComicStripDescriptor(current, previous, next, title, (link != null) ? new URL(link) : null, image);
            stripsCache.insert(current.getId(), d);
            ImageAsynchronousOperation imageFetch = new ImageAsynchronousOperation(getComicDriver(), getUserInterface(), d);
            imageFetch.addListener(getUserInterface());
            imageFetch.perform();
        }

        private boolean queryCache(ComicStrip s) {
            ComicStripDescriptor c = stripsCache.query(s.getId());
            if (c != null) {
                current = c.current();
                next = c.next();
                previous = c.previous();
                image = c.getImageLink();
                if (c.getLink() != null) link = c.getLink().toString();
                title = c.getTitle();
                stripsCache.remove(s.getId());
                return true;
            }
            return false;
        }
    }

    ;

    private class ImageAsynchronousOperation extends ComicDriverAsynchronousOperation {

        ComicStripDescriptor origin;

        public ImageAsynchronousOperation(ComicDriver c, UserInterface ui, ComicStripDescriptor origin) {
            super(c, ui, getImageCache());
            this.origin = origin;
        }

        @Override
        protected void performOperation() throws Exception {
            if (state == State.DELETED) return;
            StripImage stripImage = preloader.getPreloaded(origin.current());
            if (stripImage == null) {
                Image image = contentProvider.retriveImage(origin.getImageLink().toString());
                if (image == null) {
                    if (getUserInterface() != null) getUserInterface().setStatusMessage(null, StatusMessageType.ERROR, "Unable to load image for comic " + getComicName());
                    return;
                }
                WebStrips.getApplicationLogger().report(WebStrips.TRANSFER, "Image query complete");
                stripImage = new StripImage(origin, image);
            } else {
                WebStrips.getApplicationLogger().report(WebStrips.PRELOADER, "Strip preloaded " + stripImage);
            }
            if (getUserInterface() != null) getUserInterface().displayStripImage(getComicDriver(), stripImage);
        }
    }

    private class UpdateAsynchronousOperation extends ComicDriverAsynchronousOperation {

        public UpdateAsynchronousOperation(ComicDriver c, UserInterface ui) {
            super(c, ui, new ObjectCache<byte[]>(5));
        }

        @Override
        protected void performOperation() throws Exception {
            if (state != State.IDLE) return;
            changeState(State.UPDATING);
            try {
                ComicStrip newest = newest();
                if (firstStrip == null) firstStrip = first();
                if (newest != newestStrip || !newest.equals(newestStrip)) {
                    newestStrip = newest;
                    ComicCapabilities cc = comicEngine.getCapabilities();
                    if (cc.hasCapability(Archive.ARCHIVE)) {
                        ComicArchive archive = getArchive();
                        if (archive != null) {
                            ArchiveEntry last = archive.getLast();
                            ArchiveEntry[] a = ((Archive) comicEngine).archive(last);
                            archive.append(a);
                        }
                    }
                }
            } catch (Exception e) {
                changeState(State.IDLE);
                if (getUserInterface() != null) getUserInterface().update(getSelf());
                throw e;
            }
            changeState(State.IDLE);
            if (getUserInterface() != null) getUserInterface().update(getSelf());
        }
    }

    private NavigationAsynchronousOperation retrieveComic = null;

    private UpdateAsynchronousOperation retrieveArchive = null;

    private String comicPath;

    private ObjectCache<ComicStripDescriptor> stripsCache = new ObjectCache<ComicStripDescriptor>(40);

    public ComicDriver(String comicPath) throws ComicException {
        File descriptionFile = new File(comicPath, ComicBundle.DESCRIPTION_NAME);
        this.comicPath = comicPath;
        if (!descriptionFile.exists()) throw new ComicException("Description not found in " + comicPath);
        description = new ComicDescription(descriptionFile);
        contentProvider = new ContentProvider(description.getShortName());
        switch(description.engineType()) {
            case NATIVE:
                {
                    try {
                        File path = new File(comicPath);
                        comicEngine = loadNative(path.toURI().toURL(), description.getShortName(), contentProvider);
                    } catch (MalformedURLException e) {
                        throw new ComicException(e, description);
                    }
                    break;
                }
            case JAVASCRIPT:
                {
                    File scriptFile = new File(comicPath, "comic.js");
                    try {
                        comicEngine = new JavaScriptComic(scriptFile, contentProvider, description);
                    } catch (IOException e) {
                        throw new ComicException(e, description);
                    } catch (LoadingException e) {
                        throw new ComicException(e, description);
                    }
                    break;
                }
        }
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (cc == null) {
            throw new ComicException("Load error: comic engine " + description.getShortName() + " does not specify capabilities");
        }
        if (!cc.hasCapability(Navigation.NAVIGATION_FIRST) && !cc.hasCapability(Navigation.NAVIGATION_NEWEST)) {
            throw new ComicException("Load error: comic engine " + description.getShortName() + " has no anchor capability");
        }
        comicSettings = WebStrips.getApplication().getSettingsManager().getSettings(description.getShortName() + File.separator + SETTINGS_FILE);
        preloader = new SimplePreloader(this, 3);
    }

    /**
	 * Loads a comic engine specified by its class name. It creates an object
	 * instance with the provided ContentProvider
	 * 
	 * @param path path where the class file can be found
	 * @param name
	 *            Class name of the comic engine
	 * @param cp
	 *            ContentProvider object that this comic engine can use.
	 * @return newly loaded comic engine
	 * @throws ComicException
	 *             if the method was unable to load the specified engine. See
	 *             the message of the error for the details.
	 */
    private Comic loadNative(URL path, String name, ContentProvider cp) throws ComicException {
        URLClassLoader loader = new URLClassLoader(new URL[] { path });
        try {
            Class<?> cClass = loader.loadClass(name);
            if (!Comic.class.isAssignableFrom(cClass)) throw new ComicException("Load error: Bad class file");
            Constructor cons = cClass.getConstructor(new Class[] { ContentProvider.class });
            if (cons == null) throw new ComicException("Unable to find correct constructor");
            return (Comic) cons.newInstance(new Object[] { cp });
        } catch (NoSuchMethodException e) {
            throw new ComicException("Unable to find correct constructor");
        } catch (InstantiationException e) {
            throw new ComicException("Unable to load comic " + name + " (init exception)");
        } catch (InvocationTargetException e) {
            throw new ComicException("Unable to invocate constructor " + name + " " + e.getCause());
        } catch (IllegalAccessException e) {
            throw new ComicException("Unable to access comic " + name);
        } catch (ClassNotFoundException e) {
            throw new ComicException("Unable to locate comic " + name);
        } catch (NoClassDefFoundError e) {
            throw new ComicException("Unable to load comic " + name + " (no class: " + e.getMessage() + ")");
        }
    }

    public void setAnchor(StripImage s) {
        comicSettings.setString("strip.current", s.current().getId());
        preloader.setPreloadAnchor(s);
    }

    public void displayAnchor(UserInterface ui) {
        if (state == State.DELETED) return;
        String id = null;
        try {
            id = comicSettings.getString("strip.current");
        } catch (SettingsNotFoundException e) {
        }
        if (id != null) {
            ComicStrip anchorStrip = new ComicStrip(comicEngine);
            anchorStrip.setId(id);
            displayStrip(anchorStrip, ui);
        } else {
            if (newestStrip != null) {
                displayStrip(newestStrip, ui);
            } else if (firstStrip != null) {
                displayStrip(firstStrip, ui);
            }
        }
    }

    public void displayNewest(UserInterface ui) {
        if (state == State.DELETED) return;
        if (newestStrip != null) displayStrip(newestStrip, ui);
    }

    public void displayFirst(UserInterface ui) {
        if (state == State.DELETED) return;
        if (firstStrip != null) displayStrip(firstStrip, ui);
    }

    public void displayNext(UserInterface ui, ComicStripDescriptor origin) {
        if (state == State.DELETED) return;
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (cc.hasCapability(Navigation.NAVIGATION_NEXT)) {
            retrieveComic = new NavigationAsynchronousOperation(origin, NavigationOperation.NEXT, ui);
            retrieveComic.addListener(ui);
            retrieveComic.perform();
        }
    }

    public void displayPrevious(UserInterface ui, ComicStripDescriptor origin) {
        if (state == State.DELETED) return;
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (cc.hasCapability(Navigation.NAVIGATION_PREVIOUS)) {
            retrieveComic = new NavigationAsynchronousOperation(origin, NavigationOperation.PREVIOUS, ui);
            retrieveComic.addListener(ui);
            retrieveComic.perform();
        }
    }

    public void displayStrip(ComicStrip s, UserInterface ui) {
        if (state == State.DELETED) return;
        ComicStripDescriptor si = new ComicStripDescriptor(s, null, null, null, null, null);
        retrieveComic = new NavigationAsynchronousOperation(si, NavigationOperation.ANCHOR, ui);
        retrieveComic.addListener(ui);
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (cc.hasCapability(Archive.ARCHIVE) && archive != null && archive.isEmpty() && retrieveArchive != null && retrieveArchive.isBeingPerformed()) retrieveArchive.addListener(new AsynchronousOperationAdapter() {

            public void operationEnded(AsynchronousOperation o) {
                retrieveComic.perform();
            }
        }); else retrieveComic.perform();
    }

    public String toString() {
        return description.comicName();
    }

    /**
	 * @see org.webstrips.core.ComicInformationProvider#getComicName()
	 */
    public String getComicName() {
        return description.comicName();
    }

    /**
	 * @see org.webstrips.core.ComicInformationProvider#getComicAuthor()
	 */
    public String getComicAuthor() {
        return description.comicAuthor();
    }

    /**
	 * @see org.webstrips.core.ComicInformationProvider#getComicDescription()
	 */
    public String getComicDescription() {
        return description.comicDescription();
    }

    /**
	 * @see org.webstrips.core.ComicInformationProvider#getComicHomapage()
	 */
    public String getComicHomapage() {
        return description.comicHomepage();
    }

    public ComicArchive getArchive() {
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (!cc.hasCapability(Archive.ARCHIVE)) return null;
        if (archive == null) {
            archive = ComicArchive.loadArchive(this, comicEngine);
            fireUnreadStripsNumberChangedEvent(archive.getUnread());
            if (archive != null) archive.addListDataListener(new ListDataListener() {

                public void contentsChanged(ListDataEvent e) {
                    fireUnreadStripsNumberChangedEvent(archive.getUnread());
                }

                public void intervalAdded(ListDataEvent e) {
                    fireUnreadStripsNumberChangedEvent(archive.getUnread());
                }

                public void intervalRemoved(ListDataEvent e) {
                    fireUnreadStripsNumberChangedEvent(archive.getUnread());
                }
            });
        }
        return archive;
    }

    public int getUnreadStripsCount() {
        ComicArchive a = getArchive();
        if (a == null) return -1;
        return a.getUnread();
    }

    public void update(UserInterface ui) {
        retrieveArchive = new UpdateAsynchronousOperation(this, ui);
        retrieveArchive.perform();
    }

    public boolean isUpdating() {
        return (retrieveArchive != null && retrieveArchive.isBeingPerformed());
    }

    public void addComicDriverListener(ComicDriverListener l) {
        if (l == null) return;
        listeners.add(l);
    }

    public void removeComicDriverListener(ComicDriverListener l) {
        if (l == null) return;
        listeners.remove(l);
    }

    private void fireUnreadStripsNumberChangedEvent(int unread) {
        for (int i = 0; i < listeners.size(); i++) listeners.get(i).unreadStripsNumberChanged(this, unread);
    }

    public ComicDescription getDescription() {
        return description;
    }

    private ComicStrip first() {
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (cc.hasCapability(Navigation.NAVIGATION_FIRST)) return ((Navigation) comicEngine).first();
        return null;
    }

    private ComicStrip newest() {
        ComicCapabilities cc = comicEngine.getCapabilities();
        if (cc.hasCapability(Navigation.NAVIGATION_NEWEST)) return ((Navigation) comicEngine).newest();
        return null;
    }

    public ComicStrip getFirst() {
        return firstStrip;
    }

    public ComicStrip getNewest() {
        return newestStrip;
    }

    private ComicStrip getPrevious(ComicStrip s) {
        ComicCapabilities cc = comicEngine.getCapabilities();
        StripImage i = preloader.getPreloaded(s);
        if (i != null) return i.previous();
        if (cc.hasCapability(Archive.ARCHIVE) && archive != null) return archive.getPrevious(s);
        if (cc.hasCapability(Navigation.NAVIGATION_PREVIOUS)) return ((Navigation) comicEngine).previous(s);
        return null;
    }

    private ComicStrip getNext(ComicStrip s) {
        ComicCapabilities cc = comicEngine.getCapabilities();
        StripImage i = preloader.getPreloaded(s);
        if (i != null) return i.next();
        if (cc.hasCapability(Archive.ARCHIVE) && archive != null) return archive.getNext(s);
        if (cc.hasCapability(Navigation.NAVIGATION_PREVIOUS)) return ((Navigation) comicEngine).next(s);
        return null;
    }

    private ComicDriver getSelf() {
        return this;
    }

    public boolean delete(boolean cleanData, boolean cleanBinary) {
        if (cleanData && cleanBinary) {
            return Files.delete(comicPath);
        }
        if (cleanBinary) {
            File descriptionFile = new File(comicPath, ComicBundle.DESCRIPTION_NAME);
            File engineFile = null;
            switch(description.engineType()) {
                case NATIVE:
                    {
                        engineFile = new File(comicPath, description.getShortName() + ".class");
                        break;
                    }
                case JAVASCRIPT:
                    {
                        engineFile = new File(comicPath, "comic.js");
                        break;
                    }
            }
            if (engineFile == null) return false;
            descriptionFile.delete();
            engineFile.delete();
        }
        if (cleanData) {
        }
        state = State.DELETED;
        return true;
    }

    public String getPath() {
        return comicPath;
    }

    public File getSourceFile() {
        if (description.engineType() == EngineType.NATIVE) return null;
        return new File(comicPath, "comic.js");
    }

    private void changeState(State newState) {
        State oldState = state;
        state = newState;
        for (ComicDriverListener l : listeners) {
            try {
                l.stateChanged(this, newState, oldState);
            } catch (Exception e) {
                WebStrips.getApplicationLogger().report(e);
            }
        }
    }
}
