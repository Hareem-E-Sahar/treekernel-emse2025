public class Test {    @Override
    protected synchronized byte[] basicDecrypt(byte[] data, byte[] encryptionKey, int objectNum, int genNum) throws COSSecurityException {
        try {
            updateHash(encryptionKey, objectNum, genNum);
            byte[] keyBase = md.digest();
            SecretKey skeySpec = new SecretKeySpec(keyBase, 0, length, KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new COSSecurityException(e);
        }
    }
}