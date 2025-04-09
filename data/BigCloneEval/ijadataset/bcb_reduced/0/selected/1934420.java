package de.psychomatic.mp3db.gui.utils.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import de.psychomatic.mp3db.core.dblayer.dao.GenericDAO;
import de.psychomatic.mp3db.core.utils.AbstractStatusRunnable;
import de.psychomatic.mp3db.core.utils.events.StatusEvent;
import de.psychomatic.mp3db.core.utils.events.StatusEvent.StatusEventType;
import de.psychomatic.mp3db.gui.modules.WaitDialog;
import de.psychomatic.mp3db.gui.threads.backup.TypeConstants;
import de.psychomatic.mp3db.gui.threads.backup.backup.BackupRunnable;

public class OldDbBackupRunnable extends AbstractStatusRunnable {

    /**
     * Logger for this class
     */
    static final Logger LOG = Logger.getLogger(BackupRunnable.class);

    boolean _break;

    WaitDialog _wd;

    private final Set<String> _usedFilenames;

    private final int _version;

    private final File _file;

    public OldDbBackupRunnable(final int sourceVersion, final File file) {
        _file = file;
        _version = sourceVersion;
        _usedFilenames = new HashSet<String>();
    }

    void exportAlbum(final XMLStreamWriter writer, final Vector a, final Integer id) throws XMLStreamException {
        writer.writeAttribute("id", id.toString());
        writer.writeAttribute("name", String.valueOf(a.get(2)));
        writer.writeAttribute("type", String.valueOf(a.get(1)));
    }

    void exportCd(final XMLStreamWriter writer, final Vector vdCd, final Integer id) throws XMLStreamException {
        writer.writeAttribute("id", Integer.toString(id));
        writer.writeAttribute("name", String.valueOf(vdCd.get(1)));
        writer.writeAttribute("md5", String.valueOf(vdCd.get(2)));
    }

    void exportCoveritem(final XMLStreamWriter writer, final ZipOutputStream out, final String type, final byte[] data, final Integer id) throws XMLStreamException, IOException {
        writer.writeStartElement(TypeConstants.XML_COVERITEM);
        writer.writeAttribute("id", id.toString());
        writer.writeAttribute("type", type);
        final String filename = getRandomFilename();
        out.putNextEntry(new ZipEntry(filename));
        IOUtils.write(data, out);
        out.closeEntry();
        writer.writeAttribute("filename", filename);
        _usedFilenames.add(filename);
        writer.writeEndElement();
    }

    private String getRandomFilename() {
        String filename = RandomStringUtils.random(10, "abcdefghijklmnopqrstuvwxyz0123456789");
        while (_usedFilenames.contains(filename)) {
            filename = RandomStringUtils.random(10, "abcdefghijklmnopqrstuvwxyz0123456789");
        }
        return filename;
    }

    void exportMediafile(final XMLStreamWriter writer, final Vector mf, final Integer id) throws XMLStreamException {
        writer.writeAttribute("id", id.toString());
        writer.writeAttribute("title", String.valueOf(mf.get(1) == null ? "" : mf.get(1)));
        writer.writeAttribute("artist", String.valueOf(mf.get(2) == null ? "" : mf.get(2)));
        writer.writeAttribute("playtime", String.valueOf(mf.get(5)));
        writer.writeAttribute("bitrate", String.valueOf(mf.get(6)));
        writer.writeAttribute("filesize", String.valueOf(mf.get(7)));
        writer.writeAttribute("path", String.valueOf(mf.get(8)));
    }

    @Override
    public void reset() {
    }

