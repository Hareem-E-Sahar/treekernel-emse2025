import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import es.cim.ESBClient.v1.ServiceESBClientFactory;
import es.cim.ESBClient.v1.documentos.GestorDocumentalESBClient;
import es.cim.ESBClient.v1.documentos.ObtenerDocumentoResponse;
import es.cim.ESBClient.v1.documentos.ObtenerFirmasDocumentoResponse;
import es.cim.ESBClient.v1.documentos.ObtenerMetadatosDocumentoResponse;
import es.cim.ESBClient.v1.documentos.ObtenerVistaDocumentoResponse;
import es.cim.ESBClient.v1.documentos.ReferenciaDocumentoResponse;
import es.cim.ESBClient.v1.documentos.ResultadoResponse;
import es.cim.ESBClient.v1.documentos.TypeFirma;
import es.cim.ESBClient.v1.documentos.TypeMetaDato;

public class TestGestorDocumental {

    private String urlService = "https://sistraper01:9443/services/ServicioGestorDocumental";

    private boolean generateTimestamp = true;

    private boolean logCalls = true;

    private boolean disableCnCheck = true;

    private ServiceESBClientFactory sf = null;

    private GestorDocumentalESBClient gdService = null;

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        prepareDirs();
        TestGestorDocumental test = new TestGestorDocumental();
        try {
            if (!test.testBandejaEntrada("sis_sistra", "sis_sistra1")) return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void prepareDirs() {
        prepareDir(new File("moduls/ESBClient/test/files_download/bandejaentrada"));
        prepareDir(new File("moduls/ESBClient/test/files_download/bandejasalida"));
        prepareDir(new File("moduls/ESBClient/test/files_download/expediente"));
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

    public TestGestorDocumental() {
        this.sf = new ServiceESBClientFactory(generateTimestamp, logCalls, disableCnCheck);
        this.gdService = sf.getGestorDocumentalESBClient();
    }

    public boolean testBandejaEntrada(String user, String passwd) throws Exception {
        this.gdService.setAutenticacion(urlService, user, passwd);
        String numero = "14/111/2040";
        String tipo = "ENVIO";
        String anyo = "2040";
        String numEntrada = "BTE/334/2040";
        String codDocumento = Long.toString(System.currentTimeMillis());
        System.out.println("------------------ testBandejaEntrada");
        ReferenciaDocumentoResponse res = gdService.insertarDocumentoBandejaEntrada(tipo, numero, new Date(), numEntrada, anyo, "Documento de Prueba", "asiento.xml", getDataFile("files/asiento.xml"), codDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: insertarDocumentoBandejaEntrada");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        res = gdService.existeDocumentoBandejaEntrada(anyo, tipo, numero, codDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: existeDocumentoBandejaEntrada");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ResultadoResponse rg = gdService.adjuntarFirmaDocumento(res.getReferenciaDocumento(), getDataFile("files/Xades.xml"), "XADES-BES");
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarFirmaDocumento Xades.xml");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.adjuntarFirmaDocumento(res.getReferenciaDocumento(), getDataFile("files/Xades2.xml"), "XADES-BES");
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarFirmaDocumento Xades2.xml");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.adjuntarVistaDocumento(res.getReferenciaDocumento(), "vista.pdf", getDataFile("files/Justificante.pdf"));
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarVistaDocumento");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerMetadatosDocumentoResponse omdr = gdService.obtenerMetadatosDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerMetadatosDocumento");
        System.out.println("Codigo: " + omdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + omdr.getResultado().getDescripcionError());
        System.out.println("--------MetaDatos-----------");
        printMetadatos(omdr.getMetadatos());
        System.out.println("----------------------------");
        if (!("OK".equals(omdr.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerDocumentoResponse odr = gdService.obtenerDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerDocumento");
        System.out.println("Codigo: " + odr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + odr.getResultado().getDescripcionError());
        System.out.println("Nombre: " + odr.getNombreDocumento());
        System.out.println("-----Guardamos------------");
        saveFile(odr.getContenidoDocumento(), odr.getNombreDocumento(), "files_download/bandejaentrada");
        System.out.println("----------------------------");
        if (!("OK".equals(odr.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerVistaDocumentoResponse ovdr = gdService.obtenerVistaDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerVistaDocumento");
        System.out.println("Codigo: " + ovdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + ovdr.getResultado().getDescripcionError());
        System.out.println("Nombre: " + ovdr.getNombreVista());
        System.out.println("-----Guardamos------------");
        saveFile(ovdr.getContenidoVista(), ovdr.getNombreVista(), "files_download/bandejaentrada");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerFirmasDocumentoResponse ofdr = gdService.obtenerFirmasDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerFirmasDocumento");
        System.out.println("Codigo: " + ofdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + ofdr.getResultado().getDescripcionError());
        System.out.println("-----Guardamos------------");
        saveFirmas(ofdr.getFirmas(), "files_download/bandejaentrada");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.eliminarDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: eliminarDocumento");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        res = gdService.existeDocumentoBandejaEntrada(anyo, tipo, numero, codDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: existeDocumentoBandejaEntrada");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        return true;
    }

    public boolean testBandejaSalida(String user, String passwd) throws Exception {
        this.gdService.setAutenticacion(urlService, user, passwd);
        String anyo = "2040";
        String expe = "EXP/123/2040";
        String codigoDocumento = Long.toString(System.currentTimeMillis());
        String tipo = "NOTIFICACION";
        String numero = "14/111/2040";
        System.out.println("------------------ testBandejaSalida");
        ReferenciaDocumentoResponse res = gdService.insertarDocumentoBandejaSalida(tipo, numero, new Date(), expe, anyo, "Documento de Prueba", "asiento.xml", getDataFile("files/asiento.xml"), codigoDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: insertarDocumentoBandejaSalida");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        res = gdService.existeDocumentoBandejaSalida(anyo, tipo, numero, codigoDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: existeDocumentoBandejaSalida");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ResultadoResponse rg = gdService.adjuntarFirmaDocumento(res.getReferenciaDocumento(), getDataFile("files/Xades.xml"), "XADES-BES");
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarFirmaDocumento Xades.xml");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.adjuntarFirmaDocumento(res.getReferenciaDocumento(), getDataFile("files/Xades2.xml"), "XADES-BES");
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarFirmaDocumento Xades2.xml");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.adjuntarVistaDocumento(res.getReferenciaDocumento(), "vista.pdf", getDataFile("files/Justificante.pdf"));
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarVistaDocumento");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerMetadatosDocumentoResponse omdr = gdService.obtenerMetadatosDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerMetadatosDocumento");
        System.out.println("Codigo: " + omdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + omdr.getResultado().getDescripcionError());
        System.out.println("--------MetaDatos-----------");
        printMetadatos(omdr.getMetadatos());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerDocumentoResponse odr = gdService.obtenerDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerDocumento");
        System.out.println("Codigo: " + odr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + odr.getResultado().getDescripcionError());
        System.out.println("Nombre: " + odr.getNombreDocumento());
        System.out.println("-----Guardamos------------");
        saveFile(odr.getContenidoDocumento(), odr.getNombreDocumento(), "files_download/bandejaSalida");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerVistaDocumentoResponse ovdr = gdService.obtenerVistaDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerVistaDocumento");
        System.out.println("Codigo: " + ovdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + ovdr.getResultado().getDescripcionError());
        System.out.println("Nombre: " + ovdr.getNombreVista());
        System.out.println("-----Guardamos------------");
        saveFile(ovdr.getContenidoVista(), ovdr.getNombreVista(), "files_download/bandejaSalida");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerFirmasDocumentoResponse ofdr = gdService.obtenerFirmasDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerFirmasDocumento");
        System.out.println("Codigo: " + ofdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + ofdr.getResultado().getDescripcionError());
        System.out.println("-----Guardamos------------");
        saveFirmas(ofdr.getFirmas(), "files_download/bandejaSalida");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.eliminarDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: eliminarDocumento");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        res = gdService.existeDocumentoBandejaSalida(anyo, tipo, numero, codigoDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: existeDocumentoBandejaSalida");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        return true;
    }

    public boolean testExpediente(String user, String passwd) throws Exception {
        this.gdService.setAutenticacion(urlService, user, passwd);
        String numero = "EXPE/1234/2040";
        String tipo = "tipo 1";
        String anyo = "2040";
        String codigoDocumento = Long.toString(System.currentTimeMillis());
        System.out.println("------------------ testExpediente");
        ReferenciaDocumentoResponse res = gdService.insertarDocumentoExpediente(numero, tipo, anyo, "Documento de Prueba", "asiento.xml", getDataFile("files/asiento.xml"), codigoDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: insertarDocumentoExpediente");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        res = gdService.existeDocumentoExpediente(anyo, tipo, numero, codigoDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: existeDocumentoExpediente");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ResultadoResponse rg = gdService.adjuntarFirmaDocumento(res.getReferenciaDocumento(), getDataFile("files/Xades.xml"), "XADES-BES");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarFirmaDocumento Xades.xml");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        rg = gdService.adjuntarFirmaDocumento(res.getReferenciaDocumento(), getDataFile("files/Xades2.xml"), "XADES-BES");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarFirmaDocumento Xades2.xml");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        rg = gdService.adjuntarVistaDocumento(res.getReferenciaDocumento(), "vista.pdf", getDataFile("files/Justificante.pdf"));
        System.out.println("----------------------------");
        System.out.println("Test: adjuntarVistaDocumento");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerMetadatosDocumentoResponse omdr = gdService.obtenerMetadatosDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerMetadatosDocumento");
        System.out.println("Codigo: " + omdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + omdr.getResultado().getDescripcionError());
        System.out.println("--------MetaDatos-----------");
        printMetadatos(omdr.getMetadatos());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerDocumentoResponse odr = gdService.obtenerDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerDocumento");
        System.out.println("Codigo: " + odr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + odr.getResultado().getDescripcionError());
        System.out.println("Nombre: " + odr.getNombreDocumento());
        System.out.println("-----Guardamos------------");
        saveFile(odr.getContenidoDocumento(), odr.getNombreDocumento(), "files_download/expediente");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerVistaDocumentoResponse ovdr = gdService.obtenerVistaDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerVistaDocumento");
        System.out.println("Codigo: " + ovdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + ovdr.getResultado().getDescripcionError());
        System.out.println("Nombre: " + ovdr.getNombreVista());
        System.out.println("-----Guardamos------------");
        saveFile(ovdr.getContenidoVista(), ovdr.getNombreVista(), "files_download/expediente");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        ObtenerFirmasDocumentoResponse ofdr = gdService.obtenerFirmasDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: obtenerFirmasDocumento");
        System.out.println("Codigo: " + ofdr.getResultado().getCodigoError());
        System.out.println("DescripionError: " + ofdr.getResultado().getDescripcionError());
        System.out.println("-----Guardamos------------");
        saveFirmas(ofdr.getFirmas(), "files_download/expediente");
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        rg = gdService.eliminarDocumento(res.getReferenciaDocumento());
        System.out.println("----------------------------");
        System.out.println("Test: eliminarDocumento");
        System.out.println("Codigo: " + rg.getCodigoError());
        System.out.println("DescripionError: " + rg.getDescripcionError());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        res = gdService.existeDocumentoExpediente(anyo, tipo, numero, codigoDocumento);
        System.out.println("----------------------------");
        System.out.println("Test: existeDocumentoExpediente");
        System.out.println("Codigo: " + res.getResultado().getCodigoError());
        System.out.println("DescripionError: " + res.getResultado().getDescripcionError());
        System.out.println("Referencia: " + res.getReferenciaDocumento());
        System.out.println("----------------------------");
        if (!("OK".equals(res.getResultado().getCodigoError()))) {
            return false;
        }
        return true;
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
        filename = "moduls/ESBClient/test/" + filename;
        FileInputStream fis = new FileInputStream(filename);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        copy(fis, bos);
        return bos.toByteArray();
    }

    private static void printMetadatos(List<TypeMetaDato> metadatos) {
        if (metadatos == null) return;
        for (Iterator it = metadatos.iterator(); it.hasNext(); ) {
            TypeMetaDato mt = (TypeMetaDato) it.next();
            System.out.println("Metadato: " + mt.getNombre() + " - Valor: " + mt.getValor());
        }
    }

    private static void saveFile(byte[] content, String nombreDocumento, String path) throws Exception {
        if (content == null || content.length == 0) {
            return;
        }
        path = "moduls/ESBClient/test/" + path;
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        copy(bis, new FileOutputStream(path + "/" + nombreDocumento));
        System.out.println("Downloaded to: " + path + "/" + nombreDocumento);
    }

    private static void saveFirmas(List<TypeFirma> firmas, String path) throws Exception {
        int i = 0;
        for (Iterator<TypeFirma> it = firmas.iterator(); it.hasNext(); i++) {
            TypeFirma tf = it.next();
            saveFile(tf.getFirmaElectronica(), "firma" + i + ".xml", path);
        }
    }
}
