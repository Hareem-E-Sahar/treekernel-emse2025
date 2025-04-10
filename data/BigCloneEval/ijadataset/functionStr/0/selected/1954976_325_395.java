public class Test {    public void setSocketStreams() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (encryptionType == SAW.SAW_CONNECTION_ENCRYPT_NONE) {
            connectionInputStream = connectionSocketInputStream;
            connectionOutputStream = connectionSocketOutputStream;
        } else if (encryptionType == SAW.SAW_CONNECTION_ENCRYPT_RC4) {
            sha256Digester = MessageDigest.getInstance("SHA-256");
            encryptionCipher = Cipher.getInstance("RC4");
            decryptionCipher = Cipher.getInstance("RC4");
            sha256Digester.update(localNonce);
            sha256Digester.update(remoteNonce);
            digestedKey = sha256Digester.digest(encryptionKey);
            encryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "RC4");
            decryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "RC4");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec);
            decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec);
            connectionInputStream = new CipherInputStream(connectionSocketInputStream, decryptionCipher);
            connectionOutputStream = new CipherOutputStream(connectionSocketOutputStream, encryptionCipher);
        } else if (encryptionType == SAW.SAW_CONNECTION_ENCRYPT_AES) {
            sha256Digester = MessageDigest.getInstance("SHA-256");
            encryptionCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            decryptionCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            sha256Digester.update(localNonce);
            sha256Digester.update(remoteNonce);
            digestedKey = sha256Digester.digest(encryptionKey);
            encryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "AES");
            decryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "AES");
            digestedIv = sha256Digester.digest(digestedKey);
            encryptionIvParameterSpec = new IvParameterSpec(digestedIv, 0, 16);
            decryptionIvParameterSpec = new IvParameterSpec(digestedIv, 16, 16);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec, encryptionIvParameterSpec);
            decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec, decryptionIvParameterSpec);
            connectionInputStream = new CipherInputStream(connectionSocketInputStream, decryptionCipher);
            connectionOutputStream = new CipherOutputStream(connectionSocketOutputStream, encryptionCipher);
        }
        multiplexedConnectionInputStream = new SAWMultiplexingInputStream(connectionInputStream, 7, 1024, 8192, false);
        multiplexedConnectionOutputStream = new SAWMultiplexingOutputStream(connectionOutputStream, 7, 1024, false, true, false);
        authenticationInputStream = multiplexedConnectionInputStream.getInputStream(0);
        authenticationOutputStream = multiplexedConnectionOutputStream.getOutputStream(0);
        shellInputStream = multiplexedConnectionInputStream.getInputStream(1);
        shellOutputStream = multiplexedConnectionOutputStream.getOutputStream(1);
        fileTransferControlInputStream = multiplexedConnectionInputStream.getInputStream(2);
        fileTransferControlOutputStream = multiplexedConnectionOutputStream.getOutputStream(2);
        fileTransferDataInputStream = multiplexedConnectionInputStream.getInputStream(3);
        fileTransferDataOutputStream = multiplexedConnectionOutputStream.getOutputStream(3);
        graphicsControlInputStream = multiplexedConnectionInputStream.getInputStream(4);
        graphicsControlOutputStream = multiplexedConnectionOutputStream.getOutputStream(4);
        graphicsImageInputStream = multiplexedConnectionInputStream.getInputStream(5);
        graphicsImageOutputStream = multiplexedConnectionOutputStream.getOutputStream(5);
        graphicsClipboardInputStream = multiplexedConnectionInputStream.getInputStream(6);
        graphicsClipboardOutputStream = multiplexedConnectionOutputStream.getOutputStream(6);
        authenticationReader = new BufferedReader(new InputStreamReader(authenticationInputStream, "UTF-8"));
        authenticationWriter = new BufferedWriter(new OutputStreamWriter(authenticationOutputStream, "UTF-8"));
        zShellDeflater = new ZOutputStream(shellOutputStream, JZlib.Z_DEFAULT_COMPRESSION, true, 4096);
        zShellDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        resultWriter = new BufferedWriter(new OutputStreamWriter(zShellDeflater, "UTF-8"));
        zShellInflater = new ZInputStream(shellInputStream, true, 4096);
        zShellInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        commandReader = new BufferedReader(new InputStreamReader(zShellInflater, "UTF-8"));
        zImageInflater = new ZInputStream(graphicsImageInputStream, true, 4096);
        zImageInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        zImageDeflater = new ZOutputStream(graphicsImageOutputStream, JZlib.Z_DEFAULT_COMPRESSION, true, 4096);
        zImageDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        zClipboardInflater = new ZInputStream(graphicsClipboardInputStream, true, 4096);
        zClipboardInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        zClipboardDeflater = new ZOutputStream(graphicsClipboardOutputStream, JZlib.Z_DEFAULT_COMPRESSION, true, 4096);
        zClipboardDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        fileTransferControlDataInputStream = new SAWLittleEndianInputStream(new BufferedInputStream(fileTransferControlInputStream));
        fileTransferControlDataOutputStream = new SAWLittleEndianOutputStream(new BufferedOutputStream(fileTransferControlOutputStream));
        graphicsControlDataInputStream = new SAWLittleEndianInputStream(new BufferedInputStream(graphicsControlInputStream));
        graphicsControlDataOutputStream = new SAWLittleEndianOutputStream(new BufferedOutputStream(graphicsControlOutputStream));
    }
}