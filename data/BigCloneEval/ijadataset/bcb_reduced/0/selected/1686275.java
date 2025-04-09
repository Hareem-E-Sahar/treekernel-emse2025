package visitpc.destclient;

import visitpc.VisitPCConstants;
import visitpc.messages.*;
import visitpc.srcclient.SrcConnectionHandler;
import java.io.*;
import java.net.*;
import java.util.zip.CRC32;
import visitpc.*;

public class DestConnectionHandler extends Thread {

    private Socket socket;

    private InputStream is;

    private OutputStream os;

    private DestClient destClient;

    private ConnectFromDestClient connectFromDestClient;

    private UserOutput uo;

    public DestConnectionHandler(UserOutput uo, ConnectFromDestClient connectFromDestClient, DestClient destClient) {
        this.uo = uo;
        this.connectFromDestClient = connectFromDestClient;
        this.destClient = destClient;
    }

    public void connect() throws IOException {
        uo.info("Connecting to " + connectFromDestClient.destAddress + ":" + connectFromDestClient.destPort);
        socket = new Socket(connectFromDestClient.destAddress, connectFromDestClient.destPort);
        socket.setKeepAlive(true);
        os = new BufferedOutputStream(socket.getOutputStream());
        is = new BufferedInputStream(socket.getInputStream());
        uo.info("Connected to " + connectFromDestClient.destAddress + ":" + connectFromDestClient.destPort);
    }

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
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
            os = null;
            is = null;
            uo.info("Disconnected from " + connectFromDestClient.destAddress + ":" + connectFromDestClient.destPort);
        }
    }

    @Override
    public void run() {
        int readByteCount;
        byte readBuffer[] = new byte[VisitPCConstants.RXBUFFERSIZE];
        try {
            while (socket != null) {
                readByteCount = is.read(readBuffer);
                if (readByteCount == -1) {
                    break;
                }
                if (connectFromDestClient.useCRC32) {
                    CRC32DataMessage crc32DataMessage = SrcConnectionHandler.GetCRC32DataMessage(readBuffer, readByteCount, connectFromDestClient.sessionID);
                    if (crc32DataMessage.buffer.length > 0) {
                        destClient.send(crc32DataMessage);
                    }
                } else {
                    DataMessage dataMessage = SrcConnectionHandler.GetDataMessage(readBuffer, readByteCount, connectFromDestClient.sessionID);
                    if (dataMessage.buffer.length > 0) {
                        destClient.send(dataMessage);
                    }
                }
            }
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.message = e.getMessage();
            try {
                destClient.send(errorMessage);
            } catch (Exception ex) {
                uo.error(ex);
            }
        } finally {
            stopHandlingConnection();
        }
    }

    public int getSessionID() {
        return connectFromDestClient.sessionID;
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
}
