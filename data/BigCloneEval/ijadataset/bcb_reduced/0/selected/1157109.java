package net.sourceforge.blogentis.storage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarOutputStream;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.CopyUtils;
import net.sourceforge.blogentis.om.Blog;
import net.sourceforge.blogentis.plugins.AbstractBlogExtension;
import net.sourceforge.blogentis.plugins.importexport.IExportExtension;
import net.sourceforge.blogentis.storage.AbstractFileResource;
import net.sourceforge.blogentis.storage.FileResourceFilter;
import net.sourceforge.blogentis.storage.FileRetrieverService;
import net.sourceforge.blogentis.turbine.BlogParameterParser;
import net.sourceforge.blogentis.turbine.BlogRunData;

public class StorageExportExtension extends AbstractBlogExtension implements IExportExtension {

    public static final String ZIP_MIME_TYPE = "application/zip";

    public static final String TAR_GZ_MIME_TYPE = "application/x-gtar";

    public String getContentType(Blog blog, Configuration conf) {
        return conf.getBoolean("isZip", true) ? ZIP_MIME_TYPE : TAR_GZ_MIME_TYPE;
    }

    public String getFileName(BlogRunData data, Blog blog, Configuration conf) {
        return blog.getName() + "-" + (conf.getBoolean("media", false) ? "media" : "templates") + (conf.getBoolean("isZip", true) ? ".zip" : ".tar.gz");
    }

    public String getFileTypeName() {
        return "Theme or media files as an archive";
    }

    public String getIdentifier() {
        return "stored-files";
    }

    public String getPreferencesKey() {
        return StoragePrefsExtension.EXPORT_FILES_OPTIONS;
    }

    public String getName() {
        return "Backup the theme or the media folder.";
    }

    public String performExport(BlogRunData data, Blog blog, OutputStream stream, Configuration conf) throws Exception {
        BlogParameterParser bpp = (BlogParameterParser) data.getParameters();
        FileResourceFilter filter;
        if (conf.getBoolean("media", false)) filter = new FileResourceFilter.AllMediaFilter(); else filter = new FileResourceFilter.AllTemplatesFilter();
        List l = FileRetrieverService.getInstance().getTemplatePathList(data, bpp.getBlog(), "/", filter);
        ServletOutputStream os = data.getResponse().getOutputStream();
        if (conf.getBoolean("isZip", true)) writeZipFile(bpp.getBlog(), l, os); else writeTarFile(bpp.getBlog(), l, os);
        os.flush();
        return null;
    }

    protected void writeTarFile(Blog blog, List files, OutputStream out) throws IOException {
        GZIPOutputStream gzos = new GZIPOutputStream(out);
        TarOutputStream tos = new TarOutputStream(gzos);
        tos.setLongFileMode(TarOutputStream.LONGFILE_GNU);
        FileRetrieverService frs = FileRetrieverService.getInstance();
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            String path = (String) i.next();
            AbstractFileResource file = frs.getFile(blog, path);
            TarEntry te;
            if (path.startsWith("/")) path = path.substring(1);
            if (file.isDirectory()) {
                if (!path.endsWith("/")) path = path + "/";
                te = new TarEntry(path);
            } else {
                if (file.isOriginal() || file.getSize() == 0) continue;
                te = new TarEntry(path);
                te.setSize(file.getSize());
                te.setModTime(file.getLastModified());
                tos.putNextEntry(te);
                InputStream fis = file.getFileAsInputStream();
                tos.copyEntryContents(fis);
                fis.close();
            }
            tos.closeEntry();
        }
        tos.close();
    }

    protected void writeZipFile(Blog blog, List files, OutputStream out) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(out);
        FileRetrieverService frs = FileRetrieverService.getInstance();
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            String path = (String) i.next();
            AbstractFileResource file = frs.getFile(blog, path);
            if (file.isDirectory() || file.isOriginal() || file.getSize() == 0) continue;
            ZipEntry ze = new ZipEntry(path);
            ze.setSize(file.getSize());
            zos.putNextEntry(ze);
            InputStream fis = file.getFileAsInputStream();
            CopyUtils.copy(fis, zos);
            fis.close();
            zos.closeEntry();
        }
        zos.finish();
        zos.flush();
    }
}
