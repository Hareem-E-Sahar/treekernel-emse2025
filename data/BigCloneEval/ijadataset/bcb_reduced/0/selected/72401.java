package net.sourceforge.filebot.hash;

import static net.sourceforge.tuned.FileUtilities.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VerificationUtilities {

    /**
	 * A {@link Pattern} that will match checksums enclosed in brackets ("[]" or "()"). A
	 * checksum string is a hex number with at least 8 digits. Capturing group 0 will contain
	 * the matched checksum string.
	 */
    public static final Pattern EMBEDDED_CHECKSUM = Pattern.compile("(?<=\\[|\\()(\\p{XDigit}{8})(?=\\]|\\))");

    public static String getEmbeddedChecksum(CharSequence string) {
        Matcher matcher = EMBEDDED_CHECKSUM.matcher(string);
        String embeddedChecksum = null;
        while (matcher.find()) {
            embeddedChecksum = matcher.group();
        }
        return embeddedChecksum;
    }

    public static String getHashFromVerificationFile(File file, HashType type, int maxDepth) throws IOException {
        return getHashFromVerificationFile(file.getParentFile(), file, type, 0, maxDepth);
    }

    private static String getHashFromVerificationFile(File folder, File target, HashType type, int depth, int maxDepth) throws IOException {
        if (folder == null || depth > maxDepth) return null;
        for (File verificationFile : folder.listFiles(type.getFilter())) {
            VerificationFileReader parser = new VerificationFileReader(createTextReader(verificationFile), type.getFormat());
            try {
                while (parser.hasNext()) {
                    Entry<File, String> entry = parser.next();
                    File file = new File(folder, entry.getKey().getPath());
                    if (target.equals(file)) {
                        return entry.getValue();
                    }
                }
            } finally {
                parser.close();
            }
        }
        return getHashFromVerificationFile(folder.getParentFile(), target, type, depth + 1, maxDepth);
    }

    public static HashType getHashType(File verificationFile) {
        for (HashType hashType : HashType.values()) {
            if (hashType.getFilter().accept(verificationFile)) return hashType;
        }
        return null;
    }

    public static HashType getHashTypeByExtension(String extension) {
        for (HashType hashType : HashType.values()) {
            if (hashType.getFilter().acceptExtension(extension)) return hashType;
        }
        return null;
    }

    public static String computeHash(File file, HashType type) throws IOException, InterruptedException {
        Hash hash = type.newHash();
        InputStream in = new FileInputStream(file);
        try {
            byte[] buffer = new byte[32 * 1024];
            int len = 0;
            while ((len = in.read(buffer)) >= 0) {
                hash.update(buffer, 0, len);
                if (Thread.interrupted()) throw new InterruptedException();
            }
        } finally {
            in.close();
        }
        return hash.digest();
    }

    /**
	 * Dummy constructor to prevent instantiation.
	 */
    private VerificationUtilities() {
        throw new UnsupportedOperationException();
    }
}
