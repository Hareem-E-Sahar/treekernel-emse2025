package org.pixory.pxmodel;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pixory.pxfoundation.PXCacheMap;
import org.pixory.pxfoundation.PXFileUtility;
import org.pixory.pxfoundation.PXInvocation;
import org.pixory.pxfoundation.PXJob;
import org.pixory.pxfoundation.SZNotification;
import org.pixory.pxfoundation.concurrent.PXLockManager;

/**
 * Design note: every time something comes out of the db, it should be checked
 * against the filesystem to ensure that something hasn't changed. This is a
 * sort of "just-in-time" synchronization strategy.
 *  
 */
public class PXAlbumManager extends Object implements SZNotification.Observer {

    /**
	 * this path is relative to the image root
	 */
    private static final String META_DIRECTORY_PATH = ".pixory";

    /**
	 * this path is relative to the META_DIRECTORY
	 */
    private static final String TRASH_DIRECTORY_PATH = "trash";

    /**
	 * this path is relative to the META_DIRECTORY
	 */
    private static final String PROPERTY_SETS_DIRECTORY_PATH = "propertysets";

    private static final int ALBUM_CACHE_SIZE = 15;

    private static final long LOCK_TIMEOUT = 60000;

    private static final Log LOG = LogFactory.getLog(PXAlbumManager.class);

    private static PXAlbumManager _instance;

    private File _albumRoot;

    private File _metaDirectory;

    private File _trashDirectory;

    private File _propertySetsDirectory;

    /**
	 * values are propertySets (PXAlbumContent) and keys are ids (String)
	 */
    private Map _propertySetsMap;

    private PXCacheMap _albumCache = new PXCacheMap(ALBUM_CACHE_SIZE);

    private PXLockManager _albumLocks = new PXLockManager();

    private PXAlbumManager() {
        SZNotification.Center.defaultCenter().addObserver(this, PXAlbumContent.NOTIFICATION_NAME_DID_SAVE, null);
    }

    public static PXAlbumManager getInstance() {
        if (_instance == null) {
            _instance = new PXAlbumManager();
        }
        return _instance;
    }

    public File getAlbumRoot() {
        return _albumRoot;
    }

    public void setAlbumRoot(File albumRoot_) {
        _albumRoot = albumRoot_;
        this.clearCachedValues();
    }

    public File getTrashDirectory() {
        if (_trashDirectory == null) {
            File metaDirectory = this.getMetaDirectory();
            if (metaDirectory != null) {
                _trashDirectory = new File(metaDirectory, TRASH_DIRECTORY_PATH);
                if (!_trashDirectory.exists()) {
                    _trashDirectory.mkdirs();
                }
            }
        }
        return _trashDirectory;
    }

    public void rebuildAlbumStore(PXJob.StatusCheck statusCheck_) throws InvocationTargetException, InterruptedException {
        Date startDate = new Date();
        PXInvocation albumInvocation = new PXInvocation("notifyUpdate", null);
        File albumRoot = this.getAlbumRoot();
        this.invokeOnAlbumsAtPath(statusCheck_, albumInvocation, albumRoot);
        PXAlbumFace.removeFacesUpdatedBefore(startDate);
    }

