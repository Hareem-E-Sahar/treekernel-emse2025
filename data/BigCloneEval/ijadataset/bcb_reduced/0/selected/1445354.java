package application.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import application.Launcher;
import application.XMLUtil;

public class ProgramBuilder {

    List<Method> methods = new ArrayList<Method>();

    private static Map<Integer, String> responseCodes = loadResponseCodes();

    public static final String START = getProgramStart();

    public static final String END = ".END";

    public static final int START_LOCATION = 1;

    public static final int ERROR_LOCATION = 2;

    public static final int EXIT_LOCATION = 3;

    private static String getProgramStart() {
        StringBuilder build = new StringBuilder();
        build.append(".PROGRAM runner()\r");
        build.append("\tAUTO $text\r");
        build.append("\tAUTO $file.name\r\r");
        build.append("\tATTACH (slun, 4) \"SERIAL:1\"\r");
        build.append("\tIF IOSTAT(slun) < 0 GOTO " + ERROR_LOCATION + "\r");
        build.append("\tTYPE \"Serial Line Attached\"\r");
        build.append("\tWRITE(slun) \"204\"\r");
        return build.toString();
    }

    private static HashMap<Integer, String> loadResponseCodes() {
        HashMap<Integer, String> codes = new HashMap<Integer, String>();
        String uri = Launcher.root + "/lib/codes.xml";
        Document content = XMLUtil.getDocument(new File(uri));
        NodeList list = content.getElementsByTagName("code");
        for (int i = 0; i < list.getLength(); i++) {
            Element elem = (Element) list.item(i);
            String code = elem.getAttribute("digit");
            String value = elem.getAttribute("text");
            codes.put(Integer.parseInt(code), value);
            System.out.println("Added code: " + code + " ---> " + value);
        }
        return codes;
    }

    public ProgramBuilder() {
        setMethod("start", getDefaultRunner());
        setMethod("error", getErrorRunner());
        setMethod("exit", getExitRunner());
        setMethod("mon", getTypeRunner());
    }

    private String getTypeRunner() {
        String st = "\tTYPE $text\r";
        return st;
    }

    private String getErrorRunner() {
        String st = "\tTYPE IOSTAT(slun), \" \", $ERROR(IOSTAT(slun))\r";
        st += "\tWRITE(slun) \"401\"\r";
        return st;
    }

    private String getExitRunner() {
        return "\tTYPE \"Exiting Program\"\r" + "\tWRITE(slun) \"205\"\r";
    }

    private String getDefaultRunner() {
        StringBuilder st = new StringBuilder();
        st.append("\tTYPE \"Reading Serial Data\"\r");
        st.append("\tIF IOSTAT(slun) < 0 GOTO " + ERROR_LOCATION + "\r");
        st.append("\tREAD (slun) $text\r");
        st.append("\tTYPE \"Command Received: \" + $text\r");
        st.append("\tWRITE(slun) \"203\"\r");
        for (Method m : methods) {
            st.append("\t\tIF $text == \"" + m.name + "\" GOTO " + m.id + "\r");
        }
        st.append("\tWRITE(slun) \"402\"\r");
        return st.toString();
    }

    private String modContentForIds(String con) {
        Pattern p = Pattern.compile("GOTO [a-zA-Z]{1,}+");
        Matcher m = p.matcher(con);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            System.out.println(start + ", " + end);
            String sub = con.substring(start, end);
            String[] splitter = sub.split(" ");
            if (splitter.length >= 2) {
                String idSt = splitter[1];
                for (Method meth : methods) {
                    if (meth.name.equals(idSt)) {
                        idSt = meth.id + "";
                        break;
                    }
                }
                String sub2 = splitter[0] + " " + idSt;
                con = con.replaceAll(sub, sub2);
            }
        }
        return con;
    }

    public void setMethod(String name, String content) {
        content = modContentForIds(content);
        boolean found = false;
        for (Method m : methods) {
            if (m.name.equals(name)) {
                m.content = content;
                found = true;
            }
        }
        if (!found) {
            Method m = new Method();
            m.name = name;
            m.content = content;
            methods.add(m);
            int idx = 1;
            for (Method d : methods) {
                d.id = idx;
                idx++;
            }
            setMethod("start", getDefaultRunner());
        }
    }

    public String toString() {
        StringBuilder build = new StringBuilder();
        build.append(START);
        for (Method m : methods) build.append(m.toString());
        build.append(END);
        return build.toString();
    }

    private static class Method {

        private int id = 4;

        private String name = "";

        private String content = "";

        public String toString() {
            String st = id + " ;" + name + "\r";
            String[] splitter = content.split("\r");
            for (int i = 0; i < splitter.length; i++) {
                if (!splitter[i].startsWith("\t")) splitter[i] = "\t" + splitter[i];
                st += splitter[i] + "\r";
            }
            st += "\tGOTO " + START_LOCATION + "\r";
            return st;
        }
    }
}
