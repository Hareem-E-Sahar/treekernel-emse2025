package ddbadmin.admin;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Roar
 */
public class FileServer {

    int port;

    File fileToSend;

    public FileServer(int port, File fileToSend) {
        this.port = port;
        this.fileToSend = fileToSend;
        try {
            ServerSocket fileServer = new ServerSocket(port);
            Socket socket = fileServer.accept();
            DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
            byte[] buffer = new byte[2048];
            DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToSend)));
            while (true) {
                int read = 0;
                if (fis != null) {
                    read = fis.read(buffer);
                }
                if (read == -1) {
                    break;
                }
                dataOutput.write(buffer, 0, read);
            }
            dataOutput.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
