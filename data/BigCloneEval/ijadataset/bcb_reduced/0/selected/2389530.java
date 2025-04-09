package jsynoptic.plugins.export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import simtools.data.DataException;
import simtools.data.DataInfo;
import simtools.data.DataSource;
import simtools.data.DataSourceCollection;
import simtools.data.DataSourcePool;
import simtools.data.DuplicateIdException;
import simtools.shapes.AbstractShape;
import simtools.shapes.ShapeListener;
import simtools.shapes.ShapesContainer;
import simtools.shapes.PlotShape.Curve;
import simtools.ui.MenuResourceBundle;
import simtools.ui.ResourceFinder;
import simtools.util.CurrentPathProvider;
import simtools.util.FileReferenceListener;
import simtools.util.FileSerializer;
import jsynoptic.base.Linkable;
import jsynoptic.base.Plugin;
import jsynoptic.builtin.Plot;
import jsynoptic.plugins.export.ui.SynopticsExportPanel;
import jsynoptic.ui.JSynoptic;
import jsynoptic.ui.JSynopticPanels;
import jsynoptic.ui.ShapesContainer.ShapesComponent;

/**
 * 
 * A plugin dedicated to export some synoptics to an archive (zip) file.
 * It shall be possible to include all synoptics references into this archive file.
 * @author zxpletran007
 *
 */
public class SynopticsExportPlugin extends Plugin implements FileReferenceListener {

    public static MenuResourceBundle resources = ResourceFinder.getMenu(SynopticsExportPlugin.class);

    protected static final int BUFFER = 4096;

    protected static MenuResourceBundle.FileFilter zipFileFilter = resources.getFileFilter("zipFileFilter");

    protected static javax.swing.filechooser.FileFilter[] filters = new javax.swing.filechooser.FileFilter[] { zipFileFilter };

    protected static SynopticsExportPanel optionPanel;

    private List dependancies;

    private File dependanciesRepository;

    private File temRep;

    public JComponent getOptionPanelForFilter(FileFilter filter) {
        if (zipFileFilter.equals(filter)) {
            return optionPanel = new SynopticsExportPanel();
        }
        return null;
    }

    public javax.swing.filechooser.FileFilter[] getFileFilters(int action) {
        if (action != EXPORT) return null;
        return filters;
    }

    public boolean processFile(File zipFile, int action) {
        boolean hasSucceed = true;
        if (action == Plugin.EXPORT) {
            List selectedSheets = optionPanel.getSelectSheet();
            if (!selectedSheets.isEmpty()) {
                File oldCurrentPath = CurrentPathProvider.currentPathProvider.getCurrentPath();
                temRep = new File(oldCurrentPath, "tempRep");
                int index = 0;
                while (temRep.exists()) {
                    temRep = new File(oldCurrentPath, "tempRep" + index);
                    index++;
                }
                temRep.mkdir();
                CurrentPathProvider.currentPathProvider.setCurrentPath(temRep);
                if (optionPanel.includeDependingResources()) {
                    dependancies = new ArrayList();
                    dependanciesRepository = new File(temRep, "resources");
                    dependanciesRepository.mkdir();
                    FileSerializer.addListener(this);
                } else {
                    dependancies = null;
                    dependanciesRepository = null;
                }
                try {
                    for (int i = 0; i < selectedSheets.size(); i++) {
                        ShapesContainer sc = ((ShapesComponent) selectedSheets.get(i)).getContainer();
                        File sheetFile = JSynoptic.gui.getFilePanel().getFile(sc.getComponent());
                        String fileName;
                        if (sheetFile == null) {
                            fileName = sc.getComponent().getName();
                            fileName = "Doc" + fileName.charAt(fileName.length() - 1) + JSynopticPanels.resources.getStringValue("defaultSaveExtension");
                        } else {
                            fileName = sheetFile.getName();
                        }
                        sheetFile = new File(temRep, fileName);
                        saveShapesContainer(sc, sheetFile);
                    }
                } catch (IOException e) {
                    hasSucceed = false;
                }
                CurrentPathProvider.currentPathProvider.setCurrentPath(oldCurrentPath);
                if (hasSucceed) {
                    hasSucceed = archiveSynoptics(temRep.listFiles(), zipFile);
                }
                deleteResource(temRep);
            }
        }
        return hasSucceed;
    }

    /**
     * Save shape container
     * @param shapeContainer
     * @return
     * @throws IOException 
     */
    protected boolean saveShapesContainer(ShapesContainer sc, File f) throws IOException {
        setRelativeLinks(sc, f);
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(sc);
        os.writeObject(null);
        os.writeObject(null);
        os.writeObject(null);
        os.flush();
        os.close();
        fos.close();
        restoreAbsoluteLinks(sc, f);
        return true;
    }

