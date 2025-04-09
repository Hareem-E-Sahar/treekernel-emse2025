public class Test {    public String getDigest() {
        return getMessageDigest() != null ? SVNFileUtil.toHexDigest(getMessageDigest().digest()) : null;
    }
}