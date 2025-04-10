public class Test {    public byte[] createHash(String algorithm, byte[] data) throws SecurityServiceException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm, "IAIK");
            return digest.digest(data);
        } catch (Exception e) {
            throw new SecurityServiceException("Couldn't create hash value provided data. Reason: " + e.getMessage());
        }
    }
}