package net.sourceforge.scrollrack;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.StringTokenizer;

public class CardBase {

    private String filename;

    private CardInfo[] cards;

    private List extras;

    public List expansion_list;

    public List format_list;

    /**
 * Parse the plain text file containing the full Oracle name, cost,
 * and text of every Magic card ever printed.
 */
    public CardBase(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        this.filename = filename;
        TreeSet cardset = new TreeSet();
        while (true) {
            String text = reader.readLine();
            if (text == null) break;
            if (text.equals("")) continue;
            if (text.startsWith("<")) {
                process_section(reader, text);
                continue;
            }
            CardInfo cardinfo = new CardInfo();
            cardinfo.name = text;
            text = reader.readLine();
            if ((text == null) || text.equals("")) {
                continue;
            }
            if (is_type_of(text, "Land")) {
                cardinfo.cost = null;
                cardinfo.color = CardInfo.LAND_BIT;
            } else {
                cardinfo.cost = text;
                cardinfo.taps = cost_to_taps(text);
                cardinfo.color = cost_to_color_bits(text);
                text = reader.readLine();
            }
            cardinfo.cardtype = text;
            cardinfo.pow_tgh = null;
            if ((text != null) && is_type_of(text, "Creature")) {
                cardinfo.pow_tgh = reader.readLine();
            }
            String full = null;
            while ((text = reader.readLine()) != null) {
                if (text.equals("")) break;
                if (text.startsWith("[") && (text.length() > 3)) break;
                if (full == null) {
                    full = text;
                } else {
                    full = full + "\n" + text;
                }
            }
            cardinfo.text = full;
            if (cardinfo.taps == 0) {
                int text_color = get_text_color(cardinfo);
                if (text_color > 0) cardinfo.color = text_color;
            }
            process_prints(text, cardinfo);
            cardset.add(cardinfo);
        }
        reader.close();
        int size = cardset.size();
        cards = new CardInfo[size + 1];
        int i = 1;
        Iterator iterator = cardset.iterator();
        while (iterator.hasNext()) cards[i++] = (CardInfo) iterator.next();
        extras = new ArrayList();
    }

