package aoetec.javalang._351regex;

import static java.lang.System.out;
import java.io.Console;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import aoetec.util.lession.Lesson;

@Lesson(title = "正在表达式（regular expression）", lastModifed = "2007/02/16", keyword = { "                 *- Pattern（模式）                                 ", "java.util.regex--|- Matcher（匹配器）                               ", "                 *- PatternSyntaxException（正则表达式语法错误异常） ", "                                                                       ", "*- 正则表达式语法                                                       ", "|       字符串字面值、元字符、正则表达式字符类/预定义字符类、              ", "|       数量词（三种）、捕获组（Capturing Groups, backreference）、      ", "|       边界匹配器                                                      ", "|- 模式对象（Pattern）                                                  ", "|- 字符序列（子序列）                                                    ", "|- 匹配器对象（Matcher）                                                 ", "|- 三种形式的匹配                                                        ", "*- Pattern 类、Matcher 类里的常用方法、String 类里与正则表达式相关的方法   ", "                                                            ", " 正则表达式的匹配包含两部分：数量词部分的匹配、整个表达式的匹配 ", "" }, content = { "   --> 定义为字符串的正则表达式                            ", "   --> 编译为 模式对象                                    ", "   --> 模式对象与任意字符序列匹配（得到一个 匹配器对象）    ", "   --> 匹配器对象通过解释模式对字符序列执行匹配操作         ", "                                                                                         ", "1 java.util.regex.Pattern（模式） ", "       一个 Pattern（模式）对象 是一个正则表达式的编译表示形式。Pattern类里没有公有的 ", "   构造方法，为了创建一个 模式对象，需要使用其静态的 compile 方法                    ", "       public static Pattern compile(String regex)                               ", "   。模式对象可以与任意 字符序列 匹配，得到一个 Matcher（匹配器） 对象。             ", "                                                                                         ", "2 java.util.regex.Matcher（匹配器）     ", "       Matcher（匹配器）对象 是通过解释模式（Pattern）对字符序列（character sequence） ", "   执行匹配操作的引擎。Matcher类里没有公有的构造方法，通过调用Pattern类的 matcher 方法 ", "       public Matcher matcher(CharSequence input)", "   从模式创建匹配器。", "                                                                                         ", "3 java.util.regex.PatternSyntaxException（正则表达式语法错误异常）", "       表明正则表达式模式中的语法错误的 未经检查的异常", "                                                                                         ", "4 三种形式的匹配（对应于Matcher 类里三个方法）", "   matches     方法尝试将整个输入序列与该模式匹配。             （从头至尾）            ", "   lookingAt   方法尝试将输入序列从头开始与该模式匹配。         （从头 可以不至尾）      ", "   find        方法扫描输入序列以查找与该模式匹配的下一个子序列。（可以不从头 可以不至尾）", "                                                                                         ", "5 字符串字面值（String literals）               ", "         cell 0        cell 1        cell 2    ", "   +-------------+-------------+-------------+ ", "   |       f     |       o     |       o     | ", "   +-------------+-------------+-------------+ ", "   |             |             |               ", " index 0       index 1       index 2           ", "                                                                                         ", "6 元字符（Matacharacters）", "       影响模式匹配方式的特殊字符 ", "   把元字符作为普通字符处理的情况", "       在元字符前加一个反斜杠'\'（precede the metacharacter with a backslash）", "       把元字符放在\\Q与\\E之间", "                                                                                         ", "7 字符类（Character class）", "       词组 字符类（Character class）里的 类（class）并不表示一个.class文件，        ", "   在正则表达式里，字符类表示用中括号[]括起来的字符集合，用来匹配字符序列里的单个字符。 ", "                                                                            ", "   +---------------------------------------------------------------------+  ", "   |       字 符 类（Character classes）                                  |  ", "   +---------------------------------------------------------------------+  ", "   |[abc]           a、b 或 c（简单类 Simple Classes）                    |  ", "   +---------------------------------------------------------------------+  ", "   |[^abc]          任何字符，除了 a、b 或 c（否定 Negation）              |  ", "   +---------------------------------------------------------------------+  ", "   |[a-zA-Z]        a 到 z 或 A 到 Z，两头的字母包括在内（范围 Ranges）    |   ", "   +---------------------------------------------------------------------+  ", "   |[a-d[m-p]]      a 到 d 或 m 到 p：[a-dm-p]（并集 Unions）             |  ", "   +---------------------------------------------------------------------+  ", "   |[a-z&&[def]]    d、e 或 f（交集 Intersections）                       |  ", "   +---------------------------------------------------------------------+  ", "   |[a-z&&[^bc]]    a 到 z，除了 b 和 c：[ad-z]（减去 Subtraction）       |   ", "   +---------------------------------------------------------------------+  ", "   |[a-z&&[^m-p]]   a 到 z，而非 m 到 p：[a-lq-z]（减去 Subtraction）     |  ", "   +---------------------------------------------------------------------+  ", "                                                                                         ", "8  预定义字符类（Predefined character classes）", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "                                                                                         ", "9 数量词（Quantifiers）", "   +------------------------------+-------------------------------+   ", "   |     数量词（Quantifiers）     |                               |   ", "   +-------+----------+-----------+           含义（Meaning）      |   ", "   |Greedy |Reluctant |Possessive |                               |   ", "   +-------+----------+-----------+-------------------------------+   ", "   |X?     |X??       |X?+        |X, 一次或一次也没有             |   ", "   +-------+----------+-----------+-------------------------------+   ", "   |X*     |X*?       |X*+        |X, 零次或多次                   |   ", "   +-------+----------+-----------+-------------------------------+   ", "   |X+     |X+?       |X++        |X, 一次或多次                   |   ", "   +-------+----------+-----------+-------------------------------+   ", "   |X{n}   |X{n}?     |X{n}+      |X, 恰好 n 次                    |  ", "   +-------+----------+-----------+-------------------------------+   ", "   |X{n,}  |X{n,}?    |X{n,}+     |X, 至少 n 次                    |  ", "   +-------+----------+-----------+-------------------------------+   ", "   |X{n,m} |X{n,m}?   |X{n,m}+    |X, 至少 n 次，但是不超过 m 次    |  ", "   +-------+----------+-----------+-------------------------------+   ", "   零长度匹配（Zero-Length Matches）", "       当使用?或*时会发生0长度匹配的情况", "   数量词可以和单个字符、字符类以及捕获组一起使用构造正则表达式", "   Greedy数量词、Reluctant数量词、Possessive数量词之间的差别", "       Greedy数量词：首先读取整个字符序列，同正则表达式里的 数量词部分 匹配，", "           不成功则后退一个字符 重新匹配，成功后同整个正则表达式匹配", "       Reluctant数量词：首先读取一个字符，同正则表达式里的 数量词部分 匹配，", "           不成功则前进一个字符 重新匹配，成功后同整个正则表达式匹配", "       Possessive数量词：首先读取整个字符序列，同正则表达式里的 数量词部分 匹配，", "           不成功则退出，成功后同整个正则表达式匹配", "                                                                                         ", "10 捕获组（Capturing Groups）", "   正则表达式abc+、[abc]+、(abc)+的含义", "   确定正则表达式里捕获组的个数及编号 1 ((A)(B(C))) ,2 (A) ,3 (B(C)) ,4 (C) ", "   backreference", "                                                                                         ", "11 边界匹配器（Boundary Matchers）", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "                                                                                         ", "12 String 类里支持正则表达式的方法", "   ", "" })
public class A01_RegularExpression {

