package net.sourceforge.filebot.ui.sfv;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.SwingWorker;
import net.sourceforge.filebot.hash.Hash;
import net.sourceforge.filebot.hash.HashType;

class ChecksumComputationTask extends SwingWorker<Map<HashType, String>, Void> {

    private static final int BUFFER_SIZE = 32 * 1024;

    private final File file;

    private final HashType hashType;

    public ChecksumComputationTask(File file, HashType hashType) {
        this.file = file;
        this.hashType = hashType;
    }

    @Override
    protected Map<HashType, String> doInBackground() throws Exception {
        Hash hash = hashType.newHash();
        long length = file.length();
        InputStream in = new FileInputStream(file);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            long position = 0;
            int len = 0;
            while ((len = in.read(buffer)) >= 0) {
                position += len;
                hash.update(buffer, 0, len);
                setProgress((int) ((position * 100) / length));
                if (isCancelled() || Thread.interrupted()) {
                    throw new CancellationException();
                }
            }
        } finally {
            in.close();
        }
        return Collections.singletonMap(hashType, hash.digest());
    }
}
