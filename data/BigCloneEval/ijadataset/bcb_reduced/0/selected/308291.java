package org.t2framework.t2.spi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.t2framework.commons.exception.IORuntimeException;
import org.t2framework.commons.util.Logger;
import org.t2framework.t2.contexts.Multipart;
import org.t2framework.t2.contexts.Request;
import org.t2framework.t2.contexts.UploadFile;
import org.t2framework.t2.contexts.impl.MultipartImpl;
import org.t2framework.t2.contexts.impl.StreamUploadFileImpl;
import org.t2framework.t2.contexts.impl.UploadFileImpl;
import org.t2framework.t2.filter.MultiPartRequestFilter;
import org.t2framework.t2.spi.MultipartRequestHandler;

/**
 * <#if locale="en">
 * <p>
 * 
 * This class depends on Apache commons fileupload.
 * 
 * </p>
 * <#else>
 * <p>
 * 
 * </p>
 * </#if>
 * 
 * @author shot
 * @see org.apache.commons.fileupload.FileItem
 * @see org.apache.commons.fileupload.FileItemStream
 * @see org.apache.commons.fileupload.FileUploadException
 * @see org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException
 * @see org.apache.commons.fileupload.FileUploadBase.IOFileUploadException
 * @see org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException
 * @see org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException
 */
public abstract class AbstractMultipartRequestHandler implements MultipartRequestHandler {

    private static final Logger log = Logger.getLogger(AbstractMultipartRequestHandler.class);

    protected void logUploadError(final FileUploadException e, final HttpServletRequest request) {
        if (e instanceof InvalidContentTypeException) {
            log.log("WTDT0007", new Object[] { e, request.getContentType() });
        } else if (e instanceof IOFileUploadException) {
            log.log("WTDT0008", e, new Object[] { e.getMessage() });
        } else if (e instanceof FileSizeLimitExceededException) {
            log.log("WTDT0009", e, new Object[] { MultiPartRequestFilter.maxSize });
        } else if (e instanceof SizeLimitExceededException) {
            log.log("WTDT0010", e, new Object[] { request.getContentLength(), MultiPartRequestFilter.maxSize });
        } else {
            log.log("WTDT0011", e, new Object[] { e.toString() });
        }
    }

    protected void storeMultipart(HttpServletRequest request, Multipart multiPart) {
        request.setAttribute(Request.MULTIPART_ATTRIBUTE_KEY, multiPart);
    }

    protected Multipart getMultipart(HttpServletRequest request) {
        return (Multipart) request.getAttribute(Request.MULTIPART_ATTRIBUTE_KEY);
    }

    protected Multipart createMultipart() {
        return new MultipartImpl();
    }

    protected UploadFile createUploadFile(InputStream is, String contentType, String name) {
        try {
            return new StreamUploadFileImpl(is, contentType, name);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    protected UploadFile createUploadFile(FileItem fileItem) {
        return new UploadFileImpl(fileItem);
    }

    @SuppressWarnings("unchecked")
    protected <T> T[] add(T[] objs, T value, Class<T> clazz) {
        T[] newObjs;
        if (objs == null) {
            newObjs = (T[]) Array.newInstance(clazz, 1);
            newObjs[0] = value;
        } else {
            newObjs = (T[]) Array.newInstance(objs.getClass().getComponentType(), objs.length + 1);
            System.arraycopy(objs, 0, newObjs, 0, objs.length);
            newObjs[objs.length] = value;
        }
        return newObjs;
    }

    protected Object getFirst(Object obj) {
        Object[] objs = (Object[]) obj;
        if (objs == null || objs.length == 0) {
            return null;
        } else {
            return objs[0];
        }
    }

    @Override
    public void close(HttpServletRequest request) {
        final Multipart multipart = getMultipart(request);
        if (multipart != null) {
            for (UploadFile file : multipart.getUploadList()) {
                file.close();
            }
        }
    }
}
