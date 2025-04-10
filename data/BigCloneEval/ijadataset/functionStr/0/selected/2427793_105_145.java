public class Test {    public void init(Map attrib) throws InvalidKeyException {
        byte[] nonce = (byte[]) attrib.get(IV);
        if (nonce == null) throw new IllegalArgumentException("no nonce provided");
        byte[] key = (byte[]) attrib.get(KEY_MATERIAL);
        if (key == null) throw new IllegalArgumentException("no key provided");
        Arrays.fill(t_n, (byte) 0);
        nonceOmac.reset();
        nonceOmac.init(Collections.singletonMap(MAC_KEY_MATERIAL, key));
        nonceOmac.update(t_n, 0, t_n.length);
        nonceOmac.update(nonce, 0, nonce.length);
        byte[] N = nonceOmac.digest();
        nonceOmac.reset();
        nonceOmac.update(t_n, 0, t_n.length);
        nonceOmac.update(nonce, 0, nonce.length);
        t_n[t_n.length - 1] = 1;
        headerOmac.reset();
        headerOmac.init(Collections.singletonMap(MAC_KEY_MATERIAL, key));
        headerOmac.update(t_n, 0, t_n.length);
        t_n[t_n.length - 1] = 2;
        msgOmac.reset();
        msgOmac.init(Collections.singletonMap(MAC_KEY_MATERIAL, key));
        msgOmac.update(t_n, 0, t_n.length);
        Integer modeSize = (Integer) attrib.get(MODE_BLOCK_SIZE);
        if (modeSize == null) modeSize = Integer.valueOf(cipherBlockSize);
        HashMap ctrAttr = new HashMap();
        ctrAttr.put(KEY_MATERIAL, key);
        ctrAttr.put(IV, N);
        ctrAttr.put(STATE, Integer.valueOf(ENCRYPTION));
        ctrAttr.put(MODE_BLOCK_SIZE, modeSize);
        ctr.reset();
        ctr.init(ctrAttr);
        Integer st = (Integer) attrib.get(STATE);
        if (st != null) {
            state = st.intValue();
            if (state != ENCRYPTION && state != DECRYPTION) throw new IllegalArgumentException("invalid state");
        } else state = ENCRYPTION;
        Integer ts = (Integer) attrib.get(TRUNCATED_SIZE);
        if (ts != null) tagSize = ts.intValue(); else tagSize = cipherBlockSize;
        if (tagSize < 0 || tagSize > cipherBlockSize) throw new IllegalArgumentException("tag size out of range");
        init = true;
    }
}