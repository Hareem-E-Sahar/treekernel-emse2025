package net.sf.intltyper.lib;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;

public class RFC1345List {

    private Map mnemos = null;

    public RFC1345List(URL url) {
        if (url == null) return;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(url.openStream())));
            final String linePattern = " XX???????      HHHH    X";
            String line;
            mnemos = new HashMap();
            nextline: while ((line = br.readLine()) != null) {
                if (line.length() < 9) continue nextline;
                if (line.charAt(7) == ' ' || line.charAt(8) != ' ') {
                    line = line.substring(0, 8) + "        " + line.substring(8);
                }
                if (line.length() < linePattern.length()) continue nextline;
                for (int i = 0; i < linePattern.length(); i++) {
                    char c = line.charAt(i);
                    switch(linePattern.charAt(i)) {
                        case ' ':
                            if (c != ' ') continue nextline;
                            break;
                        case 'X':
                            if (c == ' ') continue nextline;
                            break;
                        case '?':
                            break;
                        case 'H':
                            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) ; else continue nextline;
                            break;
                        default:
                            throw new RuntimeException("Pattern broken!");
                    }
                }
                char c = (char) Integer.parseInt(line.substring(16, 20), 16);
                String mnemo = line.substring(1, 16).trim();
                if (mnemo.length() < 2) throw new RuntimeException();
                mnemos.put(mnemo, new Character(c));
            }
            br.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public char getCharacter(String mnemonic) {
        if (mnemos == null) return '?';
        Character c = (Character) mnemos.get(mnemonic);
        if (c == null) return 0;
        return c.charValue();
    }

    public List getMnemonics(char c) {
        if (mnemos == null) return Collections.singletonList("RFC1345 not found");
        List result = new ArrayList(1);
        for (Iterator iter = mnemos.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Character value = (Character) entry.getValue();
            if (value.charValue() == c) result.add(key);
        }
        return result;
    }
}
