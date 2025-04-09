import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.SignedData;

/**
 * TEST QUE MUESTRA COMO VERIFICAR QUE LOS DATOS QUE APARECEN EN UNA FIRMA CMS/CADES SON 
 * IGUALES QUE UN DOCUMENTO
 * 
 * @author rsanz
 *
 */
public class VerificarDatosCADES {

    /**
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(new FileInputStream("moduls/ESBClient/test/files/cades-implicito.csig"), bos);
        byte[] firma = bos.toByteArray();
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        copy(new FileInputStream("moduls/ESBClient/test/files/datos.txt"), bos2);
        byte[] documento = bos2.toByteArray();
        byte[] datosEnFirma = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(firma);
        ASN1InputStream lObjDerOut = new ASN1InputStream(bis);
        DERObject lObjDER = lObjDerOut.readObject();
        ContentInfo lObjPKCS7 = ContentInfo.getInstance(lObjDER);
        SignedData lObjSignedData = SignedData.getInstance(lObjPKCS7.getContent());
        ContentInfo lObjContent = lObjSignedData.getContentInfo();
        datosEnFirma = ((ASN1OctetString) lObjContent.getContent()).getOctets();
        System.out.println("Son iguales? " + Arrays.equals(documento, datosEnFirma));
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte buffer[] = new byte[4096];
        int count = 0;
        for (int n = 0; -1 != (n = input.read(buffer)); ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
