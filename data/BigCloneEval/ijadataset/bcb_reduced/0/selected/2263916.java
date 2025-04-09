package org.fudaa.fudaa.commun.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.memoire.fu.FuLog;
import org.fudaa.ctulu.CtuluArkSaver;
import org.fudaa.ctulu.CtuluLibFile;
import org.fudaa.ctulu.CtuluLibString;

/**
 * Une classe pour sauvegarder des donn�es dans une archive, suivant un format d�fini par
 * F.Deniger. Chaque type de donn�es est stock� dans une entry nomm�e.
 * 
 * @author fred deniger
 * @version $Id: FudaaSaveZipWriter.java,v 1.4.6.1 2008-01-22 11:14:02 bmarchan Exp $
 */
public class FudaaSaveZipWriter implements CtuluArkSaver {

    public class DirZipEntry {

        int idx_;

        final String name_;

        final int nbDigits_;

        /**
     * @param _name le nom du dossier a inserer dans le zip
     * @param _nbEntry le nombre d'entrees pr�vues y compris les dossiers
     */
        public DirZipEntry(final String _name, final int _nbEntry) {
            super();
            name_ = _name;
            nbDigits_ = Integer.toString(_nbEntry).length() + 1;
        }

        /**
     * @param _entry l'entree a ajouter
     * @return name_+"/"+_entry
     */
        public String getEntryName(final String _entry) {
            return name_ + '/' + _entry;
        }

        /**
     * @return un nouvelle identifiant
     */
        public String getNextId() {
            return CtuluLibString.adjustSizeBefore(nbDigits_, Integer.toString(++idx_), '0') + '-';
        }
    }

    ObjectContainer cont_;

    File dbFile_;

    final File f_;

    public static String getIdFromEntry(final String _entry) {
        if (_entry == null) {
            return null;
        }
        final int idx = _entry.indexOf('-');
        if (idx >= 0) {
            return _entry.substring(0, idx + 1);
        }
        return null;
    }

    Map nameDirZip_;

    final ZipOutputStream zipOut_;

    public FudaaSaveZipWriter(final File _f) throws IOException {
        super();
        f_ = _f;
        zipOut_ = new ZipOutputStream(new FileOutputStream(f_));
        zipOut_.setLevel(8);
    }

    public File getDestDir() {
        return f_.getParentFile();
    }

    public void close() throws IOException {
        if (zipOut_ != null) {
            if (dbFile_ != null && cont_ != null) {
                cont_.close();
                final ZipEntry entry = new ZipEntry(getDbEntryName());
                entry.setComment("Donnees concernant le style et les calculs interm�diaires");
                zipOut_.putNextEntry(entry);
                CtuluLibFile.copyStream(new FileInputStream(dbFile_), zipOut_, true, false);
                zipOut_.closeEntry();
                dbFile_.delete();
            }
            zipOut_.close();
        }
    }

    public void safeClose() {
        try {
            close();
        } catch (final IOException _evt) {
            FuLog.error(_evt);
        }
    }

    public boolean isDirCreated(final String _name) {
        return (nameDirZip_ != null) && nameDirZip_.containsKey(_name);
    }

    public void createDir(final String _dirName, final int _nbEntry) {
        createDirZipEntry(_dirName, _nbEntry);
    }

    public String getNextIdForDir(final String _dir) {
        final DirZipEntry entry = getDirZipEntry(_dir);
        return entry == null ? null : entry.getNextId();
    }

    public void startEntry(final String _entryName) throws IOException {
        zipOut_.putNextEntry(new ZipEntry(_entryName));
    }

    public DirZipEntry createDirZipEntry(final String _name, final int _nbEntry) {
        if (nameDirZip_ == null) {
            nameDirZip_ = new HashMap();
        } else if (nameDirZip_.containsKey(_name)) {
            throw new IllegalAccessError("ebtry is already created");
        }
        final DirZipEntry res = new DirZipEntry(_name, _nbEntry);
        nameDirZip_.put(_name, res);
        return res;
    }

    public boolean isDbCreated() {
        return cont_ != null;
    }

    public ObjectContainer getDb() throws IOException {
        if (cont_ == null) {
            dbFile_ = File.createTempFile("fudaa.project", ".db");
            if (dbFile_ != null) {
                FudaaSaveLib.configureDb4o();
                cont_ = Db4o.openFile(dbFile_.getAbsolutePath());
            }
        }
        return cont_;
    }

    public static String getDbEntryName() {
        return "project.db";
    }

    public OutputStream getOutStream() {
        return zipOut_;
    }

    public DirZipEntry getDirZipEntry(final String _name) {
        return (nameDirZip_ == null) ? null : (DirZipEntry) nameDirZip_.get(_name);
    }

    public static String getProjectDescEntryName() {
        return "project.desc.xml";
    }
}
