package base.testing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import crypto.password.Hash;
import crypto.password.SHA512Matrix;
import database.SentinelDatabase;
import objects.BatchSettings;

public class TestPasswordMatrix {

    public static void DoTest() throws NoSuchAlgorithmException {
        MessageDigest Digest = MessageDigest.getInstance("SHA-512");
        String Tmp = Hash.convertToHex((Digest.digest("Test".getBytes())));
        System.out.println("Test Hash: " + Tmp);
        System.out.println("Test Hash Length: " + Tmp.length());
        byte[] t3 = Digest.digest("Test".getBytes());
        System.out.println("Password Byte Array length: " + t3.length);
        SHA512Matrix tMatrix = new SHA512Matrix(t3);
        tMatrix.print();
    }
}
