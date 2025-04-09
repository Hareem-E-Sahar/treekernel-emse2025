package org.jcvi.fastX.fasta;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author dkatzel
 *
 *
 */
public class FastaUtil {

    public static final String CR = "\n";

    public static final char HEADER_PREFIX = '>';

    public static long calculateCheckSum(CharSequence data) {
        final Checksum checksummer = new CRC32();
        for (int i = 0; i < data.length(); i++) {
            checksummer.update(data.charAt(i));
        }
        return checksummer.getValue();
    }
}
