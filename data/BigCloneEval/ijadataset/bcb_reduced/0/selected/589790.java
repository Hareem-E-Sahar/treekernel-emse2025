package mx.bigdata.sat.cfdi;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import mx.bigdata.sat.cfdi.examples.ExampleCFDFactory;
import mx.bigdata.sat.security.KeyLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public final class TFDv1Test {

    private static PrivateKey key;

    private static X509Certificate cert;

    private static PrivateKey pacKey;

    private static X509Certificate pacCert;

    private TFDv1 tfd;

    @BeforeClass
    public static void loadKeys() throws Exception {
        key = KeyLoader.loadPKCS8PrivateKey(new FileInputStream("resources/certs/emisor.key"), "a0123456789");
        cert = KeyLoader.loadX509Certificate(new FileInputStream("resources/certs/emisor.cer"));
        pacKey = KeyLoader.loadPKCS8PrivateKey(new FileInputStream("resources/certs/pac.key"), "a0123456789");
        pacCert = KeyLoader.loadX509Certificate(new FileInputStream("resources/certs/pac.cer"));
    }

    @Before
    public void setupTFD() throws Exception {
        CFDv3 cfd = new CFDv3(ExampleCFDFactory.createComprobante(), "mx.bigdata.sat.cfdi.examples");
        cfd.sellar(key, cert);
        Date date = new GregorianCalendar(2011, 01, 07, 8, 51, 00).getTime();
        UUID uuid = UUID.fromString("843a05d7-207d-4adc-91e8-bda7175bcda3");
        tfd = new TFDv1(cfd, pacCert, uuid, date);
    }

    @Test
    public void testOriginalString() throws Exception {
        String cadena = "||1.0|843a05d7-207d-4adc-91e8-bda7175bcda3|2011-02-07T08:51:00|C3erAp8WBWhndqVrWHF4stkhtEYqxhg1jeqgypnzFaj7q+NVTkBsBbRqGo48IUsgvb2mSDjEdRvFqb3bcQX3iA7mykWCqXWi0DD75DmguGSjNkEDPdvzPR8+JFSILDdmEvJHSDl+tFM3vf4ApccIrRO5Ouk6ruQloZGl4xWKS7k=|30001000000100000801||";
        assertEquals(cadena, tfd.getCadenaOriginal());
    }

    @Test
    public void testStamp() throws Exception {
        tfd.timbrar(pacKey);
        String signature = "dtClAtpaCX6095uxE+QGyOc9wIxtUyFLfRZIKtP56czfHVT/rTozD0JNYfEoL8Al1u26MHqLTIImO6cg0I2jSv9N3h9F1VRLHZWAEha70NrVvyARgcWjGWJ6yPf2//u0WWf1oSQ++16ab5XDR/rT1eZKKkYp/SwELHzWqap+zEM=";
        assertEquals(signature, tfd.getTimbre().getSelloSAT());
        BigInteger bi = pacCert.getSerialNumber();
        String certificateNum = new String(bi.toByteArray());
        assertEquals(certificateNum, tfd.getTimbre().getNoCertificadoSAT());
    }

    @Test
    public void testValidateVerify() throws Exception {
        tfd.timbrar(pacKey);
        tfd.validar();
        tfd.verificar();
    }

    @Test
    public void testValidateVerifyWithFile() throws Exception {
        CFDv3 cfd = new CFDv3(new FileInputStream("resources/xml/cfdv3_tfd.xml"));
        TFDv1 tfd = new TFDv1(cfd, pacCert);
        tfd.timbrar(pacKey);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tfd.guardar(baos);
        CFDv3 cfd2 = new CFDv3(new ByteArrayInputStream(baos.toByteArray()));
        TFDv1 tfd2 = new TFDv1(cfd2, pacCert);
        tfd2.validar();
        tfd2.verificar();
    }
}
