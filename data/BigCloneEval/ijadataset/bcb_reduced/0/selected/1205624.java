package net.kano.joustsim.oscar.oscar.service.chatrooms;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.snaccmd.chat.ChatMsg;
import net.kano.joustsim.oscar.AimConnection;
import net.kano.joustsim.trust.PrivateKeys;
import net.kano.joustsim.trust.KeyPair;
import net.kano.joustsim.trust.TrustPreferences;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.BERConstructedOctetString;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.Locale;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;

public class EncryptedChatRoomMessageFactory implements ChatRoomMessageFactory {

    private AimConnection aimConnection;

    private ChatRoomService roomService;

    private SecretKey key;

    private SecureRandom random = new SecureRandom();

    public EncryptedChatRoomMessageFactory(AimConnection aimConnection, ChatRoomService roomService, SecretKey key) {
        this.aimConnection = aimConnection;
        this.roomService = roomService;
        this.key = key;
    }

    public ChatMessage createMessage(ChatRoomService service, ChatRoomUser user, ChatMsg message) {
        ByteBlock data = message.getMessageData();
        return null;
    }

    public ChatMsg encodeMessage(String message) throws EncodingException {
        byte[] data;
        try {
            data = getEncodedMessageData(message);
        } catch (IOException e) {
            throw new EncodingException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new EncodingException(e);
        } catch (NoSuchProviderException e) {
            throw new EncodingException(e);
        } catch (NoSuchPaddingException e) {
            throw new EncodingException(e);
        } catch (InvalidKeyException e) {
            throw new EncodingException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncodingException(e);
        } catch (IllegalBlockSizeException e) {
            throw new EncodingException(e);
        } catch (BadPaddingException e) {
            throw new EncodingException(e);
        } catch (CMSException e) {
            throw new EncodingException(e);
        }
        return new ChatMsg(ChatMsg.CONTENTTYPE_SECURE, ChatMsg.CONTENTENCODING_DEFAULT, "UTF-8", ByteBlock.wrap(data), Locale.getDefault());
    }

    private byte[] getEncodedMessageData(String message) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CMSException {
        byte[] dataToEncrypt = getCmsSignedBlock(message);
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        Cipher c = Cipher.getInstance("2.16.840.1.101.3.4.1.42", "BC");
        c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = c.doFinal(dataToEncrypt);
        EncryptedContentInfo eci = new EncryptedContentInfo(new DERObjectIdentifier("1.2.840.113549.1.7.1"), new AlgorithmIdentifier(new DERObjectIdentifier("2.16.840.1.101.3.4.1.42"), new DEROctetString(iv)), new BERConstructedOctetString(encrypted));
        EncryptedData ed = new EncryptedData(eci.getContentType(), eci.getContentEncryptionAlgorithm(), eci.getEncryptedContent());
        BERTaggedObject bert = new BERTaggedObject(0, ed.getDERObject());
        DERObjectIdentifier rootid = new DERObjectIdentifier("1.2.840.113549.1.7.6");
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(rootid);
        vec.add(bert);
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        ASN1OutputStream out = new ASN1OutputStream(fout);
        out.writeObject(new BERSequence(vec));
        out.close();
        return fout.toByteArray();
    }

    private byte[] getCmsSignedBlock(String msg) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, CMSException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bout, "US-ASCII");
        osw.write("Content-Transfer-Encoding: binary\r\n" + "Content-Type: text/x-aolrtf; charset=us-ascii\r\n" + "Content-Language: en\r\n" + "\r\n");
        osw.flush();
        bout.write(msg.getBytes("UTF-8"));
        byte[] dataToSign = bout.toByteArray();
        byte[] signedData = signData(dataToSign);
        bout = new ByteArrayOutputStream();
        osw = new OutputStreamWriter(bout, "US-ASCII");
        osw.write("Content-Transfer-Encoding: binary\r\n" + "Content-Type: application/pkcs7-mime; charset=us-ascii\r\n" + "Content-Language: en\r\n" + "\r\n");
        osw.flush();
        bout.write(signedData);
        return bout.toByteArray();
    }

    private byte[] signData(byte[] dataToSign) throws NoSuchProviderException, NoSuchAlgorithmException, CMSException, IOException {
        CMSSignedDataGenerator sgen = new CMSSignedDataGenerator();
        TrustPreferences localPrefs = aimConnection.getLocalPrefs();
        KeyPair signingKeys = localPrefs.getPrivateKeysPreferences().getKeysInfo().getSigningKeys();
        sgen.addSigner(signingKeys.getPrivateKey(), signingKeys.getPublicCertificate(), CMSSignedDataGenerator.DIGEST_MD5);
        CMSSignedData csd = sgen.generate(new CMSProcessableByteArray(dataToSign), true, "BC");
        return csd.getEncoded();
    }
}
