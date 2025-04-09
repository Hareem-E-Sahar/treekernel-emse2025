import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.signers.*;

public class FileServer extends Server {

    public static final int SERVER_PORT = 4321;

    public static FileList fileList;

    private KeyPair fileSignKey;

    private static PrivateKey filePrivateKey;

    private static PublicKey filePublicKey;

    public SecretKey hmackey = null;

    public FileServer() {
        super(SERVER_PORT, "FilePile");
    }

    public FileServer(int _port) {
        super(_port, "FilePile");
    }

    public void start() {
        String fileFile = "FileList.bin";
        ObjectInputStream fileStream;
        Runtime runtime = Runtime.getRuntime();
        Thread catchExit = new Thread(new ShutDownListenerFS());
        runtime.addShutdownHook(catchExit);
        try {
            Security.addProvider(new BouncyCastleProvider());
            fileSignKey = generateFileServerKeys();
            System.out.println("generated key pair");
            filePrivateKey = fileSignKey.getPrivate();
            filePublicKey = fileSignKey.getPublic();
            X509EncodedKeySpec publickeyspec = new X509EncodedKeySpec(filePublicKey.getEncoded());
            FileOutputStream outstream = new FileOutputStream("FileServerPublicKey");
            outstream.write(publickeyspec.getEncoded());
            outstream.close();
            System.out.println("Fileservers public key: " + filePublicKey);
            System.out.println("Fileservers private key: " + filePrivateKey.getEncoded());
        } catch (Exception e) {
            System.out.println("error getting key pair");
        }
        try {
            FileInputStream fis = new FileInputStream(fileFile);
            fileStream = new ObjectInputStream(fis);
            fileList = (FileList) fileStream.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("FileList Does Not Exist. Creating FileList...");
            fileList = new FileList();
        } catch (IOException e) {
            System.out.println("Error reading from FileList file");
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading from FileList file");
            System.exit(-1);
        }
        File file = new File("shared_files");
        if (file.mkdir()) {
            System.out.println("Created new shared_files directory");
        } else if (file.exists()) {
            System.out.println("Found shared_files directory");
        } else {
            System.out.println("Error creating shared_files directory");
        }
        AutoSaveFS aSave = new AutoSaveFS();
        aSave.setDaemon(true);
        aSave.start();
        boolean running = true;
        try {
            final ServerSocket serverSock = new ServerSocket(port);
            System.out.printf("%s up and running\n", this.getClass().getName());
            System.out.println("Server hosted at : " + InetAddress.getLocalHost().getHostAddress());
            Socket sock = null;
            Thread thread = null;
            while (running) {
                sock = serverSock.accept();
                thread = new FileThread(sock);
                thread.start();
            }
            System.out.printf("%s shut down\n", this.getClass().getName());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private KeyPair generateFileServerKeys() throws NoSuchAlgorithmException {
        try {
            KeyPairGenerator fileKeysGen = KeyPairGenerator.getInstance("RSA", "BC");
            System.out.println("keypairgenerator: " + fileKeysGen.toString());
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            fileKeysGen.initialize(1024, random);
            KeyPair dsaFileKey = fileKeysGen.generateKeyPair();
            return dsaFileKey;
        } catch (NoSuchProviderException e) {
            System.out.println("No such provider available.");
            return null;
        }
    }

    protected static PrivateKey getPrivateKey() {
        return filePrivateKey;
    }

    public static PublicKey getPublicKey() {
        return filePublicKey;
    }
}

class ShutDownListenerFS implements Runnable {

    public void run() {
        System.out.println("Shutting down server");
        ObjectOutputStream outStream;
        try {
            outStream = new ObjectOutputStream(new FileOutputStream("FileList.bin"));
            outStream.writeObject(FileServer.fileList);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

class AutoSaveFS extends Thread {

    public void run() {
        do {
            try {
                Thread.sleep(300000);
                System.out.println("Autosave file list...");
                ObjectOutputStream outStream;
                try {
                    outStream = new ObjectOutputStream(new FileOutputStream("FileList.bin"));
                    outStream.writeObject(FileServer.fileList);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            } catch (Exception e) {
                System.out.println("Autosave Interrupted");
            }
        } while (true);
    }
}
