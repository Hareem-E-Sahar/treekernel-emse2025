package org.xmlsh.commands.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xzip extends XCommand {

    public int run(List<XValue> args) throws Exception {
        Options opts = new Options("f=file:", SerializeOpts.getOptionDefs());
        opts.parse(args);
        XValue zipfile = opts.getOptValue("f");
        args = opts.getRemainingArgs();
        SerializeOpts serializeOpts = getSerializeOpts(opts);
        OutputPort outp = this.getOutput(zipfile, false);
        OutputStream outs = outp.asOutputStream(serializeOpts);
        ZipOutputStream zos = new ZipOutputStream(outs);
        try {
            int ret = 0;
            ret = zip(zos, args);
            zos.finish();
            return ret;
        } finally {
            zos.close();
            outs.close();
            outp.release();
        }
    }

    private int zip(ZipOutputStream zos, List<XValue> args) throws IOException {
        int ret;
        for (XValue v : args) if ((ret = zip(zos, v.toString())) != 0) return ret;
        return 0;
    }

    private int zip(ZipOutputStream zos, String fname) throws IOException {
        int ret;
        File file = getFile(fname);
        if (file.isDirectory()) {
            String[] files = file.list();
            for (String f : files) {
                if ((ret = zip(zos, fname + "/" + f)) != 0) return ret;
            }
            return 0;
        }
        ZipEntry entry = new ZipEntry(fname);
        entry.setTime(file.lastModified());
        zos.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(file);
        Util.copyStream(fis, zos);
        fis.close();
        zos.closeEntry();
        return 0;
    }
}
