package xreplicator.utility.operation;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import xreplicator.utility.XOperationCollector;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kesha78
 * Date: 17.04.2010
 * Time: 17:18:49
 * To change this template use File | Settings | File Templates.
 */
public class Encrypt implements XOperation {

    private static final byte[] raw = { -7, 4, 71, -24, 13, -42, -61, 1, 7, -9, -9, 82, -56, 14, -21, 11 };

    public Encrypt() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public String getName() {
        return "encrypt";
    }

    @Override
    public String getDescription() {
        return "Encrypt a login and password to an access file";
    }

    @Override
    public Map<String, String> getNeededInput() {
        Map<String, String> needed = new LinkedHashMap<String, String>();
        needed.put("login", "User login");
        needed.put("password", "User password");
        needed.put("path_to_file", "Path to encrypt file");
        return needed;
    }

    @Override
    public boolean execute(ArrayList<String> input, XOperationCollector xCol, PrintStream out) {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "Blowfish");
        try {
            Cipher cipher = Cipher.getInstance("Blowfish");
            DataOutputStream outFile = new DataOutputStream(new FileOutputStream(new File(input.get(2))));
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encryptedLogin = cipher.doFinal(input.get(0).getBytes());
            byte[] encryptedPassword = cipher.doFinal(input.get(1).getBytes());
            outFile.write(encryptedLogin.length);
            outFile.write(encryptedPassword.length);
            outFile.write(encryptedLogin);
            outFile.write(encryptedPassword);
            outFile.flush();
            outFile.close();
            out.println("The new access file was created");
        } catch (NoSuchAlgorithmException e) {
            out.println(e);
        } catch (NoSuchPaddingException e) {
            out.println(e);
        } catch (BadPaddingException e) {
            out.println(e);
        } catch (IllegalBlockSizeException e) {
            out.println(e);
        } catch (FileNotFoundException e) {
            out.println(e);
        } catch (InvalidKeyException e) {
            out.println(e);
        } catch (IOException e) {
            out.println(e);
        }
        return true;
    }

    @Override
    public boolean onInterrupt() {
        return true;
    }
}
