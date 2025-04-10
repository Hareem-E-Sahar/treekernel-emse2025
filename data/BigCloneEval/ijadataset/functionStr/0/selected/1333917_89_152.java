public class Test {    public boolean WriteFile(java.io.Serializable inObj, String fileName) throws Exception {
        FileOutputStream out;
        try {
            FileInputStream fsKeysIn;
            FileOutputStream fsKeysOut;
            Key key = null;
            byte[] raw;
            SecretKey skey = null;
            if ((keyFile == null) || (keyFile.length() == 0)) {
                fsKeysIn = null;
            } else {
                fsKeysIn = new FileInputStream(keyFile);
            }
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(fsKeysIn, keyPasswd.toCharArray());
            if (fsKeysIn != null) {
                fsKeysIn.close();
            }
            try {
                key = ks.getKey(fileName, filePasswd.toCharArray());
                if (key != null) {
                    if (key.getAlgorithm().compareTo(algorithm) != 0) {
                        key = null;
                    }
                }
            } catch (KeyStoreException ksE) {
                Log.out(ksE);
            } catch (NoSuchAlgorithmException nsaE) {
                Log.out(nsaE);
            } catch (UnrecoverableKeyException urkE) {
                Log.out(urkE);
            }
            out = new FileOutputStream(fileName);
            cipher = Cipher.getInstance(algorithm);
            if (key == null) {
                KeyGenerator kgen = KeyGenerator.getInstance(algorithm);
                skey = kgen.generateKey();
                raw = skey.getEncoded();
            } else {
                raw = key.getEncoded();
            }
            SecretKeySpec skeySpec = new SecretKeySpec(raw, algorithm);
            ObjectOutputStream s = new ObjectOutputStream(out);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            SealedObject so = new SealedObject(inObj, cipher);
            s.writeObject(so);
            s.flush();
            if (skey != null) {
                ks.setKeyEntry(fileName, skey, filePasswd.toCharArray(), null);
                if (keyFile.length() == 0) {
                    fsKeysOut = new FileOutputStream("SDM.keystore");
                } else {
                    fsKeysOut = new FileOutputStream(keyFile);
                }
                ks.store(fsKeysOut, keyPasswd.toCharArray());
                fsKeysOut.close();
            }
            out.close();
        } catch (Exception e) {
            Log.out(e);
            throw e;
        }
        return true;
    }
}