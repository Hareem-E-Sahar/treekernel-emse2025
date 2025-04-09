import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TaxonomyRetrieve {

    private FetchPage search = null;

    private QueryXML xmlFile = null;

    private String ret;

    ArrayList<Integer> idList = null;

    private String baseFetchLoc = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=genome";

    public boolean uncompressFile(String tgzFileBase) {
        executeCommand("gunzip " + tgzFileBase + ".tgz");
        executeCommand("tar -xvf " + tgzFileBase + ".tar");
        return true;
    }

    private boolean executeCommand(String cmd) {
        String s = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            return true;
        } catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            return false;
        }
    }

    public TaxonomyRetrieve() {
    }

    public boolean searchForGenomeID(String ID) {
        assert (ID != null);
        String searchStr = baseFetchLoc;
        searchStr = searchStr.concat("&term=");
        searchStr = searchStr.concat(ID);
        searchStr = searchStr.concat("&retmode=xml");
        try {
            search = new FetchPage(searchStr);
            ret = search.getPageContent();
            System.out.println(ret);
        } catch (Exception e) {
            System.err.println("Failed to fetch page");
            e.printStackTrace();
        }
        try {
            FileWriter fr = new FileWriter("/Users/aaronmckenna/Desktop/page_ret2.txt");
            BufferedWriter br = new BufferedWriter(fr);
            br.write(ret, 0, ret.length());
            br.close();
        } catch (Exception e) {
            System.out.println("Couldn't open file");
            e.printStackTrace();
        }
        return true;
    }
}
