package com.itbs.aimcer.commune.joscar;

import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferService;
import net.kano.joscar.rvcmd.SegmentedFilename;
import net.kano.joscar.rvproto.ft.FileTransferHeader;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendFileThread extends Thread implements FileTransferService {

    private static Logger log = Logger.getLogger(SendFileThread.class.getName());

    private ServerSocket serverSocket;

    FileTransferListener listener;

    boolean goOn = true;

    public SendFileThread(FileTransferListener listener, ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.listener = listener;
        setName("File Transfer Thread");
    }

    public void cancelTransfer() {
        goOn = false;
        listener.notifyCancel();
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
    }

    public void run() {
        RandomAccessFile reader = null;
        try {
            log.fine("waiting for connection..");
            listener.notifyWaiting();
            Socket socket = serverSocket.accept();
            log.fine("got connection from " + socket.getInetAddress());
            listener.notifyNegotiation();
            final OutputStream out = socket.getOutputStream();
            final InputStream in = socket.getInputStream();
            FileTransferHeader fsh = new FileTransferHeader();
            fsh.setDefaults();
            fsh.setFileCount(1);
            fsh.setFilesLeft(1);
            fsh.setFilename(SegmentedFilename.fromNativeFilename(listener.getFile().getName()));
            final File file = listener.getFile();
            final long fileLength = file.length();
            fsh.setFileSize(fileLength);
            fsh.setTotalFileSize(fileLength);
            fsh.setHeaderType(FileTransferHeader.HEADERTYPE_SENDHEADER);
            fsh.setPartCount(1);
            fsh.setPartsLeft(1);
            fsh.setLastmod(file.lastModified() / 1000);
            fsh.setChecksum(100);
            log.fine("writing: " + fsh);
            fsh.write(out);
            log.fine("waiting for ack header..");
            FileTransferHeader inFsh = FileTransferHeader.readHeader(in);
            log.fine("got ack:" + inFsh);
            if (inFsh.getHeaderType() == FileTransferHeader.HEADERTYPE_RESUME) {
                fsh.setHeaderType(FileTransferHeader.HEADERTYPE_RESUME_SENDHEADER);
                fsh.write(out);
                inFsh = FileTransferHeader.readHeader(in);
                log.fine("resumesendresponse: " + inFsh);
            }
            reader = new RandomAccessFile(file, "r");
            byte[] buffer = new byte[1024 * 10];
            int read;
            long totalRead = 0;
            do {
                read = reader.read(buffer);
                if (read == -1) break;
                out.write(buffer, 0, read);
                totalRead += read;
                listener.setProgress((int) (totalRead * 100 / fileLength));
            } while (goOn);
            reader.close();
            if (!goOn) listener.notifyCancel(); else listener.notifyDone();
        } catch (IOException e) {
            log.log(Level.SEVERE, "", e);
            cancelTransfer();
            listener.notifyFail();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                ;
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
