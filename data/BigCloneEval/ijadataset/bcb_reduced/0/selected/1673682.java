package demo.pkcs.pkcs11;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import iaik.pkcs.pkcs11.Mechanism;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.objects.DES3SecretKey;
import iaik.pkcs.pkcs11.parameters.InitializationVectorParameters;

/**
 * This demo program uses a PKCS#11 module to encrypt a given file and test
 * if the data can be decrpted.
 *
 * @author <a href="mailto:Karl.Scheibelhofer@iaik.at"> Karl Scheibelhofer </a>
 * @version 0.1
 * @invariants
 */
public class EncryptDecrypt {

    static PrintWriter output_;

    static {
        try {
            output_ = new PrintWriter(System.out, true);
        } catch (Throwable thr) {
            thr.printStackTrace();
            output_ = new PrintWriter(System.out, true);
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            printUsage();
            System.exit(1);
        }
        try {
            Module pkcs11Module = Module.getInstance(args[0]);
            pkcs11Module.initialize(null);
            Slot[] slots = pkcs11Module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
            if (slots.length == 0) {
                output_.println("No slot with present token found!");
                System.exit(0);
            }
            Slot selectedSlot = slots[0];
            Token token = selectedSlot.getToken();
            Session session = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION, null, null);
            session.login(Session.UserType.USER, args[1].toCharArray());
            output_.println("################################################################################");
            output_.println("generate secret encryption/decryption key");
            Mechanism keyMechanism = Mechanism.DES3_KEY_GEN;
            DES3SecretKey secretEncryptionKeyTemplate = new DES3SecretKey();
            secretEncryptionKeyTemplate.getEncrypt().setBooleanValue(Boolean.TRUE);
            secretEncryptionKeyTemplate.getDecrypt().setBooleanValue(Boolean.TRUE);
            DES3SecretKey encryptionKey = (DES3SecretKey) session.generateKey(keyMechanism, secretEncryptionKeyTemplate);
            output_.println("################################################################################");
            output_.println("################################################################################");
            output_.println("encrypting data from file: " + args[2]);
            InputStream dataInputStream = new FileInputStream(args[2]);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream streamBuffer = new ByteArrayOutputStream();
            while ((bytesRead = dataInputStream.read(dataBuffer)) >= 0) {
                streamBuffer.write(dataBuffer, 0, bytesRead);
            }
            Arrays.fill(dataBuffer, (byte) 0);
            streamBuffer.flush();
            streamBuffer.close();
            byte[] rawData = streamBuffer.toByteArray();
            Mechanism encryptionMechanism = Mechanism.DES3_CBC_PAD;
            byte[] encryptInitializationVector = { 0, 0, 0, 0, 0, 0, 0, 0 };
            InitializationVectorParameters encryptInitializationVectorParameters = new InitializationVectorParameters(encryptInitializationVector);
            encryptionMechanism.setParameters(encryptInitializationVectorParameters);
            session.encryptInit(encryptionMechanism, encryptionKey);
            byte[] encryptedData = session.encrypt(rawData);
            output_.println("################################################################################");
            output_.println("################################################################################");
            output_.println("trying to decrypt");
            Mechanism decryptionMechanism = Mechanism.DES3_CBC_PAD;
            byte[] decryptInitializationVector = { 0, 0, 0, 0, 0, 0, 0, 0 };
            InitializationVectorParameters decryptInitializationVectorParameters = new InitializationVectorParameters(decryptInitializationVector);
            decryptionMechanism.setParameters(decryptInitializationVectorParameters);
            session.decryptInit(decryptionMechanism, encryptionKey);
            byte[] decryptedData = session.decrypt(encryptedData);
            boolean equal = false;
            if (rawData.length != decryptedData.length) {
                equal = false;
            } else {
                equal = true;
                for (int i = 0; i < rawData.length; i++) {
                    if (rawData[i] != decryptedData[i]) {
                        equal = false;
                        break;
                    }
                }
            }
            output_.println((equal) ? "successful" : "ERROR");
            output_.println("################################################################################");
            session.closeSession();
            pkcs11Module.finalize(null);
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }

    public static void printUsage() {
        output_.println("Usage: EncryptDecrypt <PKCS#11 module> <user-PIN> <file to be encrypted>");
        output_.println(" e.g.: EncryptDecrypt pk2priv.dll password data.dat");
        output_.println("The given DLL must be in the search path of the system.");
    }
}
