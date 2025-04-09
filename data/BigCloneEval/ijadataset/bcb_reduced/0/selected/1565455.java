package symbols;

import java.util.*;
import java.io.*;
import xmlutils.*;
import midi.*;

public class JHumSymbolList extends ArrayList<JHumSymbol> implements Serializable {

    public int getMaxX() {
        if (size() == 0) return 0;
        return get(size() - 1).x;
    }

    public boolean add(JHumSymbol s) {
        return add(s, -1);
    }

    public boolean add(JHumSymbol s, int diff) {
        if (getMaxX() <= s.x) return addOrSet(size(), s, diff);
        int inf = 0;
        int sup = size() - 1;
        while (Math.abs(inf - sup) > 1) {
            int center = (inf + sup) / 2;
            if (s.x >= get(center).x) inf = center; else sup = center;
        }
        if (get(inf).x >= s.x) addOrSet(inf, s, diff); else addOrSet(inf + 1, s, diff);
        return true;
    }

    private boolean addOrSet(int pos, JHumSymbol ns, int diff) {
        while ((pos > 0) && Math.abs(get(pos - 1).x - ns.x) < diff) {
            remove(pos - 1);
            pos--;
        }
        while ((pos < size()) && Math.abs(get(pos).x - ns.x) < diff) {
            remove(pos);
        }
        add(pos, ns);
        return true;
    }

    public void sel_del(int from, int to) {
        Iterator i = iterator();
        int delta = to - from + 1;
        while (i.hasNext()) {
            JHumSymbol s = (JHumSymbol) i.next();
            if (s.x >= from) {
                if (s.x <= to) i.remove(); else s.x = s.x - delta;
            }
        }
    }

    public void sel_space(int from, int space) {
        Iterator i = iterator();
        while (i.hasNext()) {
            JHumSymbol s = (JHumSymbol) i.next();
            if (s.x >= from) s.x = s.x + space;
        }
    }

    public JHumSymbolList sel_copy(int from, int to) {
        JHumSymbolList buf = new JHumSymbolList();
        Iterator i = iterator();
        int delta = to - from + 1;
        while (i.hasNext()) {
            JHumSymbol s = (JHumSymbol) i.next();
            if ((s.x >= from) && (s.x <= to)) {
                JHumSymbol sclone = s.clone();
                sclone.x -= from;
                buf.add(sclone);
            }
        }
        return buf;
    }

    public void sel_paste(int pos, JHumSymbolList buf) {
        Iterator i = buf.iterator();
        while (i.hasNext()) {
            JHumSymbol s = ((JHumSymbol) i.next()).clone();
            s.x += pos;
            add(s);
        }
    }

    public void fill(JHumMidiPlayer mp, int from, int to) {
        Iterator<JHumSymbol> it = iterator();
        while (it.hasNext()) it.next().preparePlay();
        int i = 0;
        boolean first = true;
        while (i < size()) {
            if ((get(i).x >= from) && (get(i).x <= to)) {
                if (first) {
                    mp.setBeginRepeat(i);
                    first = false;
                }
                i = get(i).addMeTo(i, mp);
            } else i++;
        }
    }

    public void writeSymbols(XMLWriter w) {
        Iterator<JHumSymbol> it = iterator();
        while (it.hasNext()) {
            JHumSymbol s = it.next();
            String name = s.getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            Object[][] props = { { "type", name }, { "x", s.x } };
            w.open("symbol", props);
            s.writeSymbol(w);
            w.close();
        }
    }
}
