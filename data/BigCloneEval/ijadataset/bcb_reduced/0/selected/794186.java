package org.iosgi.outpost.operations;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.iosgi.outpost.Operation;
import org.iosgi.util.io.Streams;

/**
 * @author Sven Schulz
 */
public class Exec implements Operation<Integer>, Serializable {

    private static final long serialVersionUID = 1520027915858678197L;

    private final File workDir;

    private final List<String> command;

    private final boolean block;

    private final File out, err;

    public Exec(File workDir, List<String> command) {
        this(workDir, command, true, null, null);
    }

    public Exec(File workDir, List<String> command, boolean block, File out, File err) {
        this.workDir = workDir;
        this.command = command;
        this.block = block;
        this.out = out;
        this.err = err;
    }

    @Override
    public Integer perform() throws Exception {
        ProcessBuilder b = new ProcessBuilder();
        b.command(command);
        b.directory(workDir);
        Process p = b.start();
        if (out != null) Streams.drain(p.getInputStream(), out);
        if (err != null) Streams.drain(p.getErrorStream(), err);
        return block ? p.waitFor() : 0;
    }
}
