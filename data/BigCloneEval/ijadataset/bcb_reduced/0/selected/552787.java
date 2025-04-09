package rjws.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import org.apache.log4j.Logger;
import rjws.server.parser.HTTPConnection;
import rjws.server.parser.HTTPRequest;
import rjws.server.parser.MimeType;
import rjws.server.response.HTTPResponse;
import rjws.server.response.ResponseManager;
import rjws.stream.CountedInputStream;
import rjws.stream.CountedOutputStream;
import rjws.utils.StreamUtils;

/**
 * The Dispatcher-Class<br>
 * Instanciated from the Server
 * 
 * @author Thomas Rudin
 *
 */
public class RJWSDispatcher implements Runnable {

    /**
	 * Local Logger
	 */
    private static final Logger logger = Logger.getLogger(RJWSDispatcher.class);

    /**
	 * Instance-Counter
	 */
    private static int instanceCounter = 0;

    /**
	 * The Server-instance
	 */
    private RJWServer parent;

    /**
	 * The Client-Socket
	 */
    private Socket socket;

    /**
	 * The Local Thread
	 */
    private Thread thread;

    /**
	 * The Outputstream from the Socket
	 */
    private CountedOutputStream output;

    private BufferedOutputStream bufferedOutput;

    /**
	 * The Inputstream from the Socket
	 */
    private CountedInputStream input;

    /**
	 * Dispatcher-Class
	 * @param parent The Parent, who created it
	 * @param socket The Socket to listen to
	 */
    public RJWSDispatcher(RJWServer parent, Socket socket) {
        this.parent = parent;
        this.socket = socket;
        this.start();
    }

    /**
	 * Starts the Thread
	 */
    private void start() {
        this.thread = new Thread(this);
        this.thread.setName("RJWServer-Dispatcher<" + instanceCounter++ + ">");
        this.thread.start();
    }

    /**
	 * Stops the Dispatcher
	 */
    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    /**
	 * Cleans everything up
	 */
    private void cleanup() {
        thread = null;
        socket = null;
        instanceCounter--;
        this.parent.dispatcherFinished(this);
    }

    /**
	 * The Threaded Client-Method
	 */
    public void run() {
        try {
            socket.setSoLinger(false, 0);
            socket.setTcpNoDelay(true);
            this.input = new CountedInputStream(socket.getInputStream());
            this.output = new CountedOutputStream(socket.getOutputStream());
            this.bufferedOutput = new BufferedOutputStream(this.output);
            while (socket != null) {
                logger.debug("Reading...");
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                HTTPRequest httpRequest = HTTPRequest.parse(reader, this);
                HTTPResponse response = ResponseManager.getResponse(parent, this, httpRequest);
                if (parent.getRequestHook() != null) parent.getRequestHook().onRequest(httpRequest, response);
                send(response);
                if (httpRequest.getConnection().equals(HTTPConnection.CLOSE)) break;
            }
        } catch (Exception e) {
            logger.debug("Non-fatal exception in run(): " + e.getMessage());
        }
        try {
            socket.close();
        } catch (Exception e) {
            logger.debug("Non-fatal socket-close() exception in run(): " + e.getMessage());
        }
        cleanup();
    }

    /**
	 * Sends a response to the client
	 * @param response
	 * @throws IOException
	 */
    private void send(HTTPResponse response) throws IOException {
        logger.debug("Sending response: " + "type(" + response.getHttpResponseNumber() + ")" + " mime(" + response.getMimeType() + ")");
        if (response.getContent() instanceof InputStream) {
            InputStream input = (InputStream) response.getContent();
            byte[] buffer = new byte[10000];
            int count;
            do {
                count = input.read(buffer);
                if (count > 0) bufferedOutput.write(buffer, 0, count);
            } while (count > 0);
        } else if (response.getContent() instanceof File) {
            File file = (File) response.getContent();
            String mimeType = MimeType.getMimeType(file.getName());
            response.setMimeType(mimeType);
            InputStream input = new FileInputStream(file);
            response.setContent(StreamUtils.printStreamToByteArray(input));
            bufferedOutput.write(response.getResponseBytes());
        } else {
            bufferedOutput.write(response.getResponseBytes());
        }
        bufferedOutput.flush();
    }

    /**
	 * Gets the Remote-Host
	 * @return The Remote-Host
	 */
    public String getHost() {
        try {
            return socket.getInetAddress().getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    /**
	 * @return the rxBytes
	 */
    public long getRxBytes() {
        return input.getCount();
    }

    /**
	 * @return the txBytes
	 */
    public long getTxBytes() {
        return output.getCount();
    }
}
