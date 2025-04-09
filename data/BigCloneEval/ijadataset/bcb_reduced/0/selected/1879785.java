package au.reba.PlaylistManager.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import au.reba.PlaylistManager.PlaylistManager;
import au.reba.PlaylistManager.Util.Logging.PMLogger;

/**
 * All the odd bits of code used statically here and there throughout the 
 * application
 * 
 * @author Reba Kearns
 */
public class Utils {

    /**
     * @param time value in seconds
     * @return the @param time value formatted as "[X day[s] ]HH:mm:ss"
     */
    public static String formatTime(Long time) {
        String result = "";
        long secsInDay = 24 * 60 * 60;
        long numDays = time / secsInDay;
        long remainder = time % secsInDay;
        remainder = remainder * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat();
        Date d;
        try {
            sdf.applyPattern("dd/MM/yyyy HH:mm:ss");
            d = sdf.parse("01/01/2000 00:00:00");
            long dateTime = d.getTime();
            dateTime += remainder;
            d.setTime(dateTime);
            sdf.applyPattern("HH:mm:ss");
            if (numDays != 0) {
                result += numDays + " day" + ((numDays > 1) ? "s" : "") + " ";
            }
            result += sdf.format(d);
        } catch (Exception e) {
            ((PMLogger) Logger.getLogger(PlaylistManager.LOGGER_KEY)).warning("Error formatting time String: " + e.getMessage());
        }
        return result;
    }

    /**
     * @return true if the supplied String is null or contains only whitespace,
     *          false otherwise
     */
    public static boolean nullOrEmptyString(String str) {
        if (str == null) return true;
        str = str.trim();
        if (str.length() == 0) return true;
        return false;
    }

