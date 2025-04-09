package de.ddb.conversion.converters;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import de.dNb.conversion.rdf.enrichment.SwdPndEnrichedConverter;
import de.ddb.conversion.ConversionParameters;
import de.ddb.conversion.ConverterException;
import junit.framework.TestCase;

public class SwdPndEnrichedConverterTest extends TestCase {

    public void test() throws IOException, ConverterException {
        SwdPndEnrichedConverter converter = new SwdPndEnrichedConverter();
        FileInputStream inputStream = null;
        ByteArrayOutputStream out;
        try {
            inputStream = new FileInputStream("test/input/04126911X.out");
            out = new ByteArrayOutputStream();
            ConversionParameters params = new ConversionParameters();
            params.setSourceCharset("UTF-8");
            params.setTargetCharset("UTF-8");
            converter.convert(inputStream, out, params);
            System.out.println(new String(out.toByteArray(), "UTF-8"));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