    /**
	 * recursively apply invocation to all albums under path; the invocation is
	 * applied to a *clone* of the PXAlbumContent, so that we can unlock the
	 * PXAlbumContent and not tie it up in the case of long-running invocations
	 * this method should be thread-safe and well behaved with respect to
	 * Album-based operations in other parts of the model N.B. there is a little
	 * funniness here; this method modifies the invocation in order to also add
	 * the statusCheck as the first arg
	 */
    public void invokeOnAlbumsAtPath(PXJob.StatusCheck statusCheck_, PXInvocation invocation_, File directory_) throws InvocationTargetException, InterruptedException {
        LOG.debug("invoking for path: " + directory_.getAbsoluteFile());
        if ((invocation_ != null) && (directory_ != null) && (directory_.isDirectory())) {
            FileFilter albumFileFilter = PXFileFilters.getPXAlbumFileFilter();
            FileFilter directoryFileFilter = PXFileFilters.getPXDirectoryFileFilter();
            if (albumFileFilter.accept(directory_)) {
                ArrayList newArgs = new ArrayList();
                Object[] invocationArgs = invocation_.getArgs();
                newArgs.add(statusCheck_);
                if (invocationArgs != null) {
                    CollectionUtils.addAll(newArgs, invocation_.getArgs());
                }
                PXInvocation invocation = new PXInvocation(invocation_.getMethodName(), newArgs.toArray());
                if (statusCheck_ != null) {
                    statusCheck_.check();
                }
                PXAlbumContent albumContent = this.lockAlbumContentAtPath(directory_);
                if (albumContent != null) {
                    PXAlbumContent albumCopy = null;
                    try {
                        albumCopy = (PXAlbumContent) new PXAlbumContent(albumContent);
                    } catch (Exception anException) {
                        LOG.warn(null, anException);
                    } finally {
                        this.releaseAlbumContent(albumContent);
                    }
                    if (albumCopy != null) {
                        try {
                            invocation.invoke(albumCopy);
                        } catch (NoSuchMethodException e) {
                            LOG.warn(null, e);
                        }
                    }
                } else {
                    LOG.warn("could not lock album at path: " + directory_);
                }
            } else if (directoryFileFilter.accept(directory_)) {
                FileFilter nodeFilter = PXFileFilters.getPXNodeFileFilter();
                File[] nodes = directory_.listFiles(nodeFilter);
                if (nodes != null) {
                    for (int i = 0; i < nodes.length; i++) {
                        this.invokeOnAlbumsAtPath(statusCheck_, invocation_, nodes[i]);
                    }
                }
            } else {
            }
        }
    }

    private PXAlbum getAlbumForId(String albumId_) {
        PXAlbum getAlbumForId = null;
        if (albumId_ != null) {
            getAlbumForId = this.getStoredAlbumForId(albumId_);
            if (getAlbumForId == null) {
                LOG.debug("no stored album for id: " + albumId_);
                getAlbumForId = this.findAlbumForId(albumId_);
            } else {
                getAlbumForId = this.checkStoredAlbum((PXAlbumFace) getAlbumForId);
            }
        }
        return getAlbumForId;
    }

    /**
	 * this verifies that album information from the store (db) accurately
	 * reflects what's on the filesystem, including that it's under the currently
	 * active album root
	 */
    private PXAlbum checkStoredAlbum(PXAlbumFace album_) {
        PXAlbum checkStoredAlbum = null;
        LOG.debug("album: " + album_);
        if (album_ != null) {
            File albumPath = album_.getAlbumPath();
            if (albumPath != null) {
                File metaFile = PXAlbumContent.albumMetaFileForAlbumId(albumPath, album_.getId());
                if ((metaFile != null) && (metaFile.isFile())) {
                    LOG.debug("album exists at path: " + albumPath);
                    checkStoredAlbum = album_;
                } else {
                    PXAlbumContent album = this.findAlbumForId(album_.getId());
                    if (album != null) {
                        checkStoredAlbum = PXAlbumFace.updateFaceForAlbum(album);
                        LOG.info("album moved; updated to: " + album);
                    } else {
                        PXAlbumFace.removeFaceFromStore(album_);
                        LOG.info("couldn't find album on fs; deleted: " + album_);
                    }
                }
                if (checkStoredAlbum != null) {
                    File checkPath = checkStoredAlbum.getAlbumPath();
                    File albumRoot = this.getAlbumRoot();
                    if (!PXFileUtility.isParent(checkPath, albumRoot)) {
                        String message = "album at path: " + checkPath + " is no longer under the album root";
                        LOG.debug(message);
                        checkStoredAlbum = null;
                    }
                }
            }
        }
        return checkStoredAlbum;
    }

