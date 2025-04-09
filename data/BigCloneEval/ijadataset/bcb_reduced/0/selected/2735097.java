package hu.ihash.common.parser;

import java.io.Reader;
import java.io.StringReader;

/**
 * A factory class for creating parsers.
 *
 * @author Gergely Kiss
 */
public class ParserFactory {

    public static <T extends IParser<?>> T createParser(Class<T> type, String text) {
        try {
            return type.getConstructor(Reader.class).newInstance(new StringReader(text));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
