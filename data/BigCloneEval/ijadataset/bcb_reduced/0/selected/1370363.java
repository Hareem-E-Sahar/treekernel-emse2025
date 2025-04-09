package ca.qc.mtl.cdn.plr.pinyin;

import java.text.Collator;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pinyin {

    private static final String[] allPinyinArray = { "a", "o", "e", "ai", "ei", "ao", "ou", "an", "en", "ang", "eng", "er", "yo", "yi", "ya", "ye", "yao", "you", "yan", "yin", "yang", "ying", "wu", "wa", "wo", "wai", "wei", "wan", "wen", "wang", "weng", "yu", "yue", "yuan", "yun", "yong", "bi", "ba", "bo", "bai", "bei", "bao", "ban", "ben", "bang", "beng", "bie", "biao", "bian", "bin", "bing", "bu", "pi", "pa", "po", "pai", "pei", "pao", "pou", "pan", "pen", "pang", "peng", "pie", "piao", "pian", "pin", "ping", "pu", "mi", "ma", "mo", "me", "mai", "mei", "mao", "mou", "man", "men", "mang", "meng", "mie", "miao", "miu", "mian", "min", "ming", "mu", "fa", "fo", "fei", "fou", "fan", "fen", "fang", "feng", "fu", "di", "da", "de", "dai", "dei", "dao", "dou", "dan", "dang", "deng", "die", "diao", "diu", "dian", "ding", "du", "duo", "dui", "duan", "dun", "dong", "ti", "ta", "te", "tai", "tao", "tou", "tan", "tang", "teng", "tie", "tiao", "tian", "ting", "tu", "tuo", "tui", "tuan", "tun", "tong", "ni", "na", "ne", "nai", "nei", "nao", "nou", "nan", "nen", "nang", "neng", "nie", "niao", "niu", "nian", "nin", "niang", "ning", "nu", "nuo", "nuan", "nong", "nv", "nue", "li", "la", "lo", "le", "lai", "lei", "lao", "lou", "lan", "lang", "leng", "lia", "lie", "liao", "liu", "lian", "lin", "liang", "ling", "lu", "luo", "luan", "lun", "long", "lv", "lue", "ga", "ge", "gai", "gei", "gao", "gou", "gan", "gen", "gang", "geng", "gu", "gua", "guo", "guai", "gui", "guan", "gun", "guang", "gong", "ka", "ke", "kai", "kei", "kao", "kou", "kan", "ken", "kang", "keng", "ku", "kua", "kuo", "kuai", "kui", "kuan", "kun", "kuang", "kong", "ha", "he", "hai", "hei", "hao", "hou", "han", "hen", "hang", "heng", "hu", "hua", "huo", "huai", "hui", "huan", "hun", "huang", "hong", "ji", "jia", "jie", "jiao", "jiu", "jian", "jin", "jiang", "jing", "ju", "juan", "jun", "jue", "jiong", "qi", "qia", "qie", "qiao", "qiu", "qian", "qin", "qiang", "qing", "qu", "quan", "qun", "que", "qiong", "xi", "xia", "xie", "xiao", "xiu", "xian", "xin", "xiang", "xing", "xu", "xuan", "xun", "xue", "xiong", "zhi", "zha", "zhe", "zhai", "zhei", "zhao", "zhou", "zhan", "zhen", "zhang", "zheng", "zhu", "zhua", "zhuo", "zhuai", "zhui", "zhuan", "zhun", "zhuang", "zhong", "chi", "cha", "che", "chai", "chao", "chou", "chan", "chen", "chang", "cheng", "chu", "chua", "chuo", "chuai", "chui", "chuan", "chun", "chuang", "chong", "shi", "sha", "she", "shai", "shei", "shao", "shou", "shan", "shen", "shang", "sheng", "shu", "shua", "shuo", "shuai", "shui", "shuan", "shun", "shuang", "ri", "re", "rao", "rou", "ran", "ren", "rang", "reng", "ru", "ruo", "rui", "ruan", "run", "rong", "zi", "za", "ze", "zai", "zei", "zao", "zou", "zan", "zen", "zang", "zeng", "zu", "zuo", "zui", "zuan", "zun", "zong", "ci", "ca", "ce", "cai", "cao", "cou", "can", "cen", "cang", "ceng", "cu", "cuo", "cui", "cuan", "cun", "cong", "si", "sa", "se", "sai", "sao", "sou", "san", "sen", "sang", "seng", "su", "suo", "sui", "suan", "sun", "song" };

    public static final char COMBINING_GRAVE_ACCENT = '̀';

    public static final char COMBINING_ACUTE_ACCENT = '́';

    public static final char COMBINING_MACRON = '̄';

    public static final char COMBINING_BREVE = '̆';

    public static final char COMBINING_CARON = '̌';

    private static final RuleBasedCollator myCollator = generateCollator();

    private static final char[] pinyinVoy;

    private static final Pattern p;

    private static final int NOT_FOUND = -1;

    static {
        String COMBININGS = "" + COMBINING_CARON + COMBINING_ACUTE_ACCENT + COMBINING_MACRON + COMBINING_ACUTE_ACCENT + COMBINING_GRAVE_ACCENT;
        String voyellBase = "aeiou";
        StringBuilder sb = new StringBuilder((COMBININGS.length() + 1) * voyellBase.length());
        for (int i = 0; i < COMBININGS.length(); i++) {
            char comb = COMBININGS.charAt(i);
            for (int j = 0; j < voyellBase.length(); j++) {
                char voy = voyellBase.charAt(j);
                sb.append(voy).append(comb);
            }
        }
        sb.append(voyellBase);
        sb.append(sb);
        for (int i = sb.length() / 2; i < sb.length(); i++) {
            char voy = sb.charAt(i);
            voy = Character.toUpperCase(voy);
            sb.setCharAt(i, voy);
        }
        String allVoyelle = Normalizer.normalize(sb, Form.NFC);
        pinyinVoy = new char[allVoyelle.length()];
        allVoyelle.getChars(0, allVoyelle.length(), pinyinVoy, 0);
        Arrays.sort(pinyinVoy);
        String regex = "((?:b|c|d|f|g|h|j|k|l|m|n|p|q|r|s|t|w|x|y|z|zh|sh|ch)" + "(?![" + COMBININGS + "]))?(?:[" + allVoyelle + COMBININGS + "]){1,4}" + "(?:(?:ngr|ng|nr|n|r)(?![" + allVoyelle + "]))?+\\d?";
        p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    private static int binarySearch(char x) {
        int low = 0;
        int high = pinyinVoy.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (pinyinVoy[mid] < x) low = mid + 1; else if (pinyinVoy[mid] > x) high = mid - 1; else return mid;
        }
        return NOT_FOUND;
    }

    public static Collator getCollator() {
        return (Collator) myCollator.clone();
    }

    /**
	 * @return
	 */
    private static RuleBasedCollator generateCollator() {
        Collator collator = Collator.getInstance();
        RuleBasedCollator ruleBasedCollator = (RuleBasedCollator) collator;
        String rule = ruleBasedCollator.getRules();
        RuleBasedCollator myCollator = null;
        try {
            String addRules = "& a < ā , Ā ; á , Á ; ǎ , Ǎ ; à , À ; a , A" + "& e < ē , Ē ; é , É ; ě , Ě ; è , È ; e , E" + "& i < ī , Ī ; í , Í ; ǐ , Ǐ ; ì , Ì ; i , I" + "& o < ō , Ō ; ó , Ó ; ǒ , Ǒ ; ò , Ò ; o , O" + "& u < ū , Ū ; ú , Ú ; ǔ , Ǔ ; ù , Ù ; u , U ; ǖ , Ǖ ; ǘ , Ǘ ; ǚ , Ǚ ; ǜ , Ǜ ;ü , Ü";
            myCollator = new RuleBasedCollator(rule + addRules);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return myCollator;
    }

    static final HashSet<String> allPinyin = new HashSet<String>();

    static {
        for (String s : allPinyinArray) {
            allPinyin.add(s);
        }
    }

    /**
	 * <ul>
	 * <li><b>a</b> and <b>e</b> trump all other vowels and always take the tone
	 * mark. There are no Mandarin syllables in Hanyu Pinyin that contain both a
	 * and e.
	 * <li>
	 * In the combination <b>ou</b>, <b>o</b> takes the mark.
	 * <li>In all other cases, the final vowel takes the mark.
	 * </ul>
	 * 
	 * @param input
	 * @return
	 */
    public String convertSyllable(String input) {
        input = Normalizer.normalize(input, Form.NFD);
        return convertSyllable_(input);
    }

    private String convertSyllable_(String input) {
        StringBuilder returnString = new StringBuilder(input.length() + 4);
        char toaccent = 0;
        int position = 0;
        int tone = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (isVoyel(c)) {
                if (setNewToAccent(toaccent, c)) {
                    toaccent = c;
                    position = i;
                }
                returnString.append(c);
            } else if (!Character.isDigit(c)) {
                returnString.append(c);
            } else if (Character.isDigit(c)) {
                tone = Character.digit(c, 10);
            } else if (isCombining(c)) {
                tone = getToneFromCombined(c);
            }
        }
        toaccent = getCharTone(toaccent, tone);
        returnString.setCharAt(position, toaccent);
        return returnString.toString();
    }

    private boolean setNewToAccent(char toaccent, char c) {
        switch(toaccent) {
            case 'a':
            case 'A':
                return false;
            case 'e':
            case 'E':
                if (c == 'i' || c == 'I') {
                    return false;
                }
                break;
            case 'i':
            case 'I':
                break;
            case 'o':
            case 'O':
                if (c == 'u') {
                    return false;
                }
                break;
            case 'u':
            case 'v':
            case 'U':
            case 'V':
                return true;
        }
        return true;
    }

    private char getCharTone(char toaccent, int tone) {
        char combining;
        switch(tone) {
            case 1:
                combining = COMBINING_MACRON;
                break;
            case 2:
                combining = COMBINING_ACUTE_ACCENT;
                break;
            case 3:
                combining = COMBINING_CARON;
                break;
            case 4:
                combining = COMBINING_GRAVE_ACCENT;
                break;
            default:
                return toaccent;
        }
        StringBuilder sb = new StringBuilder(2);
        sb.append(toaccent);
        sb.append(combining);
        String norm = Normalizer.normalize(sb, Form.NFC);
        return norm.charAt(0);
    }

    private boolean isVoyel(char c) {
        return binarySearch(c) != NOT_FOUND;
    }

    public List<String> separate(String input) {
        ArrayList<String> list = new ArrayList<String>();
        java.util.regex.Matcher m = p.matcher(input);
        while (m.find()) {
            String s = m.group();
            list.add(s);
        }
        return list;
    }

    /**
	 * @return the word
	 */
    public String getWord() {
        return word;
    }

    /**
	 * @return the tone
	 */
    public int getTone() {
        return tone;
    }

    private String word;

    private int tone;

    public void extractTone(String originalWord) {
        StringBuilder builder = new StringBuilder();
        String norm = Normalizer.normalize(originalWord, Form.NFD);
        for (int i = 0; i < norm.length(); i++) {
            char ch = norm.charAt(i);
            if (Character.isDigit(ch)) {
                tone = Character.getNumericValue(ch);
                continue;
            }
            if (isCombining(ch)) {
                tone = getToneFromCombined(ch);
            } else {
                builder.append(ch);
            }
        }
        word = builder.toString();
    }

    /**
	 * @param ch
	 */
    private int getToneFromCombined(char ch) {
        int tone;
        switch(ch) {
            case COMBINING_MACRON:
                tone = 1;
                break;
            case COMBINING_ACUTE_ACCENT:
                tone = 2;
                break;
            case COMBINING_BREVE:
            case COMBINING_CARON:
                tone = 3;
                break;
            case COMBINING_GRAVE_ACCENT:
                tone = 4;
                break;
            default:
                tone = 5;
        }
        return tone;
    }

    /**
	 * @param ch
	 * @return
	 */
    private boolean isCombining(char ch) {
        return Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS.equals(Character.UnicodeBlock.of(ch));
    }

    /**
	 * @param actual
	 * @return
	 */
    public String convertToAccent(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        Matcher m = p.matcher(input);
        int start = 0;
        while (m.find()) {
            String sep = m.group();
            String sep2 = convertSyllable_(sep);
            String inter = input.substring(start, m.start());
            sb.append(inter);
            sb.append(sep2);
            start = m.end();
        }
        String inter = input.substring(start, input.length());
        sb.append(inter);
        return sb.toString();
    }
}
