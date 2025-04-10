public class Test {    public String generateHash(final String key) {
        justInTimeInitialiseAlgorithm();
        if (ALGORITHM.equalsIgnoreCase(NO_ALGORITHM)) {
            return key;
        }
        synchronized (messageDigest) {
            messageDigest.reset();
            messageDigest.update(key.getBytes());
            final byte[] bytes = messageDigest.digest();
            final StringBuffer buff = new StringBuffer();
            for (int l = 0; l < bytes.length; l++) {
                final String hx = Integer.toHexString(0xFF & bytes[l]);
                if (hx.length() == 1) {
                    buff.append("0");
                }
                buff.append(hx);
            }
            return buff.toString().trim();
        }
    }
}