package uk.gov.dti.og.fox.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import uk.gov.dti.og.fox.App;
import uk.gov.dti.og.fox.FoxRequest;
import uk.gov.dti.og.fox.FoxResponse;
import uk.gov.dti.og.fox.FoxResponseByteStream;
import uk.gov.dti.og.fox.UCon;
import uk.gov.dti.og.fox.XFUtil;
import uk.gov.dti.og.fox.XThread;
import uk.gov.dti.og.fox.ex.ExInternal;
import uk.gov.dti.og.fox.ex.ExModule;
import uk.gov.dti.og.fox.ex.ExServiceUnavailable;
import uk.gov.dti.og.fox.ex.ExUserRequest;
import uk.gov.dti.og.fox.track.Track;
import uk.gov.dti.og.fox.util.ObjectCache;
import uk.gov.dti.og.fox.util.TimeToLiveCachePolicy;
import uk.gov.dti.og.fox.io.StreamParcelInputCLOB;
import uk.gov.dti.og.fox.xfsession;

public class StreamParcel extends Track {

    public static final String TYPE_ZIP_FILE = "ZIP-FILE";

    public static final String TYPE_STANDARD = "STANDARD";

    private static final Map gStreamParcelMap;

    static {
        ObjectCache lObjectCache = new ObjectCache();
        TimeToLiveCachePolicy timePolicy = new TimeToLiveCachePolicy();
        timePolicy.setItemTimeToLiveMillis(1000 * 60 * 5);
        lObjectCache.addCachingPolicy(timePolicy);
        gStreamParcelMap = lObjectCache;
    }

    private String mStreamParcelID = XFUtil.unique();

    private String mType;

    private String mFileName = null;

    private UCon mConnection = null;

    private List mStreamParcelInputList = new ArrayList();

    private int mZipCompressionLevel = Deflater.DEFAULT_COMPRESSION;

    private App mApp;

    private String mSessionCookie = null;

    /** 
   * Get the StreamParcel Object for a given reference ID
   * 
   * @param pStreamParcelID  The ID of the StreamParcel required
   * @return                 The StreamParcel Object with the given ID
   */
    public static StreamParcel getStreamParcelFromID(String pStreamParcelID) {
        StreamParcel lStreamParcel = (StreamParcel) gStreamParcelMap.get(pStreamParcelID);
        if (lStreamParcel == null) {
            throw new ExInternal("This stream parcel no longer exists. Links are valid for 5 minutes. StreamParcelID = " + pStreamParcelID);
        }
        return lStreamParcel;
    }

    /** 
   * Constructor to create a new StreamParcel and assign and register a new id
   * in the StreamParcelMap.
   * 
   * @param pType         Type of StreamParcel Required e.g. ZIP-FILE
   * @param pFileName     Name of file returned
   * * @param pConnection   Connection used for data query
   */
    public StreamParcel(String pType, String pFileName, App pApp, UCon pConnection, boolean pCache, String pSessionCookie) {
        mSessionCookie = pSessionCookie;
        if (pType == null) {
            throw new ExInternal("New StreamParcel requires a Type and null was passed.");
        }
        if (pType == TYPE_ZIP_FILE || pType == TYPE_STANDARD) {
            mType = pType;
        } else {
            throw new ExInternal("New StreamParcel requires a type. Currently only " + TYPE_ZIP_FILE + " or " + TYPE_STANDARD + " are accepted.");
        }
        if (pConnection != null) {
            mConnection = pConnection;
            mConnection.registerInterest(this);
        }
        mFileName = pFileName;
        mApp = pApp;
        if (pCache) {
            synchronized (gStreamParcelMap) {
                gStreamParcelMap.put(mStreamParcelID, this);
            }
        }
    }

    public StreamParcel(String pType, String pFileName, App pApp, UCon pConnection, String pSessionCookie) {
        this(pType, pFileName, pApp, pConnection, true, pSessionCookie);
    }

    /**
   * Constructor to create a new StreamParcel and assign and register a new id
   * in the StreamParcelMap.
   * 
   * @param pType         Type of StreamParcel Required e.g. PDF-DOCUMENT
   * @param pFileName     Name of file returned
   */
    public StreamParcel(String pType, String pFileName, App pApp, String pSessionCookie) {
        this(pType, pFileName, pApp, null, true, pSessionCookie);
    }

    public StreamParcel(String pType, String pFileName, App pApp, boolean pCache, String pSessionCookie) {
        this(pType, pFileName, pApp, null, pCache, pSessionCookie);
    }

