import java.io.*;
import java.util.*;

public class getSensus {

    /** Creates a new instance of getSensus */
    public getSensus() {
    }

    public static Set getSensiFromFile(String filename, Set sensi) throws Exception {
        Set ret = sensi;
        File file = new File(filename);
        if (file.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String inputline;
            while ((inputline = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(inputline);
                if (st.countTokens() > 0) {
                    while (st.hasMoreTokens()) {
                        String next = st.nextToken();
                        if (next.startsWith("(sensu")) {
                            String sensu = "";
                            while (sensu.indexOf(")") == -1) sensu = sensu + st.nextToken() + " ";
                            sensu = sensu.substring(0, sensu.length() - 2);
                            ret.add(sensu);
                        }
                    }
                }
            }
            in.close();
        } else System.out.println("Couldn't access file " + filename);
        return ret;
    }

    public static void main(String[] args) {
        try {
            Set sensi = new TreeSet();
            sensi = getSensiFromFile("/data/importdata/GO/flatfiles/component.ontology.txt", sensi);
            sensi = getSensiFromFile("/data/importdata/GO/flatfiles/function.ontology.txt", sensi);
            sensi = getSensiFromFile("/data/importdata/GO/flatfiles/process.ontology.txt", sensi);
            Object[] allSensi = sensi.toArray();
            for (int i = 0; i < allSensi.length; i++) System.out.println(allSensi[i]);
        } catch (Exception e) {
            System.out.println("Exception message: " + e.getMessage());
        }
    }
}
