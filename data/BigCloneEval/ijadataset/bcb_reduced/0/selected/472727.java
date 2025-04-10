package app.decorator;

import java.io.*;
import com.oozinoz.filter.TitleCaseFilter;
import com.oozinoz.filter.WrapFilter;

/**
 * Show filters that wrap and title-case the characters in a file supplied on
 * the command line.
 */
public class ShowFilters {

    public static void main(String args[]) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        Writer out = new FileWriter(args[1]);
        out = new WrapFilter(new BufferedWriter(out), 40);
        out = new TitleCaseFilter(out);
        String line;
        while ((line = in.readLine()) != null) out.write(line + "\n");
        out.close();
        in.close();
    }
}
