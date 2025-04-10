package games.strategy.engine.vault;

import games.strategy.engine.message.IChannelMessenger;
import games.strategy.engine.message.IChannelSubscribor;
import games.strategy.engine.message.RemoteName;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * A vault is a secure way for the client and server to share information without
 * trusting each other.
 * <p>
 * 
 * Data can be locked in the vault by a node. This data then is not readable by other nodes until the data is unlocked.
 * <p>
 * 
 * When the data is unlocked by the original node, other nodes can read the data. When data is put in the vault, it cant be changed by the originating node.
 * <p>
 * 
 * NOTE: to allow the data locked in the vault to be gc'd, the <code>release(VaultID id)<code> method 
 * should be called when it is no longer needed.
 * 
 * 
 * @author Sean Bridges
 */
public class Vault {

    private static final RemoteName VAULT_CHANNEL = new RemoteName("games.strategy.engine.vault.IServerVault.VAULT_CHANNEL", IRemoteVault.class);

    private static final String ALGORITHM = "DES";

    private SecretKeyFactory mSecretKeyFactory;

    private static final byte[] KNOWN_VAL = new byte[] { 0xC, 0xA, 0xF, 0xE, 0xB, 0xA, 0xB, 0xE };

    private final KeyGenerator m_keyGen;

    private final IChannelMessenger m_channelMessenger;

    private final ConcurrentMap<VaultID, SecretKey> m_secretKeys = new ConcurrentHashMap<VaultID, SecretKey>();

    private final ConcurrentMap<VaultID, byte[]> m_unverifiedValues = new ConcurrentHashMap<VaultID, byte[]>();

    private final ConcurrentMap<VaultID, byte[]> m_verifiedValues = new ConcurrentHashMap<VaultID, byte[]>();

    private final Object m_waitForLock = new Object();

    /**
	 * @param channelMessenger
	 */
    public Vault(final IChannelMessenger channelMessenger) {
        m_channelMessenger = channelMessenger;
        m_channelMessenger.registerChannelSubscriber(m_remoteVault, VAULT_CHANNEL);
        try {
            mSecretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            m_keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException("Nothing known about algorithm:" + ALGORITHM);
        }
    }

    public void shutDown() {
        m_channelMessenger.unregisterChannelSubscriber(m_remoteVault, VAULT_CHANNEL);
    }

