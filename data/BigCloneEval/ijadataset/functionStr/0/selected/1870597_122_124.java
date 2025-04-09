public class Test {    public PublisherID(PublisherPublicKeyDigest keyID) {
        this(keyID.digest(), PublisherType.KEY);
    }
}