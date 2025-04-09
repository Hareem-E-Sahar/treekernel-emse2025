package properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Imi
 */
public class ClanSession {

    BufferedReader reader = null;

    BufferedWriter writer = null;

    int anchor = 0;

    /**
	 * Maps the anchors in the session to objects already created by ClanSession.
	 * An object created this way can request to all other objects this way.
	 */
    List anchorList = new ArrayList();

    public ClanSession(Reader r) {
        if (r instanceof BufferedReader) reader = (BufferedReader) r; else reader = new BufferedReader(r);
    }

    public ClanSession(Writer w) {
        if (w instanceof BufferedWriter) writer = (BufferedWriter) w; else writer = new BufferedWriter(w);
    }

    public String readString() throws IOException {
        return reader.readLine();
    }

    public int readInt() throws IOException {
        return Integer.parseInt(readString());
    }

    public boolean readBoolean() throws IOException {
        return readInt() == 0 ? false : true;
    }

    public boolean ready() throws IOException {
        return reader.ready();
    }

    public void write(String s) throws IOException {
        writer.write(s);
        writer.newLine();
    }

    public void write(int s) throws IOException {
        write(String.valueOf(s));
    }

    public void write(boolean s) throws IOException {
        write(s ? 1 : 0);
    }

    public ClanSessionable readObject() throws Exception {
        Class[] paramTypes = new Class[1];
        paramTypes[0] = getClass();
        Constructor c = Class.forName(readString()).getConstructor(paramTypes);
        ClanSession[] param = new ClanSession[1];
        param[0] = this;
        ClanSessionable ret = (ClanSessionable) c.newInstance(param);
        anchorList.add(ret);
        return ret;
    }

    public void write(ClanSessionable f) throws IOException {
        writer.write(f.getClass().getName());
        writer.newLine();
    }

    public void close() throws IOException {
        if (writer != null) writer.close();
        if (reader != null) reader.close();
    }

    public ClanSessionable getAnchor(int anchorKey) {
        return (ClanSessionable) anchorList.get(anchor);
    }

    public int getAnchorKey(ClanSessionable anchor) {
        return anchorList.indexOf(anchor);
    }
}