    private SecretKey bytesToKey(final byte[] bytes) {
        try {
            final DESKeySpec spec = new DESKeySpec(bytes);
            return mSecretKeyFactory.generateSecret(spec);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private byte[] secretKeyToBytes(final SecretKey key) {
        DESKeySpec ks;
        try {
            ks = (DESKeySpec) mSecretKeyFactory.getKeySpec(key, DESKeySpec.class);
            return ks.getKey();
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private IRemoteVault getRemoteBroadcaster() {
        return (IRemoteVault) m_channelMessenger.getChannelBroadcastor(VAULT_CHANNEL);
    }

    /**
	 * place data in the vault. An encrypted form of the data is sent at this
	 * time to all nodes.
	 * <p>
	 * The same key used to encrypt the KNOWN_VALUE so that nodes can verify the key when it is used to decrypt the data.
	 * 
	 * @param data
	 *            - the data to lock
	 * @return the VaultId of the data
	 */
    public VaultID lock(final byte[] data) {
        final VaultID id = new VaultID(m_channelMessenger.getLocalNode());
        final SecretKey key = m_keyGen.generateKey();
        if (m_secretKeys.putIfAbsent(id, key) != null) {
            throw new IllegalStateException("dupliagte id:" + id);
        }
        m_verifiedValues.put(id, data);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        } catch (final NoSuchPaddingException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        } catch (final InvalidKeyException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
        final byte[] dataAndCheck = joinDataAndKnown(data);
        byte[] encrypted;
        try {
            encrypted = cipher.doFinal(dataAndCheck);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
        getRemoteBroadcaster().addLockedValue(id, encrypted);
        return id;
    }

    /**
	 * Join known and data into one array.
	 * <p>
	 * package access so we can test.
	 */
    static byte[] joinDataAndKnown(final byte[] data) {
        final byte[] dataAndCheck = new byte[KNOWN_VAL.length + data.length];
        System.arraycopy(KNOWN_VAL, 0, dataAndCheck, 0, KNOWN_VAL.length);
        System.arraycopy(data, 0, dataAndCheck, KNOWN_VAL.length, data.length);
        return dataAndCheck;
    }

    /**
	 * allow other nodes to see the data.
	 * <p>
	 * 
	 * You can only unlock data that was locked by the same instance of the Vault
	 * 
	 * @param id
	 *            - the vault id to unlock
	 */
    public void unlock(final VaultID id) {
        if (!id.getGeneratedOn().equals(m_channelMessenger.getLocalNode())) {
            throw new IllegalArgumentException("Cant unlock data that wasnt locked on this node");
        }
        final SecretKey key = m_secretKeys.remove(id);
        getRemoteBroadcaster().unlock(id, secretKeyToBytes(key));
    }

    /**
	 * Note - if an id has been released, then this will return false.
	 * If this instance of vault locked id, then this method will return true
	 * if the id has not been released.
	 * 
	 * @return - has this id been unlocked
	 */
    public boolean isUnlocked(final VaultID id) {
        return m_verifiedValues.containsKey(id);
    }

    /**
	 * Get the unlocked data.
	 * 
	 */
    public byte[] get(final VaultID id) throws NotUnlockedException {
        if (m_verifiedValues.containsKey(id)) return m_verifiedValues.get(id); else if (m_unverifiedValues.containsKey(id)) throw new NotUnlockedException(); else throw new IllegalStateException("Nothing known about id:" + id);
    }

    /**
	 * Do we know about the given vault id.
	 */
    public boolean knowsAbout(final VaultID id) {
        return m_verifiedValues.containsKey(id) || m_unverifiedValues.containsKey(id);
    }

    public List<VaultID> knownIds() {
        final ArrayList<VaultID> rVal = new ArrayList<VaultID>(m_verifiedValues.keySet());
        rVal.addAll(m_unverifiedValues.keySet());
        return rVal;
    }

    /**
	 * Allow all data associated with the given vault id to be released and garbage collected
	 * <p>
	 * An id can be released by any node.
	 * <p>
	 * If the id has already been released, then nothing will happen.
	 * 
	 */
    public void release(final VaultID id) {
        getRemoteBroadcaster().release(id);
    }

    private final IRemoteVault m_remoteVault = new IRemoteVault() {

        public void addLockedValue(final VaultID id, final byte[] data) {
            if (id.getGeneratedOn().equals(m_channelMessenger.getLocalNode())) return;
            if (m_unverifiedValues.putIfAbsent(id, data) != null) {
                throw new IllegalStateException("duplicate values for id:" + id);
            }
            synchronized (m_waitForLock) {
                m_waitForLock.notifyAll();
            }
        }

        public void unlock(final VaultID id, final byte[] secretKeyBytes) {
            if (id.getGeneratedOn().equals(m_channelMessenger.getLocalNode())) return;
            final SecretKey key = bytesToKey(secretKeyBytes);
            Cipher cipher;
            try {
                cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key);
            } catch (final NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new IllegalStateException(e.getMessage());
            } catch (final NoSuchPaddingException e) {
                e.printStackTrace();
                throw new IllegalStateException(e.getMessage());
            } catch (final InvalidKeyException e) {
                e.printStackTrace();
                throw new IllegalStateException(e.getMessage());
            }
            final byte[] encrypted = m_unverifiedValues.remove(id);
            byte[] decrypted;
            try {
                decrypted = cipher.doFinal(encrypted);
            } catch (final Exception e1) {
                e1.printStackTrace();
                throw new IllegalStateException(e1.getMessage());
            }
            if (decrypted.length < KNOWN_VAL.length) throw new IllegalStateException("decrypted is not long enough to have known value, cheating is suspected");
            for (int i = 0; i < KNOWN_VAL.length; i++) {
                if (KNOWN_VAL[i] != decrypted[i]) throw new IllegalStateException("Known value of cipher not correct, cheating is suspected");
            }
            final byte[] data = new byte[decrypted.length - KNOWN_VAL.length];
            System.arraycopy(decrypted, KNOWN_VAL.length, data, 0, data.length);
            if (m_verifiedValues.putIfAbsent(id, data) != null) {
                throw new IllegalStateException("duplicate values for id:" + id);
            }
            synchronized (m_waitForLock) {
                m_waitForLock.notifyAll();
            }
        }

        public void release(final VaultID id) {
            m_unverifiedValues.remove(id);
            m_verifiedValues.remove(id);
        }
    };

    /**
	 * Waits until we know about a given vault id.
	 * waits for at most timeout milliseconds
	 */
    public void waitForID(final VaultID id, final long timeoutMS) {
        if (timeoutMS <= 0) throw new IllegalArgumentException("Must suppply positive timeout argument");
        final long endTime = timeoutMS + System.currentTimeMillis();
        while (System.currentTimeMillis() < endTime && !knowsAbout(id)) {
            synchronized (m_waitForLock) {
                if (knowsAbout(id)) return;
                try {
                    final long waitTime = endTime - System.currentTimeMillis();
                    if (waitTime > 0) {
                        m_waitForLock.wait(waitTime);
                    }
                } catch (final InterruptedException e) {
                }
            }
        }
    }

    /**
	 * Wait until the given id is unlocked
	 */
    public void waitForIdToUnlock(final VaultID id, final long timeout) {
        if (timeout <= 0) throw new IllegalArgumentException("Must suppply positive timeout argument");
        final long startTime = System.currentTimeMillis();
        long leftToWait = timeout;
        while (leftToWait > 0 && !isUnlocked(id)) {
            synchronized (m_waitForLock) {
                if (isUnlocked(id)) return;
                try {
                    m_waitForLock.wait(leftToWait);
                } catch (final InterruptedException e) {
                }
                leftToWait = startTime + timeout - System.currentTimeMillis();
            }
        }
    }
}

interface IRemoteVault extends IChannelSubscribor {

    public void addLockedValue(VaultID id, byte[] data);

    public void unlock(VaultID id, byte[] secretKeyBytes);

    public void release(VaultID id);
}