    /** 
   * Add a new StreamParcelInput to the StreamParcel
   * 
   * @param pStreamParcelInput  The StreamParcelInput to be added
   */
    public void addStreamParcelInput(StreamParcelInput pStreamParcelInput, XThread pXThread) {
        xfsession.getCachedFoxSession(pXThread.getXfsessionId()).setDownloadAttempt(true);
        if (pStreamParcelInput != null) {
            synchronized (mStreamParcelInputList) {
                mStreamParcelInputList.add(pStreamParcelInput);
            }
        } else {
            throw new ExInternal("addStreamParcelInput requires a StreamParcelInput and null was passed.");
        }
    }

    /** 
   * Process the response for this StreamParcel returning the output stream to 
   * the browser
   * 
   * @param pFoxRequest   The current FoxRequest
   * @return              The FoxResponse
   */
    public FoxResponse processResponse(FoxRequest pFoxRequest) {
        if (pFoxRequest.getCookieValue("streamsession") == null || !pFoxRequest.getCookieValue("streamsession").equals(mSessionCookie)) {
            throw new ExInternal("Cannot serve links to users who did not generate the content");
        }
        FoxResponseByteStream lFoxResponse = null;
        int lFileIndex = 0;
        String lMode = XFUtil.nvl(pFoxRequest.getHttpRequest().getParameter("mode"), "save");
        boolean lIsAttachment = false;
        String lAttach = pFoxRequest.getHttpRequest().getParameter("attach");
        if (!XFUtil.isNull(lAttach) && lAttach.equals("true")) {
            lIsAttachment = true;
        }
        if (mType == TYPE_STANDARD) {
            if (mStreamParcelInputList.size() == 0) {
                throw new ExInternal("There are no documents in this stream parcel");
            } else if (mStreamParcelInputList.size() > 1) {
                String lfileIndexParam = pFoxRequest.getHttpRequest().getParameter("i");
                if (XFUtil.isNull(lfileIndexParam)) {
                    throw new ExInternal("This stream parcel has multiple documents and no file id passed in url");
                }
                try {
                    lFileIndex = Integer.parseInt(lfileIndexParam) - 1;
                } catch (NumberFormatException e) {
                    throw new ExInternal("The file id requested is not valid id.", e);
                }
            }
            StreamParcelInput lInputStreamFile = (StreamParcelInput) mStreamParcelInputList.get(lFileIndex);
            UCon lUcon = null;
            if (lInputStreamFile instanceof StreamParcelInputTempResource || lInputStreamFile instanceof StreamParcelInputFileUpload) {
                try {
                    lUcon = UCon.open(mApp.mConnectKey, "StreamParcel.processResponse");
                    lUcon.setClientInfo("StreamParcel");
                } catch (ExServiceUnavailable e) {
                    throw new ExInternal("Error getting database connection for StreamParcel response", e);
                }
            }
            XThread lXThread = null;
            if (lInputStreamFile instanceof StreamParcelInputFileUpload) {
                String lThreadID = pFoxRequest.getHttpRequest().getParameter("t");
                String lAppMnem = pFoxRequest.getHttpRequest().getParameter("a");
                try {
                    lXThread = XThread.reloadThread(lAppMnem, lThreadID, pFoxRequest);
                } catch (ExModule e) {
                    throw new ExInternal("Problem starting thread for !STREAM request", e);
                } catch (ExServiceUnavailable e) {
                    throw new ExInternal("Problem starting thread for !STREAM request", e);
                }
                try {
                    lXThread.alterThreadSetLevel(pFoxRequest, XThread.THREAD_MOUNTED, lUcon);
                } catch (Exception e) {
                    throw new ExInternal("Error mounting thread for StreamParcel", e);
                }
            }
            if (lInputStreamFile instanceof StreamParcelInputTempResource) {
                StreamParcelInputTempResource lInputStreamFileTempResource = (StreamParcelInputTempResource) lInputStreamFile;
                lInputStreamFileTempResource.setParams(lUcon, mApp);
            } else if (lInputStreamFile instanceof StreamParcelInputFileUpload) {
                StreamParcelInputFileUpload lInputStreamFileUpload = (StreamParcelInputFileUpload) lInputStreamFile;
                lInputStreamFileUpload.setParams(lXThread);
            }
            lFoxResponse = new FoxResponseByteStream(lInputStreamFile.getFileType(), pFoxRequest, 0);
            OutputStream lFileOutputStream = null;
            try {
                if (lMode.equals("save")) {
                    lFoxResponse.setHttpHeader("Content-Disposition", "attachment; filename=\"" + lInputStreamFile.getFileName() + "\"");
                } else {
                    lFoxResponse.setHttpHeader("Content-Disposition", "filename=\"" + lInputStreamFile.getFileName() + "\"");
                }
                String lContentType = lInputStreamFile.getFileType();
                if (!XFUtil.isNull(lContentType)) {
                    lFoxResponse.setHttpHeader("Content-Type", lContentType);
                }
                long lContentLength = lInputStreamFile.getSize();
                if (lContentLength > 0) {
                    lFoxResponse.setHttpHeader("Content-Length", Long.toString(lContentLength));
                }
                lFileOutputStream = lFoxResponse.getHttpServletOutputStream();
                streamDataFromStreamParcelInput(lInputStreamFile, lFileOutputStream, lXThread, lUcon, pFoxRequest);
            } catch (Throwable th) {
                if (lXThread != null) {
                    lXThread.abort(pFoxRequest);
                }
            } finally {
                try {
                    if (lFileOutputStream != null) {
                        lFileOutputStream.close();
                    }
                } catch (Throwable th) {
                }
                if (lUcon != null) {
                    try {
                        lUcon.rollback();
                    } catch (Throwable th) {
                        int i = 1;
                    }
                    lUcon.closeForRecycle();
                }
            }
        } else if (mType == TYPE_ZIP_FILE) {
            HashMap lFileNameList = new HashMap();
            lFoxResponse = new FoxResponseByteStream("application/zip", pFoxRequest, 0);
            lFoxResponse.setHttpHeader("Content-Disposition", "attachment; filename=\"" + mFileName + "\"");
            try {
                ZipOutputStream lZipOutputStream = new ZipOutputStream(lFoxResponse.getHttpServletOutputStream());
                lZipOutputStream.setLevel(mZipCompressionLevel);
                int lBytesCopied = 0;
                for (int i = 0; i < mStreamParcelInputList.size(); i++) {
                    StreamParcelInput lInputStreamFile = (StreamParcelInput) mStreamParcelInputList.get(i);
                    String lPathFile = lInputStreamFile.getPath() + lInputStreamFile.getFileName();
                    Integer lcount = (Integer) lFileNameList.get(lPathFile);
                    if (lcount == null) {
                        lFileNameList.put(lPathFile, new Integer(1));
                    } else {
                        lFileNameList.put(lPathFile, new Integer(lcount.intValue() + 1));
                        int lExtIndex = lPathFile.lastIndexOf(".");
                        String lExt = "";
                        String lFileName = "";
                        if (lExtIndex >= 0) {
                            lExt = lPathFile.substring(lExtIndex);
                            lFileName = lPathFile.substring(0, lExtIndex);
                        } else {
                            lFileName = lPathFile;
                        }
                        lPathFile = lFileName + "(" + new Integer(lcount.intValue() + 1) + ")" + lExt;
                    }
                    try {
                        ZipEntry lZipFileEntry = new ZipEntry(lPathFile);
                        lZipOutputStream.putNextEntry(lZipFileEntry);
                        streamDataFromStreamParcelInput(lInputStreamFile, lZipOutputStream, null, null, pFoxRequest);
                        lZipOutputStream.closeEntry();
                    } catch (IOException e) {
                        throw new ExInternal("Error writing ZIP file entry from StreamParcelInput", e);
                    } catch (Exception e) {
                        throw new ExInternal("Error writing ZIP file entry from StreamParcelInput", e);
                    }
                }
                lZipOutputStream.close();
            } catch (IOException e) {
                throw new ExInternal("Error closing ZIP", e);
            }
        }
        return lFoxResponse;
    }

