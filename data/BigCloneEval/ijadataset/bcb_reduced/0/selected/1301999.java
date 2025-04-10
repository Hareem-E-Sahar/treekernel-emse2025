package org.jgroups.protocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.Global;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.annotations.Property;
import org.jgroups.stack.Protocol;
import org.jgroups.util.QueueClosedException;
import org.jgroups.util.Util;

/**
 * ENCRYPT layer. Encrypt and decrypt the group communication in JGroups
 * 
 * The file can be used in two ways:
 * <ul>
 * <li> Option 1. Configured with a secretKey in a keystore so it can be used at
 * any layer in JGroups without the need for a coordinator, or if you want
 * protection against passive monitoring but do not want the key exchange
 * overhead and complexity. In this mode all nodes must be distributed with the
 * same keystore file.
 * <li> Option 2. Configured with algorithms and key sizes. The Encrypt Layer in
 * this mode sould be used between the FRAG and PBCast layers in the stack. The
 * coordinator then chooses the secretkey which it distributes amongst all the
 * peers. In this form no keystore exists as the keys are distributed using a
 * public/private key exchange. View changes that identify a new controller will
 * result in a new session key being generated and then distributed to all
 * peers. This overhead can be substantial in a an application with a reasonable
 * peer churn.
 * </ul>
 * <p>
 * <p>
 * Each message is identified as encrypted with a specific encryption header
 * which identifies the type of encrypt header and an MD5 digest that identifies
 * the version of the key being used to encrypt/decrypt the messages.
 * <p>
 * <p>
 * <h2>Option 1</h2>
 * <br>
 * This is the simplest option and can be used by simply inserting the
 * Encryption layer at any point in the JGroup stack - it will encrypt all
 * Events of a type MSG that have a non-null message buffer. The format of the
 * entry in this form is:<br>
 * &lt;ENCRYPT key_store_name="defaultStore.keystore" store_password="changeit"
 * alias="myKey"/&gt;<br>
 * An example bare-bones.xml file showing the keystore version can be found in
 * the conf ina file called EncryptKeyStore.xml - along with a
 * defaultStore.keystore file.<br>
 * In order to use the Encrypt layer in this manner it is necessary to have the
 * secretKey already generated in a keystore file. The directory containing the
 * keystore file must be on the application's classpath. You cannot create a
 * SecretKey keystore file using the keytool application shipped with the JDK. A
 * java file called KeyStoreGenerator is included in the demo package that can
 * be used from the command line (or IDE) to generate a suitable keystore.
 * <p>
 * <p>
 * <h2>Option 2</h2>
 * <br>
 * This option is suited to an application that does not ship with a known key
 * but instead it is generated and distributed by the controller. The secret key
 * is first generated by the Controller (in JGroup terms). When a view change
 * occurs a peer will request the secret key by sending a key request with its
 * own public key. The controller encrypts the secret key with this key and
 * sends it back to the peer who then decrypts it and installs the key as its
 * own secret key. <br>
 * All encryption and decryption of Messages is done using this key. When a peer
 * receives a view change that shows a different keyserver it will repeat this
 * process - the view change event also trigger the encrypt layer to queue up
 * and down messages until the new key is installed. The previous keys are
 * retained so that messages sent before the view change that are queued can be
 * decrypted if the key is different. <br>
 * An example EncryptNoKeyStore.xml is included in the conf file as a guide.
 * <p>
 * <p>
 * <br>
 * Note: the current version does not support the concept of perfect forward
 * encryption (PFE) which means that if a peer leaves the group the keys are
 * re-generated preventing the departed peer from decrypting future messages if
 * it chooses to listen in on the group. This is not included as it really
 * requires a suitable authentication scheme as well to make this feature useful
 * as there is nothing to stop the peer rejoining and receiving the new key. A
 * future release will address this issue.
 * 
 * @author Steve Woodcock
 * @author Bela Ban
 */
public class ENCRYPT extends Protocol {

    Observer observer;

    interface Observer {

        void up(Event evt);

        void passUp(Event evt);

        void down(Event evt);

        void passDown(Event evt);
    }

    private static final String DEFAULT_SYM_ALGO = "AES";

    Address local_addr = null;

    Address keyServerAddr = null;

    boolean keyServer = false;

