public class Test {    public static void encrypt(File inputFile, File outputFile, byte[] encKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, IOException {
        SecretKeySpec skeySpec = new SecretKeySpec(encKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        if (inputFile.isFile()) {
            if (outputFile.exists()) outputFile.delete();
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(outputFile);
            CipherOutputStream cout = new CipherOutputStream(out, cipher);
            int length = 0;
            byte[] buffer = new byte[8];
            while ((length = in.read(buffer)) != -1) {
                cout.write(buffer, 0, length);
            }
            cout.flush();
            cout.close();
            out.close();
            in.close();
        }
    }
}