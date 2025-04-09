package org.xmlsh.commands.posix;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class rm extends XCommand {

    private File getFile(XValue arg, boolean bForce) throws IOException {
        try {
            File f = getFile(arg);
            return f;
        } catch (IOException e) {
            if (!bForce) throw e;
        }
        return null;
    }

    @Override
    public int run(List<XValue> args) throws Exception {
        Options opts = new Options("f,r,rf");
        opts.parse(args);
        args = opts.getRemainingArgs();
        boolean bForce = opts.hasOpt("f") || opts.hasOpt("rf");
        boolean bRecurse = opts.hasOpt("r") || opts.hasOpt("rf");
        for (XValue arg : args) {
            File f = getFile(arg, bForce);
            if (f != null) delete(f, bForce, bRecurse);
        }
        return 0;
    }

    private void delete(File f, boolean force, boolean recurse) {
        if (f.exists()) {
            if (f.isDirectory()) {
                if (!recurse) {
                    printErr("Is a directory: " + f.getPath());
                    return;
                }
                File files[] = f.listFiles();
                for (File subf : files) {
                    delete(subf, force, recurse);
                }
            }
            if (!force && !f.canWrite()) {
                printErr("File is not writable: " + f.getPath());
                return;
            }
            if (!f.delete()) {
                printErr("Error deleting file: " + f.getPath());
            }
        } else if (!force) printErr("File does not exist: " + f.getPath());
    }
}
