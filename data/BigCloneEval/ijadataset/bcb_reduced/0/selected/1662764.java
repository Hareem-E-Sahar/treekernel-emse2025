package org.qedeq.kernel.xml.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.qedeq.base.io.SourceArea;
import org.qedeq.base.io.TextOutput;
import org.qedeq.base.trace.Trace;
import org.qedeq.kernel.bo.common.QedeqBo;
import org.qedeq.kernel.bo.module.InternalKernelServices;
import org.qedeq.kernel.bo.module.KernelQedeqBo;
import org.qedeq.kernel.bo.module.QedeqFileDao;
import org.qedeq.kernel.se.base.module.Qedeq;
import org.qedeq.kernel.se.common.ModuleContext;
import org.qedeq.kernel.se.common.ModuleDataException;
import org.qedeq.kernel.se.common.Plugin;
import org.qedeq.kernel.se.common.SourceFileExceptionList;
import org.qedeq.kernel.xml.handler.common.SaxDefaultHandler;
import org.qedeq.kernel.xml.handler.module.QedeqHandler;
import org.qedeq.kernel.xml.mapper.Context2SimpleXPath;
import org.qedeq.kernel.xml.parser.SaxParser;
import org.qedeq.kernel.xml.tracker.SimpleXPath;
import org.qedeq.kernel.xml.tracker.XPathLocationParser;
import org.xml.sax.SAXException;
import com.sun.syndication.io.XmlReader;

/**
 * This class provides access methods for loading QEDEQ modules from XML files.
 *
 * @author  Michael Meyling
 */
public class XmlQedeqFileDao implements QedeqFileDao, Plugin {

    /** This class. */
    private static final Class CLASS = XmlQedeqFileDao.class;

    /** Internal kernel services. */
    private InternalKernelServices services;

    /**
     * Constructor.
     */
    public XmlQedeqFileDao() {
    }

    public void setServices(final InternalKernelServices services) {
        this.services = services;
    }

    public InternalKernelServices getServices() {
        return this.services;
    }

    public Qedeq loadQedeq(final QedeqBo prop, final File file) throws SourceFileExceptionList {
        final String method = "loadLocalModule";
        SaxDefaultHandler handler = new SaxDefaultHandler(this);
        QedeqHandler simple = new QedeqHandler(handler);
        handler.setBasisDocumentHandler(simple);
        SaxParser parser = null;
        Locale.setDefault(Locale.US);
        try {
            parser = new SaxParser(this, handler);
        } catch (SAXException e) {
            Trace.fatal(CLASS, this, method, "XML Parser: Severe configuration problem.", e);
            throw services.createSourceFileExceptionList(DaoErrors.PARSER_CONFIGURATION_ERROR_CODE, DaoErrors.PARSER_CONFIGURATION_ERROR_TEXT, file + "", e);
        } catch (ParserConfigurationException e) {
            Trace.fatal(CLASS, this, method, "XML Parser: Option not recognized or supported.", e);
            throw services.createSourceFileExceptionList(DaoErrors.PARSER_CONFIGURATION_OPTION_ERROR_CODE, DaoErrors.PARSER_CONFIGURATION_OPTION_ERROR_TEXT, file + "", e);
        }
        try {
            parser.parse(file, prop.getUrl());
        } catch (SourceFileExceptionList e) {
            Trace.trace(CLASS, this, method, e);
            throw e;
        }
        return simple.getQedeq();
    }

    public void saveQedeq(final KernelQedeqBo prop, final File localFile) throws SourceFileExceptionList, IOException {
        final OutputStream outputStream = new FileOutputStream(localFile);
        final TextOutput printer = new TextOutput(localFile.getName(), outputStream, "UTF-8");
        Qedeq2Xml.print(this, prop, printer);
    }

    public SourceArea createSourceArea(final Qedeq qedeq, final ModuleContext context) {
        final String method = "createSourceArea(Qedeq, ModuleContext)";
        if (context == null) {
            return null;
        }
        if (qedeq == null) {
            return new SourceArea(context.getModuleLocation().getUrl());
        }
        ModuleContext ctext = new ModuleContext(context);
        final SimpleXPath xpath;
        try {
            xpath = Context2SimpleXPath.getXPath(ctext, qedeq);
        } catch (ModuleDataException e) {
            Trace.fatal(CLASS, method, "not found: \"" + ctext + "\"", e);
            if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("qedeq.test.xmlLocationFailures"))) {
                throw new RuntimeException(e);
            }
            return new SourceArea(ctext.getModuleLocation().getUrl());
        }
        final File local = services.getLocalFilePath(ctext.getModuleLocation());
        return XPathLocationParser.findSourceArea(ctext.getModuleLocation().getUrl(), xpath, ctext.getStartDelta(), ctext.getEndDelta(), local);
    }

    public Reader getModuleReader(final KernelQedeqBo bo) throws IOException {
        return new XmlReader(services.getLocalFilePath(bo.getModuleAddress()));
    }

    public String getPluginId() {
        return CLASS.getName();
    }

    public String getPluginActionName() {
        return "XML Worker";
    }

    public String getPluginDescription() {
        return "can read and write XML QEDEQ modules";
    }
}