    /**
   * Set the default Zip Compression level. If the compresssion level passed 
   * is 0 then the default compression level is used.
   * 
   * @param pZipCompressionLevel   The zip compression level
   */
    public void setZipCompression(Integer pZipCompressionLevel) {
        if (mType != TYPE_ZIP_FILE) {
            throw new ExInternal("Zip compression level can only be set for stream parcels of type ZIP-FILE");
        }
        if (pZipCompressionLevel == null || (pZipCompressionLevel.intValue() >= -1 && pZipCompressionLevel.intValue() <= 9)) {
            mZipCompressionLevel = pZipCompressionLevel != null ? pZipCompressionLevel.intValue() : Deflater.DEFAULT_COMPRESSION;
        } else {
            throw new ExInternal("Invalid zip compression level set. Valid values are 0-9 or -1 for default. Value passed=\"" + pZipCompressionLevel + "\"");
        }
    }

    /** 
   * Get the StreamParcel's ID
   * 
   * @return   The StreamParcel ID
   */
    public String getStreamParcelID() {
        return mStreamParcelID;
    }

    /**
   * Get the StreamParcel's File Name
   * 
   * @return The StreamParcel's File Name
   */
    public String getFileName() {
        return mFileName;
    }

    public String toString() {
        return "StreamParcel: (" + mStreamParcelID + ") " + mType + " " + mFileName;
    }

