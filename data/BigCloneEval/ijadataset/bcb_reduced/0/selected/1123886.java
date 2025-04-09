package justsftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

public class Connection implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String userName;

    private String host;

    private int port;

    private String protocol;

    private String defaultDir;

    private byte[] pwd;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDefaultDir() {
        return defaultDir;
    }

    public void setDefaultDir(String defaultDir) {
        this.defaultDir = defaultDir;
    }

    public byte[] getPassword() {
        byte[] pawd = null;
        try {
            Cipher cipher = Cipher.getInstance("DES");
            Key key = getKey();
            if (key == null) {
                return null;
            }
            cipher.init(Cipher.DECRYPT_MODE, key);
            pawd = cipher.doFinal(pwd);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pawd;
    }

    public void setPwd(byte[] pwd) {
        try {
            Cipher cipher = Cipher.getInstance("DES");
            Key key = getKey();
            if (key == null) {
                key = KeyGenerator.getInstance("DES").generateKey();
                writeKey(key);
            }
            cipher.init(Cipher.ENCRYPT_MODE, key);
            pwd = cipher.doFinal(pwd);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pwd = pwd;
    }

    private static void writeKey(Key key) {
        File file = new File(Constants.TEMP_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(Constants.TEMP_DIR + "Key.key");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Key getKey() throws IOException {
        File file = new File(Constants.TEMP_DIR + "Key.key");
        if (!file.exists()) {
            return null;
        }
        FileInputStream fos = new FileInputStream(file);
        ObjectInputStream oos = new ObjectInputStream(fos);
        try {
            return (Key) oos.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