    protected void restoreAbsoluteLinks(ShapesContainer sc, File f) throws IOException {
        for (Iterator it = sc.iterator(); it.hasNext(); ) {
            try {
                AbstractShape abs = (AbstractShape) it.next();
                abs.addListener((ShapeListener) sc.getComponent());
                if (!(abs instanceof Linkable)) {
                    continue;
                }
                Linkable l = (Linkable) abs;
                File parent = f.getParentFile();
                if (parent == null) {
                    parent = new File("./");
                }
                String link = l.getLink();
                if ((link != null) && (!link.equals(""))) {
                    File nl = new File(parent, link);
                    l.setLink(nl.getCanonicalPath());
                }
            } catch (ClassCastException cce) {
            }
        }
    }

    protected void setRelativeLinks(ShapesContainer sc, File f) throws IOException {
        for (Iterator it = sc.iterator(); it.hasNext(); ) {
            Object o = it.next();
            if (!(o instanceof Linkable)) {
                continue;
            }
            Linkable l = (Linkable) o;
            String link = l.getLink();
            if (link != null) {
                File flink = new File(link);
                String sf = f.getCanonicalPath();
                String sl = flink.getCanonicalPath();
                int i = -1, lastOK = -1;
                while ((i < sl.length()) && (i < sf.length()) && ((i == -1) || sf.substring(0, i).equals(sl.substring(0, i)))) {
                    lastOK = i;
                    i = sl.indexOf(File.separator, i + 1);
                    if (i == -1) {
                        break;
                    }
                }
                String subf = sf.substring(lastOK + 1, sf.length());
                String subl = sl.substring(lastOK + 1, sl.length());
                String prefix = "";
                i = -1;
                while (true) {
                    i = subf.indexOf(File.separator, i + 1);
                    if (i == -1) {
                        break;
                    }
                    prefix += "../";
                }
                link = prefix + subl;
                link = link.replaceAll("\\\\", "/");
                l.setLink(link);
            }
        }
    }

    /**
     * Add a file entry to the given archive output stream
     * @param zos
     * @param f
     * @throws IOException
     */
    protected void addFileResourceToArchive(ZipOutputStream zos, File f, String zipEntryName) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                addFileResourceToArchive(zos, files[i], zipEntryName + File.separator + files[i].getName());
            }
        } else {
            FileInputStream fi = new FileInputStream(f);
            BufferedInputStream buffi = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(zipEntryName);
            zos.putNextEntry(entry);
            byte data[] = new byte[BUFFER];
            int count;
            while ((count = buffi.read(data, 0, BUFFER)) != -1) {
                zos.write(data, 0, count);
            }
            zos.closeEntry();
            buffi.close();
        }
    }

    public String about() {
        return resources.getString("about");
    }

    public void fileReferenceCalled(File srcFile) {
        File destFile = new File(dependanciesRepository, srcFile.getName());
        FileSerializer.setFile(destFile);
        if ((dependancies != null) && (srcFile.exists()) && !dependancies.contains(srcFile)) {
            dependancies.add(srcFile);
            copyResource(srcFile, destFile);
        }
    }

    /**
     * Archive file into zip archive file
     * @param source
     * @param zipFile
     * @return true if archive process has succeeded
     */
    protected boolean archiveSynoptics(File[] files, File zipFile) {
        boolean res = true;
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            zos.setMethod(ZipOutputStream.DEFLATED);
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    addFileResourceToArchive(zos, files[i], files[i].getName());
                }
            }
        } catch (IOException e) {
            res = false;
        }
        try {
            if (zos != null) {
                zos.finish();
                zos.close();
            }
        } catch (IOException e) {
            res = false;
        }
        return res;
    }

    /**
     * Delete a file. If file is a repository, delete also its contents
     * @param resource the file to be deleted
     */
    protected void deleteResource(File resource) {
        if (resource != null) {
            if (resource.isDirectory()) {
                File[] files = resource.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        File f = files[i];
                        deleteResource(f);
                    }
                }
            }
            resource.delete();
        }
    }

    /**
     * Copy the resource to another destination
     * @param source The file to copy
     * @param destination The new file
     */
    protected void copyResource(File source, File destination) {
        if (!(source.equals((temRep)))) {
            if (source.isDirectory()) {
                File directory = destination;
                directory.mkdir();
                File[] files = source.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        File f = files[i];
                        destination = new File(directory, f.getName());
                        copyResource(f, destination);
                    }
                }
            } else {
                FileOutputStream outStream = null;
                FileInputStream inStream = null;
                if (destination.exists()) {
                    deleteResource(destination);
                }
                try {
                    destination.createNewFile();
                    inStream = new FileInputStream(source);
                    outStream = new FileOutputStream(destination);
                    byte buffer[] = new byte[512 * 1024];
                    int nb;
                    while ((nb = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, nb);
                    }
                } catch (java.io.FileNotFoundException f) {
                } catch (java.io.IOException e) {
                } finally {
                    try {
                        if (inStream != null) inStream.close();
                    } catch (Exception e) {
                    }
                    try {
                        if (outStream != null) outStream.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}
