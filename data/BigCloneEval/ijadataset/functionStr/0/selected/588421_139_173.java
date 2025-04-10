public class Test {    public EncryptStream(Key key, String fname, OutputStream output, SecureRandom rnd, char mode, Signer signer) throws Exception {
        super(output);
        byte[] salt = new byte[8];
        byte[] prefix = new byte[10];
        Cipher c = Cipher.getInstance("DESede/CFB/NoPadding");
        long time = new Date().getTime() / 1000;
        sig = signer;
        out.write(0xc0 | Packet.PROCRYPTED);
        out = new PacketOutputStream(out, 13);
        out.write(1);
        rnd.nextBytes(salt);
        System.arraycopy(salt, 0, prefix, 0, 8);
        System.arraycopy(salt, 6, prefix, 8, 2);
        md = MessageDigest.getInstance("SHA1");
        c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[8]));
        out = new CipherOutputStream(out, c);
        out = new DigestOutputStream(out, md);
        out.write(prefix);
        out.write(0xc0 | Packet.COMPRESSED);
        out = pos = new PacketOutputStream(out, 13);
        out.write(2);
        out = new DeflaterOutputStream(out);
        if (sig != null) sig.writePreamble(out);
        out.write(0xc0 | Packet.LITERAL);
        out = sos = new PacketOutputStream(out, 13);
        out.write(mode);
        if (fname.length() > 255) fname = fname.substring(0, 255);
        out.write(fname.length());
        out.write(fname.getBytes());
        out.write(new Long((time >> 24) & 0xff).byteValue());
        out.write(new Long((time >> 16) & 0xff).byteValue());
        out.write(new Long((time >> 8) & 0xff).byteValue());
        out.write(new Long(time & 0xff).byteValue());
        if (sig != null) out = new SignerStream(sig, out);
    }
}