public class Test {    public Digest getComputedDigest() {
        return new Digest(this.algorithm, computedDigest.digest());
    }
}