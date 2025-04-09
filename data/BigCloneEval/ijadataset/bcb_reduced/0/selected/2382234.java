package BacParser;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import BacJuxtaposer.*;
import AccordionBacDrawer.*;

/**
 * @author hilde
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BacLoader {

    boolean eof = false;

    int bacNum = 0;

    final int[] unsortedColumns = { 1, 2, 3, 4, 5, 6, 53, 54, 61, 62, 73 };

    final int length = unsortedColumns.length;

    String tableHead = "";

    public BacLoader(BacJuxtaposer bacJ, ArrayList filenames, int maxLoad) throws FileNotFoundException {
        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = (String) filenames.get(i);
                BufferedReader br;
                String line = "";
                String seqPart = "";
                Sample bacSample;
                String[] splits = filename.split(".txt");
                String sortedFile;
                if (filename.indexOf("sorted") != -1) sortedFile = filename; else sortedFile = splits[0] + "_sorted.txt";
                if (sortedFileExists(sortedFile)) {
                    br = new BufferedReader(new FileReader(sortedFile));
                    System.out.println("read sample information from a sorted file");
                    while (!eof) {
                        Bac bac = readSortedFile(filename, br);
                        bacJ.addBac(bac);
                    }
                    eof = false;
                } else {
                    br = new BufferedReader(new FileReader(filename));
                    while (!eof) {
                        Bac bac = readAndCreateBacs(filename, br);
                        bacJ.addBac(bac);
                    }
                    eof = false;
                }
            }
            bacJ.createABD();
        } catch (IOException e) {
            System.err.println("abort loading... " + e.getMessage());
        }
    }

    private Bac readSortedFile(String file, BufferedReader br) throws IOException {
        int[] sortedColumns = new int[length];
        for (int i = 0; i < length; i++) sortedColumns[i] = i + 1;
        getColumnNames(sortedColumns, br);
        TreeSet samples = getColumns(sortedColumns, br);
        return (new Bac(file, bacNum++, samples));
    }

    private boolean sortedFileExists(String file) {
        File sf = new File(file);
        return sf.exists();
    }

    void getColumnNames(int[] columns, BufferedReader br) throws IOException {
        String line = br.readLine();
        StringTokenizer bt = new StringTokenizer(line);
        int count = 1;
        int prev = 0;
        for (int i = 0; i < columns.length; i++) {
            String str = "";
            int steps = columns[i] - prev;
            for (int j = 0; j < steps; j++) {
                prev = columns[i];
                if (bt.hasMoreTokens()) str = bt.nextToken("\t"); else return;
            }
            tableHead = tableHead + str + "\t";
            System.out.println("column " + str);
        }
    }

    void parseSpecialCase(int[] columns, String str) {
        int j = 0;
        String values[] = new String[columns.length];
        int counter = 0;
        for (int i = 0; i < columns.length; i++) {
            String tmp = "";
            while (counter != columns[i]) {
                counter++;
                while (str.charAt(j) != '\t') {
                    if (counter == columns[i]) tmp += str.substring(j, ++j); else j++;
                }
                j++;
            }
            if (tmp == "") tmp = "-1";
            values[i] = tmp;
        }
    }

    TreeSet getColumns(int[] columns, BufferedReader br) throws IOException {
        int key = 0;
        String line;
        TreeSet samples = new TreeSet();
        while ((line = br.readLine()) != null) {
            while (line.length() == 0) {
                line = br.readLine();
                if (line == null) {
                    eof = true;
                    break;
                }
            }
            if (eof) break;
            String values[] = new String[columns.length];
            StringTokenizer bt = new StringTokenizer(line);
            boolean eol = false;
            if (line.lastIndexOf("\t\t") != -1) continue;
            int helper = 0;
            int helper2 = 0;
            while (helper < 2) {
                if (line.charAt(helper2++) == '\t') helper++;
            }
            if (line.charAt(helper2) == '0') {
                continue;
            }
            int count = 1;
            int prev = 0;
            for (int i = 0; i < columns.length; i++) {
                String str = "";
                int steps = columns[i] - prev;
                for (int j = 0; j < steps; j++) {
                    prev = columns[i];
                    if (bt.hasMoreTokens()) {
                        str = bt.nextToken();
                    } else {
                        eol = true;
                        break;
                    }
                }
                if (!eol) {
                    values[i] = str;
                } else {
                    break;
                }
            }
            Sample newSample = new Sample(values, key);
            if (newSample.getGeneStart() > 0 && newSample.getGeneEnd() > 0) {
                samples.add(newSample);
                key++;
            }
        }
        eof = true;
        return samples;
    }

    private int getChromosomPositionInArray(ArrayList s, String[] values) {
        int low = 0;
        int high = s.size() - 1;
        int pos = low;
        int tmpChr;
        String c = values[2];
        if (c.equalsIgnoreCase("X")) tmpChr = Sample.X; else if (c.equalsIgnoreCase("Y")) tmpChr = Sample.Y; else tmpChr = (new Integer(c)).intValue();
        tmpChr--;
        while (low <= high) {
            int mid = (low + high) / 2;
            int chromosome = ((Sample) s.get(mid)).getChromosomeID() - 1;
            if (chromosome > tmpChr) {
                high = mid - 1;
            } else if (chromosome < tmpChr) {
                low = mid + 1;
                pos = low;
            } else {
                pos = mid;
                break;
            }
        }
        if (s.size() != 0 && pos > 0) {
            while (pos < s.size() && pos > 0 && ((Sample) s.get(pos - 1)).getChromosomeID() - 1 == tmpChr) pos--;
            while (pos < s.size() && ((Sample) s.get(pos)).getChromosomeID() - 1 == tmpChr) {
                int v = Integer.parseInt(values[3]);
                int map = ((Sample) s.get(pos)).getGeneStart();
                if (v > map) pos++; else return pos;
            }
        }
        return pos;
    }

    private int getPositionInArray(ArrayList s, String[] values) {
        int low = 0;
        int high = s.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int v = Integer.parseInt(values[3]);
            int map = ((Sample) s.get(mid)).getGeneMap();
            if (map > v) high = mid - 1; else if (map < v) low = mid + 1; else return mid;
        }
        return low;
    }

    Bac readAndCreateBacs(String filename, BufferedReader br) throws IOException {
        getColumnNames(unsortedColumns, br);
        TreeSet samples = getColumns(unsortedColumns, br);
        writeToSortedFile(samples, filename);
        return (new Bac(filename, bacNum++, samples));
    }

    private void writeToSortedFile(TreeSet s, String filename) {
        try {
            String[] splits = filename.split(".txt");
            String newFilename = splits[0] + "_sorted.txt";
            System.out.println("write sorted information to file " + newFilename);
            BufferedWriter out = new BufferedWriter(new FileWriter(newFilename));
            out.write(tableHead + "\n");
            Iterator iter = s.iterator();
            while (iter.hasNext()) {
                Sample sample = (Sample) iter.next();
                out.write(sample.toString() + "\n");
            }
            out.close();
        } catch (IOException e) {
            System.err.println("abort writing to file... " + e.getMessage());
        }
    }

    ArrayList removeDoubledSamples(ArrayList s) {
        Sample tempMax;
        for (int i = 0; i < s.size() - 1; i++) {
            Sample now = (Sample) s.get(i);
            Sample next = (Sample) s.get(i + 1);
            if (now.getGeneMap() == next.getGeneMap() && now.getGeneSymbol() == next.getGeneSymbol()) {
                if (now.getLogRatio685() > next.getLogRatio685()) s.remove(next); else s.remove(now);
                i--;
            }
        }
        return s;
    }
}
