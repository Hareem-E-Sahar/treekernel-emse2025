package eu.more.compressionservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.osgi.framework.BundleContext;
import org.soda.dpws.DPWSContext;
import org.soda.dpws.DPWSException;
import eu.more.compressionservice.generated.CompressionService;
import eu.more.compressionservice.generated.CompressionServiceWSDLInfoFactory;
import eu.more.compressionservice.generated.jaxb.AlgorithmDescription;
import eu.more.compressionservice.generated.jaxb.CompressString;
import eu.more.compressionservice.generated.jaxb.CompressStringResponse;
import eu.more.compressionservice.generated.jaxb.DecompressString;
import eu.more.compressionservice.generated.jaxb.DecompressStringFault;
import eu.more.compressionservice.generated.jaxb.DecompressStringResponse;
import eu.more.compressionservice.generated.jaxb.ListAlgorithmsResponse;
import eu.more.compressionservice.generated.jaxb.impl.AlgorithmDescriptionImpl;
import eu.more.compressionservice.generated.jaxb.impl.CompressStringImpl;
import eu.more.compressionservice.generated.jaxb.impl.CompressStringResponseImpl;
import eu.more.compressionservice.generated.jaxb.impl.DecompressStringFaultImpl;
import eu.more.compressionservice.generated.jaxb.impl.DecompressStringImpl;
import eu.more.compressionservice.generated.jaxb.impl.DecompressStringResponseImpl;
import eu.more.compressionservice.generated.jaxb.impl.ListAlgorithmsResponseImpl;
import eu.more.core.internal.MOREService;

/**
 * This service provides de-/compression functionality.
 * <p>
 * With this service the user can de-/compress the data which has to be send or
 * stored.<br>
 * At the moment this service can use only the <i>ZIP</i>-Algorithm for
 * de-/compression. This causes that the <tt>method</tt> field of
 * {@link CompressString} and {@link DecompressString} is not evaluated.
 * </p>
 * 
 * @author akos
 * 
 */
public class CompressionServiceActivator extends MOREService implements CompressionService {

    /**
   * Current avaiable compression algorithm
   */
    public static final String ALGORITHM_ZIP = "ZIP";

    /**
   * Service id of this service
   */
    public static final String MY_SERVICE_ID = "http://www.ist-more.org/CompressionService";

    public void start(BundleContext context) throws Exception {
        Thread.currentThread().setContextClassLoader(thisClassLoader);
        System.out.println("Starting CompressionService");
        SERVICE_ID = MY_SERVICE_ID;
        WSDL_NAME = "CompressionService.wsdl";
        ServiceName = "CompressionService";
        WSDLFactory = CompressionServiceWSDLInfoFactory.class;
        super.start(context, this);
        device.registerAsLocalService(context, CompressionService.class, SERVICE_ID, this);
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping CompressionService");
        device.deRegisterLocalservice(SERVICE_ID);
    }

    /**
   * This method compresses given data via the specified algorithm.
   * 
   * @param context
   *          current {@link DPWSContext} (at the moment not used)
   * @param parameters
   *          data and method as string to compress
   * @return compressed data as utf-8
   * @throws DPWSException
   * 
   */
    public CompressStringResponse compressString(DPWSContext context, CompressString parameters) throws DPWSException {
        if (ALGORITHM_ZIP.equals(parameters.getMethod())) {
            try {
                CompressStringResponse resp = new CompressStringResponseImpl();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
                ZipEntry entry = new ZipEntry("d");
                String tocompress = parameters.getTocompress();
                entry.setSize(tocompress.getBytes("UTF-8").length);
                zipOutputStream.putNextEntry(entry);
                OutputStreamWriter osw = new OutputStreamWriter(zipOutputStream, "UTF-8");
                osw.write(tocompress, 0, tocompress.length());
                osw.close();
                resp.setCompressed(baos.toByteArray());
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
                throw new DPWSException(e);
            }
        }
        throw new DPWSException("Unimplemented algorithm:" + parameters.getMethod());
    }

    /**
   * Decompresses compressed data.
   * 
   * @param context
   *          current {@link DPWSContext} (at the moment not used)
   * @param parameters
   *          compressed data and compressing method as string
   * @return Decompressed data
   * @throws DPWSException
   * 
   */
    public DecompressStringResponse decompressString(DPWSContext context, DecompressString parameters) throws DPWSException {
        if (ALGORITHM_ZIP.equals(parameters.getMethod())) {
            try {
                DecompressStringResponse resp = new DecompressStringResponseImpl();
                StringBuffer sb = new StringBuffer();
                ByteArrayInputStream bais = new ByteArrayInputStream(parameters.getIn());
                ZipInputStream zis = new ZipInputStream(bais);
                InputStreamReader isr = new InputStreamReader(zis, "UTF-8");
                zis.getNextEntry();
                char[] c = new char[500];
                int r = 0;
                boolean read = false;
                while ((r = isr.read(c)) != -1) {
                    sb.append(c, 0, r);
                    read = true;
                }
                if (!read) {
                    throw new DPWSException("Invalid input");
                }
                resp.setOut(sb.toString());
                isr.close();
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
                throw new DPWSException(e);
            }
        }
        throw new DPWSException("Unimplemented algorithm:" + parameters.getMethod());
    }

    /**
   * Lists all avaiable compression algorithms.
   * <p>
   * At the moment only <i>ZIP</i> is avaiable.
   * </p>
   * 
   * @param context
   *          current {@link DPWSContext} (at the moment not used)
   * @param parameters
   * @return list of avaiable compression algorithms
   * @throws DPWSException
   * 
   */
    @SuppressWarnings("unchecked")
    public ListAlgorithmsResponse ListAlgorithms(DPWSContext context, eu.more.compressionservice.generated.jaxb.ListAlgorithms parameters) throws DPWSException {
        ListAlgorithmsResponse resp = new ListAlgorithmsResponseImpl();
        List<AlgorithmDescription> list = resp.getOut();
        AlgorithmDescription alg = new AlgorithmDescriptionImpl();
        alg.setName(ALGORITHM_ZIP);
        alg.setMaxSize(1000 * 1000 * 10);
        list.add(alg);
        return resp;
    }

    /**
   * Calls a test which can be invoked from commandline.
   * 
   * @param args
   *          not used
   * @throws DPWSException
   */
    public static void main(String[] args) throws DPWSException {
        CompressionServiceActivator a = new CompressionServiceActivator();
        String what = "01234567890";
        System.out.println("Compressing " + what);
        CompressString cs = new CompressStringImpl();
        cs.setMethod(ALGORITHM_ZIP);
        cs.setTocompress(what);
        CompressStringResponse csr = a.compressString(null, cs);
        byte[] c = csr.getCompressed();
        System.out.println("Compressed length is " + c.length);
        DecompressString ds = new DecompressStringImpl();
        ds.setMethod(ALGORITHM_ZIP);
        ds.setIn(c);
        DecompressStringResponse dsr = a.decompressString(null, ds);
        String res = dsr.getOut();
        System.out.println("Decompressed is: " + res);
        if (what.equals(res)) {
            System.out.println("OK");
        } else {
            System.out.println("Service is not OK");
        }
    }
}
