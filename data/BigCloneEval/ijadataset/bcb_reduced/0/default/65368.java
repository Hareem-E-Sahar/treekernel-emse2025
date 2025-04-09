import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

public class DocumentParser {

    private static int validDocumentNo = 1000000;

    public static String OUTPUTFOLDER = "C:\\Research\\Reloaded";

    public static int getDocumentNo() {
        return validDocumentNo++;
    }

    public static void parseFile(File file, int no) throws IOException {
        String line = "", token = "";
        BufferedReader bufferedReader = Helper.getBufferedReader(file);
        StringTokenizer tokenizer;
        int documentNo = 1;
        boolean isDocTagRead = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equals("<doc>") || isDocTagRead) {
                if (!isDocTagRead) line = bufferedReader.readLine(); else {
                    isDocTagRead = false;
                }
                tokenizer = new StringTokenizer(line, "<>");
                tokenizer.nextToken();
                documentNo = Integer.parseInt(tokenizer.nextToken());
                if (documentNo == 0) documentNo = getDocumentNo();
            }
            line = bufferedReader.readLine();
            FileWriter fstream = new FileWriter(OUTPUTFOLDER + "\\" + documentNo + ".txt");
            BufferedWriter out = new BufferedWriter(fstream);
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("</doc>") || line.equals("<doc>")) break;
                tokenizer = new StringTokenizer(line, "<>");
                if (!tokenizer.hasMoreTokens()) continue;
                tokenizer.nextToken();
                if (!tokenizer.hasMoreTokens()) continue;
                token = tokenizer.nextToken();
                token = token.replaceAll("\\p{Punct}", "");
                token = token.toLowerCase();
                System.out.println(token);
                out.write(token + "\n");
            }
            out.close();
            fstream.close();
            if (line == null) break;
            if (line.equals("<doc>")) isDocTagRead = true;
        }
        bufferedReader.close();
    }

    public static void execute(String documentFolderPath) throws IOException {
        int i, fcnt = 0;
        File documentFolder = new File(documentFolderPath);
        File[] fullFileList = documentFolder.listFiles();
        for (i = 0; i < fullFileList.length; i++) if (fullFileList[i].isFile()) fcnt++;
        File[] fileList = new File[fcnt];
        for (i = 0, fcnt = 0; i < fullFileList.length; i++) if (fullFileList[i].isFile()) fileList[fcnt++] = fullFileList[i];
        for (i = 0; i < fileList.length; i++) parseFile(fileList[i], i);
    }

    /**
	 * @throws IOException 
	 * @Input: Document folder that is in specified format.
	 * 
	 * It parses the given files into one sentence per line document format.
	 */
    public static void main(String[] args) throws IOException {
        String documentFolderPath = "C:\\Research\\data\\docBasedDataset\\texts";
        execute(documentFolderPath);
        System.out.println("Document Parser: FINISHED!!!");
    }
}
