package net.winstone.filters.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This class is used to encapsulate the decoding of HTTP POST requests using
 * the "multipart/form-data" encoding type. <br/>
 * <br/>
 * It uses Javamail for Mime libraries and the JavaBeans Activation Framework
 * (JAF), so make sure you have activation.jar and mail.jar in the class path
 * before using this class. <br/>
 * <br/>
 * Note: The servlet input stream is empty after the contructor executes. This
 * prevents the use of this class on the same request twice.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: MultipartRequestWrapper.java,v 1.3 2005/09/08 02:42:02
 *          rickknowles Exp $
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    public static final String MPH_ATTRIBUTE = "MultipartRequestWrapper.reference";

    private Map<String, String[]> stringParameters;

    private Map<String, File> tempFileNames;

    private Map<String, String> mimeTypes;

    private Map<String, String> uploadFileNames;

    /**
	 * Constructor - this uses a servlet request, validating it to make sure
	 * it's a multipart/form-data request, then reads the ServletInputStream,
	 * storing the results after Mime decoding in a member array. Use
	 * getParameter etc to retrieve the contents.
	 * 
	 * @param request
	 *            The Servlet's request object.
	 */
    public MultipartRequestWrapper(final ServletRequest request) throws IOException {
        super((HttpServletRequest) request);
        final String contentType = request.getContentType();
        if (!contentType.toLowerCase().startsWith("multipart/form-data")) {
            throw new IOException("The MIME Content-Type of the Request must be " + '"' + "multipart/form-data" + '"' + ", not " + '"' + contentType + '"' + ".");
        } else if (request.getAttribute(MultipartRequestWrapper.MPH_ATTRIBUTE) != null) {
            final MultipartRequestWrapper oldMPH = (MultipartRequestWrapper) request.getAttribute(MultipartRequestWrapper.MPH_ATTRIBUTE);
            stringParameters = oldMPH.stringParameters;
            mimeTypes = oldMPH.mimeTypes;
            tempFileNames = oldMPH.tempFileNames;
            uploadFileNames = oldMPH.uploadFileNames;
            return;
        }
        try {
            final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            final InputStream inputServlet = request.getInputStream();
            final byte buffer[] = new byte[2048];
            int readBytes = inputServlet.read(buffer);
            while (readBytes != -1) {
                byteArray.write(buffer, 0, readBytes);
                readBytes = inputServlet.read(buffer);
            }
            inputServlet.close();
            MimeMultipart parts = new MimeMultipart(new MultipartRequestWrapperDataSource(contentType, byteArray.toByteArray()));
            byteArray.close();
            final Map<String, String[]> parameters = new HashMap<String, String[]>();
            final Map<String, String> mimes = new HashMap<String, String>();
            final Map<String, File> fileNames = new HashMap<String, File>();
            final Map<String, String> uploadNames = new HashMap<String, String>();
            String encoding = request.getCharacterEncoding();
            if (encoding == null) {
                encoding = "8859_1";
            }
            for (int loopCount = 0; loopCount < parts.getCount(); loopCount++) {
                final MimeBodyPart current = (MimeBodyPart) parts.getBodyPart(loopCount);
                final String headers = current.getHeader("Content-Disposition", "; ");
                if (headers.indexOf(" name=" + '"') == -1) {
                    throw new MessagingException("No name header found in " + "Content-Disposition field.");
                } else {
                    String namePart = headers.substring(headers.indexOf(" name=\"") + 7);
                    namePart = namePart.substring(0, namePart.indexOf('"'));
                    final String nameField = javax.mail.internet.MimeUtility.decodeText(namePart);
                    final InputStream inRaw = current.getInputStream();
                    if (headers.indexOf(" filename=" + '"') != -1) {
                        String fileName = headers.substring(headers.indexOf(" filename=" + '"') + 11);
                        fileName = fileName.substring(0, fileName.indexOf('"'));
                        if (fileName.lastIndexOf('/') != -1) {
                            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                        }
                        if (fileName.lastIndexOf('\\') != -1) {
                            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
                        }
                        uploadNames.put(nameField, fileName);
                        if (fileNames.containsKey(nameField)) {
                            throw new IOException("Multiple parameters named " + nameField);
                        }
                        if (current.getContentType() == null) {
                            mimes.put(nameField, "text/plain");
                        } else {
                            mimes.put(nameField, current.getContentType());
                        }
                        final File tempFile = File.createTempFile("mph", ".tmp");
                        final OutputStream outStream = new FileOutputStream(tempFile, Boolean.TRUE);
                        while ((readBytes = inRaw.read(buffer)) != -1) {
                            outStream.write(buffer, 0, readBytes);
                        }
                        inRaw.close();
                        outStream.close();
                        fileNames.put(nameField, tempFile.getAbsoluteFile());
                    } else {
                        final byte[] stash = new byte[inRaw.available()];
                        inRaw.read(stash);
                        inRaw.close();
                        final Object oldParam = parameters.get(nameField);
                        if (oldParam == null) {
                            parameters.put(nameField, new String[] { new String(stash, encoding) });
                        } else {
                            final String oldParams[] = (String[]) oldParam;
                            final String newParams[] = new String[oldParams.length + 1];
                            System.arraycopy(oldParams, 0, newParams, 0, oldParams.length);
                            newParams[oldParams.length] = new String(stash, encoding);
                            parameters.put(nameField, newParams);
                        }
                    }
                }
            }
            parts = null;
            stringParameters = Collections.unmodifiableMap(parameters);
            mimeTypes = Collections.unmodifiableMap(mimes);
            tempFileNames = Collections.unmodifiableMap(fileNames);
            uploadFileNames = Collections.unmodifiableMap(uploadNames);
            request.setAttribute(MultipartRequestWrapper.MPH_ATTRIBUTE, this);
        } catch (final MessagingException errMime) {
            throw new IOException(errMime.toString());
        }
    }

    @Override
    public String getParameter(final String name) {
        final String parameterValues[] = getParameterValues(name);
        if ((parameterValues == null) || (parameterValues.length == 0)) {
            return null;
        } else {
            return parameterValues[0];
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        final Map<String, String[]> paramMap = new HashMap<String, String[]>();
        for (final Enumeration<String> names = getParameterNames(); names.hasMoreElements(); ) {
            final String name = names.nextElement();
            paramMap.put(name, getParameterValues(name));
        }
        return Collections.unmodifiableMap(paramMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<String> getParameterNames() {
        final Set<String> names = new HashSet<String>(stringParameters.keySet());
        names.addAll(tempFileNames.keySet());
        final Enumeration<String> parent = super.getParameterNames();
        names.addAll(Collections.list(parent));
        return Collections.enumeration(names);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new IOException("InputStream already parsed by the MultipartRequestWrapper class");
    }

    @Override
    public String[] getParameterValues(final String name) {
        final String parentValue[] = super.getParameterValues(name);
        if (parentValue != null) {
            return parentValue;
        } else if (name == null) {
            return null;
        } else if (name.endsWith(".filename") && isFileUploadParameter(name.substring(0, name.length() - 9))) {
            return new String[] { getUploadFileName(name.substring(0, name.length() - 9)) };
        } else if (name.endsWith(".content-type") && isFileUploadParameter(name.substring(0, name.length() - 13))) {
            return new String[] { getContentType(name.substring(0, name.length() - 13)) };
        } else if (isNonFileUploadParameter(name)) {
            return stringParameters.get(name);
        } else if (isFileUploadParameter(name)) {
            return new String[] { tempFileNames.get(name).getAbsolutePath() };
        } else {
            return null;
        }
    }

    /**
	 * The byte array version of the parameter requested (as an Object). This
	 * always returns a byte array, ignoring the mime type of the submitted
	 * object.
	 * 
	 * @param name
	 *            The parameter you wish to retrieve.
	 * @return A byte array representation of the supplied parameter.
	 */
    public byte[] getRawParameter(final String name) throws IOException {
        if (name == null) {
            return null;
        }
        final File tempFile = tempFileNames.get(name);
        if (tempFile == null) {
            return null;
        }
        final InputStream inFile = new FileInputStream(tempFile);
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final byte buffer[] = new byte[2048];
        int readBytes = inFile.read(buffer);
        while (readBytes != -1) {
            byteArray.write(buffer, 0, readBytes);
            readBytes = inFile.read(buffer);
        }
        inFile.close();
        final byte output[] = byteArray.toByteArray();
        byteArray.close();
        return output;
    }

    /**
	 * Get the MimeType of a particular parameter.
	 * 
	 * @param name
	 *            The parameter you wish to find the Mime type of.
	 * @return The Mime type for the requested parameter (as specified in the
	 *         Mime header during the post).
	 */
    public String getContentType(final String name) {
        return mimeTypes.get(name);
    }

    /**
	 * The local (client) name of the file submitted if this parameter was a
	 * file.
	 * 
	 * @param name
	 *            The parameter you wish to find the file name for.
	 * @return The local name for the requested parameter (as specified in the
	 *         Mime header during the post).
	 */
    public String getUploadFileName(final String name) {
        return uploadFileNames.get(name);
    }

    public boolean isFileUploadParameter(final String name) {
        return tempFileNames.containsKey(name);
    }

    public boolean isNonFileUploadParameter(final String name) {
        return stringParameters.containsKey(name);
    }

    /**
	 * Retrieve a Map of the raw bytes of the parameters supplied in the HTTP
	 * POST request.
	 */
    public Map<String, byte[]> getRawParameterMap() throws IOException {
        final Map<String, byte[]> output = new HashMap<String, byte[]>();
        for (final Iterator<String> i = uploadFileNames.keySet().iterator(); i.hasNext(); ) {
            final String key = i.next();
            output.put(key, getRawParameter(key));
        }
        return output;
    }

    /**
	 * Retrieve a Map of the filenames supplied in the HTTP POST request.
	 */
    public Map<String, String> getContentTypeMap() {
        return mimeTypes;
    }

    /**
	 * Retrieve a Map of the filenames supplied in the HTTP POST request.
	 */
    public Map<String, String> getUploadFileNameMap() {
        return uploadFileNames;
    }

    private class MultipartRequestWrapperDataSource implements DataSource {

        private final byte mimeByteArray[];

        private final String contentType;

        private MultipartRequestWrapperDataSource(final String contentType, final byte mimeByteArray[]) {
            this.mimeByteArray = mimeByteArray;
            this.contentType = contentType;
        }

        /**
		 * Required for implementation of the DataSource interface. Internal use
		 * only.
		 */
        @Override
        public String getName() {
            return "MultipartHandler";
        }

        /**
		 * Required for implementation of the DataSource interface. Internal use
		 * only.
		 */
        @Override
        public String getContentType() {
            return contentType;
        }

        /**
		 * Required for implementation of the DataSource interface. Internal use
		 * only.
		 */
        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return new ByteArrayInputStream(mimeByteArray);
        }

        /**
		 * Required for implementation of the DataSource interface. Internal use
		 * only.
		 */
        @Override
        public java.io.OutputStream getOutputStream() throws java.io.IOException {
            throw new IOException("This is a read-only datasource.");
        }
    }
}