    public String getStreamURL(FoxRequest pFoxRequest) {
        return pFoxRequest.getURLContextServletPath() + "/!STREAM?id=" + mStreamParcelID;
    }

    public int getInputCount() {
        synchronized (mStreamParcelInputList) {
            return mStreamParcelInputList.size();
        }
    }

    /**
   * When we have finished with this stream parcel we need to unregister our
   * interest in the Ucon to release back to the pool
   * 
   * @throws Throwable
   */
    protected void finalizeObject() throws Throwable {
        mConnection.unRegisterInterest(this);
    }

    private void streamDataFromStreamParcelInput(StreamParcelInput pInputStreamFile, OutputStream pFileOutputStream, XThread pXThread, UCon pUCon, FoxRequest pFoxRequest) throws ExServiceUnavailable, ExUserRequest, ExModule {
        if (pInputStreamFile instanceof StreamParcelInputCLOB) {
            StreamParcelInputCLOB lStreamParcelInputCLOB = (StreamParcelInputCLOB) pInputStreamFile;
            try {
                InputStream lInputStream = lStreamParcelInputCLOB.getInputStream();
                IOUtil.transfer(lInputStream, pFileOutputStream, 64000);
                lInputStream.close();
            } catch (IOException e) {
                throw new ExInternal("Error streaming CLOB", e);
            }
        } else if (pInputStreamFile instanceof StreamParcelInputBLOB) {
            StreamParcelInputBLOB lStreamParcelInputBLOB = (StreamParcelInputBLOB) pInputStreamFile;
            try {
                InputStream lInputStream = lStreamParcelInputBLOB.getInputStream();
                IOUtil.transfer(lInputStream, pFileOutputStream, 64000);
                lInputStream.close();
            } catch (IOException e) {
                throw new ExInternal("Error streaming BLOB", e);
            }
        } else if (pInputStreamFile instanceof StreamParcelInputTempResource) {
            StreamParcelInputTempResource lStreamParcelInputTempResource = (StreamParcelInputTempResource) pInputStreamFile;
            try {
                Object lDataStreamObject = lStreamParcelInputTempResource.getDataStreamObject();
                if (lDataStreamObject instanceof InputStream) {
                    InputStream lInputStream = (InputStream) lDataStreamObject;
                    IOUtil.transfer(lInputStream, pFileOutputStream, 64000);
                    lInputStream.close();
                } else if (lDataStreamObject instanceof Reader) {
                    Reader lReader = (Reader) lDataStreamObject;
                    IOUtil.transfer(lReader, pFileOutputStream, 64000);
                    lReader.close();
                } else {
                    throw new ExInternal("Unexpected object returned from StreamParcelInput.getDataStreamObject()");
                }
            } catch (IOException e) {
                throw new ExInternal("Error streaming TemporaryResource", e);
            }
        } else if (pInputStreamFile instanceof StreamParcelInputFileUpload) {
            StreamParcelInputFileUpload lStreamParcelInputFileUpload = (StreamParcelInputFileUpload) pInputStreamFile;
            try {
                lStreamParcelInputFileUpload.setParams(pXThread);
                InputStream lInputStream = lStreamParcelInputFileUpload.getInputStream();
                if (lStreamParcelInputFileUpload.getStorageType() == StreamParcelInputFileUpload.TYPE_STORAGE_LOCATION) {
                    pXThread.alterThreadSetLevel(pFoxRequest, XThread.THREAD_CLOSED, pUCon);
                }
                IOUtil.transfer(lInputStream, pFileOutputStream, 64000);
                if (lStreamParcelInputFileUpload.getStorageType() == StreamParcelInputFileUpload.TYPE_DOM) {
                    pXThread.alterThreadSetLevel(pFoxRequest, XThread.THREAD_CLOSED, pUCon);
                }
                lInputStream.close();
            } catch (IOException e) {
                throw new ExInternal("Error streaming FileUpload", e);
            }
        }
    }
}
