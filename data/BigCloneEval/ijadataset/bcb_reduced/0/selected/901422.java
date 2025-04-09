package securus.jssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import com.mindbright.jca.security.KeyPair;
import com.mindbright.jca.security.KeyPairGenerator;
import com.mindbright.jca.security.NoSuchAlgorithmException;
import com.mindbright.jca.security.SecureRandom;
import com.mindbright.ssh2.SSH2Exception;
import com.mindbright.ssh2.SSH2KeyPairFile;
import com.mindbright.ssh2.SSH2PublicKeyFile;
import com.mindbright.util.RandomSeed;

/**
 * This is a simple demo of how to generate public key pairs for use with ssh2.
 * A file containing some definitions of the key must supplied as argument.
 * <p>
 * Usage: <code> java -cp examples.jar examples.GenerateKeyPair
 * <em>definition_file</em>
 * <p>
 * The definition file is a standard Java property file which should
 * contain the following properties:
 * <dl>
 *   <dt>format</dt>
 *   <dd>Format of keyfile. Valid values are <code>openssh</code> and
 * <code>sshinc</code>. The default value is openssh.</dd>
 * 
 * <dt>algorithm</dt>
 * <dd>Which algorithm to generate a key for. Valid valus are <code>RSA</code>
 * and <code>DSA</code>. The default value is RSA.</dd>
 * 
 * <dt>bits</dt>
 * <dd>Number of bits in key. The default value is 1024.</dd>
 * 
 * <dt>password</dt>
 * <dd>Password used to encrypt the private key</dd>
 * 
 * <dt>subject</dt>
 * <dd>String identifying the owner of the key</dd>
 * 
 * <dt>comment</dt>
 * <dd>Key comment</dd>
 * 
 * <dt>keyfile</dt>
 * <dd>Base-name of files to save keys in. The private file will be stored in
 * <em>keyfile</em> and the public key in <em>keyfile</em><code>.pub</code></dd>
 */
public class GenerateKeyPair {

    private String format = "";

    private String algorithm = "";

    private int bits = -1;

    private String keyfile = "";

    private String password = "";

    private String subject = "";

    private String comment = "";

    public GenerateKeyPair(Properties definition) throws Exception {
        super();
        format = definition.getProperty("format");
        algorithm = definition.getProperty("algorithm");
        bits = Integer.parseInt(definition.getProperty("bits"));
        keyfile = definition.getProperty("keyfile");
        password = definition.getProperty("password");
        subject = definition.getProperty("subject");
        comment = definition.getProperty("comment");
    }

    public GenerateKeyPair(String format, String algorithm, int bits, String keyfile, String password, String subject, String comment) {
        super();
        this.format = format;
        this.algorithm = algorithm;
        this.bits = bits;
        this.keyfile = keyfile;
        this.password = password;
        this.subject = subject;
        this.comment = comment;
    }

    /**
	 * Actually generate the key pair
	 * 
	 * @param alg
	 *            algorithm to generate for (RSA or DSA)
	 * @param bits
	 *            key size
	 * @param rand
	 *            random number source
	 */
    public KeyPair generateKeyPair(String alg, int bits, SecureRandom rand) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(alg);
        kpg.initialize(bits, rand);
        return kpg.generateKeyPair();
    }

    /**
	 * Save the given keypair to a file.
	 */
    public void saveKeyPair() throws Exception {
        if (keyfile == null || keyfile.trim().length() == 0) {
            throw new Exception("no 'keyfile' provided");
        }
        SecureRandom rand = createSecureRandom();
        KeyPair kp = generateKeyPair(algorithm, bits, rand);
        boolean sshComFormat = "sshinc".equals(format);
        saveKeyPair(kp, password, keyfile, subject, comment, sshComFormat, rand);
    }

    public void saveKeyPair(KeyPair kp, String passwd, String fileName, String subject, String comment, boolean sshComFormat, SecureRandom rand) throws IOException, SSH2Exception, NoSuchAlgorithmException {
        SSH2PublicKeyFile pkif = new SSH2PublicKeyFile(kp.getPublic(), subject, comment);
        if (!sshComFormat && (passwd == null || passwd.length() == 0)) {
            subject = null;
            comment = null;
        }
        SSH2KeyPairFile kpf = new SSH2KeyPairFile(kp, subject, comment);
        kpf.store(fileName, rand, passwd, sshComFormat);
        pkif.store(fileName + ".pub", sshComFormat);
    }

    /**
	 * Create a random number generator. This implementation uses the system
	 * random device if available to generate good random numbers. Otherwise it
	 * falls back to some low-entropy garbage.
	 */
    private SecureRandom createSecureRandom() {
        byte[] seed;
        File devRandom = new File("/dev/urandom");
        if (devRandom.exists()) {
            RandomSeed rs = new RandomSeed("/dev/urandom", "/dev/urandom");
            seed = rs.getBytesBlocking(20);
        } else {
            seed = RandomSeed.getSystemStateHash();
        }
        return new SecureRandom(seed);
    }

    /**
	 * Run the application
	 */
    public static void main(String[] argv) {
        GenerateKeyPair generateKeyPair = null;
        try {
            if (argv.length == 1) {
                String file = argv[0];
                Properties defaults = new Properties();
                defaults.put("format", "openssh");
                defaults.put("algorithm", "RSA");
                defaults.put("bits", "1024");
                Properties definition = new Properties(defaults);
                definition.load(new FileInputStream(file));
                generateKeyPair = new GenerateKeyPair(definition);
            } else {
                generateKeyPair = new GenerateKeyPair("openssh", "RSA", 1024, "./id_rsa", null, null, "");
            }
            generateKeyPair.saveKeyPair();
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't load file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occured: " + e.getMessage());
        }
    }
}