    @Property(name = "asym_provider", description = "Cryptographic Service Provider. Default is Bouncy Castle Provider")
    String asymProvider = null;

    static final String symProvider = null;

    @Property(name = "asym_algorithm", description = "Cipher engine transformation for asymmetric algorithm. Default is RSA")
    String asymAlgorithm = "RSA";

    @Property(name = "sym_algorithm", description = "Cipher engine transformation for symmetric algorithm. Default is AES")
    String symAlgorithm = DEFAULT_SYM_ALGO;

    @Property(name = "asym_init", description = "Initial public/private key length. Default is 512")
    int asymInit = 512;

    @Property(name = "sym_init", description = "Initial key length for matching symmetric algorithm. Default is 128")
    int symInit = 128;

    private boolean suppliedKey = false;

    @Property(name = "key_store_name", description = "File on classpath that contains keystore repository")
    String keyStoreName;

    @Property(name = "store_password", description = "Password used to check the integrity/unlock the keystore. Change the default")
    private String storePassword = "changeit";

    @Property(name = "key_password", description = "Password for recovering the key. Change the default")
    private String keyPassword = "changeit";

    @Property(name = "alias", description = "Alias used for recovering the key. Change the default")
    private String alias = "mykey";

    KeyPair Kpair;

    PublicKey serverPubKey = null;

    Cipher symEncodingCipher;

    Cipher symDecodingCipher;

    private String symVersion = null;

    SecretKey secretKey = null;

    final Map<String, Cipher> keyMap = new WeakHashMap<String, Cipher>();

    private boolean queue_up = true;

    private boolean queue_down = false;

    private BlockingQueue<Event> upMessageQueue = new LinkedBlockingQueue<Event>();

    private BlockingQueue<Event> downMessageQueue = new LinkedBlockingQueue<Event>();

    private Cipher asymCipher;

    /** determines whether to encrypt the entire message, or just the buffer */
    @Property
    private boolean encrypt_entire_message = false;

    public ENCRYPT() {
    }

    public void setObserver(Observer o) {
        observer = o;
    }

    private static String getAlgorithm(String s) {
        int index = s.indexOf("/");
        if (index == -1) return s;
        return s.substring(0, index);
    }

    public void init() throws Exception {
        if (keyPassword == null && storePassword != null) {
            keyPassword = storePassword;
            if (log.isInfoEnabled()) log.info("key_password used is same as store_password");
        }
        if (keyStoreName == null) {
            initSymKey();
            initKeyPair();
        } else {
            initConfiguredKey();
        }
        initSymCiphers(symAlgorithm, getSecretKey());
    }