    @Override
    public void run() {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Backupthread started");
            }
            final ZipOutputStream zOut = new ZipOutputStream(new FileOutputStream(_file));
            final ByteArrayOutputStream ost = new ByteArrayOutputStream();
            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(ost, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("mp3db");
            writer.writeAttribute("version", "5");
            int itemCount = 0;
            try {
                itemCount += getItemCount("mediafile");
                itemCount += getItemCount("album");
                itemCount += getItemCount("cd");
                itemCount += _version < 3 ? getItemCount("cover") : getItemCount("coveritem");
                fireStatusEvent(new StatusEvent(this, StatusEventType.MAX_VALUE, itemCount));
            } catch (final Exception e) {
                LOG.error("Error getting size", e);
                fireStatusEvent(new StatusEvent(this, StatusEventType.MAX_VALUE, -1));
            }
            int cdCounter = 0;
            int mediafileCounter = 0;
            int albumCounter = 0;
            int coveritemCounter = 0;
            int counter = 0;
            final List data = getCdsOrderById();
            if (data.size() > 0) {
                final Map<Integer, Integer> albums = new HashMap<Integer, Integer>();
                final Iterator it = data.iterator();
                while (it.hasNext() && !_break) {
                    final Vector vdCd = (Vector) it.next();
                    final Integer cdId = Integer.valueOf(cdCounter++);
                    writer.writeStartElement(TypeConstants.XML_CD);
                    exportCd(writer, vdCd, cdId);
                    fireStatusEvent(new StatusEvent(this, StatusEventType.NEW_VALUE, ++counter));
                    final List files = getMediafileByCd(((Number) vdCd.get(0)).intValue());
                    final Iterator mfit = files.iterator();
                    while (mfit.hasNext() && !_break) {
                        final Vector mf = (Vector) mfit.next();
                        final Integer mfId = Integer.valueOf(mediafileCounter++);
                        writer.writeStartElement(TypeConstants.XML_MEDIAFILE);
                        exportMediafile(writer, mf, mfId);
                        fireStatusEvent(new StatusEvent(this, StatusEventType.NEW_VALUE, ++counter));
                        final int albumId = ((Number) mf.get(3)).intValue();
                        final Vector a = getAlbumById(albumId);
                        if (a != null) {
                            Integer inte;
                            if (albums.containsKey(a.get(0))) {
                                inte = albums.get(a.get(0));
                                writeLink(writer, TypeConstants.XML_ALBUM, inte);
                            } else {
                                inte = Integer.valueOf(albumCounter++);
                                writer.writeStartElement(TypeConstants.XML_ALBUM);
                                exportAlbum(writer, a, inte);
                                fireStatusEvent(new StatusEvent(this, StatusEventType.NEW_VALUE, ++counter));
                                albums.put(albumId, inte);
                                if (!_break) {
                                    switch(_version) {
                                        case 1:
                                        case 2:
                                            final int cid = ((Number) a.get(4)).intValue();
                                            if (cid > 0) {
                                                final Vector covers = getCoversById(cid);
                                                for (int i = 1; i < 5; i++) {
                                                    final byte[] coverData = (byte[]) covers.get(i);
                                                    if (coverData != null) {
                                                        final Integer coveritemId = Integer.valueOf(coveritemCounter++);
                                                        String type;
                                                        switch(i) {
                                                            case 1:
                                                                type = "Front";
                                                                break;
                                                            case 2:
                                                                type = "Back";
                                                                break;
                                                            case 3:
                                                                type = "Inlay";
                                                                break;
                                                            case 4:
                                                                type = "Cd";
                                                                break;
                                                            case 5:
                                                                type = "Other";
                                                                break;
                                                            default:
                                                                type = "Unknown";
                                                                break;
                                                        }
                                                        exportCoveritem(writer, zOut, type, coverData, coveritemId);
                                                    }
                                                }
                                            }
                                            fireStatusEvent(new StatusEvent(this, StatusEventType.NEW_VALUE, ++counter));
                                            break;
                                        case 3:
                                        case 4:
                                            final List covers = getCoveritemByAlbum(albumId);
                                            final Iterator coit = covers.iterator();
                                            while (coit.hasNext() && !_break) {
                                                final Integer coveritemId = Integer.valueOf(coveritemCounter++);
                                                final Vector coveritem = (Vector) coit.next();
                                                exportCoveritem(writer, zOut, String.valueOf(coveritem.get(2)), (byte[]) coveritem.get(3), coveritemId);
                                                fireStatusEvent(new StatusEvent(this, StatusEventType.NEW_VALUE, ++counter));
                                            }
                                    }
                                }
                                writer.writeEndElement();
                            }
                        }
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                    writer.flush();
                    it.remove();
                    GenericDAO.getEntityManager().close();
                }
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            ost.flush();
            ost.close();
            if (_break) {
                zOut.close();
                _file.delete();
            } else {
                zOut.putNextEntry(new ZipEntry("mp3.xml"));
                IOUtils.write(ost.toByteArray(), zOut);
                zOut.close();
            }
            fireStatusEvent(new StatusEvent(this, StatusEventType.FINISH));
        } catch (final Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error backup database", e);
            }
            fireStatusEvent(new StatusEvent(this, e, ""));
        }
    }

