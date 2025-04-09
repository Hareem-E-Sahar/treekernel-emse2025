package se.sandos.pediasuckr;

import java.util.Date;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.sandos.pediasuckr.wikipedia.AbstractArticlePart;
import se.sandos.pediasuckr.wikipedia.Article;
import se.sandos.pediasuckr.wikipedia.Section;
import se.sandos.pediasuckr.wikipedia.Text;

public class Test {

    public static Statement st = null;

    public static PreparedStatement prepSelectLink = null;

    public static PreparedStatement prepInsertLink = null;

    public static PreparedStatement prepSelectConcept = null;

    public static PreparedStatement prepInsertConcept = null;

    public static PreparedStatement prepSelectHisto = null;

    public static PreparedStatement prepInsertHisto = null;

    public static PreparedStatement prepUpdateHisto = null;

    public static Connection conn = null;

    public static HashMap<StringDuo, Integer> wFreq = new HashMap<StringDuo, Integer>(2000000, 0.5f);

    private static String inputString;

    private static String nextPart;

    private static String sectionName = null;

    private static Article article = null;

    private static Section currSection = null;

    private static AbstractArticlePart currentPart = null;

    static {
        Integer tmp = new Integer(-1);
        wFreq.put(new StringDuo("", ""), tmp);
        wFreq.put(new StringDuo("#redirect", ""), tmp);
        wFreq.put(new StringDuo("#redirect", "{{r from camelcase}}"), tmp);
        wFreq.put(new StringDuo("see also == *", ""), tmp);
    }

    private static int count = 0;

    private class My implements Comparator {

        public int compare(Object o1, Object o2) {
            Object[] raw1 = (Object[]) o1;
            Object[] raw2 = (Object[]) o2;
            return ((Integer) raw2[0]).compareTo((Integer) raw1[0]);
        }
    }

