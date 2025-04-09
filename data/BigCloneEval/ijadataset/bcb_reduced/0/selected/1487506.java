package r2q2.router;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Service;
import r2q2.NoAttachmentException;
import r2q2.SpecificationViolationException;
import r2q2.content.ws.ContentUnpackerServiceLocator;
import r2q2.content.ws.ContentUnpackerSoapBindingStub;
import r2q2.content.ws.EncodedFileList;
import r2q2.initialisation.InitialisationException;
import r2q2.processing.ProcessingException;
import r2q2.rendering.RenderingException;
import r2q2.router.interactions.InteractionProcessorController;
import r2q2.router.interactions.NoPreviousStageException;
import r2q2.router.interactions.NoSuchProcessorException;
import r2q2.util.soap.attachments.AttachmentBuilder;
import r2q2.util.streams.StreamCopier;
import r2q2.util.properties.*;

/**
 * TODO javadoc
 * @author Gavin Willingham
 */
public class Router {

    private static String localBasePath = null;

    private static String httpBasePath = null;

    private static String contentUnpackerWebServiceAddress = null;

    private static InteractionProcessorController ipc = new InteractionProcessorController();

    /**
     * Default constructor. Loads properties if not already set
     * 
     * @throws IOException 	if the PropertiesLoader can't find the properties 
     * 						file
     * @throws NoSuchPropertyException 	if either the VM argument is not set, 
     * 									or one of the requested properties is 
     * 									not found
     */
    public Router() throws IOException, NoSuchPropertyException {
        if (localBasePath == null) localBasePath = PropertiesLoader.getProperty("r2q2.router.content.localBasePath");
        if (httpBasePath == null) httpBasePath = PropertiesLoader.getProperty("r2q2.router.content.httpBasePath");
        if (contentUnpackerWebServiceAddress == null) contentUnpackerWebServiceAddress = PropertiesLoader.getProperty("r2q2.content.ws.location");
    }

    /**
     * Creates a new session.
     * 
     * ADAPTED to enable delivery engines to determine what is rendered via the renderOpts int
     * 
     * @author Rob Blowers - rwb104@ecs.soton.ac.uk
     * 
     * <p>
     * Creates a new InteractionProcessor built upon the QTI XML content
     * package or XML file supplied as an attachment.
     * </p>
     * 
     * @param _xmlName 	This parameter is the filename of the question XML
     * @param _isContentPackage Specifies whether the attachment is a content 
     * 							package or xml file
     * @param _format The rendering format
     * 
     * @return GUID the unique identifier for the new session
     * 
     * TODO document exceptions
     * TODO don't throw so many exceptions
     * 
     * @throws IOException 
     * @throws SOAPException 
     * @throws MalformedURLException 
     * @throws NoSuchProcessorException 
     * @throws NoAttachmentException 
     * @throws InitialisationException 
     * @throws NoSuchPropertyException 
     * @throws IOException 
     * @throws SOAPException 
     * @throws MalformedURLException 
     * @throws RoutingException 
     */
    public String newContentPackageSession(String _xmlName, String _format, int _renderOpts) throws NoSuchProcessorException, NoAttachmentException, InitialisationException, NoSuchPropertyException, MalformedURLException, SOAPException, IOException, RoutingException {
        try {
            Message request = MessageContext.getCurrentContext().getRequestMessage();
            if (request.countAttachments() == 0) {
                throw new NoAttachmentException("No supplied content package");
            }
            Iterator attachmentIter = request.getAttachments();
            AttachmentPart ap = (AttachmentPart) attachmentIter.next();
            String guid = ipc.newProcessor(_format, _renderOpts);
            String encodedXmlName = unpack(ap, guid, _xmlName);
            byte[] xml = StreamCopier.copyToByteArray(new FileInputStream(new File(encodedXmlName)));
            ipc.initialiseProcessor(guid, xml);
            return guid;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new RoutingException("Error creating new session", e);
        }
    }

