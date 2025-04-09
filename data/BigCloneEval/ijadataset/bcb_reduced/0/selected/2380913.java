package org.json.test.compliance;

import java.util.Random;
import org.json.test.compliance.helper.JSONArrayMetaImpl;
import org.json.test.compliance.helper.JSONObjectMetaImpl;
import org.json.test.compliance.std.JSONArrayMeta;
import org.json.test.compliance.std.JSONEncoder;
import org.json.test.compliance.std.JSONObjectMeta;

public class DataGenerator {

    static final int V_STRING = 0;

    static final int V_INT = 1;

    static final int V_DOUBLE = 2;

    static final int V_BOOLEAN = 3;

    static final int V_NULL = 4;

    static final int V_OBJECT = 5;

    static final int V_ARRAY = 6;

    /**
	 * Since there are lots of parameters, set it public for convenience.
	 */
    public int minLevel = 1;

    /**
	 * maxLevel is satisfied after minNodeCount.
	 */
    public int maxLevel = 40;

    /**
	 * minSizePerLevel is satisfied after maxNodeCount.
	 */
    public int minSizePerLevel = 0;

    public int maxSizePerLevel = 40;

    public int minStringLen = 0;

    public int maxStringLen = 80;

    public int minNodeCount = 1;

    public int maxNodeCount = 200;

    private Random random = new Random(System.currentTimeMillis());

    private int nodeCount = 0;

    private JSONEncoder riEncoder = new org.json.compliance.EncoderImpl();

    private static final char[] SPECIALS = { '\"', '\\', '/' };

    private void reset() {
        nodeCount = 0;
    }

    public String generateJSONString() {
        return riEncoder.toJSONString(generateGraph());
    }

    /**
	 * Generate graph of meta entities base on parameters.
	 * @return Instance of:
	 * 			org.json.test.compliance.std.JSONObjectMeta,
	 * 			org.json.test.compliance.std.JSONArrayMeta
	 */
    public Object generateGraph() {
        reset();
        return generateGraph(0);
    }

    private Object generateGraph(int parentLevel) {
        nodeCount++;
        if (random.nextBoolean()) return generateObjectMeta(parentLevel); else return generateArrayMeta(parentLevel);
    }

    private char generateControl() {
        return (char) random.nextInt(' ');
    }

    private char generateSpecial() {
        return SPECIALS[random.nextInt(SPECIALS.length)];
    }

    private char generatePrintable() {
        char ch = (char) (' ' + random.nextInt('' - ' ' + 1));
        return ch;
    }

    private char generateOther() {
        return (char) random.nextInt(Character.MAX_VALUE + 1);
    }

    private String generateString() {
        int len = minStringLen + random.nextInt(maxStringLen - minStringLen + 1);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char ch;
            int t = random.nextInt(82);
            switch(t) {
                case 0:
                    ch = generateControl();
                    break;
                case 1:
                    ch = generateSpecial();
                    if (ch == '/' && i < len - 1) {
                        sb.append('<');
                        i++;
                    }
                    break;
                default:
                    if (t < 41) ch = generatePrintable(); else ch = generateOther();
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private Long generateInt() {
        long value = random.nextLong();
        return value;
    }

    private Double generateDouble() {
        double value = random.nextDouble();
        long fractor = random.nextLong();
        if (random.nextBoolean() && fractor != 0) value *= fractor; else value /= fractor;
        if (random.nextBoolean()) return value; else return -1 * value;
    }

    private Boolean generateBoolean() {
        return random.nextBoolean();
    }

    private JSONObjectMeta generateObjectMeta(int parentLevel) {
        JSONObjectMetaImpl result = new JSONObjectMetaImpl();
        int size = minSizePerLevel + random.nextInt(maxSizePerLevel - minSizePerLevel + 1);
        if (nodeCount < minNodeCount && size == maxSizePerLevel) size--;
        for (int i = 0; i < size; i++) {
            if (nodeCount < maxNodeCount) result.getMap().put(generateString(), generateValue(parentLevel)); else break;
        }
        if (nodeCount < minNodeCount) {
            result.getMap().put(generateString(), generateGraph(parentLevel + 1));
        }
        return result;
    }

    private JSONArrayMeta generateArrayMeta(int parentLevel) {
        JSONArrayMetaImpl result = new JSONArrayMetaImpl();
        int size = minSizePerLevel + random.nextInt(maxSizePerLevel - minSizePerLevel + 1);
        if (nodeCount < minNodeCount && size == maxSizePerLevel) size--;
        for (int i = 0; i < size; i++) {
            if (nodeCount < maxNodeCount) result.getList().add(generateValue(parentLevel)); else break;
        }
        if (nodeCount < minNodeCount) result.getList().add(generateGraph(parentLevel + 1));
        return result;
    }

    private Object generateValue(int parentLevel) {
        int maxValueType = V_ARRAY;
        if (parentLevel >= maxLevel && nodeCount >= minNodeCount) maxValueType = V_NULL;
        int valueType = random.nextInt(maxValueType + 1);
        nodeCount++;
        switch(valueType) {
            case V_STRING:
                return generateString();
            case V_INT:
                return generateInt();
            case V_DOUBLE:
                return generateDouble();
            case V_BOOLEAN:
                return generateBoolean();
            case V_NULL:
                return null;
            case V_OBJECT:
                return generateObjectMeta(parentLevel + 1);
            case V_ARRAY:
                return generateArrayMeta(parentLevel + 1);
        }
        return null;
    }
}
