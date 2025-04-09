import java.io.*;
import java.text.*;
import java.util.*;
import java.lang.*;

public class WorldReader {

    World newWorld;

    public WorldReader(String worldFile, WIC WI) {
        String storeWorld = "";
        String temp = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(worldFile));
            while ((temp = in.readLine()) != null) {
                storeWorld = storeWorld + temp;
            }
        } catch (IOException e) {
        }
        char[] worldChar = storeWorld.toCharArray();
        int worldCounter = 0;
        while (worldCounter < storeWorld.length()) {
            char c = worldChar[worldCounter];
            if (c == '<') {
                String[] inputs = new String[3];
                int start = worldCounter + 1;
                int end = worldCounter;
                while (c != '>') {
                    end++;
                    c = worldChar[end];
                }
                String parseWorld = storeWorld.substring(start, end);
                StringTokenizer st = new StringTokenizer(parseWorld);
                String keyword = st.nextToken();
                int inputCounter = 0;
                while (st.hasMoreTokens()) {
                    inputs[inputCounter] = st.nextToken();
                    inputCounter++;
                }
                newWorld = findCommand(keyword, inputs, newWorld, WI);
                worldCounter = end + 1;
            } else {
                worldCounter++;
            }
        }
    }

    public World getWorld() {
        return newWorld;
    }

    public World findCommand(String s, String[] i, World w, WIC WI) {
        if (s.equals("Dimensions")) {
            int x = Integer.parseInt(i[0].substring(2));
            int y = Integer.parseInt(i[1].substring(2));
            w = new World(x, y);
        } else if (s.equals("Tile")) {
            int x = Integer.parseInt(i[0].substring(2));
            int y = Integer.parseInt(i[1].substring(2));
            double val = Double.parseDouble(i[2].substring(2));
            Random rand = new Random();
            boolean testID = true;
            while (testID) {
                int randID = rand.nextInt(10000);
                if (w.idAllowed(randID)) {
                    w.makeNode(randID, x, y, val);
                    testID = false;
                }
            }
        }
        return w;
    }
}
