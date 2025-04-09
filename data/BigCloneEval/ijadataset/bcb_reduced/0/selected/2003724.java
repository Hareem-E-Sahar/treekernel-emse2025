package mail.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.Random;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.*;
import mail.EncSupplyException;
import mail.Errors;
import mail.Message;
import mail.node.crypt.CryptEngine;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EncSupplyUI extends JDialog implements mail.EncSupply {

    private static final long serialVersionUID = 6007106132204684061L;

    private static final int OK = 0;

    private static final int CANCEL = 1;

    private static final int OPEN = 2;

    private static final int KEY_GENERATION_SIZE = 1024;

    private static final int KEY_ALIVE = 300000;

    private static final String KEYSTORE = "keys";

    private static final String SALT = "salt";

    File keystore;

    File salt;

    private JPasswordField password;

    private JButton okButton;

    private JLabel message;

    private JEditorPane details;

    private int entered;

    private PublicKey pubkey;

    private PrivateKey privkey;

    public EncSupplyUI(JFrame owner) {
        super(owner);
        init();
    }

    @Deprecated
    public EncSupplyUI() {
        init();
    }

    private void init() {
        getContentPane().setLayout(new BorderLayout());
        JPanel messagePan = new JPanel(new BorderLayout());
        message = new JLabel();
        messagePan.add("North", message);
        messagePan.add("Center", new JLabel("Please enter your password to access your keystore  "));
        password = new JPasswordField(20);
        messagePan.add("South", password);
        getContentPane().add("North", messagePan);
        okButton = new JButton("ok");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                entered = OK;
            }
        });
        JPanel southPan = new JPanel(new BorderLayout());
        southPan.add("South", okButton);
        details = new JEditorPane("text/html", "");
        details.setEditable(false);
        JScrollPane span = new JScrollPane(details);
        span.setPreferredSize(new Dimension(100, 60));
        southPan.add("Center", span);
        getContentPane().add("Center", southPan);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                entered = CANCEL;
            }
        });
        pack();
    }

    private char[] getPassword(Message m) throws EncSupplyException {
        entered = OPEN;
        setVisible(true);
        message.setText(m.getMessage());
        if (m.hasDetails()) details.setText(m.getDetails()); else details.setText("no details available");
        pack();
        while (entered == OPEN) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        if (entered == CANCEL) {
            setVisible(false);
            password.setText("");
            throw new EncSupplyException("User aborted passwort-dialog", null, true);
        } else {
            char[] pw = password.getPassword();
            password.setText("");
            setVisible(false);
            return (pw);
        }
    }

    public PrivateKey getPrivateKey() throws EncSupplyException {
        Message additionalMessage = new PlainMessage("Your private key is required");
        for (int k = 0; k < 3 && pubkey == null; k++) {
            char[] pw = getPassword(additionalMessage);
            try {
                if (pw == null) return (null);
                loadKeys(pw);
            } catch (EncSupplyException e) {
                if (e.isFatal()) throw e; else additionalMessage = e;
            } finally {
                for (int i = 0; i < pw.length; i++) pw[i] = 0;
            }
        }
        if (privkey == null) throw new EncSupplyException("Passwort wrong after 3 attempts", null, true);
        return (privkey);
    }

    public Cipher getStoreCipher() {
        return null;
    }

    public SecretKey getStoreKey() {
        return null;
    }

    private final KeySpec getKeySpec(String s) {
        String[] split = s.split(":");
        String format = split[0];
        byte[] data = CryptEngine.fromHex(split[1]);
        EncodedKeySpec kspec = null;
        if (format.equals("X.509")) kspec = new X509EncodedKeySpec(data); else if (format.equals("PKCS#8")) kspec = new PKCS8EncodedKeySpec(data); else {
            System.err.println("not supported Key!");
        }
        return (kspec);
    }

    private final byte[] getSalt() throws IOException {
        byte[] out = new byte[8];
        FileInputStream fin;
        fin = new FileInputStream(salt);
        fin.read(out);
        fin.close();
        return (out);
    }

    private final byte[] generateSalt() throws IOException {
        byte[] out = new byte[8];
        Random r = new Random();
        r.nextBytes(out);
        FileOutputStream fout;
        fout = new FileOutputStream(salt);
        fout.write(out);
        fout.close();
        return (out);
    }

    private final void loadKeys(char[] pw) throws EncSupplyException {
        PBEKeySpec spec = null;
        Properties p = new Properties();
        CipherInputStream cis = null;
        try {
            Cipher c = Cipher.getInstance("PBEWithMD5AndDES");
            SecretKeyFactory skfactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            PBEParameterSpec pps = new PBEParameterSpec(getSalt(), 11);
            spec = new PBEKeySpec(pw);
            SecretKey sk = skfactory.generateSecret(spec);
            c.init(Cipher.DECRYPT_MODE, sk, pps);
            cis = new CipherInputStream(new FileInputStream(keystore), c);
            try {
                p.load(cis);
            } catch (Exception e) {
                throw new EncSupplyException("Unable to read keystore! Password wrong?", "<html>Please also check, if the file is readable,<br> e.g. if you have the required permissions</html>", e, false);
            }
            String publickey = p.getProperty("pubkey");
            String privatekey = p.getProperty("privkey");
            String algorithm = p.getProperty("algorithm");
            if (publickey != null && privatekey != null && algorithm != null) {
                KeySpec pubspec = getKeySpec(publickey);
                KeySpec privspec = getKeySpec(privatekey);
                try {
                    KeyFactory factory = KeyFactory.getInstance(algorithm);
                    pubkey = factory.generatePublic(pubspec);
                    privkey = factory.generatePrivate(privspec);
                } catch (Exception e) {
                    throw new EncSupplyException("Unable to parse keys! Password wrong?", "<html>The keys might also be saved in the wrong format.<br>If this is the case, please submit a Bug report</html>", e, false);
                }
                startCounter();
            } else {
                throw new EncSupplyException("Contains no keys! Password wrong?", "<html>There might be also a different problem, <br>but this is very improbably. If it is please submit a bugreport</html>", null, false);
            }
            publickey = null;
            privatekey = null;
        } catch (NoSuchAlgorithmException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } catch (NoSuchPaddingException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } catch (InvalidKeyException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } catch (InvalidKeySpecException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } catch (FileNotFoundException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } catch (IOException e) {
            throw new EncSupplyException("unable to load Keystore", e, true);
        } finally {
            if (spec != null) spec.clearPassword();
            if (cis != null) try {
                cis.close();
            } catch (IOException e) {
                Errors.addException("Unable to close Cipher-Stream", e);
            }
        }
    }

    private final void storeKeys(char[] pw) throws EncSupplyException {
        PBEKeySpec spec = null;
        Properties p = new Properties();
        CipherOutputStream cos = null;
        try {
            p.put("algorithm", pubkey.getAlgorithm());
            p.put("pubkey", pubkey.getFormat() + ":" + CryptEngine.toHex(pubkey.getEncoded()));
            p.put("privkey", privkey.getFormat() + ":" + CryptEngine.toHex(privkey.getEncoded()));
            Cipher c = Cipher.getInstance("PBEWithMD5AndDES");
            SecretKeyFactory skfactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            PBEParameterSpec pps = new PBEParameterSpec(generateSalt(), 11);
            spec = new PBEKeySpec(pw);
            SecretKey sk = skfactory.generateSecret(spec);
            c.init(Cipher.ENCRYPT_MODE, sk, pps);
            cos = new CipherOutputStream(new FileOutputStream(keystore), c);
            p.store(cos, "keystore");
        } catch (IOException e) {
            throw new EncSupplyException("Problem writing Keystore, Keys are not saved!", e, false);
        } catch (NoSuchAlgorithmException e) {
            throw new EncSupplyException("Problem creating Keystore, Keys are not saved!", e, false);
        } catch (NoSuchPaddingException e) {
            throw new EncSupplyException("Problem creating Keystore, Keys are not saved!", e, false);
        } catch (InvalidKeyException e) {
            throw new EncSupplyException("Problem creating Keystore, Keys are not saved!", e, false);
        } catch (InvalidKeySpecException e) {
            throw new EncSupplyException("Problem creating Keystore, Keys are not saved!", e, false);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncSupplyException("Problem creating Keystore, Keys are not saved!", e, false);
        } finally {
            p.put("pubkey", "..........................................................................................................................................................");
            p.put("privkey", ".........................................................................................................................................................");
            p.clear();
            if (spec != null) spec.clearPassword();
            if (cos != null) try {
                cos.close();
            } catch (IOException e) {
                Errors.addException("Unable to close Cipher-Stream", e);
            }
        }
    }

    private final void startCounter() {
        Thread t = new Thread() {

            public void run() {
                try {
                    Thread.sleep(KEY_ALIVE);
                } catch (InterruptedException e) {
                }
                privkey = null;
                pubkey = null;
            }
        };
        t.start();
    }

    public void setKeyStore(File basedir) throws EncSupplyException {
        keystore = new File(basedir, KEYSTORE);
        salt = new File(basedir, SALT);
        char[] pw = new char[0];
        try {
            if (!keystore.exists()) {
                pw = getPassword(new PlainMessage("Creating your keystore", "<html>The KeyStore is used to store<br>your keys you use to identify <br> and encrypt messages with</html>"));
                if (pw == null) return;
                KeyPairGenerator kpgen;
                kpgen = KeyPairGenerator.getInstance("RSA");
                kpgen.initialize(KEY_GENERATION_SIZE);
                KeyPair kp = kpgen.generateKeyPair();
                pubkey = kp.getPublic();
                privkey = kp.getPrivate();
                storeKeys(pw);
                startCounter();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new EncSupplyException("Keys weren't able to be created", e, true);
        } finally {
            for (int i = 0; i < pw.length; i++) pw[i] = 0;
        }
    }

    public PublicKey getPublicKey() throws EncSupplyException {
        Message additionalMessage = new PlainMessage("Your public key is required");
        for (int k = 0; k < 3 && pubkey == null; k++) {
            char[] pw = getPassword(additionalMessage);
            try {
                loadKeys(pw);
            } catch (EncSupplyException e) {
                if (e.isFatal()) throw e; else additionalMessage = e;
            } finally {
                for (int i = 0; i < pw.length; i++) pw[i] = 0;
            }
        }
        if (pubkey == null) throw new EncSupplyException("Passwort wrong after 3 attempts", null, true);
        return (pubkey);
    }

    private class PlainMessage implements Message {

        String details;

        String message;

        public PlainMessage(String message) {
            this.message = message;
            details = null;
        }

        public PlainMessage(String message, String details) {
            this.message = message;
            this.details = details;
        }

        public String getDetails() {
            return details;
        }

        public String getMessage() {
            return message;
        }

        public boolean hasDetails() {
            return details != null;
        }
    }
}