    public static void main(String[] args) {
        RegexTaste regexTasteImpl = new RegexTaste() {

            public boolean containTwoOrMoreNumbers(CharSequence input) {
                String regex = "\\d{2,}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(input);
                return m.find();
            }
        };
        useFind();
    }

    static interface RegexTaste {

        /**
         * 判断 输入的字符序列里 是否包含 2个或2个以上 数字。
         *
         * @param input 输入的字符序列
         * @return true 当且仅当输入序列里包含2个或2个以上数字
         *
         * @author JKSUN
         */
        boolean containTwoOrMoreNumbers(CharSequence input);
    }

    static void useMatches() {
        Console c = System.console();
        if (c == null) {
            System.err.println("No console");
            System.exit(1);
        }
        while (true) {
            Pattern p = Pattern.compile(c.readLine("%n matches(), Enter your regex:"));
            Matcher m = p.matcher(c.readLine("Enter input string to search:"));
            if (m.matches()) {
                c.printf("Found the text \"%s\" starting at %d and ending at %d.%n", m.group(), m.start(), m.end());
            } else {
                c.format("No matcher found.%n");
            }
        }
    }

    static void useLookingAt() {
        Console c = System.console();
        if (c == null) {
            System.err.println("No console!");
            System.exit(1);
        }
        while (true) {
            Pattern p = Pattern.compile(c.readLine("%n lookingAt(), Enter your regex:"));
            Matcher m = p.matcher(c.readLine("Enter input string to search:"));
            if (m.lookingAt()) {
                c.printf("Found the text \"%s\" starting at %d end ending at %d.%n", m.group(), m.start(), m.end());
            } else {
                c.printf("No matcher found.%n");
            }
        }
    }

    static void useFind() {
        Console c = System.console();
        if (c == null) {
            System.err.println("No console");
            System.exit(1);
        }
        while (true) {
            Pattern p = Pattern.compile(c.readLine("%n find(), Enter your regex:"));
            Matcher m = p.matcher(c.readLine("Enter input string to search:"));
            boolean found = false;
            while (m.find()) {
                c.printf("Found the text \"%s\" starting at %d and ending at %d.%n", m.group(), m.start(), m.end());
                found = true;
            }
            if (!found) {
                c.format("No matcher found.%n");
            }
        }
    }

    static void regexMethodsInString() {
        String regex = null;
        String str = null;
        regex = "\\d+\\w\\d+";
        str = "1e34";
        out.println(str.matches(regex));
        regex = "\\d:";
        str = "boo4:and5:foo6:and  ";
        String[] strs = str.split(regex);
        System.out.println(Arrays.asList(strs));
        strs = str.split(regex, 3);
        System.out.println(Arrays.asList(strs));
        String temp = str.replaceFirst(regex, "+");
        System.out.printf("%n%s%n%s%n", str, temp);
        temp = str.replaceAll(regex, "+");
        System.out.printf("%n%s%n%s%n", str, temp);
        temp = str.replace("4:", new StringBuilder("+-"));
        System.out.printf("%n%s%n%s%n", str, temp);
        temp = str.replace(":", new StringBuilder("+"));
        System.out.printf("%n%s%n%s%n", str, temp);
    }
}
