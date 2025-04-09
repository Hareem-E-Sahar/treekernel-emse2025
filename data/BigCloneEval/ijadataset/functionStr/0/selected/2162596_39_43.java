public class Test {    @Test
    public void testDigestUsingNull() {
        String encryptedValue = encrypter.digest(null);
        assertNull("encryptedValue aws not null", encryptedValue);
    }
}