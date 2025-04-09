package ch.skyguide.tools.requirement.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import ch.skyguide.tools.requirement.data.AbstractRequirement;
import ch.skyguide.tools.requirement.data.DescriptionDataSource;
import ch.skyguide.tools.requirement.data.GenericRequirementVisitor;
import ch.skyguide.tools.requirement.data.RequirementDomain;
import ch.skyguide.tools.requirement.data.RequirementProject;

public class RequirementDocumentFormat implements IRequirementDocumentFormat {

    private static final boolean DEBUG_ALSO_SAVE_SEPARATE_MODEL_XML = false;

    public static final String SERIALIZED_EXTENSION_SUFFIX = ".req";

    public static final String XML_EXTENSION_SUFFIX = ".reqx";

    private static final String MODEL_XML_FILE_NAME = "model.xml";

    private static final String REQ_DESCRIPTION_ENTRY_PREFIX = "description/";

    private static final String REQ_DESCRIPTION_ENTRY_SUFFIX = ".odt";

    private static RequirementDocumentFormat instance;

    private final JAXBContext jaxbContext;

    private CustomAttachmentMarshaller attachmentMarshaller;

    private CustomAttachmentUnmarshaller attachmentUnmarshaller;

    private final Marshaller marshaller;

    private final Unmarshaller unmarshaller;

