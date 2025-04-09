package org.neblipedia.wiki.mediawiki.plantillas;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncionesPHP {

    public static Boolean array_pop(LinkedList<Boolean> s) {
        return s.removeFirst();
    }

    public static String array_pop(LinkedList<String> s) {
        return s.removeFirst();
    }

    @Deprecated
    public static void array_push(LinkedList<Boolean> l, Boolean o) {
        l.addFirst(o);
    }

    @Deprecated
    public static void array_push(LinkedList<String> l, String o) {
        l.addFirst(o);
    }

    public static String array_shift(List<String> str) {
        return str.remove(0);
    }

    public static int count(String[] str) {
        return str.length;
    }

    public static boolean empty(Object o) {
        return o != null;
    }

    @Deprecated
    public static String[] explode(String regex, String cadena) {
        return cadena.split(regex);
    }

    @Deprecated
    public static String[] explode(String regex, String cadena, int limit) {
        return cadena.split(regex, limit);
    }

    @Deprecated
    public static boolean preg_match(Pattern regex, String cadena) {
        Matcher m = regex.matcher(cadena);
        return m.find();
    }

    @Deprecated
    public static boolean preg_match(String regex, String cadena) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(cadena);
        return m.find();
    }

    @Deprecated
    public static boolean preg_match(String regex, String cadena, Integer[][] matches) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(cadena);
        boolean b = m.matches();
        LinkedList<Integer[]> o = new LinkedList<Integer[]>();
        while (m.find()) {
            Integer i = m.start(0);
            Integer f = m.end(0);
            o.add(new Integer[] { i, f });
        }
        matches = new Integer[o.size()][];
        for (int i = 0; i < o.size(); i++) {
            matches[i] = o.get(i);
        }
        return b;
    }

    @Deprecated
    public static boolean preg_match(String regex, String cadena, String[] matches) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(cadena);
        boolean b = m.matches();
        LinkedList<String> o = new LinkedList<String>();
        while (m.find()) {
            o.add(m.group(0));
        }
        matches = new String[o.size()];
        for (int i = 0; i < o.size(); i++) {
            matches[i] = o.get(i);
        }
        return b;
    }

    @Deprecated
    public static String preg_replace(String regex, String reemplazo, String cadena) {
        return cadena.replaceAll(regex, reemplazo);
    }

    public static String str_repeat(String s, int i) {
        StringBuilder tmp = new StringBuilder(s.length() * i);
        for (int j = 0; j < i; j++) {
            tmp.append(s);
        }
        return tmp.toString();
    }

    @Deprecated
    public static String str_replace(String regex, String reemplazo, String cadena) {
        return cadena.replace(regex, reemplazo);
    }

    public static int strcmp(String cad1, String cad2) {
        return cad1.compareTo(cad2);
    }

    public static int strlen(String str) {
        return str.length();
    }

    public static int strpos(String cadena, String buscar) {
        return FuncionesPHP.strpos(cadena, buscar, 0);
    }

    public static int strpos(String cadena, String buscar, int i) {
        return cadena.indexOf(buscar, i);
    }

    public static int strspn(String txt, Pattern regex) {
        Matcher m = regex.matcher(txt);
        int i = 0;
        if (m.find()) {
            i = m.group(0).length();
        }
        return i;
    }

    public static String substr(String str, int comienzo) {
        if (comienzo < 0) {
            comienzo = str.length() + comienzo;
        }
        if (comienzo < 0) {
            comienzo = 0;
        }
        return str.substring(comienzo);
    }

    public static String substr(String str, int comienzo, int longitud) {
        try {
            if (str.length() == 0 || longitud == 0) {
                return " ";
            }
            if (comienzo < 0) {
                comienzo = str.length() + comienzo;
            }
            if (longitud < 0) {
                longitud = str.length() + longitud;
            } else if (longitud > 0) {
                longitud = comienzo + longitud;
            }
            if (longitud >= str.length()) {
                longitud = str.length() - 1;
            }
            return str.substring(comienzo, longitud);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("----------- funcionesphp.substr indexoutofbounds: " + str + " " + comienzo + " " + longitud);
            return str;
        }
    }

    @Deprecated
    public static String trim(String str) {
        return str.trim();
    }
}
