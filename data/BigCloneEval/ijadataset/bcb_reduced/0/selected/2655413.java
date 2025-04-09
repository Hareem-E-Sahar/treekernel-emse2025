package safcr.data;

import java.util.ArrayList;
import java.util.List;

public class CharacteristicData {

    private List<String> charsData;

    private String[] defaultCharacteristic = { "punchPower", "kickPower", "punchReach", "kickReach" };

    public int defaultValue = 5;

    private int minValue = 1;

    private int maxValue = 10;

    public CharacteristicData() {
        charsData = new ArrayList<String>();
        for (String s : defaultCharacteristic) {
            charsData.add(s);
        }
    }

    public List<String> getChars() {
        return charsData;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public int calculateSpeed(int pPower, int kPower, int pReach, int kReach) {
        int weight = (pPower + kPower) / 2;
        int height = (pReach + kReach) / 2;
        int speed = (int) ((height - weight) * 0.5);
        if (speed <= 0) speed = 1;
        return speed;
    }
}