    /**
 * Where text is "TYPE - SUBTYPE", is the given word found int the type?
 */
    public static boolean is_type_of(String text, String word) {
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("-")) break;
            if (token.equalsIgnoreCase(word)) return (true);
        }
        return (false);
    }

    /**
 * Given number-item,item, return the number of items it represents.
 */
    private int cost_to_taps(String text) {
        int first, length, digits, taps, i;
        char c;
        length = text.length();
        taps = 0;
        for (first = 0; first < length; first++) {
            if (text.charAt(first) != 'X') break;
        }
        for (digits = first; digits < length; digits++) {
            if (!Character.isDigit(text.charAt(digits))) break;
        }
        if (digits > first) taps = Integer.parseInt(text.substring(first, digits));
        for (i = digits; i < length; i++) {
            c = text.charAt(i);
            if (c == '(') {
                i = text.indexOf(')', i);
                if (i < 0) break;
            }
            if ((c == '(') || (get_color_bit(c) > 0)) taps++;
        }
        return taps;
    }

    /**
 * Given abc, return an integer with the bits for a, b, and c turned on.
 */
    private int cost_to_color_bits(String text) {
        int bits, i, bit;
        bits = 0;
        for (i = 0; i < text.length(); i++) {
            bit = get_color_bit(text.charAt(i));
            if (bit > 0) {
                bits |= bit;
                if (bits != bit) bits |= CardInfo.GOLD_BIT;
            }
        }
        if ((i > 0) && (bits == 0)) bits = CardInfo.ARTIFACT_BIT;
        return (bits);
    }

    public static int get_color_bit(char c) {
        switch(c) {
            case 'W':
                return CardInfo.WHITE_BIT;
            case 'U':
                return CardInfo.BLUE_BIT;
            case 'B':
                return CardInfo.BLACK_BIT;
            case 'R':
                return CardInfo.RED_BIT;
            case 'G':
                return CardInfo.GREEN_BIT;
        }
        return 0;
    }

    private int get_text_color(CardInfo info) {
        if ((info.name == null) || (info.text == null)) return 0;
        String prefix = info.name + " is ";
        if (!info.text.startsWith(prefix)) return 0;
        String color = info.text.substring(prefix.length());
        int idx = color.indexOf('.');
        if (idx < 0) return 0;
        color = color.substring(0, idx);
        if (color.equals("white")) return CardInfo.WHITE_BIT;
        if (color.equals("blue")) return CardInfo.BLUE_BIT;
        if (color.equals("black")) return CardInfo.BLACK_BIT;
        if (color.equals("red")) return CardInfo.RED_BIT;
        if (color.equals("green")) return CardInfo.GREEN_BIT;
        return 0;
    }

    private void process_prints(String text, CardInfo info) {
        if ((text == null) || (!text.startsWith("[")) || (!text.endsWith("]"))) return;
        text = text.substring(1, text.length() - 1);
        StringTokenizer tokenizer = new StringTokenizer(text, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            StringTokenizer t2 = new StringTokenizer(token);
            if (t2.countTokens() == 2) {
                String exp = t2.nextToken();
                try {
                    int number = Integer.parseInt(t2.nextToken());
                    info.add_print(exp, number);
                } catch (NumberFormatException nfe) {
                }
            }
        }
    }

    /**
 * Look up global ids for the cards in a deck.
 * If the card name is not found in the database, then add it to the
 * database (with no attributes), which beats having to remove it from
 * the deck.
 */
    public int find_globalid(String name) {
        int lo, hi, mid, val;
        CardInfo info;
        lo = 0;
        hi = cards.length - 1;
        while (lo <= hi) {
            mid = (lo + hi) / 2;
            val = cards[mid].name.compareToIgnoreCase(name);
            if (val == 0) return (mid);
            if (val < 0) lo = mid + 1; else hi = mid - 1;
        }
        for (mid = 0; mid < extras.size(); mid++) {
            info = (CardInfo) extras.get(mid);
            if (info.name.equalsIgnoreCase(name)) return (cards.length + mid);
        }
        return (add_extra(name));
    }

    /**
 * Like cardbase_find_globalid(), but look for a description of the
 * card name, rather than the card name itself.
 */
    public int find_prefix(String prefix, int length) {
        int plen, id;
        String name;
        CardInfo info;
        plen = prefix.length();
        for (id = 0; id < cards.length; id++) {
            name = cards[id].name;
            if ((name.length() == length) && name.substring(0, plen).equalsIgnoreCase(prefix)) return (id);
        }
        for (id = 0; id < extras.size(); id++) {
            info = (CardInfo) extras.get(id);
            if ((info.name.length() == length) && info.name.substring(0, plen).equalsIgnoreCase(prefix)) return (cards.length + id);
        }
        while (plen < length) {
            prefix += '_';
            plen++;
        }
        return (add_extra(prefix));
    }

    /**
 * Quick O(1) lookup a card by globalid.
 */
    public CardInfo get(int globalid) {
        return (globalid < cards.length ? cards[globalid] : (CardInfo) extras.get(globalid - cards.length));
    }

    /**
 * Associate a globalid with a card name.
 */
    private int add_extra(String name) {
        CardInfo info;
        int globalid;
        globalid = cards.length + extras.size();
        info = new CardInfo();
        info.name = name;
        extras.add(info);
        return (globalid);
    }

    /**
 * Add a token creature.
 */
    public int add_card(String name, String pow_tgh, int color) {
        int globalid;
        CardInfo info;
        globalid = find_globalid(name);
        info = get(globalid);
        if ((info.pow_tgh != null) || (info.color != 0)) {
            if ((!same_string(info.pow_tgh, pow_tgh)) || (info.color != color)) return (-1);
            return (globalid);
        }
        info.pow_tgh = pow_tgh;
        info.color = color;
        return (globalid);
    }

    public static boolean same_string(String s1, String s2) {
        if (s1 == null) return ((s2 == null) || s2.equals(""));
        if (s2 == null) return (s1.equals(""));
        return (s1.equalsIgnoreCase(s2));
    }

    /**
 * Return the number of cards read from the database file and added.
 */
    public int size() {
        return (cards.length + extras.size());
    }

    /**
 * Return the name of the cardbase file.
 */
    public String get_filename() {
        return filename;
    }

    /**
 * Process the <expansions> and <formats> sections.
 */
    private void process_section(BufferedReader reader, String text) throws IOException {
        if (text.equalsIgnoreCase("<expansions>")) {
            process_expansions(reader);
            return;
        }
        if (text.equalsIgnoreCase("<formats>")) {
            process_formats(reader);
            return;
        }
    }

    private void process_expansions(BufferedReader reader) throws IOException {
        this.expansion_list = new ArrayList();
        while (true) {
            String text = reader.readLine();
            if ((text == null) || text.equals("")) return;
            int idx = text.indexOf('=');
            if (idx <= 0) continue;
            String[] expansion = new String[2];
            expansion[0] = text.substring(0, idx);
            expansion[1] = text.substring(idx + 1);
            expansion_list.add(expansion);
        }
    }

    private void process_formats(BufferedReader reader) throws IOException {
        this.format_list = new ArrayList();
        while (true) {
            String text = reader.readLine();
            if ((text == null) || text.equals("")) return;
            int idx = text.indexOf(':');
            if (idx <= 0) continue;
            Object[] format = new Object[2];
            format[0] = text.substring(0, idx);
            String value = text.substring(idx + 1);
            StringTokenizer tokenizer = new StringTokenizer(value);
            String[] exparr = new String[tokenizer.countTokens()];
            for (int iii = 0; iii < exparr.length; iii++) exparr[iii] = tokenizer.nextToken();
            format[1] = exparr;
            format_list.add(format);
        }
    }
}
