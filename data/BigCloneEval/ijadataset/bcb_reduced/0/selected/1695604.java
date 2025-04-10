package gnu.javax.crypto.keyring;

import gnu.java.security.Registry;
import gnu.javax.crypto.mac.IMac;
import gnu.javax.crypto.mac.MacFactory;
import gnu.javax.crypto.mac.MacOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public final class AuthenticatedEntry extends MaskableEnvelopeEntry implements Registry {

    public static final int TYPE = 2;

    public AuthenticatedEntry(String mac, int macLen, Properties properties) {
        super(TYPE, properties);
        if (macLen <= 0) throw new IllegalArgumentException("invalid mac length");
        this.properties.put("mac", mac);
        this.properties.put("maclen", String.valueOf(macLen));
        setMasked(false);
    }

    private AuthenticatedEntry() {
        super(TYPE);
        setMasked(true);
    }

    public static AuthenticatedEntry decode(DataInputStream in) throws IOException {
        AuthenticatedEntry entry = new AuthenticatedEntry();
        entry.properties.decode(in);
        if (!entry.properties.containsKey("mac")) throw new MalformedKeyringException("no mac specified");
        if (!entry.properties.containsKey("maclen")) throw new MalformedKeyringException("no mac length specified");
        return entry;
    }

    /**
   * Computes the mac over this envelope's data. This method <b>must</b> be
   * called before this entry in encoded.
   * 
   * @param key The key to authenticate with.
   * @throws IOException If encoding fails.
   * @throws InvalidKeyException If the supplied key is bad.
   */
    public void authenticate(byte[] key) throws IOException, InvalidKeyException {
        if (isMasked()) throw new IllegalStateException("entry is masked");
        IMac m = getMac(key);
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        MacOutputStream macout = new MacOutputStream(bout, m);
        DataOutputStream out2 = new DataOutputStream(macout);
        for (Iterator it = entries.iterator(); it.hasNext(); ) {
            Entry entry = (Entry) it.next();
            entry.encode(out2);
        }
        bout.write(m.digest());
        payload = bout.toByteArray();
    }

    /**
   * Verifies this entry's payload. This method will unmask this entry, thus it
   * must be called before accessing its contents.
   * 
   * @param key The key to use to authenticate.
   * @throws InvalidKeyException If the given key is improper.
   */
    public void verify(byte[] key) throws InvalidKeyException {
        if (!isMasked() || payload == null) return;
        IMac m = getMac(key);
        m.update(payload, 0, payload.length - m.macSize());
        byte[] macValue = new byte[m.macSize()];
        System.arraycopy(payload, payload.length - macValue.length, macValue, 0, macValue.length);
        if (!Arrays.equals(macValue, m.digest())) throw new IllegalArgumentException("MAC verification failed");
        try {
            int len = payload.length - m.macSize();
            ByteArrayInputStream bais = new ByteArrayInputStream(payload, 0, len);
            DataInputStream in = new DataInputStream(bais);
            decodeEnvelope(in);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("malformed keyring fragment");
        }
        setMasked(false);
        payload = null;
    }

    protected void encodePayload() throws IOException {
        if (payload == null) throw new IllegalStateException("not authenticated");
    }

    private IMac getMac(byte[] key) throws InvalidKeyException {
        IMac mac = MacFactory.getInstance(properties.get("mac"));
        if (mac == null) throw new IllegalArgumentException("no such mac: " + properties.get("mac"));
        int maclen = 0;
        if (!properties.containsKey("maclen")) throw new IllegalArgumentException("no MAC length");
        try {
            maclen = Integer.parseInt(properties.get("maclen"));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("bad MAC length");
        }
        HashMap macAttr = new HashMap();
        macAttr.put(IMac.MAC_KEY_MATERIAL, key);
        macAttr.put(IMac.TRUNCATED_SIZE, Integer.valueOf(maclen));
        mac.init(macAttr);
        return mac;
    }
}
