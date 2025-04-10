public class Test {    public static String createKeyHash(PublicKey key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256", "BC");
            final String res = new String(Hex.encode(md.digest(key.getEncoded())));
            md.reset();
            return res;
        } catch (NoSuchProviderException ex) {
            final String message = "Nu such provider trying to hash public key";
            LOG.error(message, ex);
            throw new RuntimeException(message, ex);
        } catch (NoSuchAlgorithmException ex) {
            final String message = "Nu such algorithm trying to hash public key";
            LOG.error(message, ex);
            throw new RuntimeException(message, ex);
        }
    }
}