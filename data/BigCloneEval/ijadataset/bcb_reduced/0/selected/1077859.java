package mipt.util.decode;

import java.io.*;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Evdokimov
 */
public class UnicodeDecoder {

    public static void main(String[] args) {
        System.out.println("Input any text with Unicode symbols: \\u**** (or &#****; if the program started with &#; parameter). Type 'stop' to exit");
        System.out.println("If you want to read from and save to file, use < and > command line syntax");
        String unicode = "\\u";
        if (args.length > 0) unicode = args[0];
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Writer writer = new OutputStreamWriter(System.out);
            String delim = " ";
            Pattern pattern = Pattern.compile(delim);
            while (true) {
                String ss = reader.readLine();
                if (ss == null || "stop".equalsIgnoreCase(ss)) break;
                Matcher m = pattern.matcher(ss);
                int i = 0;
                while (m.find()) {
                    String s = ss.substring(i, m.start());
                    i = m.end();
                    decode(writer, s, unicode);
                    writer.write(delim);
                }
                if (i < ss.length()) decode(writer, ss.substring(i), unicode);
                writer.write("\r\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void decode(Writer writer, String s, String pattern) throws IOException {
        StringTokenizer st = new StringTokenizer(s, pattern, false);
        while (st.hasMoreTokens()) {
            s = st.nextToken();
            String end = null;
            if (s.length() > 4) {
                end = s.substring(4);
                s = s.substring(0, 4);
            }
            try {
                s = Character.toString((char) Integer.parseInt(s, 16));
            } catch (NumberFormatException e) {
            }
            writer.write(s);
            if (end != null) writer.write(end);
        }
    }
}
