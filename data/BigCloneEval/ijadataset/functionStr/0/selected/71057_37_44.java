public class Test {    protected OutgoingMessage computeSharedSecret(final IncomingMessage in) throws KeyAgreementException {
        super.computeSharedSecret(in);
        final byte[] sBytes = Util.trim(K);
        final IMessageDigest hash = srp.newDigest();
        hash.update(sBytes, 0, sBytes.length);
        K = new BigInteger(1, hash.digest());
        return null;
    }
}