package com.knowgate.jcifs.smb;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * There are hundreds of error codes that may be returned by a CIFS
 * server. Rather than represent each with it's own <code>Exception</code>
 * class, this class represents all of them. For many of the popular
 * error codes, constants and text messages like "The device is not ready"
 * are provided.
 * <p>
 * The jCIFS client maps DOS error codes to NTSTATUS codes. This means that
 * the user may recieve a different error from a legacy server than that of
 * a newer varient such as Windows NT and above. If you should encounter
 * such a case, please report it to jcifs at samba dot org and we will
 * change the mapping.
 */
public class SmbException extends IOException implements NtStatus, DosError, WinError {

    static String getMessageByCode(int errcode) {
        if ((errcode & 0xC0000000) == 0xC0000000) {
            int min = 0;
            int max = NT_STATUS_CODES.length;
            while (max >= min) {
                int mid = (min + max) / 2;
                if (errcode > NT_STATUS_CODES[mid]) {
                    min = mid + 1;
                } else if (errcode < NT_STATUS_CODES[mid]) {
                    max = mid - 1;
                } else {
                    return NT_STATUS_MESSAGES[mid];
                }
            }
        } else {
            int min = 0;
            int max = DOS_ERROR_CODES.length;
            while (max >= min) {
                int mid = (min + max) / 2;
                if (errcode > DOS_ERROR_CODES[mid][0]) {
                    min = mid + 1;
                } else if (errcode < DOS_ERROR_CODES[mid][0]) {
                    max = mid - 1;
                } else {
                    return DOS_ERROR_MESSAGES[mid];
                }
            }
        }
        return "0x" + com.knowgate.misc.Gadgets.toHexString(errcode, 8);
    }

    static int getStatusByCode(int errcode) {
        if ((errcode & 0xC0000000) == 0xC0000000) {
            return errcode;
        } else {
            int min = 0;
            int max = DOS_ERROR_CODES.length;
            while (max >= min) {
                int mid = (min + max) / 2;
                if (errcode > DOS_ERROR_CODES[mid][0]) {
                    min = mid + 1;
                } else if (errcode < DOS_ERROR_CODES[mid][0]) {
                    max = mid - 1;
                } else {
                    return DOS_ERROR_CODES[mid][1];
                }
            }
        }
        return NT_STATUS_UNSUCCESSFUL;
    }

    static String getMessageByWinerrCode(int errcode) {
        int min = 0;
        int max = WINERR_CODES.length;
        while (max >= min) {
            int mid = (min + max) / 2;
            if (errcode > WINERR_CODES[mid]) {
                min = mid + 1;
            } else if (errcode < WINERR_CODES[mid]) {
                max = mid - 1;
            } else {
                return WINERR_MESSAGES[mid];
            }
        }
        return errcode + "";
    }

    private int status;

    private Throwable rootCause;

    SmbException() {
    }

    SmbException(int errcode, Throwable rootCause) {
        super(getMessageByCode(errcode));
        status = getStatusByCode(errcode);
        this.rootCause = rootCause;
    }

    SmbException(String msg) {
        super(msg);
        status = NT_STATUS_UNSUCCESSFUL;
    }

    SmbException(String msg, Throwable rootCause) {
        super(msg);
        this.rootCause = rootCause;
        status = NT_STATUS_UNSUCCESSFUL;
    }

    SmbException(int errcode, boolean winerr) {
        super(winerr ? getMessageByWinerrCode(errcode) : getMessageByCode(errcode));
        status = winerr ? errcode : getStatusByCode(errcode);
    }

    public int getNtStatus() {
        return status;
    }

    public Throwable getRootCause() {
        return rootCause;
    }

    public String toString() {
        if (rootCause != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            rootCause.printStackTrace(pw);
            return super.toString() + "\n" + sw;
        } else {
            return super.toString();
        }
    }
}