    private PXAlbumContent findAlbumForId(String albumId_) {
        PXAlbumContent findAlbumForId = null;
        if (albumId_ != null) {
            String metafileName = PXAlbumContent.getMetafileName(albumId_);
            File albumRoot = this.getAlbumRoot();
            File metafilePath = PXFileUtility.findFileNamed(albumRoot, metafileName);
            if (metafilePath != null) {
                File albumPath = PXAlbumContent.albumPathForMetafile(metafilePath);
                if (albumPath != null) {
                    try {
                        findAlbumForId = PXAlbumContent.albumAtPath(albumPath);
                    } catch (Exception anException) {
                        LOG.warn(null, anException);
                    }
                }
            }
        }
        return findAlbumForId;
    }

    public PXAlbumFace getStoredAlbumForId(String albumId_) {
        PXAlbumFace getStoredAlbum = null;
        if (albumId_ != null) {
            try {
                Session session = PXObjectStore.getInstance().getThreadSession();
                if (session != null) {
                    getStoredAlbum = (PXAlbumFace) session.load(PXAlbumFace.class, albumId_);
                } else {
                    LOG.warn("couldn't get Session");
                }
            } catch (ObjectNotFoundException anException) {
                LOG.debug(anException.getMessage());
            } catch (Exception anException) {
                LOG.warn(null, anException);
            }
        }
        return getStoredAlbum;
    }

    public List getPublicAlbums() {
        List getPublicAlbums = null;
        List uncheckedAlbums = null;
        try {
            Session session = PXObjectStore.getInstance().getThreadSession();
            uncheckedAlbums = session.createQuery("from org.pixory.pxmodel.PXAlbumFace album where album.shareMethod = :shareMethod").setEntity("shareMethod", PXShareMethod.PUBLIC).list();
        } catch (Exception anException) {
            LOG.warn(null, anException);
        }
        if ((uncheckedAlbums != null) && (uncheckedAlbums.size() > 0)) {
            getPublicAlbums = new ArrayList(uncheckedAlbums.size());
            Iterator albumIterator = uncheckedAlbums.iterator();
            while (albumIterator.hasNext()) {
                PXAlbum album = this.checkStoredAlbum((PXAlbumFace) albumIterator.next());
                if (album != null) {
                    getPublicAlbums.add(album);
                }
            }
        }
        return getPublicAlbums;
    }

    public List getAllAlbums() {
        List getAllAlbums = null;
        List uncheckedAlbums = null;
        try {
            Session aSession = PXObjectStore.getInstance().getThreadSession();
            uncheckedAlbums = aSession.createQuery("from org.pixory.pxmodel.PXAlbumFace album").list();
        } catch (Exception anException) {
            LOG.warn(null, anException);
        }
        if ((uncheckedAlbums != null) && (uncheckedAlbums.size() > 0)) {
            getAllAlbums = new ArrayList(uncheckedAlbums.size());
            Iterator albumIterator = uncheckedAlbums.iterator();
            while (albumIterator.hasNext()) {
                PXAlbum album = this.checkStoredAlbum((PXAlbumFace) albumIterator.next());
                if (album != null) {
                    getAllAlbums.add(album);
                }
            }
        }
        return getAllAlbums;
    }

    public void notify(SZNotification notification_) {
        LOG.debug("received notification: " + notification_);
        if (notification_ != null) {
            if (notification_.name().equals(PXAlbumContent.NOTIFICATION_NAME_DID_SAVE)) {
                PXAlbumContent albumContent = (PXAlbumContent) notification_.object();
                PXAlbumFace.updateFaceForAlbum(albumContent);
            }
        }
    }

    public PXAlbumContent lockAlbumContentForId(String albumId_) {
        PXAlbumContent lockAlbumContent = null;
        if (albumId_ != null) {
            PXAlbum album = this.getAlbumForId(albumId_);
            if (album != null) {
                lockAlbumContent = this.lockAlbumContentAtPath(album.getAlbumPath());
            } else {
                LOG.warn("could not find album for id: " + albumId_);
            }
        }
        return lockAlbumContent;
    }

