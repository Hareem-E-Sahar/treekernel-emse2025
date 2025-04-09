import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import es.cim.alfrescoClient.AlfrescoClientCIM;
import es.cim.alfrescoClient.Fichero;
import es.cim.alfrescoClient.Firma;

public class TestAlfrescoClient {

    private static String url = "http://192.168.2.27:8080/alfresco";

    private static String USU_EXPE = "sis_helium";

    private static String USU_BANENT = "sis_sistra";

    private static String USU_BANSAL = "sis_sistra";

    private static String PASS_EXPE = "SJ2ScOmLOpYEFoRdZGS7yw==";

    private static String PASS_BANENT = "SJ2ScOmLOpYEFoRdZGS7yw==";

    private static String PASS_BANSAL = "SJ2ScOmLOpYEFoRdZGS7yw==";

    public static void main(String[] args) throws Exception {
        prepareDirs();
        testBandejaEntrada();
    }

    private static void testBandejaEntrada() throws Exception {
        String numero = "14/111/2010";
        String tipo = "envio";
        String anyo = "2010";
        String numEntrada = "BTE/334/2010";
        String codDocumento = Long.toString(System.currentTimeMillis());
        String fecha = fechaACadena(new Date());
        System.out.println("------------------ testBandejaEntrada");
        AlfrescoClientCIM alfClient = new AlfrescoClientCIM(url);
        String ticket = alfClient.loginPost(USU_BANENT, PASS_BANENT);
        String refDoc = alfClient.insertarDocumentoBandejaEntrada(ticket, tipo, numero, fecha, numEntrada, anyo, "Documento de test", "asiento.xml", getDataFile("files/asiento.xml"), codDocumento);
        alfClient.existeDocumentoBandejaEntrada(ticket, anyo, tipo, numero, codDocumento);
        alfClient.adjuntarFirmaDocumento(ticket, refDoc, getDataFile("files/Xades.xml"), "XADES-BES");
        alfClient.adjuntarFirmaDocumento(ticket, refDoc, getDataFile("files/Xades2.xml"), "XADES-BES");
        alfClient.adjuntarVistaDocumento(ticket, refDoc, "vista.pdf", getDataFile("files/Justificante.pdf"));
        Map<String, String> metadatos = alfClient.obtenerMetadatosDocumento(ticket, refDoc);
        printMetadatos(metadatos);
        Fichero doc = alfClient.obtenerDocumento(ticket, refDoc);
        saveFile(doc, "files_download/bandejaentrada");
        Fichero vista = alfClient.obtenerVistaDocumento(ticket, refDoc);
        saveFile(vista, "files_download/bandejaentrada");
        Firma[] firmas = alfClient.obtenerFirmasDocumento(ticket, refDoc);
        saveFirmas(firmas, "files_download/bandejaentrada");
        alfClient.eliminarDocumento(ticket, refDoc);
        alfClient.existeDocumentoBandejaEntrada(ticket, anyo, tipo, numero, codDocumento);
    }

    private static void testExpediente() throws Exception {
        System.out.println("------------------ testExpediente");
        String expe = "EXPE/1234/2010";
        String tipoProc = "Tipo 1";
        String anyo = "2010";
        String codDocumento = Long.toString(System.currentTimeMillis());
        AlfrescoClientCIM alfClient = new AlfrescoClientCIM(url);
        String ticket = alfClient.loginPost(USU_EXPE, PASS_EXPE);
        String refDoc = alfClient.insertarDocumentoExpediente(ticket, expe, tipoProc, anyo, "Documento de test", "asiento.xml", getDataFile("files/asiento.xml"), codDocumento);
        alfClient.existeDocumentoExpediente(ticket, anyo, tipoProc, expe, codDocumento);
        alfClient.adjuntarVistaDocumento(ticket, refDoc, "vista.pdf", getDataFile("files/Justificante.pdf"));
        alfClient.adjuntarFirmaDocumento(ticket, refDoc, getDataFile("files/Xades.xml"), "XADES-BES");
        alfClient.adjuntarFirmaDocumento(ticket, refDoc, getDataFile("files/Xades2.xml"), "XADES-BES");
        Map<String, String> metadatos = alfClient.obtenerMetadatosDocumento(ticket, refDoc);
        printMetadatos(metadatos);
        Fichero doc = alfClient.obtenerDocumento(ticket, refDoc);
        saveFile(doc, "files_download/bandejaentrada");
        Fichero vista = alfClient.obtenerVistaDocumento(ticket, refDoc);
        saveFile(vista, "files_download/bandejaentrada");
        Firma[] firmas = alfClient.obtenerFirmasDocumento(ticket, refDoc);
        saveFirmas(firmas, "files_download/bandejaentrada");
        alfClient.eliminarDocumento(ticket, refDoc);
        alfClient.existeDocumentoExpediente(ticket, anyo, tipoProc, expe, codDocumento);
    }

