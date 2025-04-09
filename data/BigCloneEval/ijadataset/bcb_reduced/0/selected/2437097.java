package net.sourceforge.gatherer;

import java.io.*;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import net.sourceforge.scrollrack.CardInfo;

public class OracleSpoiler {

    private CardInfo[] cards;

    /**
 * Parse the html file containing and <xmp> section with the full
 * Oracle name, cost, and text of every Magic card ever printed.
 */
    public OracleSpoiler(String filename) throws IOException {
        InputStream istream = new FileInputStream(filename);
        Reader windowsReader = new InputStreamReader(istream, "WINDOWS-1252");
        BufferedReader reader = new BufferedReader(windowsReader);
        TreeSet cardset = new TreeSet();
        boolean is_html = false;
        boolean empty = true;
        while (true) {
            String text = read_c_string(reader);
            if (text == null) break;
            if (text.equals("")) continue;
            if (empty && text.startsWith("<")) {
                is_html = true;
                text = skip_html(reader, text);
                if ((text == null) || text.equals("")) continue;
            }
            if (is_html && end_of_preformat(text)) {
                break;
            }
            CardInfo cardinfo = new CardInfo();
            cardinfo.name = text;
            text = read_c_string(reader);
            if ((text == null) || text.equals("")) {
                continue;
            }
            if (is_type_of(text, "Land")) {
                cardinfo.cost = null;
                cardinfo.color = CardInfo.LAND_BIT;
            } else if (is_type_of_card(text)) {
                cardinfo.cost = "-";
            } else {
                cardinfo.cost = remove_spaces(text);
                cardinfo.color = cost_to_color_bits(text);
                text = read_c_string(reader);
            }
            cardinfo.cardtype = text;
            cardinfo.pow_tgh = null;
            if ((text != null) && is_type_of(text, "Creature")) {
                cardinfo.pow_tgh = read_c_string(reader);
            }
            String full = null;
            while ((text = read_c_string(reader)) != null) {
                if (text.equals("") || text.startsWith("Expansion:")) break;
                if (full == null) {
                    full = text;
                } else {
                    full = full + "\n" + text;
                }
            }
            cardinfo.text = full;
            cardset.add(cardinfo);
            empty = false;
        }
        reader.close();
        int size = cardset.size();
        cards = new CardInfo[size];
        int i = 0;
        Iterator iterator = cardset.iterator();
        while (iterator.hasNext()) cards[i++] = (CardInfo) iterator.next();
    }

    /**
 * Read a line of text and trim it.
 */
    private String read_c_string(BufferedReader reader) throws IOException {
        String text = reader.readLine();
        if (text != null) text = text.trim();
        return text;
    }

    /**
 * Read until <pre>.
 */
    private String skip_html(BufferedReader reader, String text) throws IOException {
        final String pre = "<pre>";
        while (true) {
            int idx = text.indexOf(pre);
            if (idx < 0) idx = text.indexOf("<xmp>");
            if (idx >= 0) return text.substring(idx + pre.length()).trim();
            text = read_c_string(reader);
            if (text == null) return (null);
        }
    }

    /**
 * Read until </pre>.
 */
    private boolean end_of_preformat(String text) {
        return (text.startsWith("</pre>") || text.startsWith("</xmp>"));
    }

    /**
 * Where text is "TYPE - SUBTYPE", is the given word found int the type?
 */
    private boolean is_type_of(String text, String word) {
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("-")) break;
            if (token.equalsIgnoreCase(word)) return (true);
        }
        return (false);
    }

    /**
 * Remove all spaces from the given String.
 */
    private String remove_spaces(String text) {
        int idx;
        while ((idx = text.indexOf(' ')) >= 0) text = text.substring(0, idx) + text.substring(idx + 1);
        return text;
    }

    /**
 * Given abc, return an integer with the bits for a, b, and c turned on.
 */
    private int cost_to_color_bits(String text) {
        int bits, i, bit;
        char c;
        bits = 0;
        for (i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            bit = 0;
            switch(c) {
                case 'W':
                    bit = CardInfo.WHITE_BIT;
                    break;
                case 'U':
                    bit = CardInfo.BLUE_BIT;
                    break;
                case 'B':
                    bit = CardInfo.BLACK_BIT;
                    break;
                case 'R':
                    bit = CardInfo.RED_BIT;
                    break;
                case 'G':
                    bit = CardInfo.GREEN_BIT;
                    break;
            }
            bits |= bit;
            if (bits != bit) bits |= CardInfo.GOLD_BIT;
        }
        if ((i > 0) && (bits == 0)) bits = CardInfo.ARTIFACT_BIT;
        return (bits);
    }

    /**
 * Search for cards with no mana cost (Splice-only, Suspend-only, etc.)
 */
    private boolean is_type_of_card(String text) {
        return (is_type_of(text, "Creature") || is_type_of(text, "Artifact") || is_type_of(text, "Instant") || is_type_of(text, "Sorcery"));
    }

    /**
 * Look up global ids for the cards in a deck.
 * If the card name is not found in the database, then add it to the
 * database (with no attributes), which beats having to remove it from
 * the deck.
 */
    public int find_globalid(String name) {
        int lo, hi, mid, val;
        lo = 0;
        hi = cards.length - 1;
        while (lo <= hi) {
            mid = (lo + hi) / 2;
            val = cards[mid].name.compareToIgnoreCase(name);
            if (val == 0) return (mid);
            if (val < 0) lo = mid + 1; else hi = mid - 1;
        }
        return (-1);
    }

    /**
 * Quick O(1) lookup a card by globalid.
 */
    public CardInfo get(int globalid) {
        return (cards[globalid]);
    }

    /**
 * Return the number of cards read from the database file and added.
 */
    public int size() {
        return (cards.length);
    }

    /**
 * Note that a given card was in a given expansion.
 */
    public void add_print(String name, String exp, int number) {
        int globalid;
        CardInfo info;
        globalid = find_globalid(name);
        if (globalid < 0) {
            System.out.println("not found: " + name);
            return;
        }
        info = cards[globalid];
        info.add_print(exp, number);
    }
}