    /**
     * Strips any unicode control characters from a string.
     */
    public static String stripControlChars(String s) {
        if (s == null) return s;
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isISOControl(s.codePointAt(i))) result += s.charAt(i);
        }
        return result;
    }

    /**
     * Check whether the supplied file is writable.<br>
     * As a side-effect, the File is created empty and ready for writing
     * 
     * @param file
     * @param fileTypeAndName a descriptive String used if there is an error.
     * @param deleteAndRecreate if true and the file already exists on the file 
     *                              system then delete it and create a new file
     */
    public static void checkFileWritability(File file, String fileTypeAndName, boolean deleteAndRecreate) throws Exception {
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) throw new Exception("Error: cannot create file '" + file.getPath() + "' for " + fileTypeAndName);
            } catch (Exception e) {
                throw new Exception("Error: cannot create file '" + file.getPath() + "' for " + fileTypeAndName + ": " + e.getMessage());
            }
        } else {
            if (!file.isFile()) throw new Exception("Error: cannot create/edit file for " + fileTypeAndName + ": '" + file.getPath() + "' is not a file");
            if (!file.canWrite()) throw new Exception("Error: cannot create/edit file for " + fileTypeAndName + ": file '" + file.getPath() + "' is not writable");
            if (deleteAndRecreate) {
                if (!file.delete()) throw new Exception("Error: cannot create/edit file for " + fileTypeAndName + ": cannot remove old file '" + file.getPath() + "'");
                if (!file.createNewFile()) throw new Exception("Error: cannot create/edit file '" + file.getPath() + "' for " + fileTypeAndName);
            }
        }
    }

    /**
     * convert all spaces and non-alphanumeric characters other than "." and 
     * "-" to underscores
     */
    public static String convertCharsForFileName(String fileName) {
        char[] temp = fileName.toCharArray();
        String convertedFileName = "";
        for (char c : temp) {
            if (Character.isLetterOrDigit(c)) convertedFileName += c; else {
                if ((c == '.') || (c == '-')) convertedFileName += c; else convertedFileName += "_";
            }
        }
        return convertedFileName;
    }

    /**
     * Copy the contents of File @param src to File @param dst. Does not check 
     * if either file exists, if the source file is readable or if the 
     * destination file is writable. 
     * 
     * @throws IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);
        copyFileToStream(src, out);
        out.close();
    }

    /**
     * Copy the contents of File @param src to OutputStream @param out. Does not
     * check if src file exists and is readable. Does not close OutputStream - 
     * that is up to the client code.
     * 
     * @throws IOException
     */
    public static void copyFileToStream(File src, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(src);
        copyStreamToStream(in, out);
        in.close();
    }

    /**
     * Copy the contents of InputStream @param in to File @param dst. Does not
     * check if dst file exists and is writable. Does not close InputStream - 
     * that is up to the client code.
     * 
     * @throws IOException
     */
    public static void copyStreamToFile(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);
        copyStreamToStream(in, out);
        out.close();
    }

    /**
     * Copies the contents of InputStream @param in to OutputStream @param out.
     * 
     * NB: Does not close either stream - that is up to the client code
     * 
     * @throws IOException
     */
    public static void copyStreamToStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Open and create a zip file with the supplied @param zipFileName (full 
     * filename path) and put the contents of the specified @param filenames
     * files in it. Will overwrite the zip file if it already exists without 
     * further confirmation from the user. Checks that @param zipFileName is 
     * writable, but does not check that the files in @param filenames exist or
     * are readable. 
     * 
     * @param includePathInfo - if true include the path information for the 
     * 							file, otherwise, omit this info
     */
    public static void createZipFile(String zipFileName, File[] files, boolean includePathInfo) throws Exception {
        File zipFile = new File(zipFileName);
        checkFileWritability(zipFile, "zip File '" + zipFile + "'", true);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        for (File file : files) {
            if (includePathInfo) out.putNextEntry(new ZipEntry(file.getPath())); else out.putNextEntry(new ZipEntry(file.getName()));
            copyFileToStream(file, out);
            out.closeEntry();
        }
        out.close();
    }

    /**
     * Replace any special XML characters in the supplied String with their 
     * escape sequences
     */
    public static String escapeXMLChars(String str) {
        String answer;
        answer = escapeSingleChar(str, '&');
        answer = escapeSingleChar(answer, '<');
        answer = escapeSingleChar(answer, '>');
        answer = escapeSingleChar(answer, '\'');
        answer = escapeSingleChar(answer, '"');
        return answer;
    }

    /** 
     * Replace all instances of a particular XML special character in the 
     * supplied String with its escape sequence
     */
    private static String escapeSingleChar(String str, char ch) {
        String answer;
        int pos = str.indexOf(ch);
        if (pos != -1) {
            String esc;
            switch(ch) {
                case '<':
                    esc = "&lt;";
                    break;
                case '>':
                    esc = "&gt;";
                    break;
                case '\'':
                    esc = "&apos;";
                    break;
                case '"':
                    esc = "&quot;";
                    break;
                case '&':
                    esc = "&amp;";
                    break;
                default:
                    esc = "" + ch;
            }
            String temp = escapeSingleChar(str.substring(pos + 1), ch);
            answer = str.substring(0, pos) + esc + temp;
        } else answer = str;
        return answer;
    }

    /**
     * This constant indicates a language from ISO 639-2 with no corresponding 
     * ISO 639-1 code
     */
    public static String UNKNOWN_LANG = "UNKNOWN_LANG";

    /**
     * Converts to and from the three-letter ISO 639-2 codes for languages as 
     * used in ID3 tags to the two-letter ISO 639-1 code used within Java. 
     * Return UNKNOWN_LANG if no corresponding code 
     */
    public static String id3LangCodeToFromJavaLangCode(String inputLangCode) {
        if (inputLangCode == null) return UNKNOWN_LANG;
        String outputLangCode = langLookup.get(inputLangCode);
        if (outputLangCode == null) return UNKNOWN_LANG; else return outputLangCode;
    }

    private static final Hashtable<String, String> langLookup = new Hashtable<String, String>();

    static {
        langLookup.put("aar", "aa");
        langLookup.put("abk", "ab");
        langLookup.put("afr", "af");
        langLookup.put("aka", "ak");
        langLookup.put("alb", "sq");
        langLookup.put("sqi", "sq");
        langLookup.put("amh", "am");
        langLookup.put("ara", "ar");
        langLookup.put("arg", "an");
        langLookup.put("arm", "hy");
        langLookup.put("hye", "hy");
        langLookup.put("asm", "as");
        langLookup.put("ava", "av");
        langLookup.put("ave", "ae");
        langLookup.put("aym", "ay");
        langLookup.put("aze", "az");
        langLookup.put("bak", "ba");
        langLookup.put("bam", "bm");
        langLookup.put("baq", "eu");
        langLookup.put("eus", "eu");
        langLookup.put("bel", "be");
        langLookup.put("ben", "bn");
        langLookup.put("bih", "bh");
        langLookup.put("bis", "bi");
        langLookup.put("bod", "bo");
        langLookup.put("tib", "bo");
        langLookup.put("bos", "bs");
        langLookup.put("bre", "br");
        langLookup.put("bul", "bg");
        langLookup.put("bur", "my");
        langLookup.put("mya", "my");
        langLookup.put("cat", "ca");
        langLookup.put("ces", "cs");
        langLookup.put("cze", "cs");
        langLookup.put("cha", "ch");
        langLookup.put("che", "ce");
        langLookup.put("chi", "zh");
        langLookup.put("zho", "zh");
        langLookup.put("chu", "cu");
        langLookup.put("chv", "cv");
        langLookup.put("cor", "kw");
        langLookup.put("cos", "co");
        langLookup.put("cre", "cr");
        langLookup.put("cym", "cy");
        langLookup.put("wel", "cy");
        langLookup.put("dan", "da");
        langLookup.put("deu", "de");
        langLookup.put("ger", "de");
        langLookup.put("div", "dv");
        langLookup.put("dut", "nl");
        langLookup.put("nld", "nl");
        langLookup.put("dzo", "dz");
        langLookup.put("ell", "el");
        langLookup.put("gre", "el");
        langLookup.put("eng", "en");
        langLookup.put("epo", "eo");
        langLookup.put("est", "et");
        langLookup.put("ewe", "ee");
        langLookup.put("fao", "fo");
        langLookup.put("fas", "fa");
        langLookup.put("per", "fa");
        langLookup.put("fij", "fj");
        langLookup.put("fin", "fi");
        langLookup.put("fra", "fr");
        langLookup.put("fre", "fr");
        langLookup.put("fry", "fy");
        langLookup.put("ful", "ff");
        langLookup.put("geo", "ka");
        langLookup.put("kat", "ka");
        langLookup.put("gla", "gd");
        langLookup.put("gle", "ga");
        langLookup.put("glg", "gl");
        langLookup.put("glv", "gv");
        langLookup.put("grn", "gn");
        langLookup.put("guj", "gu");
        langLookup.put("hat", "ht");
        langLookup.put("hau", "ha");
        langLookup.put("heb", "he");
        langLookup.put("her", "hz");
        langLookup.put("hin", "hi");
        langLookup.put("hmo", "ho");
        langLookup.put("hrv", "hr");
        langLookup.put("scr", "hr");
        langLookup.put("hun", "hu");
        langLookup.put("ibo", "ig");
        langLookup.put("ice", "is");
        langLookup.put("isl", "is");
        langLookup.put("ido", "io");
        langLookup.put("iii", "ii");
        langLookup.put("iku", "iu");
        langLookup.put("ile", "ie");
        langLookup.put("ina", "ia");
        langLookup.put("ind", "id");
        langLookup.put("ipk", "ik");
        langLookup.put("ita", "it");
        langLookup.put("jav", "jv");
        langLookup.put("jpn", "ja");
        langLookup.put("kal", "kl");
        langLookup.put("kan", "kn");
        langLookup.put("kas", "ks");
        langLookup.put("kau", "kr");
        langLookup.put("kaz", "kk");
        langLookup.put("khm", "km");
        langLookup.put("kik", "ki");
        langLookup.put("kin", "rw");
        langLookup.put("kir", "ky");
        langLookup.put("kom", "kv");
        langLookup.put("kon", "kg");
        langLookup.put("kor", "ko");
        langLookup.put("kua", "kj");
        langLookup.put("kur", "ku");
        langLookup.put("lao", "lo");
        langLookup.put("lat", "la");
        langLookup.put("lav", "lv");
        langLookup.put("lim", "li");
        langLookup.put("lin", "ln");
        langLookup.put("lit", "lt");
        langLookup.put("ltz", "lb");
        langLookup.put("lub", "lu");
        langLookup.put("lug", "lg");
        langLookup.put("mac", "mk");
        langLookup.put("mkd", "mk");
        langLookup.put("mah", "mh");
        langLookup.put("mal", "ml");
        langLookup.put("mao", "mi");
        langLookup.put("mri", "mi");
        langLookup.put("mar", "mr");
        langLookup.put("may", "ms");
        langLookup.put("msa", "ms");
        langLookup.put("mlg", "mg");
        langLookup.put("mlt", "mt");
        langLookup.put("mol", "mo");
        langLookup.put("mon", "mn");
        langLookup.put("nau", "na");
        langLookup.put("nav", "nv");
        langLookup.put("nbl", "nr");
        langLookup.put("nde", "nd");
        langLookup.put("ndo", "ng");
        langLookup.put("nep", "ne");
        langLookup.put("nno", "nn");
        langLookup.put("nob", "nb");
        langLookup.put("nor", "no");
        langLookup.put("nya", "ny");
        langLookup.put("oci", "oc");
        langLookup.put("oji", "oj");
        langLookup.put("ori", "or");
        langLookup.put("orm", "om");
        langLookup.put("oss", "os");
        langLookup.put("pan", "pa");
        langLookup.put("pli", "pi");
        langLookup.put("pol", "pl");
        langLookup.put("por", "pt");
        langLookup.put("pus", "ps");
        langLookup.put("que", "qu");
        langLookup.put("roh", "rm");
        langLookup.put("ron", "ro");
        langLookup.put("rum", "ro");
        langLookup.put("run", "rn");
        langLookup.put("rus", "ru");
        langLookup.put("sag", "sg");
        langLookup.put("san", "sa");
        langLookup.put("scc", "sr");
        langLookup.put("srp", "sr");
        langLookup.put("sin", "si");
        langLookup.put("slk", "sk");
        langLookup.put("slo", "sk");
        langLookup.put("slv", "sl");
        langLookup.put("sme", "se");
        langLookup.put("smo", "sm");
        langLookup.put("sna", "sn");
        langLookup.put("snd", "sd");
        langLookup.put("som", "so");
        langLookup.put("sot", "st");
        langLookup.put("spa", "es");
        langLookup.put("srd", "sc");
        langLookup.put("ssw", "ss");
        langLookup.put("sun", "su");
        langLookup.put("swa", "sw");
        langLookup.put("swe", "sv");
        langLookup.put("tah", "ty");
        langLookup.put("tam", "ta");
        langLookup.put("tat", "tt");
        langLookup.put("tel", "te");
        langLookup.put("tgk", "tg");
        langLookup.put("tgl", "tl");
        langLookup.put("tha", "th");
        langLookup.put("tir", "ti");
        langLookup.put("ton", "to");
        langLookup.put("tsn", "tn");
        langLookup.put("tso", "ts");
        langLookup.put("tuk", "tk");
        langLookup.put("tur", "tr");
        langLookup.put("twi", "tw");
        langLookup.put("uig", "ug");
        langLookup.put("ukr", "uk");
        langLookup.put("urd", "ur");
        langLookup.put("uzb", "uz");
        langLookup.put("ven", "ve");
        langLookup.put("vie", "vi");
        langLookup.put("vol", "vo");
        langLookup.put("wln", "wa");
        langLookup.put("wol", "wo");
        langLookup.put("xho", "xh");
        langLookup.put("yid", "yi");
        langLookup.put("yor", "yo");
        langLookup.put("zha", "za");
        langLookup.put("zul", "zu");
        langLookup.put("aa", "aar");
        langLookup.put("ab", "abk");
        langLookup.put("af", "afr");
        langLookup.put("ak", "aka");
        langLookup.put("sq", "sqi");
        langLookup.put("am", "amh");
        langLookup.put("ar", "ara");
        langLookup.put("an", "arg");
        langLookup.put("hy", "hye");
        langLookup.put("as", "asm");
        langLookup.put("av", "ava");
        langLookup.put("ae", "ave");
        langLookup.put("ay", "aym");
        langLookup.put("az", "aze");
        langLookup.put("ba", "bak");
        langLookup.put("bm", "bam");
        langLookup.put("eu", "eus");
        langLookup.put("be", "bel");
        langLookup.put("bn", "ben");
        langLookup.put("bh", "bih");
        langLookup.put("bi", "bis");
        langLookup.put("bo", "bod");
        langLookup.put("bs", "bos");
        langLookup.put("br", "bre");
        langLookup.put("bg", "bul");
        langLookup.put("my", "mya");
        langLookup.put("ca", "cat");
        langLookup.put("cs", "cze");
        langLookup.put("ch", "cha");
        langLookup.put("ce", "che");
        langLookup.put("zh", "zho");
        langLookup.put("cu", "chu");
        langLookup.put("cv", "chv");
        langLookup.put("kw", "cor");
        langLookup.put("co", "cos");
        langLookup.put("cr", "cre");
        langLookup.put("cy", "cym");
        langLookup.put("da", "dan");
        langLookup.put("de", "deu");
        langLookup.put("dv", "div");
        langLookup.put("nl", "nld");
        langLookup.put("dz", "dzo");
        langLookup.put("el", "ell");
        langLookup.put("en", "eng");
        langLookup.put("eo", "epo");
        langLookup.put("et", "est");
        langLookup.put("ee", "ewe");
        langLookup.put("fo", "fao");
        langLookup.put("fa", "fas");
        langLookup.put("fj", "fij");
        langLookup.put("fi", "fin");
        langLookup.put("fr", "fra");
        langLookup.put("fy", "fry");
        langLookup.put("ff", "ful");
        langLookup.put("ka", "kat");
        langLookup.put("gd", "gla");
        langLookup.put("ga", "gle");
        langLookup.put("gl", "glg");
        langLookup.put("gv", "glv");
        langLookup.put("gn", "grn");
        langLookup.put("gu", "guj");
        langLookup.put("ht", "hat");
        langLookup.put("ha", "hau");
        langLookup.put("he", "heb");
        langLookup.put("hz", "her");
        langLookup.put("hi", "hin");
        langLookup.put("ho", "hmo");
        langLookup.put("hr", "hrv");
        langLookup.put("hu", "hun");
        langLookup.put("ig", "ibo");
        langLookup.put("is", "isl");
        langLookup.put("io", "ido");
        langLookup.put("ii", "iii");
        langLookup.put("iu", "iku");
        langLookup.put("ie", "ile");
        langLookup.put("ia", "ina");
        langLookup.put("id", "ind");
        langLookup.put("ik", "ipk");
        langLookup.put("it", "ita");
        langLookup.put("jv", "jav");
        langLookup.put("ja", "jpn");
        langLookup.put("kl", "kal");
        langLookup.put("kn", "kan");
        langLookup.put("ks", "kas");
        langLookup.put("kr", "kau");
        langLookup.put("kk", "kaz");
        langLookup.put("km", "khm");
        langLookup.put("ki", "kik");
        langLookup.put("rw", "kin");
        langLookup.put("ky", "kir");
        langLookup.put("kv", "kom");
        langLookup.put("kg", "kon");
        langLookup.put("ko", "kor");
        langLookup.put("kj", "kua");
        langLookup.put("ku", "kur");
        langLookup.put("lo", "lao");
        langLookup.put("la", "lat");
        langLookup.put("lv", "lav");
        langLookup.put("li", "lim");
        langLookup.put("ln", "lin");
        langLookup.put("lt", "lit");
        langLookup.put("lb", "ltz");
        langLookup.put("lu", "lub");
        langLookup.put("lg", "lug");
        langLookup.put("mk", "mkd");
        langLookup.put("mh", "mah");
        langLookup.put("ml", "mal");
        langLookup.put("mi", "mao");
        langLookup.put("mr", "mar");
        langLookup.put("ms", "msa");
        langLookup.put("mg", "mlg");
        langLookup.put("mt", "mlt");
        langLookup.put("mo", "mol");
        langLookup.put("mn", "mon");
        langLookup.put("na", "nau");
        langLookup.put("nv", "nav");
        langLookup.put("nr", "nbl");
        langLookup.put("nd", "nde");
        langLookup.put("ng", "ndo");
        langLookup.put("ne", "nep");
        langLookup.put("nn", "nno");
        langLookup.put("nb", "nob");
        langLookup.put("no", "nor");
        langLookup.put("ny", "nya");
        langLookup.put("oc", "oci");
        langLookup.put("oj", "oji");
        langLookup.put("or", "ori");
        langLookup.put("om", "orm");
        langLookup.put("os", "oss");
        langLookup.put("pa", "pan");
        langLookup.put("pi", "pli");
        langLookup.put("pl", "pol");
        langLookup.put("pt", "por");
        langLookup.put("ps", "pus");
        langLookup.put("qu", "que");
        langLookup.put("rm", "roh");
        langLookup.put("ro", "ron");
        langLookup.put("rn", "run");
        langLookup.put("ru", "rus");
        langLookup.put("sg", "sag");
        langLookup.put("sa", "san");
        langLookup.put("sr", "srp");
        langLookup.put("si", "sin");
        langLookup.put("sk", "slk");
        langLookup.put("sl", "slv");
        langLookup.put("se", "sme");
        langLookup.put("sm", "smo");
        langLookup.put("sn", "sna");
        langLookup.put("sd", "snd");
        langLookup.put("so", "som");
        langLookup.put("st", "sot");
        langLookup.put("es", "spa");
        langLookup.put("sc", "srd");
        langLookup.put("ss", "ssw");
        langLookup.put("su", "sun");
        langLookup.put("sw", "swa");
        langLookup.put("sv", "swe");
        langLookup.put("ty", "tah");
        langLookup.put("ta", "tam");
        langLookup.put("tt", "tat");
        langLookup.put("te", "tel");
        langLookup.put("tg", "tgk");
        langLookup.put("tl", "tgl");
        langLookup.put("th", "tha");
        langLookup.put("ti", "tir");
        langLookup.put("to", "ton");
        langLookup.put("tn", "tsn");
        langLookup.put("ts", "tso");
        langLookup.put("tk", "tuk");
        langLookup.put("tr", "tur");
        langLookup.put("tw", "twi");
        langLookup.put("ug", "uig");
        langLookup.put("uk", "ukr");
        langLookup.put("ur", "urd");
        langLookup.put("uz", "uzb");
        langLookup.put("ve", "ven");
        langLookup.put("vi", "vie");
        langLookup.put("vo", "vol");
        langLookup.put("wa", "wln");
        langLookup.put("wo", "wol");
        langLookup.put("xh", "xho");
        langLookup.put("yi", "yid");
        langLookup.put("yo", "yor");
        langLookup.put("za", "zha");
        langLookup.put("zu", "zul");
    }
}
