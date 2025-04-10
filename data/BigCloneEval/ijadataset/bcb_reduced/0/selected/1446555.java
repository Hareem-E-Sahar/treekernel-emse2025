package se.unlogic.standardutils.readwrite;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class ReadWriteUtils {

    public static void transfer(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[8192];
        int count = 0;
        while ((count = reader.read(buf)) >= 0) {
            writer.write(buf, 0, count);
        }
        writer.flush();
    }

    public static void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    public static void closeWriter(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }
}
