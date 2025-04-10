package gnu.kawa.functions;

import gnu.text.*;
import java.text.*;
import java.io.Writer;
import java.io.CharArrayWriter;
import gnu.mapping.*;
import kawa.standard.Scheme;
import gnu.lists.AbstractFormat;

public class ObjectFormat extends ReportFormat {

    /** Maxiumum number of characters to show.
   * Truncate any following characters.
   * The value PARAM_UNSPECIFIED means "no limit". */
    int maxChars;

    boolean readable;

    private static ObjectFormat readableFormat;

    private static ObjectFormat plainFormat;

    public static ObjectFormat getInstance(boolean readable) {
        if (readable) {
            if (readableFormat == null) readableFormat = new ObjectFormat(true);
            return readableFormat;
        } else {
            if (plainFormat == null) plainFormat = new ObjectFormat(false);
            return plainFormat;
        }
    }

    public ObjectFormat(boolean readable) {
        this.readable = readable;
        maxChars = PARAM_UNSPECIFIED;
    }

    public ObjectFormat(boolean readable, int maxChars) {
        this.readable = readable;
        this.maxChars = maxChars;
    }

    public int format(Object[] args, int start, Writer dst, FieldPosition fpos) throws java.io.IOException {
        int maxChars = getParam(this.maxChars, -1, args, start);
        if (this.maxChars == PARAM_FROM_LIST) start++;
        return format(args, start, dst, maxChars, readable);
    }

    private static void print(Object obj, OutPort out, boolean readable) {
        boolean saveReadable = out.printReadable;
        AbstractFormat saveFormat = out.objectFormat;
        try {
            out.printReadable = readable;
            AbstractFormat format = readable ? Scheme.writeFormat : Scheme.displayFormat;
            out.objectFormat = format;
            format.writeObject(obj, (gnu.lists.Consumer) out);
        } finally {
            out.printReadable = saveReadable;
            out.objectFormat = saveFormat;
        }
    }

    /**
   * Return false iff truncation.
   * @param maxChars maximum number of characters; -1 means unlimited
   */
    public static boolean format(Object arg, Writer dst, int maxChars, boolean readable) throws java.io.IOException {
        if (maxChars < 0 && dst instanceof OutPort) {
            print(arg, (OutPort) dst, readable);
            return true;
        } else if (maxChars < 0 && dst instanceof CharArrayWriter) {
            OutPort oport = new OutPort(dst);
            print(arg, oport, readable);
            oport.close();
            return true;
        } else {
            CharArrayWriter wr = new CharArrayWriter();
            OutPort oport = new OutPort(wr);
            print(arg, oport, readable);
            oport.close();
            int len = wr.size();
            if (maxChars < 0 || len <= maxChars) {
                wr.writeTo(dst);
                return true;
            } else {
                dst.write(wr.toCharArray(), 0, maxChars);
                return false;
            }
        }
    }

    public static int format(Object[] args, int start, Writer dst, int maxChars, boolean readable) throws java.io.IOException {
        Object arg;
        if (start >= args.length) {
            arg = "#<missing format argument>";
            start--;
            readable = false;
            maxChars = -1;
        } else arg = args[start];
        format(arg, dst, maxChars, readable);
        return start + 1;
    }

    public Object parseObject(String text, java.text.ParsePosition status) {
        throw new RuntimeException("ObjectFormat.parseObject - not implemented");
    }
}
