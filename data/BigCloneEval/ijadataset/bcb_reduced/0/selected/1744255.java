package org.armedbear.lisp;

import static org.armedbear.lisp.Nil.NIL;
import static org.armedbear.lisp.Lisp.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class zip extends Primitive {

    private zip() {
        super("zip", PACKAGE_SYS, true, "pathname pathnames");
    }

    @Override
    public LispObject execute(LispObject first, LispObject second) throws ConditionThrowable {
        Pathname zipfilePathname = coerceToPathname(first);
        byte[] buffer = new byte[4096];
        try {
            String zipfileNamestring = zipfilePathname.getNamestring();
            if (zipfileNamestring == null) return error(new SimpleError("Pathname has no namestring: " + zipfilePathname.writeToString()));
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfileNamestring));
            LispObject list = second;
            while (list != NIL) {
                Pathname pathname = coerceToPathname(list.CAR());
                String namestring = pathname.getNamestring();
                if (namestring == null) {
                    out.close();
                    File zipfile = new File(zipfileNamestring);
                    zipfile.delete();
                    return error(new SimpleError("Pathname has no namestring: " + pathname.writeToString()));
                }
                File file = new File(namestring);
                FileInputStream in = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(file.getName());
                out.putNextEntry(entry);
                int n;
                while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                out.closeEntry();
                in.close();
                list = list.CDR();
            }
            out.close();
        } catch (IOException e) {
            return error(new LispError(e.getMessage()));
        }
        return zipfilePathname;
    }

    private static final Primitive zip = new zip();
}