    public String newItemSession(String _xmlName, String _format, int _renderOpts, String _itemPath) throws NoSuchProcessorException, NoAttachmentException, InitialisationException, NoSuchPropertyException, MalformedURLException, SOAPException, IOException, RoutingException {
        try {
            Message request = MessageContext.getCurrentContext().getRequestMessage();
            if (request.countAttachments() == 0) {
                throw new NoAttachmentException("No supplied content package");
            }
            Iterator attachmentIter = request.getAttachments();
            AttachmentPart ap = (AttachmentPart) attachmentIter.next();
            String guid = ipc.newProcessor(_format, _renderOpts);
            byte[] xml = StreamCopier.copyToByteArray(ap.getDataHandler().getInputStream());
            xml = processItemXML(xml, _itemPath);
            ipc.initialiseProcessor(guid, xml);
            return guid;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new RoutingException("Error creating new session", e);
        }
    }

    /**
     * TODO javadoc
     * 
     * @param _guid
     * @return 
     * @throws NoSuchProcessorException
     * @throws MalformedURLException
     * @throws IOException
     * @throws NoSuchPropertyException
     * @throws NoAttachmentException
     * @throws SOAPException
     * @throws SpecificationViolationException 
     * @throws RenderingException 
     */
    public HashMap firstStage(String _guid) throws NoSuchProcessorException, MalformedURLException, IOException, NoSuchPropertyException, NoAttachmentException, SOAPException, RenderingException, SpecificationViolationException {
        try {
            RenderingResponse resp = ipc.firstStage(_guid);
            MessageContext msgContext = MessageContext.getCurrentContext();
            Message msgResponse = msgContext.getResponseMessage();
            AttachmentBuilder.addByteAttachment(msgResponse, resp.file, "question.xml");
            return resp.getOutcomeDeclaration();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calls the InteractionProcessor to process any response and render the
     * next stage for (potential) further interaction
     * 
     * TODO should probably be attaching the byte array here, not returning
     * 
     * @param _guid GUID of the InteractionProcessor
     * @return byte array containing the rendered output
     * @throws NoSuchProcessorException if an invalid GUID is supplied
     * @throws SOAPException 
     * @throws NoAttachmentException 
     * @throws NoSuchPropertyException 
     * @throws IOException 
     * @throws MalformedURLException 
     * @throws ProcessingException 
     * @throws InvalidUserResponseException 
     * @throws SpecificationViolationException 
     * @throws RenderingException 
     */
    public HashMap[] nextStage(String _guid, HashMap _response) throws NoSuchProcessorException, MalformedURLException, IOException, NoSuchPropertyException, NoAttachmentException, SOAPException, RenderingException, SpecificationViolationException, InvalidUserResponseException, ProcessingException {
        RenderingResponse resp = ipc.nextStage(_guid, _response);
        MessageContext msgContext = MessageContext.getCurrentContext();
        Message msgResponse = msgContext.getResponseMessage();
        AttachmentBuilder.addByteAttachment(msgResponse, resp.file, "question.xml");
        HashMap response = resp.getResponseDeclation();
        HashMap outcome = resp.getOutcomeDeclaration();
        Iterator i = response.keySet().iterator();
        HashMap[] out = new HashMap[2];
        out[0] = response;
        out[1] = outcome;
        return out;
    }

    /**
     * TODO javadoc
     * 
     * @param _guid
     * @throws NoSuchProcessorException
     * @throws MalformedURLException
     * @throws IOException
     * @throws NoSuchPropertyException
     * @throws NoAttachmentException
     * @throws SOAPException
     * @throws NoPreviousStageException 
     * @throws SpecificationViolationException 
     * @throws RenderingException 
     */
    public HashMap previousStage(String _guid) throws NoSuchProcessorException, MalformedURLException, IOException, NoSuchPropertyException, NoAttachmentException, SOAPException, NoPreviousStageException, RenderingException, SpecificationViolationException {
        RenderingResponse resp = ipc.previousStage(_guid);
        MessageContext msgContext = MessageContext.getCurrentContext();
        Message msgResponse = msgContext.getResponseMessage();
        AttachmentBuilder.addByteAttachment(msgResponse, resp.file, "question.xml");
        return resp.getOutcomeDeclaration();
    }

    public void terminateSession(String _guid) throws NoSuchProcessorException {
        ipc.terminateSession(_guid);
    }

    /**
     * Calls the unpacking service
     * 
     * 
     * @param _ap The AttachmentPart holding the content package
     * @param _guid The GUID of this user interaction
     * @param _xmlName 
     * @return The path to the newly-unpacked XML file
     * TODO document exceptions
     * @throws NoAttachmentException 
     * @throws RoutingException 
     */
    private String unpack(AttachmentPart _ap, String _guid, String _xmlName) throws SOAPException, MalformedURLException, IOException, NoAttachmentException, RoutingException {
        DataHandler dh = _ap.getDataHandler();
        Service loc = new ContentUnpackerServiceLocator();
        ContentUnpackerSoapBindingStub stub = new ContentUnpackerSoapBindingStub(new URL(contentUnpackerWebServiceAddress), loc);
        stub.addAttachment(dh);
        EncodedFileList filelist = stub.unpack(_xmlName, httpBasePath + _guid + "/");
        if (filelist == null) throw new RoutingException("Content unpacker response empty!");
        Object[] attachments = stub.getAttachments();
        return processUnpackerResponse(filelist, attachments, _guid);
    }

    private byte[] processItemXML(byte[] _xml, String _itemPath) throws MalformedURLException, RemoteException {
        Service loc = new ContentUnpackerServiceLocator();
        ContentUnpackerSoapBindingStub stub = new ContentUnpackerSoapBindingStub(new URL(contentUnpackerWebServiceAddress), loc);
        return stub.processQTIItem(_xml, _itemPath);
    }

    /**
     * TODO javadoc
     * 
     * @param _guid
     * @param _xml
     * @param _xmlName
     * @return
     * @throws IOException
     * @throws SOAPException
     * @throws NoAttachmentException
     * @throws RoutingException 
     */
    private String unpack(String _guid, String _xml, String _xmlName) throws IOException, SOAPException, NoAttachmentException, RoutingException, NoSuchPropertyException {
        Service loc = new ContentUnpackerServiceLocator();
        ContentUnpackerSoapBindingStub stub = new ContentUnpackerSoapBindingStub(new URL(contentUnpackerWebServiceAddress), loc);
        EncodedFileList filelist = stub.collateFiles(_xml, _xmlName, httpBasePath);
        if (filelist == null) throw new RoutingException("Content unpacker response empty!");
        Object[] attachments = stub.getAttachments();
        return processUnpackerResponse(filelist, attachments, _guid);
    }

    /**
     * TODO javadoc
     * 
     * @param _filelist
     * @param _attachments
     * @return
     * @throws IOException
     * @throws SOAPException
     * @throws NoAttachmentException
     * @throws RoutingException 
     */
    private String processUnpackerResponse(EncodedFileList _filelist, Object[] _attachments, String _guid) throws IOException, SOAPException, NoAttachmentException, RoutingException {
        if (_filelist == null) throw new RoutingException("Content unpacker response empty!");
        String[] filenames = _filelist.getFilenames();
        if (filenames == null || filenames.length == 0) return null;
        ;
        if (_attachments == null || _attachments.length == 0) throw new NoAttachmentException("No attachments returned from content unpacker");
        String path = localBasePath + _guid + File.separator;
        File dir = new File(path);
        dir.mkdirs();
        for (int i = 0, j = 0; i < filenames.length; i++) {
            File file = new File(path + filenames[i]);
            if (filenames[i].endsWith("\\") || filenames[i].endsWith("/")) {
                file.mkdir();
            } else {
                try {
                    AttachmentPart responseAttachment = (AttachmentPart) _attachments[j++];
                    InputStream is = responseAttachment.getDataHandler().getInputStream();
                    OutputStream os = new FileOutputStream(file);
                    StreamCopier.copy(is, os);
                    is.close();
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return path + _filelist.getXmlName();
    }
}
