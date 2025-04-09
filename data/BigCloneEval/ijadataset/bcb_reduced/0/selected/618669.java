package org.monet.backservice.control;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.monet.Monet;
import org.monet.kernel.agents.AgentLogger;

public class NodeProcessor {

    private HashMap<String, String> symbolsTable;

    public NodeProcessor() {
    }

    public String processNode(String sContent) {
        loadSymbolsTable();
        int index = 0;
        Pattern pattern = Pattern.compile("@.[^\",]*");
        Matcher matcher = pattern.matcher(sContent);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(sContent.substring(index, matcher.start()));
            String name = sContent.substring(matcher.start() + 1, matcher.end());
            sb.append(symbolsTable.get(name));
            index = matcher.end();
        }
        sb.append(sContent.substring(index));
        return sb.toString();
    }

    public String processCode(String name) {
        loadSymbolsTable();
        return symbolsTable.get(name);
    }

    private void loadSymbolsTable() {
        String symbolsTableFile = Monet.getInstance().getConfiguration().getBusinessModelDir() + "/symbolsTable.txt";
        try {
            FileInputStream inputStream = new FileInputStream(symbolsTableFile);
            BufferedReader data = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String pattern = "#";
            while (null != ((line = data.readLine()))) {
                String[] values = line.split(pattern);
                String code = values[0];
                String name = values[1];
                this.symbolsTable.put(name, code);
            }
        } catch (Exception e) {
            AgentLogger.getInstance().error(e);
        }
    }
}
