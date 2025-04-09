package org.vosao.business.impl.imex;

import java.io.IOException;
import java.util.zip.ZipEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vosao.business.Business;
import org.vosao.business.imex.ExporterFactory;
import org.vosao.business.imex.task.DaoTaskAdapter;
import org.vosao.business.imex.task.TaskTimeoutException;
import org.vosao.business.imex.task.ZipOutStreamTaskAdapter;
import org.vosao.dao.Dao;

public abstract class AbstractExporter {

    protected static final Log logger = LogFactory.getLog(AbstractExporter.class);

    public static void saveFile(final ZipOutStreamTaskAdapter out, String name, String content) throws IOException, TaskTimeoutException {
        out.putNextEntry(new ZipEntry(name));
        out.write(content.getBytes("UTF-8"));
        out.closeEntry();
        out.nextFile();
    }

    private ExporterFactory exporterFactory;

    public AbstractExporter(ExporterFactory factory) {
        exporterFactory = factory;
    }

    public Dao getDao() {
        return getBusiness().getDao();
    }

    public Business getBusiness() {
        return getExporterFactory().getBusiness();
    }

    public DaoTaskAdapter getDaoTaskAdapter() {
        return getExporterFactory().getDaoTaskAdapter();
    }

    public ExporterFactory getExporterFactory() {
        return exporterFactory;
    }
}
