package at.fhjoanneum.aim.sdi.project.ldap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class TestMD5 {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        String sessionid = "test";
        byte[] defaultBytes = sessionid.getBytes();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA");
            algorithm.reset();
            algorithm.update(defaultBytes);
            byte messageDigest[] = algorithm.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            String foo = messageDigest.toString();
            sessionid = hexString + "";
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
    }
}
