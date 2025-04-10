package loro.util;

import loro.Loro.Str;
import loro.arbol.*;
import loro.Rango;
import loro.IOroLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/**
 * Algunas utilerias generales
 *
 */
public final class Util {

    /**
	 */
    public static void _assert(boolean c, String m) {
        if (!c) {
            throw new RuntimeException("Condici�n fallida: " + m);
        }
    }

    /**
	 * Es var una variable sem'antica?
	 */
    public static boolean esVarSemantica(String var) {
        return var.endsWith("'");
    }

    /**
	 * Obtiene la ruta completa correspondiente a la ruta
	 * dada. No pone separador de directorio al final.
	 */
    public static String obtStringRuta(String[] ruta) {
        return obtStringRuta(ruta, "::");
    }

    /**
	 * Procesa las posibles secuencias de escape
	 * y arma una cadena.
	 *
	 * @param s La cadena con secuencia de escape.
	 * @return La cadena procesada.
	 */
    public static String procesarCadena(String s) {
        StringBuffer sb = new StringBuffer();
        boolean enEscape = false;
        int enOctal = 0;
        int enUnicode = 0;
        int res = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (enOctal > 0) {
                if ('0' <= c && c <= '7') {
                    res = res * 8 + c - '0';
                    enOctal--;
                    if (enOctal == 0) sb.append((char) res);
                    continue;
                } else {
                    sb.append((char) res);
                    enOctal = 0;
                }
            } else if (enUnicode > 0) {
                if ('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F') {
                    if ('0' <= c && c <= '9') res = res * 16 + c - '0'; else if ('a' <= c && c <= 'f') res = res * 16 + c - 'a' + 10; else res = res * 16 + c - 'A' + 10;
                    enUnicode--;
                    if (enUnicode == 0) sb.append((char) res);
                    continue;
                } else {
                    sb.append((char) res);
                    enUnicode = 0;
                }
            } else if (enEscape) {
                enEscape = false;
                switch(c) {
                    case 'n':
                        res = '\n';
                        break;
                    case 't':
                        res = '\t';
                        break;
                    case 'b':
                        res = '\b';
                        break;
                    case 'r':
                        res = '\r';
                        break;
                    case 'f':
                        res = '\f';
                        break;
                    case '\\':
                        res = '\\';
                        break;
                    case '\'':
                        res = '\'';
                        break;
                    case '"':
                        res = '"';
                        break;
                    case 'u':
                        enUnicode = 4;
                        res = 0;
                        continue;
                    default:
                        if ('0' <= c && c <= '7') {
                            res = c - '0';
                            if (c <= '3') enOctal = 2; else enOctal = 1;
                            continue;
                        }
                        break;
                }
                sb.append((char) res);
                continue;
            }
            if (c == '\\') enEscape = true; else sb.append(c);
        }
        if (enOctal > 0 || enUnicode > 0) {
            sb.append((char) res);
        }
        return sb.toString();
    }

    /**
	 * Procesa la posible secuencia de escape
	 * y arma un caracter.
	 *
	 * @param s La cadena con secuencia de escape.
	 * @return El caracter correspondiente.
	 */
    public static char procesarCaracter(String s) {
        String cad = procesarCadena(s);
        return cad.charAt(0);
    }

    /**
	 * Obtiene un string correspondiente a una cadena Loro, o sea,
	 * con las comillas y secuencias de escape.
	 * <p>
	 * Por ejemplo, un cambio de linea se convierte en los caracteres ``\n''.
	 * <p>
	 * En general se convierten: <code> \n   \t   \r   \f   \b </code>
	 * <p>
	 * A continuaci�n, Si q es ', se hace la conversi�n ' por \'
	 * Si q es ", se hace la conversi�n " por \"
	 * <p>
	 * finalmente se pone q a ambos extremos.
	 *
	 * @param q  Normalmente uno de  ' o ". 
	 * @param s  La cadena a procesar
	 * @return La cadena procesada.
	 */
    public static String quote(char q, String s) {
        String[] ss = { "\n", "\\n", "\t", "\\t", "\r", "\\r", "\f", "\\f", "\b", "\\b" };
        for (int i = 0; i < ss.length; i += 2) {
            s = replace(s, ss[i], ss[i + 1]);
        }
        if (q == '\'') {
            s = replace(s, "'", "\\'");
        } else if (q == '"') {
            s = replace(s, "\"", "\\\"");
        }
        return q + s + q;
    }

    /**
	 * Formatea una cadena completando con cierto caracter a la derecha
	 * tal que la longitud total sea una dada.
	 * El resultado se agrega al StringBuffer dado.
	 */
    public static void format(Object o, int width, char fill, StringBuffer sb) {
        String s = o == null ? Str.get("null") : o.toString();
        sb.append(s);
        int len = s.length();
        while (len < width) {
            sb.append(fill);
            len++;
        }
    }

    private Util() {
    }

    /**
	 * Concatena un token a una secuencia de tokens.
	 */
    public static String[] concatenarNombre(String[] ids, String id) {
        String[] nruta = new String[ids.length + 1];
        System.arraycopy(ids, 0, nruta, 0, ids.length);
        nruta[nruta.length - 1] = id;
        return nruta;
    }

    /**
	 * Es var una variable sem'antica?
	 */
    public static boolean esVarSemantica(TId id) {
        return id != null && esVarSemantica(id.obtId());
    }

    /**
	 * Obtiene la direccion relativa (estilo recurso) dest de acuerdo con base.
	 * Estrategia simple: siempre "retrocede" con "../" la misma cantidad de
	 * separadores de base; y al final va al destino, sin
	 * importar si se repite algo del recorrido.
	 *
	 * <p>
	 * Ejs (El :: significa separacion entre elementos de arreglo):
	 *
	 * <pre>
	 *  base         dest       retorno
	 *  a            x::y::z    x/y/z
	 *  a::b         x::y::z    ../x/y/z
	 *  a::b         a::b::e    ../a/b/e
	 *  a::b::c      a::f::g    ../../a/f/g
	 *  a::b::c::d   a::b       ../../../a/b
	 *  a            b          b
	 * </pre>
	 * Por conveniencia, baseids puede ser null, en cuyo caso simplemente
	 * se fabrica el resultado con separadores '/'.
	 */
    public static String getRelativeLocation(String[] baseids, String[] destids) {
        StringBuffer sb = new StringBuffer();
        if (baseids != null) {
            for (int k = 0; k < baseids.length - 1; k++) sb.append("../");
        }
        for (int k = 0; k < destids.length; k++) {
            sb.append(destids[k]);
            if (k < destids.length - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    /**
	 * Obtiene la direccion relativa de dest de acuerdo con base.
	 * Ver documentacion de getRelativeLocation(String[] baseids, String[] destids).
	 */
    public static String getRelativeLocation(String[] baseids, String sdest) {
        StringBuffer sb = new StringBuffer();
        if (baseids != null) {
            for (int k = 0; k < baseids.length - 1; k++) sb.append("../");
        }
        StringTokenizer st = new StringTokenizer(sdest, ":");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            if (st.hasMoreTokens()) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    /**
	 * Dice si los dos nombre son iguales.
	 */
    public static boolean nombresIguales(String[] n1, String[] n2) {
        return obtStringRuta(n1).equals(obtStringRuta(n1));
    }

    /**
	 * Obtiene la ruta completa correspondiente a la ruta
	 * dada. No pone separador de directorio al final.
	 */
    public static String obtStringRuta(String[] ids, String sep) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) sb.append(sep);
            sb.append(ids[i]);
        }
        return sb.toString();
    }

    /**
	 * Si e corresponde a variable sem�ntica, retorna
	 * su termino; si no, retorna null.
	 */
    public static TId obtVarSemantica(NExpresion e) {
        if (!(e instanceof NId)) {
            return null;
        }
        TId id = ((NId) e).obtId();
        if (id.obtId().endsWith("'")) {
            return id;
        }
        return null;
    }

    /**
	 * Replace in 's' all occurrences of 'from' to 'to'.
	 */
    public static String replace(String s, String from, String to) {
        StringBuffer sb = new StringBuffer();
        int len = from.length();
        int i, p = 0;
        while ((i = s.indexOf(from, p)) >= 0) {
            sb.append(s.substring(p, i) + to);
            p = i + len;
        }
        sb.append(s.substring(p));
        return sb.toString();
    }

    /**
	 * Gets the list of names of all files in a directory.
	 *
	 * @param directory  The directory
	 * @param recurse    Recurse into subdirectories?
	 *
	 * @return The list of filenames (String's).
	 */
    public static List getFilenames(String directory, boolean recurse) {
        return getFilenames(directory, recurse, null);
    }

    /**
	 * Gets the list of names of all files in a directory accordinf to a
	 * filename filter.
	 *
	 * @param directory  The directory
	 * @param recurse    Recurse into subdirectories?
	 * @param fnfilter   The filename filter. Can be null.
	 *
	 * @return The list of filenames (String's).
	 */
    public static List getFilenames(String directory, boolean recurse, FilenameFilter fnfilter) {
        List list = new ArrayList();
        File file = new File(directory);
        int level = recurse ? Integer.MAX_VALUE : 1;
        addFilenames(list, file, "", level, fnfilter);
        return list;
    }

    /**
	 * Adds filenames to a list.
	 *
	 * @param list The list.
	 * @param file The file to start with.
	 * @param level The level of recursion. Must be >= 1
	 * @param fnfilter   The filename filter. Can be null.
	 */
    private static void addFilenames(List list, File file, String basename, int level, FilenameFilter fnfilter) {
        if (file.isDirectory()) {
            if (level > 0) {
                File[] dir = file.listFiles();
                for (int i = 0; i < dir.length; i++) {
                    if (dir[i].isDirectory()) {
                        addFilenames(list, dir[i], basename + dir[i].getName() + "/", level - 1, fnfilter);
                    } else {
                        if (fnfilter == null || fnfilter.accept(file, dir[i].getName())) {
                            list.add(basename + dir[i].getName());
                        }
                    }
                }
            }
        } else {
            String name2 = basename + file.getName();
            File file2 = new File(name2);
            if (fnfilter == null || fnfilter.accept(file2.getParentFile(), file2.getName())) {
                list.add(name2);
            }
        }
    }

    /**
	 * Dice si dos valores son iguales, teniendo en cuenta si
	 * son null.
	 */
    public static boolean valoresIguales(Object e_val, Object f_val) {
        if (e_val == null ^ f_val == null) return false;
        if (e_val == null) return true;
        return e_val.equals(f_val);
    }

    public static void copyDirectoryToZip(File basedir, ZipOutputStream zos, FilenameFilter fnfilter) throws Exception {
        List list = getFilenames(basedir.getAbsolutePath(), true, fnfilter);
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            String filename = (String) it.next();
            copyFileToZip(filename, basedir.getAbsolutePath() + "/" + filename, zos);
        }
    }

    public static void copyExtensionToZip(IOroLoader oroLoader, ZipOutputStream zos, FilenameFilter fnfilter) throws Exception {
        List list = oroLoader.getFilenames(fnfilter);
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            String name = (String) it.next();
            InputStream is = oroLoader.getResourceAsStream(name);
            copyStreamToZip(name, is, zos);
        }
    }

    public static void copyExtensionToDirectory(IOroLoader oroLoader, File dir, FilenameFilter fnfilter) throws Exception {
        List list = oroLoader.getFilenames(fnfilter);
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            String name = (String) it.next();
            InputStream is = oroLoader.getResourceAsStream(name);
            File dest = new File(dir, name);
            File parent = dest.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            OutputStream os = new FileOutputStream(dest);
            copyStreamToStream(is, os);
            os.close();
        }
    }

    public static void copyFileToZip(String entryName, String filename, ZipOutputStream zos) throws Exception {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(filename);
        copyStreamToStream(new FileInputStream(filename), zos);
        fis.close();
        zos.closeEntry();
    }

    public static void copyStreamToZip(String entryName, InputStream is, ZipOutputStream zos) throws Exception {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        copyStreamToStream(is, zos);
        zos.closeEntry();
    }

    private static void copyStreamToStream(InputStream is, OutputStream os) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(is);
        int avail = bis.available();
        byte[] buffer = new byte[avail];
        bis.read(buffer, 0, avail);
        os.write(buffer, 0, avail);
    }

    /**
	 * Process a string for HTML formatting:
	 */
    public static String formatHtml(String str) {
        str = replace(str, "&", "&amp;");
        str = replace(str, "<", "&lt;");
        str = replace(str, "�", "&laquo;");
        str = replace(str, "�", "&raquo;");
        return str;
    }

    /**
	 * Replaces all ``@{...}'' tags.
	 * 
	 * @param s La cadena a procesar.
	 * @param   qname Nombre de base para resolver enlaces relativos.
	 */
    public static String processInlineTags(String s, String[] qname) {
        StringBuffer sb = new StringBuffer();
        int len = "@{".length();
        int i, p = 0;
        while ((i = s.indexOf("@{", p)) >= 0) {
            sb.append(s.substring(p, i));
            int e = s.indexOf("}", i + len);
            if (e < 0) {
                p = i;
                break;
            }
            String it = s.substring(i + len, e);
            sb.append(replaceInlineTag(it, qname));
            p = e + 1;
        }
        sb.append(s.substring(p));
        return sb.toString();
    }

    /**
	 * Replaces an inline tag.
	 * This is assumed to have the form:
	 *	p::q::u.x
	 * where x is one of:  i, o, c, e, a.
	 */
    private static String replaceInlineTag(String it, String[] qname) {
        if (!it.endsWith(".i") && !it.endsWith(".o") && !it.endsWith(".c") && !it.endsWith(".e") && !it.endsWith(".a")) {
            return it;
        }
        int len = it.length();
        String dest = it.substring(0, len - 2);
        char x = it.charAt(len - 1);
        String href = getRelativeLocation(qname, dest) + "." + x + ".html";
        String simple = dest;
        int i;
        if ((i = simple.lastIndexOf(":")) >= 0) {
            simple = simple.substring(i + 1);
        }
        return "<a href=\"" + href + "\"><code>" + simple + "</code></a>";
    }
}
