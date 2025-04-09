package net.sourceforge.circuitsmith.xmlparser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.sourceforge.circuitsmith.eda.CircuitSmith;
import net.sourceforge.circuitsmith.eda.CircuitSmithSettings;
import net.sourceforge.circuitsmith.objectfactory.EdaSaveableObjectFactory;
import net.sourceforge.circuitsmith.projects.EdaProject;
import net.sourceforge.circuitsmith.xmlparser.project.EdaVersionedProjectHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Facade for XML parsing to EDA. Offers two methods: parseEdaSettings to retrieve an EdaSettings instance from its xml
 * representation and parseEdaProject to retrieve an EdaProject from ...
 * <p>
 * @author holger
 */
public final class EdaXmlParser {

    private final EdaSaveableObjectFactory factory;

    private final SAXParser saxParser;

    private final CircuitSmith eda;

    public EdaXmlParser(final EdaSaveableObjectFactory aFactory, final CircuitSmith anEda) throws EdaXmlException {
        if (aFactory == null) {
            throw new IllegalArgumentException("Factory must not be null.");
        }
        factory = aFactory;
        if (anEda == null) {
            throw new IllegalArgumentException("Eda must not be null.");
        }
        eda = anEda;
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(false);
        try {
            saxParser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new EdaXmlException("Cannot create parser.", e);
        } catch (SAXException e) {
            throw new EdaXmlException("Cannot create parser.", e);
        }
    }

    public CircuitSmithSettings parseEdaSettings(final File settingsFile) throws EdaXmlException {
        if (settingsFile == null) {
            throw new IllegalArgumentException("settingsFile must not be null.");
        }
        if (!settingsFile.exists()) {
            throw new EdaXmlException("File " + settingsFile + " does not exist.");
        }
        final EdaSettingsHandler handler = new EdaSettingsHandler(eda);
        synchronized (saxParser) {
            try {
                final FileInputStream fileInput = new FileInputStream(settingsFile);
                final InputSource source = new InputSource(fileInput);
                source.setPublicId(settingsFile.getAbsolutePath());
                parse(source, handler, Boolean.FALSE);
            } catch (FileNotFoundException e) {
                throw new EdaXmlException("Error reading settings file " + settingsFile + ".", e);
            }
        }
        return handler.getEdaSettings();
    }

    public EdaProject parseEdaProject(final File projectFile) throws EdaXmlException {
        if (projectFile == null) {
            throw new IllegalArgumentException("projectFile must not be null.");
        }
        if (!projectFile.exists()) {
            throw new EdaXmlException("File " + projectFile + " does not exist.");
        }
        final EdaProjectHandler handler = new EdaProjectHandler(factory, eda, projectFile);
        synchronized (saxParser) {
            try {
                final ByteArrayInputStream inputStream = createInputStream(projectFile);
                final InputSource source = new InputSource(inputStream);
                source.setPublicId(projectFile.getAbsolutePath());
                parse(source, handler, Boolean.TRUE);
            } catch (IOException e) {
                throw new EdaXmlException("Error reading project file " + projectFile + ".", e);
            }
        }
        return handler.getProject();
    }

    private ByteArrayInputStream createInputStream(final File projectFile) throws IOException {
        final boolean hasXmlTag;
        final BufferedReader reader = new BufferedReader(new FileReader(projectFile));
        try {
            final String line = reader.readLine();
            if (null == line) {
                throw new IOException("Error parsing project file " + projectFile + ": it is empty.");
            }
            hasXmlTag = line.startsWith("<?xml");
        } finally {
            reader.close();
        }
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (!hasXmlTag) {
            buffer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
            buffer.write(("<!DOCTYPE OBJECT SYSTEM \"" + EdaVersionedProjectHandler.Version.VERSION0.getDtd() + "\">").getBytes());
        }
        final FileInputStream fileInput = new FileInputStream(projectFile);
        try {
            final byte[] tmp = new byte[4096];
            do {
                final int bytesRead = fileInput.read(tmp);
                if (-1 == bytesRead) {
                    break;
                }
                buffer.write(tmp, 0, bytesRead);
            } while (true);
        } finally {
            fileInput.close();
        }
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());
        return inputStream;
    }

    private void parse(final InputSource source, final EdaDefaultHandler handler, final Boolean validate) throws EdaXmlException {
        try {
            saxParser.parse(source, handler);
        } catch (EdaSAXException e) {
            Throwable nested = e.getException();
            if (nested != null) {
                throw new EdaXmlException(e.getMessage(), nested);
            }
            throw new EdaXmlException(e.getMessage(), e);
        } catch (SAXParseException e) {
            throw new EdaXmlException("Parser error: >>" + e.getMessage() + "<< in file \"" + e.getPublicId() + "\" at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ".", e);
        } catch (SAXException e) {
            Throwable nested = e.getException();
            if (nested instanceof RuntimeException) {
                throw (RuntimeException) nested;
            }
            throw new EdaXmlException("Error during parsing file " + source.getPublicId() + ".", e);
        } catch (IOException e) {
            throw new EdaXmlException("Error parsing file " + source.getPublicId() + ".", e);
        }
        for (final SAXParseException saxException : handler.getSaxParserWarnings()) {
            System.err.println("Warning: >>" + saxException.getMessage() + "<< in file \"" + saxException.getPublicId() + "\" at line " + saxException.getLineNumber() + ", column " + saxException.getColumnNumber() + ".");
        }
    }

    public EdaProject parseLibrary(final File libraryFile, final File projectDirectory) throws EdaXmlException {
        final File foundFile;
        if (libraryFile.exists()) {
            foundFile = libraryFile;
        } else {
            if (libraryFile.isAbsolute()) {
                throw new EdaXmlException("Library file " + libraryFile + " does not exist.");
            }
            if (projectDirectory.equals(libraryFile.getParent())) {
                throw new EdaXmlException("Library file " + libraryFile + " does not exist.");
            }
            final String path = libraryFile.getPath();
            foundFile = new File(projectDirectory, path);
            if (!foundFile.exists()) {
                throw new EdaXmlException("Library file not found. Neither " + path + " nor " + foundFile + "exist.");
            }
        }
        return parseEdaProject(foundFile);
    }
}
