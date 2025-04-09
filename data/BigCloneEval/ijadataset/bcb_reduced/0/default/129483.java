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

/**
 * Clase con utilidades
 * 
 */
public class Utilidades {

    public static final String FORMATO_FECHA = "dd/MM/yyyy HH:mm";

    /**
	 * Serializa fecha a una cadea
	 * @param fecha
	 * @return
	 */
    public static String fechaACadena(Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(FORMATO_FECHA);
        return sdf.format(fecha);
    }

    /**
	 * Deserializa fecha de una cadea
	 * @param fecha
	 * @return
	 */
    public static Date cadenaAFecha(String fecha) {
        try {
            if (fecha == null) return null;
            SimpleDateFormat sdf = new SimpleDateFormat(FORMATO_FECHA);
            return sdf.parse(fecha);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
	 * Copia el contenido de un InputStream en un OutputStream
	 * 
	 * @param input
	 * @param output
	 * @return
	 * @throws IOException
	 */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte buffer[] = new byte[4096];
        int count = 0;
        for (int n = 0; -1 != (n = input.read(buffer)); ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
	 * Obtiene el contenido de un fichero en bytes 
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
    public static byte[] getBytesFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        return getBytesFromFile(file);
    }

    /**
	 * Obtiene el contenido de un fichero en bytes 
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
    public static void saveBytesToFile(String filePath, byte[] content) throws Exception {
        File file = new File(filePath);
        saveBytesToFile(file, content);
    }

    /**
	 * Obtiene el contenido de un fichero en bytes 
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
    public static byte[] getBytesFromFile(File file) throws Exception {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        try {
            fis = new FileInputStream(file);
            copy(fis, bos);
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new Exception("Excepcion obteniendo bytes del fichero: " + file.getName(), ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex2) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception ex2) {
                }
            }
        }
    }

    /**
	 * Obtiene el contenido de un fichero en bytes 
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
    public static void saveBytesToFile(File file, byte[] content) throws Exception {
        FileOutputStream fos = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        try {
            fos = new FileOutputStream(file);
            copy(bis, fos);
        } catch (Exception ex) {
            throw new Exception("Excepcion guardando bytes en el fichero: " + file.getName(), ex);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception ex2) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex2) {
                }
            }
        }
    }

    public static byte[] base64ToBytes(String cadenaB64) throws Exception {
        return new sun.misc.BASE64Decoder().decodeBuffer(cadenaB64);
    }
}
