package de.felixbruns.jotify.protocol.channel;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChannelAudioHandler implements ChannelListener {

    private Cipher cipher;

    private Key key;

    private byte[] iv;

    private int offset;

    private OutputStream output;

    public ChannelAudioHandler(byte[] key, OutputStream output) {
        try {
            this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("AES not available! Aargh!");
        } catch (NoSuchPaddingException e) {
            System.out.println("No padding not available... haha!");
        }
        this.key = new SecretKeySpec(key, "AES");
        this.iv = new byte[] { (byte) 0x72, (byte) 0xe0, (byte) 0x67, (byte) 0xfb, (byte) 0xdd, (byte) 0xcb, (byte) 0xcf, (byte) 0x77, (byte) 0xeb, (byte) 0xe8, (byte) 0xbc, (byte) 0x64, (byte) 0x3f, (byte) 0x63, (byte) 0x0d, (byte) 0x93 };
        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.iv));
        } catch (InvalidKeyException e) {
            System.out.println("Invalid key!");
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Invalid IV!");
        }
        this.output = output;
    }

    @Override
    public void channelHeader(Channel channel, byte[] header) {
    }

    @Override
    public void channelData(Channel channel, byte[] data) {
        int off, w, x, y, z;
        byte[] ciphertext = new byte[data.length + 1024];
        byte[] keystream = new byte[16];
        for (int block = 0; block < data.length / 1024; block++) {
            off = block * 1024;
            w = block * 1024 + 0 * 256;
            x = block * 1024 + 1 * 256;
            y = block * 1024 + 2 * 256;
            z = block * 1024 + 3 * 256;
            for (int i = 0; i < 1024 && (block * 1024 + i) < data.length; i += 4) {
                ciphertext[off++] = data[w++];
                ciphertext[off++] = data[x++];
                ciphertext[off++] = data[y++];
                ciphertext[off++] = data[z++];
            }
            for (int i = 0; i < 1024 && (block * 1024 + i) < data.length; i += 16) {
                try {
                    keystream = this.cipher.doFinal(this.iv);
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < 16; j++) {
                    ciphertext[block * 1024 + i + j] ^= keystream[j] ^ this.iv[j];
                }
                for (int j = 15; j >= 0; j--) {
                    this.iv[j] += 1;
                    if ((this.iv[j] & 0xFF) != 0) {
                        break;
                    }
                }
                try {
                    this.cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.iv));
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            this.output.write(ciphertext, 0, ciphertext.length - 1024);
        } catch (IOException e) {
        }
    }

    @Override
    public void channelEnd(Channel channel) {
        this.offset += channel.getDataLength();
        Channel.unregister(channel.getId());
    }

    @Override
    public void channelError(Channel channel) {
    }
}
