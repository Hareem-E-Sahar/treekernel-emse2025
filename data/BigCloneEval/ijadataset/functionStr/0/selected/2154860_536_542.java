public class Test {    private String generateUid(byte[] bytes) throws NoSuchAlgorithmException {
        String docid = null;
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha.digest(bytes);
        docid = PluginHelper.hexEncode(digest);
        return docid;
    }
}