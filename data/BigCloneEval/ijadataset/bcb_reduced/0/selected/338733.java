package de.mnit.basis.daten;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.mnit.basis.daten.struktur.S_Folge;
import de.mnit.basis.daten.struktur.liste.Liste;
import de.mnit.basis.daten.struktur.liste.S_Liste;
import de.mnit.basis.daten.struktur.tabelle.typ.TypTabelle2;
import de.mnit.basis.daten.struktur.tabelle.typ.TypTabelle3;

/**
 * @author Michael Nitsche
 * 07.12.2010	Erstellt
 */
public class SucheText {

    /**
	 * Start, Ende, Textteil
	 */
    public static TypTabelle3<Integer, Integer, String> suche(String text, String regex) {
        TypTabelle3<Integer, Integer, String> erg = TypTabelle3.neu(Integer.class, Integer.class, String.class);
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(text);
        while (matcher.find()) erg.plus(matcher.start(), matcher.end(), matcher.group());
        return erg;
    }

    public static S_Folge<Integer> positionen(String suche, String text) {
        S_Liste<Integer> erg = Liste.neu();
        int pos = -1;
        while ((pos = text.indexOf(suche, pos + 1)) > -1) erg.plus(pos + 1);
        return erg;
    }

    /**
	 * Sucht z.B. nach HTML-Tags! Achtung, die Such-Strings dürfen sich nicht überschneiden!
	 */
    public static TypTabelle2<Integer, String> positionenMulti(String text, String... suche) {
        TypTabelle2<Integer, String> erg = TypTabelle2.neu(Integer.class, String.class);
        for (String s : suche) {
            S_Folge<Integer> pos = positionen(s, text);
            for (Integer i : pos) erg.plus(i, s);
        }
        erg.sortieren(1, 2);
        return erg;
    }
}
