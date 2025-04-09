package queryreport.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import queryreport.exception.QueryReportException;

/**
 * Class to create zip archives.
 * @author robert.mihaly
 *
 */
public class ZipCompress {

    private static final int BUF_SIZE = 2048;

    private static Logger logger = Logger.getLogger(ZipCompress.class);

    /**
	 * Copy file to zip archive.
	 * @param in - file to be zipped
	 * @param out - output zip file
	 * @throws QueryReportException
	 */
    public static void copyZip(String in, String out) throws QueryReportException {
        File fin = new File(in);
        File fout = new File(out);
        BufferedInputStream bin = null;
        ZipOutputStream zout = null;
        try {
            bin = new BufferedInputStream(new FileInputStream(fin), BUF_SIZE);
            zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fout)));
            zout.putNextEntry(new ZipEntry(fin.getName()));
            byte buffer[] = new byte[BUF_SIZE];
            int count = 0;
            while ((count = bin.read(buffer, 0, BUF_SIZE)) != -1) {
                zout.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new QueryReportException(e.getMessage(), e);
        } finally {
            try {
                if (bin != null) bin.close();
                if (zout != null) zout.close();
            } catch (IOException e) {
                throw new QueryReportException(e.getMessage(), e);
            }
        }
    }

    /**
	 * Move file to zip archive.
	 * @param in - file to be zipped
	 * @param out - output zip file
	 * @throws QueryReportException
	 */
    public static void moveZip(String in, String out) throws QueryReportException {
        copyZip(in, out);
        if (!(new File(in).delete())) {
            logger.error("Error deleting " + in);
        }
    }
}
