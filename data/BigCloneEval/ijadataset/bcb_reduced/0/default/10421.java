import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.en.us.CMULexicon;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Implements a text-to-speech server for the Client/Server demo.
 * It creates two Voices when it starts up, one 8k and one 16k,
 * and then waits for socket connections.
 * After it receives a connection, it waits for TTS requests from the client,
 * does speech synthesis, and then sends the synthesized wave bytes back to
 * the client. For a complete specification of the protocol, please refer
 * to the document <code>Protocol.txt</code>.
 */
public class Server extends TTSServer {

    private Voice voice8k;

    private String voice8kClassName = System.getProperty("voice8kClassName", "com.sun.speech.freetts.en.us.CMUDiphoneVoice");

    private String voice8kDatabaseName = "cmu_kal/diphone_units.bin";

    private Voice voice16k;

    private String voice16kClassName = System.getProperty("voice16kClassName", "com.sun.speech.freetts.en.us.CMUDiphoneVoice");

    private String voice16kDatabaseName = "cmu_kal/diphone_units16.bin";

    /**
     * Constructs a default Server, which loads an 8k Voice and a 16k Voice
     * by default.
     */
    public Server() {
        port = Integer.getInteger("port", 5555).intValue();
        try {
            voice8k = loadVoice(voice8kClassName, voice8kDatabaseName);
            voice16k = loadVoice(voice16kClassName, voice16kDatabaseName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates and loads the FreeTTS Voice.
     *
     * @param voiceClassName the name of the Voice class
     * @param databaseName the name of the diphone database file
     */
    private static Voice loadVoice(String voiceClassName, String databaseName) throws Exception {
        Class voiceClass = Class.forName(voiceClassName);
        Voice voice = (Voice) voiceClass.newInstance();
        voice.setLexicon(new CMULexicon());
        voice.getFeatures().setString(Voice.DATABASE_NAME, databaseName);
        voice.load();
        return voice;
    }

    /**
     * Returns the 8k diphone voice.
     *
     * @return 8k diphone voice
     */
    public Voice get8kVoice() {
        return voice8k;
    }

    /**
     * Returns the 16k diphone voice.
     *
     * @return 16k diphone voice
     */
    public Voice get16kVoice() {
        return voice16k;
    }

    /**
     * Spawns a ProtocolHandler depending on the current protocol.
     *
     * @param socket the socket that the spawned protocol handler will use
     */
    protected void spawnProtocolHandler(Socket socket) {
        try {
            SocketTTSHandler handler = new SocketTTSHandler(socket, this);
            (new Thread(handler)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts this TTS Server.
     */
    public static void main(String[] args) {
        Server server = new Server();
        (new Thread(server)).start();
    }
}

/**
 * A simple socket TTS request handler.
 */
class SocketTTSHandler implements Runnable {

    private Voice voice;

    private Server server;

    private Socket socket;

    private SocketAudioPlayer socketAudioPlayer;

    private BufferedReader reader;

    private PrintWriter writer;

    private static final int INVALID_SAMPLE_RATE = 1;

    private boolean metrics = Boolean.getBoolean("metrics");

    private long requestReceivedTime;

    private long requestSpeakTime;

    /**
     * Constructs a SocketTTSHandler with the given <code>Socket</code>
     * and <code>Server</code>.
     *
     * @param socket the Socket to read from and write to
     * @param server the Server to obtain Voices from
     */
    public SocketTTSHandler(Socket socket, Server server) {
        setSocket(socket);
        this.server = server;
        this.socketAudioPlayer = new SocketAudioPlayer(socket);
    }

    /**
     * Sets the Socket to be used by this ProtocolHandler.
     *
     * @param socket the Socket to be used
     */
    private void setSocket(Socket socket) {
        this.socket = socket;
        if (socket != null) {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                println("Socket reader/writer not instantiated");
                throw new Error();
            }
        }
    }

    /**
     * Implements the run() method of Runnable
     */
    public void run() {
        try {
            writer.println("READY");
            String command = null;
            int status;
            while ((command = reader.readLine()) != null && command.equals("TTS")) {
                requestReceivedTime = System.currentTimeMillis();
                status = handleSynthesisRequest();
                if (status == INVALID_SAMPLE_RATE) {
                    println("Invalid sample rate\nexit.");
                    return;
                } else if (metrics) {
                    System.out.println("Time To Sending First Byte: " + (socketAudioPlayer.getFirstByteSentTime() - requestReceivedTime) + " ms");
                }
            }
            if (command != null) {
                if (command.equals("DONE")) {
                    socket.close();
                    println("... closed socket connection");
                } else {
                    println("invalid command: " + command);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Handles a single speech synthesis request.
     */
    private int handleSynthesisRequest() {
        try {
            String sampleRateLine = reader.readLine();
            int sampleRate = Integer.parseInt(sampleRateLine);
            if (sampleRate == 8000) {
                voice = server.get8kVoice();
            } else if (sampleRate == 16000) {
                voice = server.get16kVoice();
            } else {
                writer.println("-2");
                return INVALID_SAMPLE_RATE;
            }
            String text = reader.readLine();
            voice.setAudioPlayer(socketAudioPlayer);
            voice.speak(text);
            writer.println("-1");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return 0;
    }

    /**
     * A central point to write out all message.
     *
     * @param message the message
     */
    private void println(String message) {
        System.out.println(message);
    }
}
