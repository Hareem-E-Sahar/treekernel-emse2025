import java.io.*;
import java.util.*;

class LTabFileReader {

    public LTabFileReader() {
        m_line = new ArrayList();
    }

    public void write(String file) throws IOException {
        int i;
        String str;
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        for (i = 0; i < m_line.size(); i++) {
            str = (String) m_line.get(i);
            out.write(str, 0, str.length());
            out.newLine();
        }
        out.close();
    }

    public void reset() {
        m_line.clear();
    }

    public void addLine(String str) {
        m_line.add(str);
    }

    public void load(File file) throws IOException {
        load(file.getAbsolutePath());
    }

    public void load(String file) throws IOException {
        String s;
        String z;
        String token;
        StringTokenizer t;
        int i;
        BufferedReader in = new BufferedReader(new FileReader(file));
        ArrayList tokenList = new ArrayList();
        while ((s = in.readLine()) != null) {
            if (s.startsWith("#") == true) continue;
            t = new StringTokenizer(s, "\t");
            while (t.hasMoreTokens()) {
                z = t.nextToken();
                tokenList.add(z);
            }
            for (i = 0; i < tokenList.size(); i++) {
                token = (String) tokenList.get(i);
                if (token.length() != 0) {
                    break;
                }
            }
            if (i < tokenList.size()) {
                m_line.add(tokenList);
                tokenList = new ArrayList();
            } else {
                tokenList.clear();
            }
        }
        in.close();
    }

    public String get(int row, int col) {
        String retVal = "";
        if (row < m_line.size()) {
            ArrayList tokenList = (ArrayList) m_line.get(row);
            if (col < tokenList.size()) {
                retVal = (String) tokenList.get(col);
            }
        }
        return retVal;
    }

    public int numRows() {
        return m_line.size();
    }

    public int numCols(int row) {
        if (row < m_line.size()) {
            ArrayList tokenList = (ArrayList) m_line.get(row);
            return tokenList.size();
        } else return 0;
    }

    private ArrayList m_line;

    public static void main(String[] args) {
        LTabFileReader rdr = new LTabFileReader();
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(".tfr.tmp"));
            out.println("abc");
            out.println("def\tghi");
            out.println("jkl");
            out.println("");
            out.println("mno\tpqr");
            out.println("stu\tvwx\tyz-");
            out.println("ABC\tDEF");
            out.println("GHI");
            out.println("");
            out.println("JKL");
            out.println("MNO\tPQR");
            out.println("STU\tVWX\tYZ_");
            out.close();
            rdr.load(".tfr.tmp");
            int r = rdr.numRows();
            String s;
            int c;
            for (int i = 0; i < r; i++) {
                c = rdr.numCols(i);
                for (int j = 0; j < c; j++) {
                    if (j > 0) System.out.print("\t");
                    s = rdr.get(i, j);
                    System.out.print(s);
                }
                System.out.println("");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
