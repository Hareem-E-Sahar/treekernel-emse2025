import java.io.*;
import java.util.*;

class TabFileHandler extends FileHandler {

    public TabFileHandler() {
        super("tabfile", "tab");
    }

    public void doLoad() throws IOException {
        throw new IOException("unsupported function");
    }

    public void load(String file) throws IOException {
        String id;
        int i;
        StringTokenizer t;
        String srcText = "";
        String s;
        String nonTrans = "";
        BufferedReader in = new BufferedReader(new FileReader(file));
        while ((s = in.readLine()) != null) {
            t = new StringTokenizer(s, "\t");
            if (t.countTokens() < 2) {
                nonTrans += s;
                continue;
            }
            id = t.nextToken();
            nonTrans += id + "\t";
            srcText = t.nextToken();
            if (m_outFile != null) {
                m_outFile.write(nonTrans);
                nonTrans = "";
            }
            processEntry(srcText, file);
            while (t.hasMoreTokens()) {
                nonTrans += "\t" + t.nextToken();
            }
        }
        if ((m_outFile != null) && (nonTrans.compareTo("") != 0)) {
            m_outFile.write(nonTrans);
        }
    }

    public static void main(String[] args) {
        TabFileHandler tab = new TabFileHandler();
        CommandThread.core = new CommandThread(null);
        tab.setTestMode(true);
        try {
            tab.load("src/glos1.txt");
        } catch (IOException e) {
            System.out.println("failed to open test file");
        }
    }
}
