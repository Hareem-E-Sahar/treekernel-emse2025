package ti.pub;

import java.io.*;
import java.util.*;

/**
 * A Hashstore is like a Hashtable of Hashtables and Hashstores.  In addition,
 * a Hashstore supports writing to and reading from a file.
 * <p>
 * The file format for the Hashstore is as follows:
 * <pre>
 *    HASH_FILE   ::== [HASH_ENTRY]*
 *    HASH_ENTRY  ::== &lt;Key&gt; '=' [HASH_STORE | ARRAY | &lt;Value&gt;] ';'
 *    HASH_STORE  ::== '{' HASH_FILE '}'
 *    ARRAY       ::== '[' HASH_ENTRY [',' HASH_ENTRY]* ']'
 * </pre>
 * The &lt;Key&gt; is used used as the key in the hashtable, and the 
 * &lt;Value&gt; is the value that corresponds to that key.  If the
 * value is a HASH_STORE, then the value is an instance of Hashstore.
 * A comment begins with '#' or '//' and continues to the end of the line.
 * 
 * @author Rob Clark
 * @version 0.1
 */
public class Hashstore extends java.util.Hashtable {

    private static final long serialVersionUID = 8956390575848603585L;

    private String filename = null;

    private int lineOffset = 0;

    private boolean needsSave = false;

    /**
   * Class Constructor.  Create a hashstore initialized from the
   * named file
   *
   * @param filename    the name of the file to read in
   * @exception FileNotFoundException is thrown if the file cannot be
   * found
   */
    public Hashstore(String filename) throws FileNotFoundException {
        this(new File(filename));
    }

    public Hashstore(File file) throws FileNotFoundException {
        this.filename = file.getPath();
        load(new FileReader(file));
    }

    /**
   * Class Constructor.  Create a hashstore initialized from an
   * input reader.
   *
   * @param in          the input stream to load the hashstore from
   */
    public Hashstore(Reader in) {
        this();
        load(in);
    }

    /**
   * Class Constructor.  Creates an empty hashstore.
   */
    public Hashstore() {
        super();
    }

