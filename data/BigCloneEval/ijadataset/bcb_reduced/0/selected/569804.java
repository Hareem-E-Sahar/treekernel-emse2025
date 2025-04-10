package playground.scnadine.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

public class GPSFilterZGPS {

    /**
	 * @param args
	 * @throws IOException
	 */
    public static void main(String[] args) throws IOException {
        System.out.println("Start filtering zgps...");
        final Config config = ConfigUtils.loadConfig(args[0]);
        final String CONFIG_MODULE = "GPSFilterZGPS";
        File sourceFileSelectedStages = new File(config.findParam(CONFIG_MODULE, "sourceFileSelectedStages"));
        File sourceFileZGPS = new File(config.findParam(CONFIG_MODULE, "sourceFileZGPS"));
        File targetFile = new File(config.findParam(CONFIG_MODULE, "targetFile"));
        System.out.println("Start reading selected stages...");
        FilterZGPSSelectedStages selectedStages = new FilterZGPSSelectedStages();
        selectedStages.createSelectedStages(sourceFileSelectedStages);
        System.out.println("Done. " + selectedStages.getSelectedStages().size() + " stages were stored");
        System.out.println("Start reading and writing zgps...");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFileZGPS)));
            BufferedWriter out = new BufferedWriter(new FileWriter(targetFile));
            out.write(in.readLine());
            out.newLine();
            String lineFromInputFile;
            while ((lineFromInputFile = in.readLine()) != null) {
                String[] entries = lineFromInputFile.split("\t");
                if (selectedStages.containsStage(Integer.parseInt(entries[0]), Integer.parseInt(entries[1]), Integer.parseInt(entries[2]))) {
                    out.write(lineFromInputFile);
                    out.newLine();
                    out.flush();
                }
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find source file for selected stages.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception while reading or writing zgps.");
            e.printStackTrace();
        }
        System.out.println("Done.");
    }
}
