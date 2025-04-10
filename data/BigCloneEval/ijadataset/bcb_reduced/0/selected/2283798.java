package org.javasign.operators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertStoreException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.javasign.util.ApduData;
import org.javasign.util.TimeStampTokenGetter;

/**
 * @author raffaello bindi,simone rastelli
 */
public class TsrGenerator {

    File src = null;

    File dest = null;

    String url = null;

    public TsrGenerator(String url, File src, File dest) {
        this.src = src;
        this.dest = dest;
        this.url = url;
    }

    public void generate() throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        int sizecontent = ((int) src.length());
        byte[] contentbytes = new byte[sizecontent];
        FileInputStream freader = new FileInputStream(src);
        System.out.println("\nContent Bytes: " + freader.read(contentbytes, 0, sizecontent));
        freader.close();
        MessageDigest md = MessageDigest.getInstance("SHA-1", "BC");
        byte[] fingerprint = md.digest(contentbytes);
        System.out.println("Digest : " + ApduData.toHexString(fingerprint));
        TimeStampToken tst = null;
        TimeStampTokenGetter tstGetter = new TimeStampTokenGetter(new PostMethod(url), fingerprint, BigInteger.valueOf(0));
        tst = tstGetter.getTimeStampToken();
        if (tst == null) {
            System.out.println("NO TST");
            return;
        }
        byte[] tsrdata = tst.getEncoded();
        System.out.println("Got tsr " + tsrdata.length + " bytes");
        FileOutputStream efos = new FileOutputStream(dest);
        efos.write(tsrdata);
        efos.close();
    }
}