    /**
   * Indicate whether some object is equal to this object.  Two
   * <code>Hashstore</code>s are equal if their keys and values are equal.
   * 
   * @param obj         the object to compare for equality
   */
    public boolean equals(Object obj) {
        if (obj instanceof Hashstore) {
            Hashstore h = (Hashstore) obj;
            int cnt = size();
            if (cnt == h.size()) {
                Enumeration e = keys();
                for (int i = 0; i < cnt; i++) {
                    Object key = e.nextElement();
                    Object val = get(key);
                    Object hval = h.get(key);
                    if ((hval == null) || (!hval.equals(val))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
   * Create a clone of this object.  Keys are not copied, and values are only
   * cloned if they are <code>LinkedList</code>s or <code>Hashstore</code>s.
   * This is a potentially expensive operation.
   * 
   * @return the cloned object
   */
    public Object clone() {
        Hashstore h = (Hashstore) (super.clone());
        for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            Object val = h.get(key);
            if (val instanceof Hashstore) {
                val = ((Hashstore) val).clone();
                h.put(key, val);
            } else if (val instanceof LinkedList) {
                val = cloneLinkedListReq((LinkedList) val);
                h.put(key, val);
            }
        }
        return h;
    }

    private static LinkedList cloneLinkedListReq(LinkedList list) {
        Object arr[] = list.toArray();
        LinkedList newList = new LinkedList();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Hashstore) {
                newList.add(((Hashstore) (arr[i])).clone());
            } else if (arr[i] instanceof LinkedList) {
                newList.add(cloneLinkedListReq((LinkedList) (arr[i])));
            }
        }
        return newList;
    }

    /**
   * Put a value in the table.
   * 
   * @param key          the hashtable key
   * @param value        the value to put
   * @return the previous value or <code>null</code> if none
   */
    public Object put(Object key, Object value) {
        needsSave = true;
        return super.put(key, value);
    }

    /**
   * Determine if this hashtable has changed since last save.
   * 
   * Note: this currently ignore changes which might have happened in a
   *       LinkedList or other non-hashstore child.
   * 
   * @return <code>true</code> if changed since save, else <code>false</code>
   */
    public boolean needsSave() {
        boolean ret = needsSave;
        for (Enumeration e = elements(); e.hasMoreElements() && !ret; ) {
            Object o = e.nextElement();
            if (o instanceof Hashstore) {
                ret = ((Hashstore) o).needsSave();
            }
        }
        return ret;
    }

    public void save() throws IOException {
        if (needsSave()) {
            System.err.println("saving: " + filename);
            FileWriter fw = new FileWriter(filename);
            write(fw);
            fw.flush();
            fw.close();
        }
    }

    /**
   * Get the value corresponding to the specified key, assuming the value
   * is a number.
   * 
   * @param key          the key
   * @return the value cast as a Number
   * @exception NumberFormatException if the number cannot be parsed
   */
    public Number getNumber(String key) throws NumberFormatException {
        String value = getString(key);
        try {
            return Integer.decode(value);
        } catch (NumberFormatException e) {
            return Double.valueOf(value);
        }
    }

    /** @deprecated */
    public Number getNumber(Object key) {
        return getNumber(key.toString());
    }

    /**
   * Get the value corresponding to the specified key, assuming the value
   * is a string.
   * 
   * @param key          the key
   * @return the value cast as a String
   */
    public String getString(String key) {
        return (String) getEntry(key);
    }

    /** @deprecated */
    public String getString(Object key) {
        return getString(key.toString());
    }

    /**
   * Get the value corresponding to the specified key, assuming the value
   * is boolean.
   * 
   * @param key          the key
   * @return the value cast as a boolean
   */
    public boolean getBoolean(String key) {
        String value = getString(key);
        boolean retVal = false;
        if (value != null) {
            retVal = value.equals("true");
        }
        return retVal;
    }

    /** @deprecated */
    public boolean getBoolean(Object key) {
        return getBoolean(key.toString());
    }

    /**
   * Get the value corresponding to the specified key, assuming the value
   * is a list.
   * 
   * @param key          the key
   * @return the value cast as a LinkedList
   */
    public LinkedList getList(String key) {
        return (LinkedList) getEntry(key);
    }

    /** @deprecated */
    public LinkedList getList(Object key) {
        return getList(key.toString());
    }

    /**
   * Get the value corresponding to the specified key, assuming the value
   * is a hashstore.
   * 
   * @param key          the key
   * @return the value cast as a Hashstore
   */
    public Hashstore getHashstore(String key) {
        return (Hashstore) getEntry(key);
    }

    /** @deprecated */
    public Hashstore getHashstore(Object key) {
        return getHashstore(key.toString());
    }

    /**
   * Set an entry in the hashstore.  The format for key is:
   *<pre>
   *  KEY[:SUBKEY[:SUBKEY...]]
   *</pre>
   * where <code>SUBKEY</code> is either a string specifying an entry in
   * a hashstore, if it is a subkey of a hashstore, or an index into an
   * array, if it is a subkey of an array.
   * 
   * @param key         the key (see above)
   * @param value       the value to set the key to (as a string)
   * @return the old value of the specified <code>key</code>, or <code>null</code>
   * if <code>key</code> was previously unset
   * @see #getEntry
   */
    public Object putEntry(String key, Object value) {
        java.util.StringTokenizer tok = new java.util.StringTokenizer(key, ":");
        return putEntryRec(this, tok, value);
    }

    private Object putEntryRec(Object store, java.util.StringTokenizer key, Object value) {
        if (store instanceof java.util.LinkedList) {
            throw new RuntimeException("unimlemented feature");
        } else if (store instanceof Hashstore) {
            Hashstore hashstore = (Hashstore) store;
            String subkey = key.nextToken();
            if (key.hasMoreTokens()) {
                if (hashstore.get(subkey) == null) {
                    hashstore.put(subkey, new Hashstore());
                }
                return putEntryRec(hashstore.get(subkey), key, value);
            } else {
                if (value == null) return hashstore.remove(subkey); else return hashstore.put(subkey, value);
            }
        } else {
            throw new RuntimeException("Internal error!  Unknown store type: " + store + " (" + store.getClass().getName() + ")");
        }
    }

    /**
   * Get an entry in the hashstore.
   * 
   * @param key         the name of the component to get config-info for
   * @return the value associated with <code>key</code>
   * @see #putEntry for a description of the key format
   */
    public Object getEntry(String key) {
        java.util.StringTokenizer tok = new java.util.StringTokenizer(key, ":");
        return getEntryRec(this, tok);
    }

    private Object getEntryRec(Object store, java.util.StringTokenizer key) {
        if (store == null) {
            return null;
        } else if (store instanceof java.util.LinkedList) {
            throw new RuntimeException("unimlemented feature");
        } else if (store instanceof Hashstore) {
            Hashstore hashstore = (Hashstore) store;
            String subkey = key.nextToken();
            if (key.hasMoreTokens()) {
                return getEntryRec(hashstore.get(subkey), key);
            } else {
                return hashstore.get(subkey);
            }
        } else {
            throw new RuntimeException("Internal error!  Unknown store type: " + store + " (" + store.getClass().getName() + ")");
        }
    }

    private static ti.token.Token SEMICOLON = new ti.token.Token(";");

    private static ti.token.Token LBRACE = new ti.token.Token("{");

    private static ti.token.Token RBRACE = new ti.token.Token("}");

    private static ti.token.Token LBRACKET = new ti.token.Token("[");

    private static ti.token.Token RBRACKET = new ti.token.Token("]");

    private static ti.token.Token COMMENT_START_1 = new ti.token.Token("#");

    private static ti.token.Token COMMENT_START_2 = new ti.token.Token("//");

    private static ti.token.Token EQUALS = new ti.token.Token("=");

    private static ti.token.Token COMMA = new ti.token.Token(",");

    /**
   * Load values into this hashstore from an input stream.
   *
   * @param in          the input stream to load the hashstore from
   */
    public void load(Reader in) {
        FilteredTokenReader tok = new FilteredTokenReader(in);
        tok.addSeperatorToken(SEMICOLON);
        tok.addSeperatorToken(LBRACE);
        tok.addSeperatorToken(RBRACE);
        tok.addSeperatorToken(LBRACKET);
        tok.addSeperatorToken(RBRACKET);
        tok.addSeperatorToken(COMMENT_START_1);
        tok.addSeperatorToken(COMMENT_START_2);
        tok.addSeperatorToken(EQUALS);
        tok.addSeperatorToken(COMMA);
        tok.setQuotingChar('"');
        try {
            parse_HASH_FILE(tok, this);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void parse_HASH_FILE(FilteredTokenReader tok, Hashstore store) throws Exception {
        while (true) {
            try {
                tok.putBackToken(tok.getNextToken());
            } catch (ti.token.TokenReaderEOFException e) {
                return;
            }
            ti.token.Token token = tok.getNextToken();
            Object key = token.getValue();
            token = tok.getNextToken();
            if (!token.equals(EQUALS)) {
                throw new Exception(parseErrorMsg(tok.getLineNumber(), "expected \"=\", got \"" + token.getValue() + "\""));
            }
            Object value = parse_HASH_ENTRY(tok);
            if ((store.put(key, value)) != null) {
                System.err.println("Warning, " + key.toString() + " was specified multiple times.  Discarding previous specification.");
            }
            token = tok.getNextToken();
            if (!token.equals(SEMICOLON)) {
                throw new Exception(parseErrorMsg(tok.getLineNumber(), "expected \"=\", got \"" + token.getValue() + "\""));
            }
        }
    }

    private Object parse_HASH_ENTRY(FilteredTokenReader tok) throws Exception {
        ti.token.Token token;
        token = tok.getNextToken();
        Object value;
        if (token.equals(LBRACE)) {
            tok.putBackToken(token);
            value = parse_HASH_STORE(tok);
        } else if (token.equals(LBRACKET)) {
            LinkedList list = new LinkedList();
            token = tok.getNextToken();
            if (!token.equals(RBRACKET)) {
                tok.putBackToken(token);
                list.add(parse_HASH_ENTRY(tok));
                token = tok.getNextToken();
                while (!(token.equals(RBRACKET))) {
                    if (!(token.equals(COMMA))) {
                        throw new Exception(parseErrorMsg(tok.getLineNumber(), "expected \"=\", got \"" + token.getValue() + "\""));
                    }
                    list.add(parse_HASH_ENTRY(tok));
                    token = tok.getNextToken();
                }
            }
            value = list;
        } else {
            value = token.getValue();
        }
        return value;
    }

    private Hashstore parse_HASH_STORE(FilteredTokenReader tok) throws Exception {
        ti.token.Token token;
        Hashstore store = new Hashstore();
        token = tok.getNextToken();
        if (!token.equals(LBRACE)) {
            throw new Exception(parseErrorMsg(tok.getLineNumber(), "expected \"=\", got \"" + token.getValue() + "\""));
        }
        while (!(token = tok.getNextToken()).equals(RBRACE)) {
            Object key = token.getValue();
            token = tok.getNextToken();
            if (!token.equals(EQUALS)) {
                throw new Exception(parseErrorMsg(tok.getLineNumber(), "expected \"=\", got \"" + token.getValue() + "\""));
            }
            Object value = parse_HASH_ENTRY(tok);
            if ((store.put(key, value)) != null) {
                System.err.println("Warning, " + key.toString() + " was specified multiple times.  Discarding previous specification.");
            }
            token = tok.getNextToken();
            if (!token.equals(SEMICOLON)) {
                throw new Exception(parseErrorMsg(tok.getLineNumber(), "expected \"=\", got \"" + token.getValue() + "\""));
            }
        }
        return store;
    }

    private class FilteredTokenReader extends ti.token.TokenReader {

        FilteredTokenReader(Reader in) {
            super(in);
        }

        ti.token.Token getNextToken() throws ti.token.TokenReaderEOFException {
            while (true) {
                ti.token.Token token = getNextToken(true);
                if (token.equals(COMMENT_START_1)) {
                    ti.token.Token numToken = getNextToken(true);
                    ti.token.Token fileToken = getNextToken(true);
                    try {
                        int num = (Integer.decode(numToken.getValue())).intValue();
                        if (fileToken.isQuoted()) {
                            filename = fileToken.getValue();
                            lineOffset = getLineNumber() - num;
                        }
                    } catch (NumberFormatException e) {
                    }
                    putBackToken(fileToken);
                    putBackToken(numToken);
                }
                if (token.equals(COMMENT_START_1) || token.equals(COMMENT_START_2)) {
                    for (token = getNextToken(false); !token.equals(ti.token.TokenReader.EOL); token = getNextToken(false)) {
                        ;
                    }
                } else {
                    return token;
                }
            }
        }
    }

    /**
   * Generate a parse error mesg.
   */
    private String parseErrorMsg(int line, String msg) {
        return (((filename != null) ? (filename + ":") : ("line ")) + (line - lineOffset) + ": Syntax error: " + msg);
    }

    /**
   * Write values from this hashstore into an output stream.
   *
   * @param out         the output stream to write to
   */
    public void write(Writer out) {
        PrintWriter pw = new PrintWriter(out);
        writeRecursive(pw, 0);
    }

    /**
   * This is the method that does the dirty work for <code>write</code>.  This
   * method can be called recursively.
   *
   * @param pw          the printwriter to write to
   * @param level       the indent level
   */
    private void writeRecursive(PrintWriter pw, int level) {
        needsSave = false;
        char tabc[] = new char[2 * level];
        for (int i = 0; i < tabc.length; i++) {
            tabc[i] = ' ';
        }
        String tab = new String(tabc);
        Enumeration e = keys();
        String keys[] = new String[size()];
        for (int i = 0; i < keys.length; i++) keys[i] = (String) (e.nextElement());
        inPlaceSort(keys);
        for (int i = 0; i < keys.length; i++) {
            Object value = get(keys[i]);
            pw.print(tab + quote(keys[i].toString()) + " = ");
            writeValue(pw, level, tab, value);
            pw.println(";");
            if (level == 0) pw.println("");
        }
        pw.flush();
    }

    private void writeValue(PrintWriter pw, int level, String tab, Object value) {
        if (value instanceof Hashstore) writeHashstore(pw, level, tab, (Hashstore) value); else if (value instanceof LinkedList) writeLinkedList(pw, level, tab, (LinkedList) value); else pw.print(quote(value.toString()));
    }

    private void writeLinkedList(PrintWriter pw, int level, String tab, LinkedList list) {
        Object entries[] = list.toArray();
        pw.print("[ ");
        level += 1;
        tab += "  ";
        if (entries.length >= 1) {
            writeValue(pw, level, tab, entries[0]);
            for (int j = 1; j < entries.length; j++) {
                pw.print(", ");
                writeValue(pw, level, tab, entries[j]);
            }
        }
        pw.print(" ]");
    }

    private void writeHashstore(PrintWriter pw, int level, String tab, Hashstore value) {
        pw.println("");
        pw.println(tab + "{");
        value.writeRecursive(pw, level + 1);
        pw.print(tab + "}");
    }

    private String quote(String str) {
        String result = null;
        int idx;
        while ((idx = str.indexOf('"')) != -1) {
            String substr = str.substring(0, idx) + "\\\"";
            if (result == null) result = substr; else result += substr;
            str = str.substring(idx + 1);
        }
        if (result == null) {
            result = str;
        } else {
            result += str;
        }
        return ("\"" + result + "\"");
    }

    private static void inPlaceSort(String arr[]) {
        inPlaceSort(arr, (new String[arr.length]), 0, arr.length);
    }

    private static void inPlaceSort(String arr[], String buf[], int a, int b) {
        if (b - a <= 1) {
            return;
        }
        int c = (b + a) / 2;
        inPlaceSort(arr, buf, a, c);
        inPlaceSort(arr, buf, c, b);
        int s1 = a;
        int s2 = c;
        int i = a;
        while ((s1 < c) && (s2 < b)) {
            if (arr[s1].compareTo(arr[s2]) <= 0) {
                buf[i++] = arr[s1++];
            } else {
                buf[i++] = arr[s2++];
            }
        }
        while (s1 < c) {
            buf[i++] = arr[s1++];
        }
        while (s2 < b) {
            buf[i++] = arr[s2++];
        }
        for (int j = a; j < b; j++) {
            arr[j] = buf[j];
        }
    }
}