    private RequirementDocumentFormat() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(RequirementProject.class);
        attachmentMarshaller = new CustomAttachmentMarshaller();
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setAttachmentMarshaller(attachmentMarshaller);
        attachmentUnmarshaller = new CustomAttachmentUnmarshaller();
        unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
    }

    public static IRequirementDocumentFormat getInstance() throws JAXBException {
        if (instance == null) {
            instance = new RequirementDocumentFormat();
        }
        return instance;
    }

    public void save(RequirementProject _requirementProject, File _file) throws IOException, RequirementDocumentFormatException {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(_file));
        save(_requirementProject, bufferedOutputStream);
    }

    public void save(RequirementProject _requirementProject, OutputStream _out) throws IOException, RequirementDocumentFormatException {
        ZipOutputStream zout = new ZipOutputStream(_out);
        zout.setLevel(0);
        ByteArrayOutputStream xmlBuffer = new ByteArrayOutputStream();
        attachmentMarshaller.setZout(zout);
        writeXml(_requirementProject, xmlBuffer);
        attachmentMarshaller.setZout(null);
        if (DEBUG_ALSO_SAVE_SEPARATE_MODEL_XML) {
            IoHelper.write(xmlBuffer.toByteArray(), new File("model_debug.xml"));
        }
        zout.setLevel(9);
        ZipEntry zipEntry = new ZipEntry(MODEL_XML_FILE_NAME);
        zout.putNextEntry(zipEntry);
        zout.write(xmlBuffer.toByteArray());
        zout.closeEntry();
        zout.close();
    }

    public RequirementProject load(File _file) throws IOException, RequirementDocumentFormatException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(_file));
        RequirementProject result = load(bufferedInputStream);
        bufferedInputStream.close();
        return result;
    }

    public RequirementProject load(InputStream _in) throws IOException, RequirementDocumentFormatException {
        ZipInputStream zin = new ZipInputStream(_in);
        attachmentUnmarshaller.clearData();
        RequirementProject result = null;
        for (; ; ) {
            ZipEntry zipEntry = zin.getNextEntry();
            if (zipEntry == null) {
                break;
            }
            String zipEntryName = zipEntry.getName();
            if (MODEL_XML_FILE_NAME.equals(zipEntryName)) {
                result = readXml(zin);
            } else {
                if (result != null) {
                    throw new RequirementDocumentFormatException("Unexpected entry " + zipEntryName + " (model already set)");
                }
                attachmentUnmarshaller.addData(zipEntryName, IoHelper.readFully(zin));
            }
            zin.closeEntry();
        }
        return result;
    }

    private RequirementProject readXml(ZipInputStream _zin) throws RequirementDocumentFormatException {
        NonClosableInputStream nonClosableInputStream = new NonClosableInputStream(_zin);
        RequirementProject project;
        try {
            project = (RequirementProject) unmarshaller.unmarshal(nonClosableInputStream);
        } catch (JAXBException e) {
            throw new RequirementDocumentFormatException("Unmarshal failed", e);
        }
        project.accept(new GenericRequirementVisitor() {

            @Override
            public void visit(RequirementDomain _requirementDomain) {
                for (AbstractRequirement req : _requirementDomain) {
                    req.setParent(_requirementDomain);
                }
                super.visit(_requirementDomain);
            }

            @Override
            protected void visitAny(AbstractRequirement _requirement) {
                if (_requirement.getParent() == null && !(_requirement instanceof RequirementProject)) {
                    throw new RuntimeException("Node " + _requirement.getCode() + " has no parent");
                }
            }
        });
        return project;
    }

    private void writeXml(RequirementProject _project, OutputStream _out) throws RequirementDocumentFormatException {
        try {
            marshaller.marshal(_project, _out);
        } catch (JAXBException e) {
            throw new RequirementDocumentFormatException("Marshal failed", e);
        }
    }

    @SuppressWarnings("serial")
    private static class IOExceptionWrapper extends RuntimeException {

        private final IOException ioException;

        public IOExceptionWrapper(IOException _ioe) {
            super(_ioe);
            ioException = _ioe;
        }

        public IOException getIoException() {
            return ioException;
        }
    }

    private static class CustomAttachmentMarshaller extends AttachmentMarshaller {

        private ZipOutputStream zout;

        private int counter;

        public void setZout(ZipOutputStream _zout) {
            zout = _zout;
            counter = 0;
        }

        @Override
        public String addSwaRefAttachment(DataHandler _data) {
            String contentType = _data.getContentType();
            if (!DescriptionDataSource.CONTENT_TYPE.equals(contentType)) {
                throw new IllegalArgumentException("Content type not handled: " + contentType);
            }
            InputStream in;
            try {
                in = _data.getInputStream();
            } catch (IOException e) {
                throw new IOExceptionWrapper(e);
            }
            if (in == null) {
                return "";
            }
            String entry = "" + counter++;
            String name = _data.getName();
            if (name != null) {
                entry += "_" + name;
            }
            String zipEntryName = REQ_DESCRIPTION_ENTRY_PREFIX + entry + REQ_DESCRIPTION_ENTRY_SUFFIX;
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            try {
                zout.putNextEntry(zipEntry);
                IoHelper.copy(in, zout);
                in.close();
                zout.closeEntry();
            } catch (IOException e) {
                throw new IOExceptionWrapper(e);
            }
            return zipEntryName;
        }

        @Override
        public String addMtomAttachment(DataHandler _data, String _elementNamespace, String _elementLocalName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String addMtomAttachment(byte[] _data, int _offset, int _length, String _mimeType, String _elementNamespace, String _elementLocalName) {
            throw new UnsupportedOperationException();
        }
    }

    private static class CustomAttachmentUnmarshaller extends AttachmentUnmarshaller {

        private final Map<String, byte[]> dataMap = new HashMap<String, byte[]>();

        public void clearData() {
            dataMap.clear();
        }

        public void addData(String _name, byte[] _data) {
            dataMap.put(_name, _data);
        }

        @Override
        public byte[] getAttachmentAsByteArray(String _cid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataHandler getAttachmentAsDataHandler(final String _cid) {
            return new DataHandler(new DataSource() {

                public InputStream getInputStream() {
                    return new ByteArrayInputStream(dataMap.get(_cid));
                }

                public String getContentType() {
                    throw new UnsupportedOperationException();
                }

                public String getName() {
                    throw new UnsupportedOperationException();
                }

                public OutputStream getOutputStream() {
                    throw new UnsupportedOperationException();
                }
            });
        }
    }
}
