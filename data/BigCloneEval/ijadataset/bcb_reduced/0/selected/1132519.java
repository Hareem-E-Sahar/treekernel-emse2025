package de.ddb.conversion.converters;

import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import de.ddb.conversion.ConverterException;
import de.ddb.pica.record.PicaRecord;
import junit.framework.TestCase;

public class PicaPlusToOaiDcConverterTest extends TestCase {

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(PicaPlusToOaiDcConverterTest.class);

    public void testConvert() throws ConverterException, IOException {
        File picaRecordFile = new File("data/p119_test.dat");
        FileInputStream in = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            in = new FileInputStream(picaRecordFile);
            int i;
            while ((i = in.read()) != -1) {
                buffer.write(i);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        PicaPlusToOaiDcConverter converter = new PicaPlusToOaiDcConverter();
        byte[] convertedRecords = converter.convert(buffer.toByteArray(), "x-PICA", "UTF-8");
        logger.info("convertToList() - " + new String(convertedRecords, "UTF-8"));
    }
}