    public static void addLinks(String from, Matcher to, String type, String namespace, String all) throws SQLException {
        if (from.indexOf(":") != -1) {
            return;
        }
        if (type.equals("wikiLink")) {
            count++;
            if ((count % 2000) == 0) {
                st.execute("SELECT COUNT(*) FROM histo");
                ResultSet rs = st.getResultSet();
                while (rs.next()) {
                    System.out.print(rs.getInt(1));
                }
                System.out.println(" " + count + " " + new Date());
            }
            if ((count % 10000) == 0) {
                System.out.println("\nstart -------------------------------- " + new Date());
                try {
                    st.setMaxRows(100);
                    st.execute("SELECT * FROM histo ORDER BY cnt DESC");
                    ResultSet rs = st.getResultSet();
                    while (rs.next()) {
                        System.out.println("" + rs.getString(1) + " = " + rs.getInt(2));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                System.out.println("-------------------------------- " + new Date());
            }
        }
        while (to.find()) {
            if (type.equals("wikiLink")) {
                int start = Math.max(0, to.start(0) - 100);
                int end = Math.min(all.length(), to.end(0) + 100);
                String contextBefore = all.substring(start, to.start(0));
                String contextEnd = all.substring(to.end(0), end);
                List bWords = null;
                List eWords = null;
                if (!(contextBefore.length() < 2 || (contextBefore.length() > 0 && contextBefore.charAt(contextBefore.length() - 1) != ' '))) {
                    bWords = getWordsReverse(contextBefore);
                }
                if (!(contextEnd.length() < 2 || (contextEnd.length() > 0 && contextEnd.charAt(0) != ' '))) {
                    eWords = getWords(contextEnd);
                }
                int wordBudget = 1;
                int bias = wordBudget;
                while (bias >= 0) {
                    if ((bias == 0 || (bWords != null && bWords.size() >= bias)) && (wordBudget - bias == 0 || (eWords != null && eWords.size() >= wordBudget - bias))) {
                        StringDuo tmp = new StringDuo(takeWords(bWords, true, bias), takeWords(eWords, false, wordBudget - bias));
                        addOneDB(tmp.toString());
                    }
                    bias--;
                }
            }
        }
    }

    private static String takeWords(List from, boolean isForward, int number) {
        if (number == 0) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        int pos, step;
        if (isForward) {
            pos = number - 1;
            step = -1;
        } else {
            pos = 0;
            step = 1;
        }
        while (number > 0) {
            result.append(from.get(pos));
            pos += step;
            number--;
            if (number > 0) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
	 * DB-backed histogram addition.
	 * 
	 * @param what
	 * @throws SQLException 
	 */
    private static void addOneDB(String what) throws SQLException {
        try {
            prepUpdateHisto.setString(1, what);
            int res = prepUpdateHisto.executeUpdate();
            if (res == 0) {
                prepInsertHisto.setString(1, what);
                prepInsertHisto.setInt(2, 1);
                if (prepInsertHisto.executeUpdate() == 0) {
                    throw new Exception("skit");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static List getWordsReverse(String in) {
        LinkedList l = new LinkedList();
        StringTokenizer st = new StringTokenizer(in);
        while (st.hasMoreTokens()) {
            l.addFirst(st.nextToken());
        }
        return l;
    }

    private static List getWords(String in) {
        List l = new LinkedList();
        StringTokenizer st = new StringTokenizer(in);
        while (st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        return l;
    }

    public static void parse(String text, String title) {
        if (title.indexOf(":") != -1) {
            return;
        }
        System.out.println(text);
        text.replace("&lt;", "<");
        text.replace("&gt;", ">");
        text.replace("&quot;", "\"");
        String clean = text;
        clean = clean.replace("[", "");
        clean = clean.replace("]", "");
        clean = clean.replace("}", "");
        clean = clean.replace("{", "");
        clean = clean.replace(".", "");
        clean = clean.replace(",", "");
        clean = clean.toLowerCase();
        StringTokenizer st = new StringTokenizer(clean);
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            Pattern p = Pattern.compile("^[\\p{Alpha}']*$");
            Matcher m = p.matcher(word);
            boolean b = m.matches();
            if (b) {
                int startTicks = 0;
                while (startTicks < word.length() && word.charAt(startTicks) == '\'') {
                    startTicks++;
                }
                int endTicks = 0;
                while (endTicks < word.length() && word.charAt(word.length() - endTicks - 1) == '\'') {
                    endTicks++;
                }
                if (endTicks != word.length() && (endTicks != 0 || startTicks != 0)) {
                    if (endTicks == startTicks) {
                        if (endTicks > 1 && endTicks < 4 || endTicks == 0 || startTicks == 0) {
                            word = word.substring(startTicks, word.length() - endTicks);
                            if (word.length() < 256) {
                            }
                        }
                    }
                } else {
                    if (word.indexOf('\'') == -1 && word.length() < 256) {
                    }
                }
            }
        }
        count++;
        if ((count % 1000000) == 0) {
            System.out.println(count + ": " + wordMap.size() + " " + new Date().getTime());
            try {
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Committed " + new Date().getTime());
        }
        if ((count % 600000) == 0) {
            dumpToFile();
        }
    }

    public static Map wordMap = new HashMap();

    public static void lek() {
        article = new Article();
        currentPart = article;
        currSection = new Section(article, null);
        add(currSection);
        File f = new File("articles/law.txt");
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f));
            char[] text = new char[1000000];
            int res = isr.read(text, 0, 1000000);
            String resString = new String(text, 0, res);
            Pattern start = Pattern.compile("<nowiki\\s*>");
            Pattern end = Pattern.compile("</nowiki\\s*>");
            inputString = resString;
            while (inputString.length() > 0) {
                if (findNext(start)) {
                    if (nextPart.length() > 0) {
                        parseFirstLevel(nextPart);
                    }
                    if (findNext(end)) {
                        System.out.print(">" + nextPart.replace("\n", "").replace("\r", "") + "<");
                        if (currentPart instanceof Text) {
                            Text c = (Text) currentPart;
                            c.setText(c.getText() + nextPart);
                        } else {
                            Text t = new Text(currentPart, nextPart);
                            add(t);
                        }
                    }
                } else {
                    parseFirstLevel(inputString);
                    inputString = "";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printTree(article);
        System.exit(1);
    }

    private static void printTree(AbstractArticlePart ap) {
        LinkedList<AbstractArticlePart> next = new LinkedList<AbstractArticlePart>();
        next.addFirst(ap);
        while (!next.isEmpty()) {
            AbstractArticlePart curr = next.removeFirst();
            StringBuffer repl = new StringBuffer();
            for (int l = 0; l < curr.getLevel(); l++) {
                repl.append(" ");
            }
            System.out.println(repl.toString() + curr.toString().replace("\n", "\n" + repl.toString()) + " " + curr.getLevel());
            next.addAll(0, curr.getChildren());
        }
    }

    /**
	 * Parses one nowiki-free chunk of text. This can be small!
	 * Could be a fragment _within_ a list, for example.
	 * Will need to use global data to build the parse-tree.
	 * 
	 * @param part
	 */
    private static void parseFirstLevel(String part) {
        Pattern p = Pattern.compile("^=([^\r\n]*)=$", Pattern.MULTILINE);
        Matcher m = p.matcher(part);
        int lastHandled = -1;
        int oldend = 0;
        while (m.find()) {
            System.out.println("\nThis section (" + sectionName + ") ends with \n---------------------------\n" + part.substring(oldend, m.start(1) - 1) + "---------------------------");
            int start = m.start(1) - 1;
            addNewSection(part.substring(oldend, start), m.group(1));
            oldend = m.end(1) + 3;
            System.out.print("Going from section " + sectionName + " to " + m.group(1));
            sectionName = m.group(1);
            lastHandled = m.end(1) + 3;
        }
        if (lastHandled == -1) {
            System.out.print(part);
            if (currentPart instanceof Text) {
                ((Text) currentPart).appendText(part);
            } else {
                Text t = new Text(currentPart, part);
                add(t);
            }
        } else if (lastHandled < part.length()) {
            String curr = part.substring(lastHandled, part.length());
            System.out.print("(" + sectionName + ") part: \n---------------------------\n" + curr);
            if (currentPart instanceof Text) {
                ((Text) currentPart).appendText(curr);
            } else if (currentPart instanceof Section) {
                Text t = new Text(currentPart, curr);
                add(t);
            }
        }
    }

    private static void add(AbstractArticlePart part) {
        currentPart.add(part);
        currentPart = part;
    }

    private static void addNewSection(String section, String newName) {
        if (currentPart instanceof Text) {
            ((Text) currentPart).appendText(section);
        } else if (section.length() > 0) {
            Text t = new Text(currentPart, section);
            add(t);
        }
        AbstractArticlePart c = currentPart;
        Section tempS = new Section(currentPart, newName);
        majs: while (true) {
            while (c != null && !(c instanceof Section)) {
                AbstractArticlePart n = c.getParent();
                if (n == null) {
                    break majs;
                }
                c = n;
            }
            if (c != null) {
                if (c instanceof Section) {
                    if (((Section) c).getSectionLevel() < tempS.getSectionLevel()) {
                        break;
                    }
                }
                c = c.getParent();
            } else {
                break;
            }
        }
        currentPart = c;
        Section s = new Section(currentPart, newName);
        if (c instanceof Section) {
            System.out.println("Found parent section " + ((Section) c).getName() + " to us: " + newName);
        } else {
            System.out.println("We are topmost " + sectionName + " parent: " + c + " Level: " + s.getLevel());
        }
        add(s);
        currSection = s;
    }

    private static boolean findNext(Pattern p) {
        Matcher m = p.matcher(inputString);
        if (m.find()) {
            nextPart = inputString.substring(0, m.start(0));
            inputString = inputString.substring(m.end(0), inputString.length());
            return true;
        } else {
            return false;
        }
    }

    public static void dumpToFile() {
        File f = new File("outdump" + count + ".csv");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        PrintStream pos = new PrintStream(fos);
        Iterator i = wordMap.keySet().iterator();
        while (i.hasNext()) {
            ByteArrayClass bac = (ByteArrayClass) i.next();
            Integer in = (Integer) wordMap.get(bac);
            try {
                String word = new String(bac.getArray(), "US-ASCII");
                pos.println(word + ", " + in);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            ResultSet rs = prepSelectHisto.executeQuery();
            while (rs.next()) {
                pos.println(rs.getString(1) + ", " + rs.getInt(2));
            }
        } catch (Throwable e) {
        }
    }

    private static void addOne2(String word) {
        ByteArrayClass bac = null;
        try {
            bac = new ByteArrayClass(word.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return;
        }
        Object o = wordMap.get(bac);
        if (o != null) {
            Integer i = (Integer) o;
            if (i.intValue() == -1) {
                return;
            }
            i = new Integer(i.intValue() + 1);
            wordMap.put(bac, i);
        } else {
            if (wordMap.size() > 2000000) {
                try {
                    addOneDB(word);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                wordMap.put(bac, new Integer(1));
            }
        }
    }
}
