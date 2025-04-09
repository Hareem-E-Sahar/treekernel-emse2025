package org.unigrids.ssh;

import java.io.IOException;
import java.rmi.server.UID;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.mindbright.jca.security.KeyPairGenerator;
import com.mindbright.jca.security.KeyPair;
import com.mindbright.jca.security.NoSuchAlgorithmException;
import com.mindbright.ssh.SSH;
import com.mindbright.ssh.SSHInteractiveClient;
import com.mindbright.ssh2.SSH2Exception;
import com.mindbright.ssh2.SSH2KeyPairFile;
import com.mindbright.ssh2.SSH2PublicKeyFile;

/**
 * SSH implementation for mindterm.
 * @author Morris Riedel 10.06.2005
 * @version $Id: SshImplMindTerm.java,v 1.1.1.1 2005/10/06 13:42:53 mri1706 Exp $
 */
public class SshImplMindTerm extends SshImplFunctionalityDefaultImpl {

    private static Logger logger = Logger.getLogger(SshImplMindTerm.class.getName());

    private KeyPair keyPair;

    /**
	 * Stores a keypair as sshKeyFilename.
	 * @param kp
	 * @param sshKeyDirectory
	 * @return
	 * @throws IOException
	 * @throws SSH2Exception
	 * @throws NoSuchAlgorithmException
	 * @author Morris Riedel 10.06.2005
	 */
    private String storeKeyPair(KeyPair kp, String sshKeyFilename) throws IOException, SSH2Exception, NoSuchAlgorithmException {
        String passwd = "";
        String subject = "";
        boolean sshcomformat = false;
        commentGuid = new UID().toString();
        logger.log(Level.FINE, "[SshImplMindTerm] SSH KeyFilename: " + sshKeyFilename);
        SSH2PublicKeyFile publicKeyFile = new SSH2PublicKeyFile(kp.getPublic(), subject, commentGuid);
        logger.log(Level.FINE, "[SshImplMindTerm] Storing public key...");
        publicKeyFile.store(sshKeyFilename + ".pub", sshcomformat);
        logger.log(Level.FINE, "[SshImplMindTerm] Creating private key...");
        SSH2KeyPairFile kpf = new SSH2KeyPairFile(kp, subject, commentGuid);
        logger.log(Level.FINE, "[SshImplMindTerm] Storing private key...");
        kpf.store(sshKeyFilename, SSH.secureRandom(), passwd, sshcomformat);
        rsaPublicKey = readKeyFromFile(sshKeyFilename + ".pub");
        rsaPrivateKey = readKeyFromFile(sshKeyFilename);
        rsaPrivateKeyFile = sshKeyFilename;
        rsaPublicKeyFile = sshKeyFilename + ".pub";
        return rsaPublicKey;
    }

    /**
	 * Creates an 1024 bit RSA key.
	 * @param rsaKeyDirectory directory for the ssh keys
	 * @author Morris Riedel 10.06.2005
	 */
    public String createRSAKey(String rsaKeyDirectory) {
        String uuid = (new UID()).toString();
        uuid = uuid.replace(':', '_');
        String filename = rsaKeyDirectory + uuid;
        String alg = "RSA";
        int bits = 1024;
        String result = "";
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(alg);
            kpg.initialize(bits, SSHInteractiveClient.secureRandom());
            keyPair = kpg.generateKeyPair();
            try {
                result = storeKeyPair(keyPair, filename);
            } catch (Exception e) {
                logger.log(Level.WARNING, "[SshImplMindTerm] Error in storing the keypair: " + e.getMessage() + " " + e.getStackTrace());
            }
        } catch (NoSuchAlgorithmException algException) {
            logger.log(Level.WARNING, "[SshImplMindTerm] Error in rsa key algorithm.");
        }
        return result;
    }
}
