import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class LrnRead extends Matrix2D {

    private int[] column_type;

    private String[] column_name;

    private String comment = "";

    public LrnRead(int m, int n) {
        super(m, n);
        column_type = new int[n];
        column_name = new String[n];
    }

    public LrnRead(int m, int n, double value) {
        super(m, n, value);
        column_type = new int[n];
        column_name = new String[n];
        for (int i = 0; i < n; i++) {
            column_type[i] = 1;
            column_name[i] = "C" + i;
        }
    }

    public LrnRead(File file) {
        super(1, 1);
        try {
            FileReader fw = new FileReader(file);
            BufferedReader bw = new BufferedReader(fw);
            String buffer = "";
            boolean go_on = true;
            while (go_on) {
                buffer = bw.readLine();
                if (buffer.charAt(0) == '#') {
                    this.comment = this.comment + buffer.substring(1);
                } else {
                    go_on = false;
                }
            }
            StringTokenizer st = new StringTokenizer(buffer);
            st.nextToken();
            Integer m = Integer.valueOf(st.nextToken());
            setRows(m);
            buffer = bw.readLine();
            st = new StringTokenizer(buffer);
            st.nextToken();
            Integer n = Integer.valueOf(st.nextToken());
            setColumns(n);
            column_type = new int[n];
            column_name = new String[n];
            buffer = bw.readLine();
            st = new StringTokenizer(buffer);
            st.nextToken();
            for (int i = 0; st.hasMoreTokens(); i++) {
                column_type[i] = Integer.valueOf(st.nextToken());
            }
            buffer = bw.readLine();
            st = new StringTokenizer(buffer);
            st.nextToken();
            for (int i = 0; st.hasMoreTokens(); i++) {
                column_name[i] = st.nextToken();
            }
            for (int i = 0; i < m; i++) {
                buffer = bw.readLine();
                st = new StringTokenizer(buffer);
                double[] values = new double[st.countTokens()];
                st.nextToken();
                for (int j = 0; st.hasMoreTokens(); j++) {
                    values[j] = Double.valueOf(st.nextToken());
                }
                super.set(i, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