    public PXAlbumContent lockAlbumContentAtPath(File albumPath_) {
        PXAlbumContent lockAlbumContent = null;
        if (albumPath_ != null) {
            try {
                if (_albumLocks.lock(albumPath_, LOCK_TIMEOUT)) {
                    synchronized (_albumCache) {
                        LOG.debug("locked album at path: " + albumPath_);
                        lockAlbumContent = (PXAlbumContent) _albumCache.get(albumPath_);
                        if (lockAlbumContent == null) {
                            try {
                                lockAlbumContent = PXAlbumContent.albumAtPath(albumPath_);
                            } catch (Exception anException) {
                                LOG.warn(null, anException);
                            }
                            if (lockAlbumContent != null) {
                                LOG.debug("created albumContent for path: " + albumPath_);
                                _albumCache.put(albumPath_, lockAlbumContent);
                            } else {
                                LOG.warn("could not create Album for path: " + albumPath_);
                            }
                        } else {
                            LOG.debug("cached album found for path: " + albumPath_);
                            lockAlbumContent.ensureSynchronized();
                        }
                    }
                    if (lockAlbumContent == null) {
                        _albumLocks.unlock(albumPath_);
                    }
                } else {
                    LOG.warn("could not acquire lock on albumPath: " + albumPath_);
                }
            } catch (Exception anException) {
                LOG.warn(null, anException);
            }
        }
        return lockAlbumContent;
    }

    public boolean releaseAlbumContent(PXAlbumContent albumContent_) {
        boolean releaseAlbum = false;
        if (albumContent_ != null) {
            try {
                File albumPath = albumContent_.getAlbumPath();
                releaseAlbum = _albumLocks.unlock(albumPath);
                if (releaseAlbum) {
                    LOG.debug("unlocked album at path: " + albumContent_.getAlbumPath());
                } else {
                    LOG.warn("could not unlock album at path: " + albumContent_.getAlbumPath());
                }
            } catch (Exception anException) {
                LOG.warn(null, anException);
            }
        }
        return releaseAlbum;
    }

    private File getMetaDirectory() {
        if (_metaDirectory == null) {
            File albumRoot = this.getAlbumRoot();
            if (albumRoot != null) {
                _metaDirectory = new File(albumRoot, META_DIRECTORY_PATH);
                if (!_metaDirectory.exists()) {
                    _metaDirectory.mkdirs();
                }
            }
        }
        return _metaDirectory;
    }

    public File getPropertySetsDirectory() {
        if (_propertySetsDirectory == null) {
            File metaDirectory = this.getMetaDirectory();
            if (metaDirectory != null) {
                _propertySetsDirectory = new File(metaDirectory, PROPERTY_SETS_DIRECTORY_PATH);
                if (!_propertySetsDirectory.exists()) {
                    _propertySetsDirectory.mkdirs();
                }
            }
        }
        return _propertySetsDirectory;
    }

    /**
	 * @return List of PXAlbumContent that are *not* locked
	 */
    public List getAllPropertySets() {
        return null;
    }

    public PXAlbumContent lockPropertySetForId(String id_) {
        return null;
    }

    public PXAlbumContent lockPropertySetAtPath(File path_) {
        return null;
    }

    public PXAlbumContent lockDefaultPropertySet() {
        return null;
    }

    public void releasePropertySet(PXAlbumContent propertySet_) {
    }

    public PXAlbumContent createPropertySetNamed(String name_) {
        return null;
    }

    public void removePropertySetNamed(String name_) {
    }

    private Map getPropertySetsMap() {
        if (_propertySetsMap == null) {
        }
        return _propertySetsMap;
    }

    private void clearCachedValues() {
        _metaDirectory = null;
        _trashDirectory = null;
        _propertySetsDirectory = null;
        _propertySetsMap = null;
        _albumCache.clear();
    }
}
