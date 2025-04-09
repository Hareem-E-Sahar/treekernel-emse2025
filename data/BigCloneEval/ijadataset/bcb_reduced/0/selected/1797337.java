package visitpc.srcclient;

import java.io.*;
import java.net.Socket;
import visitpc.*;
import visitpc.messages.*;
import java.util.zip.*;

/**
 * Responsible for handling connections to clients
 */
public class SrcConnectionHandler extends Thread {

    public static int CurrentSessionID = 1;

    private Socket socket;

    private SrcClientConfig srcClientConfig;

    private SSLConnectionHandler sslConnectionHandler;

    private int sessionID;

    private InputStream is;

    private OutputStream os;

    private UserOutput uo;

    public SrcConnectionHandler(UserOutput uo, Socket socket, SrcClientConfig srcClientConfig, SSLConnectionHandler sslConnectionHandler) {
        synchronized (this) {
            this.uo = uo;
            this.socket = socket;
            this.srcClientConfig = srcClientConfig;
            this.sslConnectionHandler = sslConnectionHandler;
            sessionID = SrcConnectionHandler.CurrentSessionID;
            SrcConnectionHandler.CurrentSessionID += 1;
        }
    }

    @Override
    public void run() {
        try {
            handleSocket();
        } catch (Exception e) {
            uo.error(e);
        }
    }

    private void handleSocket() throws IOException, InterruptedException {
        int readByteCount;
        byte readBuffer[] = new byte[VisitPCConstants.RXBUFFERSIZE];
        socket.setKeepAlive(true);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        try {
            sslConnectionHandler.connectSrcAndDestAtServer(srcClientConfig);
            sslConnectionHandler.connectDestPCToDestServerSocket(sessionID, srcClientConfig);
            while (socket != null) {
                readByteCount = is.read(readBuffer);
                if (readByteCount == -1) {
                    break;
                }
                if (srcClientConfig.useCRC32) {
                    CRC32DataMessage crc32DataMessage = SrcConnectionHandler.GetCRC32DataMessage(readBuffer, readByteCount, sessionID);
                    if (crc32DataMessage.buffer.length > 0) {
                        sslConnectionHandler.send(crc32DataMessage);
                    }
                } else {
                    DataMessage dataMessage = SrcConnectionHandler.GetDataMessage(readBuffer, readByteCount, sessionID);
                    if (dataMessage.buffer.length > 0) {
                        sslConnectionHandler.send(dataMessage);
                    }
                }
            }
        } finally {
            stopHandlingConnection();
        }
    }

    public static CRC32DataMessage GetCRC32DataMessage(byte buffer[], int byteCount, int sessionID) {
        CRC32DataMessage crc32DataMessage = new CRC32DataMessage();
        crc32DataMessage.sessionID = sessionID;
        crc32DataMessage.buffer = SrcConnectionHandler.GetBuffer(buffer, byteCount);
        CRC32 crc32 = new CRC32();
        crc32.update(crc32DataMessage.buffer);
        crc32DataMessage.crc32 = crc32.getValue();
        return crc32DataMessage;
    }

    public static DataMessage GetDataMessage(byte buffer[], int byteCount, int sessionID) {
        DataMessage dataMessage = new DataMessage();
        dataMessage.sessionID = sessionID;
        dataMessage.buffer = SrcConnectionHandler.GetBuffer(buffer, byteCount);
        return dataMessage;
    }

    /**
   * Return a buffer that is the size of the data
   * 
   * @param buffer
   * @param dataLength
   * @return
   */
    public static byte[] GetBuffer(byte buffer[], int dataLength) {
        if (dataLength == buffer.length) {
            return buffer;
        } else if (dataLength > 0 && dataLength <= buffer.length) {
            byte newBuffer[] = new byte[dataLength];
            System.arraycopy(buffer, 0, newBuffer, 0, dataLength);
            return newBuffer;
        }
        return new byte[0];
    }

    /**
   * Call this to close the server from another object
   */
    public void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private synchronized void stopHandlingConnection() {
        if (socket != null) {
            try {
                DisconnectFromDestClient disconnectFromDestClient = new DisconnectFromDestClient();
                disconnectFromDestClient.sessionID = sessionID;
                sslConnectionHandler.send(disconnectFromDestClient);
            } catch (Exception e) {
            }
            if (sslConnectionHandler != null) {
                sslConnectionHandler.removeSrcConnectionHandler(this);
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
            os = null;
            is = null;
            uo.info("Disconnected connection");
        }
    }

    /**
   * Send data that was received from the server out on the socket
   * 
   * @param dataMessage
   */
    public void send(DataMessage dataMessage) throws IOException {
        if (dataMessage instanceof CRC32DataMessage) {
            CRC32DataMessage crc32DataMessage = (CRC32DataMessage) dataMessage;
            CRC32 crc32 = new CRC32();
            crc32.update(dataMessage.buffer);
            if (crc32.getValue() != crc32DataMessage.crc32) {
                throw new IOException("CRC32 value is incorrect on the data received from the server (Expected " + crc32DataMessage.crc32 + ", Found " + crc32.getValue() + ")");
            }
        }
        os.write(dataMessage.buffer);
        os.flush();
    }

    public int getSessionID() {
        return sessionID;
    }
}
