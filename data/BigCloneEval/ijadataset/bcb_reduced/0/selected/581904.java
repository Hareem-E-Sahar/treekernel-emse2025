package com.scottandjoe.texasholdem.networking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author Scott DellaTorre
 * @author Joe Stein
 */
public class EncryptedMessageWriter {

    private Cipher cipher;

    private EMSExceptionHandler exceptionHandler;

    private Key key;

    private int maxBlockSize;

    private final MessageWriter writer;

    public EncryptedMessageWriter(EMSExceptionHandler exceptionHandler, MessageWriter messageWriter) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.exceptionHandler = exceptionHandler;
        writer = messageWriter;
    }

    public EncryptedMessageWriter(EMSExceptionHandler exceptionHandler, MessageWriter messageWriter, String algorithm, int maximumBlockSize, PublicKey encryptionKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.exceptionHandler = exceptionHandler;
        writer = messageWriter;
        key = encryptionKey;
        maxBlockSize = maximumBlockSize;
        cipher = Cipher.getInstance(algorithm);
        if (key != null) {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }
    }

    public void close() throws IOException {
        writer.close();
    }

    private byte[] encryptMessage(Message message) throws EMSCorruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = getBytesFromMessage(message);
        byte[] currBytes;
        for (int i = 0; i <= bytes.length / maxBlockSize; i++) {
            if (bytes.length - i * maxBlockSize >= maxBlockSize) {
                currBytes = new byte[maxBlockSize];
            } else {
                currBytes = new byte[bytes.length - i * maxBlockSize];
            }
            for (int j = 0; j < currBytes.length; j++) {
                currBytes[j] = bytes[i * maxBlockSize + j];
            }
            if (currBytes.length > 0) {
                try {
                    baos.write(cipher.doFinal(currBytes));
                } catch (Exception e) {
                    throw new EMSCorruptedException(e.getMessage(), e);
                }
            }
        }
        return baos.toByteArray();
    }

    private static byte[] getBytesFromMessage(Message message) throws EMSCorruptedException {
        byte[] bytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(message);
            out.close();
        } catch (IOException ioe) {
            throw new EMSCorruptedException(ioe.getMessage(), ioe);
        }
        bytes = baos.toByteArray();
        return bytes;
    }

    public void reInitialize(String algorithm, int maxBlockSize, Key encryptionKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        cipher = Cipher.getInstance(algorithm);
        key = encryptionKey;
        this.maxBlockSize = maxBlockSize;
        cipher.init(Cipher.ENCRYPT_MODE, key);
    }

    public void setExceptionHandler(EMSExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    void writeMessage(Message message, boolean encrypted) {
        try {
            byte[] bytes;
            if (encrypted) {
                bytes = encryptMessage(message);
            } else {
                bytes = getBytesFromMessage(message);
            }
            writer.writeMessage(bytes);
        } catch (EMSCorruptedException emsce) {
            exceptionHandler.handleException(emsce);
        }
    }
}
