import java.io.*;
import java.util.ArrayList;

public class GeoRetrieve {

    private String taxID;

    private FetchPage search = null;

    private QueryXML xmlFile = null;

    private String ret;

    ArrayList<Integer> idList = null;

    private String baseFetchLoc = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gds";

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

    public GeoRetrieve() {
        taxID = null;
    }

    public boolean searchForTaxonomyID(String ID) {
        assert (ID != null);
        String searchStr = baseFetchLoc;
        searchStr = searchStr.concat("&term=");
        searchStr = searchStr.concat(ID);
        searchStr = searchStr.concat("[Organism]&retmode=xml");
        try {
            search = new FetchPage(searchStr);
            ret = search.getPageContent();
            System.out.println(ret);
        } catch (Exception e) {
            System.err.println("Failed to fetch page");
            e.printStackTrace();
        }
        try {
            FileWriter fr = new FileWriter("/Users/aaronmckenna/Desktop/page_ret.txt");
            BufferedWriter br = new BufferedWriter(fr);
            br.write(ret, 0, ret.length());
            br.close();
            File f = new File("/Users/aaronmckenna/Desktop/page_ret.txt");
            this.xmlFile = new QueryXML(f);
            if (xmlFile.getIdCount() < 1) {
                return false;
            }
            this.idList = xmlFile.getIdList();
            if (idList != null) {
                for (int lpVal = 0; lpVal < idList.size(); lpVal++) {
                    GeoFTPFetch g = new GeoFTPFetch(idList.get(lpVal), "/Users/aaronmckenna/species/");
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Couldn't open file");
            e.printStackTrace();
        }
        return true;
    }
}
