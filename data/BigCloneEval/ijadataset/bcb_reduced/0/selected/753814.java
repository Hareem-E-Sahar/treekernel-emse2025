package bt747.j2se_view;

import gps.BT747Constants;
import gps.log.out.GPSFileInterface;
import gps.log.out.GPSKMLFile;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.bt747.j2se.system.J2SEHashtable;
import bt747.sys.Generic;
import bt747.sys.interfaces.BT747Path;

/**
 * Class to write a KML file.
 * 
 * @author Mario De Weerd
 * 
 */
public final class GPSKMZFile extends GPSKMLFile implements GPSFileInterface {

    private ZipOutputStream currentZipStream;

    private J2SEHashtable zips;

    /**
     * 
     */
    public GPSKMZFile() {
        super();
    }

    public final void initialiseFile(final BT747Path basename, final String ext, final int oneFilePerDay) {
        super.initialiseFile(basename, ext, oneFilePerDay);
        zips = new J2SEHashtable(10);
    }

    public final void finaliseFile() {
        super.finaliseFile();
        if (currentZipStream != null) {
            try {
                zips.remove(currentZipStream);
            } catch (final Exception e) {
                Generic.debug("zip stream removal", e);
            }
            try {
                currentZipStream.closeEntry();
                currentZipStream.close();
            } catch (final Exception e) {
                Generic.debug("finaliseFile", e);
            }
            currentZipStream = null;
        }
    }

    @Override
    protected int createFile(final int utc, final String extra_ext, final boolean createNewFile) {
        String zipFileName;
        String zipEntryFileName;
        setTrackName(extra_ext);
        zipFileName = filenameBuilder.getOutputFileName(basename, utc, ".kmz", extra_ext).getPath();
        zipEntryFileName = basename + extra_ext + ".kml";
        int l;
        l = zipEntryFileName.lastIndexOf('/');
        if (l > 0) {
            zipEntryFileName = zipEntryFileName.substring(l + 1);
        }
        l = zipEntryFileName.lastIndexOf('\\');
        if (l > 0) {
            zipEntryFileName = zipEntryFileName.substring(l + 1);
        }
        l = zipEntryFileName.lastIndexOf(':');
        if (l > 0) {
            zipEntryFileName = zipEntryFileName.substring(l + 1);
        }
        int error = BT747Constants.NO_ERROR;
        try {
            if (createNewFile) {
                final File tmpFile = new File(zipFileName);
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
            }
        } catch (final Exception e) {
            Generic.debug("File deletion", e);
        }
        try {
            currentZipStream = null;
            if (createNewFile) {
                final FileOutputStream fos = new FileOutputStream(zipFileName, false);
                final BufferedOutputStream bos = new BufferedOutputStream(fos);
                currentZipStream = new ZipOutputStream(bos);
                final ZipEntry e = new ZipEntry(zipEntryFileName);
                currentZipStream.putNextEntry(e);
                zips.put(zipFileName, currentZipStream);
            } else {
                if (zips == null) {
                    Generic.debug("Zip name is null ");
                } else {
                    currentZipStream = (ZipOutputStream) zips.get(zipFileName);
                    if (currentZipStream == null) {
                        Generic.debug("Could not find " + zipFileName + " zip stream.");
                    }
                }
            }
        } catch (final Exception e) {
            Generic.debug("Zip Entry Creation", e);
        }
        return error;
    }

    protected final void writeTxt(final String s) {
        try {
            if (currentZipStream != null) {
                currentZipStream.write(s.getBytes(), 0, s.length());
            } else {
                Generic.debug("Write to closed file", null);
            }
        } catch (final Exception e) {
            Generic.debug("writeTxt", e);
        }
    }

    @Override
    protected boolean isOpen() {
        return currentZipStream != null;
    }
}
