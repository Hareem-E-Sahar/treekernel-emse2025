public class Test {    public static void main(String[] argv) {
        StandardStringDigester digester = new StandardStringDigester();
        digester.setAlgorithm("SHA-1");
        digester.setIterations(50000);
        String digest = digester.digest("odpsoft");
        System.out.println(digest);
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword("odpsoftmm");
        String encryptedText = encryptor.encrypt("myText");
        String plainText = encryptor.decrypt(encryptedText);
        System.out.println(encryptedText);
        System.out.println(plainText);
        ConfigurablePasswordEncryptor passwordEncryptor = new ConfigurablePasswordEncryptor();
        passwordEncryptor.setAlgorithm("SHA-1");
        passwordEncryptor.setPlainDigest(true);
        String encryptedPassword = passwordEncryptor.encryptPassword("odpsoft");
        System.out.println(encryptedPassword);
        BasicIntegerNumberEncryptor integerEncryptor = new BasicIntegerNumberEncryptor();
        integerEncryptor.setPassword("mmgg");
        BigInteger myEncryptedNumber = integerEncryptor.encrypt(new BigInteger("123456"));
        BigInteger plainNumber = integerEncryptor.decrypt(myEncryptedNumber);
        System.out.println(myEncryptedNumber + ":" + plainNumber);
    }
}