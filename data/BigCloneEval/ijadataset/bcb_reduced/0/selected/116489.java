package net.sf.kpex.io;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

public class IO {

    public static Applet applet = null;

    public static final Reader input = toReader(System.in);

    public static long maxAnswers = 0;

    public static Writer output = toWriter(System.out);

    public static IOPeer peer = null;

    public static boolean showErrors = true;

    public static boolean showOutput = true;

    public static final int showTrace = 0;

    static final int MAXBUF = 1 << 30;

    public static final void assertion(String Mes) {
        IO.errmes("assertion failed", new java.lang.Exception(Mes));
    }

    public static final void errmes(String s) {
        if (showErrors) {
            println(getStdOutput(), s);
        }
    }

    public static final synchronized void errmes(String s, Throwable e) {
        errmes(s);
        printStackTrace(e);
    }

    public static final URL find_url(String s) {
        String valid = null;
        Reader stream;
        String baseDir = getBaseDir();
        valid = baseDir + s;
        stream = url2stream(valid, true);
        if (null == stream) {
            valid = s;
            stream = url2stream(valid, true);
        }
        try {
            stream.close();
        } catch (IOException e) {
            valid = null;
        }
        URL url = null;
        try {
            url = new URL(valid);
        } catch (MalformedURLException e) {
        }
        return url;
    }

    public static String getBaseDir() {
        if (null == applet) {
            return "";
        }
        String appletURL = applet.getCodeBase().toString();
        int slash = appletURL.lastIndexOf('/');
        if (slash >= 0) {
            appletURL = appletURL.substring(0, slash + 1);
        }
        return appletURL;
    }

    public static Reader getStdInput() {
        return input;
    }

    public static Writer getStdOutput() {
        return output;
    }

    public static final void mes(String s) {
        println(getStdOutput(), s);
    }

    public static final void print(String s) {
        print(getStdOutput(), s);
    }

    public static final synchronized void print(Writer f, String s) {
        if (!showOutput) {
            return;
        }
        if (peer == null) {
            try {
                f.write(s);
                f.flush();
            } catch (IOException e) {
                System.err.println("*** error in printing: " + e);
            }
        } else {
            peer.print(s);
        }
        return;
    }

    public static final void println(String s) {
        println(getStdOutput(), s);
    }

    public static final void println(Writer o, String s) {
        print(o, s + "\n");
    }

    public static void printStackTrace(Throwable e) {
        if (showErrors) {
            CharArrayWriter b = new CharArrayWriter();
            PrintWriter fb = new PrintWriter(b);
            e.printStackTrace(fb);
            IO.errmes(b.toString());
            fb.close();
        }
    }

    public static final String promptln(String prompt) {
        print(prompt);
        return readln();
    }

    public static final String readln() {
        String s;
        if (peer == null) {
            s = readln(getStdInput());
        } else {
            s = peer.readln();
        }
        return s;
    }

    public static final Reader string_to_stream(String s) throws IOException {
        StringReader stream = new StringReader(s);
        return stream;
    }

    public static final int system(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            IO.errmes("error in system cmd: " + cmd, e);
            return 0;
        }
        return 1;
    }

    public static Reader toFileReader(String fname) {
        return url_or_file(fname);
    }

    public static Writer toFileWriter(String s) {
        Writer f = null;
        try {
            f = toWriter(new FileOutputStream(s));
        } catch (IOException e) {
            errmes("write error, to: " + s);
        }
        return f;
    }

    public static final void trace(int level, String s) {
        if (!showOutput || showTrace < level) {
            return;
        }
        if (peer == null) {
            println(getStdOutput(), s);
        } else {
            peer.traceln(s);
        }
    }

    public static final void trace(String s) {
        if (showTrace >= 1) {
            println(getStdOutput(), s);
        }
    }

    public static final Reader url_or_file(String s) {
        Reader stream = null;
        try {
            if (applet != null) {
                String baseDir = getBaseDir();
                stream = url2stream(baseDir + s, true);
            }
            if (null == stream) {
                stream = url2stream(s, true);
            }
            if (null == stream && null == IO.applet) {
                stream = toReader(new FileInputStream(s));
            }
        } catch (IOException e) {
        }
        return stream;
    }

    public static final Reader url2stream(String f) {
        return url2stream(f, false);
    }

    public static final Reader url2stream(String f, boolean quiet) {
        Reader stream = null;
        try {
            URL url = new URL(f);
            stream = toReader(url.openStream());
        } catch (MalformedURLException e) {
            if (quiet) {
                return null;
            }
            IO.errmes("bad URL: " + f, e);
        } catch (IOException e) {
            if (quiet) {
                return null;
            }
            IO.errmes("unable to read URL: " + f, e);
        }
        return stream;
    }

    static final String read_from(Reader f) {
        return readln(f);
    }

    static String readLine(Reader f) throws IOException {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < MAXBUF; i++) {
            int c = f.read();
            if (c == '\0' || c == '\n' || c == -1 || c == '\r' && '\n' == f.read()) {
                if (i == 0 && c == -1) {
                    return null;
                }
                break;
            }
            s.append(c);
        }
        return s.toString();
    }

    static final String readln(Reader f) {
        trace(2, "READLN TRACE: entering");
        String s = null;
        try {
            if (f instanceof BufferedReader) {
                s = ((BufferedReader) f).readLine();
            } else {
                s = readLine(f);
            }
        } catch (IOException e) {
            errmes("error in readln: e.toString()");
        }
        trace(2, "READLN TRACE:" + "<" + s + ">");
        return s;
    }

    static Reader toReader(InputStream f) {
        return new BufferedReader(new InputStreamReader(f));
    }

    static Writer toWriter(OutputStream f) {
        return new BufferedWriter(new OutputStreamWriter(f));
    }

    static final void write_to(Writer f, String s) {
        println(f, s);
    }
}
