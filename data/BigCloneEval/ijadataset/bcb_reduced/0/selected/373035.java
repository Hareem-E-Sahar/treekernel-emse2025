package org.eyewitness.hids.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vkorennoy
 */
public class StatisticsGatherer {

    public static double EVENT_RATE_DIFF_LIMIT = 5;

    public static boolean HAS_NEW_EVENTS = false;

    public static Vector<String> ANOMALOUS_SERVICES = new Vector<String>();

    public HashMap<String, Double> statistics = new HashMap<String, Double>();

    public HashMap<String, Double> prevStatistics = new HashMap<String, Double>();

    private HashMap<String, String> averageRateStatistics = new HashMap<String, String>();

    public HashMap<String, Long> rawStatistics = new HashMap<String, Long>();

    public HashMap<String, Double> rawAverageStatistics = new HashMap<String, Double>();

    public HashMap<String, Long> occursStatistics = new HashMap<String, Long>();

    public boolean isReady = false;

    private HashMap<String, Double> rawSumm = new HashMap<String, Double>();

    public long globalEventsNumber = 0;

    public StatisticsGatherer() {
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                isReady = false;
                getStatistics();
                isReady = true;
                Thread.sleep(30000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                isReady = false;
                break;
            }
        }
        isReady = false;
        return;
    }

    private String loadLogFile() {
        StringBuffer result = new StringBuffer();
        try {
            String line;
            Process p = Runtime.getRuntime().exec("eventviewer.exe");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                result.append(line + "\n");
                System.out.println(line);
            }
            input.close();
            return result.toString();
        } catch (Exception err) {
            err.printStackTrace();
            return "";
        }
    }

    public void getStatistics() {
        String stats = loadLogFile();
        parseStats(stats);
        updateAverageRates();
        isReady = true;
    }

    private void parseStats(String stats) {
        Matcher m = Pattern.compile("(.*)\\:(.*)\n").matcher(stats);
        globalEventsNumber = 0;
        while (m.find()) {
            String source = m.group(1);
            Long count = Long.parseLong(m.group(2));
            globalEventsNumber += count;
            rawStatistics.put(source, count);
        }
        statistics.clear();
        for (Entry<String, Long> entry : rawStatistics.entrySet()) {
            double rate = (double) entry.getValue() / (double) globalEventsNumber;
            statistics.put(entry.getKey(), rate);
        }
    }

    private void updateAverageRates() {
        boolean anomalityDetected = false;
        ANOMALOUS_SERVICES = new Vector<String>();
        for (Entry<String, Double> entry : statistics.entrySet()) {
            if (getAverageRateStatistics().containsKey(entry.getKey())) {
                Double prevRate = 0D;
                if (prevStatistics.containsKey(entry.getKey())) prevRate = prevStatistics.get(entry.getKey());
                Double newRate = entry.getValue();
                String f = "";
                String newAverageRate = String.valueOf(newRate);
                Double newRawRate = prevRate;
                if (!prevRate.equals(newRate)) {
                    long m = 2;
                    if (occursStatistics.containsKey(entry.getKey())) {
                        m = (long) occursStatistics.get(entry.getKey());
                    }
                    double rawSum = 0;
                    if (rawSumm.containsKey(entry.getKey())) {
                        rawSum = rawSumm.get(entry.getKey());
                    }
                    newRawRate = (newRate + rawSum) / m;
                    newAverageRate = String.valueOf(String.format("%.6f", newRawRate));
                    rawSumm.put(entry.getKey(), rawSum + newRawRate);
                    if (Math.abs(newRawRate - prevRate) >= ((EVENT_RATE_DIFF_LIMIT / 100) * prevRate)) {
                        anomalityDetected = true;
                        ANOMALOUS_SERVICES.add(entry.getKey());
                    }
                    Logger.getLogger("org.eyewitness.nids").log(Level.INFO, "source: " + entry.getKey() + " occursNumber=" + m + ". prevRate=" + prevRate + ". new rate:" + newRate + ". av: " + newAverageRate);
                    occursStatistics.put(entry.getKey(), ++m);
                }
                Matcher m = Pattern.compile("(\\d+\\......).*").matcher(newAverageRate);
                if (m.find()) {
                    f = m.group(1);
                } else {
                    f = newAverageRate;
                }
                getAverageRateStatistics().put(entry.getKey(), f);
                rawAverageStatistics.put(entry.getKey(), newRawRate);
            } else {
                String newAverageRate = String.valueOf(String.format("%.6f", (double) entry.getValue())).replace(",", ".");
                String f = "";
                Matcher m = Pattern.compile("(\\d+\\......).*").matcher(newAverageRate);
                if (m.find()) {
                    f = m.group(1);
                }
                getAverageRateStatistics().put(entry.getKey(), f);
                rawAverageStatistics.put(entry.getKey(), (double) entry.getValue());
                rawSumm.put(entry.getKey(), (double) entry.getValue());
            }
        }
        prevStatistics.putAll(rawAverageStatistics);
        if (anomalityDetected) HAS_NEW_EVENTS = true;
    }

    public HashMap<String, String> getAverageRateStatistics() {
        return averageRateStatistics;
    }

    public void setAverageRateStatistics(HashMap<String, String> averageRateStatistics) {
        this.averageRateStatistics = averageRateStatistics;
    }
}
