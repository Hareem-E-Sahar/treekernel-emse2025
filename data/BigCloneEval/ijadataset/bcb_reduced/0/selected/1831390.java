package it.trento.comune.j4sign.examples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.cms.CMSTimeStampedDataParser;
import org.bouncycastle.tsp.cms.ImprintDigestInvalidException;
import org.bouncycastle.util.io.Streams;

/**
 * A command line interface sample program for parsing a TimeStampedData
 * (RFC5544) file. It works only with BouncyCastle v1.46 (or later) libraries which
 * introduced RFC5544 support.
 * <p>
 * TimeStamp token is verified, and if verification is correct data content is
 * extracted.
 * <p>
 * 
 * @author Roberto Resoli
 */
public class TsdTest {

    private byte[] baseData;

    CMSTimeStampedDataParser cmsTimeStampedData = null;

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: TsdTest <path of TimestampedData File>");
            return;
        }
        boolean isValid = false;
        String inPath = args[0];
        TsdTest tt = new TsdTest();
        if (tt.parse(inPath)) {
            String outPath = inPath.substring(0, inPath.lastIndexOf("."));
            isValid = tt.validate(outPath);
        }
        if (isValid) System.exit(0); else System.exit(1);
    }

    private boolean parse(String path) {
        boolean parseOk = false;
        try {
            this.baseData = readTsdFromFile(path);
            cmsTimeStampedData = new CMSTimeStampedDataParser(baseData);
            parseOk = true;
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (CMSException e) {
            System.out.println("CMSException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return parseOk;
    }

    public boolean validate(String outPath) {
        boolean timestampValid = false;
        DigestCalculatorProvider digestCalculatorProvider = new BcDigestCalculatorProvider();
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        System.out.println("Validating TSD ...");
        try {
            Streams.pipeAll(cmsTimeStampedData.getContent(), bOut);
            DigestCalculator imprintCalculator = cmsTimeStampedData.getMessageImprintDigestCalculator(digestCalculatorProvider);
            Streams.pipeAll(new ByteArrayInputStream(bOut.toByteArray()), imprintCalculator.getOutputStream());
            cmsTimeStampedData.validate(digestCalculatorProvider, imprintCalculator.getDigest());
            timestampValid = true;
            System.out.println("Timestamp validated.");
            System.out.println("Writing extracted data to file: " + outPath);
            FileOutputStream fos = new FileOutputStream(outPath);
            fos.write(bOut.toByteArray());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (OperatorCreationException e) {
            System.out.println("OperatorCreationException: " + e.getMessage());
        } catch (ImprintDigestInvalidException e) {
            System.out.println("ImprintDigestInvalidException: " + e.getMessage());
        } catch (CMSException e) {
            System.out.println("CMSException: " + e.getMessage());
        }
        return timestampValid;
    }

    public byte[] readTsdFromFile(String filePath) throws IOException {
        System.out.println("reading TSD from file: " + filePath);
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = 0;
        while ((bytesRead = fis.read(buffer, 0, buffer.length)) >= 0) {
            baos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return baos.toByteArray();
    }
}