    @Override
    public void stopProcess() {
        _break = true;
    }

    public void writeLink(final XMLStreamWriter writer, final String type, final Integer id) throws XMLStreamException {
        writer.writeStartElement(TypeConstants.XML_LINK);
        writer.writeAttribute("type", type);
        writer.writeAttribute("targetid", id.toString());
        writer.writeEndElement();
    }

    private List getCdsOrderById() {
        final EntityManager em = GenericDAO.getEntityManager();
        final Query q = em.createNativeQuery("SELECT cdid, cd_name, cd_md5 FROM cd ORDER BY cdid");
        final List result = q.getResultList();
        em.close();
        return result;
    }

    private List getMediafileByCd(final int cdId) {
        final EntityManager em = GenericDAO.getEntityManager();
        Query q;
        switch(_version) {
            case 1:
                q = em.createNativeQuery("SELECT id, title, artist, albumnr, cdnr, laenge, bitrate, groesse, path FROM mediafile WHERE cdnr = ?");
                break;
            case 2:
            case 3:
                q = em.createNativeQuery("SELECT id, title, artist, albumnr, cdnr, playtime, bitrate, size, path FROM mediafile WHERE cdnr = ?");
                break;
            case 4:
                q = em.createNativeQuery("SELECT mfid, title, artist, albumnr, cdnr, playtime, bitrate, filesize, path FROM mediafile WHERE cdnr = ?");
                break;
            default:
                q = null;
                break;
        }
        q.setParameter(1, cdId);
        final List result = q.getResultList();
        em.close();
        return result;
    }

    private Vector getAlbumById(final int aId) {
        final EntityManager em = GenericDAO.getEntityManager();
        Query q;
        switch(_version) {
            case 1:
            case 2:
                q = em.createNativeQuery("SELECT aid, is_sampler, album, cover FROM album WHERE aid = ?");
                break;
            case 3:
                q = em.createNativeQuery("SELECT aid, is_sampler, album FROM album WHERE aid = ?");
                break;
            case 4:
                q = em.createNativeQuery("SELECT aid, album_type, album FROM album WHERE aid = ?");
                break;
            default:
                q = null;
                break;
        }
        q.setParameter(1, aId);
        final List result = q.getResultList();
        em.close();
        return (Vector) (result.size() > 0 ? result.get(0) : null);
    }

    private Vector getCoversById(final int cId) {
        final EntityManager em = GenericDAO.getEntityManager();
        final Query q = em.createNativeQuery("SELECT cid, front, back, inlay, cd, other FROM covers WHERE cid = ?");
        q.setParameter(1, cId);
        final List result = q.getResultList();
        em.close();
        return (Vector) (result.size() > 0 ? result.get(0) : null);
    }

    private List getCoveritemByAlbum(final int aId) {
        final EntityManager em = GenericDAO.getEntityManager();
        final Query q = em.createNativeQuery("SELECT ciid, albumid, citype, cidata FROM coveritem WHERE albumid = ?");
        q.setParameter(1, aId);
        final List result = q.getResultList();
        em.close();
        return result;
    }

    private int getItemCount(final String table) {
        final EntityManager em = GenericDAO.getEntityManager();
        final Query q = em.createNativeQuery("SELECT count(*) FROM " + table);
        final List qres = q.getResultList();
        int result;
        if (qres.size() == 1) {
            final Object o = qres.get(0);
            Number n;
            if (o.getClass().isArray()) {
                n = (Number) ((Object[]) o)[0];
            } else if (o instanceof Collection) {
                n = (Number) ((Collection) o).toArray()[0];
            } else {
                n = (Number) o;
            }
            result = n.intValue();
        } else {
            result = 0;
        }
        em.close();
        return result;
    }
}
