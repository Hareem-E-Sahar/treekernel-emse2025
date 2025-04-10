package com.android.bluetooth.opp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.webkit.MimeTypeMap;
import javax.obex.HeaderSet;
import javax.obex.ObexTransport;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import javax.obex.ServerSession;

/**
 * This class runs as an OBEX server
 */
public class BluetoothOppObexServerSession extends ServerRequestHandler implements BluetoothOppObexSession {

    private static final String TAG = "BtOppObexServer";

    private static final boolean D = Constants.DEBUG;

    private static final boolean V = Constants.VERBOSE;

    private ObexTransport mTransport;

    private Context mContext;

    private Handler mCallback = null;

    private boolean mServerBlocking = true;

    private BluetoothOppShareInfo mInfo;

    private int mLocalShareInfoId;

    private int mAccepted = BluetoothShare.USER_CONFIRMATION_PENDING;

    private boolean mInterrupted = false;

    private ServerSession mSession;

    private long mTimestamp;

    private BluetoothOppReceiveFileInfo mFileInfo;

    private WakeLock mWakeLock;

    private WakeLock mPartialWakeLock;

    boolean mTimeoutMsgSent = false;

    public BluetoothOppObexServerSession(Context context, ObexTransport transport) {
        mContext = context;
        mTransport = transport;
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    public void unblock() {
        mServerBlocking = false;
    }

    /**
     * Called when connection is accepted from remote, to retrieve the first
     * Header then wait for user confirmation
     */
    public void preStart() {
        if (D) Log.d(TAG, "acquire full WakeLock");
        mWakeLock.acquire();
        try {
            if (D) Log.d(TAG, "Create ServerSession with transport " + mTransport.toString());
            mSession = new ServerSession(mTransport, this, null);
        } catch (IOException e) {
            Log.e(TAG, "Create server session error" + e);
        }
    }

    /**
     * Called from BluetoothOppTransfer to start the "Transfer"
     */
    public void start(Handler handler) {
        if (D) Log.d(TAG, "Start!");
        mCallback = handler;
    }

    /**
     * Called from BluetoothOppTransfer to cancel the "Transfer" Otherwise,
     * server should end by itself.
     */
    public void stop() {
        if (D) Log.d(TAG, "Stop!");
        mInterrupted = true;
        if (mSession != null) {
            try {
                mSession.close();
                mTransport.close();
            } catch (IOException e) {
                Log.e(TAG, "close mTransport error" + e);
            }
        }
        mCallback = null;
        mSession = null;
    }

    public void addShare(BluetoothOppShareInfo info) {
        if (D) Log.d(TAG, "addShare for id " + info.mId);
        mInfo = info;
        mFileInfo = processShareInfo();
    }

    @Override
    public int onPut(Operation op) {
        if (D) Log.d(TAG, "onPut " + op.toString());
        HeaderSet request;
        String name, mimeType;
        Long length;
        int obexResponse = ResponseCodes.OBEX_HTTP_OK;
        if (mAccepted == BluetoothShare.USER_CONFIRMATION_DENIED) {
            return ResponseCodes.OBEX_HTTP_FORBIDDEN;
        }
        try {
            boolean pre_reject = false;
            request = op.getReceivedHeader();
            if (V) Constants.logHeader(request);
            name = (String) request.getHeader(HeaderSet.NAME);
            length = (Long) request.getHeader(HeaderSet.LENGTH);
            mimeType = (String) request.getHeader(HeaderSet.TYPE);
            if (length == 0) {
                if (D) Log.w(TAG, "length is 0, reject the transfer");
                pre_reject = true;
                obexResponse = ResponseCodes.OBEX_HTTP_LENGTH_REQUIRED;
            }
            if (name == null || name.equals("")) {
                if (D) Log.w(TAG, "name is null or empty, reject the transfer");
                pre_reject = true;
                obexResponse = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }
            if (!pre_reject) {
                String extension, type;
                int dotIndex = name.lastIndexOf(".");
                if (dotIndex < 0) {
                    if (D) Log.w(TAG, "There is no file extension, reject the transfer");
                    pre_reject = true;
                    obexResponse = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
                } else {
                    extension = name.substring(dotIndex + 1).toLowerCase();
                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    type = map.getMimeTypeFromExtension(extension);
                    if (V) Log.v(TAG, "Mimetype guessed from extension " + extension + " is " + type);
                    if (type != null) {
                        mimeType = type;
                    } else {
                        if (mimeType == null) {
                            if (D) Log.w(TAG, "Can't get mimetype, reject the transfer");
                            pre_reject = true;
                            obexResponse = ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE;
                        }
                    }
                    if (mimeType != null) {
                        mimeType = mimeType.toLowerCase();
                    }
                }
            }
            if (!pre_reject && (mimeType == null || (!Constants.mimeTypeMatches(mimeType, Constants.ACCEPTABLE_SHARE_INBOUND_TYPES)))) {
                if (D) Log.w(TAG, "mimeType is null or in unacceptable list, reject the transfer");
                pre_reject = true;
                obexResponse = ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE;
            }
            if (pre_reject && obexResponse != ResponseCodes.OBEX_HTTP_OK) {
                return obexResponse;
            }
        } catch (IOException e) {
            Log.e(TAG, "get getReceivedHeaders error " + e);
            return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }
        ContentValues values = new ContentValues();
        values.put(BluetoothShare.FILENAME_HINT, name);
        values.put(BluetoothShare.TOTAL_BYTES, length.intValue());
        values.put(BluetoothShare.MIMETYPE, mimeType);
        if (mTransport instanceof BluetoothOppRfcommTransport) {
            String a = ((BluetoothOppRfcommTransport) mTransport).getRemoteAddress();
            values.put(BluetoothShare.DESTINATION, a);
        } else {
            values.put(BluetoothShare.DESTINATION, "FF:FF:FF:00:00:00");
        }
        values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_INBOUND);
        values.put(BluetoothShare.TIMESTAMP, mTimestamp);
        boolean needConfirm = true;
        if (!mServerBlocking) {
            values.put(BluetoothShare.USER_CONFIRMATION, BluetoothShare.USER_CONFIRMATION_AUTO_CONFIRMED);
            needConfirm = false;
        }
        Uri contentUri = mContext.getContentResolver().insert(BluetoothShare.CONTENT_URI, values);
        mLocalShareInfoId = Integer.parseInt(contentUri.getPathSegments().get(1));
        if (needConfirm) {
            Intent in = new Intent(BluetoothShare.INCOMING_FILE_CONFIRMATION_REQUEST_ACTION);
            in.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            mContext.sendBroadcast(in);
        }
        if (V) Log.v(TAG, "insert contentUri: " + contentUri);
        if (V) Log.v(TAG, "mLocalShareInfoId = " + mLocalShareInfoId);
        if (V) Log.v(TAG, "acquire partial WakeLock");
        if (mWakeLock.isHeld()) {
            mPartialWakeLock.acquire();
            mWakeLock.release();
        }
        mServerBlocking = true;
        synchronized (this) {
            try {
                while (mServerBlocking) {
                    wait(1000);
                    if (mCallback != null && !mTimeoutMsgSent) {
                        mCallback.sendMessageDelayed(mCallback.obtainMessage(BluetoothOppObexSession.MSG_CONNECT_TIMEOUT), BluetoothOppObexSession.SESSION_TIMEOUT);
                        mTimeoutMsgSent = true;
                        if (V) Log.v(TAG, "MSG_CONNECT_TIMEOUT sent");
                    }
                }
            } catch (InterruptedException e) {
                if (V) Log.v(TAG, "Interrupted in onPut blocking");
            }
        }
        if (D) Log.d(TAG, "Server unblocked ");
        synchronized (this) {
            if (mCallback != null && mTimeoutMsgSent) {
                mCallback.removeMessages(BluetoothOppObexSession.MSG_CONNECT_TIMEOUT);
            }
        }
        if (mInfo.mId != mLocalShareInfoId) {
            Log.e(TAG, "Unexpected error!");
        }
        mAccepted = mInfo.mConfirm;
        if (V) Log.v(TAG, "after confirm: userAccepted=" + mAccepted);
        int status = BluetoothShare.STATUS_SUCCESS;
        if (mAccepted == BluetoothShare.USER_CONFIRMATION_CONFIRMED || mAccepted == BluetoothShare.USER_CONFIRMATION_AUTO_CONFIRMED) {
            if (mFileInfo.mFileName == null) {
                status = mFileInfo.mStatus;
                mInfo.mStatus = mFileInfo.mStatus;
                Constants.updateShareStatus(mContext, mInfo.mId, status);
                obexResponse = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            }
            if (mFileInfo.mFileName != null) {
                ContentValues updateValues = new ContentValues();
                contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + mInfo.mId);
                updateValues.put(BluetoothShare._DATA, mFileInfo.mFileName);
                updateValues.put(BluetoothShare.STATUS, BluetoothShare.STATUS_RUNNING);
                mContext.getContentResolver().update(contentUri, updateValues, null, null);
                status = receiveFile(mFileInfo, op);
                if (status != BluetoothShare.STATUS_SUCCESS) {
                    obexResponse = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                }
                Constants.updateShareStatus(mContext, mInfo.mId, status);
            }
            if (status == BluetoothShare.STATUS_SUCCESS) {
                Message msg = Message.obtain(mCallback, BluetoothOppObexSession.MSG_SHARE_COMPLETE);
                msg.obj = mInfo;
                msg.sendToTarget();
            } else {
                if (mCallback != null) {
                    Message msg = Message.obtain(mCallback, BluetoothOppObexSession.MSG_SESSION_ERROR);
                    mInfo.mStatus = status;
                    msg.obj = mInfo;
                    msg.sendToTarget();
                }
            }
        } else if (mAccepted == BluetoothShare.USER_CONFIRMATION_DENIED || mAccepted == BluetoothShare.USER_CONFIRMATION_TIMEOUT) {
            Log.i(TAG, "Rejected incoming request");
            if (mFileInfo.mFileName != null) {
                try {
                    mFileInfo.mOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "error close file stream");
                }
                new File(mFileInfo.mFileName).delete();
            }
            status = BluetoothShare.STATUS_CANCELED;
            Constants.updateShareStatus(mContext, mInfo.mId, status);
            obexResponse = ResponseCodes.OBEX_HTTP_FORBIDDEN;
            Message msg = Message.obtain(mCallback);
            msg.what = BluetoothOppObexSession.MSG_SHARE_INTERRUPTED;
            mInfo.mStatus = status;
            msg.obj = mInfo;
            msg.sendToTarget();
        }
        return obexResponse;
    }

    private int receiveFile(BluetoothOppReceiveFileInfo fileInfo, Operation op) {
        int status = -1;
        BufferedOutputStream bos = null;
        InputStream is = null;
        boolean error = false;
        try {
            is = op.openInputStream();
        } catch (IOException e1) {
            Log.e(TAG, "Error when openInputStream");
            status = BluetoothShare.STATUS_OBEX_DATA_ERROR;
            error = true;
        }
        Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + mInfo.mId);
        if (!error) {
            ContentValues updateValues = new ContentValues();
            updateValues.put(BluetoothShare._DATA, fileInfo.mFileName);
            mContext.getContentResolver().update(contentUri, updateValues, null, null);
        }
        int position = 0;
        if (!error) {
            bos = new BufferedOutputStream(fileInfo.mOutputStream, 0x10000);
        }
        if (!error) {
            int outputBufferSize = op.getMaxPacketSize();
            byte[] b = new byte[outputBufferSize];
            int readLength = 0;
            long timestamp = 0;
            try {
                while ((!mInterrupted) && (position != fileInfo.mLength)) {
                    if (V) timestamp = System.currentTimeMillis();
                    readLength = is.read(b);
                    if (readLength == -1) {
                        if (D) Log.d(TAG, "Receive file reached stream end at position" + position);
                        break;
                    }
                    bos.write(b, 0, readLength);
                    position += readLength;
                    if (V) {
                        Log.v(TAG, "Receive file position = " + position + " readLength " + readLength + " bytes took " + (System.currentTimeMillis() - timestamp) + " ms");
                    }
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(BluetoothShare.CURRENT_BYTES, position);
                    mContext.getContentResolver().update(contentUri, updateValues, null, null);
                }
            } catch (IOException e1) {
                Log.e(TAG, "Error when receiving file");
                status = BluetoothShare.STATUS_OBEX_DATA_ERROR;
                error = true;
            }
        }
        if (mInterrupted) {
            if (D) Log.d(TAG, "receiving file interrupted by user.");
            status = BluetoothShare.STATUS_CANCELED;
        } else {
            if (position == fileInfo.mLength) {
                if (D) Log.d(TAG, "Receiving file completed for " + fileInfo.mFileName);
                status = BluetoothShare.STATUS_SUCCESS;
            } else {
                if (D) Log.d(TAG, "Reading file failed at " + position + " of " + fileInfo.mLength);
                if (status == -1) {
                    status = BluetoothShare.STATUS_UNKNOWN_ERROR;
                }
            }
        }
        if (bos != null) {
            try {
                bos.close();
            } catch (IOException e) {
                Log.e(TAG, "Error when closing stream after send");
            }
        }
        return status;
    }

    private BluetoothOppReceiveFileInfo processShareInfo() {
        if (D) Log.d(TAG, "processShareInfo() " + mInfo.mId);
        BluetoothOppReceiveFileInfo fileInfo = BluetoothOppReceiveFileInfo.generateFileInfo(mContext, mInfo.mId);
        if (V) {
            Log.v(TAG, "Generate BluetoothOppReceiveFileInfo:");
            Log.v(TAG, "filename  :" + fileInfo.mFileName);
            Log.v(TAG, "length    :" + fileInfo.mLength);
            Log.v(TAG, "status    :" + fileInfo.mStatus);
        }
        return fileInfo;
    }

    @Override
    public int onConnect(HeaderSet request, HeaderSet reply) {
        if (D) Log.d(TAG, "onConnect");
        if (V) Constants.logHeader(request);
        try {
            byte[] uuid = (byte[]) request.getHeader(HeaderSet.TARGET);
            if (V) Log.v(TAG, "onConnect(): uuid =" + Arrays.toString(uuid));
            if (uuid != null) {
                reply.setHeader(HeaderSet.WHO, uuid);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        mTimestamp = System.currentTimeMillis();
        return ResponseCodes.OBEX_HTTP_OK;
    }

    @Override
    public void onDisconnect(HeaderSet req, HeaderSet resp) {
        if (D) Log.d(TAG, "onDisconnect");
        resp.responseCode = ResponseCodes.OBEX_HTTP_OK;
    }

    @Override
    public void onClose() {
        if (V) Log.v(TAG, "release WakeLock");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mPartialWakeLock.isHeld()) {
            mPartialWakeLock.release();
        }
        if (mCallback != null) {
            Message msg = Message.obtain(mCallback);
            msg.what = BluetoothOppObexSession.MSG_SESSION_COMPLETE;
            msg.obj = mInfo;
            msg.sendToTarget();
        }
    }
}