    /**
     * Initialisation if a supplied key is defined in the properties. This
     * supplied key must be in a keystore which can be generated using the
     * keystoreGenerator file in demos. The keystore must be on the classpath to
     * find it.
     * 
     * @throws KeyStoreException
     * @throws Exception
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     */
    private void initConfiguredKey() throws Exception {
        InputStream inputStream = null;
        KeyStore store = KeyStore.getInstance("JCEKS");
        SecretKey tempKey = null;
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreName);
            if (inputStream == null) {
                throw new Exception("Unable to load keystore " + keyStoreName + " ensure file is on classpath");
            }
            try {
                store.load(inputStream, storePassword.toCharArray());
                tempKey = (SecretKey) store.getKey(alias, keyPassword.toCharArray());
            } catch (IOException e) {
                throw new Exception("Unable to load keystore " + keyStoreName + ": " + e);
            } catch (NoSuchAlgorithmException e) {
                throw new Exception("No Such algorithm " + keyStoreName + ": " + e);
            } catch (CertificateException e) {
                throw new Exception("Certificate exception " + keyStoreName + ": " + e);
            }
            if (tempKey == null) {
                throw new Exception("Unable to retrieve key '" + alias + "' from keystore " + keyStoreName);
            }
            setSecretKey(tempKey);
            if (symAlgorithm.equals(DEFAULT_SYM_ALGO)) {
                symAlgorithm = tempKey.getAlgorithm();
            }
            suppliedKey = true;
            queue_down = false;
            queue_up = false;
        } finally {
            Util.close(inputStream);
        }
    }

    /**
     * Used to initialise the symmetric key if none is supplied in a keystore.
     * 
     * @throws Exception
     */
    public void initSymKey() throws Exception {
        KeyGenerator keyGen = null;
        if (symProvider != null && symProvider.trim().length() > 0) {
            keyGen = KeyGenerator.getInstance(getAlgorithm(symAlgorithm), symProvider);
        } else {
            keyGen = KeyGenerator.getInstance(getAlgorithm(symAlgorithm));
        }
        keyGen.init(symInit);
        secretKey = keyGen.generateKey();
        setSecretKey(secretKey);
        if (log.isInfoEnabled()) log.info(" Symmetric key generated ");
    }

    /**
     * Initialises the Ciphers for both encryption and decryption using the
     * generated or supplied secret key.
     * 
     * @param algorithm
     * @param secret
     * @throws Exception
     */
    private void initSymCiphers(String algorithm, SecretKey secret) throws Exception {
        if (log.isInfoEnabled()) log.info(" Initializing symmetric ciphers");
        symEncodingCipher = Cipher.getInstance(algorithm);
        symDecodingCipher = Cipher.getInstance(algorithm);
        symEncodingCipher.init(Cipher.ENCRYPT_MODE, secret);
        symDecodingCipher.init(Cipher.DECRYPT_MODE, secret);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.reset();
        digest.update(secret.getEncoded());
        symVersion = new String(digest.digest(), "UTF-8");
        if (log.isInfoEnabled()) {
            log.info(" Initialized symmetric ciphers with secret key (" + symVersion.length() + " bytes)");
        }
    }

    /**
     * Generates the public/private key pair from the init params
     * 
     * @throws Exception
     */
    public void initKeyPair() throws Exception {
        KeyPairGenerator KpairGen = null;
        if (asymProvider != null && asymProvider.trim().length() > 0) {
            KpairGen = KeyPairGenerator.getInstance(getAlgorithm(asymAlgorithm), asymProvider);
        } else {
            KpairGen = KeyPairGenerator.getInstance(getAlgorithm(asymAlgorithm));
        }
        KpairGen.initialize(asymInit, new SecureRandom());
        Kpair = KpairGen.generateKeyPair();
        asymCipher = Cipher.getInstance(asymAlgorithm);
        asymCipher.init(Cipher.DECRYPT_MODE, Kpair.getPrivate());
        if (log.isInfoEnabled()) log.info(" asym algo initialized");
    }

    /** Just remove if you don't need to reset any state */
    public void reset() {
    }

    public Object up(Event evt) {
        switch(evt.getType()) {
            case Event.VIEW_CHANGE:
                View view = (View) evt.getArg();
                if (log.isInfoEnabled()) log.info("handling view-change up: " + view);
                if (!suppliedKey) {
                    handleViewChange(view, false);
                }
                break;
            case Event.TMP_VIEW:
                view = (View) evt.getArg();
                if (log.isInfoEnabled()) log.info("handling tmp-view up: " + view);
                if (!suppliedKey) {
                    handleViewChange(view, true);
                }
                break;
            case Event.MSG:
                try {
                    handleUpMessage(evt);
                } catch (Exception e) {
                    log.warn("exception occurred decrypting message", e);
                }
                return null;
            default:
                break;
        }
        return passItUp(evt);
    }

    public Object passItUp(Event evt) {
        if (observer != null) observer.passUp(evt);
        return up_prot != null ? up_prot.up(evt) : null;
    }

    private synchronized void handleViewChange(View view, boolean makeServer) {
        Vector<Address> members = view.getMembers();
        if (members == null || members.isEmpty() || members.get(0) == null) {
            becomeKeyServer(local_addr);
            return;
        }
        Address tmpKeyServer = view.getMembers().get(0);
        if (makeServer || (tmpKeyServer.equals(local_addr) && (keyServerAddr == null || (!tmpKeyServer.equals(keyServerAddr))))) {
            becomeKeyServer(tmpKeyServer);
        } else if (keyServerAddr == null || (!tmpKeyServer.equals(keyServerAddr))) {
            handleNewKeyServer(tmpKeyServer);
        } else {
            if (log.isDebugEnabled()) log.debug("Membership has changed but I do not care");
        }
    }

    /**
     * Handles becoming server - resetting queue settings and setting keyserver
     * address to be local address.
     * 
     * @param tmpKeyServer
     */
    private void becomeKeyServer(Address tmpKeyServer) {
        keyServerAddr = tmpKeyServer;
        keyServer = true;
        if (log.isInfoEnabled()) log.info("I have become key server " + keyServerAddr);
        queue_down = false;
        queue_up = false;
    }

    /**
     * Sets up the peer for a new keyserver - this is setting queueing to buffer
     * messages until we have a new secret key from the key server and sending a
     * key request to the new keyserver.
     * 
     * @param newKeyServer
     */
    private void handleNewKeyServer(Address newKeyServer) {
        queue_up = true;
        queue_down = true;
        keyServerAddr = newKeyServer;
        keyServer = false;
        if (log.isInfoEnabled()) log.info("Sending key request");
        sendKeyRequest();
    }

    /**
     * @param evt
     */
    private void handleUpMessage(Event evt) throws Exception {
        Message msg = (Message) evt.getArg();
        if (msg == null) {
            if (log.isTraceEnabled()) log.trace("null message - passing straight up");
            passItUp(evt);
            return;
        }
        if (msg.getLength() == 0) {
            passItUp(evt);
            return;
        }
        EncryptHeader hdr = (EncryptHeader) msg.getHeader(this.id);
        if (hdr == null) {
            if (log.isTraceEnabled()) log.trace("dropping message as ENCRYPT header is null  or has not been recognized, msg will not be passed up, " + "headers are " + msg.printHeaders());
            return;
        }
        if (log.isTraceEnabled()) log.trace("header received " + hdr);
        if (hdr.getType() == EncryptHeader.ENCRYPT) {
            if (!hdr.encrypt_entire_msg && ((Message) evt.getArg()).getLength() == 0) {
                if (log.isTraceEnabled()) log.trace("passing up message as it has an empty buffer ");
                passItUp(evt);
                return;
            }
            if (queue_up) {
                if (log.isTraceEnabled()) log.trace("queueing up message as no session key established: " + evt.getArg());
                upMessageQueue.put(evt);
            } else {
                if (!suppliedKey) {
                    drainUpQueue();
                }
                Message tmpMsg = decryptMessage(symDecodingCipher, msg.copy());
                if (tmpMsg != null) {
                    if (log.isTraceEnabled()) log.trace("decrypted message " + tmpMsg);
                    passItUp(new Event(Event.MSG, tmpMsg));
                } else {
                    log.warn("Unrecognised cipher discarding message");
                }
            }
        } else {
            if (suppliedKey) {
                if (log.isWarnEnabled()) {
                    log.warn("We received an encrypt header of " + hdr.getType() + " while in configured mode");
                }
            } else {
                switch(hdr.getType()) {
                    case EncryptHeader.KEY_REQUEST:
                        if (log.isInfoEnabled()) {
                            log.info("received a key request from peer");
                        }
                        try {
                            PublicKey tmpKey = generatePubKey(msg.getBuffer());
                            sendSecretKey(getSecretKey(), tmpKey, msg.getSrc());
                        } catch (Exception e) {
                            log.warn("unable to reconstitute peer's public key");
                        }
                        break;
                    case EncryptHeader.SECRETKEY:
                        if (log.isInfoEnabled()) {
                            log.info("received a secretkey response from keyserver");
                        }
                        try {
                            SecretKey tmp = decodeKey(msg.getBuffer());
                            if (tmp == null) {
                                sendKeyRequest();
                            } else {
                                setKeys(tmp, hdr.getVersion());
                                if (log.isInfoEnabled()) {
                                    log.info("Decoded secretkey response");
                                }
                            }
                        } catch (Exception e) {
                            log.warn("unable to process received public key");
                        }
                        break;
                    default:
                        log.warn("Received ignored encrypt header of " + hdr.getType());
                        break;
                }
            }
        }
    }

    /**
     * used to drain the up queue - synchronized so we can call it safely
     * despite access from potentially two threads at once
     * 
     * @throws QueueClosedException
     * @throws Exception
     */
    private void drainUpQueue() throws Exception {
        Event tmp = null;
        while ((tmp = upMessageQueue.poll(0L, TimeUnit.MILLISECONDS)) != null) {
            Message msg = decryptMessage(symDecodingCipher, ((Message) tmp.getArg()).copy());
            if (msg != null) {
                if (log.isTraceEnabled()) {
                    log.trace("passing up message from drain " + msg);
                }
                passItUp(new Event(Event.MSG, msg));
            } else {
                log.warn("discarding message in queue up drain as cannot decode it");
            }
        }
    }

    /**
     * Sets the keys for the app. and drains the queues - the drains could be
     * called att he same time as the up/down messages calling in to the class
     * so we may have an extra call to the drain methods but this slight expense
     * is better than the alternative of waiting until the next message to
     * trigger the drains which may never happen.
     * 
     * @param key
     * @param version
     * @throws Exception
     */
    private void setKeys(SecretKey key, String version) throws Exception {
        keyMap.put(getSymVersion(), getSymDecodingCipher());
        setSecretKey(key);
        initSymCiphers(key.getAlgorithm(), key);
        setSymVersion(version);
        log.info("setting queue up to false in setKeys");
        queue_up = false;
        drainUpQueue();
        queue_down = false;
        drainDownQueue();
    }

    /**
     * Does the actual work for decrypting - if version does not match current
     * cipher then tries to use previous cipher
     * 
     * @param cipher
     * @param msg
     * @return
     * @throws Exception
     */
    private Message decryptMessage(Cipher cipher, Message msg) throws Exception {
        EncryptHeader hdr = (EncryptHeader) msg.getHeader(this.id);
        if (!hdr.getVersion().equals(getSymVersion())) {
            log.warn("attempting to use stored cipher as message does not uses current encryption version ");
            cipher = keyMap.get(hdr.getVersion());
            if (cipher == null) {
                log.warn("Unable to find a matching cipher in previous key map");
                return null;
            } else {
                if (log.isTraceEnabled()) log.trace("decrypting using previous cipher version " + hdr.getVersion());
                return _decrypt(cipher, msg, hdr.encrypt_entire_msg);
            }
        } else {
            return _decrypt(cipher, msg, hdr.encrypt_entire_msg);
        }
    }

    private static Message _decrypt(Cipher cipher, Message msg, boolean decrypt_entire_msg) throws Exception {
        if (!decrypt_entire_msg) {
            msg.setBuffer(cipher.doFinal(msg.getRawBuffer(), msg.getOffset(), msg.getLength()));
            return msg;
        }
        byte[] decrypted_msg = cipher.doFinal(msg.getRawBuffer(), msg.getOffset(), msg.getLength());
        Message ret = (Message) Util.streamableFromByteBuffer(Message.class, decrypted_msg);
        if (ret.getDest() == null) ret.setDest(msg.getDest());
        if (ret.getSrc() == null) ret.setSrc(msg.getSrc());
        return ret;
    }

    /**
     * @param secret
     * @param pubKey
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private void sendSecretKey(SecretKey secret, PublicKey pubKey, Address source) throws InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Message newMsg;
        if (log.isDebugEnabled()) log.debug("encoding shared key ");
        Cipher tmp = Cipher.getInstance(asymAlgorithm);
        tmp.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encryptedKey = tmp.doFinal(secret.getEncoded());
        if (log.isDebugEnabled()) log.debug(" Generated encoded key which only client can decode:" + formatArray(encryptedKey));
        newMsg = new Message(source, local_addr, encryptedKey);
        newMsg.putHeader(this.id, new EncryptHeader(EncryptHeader.SECRETKEY, getSymVersion()));
        if (log.isDebugEnabled()) log.debug(" Sending version " + getSymVersion() + " encoded key to client");
        passItDown(new Event(Event.MSG, newMsg));
    }

    /**
     * @return Message
     */
    private Message sendKeyRequest() {
        Message newMsg = new Message(keyServerAddr, local_addr, Kpair.getPublic().getEncoded());
        newMsg.putHeader(this.id, new EncryptHeader(EncryptHeader.KEY_REQUEST, getSymVersion()));
        passItDown(new Event(Event.MSG, newMsg));
        return newMsg;
    }

    public Object down(Event evt) {
        if (observer != null) observer.down(evt);
        switch(evt.getType()) {
            case Event.MSG:
                try {
                    if (queue_down) {
                        if (log.isTraceEnabled()) log.trace("queueing down message as no session key established" + evt.getArg());
                        downMessageQueue.put(evt);
                    } else {
                        if (!suppliedKey) {
                            drainDownQueue();
                        }
                        sendDown(evt);
                    }
                } catch (Exception e) {
                    log.warn("unable to send down event " + e);
                }
                return null;
            case Event.VIEW_CHANGE:
                View view = (View) evt.getArg();
                if (log.isInfoEnabled()) log.info("handling view-change down: " + view);
                if (!suppliedKey) {
                    handleViewChange(view, false);
                }
                break;
            case Event.SET_LOCAL_ADDRESS:
                local_addr = (Address) evt.getArg();
                if (log.isDebugEnabled()) log.debug("set local address to " + local_addr);
                break;
            case Event.TMP_VIEW:
                view = (View) evt.getArg();
                if (log.isInfoEnabled()) log.info("handling tmp-view down: " + view);
                if (!suppliedKey) {
                    handleViewChange(view, true);
                }
                break;
            default:
                break;
        }
        return down_prot.down(evt);
    }

    public Object passItDown(Event evt) {
        if (observer != null) observer.passDown(evt);
        return down_prot != null ? down_prot.down(evt) : null;
    }

    /**
     * @throws Exception
     * @throws QueueClosedException
     */
    private void drainDownQueue() throws Exception {
        Event tmp = null;
        while ((tmp = downMessageQueue.poll(0L, TimeUnit.MILLISECONDS)) != null) {
            sendDown(tmp);
        }
    }

    /**
     * @param evt
     * @throws Exception
     */
    private void sendDown(Event evt) throws Exception {
        if (evt.getType() != Event.MSG) {
            return;
        }
        Message msg = (Message) evt.getArg();
        if (msg.getLength() == 0) {
            passItDown(evt);
            return;
        }
        EncryptHeader hdr = new EncryptHeader(EncryptHeader.ENCRYPT, getSymVersion());
        hdr.encrypt_entire_msg = this.encrypt_entire_message;
        if (encrypt_entire_message) {
            byte[] serialized_msg = Util.streamableToByteBuffer(msg);
            byte[] encrypted_msg = encryptMessage(symEncodingCipher, serialized_msg, 0, serialized_msg.length);
            Message tmp = msg.copy(false);
            tmp.setBuffer(encrypted_msg);
            tmp.setSrc(local_addr);
            tmp.putHeader(this.id, hdr);
            passItDown(new Event(Event.MSG, tmp));
            return;
        }
        msg.putHeader(this.id, hdr);
        Message msgEncrypted = msg.copy(false);
        msgEncrypted.setBuffer(encryptMessage(symEncodingCipher, msg.getRawBuffer(), msg.getOffset(), msg.getLength()));
        passItDown(new Event(Event.MSG, msgEncrypted));
    }

    /**
     * 
     * @param cipher
     * @param plain
     * @return
     * @throws Exception
     */
    private static byte[] encryptMessage(Cipher cipher, byte[] plain, int offset, int length) throws Exception {
        return cipher.doFinal(plain, offset, length);
    }

    private SecretKeySpec decodeKey(byte[] encodedKey) throws Exception {
        byte[] keyBytes = asymCipher.doFinal(encodedKey);
        SecretKeySpec keySpec = null;
        try {
            keySpec = new SecretKeySpec(keyBytes, getAlgorithm(symAlgorithm));
            Cipher temp = Cipher.getInstance(symAlgorithm);
            temp.init(Cipher.SECRET_KEY, keySpec);
        } catch (Exception e) {
            log.fatal(e.toString());
            keySpec = null;
        }
        return keySpec;
    }

    /**
     * used to reconstitute public key sent in byte form from peer
     * 
     * @param encodedKey
     * @return PublicKey
     */
    private PublicKey generatePubKey(byte[] encodedKey) {
        PublicKey pubKey = null;
        try {
            KeyFactory KeyFac = KeyFactory.getInstance(getAlgorithm(asymAlgorithm));
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encodedKey);
            pubKey = KeyFac.generatePublic(x509KeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pubKey;
    }

    private static String formatArray(byte[] array) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            buf.append(Integer.toHexString(array[i]));
        }
        return buf.toString();
    }

    /**
     * @return Returns the asymInit.
     */
    protected int getAsymInit() {
        return asymInit;
    }

    /**
     * @return Returns the asymProvider.
     */
    protected String getAsymProvider() {
        return asymProvider;
    }

    /**
     * @return Returns the desKey.
     */
    protected SecretKey getDesKey() {
        return secretKey;
    }

    /**
     * @return Returns the kpair.
     */
    protected KeyPair getKpair() {
        return Kpair;
    }

    /**
     * @return Returns the asymCipher.
     */
    protected Cipher getAsymCipher() {
        return asymCipher;
    }

    /**
     * @return Returns the serverPubKey.
     */
    protected PublicKey getServerPubKey() {
        return serverPubKey;
    }

    /**
     * @return Returns the symAlgorithm.
     */
    protected String getSymAlgorithm() {
        return symAlgorithm;
    }

    /**
     * @return Returns the symInit.
     */
    protected int getSymInit() {
        return symInit;
    }

    /**
     * @return Returns the symProvider.
     */
    protected static String getSymProvider() {
        return symProvider;
    }

    /**
     * @return Returns the asymAlgorithm.
     */
    protected String getAsymAlgorithm() {
        return asymAlgorithm;
    }

    /**
     * @return Returns the symVersion.
     */
    private String getSymVersion() {
        return symVersion;
    }

    /**
     * @param symVersion
     *                The symVersion to set.
     */
    private void setSymVersion(String symVersion) {
        this.symVersion = symVersion;
    }

    /**
     * @return Returns the secretKey.
     */
    private SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey
     *                The secretKey to set.
     */
    private void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @return Returns the keyStoreName.
     */
    protected String getKeyStoreName() {
        return keyStoreName;
    }

    /**
     * @return Returns the symDecodingCipher.
     */
    protected Cipher getSymDecodingCipher() {
        return symDecodingCipher;
    }

    /**
     * @return Returns the symEncodingCipher.
     */
    protected Cipher getSymEncodingCipher() {
        return symEncodingCipher;
    }

    /**
     * @return Returns the local_addr.
     */
    protected Address getLocal_addr() {
        return local_addr;
    }

    /**
     * @param local_addr
     *                The local_addr to set.
     */
    protected void setLocal_addr(Address local_addr) {
        this.local_addr = local_addr;
    }

    /**
     * @return Returns the keyServerAddr.
     */
    protected Address getKeyServerAddr() {
        return keyServerAddr;
    }

    /**
     * @param keyServerAddr
     *                The keyServerAddr to set.
     */
    protected void setKeyServerAddr(Address keyServerAddr) {
        this.keyServerAddr = keyServerAddr;
    }

    public static class EncryptHeader extends org.jgroups.Header {

        short type;

        public static final short ENCRYPT = 0;

        public static final short KEY_REQUEST = 1;

        public static final short SERVER_PUBKEY = 2;

        public static final short SECRETKEY = 3;

        public static final short SECRETKEY_READY = 4;

        String version;

        boolean encrypt_entire_msg = false;

        public EncryptHeader() {
        }

        public EncryptHeader(short type) {
            this.type = type;
            this.version = "";
        }

        public EncryptHeader(short type, String version) {
            this.type = type;
            this.version = version;
        }

        public void writeTo(DataOutputStream out) throws IOException {
            out.writeShort(type);
            Util.writeString(version, out);
            out.writeBoolean(encrypt_entire_msg);
        }

        public void readFrom(DataInputStream in) throws IOException, IllegalAccessException, InstantiationException {
            type = in.readShort();
            version = Util.readString(in);
            encrypt_entire_msg = in.readBoolean();
        }

        public String toString() {
            return "ENCRYPT [type=" + type + " version=\"" + (version != null ? version.length() + " bytes" : "n/a") + "\"]";
        }

        public int size() {
            int retval = Global.SHORT_SIZE + Global.BYTE_SIZE + Global.BYTE_SIZE;
            if (version != null) retval += version.length() + 2;
            return retval;
        }

        /**
         * @return Returns the type.
         */
        protected short getType() {
            return type;
        }

        /**
         * @return Returns the version.
         */
        protected String getVersion() {
            return version;
        }
    }
}
