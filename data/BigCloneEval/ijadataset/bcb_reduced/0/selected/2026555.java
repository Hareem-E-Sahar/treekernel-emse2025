package demo.pkcs.pkcs11;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import iaik.pkcs.pkcs11.Mechanism;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;

/**
 * This demo program uses a PKCS#11 module to calculate a hash of a given file.
 * Optionally the calcualted raw hash can be written to file. The program also
 * verifies the calculated hash with a software hash from Java.
 *
 * @author <a href="mailto:Karl.Scheibelhofer@iaik.at"> Karl Scheibelhofer </a>
 * @version 0.1
 * @invariants
 */
public class Digest {

    public static void main(String[] args) {
        if ((args.length != 3) && (args.length != 4)) {
            printUsage();
            System.exit(1);
        }
        try {
            Module pkcs11Module = Module.getInstance(args[0]);
            pkcs11Module.initialize(null);
            Slot[] slots = pkcs11Module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
            if (slots.length == 0) {
                System.out.println("No slot with present token found!");
                System.exit(0);
            }
            Slot selectedSlot = slots[0];
            Token token = selectedSlot.getToken();
            Session session = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION, null, null);
            System.out.println("################################################################################");
            System.out.println("digesting data from file: " + args[2]);
            Mechanism digestMechanism = Mechanism.SHA_1;
            byte[] dataBuffer = new byte[4096];
            byte[] helpBuffer;
            int bytesRead;
            FileInputStream dataInputStream = new FileInputStream(args[2]);
            int updateCounter = 0;
            long t0 = System.currentTimeMillis();
            session.digestInit(digestMechanism);
            while ((bytesRead = dataInputStream.read(dataBuffer)) >= 0) {
                if (bytesRead < dataBuffer.length) {
                    helpBuffer = new byte[bytesRead];
                    System.arraycopy(dataBuffer, 0, helpBuffer, 0, bytesRead);
                    session.digestUpdate(helpBuffer);
                } else {
                    session.digestUpdate(dataBuffer);
                }
                updateCounter++;
            }
            byte[] digestValue = session.digestFinal();
            long t1 = System.currentTimeMillis();
            dataInputStream.close();
            Arrays.fill(dataBuffer, (byte) 0);
            System.out.println("The digest value is: " + new BigInteger(1, digestValue).toString(16));
            System.out.println("Calculation took " + (t1 - t0) + " milliseconds using " + updateCounter + " update calls.");
            if (args.length == 4) {
                System.out.println("Writing digest value to file: " + args[3]);
                FileOutputStream signatureOutput = new FileOutputStream(args[3]);
                signatureOutput.write(digestValue);
                signatureOutput.flush();
                signatureOutput.close();
            }
            System.out.println("################################################################################");
            System.out.println("################################################################################");
            System.out.println("verifying digest with software digest");
            MessageDigest softwareDigestEngine = MessageDigest.getInstance("SHA-1");
            dataInputStream = new FileInputStream(args[2]);
            updateCounter = 0;
            t0 = System.currentTimeMillis();
            while ((bytesRead = dataInputStream.read(dataBuffer)) >= 0) {
                softwareDigestEngine.update(dataBuffer, 0, bytesRead);
                updateCounter++;
            }
            byte[] softwareDigestValue = softwareDigestEngine.digest();
            t1 = System.currentTimeMillis();
            dataInputStream.close();
            Arrays.fill(dataBuffer, (byte) 0);
            System.out.println("The digest value is: " + new BigInteger(1, softwareDigestValue).toString(16));
            System.out.println("Calculation took " + (t1 - t0) + " milliseconds using " + updateCounter + " update calls.");
            if (Arrays.equals(digestValue, softwareDigestValue)) {
                System.out.println("Verified Message Digest successfully");
            } else {
                System.out.println("Verification of Message Digest FAILED");
            }
            System.out.println("################################################################################");
            session.closeSession();
            pkcs11Module.finalize(null);
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }

    public static void printUsage() {
        System.out.println("Usage: Digest <PKCS#11 module> <user-PIN> <file to be digested> [<digest value file>]");
        System.out.println(" e.g.: Digest pk2priv.dll password data.dat digest.bin");
        System.out.println("The given DLL must be in the search path of the system.");
    }
}
