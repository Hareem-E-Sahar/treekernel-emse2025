package org.isodl.certificates;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Calendar;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ejbca.cvc.AccessRight;
import org.ejbca.cvc.AuthorizationRole;
import org.ejbca.cvc.CAReferenceField;
import org.ejbca.cvc.CVCertificate;
import org.ejbca.cvc.CertificateGenerator;
import org.ejbca.cvc.HolderReferenceField;
import org.isodl.util.Files;

/**
 * A class for generating card verifiable certificates according to the ISO18013
 * standard. Uses the modified org.ejbca.cvc library. The files are written to
 * the predefined files, see below.
 * 
 * @author Wojciech Mostowski <woj@cs.ru.nl>
 * 
 */
public class DrivingLicenseCertificateGenerator {

    public static final String filenameCA = "/home/sos/woj/cvc/cacert.cvcert";

    public static final String filenameTerminal = "/home/sos/woj/cvc/terminalcert.cvcert";

    public static final String filenameKey = "/home/sos/woj/cvc/terminalkey.der";

    public static void main(String[] args) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Calendar cal1 = Calendar.getInstance();
            Date validFrom = cal1.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.MONTH, 3);
            Date validTo = cal2.getTime();
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            SecureRandom random = new SecureRandom();
            keyGen.initialize(1024, random);
            KeyPair caKeyPair = keyGen.generateKeyPair();
            keyGen.initialize(1024, random);
            KeyPair terminalKeyPair = keyGen.generateKeyPair();
            CAReferenceField caRef = new CAReferenceField("NL", "MYDL-CVCA", "00001");
            HolderReferenceField holderRef = new HolderReferenceField(caRef.getCountry(), caRef.getMnemonic(), caRef.getSequence());
            CVCertificate caCvc = CertificateGenerator.createCertificate(caKeyPair.getPublic(), caKeyPair.getPrivate(), "SHA1WithRSA", caRef, holderRef, new AuthorizationRole(AuthorizationRole.TRUST_ROOT, 2), new AccessRight(AccessRight.DG2 | AccessRight.DG3), validFrom, validTo, "BC");
            HolderReferenceField terminalHolderRef = new HolderReferenceField("NL", "RUDL-CVCT", "00001");
            CVCertificate terminalCvc = CertificateGenerator.createCertificate(terminalKeyPair.getPublic(), caKeyPair.getPrivate(), "SHA1WithRSA", caRef, terminalHolderRef, new AuthorizationRole(), new AccessRight(AccessRight.ALL), validFrom, validTo, "BC");
            byte[] caCertData = caCvc.getDEREncoded();
            byte[] terminalCertData = terminalCvc.getDEREncoded();
            byte[] terminalPrivateKey = terminalKeyPair.getPrivate().getEncoded();
            Files.writeFile(new File(filenameCA), caCertData);
            Files.writeFile(new File(filenameTerminal), terminalCertData);
            Files.writeFile(new File(filenameKey), terminalPrivateKey);
            CVCertificate c = Files.readCVCertificateFromFile(new File(filenameCA));
            System.out.println(c.getCertificateBody().getAsText());
            c = Files.readCVCertificateFromFile(new File(filenameTerminal));
            System.out.println(c.getCertificateBody().getAsText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
