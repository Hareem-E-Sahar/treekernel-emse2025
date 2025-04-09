package net.sf.ninjakore.utils.perl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.ninjakore.utils.Utils;
import org.apache.log4j.Logger;

public class PerlUtils {

    private static Logger logger = Logger.getLogger(PerlUtils.class);

    /**
	 * <p>
	 * This function emulates Perl's qw function. Note that only parentheses are
	 * supported as delimiters.
	 * <p>
	 * From http://www.perlmeme.org/howtos/perlfunc/qw_function.html<br>
	 * Extracts words out of your string using embedded whitspace as the
	 * delimiter and returns the words as a list.
	 * <p>
	 * The 'quote word' function qw() is used to generate a list of words. It
	 * takes a string such as: <br>
	 * {@code tempfile tempdir} <br>
	 * and returns a quoted list: <br>
	 * {@code tempfile', 'tempdir'} <br>
	 * saving you from the tedium of having to quote and comma-separate each
	 * element of the list by hand.
	 * 
	 * @param elements
	 *            the whitespace separated list of elements to tokenize
	 * @return an array of {@link String} containing the tokenized elements
	 */
    public static String[] qw(String elements) {
        return elements.split(" ");
    }

    /**
	 * <p>
	 * A minimal Java implementation of Perl's unpack function. The syntax is
	 * slightly modified mainly due to language differences (Java being
	 * statically typed vs. Perl being dynamically typed, among other things).
	 * <p>
	 * Note that using the type code "Z" will return a {@link String} instead of
	 * a null-terminated byte[] (C-String).
	 * <p>
	 * From the perldocs:<br>
	 * Takes a string and expands it out into a list of values.
	 * <p>
	 * The string is broken into chunks described by the TEMPLATE. Each chunk is
	 * converted separately to a value. Typically, either the string is a result
	 * of pack, or the characters of the string represent a C structure of some
	 * kind.
	 * <p>
	 * Typically, each converted value looks like its machine-level
	 * representation. For example, on 32-bit machines an integer may be
	 * represented by a sequence of 4 bytes that will be converted to a sequence
	 * of 4 characters.
	 * <p>
	 * The TEMPLATE is a sequence of characters that give the order and type of
	 * values, as follows:
	 * <ul>
	 * <li>a A string with arbitrary binary data, will be null padded.
	 * <li>Z A null terminated (ASCIZ) string, will be null padded.
	 * <li>C An unsigned char (octet) value.
	 * <li>v An unsigned short (16-bit) in "VAX" (little-endian) order.
	 * <li>V An unsigned long (32-bit) in "VAX" (little-endian) order.
	 * <li>x A null byte.
	 * </ul>
	 * 
	 * @param template
	 * @param scalar
	 * @param keys
	 * @return
	 * @throws UnsupportedOperationException
	 *             if a template type is not recognized
	 */
    public static Object[] unpack(String template, byte[] scalar) {
        PackingTemplate packer = new PackingTemplate(template);
        ByteBuffer buffer = ByteBuffer.wrap(scalar);
        List<Object> values = new ArrayList<Object>();
        for (PackingCode code : packer.getPackingCodes()) {
            for (Object value : code.unpack(buffer)) {
                if (value != null) values.add(value);
            }
        }
        return values.toArray();
    }

    public static byte[] pack(String template, Object... values) {
        logger.trace("processing template: " + template);
        PackingTemplate packer = new PackingTemplate(template);
        List<byte[]> buffers = new ArrayList<byte[]>();
        List<PackingCode> codes = packer.getPackingCodes();
        int valueIndex = 0;
        int bufferSize = 0;
        for (PackingCode code : codes) {
            ByteBuffer buffer = null;
            if (code instanceof NullByte) {
                buffer = code.pack();
                logger.trace("processing code: NullByte x " + code.reps() + " => " + Arrays.toString(buffer.array()));
                buffers.add(buffer.array());
                bufferSize += buffer.array().length;
            } else {
                int reps = code.reps();
                if (reps < 0) reps = values.length - valueIndex;
                for (int i = 0; i < reps; ++i) {
                    buffer = code.pack(values[valueIndex]);
                    logger.trace("processing code: " + code.getClass().getSimpleName() + " value: " + (values[valueIndex] instanceof byte[] ? Arrays.toString((byte[]) values[valueIndex]) : values[valueIndex]) + " => " + Arrays.toString(buffer.array()));
                    ++valueIndex;
                    buffers.add(buffer.array());
                    bufferSize += buffer.array().length;
                }
            }
        }
        return Utils.concatArrays(buffers);
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putInt(1234567890).putShort((short) 1234);
        buffer.flip();
        byte[] scalar = Arrays.copyOf(buffer.array(), buffer.limit());
        Object[] ret = unpack("Vv", scalar);
        for (Object object : ret) {
            System.out.println(object);
        }
        testRepetitions();
    }

    private static void testRepetitions() {
        String type = "V2";
        Pattern number = Pattern.compile("([0-9]*)");
        Matcher matcher = number.matcher(type);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            System.out.print(start + ":");
            System.out.print(end + "=>");
            System.out.println(type.substring(start, end));
        }
    }
}