    private static void testBandejaSalida() throws Exception {
        System.out.println("------------------ testBandejaSalida");
        String anyo = "2010";
        String expe = "EXP/123/2010";
        String codigoDocumento = Long.toString(System.currentTimeMillis());
        String fecha = fechaACadena(new Date());
        String tipo = "notificacion";
        String numero = "14/111/2010";
        AlfrescoClientCIM alfClient = new AlfrescoClientCIM(url);
        String ticket = alfClient.loginPost(USU_BANSAL, PASS_BANSAL);
        String refDoc = alfClient.insertarDocumentoBandejaSalida(ticket, tipo, numero, fecha, expe, anyo, "Documento de test", "asiento.xml", getDataFile("files/asiento.xml"), codigoDocumento);
        alfClient.existeDocumentoBandejaSalida(ticket, anyo, tipo, numero, codigoDocumento);
        alfClient.adjuntarFirmaDocumento(ticket, refDoc, getDataFile("files/Xades.xml"), "XADES-BES");
        alfClient.adjuntarFirmaDocumento(ticket, refDoc, getDataFile("files/Xades2.xml"), "XADES-BES");
        alfClient.adjuntarVistaDocumento(ticket, refDoc, "vista.pdf", getDataFile("files/Justificante.pdf"));
        Map<String, String> metadatos = alfClient.obtenerMetadatosDocumento(ticket, refDoc);
        printMetadatos(metadatos);
        Fichero doc = alfClient.obtenerDocumento(ticket, refDoc);
        saveFile(doc, "files_download/bandejaentrada");
        Fichero vista = alfClient.obtenerVistaDocumento(ticket, refDoc);
        saveFile(vista, "files_download/bandejaentrada");
        Firma[] firmas = alfClient.obtenerFirmasDocumento(ticket, refDoc);
        saveFirmas(firmas, "files_download/bandejaentrada");
        alfClient.eliminarDocumento(ticket, refDoc);
        alfClient.existeDocumentoBandejaSalida(ticket, anyo, tipo, numero, codigoDocumento);
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

    private static byte[] getDataFile(String filename) throws Exception {
        filename = "moduls/alfrescoClient/test/" + filename;
        FileInputStream fis = new FileInputStream(filename);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        copy(fis, bos);
        return bos.toByteArray();
    }

    private static String fechaACadena(Date fecha) {
        if (fecha == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(fecha);
    }

    private static byte[] readInputStream(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        try {
            copy(is, bos);
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    private static void prepareDirs() {
        prepareDir(new File("moduls/alfrescoClient/test/files_download/bandejaentrada"));
        prepareDir(new File("moduls/alfrescoClient/test/files_download/bandejasalida"));
        prepareDir(new File("moduls/alfrescoClient/test/files_download/expediente"));
    }

    private static void prepareDir(File dir) {
        if (dir.exists()) {
            recursiveDelete(dir);
        } else {
            dir.mkdirs();
        }
    }

    private static void recursiveDelete(File dirPath) {
        String[] ls = dirPath.list();
        for (int idx = 0; idx < ls.length; idx++) {
            File file = new File(dirPath, ls[idx]);
            if (file.isDirectory()) recursiveDelete(file);
            file.delete();
        }
    }

    private static void printMetadatos(Map<String, String> metadatos) {
        for (Iterator it = metadatos.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            System.out.println("Metadato: " + key + " - Valor: " + metadatos.get(key));
        }
    }

    private static void saveFile(Fichero f, String path) throws Exception {
        path = "moduls/alfrescoClient/test/" + path;
        ByteArrayInputStream bis = new ByteArrayInputStream(f.getContenidoFichero());
        copy(bis, new FileOutputStream(path + "/" + f.getNombreFichero()));
        System.out.println("Downloaded to: " + path + "/" + f.getNombreFichero());
    }

    private static void saveFirmas(Firma f[], String path) throws Exception {
        for (int i = 0; i < f.length; i++) {
            saveFile(f[i].getFichero(), path);
        }
    }
}
