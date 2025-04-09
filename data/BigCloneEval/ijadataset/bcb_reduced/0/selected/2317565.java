package tests.com;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;
import org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
import org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.pfyshnet.com.IncommingCallback;
import org.pfyshnet.com.IncommingProcessor;
import org.pfyshnet.com.OutgoingCallback;
import org.pfyshnet.com.OutgoingProcessor;
import org.pfyshnet.utils.DiffFiles;

public class ConnectionTester implements Runnable {

    private File TransferFile;

    private File ReceivedFile;

    private ElGamalPublicKeyParameters PublicKey;

    private ElGamalPrivateKeyParameters PrivateKey;

    public ConnectionTester(String outgoing, String incoming) {
        TransferFile = new File(outgoing);
        ReceivedFile = new File(incoming);
    }

    public class IncommingCB implements IncommingCallback {

        public void FileComplete(File f) {
            try {
                if (!DiffFiles.diffFiles(f, TransferFile)) {
                    System.out.println("TRANSFER FAILED: " + f.getPath() + " vs. " + TransferFile.getPath());
                } else {
                    System.out.println("SUCCESS: " + f.getPath() + " vs. " + TransferFile.getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void IncomingClosed(IncommingProcessor proc) {
            System.out.println("IncommingClosed called!");
        }
    }

    public class OutgoingCB implements OutgoingCallback {

        public void Fail(String failstr) {
            System.out.println("Outgoing fail: " + failstr);
        }

        public void Success(String IP) {
            System.out.println("IP returned for outgoing: " + IP);
        }

        public void OutgoingClosed(OutgoingProcessor proc) {
        }
    }

    public void RunTest() {
        Thread t = new Thread(this);
        t.start();
        SecureRandom srand = new SecureRandom();
        ElGamalParametersGenerator generator = new ElGamalParametersGenerator();
        generator.init(768, 5, srand);
        ElGamalParameters ElGamalParms = generator.generateParameters();
        ElGamalKeyGenerationParameters genparms = new ElGamalKeyGenerationParameters(srand, ElGamalParms);
        ElGamalKeyPairGenerator keygen = new ElGamalKeyPairGenerator();
        keygen.init(genparms);
        AsymmetricCipherKeyPair keypair = keygen.generateKeyPair();
        PublicKey = (ElGamalPublicKeyParameters) keypair.getPublic();
        PrivateKey = (ElGamalPrivateKeyParameters) keypair.getPrivate();
        try {
            new OutgoingProcessor("127.0.0.1,6543", TransferFile, File.createTempFile("blah", ".dat"), new OutgoingCB(), PublicKey, srand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(6543);
            Socket insock = server.accept();
            new IncommingProcessor(insock, ReceivedFile, new IncommingCB(), PrivateKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Args: outgoingfile incomingfile");
            System.exit(0);
        }
        ConnectionTester tester = new ConnectionTester(args[0], args[1]);
        tester.RunTest();
    }
}
